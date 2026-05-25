void imprimir_separador() {
    print_str("--- --- ---\n");
}

int main() {
    int contador = 0;
    char letra = 'A';
    char tab = '\t';
    bool activo = true;

    while (activo) {
        print_str("Iteracion:\t");
        print_int(contador);
        print_str("\n");
        contador = contador + 1;
        if (contador == 3) {
            activo = false;
        }
    }

    imprimir_separador();
    print_str("Letra inicial: ");
    print_char(letra);
    print_str("\n");
    return 0;
}