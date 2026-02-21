package ir;

import java.util.*;
import mips.MipsGenerator;
import temp.Temp;

public class IrCommandStoreFrame extends IrCommand {
    public int offset;
    public Temp src;

    public IrCommandStoreFrame(int offset, Temp src) {
        this.offset = offset;
        this.src = src;
    }

    @Override
    public Set<Integer> getUsedTemps() {
        return Set.of(src.getSerialNumber());
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().storeFrame(offset, src);
    }
}
