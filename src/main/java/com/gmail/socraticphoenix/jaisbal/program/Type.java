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
package com.gmail.socraticphoenix.jaisbal.program;

import com.gmail.socraticphoenix.jaisbal.util.JAISBaLExecutionException;
import com.gmail.socraticphoenix.plasma.base.PlasmaObject;
import com.gmail.socraticphoenix.plasma.math.PlasmaMathUtil;
import com.gmail.socraticphoenix.plasma.reflection.CastableValue;
import com.gmail.socraticphoenix.plasma.string.BracketCounter;
import com.gmail.socraticphoenix.plasma.string.CharacterStream;
import com.gmail.socraticphoenix.plasma.string.PlasmaStringUtil;
import com.gmail.socraticphoenix.plasma.string.QuotationTracker;
import com.gmail.socraticphoenix.plasma.string.StringParseException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Type extends PlasmaObject {
    public static final Type NUMBER = new Type(0, 'n', null);
    public static final Type STRING = new Type(0, 's', null);
    public static final Type WILDCARD = new Type(0, '?', null);
    public static final Type GENERAL_ARRAY = new Type(-1, 'a', Type.WILDCARD);

    private Type arrayType;
    private int array;
    private char type;

    public Type(int array, char type, Type arrayType) {
        this.array = array;
        this.type = type;
        this.arrayType = arrayType;
    }


    public static boolean hasTypeNext(String s) {
        return Type.hasTypeNext(new CharacterStream(s));
    }

    public static boolean hasTypeNext(CharacterStream stream) {
        return stream.isNext('a', 'n', 's', '?', 'i');
    }

    public static Type parse(String type) throws StringParseException {
        return Type.read(new CharacterStream(type));
    }

    public static Type read(CharacterStream stream) throws StringParseException {
        if (stream.hasNext()) {
            if (!Type.hasTypeNext(stream)) {
                throw stream.syntaxError("Unknown type");
            }
            char next = stream.next().get();
            if (next == 'a') {
                String times = stream.nextWhile((Predicate<String>) PlasmaMathUtil::isInteger);
                Type sub = Type.read(stream);
                return new Type(times.equals("") ? -1 : Integer.parseInt(times), next, sub);
            } else {
                return new Type(0, next, null);
            }
        } else {
            throw stream.syntaxError("Expected type");
        }
    }


    public static CastableValue easyReadValues(CharacterStream stream, Program program) {
        CastableValue[] values = Type.readValues(stream, program);
        if (values.length == 1) {
            return values[0];
        } else {
            return new CastableValue(values);
        }
    }

    public static CastableValue[] readValues(CharacterStream stream, Program program) {
        BracketCounter counter = new BracketCounter();
        counter.registerBrackets('[', ']');

        String value = stream.remaining();
        boolean hasEnd = value.endsWith("}");
        if (hasEnd) {
            value = PlasmaStringUtil.cutLastChar(value);
        }
        stream = new CharacterStream(value);
        List<CastableValue> values = new ArrayList<>();
        if (stream.isNext('[')) {
            while (stream.hasNext()) {
                String s = stream.nextUntil(c -> false, counter, new QuotationTracker(), Program.ESCAPER, false, true);
                if (s.equals("")) {
                    break;
                }

                stream.consume(']');
                values.add(Type.easyReadValues(new CharacterStream(PlasmaStringUtil.cutFirstChar(s)), program));
            }
        } else {
            String v = Program.ESCAPER.deEscape(value);
            String s = PlasmaStringUtil.deEscape(v);
            return new CastableValue[]{new CastableValue(s == null ? v : s)};
        }

        return values.toArray(new CastableValue[values.size()]);
    }

    public String getName() {
        if (this.isImplicit()) {
            return "value";
        } else if (this.isString()) {
            return "string";
        } else if (this.isNumber()) {
            return "number";
        } else if (this.isWildcard()) {
            return "value";
        } else if (this.isArray()) {
            return this.arrayType.getName() + "[]";
        } else {
            return "unknown(" + this.type + ")";
        }
    }

    public String toString() {
        if (this.isArray()) {
            return 'a' + "" + this.array + this.arrayType.toString();
        } else {
            return String.valueOf(this.type);
        }
    }

    public boolean matches(CastableValue value) {
        if (this.isWildcard() || this.isImplicit()) {
            return true;
        } else if (this.isArray() && value.getValueAs(CastableValue[].class).isPresent()) {
            if(this.array == value.getValueAs(CastableValue[].class).get().length || this.array == -1) {
                for (CastableValue v : value.getValueAs(CastableValue[].class).get()) {
                    if (!this.arrayType.matches(v)) {
                        return false;
                    }
                }
            } else {
                return false;
            }
            return true;
        } else {
            return (this.isNumber() && value.getValueAs(BigDecimal.class).isPresent()) || (this.isString() && value.getAsString().isPresent());
        }
    }

    public void checkMatches(CastableValue value) throws JAISBaLExecutionException {
        if (!this.matches(value)) {
            throw new JAISBaLExecutionException("Invalid value: " + Program.valueToString(value) + " cannot be converted to " + this.getName());
        }
    }

    public boolean isWildcard() {
        return this.type == '?' && !this.isArray();
    }

    public boolean isArray() {
        return this.array != 0;
    }

    public boolean isNumber() {
        return this.type == 'n' && !this.isArray();
    }

    public boolean isString() {
        return this.type == 's' && !this.isArray();
    }

    public CastableValue createValue() {
        if (this.isString()) {
            return CastableValue.of(this.createString());
        } else if (this.isNumber()) {
            return CastableValue.of(this.createNumber());
        } else { //this.isArray
            return CastableValue.of(this.createArray());
        }
    }

    public BigDecimal createNumber() {
        return BigDecimal.ZERO;
    }

    public String createString() {
        return "";
    }

    public CastableValue[] createArray() {
        CastableValue[] array = new CastableValue[this.array];
        for (int i = 0; i < array.length; i++) {
            array[i] = this.arrayType.createValue();
        }
        return array;
    }

    public Type getArrayType() {
        return this.arrayType;
    }

    public char getType() {
        return this.type;
    }

    public int getArray() {
        return this.array;
    }

    public boolean isKnown() {
        return (this.isNumber() || this.isString() || this.isWildcard() || this.isImplicit()) || (this.isArray());
    }

    public boolean isImplicit() {
        return this.type == 'i' && !this.isArray();
    }
}
