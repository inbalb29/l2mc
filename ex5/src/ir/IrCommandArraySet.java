package ir;

import java.util.*;
import mips.MipsGenerator;
import temp.Temp;

public class IrCommandArraySet extends IrCommand {
    public Temp arr;
    public Temp idx;
    public Temp src;

    public IrCommandArraySet(Temp arr, Temp idx, Temp src) {
        this.arr = arr;
        this.idx = idx;
        this.src = src;
    }

    @Override
    public Set<Integer> getUsedTemps() {
        return Set.of(arr.getSerialNumber(), idx.getSerialNumber(), src.getSerialNumber());
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().arraySet(arr, idx, src);
    }
}
