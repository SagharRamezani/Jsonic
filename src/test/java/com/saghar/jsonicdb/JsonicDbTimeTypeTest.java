package com.saghar.jsonicdb;

import com.saghar.jsonicdb.core.Database;
import com.saghar.jsonicdb.parser.CommandParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JsonicDbTimeTypeTest {

    @Test
    void timeComparisons_workInFilters() {
        Database db = new Database();
        CommandParser p = new CommandParser();

        p.parse("create Event {\"id\":{\"type\":\"int\",\"required\":true,\"unique\":true}," +
                "\"at\":{\"type\":\"time\",\"required\":true}}")
                .execute(db);

        p.parse("insert Event {\"id\":1,\"at\":\"2025-01-01T10:00:00\"}").execute(db);
        p.parse("insert Event {\"id\":2,\"at\":\"2025-01-01T12:00:00\"}").execute(db);
        p.parse("insert Event {\"id\":3,\"at\":\"2025-01-02T09:00:00\"}").execute(db);

        String res1 = p.parse("search Event (at > \"2025-01-01T11:00:00\")").execute(db);
        assertTrue(res1.contains("Search results (2)"));

        String res2 = p.parse("search Event (at <= \"2025-01-01T12:00:00\")").execute(db);
        assertTrue(res2.contains("Search results (2)"));
    }
}
