package com.saghar.jsonicdb.parser.commands;

import com.saghar.jsonicdb.core.*;
import com.saghar.jsonicdb.json.*;
import com.saghar.jsonicdb.parser.Command;
import com.saghar.jsonicdb.util.JsonicException;

import java.util.Map;

public final class CreateTypeCommand implements Command {
    private final String typeName;
    private final JsonValue payload;

    public CreateTypeCommand(String typeName, JsonValue payload) {
        this.typeName = typeName;
        this.payload = payload;
    }

    @Override
    public String execute(Database db) {
        if (!(payload instanceof JsonObject obj)) throw new JsonicException("Invalid create syntax: expected JSON object");
        DataType dt = db.createType(typeName);

        for (Map.Entry<String, JsonValue> e : obj.entries().entrySet()) {
            String fieldName = e.getKey();
            if (!(e.getValue() instanceof JsonObject props)) {
                throw new JsonicException("Invalid field definition for: " + fieldName);
            }
            String typeSpec = readString(props, "type", "string");
            boolean required = readBoolean(props, "required", false);
            boolean unique = readBoolean(props, "unique", false);

            FieldDef def = new FieldDef(fieldName, ValueType.fromSpec(typeSpec), required, unique);
            dt.addField(def);
        }

        return "Type '" + typeName + "' created (" + dt.fields().size() + " fields).";
    }

    private static String readString(JsonObject obj, String key, String def) {
        JsonValue v = obj.get(key);
        if (v == null) return def;
        if (v instanceof JsonString s) return s.value();
        // allow bare identifiers/numbers as strings in create (compat mode)
        if (v instanceof JsonNumber n) return n.raw();
        if (v instanceof JsonBoolean b) return String.valueOf(b.value());
        throw new JsonicException("Invalid property '" + key + "'");
    }

    private static boolean readBoolean(JsonObject obj, String key, boolean def) {
        JsonValue v = obj.get(key);
        if (v == null) return def;
        if (v instanceof JsonBoolean b) return b.value();
        if (v instanceof JsonString s) return Boolean.parseBoolean(s.value());
        throw new JsonicException("Invalid boolean for '" + key + "'");
    }
}
