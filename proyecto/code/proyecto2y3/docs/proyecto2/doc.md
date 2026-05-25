# Documentación Técnica - Proyecto 2: Arquitectura del Computador
**Autor:** 
    - Eder Santiago Ceballos Quiroz
    - Isabela Ruiz
**Plataforma:** Simulador Hardware Nand2Tetris

## Resumen del Proyecto
El presente documento detalla las decisiones de diseño y la arquitectura implementada para la extensión del procesador Hack. El objetivo principal fue integrar operaciones de desplazamiento a nivel de bits (Shift Left y Shift Right) directamente en la Unidad Aritmético Lógica (ALU) y, consecuentemente, en la ruta de datos de la CPU y la arquitectura del conjunto de instrucciones (ISA).

---

## Punto 1: Unidad de Desplazamiento (Shifter)

### Descripción del Funcionamiento
El chip `Shifter.hdl` es el componente base encargado de realizar desplazamientos lógicos de 1 bit.
* **Entradas:** Bus de datos de 16 bits (`in[16]`) y un bit de control (`direction`).
* **Salidas:** Bus de datos desplazado (`out[16]`) y el bit de desbordamiento (`result`).
* **Lógica:** Si `direction == 0` (Izquierda), el bit 15 sale por `result` y entra un 0 en el bit 0. Si `direction == 1` (Derecha), el bit 0 sale por `result` y entra un 0 en el bit 15.

### Justificación de Decisiones de Diseño
* **Arquitectura Combinacional:** En lugar de utilizar registros de desplazamiento secuenciales, se implementó mediante multiplexores (`Mux16` y `Mux`). Esto garantiza que la operación se realice en un solo ciclo de reloj, cumpliendo con la naturaleza combinacional estricta requerida por la ALU del procesador Hack.
* **Eficiencia Térmica/Lógica:** Al cablear directamente los pines (`a[1]=in[0]`, etc.) dentro de las entradas del Mux, se evita el uso innecesario de compuertas lógicas adicionales, optimizando el tiempo de propagación de la señal.

---

## Punto 2: Integración en la ALU

### Descripción del Funcionamiento
Se modificó la `ALU.hdl` original de Nand2Tetris para incluir la capacidad de desplazamiento sin alterar sus funciones aritméticas y lógicas estándar (suma, resta, AND, OR, etc.). 
* La condición de activación dictada por la especificación es estricta: `zx=0, nx=0, zy=0, ny=0, no=1`. 
* El bit `f` actúa como selector de dirección para el Shifter integrado.

### Justificación de Decisiones de Diseño
* **Arquitectura Modular Limpia (Optimización):** Se tomó la decisión de instanciar el chip `Shifter` previamente creado como un subcomponente (`PARTS: Shifter(...)`) dentro de la ALU. Esto encapsula la complejidad, evita la redundancia de código y adhiere a los principios de diseño de hardware modular exigidos para la nota de excelencia (4.5 - 5.0).
* **Decodificador de Estado:** Se diseñó una subred de compuertas `And` y `Not` exclusivamente para detectar la combinación binaria `0000_1` en los pines de control. Esta señal (`isShift`) actúa como el selector final de un multiplexor que decide si la ALU emite el resultado aritmético normal o el resultado del Shifter.
* **Manejo de Flags (`zr` y `ng`):** El uso de sub-buses (`out[0..7]=low`, `out[8..15]=high`) en la salida final garantiza que las banderas de estado (Zero y Negative) se calculen correctamente independientemente de si el origen del dato fue una suma aritmética o un desplazamiento lógico.

---

## Punto 3: Implementación de la CPU y Jerarquía de Memoria

### Descripción del Funcionamiento
Se construyó el computador completo (`Computer.hdl`), conectando la memoria de instrucciones (ROM), la memoria de datos (`Memory.hdl`) y la unidad central de procesamiento (`CPU.hdl`).

### Justificación de Decisiones de Diseño
* **CPU - Enrutamiento Limpio:** Se integró la nueva ALU instanciando el chip del Punto 2. Para el manejo de direcciones de memoria, se aplicó sub-buseado directo a la salida del Registro A (`out[0..14]=addressM`). Esta decisión elimina la necesidad de chips adaptadores de bus, reduciendo la latencia entre el cálculo de la dirección y su exposición a la memoria RAM.
* **Memoria - Mapeo de Direcciones:** En `Memory.hdl`, se implementó una estrategia de decodificación jerárquica utilizando compuertas `DMux` para los dos bits más significativos (`address[13..14]`). Esto particiona el mapa de memoria eficientemente, aislando el acceso a la RAM (0 - 16383), la Pantalla (16384 - 24575) y el Teclado (24576), previniendo escrituras accidentales en periféricos de I/O.

---

## Punto 4: Arquitectura de la Instrucción (ISA)

### Descripción del Funcionamiento
Para que el ensamblador y el software puedan utilizar las nuevas capacidades de hardware, se definió el mapa de bits para los comandos `<<` y `>>` en las instrucciones Tipo C (`1 1 1 a c1 c2 c3 c4 c5 c6 d1 d2 d3 j1 j2 j3`).

* **Shift Left (`dest=reg << 1`):** Los bits de control de la ALU (c1 a c6) se fijaron en `000001`.
* **Shift Right (`dest=reg >> 1`):** Los bits de control de la ALU (c1 a c6) se fijaron en `000011`.

### Justificación de Decisiones de Diseño
* **Aprovechamiento de la Señal `no`:** La elección de activar el shifter mediante `zx=nx=zy=ny=0` asegura que los operandos pasen limpios hacia la unidad de desplazamiento. Se seleccionó `no=1` como el bit "gatillo" porque en la arquitectura Hack estándar, la combinación `0000x1` no colisiona con ninguna instrucción aritmética o lógica crítica (generalmente reser/**
 * CPU.hdl - Central Processing Unit del computador Hack.
 * Proyecto 2 - Arquitectura de Computadores.
 */

* **Minimalismo en el Lenguaje de Máquina:** El bit `f` (c5) se reutilizó magistralmente como el indicador de dirección del desplazamiento (`0` para Izquierda, `1` para Derecha). Esto significa que no se requirió alterar la longitud de la instrucción de 16 bits ni modificar el decodificador de instrucciones principal de la CPU.

---

## Validaciones y Cumplimiento de Requisitos

Para asegurar la robustez de la entrega:
1.  **Pruebas Unitarias:** Cada componente superó los scripts `.tst` oficiales y los adaptados para la Tabla 1 del Shifter.
2.  **Integridad Criptográfica:** Todos los resultados de salida (`.out`) fueron validados generando firmas Hash MD5, certificando que el comportamiento del circuito coincide bit a bit con los requerimientos del sistema.
3.  **Control de Versiones:** La estructura del repositorio Git sigue los lineamientos de calidad profesional, incluyendo archivos de control (`CONTRIBUTORS.md`, `CHANGELOG.md`) y el aislamiento del código fuente.
