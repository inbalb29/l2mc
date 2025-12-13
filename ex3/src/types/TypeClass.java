package types;

import java.util.List;

public class TypeClass extends Type {
	/*********************************************************************/
	/* If this class does not extend a father class this should be null */
	/*********************************************************************/
	public TypeClass father;

	/**************************************************/
	/* Gather up all data members in one place */
	/* Note that data members coming from the AST are */
	/* packed together with the class methods */
	/**************************************************/
	public List<TypeClassVarDec> dataMembers;

	/****************/
	/* CTROR(S) ... */
	/****************/
	public TypeClass(TypeClass father, String name, List<TypeClassVarDec> dataMembers) {
		this.name = name;
		this.father = father;
		this.dataMembers = dataMembers;
	}

	public TypeClassVarDec findMemberInThisClass(String name) {
		for (TypeClassVarDec member : dataMembers) {
			if (member.name != null && member.name.equals(name)) {
				return member;
			}
		}
		return null;
	}

	/*****************************************************************************************/
	/*
	 * Method Overriding Validation Logic:
	 * We only need to check the nearest ancestor (immediate superclass).
	 * * Reasoning (Transitivity):
	 * 1. The superclass is already semantically valid.
	 * 2. If the superclass overrides a method from a grandparent, their signatures
	 * match.
	 * 3. Therefore, if the current class matches the superclass signature, it
	 * implicitly
	 * matches the grandparent signature as well and so on and so forth.
	 */
	/*****************************************************************************************/
	public TypeClassVarDec findMember(String name) {
		// Search in this class's data members
		for (TypeClassVarDec member : dataMembers) {
			if (member.name != null && member.name.equals(name)) {
				return member;
			}
		}
		// Search in parent class
		if (father != null) {
			return father.findMember(name);
		}
		return null;
	}

	public void addDataMember(TypeClassVarDec member) {
		this.dataMembers.add(member);
	}

}
