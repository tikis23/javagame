package com.javasl.compiler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

import com.javasl.compiler.ast.*;
import com.javasl.runtime.FunctionVariable;
import com.javasl.runtime.Statement;
import com.javasl.runtime.Variable;
import com.javasl.runtime.types.*;

public class Compiler {
    public ArrayList<Statement> compile(AST ast) throws IllegalArgumentException {
        // reset variables if someone wants to compile again with the same compiler instance
        m_backpatches = new ArrayList<>();
        m_functionIp = 0;
        m_functionVariables = new HashSet<>();
        m_inFunction = false;
        m_variableStack = new Stack<>();
        m_loopBackpatches = new Stack<>();
        m_scopeStack = new Stack<>();
        m_tempVariableCounter = 0;

        try {
            ArrayList<Statement> statements = dispatchToCompile(ast);
            backpatch(statements);
            return statements;
        } catch (Exception e) {
            throw new IllegalArgumentException("Compiler error: " + e.getMessage(), e);
        }
    }
    private ArrayList<Statement> dispatchToCompile(AST ast) throws IllegalArgumentException {
        if      (ast instanceof BlockNode) return compileBlockNode((BlockNode) ast);
        else if (ast instanceof BinaryOpNode) return compileBinaryOpNode((BinaryOpNode) ast);
        else if (ast instanceof LiteralNode) return compileLiteralNode((LiteralNode) ast);
        else if (ast instanceof AssignmentNode) return compileAssignmentNode((AssignmentNode) ast);
        else if (ast instanceof DeclarationNode) return compileDeclarationNode((DeclarationNode) ast);
        else if (ast instanceof FunctionDefNode) return compileFunctionDefinitionNode((FunctionDefNode) ast);
        else if (ast instanceof ReturnNode) return compileReturnNode((ReturnNode) ast);
        else if (ast instanceof FunctionCallNode) return compileFunctionCallNode((FunctionCallNode) ast);
        else if (ast instanceof ConditionalNode) return compileConditionalNode((ConditionalNode) ast);
        else if (ast instanceof WhileLoopNode) return compileWhileLoopNode((WhileLoopNode) ast);
        else if (ast instanceof ForLoopNode) return compileForLoopNode((ForLoopNode) ast);
        else if (ast instanceof BreakNode) return compileBreakNode((BreakNode) ast);
        else if (ast instanceof ContinueNode) return compileContinueNode((ContinueNode) ast);

        else throw new IllegalArgumentException("Unknown AST type `" + ast.getClass().getName() + "`.");
    }

    private ArrayList<Statement> compileBlockNode(BlockNode ast) {
        ArrayList<Statement> ret = new ArrayList<>();
        startScope();
        // if this is the global scope, add external functions here
        if (m_scopeStack.size() == 1) {
            // for external functions, use latest name declaration to avoid collisions
            ArrayList<ExtFuncDecl> externalFunctions = new ArrayList<>();
            for (int i = m_externalFunctions.size() - 1; i >= 0; i--) {
                ExtFuncDecl funcDecl = m_externalFunctions.get(i);
                boolean found = false;
                for (ExtFuncDecl existingDecl : externalFunctions) {
                    if (existingDecl.name.equals(funcDecl.name)) {
                        found = true;
                        break;
                    }
                }
                if (found) continue;
                externalFunctions.add(funcDecl);
            }
            for (int i = externalFunctions.size() - 1; i >= 0; i--) {
                ExtFuncDecl funcDecl = externalFunctions.get(i);
                ret.addAll(compileExternalFunction(funcDecl));
            }
        }

        // compile the block itself
        for (AST statement : ast.statements) {
            ret.addAll(dispatchToCompile(statement));
        }
        ret.addAll(endScope());
        return ret;
    }
    private ArrayList<Statement> compileExternalFunction(ExtFuncDecl funcDecl) {
        ArrayList<Statement> ret = new ArrayList<>();

        // add to scope
        FunctionVariable funcVar = new FunctionVariable(funcDecl.name, funcDecl.retType, funcDecl.params, m_functionIp + 1);
        ret.add(Statement.declareVariable(funcVar));
        m_functionIp++;
        addVariableToScope(funcVar);

        // jump past call
        ret.add(Statement.jumpRel(1));
        m_functionIp++;

        // call external function
        ret.add(Statement.externalFunctionCall(funcDecl, !(funcDecl.retType instanceof Void_T)));
        m_functionIp++;

        return ret;
    }
    private ArrayList<Statement> compileBreakNode(BreakNode ast) {
        ArrayList<Statement> ret = new ArrayList<>();
        if (m_loopBackpatches.empty()) {
            throw new IllegalArgumentException("Break statement outside of loop.");
        }
        LoopBackpatch loopBackpatch = m_loopBackpatches.peek();
        loopBackpatch.backpatches.add(new Backpatch() {{
            ip = m_functionIp;
            node = ast;
            stackClearCount = m_variableStack.size() - loopBackpatch.loopStackBreakStart;
        }});
        ret.add(Statement.breakLoop(0, 0));
        m_functionIp++;
        return ret;
    }
    private ArrayList<Statement> compileContinueNode(ContinueNode ast) {
        ArrayList<Statement> ret = new ArrayList<>();
        if (m_loopBackpatches.empty()) {
            throw new IllegalArgumentException("Continue statement outside of loop.");
        }
        LoopBackpatch loopBackpatch = m_loopBackpatches.peek();
        loopBackpatch.backpatches.add(new Backpatch() {{
            ip = m_functionIp;
            node = ast;
            stackClearCount = m_variableStack.size() - loopBackpatch.loopStackContinueStart;
        }});
        ret.add(Statement.continueLoop(0, 0));
        m_functionIp++;
        return ret;
    }
    private ArrayList<Statement> compileForLoopNode(ForLoopNode ast) {
        ArrayList<Statement> ret = new ArrayList<>();

        LoopBackpatch loopBackpatch = new LoopBackpatch();
        loopBackpatch.loopStackBreakStart = m_variableStack.size();
        m_loopBackpatches.add(loopBackpatch);
        
        // compile init
        int initPopCount = 0;
        if (ast.init != null) {
            int stackBefore = m_variableStack.size();
            ret.addAll(dispatchToCompile(ast.init));
            int stackAfter = m_variableStack.size();
            initPopCount = stackAfter - stackBefore;
        }
        // compile condition
        int conditionPopCount = 0;
        boolean conditionRel = false;
        int conditionIndex = 0;
        ArrayList<Statement> condition = null;
        if (ast.condition != null && ast.condition instanceof IdentifierNode) {
            IdentifierNode node = (IdentifierNode) ast.condition;
            Variable var = new Variable(node.identifier.textRepresentation, null);
            // check if variable exists in function scope (params + locals)(relative)
            for (Variable v : m_functionVariables) {
                if (v.equals(var)) {
                    conditionRel = true;
                }
            }
            // else check global scope (absolute)
            // check if variable exists
            if (!doesVariableExist(var)) {
                throw new IllegalArgumentException("Variable does not exist in scope: " + var.name);
            }
            conditionIndex = getVariableStackLocation(var);
            condition = new ArrayList<>();
        } else if (ast.condition != null) {
            int stackBefore = m_variableStack.size();
            condition = dispatchToCompile(ast.condition);
            int stackAfter = m_variableStack.size();
            conditionPopCount = stackAfter - stackBefore;
            conditionRel = true;
            conditionIndex = m_variableStack.size() - 1;
            if (condition == null || condition.size() == 0) {
                condition = null;
                conditionPopCount = 0;
            }
        }
        if (conditionRel) {
            conditionIndex = conditionIndex - m_variableStack.size() + 1;
        }
        if (condition != null) {
            m_functionIp++; // for the conditional jump instruction
        }
        loopBackpatch.loopStackContinueStart = m_variableStack.size();

        // compile body
        ArrayList<Statement> body = new ArrayList<>();
        int bodyPopCount = 0;
        {
            int stackBefore = m_variableStack.size();
            body = dispatchToCompile(ast.body);
            int stackAfter = m_variableStack.size();
            bodyPopCount = stackAfter - stackBefore;

            // rename variables in body to avoid conflicts in increment
            ArrayList<Variable> renamedVars = new ArrayList<>();
            for (int i = 0; i < bodyPopCount; i++) {
                Variable var = m_variableStack.peek();
                removeVariableFromScope(var);
                var.name = "0_INC_DUMMY";
                renamedVars.add(var);
            }
            for (int i = renamedVars.size() - 1; i >= 0; i--) {
                addVariableToScope(renamedVars.get(i));
            }
        }

        // create continue backpatches
        for (Backpatch backpatch : loopBackpatch.backpatches) {
            if (backpatch.node instanceof ContinueNode)
                backpatch.targetIp = m_functionIp - 1;
        }

        // compile increment
        int incrementPopCount = 0;
        ArrayList<Statement> increment = null;
        if (ast.increment != null) {
            int stackBefore = m_variableStack.size();
            increment = dispatchToCompile(ast.increment);
            int stackAfter = m_variableStack.size();
            incrementPopCount = stackAfter - stackBefore;
        }
        if (increment == null) {
            increment = new ArrayList<>();
        }

        // remove renamed + increment + condition variables from compiler stack
        for (int i = 0; i < bodyPopCount + incrementPopCount + conditionPopCount; i++) {
            Variable var = m_variableStack.peek();
            removeVariableFromScope(var);
        }

        // add clear to increment
        if (incrementPopCount + bodyPopCount + conditionPopCount > 0) {
            increment.add(Statement.clearStack(incrementPopCount + bodyPopCount + conditionPopCount));
            m_functionIp++;
        }

        // add jump to condition
        if (condition != null) {
            condition.add(Statement.jumpRelConditional(false, body.size() + increment.size() + 1, conditionRel, conditionIndex));
            // function IP increment is after compiling the condition
        }
        
        // add condition to return
        if (condition != null) {
            if (conditionRel) {
                conditionIndex = conditionIndex - m_variableStack.size() + 1;
            }
            ret.addAll(condition);
        }
        // add body to return
        ret.addAll(body);
        // add increment to return
        ret.addAll(increment);
        // add jump to return
        ret.add(Statement.jumpRel(-(increment.size() + body.size() + (condition == null ? 0 : condition.size()) + 1)));
        m_functionIp++;
        // add init clear to return
        if (initPopCount + conditionPopCount > 0) {
            ret.add(Statement.clearStack(initPopCount + conditionPopCount));
            m_functionIp++;
        }
        // clear init from compiler stack
        for (int i = 0; i < initPopCount; i++) {
            Variable var = m_variableStack.peek();
            removeVariableFromScope(var);
        }

        // create break backpatches
        for (Backpatch backpatch : loopBackpatch.backpatches) {
            if (backpatch.node instanceof BreakNode)
                backpatch.targetIp = m_functionIp - 1;
        }

        // put backpatches in array
        for (Backpatch backpatch : loopBackpatch.backpatches) {
            m_backpatches.add(backpatch);
        }
        m_loopBackpatches.pop();

        return ret;
    }
    private ArrayList<Statement> compileWhileLoopNode(WhileLoopNode ast) {
        ArrayList<Statement> ret = new ArrayList<>();

        LoopBackpatch loopBackpatch = new LoopBackpatch();
        loopBackpatch.loopStackBreakStart = m_variableStack.size();
        loopBackpatch.loopStackContinueStart = m_variableStack.size();
        m_loopBackpatches.add(loopBackpatch);

        // compile condition
        int conditionPopCount = 0;
        boolean conditionRel = false;
        int conditionIndex = 0;
        ArrayList<Statement> condition = new ArrayList<>();
        if (ast.condition instanceof IdentifierNode) {
            IdentifierNode node = (IdentifierNode) ast.condition;
            Variable var = new Variable(node.identifier.textRepresentation, null);
            // check if variable exists in function scope (params + locals)(relative)
            for (Variable v : m_functionVariables) {
                if (v.equals(var)) {
                    conditionRel = true;
                }
            }
            // else check global scope (absolute)
            // check if variable exists
            if (!doesVariableExist(var)) {
                throw new IllegalArgumentException("Variable does not exist in scope: " + var.name);
            }
            conditionIndex = getVariableStackLocation(var);
        } else {
            int stackBeforeCondition = m_variableStack.size();
            condition = dispatchToCompile(ast.condition);
            if (condition == null || condition.size() == 0) {
                throw new IllegalArgumentException("While statement needs a condition.");
            }
            conditionRel = true;
            conditionIndex = m_variableStack.size() - 1;
            int stackAfterContidion = m_variableStack.size();
            conditionPopCount = stackAfterContidion - stackBeforeCondition;
        }
        // increment IP for condition to fix break/continue
        if (conditionPopCount > 0) {
            m_functionIp++;
        }
        m_functionIp += 2; // jumpRelConditional + jumpRel

        // compile body
        ArrayList<Statement> body;
        {
            int stackBeforeCondition = m_variableStack.size();
            body = dispatchToCompile(ast.body);
            if (body.size() == 0) {
                throw new IllegalArgumentException("While statement needs a body.");
            }
            int stackAfterContidion = m_variableStack.size();
            int bodyPopCount = stackAfterContidion - stackBeforeCondition;

            // clear stack
            int clearCount = bodyPopCount + conditionPopCount;
            if (clearCount > 0) {
                body.add(Statement.clearStack(clearCount));
                m_functionIp++;
                // clear compiler stack
                for (int i = 0; i < bodyPopCount; i++) {
                    Variable var = m_variableStack.peek();
                    removeVariableFromScope(var);
                }
            }
        }

        // create condition jumps
        if (conditionRel) {
            conditionIndex = conditionIndex - m_variableStack.size() + 1;
        }
        condition.add(Statement.jumpRelConditional(true, conditionPopCount == 0 ? 1 : 2, conditionRel, conditionIndex));
        // IP increment is after compiling the condition
        if (conditionPopCount > 0) {
            condition.add(Statement.clearStack(conditionPopCount));
            // IP increment is after compiling the condition
        }
        condition.add(Statement.jumpRel(body.size() + 1));
        // IP increment is after compiling the condition

        // clear compiler stack from condition
        for (int i = 0; i < conditionPopCount; i++) {
            Variable var = m_variableStack.peek();
            removeVariableFromScope(var);
        }

        // create body jump
        body.add(Statement.jumpRel(-(body.size() + condition.size() + 1)));
        m_functionIp++;

        // add lists to return
        ret.addAll(condition);
        ret.addAll(body);

        // backpatch breaks
        for (Backpatch backpatch : loopBackpatch.backpatches) {
            if (backpatch.node instanceof BreakNode) {
                backpatch.targetIp = m_functionIp - 1;
            }
        }
        // backpatch continues
        for (Backpatch backpatch : loopBackpatch.backpatches) {
            if (backpatch.node instanceof ContinueNode) {
                backpatch.targetIp = m_functionIp - 2;
            }
        }

        // put backpatches in array
        for (Backpatch backpatch : loopBackpatch.backpatches) {
            m_backpatches.add(backpatch);
        }
        m_loopBackpatches.pop();
        return ret;
    }
    private ArrayList<Statement> compileConditionalNode(ConditionalNode ast) {
        ArrayList<Statement> ret = new ArrayList<>();

        // compile condition
        int conditionPopCount = 0;
        boolean conditionRel = false;
        int conditionIndex = 0;
        if (ast.condition instanceof IdentifierNode) {
            IdentifierNode node = (IdentifierNode) ast.condition;
            Variable var = new Variable(node.identifier.textRepresentation, null);
            // check if variable exists in function scope (params + locals)(relative)
            for (Variable v : m_functionVariables) {
                if (v.equals(var)) {
                    conditionRel = true;
                }
            }
            // else check global scope (absolute)
            // check if variable exists
            if (!doesVariableExist(var)) {
                throw new IllegalArgumentException("Variable does not exist in scope: " + var.name);
            }
            conditionIndex = getVariableStackLocation(var);
        } else {
            int stackBeforeCondition = m_variableStack.size();
            ArrayList<Statement> condition = dispatchToCompile(ast.condition);
            if (condition == null || condition.size() == 0) {
                throw new IllegalArgumentException("If statement needs a condition.");
            }
            ret.addAll(condition);
            int stackAfterContidion = m_variableStack.size();
            conditionRel = true;
            conditionIndex = m_variableStack.size() - 1;
            conditionPopCount = stackAfterContidion - stackBeforeCondition;
        }
        m_functionIp++; // for the conditional jump instruction
        if (conditionRel) {
            conditionIndex = conditionIndex - m_variableStack.size() + 1;
        }

        // compile true body
        ArrayList<Statement> trueBody;
        {
            int stackBeforeCondition = m_variableStack.size();
            trueBody = dispatchToCompile(ast.trueBody);
            if (trueBody.size() == 0) {
                throw new IllegalArgumentException("If statement needs a true-branch body.");
            }
            int stackAfterContidion = m_variableStack.size();
            int trueBodyPopCount = stackAfterContidion - stackBeforeCondition;

            // clear stack
            int clearCount = trueBodyPopCount + conditionPopCount;
            if (clearCount > 0) {
                trueBody.add(Statement.clearStack(clearCount));
                m_functionIp++;
                // clear compiler stack
                for (int i = 0; i < trueBodyPopCount; i++) {
                    Variable var = m_variableStack.peek();
                    removeVariableFromScope(var);
                }
            }
        }
        m_functionIp++; // true body skip false body (removed later if no false body is present)

        // compile false body
        ArrayList<Statement> falseBody = null;
        int falseClearCount = conditionPopCount;
        if (ast.falseBody != null) {
            int stackBeforeCondition = m_variableStack.size();
            falseBody = dispatchToCompile(ast.falseBody);
            if (falseBody.size() == 0) {
                throw new IllegalArgumentException("If-else statement needs a false-branch body.");
            }
            int stackAfterContidion = m_variableStack.size();
            int falseBodyPopCount = stackAfterContidion - stackBeforeCondition;

            falseClearCount = falseBodyPopCount + conditionPopCount;
            // clear compiler stack
            for (int i = 0; i < falseBodyPopCount; i++) {
                Variable var = m_variableStack.peek();
                removeVariableFromScope(var);
            }
        }

        if (falseClearCount > 0) {
            if (falseBody == null) {
                falseBody = new ArrayList<>();
            }
            falseBody.add(Statement.clearStack(falseClearCount));
            m_functionIp++;
        }

        // clear compiler stack from condition
        for (int i = 0; i < conditionPopCount; i++) {
            Variable var = m_variableStack.peek();
            removeVariableFromScope(var);
        }

        // if false body exists skip it at the end of true body
        if (falseBody != null) {
            trueBody.add(Statement.jumpRel(falseBody.size()));
            // IP increment is after compiling true body
        } else {
            m_functionIp--; // decrement because no false body present
        }
        ret.add(Statement.jumpRelConditional(false, trueBody.size(), conditionRel, conditionIndex));
        // IP increment is after compiling the condition

        // add bodies to return
        ret.addAll(trueBody);
        if (falseBody != null) {
            ret.addAll(falseBody);
        }

        return ret;
    }
    private ArrayList<Statement> compileFunctionCallNode(FunctionCallNode ast) {
        ArrayList<Statement> ret = new ArrayList<>();

        // check if function exists
        FunctionVariable funcVar = new FunctionVariable(ast.funcName.identifier.textRepresentation, null, null, 0);
        if (!doesVariableExist(funcVar)) {
            throw new IllegalArgumentException("Function does not exist in scope: " + funcVar.name);
        }
        int funcStackLoc = getVariableStackLocation(funcVar);
        Variable var = m_variableStack.get(funcStackLoc);
        if (!(var instanceof FunctionVariable)) {
            throw new IllegalArgumentException("Variable is not a function: " + var.name);
        }
        funcVar = (FunctionVariable) var;

        // TODO: add checks to make sure function params match
        // TODO: add checks to make sure return type matches

        // compile param list
        ret.addAll(compileCallParamsNode(ast.params));

        // call function
        ret.add(Statement.functionCall(funcVar.ip));
        m_functionIp++;

        // remove params from compiler stack and push return value, because they are cleared by the function
        for (int i = 0; i < funcVar.params.size(); i++) {
            Variable delVar = m_variableStack.peek();
            removeVariableFromScope(delVar);
        }
        if (!(funcVar.value instanceof Void_T)) {
            Variable tempVar = new TempVariable(funcVar.value.getTypeInstance());
            addVariableToScope(tempVar);
        }

        return ret;
    }
    private ArrayList<Statement> compileCallParamsNode(CallParamsNode ast) {
        ArrayList<Statement> ret = new ArrayList<>();
        
        ArrayList<Integer> exprResults = new ArrayList<>();
        for (AST param : ast.params) {
            if (!(param instanceof IdentifierNode) && !(param instanceof LiteralNode)) {
                ret.addAll(dispatchToCompile(param));
                exprResults.add(m_variableStack.size() - 1);
            }
        }

        for (AST param : ast.params) {
            if (param instanceof IdentifierNode) {
                IdentifierNode node = (IdentifierNode) param;
                Variable var = new Variable(node.identifier.textRepresentation, null);
                // check if variable exists in function scope (params + locals)(relative)
                boolean rel = false;
                if (m_inFunction) {
                    for (Variable v : m_functionVariables) {
                        if (v.equals(var)) {
                            rel = true;
                            break;
                        }
                    }
                }
                
                // check if identifier exists
                if (!doesVariableExist(var)) {
                    throw new IllegalArgumentException("Variable does not exist in scope: " + var.name);
                }
                
                // push copy to stack
                int index = getVariableStackLocation(var);
                TempVariable tempVar = new TempVariable(m_variableStack.get(index).value.getTypeInstance());
                if (rel) {
                    index = index - m_variableStack.size() + 1;
                }
                addVariableToScope(tempVar);
                ret.add(Statement.createTempCopy(rel, index));
                m_functionIp++;
            } else if (param instanceof LiteralNode) {
                LiteralNode node = (LiteralNode) param;
                TempVariable tempVar = new TempVariable(Type_T.fromToken(node.literal));
                tempVar.value.assignValueFromString(node.literal.textRepresentation);
                addVariableToScope(tempVar);
                ret.add(Statement.declareVariable(tempVar));
                m_functionIp++;
            } else {
                if (exprResults.size() == 0) {
                    throw new IllegalArgumentException("Expression result missing.");
                }
                // copy result of expression to stack
                int index = exprResults.get(0);
                exprResults.remove(0);
                TempVariable tempVar = new TempVariable(m_variableStack.get(index).value.getTypeInstance());
                addVariableToScope(tempVar);
                ret.add(Statement.createTempCopy(true, index - m_variableStack.size() + 1));
                m_functionIp++;
            }
        }
        return ret;
    }
    private ArrayList<Statement> compileReturnNode(ReturnNode ast) {
        ArrayList<Statement> ret = new ArrayList<>();
        if (!m_inFunction) {
            throw new IllegalArgumentException("Cannot return outside of function.");
        }
        // TODO: check if return types matches function
        // TODO: use createTempCopy when applicable to avoid unnecessary temp variables like in compileCallParamsNode

        // compile rhs
        if (ast.retVal != null) {
            if (ast.retVal instanceof IdentifierNode) {
                int retIndex = 0;
                boolean rel = false;
                IdentifierNode node = (IdentifierNode) ast.retVal;
                Variable var = new Variable(node.identifier.textRepresentation, null);
                // check if variable exists in function scope (params + locals)(relative)
                for (Variable v : m_functionVariables) {
                    if (v.equals(var)) {
                        rel = true;
                    }
                }
                // else check global scope (absolute)
                // check if variable exists
                if (!doesVariableExist(var)) {
                    throw new IllegalArgumentException("Variable does not exist in scope: " + var.name);
                }
                retIndex = getVariableStackLocation(var);

                Type_T retType = m_variableStack.get(retIndex).value.getTypeInstance();
                Variable tempVar = new TempVariable(retType);
                addVariableToScope(tempVar);
                ret.add(Statement.declareVariable(tempVar));
                m_functionIp++;
                if (rel) {
                    retIndex = retIndex - m_variableStack.size() + 1;
                }
                ret.add(Statement.assignVariable(true, 0, rel, retIndex));
                m_functionIp++;
            } else {
                ret.addAll(dispatchToCompile(ast.retVal));
            }
            ret.add(Statement.functionReturn(true, m_functionVariables.size() - 1));
            m_functionIp++;
        } else {
            ret.add(Statement.functionReturn(false, m_functionVariables.size()));
            m_functionIp++;
        }
        return ret;
    }
    private ArrayList<Statement> compileFunctionDefinitionNode(FunctionDefNode ast) {
        ArrayList<Statement> ret = new ArrayList<>();

        // TODO: somehow force return statement

        if (m_inFunction) {
            throw new IllegalArgumentException("Cannot define function inside another function.");
        }
        if (m_scopeStack.size() > 1) {
            throw new IllegalArgumentException("Functions can only be defined in global scope.");
        }

        // compile function params
        ArrayList<Variable> params = compileParamDeclarationNode(ast.paramDeclaration);

        // check if name is taken already
        FunctionVariable funcVar = new FunctionVariable(ast.declaration.identifier.textRepresentation, Type_T.fromToken(ast.declaration.type), params, m_functionIp + 1);
        if (doesVariableExist(funcVar)) {
            throw new IllegalArgumentException("Function name taken, variable already exists in scope: " + funcVar.name);
        }
        addVariableToScope(funcVar);
        m_inFunction = true;

        // add function variable
        ret.add(Statement.declareVariable(funcVar));
        m_functionIp++;
        ret.add(Statement.jumpRel(0));
        m_functionIp++;
        int jumpBackpatch = ret.size() - 1;

        // add params to scope
        for (Variable param : params) {
            addVariableToScope(param);
        }

        // compile function block
        ArrayList<Statement> block = compileBlockNode(ast.block);
        block.remove(block.size() - 1); // remove stack clear instruction because return clears stack
        m_functionIp--; // decrement because we removed stack clear
        ret.addAll(block);

        // remove params from scope
        for (Variable param : params) {
            removeVariableFromScope(param);
        }

        // backpatch jump
        ret.set(jumpBackpatch, Statement.jumpRel(block.size()));
        
        m_inFunction = false;
        return ret;
    }
    private ArrayList<Variable> compileParamDeclarationNode(ParamDeclarationNode ast) {
        ArrayList<Variable> ret = new ArrayList<>();
        for (DeclarationNode declaration : ast.declarations) {
            Variable var = new Variable(declaration.identifier.textRepresentation, Type_T.fromToken(declaration.type));
            for (Variable v : ret) {
                if (v.equals(var)) {
                    throw new IllegalArgumentException("Duplicate parameter name: " + var.name);
                }
            }
            ret.add(var);
        }
        return ret;
    }
    private ArrayList<Statement> compileAssignmentNode(AssignmentNode ast) {
        ArrayList<Statement> ret = new ArrayList<>();

        // TODO: if RHS is Literal, then we can optimize by not creating a temp variable

        // compile rhs of assignment
        int rhsIndex = 0;
        boolean rhsRel = false;
        if (ast.rhs instanceof IdentifierNode) {
            IdentifierNode node = (IdentifierNode) ast.rhs;
            Variable var = new Variable(node.identifier.textRepresentation, null);
            // check if variable exists in function scope (params + locals)(relative)
            if (m_inFunction) {
                for (Variable v : m_functionVariables) {
                    if (v.equals(var)) {
                        rhsRel = true;
                    }
                }
            }
            // else check global scope (absolute)
            // check if variable exists
            if (!doesVariableExist(var)) {
                throw new IllegalArgumentException("Variable does not exist in scope: " + var.name);
            }
            rhsIndex = getVariableStackLocation(var);
        } else {
            ret.addAll(dispatchToCompile(ast.rhs));
            rhsIndex = m_variableStack.size() - 1;
            rhsRel = true;
        }
        Type_T rhsType = m_variableStack.get(rhsIndex).value.getTypeInstance();


        // lhs
        int lhsIndex = 0;
        boolean lhsRel = false;
        if (ast.lhs instanceof IdentifierNode) {
            IdentifierNode node = (IdentifierNode) ast.lhs;
            Variable var = new Variable(node.identifier.textRepresentation, null);
            // check if variable exists in function scope (params + locals)(relative)
            if (m_inFunction) {
                for (Variable v : m_functionVariables) {
                    if (v.equals(var)) {
                        lhsRel = true;
                    }
                }
            }
            // else check global scope (absolute)
            // check if variable exists
            if (!doesVariableExist(var)) {
                throw new IllegalArgumentException("Variable does not exist in scope: " + var.name);
            }
            lhsIndex = getVariableStackLocation(var);
        } else {
            ret.addAll(dispatchToCompile(ast.lhs));
            lhsIndex = m_variableStack.size() - 1;
            lhsRel = true;
        }
        Type_T lhsType = m_variableStack.get(lhsIndex).value.getTypeInstance();
        
        checkIfTypesAreCompatible(lhsType, rhsType, Token.Type.OP_ASSIGN);

        // assign value to variable
        if (lhsRel) {
            lhsIndex = lhsIndex - m_variableStack.size() + 1;
        }
        if (rhsRel) {
            rhsIndex = rhsIndex - m_variableStack.size() + 1;
        }
        ret.add(Statement.assignVariable(lhsRel, lhsIndex, rhsRel, rhsIndex));
        m_functionIp++;

        return ret;
    }
    private ArrayList<Statement> compileDeclarationNode(DeclarationNode ast) {
        ArrayList<Statement> ret = new ArrayList<>();

        // create variable
        Variable var = new Variable(ast.identifier.textRepresentation, Type_T.fromToken(ast.type));

        // declare variable
        if (doesVariableExist(var)) {
            throw new IllegalArgumentException("Variable already exists in scope: " + var.name);
        }
        addVariableToScope(var);
        ret.add(Statement.declareVariable(var));
        m_functionIp++;

        return ret;
    }
    private ArrayList<Statement> compileBinaryOpNode(BinaryOpNode ast) {
        ArrayList<Statement> ret = new ArrayList<>();

        // lhs
        int lhsIndex = -1;
        boolean lhsRel = false;
        if (ast.lhs instanceof IdentifierNode) {
            IdentifierNode node = (IdentifierNode) ast.lhs;
            Variable var = new Variable(node.identifier.textRepresentation, null);
            // check if variable exists in function scope (params + locals)(relative)
            if (m_inFunction) {
                for (Variable v : m_functionVariables) {
                    if (v.equals(var)) {
                        lhsRel = true;
                    }
                }
            }
            // else check global scope (absolute)
            // check if variable exists
            if (!doesVariableExist(var)) {
                throw new IllegalArgumentException("Variable does not exist in scope: " + var.name);
            }
            lhsIndex = getVariableStackLocation(var);
        } else {
            ret.addAll(dispatchToCompile(ast.lhs));
            lhsIndex = m_variableStack.size() - 1;
            lhsRel = true;
        }
        Type_T lhsType = m_variableStack.get(lhsIndex).value.getTypeInstance();

        // rhs
        int rhsIndex = -1;
        boolean rhsRel = false;
        if (ast.rhs instanceof IdentifierNode) {
            IdentifierNode node = (IdentifierNode) ast.rhs;
            Variable var = new Variable(node.identifier.textRepresentation, null);
            // check if variable exists in function scope (params + locals)(relative)
            if (m_inFunction) {
                for (Variable v : m_functionVariables) {
                    if (v.equals(var)) {
                        rhsRel = true;
                    }
                }
            }
            // else check global scope (absolute)
            // check if variable exists
            if (!doesVariableExist(var)) {
                throw new IllegalArgumentException("Variable does not exist in scope: " + var.name);
            }
            rhsIndex = getVariableStackLocation(var);
        } else {
            ret.addAll(dispatchToCompile(ast.rhs));
            rhsIndex = m_variableStack.size() - 1;
            rhsRel = true;
        }
        Type_T rhsType = m_variableStack.get(rhsIndex).value.getTypeInstance();

        // check if lhs and rhs are compatible types
        checkIfTypesAreCompatible(lhsType, rhsType, ast.operator.type);

        // create temp variable on the stack to store result
        Variable var = new TempVariable(lhsType);
        addVariableToScope(var);
        ret.add(Statement.declareVariable(var));
        m_functionIp++;
        int resultIndex = m_variableStack.size() - 1;
        boolean resultRel = true;
        
        // perform binary operation
        if (resultRel) {
            resultIndex = resultIndex - m_variableStack.size() + 1;
        }
        if (lhsRel) {
            lhsIndex = lhsIndex - m_variableStack.size() + 1;
        }
        if (rhsRel) {
            rhsIndex = rhsIndex - m_variableStack.size() + 1;
        }
        ret.add(Statement.binaryOp(ast.operator.type, resultRel, resultIndex, lhsRel, lhsIndex, rhsRel, rhsIndex));
        m_functionIp++;

        return ret;
    }
    private ArrayList<Statement> compileLiteralNode(LiteralNode ast) {
        ArrayList<Statement> ret = new ArrayList<>();
        // create temp variable on the stack to store literal
        Variable var = new TempVariable(Type_T.fromToken(ast.literal));
        var.value.assignValueFromString(ast.literal.textRepresentation);
        addVariableToScope(var);
        // declare variable with value
        ret.add(Statement.declareVariable(var));
        m_functionIp++;
        return ret;
    }

    private void checkIfTypesAreCompatible(Type_T lhs, Type_T rhs, Token.Type op) {
        // TODO: add more checks
        if ((lhs instanceof Any_T || 
             lhs instanceof Float_T || lhs instanceof UnsignedInt_T || lhs instanceof SignedInt_T || lhs instanceof Bool_T) &&
            (rhs instanceof Float_T || rhs instanceof UnsignedInt_T || rhs instanceof SignedInt_T || rhs instanceof Bool_T)) {
            return;
        }
        if (lhs instanceof Any_T && rhs instanceof Any_T) {
            return;
        }

        throw new IllegalArgumentException("Incompatible types for operation: " + lhs + " " + op + " " + rhs);
    }

    private boolean doesVariableExist(Variable var) {
        for (Variable v : m_variableStack) {
            if (v.equals(var)) {
                return true;
            }
        }
        return false;
    }
    private int getVariableStackLocation(Variable var) {
        for (int i = m_variableStack.size() - 1; i >= 0; i--) {
            if (m_variableStack.get(i).equals(var)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Variable not found in stack: " + var.name);
    }
    private void addVariableToScope(Variable variable) {
        m_scopeStack.peek().variables.add(variable);
        m_variableStack.push(variable);
        if (m_inFunction) {
            m_functionVariables.add(variable);
        }
    }
    private void removeVariableFromScope(Variable variable) {
        m_scopeStack.peek().variables.remove(variable);
        m_variableStack.pop();
        if (m_inFunction) {
            m_functionVariables.remove(variable);
        }
    }
    private void startScope() {
        m_scopeStack.push(new Scope());
    }
    private ArrayList<Statement> endScope() {
        ArrayList<Statement> ret = new ArrayList<>();
        Scope scope = m_scopeStack.pop();
        if (m_inFunction) {
            for (Variable var : scope.variables) {
                m_functionVariables.remove(var);
            }
        }
        for (int i = 0; i < scope.variables.size(); i++) {
            m_variableStack.pop();
        }
        ret.add(Statement.clearStack(scope.variables.size()));
        m_functionIp++;
        return ret;
    }

    private class TempVariable extends Variable {
        public TempVariable(Type_T type) {
            super(m_tempVariableCounter++ + "_COMPILER_TEMP", type);
        }
    }
    private long m_tempVariableCounter = 0;
    private static class Scope {
        public HashSet<Variable> variables = new HashSet<>();
    }
    private Stack<Scope> m_scopeStack = new Stack<>();
    private Stack<Variable> m_variableStack = new Stack<>();
    private HashSet<Variable> m_functionVariables = new HashSet<>();
    private int m_functionIp = 0;
    private boolean m_inFunction = false;
    
    // backpatching
    private void backpatch(ArrayList<Statement> statements) {
        for (Backpatch backpatch : m_backpatches) {
            int relIp = backpatch.targetIp - backpatch.ip;
            Statement fixed = null;
            if (backpatch.node instanceof BreakNode) {
                fixed = Statement.breakLoop(backpatch.stackClearCount, relIp);
            } else if (backpatch.node instanceof ContinueNode) {
                fixed = Statement.continueLoop(backpatch.stackClearCount, relIp);
            }
            if (fixed != null) {
                statements.set(backpatch.ip, fixed);
            } else {
                throw new IllegalArgumentException("Unknown type for backpatching `" + backpatch.node.getClass().getName() + "`.");
            }
        }
    }
    private static class Backpatch {
        public int ip;
        public int targetIp;
        public int stackClearCount;
        AST node;
    }
    private static class LoopBackpatch {
        ArrayList<Backpatch> backpatches = new ArrayList<>();
        int loopStackBreakStart;
        int loopStackContinueStart;
    }
    private ArrayList<Backpatch> m_backpatches = new ArrayList<>();
    private Stack<LoopBackpatch> m_loopBackpatches = new Stack<>();

    // external functions
    public static class ExtFuncDecl {
        public boolean yieldAfterReturn;
        public String name;
        public Type_T retType;
        public ArrayList<Variable> params;
        public ExternalFunction<? extends Type_T> function;
    }
    @FunctionalInterface public static interface ExternalFunction<Ret extends Type_T>  {
        public Ret execute(Type_T ...params);
    }
    // return value for testing purposes only
    public ExtFuncDecl addExternalFunction(String name, boolean yieldAfterReturn, Type_T retType, Type_T[] params, ExternalFunction<? extends Type_T> function) {
        ExtFuncDecl decl = new ExtFuncDecl();
        decl.yieldAfterReturn = yieldAfterReturn;
        decl.name = name;
        decl.retType = retType;
        decl.function = function;
        decl.params = new ArrayList<>();
        for (int i = 0; i < params.length; i++) {
            Type_T param = params[i];
            decl.params.add(new Variable("param_" + params[i], param));
        }

        m_externalFunctions.add(decl);
        return decl;
    }
    private ArrayList<ExtFuncDecl> m_externalFunctions = new ArrayList<>();
}
