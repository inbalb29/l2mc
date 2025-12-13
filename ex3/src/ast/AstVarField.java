package ast;

import Exceptions.SemanticError;
import types.*;

public class AstVarField extends AstVar {
	public AstVar var;
	public String fieldName;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstVarField(AstVar var, String fieldName, int lineNumber) {
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.format("====================== var -> var DOT ID( %s )\n", fieldName);

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.var = var;
		this.fieldName = fieldName;
		this.lineNumber = lineNumber;
	}

	/*************************************************/
	/* The printing message for a field var AST node */
	/*************************************************/
	public void printMe() {
		/*********************************/
		/* AST NODE TYPE = AST FIELD VAR */
		/*********************************/
		System.out.print("AST NODE FIELD VAR\n");

		/**********************************************/
		/* RECURSIVELY PRINT VAR, then FIELD NAME ... */
		/**********************************************/
		if (var != null)
			var.printMe();
		System.out.format("FIELD NAME( %s )\n", fieldName);

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
				String.format("FIELD\nVAR\n.%s", fieldName));

		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (var != null)
			AstGraphviz.getInstance().logEdge(serialNumber, var.serialNumber);
	}

	@Override
	public Type semantMe() {
		/******************************/
		/* [1] Recursively semant var */
		/******************************/
		Type t = null;
		if (var != null) {
			t = var.semantMe();
		}

		if (t == null) {
			throw new SemanticError("cannot access field of null variable", lineNumber);
		}

		/*********************************/
		/* [2] Make sure type is a class */
		/*********************************/
		if (!t.isClass()) {
			throw new SemanticError("access '" + fieldName + "' field of a non-class variable", lineNumber);
		}

		TypeClass tc = (TypeClass) t;

		/************************************/
		/* [3] Look for fiedlName inside tc */
		/************************************/
		TypeClassVarDec memberType = tc.findMember(fieldName);

		if (memberType == null) {
			throw new SemanticError("field '" + fieldName + "' does not exist in class '" + tc.name + "'", lineNumber);
		}

		if (memberType.t instanceof TypeFunction) {
			return ((TypeFunction) memberType.t).returnType;
		}

		// If it's a field, return the actual field type
		return memberType.t;
	}
}
