package codegen;

import java.util.*;
import types.*;

public class ClassLayout {
    public String className;
    public ClassLayout parent;

    public List<FieldInfo> fields = new ArrayList<>();
    public Map<String, Integer> fieldOffsets = new LinkedHashMap<>();
    public int objectSize;

    public List<MethodInfo> vtable = new ArrayList<>();
    public Map<String, Integer> methodVtableIndices = new LinkedHashMap<>();
    public String vtableLabel;

    public static class FieldInfo {
        public String name;
        public int offset;
        public Type type;
        public Object initValue; // Integer, String, or null (nil/uninitialized)

        public FieldInfo(String name, int offset, Type type, Object initValue) {
            this.name = name;
            this.offset = offset;
            this.type = type;
            this.initValue = initValue;
        }
    }

    public static class MethodInfo {
        public String name;
        public String implementationLabel;

        public MethodInfo(String name, String implementationLabel) {
            this.name = name;
            this.implementationLabel = implementationLabel;
        }
    }

    private static Map<String, ClassLayout> layouts = new LinkedHashMap<>();
    private static int stringLabelCounter = 0;
    private static Map<String, String> stringLiterals = new LinkedHashMap<>();

    public static void reset() {
        layouts.clear();
        stringLabelCounter = 0;
        stringLiterals.clear();
    }

    public static ClassLayout getLayout(String className) {
        return layouts.get(className);
    }

    public static Map<String, ClassLayout> getAllLayouts() {
        return layouts;
    }

    public static String getOrCreateStringLabel(String value) {
        if (stringLiterals.containsKey(value)) {
            return stringLiterals.get(value);
        }
        String label = "string_const_" + stringLabelCounter++;
        stringLiterals.put(value, label);
        return label;
    }

    public static Map<String, String> getStringLiterals() {
        return stringLiterals;
    }

    public static ClassLayout computeLayout(TypeClass tc, Map<String, List<FieldInitInfo>> classFieldInits) {
        if (layouts.containsKey(tc.name)) {
            return layouts.get(tc.name);
        }

        ClassLayout layout = new ClassLayout();
        layout.className = tc.name;
        layout.vtableLabel = "vtable_" + tc.name;

        if (tc.father != null) {
            ClassLayout parentLayout = computeLayout(tc.father, classFieldInits);
            layout.parent = parentLayout;
            layout.fields = new ArrayList<>(parentLayout.fields);
            layout.fieldOffsets = new LinkedHashMap<>(parentLayout.fieldOffsets);
            layout.objectSize = parentLayout.objectSize;
            layout.vtable = new ArrayList<>(parentLayout.vtable);
            layout.methodVtableIndices = new LinkedHashMap<>(parentLayout.methodVtableIndices);
        } else {
            layout.objectSize = 4; // vtable pointer at offset 0
        }

        for (Type member : tc.dataMembers) {
            if (member instanceof TypeClassField tcf) {
                FieldInfo fi = new FieldInfo(tcf.name, layout.objectSize, tcf.fieldType, null);
                // Look up init value
                List<FieldInitInfo> inits = classFieldInits.get(tc.name);
                if (inits != null) {
                    for (FieldInitInfo init : inits) {
                        if (init.name.equals(tcf.name)) {
                            fi.initValue = init.value;
                            break;
                        }
                    }
                }
                layout.fields.add(fi);
                layout.fieldOffsets.put(tcf.name, fi.offset);
                layout.objectSize += 4;
            } else if (member instanceof TypeFunction tf) {
                String implLabel = tc.name + "_" + tf.name;
                if (layout.methodVtableIndices.containsKey(tf.name)) {
                    int idx = layout.methodVtableIndices.get(tf.name);
                    layout.vtable.set(idx, new MethodInfo(tf.name, implLabel));
                } else {
                    int idx = layout.vtable.size();
                    layout.vtable.add(new MethodInfo(tf.name, implLabel));
                    layout.methodVtableIndices.put(tf.name, idx);
                }
            }
        }

        layouts.put(tc.name, layout);
        return layout;
    }

    public int getFieldOffset(String fieldName) {
        Integer offset = fieldOffsets.get(fieldName);
        if (offset != null)
            return offset;
        if (parent != null)
            return parent.getFieldOffset(fieldName);
        return -1;
    }

    public int getMethodVtableIndex(String methodName) {
        Integer idx = methodVtableIndices.get(methodName);
        if (idx != null)
            return idx;
        if (parent != null)
            return parent.getMethodVtableIndex(methodName);
        return -1;
    }

    public static class FieldInitInfo {
        public String name;
        public Object value; // Integer, String, or null

        public FieldInitInfo(String name, Object value) {
            this.name = name;
            this.value = value;
        }
    }
}
