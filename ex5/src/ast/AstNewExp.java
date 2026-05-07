package ast;

import Exceptions.SemanticError;
import codegen.ClassLayout;
import ir.*;
import temp.Temp;
import temp.TempFactory;
import types.*;

public class AstNewExp extends AstExp {
    public AstType type;
    public AstExp sizeExp; // null for simple NEW type, non-null for NEW type[size]

    /******************/
    /* CONSTRUCTOR(S) */
    /******************/
    public AstNewExp(AstType type, AstExp sizeExp, int lineNumber) {
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        if (sizeExp != null) {
            System.out.print("====================== newExp -> NEW type LBRACK exp RBRACK\n");
        } else {
            System.out.print("====================== newExp -> NEW type\n");
        }

        /*******************************/
        /* COPY INPUT DATA MEMBERS ... */
        /*******************************/
        this.type = type;
        this.sizeExp = sizeExp;
        this.lineNumber = lineNumber;
    }

    /************************************************/
    /* The printing message for a new exp AST node */
    /************************************************/
    public void printMe() {
        /*******************************/
        /* AST NODE TYPE = AST NEW EXP */
        /*******************************/
        System.out.print("AST NODE NEW EXP\n");

        /**************************************/
        /* RECURSIVELY PRINT TYPE + SIZE ... */
        /**************************************/
        if (type != null)
            type.printMe();
        if (sizeExp != null)
            sizeExp.printMe();

        /***************************************/
        /* PRINT Node to AST GRAPHVIZ DOT file */
        /***************************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                "NEW\nEXP");

        /****************************************/
        /* PRINT Edges to AST GRAPHVIZ DOT file */
        /****************************************/
        if (type != null)
            AstGraphviz.getInstance().logEdge(serialNumber, type.serialNumber);
        if (sizeExp != null)
            AstGraphviz.getInstance().logEdge(serialNumber, sizeExp.serialNumber);
    }

    @Override
    public Type semantMe() {
        Type t = type.semantMe();

        if (sizeExp != null) {
            /* Array allocation: new T[e] */

            Type sizeType = sizeExp.semantMe();
            if (!sizeType.isInt()) {
                throw new SemanticError("array size must be of type int", lineNumber);
            }

            if (sizeExp instanceof AstExpInt size && size.value <= 0) {
                throw new SemanticError("array size must be greater than 0", lineNumber);
            }

            return new TypeArray(t.name + "[]", t);
        } else {
            /* Object allocation: new T */
            if (!t.isClass()) {
                throw new SemanticError("cannot allocate non-class type '" + type.name + "' with new", lineNumber);
            }
        }

        return t;
    }

    @Override
    public Temp irMe() {
        if (sizeExp != null) {
            return irMeArray();
        } else {
            return irMeObject();
        }
    }

    private Temp irMeArray() {
        Temp sizeTemp = sizeExp.irMe();

        // Compute (size + 1) * 4 bytes for [length][elem0]...[elemN-1]
        Temp oneTemp = TempFactory.getInstance().getFreshTemp();
        Ir.getInstance().AddIrCommand(new IRcommandConstInt(oneTemp, 1));
        Temp totalTemp = TempFactory.getInstance().getFreshTemp();
        Ir.getInstance().AddIrCommand(new IrCommandBinopAddIntegers(totalTemp, sizeTemp, oneTemp));
        Temp fourTemp = TempFactory.getInstance().getFreshTemp();
        Ir.getInstance().AddIrCommand(new IRcommandConstInt(fourTemp, 4));
        Temp bytesTemp = TempFactory.getInstance().getFreshTemp();
        Ir.getInstance().AddIrCommand(new IrCommandBinopMulIntegers(bytesTemp, totalTemp, fourTemp));

        // Malloc
        Temp arrTemp = TempFactory.getInstance().getFreshTemp();
        Ir.getInstance().AddIrCommand(new IrCommandMalloc(arrTemp, bytesTemp));

        // Store length at offset 0
        Ir.getInstance().AddIrCommand(new IrCommandFieldSet(arrTemp, 0, sizeTemp));

        // Zero-initialize all elements
        Ir.getInstance().AddIrCommand(new IrCommandZeroInitArray(arrTemp, sizeTemp));

        return arrTemp;
    }

    private Temp irMeObject() {
        ClassLayout layout = ClassLayout.getLayout(type.name);

        // Malloc object
        Temp objTemp = TempFactory.getInstance().getFreshTemp();
        Ir.getInstance().AddIrCommand(new IrCommandMalloc(objTemp, layout.objectSize));

        // Store vtable pointer at offset 0
        if (!layout.vtable.isEmpty()) {
            Temp vtableAddr = TempFactory.getInstance().getFreshTemp();
            Ir.getInstance().AddIrCommand(new IrCommandLoadAddress(vtableAddr, layout.vtableLabel));
            Ir.getInstance().AddIrCommand(new IrCommandFieldSet(objTemp, 0, vtableAddr));
        } else {
            // Even with no methods, store 0 at vtable slot
            Temp zeroTemp = TempFactory.getInstance().getFreshTemp();
            Ir.getInstance().AddIrCommand(new IRcommandConstInt(zeroTemp, 0));
            Ir.getInstance().AddIrCommand(new IrCommandFieldSet(objTemp, 0, zeroTemp));
        }

        // Initialize each field
        for (ClassLayout.FieldInfo fi : layout.fields) {
            Temp valTemp = TempFactory.getInstance().getFreshTemp();
            if (fi.initValue instanceof Integer intVal) {
                Ir.getInstance().AddIrCommand(new IRcommandConstInt(valTemp, intVal));
            } else if (fi.initValue instanceof String strVal) {
                String label = ClassLayout.getOrCreateStringLabel(strVal);
                Ir.getInstance().AddIrCommand(new IrCommandLoadAddress(valTemp, label));
            } else {
                Ir.getInstance().AddIrCommand(new IRcommandConstInt(valTemp, 0));
            }
            Ir.getInstance().AddIrCommand(new IrCommandFieldSet(objTemp, fi.offset, valTemp));
        }

        return objTemp;
    }
}
