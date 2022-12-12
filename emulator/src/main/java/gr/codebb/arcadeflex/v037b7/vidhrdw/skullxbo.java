/***************************************************************************

	vidhrdw/skullxbo.c

	Functions to emulate the video hardware of the machine.

****************************************************************************

	Playfield encoding
	------------------
		1 16-bit word is used

		Word 1:
			Bit  15    = horizontal flip
			Bits 11-14 = palette
			Bits  0-12 = image index


	Motion Object encoding
	----------------------
		4 16-bit words are used

		Word 1:
			Bit  15    = horizontal flip
			Bits  0-14 = image index

		Word 2:
			Bits  7-15 = Y position
			Bits  0-3  = height in tiles (ranges from 1-16)

		Word 3:
			Bits  3-10 = link to the next image to display

		Word 4:
			Bits  6-14 = X position
			Bit   4    = use current X position + 16 for next sprite
			Bits  0-3  = palette


	Alpha layer encoding
	--------------------
		1 16-bit word is used

		Word 1:
			Bit  15    = horizontal flip
			Bit  12-14 = palette
			Bits  0-11 = image index

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.vidhrdw;
   //generic imports
import static arcadeflex.v036.generic.funcPtr.*;     
import static gr.codebb.arcadeflex.common.PtrLib.*;
import gr.codebb.arcadeflex.common.SubArrays.UShortArray;
import static arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v037b7.machine.atarigen.*;
import static gr.codebb.arcadeflex.v037b7.machine.atarigenH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.mame.drawgfxH.*;

public class skullxbo
{
	
	public static final int XCHARS  = 42;
	public static final int YCHARS  = 30;
	
	public static final int XDIM    = (XCHARS*16);
	public static final int YDIM    = (YCHARS*8);
	
	
	public static final int DEBUG_VIDEO = 0;
	
	
	
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
	 *	Statics
	 *
	 *************************************/
	
	public static atarigen_pf_state pf_state;
	
	static int[] scroll_list;
	
	static int latch_byte;
	static int mo_bank;
	
/*TODO*///	#if DEBUG_VIDEO
/*TODO*///	static UINT8 show_colors;
/*TODO*///	#endif
	
	
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
                0,                    /* reverse order */
                0
        );

        public static atarigen_pf_desc pf_desc = new atarigen_pf_desc
        (
                16, 8,				/* width/height of each tile */
                64, 64,				/* number of tiles in each direction */
                0
        );
                
	public static VhStartHandlerPtr skullxbo_vh_start = new VhStartHandlerPtr() { public int handler() 
	{
		
		/* reset statics */
		pf_state = new atarigen_pf_state();
		latch_byte = 0;
		mo_bank = 0;
	
		/* allocate the scroll list */
		scroll_list = new int[2 * YDIM];
		if (scroll_list==null)
			return 1;
	
		/* initialize the playfield */
		if (atarigen_pf_init(pf_desc) != 0)
		{
			scroll_list=null;
			return 1;
		}
	
		/* initialize the motion objects */
		if (atarigen_mo_init(mo_desc) != 0)
		{
			scroll_list=null;
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
	
	public static VhStopHandlerPtr skullxbo_vh_stop = new VhStopHandlerPtr() { public void handler() 
	{
		if (scroll_list != null)
			scroll_list = null;
	
		atarigen_pf_free();
		atarigen_mo_free();
	} };
	
	
	
	/*************************************
	 *
	 *	Video data latch
	 *
	 *************************************/
	
	public static WriteHandlerPtr skullxbo_hscroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* update the playfield state */
		pf_state.hscroll = (data >> 7) << 1;
		atarigen_pf_update(pf_state, cpu_getscanline());
	} };
	
	
	public static WriteHandlerPtr skullxbo_vscroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* adjust for the scanline we're currently on */
		int scanline = cpu_getscanline();
		if (scanline >= YDIM) scanline -= YDIM;
	
		/* update the playfield state */
		pf_state.vscroll = ((data >> 7) - scanline) & 0x1ff;
		atarigen_pf_update(pf_state, scanline);
	} };
	
	
	
	/*************************************
	 *
	 *	Motion object bank handler
	 *
	 *************************************/
	
	public static WriteHandlerPtr skullxbo_mobmsb_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		mo_bank = (offset & 0x400) * 2;
	} };
	
	
	
	/*************************************
	 *
	 *	Playfield latch write handler
	 *
	 *************************************/
	
	public static WriteHandlerPtr skullxbo_playfieldlatch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		latch_byte = data & 0xff;
	} };
	
	
	
	/*************************************
	 *
	 *	Playfield RAM write handler
	 *
	 *************************************/
	
	public static WriteHandlerPtr skullxbo_playfieldram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = atarigen_playfieldram.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword, data);
	
		/* only update if changed */
		if (oldword != newword)
		{
			atarigen_playfieldram.WRITE_WORD(offset, newword);
			atarigen_pf_dirty.write((offset & 0x1fff) / 2, 0xff);
		}
	
		/* if we're writing the low byte, also write the upper */
		if (offset < 0x2000)
			skullxbo_playfieldram_w.handler(offset + 0x2000, latch_byte);
	} };
	
	
	
	/*************************************
	 *
	 *	Periodic playfield updater
	 *
	 *************************************/
	
	public static void skullxbo_scanline_update(int scanline)
	{
		UShortArray base = new UShortArray(atarigen_alpharam, ((scanline / 8) * 64 + XCHARS) * 2);
                //base.offset=((scanline / 8) * 64 + XCHARS) * 2;
                
		int x;
	
		/* keep in range */
		if (base.offset >= (atarigen_alpharam_size[0])){
                        //System.out.println("OUT!");
			return;
                }
	
		/* update the MOs from the SLIP table */
		atarigen_mo_update_slip_512(new UBytePtr(atarigen_spriteram, mo_bank), pf_state.vscroll, scanline, new UBytePtr(atarigen_alpharam, 0xf80));
	
		/* update the current parameters */
		for (x = XCHARS; x < 64; x++)
		{
                    //if (base.offset<base.memory.length) 
                    {
			int data = base.read(0);
                        base.inc(1);
			int command = data & 0x000f;
	
			if (command == 0x0d)
			{
				/* a new vscroll latches the offset into a counter; we must adjust for this */
				int offset = scanline;
				if (offset >= YDIM) offset -= YDIM;
				pf_state.vscroll = ((data >> 7) - offset) & 0x1ff;
				atarigen_pf_update(pf_state, scanline);
			}
                    }
		}
	}
	
	
	
	/*************************************
	 *
	 *	Main refresh
	 *
	 *************************************/
	
	public static VhUpdateHandlerPtr skullxbo_vh_screenrefresh = new VhUpdateHandlerPtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int i;
	
/*TODO*///	#if DEBUG_VIDEO
/*TODO*///		debug();
/*TODO*///	#endif
	
		/* update the palette, and mark things dirty */
		if (update_palette() != null)
			memset(atarigen_pf_dirty, 0xff, atarigen_playfieldram_size[0] / 4);
	
		/* set up the all-transparent overrender palette */
		for (i = 0; i < 32; i++)
			atarigen_overrender_colortable[i] = palette_transparent_pen;
	
		/* draw the playfield */
		memset(atarigen_pf_visit, 0, 64*64);
		atarigen_pf_process(pf_render_callback, bitmap, Machine.visible_area);
	
		/* draw the motion objects */
		atarigen_mo_process(mo_render_callback, bitmap);
	
		/* draw the alphanumerics */
		{
			GfxElement gfx = Machine.gfx[2];
			int x, y, offs;
	
			for (y = 0; y < YCHARS; y++)
				for (x = 0, offs = y * 64; x < XCHARS; x++, offs++)
				{
					int data = atarigen_alpharam.READ_WORD(offs * 2);
					int code = (data & 0x7ff) ^ 0x400;
					int opaque = data & 0x8000;
					int color = (data >> 11) & 15;
					drawgfx(bitmap, gfx, code, color, 0, 0, 16 * x, 8 * y, null, opaque!=0 ? TRANSPARENCY_NONE : TRANSPARENCY_PEN, 0);
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
		int[] pf_map=new int[16], al_map=new int[16];
		int[] mo_map=new int[16];
		int[] usage;
		int i, j, x, y, offs;
	
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
		usage = Machine.gfx[2].pen_usage;
		for (y = 0; y < YCHARS; y++)
			for (x = 0, offs = y * 64; x < XCHARS; x++, offs++)
			{
				int data = atarigen_alpharam.READ_WORD(offs * 2);
				int code = (data & 0x7ff) ^ 0x400;
				int color = (data >> 11) & 15;
				al_map[color] |= usage[code];
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
		for (i = 0; i < 16; i++)
		{
			int used = mo_map[i];
			if (used != 0)
			{
				palette_used_colors.write(0x000 + i * 32 + 0, PALETTE_COLOR_TRANSPARENT);
				for (j = 1; j < 32; j++)
					if ((used & (1 << j)) != 0)
						palette_used_colors.write(0x000 + i * 32 + j, PALETTE_COLOR_USED);
			}
		}
	
		/* rebuild the alphanumerics palette */
		for (i = 0; i < 16; i++)
		{
			int used = al_map[i];
			if (used != 0)
				for (j = 0; j < 4; j++)
					if ((used & (1 << j)) != 0)
						palette_used_colors.write(0x300 + i * 4 + j, PALETTE_COLOR_USED);
		}
	
		/* recalc */
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
		int[] usage = Machine.gfx[1].pen_usage;
		int[] colormap = (int[])param;
		int x, y;
	
		/* standard loop over tiles */
		for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
			for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
			{
				int offs = x * 64 + y;
				int data = atarigen_playfieldram.READ_WORD(offs * 2);
				int data2 = atarigen_playfieldram.READ_WORD(0x2000 + offs * 2);
				int code = data & 0x7fff;
				int color = data2 & 15;
				colormap[color] |= usage[code];
	
				/* also mark unvisited tiles dirty */
				if (atarigen_pf_visit.read(offs)==0) atarigen_pf_dirty.write(offs, 0xff);
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
            public void handler(rectangle clip, rectangle tiles, atarigen_pf_state state, Object param) {
		GfxElement gfx = Machine.gfx[1];
		osd_bitmap bitmap = (osd_bitmap) param;
		int x, y;
	
		/* standard loop over tiles */
		for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
			for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
			{
				int offs = x * 64 + y;
				int data2 = atarigen_playfieldram.READ_WORD(0x2000 + offs * 2);
				int color = data2 & 15;
	
				if (atarigen_pf_dirty.read(offs) != color)
				{
					int data = atarigen_playfieldram.READ_WORD(offs * 2);
					int code = data & 0x7fff;
					int hflip = data & 0x8000;
	
					drawgfx(atarigen_pf_bitmap, gfx, code, color, hflip, 0, 16 * x, 8 * y, null, TRANSPARENCY_NONE, 0);
					atarigen_pf_dirty.write(offs, color);
	
/*TODO*///	#if DEBUG_VIDEO
/*TODO*///					if (show_colors != 0)
/*TODO*///					{
/*TODO*///						drawgfx(atarigen_pf_bitmap, Machine.uifont, "0123456789ABCDEF"[color], 1, 0, 0, 16 * x + 4, 8 * y, 0, TRANSPARENCY_PEN, 0);
/*TODO*///						drawgfx(atarigen_pf_bitmap, Machine.uifont, "0123456789ABCDEF"[color], 1, 0, 0, 16 * x + 6, 8 * y, 0, TRANSPARENCY_PEN, 0);
/*TODO*///						drawgfx(atarigen_pf_bitmap, Machine.uifont, "0123456789ABCDEF"[color], 0, 0, 0, 16 * x + 5, 8 * y, 0, TRANSPARENCY_PEN, 0);
/*TODO*///					}
/*TODO*///	#endif
				}
	
				/* track the tiles we've visited */
				atarigen_pf_visit.write(offs, 1);
			}
	
		/* then blast the result */
		x = -state.hscroll;
		y = -state.vscroll;
		copyscrollbitmap(bitmap, atarigen_pf_bitmap, 1, new int[]{x}, 1, new int[]{y}, clip, TRANSPARENCY_NONE, 0);
	
		/* also fill in the scroll list */
		for (y = clip.min_y; y <= clip.max_y; y++)
			if (y >= 0 && y < YDIM)
			{
				scroll_list[y * 2 + 0] = state.hscroll;
				scroll_list[y * 2 + 1] = state.vscroll;
			}
            }
        };
	
	
	/*************************************
	 *
	 *	Playfield overrender check
	 *
	 *************************************/
	
	static int overrender_matrix[] =
	{
		0xf000,
		0xff00,
		0x0ff0,
		0x00f0
	};
	
	static atarigen_pf_callback pf_check_overrender_callback = new atarigen_pf_callback() {
            @Override
            public void handler(rectangle clip, rectangle tiles, atarigen_pf_state state, Object param) {
		int[] usage = Machine.gfx[1].pen_usage;
		pf_overrender_data overrender_data = (pf_overrender_data) param;
		int mo_priority = overrender_data.mo_priority;
		int x, y;
	
		/* bail if we've already decided */
		if (mo_priority == -1)
			return;
	
		/* standard loop over tiles */
		for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
			for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
			{
				int offs = x * 64 + y;
				int data2 = atarigen_playfieldram.READ_WORD(0x2000 + offs * 2);
				int color = data2 & 15;
	
				if ((overrender_matrix[mo_priority] & (1 << color)) != 0)
				{
					int data = atarigen_playfieldram.READ_WORD(offs * 2);
					int code = data & 0x7fff;
	
					if ((usage[code] & 0xff00) != 0)
					{
						overrender_data.mo_priority = -1;
						return;
					}
				}
			}
            }
        };
	
	
	/*************************************
	 *
	 *	Playfield overrendering
	 *
	 *************************************/
	
	static atarigen_pf_callback pf_overrender_callback = new atarigen_pf_callback() {
            @Override
            public void handler(rectangle clip, rectangle tiles, atarigen_pf_state state, Object param) {
		pf_overrender_data overrender_data = (pf_overrender_data) param;
		osd_bitmap bitmap = overrender_data.bitmap;
		int mo_priority = overrender_data.mo_priority;
		GfxElement gfx = Machine.gfx[1];
		int x, y;
	
		/* standard loop over tiles */
		for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
		{
			int sx = (16 * x - state.hscroll) & 0x3ff;
			if (sx >= XDIM) sx -= 0x400;
	
			for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
			{
				int offs = x * 64 + y;
				int data2 = atarigen_playfieldram.READ_WORD(0x2000 + offs * 2);
				int color = data2 & 15;
				int sy = (8 * y - state.vscroll) & 0x1ff;
				if (sy >= YDIM) sy -= 0x200;
	
				if ((overrender_matrix[mo_priority] & (1 << color)) != 0)
				{
					int data = atarigen_playfieldram.READ_WORD(offs * 2);
					int code = data & 0x7fff;
					int hflip = data & 0x8000;
	
					drawgfx(bitmap, gfx, code, color, hflip, 0, sx, sy, null, TRANSPARENCY_PENS, 0x00ff);
				}
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
		int[] usage = Machine.gfx[0].pen_usage;
		int[] colormap = (int[]) param;
		int code = data.read(1) & 0x7fff;
		int color = data.read(2) & 0x000f;
		int hsize = ((data.read(3) >> 4) & 7) + 1;
		int vsize = (data.read(3) & 15) + 1;
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
		pf_overrender_data overrender_data = new pf_overrender_data();
		osd_bitmap bitmap = (osd_bitmap) param;
		rectangle pf_clip = new rectangle();
	
		/* determine the scroll offsets */
		int scroll_scanline = (clip.min_y < 0) ? 0 : (clip.min_y > YDIM) ? YDIM : clip.min_y;
		int xscroll = scroll_list[scroll_scanline * 2 + 0];
		int yscroll = scroll_list[scroll_scanline * 2 + 1];
	
		/* extract data from the various words */
		int code = data.read(1) & 0x7fff;
		int hflip = data.read(1) & 0x8000;
		int xpos = ((data.read(2) >> 7) << 1) - xscroll;
		int color = data.read(2) & 0x000f;
		int hsize = ((data.read(3) >> 4) & 7) + 1;
		int vsize = (data.read(3) & 15) + 1;
		int ypos = -(data.read(3) >> 7) - yscroll;
		int priority = (data.read(2) >> 4) & 3;
	
		/* adjust for height */
		ypos -= vsize * 8;
	
		/* adjust the final coordinates */
		xpos &= 0x3ff;
		ypos &= 0x1ff;
		if (xpos >= XDIM) xpos -= 0x400;
		if (ypos >= YDIM) ypos -= 0x200;
	
		/* determine the bounding box */
		pf_clip = atarigen_mo_compute_clip_16x8(xpos, ypos, hsize, vsize, clip);
	
		/* see if we're going to need to overrender */
		overrender_data.mo_priority = priority;
		atarigen_pf_process(pf_check_overrender_callback, overrender_data, pf_clip);
	
		/* simple case? */
		if (overrender_data.mo_priority == priority)
		{
			/* just draw -- we have dominion over all */
			atarigen_mo_draw_16x8(bitmap, gfx, code, color, hflip, 0, xpos, ypos, hsize, vsize, clip, TRANSPARENCY_PEN, 0);
		}
	
		/* otherwise, it gets a smidge trickier */
		else
		{
			/* draw an instance of the object in all transparent pens */
			atarigen_mo_draw_transparent_16x8(bitmap, gfx, code, hflip, 0, xpos, ypos, hsize, vsize, clip, TRANSPARENCY_PEN, 0);
	
			/* and then draw it normally on the temp bitmap */
			atarigen_mo_draw_16x8(atarigen_pf_overrender_bitmap, gfx, code, color, hflip, 0, xpos, ypos, hsize, vsize, clip, TRANSPARENCY_NONE, 0);
	
			/* overrender the playfield on top of that that */
			overrender_data.mo_priority = priority;
			overrender_data.bitmap = atarigen_pf_overrender_bitmap;
			atarigen_pf_process(pf_overrender_callback, overrender_data, pf_clip);
	
			/* finally, copy this chunk to the real bitmap */
			copybitmap(bitmap, atarigen_pf_overrender_bitmap, 0, 0, 0, 0, pf_clip, TRANSPARENCY_THROUGH, palette_transparent_pen);
		}
	
/*TODO*///	#if DEBUG_VIDEO
/*TODO*///		if (show_colors != 0)
/*TODO*///		{
/*TODO*///			int tx = (pf_clip.min_x + pf_clip.max_x) / 2 - 3;
/*TODO*///			int ty = (pf_clip.min_y + pf_clip.max_y) / 2 - 4;
/*TODO*///			drawgfx(bitmap, Machine.uifont, ' ', 0, 0, 0, tx - 2, ty - 2, 0, TRANSPARENCY_NONE, 0);
/*TODO*///			drawgfx(bitmap, Machine.uifont, ' ', 0, 0, 0, tx + 2, ty - 2, 0, TRANSPARENCY_NONE, 0);
/*TODO*///			drawgfx(bitmap, Machine.uifont, ' ', 0, 0, 0, tx - 2, ty + 2, 0, TRANSPARENCY_NONE, 0);
/*TODO*///			drawgfx(bitmap, Machine.uifont, ' ', 0, 0, 0, tx + 2, ty + 2, 0, TRANSPARENCY_NONE, 0);
/*TODO*///			drawgfx(bitmap, Machine.uifont, "0123456789ABCDEF"[priority], 0, 0, 0, tx, ty, 0, TRANSPARENCY_NONE, 0);
/*TODO*///		}
/*TODO*///	#endif
            }
        };
	
	
	/*************************************
	 *
	 *	Debugging
	 *
	 *************************************/
	
/*TODO*///	#if DEBUG_VIDEO
/*TODO*///	
/*TODO*///	static void mo_print(struct osd_bitmap *bitmap, struct rectangle *clip, UINT16 *data, void *param)
/*TODO*///	{
/*TODO*///		int code = data[1] & 0x7fff;
/*TODO*///		int hsize = ((data[3] >> 4) & 7) + 1;
/*TODO*///		int vsize = (data[3] & 15) + 1;
/*TODO*///		int xpos = (data[2] >> 7);
/*TODO*///		int ypos = (data[3] >> 7);
/*TODO*///		int color = data[2] & 15;
/*TODO*///		int hflip = data[1] & 0x8000;
/*TODO*///		int priority = (data[2] >> 4) & 3;
/*TODO*///	
/*TODO*///		FILE *f = (FILE *)param;
/*TODO*///		fprintf(f, "P=%04X X=%03X Y=%03X SIZE=%Xx%X COL=%X FLIP=%X PRI=%X  -- DATA=%04X %04X %04X %04X\n",
/*TODO*///				code, xpos, ypos, hsize, vsize, color, hflip >> 15, priority, data[0], data[1], data[2], data[3]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static int debug(void)
/*TODO*///	{
/*TODO*///		int hidebank = 0;
/*TODO*///		int new_show_colors;
/*TODO*///	
/*TODO*///		new_show_colors = keyboard_pressed(KEYCODE_CAPSLOCK);
/*TODO*///		if (new_show_colors != show_colors)
/*TODO*///		{
/*TODO*///			show_colors = new_show_colors;
/*TODO*///			memset(atarigen_pf_dirty, 0xff, atarigen_playfieldram_size / 4);
/*TODO*///		}
/*TODO*///	
/*TODO*///		if (keyboard_pressed(KEYCODE_Q)) hidebank = 0;
/*TODO*///		if (keyboard_pressed(KEYCODE_W)) hidebank = 1;
/*TODO*///		if (keyboard_pressed(KEYCODE_E)) hidebank = 2;
/*TODO*///		if (keyboard_pressed(KEYCODE_R)) hidebank = 3;
/*TODO*///		if (keyboard_pressed(KEYCODE_T)) hidebank = 4;
/*TODO*///		if (keyboard_pressed(KEYCODE_Y)) hidebank = 5;
/*TODO*///		if (keyboard_pressed(KEYCODE_U)) hidebank = 6;
/*TODO*///		if (keyboard_pressed(KEYCODE_I)) hidebank = 7;
/*TODO*///	
/*TODO*///		if (keyboard_pressed(KEYCODE_A)) hidebank = 8;
/*TODO*///		if (keyboard_pressed(KEYCODE_S)) hidebank = 9;
/*TODO*///		if (keyboard_pressed(KEYCODE_D)) hidebank = 10;
/*TODO*///		if (keyboard_pressed(KEYCODE_F)) hidebank = 11;
/*TODO*///		if (keyboard_pressed(KEYCODE_G)) hidebank = 12;
/*TODO*///		if (keyboard_pressed(KEYCODE_H)) hidebank = 13;
/*TODO*///		if (keyboard_pressed(KEYCODE_J)) hidebank = 14;
/*TODO*///		if (keyboard_pressed(KEYCODE_K)) hidebank = 15;
/*TODO*///	
/*TODO*///		if (keyboard_pressed(KEYCODE_9))
/*TODO*///		{
/*TODO*///			static int count;
/*TODO*///			char name[50];
/*TODO*///			FILE *f;
/*TODO*///			int i;
/*TODO*///	
/*TODO*///			while (keyboard_pressed(KEYCODE_9)) { }
/*TODO*///	
/*TODO*///			sprintf(name, "Dump %d", ++count);
/*TODO*///			f = fopen(name, "wt");
/*TODO*///	
/*TODO*///			fprintf(f, "\n\nPalette RAM:\n");
/*TODO*///	
/*TODO*///			for (i = 0x000; i < 0x800; i++)
/*TODO*///			{
/*TODO*///				fprintf(f, "%04X ", READ_WORD(&paletteram.read(i*2)));
/*TODO*///				if ((i & 15) == 15) fprintf(f, "\n");
/*TODO*///				if ((i & 255) == 255) fprintf(f, "\n");
/*TODO*///			}
/*TODO*///	
/*TODO*///			fprintf(f, "\n\nMotion Objects (drawn)\n");
/*TODO*///	/*		atarigen_mo_process(mo_print, f);*/
/*TODO*///	
/*TODO*///			fprintf(f, "\n\nMotion Objects\n");
/*TODO*///			for (i = 0; i < 0x200; i++)
/*TODO*///			{
/*TODO*///				fprintf(f, "   Object %02X:  P=%04X  Y=%04X  L=%04X  X=%04X\n",
/*TODO*///						i,
/*TODO*///						READ_WORD(&atarigen_spriteram[i*8+0]),
/*TODO*///						READ_WORD(&atarigen_spriteram[i*8+2]),
/*TODO*///						READ_WORD(&atarigen_spriteram[i*8+4]),
/*TODO*///						READ_WORD(&atarigen_spriteram[i*8+6])
/*TODO*///				);
/*TODO*///			}
/*TODO*///	
/*TODO*///			fprintf(f, "\n\nPlayfield dump\n");
/*TODO*///			for (i = 0; i < atarigen_playfieldram_size / 4; i++)
/*TODO*///			{
/*TODO*///				fprintf(f, "%01X%04X ", READ_WORD(&atarigen_playfieldram[0x2000 + i*2]), READ_WORD(&atarigen_playfieldram[i*2]));
/*TODO*///				if ((i & 63) == 63) fprintf(f, "\n");
/*TODO*///			}
/*TODO*///	
/*TODO*///			fprintf(f, "\n\nAlpha dump\n");
/*TODO*///			for (i = 0; i < atarigen_alpharam_size / 2; i++)
/*TODO*///			{
/*TODO*///				fprintf(f, "%04X ", READ_WORD(&atarigen_alpharam[i*2]));
/*TODO*///				if ((i & 63) == 63) fprintf(f, "\n");
/*TODO*///			}
/*TODO*///	
/*TODO*///			fclose(f);
/*TODO*///		}
/*TODO*///	
/*TODO*///		return hidebank;
/*TODO*///	}
/*TODO*///	
/*TODO*///	#endif
}
