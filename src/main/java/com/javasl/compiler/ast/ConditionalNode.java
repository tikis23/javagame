package com.javasl.compiler.ast;

public class ConditionalNode extends AST {
    public AST condition;
    public AST trueBody;
    public AST falseBody;
}
