package com.minic.ir;

import java.util.ArrayList;
import java.util.List;

/**
 * Optimización obligatoria de Fase 4 (M8): constant folding sobre el TAC.
 *
 * Recorre cada instrucción BINARIA/UNARIA y, si sus operandos ya son
 * constantes en tiempo de compilación (literales enteros o booleanos —
 * no variables ni temporales), evalúa el resultado ahí mismo y reemplaza
 * la instrucción por una ASIGNAR directa, eliminando esa operación por
 * completo del código que se ejecuta en MIPS.
 *
 * Es una pasada puramente LOCAL — mira cada instrucción de forma aislada,
 * sin necesitar análisis de flujo de datos entre instrucciones (a
 * diferencia de copy propagation o DCE, que necesitan rastrear usos a
 * través de saltos y bucles). Por eso es segura de aplicar sin importar
 * la estructura de control del programa.
 *
 * Ejemplo: "t1 = 5 * 3" se reemplaza por "t1 = 15" — la multiplicación
 * nunca llega a generar una instrucción 'mul' en el .s final.
 */
public class OptimizadorConstantes {

    public static List<Instruccion> plegarConstantes(List<Instruccion> entrada) {
        List<Instruccion> salida = new ArrayList<>(entrada.size());
        for (Instruccion ins : entrada) {
            salida.add(plegar(ins));
        }
        return salida;
    }

    private static Instruccion plegar(Instruccion ins) {
        if (ins.operador == OpTAC.BINARIA) {
            Integer a = valorConstante(ins.op1);
            Integer b = valorConstante(ins.op2);
            if (a != null && b != null) {
                String resultado = evaluarBinaria(ins.simbolo, a, b);
                if (resultado != null) {
                    return Instruccion.asignar(ins.destino, resultado);
                }
            }
        } else if (ins.operador == OpTAC.UNARIA) {
            Integer a = valorConstante(ins.op1);
            if (a != null) {
                String resultado = evaluarUnaria(ins.simbolo, a);
                if (resultado != null) {
                    return Instruccion.asignar(ins.destino, resultado);
                }
            }
        }
        return ins; // no es plegable — se deja exactamente igual
    }

    /** Interpreta un operando TAC como entero en tiempo de compilación, o
     *  null si no es una constante plegable (variable, temporal, etc.).
     *  bool se trata como 0/1 para poder plegar comparaciones y operadores
     *  lógicos por el mismo camino que los aritméticos. Los literales de
     *  char y string no se pliegan todavía (caso poco común, fuera de
     *  alcance por ahora). */
    private static Integer valorConstante(String operando) {
        if (operando == null) return null;
        if (operando.equals("true"))  return 1;
        if (operando.equals("false")) return 0;
        if (operando.matches("-?\\d+")) return Integer.parseInt(operando);
        return null;
    }

    private static String evaluarBinaria(String op, int a, int b) {
        switch (op) {
            case "+": return String.valueOf(a + b);
            case "-": return String.valueOf(a - b);
            case "*": return String.valueOf(a * b);
            // División/módulo por cero: NO se pliega — que falle en
            // ejecución (como en MIPS real), no silenciosamente en
            // tiempo de compilación.
            case "/": return (b != 0) ? String.valueOf(a / b) : null;
            case "%": return (b != 0) ? String.valueOf(a % b) : null;
            case "<":  return String.valueOf(a < b);
            case ">":  return String.valueOf(a > b);
            case "<=": return String.valueOf(a <= b);
            case ">=": return String.valueOf(a >= b);
            case "==": return String.valueOf(a == b);
            case "!=": return String.valueOf(a != b);
            case "&&": return String.valueOf(a != 0 && b != 0);
            case "||": return String.valueOf(a != 0 || b != 0);
            default: return null;
        }
    }

    private static String evaluarUnaria(String op, int a) {
        switch (op) {
            case "-": return String.valueOf(-a);
            case "!": return String.valueOf(a == 0);
            default: return null;
        }
    }
}