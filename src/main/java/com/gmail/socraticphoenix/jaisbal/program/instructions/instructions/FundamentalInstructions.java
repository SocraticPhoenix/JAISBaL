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
import com.gmail.socraticphoenix.jaisbal.program.Type;
import com.gmail.socraticphoenix.jaisbal.program.instructions.util.AuxiliaryConstant;
import com.gmail.socraticphoenix.jaisbal.program.instructions.util.AuxiliaryInstruction;
import com.gmail.socraticphoenix.jaisbal.program.instructions.Instruction;
import com.gmail.socraticphoenix.jaisbal.program.instructions.util.InstructionUtility;

public interface FundamentalInstructions { //group -1
    Instruction FUNCTION = new Instruction(f -> {
        Type.STRING.checkMatches(f.getCurrentArgEasy());
        String s = f.getCurrentArgEasy().getAsString().get();
        if (f.getProgram().getFunction(s).isPresent()) {
             return f.getProgram().getFunction(s).get().run(f.getStack()).deTransmit();
        } else {
            throw new JAISBaLExecutionException("Could not find function " + s);
        }
    }, InstructionUtility.terminated(), -1, "call function ${arg}", "Calls the given function. This instruction takes one argument, terminated by '}' (see pushterm). This instruction fails if the given argument is not a string, or if no function exists for the given name", "f", "call");
    Instruction AUX_FUNCTION = new AuxiliaryInstruction();
    Instruction AUX_CONSTANT = new AuxiliaryConstant();
}
