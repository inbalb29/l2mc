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

public class IrCommandJumpLabel extends IrCommand {
	String labelName;

	public IrCommandJumpLabel(String labelName) {
		this.labelName = labelName;
	}

	@Override
	public boolean isUnconditionalJump() {
		return true;
	}

	@Override
	public String getJumpTarget() {
		return labelName;
	}
}
