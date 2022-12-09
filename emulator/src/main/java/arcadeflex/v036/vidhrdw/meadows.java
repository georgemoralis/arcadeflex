/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.artwork.*;
import static arcadeflex.v036.mame.artworkH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//common imports
import static common.libc.cstring.*;
//TODO
import static gr.codebb.arcadeflex.v036.mame.drawgfx.drawgfx;
import gr.codebb.arcadeflex.v036.mame.driverH.VhStartPtr;
import gr.codebb.arcadeflex.v036.mame.driverH.VhStopPtr;
import gr.codebb.arcadeflex.v036.mame.driverH.VhUpdatePtr;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static arcadeflex.v036.mame.drawgfxH.TRANSPARENCY_NONE;
import static arcadeflex.v036.mame.drawgfxH.TRANSPARENCY_PEN;
import arcadeflex.v036.mame.drawgfxH.rectangle;
import static gr.codebb.arcadeflex.v037b7.mame.palette.palette_recalc;

public class meadows {

    /* some constants to make life easier */
    public static final int SCR_HORZ = 32;
    public static final int SCR_VERT = 30;
    public static final int CHR_HORZ = 8;
    public static final int CHR_VERT = 8;
    public static final int SPR_COUNT = 4;
    public static final int SPR_HORZ = 16;
    public static final int SPR_VERT = 16;
    public static final int SPR_ADJUST_X = -18;
    public static final int SPR_ADJUST_Y = -14;

    static int[] sprite_dirty = new int[SPR_COUNT];/* dirty flags */
    static int[] sprite_horz = new int[SPR_COUNT];/* x position */
    static int[] sprite_vert = new int[SPR_COUNT];/* y position */
    static int[] sprite_index = new int[SPR_COUNT];/* index 0x00..0x0f, prom 0x10, flip horz 0x20 */

    static struct_artwork overlay;
    static artwork_element deadeye_artwork[] = {
        new artwork_element(new rectangle(0, SCR_HORZ * 8, 0, SCR_VERT * 8), new char[]{255, 255, 255}, (char) 255),
        new artwork_element(new rectangle(0, SCR_HORZ * 8 - 1, 0, 4 * 8 - 1), new char[]{32, 192, 64}, (char) 160),
        new artwork_element(new rectangle(0, SCR_HORZ * 8 - 1, 4 * 8, 8 * 8 - 1), new char[]{64, 64, 192}, (char) 160),
        new artwork_element(new rectangle(0, SCR_HORZ * 8 - 1, 8 * 8, 11 * 8 - 1), new char[]{192, 160, 32}, (char) 160),
        new artwork_element(new rectangle(0, 1 * 8 - 1, 11 * 8, 26 * 8 - 1), new char[]{192, 160, 32}, (char) 160),
        new artwork_element(new rectangle(SCR_HORZ * 8 - 8, SCR_HORZ * 8 - 1, 11 * 8, 26 * 8 - 1), new char[]{192, 160, 32}, (char) 160),
        new artwork_element(new rectangle(8, SCR_HORZ * 8 - 8 - 1, 11 * 8, 26 * 8 - 1), new char[]{192, 192, 192}, (char) 160),
        new artwork_element(new rectangle(0, SCR_HORZ * 8 - 1, 26 * 8, SCR_VERT * 8 - 1), new char[]{64, 64, 192}, (char) 160),
        new artwork_element(new rectangle(-1, -1, -1, -1), new char[]{0, 0, 0}, (char) 0)
    };

    static artwork_element gypsyjug_artwork[] = {
        new artwork_element(new rectangle(0, SCR_HORZ * 8, 0, SCR_VERT * 8), new char[]{255, 255, 255}, (char) 255),
        new artwork_element(new rectangle(0, SCR_HORZ * 8 - 1, 0, 4 * 8 - 1), new char[]{32, 192, 64}, (char) 160),
        new artwork_element(new rectangle(0, SCR_HORZ * 8 - 1, 4 * 8, 8 * 8 - 1), new char[]{64, 64, 192}, (char) 160),
        new artwork_element(new rectangle(0, SCR_HORZ * 8 - 1, 4 * 8, 5 * 8 - 1), new char[]{32, 192, 64}, (char) 160),
        new artwork_element(new rectangle(0, SCR_HORZ * 8 - 1, 5 * 8, 8 * 8 - 1), new char[]{64, 64, 192}, (char) 160),
        new artwork_element(new rectangle(0, SCR_HORZ * 8 - 1, 8 * 8, 11 * 8 - 1), new char[]{192, 160, 32}, (char) 160),
        new artwork_element(new rectangle(0, 1 * 8 - 1, 11 * 8, 26 * 8 - 1), new char[]{192, 160, 32}, (char) 160),
        new artwork_element(new rectangle(SCR_HORZ * 8 - 8, SCR_HORZ * 8 - 1, 11 * 8, 26 * 8 - 1), new char[]{192, 160, 32}, (char) 160),
        new artwork_element(new rectangle(8, SCR_HORZ * 8 - 8 - 1, 11 * 8, 26 * 8 - 1), new char[]{192, 192, 192}, (char) 160),
        new artwork_element(new rectangle(0, SCR_HORZ * 8 - 1, 26 * 8, SCR_VERT * 8 - 1), new char[]{192, 160, 32}, (char) 160),
        new artwork_element(new rectangle(-1, -1, -1, -1), new char[]{0, 0, 0}, (char) 0)
    };

    /**
     * **********************************************************
     */
    /* video handler start                                       */
    /**
     * **********************************************************
     */
    public static VhStartPtr deadeye_vh_start = new VhStartPtr() {
        public int handler() {
            if (generic_vh_start.handler() != 0) {
                return 1;
            }
            overlay = artwork_create(deadeye_artwork, 2, Machine.drv.total_colors - 2);
            if (overlay == null) {
                return 1;
            }
            return 0;
        }
    };

    public static VhStartPtr gypsyjug_vh_start = new VhStartPtr() {
        public int handler() {
            if (generic_vh_start.handler() != 0) {
                return 1;
            }
            overlay = artwork_create(gypsyjug_artwork, 2, Machine.drv.total_colors - 2);
            if (overlay == null) {
                return 1;
            }
            return 0;
        }
    };

    /**
     * **********************************************************
     */
    /* video handler stop                                        */
    /**
     * **********************************************************
     */
    public static VhStopPtr meadows_vh_stop = new VhStopPtr() {
        public void handler() {
            if (overlay != null) {
                artwork_free(overlay);
            }
            generic_vh_stop.handler();
        }
    };

    /**
     * **********************************************************
     */
    /* draw dirty sprites                                        */
    /**
     * **********************************************************
     */
    static void meadows_draw_sprites(osd_bitmap bitmap) {
        int i;
        for (i = 0; i < SPR_COUNT; i++) {
            int x, y, n, p, f;
            if (sprite_dirty[i] == 0) /* sprite not dirty? */ {
                continue;
            }
            sprite_dirty[i] = 0;
            x = sprite_horz[i];
            y = sprite_vert[i];
            n = sprite_index[i] & 0x0f;
            /* bit #0 .. #3 select sprite */
            //		p = (sprite_index[i] >> 4) & 1; 	/* bit #4 selects prom ??? */
            p = i;
            /* that fixes it for now :-/ */
            f = sprite_index[i] >> 5;
            /* bit #5 flip vertical flag */
            drawgfx(bitmap, Machine.gfx[p + 1],
                    n, 1, f, 0, x, y,
                    Machine.drv.visible_area,
                    TRANSPARENCY_PEN, 0);
        }
    }

    /**
     * **********************************************************
     */
    /* mark character cell dirty                                 */
    /**
     * **********************************************************
     */
    public static WriteHandlerPtr meadows_char_dirty = new WriteHandlerPtr() {
        public void handler(int x, int y) {
            int i;
            /* scan sprites */
            for (i = 0; i < 4; i++) {
                /* check if sprite rectangle intersects with text rectangle */
                if ((x + 7 >= sprite_horz[i] && x <= sprite_horz[i] + 15)
                        || (y + 7 >= sprite_vert[i] && y <= sprite_vert[i] + 15)) {
                    sprite_dirty[i] = 1;
                }
            }
        }
    };

    /**
     * **********************************************************
     */
    /* Screen refresh											 */
    /**
     * **********************************************************
     */
    public static VhUpdatePtr meadows_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int i;

            if (palette_recalc() != null || full_refresh != 0) {
                overlay_remap(overlay);
                memset(dirtybuffer, 1, SCR_VERT * SCR_HORZ);
            }

            /* the first two rows are invisible */
            for (i = 0; i < SCR_VERT * SCR_HORZ; i++) {
                if (dirtybuffer[i] != 0) {
                    int x, y;
                    dirtybuffer[i] = 0;

                    x = (i % SCR_HORZ) * CHR_HORZ;
                    y = (i / SCR_HORZ) * CHR_VERT;

                    drawgfx(bitmap, Machine.gfx[0],
                            videoram.read(i) & 0x7f, 1, 0, 0, x, y,
                            Machine.drv.visible_area,
                            TRANSPARENCY_NONE, 0);
                    meadows_char_dirty.handler(x, y);
                }
            }
            /* now draw the sprites */
            meadows_draw_sprites(bitmap);

            overlay_draw(bitmap, overlay);
        }
    };

    /**
     * **********************************************************
     */
    /*                                                           */
 /* Video RAM write                                           */
 /*                                                           */
    /**
     * **********************************************************
     */
    public static WriteHandlerPtr meadows_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (offset >= videoram_size[0]) {
                return;
            }
            if (videoram.read(offset) == data) {
                return;
            }
            videoram.write(offset, data);
            dirtybuffer[offset] = 1;
        }
    };

    /**
     * **********************************************************
     */
    /* Mark sprite n covered area dirty                          */
    /**
     * **********************************************************
     */
    static void dirty_sprite(int n) {
        int x, y;

        sprite_dirty[n] = 1;

        for (y = sprite_vert[n] / CHR_VERT;
                y < (sprite_vert[n] + CHR_VERT - 1) / CHR_VERT + SPR_VERT / CHR_VERT;
                y++) {
            for (x = sprite_horz[n] / CHR_HORZ;
                    x < (sprite_horz[n] + CHR_HORZ - 1) / CHR_HORZ + SPR_HORZ / CHR_HORZ;
                    x++) {
                if (y >= 0 && y < SCR_VERT && x >= 0 && x < SCR_HORZ) {
                    dirtybuffer[y * SCR_HORZ + x] = 1;
                }
            }
        }

        //	if (errorlog != 0) fprintf(errorlog, "sprite_dirty(%d) %d %d\n", n, sprite_horz[n], sprite_vert[n]);
    }

    /**
     * **********************************************************
     */
    /* write to the sprite registers                             */
    /**
     * **********************************************************
     */
    public static WriteHandlerPtr meadows_sprite_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int n = offset % SPR_COUNT;
            switch (offset) {
                case 0:
                /* sprite 0 horz */
                case 1:
                /* sprite 1 horz */
                case 2:
                /* sprite 2 horz */
                case 3:
                    /* sprite 3 horz */
                    if (sprite_horz[n] != data) {
                        dirty_sprite(n);
                        sprite_horz[n] = data + SPR_ADJUST_X;
                    }
                    break;
                case 4:
                /* sprite 0 vert */
                case 5:
                /* sprite 1 vert */
                case 6:
                /* sprite 2 vert */
                case 7:
                    /* sprite 3 vert */
                    if (sprite_horz[n] != data) {
                        dirty_sprite(n);
                        sprite_vert[n] = data + SPR_ADJUST_Y;
                    }
                    break;
                case 8:
                /* prom 1 select + reverse shifter */
                case 9:
                /* prom 2 select + reverse shifter */
                case 10:
                /* ??? prom 3 select + reverse shifter */
                case 11:
                    /* ??? prom 4 select + reverse shifter */
                    if (sprite_index[n] != data) {
                        dirty_sprite(n);
                        sprite_index[n] = data;
                    }
                    break;
            }
        }
    };

}
