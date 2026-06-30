.data

.text
suma:
    subu $sp, $sp, 24
    sw $ra, 20($sp)
    sw $fp, 16($sp)
    move $fp, $sp
    sw $a0, 0($fp)
    sw $a1, 4($fp)
    lw $t0, 0($fp)
    lw $t1, 4($fp)
    add $t2, $t0, $t1
    sw $t2, 8($fp)
    lw $v0, 8($fp)
    j _epilogo_suma
_epilogo_suma:
    lw $ra, 20($sp)
    lw $fp, 16($sp)
    addu $sp, $sp, 24
    jr $ra
sumar6:
    subu $sp, $sp, 56
    sw $ra, 52($sp)
    sw $fp, 48($sp)
    move $fp, $sp
    sw $a0, 0($fp)
    sw $a1, 4($fp)
    sw $a2, 8($fp)
    sw $a3, 12($fp)
    lw $t9, 56($fp)
    sw $t9, 16($fp)
    lw $t9, 60($fp)
    sw $t9, 20($fp)
    lw $t0, 0($fp)
    lw $t1, 4($fp)
    add $t2, $t0, $t1
    sw $t2, 24($fp)
    lw $t0, 24($fp)
    lw $t1, 8($fp)
    add $t2, $t0, $t1
    sw $t2, 28($fp)
    lw $t0, 28($fp)
    lw $t1, 12($fp)
    add $t2, $t0, $t1
    sw $t2, 32($fp)
    lw $t0, 32($fp)
    lw $t1, 16($fp)
    add $t2, $t0, $t1
    sw $t2, 36($fp)
    lw $t0, 36($fp)
    lw $t1, 20($fp)
    add $t2, $t0, $t1
    sw $t2, 40($fp)
    lw $v0, 40($fp)
    j _epilogo_sumar6
_epilogo_sumar6:
    lw $ra, 52($sp)
    lw $fp, 48($sp)
    addu $sp, $sp, 56
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
    beq $t0, $zero, L1
    li $v0, 1
    j _epilogo_factorial
L1:
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
    subu $sp, $sp, 24
    sw $ra, 20($sp)
    sw $fp, 16($sp)
    move $fp, $sp
    li $a0, 3
    li $a1, 4
    jal suma
    sw $v0, 0($fp)
    lw $a0, 0($fp)
    li $v0, 1
    syscall
    li $a0, 10
    li $v0, 11
    syscall
    subu $sp, $sp, 8
    li $t9, 5
    sw $t9, 0($sp)
    li $t9, 6
    sw $t9, 4($sp)
    li $a0, 1
    li $a1, 2
    li $a2, 3
    li $a3, 4
    jal sumar6
    addu $sp, $sp, 8
    sw $v0, 4($fp)
    lw $a0, 4($fp)
    li $v0, 1
    syscall
    li $a0, 10
    li $v0, 11
    syscall
    li $a0, 5
    jal factorial
    sw $v0, 8($fp)
    lw $a0, 8($fp)
    li $v0, 1
    syscall
    li $a0, 10
    li $v0, 11
    syscall
    li $v0, 0
    j _epilogo_main
_epilogo_main:
    lw $ra, 20($sp)
    lw $fp, 16($sp)
    addu $sp, $sp, 24
    jr $ra
