package com.saghar.jsonicdb.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class DataRecord {
    private final Map<String, Object> values = new HashMap<>();

    public Object get(String field) {
        return values.get(field);
    }

    public void put(String field, Object value) {
        values.put(field, value);
    }

    public Map<String, Object> snapshot() {
        return Collections.unmodifiableMap(values);
    }
}
