int main() {
    int b[5];
    int m[3][4];
    int x;

    b[2] = 10;          // OK
    m[1][2] = 5;          // OK
    x = b[0];              // OK — retorna int

   // b = m;             // ERROR: 'b' sin índices (esto es lo nuevo del Commit 6)
   b[5] = 8;
    m = b;              // ERROR: 'm' sin índices
    b[1][2] = 5;       // ERROR: b declarado con 1 dimensión, usado con 2
    //[1] = 5;            // ERROR: m declarado con 2 dimensiones, usado con 1
    //x[0] = 5;            // ERROR: 'x' no es arreglo

    return 0;
}