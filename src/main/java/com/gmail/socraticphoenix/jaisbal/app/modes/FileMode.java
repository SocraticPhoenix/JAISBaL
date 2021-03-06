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
import com.gmail.socraticphoenix.jaisbal.util.DangerousConsumer;
import com.gmail.socraticphoenix.jaisbal.util.InSupplier;
import com.gmail.socraticphoenix.jaisbal.util.Terminable;
import com.gmail.socraticphoenix.jaisbal.encode.JAISBaLCharset;
import com.gmail.socraticphoenix.plasma.file.PlasmaFileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class FileMode implements DangerousConsumer<Map<String, String>> {

    @Override
    public void accept(Map<String, String> args) throws Throwable {
        JAISBaL.setIn(new InSupplier());

        if (!args.containsKey("file")) {
            JAISBaL.getOut().println("No file specified");
            return;
        }

        File file = new File(args.get("file"));
        if (!file.exists()) {
            JAISBaL.getOut().println("Couldn't find file \"" + file.getAbsolutePath() + "\"");
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

        StringBuilder content = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(new FileInputStream(file), JAISBaLCharset.get(args.get("encoding")));
        int i;
        while ((i = reader.read()) > -1) {
            content.append((char) i);
        }
        reader.close();

        switch (args.get("action")) {
            case "exec": {
                CommonMode.commonExec(content.toString(), args);
                break;
            }
            case "minify": {
                this.backup(file);
                String m = Actions.MINIFY.apply(content.toString());
                CommonMode.commonWrite(m, file, args);
                break;
            }
            case "explain": {
                this.backup(file);
                String e = Actions.EXPLAIN.apply(content.toString());
                CommonMode.commonWrite(e, file, args);
                break;
            }
            case "view": {
                JAISBaL.getOut().println(content);
            }
            case "encode": {
                this.backup(file);
                CommonMode.commonWrite(content.toString(), file, args);
                break;
            }
        }

        if (JAISBaL.getIn() instanceof Terminable) {
            ((Terminable) JAISBaL.getIn()).terminate();
        }
    }

    private void backup(File file) throws IOException {
        File x = new File("jaisbal-backup", file.getName());
        x.getAbsoluteFile().getParentFile().mkdirs();
        x.delete();
        PlasmaFileUtil.copy(file, x);
    }

}
