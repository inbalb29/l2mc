package ast;

public abstract class AstDec extends AstStmt {
    // All declarations are also statements
    // This allows variable declarations to be used both at top-level and within
    // functions
}