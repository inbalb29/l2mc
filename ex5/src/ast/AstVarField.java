package ast;

import Exceptions.SemanticError;
import codegen.ClassLayout;
import ir.*;
import temp.Temp;
import temp.TempFactory;
import types.*;

public class AstVarField extends AstVar {
	public AstVar var;
	public String fieldName;
	public TypeClass resolvedClass; // Set during semantMe for IR generation

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
			throw new SemanticError("cannot access field '" + fieldName + "' of non-class type'" + t.name + "'",
					lineNumber);
		}

		TypeClass tc = (TypeClass) t;
		this.resolvedClass = tc; // Store for IR generation

		/************************************/
		/* [3] Look for fieldName inside tc */
		/************************************/
		Type memberType = tc.findMember(fieldName);

		if (memberType == null) {
			throw new SemanticError("field '" + fieldName + "' not found in '" + tc.name + "'", lineNumber);
		}

		// If it's a TypeClassField, return the actual field type
		if (memberType instanceof TypeClassField) {
			return ((TypeClassField) memberType).fieldType;
		}

		// If it's a function, return it as-is for method calls
		if (memberType instanceof TypeFunction) {
			return ((TypeFunction) memberType).returnType;
		}

		return memberType;
	}

	@Override
	public Temp irMe() {
		Temp objTemp = var.irMe();
		Ir.getInstance().AddIrCommand(new IrCommandCheckNullPtr(objTemp));
		ClassLayout layout = ClassLayout.getLayout(resolvedClass.name);
		int fieldOffset = layout.getFieldOffset(fieldName);
		Temp dst = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IrCommandFieldGet(dst, objTemp, fieldOffset));
		return dst;
	}
}
