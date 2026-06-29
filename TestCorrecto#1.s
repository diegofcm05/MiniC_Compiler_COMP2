.data
m: .space 264
a: .space 204
str0: .asciiz "a["
str1: .asciiz "] = "
str2: .asciiz "Gracias por usar Mini-C!\n"

.text
fill:
    subu $sp, $sp, 72
    sw $ra, 68($sp)
    sw $fp, 64($sp)
    move $fp, $sp
    sw $a0, 0($fp)
    sw $a1, 4($fp)
    li $t0, 1
    sw $t0, 16($fp)
    lw $t0, 0($fp)
    sw $t0, 8($fp)
L1:
    lw $t0, 8($fp)
    li $t1, 1
    sge $t2, $t0, $t1
    sw $t2, 20($fp)
    lw $t0, 20($fp)
    beq $t0, $zero, L3
    lw $t0, 4($fp)
    sw $t0, 12($fp)
L4:
    lw $t0, 12($fp)
    li $t1, 1
    sge $t2, $t0, $t1
    sw $t2, 24($fp)
    lw $t0, 24($fp)
    beq $t0, $zero, L6
    lw $t0, 0($fp)
    lw $t1, 4($fp)
    sub $t2, $t0, $t1
    sw $t2, 28($fp)
    lw $t0, 16($fp)
    lw $t1, 28($fp)
    add $t2, $t0, $t1
    sw $t2, 32($fp)
    lw $t0, 32($fp)
    li $t1, 5
    add $t2, $t0, $t1
    sw $t2, 36($fp)
    lw $t0, 36($fp)
    li $t1, 15
    div $t0, $t1
    mfhi $t2
    sw $t2, 40($fp)
    lw $t0, 8($fp)
    li $t1, 6
    mul $t2, $t0, $t1
    sw $t2, 44($fp)
    lw $t0, 44($fp)
    lw $t1, 12($fp)
    add $t2, $t0, $t1
    sw $t2, 48($fp)
    la $t0, m
    lw $t1, 48($fp)
    sll $t1, $t1, 2
    addu $t0, $t0, $t1
    lw $t2, 40($fp)
    sw $t2, 0($t0)
    lw $t0, 16($fp)
    li $t1, 1
    add $t2, $t0, $t1
    sw $t2, 52($fp)
    lw $t0, 52($fp)
    sw $t0, 16($fp)
L5:
    lw $t0, 12($fp)
    li $t1, 1
    sub $t2, $t0, $t1
    sw $t2, 56($fp)
    lw $t0, 56($fp)
    sw $t0, 12($fp)
    j L4
L6:
L2:
    lw $t0, 8($fp)
    li $t1, 1
    sub $t2, $t0, $t1
    sw $t2, 60($fp)
    lw $t0, 60($fp)
    sw $t0, 8($fp)
    j L1
L3:
_epilogo_fill:
    lw $ra, 68($sp)
    lw $fp, 64($sp)
    addu $sp, $sp, 72
    jr $ra
main:
    subu $sp, $sp, 80
    sw $ra, 76($sp)
    sw $fp, 72($sp)
    move $fp, $sp
    li $t0, 10
    sw $t0, 8($fp)
    li $t0, 5
    sw $t0, 12($fp)
    li $t0, 1
    sw $t0, 16($fp)
    li $t0, 50
    sw $t0, 20($fp)
    lw $a0, 8($fp)
    lw $a1, 12($fp)
    jal fill
    li $t0, 1
    sw $t0, 16($fp)
    li $t0, 1
    sw $t0, 0($fp)
L7:
    lw $t0, 0($fp)
    lw $t1, 8($fp)
    sle $t2, $t0, $t1
    sw $t2, 24($fp)
    lw $t0, 24($fp)
    beq $t0, $zero, L9
    li $t0, 1
    sw $t0, 4($fp)
L10:
    lw $t0, 4($fp)
    lw $t1, 12($fp)
    sle $t2, $t0, $t1
    sw $t2, 28($fp)
    lw $t0, 28($fp)
    beq $t0, $zero, L12
    lw $t0, 0($fp)
    li $t1, 6
    mul $t2, $t0, $t1
    sw $t2, 32($fp)
    lw $t0, 32($fp)
    lw $t1, 4($fp)
    add $t2, $t0, $t1
    sw $t2, 36($fp)
    la $t0, m
    lw $t1, 36($fp)
    sll $t1, $t1, 2
    addu $t0, $t0, $t1
    lw $t2, 0($t0)
    sw $t2, 40($fp)
    la $t0, a
    lw $t1, 16($fp)
    sll $t1, $t1, 2
    addu $t0, $t0, $t1
    lw $t2, 40($fp)
    sw $t2, 0($t0)
    lw $t0, 16($fp)
    li $t1, 1
    add $t2, $t0, $t1
    sw $t2, 44($fp)
    lw $t0, 44($fp)
    sw $t0, 16($fp)
L11:
    lw $t0, 4($fp)
    li $t1, 1
    add $t2, $t0, $t1
    sw $t2, 48($fp)
    lw $t0, 48($fp)
    sw $t0, 4($fp)
    j L10
L12:
L8:
    lw $t0, 0($fp)
    li $t1, 1
    add $t2, $t0, $t1
    sw $t2, 52($fp)
    lw $t0, 52($fp)
    sw $t0, 0($fp)
    j L7
L9:
    li $t0, 1
    sw $t0, 16($fp)
L13:
    lw $t0, 20($fp)
    li $t1, 1
    add $t2, $t0, $t1
    sw $t2, 56($fp)
    lw $t0, 16($fp)
    lw $t1, 56($fp)
    sne $t2, $t0, $t1
    sw $t2, 60($fp)
    lw $t0, 60($fp)
    beq $t0, $zero, L14
    la $a0, str0
    li $v0, 4
    syscall
    lw $a0, 16($fp)
    li $v0, 1
    syscall
    la $a0, str1
    li $v0, 4
    syscall
    la $t0, a
    lw $t1, 16($fp)
    sll $t1, $t1, 2
    addu $t0, $t0, $t1
    lw $t2, 0($t0)
    sw $t2, 64($fp)
    lw $a0, 64($fp)
    li $v0, 1
    syscall
    li $a0, 10
    li $v0, 11
    syscall
    lw $t0, 16($fp)
    li $t1, 1
    add $t2, $t0, $t1
    sw $t2, 68($fp)
    lw $t0, 68($fp)
    sw $t0, 16($fp)
    j L13
L14:
    la $a0, str2
    li $v0, 4
    syscall
    li $v0, 0
    j _epilogo_main
_epilogo_main:
    lw $ra, 76($sp)
    lw $fp, 72($sp)
    addu $sp, $sp, 80
    jr $ra
