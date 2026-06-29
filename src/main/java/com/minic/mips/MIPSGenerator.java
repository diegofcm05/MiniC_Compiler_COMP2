package com.minic.mips;

import com.minic.Scope;
import com.minic.Symbol;
import com.minic.SymbolTable;
import com.minic.ir.Instruccion;
import com.minic.ir.OpTAC;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Backend MIPS32 (ABI O32) — traduce el TAC producido por TACGenerator a
 * código ensamblador para QtSPIM/MARS.
 *
 * ALCANCE ACTUAL (Fase 4 — M1 a M5, completo): prólogo/epílogo de función,
 * código lineal (ASIGNAR/BINARIA/UNARIA), RETURN, control de flujo completo,
 * llamadas a runtime de E/S y a funciones definidas por el usuario
 * (convención de llamada ABI O32 completa), arreglos (locales, globales, y
 * por referencia), direcciones (ADDR — variables, elementos de arreglo, y
 * el decaimiento de un arreglo al pasarlo como argumento), y punteros
 * (PTR_LOAD/PTR_STORE).
 *
 * LIMITACIONES CONOCIDAS (no son hitos pendientes, son casos de borde
 * documentados y deliberadamente fuera de alcance por ahora):
 *   - Globales con inicializador ('int x = 5;' a nivel global): TACGenerator
 *     no genera TAC para esto (ver su Javadoc), así que el valor inicial
 *     nunca llega a este backend — toda global se emite sin inicializar.
 *   - Colisión de nombres: las etiquetas de control (L1, L2...) y los
 *     nombres de función comparten un solo espacio de nombres en MIPS; si
 *     una función se llamara literalmente "L7" habría colisión —
 *     astronómicamente improbable en la práctica.
 *
 * DECISIONES DE DISEÑO (ver diagrama de stack frame en la conversación):
 *   - Política "todo al stack": cada variable local, parámetro y temporal
 *     del TAC tiene su propio slot de 4 bytes en el marco. $t0-$t9 son
 *     puramente scratch transitorio; $s0-$s7 no se usan.
 *   - $fp apunta a la BASE del marco local (offsets positivos pequeños).
 *   - Todo elemento de arreglo ocupa 4 bytes, incluso char/bool (Mini-C no
 *     tiene aritmética de punteros ni sizeof, así que esto es invisible
 *     para cualquier programa válido — no es una aproximación, es exacto
 *     para este lenguaje).
 */
public class MIPSGenerator {

    private static final Set<String> FUNCIONES_RUNTIME_IO = Set.of(
            "print_int", "print_char", "print_bool", "print_str", "println",
            "read_int", "read_char", "read_str");

    private final List<Instruccion> tac;
    private final SymbolTable tabla;

    private final StringBuilder data = new StringBuilder();
    private final StringBuilder text = new StringBuilder();

    private final Map<String, String> etiquetasLiteral = new LinkedHashMap<>();
    private int contadorLiteral = 0;

    // Estado de la función que se está emitiendo en este momento.
    private Map<String, Integer> offsets;
    private int tamanioMarco;
    private String etiquetaEpilogoActual;
    private final List<String> paramsPendientes = new ArrayList<>();
    // Pares [offsetEnElMarco, índiceDelParámetro] — qué parámetros hay que
    // copiar a su slot local al entrar a la función (ver emitirPrologo).
    private List<int[]> copiasParametros;
    // Símbolo de cada nombre local de la función actual — lo necesita
    // cargarDireccionBaseArreglo para distinguir un arreglo local real
    // (su slot ES el arreglo) de un arreglo-parámetro (su slot guarda una
    // dirección recibida por referencia, hay que leerla, no calcularla).
    private Map<String, Symbol> simbolosPorNombre;

    public MIPSGenerator(List<Instruccion> tac, SymbolTable tabla) {
        this.tac = tac;
        this.tabla = tabla;
    }

    public String generar() {
        recolectarLiterales();
        emitirSeccionDatos();
        emitirFunciones();

        StringBuilder out = new StringBuilder();
        out.append(".data\n").append(data).append("\n.text\n").append(text);
        return out.toString();
    }

    // ───────────────────────── sección .data ──────────────────────────

    private void recolectarLiterales() {
        for (Instruccion ins : tac) {
            recolectarSiEsLiteral(ins.op1);
            recolectarSiEsLiteral(ins.op2);
        }
    }

    private void recolectarSiEsLiteral(String operando) {
        if (operando != null && operando.length() >= 2
                && operando.charAt(0) == '"' && operando.charAt(operando.length() - 1) == '"'
                && !etiquetasLiteral.containsKey(operando)) {
            etiquetasLiteral.put(operando, "str" + (contadorLiteral++));
        }
    }

    private void emitirSeccionDatos() {
        Scope global = tabla.getTodos().get(0);
        for (Symbol s : global.getSimbolos().values()) {
            if (s.categoria.equals("funcion")) continue;
            if (s.categoria.equals("arreglo")) {
                data.append(s.nombre).append(": .space ").append(tamanioEnBytes(s)).append("\n");
            } else {
                data.append(s.nombre).append(": .word 0\n");
            }
        }
        for (Map.Entry<String, String> e : etiquetasLiteral.entrySet()) {
            String textoCrudo = e.getKey().substring(1, e.getKey().length() - 1);
            data.append(e.getValue()).append(": .asciiz \"").append(textoCrudo).append("\"\n");
        }
    }

    private int tamanioEnBytes(Symbol s) {
        int elementos = 1;
        if (s.tamanios != null) {
            for (int d : s.tamanios) elementos *= Math.max(d, 1);
        }
        return elementos * 4;
    }

    // ───────────────────────── funciones ──────────────────────────

    private void emitirFunciones() {
        int i = 0;
        while (i < tac.size()) {
            Instruccion ins = tac.get(i);
            if (ins.operador == OpTAC.FUNC_INICIO) {
                int fin = buscarFin(i);
                emitirFuncion(ins.destino, tac.subList(i + 1, fin));
                i = fin + 1;
            } else {
                i++;
            }
        }
    }

    private int buscarFin(int inicio) {
        for (int j = inicio + 1; j < tac.size(); j++) {
            if (tac.get(j).operador == OpTAC.FUNC_FIN) return j;
        }
        throw new IllegalStateException("FUNCTION sin END FUNCTION correspondiente");
    }

    private void emitirFuncion(String nombreFuncion, List<Instruccion> cuerpo) {
        construirMarco(nombreFuncion, cuerpo);
        etiquetaEpilogoActual = "_epilogo_" + nombreFuncion;

        text.append(nombreFuncion).append(":\n");
        emitirPrologo();

        for (Instruccion ins : cuerpo) {
            emitirInstruccion(ins);
        }

        text.append(etiquetaEpilogoActual).append(":\n");
        emitirEpilogo();
    }

    // ───────────────────────── marco de pila ──────────────────────────

    private void construirMarco(String nombreFuncion, List<Instruccion> cuerpo) {
        Set<String> nombres = new LinkedHashSet<>();
        simbolosPorNombre = new LinkedHashMap<>();

        Scope scopeFuncion = buscarScopeDeFuncion(nombreFuncion);
        for (Symbol s : recolectarSimbolosLocales(scopeFuncion)) {
            nombres.add(s.nombre);
            simbolosPorNombre.put(s.nombre, s);
        }

        for (Instruccion ins : cuerpo) {
            agregarSiEsTemporal(ins.destino, nombres);
            agregarSiEsTemporal(ins.op1, nombres);
            agregarSiEsTemporal(ins.op2, nombres);
        }

        offsets = new LinkedHashMap<>();
        copiasParametros = new ArrayList<>();
        int offset = 0;
        for (String nombre : nombres) {
            Symbol s = simbolosPorNombre.get(nombre);
            int bytes;
            if (s == null) {
                bytes = 4; // temporal del TAC
            } else if (s.categoria.equals("arreglo") && s.indiceParametro >= 0) {
                // Un parámetro-arreglo decae a dirección en la llamada (ver
                // TACGenerator.direccionDe) — su slot solo guarda ESA
                // dirección, no el arreglo completo.
                bytes = 4;
            } else {
                bytes = tamanioEnBytes(s);
            }
            offsets.put(nombre, offset);
            if (s != null && s.indiceParametro >= 0) {
                copiasParametros.add(new int[]{offset, s.indiceParametro});
            }
            offset += bytes;
        }
        tamanioMarco = alinearA8(offset + 8); // +8 = $ra y $fp guardados
    }

    private void agregarSiEsTemporal(String operando, Set<String> nombres) {
        if (operando != null && operando.matches("t\\d+")) {
            nombres.add(operando);
        }
    }

    private Scope buscarScopeDeFuncion(String nombreFuncion) {
        Scope global = tabla.getTodos().get(0);
        for (Scope s : tabla.getTodos()) {
            if (s.nombre.equals(nombreFuncion) && s.padre == global) {
                return s;
            }
        }
        throw new IllegalStateException("No se encontró el scope de la función '" + nombreFuncion + "'");
    }

    private List<Symbol> recolectarSimbolosLocales(Scope scopeFuncion) {
        List<Symbol> locales = new ArrayList<>();
        for (Scope s : tabla.getTodos()) {
            if (esDescendienteOIgual(s, scopeFuncion)) {
                locales.addAll(s.getSimbolos().values());
            }
        }
        return locales;
    }

    private boolean esDescendienteOIgual(Scope s, Scope ancestro) {
        for (Scope actual = s; actual != null; actual = actual.padre) {
            if (actual == ancestro) return true;
        }
        return false;
    }

    private int alinearA8(int n) {
        return (n % 8 == 0) ? n : n + (8 - n % 8);
    }

    private void emitirPrologo() {
        text.append("    subu $sp, $sp, ").append(tamanioMarco).append("\n");
        text.append("    sw $ra, ").append(tamanioMarco - 4).append("($sp)\n");
        text.append("    sw $fp, ").append(tamanioMarco - 8).append("($sp)\n");
        text.append("    move $fp, $sp\n");

        // Copiar cada parámetro recibido a su slot local uniforme — los
        // primeros 4 llegan en $a0-$a3, el resto en el stack del llamador
        // (justo arriba de este marco, en $fp + tamanioMarco + ...).
        for (int[] copia : copiasParametros) {
            int offsetLocal = copia[0];
            int indice = copia[1];
            if (indice < 4) {
                text.append("    sw $a").append(indice).append(", ").append(offsetLocal).append("($fp)\n");
            } else {
                text.append("    lw $t9, ").append(tamanioMarco + (indice - 4) * 4).append("($fp)\n");
                text.append("    sw $t9, ").append(offsetLocal).append("($fp)\n");
            }
        }
    }

    private void emitirEpilogo() {
        text.append("    lw $ra, ").append(tamanioMarco - 4).append("($sp)\n");
        text.append("    lw $fp, ").append(tamanioMarco - 8).append("($sp)\n");
        text.append("    addu $sp, $sp, ").append(tamanioMarco).append("\n");
        text.append("    jr $ra\n");
    }

    // ───────────────────────── instrucciones ──────────────────────────

    private void emitirInstruccion(Instruccion ins) {
        switch (ins.operador) {
            case ASIGNAR:
                cargarOperando(ins.op1, "$t0");
                guardarEn(ins.destino, "$t0");
                break;

            case BINARIA:
                cargarOperando(ins.op1, "$t0");
                cargarOperando(ins.op2, "$t1");
                emitirOperadorBinario(ins.simbolo, "$t2", "$t0", "$t1");
                guardarEn(ins.destino, "$t2");
                break;

            case UNARIA:
                cargarOperando(ins.op1, "$t0");
                if (ins.simbolo.equals("-")) {
                    text.append("    neg $t1, $t0\n");
                } else { // "!"  →  !x  <=>  x == 0  (bool es siempre 0/1)
                    text.append("    seq $t1, $t0, 0\n");
                }
                guardarEn(ins.destino, "$t1");
                break;

            case RETURN:
                if (ins.op1 != null) {
                    cargarOperando(ins.op1, "$v0");
                }
                text.append("    j ").append(etiquetaEpilogoActual).append("\n");
                break;

            case PARAM:
                paramsPendientes.add(ins.op1);
                break;

            case CALL:
            case CALL_VOID:
                emitirLlamada(ins);
                break;

            case GOTO:
                text.append("    j ").append(ins.destino).append("\n");
                break;

            case IF_TRUE:
                cargarOperando(ins.op1, "$t0");
                text.append("    bne $t0, $zero, ").append(ins.destino).append("\n");
                break;

            case IF_FALSE:
                cargarOperando(ins.op1, "$t0");
                text.append("    beq $t0, $zero, ").append(ins.destino).append("\n");
                break;

            case ETIQUETA:
                text.append(ins.destino).append(":\n");
                break;

            case ARR_LOAD:
                cargarDireccionDeNombre(ins.op1, "$t0");
                cargarOperando(ins.op2, "$t1");
                text.append("    sll $t1, $t1, 2\n"); // índice * 4 (bytes por elemento)
                text.append("    addu $t0, $t0, $t1\n");
                text.append("    lw $t2, 0($t0)\n");
                guardarEn(ins.destino, "$t2");
                break;

            case ARR_STORE:
                cargarDireccionDeNombre(ins.destino, "$t0");
                cargarOperando(ins.op1, "$t1");
                text.append("    sll $t1, $t1, 2\n");
                text.append("    addu $t0, $t0, $t1\n");
                cargarOperando(ins.op2, "$t2");
                text.append("    sw $t2, 0($t0)\n");
                break;

            case ADDR:
                cargarDireccionDeNombre(ins.op1, "$t0");
                if (ins.op2 != null) {
                    cargarOperando(ins.op2, "$t1");
                    text.append("    sll $t1, $t1, 2\n");
                    text.append("    addu $t0, $t0, $t1\n");
                }
                guardarEn(ins.destino, "$t0");
                break;

            case PTR_LOAD:
                cargarOperando(ins.op1, "$t0"); // valor del puntero = una dirección
                text.append("    lw $t1, 0($t0)\n");
                guardarEn(ins.destino, "$t1");
                break;

            case PTR_STORE:
                cargarOperando(ins.destino, "$t0"); // ins.destino ES el puntero aquí (ver convención de OpTAC)
                cargarOperando(ins.op1, "$t1");     // valor a escribir
                text.append("    sw $t1, 0($t0)\n");
                break;

            default:
                throw new IllegalStateException("Instrucción TAC no reconocida: " + ins.operador);
        }
    }

    private void emitirOperadorBinario(String op, String d, String a, String b) {
        switch (op) {
            case "+": text.append("    add ").append(d).append(", ").append(a).append(", ").append(b).append("\n"); break;
            case "-": text.append("    sub ").append(d).append(", ").append(a).append(", ").append(b).append("\n"); break;
            case "*": text.append("    mul ").append(d).append(", ").append(a).append(", ").append(b).append("\n"); break;
            case "/": text.append("    div ").append(d).append(", ").append(a).append(", ").append(b).append("\n"); break;
            case "%":
                text.append("    div ").append(a).append(", ").append(b).append("\n");
                text.append("    mfhi ").append(d).append("\n");
                break;
            case "<":  text.append("    slt ").append(d).append(", ").append(a).append(", ").append(b).append("\n"); break;
            case ">":  text.append("    sgt ").append(d).append(", ").append(a).append(", ").append(b).append("\n"); break;
            case "<=": text.append("    sle ").append(d).append(", ").append(a).append(", ").append(b).append("\n"); break;
            case ">=": text.append("    sge ").append(d).append(", ").append(a).append(", ").append(b).append("\n"); break;
            case "==": text.append("    seq ").append(d).append(", ").append(a).append(", ").append(b).append("\n"); break;
            case "!=": text.append("    sne ").append(d).append(", ").append(a).append(", ").append(b).append("\n"); break;
            case "&&": text.append("    and ").append(d).append(", ").append(a).append(", ").append(b).append("\n"); break;
            case "||": text.append("    or ").append(d).append(", ").append(a).append(", ").append(b).append("\n"); break;
            default: throw new IllegalStateException("Operador binario no reconocido: " + op);
        }
    }

    // ───────────────────────── llamadas a runtime (M1) ──────────────────────────

    private void emitirLlamada(Instruccion ins) {
        String nombreFuncion = ins.op1;
        if (FUNCIONES_RUNTIME_IO.contains(nombreFuncion)) {
            emitirLlamadaRuntime(nombreFuncion, ins.destino);
        } else {
            emitirLlamadaUsuario(ins);
        }
        paramsPendientes.clear();
    }

    private static final String[] REGISTROS_ARG = {"$a0", "$a1", "$a2", "$a3"};

    private void emitirLlamadaUsuario(Instruccion ins) {
        String nombreFuncion = ins.op1;
        int n = paramsPendientes.size();
        int extra = Math.max(0, n - 4);
        int bytesExtra = alinearA8(extra * 4);

        // Argumentos extra (5°, 6°...) al stack, ANTES de cargar $a0-$a3 —
        // así no hay riesgo de que el cálculo de un argumento posterior
        // pise un registro $aN ya cargado por uno anterior.
        if (extra > 0) {
            text.append("    subu $sp, $sp, ").append(bytesExtra).append("\n");
            for (int i = 4; i < n; i++) {
                cargarOperando(paramsPendientes.get(i), "$t9");
                text.append("    sw $t9, ").append((i - 4) * 4).append("($sp)\n");
            }
        }

        for (int i = 0; i < Math.min(4, n); i++) {
            cargarOperando(paramsPendientes.get(i), REGISTROS_ARG[i]);
        }

        text.append("    jal ").append(nombreFuncion).append("\n");

        if (extra > 0) {
            text.append("    addu $sp, $sp, ").append(bytesExtra).append("\n");
        }

        if (ins.operador == OpTAC.CALL) {
            guardarEn(ins.destino, "$v0");
        }
    }

    private void emitirLlamadaRuntime(String nombre, String destino) {
        switch (nombre) {
            case "print_int":
            case "print_bool": // bool se imprime como 0/1 — mismo syscall que int
                cargarOperando(paramsPendientes.get(0), "$a0");
                text.append("    li $v0, 1\n    syscall\n");
                break;
            case "print_char":
                cargarOperando(paramsPendientes.get(0), "$a0");
                text.append("    li $v0, 11\n    syscall\n");
                break;
            case "print_str":
                emitirDireccionDeString(paramsPendientes.get(0), "$a0");
                text.append("    li $v0, 4\n    syscall\n");
                break;
            case "println":
                text.append("    li $a0, 10\n    li $v0, 11\n    syscall\n");
                break;
            case "read_int":
                text.append("    li $v0, 5\n    syscall\n");
                guardarEn(destino, "$v0");
                break;
            case "read_char":
                text.append("    li $v0, 12\n    syscall\n");
                guardarEn(destino, "$v0");
                break;
            case "read_str":
                // void read_str(char* buf, int maxlen) — los dos argumentos
                // ya llegaron por 'param' en orden; el primero es la
                // dirección del buffer del llamador (un arreglo decae a su
                // dirección automáticamente, igual que en cualquier otra
                // llamada — ver TACGenerator.direccionDe).
                cargarOperando(paramsPendientes.get(0), "$a0");
                cargarOperando(paramsPendientes.get(1), "$a1");
                text.append("    li $v0, 8\n    syscall\n");
                break;
            default:
                throw new UnsupportedOperationException(
                        "Runtime '" + nombre + "' pendiente (read_str necesita buffer en .data).");
        }
    }

    private void emitirDireccionDeString(String operando, String registro) {
        if (operando.startsWith("\"")) {
            text.append("    la ").append(registro).append(", ")
                    .append(etiquetasLiteral.get(operando)).append("\n");
        } else {
            // Variable que ya guarda una dirección de cadena (ej. el
            // resultado de read_str, o un parámetro string) — su valor
            // YA ES la dirección, basta con cargarlo normalmente.
            cargarOperando(operando, registro);
        }
    }

    // ───────────────────────── acceso a operandos/variables ──────────────────────────

    private void cargarOperando(String operando, String registro) {
        if (operando == null) {
            throw new IllegalStateException("Operando nulo inesperado");
        } else if (operando.equals("true")) {
            text.append("    li ").append(registro).append(", 1\n");
        } else if (operando.equals("false")) {
            text.append("    li ").append(registro).append(", 0\n");
        } else if (operando.matches("-?\\d+")) {
            text.append("    li ").append(registro).append(", ").append(operando).append("\n");
        } else if (operando.length() >= 3 && operando.charAt(0) == '\''
                && operando.charAt(operando.length() - 1) == '\'') {
            // NOTA (pendiente): no maneja secuencias de escape ('\n', '\t'...)
            // todavía — solo el caso de un carácter literal simple.
            char c = operando.charAt(1);
            text.append("    li ").append(registro).append(", ").append((int) c).append("\n");
        } else if (operando.startsWith("\"")) {
            throw new IllegalStateException("No se puede cargar un literal de cadena como valor escalar: " + operando);
        } else {
            text.append("    lw ").append(registro).append(", ").append(direccionDeVariable(operando)).append("\n");
        }
    }

    private void guardarEn(String nombre, String registro) {
        text.append("    sw ").append(registro).append(", ").append(direccionDeVariable(nombre)).append("\n");
    }

    private String direccionDeVariable(String nombre) {
        if (offsets.containsKey(nombre)) {
            return offsets.get(nombre) + "($fp)";
        }
        return nombre; // global: el ensamblador resuelve la etiqueta directamente
    }

    /** Carga en 'registro' la dirección de 'nombre' como un VALOR (para
     *  sumarle un índice, o para guardarla en un puntero) — distinto de
     *  direccionDeVariable, que da un operando de memoria para lw/sw
     *  directo. Tres casos:
     *   - global: la registro, nombre
     *   - local real, o parámetro escalar/puntero (su slot ES el valor):
     *     addu registro, $fp, offset
     *   - parámetro-ARREGLO (decae a dirección en la llamada — ver
     *     TACGenerator.direccionDe — su slot GUARDA esa dirección, no es
     *     el arreglo): lw registro, offset($fp) — hay que LEER el valor. */
    private void cargarDireccionDeNombre(String nombre, String registro) {
        if (!offsets.containsKey(nombre)) {
            text.append("    la ").append(registro).append(", ").append(nombre).append("\n");
            return;
        }
        Symbol s = simbolosPorNombre.get(nombre);
        int offset = offsets.get(nombre);
        boolean esParametroArreglo = s != null && s.categoria.equals("arreglo") && s.indiceParametro >= 0;
        if (esParametroArreglo) {
            text.append("    lw ").append(registro).append(", ").append(offset).append("($fp)\n");
        } else {
            text.append("    addu ").append(registro).append(", $fp, ").append(offset).append("\n");
        }
    }
}