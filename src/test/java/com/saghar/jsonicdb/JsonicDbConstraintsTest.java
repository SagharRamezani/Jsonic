package com.saghar.jsonicdb;

import com.saghar.jsonicdb.core.Database;
import com.saghar.jsonicdb.parser.CommandParser;
import com.saghar.jsonicdb.util.JsonicException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonicDbConstraintsTest {

    @Test
    void uniqueConstraint_blocksDuplicates_onInsert_andUpdate() {
        Database db = new Database();
        CommandParser p = new CommandParser();

        p.parse("create User {\"id\":{\"type\":\"int\",\"required\":true,\"unique\":true}," +
                        "\"email\":{\"type\":\"string\",\"unique\":true}," +
                        "\"name\":{\"type\":\"string\",\"required\":true}}")
                .execute(db);

        p.parse("insert User {\"id\":1,\"name\":\"A\",\"email\":\"a@x.com\"}").execute(db);
        p.parse("insert User {\"id\":2,\"name\":\"B\",\"email\":\"b@x.com\"}").execute(db);

        JsonicException ex1 = assertThrows(JsonicException.class,
                () -> p.parse("insert User {\"id\":1,\"name\":\"C\",\"email\":\"c@x.com\"}").execute(db));
        assertTrue(ex1.getMessage().toLowerCase().contains("duplicate"));

        JsonicException ex2 = assertThrows(JsonicException.class,
                () -> p.parse("update User (id = 2) {\"email\":\"a@x.com\"}").execute(db));
        assertTrue(ex2.getMessage().toLowerCase().contains("duplicate"));
    }

    @Test
    void requiredConstraint_isEnforced() {
        Database db = new Database();
        CommandParser p = new CommandParser();

        p.parse("create Person {\"id\":{\"type\":\"int\",\"required\":true}," +
                        "\"name\":{\"type\":\"string\",\"required\":true}}")
                .execute(db);

        JsonicException ex = assertThrows(JsonicException.class,
                () -> p.parse("insert Person {\"id\":1}").execute(db));
        assertTrue(ex.getMessage().toLowerCase().contains("missing required"));
    }
}
