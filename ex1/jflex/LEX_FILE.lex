/***************************/
/* FILE NAME: LEX_FILE.lex */
/***************************/

/*************/
/* USER CODE */
/*************/

import java_cup.runtime.*;
import Exceptions.LexerError;

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
_ALPHANUM		= [a-zA-Z0-9]
_COMMENT_BODY   = [a-zA-Z0-9 \t\(\)\[\]\{\}\?\!\+\-\*\/\.\;]
_LINETERMINATOR	= \r|\n|\r\n
_NOT_CLOSING_COMMENT = [^\*]|\*+[^\*/]

/* Lexical definitions */
WHITESPACE		= {_LINETERMINATOR} | [ \t]

ID				= {_ALPHANUM}*
INT_LITERAL		= [0-9]+

STRING_LITERAL	= \"{_LETTERS}*\"
INVALID_STRING 	= \"[^\"\r\n]*\"  
UNCLOSED_STRING = \"[^\"\r\n]*{_LINETERMINATOR}?

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
TYPE_STRING		= string
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
INVALID_TYPE_1_COMMENT = "//"[^\r\n]*{_LINETERMINATOR}
TYPE_2_COMMENT = "/*"({_COMMENT_BODY}|{_LINETERMINATOR})*"*/"
INVALID_TYPE_2_COMMENT = "/*"{_NOT_CLOSING_COMMENT}*"*/"
UNCLOSED_TYPE_2_COMMENT = "/*"{_NOT_CLOSING_COMMENT}*

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
{INVALID_TYPE_1_COMMENT}	{
	throw new LexerError("Invalid comment", getLine(), getTokenStartPosition());
}
{TYPE_2_COMMENT}	{ /* Do nothing */ }
{INVALID_TYPE_2_COMMENT}	{
	throw new LexerError("Invalid comment", getLine(), getTokenStartPosition());
}
{UNCLOSED_TYPE_2_COMMENT}	{
	throw new LexerError("Unclosed comment", getLine(), getTokenStartPosition());
}

{STRING_LITERAL}	{
	/* Remove the surrounding double quotes before returning the string */
	return symbol(TokenNames.STRING_LITERAL, yytext().substring(1, yytext().length() - 1));
}
{UNCLOSED_STRING}	{
	throw new LexerError("Unclosed string", getLine(), getTokenStartPosition());
}
{INVALID_STRING}	{
	throw new LexerError("Invalid string", getLine(), getTokenStartPosition());
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
{EQUAL}				{ return symbol(TokenNames.EQ);}
{LT}				{ return symbol(TokenNames.LT);}
{GT}				{ return symbol(TokenNames.GT);}

{INT_LITERAL}		{
	try {
		if (yytext().startsWith("0") && yytext().length() > 1) {
			throw new NumberFormatException();
		}
		int value = Integer.parseInt(yytext());
		if (value < 0 || value > Short.MAX_VALUE) {
			throw new NumberFormatException();
		}
		return symbol(TokenNames.INT_LITERAL, Integer.valueOf(value));
	} catch (NumberFormatException e) {
		throw new LexerError("Invalid integer literal '" + yytext() + "'", getLine(), getTokenStartPosition());
	}
}
{ID}				{
	if (yytext().isEmpty()) {
		throw new LexerError("Could not match input (invalid character?)", getLine(), getTokenStartPosition());
	}
	if (Character.isDigit(yytext().charAt(0))) {
		throw new LexerError("Invalid identifier '" + yytext() + "'", getLine(), getTokenStartPosition());
	}
	return symbol(TokenNames.ID, yytext());
}
{WHITESPACE}		{ /* just skip what was found, do nothing */ }
<<EOF>>				{ return symbol(TokenNames.EOF);}
}
