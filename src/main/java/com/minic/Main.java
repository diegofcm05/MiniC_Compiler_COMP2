package com.minic;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {



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
        tokens.fill();

        System.out.println( "TOKENS" );
        System.out.println( "══════" );
        for (Token tok : tokens.getTokens()) {
            if (tok.getType() == Token.EOF) continue;
            String typeName = MiniCLexer.VOCABULARY.getSymbolicName(tok.getType());
            if (typeName == null) typeName = "'" + tok.getText() + "'";
            System.out.printf( "  [%3d:%2d]" +
                            "  %-22s  " + "%s" + "%n",
                    tok.getLine(), tok.getCharPositionInLine(),
                    typeName, tok.getText());
        }
        System.out.println();



    //parser con listener de errores
        MiniCParser parser = new MiniCParser(tokens);
        parser.removeErrorListeners();
        MiniCErrorListener parserErrors = new MiniCErrorListener("SINTÁCTICO");
        parser.addErrorListener(parserErrors);




        ParseTree tree = parser.program();

        //header del parse tree
        System.out.println("PARSE TREE — Mini-C Compiler" );
        System.out.println("=============================" );
        System.out.println();

        //Imprimir arbol solo si no hay errores
        int totalErrors = lexerErrors.getErrorCount() + parserErrors.getErrorCount();
        if (totalErrors == 0) {
            printTree(tree, parser, "", true);
        } else {
            System.out.println("  No se muestra el árbol debido a errores.");
        }

        //Resumen
        System.out.println();
        System.out.println( "=======================================" );
        System.out.println( " Tokens procesados  : " + tokens.size() );
        if (totalErrors == 0) {
            System.out.println(" Errores encontrados: 0 " );
        } else {
            System.out.println( " Errores encontrados: " + totalErrors );
        }
        System.out.println("=======================================" );
    }

    static void printTree(ParseTree tree, MiniCParser parser, String prefix, boolean isLast) {
        String connector   = isLast ? "└── " : "├── ";
        String childPrefix = isLast ? "    " : "│   ";

        if (tree instanceof TerminalNode) {
            String text = tree.getText();
            if (text.equals("<EOF>")) {
                System.out.println(prefix + connector + "<EOF>" );
            } else {
                System.out.println(prefix + connector +  text );
            }
        } else {
            RuleContext ctx = (RuleContext) tree;
            String ruleName = parser.getRuleNames()[ctx.getRuleIndex()];
            System.out.println(prefix + connector + "[" + ruleName + "]" );

            int childCount = tree.getChildCount();
            for (int i = 0; i < childCount; i++) {
                printTree(tree.getChild(i), parser, prefix + childPrefix, i == childCount - 1);
            }
        }
    }


}


