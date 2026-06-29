@echo off
REM ============================================================
REM Compila un programa Mini-C a MIPS32 y lo abre en QtSPIM.
REM
REM Uso:  ejecutar.bat archivo.mc [-O]
REM
REM Requiere que QtSPIM este instalado y que QTSPIM_PATH (abajo)
REM apunte al .exe real en tu maquina.
REM ============================================================

REM Cambiar la consola a UTF-8 para que los acentos y la tabla
REM de simbolos se vean correctamente.
chcp 65001 > nul

set QTSPIM_PATH="C:\Program Files (x86)\QtSpim\QtSpim.exe"

if "%~1"=="" (
    echo Uso: ejecutar.bat archivo.mc [-O]
    exit /b 1
)

set ENTRADA=%~1
set SALIDA=%~n1.s
set OPT=%2

echo Compilando %ENTRADA% -^> %SALIDA% ...
java -cp "target\classes;%USERPROFILE%\.m2\repository\org\antlr\antlr4-runtime\4.13.1\antlr4-runtime-4.13.1.jar" com.minic.Main "%ENTRADA%" -S -o "%SALIDA%" --dump-ir %OPT%

if %ERRORLEVEL% NEQ 0 (
    echo Hubo errores de compilacion, no se genero %SALIDA%.
    exit /b 1
)

echo Abriendo %SALIDA% en QtSPIM ...
start "" %QTSPIM_PATH% "%SALIDA%"