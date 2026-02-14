import java.time.LocalDateTime;
import java.util.HashMap;

public class DataInstance {
    private HashMap<String, Object> values;

    public DataInstance() {
        this.values = new HashMap<>();
    }

    public void setValues(String fieldName, Object value) {
        values.put(fieldName, value);
    }
    public Object getValues(String fieldName) {
        return values.get(fieldName);
    }

    private Object getDefaultValue(Field field) {
        switch (field.getType()){
            case "string": return "";
            case "int": return 0;
            case "bool": return false;
            case "dbl": return 0.0;
            case "time": return LocalDateTime.now();
            default: return null;
        }
    }

    public static String ANSI_RESET = "\u001B[0m";
    public static String ANSI_MAGENTA = "\u001B[37m";
    public static String ANSI_YELLOW = "\u001B[33m";

    public String toTable(DataType dataType) {
        int columnWidth = 15;
        StringBuilder table = new StringBuilder();
        table.append("\n| ");
        for (Field field : dataType.getFields()) {
            table.append(ANSI_YELLOW);
            String header = String.format("%-" + columnWidth + "s", field.getName());
            table.append(header).append(ANSI_RESET).append(" | ");
        }
        table.append("\n|");
        for (Field field : dataType.getFields()) {
            table.append("-----------------|");
        }
        table.append("\n| ");
        for (Field field : dataType.getFields()) {
            table.append(ANSI_MAGENTA);
            Object value = values.getOrDefault(field.getName(), getDefaultValue(field));
            String formattedValue = String.format("%-" + columnWidth + "s", value);
            table.append(formattedValue).append(ANSI_RESET).append(" | ");
        }
        table.append("\n");
        return table.toString();
    }
}
