package com.minic.ir;

/**
 * Genera nombres únicos para temporales (t1, t2, ...) y etiquetas
 * (L1, L2, ...) durante la generación de TAC.
 *
 * El TACGenerator usa UNA sola instancia para todo el programa, de modo
 * que los temporales y etiquetas son únicos globalmente (no se reinician
 * por función). Esto se eligió a propósito: simplifica el backend MIPS32
 * de la Fase 4, porque no hay colisiones de nombres entre funciones al
 * momento de asignar registros o emitir etiquetas en el archivo .s.
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