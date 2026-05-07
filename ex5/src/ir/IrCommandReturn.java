package ir;

import java.util.*;
import mips.MipsGenerator;
import temp.Temp;

public class IrCommandReturn extends IrCommand {
    public Temp value;
    public String epilogueLabel;

    public IrCommandReturn(Temp value) {
        this.value = value;
        this.epilogueLabel = IrContext.getInstance().getFuncLabel() + "_epilogue";
    }

    @Override
    public Set<Integer> getUsedTemps() {
        if (value != null)
            return Set.of(value.getSerialNumber());
        return Collections.emptySet();
    }

    @Override
    public boolean isUnconditionalJump() {
        return true;
    }

    @Override
    public String getJumpTarget() {
        return epilogueLabel;
    }

    @Override
    public void mipsMe() {
        if (value != null) {
            MipsGenerator.getInstance().moveToReturn(value);
        }
        MipsGenerator.getInstance().jump(epilogueLabel);
    }
}
