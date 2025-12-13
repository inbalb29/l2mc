package ast;

import java.util.List;

import symboltable.SymbolTable;
import types.*;
import Exceptions.SemanticError;

public class AstFuncDec extends AstDec {
    public AstType returnType;
    public String name;
    public List<AstParam> params;
    public List<AstStmt> body;

    /******************/
    /* CONSTRUCTOR(S) */
    /******************/
    public AstFuncDec(AstType returnType, String name, List<AstParam> params, List<AstStmt> body, int lineNumber) {
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        System.out.format("====================== funcDec -> type ID(%s) LPAREN params RPAREN LBRACE stmtList RBRACE\n",
                name);

        /*******************************/
        /* COPY INPUT DATA MEMBERS ... */
        /*******************************/
        this.returnType = returnType;
        this.name = name;
        this.params = params;
        this.body = body;
        this.lineNumber = lineNumber;
    }

    /*****************************************************/
    /* The printing message for a func declaration node */
    /*****************************************************/
    public void printMe() {
        /******************************************/
        /* AST NODE TYPE = AST FUNC DECLARATION */
        /******************************************/
        System.out.format("AST NODE FUNC DEC(%s)\n", name);

        /**************************************/
        /* RECURSIVELY PRINT TYPE + PARAMS ... */
        /**************************************/
        if (returnType != null)
            returnType.printMe();
        for (AstParam param : params) {
            if (param != null)
                param.printMe();
        }
        for (AstStmt stmt : body) {
            if (stmt != null)
                stmt.printMe();
        }

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                String.format("FUNC\nDEC\n%s", name));

        /****************************************/
        /* PRINT Edges to AST GRAPHVIZ DOT file */
        /****************************************/
        if (returnType != null)
            AstGraphviz.getInstance().logEdge(serialNumber, returnType.serialNumber);
        for (AstParam param : params) {
            if (param != null)
                AstGraphviz.getInstance().logEdge(serialNumber, param.serialNumber);
        }
        for (AstStmt stmt : body) {
            if (stmt != null)
                AstGraphviz.getInstance().logEdge(serialNumber, stmt.serialNumber);
        }
    }

    public Type semantMe() throws SemanticError {
        Type returnType = null;
        List<Type> paramTypeList = new java.util.ArrayList<Type>();

        /*******************/
        /* [0] return type */
        /*******************/
        returnType = SymbolTable.getInstance().find(this.returnType.name);
        if (returnType == null) {
            throw new SemanticError("non existing return type '" + this.returnType.name + "' for function '" +
                    name + "'\n", lineNumber);
        }

        /****************************/
        /* [1] Begin Function Scope */
        /****************************/
        SymbolTable.getInstance().beginScope();

        /***************************/
        /* [2] Semant Input Params */
        /***************************/
        for (AstParam param : params) {
            Type paramType = param.semantMe();

            if (paramType == null) {
                throw new SemanticError("non existing type '" + param.type.name +
                        "' in function declaration\n", lineNumber);
            } else {
                paramTypeList.add(paramType);
                SymbolTable.getInstance().enter(param.name, paramType);
            }
        }

        /*******************/
        /* [3] Semant Body */
        /*******************/
        for (AstStmt stmt : body) {
            stmt.semantMe();
        }

        /*****************/
        /* [4] End Scope */
        /*****************/
        SymbolTable.getInstance().endScope();

        /***************************************************/
        /* [5] Enter the Function Type to the Symbol Table */
        /***************************************************/
        SymbolTable.getInstance().enter(name, new TypeFunction(returnType, name, paramTypeList));

        /************************************************************/
        /* [6] Return value is irrelevant for function declarations */
        /************************************************************/
        return null;
    }
}
