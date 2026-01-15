package analysis;

import java.util.HashSet;
import java.util.Set;

import ir.IrCommand;

/**
 * CFG node representing a single IR instruction.
 */
class CFGNode {
    IrCommand command;
    Set<Integer> predecessors = new HashSet<>();
    Set<Integer> successors = new HashSet<>();

    Set<String> initOut = new HashSet<>();
    Set<Integer> taintOut = new HashSet<>();

    CFGNode(IrCommand command) {
        this.command = command;
    }
}
