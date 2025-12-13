package types;

import java.util.ArrayList;
import java.util.List;

public class TypeFunction extends Type {
	/***********************************/
	/* The return type of the function */
	/***********************************/
	public Type returnType;

	/*************************/
	/* types of input params */
	/*************************/
	public List<Type> params;

	/****************/
	/* CTROR(S) ... */
	/****************/
	public TypeFunction(Type returnType, String name, List<Type> params) {
		this.name = name;
		this.returnType = returnType;
		this.params = params != null ? params : new ArrayList<>();
	}

	public boolean isValidOverride(TypeFunction other) {
		if (returnType != other.returnType) {
			return false;
		}
		if (params.size() != other.params.size()) {
			return false;
		}
		for (int i = 0; i < params.size(); i++) {
			if (params.get(i) != other.params.get(i)) {
				return false;
			}
		}
		return true;
	}
}
