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
import temp.*;

public class IrCommandLoad extends IrCommand {
	public Temp dst;
	public String varName;

	public IrCommandLoad(Temp dst, String varName) {
		this.dst = dst;
		this.varName = varName;
	}

	@Override
	public Set<String> getUsedVariables() {
		return Collections.singleton(varName);
	}
}
