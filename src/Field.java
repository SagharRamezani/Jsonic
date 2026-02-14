public class Field {
    private String name;
    private String type;
    private boolean required;
    private boolean unique;

    public Field(String name, String type, boolean required, boolean unique) {
        this.name = name;
        this.type = type;
        this.required = required;
        this.unique = unique;
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public boolean isRequired() { return required; }
    public boolean isUnique() { return unique; }

    public String toString() { return name + " : type = " + type + " , required = " + required + " , unique = " + unique; }
}
