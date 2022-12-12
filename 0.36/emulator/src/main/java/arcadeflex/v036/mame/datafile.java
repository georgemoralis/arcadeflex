/**
 * ported to v0.36
 * Most of the functions are based on java way of parsing files with strings
 */
package arcadeflex.v036.mame;

import arcadeflex.v036.generic.historyFileParser;
import arcadeflex.v036.generic.mameInfoFileParser;
import arcadeflex.v036.mame.driverH.GameDriver;

public class datafile {

    public static String history_filename = "history.dat";
    public static String mameinfo_filename = "mameinfo.dat";

    /**
     * ************************************************************************
     * load_driver_history Load history text for the specified driver into the
     * specified buffer. Combines $bio field of HISTORY.DAT with $mame field of
     * MAMEINFO.DAT.
     *
     * Returns 0 if successful.
     *
     * NOTE: For efficiency the indices are never freed (intentional leak).
     * ************************************************************************
     */
    public static int load_driver_history(GameDriver drv, String[] buffer, int bufsize) {
        int history = 0, mameinfo = 0;
        buffer[0] = "";

        /* try to open history datafile */
        if (historyFileParser.loadHistoryFile(history_filename) != 0) {
            historyFileParser.processFile(drv.name);
            char[] foundgame = historyFileParser.convertToCharArrray();
            if (foundgame != null) {
                buffer[0] = new String(foundgame);
                history = 1;
            } else {
                //if no found find the parent one and load
                if (drv.clone_of != null) {
                    historyFileParser.processFile(drv.clone_of.name);
                    char[] foundparent = historyFileParser.convertToCharArrray();
                    if (foundparent != null) {
                        buffer[0] = new String(foundparent);
                        history = 1;
                    }
                }
            }
        }
        if (mameInfoFileParser.loadMameInfoFile(mameinfo_filename) != 0) {

            mameInfoFileParser.processFile(drv.name);
            char[] foundgame = mameInfoFileParser.convertToCharArrray();
            if (foundgame != null) {
                buffer[0] += new String(foundgame);
                mameinfo = 1;
            } else {
                //if no found find the parent one and load
                if (drv.clone_of != null) {
                    mameInfoFileParser.processFile(drv.clone_of.name);
                    char[] foundparent = mameInfoFileParser.convertToCharArrray();
                    if (foundparent != null) {
                        buffer[0] += new String(foundparent);
                        mameinfo = 1;
                    }
                }
            }
        }
        return (history == 0 && mameinfo == 0) ? 1 : 0;
    }
}
