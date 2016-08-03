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
package com.gmail.socraticphoenix.jaisbal.program.function;

import com.gmail.socraticphoenix.jaisbal.app.util.JAISBaLExecutionException;
import com.gmail.socraticphoenix.jaisbal.program.Program;
import com.gmail.socraticphoenix.jaisbal.program.State;
import com.gmail.socraticphoenix.jaisbal.program.Type;
import com.gmail.socraticphoenix.jaisbal.program.instructions.Instruction;
import com.gmail.socraticphoenix.jaisbal.program.instructions.InstructionRegistry;
import com.gmail.socraticphoenix.plasma.base.PlasmaObject;
import com.gmail.socraticphoenix.plasma.reflection.CastableValue;
import com.gmail.socraticphoenix.plasma.string.BracketCounter;
import com.gmail.socraticphoenix.plasma.string.CharacterStream;
import com.gmail.socraticphoenix.plasma.string.StringParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Predicate;

public class Function extends PlasmaObject {
    private Program program;
    private String name;
    private String content;
    private List<String> instructions;
    private List<Type> parameters;
    private boolean implicitInput;

    public Function(String name, String content, List<Type> parameters, boolean implicitInput) {
        this.name = name;
        this.content = content;
        this.parameters = parameters;
        this.implicitInput = implicitInput;
    }

    public static List<String> instructions(String s) throws JAISBaLExecutionException {
        List<String> instructions = new ArrayList<>();
        CharacterStream stream = new CharacterStream(s);
        BracketCounter counter = new BracketCounter();
        counter.registerBrackets('[', ']');
        while (stream.hasNext()) {
            char c = stream.next().get();
            Optional<Instruction> instructionOptional = InstructionRegistry.getAccessibleInstructions().stream().filter(e -> e.getId() == c).findFirst();
            if (instructionOptional.isPresent()) {
                Instruction instruction = instructionOptional.get();
                StringBuilder builder = new StringBuilder();
                builder.append(instruction.getId());
                String val = null;
                try {
                    val = instruction.getValueReader().apply(stream);
                } catch (JAISBaLExecutionException e) {
                    throw e;
                } catch (Throwable throwable) {
                    throw new JAISBaLExecutionException("Error while reading value ", throwable);
                }
                if (val != null && !val.equals("")) {
                    builder.append(" ").append(val);
                }
                instructions.add(builder.toString());
            } else {
                throw new JAISBaLExecutionException("No instructions called " + c);
            }
        }
        return instructions;
    }

    public static List<String> verboseInstructions(String s) throws JAISBaLExecutionException {
        List<String> instructions = new ArrayList<>();
        CharacterStream stream = new CharacterStream(s);
        while (stream.hasNext()) {
            instructions.add(stream.nextUntil('\n').trim());
            stream.consumeAll('\n');
            stream.consumeAll(' ');
        }
        return instructions;
    }

    public static boolean exists(String name) {
        return InstructionRegistry.getAccessibleInstructions().stream().filter(entry -> entry.isName(name)).findFirst().isPresent();
    }

    public static Function parse(String s, boolean verbose) throws StringParseException {
        CharacterStream stream = new CharacterStream(s);
        if (!stream.hasNext()) {
            throw stream.syntaxError("Expected function");
        } else {
            stream.consumeAll(Program.IGNORE_SPACE);
            List<Type> parameters = new ArrayList<>();
            String name;
            if (s.indexOf(':') > 0 && s.indexOf(':') - 1 != s.indexOf('`')) {
                if (s.indexOf(':') == 0) {
                    throw stream.syntaxError("Expected function name");
                } else {
                    StringBuilder nameB = new StringBuilder();
                    while (stream.hasNext()) {
                        String piece = stream.nextUntil((Predicate<Character>) c -> c == ':' || new String(verbose ? Program.IGNORE_VERBOSE_SPACE : Program.IGNORE_SPACE).indexOf(c) > 0);
                        nameB.append(piece);
                        stream.consumeAll(verbose ? Program.IGNORE_VERBOSE_SPACE : Program.IGNORE_SPACE);
                        if (stream.isNext(':')) {
                            stream.consume(':');
                            break;
                        }
                    }
                    name = nameB.toString();
                }
            } else {
                name = String.valueOf(stream.next().get());
            }

            stream.consumeAll(Program.IGNORE);

            while (Type.hasTypeNext(stream)) {
                parameters.add(Type.read(stream));
                stream.consumeAll(Program.IGNORE);
            }

            stream.consumeAll(Program.IGNORE);
            if (stream.isNext('}')) {
                stream.consumeAll('}');
            }
            stream.consumeAll(Program.IGNORE);

            return new Function(name, stream.remaining(), parameters, parameters.stream().filter(Type::isImplicit).findFirst().isPresent());
        }
    }

    public void parse(boolean verbose) throws JAISBaLExecutionException {
        if (verbose) {
            this.instructions = Function.verboseInstructions(this.getContent());
        } else {
            this.instructions = Function.instructions(this.getContent());
        }
    }

    public String explain(int indent) throws JAISBaLExecutionException {
        return this.createContext().explain(indent);
    }

    public String minify(boolean main) throws JAISBaLExecutionException {
        return this.createContext().minify(main);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.name).append(this.name.length() == 1 ? "" : ":");
        this.parameters.forEach(builder::append);
        return builder.append(this.content).toString();
    }

    public Program getProgram() {
        return this.program;
    }

    public void setProgram(Program program) {
        this.program = program;
    }

    public void runAsMain() throws JAISBaLExecutionException, IOException {
        this.createContext().runAsMain();
    }

    public State run(Stack<CastableValue> parent) throws JAISBaLExecutionException {
        return this.createContext().run(parent);
    }

    public List<Type> getParameters() {
        return this.parameters;
    }

    public String getName() {
        return this.name;
    }

    public String getContent() {
        return this.content;
    }

    public FunctionContext createContext() throws JAISBaLExecutionException {
        return new FunctionContext(this, this.program);
    }

    public List<String> getInstructions() {
        return this.instructions;
    }

    public boolean isImplicitInput() {
        return this.implicitInput;
    }

    public void verify() throws JAISBaLExecutionException {
        this.createContext().verify();
    }
}
