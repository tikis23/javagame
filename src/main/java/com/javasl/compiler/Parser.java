package com.javasl.compiler;

import java.util.ArrayList;

import com.javasl.compiler.ast.*;

public class Parser {
    public AST parse(ArrayList<Token> tokens) throws IllegalArgumentException {
        if (tokens == null) {
            throw new IllegalArgumentException("Parser error: tokens is null.");
        }

        // make all tokens in a block
        Token open = new Token(Token.Type.OPEN_CURLY_BRACKET, "{");
        Token close = new Token(Token.Type.CLOSE_CURLY_BRACKET, "}");
        tokens.add(0, open);
        tokens.add(close);

        m_tokens = tokens;
        m_index = 0;

        try {
            return parseBlock();
        } catch (Exception e) {
            String localTokens = "";
            if (m_index >= m_tokens.size()) m_index = m_tokens.size() - 1;
            for (int i = 5; i >= 0; i--) {
                if (m_index - i < 0) continue;
                localTokens += m_tokens.get(m_index - i).textRepresentation + " ";
            }
            throw new IllegalArgumentException("Parser error `" + localTokens + "`: " + e.getMessage(), e);
        }
    }

    private Token peek(int amount) {
        if (m_index + amount >= m_tokens.size()) {
            return null;
        }
        return m_tokens.get(m_index + amount);
    }
    private Token next() {
        if (m_index >= m_tokens.size()) {
            return null;
        }
        return m_tokens.get(m_index++);
    }
    private void restoreBackup(int backup) {
        m_index = backup;
    }
    private int backup() {
        return m_index;
    }
    private void consumeSemicolon() {
        Token next = next();
        if (next == null || next.type != Token.Type.SEMICOLON) {
            throw new IllegalArgumentException("expected semicolon.");
        }
    }

    private AST parseBlock() {
        int backup = backup();
        Token open = next();
        if (open == null || open.type != Token.Type.OPEN_CURLY_BRACKET) {
            restoreBackup(backup);
            return null;
        }
        BlockNode node = new BlockNode();
        while (true) {
            AST statement = parseStatement();
            if (statement == null) {
                break;
            }
            node.statements.add(statement);
        }
        Token close = next();
        if (close == null || close.type != Token.Type.CLOSE_CURLY_BRACKET) {
            throw new IllegalArgumentException("expected '}' after block.");
        }
        return node;
    }
    private AST parseStatement() {
        int backup = backup();
        // comment
        Token comment = peek(0);
        while (comment != null && comment.type == Token.Type.COMMENT) {
            comment = next();
            backup = backup();
        }

        // block
        restoreBackup(backup);
        AST node = parseBlock();
        if (node != null) {
            return node;
        }

        // function definition
        restoreBackup(backup);
        node = parseFunctionDefinition();
        if (node != null) {
            return node;
        }

        // function call
        restoreBackup(backup);
        node = parseFunctionCall();
        if (node != null) {
            consumeSemicolon();
            return node;
        }

        // assignment
        restoreBackup(backup);
        node = parseAssignment();
        if (node != null) {
            consumeSemicolon();
            return node;
        }

        // return
        restoreBackup(backup);
        node = parseReturn();
        if (node != null) {
            consumeSemicolon();
            return node;
        }

        // if
        restoreBackup(backup);
        node = parseIf();
        if (node != null) {
            return node;
        }

        // while
        restoreBackup(backup);
        node = parseWhile();
        if (node != null) {
            return node;
        }

        // for
        restoreBackup(backup);
        node = parseFor();
        if (node != null) {
            return node;
        }

        // break
        restoreBackup(backup);
        Token breakToken = next();
        if (breakToken != null && breakToken.type == Token.Type.KW_BREAK) {
            consumeSemicolon();
            return new BreakNode();
        }

        // continue
        restoreBackup(backup);
        Token continueToken = next();
        if (continueToken != null && continueToken.type == Token.Type.KW_CONTINUE) {
            consumeSemicolon();
            return new ContinueNode();
        }

        restoreBackup(backup);
        return null;
    }
    private AST parseFor() {
        int backup = backup();
        Token forToken = next();
        if (forToken == null || forToken.type != Token.Type.KW_FOR) {
            restoreBackup(backup);
            return null;
        }
        Token openParen = next();
        if (openParen == null || openParen.type != Token.Type.OPEN_PAREN) {
            throw new IllegalArgumentException("expected '(' after for.");
        }
        AST init = parseAssignment();
        if (init == null) {
            init = parseExpression();
        }
        Token semicolon1 = next();
        if (semicolon1 == null || semicolon1.type != Token.Type.SEMICOLON) {
            throw new IllegalArgumentException("expected ';' after for init.");
        }
        AST condition = parseExpression();
        Token semicolon2 = next();
        if (semicolon2 == null || semicolon2.type != Token.Type.SEMICOLON) {
            throw new IllegalArgumentException("expected ';' after for condition.");
        }
        AST increment = parseAssignment();
        if (increment == null) {
            increment = parseExpression();
        }
        Token closeParen = next();
        if (closeParen == null || closeParen.type != Token.Type.CLOSE_PAREN) {
            throw new IllegalArgumentException("expected ')' after for increment.");
        }
        AST body = parseStatement();
        if (body == null) {
            throw new IllegalArgumentException("expected statement after for.");
        }
        
        ForLoopNode node = new ForLoopNode();
        node.init = init;
        node.condition = condition;
        node.increment = increment;
        node.body = body;
        return node;
    }
    private AST parseIf() {
        int backup = backup();
        Token ifToken = next();
        if (ifToken == null || ifToken.type != Token.Type.KW_IF) {
            restoreBackup(backup);
            return null;
        }
        Token openParen = next();
        if (openParen == null || openParen.type != Token.Type.OPEN_PAREN) {
            throw new IllegalArgumentException("expected '(' after if.");
        }
        AST expression = parseExpression();
        if (expression == null) {
            throw new IllegalArgumentException("expected expression inside if statement.");
        }
        Token closeParen = next();
        if (closeParen == null || closeParen.type != Token.Type.CLOSE_PAREN) {
            throw new IllegalArgumentException("expected ')' after if statement.");
        }
        AST statement = parseStatement();
        if (statement == null) {
            throw new IllegalArgumentException("expected statement after if statement.");
        }
        ConditionalNode node = new ConditionalNode();
        node.condition = expression;
        node.trueBody = statement;

        Token elseToken = peek(0);
        if (elseToken == null || elseToken.type != Token.Type.KW_ELSE) {
            return node;
        }
        next(); // consume else
        statement = parseStatement();
        if (statement == null) {
            throw new IllegalArgumentException("expected statement after else.");
        }
        node.falseBody = statement;

        return node;
    }
    private AST parseWhile() {
        int backup = backup();
        Token whileToken = next();
        if (whileToken == null || whileToken.type != Token.Type.KW_WHILE) {
            restoreBackup(backup);
            return null;
        }
        Token openParen = next();
        if (openParen == null || openParen.type != Token.Type.OPEN_PAREN) {
            throw new IllegalArgumentException("expected '(' after while.");
        }
        AST expression = parseExpression();
        if (expression == null) {
            throw new IllegalArgumentException("expected expression inside while statement.");
        }
        Token closeParen = next();
        if (closeParen == null || closeParen.type != Token.Type.CLOSE_PAREN) {
            throw new IllegalArgumentException("expected ')' after while statement.");
        }
        AST statement = parseStatement();
        if (statement == null) {
            throw new IllegalArgumentException("expected statement after while statement.");
        }
        WhileLoopNode node = new WhileLoopNode();
        node.condition = expression;
        node.body = statement;
        return node;
    }
    private AST parseReturn() {
        int backup = backup();
        Token ret = next();
        if (ret == null || ret.type != Token.Type.KW_RETURN) {
            restoreBackup(backup);
            return null;
        }
        AST expression = parseExpression();
        ReturnNode node = new ReturnNode();
        node.retVal = expression;
        return node;
    }
    private AST parseFunctionDefinition() {
        int backup = backup();

        // var decl
        AST decl = parseVarDecl();
        if (decl == null) {
            restoreBackup(backup);
            return null;
        }

        // check if next token is '('
        Token openParen = next();
        if (openParen == null || openParen.type != Token.Type.OPEN_PAREN) {
            restoreBackup(backup);
            return null;
        }

        // parse parameters
        ParamDeclarationNode params = (ParamDeclarationNode)parseParamDeclaration();
        // check if next token is ')'
        Token closeParen = next();
        if (closeParen == null || closeParen.type != Token.Type.CLOSE_PAREN) {
            throw new IllegalArgumentException("expected ')' after parameters.");
        }

        // parse block
        BlockNode block = (BlockNode)parseBlock();
        if (block == null) {
            throw new IllegalArgumentException("expected block after function definition.");
        }

        FunctionDefNode node = new FunctionDefNode();
        node.declaration = (DeclarationNode)decl;
        node.paramDeclaration = params;
        node.block = block;
        return node;
    }
    private AST parseAssignment() {
        int backup = backup();
        AST lhs = parseLHS();
        if (lhs == null) {
            restoreBackup(backup);
            return null;
        }
        Token op = next();
        if (op == null || op.type != Token.Type.OP_ASSIGN) {
            throw new IllegalArgumentException("expected assignment operator.");
        }
        AST rhs = parseRHS();
        if (rhs == null) {
            throw new IllegalArgumentException("expected rhs of assignment.");
        }
        AssignmentNode node = new AssignmentNode();
        node.lhs = lhs;
        node.rhs = rhs;
        return node;
    }
    private AST parseLHS() {
        int backup = backup();

        // format: type identifier
        AST decl = parseVarDecl();
        if (decl != null) {
            return decl;
        }

        // check if identifier1 is valid
        restoreBackup(backup);
        Token iden1 = next();
        if (iden1 == null || iden1.type != Token.Type.IDENTIFIER) {
            restoreBackup(backup);
            return null;
        }
        Token iden2 = peek(0);
        // format: identifier
        if (iden2 == null || iden2.type != Token.Type.IDENTIFIER) {
            IdentifierNode node = new IdentifierNode();
            node.identifier = iden1;
            return node;
        }
        // format: identifier1 identifier2
        next(); // consume iden2
        DeclarationNode node = new DeclarationNode();
        node.type = iden1;
        node.identifier = iden2;
        return node;
    }
    private AST parseVarDecl() {
        int backup = backup();

        Token type = next();
        Token identifier = next();
        if (type == null || identifier == null) {
            restoreBackup(backup);
            return null;
        }
        if (type.getTypeGroup() != Token.TypeGroup.TYPE || identifier.type != Token.Type.IDENTIFIER) {
            restoreBackup(backup);
            return null;
        }

        DeclarationNode node = new DeclarationNode();
        node.type = type;
        node.identifier = identifier;
        return node;
    }
    private AST parseRHS() {
        int backup = backup();
        AST expression = parseExpression();
        if (expression == null) {
            restoreBackup(backup);
            return null;
        }
        return expression;
    }
    private AST parseExpression() {
        int backup = backup();
        AST term = parseTerm(m_opPrecedenceList.size() - 1);
        if (term == null) {
            restoreBackup(backup);
            return null;
        }
        return term;
    }
    private AST parseTerm(int precedence) {
        int backup = backup();
        AST factor = null;
        if (precedence > 0) {
            factor = parseTerm(precedence - 1);
        } else {
            factor = parseFactor();
        }
        if (factor == null) {
            restoreBackup(backup);
            return null;
        }
        while (true) {
            Token op = peek(0);
            if (op == null) {
                break;
            }
            boolean opFound = false;
            for (Token.Type type : m_opPrecedenceList.get(precedence)) {
                if (op.type == type) {
                    opFound = true;
                    break;
                }
            }
            if (opFound) {
                next(); // consume operator
                AST factor2 = null;
                if (precedence > 0) {
                    factor2 = parseTerm(precedence - 1);
                } else {
                    factor2 = parseFactor();
                }
                if (factor2 == null) {
                    throw new IllegalArgumentException("expected term after operator.");
                }
                BinaryOpNode node = new BinaryOpNode();
                node.lhs = factor;
                node.rhs = factor2;
                node.operator = op;
                factor = node;
            } else {
                break;
            }
        }
        return factor;
    }
    private AST parseFactor() {
        int backup = backup();

        // function call
        AST callNode = parseFunctionCall();
        if (callNode != null) {
            return callNode;
        }

        // lit num
        Token next = parseLiteralNumber();
        if (next != null) {
            LiteralNode node = new LiteralNode();
            node.literal = next;
            return node;
        }

        // tokens
        Token token = next();
        if (token == null) {
            throw new IllegalArgumentException("Unexpected EOF.");
        }

        // TODO: make all expressions negatable with prefix -
        // literal number with prefix -
        if (token.type == Token.Type.OP_MINUS) {
            next = parseLiteralNumber();
            if (next != null && next.type == Token.Type.LIT_NUMBER) {
                next.textRepresentation = "-" + next.textRepresentation;
                LiteralNode node = new LiteralNode();
                node.literal = next;
                return node;
            }
            throw new IllegalArgumentException("Unexpected '-'.");
        }

        // bool/literal
        if (token.type == Token.Type.KW_TRUE || token.type == Token.Type.KW_FALSE || token.getTypeGroup() == Token.TypeGroup.LITERAL) {
            LiteralNode node = new LiteralNode();
            node.literal = token;
            return node;
        }
        // identifier
        if (token.type == Token.Type.IDENTIFIER) {
            IdentifierNode node = new IdentifierNode();
            node.identifier = token;
            return node;
        }

        // "(" expression ")"
        if (token.type == Token.Type.OPEN_PAREN) {
            AST expression = parseExpression();
            if (expression == null) {
                throw new IllegalArgumentException("expected expression after '('.");
            }
            Token closeParen = next();
            if (closeParen == null || closeParen.type != Token.Type.CLOSE_PAREN) {
                throw new IllegalArgumentException("expected ')' after expression.");
            }
            return expression;
        }

        restoreBackup(backup);
        return null;
    }
    private Token parseLiteralNumber() {
        int backup = backup();
        Token token = next();
        if (token == null) {
            restoreBackup(backup);
            return null;
        }

        if (token.type == Token.Type.LIT_NUMBER) { // NUMBER
            Token dot = peek(0);
            if (dot == null || dot.type != Token.Type.DOT) { // NUMBER .
                return token;
            }
            token.textRepresentation += ".";
            next(); // consume dot
            Token next = peek(0);
            if (next == null || next.type != Token.Type.LIT_NUMBER) { // NUMBER . NUMBER
                token.textRepresentation += "0";
                return token;
            }
            token.textRepresentation += next.textRepresentation;
            next(); // consume next
            return token;
        }
        else if (token.type == Token.Type.DOT) { // .
            Token next = peek(0);
            if (next == null || next.type != Token.Type.LIT_NUMBER) { // . NUMBER
                throw new IllegalArgumentException("expected number after '.'");
            }
            token.textRepresentation = "0." + next.textRepresentation;
            next(); // consume next
            return token;
        }

        restoreBackup(backup);
        return null;
    }
    private AST parseFunctionCall() {
        int backup = backup();
        Token iden = next();
        if (iden == null || iden.type != Token.Type.IDENTIFIER) {
            restoreBackup(backup);
            return null;
        }
        Token openParen = next();
        if (openParen == null || openParen.type != Token.Type.OPEN_PAREN) {
            restoreBackup(backup);
            return null;
        }

        CallParamsNode params = (CallParamsNode)parseCallParams();
        FunctionCallNode node = new FunctionCallNode();
        node.funcName = new IdentifierNode();
        node.funcName.identifier = iden;
        node.params = params;

        Token closeParen = next();
        if (closeParen == null || closeParen.type != Token.Type.CLOSE_PAREN) {
            throw new IllegalArgumentException("expected ')' after function call parameters.");
        }

        return node;
    }
    private AST parseCallParams() {
        int backup = backup();
        CallParamsNode params = new CallParamsNode();
        AST expression = parseExpression();
        if (expression == null) {
            restoreBackup(backup);
            return params;
        }
        params.params.add(expression);

        while (true) {
            Token comma = peek(0);
            if (comma == null || comma.type != Token.Type.COMMA) {
                break;
            }
            next(); // consume comma
            expression = parseExpression();
            if (expression == null) {
                throw new IllegalArgumentException("expected expression after comma.");
            }
            params.params.add(expression);
        }
        

        return params;
    }
    private AST parseParamDeclaration() {
        int backup = backup();
        ParamDeclarationNode params = new ParamDeclarationNode();
        DeclarationNode decl = (DeclarationNode)parseVarDecl();
        if (decl == null) {
            restoreBackup(backup);
            return params;
        }
        params.declarations.add(decl);
        while (true) {
            Token comma = peek(0);
            if (comma == null || comma.type != Token.Type.COMMA) {
                break;
            }
            next(); // consume comma
            decl = (DeclarationNode)parseVarDecl();
            if (decl == null) {
                throw new IllegalArgumentException("expected parameter declaration after comma.");
            }
            params.declarations.add(decl);
        }

        return params;
    }

    private ArrayList<Token> m_tokens;
    private int m_index;
    private static final ArrayList<ArrayList<Token.Type>> m_opPrecedenceList = new ArrayList<ArrayList<Token.Type>>() {{
        add(new ArrayList<Token.Type>() {{ 
            add(Token.Type.OP_MULTIPLY); 
            add(Token.Type.OP_DIVIDE); 
            add(Token.Type.OP_MODULO); 
        }});
        add(new ArrayList<Token.Type>() {{
            add(Token.Type.OP_PLUS);
            add(Token.Type.OP_MINUS);
        }});
        add(new ArrayList<Token.Type>() {{
            add(Token.Type.OP_SHIFT_LEFT);
            add(Token.Type.OP_SHIFT_RIGHT);
        }});
        add(new ArrayList<Token.Type>() {{
            add(Token.Type.OP_LESS);
            add(Token.Type.OP_LESS_EQUAL);
            add(Token.Type.OP_GREATER);
            add(Token.Type.OP_GREATER_EQUAL);
        }});
        add(new ArrayList<Token.Type>() {{
            add(Token.Type.OP_EQUAL);
            add(Token.Type.OP_NOT_EQUAL);
        }});
        add(new ArrayList<Token.Type>() {{
            add(Token.Type.OP_BITWISE_AND);
        }});
        add(new ArrayList<Token.Type>() {{
            add(Token.Type.OP_BITWISE_XOR);
        }});
        add(new ArrayList<Token.Type>() {{
            add(Token.Type.OP_BITWISE_OR);
        }});
        add(new ArrayList<Token.Type>() {{
            add(Token.Type.OP_LOGIC_AND);
        }});
        add(new ArrayList<Token.Type>() {{
            add(Token.Type.OP_LOGIC_OR);
        }});
    }};
}
