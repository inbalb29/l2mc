package ast;

import types.*;
import Exceptions.SemanticError;

public class AstVarSubscript extends AstVar {
	public AstVar var;
	public AstExp subscript;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstVarSubscript(AstVar var, AstExp subscript, int lineNumber) {
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.print("====================== var -> var [ exp ]\n");

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.var = var;
		this.subscript = subscript;
		this.lineNumber = lineNumber;
	}

	/*****************************************************/
	/* The printing message for a subscript var AST node */
	/*****************************************************/
	public void printMe() {
		/*************************************/
		/* AST NODE TYPE = AST SUBSCRIPT VAR */
		/*************************************/
		System.out.print("AST NODE SUBSCRIPT VAR\n");

		/****************************************/
		/* RECURSIVELY PRINT VAR + SUBSCRIPT ... */
		/****************************************/
		if (var != null)
			var.printMe();
		if (subscript != null)
			subscript.printMe();

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
				"SUBSCRIPT\nVAR\n...[...]");

		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (var != null)
			AstGraphviz.getInstance().logEdge(serialNumber, var.serialNumber);
		if (subscript != null)
			AstGraphviz.getInstance().logEdge(serialNumber, subscript.serialNumber);
	}

	public Type semantMe() {
		/****************************************************************************************************
		 * Accessing an array: Elements of an array can be accessed using v[e], with the
		 * following rules:
		 * • The type of the variable v must be of an array.
		 * • The subscript expression e must be of type int.
		 * • If e is a constant expression, it must be greater than or equal to zero
		 * • The resulting type is the element type T of the array that v is defined
		 * over.
		 ****************************************************************************************************/

		/*******************************************************************/
		/* [1] Semant var (i.e. v) and check that its type is indeed class */
		/*******************************************************************/
		Type v_type = null;
		if (var != null) {
			v_type = var.semantMe();
		}

		if (v_type == null) {
			throw new SemanticError("Cannot find array: '" + var + "', array undefined", lineNumber);
		}

		if (!v_type.isArray()) {
			throw new SemanticError("Cannot use array notation to access non array object: '" + var + "'", lineNumber);
		}

		TypeArray v = (TypeArray) v_type;

		/************************************/
		/* [2] Verify subscript expression */
		/* (i.e. e) is of type int */
		/* and if constant then >= 0 */
		/************************************/

		Type e_type = null;
		if (subscript != null) {
			e_type = subscript.semantMe();
		}

		if (e_type == null) {
			throw new SemanticError(
					"Array subscript: '" + subscript + "' not found, cannot access array using undefined identifier ",
					lineNumber);
		}

		if (!v_type.isInt()) {
			throw new SemanticError("Subscript expression: '" + subscript + "' must be of type int", lineNumber);
		}

		TypeInt e = (TypeInt) e_type;

		// Check if subscript is constant, and if it is verify >= 0.
		if (subscript instanceof AstExpInt && ((AstExpInt) subscript).value < 0) {
			throw new SemanticError("Constant subscript expression: '" + subscript + "' must be greater than 0",
					lineNumber);
		}

		return v.innerType;
	}
}
