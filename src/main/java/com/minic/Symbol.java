package com.minic;

public class Symbol {
    public String nombre;
    public String tipo;       // "int", "char", "bool", "void", "string"
    public String categoria;  // "variable", "funcion", "parametro", "arreglo"
    public int linea;

    public Symbol(String nombre, String tipo, String categoria, int linea) {
        this.nombre    = nombre;
        this.tipo      = tipo;
        this.categoria = categoria;
        this.linea     = linea;
    }

    @Override
    public String toString() {
        return String.format("%-15s %-10s %-12s línea %d",
                nombre, tipo, categoria, linea);
    }
}
