/***************************************************************************

  vidhrdw/klax.c

  Functions to emulate the video hardware of the machine.

****************************************************************************

	Playfield encoding
	------------------
		1 16-bit word is used

		Word 1:
			Bits 13-15 = palette
			Bits  0-12 = image number


	Motion Object encoding
	----------------------
		4 16-bit words are used

		Word 1:
			Bits  0-7  = link to the next motion object

		Word 2:
			Bits  0-11 = image index

		Word 3:
			Bits  7-15 = horizontal position
			Bits  0-3  = motion object palette

		Word 4:
			Bits  7-15 = vertical position
			Bits  4-6  = horizontal size of the object, in tiles
			Bit   3    = horizontal flip
			Bits  0-2  = vertical size of the object, in tiles

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.vidhrdw;
 //generic imports
import static arcadeflex.v036.generic.funcPtr.*;       
import gr.codebb.arcadeflex.common.PtrLib;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.common.SubArrays.*;
import static common.libc.expressions.NOT;
import static arcadeflex.v036.mame.commonH.REGION_CPU1;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v037b7.machine.atarigen.*;
import static gr.codebb.arcadeflex.v037b7.machine.atarigenH.*;
import arcadeflex.v036.mame.drawgfxH;

public class klax
{
	
	public static final int XCHARS = 42;
	public static final int YCHARS = 30;
	
	public static int XDIM = (XCHARS*8);
	public static int YDIM = (YCHARS*8);
	
	
	/*************************************
	 *
	 *	Video system start
	 *
	 *************************************/
	public static atarigen_mo_desc mo_desc = new atarigen_mo_desc
        (
                256,                 /* maximum number of MO's */
                8,                   /* number of bytes per MO entry */
                2,                   /* number of bytes between MO words */
                0,                   /* ignore an entry if this word == 0xffff */
                0, 0, 0xff,          /* link = (data[linkword] >> linkshift) & linkmask */
                0,                    /* render in reverse link order */
                0
        );

        public static atarigen_pf_desc pf_desc = new atarigen_pf_desc
        (
                8, 8,				/* width/height of each tile */
                64, 32,				/* number of tiles in each direction */
                1					/* non-scrolling */
        );
                
	public static VhStartHandlerPtr klax_vh_start = new VhStartHandlerPtr() { public int handler() 
	{
		
		/* initialize the playfield */
		if (atarigen_pf_init(pf_desc) != 0)
			return 1;
	
		/* initialize the motion objects */
		if (atarigen_mo_init(mo_desc) != 0)
		{
			atarigen_pf_free();
			return 1;
		}
	
		return 0;
	} };
	
	
	
	/*************************************
	 *
	 *	Video system shutdown
	 *
	 *************************************/
	
	public static VhStopHandlerPtr klax_vh_stop = new VhStopHandlerPtr() { public void handler() 
	{
		atarigen_pf_free();
		atarigen_mo_free();
	} };
	
	
	
	/*************************************
	 *
	 *	Playfield RAM write handler
	 *
	 *************************************/
	
	public static WriteHandlerPtr klax_playfieldram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = atarigen_playfieldram.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword, data);
	
		if (oldword != newword)
		{
			atarigen_playfieldram.WRITE_WORD(offset, newword);
			atarigen_pf_dirty.write((offset & 0xfff) / 2, 1);
		}
	} };
	
	
	
	/*************************************
	 *
	 *	Latch write handler
	 *
	 *************************************/
	
	public static WriteHandlerPtr klax_latch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	} };
	
	
	
	/*************************************
	 *
	 *	Periodic scanline updater
	 *
	 *************************************/
	
	public static void klax_scanline_update(int scanline)
	{
		/* update the MOs from the SLIP table */
		atarigen_mo_update_slip_512(new UBytePtr(atarigen_spriteram), 0, scanline, new UBytePtr(atarigen_playfieldram, 0xf80));
	}
	
	
	
	/*************************************
	 *
	 *	Main refresh
	 *
	 *************************************/
	
	public static VhUpdateHandlerPtr klax_vh_screenrefresh = new VhUpdateHandlerPtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		/* remap if necessary */
		if (update_palette() != null)
			memset(atarigen_pf_dirty, 1, atarigen_playfieldram_size[0] / 4);
	
		/* update playfield */
		atarigen_pf_process(pf_render_callback, bitmap, Machine.visible_area);
	
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
		int[] mo_map=new int[16], pf_map=new int[16];
		int i, j;
	
		/* reset color tracking */
		memset(mo_map, 0, mo_map.length);
		memset(pf_map, 0, pf_map.length);
		palette_init_used_colors();
	
		/* update color usage for the playfield */
		atarigen_pf_process(pf_color_callback, pf_map, Machine.visible_area);
	
		/* update color usage for the mo's */
		atarigen_mo_process(mo_color_callback, mo_map);
	
		/* rebuild the playfield palette */
		for (i = 0; i < 16; i++)
		{
			int used = pf_map[i];
			if (used != 0)
				for (j = 0; j < 16; j++)
					if ((used & (1 << j)) != 0)
						palette_used_colors.write(0x100 + i * 16 + j, PALETTE_COLOR_USED);
		}
	
		/* rebuild the motion object palette */
		for (i = 0; i < 16; i++)
		{
			int used = mo_map[i];
			if (used != 0)
			{
				palette_used_colors.write(0x000 + i * 16 + 0, PALETTE_COLOR_TRANSPARENT);
				for (j = 1; j < 16; j++)
					if ((used & (1 << j)) != 0)
						palette_used_colors.write(0x000 + i * 16 + j, PALETTE_COLOR_USED);
			}
		}
	
		return palette_recalc();
	}
	
	
	
	/*************************************
	 *
	 *	Playfield palette
	 *
	 *************************************/
	
	public static atarigen_pf_callback pf_color_callback = new atarigen_pf_callback() {
            @Override
            public void handler(rectangle clip, rectangle tiles, atarigen_pf_state state, Object param) {
                int[] usage = Machine.gfx[0].pen_usage;
		int[] colormap = (int[]) param;
		int x, y;
	
		/* standard loop over tiles */
		for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
			for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 31)
			{
				int offs = x * 32 + y;
				int data1 = atarigen_playfieldram.READ_WORD(offs * 2);
				int data2 = atarigen_playfieldram.READ_WORD(offs * 2 + 0x1000);
				int code = data1 & 0x1fff;
				int color = (data2 >> 8) & 15;
	
				/* mark the colors used by this tile */
				colormap[color] |= usage[code];
			}
            }
        };
        
	
	/*************************************
	 *
	 *	Playfield rendering
	 *
	 *************************************/
	
	static atarigen_pf_callback pf_render_callback = new atarigen_pf_callback() {
            @Override
            public void handler(rectangle clip, rectangle tiles, atarigen_pf_state state, Object param) {
                GfxElement gfx = Machine.gfx[0];
		osd_bitmap bitmap = (osd_bitmap) param;
		int x, y;
	
		/* standard loop over tiles */
		for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
			for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 31)
			{
				int offs = x * 32 + y;
	
				/* update only if dirty */
				if (atarigen_pf_dirty.read(offs) != 0)
				{
					int data1 = atarigen_playfieldram.READ_WORD(offs * 2);
					int data2 = atarigen_playfieldram.READ_WORD(offs * 2 + 0x1000);
					int color = (data2 >> 8) & 15;
					int hflip = data1 & 0x8000;
					int code = data1 & 0x1fff;
	
					drawgfx(atarigen_pf_bitmap, gfx, code, color, hflip, 0, 8 * x, 8 * y, null, TRANSPARENCY_NONE, 0);
					atarigen_pf_dirty.write(offs, 0);
				}
			}
	
		/* then blast the result */
		copybitmap(bitmap, atarigen_pf_bitmap, 0, 0, 0, 0, clip, TRANSPARENCY_NONE, 0);
            }
        };
	
	
	/*************************************
	 *
	 *	Playfield overrendering
	 *
	 *************************************/
	
	public static atarigen_pf_callback pf_overrender_callback = new atarigen_pf_callback() {
            @Override
            public void handler(rectangle clip, rectangle tiles, atarigen_pf_state state, Object param) {
                GfxElement gfx = Machine.gfx[0];
		osd_bitmap bitmap = (osd_bitmap) param;
		int x, y;
	
		/* standard loop over tiles */
		for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
			for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 31)
			{
				int offs = x * 32 + y;
				int data2 = atarigen_playfieldram.READ_WORD(offs * 2 + 0x1000);
				int color = (data2 >> 8) & 15;
	
				/* overdraw if the color is 15 */
				if (color == 15)
				{
					int data1 = atarigen_playfieldram.READ_WORD(offs * 2);
					int hflip = data1 & 0x8000;
					int code = data1 & 0x1fff;
	
					drawgfx(bitmap, gfx, code, color, hflip, 0, 8 * x, 8 * y, clip, TRANSPARENCY_NONE, 0);
				}
			}
            }
        };
        
	
	/*************************************
	 *
	 *	Motion object palette
	 *
	 *************************************/
	
	static atarigen_mo_callback mo_color_callback = new atarigen_mo_callback() {
            @Override
            public void handler(UShortArray data, rectangle clip, Object param) {
                int[] usage = Machine.gfx[1].pen_usage;
		int[] colormap = (int[]) param;
		int code = data.read(1) & 0x0fff;
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
                GfxElement gfx = Machine.gfx[1];
		osd_bitmap bitmap = (osd_bitmap) param;
		rectangle pf_clip=new rectangle();
	
		/* extract data from the various words */
		int code = data.read(1) & 0x0fff;
		int xpos = data.read(2) >> 7;
		int ypos = 512 - (data.read(3) >> 7);
		int color = data.read(2) & 0x000f;
		int hsize = ((data.read(3) >> 4) & 7) + 1;
		int hflip = data.read(3) & 0x0008;
		int vsize = (data.read(3) & 7) + 1;
	
		/* adjust for height */
		ypos -= vsize * 8;
	
		/* adjust the final coordinates */
		xpos &= 0x1ff;
		ypos &= 0x1ff;
		if (xpos >= XDIM) xpos -= 0x200;
		if (ypos >= YDIM) ypos -= 0x200;
	
		/* determine the bounding box */
		pf_clip = atarigen_mo_compute_clip_8x8(xpos, ypos, hsize, vsize, clip);
	
		/* draw the motion object */
		atarigen_mo_draw_8x8(bitmap, gfx, code, color, hflip, 0, xpos, ypos, hsize, vsize, clip, TRANSPARENCY_PEN, 0);
	
		/* overrender the playfield */
		atarigen_pf_process(pf_overrender_callback, bitmap, pf_clip);
            }
        };
}        