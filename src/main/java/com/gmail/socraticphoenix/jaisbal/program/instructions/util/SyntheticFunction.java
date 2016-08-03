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
package com.gmail.socraticphoenix.jaisbal.program.instructions.util;

import com.gmail.socraticphoenix.jaisbal.app.util.DangerousFunction;
import com.gmail.socraticphoenix.jaisbal.app.util.JAISBaLExecutionException;
import com.gmail.socraticphoenix.jaisbal.program.Program;
import com.gmail.socraticphoenix.jaisbal.program.State;
import com.gmail.socraticphoenix.jaisbal.program.Type;
import com.gmail.socraticphoenix.jaisbal.program.function.FunctionContext;
import com.gmail.socraticphoenix.plasma.collection.PlasmaListUtil;
import com.gmail.socraticphoenix.plasma.reflection.CastableValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class SyntheticFunction implements DangerousFunction<FunctionContext, State> {
    private List<Type> parameters;
    private DangerousFunction<FunctionContext, State> func;

    public SyntheticFunction(List<Type> parameters, DangerousFunction<FunctionContext, State> func) {
        this.parameters = parameters;
        this.func = func;
    }

    @Override
    public State apply(FunctionContext context) throws Throwable {
        this.validate(context.getStack(), context);
        return this.func.apply(context);
    }

    private void validate(Stack<CastableValue> stack, FunctionContext context) throws JAISBaLExecutionException {
        if (this.parameters.size() == 0) {
            return;
        }

        Program.checkUnderflow(this.parameters.size(), context);

        List<CastableValue> verified = new ArrayList<>();
        for(Type param : this.parameters) {
            CastableValue value = stack.pop();
            param.checkMatches(value);
            verified.add(value);
        }
        PlasmaListUtil.reverseList(verified).forEach(stack::push);
    }
}
