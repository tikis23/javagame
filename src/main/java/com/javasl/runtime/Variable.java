package com.javasl.runtime;

import com.javasl.runtime.types.Type_T;

public class Variable {
    public String name;
    public Type_T value;

    public Variable(String name, Type_T value) {
        this.name = name;
        this.value = value;
    }

    public Variable copy() {
        return new Variable(name, value.copy());
    }

    public String toString() {
        return name + "-" + value.toString();
    }

    public int hashCode() {
        return name.hashCode();
    }
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Variable) {
            Variable other = (Variable) obj;
            return name.equals(other.name);
        }
        return false;
    }
}
