package com.saghar.jsonicdb.parser;

import com.saghar.jsonicdb.json.JsonParser;
import com.saghar.jsonicdb.json.JsonValue;
import com.saghar.jsonicdb.util.Checks;
import com.saghar.jsonicdb.util.JsonicException;

public final class CommandParser {

    public Command parse(String line) {
        String trimmed = line.trim();
        int sp = trimmed.indexOf(' ');
        String action = (sp == -1 ? trimmed : trimmed.substring(0, sp)).toLowerCase();
        String rest = (sp == -1 ? "" : trimmed.substring(sp + 1).trim());

        return switch (action) {
            case "create" -> parseCreate(rest);
            case "insert" -> parseInsert(rest);
            case "search" -> parseSearch(rest);
            case "update" -> parseUpdate(rest);
            case "delete" -> parseDelete(rest);
            default -> throw new JsonicException(com.saghar.jsonicdb.util.Errors.invalidCommand(action));
        };
    }

    private Command parseCreate(String rest) {
        String type = readFirstToken(rest);
        Checks.require(!type.isBlank(), "Invalid create syntax");
        String payload = rest.substring(type.length()).trim();
        JsonValue v = parseJson(payload, "create");
        return new com.saghar.jsonicdb.parser.commands.CreateTypeCommand(type, v);
    }

    private Command parseInsert(String rest) {
        String type = readFirstToken(rest);
        Checks.require(!type.isBlank(), "Invalid insert syntax");
        String payload = rest.substring(type.length()).trim();
        JsonValue v = parseJson(payload, "insert");
        return new com.saghar.jsonicdb.parser.commands.InsertCommand(type, v);
    }

    private Command parseSearch(String rest) {
        String type = readFirstToken(rest);
        Checks.require(!type.isBlank(), "Invalid search syntax");
        String afterType = rest.substring(type.length()).trim();
        String filter = extractOptionalParenExpr(afterType);
        return new com.saghar.jsonicdb.parser.commands.SearchCommand(type, filter);
    }

    private Command parseDelete(String rest) {
        String type = readFirstToken(rest);
        Checks.require(!type.isBlank(), "Invalid delete syntax");
        String afterType = rest.substring(type.length()).trim();
        String filter = extractOptionalParenExpr(afterType);
        return new com.saghar.jsonicdb.parser.commands.DeleteCommand(type, filter);
    }

    private Command parseUpdate(String rest) {
        String type = readFirstToken(rest);
        Checks.require(!type.isBlank(), "Invalid update syntax");
        String afterType = rest.substring(type.length()).trim();

        String filter = null;
        String remaining = afterType;

        if (remaining.startsWith("(")) {
            // In update, the filter may be followed by a JSON object, so we only
            // extract the *leading* parenthesized expression.
            int end = findMatchingParen(remaining, 0);
            filter = remaining.substring(1, end).trim();
            remaining = remaining.substring(end + 1).trim();
        }

        JsonValue updates = parseJson(remaining, "update");
        return new com.saghar.jsonicdb.parser.commands.UpdateCommand(type, filter, updates);
    }

    private static JsonValue parseJson(String payload, String cmd) {
        Checks.require(payload.startsWith("{"), "Invalid " + cmd + " syntax: expected JSON object");
        try {
            return JsonParser.parse(payload);
        } catch (JsonicException ex) {
            throw new JsonicException(com.saghar.jsonicdb.util.Errors.invalidJsonFormat());
        }
    }

    private static String readFirstToken(String s) {
        String t = s.trim();
        if (t.isEmpty()) return "";
        int i = 0;
        while (i < t.length() && !Character.isWhitespace(t.charAt(i))) i++;
        return t.substring(0, i);
    }

    private static String extractOptionalParenExpr(String s) {
        String t = s.trim();
        if (t.isEmpty()) return null;
        if (!t.startsWith("(")) throw new JsonicException(com.saghar.jsonicdb.util.Errors.invalidFilter(""));
        int end = findMatchingParen(t, 0);
        String inside = t.substring(1, end).trim();
        String after = t.substring(end + 1).trim();
        if (!after.isEmpty()) throw new JsonicException(com.saghar.jsonicdb.util.Errors.invalidFilter(""));
        return inside;
    }

    private static int findMatchingParen(String s, int openIndex) {
        int depth = 0;
        for (int i = openIndex; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(') depth++;
            else if (c == ')') {
                depth--;
                if (depth == 0) return i;
            }
        }
        throw new JsonicException(com.saghar.jsonicdb.util.Errors.invalidFilter(""));
    }
}
