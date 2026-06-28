// ════════════════════════════════════════════════════════════════════════
// PRUEBA INTEGRAL — Fase 2: Commits 1-4 del SemanticVisitor
// Cada bloque está marcado con el commit que ejercita y si DEBE o NO DEBE
// dar error. Para probar por partes, comenta los bloques que no quieras
// ejecutar en ese momento (los errores se acumulan todos en una corrida).
// ════════════════════════════════════════════════════════════════════════

int suma(int a, int b) {
    return a + b;              // OK — return int en función int
}

void saludar() {
    print_str("Hola\n");
    return;                     // OK — return vacío en función void
}

int main() {

    // ─── COMMIT 1: chequeo de tipos en expresiones ────────────────────────
    int x = 10;                  // OK
    bool activo = true;          // OK
    char letra = 'A';            // OK

    int y = 3 + 4;               // OK — aritmético entre int
    bool r1 = (5 == 5);          // OK — igualdad entre mismo tipo
    bool r2 = (3 < 7);           // OK — relacional entre int
    bool r3 = (activo && r1);    // OK — lógico entre bool

    string err1 = 5;          // ERROR (Commit 1): inicializar string con int
    int err2 = 3 + true;      // ERROR (Commit 1): aritmético con bool
    bool err3 = (5 == "x");   // ERROR (Commit 1): igualdad entre tipos distintos

    // ─── COMMIT 1 (extra): condiciones de control deben ser bool ──────────
    if (r1) {                    // OK — bool
        print_str("r1 es true\n");
    }
    while (activo) {             // OK — bool
        activo = false;
    }
    if (5) { }                // ERROR (Commit 1): condición de 'if' debe ser bool
    while ("A") { }           // ERROR (Commit 1): condición de 'while' debe ser bool

    // ─── COMMIT 2: return con tipo incorrecto + arreglo con escalar ───────
    // (las funciones de arriba, suma() y saludar(), ya muestran los casos OK)

    int arrErr[5] = 3;       // ERROR (Commit 2): no se puede inicializar arreglo con escalar
    int arrOk[5];                 // OK — arreglo sin inicializar

    // ─── COMMIT 3: aridad y tipos en llamadas a función ────────────────────
    int z = suma(1, 2);           // OK — aridad y tipos correctos
    print_int(z);                  // OK — runtime con tipo real (int)
    print_str("texto\n");          // OK — runtime con tipo real (string)

    int err4 = suma(1, 2, 3); // ERROR (Commit 3): espera 2 argumentos, se recibieron 3
    int err5 = suma(1, "a");  // ERROR (Commit 3): argumento 2 debe ser int, se recibió string
    print_int("hola");         // ERROR (Commit 3): argumento 1 de print_int debe ser int

    // ─── COMMIT 4: break/continue solo dentro de loops ─────────────────────
    while (true) {
        if (true) {
            break;                 // OK — el while exterior lo permite
        }
    }

    int i;
    for (i = 0; i < 3; i = i + 1) {
        if (i == 1) {
            continue;               // OK — el for exterior lo permite
        }
        while (true) {
            break;                  // OK — rompe el while interior
        }
    }

    break;                     // ERROR (Commit 4): 'break' fuera de un ciclo
    continue;                  // ERROR (Commit 4): 'continue' fuera de un ciclo

    saludar();
    return 0;
}
