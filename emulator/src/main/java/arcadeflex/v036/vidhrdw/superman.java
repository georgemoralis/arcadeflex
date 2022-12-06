/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package arcadeflex.v036.vidhrdw;

//common imports
import static common.libc.cstring.*;
import static common.libc.expressions.*;
//TODO
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;

public class superman {

    public static int[] supes_videoram_size = new int[1];
    public static int[] supes_attribram_size = new int[1];

    public static UBytePtr supes_videoram = new UBytePtr();
    public static UBytePtr supes_attribram = new UBytePtr();
    //static unsigned char *dirtybuffer;		/* foreground */
    //static unsigned char *dirtybuffer2;		/* background */

    public static VhStartPtr superman_vh_start = new VhStartPtr() {
        public int handler() {
            return 0;
        }
    };

    public static VhStopPtr superman_vh_stop = new VhStopPtr() {
        public void handler() {
        }
    };

    /**
     * ***********************************
     *
     * Foreground RAM
     *
     ************************************
     */
    public static WriteHandlerPtr supes_attribram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int oldword = supes_attribram.READ_WORD(offset);
            int newword = COMBINE_WORD(oldword, data);

            if (oldword != newword) {
                supes_attribram.WRITE_WORD(offset, data);
                //		dirtybuffer2[offset/2] = 1;
            }
        }
    };

    public static ReadHandlerPtr supes_attribram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return supes_attribram.READ_WORD(offset);
        }
    };

    /**
     * ***********************************
     *
     * Background RAM
     *
     ************************************
     */
    public static WriteHandlerPtr supes_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int oldword = supes_videoram.READ_WORD(offset);
            int newword = COMBINE_WORD(oldword, data);

            if (oldword != newword) {
                supes_videoram.WRITE_WORD(offset, data);
                //		dirtybuffer[offset/2] = 1;
            }
        }
    };

    public static ReadHandlerPtr supes_videoram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return supes_videoram.READ_WORD(offset);
        }
    };

    public static void superman_update_palette() {
        char[] palette_map = new char[32];
        /* range of color table is 0-31 */

        int i;

        memset(palette_map, 0, sizeof(palette_map));

        /* Find colors used in the background tile plane */
        for (i = 0; i < 0x400; i += 0x40) {
            int i2;

            for (i2 = i; i2 < (i + 0x40); i2 += 2) {
                int tile;
                int color;

                color = 0;

                tile = supes_videoram.READ_WORD(0x800 + i2) & 0x3fff;
                if (tile != 0) {
                    color = supes_videoram.READ_WORD(0xc00 + i2) >> 11;
                }

                palette_map[color] |= Machine.gfx[0].pen_usage[tile];

            }
        }

        /* Find colors used in the sprite plane */
        for (i = 0x3fe; i >= 0; i -= 2) {
            int tile;
            int color;

            color = 0;

            tile = supes_videoram.READ_WORD(i) & 0x3fff;
            if (tile != 0) {
                color = supes_videoram.READ_WORD(0x400 + i) >> 11;
            }

            palette_map[color] |= Machine.gfx[0].pen_usage[tile];
        }

        /* Now tell the palette system about those colors */
        for (i = 0; i < 32; i++) {
            int usage = palette_map[i];
            int j;

            if (usage != 0) {
                palette_used_colors.write(i * 16 + 0, PALETTE_COLOR_TRANSPARENT);
                for (j = 1; j < 16; j++) {
                    if ((palette_map[i] & (1 << j)) != 0) {
                        palette_used_colors.write(i * 16 + j, PALETTE_COLOR_USED);
                    } else {
                        palette_used_colors.write(i * 16 + j, PALETTE_COLOR_UNUSED);
                    }
                }
            } else {
                memset(palette_used_colors, i * 16, PALETTE_COLOR_UNUSED, 16);
            }
        }

        palette_recalc();

    }
    public static VhUpdatePtr superman_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {

            int i;

            superman_update_palette();

            osd_clearbitmap(bitmap);

            /* Refresh the background tile plane */
            for (i = 0; i < 0x400; i += 0x40) {
                int x1, y1;
                int i2;

                x1 = supes_attribram.READ_WORD(0x408 + (i >> 1));
                y1 = supes_attribram.READ_WORD(0x400 + (i >> 1));

                for (i2 = i; i2 < (i + 0x40); i2 += 2) {
                    int tile;

                    tile = supes_videoram.READ_WORD(0x800 + i2) & 0x3fff;
                    if (tile != 0) {
                        int x, y;

                        x = (x1 + ((i2 & 0x03) << 3)) & 0x1ff;
                        y = ((265 - (y1 - ((i2 & 0x3c) << 2))) & 0xff);

                        //				if ((x > 0) && (y > 0) && (x < 388) && (y < 272))
                        {
                            int flipx = supes_videoram.READ_WORD(0x800 + i2) & 0x4000;
                            int flipy = supes_videoram.READ_WORD(0x800 + i2) & 0x8000;
                            int color = supes_videoram.READ_WORD(0xc00 + i2) >> 11;

                            /* Some tiles are transparent, e.g. the gate, so we use TRANSPARENCY_PEN */
                            drawgfx(bitmap, Machine.gfx[0],
                                    tile,
                                    color,
                                    flipx, flipy,
                                    x, y,
                                    Machine.drv.visible_area,
                                    TRANSPARENCY_PEN, 0);
                        }
                    }
                }
            }

            /* Refresh the sprite plane */
            for (i = 0x3fe; i >= 0; i -= 2) {
                int sprite;

                sprite = supes_videoram.READ_WORD(i) & 0x3fff;
                if (sprite != 0) {
                    int x, y;

                    x = (supes_videoram.READ_WORD(0x400 + i)) & 0x1ff;
                    y = (250 - supes_attribram.READ_WORD(i)) & 0xff;

                    //			if ((x > 0) && (y > 0) && (x < 388) && (y < 272))
                    {
                        int flipy = supes_videoram.READ_WORD(i) & 0x4000;
                        int flipx = supes_videoram.READ_WORD(i) & 0x8000;
                        int color = supes_videoram.READ_WORD(0x400 + i) >> 11;

                        drawgfx(bitmap, Machine.gfx[0],
                                sprite,
                                color,
                                flipx, flipy,
                                x, y,
                                Machine.drv.visible_area,
                                TRANSPARENCY_PEN, 0);
                    }
                }
            }
        }
    };
}
