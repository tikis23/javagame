package com.javasl.compiler;

import java.util.ArrayList;
import java.util.HashMap;

public class Tokenizer {
    public ArrayList<Token> tokenize(String content) throws IllegalArgumentException {
        if (content == null) {
            throw new IllegalArgumentException("Tokenizer error: content is null.");
        }
        String file = content.trim();        

        // convert escape characters
        file = file
            .replace("\\b", "\b")
            .replace("\\t", "\t")
            .replace("\\n", "\n")
            .replace("\\f", "\f")
            .replace("\\r", "\r")
            .replace("\\\"", "\"")
            .replace("\\'", "'")
            .replace("\\\\", "\\");

        // read tokens
        ArrayList<Token> tokens = new ArrayList<Token>();
        int charOffset = 0;
        FileToken fileToken = getNextFileToken(file, charOffset);
        while (!fileToken.endOfFile) {
            if (fileToken.token.type == Token.Type.UNKNOWN) {
                TokenizerError error = getError(file, charOffset, fileToken.token);
                throw new IllegalArgumentException("Tokenizer error: " + error.message + " at line " + error.line + ":" + error.column + ".");
            }
            tokens.add(fileToken.token);
            charOffset = fileToken.tokenEndOffset;
            fileToken = getNextFileToken(file, charOffset);
        }
        tokens.add(fileToken.token);
        if (fileToken.token.type == Token.Type.UNKNOWN) {
            TokenizerError error = getError(file, charOffset, fileToken.token);
            throw new IllegalArgumentException("Tokenizer error: " + error.message + " at line " + error.line + ":" + error.column + ".");
        }

        return tokens;
    }

    private static class TokenizerError {
        public String message;
        public int line;
        public int column;
    }
    private TokenizerError getError(String file, int charOffset, Token token) {
        TokenizerError error = new TokenizerError();
        error.message = "Unkown token " + token.textRepresentation;
        error.line = 1;
        error.column = 1;
        for (int i = 0; i < charOffset; i++) {
            if (file.charAt(i) == '\n') {
                error.line++;
                error.column = 1;
            } else {
                error.column++;
            }
        }
        return error;
    }

    private static class FileToken {
        public boolean endOfFile = false;
        public int tokenEndOffset = 0;
        public Token token = null;
    }

    private FileToken getNextFileToken(String file, int charOffset) {
        String tokenRepresentation = "";
        boolean skipWhitespace = true;
        boolean canBeString = true;
        boolean isString = false;
        boolean isChar = false;
        boolean isComment = false;

        while (charOffset < file.length()) {
            // get next character
            int codePoint = file.codePointAt(charOffset);
            int charCount = Character.charCount(codePoint);
            charOffset += charCount;
            // get extra characters for peeking ahead
            char c2 = 0;
            int c2CharCount = 0;
            char c3 = 0;
            int c3CharCount = 0;
            if (charOffset < file.length()) {
                c2CharCount = Character.charCount(file.codePointAt(charOffset));
                if (c2CharCount == 1) {
                    c2 = Character.toChars(file.codePointAt(charOffset))[0];
                    if (charOffset + c2CharCount < file.length()) {
                        c3CharCount = Character.charCount(file.codePointAt(charOffset + c2CharCount));
                        if (c3CharCount == 1) {
                            c3 = Character.toChars(file.codePointAt(charOffset + c2CharCount))[0];
                        }
                    }
                }
            }

            // consume comment
            if (isComment) {
                if (charCount == 1 && Character.toChars(codePoint)[0] == '\n') {
                    break;
                }
                tokenRepresentation += new String(Character.toChars(codePoint));
                continue;
            }

            // Consume whitespace
            if (Character.isWhitespace(codePoint)) {
                if (skipWhitespace) {
                    continue;
                } else if (!isString) {
                    break;
                }
            }
            skipWhitespace = false;

            if (charCount == 1) { // codepoint is char
                char c = Character.toChars(codePoint)[0];
                
                // check if comment
                if (c == '/' && c2 == '/') {
                    if (tokenRepresentation.length() == 0) {
                        isComment = true;
                        charOffset += c2CharCount;
                        continue;
                    } else {
                        charOffset -= charCount;
                        break;
                    }
                }

                // check if char is start/end of string
                if (c == '"') {
                    if (isString) break; // end string and token
                    if (canBeString) isString = true; // start string
                    continue;
                }

                // check if there is char literal
                if (c == '\'' && c3 == '\'' && !isString) {
                    if (tokenRepresentation.length() == 0) {
                        isChar = true;
                        tokenRepresentation += c2;
                        charOffset += c2CharCount + c3CharCount;
                    } else {
                        charOffset -= charCount;
                    }
                    break;
                }
                
                // other checks are only needed if not in string
                if (isString) {
                    tokenRepresentation += c;
                    continue;
                }
                
                // check if separator
                if (separators.containsKey(c)) {
                    if (tokenRepresentation.length() == 0) {
                        tokenRepresentation += c;
                    } else {
                        charOffset -= charCount;
                    }
                    break;
                }

                // check if operator with 2 chars
                if (c2 != 0 && operators.containsKey(Character.toString(c) + c2)) {
                    if (tokenRepresentation.length() == 0) {
                        tokenRepresentation += Character.toString(c) + c2;
                        charOffset += c2CharCount;
                    } else {
                        charOffset -= charCount;
                    }
                    break;
                }
                // check if operator with 1 char
                if (operators.containsKey(Character.toString(c))) {
                    if (tokenRepresentation.length() == 0) {
                        tokenRepresentation += c;
                    } else {
                        charOffset -= charCount;
                    }
                    break;
                }

                // else add char to token
                tokenRepresentation += c;
            } else { // codepoint is not char, so it can only be part of names/strings etc. so no need to check for separators and stuff
                tokenRepresentation += new String(Character.toChars(codePoint));
            }
            canBeString = false;
        }

        // construct token
        FileToken fileToken = new FileToken();
        if (charOffset >= file.length()) {
            fileToken.endOfFile = true;
        }
        fileToken.tokenEndOffset = charOffset;
        fileToken.token = new Token();
        fileToken.token.textRepresentation = tokenRepresentation;
        if (isString) {
            fileToken.token.type = Token.Type.LIT_STRING;
        } else if (isChar) {
            fileToken.token.type = Token.Type.LIT_CHAR;
        } else if (isComment) {
            fileToken.token.type = Token.Type.COMMENT;
        } else {
            fileToken.token.type = GetTokenType(tokenRepresentation);
        }

        return fileToken;
    }

    private Token.Type GetTokenType(String token) {
        // Strings, chars and comments are done in the tokenizer

        // separators
        if (separators.containsKey(token.charAt(0))) {
            return separators.get(token.charAt(0));
        }
        // operators
        if (operators.containsKey(token)) {
            return operators.get(token);
        }

        // keywords
        if (keywords.containsKey(token)) {
            return keywords.get(token);
        }

        // lit numbers
        if (token.matches("^[0-9]+$")) {
            return Token.Type.LIT_NUMBER;
        }

        // identifiers
        boolean isIdentifier = true;
        boolean first = true;
        for (int charCount = 0; charCount < token.length();) {
            int codePoint = token.codePointAt(charCount);
            charCount += Character.charCount(codePoint);
            // check if starts with letter and then only letters and digits
            // underscore is allowed anywhere
            if (((first && !Character.isLetter(codePoint)) || (!first && !Character.isLetterOrDigit(codePoint))) &&
                (charCount == 1 && Character.toChars(codePoint)[0] != '_')) {
                isIdentifier = false;
                break;
            }
            first = false;
        }
        if (isIdentifier) {
            return Token.Type.IDENTIFIER;
        }

        return Token.Type.UNKNOWN;
    }

    private static final HashMap<Character, Token.Type> separators = new HashMap<Character, Token.Type>() {{
        put('(', Token.Type.OPEN_PAREN);
        put(')', Token.Type.CLOSE_PAREN);
        put('{', Token.Type.OPEN_CURLY_BRACKET);
        put('}', Token.Type.CLOSE_CURLY_BRACKET);
        put('[', Token.Type.OPEN_SQUARE_BRACKET);
        put(']', Token.Type.CLOSE_SQUARE_BRACKET);
        put(';', Token.Type.SEMICOLON);
        put(',', Token.Type.COMMA);
        put('.', Token.Type.DOT);
    }};
    private static final HashMap<String, Token.Type> operators = new HashMap<String, Token.Type>() {{
        put("+",  Token.Type.OP_PLUS);
        put("-",  Token.Type.OP_MINUS);
        put("*",  Token.Type.OP_MULTIPLY);
        put("/",  Token.Type.OP_DIVIDE);
        put("%",  Token.Type.OP_MODULO);
        put("=",  Token.Type.OP_ASSIGN);
        put("==", Token.Type.OP_EQUAL);
        put("!=", Token.Type.OP_NOT_EQUAL);
        put("<",  Token.Type.OP_LESS);
        put("<=", Token.Type.OP_LESS_EQUAL);
        put(">",  Token.Type.OP_GREATER);
        put(">=", Token.Type.OP_GREATER_EQUAL);
        put("!",  Token.Type.OP_LOGIC_NOT);
        put("&&", Token.Type.OP_LOGIC_AND);
        put("||", Token.Type.OP_LOGIC_OR);
        put("<<", Token.Type.OP_SHIFT_LEFT);
        put(">>", Token.Type.OP_SHIFT_RIGHT);
        put("~",  Token.Type.OP_BITWISE_NOT);
        put("&",  Token.Type.OP_BITWISE_AND);
        put("|",  Token.Type.OP_BITWISE_OR);
        put("^",  Token.Type.OP_BITWISE_XOR);
    }};
    private static final HashMap<String, Token.Type> keywords = new HashMap<String, Token.Type>() {{
        // types
        put("void",   Token.Type.T_VOID);
        put("int8",   Token.Type.T_INT8);
        put("int16",  Token.Type.T_INT16);
        put("int32",  Token.Type.T_INT32);
        put("int64",  Token.Type.T_INT64);
        put("uint8",  Token.Type.T_UINT8);
        put("uint16", Token.Type.T_UINT16);
        put("uint32", Token.Type.T_UINT32);
        put("uint64", Token.Type.T_UINT64);
        put("float",  Token.Type.T_FLOAT);
        put("double", Token.Type.T_DOUBLE);
        put("bool",   Token.Type.T_BOOL);
        put("string", Token.Type.T_STRING);
        put("char",   Token.Type.T_CHAR);
        // keywords
        put("if",       Token.Type.KW_IF);
        put("else",     Token.Type.KW_ELSE);
        put("do",       Token.Type.KW_DO);
        put("while",    Token.Type.KW_WHILE);
        put("for",      Token.Type.KW_FOR);
        put("break",    Token.Type.KW_BREAK);
        put("continue", Token.Type.KW_CONTINUE);
        put("return",   Token.Type.KW_RETURN);
        put("true",     Token.Type.KW_TRUE);
        put("false",    Token.Type.KW_FALSE);
        put("null",     Token.Type.KW_NULL);
    }};
}
