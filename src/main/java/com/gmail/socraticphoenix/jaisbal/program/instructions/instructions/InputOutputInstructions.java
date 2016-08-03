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
import com.gmail.socraticphoenix.jaisbal.program.State;
import com.gmail.socraticphoenix.jaisbal.program.Type;
import com.gmail.socraticphoenix.jaisbal.program.instructions.Instruction;
import com.gmail.socraticphoenix.jaisbal.program.instructions.util.SyntheticFunction;
import com.gmail.socraticphoenix.plasma.collection.PlasmaListUtil;
import com.gmail.socraticphoenix.plasma.reflection.CastableValue;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public interface InputOutputInstructions { //group 0, non-standard
    Instruction READ_FILE = new Instruction(new SyntheticFunction(PlasmaListUtil.buildList(Type.STRING), f -> {
        String s = f.getStack().pop().getAsString().get();
        File file = new File(s);
        if(file.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder content = new StringBuilder();
            int i = 0;
            while ((i = reader.read()) > -1) {
                content.append((char) i);
            }
            reader.close();
            f.getStack().push(CastableValue.of(content.toString()));
        } else {
            f.getStack().push(CastableValue.of(""));
        }
        return State.NORMAL;
    }), 0.01, 20, "read the content of the file on the top of the stack", "Pops a string off the stack and searches the file system for a file of the same name. If the file exists, its content will be read and pushes as a string onto the stack, if it does not exist, and empty string will be pushed instead", "fread");
    Instruction WRITE_FILE = new Instruction(new SyntheticFunction(PlasmaListUtil.buildList(Type.STRING, Type.STRING), f -> {
        File file = new File(f.getStack().pop().getAsString().get());
        String content = f.getStack().pop().getAsString().get();
        FileWriter writer = new FileWriter(file);
        writer.write(content);
        writer.close();
        return State.NORMAL;
    }), 0.01, 40, "write the second value of the stack to the file on the top of the stack", "Pops a string off the stack and converts it to a file, then writes the string b to it. If the file does not exist it will be created", "fwrite");
    Instruction APPEND_FILE = new Instruction(new SyntheticFunction(PlasmaListUtil.buildList(Type.STRING, Type.STRING), f -> {
        File file = new File(f.getStack().pop().getAsString().get());
        String content = f.getStack().pop().getAsString().get();
        StringBuilder contentBuilder = new StringBuilder();
        if(file.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            int i = 0;
            while ((i = reader.read()) > -1) {
                contentBuilder.append((char) i);
            }
        }
        contentBuilder.append(content);
        FileWriter writer = new FileWriter(file);
        writer.write(contentBuilder.toString());
        writer.close();
        return State.NORMAL;
    }), 0.01, 40, "append the second value of the stack to the file on the top of the stack", "Pops a string off the stack and converts it to a file, then appends the string b to it. If the file does not exist it will be created", "fappend");
    Instruction READ_URL = new Instruction(new SyntheticFunction(PlasmaListUtil.buildList(Type.STRING), f -> {
        URL url = new URL(f.getStack().pop().getAsString().get());
        URLConnection connection = url.openConnection();
        connection.setReadTimeout(1000);
        connection.setConnectTimeout(1000);
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder content = new StringBuilder();
        int i = 0;
        while ((i = reader.read()) > -1) {
            content.append((char) i);
        }
        reader.close();
        f.getStack().push(CastableValue.of(content.toString()));
        return State.NORMAL;
    }), 0.01, 10, "read the content of the url on the top of the stack", "Pops a string off the stack and converts it to a URL, the content of the URL is then read and then pushed onto the stack", "uread");
    Instruction POST_URL = new Instruction(new SyntheticFunction(PlasmaListUtil.buildList(Type.STRING, Type.STRING), f -> {
        URL url = new URL(f.getStack().pop().getAsString().get());
        String content = f.getStack().pop().getAsString().get();
        URLConnection connection = url.openConnection();
        connection.setReadTimeout(1000);
        connection.setConnectTimeout(1000);
        if(connection instanceof HttpURLConnection) {
            HttpURLConnection http = (HttpURLConnection) connection;
            http.setRequestMethod("POST");
            DataOutputStream stream = new DataOutputStream(http.getOutputStream());
            stream.writeBytes(content);
            stream.close();
        } else {
            throw new JAISBaLExecutionException("Unknown url protocol: " + url.getProtocol());
        }
        return State.NORMAL;
    }), 0.01, 30, "posts the second value of the stack to the url on the top of the stack", "Pops a string off the stack and posts b to it", "upost");
}
