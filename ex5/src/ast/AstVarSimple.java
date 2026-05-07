package ast;

import Exceptions.SemanticError;
import codegen.ClassLayout;
import ir.*;
import symboltable.SymbolTable;
import temp.Temp;
import temp.TempFactory;
import types.*;

public class AstVarSimple extends AstVar {
	/************************/
	/* simple variable name */
	/************************/
	public String name;
	public String qualifiedName; // Set during semantMe for IR generation
	public boolean isClassField = false; // Set during semantMe for IR generation

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstVarSimple(String name, int lineNumber) {
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.format("====================== var -> ID( %s )\n", name);

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.name = name;
		this.lineNumber = lineNumber;
	}

	/**************************************************/
	/* The printing message for a simple var AST node */
	/**************************************************/
	public void printMe() {
		/**********************************/
		/* AST NODE TYPE = AST SIMPLE VAR */
		/**********************************/
		System.out.format("AST NODE SIMPLE VAR( %s )\n", name);

		/*********************************/
		/* Print to AST GRAPHVIZ DOT file */
		/*********************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
				String.format("SIMPLE\nVAR\n(%s)", name));
	}

	@Override
	public Type semantMe() {
		Type t = null;

		// Look up the variable using normal symbol table lookup
		// This finds local variables, parameters, and outer scope variables
		t = SymbolTable.getInstance().find(name);

		// If we're in a class context, class fields can shadow global variables
		// but not local variables. We need to check if what we found is at global scope
		// vs. a local variable. If it's global, class field takes precedence.
		TypeClass currentClass = SymbolTable.getInstance().getCurrentClass();
		if (currentClass != null) {
			boolean foundInLocalScope = SymbolTable.getInstance().existsInFunctionScope(name);
			if (!foundInLocalScope) {
				Type classFieldType = currentClass.findMember(name);
				if (classFieldType != null) {
					// The symbol table result is either null or at global scope
					// Class field takes precedence
					t = classFieldType;
					if (classFieldType instanceof TypeClassField) {
						this.isClassField = true;
					}
				}
			}
		}

		if (t == null) {
			throw new SemanticError("variable '" + name + "' is undefined", lineNumber);
		}

		// Store qualified name for IR generation
		this.qualifiedName = SymbolTable.getInstance().findQualifiedName(name);

		// If we got a TypeClassField, return its actual type
		if (t instanceof TypeClassField) {
			return ((TypeClassField) t).fieldType;
		}

		return t;
	}

	public Temp irMe() {
		Temp t = TempFactory.getInstance().getFreshTemp();
		IrContext ctx = IrContext.getInstance();

		if (ctx.isInFunction() && ctx.hasFrameOffset(qualifiedName)) {
			// Local or parameter variable
			Ir.getInstance().AddIrCommand(new IrCommandLoadFrame(t, ctx.getFrameOffset(qualifiedName)));
		} else if (isClassField && ctx.isMethod()) {
			// Implicit class field access: load this, then field get
			ClassLayout layout = ClassLayout.getLayout(ctx.getClassName());
			int fieldOffset = layout.getFieldOffset(name);
			Temp thisTemp = TempFactory.getInstance().getFreshTemp();
			Ir.getInstance().AddIrCommand(new IrCommandLoadFrame(thisTemp, ctx.getThisParamOffset()));
			Ir.getInstance().AddIrCommand(new IrCommandFieldGet(t, thisTemp, fieldOffset));
		} else {
			// Global variable
			Ir.getInstance().AddIrCommand(new IrCommandLoad(t, qualifiedName));
		}
		return t;
	}
}
