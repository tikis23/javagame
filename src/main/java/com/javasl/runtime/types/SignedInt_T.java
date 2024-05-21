package com.javasl.runtime.types;

public abstract class SignedInt_T<T> extends Type_T {
    public SignedInt_T(T value) {
        m_value = value;
    }

    // value
    public Object getValue() {
        return m_value;
    }
    public void assignValueFromString(String value) {
        setValue(Long.parseLong(value));
    }

    // ops 
    public Type_T op_multiply(Type_T other) {
        if (other instanceof Float_T) {
            Float_T thisFloat = (Float_T)other.getTypeInstance();
            thisFloat.setValueFrom(this);
            return thisFloat.op_multiply(other);
        }
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Int64_T();
        val.setValue(lhs * rhs);
        return val;
    }
    public Type_T op_divide(Type_T other) {
        if (other instanceof Float_T) {
            Float_T thisFloat = (Float_T)other.getTypeInstance();
            thisFloat.setValueFrom(this);
            return thisFloat.op_divide(other);
        }
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Int64_T();
        val.setValue(lhs / rhs);
        return val;
    }
    public Type_T op_modulo(Type_T other) {
        if (other instanceof Float_T) {
            Float_T thisFloat = (Float_T)other.getTypeInstance();
            thisFloat.setValueFrom(this);
            return thisFloat.op_modulo(other);
        }
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Int64_T();
        val.setValue(lhs % rhs);
        return val;
    }
    public Type_T op_plus(Type_T other) {
        if (other instanceof Float_T) {
            Float_T thisFloat = (Float_T)other.getTypeInstance();
            thisFloat.setValueFrom(this);
            return thisFloat.op_plus(other);
        }
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Int64_T();
        val.setValue(lhs + rhs);
        return val;
    }
    public Type_T op_minus(Type_T other) {
        if (other instanceof Float_T) {
            Float_T thisFloat = (Float_T)other.getTypeInstance();
            thisFloat.setValueFrom(this);
            return thisFloat.op_minus(other);
        }
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Int64_T();
        val.setValue(lhs - rhs);
        return val;
    }
    public Type_T op_shift_left(Type_T other) {
        if (other instanceof Float_T) {
            Float_T thisFloat = (Float_T)other.getTypeInstance();
            thisFloat.setValueFrom(this);
            return thisFloat.op_shift_left(other);
        }
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Int64_T();
        val.setValue(lhs << rhs);
        return val;
    }
    public Type_T op_shift_right(Type_T other) {
        if (other instanceof Float_T) {
            Float_T thisFloat = (Float_T)other.getTypeInstance();
            thisFloat.setValueFrom(this);
            return thisFloat.op_shift_right(other);
        }
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Int64_T();
        val.setValue(lhs >> rhs);
        return val;
    }
    public Type_T op_less(Type_T other) {
        if (other instanceof Float_T) {
            Float_T thisFloat = (Float_T)other.getTypeInstance();
            thisFloat.setValueFrom(this);
            return thisFloat.op_less(other);
        }
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Bool_T();
        val.setValue(lhs < rhs);
        return val;
    }
    public Type_T op_less_equal(Type_T other) {
        if (other instanceof Float_T) {
            Float_T thisFloat = (Float_T)other.getTypeInstance();
            thisFloat.setValueFrom(this);
            return thisFloat.op_less_equal(other);
        }
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Bool_T();
        val.setValue(lhs <= rhs);
        return val;
    }
    public Type_T op_greater(Type_T other) {
        if (other instanceof Float_T) {
            Float_T thisFloat = (Float_T)other.getTypeInstance();
            thisFloat.setValueFrom(this);
            return thisFloat.op_greater(other);
        }
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Bool_T();
        val.setValue(lhs > rhs);
        return val;
    }
    public Type_T op_greater_equal(Type_T other) {
        if (other instanceof Float_T) {
            Float_T thisFloat = (Float_T)other.getTypeInstance();
            thisFloat.setValueFrom(this);
            return thisFloat.op_greater_equal(other);
        }
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Bool_T();
        val.setValue(lhs >= rhs);
        return val;
    }
    public Type_T op_equal(Type_T other) {
        if (other instanceof Float_T) {
            Float_T thisFloat = (Float_T)other.getTypeInstance();
            thisFloat.setValueFrom(this);
            return thisFloat.op_equal(other);
        }
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Bool_T();
        val.setValue(lhs == rhs);
        return val;
    }
    public Type_T op_not_equal(Type_T other) {
        if (other instanceof Float_T) {
            Float_T thisFloat = (Float_T)other.getTypeInstance();
            thisFloat.setValueFrom(this);
            return thisFloat.op_not_equal(other);
        }
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Bool_T();
        val.setValue(lhs != rhs);
        return val;
    }
    public Type_T op_bitwise_and(Type_T other) {
        if (other instanceof Float_T) {
            Float_T thisFloat = (Float_T)other.getTypeInstance();
            thisFloat.setValueFrom(this);
            return thisFloat.op_bitwise_and(other);
        }
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Int64_T();
        val.setValue(lhs & rhs);
        return val;
    }
    public Type_T op_bitwise_xor(Type_T other) {
        if (other instanceof Float_T) {
            Float_T thisFloat = (Float_T)other.getTypeInstance();
            thisFloat.setValueFrom(this);
            return thisFloat.op_bitwise_xor(other);
        }
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Int64_T();
        val.setValue(lhs ^ rhs);
        return val;
    }
    public Type_T op_bitwise_or(Type_T other) {
        if (other instanceof Float_T) {
            Float_T thisFloat = (Float_T)other.getTypeInstance();
            thisFloat.setValueFrom(this);
            return thisFloat.op_bitwise_or(other);
        }
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Int64_T();
        val.setValue(lhs | rhs);
        return val;
    }
    public Type_T op_logical_and(Type_T other) {
        if (other instanceof Float_T) {
            Float_T thisFloat = (Float_T)other.getTypeInstance();
            thisFloat.setValueFrom(this);
            return thisFloat.op_logical_and(other);
        }
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Bool_T();
        val.setValue((lhs != 0) && (rhs != 0));
        return val;
    }
    public Type_T op_logical_or(Type_T other) {
        if (other instanceof Float_T) {
            Float_T thisFloat = (Float_T)other.getTypeInstance();
            thisFloat.setValueFrom(this);
            return thisFloat.op_logical_or(other);
        }
        Long lhs = ((Number)m_value).longValue();
        Long rhs = ((Number)other.getValue()).longValue();
        Type_T val = new Bool_T();
        val.setValue((lhs != 0) || (rhs != 0));
        return val;
    }

    protected T m_value;
}
