#JAISBaL Examples

##Language Examples

####Hello World
Verbose:
```
#
printterm Hello World}
```
Character:
```
wHello World
```
####Print Hello World x times
Verbose:
```
#
n} \# Request input #\
for
  printterm Hello World}
end
```
Character:
```
ÈwHello World \# Implicitly collect input (enabled if no arguments specified in main method). No 'end' of for loop since end is end of program. No '}' for value since it is end of program #\
```

##Command Line Tool Examples
####Explaining
Arguments:
```
mode=input "content=ÈwHello World}" action=explain
```
Output:
```
# \# enable verbose parsing #\

for                          \# start for loop #\
    printterm Hello World}    \# print Hello World #\

```
####Executing
Arguments:
```
mode=input "content=ÈwHello World" action=exec
```
Output:
```
Run #1:
Enter a value > 3 /# I entered three, you could theoretically enter any number #/
Hello WorldHello WorldHelloWorld
----------------------
Stack: []
Locals: {}
----------------------------------------
JAISBaL bytes: 14
UTF-8 bytes: 15
```
####Minfiying
Arguments:
```
mode=input "content=#\nfor\nprintterm Hello World" action=minify
```
Output:
```
ÈwHello World
```
