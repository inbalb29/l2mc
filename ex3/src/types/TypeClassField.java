package types;

public class TypeClassField extends Type {
	public Type fieldType;

	public TypeClassField(String name, Type fieldType) {
		this.name = name;
		this.fieldType = fieldType;
	}
}
