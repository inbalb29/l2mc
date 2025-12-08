package types;

/**
 * @deprecated Please use A List<> of TypeClassVarDec instead
 */
public class TypeClassVarDecList {
	public TypeClassVarDec head;
	public TypeClassVarDecList tail;

	public TypeClassVarDecList(TypeClassVarDec head, TypeClassVarDecList tail) {
		this.head = head;
		this.tail = tail;
	}
}
