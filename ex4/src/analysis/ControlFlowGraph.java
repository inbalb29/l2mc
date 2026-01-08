package analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ir.IrCommand;
import ir.IrCommandAllocate;

class ControlFlowGraph {

    List<CFGNode> nodes = new ArrayList<>();
    Map<String, Integer> labelToIndex = new HashMap<>();
    Set<String> allVariables = new HashSet<>();

    ControlFlowGraph(List<IrCommand> commands) {
        build(commands);
    }

    /**
     * Build the control flow graph from IR commands.
     */
    private void build(List<IrCommand> commands) {
        // Create nodes for each command
        for (int i = 0; i < commands.size(); i++) {
            IrCommand cmd = commands.get(i);
            CFGNode node = new CFGNode(cmd);
            nodes.add(node);

            // Track labels for jump resolution
            if (cmd.isLabel()) {
                labelToIndex.put(cmd.getLabelName(), i);
            }

            // Track all variables
            if (cmd instanceof IrCommandAllocate) {
                allVariables.add(((IrCommandAllocate) cmd).getAllocatedVariable());
            }
            if (cmd.getDefinedVariable() != null) {
                allVariables.add(cmd.getDefinedVariable());
            }
        }

        // Build edges between nodes
        for (int i = 0; i < nodes.size(); i++) {
            CFGNode node = nodes.get(i);
            IrCommand cmd = node.command;

            if (cmd.isUnconditionalJump()) {
                String target = cmd.getJumpTarget();
                Integer targetIdx = labelToIndex.get(target);
                if (targetIdx != null) {
                    addEdge(i, targetIdx);
                }
            } else if (cmd.isConditionalJump()) {
                if (i + 1 < nodes.size()) {
                    addEdge(i, i + 1);
                }
                String target = cmd.getJumpTarget();
                Integer targetIdx = labelToIndex.get(target);
                if (targetIdx != null) {
                    addEdge(i, targetIdx);
                }
            } else {
                if (i + 1 < nodes.size()) {
                    addEdge(i, i + 1);
                }
            }
        }
    }

    private void addEdge(int from, int to) {
        nodes.get(from).successors.add(to);
        nodes.get(to).predecessors.add(from);
    }
}
