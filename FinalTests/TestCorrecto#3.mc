void intercambiar(int* a, int* b) {
    int temp = *a;
    *a = *b;
    *b = temp;
}

void incrementar(int* p) {
    *p = *p + 1;
}

int main() {
    int x = 10;
    int y = 20;

    intercambiar(&x, &y);
    print_int(x);
    println();
    print_int(y);
    println();

    int datos[3];
    datos[0] = 5;
    incrementar(&datos[0]);
    print_int(datos[0]);
    println();

    int* p = &x;
    *p = 99;
    print_int(x);
    println();

    return 0;
}
