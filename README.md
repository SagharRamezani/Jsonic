# JsonicDB (In-memory Mini Database)

A clean, modular re-implementation of the original **Jsonic** project: an in-memory database with a small DSL supporting:

- `create <Type> {...}`
- `insert <Type> {...}`
- `update <Type> (<filter>) {...}`
- `search <Type> (<filter>)`
- `delete <Type> (<filter>)`
- `exit`

## Supported field types

| Spec | Meaning |
|---|---|
| `string` | Java `String` |
| `int` | Java `int` |
| `dbl` / `double` | Java `double` |
| `bool` / `boolean` | Java `boolean` |
| `time` | `LocalDateTime` (ISO-8601) |
| `arr_string` / `list_string` | `List<String>` |

## Filters

- Comparisons: `=  !=  <  <=  >  >=`
- Boolean operators: `AND`, `OR` with parentheses
- **Bonus:** `include` for string arrays/lists

Examples:
```
search Person (age > 30 AND isStudent = false)
search Person (skills include "Java")
update Person (id = 1) {"age": 25}
delete Person (age <= 18 OR skills include "Scratch")
```

## Build & Run

Requires Java 17+ and Maven.

```bash
mvn test
mvn package
java -jar target/jsonicdb-1.0.0.jar
```

## Notes

- JSON is parsed by a small custom parser (no external JSON libraries).
- Default values are *materialized* on insert (e.g. missing `int` => `0`).
- Unique fields are enforced with an internal hash index for fast checks.
