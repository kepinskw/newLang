grammar gram;

prog: block EOF;

block: (stat? NL)*;

stat: PRINT ID    #print
    | ID '=' expr    #assign
    | READ ID        #read
    | func           #fun
    | cond           #codn;

expr: value         #exprValue
    | letter        #epxrLetter
    |simpleExpr    #exprSimple
    | first+        #exprEq
    | boolexpr2      #exprBool
    | array         #exprArray;


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

boolexpr2:   brackedBool expr3*
    |       brackedBool;

brackedBool: LP BOOL expr3+ RP  #boolExprExtended
    |       LP BOOL RP          #boolExpr
    |   BOOL                    #bool;

expr3: AND  (BOOL | brackedBool)    #and
    |  OR  (BOOL | brackedBool)     #or
    |  XOR  (BOOL | brackedBool)    #xor;

func: IF cond THEN blockif ENDIF
    | FOR reps block ENDFOR
    | FUN fpar block ENDFUN ;

blockif: block;

cond: ID '==' (REAL | INT | STRING | BOOL );

letter: ID LSP INT RSP   #stringLetter
    | ID LSP INT ':' INT RSP #stringRange;

reps: ID
    | INT;

fpar: ID;

value: STRING   #string
    |  REAL     #real
    |  INT      #int
    |  ID       #vid
    |  array    #arrayval;


array: LSP INT (COMA INT)* RSP      #intArray
    |  LSP REAL (COMA REAL)* RSP    #realArray;




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