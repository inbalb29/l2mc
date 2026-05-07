package Exceptions;

public class SemanticError extends RuntimeException {
    private final int line;

    public SemanticError(String message, int line) {
        super("Semantic error at line " + line + ": " + message);
        this.line = line;
    }

    public int getLine() {
        return line;
    }
}
