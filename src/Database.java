import java.util.ArrayList;

public class Database {
    private ArrayList<DataType> dataTypes;

    public Database() {
        this.dataTypes = new ArrayList<>();
    }

    public static String ANSI_YELLOW = "\u001B[33m";
    public static String ANSI_RESET = "\u001B[0m";

    public void createDataType(String name) {
        for (DataType dt : dataTypes) {
            if (dt.getName().equalsIgnoreCase(name)) {
                throw new IllegalArgumentException(ANSI_YELLOW + " DataType '" + name + "' already exists." + ANSI_RESET);
            }
        }
        dataTypes.add(new DataType(name));
        System.out.println("Data type " + name + " created");
    }

    public DataType getDataType(String name) {
        for (DataType dt : dataTypes) {
            if (dt.getName().equals(name)) {
                return dt;
            }
        }
        return null;
    }
}
