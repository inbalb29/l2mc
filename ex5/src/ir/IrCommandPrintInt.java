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

public class IrCommandPrintInt extends IrCommand {
	Temp t;

	public IrCommandPrintInt(Temp t) {
		this.t = t;
	}

	@Override
	public Set<Integer> getUsedTemps() {
		return Set.of(t.getSerialNumber());
	}

	/***************/
	/* MIPS me !!! */
	/***************/
	public void mipsMe() {
		MipsGenerator.getInstance().printInt(t);
	}
}
