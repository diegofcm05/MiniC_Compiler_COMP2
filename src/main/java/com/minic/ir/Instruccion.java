package com.minic.ir;

/**
 * Una instrucción de código de tres direcciones (TAC).
 *
 * Para mantener el diseño simple, todos los operandos se representan
 * como String — un temporal ("t1"), una variable ("contador"), una
 * constante ("5", "'a'", "true"), o una etiqueta ("L1"). Esto evita
 * crear una jerarquía de clases por tipo de instrucción, suficiente
 * para un proyecto de este alcance, y simplifica tanto la impresión
 * del TAC como su traducción posterior a MIPS32.
 *
 * El significado de los campos `destino`, `op1`, `op2` y `simbolo`
 * depende del operador — ver el Javadoc de {@link OpTAC} para la
 * convención completa de cada uno. Se proveen métodos estáticos de
 * fábrica (binaria, asignar, etc.) para que el generador no tenga que
 * recordar esa convención al construir cada instrucción.
 */
public class Instruccion {

    public final OpTAC operador;
    public String destino; // variable/temporal donde se guarda el resultado (según OpTAC)
    public String op1;     // primer operando (según OpTAC)
    public String op2;     // segundo operando (según OpTAC)
    public String simbolo; // símbolo del operador binario/unario real (+, -, ==, etc.)

    private Instruccion(OpTAC operador) {
        this.operador = operador;
    }

    // ─── FÁBRICAS — una por cada forma de instrucción ────────────────────────

    /** destino = op1 */
    public static Instruccion asignar(String destino, String op1) {
        Instruccion i = new Instruccion(OpTAC.ASIGNAR);
        i.destino = destino;
        i.op1 = op1;
        return i;
    }

    /** destino = op1 SIMBOLO op2   (ej: t3 = t1 + t2) */
    public static Instruccion binaria(String destino, String simbolo, String op1, String op2) {
        Instruccion i = new Instruccion(OpTAC.BINARIA);
        i.destino = destino;
        i.simbolo = simbolo;
        i.op1 = op1;
        i.op2 = op2;
        return i;
    }

    /** destino = SIMBOLO op1   (ej: t2 = -t1,  t2 = !t1) */
    public static Instruccion unaria(String destino, String simbolo, String op1) {
        Instruccion i = new Instruccion(OpTAC.UNARIA);
        i.destino = destino;
        i.simbolo = simbolo;
        i.op1 = op1;
        return i;
    }

    /** goto destino   (destino es una etiqueta, ej: "L1") */
    public static Instruccion goTo(String etiqueta) {
        Instruccion i = new Instruccion(OpTAC.GOTO);
        i.destino = etiqueta;
        return i;
    }

    /** if op1 goto destino   (salta si op1 es verdadero) */
    public static Instruccion ifTrue(String op1, String etiqueta) {
        Instruccion i = new Instruccion(OpTAC.IF_TRUE);
        i.op1 = op1;
        i.destino = etiqueta;
        return i;
    }

    /** ifFalse op1 goto destino   (salta si op1 es falso) */
    public static Instruccion ifFalse(String op1, String etiqueta) {
        Instruccion i = new Instruccion(OpTAC.IF_FALSE);
        i.op1 = op1;
        i.destino = etiqueta;
        return i;
    }

    /** destino:   (marca de posición en el código) */
    public static Instruccion etiqueta(String nombre) {
        Instruccion i = new Instruccion(OpTAC.ETIQUETA);
        i.destino = nombre;
        return i;
    }

    /** param op1   (antes de cada CALL, uno por cada argumento, en orden) */
    public static Instruccion param(String valor) {
        Instruccion i = new Instruccion(OpTAC.PARAM);
        i.op1 = valor;
        return i;
    }

    /** destino = call op1, op2   (op1 = nombre función, op2 = cantidad de argumentos) */
    public static Instruccion call(String destino, String nombreFuncion, int cantidadArgs) {
        Instruccion i = new Instruccion(OpTAC.CALL);
        i.destino = destino;
        i.op1 = nombreFuncion;
        i.op2 = String.valueOf(cantidadArgs);
        return i;
    }

    /** call op1, op2   (llamada a función void, sin guardar resultado) */
    public static Instruccion callVoid(String nombreFuncion, int cantidadArgs) {
        Instruccion i = new Instruccion(OpTAC.CALL_VOID);
        i.op1 = nombreFuncion;
        i.op2 = String.valueOf(cantidadArgs);
        return i;
    }

    /** return op1   (op1 puede ser null si la función es void) */
    public static Instruccion retorno(String valor) {
        Instruccion i = new Instruccion(OpTAC.RETURN);
        i.op1 = valor;
        return i;
    }

    /** destino = op1[op2]   (leer de arreglo; op1 = nombre arreglo, op2 = índice) */
    public static Instruccion arrLoad(String destino, String nombreArreglo, String indice) {
        Instruccion i = new Instruccion(OpTAC.ARR_LOAD);
        i.destino = destino;
        i.op1 = nombreArreglo;
        i.op2 = indice;
        return i;
    }

    /** destino[op1] = op2   (escribir en arreglo; destino = nombre arreglo, op1 = índice, op2 = valor) */
    public static Instruccion arrStore(String nombreArreglo, String indice, String valor) {
        Instruccion i = new Instruccion(OpTAC.ARR_STORE);
        i.destino = nombreArreglo;
        i.op1 = indice;
        i.op2 = valor;
        return i;
    }

    /** FUNCTION destino:   (encabezado de función) */
    public static Instruccion funcionInicio(String nombreFuncion) {
        Instruccion i = new Instruccion(OpTAC.FUNC_INICIO);
        i.destino = nombreFuncion;
        return i;
    }

    /** END FUNCTION destino   (cierre de función) */
    public static Instruccion funcionFin(String nombreFuncion) {
        Instruccion i = new Instruccion(OpTAC.FUNC_FIN);
        i.destino = nombreFuncion;
        return i;
    }

    // ─── IMPRESIÓN LEGIBLE DEL TAC ─────────────────────────────────────────

    @Override
    public String toString() {
        switch (operador) {
            case ASIGNAR:
                return destino + " = " + op1;
            case BINARIA:
                return destino + " = " + op1 + " " + simbolo + " " + op2;
            case UNARIA:
                return destino + " = " + simbolo + op1;
            case GOTO:
                return "goto " + destino;
            case IF_TRUE:
                return "if " + op1 + " goto " + destino;
            case IF_FALSE:
                return "ifFalse " + op1 + " goto " + destino;
            case ETIQUETA:
                return destino + ":";
            case PARAM:
                return "param " + op1;
            case CALL:
                return destino + " = call " + op1 + ", " + op2;
            case CALL_VOID:
                return "call " + op1 + ", " + op2;
            case RETURN:
                return op1 == null ? "return" : "return " + op1;
            case ARR_LOAD:
                return destino + " = " + op1 + "[" + op2 + "]";
            case ARR_STORE:
                return destino + "[" + op1 + "] = " + op2;
            case FUNC_INICIO:
                return "FUNCTION " + destino + ":";
            case FUNC_FIN:
                return "END FUNCTION " + destino;
            default:
                return "??? instrucción desconocida ???";
        }
    }
}