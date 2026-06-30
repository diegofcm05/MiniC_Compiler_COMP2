.data

.text
main:
    subu $sp, $sp, 112
    sw $ra, 108($sp)
    sw $fp, 104($sp)
    move $fp, $sp
    addu $t0, $fp, 0
    li $t1, 0
    sll $t1, $t1, 2
    addu $t0, $t0, $t1
    li $t2, 10
    sw $t2, 0($t0)
    addu $t0, $fp, 0
    li $t1, 0
    sll $t1, $t1, 2
    addu $t0, $t0, $t1
    lw $t2, 0($t0)
    sw $t2, 52($fp)
    lw $t0, 52($fp)
    li $t1, 5
    add $t2, $t0, $t1
    sw $t2, 56($fp)
    addu $t0, $fp, 0
    li $t1, 1
    sll $t1, $t1, 2
    addu $t0, $t0, $t1
    lw $t2, 56($fp)
    sw $t2, 0($t0)
    li $t0, 0
    li $t1, 3
    mul $t2, $t0, $t1
    sw $t2, 60($fp)
    lw $t0, 60($fp)
    li $t1, 0
    add $t2, $t0, $t1
    sw $t2, 64($fp)
    addu $t0, $fp, 20
    lw $t1, 64($fp)
    sll $t1, $t1, 2
    addu $t0, $t0, $t1
    li $t2, 1
    sw $t2, 0($t0)
    li $t0, 1
    li $t1, 3
    mul $t2, $t0, $t1
    sw $t2, 68($fp)
    lw $t0, 68($fp)
    li $t1, 2
    add $t2, $t0, $t1
    sw $t2, 72($fp)
    addu $t0, $fp, 20
    lw $t1, 72($fp)
    sll $t1, $t1, 2
    addu $t0, $t0, $t1
    li $t2, 9
    sw $t2, 0($t0)
    li $t0, 1
    li $t1, 3
    mul $t2, $t0, $t1
    sw $t2, 76($fp)
    lw $t0, 76($fp)
    li $t1, 2
    add $t2, $t0, $t1
    sw $t2, 80($fp)
    addu $t0, $fp, 20
    lw $t1, 80($fp)
    sll $t1, $t1, 2
    addu $t0, $t0, $t1
    lw $t2, 0($t0)
    sw $t2, 84($fp)
    lw $t0, 84($fp)
    sw $t0, 44($fp)
    lw $a0, 44($fp)
    li $v0, 1
    syscall
    li $t0, 0
    sw $t0, 48($fp)
L1:
    lw $t0, 48($fp)
    li $t1, 5
    slt $t2, $t0, $t1
    sw $t2, 88($fp)
    lw $t0, 88($fp)
    beq $t0, $zero, L3
    lw $t0, 48($fp)
    li $t1, 3
    seq $t2, $t0, $t1
    sw $t2, 92($fp)
    lw $t0, 92($fp)
    beq $t0, $zero, L4
    j L3
L4:
    addu $t0, $fp, 0
    lw $t1, 48($fp)
    sll $t1, $t1, 2
    addu $t0, $t0, $t1
    lw $t2, 0($t0)
    sw $t2, 96($fp)
    lw $a0, 96($fp)
    li $v0, 1
    syscall
L2:
    lw $t0, 48($fp)
    li $t1, 1
    add $t2, $t0, $t1
    sw $t2, 100($fp)
    lw $t0, 100($fp)
    sw $t0, 48($fp)
    j L1
L3:
    li $v0, 0
    j _epilogo_main
_epilogo_main:
    lw $ra, 108($sp)
    lw $fp, 104($sp)
    addu $sp, $sp, 112
    jr $ra
