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

import com.gmail.socraticphoenix.jaisbal.program.JAISBaLExecutionException;
import com.gmail.socraticphoenix.jaisbal.program.Program;
import com.gmail.socraticphoenix.jaisbal.program.State;
import com.gmail.socraticphoenix.jaisbal.program.Type;
import com.gmail.socraticphoenix.jaisbal.program.instructions.Instruction;
import com.gmail.socraticphoenix.jaisbal.program.instructions.util.InstructionUtility;
import com.gmail.socraticphoenix.jaisbal.program.instructions.util.SyntheticFunction;
import com.gmail.socraticphoenix.jaisbal.program.instructions.vectorization.VectorizedDyad;
import com.gmail.socraticphoenix.jaisbal.program.instructions.vectorization.VectorizedMonad;
import com.gmail.socraticphoenix.plasma.collection.PlasmaListUtil;
import com.gmail.socraticphoenix.plasma.math.IntRange;
import com.gmail.socraticphoenix.plasma.reflection.CastableValue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Random;

public interface MathematicalInstructions {

    interface Operations { //Group 4
        //The basic 4, sub group .01
        Instruction ADD = new Instruction(f -> {
            Program.checkUnderflow(2, f);
            f.getStack().push(InstructionUtility.add(f.getStack().pop(), f.getStack().pop()));
            return State.NORMAL;
        }, 4.01, "add the top two values of the stack", "Adds a and b. If a and b are both numbers, normal addition will occur. If either a or b is an array, and the other is a non-array, the other value will be added to every value in the array. If a or b is a string, and the other is a number, a and b will both be converted to strings and added. If a and b are strings, the length of the longest substring of a that is also present in b will be pushed. Finally, if both values are arrays, a new array will be created with a length of the longer array, and every value in the new array will be the result of addition of same-indexed values in a and b. This instruction will fail if a or b is an array, and a contains a and/or b, or b contains a or b", "+", "add");
        Instruction SUBTRACT = new Instruction(f -> {
            Program.checkUnderflow(2, f);
            f.getStack().push(InstructionUtility.sub(f.getStack().pop(), f.getStack().pop()));
            return State.NORMAL;
        }, 4.01, "subtract the second value on the stack from the top value on the stack", "Subtracts b from a. If a and b are both numbers, normal subtraction will occur. If either a or b is an array, and the other is a non-array, the other value will be subtracted from every value in the array. If a or b is a string, and the other is a number, a and b will both be converted to strings and subtracted. If a and b are strings, the number of times b occurs in a will be pushed. Finally, if both values are arrays, a new array will be created with a length of the smaller array, and ever value in the new array will be the result of subtraction of same-indexed values in a and b. This instruction will fail if a and/or b is an array, and a contains a or b, or b contains a or b", "_", "sub");
        Instruction MULTIPLY = new Instruction(f -> {
            Program.checkUnderflow(2, f);
            f.getStack().push(InstructionUtility.mul(f.getStack().pop(), f.getStack().pop()));
            return State.NORMAL;
        }, 4.01, "multiply the top two values of the stack", "Multiplies a and b. If a and b are both numbers, normal multiplication will occur. If either a or b is an array, and the other is a non-array, every value in the array will be multiplied by the other value. If either a or b is a string, and the other is a number, the string will be duplicated <number> times. If both a and b are strings, the number of characters in a which are also in b will be pushed. Finally, if both values are arrays, a new array will be created with the length of the longer array, and every value in the new array will be the result of multiplication of same-indexed values in a and b. This instruction will fail if a and/or b is an array, and a contains a or b, or b contains a or b", "*", "mul");
        Instruction DIVIDE = new Instruction(f -> {
            Program.checkUnderflow(2, f);
            f.getStack().push(InstructionUtility.div(f.getStack().pop(), f.getStack().pop()));
            return State.NORMAL;
        }, 4.01, "divide the top value of the stack by the second value on the stack", "Divides a by b. If a and b are both numbers, normal division will occur. If either a or b is an array, and the other is a non-array, every value in the array will be divided by the other value. If a or b is a string, and the other is a number, a and b will both be converted to strings and divided. If a and b are both strings, the number of characters in a which are not in b will be pushed. Finally, if both values are arrays, a new array will be created with the length of the smaller array, and every value in the array will be the result of division of same-indexed values in a and b. This instruction will fail if a and/or b is an array, and a contains a or b, or b contains a or b", "/", "div");
        Instruction ADD_ALL = new Instruction(f -> {
            Program.checkUnderflow(1, f);
            CastableValue value = f.getStack().pop();
            while (!f.getStack().isEmpty()) {
                value = InstructionUtility.add(value, f.getStack().pop());
            }
            f.getStack().push(value);
            return State.NORMAL;
        }, 4.01, "add the entire stack together", "Adds (((a + b) + c) + d), and so on, so long as there are still values on the stack (see add for a definition of addition)", "addall");
        Instruction SUBTRACT_ALL = new Instruction(f -> {
            Program.checkUnderflow(1, f);
            CastableValue value = f.getStack().pop();
            while (!f.getStack().isEmpty()) {
                value = InstructionUtility.sub(value, f.getStack().pop());
            }
            f.getStack().push(value);
            return State.NORMAL;
        }, 4.01, "subtract the entire stack", "Subtracts (((a - b) - c) - d), and so on, so long as there are still values on the stack (see sub for a definition of subtraction)", "suball");
        Instruction MULTIPLY_ALL = new Instruction(f -> {
            Program.checkUnderflow(1, f);
            CastableValue value = f.getStack().pop();
            while (!f.getStack().isEmpty()) {
                value = InstructionUtility.mul(value, f.getStack().pop());
            }
            f.getStack().push(value);
            return State.NORMAL;
        }, 4.01, "multiply the entire stack together", "Multiplies (((a * b) * c) * d), and so on, so long as there are still values on the stack (see mul for a definition of multiplication)", "mulall");
        Instruction DIVIDE_ALL = new Instruction(f -> {
            Program.checkUnderflow(1, f);
            CastableValue value = f.getStack().pop();
            while (!f.getStack().isEmpty()) {
                value = InstructionUtility.div(value, f.getStack().pop());
            }
            f.getStack().push(value);
            return State.NORMAL;
        }, 4.01, "divide the entire stack", "Divides (((a / b) / c) / d), and so on, so long as there are still values on the stack (see div for a definition of division)", "divall");
        Instruction INCREMENT = new Instruction(f -> {
            Program.checkUnderflow(1, f);
            f.getStack().push(InstructionUtility.add(f.getStack().pop(), CastableValue.of(BigDecimal.ONE)));
            return State.NORMAL;
        }, 4.01, "increment the top value of the stack", "Takes the top value of the stack and computes (a + 1), and pushes the result", "inc", "++");
        Instruction DECREMENT = new Instruction(f -> {
            Program.checkUnderflow(1, f);
            f.getStack().push(InstructionUtility.sub(f.getStack().pop(), CastableValue.of(BigDecimal.ONE)));
            return State.NORMAL;
        }, 4.01, "decrement the top value of the stack", "Takes the top value of the stack and computes (a - 1), and pushes the result", "dec", "--");

        //Misc operators, sub group .02
        Instruction POW = new Instruction(new VectorizedDyad(new SyntheticFunction(PlasmaListUtil.buildList(Type.NUMBER, Type.NUMBER), f -> {
            BigDecimal a = f.getStack().pop().getValueAs(BigDecimal.class).get();
            BigDecimal b = f.getStack().pop().getValueAs(BigDecimal.class).get();
            if (a.compareTo(BigDecimal.ZERO) < 0) {
                throw new JAISBaLExecutionException("Invalid value: cannot raise negative " + a + " to a power");
            } else {
                f.getStack().push(CastableValue.of(new BigDecimal(Math.pow(a.doubleValue(), b.doubleValue()))));
            }
            return State.NORMAL;
        })), 4.02, "raise the top value on the stack to the second value on the stack", "Raises a to b. This instruction is only succesful if the top two values of the stack are numbers. Furthermore, accurate results can only be calculated for numbers that fit in 32-bits", "pow", "^");
        Instruction MODULO = new Instruction(new VectorizedDyad(new SyntheticFunction(PlasmaListUtil.buildList(Type.NUMBER, Type.NUMBER), f -> {
            BigDecimal a = f.getStack().pop().getValueAs(BigDecimal.class).get();
            BigDecimal b = f.getStack().pop().getValueAs(BigDecimal.class).get();
            f.getStack().push(CastableValue.of(new BigDecimal(a.toBigInteger().mod(b.toBigInteger()))));
            return State.NORMAL;
        })), 4.02, "compute the modulus of the top value on the stack by the second value on the stack", "Calculates a mod b. This instruction is only succesful if the top two values of the stack are integers", "mod", "%");
        Instruction SQRT = new Instruction(new VectorizedMonad(new SyntheticFunction(PlasmaListUtil.buildList(Type.NUMBER), f -> {
            BigDecimal decimal = f.getStack().pop().getValueAs(BigDecimal.class).get();
            f.getStack().push(CastableValue.of(InstructionUtility.sqrt(decimal)));
            return State.NORMAL;
        })), 4.02, "compute the square root of the top value on the stack", "Computes the square root of a, and pushes it to the stack. This instruction fails if a is not a number", "sqrt");
        Instruction ABSOLUTE_VALUE = new Instruction(new VectorizedMonad(new SyntheticFunction(PlasmaListUtil.buildList(Type.NUMBER), f -> {
            f.getStack().push(CastableValue.of(f.getStack().pop().getValueAs(BigDecimal.class).get().abs()));
            return State.NORMAL;
        })), 4.02, "compute the absolute value of the top of the stack", "Pops the top value off the stack, computes its absolute value, and pushes it", "abs");

        //Rounding operations, sub group .03
        Instruction FLOOR = new Instruction(new VectorizedMonad(new SyntheticFunction(PlasmaListUtil.buildList(Type.NUMBER), f -> {
            BigDecimal decimal = f.getStack().pop().getValueAs(BigDecimal.class).get();
            f.getStack().push(CastableValue.of(decimal.setScale(0, RoundingMode.FLOOR)));
            return State.NORMAL;
        })), 4.03, "floor the top value of the stack", "Calculates floor a. This instruction is only succesful if the top value of the stack is a number", "floor");
        Instruction CEIL = new Instruction(new VectorizedMonad(new SyntheticFunction(PlasmaListUtil.buildList(Type.NUMBER), f -> {
            BigDecimal decimal = f.getStack().pop().getValueAs(BigDecimal.class).get();
            f.getStack().push(CastableValue.of(decimal.setScale(0, RoundingMode.CEILING)));
            return State.NORMAL;
        })), 4.03, "ceil the top value of the stack", "Calculates ceil a. This instruction is only succesful if the top value of the stack is a number", "ceil");
        Instruction ROUND = new Instruction(new VectorizedMonad(new SyntheticFunction(PlasmaListUtil.buildList(Type.NUMBER), f -> {
            BigDecimal decimal = f.getStack().pop().getValueAs(BigDecimal.class).get();
            f.getStack().push(CastableValue.of(decimal.setScale(0, RoundingMode.HALF_UP)));
            return State.NORMAL;
        })), 4.03, "round the top value of the stack", "Calculates round a (traditional rounding). This instruction is only succesful if the top value of the stack is a number", "round");
    }

    interface Functions { //Group 5
        //Randomness, sub group .01
        Instruction RAND_DECIMAL = new Instruction(f -> {
            f.getStack().push(CastableValue.of(new BigDecimal(new Random().nextDouble())));
            return State.NORMAL;
        }, 5.01, "push a random decimal in the range [0, 1)", "Pseudorandomly generates a decimal number in the range [0, 1) and pushes it", "randd");
        Instruction RAND_INTEGER = new Instruction(f -> {
            f.getStack().push(CastableValue.of(new BigDecimal(new Random().nextInt())));
            return State.NORMAL;
        }, 5.01, "push a random integer", "Pseudorandomly generates an integer in the range [" + Integer.MIN_VALUE + ", " + Integer.MAX_VALUE + "]", "randi");
        Instruction RAND_INTEGER_BOUNDED = new Instruction(new VectorizedMonad(new SyntheticFunction(PlasmaListUtil.buildList(Type.NUMBER), f -> {
            CastableValue value = f.getStack().pop();
            BigDecimal decimal = value.getValueAs(BigDecimal.class).get();
            f.getStack().push(CastableValue.of(new BigDecimal(IntRange.cleanRandomElement(0, decimal.intValue(), new Random()))));
            return State.NORMAL;
        })), 5.01, "push a random integer in the range [0, <top value of stack>)", "Pseudorandomly generates an integer in the range [0, a) and pushes it", "randib");
        Instruction RAND_INTEGER_BOUNDED_1 = new Instruction(new VectorizedMonad(new SyntheticFunction(PlasmaListUtil.buildList(Type.NUMBER), f -> {
            CastableValue value = f.getStack().pop();
            BigDecimal decimal = value.getValueAs(BigDecimal.class).get();
            f.getStack().push(CastableValue.of(new BigDecimal(IntRange.cleanRandomElement(1, decimal.intValue(), new Random()))));
            return State.NORMAL;
        })), 5.01, "push a random integer in the range [1, <top value of stack>)", "Pseudorandomly generates an integer in the range [1, a) and pushes it", "randi1");
        Instruction RAND_INTEGER_DOUBLE_BOUNDED = new Instruction(new SyntheticFunction(PlasmaListUtil.buildList(Type.NUMBER, Type.NUMBER), f -> {
            CastableValue value = f.getStack().pop();
            CastableValue value2 = f.getStack().pop();

            BigDecimal decimal = value.getValueAs(BigDecimal.class).get();
            BigDecimal decimal2 = value2.getValueAs(BigDecimal.class).get();
            f.getStack().push(CastableValue.of(new BigDecimal(IntRange.cleanRandomElement(Math.min(decimal.intValue(), decimal2.intValue()), Math.max(decimal.intValue(), decimal2.intValue()), new Random()))));
            return State.NORMAL;
        }), 5.01, "push a random integer in the range specified by the top two values of the stack", "Pseudorandomly generates an integer in the range [min(a, b), max(a, b)) and pushes it", "randidb");
        //Mathematical functions
        Instruction FACTORIAL = new Instruction(new VectorizedMonad(new SyntheticFunction(PlasmaListUtil.buildList(Type.NUMBER), f -> {
            BigInteger integer = f.getStack().pop().getValueAs(BigDecimal.class).get().toBigInteger();
            BigInteger val = integer;
            while (integer.compareTo(BigInteger.ONE) > 0) {
                integer = integer.subtract(BigInteger.ONE);
                val = val.multiply(integer);
            }
            f.getStack().push(CastableValue.of(val));
            return State.NORMAL;
        })), 5.02, "compute the factorial of the top value on the stack", "Pops the top value off the stack, computes its factorial, and pushes it", "factorial", "fac");
        Instruction INVERSE = new Instruction(new VectorizedMonad(new SyntheticFunction(PlasmaListUtil.buildList(Type.NUMBER), f -> {
            f.getStack().push(InstructionUtility.div(CastableValue.of(BigDecimal.ONE), f.getStack().pop()));
            return State.NORMAL;
        })), 5.02, "compute 1 / <top of stack>", "Pops the top value of the stack and computes 1 / a, and pushes it", "inverse", "inv");
        Instruction SIGNUM = new Instruction(new VectorizedMonad(new SyntheticFunction(PlasmaListUtil.buildList(Type.NUMBER), f -> {
            f.getStack().push(CastableValue.of(new BigDecimal(f.getStack().pop().getValueAs(BigDecimal.class).get().signum())));
            return State.NORMAL;
        })), 5.02, "push 1 if the top value of the stack is positive, -1 otherwise", "Pops the top value off the stack and pushes its signum (i.e. 1 if a is positive, -1 if a is negative)", "signum");
        Instruction GREATEST_COMMON_FACTOR = new Instruction(new VectorizedDyad(new SyntheticFunction(PlasmaListUtil.buildList(Type.NUMBER, Type.NUMBER), f -> {
            f.getStack().push(CastableValue.of(InstructionUtility.greatestCommonFactor(f.getStack().pop().getValueAs(BigDecimal.class).get().toBigInteger(), f.getStack().pop().getValueAs(BigDecimal.class).get().toBigInteger())));
            return State.NORMAL;
        })), 5.02, "compute the greatest common factor of the top two values of the stack", "Pops the top two values off the stack, and computes gcf(a, b), then pushes it", "gcf");
        Instruction MAX = new Instruction(new VectorizedDyad(new SyntheticFunction(PlasmaListUtil.buildList(Type.NUMBER, Type.NUMBER), f -> {
            f.getStack().push(CastableValue.of(f.getStack().pop().getValueAs(BigDecimal.class).get().max(f.getStack().pop().getValueAs(BigDecimal.class).get())));
            return State.NORMAL;
        })), 5.02, "compute the maximum value of the top two on the stack", "Pops the top two values off the stack and pushes the greater one", "max");
        Instruction MIN = new Instruction(new VectorizedDyad(new SyntheticFunction(PlasmaListUtil.buildList(Type.NUMBER, Type.NUMBER), f -> {
            f.getStack().push(CastableValue.of(f.getStack().pop().getValueAs(BigDecimal.class).get().min(f.getStack().pop().getValueAs(BigDecimal.class).get())));
            return State.NORMAL;
        })), 5.02, "compute the minimum value of the top two on the stack", "Pops the top two values off the stack and pushes the lesser one", "min");
    }

}
