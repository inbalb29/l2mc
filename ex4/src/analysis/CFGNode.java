package analysis;

import java.util.HashSet;
import java.util.Set;

import ir.*;

class CFGNode {
    // The actual instruction this node represents
    IrCommand command;

    // Links to neighbors
    Set<CFGNode> successors = new HashSet<>();
    Set<CFGNode> predecessors = new HashSet<>();

    CFGNode(IrCommand command) {
        this.command = command;
    }

    void addSuccessor(CFGNode succ) {
        this.successors.add(succ);
        succ.predecessors.add(this); // Maintain bidirectional links
    }

    @Override
    public String toString() {
        return "Node(" + (command != null ? command.toString() : "End") + ")";
    }
}