package com.saghar.jsonicdb.parser.commands;

import com.saghar.jsonicdb.core.DataType;
import com.saghar.jsonicdb.core.Database;
import com.saghar.jsonicdb.core.DataRecord;
import com.saghar.jsonicdb.filter.Filter;
import com.saghar.jsonicdb.filter.FilterParser;
import com.saghar.jsonicdb.parser.Command;
import com.saghar.jsonicdb.util.JsonicException;

import java.util.ArrayList;
import java.util.List;

public final class SearchCommand implements Command {
    private final String typeName;
    private final String filterExpr; // may be null

    public SearchCommand(String typeName, String filterExpr) {
        this.typeName = typeName;
        this.filterExpr = (filterExpr == null || filterExpr.isBlank()) ? null : filterExpr;
    }

    @Override
    public String execute(Database db) {
        DataType dt = db.getType(typeName);
        if (dt == null) throw new JsonicException("Data type not found: " + typeName);

        Filter filter = (filterExpr == null) ? Filter.alwaysTrue() : new FilterParser().parse(filterExpr);
        List<DataRecord> results = new ArrayList<>();
        for (DataRecord r : dt.records()) {
            if (filter.test(dt, r)) results.add(r);
        }

        if (results.isEmpty()) return "No results found.";
        return "Search results (" + results.size() + "):\n" + dt.formatTable(results);
    }
}
