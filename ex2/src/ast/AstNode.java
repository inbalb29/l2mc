package ast;

public abstract class AstNode {
	/*******************************************/
	/* The serial number is for debug purposes */
	/* In particular, it can help in creating */
	/* a graphviz dot format of the AST ... */
	/*******************************************/
	public int serialNumber;
	public int lineNumber = -1;

	/***********************************************/
	/* The default message for an unknown AST node */
	/***********************************************/
	public void printMe() {
		System.out.print("AST NODE UNKNOWN\n");
	}
}
