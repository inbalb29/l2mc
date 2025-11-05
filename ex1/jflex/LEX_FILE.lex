/***************************/
/* FILE NAME: LEX_FILE.lex */
/***************************/

/*************/
/* USER CODE */
/*************/

import java_cup.runtime.*;

/******************************/
/* DOLLAR DOLLAR - DON'T TOUCH! */
/******************************/

%%

/************************************/
/* OPTIONS AND DECLARATIONS SECTION */
/************************************/
   
/*****************************************************/ 
/* Lexer is the name of the class JFlex will create. */
/* The code will be written to the file Lexer.java.  */
/*****************************************************/ 
%class Lexer

/********************************************************************/
/* The current line number can be accessed with the variable yyline */
/* and the current column number with the variable yycolumn.        */
/********************************************************************/
%line
%column

/*******************************************************************************/
/* Note that this has to be the EXACT same name of the class the CUP generates */
/*******************************************************************************/
%cupsym TokenNames

/******************************************************************/
/* CUP compatibility mode interfaces with a CUP generated parser. */
/******************************************************************/
%cup

/****************/
/* DECLARATIONS */
/****************/
/*****************************************************************************/   
/* Code between %{ and %}, both of which must be at the beginning of a line, */
/* will be copied verbatim (letter to letter) into the Lexer class code.     */
/* Here you declare member variables and functions that are used inside the  */
/* scanner actions.                                                          */  
/*****************************************************************************/   
%{
	/*********************************************************************************/
	/* Create a new java_cup.runtime.Symbol with information about the current token */
	/*********************************************************************************/
	private Symbol symbol(int type)               {return new Symbol(type, yyline, yycolumn);}
	private Symbol symbol(int type, Object value) {return new Symbol(type, yyline, yycolumn, value);}

	/*******************************************/
	/* Enable line number extraction from main */
	/*******************************************/
	public int getLine() { return yyline + 1; } 

	/**********************************************/
	/* Enable token position extraction from main */
	/**********************************************/
	public int getTokenStartPosition() { return yycolumn + 1; } 
%}

/***********************/
/* MACRO DECLARATIONS */
/***********************/

/* Internal definitions */
_LETTERS 		= [a-zA-Z]
_NON_LETTER 	= [^a-zA-Z\n\r\"] /* Anything that is not a letter, newline, carriage return, or double quote */
_ALPHANUM		= [a-zA-Z0-9]
_COMMENT_BODY   = [a-zA-Z0-9 \t\f\(\)\[\]\{\}\?\!\+\-\*\/\.\;]
_LINETERMINATOR	= \r|\n|\r\n
WhiteSpace		= {_LINETERMINATOR} | [ \t\f]

/* Lexical definitions */

ID				= {_LETTERS}{_ALPHANUM}*
INTEGER			= 0 | [1-9][0-9]*

STRING = \"{_LETTERS}*\"
INVALID_STRING = \"[^\"{_LINETERMINATOR}]*\"  
UNCLOSED_STRING = \"[^\"{_LINETERMINATOR}]*({_LINETERMINATOR}|<<EOF>>)


PLUS			= \+
MINUS			= -
TIMES			= \*
DIVIDE			= \/

LPAREN			= \(
RPAREN			= \)
LBRACK			= \[
RBRACK			= \]
LBRACE			= \{
RBRACE			= \}
COMMA			= ,
DOT				= \.
SEMICOLON		= ;

TYPE_INT		= int
TYPE_STRING		= STRING
TYPE_VOID		= void

ASSIGN 			= :=
EQUAL 			= =
LT 				= <
GT 				= >

ARRAY 			= array
CLASS 			= class
RETURN 			= return
WHILE 			= while
IF 				= if
ELSE 			= else
NEW 			= new
EXTENDS 		= extends
NIL 			= nil

TYPE_1_COMMENT = "//"{_COMMENT_BODY}*{_LINETERMINATOR}
TYPE_2_COMMENT = "/*"({_COMMENT_BODY}|{_LINETERMINATOR})*"*/"
UNCLOSED_TYPE_2_COMMENT = "/*"({_COMMENT_BODY}|{_LINETERMINATOR})*

/******************************/
/* DOLLAR DOLLAR - DON'T TOUCH! */
/******************************/

%%

/************************************************************/
/* LEXER matches regular expressions to actions (Java code) */
/************************************************************/

/**************************************************************/
/* YYINITIAL is the state at which the lexer begins scanning. */
/* So these regular expressions will only be matched if the   */
/* scanner is in the start state YYINITIAL.                   */
/**************************************************************/

<YYINITIAL> {
{TYPE_1_COMMENT}	{ /* Do nothing */ }
{TYPE_2_COMMENT}	{ /* Do nothing */ }
{UNCLOSED_TYPE_2_COMMENT}	{
	throw new Error("Unclosed comment at line " + getLine() + ", column "
		+ getTokenStartPosition());
}

{UNCLOSED_STRING}	{
	throw new Error("Unclosed string at line " + getLine() + ", column "
		+ getTokenStartPosition());
}

{INVALID_STRING}	{
	throw new Error("Invalid string at line " + getLine() + ", column "
		+ getTokenStartPosition());
}

{ARRAY}				{ return symbol(TokenNames.ARRAY);}
{CLASS}				{ return symbol(TokenNames.CLASS);}
{RETURN}			{ return symbol(TokenNames.RETURN);}
{WHILE}				{ return symbol(TokenNames.WHILE);}
{IF}				{ return symbol(TokenNames.IF);}
{ELSE}				{ return symbol(TokenNames.ELSE);}
{NEW}				{ return symbol(TokenNames.NEW);}
{EXTENDS}			{ return symbol(TokenNames.EXTENDS);}
{NIL}				{ return symbol(TokenNames.NIL);}

{TYPE_INT}			{ return symbol(TokenNames.TYPE_INT);}
{TYPE_STRING}		{ return symbol(TokenNames.TYPE_STRING);}
{TYPE_VOID}			{ return symbol(TokenNames.TYPE_VOID);}

{PLUS}				{ return symbol(TokenNames.PLUS);}
{MINUS}				{ return symbol(TokenNames.MINUS);}
{TIMES}				{ return symbol(TokenNames.TIMES);}
{DIVIDE}			{ return symbol(TokenNames.DIVIDE);}
{LPAREN}			{ return symbol(TokenNames.LPAREN);}
{RPAREN}			{ return symbol(TokenNames.RPAREN);}
{LBRACK}			{ return symbol(TokenNames.LBRACK);}
{RBRACK}			{ return symbol(TokenNames.RBRACK);}
{LBRACE}			{ return symbol(TokenNames.LBRACE);}
{RBRACE}			{ return symbol(TokenNames.RBRACE);}
{COMMA}				{ return symbol(TokenNames.COMMA);}
{DOT}				{ return symbol(TokenNames.DOT);}
{SEMICOLON}			{ return symbol(TokenNames.SEMICOLON);}
{ASSIGN}			{ return symbol(TokenNames.ASSIGN);}
{EQUAL}				{ return symbol(TokenNames.EQUAL);}
{LT}				{ return symbol(TokenNames.LT);}
{GT}				{ return symbol(TokenNames.GT);}

{INTEGER}			{
	try {
		int value = Integer.parseInt(yytext());
		if (value < 0 || value > Short.MAX_VALUE) {
			throw new NumberFormatException();
		}
	} catch (NumberFormatException e) {
		throw new Error("Integer out of range: " + yytext());
	}
	return symbol(TokenNames.NUMBER, Integer.valueOf(yytext()));
}
{ID}				{ return symbol(TokenNames.ID,     yytext());}
{WhiteSpace}		{ /* just skip what was found, do nothing */ }
<<EOF>>				{ return symbol(TokenNames.EOF);}
}
