package com.javasl.runtime.types;

// do not use as a type, only as a placeholder for functions that do not care about the type (external mostly)
public class Any_T extends Type_T {
    public void assignValueFromString(String value) {
        throw new UnsupportedOperationException("Unsupported method 'assignValueFromString'");
    }
    public Object getValue() {
        return m_value;
    }
    public void setValue(Object value) {
        if (value instanceof Number) {
            m_value = new Int64_T(((Number)value).longValue());
        } else if (value instanceof Boolean) {
            m_value = new Bool_T((Boolean)value);
        }
    }

    public Type_T op_multiply(Type_T other) {
        Type_T lhs = (Type_T)m_value;
        return lhs.op_multiply(other);
    }
    public Type_T op_divide(Type_T other) {
        Type_T lhs = (Type_T)m_value;
        return lhs.op_divide(other);
    }
    public Type_T op_modulo(Type_T other) {
        Type_T lhs = (Type_T)m_value;
        return lhs.op_modulo(other);
    }
    public Type_T op_plus(Type_T other) {
        Type_T lhs = (Type_T)m_value;
        return lhs.op_plus(other);
    }
    public Type_T op_minus(Type_T other) {
        Type_T lhs = (Type_T)m_value;
        return lhs.op_minus(other);
    }
    public Type_T op_shift_left(Type_T other) {
        Type_T lhs = (Type_T)m_value;
        return lhs.op_shift_left(other);
    }
    public Type_T op_shift_right(Type_T other) {
        Type_T lhs = (Type_T)m_value;
        return lhs.op_shift_right(other);
    }
    public Type_T op_less(Type_T other) {
        Type_T lhs = (Type_T)m_value;
        return lhs.op_less(other);
    }
    public Type_T op_less_equal(Type_T other) {
        Type_T lhs = (Type_T)m_value;
        return lhs.op_less_equal(other);
    }
    public Type_T op_greater(Type_T other) {
        Type_T lhs = (Type_T)m_value;
        return lhs.op_greater(other);
    }
    public Type_T op_greater_equal(Type_T other) {
        Type_T lhs = (Type_T)m_value;
        return lhs.op_greater_equal(other);
    }
    public Type_T op_equal(Type_T other) {
        Type_T lhs = (Type_T)m_value;
        return lhs.op_equal(other);
    }
    public Type_T op_not_equal(Type_T other) {
        Type_T lhs = (Type_T)m_value;
        return lhs.op_not_equal(other);
    }
    public Type_T op_bitwise_and(Type_T other) {
        Type_T lhs = (Type_T)m_value;
        return lhs.op_bitwise_and(other);
    }
    public Type_T op_bitwise_xor(Type_T other) {
        Type_T lhs = (Type_T)m_value;
        return lhs.op_bitwise_xor(other);
    }
    public Type_T op_bitwise_or(Type_T other) {
        Type_T lhs = (Type_T)m_value;
        return lhs.op_bitwise_or(other);
    }
    public Type_T op_logical_and(Type_T other) {
        Type_T lhs = (Type_T)m_value;
        return lhs.op_logical_and(other);
    }
    public Type_T op_logical_or(Type_T other) {
        Type_T lhs = (Type_T)m_value;
        return lhs.op_logical_or(other);
    }


    public String toString() {
        return "any";
    }
    public Type_T getTypeInstance() {
        return new Any_T();
    }

    private Object m_value;
}
