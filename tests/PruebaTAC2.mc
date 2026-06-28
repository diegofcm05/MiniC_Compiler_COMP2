// Segundo caso de prueba para TACGenerator.
// Cubre: arreglo 1D (lectura y escritura), arreglo 2D (cálculo de índice
// lineal fila*columnas+col), ciclo for con break, y llamada a función
// con argumentos.

int main() {
    int datos[5];
    int tabla[2][3];

    datos[0] = 10;
    datos[1] = datos[0] + 5;

    tabla[0][0] = 1;
    tabla[1][2] = 9;

    int valor = tabla[1][2];
    print_int(valor);

    int i;
    for (i = 0; i < 5; i = i + 1) {
        if (i == 3) {
            break;
        }
        print_int(datos[i]);
    }

    return 0;
}
