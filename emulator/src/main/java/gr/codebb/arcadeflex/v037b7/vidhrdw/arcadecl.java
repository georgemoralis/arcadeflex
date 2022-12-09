/***************************************************************************

	Atari Arcade Classics hardware (prototypes)

	Note: this video hardware has some similarities to Shuuz & company
	The sprite offset registers are stored to 3EFF80

****************************************************************************/


/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.vidhrdw;
 //generic imports
import static arcadeflex.v036.generic.funcPtr.*;       
import static gr.codebb.arcadeflex.common.PtrLib.*;
import gr.codebb.arcadeflex.common.SubArrays.UShortArray;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v037b7.machine.atarigen.*;
import static gr.codebb.arcadeflex.v037b7.machine.atarigenH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import arcadeflex.v036.mame.drawgfxH;
import static arcadeflex.v036.mame.drawgfxH.*;

public class arcadecl
{
	
	public static final int XCHARS  = 43;
	public static final int YCHARS  = 30;
	
	public static final int XDIM    = (XCHARS*8);
	public static final int YDIM    = (YCHARS*8);
	
	
	
	/*************************************
	 *
	 *	Statics
	 *
	 *************************************/
	
	static int[] color_usage;
	
	
	
	/*************************************
	 *
	 *	Prototypes
	 *
	 *************************************/
	
/*TODO*///	static const UINT8 *update_palette(void);
	
/*TODO*///	static void mo_color_callback(const UINT16 *data, const struct rectangle *clip, void *param);
/*TODO*///	static void mo_render_callback(const UINT16 *data, const struct rectangle *clip, void *param);
	
	
	
	/*************************************
	 *
	 *	Video system start
	 *
	 *************************************/
	static atarigen_mo_desc mo_desc = new atarigen_mo_desc
        (
                256,                 /* maximum number of MO's */
                8,                   /* number of bytes per MO entry */
                2,                   /* number of bytes between MO words */
                0,                   /* ignore an entry if this word == 0xffff */
                0, 0, 0xff,          /* link = (data[linkword] >> linkshift) & linkmask */
                0,                    /* render in reverse link order */
                0
        );

        static atarigen_pf_desc pf_desc = new atarigen_pf_desc
        (
                8, 8,				/* width/height of each tile */
                64, 64,				/* number of tiles in each direction */
                1					/* non-scrolling */
        );
                
	public static VhStartHandlerPtr arcadecl_vh_start = new VhStartHandlerPtr() { public int handler() 
	{
		
		/* allocate color usage */
		color_usage = new int[256];
		if (color_usage==null)
			return 1;
		color_usage[0] = XDIM * YDIM;
		memset(atarigen_playfieldram, 0, 0x20000);
	
		/* initialize the playfield */
		if (atarigen_pf_init(pf_desc) != 0)
		{
			color_usage=null;
			return 1;
		}
	
		/* initialize the motion objects */
		if (atarigen_mo_init(mo_desc) != 0)
		{
			atarigen_pf_free();
			color_usage=null;
			return 1;
		}
	
		return 0;
	} };
	
	
	
	/*************************************
	 *
	 *	Video system shutdown
	 *
	 *************************************/
	
	public static VhStopHandlerPtr arcadecl_vh_stop = new VhStopHandlerPtr() { public void handler() 
	{
		/* free data */
		if (color_usage != null)
			color_usage=null;
			
		atarigen_pf_free();
		atarigen_mo_free();
	} };
	
	
	
	/*************************************
	 *
	 *	Playfield RAM write handler
	 *
	 *************************************/
	
	public static WriteHandlerPtr arcadecl_playfieldram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = atarigen_playfieldram.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword, data);
		int x, y;
	
		if (oldword != newword)
		{
			atarigen_playfieldram.WRITE_WORD(offset, newword);
	
			/* track color usage */
			x = offset % 512;
			y = offset / 512;
			if (x < XDIM && y < YDIM)
			{
				color_usage[(oldword >> 8) & 0xff]--;
				color_usage[oldword & 0xff]--;
				color_usage[(newword >> 8) & 0xff]++;
				color_usage[newword & 0xff]++;
			}
	
			/* mark scanlines dirty */
			atarigen_pf_dirty.write(y, 1);
		}
	} };
	
	
	
	/*************************************
	 *
	 *	Periodic scanline updater
	 *
	 *************************************/
	
	public static void arcadecl_scanline_update(int scanline)
	{
		/* doesn't appear to use SLIPs */
		if (scanline < YDIM)
			atarigen_mo_update(atarigen_spriteram, 0, scanline);
	}
	
	
	
	/*************************************
	 *
	 *	Main refresh
	 *
	 *************************************/
	
	public static VhUpdateHandlerPtr arcadecl_vh_screenrefresh = new VhUpdateHandlerPtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		/* remap if necessary */
		if (update_palette() != null)
			memset(atarigen_pf_dirty, 1, YDIM);
	
		/* update the cached bitmap */
		{
			int x, y;
	
			for (y = 0; y < YDIM; y++)
				if (atarigen_pf_dirty.read(y) != 0)
				{
					int xx = 0;
					UBytePtr src = new UBytePtr(atarigen_playfieldram, 512 * y);
	
					/* regenerate the line */
					for (x = 0; x < XDIM/2; x++)
					{
						int bits = src.READ_WORD(0);
						src.inc( 2 );
						plot_pixel.handler(atarigen_pf_bitmap, xx++, y, Machine.pens[bits >> 8]);
						plot_pixel.handler(atarigen_pf_bitmap, xx++, y, Machine.pens[bits & 0xff]);
					}
					atarigen_pf_dirty.write(y, 0);
				}
		}
	
		/* copy the cached bitmap */
		copybitmap(bitmap, atarigen_pf_bitmap, 0, 0, 0, 0, null, TRANSPARENCY_NONE, 0);
	
		/* render the motion objects */
		atarigen_mo_process(mo_render_callback, bitmap);
	
		/* update onscreen messages */
		atarigen_update_messages();
	} };
	
	
	/*************************************
	 *
	 *	Palette management
	 *
	 *************************************/
	
	static UBytePtr update_palette()
	{
		int[] mo_map=new int[16];
		int i, j;
	
		/* reset color tracking */
		memset(mo_map, 0, mo_map.length);
		palette_init_used_colors();
	
		/* update color usage for the mo's */
		atarigen_mo_process(mo_color_callback, mo_map);
	
		/* rebuild the playfield palette */
		for (i = 0; i < 256; i++)
			if (color_usage[i] != 0)
				palette_used_colors.write(0x000 + i, PALETTE_COLOR_USED);
	
		/* rebuild the motion object palette */
		for (i = 0; i < 16; i++)
		{
			int used = mo_map[i];
			if (used != 0)
			{
				palette_used_colors.write(0x100 + i * 16 + 0, PALETTE_COLOR_TRANSPARENT);
				for (j = 1; j < 16; j++)
					if ((used & (1 << j)) != 0)
						palette_used_colors.write(0x100 + i * 16 + j, PALETTE_COLOR_USED);
			}
		}
	
		return palette_recalc();
	}
	
	
	
	/*************************************
	 *
	 *	Motion object palette
	 *
	 *************************************/
	
	static atarigen_mo_callback mo_color_callback = new atarigen_mo_callback() {
            @Override
            public void handler(UShortArray data, drawgfxH.rectangle clip, Object param) {
                int[] usage = Machine.gfx[0].pen_usage;
		int[] colormap = (int[]) param;
		int code = data.read(1) & 0x7fff;
		int color = data.read(2) & 0x000f;
		int hsize = ((data.read(3) >> 4) & 7) + 1;
		int vsize = (data.read(3) & 7) + 1;
		int tiles = hsize * vsize;
		int temp = 0;
		int i;
	
		for (i = 0; i < tiles; i++)
			temp |= usage[code++];
		colormap[color] |= temp;
            }
        };
        	
	
	/*************************************
	 *
	 *	Motion object rendering
	 *
	 *************************************/
	
	static atarigen_mo_callback mo_render_callback = new atarigen_mo_callback() {
            @Override
            public void handler(UShortArray data, rectangle clip, Object param) {
                GfxElement gfx = Machine.gfx[0];
		osd_bitmap bitmap = (osd_bitmap) param;
		rectangle pf_clip;
	
		/* extract data from the various words */
		int hflip = data.read(1) & 0x8000;
		int code = data.read(1) & 0x7fff;
		int xpos = (data.read(2) >> 7) + 4;
		int color = data.read(2) & 0x000f;
	/*	int priority = (data.read(2) >> 3) & 1;*/
		int ypos = YDIM - (data.read(3) >> 7);
		int hsize = ((data.read(3) >> 4) & 7) + 1;
		int vsize = (data.read(3) & 7) + 1;
	
		/* adjust for height */
		ypos -= vsize * 8;
	
		/* adjust the final coordinates */
		xpos &= 0x1ff;
		ypos &= 0x1ff;
		if (xpos >= XDIM) xpos -= 0x200;
		if (ypos >= YDIM) ypos -= 0x200;
	
		/* determine the bounding box */
		pf_clip=atarigen_mo_compute_clip_8x8(xpos, ypos, hsize, vsize, clip);
	
		/* draw the motion object */
		atarigen_mo_draw_8x8(bitmap, gfx, code, color, hflip, 0, xpos, ypos, hsize, vsize, clip, TRANSPARENCY_PEN, 0);
            }
        };

}
