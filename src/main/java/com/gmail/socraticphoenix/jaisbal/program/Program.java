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
import com.gmail.socraticphoenix.jaisbal.app.util.JAISBaLExecutionException;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Program extends PlasmaObject {
    public static final Escaper ESCAPER = new Escaper('\\', new HashMap<Character, Character>() {{
        put('[', '[');
        put(']', ']');
        put('\\', '\\');
        put('}', '}');
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
    public static final String CONTROL_CHARACTERS = ":()[]\\?snai-0123456789.#}, \r\t\n";
    public static final String COMMENT_START = "\\#";
    public static final String COMMENT_END = "#\\";
    public static final char[] IGNORE_VERBOSE = {'\r', '\t'};
    public static final char[] IGNORE = {'\n', '\r', '\t'};
    public static final char[] IGNORE_VERBOSE_SPACE = {'\r', '\t', ' '};
    public static final char[] IGNORE_SPACE = {' '};
    public static boolean displayPrompts = true;
    private Function main;
    private String content;
    private Map<String, Function> functions;
    private Map<String, Function> systemFunctions;
    private boolean verbose;

    public Program(Function main, List<Function> functions, String content, boolean verbose) throws JAISBaLExecutionException {
        this.main = main;
        this.main.setProgram(this);
        this.functions = new HashMap<>();
        this.systemFunctions = new HashMap<>();
        Program.applyMainFunctions(this.systemFunctions);
        functions.forEach(f -> {
            this.functions.put(f.getName(), f);
            f.setProgram(this);
        });
        this.content = content;
        this.verbose = verbose;
    }

    public static void applyMainFunctions(Map<String, Function> functions) throws JAISBaLExecutionException {
        try {
            String piece = new String(PlasmaFileUtil.getResourceBytes("library.isbl"), StandardCharsets.UTF_8);
            Program.parseFunctions(new CharacterStream(piece), true).stream().map(f -> Function.parse(f, true)).forEach(function -> functions.put(function.getName(), function));
        } catch (Throwable e) {
            throw new JAISBaLExecutionException("Unable to load library.isbl", e);
        }
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
                        CastableValue value = Type.easyReadValues(new CharacterStream(entered), context.getProgram());
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
                throw new JAISBaLExecutionException("Stack underflow: required at least " + needed + " parameter(s), but only " + context.getStack().size() + " were/was available on the stack");
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

    public static List<String> parseFunctions(CharacterStream stream, boolean verbose) {
        List<String> functions = new ArrayList<>();
        stream.consumeAll(Program.IGNORE);
        if (stream.isNext('(')) {
            stream.consume('(');
            BracketCounter counter = new BracketCounter();
            counter.registerBrackets('[', ']');
            while (stream.hasNext()) {
                String s = stream.nextUntil(c -> c == ',' || c == ')', counter, new QuotationTracker(), Program.ESCAPER, false);
                functions.add(Program.clean(s, verbose));
                if (stream.isNext(')')) {
                    stream.consume(')');
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
        String normalContent = program;
        boolean verbose = program.startsWith("#");
        if (verbose) {
            program = program.replaceFirst("#", "");
        }
        program = Program.clean(program, verbose);

        CharacterStream stream = new CharacterStream(program);
        stream.consumeAll(verbose ? Program.IGNORE_VERBOSE_SPACE : Program.IGNORE_SPACE);
        List<Function> functions = Program.parseFunctions(stream, verbose).stream().map(f -> Function.parse(f, verbose)).collect(Collectors.toList());
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
        Program p = new Program(Function.parse("main:" + (Type.hasTypeNext(s2) ? "" : "i") + main.toString(), verbose), functions, normalContent, verbose);
        p.parse();
        p.verify();
        return p;
    }

    public void verify() throws JAISBaLExecutionException {
        this.main.verify();

        for (Function f : this.functions.values()) {
            f.verify();
        }
    }

    public String getContent() {
        return this.content;
    }

    public String explain() throws JAISBaLExecutionException {
        StringBuilder builder = new StringBuilder();
        builder.append("# ").append(Program.COMMENT_START).append(" enable verbose parsing ").append(Program.COMMENT_END).append(System.lineSeparator());
        if (!this.functions.isEmpty()) {
            builder.append("(").append(System.lineSeparator());
            List<Function> functions = this.functions.values().stream().collect(Collectors.toList());
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
        if (!this.functions.isEmpty()) {
            builder.append("(");
            List<Function> functions = this.functions.values().stream().collect(Collectors.toList());
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
        this.main.parse(this.verbose);
        for (Function f : this.functions.values()) {
            f.parse(this.verbose);
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


    public Optional<Function> getFunction(String name) {
        if (this.functions.containsKey(name)) {
            return Optional.of(this.functions.get(name));
        } else if (this.systemFunctions.containsKey(name)) {
            return Optional.of(this.systemFunctions.get(name));
        } else {
            return Optional.empty();
        }
    }


}
