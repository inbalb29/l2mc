package ir;

import mips.*;

public class IrCommandAllocate extends IrCommand {
	String varName;
	String initValue;

	public IrCommandAllocate(String varName) {
		this.varName = varName;
		this.initValue = null;
	}

	public IrCommandAllocate(String varName, int value) {
		this.varName = varName;
		this.initValue = String.valueOf(value);
	}

	public IrCommandAllocate(String varName, String labelRef) {
		this.varName = varName;
		this.initValue = labelRef;
	}

	public String getAllocatedVariable() {
		return varName;
	}

	public void mipsMe() {
		MipsGenerator gen = MipsGenerator.getInstance();
		if (initValue == null) {
			gen.allocate(varName);
		} else {
			try {
				int val = Integer.parseInt(initValue);
				gen.allocateWithInit(varName, val);
			} catch (NumberFormatException e) {
				gen.allocateWithLabel(varName, initValue);
			}
		}
	}
}
