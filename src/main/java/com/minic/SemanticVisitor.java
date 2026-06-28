package com.minic;

import java.util.ArrayList;
import java.util.List;

public class SemanticVisitor extends MiniCBaseVisitor<String> {

    private SymbolTable tabla   = new SymbolTable();
    private int         errores = 0;

    public SemanticVisitor() {
        tabla.entrar("global");

        String[] funcRuntime = {
                "print_int", "print_char", "print_str", "print_bool",
                "read_int",  "read_char",  "read_str"
        };
        for (String fn : funcRuntime) {
            tabla.agregar(new Symbol(fn, "void", "funcion", 0, new ArrayList<>()));
        }
    }

    private void error(int linea, String msg) {
        System.err.printf("[ERROR SEMÁNTICO] línea %d: %s%n", linea, msg);
        errores++;
    }

    @Override
    public String visitDeclaration(MiniCParser.DeclarationContext ctx) {
        String tipo = ctx.typeSpecifier().getText();

        for (MiniCParser.DeclaratorContext decl : ctx.declaratorList().declarator()) {
            String nombre;
            String categoria;

            if (decl.getChild(0).getText().equals("*")) {
                nombre    = decl.declarator().IDENTIFIER().getText();
                categoria = "puntero";
            } else if (decl.INTEGER_CONST() != null && !decl.INTEGER_CONST().isEmpty()) {
                nombre    = decl.IDENTIFIER().getText();
                categoria = "arreglo";
            } else {
                nombre    = decl.IDENTIFIER().getText();
                categoria = "variable";
            }

            int linea = decl.IDENTIFIER() != null
                    ? decl.IDENTIFIER().getSymbol().getLine()
                    : ctx.getStart().getLine();

            System.out.println("  [Visitor] → Declaración: " + tipo + " " + nombre);

            Symbol s = new Symbol(nombre, tipo, categoria, linea);
            if (!tabla.agregar(s)) {
                error(linea, "'" + nombre + "' ya fue declarado en este ámbito");
            }

            // Si hay inicialización (expr), chequear tipo del lado derecho
            if (decl.expr() != null) {
                String tipoExpr = visit(decl.expr());
                if (tipoExpr != null && !tipoExpr.equals(tipo)) {
                    error(linea, "no se puede inicializar '" + nombre + "' de tipo '"
                            + tipo + "' con valor de tipo '" + tipoExpr + "'");
                }
            }
        }

        return null;
    }

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

        visit(ctx.compoundStmt());

        tabla.salir();

        return null;
    }

    @Override
    public String visitParam(MiniCParser.ParamContext ctx) {
        String tipo = ctx.typeSpecifier().getText();
        String nombre;
        String categoria;
        int    linea;

        if (ctx.declarator() != null) {
            MiniCParser.DeclaratorContext decl = ctx.declarator();
            nombre    = decl.IDENTIFIER() != null
                    ? decl.IDENTIFIER().getText()
                    : decl.declarator().IDENTIFIER().getText();
            linea     = ctx.getStart().getLine();
            categoria = (decl.INTEGER_CONST() != null && !decl.INTEGER_CONST().isEmpty())
                    ? "arreglo" : "parametro";
        } else {
            nombre    = ctx.IDENTIFIER().getText();
            linea     = ctx.IDENTIFIER().getSymbol().getLine();
            categoria = "arreglo";
        }

        System.out.println("  [Visitor] → Parámetro  : " + tipo + " " + nombre);

        Symbol s = new Symbol(nombre, tipo, categoria, linea);
        if (!tabla.agregar(s)) {
            error(linea, "parámetro '" + nombre + "' duplicado");
        }
        return null;
    }

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

    @Override
    public String visitAssignmentExpr(MiniCParser.AssignmentExprContext ctx) {
        // assignmentExpr : lvalue '=' assignmentExpr | logicalOrExpr ;
        if (ctx.lvalue() != null) {
            int linea = ctx.getStart().getLine();
            String nombre = ctx.lvalue().IDENTIFIER().getText();

            String tipoIzq = visit(ctx.lvalue());
            String tipoDer = visit(ctx.assignmentExpr());

            if (tipoIzq != null && tipoDer != null && !tipoIzq.equals(tipoDer)) {
                error(linea, "no se puede asignar valor de tipo '" + tipoDer
                        + "' a '" + nombre + "' de tipo '" + tipoIzq + "'");
            }
            return tipoIzq;
        }
        return visit(ctx.logicalOrExpr());
    }

    // ─── OPERADORES LÓGICOS (&&, ||) — requieren bool, retornan bool ─────────
    // IMPORTANTE: solo se valida el tipo si REALMENTE hay más de un operando,
    // es decir, si el operador && o || está presente en el código fuente.
    // Si hay un solo operando (caso normal al bajar por la cascada de
    // precedencia sin que haya operación), simplemente se propaga su tipo
    // sin compararlo contra 'bool'. Sin este chequeo de cantidad, cualquier
    // expresión —aunque no tuviera && / ||— era forzada a ser bool en cada
    // nivel de la cascada, generando errores falsos repetidos.

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
            if (tipoPrevio != null && tipoActual != null && !tipoPrevio.equals(tipoActual)) {
                error(ctx.getStart().getLine(), "no se puede comparar '" + tipoPrevio
                        + "' con '" + tipoActual + "'");
            }
            tipoPrevio = tipoActual;
        }
        return "bool";
    }

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
            if (tipo != null && !tipo.equals("int")) {
                error(ctx.getStart().getLine(), "operador relacional requiere 'int', se recibió '" + tipo + "'");
            }
        }
        return "bool";
    }

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
            if (tipo != null && !tipo.equals("int")) {
                error(linea, "operador aritmético requiere 'int', se recibió '" + tipo + "'");
            }
        }
        return "int";
    }


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
        if (op.equals("-") && tipo != null && !tipo.equals("int")) {
            error(ctx.getStart().getLine(), "operador '-' unario requiere 'int', se recibió '" + tipo + "'");
        }
        return tipo;
    }

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

    @Override
    public String visitLvalue(MiniCParser.LvalueContext ctx) {
        String nombre = ctx.IDENTIFIER().getText();
        int    linea  = ctx.IDENTIFIER().getSymbol().getLine();

        Symbol s = tabla.buscar(nombre);
        if (s == null) {
            error(linea, "'" + nombre + "' no fue declarado");
            return null;
        }

        for (MiniCParser.ExprContext idx : ctx.expr()) {
            String tipoIdx = visit(idx);
            if (tipoIdx != null && !tipoIdx.equals("int")) {
                error(linea, "el índice de arreglo debe ser 'int', se recibió '" + tipoIdx + "'");
            }
        }

        return s.tipo;
    }

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

        for (MiniCParser.ExprContext arg : ctx.expr()) {
            visit(arg);
        }

        return s.tipo;
    }


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