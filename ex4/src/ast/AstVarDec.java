package ast;

import Exceptions.SemanticError;
import ir.*;
import symboltable.SymbolTable;
import temp.Temp;
import types.*;

public class AstVarDec extends AstDec {
    public AstType type;
    public String name;
    public AstExp initExp; // Can be null if no initialization
    public String qualifiedName; // Set during semantMe for IR generation

    /******************/
    /* CONSTRUCTOR(S) */
    /******************/

    // Constructor without initialization
    public AstVarDec(AstType type, String name, int lineNumber) {
        this(type, name, null, lineNumber);
    }

    // Constructor with initialization expression
    public AstVarDec(AstType type, String name, AstExp initExp, int lineNumber) {
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        if (initExp != null) {
            System.out.format("====================== varDec -> type ID(%s) ASSIGN exp SEMICOLON\n", name);
        } else {
            System.out.format("====================== varDec -> type ID(%s) SEMICOLON\n", name);
        }

        /*******************************/
        /* COPY INPUT DATA MEMBERS ... */
        /*******************************/
        this.type = type;
        this.name = name;
        this.initExp = initExp;
        this.lineNumber = lineNumber;
    }

    /****************************************************/
    /* The printing message for a var declaration node */
    /****************************************************/
    public void printMe() {
        /****************************************/
        /* AST NODE TYPE = AST VAR DECLARATION */
        /****************************************/
        System.out.format("AST NODE VAR DEC(%s)\n", name);

        /**************************************/
        /* RECURSIVELY PRINT TYPE + INIT ... */
        /**************************************/
        if (type != null)
            type.printMe();
        if (initExp != null)
            initExp.printMe();

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                String.format("VAR\nDEC\n%s", name));

        /****************************************/
        /* PRINT Edges to AST GRAPHVIZ DOT file */
        /****************************************/
        if (type != null)
            AstGraphviz.getInstance().logEdge(serialNumber, type.serialNumber);
        if (initExp != null)
            AstGraphviz.getInstance().logEdge(serialNumber, initExp.serialNumber);
    }

    /**
     * Declare the global variable name and type in the symbol table without
     * analyzing the initialization expression, to allow for forward references.
     */
    public Type declareGlobal() {
        if (SymbolTable.getInstance().existsInCurrentScope(name)) {
            throw new SemanticError("variable '" + name + "' already exists in scope", lineNumber);
        }

        Type varType = type.semantMe();
        if (varType.isVoid()) {
            throw new SemanticError("variable cannot be declared with type void", lineNumber);
        }

        SymbolTable.getInstance().enter(name, varType);
        // Store qualified name for IR generation
        this.qualifiedName = SymbolTable.getInstance().findQualifiedName(name);
        return varType;
    }

    /**
     * Analyze the global variable initialization expression after all globals
     * have been declared.
     */
    public void semantGlobalInit() {
        if (initExp != null) {
            Type varType = SymbolTable.getInstance().find(name);
            Type initType = initExp.semantMe();

            if (!isAssignmentCompatible(varType, initType, initExp)) {
                throw new SemanticError(
                        "initialization type mismatch: '" + initType.name + "' cannot be assigned to '" + varType.name
                                + "'",
                        lineNumber);
            }
        }
    }

    @Override
    public Type semantMe() {
        if (SymbolTable.getInstance().existsInCurrentScope(name)) {
            throw new SemanticError("variable '" + name + "' already exists in scope", lineNumber);
        }

        Type varType = type.semantMe();
        if (varType.isVoid()) {
            throw new SemanticError("variable cannot be declared with type void", lineNumber);
        }

        if (initExp != null) {
            Type initType = initExp.semantMe();

            if (!isAssignmentCompatible(varType, initType, initExp)) {
                throw new SemanticError(
                        "initialization type mismatch: '" + initType.name + "' cannot be assigned to '" + varType.name
                                + "'",
                        lineNumber);
            }
        }

        /* [3] Enter the Identifier to the Symbol Table */
        if (SymbolTable.getInstance().getCurrentClass() == null
                || SymbolTable.getInstance().getCurrentFunction() != null) {
            // Normal variable declaration. Members are entered in classDec
            SymbolTable.getInstance().enter(name, varType);
            // Store qualified name for IR generation
            this.qualifiedName = SymbolTable.getInstance().findQualifiedName(name);
        }

        return varType;
    }

    public static boolean isAssignmentCompatible(Type lhs, Type rhs, AstExp rhsExp) {
        // Nil can be assigned to arrays and classes
        if (rhs.isNil()) {
            return lhs.isArray() || lhs.isClass();
        }

        if (rhsExp instanceof AstNewExp newExp) {
            if (newExp.sizeExp != null) {
                // Array allocation: lhs must be an array type
                if (!lhs.isArray()) {
                    return false;
                }

                TypeArray arrType = (TypeArray) lhs;
                Type elemType = newExp.type.semantMe();
                return arrType.innerType == elemType;
            } else {
                // Class allocation: new T
                if (!lhs.isClass()) {
                    return false;
                }

                Type classType = newExp.type.semantMe();
                if (!classType.isClass()) {
                    return false;
                }

                TypeClass lhsClass = (TypeClass) lhs;
                TypeClass rhsClass = (TypeClass) classType;
                return rhsClass.isSubclassOf(lhsClass);
            }
        }

        if (lhs == rhs) {
            return true;
        }

        // For class types, check inheritance
        if (lhs.isClass() && rhs.isClass()) {
            TypeClass lhsClass = (TypeClass) lhs;
            TypeClass rhsClass = (TypeClass) rhs;
            return rhsClass.isSubclassOf(lhsClass);
        }

        return false;
    }

    public Temp irMe() {
        Ir.getInstance().AddIrCommand(new IrCommandAllocate(qualifiedName));

        if (initExp != null) {
            Ir.getInstance().AddIrCommand(new IrCommandStore(qualifiedName, initExp.irMe()));
        }
        return null;
    }
}
