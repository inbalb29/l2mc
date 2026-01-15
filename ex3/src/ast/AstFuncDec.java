package ast;

import java.util.ArrayList;
import java.util.List;

import Exceptions.SemanticError;
import symboltable.SymbolTable;
import types.*;

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

    /**
     * Build the function signature (return type + parameters)
     * without analyzing the body.
     * This allows methods to be referenced before their bodies are analyzed.
     */
    public TypeFunction buildSignature() {
        /* [1] Check if function name already exists in global scope */
        if (SymbolTable.getInstance().inGlobalScope() && SymbolTable.getInstance().existsInCurrentScope(name)) {
            throw new SemanticError("function '" + name + "' already exists", lineNumber);
        }

        /* [2] Check return type exists */
        Type retType = SymbolTable.getInstance().find(returnType.name);
        if (retType == null) {
            throw new SemanticError("return type '" + returnType.name + "' does not exist", lineNumber);
        }

        /* [3] Build parameter type list and check them */
        List<Type> paramTypes = new ArrayList<>();
        for (AstParam param : params) {
            Type paramType = SymbolTable.getInstance().find(param.type.name);
            if (paramType == null) {
                throw new SemanticError("parameter type '" + param.type.name + "' does not exist", lineNumber);
            }
            if (paramType.isVoid()) {
                throw new SemanticError("parameter type cannot be void", lineNumber);
            }
            paramTypes.add(paramType);
        }

        /* [4] Create function type */
        return new TypeFunction(retType, name, paramTypes);
    }

    /**
     * Analyze the function body. Should be called after buildSignature().
     */
    public void semantBody(TypeFunction funcType) {
        /* [1] Begin function scope */
        SymbolTable.getInstance().beginScope();
        SymbolTable.getInstance().setCurrentFunction(funcType);

        /* [2] Enter parameters into scope */
        for (int i = 0; i < params.size(); i++) {
            AstParam param = params.get(i);
            if (SymbolTable.getInstance().existsInCurrentScope(param.name)) {
                throw new SemanticError("duplicate parameter name '" + param.name + "'", lineNumber);
            }
            SymbolTable.getInstance().enter(param.name, funcType.params.get(i));
        }

        /* [3] Semant function body */
        for (AstStmt stmt : body) {
            if (stmt != null) {
                stmt.semantMe();
            }
        }

        /* [4] End Scope */
        SymbolTable.getInstance().setCurrentFunction(null);
        SymbolTable.getInstance().endScope();
    }

    @Override
    public Type semantMe() {
        TypeFunction funcType = buildSignature();

        // Methods are entered into the symbol table when their class is declared
        if (SymbolTable.getInstance().inGlobalScope()) {
            SymbolTable.getInstance().enter(name, funcType);
        }

        semantBody(funcType);

        return funcType;
    }
}
