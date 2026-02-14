# 🗃️ JsonicDB — In-Memory Mini Database + Custom DSL (Java)

![Platform](https://img.shields.io/badge/platform-Cross--platform-blue)
![Language](https://img.shields.io/badge/language-Java-informational)
![Build](https://img.shields.io/badge/build-Maven-success)
[![CI](https://github.com/SagharRamezani/Jsonic/actions/workflows/ci.yml/badge.svg)](https://github.com/SagharRamezani/Jsonic/actions/workflows/ci.yml)
![License](https://img.shields.io/badge/license-MIT-green)

**JsonicDB** is a lightweight **in-memory database** with a small command language (DSL) inspired by database CRUD operations.  
It’s designed as an educational project showing **data structures + algorithmic design** in a realistic scenario:  
**parsing**, **type systems**, **constraints**, **indexes**, and **query evaluation** — all implemented from scratch in Java.

> ⚠️ Educational project. Not intended as a production database.

---

## ✨ Highlights

- **In-memory data model**: dynamic types + instances stored in memory
- **Custom DSL**: `create / insert / update / search / delete`
- **Custom JSON parsing**: no external JSON libraries
- **Typed fields**: `string`, `int`, `double`, `bool`, `time`, and `arr_string` (bonus)
- **Constraints**:
    - `required` → must be explicitly provided in input
    - `unique` → enforced via **O(1)** average-time index
- **Query filters with AST**:
    - comparisons: `= != < <= > >=`
    - boolean logic: `AND / OR`
    - parentheses supported
    - **bonus operator**: `include` for array fields (`skills include "Java"`)
- **Clean architecture**: parsing separated from execution (modular & testable)
- **CI ready**: GitHub Actions runs tests on push/PR

---

## 📦 Repository layout

```txt
JsonicDB/
  src/
    main/java/com/saghar/jsonicdb/
      cli/               # entry point + command loop
      core/              # Database, DataType, instances, constraints, indexes
      parser/            # DSL parser -> Command objects
      filter/            # Filter tokenizer/parser -> Expression AST
      json/              # Minimal JSON reader (objects, arrays, primitives)
      util/              # unified errors/exceptions and helpers
    test/java/com/saghar/jsonicdb/
      ...                # comprehensive JUnit tests (CRUD/constraints/errors)
  .github/workflows/ci.yml
  pom.xml
  README.md
```

---

## 🧠 Design

### 1) Type system + default materialization

Each `DataType` defines fields with:
- `type` (e.g., `int`, `time`, `arr_string`)
- `required` (must appear in input)
- `unique` (tracked via index)

**Default values** are **materialized at insert** for non-required missing fields:
- `int → 0`, `double → 0.0`, `bool → false`, `string → ""`, `time → 00:00`, `arr_string → []`

This prevents `null` pitfalls and makes filtering/printing consistent.

---

### 2) Unique index (HashMap) for O(1) constraint checks

For each unique field, a per-type index is maintained:

- `uniqueIndex[fieldName][value] = instanceId`

**Insert/Update**:
- check duplicates in O(1) average time
- update index when a unique field changes

This replaces the naive O(n) scan approach.

---

### 3) Filter parsing as AST (AND/OR + parentheses)

Filters are parsed into an expression tree:

- leaf: comparison (e.g., `age > 30`, `id = 1`)
- internal nodes: `AND`, `OR`

This makes the query system extensible (easy to add NOT, more operators, etc.).

**Bonus**: `include` operator supports array membership checks:
- `skills include "Java"`

---

## 🚀 Usage

Run the program and type commands line-by-line.

### Create a type

```txt
create Person {"id":{"type":"int","required":true,"unique":true},"name":{"type":"string","required":true},"age":{"type":"int"}}
```

### Insert

```txt
insert Person {"id":1,"name":"Saghar"}
insert Person {"id":2,"name":"Ali","age":40}
```

### Search (filters)

```txt
search Person (age > 30 AND id != 1)
search Person (id = 1 OR age >= 40)
```

### Bonus: include (array field)

```txt
create Dev {"id":{"type":"int","required":true,"unique":true},"skills":{"type":"arr_string"}}
insert Dev {"id":1,"skills":["Java","C++"]}
search Dev (skills include "Java")
```

### Update

```txt
update Person (id = 1) {"age":25}
```

### Delete

```txt
delete Person (age >= 40)
```

---

## 🏗 Build & Run

### Requirements
- Java 17+ (tested on modern JDKs)
- Maven 3+

### Build

```bash
mvn -q test
mvn -q package
```

### Run

```bash
java -cp target/classes com.saghar.jsonicdb.cli.Main
```

---

## 🧪 Tests

JUnit tests cover:
- CRUD flow end-to-end
- `required` and `unique` constraint enforcement
- default materialization behavior
- filter parsing (AND/OR/parentheses)
- `include` operator
- error cases (invalid commands / invalid JSON / invalid filters)

Run:

```bash
mvn test
```

---

## 🔎 Example session (quick demo)

```txt
create Person {"id":{"type":"int","required":true,"unique":true},"name":{"type":"string","required":true},"age":{"type":"int"}}
insert Person {"id":1,"name":"A"}
insert Person {"id":2,"name":"B","age":40}
search Person (age >= 30 OR id = 1)
update Person (id = 1) {"age":25}
delete Person (age >= 40)
```

---

## 🗺 Roadmap

- [ ] Add `NOT` operator to filters
- [ ] Add range index (TreeMap) for faster `> <` queries on numeric/time fields
- [ ] Improve CLI UX (history, multiline commands, better help)
- [ ] Export/import snapshots (serialize DB state)
- [ ] More array types (e.g., `arr_int`) and richer operators

---

## 📄 License

MIT License.

---

## 👩‍💻 Author

**Saghar Ramezani** — Computer Engineering  
(DS & Algo / Advanced Programming project)
