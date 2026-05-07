package ir;

import java.util.*;
import mips.MipsGenerator;
import temp.Temp;

public class IrCommandStringEq extends IrCommand {
    public Temp dst;
    public Temp s1;
    public Temp s2;

    public IrCommandStringEq(Temp dst, Temp s1, Temp s2) {
        this.dst = dst;
        this.s1 = s1;
        this.s2 = s2;
    }

    @Override
    public Set<Integer> getUsedTemps() {
        return Set.of(s1.getSerialNumber(), s2.getSerialNumber());
    }

    @Override
    public Integer getDefinedTemp() {
        return dst.getSerialNumber();
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().stringEq(dst, s1, s2);
    }
}
