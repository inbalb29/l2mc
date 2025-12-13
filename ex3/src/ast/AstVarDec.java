package ast;

import symboltable.SymbolTable;
import types.*;
import Exceptions.SemanticError;

public class AstVarDec extends AstDec {
    public AstType type;
    public String name;
    public AstExp initExp; // Can be null if no initialization

    /******************/
    /* CONSTRUCTOR(S) */
    /******************/

    // Constructor without initialization
    public AstVarDec(AstType type, String name, int lineNumber) {
        this(type, name, null, lineNumber);
    }

    // Constructor with initialization expression
    public AstVarDec(AstType type, String name, AstExp initExp, int lineNumber) {
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        if (initExp != null) {
            System.out.format("====================== varDec -> type ID(%s) ASSIGN exp SEMICOLON\n", name);
        } else {
            System.out.format("====================== varDec -> type ID(%s) SEMICOLON\n", name);
        }

        /*******************************/
        /* COPY INPUT DATA MEMBERS ... */
        /*******************************/
        this.type = type;
        this.name = name;
        this.initExp = initExp;
        this.lineNumber = lineNumber;
    }

    /****************************************************/
    /* The printing message for a var declaration node */
    /****************************************************/
    public void printMe() {
        /****************************************/
        /* AST NODE TYPE = AST VAR DECLARATION */
        /****************************************/
        System.out.format("AST NODE VAR DEC(%s)\n", name);

        /**************************************/
        /* RECURSIVELY PRINT TYPE + INIT ... */
        /**************************************/
        if (type != null)
            type.printMe();
        if (initExp != null)
            initExp.printMe();

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                String.format("VAR\nDEC\n%s", name));

        /****************************************/
        /* PRINT Edges to AST GRAPHVIZ DOT file */
        /****************************************/
        if (type != null)
            AstGraphviz.getInstance().logEdge(serialNumber, type.serialNumber);
        if (initExp != null)
            AstGraphviz.getInstance().logEdge(serialNumber, initExp.serialNumber);
    }

    public Type semantMe() throws SemanticError {
        Type t;
        Type initType;

        /****************************/
        /* [1] Check If Type exists */
        /****************************/
        t = SymbolTable.getInstance().find(type.name);
        if (t == null) {
            throw new SemanticError("non existing type '" + type.name + "'\n", lineNumber);
        }

        /**************************************/
        /* [2] Check That Name does NOT exist */
        /**************************************/
        if (SymbolTable.getInstance().find(name) != null) {
            throw new SemanticError("variable '" + name + "' already exists in scope\n", lineNumber);
        }

        initType = initExp.semantMe();
        if (initType != t) {
            throw new SemanticError(
                    "expression type '" + initType.name + "' does not match variable type '" + t.name + "'\n",
                    lineNumber);
        }

        /************************************************/
        /* [3] Enter the Identifier to the Symbol Table */
        /************************************************/
        SymbolTable.getInstance().enter(name, t);

        /************************************************************/
        /* [4] Return value is irrelevant for variable declarations */
        /************************************************************/
        return null;
    }
}
