package com.javasl.runtime.types;

public class Double_T extends Float_T {
    public Double_T() {
        m_value = 0;
    }
    public Double_T(double value) {
        m_value = value;
    }

    // value
    public void setValueFrom(Type_T other) {
        m_value = ((Number)other.getValue()).doubleValue();
    }
    public void setValue(Object value) {
        m_value = ((Number)value).doubleValue();
    }
    public Object getValue() {
        return m_value;
    }
    public void assignValueFromString(String value) {
        setValue(Double.parseDouble(value));
    }

    // ops 
    public Type_T op_multiply(Type_T other) {
        Double lhs = ((Number)m_value).doubleValue();
        Double rhs = ((Number)other.getValue()).doubleValue();
        Type_T val = new Double_T();
        val.setValue(lhs * rhs);
        return val;
    }
    public Type_T op_divide(Type_T other) {
        Double lhs = ((Number)m_value).doubleValue();
        Double rhs = ((Number)other.getValue()).doubleValue();
        Type_T val = new Double_T();
        val.setValue(lhs / rhs);
        return val;
    }
    public Type_T op_modulo(Type_T other) {
        Double lhs = ((Number)m_value).doubleValue();
        Double rhs = ((Number)other.getValue()).doubleValue();
        Type_T val = new Double_T();
        val.setValue(lhs % rhs);
        return val;
    }
    public Type_T op_plus(Type_T other) {
        Double lhs = ((Number)m_value).doubleValue();
        Double rhs = ((Number)other.getValue()).doubleValue();
        Type_T val = new Double_T();
        val.setValue(lhs + rhs);
        return val;
    }
    public Type_T op_minus(Type_T other) {
        Double lhs = ((Number)m_value).doubleValue();
        Double rhs = ((Number)other.getValue()).doubleValue();
        Type_T val = new Double_T();
        val.setValue(lhs - rhs);
        return val;
    }
    public Type_T op_shift_left(Type_T other) {
        throw new UnsupportedOperationException("Unsupported operation on double 'op_shift_left'");
    }
    public Type_T op_shift_right(Type_T other) {
        throw new UnsupportedOperationException("Unsupported operation on double 'op_shift_right'");
    }
    public Type_T op_less(Type_T other) {
        Double lhs = ((Number)m_value).doubleValue();
        Double rhs = ((Number)other.getValue()).doubleValue();
        Type_T val = new Bool_T();
        val.setValue(lhs < rhs);
        return val;
    }
    public Type_T op_less_equal(Type_T other) {
        Double lhs = ((Number)m_value).doubleValue();
        Double rhs = ((Number)other.getValue()).doubleValue();
        Type_T val = new Bool_T();
        val.setValue(lhs <= rhs);
        return val;
    }
    public Type_T op_greater(Type_T other) {
        Double lhs = ((Number)m_value).doubleValue();
        Double rhs = ((Number)other.getValue()).doubleValue();
        Type_T val = new Bool_T();
        val.setValue(lhs > rhs);
        return val;
    }
    public Type_T op_greater_equal(Type_T other) {
        Double lhs = ((Number)m_value).doubleValue();
        Double rhs = ((Number)other.getValue()).doubleValue();
        Type_T val = new Bool_T();
        val.setValue(lhs >= rhs);
        return val;
    }
    public Type_T op_equal(Type_T other) {
        Double lhs = ((Number)m_value).doubleValue();
        Double rhs = ((Number)other.getValue()).doubleValue();
        Type_T val = new Bool_T();
        val.setValue(lhs == rhs);
        return val;
    }
    public Type_T op_not_equal(Type_T other) {
        Double lhs = ((Number)m_value).doubleValue();
        Double rhs = ((Number)other.getValue()).doubleValue();
        Type_T val = new Bool_T();
        val.setValue(lhs != rhs);
        return val;
    }
    public Type_T op_bitwise_and(Type_T other) {
        throw new UnsupportedOperationException("Unsupported operation on double 'op_btiwise_and'");
    }
    public Type_T op_bitwise_xor(Type_T other) {
        throw new UnsupportedOperationException("Unsupported operation on double 'op_btiwise_xor'");
    }
    public Type_T op_bitwise_or(Type_T other) {
        throw new UnsupportedOperationException("Unsupported operation on double 'op_btiwise_or'");
    }
    public Type_T op_logical_and(Type_T other) {
        Double lhs = ((Number)m_value).doubleValue();
        Double rhs = ((Number)other.getValue()).doubleValue();
        Type_T val = new Bool_T();
        val.setValue((lhs != 0) && (rhs != 0));
        return val;
    }
    public Type_T op_logical_or(Type_T other) {
        Double lhs = ((Number)m_value).doubleValue();
        Double rhs = ((Number)other.getValue()).doubleValue();
        Type_T val = new Bool_T();
        val.setValue((lhs != 0) || (rhs != 0));
        return val;
    }

    // misc
    public Type_T getTypeInstance() {
        return new Double_T();
    }
    public String toString() {
        return "double(" + getValue() + ")";
    }

    protected double m_value;
}
