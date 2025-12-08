package ast;

import types.*;

public class AstExpBinop extends AstExp {
	BinOp op;
	public AstExp left;
	public AstExp right;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpBinop(AstExp left, AstExp right, BinOp op, int lineNumber) {
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.print("====================== exp -> exp BINOP exp\n");

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.left = left;
		this.right = right;
		this.op = op;
		this.lineNumber = lineNumber;
	}

	/*************************************************/
	/* The printing message for a binop exp AST node */
	/*************************************************/
	public void printMe() {
		/*************************************/
		/* AST NODE TYPE = AST BINOP EXP */
		/*************************************/
		System.out.print("AST NODE BINOP EXP\n");

		/**************************************/
		/* RECURSIVELY PRINT left + right ... */
		/**************************************/
		if (left != null)
			left.printMe();
		if (right != null)
			right.printMe();

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
				String.format("BINOP(%c)", op.getSymbol()));

		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (left != null)
			AstGraphviz.getInstance().logEdge(serialNumber, left.serialNumber);
		if (right != null)
			AstGraphviz.getInstance().logEdge(serialNumber, right.serialNumber);
	}

	public Type semantMe() {
		Type t1 = null;
		Type t2 = null;

		if (left != null)
			t1 = left.semantMe();
		if (right != null)
			t2 = right.semantMe();

		if ((t1 == TypeInt.getInstance()) && (t2 == TypeInt.getInstance())) {
			return TypeInt.getInstance();
		}
		System.exit(0);
		return null;
	}

}
