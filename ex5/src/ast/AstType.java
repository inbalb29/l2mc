package ast;

import Exceptions.SemanticError;
import symboltable.SymbolTable;
import types.*;

public class AstType extends AstNode {
    public String name;
    public boolean isPrimitive;

    /******************/
    /* CONSTRUCTOR(S) */
    /******************/
    public AstType(String name, boolean isPrimitive, int lineNumber) {
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        System.out.format("====================== type -> %s\n", name);

        /*******************************/
        /* COPY INPUT DATA MEMBERS ... */
        /*******************************/
        this.name = name;
        this.isPrimitive = isPrimitive;
        this.lineNumber = lineNumber;
    }

    // Constructor for user-defined types (class names)
    public AstType(String name, int lineNumber) {
        this(name, false, lineNumber);
    }

    // Factory methods for primitive types
    public static AstType INT(int lineNumber) {
        return new AstType("int", true, lineNumber);
    }

    public static AstType STRING(int lineNumber) {
        return new AstType("string", true, lineNumber);
    }

    public static AstType VOID(int lineNumber) {
        return new AstType("void", true, lineNumber);
    }

    /*********************************************/
    /* The printing message for a type AST node */
    /*********************************************/
    public void printMe() {
        /********************************/
        /* AST NODE TYPE = AST TYPE */
        /********************************/
        System.out.format("AST NODE TYPE(%s)\n", name);

        /*********************************/
        /* Print to AST GRAPHVIZ DOT file */
        /*********************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                String.format("TYPE\n%s", name));
    }

    @Override
    public Type semantMe() {
        if (isPrimitive == true) {
            if (name.equals("int")) {
                return TypeInt.getInstance();
            }
            if (name.equals("string")) {
                return TypeString.getInstance();
            }
            if (name.equals("void")) {
                return TypeVoid.getInstance();
            }
        }

        Type t = SymbolTable.getInstance().find(name);
        if (t == null) {
            throw new SemanticError("type '" + name + "' does not exist", lineNumber);
        }

        if (!t.isClass() && !t.isArray()) {
            throw new SemanticError("type '" + name + "' is not a valid type", lineNumber);
        }

        return t;
    }
}
