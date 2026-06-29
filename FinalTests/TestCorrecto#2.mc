int suma(int a, int b) {
    return a + b;
}

int sumar6(int a, int b, int c, int d, int e, int f) {
    return a + b + c + d + e + f;
}

int factorial(int n) {
    if (n <= 1) {
        return 1;
    }
    return n * factorial(n - 1);
}

int main() {
    print_int(suma(3, 4));
    println();
    print_int(sumar6(1, 2, 3, 4, 5, 6));
    println();
    print_int(factorial(5));
    println();
    return 0;
}
