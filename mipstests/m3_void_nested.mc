void saludar(int veces) {
    int i;
    for (i = 0; i < veces; i = i + 1) {
        print_str("Hola ");
    }
    println();
}

int doble(int x) {
    return x * 2;
}

int main() {
    saludar(3);
    print_int(doble(doble(5)));
    println();
    return 0;
}
