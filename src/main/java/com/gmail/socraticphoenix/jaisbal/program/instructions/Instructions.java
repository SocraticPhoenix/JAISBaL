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
package com.gmail.socraticphoenix.jaisbal.program.instructions;

import com.gmail.socraticphoenix.jaisbal.program.Program;
import com.gmail.socraticphoenix.jaisbal.program.Type;
import com.gmail.socraticphoenix.jaisbal.program.function.FunctionContext;
import com.gmail.socraticphoenix.jaisbal.util.DangerousConsumer;
import com.gmail.socraticphoenix.jaisbal.util.DangerousFunction;
import com.gmail.socraticphoenix.jaisbal.util.JAISBaLExecutionException;
import com.gmail.socraticphoenix.jaisbal.util.NumberNames;
import com.gmail.socraticphoenix.plasma.collection.PlasmaListUtil;
import com.gmail.socraticphoenix.plasma.math.PlasmaMathUtil;
import com.gmail.socraticphoenix.plasma.reflection.CastableValue;
import com.gmail.socraticphoenix.plasma.string.BracketCounter;
import com.gmail.socraticphoenix.plasma.string.CharacterStream;
import com.gmail.socraticphoenix.plasma.string.PlasmaStringUtil;
import com.gmail.socraticphoenix.plasma.string.QuotationTracker;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public interface Instructions {
    Instruction PUSH_NUMBER = new Instruction(f -> f.getStack().push(f.getCurrentArgEasy()), Instructions.number(), "push ${arg} onto the stack", "Pushes a number onto the stack. This instruction takes one argument, a number, and continues reading the argument until the number literal terminates", "pushnum");
    Instruction PUSH_TERMINATED = new Instruction(f -> f.getStack().push(f.getCurrentArgEasy()), Instructions.terminated(), "push ${arg} onto the stack", "Pushes any value onto the stack. This instruction takes one argument, and continues reading the argument until the '}' terminating character is found. '}' can be escaped or nested in [] to allow it to be used in the value itself", "pushterm");
    Instruction PUSH_NUMBER_OUTPUT = new Instruction(f -> {
        f.getStack().push(f.getCurrentArgEasy());
        System.out.print(f.getStack().pop());
    }, Instructions.number(), "print ${arg}", "Pushes a number onto the stack (see pushnum), and then pops and prints the number", "printnum");
    Instruction PUSH_TERMINATED_OUTPUT = new Instruction(f -> {
        f.getStack().push(f.getCurrentArgEasy());
        System.out.print(Program.valueToString(f.getStack().pop()));
    }, Instructions.terminated(), "print ${arg}", "Pushes any value onto the stack (see pushterm), and then pops and prints the value", "printterm");
    Instruction PUSH_NUMBER_OUTPUT_NEWLINE = new Instruction(f -> {
        f.getStack().push(f.getCurrentArgEasy());
        System.out.print(Program.valueToString(f.getStack().pop()));
    }, Instructions.number(), "print ${arg}", "Pushes a number onto the stack (see pushnum), and then pops and prints the number, followed by a newline", "printnumln");
    Instruction PUSH_TERMINATED_OUTPUT_NEWLINE = new Instruction(f -> {
        f.getStack().push(f.getCurrentArgEasy());
        System.out.println(Program.valueToString(f.getStack().pop()));
    }, Instructions.terminated(), "print ${arg}", "Pushes any value onto the stack (see pushterm), and then pops and prints the value, followed by a newline", "printtermln");
    Instruction POP = new Instruction(f -> {
        Program.checkUnderflow(1, f);
        f.getStack().pop();
    }, "pop the top value off the stack", "Pops the top value off the stack", "pop");
    Instruction POP_ALL = new Instruction(f -> {
        while (!f.getStack().isEmpty()) {
            f.getStack().pop();
        }
    }, "clear the stack", "Pops every value off the stack", "popall");
    Instruction SWAP = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        CastableValue top = f.getStack().pop();
        CastableValue under = f.getStack().pop();
        f.getStack().push(top);
        f.getStack().push(under);
    }, "swap the top two values of the stack", "Pops the top two values off the stack, and then pushes them in reverse order", "swap");
    Instruction DUPLICATE = new Instruction(f -> {
        Program.checkUnderflow(1, f);
        CastableValue value = f.getStack().pop();
        f.getStack().push(value);
        f.getStack().push(value);
    }, "duplicate the top value of the stack", "Pops the top value off the stack, and then pushes it twice", "dup", "duplicate");
    Instruction DUPLICATE_ALL = new Instruction(f -> {
        List<CastableValue> values = new ArrayList<>();
        while (!f.getStack().isEmpty()) {
            values.add(f.getStack().pop());
        }
        values = PlasmaListUtil.reverseList(values);
        values.stream().forEach(f.getStack()::push);
        values.stream().forEach(f.getStack()::push);
    }, "duplicate the entire stack", "Pops the entire stack, and then pushes every value to the stack, twice. For example, if the stack was [1, 2, 3], it would become [1, 2, 3, 1, 2, 3]", "dupall", "duplicateall");
    Instruction TRIPLICATE = new Instruction(f -> {
        Program.checkUnderflow(1, f);
        CastableValue value = f.getStack().pop();
        f.getStack().push(value);
        f.getStack().push(value);
        f.getStack().push(value);
    }, "triplicate the top value of the stack", "Pops the top value off the stack, and then pushes it thrice", "tri", "triplicate");
    Instruction TRIPLICATE_ALL = new Instruction(f -> {
        List<CastableValue> values = new ArrayList<>();
        while (!f.getStack().isEmpty()) {
            values.add(f.getStack().pop());
        }
        values = PlasmaListUtil.reverseList(values);
        values.stream().forEach(f.getStack()::push);
        values.stream().forEach(f.getStack()::push);
        values.stream().forEach(f.getStack()::push);
    }, "triplicate the entire stack", "Pops the entire stack, and then pushes every value to the stack, thrice. For example, if the stack was [1, 2], it would become [1, 2, 1, 2, 1, 2]", "triall", "triplicateall");
    Instruction STORE_STACK = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        CastableValue indexV = f.getStack().pop();
        Type.NUMBER.checkMatches(indexV);
        CastableValue value = f.getStack().pop();
        BigDecimal index = indexV.getValueAs(BigDecimal.class).get();
        try {
            f.getLocals().put(index.longValueExact(), value);
        } catch (ArithmeticException e) {
            throw new JAISBaLExecutionException("Invalid value: " + String.valueOf(index) + " is not an integer index, or is too large", e);
        }
    }, Instructions.number(), "store the second value in the stack at var<top value of stack>", "Pops the top two values off the stack, and stores b in var a. This instruction is only successful if a is a 64-bit integer", "sstore");
    Instruction LOAD_STACK = new Instruction(f -> {
        Program.checkUnderflow(1, f);
        CastableValue indexV = f.getStack().pop();
        Type.NUMBER.checkMatches(indexV);
        BigDecimal index = indexV.getValueAs(BigDecimal.class).get();
        try {
            if (f.getLocals().get(index.longValueExact()) != null) {
                f.getStack().push(f.getLocals().get(index.longValueExact()));
            } else {
                throw new JAISBaLExecutionException("Invalid value: " + String.valueOf(index) + " does not lead to a registered local variable");
            }
        } catch (ArithmeticException e) {
            throw new JAISBaLExecutionException("Invalid value: " + String.valueOf(index) + " is not an integer index, or is too large", e);
        }
    }, Instructions.number(), "push the value in var<top value of stack> onto the stack", "Pops the top value off the stack and loads the value in var a onto the stack. This instruction is only succesful if a is a 64-bit integer", "sload");
    Instruction STORE = new Instruction(f -> {
        Program.checkUnderflow(1, f);
        CastableValue indexV = f.getCurrentArgEasy();
        Type.NUMBER.checkMatches(indexV);
        CastableValue value = f.getStack().pop();
        BigDecimal index = indexV.getValueAs(BigDecimal.class).get();
        try {
            f.getLocals().put(index.longValueExact(), value);
        } catch (ArithmeticException e) {
            throw new JAISBaLExecutionException("Invalid value: " + String.valueOf(index) + " is not an integer index, or is too large", e);
        }
    }, Instructions.number(), "store the top value of the stack into var${arg}", "Pops the top value off the stack and stores it in the given var. This instruction takes one argument, a number (see pushnum). This instruction is only succesful if the argument is a 64-bit integer", "store");
    Instruction LOAD = new Instruction(f -> {
        CastableValue indexV = f.getCurrentArgEasy();
        Type.NUMBER.checkMatches(indexV);
        BigDecimal index = indexV.getValueAs(BigDecimal.class).get();
        try {
            if (f.getLocals().get(index.longValueExact()) != null) {
                f.getStack().push(f.getLocals().get(index.longValueExact()));
            } else {
                throw new JAISBaLExecutionException("Invalid value: " + String.valueOf(index) + " does not lead to a registered local variable");
            }
        } catch (ArithmeticException e) {
            throw new JAISBaLExecutionException("Invalid value: " + String.valueOf(index) + " is not an integer index, or is too large", e);
        }
    }, Instructions.number(), "push the value in var${arg} onto the stack", "Pushes the value in the given var onto the stack. This instruction takes on argument, a number (see pushnum). This instruction is only succesful if the argument is a 64-bit integer", "load");
    Instruction STORE_ALL = new Instruction(f -> {
        for (long i = f.getStack().size() - 1; !f.getStack().isEmpty(); i--) {
            f.getLocals().put(i, f.getStack().pop());
        }
    }, "store entire stack in var0 through var<stack size>", "Pops every value off the stack, and stores it in a var. The first pop is stored in var <size of stack>, and the second is stored in the next var, and so on, until var 0 is reached. This is implemented as such so that a call to storeall, followed by a call to loadall, does not alter the stack", "storeall");
    Instruction LOAD_ALL = new Instruction(f -> {
        f.getLocals().entrySet().stream().sorted((a, b) -> Long.compare(b.getKey(), a.getKey())).forEach(e -> {
            f.getStack().push(e.getValue());
        });
    }, "load all variables onto the stack", "Loads every value from vars onto the stack, starting at the lowest var index and continuing to the highest", "loadall");
    Instruction STORE_ALL_LOAD_ALL = new Instruction(Instructions.compose(Instructions.STORE_ALL.getAction(), Instructions.LOAD_ALL.getAction()), "store all variables on the stack, then load all variables", "Stores all variables and then loads all variables (see loadall and storeall)", "storeloadall");
    Instruction POP_OUTPUT = new Instruction(f -> {
        Program.checkUnderflow(1, f);
        System.out.print(Program.valueToString(f.getStack().pop()));
    }, "pop the top value of a stack and print it", "Pops the top value off the stack and prints it", "popout");
    Instruction POP_OUTPUT_NEWLINE = new Instruction(f -> {
        Program.checkUnderflow(1, f);
        System.out.println(Program.valueToString(f.getStack().pop()));
    }, "pop the top value off a stack and print it with a new line", "Pops the top value off the stack and prints it, followed by a new line", "popoutln");
    Instruction POP_OUTPUT_ALL = new Instruction(f -> {
        while (!f.getStack().isEmpty()) {
            System.out.print(Program.valueToString(f.getStack().pop()));
        }
    }, "pop off every value in the stack and print it", "Pops every value off the stack, and prints each one", "popoutall");
    Instruction POP_OUTPUT_ALL_NEWLINE = new Instruction(f -> {
        while (!f.getStack().isEmpty()) {
            System.out.print(Program.valueToString(f.getStack().pop()));
        }
    }, "pop off every value in the stack and print each one with a new line", "Pops every value off the stack, and prints each one on a separate line", "popoutallln");
    Instruction NEW_LINE = new Instruction(f -> {
        System.out.println();
    }, "print a new line", "Prints a newline", "ln");
    Instruction SPACE = new Instruction(f -> {
        System.out.print(" ");
    }, "print a space", "Prints a space", "space");
    Instruction TAB = new Instruction(f -> {
        System.out.print("\t");
    }, "print a tab", "Prints a tab", "tab");
    Instruction PUSH_NEW_LINE = new Instruction(f -> {
        f.getStack().push(CastableValue.of(System.lineSeparator()));
    }, "push a new line", "Pushes a newline onto the stack", "pushln");
    Instruction PUSH_SPACE = new Instruction(f -> {
        f.getStack().push(CastableValue.of(" "));
    }, "push a space", "Pushes a space onto the stack", "pushspace");
    Instruction PUSH_TAB = new Instruction(f -> {
        f.getStack().push(CastableValue.of("\t"));
    }, "push a tab", "Pushes a tab onto the stack", "pushtab");
    Instruction MULTIPLY = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        f.getStack().push(Instructions.mul(f.getStack().pop(), f.getStack().pop()));
    }, "multiply the top two values of the stack", "Multiplies a and b. If a and b are both numbers, normal multiplication will occur. If either a or b is an array, and the other is a non-array, every value in the array will be multiplied by the other value. If either a or b is a string, and the other is a number, the string will be duplicated <number> times. If both a and b are strings, the number of characters in a which are also in b will be pushed. Finally, if both values are arrays, a new array will be created with the length of the longer array, and every value in the new array will be the result of multiplication of same-indexed values in a and b", "*", "mul");
    Instruction DIVIDE = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        f.getStack().push(Instructions.div(f.getStack().pop(), f.getStack().pop()));
    }, "divide the top value of the stack by the second value on the stack", "Divides a by b. If a and b are both numbers, normal division will occur. If either a or b is an array, and the other is a non-array, every value in the array will be divided by the other value. If a or b is a string, and the other is a number, a and b will both be converted to strings and divided. If a and b are both strings, the number of characters in a which are not in b will be pushed. Finally, if both values are arrays, a new array will be created with the length of the smaller array, and every value in the array will be the result of division of same-indexed values in a and b", "/", "div");
    Instruction ADD = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        f.getStack().push(Instructions.add(f.getStack().pop(), f.getStack().pop()));
    }, "add the top two values of the stack", "Adds a and b. If a and b are both numbers, normal addition will occur. If either a or b is an array, and the other is a non-array, the other value will be added to every value in the array. If a or b is a string, and the other is a number, a and b will both be converted to strings and added. If a and b are strings, the length of the longest substring of a that is also present in b will be pushed. Finally, if both values are arrays, a new array will be created with a length of the longer array, and every value in the new array will be the result of addition of same-indexed values in a and b", "+", "add");
    Instruction SUBTRACT = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        f.getStack().push(Instructions.sub(f.getStack().pop(), f.getStack().pop()));
    }, "subtract the second value on the stack from the top value on the stack", "Subtracts b from a. If a and b are both numbers, normal subtraction will occur. If either a or b is an array, and the other is a non-array, the other value will be subtracted from every value in the array. If a or b is a string, and the other is a number, a and b will both be converted to strings and subtracted. If a and b are strings, the number of times b occurs in a will be pushed. Finally, if both values are arrays, a new array will be created with a length of the smaller array, and ever value in the new array will be the result of subtraction of same-indexed values in a and b", "-", "sub");
    Instruction REVERSE = new Instruction(f -> {
        Program.checkUnderflow(1, f);
        CastableValue value = f.getStack().pop();
        if(value.getAsString().isPresent()) {
            f.getStack().push(CastableValue.of(PlasmaStringUtil.reverseString(value.getAsString().get())));
        } else {
            f.getStack().push(CastableValue.of(PlasmaListUtil.reverseList(PlasmaListUtil.buildList(value.getValueAs(CastableValue[].class).get())).toArray(new CastableValue[0])));
        }
    }, "reverse the top value of the stack", "Pops the top value off the stack and reverses it. If the a is a number or string, it will be converted to a string and the order of characters will be reversed. If a is an array, the order of elements will be reversed", "reverse");

    Instruction CONCAT = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        f.getStack().push(Instructions.concat(f.getStack().pop(), f.getStack().pop()));
    }, "concatenate the top two values of the stack", "Concatenates a and b. If a and b are both numbers, both strings, or one of each, they will be converted to strings and concatenate using string concatenation. If a or b is an array, and the other is a non array, the non array will be prepended (if a is non-rray) or appended (if b is non array). If both a and b are arrays, list concatenation will be used to join a and b", "concat", "&+");
    Instruction PUSH_NEW_LINE_CONCAT = new Instruction(f -> {
        f.getStack().push(CastableValue.of(System.lineSeparator()));
        Instructions.CONCAT.getAction().accept(f);
    }, "push a new line and concatenate", "Pushes a new line and then concatenates the top two values of the stack (see concat)", "concatln");
    Instruction PUSH_SPACE_CONCAT = new Instruction(f -> {
        f.getStack().push(CastableValue.of(" "));
        Instructions.CONCAT.getAction().accept(f);
    }, "push a space and concatenate", "Pushes a space and then concatenates the top two values of the stack (see concat)", "concatspace");
    Instruction PUSH_TAB_CONCAT = new Instruction(f -> {
        f.getStack().push(CastableValue.of("\t"));
        Instructions.CONCAT.getAction().accept(f);
    }, "push a tab and concatenate", "Pushes a tab and then concatenates the top two values of the stack (see concat)", "concattab");
    Instruction SPLIT = new Instruction(f -> {
        Program.checkUnderflow(1, f);
        CastableValue value = f.getStack().pop();
        Type.STRING.checkMatches(value);
        Type.STRING.checkMatches(f.getCurrentArgEasy());
        String[] pieces = value.getAsString().get().split(f.getCurrentArgEasy().getAsString().get());
        CastableValue[] array = new CastableValue[pieces.length];
        for (int i = 0; i < pieces.length; i++) {
            array[i] = CastableValue.of(pieces[i]);
        }
        f.getStack().push(CastableValue.of(array));
    }, Instructions.terminated(), "split the top value of the stack by ${arg}", "Splits the top value of the stack by the given regex, and pushes the array result. This instruction takes one argument, terminated by '}' (see pushterm). This instruction is only succesful if the top value of the stack is a string or number", "split");
    Instruction POW = new Instruction(new SyntheticFunction(PlasmaListUtil.buildList(Type.NUMBER, Type.NUMBER), f -> {
        BigDecimal a = f.getStack().pop().getValueAs(BigDecimal.class).get();
        BigDecimal b = f.getStack().pop().getValueAs(BigDecimal.class).get();
        if (a.compareTo(BigDecimal.ZERO) < 0) {
            throw new JAISBaLExecutionException("Cannot raise negative " + a + " to a power");
        } else {
            f.getStack().push(CastableValue.of(new BigDecimal(Math.pow(a.doubleValue(), b.doubleValue()))));
        }
    }), "raise the top value on the stack to the second value on the stack", "Raises a to b. This instruction is only succesful if the top two values of the stack are numbers. Furthermore, accurate results can only be calculated for numbers that fit in 32-bits", "pow", "^");
    Instruction MODULO = new Instruction(new SyntheticFunction(PlasmaListUtil.buildList(Type.NUMBER, Type.NUMBER), f -> {
        BigDecimal a = f.getStack().pop().getValueAs(BigDecimal.class).get();
        BigDecimal b = f.getStack().pop().getValueAs(BigDecimal.class).get();
        try {
            f.getStack().push(CastableValue.of(new BigDecimal(a.toBigIntegerExact().mod(b.toBigIntegerExact()))));
        } catch (ArithmeticException e) {
            throw new JAISBaLExecutionException("Modulo only acts on integer values");
        }
    }), "calculate the modulus of the top value on the stack by the second value on the stack", "Calculates a mod b. This instruction is only succesful if the top two values of the stack are integers", "mod", "%");
    Instruction FLOOR = new Instruction(new SyntheticFunction(PlasmaListUtil.buildList(Type.NUMBER), f -> {
        BigDecimal decimal = f.getStack().pop().getValueAs(BigDecimal.class).get();
        f.getStack().push(CastableValue.of(decimal.setScale(0, RoundingMode.FLOOR)));
    }), "floor the top value of the stack", "Calculates floor a. This instruction is only succesful if the top value of the tack is a number", "floor");
    Instruction CEIL = new Instruction(new SyntheticFunction(PlasmaListUtil.buildList(Type.NUMBER), f -> {
        BigDecimal decimal = f.getStack().pop().getValueAs(BigDecimal.class).get();
        f.getStack().push(CastableValue.of(decimal.setScale(0, RoundingMode.CEILING)));
    }), "ceil the top value of the stack", "Calculates ceil a. This instruction is only succesful if the top value of the tack is a number", "ceil");
    Instruction ROUND = new Instruction(new SyntheticFunction(PlasmaListUtil.buildList(Type.NUMBER), f -> {
        BigDecimal decimal = f.getStack().pop().getValueAs(BigDecimal.class).get();
        f.getStack().push(CastableValue.of(decimal.setScale(0, RoundingMode.HALF_UP)));
    }), "round the top value of the stack", "Calculates round a (traditional rounding). This instruction is only succesful if the top value of the stack is a number", "round");
    Instruction SQRT = new Instruction(new SyntheticFunction(PlasmaListUtil.buildList(Type.NUMBER), f -> {
        BigDecimal decimal = f.getStack().pop().getValueAs(BigDecimal.class).get();
        f.getStack().push(CastableValue.of(Instructions.sqrt(decimal)));

    }), "compute the square root of the top value on the stack", "Computes the square root of a, and pushes it to the stack. This instruction fails if a is not a number", "sqrt");
    Instruction SET_CURRENT_ARG = new Instruction(f -> {
        //Argument setting handled automatically by Program
    }, Instructions.terminated(), "set the current context arg to ${arg}", "Sets the current program argument to the one specified. This instruction takes one argument, terminated by '}' (see pushterm)", "arg");
    Instruction FOR_LOOP = new Instruction(f -> {
        FunctionContext sub = f.subset("for", "end");
        Program.checkUnderflow(1, f);
        CastableValue indexV = f.getStack().pop();
        Type.NUMBER.checkMatches(indexV);
        BigDecimal bd = BigDecimal.ZERO;
        BigDecimal cond = indexV.getValueAs(BigDecimal.class).get();
        while (bd.compareTo(cond) < 0) {
            sub.clone().runAsSurrogate(f);
            bd = bd.add(BigDecimal.ONE);
        }
        f.setCurrent(f.getCurrent() + sub.getInstructions().size());
    }, "start for loop", "This instruction pops a number of the stack, and executes the for loop body floor a times. This instruction also opens a new function frame. This instructions is only succesful if the top value on the stack is a number", "for");
    Instruction END = new Instruction(f -> {
    }, "end current language construct", "Ends a loop, if, ifelse, or other statement", "end");
    Instruction BREAK = new Instruction(f -> {
    }, "break out of the current function frame", "Breaks out of the current function frame", "break");
    Instruction ARRAY_CREATE = new Instruction(f -> {
        CastableValue index = f.getCurrentArgEasy();
        Type.NUMBER.checkMatches(index);
        try {
            CastableValue[] array = new CastableValue[index.getValueAs(BigDecimal.class).get().intValueExact()];
            f.getStack().push(CastableValue.of(array));
        } catch (ArithmeticException e) {
            throw new JAISBaLExecutionException(Program.valueToString(index) + " is not a 32-bit integer index");
        }
    }, Instructions.number(), "create and push new array with length ${arg}", "Creates a new array with the given length. This instruction takes one argument, a number (see pushnum)", "newarray");
    Instruction ARRAY_CREATE_STACK = new Instruction(f -> {
        Program.checkUnderflow(1, f);
        CastableValue index = f.getStack().pop();
        Type.NUMBER.checkMatches(index);
        try {
            CastableValue[] array = new CastableValue[index.getValueAs(BigDecimal.class).get().intValueExact()];
            f.getStack().push(CastableValue.of(array));
        } catch (ArithmeticException e) {
            throw new JAISBaLExecutionException(Program.valueToString(index) + " is not a 32-bit integer index");
        }
    }, Instructions.number(), "create and push new array with length <top value of stack>", "Creates a new array with the length a. This instruction fails if a is not a 32-bit integer index", "snewarray");
    Instruction ARRAY_LOAD_STACK = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        CastableValue index = f.getStack().pop();
        CastableValue array = f.getStack().pop();
        Type.NUMBER.checkMatches(index);
        Type.GENERAL_ARRAY.checkMatches(array);
        try {
            f.getStack().push(array);
            f.getStack().push(array.getValueAs(CastableValue[].class).get()[index.getValueAs(BigDecimal.class).get().intValueExact()]);
        } catch (ArithmeticException e) {
            throw new JAISBaLExecutionException("Invalid value: " + String.valueOf(index) + " is not an integer index, or is too large or small", e);
        }
    }, Instructions.number(), "load the value at index ${arg} from an array", "Loads a value from array b, at index a. This instruction does not pop of the array. This instruction is only succesful if b is an array and a is a 32-bit integer that is an index in b", "sarrayload", "saload");
    Instruction ARRAY_STORE_STACK = new Instruction(f -> {
        Program.checkUnderflow(3, f);
        CastableValue index = f.getStack().pop();
        CastableValue value = f.getStack().pop();
        CastableValue array = f.getStack().pop();
        Type.NUMBER.checkMatches(index);
        Type.GENERAL_ARRAY.checkMatches(array);
        try {
            array.getValueAs(CastableValue[].class).get()[index.getValueAs(BigDecimal.class).get().intValueExact()] = value;
            f.getStack().push(array);
        } catch (ArithmeticException e) {
            throw new JAISBaLExecutionException("Invalid value: " + String.valueOf(index) + " is not an integer index, or is too large or small", e);
        }
    }, Instructions.number(), "store the second value in the stack in an array, using the top value of the stack as an index", "Stores value b in array c at index a. This instruction does not pop of the array. This instruction is only succesful if c is an array and a is 32-bit integer that is an index in b", "sarraystore", "sastore");
    Instruction ARRAY_LOAD = new Instruction(f -> {
        Program.checkUnderflow(1, f);
        CastableValue index = f.getCurrentArgEasy();
        CastableValue array = f.getStack().pop();
        Type.NUMBER.checkMatches(index);
        Type.GENERAL_ARRAY.checkMatches(array);
        try {
            f.getStack().push(array);
            f.getStack().push(array.getValueAs(CastableValue[].class).get()[index.getValueAs(BigDecimal.class).get().intValueExact()]);
        } catch (ArithmeticException e) {
            throw new JAISBaLExecutionException("Invalid value: " + String.valueOf(index) + " is not an integer index, or is too large or small", e);
        }
    }, Instructions.number(), "load a value from an array, using the top value of the stack as an index", "Loads a value from array a, from the specified index. This instruction does not pop of the array. This instruction takes one argument, a number (see pushnum). This instruction is only succesful if a is an array, and the argument given is a 32-bit integer that is an index of a", "arrayload", "aload");
    Instruction ARRAY_STORE = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        CastableValue index = f.getCurrentArgEasy();
        CastableValue value = f.getStack().pop();
        CastableValue array = f.getStack().pop();
        Type.NUMBER.checkMatches(index);
        Type.GENERAL_ARRAY.checkMatches(array);
        try {
            array.getValueAs(CastableValue[].class).get()[index.getValueAs(BigDecimal.class).get().intValueExact()] = value;
            f.getStack().push(array);
        } catch (ArithmeticException e) {
            throw new JAISBaLExecutionException("Invalid value: " + String.valueOf(index) + " is not an integer index, or is too large or small", e);
        }
    }, Instructions.number(), "store the top of the stack at index ${arg} in an array", "Stores value a in array b, at the specified index. This instruction does not pop of the array. This instruction takes on argument, a number (see pushnum). This instruction is only succesful if b is an array, and the argument given is a 32-bit integer that is an index of b", "arraystore", "astore");
    Instruction ARRAY_LENGTH = new Instruction(new SyntheticFunction(PlasmaListUtil.buildList(Type.GENERAL_ARRAY), f -> {
        CastableValue value = f.getStack().pop();
        f.getStack().push(value);
        f.getStack().push(CastableValue.of(new BigDecimal(value.getValueAs(CastableValue[].class).get().length)));
    }), "push the length of the array onto the stack", "Pushes the length of array a onto the stack. This instruction does not pop of the array. This instruction fails if a is not an array", "arraylength", "alength");
    Instruction ARRAY_SORT = new Instruction(new SyntheticFunction(PlasmaListUtil.buildList(Type.GENERAL_ARRAY), f -> {
        CastableValue[] array = f.getStack().pop().getValueAs(CastableValue[].class).get();
        Arrays.sort(array, Instructions::compare);
        f.getStack().push(CastableValue.of(array));
    }), "pop the top value of the stack, sort it, and push it", "Pops the top value off the stack and sorts it from smallest to largest (see compare). This instruction fails if a is not an array", "sort");
    Instruction ARRAY_SORT_REVERSE = new Instruction(new SyntheticFunction(PlasmaListUtil.buildList(Type.GENERAL_ARRAY), f -> {
        CastableValue[] array = f.getStack().pop().getValueAs(CastableValue[].class).get();
        Arrays.sort(array, (a, b) -> Instructions.compare(b, a));
        f.getStack().push(CastableValue.of(array));
    }), "pop the top value of the stack, sort it, reverse it, and push it", "Pops the top value off the stack and sorts it from largest to smallest (see compare). This instruction fails if a is not an array", "rsort");
    Instruction PUSH_TRUTHY = new Instruction(f -> f.getStack().push(new CastableValue(new BigDecimal(1))), "push a truthy value onto the stack", "Pushes 1, a truthy value, onto the stack", "true");
    Instruction PUSH_FALSY = new Instruction(f -> f.getStack().push(new CastableValue(new BigDecimal(0))), "push a falsy value onto the stack", "Pushes 0, a falsy value, onto the stack", "false");
    Instruction COMPARE = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        f.getStack().push(CastableValue.of(new BigDecimal(Instructions.compare(f.getStack().pop(), f.getStack().pop()))));
    }, "compare the top value of the stack with the second value on the stack", "Compares a and b and pushes a: positive int if a > b, negative int if b < a, and 0 if a = b. If a or b is an array, and the other value is a non-array, the array value will be considered larger. If a and b are both numbers, a mathematical comparison takes place. If both a and b are arrays, the comparison is calculated by initializing a single int variable, and consecutivley comparing each value of the arrays, then the difference between a.length and b.length is added to the variable. If a and b are both strings, lexical comparison takes place", "compare");
    Instruction EQUAL = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        if ((Instructions.compare(f.getStack().pop(), f.getStack().pop()) == 0)) {
            f.setCurrent(f.getCurrent() + 1);
        }
    }, "skip the next statement if the top two values on the stack are equal", "Skips the next instruction if a and b are equal (see compare)", "=", "equal");
    Instruction EQUAL_ALL = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        boolean check = true;
        CastableValue prev = null;
        while (!f.getStack().isEmpty()) {
            if (prev == null) {
                prev = f.getStack().pop();
            } else {
                CastableValue v = f.getStack().pop();
                check = check && Instructions.compare(v, prev) == 0;
                prev = v;
            }
        }

        if (check) {
            f.setCurrent(f.getCurrent() + 1);
        }
    }, "skip the next statement if all values on the stack are equal", "Consecutively pops every value of the stack, checks if it is equal to the previously popped value, and ANDs the boolean result to a single boolean. If the final boolean is true, the next instruction is skipped (see compare)", "&=", "equalall");
    Instruction NOT_EQUAL = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        if (Instructions.compare(f.getStack().pop(), f.getStack().pop()) != 0) {
            f.setCurrent(f.getCurrent() + 1);
        }
    }, "skip the next statement if the two top values on the stack are not equal", "Skips the next instruction if a and b are not equal (see compare)", "!=", "notequal");
    Instruction NOT_EQUAL_ALL = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        boolean check = true;
        CastableValue prev = null;
        while (!f.getStack().isEmpty()) {
            CastableValue v = f.getStack().pop();
            if (prev == null) {
                prev = v;
            } else {
                check = check && Instructions.compare(v, prev) != 0;
                prev = v;
            }
        }

        if (check) {
            f.setCurrent(f.getCurrent() + 1);
        }
    }, "skip the next statement if no values on the stack are equal", "Consecutively pops every value of the stack, checks if it is not equal to the previously popped value, and ANDs the boolean result to a single boolean. If the final boolean is true, the next instruction is skipped (see compare)", "&!=", "notequalall");
    Instruction GREATER = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        if (!(Instructions.compare(f.getStack().pop(), f.getStack().pop()) > 0)) {
            f.setCurrent(f.getCurrent() + 1);
        }
    }, "skip the next statement if the top value on the stack is greater than the next value on the stack", "Skips the next instruction if a > b (see compare)", ">", "greater");
    Instruction GREATER_ALL = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        boolean check = true;
        CastableValue prev = null;
        while (!f.getStack().isEmpty()) {
            if (prev == null) {
                prev = f.getStack().pop();
            } else {
                CastableValue v = f.getStack().pop();
                check = check && Instructions.compare(v, prev) > 0;
                prev = v;
            }
        }

        if (check) {
            f.setCurrent(f.getCurrent() + 1);
        }
    }, "skip the next statement if the values on the stack are in smalles to greatest order, with no duplicates", "Consecutively pops every value of the stack, checks if it is > the previously popped value, and ANDs the boolean result to a single boolean. If the final boolean is true, the next instruction is skipped (see compare)", "&>", "greaterall");
    Instruction LESS = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        if (!(Instructions.compare(f.getStack().pop(), f.getStack().pop()) < 0)) {
            f.setCurrent(f.getCurrent() + 1);
        }
    }, "skip the next statement if the top value on the stack is less than the next value on the stack", "Skips the next instruction if a < b (see compare)", "<", "less");
    Instruction LESS_ALL = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        boolean check = true;
        CastableValue prev = null;
        while (!f.getStack().isEmpty()) {
            if (prev == null) {
                prev = f.getStack().pop();
            } else {
                CastableValue v = f.getStack().pop();
                check = check && Instructions.compare(v, prev) < 0;
                prev = v;
            }
        }

        if (check) {
            f.setCurrent(f.getCurrent() + 1);
        }
    }, "skip the next statement if the values on the stack are in greatest to smallest order, with no duplicates", "Consecutively pops every value of the stack, checks if it is < the previously popped value, and ANDs the boolean result to a single boolean. If the final boolean is true, the next instruction is skipped (see compare)", "&<", "lessall");
    Instruction GREATER_EQUAL = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        if (!(Instructions.compare(f.getStack().pop(), f.getStack().pop()) >= 0)) {
            f.setCurrent(f.getCurrent() + 1);
        }
    }, "skip the next statement if the top value on the stack is greater than or equal to the next value on the stack", "Skips the next instruction if a >= b (see compare)", ">=", "greaterequal");
    Instruction GREATER_EQUAL_ALL = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        boolean check = true;
        CastableValue prev = null;
        while (!f.getStack().isEmpty()) {
            if (prev == null) {
                prev = f.getStack().pop();
            } else {
                CastableValue v = f.getStack().pop();
                check = check && Instructions.compare(v, prev) >= 0;
                prev = v;
            }
        }

        if (check) {
            f.setCurrent(f.getCurrent() + 1);
        }
    }, "skip the next statement if the values on the stack are in smallest to greatest order", "Consecutively pops every value of the stack, checks if it is >= the previously popped value, and ANDs the boolean result to a single boolean. If the final boolean is true, the next instruction is skipped (see compare)", "&>=", "greaterequalall");
    Instruction LESS_EQUAL = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        if (!(Instructions.compare(f.getStack().pop(), f.getStack().pop()) <= 0)) {
            f.setCurrent(f.getCurrent() + 1);
        }
    }, "skip the next statement if the top value on the stack is less than the next value on the stack", "Skips the next instruction if a <= b (see compare)", "<=", "lessequal");
    Instruction LESS_EQUAL_ALL = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        boolean check = true;
        CastableValue prev = null;
        while (!f.getStack().isEmpty()) {
            if (prev == null) {
                prev = f.getStack().pop();
            } else {
                CastableValue v = f.getStack().pop();
                check = check && Instructions.compare(v, prev) < 0;
                prev = v;
            }
        }

        if (check) {
            f.setCurrent(f.getCurrent() + 1);
        }
    }, "skip the next statement if the values on the stack are in greatest to smallest order", "Consecutively pops every value of the stack, checks if it is <= the previously popped value, and ANDs the boolean result to a single boolean. If the final boolean is true, the next instruction is skipped (see compare)", "&<=", "lessequalall");
    Instruction NEGATE = new Instruction(f -> {
        Program.checkUnderflow(1, f);
        if (Instructions.truthy(f.getStack().pop())) {
            Instructions.PUSH_TRUTHY.getAction().accept(f);
        } else {
            Instructions.PUSH_FALSY.getAction().accept(f);
        }
    }, "negate the top value of the stack", "Pops the current value of the stack, if the values is truthy, this instruction pushes a falsy value, otherwise this instruction pushes a truthy value (see if)", "!", "negate");
    Instruction IF_TRUTHY = new Instruction(f -> {
        Program.checkUnderflow(1, f);
        CastableValue value = f.getStack().pop();
        if (Instructions.truthy(value)) {
            f.setCurrent(f.getCurrent() + 1);
        }
    }, "if the top value on the stack is truthy, skip the next statement", "Pops the top value of the stack. If a is truthy, skip the next statement. If a is a number, it is truthy if it is greater than 0. If a is a string, it is truthy if it equals \"true,\" \"t\" or \"yes.\" If a is an array, it is truthy if it contains more truthy values than falsy ones", "if");
    Instruction IF_FALSY = new Instruction(f -> {
        Program.checkUnderflow(1, f);
        CastableValue value = f.getStack().pop();
        if (!Instructions.truthy(value)) {
            f.setCurrent(f.getCurrent() + 1);
        }
    }, "if the top value on the stack is falsy, skip the next statement", "Pops the top value of the stack. If a is not truthy, skip the next statement (see if)", "!if");
    Instruction IF_BLOCK = new Instruction(f -> {
        Program.checkUnderflow(1, f);
        FunctionContext context = f.subset("ifblock", "end");
        CastableValue value = f.getStack().pop();
        if (Instructions.truthy(value)) {
            context.runAsSurrogate(f);
        }
    }, "if the top value of the stack is truthy, execute the next block", "Pops the top value of the stack. If a is truthy, run the block (see if). This instruction also opens a new function frame", "ifblock");
    Instruction IF_ELSE_BLOCK = new Instruction(f -> {
        Program.checkUnderflow(1, f);
        FunctionContext truthy = f.subset("ifelse", "else");
        f.setCurrent(f.getCurrent() + truthy.getInstructions().size() + 1);
        FunctionContext falsy = f.subset("else", "end");
        f.setCurrent(f.getCurrent() + falsy.getInstructions().size());
        CastableValue value = f.getStack().pop();
        if (Instructions.truthy(value)) {
            truthy.runAsSurrogate(f);
        } else {
            falsy.runAsSurrogate(f);
        }
    }, "if the top value of the stack is truthy, execute the next block, otherwise, execute the else block", "Pops the top value of the stack. If a is truthy, run the  if block, otherwise run the else block (see if). This instruction also opens a new function frame", "ifelse");
    Instruction ELSE = new Instruction(f -> {
    }, "end the truthy section of the ifelse block", "The end of an ifelse's if block, and the beginning of it's else block", "else");
    Instruction SUPER_PUSH = new Instruction(f -> {
        Program.checkUnderflow(1, f);
        f.getParentStack().push(f.getStack().pop());
    }, "pop the top value of the stack and push it to the parent stack", "Pops the top value of the stack and pushes it to the parent function frame's stack", "superpush");
    Instruction RELATIVE_JUMP = new Instruction(f -> {
        CastableValue indexV = f.getCurrentArgEasy();
        Type.NUMBER.checkMatches(indexV);
        BigDecimal index = indexV.getValueAs(BigDecimal.class).get();
        try {
            f.setCurrent(f.getCurrent() + index.intValueExact());
        } catch (ArithmeticException e) {
            throw new JAISBaLExecutionException("Invalid value: " + String.valueOf(index) + " is not an integer index", e);
        }
    }, Instructions.number(), "jump ${arg} instructions", "Jumps the given amount of instructions forward. The argument may be positive or negative, and the jump will likewise be forwards or backwards. This instruction takes one argument, a number (see pushnum). This instruction fails of the argument is not a 32-bit integer", "jump");
    Instruction INDEX_JUMP = new Instruction(f -> {
        CastableValue indexV = f.getCurrentArgEasy();
        Type.NUMBER.checkMatches(indexV);
        BigDecimal index = indexV.getValueAs(BigDecimal.class).get();
        try {
            f.setCurrent(index.intValueExact());
        } catch (ArithmeticException e) {
            throw new JAISBaLExecutionException("Invalid value: " + String.valueOf(index) + " is not an integer index", e);
        }
    }, Instructions.number(), "jump to instruction ${arg}", "Jumps to the instruction at the given index. This instruction takes one argument, a number (see pushnum). This instruction fails if the argument is not a 32-bit integer", "indexjump");
    Instruction POP_SPLIT_PUSH = new Instruction(f -> {
        Program.checkUnderflow(1, f);
        CastableValue value = f.getStack().pop();
        if (value.getAsString().isPresent()) {
            String[] pieces = value.getAsString().get().split("");
            for (int i = pieces.length - 1; i >= 0; i--) {
                f.getStack().push(CastableValue.of(pieces[i]));
            }
        } else if (value.getValueAs(CastableValue[].class).isPresent()) {
            CastableValue[] pieces = value.getValueAs(CastableValue[].class).get();
            for (int i = pieces.length - 1; i >= 0; i--) {
                f.getStack().push(pieces[i]);
            }
        }
    }, "take the top value off the stack, split it up, and push each piece", "Pops the top value off the stack, splits it, and pushes each piece onto the stack. If the top value is a string or number, it will be converted to a string, and each character of the string will be pushed. If the top value is an array, the values in the array will be pushed in reverse order", "popsplitpush");
    Instruction QUINE = new Instruction(f -> f.getStack().push(CastableValue.of(f.getProgram().getContent())), "load the source code of the program onto the stack", "Pushes the programs source code, as a string, onto the stack", "quine");
    Instruction FUNCTION = new Instruction(new SyntheticFunction(PlasmaListUtil.buildList(Type.STRING), f -> {
        String s = f.getCurrentArgEasy().getAsString().get();
        if (f.getProgram().getFunction(s).isPresent()) {
            f.getProgram().getFunction(s).get().run(f.getStack());
        } else {
            throw new JAISBaLExecutionException("Could not find function " + s);
        }
    }), Instructions.terminated(), "call function ${arg}", "Calls the given function. This instruction takes one argument, terminated by '}' (see pushterm). This instruction fails if the given argument is not a string, or if no function exists for the given name", "f", "call");
    Instruction NAME = new Instruction(f -> {
        Program.checkUnderflow(1, f);
        f.getStack().push(Instructions.name(f.getStack().pop()));
    }, "take the top value off the stack, determines its name, and push it", "Determines the name of the top value on the stack. If a is a 32-bit integer, a string representation of it's number name is returned, if a is an array, the name of every value in the array is computed, and pushed as a single array. Otherwise, the string value of a is pushed", "name");
    BigDecimal SQRT_DIG = new BigDecimal(150);
    BigDecimal SQRT_PRE = new BigDecimal(1).divide(new BigDecimal(10).pow(SQRT_DIG.intValue()));
    BigDecimal TWO = new BigDecimal("2");
    List<String> TRUTHY = PlasmaListUtil.buildList("true", "yes", "t");
    static CastableValue name(CastableValue value) {
        if(value.getValueAs(BigDecimal.class).isPresent()) {
            try {
                return CastableValue.of(NumberNames.convert(value.getValueAs(BigDecimal.class).get().intValueExact()));
            } catch (ArithmeticException e) {
                return CastableValue.of(value.getAsString().get());
            }
        } else if (value.getAsString().isPresent()) {
            return CastableValue.of(value.getAsString().get());
        } else if (value.getValueAs(CastableValue[].class).isPresent()) {
            CastableValue[] array = value.getValueAs(CastableValue[].class).get();
            for (int i = 0; i < array.length; i++) {
                array[i] = CastableValue.of(Instructions.name(array[i]));
            }
            return CastableValue.of(array);
        }

        throw new IllegalStateException();
    }
    static ConstantInstruction constant(CastableValue value, String name) {
        return new ConstantInstruction(value, "push " + name + " onto the stack", "a constant referring that pushes " + name, name.length() == 1 ? new String[0] : new String[]{name});
    }

    static DangerousConsumer<FunctionContext> compose(DangerousConsumer<FunctionContext>... consumers) {
        DangerousConsumer<FunctionContext> consumer = f -> {
        };
        for (DangerousConsumer<FunctionContext> c : consumers) {
            consumer = consumer.andThen(c);
        }
        return consumer;
    }

    static Instruction push(int chars) {
        return new Instruction(f -> f.getStack().push(f.getCurrentArgEasy()), Instructions.fixed(chars), "push ${arg} onto the stack", "Pushes the given argument onto the stack. The argument is considered to be the next " + chars + " character(s) after this instruction", "push" + chars);
    }

    static Instruction pushOutput(int chars) {
        return new Instruction(f -> {
            f.getStack().push(f.getCurrentArgEasy());
            System.out.print(Program.valueToString(f.getStack().pop()));
        }, Instructions.fixed(chars), "print ${arg}", "Pushes the given argument onto the stack, then pops and prints it. The argument is considered to be the next " + chars + " character(s) after this instruction", "print" + chars);
    }

    static Instruction pushOutputNewLine(int chars) {
        return new Instruction(f -> {
            f.getStack().push(f.getCurrentArgEasy());
            System.out.println(Program.valueToString(f.getStack().pop()));
        }, Instructions.fixed(chars), "print ${arg} followed by a new line", "Pushes the given argument onto the stack, then pops and prints it, followed by a new line. The argument is considered to be the next " + chars + " character(s) after this instruction", "println" + chars);
    }

    static BigDecimal sqrt(BigDecimal c, BigDecimal xn, BigDecimal precision) {
        BigDecimal fx = xn.pow(2).add(c.negate());
        BigDecimal fpx = xn.multiply(TWO);
        BigDecimal xn1 = fx.divide(fpx, 2 * SQRT_DIG.intValue(), RoundingMode.HALF_DOWN);
        xn1 = xn.add(xn1.negate());
        BigDecimal currentSquare = xn1.pow(2);
        BigDecimal currentPrecision = currentSquare.subtract(c).abs();
        if (currentPrecision.compareTo(precision) < 0) {
            return xn1;
        } else {
            return sqrt(c, xn1, precision);
        }
    }

    static BigDecimal sqrt(BigDecimal c) {
        return sqrt(c, new BigDecimal(1), SQRT_PRE);
    }

    static boolean truthy(CastableValue value) {
        if (value.getValueAs(BigDecimal.class).isPresent()) {
            return value.getValueAs(BigDecimal.class).get().compareTo(BigDecimal.ZERO) > 0;
        } else if (value.getAsString().isPresent()) {
            return Instructions.TRUTHY.contains(value.getAsString().get());
        } else if (value.getValueAs(CastableValue[].class).isPresent()) {
            CastableValue[] array = value.getValueAs(CastableValue[].class).get();
            int truthy = 0;
            int falsy = 0;
            for (CastableValue val : array) {
                if (Instructions.truthy(val)) {
                    truthy++;
                } else {
                    falsy++;
                }
            }
            return truthy > falsy;
        }
        throw new IllegalStateException();
    }

    static CastableValue concat(CastableValue a, CastableValue b) {
        if (a.getAsString().isPresent() && b.getAsString().isPresent()) {
            return CastableValue.of(a.getAsString().get() + b.getAsString().get());
        } else if (a.getValueAs(CastableValue[].class).isPresent() && b.getValueAs(CastableValue[].class).isPresent()) {
            CastableValue[] ar = a.getValueAs(CastableValue[].class).get();
            CastableValue[] br = b.getValueAs(CastableValue[].class).get();
            CastableValue[] newa = new CastableValue[ar.length + br.length];
            int ind = 0;
            for (CastableValue anAr : ar) {
                newa[ind] = anAr;
                ind++;
            }
            for (CastableValue aBr : br) {
                newa[ind] = aBr;
                ind++;
            }
            return CastableValue.of(newa);
        } else if ((a.getValueAs(CastableValue[].class).isPresent() && !b.getValueAs(CastableValue[].class).isPresent()) || (b.getValueAs(CastableValue[].class).isPresent() && !a.getValueAs(CastableValue[].class).isPresent())) {
            CastableValue[] array = a.getValueAs(CastableValue[].class).isPresent() ? a.getValueAs(CastableValue[].class).get() : b.getValueAs(CastableValue[].class).get();
            CastableValue scalar = a.getValueAs(CastableValue[].class).isPresent() ? b : a;
            if (a.getValueAs(CastableValue[].class).isPresent()) {
                CastableValue[] newa = new CastableValue[array.length + 1];
                System.arraycopy(array, 0, newa, 0, array.length);
                newa[newa.length - 1] = scalar;
                return CastableValue.of(newa);
            } else {
                CastableValue[] newa = new CastableValue[array.length + 1];
                newa[0] = scalar;
                System.arraycopy(array, 0, newa, 1, array.length);
                return CastableValue.of(newa);
            }
        }
        throw new IllegalStateException();
    }

    static CastableValue div(CastableValue a, CastableValue b) {
        if (a.getValueAs(BigDecimal.class).isPresent() && b.getValueAs(BigDecimal.class).isPresent()) {
            BigDecimal ae = a.getValueAs(BigDecimal.class).get();
            BigDecimal be = b.getValueAs(BigDecimal.class).get();
            return CastableValue.of(ae.divide(be, ae.scale() + be.scale(), BigDecimal.ROUND_FLOOR).stripTrailingZeros());
        } else if (a.getAsString().isPresent() && b.getAsString().isPresent()) {
            int i = 0;
            String as = a.getAsString().get();
            String bs = b.getAsString().get();
            for (char c : as.toCharArray()) {
                if (!bs.contains(String.valueOf(c))) {
                    i++;
                }
            }
            return CastableValue.of(new BigDecimal(i));
        } else if ((a.getValueAs(CastableValue[].class).isPresent() && !b.getValueAs(CastableValue[].class).isPresent()) || (b.getValueAs(CastableValue[].class).isPresent() && !a.getValueAs(CastableValue[].class).isPresent())) {
            CastableValue[] array = a.getValueAs(CastableValue[].class).isPresent() ? a.getValueAs(CastableValue[].class).get() : b.getValueAs(CastableValue[].class).get();
            CastableValue scalar = a.getValueAs(CastableValue[].class).isPresent() ? b : a;
            for (int i = 0; i < array.length; i++) {
                array[i] = Instructions.div(array[i], scalar);
            }
            return CastableValue.of(array);
        } else if (a.getValueAs(CastableValue[].class).isPresent() && b.getValueAs(CastableValue[].class).isPresent()) {
            CastableValue[] ar = a.getValueAs(CastableValue[].class).get();
            CastableValue[] br = a.getValueAs(CastableValue[].class).get();
            CastableValue[] newa = new CastableValue[Math.min(ar.length, br.length)];
            for (int i = 0; i < newa.length; i++) {
                if (i < ar.length && i < br.length) {
                    newa[i] = Instructions.div(ar[i], br[i]);
                } else if (i < ar.length) {
                    newa[i] = ar[i];
                } else if (i < br.length) {
                    newa[i] = br[i];
                }
                return CastableValue.of(newa);
            }
        }
        throw new IllegalStateException();
    }

    static CastableValue sub(CastableValue a, CastableValue b) {
        if (a.getValueAs(BigDecimal.class).isPresent() && b.getValueAs(BigDecimal.class).isPresent()) {
            return CastableValue.of(a.getValueAs(BigDecimal.class).get().subtract(b.getValueAs(BigDecimal.class).get()).stripTrailingZeros());
        } else if (a.getAsString().isPresent() && b.getAsString().isPresent()) {
            int i = 0;
            String as = a.getAsString().get();
            String bs = b.getAsString().get();
            while (as.contains(bs)) {
                as = as.replaceFirst(Pattern.quote(as), "");
                i++;
            }
            return CastableValue.of(new BigDecimal(i));
        } else if ((a.getValueAs(CastableValue[].class).isPresent() && !b.getValueAs(CastableValue[].class).isPresent()) || (b.getValueAs(CastableValue[].class).isPresent() && !a.getValueAs(CastableValue[].class).isPresent())) {
            CastableValue[] array = a.getValueAs(CastableValue[].class).isPresent() ? a.getValueAs(CastableValue[].class).get() : b.getValueAs(CastableValue[].class).get();
            CastableValue scalar = a.getValueAs(CastableValue[].class).isPresent() ? b : a;
            for (int i = 0; i < array.length; i++) {
                array[i] = Instructions.sub(array[i], scalar);
            }
            return CastableValue.of(array);
        } else if (a.getValueAs(CastableValue[].class).isPresent() && b.getValueAs(CastableValue[].class).isPresent()) {
            CastableValue[] ar = a.getValueAs(CastableValue[].class).get();
            CastableValue[] br = a.getValueAs(CastableValue[].class).get();
            CastableValue[] newa = new CastableValue[Math.min(ar.length, br.length)];
            for (int i = 0; i < newa.length; i++) {
                if (i < ar.length && i < br.length) {
                    newa[i] = Instructions.sub(ar[i], br[i]);
                } else if (i < ar.length) {
                    newa[i] = ar[i];
                } else if (i < br.length) {
                    newa[i] = br[i];
                }
                return CastableValue.of(newa);
            }
        }
        throw new IllegalStateException();
    }

    static CastableValue add(CastableValue a, CastableValue b) {
        if (a.getValueAs(BigDecimal.class).isPresent() && b.getValueAs(BigDecimal.class).isPresent()) {
            return CastableValue.of(a.getValueAs(BigDecimal.class).get().add(b.getValueAs(BigDecimal.class).get()).stripTrailingZeros());
        } else if (a.getAsString().isPresent() && b.getAsString().isPresent()) {
            String as = a.getAsString().get();
            String bs = b.getAsString().get();
            int i = 0;
            for (String s : PlasmaStringUtil.allPossibleSubs(bs)) {
                if (as.contains(s) && s.length() > i) {
                    i = s.length();
                }
            }
            return CastableValue.of(new BigDecimal(i));
        } else if ((a.getValueAs(CastableValue[].class).isPresent() && !b.getValueAs(CastableValue[].class).isPresent()) || (b.getValueAs(CastableValue[].class).isPresent() && !a.getValueAs(CastableValue[].class).isPresent())) {
            CastableValue[] array = a.getValueAs(CastableValue[].class).isPresent() ? a.getValueAs(CastableValue[].class).get() : b.getValueAs(CastableValue[].class).get();
            CastableValue scalar = a.getValueAs(CastableValue[].class).isPresent() ? b : a;
            for (int i = 0; i < array.length; i++) {
                array[i] = Instructions.add(array[i], scalar);
            }
            return CastableValue.of(array);
        } else if (a.getValueAs(CastableValue[].class).isPresent() && b.getValueAs(CastableValue[].class).isPresent()) {
            CastableValue[] ar = a.getValueAs(CastableValue[].class).get();
            CastableValue[] br = a.getValueAs(CastableValue[].class).get();
            CastableValue[] newa = new CastableValue[Math.max(ar.length, br.length)];
            for (int i = 0; i < newa.length; i++) {
                if (i < ar.length && i < br.length) {
                    newa[i] = Instructions.add(ar[i], br[i]);
                } else if (i < ar.length) {
                    newa[i] = ar[i];
                } else if (i < br.length) {
                    newa[i] = br[i];
                }
                return CastableValue.of(newa);
            }
        }
        throw new IllegalStateException();
    }

    static CastableValue mul(CastableValue a, CastableValue b) {
        if (a.getValueAs(BigDecimal.class).isPresent() && b.getValueAs(BigDecimal.class).isPresent()) {
            return CastableValue.of(a.getValueAs(BigDecimal.class).get().multiply(b.getValueAs(BigDecimal.class).get()).stripTrailingZeros());
        } else if ((a.getAsString().isPresent() && b.getValueAs(BigDecimal.class).isPresent()) || (b.getAsString().isPresent() && a.getValueAs(BigDecimal.class).isPresent())) {
            BigDecimal bd = b.getValueAs(BigDecimal.class).isPresent() ? b.getValueAs(BigDecimal.class).get() : a.getValueAs(BigDecimal.class).get();
            String s = b.getValueAs(BigDecimal.class).isPresent() ? a.getAsString().get() : b.getAsString().get();
            StringBuilder builder = new StringBuilder();
            BigInteger i = bd.toBigInteger();
            for (BigInteger j = BigInteger.ZERO; j.compareTo(i) < 0; j = j.add(BigInteger.ONE)) {
                builder.append(s);
            }
            int additional = (int) (s.length() * Instructions.getFraction(bd).doubleValue());
            for (int j = 0; j < additional && j < s.length(); j++) {
                builder.append(s.charAt(j));
            }
            return CastableValue.of(builder.toString());
        } else if (a.getAsString().isPresent() && b.getAsString().isPresent()) {
            int i = 0;
            String as = a.getAsString().get();
            String bs = b.getAsString().get();
            for (char c : as.toCharArray()) {
                if (bs.contains(String.valueOf(c))) {
                    i++;
                }
            }
            return CastableValue.of(new BigDecimal(i));
        } else if ((a.getValueAs(CastableValue[].class).isPresent() && !b.getValueAs(CastableValue[].class).isPresent()) || (b.getValueAs(CastableValue[].class).isPresent() && !a.getValueAs(CastableValue[].class).isPresent())) {
            CastableValue[] array = a.getValueAs(CastableValue[].class).isPresent() ? a.getValueAs(CastableValue[].class).get() : b.getValueAs(CastableValue[].class).get();
            CastableValue scalar = a.getValueAs(CastableValue[].class).isPresent() ? b : a;
            for (int i = 0; i < array.length; i++) {
                array[i] = Instructions.mul(array[i], scalar);
            }
            return CastableValue.of(array);
        } else if (a.getValueAs(CastableValue[].class).isPresent() && b.getValueAs(CastableValue[].class).isPresent()) {
            CastableValue[] ar = a.getValueAs(CastableValue[].class).get();
            CastableValue[] br = a.getValueAs(CastableValue[].class).get();
            CastableValue[] newa = new CastableValue[Math.max(ar.length, br.length)];
            for (int i = 0; i < newa.length; i++) {
                if (i < ar.length && i < br.length) {
                    newa[i] = Instructions.mul(ar[i], br[i]);
                } else if (i < ar.length) {
                    newa[i] = ar[i];
                } else if (i < br.length) {
                    newa[i] = br[i];
                }
            }
            return CastableValue.of(newa);
        }
        throw new IllegalStateException();
    }

    static BigDecimal getFraction(BigDecimal b) {
        return b.abs().subtract(b.abs().setScale(0, BigDecimal.ROUND_FLOOR).abs()).abs();
    }

    static int compare(CastableValue a, CastableValue b) {
        if (a.getValueAs(CastableValue[].class).isPresent() && b.getValueAs(CastableValue[].class).isPresent()) {
            CastableValue[] aa = a.getValueAs(CastableValue[].class).get();
            CastableValue[] bb = b.getValueAs(CastableValue[].class).get();
            int compare = 0;
            for (int i = 0; i < Math.min(aa.length, bb.length); i++) {
                compare += Instructions.compare(aa[i], bb[i]);
            }
            compare += aa.length - bb.length;
            return compare;
        } else if (a.getValueAs(CastableValue[].class).isPresent()) {
            return 1;
        } else if (b.getValueAs(CastableValue[].class).isPresent()) {
            return -1;
        } else if (a.getValueAs(BigDecimal.class).isPresent() && b.getValueAs(BigDecimal.class).isPresent()) {
            return a.getValueAs(BigDecimal.class).get().compareTo(b.getValueAs(BigDecimal.class).get());
        } else {
            return a.getAsString().get().compareTo(b.getAsString().get());
        }
    }

    static DangerousFunction<CharacterStream, String> fixed(int i) {
        return c -> c.next(i);
    }

    static DangerousFunction<CharacterStream, String> number() {
        return c -> c.nextWhile((Predicate<String>) PlasmaMathUtil::isBigDecimal);
    }

    static DangerousFunction<CharacterStream, String> terminated() {
        return c -> {
            BracketCounter counter = new BracketCounter();
            counter.registerBrackets('[', ']');
            String s = c.nextUntil(z -> z == '}', counter, new QuotationTracker(), Program.ESCAPER, false);
            c.consume('}');
            return s;
        };
    }
}
