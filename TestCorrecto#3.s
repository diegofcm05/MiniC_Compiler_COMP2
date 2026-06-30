.data

.text
intercambiar:
    subu $sp, $sp, 32
    sw $ra, 28($sp)
    sw $fp, 24($sp)
    move $fp, $sp
    sw $a0, 0($fp)
    sw $a1, 4($fp)
    lw $t0, 0($fp)
    lw $t1, 0($t0)
    sw $t1, 12($fp)
    lw $t0, 12($fp)
    sw $t0, 8($fp)
    lw $t0, 4($fp)
    lw $t1, 0($t0)
    sw $t1, 16($fp)
    lw $t0, 0($fp)
    lw $t1, 16($fp)
    sw $t1, 0($t0)
    lw $t0, 4($fp)
    lw $t1, 8($fp)
    sw $t1, 0($t0)
_epilogo_intercambiar:
    lw $ra, 28($sp)
    lw $fp, 24($sp)
    addu $sp, $sp, 32
    jr $ra
incrementar:
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
_epilogo_incrementar:
    lw $ra, 20($sp)
    lw $fp, 16($sp)
    addu $sp, $sp, 24
    jr $ra
main:
    subu $sp, $sp, 56
    sw $ra, 52($sp)
    sw $fp, 48($sp)
    move $fp, $sp
    li $t0, 10
    sw $t0, 0($fp)
    li $t0, 20
    sw $t0, 4($fp)
    addu $t0, $fp, 0
    sw $t0, 24($fp)
    addu $t0, $fp, 4
    sw $t0, 28($fp)
    lw $a0, 24($fp)
    lw $a1, 28($fp)
    jal intercambiar
    lw $a0, 0($fp)
    li $v0, 1
    syscall
    li $a0, 10
    li $v0, 11
    syscall
    lw $a0, 4($fp)
    li $v0, 1
    syscall
    li $a0, 10
    li $v0, 11
    syscall
    addu $t0, $fp, 8
    li $t1, 0
    sll $t1, $t1, 2
    addu $t0, $t0, $t1
    li $t2, 5
    sw $t2, 0($t0)
    addu $t0, $fp, 8
    li $t1, 0
    sll $t1, $t1, 2
    addu $t0, $t0, $t1
    sw $t0, 32($fp)
    lw $a0, 32($fp)
    jal incrementar
    addu $t0, $fp, 8
    li $t1, 0
    sll $t1, $t1, 2
    addu $t0, $t0, $t1
    lw $t2, 0($t0)
    sw $t2, 36($fp)
    lw $a0, 36($fp)
    li $v0, 1
    syscall
    li $a0, 10
    li $v0, 11
    syscall
    addu $t0, $fp, 0
    sw $t0, 40($fp)
    lw $t0, 40($fp)
    sw $t0, 20($fp)
    lw $t0, 20($fp)
    li $t1, 99
    sw $t1, 0($t0)
    lw $a0, 0($fp)
    li $v0, 1
    syscall
    li $a0, 10
    li $v0, 11
    syscall
    li $v0, 0
    j _epilogo_main
_epilogo_main:
    lw $ra, 52($sp)
    lw $fp, 48($sp)
    addu $sp, $sp, 56
    jr $ra
