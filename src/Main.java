import java.util.Scanner;
import java.util.regex.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("> ");
        String input = sc.nextLine();
        while (!input.equals("exit")) {
            Matcher createMatch = Pattern.compile("/create\\s+\\w+\\s*\\{\\s*\"\\w+\"\\s*:\\s*\\{\\s*\"type\"\\s*:\\s*\"\\w+\"\\s*(,\\s*\"unique\"\\s*:\\s*(true|false)\\s*)?\\s*(,\\s*\"required\"\\s*:\\s*(true|false)\\s*)?}\\s*(\\s*,\\s*\"\\w+\"\\s*:\\s*\\{\\s*\"type\"\\s*:\\s*\"\\w+\"\\s*(,\\s*\"unique\"\\s*:\\s*(true|false)\\s*)?\\s*(,\\s*\"required\"\\s*:\\s*(true|false)\\s*)?\\s*}\\s*)*\\s*}/gm").matcher(input);
            Matcher insertMatch = Pattern.compile("/insert\\s+\\w+\\s*\\{\\s*\"\\w+\"\\s*:\\s*(\\d+|\"\\S+\"|true|false)\\s*(\\s*,\\s*\"\\w+\"\\s*:\\s*(\\d+|\"\\S+\"|true|false)\\s*)*\\s*}/gm").matcher(input);
            Matcher updateMatch = Pattern.compile("/update\\s+\\w+\\s*(\\(\\S+\\s*[=><]\\s*(\\d+|\"\\S+\"|true|false)\\))?\\s*\\{\\s*\"\\w+\"\\s*:\\s*(\\d+|\"\\S+\"|true|false)(\\s*,\\s*\"\\w+\"\\s*:\\s*(\\d+|\"\\S+\"|true|false)\\s*)*\\s*}/gm").matcher(input);
            Matcher searchMatch = Pattern.compile("/search\\s+\\w+\\s*(\\(\\S+\\s*[=><]\\s*(\\d+|\"\\S+\"|true|false)\\))?/gm").matcher(input);
            Matcher deleteMatch = Pattern.compile("/delete\\s+\\w+\\s*(\\(\\S+\\s*[=><]\\s*(\\d+|\"\\S+\"|true|false)\\))?/gm").matcher(input);
            Matcher matcher = Pattern.compile("").matcher(input);
        }

    }
}