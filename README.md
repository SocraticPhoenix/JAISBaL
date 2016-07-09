# JAISBaL
(pronounced jace-ball)

JAISBaL is Just Another Intepreted Stack-Based Language, created by me. JAISBaL is meant to be a golfing language for dummies and fanatics alike, and features a verbose and character parsing modes, as well as a minifier and explainer function. The JAISBaL intepreter is written in Java, and this repository contains its source code. The JAISBaL grammar specification is fully developed, though not all instructions are defined.

##This Repository
I open to all contributions! Please open a pull request or issue. However, if you decide to add an instruction, please do the following:

- Specify whether your new instruction is standard, supplementary, constant, auxiliary, or auxiliary constant
- Explain why you believe the instruction best fits the given category
- If it is a pull request, properly define the instruction in the [Instructions](https://github.com/SocraticPhoenix/JAISBaL/blob/master/src/main/java/com/gmail/socraticphoenix/jaisbal/program/instructions/Instructions.java) class and properly register it in the [InstructionRegistry](https://github.com/SocraticPhoenix/JAISBaL/blob/master/src/main/java/com/gmail/socraticphoenix/jaisbal/program/instructions/InstructionRegistry.java) class.
- If it is an issue/comment, either define the full instruction behavior, or provide a code snippet defining an [Instruction](https://github.com/SocraticPhoenix/JAISBaL/blob/master/src/main/java/com/gmail/socraticphoenix/jaisbal/program/instructions/Instruction.java) or [DangerousConsumer<FunctionContext>](https://github.com/SocraticPhoenix/JAISBaL/blob/master/src/main/java/com/gmail/socraticphoenix/jaisbal/util/DangerousConsumer.java)

##The JAISBaL Command Line Tool
A JAISBaL GUI is planned, but currently not implemented.
Arguments for the JAISBaL command line tool are entered in key-value pairs, such as "mode=file" or "action=exec." See the table below for arguments.

| Argument | Acceptable Values | Description | Optional? |
| -------- | ----------- | ---------- | ---------- |
|mode |file, input, gui | defines the input mode for the tool | true (input assumed)|
|content |JAISBaL program | defines the input for mode=input or mode=gui| false (when mode=input)|
|file |Existing File |defines the input for mode=file or mode=gui| false (when mode=file)|
|encoding| utf8, jaisbal| defines the character encoding for file=...| true (jaisbal assumed) |
|target-encoding | utf8, jaisbal | defines the target character encoding for mode=file | true (jaisbal assumed) |
|exec-number| integer | defines the number of times to run the program for action=exec| true (1 assumed) |
|exec-time| iteger | defines the maximum runtime, in milliseconds, of the program for action=exec| true (infinite assumed |
|input | [JLSCArray](https://gist.github.com/SocraticPhoenix/ea6386fb7a610bdbb8bd) | defines the input for the program for action=exec | true |
|input-file | [JLSCCompound](https://gist.github.com/SocraticPhoenix/ea6386fb7a610bdbb8bd) with input array at key "input" | defines the input for the program for action=exec | true |
|action | exec, minify, explain, encode| defines the action the tool should take| false |
###Actions
- exec : executes the program exec-number times, and exits after exec-time milliseconds have passed, or execution is finished
- minify : minifies the given program, and either outputs it to the screen (mode=input) or writes it to the file (mode=file)
- explain : expands and provides a step-by-step explaination for the given program, and either outputs it to the screen (mode=input) or writes it to the file (mode=file)
- encode : reads input as encoding, and writes it to target-encoding. Outputs the result to the screen (mode=input), or writes it to the file and outputs it to the screen (mode=file)

###Download
The JAISBaL command line tool has its entry point in the main [JAISBaL](https://github.com/SocraticPhoenix/JAISBaL/blob/master/src/main/java/com/gmail/socraticphoenix/jaisbal/JAISBaL.java) file, and a compiled version can be found published [here](https://dl.bintray.com/meguy26/Main/com/gmail/socraticphoenix/JAISBaL/)


##Examples
Examples for the language and the command line tool can be found in the [Examples](https://github.com/SocraticPhoenix/JAISBaL/blob/master/EXAMPLES.md) document

##File Format
JAISBaL files have the extention .isbl (interpreted stack based language), and are either in UTF-8 or the JAISBaL character encodings. JAISBaL defines its own character encodings to maximize byte usage, and JAISBaL provides a [java Charset class] (https://github.com/SocraticPhoenix/JAISBaL/blob/master/src/main/java/com/gmail/socraticphoenix/jaisbal/encode/JAISBaLCharset.java) for its encoding.

###JAISBaL Character Encoding Scheme
In this section, the term "character page" means a list of one-to-one character-to-byte mappings. The JAISBaL character set consists of 7 character pages, Page S and pages A-F. Page S is the standard page, and encompasses white space, syntax characters, and all standard JAISBaL instructions. Pages A-D are the suplementary instruction pages, and pages E-F are the constant pages. Pages S maps 250 characters (0 - 249), and the remaining 6 bytes are page bytes. 250 refers to Page A, 251 to page B, 252 to page C, etc. Any character in Page S is encoded as a single byte, as defined by Page S. Any character that is in pages A-F is encoded as two bytes, the first byte being the character's page byte, and the second byte being the byte defined in the appropriate page. The character pages can be [found here](https://github.com/SocraticPhoenix/JAISBaL/tree/master/src/main/resources). Each pages is named, and holds its bytes in order, encoded in UTF-8. The first character in each page is mapped to 0, the second to 1, and so on. Note that Page-S contains some white space characters that may not be apparently visible, including \r, \n, \t and a space.

Disclaimer: The JAISBaL character pages are **in no way** finalized, and as of right now were computer generated. They will likely change to support more commonly used characters in the future

###JAISBaL Instruction Mappings
JAISBaL syntax characters makeup the first 40 codes of Page S. Syntax characters are things that define language behavior, such as number literals, or the value terminator character "}." The remaining 210 codes in Page S are reserved for JAISBaL standard instructions, taking up one byte per character. The 1024 codes in pages A-D are supplementary instructions, taking up two bytes per character. The 512 codes in Pages E-F are constant instructions, taking up two bytes per character. A constant instruction simply pushes a constant value onto the stack, such as Pi. Finally, the standard instructions F and C provide links to auxiliary instructions and auxiliary constants respectivley. There can be a total of 2^31 - 1 auxiliary instructions or constants, however having so many would likely require too much memory. Auxiliary instructions/constants are defined by an index, and vary in byte length depending on the length of said index.

##Syntax
Every JAISBaL file must define its parsing type. If the first character of a file is '#' then verbose parsing is used, otherwise, character parsing is used.
###Basic Syntax
JAISBaL files have the general format
```
# /# Optional verbose specifier #/
(
  /# Function block #/
)
/# Main function #/
```
and comments are enclosed between `\#` and `#\`
###Verbose Parsing
If the parser is set to verbose, instructions will be assumed to be on seperate lines, and **arguments will not be defined by instructions**, instead the argument for an instruction is whatever value is seperated by a space, and directly after the instruction. For example:
```
#
printterm hello world}
printterm hello world
print1 hello world}
```
are all equivilent.
###Character Parsing
If the parser is set to character, each character will be considered an instruction. This is were the difference between printterm and print1 occurs. In character mode, the parser reads the file as a single stream of charaters, instead of splitting it up into lines. Therefore, an instruction must read it's value out of the stream. Print1 reads the next character and prints it, print2 reads the next two characters and prints them, and printterm reads up to the next unescaped "}." This is, of course, signfigant, because the verbose program:
```
#
print1 hello world
```
will behave completley differently from the character program:
```
hhello world /# h is the character id for print1 #/
```
##The Enviroment
JAISBaL is heavily inspired by the JVM, and has both a stack and variable register. Variables are registered at 64-bit indices, and can be stored and loaded on a whim. JAISBaL is technically statically typed, but most instructions attempt to define behavior for all possible operands. Moreover, numbers are represeted as arbitrary precision [BigDecimals](https://docs.oracle.com/javase/7/docs/api/java/math/BigDecimal.html), and can be converted to and from strings. Finally, JAISBaL handles control flow with function frames. Blocks and functions both create their own function frame, however a block's function frame shares its stack with the parent function frame, whereas functions do not. This has the unfortunate consequence of 'break' affecting if statements as well, but that's a feature, not a bug ;)

##Instruction Reference
The full instruction reference is available [here](https://github.com/SocraticPhoenix/JAISBaL/blob/master/INSTRUCTIONS.md)
