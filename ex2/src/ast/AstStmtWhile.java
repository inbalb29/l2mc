package ast;

import java.util.List;

public class AstStmtWhile extends AstStmt {
	public AstExp cond;
	public List<AstStmt> body;

	/*******************/
	/* CONSTRUCTOR(S) */
	/*******************/
	public AstStmtWhile(AstExp cond, List<AstStmt> body) {
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
}