import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.*;
import java.util.List;

public class HackAssemblerTest {

    @TempDir
    Path tempDir;

    // helpers

    private Path writeAsm(String name, String content) throws IOException {
        Path f = tempDir.resolve(name);
        Files.writeString(f, content);
        return f;
    }

    private Path writeHack(String name, String content) throws IOException {
        Path f = tempDir.resolve(name);
        Files.writeString(f, content);
        return f;
    }

    private List<String> runAssembler(Path asmFile) throws Exception {
        HackAssembler.main(new String[] { asmFile.toString() });
        String outName = asmFile.getFileName().toString().replace(".asm", ".hack");
        return Files.readAllLines(tempDir.resolve(outName));
    }

    private List<String> runDisassembler(Path hackFile) throws Exception {
        HackDisassembler.disassemble(hackFile.toString());
        String outName = hackFile.getFileName().toString().replace(".hack", "Dis.asm");
        return Files.readAllLines(tempDir.resolve(outName));
    }

    // instrucciones a

    @Test
    void aInstruction_numerica() throws Exception {
        Path asm = writeAsm("a_num.asm", "@21\n");
        List<String> lines = runAssembler(asm);
        assertEquals("0000000000010101", lines.get(0));
    }

    @Test
    void aInstruction_cero() throws Exception {
        Path asm = writeAsm("a_cero.asm", "@0\n");
        List<String> lines = runAssembler(asm);
        assertEquals("0000000000000000", lines.get(0));
    }

    @Test
    void aInstruction_valorMaximo() throws Exception {
        Path asm = writeAsm("a_max.asm", "@32767\n");
        List<String> lines = runAssembler(asm);
        assertEquals("0111111111111111", lines.get(0));
    }

    // instrucciones c

    @Test
    void cInstruction_destComp() throws Exception {
        // D=A -> 111 0110000 010 000
        Path asm = writeAsm("c_destcomp.asm", "D=A\n");
        List<String> lines = runAssembler(asm);
        assertEquals("1110110000010000", lines.get(0));
    }

    @Test
    void cInstruction_soloJump() throws Exception {
        // 0;JMP -> 111 0101010 000 111
        Path asm = writeAsm("c_jump.asm", "0;JMP\n");
        List<String> lines = runAssembler(asm);
        assertEquals("1110101010000111", lines.get(0));
    }

    @Test
    void cInstruction_destCompJump() throws Exception {
        // AMD=D+1;JGT -> 111 0011111 111 001
        Path asm = writeAsm("c_full.asm", "AMD=D+1;JGT\n");
        List<String> lines = runAssembler(asm);
        assertEquals("1110011111111001", lines.get(0));
    }

    @Test
    void cInstruction_conMemoria() throws Exception {
        // D=M -> 111 1110000 010 000
        Path asm = writeAsm("c_mem.asm", "D=M\n");
        List<String> lines = runAssembler(asm);
        assertEquals("1111110000010000", lines.get(0));
    }
    // tests de ensamblado - simbolos predefinidos

    @Test
    void simboloPredefinido_R0() throws Exception {
        Path asm = writeAsm("sym_r0.asm", "@R0\n");
        List<String> lines = runAssembler(asm);
        assertEquals("0000000000000000", lines.get(0));
    }

    @Test
    void simboloPredefinido_SCREEN() throws Exception {
        // SCREEN = 16384 = 0100000000000000
        Path asm = writeAsm("sym_screen.asm", "@SCREEN\n");
        List<String> lines = runAssembler(asm);
        assertEquals("0100000000000000", lines.get(0));
    }

    @Test
    void simboloPredefinido_KBD() throws Exception {
        // KBD = 24576 = 0110000000000000
        Path asm = writeAsm("sym_kbd.asm", "@KBD\n");
        List<String> lines = runAssembler(asm);
        assertEquals("0110000000000000", lines.get(0));
    }
    // tests de ensamblado - etiquetas y variables

    @Test
    void etiqueta_saltoHaciaAdelante() throws Exception {
        String src = "@END\n" + // @3 (la etiqueta END apunta a linea 3)
                "D=D-1\n" +
                "D=D+1\n" +
                "(END)\n" +
                "0;JMP\n";
        Path asm = writeAsm("label.asm", src);
        List<String> lines = runAssembler(asm);
        // @END -> @3
        assertEquals("0000000000000011", lines.get(0));
        assertEquals("1110001110010000", lines.get(1)); // D=D-1
        assertEquals("1110011111010000", lines.get(2)); // D=D+1
        assertEquals("1110101010000111", lines.get(3)); // 0;JMP
    }

    @Test
    void variable_asignadaDesdeRam16() throws Exception {
        String src = "@miVar\nD=A\n";
        Path asm = writeAsm("var.asm", src);
        List<String> lines = runAssembler(asm);
        // miVar -> RAM[16] = 0000000000010000
        assertEquals("0000000000010000", lines.get(0));
    }

    @Test
    void dosVariables_asignadasConsecutivas() throws Exception {
        String src = "@x\n@y\n";
        Path asm = writeAsm("vars2.asm", src);
        List<String> lines = runAssembler(asm);
        assertEquals("0000000000010000", lines.get(0)); // x -> 16
        assertEquals("0000000000010001", lines.get(1)); // y -> 17
    }
    // tests de ensamblado - comentarios e instrucciones vacias

    @Test
    void comentariosIgnorados() throws Exception {
        String src = "// esto es un comentario\n" +
                "@5 // comentario al final\n" +
                "D=A\n";
        Path asm = writeAsm("comments.asm", src);
        List<String> lines = runAssembler(asm);
        assertEquals(2, lines.size());
        assertEquals("0000000000000101", lines.get(0));
        assertEquals("1110110000010000", lines.get(1));
    }
    // tests de ensamblado - shift (D<<1 / D>>1)

    @Test
    void shift_izquierda() throws Exception {
        // D=D<<1 -> 111 0000001 010 000
        Path asm = writeAsm("shl.asm", "D=D<<1\n");
        List<String> lines = runAssembler(asm);
        assertEquals("1110000001010000", lines.get(0));
    }

    @Test
    void shift_derecha() throws Exception {
        // D=D>>1 -> 111 0000011 010 000
        Path asm = writeAsm("shr.asm", "D=D>>1\n");
        List<String> lines = runAssembler(asm);
        assertEquals("1110000011010000", lines.get(0));
    }
    // tests de desensamblado - instrucciones a

    @Test
    void dis_aInstruction() throws Exception {
        Path hack = writeHack("dis_a.hack", "0000000000010101\n"); // @21
        List<String> lines = runDisassembler(hack);
        assertEquals("@21", lines.get(0));
    }

    @Test
    void dis_aInstruction_cero() throws Exception {
        Path hack = writeHack("dis_a0.hack", "0000000000000000\n");
        List<String> lines = runDisassembler(hack);
        assertEquals("@0", lines.get(0));
    }
    // tests de desensamblado - instrucciones c

    @Test
    void dis_cInstruction_destComp() throws Exception {
        Path hack = writeHack("dis_c1.hack", "1110110000010000\n"); // D=A
        List<String> lines = runDisassembler(hack);
        assertEquals("D=A", lines.get(0));
    }

    @Test
    void dis_cInstruction_soloJump() throws Exception {
        Path hack = writeHack("dis_c2.hack", "1110101010000111\n"); // 0;JMP
        List<String> lines = runDisassembler(hack);
        assertEquals("0;JMP", lines.get(0));
    }

    @Test
    void dis_cInstruction_conMemoria() throws Exception {
        Path hack = writeHack("dis_c3.hack", "1111110000010000\n"); // D=M
        List<String> lines = runDisassembler(hack);
        assertEquals("D=M", lines.get(0));
    }
    // tests de desensamblado - shift

    @Test
    void dis_shiftIzquierda() throws Exception {
        Path hack = writeHack("dis_shl.hack", "1110000001010000\n"); // D=D<<1
        List<String> lines = runDisassembler(hack);
        assertEquals("D=D<<1", lines.get(0));
    }

    @Test
    void dis_shiftDerecha() throws Exception {
        Path hack = writeHack("dis_shr.hack", "1110000011010000\n"); // D=D>>1
        List<String> lines = runDisassembler(hack);
        assertEquals("D=D>>1", lines.get(0));
    }
    // test de ida y vuelta: ensambla y desensambla

    @Test
    void roundtrip_instruccionSimple() throws Exception {
        String src = "D=A\n0;JMP\n";
        Path asm = writeAsm("rt.asm", src);
        List<String> hackLines = runAssembler(asm);

        Path hack = writeHack("rtDis.hack", String.join("\n", hackLines) + "\n");
        List<String> asmLines = runDisassembler(hack);

        assertEquals("D=A", asmLines.get(0));
        assertEquals("0;JMP", asmLines.get(1));
    }
}

