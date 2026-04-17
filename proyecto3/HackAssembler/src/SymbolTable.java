import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    private final Map<String, Integer> table;

    // Crea la tabla y mete los simbolos predefinidos de Hack
    public SymbolTable() {
        table = new HashMap<>();

        // Registros R0-R15
        for (int i = 0; i <= 15; i++) {
            table.put("R" + i, i);
        }

        // Punteros principales
        table.put("SP", 0);
        table.put("LCL", 1);
        table.put("ARG", 2);
        table.put("THIS", 3);
        table.put("THAT", 4);

        // in-out
        table.put("SCREEN", 16384);
        table.put("KBD", 24576);
    }

    public void addEntry(String symbol, int address) {
        table.put(symbol, address);
    }

    public boolean contains(String symbol) {
        return table.containsKey(symbol);
    }

    public int getAddress(String symbol) {
        return table.get(symbol);
    }
}
