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
package com.gmail.socraticphoenix.jaisbal.program.instructions;

import com.gmail.socraticphoenix.jaisbal.JAISBaL;
import com.gmail.socraticphoenix.jaisbal.program.Program;
import com.gmail.socraticphoenix.jaisbal.program.instructions.constants.StandardConstants;
import com.gmail.socraticphoenix.jaisbal.program.instructions.instructions.ArrayInstructions;
import com.gmail.socraticphoenix.jaisbal.program.instructions.instructions.ConditionalInstructions;
import com.gmail.socraticphoenix.jaisbal.program.instructions.instructions.ControlFlowInstructions;
import com.gmail.socraticphoenix.jaisbal.program.instructions.instructions.FundamentalInstructions;
import com.gmail.socraticphoenix.jaisbal.program.instructions.instructions.MathematicalInstructions;
import com.gmail.socraticphoenix.jaisbal.program.instructions.instructions.MiscellaneousInstructions;
import com.gmail.socraticphoenix.jaisbal.program.instructions.instructions.StackInstructions;
import com.gmail.socraticphoenix.jaisbal.program.instructions.util.ConstantInstruction;
import com.gmail.socraticphoenix.jaisbal.program.instructions.util.InstructionUtility;
import com.gmail.socraticphoenix.plasma.reflection.CastableValue;
import com.gmail.socraticphoenix.plasma.string.PlasmaStringUtil;
import com.gmail.socraticphoenix.plasma.string.TableFormat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InstructionRegistry {
    private static List<Instruction> accessibleInstructions;

    private static List<Instruction> instructions;
    private static List<Instruction> supplementaryInstructions;
    private static List<ConstantInstruction> constants;
    private static List<Instruction> auxiliaryInstructions;
    private static List<CastableValue> auxiliaryConstants;
    private static List<String> floofyBlocks;
    private static List<String> blockStart;
    private static List<String> blockEnd;

    public static void registerDefaults() {
        InstructionRegistry.instructions = new ArrayList<>();
        InstructionRegistry.auxiliaryInstructions = new ArrayList<>();
        InstructionRegistry.auxiliaryConstants = new ArrayList<>();
        InstructionRegistry.supplementaryInstructions = new ArrayList<>();
        InstructionRegistry.constants = new ArrayList<>();
        InstructionRegistry.blockStart = new ArrayList<>();
        InstructionRegistry.blockEnd = new ArrayList<>();
        InstructionRegistry.floofyBlocks = new ArrayList<>();
        //Block ends and starts
        InstructionRegistry.getBlockStarts().add("for");
        InstructionRegistry.getBlockStarts().add("while");
        InstructionRegistry.getBlockStarts().add("dowhile");
        InstructionRegistry.getBlockStarts().add("ifblock");
        InstructionRegistry.getBlockStarts().add("ifelse");

        InstructionRegistry.getBlockEnds().add("end");

        InstructionRegistry.getBlockStarts().add("else");
        InstructionRegistry.getBlockEnds().add("else");
        InstructionRegistry.getFloofyBlocks().add("else");


        //-----------------------------------------------------------------------
        //Instructions, version 1
        //-----------------------------------------------------------------------

        //Fundamental instructions
        r(FundamentalInstructions.FUNCTION);
        r(FundamentalInstructions.AUX_FUNCTION);
        r(FundamentalInstructions.AUX_CONSTANT);


        //Stack instructions, Manipulators
            //Pushes
        r(StackInstructions.Manipulators.PUSH_NUMBER);
        r(StackInstructions.Manipulators.PUSH_TERMINATED);
        r(StackInstructions.Manipulators.PUSH_NEW_LINE);
        r(StackInstructions.Manipulators.PUSH_SPACE);
        r(StackInstructions.Manipulators.PUSH_TAB);
        r(StackInstructions.Manipulators.PUSH_1_CHAR);
        r(StackInstructions.Manipulators.PUSH_2_CHAR);
        r(StackInstructions.Manipulators.PUSH_3_CHAR);
        r(StackInstructions.Manipulators.PUSH_4_CHAR);
        r(StackInstructions.Manipulators.PUSH_5_CHAR);
        r(StackInstructions.Manipulators.PUSH_6_CHAR);
            //Pops
        r(StackInstructions.Manipulators.POP);
        r(StackInstructions.Manipulators.POP_ALL);
        r(StackInstructions.Manipulators.POP_ALL_BUT_ONE);
            //Swaps
        r(StackInstructions.Manipulators.SWAP);
        r(StackInstructions.Manipulators.SWAP_ALL);
            //Multipliers
        r(StackInstructions.Manipulators.DUPLICATE);
        r(StackInstructions.Manipulators.DUPLICATE_ALL);
        r(StackInstructions.Manipulators.TRIPLICATE);
        r(StackInstructions.Manipulators.TRIPLICATE_ALL);
        r(StackInstructions.Manipulators.DUPLICATE_MANY);
        r(StackInstructions.Manipulators.DUPLICATE_MANY_STACK);
            //Register Manipulators
        r(StackInstructions.Manipulators.STORE);
        r(StackInstructions.Manipulators.LOAD);
        r(StackInstructions.Manipulators.STORE_STACK);
        r(StackInstructions.Manipulators.LOAD_STACK);
        r(StackInstructions.Manipulators.STORE_ALL);
        r(StackInstructions.Manipulators.LOAD_ALL);
        r(StackInstructions.Manipulators.STORE_ALL_LOAD_ALL);
        //Stack instructions, Outputters
            //Push, output
        r(StackInstructions.Outputters.PUSH_NUMBER_OUTPUT);
        r(StackInstructions.Outputters.PUSH_TERMINATED_OUTPUT);
        r(StackInstructions.Outputters.NEW_LINE);
        r(StackInstructions.Outputters.SPACE);
        r(StackInstructions.Outputters.TAB);
        r(StackInstructions.Outputters.PUSH_1_OUTPUT);
        r(StackInstructions.Outputters.PUSH_2_OUTPUT);
        r(StackInstructions.Outputters.PUSH_3_OUTPUT);
        r(StackInstructions.Outputters.PUSH_4_OUTPUT);
        r(StackInstructions.Outputters.PUSH_5_OUTPUT);
        r(StackInstructions.Outputters.PUSH_6_OUTPUT);
            //Push, output, ln
        r(StackInstructions.Outputters.PUSH_NUMBER_OUTPUT_NEWLINE);
        r(StackInstructions.Outputters.PUSH_TERMINATED_OUTPUT_NEWLINE);
        r(StackInstructions.Outputters.PUSH_1_OUTPUT_LN);
        r(StackInstructions.Outputters.PUSH_2_OUTPUT_LN);
        r(StackInstructions.Outputters.PUSH_3_OUTPUT_LN);
        r(StackInstructions.Outputters.PUSH_4_OUTPUT_LN);
        r(StackInstructions.Outputters.PUSH_5_OUTPUT_LN);
        r(StackInstructions.Outputters.PUSH_6_OUTPUT_LN);
            //Pop, output
        r(StackInstructions.Outputters.POP_OUTPUT);
        r(StackInstructions.Outputters.POP_OUTPUT_ALL);
        r(StackInstructions.Outputters.POP_OUTPUT_RESTORE_ALL);
        r(StackInstructions.Outputters.POP_OUTPUT_NEWLINE);
        r(StackInstructions.Outputters.POP_OUTPUT_ALL_NEWLINE);
        r(StackInstructions.Outputters.POP_OUTPUT_RESTORE_ALL_NEWLINE);


        //Conditionals
            //Line skippers
        r(ConditionalInstructions.EQUAL);
        r(ConditionalInstructions.NOT_EQUAL);
        r(ConditionalInstructions.GREATER);
        r(ConditionalInstructions.LESS);
        r(ConditionalInstructions.GREATER_EQUAL);
        r(ConditionalInstructions.LESS_EQUAL);

        r(ConditionalInstructions.EQUAL_ALL);
        r(ConditionalInstructions.NOT_EQUAL_ALL);
        r(ConditionalInstructions.GREATER_ALL);
        r(ConditionalInstructions.LESS_ALL);
        r(ConditionalInstructions.GREATER_EQUAL_ALL);
        r(ConditionalInstructions.LESS_EQUAL_ALL);

        r(ConditionalInstructions.IF_TRUTHY);
        r(ConditionalInstructions.IF_FALSEY);
            //Pushes
        r(ConditionalInstructions.PUSH_TRUTHY);
        r(ConditionalInstructions.PUSH_FALSEY);
        r(ConditionalInstructions.NEGATE);
        r(ConditionalInstructions.COMPARE);


        //Control flow
            //General
        r(ControlFlowInstructions.END);
        r(ControlFlowInstructions.BREAK);
        r(ControlFlowInstructions.SUPER_PUSH);
            //Goto
        r(ControlFlowInstructions.RELATIVE_JUMP);
        r(ControlFlowInstructions.INDEX_JUMP);
            //Loops
        r(ControlFlowInstructions.FOR_LOOP);
        r(ControlFlowInstructions.WHILE);
        r(ControlFlowInstructions.DO_WHILE);
            //Conditional blocks
        r(ControlFlowInstructions.IF_BLOCK);
        r(ControlFlowInstructions.IF_ELSE_BLOCK);
        r(ControlFlowInstructions.ELSE);


        //Math, Operations
            //Basic 4
        r(MathematicalInstructions.Operations.ADD);
        r(MathematicalInstructions.Operations.SUBTRACT);
        r(MathematicalInstructions.Operations.MULTIPLY);
        r(MathematicalInstructions.Operations.DIVIDE);
            //Other operations
        r(MathematicalInstructions.Operations.POW);
        r(MathematicalInstructions.Operations.MODULO);
        r(MathematicalInstructions.Operations.SQRT);
            //Rounding operations
        r(MathematicalInstructions.Operations.FLOOR);
        r(MathematicalInstructions.Operations.CEIL);
        r(MathematicalInstructions.Operations.ROUND);
        //Math, Functions
            //Randomness
        r(MathematicalInstructions.Functions.RAND_DECIMAL);
        r(MathematicalInstructions.Functions.RAND_INTEGER);
        r(MathematicalInstructions.Functions.RAND_INTEGER_BOUNDED);
        r(MathematicalInstructions.Functions.RAND_INTEGER_BOUNDED_1);
        r(MathematicalInstructions.Functions.RAND_INTEGER_DOUBLE_BOUNDED);


        //Arrays
            //Base operations
        r(ArrayInstructions.ARRAY_CREATE);
        r(ArrayInstructions.ARRAY_CREATE_STACK);
        r(ArrayInstructions.ARRAY_LENGTH);
        r(ArrayInstructions.ARRAY_LOAD);
        r(ArrayInstructions.ARRAY_STORE);
        r(ArrayInstructions.ARRAY_LOAD_STACK);
        r(ArrayInstructions.ARRAY_STORE_STACK);
            //List operations
        r(ArrayInstructions.ARRAY_SORT);
        r(ArrayInstructions.ARRAY_SORT_REVERSE);
        r(ArrayInstructions.ROTATE);
        r(ArrayInstructions.ROTATE_NUMBER);
        r(ArrayInstructions.ROTATE_NUMBER_STACK);
        r(ArrayInstructions.ARRAY_RANGED);
        r(ArrayInstructions.ARRAY_RANGED_INCLUSIVE);
            //Stack and array conversions
        r(ArrayInstructions.ARRAY_WRAP);
        r(ArrayInstructions.POP_SPLIT_PUSH);
        r(ArrayInstructions.REVERSE);
            //Concatenation
        r(ArrayInstructions.CONCAT);
        r(ArrayInstructions.PUSH_NEW_LINE_CONCAT);
        r(ArrayInstructions.PUSH_SPACE_CONCAT);
        r(ArrayInstructions.PUSH_TAB_CONCAT);
            //String operations
        r(ArrayInstructions.SPLIT);
        r(ArrayInstructions.SPLIT_STACK);


        //Miscellaneous
        r(MiscellaneousInstructions.QUINE);
        r(MiscellaneousInstructions.NAME);

        //-----------------------------------------------------------------------
        //End Of Instructions, version 1
        //-----------------------------------------------------------------------



        //-----------------------------------------------------------------------
        //Constants, version 1
        //-----------------------------------------------------------------------

        //Constants
        rc(CastableValue.of(StandardConstants.NEGATIVE_ONE), "negative_one");
        rc(CastableValue.of(StandardConstants.HALF), "half");
        rc(CastableValue.of(StandardConstants.FOURTH), "one_quarter");
        rc(CastableValue.of(StandardConstants.THREE_QUARTER), "three_quarters");
        rc(CastableValue.of(StandardConstants.TEN), "ten");
        rc(CastableValue.of(StandardConstants.HUNDRED), "one_hundred");
        rc(CastableValue.of(StandardConstants.THOUSAND), "one_thousand");
        rc(CastableValue.of(StandardConstants.PI), "pi");
        rc(CastableValue.of(StandardConstants.TAU), "tau");
        rc(CastableValue.of(StandardConstants.E), "natural_base");
        rc(CastableValue.of(StandardConstants.PHI), "phi");

        for (int i = 4; i <= 100; i++) {
            rac(CastableValue.of(new BigDecimal("10").pow(i)));
        }

        //-----------------------------------------------------------------------
        //End Of Constants, version 1
        //-----------------------------------------------------------------------
    }

    public static void ra(Instruction instruction) {
        InstructionRegistry.registerAuxiliaryInstruction(instruction);
    }

    public static void rc(ConstantInstruction instruction) {
        InstructionRegistry.registerConstant(instruction);
    }

    public static void rc(CastableValue value, String name) {
        InstructionRegistry.registerConstant(InstructionUtility.constant(value, name));
    }

    public static void rc(Object value, String name) {
        rc(CastableValue.of(value), name);
    }

    public static void rac(Object value) {
        rac(CastableValue.of(value));
    }

    public static void rac(CastableValue value) {
        InstructionRegistry.registerAuxiliaryConstant(value);
    }

    public static void r(Instruction instruction) {
        InstructionRegistry.register(instruction);
    }

    public static void rsi(Instruction instruction) {
        InstructionRegistry.registerSupplementaryInstruction(instruction);
    }

    public static void checkIds() throws IllegalStateException {
        for(Instruction instruction : InstructionRegistry.instructions) {
            if(instruction.getId() == '\0') {
                throw new IllegalStateException(instruction.getMainAlias() + " has a null character id (\\0)");
            } else if (Program.CONTROL_CHARACTERS.indexOf(instruction.getId()) != -1) {
                throw new IllegalStateException(instruction.getMainAlias() + " has illegal control character id " + PlasmaStringUtil.escape(String.valueOf(instruction.getId())));
            }
        }

        for(Instruction instruction : InstructionRegistry.supplementaryInstructions) {
            if(instruction.getId() == '\0') {
                throw new IllegalStateException(instruction.getMainAlias() + " has a null character id (\\0)");
            } else if (Program.CONTROL_CHARACTERS.indexOf(instruction.getId()) != -1) {
                throw new IllegalStateException(instruction.getMainAlias() + " has illegal control character id " + PlasmaStringUtil.escape(String.valueOf(instruction.getId())));
            }
        }

        for(Instruction instruction : InstructionRegistry.constants) {
            if(instruction.getId() == '\0') {
                throw new IllegalStateException(instruction.getMainAlias() + " has a null character id (\\0)");
            } else if (Program.CONTROL_CHARACTERS.indexOf(instruction.getId()) != -1) {
                throw new IllegalStateException(instruction.getMainAlias() + " has illegal control character id " + PlasmaStringUtil.escape(String.valueOf(instruction.getId())));
            }
        }

        for(Instruction instruction : InstructionRegistry.auxiliaryInstructions) {
            if(instruction.getId() != '\0') {
                throw new IllegalStateException(instruction.getMainAlias() + " is auxiliary, and does not have a null character id (\\0), instead it has " + PlasmaStringUtil.escape(String.valueOf(instruction.getId())));
            } else if (Program.CONTROL_CHARACTERS.indexOf(instruction.getId()) != -1) {
                throw new IllegalStateException(instruction.getMainAlias() + " has illegal control character id " + PlasmaStringUtil.escape(String.valueOf(instruction.getId())));
            }
        }
    }

    public static void checkDuplicates() {
        List<Instruction> accessible = InstructionRegistry.getAccessibleInstructions();
        for (int i = 0; i < accessible.size(); i++) {
            for (int j = 0; j < accessible.size(); j++) {
                Instruction a = accessible.get(i);
                Instruction b = accessible.get(j);
                Optional<String> stringOptional = a.getAliases().stream().filter(b.getAliases()::contains).findFirst();
                if (i != j && stringOptional.isPresent()) {
                    throw new IllegalStateException("Duplicate alias \"" + stringOptional.get() + "\" for instructions " + accessible.get(i).getMainAlias() + " and " + accessible.get(j).getMainAlias());
                }
            }
        }
    }

    public static void checkNumbers() throws IllegalStateException {
        if(InstructionRegistry.instructions.size() > 204) {
            throw new IllegalStateException("More than 204 standard instructions are defined");
        } else if (InstructionRegistry.supplementaryInstructions.size() > JAISBaL.getSupplementaryPages().size() * 256) {
            throw new IllegalStateException("More than" + JAISBaL.getSupplementaryPages().size() * 256 + "supplementary instructions are defined");
        } else if (InstructionRegistry.constants.size() > JAISBaL.getConstantPages().size() * 256) {
            throw new IllegalStateException("More than" + JAISBaL.getConstantPages().size() * 256 + "standard constants are defined");
        }
    }

    public static void registerSupplementaryInstruction(Instruction instruction) {
        InstructionRegistry.supplementaryInstructions.add(instruction);
    }

    public static void registerAuxiliaryInstruction(Instruction instruction) {
        InstructionRegistry.auxiliaryInstructions.add(instruction);
    }

    public static void registerAuxiliaryConstant(CastableValue constant) {
        InstructionRegistry.auxiliaryConstants.add(constant);
    }

    public static void registerConstant(ConstantInstruction instruction) {
        InstructionRegistry.constants.add(instruction);
    }

    public static void register(Instruction instruction) {
        InstructionRegistry.instructions.add(instruction);
    }

    public static void registerBlockStart(String start) {
        InstructionRegistry.blockStart.add(start);
    }

    public static void registerBlockEnd(String end) {
        InstructionRegistry.blockEnd.add(end);
    }

    public static List<String> getBlockStarts() {
        return InstructionRegistry.blockStart;
    }

    public static List<String> getBlockEnds() {
        return InstructionRegistry.blockEnd;
    }

    public static List<Instruction> getSupplementaryInstructions() {
        return InstructionRegistry.supplementaryInstructions;
    }

    public static List<ConstantInstruction> getConstants() {
        return InstructionRegistry.constants;
    }

    public static List<Instruction> getAuxiliaryInstructions() {
        return InstructionRegistry.auxiliaryInstructions;
    }

    public static List<CastableValue> getAuxiliaryConstants() {
        return InstructionRegistry.auxiliaryConstants;
    }

    public static List<Instruction> getStandardInstructions() {
        return InstructionRegistry.instructions;
    }

    public static TableFormat getStandardInstructionsDocumentation() {
        return InstructionRegistry.of(InstructionRegistry.getStandardInstructions());
    }

    public static TableFormat getSupplementaryInstructionsDocumentation() {
        return InstructionRegistry.of(InstructionRegistry.getSupplementaryInstructions());
    }

    public static TableFormat getAccessibleInstructionsDocumentation() {
        return InstructionRegistry.of(InstructionRegistry.getAccessibleInstructions());
    }

    public static TableFormat getConstantsDocumentation() {
        TableFormat format = new TableFormat("ID", "Main Alias", "All Aliases", "Value", "Specification");
        InstructionRegistry.getConstants().stream().sorted((a, b) -> Double.compare(a.getGroup(), b.getGroup())).forEach(con -> {
            format.addRow(String.valueOf(con.getId()), con.getMainAlias(), PlasmaStringUtil.joinIntermediate(", ", con.getAliases().toArray()), check(Program.valueToString(con.getValue()), 10), con.getDocumentation());
        });
        return format;
    }

    public static TableFormat getAuxiliaryInstructionsDocumentation() {
        TableFormat format = new TableFormat("ID", "Specification");
        for (int i = 0; i < InstructionRegistry.auxiliaryInstructions.size(); i++) {
            Instruction aux = InstructionRegistry.auxiliaryInstructions.get(i);
            format.addRow(String.valueOf(i), aux.getDocumentation());
        }
        return format;
    }

    public static TableFormat getAuxiliaryConstantsDocumentation() {
        TableFormat format = new TableFormat("ID", "Value");
        for (int i = 0; i < InstructionRegistry.auxiliaryConstants.size(); i++) {
            CastableValue aux = InstructionRegistry.auxiliaryConstants.get(i);
            format.addRow(String.valueOf(i), check(Program.valueToString(aux), 10));
        }
        return format;
    }

    private static TableFormat of(List<Instruction> instructions) {
        TableFormat format = new TableFormat("ID", "Main Alias", "All Aliases", "Specification");
        instructions.stream().sorted((a, b) -> Double.compare(a.getGroup(), b.getGroup())).forEach(i -> format.addRow(String.valueOf(i.getId()), i.getMainAlias(), PlasmaStringUtil.joinIntermediate(", ", i.getAliases().toArray()), i.getDocumentation()));
        return format;
    }

    public static List<Instruction> getAccessibleInstructions() {
        if(InstructionRegistry.accessibleInstructions == null) {
            InstructionRegistry.accessibleInstructions = new ArrayList<>();
            InstructionRegistry.accessibleInstructions.addAll(InstructionRegistry.getStandardInstructions());
            InstructionRegistry.accessibleInstructions.addAll(InstructionRegistry.getSupplementaryInstructions());
            InstructionRegistry.accessibleInstructions.addAll(InstructionRegistry.getConstants());
        }
        return InstructionRegistry.accessibleInstructions;
    }

    public static String check(String s, int i) {
        if(s.length() > i) {
            s = s.substring(0, i) + "...";
        }
        return s;
    }

    public static List<String> getFloofyBlocks() {
        return floofyBlocks;
    }
}
