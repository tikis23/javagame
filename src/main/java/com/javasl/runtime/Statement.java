package com.javasl.runtime;

import com.javasl.compiler.Compiler;
import com.javasl.compiler.Token;
import com.javasl.runtime.types.*;

public interface Statement {
    public void execute(Interpreter.State state);
    public String toString();

    public static Statement declareVariable(Variable variable) {
        return new Statement() {
            public void execute(Interpreter.State state) {
                state.variables.push(variable.copy());
            }
            public String toString() {
                return "declareVariable " + variable.copy().toString();
            }
        };
    }
    public static Statement binaryOp(Token.Type op, boolean result_rel, int result_sp, boolean left_rel, int left_sp,
                                                    boolean right_rel, int right_sp) {
        abstract class OperationOverload implements Statement {
            public void execute(Interpreter.State state) {
                // TODO: branches can be eliminated by returning a custom statement for each combination
                int result_loc = result_sp;
                int left_loc = left_sp;
                int right_loc = right_sp;
                if (result_rel) result_loc = state.variables.size() + result_sp - 1;
                if (left_rel) left_loc = state.variables.size() + left_sp - 1;
                if (right_rel) right_loc = state.variables.size() + right_sp - 1;

                Variable result = state.variables.get(result_loc);
                Variable left = state.variables.get(left_loc);
                Variable right = state.variables.get(right_loc);
                operation(result, left, right);
            }
            public String toString() {
                return "binaryOp " + op + " " +
                        (result_rel ? "rel(" + result_sp + ") " : "abs(" + result_sp + ") ") +
                        (left_rel ? "rel(" + left_sp + ") " : "abs(" + left_sp + ") ") +
                        (right_rel ? "rel(" + right_sp + ") " : "abs(" + right_sp + ")");
            }
            public abstract void operation(Variable result, Variable left, Variable right);
        }

        if (op == Token.Type.OP_MULTIPLY) {
            return new OperationOverload() {
                public void operation(Variable result, Variable left, Variable right) {
                    result.value = left.value.op_multiply(right.value);
                }
            };
        } else if (op == Token.Type.OP_DIVIDE) {
            return new OperationOverload() {
                public void operation(Variable result, Variable left, Variable right) {
                    result.value = left.value.op_divide(right.value);
                }
            };
        } else if (op == Token.Type.OP_MODULO) {
            return new OperationOverload() {
                public void operation(Variable result, Variable left, Variable right) {
                    result.value = left.value.op_modulo(right.value);
                }
            };
        } else if (op == Token.Type.OP_PLUS) {
            return new OperationOverload() {
                public void operation(Variable result, Variable left, Variable right) {
                    result.value = left.value.op_plus(right.value);
                }
            };
        } else if (op == Token.Type.OP_MINUS) {
            return new OperationOverload() {
                public void operation(Variable result, Variable left, Variable right) {
                    result.value = left.value.op_minus(right.value);
                }
            };
        } else if (op == Token.Type.OP_SHIFT_LEFT) {
            return new OperationOverload() {
                public void operation(Variable result, Variable left, Variable right) {
                    result.value = left.value.op_shift_left(right.value);
                }
            };
        } else if (op == Token.Type.OP_SHIFT_RIGHT) {
            return new OperationOverload() {
                public void operation(Variable result, Variable left, Variable right) {
                    result.value = left.value.op_shift_right(right.value);
                }
            };
        } else if (op == Token.Type.OP_LESS) {
            return new OperationOverload() {
                public void operation(Variable result, Variable left, Variable right) {
                    result.value = left.value.op_less(right.value);
                }
            };
        } else if (op == Token.Type.OP_LESS_EQUAL) {
            return new OperationOverload() {
                public void operation(Variable result, Variable left, Variable right) {
                    result.value = left.value.op_less_equal(right.value);
                }
            };
        } else if (op == Token.Type.OP_GREATER) {
            return new OperationOverload() {
                public void operation(Variable result, Variable left, Variable right) {
                    result.value = left.value.op_greater(right.value);
                }
            };
        } else if (op == Token.Type.OP_GREATER_EQUAL) {
            return new OperationOverload() {
                public void operation(Variable result, Variable left, Variable right) {
                    result.value = left.value.op_greater_equal(right.value);
                }
            };
        } else if (op == Token.Type.OP_EQUAL) {
            return new OperationOverload() {
                public void operation(Variable result, Variable left, Variable right) {
                    result.value = left.value.op_equal(right.value);
                }
            };
        } else if (op == Token.Type.OP_NOT_EQUAL) {
            return new OperationOverload() {
                public void operation(Variable result, Variable left, Variable right) {
                    result.value = left.value.op_not_equal(right.value);
                }
            };
        } else if (op == Token.Type.OP_BITWISE_AND) {
            return new OperationOverload() {
                public void operation(Variable result, Variable left, Variable right) {
                    result.value = left.value.op_bitwise_and(right.value);
                }
            };
        } else if (op == Token.Type.OP_BITWISE_XOR) {
            return new OperationOverload() {
                public void operation(Variable result, Variable left, Variable right) {
                    result.value = left.value.op_bitwise_xor(right.value);
                }
            };
        } else if (op == Token.Type.OP_BITWISE_OR) {
            return new OperationOverload() {
                public void operation(Variable result, Variable left, Variable right) {
                    result.value = left.value.op_bitwise_or(right.value);
                }
            };
        } else if (op == Token.Type.OP_LOGIC_AND) {
            return new OperationOverload() {
                public void operation(Variable result, Variable left, Variable right) {
                    result.value = left.value.op_logical_and(right.value);
                }
            };
        } else if (op == Token.Type.OP_LOGIC_OR) {
            return new OperationOverload() {
                public void operation(Variable result, Variable left, Variable right) {
                    result.value = left.value.op_logical_or(right.value);
                }
            };
        } else {
            throw new RuntimeException("Unsupported binary operation: " + op);
        }
    }
    public static Statement assignVariable(boolean target_rel, int target_sp, boolean source_rel, int source_sp) {
        return new Statement() {
            public void execute(Interpreter.State state) {
                // TODO: branches can be eliminated by returning a custom statement for each combination
                int target = target_sp;
                int source = source_sp;
                if (target_rel) target = state.variables.size() + target_sp - 1;
                if (source_rel) source = state.variables.size() + source_sp - 1;
                Variable var1 = state.variables.get(target);
                Variable var2 = state.variables.get(source);
                var1.value.setValue(var2.value.getValue());
            }
            public String toString() {
                return "assignVariable " +
                    (target_rel ? "rel(" + target_sp + ") " : "abs(" + target_sp + ") ") +
                    (source_rel ? "rel(" + source_sp + ") " : "abs(" + source_sp + ")");
            }
        };
    }
    public static Statement createTempCopy(boolean source_rel, int source_sp) {
        if (source_rel) {
            return new Statement() {
                public void execute(Interpreter.State state) {
                    Variable source = state.variables.get(state.variables.size() + source_sp - 1);
                    state.variables.push(source.copy());
                }
                public String toString() {
                    return "createTempCopy rel(" + source_sp + ")";
                }
            };
        } else {
            return new Statement() {
                public void execute(Interpreter.State state) {
                    Variable source = state.variables.get(source_sp);
                    state.variables.push(source.copy());
                }
                public String toString() {
                    return "createTempCopy abs(" + source_sp + ")";
                }
            };
        
        }
    }
    public static Statement clearStack(int count) {
        return new Statement() {
            public void execute(Interpreter.State state) {
                for (int i = 0; i < count; i++) {
                    state.variables.pop();
                }
            }
            public String toString() {
                return "clearStack " + count;
            }
        };
    }
    public static Statement breakLoop(int clearCount, int relJumpCount) {
        return new Statement() {
            public void execute(Interpreter.State state) {
                for (int i = 0; i < clearCount; i++) {
                    state.variables.pop();
                }
                state.ip += relJumpCount;
            }
            public String toString() {
                return "breakLoop " + clearCount + " rel(" + relJumpCount + ")";
            }
        };
    }
    public static Statement continueLoop(int clearCount, int relJumpCount) {
        return new Statement() {
            public void execute(Interpreter.State state) {
                for (int i = 0; i < clearCount; i++) {
                    state.variables.pop();
                }
                state.ip += relJumpCount;
            }
            public String toString() {
                return "continueLoop " + clearCount + " rel(" + relJumpCount + ")";
            }
        };
    }
    public static Statement jumpRel(int offset) {
        return new Statement() {
            public void execute(Interpreter.State state) {
                state.ip += offset;
            }
            public String toString() {
                return "jumpRel rel(" + offset + ")";
            }
        };
    }
    public static Statement jumpRelConditional(boolean invert, int offset, boolean source_rel, int source_sp) {
        Uint8_T zero = new Uint8_T(0);
        if (source_rel) {
            return new Statement() {
                public void execute(Interpreter.State state) {
                    Variable source = state.variables.get(state.variables.size() + source_sp - 1);
                    if ((Long)source.value.op_not_equal(zero).getValue() == (invert ? 1 : 0)) {
                        state.ip += offset;
                    }
                }
                public String toString() {
                    return "jumpRelCondition "+ invert +" rel(" + offset + ") " + "rel(" + source_sp + ")";
                }
            };
        } else {
            return new Statement() {
                public void execute(Interpreter.State state) {
                    Variable source = state.variables.get(source_sp);
                    if ((Long)source.value.op_not_equal(zero).getValue() == (invert ? 1 : 0)) {
                        state.ip += offset;
                    }
                }
                public String toString() {
                    return "jumpRelCondition "+ invert +" rel(" + offset + ") " + "abs(" + source_sp + ")";
                }
            };
        }
    }
    public static Statement functionCall(int ip) {
        return new Statement() {
            public void execute(Interpreter.State state) {
                state.funcCalls.push(state.ip);
                state.ip = ip;
            }
            public String toString() {
                return "functionCall abs(" + ip + ")";
            }
        };
    }
    public static Statement functionReturn(boolean keepTopVariable, int clearCount) {
        if (keepTopVariable) {
            return new Statement() {
                public void execute(Interpreter.State state) {
                    state.ip = state.funcCalls.pop();
                    Variable top = state.variables.peek();
                    for (int i = 0; i < clearCount; i++) {
                        state.variables.pop();
                    }
                    state.variables.set(state.variables.size() - 1, top);
                }
                public String toString() {
                    return "functionReturn " + keepTopVariable + " " + clearCount;
                }
            };
        } else {
            return new Statement() {
                public void execute(Interpreter.State state) {
                    state.ip = state.funcCalls.pop();
                    for (int i = 0; i < clearCount; i++) {
                        state.variables.pop();
                    }
                }
                public String toString() {
                    return "functionReturn " + keepTopVariable + " " + clearCount;
                }
            };
        }
    }
    public static Statement externalFunctionCall(Compiler.ExtFuncDecl funcDecl, boolean returnsValue) {
        return new Statement() {
            public void execute(Interpreter.State state) {
                // take params from stack
                Type_T[] params = new Type_T[funcDecl.params.size()];
                for (int i = funcDecl.params.size() - 1; i >= 0; i--) {
                    params[i] = state.variables.pop().value;
                }
                
                // execute function
                Type_T ret = funcDecl.function.execute(params);
                if (returnsValue) state.variables.add(new Variable("func_ret_val", ret));

                // return from function
                state.ip = state.funcCalls.pop();
                state.yield = funcDecl.yieldAfterReturn;
            }
            public String toString() {
                return "externalFunctionCall " + funcDecl.name;
            }
        };
    }
    public static Statement debugPrint() {
        return new Statement() {
            public void execute(Interpreter.State state) {
                System.out.println("######## DEBUG PRINT");
                for (Variable v : state.variables) {
                    System.out.println(v);
                }
                System.out.println("####################");
            }
            public String toString() {
                return "debugPrint";
            }
        };
    }
}
