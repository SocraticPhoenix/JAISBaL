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
§wHello World \# Implicitly collect input (enabled if no arguments specified in main method). No 'end' of for loop since end is end of program. No '}' for value since it is end of program #\
```
####Sort and Print Array
Verbose
```
#
(
sort_print:
a?
loadall     \# load input (non-main function input is stored in local variables) #\
sort        \# sort the array #\
popoutallln \# pop and output #\
)
a?  \# request input of type wildcard array  #\
call sort_print \# call function  #\
```
Example Run:
```
Run #1:
Enter a value[] > [a][b][1][45][l][c][4321][-3][0][[g][5][hello]][[world][4][[-43][3][53]]]}
[-3, 0, 1, 45, 4321, a, b, c, l, [g, 5, hello], [world, 4, [-43, 3, 53]]]

--------------------
Stack: []
Locals: {0:[-3, 0, 1, 45, 4321, a, b, c, l, [g, 5, hello], [world, 4, [-43, 3, 53]]]}
------------------------------------------------------------

JAISBaL bytes: 80
UTF-8 bytes: 80
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
mode=input "content=§wHello World" action=exec
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
§wHello World
```
