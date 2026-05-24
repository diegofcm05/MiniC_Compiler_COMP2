int main() {
    int numeros[5];
    int i;
    for (i = 0; i < 5; i = i + 1) {
        numeros[i] = i * i;
    }
    for (i = 0; i < 5; i = i + 1) {
        print_int(numeros[i]);
        println();
    }
    return 0;
}