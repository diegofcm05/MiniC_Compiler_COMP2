int contadorGlobal;
int memoria[10];

int triple(int x) {
    return x * 3;
}

int sumarConTriple(int a, int b) {
    return triple(a) + triple(b);
}

void incrementarPuntero(int* p) {
    *p = *p + 1;
}

void llenarArreglo(int arr[10], int base) {
    int i;
    for (i = 0; i < 10; i = i + 1) {
        arr[i] = base + i;
    }
}

int sumaFila(int m[2][4], int fila) {
    int total = 0;
    int j = 0;
    while (j < 4) {
        total = total + m[fila][j];
        j = j + 1;
    }
    return total;
}

int factorial(int n) {
    if (n <= 1) {
        return 1;
    }
    return n * factorial(n - 1);
}

int main() {
    int matriz[2][4];
    matriz[0][0] = 1; matriz[0][1] = 2; matriz[0][2] = 3; matriz[0][3] = 4;
    matriz[1][0] = 5; matriz[1][1] = 6; matriz[1][2] = 7; matriz[1][3] = 8;

    print_str("Suma fila 0: ");
    print_int(sumaFila(matriz, 0));
    println();
    print_str("Suma fila 1: ");
    print_int(sumaFila(matriz, 1));
    println();

    llenarArreglo(memoria, 100);
    int i = 0;
    do {
        print_int(memoria[i]);
        print_char(' ');
        i = i + 1;
    } while (i < 10);
    println();

    contadorGlobal = sumarConTriple(2, 3);
    print_str("sumarConTriple(2,3) = ");
    print_int(contadorGlobal);
    println();

    int x = 10;
    incrementarPuntero(&x);
    incrementarPuntero(&x);
    print_str("x tras dos incrementos: ");
    print_int(x);
    println();

    print_str("factorial(6) = ");
    print_int(factorial(6));
    println();

    int j;
    int pares = 0;
    for (j = 0; j < 20; j = j + 1) {
        if (j % 2 != 0) {
            continue;
        }
        if (j == 14) {
            break;
        }
        pares = pares + 1;
    }
    print_str("pares contados antes del break: ");
    print_int(pares);
    println();

    bool esMayor = (factorial(4) > 20) && (x != 0);
    print_str("esMayor: ");
    print_bool(esMayor);
    println();

    char letra = 'Z';
    print_str("letra: ");
    print_char(letra);
    println();

    print_str("Programa completo terminado");
    println();

    return 0;
}
