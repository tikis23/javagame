package com.javasl.compiler.ast;

public class ForLoopNode extends AST {
    public AST init;
    public AST condition;
    public AST increment;
    public AST body;
}
