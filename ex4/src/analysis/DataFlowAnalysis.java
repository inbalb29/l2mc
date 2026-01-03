package analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public abstract class DataFlowAnalysis {
    protected ControlFlowGraph cfg;

    // The "Lattice": Maps every node to a Set of Strings (variable names)
    // inSet(CFGNode, i.e. Instruction): Variables guaranteed to be initialized
    // BEFORE the instruction
    // outSet(CFGNode, i.e. Instruction): Variables guaranteed to be initialized
    // AFTER the instruction
    private Map<CFGNode, Set<String>> inSets = new HashMap<>();
    private Map<CFGNode, Set<String>> outSets = new HashMap<>();

    // Corresponds to "Top" element of our lattice: The set of ALL variables in the
    // program.
    // We start assuming everything is initialized (optimistic) and whittle it down.
    // See slides 13 and 18 of recitation 8 for specific mentions of this.
    private Set<String> allVariables;

    public DataFlowAnalysis(ControlFlowGraph cfg) {
        this.cfg = cfg;
    }

    // See slide 18 of recitation 8 for reference, Need to initialize ⊔=∩ (Join =
    // Intersection) with the maximal set, because we need all paths to initialize a
    // variable in order for it to be considered initialized at that point.
    // Set Entry Node to Empty (Bottom) -> Nothing is initialized at start of main.
    // Set All Other Nodes to All Variables (Top) -> Optimistic starting assumption.
    private void initialization() {
        for (CFGNode node : cfg.nodes) {
            // Initialize empty
            inSets.put(node, new HashSet<>());

            if (node == cfg.entryNode) {
                // "The entry labels need special initialization" (Slide 18)
                // At the start of main, nothing is initialized.
                outSets.put(node, new HashSet<>());
            } else {
                // "For Join = Intersection, initialize with the maximal set" (Slide 18)
                // We use Intersection because we need a "Must" analysis (Slide 30).
                outSets.put(node, new HashSet<>(allVariables));
            }
        }
    }

    // Create transfer function based on FORWARD directional analysis as we need to
    // check
    // value of current command based on previous commands, i.e. information flows
    // forward.
    // TODO: Implement Gen and Kill sets based on Section 3 of the assignment
    // handout
    // and what recitation defines.
    private Set<String> transferFunction(CFGNode node, Set<String> inset) {
        Set<String> outsetRes = new HashSet<>(inset);
        return outsetRes;
    }

    // TODO: Implement Chaotic Iterations algorithm. Doesn't appear concretely in
    // recitation, ask gemini or find resource.

}