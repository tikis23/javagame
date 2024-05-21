package com.javasl.runtime.types;

import java.util.HashMap;

import com.javasl.compiler.Token;

public abstract class Type_T {
    // value
    public abstract void assignValueFromString(String value);
    public abstract Object getValue();
    public abstract void setValue(Object value);

    // ops
    public abstract Type_T op_multiply(Type_T other);
    public abstract Type_T op_divide(Type_T other);
    public abstract Type_T op_modulo(Type_T other);
    public abstract Type_T op_plus(Type_T other);
    public abstract Type_T op_minus(Type_T other);
    public abstract Type_T op_shift_left(Type_T other);
    public abstract Type_T op_shift_right(Type_T other);
    public abstract Type_T op_less(Type_T other);
    public abstract Type_T op_less_equal(Type_T other);
    public abstract Type_T op_greater(Type_T other);
    public abstract Type_T op_greater_equal(Type_T other);
    public abstract Type_T op_equal(Type_T other);
    public abstract Type_T op_not_equal(Type_T other);
    public abstract Type_T op_bitwise_and(Type_T other);
    public abstract Type_T op_bitwise_xor(Type_T other);
    public abstract Type_T op_bitwise_or(Type_T other);
    public abstract Type_T op_logical_and(Type_T other);
    public abstract Type_T op_logical_or(Type_T other);

    // misc
    public Type_T copy() {
        Type_T val = getTypeInstance();
        val.setValue(getValue());
        return val;
    }
    public abstract String toString();
    public abstract Type_T getTypeInstance();
    public static Type_T fromToken(Token token) {
        if (token.type == Token.Type.LIT_NUMBER) {
            if (token.textRepresentation.contains(".")) return new Double_T();
            else return new Int64_T();
        } else {
            Type_T t = types.get(token.type);
            if (t == null) throw new IllegalArgumentException("Unsupported type: " + token.type + " " + token.textRepresentation);
            return t.getTypeInstance();
        }
    }
    private static final HashMap<Token.Type, Type_T> types = new HashMap<Token.Type, Type_T>() {{
        put(Token.Type.T_VOID,   new Void_T());
        put(Token.Type.T_INT8,   new Int8_T());
        put(Token.Type.T_INT16,  new Int16_T());
        put(Token.Type.T_INT32,  new Int32_T());
        put(Token.Type.T_INT64,  new Int64_T());
        put(Token.Type.T_UINT8,  new Uint8_T());
        put(Token.Type.T_UINT16, new Uint16_T());
        put(Token.Type.T_UINT32, new Uint32_T());
        put(Token.Type.T_UINT64, new Uint64_T());
        put(Token.Type.T_FLOAT,  new Float_T());
        put(Token.Type.T_DOUBLE, new Double_T());
        put(Token.Type.T_BOOL,   new Bool_T());
        put(Token.Type.T_STRING, null);
        put(Token.Type.T_CHAR,   null);
        put(Token.Type.LIT_STRING, null);
        put(Token.Type.LIT_CHAR, null);
        put(Token.Type.KW_TRUE, new Bool_T());
        put(Token.Type.KW_FALSE, new Bool_T());
    }};
}
