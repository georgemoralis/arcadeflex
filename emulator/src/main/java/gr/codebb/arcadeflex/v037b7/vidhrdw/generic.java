/*
 * ported to v0.37b7
 * 
 */
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.*;

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
                logerror("Error: generic_vh_start() called but videoram_size not initialized\n");
                return 1;
            }
            if ((dirtybuffer = new char[videoram_size[0]]) == null) {
                return 1;
            }
            memset(dirtybuffer, 1, videoram_size[0]);

            if ((tmpbitmap = bitmap_alloc(Machine.drv.screen_width, Machine.drv.screen_height)) == null) {
                dirtybuffer = null;
                return 1;
            }

            return 0;
        }
    };

    public static VhStartPtr generic_bitmapped_vh_start = new VhStartPtr() {
        public int handler() {
            if ((tmpbitmap = bitmap_alloc(Machine.drv.screen_width, Machine.drv.screen_height)) == null) {
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
            bitmap_free(tmpbitmap);

            dirtybuffer = null;
            tmpbitmap = null;
        }
    };

    public static VhStopPtr generic_bitmapped_vh_stop = new VhStopPtr() {
        public void handler() {
            bitmap_free(tmpbitmap);

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
                copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.visible_area, TRANSPARENCY_NONE, 0);
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

    /* Mish:  171099

            'Buffered spriteram' is where the graphics hardware draws the sprites
    from private ram that the main CPU cannot access.  The main CPU typically
    prepares sprites for the next frame in it's own sprite ram as the graphics
    hardware renders sprites for the current frame from private ram.  Main CPU
    sprite ram is usually copied across to private ram by setting some flag
    in the VBL interrupt routine.

            The reason for this is to avoid sprite flicker or lag - if a game
    is unable to prepare sprite ram within a frame (for example, lots of sprites
    on screen) then it doesn't trigger the buffering hardware - instead the
    graphics hardware will use the sprites from the last frame. An example is
    Dark Seal - the buffer flag is only written to if the CPU is idle at the time
    of the VBL interrupt.  If the buffering is not emulated the sprites flicker
    at busy scenes.

            Some games seem to use buffering because of hardware constraints -
    Capcom games (Cps1, Last Duel, etc) render spriteram _1 frame ahead_ and
    buffer this spriteram at the end of a frame, so the _next_ frame must be drawn
    from the buffer.  Presumably the graphics hardware and the main cpu cannot
    share the same spriteram for whatever reason.

            Sprite buffering & Mame:

            To use sprite buffering in a driver use VIDEO_BUFFERS_SPRITERAM in the
    machine driver.  This will automatically create an area for buffered spriteram
    equal to the size of normal spriteram.

            Spriteram size _must_ be declared in the memory map:

            { 0x120000, 0x1207ff, MWA_BANK2, &spriteram, &spriteram_size },

            Then the video driver must draw the sprites from the buffered_spriteram
    pointer.  The function buffer_spriteram_w() is used to simulate hardware
    which buffers the spriteram from a memory location write.  The function
    buffer_spriteram(unsigned char *ptr, int length) can be used where
    more control is needed over what is buffered.

            For examples see darkseal.c, contra.c, lastduel.c, bionicc.c etc.

     */
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
