int x = 10;
bool activo = true;

int suma(int a, int b) {
    int resultado = a + b;
    return resultado;
}

int factorial(int n) {
    int resultado = 1;
    if (n == 0) {
        int resultado = 99;
    }
    return resultado;
}

int main() {
    int x = 5;
    int resultado = suma(x, x);

    print_str("Suma: ");
    print_int(resultado);
    print_str("\n");

    int i = 0;
    while (i < 3) {
        int x = i;
        print_int(x);
        print_str("\n");
        i = i + 1;
    }

    print_int(factorial(5));
    print_str("\n");
    return 0;
}