package com.saghar.jsonicdb;

import com.saghar.jsonicdb.core.DataRecord;
import com.saghar.jsonicdb.core.DataType;
import com.saghar.jsonicdb.core.Database;
import com.saghar.jsonicdb.parser.CommandParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end CRUD tests through the DSL parser + executor.
 */
public class JsonicDbCrudTest {

    @Test
    void createInsertSearchUpdateDelete_workTogether() {
        Database db = new Database();
        CommandParser p = new CommandParser();

        // create
        p.parse("create Person {\"id\":{\"type\":\"int\",\"required\":true,\"unique\":true}," +
                        "\"name\":{\"type\":\"string\",\"required\":true}," +
                        "\"age\":{\"type\":\"int\"}," +
                        "\"skills\":{\"type\":\"arr_string\"}}")
                .execute(db);

        // insert (defaults materialize)
        p.parse("insert Person {\"id\":1,\"name\":\"A\"}").execute(db);
        p.parse("insert Person {\"id\":2,\"name\":\"B\",\"age\":40,\"skills\":[\"Java\",\"C++\"]}").execute(db);

        DataType person = db.getType("person");
        assertNotNull(person);
        assertEquals(2, person.records().size());

        DataRecord r1 = person.records().get(0);
        assertEquals(0, r1.get("age"));
        assertEquals(List.of(), r1.get("skills"));

        // search with AND/OR + include
        String res = p.parse("search Person ((age > 30 AND skills include \"Java\") OR id = 1)")
                .execute(db);
        assertTrue(res.contains("Search results (2)"));

        // update one row
        String upd = p.parse("update Person (id = 1) {\"age\":25,\"skills\":[\"Python\"]}")
                .execute(db);
        assertTrue(upd.contains("1 instances updated."));
        assertEquals(25, r1.get("age"));
        assertEquals(List.of("Python"), r1.get("skills"));

        // delete with filter
        String del = p.parse("delete Person (age >= 40)").execute(db);
        assertTrue(del.contains("1 instances deleted."));
        assertEquals(1, person.records().size());

        // search after delete
        String res2 = p.parse("search Person (id = 2)").execute(db);
        assertEquals("No results found.", res2);
    }
}
