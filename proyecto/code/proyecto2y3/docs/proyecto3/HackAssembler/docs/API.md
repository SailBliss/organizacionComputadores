# proyecto 3

- **¿Cúal es el objetivo del proyecto 3? →** Construir un **ensamblador (Assembler)** que convierte código `.asm` a binario `.hack` y un **desensamblador (Dissasembler)** que convierte binario `.hack` a código `.asm`.
- **ISA = Instruction Set Architecture**

# Punto 1

## Objetivos

Tenemos que hacer un programa que `Prog.asm` → `Prog.hack`

### Requerimientos

- Que pueda encontrar cualquiera de las siguientes y convertirlo a un binario de 16 bits:
    - Líneas vacías
    - Comentarios (`//`)
    - Instrucciones tipo A → `@value`
    - Instrucciones tipo C → `dest=comp;jump`
    - Etiquetas → `(LOOP)`
- Soportar las nuevas instrucciones del shift:
    
    ```java
    D = M << 1
    AM = D >> 1
    ```
    

### Manejo de errores

- Si TODO está bien → no imprime nada
- Si hay error → muestra:
    - línea del error
    - detiene ejecución

### Entregables

- [ ]  Código fuente (`.java` o `.py`)
- [ ]  Tests
- [x]  Documentación
- [ ]  `.md5` de cada archivo

---

## Proceso

### pom.xml

Especifica que el proyecto es:

- un proyecto Java
- llamado **HackAssembler**
- que se compila con **Java 11**
- que genera un archivo **`.jar`**
- cuyo punto de entrada es la clase **`HackAssembler`**
- y que usa **JUnit** para pruebas

### Arquitectura del HackAssembler

Flujo general

```java
Prog.asm
    |
[Parser] -- lee línea a línea, clasifica instrucciones
    |
[Primera pasada] -- detecta etiquetas (LABEL) -> las mete en SymbolTable
    |
[Segunda pasada] -- traduce A-instructions y C-instructions
    |                             | 
 [SymbolTable]         [Code] -- convierte dest/comp/jump a bits
    |
 Prog.hack
```

**¿Por qué dos pasadas?:** en la primera se recolectan todas las etiquetas `(LOOP)` con su número de línea. Si se hiciera en una sola pasada, un `@LOOP` antes de la declaración `(LOOP)` no podría resolverse.

### Clases

#### **`SymbolTable.java`** - tabla de símbolos

Esta clase guarda los simbolos del ensamblador Hack (los que ya vienen por defecto y los que se van creando).

- Mapa `String → int`
- Precargada con los símbolos predefinidos (`R0`-`R15`, `SCREEN`, `KBD`, etc.)
- Métodos:
    - `addEntry(symbol, address)`
        - Revisa si un simbolo ya esta en la tabla.
        - symbol: simbolo que se quiere buscar
        - retorna true si ya existe
    - `contains(symbol)`
        - Guarda un simbolo nuevo con su direccion
        - symbol: nombre del simbolo (etiqueta o variable
        - address: direccion de memoria o ROM que le corresponde
    - `getAddress(symbol)`
        - Devuelve la direccion que tiene guardada un simbolos
        - symbol: simbolo a consultar (debe existir)
        - retorna la direccion en entero

#### **`Parser.java`** - lector de líneas

Lee y clasifica línea a línea un archivo .asm del ISA Hack. Elimina comentarios y espacios en blanco y expone los campos de cada instrucción (symbol, dest, comp, jump).

- Abre el `.asm`, itera línea a línea
- Elimina comentarios (`//`) y espacios en blanco
- Guarda solo las líneas no vacías en una lista interna
- Clasifica cada línea como: `A_INSTRUCTION`, `C_INSTRUCTION`, `L_INSTRUCTION` (etiqueta)
- Métodos:
    - `hasMoreLines()` → true si quedan líneas por leer
    - `advance()` → avanza a la siguiente línea
    - `instructionType()` → devuelve el tipo de la línea actual
    - `symbol()` → para `A_INSTRUCTION`: valor después de `@`; para `L_INSTRUCTION`: nombre sin paréntesis
    - `dest()` → parte izquierda del `=` (vacío si no hay `=`)
    - `comp()` → parte central entre `=` y `;`
    - `jump()` → parte después de `;` (vacío si no hay `;`)

#### **`HackAssembler.java`** - punto de entrada

Punto de entrada del ensamblador Hack. Traduce un archivo `.asm` a `.hack` usando dos pasadas. También delega al desensamblador con el flag `-d`.

**Uso:**
```
java HackAssembler Prog.asm       →  Prog.hack
java HackAssembler -d Prog.hack   →  ProgDis.asm
```

**Primera pasada** — recorre el archivo buscando solo `L_INSTRUCTION` (`(LABEL)`). Cada etiqueta se registra en `SymbolTable` con el número de instrucción ROM que le corresponde (no cuenta las etiquetas, solo las instrucciones reales).

**Segunda pasada** — recorre el archivo traduciendo instrucciones:

- `L_INSTRUCTION` → se ignora
- `A_INSTRUCTION` con número (`@42`) → binario de 16 bits
- `A_INSTRUCTION` con símbolo (`@sum`) → busca en `SymbolTable`; si no existe, asigna la próxima dirección de RAM disponible (empieza en 16)
- `C_INSTRUCTION` → `111` + `comp(7 bits)` + `dest(3 bits)` + `jump(3 bits)`

#### **`Code.java`** - traductor a bits

Convierte los mnemónicos `dest`, `comp`, `jump` a sus bits respectivos usando tablas internas (`Map<String, String>`).

- `comp(String)` → 7 bits (`a` + `cccccc`)
- `dest(String)` → 3 bits
- `jump(String)` → 3 bits
- Incluye shift: `D<<1` → `0000001`, `D>>1` → `0000011` (de `design.txt`)
- Normaliza espacios internos antes de buscar en la tabla, por lo que acepta tanto `D<<1` como `D << 1`
- Si el mnemónico no existe → lanza `IllegalArgumentException` con el valor no reconocido

**Soporte Shift (B4)**

El ensamblador acepta las instrucciones de shift definidas en `design.txt`:

```
D = D << 1    →   1110000001010000
AM = D >> 1   →   1110000011101000
```

El Parser extrae `comp()` como texto (ej: `"D << 1"`). `Code.comp()` elimina los espacios internos antes de buscar en la tabla, así `"D << 1"` y `"D<<1"` se resuelven igual. El bit `a` se toma del primer carácter del resultado de 7 bits:

- `D<<1` → `0000001` → `a=0`, `cccccc=000001`
- `D>>1` → `0000011` → `a=0`, `cccccc=000011`

#### **`HackDisassembler.java`** - desensamblador

Traduce un archivo `.hack` (binario, 16 bits por línea) a lenguaje ensamblador Hack legible (`.asm`). Es el proceso inverso al ensamblador.

- Lee el `.hack` línea a línea
- Detecta el tipo de instrucción por el primer bit:
    - `0` → A-instruction → `@valor`
    - `111` → C-instruction → decodifica `a`, `cccccc`, `ddd`, `jjj`
- Genera el archivo `ProgDis.asm` en la misma carpeta que el `.hack`
- Soporta shift (`<<` y `>>`) usando el diseño de `design.txt`

**Flujo general**

```
Prog.hack
    |
[leer línea] -- 16 bits por línea
    |
[detectar tipo]
    |              |
  bit[0]=0       bits[0..2]=111
    |              |
A-instruction    C-instruction
@valor           decodificar a/comp/dest/jump
    |              |
    ProgDis.asm ←──┘
```

**Decodificación de C-instruction**

El formato binario es: `1 1 1 a c1 c2 c3 c4 c5 c6 d1 d2 d3 j1 j2 j3`

| Bits | Campo | Descripción |
|------|-------|-------------|
| 15-13 | `111` | Marca instrucción tipo C |
| 12 | `a` | 0 = usa A como operando Y, 1 = usa M |
| 11-6 | `cccccc` | Control de la ALU (operación) |
| 5-3 | `ddd` | Registro destino |
| 2-0 | `jjj` | Condición de salto |

**Tabla dest (`ddd`)**

| Bits | etiqueta |
|------|-----------|
| 000 | (ninguno) |
| 001 | M |
| 010 | D |
| 011 | MD |
| 100 | A |
| 101 | AM |
| 110 | AD |
| 111 | AMD |

**Tabla jump (`jjj`)**

| Bits | etiqueta |
|------|-----------|
| 000 | (ninguno) |
| 001 | JGT |
| 010 | JEQ |
| 011 | JGE |
| 100 | JLT |
| 101 | JNE |
| 110 | JLE |
| 111 | JMP |

**Soporte Shift (de `design.txt`)**

Antes de buscar en la tabla de comp, se revisa si `cccccc` es shift:

- `cccccc = 000001` → `D<<1` (shift left)
- `cccccc = 000011` → `D>>1` (shift right)

El bit `a` se ignora en este caso. El `dest` y `jump` siguen aplicando normalmente.

Ejemplo:
```
111 0 000001 010 000  →  D=D<<1
111 0 000001 101 000  →  AM=D<<1
```

**Manejo de errores**

- Si una línea no tiene exactamente 16 bits → imprime el número de línea y detiene la ejecución
- Si el patrón `a+cccccc` no está en la tabla de comp → imprime el número de línea y detiene la ejecución

**`HackAssemblerTest.java`** - pruebas

# Punto 2

Hacer lo inverso: `Prog.hack` → `ProgDis.asm`

### Requerimientos

- Leer binario como: `1110001100001000` y convertirlo a `D=M`
- Que se ejecute `java HackAssembler -d Prog.hack` y de como resultado `ProgDis.asm`.
- Debe soportar SHIFT. Debe poder traducir: `<<  y  >>` desde binario → assembler

## Proceso

### `HackDisassembler.java`

- El método principal es `disassemble(String inputPath)`
- Carga la tabla de comp como un `Map<String, String>` donde la clave es `a + cccccc` (7 caracteres) y el valor es el mnemónico
- Por cada línea del `.hack`:
    1. Verifica que tenga 16 caracteres
    2. Si `bits[0] == '0'` → parsea como entero → `@valor`
    3. Si `bits[0..2] == "111"` → extrae `cccccc` (bits 4-9) → revisa shift primero, si no busca en tabla comp
    4. Extrae `ddd` (bits 10-12) y `jjj` (bits 13-15) como índices de arreglo
    5. Arma el string `dest=comp;jump` omitiendo las partes vacías

## Fuentes

https://medium.com/@pravinmuruganantham123/a-beginners-guide-to-pom-xml-and-maven-0c2680bc347d

TODO ESTE ARCHIVO FUE ESCRITO EN NOTION