.data

.text
main:
    subu $sp, $sp, 48
    sw $ra, 44($sp)
    sw $fp, 40($sp)
    move $fp, $sp
    li $t0, 5
    sw $t0, 0($fp)
    li $t0, 7
    sw $t0, 20($fp)
    lw $t0, 20($fp)
    sw $t0, 4($fp)
    li $t0, 20
    sw $t0, 24($fp)
    lw $t0, 24($fp)
    sw $t0, 8($fp)
    lw $t0, 0($fp)
    li $t1, 1
    add $t2, $t0, $t1
    sw $t2, 28($fp)
    lw $t0, 28($fp)
    sw $t0, 12($fp)
    li $t0, 1
    sw $t0, 32($fp)
    lw $t0, 32($fp)
    sw $t0, 16($fp)
    lw $a0, 4($fp)
    li $v0, 1
    syscall
    li $a0, 10
    li $v0, 11
    syscall
    lw $a0, 8($fp)
    li $v0, 1
    syscall
    li $a0, 10
    li $v0, 11
    syscall
    lw $a0, 12($fp)
    li $v0, 1
    syscall
    li $a0, 10
    li $v0, 11
    syscall
    lw $a0, 16($fp)
    li $v0, 1
    syscall
    li $a0, 10
    li $v0, 11
    syscall
    li $v0, 0
    j _epilogo_main
_epilogo_main:
    lw $ra, 44($sp)
    lw $fp, 40($sp)
    addu $sp, $sp, 48
    jr $ra
