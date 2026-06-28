
int main() {
    int notas[3][2];
    int totales[5];
    bool activo = true;

    notas[0][0] = 90;
    notas[0][1] = 85;

    int primera = notas[0];           // ERROR: 'notas' fue declarado con 2 dimensión(es),
                                         // se usó con 1 índice(s)

    totales[0] = 100;
    totales[5] = 200;                  // ERROR: índice 5 fuera de rango para 'totales'
                                          // (dimensión 1 tiene tamaño 5)

    int valor = totales[activo];       // ERROR: el índice de arreglo debe ser 'int',
                                          // se recibió 'bool'

    print_str("Primera nota: ");
    print_int(primera);
    print_str("\n");

    return 0;
}
