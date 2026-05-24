package com.minic;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {

    static final String RESET  = "\u001B[0m";
    static final String BLUE   = "\u001B[34m";
    static final String GREEN  = "\u001B[32m";
    static final String YELLOW = "\u001B[33m";
    static final String CYAN   = "\u001B[36m";
    static final String GRAY   = "\u001B[90m";
    static final String RED    = "\u001B[31m";

    public static void main(String[] args) throws Exception {

        String inputFile = args.length > 0 ? args[0] : null;
        CharStream input;
        if (inputFile != null) {
            input = CharStreams.fromFileName(inputFile);
        } else {
            input = CharStreams.fromStream(System.in);
        }

        //lexer con listener de errores
        MiniCLexer lexer = new MiniCLexer(input);
        lexer.removeErrorListeners();
        MiniCErrorListener lexerErrors = new MiniCErrorListener("LÉXICO");
        lexer.addErrorListener(lexerErrors);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        //parser con listener de errores
        MiniCParser parser = new MiniCParser(tokens);
        parser.removeErrorListeners();
        MiniCErrorListener parserErrors = new MiniCErrorListener("SINTÁCTICO");
        parser.addErrorListener(parserErrors);

        ParseTree tree = parser.program();

        //header del parse tree
        System.out.println(CYAN + "PARSE TREE — Mini-C Compiler" + RESET);
        System.out.println(CYAN + "=============================" + RESET);
        System.out.println();

        //Imprimir arbol solo si no hay errores
        int totalErrors = lexerErrors.getErrorCount() + parserErrors.getErrorCount();
        if (totalErrors == 0) {
            printTree(tree, parser, "", true);
        } else {
            System.out.println(RED + "  No se muestra el árbol debido a errores." + RESET);
        }

        //Resumen
        System.out.println();
        System.out.println(GRAY + "=======================================" + RESET);
        System.out.println(GRAY + " Tokens procesados  : " + tokens.size() + RESET);
        if (totalErrors == 0) {
            System.out.println(GREEN + " Errores encontrados: 0 ✓" + RESET);
        } else {
            System.out.println(RED + " Errores encontrados: " + totalErrors + RESET);
        }
        System.out.println(GRAY + "=======================================" + RESET);
    }

    static void printTree(ParseTree tree, MiniCParser parser, String prefix, boolean isLast) {
        String connector   = isLast ? "└── " : "├── ";
        String childPrefix = isLast ? "    " : "│   ";

        if (tree instanceof TerminalNode) {
            String text = tree.getText();
            if (text.equals("<EOF>")) {
                System.out.println(prefix + connector + GRAY + "<EOF>" + RESET);
            } else {
                System.out.println(prefix + connector + YELLOW + text + RESET);
            }
        } else {
            RuleContext ctx = (RuleContext) tree;
            String ruleName = parser.getRuleNames()[ctx.getRuleIndex()];
            String color = getRuleColor(ruleName);
            System.out.println(prefix + connector + color + "[" + ruleName + "]" + RESET);

            int childCount = tree.getChildCount();
            for (int i = 0; i < childCount; i++) {
                printTree(tree.getChild(i), parser, prefix + childPrefix, i == childCount - 1);
            }
        }
    }

    static String getRuleColor(String ruleName) {
        return switch (ruleName) {
            case "program"                             -> CYAN;
            case "funcDef"                             -> BLUE;
            case "compoundStmt"                        -> BLUE;
            case "declaration", "typeSpecifier"        -> GREEN;
            case "ifStmt", "whileStmt", "forStmt",
                 "doWhileStmt", "returnStmt",
                 "breakStmt", "continueStmt",
                 "exprStmt"                            -> "\u001B[35m";
            case "expr", "assignmentExpr",
                 "logicalOrExpr", "logicalAndExpr",
                 "equalityExpr", "relationalExpr",
                 "additiveExpr", "multiplicativeExpr",
                 "unaryExpr"                           -> YELLOW;
            case "primary", "call", "lvalue"           -> "\u001B[96m";
            default                                    -> RESET;
        };
    }
}


