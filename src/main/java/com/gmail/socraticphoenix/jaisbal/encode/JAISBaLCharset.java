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

import com.gmail.socraticphoenix.jaisbal.JAISBaL;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.List;
import java.util.Optional;

public class JAISBaLCharset extends Charset {
    private static JAISBaLCharset charset;

    protected JAISBaLCharset(String canonicalName, String[] aliases) {
        super(canonicalName, aliases);
    }

    public static void init() {
        JAISBaLCharset.charset = new JAISBaLCharset("JAISBAL", new String[0]);
    }

    public static Charset get(String name) {
        if(name.equalsIgnoreCase(JAISBaLCharset.getCharset().name())) {
            return JAISBaLCharset.getCharset();
        } else {
            return Charset.forName(name);
        }
    }

    public static JAISBaLCharset getCharset() {
        return JAISBaLCharset.charset;
    }

    @Override
    public boolean contains(Charset cs) {
        return false;
    }

    @Override
    public CharsetDecoder newDecoder() {
        return new Decoder();
    }

    @Override
    public CharsetEncoder newEncoder() {
        return new Encoder();
    }

    private static class Encoder extends CharsetEncoder {
        private List<JAISBaLCharPage> pages = JAISBaL.getAllPages();

        public Encoder() {
            this(getCharset(), 2, 2,  new byte[] {31});
        }

        protected Encoder(Charset cs, float averageBytesPerChar, float maxBytesPerChar, byte[] replacement) {
            super(cs, averageBytesPerChar, maxBytesPerChar, replacement);
        }

        @Override
        protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
            while (in.hasRemaining() && out.hasRemaining()) {
                char c = in.get();
                Optional<JAISBaLCharPage> charPageOptional = this.pages.stream().filter(page -> page.canEncode(c)).findFirst();
                if(charPageOptional.isPresent()) {
                    JAISBaLCharPage page = charPageOptional.get();
                    if(page.getPageByte() == -1) {
                       out.put(page.encode(c));
                    } else if (out.remaining() >= 2) {
                        out.put((byte) page.getPageByte());
                        out.put(page.encode(c));
                    } else {
                        return CoderResult.UNDERFLOW;
                    }
                } else {
                    return CoderResult.unmappableForLength(1);
                }
            }

            return CoderResult.UNDERFLOW;
        }
    }

    private static class Decoder extends CharsetDecoder {
        private List<JAISBaLCharPage> pages = JAISBaL.getAllPages();

        public Decoder() {
            this(getCharset(), 0.5f, 1);
        }

        protected Decoder(Charset cs, float averageCharsPerByte, float maxCharsPerByte) {
            super(cs, averageCharsPerByte, maxCharsPerByte);
        }

        @Override
        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
            while (in.hasRemaining() && out.hasRemaining()) {
                byte b = in.get();
                Optional<JAISBaLCharPage> charPageOptional = this.pages.stream().filter(page -> page.isPageByte(b)).findFirst();
                if(charPageOptional.isPresent()) {
                    JAISBaLCharPage page = charPageOptional.get();
                    if(in.hasRemaining()) {
                        out.put(page.decode(in.get()));
                    } else {
                        return CoderResult.UNDERFLOW;
                    }
                } else {
                    out.put(JAISBaL.getRootPage().decode(b));
                }
            }
            return CoderResult.UNDERFLOW;
        }
    }
}
