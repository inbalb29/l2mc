package ast;

public class AstExpNil extends AstExp {
    /******************/
    /* CONSTRUCTOR(S) */
    /******************/
    public AstExpNil() {
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        System.out.print("====================== exp -> NIL\n");
    }

    /************************************************/
    /* The printing message for a nil exp AST node */
    /************************************************/
    public void printMe() {
        /*******************************/
        /* AST NODE TYPE = AST NIL EXP */
        /*******************************/
        System.out.print("AST NODE NIL\n");

        /*********************************/
        /* Print to AST GRAPHVIZ DOT file */
        /*********************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                "NIL");
    }
}
