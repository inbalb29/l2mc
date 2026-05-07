package ir;

import java.util.*;
import mips.MipsGenerator;
import temp.Temp;

public class IrCommandCallFunc extends IrCommand {
    public Temp dst; // null for void calls
    public String funcLabel;
    public List<Temp> args;

    public IrCommandCallFunc(Temp dst, String funcLabel, List<Temp> args) {
        this.dst = dst;
        this.funcLabel = funcLabel;
        this.args = args != null ? args : new ArrayList<>();
    }

    @Override
    public Set<Integer> getUsedTemps() {
        Set<Integer> used = new HashSet<>();
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
        MipsGenerator.getInstance().callFunc(dst, funcLabel, args);
    }
}
