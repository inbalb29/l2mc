package ast;

public class AstType extends AstNode {
    public String name;
    public boolean isPrimitive;

    /******************/
    /* CONSTRUCTOR(S) */
    /******************/
    public AstType(String name, boolean isPrimitive) {
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        System.out.format("====================== type -> %s\n", name);

        /*******************************/
        /* COPY INPUT DATA MEMBERS ... */
        /*******************************/
        this.name = name;
        this.isPrimitive = isPrimitive;
    }

    // Constructor for user-defined types (class names)
    public AstType(String name) {
        this(name, false);
    }

    // Factory methods for primitive types
    public static AstType INT() {
        return new AstType("int", true);
    }

    public static AstType STRING() {
        return new AstType("string", true);
    }

    public static AstType VOID() {
        return new AstType("void", true);
    }

    /*********************************************/
    /* The printing message for a type AST node */
    /*********************************************/
    public void printMe() {
        /********************************/
        /* AST NODE TYPE = AST TYPE */
        /********************************/
        System.out.format("AST NODE TYPE(%s)\n", name);

        /*********************************/
        /* Print to AST GRAPHVIZ DOT file */
        /*********************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                String.format("TYPE\n%s", name));
    }
}
