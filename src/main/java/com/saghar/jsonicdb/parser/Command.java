package com.saghar.jsonicdb.parser;

import com.saghar.jsonicdb.core.Database;

public interface Command {
    /**
     * Executes the command and returns a user-facing message.
     */
    String execute(Database db);
}
