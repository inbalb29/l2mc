package types;

public abstract class Type {
	/******************************/
	/* Every type has a name ... */
	/******************************/
	public String name;
	public int lineNumber;

	/*************/
	/* isClass() */
	/*************/
	public boolean isClass() {
		return false;
	}

	/*************/
	/* isArray() */
	/*************/
	public boolean isArray() {
		return false;
	}

	public boolean isInt() {
		return false;
	}

	public boolean isString() {
		return false;
	}

	public boolean isVoid() {
		return false;
	}

	public boolean isNil() {
		return false;
	}
}
