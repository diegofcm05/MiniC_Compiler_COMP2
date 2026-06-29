package com.minic.ir;

/**
 * Catálogo de operaciones soportadas por una instrucción de código de
 * tres direcciones (TAC). Cada constante corresponde a una construcción
 * de Mini-C que el generador de IR necesita emitir.
 *
 * Convención de uso de los campos destino/op1/op2 de Instruccion según
 * el operador (ver Instruccion.java para más detalle):
 *
 *   ASIGNAR        destino = op1                 (op2 sin uso)
 *   BINARIA        destino = op1 OP op2           (OP guardado aparte, en 'simbolo')
 *   UNARIA         destino = OP op1               (op2 sin uso)
 *   GOTO           goto destino                    (op1/op2 sin uso, destino = etiqueta)
 *   IF_TRUE        if op1 goto destino             (salta si op1 es verdadero)
 *   IF_FALSE       ifFalse op1 goto destino         (salta si op1 es falso)
 *   ETIQUETA       destino:                          (marca una posición en el código)
 *   PARAM          param op1                         (antes de cada CALL)
 *   CALL           destino = call op1, op2          (op1 = nombre función, op2 = nro args)
 *   CALL_VOID      call op1, op2                     (igual, sin guardar resultado)
 *   RETURN         return op1                         (op1 puede ser null)
 *   ARR_LOAD       destino = op1[op2]                 (leer de arreglo; op1 = arreglo, op2 = índice)
 *   ARR_STORE      destino[op1] = op2                 (escribir en arreglo; destino = arreglo, op1 = índice, op2 = valor)
 *   FUNC_INICIO    FUNCTION destino:                   (encabezado de función)
 *   FUNC_FIN       END FUNCTION destino                 (cierre de función)
 *   ADDR           destino = &op1                     (dirección de una variable;
 *                  destino = &op1[op2]                  o de un elemento de arreglo si op2 != null;
 *                                                        op1 = nombre, op2 = índice ya linealizado o null)
 *   PTR_LOAD       destino = *op1                       (leer el valor apuntado por op1)
 *   PTR_STORE      *destino = op1                       (escribir op1 en la dirección apuntada por destino)
 */
public enum OpTAC {
    ASIGNAR,
    BINARIA,
    UNARIA,
    GOTO,
    IF_TRUE,
    IF_FALSE,
    ETIQUETA,
    PARAM,
    CALL,
    CALL_VOID,
    RETURN,
    ARR_LOAD,
    ARR_STORE,
    FUNC_INICIO,
    FUNC_FIN,
    ADDR,
    PTR_LOAD,
    PTR_STORE
}