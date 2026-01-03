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

public class IrCommandLabel extends IrCommand {
	String labelName;

	public IrCommandLabel(String labelName) {
		this.labelName = labelName;
	}

	@Override
	public boolean isLabel() {
		return true;
	}

	@Override
	public String getLabelName() {
		return labelName;
	}
}
