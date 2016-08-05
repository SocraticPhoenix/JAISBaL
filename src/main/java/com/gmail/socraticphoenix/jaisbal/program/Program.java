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
package com.gmail.socraticphoenix.jaisbal.program;

import com.gmail.socraticphoenix.jaisbal.JAISBaL;
import com.gmail.socraticphoenix.jaisbal.program.function.Function;
import com.gmail.socraticphoenix.jaisbal.program.function.FunctionContext;
import com.gmail.socraticphoenix.plasma.base.PlasmaObject;
import com.gmail.socraticphoenix.plasma.file.PlasmaFileUtil;
import com.gmail.socraticphoenix.plasma.reflection.CastableValue;
import com.gmail.socraticphoenix.plasma.string.BracketCounter;
import com.gmail.socraticphoenix.plasma.string.CharacterStream;
import com.gmail.socraticphoenix.plasma.string.CommentRemover;
import com.gmail.socraticphoenix.plasma.string.Escaper;
import com.gmail.socraticphoenix.plasma.string.QuotationTracker;
import com.gmail.socraticphoenix.plasma.string.StringParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

public class Program extends PlasmaObject {
    public static final Escaper ESCAPER = new Escaper('\\', new HashMap<Character, Character>() {{
        put('[', '[');
        put(']', ']');
        put('\\', '\\');
        put('}', '}');
        put('(', '(');
        put(')', ')');
        put(',', ',');
    }});

    /*
     * :            Function name separator
     * ()           Function block
     * []           Value / value array
     * \            Escape character
     * ?            Wildcard type
     * i            Implicit type (enabled in main by default)
     * s            String type
     * n            Number type
     * a            Array type
     * -0123456789. Digits/Number parts
     * #            Must be first character of file. Enables verbose parsing. Also used in comments
     * }            Value terminator
     * ,            Function separator
     */
    public static final String CONTROL_CHARACTERS = ":()[]{}\u2985\u2986\\?snai-0123456789.#, \r\t\n";
    public static final String COMMENT_START = "\\#";
    public static final String COMMENT_END = "#\\";
    public static final char[] IGNORE_VERBOSE = {'\r', '\t', '\0'};
    public static final char[] IGNORE = {'\n', '\r', '\t', '\0'};
    public static final char[] IGNORE_VERBOSE_SPACE = {'\r', '\t', '\0', ' '};
    public static final char[] IGNORE_SPACE = {'\n', '\r', '\t', '\0', ' '};
    public static boolean displayPrompts = true;
    private Function main;
    private String content;
    private Map<String, Function> functions;
    private Map<String, Function> snippets;
    private List<String> imported;
    private boolean verbose;

    public Program(Function main, List<Function> functions, List<Function> snippets, String content, boolean verbose, boolean importSystem) throws JAISBaLExecutionException {
        this.main = main;
        this.imported = new ArrayList<>();
        this.main.setProgram(this);
        this.functions = new LinkedHashMap<>();
        this.snippets = new LinkedHashMap<>();
        if (functions.stream().filter(f -> f.getName().equals("main")).findFirst().isPresent()) {
            throw new JAISBaLExecutionException("Invalid state: main is a reserved function name");
        }
        snippets.forEach(f -> {
            this.snippets.put(f.getName(), f);
            f.setProgram(this);
        });
        functions.forEach(f -> {
            this.functions.put(f.getName(), f);
            f.setProgram(this);
        });
        this.functions.put("main", main);
        this.content = content;
        this.verbose = verbose;
        if (importSystem) {
            try {
                this.$import("system", new String(PlasmaFileUtil.getResourceBytes("system.isbl"), StandardCharsets.UTF_8), false);
            } catch (Throwable e) {
                throw new JAISBaLExecutionException("Invalid state: error while importing system library", e);
            }
        }
    }

    public Program(Function main, List<Function> functions, List<Function> snippets, String content, boolean verbose) throws JAISBaLExecutionException {
        this(main, functions, snippets, content, verbose, true);
    }

    public static String valueToString(CastableValue value) {
        return FunctionContext.valueToString(value);
    }

    public static void checkUnderflow(int needed, FunctionContext context) throws JAISBaLExecutionException {
        if (context.getStack().size() < needed) {
            if (context.isImplicitInput()) {
                while (context.getStack().size() < needed) {
                    Type type = Type.WILDCARD;
                    JAISBaL.getOut().print("Enter a " + type.getName() + " > ");
                    String entered = JAISBaL.getIn().get();
                    try {
                        CastableValue value = Type.easyReadValues(new CharacterStream(entered));
                        if (type.matches(value)) {
                            context.getStack().push(value);
                        } else {
                            JAISBaL.getOut().println("Invalid value: " + Program.valueToString(value) + " cannot be converted to " + type.getName());
                        }
                    } catch (StringParseException e) {
                        JAISBaL.getOut().println("Invalid value:");
                        e.printStackTrace(JAISBaL.getOut());
                    }
                }
            } else {
                throw new JAISBaLExecutionException("Invalid State: stack underflow, required at least " + needed + " parameter(s), but only " + context.getStack().size() + " were/was available on the stack");
            }
        }
    }

    public static String clean(String program, boolean verbose) {
        program = program.replaceAll("\r", "\n").replaceAll(System.lineSeparator(), "\n");
        while (program.contains("\n\n")) {
            program = program.replaceAll("\n\n", "\n");
        }
        CommentRemover remover = new CommentRemover(Program.COMMENT_START, Program.COMMENT_END);
        StringBuilder real = new StringBuilder();
        CharacterStream stream = new CharacterStream(program);
        stream.consumeAll(verbose ? Program.IGNORE_VERBOSE : Program.IGNORE);
        BracketCounter counter = new BracketCounter();
        counter.registerBrackets('[', ']');
        while (stream.hasNext()) {
            real.append(stream.nextUntil(counter, verbose ? Program.IGNORE_VERBOSE : Program.IGNORE));
            stream.consumeAll(verbose ? Program.IGNORE_VERBOSE : Program.IGNORE);
        }
        return remover.clean(real.toString());
    }

    public static boolean only(String s, char... c) {
        for (char z : s.toCharArray()) {
            if (!Program.contains(z, c)) {
                return false;
            }
        }
        return true;
    }

    public static List<String> parseFunctions(CharacterStream stream, boolean verbose, char left, char right) {
        List<String> functions = new ArrayList<>();
        stream.consumeAll(Program.IGNORE_SPACE);
        if (stream.isNext(left)) {
            stream.consume(left);
            stream.consumeAll(Program.IGNORE_SPACE);
            BracketCounter counter = new BracketCounter();
            counter.registerBrackets('[', ']');
            while (stream.hasNext()) {
                String s = stream.nextUntil(c -> c == ',' || c == right, counter, new QuotationTracker(), Program.ESCAPER, false);
                functions.add(Program.clean(s, verbose));
                if (stream.isNext(right)) {
                    stream.consume(right);
                    stream.consumeAll(Program.IGNORE_SPACE);
                    break;
                } else {
                    stream.consumeAll(',');
                }
            }
        }
        return functions;
    }

    public static boolean contains(char c, char... chars) {
        for (char z : chars) {
            if (z == c) {
                return true;
            }
        }
        return false;
    }

    public static Program parse(String program) throws StringParseException, JAISBaLExecutionException {
        return Program.parse(program, true);
    }

    public static Program parse(String program, boolean importSystem) throws StringParseException, JAISBaLExecutionException {
        String normalContent = program;
        boolean verbose = program.startsWith("#");
        if (verbose) {
            program = program.replaceFirst("#", "");
        }
        program = Program.clean(program, verbose);

        CharacterStream stream = new CharacterStream(program);
        stream.consumeAll(verbose ? Program.IGNORE_VERBOSE_SPACE : Program.IGNORE_SPACE);
        List<Function> snippets = new ArrayList<>();
        for(String f : Program.parseFunctions(stream, verbose, '\u2985', '\u2986')) {
            snippets.add(Function.parse(f, verbose));
        }
        stream.consumeAll(verbose ? Program.IGNORE_VERBOSE_SPACE : Program.IGNORE_SPACE);
        List<Function> functions = new ArrayList<>();
        for (String f : Program.parseFunctions(stream, verbose, '(', ')')) {
            functions.add(Function.parse(f, verbose));
        }
        StringBuilder main = new StringBuilder();
        BracketCounter counter = new BracketCounter();
        counter.registerBrackets('[', ']');

        while (stream.hasNext()) {
            char c = stream.next().get();
            if (counter.isCounting(c)) {
                counter.consider(c);
            }

            if (!counter.isBalanced() || !Program.contains(c, verbose ? Program.IGNORE_VERBOSE : Program.IGNORE)) {
                main.append(c);
            }
        }

        CharacterStream s2 = new CharacterStream(main.toString());
        s2.consumeAll(Program.IGNORE);
        Program p = new Program(Function.parse("main:" + (Type.hasTypeNext(s2) ? "" : "i") + main.toString(), verbose), functions, snippets, normalContent, verbose, importSystem);
        p.parse();
        p.verify();
        p.prep();
        return p;
    }

    public boolean isVerbose() {
        return this.verbose;
    }

    public void $import(String name, String content) throws JAISBaLExecutionException {
        this.$import(name, content, true);
    }

    public List<String> getImported() {
        return this.imported;
    }

    public void $import(String name, String content, boolean importSystem) throws JAISBaLExecutionException {
        if (!this.imported.contains(name)) {
            try {
                Program parsed = Program.parse(content, importSystem);
                this.functions.put(name + ".main", parsed.getMain());
                parsed.getFunctions().entrySet().stream().filter(f -> !f.getKey().contains(".")).forEach(f -> this.functions.put(name + "." + f.getKey(), f.getValue()));
                parsed.getSnippets().entrySet().stream().filter(f -> !f.getKey().contains(".")).forEach(f -> this.snippets.put(name + "." + f.getKey(), f.getValue()));
                this.imported.add(name);
            } catch (Throwable e) {
                throw new JAISBaLExecutionException("Invalid state: error while importing \"" + name + "\"", e);
            }
        }
    }

    public void prep() throws JAISBaLExecutionException {
        if (this.functions.containsKey("prep")) {
            Function function = this.functions.get("prep");
            if (function.getParameters().size() == 0) {
                function.run(new Stack<>());
            } else {
                throw new JAISBaLExecutionException("Invalid state: prep method may not have arguments");
            }
        }
    }

    public Map<String, Function> getSnippets() {
        return this.snippets;
    }

    public Map<String, Function> getFunctions() {
        return this.functions;
    }

    public void verify() throws JAISBaLExecutionException {
        this.main.verify();
        for (Function f : this.functions.values()) {
            f.verify();
        }
        for(Function s : this.snippets.values()) {
            if(s.getParameters().size() != 0) {
                throw new JAISBaLExecutionException("Invalid state: snippets may not have arguments");
            }
            s.verify();
        }
    }

    public String getContent() {
        return this.content;
    }

    public String explain() throws JAISBaLExecutionException {
        StringBuilder builder = new StringBuilder();
        builder.append("# ").append(Program.COMMENT_START).append(" enable verbose parsing ").append(Program.COMMENT_END).append(System.lineSeparator());
        if (!this.snippets.entrySet().stream().filter(e -> !e.getKey().contains(".")).filter(e -> !e.getValue().getName().equals("main")).map(Map.Entry::getValue).collect(Collectors.toList()).isEmpty()) {
            builder.append("\u2985").append(System.lineSeparator());
            List<Function> functions = this.snippets.entrySet().stream().filter(e -> !e.getKey().contains(".")).filter(e -> !e.getValue().getName().equals("main")).map(Map.Entry::getValue).collect(Collectors.toList());
            for (int i = 0; i < functions.size(); i++) {
                Function f = functions.get(i);
                builder.append(f.getName()).append(":").append(System.lineSeparator()).append(f.explain(1));
                if (i < functions.size() - 1) {
                    builder.append(",").append(System.lineSeparator());

                }
            }
            builder.append("\u2986").append(System.lineSeparator());
        }
        if (!this.functions.entrySet().stream().filter(e -> !e.getKey().contains(".")).filter(e -> !e.getValue().getName().equals("main")).map(Map.Entry::getValue).collect(Collectors.toList()).isEmpty()) {
            builder.append("(").append(System.lineSeparator());
            List<Function> functions = this.functions.entrySet().stream().filter(e -> !e.getKey().contains(".")).filter(e -> !e.getValue().getName().equals("main")).map(Map.Entry::getValue).collect(Collectors.toList());
            for (int i = 0; i < functions.size(); i++) {
                Function f = functions.get(i);
                builder.append(f.getName()).append(":").append(System.lineSeparator()).append(f.explain(1));
                if (i < functions.size() - 1) {
                    builder.append(",").append(System.lineSeparator());

                }
            }
            builder.append(")").append(System.lineSeparator());
        }
        return builder.append(this.getMain().explain(0)).toString();
    }

    public String minify() throws JAISBaLExecutionException {
        StringBuilder builder = new StringBuilder();
        if (!this.snippets.entrySet().stream().filter(e -> !e.getKey().contains(".")).filter(e -> !e.getValue().getName().equals("main")).map(Map.Entry::getValue).collect(Collectors.toList()).isEmpty()) {
            builder.append("\u2985");
            List<Function> functions = this.snippets.entrySet().stream().filter(e -> !e.getKey().contains(".")).filter(e -> !e.getValue().getName().equals("main")).map(Map.Entry::getValue).collect(Collectors.toList());
            for (int i = 0; i < functions.size(); i++) {
                Function f = functions.get(i);
                builder.append(f.getName()).append(f.getName().length() == 1 ? "" : ":").append(f.minify(false));
                if (i < functions.size() - 1) {
                    builder.append(",");

                }
            }
            builder.append("\u2986");
        }
        if (!this.functions.entrySet().stream().filter(e -> !e.getKey().contains(".")).filter(e -> !e.getValue().getName().equals("main")).map(Map.Entry::getValue).collect(Collectors.toList()).isEmpty()) {
            builder.append("(");
            List<Function> functions = this.functions.entrySet().stream().filter(e -> !e.getKey().contains(".")).filter(e -> !e.getValue().getName().equals("main")).map(Map.Entry::getValue).collect(Collectors.toList());
            for (int i = 0; i < functions.size(); i++) {
                Function f = functions.get(i);
                builder.append(f.getName()).append(f.getName().length() == 1 ? "" : ":").append(f.minify(false));
                if (i < functions.size() - 1) {
                    builder.append(",");

                }
            }
            builder.append(")");
        }
        return builder.append(this.getMain().minify(true)).toString();
    }

    public void parse() throws JAISBaLExecutionException {
        this.main.parse();
        for (Function f : this.functions.values()) {
            f.parse();
        }
        for(Function s : this.snippets.values()) {
            s.parse();
        }
    }

    public String toString() {
        return this.content;
    }

    public void run() throws JAISBaLExecutionException, IOException {
        this.getMain().runAsMain();
    }

    public Function getMain() {
        return this.main;
    }

    public Optional<Function> getSnippet(String name) {
        if (this.snippets.containsKey(name)) {
            return Optional.of(this.snippets.get(name));
        } else {
            return Optional.empty();
        }
    }

    public Optional<Function> getFunction(String name) {
        if (this.functions.containsKey(name)) {
            return Optional.of(this.functions.get(name));
        } else {
            return Optional.empty();
        }
    }


}
