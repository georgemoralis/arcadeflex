/*
 * ported to v0.36
 */
/**
 * Changelog
 * =========
 * 24/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.platform;

//mame imports
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.mame.*;

public class vector {

    /* Scale the vector games to a given resolution */
    public static void scale_vectorgames(int gfx_width, int gfx_height, int[] width, int[] height) {
        double x_scale, y_scale, scale;

        if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0) {
            x_scale = (double) gfx_width / (double) (height[0]);
            y_scale = (double) gfx_height / (double) (width[0]);
        } else {
            x_scale = (double) gfx_width / (double) (width[0]);
            y_scale = (double) gfx_height / (double) (height[0]);
        }
        if (x_scale < y_scale) {
            scale = x_scale;
        } else {
            scale = y_scale;
        }
        width[0] = (int) ((double) width[0] * scale);
        height[0] = (int) ((double) height[0] * scale);

        /* Padding to an dword value */
        width[0] -= width[0] % 4;
        height[0] -= height[0] % 4;
    }

}
