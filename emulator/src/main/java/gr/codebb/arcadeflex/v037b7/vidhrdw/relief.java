/***************************************************************************

  vidhrdw/relief.c

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

import arcadeflex.v036.generic.funcPtr.TimerCallbackHandlerPtr;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import gr.codebb.arcadeflex.common.SubArrays.UShortArray;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v037b7.machine.atarigen.*;
import static gr.codebb.arcadeflex.v037b7.machine.atarigenH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import gr.codebb.arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.*;
import static common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import gr.codebb.arcadeflex.v037b7.mame.drawgfxH;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;

public class relief
{
	
	public static final int XCHARS  = 42;
	public static final int YCHARS  = 30;
	
	public static final int XDIM    = (XCHARS*8);
	public static final int YDIM    = (YCHARS*8);
	
	
/*TODO*///	#define DEBUG_VIDEO 0
	
	
	
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
	public static atarigen_pf_state pf2_state;
	
/*TODO*///	#if DEBUG_VIDEO
/*TODO*///	static int show_colors;
/*TODO*///	static int special_pen;
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Prototypes
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static const UINT8 *update_palette(void);
/*TODO*///	
/*TODO*///	static void pf_color_callback(const struct rectangle *clip, const struct rectangle *tiles, const struct atarigen_pf_state *state, void *data);
/*TODO*///	static void pf2_color_callback(const struct rectangle *clip, const struct rectangle *tiles, const struct atarigen_pf_state *state, void *data);
/*TODO*///	static void pf_render_callback(const struct rectangle *clip, const struct rectangle *tiles, const struct atarigen_pf_state *state, void *data);
/*TODO*///	static void pf2_render_callback(const struct rectangle *clip, const struct rectangle *tiles, const struct atarigen_pf_state *state, void *data);
/*TODO*///	static void pf2_overrender_callback(const struct rectangle *clip, const struct rectangle *tiles, const struct atarigen_pf_state *state, void *data);
/*TODO*///	
/*TODO*///	static void mo_color_callback(const UINT16 *data, const struct rectangle *clip, void *param);
/*TODO*///	static void mo_render_callback(const UINT16 *data, const struct rectangle *clip, void *param);
/*TODO*///	
/*TODO*///	#if DEBUG_VIDEO
/*TODO*///	static #endif
	
	
	
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
                8, 8,				/* width/height of each tile */
                64, 64,				/* number of tiles in each direction */
                0
        );
                
	public static VhStartPtr relief_vh_start = new VhStartPtr() { public int handler() 
	{
		
		/* reset statics */
		pf_state = new atarigen_pf_state();
		pf2_state = new atarigen_pf_state();
		
		/* initialize the playfield */
		if (atarigen_pf_init(pf_desc) != 0)
			return 1;
		
		/* initialize the second playfield */
		if (atarigen_pf2_init(pf_desc) != 0)
		{
			atarigen_pf_free();
			return 1;
		}
		
		/* initialize the motion objects */
		if (atarigen_mo_init(mo_desc) != 0)
		{
			atarigen_pf2_free();
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
	
	public static VhStopPtr relief_vh_stop = new VhStopPtr() { public void handler() 
	{
		atarigen_pf2_free();
		atarigen_pf_free();
		atarigen_mo_free();
	} };
	
	
	
	/*************************************
	 *
	 *	Playfield RAM write handlers
	 *
	 *************************************/
	
	public static WriteHandlerPtr relief_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = atarigen_playfieldram_color.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword, data);
	
		/* only update if changed */
		if (oldword != newword)
		{
			atarigen_playfieldram_color.WRITE_WORD(offset, newword);
			
			oldword ^= newword;
			
			/* low byte affects pf1 */
			if ((oldword & 0x00ff) != 0)
				atarigen_pf_dirty.write((offset / 2) & 0xfff, 1);
	
			/* upper byte affects pf2 */
			if ((oldword & 0xff00) != 0)
				atarigen_pf2_dirty.write((offset / 2) & 0xfff, 1);
		}
	} };
	
	
	public static WriteHandlerPtr relief_playfieldram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = atarigen_playfieldram.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword, data);
	
		/* only update if changed */
		if (oldword != newword)
		{
			atarigen_playfieldram.WRITE_WORD(offset, newword);
			atarigen_pf_dirty.write((offset / 2) & 0xfff, 1);
		}
		
		/* handle the latch, but only write the lower byte */
		if (atarigen_video_control_state.latch2 != -1)
			relief_colorram_w.handler(offset, atarigen_video_control_state.latch2 | 0xff000000);
	} };
	
	
	public static WriteHandlerPtr relief_playfield2ram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = atarigen_playfield2ram.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword, data);
	
		/* only update if changed */
		if (oldword != newword)
		{
			atarigen_playfield2ram.WRITE_WORD(offset, newword);
			atarigen_pf2_dirty.write((offset / 2) & 0xfff, 1);
		}
		
		/* handle the latch, but only write the upper byte */
		if (atarigen_video_control_state.latch1 != -1)
			relief_colorram_w.handler(offset, atarigen_video_control_state.latch1 | 0x00ff0000);
	} };
	
	
	
	/*************************************
	 *
	 *	Periodic scanline updater
	 *
	 *************************************/
	
	public static TimerCallbackHandlerPtr relief_scanline_update = new TimerCallbackHandlerPtr() {
            @Override
            public void handler(int scanline) {
                /* update the playfield */
		if (scanline == 0)
		{
			atarigen_video_control_update(new UBytePtr(atarigen_alpharam, 0xf00));
	
			/* copy in the scroll values */
			pf_state.hscroll = atarigen_video_control_state.pf1_xscroll + (atarigen_video_control_state.pf2_xscroll & 7);
			pf_state.vscroll = atarigen_video_control_state.pf1_yscroll;
			pf2_state.hscroll = atarigen_video_control_state.pf2_xscroll + 4;
			pf2_state.vscroll = atarigen_video_control_state.pf2_yscroll;
			
			/* update the two playfields */
			atarigen_pf_update(pf_state, scanline);
			atarigen_pf2_update(pf2_state, scanline);
		}
	
		/* update the MOs from the SLIP table */
		atarigen_mo_update_slip_512(atarigen_spriteram, atarigen_video_control_state.sprite_yscroll, scanline, new UBytePtr(atarigen_alpharam, 0xf80));
            }
        };
	
	
	/*************************************
	 *
	 *	Main refresh
	 *
	 *************************************/
	
	public static VhUpdatePtr relief_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int i;
		
/*TODO*///	#if DEBUG_VIDEO
/*TODO*///		debug();
/*TODO*///	#endif
	
		/* update the palette */
		if (update_palette() != null)
		{
			memset(atarigen_pf_dirty, 1, atarigen_playfieldram_size[0] / 2);
			memset(atarigen_pf2_dirty, 1, atarigen_playfield2ram_size[0] / 2);
		}
	
		/* set up the all-transparent overrender palette */
		for (i = 0; i < 16; i++)
			atarigen_overrender_colortable[i] = palette_transparent_pen;
	
		/* render the playfield */
		memset(atarigen_pf_visit, 0, 64*64);
/*TODO*///	#if DEBUG_VIDEO
/*TODO*///		if (show_colors == 2)
/*TODO*///			osd_clearbitmap(bitmap);
/*TODO*///		else
/*TODO*///	#endif
		atarigen_pf_process(pf_render_callback, bitmap, Machine.visible_area);
	
		/* render the playfield */
		memset(atarigen_pf2_visit, 0, 64*64);
/*TODO*///	#if DEBUG_VIDEO
/*TODO*///		if (show_colors != 1)
/*TODO*///	#endif
		atarigen_pf2_process(pf2_render_callback, bitmap, Machine.visible_area);
	
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
		int[] mo_map=new int[16], pf_map=new int[48];
		int i, j;
	
		/* reset color tracking */
		memset(mo_map, 0, mo_map.length);
		memset(pf_map, 0, pf_map.length);
		palette_init_used_colors();
		
		/* update color usage for the playfields */
		atarigen_pf_process(pf_color_callback, pf_map, Machine.visible_area);
		atarigen_pf2_process(pf2_color_callback, pf_map, Machine.visible_area);
	
		/* update color usage for the mo's */
		atarigen_mo_process(mo_color_callback, mo_map);
	
		/* rebuild the playfield palettes */
		for (i = 0; i < 48; i++)
		{
			int used = pf_map[i];
			if (used != 0)
			{
				if (i < 16)
					palette_used_colors.write(0x000 + i * 16 + 0, PALETTE_COLOR_TRANSPARENT);
				else if ((used & 0x0001) != 0)
					palette_used_colors.write(0x000 + i * 16 + 0, PALETTE_COLOR_USED);
				for (j = 1; j < 16; j++)
					if ((used & (1 << j)) != 0)
						palette_used_colors.write(0x000 + i * 16 + j, PALETTE_COLOR_USED);
			}
		}
	
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
	 *	Playfield palette
	 *
	 *************************************/
	
	static atarigen_pf_callback pf_color_callback = new atarigen_pf_callback() {
            @Override
            public void handler(rectangle clip, rectangle tiles, atarigen_pf_state state, Object param) {
                int[] usage = Machine.gfx[0].pen_usage;
		int[] colormap = (int[]) param;
		int x, y;
		
		/* standard loop over tiles */
		for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
			for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
			{
				int offs = x * 64 + y;
				int data1 = atarigen_playfieldram.READ_WORD(offs * 2);
				int data2 = atarigen_playfieldram_color.READ_WORD(offs * 2);
				int code = data1 & 0x7fff;
				int color = 0x20 + (data2 & 0x0f);
	
				/* mark the colors used by this tile */
				colormap[color] |= usage[code];
				
				/* also mark unvisited tiles dirty */
				if (atarigen_pf_visit.read(offs)==0) atarigen_pf_dirty.write(offs, 1);
			}
            }
        };
        
	
	static atarigen_pf_callback pf2_color_callback = new atarigen_pf_callback() {
            @Override
            public void handler(rectangle clip, rectangle tiles, atarigen_pf_state state, Object param) {
		int[] usage = Machine.gfx[0].pen_usage;
		int[] colormap = (int[]) param;
		int x, y;
		
		/* standard loop over tiles */
		for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
			for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
			{
				int offs = x * 64 + y;
				int data1 = atarigen_playfield2ram.READ_WORD(offs * 2);
				int data2 = atarigen_playfieldram_color.READ_WORD(offs * 2);
				int code = data1 & 0x7fff;
				int color = (data2 >> 8) & 0x0f;
	
				/* mark the colors used by this tile */
				colormap[color] |= usage[code];
				
				/* also mark unvisited tiles dirty */
				if (atarigen_pf2_visit.read(offs) == 0) atarigen_pf2_dirty.write(offs, 1);
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
			for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
			{
				int offs = x * 64 + y;
				
				/* update only if dirty */
				if (atarigen_pf_dirty.read(offs) != 0)
				{
					int data1 = atarigen_playfieldram.READ_WORD(offs * 2);
					int data2 = atarigen_playfieldram_color.READ_WORD(offs * 2);
					int color = 0x20 + (data2 & 0x0f);
					int code = data1 & 0x7fff;
					int hflip = data1 & 0x8000;
					
					drawgfx(atarigen_pf_bitmap, gfx, code, color, hflip, 0, 8 * x, 8 * y, null, TRANSPARENCY_NONE, 0);
					atarigen_pf_dirty.write(offs, 0);
	
/*TODO*///	#if DEBUG_VIDEO
/*TODO*///					if (show_colors == 1)
/*TODO*///					{
/*TODO*///						drawgfx(atarigen_pf_bitmap, Machine.uifont, "0123456789ABCDEF"[color - 0x20], 1, 0, 0, 8 * x + 0, 8 * y, 0, TRANSPARENCY_PEN, 0);
/*TODO*///						drawgfx(atarigen_pf_bitmap, Machine.uifont, "0123456789ABCDEF"[color - 0x20], 1, 0, 0, 8 * x + 2, 8 * y, 0, TRANSPARENCY_PEN, 0);
/*TODO*///						drawgfx(atarigen_pf_bitmap, Machine.uifont, "0123456789ABCDEF"[color - 0x20], 0, 0, 0, 8 * x + 1, 8 * y, 0, TRANSPARENCY_PEN, 0);
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
            }
        };
        
	
	static atarigen_pf_callback pf2_render_callback = new atarigen_pf_callback() {
            @Override
            public void handler(rectangle clip, rectangle tiles, atarigen_pf_state state, Object param) {
                GfxElement gfx = Machine.gfx[0];
		osd_bitmap bitmap = (osd_bitmap) param;
		int x, y;
	
		/* standard loop over tiles */
		for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
			for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
			{
				int offs = x * 64 + y;
				
				/* update only if dirty */
				if (atarigen_pf2_dirty.read(offs) != 0)
				{
					int data1 = atarigen_playfield2ram.READ_WORD(offs * 2);
					int data2 = atarigen_playfieldram_color.READ_WORD(offs * 2);
					int color = (data2 >> 8) & 0x0f;
					int code = data1 & 0x7fff;
					int hflip = data1 & 0x8000;
					
					drawgfx(atarigen_pf2_bitmap, gfx, code, color, hflip, 0, 8 * x, 8 * y, null, TRANSPARENCY_NONE, 0);
					atarigen_pf2_dirty.write(offs, 0);
	
/*TODO*///	#if DEBUG_VIDEO
/*TODO*///					if (show_colors == 2)
/*TODO*///					{
/*TODO*///						drawgfx(atarigen_pf2_bitmap, Machine.uifont, "0123456789ABCDEF"[color], 1, 0, 0, 8 * x + 0, 8 * y, 0, TRANSPARENCY_PEN, 0);
/*TODO*///						drawgfx(atarigen_pf2_bitmap, Machine.uifont, "0123456789ABCDEF"[color], 1, 0, 0, 8 * x + 2, 8 * y, 0, TRANSPARENCY_PEN, 0);
/*TODO*///						drawgfx(atarigen_pf2_bitmap, Machine.uifont, "0123456789ABCDEF"[color], 0, 0, 0, 8 * x + 1, 8 * y, 0, TRANSPARENCY_PEN, 0);
/*TODO*///					}
/*TODO*///	#endif
				}
				
				/* track the tiles we've visited */
				atarigen_pf2_visit.write(offs, 1);
			}
	
		/* then blast the result */
		x = -state.hscroll;
		y = -state.vscroll;
		copyscrollbitmap(bitmap, atarigen_pf2_bitmap, 1, new int[]{x}, 1, new int[]{y}, clip, TRANSPARENCY_PEN, palette_transparent_pen);
            }
        };
	
	
	/*************************************
	 *
	 *	Playfield overrendering
	 *
	 *************************************/
	
	static atarigen_pf_callback pf2_overrender_callback = new atarigen_pf_callback() {
            @Override
            public void handler(rectangle clip, rectangle tiles, atarigen_pf_state state, Object param) {
                pf_overrender_data overrender_data = (pf_overrender_data) param;
		osd_bitmap bitmap = overrender_data.bitmap;
		int min_color = overrender_data.mo_priority!=0 ? 1 : 0;
		GfxElement gfx = Machine.gfx[0];
		int x, y;
	
		/* standard loop over tiles */
		for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
		{
			int sx = (8 * x - state.hscroll) & 0x1ff;
			if (sx >= XDIM) sx -= 0x200;
	
			for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
			{
				int offs = x * 64 + y;
				int data2 = atarigen_playfieldram_color.READ_WORD(offs * 2);
				int color = (data2 >> 8) & 0x0f;
				
				if (color >= min_color)
				{
					int data1 = atarigen_playfield2ram.READ_WORD(offs * 2);
					int code = data1 & 0x7fff;
					int hflip = data1 & 0x8000;
	
					int sy = (8 * y - state.vscroll) & 0x1ff;
					if (sy >= YDIM) sy -= 0x200;
				
					drawgfx(bitmap, gfx, code, color, hflip, 0, sx, sy, clip, TRANSPARENCY_PEN, 0);
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
	 
	static atarigen_mo_callback mo_color_callback = new atarigen_mo_callback() {
            @Override
            public void handler(UShortArray data, drawgfxH.rectangle clip, Object param) {
                int[] usage = Machine.gfx[1].pen_usage;
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
                GfxElement gfx = Machine.gfx[1];
		pf_overrender_data overrender_data = new pf_overrender_data();
		osd_bitmap bitmap = (osd_bitmap) param;
		rectangle pf_clip=new rectangle();
	
		/* extract data from the various words */
		int hflip = data.read(1) & 0x8000;
		int code = data.read(1) & 0x7fff;
		int xpos = (data.read(2) >> 7) - atarigen_video_control_state.sprite_xscroll;
		int priority = (data.read(2) >> 3) & 1;
		int color = data.read(2) & 0x000f;
		int ypos = -(data.read(3) >> 7) - atarigen_video_control_state.sprite_yscroll;
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
		pf_clip = atarigen_mo_compute_clip_8x8(xpos, ypos, hsize, vsize, clip);
	
		/* draw an instance of the object in all transparent pens */
		atarigen_mo_draw_transparent_8x8(bitmap, gfx, code, hflip, 0, xpos, ypos, hsize, vsize, clip, TRANSPARENCY_PEN, 0);
	
		/* and then draw it normally on the temp bitmap */
		atarigen_mo_draw_8x8(atarigen_pf_overrender_bitmap, gfx, code, color, hflip, 0, xpos, ypos, hsize, vsize, clip, TRANSPARENCY_NONE, 0);
	
		/* overrender the playfield on top of that that */
		overrender_data.mo_priority = priority;
		overrender_data.bitmap = atarigen_pf_overrender_bitmap;
		atarigen_pf2_process(pf2_overrender_callback, overrender_data, pf_clip);
	
		/* finally, copy this chunk to the real bitmap */
		copybitmap(bitmap, atarigen_pf_overrender_bitmap, 0, 0, 0, 0, pf_clip, TRANSPARENCY_THROUGH, palette_transparent_pen);
	
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
	
	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Debugging
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	 
/*TODO*///	#if DEBUG_VIDEO
/*TODO*///	
/*TODO*///	static void debug(void)
/*TODO*///	{
/*TODO*///		int new_show_colors;
/*TODO*///		
/*TODO*///		new_show_colors = (keyboard_pressed(KEYCODE_LSHIFT)) ? 1 : keyboard_pressed(KEYCODE_RSHIFT) ? 2 : 0;
/*TODO*///		if (new_show_colors != show_colors)
/*TODO*///		{
/*TODO*///			show_colors = new_show_colors;
/*TODO*///			memset(atarigen_pf_dirty, 0xff, atarigen_playfieldram_size / 2);
/*TODO*///			memset(atarigen_pf2_dirty, 0xff, atarigen_playfieldram_size / 2);
/*TODO*///		}
/*TODO*///	
/*TODO*///		special_pen = -1;
/*TODO*///		if (keyboard_pressed(KEYCODE_Q)) special_pen = 0;
/*TODO*///		if (keyboard_pressed(KEYCODE_W)) special_pen = 1;
/*TODO*///		if (keyboard_pressed(KEYCODE_E)) special_pen = 2;
/*TODO*///		if (keyboard_pressed(KEYCODE_R)) special_pen = 3;
/*TODO*///		if (keyboard_pressed(KEYCODE_T)) special_pen = 4;
/*TODO*///		if (keyboard_pressed(KEYCODE_Y)) special_pen = 5;
/*TODO*///		if (keyboard_pressed(KEYCODE_U)) special_pen = 6;
/*TODO*///		if (keyboard_pressed(KEYCODE_I)) special_pen = 7;
/*TODO*///	
/*TODO*///		if (keyboard_pressed(KEYCODE_A)) special_pen = 8;
/*TODO*///		if (keyboard_pressed(KEYCODE_S)) special_pen = 9;
/*TODO*///		if (keyboard_pressed(KEYCODE_D)) special_pen = 10;
/*TODO*///		if (keyboard_pressed(KEYCODE_F)) special_pen = 11;
/*TODO*///		if (keyboard_pressed(KEYCODE_G)) special_pen = 12;
/*TODO*///		if (keyboard_pressed(KEYCODE_H)) special_pen = 13;
/*TODO*///		if (keyboard_pressed(KEYCODE_J)) special_pen = 14;
/*TODO*///		if (keyboard_pressed(KEYCODE_K)) special_pen = 15;
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
/*TODO*///			fprintf(f, "\n\nAlpha Palette:\n");
/*TODO*///			for (i = 0x000; i < 0x100; i++)
/*TODO*///			{
/*TODO*///				fprintf(f, "%04X ", READ_WORD(&paletteram.read(i*2)));
/*TODO*///				if ((i & 15) == 15) fprintf(f, "\n");
/*TODO*///			}
/*TODO*///	
/*TODO*///			fprintf(f, "\n\nMotion Object Palette:\n");
/*TODO*///			for (i = 0x100; i < 0x200; i++)
/*TODO*///			{
/*TODO*///				fprintf(f, "%04X ", READ_WORD(&paletteram.read(i*2)));
/*TODO*///				if ((i & 15) == 15) fprintf(f, "\n");
/*TODO*///			}
/*TODO*///	
/*TODO*///			fprintf(f, "\n\nPlayfield Palette:\n");
/*TODO*///			for (i = 0x200; i < 0x400; i++)
/*TODO*///			{
/*TODO*///				fprintf(f, "%04X ", READ_WORD(&paletteram.read(i*2)));
/*TODO*///				if ((i & 15) == 15) fprintf(f, "\n");
/*TODO*///			}
/*TODO*///	
/*TODO*///			fprintf(f, "\n\nMotion Object Config:\n");
/*TODO*///			for (i = 0x00; i < 0x40; i++)
/*TODO*///			{
/*TODO*///				fprintf(f, "%04X ", READ_WORD(&atarigen_playfieldram[0xf00 + i*2]));
/*TODO*///				if ((i & 15) == 15) fprintf(f, "\n");
/*TODO*///			}
/*TODO*///	
/*TODO*///			fprintf(f, "\n\nMotion Object SLIPs:\n");
/*TODO*///			for (i = 0x00; i < 0x40; i++)
/*TODO*///			{
/*TODO*///				fprintf(f, "%04X ", READ_WORD(&atarigen_playfieldram[0xf80 + i*2]));
/*TODO*///				if ((i & 15) == 15) fprintf(f, "\n");
/*TODO*///			}
/*TODO*///	
/*TODO*///			fprintf(f, "\n\nMotion Objects\n");
/*TODO*///			for (i = 0; i < 0x400; i++)
/*TODO*///			{
/*TODO*///				UINT16 *data = (UINT16 *)&atarigen_spriteram[i*8];
/*TODO*///				int code = data[1] & 0x7fff;
/*TODO*///				int hsize = ((data[3] >> 4) & 7) + 1;
/*TODO*///				int vsize = (data[3] & 7) + 1;
/*TODO*///				int xpos = (data[2] >> 7);
/*TODO*///				int ypos = (data[3] >> 7) - vsize * 8;
/*TODO*///				int color = data[2] & 15;
/*TODO*///				int hflip = data[3] & 0x0008;
/*TODO*///				fprintf(f, "   Object %03X: L=%03X P=%04X C=%X X=%03X Y=%03X W=%d H=%d F=%d LEFT=(%04X %04X %04X %04X)\n",
/*TODO*///						i, data[0] & 0x3ff, code, color, xpos & 0x1ff, ypos & 0x1ff, hsize, vsize, hflip,
/*TODO*///						data[0] & 0xfc00, data[1] & 0x0000, data[2] & 0x0070, data[3] & 0x0000);
/*TODO*///			}
/*TODO*///	
/*TODO*///			fprintf(f, "\n\nPlayfield 1 dump\n");
/*TODO*///			for (i = 0; i < atarigen_playfieldram_size / 2; i++)
/*TODO*///			{
/*TODO*///				fprintf(f, "%X%04X ", READ_WORD(&atarigen_playfieldram_color[i*2]) & 0xff, READ_WORD(&atarigen_playfieldram[i*2]));
/*TODO*///				if ((i & 63) == 63) fprintf(f, "\n");
/*TODO*///			}
/*TODO*///	
/*TODO*///			fprintf(f, "\n\nPlayfield 2 dump\n");
/*TODO*///			for (i = 0; i < atarigen_playfield2ram_size / 2; i++)
/*TODO*///			{
/*TODO*///				fprintf(f, "%X%04X ", (READ_WORD(&atarigen_playfieldram_color[i*2]) >> 8) & 0xff, READ_WORD(&atarigen_playfield2ram[i*2]));
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
/*TODO*///	}
/*TODO*///	
/*TODO*///	#endif
}
