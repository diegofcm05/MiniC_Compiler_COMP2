
int area(int base, int altura) {
    return base * altura;
}

bool esValida(int valor) {
    if (valor > 0) {
        return "si";              // ERROR: se esperaba retornar 'bool', se recibió 'string'
    }
    return false;
}

int main() {
    int base = 10;
    int alto = 5;

    int resultado = area(base, alto, 2);   // ERROR: 'area' espera 2 argumento(s),
                                              // se recibieron 3

    print_str("Area: ");
    print_int(resultado);
    print_str("\n");

    if (altura > 0) {                        // ERROR: 'altura' no fue declarado
        print_str("Altura valida\n");
    }

    return 0;
}
