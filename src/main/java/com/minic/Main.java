package com.minic;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {
    public static void main(String[] args) throws Exception {

        // Lee el archivo fuente Mini-C pasado como argumento
        String inputFile = args.length > 0 ? args[0] : null;

        CharStream input;
        if (inputFile != null) {
            input = CharStreams.fromFileName(inputFile);
        } else {
            input = CharStreams.fromStream(System.in);
        }

        // 1. Lexer
        MiniCLexer lexer = new MiniCLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // 2. Parser
        MiniCParser parser = new MiniCParser(tokens);
        ParseTree tree = parser.program();

        // 3. Imprimir el árbol (para verificar que funciona)
        System.out.println(prettyPrint(tree, parser, 0));
    }


    static String prettyPrint(ParseTree tree, MiniCParser parser, int depth) {
        StringBuilder sb = new StringBuilder();
        String indent = "  ".repeat(depth);

        if (tree instanceof TerminalNode) {
            sb.append(indent).append(tree.getText()).append("\n");
        } else {
            String ruleName = parser.getRuleNames()[((RuleContext) tree).getRuleIndex()];
            sb.append(indent).append("(").append(ruleName).append("\n");
            for (int i = 0; i < tree.getChildCount(); i++) {
                sb.append(prettyPrint(tree.getChild(i), parser, depth + 1));
            }
            sb.append(indent).append(")\n");
        }
        return sb.toString();
    }
}


