package ast;

import java.util.List;

public class AstCallExp extends AstExp {
    public AstVar object; // null for simple function calls, non-null for method calls
    public String funcName;
    public List<AstExp> args;

    /******************/
    /* CONSTRUCTOR(S) */
    /******************/
    public AstCallExp(AstVar object, String funcName, List<AstExp> args, int lineNumber) {
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        if (object != null) {
            System.out.format("====================== callExp -> var DOT ID(%s) LPAREN args RPAREN\n", funcName);
        } else {
            System.out.format("====================== callExp -> ID(%s) LPAREN args RPAREN\n", funcName);
        }

        /*******************************/
        /* COPY INPUT DATA MEMBERS ... */
        /*******************************/
        this.object = object;
        this.funcName = funcName;
        this.args = args;
        this.lineNumber = lineNumber;
    }

    /*************************************************/
    /* The printing message for a call exp AST node */
    /*************************************************/
    public void printMe() {
        /********************************/
        /* AST NODE TYPE = AST CALL EXP */
        /********************************/
        System.out.format("AST NODE CALL EXP(%s)\n", funcName);

        /******************************************/
        /* RECURSIVELY PRINT OBJECT + ARGS ... */
        /******************************************/
        if (object != null)
            object.printMe();
        for (AstExp arg : args) {
            if (arg != null)
                arg.printMe();
        }

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                String.format("CALL\n%s", funcName));

        /****************************************/
        /* PRINT Edges to AST GRAPHVIZ DOT file */
        /****************************************/
        if (object != null)
            AstGraphviz.getInstance().logEdge(serialNumber, object.serialNumber);
        for (AstExp arg : args) {
            if (arg != null)
                AstGraphviz.getInstance().logEdge(serialNumber, arg.serialNumber);
        }
    }
}
