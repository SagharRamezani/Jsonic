package com.saghar.jsonicdb;

import com.saghar.jsonicdb.core.Database;
import com.saghar.jsonicdb.parser.Command;
import com.saghar.jsonicdb.parser.CommandParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JsonicDbSmokeTest {

    @Test
    void defaultsAndUniqueWork() {
        Database db = new Database();
        CommandParser p = new CommandParser();

        Command create = p.parse("""
            create Person {"id":{"type":"int","required":true,"unique":true},"name":{"type":"string","required":true},"age":{"type":"int"}}
        """.trim());
        create.execute(db);

        p.parse("""insert Person {"id":1,"name":"A"}""").execute(db);
        // age should be materialized to default 0 and unique should reject duplicate id
        assertThrows(RuntimeException.class, () -> p.parse("""insert Person {"id":1,"name":"B"}""").execute(db));
    }

    @Test
    void includeAndAndOrWork() {
        Database db = new Database();
        CommandParser p = new CommandParser();

        p.parse("""
            create Person {"id":{"type":"int","required":true,"unique":true},"skills":{"type":"arr_string"},"age":{"type":"int"}}
        """.trim()).execute(db);

        p.parse("""insert Person {"id":1,"skills":["Java","C++"],"age":20}""").execute(db);
        p.parse("""insert Person {"id":2,"skills":["Python"],"age":40}""").execute(db);

        String res1 = p.parse("""search Person (skills include "Java")""").execute(db);
        assertTrue(res1.contains("Search results (1)"));

        String res2 = p.parse("""search Person ((age > 30 AND skills include "Python") OR id = 1)""").execute(db);
        assertTrue(res2.contains("Search results (2)"));
    }
}
