/***************************************************************************

	vidhrdw/hydra.c

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
			Bits  0-3  = height in tiles

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
        
import arcadeflex.v036.generic.funcPtr.TimerCallbackHandlerPtr;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import gr.codebb.arcadeflex.common.SubArrays.IntSubArray;
import gr.codebb.arcadeflex.common.SubArrays.UShortArray;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v037b7.machine.atarigen.*;
import static gr.codebb.arcadeflex.v037b7.machine.atarigenH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import gr.codebb.arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;

public class hydra
{
	
	/* Note: if this is set to 1, it must also be set in the driver */
	public static final int HIGH_RES    = 0;
	
	public static final int XCHARS      = 42;
	public static final int YCHARS      = 30;
	
	public static final int XDIM        = (XCHARS*(8 << HIGH_RES));
	public static final int YDIM        = (YCHARS*8);
	
	
/*TODO*///	#define DEBUG_VIDEO 0
	
	
	
	/*************************************
	 *
	 *	Globals
	 *
	 *************************************/
	
	public static rectangle hydra_mo_area = new rectangle();
	public static int hydra_mo_priority_offset;
	public static int hydra_pf_xoffset;
	
	
	
	/*************************************
	 *
	 *	Structures
	 *
	 *************************************/
	
	public static class mo_params
	{
		public int xhold;
		public osd_bitmap bitmap;
	};
	
	
	public static class mo_sort_entry
	{
		public mo_sort_entry next;
		public int entry;
	};
	
	
	
	/*************************************
	 *
	 *	Statics
	 *
	 *************************************/
	
	public static atarigen_pf_state pf_state;
	public static int current_control;
	
	
	/*************************************
	 *
	 *	Video system start
	 *
	 *************************************/
	public static atarigen_pf_desc pf_desc = new atarigen_pf_desc
        (
                8 << HIGH_RES, 8,	/* width/height of each tile */
                64, 64,				/* number of tiles in each direction */
                0
        );
        
	public static VhStartPtr hydra_vh_start = new VhStartPtr() { public int handler() 
	{
		
	
	
		/* reset statics */
		pf_state = new atarigen_pf_state();
		current_control = 0;
	
		/* add the top bit to the playfield graphics */
		if (Machine.gfx[0] != null)
		{
			UBytePtr src = new UBytePtr(memory_region(REGION_GFX1), 0x80000);
			IntSubArray pen_usage = new IntSubArray(Machine.gfx[0].pen_usage);
			int n, h, w;
			UBytePtr dst = new UBytePtr(Machine.gfx[0].gfxdata);
	
			for (n = 0; n < Machine.gfx[0].total_elements; n ++)
			{
				int usage = 0;
	
				for (h = 0; h < 8; h++)
				{
					int bits = src.readinc();
	
					for (w = 0; w < 8; w++, dst.inc( (1 << HIGH_RES)), bits <<= 1)
					{
						dst.write(0, (dst.read(0) & 0x0f) | ((bits >> 3) & 0x10));
						if (HIGH_RES != 0)
							dst.write(1, (dst.read(1) & 0x0f) | ((bits >> 3) & 0x10));
						usage |= 1 << dst.read(0);
					}
				}
	
				/* update the tile's pen usage */
				if (pen_usage != null){
					pen_usage.write( usage );
                                        pen_usage.inc(1);
                                }
			}
		}
	
		/* decode the motion objects */
		if (atarigen_rle_init(REGION_GFX3, 0x200) != 0)
			return 1;
	
		/* initialize the playfield */
		if (atarigen_pf_init(pf_desc) != 0)
		{
			atarigen_rle_free();
			return 1;
		}
	
		return 0;
	} };
	
	
	
	/*************************************
	 *
	 *	Video system shutdown
	 *
	 *************************************/
	
	public static VhStopPtr hydra_vh_stop = new VhStopPtr() { public void handler() 
	{
		atarigen_rle_free();
		atarigen_pf_free();
	} };
	
	
	
	/*************************************
	 *
	 *	Playfield RAM write handler
	 *
	 *************************************/
	
	public static WriteHandlerPtr hydra_playfieldram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = atarigen_playfieldram.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword, data);
	
		if (oldword != newword)
		{
			atarigen_playfieldram.WRITE_WORD(offset, newword);
			atarigen_pf_dirty.write((offset / 2) & 0xfff, 0xff);
		}
	} };
	
	
	
	/*************************************
	 *
	 *	Periodic scanline updater
	 *
	 *************************************/
	
	public static WriteHandlerPtr hydra_mo_control_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		logerror("MOCONT = %d (scan = %d)\n", data, cpu_getscanline());
	
		/* set the control value */
		current_control = data;
	} };
	
	
	public static TimerCallbackHandlerPtr hydra_scanline_update = new TimerCallbackHandlerPtr() {
            @Override
            public void handler(int scanline) {
                UShortArray base = new UShortArray(atarigen_alpharam, ((scanline / 8) * 64 + 47) * 2);
		int i;
	
		if (scanline == 0) logerror("-------\n");
	
		/* keep in range */
		//if ((UINT8 *)base >= &atarigen_alpharam[atarigen_alpharam_size])
                if (base.offset>=atarigen_alpharam_size[0])
			return;
	
		/* update the current parameters */
		for (i = 0; i < 8; i++)
		{
			int word;
	
			word = base.read(i * 2 + 1);
			if ((word & 0x8000) != 0)
				pf_state.hscroll = (((word >> 6) + hydra_pf_xoffset) & 0x1ff) << HIGH_RES;
	
			word = base.read(i * 2 + 2);
			if ((word & 0x8000) != 0)
			{
				/* a new vscroll latches the offset into a counter; we must adjust for this */
				int offset = scanline + i;
				if (offset >= 256)
					offset -= 256;
				pf_state.vscroll = ((word >> 6) - offset) & 0x1ff;
	
				pf_state.param[0] = word & 7;
			}
	
			/* update the playfield with the new parameters */
			atarigen_pf_update(pf_state, scanline + i);
		}
            }
        };
        
	
	/*************************************
	 *
	 *	Main refresh
	 *
	 *************************************/
	static int mo_checksum[] =
        {
                0xc289, 0x3103, 0x2b8d, 0xe048, 0xc12e, 0x0ede, 0x2cd7, 0x7dc8,
                0x58fc, 0xb877, 0x9449, 0x59d4, 0x8b63, 0x241b, 0xa3de, 0x4724
        };
        
	public static VhUpdatePtr hydra_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		/* 	a note about this: I don't see how to compute the MO ROM checksums, so these
			are just the values Pit Fighter is expecting. Hydra never checks. */
		
	
		mo_sort_entry[] sort_entry=new mo_sort_entry[256];
		mo_sort_entry[] list_head=new mo_sort_entry[256];
		mo_sort_entry current;
	
		mo_params modata=new mo_params();
		GfxElement gfx;
		int x, y, offs;
	
/*TODO*///	#if DEBUG_VIDEO
/*TODO*///		int xorval = debug();
/*TODO*///	#endif
	
		/* special case: checksum the sprite ROMs */
		if (atarigen_spriteram.READ_WORD(0) == 0x000f)
		{
			for (x = 1; x < 5; x++)
				if (atarigen_spriteram.READ_WORD(x * 2) != 0)
					break;
			if (x == 5)
			{
				logerror("Wrote checksums\n");
				for (x = 0; x < 16; x++)
					atarigen_spriteram.WRITE_WORD(x * 2, mo_checksum[x]);
			}
		}
	
		/* update the palette, and mark things dirty */
		if (update_palette() != null)
			memset(atarigen_pf_dirty, 0xff, atarigen_playfieldram_size[0] / 2);
	
		/* draw the playfield */
		memset(atarigen_pf_visit, 0, 64*64);
		atarigen_pf_process(pf_render_callback, bitmap, Machine.visible_area);
	
		/* draw the motion objects */
		modata.xhold = 1000;
		modata.bitmap = bitmap;
	
		/* sort the motion objects into their proper priorities */
		//memset(list_head, 0, list_head.length);
                for (int _i=0 ; _i<256 ; _i++)
                    list_head[_i] = new mo_sort_entry();
                    
		for (x = 0; x < 256; x++)
		{
			int priority = atarigen_spriteram.READ_WORD(x * 16 + hydra_mo_priority_offset) & 0xff;
                        sort_entry[x] = new mo_sort_entry();
			sort_entry[x].entry = x;
			sort_entry[x].next = list_head[priority];
			list_head[priority] = sort_entry[x];
		}
	
		/* now loop back and process */
		for (x = 1; x < 256; x++)
			for (current = list_head[x]; current!=null; current = current.next)
				mo_render_callback.handler(new UShortArray(atarigen_spriteram, current.entry * 16), hydra_mo_area, modata);
	
		/* draw the alphanumerics */
		gfx = Machine.gfx[1];
		for (y = 0; y < YCHARS; y++)
			for (x = 0, offs = y * 64; x < XCHARS; x++, offs++)
			{
				int data = atarigen_alpharam.READ_WORD(offs * 2);
				int code = data & 0xfff;
				int color = (data >> 12) & 15;
				int opaque = data & 0x8000;
				drawgfx(bitmap, gfx, code, color, 0, 0, (8 << HIGH_RES) * x, 8 * y, null, opaque!=0 ? TRANSPARENCY_NONE : TRANSPARENCY_PEN, 0);
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
		int[] mo_map=new int[16+4], al_map=new int[16];
		int[] pf_map=new int[8];
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
		for (j = 0; j < 256; j++)
		{
			int priority = atarigen_spriteram.READ_WORD(j * 16 + hydra_mo_priority_offset) & 0xff;
			if (priority != 0)
				mo_color_callback.handler(new UShortArray(atarigen_spriteram, j * 16), Machine.visible_area, mo_map);
		}
	
		/* update color usage for the alphanumerics */
		usage = Machine.gfx[1].pen_usage;
		for (y = 0; y < YCHARS; y++)
			for (x = 0, offs = y * 64; x < XCHARS; x++, offs++)
			{
				int data = atarigen_alpharam.READ_WORD(offs * 2);
				int code = data & 0xfff;
				int color = (data >> 12) & 15;
				al_map[color] |= usage[code];
			}
	
		/* rebuild the playfield palette */
		for (i = 0; i < 8; i++)
		{
			int used = pf_map[i];
			if (used != 0)
				for (j = 0; j < 32; j++)
					if ((used & (1 << j)) != 0)
						palette_used_colors.write(0x300 + i * 32 + j, PALETTE_COLOR_USED);
		}
	
		/* rebuild the motion object palette */
		for (i = 0; i < 16; i++)
		{
			int used = mo_map[i];
			if (used != 0)
			{
				for (j = 0; j < 16; j++)
					if ((used & (1 << j)) != 0)
						palette_used_colors.write(0x200 + i * 16 + j, PALETTE_COLOR_USED);
			}
		}
	
		/* rebuild the alphanumerics palette */
		for (i = 0; i < 16; i++)
		{
			int used = al_map[i];
			if (used != 0)
			{
				if (i < 8)
					palette_used_colors.write(0x100 + i * 16 + 0, PALETTE_COLOR_TRANSPARENT);
				else if ((used & 0x0001) != 0)
					palette_used_colors.write(0x100 + i * 16 + 0, PALETTE_COLOR_USED);
				for (j = 1; j < 16; j++)
					if ((used & (1 << j)) != 0)
						palette_used_colors.write(0x100 + i * 16 + j, PALETTE_COLOR_USED);
			}
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
                int[] usage = Machine.gfx[0].pen_usage;
		int[] colormap = (int[])param;
		int bankbase = state.param[0] * 0x1000;
		int x, y;
	
		for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
			for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
			{
				int offs = y * 64 + x;
				int data = atarigen_playfieldram.READ_WORD(offs * 2);
				int code = bankbase + (data & 0x0fff);
				int color = (data >> 12) & 7;
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
	
	static atarigen_pf_callback pf_render_callback = new atarigen_pf_callback() {
            @Override
            public void handler(rectangle clip, rectangle tiles, atarigen_pf_state state, Object param) {
		GfxElement gfx = Machine.gfx[0];
		int bankbase = state.param[0] * 0x1000;
		osd_bitmap bitmap = (osd_bitmap) param;
		int x, y;
	
		/* first update any tiles whose color is out of date */
		for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
			for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
			{
				int offs = y * 64 + x;
				int data = atarigen_playfieldram.READ_WORD(offs * 2);
				int color = (data >> 12) & 7;
	
				if (atarigen_pf_dirty.read(offs) != state.param[0])
				{
					int code = bankbase + (data & 0x0fff);
					int hflip = data & 0x8000;
	
					drawgfx(atarigen_pf_bitmap, gfx, code, color, hflip, 0, (8 << HIGH_RES) * x, 8 * y, null, TRANSPARENCY_NONE, 0);
					atarigen_pf_dirty.write(offs, state.param[0]);
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
	 *	Motion object palette
	 *
	 *************************************/
	
	public static atarigen_mo_callback mo_color_callback = new atarigen_mo_callback() {
            @Override
            public void handler(UShortArray data, rectangle clip, Object param) {
		int[] colormap = (int[]) param;
	
		int scale = data.read(4);
		int code = data.read(0) & 0x7fff;
		if (scale > 0x0000 && code < atarigen_rle_count)
		{
			atarigen_rle_descriptor rle = atarigen_rle_info[code];
			int colorentry = (data.read(1) & 0xff) >> 4;
			int colorshift = (data.read(1) & 0x0f);
			int usage = rle.pen_usage;
			int bpp = rle.bpp;
	
			if (bpp == 4)
			{
				colormap[colorentry + 0] |= usage << colorshift;
				if (colorshift != 0)
					colormap[colorentry + 1] |= usage >> (16 - colorshift);
			}
			else if (bpp == 5)
			{
				colormap[colorentry + 0] |= usage << colorshift;
				colormap[colorentry + 1] |= usage >> (16 - colorshift);
				if (colorshift != 0)
					colormap[colorentry + 2] |= usage >> (32 - colorshift);
			}
			else
			{
				colormap[colorentry + 0] |= usage << colorshift;
				colormap[colorentry + 1] |= usage >> (16 - colorshift);
				if (colorshift != 0)
					colormap[colorentry + 2] |= usage >> (32 - colorshift);
	
				usage = rle.pen_usage_hi;
				colormap[colorentry + 2] |= usage << colorshift;
				colormap[colorentry + 3] |= usage >> (16 - colorshift);
				if (colorshift != 0)
					colormap[colorentry + 4] |= usage >> (32 - colorshift);
			}
		}
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
		int scale = data.read(4);
		int code = data.read(0) & 0x7fff;
		if (scale > 0x0000 && code < atarigen_rle_count)
		{
			mo_params modata = (mo_params) param;
			osd_bitmap bitmap = modata.bitmap;
			int hflip = data.read(0) & 0x8000;
			int color = data.read(1) & 0xff;
			int x = (data.read(2) >> 6);
			int y = (data.read(3) >> 6);
	
			atarigen_rle_render(bitmap, atarigen_rle_info[code], color, hflip, 0, (x << HIGH_RES) + clip.min_x, y, scale << HIGH_RES, scale, clip);
		}
            }
        };
	
	
	
	/*************************************
	 *
	 *	Debugging
	 *
	 *************************************/
	
/*TODO*///	#if DEBUG_VIDEO
/*TODO*///	
/*TODO*///	static void mo_print_callback(struct osd_bitmap *bitmap, struct rectangle *clip, UINT16 *data, void *param)
/*TODO*///	{
/*TODO*///		int code = (data[0] & 0x7fff);
/*TODO*///		int vsize = (data[1] & 15) + 1;
/*TODO*///		int xpos = (data[3] >> 7);
/*TODO*///		int ypos = (data[1] >> 7);
/*TODO*///		int color = data[3] & 15;
/*TODO*///		int hflip = data[0] & 0x8000;
/*TODO*///	
/*TODO*///		FILE *f = (FILE *)param;
/*TODO*///		fprintf(f, "P=%04X X=%03X Y=%03X SIZE=%X COL=%X FLIP=%X  -- DATA=%04X %04X %04X %04X\n",
/*TODO*///				code, xpos, ypos, vsize, color, hflip >> 15, data[0], data[1], data[2], data[3]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static int debug(void)
/*TODO*///	{
/*TODO*///		int hidebank = -1;
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
/*TODO*///	/*		atarigen_mo_process(mo_print_callback, f);*/
/*TODO*///	
/*TODO*///			fprintf(f, "\n\nMotion Objects (control = %d)\n", current_control);
/*TODO*///			for (i = 0; i < 0x100; i++)
/*TODO*///			{
/*TODO*///				fprintf(f, "   Object %03X:  P=%04X  ?=%04X  X=%04X  Y=%04X  S=%04X  ?=%04X  L=%04X  ?=%04X\n",
/*TODO*///						i,
/*TODO*///						READ_WORD(&atarigen_spriteram[i*16+0]),
/*TODO*///						READ_WORD(&atarigen_spriteram[i*16+2]),
/*TODO*///						READ_WORD(&atarigen_spriteram[i*16+4]),
/*TODO*///						READ_WORD(&atarigen_spriteram[i*16+6]),
/*TODO*///						READ_WORD(&atarigen_spriteram[i*16+8]),
/*TODO*///						READ_WORD(&atarigen_spriteram[i*16+10]),
/*TODO*///						READ_WORD(&atarigen_spriteram[i*16+12]),
/*TODO*///						READ_WORD(&atarigen_spriteram[i*16+14])
/*TODO*///				);
/*TODO*///			}
/*TODO*///	
/*TODO*///			fprintf(f, "\n\nPlayfield dump\n");
/*TODO*///			for (i = 0; i < atarigen_playfieldram_size / 2; i++)
/*TODO*///			{
/*TODO*///				fprintf(f, "%04X ", READ_WORD(&atarigen_playfieldram[i*2]));
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
/*TODO*///			fprintf(f, "\n\nMemory dump");
/*TODO*///			for (i = 0xff0000; i < 0xffffff; i += 2)
/*TODO*///			{
/*TODO*///				if ((i & 31) == 0) fprintf(f, "\n%06X: ", i);
/*TODO*///				fprintf(f, "%04X ", READ_WORD(&atarigen_spriteram[i - 0xff0000]));
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
