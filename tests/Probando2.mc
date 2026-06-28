int suma(int a, int b) {
    return a + b;
}

void imprimir(int valor) {
    print_int(valor);
}

int factorial(int n) {
    if (n <= 1) {
        return 1;
    }
    return n * factorial(n - 1);
}

void sinRetorno(int x) {
    int resultado = x * 2;
}

int main() {

    int r1 = suma(5);

    int r2 = suma(5, 10, 3);

    int r3 = imprimir(5);

    void r4 = suma(1, 2);

    sinRetorno(10);

    int r5 = sinRetorno(10);

    suma(5, 10);

    int r6 = noExisteFunc(5);

    return 0;
}