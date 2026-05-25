# User Guide - HackAssembler

## Requisitos

- Java instalado.
- Maven instalado para compilar y ejecutar las pruebas.

## Compilar

Desde la carpeta `proyecto3/HackAssembler`:

```bash
mvn clean package
```

## Ejecutar el assembler

```bash
java -cp target/HackAssembler-1.0-SNAPSHOT.jar HackAssembler ruta/Prog.asm
```

El programa genera un archivo `.hack` con la traduccion binaria del archivo `.asm`.

## Ejecutar el disassembler

```bash
java -cp target/HackAssembler-1.0-SNAPSHOT.jar HackDisassembler ruta/Prog.hack
```

El programa genera un archivo `.asm` a partir del archivo `.hack`.

## Ejecutar pruebas

Desde la carpeta `proyecto3/HackAssembler`:

```bash
mvn test
```

## Archivos principales

- `src/HackAssembler.java`: punto de entrada del ensamblador.
- `src/HackDisassembler.java`: punto de entrada del desensamblador.
- `src/Parser.java`: lectura y clasificacion de instrucciones.
- `src/Code.java`: traduccion de mnemonicos a codigo binario.
- `src/SymbolTable.java`: manejo de simbolos, etiquetas y variables.
- `test/HackAssemblerTest.java`: pruebas del ensamblador.
