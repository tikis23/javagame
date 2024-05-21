package com.javasl.runtime.types;

public class Uint16_T extends UnsignedInt_T<Short> {
    public Uint16_T() {
        super((short) 0);
    }
    public Uint16_T(long value) {
        super((short)value);
    }
    
    // value
    public void setValue(Object value) {
        m_value = ((Number)value).shortValue();
    }

    // misc
    public Type_T getTypeInstance() {
        return new Uint16_T();
    }
    public String toString() {
        return "uint16(" + Integer.toUnsignedString(Short.toUnsignedInt((Short)getValue())) + ")";
    }
}
