.data
str0: .asciiz "suma pequena\n"

.text
main:
    subu $sp, $sp, 48
    sw $ra, 44($sp)
    sw $fp, 40($sp)
    move $fp, $sp
    li $t0, 5
    sw $t0, 0($fp)
    li $t0, 10
    sw $t0, 4($fp)
    lw $t0, 0($fp)
    lw $t1, 4($fp)
    add $t2, $t0, $t1
    sw $t2, 16($fp)
    lw $t0, 16($fp)
    sw $t0, 8($fp)
    lw $t0, 8($fp)
    li $t1, 10
    sgt $t2, $t0, $t1
    sw $t2, 20($fp)
    lw $t0, 20($fp)
    bne $t0, $zero, L1
    j L2
L1:
    lw $a0, 8($fp)
    li $v0, 1
    syscall
    j L3
L2:
    la $a0, str0
    li $v0, 4
    syscall
L3:
    li $t0, 0
    sw $t0, 12($fp)
L4:
    lw $t0, 12($fp)
    li $t1, 3
    slt $t2, $t0, $t1
    sw $t2, 24($fp)
    lw $t0, 24($fp)
    beq $t0, $zero, L5
    lw $t0, 12($fp)
    li $t1, 1
    seq $t2, $t0, $t1
    sw $t2, 28($fp)
    lw $t0, 28($fp)
    beq $t0, $zero, L6
    lw $t0, 12($fp)
    li $t1, 1
    add $t2, $t0, $t1
    sw $t2, 32($fp)
    lw $t0, 32($fp)
    sw $t0, 12($fp)
    j L4
L6:
    lw $a0, 12($fp)
    li $v0, 1
    syscall
    lw $t0, 12($fp)
    li $t1, 1
    add $t2, $t0, $t1
    sw $t2, 36($fp)
    lw $t0, 36($fp)
    sw $t0, 12($fp)
    j L4
L5:
    li $v0, 0
    j _epilogo_main
_epilogo_main:
    lw $ra, 44($sp)
    lw $fp, 40($sp)
    addu $sp, $sp, 48
    jr $ra
