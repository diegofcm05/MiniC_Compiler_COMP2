int main() {
    int i = 0;
    int contador = 0;
    while (i < 10) {
        i = i + 1;
        if (i % 2 == 0) {
            continue;
        }
        contador = contador + 1;
    }
    print_int(contador);
    println();
    print_int(i);
    println();
    return 0;
}
