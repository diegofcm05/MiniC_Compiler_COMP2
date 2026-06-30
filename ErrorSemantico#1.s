.data
str0: .asciiz "Hello"
str1: .asciiz "Promedio: "
str2: .asciiz "\n"

.text
main:
    subu $sp, $sp, 40
    sw $ra, 36($sp)
    sw $fp, 32($sp)
    move $fp, $sp
    li $t0, 85
    sw $t0, 0($fp)
    li $t0, 90
    sw $t0, 4($fp)
    li $t0, 78
    sw $t0, 8($fp)
    lw $t0, 0($fp)
    lw $t1, 4($fp)
    add $t2, $t0, $t1
    sw $t2, 20($fp)
    lw $t0, 20($fp)
    lw $t1, 8($fp)
    add $t2, $t0, $t1
    sw $t2, 24($fp)
    lw $t0, 24($fp)
    li $t1, 3
    div $t2, $t0, $t1
    sw $t2, 28($fp)
    lw $t0, 28($fp)
    sw $t0, 12($fp)
    la $t0, str0
    sw $t0, 16($fp)
    la $a0, str1
    li $v0, 4
    syscall
    lw $a0, 12($fp)
    li $v0, 1
    syscall
    la $a0, str2
    li $v0, 4
    syscall
    lw $a0, 16($fp)
    li $v0, 4
    syscall
    li $v0, 0
    j _epilogo_main
_epilogo_main:
    lw $ra, 36($sp)
    lw $fp, 32($sp)
    addu $sp, $sp, 40
    jr $ra
