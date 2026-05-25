int main() {
    int a;
    int b;
    int i;

    a = b = 0;

    for (i = 1; i <= 10; i = i + 1) {
        if (i == 5) {
            continue;
        }
        a = a + i;
    }

    print_str("Suma sin 5: ");
    print_int(a);
    print_str("\n");

    for (i = 0; i < 3; i = i + 1) {
        b = b + i * i;
    }

    print_str("Suma cuadrados: ");
    print_int(b);
    print_str("\n");
    return 0;
}