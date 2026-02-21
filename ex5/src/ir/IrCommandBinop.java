package ir;

import java.util.Set;
import temp.*;

public abstract class IrCommandBinop extends IrCommand {
    public Temp t1;
    public Temp t2;
    public Temp dst;

    public IrCommandBinop(Temp dst, Temp t1, Temp t2) {
        this.dst = dst;
        this.t1 = t1;
        this.t2 = t2;
    }

    @Override
    public Set<Integer> getUsedTemps() {
        return Set.of(t1.getSerialNumber(), t2.getSerialNumber());
    }

    @Override
    public Integer getDefinedTemp() {
        return dst.getSerialNumber();
    }
}
