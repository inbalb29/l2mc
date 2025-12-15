package ast;

import Exceptions.SemanticError;
import symboltable.SymbolTable;
import types.*;

public class AstTypedef extends AstDec {
    public String name;
    public AstType elementType;

    /******************/
    /* CONSTRUCTOR(S) */
    /******************/
    public AstTypedef(String name, AstType elementType, int lineNumber) {
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        System.out.format("====================== arrayTypedef -> ARRAY ID(%s) EQ type LBRACK RBRACK SEMICOLON\n",
                name);

        /*******************************/
        /* COPY INPUT DATA MEMBERS ... */
        /*******************************/
        this.name = name;
        this.elementType = elementType;
        this.lineNumber = lineNumber;
    }

    /****************************************************/
    /* The printing message for an array typedef node */
    /****************************************************/
    public void printMe() {
        /****************************************/
        /* AST NODE TYPE = AST ARRAY TYPEDEF */
        /****************************************/
        System.out.format("AST NODE ARRAY TYPEDEF(%s)\n", name);

        /**************************************/
        /* RECURSIVELY PRINT ELEMENT TYPE ... */
        /**************************************/
        if (elementType != null)
            elementType.printMe();

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                String.format("ARRAY\nTYPEDEF\n%s", name));

        /****************************************/
        /* PRINT Edges to AST GRAPHVIZ DOT file */
        /****************************************/
        if (elementType != null)
            AstGraphviz.getInstance().logEdge(serialNumber, elementType.serialNumber);
    }

    @Override
    public Type semantMe() {
        /*********************************************/
        /* [1] Verify array type not already defined */
        /*********************************************/
        if (SymbolTable.getInstance().find(name) != null) {
            throw new SemanticError("array type'" + name + "' already exists", lineNumber);
        }

        /*************************************************/
        /* [2] Verify array type defined in global scope */
        /*************************************************/
        if (!SymbolTable.getInstance().inGlobalScope()) {
            throw new SemanticError("array type cannot be declared inside non global scope", lineNumber);
        }

        /***************************************************/
        /* [3] Create array type and enter to symbol table */
        /***************************************************/
        Type innerType = elementType.semantMe();
        if (innerType.isVoid()) {
            throw new SemanticError("array cannot have be of type void", lineNumber);
        }

        TypeArray t = new TypeArray(name, innerType);
        SymbolTable.getInstance().enter(name, t);

        /**************************************************************/
        /* [4] Return value is irrelevant for array type declarations */
        /**************************************************************/
        return null;
    }
}
