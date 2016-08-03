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

import com.gmail.socraticphoenix.jaisbal.app.util.JAISBaLExecutionException;
import com.gmail.socraticphoenix.jaisbal.program.Program;
import com.gmail.socraticphoenix.jaisbal.program.State;
import com.gmail.socraticphoenix.jaisbal.program.Type;
import com.gmail.socraticphoenix.jaisbal.program.instructions.Instruction;
import com.gmail.socraticphoenix.jaisbal.program.instructions.util.InstructionUtility;
import com.gmail.socraticphoenix.jaisbal.program.instructions.util.SyntheticFunction;
import com.gmail.socraticphoenix.plasma.collection.PlasmaListUtil;
import com.gmail.socraticphoenix.plasma.reflection.CastableValue;
import com.gmail.socraticphoenix.plasma.string.PlasmaStringUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

public interface ArrayInstructions { //Group 6, for convenience, strings are considered char[] in this organization system (hence string instructions appear here)
    //Base array operations, sub group .01
    Instruction ARRAY_CREATE = new Instruction(f -> {
        CastableValue index = f.getCurrentArgEasy();
        Type.NUMBER.checkMatches(index);
        try {
            CastableValue[] array = new CastableValue[index.getValueAs(BigDecimal.class).get().intValueExact()];
            f.getStack().push(CastableValue.of(array));
        } catch (ArithmeticException e) {
            throw new JAISBaLExecutionException(Program.valueToString(index) + " is not a 32-bit integer index");
        }
        return State.NORMAL;
    }, InstructionUtility.number(), 6.01, "create and push new array with length ${arg}", "Creates a new array with the given length. This instruction takes one argument, a number (see pushnum)", "newarray");
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
        return State.NORMAL;
    }, InstructionUtility.number(), 6.01, "create and push new array with length <top value of stack>", "Creates a new array with the length a. This instruction fails if a is not a 32-bit integer index", "snewarray");
    Instruction ARRAY_LENGTH = new Instruction(new SyntheticFunction(PlasmaListUtil.buildList(Type.GENERAL_ARRAY), f -> {
        CastableValue value = f.getStack().pop();
        f.getStack().push(value);
        f.getStack().push(CastableValue.of(new BigDecimal(value.getValueAs(CastableValue[].class).get().length)));
        return State.NORMAL;
    }), 6.01, "push the length of the array onto the stack", "Pushes the length of array a onto the stack. This instruction does not pop of the array. This instruction fails if a is not an array", "arraylength", "alength");

    //Array register operations, sub group .02
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
        return State.NORMAL;
    }, InstructionUtility.number(), 6.02, "load a value from an array, using the top value of the stack as an index", "Loads a value from array a, from the specified index. This instruction does not pop of the array. This instruction takes one argument, a number (see pushnum). This instruction is only succesful if a is an array, and the argument given is a 32-bit integer that is an index of a", "arrayload", "aload");
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
        return State.NORMAL;
    }, InstructionUtility.number(), 6.02, "store the top of the stack at index ${arg} in an array", "Stores value a in array b, at the specified index. This instruction does not pop of the array. This instruction takes on argument, a number (see pushnum). This instruction is only succesful if b is an array, and the argument given is a 32-bit integer that is an index of b", "arraystore", "astore");

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
        return State.NORMAL;
    }, InstructionUtility.number(), 6.02, "load the value at index ${arg} from an array", "Loads a value from array b, at index a. This instruction does not pop of the array. This instruction is only succesful if b is an array and a is a 32-bit integer that is an index in b", "sarrayload", "saload");
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
        return State.NORMAL;
    }, InstructionUtility.number(), 6.02, "store the second value in the stack in an array, using the top value of the stack as an index", "Stores value b in array c at index a. This instruction does not pop of the array. This instruction is only succesful if c is an array and a is 32-bit integer that is an index in b", "sarraystore", "sastore");

    //List operations, sub group .03
    Instruction ARRAY_SORT = new Instruction(new SyntheticFunction(PlasmaListUtil.buildList(Type.GENERAL_ARRAY), f -> {
        CastableValue[] array = f.getStack().pop().getValueAs(CastableValue[].class).get();
        Arrays.sort(array, InstructionUtility::compare);
        f.getStack().push(CastableValue.of(array));
        return State.NORMAL;
    }), 6.03, "pop the top value of the stack, sort it, and push it", "Pops the top value off the stack and sorts it from smallest to largest (see compare). This instruction fails if a is not an array", "sort");
    Instruction ARRAY_SORT_REVERSE = new Instruction(new SyntheticFunction(PlasmaListUtil.buildList(Type.GENERAL_ARRAY), f -> {
        CastableValue[] array = f.getStack().pop().getValueAs(CastableValue[].class).get();
        Arrays.sort(array, (a, b) -> InstructionUtility.compare(b, a));
        f.getStack().push(CastableValue.of(array));
        return State.NORMAL;
    }), 6.03, "pop the top value of the stack, sort it, reverse it, and push it", "Pops the top value off the stack and sorts it from largest to smallest (see compare). This instruction fails if a is not an array", "rsort");
    Instruction ROTATE = new Instruction(new SyntheticFunction(PlasmaListUtil.buildList(Type.GENERAL_ARRAY), f-> {
        CastableValue[] array = f.getStack().pop().getValueAs(CastableValue[].class).get();
        CastableValue[] newArray = new CastableValue[array.length];
        for (int i = 0; i < array.length; i++) {
            int l = i + 1 >= newArray.length ? i + 1 - newArray.length : i + 1;
            newArray[l] = array[l];
        }
        f.getStack().push(CastableValue.of(newArray));
        return State.NORMAL;
    }), 6.03, "rotate the top value of the stack", "Pops a off the stack, and rotates array a to the right, meaning the last value of array a will become the first, the first the second, etc. This instruction fails if a is not an array", "rotate");
    Instruction ROTATE_NUMBER = new Instruction(new SyntheticFunction(PlasmaListUtil.buildList(Type.GENERAL_ARRAY), f-> {
        CastableValue num = f.getCurrentArgEasy();
        Type.NUMBER.checkMatches(num);
        int g = num.getValueAs(BigDecimal.class).get().intValue();
        CastableValue[] array = f.getStack().pop().getValueAs(CastableValue[].class).get();
        CastableValue[] newArray = new CastableValue[array.length];
        for (int i = 0; i < array.length; i++) {
            int l = i + g >= newArray.length ? g + 1 - newArray.length : g + 1;
            newArray[l] = array[l];
        }
        f.getStack().push(CastableValue.of(newArray));
        return State.NORMAL;
    }), 6.03, "rotate the top value of the stack ${arg} time(s) to the right", "Pops a off the stack, and rotates array a to the right, depending on the argument given. Generally, and array of the form [l(n), l(n - 1), l(n - 2)...] will become [l(n + arg), l(n - 1 + arg), l(n - 2 + arg)...], with values at the end of the array wrapping around to the beginning. This instruction fails if a is not an array.", "rotaten");
    Instruction ROTATE_NUMBER_STACK = new Instruction(new SyntheticFunction(PlasmaListUtil.buildList(Type.NUMBER, Type.GENERAL_ARRAY), f-> {
        CastableValue num = f.getStack().pop();
        int g = num.getValueAs(BigDecimal.class).get().intValue();
        CastableValue[] array = f.getStack().pop().getValueAs(CastableValue[].class).get();
        CastableValue[] newArray = new CastableValue[array.length];
        for (int i = 0; i < array.length; i++) {
            int l = i + g >= newArray.length ? g + 1 - newArray.length : g + 1;
            newArray[l] = array[l];
        }
        f.getStack().push(CastableValue.of(newArray));
        return State.NORMAL;
    }), 6.03, "rotate the second value of the stack <top value of stack> times", "Pops a and b off the stack, and rotates array b to the right, a times. Generally, and array of the form [l(n), l(n - 1), l(n - 2)...] will become [;(n + a), l(n - 1 + a), l(n - 2 + a)...], with values at the end of the array wrapping around to the beginning. This instruction fails if a is not an array,", "");
    Instruction ARRAY_RANGED = new Instruction(new SyntheticFunction(PlasmaListUtil.buildList(Type.NUMBER, Type.NUMBER), f -> {
        try {
            BigInteger a = f.getStack().pop().getValueAs(BigDecimal.class).get().toBigInteger();
            BigInteger b = f.getStack().pop().getValueAs(BigDecimal.class).get().toBigInteger();
            BigInteger min = PlasmaListUtil.getMinimum(new BigInteger[]{a, b});
            BigInteger max = PlasmaListUtil.getMaximum(new BigInteger[]{a, b});
            int size = max.subtract(min).intValueExact();
            CastableValue[] array = new CastableValue[size];
            for (int i = 0; i < array.length; i++) {
                array[i] = CastableValue.of(min);
                min = min.add(BigInteger.ONE);
            }
            f.getStack().push(CastableValue.of(array));

        } catch (ArithmeticException | NegativeArraySizeException e) {
            throw new JAISBaLExecutionException("Either the range is too large/small", e);
        }
        return State.NORMAL;
    }), 6.03, "push an array containing all numbers in the range of the two numbers on the top of the stack", "Pops a and b off the stack and pushes an array containing the range between them. The range will be 1-incremented from min(a, b) to max(a, b) - 1. ", "range");
    Instruction ARRAY_RANGED_INCLUSIVE = new Instruction(new SyntheticFunction(PlasmaListUtil.buildList(Type.NUMBER, Type.NUMBER), f -> {
        try {
            BigInteger a = f.getStack().pop().getValueAs(BigDecimal.class).get().toBigInteger();
            BigInteger b = f.getStack().pop().getValueAs(BigDecimal.class).get().toBigInteger();
            BigInteger min = PlasmaListUtil.getMinimum(new BigInteger[]{a, b});
            BigInteger max = PlasmaListUtil.getMaximum(new BigInteger[]{a, b});
            int size = max.subtract(min).intValueExact() + 1;
            CastableValue[] array = new CastableValue[size];
            for (int i = 0; i < array.length; i++) {
                array[i] = CastableValue.of(min);
                min = min.add(BigInteger.ONE);
            }
            f.getStack().push(CastableValue.of(array));

        } catch (ArithmeticException e) {
            throw new JAISBaLExecutionException("Either the range is too large");
        }
        return State.NORMAL;
    }), 6.03, "push an array containing all numbers in the range of the two numbers on the top of the stack, inclusively", "Pops a and b off the stack and pushes an array containing the range between them. The range will be 1-incremented from min(a, b) to max(a, b). ", "rangein");

    //Stack <-> array operatoins, sub group .04
    Instruction ARRAY_WRAP = new Instruction(f -> {
        CastableValue[] array = new CastableValue[f.getStack().size()];
        int ind = array.length - 1;
        while (!f.getStack().isEmpty()) {
            array[ind] = f.getStack().pop();
            ind--;
        }
        f.getStack().push(CastableValue.of(array));
        return State.NORMAL;
    }, 6.04, "take the entire stack and wrap it into an array", "Pops every value of the stack and stores it in an array, in reverse order. This is implemented this way so that subsequent calls to popsplitpush and arrwrap do not modify the stack", "arrwrap");
    Instruction POP_SPLIT_PUSH = new Instruction(f -> {
        Program.checkUnderflow(1, f);
        CastableValue value = f.getStack().pop();
        if (value.getAsString().isPresent()) {
            String[] pieces = value.getAsString().get().split("");
            for(String piece : pieces) {
                f.getStack().push(CastableValue.of(piece));
            }
        } else if (value.getValueAs(CastableValue[].class).isPresent()) {
            CastableValue[] pieces = value.getValueAs(CastableValue[].class).get();
            for(CastableValue piece : pieces) {
                f.getStack().push(piece);
            }
        }
        return State.NORMAL;
    }, 6.04, "take the top value off the stack, split it up, and push each piece", "Pops the top value off the stack, splits it, and pushes each piece onto the stack. If the top value is a string or number, it will be converted to a string, and each character of the string will be pushed. If the top value is an array, the values in the array will be pushed in order", "popsplitpush", "explode");
    Instruction REVERSE = new Instruction(f -> {
        Program.checkUnderflow(1, f);
        CastableValue value = f.getStack().pop();
        if (value.getAsString().isPresent()) {
            f.getStack().push(CastableValue.of(PlasmaStringUtil.reverseString(value.getAsString().get())));
        } else {
            f.getStack().push(CastableValue.of(PlasmaListUtil.reverseList(PlasmaListUtil.buildList(value.getValueAs(CastableValue[].class).get())).toArray(new CastableValue[0])));
        }
        return State.NORMAL;
    }, 6.04, "reverse the top value of the stack", "Pops the top value off the stack and reverses it. If the a is a number or string, it will be converted to a string and the order of characters will be reversed. If a is an array, the order of elements will be reversed", "reverse");

    //Concatenation, sub group .05
    Instruction CONCAT = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        f.getStack().push(InstructionUtility.concat(f.getStack().pop(), f.getStack().pop()));
        return State.NORMAL;
    }, 6.05, "concatenate the top two values of the stack", "Concatenates a and b. If a and b are both numbers, both strings, or one of each, they will be converted to strings and concatenate using string concatenation. If a or b is an array, and the other is a non array, the non array will be prepended (if a is non-rray) or appended (if b is non array). If both a and b are arrays, list concatenation will be used to join a and b. This instruction will fail if a and/or b is an array, and a contains a or b, or b contains a or b", "concat", "&+");
    Instruction PUSH_NEW_LINE_CONCAT = new Instruction(f -> {
        f.getStack().push(CastableValue.of(System.lineSeparator()));
        return ArrayInstructions.CONCAT.getAction().apply(f);
    }, 6.05, "push a new line and concatenate", "Pushes a new line and then concatenates the top two values of the stack (see concat)", "concatln");
    Instruction PUSH_SPACE_CONCAT = new Instruction(f -> {
        f.getStack().push(CastableValue.of(" "));
        return ArrayInstructions.CONCAT.getAction().apply(f);
    }, 6.05, "push a space and concatenate", "Pushes a space and then concatenates the top two values of the stack (see concat)", "concatspace");
    Instruction PUSH_TAB_CONCAT = new Instruction(f -> {
        f.getStack().push(CastableValue.of("\t"));
        return ArrayInstructions.CONCAT.getAction().apply(f);
    }, 6.05, "push a tab and concatenate", "Pushes a tab and then concatenates the top two values of the stack (see concat)", "concattab");

    //Solely string operations, sub group .06
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
        return State.NORMAL;
    }, InstructionUtility.terminated(), 6.06, "split the top value of the stack by ${arg}", "Splits the top value of the stack by the given regex, and pushes the array result. This instruction takes one argument, terminated by '}' (see pushterm). This instruction is only succesful if the top value of the stack is a string or number", "split");
    Instruction SPLIT_STACK = new Instruction(new SyntheticFunction(PlasmaListUtil.buildList(Type.STRING, Type.STRING), f -> {
        String[] pieces = f.getStack().pop().getAsString().get().split(f.getStack().pop().getAsString().get());
        CastableValue[] array = new CastableValue[pieces.length];
        for (int i = 0; i < pieces.length; i++) {
            array[i] = CastableValue.of(pieces[i]);
        }
        f.getStack().push(CastableValue.of(array));
        return State.NORMAL;
    }), 6.06, "split the top value of the stack by the second value on the stack", "Splits a by b, interpreting b as a regex and pushes the array result.", "splits");

}
