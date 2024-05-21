package com.javasl.runtime.types;

public class Uint8_T extends UnsignedInt_T<Byte> {
    public Uint8_T() {
        super((byte) 0);
    }
    public Uint8_T(long value) {
        super((byte)value);
    }
    
    // value
    public void setValue(Object value) {
        m_value = ((Number)value).byteValue();
    }

    // misc
    public Type_T getTypeInstance() {
        return new Uint8_T();
    }
    public String toString() {
        return "uint8(" + Integer.toUnsignedString(Byte.toUnsignedInt((Byte)getValue())) + ")";
    }
}
