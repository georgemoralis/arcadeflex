/***************************************************************************

	Atari System 1 hardware

****************************************************************************/


/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.v036.mame.mameH.MAX_GFX_ELEMENTS;
import static gr.codebb.arcadeflex.v036.machine.atarigen.*;
import static gr.codebb.arcadeflex.v036.machine.atarigenH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v037b7.mame.timer.*;
import static gr.codebb.arcadeflex.v037b7.mame.timerH.*;
import gr.codebb.arcadeflex.common.SubArrays.UShortArray;

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
	 *	Prototypes
	 *
	 *************************************/
	
/*TODO*///	static const UINT8 *update_palette(void);
	
/*TODO*///	static void pf_color_callback(const struct rectangle *clip, const struct rectangle *tiles, const struct atarigen_pf_state *state, void *data);
/*TODO*///	static void pf_render_callback(const struct rectangle *clip, const struct rectangle *tiles, const struct atarigen_pf_state *state, void *data);
/*TODO*///	static void pf_overrender_callback(const struct rectangle *clip, const struct rectangle *tiles, const struct atarigen_pf_state *state, void *data);
	
/*TODO*///	static void mo_color_callback(const UINT16 *data, const struct rectangle *clip, void *param);
/*TODO*///	static void mo_render_callback(const UINT16 *data, const struct rectangle *clip, void *param);
	
/*TODO*///	static static int get_bank(UINT8 prom1, UINT8 prom2, int bpp);
	
/*TODO*///	void atarisys1_scanline_update(int scanline);
	
	
	
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
                0                    /* render in reverse link order */
        );

        static atarigen_pf_desc pf_desc = new atarigen_pf_desc
        (
                8, 8,				/* width/height of each tile */
                64, 64				/* number of tiles in each direction */
        );
        
	public static VhStartPtr atarisys1_vh_start = new VhStartPtr() { public int handler() 
	{
		
		int i, e;
	
		/* first decode the graphics */
/*TODO*///		if (decode_gfx() != 0)
/*TODO*///			return 1;
	
		/* reset the statics */
		pf_state = new atarigen_pf_state();
/*TODO*///		memset(int3_timer, 0, sizeof(int3_timer));
	
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
	
	public static VhStopPtr atarisys1_vh_stop = new VhStopPtr() { public void handler() 
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
		atarisys1_scanline_update(scanline);
	
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
					atarisys1_scanline_update(cpu_getscanline());
				}
			}
		}
	} };
	
	
	
	/*************************************
	 *
	 *	MO interrupt handlers
	 *
	 *************************************/
	
	public static timer_callback int3off_callback = new timer_callback() { public void handler(int param) 
	{
		/* clear the state */
		atarigen_scanline_int_ack_w.handler(0, 0);
	
		/* make this timer go away */
		int3off_timer = null;
	} };
	
	
	static timer_callback atarisys1_int3_callback = new timer_callback() {
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
	
	static void atarisys1_scanline_update(int scanline)
	{
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
/*TODO*///			if (scanline==0)
/*TODO*///				atarigen_mo_update(base, 0, 0);
/*TODO*///			else
/*TODO*///				atarigen_mo_update(base, 0, scanline + 1);
		}
	
		/* visit all the sprites and look for timers */
/*TODO*///		memset(spritevisit, 0, sizeof(spritevisit));
/*TODO*///		memset(timer, 0, sizeof(timer));
/*TODO*///		while (spritevisit[link]==0)
/*TODO*///		{
/*TODO*///			int data2 = READ_WORD(&base[link * 2 + 0x080]);
	
			/* a codeure of 0xffff is really an interrupt - gross! */
/*TODO*///			if (data2 == 0xffff)
/*TODO*///			{
/*TODO*///				int data1 = READ_WORD(&base[link * 2 + 0x000]);
/*TODO*///				int vsize = (data1 & 15) + 1;
/*TODO*///				int ypos = (256 - (data1 >> 5) - vsize * 8) & 0x1ff;
	
				/* only generate timers on visible scanlines */
/*TODO*///				if (ypos < YDIM)
/*TODO*///					timer[ypos] = 1;
/*TODO*///			}
	
			/* link to the next object */
/*TODO*///			spritevisit[link] = 1;
/*TODO*///			link = READ_WORD(&atarigen_spriteram[bank + link * 2 + 0x180]) & 0x3f;
/*TODO*///		}
	
		/* update our interrupt timers */
/*TODO*///		for (i = 0; i < YDIM; i++)
/*TODO*///		{
/*TODO*///			if (timer[i] && !int3_timer[i])
/*TODO*///				int3_timer[i] = timer_set(cpu_getscanlinetime(i), i, atarisys1_int3_callback);
/*TODO*///			else if (!timer[i] && int3_timer[i])
/*TODO*///			{
/*TODO*///				timer_remove(int3_timer[i]);
/*TODO*///				int3_timer[i] = 0;
/*TODO*///			}
/*TODO*///		}
	}
/*TODO*///	
	
	
	/*************************************
	 *
	 *	Main refresh
	 *
	 *************************************/
	
/*TODO*///	public static VhUpdatePtr atarisys1_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		/* update the palette */
/*TODO*///		if (update_palette())
/*TODO*///			memset(atarigen_pf_dirty, 0xff, atarigen_playfieldram_size / 2);
/*TODO*///	
/*TODO*///		/* set up the all-transparent overrender palette */
/*TODO*///		for (i = 0; i < 16; i++)
/*TODO*///			atarigen_overrender_colortable[i] = palette_transparent_pen;
/*TODO*///	
/*TODO*///		/* render the playfield */
/*TODO*///		memset(atarigen_pf_visit, 0, 64*64);
/*TODO*///		atarigen_pf_process(pf_render_callback, bitmap, &Machine.visible_area);
/*TODO*///	
/*TODO*///		/* render the motion objects */
/*TODO*///		priority_pens = READ_WORD(&atarisys1_prioritycolor[0]) & 0xff;
/*TODO*///		atarigen_mo_process(mo_render_callback, bitmap);
/*TODO*///	
/*TODO*///		/* redraw the alpha layer completely */
/*TODO*///		{
/*TODO*///			const struct GfxElement *gfx = Machine.gfx[0];
/*TODO*///			int sx, sy, offs;
/*TODO*///	
/*TODO*///			for (sy = 0; sy < YCHARS; sy++)
/*TODO*///				for (sx = 0, offs = sy*64; sx < XCHARS; sx++, offs++)
/*TODO*///				{
/*TODO*///					int data = READ_WORD(&atarigen_alpharam[offs * 2]);
/*TODO*///					int opaque = data & 0x2000;
/*TODO*///					int code = data & 0x3ff;
/*TODO*///	
/*TODO*///					if (code || opaque)
/*TODO*///					{
/*TODO*///						int color = (data >> 10) & 7;
/*TODO*///						drawgfx(bitmap, gfx, code, color, 0, 0, 8 * sx, 8 * sy, 0,
/*TODO*///								opaque ? TRANSPARENCY_NONE : TRANSPARENCY_PEN, 0);
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
/*TODO*///		UINT16 al_map[8], pfmo_map[32];
/*TODO*///		int i, j;
/*TODO*///	
/*TODO*///		/* reset color tracking */
/*TODO*///		memset(pfmo_map, 0, sizeof(pfmo_map));
/*TODO*///		memset(al_map, 0, sizeof(al_map));
/*TODO*///		palette_init_used_colors();
/*TODO*///	
/*TODO*///		/* always remap the transluscent colors */
/*TODO*///		memset(&palette_used_colors[0x300], PALETTE_COLOR_USED, 16);
/*TODO*///	
/*TODO*///		/* update color usage for the playfield */
/*TODO*///		atarigen_pf_process(pf_color_callback, pfmo_map, &Machine.visible_area);
/*TODO*///	
/*TODO*///		/* update color usage for the mo's */
/*TODO*///		atarigen_mo_process(mo_color_callback, pfmo_map);
/*TODO*///	
/*TODO*///		/* update color usage for the alphanumerics */
/*TODO*///		{
/*TODO*///			const unsigned int *usage = Machine.gfx[0].pen_usage;
/*TODO*///			int sx, sy, offs;
/*TODO*///	
/*TODO*///			for (sy = 0; sy < YCHARS; sy++)
/*TODO*///				for (sx = 0, offs = sy * 64; sx < XCHARS; sx++, offs++)
/*TODO*///				{
/*TODO*///					int data = READ_WORD(&atarigen_alpharam[offs * 2]);
/*TODO*///					int color = (data >> 10) & 7;
/*TODO*///					int code = data & 0x3ff;
/*TODO*///					al_map[color] |= usage[code];
/*TODO*///				}
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* determine the final playfield palette */
/*TODO*///		for (i = 16; i < 32; i++)
/*TODO*///		{
/*TODO*///			UINT16 used = pfmo_map[i];
/*TODO*///			if (used != 0)
/*TODO*///				for (j = 0; j < 16; j++)
/*TODO*///					if (used & (1 << j))
/*TODO*///						palette_used_colors[0x100 + i * 16 + j] = PALETTE_COLOR_USED;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* determine the final motion object palette */
/*TODO*///		for (i = 0; i < 16; i++)
/*TODO*///		{
/*TODO*///			UINT16 used = pfmo_map[i];
/*TODO*///			if (used != 0)
/*TODO*///			{
/*TODO*///				palette_used_colors[0x100 + i * 16] = PALETTE_COLOR_TRANSPARENT;
/*TODO*///				for (j = 1; j < 16; j++)
/*TODO*///					if (used & (1 << j))
/*TODO*///						palette_used_colors[0x100 + i * 16 + j] = PALETTE_COLOR_USED;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* determine the final alpha palette */
/*TODO*///		for (i = 0; i < 8; i++)
/*TODO*///		{
/*TODO*///			UINT16 used = al_map[i];
/*TODO*///			if (used != 0)
/*TODO*///				for (j = 0; j < 4; j++)
/*TODO*///					if (used & (1 << j))
/*TODO*///						palette_used_colors[0x000 + i * 4 + j] = PALETTE_COLOR_USED;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* recalculate the palette */
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
/*TODO*///		const UINT32 *lookup_table = &pf_lookup[state.param[0]];
/*TODO*///		UINT16 *colormap = param;
/*TODO*///		int x, y;
/*TODO*///	
/*TODO*///		/* standard loop over tiles */
/*TODO*///		for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
/*TODO*///			for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
/*TODO*///			{
/*TODO*///				int offs = y * 64 + x;
/*TODO*///				int data = READ_WORD(&atarigen_playfieldram[offs * 2]);
/*TODO*///				int lookup = lookup_table[(data >> 8) & 0x7f];
/*TODO*///				const unsigned int *usage = pen_usage[LOOKUP_GFX(lookup)];
/*TODO*///				int bpp = LOOKUP_BPP(lookup);
/*TODO*///				int code = LOOKUP_CODE(lookup) | (data & 0xff);
/*TODO*///				int color = LOOKUP_COLOR(lookup);
/*TODO*///				unsigned int bits;
/*TODO*///	
/*TODO*///				/* based on the depth, we need to tweak our pen mapping */
/*TODO*///				if (bpp == 0)
/*TODO*///					colormap[color] |= usage[code];
/*TODO*///				else if (bpp == 1)
/*TODO*///				{
/*TODO*///					bits = usage[code];
/*TODO*///					colormap[color * 2] |= bits;
/*TODO*///					colormap[color * 2 + 1] |= bits >> 16;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					bits = usage[code * 2];
/*TODO*///					colormap[color * 4] |= bits;
/*TODO*///					colormap[color * 4 + 1] |= bits >> 16;
/*TODO*///					bits = usage[code * 2 + 1];
/*TODO*///					colormap[color * 4 + 2] |= bits;
/*TODO*///					colormap[color * 4 + 3] |= bits >> 16;
/*TODO*///				}
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
/*TODO*///		int bank = state.param[0];
/*TODO*///		const UINT32 *lookup_table = &pf_lookup[bank];
/*TODO*///		struct osd_bitmap *bitmap = param;
/*TODO*///		int x, y;
/*TODO*///	
/*TODO*///		/* standard loop over tiles */
/*TODO*///		for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
/*TODO*///			for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
/*TODO*///			{
/*TODO*///				int offs = y * 64 + x;
/*TODO*///	
/*TODO*///				/* update only if dirty */
/*TODO*///				if (atarigen_pf_dirty[offs] != bank)
/*TODO*///				{
/*TODO*///					int data = READ_WORD(&atarigen_playfieldram[offs * 2]);
/*TODO*///					int lookup = lookup_table[(data >> 8) & 0x7f];
/*TODO*///					const struct GfxElement *gfx = Machine.gfx[LOOKUP_GFX(lookup)];
/*TODO*///					int code = LOOKUP_CODE(lookup) | (data & 0xff);
/*TODO*///					int color = LOOKUP_COLOR(lookup);
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
/*TODO*///		const UINT32 *lookup_table = &pf_lookup[state.param[0]];
/*TODO*///		const struct pf_overrender_data *overrender_data = param;
/*TODO*///		struct osd_bitmap *bitmap = overrender_data.bitmap;
/*TODO*///		int type = overrender_data.type;
/*TODO*///		int x, y;
/*TODO*///	
/*TODO*///		/* standard loop over tiles */
/*TODO*///		for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
/*TODO*///		{
/*TODO*///			int sy = (8 * y - state.vscroll) & 0x1ff;
/*TODO*///			if (sy >= YDIM) sy -= 0x200;
/*TODO*///	
/*TODO*///			for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
/*TODO*///			{
/*TODO*///				int offs = y * 64 + x;
/*TODO*///				int data = READ_WORD(&atarigen_playfieldram[offs * 2]);
/*TODO*///				int lookup = lookup_table[(data >> 8) & 0x7f];
/*TODO*///				const struct GfxElement *gfx = Machine.gfx[LOOKUP_GFX(lookup)];
/*TODO*///				int code = LOOKUP_CODE(lookup) | (data & 0xff);
/*TODO*///				int color = LOOKUP_COLOR(lookup);
/*TODO*///				int hflip = data & 0x8000;
/*TODO*///	
/*TODO*///				int sx = (8 * x - state.hscroll) & 0x1ff;
/*TODO*///				if (sx >= XDIM) sx -= 0x200;
/*TODO*///	
/*TODO*///				/* overrender based on the type */
/*TODO*///				if (type == OVERRENDER_PRIORITY)
/*TODO*///				{
/*TODO*///					int bpp = LOOKUP_BPP(lookup);
/*TODO*///					if (color == (16 >> bpp))
/*TODO*///						drawgfx(bitmap, gfx, code, color, hflip, 0, sx, sy, clip, TRANSPARENCY_PENS, ~priority_pens);
/*TODO*///				}
/*TODO*///				else
/*TODO*///					drawgfx(bitmap, gfx, code, color, hflip, 0, sx, sy, clip, TRANSPARENCY_PEN, 0);
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
/*TODO*///		UINT16 *colormap = param;
/*TODO*///		UINT16 temp = 0;
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		UINT32 lookup = mo_lookup[data[1] >> 8];
/*TODO*///		const unsigned int *usage = pen_usage[LOOKUP_GFX(lookup)];
/*TODO*///		int code = LOOKUP_CODE(lookup) | (data[1] & 0xff);
/*TODO*///		int color = LOOKUP_COLOR(lookup);
/*TODO*///		int vsize = (data[0] & 0x000f) + 1;
/*TODO*///		int bpp = LOOKUP_BPP(lookup);
/*TODO*///	
/*TODO*///		if (bpp == 0)
/*TODO*///		{
/*TODO*///			for (i = 0; i < vsize; i++)
/*TODO*///				temp |= usage[code++];
/*TODO*///			colormap[color] |= temp;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* in theory we should support all 3 possible depths, but motion objects are all 4bpp */
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
/*TODO*///		struct osd_bitmap *bitmap = param;
/*TODO*///		struct pf_overrender_data overrender_data;
/*TODO*///		struct rectangle pf_clip;
/*TODO*///	
/*TODO*///		/* extract data from the various words */
/*TODO*///		UINT32 lookup = mo_lookup[data[1] >> 8];
/*TODO*///		struct GfxElement *gfx = Machine.gfx[LOOKUP_GFX(lookup)];
/*TODO*///		int code = LOOKUP_CODE(lookup) | (data[1] & 0xff);
/*TODO*///		int color = LOOKUP_COLOR(lookup);
/*TODO*///		int xpos = data[2] >> 5;
/*TODO*///		int ypos = 256 - (data[0] >> 5);
/*TODO*///		int hflip = data[0] & 0x8000;
/*TODO*///		int vsize = (data[0] & 0x000f) + 1;
/*TODO*///		int priority = data[2] >> 15;
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
/*TODO*///		/* bail if X coordinate is out of range */
/*TODO*///		if (xpos <= -8 || xpos >= XDIM)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* determine the bounding box */
/*TODO*///		atarigen_mo_compute_clip_8x8(pf_clip, xpos, ypos, 1, vsize, clip);
/*TODO*///	
/*TODO*///		/* standard priority case? */
/*TODO*///		if (!priority)
/*TODO*///		{
/*TODO*///			/* draw the motion object */
/*TODO*///			atarigen_mo_draw_8x8_strip(bitmap, gfx, code, color, hflip, 0, xpos, ypos, vsize, clip, TRANSPARENCY_PEN, 0);
/*TODO*///	
/*TODO*///			/* do we have a priority color active? */
/*TODO*///			if (priority_pens != 0)
/*TODO*///			{
/*TODO*///				overrender_data.bitmap = bitmap;
/*TODO*///				overrender_data.type = OVERRENDER_PRIORITY;
/*TODO*///	
/*TODO*///				/* overrender the playfield */
/*TODO*///				atarigen_pf_process(pf_overrender_callback, &overrender_data, &pf_clip);
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* high priority case? */
/*TODO*///		else
/*TODO*///		{
/*TODO*///			/* draw the sprite in bright pink on the real bitmap */
/*TODO*///			atarigen_mo_draw_transparent_8x8_strip(bitmap, gfx, code, hflip, 0, xpos, ypos, vsize, clip, TRANSPARENCY_PEN, 0);
/*TODO*///	
/*TODO*///			/* also draw the sprite normally on the temp bitmap */
/*TODO*///			atarigen_mo_draw_8x8_strip(atarigen_pf_overrender_bitmap, gfx, code, 0x20, hflip, 0, xpos, ypos, vsize, clip, TRANSPARENCY_NONE, 0);
/*TODO*///	
/*TODO*///			/* now redraw the playfield tiles over top of the sprite */
/*TODO*///			overrender_data.bitmap = atarigen_pf_overrender_bitmap;
/*TODO*///			overrender_data.type = OVERRENDER_SPECIAL;
/*TODO*///			atarigen_pf_process(pf_overrender_callback, &overrender_data, &pf_clip);
/*TODO*///	
/*TODO*///			/* finally, copy this chunk to the real bitmap */
/*TODO*///			copybitmap(bitmap, atarigen_pf_overrender_bitmap, 0, 0, 0, 0, &pf_clip, TRANSPARENCY_THROUGH, palette_transparent_pen);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Graphics decoding
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static int decode_gfx()
/*TODO*///	{
/*TODO*///		UBytePtr prom1 = new UBytePtr(memory_region(REGION_PROMS), 0x000);
/*TODO*///		UBytePtr prom2 = new UBytePtr(memory_region(REGION_PROMS), 0x200);
/*TODO*///		int obj, i;
/*TODO*///	
/*TODO*///		/* reset the globals */
/*TODO*///		memset(&bank_gfx, 0, sizeof(bank_gfx));
/*TODO*///	
/*TODO*///		/* loop for two sets of objects */
/*TODO*///		for (obj = 0; obj < 2; obj++)
/*TODO*///		{
/*TODO*///			UINT32 *table = (obj == 0) ? pf_lookup : mo_lookup;
/*TODO*///	
/*TODO*///			/* loop for 256 objects in the set */
/*TODO*///			for (i = 0; i < 256; i++, prom1++, prom2++)
/*TODO*///			{
/*TODO*///				int bank, bpp, color, offset;
/*TODO*///	
/*TODO*///				/* determine the bpp */
/*TODO*///				bpp = 4;
/*TODO*///				if (*prom2 & PROM2_PLANE_4_ENABLE)
/*TODO*///				{
/*TODO*///					bpp = 5;
/*TODO*///					if (*prom2 & PROM2_PLANE_5_ENABLE)
/*TODO*///						bpp = 6;
/*TODO*///				}
/*TODO*///	
/*TODO*///				/* determine the color */
/*TODO*///				if (obj == 0)
/*TODO*///					color = (16 + (~*prom2 & PROM2_PF_COLOR_MASK)) >> (bpp - 4); /* playfield */
/*TODO*///				else
/*TODO*///					color = (~*prom2 & PROM2_MO_COLOR_MASK) >> (bpp - 4);	/* motion objects (high bit ignored) */
/*TODO*///	
/*TODO*///				/* determine the offset */
/*TODO*///				offset = *prom1 & PROM1_OFFSET_MASK;
/*TODO*///	
/*TODO*///				/* determine the bank */
/*TODO*///				bank = get_bank(*prom1, *prom2, bpp);
/*TODO*///				if (bank < 0)
/*TODO*///					return 1;
/*TODO*///	
/*TODO*///				/* set the value */
/*TODO*///				if (bank == 0)
/*TODO*///					*table++ = 0;
/*TODO*///				else
/*TODO*///					*table++ = PACK_LOOKUP_DATA(bank, color, offset, bpp);
/*TODO*///			}
/*TODO*///		}
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Graphics bank mapping
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static int get_bank(UINT8 prom1, UINT8 prom2, int bpp)
/*TODO*///	{
/*TODO*///		int bank_offset[8] = { 0, 0x00000, 0x30000, 0x60000, 0x90000, 0xc0000, 0xe0000, 0x100000 };
/*TODO*///		int bank_index, i, gfx_index;
/*TODO*///	
/*TODO*///		/* determine the bank index */
/*TODO*///		if ((prom1 & PROM1_BANK_1) == 0)
/*TODO*///			bank_index = 1;
/*TODO*///		else if ((prom1 & PROM1_BANK_2) == 0)
/*TODO*///			bank_index = 2;
/*TODO*///		else if ((prom1 & PROM1_BANK_3) == 0)
/*TODO*///			bank_index = 3;
/*TODO*///		else if ((prom1 & PROM1_BANK_4) == 0)
/*TODO*///			bank_index = 4;
/*TODO*///		else if ((prom2 & PROM2_BANK_5) == 0)
/*TODO*///			bank_index = 5;
/*TODO*///		else if ((prom2 & PROM2_BANK_6_OR_7) == 0)
/*TODO*///		{
/*TODO*///			if ((prom2 & PROM2_BANK_7) == 0)
/*TODO*///				bank_index = 7;
/*TODO*///			else
/*TODO*///				bank_index = 6;
/*TODO*///		}
/*TODO*///		else
/*TODO*///			return 0;
/*TODO*///	
/*TODO*///		/* find the bank */
/*TODO*///		if (bank_gfx[bpp - 4][bank_index] != 0)
/*TODO*///			return bank_gfx[bpp - 4][bank_index];
/*TODO*///	
/*TODO*///		/* if the bank is out of range, call it 0 */
/*TODO*///		if (bank_offset[bank_index] >= memory_region_length(REGION_GFX2))
/*TODO*///			return 0;
/*TODO*///	
/*TODO*///		/* don't have one? let's make it ... first find any empty slot */
/*TODO*///		for (gfx_index = 0; gfx_index < MAX_GFX_ELEMENTS; gfx_index++)
/*TODO*///			if (Machine.gfx[gfx_index] == NULL)
/*TODO*///				break;
/*TODO*///		if (gfx_index == MAX_GFX_ELEMENTS)
/*TODO*///			return -1;
/*TODO*///	
/*TODO*///		/* tweak the structure for the number of bitplanes we have */
/*TODO*///		objlayout.planes = bpp;
/*TODO*///		for (i = 0; i < bpp; i++)
/*TODO*///			objlayout.planeoffset[i] = (bpp - i - 1) * 0x8000 * 8;
/*TODO*///	
/*TODO*///		/* decode the graphics */
/*TODO*///		Machine.gfx[gfx_index] = decodegfx(&memory_region(REGION_GFX2)[bank_offset[bank_index]], &objlayout);
/*TODO*///		if (!Machine.gfx[gfx_index])
/*TODO*///			return -1;
/*TODO*///	
/*TODO*///		/* set the color information */
/*TODO*///		Machine.gfx[gfx_index].colortable = &Machine.remapped_colortable[256];
/*TODO*///		Machine.gfx[gfx_index].total_colors = 48 >> (bpp - 4);
/*TODO*///	
/*TODO*///		/* set the entry and return it */
/*TODO*///		return bank_gfx[bpp - 4][bank_index] = gfx_index;
/*TODO*///	}
}
