grammar gram;

prog: block EOF;

block: (stat? NL)*;

stat: PRINT letter #printLetter
    | PRINT ID    #print
    | ID '=' expr    #assign
    | (ID LFP INT RFP | ID) '=' array #assignArray
    | READ ID        #read
    | func           #fun
    | cond           #codn;

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

func: IF cond THEN blockif ENDIF
    | FOR reps block ENDFOR
    | FUN fpar block ENDFUN ;

blockif: block;

cond: ID '==' (REAL | INT | STRING | BOOL );

letter: ID LSP INT RSP   #arrayLetter
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


REAL: ([1-9]([0-9]|[0])*) + '.' + [0-9]+ ;
INT: [1-9][0-9]* | [0];
STRING: '"'[a-zA-Z0-9]*'"';
ID: [a-zA-Z][a-zA-Z0-9]* ;

NL: '\r'? '\n' ;
WS:   [ \t]+ { skip(); } ;