package ir;

import mips.MipsGenerator;
import temp.Temp;

public class IrCommandLoadAddress extends IrCommand {
    public Temp dst;
    public String label;

    public IrCommandLoadAddress(Temp dst, String label) {
        this.dst = dst;
        this.label = label;
    }

    @Override
    public Integer getDefinedTemp() {
        return dst.getSerialNumber();
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().loadAddress(dst, label);
    }
}
