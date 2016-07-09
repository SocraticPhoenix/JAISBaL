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
package com.gmail.socraticphoenix.jaisbal.modes;

import com.gmail.socraticphoenix.jaisbal.encode.JAISBaLCharset;
import com.gmail.socraticphoenix.jaisbal.util.DangerousConsumer;
import com.gmail.socraticphoenix.jaisbal.util.JAISBaLExecutionException;
import com.gmail.socraticphoenix.plasma.string.PlasmaStringUtil;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;
import java.util.Map;

public class InputMode implements DangerousConsumer<Map<String, String>> {

    @Override
    public void accept(Map<String, String> args) throws JAISBaLExecutionException, IOException {
        if (!args.containsKey("content")) {
            System.out.println("No content specified");
            return;
        }

        try {
            JAISBaLCharset.get(args.get("encoding"));
            JAISBaLCharset.get(args.get("target-encoding"));
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown charset \"" + args.get("encoding") + "\"");
        }

        switch (args.get("action")) {
            case "exec": {
                CommonMode.commonExec(args.get("content"), args);
                break;
            }
            case "minify": {
                System.out.println(Actions.MINIFY.apply(args.get("content")));
                break;
            }
            case "explain": {
                System.out.println(Actions.EXPLAIN.apply(args.get("content")));
                break;
            }
            case "encode": {
                String in = args.get("content");
                Charset charset = JAISBaLCharset.get(args.get("target-encoding"));
                CharsetEncoder encoder = charset.newEncoder();
                for (char c : in.toCharArray()) {
                    if (!encoder.canEncode(c)) {
                        System.out.println("The " + args.get("encoding") + " Charset does not support '" + PlasmaStringUtil.escape(String.valueOf(c)) + "'");
                        return;
                    }
                }

                System.out.println("Encoded bytes:");
                System.out.println(Arrays.toString(in.getBytes(charset)));
                break;
            }
            default: {
                System.out.println("Unknown action \"" + args.get("action" + "\""));
            }
        }
    }

}
