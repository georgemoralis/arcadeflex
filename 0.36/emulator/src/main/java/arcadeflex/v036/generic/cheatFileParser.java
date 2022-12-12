/**
 * 0.36 compatible
 */
package arcadeflex.v036.generic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class cheatFileParser {

    private static File file = null;
    private static ArrayList<String> cheatlines = new ArrayList<>();

    public static int loadCheatFile(String fileName) {
        file = new File(fileName);
        if (file.exists()) {
            return 1;
        } else {
            return 0;
        }
    }

    public static ArrayList<String> read(String gamename) {
        BufferedReader reader = null;
        cheatlines.clear();
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!isComment(line)) {
                    if (isCheatLine(line, gamename)) {
                        cheatlines.add(line);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return cheatlines;
    }

    private static boolean isComment(String line) {
        return line.trim().startsWith(";");
    }

    private static boolean isCheatLine(String line, String gamename) {
        //game starts with game name and next char is :
        return line.startsWith(gamename) && line.charAt(gamename.length()) == ':';
    }
}
