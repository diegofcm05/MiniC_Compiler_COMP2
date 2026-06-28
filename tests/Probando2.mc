int main() {

    while (true) {
        if (true) {
            break;             // OK — el while exterior cuenta
            continue;          // OK
        }
    }

    int i = 0;
    for (i = 0; i < 5; i = i + 1) {
        while (true) {
            break;             // OK — rompe el while interior
        }
        continue;               // OK — el for exterior cuenta
    }

    return 0;
}