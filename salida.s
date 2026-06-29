.data
x: .word 0
activo: .word 0
__buf_read_str: .space 256
str0: .asciiz "Suma: "
str1: .asciiz "\n"

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
    sw $t2, 12($fp)
    lw $t0, 12($fp)
    sw $t0, 8($fp)
    lw $v0, 8($fp)
    j _epilogo_suma
_epilogo_suma:
    lw $ra, 20($sp)
    lw $fp, 16($sp)
    addu $sp, $sp, 24
    jr $ra
factorial:
    subu $sp, $sp, 24
    sw $ra, 20($sp)
    sw $fp, 16($sp)
    move $fp, $sp
    sw $a0, 0($fp)
    li $t0, 1
    sw $t0, 4($fp)
    lw $t0, 0($fp)
    li $t1, 0
    seq $t2, $t0, $t1
    sw $t2, 8($fp)
    lw $t0, 8($fp)
    beq $t0, $zero, L1
    li $t0, 99
    sw $t0, 4($fp)
L1:
    lw $v0, 4($fp)
    j _epilogo_factorial
_epilogo_factorial:
    lw $ra, 20($sp)
    lw $fp, 16($sp)
    addu $sp, $sp, 24
    jr $ra
main:
    subu $sp, $sp, 40
    sw $ra, 36($sp)
    sw $fp, 32($sp)
    move $fp, $sp
    li $t0, 5
    sw $t0, 0($fp)
    lw $a0, 0($fp)
    lw $a1, 0($fp)
    jal suma
    sw $v0, 12($fp)
    lw $t0, 12($fp)
    sw $t0, 4($fp)
    la $a0, str0
    li $v0, 4
    syscall
    lw $a0, 4($fp)
    li $v0, 1
    syscall
    la $a0, str1
    li $v0, 4
    syscall
    li $t0, 0
    sw $t0, 8($fp)
L2:
    lw $t0, 8($fp)
    li $t1, 3
    slt $t2, $t0, $t1
    sw $t2, 16($fp)
    lw $t0, 16($fp)
    beq $t0, $zero, L3
    lw $t0, 8($fp)
    sw $t0, 0($fp)
    lw $a0, 0($fp)
    li $v0, 1
    syscall
    la $a0, str1
    li $v0, 4
    syscall
    lw $t0, 8($fp)
    li $t1, 1
    add $t2, $t0, $t1
    sw $t2, 20($fp)
    lw $t0, 20($fp)
    sw $t0, 8($fp)
    j L2
L3:
    li $a0, 5
    jal factorial
    sw $v0, 24($fp)
    lw $a0, 24($fp)
    li $v0, 1
    syscall
    la $a0, str1
    li $v0, 4
    syscall
    li $v0, 0
    j _epilogo_main
_epilogo_main:
    lw $ra, 36($sp)
    lw $fp, 32($sp)
    addu $sp, $sp, 40
    jr $ra
