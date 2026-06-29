grammar MiniC;

program
    : (declaration | funcDef)* EOF
    ;

declaration
    : typeSpecifier declaratorList ';'
    ;

declaratorList
    : declarator (',' declarator)*
    ;

declarator
    : IDENTIFIER ('[' INTEGER_CONST ']')* ('=' expr)?
    | IDENTIFIER '[' INTEGER_CONST ']' '[' INTEGER_CONST ']'
    | '*' declarator
    ;

typeSpecifier
    : 'int'
    | 'char'
    | 'bool'
    | 'void'
    | 'string'
    ;

funcDef
    : typeSpecifier IDENTIFIER '(' params? ')' compoundStmt
    ;

params
    : param (',' param)*
    ;

param
    : typeSpecifier declarator
    | typeSpecifier IDENTIFIER '[' ']' '[' INTEGER_CONST ']'
    ;

compoundStmt
    : '{' (declaration | statement)* '}'
    ;

statement
    : compoundStmt
    | ifStmt
    | whileStmt
    | forStmt
    | doWhileStmt
    | returnStmt
    | exprStmt
    | breakStmt
    | continueStmt
    ;

ifStmt
    : 'if' '(' expr ')' statement ('else' statement)?
    ;

whileStmt
    : 'while' '(' expr ')' statement
    ;

forStmt
    : 'for' '(' exprStmt expr? ';' expr? ')' statement
    ;

doWhileStmt
    : 'do' statement 'while' '(' expr ')' ';'
    ;

returnStmt
    : 'return' expr? ';'
    ;

exprStmt
    : expr? ';'
    ;

breakStmt
    : 'break' ';'
    ;

continueStmt
    : 'continue' ';'
    ;

expr
    : assignmentExpr
    ;

assignmentExpr
    : lvalue '=' assignmentExpr
    | logicalOrExpr
    ;


logicalOrExpr
    : logicalAndExpr ('||' logicalAndExpr)*
    ;



logicalAndExpr
    : equalityExpr ('&&' equalityExpr)*
    ;

equalityExpr
    : relationalExpr (('==' | '!=') relationalExpr)*
    ;

relationalExpr
    : additiveExpr (('<' | '>' | '<=' | '>=') additiveExpr)*
    ;

additiveExpr
    : multiplicativeExpr (('+' | '-') multiplicativeExpr)*
    ;

multiplicativeExpr
    : unaryExpr (('*' | '/' | '%') unaryExpr)*
    ;

unaryExpr
    : ('!' | '-' | '*' | '&') unaryExpr
    | primary
    ;

primary
    : INTEGER_CONST
    | CHAR_CONST
    | STRING_LITERAL
    | 'true'
    | 'false'
    | '(' expr ')'
    | lvalue
    | call
    ;

call
    : IDENTIFIER '(' (expr (',' expr)*)? ')'
    ;

lvalue
    : IDENTIFIER ('[' expr ']')*
    | IDENTIFIER '[' expr ']' '[' expr ']'
    | '*' IDENTIFIER
    ;

// ─── TOKENS ──────────────────────────────────────────────────────────────────
IDENTIFIER      : [A-Za-z_][A-Za-z0-9_]* ;
INTEGER_CONST   : [0-9]+ ;
CHAR_CONST     : '\'' ( ~['\\\n] | '\\' . ) '\'' ;
STRING_LITERAL : '"'  ( ~["\\\n] | '\\' . )* '"' ;

WS              : [ \t\r\n]+ -> skip ;
LINE_COMMENT    : '//' ~[\r\n]* -> skip ;
BLOCK_COMMENT   : '/*' .*? '*/' -> skip ;