/***************************************************************************

	Atari Gauntlet hardware

****************************************************************************/


/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.vidhrdw;
        
import static gr.codebb.arcadeflex.common.PtrLib.*;
import gr.codebb.arcadeflex.common.SubArrays.UShortArray;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v037b7.machine.atarigen.*;
import static gr.codebb.arcadeflex.v037b7.machine.atarigenH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;
import gr.codebb.arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.*;
import static gr.codebb.arcadeflex.common.libc.cstring.*;
import static gr.codebb.arcadeflex.v037b7.machine.atarigenH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import gr.codebb.arcadeflex.v037b7.mame.timer.timer_callback;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;

public class gauntlet
{
	
	public static final int XCHARS  = 42;
	public static final int YCHARS  = 30;
	
	public static final int XDIM    = (XCHARS*8);
	public static final int YDIM    = (YCHARS*8);
	
	
	
	/*************************************
	 *
	 *	Globals we own
	 *
	 *************************************/
	
	public static int  vindctr2_screen_refresh;
	
	
	
	/*************************************
	 *
	 *	Statics
	 *
	 *************************************/
	
	public static class mo_data
	{
		public osd_bitmap bitmap;
		public int color_xor;
	};
	
	public static atarigen_pf_state pf_state;
	
	static int playfield_color_base;
	
        
	/*************************************
	 *
	 *	Prototypes
	 *
	 *************************************/
	
/*TODO*///	static const UINT8 *update_palette(void);
	
/*TODO*///	static void pf_color_callback(const struct rectangle *clip, const struct rectangle *tiles, const struct atarigen_pf_state *state, void *data);
/*TODO*///	static void pf_render_callback(const struct rectangle *clip, const struct rectangle *tiles, const struct atarigen_pf_state *state, void *data);
/*TODO*///	static void pf_overrender_callback(const struct rectangle *clip, const struct rectangle *tiles, const struct atarigen_pf_state *state, void *param);
	
/*TODO*///	static void mo_color_callback(const UINT16 *data, const struct rectangle *clip, void *param);
/*TODO*///	static void mo_render_callback(const UINT16 *data, const struct rectangle *clip, void *param);
	
	
	
	/*************************************
	 *
	 *	Video system start
	 *
	 *************************************/
	static atarigen_mo_desc mo_desc = new atarigen_mo_desc
        (
                1024,                /* maximum number of MO's */
                2,                   /* number of bytes per MO entry */
                0x800,               /* number of bytes between MO words */
                3,                   /* ignore an entry if this word == 0xffff */
                3, 0, 0x3ff,         /* link = (data[linkword] >> linkshift) & linkmask */
                0,                    /* render in reverse link order */
                0
        );

        static atarigen_pf_desc pf_desc = new atarigen_pf_desc
        (
                8, 8,				/* width/height of each tile */
                64, 64,				/* number of tiles in each direction */
                0
        );
                
	public static VhStartPtr gauntlet_vh_start = new VhStartPtr() { public int handler() 
	{
		/* reset statics */
		//memset(&pf_state, 0, sizeof(pf_state));
                pf_state = new atarigen_pf_state();
		playfield_color_base = vindctr2_screen_refresh!=0 ? 0x10 : 0x18;
	
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
	
	public static VhStopPtr gauntlet_vh_stop = new VhStopPtr() { public void handler() 
	{
		atarigen_pf_free();
		atarigen_mo_free();
	} };
	
	
	
	/*************************************
	 *
	 *	Horizontal scroll register
	 *
	 *************************************/
	
	public static WriteHandlerPtr gauntlet_hscroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* update memory */
		int oldword = atarigen_hscroll.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword, data);
		atarigen_hscroll.WRITE_WORD(offset, newword);
	
		/* update parameters */
		pf_state.hscroll = newword & 0x1ff;
		atarigen_pf_update(pf_state, cpu_getscanline());
	} };
	
	
	
	/*************************************
	 *
	 *	Vertical scroll/PF bank register
	 *
	 *************************************/
	
	public static WriteHandlerPtr gauntlet_vscroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* update memory */
		int oldword = atarigen_vscroll.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword, data);
		atarigen_vscroll.WRITE_WORD(offset, newword);
	
		/* update parameters */
		pf_state.vscroll = (newword >> 7) & 0x1ff;
		pf_state.param[0] = newword & 3;
		atarigen_pf_update(pf_state, cpu_getscanline());
	} };
	
	
	
	/*************************************
	 *
	 *	Playfield RAM write handler
	 *
	 *************************************/
	
	public static WriteHandlerPtr gauntlet_playfieldram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = atarigen_playfieldram.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword, data);
		if (oldword != newword)
		{
			atarigen_playfieldram.WRITE_WORD(offset, newword);
			atarigen_pf_dirty.write(offset / 2, 0xff);
		}
	} };
	
	
	
	/*************************************
	 *
	 *	Periodic scanline updater
	 *
	 *************************************/
	
	void gauntlet_scanline_update(int scanline)
	{
		atarigen_mo_update_slip_512(atarigen_spriteram, pf_state.vscroll, scanline, new UBytePtr(atarigen_alpharam, 0xf80));
	}
	
	
	
	/*************************************
	 *
	 *	Main refresh
	 *
	 *************************************/
/*TODO*///	
/*TODO*///	public static VhUpdatePtr gauntlet_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
/*TODO*///	{
/*TODO*///		/* update the palette, and mark things dirty */
/*TODO*///		if (update_palette())
/*TODO*///			memset(atarigen_pf_dirty, 0xff, atarigen_playfieldram_size / 2);
/*TODO*///	
/*TODO*///		/* draw the playfield */
/*TODO*///		memset(atarigen_pf_visit, 0, 64*64);
/*TODO*///		atarigen_pf_process(pf_render_callback, bitmap, &Machine.visible_area);
/*TODO*///	
/*TODO*///		/* draw the motion objects */
/*TODO*///		atarigen_mo_process(mo_render_callback, bitmap);
/*TODO*///	
/*TODO*///		/* draw the alphanumerics */
/*TODO*///		{
/*TODO*///			const struct GfxElement *gfx = Machine.gfx[1];
/*TODO*///			int x, y, offs;
/*TODO*///	
/*TODO*///			for (y = 0; y < YCHARS; y++)
/*TODO*///				for (x = 0, offs = y * 64; x < XCHARS; x++, offs++)
/*TODO*///				{
/*TODO*///					int data = READ_WORD(&atarigen_alpharam[offs * 2]);
/*TODO*///					int code = data & 0x3ff;
/*TODO*///					int opaque = data & 0x8000;
/*TODO*///	
/*TODO*///					if (code || opaque)
/*TODO*///					{
/*TODO*///						int color = ((data >> 10) & 0xf) | ((data >> 9) & 0x20);
/*TODO*///						drawgfx(bitmap, gfx, code, color, 0, 0, 8 * x, 8 * y, 0, opaque ? TRANSPARENCY_NONE : TRANSPARENCY_PEN, 0);
/*TODO*///					}
/*TODO*///				}
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* update onscreen messages */
/*TODO*///		atarigen_update_messages();
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Palette management
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static const UINT8 *update_palette(void)
/*TODO*///	{
/*TODO*///		UINT16 pf_map[32], al_map[64], mo_map[16];
/*TODO*///		int i, j;
/*TODO*///	
/*TODO*///		/* reset color tracking */
/*TODO*///		memset(mo_map, 0, sizeof(mo_map));
/*TODO*///		memset(pf_map, 0, sizeof(pf_map));
/*TODO*///		memset(al_map, 0, sizeof(al_map));
/*TODO*///		palette_init_used_colors();
/*TODO*///	
/*TODO*///		/* update color usage for the playfield */
/*TODO*///		atarigen_pf_process(pf_color_callback, pf_map, &Machine.visible_area);
/*TODO*///	
/*TODO*///		/* update color usage for the mo's */
/*TODO*///		atarigen_mo_process(mo_color_callback, mo_map);
/*TODO*///	
/*TODO*///		/* update color usage for the alphanumerics */
/*TODO*///		{
/*TODO*///			const unsigned int *usage = Machine.gfx[1].pen_usage;
/*TODO*///			int x, y, offs;
/*TODO*///	
/*TODO*///			for (y = 0; y < YCHARS; y++)
/*TODO*///				for (x = 0, offs = y * 64; x < XCHARS; x++, offs++)
/*TODO*///				{
/*TODO*///					int data = READ_WORD(&atarigen_alpharam[offs * 2]);
/*TODO*///					int code = data & 0x3ff;
/*TODO*///					int color = ((data >> 10) & 0xf) | ((data >> 9) & 0x20);
/*TODO*///					al_map[color] |= usage[code];
/*TODO*///				}
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* rebuild the playfield palette */
/*TODO*///		for (i = 0; i < 16; i++)
/*TODO*///		{
/*TODO*///			UINT16 used = pf_map[i + 16];
/*TODO*///			if (used != 0)
/*TODO*///				for (j = 0; j < 16; j++)
/*TODO*///					if (used & (1 << j))
/*TODO*///						palette_used_colors[0x200 + i * 16 + j] = PALETTE_COLOR_USED;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* rebuild the motion object palette */
/*TODO*///		for (i = 0; i < 16; i++)
/*TODO*///		{
/*TODO*///			UINT16 used = mo_map[i];
/*TODO*///			if (used != 0)
/*TODO*///			{
/*TODO*///				palette_used_colors[0x100 + i * 16 + 0] = PALETTE_COLOR_TRANSPARENT;
/*TODO*///				palette_used_colors[0x100 + i * 16 + 1] = PALETTE_COLOR_TRANSPARENT;
/*TODO*///				for (j = 2; j < 16; j++)
/*TODO*///					if (used & (1 << j))
/*TODO*///						palette_used_colors[0x100 + i * 16 + j] = PALETTE_COLOR_USED;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* rebuild the alphanumerics palette */
/*TODO*///		for (i = 0; i < 64; i++)
/*TODO*///		{
/*TODO*///			UINT16 used = al_map[i];
/*TODO*///			if (used != 0)
/*TODO*///				for (j = 0; j < 4; j++)
/*TODO*///					if (used & (1 << j))
/*TODO*///						palette_used_colors[0x000 + i * 4 + j] = PALETTE_COLOR_USED;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* recalc */
/*TODO*///		return palette_recalc();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Playfield palette
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static void pf_color_callback(const struct rectangle *clip, const struct rectangle *tiles, const struct atarigen_pf_state *state, void *param)
/*TODO*///	{
/*TODO*///		const unsigned int *usage = &Machine.gfx[0].pen_usage[state.param[0] * 0x1000];
/*TODO*///		UINT16 *colormap = (UINT16 *)param;
/*TODO*///		int x, y;
/*TODO*///	
/*TODO*///		for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
/*TODO*///			for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
/*TODO*///			{
/*TODO*///				int offs = x * 64 + y;
/*TODO*///				int data = READ_WORD(&atarigen_playfieldram[offs * 2]);
/*TODO*///				int code = (data & 0xfff) ^ 0x800;
/*TODO*///				int color = playfield_color_base + ((data >> 12) & 7);
/*TODO*///				colormap[color] |= usage[code];
/*TODO*///				colormap[color ^ 8] |= usage[code];
/*TODO*///	
/*TODO*///				/* also mark unvisited tiles dirty */
/*TODO*///				if (!atarigen_pf_visit[offs]) atarigen_pf_dirty[offs] = 0xff;
/*TODO*///			}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Playfield rendering
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static void pf_render_callback(const struct rectangle *clip, const struct rectangle *tiles, const struct atarigen_pf_state *state, void *param)
/*TODO*///	{
/*TODO*///		const struct GfxElement *gfx = Machine.gfx[0];
/*TODO*///		struct osd_bitmap *bitmap = param;
/*TODO*///		int bank = state.param[0];
/*TODO*///		int x, y;
/*TODO*///	
/*TODO*///		/* first update any tiles whose color is out of date */
/*TODO*///		for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
/*TODO*///			for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
/*TODO*///			{
/*TODO*///				int offs = x * 64 + y;
/*TODO*///				int data = READ_WORD(&atarigen_playfieldram[offs * 2]);
/*TODO*///	
/*TODO*///				if (atarigen_pf_dirty[offs] != bank)
/*TODO*///				{
/*TODO*///					int color = playfield_color_base + ((data >> 12) & 7);
/*TODO*///					int code = bank * 0x1000 + ((data & 0xfff) ^ 0x800);
/*TODO*///					int hflip = data & 0x8000;
/*TODO*///	
/*TODO*///					drawgfx(atarigen_pf_bitmap, gfx, code, color, hflip, 0, 8 * x, 8 * y, 0, TRANSPARENCY_NONE, 0);
/*TODO*///					atarigen_pf_dirty[offs] = bank;
/*TODO*///				}
/*TODO*///	
/*TODO*///				/* track the tiles we've visited */
/*TODO*///				atarigen_pf_visit[offs] = 1;
/*TODO*///			}
/*TODO*///	
/*TODO*///		/* then blast the result */
/*TODO*///		x = -state.hscroll;
/*TODO*///		y = -state.vscroll;
/*TODO*///		copyscrollbitmap(bitmap, atarigen_pf_bitmap, 1, &x, 1, &y, clip, TRANSPARENCY_NONE, 0);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Playfield overrendering
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static void pf_overrender_callback(const struct rectangle *clip, const struct rectangle *tiles, const struct atarigen_pf_state *state, void *param)
/*TODO*///	{
/*TODO*///		const struct GfxElement *gfx = Machine.gfx[0];
/*TODO*///		const struct mo_data *modata = param;
/*TODO*///		struct osd_bitmap *bitmap = modata.bitmap;
/*TODO*///		int color_xor = modata.color_xor;
/*TODO*///		int bank = state.param[0];
/*TODO*///		int x, y;
/*TODO*///	
/*TODO*///		/* first update any tiles whose color is out of date */
/*TODO*///		for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
/*TODO*///		{
/*TODO*///			int sy = (8 * y - state.vscroll) & 0x1ff;
/*TODO*///			if (sy >= YDIM) sy -= 0x200;
/*TODO*///	
/*TODO*///			for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
/*TODO*///			{
/*TODO*///				int offs = x * 64 + y;
/*TODO*///				int data = READ_WORD(&atarigen_playfieldram[offs * 2]);
/*TODO*///				int color = playfield_color_base + ((data >> 12) & 7);
/*TODO*///				int code = bank * 0x1000 + ((data & 0xfff) ^ 0x800);
/*TODO*///				int hflip = data & 0x8000;
/*TODO*///				int sx = (8 * x - state.hscroll) & 0x1ff;
/*TODO*///				if (sx >= XDIM) sx -= 0x200;
/*TODO*///	
/*TODO*///				drawgfx(bitmap, gfx, code, color ^ color_xor, hflip, 0, sx, sy, 0, TRANSPARENCY_THROUGH, palette_transparent_pen);
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Motion object palette
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static void mo_color_callback(const UINT16 *data, const struct rectangle *clip, void *param)
/*TODO*///	{
/*TODO*///		const unsigned int *usage = Machine.gfx[0].pen_usage;
/*TODO*///		UINT16 *colormap = param;
/*TODO*///		int code = (data[0] & 0x7fff) ^ 0x800;
/*TODO*///		int hsize = ((data[2] >> 3) & 7) + 1;
/*TODO*///		int vsize = (data[2] & 7) + 1;
/*TODO*///		int color = data[1] & 0x000f;
/*TODO*///		int tiles = hsize * vsize;
/*TODO*///		UINT16 temp = 0;
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		for (i = 0; i < tiles; i++)
/*TODO*///			temp |= usage[code++];
/*TODO*///		colormap[color] |= temp;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Motion object rendering
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static void mo_render_callback(const UINT16 *data, const struct rectangle *clip, void *param)
/*TODO*///	{
/*TODO*///		const struct GfxElement *gfx = Machine.gfx[0];
/*TODO*///		const unsigned int *usage = gfx.pen_usage;
/*TODO*///		unsigned int total_usage = 0;
/*TODO*///		struct osd_bitmap *bitmap = param;
/*TODO*///		struct rectangle pf_clip;
/*TODO*///		int x, y, sx, sy;
/*TODO*///	
/*TODO*///		/* extract data from the various words */
/*TODO*///		int code = (data[0] & 0x7fff) ^ 0x800;
/*TODO*///		int color = data[1] & 0x000f;
/*TODO*///		int ypos = -pf_state.vscroll - (data[2] >> 7);
/*TODO*///		int hflip = data[2] & 0x0040;
/*TODO*///		int hsize = ((data[2] >> 3) & 7) + 1;
/*TODO*///		int vsize = (data[2] & 7) + 1;
/*TODO*///		int xpos = -pf_state.hscroll + (data[1] >> 7);
/*TODO*///		int xadv;
/*TODO*///	
/*TODO*///		/* adjust for height */
/*TODO*///		ypos -= vsize * 8;
/*TODO*///	
/*TODO*///		/* adjust the final coordinates */
/*TODO*///		xpos &= 0x1ff;
/*TODO*///		ypos &= 0x1ff;
/*TODO*///		if (xpos >= XDIM) xpos -= 0x200;
/*TODO*///		if (ypos >= YDIM) ypos -= 0x200;
/*TODO*///	
/*TODO*///		/* determine the bounding box */
/*TODO*///		atarigen_mo_compute_clip_8x8(pf_clip, xpos, ypos, hsize, vsize, clip);
/*TODO*///	
/*TODO*///		/* adjust for h flip */
/*TODO*///		if (hflip != 0)
/*TODO*///			xpos += (hsize - 1) * 8, xadv = -8;
/*TODO*///		else
/*TODO*///			xadv = 8;
/*TODO*///	
/*TODO*///		/* loop over the height */
/*TODO*///		for (y = 0, sy = ypos; y < vsize; y++, sy += 8)
/*TODO*///		{
/*TODO*///			/* clip the Y coordinate */
/*TODO*///			if (sy <= clip.min_y - 8)
/*TODO*///			{
/*TODO*///				code += hsize;
/*TODO*///				continue;
/*TODO*///			}
/*TODO*///			else if (sy > clip.max_y)
/*TODO*///				break;
/*TODO*///	
/*TODO*///			/* loop over the width */
/*TODO*///			for (x = 0, sx = xpos; x < hsize; x++, sx += xadv, code++)
/*TODO*///			{
/*TODO*///				/* clip the X coordinate */
/*TODO*///				if (sx <= -8 || sx >= XDIM)
/*TODO*///					continue;
/*TODO*///	
/*TODO*///				/* draw the sprite */
/*TODO*///				drawgfx(bitmap, gfx, code, color, hflip, 0, sx, sy, clip, TRANSPARENCY_PEN, 0);
/*TODO*///				total_usage |= usage[code];
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* overrender the playfield */
/*TODO*///		if ((total_usage & 0x0002) != 0)
/*TODO*///		{
/*TODO*///			struct mo_data modata;
/*TODO*///			modata.bitmap = bitmap;
/*TODO*///			modata.color_xor = (color == 0 && vindctr2_screen_refresh) ? 0 : 8;
/*TODO*///			atarigen_pf_process(pf_overrender_callback, &modata, &pf_clip);
/*TODO*///		}
/*TODO*///	}
}
