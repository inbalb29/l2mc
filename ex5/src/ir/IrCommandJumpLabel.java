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
import mips.*;

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

	/***************/
	/* MIPS me !!! */
	/***************/
	public void mipsMe() {
		MipsGenerator.getInstance().jump(labelName);
	}
}
