/***************************************************************************

	Atari Bad Lands hardware

***************************************************************************/


/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.vidhrdw;
        
import static gr.codebb.arcadeflex.common.PtrLib.*;
import gr.codebb.arcadeflex.common.SubArrays;
import static gr.codebb.arcadeflex.common.SubArrays.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v037b7.machine.atarigen.*;
import static gr.codebb.arcadeflex.v037b7.machine.atarigenH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;
import gr.codebb.arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.*;
import static common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.mameH.MAX_GFX_ELEMENTS;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v036.mame.timer.*;
import static arcadeflex.v036.mame.timerH.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import gr.codebb.arcadeflex.v037b7.machine.atarigenH;
import gr.codebb.arcadeflex.v037b7.mame.drawgfxH;


public class badlands
{
	
	public static final int XCHARS  = 42;
	public static final int YCHARS  = 30;
	
	public static final int XDIM    = (XCHARS*8);
	public static final int YDIM    = (YCHARS*8);
	
	
	
	/*************************************
	 *
	 *	Statics
	 *
	 *************************************/
	
	public static atarigen_pf_state pf_state;
	
	/*************************************
	 *
	 *	Generic video system start
	 *
	 *************************************/
        static atarigen_mo_desc mo_desc = new atarigen_mo_desc
        (
                32,                  /* maximum number of MO's */
                4,                   /* number of bytes per MO entry */
                0x80,                /* number of bytes between MO words */
                0,                   /* ignore an entry if this word == 0xffff */
                -1, 0, 0x3f,         /* link = (data[linkword] >> linkshift) & linkmask */
                0,                    /* render in reverse link order */
                0
        );

        static atarigen_pf_desc pf_desc = new atarigen_pf_desc
        (
                8, 8,				/* width/height of each tile */
                64, 64,				/* number of tiles in each direction */
                1					/* non-scrolling */
        );

	public static VhStartPtr badlands_vh_start = new VhStartPtr() { public int handler() 
	{
			
		/* initialize statics */
		//memset(&pf_state, 0, sizeof(pf_state));
                pf_state = new atarigen_pf_state();
	
		/* initialize the playfield */
		if (atarigen_pf_init(pf_desc) != 0)
			return 1;
	
		/* initialize the motion objects */
		if (atarigen_mo_init(mo_desc) != 0)
		{
			atarigen_pf_free();
			return 1;
		}
	
		/*
		 * if we are palette reducing, do the simple thing by marking everything used except for
		 * the transparent sprite and alpha colors; this should give some leeway for machines
		 * that can't give up all 256 colors
		 */
		if (palette_used_colors != null)
		{
			int i;
	
			memset(palette_used_colors, PALETTE_COLOR_USED, Machine.drv.total_colors);
			for (i = 0; i < 8; i++)
				palette_used_colors.write(0x80 + i * 16, PALETTE_COLOR_TRANSPARENT);
		}
	
		return 0;
	} };
	
	
	
	/*************************************
	 *
	 *	Video system shutdown
	 *
	 *************************************/
	
	public static VhStopPtr badlands_vh_stop = new VhStopPtr() { public void handler() 
	{
		atarigen_pf_free();
		atarigen_mo_free();
	} };
	
	
	
	/*************************************
	 *
	 *	Periodic scanline updater
	 *
	 *************************************/
	
	public static void badlands_scanline_update(int scanline)
	{
		/* update motion objects */
		if (scanline == 0)
			atarigen_mo_update(atarigen_spriteram, 0, scanline);
	}
	
	
	
	/*************************************
	 *
	 *	Playfield bank write handler
	 *
	 *************************************/
	
	public static WriteHandlerPtr badlands_pf_bank_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = atarigen_playfieldram.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword, data);
	
		if (oldword != newword)
		{
			pf_state.param[0] = data & 1;
			atarigen_pf_update(pf_state, cpu_getscanline());
		}
	} };
	
	
	
	/*************************************
	 *
	 *	Playfield RAM write handler
	 *
	 *************************************/
	
	public static WriteHandlerPtr badlands_playfieldram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
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
	 *	Main refresh
	 *
	 *************************************/
	
	public static VhUpdatePtr badlands_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int i;
	
		/* remap if necessary */
		if (palette_recalc() != null)
			memset(atarigen_pf_dirty, 0xff, atarigen_playfieldram_size[0] / 2);
	
		/* set up the all-transparent overrender palette */
		for (i = 0; i < 16; i++)
			atarigen_overrender_colortable[i] = palette_transparent_pen;
	
		/* draw the playfield */
		atarigen_pf_process(pf_render_callback, bitmap, Machine.visible_area);
	
		/* render the motion objects */
		atarigen_mo_process(mo_render_callback, bitmap);
	
		/* update onscreen messages */
		atarigen_update_messages();
	} };
	
	
	
	/*************************************
	 *
	 *	Playfield rendering
	 *
	 *************************************/
	
	static atarigen_pf_callback pf_render_callback = new atarigen_pf_callback() {
            @Override
            public void handler(rectangle clip, rectangle tiles, atarigen_pf_state state, Object param) {
                GfxElement gfx = Machine.gfx[0];
		int bank = state.param[0] * 0x1000;
		osd_bitmap bitmap = (osd_bitmap) param;
		int x, y;
	
		/* standard loop over tiles */
		for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
			for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
			{
				int offs = y * 64 + x;
	
				/* update only if dirty */
				if (atarigen_pf_dirty.read(offs) != state.param[0])
				{
					int data = atarigen_playfieldram.READ_WORD(offs * 2);
					int code = data & 0x1fff;
					int color = data >> 13;
					if ((code & 0x1000) != 0) code += bank;
	
					drawgfx(atarigen_pf_bitmap, gfx, code, color, 0, 0, 8 * x, 8 * y, null, TRANSPARENCY_NONE, 0);
					atarigen_pf_dirty.write(offs, state.param[0]);
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
		int bank = state.param[0] * 0x1000;
		osd_bitmap bitmap = (osd_bitmap) param;
		int x, y;
	
		/* standard loop over tiles */
		for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
			for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
			{
				int offs = y * 64 + x;
	/*			int priority_offs = y * 64 + XCHARS + x / 2;*/
				int data = atarigen_playfieldram.READ_WORD(offs * 2);
				int color = data >> 13;
				int code = data & 0x1fff;
				if ((code & 0x1000) != 0) code += bank;
	
				drawgfx(bitmap, gfx, code, color, 0, 0, 8 * x, 8 * y, clip, TRANSPARENCY_PENS, 0x00ff);
			}
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
		int ypos = -(data.read(1) >> 7);
		int vsize = (data.read(1) & 0x000f) + 1;
		int code = data.read(0) & 0x0fff;
		int xpos = data.read(3) >> 7;
		int color = data.read(3) & 0x0007;
		int priority = (data.read(3) >> 3) & 1;
	
		/* adjust for height */
		ypos -= vsize * 8;
	
		/* adjust the final coordinates */
		xpos &= 0x1ff;
		ypos &= 0x1ff;
		if (xpos >= XDIM) xpos -= 0x200;
		if (ypos >= YDIM) ypos -= 0x200;
	
		/* clip the X coordinate */
		if (xpos <= -16 || xpos >= XDIM)
			return;
	
		/* determine the bounding box */
		pf_clip = atarigen_mo_compute_clip_16x16(xpos, ypos, 1, vsize, clip);
	
		/* simple case? */
		if (priority == 1)
		{
			/* draw the motion object */
			atarigen_mo_draw_16x8_strip(bitmap, gfx, code, color, 0, 0, xpos, ypos, vsize, clip, TRANSPARENCY_PEN, 0);
		}
	
		/* otherwise, make it tricky */
		else
		{
			/* draw an instance of the object in all transparent pens */
			atarigen_mo_draw_transparent_16x8_strip(bitmap, gfx, code, 0, 0, xpos, ypos, vsize, clip, TRANSPARENCY_PEN, 0);
	
			/* and then draw it normally on the temp bitmap */
			atarigen_mo_draw_16x8_strip(atarigen_pf_overrender_bitmap, gfx, code, color, 0, 0, xpos, ypos, vsize, clip, TRANSPARENCY_NONE, 0);
	
			/* overrender the playfield on top of that that */
			atarigen_pf_process(pf_overrender_callback, atarigen_pf_overrender_bitmap, pf_clip);
	
			/* finally, copy this chunk to the real bitmap */
			copybitmap(bitmap, atarigen_pf_overrender_bitmap, 0, 0, 0, 0, pf_clip, TRANSPARENCY_THROUGH, palette_transparent_pen);
		}
            }
        };
        
}
