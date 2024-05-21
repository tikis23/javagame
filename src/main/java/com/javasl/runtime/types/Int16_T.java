package com.javasl.runtime.types;

public class Int16_T extends SignedInt_T<Short> {
    public Int16_T() {
        super((short) 0);
    }
    public Int16_T(long value) {
        super((short)value);
    }
    
    // value
    public void setValue(Object value) {
        m_value = ((Number)value).shortValue();
    }

    // misc
    public Type_T getTypeInstance() {
        return new Int16_T();
    }
    public String toString() {
        return "int16(" + getValue() + ")";
    }
}
