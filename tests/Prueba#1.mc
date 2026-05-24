int mayor(int a, int b) {
    if (a > b) {
        return a + b / 5;
    } else {
        return a * b + 3;
    }
}

int main() {
    int x;
    int y;
    x = 15;
    y = 30;
    print_int(mayor(x, y));
    println();
    return 0;
}