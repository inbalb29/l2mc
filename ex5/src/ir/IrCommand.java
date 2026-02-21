/***********/
/* PACKAGE */
/***********/
package ir;

import java.util.Collections;
import java.util.Set;

/*******************/
/* GENERAL IMPORTS */
/*******************/

/*******************/
/* PROJECT IMPORTS */
/*******************/

public abstract class IrCommand {
	/*****************/
	/* Label Factory */
	/*****************/
	protected static int labelCounter = 0;

	public static String getFreshLabel(String msg) {
		return String.format("Label_%d_%s", labelCounter++, msg);
	}

	/**
	 * Set of variable names read by this command.
	 * (RHS)
	 */
	public Set<String> getUsedVariables() {
		return Collections.emptySet();
	}

	/**
	 * Variable name that is defined (written to) by this command.
	 * (LHS)
	 */
	public String getDefinedVariable() {
		return null;
	}

	public boolean isConditionalJump() {
		return false;
	}

	public boolean isUnconditionalJump() {
		return false;
	}

	public String getJumpTarget() {
		return null;
	}

	public boolean isLabel() {
		return false;
	}

	public String getLabelName() {
		return null;
	}
}
