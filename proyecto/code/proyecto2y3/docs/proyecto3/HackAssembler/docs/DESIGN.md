# Design - HackAssembler

## Diagrama de clases

```text
HackAssembler
|-- usa Parser para leer y clasificar instrucciones
|-- usa SymbolTable para resolver simbolos y etiquetas
|-- usa Code para traducir campos dest, comp y jump

HackDisassembler
|-- lee instrucciones binarias .hack
|-- reconstruye instrucciones tipo A y tipo C en formato .asm

Parser
|-- limpia comentarios y espacios
|-- identifica instrucciones A, C y L
|-- extrae symbol, dest, comp y jump

SymbolTable
|-- guarda simbolos predefinidos de Hack
|-- registra etiquetas encontradas en la primera pasada
|-- asigna direcciones a variables nuevas

Code
|-- traduce mnemonicos Hack a bits
|-- soporta instrucciones aritmeticas, logicas, saltos y shift
```

## Flujo del assembler

```text
Archivo .asm
    |
    v
Parser
    |
    v
Primera pasada: registrar etiquetas en SymbolTable
    |
    v
Segunda pasada: traducir instrucciones A y C
    |
    v
Archivo .hack
```

## Flujo del disassembler

```text
Archivo .hack
    |
    v
Lectura de instrucciones binarias
    |
    v
Traduccion a instrucciones A o C
    |
    v
Archivo .asm
```
