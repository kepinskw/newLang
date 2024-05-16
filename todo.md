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
