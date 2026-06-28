package com.minic.ir;

/**
 * Genera nombres únicos para temporales (t1, t2, ...) y etiquetas
 * (L1, L2, ...) durante la generación de TAC. Cada función debe usar
 * su propia instancia (o resetear los contadores) para que los
 * temporales no se compartan innecesariamente entre funciones —
 * aunque tampoco es un error si los números se repiten entre
 * funciones distintas, ya que sus ámbitos en MIPS serán independientes.
 */
public class GeneradorNombres {

    private int contadorTemporales = 0;
    private int contadorEtiquetas  = 0;

    /** Genera el siguiente temporal disponible, ej: "t1", "t2", ... */
    public String nuevoTemporal() {
        contadorTemporales++;
        return "t" + contadorTemporales;
    }

    /** Genera la siguiente etiqueta disponible, ej: "L1", "L2", ... */
    public String nuevaEtiqueta() {
        contadorEtiquetas++;
        return "L" + contadorEtiquetas;
    }
}