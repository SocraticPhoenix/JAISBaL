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
package com.gmail.socraticphoenix.jaisbal.program.instructions.util;

import com.gmail.socraticphoenix.jaisbal.util.DangerousFunction;
import com.gmail.socraticphoenix.jaisbal.util.NumberNames;
import com.gmail.socraticphoenix.jaisbal.program.Program;
import com.gmail.socraticphoenix.plasma.collection.PlasmaListUtil;
import com.gmail.socraticphoenix.plasma.math.PlasmaMathUtil;
import com.gmail.socraticphoenix.plasma.reflection.CastableValue;
import com.gmail.socraticphoenix.plasma.string.BracketCounter;
import com.gmail.socraticphoenix.plasma.string.CharacterStream;
import com.gmail.socraticphoenix.plasma.string.PlasmaStringUtil;
import com.gmail.socraticphoenix.plasma.string.QuotationTracker;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public interface InstructionUtility {
    BigDecimal SQRT_DIG = new BigDecimal(150);
    BigDecimal SQRT_PRE = new BigDecimal(1).divide(new BigDecimal(10).pow(SQRT_DIG.intValue()));
    BigDecimal TWO = new BigDecimal("2");
    List<String> TRUTHY = PlasmaListUtil.buildList("true", "yes", "y", "t");

    static CastableValue name(CastableValue value) {
        if (value.getValueAs(BigDecimal.class).isPresent()) {
            try {
                return CastableValue.of(NumberNames.convert(value.getValueAs(BigDecimal.class).get().intValueExact()));
            } catch (ArithmeticException e) {
                return CastableValue.of(value.getAsString().get());
            }
        } else if (value.getAsString().isPresent()) {
            return CastableValue.of(value.getAsString().get());
        } else if (value.getValueAs(CastableValue[].class).isPresent()) {
            CastableValue[] array = value.getValueAs(CastableValue[].class).get();
            for (int i = 0; i < array.length; i++) {
                array[i] = CastableValue.of(InstructionUtility.name(array[i]));
            }
            return CastableValue.of(array);
        }

        throw new IllegalStateException();
    }

    static ConstantInstruction constant(CastableValue value, String name) {
        return new ConstantInstruction(value, "push " + name + " onto the stack", "A constant that pushes " + name + " onto the stack", name.length() == 1 ? new String[0] : new String[]{name});
    }

    static BigDecimal sqrt(BigDecimal c, BigDecimal xn, BigDecimal precision) {
        BigDecimal fx = xn.pow(2).add(c.negate());
        BigDecimal fpx = xn.multiply(TWO);
        BigDecimal xn1 = fx.divide(fpx, 2 * SQRT_DIG.intValue(), RoundingMode.HALF_DOWN);
        xn1 = xn.add(xn1.negate());
        BigDecimal currentSquare = xn1.pow(2);
        BigDecimal currentPrecision = currentSquare.subtract(c).abs();
        if (currentPrecision.compareTo(precision) < 0) {
            return xn1;
        } else {
            return sqrt(c, xn1, precision);
        }
    }

    static BigDecimal sqrt(BigDecimal c) {
        return sqrt(c, new BigDecimal(1), SQRT_PRE);
    }

    static boolean truthy(CastableValue value) {
        if (value.getValueAs(BigDecimal.class).isPresent()) {
            return value.getValueAs(BigDecimal.class).get().compareTo(BigDecimal.ZERO) > 0;
        } else if (value.getAsString().isPresent()) {
            return InstructionUtility.TRUTHY.contains(value.getAsString().get().toLowerCase());
        } else if (value.getValueAs(CastableValue[].class).isPresent()) {
            CastableValue[] array = value.getValueAs(CastableValue[].class).get();
            int truthy = 0;
            int falsy = 0;
            for (CastableValue val : array) {
                if (InstructionUtility.truthy(val)) {
                    truthy++;
                } else {
                    falsy++;
                }
            }
            return truthy > falsy;
        }
        throw new IllegalStateException();
    }

    static CastableValue concat(CastableValue a, CastableValue b) {
        if (a.getAsString().isPresent() && b.getAsString().isPresent()) {
            return CastableValue.of(a.getAsString().get() + b.getAsString().get());
        } else if (a.getValueAs(CastableValue[].class).isPresent() && b.getValueAs(CastableValue[].class).isPresent()) {
            CastableValue[] ar = a.getValueAs(CastableValue[].class).get();
            CastableValue[] br = b.getValueAs(CastableValue[].class).get();
            CastableValue[] newa = new CastableValue[ar.length + br.length];
            int ind = 0;
            for (CastableValue anAr : ar) {
                newa[ind] = anAr;
                ind++;
            }
            for (CastableValue aBr : br) {
                newa[ind] = aBr;
                ind++;
            }
            return CastableValue.of(newa);
        } else if ((a.getValueAs(CastableValue[].class).isPresent() && !b.getValueAs(CastableValue[].class).isPresent()) || (b.getValueAs(CastableValue[].class).isPresent() && !a.getValueAs(CastableValue[].class).isPresent())) {
            CastableValue[] array = a.getValueAs(CastableValue[].class).isPresent() ? a.getValueAs(CastableValue[].class).get() : b.getValueAs(CastableValue[].class).get();
            CastableValue scalar = a.getValueAs(CastableValue[].class).isPresent() ? b : a;
            if (a.getValueAs(CastableValue[].class).isPresent()) {
                CastableValue[] newa = new CastableValue[array.length + 1];
                System.arraycopy(array, 0, newa, 0, array.length);
                newa[newa.length - 1] = scalar;
                return CastableValue.of(newa);
            } else {
                CastableValue[] newa = new CastableValue[array.length + 1];
                newa[0] = scalar;
                System.arraycopy(array, 0, newa, 1, array.length);
                return CastableValue.of(newa);
            }
        }
        throw new IllegalStateException();
    }

    static CastableValue div(CastableValue a, CastableValue b) {
        if (a.getValueAs(BigDecimal.class).isPresent() && b.getValueAs(BigDecimal.class).isPresent()) {
            BigDecimal ae = a.getValueAs(BigDecimal.class).get();
            BigDecimal be = b.getValueAs(BigDecimal.class).get();
            return CastableValue.of(ae.divide(be, ae.scale() + be.scale(), BigDecimal.ROUND_FLOOR).stripTrailingZeros());
        } else if (a.getAsString().isPresent() && b.getAsString().isPresent()) {
            int i = 0;
            String as = a.getAsString().get();
            String bs = b.getAsString().get();
            for (char c : as.toCharArray()) {
                if (!bs.contains(String.valueOf(c))) {
                    i++;
                }
            }
            return CastableValue.of(new BigDecimal(i));
        } else if ((a.getValueAs(CastableValue[].class).isPresent() && !b.getValueAs(CastableValue[].class).isPresent()) || (b.getValueAs(CastableValue[].class).isPresent() && !a.getValueAs(CastableValue[].class).isPresent())) {
            CastableValue[] array = a.getValueAs(CastableValue[].class).isPresent() ? a.getValueAs(CastableValue[].class).get() : b.getValueAs(CastableValue[].class).get();
            CastableValue scalar = a.getValueAs(CastableValue[].class).isPresent() ? b : a;
            for (int i = 0; i < array.length; i++) {
                array[i] = InstructionUtility.div(array[i], scalar);
            }
            return CastableValue.of(array);
        } else if (a.getValueAs(CastableValue[].class).isPresent() && b.getValueAs(CastableValue[].class).isPresent()) {
            CastableValue[] ar = a.getValueAs(CastableValue[].class).get();
            CastableValue[] br = a.getValueAs(CastableValue[].class).get();
            CastableValue[] newa = new CastableValue[Math.min(ar.length, br.length)];
            for (int i = 0; i < newa.length; i++) {
                if (i < ar.length && i < br.length) {
                    newa[i] = InstructionUtility.div(ar[i], br[i]);
                } else if (i < ar.length) {
                    newa[i] = ar[i];
                } else if (i < br.length) {
                    newa[i] = br[i];
                }
                return CastableValue.of(newa);
            }
        }
        throw new IllegalStateException();
    }

    static CastableValue sub(CastableValue a, CastableValue b) {
        if (a.getValueAs(BigDecimal.class).isPresent() && b.getValueAs(BigDecimal.class).isPresent()) {
            return CastableValue.of(a.getValueAs(BigDecimal.class).get().subtract(b.getValueAs(BigDecimal.class).get()).stripTrailingZeros());
        } else if (a.getAsString().isPresent() && b.getAsString().isPresent()) {
            int i = 0;
            String as = a.getAsString().get();
            String bs = b.getAsString().get();
            while (as.contains(bs)) {
                as = as.replaceFirst(Pattern.quote(as), "");
                i++;
            }
            return CastableValue.of(new BigDecimal(i));
        } else if ((a.getValueAs(CastableValue[].class).isPresent() && !b.getValueAs(CastableValue[].class).isPresent()) || (b.getValueAs(CastableValue[].class).isPresent() && !a.getValueAs(CastableValue[].class).isPresent())) {
            CastableValue[] array = a.getValueAs(CastableValue[].class).isPresent() ? a.getValueAs(CastableValue[].class).get() : b.getValueAs(CastableValue[].class).get();
            CastableValue scalar = a.getValueAs(CastableValue[].class).isPresent() ? b : a;
            for (int i = 0; i < array.length; i++) {
                array[i] = InstructionUtility.sub(array[i], scalar);
            }
            return CastableValue.of(array);
        } else if (a.getValueAs(CastableValue[].class).isPresent() && b.getValueAs(CastableValue[].class).isPresent()) {
            CastableValue[] ar = a.getValueAs(CastableValue[].class).get();
            CastableValue[] br = a.getValueAs(CastableValue[].class).get();
            CastableValue[] newa = new CastableValue[Math.min(ar.length, br.length)];
            for (int i = 0; i < newa.length; i++) {
                if (i < ar.length && i < br.length) {
                    newa[i] = InstructionUtility.sub(ar[i], br[i]);
                } else if (i < ar.length) {
                    newa[i] = ar[i];
                } else if (i < br.length) {
                    newa[i] = br[i];
                }
                return CastableValue.of(newa);
            }
        }
        throw new IllegalStateException();
    }

    static CastableValue add(CastableValue a, CastableValue b) {
        if (a.getValueAs(BigDecimal.class).isPresent() && b.getValueAs(BigDecimal.class).isPresent()) {
            return CastableValue.of(a.getValueAs(BigDecimal.class).get().add(b.getValueAs(BigDecimal.class).get()).stripTrailingZeros());
        } else if (a.getAsString().isPresent() && b.getAsString().isPresent()) {
            String as = a.getAsString().get();
            String bs = b.getAsString().get();
            int i = 0;
            for (String s : PlasmaStringUtil.allPossibleSubs(bs)) {
                if (as.contains(s) && s.length() > i) {
                    i = s.length();
                }
            }
            return CastableValue.of(new BigDecimal(i));
        } else if ((a.getValueAs(CastableValue[].class).isPresent() && !b.getValueAs(CastableValue[].class).isPresent()) || (b.getValueAs(CastableValue[].class).isPresent() && !a.getValueAs(CastableValue[].class).isPresent())) {
            CastableValue[] array = a.getValueAs(CastableValue[].class).isPresent() ? a.getValueAs(CastableValue[].class).get() : b.getValueAs(CastableValue[].class).get();
            CastableValue scalar = a.getValueAs(CastableValue[].class).isPresent() ? b : a;
            for (int i = 0; i < array.length; i++) {
                array[i] = InstructionUtility.add(array[i], scalar);
            }
            return CastableValue.of(array);
        } else if (a.getValueAs(CastableValue[].class).isPresent() && b.getValueAs(CastableValue[].class).isPresent()) {
            CastableValue[] ar = a.getValueAs(CastableValue[].class).get();
            CastableValue[] br = a.getValueAs(CastableValue[].class).get();
            CastableValue[] newa = new CastableValue[Math.max(ar.length, br.length)];
            for (int i = 0; i < newa.length; i++) {
                if (i < ar.length && i < br.length) {
                    newa[i] = InstructionUtility.add(ar[i], br[i]);
                } else if (i < ar.length) {
                    newa[i] = ar[i];
                } else if (i < br.length) {
                    newa[i] = br[i];
                }
                return CastableValue.of(newa);
            }
        }
        throw new IllegalStateException();
    }

    static CastableValue mul(CastableValue a, CastableValue b) {
        if (a.getValueAs(BigDecimal.class).isPresent() && b.getValueAs(BigDecimal.class).isPresent()) {
            return CastableValue.of(a.getValueAs(BigDecimal.class).get().multiply(b.getValueAs(BigDecimal.class).get()).stripTrailingZeros());
        } else if ((a.getAsString().isPresent() && b.getValueAs(BigDecimal.class).isPresent()) || (b.getAsString().isPresent() && a.getValueAs(BigDecimal.class).isPresent())) {
            BigDecimal bd = b.getValueAs(BigDecimal.class).isPresent() ? b.getValueAs(BigDecimal.class).get() : a.getValueAs(BigDecimal.class).get();
            String s = b.getValueAs(BigDecimal.class).isPresent() ? a.getAsString().get() : b.getAsString().get();
            StringBuilder builder = new StringBuilder();
            BigInteger i = bd.toBigInteger();
            for (BigInteger j = BigInteger.ZERO; j.compareTo(i) < 0; j = j.add(BigInteger.ONE)) {
                builder.append(s);
            }
            int additional = (int) (s.length() * InstructionUtility.getFraction(bd).doubleValue());
            for (int j = 0; j < additional && j < s.length(); j++) {
                builder.append(s.charAt(j));
            }
            return CastableValue.of(builder.toString());
        } else if (a.getAsString().isPresent() && b.getAsString().isPresent()) {
            int i = 0;
            String as = a.getAsString().get();
            String bs = b.getAsString().get();
            for (char c : as.toCharArray()) {
                if (bs.contains(String.valueOf(c))) {
                    i++;
                }
            }
            return CastableValue.of(new BigDecimal(i));
        } else if ((a.getValueAs(CastableValue[].class).isPresent() && !b.getValueAs(CastableValue[].class).isPresent()) || (b.getValueAs(CastableValue[].class).isPresent() && !a.getValueAs(CastableValue[].class).isPresent())) {
            CastableValue[] array = a.getValueAs(CastableValue[].class).isPresent() ? a.getValueAs(CastableValue[].class).get() : b.getValueAs(CastableValue[].class).get();
            CastableValue scalar = a.getValueAs(CastableValue[].class).isPresent() ? b : a;
            for (int i = 0; i < array.length; i++) {
                array[i] = InstructionUtility.mul(array[i], scalar);
            }
            return CastableValue.of(array);
        } else if (a.getValueAs(CastableValue[].class).isPresent() && b.getValueAs(CastableValue[].class).isPresent()) {
            CastableValue[] ar = a.getValueAs(CastableValue[].class).get();
            CastableValue[] br = a.getValueAs(CastableValue[].class).get();
            CastableValue[] newa = new CastableValue[Math.max(ar.length, br.length)];
            for (int i = 0; i < newa.length; i++) {
                if (i < ar.length && i < br.length) {
                    newa[i] = InstructionUtility.mul(ar[i], br[i]);
                } else if (i < ar.length) {
                    newa[i] = ar[i];
                } else if (i < br.length) {
                    newa[i] = br[i];
                }
            }
            return CastableValue.of(newa);
        }
        throw new IllegalStateException();
    }

    static BigDecimal getFraction(BigDecimal b) {
        return b.abs().subtract(b.abs().setScale(0, BigDecimal.ROUND_FLOOR).abs()).abs();
    }

    static int compare(CastableValue a, CastableValue b) {
        if (a.getValueAs(CastableValue[].class).isPresent() && b.getValueAs(CastableValue[].class).isPresent()) {
            CastableValue[] aa = a.getValueAs(CastableValue[].class).get();
            CastableValue[] bb = b.getValueAs(CastableValue[].class).get();
            int compare = 0;
            for (int i = 0; i < Math.min(aa.length, bb.length); i++) {
                compare += InstructionUtility.compare(aa[i], bb[i]);
            }
            compare += aa.length - bb.length;
            return compare;
        } else if (a.getValueAs(CastableValue[].class).isPresent()) {
            return 1;
        } else if (b.getValueAs(CastableValue[].class).isPresent()) {
            return -1;
        } else if (a.getValueAs(BigDecimal.class).isPresent() && b.getValueAs(BigDecimal.class).isPresent()) {
            return a.getValueAs(BigDecimal.class).get().compareTo(b.getValueAs(BigDecimal.class).get());
        } else {
            return a.getAsString().get().compareTo(b.getAsString().get());
        }
    }

    static CastableValue negate(CastableValue value) {
        if (value.getValueAs(BigDecimal.class).isPresent()) {
            BigDecimal decimal = value.getValueAs(BigDecimal.class).get();
            if(decimal.equals(BigDecimal.ZERO)) {
                return CastableValue.of(BigDecimal.ONE);
            } else {
                return CastableValue.of(decimal.negate());
            }
        } else if (value.getAsString().isPresent()) {
            String s = value.getAsString().get();
            if (InstructionUtility.TRUTHY.contains(s.toLowerCase())) {
                return CastableValue.of("false");
            } else {
                return CastableValue.of("true");
            }
        } else if (value.getValueAs(CastableValue[].class).isPresent()) {
            CastableValue[] old = value.getValueAs(CastableValue[].class).get();
            CastableValue[] newa = new CastableValue[old.length];
            for (int i = 0; i < old.length; i++) {
                newa[i] = InstructionUtility.negate(old[i]);
            }
            return CastableValue.of(newa);
        }

        throw new IllegalStateException();
    }

    static DangerousFunction<CharacterStream, String> fixed(int i) {
        return c -> c.next(i);
    }

    static DangerousFunction<CharacterStream, String> number() {
        return c -> {
            boolean neg = c.isNext('-');
            if (neg) {
                c.consume('-');
            }
            String num = c.nextWhile((Predicate<String>) PlasmaMathUtil::isBigDecimal);
            if (num.equals("")) {
                if (neg) {
                    c.back();
                }
                return num;
            } else {
                return (neg ? "-" : "") + num;
            }
        };
    }

    static BigInteger greatestCommonFactor(BigInteger a, BigInteger b) {
        a = a.abs();
        b = b.abs();
        return b.equals(BigInteger.ZERO) ? a : InstructionUtility.greatestCommonFactor(b, a.mod(b));
    }

    static DangerousFunction<CharacterStream, String> terminated() {
        return c -> {
            BracketCounter counter = new BracketCounter();
            counter.registerBrackets('[', ']');
            String s = c.nextUntil(z -> z == '}', counter, new QuotationTracker(), Program.ESCAPER, false);
            boolean end = c.isNext('}');
            c.consume('}');
            return s + (end ? "}" : "");
        };
    }
}
