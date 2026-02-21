package ir;

import java.util.*;
import codegen.ClassLayout;
import mips.MipsGenerator;

public class Ir {
	private List<IrCommand> globalCommands = new ArrayList<>();
	private List<IrFunction> functions = new ArrayList<>();
	private IrFunction currentFunction = null;
	private boolean hasGlobalInit = false;

	public void setHasGlobalInit() {
		this.hasGlobalInit = true;
	}

	public void AddIrCommand(IrCommand cmd) {
		if (currentFunction != null) {
			currentFunction.addCommand(cmd);
		} else {
			globalCommands.add(cmd);
		}
	}

	public void beginFunction(String name, int numParams, int numLocals, boolean isMethod) {
		currentFunction = new IrFunction(name, numParams, numLocals, isMethod);
		functions.add(currentFunction);
	}

	public void endFunction() {
		currentFunction = null;
	}

	public IrFunction getCurrentFunction() {
		return currentFunction;
	}

	public List<IrFunction> getFunctions() {
		return functions;
	}

	public void allocateRegisters() {
		for (IrFunction func : functions) {
			func.allocateRegisters();
		}
	}

	public void mipsMe() {
		MipsGenerator gen = MipsGenerator.getInstance();

		// Generate global data commands (.data section)
		for (IrCommand cmd : globalCommands) {
			cmd.mipsMe();
		}

		// Emit string constants
		for (Map.Entry<String, String> entry : ClassLayout.getStringLiterals().entrySet()) {
			gen.allocateString(entry.getValue(), entry.getKey());
		}

		// Emit vtables
		for (ClassLayout layout : ClassLayout.getAllLayouts().values()) {
			if (!layout.vtable.isEmpty()) {
				List<String> labels = new ArrayList<>();
				for (ClassLayout.MethodInfo mi : layout.vtable) {
					labels.add(mi.implementationLabel);
				}
				gen.allocateVtable(layout.vtableLabel, labels);
			}
		}

		// Generate each function (.text section)
		for (IrFunction func : functions) {
			func.mipsMe();
		}

		// Generate main entry point and error handlers
		gen.mainEntryPoint(hasGlobalInit);
	}

	private static Ir instance = null;

	protected Ir() {
	}

	public static Ir getInstance() {
		if (instance == null) {
			instance = new Ir();
		}
		return instance;
	}

	public static void reset() {
		instance = null;
	}
}
