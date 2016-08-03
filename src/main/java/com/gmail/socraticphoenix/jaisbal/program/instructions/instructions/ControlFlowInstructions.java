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
import com.gmail.socraticphoenix.plasma.math.PlasmaMathUtil;
import com.gmail.socraticphoenix.plasma.reflection.CastableValue;

import java.math.BigDecimal;

public interface ControlFlowInstructions { //Group 3
    //General instructions, sub group .01
    Instruction END = new Instruction(f -> State.NORMAL, 3.01, "end current language construct", "Ends a loop, if, ifelse, or other statement", "end");
    Instruction BREAK = new Instruction(f -> State.TRANSMITTING_BREAK, 3.01, "break out of the current function frame or loop", "Breaks out of the current function frame or loop", "break");
    Instruction SUPER_PUSH = new Instruction(f -> {
        Program.checkUnderflow(1, f);
        f.getParentStack().push(f.getStack().pop());
        return State.NORMAL;
    }, 3.01, "pop the top value of the stack and push it to the parent stack", "Pops the top value of the stack and pushes it to the parent function frame's stack", "superpush");

    //Goto's, sub group .02
    Instruction RELATIVE_JUMP = new Instruction(f -> {
        CastableValue indexV = f.getCurrentArgEasy();
        Type.NUMBER.checkMatches(indexV);
        BigDecimal index = indexV.getValueAs(BigDecimal.class).get();
        try {
            int i = f.getCurrent();
            int g = f.getCurrent() + index.intValue() - 1;
            if (i != g) {
                f.setCurrent(g);
                return State.JUMPED;
            } else {
                return State.NORMAL;
            }
        } catch (ArithmeticException e) {
            throw new JAISBaLExecutionException("Invalid value: " + String.valueOf(index) + " is not an integer index", e);
        }
    }, InstructionUtility.number(), 3.02, "jump ${arg} instructions", "Jumps the given amount of instructions forward. The argument may be positive or negative, and the jump will likewise be forwards or backwards. This instruction takes one argument, a number (see pushnum). This instruction fails of the argument is not a 32-bit integer", "jump");
    Instruction INDEX_JUMP = new Instruction(f -> {
        CastableValue indexV = f.getCurrentArgEasy();
        Type.NUMBER.checkMatches(indexV);
        BigDecimal index = indexV.getValueAs(BigDecimal.class).get();
        try {
            int i = f.getCurrent();
            int g = index.intValue();
            if (i != g) {
                f.setCurrent(g);
                return State.JUMPED;
            } else {
                return State.NORMAL;
            }
        } catch (ArithmeticException e) {
            throw new JAISBaLExecutionException("Invalid value: " + String.valueOf(index) + " is not an integer index", e);
        }
    }, InstructionUtility.number(), 3.02, "jump to instruction ${arg}", "Jumps to the instruction at the given index. This instruction takes one argument, a number (see pushnum). This instruction fails if the argument is not a 32-bit integer", "jumpindex");

    //Loops, sub group .03
    Instruction FOR_LOOP = new Instruction(f -> {
        int end = f.subsetIndex("for", "end");
        int start = f.getCurrent();
        Program.checkUnderflow(1, f);
        CastableValue val = f.getStack().pop();
        if (Type.NUMBER.matches(val)) {
            BigDecimal bd = BigDecimal.ZERO;
            BigDecimal cond = val.getValueAs(BigDecimal.class).get();
            while (bd.compareTo(cond) < 0) {
                State state = f.runSubset(end, c -> !PlasmaMathUtil.fitsBounds(start, c.getCurrent(), end));
                if (state == State.TRANSMITTING_BREAK || state == State.JUMPED) {
                    return State.BROKEN;
                }
                f.setCurrent(start);
                bd = bd.add(BigDecimal.ONE);
            }
            f.setCurrent(end);
        } else {
            CastableValue[] values;
            if (val.getValueAs(CastableValue[].class).isPresent()) {
                values = val.getValueAs(CastableValue[].class).get();
            } else {
                String s = val.getAsString().get();
                values = new CastableValue[s.length()];
                String[] pieces = s.split("");
                for (int i = 0; i < pieces.length; i++) {
                    values[i] = CastableValue.of(pieces[i]);
                }
            }
            for (CastableValue value : values) {
                f.getStack().push(value);
                State state = f.runSubset(end, c -> !PlasmaMathUtil.fitsBounds(start, c.getCurrent(), end));
                if (state == State.TRANSMITTING_BREAK || state == State.JUMPED) {
                    return state.deTransmit();
                }
                f.setCurrent(start);
            }
            f.setCurrent(end);
        }

        return State.NORMAL;
    }, 3.03, "start for loop", "This instruction functions as a for loop or a for-each loop. If the top value of the stack is a number, the for block will be run that many times. If the top value of the stack is a string, it will be converted to a character array and run as an array. If the top value of the stack is an array, the for block will be run once for each value in the array, and the current value will be pushed to the stack directly before the block is run.", "for");
    Instruction WHILE = new Instruction(f -> {
        int end = f.subsetIndex("while", "end");
        int start = f.getCurrent();
        Program.checkUnderflow(1, f);
        CastableValue val = f.getStack().pop();
        f.getStack().push(val);
        while (InstructionUtility.truthy(val)) {
            State state = f.runSubset(end, c -> !PlasmaMathUtil.fitsBounds(start, c.getCurrent(), end - 1));
            f.setCurrent(start);
            if (state == State.TRANSMITTING_BREAK || state == State.JUMPED) {
                return state.deTransmit();
            }
            Program.checkUnderflow(1, f);
            val = f.getStack().pop();
            f.getStack().push(val);
        }
        f.setCurrent(end);
        return State.NORMAL;
    }, 3.03, "start while loop", "This instruction functions as a while loop. The while block will be run so long as the top value on the stack is truthy. Note that the while loop will not pop off the top value of the stack when checking if it is truthy.", "while");
    Instruction DO_WHILE = new Instruction(f -> {
        int end = f.subsetIndex("dowhile", "end");
        int start = f.getCurrent();
        CastableValue val;
        do {
            State state = f.runSubset(end, c -> !PlasmaMathUtil.fitsBounds(start, c.getCurrent(), end - 1));
            f.setCurrent(start);
            if (state == State.TRANSMITTING_BREAK || state == State.JUMPED) {
                return state.deTransmit();
            }
            Program.checkUnderflow(1, f);
            val = f.getStack().pop();
            f.getStack().push(val);
        } while (InstructionUtility.truthy(val));
        f.setCurrent(end);
        return State.NORMAL;
    }, 3.03, "start do-while loop", "This instruction functions as a dowhile loop. The dowhile block will run once, regardless of the truthiness of the top value of the stack, and then will continue running so long as the top value of the stack is truthy.  Note that the dowhile loop will not pop off the top value of the stack when checking if it is truthy.", "dowhile");

    //Conditionals, sub group .04
    Instruction IF_BLOCK = new Instruction(f -> {
        Program.checkUnderflow(1, f);
        int start = f.getCurrent();
        int end = f.subsetIndex("ifblock", "end");
        CastableValue value = f.getStack().pop();
        if (InstructionUtility.truthy(value)) {
            f.setCurrent(start);
            return State.NORMAL;
        } else {
            f.setCurrent(end);
            return State.JUMPED;
        }
    }, 3.04, "if the top value of the stack is truthy, execute the next block", "Pops the top value of the stack. If a is truthy, run the block (see if).", "ifblock");
    Instruction IF_ELSE_BLOCK = new Instruction(f -> {
        Program.checkUnderflow(1, f);
        int start = f.getCurrent();
        int truthyEnd = f.subsetIndex("ifelse", "else");
        f.setCurrent(truthyEnd + 1);
        int falsyEnd = f.subsetIndex("else", "end");
        f.setCurrent(start);
        CastableValue value = f.getStack().pop();
        if (InstructionUtility.truthy(value)) {
            State state = f.runSubset(truthyEnd - 1, c -> !PlasmaMathUtil.fitsBounds(start, c.getCurrent(), truthyEnd - 1));
            if(state == State.TRANSMITTING_BREAK || state == State.JUMPED) {
                return state;
            }
            f.setCurrent(falsyEnd);
        } else {
            f.setCurrent(truthyEnd + 1);
            State state = f.runSubset(falsyEnd, c -> !PlasmaMathUtil.fitsBounds(truthyEnd + 1, c.getCurrent(), falsyEnd));
            if(state == State.TRANSMITTING_BREAK || state == State.JUMPED) {
                return state;
            }
            f.setCurrent(falsyEnd);
        }
        return State.NORMAL;
    }, 3.04, "if the top value of the stack is truthy, execute the next block, otherwise, execute the else block", "Pops the top value of the stack. If a is truthy, run the  if block, otherwise run the else block (see if).", "ifelse");
    Instruction ELSE = new Instruction(f -> {
        int falsyEnd = f.subsetIndex("else", "end");
        f.setCurrent(falsyEnd);
        return State.JUMPED;
    }, 3.04, "end the truthy section of the ifelse block", "The end of an ifelse's if block, and the beginning of it's else block", "else");


}
