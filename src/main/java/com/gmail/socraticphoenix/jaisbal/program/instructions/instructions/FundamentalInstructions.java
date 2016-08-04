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
import com.gmail.socraticphoenix.jaisbal.encode.JAISBaLCharset;
import com.gmail.socraticphoenix.jaisbal.program.State;
import com.gmail.socraticphoenix.jaisbal.program.Type;
import com.gmail.socraticphoenix.jaisbal.program.instructions.Instruction;
import com.gmail.socraticphoenix.jaisbal.program.instructions.util.AuxiliaryConstant;
import com.gmail.socraticphoenix.jaisbal.program.instructions.util.AuxiliaryInstruction;
import com.gmail.socraticphoenix.jaisbal.program.instructions.util.InstructionUtility;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Pattern;

public interface FundamentalInstructions { //group -1
    Instruction FUNCTION = new Instruction(f -> {
        Type.STRING.checkMatches(f.getCurrentArgEasy());
        String s = f.getCurrentArgEasy().getAsString().get();
        if (f.getProgram().getFunction(s).isPresent()) {
            return f.getProgram().getFunction(s).get().run(f.getStack()).deTransmitBreak();
        } else {
            throw new JAISBaLExecutionException("Could not find function " + s);
        }
    }, InstructionUtility.terminated(), -1, "call function ${arg}", "Calls the given function. This instruction takes one argument, terminated by '}' (see pushterm). This instruction fails if the given argument is not a string, or if no function exists for the given name", "f", "call");
    Instruction IMPORT = new Instruction(f -> {
        Type.STRING.checkMatches(f.getCurrentArgEasy());
        String s = f.getCurrentArgEasy().getAsString().get().replaceAll(Pattern.quote("."), "/");
        if(!f.getProgram().getImported().contains(s)) {
            File lib = new File(s + ".isbl");
            if (lib.exists()) {
                byte[] bytes = Files.readAllBytes(lib.toPath());
                f.getProgram().$import(s.replaceAll("/", "."), new String(bytes, JAISBaLCharset.getCharset()));
            } else {
                throw new JAISBaLExecutionException("No library found called \"" + s + "\"");
            }
        }
        return State.NORMAL;
    }, InstructionUtility.terminated(), -1, "import ${arg}", "Imports a library. The argument is converted to a file with the extension .isbl, and every function in the given library is added to the running program, prefixed with <libraryname>.", "import");
    Instruction IMPORT_UTF8 = new Instruction(f -> {
        Type.STRING.checkMatches(f.getCurrentArgEasy());
        String s = f.getCurrentArgEasy().getAsString().get().replaceAll(Pattern.quote("."), "/");
        if(!f.getProgram().getImported().contains(s)) {
            File lib = new File(s + ".isbl");
            if (lib.exists()) {
                byte[] bytes = Files.readAllBytes(lib.toPath());
                f.getProgram().$import(s.replaceAll("/", "."), new String(bytes, StandardCharsets.UTF_8));
            } else {
                throw new JAISBaLExecutionException("No library found called \"" + s + "\"");
            }
        }
        return State.NORMAL;
    }, InstructionUtility.terminated(), -1, "import ${arg}", "Imports a library encoded in utf8. The argument is converted to a file with the extension .isbl, and every function in the given library is added to the running program, prefixed with <libraryname>.", "importutf");
    Instruction CHAR_FUNCTION = new Instruction(f -> {
        Type.STRING.checkMatches(f.getCurrentArgEasy());
        String s = f.getCurrentArgEasy().getAsString().get();
        if (f.getProgram().getFunction(s).isPresent()) {
            return f.getProgram().getFunction(s).get().run(f.getStack()).deTransmitBreak();
        } else {
            throw new JAISBaLExecutionException("Could not find function " + s);
        }
    }, InstructionUtility.fixed(1), -1, "call function ${arg}", "Calls the given function. This instruction takes one argument, terminated by '}' (see pushterm). This instruction fails if the given argument is not a string, or if no function exists for the given name", "l", "call1");
    Instruction AUX_FUNCTION = new AuxiliaryInstruction();
    Instruction AUX_CONSTANT = new AuxiliaryConstant();
}
