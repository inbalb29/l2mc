package Exceptions;

public class ParserError extends RuntimeException {
    private final int line;

    public ParserError(int line) {
        super("Syntax error at line " + line);
        this.line = line;
    }

    public int getLine() {
        return line;
    }
}
