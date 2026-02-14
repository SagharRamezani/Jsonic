package com.saghar.jsonicdb.core;

import com.saghar.jsonicdb.util.JsonicException;

import java.util.HashMap;
import java.util.Map;

public final class Database {
    private final Map<String, DataType> types = new HashMap<>();

    public DataType getType(String name) {
        return types.get(canon(name));
    }

    public DataType createType(String name) {
        String key = canon(name);
        if (types.containsKey(key)) throw new JsonicException("Data type already exists: " + name);
        DataType dt = new DataType(name);
        types.put(key, dt);
        return dt;
    }

    private static String canon(String s) { return s.trim().toLowerCase(); }
}
