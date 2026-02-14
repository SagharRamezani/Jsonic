package com.saghar.jsonicdb.parser.commands;

import com.saghar.jsonicdb.core.DataType;
import com.saghar.jsonicdb.core.Database;
import com.saghar.jsonicdb.filter.Filter;
import com.saghar.jsonicdb.filter.FilterParser;
import com.saghar.jsonicdb.parser.Command;
import com.saghar.jsonicdb.util.JsonicException;

public final class DeleteCommand implements Command {
    private final String typeName;
    private final String filterExpr; // may be null

    public DeleteCommand(String typeName, String filterExpr) {
        this.typeName = typeName;
        this.filterExpr = (filterExpr == null || filterExpr.isBlank()) ? null : filterExpr;
    }

    @Override
    public String execute(Database db) {
        DataType dt = db.getType(typeName);
        if (dt == null) throw new JsonicException(com.saghar.jsonicdb.util.Errors.typeNotFound(typeName));

        Filter filter = (filterExpr == null) ? Filter.alwaysTrue() : new FilterParser().parse(filterExpr);
        int deleted = dt.deleteWhere(r -> filter.test(dt, r));
        return deleted + " instances deleted.";
    }
}
