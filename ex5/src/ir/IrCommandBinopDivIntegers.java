package ir;

import mips.MipsGenerator;
import temp.Temp;

public class IrCommandBinopDivIntegers extends IrCommandBinop {
    public IrCommandBinopDivIntegers(Temp dst, Temp t1, Temp t2) {
        super(dst, t1, t2);
    }

    public void mipsMe() {
        MipsGenerator.getInstance().divFloor(dst, t1, t2);
    }
}
