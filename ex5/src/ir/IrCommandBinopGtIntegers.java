package ir;

import mips.MipsGenerator;
import temp.Temp;

public class IrCommandBinopGtIntegers extends IrCommandBinop {
    public IrCommandBinopGtIntegers(Temp dst, Temp t1, Temp t2) {
        super(dst, t1, t2);
    }

    public void mipsMe() {
        // a > b is the same as b < a
        String labelEnd = getFreshLabel("end");
        String labelAssignOne = getFreshLabel("AssignOne");
        String labelAssignZero = getFreshLabel("AssignZero");
        MipsGenerator gen = MipsGenerator.getInstance();
        gen.blt(t2, t1, labelAssignOne);
        gen.bge(t2, t1, labelAssignZero);
        gen.label(labelAssignOne);
        gen.li(dst, 1);
        gen.jump(labelEnd);
        gen.label(labelAssignZero);
        gen.li(dst, 0);
        gen.jump(labelEnd);
        gen.label(labelEnd);
    }
}
