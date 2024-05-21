package com.javasl.runtime.types;

public class Int32_T extends SignedInt_T<Integer> {
    public Int32_T() {
        super((int) 0);
    }
    public Int32_T(long value) {
        super((int)value);
    }
    
    // value
    public void setValue(Object value) {
        m_value = ((Number)value).intValue();
    }

    // misc
    public Type_T getTypeInstance() {
        return new Int32_T();
    }
    public String toString() {
        return "int32(" + getValue() + ")";
    }
}