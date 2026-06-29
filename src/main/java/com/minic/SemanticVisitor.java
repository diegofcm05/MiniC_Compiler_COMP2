package com.minic;

import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SemanticVisitor extends MiniCBaseVisitor<String> {

    private SymbolTable tabla   = new SymbolTable();
    private int         errores = 0;

    private String tipoRetornoActual = null;
    private int nivelLoop = 0;
    private int indiceParametroActual = 0;

    public SemanticVisitor() {
        tabla.entrar("global");

        registrarRuntime("print_int",  "void", "int");
        registrarRuntime("print_char", "void", "char");
        registrarRuntime("print_str",  "void", "string");
        registrarRuntime("print_bool", "void", "bool");
        registrarRuntime("println",    "void");
        registrarRuntime("read_int",   "int");
        registrarRuntime("read_char",  "char");
        // void read_str(char* buf, int maxlen) — tal como lo especifica el
        // enunciado (§3): el llamador da su propio buffer (un arreglo de
        // char) y un tamaño máximo; read_str lo llena, no devuelve nada.
        // "char[]" (dimensión abierta) reutiliza el mismo chequeo de forma
        // de arreglos que ya usamos para parámetros tipo "int m[][3]" —
        // acepta un arreglo de char de cualquier tamaño.
        registrarRuntime("read_str",   "void", "char[]", "int");
    }

    private void registrarRuntime(String nombre, String tipoRetorno, String... tiposParams) {
        tabla.agregar(new Symbol(nombre, tipoRetorno, "funcion", 0, Arrays.asList(tiposParams)));
    }

    private void error(Token tok, String msg) {
        System.err.printf("[ERROR SEMÁNTICO] línea %d, col %d: %s%n",
                tok.getLine(), tok.getCharPositionInLine() + 1, msg);
        errores++;
    }

    // ─── HELPERS DE COMPATIBILIDAD DE TIPOS (promociones char↔int) ───────────

    private boolean esNumerico(String tipo) {
        return "int".equals(tipo) || "char".equals(tipo);
    }

    private boolean compatiblePromocion(String a, String b) {
        if (a == null || b == null) return true;
        if (esNumerico(a) && esNumerico(b)) return true;
        return a.equals(b);
    }

    private boolean esCondicionValida(String tipo) {
        return "bool".equals(tipo) || "int".equals(tipo);
    }

    private boolean esPuntero(String tipo) {
        return tipo != null && tipo.endsWith("*");
    }

    /** Quita el sufijo '*' — "int*" → "int". Solo llamar si esPuntero(tipo) es true. */
    private String tipoBase(String tipo) {
        return tipo.substring(0, tipo.length() - 1);
    }

    /** Construye el sufijo de forma de un arreglo, ej. tamanios=[2,3] → "[2][3]".
     *  Una dimensión con tamaño -1 (o tamanios == null) se codifica como "[]"
     *  (abierta / desconocida — no se exige tamaño exacto al pasar como argumento). */
    private String formaArreglo(int[] tamanios, int dimensiones) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dimensiones; i++) {
            if (tamanios != null && i < tamanios.length && tamanios[i] >= 0) {
                sb.append("[").append(tamanios[i]).append("]");
            } else {
                sb.append("[]");
            }
        }
        return sb.toString();
    }

    /** Extrae cada dimensión de un tipo-arreglo como texto, ej.
     *  "int[2][3]" → ["2", "3"]; "int[][3]" → ["", "3"]. */
    private List<String> extraerDimensiones(String tipoArreglo) {
        List<String> dims = new ArrayList<>();
        int i = tipoArreglo.indexOf('[');
        while (i != -1) {
            int fin = tipoArreglo.indexOf(']', i);
            dims.add(tipoArreglo.substring(i + 1, fin));
            i = tipoArreglo.indexOf('[', fin + 1);
        }
        return dims;
    }

    /** Como compatiblePromocion, pero con manejo especial para arreglos:
     *  una dimensión que el parámetro deja abierta (ej. "int[][3]") no se
     *  exige exacta en el argumento recibido — solo el tipo base y el
     *  resto de las dimensiones declaradas. */
    private boolean tipoArgumentoValido(String esperado, String recibido) {
        if (esperado == null || recibido == null) return true;
        // "string" es, por especificación, un arreglo de char bajo el
        // capó (§2.2: "string como arreglo de char... manejado a nivel de
        // compilador como puntero a char") — un parámetro 'string' debe
        // aceptar un arreglo de char decaído (ej. print_str(buf) después
        // de read_str(buf, 100)), no solo un literal.
        if (esperado.equals("string") && recibido.startsWith("char[")) return true;
        if (!esperado.contains("[")) {
            if (recibido.contains("[")) return false; // se esperaba escalar/puntero, llegó un arreglo
            return compatiblePromocion(esperado, recibido);
        }
        if (!recibido.contains("[")) return false; // se esperaba un arreglo, llegó otra cosa

        String baseEsp = esperado.substring(0, esperado.indexOf('['));
        String baseRec = recibido.substring(0, recibido.indexOf('['));
        if (!baseEsp.equals(baseRec)) return false;

        List<String> dimsEsp = extraerDimensiones(esperado);
        List<String> dimsRec = extraerDimensiones(recibido);
        if (dimsEsp.size() != dimsRec.size()) return false;

        for (int i = 0; i < dimsEsp.size(); i++) {
            String de = dimsEsp.get(i);
            if (de.isEmpty()) continue; // dimensión abierta en el parámetro — no se exige
            if (!de.equals(dimsRec.get(i))) return false;
        }
        return true;
    }


    private Integer evaluarConstante(MiniCParser.ExprContext ctx) {
        return evaluarConstante(ctx.assignmentExpr());
    }

    private Integer evaluarConstante(MiniCParser.AssignmentExprContext ctx) {
        if (ctx.lvalue() != null) return null; // una asignación no es una constante
        return evaluarConstante(ctx.logicalOrExpr());
    }

    private Integer evaluarConstante(MiniCParser.LogicalOrExprContext ctx) {
        if (ctx.logicalAndExpr().size() != 1) return null; // hay || real, no es aritmética pura
        return evaluarConstante(ctx.logicalAndExpr(0));
    }

    private Integer evaluarConstante(MiniCParser.LogicalAndExprContext ctx) {
        if (ctx.equalityExpr().size() != 1) return null;
        return evaluarConstante(ctx.equalityExpr(0));
    }

    private Integer evaluarConstante(MiniCParser.EqualityExprContext ctx) {
        if (ctx.relationalExpr().size() != 1) return null;
        return evaluarConstante(ctx.relationalExpr(0));
    }

    private Integer evaluarConstante(MiniCParser.RelationalExprContext ctx) {
        if (ctx.additiveExpr().size() != 1) return null;
        return evaluarConstante(ctx.additiveExpr(0));
    }

    private Integer evaluarConstante(MiniCParser.AdditiveExprContext ctx) {
        List<MiniCParser.MultiplicativeExprContext> operandos = ctx.multiplicativeExpr();
        Integer resultado = evaluarConstante(operandos.get(0));
        if (resultado == null) return null;

        int indiceOperador = 1;
        for (int i = 1; i < operandos.size(); i++) {
            Integer siguiente = evaluarConstante(operandos.get(i));
            if (siguiente == null) return null;

            String op = ctx.getChild(indiceOperador).getText();
            if (op.equals("+")) resultado = resultado + siguiente;
            else if (op.equals("-")) resultado = resultado - siguiente;
            else return null;

            indiceOperador += 2;
        }
        return resultado;
    }

    private Integer evaluarConstante(MiniCParser.MultiplicativeExprContext ctx) {
        List<MiniCParser.UnaryExprContext> operandos = ctx.unaryExpr();
        Integer resultado = evaluarConstante(operandos.get(0));
        if (resultado == null) return null;

        int indiceOperador = 1;
        for (int i = 1; i < operandos.size(); i++) {
            Integer siguiente = evaluarConstante(operandos.get(i));
            if (siguiente == null) return null;

            String op = ctx.getChild(indiceOperador).getText();
            if (op.equals("*")) resultado = resultado * siguiente;
            else if (op.equals("/")) {
                if (siguiente == 0) return null; // evitar división por cero en compilación
                resultado = resultado / siguiente;
            } else if (op.equals("%")) {
                if (siguiente == 0) return null;
                resultado = resultado % siguiente;
            } else return null;

            indiceOperador += 2;
        }
        return resultado;
    }

    private Integer evaluarConstante(MiniCParser.UnaryExprContext ctx) {
        if (ctx.primary() != null) return evaluarConstante(ctx.primary());

        String op = ctx.getChild(0).getText();
        if (!op.equals("-")) return null; // '!', '*', '&' no producen una constante entera aquí

        Integer val = evaluarConstante(ctx.unaryExpr());
        return (val == null) ? null : -val;
    }

    private Integer evaluarConstante(MiniCParser.PrimaryContext ctx) {
        if (ctx.INTEGER_CONST() != null) {
            return Integer.parseInt(ctx.INTEGER_CONST().getText());
        }
        if (ctx.expr() != null) {
            return evaluarConstante(ctx.expr()); // paréntesis: '(' expr ')'
        }
        return null; // CHAR_CONST, STRING_LITERAL, true/false, lvalue, call: no es constante entera
    }

    @Override
    public String visitDeclaration(MiniCParser.DeclarationContext ctx) {
        String tipoBase = ctx.typeSpecifier().getText();

        for (MiniCParser.DeclaratorContext decl : ctx.declaratorList().declarator()) {
            String nombre;
            String categoria;
            int dimensiones = 0;
            int[] tamanios = null;
            Token tok;
            // Dónde vive el '= expr' de la inicialización — para arreglos y
            // variables normales está directo en 'decl', pero para punteros
            // ('*' declarator) la gramática anida el '=' en el declarator
            // INTERNO (ej. en "int* p = &x;", el '=' pertenece al declarator
            // de "p", no al de "* p").
            MiniCParser.ExprContext inicializador;

            if (decl.getChild(0).getText().equals("*")) {
                MiniCParser.DeclaratorContext interno = decl.declarator();

                if (interno.getChild(0).getText().equals("*")) {
                    error(ctx.getStart(), "Mini-C solo soporta punteros de un nivel "
                            + "('T*'); no se soportan punteros a punteros ('T**')");
                    continue;
                }
                if (interno.INTEGER_CONST() != null && !interno.INTEGER_CONST().isEmpty()) {
                    error(ctx.getStart(), "no se soportan arreglos de punteros en Mini-C");
                    continue;
                }

                nombre        = interno.IDENTIFIER().getText();
                categoria     = "puntero";
                inicializador = interno.expr();
                tok           = interno.IDENTIFIER().getSymbol();
            } else if (decl.INTEGER_CONST() != null && !decl.INTEGER_CONST().isEmpty()) {
                nombre    = decl.IDENTIFIER().getText();
                categoria = "arreglo";
                dimensiones = decl.INTEGER_CONST().size();
                tamanios = new int[dimensiones];
                for (int i = 0; i < dimensiones; i++) {
                    tamanios[i] = Integer.parseInt(decl.INTEGER_CONST(i).getText());
                }
                inicializador = decl.expr();
                tok = decl.IDENTIFIER().getSymbol();
            } else {
                nombre        = decl.IDENTIFIER().getText();
                categoria     = "variable";
                inicializador = decl.expr();
                tok           = decl.IDENTIFIER().getSymbol();
            }

            // Tipo final tal como se usará en chequeos y se imprimirá en la
            // tabla de símbolos: para punteros incluye el sufijo '*' (ej.
            // "int*"), de modo que el resto del visitor pueda reutilizar el
            // sistema de tipos basado en String sin lógica especial — solo
            // checa si el tipo termina en '*' (ver esPuntero/tipoBase).
            String tipo = categoria.equals("puntero") ? tipoBase + "*" : tipoBase;

            System.out.println("  [Visitor] → Declaración: " + tipo + " " + nombre
                    + (dimensiones > 0 ? " (" + dimensiones + "D)" : ""));

            Symbol s = new Symbol(nombre, tipo, categoria, tok.getLine());
            s.dimensiones = dimensiones;
            s.tamanios = tamanios;
            if (!tabla.agregar(s)) {
                error(tok, "'" + nombre + "' ya fue declarado en este ámbito");
            }

            if (inicializador != null) {
                if (categoria.equals("arreglo")) {
                    error(tok, "no se puede inicializar el arreglo '" + nombre
                            + "' con un valor escalar");
                } else {
                    String tipoExpr = visit(inicializador);
                    if (tipoExpr != null && !compatiblePromocion(tipo, tipoExpr)) {
                        error(tok, "no se puede inicializar '" + nombre + "' de tipo '"
                                + tipo + "' con valor de tipo '" + tipoExpr + "'");
                    }
                }
            }
        }

        return null;
    }

    private String tipoDeParam(MiniCParser.ParamContext param) {
        String base = param.typeSpecifier().getText();

        if (param.declarator() != null) {
            MiniCParser.DeclaratorContext decl = param.declarator();
            if (decl.getChild(0).getText().equals("*")) {
                return base + "*";
            }
            if (decl.INTEGER_CONST() != null && !decl.INTEGER_CONST().isEmpty()) {
                // ej. int m[2][3]  →  "int[2][3]"
                StringBuilder sb = new StringBuilder(base);
                for (var n : decl.INTEGER_CONST()) {
                    sb.append("[").append(n.getText()).append("]");
                }
                return sb.toString();
            }
            return base;
        }

        if (param.INTEGER_CONST() != null) {
            // ej. int m[][3]  →  "int[][3]" (primera dimensión abierta:
            // no se exige tamaño exacto al validar argumentos en visitCall)
            return base + "[][" + param.INTEGER_CONST().getText() + "]";
        }

        return base;
    }

    @Override
    public String visitFuncDef(MiniCParser.FuncDefContext ctx) {
        String tipo   = ctx.typeSpecifier().getText();
        String nombre = ctx.IDENTIFIER().getText();
        Token  tok    = ctx.IDENTIFIER().getSymbol();

        System.out.println("  [Visitor] → Función   : " + nombre + " (" + tipo + ")");

        List<String> tiposParams = new ArrayList<>();
        if (ctx.params() != null) {
            for (MiniCParser.ParamContext param : ctx.params().param()) {
                tiposParams.add(tipoDeParam(param));
            }
        }

        Symbol s = new Symbol(nombre, tipo, "funcion", tok.getLine(), tiposParams);
        if (!tabla.agregar(s)) {
            error(tok, "función '" + nombre + "' ya fue declarada");
        }

        tabla.entrar(nombre);

        indiceParametroActual = 0;
        if (ctx.params() != null) {
            for (MiniCParser.ParamContext param : ctx.params().param()) {
                visitParam(param);
            }
        }

        String tipoRetornoPrevio = tipoRetornoActual;
        tipoRetornoActual = tipo;

        int nivelLoopPrevio = nivelLoop;
        nivelLoop = 0;

        visit(ctx.compoundStmt());

        nivelLoop = nivelLoopPrevio;
        tipoRetornoActual = tipoRetornoPrevio;

        tabla.salir();

        return null;
    }

    @Override
    public String visitParam(MiniCParser.ParamContext ctx) {
        String tipoBase = ctx.typeSpecifier().getText();
        String nombre;
        String categoria;
        Token  tok;
        int    dimensiones = 0;
        int[]  tamanios = null;

        if (ctx.declarator() != null) {
            MiniCParser.DeclaratorContext decl = ctx.declarator();
            boolean esPuntero = decl.getChild(0).getText().equals("*");

            if (esPuntero) {
                // ej: int* b  →  decl es '* declarator', el IDENTIFIER real
                // ("b") vive en el declarator interno (igual que en
                // visitDeclaration). Antes este caso caía en el 'else' de
                // abajo y el parámetro quedaba registrado como escalar
                // común — perdiendo por completo que era un puntero.
                MiniCParser.DeclaratorContext interno = decl.declarator();
                if (interno.getChild(0).getText().equals("*")) {
                    error(ctx.getStart(), "Mini-C solo soporta punteros de un nivel "
                            + "('T*'); no se soportan punteros a punteros ('T**')");
                    return null;
                }
                nombre    = interno.IDENTIFIER().getText();
                tok       = interno.IDENTIFIER().getSymbol();
                categoria = "puntero";
            } else {
                nombre    = decl.IDENTIFIER().getText();
                tok       = decl.IDENTIFIER().getSymbol();
                boolean esArregloSimple = decl.INTEGER_CONST() != null && !decl.INTEGER_CONST().isEmpty();
                categoria = esArregloSimple ? "arreglo" : "parametro";
                if (esArregloSimple) {
                    dimensiones = decl.INTEGER_CONST().size();
                    tamanios = new int[dimensiones];
                    for (int i = 0; i < dimensiones; i++) {
                        tamanios[i] = Integer.parseInt(decl.INTEGER_CONST(i).getText());
                    }
                }
            }
        } else {
            nombre      = ctx.IDENTIFIER().getText();
            tok         = ctx.IDENTIFIER().getSymbol();
            categoria   = "arreglo";
            dimensiones = 2;
            // int m[][3]  →  primera dimensión abierta (-1 = sin tamaño fijo,
            // no se exige al validar argumentos en visitCall).
            tamanios    = new int[]{-1, Integer.parseInt(ctx.INTEGER_CONST().getText())};
        }

        String tipo = categoria.equals("puntero") ? tipoBase + "*" : tipoBase;

        System.out.println("  [Visitor] → Parámetro  : " + tipo + " " + nombre
                + (dimensiones > 0 ? " (" + dimensiones + "D)" : ""));

        Symbol s = new Symbol(nombre, tipo, categoria, tok.getLine());
        s.dimensiones = dimensiones;
        s.tamanios = tamanios;
        s.indiceParametro = indiceParametroActual++;
        if (!tabla.agregar(s)) {
            error(tok, "parámetro '" + nombre + "' duplicado");
        }
        return null;
    }

    @Override
    public String visitCompoundStmt(MiniCParser.CompoundStmtContext ctx) {
        boolean esCuerpoFuncion = ctx.parent instanceof MiniCParser.FuncDefContext;

        if (!esCuerpoFuncion) {
            tabla.entrar("bloque@" + ctx.getStart().getLine());
        }

        visitChildren(ctx);

        if (!esCuerpoFuncion) {
            tabla.salir();
        }

        return null;
    }

    @Override
    public String visitReturnStmt(MiniCParser.ReturnStmtContext ctx) {
        Token tok = ctx.getStart();

        if (ctx.expr() == null) {
            if (tipoRetornoActual != null && !tipoRetornoActual.equals("void")) {
                error(tok, "función de tipo '" + tipoRetornoActual
                        + "' debe retornar un valor");
            }
            return null;
        }

        String tipoExpr = visit(ctx.expr());

        if (tipoRetornoActual != null && tipoExpr != null) {
            if (tipoRetornoActual.equals("void")) {
                error(tok, "función de tipo 'void' no puede retornar un valor");
            } else if (!compatiblePromocion(tipoRetornoActual, tipoExpr)) {
                error(tok, "se esperaba retornar '" + tipoRetornoActual
                        + "', se recibió '" + tipoExpr + "'");
            }
        }

        return tipoExpr;
    }

    @Override
    public String visitBreakStmt(MiniCParser.BreakStmtContext ctx) {
        if (nivelLoop == 0) {
            error(ctx.getStart(), "'break' fuera de un ciclo");
        }
        return null;
    }

    @Override
    public String visitContinueStmt(MiniCParser.ContinueStmtContext ctx) {
        if (nivelLoop == 0) {
            error(ctx.getStart(), "'continue' fuera de un ciclo");
        }
        return null;
    }

    @Override
    public String visitIfStmt(MiniCParser.IfStmtContext ctx) {
        String tipoCond = visit(ctx.expr());
        if (tipoCond != null && !esCondicionValida(tipoCond)) {
            error(ctx.getStart(),
                    "la condición de 'if' debe ser 'bool' o 'int', se recibió '" + tipoCond + "'");
        }

        for (MiniCParser.StatementContext stmt : ctx.statement()) {
            visit(stmt);
        }
        return null;
    }

    @Override
    public String visitWhileStmt(MiniCParser.WhileStmtContext ctx) {
        String tipoCond = visit(ctx.expr());
        if (tipoCond != null && !esCondicionValida(tipoCond)) {
            error(ctx.getStart(),
                    "la condición de 'while' debe ser 'bool' o 'int', se recibió '" + tipoCond + "'");
        }

        nivelLoop++;
        visit(ctx.statement());
        nivelLoop--;
        return null;
    }

    @Override
    public String visitDoWhileStmt(MiniCParser.DoWhileStmtContext ctx) {
        nivelLoop++;
        visit(ctx.statement());
        nivelLoop--;

        String tipoCond = visit(ctx.expr());
        if (tipoCond != null && !esCondicionValida(tipoCond)) {
            error(ctx.getStart(),
                    "la condición de 'do-while' debe ser 'bool' o 'int', se recibió '" + tipoCond + "'");
        }
        return null;
    }

    @Override
    public String visitForStmt(MiniCParser.ForStmtContext ctx) {
        visit(ctx.exprStmt());

        List<MiniCParser.ExprContext> expresiones = ctx.expr();
        if (!expresiones.isEmpty()) {
            String tipoCond = visit(expresiones.get(0));
            if (tipoCond != null && !esCondicionValida(tipoCond)) {
                error(ctx.getStart(),
                        "la condición de 'for' debe ser 'bool' o 'int', se recibió '" + tipoCond + "'");
            }

            if (expresiones.size() > 1) {
                visit(expresiones.get(1));
            }
        }

        nivelLoop++;
        visit(ctx.statement());
        nivelLoop--;
        return null;
    }

    @Override
    public String visitAssignmentExpr(MiniCParser.AssignmentExprContext ctx) {
        if (ctx.lvalue() != null) {
            Token tok = ctx.getStart();
            String nombre = ctx.lvalue().IDENTIFIER().getText();

            // "asignación entre arreglos no permitida" (§4 del enunciado):
            // si el destino es un arreglo completo sin índices, es ilegal
            // sin importar qué haya del lado derecho — chequear ANTES de
            // delegar a visitLvalue, porque ese método le da a un arreglo
            // sin índices un tipo "con forma" (ej. "int[5]") para permitir
            // el caso de pasarlo como argumento de función; sin este
            // chequeo, "a = b;" entre dos arreglos del mismo tamaño
            // pasaría la validación de tipos por accidente.
            boolean esDestinoArregloCompleto = !ctx.lvalue().getChild(0).getText().equals("*")
                    && ctx.lvalue().expr().isEmpty();
            if (esDestinoArregloCompleto) {
                Symbol sDestino = tabla.buscar(nombre);
                if (sDestino != null && sDestino.categoria.equals("arreglo")) {
                    error(tok, "no se puede asignar al arreglo completo '" + nombre
                            + "' (asignación entre arreglos no permitida)");
                    visit(ctx.assignmentExpr()); // visitar el lado derecho igual, por si tiene sus propios errores
                    return sDestino.tipo;
                }
            }

            String tipoIzq = visit(ctx.lvalue());
            String tipoDer = visit(ctx.assignmentExpr());

            if (tipoIzq != null && tipoDer != null && !compatiblePromocion(tipoIzq, tipoDer)) {
                error(tok, "no se puede asignar valor de tipo '" + tipoDer
                        + "' a '" + nombre + "' de tipo '" + tipoIzq + "'");
            }
            return tipoIzq;
        }
        return visit(ctx.logicalOrExpr());
    }

    @Override
    public String visitLogicalOrExpr(MiniCParser.LogicalOrExprContext ctx) {
        return chequearLogico(ctx.logicalAndExpr(), ctx.getStart());
    }

    @Override
    public String visitLogicalAndExpr(MiniCParser.LogicalAndExprContext ctx) {
        return chequearLogico(ctx.equalityExpr(), ctx.getStart());
    }

    private String chequearLogico(List<? extends org.antlr.v4.runtime.tree.ParseTree> operandos, Token tok) {
        List<String> tipos = new ArrayList<>();
        for (var op : operandos) {
            tipos.add(visit(op));
        }

        if (operandos.size() == 1) {
            return tipos.get(0);
        }

        for (String tipo : tipos) {
            if (tipo != null && !tipo.equals("bool")) {
                error(tok, "operador lógico requiere operandos 'bool', se recibió '" + tipo + "'");
            }
        }
        return "bool";
    }

    @Override
    public String visitEqualityExpr(MiniCParser.EqualityExprContext ctx) {
        List<MiniCParser.RelationalExprContext> operandos = ctx.relationalExpr();
        List<String> tipos = new ArrayList<>();
        for (var op : operandos) {
            tipos.add(visit(op));
        }

        if (operandos.size() == 1) {
            return tipos.get(0);
        }

        String tipoPrevio = tipos.get(0);
        for (int i = 1; i < tipos.size(); i++) {
            String tipoActual = tipos.get(i);
            if (tipoPrevio != null && tipoActual != null && !compatiblePromocion(tipoPrevio, tipoActual)) {
                error(ctx.getStart(), "no se puede comparar '" + tipoPrevio
                        + "' con '" + tipoActual + "'");
            }
            tipoPrevio = tipoActual;
        }
        return "bool";
    }

    @Override
    public String visitRelationalExpr(MiniCParser.RelationalExprContext ctx) {
        List<MiniCParser.AdditiveExprContext> operandos = ctx.additiveExpr();
        List<String> tipos = new ArrayList<>();
        for (var op : operandos) {
            tipos.add(visit(op));
        }

        if (operandos.size() == 1) {
            return tipos.get(0);
        }

        for (String tipo : tipos) {
            if (tipo != null && !esNumerico(tipo)) {
                error(ctx.getStart(), "operador relacional requiere 'int' o 'char', se recibió '" + tipo + "'");
            }
        }
        return "bool";
    }

    @Override
    public String visitAdditiveExpr(MiniCParser.AdditiveExprContext ctx) {
        return chequearAritmetico(ctx.multiplicativeExpr(), ctx.getStart());
    }

    @Override
    public String visitMultiplicativeExpr(MiniCParser.MultiplicativeExprContext ctx) {
        return chequearAritmetico(ctx.unaryExpr(), ctx.getStart());
    }

    private String chequearAritmetico(List<? extends org.antlr.v4.runtime.tree.ParseTree> operandos, Token tok) {
        List<String> tipos = new ArrayList<>();
        for (var op : operandos) {
            tipos.add(visit(op));
        }

        if (operandos.size() == 1) {
            return tipos.get(0);
        }

        for (String tipo : tipos) {
            if (tipo != null && !esNumerico(tipo)) {
                error(tok, "operador aritmético requiere 'int' o 'char', se recibió '" + tipo + "'");
            }
        }
        return "int";
    }

    @Override
    public String visitUnaryExpr(MiniCParser.UnaryExprContext ctx) {
        if (ctx.primary() != null) {
            return visit(ctx.primary());
        }
        String op = ctx.getChild(0).getText();
        MiniCParser.UnaryExprContext operandoCtx = ctx.unaryExpr();
        String tipo = visit(operandoCtx);

        if (op.equals("!") && tipo != null && !tipo.equals("bool")) {
            error(ctx.getStart(), "operador '!' requiere 'bool', se recibió '" + tipo + "'");
        }
        if (op.equals("-")) {
            if (tipo != null && !esNumerico(tipo)) {
                error(ctx.getStart(), "operador '-' unario requiere 'int' o 'char', se recibió '" + tipo + "'");
            }
            return "int";
        }
        if (op.equals("&")) {
            // El operando debe ser exactamente una variable o un elemento de
            // arreglo (algo con dirección real en memoria) — no la dirección
            // de un literal, una llamada, o una expresión calculada.
            boolean esDireccionable = operandoCtx.primary() != null
                    && operandoCtx.primary().lvalue() != null;
            if (!esDireccionable) {
                error(ctx.getStart(), "'&' solo puede aplicarse a una variable o a un elemento de arreglo");
                return null;
            }
            if (tipo == null) return null;
            if (esPuntero(tipo)) {
                error(ctx.getStart(), "no se soportan punteros a punteros (Mini-C solo admite un nivel de indirección)");
                return null;
            }
            return tipo + "*";
        }
        if (op.equals("*")) {
            if (tipo == null) return null;
            if (!esPuntero(tipo)) {
                error(ctx.getStart(), "operador '*' (desreferencia) requiere un puntero, se recibió '" + tipo + "'");
                return null;
            }
            return tipoBase(tipo);
        }
        return tipo;
    }

    @Override
    public String visitPrimary(MiniCParser.PrimaryContext ctx) {
        if (ctx.INTEGER_CONST() != null) return "int";
        if (ctx.CHAR_CONST() != null)    return "char";
        if (ctx.STRING_LITERAL() != null) return "string";
        if (ctx.getText().equals("true") || ctx.getText().equals("false")) return "bool";
        if (ctx.expr() != null) return visit(ctx.expr());
        if (ctx.lvalue() != null) return visit(ctx.lvalue());
        if (ctx.call() != null) return visit(ctx.call());
        return null;
    }

    @Override
    public String visitLvalue(MiniCParser.LvalueContext ctx) {
        boolean esDesreferencia = ctx.getChild(0).getText().equals("*");

        String nombre = ctx.IDENTIFIER().getText();
        Token  tok    = ctx.IDENTIFIER().getSymbol();

        Symbol s = tabla.buscar(nombre);
        if (s == null) {
            error(tok, "'" + nombre + "' no fue declarado");
            return null;
        }

        if (esDesreferencia) {
            // *p = expr;  →  el resultado es el tipo al que apunta 'p'
            // (la posición de memoria que se va a leer/escribir), no el
            // tipo del puntero en sí.
            if (!esPuntero(s.tipo)) {
                error(tok, "'" + nombre + "' no es un puntero, no se puede desreferenciar con '*'");
                return null;
            }
            return tipoBase(s.tipo);
        }

        int indicesUsados = ctx.expr().size();

        if (s.categoria.equals("arreglo")) {
            if (indicesUsados == 0) {
                // Usar el arreglo completo sin índices solo es válido para
                // pasarlo como argumento de función (paso por referencia,
                // ej. suma_fila(tabla, i)). Devolvemos un tipo que codifica
                // la forma real (ej. "int[2][3]") — visitCall lo valida
                // contra la forma del parámetro; cualquier otro uso (suma,
                // asignación a escalar, condición, etc.) lo va a rechazar
                // igual, porque ese tipo nunca calza con un tipo escalar.
                return s.tipo + formaArreglo(s.tamanios, s.dimensiones);
            } else if (indicesUsados != s.dimensiones) {
                error(tok, "'" + nombre + "' fue declarado con " + s.dimensiones
                        + " dimensión(es), se usó con " + indicesUsados + " índice(s)");
            }
        } else if (indicesUsados > 0) {
            error(tok, "'" + nombre + "' no es un arreglo, no admite indexación");
        }

        for (int i = 0; i < ctx.expr().size(); i++) {
            MiniCParser.ExprContext idxExpr = ctx.expr(i);
            String tipoIdx = visit(idxExpr);
            if (tipoIdx != null && !tipoIdx.equals("int")) {
                error(tok, "el índice de arreglo debe ser 'int', se recibió '" + tipoIdx + "'");
                continue;
            }

            if (s.categoria.equals("arreglo") && s.tamanios != null
                    && indicesUsados == s.dimensiones && i < s.tamanios.length) {

                Integer valorConstante = evaluarConstante(idxExpr);
                if (valorConstante != null) {
                    int tamanio = s.tamanios[i];
                    if (valorConstante < 0 || valorConstante >= tamanio) {
                        error(tok, "índice " + valorConstante + " fuera de rango para '"
                                + nombre + "' (dimensión " + (i + 1) + " tiene tamaño " + tamanio + ")");
                    }
                }
            }
        }

        return s.tipo;
    }

    @Override
    public String visitCall(MiniCParser.CallContext ctx) {
        String nombre = ctx.IDENTIFIER().getText();
        Token  tok    = ctx.IDENTIFIER().getSymbol();

        Symbol s = tabla.buscar(nombre);
        if (s == null) {
            error(tok, "función '" + nombre + "' no declarada");
            for (MiniCParser.ExprContext arg : ctx.expr()) visit(arg);
            return null;
        }

        if (!s.categoria.equals("funcion")) {
            error(tok, "'" + nombre + "' no es una función");
            for (MiniCParser.ExprContext arg : ctx.expr()) visit(arg);
            return s.tipo;
        }

        List<MiniCParser.ExprContext> args = ctx.expr();

        List<String> tiposArgs = new ArrayList<>();
        for (MiniCParser.ExprContext arg : args) {
            tiposArgs.add(visit(arg));
        }

        List<String> tiposParams = s.tiposParametros;

        if (tiposParams != null && tiposParams.size() != args.size()) {
            error(tok, "función '" + nombre + "' espera " + tiposParams.size()
                    + " argumento(s), se recibieron " + args.size());
            return s.tipo;
        }

        if (tiposParams != null) {
            for (int i = 0; i < tiposParams.size(); i++) {
                String tipoEsperado = tiposParams.get(i);
                String tipoRecibido = tiposArgs.get(i);
                if (tipoEsperado != null && tipoRecibido != null
                        && !tipoArgumentoValido(tipoEsperado, tipoRecibido)) {
                    error(tok, "argumento " + (i + 1) + " de '" + nombre
                            + "' debe ser '" + tipoEsperado + "', se recibió '" + tipoRecibido + "'");
                }
            }
        }

        return s.tipo;
    }

    public void imprimirTabla() {
        System.out.println();
        System.out.println("TABLA DE SÍMBOLOS");
        System.out.println("-".repeat(95));
        System.out.printf("%-22s %-10s %-12s %-40s %s%n",
                "NOMBRE", "TIPO", "CATEGORÍA", "ÁMBITO", "LÍNEA");
        System.out.println("-".repeat(95));

        for (Scope scope : tabla.getTodos()) {
            for (Symbol s : scope.getSimbolos().values()) {
                System.out.printf("%-22s %-10s %-12s %-40s %d%n",
                        s.nombre, s.tipo, s.categoria, scope.rutaCompleta(), s.linea);
            }
        }

        System.out.println("-".repeat(95));
    }

    public int getErrores() { return errores; }
    public SymbolTable getTabla() { return tabla; }
}