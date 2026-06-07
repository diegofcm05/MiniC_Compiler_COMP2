package com.minic;

import java.util.ArrayDeque;
import java.util.Deque;

public class SymbolTable {
    private Deque<Scope> pila = new ArrayDeque<>();

    // Entrar a un nuevo ámbito
    public void entrar(String nombre) {
        Scope padre = pila.isEmpty() ? null : pila.peek();
        pila.push(new Scope(nombre, padre));
    }

    // Salir del ámbito actual
    public void salir() {
        if (!pila.isEmpty()) pila.pop();
    }

    // Agregar símbolo al ámbito actual
    public boolean agregar(Symbol s) {
        return pila.peek().agregar(s);
    }

    // Buscar símbolo desde el ámbito actual hacia arriba
    public Symbol buscar(String nombre) {
        return pila.isEmpty() ? null : pila.peek().buscar(nombre);
    }

    // Ámbito actual
    public Scope ambito() {
        return pila.peek();
    }
}
