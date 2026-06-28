package com.minic.ir;

import com.minic.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Genera código de tres direcciones (TAC) recorriendo el parse tree de
 * Mini-C. Se ejecuta DESPUÉS de que SemanticVisitor ya validó el
 * programa sin errores — por lo tanto este visitor no revalida tipos,
 * dimensiones de arreglos, aridad, etc. Su único trabajo es traducir
 * construcciones ya válidas a instrucciones TAC.
 *
 * Como SemanticVisitor, extiende MiniCBaseVisitor<String>: cada
 * visitXxx de una EXPRESIÓN retorna el nombre del operando (temporal,
 * variable, o constante) donde quedó el resultado de esa expresión.
 * Cada visitXxx de una SENTENCIA retorna null.
 *
 * Reconstruye los mismos scopes que SemanticVisitor (entrar/salir en
 * cada función y bloque) para que las búsquedas de símbolos —
 * necesarias para saber si un identificador es variable simple o
 * arreglo— sigan funcionando igual sobre la misma SymbolTable ya
 * construida.
 */
public class TACGenerator extends MiniCBaseVisitor<String> {

    private final List<Instruccion> codigo = new ArrayList<>();
    private final GeneradorNombres  gen     = new GeneradorNombres();
    private final SymbolTable       tabla;

    // Pila de contextos de loop: cada elemento es [etiquetaContinue, etiquetaBreak]
    // del loop más interno en el que el generador se encuentra actualmente.
    // break -> salta a etiquetaBreak (tope de la pila).
    // continue -> salta a etiquetaContinue (tope de la pila) — que es
    // Linicio en while/do-while, pero Lincr en for (para no saltarse el
    // incremento, comportamiento correcto de C real).
    private final Deque<String[]> pilaLoops = new ArrayDeque<>();

    public TACGenerator(SymbolTable tablaYaConstruida) {
        this.tabla = tablaYaConstruida;
        this.tabla.reiniciarCursor(); // ver SymbolTable.java — vuelve al scope global
    }

    public List<Instruccion> getCodigo() {
        return codigo;
    }

    private void emitir(Instruccion instr) {
        codigo.add(instr);
    }

    // ─── PROGRAMA / FUNCIÓN ────────────────────────────────────────────────

    @Override
    public String visitProgram(MiniCParser.ProgramContext ctx) {
        // Solo nos interesan las funciones para generar TAC ejecutable;
        // las declaraciones globales se asumen reservadas en .data por
        // el backend MIPS más adelante (Fase 4), no generan TAC aquí.
        for (MiniCParser.FuncDefContext f : ctx.funcDef()) {
            visit(f);
        }
        return null;
    }

    @Override
    public String visitFuncDef(MiniCParser.FuncDefContext ctx) {
        String nombre = ctx.IDENTIFIER().getText();

        tabla.entrarScopeExistente(nombre);

        emitir(Instruccion.funcionInicio(nombre));
        visit(ctx.compoundStmt());
        emitir(Instruccion.funcionFin(nombre));

        tabla.salirScope();
        return null;
    }

    // ─── BLOQUES ───────────────────────────────────────────────────────────

    @Override
    public String visitCompoundStmt(MiniCParser.CompoundStmtContext ctx) {
        boolean esCuerpoFuncion = ctx.parent instanceof MiniCParser.FuncDefContext;

        if (!esCuerpoFuncion) {
            tabla.entrarScopeExistente("bloque@" + ctx.getStart().getLine());
        }

        for (var hijo : ctx.children) {
            if (hijo instanceof MiniCParser.DeclarationContext) {
                visit(hijo);
            } else if (hijo instanceof MiniCParser.StatementContext) {
                visit(hijo);
            }
            // '{' y '}' (tokens literales) se ignoran
        }

        if (!esCuerpoFuncion) {
            tabla.salirScope();
        }

        return null;
    }

    // ─── DECLARACIONES ─────────────────────────────────────────────────────

    @Override
    public String visitDeclaration(MiniCParser.DeclarationContext ctx) {
        for (MiniCParser.DeclaratorContext decl : ctx.declaratorList().declarator()) {
            if (decl.expr() == null) continue; // sin inicialización, nada que generar

            // Arreglos no llegan aquí con expr() != null (el SemanticVisitor
            // ya lo prohíbe), así que esto es siempre una variable simple.
            String nombre = decl.IDENTIFIER().getText();
            String valor  = visit(decl.expr());
            emitir(Instruccion.asignar(nombre, valor));
        }
        return null;
    }

    // ─── ASIGNACIÓN ────────────────────────────────────────────────────────

    @Override
    public String visitAssignmentExpr(MiniCParser.AssignmentExprContext ctx) {
        if (ctx.lvalue() == null) {
            return visit(ctx.logicalOrExpr());
        }

        String valor = visit(ctx.assignmentExpr()); // lado derecho, recursivo

        MiniCParser.LvalueContext lv = ctx.lvalue();
        String nombre = lv.IDENTIFIER().getText();
        Symbol s = tabla.buscar(nombre);

        if (s != null && s.categoria.equals("arreglo") && !lv.expr().isEmpty()) {
            String indice = resolverIndiceArreglo(lv);
            emitir(Instruccion.arrStore(nombre, indice, valor));
        } else {
            emitir(Instruccion.asignar(nombre, valor));
        }

        return valor;
    }

    // Para arreglos 2D (m[i][j]), Mini-C los almacena como una sola región
    // contigua — calculamos el índice lineal aquí mismo emitiendo el TAC
    // necesario: indice = i * columnas + j. Para arreglos 1D, simplemente
    // se usa el único índice tal cual.
    private String resolverIndiceArreglo(MiniCParser.LvalueContext lv) {
        String nombre = lv.IDENTIFIER().getText();
        Symbol s = tabla.buscar(nombre);

        if (lv.expr().size() == 1) {
            return visit(lv.expr(0));
        }

        // 2D: indice = (fila * columnas) + col
        String fila = visit(lv.expr(0));
        String col  = visit(lv.expr(1));
        int columnas = (s.tamanios != null && s.tamanios.length > 1) ? s.tamanios[1] : 0;

        String tFila = gen.nuevoTemporal();
        emitir(Instruccion.binaria(tFila, "*", fila, String.valueOf(columnas)));

        String tIndice = gen.nuevoTemporal();
        emitir(Instruccion.binaria(tIndice, "+", tFila, col));

        return tIndice;
    }

    // ─── CONTROL DE FLUJO ──────────────────────────────────────────────────

    @Override
    public String visitIfStmt(MiniCParser.IfStmtContext ctx) {
        List<MiniCParser.StatementContext> ramas = ctx.statement();

        if (ramas.size() == 1) {
            // if sin else: ifFalse cond goto Lfin / cuerpo / Lfin:
            String cond = visit(ctx.expr());
            String lFin = gen.nuevaEtiqueta();

            emitir(Instruccion.ifFalse(cond, lFin));
            visit(ramas.get(0));
            emitir(Instruccion.etiqueta(lFin));
        } else {
            // if con else: if cond goto Lif / goto Lelse
            //              Lif: cuerpoIf goto Lfin
            //              Lelse: cuerpoElse
            //              Lfin:
            String cond  = visit(ctx.expr());
            String lIf   = gen.nuevaEtiqueta();
            String lElse = gen.nuevaEtiqueta();
            String lFin  = gen.nuevaEtiqueta();

            emitir(Instruccion.ifTrue(cond, lIf));
            emitir(Instruccion.goTo(lElse));

            emitir(Instruccion.etiqueta(lIf));
            visit(ramas.get(0));
            emitir(Instruccion.goTo(lFin));

            emitir(Instruccion.etiqueta(lElse));
            visit(ramas.get(1));

            emitir(Instruccion.etiqueta(lFin));
        }
        return null;
    }

    @Override
    public String visitWhileStmt(MiniCParser.WhileStmtContext ctx) {
        // Linicio: ifFalse cond goto Lfin / cuerpo / goto Linicio / Lfin:
        String lInicio = gen.nuevaEtiqueta();
        String lFin    = gen.nuevaEtiqueta();

        emitir(Instruccion.etiqueta(lInicio));
        String cond = visit(ctx.expr());
        emitir(Instruccion.ifFalse(cond, lFin));

        pilaLoops.push(new String[]{lInicio, lFin}); // continue -> Linicio, break -> Lfin
        visit(ctx.statement());
        pilaLoops.pop();

        emitir(Instruccion.goTo(lInicio));
        emitir(Instruccion.etiqueta(lFin));
        return null;
    }

    @Override
    public String visitDoWhileStmt(MiniCParser.DoWhileStmtContext ctx) {
        // Linicio: cuerpo / ifTrue cond goto Linicio
        String lInicio = gen.nuevaEtiqueta();
        String lFin    = gen.nuevaEtiqueta(); // break necesita salir incluso sin un "Lfin" natural

        emitir(Instruccion.etiqueta(lInicio));

        // En do-while, continue debe re-evaluar la condición (no saltar al
        // inicio del cuerpo otra vez sin chequear) — así que continue
        // salta a una etiqueta justo antes de la condición.
        String lCond = gen.nuevaEtiqueta();

        pilaLoops.push(new String[]{lCond, lFin});
        visit(ctx.statement());
        pilaLoops.pop();

        emitir(Instruccion.etiqueta(lCond));
        String cond = visit(ctx.expr());
        emitir(Instruccion.ifTrue(cond, lInicio));
        emitir(Instruccion.etiqueta(lFin));
        return null;
    }

    @Override
    public String visitForStmt(MiniCParser.ForStmtContext ctx) {
        // init / Lcond: ifFalse cond goto Lfin / cuerpo
        // Lincr: incremento / goto Lcond / Lfin:
        visit(ctx.exprStmt()); // inicialización

        String lCond = gen.nuevaEtiqueta();
        String lIncr = gen.nuevaEtiqueta();
        String lFin  = gen.nuevaEtiqueta();

        emitir(Instruccion.etiqueta(lCond));

        List<MiniCParser.ExprContext> expresiones = ctx.expr();
        if (!expresiones.isEmpty()) {
            String cond = visit(expresiones.get(0));
            emitir(Instruccion.ifFalse(cond, lFin));
        }

        // continue -> Lincr (NO Lcond), para no saltarse el incremento
        pilaLoops.push(new String[]{lIncr, lFin});
        visit(ctx.statement());
        pilaLoops.pop();

        emitir(Instruccion.etiqueta(lIncr));
        if (expresiones.size() > 1) {
            visit(expresiones.get(1)); // incremento
        }

        emitir(Instruccion.goTo(lCond));
        emitir(Instruccion.etiqueta(lFin));
        return null;
    }

    @Override
    public String visitBreakStmt(MiniCParser.BreakStmtContext ctx) {
        String etiquetaBreak = pilaLoops.peek()[1];
        emitir(Instruccion.goTo(etiquetaBreak));
        return null;
    }

    @Override
    public String visitContinueStmt(MiniCParser.ContinueStmtContext ctx) {
        String etiquetaContinue = pilaLoops.peek()[0];
        emitir(Instruccion.goTo(etiquetaContinue));
        return null;
    }

    @Override
    public String visitReturnStmt(MiniCParser.ReturnStmtContext ctx) {
        if (ctx.expr() == null) {
            emitir(Instruccion.retorno(null));
        } else {
            String valor = visit(ctx.expr());
            emitir(Instruccion.retorno(valor));
        }
        return null;
    }

    // ─── OPERADORES BINARIOS — sin corto-circuito, todos uniformes ─────────
    // && y || se tratan igual que cualquier otro operador binario: se
    // evalúan ambos lados siempre y se emite una sola instrucción BINARIA.
    // Para operandos únicos (sin operador real), se propaga el valor tal
    // cual, igual que en SemanticVisitor.

    @Override
    public String visitLogicalOrExpr(MiniCParser.LogicalOrExprContext ctx) {
        return encadenarBinaria(ctx.logicalAndExpr(), ctx, "||");
    }

    @Override
    public String visitLogicalAndExpr(MiniCParser.LogicalAndExprContext ctx) {
        return encadenarBinaria(ctx.equalityExpr(), ctx, "&&");
    }

    @Override
    public String visitEqualityExpr(MiniCParser.EqualityExprContext ctx) {
        return encadenarBinariaConSimbolos(ctx.relationalExpr(), ctx);
    }

    @Override
    public String visitRelationalExpr(MiniCParser.RelationalExprContext ctx) {
        return encadenarBinariaConSimbolos(ctx.additiveExpr(), ctx);
    }

    @Override
    public String visitAdditiveExpr(MiniCParser.AdditiveExprContext ctx) {
        return encadenarBinariaConSimbolos(ctx.multiplicativeExpr(), ctx);
    }

    @Override
    public String visitMultiplicativeExpr(MiniCParser.MultiplicativeExprContext ctx) {
        return encadenarBinariaConSimbolos(ctx.unaryExpr(), ctx);
    }

    // Para niveles donde el operador es SIEMPRE el mismo símbolo fijo
    // (logicalOr siempre usa "||", logicalAnd siempre usa "&&").
    private String encadenarBinaria(List<? extends org.antlr.v4.runtime.tree.ParseTree> operandos,
                                    org.antlr.v4.runtime.ParserRuleContext ctx, String simboloFijo) {
        String resultado = visit(operandos.get(0));
        for (int i = 1; i < operandos.size(); i++) {
            String siguiente = visit(operandos.get(i));
            String t = gen.nuevoTemporal();
            emitir(Instruccion.binaria(t, simboloFijo, resultado, siguiente));
            resultado = t;
        }
        return resultado;
    }

    // Para niveles donde el operador VARÍA entre operandos (==, !=, <, >,
    // <=, >=, +, -, *, /, %) — el símbolo real se lee directamente del
    // token hijo correspondiente en el árbol, en la posición impar
    // (operando, OP, operando, OP, ...).
    private String encadenarBinariaConSimbolos(List<? extends org.antlr.v4.runtime.tree.ParseTree> operandos,
                                               org.antlr.v4.runtime.ParserRuleContext ctx) {
        String resultado = visit(operandos.get(0));
        int indiceHijo = 1;
        for (int i = 1; i < operandos.size(); i++) {
            String simbolo = ctx.getChild(indiceHijo).getText();
            String siguiente = visit(operandos.get(i));
            String t = gen.nuevoTemporal();
            emitir(Instruccion.binaria(t, simbolo, resultado, siguiente));
            resultado = t;
            indiceHijo += 2;
        }
        return resultado;
    }

    // ─── UNARIO ────────────────────────────────────────────────────────────

    @Override
    public String visitUnaryExpr(MiniCParser.UnaryExprContext ctx) {
        if (ctx.primary() != null) {
            return visit(ctx.primary());
        }

        String simbolo = ctx.getChild(0).getText();
        String operando = visit(ctx.unaryExpr());

        // '*' (desreferencia) y '&' (dirección) quedan fuera del alcance
        // actual del generador de TAC — Mini-C los acepta sintácticamente,
        // pero su traducción a TAC/MIPS requeriría manejo de punteros que
        // no se ha definido aún para esta fase.
        if (simbolo.equals("*") || simbolo.equals("&")) {
            return operando; // placeholder: se trata como no-op por ahora
        }

        String t = gen.nuevoTemporal();
        emitir(Instruccion.unaria(t, simbolo, operando));
        return t;
    }

    // ─── PRIMARY — literales no generan instrucción, solo se propagan ──────

    @Override
    public String visitPrimary(MiniCParser.PrimaryContext ctx) {
        if (ctx.INTEGER_CONST() != null)  return ctx.INTEGER_CONST().getText();
        if (ctx.CHAR_CONST() != null)     return ctx.CHAR_CONST().getText();
        if (ctx.STRING_LITERAL() != null) return ctx.STRING_LITERAL().getText();
        if (ctx.getText().equals("true"))  return "true";
        if (ctx.getText().equals("false")) return "false";
        if (ctx.expr() != null)    return visit(ctx.expr());
        if (ctx.lvalue() != null)  return visit(ctx.lvalue());
        if (ctx.call() != null)    return visit(ctx.call());
        return null;
    }

    // ─── LVALUE — variable simple se propaga, arreglo genera ARR_LOAD ──────

    @Override
    public String visitLvalue(MiniCParser.LvalueContext ctx) {
        String nombre = ctx.IDENTIFIER().getText();

        if (ctx.expr().isEmpty()) {
            // Variable simple: no hace falta "cargar" nada, su nombre
            // ya es directamente usable como operando en el TAC.
            return nombre;
        }

        // Arreglo con índices: generar ARR_LOAD a un nuevo temporal
        String indice = resolverIndiceArreglo(ctx);
        String t = gen.nuevoTemporal();
        emitir(Instruccion.arrLoad(t, nombre, indice));
        return t;
    }

    // ─── LLAMADA A FUNCIÓN ─────────────────────────────────────────────────

    @Override
    public String visitCall(MiniCParser.CallContext ctx) {
        String nombre = ctx.IDENTIFIER().getText();
        List<MiniCParser.ExprContext> args = ctx.expr();

        // Evaluar todos los argumentos PRIMERO, luego emitir los PARAM en
        // orden — evita que un argumento con efectos colaterales (otra
        // llamada) se mezcle con los PARAM de esta llamada.
        List<String> valoresArgs = new ArrayList<>();
        for (MiniCParser.ExprContext arg : args) {
            valoresArgs.add(visit(arg));
        }
        for (String valor : valoresArgs) {
            emitir(Instruccion.param(valor));
        }

        Symbol s = tabla.buscar(nombre);
        boolean esVoid = s != null && s.tipo.equals("void");

        if (esVoid) {
            emitir(Instruccion.callVoid(nombre, args.size()));
            return null;
        } else {
            String t = gen.nuevoTemporal();
            emitir(Instruccion.call(t, nombre, args.size()));
            return t;
        }
    }

    // ─── IMPRESIÓN DEL TAC COMPLETO ────────────────────────────────────────

    public void imprimirCodigo() {
        System.out.println();
        System.out.println("CÓDIGO INTERMEDIO (TAC)");
        System.out.println("-".repeat(60));
        for (Instruccion instr : codigo) {
            // Las etiquetas y encabezados de función no se indentan, para
            // que se distingan visualmente del código que contienen.
            if (instr.operador == OpTAC.ETIQUETA
                    || instr.operador == OpTAC.FUNC_INICIO
                    || instr.operador == OpTAC.FUNC_FIN) {
                System.out.println(instr);
            } else {
                System.out.println("    " + instr);
            }
        }
        System.out.println("-".repeat(60));
    }
}