package ir;

import java.util.*;
import mips.MipsGenerator;
import temp.Temp;

public class IrCommandMalloc extends IrCommand {
    public Temp dst;
    public Temp size; // null if using constant size
    public int constSize; // used when size temp is null

    public IrCommandMalloc(Temp dst, Temp size) {
        this.dst = dst;
        this.size = size;
        this.constSize = 0;
    }

    public IrCommandMalloc(Temp dst, int constSize) {
        this.dst = dst;
        this.size = null;
        this.constSize = constSize;
    }

    @Override
    public Set<Integer> getUsedTemps() {
        if (size != null)
            return Set.of(size.getSerialNumber());
        return Collections.emptySet();
    }

    @Override
    public Integer getDefinedTemp() {
        return dst.getSerialNumber();
    }

    @Override
    public void mipsMe() {
        MipsGenerator gen = MipsGenerator.getInstance();
        if (size != null) {
            gen.mallocDynamic(dst, size);
        } else {
            gen.mallocConst(dst, constSize);
        }
    }
}
