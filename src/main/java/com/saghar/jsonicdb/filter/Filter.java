package com.saghar.jsonicdb.filter;

import com.saghar.jsonicdb.core.DataType;
import com.saghar.jsonicdb.core.DataRecord;

@FunctionalInterface
public interface Filter {
    boolean test(DataType type, DataRecord record);

    static Filter alwaysTrue() { return (t, r) -> true; }
}
