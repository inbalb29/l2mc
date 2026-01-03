/***********/
/* PACKAGE */
/***********/
package ir;

/*******************/
/* GENERAL IMPORTS */
/*******************/

/*******************/
/* PROJECT IMPORTS */
/*******************/

public class IrCommandAllocate extends IrCommand {
	String varName;

	public IrCommandAllocate(String varName) {
		this.varName = varName;
	}

	public String getAllocatedVariable() {
		return varName;
	}
}
