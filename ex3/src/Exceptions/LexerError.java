package Exceptions;

public class LexerError extends RuntimeException {
    public LexerError(String message, int row, int column) {
        super(message + " at line " + row + ", column " + column);
    }
}
