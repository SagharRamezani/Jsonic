package com.saghar.jsonicdb.json;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class JsonObject implements JsonValue {
    private final Map<String, JsonValue> map;

    public JsonObject(Map<String, JsonValue> map) {
        this.map = new LinkedHashMap<>(map);
    }

    public Map<String, JsonValue> entries() {
        return Collections.unmodifiableMap(map);
    }

    public JsonValue get(String key) {
        return map.get(key);
    }
}
