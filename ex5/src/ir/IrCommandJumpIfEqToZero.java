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
import temp.*;
import mips.*;

import java.util.Set;

public class IrCommandJumpIfEqToZero extends IrCommand {
	Temp t;
	String labelName;

	public IrCommandJumpIfEqToZero(Temp t, String labelName) {
		this.t = t;
		this.labelName = labelName;
	}

	@Override
	public Set<Integer> getUsedTemps() {
		return Set.of(t.getSerialNumber());
	}

	@Override
	public boolean isConditionalJump() {
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
		MipsGenerator.getInstance().beqz(t, labelName);
	}
}
