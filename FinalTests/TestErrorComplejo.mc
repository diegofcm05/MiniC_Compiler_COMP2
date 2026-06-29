int global_total;

int cuadrado(int x) {
    return x * x;
}

int sumaCuadrados(int a, int b, int c) {
    return cuadrado(a) + cuadrado(b) + cuadrado(c);
}

void mostrarResultado(string etiqueta, int valor) {
    print_str(etiqueta);
    print_int(valor);
    println();
}

int main() {
    int datos[5];
    int i;
    for (i = 0; i < 5; i = i + 1) {
        datos[i] = i + 1;
    }

    int suma = 0;
    for (i = 0; i < 5; i = i + 1) {
        suma = suma + datos[i];
    }
    mostrarResultado("Suma de datos: ", suma);

    int resultado = sumaCuadrados(2, 3, 4);
    mostrarResultado("Suma de cuadrados: ", resultado);

    int j = 0;
    while (j < 5) {
        if (datos[j] > 3) {
            print_str("Dato grande encontrado: ");
            print_int(datos[j]);
            println();
        }
        j = j + 1;
    }

    int otroResultado = sumaCuadrados(1, 2);
    mostrarResultado("Otro resultado: ", otroResultado);

    global_total = suma + resultado;
    print_str("Total global: ");
    print_int(global_total);
    println();

    return 0;
}
