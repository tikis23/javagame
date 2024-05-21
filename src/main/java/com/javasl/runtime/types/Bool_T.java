package com.javasl.runtime.types;

public class Bool_T extends Type_T {
    public Bool_T() {
        m_value = false;
    }
    public Bool_T(boolean value) {
        m_value = value;
    }

    // value
    public void setValue(Object value) {
        if (value instanceof Number)
            m_value = ((Number)value).longValue() != 0;
        else if (value instanceof Boolean)
            m_value = (Boolean)value;
        else
            throw new RuntimeException("Invalid boolean value type");
    }
    public Object getValue() {
        return m_value ? 1L : 0L;
    }
    public void assignValueFromString(String value) {
        setValue(Boolean.parseBoolean(value));
    }

    // ops
    public Type_T op_multiply(Type_T other) {
        Long lhs = m_value ? 1L : 0L;
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Int64_T();
        val.setValue(lhs * rhs);
        return val;
    }
    public Type_T op_divide(Type_T other) {
        Long lhs = m_value ? 1L : 0L;
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Int64_T();
        val.setValue(lhs / rhs);
        return val;
    }
    public Type_T op_modulo(Type_T other) {
        Long lhs = m_value ? 1L : 0L;
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Int64_T();
        val.setValue(lhs % rhs);
        return val;
    }
    public Type_T op_plus(Type_T other) {
        Long lhs = m_value ? 1L : 0L;
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Int64_T();
        val.setValue(lhs + rhs);
        return val;
    }
    public Type_T op_minus(Type_T other) {
        Long lhs = m_value ? 1L : 0L;
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Int64_T();
        val.setValue(lhs - rhs);
        return val;
    }
    public Type_T op_shift_left(Type_T other) {
        Long lhs = m_value ? 1L : 0L;
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Int64_T();
        val.setValue(lhs << rhs);
        return val;
    }
    public Type_T op_shift_right(Type_T other) {
        Long lhs = m_value ? 1L : 0L;
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Int64_T();
        val.setValue(lhs >> rhs);
        return val;
    }
    public Type_T op_less(Type_T other) {
        Long lhs = m_value ? 1L : 0L;
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Bool_T();
        val.setValue(lhs < rhs);
        return val;
    }
    public Type_T op_less_equal(Type_T other) {
        Long lhs = m_value ? 1L : 0L;
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Bool_T();
        val.setValue(lhs <= rhs);
        return val;
    }
    public Type_T op_greater(Type_T other) {
        Long lhs = m_value ? 1L : 0L;
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Bool_T();
        val.setValue(lhs > rhs);
        return val;
    }
    public Type_T op_greater_equal(Type_T other) {
        Long lhs = m_value ? 1L : 0L;
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Bool_T();
        val.setValue(lhs >= rhs);
        return val;
    }
    public Type_T op_equal(Type_T other) {
        Long lhs = m_value ? 1L : 0L;
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Bool_T();
        val.setValue(lhs == rhs);
        return val;
    }
    public Type_T op_not_equal(Type_T other) {
        Long lhs = m_value ? 1L : 0L;
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Bool_T();
        val.setValue(lhs != rhs);
        return val;
    }
    public Type_T op_bitwise_and(Type_T other) {
        Long lhs = m_value ? 1L : 0L;
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Int64_T();
        val.setValue(lhs & rhs);
        return val;
    }
    public Type_T op_bitwise_xor(Type_T other) {
        Long lhs = m_value ? 1L : 0L;
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Int64_T();
        val.setValue(lhs ^ rhs);
        return val;
    }
    public Type_T op_bitwise_or(Type_T other) {
        Long lhs = m_value ? 1L : 0L;
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Int64_T();
        val.setValue(lhs | rhs);
        return val;
    }
    public Type_T op_logical_and(Type_T other) {
        Long lhs = m_value ? 1L : 0L;
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Bool_T();
        val.setValue((lhs != 0) && (rhs != 0));
        return val;
    }
    public Type_T op_logical_or(Type_T other) {
        Long lhs = m_value ? 1L : 0L;
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Bool_T();
        val.setValue((lhs != 0) || (rhs != 0));
        return val;
    }

    // misc
    public Type_T getTypeInstance() {
        return new Bool_T();
    }
    public String toString() {
        return "bool(" + Boolean.toString(m_value) + ")";
    }

    protected boolean m_value;
}
