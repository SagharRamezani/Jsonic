package com.saghar.jsonicdb.json;

import java.util.Collections;
import java.util.List;

public final class JsonArray implements JsonValue {
    private final List<JsonValue> items;

    public JsonArray(List<JsonValue> items) {
        this.items = List.copyOf(items);
    }

    public List<JsonValue> items() { return Collections.unmodifiableList(items); }
}
