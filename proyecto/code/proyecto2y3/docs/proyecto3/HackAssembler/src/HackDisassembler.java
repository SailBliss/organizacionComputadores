import java.io.*;
import java.nio.file.*;
import java.util.*;

//Traduce un archivo .hack a .asm 
public class HackDisassembler {

    private static final Map<String, String> COMP = new HashMap<>();

    static {
        // a = 0
        COMP.put("0101010", "0");
        COMP.put("0111111", "1");
        COMP.put("0111010", "-1");
        COMP.put("0001100", "D");
        COMP.put("0110000", "A");
        COMP.put("0001101", "!D");
        COMP.put("0110001", "!A");
        COMP.put("0001111", "-D");
        COMP.put("0110011", "-A");
        COMP.put("0011111", "D+1");
        COMP.put("0110111", "A+1");
        COMP.put("0001110", "D-1");
        COMP.put("0110010", "A-1");
        COMP.put("0000010", "D+A");
        COMP.put("0010011", "D-A");
        COMP.put("0000111", "A-D");
        COMP.put("0000000", "D&A");
        COMP.put("0010101", "D|A");
        // a = 1
        COMP.put("1110000", "M");
        COMP.put("1110001", "!M");
        COMP.put("1110011", "-M");
        COMP.put("1110111", "M+1");
        COMP.put("1110010", "M-1");
        COMP.put("1000010", "D+M");
        COMP.put("1010011", "D-M");
        COMP.put("1000111", "M-D");
        COMP.put("1000000", "D&M");
        COMP.put("1010101", "D|M");
    }

    private static final String[] DEST = {
            null, "M", "D", "MD", "A", "AM", "AD", "AMD"
    };

    private static final String[] JUMP = {
            null, "JGT", "JEQ", "JGE", "JLT", "JNE", "JLE", "JMP"
    };

    public static void disassemble(String inputPath) throws IOException {
        Path inFile = Paths.get(inputPath);
        String baseName = inFile.getFileName().toString().replaceAll("\\.hack$", "");
        Path outFile = inFile.resolveSibling(baseName + "Dis.asm");

        List<String> lines = Files.readAllLines(inFile);
        List<String> output = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String raw = lines.get(i).trim();
            if (raw.isEmpty())
                continue;

            if (raw.length() != 16) {
                System.err.println(
                        "Error en línea " + (i + 1) + ": se esperaban 16 bits, se encontraron " + raw.length());
                System.exit(1);
            }

            output.add(decodeLine(raw, i + 1));
        }

        Files.write(outFile, output);
        System.out.println("Archivo generado: " + outFile);
    }

    private static String decodeLine(String bits, int lineNum) {
        if (bits.charAt(0) == '0') {
            // A-instruction
            int value = Integer.parseInt(bits, 2);
            return "@" + value;
        }

        if (!bits.startsWith("111")) {
            System.err.println("Error en línea " + lineNum + ": instrucción no reconocida: " + bits);
            System.exit(1);
        }

        // C-instruction
        String cccccc = bits.substring(4, 10); // bits 11-6

        // Detectar shift antes de cualquier otra decodificación
        if (cccccc.equals("000001") || cccccc.equals("000011")) {
            return decodeShift(bits, cccccc);
        }

        String aAndComp = bits.charAt(3) + cccccc; // a + cccccc
        String comp = COMP.get(aAndComp);
        if (comp == null) {
            System.err.println("Error en línea " + lineNum + ": comp no reconocido: " + aAndComp);
            System.exit(1);
        }

        int destIdx = Integer.parseInt(bits.substring(10, 13), 2);
        int jumpIdx = Integer.parseInt(bits.substring(13, 16), 2);

        String dest = DEST[destIdx];
        String jump = JUMP[jumpIdx];

        StringBuilder sb = new StringBuilder();
        if (dest != null)
            sb.append(dest).append("=");
        sb.append(comp);
        if (jump != null)
            sb.append(";").append(jump);
        return sb.toString();
    }

    private static String decodeShift(String bits, String cccccc) {
        // shift left: 000001, shift right: 000011
        String op = cccccc.equals("000001") ? "D<<1" : "D>>1";

        int destIdx = Integer.parseInt(bits.substring(10, 13), 2);
        int jumpIdx = Integer.parseInt(bits.substring(13, 16), 2);

        String dest = DEST[destIdx];
        String jump = JUMP[jumpIdx];

        StringBuilder sb = new StringBuilder();
        if (dest != null)
            sb.append(dest).append("=");
        sb.append(op);
        if (jump != null)
            sb.append(";").append(jump);
        return sb.toString();
    }
}
