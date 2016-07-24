# JAISBaL

**DISCLAIMER:** JAISBaL is currently under development! Character pages and instruction ids may change without notice!

JAISBaL is Just Another Interpreted Stack-Based Language, created by me. JAISBaL is meant to be a golfing language for dummies and fanatics alike, and features a verbose and character parsing mode, as well as a minifier and explainer to translate between the two. The JAISBaL interpreter is written in Java, and this repository contains its source code. The JAISBaL grammar specification is fully developed, though not all instructions are defined.

##This Repository
I open to all contributions! Please open a pull request or issue. However, if you decide to add an instruction or constant, please do the following:

- Specify whether your new instruction is standard, supplementary, constant, auxiliary, or auxiliary constant
- Explain why you believe the instruction best fits the given category
- If it is a pull request, properly define the instruction in the [Instructions](https://github.com/SocraticPhoenix/JAISBaL/blob/master/src/main/java/com/gmail/socraticphoenix/jaisbal/program/instructions/Instructions.java) class and properly register it in the [InstructionRegistry](https://github.com/SocraticPhoenix/JAISBaL/blob/master/src/main/java/com/gmail/socraticphoenix/jaisbal/program/instructions/InstructionRegistry.java) class.
- If it is an issue/comment, either define the full instruction behavior, or provide a code snippet defining an [Instruction](https://github.com/SocraticPhoenix/JAISBaL/blob/master/src/main/java/com/gmail/socraticphoenix/jaisbal/program/instructions/Instruction.java) or [DangerousConsumer[FunctionContext]](https://github.com/SocraticPhoenix/JAISBaL/blob/master/src/main/java/com/gmail/socraticphoenix/jaisbal/util/DangerousConsumer.java)

##The JAISBaL Command Line Tool
Arguments for the JAISBaL command line tool are entered in key-value pairs, such as "mode=file" or "action=exec." See the table below for arguments.

| Argument | Acceptable Values | Description | Optional? |
| -------- | ----------- | ---------- | ---------- |
|mode |file, input, gui | defines the input mode for the tool | true (input assumed)|
|content |JAISBaL program | defines the input for mode=input or mode=gui| false (when mode=input)|
|file |Existing File |defines the input for mode=file or mode=gui| false (when mode=file)|
|encoding| utf8, jaisbal| defines the character encoding for file=...| true (jaisbal assumed) |
|target-encoding | utf8, jaisbal | defines the target character encoding for mode=file | true (jaisbal assumed) |
|exec-number| integer | defines the number of times to run the program for action=exec| true (1 assumed) |
|exec-time| integer | defines the maximum runtime, in milliseconds, of the program for action=exec| true (infinite assumed |
|input[x] | A common separated list of input| defines the input for the program for action=exec. There can be up to [exec-number] of these arguments, each one specifying the arguments for its given run. Indices start at 0, so input0 defines input for the first run | true |
|action | exec, minify, explain, encode| defines the action the tool should take| false |
###Actions
- exec : executes the program exec-number times, and exits after exec-time milliseconds have passed, or execution is finished
- minify : minifies the given program, and either outputs it to the screen (mode=input) or writes it to the file (mode=file)
- explain : expands and provides a step-by-step explanation for the given program, and either outputs it to the screen (mode=input) or writes it to the file (mode=file)
- encode : reads input as encoding, and writes it to target-encoding. Outputs the result to the screen (mode=input), or writes it to the file and outputs it to the screen (mode=file)

##The JAISBaL GUI
The JAISBaL GUI provides most of the functionality of the command line tool in a nice little window. It can be opened by either specifying no arguments when opening the command line tool (i.e. by double clicking the .jar), or by specifying mode=gui. The GUI will ignore any action=, input=, or input-file= arguments, but will load input from either content= or file=, and will take note of encoding= and target-encoding= arguments, using to open a file= and set the default save encoding. In the GUI, the top text area is the program, the middle is the output, and the small bottom one is the input. Pressing "enter" in the input box will send it's current content to the input queue. I'm no GUI wiz, so at the moment file names must be typed out in the file box at the bottom of the window if you want to save or load.

###Download
The JAISBaL command line tool has its entry point in the main [JAISBaL](https://github.com/SocraticPhoenix/JAISBaL/blob/master/src/main/java/com/gmail/socraticphoenix/jaisbal/JAISBaL.java) file, and a compiled version can be found published [here](https://dl.bintray.com/meguy26/Main/com/gmail/socraticphoenix/JAISBaL/)

##Examples
Examples for the language and the command line tool can be found in the [Examples](https://github.com/SocraticPhoenix/JAISBaL/blob/master/EXAMPLES.md) document

##File Format
JAISBaL files have the extension .isbl (interpreted stack based language), and are either in UTF-8 or the JAISBaL character encodings, with JAISBaL being preferred. JAISBaL defines its own character encodings to maximize byte usage, and JAISBaL provides a [java Charset class] (https://github.com/SocraticPhoenix/JAISBaL/blob/master/src/main/java/com/gmail/socraticphoenix/jaisbal/encode/JAISBaLCharset.java) for its encoding.

###JAISBaL Character Encoding Scheme
In this section, the term "character page" means a list of one-to-one character-to-byte mappings. The JAISBaL character set consists of 14 character pages, Page S and pages A-M. Page S is the standard page, and encompasses white space, syntax characters, and all standard JAISBaL instructions. Pages A-K are the supplementary instruction pages, and pages L-M are the constant pages. Pages S maps 241 characters (0 - 240), and the remaining 14 bytes are page bytes. 244 refers to Page A, 245 to page B, 246 to page C, etc. Any character in Page S is encoded as a single byte, as defined by Page S. Any character that is in pages A-M is encoded as two bytes, the first byte is the page byte, and the second byte is the byte mapped to the character in the given page. The character pages can be [found here](https://github.com/SocraticPhoenix/JAISBaL/tree/master/src/main/resources). Each pages is named, and holds its bytes in order, encoded in UTF-8. The first character in each page is mapped to 0, the second to 1, and so on. Note that Page-S contains some white space characters that may not be apparently visible, including \r, \n, \t and a space.

The JAISBaL Character Pages are fairly stable, but they may still be changed in the future.

###JAISBaL Instruction Mappings
JAISBaL syntax characters makeup the first 40 codes of Page S. Syntax characters are things that define language behavior, such as number literals, or the value terminator character "}." The remaining 200 codes in Page S are reserved for JAISBaL standard instructions, taking up one byte per character. The 2816 codes in pages A-K are supplementary instructions, taking up two bytes per character. The 512 codes in pages L-M are constant instructions, taking up two bytes per character. A constant instruction simply pushes a constant value onto the stack, such as Pi. Finally, the standard instructions F and C provide links to auxiliary instructions and auxiliary constants respectively. There can be a total of 2^31 - 1 auxiliary instructions or constants, however having so many would likely require too much memory. Auxiliary instructions/constants are defined by an index, and vary in byte length depending on the length of said index.

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
If the parser is set to verbose, instructions will be assumed to be on separate lines, and **arguments will not be defined by instructions**, instead the argument for an instruction is whatever value is separated by a space, and directly after the instruction. For example:
```
#
printterm hello world}
printterm hello world
print1 hello world}
```
are all equivalent.
###Character Parsing
If the parser is set to character, each character will be considered an instruction. This is were the difference between printterm and print1 occurs. In character mode, the parser reads the file as a single stream of charaters, instead of splitting it up into lines. Therefore, an instruction must read it's value out of the stream. Print1 reads the next character and prints it, print2 reads the next two characters and prints them, and printterm reads up to the next unescaped "}." This is, of course, significant, because the verbose program:
```
#
print1 hello world
```
will behave completely differently from the character program:
```
hhello world /# assuming h is the character id for print1 #/
```

###Value Parsing
JAISBaL supports strings, numbers, and arrays. Anything not interpretable as a number or an array becomes a string, and numbers can be converted to strings for instructions or functions that only accept numbers. A number is a valid java [BigDecimal](https://docs.oracle.com/javase/7/docs/api/java/math/BigDecimal.html), and can optionally start with a '-', continue with an arbitrary number of digits, and have an optional decimal part. The "pushnum" instruction will read until a number is terminated, and push that number. A string consists of any sequence of digits, including escaped characters such as `\n`, `\t`, `\}`,`\,`, `\[`, and `\]`. The "pushterm" instruction pushes a value up to the '}' terminating character. Finally, there are arrays. So far, we've only discussed unboxed values, a boxed JAISbaL value is of the form `[<value>]` and they can be nested. An array is simply a list of boxed values, so the an array of the characters in "array" would look like this: `[a][r][r][a][y]`. Furthermore, these boxes can be nested to create array-arrays, like so: `[[a][r][r]][[r][a][y]]`. This is an array of the array "arr" and the array "ray".

##The Environment
###Function Contexts
Every function in JAISBaL has it's own context. Function contexts each have their own stack, parent stack, and locals. The main function has an empty parent stack. Functions can specify arguments, and these arguments are loaded from the function's parent stack (the stack of it's calling function), into the locals. Loops and if-statements are run by changing the current instruction index of a function context, basically they use goto's. Due to the way running instructions are implemented, as of right now, 'break' will break out of either the current function context, the current loop, or an if-statement, but I'm working to fix this. They should only break out of function contexts and loops - not if-statements. 

###Design
JAISBaL is heavily inspired by the JVM, and has both a stack and variable register. Variables are registered at 64-bit indices, and can be stored and loaded on a whim. JAISBaL is technically statically typed, but most instructions attempt to define behavior for all possible operands. Moreover, numbers are represented as arbitrary precision [BigDecimals](https://docs.oracle.com/javase/7/docs/api/java/math/BigDecimal.html), and can be converted to and from strings.

##Instruction Reference
The full instruction reference is available [here](https://github.com/SocraticPhoenix/JAISBaL/blob/master/INSTRUCTIONS.md)
