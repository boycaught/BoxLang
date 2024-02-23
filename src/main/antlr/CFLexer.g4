lexer grammar CFLexer;

options {
	caseInsensitive = true;
}

/**
 * DEFAULT MODE
 https://github.com/antlr/antlr4/blob/master/doc/lexer-rules.md#lexical-modes
 */
ABSTRACT: 'ABSTRACT';
ANY: 'ANY';
APPLICATION: 'APPLICATION';
ARGUMENTS: 'ARGUMENTS';
ARRAY: 'ARRAY';
AS: 'AS';
ASSERT: 'ASSERT';
BOOLEAN: 'BOOLEAN';
BREAK: 'BREAK';
CASE: 'CASE';
CASTAS: 'CASTAS';
CATCH: 'CATCH';
CGI: 'CGI';
CLASS: 'CLASS';
COMPONENT: 'COMPONENT';
CONTAIN: 'CONTAIN';
CONTAINS: 'CONTAINS';
CONTINUE: 'CONTINUE';
COOKIE: 'COOKIE';
DEFAULT: 'DEFAULT';
DO: 'DO';
DOES: 'DOES';
ELIF: 'ELIF';
ELSE: 'ELSE';
EQV: 'EQV';
FALSE: 'FALSE';
FINALLY: 'FINALLY';
FOR: 'FOR';
FORM: 'FORM';
FUNCTION: 'FUNCTION';
GREATER: 'GREATER';
IF: 'IF';
IMP: 'IMP';
IMPORT: 'IMPORT';
IN: 'IN';
INCLUDE: 'INCLUDE';
INSTANCEOF: 'INSTANCEOF';
INTERFACE: 'INTERFACE';
IS: 'IS';
JAVA: 'JAVA';
LESS: 'LESS';
LOCAL: 'LOCAL';
MESSAGE: 'MESSAGE';
MOD: 'MOD';
NEW: 'NEW';
NULL: 'NULL';
NUMERIC: 'NUMERIC';
PARAM: 'PARAM';
PACKAGE: 'PACKAGE';
PRIVATE: 'PRIVATE';
PROPERTY: 'PROPERTY';
PUBLIC: 'PUBLIC';
QUERY: 'QUERY';
REMOTE: 'REMOTE';
REQUEST: 'REQUEST';
REQUIRED: 'REQUIRED';
RETHROW: 'RETHROW';
RETURN: 'RETURN';
SERVER: 'SERVER';
SESSION: 'SESSION';
SETTING: 'SETTING';
STATIC: 'STATIC';
STRING: 'STRING';
STRUCT: 'STRUCT';
SUPER: 'SUPER';
SWITCH: 'SWITCH';
THAN: 'THAN';
THIS: 'THIS';
THREAD: 'THREAD';
THROW: 'THROW';
TO: 'TO';
TRUE: 'TRUE';
TRY: 'TRY';
TYPE: 'TYPE';
URL: 'URL';
VAR: 'VAR';
VARIABLES: 'VARIABLES';
WHEN: 'WHEN';
WHILE: 'WHILE';
XOR: 'XOR';

AND: 'AND';
AMPAMP: '&&';

EQ: 'EQ';
EQUAL: 'EQUAL';
EQEQ: '==';

GT: 'GT';
GTSIGN: '>';

GTE: 'GTE';
GE: 'GE';
GTESIGN: '>=';

LT: 'LT';
LTSIGN: '<';

LTE: 'LTE';
LE: 'LE';
LTESIGN: '<=';

NEQ: 'NEQ';
BANGEQUAL: '!=';
LESSTHANGREATERTHAN: '<>';

NOT: 'NOT';
BANG: '!';

OR: 'OR';
PIPEPIPE: '||';

AMPERSAND: '&';
ARROW: '->';
AT: '@';
BACKSLASH: '\\';
COMMA: ',';
COLON: ':';
COLONCOLON: '::';
DOT: '.';
ELVIS: '?:';
EQUALSIGN: '=';
LBRACE: '{';
RBRACE: '}';
LPAREN: '(';
RPAREN: ')';
LBRACKET: '[';
RBRACKET: ']';
ARROW_RIGHT: '=>';
MINUS: '-';
MINUSMINUS: '--';
PIPE: '|';
PERCENT: '%';
POWER: '^';
QM: '?';
SEMICOLON: ';';
SLASH: '/';
STAR: '*';
CONCATEQUAL: '&=';
PLUSEQUAL: '+=';
MINUSEQUAL: '-=';
STAREQUAL: '*=';
SLASHEQUAL: '/=';
MODEQUAL: '%=';
PLUS: '+';
PLUSPLUS: '++';
TEQ: '===';
PREFIX: 'CF';

// ANY NEW LEXER RULES FOR AN ENGLISH WORD NEEDS ADDED TO THE identifer RULE IN THE PARSER

ICHAR_1:
	'#' {_modeStack.contains(hashMode)}? -> type(ICHAR), popMode, popMode;
ICHAR: '#';

WS: (' ' | '\t' | '\f')+ -> channel(HIDDEN);
NEWLINE: ('\n' | '\r')+ (' ' | '\t' | '\f' | '\n' | '\r')* -> channel(HIDDEN);
JAVADOC_COMMENT: '/**' .*? '*/' -> channel(HIDDEN);

COMMENT: '/*' .*? '*/' -> skip;

LINE_COMMENT: '//' ~[\r\n]* -> skip;

OPEN_QUOTE: '"' -> pushMode(quotesMode);

OPEN_SINGLE: '\'' -> type(OPEN_QUOTE), pushMode(squotesMode);

fragment DIGIT: [0-9];
fragment E_SIGN: [e];
fragment E_NOTATION: E_SIGN [+-]? DIGIT+;
FLOAT_LITERAL:
	DIGIT+ DOT DIGIT* (E_NOTATION)?
	| DOT DIGIT+ (E_NOTATION)?
	| DIGIT+ E_NOTATION;

INTEGER_LITERAL: DIGIT+;
PREFIXEDIDENTIFIER: PREFIX IDENTIFIER;
IDENTIFIER: [a-z_$]+ ( [_]+ | [a-z]+ | DIGIT)*;

COMPONENT_ISLAND_START: '```' -> pushMode(componentIsland);

mode componentIsland;

COMPONENT_ISLAND_END: '```' -> popMode;

COMPONENT_ISLAND_BODY: .+?;

mode squotesMode;
CLOSE_SQUOTE: '\'' -> type(CLOSE_QUOTE), popMode;

SHASHHASH: '##' -> type(HASHHASH);

SSTRING_LITERAL: (~['#]+ | '\'\'')+ -> type(STRING_LITERAL);

SHASH:
	'#' -> type(ICHAR), pushMode(hashMode), pushMode(DEFAULT_MODE);

mode quotesMode;
CLOSE_QUOTE: '"' -> popMode;

HASHHASH: '##';
STRING_LITERAL: (~["#]+ | '""')+;

HASH:
	'#' -> type(ICHAR), pushMode(hashMode), pushMode(DEFAULT_MODE);

mode hashMode;
HANY: [.]+ -> popMode, skip;