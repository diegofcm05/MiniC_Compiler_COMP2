package com.minic;

import java.util.List;

public class Symbol {
    public String nombre;
    public String tipo;       // "int", "char", "bool", "void", "string"
    public String categoria;  // "variable", "funcion", "parametro", "arreglo", "puntero"
    public int linea;
    public List<String> tiposParametros; // null si no es función

    // Número de dimensiones si categoria == "arreglo" (1 para int b[5],
    // 2 para int m[3][4]). 0 para cualquier símbolo que no sea arreglo.
    // Se usa para validar que la cantidad de índices en un lvalue (b[i]
    // o m[i][j]) coincida con cómo fue declarado el arreglo.
    public int dimensiones = 0;

    // Constructor para variables, parámetros, arreglos (sin dimensiones explícitas)
    public Symbol(String nombre, String tipo, String categoria, int linea) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.categoria = categoria;
        this.linea = linea;
        this.tiposParametros = null;
    }

    // Constructor para funciones (guarda tipos de parámetros para chequeo de aridad)
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