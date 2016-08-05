/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 socraticphoenix@gmail.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * @author Socratic_Phoenix (socraticphoenix@gmail.com)
 */
package com.gmail.socraticphoenix.jaisbal;

import com.gmail.socraticphoenix.jaisbal.app.gui.ErrorScreen;
import com.gmail.socraticphoenix.jaisbal.app.modes.FileMode;
import com.gmail.socraticphoenix.jaisbal.app.modes.GuiMode;
import com.gmail.socraticphoenix.jaisbal.app.modes.InputMode;
import com.gmail.socraticphoenix.jaisbal.util.DangerousConsumer;
import com.gmail.socraticphoenix.jaisbal.program.JAISBaLExecutionException;
import com.gmail.socraticphoenix.jaisbal.util.StringNumberCaster;
import com.gmail.socraticphoenix.jaisbal.encode.JAISBaLCharPage;
import com.gmail.socraticphoenix.jaisbal.encode.JAISBaLCharset;
import com.gmail.socraticphoenix.jaisbal.program.Program;
import com.gmail.socraticphoenix.jaisbal.program.SecurityMonitor;
import com.gmail.socraticphoenix.jaisbal.program.instructions.util.ConstantInstruction;
import com.gmail.socraticphoenix.jaisbal.program.instructions.Instruction;
import com.gmail.socraticphoenix.jaisbal.program.instructions.InstructionRegistry;
import com.gmail.socraticphoenix.plasma.file.jlsc.JLSCException;
import com.gmail.socraticphoenix.plasma.math.PlasmaMathUtil;
import com.gmail.socraticphoenix.plasma.reflection.util.PlasmaReflectionUtil;
import com.gmail.socraticphoenix.plasma.string.BracketCounter;
import com.gmail.socraticphoenix.plasma.string.CharacterStream;
import com.gmail.socraticphoenix.plasma.string.Escaper;
import com.gmail.socraticphoenix.plasma.string.PlasmaStringUtil;
import com.gmail.socraticphoenix.plasma.string.QuotationTracker;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JAISBaL {
    private static JAISBaLCharPage rootPage;
    private static List<JAISBaLCharPage> supplementaryPages;
    private static List<JAISBaLCharPage> constantPages;

    private static PrintStream out;
    private static Supplier<String> in;

    public static Supplier<String> getIn() {
        return in;
    }

    public static void setIn(Supplier<String> in) {
        JAISBaL.in = in;
    }

    public static PrintStream getOut() {
        return out;
    }

    public static void setOut(PrintStream out) {
        JAISBaL.out = out;
    }

    public static void main(String[] a) throws IOException, JAISBaLExecutionException, JLSCException {
        boolean gui = false;
        try {

            JAISBaL.out = System.out;
            JAISBaL.generalInit();
            if (a.length == 0) {
                JAISBaL.charsetInit();
                gui = true;
                InstructionRegistry.setMonitor(new SecurityMonitor(100));
                modes().get("gui").accept(getDefaultArgs());
            } else {
                Map<String, String> args = new HashMap<>();
                args.put("immediate-quit", "false");
                for (String s : a) {
                    String[] pieces = s.split("(:|=)", 2);
                    if (pieces.length != 2) {
                        JAISBaL.getOut().println("Unrecognized argument \"" + s + "\" no = or : name separator was found.");
                        args.put("immediate-quit", "true");
                        break;
                    } else {
                        args.put(pieces[0], pieces[1]);
                    }
                }

                int slevel;
                if(args.containsKey("security") && PlasmaMathUtil.isInteger(args.get("security"))) {
                    slevel = Integer.parseInt(args.get("security"));
                } else {
                    slevel = 100;
                }

                InstructionRegistry.setMonitor(new SecurityMonitor(slevel));

                if (!args.get("immediate-quit").equals("true")) {
                    if (args.containsKey("dev")) {
                        String action = args.get("dev");
                        switch (action) {
                            case "seq": {
                                JAISBaL.generateInstructionsSequence("Monospaced.plain");
                                break;
                            }
                            case "spec": {
                                JAISBaL.charsetInit();
                                JAISBaL.generateSpecFile();
                                break;
                            }
                            case "pages": {
                                JAISBaL.generateCodePages();
                                break;
                            }
                            default:
                                JAISBaL.getOut().println("Unknown dev action \"" + action + "\"");
                        }
                    } else {
                        JAISBaL.charsetInit();
                        Map<String, String> defaults = JAISBaL.getDefaultArgs();
                        defaults.entrySet().stream().filter(e -> !args.containsKey(e.getKey())).forEach(e -> args.put(e.getKey(), e.getValue()));
                        Map<String, DangerousConsumer<Map<String, String>>> modes = JAISBaL.modes();

                        gui = "gui".equals(args.get("mode"));

                        String mode = args.get("mode");
                        if (modes.containsKey(mode)) {
                            modes.get(mode).accept(args);
                        } else {
                            JAISBaL.getOut().println("Unknown mode \"" + mode + "\"");
                        }
                    }
                }
            }
        } catch (Throwable e) {
            if (gui) {
                new ErrorScreen("An error occurred that was uncaught by handlers", e).make().setVisible(true);
            }
            e.printStackTrace(JAISBaL.getOut());
            if(JAISBaL.getOut() != System.out) {
                e.printStackTrace(System.out);
            }
        }
    }

    public static void generateSpecFile() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("INSTRUCTIONS.md"));
        writer.write("#JAISBaL Instruction Reference");
        writer.newLine();
        writer.write("\tFor the sake of convenience, a is used to refer to the top value of the stack, b is used to refer to the second value on the stack, c is used to refer to the third value on the stack, and so on.");
        writer.newLine();
        writer.newLine();
        writer.write("\tBelow are all defined instructions and constants. Although not stated in every specification, most instructions that accept an array can also accept a string, and many instructions are vectorized. A vectorized instruction operates normally so long as its arguments are passed normally, however if one of the arguments is an array, the instruction will be applied across the array, using its other arguments. As a rule of thumb, instructions that define behavior between operands which are not arrays will be vectorized (such as exponentiation, modulus, rounding operations, etc.).");
        writer.newLine();
        writer.newLine();
        writer.write("Standard Instructions, ");
        writer.write(InstructionRegistry.getStandardInstructions().size() + " defined");
        writer.newLine();
        writer.newLine();
        writer.write(InstructionRegistry.getStandardInstructionsDocumentation().githubFormat());
        writer.newLine();
        writer.write("Supplementary Instructions, ");
        writer.write(InstructionRegistry.getSupplementaryInstructions().size() + " defined");
        writer.newLine();
        writer.newLine();
        writer.write(InstructionRegistry.getSupplementaryInstructionsDocumentation().githubFormat());
        writer.newLine();
        writer.write("Standard Constants, ");
        writer.write(InstructionRegistry.getConstants().size() + " defined");
        writer.newLine();
        writer.newLine();
        writer.write(InstructionRegistry.getConstantsDocumentation().githubFormat());
        writer.newLine();
        writer.write("Auxiliary Instructions, ");
        writer.write(InstructionRegistry.getAuxiliaryInstructions().size() + " defined");
        writer.newLine();
        writer.newLine();
        writer.write(InstructionRegistry.getAuxiliaryInstructionsDocumentation().githubFormat());
        writer.newLine();
        writer.write("Auxiliary Constants, ");
        writer.write(InstructionRegistry.getAuxiliaryConstants().size() + " defined");
        writer.newLine();
        writer.newLine();
        writer.write(InstructionRegistry.getAuxiliaryConstantsDocumentation().githubFormat());
        writer.close();
    }

    public static Map<String, DangerousConsumer<Map<String, String>>> modes() {
        return new HashMap<String, DangerousConsumer<Map<String, String>>>() {{
            put("input", new InputMode());
            put("file", new FileMode());
            put("gui", new GuiMode());
        }};
    }

    public static void collectAndPrintInfo(Program program) throws JAISBaLExecutionException {
        String source = program.getContent();
        if (JAISBaLCharset.getCharset().newEncoder().canEncode(source)) {
            JAISBaL.getOut().println("JAISBaL bytes: " + source.getBytes(JAISBaLCharset.getCharset()).length);
            JAISBaL.getOut().println("UTF-8 bytes: " + source.getBytes(StandardCharsets.UTF_8).length);
        } else {
            char bad = '\0';
            for (char c : source.toCharArray()) {
                if (!JAISBaLCharset.getCharset().newEncoder().canEncode(c)) {
                    bad = c;
                    break;
                }
            }
            JAISBaL.getOut().println("JAISBaL bytes: error: unsupported character '" + PlasmaStringUtil.escape(String.valueOf(bad)) + "'");
            JAISBaL.getOut().println("UTF-8 bytes: " + source.getBytes(StandardCharsets.UTF_8).length);
        }
    }

    public static Map<String, String> getDefaultArgs() {
        return new HashMap<String, String>() {{
            put("mode", "input");
            put("action", "exec");
            put("encoding", "JAISBAL");
            put("target-encoding", "JAISBAL");
            put("resource", "false");
            put("exec-number", "1");
        }};
    }

    public static List<JAISBaLCharPage> getAllPages() {
        List<JAISBaLCharPage> list = new ArrayList<>();
        list.add(JAISBaL.getRootPage());
        list.addAll(JAISBaL.getConstantPages());
        list.addAll(JAISBaL.getSupplementaryPages());
        return list;
    }

    public static JAISBaLCharPage getRootPage() {
        return rootPage;
    }

    public static List<JAISBaLCharPage> getSupplementaryPages() {
        return supplementaryPages;
    }

    public static List<JAISBaLCharPage> getConstantPages() {
        return constantPages;
    }

    private static void generalInit() throws IOException {
        PlasmaReflectionUtil.registerCaster(new StringNumberCaster());
        InstructionRegistry.registerDefaults();
    }

    private static void charsetInit() throws IOException {
        JAISBaL.rootPage = JAISBaLCharPage.of("S", 241);
        JAISBaL.supplementaryPages = new ArrayList<>();
        JAISBaL.constantPages = new ArrayList<>();
        JAISBaL.supplementaryPages.add(JAISBaLCharPage.of("A", 256));
        JAISBaL.supplementaryPages.add(JAISBaLCharPage.of("B", 256));
        JAISBaL.supplementaryPages.add(JAISBaLCharPage.of("C", 256));
        JAISBaL.supplementaryPages.add(JAISBaLCharPage.of("D", 256));
        JAISBaL.supplementaryPages.add(JAISBaLCharPage.of("E", 256));
        JAISBaL.supplementaryPages.add(JAISBaLCharPage.of("F", 256));
        JAISBaL.supplementaryPages.add(JAISBaLCharPage.of("G", 256));
        JAISBaL.supplementaryPages.add(JAISBaLCharPage.of("H", 256));
        JAISBaL.supplementaryPages.add(JAISBaLCharPage.of("I", 256));
        JAISBaL.supplementaryPages.add(JAISBaLCharPage.of("J", 256));
        JAISBaL.supplementaryPages.add(JAISBaLCharPage.of("K", 256));
        JAISBaL.constantPages.add(JAISBaLCharPage.of("L", 256));
        JAISBaL.constantPages.add(JAISBaLCharPage.of("M", 256));
        JAISBaLCharset.init();

        InstructionRegistry.checkNumbers();

        char[] root = JAISBaL.rootPage.getMappings();
        int id = 0;
        for (Instruction instruction : InstructionRegistry.getStandardInstructions().stream().filter(i -> i.getId() == '\0').collect(Collectors.toList())) {
            do {
                id++;
            } while (!JAISBaL.isAllowable(root[id]));
            instruction.assignId(root[id]);
        }

        List<JAISBaLCharPage> sup = JAISBaL.getSupplementaryPages();
        id = 0;
        int p = 0;
        for (Instruction instruction : InstructionRegistry.getSupplementaryInstructions().stream().filter(i -> i.getId() == '\0').collect(Collectors.toList())) {
            do {
                id++;
                if (sup.get(p).getMappings().length <= id) {
                    p++;
                    id = 0;
                }
            } while (!JAISBaL.isAllowable(sup.get(p).getMappings()[id]));
            instruction.assignId(sup.get(p).getMappings()[id]);
        }

        List<JAISBaLCharPage> consts = JAISBaL.getConstantPages();
        id = 0;
        p = 0;
        for(ConstantInstruction instruction : InstructionRegistry.getConstants().stream().filter(i -> i.getId() == '\0').collect(Collectors.toList())) {
            do {
                id++;
                if(sup.get(p).getMappings().length <= id) {
                    p++;
                    id = 0;
                }
            } while (!JAISBaL.isAllowable(consts.get(p).getMappings()[id]));
            instruction.assignId(consts.get(p).getMappings()[id]);
        }

        InstructionRegistry.checkDuplicates();
        InstructionRegistry.checkIds();
    }



    private static boolean isAllowable(char c) {
        return Program.CONTROL_CHARACTERS.indexOf(c) == -1 && !InstructionRegistry.getAccessibleInstructions().stream().filter(z -> z.getId() == c).findFirst().isPresent();
    }

    public static void generateCodePages() throws IOException {
        JAISBaL.generateInstructionsSequence("Monospaced.plain");
        BufferedReader reader = new BufferedReader(new FileReader(new File("generated-instructions.txt")));
        String names = "SABCDEFGHIJKLMNOPQRSTUV";
        for (int i = 0; i < 13; i++) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("src/main/resources/page-" + names.charAt(i) + ".txt")), StandardCharsets.UTF_8));
            int z = i != 0 ? 255 : 240;
            for (int j = 0; j <= z; j++) {
                int g = reader.read();
                writer.write(g);
            }
            writer.close();
        }
    }

    public static void displayFonts() {
        GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] fonts = e.getAllFonts();
        for (Font f : fonts) {
            JAISBaL.getOut().println(f.getName());
        }
    }

    public static void generateInstructionsSequence(String fontName) throws IOException {
        Font font = Stream.of(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()).filter(f -> f.getName().equals(fontName)).findFirst().get();
        StringBuilder builder = new StringBuilder();

        int size = Program.CONTROL_CHARACTERS.length();
        for (char c : Program.CONTROL_CHARACTERS.toCharArray()) {
            builder.append(c);
        }
        if (size > 40) {
            throw new IllegalStateException("Greater than 40 language characters are defined");
        }
        for (int i = 0; i < 40 - size; i++) {
            builder.append("?");
        }

        InstructionRegistry.getAccessibleInstructions().forEach(instruction -> {
            instruction.getAliases().forEach(s -> {
                for (char c : s.toCharArray()) {
                    String current = builder.toString();
                    if (current.indexOf(c) == -1 && c != '\0') {
                        builder.append(c);
                    }
                }
            });
        });

        for (int i = Character.MIN_VALUE; i <= Character.MAX_VALUE; i++) {
            String current = builder.toString();
            if (StandardCharsets.ISO_8859_1.newEncoder().canEncode((char) i) && !Character.isISOControl(i) && !Character.isWhitespace(i) && current.indexOf(i) == -1 && font.canDisplay(i) && (Character.getDirectionality(i) == Character.DIRECTIONALITY_LEFT_TO_RIGHT || Character.getDirectionality(i) == Character.DIRECTIONALITY_OTHER_NEUTRALS) && Normalizer.normalize(new String(new char[]{(char) i}), Normalizer.Form.NFD).length() == 1 && font.canDisplay(i)) {
                builder.append((char) i);
            }
        }


        for (int i = Character.MIN_VALUE; i <= Character.MAX_VALUE; i++) {
            String current = builder.toString();
            if (!Character.isISOControl(i) && !Character.isWhitespace(i) && current.indexOf(i) == -1 && (Character.getDirectionality(i) == Character.DIRECTIONALITY_LEFT_TO_RIGHT || Character.getDirectionality(i) == Character.DIRECTIONALITY_OTHER_NEUTRALS) && (Character.getType(i) == Character.OTHER_SYMBOL || Character.getType(i) == Character.MATH_SYMBOL || Character.getType(i) == Character.CURRENCY_SYMBOL || Character.getType(i) == Character.MODIFIER_SYMBOL) && font.canDisplay(i) && !Character.isIdeographic(i) && !Character.isAlphabetic(i) && Normalizer.normalize(new String(new char[]{(char) i}), Normalizer.Form.NFD).length() == 1) {
                builder.append((char) i);
            }
        }

        for (int i = Character.MIN_VALUE; i <= Character.MAX_VALUE; i++) {
            String current = builder.toString();
            if (!Character.isISOControl(i) && !Character.isWhitespace(i) && current.indexOf(i) == -1 && (Character.getDirectionality(i) == Character.DIRECTIONALITY_LEFT_TO_RIGHT || Character.getDirectionality(i) == Character.DIRECTIONALITY_OTHER_NEUTRALS) && font.canDisplay(i) && !Character.isIdeographic(i) && Normalizer.normalize(new String(new char[]{(char) i}), Normalizer.Form.NFD).length() == 1) {
                builder.append((char) i);
            }
        }

        OutputStream stream = new FileOutputStream("generated-instructions.txt");
        stream.write(builder.toString().getBytes(StandardCharsets.UTF_8));
        stream.close();
    }

    public static List<String> parseInput(String s) {
        CharacterStream stream = new CharacterStream(s);
        List<String> list = new ArrayList<>();
        Escaper escaper = Program.ESCAPER;
        BracketCounter counter = new BracketCounter();
        counter.registerBrackets('[', ']');
        while (stream.hasNext()) {
            list.add(stream.nextUntil(c -> c == ',', counter, new QuotationTracker(), escaper, false));
            stream.consume(',');
        }
        return list.stream().map(JAISBaL::simplisticClean).collect(Collectors.toList());
    }

    public static String simplisticClean(String s) {
        CharacterStream stream = new CharacterStream(s);
        StringBuilder builder = new StringBuilder();
        BracketCounter counter = new BracketCounter();
        counter.registerBrackets('[', ']');
        while (stream.hasNext()) {
            String z = stream.nextUntil(Program.ESCAPER.getEscapeChar(), counter);
            builder.append(z);
            if (stream.isNext(Program.ESCAPER.getEscapeChar())) {
                char c = stream.next().get();
                if (stream.isNext(',')) {
                    builder.append(stream.next().get());
                } else {
                    builder.append(c).append(stream.hasNext() ? stream.next().get() : "");
                }
            }
        }
        return builder.toString();
    }

    public static String makeInput(String... s) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < s.length; i++) {
            String sub = s[i];
            int b = 0;
            boolean escape = false;
            for (char c : sub.toCharArray()) {
                boolean local = escape;
                if (c == '\\' && !escape) {
                    escape = true;
                } else if (c == '[' && !escape) {
                    b++;
                    builder.append("[");
                } else if (c == ']' && !escape) {
                    b--;
                    builder.append("]");
                } else if (c == ',' && b == 0 && !escape) {
                    builder.append("\\,");
                } else if (c == '"' && !escape) {
                    builder.append("\\\"");
                } else {
                    builder.append(c);
                }
                if (local) {
                    escape = false;
                }
            }

            if (i < s.length - 1) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

    public static String read(File file) throws IOException {
        StringBuilder contents = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            contents.append(line).append("\n");
        }
        return PlasmaStringUtil.cutLastChar(contents.toString());
    }

}
