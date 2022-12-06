/***************************************************************************

  vidhrdw/blstroid.c

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
			Bits  7-15 = vertical position
			Bits  0-3  = vertical size of the object, in tiles

		Word 2:
			Bit  15    = horizontal flip
			Bit  14    = vertical flip
			Bits  0-13 = image index

		Word 3:
			Bits  3-11 = link to the next motion object

		Word 4:
			Bits  6-15 = horizontal position
			Bits  0-3  = motion object palette

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
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v036.mame.timer.*;
import gr.codebb.arcadeflex.v037b7.machine.atarigenH;
import gr.codebb.arcadeflex.v037b7.mame.drawgfxH;

public class blstroid
{
	
	public static final int XCHARS  = 40;
	public static final int YCHARS  = 30;
	
	public static final int XDIM    = (XCHARS*16);
	public static final int YDIM    = (YCHARS*8);
	
	
	
	/*************************************
	 *
	 *	Statics
	 *
	 *************************************/
	
	static int[] priority=new int[8];
	
	
	/*************************************
	 *
	 *	Generic video system start
	 *
	 *************************************/
	static atarigen_mo_desc mo_desc = new atarigen_mo_desc
        (
                512,                 /* maximum number of MO's */
                8,                   /* number of bytes per MO entry */
                2,                   /* number of bytes between MO words */
                0,                   /* ignore an entry if this word == 0xffff */
                2, 3, 0x1ff,         /* link = (data[linkword] >> linkshift) & linkmask */
                0,                    /* render in reverse link order */
                0
        );

        static atarigen_pf_desc pf_desc = new atarigen_pf_desc
        (
                16, 8,				/* width/height of each tile */
                64, 64,				/* number of tiles in each direction */
                1					/* non-scrolling */
        );
        
	public static VhStartPtr blstroid_vh_start = new VhStartPtr() { public int handler() 
	{
		
	
		/* reset statics */
		memset(priority, 0, priority.length);
	
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
	
	public static VhStopPtr blstroid_vh_stop = new VhStopPtr() { public void handler() 
	{
		atarigen_pf_free();
		atarigen_mo_free();
	} };
	
	
	
	/*************************************
	 *
	 *	Periodic scanline updater
	 *
	 *************************************/
	
	static TimerCallbackHandlerPtr irq_off = new TimerCallbackHandlerPtr() {
            @Override
            public void handler(int param) {
                atarigen_scanline_int_ack_w.handler(0, 0);
            }
        };
	
	public static TimerCallbackHandlerPtr blstroid_scanline_update = new TimerCallbackHandlerPtr() {
            @Override
            public void handler(int scanline) {
		int offset = (scanline / 8) * 0x80 + 0x50;
	
		/* update motion objects */
		if (scanline == 0)
			atarigen_mo_update(atarigen_spriteram, 0, scanline);
	
		/* check for interrupts */
		if (offset < atarigen_playfieldram_size[0])
			if ((atarigen_playfieldram.READ_WORD(offset) & 0x8000) != 0)
			{
				/* generate the interrupt */
				atarigen_scanline_int_gen();
				atarigen_update_interrupts();
	
				/* also set a timer to turn ourself off */
				timer_set(cpu_getscanlineperiod(), 0, irq_off);
			}
            }
        };
	
	
	/*************************************
	 *
	 *	Playfield RAM write handler
	 *
	 *************************************/
	
	public static WriteHandlerPtr blstroid_playfieldram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
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
	 *	Priority RAM write handler
	 *
	 *************************************/
	
	public static WriteHandlerPtr blstroid_priorityram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int shift, which;
	
		/* pick which playfield palette to look at */
		which = (offset >> 5) & 7;
	
		/* upper 16 bits are for H == 1, lower 16 for H == 0 */
		shift = (offset >> 4) & 0x10;
		shift += (offset >> 1) & 0x0f;
	
		/* set or clear the appropriate bit */
		priority[which] = (priority[which] & ~(1 << shift)) | ((data & 1) << shift);
	} };
	
	
	
	/*************************************
	 *
	 *	Main refresh
	 *
	 *************************************/
	
	public static VhUpdatePtr blstroid_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		/* remap if necessary */
		if (update_palette() != null)
			memset(atarigen_pf_dirty, 1, atarigen_playfieldram_size[0] / 2);
	
		/* draw the playfield */
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
		int[] pf_map=new int[8], mo_map=new int[16];
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
		for (i = 0; i < 8; i++)
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
	
	static atarigen_pf_callback pf_color_callback = new atarigen_pf_callback() {
            @Override
            public void handler(rectangle clip, rectangle tiles, atarigen_pf_state state, Object param) {
		int[] usage = Machine.gfx[0].pen_usage;
		int[] colormap = (int[]) param;
		int x, y;
	
		/* standard loop over tiles */
		for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
			for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
			{
				int offs = y * 64 + x;
				int data = atarigen_playfieldram.READ_WORD(offs * 2);
				int code = data & 0x1fff;
				int color = data >> 13;
	
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
		for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
			for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
			{
				int offs = y * 64 + x;
	
				/* update only if dirty */
				if (atarigen_pf_dirty.read(offs) != 0)
				{
					int data = atarigen_playfieldram.READ_WORD(offs * 2);
					int code = data & 0x1fff;
					int color = data >> 13;
	
					drawgfx(atarigen_pf_bitmap, gfx, code, color, 0, 0, 16 * x, 8 * y, null, TRANSPARENCY_NONE, 0);
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
	
	static atarigen_pf_callback pf_overrender_callback = new atarigen_pf_callback() {
            @Override
            public void handler(drawgfxH.rectangle clip, drawgfxH.rectangle tiles, atarigenH.atarigen_pf_state state, Object param) {
                GfxElement gfx = Machine.gfx[0];
		osd_bitmap bitmap = (osd_bitmap) param;
		int x, y;
	
		/* standard loop over tiles */
		for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
			for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
			{
				int offs = y * 64 + x;
				int data = atarigen_playfieldram.READ_WORD(offs * 2);
				int color = data >> 13;
	
				/* overrender if there is a non-zero priority for this color */
				/* not perfect, but works for the most obvious cases */
				if (priority[color]==0)
				{
					int code = data & 0x1fff;
					drawgfx(bitmap, gfx, code, color, 0, 0, 16 * x, 8 * y, clip, TRANSPARENCY_NONE, 0);
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
		int vsize = (data.read(0) & 0x000f) + 1;
		int code = data.read(1) & 0x3fff;
		int color = data.read(3) & 0x000f;
		int temp = 0;
		int i;
	
		for (i = 0; i < vsize; i++)
			temp |= usage[code++];
		colormap[color] |= temp;;
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
		osd_bitmap bitmap = (osd_bitmap) param;
		rectangle pf_clip = new rectangle();
	
		/* extract data from the various words */
		int ypos = -(data.read(0) >> 7);
		int vsize = (data.read(0) & 0x000f) + 1;
		int hflip = data.read(1) & 0x8000;
		int vflip = data.read(1) & 0x4000;
		int code = data.read(1) & 0x3fff;
		int xpos = (data.read(3) >> 7) << 1;
		int color = data.read(3) & 0x000f;
	
		/* adjust for height */
		ypos -= vsize * 8;
	
		/* adjust the final coordinates */
		xpos &= 0x3ff;
		ypos &= 0x1ff;
		if (xpos >= XDIM) xpos -= 0x400;
		if (ypos >= YDIM) ypos -= 0x200;
	
		/* clip the X coordinate */
		if (xpos <= -16 || xpos >= XDIM)
			return;
	
		/* determine the bounding box */
		pf_clip = atarigen_mo_compute_clip_16x8(xpos, ypos, 1, vsize, clip);
	
		/* draw the motion object */
		atarigen_mo_draw_16x8_strip(bitmap, gfx, code, color, hflip, vflip, xpos, ypos, vsize, clip, TRANSPARENCY_PEN, 0);
	
		/* overrender the playfield */
		atarigen_pf_process(pf_overrender_callback, bitmap, pf_clip);
            }
        };
}
