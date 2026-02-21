package analysis;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import ir.*;

/**
 * Uninitialized variable analysis using chaotic iteration.
 * 
 * Tracks which variables are initialized with valid values
 * at each program point. A variable is considered initialized only if:
 * 1. It has been assigned a constant value, or
 * 2. It has been assigned from variables that are all initialized
 * 
 * If there's any path where a variable might be uninitialized when accessed, we
 * report it.
 */
public class DataFlowAnalysis {

    private ControlFlowGraph cfg;
    private Set<String> uninitializedAccesses = new TreeSet<>(); // TreeSet for sorted output

    /**
     * Extract the original variable name from a qualified name.
     * Original variable modified during IrMe with "@<number>" suffix to denote
     * scope level.
     */
    private String getOriginalName(String qualifiedName) {
        int lastUnderscore = qualifiedName.lastIndexOf("@");
        if (lastUnderscore > 0) {
            return qualifiedName.substring(0, lastUnderscore);
        }
        return qualifiedName;
    }

    /**
     * Run the analysis on the given IR commands.
     * Returns the set of variable names that are accessed before initialization.
     */
    public Set<String> analyze(List<IrCommand> commands) {
        if (commands.isEmpty()) {
            return Collections.emptySet();
        }

        // Build the CFG
        cfg = new ControlFlowGraph(commands);

        // Run combined chaotic iteration
        runAnalysis();

        return uninitializedAccesses;
    }

    /**
     * Run combined initialization and taint analysis.
     * 
     * A variable is initialized only if it's been assigned a value that doesn't
     * depend on any uninitialized variables.
     * 
     * initIn: variables that are definitely initialized with valid values
     * taintIn: temps that carry data derived from uninitialized variables
     * 
     * For MUST analysis, we initialize all out sets to the universal set except
     * the entry node. The intersection at merge points then captures the MUST
     * property.
     */
    private void runAnalysis() {
        // Initialize all out sets to universal set
        for (int i = 1; i < cfg.nodes.size(); i++) {
            cfg.nodes.get(i).initOut = new HashSet<>(cfg.allVariables);
        }

        // Queue of nodes to process
        Queue<Integer> processQueue = new LinkedList<>();
        for (int i = 0; i < cfg.nodes.size(); i++) {
            processQueue.add(i);
        }

        while (!processQueue.isEmpty()) {
            int idx = processQueue.poll();
            CFGNode node = cfg.nodes.get(idx);
            IrCommand cmd = node.command;

            // initIn := intersection of predecessors' initOut (MUST analysis)
            Set<String> initIn;
            // taintIn := union of predecessors' taintOut (MAY analysis)
            Set<Integer> taintIn;

            if (node.predecessors.isEmpty()) {
                initIn = new HashSet<>();
                taintIn = new HashSet<>();
            } else {
                initIn = null;
                taintIn = new HashSet<>();
                boolean first = true;
                for (int predIdx : node.predecessors) {
                    CFGNode pred = cfg.nodes.get(predIdx);
                    if (first) {
                        initIn = new HashSet<>(pred.initOut);
                        first = false;
                    } else {
                        // Intersection
                        initIn.retainAll(pred.initOut);
                    }
                    // Union
                    taintIn.addAll(pred.taintOut);
                }
                if (initIn == null) {
                    initIn = new HashSet<>();
                }
            }

            // Compute out sets using transfer function below
            Set<String> newInitOut = new HashSet<>(initIn);
            Set<Integer> newTaintOut = new HashSet<>(taintIn);

            // Process the command
            if (cmd instanceof IrCommandLoad load) {
                // If we're loading from an uninitialized variable, report the load
                // as uninitialized access, and taint the destination temp.
                // Otherwise, the destination temp is clean.
                if (!initIn.contains(load.varName)) {
                    uninitializedAccesses.add(getOriginalName(load.varName));
                    newTaintOut.add(load.dst.getSerialNumber());
                } else {
                    newTaintOut.remove(load.dst.getSerialNumber());
                }
            } else if (cmd instanceof IRcommandConstInt constInt) {
                // Constants are always clean
                newTaintOut.remove(constInt.t.getSerialNumber());
            } else if (cmd instanceof IrCommandStore store) {
                // If the source temp is tainted, the variable being stored to
                // is not properly initialized (we report when it's read).
                // Otherwise, the variable is now initialized with a clean value.
                if (taintIn.contains(store.src.getSerialNumber())) {
                    newInitOut.remove(store.varName);
                } else {
                    newInitOut.add(store.varName);
                }
            } else if (cmd instanceof IrCommandBinop binop) {
                // If either operand is tainted, the result temp is tainted.
                // Otherwise, the result temp is clean.
                if (taintIn.contains(binop.t1.getSerialNumber()) ||
                        taintIn.contains(binop.t2.getSerialNumber())) {
                    newTaintOut.add(binop.dst.getSerialNumber());
                } else {
                    newTaintOut.remove(binop.dst.getSerialNumber());
                }
            }

            // Check if anything changed
            boolean changed = !newInitOut.equals(node.initOut) || !newTaintOut.equals(node.taintOut);

            node.initOut = newInitOut;
            node.taintOut = newTaintOut;

            if (changed) {
                // Chaotic iteration: reprocess successors
                for (int succIdx : node.successors) {
                    processQueue.add(succIdx);
                }
            }
        }
    }
}