package com.javasl.runtime.types;

public abstract class UnsignedInt_T<T> extends Type_T {
    public UnsignedInt_T(T value) {
        m_value = value;
    }

    // value
    public Object getValue() {
        return m_value;
    }
    public void assignValueFromString(String value) {
        setValue(Long.parseUnsignedLong(value));
    }

    // ops 
    public Type_T op_multiply(Type_T other) {
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Uint64_T();
        val.setValue(lhs * rhs);
        return val;
    }
    public Type_T op_divide(Type_T other) {
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Uint64_T();
        val.setValue(Long.divideUnsigned(lhs, rhs));
        return val;
    }
    public Type_T op_modulo(Type_T other) {
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Uint64_T();
        val.setValue(Long.remainderUnsigned(lhs, rhs));
        return val;
    }
    public Type_T op_plus(Type_T other) {
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Uint64_T();
        val.setValue(lhs + rhs);
        return val;
    }
    public Type_T op_minus(Type_T other) {
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Uint64_T();
        val.setValue(lhs - rhs);
        return val;
    }
    public Type_T op_shift_left(Type_T other) {
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Uint64_T();
        val.setValue(lhs << rhs);
        return val;
    }
    public Type_T op_shift_right(Type_T other) {
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Uint64_T();
        val.setValue(lhs >>> rhs);
        return val;
    }
    public Type_T op_less(Type_T other) {
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Bool_T();
        val.setValue(Long.compareUnsigned(lhs, rhs) < 0);
        return val;
    }
    public Type_T op_less_equal(Type_T other) {
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Bool_T();
        val.setValue(Long.compareUnsigned(lhs, rhs) <= 0);
        return val;
    }
    public Type_T op_greater(Type_T other) {
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Bool_T();
        val.setValue(Long.compareUnsigned(lhs, rhs) > 0);
        return val;
    }
    public Type_T op_greater_equal(Type_T other) {
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Bool_T();
        val.setValue(Long.compareUnsigned(lhs, rhs) >= 0);
        return val;
    }
    public Type_T op_equal(Type_T other) {
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Bool_T();
        val.setValue(lhs == rhs);
        return val;
    }
    public Type_T op_not_equal(Type_T other) {
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Bool_T();
        val.setValue(lhs != rhs);
        return val;
    }
    public Type_T op_bitwise_and(Type_T other) {
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Uint64_T();
        val.setValue(lhs & rhs);
        return val;
    }
    public Type_T op_bitwise_xor(Type_T other) {
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Uint64_T();
        val.setValue(lhs ^ rhs);
        return val;
    }
    public Type_T op_bitwise_or(Type_T other) {
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Uint64_T();
        val.setValue(lhs | rhs);
        return val;
    }
    public Type_T op_logical_and(Type_T other) {
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Bool_T();
        val.setValue((lhs != 0) && (rhs != 0));
        return val;
    }
    public Type_T op_logical_or(Type_T other) {
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Bool_T();
        val.setValue((lhs != 0) || (rhs != 0));
        return val;
    }

    protected T m_value;
}
