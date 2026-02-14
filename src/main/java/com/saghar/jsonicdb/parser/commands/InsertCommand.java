package com.saghar.jsonicdb.parser.commands;

import com.saghar.jsonicdb.core.DataType;
import com.saghar.jsonicdb.core.Database;
import com.saghar.jsonicdb.core.FieldDef;
import com.saghar.jsonicdb.core.ValueType;
import com.saghar.jsonicdb.json.*;
import com.saghar.jsonicdb.parser.Command;
import com.saghar.jsonicdb.util.JsonicException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InsertCommand implements Command {
    private final String typeName;
    private final JsonValue payload;

    public InsertCommand(String typeName, JsonValue payload) {
        this.typeName = typeName;
        this.payload = payload;
    }

    @Override
    public String execute(Database db) {
        DataType dt = db.getType(typeName);
        if (dt == null) throw new JsonicException(com.saghar.jsonicdb.util.Errors.typeNotFound(typeName));
        if (!(payload instanceof JsonObject obj))
            throw new JsonicException(com.saghar.jsonicdb.util.Errors.invalidSyntax("insert"));

        Map<String, Object> values = new HashMap<>();
        for (Map.Entry<String, JsonValue> e : obj.entries().entrySet()) {
            String fieldKey = e.getKey().toLowerCase();
            FieldDef def = dt.field(fieldKey);
            if (def == null) throw new JsonicException(com.saghar.jsonicdb.util.Errors.fieldNotFound(e.getKey()));

            Object converted = convertJsonToTyped(def, e.getValue());
            values.put(fieldKey, converted);
        }

        dt.insert(values);
        return "Instance inserted into '" + dt.name() + "'.";
    }

    private static Object convertJsonToTyped(FieldDef def, JsonValue v) {
        ValueType type = def.type();
        return switch (type) {
            case STRING -> {
                if (v instanceof JsonString s) yield s.value();
                throw new JsonicException(com.saghar.jsonicdb.util.Errors.invalidValueForField(def.name()));
            }
            case INT -> {
                if (v instanceof JsonNumber n) {
                    try {
                        yield Integer.parseInt(n.raw());
                    } catch (NumberFormatException ex) {
                        throw new JsonicException(com.saghar.jsonicdb.util.Errors.invalidValueForField(def.name()));
                    }
                }
                throw new JsonicException(com.saghar.jsonicdb.util.Errors.invalidValueForField(def.name()));
            }
            case DOUBLE -> {
                if (v instanceof JsonNumber n) {
                    try {
                        yield Double.parseDouble(n.raw());
                    } catch (NumberFormatException ex) {
                        throw new JsonicException(com.saghar.jsonicdb.util.Errors.invalidValueForField(def.name()));
                    }
                }
                throw new JsonicException(com.saghar.jsonicdb.util.Errors.invalidValueForField(def.name()));
            }
            case BOOL -> {
                if (v instanceof JsonBoolean b) yield b.value();
                throw new JsonicException(com.saghar.jsonicdb.util.Errors.invalidValueForField(def.name()));
            }
            case TIME -> {
                if (v instanceof JsonString s) {
                    try {
                        yield LocalDateTime.parse(s.value());
                    } catch (Exception ex) {
                        throw new JsonicException(com.saghar.jsonicdb.util.Errors.invalidValueForField(def.name()));
                    }
                }
                throw new JsonicException(com.saghar.jsonicdb.util.Errors.invalidValueForField(def.name()));
            }
            case STRING_LIST -> {
                if (v instanceof JsonArray arr) {
                    List<String> out = new ArrayList<>();
                    for (JsonValue item : arr.items()) {
                        if (!(item instanceof JsonString s))
                            throw new JsonicException(com.saghar.jsonicdb.util.Errors.invalidValueForField(def.name()));
                        out.add(s.value());
                    }
                    yield List.copyOf(out);
                }
                throw new JsonicException(com.saghar.jsonicdb.util.Errors.invalidValueForField(def.name()));
            }
        };
    }
}
