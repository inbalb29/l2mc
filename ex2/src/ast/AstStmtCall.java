package ast;

public class AstStmtCall extends AstStmt {
    public AstCallExp callExp;

    /******************/
    /* CONSTRUCTOR(S) */
    /******************/
    public AstStmtCall(AstCallExp callExp, int lineNumber) {
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        System.out.print("====================== stmt -> callExp SEMICOLON\n");

        /*******************************/
        /* COPY INPUT DATA MEMBERS ... */
        /*******************************/
        this.callExp = callExp;
        this.lineNumber = lineNumber;
    }

    /******************************************************/
    /* The printing message for a call statement AST node */
    /******************************************************/
    public void printMe() {
        /******************************************/
        /* AST NODE TYPE = AST CALL STATEMENT */
        /******************************************/
        System.out.print("AST NODE CALL STMT\n");

        /***********************************/
        /* RECURSIVELY PRINT CALLEXP ... */
        /***********************************/
        if (callExp != null)
            callExp.printMe();

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                "CALL\nSTMT");

        /****************************************/
        /* PRINT Edges to AST GRAPHVIZ DOT file */
        /****************************************/
        if (callExp != null)
            AstGraphviz.getInstance().logEdge(serialNumber, callExp.serialNumber);
    }
}
