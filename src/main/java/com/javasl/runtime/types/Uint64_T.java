package com.javasl.runtime.types;

public class Uint64_T extends UnsignedInt_T<Long> {
    public Uint64_T() {
        super((long) 0);
    }
    public Uint64_T(long value) {
        super((long)value);
    }
    
    // value
    public void setValue(Object value) {
        m_value = ((Number)value).longValue();
    }

    // misc
    public Type_T getTypeInstance() {
        return new Uint64_T();
    }
    public String toString() {
        return "uint64(" + Long.toUnsignedString((Long)getValue()) + ")";
    }
}
