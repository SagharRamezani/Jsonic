package com.saghar.jsonicdb.core;

import com.saghar.jsonicdb.util.Checks;
import com.saghar.jsonicdb.util.JsonicException;

import java.util.*;

public final class DataType {
    private final String name;
    private final Map<String, FieldDef> fields = new LinkedHashMap<>();
    private final List<DataRecord> records = new ArrayList<>();
    private final Map<String, Map<Object, DataRecord>> uniqueIndex = new HashMap<>();

    public DataType(String name) {
        Checks.require(name != null && !name.isBlank(), "Type name is empty");
        this.name = name;
    }

    public String name() { return name; }

    public Collection<FieldDef> fields() { return Collections.unmodifiableCollection(fields.values()); }

    public FieldDef field(String name) {
        return fields.get(canon(name));
    }

    public List<DataRecord> records() { return records; }

    public void addField(FieldDef def) {
        String key = canon(def.name());
        if (fields.containsKey(key)) throw new JsonicException("Field already exists: " + def.name());
        fields.put(key, def);
        if (def.unique()) uniqueIndex.put(key, new HashMap<>());
    }

    public DataRecord insert(Map<String, Object> provided) {
        DataRecord r = new DataRecord();

        // materialize defaults
        for (FieldDef f : fields.values()) {
            Object v = provided.get(canon(f.name()));
            if (v == null) v = f.type().defaultValue();
            r.put(canon(f.name()), v);
        }

        // required check (after defaults, only missing if field exists but provided null explicitly)
        for (FieldDef f : fields.values()) {
            if (f.required()) {
                Object v = r.get(canon(f.name()));
                if (v == null) throw new JsonicException("Missing required field: " + f.name());
            }
        }

        // unique check
        for (FieldDef f : fields.values()) {
            if (!f.unique()) continue;
            String k = canon(f.name());
            Object v = r.get(k);
            Map<Object, DataRecord> idx = uniqueIndex.get(k);
            if (idx.containsKey(v)) throw new JsonicException("Duplicate value for unique field: " + f.name());
        }

        // commit: add to records + index
        records.add(r);
        for (FieldDef f : fields.values()) {
            if (!f.unique()) continue;
            String k = canon(f.name());
            uniqueIndex.get(k).put(r.get(k), r);
        }
        return r;
    }

    public int deleteWhere(java.util.function.Predicate<DataRecord> predicate) {
        int count = 0;
        Iterator<DataRecord> it = records.iterator();
        while (it.hasNext()) {
            DataRecord r = it.next();
            if (!predicate.test(r)) continue;
            removeFromIndexes(r);
            it.remove();
            count++;
        }
        return count;
    }

    public int updateWhere(java.util.function.Predicate<DataRecord> predicate, Map<String, Object> updates) {
        // pre-validate: unknown fields
        for (String k : updates.keySet()) {
            if (!fields.containsKey(canon(k))) throw new JsonicException("Unknown field: " + k);
        }

        // if updates touch a unique field, pre-check collisions
        for (DataRecord r : records) {
            if (!predicate.test(r)) continue;

            for (Map.Entry<String, Object> e : updates.entrySet()) {
                String field = canon(e.getKey());
                FieldDef f = fields.get(field);
                if (f == null || !f.unique()) continue;
                Object newVal = e.getValue();
                Object oldVal = r.get(field);
                if (Objects.equals(newVal, oldVal)) continue;

                Map<Object, DataRecord> idx = uniqueIndex.get(field);
                DataRecord other = idx.get(newVal);
                if (other != null && other != r) throw new JsonicException("Duplicate value for unique field: " + f.name());
            }
        }

        int count = 0;
        for (DataRecord r : records) {
            if (!predicate.test(r)) continue;

            // update indexes safely
            removeFromIndexes(r);
            for (Map.Entry<String, Object> e : updates.entrySet()) {
                r.put(canon(e.getKey()), e.getValue());
            }
            // required check after updates
            for (FieldDef f : fields.values()) {
                if (f.required() && r.get(canon(f.name())) == null) {
                    // rollback to indexes only (values already changed); simplest: throw and leave state inconsistent is bad.
                    // Instead: re-add indexes with current values before throwing.
                    addToIndexes(r);
                    throw new JsonicException("Missing required field after update: " + f.name());
                }
            }
            addToIndexes(r);
            count++;
        }
        return count;
    }

    public String formatTable(List<DataRecord> rs) {
        int width = Math.max(10, fields.values().stream().mapToInt(f -> f.name().length()).max().orElse(10));
        StringBuilder sb = new StringBuilder();
        // header
        sb.append("| ");
        for (FieldDef f : fields.values()) {
            sb.append(pad(f.name(), width)).append(" | ");
        }
        sb.append("\n|");
        for (int i = 0; i < fields.size(); i++) sb.append("-".repeat(width + 2)).append("|");
        sb.append("\n");

        for (DataRecord r : rs) {
            sb.append("| ");
            for (FieldDef f : fields.values()) {
                Object v = r.get(canon(f.name()));
                sb.append(pad(String.valueOf(v), width)).append(" | ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private void removeFromIndexes(DataRecord r) {
        for (Map.Entry<String, Map<Object, DataRecord>> e : uniqueIndex.entrySet()) {
            Object v = r.get(e.getKey());
            // remove only if points to this record
            if (e.getValue().get(v) == r) e.getValue().remove(v);
        }
    }

    private void addToIndexes(DataRecord r) {
        for (Map.Entry<String, Map<Object, DataRecord>> e : uniqueIndex.entrySet()) {
            Object v = r.get(e.getKey());
            e.getValue().put(v, r);
        }
    }

    private static String canon(String s) { return s.trim().toLowerCase(); }

    private static String pad(String s, int w) {
        if (s == null) s = "null";
        if (s.length() >= w) return s.substring(0, w);
        return s + " ".repeat(w - s.length());
    }
}
