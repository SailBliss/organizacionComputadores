import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    public enum InstructionType {
        A_INSTRUCTION, // @valor ó @SIMBOLO
        C_INSTRUCTION, // dest=comp;jump
        L_INSTRUCTION // (ETIQUETA)
    }

    private final List<String> lines;
    private int current;
    private String currentLine;

    // Abre el archivo y guarda solo las lineas utiles (sin vacias ni comentarios).
    public Parser(String filename) throws IOException {
        lines = new ArrayList<>();
        current = -1;

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String raw;
            while ((raw = reader.readLine()) != null) {
                // Quita comentarios en linea y luego espacios.
                int commentIdx = raw.indexOf("//");
                if (commentIdx >= 0)
                    raw = raw.substring(0, commentIdx);
                raw = raw.trim();
                if (!raw.isEmpty())
                    lines.add(raw);
            }
        }
    }

    // Dice si todavia quedan lineas por leer
    public boolean hasMoreLines() {
        return current < lines.size() - 1;
    }

    // Avanza a la siguiente instruccion
    public void advance() {
        current++;
        currentLine = lines.get(current);
    }

    // Identifica si la instruccion actual es A, C o L.
    public InstructionType instructionType() {
        if (currentLine.startsWith("@"))
            return InstructionType.A_INSTRUCTION;
        if (currentLine.startsWith("("))
            return InstructionType.L_INSTRUCTION;
        return InstructionType.C_INSTRUCTION;
    }

    // para A devuelve lo que va despues de @ y para L devuelve la etiqueta sin
    // parentesis.
    public String symbol() {
        if (instructionType() == InstructionType.A_INSTRUCTION) {
            return currentLine.substring(1); // quita el @
        } else {
            // quita ( y )
            return currentLine.substring(1, currentLine.length() - 1);
        }
    }

    // En una C-instruction, devuelve dest. Si no hay, devuelve ""
    public String dest() {
        if (currentLine.contains("=")) {
            return currentLine.split("=")[0].trim();
        }
        return "";
    }

    // En una C instruction, devuelve comp
    public String comp() {
        String tmp = currentLine;
        if (tmp.contains("="))
            tmp = tmp.split("=", 2)[1];
        if (tmp.contains(";"))
            tmp = tmp.split(";")[0];
        return tmp.trim();
    }

    // En una C-instruction, devuelve jump. Si no hay, devuelve ""
    public String jump() {
        if (currentLine.contains(";")) {
            return currentLine.split(";")[1].trim();
        }
        return "";
    }

    // reinicia el parser para volver a leer desde el inicio
    public void reset() {
        current = -1;
        currentLine = null;
    }
}
