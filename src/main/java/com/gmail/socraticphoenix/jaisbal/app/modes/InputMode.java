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
import com.gmail.socraticphoenix.jaisbal.app.util.DangerousConsumer;
import com.gmail.socraticphoenix.jaisbal.app.util.InSupplier;
import com.gmail.socraticphoenix.jaisbal.app.util.Terminable;
import com.gmail.socraticphoenix.jaisbal.encode.JAISBaLCharset;
import com.gmail.socraticphoenix.plasma.string.PlasmaStringUtil;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;
import java.util.Map;

public class InputMode implements DangerousConsumer<Map<String, String>> {

    @Override
    public void accept(Map<String, String> args) throws Throwable {
        JAISBaL.setIn(new InSupplier());

        if (!args.containsKey("content")) {
            JAISBaL.getOut().println("No content specified");
            return;
        }

        try {
            JAISBaLCharset.get(args.get("encoding"));
        } catch (IllegalArgumentException e) {
            JAISBaL.getOut().println("Unknown charset \"" + args.get("encoding") + "\"");
            return;
        }

        try {
            JAISBaLCharset.get(args.get("target-encoding"));
        } catch (IllegalArgumentException e) {
            JAISBaL.getOut().println("Unknown charset \"" + args.get("target-encoding") + "\"");
            return;
        }

        String content = args.get("content");
        content = PlasmaStringUtil.deEscape(content);
        content = content == null ? args.get("content") : content;
        
        switch (args.get("action")) {
            case "exec": {
                CommonMode.commonExec(content, args);
                break;
            }
            case "minify": {
                JAISBaL.getOut().println(Actions.MINIFY.apply(content));
                break;
            }
            case "explain": {
                JAISBaL.getOut().println(Actions.EXPLAIN.apply(content));
                break;
            }
            case "encode": {
                String in = content;
                Charset charset = JAISBaLCharset.get(args.get("target-encoding"));
                CharsetEncoder encoder = charset.newEncoder();
                for (char c : in.toCharArray()) {
                    if (!encoder.canEncode(c)) {
                        JAISBaL.getOut().println("The " + args.get("encoding") + " Charset does not support '" + PlasmaStringUtil.escape(String.valueOf(c)) + "'");
                        return;
                    }
                }

                JAISBaL.getOut().println("Encoded bytes:");
                JAISBaL.getOut().println(Arrays.toString(in.getBytes(charset)));
                break;
            }
            default: {
                JAISBaL.getOut().println("Unknown action \"" + args.get("action" + "\""));
            }
        }

        if (JAISBaL.getIn() instanceof Terminable) {
            ((Terminable) JAISBaL.getIn()).terminate();
        }
    }

}
