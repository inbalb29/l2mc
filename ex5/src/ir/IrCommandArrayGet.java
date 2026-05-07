package ir;

import java.util.*;
import mips.MipsGenerator;
import temp.Temp;

public class IrCommandArrayGet extends IrCommand {
    public Temp dst;
    public Temp arr;
    public Temp idx;

    public IrCommandArrayGet(Temp dst, Temp arr, Temp idx) {
        this.dst = dst;
        this.arr = arr;
        this.idx = idx;
    }

    @Override
    public Set<Integer> getUsedTemps() {
        return Set.of(arr.getSerialNumber(), idx.getSerialNumber());
    }

    @Override
    public Integer getDefinedTemp() {
        return dst.getSerialNumber();
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().arrayGet(dst, arr, idx);
    }
}
