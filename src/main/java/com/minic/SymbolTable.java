package com.minic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class SymbolTable {
    private Deque<Scope> pila  = new ArrayDeque<>();
    private List<Scope>  todos = new ArrayList<>();
    private int cursorReplay = 0;

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

    public void reiniciarCursor() {
        cursorReplay = 1; // el índice 0 de 'todos' es siempre "global", ya activo
        pila.clear();
        pila.push(todos.get(0));
    }


    public void entrarScopeExistente(String nombreEsperado) {
        if (cursorReplay >= todos.size()) {
            throw new IllegalStateException(
                    "No hay más scopes registrados — TACGenerator y SemanticVisitor "
                            + "recorrieron el árbol de forma distinta (se esperaba '" + nombreEsperado + "')");
        }
        Scope siguiente = todos.get(cursorReplay);
        cursorReplay++;
        pila.push(siguiente);
    }

    public void salirScope() {
        salir();
    }
}