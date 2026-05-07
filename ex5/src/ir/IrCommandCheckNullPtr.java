package ir;

import java.util.*;
import mips.MipsGenerator;
import temp.Temp;

public class IrCommandCheckNullPtr extends IrCommand {
    public Temp ptr;

    public IrCommandCheckNullPtr(Temp ptr) {
        this.ptr = ptr;
    }

    @Override
    public Set<Integer> getUsedTemps() {
        return Set.of(ptr.getSerialNumber());
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().checkNullPtr(ptr);
    }
}
