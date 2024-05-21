package com.javasl.compiler;

public class Token {
    public Token() {}
    public Token(Type type, String textRepresentation) {
        this.type = type;
        this.textRepresentation = textRepresentation;
    }
    public static enum Type {
        // unknonw type
        UNKNOWN,
        // words
        IDENTIFIER, COMMENT,
        // keywords
        KW_IF, KW_ELSE, KW_DO, KW_WHILE, KW_FOR, KW_BREAK, KW_CONTINUE, KW_RETURN, KW_TRUE, KW_FALSE, KW_NULL,
        // types
        GROUP_TYPES_START,
            T_VOID,
            T_INT8, T_INT16, T_INT32, T_INT64,
            T_UINT8, T_UINT16, T_UINT32, T_UINT64,
            T_FLOAT, T_DOUBLE,
            T_BOOL, T_STRING, T_CHAR,
        GROUP_TYPES_END,
        // operators
        GROUP_OP_PREFIX_START,
            OP_LOGIC_NOT, OP_BITWISE_NOT,
        GROUP_OP_PREFIX_END,
        GROUP_OP_POSTFIX_START,

        GROUP_OP_POSTFIX_END,
        GROUP_OP_INFIX_START,
            OP_PLUS, OP_MINUS, OP_MULTIPLY, OP_DIVIDE, OP_MODULO,
            OP_LOGIC_AND, OP_LOGIC_OR, OP_SHIFT_LEFT, OP_SHIFT_RIGHT, OP_BITWISE_AND, OP_BITWISE_OR, OP_BITWISE_XOR,
            OP_ASSIGN, OP_EQUAL, OP_NOT_EQUAL, OP_LESS, OP_LESS_EQUAL, OP_GREATER, OP_GREATER_EQUAL,
        GROUP_OP_INFIX_END,
        // literals
        GROUP_LIT_START,
            LIT_STRING, LIT_NUMBER, LIT_CHAR,
        GROUP_LIT_END,
        // separators
        OPEN_PAREN, CLOSE_PAREN,
        OPEN_SQUARE_BRACKET, CLOSE_SQUARE_BRACKET,
        OPEN_CURLY_BRACKET, CLOSE_CURLY_BRACKET,
        COMMA, SEMICOLON, DOT
    }
    public static enum TypeGroup {
        UNKNOWN, TYPE, LITERAL, OP_PREFIX, OP_POSTFIX, OP_INFIX
    }
    public String textRepresentation = "";
    public Type type = Type.UNKNOWN;
    public TypeGroup getTypeGroup() {
        // types
        if (type.ordinal() > Type.GROUP_TYPES_START.ordinal() && type.ordinal() < Type.GROUP_TYPES_END.ordinal()) {
            return TypeGroup.TYPE;
        }
        // literals
        if (type.ordinal() > Type.GROUP_LIT_START.ordinal() && type.ordinal() < Type.GROUP_LIT_END.ordinal()) {
            return TypeGroup.LITERAL;
        }
        // prefix operators
        if (type.ordinal() > Type.GROUP_OP_PREFIX_START.ordinal() && type.ordinal() < Type.GROUP_OP_PREFIX_END.ordinal()) {
            return TypeGroup.OP_PREFIX;
        }
        // postfix operators
        if (type.ordinal() > Type.GROUP_OP_POSTFIX_START.ordinal() && type.ordinal() < Type.GROUP_OP_POSTFIX_END.ordinal()) {
            return TypeGroup.OP_POSTFIX;
        }
        // infix operators
        if (type.ordinal() > Type.GROUP_OP_INFIX_START.ordinal() && type.ordinal() < Type.GROUP_OP_INFIX_END.ordinal()) {
            return TypeGroup.OP_INFIX;
        }

        
        return TypeGroup.UNKNOWN;
    }
}
