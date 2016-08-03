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
package com.gmail.socraticphoenix.jaisbal.program.instructions.vectorization;

import com.gmail.socraticphoenix.jaisbal.app.util.DangerousFunction;
import com.gmail.socraticphoenix.jaisbal.program.Program;
import com.gmail.socraticphoenix.jaisbal.program.State;
import com.gmail.socraticphoenix.jaisbal.program.function.FunctionContext;
import com.gmail.socraticphoenix.plasma.reflection.CastableValue;

public class VectorizedDyad implements DangerousFunction<FunctionContext, State> {
    private DangerousFunction<FunctionContext, State> function;

    public VectorizedDyad(DangerousFunction<FunctionContext, State> function) {
        this.function = function;
    }

    @Override
    public State apply(FunctionContext context) throws Throwable {
        Program.checkUnderflow(2, context);
        CastableValue top = context.getStack().pop();
        CastableValue next = context.getStack().pop();
        if(top.getValueAs(CastableValue[].class).isPresent() || next.getValueAs(CastableValue[].class).isPresent()) {
            CastableValue[] vector;
            CastableValue scalar;
            boolean first;
            if (top.getValueAs(CastableValue[].class).isPresent()) {
                vector = top.getValueAs(CastableValue[].class).get();
                scalar = next;
                first = true;
            } else {
                vector = next.getValueAs(CastableValue[].class).get();
                scalar = top;
                first = false;
            }
            CastableValue[] newArray = new CastableValue[vector.length];
            for (int i = 0; i < vector.length; i++) {
                if(first) {
                    context.getStack().push(scalar);
                    context.getStack().push(vector[i]);
                } else {
                    context.getStack().push(scalar);
                    context.getStack().push(vector[i]);
                }
                State state = this.function.apply(context);
                if(state == State.TRANSMITTING_BREAK) {
                    return state;
                }

                Program.checkUnderflow(1, context);
                newArray[i] = context.getStack().pop();
            }
            context.getStack().push(CastableValue.of(newArray));
            return State.NORMAL;
        } else {
            context.getStack().push(next);
            context.getStack().push(top);
            return this.function.apply(context);
        }
    }
}
