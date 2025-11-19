package ast;

import java.util.List;

public class AstFuncDec extends AstDec {
    public AstType returnType;
    public String name;
    public List<AstParam> params;
    public List<AstStmt> body;

    /******************/
    /* CONSTRUCTOR(S) */
    /******************/
    public AstFuncDec(AstType returnType, String name, List<AstParam> params, List<AstStmt> body) {
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
}
