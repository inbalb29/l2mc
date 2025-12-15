package ast;

import types.*;
import symboltable.SymbolTable;
import Exceptions.SemanticError;

public class AstNewExp extends AstExp {
    public AstType type;
    public AstExp sizeExp; // null for simple NEW type, non-null for NEW type[size]

    /******************/
    /* CONSTRUCTOR(S) */
    /******************/
    public AstNewExp(AstType type, AstExp sizeExp, int lineNumber) {
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        if (sizeExp != null) {
            System.out.print("====================== newExp -> NEW type LBRACK exp RBRACK\n");
        } else {
            System.out.print("====================== newExp -> NEW type\n");
        }

        /*******************************/
        /* COPY INPUT DATA MEMBERS ... */
        /*******************************/
        this.type = type;
        this.sizeExp = sizeExp;
        this.lineNumber = lineNumber;
    }

    /************************************************/
    /* The printing message for a new exp AST node */
    /************************************************/
    public void printMe() {
        /*******************************/
        /* AST NODE TYPE = AST NEW EXP */
        /*******************************/
        System.out.print("AST NODE NEW EXP\n");

        /**************************************/
        /* RECURSIVELY PRINT TYPE + SIZE ... */
        /**************************************/
        if (type != null)
            type.printMe();
        if (sizeExp != null)
            sizeExp.printMe();

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                "NEW\nEXP");

        /****************************************/
        /* PRINT Edges to AST GRAPHVIZ DOT file */
        /****************************************/
        if (type != null)
            AstGraphviz.getInstance().logEdge(serialNumber, type.serialNumber);
        if (sizeExp != null)
            AstGraphviz.getInstance().logEdge(serialNumber, sizeExp.serialNumber);
    }

    public Type semantMe() {
        Type t = type.semantMe();

        if (sizeExp != null) {
            /* Array allocation: new T[e] */
            if (!t.isArray()) {
                throw new SemanticError("cannot allocate non-array type '" + type.name + "' with new[]", lineNumber);
            }

            Type sizeType = sizeExp.semantMe();
            if (sizeType != TypeInt.getInstance()) {
                throw new SemanticError("array size must be of type int", lineNumber);
            }

            if (sizeExp instanceof AstExpInt size && size.value <= 0) {
                throw new SemanticError("array size must be greater than 0", lineNumber);
            }
        } else {
            /* Object allocation: new T */
            if (!t.isClass()) {
                throw new SemanticError("cannot allocate non-class type '" + type.name + "' with new", lineNumber);
            }
        }

        return t;
    }
}
