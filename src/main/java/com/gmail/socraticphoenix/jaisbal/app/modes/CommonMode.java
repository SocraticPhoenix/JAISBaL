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
package com.gmail.socraticphoenix.jaisbal.app.modes;

import com.gmail.socraticphoenix.jaisbal.JAISBaL;
import com.gmail.socraticphoenix.jaisbal.app.util.LoopedSupplier;
import com.gmail.socraticphoenix.jaisbal.encode.JAISBaLCharset;
import com.gmail.socraticphoenix.jaisbal.program.Program;
import com.gmail.socraticphoenix.jaisbal.program.function.FunctionContext;
import com.gmail.socraticphoenix.jaisbal.app.util.JAISBaLExecutionException;
import com.gmail.socraticphoenix.plasma.base.Stopwatch;
import com.gmail.socraticphoenix.plasma.math.PlasmaMathUtil;
import com.gmail.socraticphoenix.plasma.string.PlasmaStringUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public interface CommonMode {

    static void commonWrite(String in, File file, Map<String, String> args) throws IOException {
        Charset charset = JAISBaLCharset.get(args.get("target-encoding"));
        CharsetEncoder encoder = charset.newEncoder();
        for (char c : in.toCharArray()) {
            if (!encoder.canEncode(c)) {
                JAISBaL.getOut().println("The " + charset.displayName() + " Charset does not support '" + PlasmaStringUtil.escape(String.valueOf(c)) + "'");
                return;
            }
        }

        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), charset);
        writer.write(in);
        writer.close();
    }

    static void commonExec(String content, Map<String, String> args) throws JAISBaLExecutionException, IOException {
        Supplier<String> prev = JAISBaL.getIn();
        String execRunsS = args.get("exec-number");
        if (PlasmaMathUtil.isInteger(execRunsS)) {
            int execRuns = Integer.parseInt(execRunsS);
            if (args.containsKey("exec-time")) {
                String timeS = args.get("exec-time");
                if (PlasmaMathUtil.isLong(timeS)) {
                    long millis = Long.parseLong(timeS);
                    Program program = Program.parse(content);
                    FunctionContext context = program.getMain().createContext();
                    Thread thread = new Thread() {

                        @Override
                        public void run() {
                            try {
                                TimeUnit.MILLISECONDS.sleep(millis);
                                context.terminate();
                                JAISBaL.getOut().println();
                                JAISBaL.getOut().println("Script took more than " + Stopwatch.toTimeString(millis, false) + " to complete. Terminated it.");
                                System.exit(0);
                            } catch (Throwable ignore) {

                            }
                        }

                    };
                    thread.setDaemon(true);
                    thread.start();
                    for (int z = 0; z < execRuns; z++) {
                        JAISBaL.getOut().println("Run #" + (z + 1) + ":");
                        if(args.containsKey("input" + z)) {
                            LoopedSupplier supplier = new LoopedSupplier();
                            JAISBaL.parseInput(args.get("input" + z)).forEach(supplier::add);
                            JAISBaL.setIn(supplier);
                        } else {
                            JAISBaL.setIn(prev);
                        }
                        context.clone().runAsMain();
                        JAISBaL.getOut().println(PlasmaStringUtil.indent(60, "-"));
                        JAISBaL.getOut().println();
                    }
                    JAISBaL.collectAndPrintInfo(program);
                } else {
                    JAISBaL.getOut().println("\"" + timeS + "\" is not a number");
                }
            } else {
                Program program = Program.parse(content);
                FunctionContext context = program.getMain().createContext();
                for (int z = 0; z < execRuns; z++) {
                    JAISBaL.getOut().println("Run #" + (z + 1) + ":");
                    if(args.containsKey("input" + z)) {
                        LoopedSupplier supplier = new LoopedSupplier();
                        JAISBaL.parseInput(args.get("input" + z)).forEach(supplier::add);
                        JAISBaL.setIn(supplier);
                    } else {
                        JAISBaL.setIn(prev);
                    }
                    context.clone().runAsMain();
                    JAISBaL.getOut().println(PlasmaStringUtil.indent(60, "-"));
                    JAISBaL.getOut().println();
                }
                JAISBaL.collectAndPrintInfo(program);
            }
        } else {
            JAISBaL.getOut().println("\"" + execRunsS + "\" is not a number");
        }
    }

}
