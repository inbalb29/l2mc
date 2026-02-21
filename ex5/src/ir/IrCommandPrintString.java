package ir;

import java.util.*;
import mips.MipsGenerator;
import temp.Temp;

public class IrCommandPrintString extends IrCommand {
    public Temp t;

    public IrCommandPrintString(Temp t) {
        this.t = t;
    }

    @Override
    public Set<Integer> getUsedTemps() {
        return Set.of(t.getSerialNumber());
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().printString(t);
    }
}
