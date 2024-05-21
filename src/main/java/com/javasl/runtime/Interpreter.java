package com.javasl.runtime;

import java.util.ArrayList;
import java.util.Stack;

public class Interpreter {
    public void setStatements(ArrayList<Statement> statements) {
        m_statements = statements;
    }
    public void execute() {
        while (m_state.ip < m_statements.size()) {
            m_statements.get(m_state.ip).execute(m_state);
            m_state.ip++;
            if (m_state.yield) {
                m_state.yield = false;
                break;
            }
        }
    }
    public void execute(int cycles) {
        for (int i = 0; i < cycles; i++) {
            if (m_state.ip >= m_statements.size()) {
                break;
            }
            m_statements.get(m_state.ip).execute(m_state);
            m_state.ip++;
            if (m_state.yield) {
                m_state.yield = false;
                break;
            }
        }
    }
    public void reset() {
        m_state = new State();
    }
    public Stack<Variable> getStack() {
        return m_state.variables;
    }
    public boolean isFinished() {
        return m_state.ip >= m_statements.size();
    }
    protected static class State {
        public int ip = 0;
        public boolean yield = false;
        public Stack<Variable> variables = new Stack<>();
        public Stack<Integer> funcCalls = new Stack<>();
    }
    private State m_state = new State();
    private ArrayList<Statement> m_statements = new ArrayList<>();
}
