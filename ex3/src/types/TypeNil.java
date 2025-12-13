package types;

public class TypeNil extends Type {
    /**************************************/
    /* USUAL SINGLETON IMPLEMENTATION ... */
    /**************************************/
    private static TypeNil instance = null;

    /*****************************/
    /* PREVENT INSTANTIATION ... */
    /*****************************/
    protected TypeNil() {
    }

    /******************************/
    /* GET SINGLETON INSTANCE ... */
    /******************************/
    public static TypeNil getInstance() {
        if (instance == null) {
            instance = new TypeNil();
            instance.name = "nil";
        }
        return instance;
    }
}
