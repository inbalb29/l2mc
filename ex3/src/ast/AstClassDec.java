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

    public Type semantMe() throws SemanticError {
        /****************************************/
        /* [1] Verify class not already defined */
        /****************************************/
        if (SymbolTable.getInstance().find(name) != null) {
            throw new SemanticError("type name  '" + name + "' already exists", lineNumber);
        }

        /*****************************************************************************************/
        /*
         * [2] Verify no circular dependency with super class if this class extends from
         * another
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

        /*******************************/
        /* [5] Semant The Class Itself */
        /*******************************/

        /*****************************************************************************************/
        /*
         * [6] Get data members, we need to verify:
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
        List<TypeClassVarDec> dataMembers = new ArrayList<>();
        for (AstDec field : this.fields) {
            Type dataMemberType;
            TypeClassVarDec dataMember;
            if (field instanceof AstVarDec varDec) {
                dataMemberType = varDec.semantMe();
                dataMember = new TypeClassVarDec(dataMemberType, varDec.name);
                dataMembers.add(dataMember);

                // No Field shadowing
                if (t.findMemberInAncestor(dataMember.name) != null) {
                    throw new SemanticError("field '" + dataMember.name
                            + "' already defined as field or method in class or superclass", lineNumber);
                }
                t.addDataMember(dataMember);

            } else if (field instanceof AstFuncDec funcDec) {
                dataMemberType = funcDec.semantMe();
                dataMember = new TypeClassVarDec(dataMemberType, funcDec.name);
                dataMembers.add(dataMember);

                // No Method overloading
                if (t.findClassMember(dataMember.name) != null) {
                    throw new SemanticError(
                            "method '" + dataMember.name
                                    + "' already exists in current class (Overloading/Shadowing not allowed)",
                            lineNumber);
                }
            }
        }

        /*****************/
        /* [3] End Scope */
        /*****************/
        SymbolTable.getInstance().endScope();

        /************************************************/
        /* [4] Enter the Class Type to the Symbol Table */
        /************************************************/
        SymbolTable.getInstance().enter(name, t);

        /*********************************************************/
        /* [5] Return value is irrelevant for class declarations */
        /*********************************************************/
        return null;
    }
}
