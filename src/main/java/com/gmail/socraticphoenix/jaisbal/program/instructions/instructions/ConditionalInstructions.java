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

import com.gmail.socraticphoenix.jaisbal.program.Program;
import com.gmail.socraticphoenix.jaisbal.program.State;
import com.gmail.socraticphoenix.jaisbal.program.instructions.Instruction;
import com.gmail.socraticphoenix.jaisbal.program.instructions.util.InstructionUtility;
import com.gmail.socraticphoenix.plasma.reflection.CastableValue;

import java.math.BigDecimal;

public interface ConditionalInstructions { //Group 2
    //Skip-lines, sub group .01
    Instruction EQUAL = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        if ((InstructionUtility.compare(f.getStack().pop(), f.getStack().pop()) == 0)) {
            f.setCurrent(f.getCurrent() + 1);
            return State.JUMPED;
        }
        return State.NORMAL;
    }, 2.01, "skip the next statement if the top two values on the stack are equal", "Skips the next instruction if a and b are equal (see compare)", "=", "equal");
    Instruction NOT_EQUAL = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        if (InstructionUtility.compare(f.getStack().pop(), f.getStack().pop()) != 0) {
            f.setCurrent(f.getCurrent() + 1);
            return State.JUMPED;
        }
        return State.NORMAL;
    }, 2.01, "skip the next statement if the two top values on the stack are not equal", "Skips the next instruction if a and b are not equal (see compare)", "!=", "notequal");
    Instruction GREATER = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        if (!(InstructionUtility.compare(f.getStack().pop(), f.getStack().pop()) > 0)) {
            f.setCurrent(f.getCurrent() + 1);
            return State.JUMPED;
        }
        return State.NORMAL;
    }, 2.01, "skip the next statement if the top value on the stack is greater than the next value on the stack", "Skips the next instruction if a > b (see compare)", ">", "greater");
    Instruction LESS = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        if (!(InstructionUtility.compare(f.getStack().pop(), f.getStack().pop()) < 0)) {
            f.setCurrent(f.getCurrent() + 1);
            return State.JUMPED;
        }
        return State.NORMAL;
    }, 2.01, "skip the next statement if the top value on the stack is less than the next value on the stack", "Skips the next instruction if a < b (see compare)", "<", "less");
    Instruction GREATER_EQUAL = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        if (!(InstructionUtility.compare(f.getStack().pop(), f.getStack().pop()) >= 0)) {
            f.setCurrent(f.getCurrent() + 1);
            return State.JUMPED;
        }
        return State.NORMAL;
    }, 2.01, "skip the next statement if the top value on the stack is greater than or equal to the next value on the stack", "Skips the next instruction if a >= b (see compare)", ">=", "greaterequal");
    Instruction LESS_EQUAL = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        if (!(InstructionUtility.compare(f.getStack().pop(), f.getStack().pop()) <= 0)) {
            f.setCurrent(f.getCurrent() + 1);
            return State.JUMPED;
        }
        return State.NORMAL;
    }, 2.01, "skip the next statement if the top value on the stack is less than the next value on the stack", "Skips the next instruction if a <= b (see compare)", "<=", "lessequal");

    Instruction EQUAL_ALL = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        boolean check = true;
        CastableValue prev = null;
        while (!f.getStack().isEmpty()) {
            if (prev == null) {
                prev = f.getStack().pop();
            } else {
                CastableValue v = f.getStack().pop();
                check = check && InstructionUtility.compare(v, prev) == 0;
                prev = v;
            }
        }

        if (check) {
            f.setCurrent(f.getCurrent() + 1);
            return State.JUMPED;
        }
        return State.NORMAL;
    }, 2.01, "skip the next statement if all values on the stack are equal", "Consecutively pops every value of the stack, checks if it is equal to the previously popped value, and ANDs the boolean result to a single boolean. If the final boolean is true, the next instruction is skipped (see compare)", "&=", "equalall");
    Instruction NOT_EQUAL_ALL = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        boolean check = true;
        CastableValue prev = null;
        while (!f.getStack().isEmpty()) {
            CastableValue v = f.getStack().pop();
            if (prev == null) {
                prev = v;
            } else {
                check = check && InstructionUtility.compare(v, prev) != 0;
                prev = v;
            }
        }

        if (check) {
            f.setCurrent(f.getCurrent() + 1);
            return State.JUMPED;
        }
        return State.NORMAL;
    }, 2.01, "skip the next statement if any values on the stack are not equal", "Consecutively pops every value of the stack, checks if it is not equal to the previously popped value, and ANDs the boolean result to a single boolean. If the final boolean is true, the next instruction is skipped (see compare)", "&!=", "notequalall");
    Instruction GREATER_ALL = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        boolean check = true;
        CastableValue prev = null;
        while (!f.getStack().isEmpty()) {
            if (prev == null) {
                prev = f.getStack().pop();
            } else {
                CastableValue v = f.getStack().pop();
                check = check && InstructionUtility.compare(v, prev) > 0;
                prev = v;
            }
        }

        if (check) {
            f.setCurrent(f.getCurrent() + 1);
            return State.JUMPED;
        }
        return State.NORMAL;
    }, 2.01, "skip the next statement if the values on the stack are in smalles to greatest order, with no duplicates", "Consecutively pops every value of the stack, checks if it is > the previously popped value, and ANDs the boolean result to a single boolean. If the final boolean is true, the next instruction is skipped (see compare)", "&>", "greaterall");
    Instruction LESS_ALL = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        boolean check = true;
        CastableValue prev = null;
        while (!f.getStack().isEmpty()) {
            if (prev == null) {
                prev = f.getStack().pop();
            } else {
                CastableValue v = f.getStack().pop();
                check = check && InstructionUtility.compare(v, prev) < 0;
                prev = v;
            }
        }

        if (check) {
            f.setCurrent(f.getCurrent() + 1);
            return State.JUMPED;
        }
        return State.NORMAL;
    }, 2.01, "skip the next statement if the values on the stack are in greatest to smallest order, with no duplicates", "Consecutively pops every value of the stack, checks if it is < the previously popped value, and ANDs the boolean result to a single boolean. If the final boolean is true, the next instruction is skipped (see compare)", "&<", "lessall");
    Instruction GREATER_EQUAL_ALL = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        boolean check = true;
        CastableValue prev = null;
        while (!f.getStack().isEmpty()) {
            if (prev == null) {
                prev = f.getStack().pop();
            } else {
                CastableValue v = f.getStack().pop();
                check = check && InstructionUtility.compare(v, prev) >= 0;
                prev = v;
            }
        }

        if (check) {
            f.setCurrent(f.getCurrent() + 1);
            return State.JUMPED;
        }
        return State.NORMAL;
    }, 2.01, "skip the next statement if the values on the stack are in smallest to greatest order", "Consecutively pops every value of the stack, checks if it is >= the previously popped value, and ANDs the boolean result to a single boolean. If the final boolean is true, the next instruction is skipped (see compare)", "&>=", "greaterequalall");
    Instruction LESS_EQUAL_ALL = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        boolean check = true;
        CastableValue prev = null;
        while (!f.getStack().isEmpty()) {
            if (prev == null) {
                prev = f.getStack().pop();
            } else {
                CastableValue v = f.getStack().pop();
                check = check && InstructionUtility.compare(v, prev) < 0;
                prev = v;
            }
        }

        if (check) {
            f.setCurrent(f.getCurrent() + 1);
            return State.JUMPED;
        }
        return State.NORMAL;
    }, 2.01, "skip the next statement if the values on the stack are in greatest to smallest order", "Consecutively pops every value of the stack, checks if it is <= the previously popped value, and ANDs the boolean result to a single boolean. If the final boolean is true, the next instruction is skipped (see compare)", "&<=", "lessequalall");
    Instruction IF_TRUTHY = new Instruction(f -> {
        Program.checkUnderflow(1, f);
        CastableValue value = f.getStack().pop();
        if (InstructionUtility.truthy(value)) {
            f.setCurrent(f.getCurrent() + 1);
            return State.JUMPED;
        }
        return State.NORMAL;
    }, 2.01, "if the top value on the stack is truthy, skip the next statement", "Pops the top value of the stack. If a is truthy, skip the next statement. If a is a number, it is truthy if it is greater than 0. If a is a string, it is truthy if it equals \"true,\" \"t\" or \"yes.\" If a is an array, it is truthy if it contains more truthy values than falsy ones", "if");
    Instruction IF_FALSEY = new Instruction(f -> {
        Program.checkUnderflow(1, f);
        CastableValue value = f.getStack().pop();
        if (!InstructionUtility.truthy(value)) {
            f.setCurrent(f.getCurrent() + 1);
            return State.JUMPED;
        }
        return State.NORMAL;
    }, 2.01, "if the top value on the stack is falsy, skip the next statement", "Pops the top value of the stack. If a is not truthy, skip the next statement (see if)", "!if");

    //Pushes, sub group .02
    Instruction PUSH_TRUTHY = new Instruction(f -> {
        f.getStack().push(new CastableValue(new BigDecimal(1)));
        return State.NORMAL;
    }, 2.02, "push a truthy value onto the stack", "Pushes 1, a truthy value, onto the stack", "true");
    Instruction PUSH_FALSEY = new Instruction(f -> {
        f.getStack().push(new CastableValue(new BigDecimal(0)));
        return State.NORMAL;
    }, 2.02, "push a falsey value onto the stack", "Pushes 0, a falsy value, onto the stack", "false");
    Instruction NEGATE = new Instruction(f -> {
        Program.checkUnderflow(1, f);
        CastableValue value = f.getStack().pop();
        f.getStack().push(InstructionUtility.negate(value));
        return State.NORMAL;
    }, 2.02, "negate the top value of the stack", "Pops the top value of the stack and negates it. If the top value of the stack is a number, its sign will be flipped (unless the number is 0, in which case 1 will be pushed to maintain boolean negation). If the top value of the stack is a truthy string, 'false' will be pushed, or if the top value is a falsey string 'true' will be pushed. If the top value is an array, every value in the array will be negated, and the array will be pushed. If a is an array and contains itself, this instruction will fail.", "!", "negate");
    Instruction COMPARE = new Instruction(f -> {
        Program.checkUnderflow(2, f);
        f.getStack().push(CastableValue.of(new BigDecimal(InstructionUtility.compare(f.getStack().pop(), f.getStack().pop()))));
        return State.NORMAL;
    }, 2.02, "compare the top value of the stack with the second value on the stack", "Compares a and b and pushes a: positive int if a > b, negative int if b < a, and 0 if a = b. If a or b is an array, and the other value is a non-array, the array value will be considered larger. If a and b are both numbers, a mathematical comparison takes place. If both a and b are arrays, the comparison is calculated by initializing a single int variable, and consecutively comparing each value of the arrays, then the difference between a.length and b.length is added to the variable. If a and b are both strings, lexical comparison takes place", "compare");

}
