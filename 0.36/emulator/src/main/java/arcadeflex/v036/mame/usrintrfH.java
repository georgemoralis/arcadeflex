/**
 * ported to v0.36
 */
/**
 * Changelog
 * =========
 * 12/12/2022 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.mame;

public class usrintrfH {

    public static class DisplayText {

        public static DisplayText[] create(int n) {
            DisplayText[] a = new DisplayText[n];
            for (int k = 0; k < n; k++) {
                a[k] = new DisplayText();
            }
            return a;
        }
        public String text;/* 0 marks the end of the array */
        public int color;/* see #defines below */
        public int x;
        public int y;
    }

    public static final int DT_COLOR_WHITE = 0;
    public static final int DT_COLOR_YELLOW = 1;
    public static final int DT_COLOR_RED = 2;
}
