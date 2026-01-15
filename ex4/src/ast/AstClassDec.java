package ast;

import java.util.ArrayList;
import java.util.List;

import Exceptions.SemanticError;
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
        TypeClass classType = new TypeClass(parentType, name, new ArrayList<>());
        SymbolTable.getInstance().enter(name, classType);

        /*************************/
        /* [4] Begin Class Scope */
        /*************************/
        SymbolTable.getInstance().beginScope();
        SymbolTable.getInstance().setCurrentClass(classType);

        /***************************************************************/
        /* [5] Process each field/method declaration in order */
        /* (each member can only reference previously defined members) */
        /***************************************************************/
        for (AstDec field : this.fields) {
            if (field instanceof AstVarDec varDec) {
                Type fieldType = varDec.semantMe();

                // No duplicate in same class
                if (classType.findMemberInThisClass(varDec.name) != null) {
                    throw new SemanticError(
                            "field '" + varDec.name + "' already exists in class '" + classType.name + "'",
                            field.lineNumber);
                }

                // No shadowing from parent classes
                if (parentType != null && parentType.findMember(varDec.name) != null) {
                    throw new SemanticError(
                            "field '" + varDec.name + "in class '" + classType.name
                                    + "' shadows inherited member in '" + parentType.name
                                    + "'",
                            field.lineNumber);
                }

                // Add valid datamember to class type
                TypeClassField fieldMember = new TypeClassField(varDec.name, fieldType);
                classType.addDataMember(fieldMember);
                SymbolTable.getInstance().enter(varDec.name, fieldType);
            } else if (field instanceof AstFuncDec funcDec) {
                // Build method signature
                TypeFunction funcType = funcDec.buildSignature();

                // No Method overloading
                if (classType.findMemberInThisClass(funcDec.name) != null) {
                    throw new SemanticError(
                            "method '" + funcDec.name + "' already exists in class '" + classType.name + "'",
                            field.lineNumber);
                }

                Type ancestorMember = null;
                if (parentType != null) {
                    ancestorMember = parentType.findMember(funcDec.name);
                }

                if (ancestorMember != null) {
                    // Trying to define method with same name as inherited field
                    if (!(ancestorMember instanceof TypeFunction)) {
                        throw new SemanticError(
                                "method '" + funcDec.name + "' cannot have same name as inherited field",
                                field.lineNumber);
                    }

                    // Check for valid override (same signature)
                    TypeFunction parentFunc = (TypeFunction) ancestorMember;
                    if (!funcType.isValidOverride(parentFunc)) {
                        throw new SemanticError(
                                "method override '" + funcDec.name
                                        + "' has incompatible signature with inherited method in '" + parentType.name
                                        + "'",
                                field.lineNumber);
                    }
                }

                // Add method signature to class
                classType.addDataMember(funcType);
                SymbolTable.getInstance().enter(funcDec.name, funcType);

                // Analyze the body
                funcDec.semantBody(funcType);
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
