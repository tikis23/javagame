package com.javasl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import com.javasl.compiler.Compiler;
import com.javasl.compiler.Compiler.ExternalFunction;
import com.javasl.compiler.Parser;
import com.javasl.compiler.Token;
import com.javasl.compiler.Tokenizer;
import com.javasl.compiler.ast.AST;
import com.javasl.runtime.Interpreter;
import com.javasl.runtime.Statement;
import com.javasl.runtime.types.*;

public class Script {
    public Script() {}
    public void addExternalFunction(String name, boolean yieldAfterReturn, Type_T retType, Type_T[] params, ExternalFunction<? extends Type_T> function) {
        m_compiler.addExternalFunction(name, yieldAfterReturn, retType, params, function);
    }
    public void compileFromFile(String path) throws IOException, IllegalArgumentException {
        // load file
        String content = null;
        try {
            content = Files.readString(Path.of(path));
        } catch (IOException e) {
            throw new IOException("Loading error: Failed to load file " + path);
        }
        compileFromSource(content);
    }
    public void compileFromSource(String source) throws IllegalArgumentException {
        // tokenize
        ArrayList<Token> tokens = m_tokenizer.tokenize(source);
        if (tokens == null) {
            throw new IllegalArgumentException("Tokenizer error: Failed to tokenize source.");
        }
        // parse
        AST ast = m_parser.parse(tokens);
        if (ast == null) {
            throw new IllegalArgumentException("Parser error: Failed to parse source.");
        }
        ArrayList<Statement> statements = m_compiler.compile(ast);
        if (statements == null) {
            throw new IllegalArgumentException("Compiler error: Failed to compile source.");
        }
        m_interpreter.setStatements(statements);

        m_ready = true;
    }

    public boolean isReady() {
        return m_ready;
    }
    public boolean isFinished() {
        return m_interpreter.isFinished();
    }

    public void execute() {
        m_interpreter.execute();
    }
    public void execute(int cycles) {
        m_interpreter.execute(cycles);
    }

    private boolean m_ready = false;
    private Tokenizer m_tokenizer = new Tokenizer();
    private Parser m_parser = new Parser();
    private Compiler m_compiler = new Compiler();
    private Interpreter m_interpreter = new Interpreter();

    // helper funcs for external functions
    public static boolean boolParam(Type_T param) {
        return ((Number)param.getValue()).longValue() != 0;
    }
    public static byte byteParam(Type_T param) {
        return ((Number)param.getValue()).byteValue();
    }
    public static short shortParam(Type_T param) {
        return ((Number)param.getValue()).shortValue();
    }
    public static int intParam(Type_T param) {
        return ((Number)param.getValue()).intValue();
    }
    public static long longParam(Type_T param) {
        return ((Number)param.getValue()).longValue();
    }
    public static float floatParam(Type_T param) {
        return ((Number)param.getValue()).floatValue();
    }
    public static double doubleParam(Type_T param) {
        return ((Number)param.getValue()).doubleValue();
    }

    // default (optional) external functions
    public void addDefaultFunctionPrint(boolean yieldAfterReturn) {
        addExternalFunction("print", yieldAfterReturn, new Void_T(), new Type_T[] {new Any_T()}, (Type_T[] params) -> {
            Type_T p0 = params[0];
            if (p0 instanceof Bool_T) {
                boolean val = boolParam(p0);
                System.out.println(Boolean.toString(val));
            } else if (p0 instanceof UnsignedInt_T) {
                long val = longParam(p0);
                System.out.println(Long.toUnsignedString(val));
            } else if (p0 instanceof SignedInt_T) {
                long val = longParam(p0);
                System.out.println(Long.toString(val));
            } else if (p0 instanceof Float_T) {
                double val = ((Number)p0.getValue()).doubleValue();
                System.out.println(Double.toString(val));
            } else {
                System.out.println(p0);
            }
            return new Void_T();
        });
    }
}
