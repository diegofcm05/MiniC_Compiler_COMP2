void llenar(int arr[5], int valor) {
    int i;
    for (i = 0; i < 5; i = i + 1) {
        arr[i] = valor * (i + 1);
    }
}

int main() {
    int datos[5];
    llenar(datos, 3);
    int i = 0;
    while (i < 5) {
        print_int(datos[i]);
        println();
        i = i + 1;
    }
    return 0;
}
