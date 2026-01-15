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
	public List<Type> dataMembers;

	/****************/
	/* CTROR(S) ... */
	/****************/
	public TypeClass(TypeClass father, String name, List<Type> dataMembers) {
		this.name = name;
		this.father = father;
		this.dataMembers = dataMembers != null ? dataMembers : List.of();
	}

	@Override
	public boolean isClass() {
		return true;
	}

	public boolean isSubclassOf(TypeClass other) {
		if (this == other) {
			return true;
		}
		if (father != null) {
			return father.isSubclassOf(other);
		}
		return false;
	}

	/**
	 * Find a member (field or method) in this class or its superclasses
	 */
	public Type findMember(String name) {
		// Search in this class's data members
		for (Type member : dataMembers) {
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

	/**
	 * Find a member only in this class (not superclasses)
	 */
	public Type findMemberInThisClass(String name) {
		for (Type member : dataMembers) {
			if (member.name != null && member.name.equals(name)) {
				return member;
			}
		}
		return null;
	}

	public void addDataMember(Type member) {
		this.dataMembers.add(member);
	}

}
