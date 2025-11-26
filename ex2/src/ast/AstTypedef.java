package ast;

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
}
