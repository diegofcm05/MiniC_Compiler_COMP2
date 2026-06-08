package com.minic;

public class Symbol {
    public String nombre;
    public String tipo;
    public String categoria;
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