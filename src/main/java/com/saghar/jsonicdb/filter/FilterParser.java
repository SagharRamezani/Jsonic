package com.saghar.jsonicdb.filter;

import com.saghar.jsonicdb.util.JsonicException;

import java.util.ArrayList;
import java.util.List;

public final class FilterParser {
    private List<Token> tokens;
    private int pos;

    public Filter parse(String expr) {
        this.tokens = tokenize(expr);
        this.pos = 0;
        Filter f = parseOr();
        expect(TokenType.EOF);
        return f;
    }

    // OR is lowest precedence
    private Filter parseOr() {
        Filter left = parseAnd();
        while (matchKeyword("or")) {
            Filter right = parseAnd();
            Filter l = left;
            left = (t, r) -> l.test(t, r) || right.test(t, r);
        }
        return left;
    }

    private Filter parseAnd() {
        Filter left = parseAtom();
        while (matchKeyword("and")) {
            Filter right = parseAtom();
            Filter l = left;
            left = (t, r) -> l.test(t, r) && right.test(t, r);
        }
        return left;
    }

    private Filter parseAtom() {
        if (match(TokenType.LPAREN)) {
            Filter inside = parseOr();
            expect(TokenType.RPAREN);
            return inside;
        }
        return parseComparison();
    }

    private Filter parseComparison() {
        Token a = expect(TokenType.IDENT, TokenType.STRING, TokenType.NUMBER, TokenType.BOOL, TokenType.BARE);
        Token op = expect(TokenType.OP, TokenType.KEYWORD);
        Token b = expect(TokenType.IDENT, TokenType.STRING, TokenType.NUMBER, TokenType.BOOL, TokenType.BARE);

        return new PredicateFilter(a, op, b);
    }

    // --- tokenization ---
    private static List<Token> tokenize(String s) {
        List<Token> out = new ArrayList<>();
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            if (c == '(') {
                out.add(new Token(TokenType.LPAREN, "("));
                i++;
                continue;
            }
            if (c == ')') {
                out.add(new Token(TokenType.RPAREN, ")"));
                i++;
                continue;
            }

            // operators
            if (c == '<' || c == '>' || c == '=' || c == '!') {
                int j = i + 1;
                if (j < s.length() && s.charAt(j) == '=') j++;
                out.add(new Token(TokenType.OP, s.substring(i, j)));
                i = j;
                continue;
            }

            // string literal
            if (c == '"') {
                StringBuilder sb = new StringBuilder();
                i++; // skip "
                while (i < s.length()) {
                    char ch = s.charAt(i++);
                    if (ch == '"') break;
                    if (ch == '\\') {
                        if (i >= s.length()) throw new JsonicException("Invalid string literal in filter");
                        char esc = s.charAt(i++);
                        sb.append(switch (esc) {
                            case '"', '\\' -> esc;
                            case 'n' -> '\n';
                            case 't' -> '\t';
                            case 'r' -> '\r';
                            default -> esc;
                        });
                    } else {
                        sb.append(ch);
                    }
                }
                out.add(new Token(TokenType.STRING, sb.toString()));
                continue;
            }

            // identifier/keyword/number/bool/bare
            int j = i;
            while (j < s.length() && !Character.isWhitespace(s.charAt(j)) && "()".indexOf(s.charAt(j)) == -1) {
                // stop before operators
                char ch = s.charAt(j);
                if (ch == '<' || ch == '>' || ch == '=' || ch == '!') break;
                j++;
            }
            String word = s.substring(i, j);
            String lw = word.toLowerCase();
            if (lw.equals("and") || lw.equals("or") || lw.equals("include")) {
                out.add(new Token(TokenType.KEYWORD, lw));
            } else if (lw.equals("true") || lw.equals("false")) {
                out.add(new Token(TokenType.BOOL, lw));
            } else if (word.matches("-?\\d+(\\.\\d+)?")) {
                out.add(new Token(TokenType.NUMBER, word));
            } else if (word.matches("[A-Za-z_][A-Za-z0-9_]*")) {
                out.add(new Token(TokenType.IDENT, word));
            } else {
                // fallback: allow values like 2024-01-01T12:30:00 without quotes
                out.add(new Token(TokenType.BARE, word));
            }
            i = j;
        }
        out.add(new Token(TokenType.EOF, ""));
        return out;
    }

    // --- parsing helpers ---
    private boolean match(TokenType t) {
        if (peek().type == t) {
            pos++;
            return true;
        }
        return false;
    }

    private boolean matchKeyword(String kw) {
        Token p = peek();
        if (p.type == TokenType.KEYWORD && p.text.equalsIgnoreCase(kw)) {
            pos++;
            return true;
        }
        return false;
    }

    private Token expect(TokenType... types) {
        Token p = peek();
        for (TokenType t : types) {
            if (p.type == t) {
                pos++;
                return p;
            }
        }
        throw new JsonicException("Invalid filter near: " + p.text);
    }

    private void expect(TokenType t) {
        Token p = peek();
        if (p.type != t) throw new JsonicException("Invalid filter near: " + p.text);
        pos++;
    }

    private Token peek() {
        return tokens.get(pos);
    }

    enum TokenType {LPAREN, RPAREN, OP, KEYWORD, IDENT, STRING, NUMBER, BOOL, BARE, EOF}

    record Token(TokenType type, String text) {
    }
}
