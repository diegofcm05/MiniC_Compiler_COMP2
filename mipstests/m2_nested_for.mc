int main() {
    int total = 0;
    int i;
    int j;
    for (i = 1; i <= 3; i = i + 1) {
        for (j = 1; j <= 3; j = j + 1) {
            total = total + i * j;
        }
    }
    print_int(total);
    println();
    return 0;
}
