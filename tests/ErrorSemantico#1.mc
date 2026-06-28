
int main() {
    int nota1 = 85;
    int nota2 = 90;
    int nota3 = 78;

    int promedio = (nota1 + nota2 + nota3) / 3;
    int promedio = 0;              // ERROR: 'promedio' ya fue declarado en este ámbito

    string mensaje = 100;           // ERROR: no se puede inicializar 'mensaje' de tipo
                                       // 'string' con valor de tipo 'int'

    print_str("Promedio: ");
    print_int(promedio);
    print_str("\n");

    return 0;
}
