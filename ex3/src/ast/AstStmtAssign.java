package ast;

import types.*;
import Exceptions.SemanticError;

public class AstStmtAssign extends AstStmt {
	/***************/
	/* var := exp */
	/***************/
	public AstVar var;
	public AstExp exp; // Can be AstNewExp since it extends AstExp

	/*******************/
	/* CONSTRUCTOR(S) */
	/*******************/
	public AstStmtAssign(AstVar var, AstExp exp, int lineNumber) {
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		if (exp instanceof AstNewExp) {
			System.out.print("====================== stmt -> var ASSIGN newExp SEMICOLON\n");
		} else {
			System.out.print("====================== stmt -> var ASSIGN exp SEMICOLON\n");
		}

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.var = var;
		this.exp = exp;
		this.lineNumber = lineNumber;
	}

	/*********************************************************/
	/* The printing message for an assign statement AST node */
	/*********************************************************/
	public void printMe() {
		/********************************************/
		/* AST NODE TYPE = AST ASSIGNMENT STATEMENT */
		/********************************************/
		System.out.print("AST NODE ASSIGN STMT\n");

		/***********************************/
		/* RECURSIVELY PRINT VAR + EXP ... */
		/***********************************/
		if (var != null)
			var.printMe();
		if (exp != null)
			exp.printMe();

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
				"ASSIGN\nleft := right\n");

		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (var != null)
			AstGraphviz.getInstance().logEdge(serialNumber, var.serialNumber);
		if (exp != null)
			AstGraphviz.getInstance().logEdge(serialNumber, exp.serialNumber);
	}

	public Type semantMe() throws SemanticError {
		Type varType = null;
		Type expType = null;

		if (var != null)
			varType = var.semantMe();
		if (exp != null)
			expType = exp.semantMe();

		if (!varType.isClass() && !varType.isArray() && expType.isVoid()) {
			throw new SemanticError(
					"type '" + varType.name + "' cannot be assigned NIL\n",
					lineNumber);
		}

		if (expType.isArray() && !varType.isArray()) {
			throw new SemanticError("non array type '" + varType.name + "' cannon be assigned array\n",
					lineNumber);
		}

		if (varType.isArray() && expType.isArray()) {
			if (varType.name != expType.name) {
				throw new SemanticError(
						"mismatching array types '" + varType.name + "', '" + expType.name +
								"' for variable assignment\n",
						lineNumber);
			}

			if (((TypeArray) varType).innerType.name != ((TypeArray) expType).innerType.name) {
				throw new SemanticError(
						"mismatching array inner types '" + ((TypeArray) varType).innerType.name +
								"', '" + ((TypeArray) expType).innerType.name +
								"' for variable assignment\n",
						lineNumber);
			}
		}

		if (varType.isClass() && expType.isClass()) {
			for (TypeClass ancestor = (TypeClass) expType; ancestor != null; ancestor = ancestor.father) {
				if (ancestor.name.equals(varType.name)) {
					return null;
				}
			}
			throw new SemanticError(
					"assignment of mismatching classes '" + varType.name + "', '" + expType.name + "'\n",
					lineNumber);
		}

		if (varType != expType) {
			throw new SemanticError(
					"mismatching types '" + varType.name + "', '" + expType.name + "' for variable assignment\n",
					lineNumber);
		}
		return null;
	}
}
