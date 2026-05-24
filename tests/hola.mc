int suma_matriz(int m[  ][4], int filas) {
    int total;
    total = 0;
    int i;
    int j;
    i = 0;
    while (i < filas) {
        j = 0;
        while (j < 4) {
            total = total + m[i][j];
            j = j + 1;
        }
        i = i + 1;
    }
    return total;
}

int main() {
    int matriz[3][4];
    matriz[0][0] = 1;
    matriz[1][2] = 99;
    int res;
    res = suma_matriz(matriz, 3);
    print_int(res);
    return 0;
}