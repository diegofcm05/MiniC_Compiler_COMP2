package com.minic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class SymbolTable {
    private Deque<Scope> pila   = new ArrayDeque<>();
    private List<Scope>  todos  = new ArrayList<>();

    public void entrar(String nombre) {
        Scope padre = pila.isEmpty() ? null : pila.peek();
        Scope nuevo = new Scope(nombre, padre);
        pila.push(nuevo);
        todos.add(nuevo);
    }

    public void salir() {
        if (!pila.isEmpty()) pila.pop();
    }

    public boolean agregar(Symbol s) {
        if (pila.isEmpty()) return false;
        return pila.peek().agregar(s);
    }

    public Symbol buscar(String nombre) {
        return pila.isEmpty() ? null : pila.peek().buscar(nombre);
    }

    public Scope ambitoActual() {
        return pila.isEmpty() ? null : pila.peek();
    }

    public List<Scope> getTodos() { return todos; }
}