/***************************************************************************

  vidhrdw/shuuz.c

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

import arcadeflex.v036.generic.funcPtr.TimerCallbackHandlerPtr;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import gr.codebb.arcadeflex.common.SubArrays.UShortArray;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v037b7.machine.atarigen.*;
import static gr.codebb.arcadeflex.v037b7.machine.atarigenH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;

public class shuuz
{
	
        public static final int XCHARS  = 42;
        public static final int YCHARS  = 30;

	public static final int XDIM    = (XCHARS*8);
        public static final int YDIM    = (YCHARS*8);


/*TODO*///	#define DEBUG_VIDEO 0
	
	
	
	/*************************************
	 *
	 *	Constants
	 *
	 *************************************/
	
        public static final int OVERRENDER_STANDARD = 0;
	public static final int OVERRENDER_PRIORITY = 1;
	
	
	
	/*************************************
	 *
	 *	Structures
	 *
	 *************************************/
	
	public static class pf_overrender_data
	{
		public osd_bitmap bitmap;
		public int type, color;
	};
	
	
	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Statics
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	#if DEBUG_VIDEO
/*TODO*///	static UINT8 show_colors;
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
/*TODO*///	static void pf_render_callback(const struct rectangle *clip, const struct rectangle *tiles, const struct atarigen_pf_state *state, void *data);
/*TODO*///	static void pf_overrender_callback(const struct rectangle *clip, const struct rectangle *tiles, const struct atarigen_pf_state *state, void *data);
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
                0,                    /* render in reverse link order */
                0
        );
	
        public static atarigen_pf_desc pf_desc = new atarigen_pf_desc
        (
                8, 8,				/* width/height of each tile */
                64, 64,				/* number of tiles in each direction */
                1					/* non-scrolling */
        );    
	
	public static VhStartPtr shuuz_vh_start = new VhStartPtr() { public int handler() 
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
	
	public static VhStopPtr shuuz_vh_stop = new VhStopPtr() { public void handler() 
	{
		atarigen_pf_free();
		atarigen_mo_free();
	} };
	
	
	
	/*************************************
	 *
	 *	Playfield RAM write handler
	 *
	 *************************************/
	
	public static WriteHandlerPtr shuuz_playfieldram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = atarigen_playfieldram.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword, data);
	
		/* update the data if different */
		if (oldword != newword)
		{
			atarigen_playfieldram.WRITE_WORD(offset, newword);
			atarigen_pf_dirty.write((offset & 0x1fff) / 2, 1);
		}
	
		/* handle the latch, but only write the upper byte */
		if (offset < 0x2000 && atarigen_video_control_state.latch1 != -1)
			shuuz_playfieldram_w.handler(offset + 0x2000, atarigen_video_control_state.latch1 | 0x00ff0000);
	} };
	
	
	
	/*************************************
	 *
	 *	Periodic scanline updater
	 *
	 *************************************/
	
	public static TimerCallbackHandlerPtr shuuz_scanline_update = new TimerCallbackHandlerPtr() {
            @Override
            public void handler(int scanline) {
                /* update the playfield */
		if (scanline == 0)
			atarigen_video_control_update(new UBytePtr(atarigen_playfieldram, 0x1f00));
	
		/* update the MOs from the SLIP table */
		atarigen_mo_update_slip_512(atarigen_spriteram, atarigen_video_control_state.sprite_yscroll, scanline, new UBytePtr(atarigen_playfieldram, 0x1f80));
            }
        };
	
	
	
	/*************************************
	 *
	 *	Main refresh
	 *
	 *************************************/
	
	public static VhUpdatePtr shuuz_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
/*TODO*///	#if DEBUG_VIDEO
/*TODO*///		debug();
/*TODO*///	#endif
	
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
	
	public static UBytePtr update_palette()
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
	
		/* special case color 15 of motion object palette 15 */
		palette_used_colors.write(0x000 + 15 * 16 + 15, PALETTE_COLOR_TRANSPARENT);
	
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
		for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63){
			for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
			{
				int offs = x * 64 + y;
				int data1 = atarigen_playfieldram.READ_WORD(offs * 2);
				int data2 = atarigen_playfieldram.READ_WORD(offs * 2 + 0x2000);
				int code = data1 & 0x3fff;
				int color = (data2 >> 8) & 15;
	
				/* mark the colors used by this tile */
				colormap[color] |= usage[code];
                        }
                }
                param = colormap;
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
					int data2 = atarigen_playfieldram.READ_WORD(offs * 2 + 0x2000);
					int color = (data2 >> 8) & 15;
					int hflip = data1 & 0x8000;
					int code = data1 & 0x3fff;
	
					drawgfx(atarigen_pf_bitmap, gfx, code, color, hflip, 0, 8 * x, 8 * y, null, TRANSPARENCY_NONE, 0);
					atarigen_pf_dirty.write(offs, 0);
	
/*TODO*///	#if DEBUG_VIDEO
/*TODO*///					if (show_colors != 0)
/*TODO*///					{
/*TODO*///						drawgfx(atarigen_pf_bitmap, Machine.uifont, "0123456789ABCDEF".charAt(color), 1, 0, 0, 8 * x + 0, 8 * y, null, TRANSPARENCY_PEN, 0);
/*TODO*///						drawgfx(atarigen_pf_bitmap, Machine.uifont, "0123456789ABCDEF".charAt(color), 1, 0, 0, 8 * x + 2, 8 * y, null, TRANSPARENCY_PEN, 0);
/*TODO*///						drawgfx(atarigen_pf_bitmap, Machine.uifont, "0123456789ABCDEF".charAt(color), 0, 0, 0, 8 * x + 1, 8 * y, null, TRANSPARENCY_PEN, 0);
/*TODO*///					}
/*TODO*///	#endif
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
                pf_overrender_data overrender_data = (pf_overrender_data) param;
		GfxElement gfx = Machine.gfx[0];
		//osd_bitmap bitmap = overrender_data.bitmap;
		int x, y;
	
		/* standard loop over tiles */
		for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63)
			for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63)
			{
				int offs = x * 64 + y;
				int data2 = atarigen_playfieldram.READ_WORD(offs * 2 + 0x2000);
				int color = (data2 >> 8) & 15;
	
				/* overdraw if the color is 15 */
				if (((color & 8)!=0 && color >= overrender_data.color) || overrender_data.type == OVERRENDER_PRIORITY)
				{
					int data1 = atarigen_playfieldram.READ_WORD(offs * 2);
					int hflip = data1 & 0x8000;
					int code = data1 & 0x3fff;
	
					drawgfx(overrender_data.bitmap, gfx, code, color, hflip, 0, 8 * x, 8 * y, clip, TRANSPARENCY_NONE, 0);
	
/*TODO*///	#if DEBUG_VIDEO
/*TODO*///					if (show_colors != 0)
/*TODO*///					{
/*TODO*///						drawgfx(bitmap, Machine.uifont, "0123456789ABCDEF"[color], 1, 0, 0, 8 * x + 0, 8 * y, 0, TRANSPARENCY_PEN, 0);
/*TODO*///						drawgfx(bitmap, Machine.uifont, "0123456789ABCDEF"[color], 1, 0, 0, 8 * x + 2, 8 * y, 0, TRANSPARENCY_PEN, 0);
/*TODO*///						drawgfx(bitmap, Machine.uifont, "0123456789ABCDEF"[color], 0, 0, 0, 8 * x + 1, 8 * y, 0, TRANSPARENCY_PEN, 0);
/*TODO*///					}
/*TODO*///	#endif
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
	
	public static atarigen_mo_callback mo_render_callback = new atarigen_mo_callback() {
            @Override
            public void handler(UShortArray data, rectangle clip, Object param) {
                //System.out.println("mo_render_callback");
                GfxElement gfx = Machine.gfx[1];
		pf_overrender_data overrender_data = new pf_overrender_data();
		osd_bitmap bitmap = (osd_bitmap) param;
		rectangle pf_clip=new rectangle();
	
		/* extract data from the various words */
		int hflip = data.read(1) & 0x8000;
		int code = data.read(1) & 0x7fff;
		int xpos = (data.read(2) >> 7) - atarigen_video_control_state.sprite_xscroll;
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
                //System.out.println("xpos2: "+pf_clip.min_x);
	
		/* draw the motion object */
		atarigen_mo_draw_8x8(bitmap, gfx, code, color, hflip, 0, xpos, ypos, hsize, vsize, clip, TRANSPARENCY_PEN, 0);
	
		/* standard priority case? */
		if (color != 15)
		{
			/* overrender the playfield */
			overrender_data.bitmap = bitmap;
			overrender_data.type = OVERRENDER_STANDARD;
			overrender_data.color = color;
			atarigen_pf_process(pf_overrender_callback, overrender_data, pf_clip);
		}
	
		/* high priority case? */
		else
		{
			/* overrender the playfield */
			overrender_data.bitmap = atarigen_pf_overrender_bitmap;
			overrender_data.type = OVERRENDER_PRIORITY;
			overrender_data.color = color;
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
/*TODO*///			drawgfx(bitmap, Machine.uifont, "0123456789ABCDEF"[color], 0, 0, 0, tx, ty, 0, TRANSPARENCY_NONE, 0);
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
/*TODO*///	static void mo_print_callback(const UINT16 *data, const struct rectangle *clip, void *param)
/*TODO*///	{
/*TODO*///		FILE *file = param;
/*TODO*///	
/*TODO*///		/* extract data from the various words */
/*TODO*///		int hflip = data[1] & 0x8000;
/*TODO*///		int code = data[1] & 0x7fff;
/*TODO*///		int xpos = (data[2] >> 7) - 5;
/*TODO*///		int color = data[2] & 0x000f;
/*TODO*///		int ypos = YDIM - (data[3] >> 7);
/*TODO*///		int hsize = ((data[3] >> 4) & 7) + 1;
/*TODO*///		int vsize = (data[3] & 7) + 1;
/*TODO*///	
/*TODO*///		fprintf(file, "P=%04X C=%X F=%X  X=%03X Y=%03X S=%dx%d\n", code, color, hflip >> 15, xpos & 0xfff, ypos & 0xfff, hsize, vsize);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void debug(void)
/*TODO*///	{
/*TODO*///		int new_show_colors;
/*TODO*///		int new_special;
/*TODO*///	
/*TODO*///		new_show_colors = keyboard_pressed(KEYCODE_CAPSLOCK);
/*TODO*///		if (new_show_colors != show_colors)
/*TODO*///		{
/*TODO*///			show_colors = new_show_colors;
/*TODO*///			memset(atarigen_pf_dirty, 0xff, atarigen_playfieldram_size / 4);
/*TODO*///		}
/*TODO*///	
/*TODO*///		if (keyboard_pressed(KEYCODE_Q)) new_special = 0;
/*TODO*///		if (keyboard_pressed(KEYCODE_W)) new_special = 1;
/*TODO*///		if (keyboard_pressed(KEYCODE_E)) new_special = 2;
/*TODO*///		if (keyboard_pressed(KEYCODE_R)) new_special = 3;
/*TODO*///		if (keyboard_pressed(KEYCODE_T)) new_special = 4;
/*TODO*///		if (keyboard_pressed(KEYCODE_Y)) new_special = 5;
/*TODO*///		if (keyboard_pressed(KEYCODE_U)) new_special = 6;
/*TODO*///		if (keyboard_pressed(KEYCODE_I)) new_special = 7;
/*TODO*///	
/*TODO*///		if (keyboard_pressed(KEYCODE_A)) new_special = 8;
/*TODO*///		if (keyboard_pressed(KEYCODE_S)) new_special = 9;
/*TODO*///		if (keyboard_pressed(KEYCODE_D)) new_special = 10;
/*TODO*///		if (keyboard_pressed(KEYCODE_F)) new_special = 11;
/*TODO*///		if (keyboard_pressed(KEYCODE_G)) new_special = 12;
/*TODO*///		if (keyboard_pressed(KEYCODE_H)) new_special = 13;
/*TODO*///		if (keyboard_pressed(KEYCODE_J)) new_special = 14;
/*TODO*///		if (keyboard_pressed(KEYCODE_K)) new_special = 15;
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
/*TODO*///			fprintf(f, "\n\nMotion Object Palette:\n");
/*TODO*///			for (i = 0x000; i < 0x100; i++)
/*TODO*///			{
/*TODO*///				fprintf(f, "%04X ", READ_WORD(&paletteram.read(i*2)));
/*TODO*///				if ((i & 15) == 15) fprintf(f, "\n");
/*TODO*///			}
/*TODO*///	
/*TODO*///			fprintf(f, "\n\nPlayfield Palette:\n");
/*TODO*///			for (i = 0x100; i < 0x200; i++)
/*TODO*///			{
/*TODO*///				fprintf(f, "%04X ", READ_WORD(&paletteram.read(i*2)));
/*TODO*///				if ((i & 15) == 15) fprintf(f, "\n");
/*TODO*///			}
/*TODO*///	
/*TODO*///			fprintf(f, "\n\nMotion Objects Drawn:\n");
/*TODO*///			atarigen_mo_process(mo_print_callback, f);
/*TODO*///	
/*TODO*///			fprintf(f, "\n\nMotion Objects\n");
/*TODO*///			for (i = 0; i < 256; i++)
/*TODO*///			{
/*TODO*///				UINT16 *data = (UINT16 *)&atarigen_spriteram[i*8];
/*TODO*///				int hflip = data[1] & 0x8000;
/*TODO*///				int code = data[1] & 0x7fff;
/*TODO*///				int xpos = (data[2] >> 7) - 5;
/*TODO*///				int ypos = YDIM - (data[3] >> 7);
/*TODO*///				int color = data[2] & 0x000f;
/*TODO*///				int hsize = ((data[3] >> 4) & 7) + 1;
/*TODO*///				int vsize = (data[3] & 7) + 1;
/*TODO*///				fprintf(f, "   Object %03X: L=%03X P=%04X C=%X X=%03X Y=%03X W=%d H=%d F=%d LEFT=(%04X %04X %04X %04X)\n",
/*TODO*///						i, data[0] & 0x3ff, code, color, xpos & 0x1ff, ypos & 0x1ff, hsize, vsize, hflip,
/*TODO*///						data[0] & 0xfc00, data[1] & 0x0000, data[2] & 0x0070, data[3] & 0x0008);
/*TODO*///			}
/*TODO*///	
/*TODO*///			fprintf(f, "\n\nPlayfield dump\n");
/*TODO*///			for (i = 0; i < atarigen_playfieldram_size / 4; i++)
/*TODO*///			{
/*TODO*///				fprintf(f, "%X%04X ", READ_WORD(&atarigen_playfieldram[0x2000 + i*2]) >> 8, READ_WORD(&atarigen_playfieldram[i*2]));
/*TODO*///				if ((i & 63) == 63) fprintf(f, "\n");
/*TODO*///			}
/*TODO*///	
/*TODO*///			fprintf(f, "\n\nSprite RAM dump\n");
/*TODO*///			for (i = 0; i < 0x3000 / 2; i++)
/*TODO*///			{
/*TODO*///				fprintf(f, "%04X ", READ_WORD(&atarigen_spriteram[i*2]));
/*TODO*///				if ((i & 31) == 31) fprintf(f, "\n");
/*TODO*///			}
/*TODO*///	
/*TODO*///			fclose(f);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	#endif
}
