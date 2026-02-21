package ir;

import java.util.*;
import mips.MipsGenerator;
import temp.Temp;

public class IrCommandFieldGet extends IrCommand {
    public Temp dst;
    public Temp obj;
    public int fieldOffset;

    public IrCommandFieldGet(Temp dst, Temp obj, int fieldOffset) {
        this.dst = dst;
        this.obj = obj;
        this.fieldOffset = fieldOffset;
    }

    @Override
    public Set<Integer> getUsedTemps() {
        return Set.of(obj.getSerialNumber());
    }

    @Override
    public Integer getDefinedTemp() {
        return dst.getSerialNumber();
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().fieldGet(dst, obj, fieldOffset);
    }
}
