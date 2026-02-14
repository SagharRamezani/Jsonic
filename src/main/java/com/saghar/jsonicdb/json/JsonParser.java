package com.saghar.jsonicdb.json;

import com.saghar.jsonicdb.util.JsonicException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonParser {
    private final String s;
    private int i;

    private JsonParser(String s) {
        this.s = s;
        this.i = 0;
    }

    public static JsonValue parse(String input) {
        JsonParser p = new JsonParser(input);
        JsonValue v = p.parseValue();
        p.skipWs();
        if (!p.eof()) throw new JsonicException("Invalid JSON: trailing characters");
        return v;
    }

    private JsonValue parseValue() {
        skipWs();
        if (eof()) throw new JsonicException("Invalid JSON: unexpected end");
        char c = peek();
        if (c == '{') return parseObject();
        if (c == '[') return parseArray();
        if (c == '"') return new JsonString(parseString());
        if (c == 't' || c == 'f') return parseBoolean();
        if (c == 'n') return parseNull();
        if (c == '-' || Character.isDigit(c)) return parseNumber();
        throw new JsonicException("Invalid JSON: unexpected character '" + c + "'");
    }

    private JsonObject parseObject() {
        expect('{');
        skipWs();
        Map<String, JsonValue> map = new LinkedHashMap<>();
        if (tryConsume('}')) return new JsonObject(map);

        while (true) {
            skipWs();
            String key = parseKey();
            skipWs();
            expect(':');
            JsonValue v = parseValue();
            map.put(key, v);
            skipWs();
            if (tryConsume('}')) break;
            expect(',');
        }
        return new JsonObject(map);
    }

    private String parseKey() {
        skipWs();
        if (peek() == '"') return parseString();
        // allow bare identifiers (compat with original project)
        StringBuilder sb = new StringBuilder();
        while (!eof()) {
            char c = peek();
            if (Character.isLetterOrDigit(c) || c == '_' ) {
                sb.append(c); i++;
            } else break;
        }
        if (sb.isEmpty()) throw new JsonicException("Invalid JSON object key");
        return sb.toString();
    }

    private JsonArray parseArray() {
        expect('[');
        skipWs();
        List<JsonValue> items = new ArrayList<>();
        if (tryConsume(']')) return new JsonArray(items);

        while (true) {
            items.add(parseValue());
            skipWs();
            if (tryConsume(']')) break;
            expect(',');
        }
        return new JsonArray(items);
    }

    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (!eof()) {
            char c = next();
            if (c == '"') return sb.toString();
            if (c == '\\') {
                if (eof()) throw new JsonicException("Invalid JSON string escape");
                char e = next();
                switch (e) {
                    case '"', '\\', '/' -> sb.append(e);
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        if (i + 4 > s.length()) throw new JsonicException("Invalid JSON unicode escape");
                        String hex = s.substring(i, i + 4);
                        try {
                            sb.append((char) Integer.parseInt(hex, 16));
                        } catch (NumberFormatException ex) {
                            throw new JsonicException("Invalid JSON unicode escape");
                        }
                        i += 4;
                    }
                    default -> throw new JsonicException("Invalid JSON escape: \\" + e);
                }
            } else {
                sb.append(c);
            }
        }
        throw new JsonicException("Invalid JSON string: missing closing quote");
    }

    private JsonValue parseBoolean() {
        if (matchAhead("true")) { i += 4; return new JsonBoolean(true); }
        if (matchAhead("false")) { i += 5; return new JsonBoolean(false); }
        throw new JsonicException("Invalid JSON boolean literal");
    }

    private JsonValue parseNull() {
        if (matchAhead("null")) { i += 4; return JsonNull.INSTANCE; }
        throw new JsonicException("Invalid JSON null literal");
    }

    private JsonValue parseNumber() {
        int start = i;
        if (peek() == '-') i++;
        while (!eof() && Character.isDigit(peek())) i++;
        if (!eof() && peek() == '.') {
            i++;
            while (!eof() && Character.isDigit(peek())) i++;
        }
        if (!eof() && (peek() == 'e' || peek() == 'E')) {
            i++;
            if (!eof() && (peek() == '+' || peek() == '-')) i++;
            while (!eof() && Character.isDigit(peek())) i++;
        }
        return new JsonNumber(s.substring(start, i));
    }

    private void skipWs() {
        while (!eof() && Character.isWhitespace(peek())) i++;
    }

    private boolean eof() { return i >= s.length(); }
    private char peek() { return s.charAt(i); }
    private char next() { return s.charAt(i++); }

    private void expect(char c) {
        skipWs();
        if (eof() || s.charAt(i) != c) throw new JsonicException("Invalid JSON: expected '" + c + "'");
        i++;
    }

    private boolean tryConsume(char c) {
        skipWs();
        if (!eof() && s.charAt(i) == c) { i++; return true; }
        return false;
    }

    private boolean matchAhead(String lit) {
        return s.regionMatches(i, lit, 0, lit.length());
    }
}
