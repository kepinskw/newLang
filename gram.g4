grammar gram;

prog: block EOF;

block: (stat? NL)*;

stat: PRINT ID    #print
    | ID '=' expr    #assign
    | READ ID        #read
    | func           #fun ;

expr: value         #exprValue
    |simpleExpr    #exprSimple
    | first+        #exprEq
    ;

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
    |  LP expr RP       #par
    |  SUB value         #neg;

expr3: BOOL AND BOOL
    |  BOOL OR BOOL
    |  BOOL XOR BOOL
    |  NEG BOOL;

func: IF cond THEN blockif ENDIF
    | FOR reps block ENDFOR
    | FUN fpar block ENDFUN ;

blockif: block;

cond: ID '==' num;

reps: ID
    | INT;

fpar: ID;

value: STRING   #string
    |  REAL     #real
    |  INT      #int
    |  ID       #vid
    |  array    #arrayval
    |  BOOL     #bool ;

num: INT
    | REAL ;

array: LSP num (COMA num) RSP ;




PRINT: 'print' ;
READ: 'read' ;
BOOL: 'true'
    | 'false';

TOINT: '(int)' ;
TOREAL: '(real)' ;

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


REAL: ([1-9]([0-9]|[0])*) + '.' + [0-9]+ ;
INT: [1-9][0-9]* ;
STRING: '"'[a-zA-Z0-9]*'"';
ID: [a-zA-Z][a-zA-Z0-9]* ;

NL: '\r'? '\n' ;
WS:   [ \t]+ { skip(); } ;