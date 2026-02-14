package com.saghar.jsonicdb.filter;

import com.saghar.jsonicdb.core.DataRecord;
import com.saghar.jsonicdb.core.DataType;
import com.saghar.jsonicdb.core.FieldDef;
import com.saghar.jsonicdb.core.ValueType;
import com.saghar.jsonicdb.filter.FilterParser.Token;
import com.saghar.jsonicdb.util.JsonicException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

final class PredicateFilter implements Filter {
    private final Token a;
    private final Token op;
    private final Token b;

    PredicateFilter(Token a, Token op, Token b) {
        this.a = a;
        this.op = op;
        this.b = b;
    }

    @Override
    public boolean test(DataType type, DataRecord record) {
        String operator = op.text().toLowerCase();
        if (operator.equals("include")) {
            Operand left = Operand.fromToken(a);
            Operand right = Operand.fromToken(b);
            if (!left.isField()) throw new JsonicException("include expects a field on the left");
            FieldDef f = type.field(left.asField());
            if (f == null) throw new JsonicException("Unknown field in filter: " + left.asField());
            if (f.type() != ValueType.STRING_LIST)
                throw new JsonicException("include is only supported for string lists");
            Object lv = record.get(left.asField().toLowerCase());
            if (!(lv instanceof List<?> list)) return false;
            String needle = right.asStringLiteral();
            return list.contains(needle);
        }

        // comparison: allow "field op literal" or "literal op field"
        Operand left = Operand.fromToken(a);
        Operand right = Operand.fromToken(b);

        if (left.isField() && type.field(left.asField()) == null) {
            // if left looks like an identifier but isn't a field, treat as literal
            left = Operand.literal(left.raw());
        }
        if (right.isField() && type.field(right.asField()) == null) {
            right = Operand.literal(right.raw());
        }

        if (left.isField()) {
            String fieldName = left.asField().toLowerCase();
            FieldDef f = type.field(fieldName);
            if (f == null) throw new JsonicException("Unknown field in filter: " + left.asField());
            Object lv = record.get(fieldName);
            Object rv = parseToType(f.type(), right.raw(), right);
            return apply(operator, lv, rv);
        }

        if (right.isField()) {
            String fieldName = right.asField().toLowerCase();
            FieldDef f = type.field(fieldName);
            if (f == null) throw new JsonicException("Unknown field in filter: " + right.asField());
            Object rv = record.get(fieldName);
            Object lv = parseToType(f.type(), left.raw(), left);
            // swap sides => invert comparison operators
            return apply(invert(operator), rv, lv);
        }

        // literal vs literal
        return apply(operator, left.asBestEffortLiteral(), right.asBestEffortLiteral());
    }

    private static Object parseToType(ValueType type, String raw, Operand op) {
        return switch (type) {
            case STRING -> op.asStringLiteral();
            case INT -> op.asIntLiteral();
            case DOUBLE -> op.asDoubleLiteral();
            case BOOL -> op.asBoolLiteral();
            case TIME -> op.asTimeLiteral();
            case STRING_LIST -> throw new JsonicException("Cannot compare a list directly; use include");
        };
    }

    private static boolean apply(String operator, Object left, Object right) {
        return switch (operator) {
            case "=" -> Objects.equals(left, right);
            case "!=" -> !Objects.equals(left, right);
            case "<" -> compare(left, right) < 0;
            case "<=" -> compare(left, right) <= 0;
            case ">" -> compare(left, right) > 0;
            case ">=" -> compare(left, right) >= 0;
            default -> throw new JsonicException("Unknown operator: " + operator);
        };
    }

    private static int compare(Object a, Object b) {
        if (a instanceof Integer ai && b instanceof Integer bi) return Integer.compare(ai, bi);
        if (a instanceof Double ad && b instanceof Double bd) return Double.compare(ad, bd);
        if (a instanceof Number an && b instanceof Number bn) return Double.compare(an.doubleValue(), bn.doubleValue());
        if (a instanceof Boolean ab && b instanceof Boolean bb) return Boolean.compare(ab, bb);
        if (a instanceof LocalDateTime at && b instanceof LocalDateTime bt) return at.compareTo(bt);
        return String.valueOf(a).compareTo(String.valueOf(b));
    }

    private static String invert(String op) {
        return switch (op) {
            case "<" -> ">";
            case ">" -> "<";
            case "<=" -> ">=";
            case ">=" -> "<=";
            default -> op; // =, !=
        };
    }

    // operand helper
    private static final class Operand {
        private final boolean field;
        private final String raw;

        private Operand(boolean field, String raw) {
            this.field = field;
            this.raw = raw;
        }

        static Operand fromToken(Token t) {
            // IDENT is likely a field, but we may later downgrade to literal if field doesn't exist
            boolean isField = (t.type().name().equals("IDENT"));
            if (t.type().name().equals("STRING")) return new Operand(false, "\"" + t.text() + "\"");
            if (t.type().name().equals("BOOL") || t.type().name().equals("NUMBER") || t.type().name().equals("BARE"))
                return new Operand(false, t.text());
            return new Operand(isField, t.text());
        }

        static Operand literal(String raw) {
            return new Operand(false, raw);
        }

        boolean isField() {
            return field;
        }

        String asField() {
            return raw;
        }

        String raw() {
            return raw;
        }

        String asStringLiteral() {
            String t = raw.trim();
            if (t.startsWith("\"") && t.endsWith("\"") && t.length() >= 2) return t.substring(1, t.length() - 1);
            // allow bare string for include
            return t;
        }

        int asIntLiteral() {
            try {
                return Integer.parseInt(stripQuotes(raw));
            } catch (NumberFormatException ex) {
                throw new JsonicException("Invalid int literal in filter: " + raw);
            }
        }

        double asDoubleLiteral() {
            try {
                return Double.parseDouble(stripQuotes(raw));
            } catch (NumberFormatException ex) {
                throw new JsonicException("Invalid double literal in filter: " + raw);
            }
        }

        boolean asBoolLiteral() {
            String t = stripQuotes(raw).toLowerCase();
            if (t.equals("true")) return true;
            if (t.equals("false")) return false;
            throw new JsonicException("Invalid boolean literal in filter: " + raw);
        }

        LocalDateTime asTimeLiteral() {
            try {
                return LocalDateTime.parse(stripQuotes(raw));
            } catch (Exception ex) {
                throw new JsonicException("Invalid time literal in filter: " + raw);
            }
        }

        Object asBestEffortLiteral() {
            String t = stripQuotes(raw);
            if (t.equalsIgnoreCase("true") || t.equalsIgnoreCase("false")) return Boolean.parseBoolean(t);
            if (t.matches("-?\\d+")) {
                try {
                    return Integer.parseInt(t);
                } catch (Exception ignored) {
                }
            }
            if (t.matches("-?\\d+(\\.\\d+)?")) {
                try {
                    return Double.parseDouble(t);
                } catch (Exception ignored) {
                }
            }
            try {
                return LocalDateTime.parse(t);
            } catch (Exception ignored) {
            }
            return t;
        }

        private static String stripQuotes(String s) {
            String t = s.trim();
            if (t.startsWith("\"") && t.endsWith("\"") && t.length() >= 2) return t.substring(1, t.length() - 1);
            return t;
        }
    }
}
