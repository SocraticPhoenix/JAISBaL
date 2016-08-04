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
package com.gmail.socraticphoenix.jaisbal.program.instructions.instructions;

import com.gmail.socraticphoenix.jaisbal.JAISBaL;
import com.gmail.socraticphoenix.jaisbal.app.util.JAISBaLExecutionException;
import com.gmail.socraticphoenix.jaisbal.program.Program;
import com.gmail.socraticphoenix.jaisbal.program.State;
import com.gmail.socraticphoenix.jaisbal.program.Type;
import com.gmail.socraticphoenix.jaisbal.program.instructions.Instruction;
import com.gmail.socraticphoenix.jaisbal.program.instructions.util.InstructionUtility;
import com.gmail.socraticphoenix.jaisbal.program.instructions.util.SyntheticFunction;
import com.gmail.socraticphoenix.plasma.collection.PlasmaListUtil;
import com.gmail.socraticphoenix.plasma.reflection.CastableValue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public interface StackInstructions {

    static Instruction push(int chars) {
        return new Instruction(f -> {
            f.getStack().push(f.getCurrentArgEasy());
            return State.NORMAL;
        }, InstructionUtility.fixed(chars), 0.01, "push ${arg} onto the stack", "Pushes the given argument onto the stack. The argument is considered to be the next " + chars + " character(s) after this instruction", "push" + chars);
    }

    static Instruction pushOutput(int chars) {
        return new Instruction(f -> {
            f.getStack().push(f.getCurrentArgEasy());
            JAISBaL.getOut().print(Program.valueToString(f.getStack().pop()));
            return State.NORMAL;
        }, InstructionUtility.fixed(chars), 1.01, "print ${arg}", "Pushes the given argument onto the stack, then pops and prints it. The argument is considered to be the next " + chars + " character(s) after this instruction", "print" + chars);
    }

    static Instruction pushOutputNewLine(int chars) {
        return new Instruction(f -> {
            f.getStack().push(f.getCurrentArgEasy());
            JAISBaL.getOut().println(Program.valueToString(f.getStack().pop()));
            return State.NORMAL;
        }, InstructionUtility.fixed(chars), 1.02, "print ${arg} followed by a new line", "Pushes the given argument onto the stack, then pops and prints it, followed by a new line. The argument is considered to be the next " + chars + " character(s) after this instruction", "println" + chars);
    }

    interface Manipulators { //Group 0
        //Pushes, sub group .01
        Instruction PUSH_NUMBER = new Instruction(f -> {
            f.getStack().push(f.getCurrentArgEasy());
            return State.NORMAL;
        }, InstructionUtility.number(), 0.01, "push ${arg} onto the stack", "Pushes a number onto the stack. This instruction takes one argument, a number, and continues reading the argument until the number literal terminates", "pushnum");
        Instruction PUSH_TERMINATED = new Instruction(f -> {
            f.getStack().push(f.getCurrentArgEasy());
            return State.NORMAL;
        }, InstructionUtility.terminated(), 0.01, "push ${arg} onto the stack", "Pushes any value onto the stack. This instruction takes one argument, and continues reading the argument until the '}' terminating character is found. '}' can be escaped or nested in [] to allow it to be used in the value itself", "pushterm");
        Instruction PUSH_NEW_LINE = new Instruction(f -> {
            f.getStack().push(CastableValue.of(System.lineSeparator()));
            return State.NORMAL;
        }, 0.01, "push a new line", "Pushes a newline onto the stack", "pushln");
        Instruction PUSH_SPACE = new Instruction(f -> {
            f.getStack().push(CastableValue.of(" "));
            return State.NORMAL;
        }, 0.01, "push a space", "Pushes a space onto the stack", "pushspace");
        Instruction PUSH_TAB = new Instruction(f -> {
            f.getStack().push(CastableValue.of("\t"));
            return State.NORMAL;
        }, 0.01, "push a tab", "Pushes a tab onto the stack", "pushtab");
        Instruction PUSH_1_CHAR = StackInstructions.push(1);
        Instruction PUSH_2_CHAR = StackInstructions.push(2);
        Instruction PUSH_3_CHAR = StackInstructions.push(3);
        Instruction PUSH_4_CHAR = StackInstructions.push(4);
        Instruction PUSH_5_CHAR = StackInstructions.push(5);
        Instruction PUSH_6_CHAR = StackInstructions.push(6);


        //Pops, sub group .02
        Instruction POP = new Instruction(f -> {
            Program.checkUnderflow(1, f);
            f.getStack().pop();
            return State.NORMAL;
        }, 0.02, "pop the top value off the stack", "Pops the top value off the stack", "pop");
        Instruction POP_ALL = new Instruction(f -> {
            while (!f.getStack().isEmpty()) {
                f.getStack().pop();
            }
            return State.NORMAL;
        }, 0.02, "clear the stack", "Pops every value off the stack", "popall");
        Instruction POP_ALL_BUT_ONE = new Instruction(f -> {
            while (f.getStack().size() > 1) {
                f.getStack().pop();
            }
            return State.NORMAL;
        }, 0.02, "pop every value of the stack, except for the bottom", "Pops every value off the stack, except for the bottom", "popmost");

        //Swaps, sub group .03
        Instruction SWAP = new Instruction(f -> {
            Program.checkUnderflow(2, f);
            CastableValue top = f.getStack().pop();
            CastableValue under = f.getStack().pop();
            f.getStack().push(top);
            f.getStack().push(under);
            return State.NORMAL;
        }, 0.03, "swap the top two values of the stack", "Pops the top two values off the stack, and then pushes them in reverse order", "swap");
        Instruction SWAP_ALL = new Instruction(f -> {
            int ind = 0;
            CastableValue[] array = new CastableValue[f.getStack().size()];
            while (!f.getStack().isEmpty()) {
                array[ind] = f.getStack().pop();
                ind++;
            }

            for (CastableValue value : array) {
                f.getStack().push(value);
            }
            return State.NORMAL;
        }, 0.03, "swap the entire stack", "Pops every value off the stack and pushes them all back in reverse order.", "swapall");

        //Multipliers, sub group .04
        Instruction DUPLICATE = new Instruction(f -> {
            Program.checkUnderflow(1, f);
            CastableValue value = f.getStack().pop();
            f.getStack().push(value);
            f.getStack().push(value);
            return State.NORMAL;
        }, 0.04, "duplicate the top value of the stack", "Pops the top value off the stack, and then pushes it twice", "dup", "duplicate");
        Instruction DUPLICATE_ALL = new Instruction(f -> {
            List<CastableValue> values = new ArrayList<>();
            while (!f.getStack().isEmpty()) {
                values.add(f.getStack().pop());
            }
            values = PlasmaListUtil.reverseList(values);
            values.stream().forEach(f.getStack()::push);
            values.stream().forEach(f.getStack()::push);
            return State.NORMAL;
        }, 0.04, "duplicate the entire stack", "Pops the entire stack, and then pushes every value to the stack, twice. For example, if the stack was [1, 2, 3], it would become [1, 2, 3, 1, 2, 3]", "dupall", "duplicateall");
        Instruction TRIPLICATE = new Instruction(f -> {
            Program.checkUnderflow(1, f);
            CastableValue value = f.getStack().pop();
            f.getStack().push(value);
            f.getStack().push(value);
            f.getStack().push(value);
            return State.NORMAL;
        }, 0.04, "triplicate the top value of the stack", "Pops the top value off the stack, and then pushes it thrice", "tri", "triplicate");
        Instruction TRIPLICATE_ALL = new Instruction(f -> {
            List<CastableValue> values = new ArrayList<>();
            while (!f.getStack().isEmpty()) {
                values.add(f.getStack().pop());
            }
            values = PlasmaListUtil.reverseList(values);
            values.stream().forEach(f.getStack()::push);
            values.stream().forEach(f.getStack()::push);
            values.stream().forEach(f.getStack()::push);
            return State.NORMAL;
        }, 0.04, "triplicate the entire stack", "Pops the entire stack, and then pushes every value to the stack, thrice. For example, if the stack was [1, 2], it would become [1, 2, 1, 2, 1, 2]", "triall", "triplicateall");
        Instruction DUPLICATE_MANY = new Instruction(f -> {
            CastableValue value = f.getCurrentArgEasy();
            Type.NUMBER.checkMatches(value);
            Program.checkUnderflow(1, f);

            BigInteger val = value.getValueAs(BigDecimal.class).get().toBigInteger();
            CastableValue toDup = f.getStack().pop();

            for (BigInteger i = BigInteger.ZERO; i.compareTo(val) < 0; i = i.add(BigInteger.ONE)) {
                f.getStack().push(toDup);
            }
            return State.NORMAL;
        }, InstructionUtility.number(), 0.04, "duplicate the top value of the stack ${arg} times", "Takes a and pushes as many times as the argument given specifies. This instruction takes one argument, a number (see pushnum)", "dupmany");
        Instruction DUPLICATE_MANY_STACK = new Instruction(f -> {
            Program.checkUnderflow(2, f);
            CastableValue value = f.getStack().pop();
            Type.NUMBER.checkMatches(value);

            BigInteger val = value.getValueAs(BigDecimal.class).get().toBigInteger();
            CastableValue toDup = f.getStack().pop();

            for (BigInteger i = BigInteger.ZERO; i.compareTo(val) < 0; i = i.add(BigInteger.ONE)) {
                f.getStack().push(toDup);
            }
            return State.NORMAL;
        }, InstructionUtility.number(), 0.04, "duplicate the second value of the stack <top value of stack> times", "Duplicates b, a times. Fails if b is not a number.", "dupmanys");


        //Register manipulators, sub group .05
        Instruction STORE = new Instruction(f -> {
            Program.checkUnderflow(1, f);
            CastableValue indexV = f.getCurrentArgEasy();
            Type.NUMBER.checkMatches(indexV);
            CastableValue value = f.getStack().pop();
            BigDecimal index = indexV.getValueAs(BigDecimal.class).get();
            try {
                f.getLocals().put(index.longValue(), value);
            } catch (ArithmeticException e) {
                throw new JAISBaLExecutionException("Invalid value: " + String.valueOf(index) + " is not an integer index, or is too large", e);
            }
            return State.NORMAL;
        }, InstructionUtility.number(), 0.05, "store the top value of the stack into var${arg}", "Pops the top value off the stack and stores it in the given var. This instruction takes one argument, a number (see pushnum)", "store");
        Instruction LOAD = new Instruction(f -> {
            CastableValue indexV = f.getCurrentArgEasy();
            Type.NUMBER.checkMatches(indexV);
            BigDecimal index = indexV.getValueAs(BigDecimal.class).get();
            try {
                if (f.getLocals().get(index.longValue()) != null) {
                    f.getStack().push(f.getLocals().get(index.longValue()));
                } else {
                    throw new JAISBaLExecutionException("Invalid value: " + String.valueOf(index) + " does not lead to a registered local variable");
                }
            } catch (ArithmeticException e) {
                throw new JAISBaLExecutionException("Invalid value: " + String.valueOf(index) + " is not an integer index, or is too large", e);
            }
            return State.NORMAL;
        }, InstructionUtility.number(), 0.05, "push the value in var${arg} onto the stack", "Pushes the value in the given var onto the stack. This instruction takes on argument, a number (see pushnum)", "load");

        Instruction STORE_STACK = new Instruction(f -> {
            Program.checkUnderflow(2, f);
            CastableValue indexV = f.getStack().pop();
            Type.NUMBER.checkMatches(indexV);
            CastableValue value = f.getStack().pop();
            BigDecimal index = indexV.getValueAs(BigDecimal.class).get();
            try {
                f.getLocals().put(index.longValue(), value);
            } catch (ArithmeticException e) {
                throw new JAISBaLExecutionException("Invalid value: " + String.valueOf(index) + " is not an integer index, or is too large", e);
            }
            return State.NORMAL;
        }, InstructionUtility.number(), 0.05, "store the second value in the stack at var<top value of stack>", "Pops the top two values off the stack, and stores b in var a", "sstore");
        Instruction LOAD_STACK = new Instruction(f -> {
            Program.checkUnderflow(1, f);
            CastableValue indexV = f.getStack().pop();
            Type.NUMBER.checkMatches(indexV);
            BigDecimal index = indexV.getValueAs(BigDecimal.class).get();
            try {
                if (f.getLocals().get(index.longValue()) != null) {
                    f.getStack().push(f.getLocals().get(index.longValue()));
                } else {
                    throw new JAISBaLExecutionException("Invalid value: " + String.valueOf(index) + " does not lead to a registered local variable");
                }
            } catch (ArithmeticException e) {
                throw new JAISBaLExecutionException("Invalid value: " + String.valueOf(index) + " is not an integer index, or is too large", e);
            }
            return State.NORMAL;
        }, InstructionUtility.number(), 0.05, "push the value in var<top value of stack> onto the stack", "Pops the top value off the stack and loads the value in var a onto the stack", "sload");
        Instruction STORE_ALL = new Instruction(f -> {
            for (long i = f.getStack().size() - 1; !f.getStack().isEmpty(); i--) {
                f.getLocals().put(i, f.getStack().pop());
            }
            return State.NORMAL;
        }, 0.05, "store entire stack in var0 through var<stack size>", "Pops every value off the stack, and stores it in a var. The first pop is stored in var <size of stack>, and the second is stored in the next var, and so on, until var 0 is reached. This is implemented as such so that a call to storeall, followed by a call to loadall, does not alter the stack", "storeall");
        Instruction LOAD_ALL = new Instruction(f -> {
            f.getLocals().entrySet().stream().sorted((a, b) -> Long.compare(b.getKey(), a.getKey())).forEach(e -> {
                f.getStack().push(e.getValue());
            });
            return State.NORMAL;
        }, 0.05, "load all variables onto the stack", "Loads every value from vars onto the stack, starting at the lowest var index and continuing to the highest", "loadall");
        Instruction STORE_ALL_LOAD_ALL = new Instruction(f -> {
            Manipulators.STORE_ALL.getAction().apply(f);
            Manipulators.LOAD_ALL.getAction().apply(f);
            return State.NORMAL;
        }, 0.05, "store all values on the stack into variables, then load all variables", "Stores all values and then loads all variables (see loadall and storeall)", "storeloadall");
        Instruction IS_FULL = new Instruction(f -> {
            CastableValue value = f.getCurrentArgEasy();
            Type.NUMBER.checkMatches(value);
            if (f.getLocals().containsKey(value.getValueAs(BigDecimal.class).get().longValue())) {
                return ConditionalInstructions.PUSH_TRUTHY.getAction().apply(f);
            } else {
                return ConditionalInstructions.PUSH_FALSEY.getAction().apply(f);
            }
        }, InstructionUtility.number(), 0.05, "push truthy if var${arg} is occupied, falsey otherwise", "Pushes a truthy value if the local variable given in the argument is occupied, falsey otherwise. This isntruction takes one argument, a number (see pushnum)", "isfull");
        Instruction IS_FULL_STACK = new Instruction(new SyntheticFunction(PlasmaListUtil.buildList(Type.NUMBER), f -> {
            CastableValue value = f.getStack().pop();
            if (f.getLocals().containsKey(value.getValueAs(BigDecimal.class).get().longValue())) {
                return ConditionalInstructions.PUSH_TRUTHY.getAction().apply(f);
            } else {
                return ConditionalInstructions.PUSH_FALSEY.getAction().apply(f);
            }
        }), InstructionUtility.number(), 0.05, "push truthy if var<top value of stack> is occupied, falsey otherwise", "Pushes a truthy value if the local variable at index a is occupied, falsey otherwise", "isfulls");
    }

    interface Outputters { //Group 1
        //Push-output, sub group .01
        Instruction PUSH_NUMBER_OUTPUT = new Instruction(f -> {
            f.getStack().push(f.getCurrentArgEasy());
            JAISBaL.getOut().print(Program.valueToString(f.getStack().pop()));
            return State.NORMAL;
        }, InstructionUtility.number(), 1.01, "print ${arg}", "Pushes a number onto the stack (see pushnum), and then pops and prints the number", "printnum");
        Instruction PUSH_TERMINATED_OUTPUT = new Instruction(f -> {
            f.getStack().push(f.getCurrentArgEasy());
            JAISBaL.getOut().print(Program.valueToString(f.getStack().pop()));
            return State.NORMAL;
        }, InstructionUtility.terminated(), 1.01, "print ${arg}", "Pushes any value onto the stack (see pushterm), and then pops and prints the value", "printterm");
        Instruction NEW_LINE = new Instruction(f -> {
            JAISBaL.getOut().println();
            return State.NORMAL;
        }, 1.01, "print a new line", "Prints a newline", "ln");
        Instruction SPACE = new Instruction(f -> {
            JAISBaL.getOut().print(" ");
            return State.NORMAL;
        }, 1.01, "print a space", "Prints a space", "space");
        Instruction TAB = new Instruction(f -> {
            JAISBaL.getOut().print("\t");
            return State.NORMAL;
        }, 1.01, "print a tab", "Prints a tab", "tab");
        Instruction PUSH_1_OUTPUT = StackInstructions.pushOutput(1);
        Instruction PUSH_2_OUTPUT = StackInstructions.pushOutput(2);
        Instruction PUSH_3_OUTPUT = StackInstructions.pushOutput(3);
        Instruction PUSH_4_OUTPUT = StackInstructions.pushOutput(4);
        Instruction PUSH_5_OUTPUT = StackInstructions.pushOutput(5);
        Instruction PUSH_6_OUTPUT = StackInstructions.pushOutput(6);


        //Push-output-ln, sub group .02
        Instruction PUSH_NUMBER_OUTPUT_NEWLINE = new Instruction(f -> {
            f.getStack().push(f.getCurrentArgEasy());
            JAISBaL.getOut().println(Program.valueToString(f.getStack().pop()));
            return State.NORMAL;
        }, InstructionUtility.number(), 1.02, "print ${arg}", "Pushes a number onto the stack (see pushnum), and then pops and prints the number, followed by a newline", "printnumln");
        Instruction PUSH_TERMINATED_OUTPUT_NEWLINE = new Instruction(f -> {
            f.getStack().push(f.getCurrentArgEasy());
            JAISBaL.getOut().println(Program.valueToString(f.getStack().pop()));
            return State.NORMAL;
        }, InstructionUtility.terminated(), 1.02, "print ${arg}", "Pushes any value onto the stack (see pushterm), and then pops and prints the value, followed by a newline", "printtermln");
        Instruction PUSH_1_OUTPUT_LN = StackInstructions.pushOutputNewLine(1);
        Instruction PUSH_2_OUTPUT_LN = StackInstructions.pushOutputNewLine(2);
        Instruction PUSH_3_OUTPUT_LN = StackInstructions.pushOutputNewLine(3);
        Instruction PUSH_4_OUTPUT_LN = StackInstructions.pushOutputNewLine(4);
        Instruction PUSH_5_OUTPUT_LN = StackInstructions.pushOutputNewLine(5);
        Instruction PUSH_6_OUTPUT_LN = StackInstructions.pushOutputNewLine(6);

        //Pop output, sub group .03
        Instruction POP_OUTPUT = new Instruction(f -> {
            Program.checkUnderflow(1, f);
            JAISBaL.getOut().print(Program.valueToString(f.getStack().pop()));
            return State.NORMAL;
        }, 1.03, "pop the top value of a stack and print it", "Pops the top value off the stack and prints it", "popout");
        Instruction POP_OUTPUT_ALL = new Instruction(f -> {
            while (!f.getStack().isEmpty()) {
                JAISBaL.getOut().print(Program.valueToString(f.getStack().pop()));
            }
            return State.NORMAL;
        }, 1.03, "pop off every value in the stack and print it", "Pops every value off the stack, and prints each one", "popoutall");
        Instruction POP_OUTPUT_RESTORE_ALL = new Instruction(f -> {
            List<CastableValue> popped = new ArrayList<>();
            while (!f.getStack().isEmpty()) {
                CastableValue value = f.getStack().pop();
                JAISBaL.getOut().print(Program.valueToString(value));
                popped.add(value);
            }
            PlasmaListUtil.reverseList(popped).forEach(f.getStack()::push);
            return State.NORMAL;
        }, 1.03, "pop off every value in the stack and print it, then restore the stack", "Pops every value off the stack, and prints each one, then pushes each value back onto the stack.", "popoutallrestore");
        Instruction POP_OUTPUT_NEWLINE = new Instruction(f -> {
            Program.checkUnderflow(1, f);
            JAISBaL.getOut().println(Program.valueToString(f.getStack().pop()));
            return State.NORMAL;
        }, 1.03, "pop the top value off a stack and print it with a new line", "Pops the top value off the stack and prints it, followed by a new line", "popoutln");
        Instruction POP_OUTPUT_ALL_NEWLINE = new Instruction(f -> {
            while (!f.getStack().isEmpty()) {
                JAISBaL.getOut().println(Program.valueToString(f.getStack().pop()));
            }
            return State.NORMAL;
        }, 1.03, "pop off every value in the stack and print each one with a new line", "Pops every value off the stack, and prints each one on a separate line", "popoutallln");
        Instruction POP_OUTPUT_RESTORE_ALL_NEWLINE = new Instruction(f -> {
            List<CastableValue> popped = new ArrayList<>();
            while (!f.getStack().isEmpty()) {
                CastableValue value = f.getStack().pop();
                JAISBaL.getOut().println(Program.valueToString(value));
                popped.add(value);
            }
            PlasmaListUtil.reverseList(popped).forEach(f.getStack()::push);
            return State.NORMAL;
        }, 1.03, "pop off every value in the stack and print each one with a new line, then restore the stack", "Pops every value off the stack, and prints each one on a separate line, then pushes each value back onto the stack.", "popoutalllnrestore");


    }

}
