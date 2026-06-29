int contadorGlobal[3];

int suma_fila(int m[2][3], int fila) {
    int total = 0;
    int j = 0;
    while (j < 3) {
        total = total + m[fila][j];
        j = j + 1;
    }
    return total;
}

int main() {
    int datos[5];
    int i = 0;
    while (i < 5) {
        datos[i] = i * 10;
        i = i + 1;
    }
    print_int(datos[3]);
    println();

    int tabla[2][3];
    tabla[0][0] = 1; tabla[0][1] = 2; tabla[0][2] = 3;
    tabla[1][0] = 4; tabla[1][1] = 5; tabla[1][2] = 6;
    print_int(suma_fila(tabla, 1));
    println();

    contadorGlobal[0] = 100;
    contadorGlobal[1] = contadorGlobal[0] + 1;
    print_int(contadorGlobal[1]);
    println();

    return 0;
}
