package com.saghar.jsonicdb.core;

import com.saghar.jsonicdb.util.JsonicException;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public enum ValueType {
    STRING,
    INT,
    DOUBLE,
    BOOL,
    TIME,
    STRING_LIST;

    public static ValueType fromSpec(String spec) {
        String s = spec.trim().toLowerCase();
        return switch (s) {
            case "string" -> STRING;
            case "int" -> INT;
            case "dbl", "double" -> DOUBLE;
            case "bool", "boolean" -> BOOL;
            case "time" -> TIME;
            case "arr_string", "list_string", "string_list" -> STRING_LIST;
            default -> throw new JsonicException("Invalid type: " + spec);
        };
    }

    public Object defaultValue() {
        return switch (this) {
            case STRING -> "";
            case INT -> 0;
            case DOUBLE -> 0.0;
            case BOOL -> false;
            case TIME -> LocalDateTime.of(1970, 1, 1, 0, 0, 0);
            case STRING_LIST -> List.of();
        };
    }

    public Object parseLiteral(String raw) {
        String t = raw.trim();
        return switch (this) {
            case STRING -> {
                if (t.startsWith("\"") && t.endsWith("\"") && t.length() >= 2) yield t.substring(1, t.length() - 1);
                throw new JsonicException("Invalid string literal: " + raw);
            }
            case INT -> {
                try { yield Integer.parseInt(t); }
                catch (NumberFormatException e) { throw new JsonicException("Invalid int literal: " + raw); }
            }
            case DOUBLE -> {
                try { yield Double.parseDouble(t); }
                catch (NumberFormatException e) { throw new JsonicException("Invalid double literal: " + raw); }
            }
            case BOOL -> {
                if (t.equalsIgnoreCase("true")) yield true;
                if (t.equalsIgnoreCase("false")) yield false;
                throw new JsonicException("Invalid boolean literal: " + raw);
            }
            case TIME -> {
                // Accept either quoted or bare ISO-8601 LocalDateTime (e.g. 2024-01-01T12:30:00)
                String v = (t.startsWith("\"") && t.endsWith("\"") && t.length() >= 2) ? t.substring(1, t.length() - 1) : t;
                try { yield LocalDateTime.parse(v); }
                catch (DateTimeParseException e) { throw new JsonicException("Invalid time literal. Use ISO_LOCAL_DATE_TIME like 2024-01-01T12:30:00"); }
            }
            case STRING_LIST -> throw new JsonicException("STRING_LIST cannot be parsed from a single literal");
        };
    }
}
