/***************************************************************************

  vidhrdw/xybots.c

  Functions to emulate the video hardware of the machine.

****************************************************************************

	Playfield encoding
	------------------
		1 16-bit word is used

		Word 1:
			Bit  15    = horizontal flip
			Bits 12-14 = color
			Bits  0-11 = image


	Motion Object encoding
	----------------------
		4 16-bit words are used

		Word 1:
			Bits  0-13 = index of the image (0-16384)
			Bit  15    = horizontal flip

		Word 2:
			Bits  0-3  = priority

		Word 3:
			Bits  0-2  = height of the sprite / 8 (ranges from 1-8)
			Bits  7-14 = Y position of the sprite

		Word 4:
			Bits  0-3  = image palette
			Bits  7-14 = X position of the sprite


	Alpha layer encoding
	--------------------
		1 16-bit word is used

		Word 1:
			Bit  15    = transparent/opaque
			Bit  10-13 = color
			Bits  0-9  = index of the character

***************************************************************************/


/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.vidhrdw;
  //generic imports
import static arcadeflex.v036.generic.funcPtr.*;      
import arcadeflex.v036.generic.funcPtr.TimerCallbackHandlerPtr;
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
import gr.codebb.arcadeflex.v037b7.machine.atarigenH;
import static arcadeflex.v036.mame.drawgfxH.*;
import arcadeflex.v036.mame.drawgfxH;

public class xybots
{
	
	public static final int XCHARS  = 42;
	public static final int YCHARS  = 30;
	
	public static final int XDIM    = (XCHARS*8);
	public static final int YDIM    = (YCHARS*8);
	
	
	
	/*************************************
	 *
	 *	Structures
	 *
	 *************************************/
	
	public static class pf_overrender_data
	{
		public osd_bitmap bitmap;
		public int mo_priority;
	};
	
	
	/*************************************
	 *
	 *	Video system start
	 *
	 *************************************/
	public static atarigen_mo_desc mo_desc = new atarigen_mo_desc
        (
                64,                  /* maximum number of MO's */
                8,                   /* number of bytes per MO entry */
                2,                   /* number of bytes between MO words */
                0,                   /* ignore an entry if this word == 0xffff */
                -1, 0, 0x3f,         /* link = (data[linkword] >> linkshift) & linkmask */
                0,                    /* render in reverse link order */
                0
        );

        public static atarigen_pf_desc pf_desc = new atarigen_pf_desc
        (
                8, 8,				/* width/height of each tile */
                64, 64,				/* number of tiles in each direction */
                1					/* non-scrolling */
        );
        
	public static VhStartHandlerPtr xybots_vh_start = new VhStartHandlerPtr() { public int handler() 
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
	
	public static VhStopHandlerPtr xybots_vh_stop = new VhStopHandlerPtr() { public void handler() 
	{
		atarigen_pf_free();
		atarigen_mo_free();
	} };
	
	
	
	/*************************************
	 *
	 *	Playfield RAM write handler
	 *
	 *************************************/
	
	public static WriteHandlerPtr xybots_playfieldram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = atarigen_playfieldram.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword, data);
	
		if (oldword != newword)
		{
			atarigen_playfieldram.WRITE_WORD(offset, newword);
			atarigen_pf_dirty.write(offset / 2, 1);
		}
	} };
	
	
	
	/*************************************
	 *
	 *	Periodic scanline updater
	 *
	 *************************************/
	
	public static TimerCallbackHandlerPtr xybots_scanline_update = new TimerCallbackHandlerPtr() {
            @Override
            public void handler(int scanline) {
                if (scanline < YDIM)
			atarigen_mo_update(atarigen_spriteram, 0, scanline);
            }
        };
        
	
	/*************************************
	 *
	 *	Main refresh
	 *
	 *************************************/
	
	public static VhUpdateHandlerPtr xybots_vh_screenrefresh = new VhUpdateHandlerPtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int i;
	
		/* update the palette */
		if (update_palette() != null)
			memset(atarigen_pf_dirty, 1, atarigen_playfieldram_size[0] / 2);
	
		/* set up the all-transparent overrender palette */
		for (i = 0; i < 16; i++)
			atarigen_overrender_colortable[i] = palette_transparent_pen;
	
		/* render the playfield */
		atarigen_pf_process(pf_render_callback, bitmap, Machine.visible_area);
	
		/* render the motion objects */
		atarigen_mo_process(mo_render_callback, bitmap);
	
		/* redraw the alpha layer completely */
		{
			GfxElement gfx = Machine.gfx[2];
			int sx, sy, offs;
	
			for (sy = 0; sy < YCHARS; sy++)
				for (sx = 0, offs = sy * 64; sx < XCHARS; sx++, offs++)
				{
					int data = atarigen_alpharam.READ_WORD(offs * 2);
					int code = data & 0x3ff;
					int opaque = data & 0x8000;
	
					if (code!=0 || opaque!=0)
					{
						int color = (data >> 12) & 7;
	
						drawgfx(bitmap, gfx, code, color, 0, 0, 8 * sx, 8 * sy, null,
								opaque!=0 ? TRANSPARENCY_NONE : TRANSPARENCY_PEN, 0);
					}
				}
		}
	
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
		int[] mo_map=new int[48], al_map=new int[8], pf_map=new int[16];
		int i, j;
	
		/* reset color tracking */
		memset(mo_map, 0, mo_map.length);
		memset(pf_map, 0, pf_map.length);
		memset(al_map, 0, al_map.length);
		palette_init_used_colors();
	
		/* update color usage for the playfield */
		atarigen_pf_process(pf_color_callback, pf_map, Machine.visible_area);
	
		/* update color usage for the mo's */
		atarigen_mo_process(mo_color_callback, mo_map);
	
		/* update color usage for the alphanumerics */
		{
			int[] usage = Machine.gfx[2].pen_usage;
			int sx, sy, offs;
	
			for (sy = 0; sy < YCHARS; sy++)
				for (sx = 0, offs = sy * 64; sx < XCHARS; sx++, offs++)
				{
					int data = atarigen_alpharam.READ_WORD(offs * 2);
					int color = (data >> 12) & 7;
					int code = data & 0x3ff;
					al_map[color] |= usage[code];
				}
		}
	
		/* rebuild the playfield palette */
		for (i = 0; i < 16; i++)
		{
			int used = pf_map[i];
			if (used != 0)
				for (j = 0; j < 16; j++)
					if ((used & (1 << j)) != 0)
						palette_used_colors.write(0x200 + i * 16 + j, PALETTE_COLOR_USED);
		}
	
		/* rebuild the motion object palette */
		for (i = 0; i < 48; i++)
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
	
		/* rebuild the alphanumerics palette */
		for (i = 0; i < 8; i++)
		{
			int used = al_map[i];
			if (used != 0)
				for (j = 0; j < 4; j++)
					if ((used & (1 << j)) != 0)
						palette_used_colors.write(0x000 + i * 4 + j, PALETTE_COLOR_USED);
		}
	
		return palette_recalc();
	}
	
	
	
	/*************************************
	 *
	 *	Playfield palette
	 *
	 *************************************/
	
	static atarigen_pf_callback pf_color_callback = new atarigen_pf_callback() {
            @Override
            public void handler(drawgfxH.rectangle clip, drawgfxH.rectangle tiles, atarigenH.atarigen_pf_state state, Object param) {
                int[] usage = Machine.gfx[0].pen_usage;
		int[] colormap = (int[]) param;
		int x, y;
	
		/* standard loop over tiles */
		for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
			for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
			{
				int offs = y * 64 + x;
				int data = atarigen_playfieldram.READ_WORD(offs * 2);
				int color = (data >> 11) & 15;
				int code = data & 0x1fff;
	
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
	
	public static atarigen_pf_callback pf_render_callback = new atarigen_pf_callback() {
            @Override
            public void handler(drawgfxH.rectangle clip, drawgfxH.rectangle tiles, atarigenH.atarigen_pf_state state, Object param) {
                GfxElement gfx = Machine.gfx[0];
		osd_bitmap bitmap = (osd_bitmap) param;
		int x, y;
	
		/* standard loop over tiles */
		for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
			for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
			{
				int offs = y * 64 + x;
	
				/* update only if dirty */
				if (atarigen_pf_dirty.read(offs) != 0)
				{
					int data = atarigen_playfieldram.READ_WORD(offs * 2);
					int color = (data >> 11) & 15;
					int hflip = data & 0x8000;
					int code = data & 0x1fff;
	
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
	 *	Playfield overrender check
	 *
	 *************************************/
	
	public static atarigen_pf_callback pf_check_overrender_callback = new atarigen_pf_callback() {
            @Override
            public void handler(rectangle clip, rectangle tiles, atarigen_pf_state state, Object param) {
                pf_overrender_data overrender_data = (pf_overrender_data) param;
		int mo_priority = overrender_data.mo_priority;
		int x, y;
	
		/* if we've already decided, bail */
		if (mo_priority == -1)
			return;
	
		/* standard loop over tiles */
		for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
			for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
			{
				int offs = y * 64 + x;
				int data = atarigen_playfieldram.READ_WORD(offs * 2);
				int color = (data >> 11) & 15;
	
				/* this is the priority equation from the schematics */
				if (mo_priority > color)
				{
					overrender_data.mo_priority = -1;
					return;
				}
			}
            }
        };
        	
	/*************************************
	 *
	 *	Playfield overrendering
	 *
	 *************************************/
	
	public static atarigen_pf_callback pf_overrender_callback = new atarigen_pf_callback() {
            @Override
            public void handler(drawgfxH.rectangle clip, drawgfxH.rectangle tiles, atarigenH.atarigen_pf_state state, Object param) {
                pf_overrender_data overrender_data = (pf_overrender_data) param;
		GfxElement gfx = Machine.gfx[0];
		osd_bitmap bitmap = overrender_data.bitmap;
		int mo_priority = overrender_data.mo_priority;
		int x, y;
	
		/* standard loop over tiles */
		for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
			for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
			{
				int offs = y * 64 + x;
				int data = atarigen_playfieldram.READ_WORD(offs * 2);
				int color = (data >> 11) & 15;
	
				/* this is the priority equation from the schematics */
				if (mo_priority > color)
				{
					int hflip = data & 0x8000;
					int code = data & 0x1fff;
	
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
	
	public static atarigen_mo_callback mo_color_callback = new atarigen_mo_callback() {
            @Override
            public void handler(UShortArray data, rectangle clip, Object param) {
                int[] usage = Machine.gfx[1].pen_usage;
		int[] colormap = (int[]) param;
		int code = data.read(0) & 0x3fff;
		int color = data.read(3) & 7;
		int vsize = (data.read(2) & 7) + 1;
		int temp = 0;
		int i;
	
		/* sneaky -- an extra color bank is hidden after the playfield palette */
		if ((data.read(3) & 8)!=0) color = 0x20 + (color ^ 7);
	
		for (i = 0; i < vsize; i++)
			temp |= usage[code++];
		colormap[color] |= temp;
            }
        };
        	
	/*************************************
	 *
	 *	Motion object rendering
	 *
	 *************************************/
	
	public static atarigen_mo_callback mo_render_callback = new atarigen_mo_callback() {
            @Override
            public void handler(UShortArray data, rectangle clip, Object param) {
                GfxElement gfx = Machine.gfx[1];
		pf_overrender_data overrender_data=new pf_overrender_data();
		osd_bitmap bitmap = (osd_bitmap) param;
		rectangle pf_clip=new rectangle();
	
		/* extract data from the various words */
		int code = data.read(0) & 0x3fff;
		int hflip = data.read(0) & 0x8000;
		int priority = ~data.read(1) & 15;
		int vsize = (data.read(2) & 7) + 1;
		int ypos = -(data.read(2) >> 7);
		int color = data.read(3) & 7;
		int xpos = data.read(3) >> 7;
	
		/* sneaky -- an extra color bank is hidden after the playfield palette */
		if ((data.read(3) & 8)!=0) color = 0x20 + (color ^ 7);
	
		/* adjust for the height */
		ypos -= vsize * 8;
	
		/* adjust the final coordinates */
		xpos &= 0x1ff;
		ypos &= 0x1ff;
		if (xpos >= XDIM) xpos -= 0x200;
		if (ypos >= YDIM) ypos -= 0x200;
	
		/* clip the X coordinate */
		if (xpos <= -8 || xpos >= XDIM)
			return;
	
		/* determine the bounding box */
		pf_clip = atarigen_mo_compute_clip_8x8(xpos, ypos, 1, vsize, clip);
	
		/* see if we need to overrender */
		overrender_data.mo_priority = priority;
		atarigen_pf_process(pf_check_overrender_callback, overrender_data, pf_clip);
	
		/* if not, do it the easy way */
		if (overrender_data.mo_priority == priority)
		{
			atarigen_mo_draw_8x8_strip(bitmap, gfx, code, color, hflip, 0, xpos, ypos, vsize, clip, TRANSPARENCY_PEN, 0);
		}
	
		/* otherwise, make it tricky */
		else
		{
			/* draw an instance of the object in all transparent pens */
			atarigen_mo_draw_transparent_8x8_strip(bitmap, gfx, code, hflip, 0, xpos, ypos, vsize, clip, TRANSPARENCY_PEN, 0);
	
			/* and then draw it normally on the temp bitmap */
			atarigen_mo_draw_8x8_strip(atarigen_pf_overrender_bitmap, gfx, code, color, hflip, 0, xpos, ypos, vsize, clip, TRANSPARENCY_NONE, 0);
	
			/* overrender the playfield on top of that that */
			overrender_data.mo_priority = priority;
			overrender_data.bitmap = atarigen_pf_overrender_bitmap;
			atarigen_pf_process(pf_overrender_callback, overrender_data, pf_clip);
	
			/* finally, copy this chunk to the real bitmap */
			copybitmap(bitmap, atarigen_pf_overrender_bitmap, 0, 0, 0, 0, pf_clip, TRANSPARENCY_THROUGH, palette_transparent_pen);
		}
            }
        };
        
}
