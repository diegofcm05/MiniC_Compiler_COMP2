// Prueba simple para verificar la generación de TAC.
// Cubre: declaración con inicialización, operación aritmética,
// condición if/else, ciclo while con break/continue, y llamada a función.

int main() {
    int x = 5;
    int y = 10;
    int suma = x + y;

    if (suma > 10) {
        print_int(suma);
    } else {
        print_str("suma pequena\n");
    }

    int i = 0;
    while (i < 3) {
        if (i == 1) {
            i = i + 1;
            continue;
        }
        print_int(i);
        i = i + 1;
    }

    return 0;
}
