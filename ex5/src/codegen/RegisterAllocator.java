package codegen;

import java.util.*;
import ir.*;

public class RegisterAllocator {
    private static final int NUM_REGISTERS = 10; // $t0 - $t9

    public static Map<Integer, Integer> allocate(List<IrCommand> commands) {
        Set<Integer> allTemps = collectTemps(commands);
        if (allTemps.isEmpty()) {
            return new HashMap<>();
        }

        // Build liveness info
        Map<Integer, Set<Integer>> liveOut = computeLiveness(commands, allTemps);

        // Build interference graph
        Map<Integer, Set<Integer>> interference = buildInterferenceGraph(commands, liveOut);

        // Graph coloring by simplification
        return colorGraph(interference, allTemps);
    }

    private static Set<Integer> collectTemps(List<IrCommand> commands) {
        Set<Integer> temps = new HashSet<>();
        for (IrCommand cmd : commands) {
            temps.addAll(cmd.getUsedTemps());
            Integer def = cmd.getDefinedTemp();
            if (def != null)
                temps.add(def);
        }
        return temps;
    }

    private static Map<Integer, Set<Integer>> computeLiveness(List<IrCommand> commands, Set<Integer> allTemps) {
        int n = commands.size();
        if (n == 0)
            return new HashMap<>();

        // Build CFG
        Map<String, Integer> labelToIndex = new HashMap<>();
        for (int i = 0; i < n; i++) {
            IrCommand cmd = commands.get(i);
            if (cmd.isLabel()) {
                labelToIndex.put(cmd.getLabelName(), i);
            }
        }

        List<Set<Integer>> successors = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Set<Integer> succs = new HashSet<>();
            IrCommand cmd = commands.get(i);
            if (cmd.isUnconditionalJump()) {
                Integer target = labelToIndex.get(cmd.getJumpTarget());
                if (target != null)
                    succs.add(target);
            } else if (cmd.isConditionalJump()) {
                if (i + 1 < n)
                    succs.add(i + 1);
                Integer target = labelToIndex.get(cmd.getJumpTarget());
                if (target != null)
                    succs.add(target);
            } else {
                if (i + 1 < n)
                    succs.add(i + 1);
            }
            successors.add(succs);
        }

        // Iterative liveness analysis
        Set<Integer>[] liveIn = new Set[n];
        Set<Integer>[] liveOutArr = new Set[n];
        for (int i = 0; i < n; i++) {
            liveIn[i] = new HashSet<>();
            liveOutArr[i] = new HashSet<>();
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = n - 1; i >= 0; i--) {
                IrCommand cmd = commands.get(i);
                Set<Integer> newLiveOut = new HashSet<>();
                for (int succ : successors.get(i)) {
                    newLiveOut.addAll(liveIn[succ]);
                }

                Set<Integer> newLiveIn = new HashSet<>(newLiveOut);
                Integer def = cmd.getDefinedTemp();
                if (def != null)
                    newLiveIn.remove(def);
                newLiveIn.addAll(cmd.getUsedTemps());

                if (!newLiveIn.equals(liveIn[i]) || !newLiveOut.equals(liveOutArr[i])) {
                    liveIn[i] = newLiveIn;
                    liveOutArr[i] = newLiveOut;
                    changed = true;
                }
            }
        }

        Map<Integer, Set<Integer>> result = new HashMap<>();
        for (int i = 0; i < n; i++) {
            result.put(i, liveOutArr[i]);
        }
        return result;
    }

    private static Map<Integer, Set<Integer>> buildInterferenceGraph(
            List<IrCommand> commands, Map<Integer, Set<Integer>> liveOut) {
        Map<Integer, Set<Integer>> graph = new HashMap<>();

        // Initialize nodes
        for (IrCommand cmd : commands) {
            for (int t : cmd.getUsedTemps())
                graph.computeIfAbsent(t, k -> new HashSet<>());
            Integer def = cmd.getDefinedTemp();
            if (def != null)
                graph.computeIfAbsent(def, k -> new HashSet<>());
        }

        // Add edges: if temp t is defined at command i, then t interferes with all
        // temps in liveOut[i] (except t itself)
        for (int i = 0; i < commands.size(); i++) {
            Integer def = commands.get(i).getDefinedTemp();
            if (def != null) {
                Set<Integer> live = liveOut.get(i);
                if (live != null) {
                    for (int t : live) {
                        if (t != def) {
                            graph.computeIfAbsent(def, k -> new HashSet<>()).add(t);
                            graph.computeIfAbsent(t, k -> new HashSet<>()).add(def);
                        }
                    }
                }
            }
        }

        return graph;
    }

    private static Map<Integer, Integer> colorGraph(Map<Integer, Set<Integer>> graph, Set<Integer> allTemps) {
        // Make a mutable copy
        Map<Integer, Set<Integer>> adjLists = new HashMap<>();
        for (int t : allTemps) {
            adjLists.put(t, new HashSet<>(graph.getOrDefault(t, Collections.emptySet())));
        }

        // Simplification: push nodes with degree < NUM_REGISTERS onto stack
        Deque<Integer> stack = new ArrayDeque<>();
        Set<Integer> removed = new HashSet<>();

        while (removed.size() < allTemps.size()) {
            boolean found = false;
            for (int t : allTemps) {
                if (removed.contains(t))
                    continue;
                // Count non-removed neighbors
                int degree = 0;
                for (int neighbor : adjLists.get(t)) {
                    if (!removed.contains(neighbor))
                        degree++;
                }
                if (degree < NUM_REGISTERS) {
                    stack.push(t);
                    removed.add(t);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new RegisterAllocationFailed();
            }
        }

        // Pop and assign colors
        Map<Integer, Integer> coloring = new HashMap<>();
        while (!stack.isEmpty()) {
            int t = stack.pop();
            Set<Integer> usedColors = new HashSet<>();
            for (int neighbor : graph.getOrDefault(t, Collections.emptySet())) {
                if (coloring.containsKey(neighbor)) {
                    usedColors.add(coloring.get(neighbor));
                }
            }
            // Find smallest available color
            for (int color = 0; color < NUM_REGISTERS; color++) {
                if (!usedColors.contains(color)) {
                    coloring.put(t, color);
                    break;
                }
            }
        }

        return coloring;
    }
}
