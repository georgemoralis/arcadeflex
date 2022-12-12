/**
 * 0.36 compatible
 */
package arcadeflex.v036.generic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class historyFileParser {

    private static File file = null;
    protected static Scanner scanner;
    public static String gameinfo = null;

    public static int loadHistoryFile(String fileName) {
        file = new File(fileName);
        if (file.exists()) {
            return 1;
        } else {
            return 0;
        }
    }

    public static void processFile(String gamename) {
        try {
            scanner = new Scanner(new FileReader(file));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        try {
            //Scanner to get each line
            while (scanner.hasNextLine()) {
                processLine(scanner.nextLine(), gamename);
            }
        } finally {
            //Close the underlying stream
            scanner.close();
        }

    }

    public static void processLine(String line, String gamename) {

        StringBuilder historyText = new StringBuilder();

        /* Find next line starting with $info */
        while (!line.startsWith("$info") && scanner.hasNext()) {
            line = scanner.nextLine();
        }
        /* Are we at EOF? */
        if (!scanner.hasNext()) {
            return;
        }
        // $info line has one or more game names
        line = line.substring(6, line.length() - 1);
        String[] games = line.trim().split(",");
        /* Find next line starting with $bio & GOTO next line */
        do {
            line = scanner.nextLine();
        } while (!line.startsWith("$bio"));
        line = scanner.nextLine(); // skip $bio

        /* Until line starting with $end */
        do {
            // TODO do we need to add an endline here?
            // YES But it appears to give us two!! ??
            historyText.append(line).append('\n');
            line = scanner.nextLine();
        } while (!line.startsWith("$end"));
        for (String game : games) {
            if (game.equals(gamename)) {
                gameinfo = historyText.toString();
            }
        }
    }

    public static char[] convertToCharArrray() {
        if (gameinfo == null) {
            return null;
        }
        return gameinfo.toCharArray();
    }
}
