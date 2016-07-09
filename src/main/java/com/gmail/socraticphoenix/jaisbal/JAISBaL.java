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

import com.gmail.socraticphoenix.jaisbal.program.Program;
import com.gmail.socraticphoenix.jaisbal.program.instructions.Instruction;
import com.gmail.socraticphoenix.jaisbal.program.instructions.InstructionRegistry;
import com.gmail.socraticphoenix.jaisbal.encode.JAISBaLCharPage;
import com.gmail.socraticphoenix.jaisbal.encode.JAISBaLCharset;
import com.gmail.socraticphoenix.jaisbal.modes.FileMode;
import com.gmail.socraticphoenix.jaisbal.modes.GuiMode;
import com.gmail.socraticphoenix.jaisbal.modes.InputMode;
import com.gmail.socraticphoenix.jaisbal.util.DangerousConsumer;
import com.gmail.socraticphoenix.jaisbal.util.JAISBaLExecutionException;
import com.gmail.socraticphoenix.jaisbal.util.StringNumberCaster;
import com.gmail.socraticphoenix.plasma.file.jlsc.JLSCArray;
import com.gmail.socraticphoenix.plasma.file.jlsc.JLSCException;
import com.gmail.socraticphoenix.plasma.file.jlsc.JLSConfiguration;
import com.gmail.socraticphoenix.plasma.file.jlsc.io.JLSCReader;
import com.gmail.socraticphoenix.plasma.file.stream.WritableInputStream;
import com.gmail.socraticphoenix.plasma.reflection.util.PlasmaReflectionUtil;
import com.gmail.socraticphoenix.plasma.string.PlasmaStringUtil;

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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JAISBaL {
    private static Scanner inScanner;

    private static JAISBaLCharPage rootPage;
    private static List<JAISBaLCharPage> supplementaryPages;
    private static List<JAISBaLCharPage> constantPages;

    public static void main(String[] a) throws IOException, JAISBaLExecutionException {
        JAISBaL.generalInit();
        Map<String, String> args = new HashMap<>();
        args.put("immediate-quit", "false");
        for (String s : a) {
            String[] pieces = s.split("(:|=)", 2);
            if (pieces.length != 2) {
                System.out.println("Unrecognized argument \"" + s + "\" no = or : name separator was found.");
                args.put("immediate-quit", "true");
                break;
            } else {
                args.put(pieces[0], pieces[1]);
            }
        }

        if (!args.get("immediate-quit").equals("true")) {
            if (args.containsKey("dev")) {
                String action = args.get("dev");
                switch (action) {
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
                        System.out.println("Unknown dev action \"" + action + "\"");
                }
            } else {
                JAISBaL.charsetInit();
                Map<String, String> defaults = JAISBaL.getDefaultArgs();
                defaults.entrySet().stream().filter(e -> !args.containsKey(e.getKey())).forEach(e -> args.put(e.getKey(), e.getValue()));
                Map<String, DangerousConsumer<Map<String, String>>> modes = JAISBaL.modes();

                if (args.containsKey("input")) {
                    Program.displayPrompts = false;
                    WritableInputStream stream = new WritableInputStream();
                    System.setIn(stream);
                    try {
                        JLSCArray input = JLSCReader.readArray(args.get("input"));
                        input.forEach(value -> stream.write(String.valueOf(value.getValue().orElse(null)) + System.lineSeparator()));
                    } catch (JLSCException e) {
                        throw new JAISBaLExecutionException("Unable to read input \"" + args.get("input") + "\"", e);
                    }
                } else if (args.containsKey("input-file")) {
                    Program.displayPrompts = false;
                    File input = new File(args.get("input-file"));
                    if (input.exists()) {
                        try {
                            WritableInputStream stream = new WritableInputStream();
                            System.setIn(stream);
                            JLSConfiguration conf = JLSConfiguration.fromFile(input);
                            JLSCArray array = conf.getOrSetArray("input");
                            array.forEach(value -> {
                                if (value.getAsArray().isPresent()) {
                                    value.getAsArray().get().forEach(val -> stream.write(String.valueOf(val.getValue().orElse(null)) + System.lineSeparator()));
                                } else {
                                    stream.write(String.valueOf(value.getValue().orElse(null)));
                                }
                            });
                        } catch (JLSCException e) {
                            throw new JAISBaLExecutionException("Unable to read input file \"" + input.getAbsolutePath() + "\"", e);
                        }
                    } else {
                        System.out.println("Couldn't find file \"" + input.getAbsolutePath() + "\"");
                        return;
                    }
                }

                JAISBaL.createInScanner();
                String mode = args.get("mode");
                if (modes.containsKey(mode)) {
                    modes.get(mode).accept(args);
                } else {
                    System.out.println("Unknown mode \"" + mode + "\"");
                }
            }
        }
    }

    public static void generateSpecFile() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("generated-spec.txt"));
        writer.write("For the sake of convenience, a is used to refer to the top value of the stack, b is used to refer to the second value on the stack, c is used to refer to the third value on the stack, and so on.");
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
            System.out.println("JAISBaL bytes: " + source.getBytes(JAISBaLCharset.getCharset()).length);
            System.out.println("UTF-8 bytes: " + source.getBytes(StandardCharsets.UTF_8).length);
        } else {
            char bad = '\0';
            for(char c : source.toCharArray()) {
                if(!JAISBaLCharset.getCharset().newEncoder().canEncode(c)) {
                    bad = c;
                    break;
                }
            }
            System.out.println("JAISBaL bytes: error: unsupported character '" + PlasmaStringUtil.escape(String.valueOf(bad)) + "'");
            System.out.println("UTF-8 bytes: " + source.getBytes(StandardCharsets.UTF_8).length);
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
        list.addAll(JAISBaL.getSupplementaryPages());
        list.addAll(JAISBaL.getConstantPages());
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
        JAISBaL.rootPage = JAISBaLCharPage.of("S", 250);
        JAISBaL.supplementaryPages = new ArrayList<>();
        JAISBaL.constantPages = new ArrayList<>();
        JAISBaL.supplementaryPages.add(JAISBaLCharPage.of("A", 256));
        JAISBaL.supplementaryPages.add(JAISBaLCharPage.of("B", 256));
        JAISBaL.supplementaryPages.add(JAISBaLCharPage.of("C", 256));
        JAISBaL.supplementaryPages.add(JAISBaLCharPage.of("D", 256));
        JAISBaL.constantPages.add(JAISBaLCharPage.of("E", 256));
        JAISBaL.constantPages.add(JAISBaLCharPage.of("F", 256));

        JAISBaLCharset.init();

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
                }
            } while (!JAISBaL.isAllowable(sup.get(p).getMappings()[id]));
            instruction.assignId(sup.get(p).getMappings()[id]);
        }

        JAISBaL.checkDuplicates();
    }

    public static void createInScanner() {
        if (JAISBaL.inScanner == null) {
            JAISBaL.inScanner = new Scanner(System.in);
        }
    }

    private static void checkDuplicates() {
        List<Instruction> accessible = InstructionRegistry.getAccessibleInstructions();
        for (int i = 0; i < accessible.size(); i++) {
            for (int j = 0; j < accessible.size(); j++) {
                Instruction a = accessible.get(i);
                Instruction b = accessible.get(j);
                Optional<String> stringOptional = a.getAliases().stream().filter(b.getAliases()::contains).findFirst();
                if (i != j && stringOptional.isPresent()) {
                    throw new IllegalStateException("Duplicate alias \"" + stringOptional.get() + "\" for instructions " + accessible.get(i).getMainAlias() + " and " + accessible.get(j).getMainAlias());
                }
            }
        }
    }

    private static boolean isAllowable(char c) {
        return Program.CONTROL_CHARACTERS.indexOf(c) == -1 && !InstructionRegistry.getAccessibleInstructions().stream().filter(z -> z.getId() == c).findFirst().isPresent();
    }

    public static void generateCodePages() throws IOException {
        JAISBaL.generateInstructionsSequence("Monospaced.plain");
        BufferedReader reader = new BufferedReader(new FileReader(new File("instructions.txt")));
        String names = "SABCDEF";
        for (int i = 0; i < 7; i++) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("src/main/resources/page-" + names.charAt(i) + ".txt")), StandardCharsets.UTF_8));
            int z = i != 0 ? 255 : 249;
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
            System.out.println(f.getName());
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
            throw new IllegalStateException("Greater than 20 language characters are defined");
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
            if (StandardCharsets.ISO_8859_1.newEncoder().canEncode((char) i) && !Character.isISOControl(i) && !Character.isWhitespace(i) && current.indexOf(i) == -1 && Character.getDirectionality(i) == Character.DIRECTIONALITY_LEFT_TO_RIGHT) {
                builder.append((char) i);
            }
        }


        for (int i = Character.MAX_VALUE; i >= Character.MIN_VALUE; i--) {
            String current = builder.toString();
            if (!Character.isISOControl(i) && !Character.isWhitespace(i) && current.indexOf(i) == -1 && Character.getDirectionality(i) == Character.DIRECTIONALITY_LEFT_TO_RIGHT && font.canDisplay(i) && !Character.isIdeographic(i)) {
                builder.append((char) i);
            }
        }

        OutputStream stream = new FileOutputStream("instructions.txt");
        stream.write(builder.toString().getBytes(StandardCharsets.UTF_8));
        stream.close();
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

    public static Scanner getInScanner() {
        return JAISBaL.inScanner;
    }
}
