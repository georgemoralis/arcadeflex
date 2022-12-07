/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 */
package arcadeflex.v036.vidhrdw;

//mame imports
import static arcadeflex.v036.mame.osdependH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//TODO
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class copsnrob {

    // Color constants
    public static final int COPSNROB_COLOR_BLUE = 0;
    public static final int COPSNROB_COLOR_YELLOW = 1;
    public static final int COPSNROB_COLOR_AMBER = 2;

    // Color divider position
    public static final int COPSNROB_DIVIDER_BY = 9;
    public static final int COPSNROB_DIVIDER_YA = 23;

    // This value was obtained by a line in the manual that stated that
    // there were "about 26" lines visible
    public static final int COPSNROB_VISIBLE_LINE = 26;
    public static final int COPSNROB_Y_OFFSET = 3;

    static rectangle visiblearea = new rectangle(
            0 * 8, 32 * 8 - 1,
            COPSNROB_Y_OFFSET * 8, (COPSNROB_VISIBLE_LINE + COPSNROB_Y_OFFSET) * 8 - 1
    );

    public static UBytePtr copsnrob_bulletsram = new UBytePtr();
    public static UBytePtr copsnrob_carimage = new UBytePtr();
    public static UBytePtr copsnrob_cary = new UBytePtr();
    public static UBytePtr copsnrob_trucky = new UBytePtr();

    /**
     * *************************************************************************
     *
     * Draw the game screen in the given osd_bitmap. Do NOT call
     * osd_update_display() from this function, it will be called by the main
     * emulation engine.
     *
     **************************************************************************
     */
    public static VhUpdatePtr copsnrob_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs, x, y, bullet, mask1, mask2;

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = COPSNROB_VISIBLE_LINE * 32 - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy, xoff, yoff, color;

                    dirtybuffer[offs] = 0;

                    sx = 31 - (offs % 32);
                    sy = offs / 32;

                    if (sx < COPSNROB_DIVIDER_BY) {
                        color = COPSNROB_COLOR_BLUE;
                    } else if (sx < COPSNROB_DIVIDER_YA) {
                        color = COPSNROB_COLOR_YELLOW;
                    } else {
                        color = COPSNROB_COLOR_AMBER;
                    }

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            videoram.read(offs) & 0x3f,
                            color,
                            0, 0,
                            8 * sx, 8 * (sy + COPSNROB_Y_OFFSET),
                            visiblearea, TRANSPARENCY_NONE, 0);

                    // According to the manual, the yellow area just supposed to cover
                    // the solid lines. This means that we need to turn the first
                    // 4 pixels of column 23 yellow.
                    if (sx != COPSNROB_DIVIDER_YA) {
                        continue;
                    }

                    for (yoff = 0; yoff < 8; yoff++) {
                        for (xoff = 0; xoff < 4; xoff++) {
                            if (read_pixel.handler(tmpbitmap, 8 * sx + xoff, 8 * (sy + COPSNROB_Y_OFFSET) + yoff) != Machine.pens[0]) {
                                plot_pixel.handler(tmpbitmap, 8 * sx + xoff, 8 * (sy + COPSNROB_Y_OFFSET) + yoff, Machine.pens[COPSNROB_COLOR_YELLOW + 1]);
                            }
                        }
                    }
                }
            }

            /* copy the character mapped graphics */
            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

            /* Draw the cars. Positioning was based on a screen shot */
            if (copsnrob_cary.read(0) != 0) {
                drawgfx(bitmap, Machine.gfx[1],
                        copsnrob_carimage.read(0),
                        COPSNROB_COLOR_AMBER,
                        1, 0,
                        0xe4, (256 - copsnrob_cary.read(0)) + 8 * COPSNROB_Y_OFFSET,
                        visiblearea, TRANSPARENCY_PEN, 0);
            }

            if (copsnrob_cary.read(1) != 0) {
                drawgfx(bitmap, Machine.gfx[1],
                        copsnrob_carimage.read(1),
                        COPSNROB_COLOR_AMBER,
                        1, 0,
                        0xc4, (256 - copsnrob_cary.read(1)) + 8 * COPSNROB_Y_OFFSET,
                        visiblearea, TRANSPARENCY_PEN, 0);
            }

            if (copsnrob_cary.read(2) != 0) {
                drawgfx(bitmap, Machine.gfx[1],
                        copsnrob_carimage.read(2),
                        COPSNROB_COLOR_BLUE,
                        0, 0,
                        0x24, (256 - copsnrob_cary.read(2)) + 8 * COPSNROB_Y_OFFSET,
                        visiblearea, TRANSPARENCY_PEN, 0);
            }

            if (copsnrob_cary.read(3) != 0) {
                drawgfx(bitmap, Machine.gfx[1],
                        copsnrob_carimage.read(3),
                        COPSNROB_COLOR_BLUE,
                        0, 0,
                        0x04, (256 - copsnrob_cary.read(3)) + 8 * COPSNROB_Y_OFFSET,
                        visiblearea, TRANSPARENCY_PEN, 0);
            }

            /* Draw the beer truck. Positioning was based on a screen shot.
             Even though the manual says there can be up to 3 beer trucks
             on the screen, after examining the code, I don't think that's the
             case. I also verified this just by playing the game, if there were
             invisible trucks, the bullets would disappear. */
            if (copsnrob_trucky.read(0) != 0) {
                drawgfx(bitmap, Machine.gfx[2],
                        0,
                        COPSNROB_COLOR_YELLOW,
                        0, 0,
                        0x80, (256 - copsnrob_trucky.read(0)) + 8 * COPSNROB_Y_OFFSET,
                        visiblearea, TRANSPARENCY_PEN, 0);
            }

            /* Draw the bullets.
             They are flickered on/off every frame by the software, so don't
             play it with frameskip 1 or 3, as they could become invisible */
            for (x = 0; x < 256; x++) {
                int color;
                int val = copsnrob_bulletsram.read(x);

                // Check for the most common case
                if ((val & 0x0f) == 0) {
                    continue;
                }

                if (256 - x < COPSNROB_DIVIDER_BY * 8) {
                    color = COPSNROB_COLOR_BLUE;
                } else if (256 - x < COPSNROB_DIVIDER_YA * 8 + 2) {
                    color = COPSNROB_COLOR_YELLOW;
                } else {
                    color = COPSNROB_COLOR_AMBER;
                }

                mask1 = 0x01;
                mask2 = 0x10;

                // Check each bullet
                for (bullet = 0; bullet < 4; bullet++) {
                    if ((val & mask1) != 0) {
                        for (y = 0; y < COPSNROB_VISIBLE_LINE * 8; y++) {
                            if ((copsnrob_bulletsram.read(y) & mask2) != 0) {
                                plot_pixel.handler(bitmap, 256 - x, y + 8 * COPSNROB_Y_OFFSET, Machine.pens[color + 1]);
                            }
                        }
                    }

                    mask1 <<= 1;
                    mask2 <<= 1;
                }
            }
        }
    };
}
