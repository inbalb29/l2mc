package ast;

import java.util.List;
import types.*;

public class AstProg extends AstNode {
    public List<AstDec> decs;

    public AstProg(AstDec dec, int lineNumber) {
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        System.out.print("====================== program -> dec\n");
        /*******************************/
        /* COPY INPUT DATA MEMBERS ... */
        /*******************************/
        decs = new java.util.ArrayList<AstDec>();
        decs.add(dec);
        this.decs = decs;
        this.lineNumber = lineNumber;
    }

    public void addDeclaration(AstDec dec) {
        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        System.out.print("====================== program -> program dec\n");

        this.decs.add(dec);
    }

    /***********************************************/
    /* The printing message for a program AST node */
    /***********************************************/
    public void printMe() {
        /*********************************/
        /* AST NODE TYPE = AST PROGRAM */
        /*********************************/
        System.out.print("AST NODE PROGRAM\n");

        /**************************************/
        /* PRINT ALL THE DECLARATIONS INSIDE IT */
        /**************************************/
        for (AstDec dec : decs) {
            if (dec != null)
                dec.printMe();
        }

        /*********************************/
        /* PRINT TO AST GRAPHVIZ DOT FILE */
        /*********************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                "PROGRAM");
        for (AstDec dec : decs) {
            if (dec != null)
                AstGraphviz.getInstance().logEdge(serialNumber, dec.serialNumber);
        }
    }

    @Override
    public Type semantMe() {
        for (AstDec dec : decs) {
            if (dec != null) {
                dec.semantMe();
            }
        }

        return null;
    }
}