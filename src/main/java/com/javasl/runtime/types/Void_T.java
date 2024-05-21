package com.javasl.runtime.types;

public class Void_T extends Type_T {
    public void assignValueFromString(String value) {
        throw new UnsupportedOperationException("Unsupported method 'assignValueFromString'");
    }
    public Object getValue() {
        throw new UnsupportedOperationException("Unsupported method 'getValue'");
    }
    public void setValue(Object value) {
        throw new UnsupportedOperationException("Unsupported method 'setValue'");
    }

    public Type_T op_multiply(Type_T other) {
        throw new UnsupportedOperationException("Unsupported method 'op_multiply'");
    }
    public Type_T op_divide(Type_T other) {
        throw new UnsupportedOperationException("Unsupported method 'op_divide'");
    }
    public Type_T op_modulo(Type_T other) {
        throw new UnsupportedOperationException("Unsupported method 'op_modulo'");
    }
    public Type_T op_plus(Type_T other) {
        throw new UnsupportedOperationException("Unsupported method 'op_plus'");
    }
    public Type_T op_minus(Type_T other) {
        throw new UnsupportedOperationException("Unsupported method 'op_minus'");
    }
    public Type_T op_shift_left(Type_T other) {
        throw new UnsupportedOperationException("Unsupported method 'op_shift_left'");
    }
    public Type_T op_shift_right(Type_T other) {
        throw new UnsupportedOperationException("Unsupported method 'op_shift_right'");
    }
    public Type_T op_less(Type_T other) {
        throw new UnsupportedOperationException("Unsupported method 'op_less'");
    }
    public Type_T op_less_equal(Type_T other) {
        throw new UnsupportedOperationException("Unsupported method 'op_less_equal'");
    }
    public Type_T op_greater(Type_T other) {
        throw new UnsupportedOperationException("Unsupported method 'op_greater'");
    }
    public Type_T op_greater_equal(Type_T other) {
        throw new UnsupportedOperationException("Unsupported method 'op_greater_equal'");
    }
    public Type_T op_equal(Type_T other) {
        throw new UnsupportedOperationException("Unsupported method 'op_equal'");
    }
    public Type_T op_not_equal(Type_T other) {
        throw new UnsupportedOperationException("Unsupported method 'op_not_equal'");
    }
    public Type_T op_bitwise_and(Type_T other) {
        throw new UnsupportedOperationException("Unsupported method 'op_bitwise_and'");
    }
    public Type_T op_bitwise_xor(Type_T other) {
        throw new UnsupportedOperationException("Unsupported method 'op_bitwise_xor'");
    }
    public Type_T op_bitwise_or(Type_T other) {
        throw new UnsupportedOperationException("Unsupported method 'op_bitwise_or'");
    }
    public Type_T op_logical_and(Type_T other) {
        throw new UnsupportedOperationException("Unsupported method 'op_logical_and'");
    }
    public Type_T op_logical_or(Type_T other) {
        throw new UnsupportedOperationException("Unsupported method 'op_logical_or'");
    }


    public String toString() {
        return "void";
    }
    public Type_T getTypeInstance() {
        return new Void_T();
    }
}
