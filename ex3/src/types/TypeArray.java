package types;

public class TypeArray extends Type {
    public Type innerType;

    public TypeArray(String name, Type innerType) {
        this.name = name;
        this.innerType = innerType;
    }

    @Override
    public boolean isArray() {
        return true;
    }
}
