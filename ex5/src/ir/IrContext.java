package ir;

import java.util.*;
import types.*;

public class IrContext {
    private static IrContext instance;

    private boolean inFunction = false;
    private String funcName;
    private boolean isMethod;
    private TypeClass classType;
    private String className;

    private Map<String, Integer> frameOffsets = new HashMap<>();
    private Set<String> globals = new HashSet<>();

    private int numParams;
    private int numLocals;
    private int nextLocalOffset;

    // Saved across function entries
    private Deque<FuncState> stateStack = new ArrayDeque<>();

    private static class FuncState {
        boolean inFunction;
        String funcName;
        boolean isMethod;
        TypeClass classType;
        String className;
        Map<String, Integer> frameOffsets;
        int numParams;
        int numLocals;
        int nextLocalOffset;
    }

    protected IrContext() {
    }

    public static IrContext getInstance() {
        if (instance == null) {
            instance = new IrContext();
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    public void addGlobal(String qualifiedName) {
        globals.add(qualifiedName);
    }

    public boolean isGlobal(String qualifiedName) {
        return globals.contains(qualifiedName);
    }

    public void enterFunction(String name, boolean isMethod, TypeClass cls) {
        // Save current state
        FuncState state = new FuncState();
        state.inFunction = this.inFunction;
        state.funcName = this.funcName;
        state.isMethod = this.isMethod;
        state.classType = this.classType;
        state.className = this.className;
        state.frameOffsets = this.frameOffsets;
        state.numParams = this.numParams;
        state.numLocals = this.numLocals;
        state.nextLocalOffset = this.nextLocalOffset;
        stateStack.push(state);

        this.inFunction = true;
        this.funcName = name;
        this.isMethod = isMethod;
        this.classType = cls;
        this.className = cls != null ? cls.name : null;
        this.frameOffsets = new HashMap<>();
        this.numParams = 0;
        this.numLocals = 0;
        this.nextLocalOffset = -44; // after saved $t0-$t9 at -4..-40
    }

    public void addParam(String qualifiedName) {
        int offset = 8 + numParams * 4;
        frameOffsets.put(qualifiedName, offset);
        numParams++;
    }

    public int addLocal(String qualifiedName) {
        int offset = nextLocalOffset;
        frameOffsets.put(qualifiedName, offset);
        numLocals++;
        nextLocalOffset -= 4;
        return offset;
    }

    public boolean hasFrameOffset(String qualifiedName) {
        return frameOffsets.containsKey(qualifiedName);
    }

    public int getFrameOffset(String qualifiedName) {
        return frameOffsets.get(qualifiedName);
    }

    public boolean isInFunction() {
        return inFunction;
    }

    public String getFuncName() {
        return funcName;
    }

    public boolean isMethod() {
        return isMethod;
    }

    public TypeClass getClassType() {
        return classType;
    }

    public String getClassName() {
        return className;
    }

    public int getNumParams() {
        return numParams;
    }

    public int getNumLocals() {
        return numLocals;
    }

    public int getThisParamOffset() {
        // 'this' is always the first parameter for methods
        return 8;
    }

    public void exitFunction() {
        FuncState state = stateStack.pop();
        this.inFunction = state.inFunction;
        this.funcName = state.funcName;
        this.isMethod = state.isMethod;
        this.classType = state.classType;
        this.className = state.className;
        this.frameOffsets = state.frameOffsets;
        this.numParams = state.numParams;
        this.numLocals = state.numLocals;
        this.nextLocalOffset = state.nextLocalOffset;
    }

    public String getFuncLabel() {
        if ("main".equals(funcName))
            return "user_main";
        if (isMethod && className != null)
            return "_" + className + "_" + funcName;
        return "_" + funcName;
    }
}
