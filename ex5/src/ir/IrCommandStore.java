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

public class IrCommandStore extends IrCommand {
	public String varName;
	public Temp src;

	public IrCommandStore(String varName, Temp src) {
		this.src = src;
		this.varName = varName;
	}

	@Override
	public String getDefinedVariable() {
		return varName;
	}

	@Override
	public Set<Integer> getUsedTemps() {
		return Set.of(src.getSerialNumber());
	}

	/***************/
	/* MIPS me !!! */
	/***************/
	public void mipsMe() {
		MipsGenerator.getInstance().store(varName, src);
	}
}
