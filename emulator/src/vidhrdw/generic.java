//copied with TODOS from original src
package vidhrdw;

import static platform.libc_old.*;
import static platform.osdepend.*;
import static mame.osdependH.*;
import static mame.driverH.*;
import static mame.mame.*;
import static platform.video.*;
import static mame.drawgfx.*;
import static mame.drawgfxH.*;
import static platform.ptrlib.*;

public class generic {

    public static UBytePtr videoram = new UBytePtr();
    public static int[] videoram_size = new int[1];
    public static UBytePtr colorram = new UBytePtr();
    public static UBytePtr spriteram = new UBytePtr();	/* not used in this module... */

    public static UBytePtr spriteram_2 = new UBytePtr();
    public static UBytePtr spriteram_3 = new UBytePtr();
    public static UBytePtr buffered_spriteram = new UBytePtr();	/* not used in this module... */
    public static UBytePtr buffered_spriteram_2 = new UBytePtr();	/* ... */
    public static int[] spriteram_size = new int[1];/* ... here just for convenience */

    public static int[] spriteram_2_size = new int[1];/* ... here just for convenience */

    public static int[] spriteram_3_size = new int[1];/* ... here just for convenience */
    /*TODO*///unsigned char *flip_screen;	/* ... */
    public static UBytePtr flip_screen_x = new UBytePtr();	/* ... */
    public static UBytePtr flip_screen_y = new UBytePtr();	/* ... */

    public static char dirtybuffer[];
    static osd_bitmap tmpbitmap;
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
	if ((tmpbitmap = osd_new_bitmap(Machine.drv.screen_width,Machine.drv.screen_height,Machine.scrbitmap.depth)) == null)
	{
		return 1;
	}

	return 0;
    }};


    /***************************************************************************

      Stop the video hardware emulation.

    ***************************************************************************/

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
    }};


    /***************************************************************************

      Draw the game screen in the given osd_bitmap.
      To be used by bitmapped games not using sprites.

    ***************************************************************************/
    public static VhUpdatePtr generic_bitmapped_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
    {

	if (full_refresh!=0)
		copybitmap(bitmap,tmpbitmap,0,0,0,0,Machine.drv.visible_area,TRANSPARENCY_NONE,0);
    }};

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
    }};
    public static WriteHandlerPtr spriteram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            spriteram.write(offset,data);
        }
    };
/*TODO*///int spriteram_2_r(int offset)
/*TODO*///{
/*TODO*///	return spriteram_2[offset];
/*TODO*///}
/*TODO*///
/*TODO*///void spriteram_2_w(int offset,int data)
/*TODO*///{
/*TODO*///	spriteram_2[offset] = data;
/*TODO*///}
/*TODO*///
    public static WriteHandlerPtr buffer_spriteram_w = new WriteHandlerPtr() {public void handler(int offset, int data) 
    {
        memcpy(buffered_spriteram,spriteram,spriteram_size[0]);
    }};

/*TODO*///
/*TODO*///void buffer_spriteram_2_w(int offset,int data)
/*TODO*///{
/*TODO*///	memcpy(buffered_spriteram_2,spriteram_2,spriteram_2_size);
/*TODO*///}
/*TODO*///
    public static void buffer_spriteram(UBytePtr ptr,int length)
    {
            memcpy(buffered_spriteram,ptr,length);
    }

    public static void buffer_spriteram_2(UBytePtr ptr,int length)
    {
            memcpy(buffered_spriteram_2,ptr,length);
    }
    
}
