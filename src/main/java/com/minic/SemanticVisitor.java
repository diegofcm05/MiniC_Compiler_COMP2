package com.minic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SemanticVisitor extends MiniCBaseVisitor<String> {

    private SymbolTable tabla   = new SymbolTable();
    private int         errores = 0;

    private String tipoRetornoActual = null;
    private int nivelLoop = 0;

    public SemanticVisitor() {
        tabla.entrar("global");

        registrarRuntime("print_int",  "void", "int");
        registrarRuntime("print_char", "void", "char");
        registrarRuntime("print_str",  "void", "string");
        registrarRuntime("print_bool", "void", "bool");
        registrarRuntime("read_int",   "int");
        registrarRuntime("read_char",  "char");
        registrarRuntime("read_str",   "string");
    }

    private void registrarRuntime(String nombre, String tipoRetorno, String... tiposParams) {
        tabla.agregar(new Symbol(nombre, tipoRetorno, "funcion", 0, Arrays.asList(tiposParams)));
    }

    private void error(int linea, String msg) {
        System.err.printf("[ERROR SEMÁNTICO] línea %d: %s%n", linea, msg);
        errores++;
    }

    // ─── HELPERS DE COMPATIBILIDAD DE TIPOS (promociones char↔int) ───────────

    private boolean esNumerico(String tipo) {
        return "int".equals(tipo) || "char".equals(tipo);
    }

    private boolean compatiblePromocion(String a, String b) {
        if (a == null || b == null) return true;
        if (esNumerico(a) && esNumerico(b)) return true;
        return a.equals(b);
    }

    private boolean esCondicionValida(String tipo) {
        return "bool".equals(tipo) || "int".equals(tipo);
    }

    // ─── DECLARACIONES DE VARIABLES ──────────────────────────────────────────

    @Override
    public String visitDeclaration(MiniCParser.DeclarationContext ctx) {
        String tipo = ctx.typeSpecifier().getText();

        for (MiniCParser.DeclaratorContext decl : ctx.declaratorList().declarator()) {
            String nombre;
            String categoria;
            int dimensiones = 0;

            if (decl.getChild(0).getText().equals("*")) {
                nombre    = decl.declarator().IDENTIFIER().getText();
                categoria = "puntero";
            } else if (decl.INTEGER_CONST() != null && !decl.INTEGER_CONST().isEmpty()) {
                nombre    = decl.IDENTIFIER().getText();
                categoria = "arreglo";
                // decl.INTEGER_CONST() devuelve la lista de constantes entre
                // corchetes — su tamaño ES la cantidad de dimensiones.
                // Cubre tanto "int b[5]" (1 constante) como "int m[3][4]"
                // (2 constantes, alternativa dedicada de la gramática).
                dimensiones = decl.INTEGER_CONST().size();
            } else {
                nombre    = decl.IDENTIFIER().getText();
                categoria = "variable";
            }

            int linea = decl.IDENTIFIER() != null
                    ? decl.IDENTIFIER().getSymbol().getLine()
                    : ctx.getStart().getLine();

            System.out.println("  [Visitor] → Declaración: " + tipo + " " + nombre
                    + (dimensiones > 0 ? " (" + dimensiones + "D)" : ""));

            Symbol s = new Symbol(nombre, tipo, categoria, linea);
            s.dimensiones = dimensiones;
            if (!tabla.agregar(s)) {
                error(linea, "'" + nombre + "' ya fue declarado en este ámbito");
            }

            if (decl.expr() != null) {
                if (categoria.equals("arreglo")) {
                    error(linea, "no se puede inicializar el arreglo '" + nombre
                            + "' con un valor escalar");
                } else {
                    String tipoExpr = visit(decl.expr());
                    if (tipoExpr != null && !compatiblePromocion(tipo, tipoExpr)) {
                        error(linea, "no se puede inicializar '" + nombre + "' de tipo '"
                                + tipo + "' con valor de tipo '" + tipoExpr + "'");
                    }
                }
            }
        }

        return null;
    }

    // ─── DEFINICIÓN DE FUNCIÓN ───────────────────────────────────────────────

    @Override
    public String visitFuncDef(MiniCParser.FuncDefContext ctx) {
        String tipo   = ctx.typeSpecifier().getText();
        String nombre = ctx.IDENTIFIER().getText();
        int    linea  = ctx.IDENTIFIER().getSymbol().getLine();

        System.out.println("  [Visitor] → Función   : " + nombre + " (" + tipo + ")");

        List<String> tiposParams = new ArrayList<>();
        if (ctx.params() != null) {
            for (MiniCParser.ParamContext param : ctx.params().param()) {
                tiposParams.add(param.typeSpecifier().getText());
            }
        }

        Symbol s = new Symbol(nombre, tipo, "funcion", linea, tiposParams);
        if (!tabla.agregar(s)) {
            error(linea, "función '" + nombre + "' ya fue declarada");
        }

        tabla.entrar(nombre);

        if (ctx.params() != null) {
            for (MiniCParser.ParamContext param : ctx.params().param()) {
                visitParam(param);
            }
        }

        String tipoRetornoPrevio = tipoRetornoActual;
        tipoRetornoActual = tipo;

        int nivelLoopPrevio = nivelLoop;
        nivelLoop = 0;

        visit(ctx.compoundStmt());

        nivelLoop = nivelLoopPrevio;
        tipoRetornoActual = tipoRetornoPrevio;

        tabla.salir();

        return null;
    }

    // ─── PARÁMETROS ──────────────────────────────────────────────────────────

    @Override
    public String visitParam(MiniCParser.ParamContext ctx) {
        String tipo = ctx.typeSpecifier().getText();
        String nombre;
        String categoria;
        int    linea;
        int    dimensiones = 0;

        if (ctx.declarator() != null) {
            MiniCParser.DeclaratorContext decl = ctx.declarator();
            nombre    = decl.IDENTIFIER() != null
                    ? decl.IDENTIFIER().getText()
                    : decl.declarator().IDENTIFIER().getText();
            linea     = ctx.getStart().getLine();
            boolean esArregloSimple = decl.INTEGER_CONST() != null && !decl.INTEGER_CONST().isEmpty();
            categoria = esArregloSimple ? "arreglo" : "parametro";
            if (esArregloSimple) {
                dimensiones = decl.INTEGER_CONST().size();
            }
        } else {
            // param de matriz 2D: typeSpecifier IDENTIFIER '[' ']' '[' INTEGER_CONST ']'
            // (la primera dimensión se omite por convención de C en parámetros)
            nombre      = ctx.IDENTIFIER().getText();
            linea       = ctx.IDENTIFIER().getSymbol().getLine();
            categoria   = "arreglo";
            dimensiones = 2;
        }

        System.out.println("  [Visitor] → Parámetro  : " + tipo + " " + nombre
                + (dimensiones > 0 ? " (" + dimensiones + "D)" : ""));

        Symbol s = new Symbol(nombre, tipo, categoria, linea);
        s.dimensiones = dimensiones;
        if (!tabla.agregar(s)) {
            error(linea, "parámetro '" + nombre + "' duplicado");
        }
        return null;
    }

    // ─── BLOQUES ANIDADOS (if / while / for / do-while) ──────────────────────

    @Override
    public String visitCompoundStmt(MiniCParser.CompoundStmtContext ctx) {
        boolean esCuerpoFuncion = ctx.parent instanceof MiniCParser.FuncDefContext;

        if (!esCuerpoFuncion) {
            tabla.entrar("bloque@" + ctx.getStart().getLine());
        }

        visitChildren(ctx);

        if (!esCuerpoFuncion) {
            tabla.salir();
        }

        return null;
    }

    // ─── RETURN ───────────────────────────────────────────────────────────────

    @Override
    public String visitReturnStmt(MiniCParser.ReturnStmtContext ctx) {
        int linea = ctx.getStart().getLine();

        if (ctx.expr() == null) {
            if (tipoRetornoActual != null && !tipoRetornoActual.equals("void")) {
                error(linea, "función de tipo '" + tipoRetornoActual
                        + "' debe retornar un valor");
            }
            return null;
        }

        String tipoExpr = visit(ctx.expr());

        if (tipoRetornoActual != null && tipoExpr != null) {
            if (tipoRetornoActual.equals("void")) {
                error(linea, "función de tipo 'void' no puede retornar un valor");
            } else if (!compatiblePromocion(tipoRetornoActual, tipoExpr)) {
                error(linea, "se esperaba retornar '" + tipoRetornoActual
                        + "', se recibió '" + tipoExpr + "'");
            }
        }

        return tipoExpr;
    }

    // ─── BREAK / CONTINUE ──────────────────────────────────────────────────────

    @Override
    public String visitBreakStmt(MiniCParser.BreakStmtContext ctx) {
        if (nivelLoop == 0) {
            error(ctx.getStart().getLine(), "'break' fuera de un ciclo");
        }
        return null;
    }

    @Override
    public String visitContinueStmt(MiniCParser.ContinueStmtContext ctx) {
        if (nivelLoop == 0) {
            error(ctx.getStart().getLine(), "'continue' fuera de un ciclo");
        }
        return null;
    }

    // ─── CONDICIONES DE CONTROL DE FLUJO — bool O int ────────────────────────

    @Override
    public String visitIfStmt(MiniCParser.IfStmtContext ctx) {
        String tipoCond = visit(ctx.expr());
        if (tipoCond != null && !esCondicionValida(tipoCond)) {
            error(ctx.getStart().getLine(),
                    "la condición de 'if' debe ser 'bool' o 'int', se recibió '" + tipoCond + "'");
        }

        for (MiniCParser.StatementContext stmt : ctx.statement()) {
            visit(stmt);
        }
        return null;
    }

    @Override
    public String visitWhileStmt(MiniCParser.WhileStmtContext ctx) {
        String tipoCond = visit(ctx.expr());
        if (tipoCond != null && !esCondicionValida(tipoCond)) {
            error(ctx.getStart().getLine(),
                    "la condición de 'while' debe ser 'bool' o 'int', se recibió '" + tipoCond + "'");
        }

        nivelLoop++;
        visit(ctx.statement());
        nivelLoop--;
        return null;
    }

    @Override
    public String visitDoWhileStmt(MiniCParser.DoWhileStmtContext ctx) {
        nivelLoop++;
        visit(ctx.statement());
        nivelLoop--;

        String tipoCond = visit(ctx.expr());
        if (tipoCond != null && !esCondicionValida(tipoCond)) {
            error(ctx.getStart().getLine(),
                    "la condición de 'do-while' debe ser 'bool' o 'int', se recibió '" + tipoCond + "'");
        }
        return null;
    }

    @Override
    public String visitForStmt(MiniCParser.ForStmtContext ctx) {
        visit(ctx.exprStmt());

        List<MiniCParser.ExprContext> expresiones = ctx.expr();
        if (!expresiones.isEmpty()) {
            String tipoCond = visit(expresiones.get(0));
            if (tipoCond != null && !esCondicionValida(tipoCond)) {
                error(ctx.getStart().getLine(),
                        "la condición de 'for' debe ser 'bool' o 'int', se recibió '" + tipoCond + "'");
            }

            if (expresiones.size() > 1) {
                visit(expresiones.get(1));
            }
        }

        nivelLoop++;
        visit(ctx.statement());
        nivelLoop--;
        return null;
    }

    // ─── ASIGNACIÓN ──────────────────────────────────────────────────────────

    @Override
    public String visitAssignmentExpr(MiniCParser.AssignmentExprContext ctx) {
        if (ctx.lvalue() != null) {
            int linea = ctx.getStart().getLine();
            String nombre = ctx.lvalue().IDENTIFIER().getText();

            String tipoIzq = visit(ctx.lvalue());
            String tipoDer = visit(ctx.assignmentExpr());

            if (tipoIzq != null && tipoDer != null && !compatiblePromocion(tipoIzq, tipoDer)) {
                error(linea, "no se puede asignar valor de tipo '" + tipoDer
                        + "' a '" + nombre + "' de tipo '" + tipoIzq + "'");
            }
            return tipoIzq;
        }
        return visit(ctx.logicalOrExpr());
    }

    // ─── OPERADORES LÓGICOS (&&, ||) ──────────────────────────────────────────

    @Override
    public String visitLogicalOrExpr(MiniCParser.LogicalOrExprContext ctx) {
        return chequearLogico(ctx.logicalAndExpr(), ctx.getStart().getLine());
    }

    @Override
    public String visitLogicalAndExpr(MiniCParser.LogicalAndExprContext ctx) {
        return chequearLogico(ctx.equalityExpr(), ctx.getStart().getLine());
    }

    private String chequearLogico(List<? extends org.antlr.v4.runtime.tree.ParseTree> operandos, int linea) {
        List<String> tipos = new ArrayList<>();
        for (var op : operandos) {
            tipos.add(visit(op));
        }

        if (operandos.size() == 1) {
            return tipos.get(0);
        }

        for (String tipo : tipos) {
            if (tipo != null && !tipo.equals("bool")) {
                error(linea, "operador lógico requiere operandos 'bool', se recibió '" + tipo + "'");
            }
        }
        return "bool";
    }

    // ─── IGUALDAD (==, !=) ─────────────────────────────────────────────────────

    @Override
    public String visitEqualityExpr(MiniCParser.EqualityExprContext ctx) {
        List<MiniCParser.RelationalExprContext> operandos = ctx.relationalExpr();
        List<String> tipos = new ArrayList<>();
        for (var op : operandos) {
            tipos.add(visit(op));
        }

        if (operandos.size() == 1) {
            return tipos.get(0);
        }

        String tipoPrevio = tipos.get(0);
        for (int i = 1; i < tipos.size(); i++) {
            String tipoActual = tipos.get(i);
            if (tipoPrevio != null && tipoActual != null && !compatiblePromocion(tipoPrevio, tipoActual)) {
                error(ctx.getStart().getLine(), "no se puede comparar '" + tipoPrevio
                        + "' con '" + tipoActual + "'");
            }
            tipoPrevio = tipoActual;
        }
        return "bool";
    }

    // ─── RELACIONALES (<, >, <=, >=) ─────────────────────────────────────────

    @Override
    public String visitRelationalExpr(MiniCParser.RelationalExprContext ctx) {
        List<MiniCParser.AdditiveExprContext> operandos = ctx.additiveExpr();
        List<String> tipos = new ArrayList<>();
        for (var op : operandos) {
            tipos.add(visit(op));
        }

        if (operandos.size() == 1) {
            return tipos.get(0);
        }

        for (String tipo : tipos) {
            if (tipo != null && !esNumerico(tipo)) {
                error(ctx.getStart().getLine(), "operador relacional requiere 'int' o 'char', se recibió '" + tipo + "'");
            }
        }
        return "bool";
    }

    // ─── ARITMÉTICOS (+, -, *, /, %) ─────────────────────────────────────────

    @Override
    public String visitAdditiveExpr(MiniCParser.AdditiveExprContext ctx) {
        return chequearAritmetico(ctx.multiplicativeExpr(), ctx.getStart().getLine());
    }

    @Override
    public String visitMultiplicativeExpr(MiniCParser.MultiplicativeExprContext ctx) {
        return chequearAritmetico(ctx.unaryExpr(), ctx.getStart().getLine());
    }

    private String chequearAritmetico(List<? extends org.antlr.v4.runtime.tree.ParseTree> operandos, int linea) {
        List<String> tipos = new ArrayList<>();
        for (var op : operandos) {
            tipos.add(visit(op));
        }

        if (operandos.size() == 1) {
            return tipos.get(0);
        }

        for (String tipo : tipos) {
            if (tipo != null && !esNumerico(tipo)) {
                error(linea, "operador aritmético requiere 'int' o 'char', se recibió '" + tipo + "'");
            }
        }
        return "int";
    }

    // ─── UNARIO (!, -, *, &) ──────────────────────────────────────────────────

    @Override
    public String visitUnaryExpr(MiniCParser.UnaryExprContext ctx) {
        if (ctx.primary() != null) {
            return visit(ctx.primary());
        }
        String op = ctx.getChild(0).getText();
        String tipo = visit(ctx.unaryExpr());

        if (op.equals("!") && tipo != null && !tipo.equals("bool")) {
            error(ctx.getStart().getLine(), "operador '!' requiere 'bool', se recibió '" + tipo + "'");
        }
        if (op.equals("-")) {
            if (tipo != null && !esNumerico(tipo)) {
                error(ctx.getStart().getLine(), "operador '-' unario requiere 'int' o 'char', se recibió '" + tipo + "'");
            }
            return "int";
        }
        return tipo;
    }

    // ─── PRIMARY ──────────────────────────────────────────────────────────────

    @Override
    public String visitPrimary(MiniCParser.PrimaryContext ctx) {
        if (ctx.INTEGER_CONST() != null) return "int";
        if (ctx.CHAR_CONST() != null)    return "char";
        if (ctx.STRING_LITERAL() != null) return "string";
        if (ctx.getText().equals("true") || ctx.getText().equals("false")) return "bool";
        if (ctx.expr() != null) return visit(ctx.expr());
        if (ctx.lvalue() != null) return visit(ctx.lvalue());
        if (ctx.call() != null) return visit(ctx.call());
        return null;
    }

    // ─── USO DE IDENTIFICADORES (lvalue) — validación de dimensiones ─────────
    // lvalue : IDENTIFIER ('[' expr ']')* — la cantidad de corchetes usados
    // (ctx.expr().size()) debe coincidir con cómo fue declarado el símbolo:
    //   - variable simple (dimensiones == 0): NO debe llevar corchetes
    //   - arreglo (dimensiones == 1 o 2): debe llevar EXACTAMENTE esa
    //     cantidad de corchetes para acceder a un elemento individual
    // Si el símbolo es un arreglo y se usa SIN corchetes (ej. "b = otro;"),
    // se está intentando tratar el arreglo completo como un valor escalar
    // — el Inge prohíbe explícitamente la asignación arreglo-a-arreglo.

    @Override
    public String visitLvalue(MiniCParser.LvalueContext ctx) {
        String nombre = ctx.IDENTIFIER().getText();
        int    linea  = ctx.IDENTIFIER().getSymbol().getLine();

        Symbol s = tabla.buscar(nombre);
        if (s == null) {
            error(linea, "'" + nombre + "' no fue declarado");
            return null;
        }

        int indicesUsados = ctx.expr().size();

        if (s.categoria.equals("arreglo")) {
            if (indicesUsados == 0) {
                // Caso C: arreglo usado sin índices — equivalente a tratar
                // todo el arreglo como un escalar (ej. en una asignación
                // arreglo-a-arreglo o pasándolo como valor suelto).
                error(linea, "no se puede usar el arreglo '" + nombre
                        + "' sin índices (asignación de arreglo completo no permitida)");
                return null;
            } else if (indicesUsados != s.dimensiones) {
                // Caso D: cantidad de índices no coincide con cómo se declaró
                error(linea, "'" + nombre + "' fue declarado con " + s.dimensiones
                        + " dimensión(es), se usó con " + indicesUsados + " índice(s)");
            }
        } else if (indicesUsados > 0) {
            // Caso E: se intentan usar corchetes sobre algo que no es arreglo
            error(linea, "'" + nombre + "' no es un arreglo, no admite indexación");
        }

        // Validar que cada índice usado sea de tipo int
        for (MiniCParser.ExprContext idx : ctx.expr()) {
            String tipoIdx = visit(idx);
            if (tipoIdx != null && !tipoIdx.equals("int")) {
                error(linea, "el índice de arreglo debe ser 'int', se recibió '" + tipoIdx + "'");
            }
        }

        // Si se accedió correctamente con índices, el resultado es el tipo
        // BASE del arreglo (ej. "int"), no la categoría "arreglo" — por
        // ejemplo b[2] es un int individual, no el arreglo completo.
        return s.tipo;
    }

    // ─── LLAMADA A FUNCIÓN — aridad y tipos de argumentos (con promoción) ────

    @Override
    public String visitCall(MiniCParser.CallContext ctx) {
        String nombre = ctx.IDENTIFIER().getText();
        int    linea  = ctx.IDENTIFIER().getSymbol().getLine();

        Symbol s = tabla.buscar(nombre);
        if (s == null) {
            error(linea, "función '" + nombre + "' no declarada");
            for (MiniCParser.ExprContext arg : ctx.expr()) visit(arg);
            return null;
        }

        if (!s.categoria.equals("funcion")) {
            error(linea, "'" + nombre + "' no es una función");
            for (MiniCParser.ExprContext arg : ctx.expr()) visit(arg);
            return s.tipo;
        }

        List<MiniCParser.ExprContext> args = ctx.expr();

        List<String> tiposArgs = new ArrayList<>();
        for (MiniCParser.ExprContext arg : args) {
            tiposArgs.add(visit(arg));
        }

        List<String> tiposParams = s.tiposParametros;

        if (tiposParams != null && tiposParams.size() != args.size()) {
            error(linea, "función '" + nombre + "' espera " + tiposParams.size()
                    + " argumento(s), se recibieron " + args.size());
            return s.tipo;
        }

        if (tiposParams != null) {
            for (int i = 0; i < tiposParams.size(); i++) {
                String tipoEsperado = tiposParams.get(i);
                String tipoRecibido = tiposArgs.get(i);
                if (tipoEsperado != null && tipoRecibido != null
                        && !compatiblePromocion(tipoEsperado, tipoRecibido)) {
                    error(linea, "argumento " + (i + 1) + " de '" + nombre
                            + "' debe ser '" + tipoEsperado + "', se recibió '" + tipoRecibido + "'");
                }
            }
        }

        return s.tipo;
    }

    // ─── IMPRESIÓN DE TABLA ───────────────────────────────────────────────────

    public void imprimirTabla() {
        System.out.println();
        System.out.println("TABLA DE SÍMBOLOS");
        System.out.println("-".repeat(95));
        System.out.printf("%-22s %-10s %-12s %-40s %s%n",
                "NOMBRE", "TIPO", "CATEGORÍA", "ÁMBITO", "LÍNEA");
        System.out.println("-".repeat(95));

        for (Scope scope : tabla.getTodos()) {
            for (Symbol s : scope.getSimbolos().values()) {
                System.out.printf("%-22s %-10s %-12s %-40s %d%n",
                        s.nombre, s.tipo, s.categoria, scope.rutaCompleta(), s.linea);
            }
        }

        System.out.println("-".repeat(95));
    }

    public int getErrores() { return errores; }
    public SymbolTable getTabla() { return tabla; }
}