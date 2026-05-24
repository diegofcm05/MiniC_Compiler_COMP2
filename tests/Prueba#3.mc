int main() {
    int i;
    bool encontrado;
    encontrado = false;
    i = 0;
    do {
        if (i == 5) {
            encontrado = true;
            break;
        }
        i = i + 1;
    } while (i < 10);

    if (encontrado) {
        print_str("Encontrado en: ");
        print_int(i);
    } else {
        print_str("No encontrado");
    }
    println();
    return 0;
}