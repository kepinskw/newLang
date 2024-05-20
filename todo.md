# TODO

* Update grammar:
	* Expressions
	* DIV
	* SUB

* Update LLVMActions:
	* class Value: consider adding length of variale
	* exitDiv
	* exitSub
	* exitLogical
	* exitString
	* exitArray
	* Float32, Float64

* Works:
  a = "hello"
  b = "abc"
  c = a + b
  print c -> helloabc

* Doesnt work:
  c = "hello" + "abc"
  print c -> ptr error

* For tests:
  * INT / REAL /FLOAT
    * print
    * read
    * assign
    * aritmetics
    * toint, tofloat, toreal
  * ARRAY INT/REAL
    * print 
    * insert 
    * select
    * assign undeclared
    * assign declared
  * MATRIX INT/REAL
    * print number
    * print array
    * print matrix
	* insert from number
    * insert from array
	* select to number
    * select to array
	* assign undeclared
	* assign declared
  * BOOL
    * XOR
    * NOR
    * AND
    * OR
    * print
    * read
    * brackets
  * STRING
    * read
    * print 
    * assign
    * add STRING/INT
    * 
