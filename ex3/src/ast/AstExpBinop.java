package ast;

import Exceptions.SemanticError;
import types.*;

public class AstExpBinop extends AstExp {
	public BinOp op;
	public AstExp left;
	public AstExp right;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpBinop(AstExp left, AstExp right, BinOp op, int lineNumber) {
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/***************************************/
		/* PRINT CORRESPONDING DERIVATION RULE */
		/***************************************/
		System.out.print("====================== exp -> exp BINOP exp\n");

		/*******************************/
		/* COPY INPUT DATA MEMBERS ... */
		/*******************************/
		this.left = left;
		this.right = right;
		this.op = op;
		this.lineNumber = lineNumber;
	}

	/*************************************************/
	/* The printing message for a binop exp AST node */
	/*************************************************/
	public void printMe() {
		/*************************************/
		/* AST NODE TYPE = AST BINOP EXP */
		/*************************************/
		System.out.print("AST NODE BINOP EXP\n");

		/**************************************/
		/* RECURSIVELY PRINT left + right ... */
		/**************************************/
		if (left != null)
			left.printMe();
		if (right != null)
			right.printMe();

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
				String.format("BINOP(%c)", op.getSymbol()));

		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (left != null)
			AstGraphviz.getInstance().logEdge(serialNumber, left.serialNumber);
		if (right != null)
			AstGraphviz.getInstance().logEdge(serialNumber, right.serialNumber);
	}

	@Override
	public Type semantMe() {
		Type t1 = null;
		Type t2 = null;

		if (left != null) {
			t1 = left.semantMe();
		}
		if (right != null) {
			t2 = right.semantMe();
		}

		if (t1 == null || t2 == null) {
			throw new SemanticError("invalid operand types in binary operation", lineNumber);
		}

		switch (op) {
			case PLUS:
				// + can be int + int or string + string
				if (t1 == TypeInt.getInstance() && t2 == TypeInt.getInstance()) {
					return TypeInt.getInstance();
				}
				if (t1 == TypeString.getInstance() && t2 == TypeString.getInstance()) {
					return TypeString.getInstance();
				}
				throw new SemanticError("invalid operand type for + operator", lineNumber);
			case MINUS:
			case TIMES:
			case LT:
			case GT:
				// Only int operands allowed
				if (t1 == TypeInt.getInstance() && t2 == TypeInt.getInstance()) {
					return TypeInt.getInstance();
				}
				throw new SemanticError("invalid operand type for " + op.getSymbol() + " operator", lineNumber);
			case DIVIDE:
				// Only int operands allowed, and right operand cannot be constant zero
				if (t1 != TypeInt.getInstance() || t2 != TypeInt.getInstance()) {
					throw new SemanticError("invalid operand type for / operator", lineNumber);
				}
				if (right instanceof AstExpInt) {
					int divisor = ((AstExpInt) right).value;
					if (divisor == 0) {
						throw new SemanticError("division by zero", lineNumber);
					}
				}
				return TypeInt.getInstance();
			case EQ:
				// Equality testing
				return checkEqualityCompatibility(t1, t2);
			default:
				throw new SemanticError("unknown binary operator", lineNumber);
		}
	}

	/**
	 * Check if two types can be compared for equality
	 */
	private Type checkEqualityCompatibility(Type t1, Type t2) {
		// nil can be compared with arrays and classes
		if (t1 == TypeNil.getInstance()) {
			if (t2.isArray() || t2.isClass()) {
				return TypeInt.getInstance();
			}
			throw new SemanticError("cannot compare nil with non-array/non-class type", lineNumber);
		}
		if (t2 == TypeNil.getInstance()) {
			if (t1.isArray() || t1.isClass()) {
				return TypeInt.getInstance();
			}
			throw new SemanticError("cannot compare nil with non-array/non-class type", lineNumber);
		}

		// Primitive types: must be exactly same type
		if (t1 == TypeInt.getInstance() || t1 == TypeString.getInstance()) {
			if (t1 == t2) {
				return TypeInt.getInstance();
			}
			throw new SemanticError("type mismatch in equality comparison", lineNumber);
		}

		// Arrays: must be exactly same array type
		if (t1.isArray() && t2.isArray()) {
			if (t1 == t2) {
				return TypeInt.getInstance();
			}
			throw new SemanticError("array types are not comparable", lineNumber);
		}

		// Classes: can compare if one is subclass of the other
		if (t1.isClass() && t2.isClass()) {
			TypeClass c1 = (TypeClass) t1;
			TypeClass c2 = (TypeClass) t2;
			if (c1.isSubClassOf(c2) || c2.isSubClassOf(c1)) {
				return TypeInt.getInstance();
			}
			throw new SemanticError("class types are not comparable", lineNumber);
		}

		throw new SemanticError("type mismatch in equality comparison", lineNumber);
	}
}
