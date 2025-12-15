package ast;

import java.util.List;

import types.*;
import symboltable.*;
import Exceptions.SemanticError;

public class AstStmtIf extends AstStmt {
	public AstExp cond;
	public List<AstStmt> thenBody;
	public List<AstStmt> elseBody; // null if no else clause

	/*******************/
	/* CONSTRUCTOR(S) */
	/*******************/
	public AstStmtIf(AstExp cond, List<AstStmt> thenBody, List<AstStmt> elseBody, int lineNumber) {
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		if (elseBody != null) {
			System.out.print(
					"====================== stmt -> IF LPAREN exp RPAREN LBRACE stmtList RBRACE ELSE LBRACE stmtList RBRACE\n");
		} else {
			System.out.print("====================== stmt -> IF LPAREN exp RPAREN LBRACE stmtList RBRACE\n");
		}

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.cond = cond;
		this.thenBody = thenBody;
		this.elseBody = elseBody;
		this.lineNumber = lineNumber;
	}

	/*****************************************************/
	/* The printing message for an if statement AST node */
	/*****************************************************/
	public void printMe() {
		/***************************************/
		/* AST NODE TYPE = AST IF STATEMENT */
		/***************************************/
		System.out.print("AST NODE IF STMT\n");

		/************************************/
		/* RECURSIVELY PRINT COND + BODY ... */
		/************************************/
		if (cond != null)
			cond.printMe();
		for (AstStmt stmt : thenBody) {
			if (stmt != null)
				stmt.printMe();
		}
		if (elseBody != null) {
			for (AstStmt stmt : elseBody) {
				if (stmt != null)
					stmt.printMe();
			}
		}

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
				"IF\nSTMT");

		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (cond != null)
			AstGraphviz.getInstance().logEdge(serialNumber, cond.serialNumber);
		for (AstStmt stmt : thenBody) {
			if (stmt != null)
				AstGraphviz.getInstance().logEdge(serialNumber, stmt.serialNumber);
		}
		if (elseBody != null) {
			for (AstStmt stmt : elseBody) {
				if (stmt != null)
					AstGraphviz.getInstance().logEdge(serialNumber, stmt.serialNumber);
			}
		}
	}

	@Override
	public Type semantMe() {
		/****************************/
		/* [0] Semant the Condition */
		/****************************/
		if (cond.semantMe() != TypeInt.getInstance()) {
			throw new SemanticError("condition inside if statement is not integral", lineNumber);
		}

		/*************************/
		/* [1] Begin then Scope */
		/*************************/
		SymbolTable.getInstance().beginScope();

		/***************************/
		/* [2] Semant then body */
		/***************************/
		for (AstStmt stmt : thenBody) {
			if (stmt != null) {
				stmt.semantMe();
			}
		}

		/***********************/
		/* [3] End then Scope */
		/***********************/
		SymbolTable.getInstance().endScope();

		/***********************************/
		/* [4] Semant else body if present */
		/***********************************/
		if (elseBody != null) {
			SymbolTable.getInstance().beginScope();

			for (AstStmt stmt : elseBody) {
				if (stmt != null) {
					stmt.semantMe();
				}
			}

			SymbolTable.getInstance().endScope();
		}

		// Return value is irrelevant for if statement
		return null;
	}
}
