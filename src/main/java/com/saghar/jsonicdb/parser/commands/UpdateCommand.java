package com.saghar.jsonicdb.parser.commands;

import com.saghar.jsonicdb.core.DataType;
import com.saghar.jsonicdb.core.Database;
import com.saghar.jsonicdb.core.FieldDef;
import com.saghar.jsonicdb.core.ValueType;
import com.saghar.jsonicdb.filter.Filter;
import com.saghar.jsonicdb.filter.FilterParser;
import com.saghar.jsonicdb.json.*;
import com.saghar.jsonicdb.parser.Command;
import com.saghar.jsonicdb.util.JsonicException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class UpdateCommand implements Command {
    private final String typeName;
    private final String filterExpr; // may be null
    private final JsonValue payload;

    public UpdateCommand(String typeName, String filterExpr, JsonValue payload) {
        this.typeName = typeName;
        this.filterExpr = (filterExpr == null || filterExpr.isBlank()) ? null : filterExpr;
        this.payload = payload;
    }

    @Override
    public String execute(Database db) {
        DataType dt = db.getType(typeName);
        if (dt == null) throw new JsonicException(com.saghar.jsonicdb.util.Errors.typeNotFound(typeName));
        if (!(payload instanceof JsonObject obj))
            throw new JsonicException(com.saghar.jsonicdb.util.Errors.invalidSyntax("update"));

        Map<String, Object> updates = new HashMap<>();
        for (Map.Entry<String, JsonValue> e : obj.entries().entrySet()) {
            String fieldKey = e.getKey().toLowerCase();
            FieldDef def = dt.field(fieldKey);
            if (def == null) throw new JsonicException("Field not found: " + e.getKey());
            updates.put(fieldKey, convertJsonToTyped(def.type(), e.getValue()));
        }

        Filter filter = (filterExpr == null) ? Filter.alwaysTrue() : new FilterParser().parse(filterExpr);
        int updated = dt.updateWhere(r -> filter.test(dt, r), updates);
        return updated + " instances updated.";
    }

    private static Object convertJsonToTyped(ValueType type, JsonValue v) {
        return switch (type) {
            case STRING -> {
                if (v instanceof JsonString s) yield s.value();
                throw new JsonicException(com.saghar.jsonicdb.util.Errors.invalidValueForField("<unknown>"));
            }
            case INT -> {
                if (v instanceof JsonNumber n) {
                    try {
                        yield Integer.parseInt(n.raw());
                    } catch (NumberFormatException ex) {
                        throw new JsonicException(com.saghar.jsonicdb.util.Errors.invalidValueForField("<unknown>"));
                    }
                }
                throw new JsonicException(com.saghar.jsonicdb.util.Errors.invalidValueForField("<unknown>"));
            }
            case DOUBLE -> {
                if (v instanceof JsonNumber n) {
                    try {
                        yield Double.parseDouble(n.raw());
                    } catch (NumberFormatException ex) {
                        throw new JsonicException(com.saghar.jsonicdb.util.Errors.invalidValueForField("<unknown>"));
                    }
                }
                throw new JsonicException(com.saghar.jsonicdb.util.Errors.invalidValueForField("<unknown>"));
            }
            case BOOL -> {
                if (v instanceof JsonBoolean b) yield b.value();
                throw new JsonicException(com.saghar.jsonicdb.util.Errors.invalidValueForField("<unknown>"));
            }
            case TIME -> {
                if (v instanceof JsonString s) {
                    try {
                        yield LocalDateTime.parse(s.value());
                    } catch (Exception ex) {
                        throw new JsonicException("Invalid time format. Use ISO_LOCAL_DATE_TIME");
                    }
                }
                throw new JsonicException(com.saghar.jsonicdb.util.Errors.invalidValueForField("<unknown>"));
            }
            case STRING_LIST -> {
                if (v instanceof JsonArray arr) {
                    List<String> out = new ArrayList<>();
                    for (JsonValue item : arr.items()) {
                        if (!(item instanceof JsonString s)) throw new JsonicException("Invalid string_list item");
                        out.add(s.value());
                    }
                    yield List.copyOf(out);
                }
                throw new JsonicException(com.saghar.jsonicdb.util.Errors.invalidValueForField("<unknown>"));
            }
        };
    }
}
