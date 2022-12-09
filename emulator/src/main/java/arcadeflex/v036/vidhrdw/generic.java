/*
 * ported to v0.36
 * 
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.osdependH.*;
//common imports
import static common.libc.cstring.*;
//TODO
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.mame.errorlog;
import static gr.codebb.arcadeflex.v036.platform.video.osd_free_bitmap;
import static gr.codebb.arcadeflex.v036.platform.video.osd_new_bitmap;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;

public class generic {

    public static UBytePtr videoram = new UBytePtr();
    public static int[] videoram_size = new int[1];
    public static UBytePtr colorram = new UBytePtr();
    public static UBytePtr spriteram = new UBytePtr();/* not used in this module... */

    public static UBytePtr spriteram_2 = new UBytePtr();
    public static UBytePtr spriteram_3 = new UBytePtr();
    public static UBytePtr buffered_spriteram = new UBytePtr();/* not used in this module... */
    public static UBytePtr buffered_spriteram_2 = new UBytePtr();/* ... */
    public static int[] spriteram_size = new int[1];/* ... here just for convenience */

    public static int[] spriteram_2_size = new int[1];/* ... here just for convenience */

    public static int[] spriteram_3_size = new int[1];/* ... here just for convenience */
    public static UBytePtr flip_screen = new UBytePtr();/* ... */
    public static UBytePtr flip_screen_x = new UBytePtr();/* ... */
    public static UBytePtr flip_screen_y = new UBytePtr();/* ... */

    public static char dirtybuffer[];
    public static osd_bitmap tmpbitmap;
    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartPtr generic_vh_start = new VhStartPtr() {
        public int handler() {
            dirtybuffer = null;
            tmpbitmap = null;
            if (videoram_size[0] == 0) {
                if (errorlog != null) {
                    fprintf(errorlog, "Error: generic_vh_start() called but videoram_size not initialized\n");
                }
                return 1;
            }

            if ((dirtybuffer = new char[videoram_size[0]]) == null) {
                return 1;
            }

            memset(dirtybuffer, 1, videoram_size[0]);

            if ((tmpbitmap = osd_new_bitmap(Machine.drv.screen_width, Machine.drv.screen_height, Machine.scrbitmap.depth)) == null) {
                dirtybuffer = null;
                return 1;
            }

            return 0;
        }
    };

    public static VhStartPtr generic_bitmapped_vh_start = new VhStartPtr() {
        public int handler() {
            if ((tmpbitmap = osd_new_bitmap(Machine.drv.screen_width, Machine.drv.screen_height, Machine.scrbitmap.depth)) == null) {
                return 1;
            }

            return 0;
        }
    };

    /**
     * *************************************************************************
     * Stop the video hardware emulation.
     * *************************************************************************
     */
    public static VhStopPtr generic_vh_stop = new VhStopPtr() {
        public void handler() {
            dirtybuffer = null;
            osd_free_bitmap(tmpbitmap);
            tmpbitmap = null;
        }
    };
    public static VhStopPtr generic_bitmapped_vh_stop = new VhStopPtr() {
        public void handler() {

            osd_free_bitmap(tmpbitmap);

            tmpbitmap = null;
        }
    };

    /**
     * *************************************************************************
     * Draw the game screen in the given osd_bitmap. To be used by bitmapped
     * games not using sprites.
     * *************************************************************************
     */
    public static VhUpdatePtr generic_bitmapped_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {

            if (full_refresh != 0) {
                copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
            }
        }
    };

    public static ReadHandlerPtr videoram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return videoram.read(offset);
        }
    };
    public static ReadHandlerPtr colorram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return colorram.read(offset);
        }
    };
    public static WriteHandlerPtr videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (videoram.read(offset) != data) {
                dirtybuffer[offset] = 1;

                videoram.write(offset, data);
            }
        }
    };
    public static WriteHandlerPtr colorram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (colorram.read(offset) != data) {
                dirtybuffer[offset] = 1;

                colorram.write(offset, data);
            }
        }
    };
    public static ReadHandlerPtr spriteram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return spriteram.read(offset);
        }
    };
    public static WriteHandlerPtr spriteram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            spriteram.write(offset, data);
        }
    };

    public static ReadHandlerPtr spriteram_2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return spriteram_2.read(offset);
        }
    };

    public static WriteHandlerPtr spriteram_2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            spriteram_2.write(offset, data);
        }
    };

    public static WriteHandlerPtr buffer_spriteram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            memcpy(buffered_spriteram, spriteram, spriteram_size[0]);
        }
    };

    public static WriteHandlerPtr buffer_spriteram_2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            memcpy(buffered_spriteram_2, spriteram_2, spriteram_2_size[0]);
        }
    };

    public static void buffer_spriteram(UBytePtr ptr, int length) {
        memcpy(buffered_spriteram, ptr, length);
    }

    public static void buffer_spriteram_2(UBytePtr ptr, int length) {
        memcpy(buffered_spriteram_2, ptr, length);
    }

}
