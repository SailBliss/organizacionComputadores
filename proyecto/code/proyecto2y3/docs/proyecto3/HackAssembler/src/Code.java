import java.util.*;

// Convierte dest/comp/jump a sus bits respectivos
public class Code {

    private static final Map<String, String> COMP_MAP = new HashMap<>();
    private static final Map<String, String> DEST_MAP = new HashMap<>();
    private static final Map<String, String> JUMP_MAP = new HashMap<>();

    static {
        // comp: a=0
        COMP_MAP.put("0", "0101010");
        COMP_MAP.put("1", "0111111");
        COMP_MAP.put("-1", "0111010");
        COMP_MAP.put("D", "0001100");
        COMP_MAP.put("A", "0110000");
        COMP_MAP.put("!D", "0001101");
        COMP_MAP.put("!A", "0110001");
        COMP_MAP.put("-D", "0001111");
        COMP_MAP.put("-A", "0110011");
        COMP_MAP.put("D+1", "0011111");
        COMP_MAP.put("A+1", "0110111");
        COMP_MAP.put("D-1", "0001110");
        COMP_MAP.put("A-1", "0110010");
        COMP_MAP.put("D+A", "0000010");
        COMP_MAP.put("D-A", "0010011");
        COMP_MAP.put("A-D", "0000111");
        COMP_MAP.put("D&A", "0000000");
        COMP_MAP.put("D|A", "0010101");
        // comp: a=1
        COMP_MAP.put("M", "1110000");
        COMP_MAP.put("!M", "1110001");
        COMP_MAP.put("-M", "1110011");
        COMP_MAP.put("M+1", "1110111");
        COMP_MAP.put("M-1", "1110010");
        COMP_MAP.put("D+M", "1000010");
        COMP_MAP.put("D-M", "1010011");
        COMP_MAP.put("M-D", "1000111");
        COMP_MAP.put("D&M", "1000000");
        COMP_MAP.put("D|M", "1010101");
        // shift (design.txt): a ignorado, cccccc = 000001 / 000011
        COMP_MAP.put("D<<1", "0000001");
        COMP_MAP.put("D>>1", "0000011");

        // dest
        DEST_MAP.put("", "000");
        DEST_MAP.put("M", "001");
        DEST_MAP.put("D", "010");
        DEST_MAP.put("MD", "011");
        DEST_MAP.put("A", "100");
        DEST_MAP.put("AM", "101");
        DEST_MAP.put("AD", "110");
        DEST_MAP.put("AMD", "111");

        // jump
        JUMP_MAP.put("", "000");
        JUMP_MAP.put("JGT", "001");
        JUMP_MAP.put("JEQ", "010");
        JUMP_MAP.put("JGE", "011");
        JUMP_MAP.put("JLT", "100");
        JUMP_MAP.put("JNE", "101");
        JUMP_MAP.put("JLE", "110");
        JUMP_MAP.put("JMP", "111");
    }

    public static String comp(String mnemonic) {
        // normaliza espacios para aceptar "D << 1" y "D<<1" por igual
        String bits = COMP_MAP.get(mnemonic.replaceAll("\\s+", ""));
        if (bits == null)
            throw new IllegalArgumentException("comp no reconocido: " + mnemonic);
        return bits;
    }

    public static String dest(String mnemonic) {
        String bits = DEST_MAP.get(mnemonic);
        if (bits == null)
            throw new IllegalArgumentException("dest no reconocido: " + mnemonic);
        return bits;
    }

    public static String jump(String mnemonic) {
        String bits = JUMP_MAP.get(mnemonic);
        if (bits == null)
            throw new IllegalArgumentException("jump no reconocido: " + mnemonic);
        return bits;
    }
}
