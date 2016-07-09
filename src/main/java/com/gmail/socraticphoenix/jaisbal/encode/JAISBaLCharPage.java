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
package com.gmail.socraticphoenix.jaisbal.encode;

import com.gmail.socraticphoenix.plasma.file.PlasmaFileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class JAISBaLCharPage {
    private static Map<String, Integer> namings;

    static {
        namings = new HashMap<>();
        namings.put("A", 250);
        namings.put("B", 251);
        namings.put("C", 252);
        namings.put("D", 253);
        namings.put("E", 254);
        namings.put("F", 255);
    }

    private char[] mappings;
    private String name;

    public JAISBaLCharPage(String s, String name) {
        this.name = name;
        this.mappings = s.toCharArray();
    }

    public JAISBaLCharPage(char[] s, String name) {
        this.mappings = s;
        this.name = name;
    }

    public static JAISBaLCharPage of(String name, int length) throws IOException {
        InputStream stream = PlasmaFileUtil.getResource("page-" + name + ".txt");
        if(stream == null) {
            throw new IllegalArgumentException("No known code page called '" + name + "'");
        }

        InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = (char) reader.read();
        }
        reader.close();
        return new JAISBaLCharPage(chars, name);
    }

    public boolean canEncode(char c) {
        for(char z : this.mappings) {
            if(z == c) {
                return true;
            }
        }

        return false;
    }

    public char[] getMappings() {
        return this.mappings;
    }

    public boolean isPageByte(byte c) {
        return Byte.toUnsignedInt(c) == this.getPageByte();
    }

    public int getPageByte() {
        if(JAISBaLCharPage.namings.containsKey(this.getName())) {
            return JAISBaLCharPage.namings.get(this.getName());
        } else {
            return -1;
        }
    }

    public String getName() {
        return this.name;
    }

    public boolean canDecode(byte b) {
        return this.mappings.length < Byte.toUnsignedInt(b);
    }

    public char decode(byte b) {
        return this.mappings[Byte.toUnsignedInt(b)];
    }

    public byte encode(char c) {
        for (int i = 0; i < this.mappings.length; i++) {
            if(this.mappings[i] == c) {
                return (byte) i;
            }
        }
        return 0;
    }

}
