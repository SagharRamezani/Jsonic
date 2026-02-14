package com.saghar.jsonicdb;

import com.saghar.jsonicdb.core.Database;
import com.saghar.jsonicdb.parser.CommandParser;
import com.saghar.jsonicdb.util.JsonicException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonicDbErrorCasesTest {

    @Test
    void invalidJson_isRejected() {
        Database db = new Database();
        CommandParser p = new CommandParser();

        p.parse("create X {\"id\":{\"type\":\"int\",\"required\":true}}")
                .execute(db);

        JsonicException ex = assertThrows(JsonicException.class,
                // unclosed string => invalid JSON
                () -> p.parse("insert X {\"id\":\"abc}").execute(db));
        assertTrue(ex.getMessage().toLowerCase().contains("json"));
    }

    @Test
    void invalidFilter_isRejected() {
        Database db = new Database();
        CommandParser p = new CommandParser();

        p.parse("create X {\"id\":{\"type\":\"int\",\"required\":true}}")
                .execute(db);
        p.parse("insert X {\"id\":1}").execute(db);

        JsonicException ex = assertThrows(JsonicException.class,
                () -> p.parse("search X (id === 1)").execute(db));
        assertTrue(ex.getMessage().toLowerCase().contains("filter"));
    }
}
