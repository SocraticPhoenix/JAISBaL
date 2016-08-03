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
package com.gmail.socraticphoenix.jaisbal.program.instructions.constants;

import com.gmail.socraticphoenix.jaisbal.program.instructions.util.InstructionUtility;

import java.math.BigDecimal;

public interface StandardConstants {
    BigDecimal NEGATIVE_ONE = BigDecimal.ONE.negate();
    BigDecimal HALF = new BigDecimal("0.5");
    BigDecimal FOURTH = new BigDecimal("0.25");
    BigDecimal THREE_QUARTER = new BigDecimal("0.75");
    BigDecimal TEN = BigDecimal.TEN;
    BigDecimal HUNDRED = new BigDecimal("100");
    BigDecimal THOUSAND = new BigDecimal("1000");

    BigDecimal PI = new BigDecimal("3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679");
    BigDecimal TAU = StandardConstants.PI.multiply(InstructionUtility.TWO);
    BigDecimal E = new BigDecimal("2.7182818284590452353602874713526624977572470936999595749669676277240766303535475945713821785251664274");
    BigDecimal PHI = new BigDecimal("1.6180339887498948482045868343656381177203091798057628621354486227052604628189024497072072041893911374");

}
