package com.javasl.runtime;

import java.util.ArrayList;

import com.javasl.runtime.types.Type_T;

public class FunctionVariable extends Variable {
    public FunctionVariable(String name, Type_T retType, ArrayList<Variable> params, int ip) {
        super(name, retType);
        this.params = params;
        this.ip = ip;
    }
    public ArrayList<Variable> params;
    public int ip;

    public Variable copy() {
        ArrayList<Variable> newParams = new ArrayList<>();
        for (Variable param : params) {
            newParams.add(param.copy());
        }
        return new FunctionVariable(name, value.copy(), newParams, ip);
    }

    public String toString() {
        return "func-" + name + "-" + value.toString() + "-" + ip;
    }
}
