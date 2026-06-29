// Prueba de paso por referencia mediante punteros (Camino B).
// Cubre: '&' (dirección de variable y de elemento de arreglo),
// '*' como lectura y como destino de asignación, y un parámetro
// de tipo puntero (int*) modificando una variable del llamador.

void intercambiar(int* a, int* b) {
    int temp = *a;
    *a = *b;
    *b = temp;
}

void incrementar(int* p) {
    *p = *p + 1;
}

int main() {
    int x = 10;
    int y = 20;

    intercambiar(&x, &y);
    print_int(x);   // 20
    print_int(y);   // 10

    int datos[3];
    datos[0] = 5;
    incrementar(&datos[0]);
    print_int(datos[0]);   // 6

    int* p = &x;
    *p = 99;
    print_int(x);   // 99

    return 0;
}
