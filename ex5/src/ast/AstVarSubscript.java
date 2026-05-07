package ast;

import Exceptions.SemanticError;
import ir.*;
import temp.Temp;
import temp.TempFactory;
import types.*;

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

	@Override
	public Type semantMe() {
		/****************************************************************************************************
		 * Accessing an array: Elements of an array can be accessed using arrType[e],
		 * with the
		 * following rules:
		 * • The type of the variable arrType must be of an array.
		 * • The subscript expression e must be of type int.
		 * • If e is a constant expression, it must be greater than or equal to zero
		 * • The resulting type is the element type T of the array that arrType is
		 * defined
		 * over.
		 ****************************************************************************************************/

		/*******************************************************************/
		/* [1] Semant var (i.e. arrType) and check that its type is indeed class */
		/*******************************************************************/
		Type varType = null;
		if (var != null) {
			varType = var.semantMe();
		}

		if (varType == null) {
			throw new SemanticError("cannot subscript undefined variable", lineNumber);
		}

		if (!varType.isArray()) {
			throw new SemanticError("cannot subscript non- array type'" + varType.name + "'", lineNumber);
		}

		TypeArray arrType = (TypeArray) varType;

		/************************************/
		/* [2] Verify subscript expression */
		/* (i.e. e) is of type int */
		/* and if constant then >= 0 */
		/************************************/

		Type subscriptType = null;
		if (subscript != null) {
			subscriptType = subscript.semantMe();
		}

		if (subscriptType == null) {
			throw new SemanticError(
					"Array subscript type not found, cannot access array using undefined identifier ",
					lineNumber);
		}

		if (!subscriptType.isInt()) {
			throw new SemanticError("array subscript must be of type int", lineNumber);
		}

		// Check if subscript is constant, and if it is verify >= 0.
		if (subscript instanceof AstExpInt index && index.value < 0) {
			throw new SemanticError(
					"array subscript expression '" + subscriptType.name + "' must be greater than 0",
					lineNumber);
		}

		return arrType.innerType;
	}

	@Override
	public Temp irMe() {
		Temp arrTemp = var.irMe();
		Ir.getInstance().AddIrCommand(new IrCommandCheckNullPtr(arrTemp));
		Temp idxTemp = subscript.irMe();
		Temp dst = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IrCommandArrayGet(dst, arrTemp, idxTemp));
		return dst;
	}
}
