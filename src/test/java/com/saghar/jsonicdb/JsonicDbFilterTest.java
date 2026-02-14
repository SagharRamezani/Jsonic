package com.saghar.jsonicdb;

import com.saghar.jsonicdb.core.Database;
import com.saghar.jsonicdb.parser.CommandParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JsonicDbFilterTest {

    @Test
    void andHasHigherPrecedenceThanOr() {
        Database db = new Database();
        CommandParser p = new CommandParser();

        p.parse("create T {\"id\":{\"type\":\"int\",\"required\":true,\"unique\":true}," +
                "\"a\":{\"type\":\"bool\"}," +
                "\"b\":{\"type\":\"bool\"}," +
                "\"c\":{\"type\":\"bool\"}}")
                .execute(db);

        // id=1: a=true, b=false, c=false
        p.parse("insert T {\"id\":1,\"a\":true,\"b\":false,\"c\":false}").execute(db);
        // id=2: a=false, b=true, c=true
        p.parse("insert T {\"id\":2,\"a\":false,\"b\":true,\"c\":true}").execute(db);
        // id=3: a=false, b=true, c=false
        p.parse("insert T {\"id\":3,\"a\":false,\"b\":true,\"c\":false}").execute(db);

        // Expression: a = true OR b = true AND c = true
        // With precedence: a=true OR (b=true AND c=true) => ids {1,2}
        String res = p.parse("search T (a = true OR b = true AND c = true)").execute(db);
        assertTrue(res.contains("Search results (2)"));

        // Parentheses change meaning: (a=true OR b=true) AND c=true => only id=2
        String res2 = p.parse("search T ((a = true OR b = true) AND c = true)").execute(db);
        assertTrue(res2.contains("Search results (1)"));
    }

    @Test
    void includeOperator_worksOnStringArrays() {
        Database db = new Database();
        CommandParser p = new CommandParser();

        p.parse("create P {\"id\":{\"type\":\"int\",\"required\":true,\"unique\":true}," +
                "\"skills\":{\"type\":\"list_string\"}}")
                .execute(db);

        p.parse("insert P {\"id\":1,\"skills\":[\"Java\",\"C++\"]}").execute(db);
        p.parse("insert P {\"id\":2,\"skills\":[\"Python\"]}").execute(db);

        String res = p.parse("search P (skills include \"Java\")").execute(db);
        assertTrue(res.contains("Search results (1)"));
        assertTrue(res.contains("id"));
    }
}
