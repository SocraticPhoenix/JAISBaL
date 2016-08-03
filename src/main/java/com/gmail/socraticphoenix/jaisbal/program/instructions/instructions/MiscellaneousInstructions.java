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
import com.gmail.socraticphoenix.jaisbal.program.instructions.vectorization.VectorizedMonad;
import com.gmail.socraticphoenix.plasma.reflection.CastableValue;

public interface MiscellaneousInstructions { //group 100
    Instruction QUINE = new Instruction(f -> {
        f.getStack().push(CastableValue.of(f.getProgram().getContent()));
        return State.NORMAL;
    }, 100, "load the source code of the program onto the stack", "Pushes the programs source code, as a string, onto the stack", "quine");
    Instruction EXPLAINED_QUINE = new Instruction(f -> {
        f.getStack().push(CastableValue.of(f.getProgram().explain()));
        return State.NORMAL;
    }, 100, "load the source code of the program onto the stack, in expanded form", "Pushes the programs source code, in expanded form, as a string, onto the stack", "equine");
    Instruction MINI_QUINE = new Instruction(f -> {
        f.getStack().push(CastableValue.of(f.getProgram().minify()));
        return State.NORMAL;
    }, 100, "load the source code of the program onto the stack, in mini form", "Pushes the programs source code, in mini form, as a string, onto the stack", "mquine");
    Instruction NAME = new Instruction(new VectorizedMonad(f -> {
        Program.checkUnderflow(1, f);
        f.getStack().push(InstructionUtility.name(f.getStack().pop()));
        return State.NORMAL;
    }), 100, "take the top value off the stack, determines its name, and push it", "Determines the name of the top value on the stack. If a is a 32-bit integer, a string representation of it's number name is returned, if a is an array, the name of every value in the array is computed, and pushed as a single array. Otherwise, the string value of a is pushed", "name");

}
