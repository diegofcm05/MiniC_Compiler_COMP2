.data
contadorGlobal: .word 0
memoria: .space 40
str0: .asciiz "Suma fila 0: "
str1: .asciiz "Suma fila 1: "
str2: .asciiz "sumarConTriple(2,3) = "
str3: .asciiz "x tras dos incrementos: "
str4: .asciiz "factorial(6) = "
str5: .asciiz "pares contados antes del break: "
str6: .asciiz "esMayor: "
str7: .asciiz "letra: "
str8: .asciiz "Programa completo terminado"

.text
triple:
    subu $sp, $sp, 16
    sw $ra, 12($sp)
    sw $fp, 8($sp)
    move $fp, $sp
    sw $a0, 0($fp)
    lw $t0, 0($fp)
    li $t1, 3
    mul $t2, $t0, $t1
    sw $t2, 4($fp)
    lw $v0, 4($fp)
    j _epilogo_triple
_epilogo_triple:
    lw $ra, 12($sp)
    lw $fp, 8($sp)
    addu $sp, $sp, 16
    jr $ra
sumarConTriple:
    subu $sp, $sp, 32
    sw $ra, 28($sp)
    sw $fp, 24($sp)
    move $fp, $sp
    sw $a0, 0($fp)
    sw $a1, 4($fp)
    lw $a0, 0($fp)
    jal triple
    sw $v0, 8($fp)
    lw $a0, 4($fp)
    jal triple
    sw $v0, 12($fp)
    lw $t0, 8($fp)
    lw $t1, 12($fp)
    add $t2, $t0, $t1
    sw $t2, 16($fp)
    lw $v0, 16($fp)
    j _epilogo_sumarConTriple
_epilogo_sumarConTriple:
    lw $ra, 28($sp)
    lw $fp, 24($sp)
    addu $sp, $sp, 32
    jr $ra
incrementarPuntero:
    subu $sp, $sp, 24
    sw $ra, 20($sp)
    sw $fp, 16($sp)
    move $fp, $sp
    sw $a0, 0($fp)
    lw $t0, 0($fp)
    lw $t1, 0($t0)
    sw $t1, 4($fp)
    lw $t0, 4($fp)
    li $t1, 1
    add $t2, $t0, $t1
    sw $t2, 8($fp)
    lw $t0, 0($fp)
    lw $t1, 8($fp)
    sw $t1, 0($t0)
_epilogo_incrementarPuntero:
    lw $ra, 20($sp)
    lw $fp, 16($sp)
    addu $sp, $sp, 24
    jr $ra
llenarArreglo:
    subu $sp, $sp, 32
    sw $ra, 28($sp)
    sw $fp, 24($sp)
    move $fp, $sp
    sw $a0, 0($fp)
    sw $a1, 4($fp)
    li $t0, 0
    sw $t0, 8($fp)
L1:
    lw $t0, 8($fp)
    li $t1, 10
    slt $t2, $t0, $t1
    sw $t2, 12($fp)
    lw $t0, 12($fp)
    beq $t0, $zero, L3
    lw $t0, 4($fp)
    lw $t1, 8($fp)
    add $t2, $t0, $t1
    sw $t2, 16($fp)
    lw $t0, 0($fp)
    lw $t1, 8($fp)
    sll $t1, $t1, 2
    addu $t0, $t0, $t1
    lw $t2, 16($fp)
    sw $t2, 0($t0)
L2:
    lw $t0, 8($fp)
    li $t1, 1
    add $t2, $t0, $t1
    sw $t2, 20($fp)
    lw $t0, 20($fp)
    sw $t0, 8($fp)
    j L1
L3:
_epilogo_llenarArreglo:
    lw $ra, 28($sp)
    lw $fp, 24($sp)
    addu $sp, $sp, 32
    jr $ra
sumaFila:
    subu $sp, $sp, 48
    sw $ra, 44($sp)
    sw $fp, 40($sp)
    move $fp, $sp
    sw $a0, 0($fp)
    sw $a1, 4($fp)
    li $t0, 0
    sw $t0, 8($fp)
    li $t0, 0
    sw $t0, 12($fp)
L4:
    lw $t0, 12($fp)
    li $t1, 4
    slt $t2, $t0, $t1
    sw $t2, 16($fp)
    lw $t0, 16($fp)
    beq $t0, $zero, L5
    lw $t0, 4($fp)
    li $t1, 4
    mul $t2, $t0, $t1
    sw $t2, 20($fp)
    lw $t0, 20($fp)
    lw $t1, 12($fp)
    add $t2, $t0, $t1
    sw $t2, 24($fp)
    lw $t0, 0($fp)
    lw $t1, 24($fp)
    sll $t1, $t1, 2
    addu $t0, $t0, $t1
    lw $t2, 0($t0)
    sw $t2, 28($fp)
    lw $t0, 8($fp)
    lw $t1, 28($fp)
    add $t2, $t0, $t1
    sw $t2, 32($fp)
    lw $t0, 32($fp)
    sw $t0, 8($fp)
    lw $t0, 12($fp)
    li $t1, 1
    add $t2, $t0, $t1
    sw $t2, 36($fp)
    lw $t0, 36($fp)
    sw $t0, 12($fp)
    j L4
L5:
    lw $v0, 8($fp)
    j _epilogo_sumaFila
_epilogo_sumaFila:
    lw $ra, 44($sp)
    lw $fp, 40($sp)
    addu $sp, $sp, 48
    jr $ra
factorial:
    subu $sp, $sp, 32
    sw $ra, 28($sp)
    sw $fp, 24($sp)
    move $fp, $sp
    sw $a0, 0($fp)
    lw $t0, 0($fp)
    li $t1, 1
    sle $t2, $t0, $t1
    sw $t2, 4($fp)
    lw $t0, 4($fp)
    beq $t0, $zero, L6
    li $v0, 1
    j _epilogo_factorial
L6:
    lw $t0, 0($fp)
    li $t1, 1
    sub $t2, $t0, $t1
    sw $t2, 8($fp)
    lw $a0, 8($fp)
    jal factorial
    sw $v0, 12($fp)
    lw $t0, 0($fp)
    lw $t1, 12($fp)
    mul $t2, $t0, $t1
    sw $t2, 16($fp)
    lw $v0, 16($fp)
    j _epilogo_factorial
_epilogo_factorial:
    lw $ra, 28($sp)
    lw $fp, 24($sp)
    addu $sp, $sp, 32
    jr $ra
main:
    subu $sp, $sp, 216
    sw $ra, 212($sp)
    sw $fp, 208($sp)
    move $fp, $sp
    li $t0, 0
    li $t1, 4
    mul $t2, $t0, $t1
    sw $t2, 56($fp)
    lw $t0, 56($fp)
    li $t1, 0
    add $t2, $t0, $t1
    sw $t2, 60($fp)
    addu $t0, $fp, 0
    lw $t1, 60($fp)
    sll $t1, $t1, 2
    addu $t0, $t0, $t1
    li $t2, 1
    sw $t2, 0($t0)
    li $t0, 0
    li $t1, 4
    mul $t2, $t0, $t1
    sw $t2, 64($fp)
    lw $t0, 64($fp)
    li $t1, 1
    add $t2, $t0, $t1
    sw $t2, 68($fp)
    addu $t0, $fp, 0
    lw $t1, 68($fp)
    sll $t1, $t1, 2
    addu $t0, $t0, $t1
    li $t2, 2
    sw $t2, 0($t0)
    li $t0, 0
    li $t1, 4
    mul $t2, $t0, $t1
    sw $t2, 72($fp)
    lw $t0, 72($fp)
    li $t1, 2
    add $t2, $t0, $t1
    sw $t2, 76($fp)
    addu $t0, $fp, 0
    lw $t1, 76($fp)
    sll $t1, $t1, 2
    addu $t0, $t0, $t1
    li $t2, 3
    sw $t2, 0($t0)
    li $t0, 0
    li $t1, 4
    mul $t2, $t0, $t1
    sw $t2, 80($fp)
    lw $t0, 80($fp)
    li $t1, 3
    add $t2, $t0, $t1
    sw $t2, 84($fp)
    addu $t0, $fp, 0
    lw $t1, 84($fp)
    sll $t1, $t1, 2
    addu $t0, $t0, $t1
    li $t2, 4
    sw $t2, 0($t0)
    li $t0, 1
    li $t1, 4
    mul $t2, $t0, $t1
    sw $t2, 88($fp)
    lw $t0, 88($fp)
    li $t1, 0
    add $t2, $t0, $t1
    sw $t2, 92($fp)
    addu $t0, $fp, 0
    lw $t1, 92($fp)
    sll $t1, $t1, 2
    addu $t0, $t0, $t1
    li $t2, 5
    sw $t2, 0($t0)
    li $t0, 1
    li $t1, 4
    mul $t2, $t0, $t1
    sw $t2, 96($fp)
    lw $t0, 96($fp)
    li $t1, 1
    add $t2, $t0, $t1
    sw $t2, 100($fp)
    addu $t0, $fp, 0
    lw $t1, 100($fp)
    sll $t1, $t1, 2
    addu $t0, $t0, $t1
    li $t2, 6
    sw $t2, 0($t0)
    li $t0, 1
    li $t1, 4
    mul $t2, $t0, $t1
    sw $t2, 104($fp)
    lw $t0, 104($fp)
    li $t1, 2
    add $t2, $t0, $t1
    sw $t2, 108($fp)
    addu $t0, $fp, 0
    lw $t1, 108($fp)
    sll $t1, $t1, 2
    addu $t0, $t0, $t1
    li $t2, 7
    sw $t2, 0($t0)
    li $t0, 1
    li $t1, 4
    mul $t2, $t0, $t1
    sw $t2, 112($fp)
    lw $t0, 112($fp)
    li $t1, 3
    add $t2, $t0, $t1
    sw $t2, 116($fp)
    addu $t0, $fp, 0
    lw $t1, 116($fp)
    sll $t1, $t1, 2
    addu $t0, $t0, $t1
    li $t2, 8
    sw $t2, 0($t0)
    la $a0, str0
    li $v0, 4
    syscall
    addu $t0, $fp, 0
    sw $t0, 120($fp)
    lw $a0, 120($fp)
    li $a1, 0
    jal sumaFila
    sw $v0, 124($fp)
    lw $a0, 124($fp)
    li $v0, 1
    syscall
    li $a0, 10
    li $v0, 11
    syscall
    la $a0, str1
    li $v0, 4
    syscall
    addu $t0, $fp, 0
    sw $t0, 128($fp)
    lw $a0, 128($fp)
    li $a1, 1
    jal sumaFila
    sw $v0, 132($fp)
    lw $a0, 132($fp)
    li $v0, 1
    syscall
    li $a0, 10
    li $v0, 11
    syscall
    la $t0, memoria
    sw $t0, 136($fp)
    lw $a0, 136($fp)
    li $a1, 100
    jal llenarArreglo
    li $t0, 0
    sw $t0, 32($fp)
L7:
    la $t0, memoria
    lw $t1, 32($fp)
    sll $t1, $t1, 2
    addu $t0, $t0, $t1
    lw $t2, 0($t0)
    sw $t2, 140($fp)
    lw $a0, 140($fp)
    li $v0, 1
    syscall
    li $a0, 32
    li $v0, 11
    syscall
    lw $t0, 32($fp)
    li $t1, 1
    add $t2, $t0, $t1
    sw $t2, 144($fp)
    lw $t0, 144($fp)
    sw $t0, 32($fp)
L9:
    lw $t0, 32($fp)
    li $t1, 10
    slt $t2, $t0, $t1
    sw $t2, 148($fp)
    lw $t0, 148($fp)
    bne $t0, $zero, L7
L8:
    li $a0, 10
    li $v0, 11
    syscall
    li $a0, 2
    li $a1, 3
    jal sumarConTriple
    sw $v0, 152($fp)
    lw $t0, 152($fp)
    sw $t0, contadorGlobal
    la $a0, str2
    li $v0, 4
    syscall
    lw $a0, contadorGlobal
    li $v0, 1
    syscall
    li $a0, 10
    li $v0, 11
    syscall
    li $t0, 10
    sw $t0, 36($fp)
    addu $t0, $fp, 36
    sw $t0, 156($fp)
    lw $a0, 156($fp)
    jal incrementarPuntero
    addu $t0, $fp, 36
    sw $t0, 160($fp)
    lw $a0, 160($fp)
    jal incrementarPuntero
    la $a0, str3
    li $v0, 4
    syscall
    lw $a0, 36($fp)
    li $v0, 1
    syscall
    li $a0, 10
    li $v0, 11
    syscall
    la $a0, str4
    li $v0, 4
    syscall
    li $a0, 6
    jal factorial
    sw $v0, 164($fp)
    lw $a0, 164($fp)
    li $v0, 1
    syscall
    li $a0, 10
    li $v0, 11
    syscall
    li $t0, 0
    sw $t0, 44($fp)
    li $t0, 0
    sw $t0, 40($fp)
L10:
    lw $t0, 40($fp)
    li $t1, 20
    slt $t2, $t0, $t1
    sw $t2, 168($fp)
    lw $t0, 168($fp)
    beq $t0, $zero, L12
    lw $t0, 40($fp)
    li $t1, 2
    div $t0, $t1
    mfhi $t2
    sw $t2, 172($fp)
    lw $t0, 172($fp)
    li $t1, 0
    sne $t2, $t0, $t1
    sw $t2, 176($fp)
    lw $t0, 176($fp)
    beq $t0, $zero, L13
    j L11
L13:
    lw $t0, 40($fp)
    li $t1, 14
    seq $t2, $t0, $t1
    sw $t2, 180($fp)
    lw $t0, 180($fp)
    beq $t0, $zero, L14
    j L12
L14:
    lw $t0, 44($fp)
    li $t1, 1
    add $t2, $t0, $t1
    sw $t2, 184($fp)
    lw $t0, 184($fp)
    sw $t0, 44($fp)
L11:
    lw $t0, 40($fp)
    li $t1, 1
    add $t2, $t0, $t1
    sw $t2, 188($fp)
    lw $t0, 188($fp)
    sw $t0, 40($fp)
    j L10
L12:
    la $a0, str5
    li $v0, 4
    syscall
    lw $a0, 44($fp)
    li $v0, 1
    syscall
    li $a0, 10
    li $v0, 11
    syscall
    li $a0, 4
    jal factorial
    sw $v0, 192($fp)
    lw $t0, 192($fp)
    li $t1, 20
    sgt $t2, $t0, $t1
    sw $t2, 196($fp)
    lw $t0, 36($fp)
    li $t1, 0
    sne $t2, $t0, $t1
    sw $t2, 200($fp)
    lw $t0, 196($fp)
    lw $t1, 200($fp)
    and $t2, $t0, $t1
    sw $t2, 204($fp)
    lw $t0, 204($fp)
    sw $t0, 48($fp)
    la $a0, str6
    li $v0, 4
    syscall
    lw $a0, 48($fp)
    li $v0, 1
    syscall
    li $a0, 10
    li $v0, 11
    syscall
    li $t0, 90
    sw $t0, 52($fp)
    la $a0, str7
    li $v0, 4
    syscall
    lw $a0, 52($fp)
    li $v0, 11
    syscall
    li $a0, 10
    li $v0, 11
    syscall
    la $a0, str8
    li $v0, 4
    syscall
    li $a0, 10
    li $v0, 11
    syscall
    li $v0, 0
    j _epilogo_main
_epilogo_main:
    lw $ra, 212($sp)
    lw $fp, 208($sp)
    addu $sp, $sp, 216
    jr $ra
