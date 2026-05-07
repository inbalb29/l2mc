package ast;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import codegen.ClassLayout;
import ir.Ir;
import ir.IrCommandStore;
import ir.IrContext;
import symboltable.SymbolTable;
import temp.Temp;
import types.*;

public class AstProg extends AstNode {
    public List<AstDec> decs;

    public AstProg(AstDec dec, int lineNumber) {
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();

        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        System.out.print("====================== program -> dec\n");
        /*******************************/
        /* COPY INPUT DATA MEMBERS ... */
        /*******************************/
        decs = new java.util.ArrayList<AstDec>();
        decs.add(dec);
        this.lineNumber = lineNumber;
    }

    public void addDeclaration(AstDec dec) {
        /***************************************/
        /* PRINT CORRESPONDING DERIVATION RULE */
        /***************************************/
        System.out.print("====================== program -> program dec\n");

        this.decs.add(dec);
    }

    /***********************************************/
    /* The printing message for a program AST node */
    /***********************************************/
    public void printMe() {
        /*********************************/
        /* AST NODE TYPE = AST PROGRAM */
        /*********************************/
        System.out.print("AST NODE PROGRAM\n");

        /**************************************/
        /* PRINT ALL THE DECLARATIONS INSIDE IT */
        /**************************************/
        for (AstDec dec : decs) {
            if (dec != null)
                dec.printMe();
        }

        /*********************************/
        /* PRINT TO AST GRAPHVIZ DOT FILE */
        /*********************************/
        AstGraphviz.getInstance().logNode(
                serialNumber,
                "PROGRAM");
        for (AstDec dec : decs) {
            if (dec != null)
                AstGraphviz.getInstance().logEdge(serialNumber, dec.serialNumber);
        }
    }

    @Override
    public Type semantMe() {
        // Collect all global declarations
        for (AstDec dec : decs) {
            if (dec != null) {
                if (dec instanceof AstVarDec varDec) {
                    varDec.declareGlobal();
                } else if (dec instanceof AstFuncDec funcDec) {
                    TypeFunction funcType = funcDec.buildSignature();
                    SymbolTable.getInstance().enter(funcDec.name, funcType);
                } else if (dec instanceof AstClassDec classDec) {
                    classDec.semantMe();
                } else if (dec instanceof AstTypedef typeDef) {
                    typeDef.semantMe();
                }
            }
        }

        // Analyze global initializations and function bodies
        for (AstDec dec : decs) {
            if (dec != null) {
                if (dec instanceof AstVarDec varDec) {
                    varDec.semantGlobalInit();
                } else if (dec instanceof AstFuncDec funcDec) {
                    TypeFunction funcType = (TypeFunction) SymbolTable.getInstance().find(funcDec.name);
                    funcDec.semantBody(funcType);
                }
                // Classes and array type defs already handled in pass 1
            }
        }

        return null;
    }

    @Override
    public Temp irMe() {
        // Phase 1: Compute class layouts for all classes
        Map<String, List<ClassLayout.FieldInitInfo>> classFieldInits = new HashMap<>();
        for (AstDec dec : decs) {
            if (dec instanceof AstClassDec classDec) {
                List<ClassLayout.FieldInitInfo> inits = new ArrayList<>();
                for (AstDec field : classDec.fields) {
                    if (field instanceof AstVarDec varDec) {
                        Object initVal = null;
                        if (varDec.initExp instanceof AstExpInt intExp) {
                            initVal = intExp.value;
                        } else if (varDec.initExp instanceof AstExpString strExp) {
                            initVal = strExp.value;
                        }
                        inits.add(new ClassLayout.FieldInitInfo(varDec.name, initVal));
                    }
                }
                classFieldInits.put(classDec.name, inits);
            }
        }
        for (AstDec dec : decs) {
            if (dec instanceof AstClassDec classDec) {
                Type t = SymbolTable.getInstance().find(classDec.name);
                if (t instanceof TypeClass tc) {
                    ClassLayout.computeLayout(tc, classFieldInits);
                }
            }
        }

        // Phase 2: Global variable declarations
        for (AstDec dec : decs) {
            if (dec != null && dec instanceof AstVarDec) {
                dec.irMe();
            }
        }

        // Phase 2.5: Runtime initialization for non-constant global initializers
        List<AstVarDec> nonConstGlobalInits = new ArrayList<>();
        for (AstDec dec : decs) {
            if (dec instanceof AstVarDec varDec && varDec.initExp != null
                && !(varDec.initExp instanceof AstExpInt)
                && !(varDec.initExp instanceof AstExpString)) {
                nonConstGlobalInits.add(varDec);
            }
        }
        if (!nonConstGlobalInits.isEmpty()) {
            IrContext.getInstance().enterFunction("__init_globals", false, null);
            Ir.getInstance().beginFunction("__init_globals", 0, 0, false);
            Ir.getInstance().setHasGlobalInit();
            for (AstVarDec varDec : nonConstGlobalInits) {
                Temp t = varDec.initExp.irMe();
                Ir.getInstance().AddIrCommand(new IrCommandStore(varDec.qualifiedName, t));
            }
            Ir.getInstance().getCurrentFunction().setNumLocals(
                IrContext.getInstance().getNumLocals());
            Ir.getInstance().endFunction();
            IrContext.getInstance().exitFunction();
        }

        // Phase 3: Global functions
        for (AstDec dec : decs) {
            if (dec != null && dec instanceof AstFuncDec) {
                dec.irMe();
            }
        }

        // Phase 4: Class methods
        for (AstDec dec : decs) {
            if (dec != null && dec instanceof AstClassDec) {
                dec.irMe();
            }
        }

        return null;
    }
}