package com.minic;

import com.minic.ir.TACGenerator;
import com.minic.mips.MIPSGenerator;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Punto de entrada del compilador Mini-C.
 *
 * Uso:  minicc input.mc -S -o output.s [-O] [--dump-ir]
 *
 *   -S            genera código MIPS32 (.s) — sin esta bandera, el programa
 *                 corre en "modo diagnóstico": imprime tokens, árbol de
 *                 parseo, tabla de símbolos y TAC, sin generar ningún
 *                 archivo (comportamiento histórico de Fases 1-3, se
 *                 conserva para no romper los flujos de prueba existentes).
 *   -o <archivo>  archivo de salida para el .s — obligatorio junto con -S.
 *   -O            aplica constant folding sobre el TAC antes de generar
 *                 MIPS, mostrando el IR antes y después en stdout.
 *   --dump-ir     imprime el TAC a stdout también en modo -S (en modo
 *                 diagnóstico el TAC ya se imprime siempre).
 */
public class Main {

    public static void main(String[] args) throws Exception {

        // Fuerza la salida a UTF-8 para que los acentos de los mensajes
        // (TABLA DE SÍMBOLOS, [ERROR SEMÁNTICO], etc.) salgan correctos al
        // redirigir la salida o ejecutar por `java -jar`, sin depender de la
        // codificación por defecto de la consola.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));

        // ─── argumentos: minicc input.mc -S -o output.s [-O] [--dump-ir] ───
        String inputFile  = null;
        String outputFile = null;
        boolean modoMips  = false;
        boolean optimizar = false;
        boolean dumpIr    = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-S":
                    modoMips = true;
                    break;
                case "-O":
                    optimizar = true;
                    break;
                case "--dump-ir":
                    dumpIr = true;
                    break;
                case "-o":
                    if (i + 1 >= args.length) {
                        System.err.println("Falta el nombre de archivo después de -o");
                        return;
                    }
                    outputFile = args[++i];
                    break;
                default:
                    if (args[i].startsWith("-")) {
                        System.err.println("Argumento no reconocido: " + args[i]);
                        return;
                    }
                    inputFile = args[i];
            }
        }

        if (modoMips && outputFile == null) {
            System.err.println("Uso: minicc input.mc -S -o output.s [-O] [--dump-ir]");
            System.err.println("-S requiere -o <archivo>");
            return;
        }

        CharStream input = (inputFile != null)
                ? CharStreams.fromFileName(inputFile)
                : CharStreams.fromStream(System.in);

        MiniCLexer lexer = new MiniCLexer(input);
        lexer.removeErrorListeners();
        MiniCErrorListener lexerErrors = new MiniCErrorListener("LÉXICO");
        lexer.addErrorListener(lexerErrors);

        // Un solo interruptor para todo el diagnóstico verboso (tokens,
        // árbol, tabla, TAC): siempre se muestra en modo diagnóstico
        // (sin -S), y también en modo -S si se pide --dump-ir — así una
        // sola invocación puede mostrar todo Y generar el .s a la vez,
        // sin necesitar dos configuraciones de Run distintas.
        boolean mostrarDiagnosticos = !modoMips || dumpIr;

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();

        if (mostrarDiagnosticos) {
            System.out.println("TOKENS");
            System.out.println("------");
            for (Token tok : tokens.getTokens()) {
                if (tok.getType() == Token.EOF) continue;
                String typeName = MiniCLexer.VOCABULARY.getSymbolicName(tok.getType());
                if (typeName == null) typeName = "'" + tok.getText() + "'";
                System.out.printf("  [%3d:%2d]  %-22s  %s%n",
                        tok.getLine(), tok.getCharPositionInLine(),
                        typeName, tok.getText());
            }
            System.out.println();
        }

        MiniCParser parser = new MiniCParser(tokens);
        parser.removeErrorListeners();
        MiniCErrorListener parserErrors = new MiniCErrorListener("SINTÁCTICO");
        parser.addErrorListener(parserErrors);

        ParseTree tree = parser.program();

        int totalErrors = lexerErrors.getErrorCount() + parserErrors.getErrorCount();

        if (mostrarDiagnosticos) {
            System.out.println("PARSE TREE — Mini-C Compiler");
            System.out.println("-----------------------------");
            System.out.println();
            if (totalErrors == 0) {
                printTree(tree, parser, "", true);
            } else {
                System.out.println("  No se muestra el árbol debido a errores.");
            }
            System.out.println();
        }

        SemanticVisitor visitor = null;
        if (totalErrors == 0) {
            if (mostrarDiagnosticos) {
                System.out.println("ANÁLISIS SEMÁNTICO — Recorrido del Visitor");
                System.out.println("-------------------------------------------");
                System.out.println();
            }
            visitor = new SemanticVisitor();
            visitor.visit(tree);
            if (mostrarDiagnosticos) {
                visitor.imprimirTabla();
            }
            totalErrors += visitor.getErrores();
        }

        if (totalErrors > 0) {
            if (modoMips) {
                System.err.println("Errores encontrados: " + totalErrors + " — no se genera MIPS.");
            }
        } else {
            TACGenerator tacGen = new TACGenerator(visitor.getTabla());
            tacGen.visit(tree);

            java.util.List<com.minic.ir.Instruccion> codigoFinal = tacGen.getCodigo();

            if (optimizar) {
                TACGenerator.imprimirCodigo(codigoFinal,
                        "CÓDIGO INTERMEDIO (TAC) — ANTES de optimizar");
                codigoFinal = com.minic.ir.OptimizadorConstantes.plegarConstantes(codigoFinal);
                TACGenerator.imprimirCodigo(codigoFinal,
                        "CÓDIGO INTERMEDIO (TAC) — DESPUÉS de optimizar (constant folding)");
            } else if (mostrarDiagnosticos) {
                TACGenerator.imprimirCodigo(codigoFinal, "CÓDIGO INTERMEDIO (TAC)");
            }

            if (modoMips) {
                MIPSGenerator mipsGen = new MIPSGenerator(codigoFinal, visitor.getTabla());
                String mips = mipsGen.generar();
                Files.writeString(Paths.get(outputFile), mips);
                System.out.println("MIPS generado en " + outputFile);
            }
        }

        if (mostrarDiagnosticos) {
            System.out.println();
            System.out.println("---------------------------------------");
            System.out.printf(" Tokens procesados  : %d%n", tokens.size());
            System.out.printf(" Errores encontrados: %d%n", totalErrors);
            System.out.println("---------------------------------------");
        }
    }

    static void printTree(ParseTree tree, MiniCParser parser, String prefix, boolean isLast) {
        String connector   = isLast ? "+-- " : "+-- ";
        String childPrefix = isLast ? "    " : "|   ";

        if (tree instanceof TerminalNode) {
            String text = tree.getText();
            System.out.println(prefix + connector + (text.equals("<EOF>") ? "<EOF>" : text));
        } else {
            RuleContext ctx = (RuleContext) tree;
            String ruleName = parser.getRuleNames()[ctx.getRuleIndex()];
            System.out.println(prefix + connector + "[" + ruleName + "]");

            int childCount = tree.getChildCount();
            for (int i = 0; i < childCount; i++) {
                printTree(tree.getChild(i), parser, prefix + childPrefix, i == childCount - 1);
            }
        }
    }
}