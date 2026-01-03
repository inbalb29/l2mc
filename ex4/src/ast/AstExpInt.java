package ast;

import types.*;
import temp.*;
import ir.*;

public class AstExpInt extends AstExp {
	public int value;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpInt(int value, int lineNumber) {
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.format("====================== exp -> INT( %d )\n", value);

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.value = value;
		this.lineNumber = lineNumber;
	}

	/************************************************/
	/* The printing message for an int exp AST node */
	/************************************************/
	public void printMe() {
		/*******************************/
		/* AST NODE TYPE = AST INT EXP */
		/*******************************/
		System.out.format("AST NODE INT( %d )\n", value);

		/*********************************/
		/* Print to AST GRAPHVIZ DOT file */
		/*********************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
				String.format("INT(%d)", value));
	}

	@Override
	public Type semantMe() {
		return TypeInt.getInstance();
	}

	public Temp irMe() {
		// Create a fresh register to "hold" the int value
		Temp t = TempFactory.getInstance().getFreshTemp();

		// Create the IR command to load the int value into the register
		// I.e. t = value // li $t, value
		Ir.getInstance().AddIrCommand(new IRcommandConstInt(t, value));

		return t;
	}
}
