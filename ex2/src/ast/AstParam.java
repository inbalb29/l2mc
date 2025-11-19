
package ast;

public class AstParam extends AstNode {
    public final AstType type;
    public final ID id;

    public AstParam(AstType type, String id) {
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        /*******************************/
        /* COPY INPUT DATA MEMBERS ... */
        /*******************************/
        this.type = type;
        this.id = id;
    }
}