package com.minic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class SymbolTable {
    private Deque<Scope> pila   = new ArrayDeque<>();
    // Historial de todos los scopes en orden de creación (para impresión)
    private List<Scope>  todos  = new ArrayList<>();

    // Entrar a un nuevo ámbito
    public void entrar(String nombre) {
        Scope padre = pila.isEmpty() ? null : pila.peek();
        Scope nuevo = new Scope(nombre, padre);
        pila.push(nuevo);
        todos.add(nuevo);   // registrar para impresión posterior
    }

    // Salir del ámbito actual (sin descartarlo — ya está en `todos`)
    public void salir() {
        if (!pila.isEmpty()) pila.pop();
    }

    // Agregar símbolo al ámbito actual
    public boolean agregar(Symbol s) {
        if (pila.isEmpty()) return false;
        return pila.peek().agregar(s);
    }

    // Buscar símbolo desde el ámbito actual hacia arriba
    public Symbol buscar(String nombre) {
        return pila.isEmpty() ? null : pila.peek().buscar(nombre);
    }

    // Ámbito actual
    public Scope ambitoActual() {
        return pila.isEmpty() ? null : pila.peek();
    }

    // Todos los scopes registrados (para imprimirTabla)
    public List<Scope> getTodos() { return todos; }
}