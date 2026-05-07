package ir;

import java.util.*;
import codegen.RegisterAllocator;
import mips.MipsGenerator;

public class IrFunction {
    public String name;
    public int numParams;
    public int numLocals;
    public boolean isMethod;
    public List<IrCommand> commands = new ArrayList<>();
    public Map<Integer, Integer> registerAllocation;

    public IrFunction(String name, int numParams, int numLocals, boolean isMethod) {
        this.name = name;
        this.numParams = numParams;
        this.numLocals = numLocals;
        this.isMethod = isMethod;
    }

    public void addCommand(IrCommand cmd) {
        commands.add(cmd);
    }

    public void setNumLocals(int numLocals) {
        this.numLocals = numLocals;
    }

    public void allocateRegisters() {
        registerAllocation = RegisterAllocator.allocate(commands);
    }

    public void mipsMe() {
        MipsGenerator gen = MipsGenerator.getInstance();

        // Set register mapping
        Map<Integer, String> tempMap = new HashMap<>();
        if (registerAllocation != null) {
            for (Map.Entry<Integer, Integer> entry : registerAllocation.entrySet()) {
                tempMap.put(entry.getKey(), "$t" + entry.getValue());
            }
        }
        gen.setTempRegisterMap(tempMap);

        // Generate prologue
        gen.functionPrologue(name, numLocals);

        // Generate body
        for (IrCommand cmd : commands) {
            cmd.mipsMe();
        }

        // Generate epilogue
        gen.functionEpilogue(name);
    }
}
