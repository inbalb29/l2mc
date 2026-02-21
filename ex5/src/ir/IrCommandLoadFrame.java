package ir;

import mips.MipsGenerator;
import temp.Temp;

public class IrCommandLoadFrame extends IrCommand {
    public Temp dst;
    public int offset;

    public IrCommandLoadFrame(Temp dst, int offset) {
        this.dst = dst;
        this.offset = offset;
    }

    @Override
    public Integer getDefinedTemp() {
        return dst.getSerialNumber();
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().loadFrame(dst, offset);
    }
}
