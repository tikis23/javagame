package com.javasl.runtime.types;

public class Int64_T extends SignedInt_T<Long> {
    public Int64_T() {
        super((long) 0);
    }
    public Int64_T(long value) {
        super(value);
    }
    
    // value
    public void setValue(Object value) {
        m_value = ((Number)value).longValue();
    }

    // misc
    public Type_T getTypeInstance() {
        return new Int64_T();
    }
    public String toString() {
        return "int64(" + getValue() + ")";
    }
}