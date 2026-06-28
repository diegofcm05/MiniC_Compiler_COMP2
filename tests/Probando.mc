int x = 10;
bool activo = true;

int suma(int a, int b) {
    int resultado = a + b;
    return resultado;
}

int factorial(int n) {
    int resultado = 1;
    if (n == 0) {
        int resultado = 99;
    }
    return resultado;
}
/*
int sumar(int a, int b) {
    return "hola";      // ERROR: se esperaba 'int', se recibió 'string'
}

void saludar() {
    return 5;            // ERROR: función 'void' no puede retornar un valor
}

int factoriales(int n) {
    return;               // ERROR: función de tipo 'int' debe retornar un valor
}

int main() {
    int arr[5] = 3;       // ERROR: no se puede inicializar el arreglo con un valor escalar
    return 0;
}
*/


void saludar() {
    print_str("hola\n");
    return;                 // OK, void sin valor
}

int main() {
    int x = 5;
    int resultado = suma(x, x);

    print_str("Suma: ");
    print_int(resultado);
    print_str("\n");

    int i = 0;
    int j = 0;
    int q = 0;
    while (i < 3) {
        for (j = 1; j <= 10; j = j + 1) {
                if (j == 5) {
                    if ( i == 2){
                        int post = 0;
                        continue;
                    }
                }
                q = q + j * i;
            }
        int x = i;
        print_int(x);
        print_str("\n");
        i = i + 1;
    }


    print_int(factorial(5));
    print_str("\n");
    return 0;
}