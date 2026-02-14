import java.util.ArrayList;
import java.util.HashMap;

public class DataType {
    private String name;
    private ArrayList<Field> fields;
    private HashMap<String, Field> fieldMap;
    private ArrayList<DataInstance> instances = new ArrayList<>();

    public DataType(String name) {
        this.name = name;
        this.fields = new ArrayList<>();
        this.fieldMap = new HashMap<>();
        this.instances = new ArrayList<>();
    }

    public static String ANSI_YELLOW = "\u001B[33m";
    public static String ANSI_RESET = "\u001B[0m";

    public String getName() { return name; }
    public void addField(Field field) {
        String lowerName = field.getName().toLowerCase();
        if (fieldMap.containsKey(lowerName)) {
            throw new IllegalArgumentException(ANSI_YELLOW + " Field '" + field.getName() + "' already exists." + ANSI_RESET);
        }
        fieldMap.put(lowerName, field);
        fields.add(field);
    }
    public Field getField(String name) {
        return fieldMap.get(name.toLowerCase());
    }
    public ArrayList<Field> getFields() { return fields; }
    public void removeField(Field field) { fields.remove(field); }
    public void addInstance(DataInstance instance) { instances.add(instance); }
    public ArrayList<DataInstance> getInstances() { return instances; }
    public String toString() { return name + " : {" + fields + "}"; }
}