.data

.text
main:
    subu $sp, $sp, 48
    sw $ra, 44($sp)
    sw $fp, 40($sp)
    move $fp, $sp
    li $v0, 0
    j _epilogo_main
_epilogo_main:
    lw $ra, 44($sp)
    lw $fp, 40($sp)
    addu $sp, $sp, 48
    jr $ra
