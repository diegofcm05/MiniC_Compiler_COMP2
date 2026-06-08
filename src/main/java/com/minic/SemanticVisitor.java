package com.minic;

public class SemanticVisitor extends MiniCBaseVisitor<Void> {

    private SymbolTable tabla  = new SymbolTable();
    private int         errores = 0;

    public SemanticVisitor() {
        tabla.entrar("global"); // ámbito global al inicio

        String[] funcRuntime = {
                "print_int", "print_char", "print_str", "print_bool",
                "read_int",  "read_char",  "read_str"
        };
        for (String fn : funcRuntime) {
            tabla.agregar(new Symbol(fn, "void", "funcion", 0));
        }
    }


    @Override
    public Void visitDeclaration(MiniCParser.DeclarationContext ctx) {
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
                System.err.printf("[ERROR SEMÁNTICO] línea %d: '%s' ya fue declarado en este ámbito%n",
                        linea, nombre);
                errores++;
            }
        }

        return visitChildren(ctx);
    }


    @Override
    public Void visitFuncDef(MiniCParser.FuncDefContext ctx) {
        System.out.println("  [Visitor] → Función   : " + ctx.IDENTIFIER().getText()
                + " (" + ctx.typeSpecifier().getText() + ")");
        String tipo   = ctx.typeSpecifier().getText();
        String nombre = ctx.IDENTIFIER().getText();
        int linea  = ctx.IDENTIFIER().getSymbol().getLine();
        Symbol s = new Symbol(nombre, tipo, "funcion", linea);
        if (!tabla.agregar(s)) {
            System.err.printf("[ERROR SEMÁNTICO] línea %d: función '%s' ya fue declarada%n",
                    linea, nombre);
            errores++;
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
    public Void visitParam(MiniCParser.ParamContext ctx) {
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
            System.err.printf("[ERROR SEMÁNTICO] línea %d: parámetro '%s' duplicado%n",
                    linea, nombre);
            errores++;
        }
        return null;
    }


    @Override
    public Void visitCompoundStmt(MiniCParser.CompoundStmtContext ctx) {
        // Solo abrimos scope extra si NO venimos directamente de una función
        // (el scope de función ya lo abrió visitFuncDef)
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
    public Void visitLvalue(MiniCParser.LvalueContext ctx) {
        String nombre = ctx.IDENTIFIER().getText();
        int    linea  = ctx.IDENTIFIER().getSymbol().getLine();

        if (tabla.buscar(nombre) == null) {
            System.err.printf("[ERROR SEMÁNTICO] línea %d: '%s' no fue declarado%n",
                    linea, nombre);
            errores++;
        }
        return visitChildren(ctx);
    }

    @Override
    public Void visitCall(MiniCParser.CallContext ctx) {
        String nombre = ctx.IDENTIFIER().getText();
        int    linea  = ctx.IDENTIFIER().getSymbol().getLine();

        if (tabla.buscar(nombre) == null) {
            System.err.printf("[ERROR SEMÁNTICO] línea %d: función '%s' no declarada%n",
                    linea, nombre);
            errores++;
        }
        return visitChildren(ctx);
    }


    public void imprimirTabla() {
        System.out.println();
        System.out.println("TABLA DE SÍMBOLOS");
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf("%-24s %-10s %-12s %-20s %s%n",
                "NOMBRE", "TIPO", "CATEGORÍA", "ÁMBITO", "LÍNEA");
        System.out.println("---------------------------------------------------------------------------");

        for (Scope scope : tabla.getTodos()) {
            for (Symbol s : scope.getSimbolos().values()) {
                System.out.printf("%-24s %-10s %-12s %-20s %d%n",
                        s.nombre, s.tipo, s.categoria, scope.nombre, s.linea);
            }
        }

        System.out.println("--------------------------------------------------------------------------");
    }

    public int getErrores() { return errores; }
    public SymbolTable getTabla() { return tabla; }
}