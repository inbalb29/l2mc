package ast;

public class AstVarDec extends AstDec {
    public AstType type;
    public String name;
    public AstExp initExp; // Can be null if no initialization

    /******************/
    /* CONSTRUCTOR(S) */
    /******************/

    // Constructor without initialization
    public AstVarDec(AstType type, String name) {
        this(type, name, null);
    }

    // Constructor with initialization expression
    public AstVarDec(AstType type, String name, AstExp initExp) {
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
}
