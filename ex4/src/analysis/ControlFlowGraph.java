package analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ir.*;

class ControlFlowGraph {
    List<CFGNode> nodes = new ArrayList<>();
    Map<String, Integer> labelToIndex = new HashMap<>();
    Set<String> allVariables = new HashSet<>();

    CFGNode entryNode;
    CFGNode exitNode;

    private Map<String, CFGNode> labelToNodeMap = new HashMap<>();

    void build(List<IrCommand> commands) {
        // Implementation for building the control flow graph from instructions
        for (IrCommand cmd : commands) {
            CFGNode node = new CFGNode(cmd);
            nodes.add(node);

            // If this command is a Label, register it so we can jump to it later
            if (cmd instanceof IrCommandLabel) {
                IrCommandLabel labelCmd = (IrCommandLabel) cmd;
                // Logic to register label commands
                labelToNodeMap.put(labelCmd.getLabelName(), node);
            }
        }

        for (int i = 0; i < nodes.size(); i++) {
            CFGNode curr = nodes.get(i);
            CFGNode next = (i + 1 < nodes.size()) ? nodes.get(i + 1) : null;
            IrCommand cmd = curr.command;

            // CASE A: Unconditional Jump (goto L)
            if (cmd instanceof IrCommandJumpLabel) {
                String targetLabel = ((IrCommandJumpLabel) cmd).getLabelName();
                CFGNode targetNode = labelToNodeMap.get(targetLabel);
                if (targetNode != null) {
                    curr.addSuccessor(targetNode);
                }
                // Note: No edge to 'next' because we forced a jump
            }
            // CASE B: Conditional Jump (if x goto L)
            else if (cmd instanceof IrCommandJumpIfEqToZero) {
                // Connect to label Node for the case in which branch taken:
                String targetLabel = ((IrCommandJumpIfEqToZero) cmd).getLabelName();
                CFGNode targetNode = labelToNodeMap.get(targetLabel);
                if (targetNode != null) {
                    curr.addSuccessor(targetNode);
                }
                // Connect to sequential node for case in which branch not taken (fall-through):
                if (next != null) {
                    curr.addSuccessor(next);
                }
            }
            // CASE C: Sequential Instruction (fall-through)
            else {
                if (next != null) {
                    curr.addSuccessor(next);
                }
            }
        }

    }

}