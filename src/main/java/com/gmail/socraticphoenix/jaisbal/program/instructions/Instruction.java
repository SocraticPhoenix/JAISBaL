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

import com.gmail.socraticphoenix.jaisbal.app.util.DangerousFunction;
import com.gmail.socraticphoenix.jaisbal.program.State;
import com.gmail.socraticphoenix.jaisbal.program.function.FunctionContext;
import com.gmail.socraticphoenix.plasma.string.CharacterStream;

import java.util.ArrayList;
import java.util.List;

public class Instruction {
    public static final Instruction DUMMY = new Instruction(f -> State.NORMAL, -1, "", "", "");
    private int dangerLevel;
    private DangerousFunction<FunctionContext, State> action;
    private DangerousFunction<CharacterStream, String> valueReader;
    private String documentation;
    private String description;
    private List<String> aliases;
    private char id;
    private double group;

    public Instruction(DangerousFunction<FunctionContext, State> action, DangerousFunction<CharacterStream, String> valueReader, double group, int dangerLevel, String explanation, String documentation, String... aliases) {
        this.group = group;
        this.action = f -> {
            InstructionRegistry.getMonitor().monitor(this);
            return action.apply(f);
        };
        this.valueReader = valueReader;
        this.aliases = new ArrayList<>();
        this.description = explanation;
        this.documentation = documentation;
        this.id = '\0';
        this.aliases = new ArrayList<>();
        this.dangerLevel = dangerLevel;
        for (String s : aliases) {
            this.aliases.add(s);
            if (s.length() == 1 && this.id == '\0') {
                this.id = s.charAt(0);
            }
        }
    }

    public Instruction(DangerousFunction<FunctionContext, State> action, DangerousFunction<CharacterStream, String> valueReader, double group, String description, String documentation, String... aliases) {
        this(action, valueReader, group, 0, description, documentation, aliases);
    }

    public Instruction(DangerousFunction<FunctionContext, State> action, double group, int dangerLevel, String description, String documentation, String... aliases) {
        this(action, c -> null, group, dangerLevel, description, documentation, aliases);
    }

    public Instruction(DangerousFunction<FunctionContext, State> action, double group, String description, String documentation, String... aliases) {
        this(action, c -> null, group, 0, description, documentation, aliases);
    }

    public int getDangerLevel() {
        return this.dangerLevel;
    }

    public double getGroup() {
        return this.group;
    }

    public String getDocumentation() {
        return this.documentation;
    }

    public String getMainAlias() {
        return this.aliases.stream().sorted((a, b) -> Integer.compare(b.length(), a.length())).findFirst().get();
    }

    public char getId() {
        return this.id;
    }

    public void assignId(char c) {
        this.id = c;
        this.aliases.add(String.valueOf(c));
    }

    public String getDescription() {
        return this.description;
    }

    public List<String> getAliases() {
        return this.aliases;
    }

    public DangerousFunction<FunctionContext, State> getAction() {
        return this.action;
    }

    public DangerousFunction<CharacterStream, String> getValueReader() {
        return this.valueReader;
    }

    public boolean isName(String instruction) {
        return this.aliases.contains(instruction);
    }
}
