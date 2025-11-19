package ast;

import java.util.List;

public class AstClassDec extends AstDec {
    public String name;
    public String parentName; // null if no extends
    public List<AstDec> fields;

    /******************/
    /* CONSTRUCTOR(S) */
    /******************/
    public AstClassDec(String name, String parentName, List<AstDec> fields) {
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        if (parentName != null) {
            System.out.format("====================== classDec -> CLASS ID(%s) EXTENDS ID(%s) LBRACE cFields RBRACE\n",
                    name, parentName);
        } else {
            System.out.format("====================== classDec -> CLASS ID(%s) LBRACE cFields RBRACE\n", name);
        }

        /*******************************/
        /* COPY INPUT DATA MEMBERS ... */
        /*******************************/
        this.name = name;
        this.parentName = parentName;
        this.fields = fields;
    }

    /******************************************************/
    /* The printing message for a class declaration node */
    /******************************************************/
    public void printMe() {
        /*******************************************/
        /* AST NODE TYPE = AST CLASS DECLARATION */
        /*******************************************/
        System.out.format("AST NODE CLASS DEC(%s)\n", name);
        if (parentName != null) {
            System.out.format("  EXTENDS: %s\n", parentName);
        }

        /**************************************/
        /* RECURSIVELY PRINT FIELDS ... */
        /**************************************/
        for (AstDec field : fields) {
            if (field != null)
                field.printMe();
        }

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        String label = parentName != null
                ? String.format("CLASS\n%s\nEXTENDS\n%s", name, parentName)
                : String.format("CLASS\n%s", name);
        AstGraphviz.getInstance().logNode(serialNumber, label);

        /****************************************/
        /* PRINT Edges to AST GRAPHVIZ DOT file */
        /****************************************/
        for (AstDec field : fields) {
            if (field != null)
                AstGraphviz.getInstance().logEdge(serialNumber, field.serialNumber);
        }
    }
}
