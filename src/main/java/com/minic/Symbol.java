package com.minic;

import java.util.List;

public class Symbol {
    public String nombre;
    public String tipo;
    public String categoria;
    public int linea;
    public List<String> tiposParametros;


    public int dimensiones = 0;

    public int[] tamanios = null;

    /** Posición (0-based) en la lista de parámetros de su función, o -1
     *  si este símbolo no es un parámetro (variable local, global, etc.).
     *  La usa el backend MIPS para decidir si llega por registro
     *  ($a0-$a3) o por stack al llamar a la función. */
    public int indiceParametro = -1;

    public Symbol(String nombre, String tipo, String categoria, int linea) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.categoria = categoria;
        this.linea = linea;
        this.tiposParametros = null;
    }

    public Symbol(String nombre, String tipo, String categoria, int linea, List<String> tiposParametros) {
        this(nombre, tipo, categoria, linea);
        this.tiposParametros = tiposParametros;
    }

    @Override
    public String toString() {
        return String.format("%-15s %-10s %-12s línea %d",
                nombre, tipo, categoria, linea);
    }
}