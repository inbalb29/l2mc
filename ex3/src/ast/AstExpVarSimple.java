package ast;

import types.*;
import symboltable.*;
import Exceptions.SemanticError;

public class AstExpVarSimple extends AstExpVar {
	/************************/
	/* simple variable name */
	/************************/
	public String name;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpVarSimple(String name) {
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		System.out.format("====================== var -> ID( %s )\n", name);
		this.name = name;
	}

	/**************************************************/
	/* The printing message for a simple var AST node */
	/**************************************************/
	public void printMe() {
		/**********************************/
		/* AST NODE TYPE = AST SIMPLE VAR */
		/**********************************/
		System.out.format("AST NODE SIMPLE VAR( %s )\n", name);

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
				String.format("SIMPLE\nVAR\n(%s)", name));
	}

	public Type semantMe() throws SemanticError {
		Type t;

		t = SymbolTable.getInstance().find(name);
		if (t == null) {
			throw new SemanticError("undefined variable '" + name + "'\n", lineNumber);
		}

		return t;
	}
}
