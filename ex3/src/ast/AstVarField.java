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
		/****************************************************************************************************
		 * Variable Field Access i.e. Accessing a Class Rules
		 * Members of a class can be accessed using v.f, with the following rules:
		 *
		 * • The variable v must have a class type.
		 *
		 * • The name f must be a previously defined member of that class or one of its
		 * superclasses.
		 *
		 *
		 * • The resulting type is the field's type (for fields) or the method's return
		 * type (for methods).
		 *
		 * /
		 ****************************************************************************************************/

		/*******************************************************************/
		/* [1] Semant var (i.e. v) and check that its type is indeed class */
		/*******************************************************************/
		Type v_type = null;
		if (var != null) {
			v_type = var.semantMe();
		}

		if (v_type == null) {
			throw new SemanticError("cannot access field of null variable", lineNumber);
		}

		if (!v_type.isClass()) {
			throw new SemanticError("Cannot access field: '" + fieldName + "' of a non-class variable: '" + var + "'",
					lineNumber);
		}

		TypeClass v = (TypeClass) v_type;

		/****************************************/
		/* [2] Verify thatfieldname (i.e. f) */
		/* is a previously defined method */
		/* inside v's class or superclass */
		/****************************************/
		TypeClassVarDec memberType = v.findMember(fieldName);

		if (memberType == null) {
			throw new SemanticError("Field: '" + fieldName + "' not previously defined in class: '" + v.name
					+ "' or one of its superclasses", lineNumber);
		}

		// If its a method return the methods return type
		if (memberType.t instanceof TypeFunction) {
			return ((TypeFunction) memberType.t).returnType;
		}

		// Wasn't a method so must be a field, and if it's a field, return the actual
		// field's field type
		return memberType.t;
	}
}
