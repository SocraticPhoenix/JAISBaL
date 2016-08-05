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
package com.gmail.socraticphoenix.jaisbal.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Supplier;

public class InSupplier implements Supplier<String>, Terminable {
    private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private boolean running = true;
    private boolean wasJustEnd = false;

    @Override
    public String get() {
        StringBuilder b = new StringBuilder();
        while (this.running) {
            try {
                if(this.reader.ready()) {
                    char c = (char) this.reader.read();
                    if(this.wasJustEnd && this.isEnd(c)) {
                        continue;
                    } else if (this.isEnd(c)) {
                        this.wasJustEnd = true;
                        break;
                    } else {
                        this.wasJustEnd = false;
                        b.append(c);
                    }
                }
            } catch (IOException ignore) {

            }
        }
        if(this.running) {
            return b.toString();
        } else {
            return null;
        }
    }

    private boolean isEnd(char c) {
        return c == '\n' || c == '\r' || System.lineSeparator().indexOf(c) != -1;
    }

    @Override
    public void terminate() {
        this.running = false;
    }

    @Override
    public void restart() {
        this.running = true;
    }
}
