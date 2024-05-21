package com.javasl.runtime.types;

public class Uint32_T extends UnsignedInt_T<Integer> {
    public Uint32_T() {
        super((int) 0);
    }
    public Uint32_T(long value) {
        super((int)value);
    }
    
    // value
    public void setValue(Object value) {
        m_value = ((Number)value).intValue();
    }

    // misc
    public Type_T getTypeInstance() {
        return new Uint32_T();
    }
    public String toString() {
        return "uint32(" + Integer.toUnsignedString((Integer)getValue()) + ")";
    }
}
