package com.javasl.runtime.types;

public class Int8_T extends SignedInt_T<Byte> {
    public Int8_T() {
        super((byte) 0);
    }
    public Int8_T(long value) {
        super((byte)value);
    }
    
    // value
    public void setValue(Object value) {
        m_value = ((Number)value).byteValue();
    }

    // misc
    public Type_T getTypeInstance() {
        return new Int8_T();
    }
    public String toString() {
        return "int8(" + getValue() + ")";
    }
}
