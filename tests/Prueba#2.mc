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
    int tabla[2][3];
    tabla[0][0] = 1;  tabla[0][1] = 2;  tabla[0][2] = 3;
    tabla[1][0] = 4;  tabla[1][1] = 5;  tabla[1][2] = 6;

    int i = 0;
    while (i < 2) {
        print_str("Suma fila ");
        print_int(i);
        print_str(": ");
        print_int(suma_fila(tabla, i));
        print_str("\n");
        i = i + 1;
    }
    return 0;
}