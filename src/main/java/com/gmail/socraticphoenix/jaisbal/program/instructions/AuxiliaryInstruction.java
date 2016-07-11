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
import com.gmail.socraticphoenix.jaisbal.app.util.JAISBaLExecutionException;
import com.gmail.socraticphoenix.plasma.reflection.CastableValue;

import java.math.BigDecimal;

public class AuxiliaryInstruction extends Instruction {

    public AuxiliaryInstruction() {
        super(f -> {
            Program.checkUnderflow(1, f);
            CastableValue value = f.getStack().pop();
            Type.NUMBER.checkMatches(value);
            try {
                int i = value.getValueAs(BigDecimal.class).get().intValueExact();
                InstructionRegistry.getAuxiliaryInstructions().get(i).getAction().accept(f);
            } catch (ArithmeticException e) {
                throw new JAISBaLExecutionException(Program.valueToString(value) + " is not an integer index");
            } catch (IndexOutOfBoundsException e) {
                throw new JAISBaLExecutionException("No aux_instruction registered for " + Program.valueToString(value));
            }
        }, Instructions.number(), "call auxiliary instruction #${arg}", "Calls the auxiliary instruction registered at the specified index. This instruction takes one argument, a number (see pushnum)", "F", "aux");
    }

}
