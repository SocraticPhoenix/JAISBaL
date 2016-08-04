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

import com.gmail.socraticphoenix.jaisbal.JAISBaL;
import com.gmail.socraticphoenix.jaisbal.app.util.JAISBaLExecutionException;
import com.gmail.socraticphoenix.jaisbal.program.Program;
import com.gmail.socraticphoenix.jaisbal.program.State;
import com.gmail.socraticphoenix.jaisbal.program.Type;
import com.gmail.socraticphoenix.jaisbal.program.instructions.util.AuxiliaryConstant;
import com.gmail.socraticphoenix.jaisbal.program.instructions.util.AuxiliaryInstruction;
import com.gmail.socraticphoenix.jaisbal.program.instructions.Instruction;
import com.gmail.socraticphoenix.jaisbal.program.instructions.InstructionRegistry;
import com.gmail.socraticphoenix.plasma.base.PlasmaObject;
import com.gmail.socraticphoenix.plasma.base.Triple;
import com.gmail.socraticphoenix.plasma.collection.PlasmaListUtil;
import com.gmail.socraticphoenix.plasma.math.PlasmaMathUtil;
import com.gmail.socraticphoenix.plasma.reflection.CastableValue;
import com.gmail.socraticphoenix.plasma.string.CharacterStream;
import com.gmail.socraticphoenix.plasma.string.PlasmaStringUtil;
import com.gmail.socraticphoenix.plasma.string.StringFormat;
import com.gmail.socraticphoenix.plasma.string.StringParseException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class FunctionContext extends PlasmaObject {
    private CastableValue[] currentArg;
    private Function function;
    private List<String> instructions;
    private Stack<CastableValue> stack;
    private Stack<CastableValue> parent;
    private Map<Long, CastableValue> locals;
    private Program program;
    private int current;
    private List<Type> parameters;
    private AtomicBoolean running;

    public FunctionContext(Function function, List<Type> parameters, List<String> instructions, Stack<CastableValue> stack, Stack<CastableValue> parent, Map<Long, CastableValue> locals, Program program) {
        this.function = function;
        this.instructions = instructions;
        this.stack = stack;
        this.parent = parent;
        this.locals = locals;
        this.program = program;
        this.current = 0;
        this.parameters = parameters;
        this.running = new AtomicBoolean(true);
    }

    public FunctionContext(Function function, Program program) {
        this(function, PlasmaListUtil.looseClone(function.getParameters()), PlasmaListUtil.looseClone(function.getInstructions()), new Stack<>(), new Stack<>(), new LinkedHashMap<>(), program);
    }

    public static String valueToString(CastableValue value) {
        if (value.getValueAs(CastableValue.class).isPresent()) {
            return valueToString(value.getValueAs(CastableValue.class).get());
        }

        StringBuilder builder = new StringBuilder();
        if (value.getValueAs(Map.class).isPresent()) {
            builder.append("{");
            Map map = value.getValueAs(Map.class).get();
            int i = 0;
            Collection<Map.Entry> entries = map.entrySet();
            for (Map.Entry entry : entries) {
                builder.append(String.valueOf(entry.getKey())).append(":").append(FunctionContext.valueToString(CastableValue.of(entry.getValue())));
                if (i < entries.size() - 1) {
                    builder.append(", ");
                }
                i++;
            }
            builder.append("}");
        } else if (value.getValueAs(CastableValue[].class).isPresent()) {
            builder.append("[");
            CastableValue[] array = value.getValueAs(CastableValue[].class).get();
            for (int i = 0; i < array.length; i++) {
                CastableValue val = array[i];
                if (val != null && val.getValue().isPresent()) {
                    builder.append(FunctionContext.valueToString(array[i]));
                    if (i < array.length - 1 && array[i + 1] != null && array[i + 1].getValue().isPresent()) {
                        builder.append(", ");
                    }
                }
            }
            builder.append("]");
        } else {
            boolean str = !value.getValueAs(BigDecimal.class).isPresent();
            builder.append(str ? String.valueOf(value.getValue().orElse(null)) : value.getValueAs(BigDecimal.class).get().toPlainString());
        }
        return builder.toString();
    }

    public static State run(FunctionContext context, int end, Predicate<FunctionContext> transmitJump) throws JAISBaLExecutionException {
        while (context.currentExists() && context.running.get() && context.getCurrent() <= end) {
            String instruction = context.getCurrentAndStep();
            try {
                if (!instruction.equals("")) {
                    String name;
                    String arg;
                    String[] pieces = instruction.split(" ", 2);
                    if (pieces.length == 1) {
                        name = pieces[0];
                        arg = "";
                    } else {
                        name = pieces[0];
                        arg = pieces[1];
                    }
                    if (InstructionRegistry.getAccessibleInstructions().stream().filter(e -> e.isName(name)).findFirst().isPresent()) {
                        Instruction entry = InstructionRegistry.getAccessibleInstructions().stream().filter(e -> e.isName(name)).findFirst().get();
                        context.currentArg = Type.readValues(new CharacterStream(arg));
                        State state = entry.getAction().apply(context);
                        if (state.isTransmit() || (state == State.JUMPED && transmitJump.test(context))) {
                            return state;
                        }
                    } else {
                        throw new JAISBaLExecutionException("No applicable instruction found");
                    }
                }
            } catch (Throwable e) {
                throw new JAISBaLExecutionException("Error while executing instruction: " + instruction, e);
            }
        }
        return State.NORMAL;
    }

    public static State run(FunctionContext context) throws JAISBaLExecutionException {
        while (context.currentExists() && context.running.get()) {
            String instruction = context.getCurrentAndStep();
            try {
                if (!instruction.equals("")) {
                    String name;
                    String arg;
                    String[] pieces = instruction.split(" ", 2);
                    if (pieces.length == 1) {
                        name = pieces[0];
                        arg = "";
                    } else {
                        name = pieces[0];
                        arg = pieces[1];
                    }
                    if (InstructionRegistry.getAccessibleInstructions().stream().filter(e -> e.isName(name)).findFirst().isPresent()) {
                        Instruction entry = InstructionRegistry.getAccessibleInstructions().stream().filter(e -> e.isName(name)).findFirst().get();
                        context.currentArg = Type.readValues(new CharacterStream(arg));
                        State state = entry.getAction().apply(context);
                        if (state.isTransmit()) {
                            return State.NORMAL;
                        }
                    } else {
                        throw new JAISBaLExecutionException("No applicable instruction found");
                    }
                }
            } catch (Throwable e) {
                throw new JAISBaLExecutionException("Error while executing instruction: " + instruction, e);
            }
        }
        return State.NORMAL;
    }

    public static String explain(FunctionContext context, int indent) throws JAISBaLExecutionException {
        List<Triple<Instruction, String, String>> preProcessed = new ArrayList<>();
        while (context.currentExists()) {
            String instruction = context.getCurrentAndStep();
            if (!instruction.equals("")) {
                String name;
                String arg;
                String[] pieces = instruction.split(" ", 2);
                if (pieces.length == 1) {
                    name = pieces[0];
                    arg = "";
                } else {
                    name = pieces[0];
                    arg = pieces[1];
                }

                if (InstructionRegistry.getAccessibleInstructions().stream().filter(e -> e.isName(name)).findFirst().isPresent()) {
                    Instruction entry = InstructionRegistry.getAccessibleInstructions().stream().filter(e -> e.isName(name)).findFirst().get();
                    if (entry.getAliases().stream().filter(InstructionRegistry.getBlockEnds()::contains).findFirst().isPresent()) {
                        indent--;
                    }
                    preProcessed.add(Triple.of(entry, arg, PlasmaStringUtil.indent(indent) + entry.getAliases().stream().sorted((a, b) -> Integer.compare(b.length(), a.length())).findFirst().get() + (arg.equals("") ? "" : " ") + arg));
                    if (entry.getAliases().stream().filter(InstructionRegistry.getBlockStarts()::contains).findFirst().isPresent()) {
                        indent++;
                    }
                } else {
                    throw new JAISBaLExecutionException("No applicable instruction found " + name);
                }

            }
        }

        StringBuilder builder = new StringBuilder();
        int length = preProcessed.stream().sorted((a, b) -> Integer.compare(b.getC().length(), a.getC().length())).findFirst().orElse(Triple.of(Instruction.DUMMY, "", "")).getC().length() + 4;
        StringBuilder params = new StringBuilder();
        if (!context.isImplicitInput()) {
            context.getParameters().forEach(params::append);
        }
        params.append("}");
        if (!context.isImplicitInput() && !context.getParameters().isEmpty()) {
            String p = params.toString();
            builder.append(p).append(PlasmaStringUtil.indent(length - p.length(), " ")).append(Program.COMMENT_START).append(" request input ").append(Program.COMMENT_END);
            builder.append(System.lineSeparator());
        }
        if (!preProcessed.isEmpty()) {
            int counter = 0;
            for (Triple<Instruction, String, String> piece : preProcessed) {
                builder.append(piece.getC()).append(PlasmaStringUtil.indent(length - piece.getC().length(), " ")).append(Program.COMMENT_START).append(" [").append(counter).append("] ").append(StringFormat.fromString(piece.getA().getDescription()).filler().var("arg", piece.getB()).fill()).append(" ");
                try {
                    if (piece.getA() instanceof AuxiliaryInstruction) {
                        int i = Integer.parseInt(piece.getB());
                        Instruction aux = InstructionRegistry.getAuxiliaryInstructions().get(i);
                        builder.append("(").append(aux.getDescription()).append(")");
                    } else if (piece.getA() instanceof AuxiliaryConstant) {
                        int i = Integer.parseInt(piece.getB());
                        CastableValue aux = InstructionRegistry.getAuxiliaryConstants().get(i);
                        builder.append("(").append(Program.valueToString(aux)).append(")");

                    }
                } catch (NumberFormatException | IndexOutOfBoundsException ignore) {

                }
                builder.append(Program.COMMENT_END).append(System.lineSeparator());
                counter++;
            }
        }
        return builder.toString();
    }

    public static String minify(FunctionContext context, boolean main) throws JAISBaLExecutionException {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        while (context.currentExists()) {
            String instruction = context.getCurrentAndStep();
            if (first) {
                StringBuilder params = new StringBuilder();
                if (!context.isImplicitInput()) {
                    context.getParameters().forEach(params::append);
                } else if (!main) {
                    params.append("i");
                }
                if (Type.hasTypeNext(instruction)) {
                    params.append("}");
                }
                builder.append(params);
                first = false;
            }
            if (!instruction.equals("")) {
                String name;
                String arg;
                String[] pieces = instruction.split(" ", 2);
                if (pieces.length == 1) {
                    name = pieces[0];
                    arg = "";
                } else {
                    name = pieces[0];
                    arg = pieces[1];
                }
                if (InstructionRegistry.getAccessibleInstructions().stream().filter(e -> e.isName(name)).findFirst().isPresent()) {
                    Instruction entry = InstructionRegistry.getAccessibleInstructions().stream().filter(e -> e.isName(name)).findFirst().get();
                    builder.append(entry.getId()).append(arg);
                } else {
                    throw new JAISBaLExecutionException("No applicable instruction found " + name);
                }
            }
        }
        return builder.toString();
    }

    private static void verify(FunctionContext context) throws JAISBaLExecutionException {
        while (context.currentExists()) {
            String instruction = context.getCurrentAndStep();
            if (!instruction.equals("")) {
                String name;
                String arg;
                String[] pieces = instruction.split(" ", 2);
                if (pieces.length == 1) {
                    name = pieces[0];
                    arg = "";
                } else {
                    name = pieces[0];
                    arg = pieces[1];
                }
                if (InstructionRegistry.getAccessibleInstructions().stream().filter(e -> e.isName(name)).findFirst().isPresent()) {
                    Instruction entry = InstructionRegistry.getAccessibleInstructions().stream().filter(e -> e.isName(name)).findFirst().get();
                    String test = null;
                    try {
                        test = entry.getValueReader().apply(new CharacterStream(arg));
                    } catch (JAISBaLExecutionException e) {
                        throw e;
                    } catch (Throwable throwable) {
                        throw new JAISBaLExecutionException("Error while reading value ", throwable);
                    }
                    if ((test == null && !arg.trim().equals("")) || (test != null && !test.equals(arg))) {
                        throw new JAISBaLExecutionException("Value reader for instruction " + entry.getMainAlias() + " did not accept argument \"" + arg + "\"");
                    }
                } else {
                    throw new JAISBaLExecutionException("No applicable instruction found " + name);
                }
            }
        }
    }

    public boolean isImplicitInput() {
        return this.function.isImplicitInput();
    }

    public String minify(boolean main) throws JAISBaLExecutionException {
        return FunctionContext.minify(this, main);
    }

    public String explain(int indent) throws JAISBaLExecutionException {
        return FunctionContext.explain(this, indent);
    }

    public FunctionContext clone() {
        FunctionContext context = new FunctionContext(this.function, this.parameters, this.instructions, this.stack, this.parent, this.locals, this.program);
        context.current = this.current;
        context.currentArg = this.currentArg;
        context.running = this.running;
        return context;
    }

    public int indexOf(char instruction) {
        for (int i = this.current; i < this.instructions.size(); i++) {
            if (this.instructions.get(i).equals(String.valueOf(instruction))) {
                return i;
            }
        }
        return -1;
    }

    public int subsetIndex(String begin, String end) throws JAISBaLExecutionException {
        int z = 0;
        int i;
        for (i = this.current - 1; i < this.instructions.size(); i++) {
            String instruction = this.instruction(i);
            String name;
            String arg;
            String[] pieces = instruction.split(" ", 2);
            if (pieces.length == 1) {
                name = pieces[0];
                arg = "";
            } else {
                name = pieces[0];
                arg = pieces[1];
            }
            if (Function.exists(name)) {
                if (InstructionRegistry.getAccessibleInstructions().stream().filter(e -> e.isName(name)).findFirst().isPresent()) {
                    Instruction entry = InstructionRegistry.getAccessibleInstructions().stream().filter(e -> e.isName(name)).findFirst().get();

                    if (entry.isName(begin) || InstructionRegistry.getBlockStarts().contains(entry.getMainAlias())) {
                        z++;
                    }

                    if (entry.isName(end) || InstructionRegistry.getBlockEnds().contains(entry.getMainAlias())) {
                        z--;
                    }

                    if (z == 0) {
                        break;
                    }
                }
            }
        }

        return i;
    }

    public int subsetIndexDubloid(String begin, String end) throws JAISBaLExecutionException {
        int z = 0;
        int i;
        for (i = this.current - 1; i < this.instructions.size(); i++) {
            String instruction = this.instruction(i);
            String name;
            String arg;
            String[] pieces = instruction.split(" ", 2);
            if (pieces.length == 1) {
                name = pieces[0];
                arg = "";
            } else {
                name = pieces[0];
                arg = pieces[1];
            }
            if (Function.exists(name)) {
                if (InstructionRegistry.getAccessibleInstructions().stream().filter(e -> e.isName(name)).findFirst().isPresent()) {
                    Instruction entry = InstructionRegistry.getAccessibleInstructions().stream().filter(e -> e.isName(name)).findFirst().get();

                    if (entry.isName(begin)) {
                        z++;
                    }

                    if(InstructionRegistry.getBlockStarts().contains(entry.getMainAlias())) {
                        z++;
                    }

                    if (entry.isName(end)) {
                        z--;
                    }

                    if(InstructionRegistry.getBlockEnds().contains(entry.getMainAlias())) {
                        z--;
                    }

                    if (z == 0) {
                        break;
                    }
                }
            }
        }

        return i;
    }

    public void terminate() {
        this.running.set(false);
    }

    public String instruction(int index) {
        return this.instructions.get(index);
    }

    public int getCurrent() {
        return this.current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public void runAsMain() throws JAISBaLExecutionException, StringParseException, IOException {
        this.parent = new Stack<>();
        List<Type> params = PlasmaListUtil.looseClone(this.function.getParameters());
        List<CastableValue> vals = new ArrayList<>();
        if (params.size() != 1 || !params.get(0).isImplicit()) {
            while (params.size() > 0) {
                Type type = params.get(0);
                if (Program.displayPrompts) {
                    JAISBaL.getOut().print("Enter a " + type.getName() + " > ");
                }
                String entered = JAISBaL.getIn().get();
                if (entered == null) { //The input supplier was terminated
                    JAISBaL.getOut().println("Program Terminated");
                    return;
                }
                try {
                    CastableValue value;
                    if(type.isString() || type.isNumber()) {
                        value = PlasmaMathUtil.isBigDecimal(entered) ? CastableValue.of(new BigDecimal(entered)) : CastableValue.of(entered);
                    } else {
                        value = Type.easyReadValues(new CharacterStream(entered));
                    }
                    if (type.matches(value)) {
                        params.remove(0);
                        vals.add(value);
                    } else {
                        JAISBaL.getOut().println("Invalid value: " + Program.valueToString(value) + " cannot be converted to " + type.getName());
                        return;
                    }
                } catch (StringParseException e) {
                    throw e;
                }
            }
        }
        PlasmaListUtil.reverseList(vals).forEach(this.getStack()::push);
        FunctionContext.run(this);

        if (this.running.get()) {
            JAISBaL.getOut().println(System.lineSeparator() + System.lineSeparator() + PlasmaStringUtil.indent(20, "-"));
            JAISBaL.getOut().println("Stack: " + FunctionContext.valueToString(new CastableValue(this.getStack().toArray(new CastableValue[0]))));
            JAISBaL.getOut().println("Locals: " + FunctionContext.valueToString(new CastableValue(this.getLocals())));
        } else {
            JAISBaL.getOut().println("Program Terminated");
        }
    }

    public State runSubset(int end, Predicate<FunctionContext> transmitJump) throws JAISBaLExecutionException {
        return FunctionContext.run(this, end, transmitJump);
    }

    public State run(Stack<CastableValue> parent) throws JAISBaLExecutionException {
        this.accept(parent);
        return FunctionContext.run(this);
    }

    public String getCurrentInstruction() {
        this.validateCurrent();
        return this.instructions.get(this.current);
    }

    public String getCurrentAndStep() {
        String s = this.getCurrentInstruction();
        this.setCurrent(this.getCurrent() + 1);
        return s;
    }

    public boolean currentExists() {
        return this.getCurrent() < this.instructions.size();
    }

    public void validateCurrent() {
        this.setCurrent(this.getCurrent() < 0 ? 0 : this.getCurrent());
    }

    public CastableValue getCurrentArgEasy() {
        CastableValue[] values = this.getCurrentArg();
        if (values.length == 1) {
            return values[0];
        } else {
            return new CastableValue(values);
        }
    }

    public CastableValue[] getCurrentArg() {
        return this.currentArg;
    }

    public void accept(Stack<CastableValue> parent) throws JAISBaLExecutionException {
        if (this.getParameters().size() == 0 || this.getParameters().stream().filter(Type::isImplicit).findFirst().isPresent()) {
            return;
        }

        if (parent.size() < this.getParameters().size()) {
            throw new JAISBaLExecutionException("Stack underflow: required at least " + this.getParameters().size() + " parameter(s), but only " + parent.size() + " were/was available on the stack");
        } else {
            for (int i = this.getParameters().size() - 1; i >= 0; i--) {
                Type param = this.getParameters().get(i);
                CastableValue value = parent.pop();
                if (param.matches(value)) {
                    this.getLocals().put((long) i, value);
                } else {
                    throw new JAISBaLExecutionException("Invalid parameter: " + value.getValue().orElse(null) + " cannot be converted to " + param.getName());
                }
            }
        }

        this.parent = parent;
    }

    public Stack<CastableValue> getParentStack() {
        return this.parent;
    }

    public Program getProgram() {
        return this.program;
    }

    public FunctionContext setProgram(Program program) {
        this.program = program;
        return this;
    }

    public Function getFunction() {
        return this.function;
    }

    public Stack<CastableValue> getStack() {
        return this.stack;
    }

    public Map<Long, CastableValue> getLocals() {
        return this.locals;
    }

    public List<String> getInstructions() {
        return this.instructions;
    }

    public List<Type> getParameters() {
        return this.parameters;
    }

    public void verify() throws JAISBaLExecutionException {
        FunctionContext.verify(this);
    }
}
