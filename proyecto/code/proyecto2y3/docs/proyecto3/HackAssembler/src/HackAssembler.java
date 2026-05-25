import java.io.*;
import java.nio.file.*;
import java.util.*;

//ensambla Prog.asm -> Prog.hack  o  desensambla con -d Prog.hack
public class HackAssembler {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Uso: HackAssembler <Prog.asm>  |  HackAssembler -d <Prog.hack>");
            System.exit(1);
        }

        if (args[0].equals("-d")) {
            if (args.length < 2) {
                System.err.println("Uso: HackAssembler -d <Prog.hack>");
                System.exit(1);
            }
            HackDisassembler.disassemble(args[1]);
        } else {
            assemble(args[0]);
        }
    }

    private static void assemble(String inputPath) throws Exception {
        SymbolTable symbols = new SymbolTable();

        // Primera pasada -Z recolectar etiquetas (L_INSTRUCTION)
        Parser p1 = new Parser(inputPath);
        int romAddress = 0;
        while (p1.hasMoreLines()) {
            p1.advance();
            if (p1.instructionType() == Parser.InstructionType.L_INSTRUCTION) {
                symbols.addEntry(p1.symbol(), romAddress);
            } else {
                romAddress++;
            }
        }

        // Segunda pasada: traducir instrucciones A y C
        Parser p2 = new Parser(inputPath);
        List<String> output = new ArrayList<>();
        int varAddress = 16; // variables empiezan en RAM[16]

        while (p2.hasMoreLines()) {
            p2.advance();
            Parser.InstructionType type = p2.instructionType();

            if (type == Parser.InstructionType.L_INSTRUCTION)
                continue;

            if (type == Parser.InstructionType.A_INSTRUCTION) {
                String sym = p2.symbol();
                int value;
                if (sym.matches("\\d+")) {
                    value = Integer.parseInt(sym);
                } else {
                    if (!symbols.contains(sym)) {
                        symbols.addEntry(sym, varAddress++);
                    }
                    value = symbols.getAddress(sym);
                }
                output.add(String.format("%16s", Integer.toBinaryString(value)).replace(' ', '0'));

            } else {
                String compBits = Code.comp(p2.comp());
                String destBits = Code.dest(p2.dest());
                String jumpBits = Code.jump(p2.jump());
                output.add("111" + compBits + destBits + jumpBits);
            }
        }

        // archivo de salida
        String outPath = inputPath.replaceAll("\\.asm$", "") + ".hack";
        Files.write(Paths.get(outPath), output);
        System.out.println("Archivo generado: " + outPath);
    }
}
