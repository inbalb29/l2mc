package ir;

import java.util.*;
import mips.MipsGenerator;
import temp.Temp;

public class IrCommandFieldSet extends IrCommand {
    public Temp obj;
    public int fieldOffset;
    public Temp src;

    public IrCommandFieldSet(Temp obj, int fieldOffset, Temp src) {
        this.obj = obj;
        this.fieldOffset = fieldOffset;
        this.src = src;
    }

    @Override
    public Set<Integer> getUsedTemps() {
        return Set.of(obj.getSerialNumber(), src.getSerialNumber());
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().fieldSet(obj, fieldOffset, src);
    }
}
