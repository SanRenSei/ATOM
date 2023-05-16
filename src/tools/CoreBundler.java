package tools;

/*

This class, when run, will take all Java files in core package, and bundle them into one file
Then http://www.jsweet.org/jsweet-live-sandbox/ will convert to Javascript, so we can have a JS interpreter of ATOM
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CoreBundler {

    public static String[] importBlacklist = {"java.io.File", "java.util.Scanner","java.util.stream.Collectors"};

    private final static int NO_COMMAND = 0;
    private final static int BEGIN_IGNORE = 1;
    private final static int END_IGNORE = 2;
    private final static int BEGIN_INJECT = 3;
    private final static int END_INJECT = 4;

    public static void main(String[] args) throws Exception {
        File f = new File("src/main");
        File[] files = f.listFiles();
        System.out.println(files.length);
        List<String> imports = new ArrayList<>();
        StringBuilder otherContent = new StringBuilder();
        for (File file : files) {
            Scanner reader = new Scanner(file);
            while (reader.hasNext()) {
                String line = reader.nextLine().trim();
                if (line.length() > 6 && line.substring(0, 6).equals("import")) {
                    if (!imports.contains(line) && !isOnImportBlacklist(line)) {
                        imports.add(line);
                    }
                }
                if (line.contains("class ") || line.contains("enum ")) {
                    if (line.substring(0, 6).equals("public")) {
                        line = line.substring(6).trim();
                    }
                    otherContent.append(line);
                    boolean ignoringLines = false;
                    boolean injectingComments = false;
                    while (reader.hasNext()) {
                        String l = reader.nextLine();
                        int command = isCommand(l);
                        switch (command) {
                            case NO_COMMAND:
                                if (!ignoringLines) {
                                    if (injectingComments) {
                                        otherContent.append(getCommentedLine(l));
                                    } else {
                                        otherContent.append(getNonCommentLine(l));
                                    }
                                }
                                break;
                            case BEGIN_IGNORE:
                                ignoringLines = true;
                                break;
                            case END_IGNORE:
                                ignoringLines = false;
                                break;
                            case BEGIN_INJECT:
                                injectingComments = true;
                                break;
                            case END_INJECT:
                                injectingComments = false;
                        }
                    }
                }
            }
        }
        System.out.println(imports);
        File out = new File("gen/ATOMTemplate.java");
        if (!out.exists()) {
            out.createNewFile();
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(out));
        for (String i : imports) {
            writer.write(i);
        }
        writer.write(otherContent.toString());
        writer.flush();
        writer.close();
    }

    public static boolean isOnImportBlacklist(String importLine) {
        for (String s : importBlacklist) {
            if (importLine.contains(s)) {
                return true;
            }
        }
        return false;
    }

    public static int isCommand(String line) {
        line = line.trim();
        if (line.length() > 2 && line.substring(0, 2).equals("//")) {
            line = line.substring(2).trim();
            if (line.length() > 8 && line.substring(0, 8).equals("BUNDLER:")) {
                line = line.substring(8);
                if (line.trim().equals("BEGIN IGNORE")) {
                    return BEGIN_IGNORE;
                }
                if (line.trim().equals("END IGNORE")) {
                    return END_IGNORE;
                }
                if (line.trim().equals("BEGIN INJECT")) {
                    return BEGIN_INJECT;
                }
                if (line.trim().equals("END INJECT")) {
                    return END_INJECT;
                }
            }
        }
        return NO_COMMAND;
    }

    private static String getCommentedLine(String line) {
        return line.trim().substring(2);
    }

    private static String getNonCommentLine(String line) {
        line = line.trim();
        if (line.contains("//")) {
            int n = line.indexOf("//");
            return line.substring(0, n);
        }
        return line;
    }

}
