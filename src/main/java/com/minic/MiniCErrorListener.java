package com.minic;

import org.antlr.v4.runtime.*;

public class MiniCErrorListener extends BaseErrorListener {

    private int errorCount = 0;
    private final String tipo;

    public MiniCErrorListener(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line, int charPositionInLine,
                            String msg, RecognitionException e) {
        errorCount++;


        String simbolo = "";
        if (offendingSymbol instanceof Token t) {
            simbolo = " → token: '" + t.getText() + "'";
        }

        System.err.printf("[ERROR %s] línea %d, col %d%s%n",
                tipo, line, charPositionInLine + 1, simbolo);
        System.err.printf("           %s%n%n", traducirMensaje(msg));
    }

    public int getErrorCount() {
        return errorCount;
    }

    private String traducirMensaje(String msg) {
        if (msg.contains("mismatched input")) {
            return "Token inesperado en esta posición";
        } else if (msg.contains("missing")) {
            return "Falta un token requerido";
        } else if (msg.contains("extraneous input")) {
            return "Token extra que no corresponde aquí";
        } else if (msg.contains("no viable alternative")) {
            return "Construcción no reconocida por la gramática";
        } else if (msg.contains("token recognition error")) {
            return "Caracter no reconocido por el lenguaje";
        }
        return msg;
    }
}