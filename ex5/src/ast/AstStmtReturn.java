package ast;

import Exceptions.SemanticError;
import symboltable.SymbolTable;
import types.*;

public class AstStmtReturn extends AstStmt {
    public AstExp exp; // null for simple RETURN, non-null for RETURN exp

    /******************/
    /* CONSTRUCTOR(S) */
    /******************/
    public AstStmtReturn(AstExp exp, int lineNumber) {
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        if (exp != null) {
            System.out.print("====================== stmt -> RETURN exp SEMICOLON\n");
        } else {
            System.out.print("====================== stmt -> RETURN SEMICOLON\n");
        }

        /*******************************/
        /* COPY INPUT DATA MEMBERS ... */
        /*******************************/
        this.exp = exp;
        this.lineNumber = lineNumber;
    }

    /*********************************************************/
    /* The printing message for a return statement AST node */
    /*********************************************************/
    public void printMe() {
        /********************************************/
        /* AST NODE TYPE = AST RETURN STATEMENT */
        /********************************************/
        System.out.print("AST NODE RETURN STMT\n");

        /***********************************/
        /* RECURSIVELY PRINT EXP ... */
        /***********************************/
        if (exp != null)
            exp.printMe();

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                "RETURN");

        /****************************************/
        /* PRINT Edges to AST GRAPHVIZ DOT file */
        /****************************************/
        if (exp != null)
            AstGraphviz.getInstance().logEdge(serialNumber, exp.serialNumber);
    }

    @Override
    public Type semantMe() {
        TypeFunction currFunc = SymbolTable.getInstance().getCurrentFunction();
        if (currFunc == null) {
            throw new SemanticError("return statement outside function", lineNumber);
        }

        Type returnType = currFunc.returnType;

        if (returnType.isVoid()) {
            // Void function: return statement must be empty
            if (exp != null) {
                throw new SemanticError("void function cannot return a value", lineNumber);
            }
        } else {
            // Non-void function: must return a value of compatible type
            if (exp == null) {
                throw new SemanticError("non-void function must return a value", lineNumber);
            }

            Type expType = exp.semantMe();
            if (!isReturnCompatible(returnType, expType, exp)) {
                throw new SemanticError(
                        "return type mismatch: '" + expType.name + "' cannot be assigned to '" + returnType.name + "'",
                        lineNumber);
            }
        }

        return null;
    }

    private static boolean isReturnCompatible(Type returnType, Type expType, AstExp exp) {
        // Nil can be returned for array and class return types
        if (expType.isNil()) {
            return returnType.isArray() || returnType.isClass();
        }

        // Same type
        if (returnType == expType) {
            return true;
        }

        if (returnType.isClass() && expType.isClass()) {
            TypeClass returnClass = (TypeClass) returnType;
            TypeClass expClass = (TypeClass) expType;
            return expClass.isSubclassOf(returnClass);
        }

        return false;
    }
}
