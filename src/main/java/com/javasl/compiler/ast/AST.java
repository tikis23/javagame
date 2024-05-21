package com.javasl.compiler.ast;

public abstract class AST {
    // for testing purposes
    public String toString(int depth) {
        String res = "";
        for (int i = 0; i < depth; i++) {
            res += '-';
        }
        if (this instanceof BlockNode) {
            BlockNode block = (BlockNode)this;
            res += "Block:\n";
            for (int i = 0; i < block.statements.size(); i++) {
                res += block.statements.get(i).toString(depth + 1);
            }
        } else if (this instanceof AssignmentNode) {
            AssignmentNode assignment = (AssignmentNode)this;
            res += "Assignment:\n";
            res += assignment.lhs.toString(depth + 1);
            res += assignment.rhs.toString(depth + 1);
        } else if (this instanceof DeclarationNode) {
            DeclarationNode declaration = (DeclarationNode) this;
            res += "Declaration: " + declaration.type.textRepresentation + " " + declaration.identifier.textRepresentation + "\n";
        } else if (this instanceof LiteralNode) {
            LiteralNode literal = (LiteralNode) this;
            res += "Literal: " + literal.literal.type.toString() + " " + literal.literal.textRepresentation + "\n";
        } else if (this instanceof IdentifierNode) {
            IdentifierNode identifier = (IdentifierNode) this;
            res += "Identifier: " + identifier.identifier.textRepresentation + "\n";
        } else if (this instanceof BinaryOpNode) {
            BinaryOpNode binaryOp = (BinaryOpNode) this;
            res += "BinaryOp: " + binaryOp.operator.textRepresentation + "\n";
            res += binaryOp.lhs.toString(depth + 1);
            res += binaryOp.rhs.toString(depth + 1);
        } else if (this instanceof ParamDeclarationNode) {
            ParamDeclarationNode paramDeclaration = (ParamDeclarationNode) this;
            res += "ParamDeclaration: ";
            for (int i = 0; i < paramDeclaration.declarations.size(); i++) {
                DeclarationNode declaration = paramDeclaration.declarations.get(i);
                res += declaration.type.textRepresentation + " " + declaration.identifier.textRepresentation;
                if (i < paramDeclaration.declarations.size() - 1) {
                    res += ", ";
                }
            }
            res += "\n";
        } else if (this instanceof FunctionDefNode) {
            FunctionDefNode functionDef = (FunctionDefNode) this;
            res += "FunctionDef: " + functionDef.declaration.type.textRepresentation + " " + functionDef.declaration.identifier.textRepresentation;
            res += "\n";
            res += functionDef.paramDeclaration.toString(depth + 1);
            res += functionDef.block.toString(depth + 1);
        } else if (this instanceof ReturnNode) {
            ReturnNode returnNode = (ReturnNode) this;
            res += "Return:\n";
            res += returnNode.retVal.toString(depth + 1);
        } else if (this instanceof CallParamsNode) {
            CallParamsNode callParams = (CallParamsNode) this;
            res += "CallParams:\n";
            for (int i = 0; i < callParams.params.size(); i++) {
                res += callParams.params.get(i).toString(depth + 1);
            }
        } else if (this instanceof FunctionCallNode) {
            FunctionCallNode functionCall = (FunctionCallNode) this;
            res += "FunctionCall: " + functionCall.funcName.identifier.textRepresentation + "\n";
            res += functionCall.params.toString(depth + 1);
        } else if (this instanceof ConditionalNode) {
            ConditionalNode conditional = (ConditionalNode) this;
            res += "Conditional:\n";
            res += conditional.condition.toString(depth + 1);
            res += "-TrueBody:\n";
            res += conditional.trueBody.toString(depth + 2);
            if (conditional.falseBody != null) {
                res += "-FalseBody:\n";
                res += conditional.falseBody.toString(depth + 2);
            }
        } else if (this instanceof WhileLoopNode) {
            WhileLoopNode whileLoop = (WhileLoopNode) this;
            res += "WhileLoop:\n";
            res += whileLoop.condition.toString(depth + 1);
            res += "-Body:\n";
            res += whileLoop.body.toString(depth + 2);
        } else if (this instanceof ForLoopNode) {
            ForLoopNode forLoop = (ForLoopNode) this;
            res += "ForLoop:\n";
            if (forLoop.init != null) res += forLoop.init.toString(depth + 1);
            if (forLoop.condition != null) res += forLoop.condition.toString(depth + 1);
            if (forLoop.increment != null) res += forLoop.increment.toString(depth + 1);
            res += "-Body:\n";
            res += forLoop.body.toString(depth + 2);
        
        } else if (this instanceof BreakNode) {
            res += "Break\n";
        } else if (this instanceof ContinueNode) {
            res += "Continue\n";
        } else {
            res += "Unknown";
        }
        return res;
    }
}