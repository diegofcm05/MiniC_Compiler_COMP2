int main() {
    int b[5];
    int m[3][4];

    b[2] = 1;            // OK
    b[2+1] = 1;            // OK — evalúa a 3, dentro de rango
    m[1][3] = 1;            // OK
    m[(1+1)][1] = 1;        // OK — evalúa a 2, dentro de rango

    int i = 10;
    b[i] = 1;               // OK — no se chequea, índice no es constante

    b[5] = 1;           // ERROR: índice 5 fuera de rango (tamaño 5, válido 0-4)
    b[-1] = 1;            // ERROR: índice -1 fuera de rango
    b[10-5] = 1;          // ERROR: evalúa a 5, fuera de rango
    m[3][0] = 1;          // ERROR: índice 3 fuera de rango (dimensión 1 tiene tamaño 3)

    return 0;
}