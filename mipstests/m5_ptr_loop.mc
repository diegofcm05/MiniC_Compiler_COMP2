void duplicar(int* p) {
    *p = *p * 2;
}

int main() {
    int valores[4];
    valores[0] = 1;
    valores[1] = 2;
    valores[2] = 3;
    valores[3] = 4;

    int i = 0;
    while (i < 4) {
        duplicar(&valores[i]);
        i = i + 1;
    }

    i = 0;
    while (i < 4) {
        print_int(valores[i]);
        println();
        i = i + 1;
    }

    return 0;
}2, 4