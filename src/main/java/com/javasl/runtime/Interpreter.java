package com.javasl.runtime;

import java.util.ArrayList;
import java.util.Stack;

public class Interpreter {
    public void setStatements(ArrayList<Statement> statements) {
        m_statements = statements;
    }
    public void execute() {
        while (m_state.ip < m_statements.size()) {
            int ipCopy = m_state.ip;
            try {
                m_statements.get(m_state.ip).execute(m_state);
            } catch (Exception e) {
                System.out.println("Error at statement (" + ipCopy + ")" + m_statements.get(ipCopy).toString() + ": " + e.getMessage());
                break;
            }
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
            int ipCopy = m_state.ip;
            try {
                m_statements.get(m_state.ip).execute(m_state);
            } catch (Exception e) {
                System.out.println("Error at statement (" + ipCopy + ")" + m_statements.get(ipCopy).toString() + ": " + e.getMessage());
                break;
            }
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
    public ArrayList<Statement> getStatements() {
        return m_statements;
    }
    public String dumpState() {
        String dump = "";
        {
            dump += "stack:\n";
            for (Variable v : m_state.variables) {
                dump += v.toString() + "\n";
            }
        }
        {
            dump += "funcCalls:\n";
            for (int i : m_state.funcCalls) {
                dump += i + "\n";
            }
        }
        {
            dump += "ip: " + m_state.ip + "\n";
            dump += "executed statement: ";
            if (m_state.ip > 0) {
                dump += m_statements.get(m_state.ip - 1).toString() + "\n";
            } else {
                dump += "none\n";
            }
            dump += "next statement: ";
            if (m_state.ip < m_statements.size()) {
                dump += m_statements.get(m_state.ip).toString() + "\n";
            } else {
                dump += "none\n";
            }
        }
        return dump;
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
