package ast;

public enum BinOp {
    PLUS('+'),
    MINUS('-'),
    TIMES('*'),
    DIVIDE('/'),
    LT('<'),
    GT('>'),
    EQ('=');

    private final char symbol;

    BinOp(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }
}
