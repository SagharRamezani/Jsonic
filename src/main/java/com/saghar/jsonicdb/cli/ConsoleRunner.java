package com.saghar.jsonicdb.cli;

import com.saghar.jsonicdb.core.Database;
import com.saghar.jsonicdb.parser.Command;
import com.saghar.jsonicdb.parser.CommandParser;
import com.saghar.jsonicdb.util.JsonicException;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

public final class ConsoleRunner {
    private ConsoleRunner() {}

    public static void run(InputStream in, PrintStream out, PrintStream err, boolean prompt) {
        Database db = new Database();
        CommandParser parser = new CommandParser();

        try (Scanner sc = new Scanner(in)) {
            while (true) {
                if (prompt) out.print("> ");
                String line;
                try {
                    line = sc.nextLine();
                } catch (NoSuchElementException eof) {
                    break;
                }
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.equalsIgnoreCase("exit")) break;

                try {
                    Command cmd = parser.parse(line);
                    String result = cmd.execute(db);
                    if (result != null && !result.isBlank()) out.println(result);
                } catch (JsonicException ex) {
                    err.println("Error: " + ex.getMessage());
                } catch (Exception ex) {
                    err.println("Error: unexpected failure (" + ex.getClass().getSimpleName() + ")");
                }
            }
        }
    }
}
