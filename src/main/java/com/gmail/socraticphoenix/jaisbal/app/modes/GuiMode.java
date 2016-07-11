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

import com.gmail.socraticphoenix.jaisbal.encode.JAISBaLCharset;
import com.gmail.socraticphoenix.jaisbal.app.gui.JAISBaLInterface;
import com.gmail.socraticphoenix.jaisbal.app.util.DangerousConsumer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class GuiMode implements DangerousConsumer<Map<String, String>> {

    @Override
    public void accept(Map<String, String> args) throws IOException {
        try {
            JAISBaLCharset.get(args.get("encoding"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown charset \"" + args.get("encoding") + "\"");
        }

        try {
            JAISBaLCharset.get(args.get("target-encoding"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown charset \"" + args.get("target-encoding") + "\"");
        }


        String content;
        if(args.containsKey("content")) {
            content = args.get("content");
        } else if (args.containsKey("file")) {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(new File(args.get("file"))), JAISBaLCharset.get(args.get("encoding")));
            StringBuilder conts = new StringBuilder();
            int i;
            while ((i = reader.read()) > -1) {
                conts.append((char) i);
            }
            reader.close();
            content = conts.toString();
        } else {
            content = "";
        }

        new JAISBaLInterface(content, args).make().setVisible(true);
    }

}
