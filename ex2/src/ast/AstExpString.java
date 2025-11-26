package ast;

public class AstExpString extends AstExp {
    public String value;

    /******************/
    /* CONSTRUCTOR(S) */
    /******************/
    public AstExpString(String value, int lineNumber) {
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        System.out.format("====================== exp -> STRING(\"%s\")\n", value);

        /*******************************/
        /* COPY INPUT DATA MEMBERS ... */
        /*******************************/
        this.value = value;
        this.lineNumber = lineNumber;
    }

    /**************************************************/
    /* The printing message for a string exp AST node */
    /**************************************************/
    public void printMe() {
        /*********************************/
        /* AST NODE TYPE = AST STRING EXP */
        /*********************************/
        System.out.format("AST NODE STRING(\"%s\")\n", value);

        /*********************************/
        /* Print to AST GRAPHVIZ DOT file */
        /*********************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                String.format("STRING\n\"%s\"", value));
    }
}
