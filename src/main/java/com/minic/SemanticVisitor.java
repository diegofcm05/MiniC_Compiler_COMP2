package com.minic;

import java.util.ArrayList;

public class SemanticVisitor extends MiniCBaseVisitor<Void> {

    private SymbolTable tabla = new SymbolTable();
    private int errores = 0;
    private ArrayList<Scope> ambitos = new ArrayList<Scope>();

    public SemanticVisitor() {
        tabla.entrar("global"); // ámbito global al inicio
    }

    // Cuando encuentra una declaración de variable
    @Override
    public Void visitDeclaration(MiniCParser.DeclarationContext ctx) {
        String tipo = ctx.typeSpecifier().getText();
        for (var decl : ctx.declaratorList().declarator()) {
            String nombre = decl.IDENTIFIER().getText();
            int linea = decl.IDENTIFIER().getSymbol().getLine();
            Symbol s = new Symbol(nombre, tipo, "variable", linea);
            if (!tabla.agregar(s)) {
                System.err.printf("[ERROR SEMÁNTICO] línea %d: '%s' ya fue declarado en este ámbito%n",
                        linea, nombre);
                errores++;
            }
        }
        return visitChildren(ctx);
    }

    // Cuando encuentra una función
    @Override
    public Void visitFuncDef(MiniCParser.FuncDefContext ctx) {
        String tipo   = ctx.typeSpecifier().getText();
        String nombre = ctx.IDENTIFIER().getText();
        int linea     = ctx.IDENTIFIER().getSymbol().getLine();

        tabla.entrar(nombre);
        ambitos.add(tabla.ambito()); // guardar antes de visitar hijos
        visitChildren(ctx);
        tabla.salir();

        Symbol s = new Symbol(nombre, tipo, "funcion", linea);
        if (!tabla.agregar(s)) {
            System.err.printf("[ERROR SEMÁNTICO] línea %d: función '%s' ya fue declarada%n",
                    linea, nombre);
            errores++;
        }

        // Entrar al ámbito de la función
        tabla.entrar(nombre);
        visitChildren(ctx);
        tabla.salir(); // salir al terminar la función
        return null;
    }

    public void imprimirTabla() {
        System.out.println();
        System.out.println("TABLA DE SÍMBOLOS");
        System.out.println("═════════════════════════════════════════════════════");
        System.out.printf("%-15s %-10s %-12s %-10s %s%n",
                "NOMBRE", "TIPO", "CATEGORÍA", "ÁMBITO", "LÍNEA");
        System.out.println("─────────────────────────────────────────────────────");

        for (Scope scope : ambitos) {
            for (Symbol s : scope.getSimbolos().values()) {
                System.out.printf("%-15s %-10s %-12s %-10s %d%n",
                        s.nombre, s.tipo, s.categoria, scope.nombre, s.linea);
            }
        }

        System.out.println("═════════════════════════════════════════════════════");
    }

    public int getErrores() { return errores; }
    public SymbolTable getTabla() { return tabla; }
}