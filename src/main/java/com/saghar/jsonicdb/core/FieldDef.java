package com.saghar.jsonicdb.core;

import com.saghar.jsonicdb.util.Checks;

public final class FieldDef {
    private final String name;
    private final ValueType type;
    private final boolean required;
    private final boolean unique;

    public FieldDef(String name, ValueType type, boolean required, boolean unique) {
        Checks.require(name != null && !name.isBlank(), "Field name is empty");
        this.name = name;
        this.type = type;
        this.required = required;
        this.unique = unique;
    }

    public String name() {
        return name;
    }

    public ValueType type() {
        return type;
    }

    public boolean required() {
        return required;
    }

    public boolean unique() {
        return unique;
    }
}
