package ir;

import java.util.*;
import mips.MipsGenerator;
import temp.Temp;

public class IrCommandZeroInitArray extends IrCommand {
    public Temp arr;
    public Temp size;

    public IrCommandZeroInitArray(Temp arr, Temp size) {
        this.arr = arr;
        this.size = size;
    }

    @Override
    public Set<Integer> getUsedTemps() {
        return Set.of(arr.getSerialNumber(), size.getSerialNumber());
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().zeroInitArray(arr, size);
    }
}
