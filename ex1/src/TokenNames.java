public interface TokenNames {
    /* terminals */
    public static final int EOF = 0;
    public static final int PLUS = 1;
    public static final int MINUS = 2;
    public static final int TIMES = 3;
    public static final int DIVIDE = 4;
    public static final int LPAREN = 5;
    public static final int RPAREN = 6;
    public static final int INT_LITERAL = 7;
    public static final int ID = 8;
    public static final int STRING_LITERAL = 9;
    public static final int LBRACK = 10;
    public static final int RBRACK = 11;
    public static final int LBRACE = 12;
    public static final int RBRACE = 13;
    public static final int COMMA = 14;
    public static final int DOT = 15;
    public static final int SEMICOLON = 16;
    public static final int TYPE_INT = 17;
    public static final int TYPE_STRING = 18;
    public static final int TYPE_VOID = 19;
    public static final int ASSIGN = 20;
    public static final int EQ = 21;
    public static final int LT = 22;
    public static final int GT = 23;
    public static final int ARRAY = 24;
    public static final int CLASS = 25;
    public static final int RETURN = 26;
    public static final int WHILE = 27;
    public static final int IF = 28;
    public static final int ELSE = 29;
    public static final int NEW = 30;
    public static final int EXTENDS = 31;
    public static final int NIL = 32;

    String[] TOKEN_NAMES = {
            "EOF",
            "PLUS",
            "MINUS",
            "TIMES",
            "DIVIDE",
            "LPAREN",
            "RPAREN",
            "INT",
            "ID",
            "STRING",
            "LBRACK",
            "RBRACK",
            "LBRACE",
            "RBRACE",
            "COMMA",
            "DOT",
            "SEMICOLON",
            "TYPE_INT",
            "TYPE_STRING",
            "TYPE_VOID",
            "ASSIGN",
            "EQ",
            "LT",
            "GT",
            "ARRAY",
            "CLASS",
            "RETURN",
            "WHILE",
            "IF",
            "ELSE",
            "NEW",
            "EXTENDS",
            "NIL"
    };

    static String getTokenName(int token) {
        if (token >= 0 && token < TOKEN_NAMES.length) {
            return TOKEN_NAMES[token];
        }
        return "UNKNOWN";
    }
}
