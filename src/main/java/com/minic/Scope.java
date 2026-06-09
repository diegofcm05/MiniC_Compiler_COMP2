package com.minic;

import java.util.LinkedHashMap;
import java.util.Map;

public class Scope {
    public String nombre;   // nombre del scope lol
    public Scope padre;     // scope padre que lo contiene
    private Map<String, Symbol> simbolos = new LinkedHashMap<>();

    public Scope(String nombre, Scope padre) {
        this.nombre = nombre;
        this.padre  = padre;
    }


    public boolean agregar(Symbol s) {
        if (simbolos.containsKey(s.nombre)) return false;
        simbolos.put(s.nombre, s);
        return true;
    }

    public Symbol buscar(String nombre) {
        if (simbolos.containsKey(nombre)) return simbolos.get(nombre);
        if (padre != null) return padre.buscar(nombre);
        return null;
    }

    public Symbol buscarLocal(String nombre) {
        return simbolos.getOrDefault(nombre, null);
    }

    public Map<String, Symbol> getSimbolos() { return simbolos; }
}