ANTLR=/usr/local/lib/antlr-4.13.1-complete.jar

all: generate compile test

generate:
	java -jar $(ANTLR) -o output gram.g4

compile:
	javac -cp $(ANTLR):output:. Main.java
	javac -cp .:output:$(ANTLR) output/*.java

test_numbers:
	java -cp $(ANTLR):output:. Main numbers.txt > input.ll
	lli input.ll

test_matrix:
	java -cp $(ANTLR):output:. Main matrix.txt > input.ll
	lli input.ll

test_arrays:
	java -cp $(ANTLR):output:. Main array.txt > input.ll
	lli input.ll


test_strings:
	java -cp $(ANTLR):output:. Main string.txt > input.ll
	lli input.ll

test_bool:
	java -cp $(ANTLR):output:. Main bool.txt > input.ll
	lli input.ll

test_func:
	java -cp $(ANTLR):output:. Main func.txt > input.ll
	lli input.ll

test_if:
	java -cp $(ANTLR):output:. Main if.txt > input.ll
	lli input.ll

test_for:
	java -cp $(ANTLR):output:. Main for.txt > input.ll
	lli input.ll

test:
	java -cp $(ANTLR):output:. Main test.txt > input.ll
	lli input.ll

parsetree: generate
	java -cp $(ANTLR):output:. org.antlr.v4.runtime.misc.TestRig gram prog -gui input.txt

clean:
	rm -f input.ll
	rm -f *.class
	rm -rf output
