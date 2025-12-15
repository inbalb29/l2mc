package ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import Exceptions.SemanticError;
import java_cup.runtime.Symbol;
import symboltable.SymbolTable;
import types.*;

public class AstClassDec extends AstDec {
    public String name;
    public String parentName; // null if no extends
    public List<AstDec> fields;

    /******************/
    /* CONSTRUCTOR(S) */
    /******************/
    public AstClassDec(String name, String parentName, List<AstDec> fields, int lineNumber) {
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        if (parentName != null) {
            System.out.format("====================== classDec -> CLASS ID(%s) EXTENDS ID(%s) LBRACE cFields RBRACE\n",
                    name, parentName);
        } else {
            System.out.format("====================== classDec -> CLASS ID(%s) LBRACE cFields RBRACE\n", name);
        }

        /*******************************/
        /* COPY INPUT DATA MEMBERS ... */
        /*******************************/
        this.name = name;
        this.parentName = parentName;
        this.fields = fields;
        this.lineNumber = lineNumber;
    }

    /******************************************************/
    /* The printing message for a class declaration node */
    /******************************************************/
    public void printMe() {
        /*******************************************/
        /* AST NODE TYPE = AST CLASS DECLARATION */
        /*******************************************/
        System.out.format("AST NODE CLASS DEC(%s)\n", name);
        if (parentName != null) {
            System.out.format("  EXTENDS: %s\n", parentName);
        }

        /**************************************/
        /* RECURSIVELY PRINT FIELDS ... */
        /**************************************/
        for (AstDec field : fields) {
            if (field != null)
                field.printMe();
        }

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        String label = parentName != null
                ? String.format("CLASS\n%s\nEXTENDS\n%s", name, parentName)
                : String.format("CLASS\n%s", name);
        AstGraphviz.getInstance().logNode(serialNumber, label);

        /****************************************/
        /* PRINT Edges to AST GRAPHVIZ DOT file */
        /****************************************/
        for (AstDec field : fields) {
            if (field != null)
                AstGraphviz.getInstance().logEdge(serialNumber, field.serialNumber);
        }
    }

    @Override
    public Type semantMe() {
        /****************************************/
        /* [1] Verify class not already defined */
        /****************************************/
        if (SymbolTable.getInstance().find(name) != null) {
            throw new SemanticError("type name  '" + name + "' already exists", lineNumber);
        }

        if (!SymbolTable.getInstance().inGlobalScope()) {
            throw new SemanticError("Class '" + name + "' cannot be declared inside non global scope", lineNumber);
        }
        /*****************************************************************************************/
        /*
         * [2] If this class extends from another, verify:
         * 1. super class already defined
         * 2. no circular dependency with super class
         */
        /*****************************************************************************************/
        TypeClass parentType = null;
        if (parentName != null) {
            Type parent = SymbolTable.getInstance().find(parentName);
            if (parent == null || !parent.isClass()) {
                throw new SemanticError("parent '" + parentName + "' does not exist or is not a class", lineNumber);
            }
            parentType = (TypeClass) parent;
        }

        for (TypeClass ancestor = parentType; ancestor != null; ancestor = ancestor.father) {
            if (ancestor.name.equals(name)) { // Circular dependency
                throw new SemanticError("class '" + name + "' directly or indirectly extends itself", lineNumber);
            }
        }

        /***************************************************************/
        /* [3] Create Class Type and enter to symbol table */
        /* We need to enter it first so the class can reference itself */
        /***************************************************************/
        TypeClass t = new TypeClass(parentType, name, new ArrayList<>());
        SymbolTable.getInstance().enter(name, t);

        /*************************/
        /* [4] Begin Class Scope */
        /*************************/
        SymbolTable.getInstance().beginScope();
        SymbolTable.getInstance().setCurrentClass(t);

        /*****************************************************************************************/
        /*
         * [5] Get data members, we need to verify:
         * - No method overloading (defining multiple methods with the same name but
         * different signatures in the same class).
         * - No Shadowing:
         * - No defining a field with the same name as an existing field or method in
         * the same class or any of its superclasses.
         * - No defining a method with the same name as an existing field in the same
         * class or any of its superclasses.
         * 
         * Note: Method overriding (redefining a method inherited from a superclass in a
         * derived class) is allowed.
         * When overriding a method, the return type and the types and order of
         * arguments must match exactly the original method in the superclass.
         * Subclass substitutions in the signature are not allowed.
         */
        /*****************************************************************************************/
        for (AstDec field : this.fields) {
            Type dataMemberType;
            TypeClassVarDec dataMember;
            if (field instanceof AstVarDec varDec) {
                dataMemberType = varDec.semantMe();
                dataMember = new TypeClassVarDec(dataMemberType, varDec.name);

                // No duplicate in same class
                if (t.findMemberInThisClass(dataMember.name) != null) {
                    throw new SemanticError("Field: '" + dataMember.name
                            + "' already defined as field or method in class. (Shadowing not allowed)",
                            field.lineNumber);
                }

                // No shadowing from parent classes
                if (t.father != null && t.father.findMember(dataMember.name) != null) {
                    throw new SemanticError("Field: '" + dataMember.name
                            + "' already defined as field or method in superclass (Shadowing not allowed)",
                            field.lineNumber);
                }

                // Add valid datamember to class type
                t.addDataMember(dataMember);
                SymbolTable.getInstance().enter(dataMember.name, dataMemberType);

            } else if (field instanceof AstFuncDec funcDec) {
                dataMemberType = funcDec.semantMe();
                dataMember = new TypeClassVarDec(dataMemberType, funcDec.name);

                // No Method overloading
                if (t.findMemberInThisClass(dataMember.name) != null) {
                    throw new SemanticError(
                            "Method: '" + dataMember.name
                                    + "' already defined as field or method in current class (Overloading/Shadowing not allowed)",
                            field.lineNumber);
                }

                TypeClassVarDec ancestorMember;
                if (t.father != null) {
                    ancestorMember = t.father.findMember(dataMember.name);
                } else {
                    ancestorMember = null;
                }

                // Member already defined in ancestor class, this this classes datamember is a
                // valid instance of method overriding
                // I.e. need to check if ancestor member is a method (datamember already checked
                // to be one) and need to check that
                // their signatures and return types are identical. If its a valid override of
                // nearest ancestor then its a valid override
                // of all ancestors, read @findMember docString for further details.
                if (ancestorMember != null) {
                    if (ancestorMember.t instanceof TypeFunction) {
                        // dataMember is derived from AstFuncDec's semantMe which returns a TypeFunction
                        // type.
                        TypeFunction mydataMemberFunc = (TypeFunction) dataMember.t;
                        TypeFunction ancestordataMemberFunc = (TypeFunction) ancestorMember.t;

                        if (!mydataMemberFunc.isValidOverride(ancestordataMemberFunc)) {
                            throw new SemanticError("Method: '" + dataMember.name
                                    + "' is an illegal override (different signatures) of an ancestor method.",
                                    field.lineNumber);
                        }
                    }
                    // AncestorMember isn't a function type so must be a field.
                    // (We implicitly assume that a valid class only has Methods and field types,
                    // additionally
                    // this implicit assumption is upheld both by the CUP parser and the AstClass
                    // grammer/derivation and is implicitly
                    // upheld here as we only handle dataMember's of type AstFuncDec and and
                    // AstVarDec)
                    else {
                        throw new SemanticError("Method: '" + dataMember.name
                                + "' already defined as field in ancestor. (Shadowing not allowed).", field.lineNumber);
                    }
                }

                // Only reach here if:
                // class hasn't already declared dataMember AND no ancestor declared datamember
                // OR
                // class hasn't already declared dataMember AND ancestor also declared
                // datamember as a method with the exact same signature
                t.addDataMember(dataMember);
                SymbolTable.getInstance().enter(dataMember.name, dataMemberType);
            }
        }

        /*****************/
        /* [6] End Scope */
        /*****************/
        SymbolTable.getInstance().setCurrentClass(null);
        SymbolTable.getInstance().endScope();

        /*********************************************************/
        /* [7] Return value is irrelevant for class declarations */
        /*********************************************************/
        return null;
    }
}
