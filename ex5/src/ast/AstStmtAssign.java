package ast;

import Exceptions.SemanticError;
import codegen.ClassLayout;
import ir.*;
import temp.Temp;
import temp.TempFactory;
import types.*;

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

	@Override
	public Type semantMe() {
		Type varType = null;
		Type expType = null;

		if (var != null) {
			varType = var.semantMe();
		}
		if (exp != null) {
			expType = exp.semantMe();
		}

		if (varType == null) {
			throw new SemanticError("assignment to undefined variable", lineNumber);
		}

		if (expType == null) {
			throw new SemanticError("invalid expression in assignment", lineNumber);
		}

		if (!AstVarDec.isAssignmentCompatible(varType, expType, exp)) {
			throw new SemanticError(
					"type mismatch: '" + expType.name + "' cannot be assigned to '" + varType.name + "'", lineNumber);
		}

		return null;
	}

	public Temp irMe() {
		if (var instanceof AstVarSimple simple) {
			Temp src = exp.irMe();
			assignToSimple(simple, src);
		} else if (var instanceof AstVarField field) {
			// Evaluate LHS object before RHS (left-to-right evaluation)
			Temp objTemp = field.var.irMe();
			Ir.getInstance().AddIrCommand(new IrCommandCheckNullPtr(objTemp));
			Temp src = exp.irMe();
			ClassLayout layout = ClassLayout.getLayout(field.resolvedClass.name);
			int fieldOffset = layout.getFieldOffset(field.fieldName);
			Ir.getInstance().AddIrCommand(new IrCommandFieldSet(objTemp, fieldOffset, src));
		} else if (var instanceof AstVarSubscript subscript) {
			// Evaluate LHS array and index before RHS (left-to-right evaluation)
			Temp arrTemp = subscript.var.irMe();
			Ir.getInstance().AddIrCommand(new IrCommandCheckNullPtr(arrTemp));
			Temp idxTemp = subscript.subscript.irMe();
			Temp src = exp.irMe();
			Ir.getInstance().AddIrCommand(new IrCommandArraySet(arrTemp, idxTemp, src));
		}

		return null;
	}

	private void assignToSimple(AstVarSimple simple, Temp src) {
		IrContext ctx = IrContext.getInstance();
		if (ctx.isInFunction() && ctx.hasFrameOffset(simple.qualifiedName)) {
			Ir.getInstance().AddIrCommand(
					new IrCommandStoreFrame(ctx.getFrameOffset(simple.qualifiedName), src));
		} else if (simple.isClassField && ctx.isMethod()) {
			ClassLayout layout = ClassLayout.getLayout(ctx.getClassName());
			int fieldOffset = layout.getFieldOffset(simple.name);
			Temp thisTemp = TempFactory.getInstance().getFreshTemp();
			Ir.getInstance().AddIrCommand(new IrCommandLoadFrame(thisTemp, ctx.getThisParamOffset()));
			Ir.getInstance().AddIrCommand(new IrCommandFieldSet(thisTemp, fieldOffset, src));
		} else {
			Ir.getInstance().AddIrCommand(new IrCommandStore(simple.qualifiedName, src));
		}
	}
}
