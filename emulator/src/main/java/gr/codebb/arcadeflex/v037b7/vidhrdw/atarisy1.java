/***************************************************************************

	Atari System 1 hardware

****************************************************************************/


/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import arcadeflex.v036.generic.funcPtr.TimerCallbackHandlerPtr;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import gr.codebb.arcadeflex.common.SubArrays.IntSubArray;
import gr.codebb.arcadeflex.common.SubArrays.UShortArray;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v037b7.machine.atarigen.*;
import static gr.codebb.arcadeflex.v037b7.machine.atarigenH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.mameH.MAX_GFX_ELEMENTS;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.timer.*;
import static arcadeflex.v036.mame.timerH.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;

public class atarisy1
{
	
	public static final int XCHARS = 42;
	public static final int YCHARS = 30;
	
	public static final int XDIM = (XCHARS*8);
	public static final int YDIM = (YCHARS*8);
	
	
	
	/*************************************
	 *
	 *	Constants
	 *
	 *************************************/
	
	/* the color and remap PROMs are mapped as follows */
	public static final int PROM1_BANK_4			= 0x80;		/* active low */
	public static final int PROM1_BANK_3			= 0x40;		/* active low */
	public static final int PROM1_BANK_2			= 0x20;		/* active low */
	public static final int PROM1_BANK_1			= 0x10;		/* active low */
	public static final int PROM1_OFFSET_MASK		= 0x0f;		/* postive logic */
	
	public static final int PROM2_BANK_6_OR_7		= 0x80;		/* active low */
	public static final int PROM2_BANK_5			= 0x40;		/* active low */
	public static final int PROM2_PLANE_5_ENABLE            = 0x20;		/* active high */
	public static final int PROM2_PLANE_4_ENABLE            = 0x10;		/* active high */
	public static final int PROM2_PF_COLOR_MASK		= 0x0f;		/* negative logic */
	public static final int PROM2_BANK_7			= 0x08;		/* active low, plus PROM2_BANK_6_OR_7 low as well */
	public static final int PROM2_MO_COLOR_MASK		= 0x07;		/* negative logic */
	
	public static final int OVERRENDER_PRIORITY		= 1;
	public static final int OVERRENDER_SPECIAL		= 2;
	
	
	
	/*************************************
	 *
	 *	Macros
	 *
	 *************************************/
	
	/* these macros make accessing the indirection table easier, plus this is how the data
	   is stored for the pfmapped array */
	public static int PACK_LOOKUP_DATA(int bank, int color, int offset, int bpp)
        {
			return (((((bpp) - 4) & 7) << 24) | 
			 (((color) & 255) << 16) | 
			 (((bank) & 15) << 12) | 
			 (((offset) & 15) << 8));
        }
	
	public static int LOOKUP_BPP(int data){ return (((data) >> 24) & 7); }
	public static int LOOKUP_COLOR(int data){ return (((data) >> 16) & 0xff); }
	public static int LOOKUP_GFX(int data){ return (((data) >> 12) & 15); }
	public static int LOOKUP_CODE(int data){ return ((data) & 0x0fff); }
	
	
	
	/*************************************
	 *
	 *	Structures
	 *
	 *************************************/
	
	static class pf_overrender_data
	{
		public osd_bitmap bitmap;
		public int type;
	};
	
	
	
	/*************************************
	 *
	 *	Globals we own
	 *
	 *************************************/
	
	public static UBytePtr  atarisys1_bankselect = new UBytePtr();
	public static UBytePtr  atarisys1_prioritycolor = new UBytePtr();
	
	
	
	/*************************************
	 *
	 *	Statics
	 *
	 *************************************/
	
	/* playfield parameters */
	static atarigen_pf_state pf_state;
	static int priority_pens;
	
	/* indirection tables */
	static int[] pf_lookup = new int[256];
	static int[] mo_lookup = new int[256];
	
	/* INT3 tracking */
	static timer_entry[] int3_timer = new timer_entry[YDIM];
	static timer_entry int3off_timer;
	
	/* graphics bank tracking */
	static int[][] bank_gfx = new int[3][8];
	static int[][] pen_usage = new int[MAX_GFX_ELEMENTS][MAX_GFX_ELEMENTS];
	
	/* basic form of a graphics bank */
	static GfxLayout objlayout = new GfxLayout
	(
		8,8,	/* 8*8 sprites */
		4096,	/* 4096 of them */
		6,		/* 6 bits per pixel */
		new int[] { 5*8*0x08000, 4*8*0x08000, 3*8*0x08000, 2*8*0x08000, 1*8*0x08000, 0*8*0x08000 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8		/* every sprite takes 8 consecutive bytes */
	);
        
        
	/*************************************
	 *
	 *	Generic video system start
	 *
	 *************************************/
	static atarigen_mo_desc mo_desc = new atarigen_mo_desc
        (
                64,                  /* maximum number of MO's */
                2,                   /* number of bytes per MO entry */
                0x80,                /* number of bytes between MO words */
                1,                   /* ignore an entry if this word == 0xffff */
                3, 0, 0x3f,          /* link = (data[linkword] >> linkshift) & linkmask */
                0,                    /* render in reverse link order */
                0
        );

        static atarigen_pf_desc pf_desc = new atarigen_pf_desc
        (
                8, 8,				/* width/height of each tile */
                64, 64,				/* number of tiles in each direction */
                0
        );
        
	public static VhStartHandlerPtr atarisys1_vh_start = new VhStartHandlerPtr() { public int handler() 
	{
		
		int i, e;
	
		/* first decode the graphics */
		if (decode_gfx() != 0)
			return 1;
	
		/* reset the statics */
		pf_state = new atarigen_pf_state();
//		memset(int3_timer, 0, sizeof(int3_timer));
                int3_timer = new timer_entry[YDIM];
	
		/* initialize the pen usage array */
		for (e = 0; e < MAX_GFX_ELEMENTS; e++)
			if (Machine.gfx[e] != null)
			{
				pen_usage[e] = Machine.gfx[e].pen_usage;
	
				/* if this element has 6bpp data, create a special new usage array for it */
				if (Machine.gfx[e].color_granularity == 64)
				{
					GfxElement gfx = Machine.gfx[e];
	
					/* allocate storage */
					pen_usage[e] = new int[gfx.total_elements * 2];
					if (pen_usage[e]!=null)
					{
						int[] entry;
                                                int entryPos=0;
						int x, y;
	
						/* scan each entry, marking which pens are used */
						pen_usage[e]=new int[gfx.total_elements];
						for (i = 0, entry = pen_usage[e]; i < gfx.total_elements; i++, entryPos += 2)
						{
							UBytePtr dp = new UBytePtr(gfx.gfxdata, i * gfx.char_modulo);
							for (y = 0; y < gfx.height; y++)
							{
								for (x = 0; x < gfx.width; x++)
								{
									int color = dp.read(x);
									entry[(color >> 5) & 1] |= 1 << (color & 31);
								}
								dp.inc( gfx.line_modulo );
							}
						}
                                                
                                                if ((e+entryPos)<pen_usage.length) // hack by Chuso
                                                    entry = pen_usage[e+entryPos];
					}
				}
			}
	
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
	
	public static VhStopHandlerPtr atarisys1_vh_stop = new VhStopHandlerPtr() { public void handler() 
	{
		int i;
	
		/* free any extra pen usage */
		for (i = 0; i < MAX_GFX_ELEMENTS; i++)
		{
			if (pen_usage[i]!=null && Machine.gfx[i]!=null && pen_usage[i] != Machine.gfx[i].pen_usage)
				pen_usage[i]=null;
			
		}
	
		atarigen_pf_free();
		atarigen_mo_free();
	} };
	
	
	
	/*************************************
	 *
	 *	Graphics bank selection
	 *
	 *************************************/
	
	public static WriteHandlerPtr atarisys1_bankselect_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = atarisys1_bankselect.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword, data);
		int scanline = cpu_getscanline();
		int diff = oldword ^ newword;
	
		/* update memory */
		atarisys1_bankselect.WRITE_WORD(offset, newword);
	
		/* sound CPU reset */
		if ((diff & 0x0080) != 0)
		{
			cpu_set_reset_line(1, (newword & 0x0080)!=0 ? CLEAR_LINE : ASSERT_LINE);
			if ((newword & 0x0080)==0) atarigen_sound_reset();
		}
	
		/* motion object bank select */
		atarisys1_scanline_update.handler(scanline);
	
		/* playfield bank select */
		if ((diff & 0x04) != 0)
		{
			pf_state.param[0] = (newword & 0x04)!=0 ? 0x80 : 0x00;
			atarigen_pf_update(pf_state, cpu_getscanline() + 1);
		}
	} };
	
	
	
	/*************************************
	 *
	 *	Playfield horizontal scroll
	 *
	 *************************************/
	
	public static WriteHandlerPtr atarisys1_hscroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = atarigen_hscroll.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword, data);
		atarigen_hscroll.WRITE_WORD(offset, newword);
	
		/* set the new scroll value and update the playfield status */
		pf_state.hscroll = newword & 0x1ff;
		atarigen_pf_update(pf_state, cpu_getscanline() + 1);
	} };
	
	
	
	/*************************************
	 *
	 *	Playfield vertical scroll
	 *
	 *************************************/
	
	public static WriteHandlerPtr atarisys1_vscroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int scanline = cpu_getscanline() + 1;
	
		int oldword = atarigen_vscroll.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword, data);
		atarigen_vscroll.WRITE_WORD(offset, newword);
	
		/* because this latches a new value into the scroll base,
		   we need to adjust for the scanline */
		pf_state.vscroll = newword & 0x1ff;
		if (scanline < YDIM) pf_state.vscroll -= scanline;
		atarigen_pf_update(pf_state, scanline);
	} };
	
	
	
	/*************************************
	 *
	 *	Playfield RAM write handler
	 *
	 *************************************/
	
	public static WriteHandlerPtr atarisys1_playfieldram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
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
	 *	Sprite RAM write handler
	 *
	 *************************************/
	
	public static WriteHandlerPtr atarisys1_spriteram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = atarigen_spriteram.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword, data);
	
		if (oldword != newword)
		{
			atarigen_spriteram.WRITE_WORD(offset, newword);
	
			/* if modifying a timer, beware */
			if (((offset & 0x180) == 0x000 && atarigen_spriteram.READ_WORD(offset | 0x080) == 0xffff) ||
			    ((offset & 0x180) == 0x080 && newword == 0xffff))
			{
				/* if the timer is in the active bank, update the display list */
				if ((offset >> 9) == ((atarisys1_bankselect.READ_WORD(0) >> 3) & 7))
				{
					logerror("Caught timer mod!\n");
					atarisys1_scanline_update.handler(cpu_getscanline());
				}
			}
		}
	} };
	
	
	
	/*************************************
	 *
	 *	MO interrupt handlers
	 *
	 *************************************/
	
	public static TimerCallbackHandlerPtr int3off_callback = new TimerCallbackHandlerPtr() { public void handler(int param) 
	{
		/* clear the state */
		atarigen_scanline_int_ack_w.handler(0, 0);
	
		/* make this timer go away */
		int3off_timer = null;
	} };
	
	
	static TimerCallbackHandlerPtr atarisys1_int3_callback = new TimerCallbackHandlerPtr() {
            @Override
            public void handler(int param) {
                /* update the state */
		atarigen_scanline_int_gen();
	
		/* set a timer to turn it off */
		if (int3off_timer != null)
			timer_remove(int3off_timer);
		int3off_timer = timer_set(cpu_getscanlineperiod(), 0, int3off_callback);
	
		/* set ourselves up to go off next frame */
		int3_timer[param] = timer_set(TIME_IN_HZ(Machine.drv.frames_per_second), param, atarisys1_int3_callback);
            }
        };
	
	
	
	/*************************************
	 *
	 *	MO interrupt state read
	 *
	 *************************************/
	
	public static ReadHandlerPtr atarisys1_int3state_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return atarigen_scanline_int_state!=0 ? 0x0080 : 0x0000;
	} };
	
	
	
	/*************************************
	 *
	 *	Periodic MO updater
	 *
	 *************************************/
	
	public static TimerCallbackHandlerPtr atarisys1_scanline_update = new TimerCallbackHandlerPtr() {
            @Override
            public void handler(int scanline) {
                int bank = ((atarisys1_bankselect.READ_WORD(0) >> 3) & 7) * 0x200;
		UBytePtr base = new UBytePtr(atarigen_spriteram, bank);
		int[] spritevisit=new int[64];
		int[] timer=new int[YDIM];
		int link = 0;
		int i;
	
		/* only process if we're still onscreen */
		if (scanline < YDIM)
		{
			/* generic update first */
			if (scanline==0)
				atarigen_mo_update(base, 0, 0);
			else
				atarigen_mo_update(base, 0, scanline + 1);
		}
	
		/* visit all the sprites and look for timers */
		memset(spritevisit, 0, spritevisit.length);
		memset(timer, 0, timer.length);
		while (spritevisit[link]==0)
		{
			int data2 = base.READ_WORD(link * 2 + 0x080);
	
			/* a codeure of 0xffff is really an interrupt - gross! */
			if (data2 == 0xffff)
			{
				int data1 = base.READ_WORD(link * 2 + 0x000);
				int vsize = (data1 & 15) + 1;
				int ypos = (256 - (data1 >> 5) - vsize * 8) & 0x1ff;
	
				/* only generate timers on visible scanlines */
				if (ypos < YDIM)
					timer[ypos] = 1;
			}
	
			/* link to the next object */
			spritevisit[link] = 1;
			link = atarigen_spriteram.READ_WORD(bank + link * 2 + 0x180) & 0x3f;
		}
	
		/* update our interrupt timers */
		for (i = 0; i < YDIM; i++)
		{
			if (timer[i]!=0 && int3_timer[i]==null)
				int3_timer[i] = timer_set(cpu_getscanlinetime(i), i, atarisys1_int3_callback);
			else if (timer[i]==0 && int3_timer[i]!=null)
			{
				timer_remove(int3_timer[i]);
				int3_timer[i] = null;
			}
		}
            }
        };
        
	
	/*************************************
	 *
	 *	Main refresh
	 *
	 *************************************/
	
	public static VhUpdateHandlerPtr atarisys1_vh_screenrefresh = new VhUpdateHandlerPtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int i;
	
		/* update the palette */
		if (update_palette() != null)
			memset(atarigen_pf_dirty, 0xff, atarigen_playfieldram_size[0] / 2);
	
		/* set up the all-transparent overrender palette */
		for (i = 0; i < 16; i++)
			atarigen_overrender_colortable[i] = palette_transparent_pen;
	
		/* render the playfield */
		memset(atarigen_pf_visit, 0, 64*64);
		atarigen_pf_process(pf_render_callback, bitmap, Machine.visible_area);
	
		/* render the motion objects */
		priority_pens = atarisys1_prioritycolor.READ_WORD(0) & 0xff;
		atarigen_mo_process(mo_render_callback, bitmap);
	
		/* redraw the alpha layer completely */
		{
			GfxElement gfx = Machine.gfx[0];
			int sx, sy, offs;
	
			for (sy = 0; sy < YCHARS; sy++)
				for (sx = 0, offs = sy*64; sx < XCHARS; sx++, offs++)
				{
					int data = atarigen_alpharam.READ_WORD(offs * 2);
					int opaque = data & 0x2000;
					int code = data & 0x3ff;
	
					if (code!=0 || opaque!=0)
					{
						int color = (data >> 10) & 7;
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
		int[] al_map=new int[8], pfmo_map=new int[32];
		int i, j;
	
		/* reset color tracking */
		memset(pfmo_map, 0, pfmo_map.length);
		memset(al_map, 0, al_map.length);
		palette_init_used_colors();
	
		/* always remap the transluscent colors */
		memset(new UBytePtr(palette_used_colors, 0x300), PALETTE_COLOR_USED, 16);
	
		/* update color usage for the playfield */
		atarigen_pf_process(pf_color_callback, pfmo_map, Machine.visible_area);
	
		/* update color usage for the mo's */
		atarigen_mo_process(mo_color_callback, pfmo_map);
	
		/* update color usage for the alphanumerics */
		{
			int[] usage = Machine.gfx[0].pen_usage;
			int sx, sy, offs;
	
			for (sy = 0; sy < YCHARS; sy++)
				for (sx = 0, offs = sy * 64; sx < XCHARS; sx++, offs++)
				{
					int data = atarigen_alpharam.READ_WORD(offs * 2);
					int color = (data >> 10) & 7;
					int code = data & 0x3ff;
					al_map[color] |= usage[code];
				}
		}
	
		/* determine the final playfield palette */
		for (i = 16; i < 32; i++)
		{
			int used = pfmo_map[i];
			if (used != 0)
				for (j = 0; j < 16; j++)
					if ((used & (1 << j)) != 0)
						palette_used_colors.write(0x100 + i * 16 + j, PALETTE_COLOR_USED);
		}
	
		/* determine the final motion object palette */
		for (i = 0; i < 16; i++)
		{
			int used = pfmo_map[i];
			if (used != 0)
			{
				palette_used_colors.write(0x100 + i * 16, PALETTE_COLOR_TRANSPARENT);
				for (j = 1; j < 16; j++)
					if ((used & (1 << j)) != 0)
						palette_used_colors.write(0x100 + i * 16 + j, PALETTE_COLOR_USED);
			}
		}
	
		/* determine the final alpha palette */
		for (i = 0; i < 8; i++)
		{
			int used = al_map[i];
			if (used != 0)
				for (j = 0; j < 4; j++)
					if ((used & (1 << j)) != 0)
						palette_used_colors.write(0x000 + i * 4 + j, PALETTE_COLOR_USED);
		}
	
		/* recalculate the palette */
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
                IntSubArray lookup_table = new IntSubArray(pf_lookup, state.param[0]);
		int[] colormap = (int[]) param;
		int x, y;
	
		/* standard loop over tiles */
		for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
			for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
			{
				int offs = y * 64 + x;
				int data = atarigen_playfieldram.READ_WORD(offs * 2);
				int lookup = lookup_table.read((data >> 8) & 0x7f);
				int[] usage = pen_usage[LOOKUP_GFX(lookup)];
				int bpp = LOOKUP_BPP(lookup);
				int code = LOOKUP_CODE(lookup) | (data & 0xff);
				int color = LOOKUP_COLOR(lookup);
				int bits;
	
				/* based on the depth, we need to tweak our pen mapping */
				if (bpp == 0)
					colormap[color] |= usage[code];
				else if (bpp == 1)
				{
					bits = usage[code];
					colormap[color * 2] |= bits;
					colormap[color * 2 + 1] |= bits >> 16;
				}
				else
				{
					bits = usage[code * 2];
					colormap[color * 4] |= bits;
					colormap[color * 4 + 1] |= bits >> 16;
					bits = usage[code * 2 + 1];
					colormap[color * 4 + 2] |= bits;
					colormap[color * 4 + 3] |= bits >> 16;
				}
	
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
                int bank = state.param[0];
		IntSubArray lookup_table = new IntSubArray(pf_lookup, bank);
		osd_bitmap bitmap = (osd_bitmap) param;
		int x, y;
	
		/* standard loop over tiles */
		for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
			for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
			{
				int offs = y * 64 + x;
	
				/* update only if dirty */
				if (atarigen_pf_dirty.read(offs) != bank)
				{
					int data = atarigen_playfieldram.READ_WORD(offs * 2);
					int lookup = lookup_table.read((data >> 8) & 0x7f);
					GfxElement gfx = Machine.gfx[LOOKUP_GFX(lookup)];
					int code = LOOKUP_CODE(lookup) | (data & 0xff);
					int color = LOOKUP_COLOR(lookup);
					int hflip = data & 0x8000;
	
					drawgfx(atarigen_pf_bitmap, gfx, code, color, hflip, 0, 8 * x, 8 * y, null, TRANSPARENCY_NONE, 0);
					atarigen_pf_dirty.write(offs, bank);
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
        
	
	/*************************************
	 *
	 *	Playfield overrendering
	 *
	 *************************************/
	
	public static atarigen_pf_callback pf_overrender_callback = new atarigen_pf_callback() {
            @Override
            public void handler(rectangle clip, rectangle tiles, atarigen_pf_state state, Object param) {
                IntSubArray lookup_table = new IntSubArray(pf_lookup, state.param[0]);
		pf_overrender_data overrender_data = (pf_overrender_data) param;
		osd_bitmap bitmap = overrender_data.bitmap;
		int type = overrender_data.type;
		int x, y;
	
		/* standard loop over tiles */
		for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
		{
			int sy = (8 * y - state.vscroll) & 0x1ff;
			if (sy >= YDIM) sy -= 0x200;
	
			for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
			{
				int offs = y * 64 + x;
				int data = atarigen_playfieldram.READ_WORD(offs * 2);
				int lookup = lookup_table.read((data >> 8) & 0x7f);
				GfxElement gfx = Machine.gfx[LOOKUP_GFX(lookup)];
				int code = LOOKUP_CODE(lookup) | (data & 0xff);
				int color = LOOKUP_COLOR(lookup);
				int hflip = data & 0x8000;
	
				int sx = (8 * x - state.hscroll) & 0x1ff;
				if (sx >= XDIM) sx -= 0x200;
	
				/* overrender based on the type */
				if (type == OVERRENDER_PRIORITY)
				{
					int bpp = LOOKUP_BPP(lookup);
					if (color == (16 >> bpp))
						drawgfx(bitmap, gfx, code, color, hflip, 0, sx, sy, clip, TRANSPARENCY_PENS, ~priority_pens);
				}
				else
					drawgfx(bitmap, gfx, code, color, hflip, 0, sx, sy, clip, TRANSPARENCY_PEN, 0);
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
                int[] colormap = (int[]) param;
		int temp = 0;
		int i;
	
		int lookup = mo_lookup[ data.read(1) >> 8 ];
		int[] usage = pen_usage[LOOKUP_GFX(lookup)];
		int code = LOOKUP_CODE(lookup) | (data.read(1) & 0xff);
		int color = LOOKUP_COLOR(lookup);
		int vsize = (data.read(0) & 0x000f) + 1;
		int bpp = LOOKUP_BPP(lookup);
	
		if (bpp == 0)
		{
			for (i = 0; i < vsize; i++)
				temp |= usage[code++];
			colormap[color] |= temp;
		}
	
		/* in theory we should support all 3 possible depths, but motion objects are all 4bpp */
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
                osd_bitmap bitmap = (osd_bitmap) param;
		pf_overrender_data overrender_data = new pf_overrender_data();
		rectangle pf_clip = new rectangle();
	
		/* extract data from the various words */
		int lookup = mo_lookup[data.read(1) >> 8];
		GfxElement gfx = Machine.gfx[LOOKUP_GFX(lookup)];
		int code = LOOKUP_CODE(lookup) | (data.read(1) & 0xff);
		int color = LOOKUP_COLOR(lookup);
		int xpos = data.read(2) >> 5;
		int ypos = 256 - (data.read(0) >> 5);
		int hflip = data.read(0) & 0x8000;
		int vsize = (data.read(0) & 0x000f) + 1;
		int priority = data.read(2) >> 15;
	
		/* adjust for height */
		ypos -= vsize * 8;
	
		/* adjust the final coordinates */
		xpos &= 0x1ff;
		ypos &= 0x1ff;
		if (xpos >= XDIM) xpos -= 0x200;
		if (ypos >= YDIM) ypos -= 0x200;
	
		/* bail if X coordinate is out of range */
		if (xpos <= -8 || xpos >= XDIM)
			return;
	
		/* determine the bounding box */
		pf_clip = atarigen_mo_compute_clip_8x8(xpos, ypos, 1, vsize, clip);
	
		/* standard priority case? */
		if (priority==0)
		{
			/* draw the motion object */
			atarigen_mo_draw_8x8_strip(bitmap, gfx, code, color, hflip, 0, xpos, ypos, vsize, clip, TRANSPARENCY_PEN, 0);
	
			/* do we have a priority color active? */
			if (priority_pens != 0)
			{
				overrender_data.bitmap = bitmap;
				overrender_data.type = OVERRENDER_PRIORITY;
	
				/* overrender the playfield */
				atarigen_pf_process(pf_overrender_callback, overrender_data, pf_clip);
			}
		}
	
		/* high priority case? */
		else
		{
			/* draw the sprite in bright pink on the real bitmap */
			atarigen_mo_draw_transparent_8x8_strip(bitmap, gfx, code, hflip, 0, xpos, ypos, vsize, clip, TRANSPARENCY_PEN, 0);
	
			/* also draw the sprite normally on the temp bitmap */
			atarigen_mo_draw_8x8_strip(atarigen_pf_overrender_bitmap, gfx, code, 0x20, hflip, 0, xpos, ypos, vsize, clip, TRANSPARENCY_NONE, 0);
	
			/* now redraw the playfield tiles over top of the sprite */
			overrender_data.bitmap = atarigen_pf_overrender_bitmap;
			overrender_data.type = OVERRENDER_SPECIAL;
			atarigen_pf_process(pf_overrender_callback, overrender_data, pf_clip);
	
			/* finally, copy this chunk to the real bitmap */
			copybitmap(bitmap, atarigen_pf_overrender_bitmap, 0, 0, 0, 0, pf_clip, TRANSPARENCY_THROUGH, palette_transparent_pen);
		}
            }
        };
        
	
	/*************************************
	 *
	 *	Graphics decoding
	 *
	 *************************************/
	
	static int decode_gfx()
	{
		UBytePtr prom1 = new UBytePtr(memory_region(REGION_PROMS), 0x000);
		UBytePtr prom2 = new UBytePtr(memory_region(REGION_PROMS), 0x200);
		int obj, i;
	
		/* reset the globals */
		//memset(&bank_gfx, 0, sizeof(bank_gfx));
                bank_gfx = new int[3][8];
	
		/* loop for two sets of objects */
		for (obj = 0; obj < 2; obj++)
		{
			IntSubArray table = (obj == 0) ? new IntSubArray(pf_lookup) : new IntSubArray(mo_lookup);
	
			/* loop for 256 objects in the set */
			for (i = 0; i < 256; i++, prom1.inc(), prom2.inc())
			{
				int bank, bpp, color, offset;
	
				/* determine the bpp */
				bpp = 4;
				if ((prom2.read() & PROM2_PLANE_4_ENABLE) != 0)
				{
					bpp = 5;
					if ((prom2.read() & PROM2_PLANE_5_ENABLE) != 0)
						bpp = 6;
				}
	
				/* determine the color */
				if (obj == 0)
					color = (16 + (~prom2.read() & PROM2_PF_COLOR_MASK)) >> (bpp - 4); /* playfield */
				else
					color = (~prom2.read() & PROM2_MO_COLOR_MASK) >> (bpp - 4);	/* motion objects (high bit ignored) */
	
				/* determine the offset */
				offset = prom1.read() & PROM1_OFFSET_MASK;
	
				/* determine the bank */
				bank = get_bank(prom1.read(), prom2.read(), bpp);
				if (bank < 0)
					return 1;
	
				/* set the value */
				if (bank == 0){
					table.write( 0 );
                                        table.inc(1);
                                } else {
					table.write( PACK_LOOKUP_DATA(bank, color, offset, bpp) );
                                        table.inc(1);
                                }
			}
		}
		return 0;
	}
	
	
	
	/*************************************
	 *
	 *	Graphics bank mapping
	 *
	 *************************************/
	
	static int get_bank(int prom1, int prom2, int bpp)
	{
		int bank_offset[] = { 0, 0x00000, 0x30000, 0x60000, 0x90000, 0xc0000, 0xe0000, 0x100000 };
		int bank_index, i, gfx_index;
	
		/* determine the bank index */
		if ((prom1 & PROM1_BANK_1) == 0)
			bank_index = 1;
		else if ((prom1 & PROM1_BANK_2) == 0)
			bank_index = 2;
		else if ((prom1 & PROM1_BANK_3) == 0)
			bank_index = 3;
		else if ((prom1 & PROM1_BANK_4) == 0)
			bank_index = 4;
		else if ((prom2 & PROM2_BANK_5) == 0)
			bank_index = 5;
		else if ((prom2 & PROM2_BANK_6_OR_7) == 0)
		{
			if ((prom2 & PROM2_BANK_7) == 0)
				bank_index = 7;
			else
				bank_index = 6;
		}
		else
			return 0;
	
		/* find the bank */
		if (bank_gfx[bpp - 4][bank_index] != 0)
			return bank_gfx[bpp - 4][bank_index];
	
		/* if the bank is out of range, call it 0 */
		if (bank_offset[bank_index] >= memory_region_length(REGION_GFX2))
			return 0;
	
		/* don't have one? let's make it ... first find any empty slot */
		for (gfx_index = 0; gfx_index < MAX_GFX_ELEMENTS; gfx_index++)
			if (Machine.gfx[gfx_index] == null)
				break;
		if (gfx_index == MAX_GFX_ELEMENTS)
			return -1;
	
		/* tweak the structure for the number of bitplanes we have */
		objlayout.planes = bpp;
		for (i = 0; i < bpp; i++)
			objlayout.planeoffset[i] = (bpp - i - 1) * 0x8000 * 8;
	
		/* decode the graphics */
		Machine.gfx[gfx_index] = decodegfx(new UBytePtr(memory_region(REGION_GFX2), bank_offset[bank_index]), objlayout);
		if (Machine.gfx[gfx_index] == null)
			return -1;
	
		/* set the color information */
		Machine.gfx[gfx_index].colortable = new UShortArray(Machine.remapped_colortable, 256);
		Machine.gfx[gfx_index].total_colors = 48 >> (bpp - 4);
	
		/* set the entry and return it */
		return bank_gfx[bpp - 4][bank_index] = gfx_index;
	}
}
