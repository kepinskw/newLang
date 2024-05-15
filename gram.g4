grammar gram;

prog: block ;

block: (stat? NL)*;

stat: PRINT ID    #print
    | ID '=' INT    #assign
    | READ ID        #read
    | func           #fun ;

expr: expr1
    | expr1 ADD expr1;

expr1: expr2
    |  expr2 MUL expr2 ;

expr2: value
    |  TOINT expr2
    |  TOREAL expr2
    |  LP expr RP 
    |  SUB expr
    |  expr3;

expr3: BOOL AND BOOL
    |  BOOL OR BOOL
    |  BOOL XOR BOOL
    |  NEG BOOL;

func: IF cond THEN blockif ENDIF
    | FOR reps block ENDFOR 
    | FUN fpar block ENDFUN ;

blockif: block;

cond: ID '==' num; /* Sprawdzic czy zadziala z string i array */

reps: ID
    | INT;

fpar: ID;

value: ID
    |  INT
    |  REAL
    |  STRING
    |  array 
    |  BOOL;

num: INT
    | REAL ;

array: LSP num (COMA num)* RSP ;




PRINT: 'print' ;
READ: 'read' ;

ID: [a-zA-Z][a-zA-Z0-9]* ;

INT: [1-9][0-9]* ;
REAL: ([1-9][0-9]*|[0]).[0-9]+ ;
STRING: '"'[a-zA-Z0-9]*'"';
BOOL: 'true'
    | 'false';

TOINT: '(int)' ;
TOREAL: '(real)' ;

ADD: '+'  ;
SUB: '-'  ;
MUL: '*'  ;
LP:  '('  ;
RP:  ')'  ;
LSP: '['  ;
RSP: ']'  ;
COMA:','  ;

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

NL: '\r'? '\n' ;
WS:   [ \t]+ { skip(); } ;