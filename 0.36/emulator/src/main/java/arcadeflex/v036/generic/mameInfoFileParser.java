/**
 * ported to v0.36
 * Most of the functions are based on java way of parsing files with strings
 */
/**
 * Changelog
 * =========
 * 13/12/2022 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.generic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class mameInfoFileParser {

    private static File file = null;
    protected static Scanner scanner;
    public static String gameinfo = null;

    public static int loadMameInfoFile(String fileName) {
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

        StringBuilder infoText = new StringBuilder();
        /* Find next line starting with $info */
        while (!line.startsWith("$info") && scanner.hasNext()) {
            line = scanner.nextLine();
        }
        // $info line has one game
        String game = line.substring(6, line.length());

        // NOTE March 2017 end of messinfo.dat has changed we can get here on EOF
        /* Find next line starting with $mame & GOTO next line */
        if (scanner.hasNext()) {
            do {
                line = scanner.nextLine();
            } while (!line.startsWith("$mame") && scanner.hasNext());
        }

        /* Are we at EOF? */
        if (!scanner.hasNext()) {
            return;
        }
        line = scanner.nextLine(); // skip $mame

        /* Until line starting with $end */
        do {
            // TODO do we need to add an endline here?
            // YES But it appears to give us two!! ??
            infoText.append(line).append('\n');
            line = scanner.nextLine();
        } while (!line.startsWith("$end"));

        if (game.equals(gamename)) {
            gameinfo = infoText.toString();
        }

    }

    public static char[] convertToCharArrray() {
        if (gameinfo == null) {
            return null;
        }
        return gameinfo.toCharArray();
    }
}
