package ast;

import types.*;
import Exceptions.SemanticError;

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

	// public Type semantMe() {
	// Type t1 = null;
	// Type t2 = null;

	// if (left != null)
	// t1 = left.semantMe();
	// if (right != null)
	// t2 = right.semantMe();

	// if ((t1 == TypeInt.getInstance()) && (t2 == TypeInt.getInstance())) {
	// return TypeInt.getInstance();
	// }
	// System.exit(0);
	// return null;
	// }

	public Type semantMe() throws SemanticError {
		Type leftType = null;
		Type rightType = null;

		if (left != null) {
			leftType = left.semantMe();
		}
		if (right != null) {
			rightType = right.semantMe();
		}

		if ((!leftType.isClass() && !rightType.isClass()) && leftType != rightType) {
			throw new SemanticError(
					"mismatching types '" + leftType + "', '" + rightType + "' for binary operation exp " + op
							+ " exp\n",
					lineNumber);
		}

		if (op == BinOp.PLUS) {
			if (!leftType.isInt() && !leftType.isString()) {
				throw new SemanticError("left-hand expression type, '" + leftType + "', should be integer or string\n",
						lineNumber);
			}
			if (!rightType.isInt() && !rightType.isString()) {
				throw new SemanticError(
						"right-hand expression type, '" + rightType + "', should be integer or string\n", lineNumber);
			}
		} else if (op == BinOp.EQ) {
			if (leftType.isClass() && rightType.isClass()) {
				for (TypeClass ancestor = (TypeClass) leftType; ancestor != null; ancestor = ancestor.father) {
					if (ancestor.name.equals(rightType.name)) {
						return TypeInt.getInstance();
					}
				}

				for (TypeClass ancestor = (TypeClass) rightType; ancestor != null; ancestor = ancestor.father) {
					if (ancestor.name.equals(leftType.name)) {
						return TypeInt.getInstance();
					}
				}

				throw new SemanticError("comparison of mismatching classes '" + leftType + "', '" + rightType + "'\n",
						lineNumber);
			}

			// leftType and rightType are the same type
			return TypeInt.getInstance();
		} else {
			if (!leftType.isInt()) {
				throw new SemanticError("left-hand expression type, '" + leftType + "', should be integer\n",
						lineNumber);
			}

			if (!rightType.isInt()) {
				throw new SemanticError("right-hand expression type, '" + rightType + "', should be integer\n",
						lineNumber);
			}
		}

		if (op == BinOp.DIVIDE) {
			if (((AstExpInt) right).value == 0) {
				throw new SemanticError("cannot divide by 0\n", lineNumber);
			}
		}

		return leftType;
	}

}
