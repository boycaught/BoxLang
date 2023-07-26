lexer grammar  CFLexer;

options {
    caseInsensitive = true;
}

// TODO: temporary patch for parsed files beginning with the \uFEFF unicode character
ZWNBSP: '\uFEFF' { System.err.println("Ignoring \\uFEFF unicode character"); } -> skip;

BEGIN:  -> skip, pushMode(codeMode);

mode codeMode;
ABSTRACT        :   'ABSTRACT';
ABORT           :   'ABORT';
ADMIN           :   'ADMIN';
ANY             :   'ANY';
APPLICATION     :   'APPLICATION';
ARGUMENTS       :   'ARGUMENTS';
ARRAY           :   'ARRAY';
BOOLEAN         :   'BOOLEAN';
BREAK           :   'BREAK';
CASE            :   'CASE';
CATCH           :   'CATCH';
CLASS           :   'CLASS';
COMPONENT       :   'COMPONENT';
CONTAINS        :    'CONTAINS';
CONTINUE        :   'CONTINUE';
DEFAULT         :   'DEFAULT';
EXTENDS         :   'EXTENDS';
ELSE            :   'ELSE';
ELIF            :   'ELIF';
FALSE           :   'FALSE';
FINALLY         :   'FINALLY';
FOR             :   'FOR';
FUNCTION        :   'FUNCTION';
IF              :   'IF';
IN              :   'IN';
IMPLEMENTS      :   'IMPLEMENTS';
IMPORT          :   'IMPORT';
INCLUDE         :   'INCLUDE';
INIT            :   'INIT';
INTERFACE       :   'INTERFACE';
IS              :   'IS';
LOCAL           :   'LOCAL';
LOCK            :   'LOCK';
NEW             :   'NEW';
NUMERIC         :   'NUMERIC';
PACKAGE         :   'PACKAGE';
PARAM           :   'PARAM';
PRIVATE         :   'PRIVATE';
PROPERTY        :   'PROPERTY';
PUBLIC          :   'PUBLIC';
QUERY           :   'QUERY';
REMOTE          :   'REMOTE';
REQUIRED        :   'REQUIRED';
REQUEST         :   'REQUEST';
RETURN          :   'RETURN';
RETHROW         :   'RETHROW';
SAVECONTENT     :   'SAVECONTENT';
SETTING         :   'SETTING';
STATIC          :   'STATIC';
STRING          :   'STRING';
STRUCT          :   'STRUCT';
SWITCH          :   'SWITCH';
THIS            :   'THIS';
THREAD          :   'THREAD';
THROW           :   'THROW';
TYPE            :   'TYPE';
TRUE            :   'TRUE';
TRY             :   'TRY';
VAR             :   'VAR';
VARIABLES       :   'VARIABLES';
WHEN            :   'WHEN';
WHILE           :   'WHILE';

AMPERSAND       :   '&';
AND             :   '&&' | 'AND';
ARROW           :   '->';
COMMA           :   ',';
COLON           :   ':';
DOT             :   '.';
ELVIS           :   '?:';
EQUAL           :   '=';
EQ              :   '==' | 'EQ';
GT              :   '>'  | 'GT';
GTE             :   '>=' | 'GTE';
LBRACE          :   '{';
RBRACE          :   '}';
LPAREN          :   '(';
RPAREN          :   ')';
LBRACKET        :   '[';
RBRACKET        :   ']';
ARROW_RIGHT     :   '=>';
LT              :   '<'  | 'LT';
LTE             :   '<=' | 'LTE';
MINUS           :   '-';
NEQ             :   '!=' | 'NEQ' | '<>';
NOT             :   '!'  | 'NOT';
OR              :   '||' | 'OR';
PERCENT         :   '%';
QM              :   '?';
SEMICOLON       :   ';';
SLASH           :   '/';
STAR            :   '*';
STRCONCASSIGN   :   '&=';
PLUSEQUAL       :   '+=';
PLUS            :   '+';




LCFTAG          :   '<' CFTAG '>' ;
RCFTAG          :   '</' CFTAG '>' ;
fragment CFTAG  :   'cfscript' ;



ICHAR_1         :   '#' {_modeStack.contains(hashMode)}? -> type(ICHAR),popMode,popMode;
ICHAR           :   '#';

WS              :   (' ' | '\t' | '\f' )+ -> skip;
NEWLINE         :   ('\n' | '\r' )+
                    (' ' | '\t' | '\f' | '\n' | '\r' )* -> channel(HIDDEN);
MULTILINE_COMMENT
    :   '/*' (.)*? '*/' -> channel(HIDDEN)
    ;

LINE_COMMENT :
            '//'
            ( ~('\n'|'\r') )*
            ( '\n'|'\r'('\n')? )?
      -> channel(HIDDEN) ;

OPEN_QUOTE      :   '"' -> pushMode(quotesMode);

OPEN_SINGLE     :   '\''-> type(OPEN_QUOTE),pushMode(squotesMode);

FLOAT_LITERAL
  : [0-9]+ DOT [0-9]* ([eE] [+-]? [0-9]+)?
  | DOT [0-9]+ ([eE] [+-]? [0-9]+)?
  | [0-9]+ [eE] [+-]? [0-9]+
  ;

INTEGER_LITERAL :   [0-9]+;
IDENTIFIER      :	[a-z_$]+ ( [_]+ | [a-z]+ | [0-9])*;



mode squotesMode;
CLOSE_SQUOTE
	: '\''	-> type(CLOSE_QUOTE),popMode
    ;

SHASHHASH
	: '##'
    ;

SSTRING_LITERAL
    : (~['#]+ | '\'\'' )* -> type(STRING_LITERAL)
    ;

SHASH
	: '#' -> type(ICHAR),pushMode(hashMode),pushMode(codeMode)
    ;


mode quotesMode;
CLOSE_QUOTE
	: '"'
	-> popMode
    ;

HASHHASH
	: '##'
;
STRING_LITERAL
	: (~["#]+ | '""' )*
;

HASH
	: '#' -> type(ICHAR),pushMode(hashMode),pushMode(codeMode)
;


mode hashMode;
HANY
    :  -> popMode,skip;

