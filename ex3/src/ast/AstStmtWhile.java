package ast;

import java.util.List;

import Exceptions.SemanticError;
import symboltable.SymbolTable;
import types.*;

public class AstStmtWhile extends AstStmt {
	public AstExp cond;
	public List<AstStmt> body;

	/*******************/
	/* CONSTRUCTOR(S) */
	/*******************/
	public AstStmtWhile(AstExp cond, List<AstStmt> body, int lineNumber) {
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.print("====================== stmt -> WHILE LPAREN exp RPAREN LBRACE stmtList RBRACE\n");

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.cond = cond;
		this.body = body;
		this.lineNumber = lineNumber;
	}

	/********************************************************/
	/* The printing message for a while statement AST node */
	/********************************************************/
	public void printMe() {
		/******************************************/
		/* AST NODE TYPE = AST WHILE STATEMENT */
		/******************************************/
		System.out.print("AST NODE WHILE STMT\n");

		/************************************/
		/* RECURSIVELY PRINT COND + BODY ... */
		/************************************/
		if (cond != null)
			cond.printMe();
		for (AstStmt stmt : body) {
			if (stmt != null)
				stmt.printMe();
		}

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
				"WHILE\nSTMT");

		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (cond != null)
			AstGraphviz.getInstance().logEdge(serialNumber, cond.serialNumber);
		for (AstStmt stmt : body) {
			if (stmt != null)
				AstGraphviz.getInstance().logEdge(serialNumber, stmt.serialNumber);
		}
	}

	@Override
	public Type semantMe() {
		/****************************/
		/* [0] Semant the Condition */
		/****************************/
		Type condType = cond.semantMe();
		if (!condType.isInt()) {
			throw new SemanticError("condition inside while statement is not integral", lineNumber);
		}

		/*************************/
		/* [1] Begin While Scope */
		/*************************/
		SymbolTable.getInstance().beginScope();

		/*******************/
		/* [2] Semant body */
		/*******************/
		for (AstStmt stmt : body) {
			if (stmt != null) {
				stmt.semantMe();
			}
		}

		/*****************/
		/* [3] End Scope */
		/*****************/
		SymbolTable.getInstance().endScope();

		// Return value is irrelevant for if statement
		return null;
	}
}