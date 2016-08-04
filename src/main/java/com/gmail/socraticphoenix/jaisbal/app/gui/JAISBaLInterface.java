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
package com.gmail.socraticphoenix.jaisbal.app.gui;

import com.gmail.socraticphoenix.jaisbal.JAISBaL;
import com.gmail.socraticphoenix.jaisbal.app.util.JAISBaLExecutionException;
import com.gmail.socraticphoenix.jaisbal.app.util.LoopedSupplier;
import com.gmail.socraticphoenix.jaisbal.app.util.Terminable;
import com.gmail.socraticphoenix.jaisbal.encode.JAISBaLCharset;
import com.gmail.socraticphoenix.jaisbal.program.Program;
import com.gmail.socraticphoenix.jaisbal.program.SecurityMonitor;
import com.gmail.socraticphoenix.jaisbal.program.function.FunctionContext;
import com.gmail.socraticphoenix.jaisbal.program.instructions.InstructionRegistry;
import com.gmail.socraticphoenix.plasma.file.ByteBuilder;
import com.gmail.socraticphoenix.plasma.file.stream.TextScreenOutputStream;
import com.gmail.socraticphoenix.plasma.string.PlasmaStringUtil;
import com.gmail.socraticphoenix.plasma.string.StringParseException;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class JAISBaLInterface {
    private LoopedSupplier supplier;
    private TextScreenOutputStream outputStream;
    private AtomicBoolean running;

    private UndoManager undo;

    private JTextArea program;
    private JPanel panel1;
    private JTextArea output;
    private JTextField input;
    private JButton runButton;
    private JButton minifyButton;
    private JButton explainButton;
    private JButton saveButton;
    private JTextField file;
    private JComboBox<String> encoding;
    private JButton clearOutputButton;
    private JButton openButton;
    private JButton forceStopButton;
    private JButton byteInfoButton;
    private JButton parseButton;
    private JSlider levelSlider;
    private JLabel level;
    private Font font;

    private JFrame frame;
    private String content;
    private Map<String, String> args;

    public JAISBaLInterface(String content, Map<String, String> args) {
        this.args = args;
        this.content = content;
        this.running = new AtomicBoolean(false);
        this.undo = new UndoManager();
        this.undo.setLimit(1_000_000);
        $$$setupUI$$$();
        this.font = new Font("Monospaced.plain", Font.PLAIN, 14);
    }

    private void createUIComponents() {
        this.encoding = new JComboBox<>();
        this.encoding.addItem(JAISBaLCharset.getCharset().name());
        this.encoding.addItem(StandardCharsets.UTF_8.name());

    }

    public JFrame make() {
        if (this.frame == null) {
            this.input.setFont(this.font);
            this.program.setFont(this.font);
            this.output.setFont(this.font);

            for (int i = 0; i < this.encoding.getItemCount(); i++) {
                if (this.encoding.getItemAt(i).equals(this.args.get("target-encoding"))) {
                    this.encoding.setSelectedIndex(i);
                    break;
                }
            }

            if (this.args.containsKey("file")) {
                this.file.setText(this.args.get("file"));
            }

            this.program.setText(this.content);

            this.frame = new JFrame("JAISBaL GUI");
            this.frame.setSize(1200, 800);
            this.frame.add(this.panel1);
            this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.outputStream = new TextScreenOutputStream(JAISBaL.getOut(), this.output);
            JAISBaL.setOut(new PrintStream(this.outputStream, true));
            this.supplier = new LoopedSupplier();
            JAISBaL.setIn(this.supplier);

            this.level.setText(String.valueOf(InstructionRegistry.getMonitor().getLevel()));
            this.levelSlider.setMaximum(Math.max(100, InstructionRegistry.getMonitor().getLevel()));
            this.levelSlider.setValue(InstructionRegistry.getMonitor().getLevel());
            this.levelSlider.addChangeListener(e -> {
                InstructionRegistry.setMonitor(new SecurityMonitor(this.levelSlider.getValue()));
                this.levelSlider.setMaximum(Math.max(100, InstructionRegistry.getMonitor().getLevel()));
                this.level.setText(String.valueOf(InstructionRegistry.getMonitor().getLevel()));
            });
            this.input.addKeyListener((KeyReleasedListener) e -> {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    this.input();
                }
            });
            this.program.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    char c = e.getKeyChar();
                    if (c == '\t') {
                        e.consume();
                        program.insert(PlasmaStringUtil.indent(4, " "), program.getCaretPosition());
                    }
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    char c = e.getKeyChar();
                    if (c == '\t') {
                        e.consume();
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    char c = e.getKeyChar();
                    if (c == '\t') {
                        e.consume();
                    }
                }
            });
            this.openButton.addActionListener(e -> {
                String f = this.file.getText();
                File file = new File(f);
                String s = this.encoding.getItemAt(this.encoding.getSelectedIndex());
                if (s == null) {
                    JAISBaL.getOut().println("Encoding not specified");
                } else {
                    Charset encoding = JAISBaLCharset.get(s);
                    try {
                        FileInputStream stream = new FileInputStream(file);
                        ByteBuilder builder = new ByteBuilder();
                        int i;
                        while ((i = stream.read()) > -1) {
                            builder.append((byte) i);
                        }
                        this.program.setText(new String(builder.toBytes(), encoding));
                    } catch (IOException e1) {
                        JAISBaL.getOut().println("Loading failed");
                        JAISBaL.getOut().println(e1.getClass().getName() + ": " + e1.getMessage());
                        Throwable cause = e1;
                        while ((cause = cause.getCause()) != null) {
                            JAISBaL.getOut().println("Caused by: " + cause.getClass().getName() + ": " + cause.getMessage());
                        }
                    }
                }

            });
            this.clearOutputButton.addActionListener(e -> this.output.setText(""));
            AtomicReference<FunctionContext> reference = new AtomicReference<>();
            this.runButton.addActionListener(e -> {
                String s = this.program.getText();
                if (!this.running.get()) {
                    this.running.set(true);
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                supplier.clear();
                                supplier.restart();
                                Program program = Program.parse(s);
                                FunctionContext context = program.getMain().createContext();
                                reference.set(context);
                                context.runAsMain();
                            } catch (JAISBaLExecutionException | IOException | StringParseException e1) {
                                JAISBaL.getOut().println("Execution failed");
                                JAISBaL.getOut().println(e1.getClass().getName() + ": " + e1.getMessage());
                                Throwable cause = e1;
                                while ((cause = cause.getCause()) != null) {
                                    JAISBaL.getOut().println("Caused by: " + cause.getClass().getName() + ": " + cause.getMessage());
                                }
                            }
                            running.set(false);
                        }

                    }.start();
                } else {
                    JAISBaL.getOut().println("A program is already running!");
                }
            });
            this.byteInfoButton.addActionListener(e -> {
                String prog = this.program.getText();
                byte[] a = this.encode(prog, JAISBaLCharset.getCharset());
                JAISBaL.getOut().println("JAISBaL Bytes: " + a.length);
                byte[] b = this.encode(prog, StandardCharsets.UTF_8);
                JAISBaL.getOut().println("UTF-8 Bytes: " + b.length);
            });
            this.forceStopButton.addActionListener(e -> {
                if (reference.get() != null) {
                    reference.get().terminate();
                    if (JAISBaL.getIn() instanceof Terminable) {
                        ((Terminable) JAISBaL.getIn()).terminate();
                    }
                }
            });
            this.minifyButton.addActionListener(e -> {
                String s = this.program.getText();
                try {
                    Program program = Program.parse(s);
                    String m = program.minify();
                    this.program.setText(m);
                } catch (JAISBaLExecutionException | StringParseException e1) {
                    JAISBaL.getOut().println("Parsing failed");
                    JAISBaL.getOut().println(e1.getClass().getName() + ": " + e1.getMessage());
                    Throwable cause = e1;
                    while ((cause = cause.getCause()) != null) {
                        JAISBaL.getOut().println("Caused by: " + cause.getClass().getName() + ": " + cause.getMessage());
                    }
                }
            });
            this.explainButton.addActionListener(e -> {
                String s = this.program.getText();
                try {
                    Program program = Program.parse(s);
                    String m = program.explain();
                    this.program.setText(m);
                } catch (JAISBaLExecutionException | StringParseException e1) {
                    JAISBaL.getOut().println("Parsing failed");
                    JAISBaL.getOut().println(e1.getClass().getName() + ": " + e1.getMessage());
                    Throwable cause = e1;
                    while ((cause = cause.getCause()) != null) {
                        JAISBaL.getOut().println("Caused by: " + cause.getClass().getName() + ": " + cause.getMessage());
                    }
                }
            });
            this.saveButton.addActionListener(e -> {
                String f = this.file.getText();
                File file = new File(f);
                String s = this.encoding.getItemAt(this.encoding.getSelectedIndex());
                String prog = this.program.getText();
                if (s == null) {
                    JAISBaL.getOut().println("Encoding not specified");
                } else {
                    Charset encoding = JAISBaLCharset.get(s);
                    if (encoding.newEncoder().canEncode(prog)) {
                        try {
                            FileOutputStream stream = new FileOutputStream(file);
                            byte[] pieces = prog.getBytes(encoding);
                            stream.write(pieces);
                            stream.close();
                            JAISBaL.getOut().println("Successfully saved file");
                        } catch (IOException e1) {
                            JAISBaL.getOut().println("Saving failed");
                            JAISBaL.getOut().println(e1.getClass().getName() + ": " + e1.getMessage());
                            Throwable cause = e1;
                            while ((cause = cause.getCause()) != null) {
                                JAISBaL.getOut().println("Caused by: " + cause.getClass().getName() + ": " + cause.getMessage());
                            }
                        }
                    } else {
                        CharsetEncoder encoder = encoding.newEncoder();
                        char bad = '\0';
                        for (char c : prog.toCharArray()) {
                            if (!encoder.canEncode(c)) {
                                bad = c;
                                break;
                            }
                        }
                        JAISBaL.getOut().println("Encoding " + encoding.name() + " cannot encode '" + PlasmaStringUtil.escape(String.valueOf(bad)) + "'");
                    }
                }
            });
            this.parseButton.addActionListener(e -> {
                try {
                    Program.parse(this.program.getText());
                    JAISBaL.getOut().println("Successfully parsed program");
                } catch (JAISBaLExecutionException | StringParseException e1) {
                    JAISBaL.getOut().println("Parsing failed");
                    JAISBaL.getOut().println(e1.getClass().getName() + ": " + e1.getMessage());
                    Throwable cause = e1;
                    while ((cause = cause.getCause()) != null) {
                        JAISBaL.getOut().println("Caused by: " + cause.getClass().getName() + ": " + cause.getMessage());
                    }
                }
            });
            this.program.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undo");
            this.program.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "redo");
            this.program.getActionMap().put("undo", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        undo.undo();
                        while (program.getText().equals("")) {
                            undo.undo();
                        }
                    } catch (CannotUndoException ignore) {

                    }
                }
            });
            this.program.getActionMap().put("redo", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        undo.redo();
                        while (program.getText().equals("")) {
                            undo.redo();
                        }
                    } catch (CannotRedoException ignore) {

                    }
                }
            });
        }
        this.program.getDocument().addUndoableEditListener(this.undo);

        return this.frame;
    }

    private byte[] encode(String prog, Charset charset) {
        if (charset.newEncoder().canEncode(prog)) {
            return prog.getBytes(charset);
        } else {
            CharsetEncoder encoder = charset.newEncoder();
            char bad = '\0';
            for (char c : prog.toCharArray()) {
                if (!encoder.canEncode(c)) {
                    bad = c;
                    break;
                }
            }
            JAISBaL.getOut().println("Encoding " + charset.name() + " cannot encode '" + PlasmaStringUtil.escape(String.valueOf(bad)) + "'");
            return new byte[0];
        }
    }

    private void input() {
        String s = this.input.getText();
        this.input.setText("");
        this.output.append(s + System.lineSeparator());
        this.supplier.add(s);
    }


    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(16, 4, new Insets(0, 0, 0, 0), -1, -1));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(2, 1, 2, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        program = new JTextArea();
        scrollPane1.setViewportView(program);
        input = new JTextField();
        panel1.add(input, new GridConstraints(7, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel1.add(scrollPane2, new GridConstraints(5, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        output = new JTextArea();
        output.setEditable(true);
        output.setEnabled(true);
        scrollPane2.setViewportView(output);
        final JLabel label1 = new JLabel();
        label1.setText("");
        panel1.add(label1, new GridConstraints(15, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("");
        panel1.add(label2, new GridConstraints(2, 3, 4, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("");
        panel1.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("");
        panel1.add(label4, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(9, 1, 6, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-16777216)), null));
        minifyButton = new JButton();
        minifyButton.setText("Minify");
        panel3.add(minifyButton, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        explainButton = new JButton();
        explainButton.setText("Explain");
        panel3.add(explainButton, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        parseButton = new JButton();
        parseButton.setText("Parse");
        panel3.add(parseButton, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        levelSlider = new JSlider();
        levelSlider.setPaintLabels(false);
        levelSlider.setPaintTicks(false);
        levelSlider.setSnapToTicks(true);
        panel3.add(levelSlider, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        level = new JLabel();
        level.setText("100");
        panel3.add(level, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Security Level:");
        panel3.add(label5, new GridConstraints(3, 0, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(8, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel4, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-16777216)), null));
        clearOutputButton = new JButton();
        clearOutputButton.setText("Clear Output");
        panel4.add(clearOutputButton, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        runButton = new JButton();
        runButton.setText("Run");
        panel4.add(runButton, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        forceStopButton = new JButton();
        forceStopButton.setText("Force Stop");
        panel4.add(forceStopButton, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        openButton = new JButton();
        openButton.setText("Open");
        panel4.add(openButton, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveButton = new JButton();
        saveButton.setText("Save");
        panel4.add(saveButton, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        byteInfoButton = new JButton();
        byteInfoButton.setText("Byte Info");
        panel4.add(byteInfoButton, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel4.add(encoding, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        file = new JTextField();
        panel4.add(file, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("File:");
        panel4.add(label6, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Encoding:");
        panel4.add(label7, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Program:");
        panel1.add(label8, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Output:");
        panel1.add(label9, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Input:");
        panel1.add(label10, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("Actions:");
        panel1.add(label11, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }
}
