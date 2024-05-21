package com.javasl.runtime.types;

public class Float_T extends Type_T {
    public Float_T() {
        m_value = 0;
    }
    public Float_T(float value) {
        m_value = value;
    }

    // value
    public void setValueFrom(Type_T other) {
        m_value = ((Number)other.getValue()).floatValue();
    }
    public void setValue(Object value) {
        m_value = ((Number)value).floatValue();
    }
    public Object getValue() {
        return m_value;
    }
    public void assignValueFromString(String value) {
        setValue(Float.parseFloat(value));
    }

    // ops 
    public Type_T op_multiply(Type_T other) {
        Float lhs = ((Number)m_value).floatValue();
        Float rhs = ((Number)other.getValue()).floatValue();
        Type_T val = new Float_T();
        val.setValue(lhs * rhs);
        return val;
    }
    public Type_T op_divide(Type_T other) {
        Float lhs = ((Number)m_value).floatValue();
        Float rhs = ((Number)other.getValue()).floatValue();
        Type_T val = new Float_T();
        val.setValue(lhs / rhs);
        return val;
    }
    public Type_T op_modulo(Type_T other) {
        Float lhs = ((Number)m_value).floatValue();
        Float rhs = ((Number)other.getValue()).floatValue();
        Type_T val = new Float_T();
        val.setValue(lhs % rhs);
        return val;
    }
    public Type_T op_plus(Type_T other) {
        Float lhs = ((Number)m_value).floatValue();
        Float rhs = ((Number)other.getValue()).floatValue();
        Type_T val = new Float_T();
        val.setValue(lhs + rhs);
        return val;
    }
    public Type_T op_minus(Type_T other) {
        Float lhs = ((Number)m_value).floatValue();
        Float rhs = ((Number)other.getValue()).floatValue();
        Type_T val = new Float_T();
        val.setValue(lhs - rhs);
        return val;
    }
    public Type_T op_shift_left(Type_T other) {
        throw new UnsupportedOperationException("Unsupported operation on float 'op_shift_left'");
    }
    public Type_T op_shift_right(Type_T other) {
        throw new UnsupportedOperationException("Unsupported operation on float 'op_shift_right'");
    }
    public Type_T op_less(Type_T other) {
        Float lhs = ((Number)m_value).floatValue();
        Float rhs = ((Number)other.getValue()).floatValue();
        Type_T val = new Bool_T();
        val.setValue(lhs < rhs);
        return val;
    }
    public Type_T op_less_equal(Type_T other) {
        Float lhs = ((Number)m_value).floatValue();
        Float rhs = ((Number)other.getValue()).floatValue();
        Type_T val = new Bool_T();
        val.setValue(lhs <= rhs);
        return val;
    }
    public Type_T op_greater(Type_T other) {
        Float lhs = ((Number)m_value).floatValue();
        Float rhs = ((Number)other.getValue()).floatValue();
        Type_T val = new Bool_T();
        val.setValue(lhs > rhs);
        return val;
    }
    public Type_T op_greater_equal(Type_T other) {
        Float lhs = ((Number)m_value).floatValue();
        Float rhs = ((Number)other.getValue()).floatValue();
        Type_T val = new Bool_T();
        val.setValue(lhs >= rhs);
        return val;
    }
    public Type_T op_equal(Type_T other) {
        Float lhs = ((Number)m_value).floatValue();
        Float rhs = ((Number)other.getValue()).floatValue();
        Type_T val = new Bool_T();
        val.setValue(lhs == rhs);
        return val;
    }
    public Type_T op_not_equal(Type_T other) {
        Float lhs = ((Number)m_value).floatValue();
        Float rhs = ((Number)other.getValue()).floatValue();
        Type_T val = new Bool_T();
        val.setValue(lhs != rhs);
        return val;
    }
    public Type_T op_bitwise_and(Type_T other) {
        throw new UnsupportedOperationException("Unsupported operation on float 'op_btiwise_and'");
    }
    public Type_T op_bitwise_xor(Type_T other) {
        throw new UnsupportedOperationException("Unsupported operation on float 'op_btiwise_xor'");
    }
    public Type_T op_bitwise_or(Type_T other) {
        throw new UnsupportedOperationException("Unsupported operation on float 'op_btiwise_or'");
    }
    public Type_T op_logical_and(Type_T other) {
        Float lhs = ((Number)m_value).floatValue();
        Float rhs = ((Number)other.getValue()).floatValue();
        Type_T val = new Bool_T();
        val.setValue((lhs != 0) && (rhs != 0));
        return val;
    }
    public Type_T op_logical_or(Type_T other) {
        Float lhs = ((Number)m_value).floatValue();
        Float rhs = ((Number)other.getValue()).floatValue();
        Type_T val = new Bool_T();
        val.setValue((lhs != 0) || (rhs != 0));
        return val;
    }

    // misc
    public Type_T getTypeInstance() {
        return new Float_T();
    }
    public String toString() {
        return "float(" + getValue() + ")";
    }

    protected float m_value;
}
