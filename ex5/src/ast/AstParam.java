package ast;

public class AstParam extends AstNode {
    public AstType type;
    public String name;

    /******************/
    /* CONSTRUCTOR(S) */
    /******************/
    public AstParam(AstType type, String name, int lineNumber) {
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        System.out.format("====================== param -> type ID(%s)\n", name);

        /*******************************/
        /* COPY INPUT DATA MEMBERS ... */
        /*******************************/
        this.type = type;
        this.name = name;
        this.lineNumber = lineNumber;
    }

    /************************************************/
    /* The printing message for a param AST node */
    /************************************************/
    public void printMe() {
        /********************************/
        /* AST NODE TYPE = AST PARAM */
        /********************************/
        System.out.format("AST NODE PARAM(%s)\n", name);

        /**************************************/
        /* RECURSIVELY PRINT TYPE ... */
        /**************************************/
        if (type != null)
            type.printMe();

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                String.format("PARAM\n%s", name));

        /****************************************/
        /* PRINT Edges to AST GRAPHVIZ DOT file */
        /****************************************/
        if (type != null)
            AstGraphviz.getInstance().logEdge(serialNumber, type.serialNumber);
    }
}