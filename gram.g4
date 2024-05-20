grammar gram;

prog: block EOF;

block: (stat? NL)*;

stat: ID LSP INT RSP '=' value    #assignArrayElem
    | ID LSP INT RSP LSP INT RSP '=' value  #assignMatrixElem
    | PRINT printElem   #printSmth
    | ID '=' expr    #assign
    | (ID LFP INT RFP | ID) '=' array #assignArray
    | (ID LFP INT RFP LFP INT RFP | ID) '=' matrix  #assignMatrix
    | READ ID        #read
    | func           #fun;

printElem: ID   #print
    |   letter  #printLetter;

expr: value         #exprValue
    | letter        #epxrLetter
    |simpleExpr    #exprSimple
    | first+        #exprEq
    | boolexpr2      #exprBool;


first: second (addOrSub)* #firstEq;

second:  simpleExpr (mulOrDiv)+ #secondEq
    | simpleExpr                #secondPass;

mulOrDiv: MUL expr  #mul
    |   DIV expr #divide;

addOrSub: ADD second    #add
    | SUB second        #sub;

simpleExpr: value            #valueexpr
    |  TOINT expr      #toint
    |  TOREAL expr     #toreal
    |  TOFLOAT expr    #tofloat
    |  LP expr RP       #par
    |  SUB value         #neg;

boolexpr2:   brackedBool expr3*
    |       brackedBool;

brackedBool: LP transBool expr3+ RP  #boolExprExtended
    |       LP transBool RP          #boolExpr
    |   transBool                    #trsBool;

expr3: AND  (transBool | brackedBool)    #and
    |  OR  (transBool | brackedBool)     #or
    |  XOR  (transBool | brackedBool)    #xor;

transBool: BOOL     #bool;

func: IF cond THEN blockif ENDIF  #if
    | FOR reps block ENDFOR       #for 
    | FUN fpar block ENDFUN       #function;

blockif: block;

cond: ID '==' INT;

letter: ID LSP INT RSP   #arrayLetter
    | ID LSP INT RSP LSP INT RSP    #matrixLetter
    | ID LSP INT ':' INT RSP #arrayRange;

reps: ID
    | INT;

fpar: ID;

value: STRING   #string
    |  REAL     #real
    |  INT      #int
    |  ID       #vid;


array: LSP INT (COMA INT)* RSP      #intArray
    |  LSP REAL (COMA REAL)* RSP    #realArray;

matrix: LSP matrixRowInt (SEMICOLON matrixRowInt)+ RSP   #intMatrix
    | LSP matrixRowDouble (SEMICOLON matrixRowDouble)+ RSP   #doubleMatrix;

matrixRowInt: (INT (COMA INT)*);

matrixRowDouble: (REAL (COMA REAL)*);



PRINT: 'print' ;
READ: 'read' ;
BOOL: 'true'
    | 'false';

TOINT: '(int)' ;
TOREAL: '(real)' ;
TOFLOAT: '(float)';

AND: 'and';
OR:  'or' ;
XOR: 'xor';
NEG: 'neg';

IF: 'if' ;
THEN: 'then';
ENDIF: 'endif';

FOR: 'for';
ENDFOR: 'endfor';

FUN: 'fun';
ENDFUN: 'endfun';

ADD: '+'  ;
SUB: '-'  ;
MUL: '*'  ;
DIV: '/'  ;
LP:  '('  ;
RP:  ')'  ;
LSP: '['  ;
RSP: ']'  ;
COMA:','  ;
RFP: '}'  ;
LFP: '{'  ;
SEMICOLON: ';';


REAL: ([1-9]([0-9]|[0])*) + '.' + [0-9]+ ;
INT: [1-9][0-9]* | [0];
STRING: '"'[a-zA-Z0-9]*'"';
ID: [a-zA-Z][a-zA-Z0-9]* ;

NL: '\r'? '\n' ;
WS:   [ \t]+ { skip(); } ;