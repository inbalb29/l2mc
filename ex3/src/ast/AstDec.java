package ast;

import types.*;

public abstract class AstDec extends AstStmt {
	/***********************************************/
	/* The default semantic action for an AST node */
	/***********************************************/
	public Type semantMe() {
		return null;
	}
}
