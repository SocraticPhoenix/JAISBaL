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

public class VectorizedMonad implements DangerousFunction<FunctionContext, State> {
    private DangerousFunction<FunctionContext, State> function;

    public VectorizedMonad(DangerousFunction<FunctionContext, State> function) {
        this.function = function;
    }

    @Override
    public State apply(FunctionContext context) throws Throwable {
        Program.checkUnderflow(1, context);
        CastableValue top = context.getStack().pop();
        if(top.getValueAs(CastableValue[].class).isPresent()) {
            CastableValue[] array = top.getValueAs(CastableValue[].class).get();
            CastableValue[] newArray = new CastableValue[array.length];
            for (int i = 0; i < array.length; i++) {
                context.getStack().push(array[i]);
                State state = this.function.apply(context);
                if(state.isTransmit()) {
                    return state;
                }
                Program.checkUnderflow(1, context);
                newArray[i] = context.getStack().pop();
            }
            context.getStack().push(CastableValue.of(newArray));
            return State.NORMAL;
        } else {
            context.getStack().push(top);
            return this.function.apply(context);
        }
    }
}
