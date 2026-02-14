import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Jsonic{
    public static void main(String[] args){
        Database db = new Database();
        Scanner sc = new Scanner(System.in);

        while(true){
            System.out.print("> ");
            String command = sc.nextLine();
            if(command.equals("exit")){ break; }
            try { processCommand(command, db); }
            catch (Exception e){
                System.out.println("Error" + e.getMessage());
            }
        }

        sc.close();
    }

    private static void processCommand(String command, Database db) throws Exception{
        String[] tokens = command.split("\\s+");
        String action = tokens[0].toLowerCase();
        switch(action){
            case "create":
                handleCreate(tokens, db);
                break;
            case "delete":
                handleDelete(tokens, db);
                break;
            case "update":
                handleUpdate(tokens, db);
                break;
            case "insert":
                handleInsert(tokens, db);
                break;
            case "search":
                handleSearch(tokens, db);
                break;
            default:
                throw new Exception(ANSI_YELLOW + " invalid command" + ANSI_RESET);
        }
    }

    public static String ANSI_RESET = "\u001B[0m";
    public static String ANSI_RED = "\u001B[31m";
    public static String ANSI_GREEN = "\u001B[32m";
    public static String ANSI_CYAN = "\u001B[36m";
    public static String ANSI_YELLOW = "\u001B[33m";

    private static void handleSearch(String[] tokens, Database db) throws Exception {
        if (tokens.length < 2) throw new Exception(ANSI_YELLOW + " Invalid search syntax" + ANSI_RESET);
        String typeName = tokens[1];
        DataType dataType = db.getDataType(typeName);
        if (dataType == null) throw new Exception(ANSI_YELLOW + " Data type not found" + ANSI_RESET);
        String filter = null;
        if (tokens.length > 2) {
            StringBuilder builder = new StringBuilder();
            for (int i = 2; i < tokens.length; i++) {
                builder.append(tokens[i]).append(" ");
            }
            filter = builder.toString().trim().replaceAll("[()]", "");
        }
        List<DataInstance> results = new ArrayList<>();
        for (DataInstance instance : dataType.getInstances()) {
            if (filter == null || evaluateFilter(filter, instance, dataType)) {
                results.add(instance);
            }
        }
        System.out.println("Search results:\n");
        int count = 1;
        for (DataInstance result : results) {
            String coloredText = ANSI_CYAN + "&&& " + count + " &&&" + ANSI_RESET;
            System.out.println(coloredText);
            System.out.println(result.toTable(dataType));
            count++;
        }
        if (count != 1) {
            String coloredText = ANSI_GREEN + "End Of Search..." + ANSI_RESET;
            System.out.println(coloredText);
        }
        else {
            String coloredText = ANSI_RED + "No results found" + ANSI_RESET;
            System.out.println(coloredText);
        }
    }
    private static void handleInsert(String[] tokens, Database db) throws Exception {
        if (tokens.length < 3) throw new Exception(ANSI_YELLOW + " Invalid insert syntax" + ANSI_RESET);
        String typeName = tokens[1];
        String rawInput = commandForm(tokens, 2).trim();
        if (!rawInput.startsWith("{") || !rawInput.endsWith("}") || rawInput.contains("}}") || rawInput.contains("{{")) {
            throw new Exception(ANSI_YELLOW + " Invalid JSON format" + ANSI_RESET);
        }
        String jsonInput = rawInput.substring(1, rawInput.length() - 1).trim();
        DataType dataType = db.getDataType(typeName);
        if (dataType == null) throw new Exception(ANSI_YELLOW + " Data type not found" + ANSI_RESET);
        DataInstance instance = new DataInstance();
        String[] fieldDefs = jsonInput.split(",(?=\\s*(\"\\w+\"|\\w+)\\s*:)");
        for (String fieldDef : fieldDefs) {
            String[] parts = fieldDef.split(":", 2);
            if (parts.length != 2) throw new Exception(ANSI_YELLOW + " Invalid field definition" + ANSI_RESET);
            String fieldName = parts[0].trim().replaceAll("\"", "");
            Field field = dataType.getField(fieldName);
            if (field == null) throw new Exception(ANSI_YELLOW + " Field '" + fieldName + "' not found" + ANSI_RESET);
            Object value = parseValue(parts[1].trim(), field.getType());
            instance.setValues(fieldName, value);
        }
        for (Field field : dataType.getFields()) {
            if (field.isRequired() && instance.getValues(field.getName()) == null) {
                throw new Exception(ANSI_YELLOW + " Missing required field: " + field.getName() + ANSI_RESET);
            }
        }
        for (Field field : dataType.getFields()) {
            if (field.isUnique()) {
                Object currentValue = instance.getValues(field.getName());
                for (DataInstance existing : dataType.getInstances()) {
                    if (existing.getValues(field.getName()).equals(currentValue)) {
                        throw new Exception(ANSI_YELLOW + " Duplicate value for unique field: " + field.getName() + ANSI_RESET);
                    }
                }
            }
        }
        dataType.addInstance(instance);
        System.out.println(ANSI_GREEN + "Instance added successfully." + ANSI_RESET);
    }
    private static void handleUpdate(String[] tokens, Database db) throws Exception {
        if (tokens.length < 3) throw new Exception(ANSI_YELLOW + " Invalid update syntax" + ANSI_RESET);
        String typeName = tokens[1];
        DataType dataType = db.getDataType(typeName);
        if (dataType == null) throw new Exception(ANSI_YELLOW + " Data type not found" + ANSI_RESET);
        String filterPart = null;
        String rawUpdateJson;
        if (tokens[2].startsWith("(")) {
            int filterEnd = -1;
            for (int i = 2; i < tokens.length; i++) {
                if (tokens[i].endsWith(")")) {
                    filterEnd = i;
                    break;
                }
            }
            if (filterEnd == -1) throw new Exception(ANSI_YELLOW + " Invalid filter syntax" + ANSI_RESET);
            StringBuilder filterBuilder = new StringBuilder();
            for (int i = 2; i <= filterEnd; i++) {
                filterBuilder.append(tokens[i]).append(" ");
            }
            filterPart = filterBuilder.toString().trim();
            StringBuilder jsonBuilder = new StringBuilder();
            for (int i = filterEnd + 1; i < tokens.length; i++) {
                jsonBuilder.append(tokens[i]).append(" ");
            }
            rawUpdateJson = jsonBuilder.toString().trim();
        } else {
            rawUpdateJson = commandForm(tokens, 2);
        }
        if (!rawUpdateJson.startsWith("{") || !rawUpdateJson.endsWith("}")) {
            throw new Exception(ANSI_YELLOW + " Invalid JSON format" + ANSI_RESET);
        }
        String jsonUpdate = rawUpdateJson.substring(1, rawUpdateJson.length() - 1).trim();
        String[] fieldDefs = jsonUpdate.split(",(?=\\s*\"\\w+\"\\s*:)");
        Map<String, Object> updates = new HashMap<>();
        for (String fieldDef : fieldDefs) {
            String[] parts = fieldDef.split(":", 2);
            if (parts.length != 2) continue;
            String fieldName = parts[0].trim().replaceAll("\"", "");
            Field field = dataType.getField(fieldName);
            if (field == null) continue;
            Object value = parseValue(parts[1].trim(), field.getType());
            updates.put(fieldName, value);
        }
        List<DataInstance> targets = new ArrayList<>(dataType.getInstances());
        if (filterPart == null ) {
            for (String fieldDef : fieldDefs) {
                String[] parts = fieldDef.split(":", 2);
                if (parts.length != 2) continue;
                String fieldName = parts[0].trim().replaceAll("\"", "");
                Field field = dataType.getField(fieldName);
                if (field.isUnique() && targets.size() > 1){
                    throw new Exception(ANSI_YELLOW + " Duplicate value for unique field: " + fieldName + ANSI_RESET);
                }
            }
        } else {
            String cleanFilter = filterPart.replaceAll("[()]", "").trim();
            for (DataInstance instance : dataType.getInstances()) {
                if (!evaluateFilter(cleanFilter, instance, dataType)) {
                    targets.remove(instance);
                }
            }
        }
        for (DataInstance instance : targets) {
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String fieldName = entry.getKey();
                Object newValue = entry.getValue();
                Field field = dataType.getField(fieldName);
                if (field != null && field.isUnique()) {
                    for (DataInstance other : dataType.getInstances()) {
                        if (other == instance) continue;
                        Object otherValue = other.getValues(fieldName);
                        if (otherValue != null && otherValue.equals(newValue)) {
                            throw new Exception(ANSI_YELLOW + " Duplicate value for unique field: " + fieldName + ANSI_RESET);
                        }
                    }
                }
            }
        }
        for (DataInstance instance : targets) {
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                instance.setValues(entry.getKey(), entry.getValue());
            }
        }
        System.out.println(ANSI_GREEN + targets.size() + " instances updated" + ANSI_RESET);
    }
    private static void handleDelete(String[] tokens, Database db) throws Exception {
        if (tokens.length < 2) throw new Exception(ANSI_YELLOW + " Invalid delete syntax" + ANSI_RESET);
        String typeName = tokens[1];
        DataType dataType = db.getDataType(typeName);
        if (dataType == null) throw new Exception(ANSI_YELLOW + " Data type not found" + ANSI_RESET);
        String filter = null;
        if (tokens.length > 2) {
            StringBuilder builder = new StringBuilder();
            for (int i = 2; i < tokens.length; i++) {
                builder.append(tokens[i]).append(" ");
            }
            filter = builder.toString().trim().replaceAll("[()]", "");
        }
        List<DataInstance> toDelete = new ArrayList<>();
        for (DataInstance instance : dataType.getInstances()) {
            if (filter == null || evaluateFilter(filter, instance, dataType)) {
                toDelete.add(instance);
            }
        }
        dataType.getInstances().removeAll(toDelete);
        System.out.println(ANSI_GREEN + toDelete.size() + " instances deleted" + ANSI_RESET);
    }
    private static void handleCreate(String[] tokens, Database db) throws Exception {
        if (tokens.length < 3) throw new Exception(ANSI_YELLOW + " Invalid create syntax" + ANSI_RESET);
        String typeName = tokens[1];
        System.out.println(typeName);
        if (Pattern.compile("\\W").matcher(typeName).find()) {
            throw new Exception(ANSI_YELLOW + " Invalid Type Name: " + typeName + ANSI_RESET);
        }
        String rawInput = commandForm(tokens, 2).trim();
        if (!rawInput.startsWith("{") || !rawInput.endsWith("}") || rawInput.contains("}}}") || rawInput.contains("{{")) {
            throw new Exception(ANSI_YELLOW + " Invalid JSON format" + ANSI_RESET);
        }
        String jsonInput = rawInput.substring(1, rawInput.length() - 1).trim();
        String[] fieldDefs = jsonInput.split("\\}\s*,\s*");
        for (int i = 0; i < fieldDefs.length-1;i++){
            if (!fieldDefs[i].endsWith("\\}")){
                fieldDefs[i] = fieldDefs[i].concat("}");
            }
        }
        DataType dataType = new DataType(typeName);
        Pattern fieldPattern = Pattern.compile("\"(.*?)\"\\s*:\\s*\\{(.*?)\\}");
        for (String fieldDef : fieldDefs) {
            Matcher matcher = fieldPattern.matcher(fieldDef.trim());
            if (!matcher.find()) {
                throw new Exception(ANSI_YELLOW + " Invalid field format: " + fieldDef + ANSI_RESET);
            }
            String fieldName = matcher.group(1);
            if (Pattern.compile("\\W").matcher(fieldName).find()) {
                throw new Exception(ANSI_YELLOW + " Invalid field name: " + fieldName + ANSI_RESET);
            }
            String propsStr = matcher.group(2).replaceAll("\"", "");
            Map<String, String> props = new HashMap<>();
            for (String prop : propsStr.split("\\s*,\\s*")) {
                String[] keyValue = prop.split("\\s*:\\s*");
                if (keyValue.length != 2) continue;
                if (keyValue[0].equalsIgnoreCase("type")){
                    if (!validate(keyValue[1])) {
                        throw new Exception(ANSI_YELLOW + " Invalid type: " + keyValue[1] + ANSI_RESET);
                    }
                }
                props.put(keyValue[0].trim(), keyValue[1].trim());
            }
            String type = props.getOrDefault("type", "string");
            boolean required = Boolean.parseBoolean(props.getOrDefault("required", "false"));
            boolean unique = Boolean.parseBoolean(props.getOrDefault("unique", "false"));
            Field field = new Field(fieldName, type, required, unique);
            dataType.addField(field);
        }
        db.createDataType(typeName);
        DataType storedType = db.getDataType(typeName);
        for (Field f : dataType.getFields()) {
            storedType.addField(f);
        }
        System.out.println(ANSI_GREEN + "Type '" + typeName + "' created successfully." + ANSI_RESET);
    }
    private static boolean evaluateFilter(String filter, DataInstance instance, DataType dataType) {
        try {
            Pattern pattern = Pattern.compile("\\s*(\\w+)\\s*(<=|>=|=|<|>|!=)\\s*(\\S+)");
            Matcher matcher = pattern.matcher(filter.trim());
            if (!matcher.find()) return false;
            String fieldName = matcher.group(1).trim();
            String operator = matcher.group(2).trim();
            String valueStr = matcher.group(3).trim();
            Field field = dataType.getField(fieldName);
            if (field == null){
                field = dataType.getField(valueStr);
                if (field == null) return false;
                String tempFieldName = fieldName;
                fieldName = valueStr;
                valueStr = tempFieldName;
                switch (operator){
                    case ">": operator = "<"; break;
                    case "<=": operator = ">="; break;
                    case ">=": operator = "<="; break;
                    case "<": operator = ">"; break;
                }
            }
            Object instanceValue = instance.getValues(fieldName);
            Object filterValue = parseValue(valueStr, field.getType());
            return switch (operator) {
                case "=" -> instanceValue.equals(filterValue);
                case "<" -> compareValues(instanceValue, filterValue) < 0;
                case ">" -> compareValues(instanceValue, filterValue) > 0;
                case "<=" -> compareValues(instanceValue, filterValue) <= 0;
                case ">=" -> compareValues(instanceValue, filterValue) >= 0;
                case "!=" -> !instanceValue.equals(filterValue);
                default -> false;
            };
        } catch (Exception e) {
            return false;
        }
    }
    private static int compareValues(Object a, Object b) {
        if (a instanceof Number && b instanceof Number) {
            return Double.compare(((Number) a).doubleValue(), ((Number) b).doubleValue());
        } else if (a instanceof LocalDateTime && b instanceof LocalDateTime) {
            return ((LocalDateTime) a).compareTo((LocalDateTime) b);
        } else {
            return a.toString().compareTo(b.toString());
        }
    }
    private static String commandForm(String[] tokens, int startIndex){
        StringBuilder sb = new StringBuilder();
        for(int i = startIndex; i < tokens.length; i++){
            sb.append(tokens[i]).append(" ");
        }
        return sb.toString().trim();
    }
    private static Object parseValue(String valueStr, String type) throws Exception {
        valueStr = valueStr.trim();

        switch (type.toLowerCase()) {
            case "string":
                if (valueStr.startsWith("\"") && valueStr.endsWith("\"")) {
                    return valueStr.substring(1, valueStr.length() - 1);
                }
                throw new Exception(ANSI_YELLOW + " Invalid string value: " + valueStr + ANSI_RESET);
            case "int":
                if (valueStr.matches("-?\\d+")) {
                    return Integer.parseInt(valueStr);
                }
                throw new Exception(ANSI_YELLOW + " Invalid integer value: " + valueStr + ANSI_RESET);
            case "dbl":
                if (valueStr.matches("-?\\d+(\\.\\d+)?")) {
                    return Double.parseDouble(valueStr);
                }
                throw new Exception(ANSI_YELLOW + " Invalid double value: " + valueStr + ANSI_RESET);
            case "bool":
                if (valueStr.equalsIgnoreCase("true")) return true;
                if (valueStr.equalsIgnoreCase("false")) return false;
                throw new Exception(ANSI_YELLOW + " Invalid boolean value: " + valueStr + ANSI_RESET);
            case "time":
                try {
                    return LocalDateTime.parse(valueStr);
                } catch (Exception e) {
                    throw new Exception(ANSI_YELLOW + " Invalid time format. Use 'yyyy-MM-ddTHH:mm:ss'" + ANSI_RESET);
                }
            default:
                throw new Exception(ANSI_RED + " Unsupported type: " + type + ANSI_RESET);
        }
    }

    public static boolean validate(String type) {
        return switch (type) {
            case "string", "int", "dbl", "bool", "time" -> true;
            default -> false;
        };
    }
}