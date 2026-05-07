package ir;

import java.util.*;
import mips.MipsGenerator;
import temp.Temp;

public class IrCommandVirtualCall extends IrCommand {
    public Temp dst; // null for void calls
    public Temp obj; // object reference
    public int vtableOffset; // byte offset in vtable
    public List<Temp> args; // explicit args (obj is implicitly first param)

    public IrCommandVirtualCall(Temp dst, Temp obj, int vtableOffset, List<Temp> args) {
        this.dst = dst;
        this.obj = obj;
        this.vtableOffset = vtableOffset;
        this.args = args != null ? args : new ArrayList<>();
    }

    @Override
    public Set<Integer> getUsedTemps() {
        Set<Integer> used = new HashSet<>();
        used.add(obj.getSerialNumber());
        for (Temp a : args)
            used.add(a.getSerialNumber());
        return used;
    }

    @Override
    public Integer getDefinedTemp() {
        return dst != null ? dst.getSerialNumber() : null;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().virtualCall(dst, obj, vtableOffset, args);
    }
}
