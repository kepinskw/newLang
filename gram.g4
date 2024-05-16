grammar gram;

prog: block ;

block: (stat? NL)*;

stat: PRINT ID    #print
    | ID '=' expr    #assign
    | READ ID        #read
    | func           #fun ;

expr: expr1           #single0
    | expr1 ADD expr1 #add
    ;

expr1: expr2           #signle1
    |  expr2 MUL expr2 #mul
    |  expr2 DIV expr2 #divide;

expr2: value            #valueexpr
    |  TOINT expr2      #toint
    |  TOREAL expr2     #toreal
    |  LP expr RP       #par
    |  SUB value         #sub
    |  expr3            #signgle2;       

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

value: ID       #vid
    |  INT      #int
    |  REAL     #real
    |  STRING   #string
    |  array    #arrayval
    |  BOOL     #bool ;

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
DIV: '/'  ;
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