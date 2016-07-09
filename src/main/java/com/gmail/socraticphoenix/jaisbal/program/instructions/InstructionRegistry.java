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

import com.gmail.socraticphoenix.jaisbal.program.Program;
import com.gmail.socraticphoenix.plasma.reflection.CastableValue;
import com.gmail.socraticphoenix.plasma.string.PlasmaStringUtil;
import com.gmail.socraticphoenix.plasma.string.TableFormat;

import java.util.ArrayList;
import java.util.List;

public class InstructionRegistry implements Instructions {
    private static List<Instruction> accessibleInstructions;

    private static List<Instruction> instructions;
    private static List<Instruction> supplementaryInstructions;
    private static List<ConstantInstruction> constants;
    private static List<Instruction> auxiliaryInstructions;
    private static List<CastableValue> auxiliaryConstants;
    private static List<String> blockStart;
    private static List<String> blockEnd;

    static {
        InstructionRegistry.instructions = new ArrayList<>();
        InstructionRegistry.auxiliaryInstructions = new ArrayList<>();
        InstructionRegistry.auxiliaryConstants = new ArrayList<>();
        InstructionRegistry.supplementaryInstructions = new ArrayList<>();
        InstructionRegistry.constants = new ArrayList<>();
        InstructionRegistry.blockStart = new ArrayList<>();
        InstructionRegistry.blockEnd = new ArrayList<>();
    }

    public static void registerDefaults() {
        r(PUSH_NUMBER);
        r(PUSH_TERMINATED);
        for (int i = 1; i <= 6; i++) {
            r(Instructions.push(i));
        }
        r(PUSH_NUMBER_OUTPUT);
        r(PUSH_TERMINATED_OUTPUT);
        for (int i = 1; i <= 6; i++) {
            r(Instructions.pushOutput(i));
        }
        r(PUSH_NUMBER_OUTPUT_NEWLINE);
        r(PUSH_TERMINATED_OUTPUT_NEWLINE);
        for (int i = 1; i <= 6; i++) {
            r(Instructions.pushOutputNewLine(i));
        }
        r(POP);
        r(POP_ALL);
        r(SWAP);
        r(DUPLICATE);
        r(DUPLICATE_ALL);
        r(STORE);
        r(STORE_STACK);
        r(LOAD);
        r(LOAD_STACK);
        r(STORE_ALL);
        r(LOAD_ALL);
        r(STORE_ALL_LOAD_ALL);
        r(POP_OUTPUT);
        r(POP_OUTPUT_NEWLINE);
        r(POP_OUTPUT_ALL);
        r(POP_OUTPUT_ALL_NEWLINE);
        r(NEW_LINE);
        r(PUSH_NEW_LINE);
        r(PUSH_NEW_LINE_CONCAT);
        r(SPACE);
        r(PUSH_SPACE);
        r(PUSH_SPACE_CONCAT);
        r(TAB);
        r(PUSH_TAB);
        r(PUSH_TAB_CONCAT);
        r(MULTIPLY);
        r(DIVIDE);
        r(ADD);
        r(SUBTRACT);
        r(CONCAT);
        r(SPLIT);
        r(POW);
        r(MODULO);
        r(FLOOR);
        r(CEIL);
        r(ROUND);
        r(SQRT);
        r(SET_CURRENT_ARG);
        r(new AuxiliaryConstant());
        r(new AuxiliaryInstruction());
        r(FOR_LOOP);
        r(END);
        r(BREAK);
        r(ARRAY_CREATE);
        r(ARRAY_LOAD);
        r(ARRAY_STORE);
        r(ARRAY_CREATE_STACK);
        r(ARRAY_LOAD_STACK);
        r(ARRAY_STORE_STACK);
        r(ARRAY_LENGTH);
        r(PUSH_TRUTHY);
        r(PUSH_FALSY);
        r(NEGATE);
        r(IF_TRUTHY);
        r(IF_FALSY);
        r(COMPARE);
        r(EQUAL);
        r(EQUAL_ALL);
        r(NOT_EQUAL);
        r(NOT_EQUAL_ALL);
        r(GREATER);
        r(GREATER_ALL);
        r(LESS);
        r(LESS_ALL);
        r(GREATER_EQUAL);
        r(GREATER_EQUAL_ALL);
        r(LESS_EQUAL);
        r(LESS_EQUAL_ALL);
        r(IF_BLOCK);
        r(IF_ELSE_BLOCK);
        r(ELSE);
        r(SUPER_PUSH);
        r(RELATIVE_JUMP);
        r(INDEX_JUMP);
        r(TRIPLICATE);
        r(TRIPLICATE_ALL);
        r(POP_SPLIT_PUSH);
        r(QUINE);
        r(FUNCTION);


        InstructionRegistry.getBlockStarts().add("for");
        InstructionRegistry.getBlockEnds().add("end");

        InstructionRegistry.getBlockStarts().add("ifblock");
        InstructionRegistry.getBlockStarts().add("ifelse");

        InstructionRegistry.checkNumbers();
    }

    public static void ra(Instruction instruction) {
        InstructionRegistry.registerAuxiliaryInstruction(instruction);
    }

    public static void rc(ConstantInstruction instruction) {
        InstructionRegistry.registerConstant(instruction);
    }

    public static void rc(CastableValue value, String name) {
        InstructionRegistry.registerConstant(Instructions.constant(value, name));
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

    public static void checkNumbers() throws IllegalStateException {
        if(InstructionRegistry.instructions.size() > 230) {
            throw new IllegalStateException("More than 230 standard instructions are defined");
        } else if (InstructionRegistry.supplementaryInstructions.size() > 1024) {
            throw new IllegalStateException("More than 1024 supplementary instructions are defined");
        } else if (InstructionRegistry.constants.size() > 512) {
            throw new IllegalStateException("More than 512 standard constants are defined");
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
        TableFormat format = new TableFormat("ID", "Main Alias", "All Aliases", "Value", "Explanation", "Specification");
        for (int i = 0; i < InstructionRegistry.constants.size(); i++) {
            ConstantInstruction con = InstructionRegistry.constants.get(i);
            format.addRow(String.valueOf(i), con.getMainAlias(), PlasmaStringUtil.joinIntermediate(", ", con.getAliases().toArray()), Program.valueToString(con.getValue()),  con.getDescription(), con.getDocumentation());
        }
        return format;
    }

    public static TableFormat getAuxiliaryInstructionsDocumentation() {
        TableFormat format = new TableFormat("ID", "Main Alias", "All Aliases", "Explanation", "Specification");
        for (int i = 0; i < InstructionRegistry.auxiliaryInstructions.size(); i++) {
            Instruction aux = InstructionRegistry.auxiliaryInstructions.get(i);
            format.addRow(String.valueOf(i), aux.getMainAlias(), PlasmaStringUtil.joinIntermediate(", ", aux.getAliases().toArray()), aux.getDescription(), aux.getDocumentation());
        }
        return format;
    }

    public static TableFormat getAuxiliaryConstantsDocumentation() {
        TableFormat format = new TableFormat("ID", "Value");
        for (int i = 0; i < InstructionRegistry.auxiliaryConstants.size(); i++) {
            CastableValue aux = InstructionRegistry.auxiliaryConstants.get(i);
            format.addRow(String.valueOf(i), Program.valueToString(aux));
        }
        return format;
    }

    private static TableFormat of(List<Instruction> instructions) {
        TableFormat format = new TableFormat("ID", "Main Alias", "All Aliases", "Explanation", "Specification");
        instructions.forEach(i -> format.addRow(String.valueOf(i.getId()), i.getMainAlias(), PlasmaStringUtil.joinIntermediate(", ", i.getAliases().toArray()), i.getDescription(), i.getDocumentation()));
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

}
