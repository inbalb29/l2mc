package ast;

import java.util.ArrayList;
import java.util.List;

import Exceptions.SemanticError;
import codegen.ClassLayout;
import ir.*;
import symboltable.SymbolTable;
import temp.Temp;
import temp.TempFactory;
import types.*;

public class AstCallExp extends AstExp {
    public AstVar object; // null for simple function calls, non-null for method calls
    public String funcName;
    public List<AstExp> args;
    public boolean isMethodCall = false; // Set during semantMe for IR generation
    public TypeClass resolvedCallClass = null; // Set during semantMe for IR generation

    /******************/
    /* CONSTRUCTOR(S) */
    /******************/
    public AstCallExp(AstVar object, String funcName, List<AstExp> args, int lineNumber) {
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        if (object != null) {
            System.out.format("====================== callExp -> var DOT ID(%s) LPAREN args RPAREN\n", funcName);
        } else {
            System.out.format("====================== callExp -> ID(%s) LPAREN args RPAREN\n", funcName);
        }

        /*******************************/
        /* COPY INPUT DATA MEMBERS ... */
        /*******************************/
        this.object = object;
        this.funcName = funcName;
        this.args = args;
        this.lineNumber = lineNumber;
    }

    /*************************************************/
    /* The printing message for a call exp AST node */
    /*************************************************/
    public void printMe() {
        /********************************/
        /* AST NODE TYPE = AST CALL EXP */
        /********************************/
        System.out.format("AST NODE CALL EXP(%s)\n", funcName);

        /******************************************/
        /* RECURSIVELY PRINT OBJECT + ARGS ... */
        /******************************************/
        if (object != null)
            object.printMe();
        for (AstExp arg : args) {
            if (arg != null)
                arg.printMe();
        }

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                String.format("CALL\n%s", funcName));

        /****************************************/
        /* PRINT Edges to AST GRAPHVIZ DOT file */
        /****************************************/
        if (object != null)
            AstGraphviz.getInstance().logEdge(serialNumber, object.serialNumber);
        for (AstExp arg : args) {
            if (arg != null)
                AstGraphviz.getInstance().logEdge(serialNumber, arg.serialNumber);
        }
    }

    @Override
    public Type semantMe() {
        TypeFunction funcType = null;

        if (object != null) {
            /* Method call */
            Type objType = object.semantMe();
            if (objType == null || !objType.isClass()) {
                throw new SemanticError("method call on a non-class type", lineNumber);
            }

            TypeClass classType = (TypeClass) objType;
            Type memberType = classType.findMember(funcName);

            if (memberType == null) {
                throw new SemanticError("method '" + funcName + "' not found in class '" + classType.name + "'",
                        lineNumber);
            }

            if (!(memberType instanceof TypeFunction)) {
                throw new SemanticError("cannot call non-method '" + funcName + "' in class '" + classType.name + "'",
                        lineNumber);
            }

            funcType = (TypeFunction) memberType;
            this.isMethodCall = true;
            this.resolvedCallClass = classType;
        } else {
            /* Simple function call, or method call from within a class */
            TypeClass currentClass = SymbolTable.getInstance().getCurrentClass();
            Type t = null;

            // If inside a class, try to resolve as a member (method) first
            if (currentClass != null) {
                t = currentClass.findMember(funcName);
                if (t != null) {
                    if (!(t instanceof TypeFunction)) {
                        throw new SemanticError(
                                "cannot call non-method '" + funcName + "' in class '" + currentClass.name + "'",
                                lineNumber);
                    }
                    funcType = (TypeFunction) t;
                    this.isMethodCall = true;
                    this.resolvedCallClass = currentClass;
                }
            }

            // Fallback to global scope if not found as a class member
            if (funcType == null) {
                t = SymbolTable.getInstance().find(funcName);
                if (t == null) {
                    throw new SemanticError("function '" + funcName + "' not found", lineNumber);
                }

                if (!(t instanceof TypeFunction)) {
                    throw new SemanticError("cannot call non-function '" + funcName + "'", lineNumber);
                }

                funcType = (TypeFunction) t;
            }
        }

        // Check arguments
        if (args.size() != funcType.params.size()) {
            throw new SemanticError("wrong number of arguments for function '" + funcName + "'", lineNumber);
        }

        for (int i = 0; i < args.size(); i++) {
            AstExp arg = args.get(i);
            Type argType = arg.semantMe();
            Type paramType = funcType.params.get(i);

            if (!isArgumentCompatible(paramType, argType, arg)) {
                throw new SemanticError("incompatible argument type for parameter " + (i + 1) +
                        " in function '" + funcName + "'", lineNumber);
            }
        }

        return funcType.returnType;
    }

    private static boolean isArgumentCompatible(Type paramType, Type argType, AstExp argExp) {
        if (argType.isNil()) {
            return paramType.isArray() || paramType.isClass();
        }

        if (paramType == argType) {
            return true;
        }

        if (paramType.isClass() && argType.isClass()) {
            TypeClass paramClass = (TypeClass) paramType;
            TypeClass argClass = (TypeClass) argType;
            return argClass.isSubclassOf(paramClass);
        }

        return false;
    }

    public Temp irMe() {
        IrContext ctx = IrContext.getInstance();

        // Handle builtins
        if (!isMethodCall && "PrintInt".equals(funcName)) {
            Temp argTemp = args.get(0).irMe();
            Ir.getInstance().AddIrCommand(new IrCommandPrintInt(argTemp));
            return null;
        }
        if (!isMethodCall && "PrintString".equals(funcName)) {
            Temp argTemp = args.get(0).irMe();
            Ir.getInstance().AddIrCommand(new IrCommandPrintString(argTemp));
            return null;
        }

        // Evaluate arguments
        List<Temp> argTemps = new ArrayList<>();
        for (AstExp arg : args) {
            argTemps.add(arg.irMe());
        }

        Temp dst = TempFactory.getInstance().getFreshTemp();

        if (isMethodCall) {
            // Virtual method call
            ClassLayout layout = ClassLayout.getLayout(resolvedCallClass.name);
            int vtableIndex = layout.getMethodVtableIndex(funcName);
            int vtableOffset = vtableIndex * 4;

            Temp objTemp;
            if (object != null) {
                // Explicit receiver: obj.method(args)
                objTemp = object.irMe();
            } else {
                // Implicit 'this': called from within the class
                objTemp = TempFactory.getInstance().getFreshTemp();
                Ir.getInstance().AddIrCommand(
                        new IrCommandLoadFrame(objTemp, ctx.getThisParamOffset()));
            }
            Ir.getInstance().AddIrCommand(new IrCommandCheckNullPtr(objTemp));
            Ir.getInstance().AddIrCommand(
                    new IrCommandVirtualCall(dst, objTemp, vtableOffset, argTemps));
        } else {
            // Regular function call
            String funcLabel = "main".equals(funcName) ? "user_main" : funcName;
            Ir.getInstance().AddIrCommand(
                    new IrCommandCallFunc(dst, funcLabel, argTemps));
        }

        return dst;
    }
}
