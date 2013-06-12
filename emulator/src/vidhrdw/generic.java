//copied with TODOS from original src
package vidhrdw;

import static arcadeflex.libc_old.*;
import static arcadeflex.osdepend.*;
import static mame.osdependH.*;
import static mame.driverH.*;
import static mame.mame.*;
import static arcadeflex.video.*;

public class generic {

    public static CharPtr videoram = new CharPtr();
    public static int[] videoram_size = new int[1];
    public static CharPtr colorram = new CharPtr();
    public static CharPtr spriteram = new CharPtr();	/* not used in this module... */

    public static CharPtr spriteram_2 = new CharPtr();
    public static CharPtr spriteram_3 = new CharPtr();
    /*TODO*///unsigned char *buffered_spriteram;	/* not used in this module... */
/*TODO*///unsigned char *buffered_spriteram_2;	/* ... */
    public static int[] spriteram_size = new int[1];/* ... here just for convenience */

    public static int[] spriteram_2_size = new int[1];/* ... here just for convenience */

    public static int[] spriteram_3_size = new int[1];/* ... here just for convenience */
    /*TODO*///unsigned char *flip_screen;	/* ... */
/*TODO*///unsigned char *flip_screen_x;	/* ... */
/*TODO*///unsigned char *flip_screen_y;	/* ... */

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
    /*TODO*///
/*TODO*///
/*TODO*///int generic_bitmapped_vh_start(void)
/*TODO*///{
/*TODO*///	if ((tmpbitmap = osd_new_bitmap(Machine->drv->screen_width,Machine->drv->screen_height,Machine->scrbitmap->depth)) == 0)
/*TODO*///	{
/*TODO*///		return 1;
/*TODO*///	}
/*TODO*///
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Stop the video hardware emulation.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///void generic_vh_stop(void)
    public static VhStopPtr generic_vh_stop = new VhStopPtr() {
        public void handler() {
            dirtybuffer = null;
            osd_free_bitmap(tmpbitmap);
            tmpbitmap = null;
        }
    };
    /*TODO*///
/*TODO*///void generic_bitmapped_vh_stop(void)
/*TODO*///{
/*TODO*///	osd_free_bitmap(tmpbitmap);
/*TODO*///
/*TODO*///	tmpbitmap = 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Draw the game screen in the given osd_bitmap.
/*TODO*///  To be used by bitmapped games not using sprites.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///void generic_bitmapped_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
/*TODO*///{
/*TODO*///	if (full_refresh)
/*TODO*///		copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);
/*TODO*///}
/*TODO*///
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
    /*TODO*///int spriteram_r(int offset)
/*TODO*///{
/*TODO*///	return spriteram[offset];
/*TODO*///}
/*TODO*///
/*TODO*///void spriteram_w(int offset,int data)
/*TODO*///{
/*TODO*///	spriteram[offset] = data;
/*TODO*///}
/*TODO*///
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
/*TODO*///void buffer_spriteram_w(int offset,int data)
/*TODO*///{
/*TODO*///	memcpy(buffered_spriteram,spriteram,spriteram_size);
/*TODO*///}
/*TODO*///
/*TODO*///void buffer_spriteram_2_w(int offset,int data)
/*TODO*///{
/*TODO*///	memcpy(buffered_spriteram_2,spriteram_2,spriteram_2_size);
/*TODO*///}
/*TODO*///
/*TODO*///void buffer_spriteram(unsigned char *ptr,int length)
/*TODO*///{
/*TODO*///	memcpy(buffered_spriteram,ptr,length);
/*TODO*///}
/*TODO*///
/*TODO*///void buffer_spriteram_2(unsigned char *ptr,int length)
/*TODO*///{
/*TODO*///	memcpy(buffered_spriteram_2,ptr,length);
/*TODO*///}
/*TODO*///    
}
