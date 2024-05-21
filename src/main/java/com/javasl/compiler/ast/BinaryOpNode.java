package com.javasl.compiler.ast;

import com.javasl.compiler.Token;

public class BinaryOpNode extends AST {
    public AST lhs;
    public AST rhs;
    public Token operator;
}
