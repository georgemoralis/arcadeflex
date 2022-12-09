/***************************************************************************

  vidhrdw/mcr68.c

  Xenophobe video hardware very similar to Rampage.

  Colour 8 in sprites indicates transparency in closed area.
  Each tile has an attribute to indicate tile drawn on top of sprite.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.vidhrdw;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static common.libc.cstring.memset;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;

public class mcr68
{
/*TODO*///  	
/*TODO*///  	
/*TODO*///  	#define LOW_BYTE(x) (READ_WORD(x) & 0xff)
/*TODO*///  	
/*TODO*///  	
  	public static int mcr68_sprite_clip;
  	public static int mcr68_sprite_xoffset;
/*TODO*///  	
/*TODO*///  	
/*TODO*///  	#define DEBUG_VIDEO		0
/*TODO*///  	
/*TODO*///  	
/*TODO*///  	#if DEBUG_VIDEO
/*TODO*///  	static int show_bg_colors;
/*TODO*///  	static int show_colors;
/*TODO*///  	
/*TODO*///  	static void mcr68_debug();
/*TODO*///  	static void zwackery_debug();
/*TODO*///  	#endif
  	
  	
  	
  	/*************************************
  	 *
  	 *	Palette RAM writes
  	 *
  	 *************************************/
  	
  	public static WriteHandlerPtr mcr68_paletteram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
  	{
  		int oldword = paletteram.READ_WORD(offset);
  		int newword = COMBINE_WORD(oldword, data);
  		int r, g, b;
  	
  		paletteram.WRITE_WORD(offset, newword);
  	
  		r = (newword >> 6) & 7;
  		b = (newword >> 3) & 7;
  		g = (newword >> 0) & 7;
  	
  		/* up to 8 bits */
  		r = (r << 5) | (r << 2) | (r >> 1);
  		g = (g << 5) | (g << 2) | (g >> 1);
  		b = (b << 5) | (b << 2) | (b >> 1);
  	
  		palette_change_color(offset / 2, r, g, b);
  	} };
  	
  	
  	
  	/*************************************
  	 *
  	 *	Video RAM writes
  	 *
  	 *************************************/
  	
  	public static WriteHandlerPtr mcr68_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
  	{
  		int oldword = videoram.READ_WORD(offset);
  		int newword = COMBINE_WORD(oldword, data);
  	
  		if (oldword != newword)
  		{
  			dirtybuffer[offset & ~3] = 1;
  			videoram.WRITE_WORD(offset, newword);
  		}
  	} };
  	
  	
  	
  	/*************************************
  	 *
  	 *	Background update
  	 *
  	 *************************************/
  	
  	static void mcr68_update_background(osd_bitmap bitmap, int overrender)
  	{
  		int offs;
  	
  		/* for every character in the Video RAM, check if it has been modified */
  		/* since last time and update it accordingly. */
  		for (offs = videoram_size[0] - 4; offs >= 0; offs -= 4)
  		{
  			/* this works for overrendering as well, since the sprite code will mark */
  			/* intersecting tiles for us */
  			if (dirtybuffer[offs] != 0)
  			{
  				int mx = (offs / 4) % 32;
  				int my = (offs / 4) / 32;
  				int attr = videoram.READ_WORD(offs + 2) & 0xff;
  				int color = (attr & 0x30) >> 4;
  				int code = (videoram.READ_WORD(offs) & 0xff) + 256 * (attr & 0x03) + 1024 * ((attr >> 6) & 0x03);
  	
  				if (overrender==0)
  					drawgfx(bitmap, Machine.gfx[0], code, color ^ 3, attr & 0x04, attr & 0x08,
  							16 * mx, 16 * my, Machine.visible_area, TRANSPARENCY_NONE, 0);
  				else if (Machine.gfx[0].total_elements < 0x1000 && (attr & 0x80)!=0)
  					drawgfx(bitmap, Machine.gfx[0], code, color ^ 3, attr & 0x04, attr & 0x08,
  							16 * mx, 16 * my, Machine.visible_area, TRANSPARENCY_PEN, 0);
  				else
  					continue;
  	
  				/* only clear the dirty flag if we're not overrendering */
  				dirtybuffer[offs] = 0;
  			}
  		}
  	}
  	
  	
  	
  	/*************************************
  	 *
  	 *	Sprite update
  	 *
  	 *************************************/
  	
  	static void mcr68_update_sprites(osd_bitmap bitmap, int priority)
  	{
  		rectangle sprite_clip = new rectangle(Machine.visible_area);
  		int offs;
  	
  		/* adjust for clipping */
  		sprite_clip.min_x += mcr68_sprite_clip;
  		sprite_clip.max_x -= mcr68_sprite_clip;
  	
  		/* loop over sprite RAM */
  		for (offs = 0; offs < spriteram_size[0]; offs += 8)
  		{
  			int code, color, flipx, flipy, x, y, sx, sy, xcount, ycount, flags;
  	
  			flags = spriteram.READ_WORD(offs + 2) & 0xff;
  			code = (spriteram.READ_WORD(offs + 4) & 0xff) + 256 * ((flags >> 3) & 0x01) + 512 * ((flags >> 6) & 0x03);
  	
  			/* skip if zero */
  			if (code == 0)
  				continue;
  	
  			/* also skip if this isn't the priority we're drawing right now */
  			if (((flags >> 2) & 1) != priority)
  				continue;
  	
  			/* extract the bits of information */
  			color = ~flags & 0x03;
  			flipx = flags & 0x10;
  			flipy = flags & 0x20;
  			x = (spriteram.READ_WORD(offs + 6) & 0xff) * 2 + mcr68_sprite_xoffset;
  			y = (241 - (spriteram.READ_WORD(offs) & 0xff)) * 2;
  	
  			/* allow sprites to clip off the left side */
  			if (x > 0x1f0) x -= 0x200;
  	
  			/* draw the sprite */
  			drawgfx(bitmap, Machine.gfx[1], code, color, flipx, flipy, x, y,
  					sprite_clip, TRANSPARENCY_PEN, 0);
  	
  			/* sprites use color 0 for background pen and 8 for the 'under tile' pen.
  				The color 8 is used to cover over other sprites. */
  			if ((Machine.gfx[1].pen_usage[code] & 0x0100) != 0)
  			{
  				rectangle clip = new rectangle();
  	
  				clip.min_x = x;
  				clip.max_x = x + 31;
  				clip.min_y = y;
  				clip.max_y = y + 31;
  	
  				copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, clip, TRANSPARENCY_THROUGH, Machine.pens[8 + color * 16]);
  			}
  	
  			/* mark tiles underneath as dirty for overrendering */
  			if (priority == 0)
  			{
  				sx = x / 16;
  				sy = y / 16;
  				xcount = (x & 15)!=0 ? 3 : 2;
  				ycount = (y & 15)!=0 ? 3 : 2;
  	
  				for (y = sy; y < sy + ycount; y++)
  					for (x = sx; x < sx + xcount; x++)
  						if (x >= 0 && x < 32 && y >= 0 && y < 30)
  							dirtybuffer[(32 * y + x) * 4] = 1;
  			}
  		}
  	}
  	
  	
  	
  	/*************************************
  	 *
  	 *	General MCR/68k update
  	 *
  	 *************************************/
  	
  	public static VhUpdateHandlerPtr mcr68_vh_screenrefresh = new VhUpdateHandlerPtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
  	{
/*TODO*///  	#if DEBUG_VIDEO
/*TODO*///  		mcr68_debug();
/*TODO*///  	#endif
  	
  		/* update palette */
  		if (palette_recalc() != null)
  			memset(dirtybuffer, 1, videoram_size[0]);
  	
  		/* draw the background */
  		mcr68_update_background(tmpbitmap, 0);
  	
  		/* copy it to the destination */
  		copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.visible_area, TRANSPARENCY_NONE, 0);
  	
  		/* draw the low-priority sprites */
  		mcr68_update_sprites(bitmap, 0);
  	
  	    /* redraw tiles with priority over sprites */
  		mcr68_update_background(bitmap, 1);
  	
  		/* draw the high-priority sprites */
  		mcr68_update_sprites(bitmap, 1);
  	} };
  	
  	
  	
/*TODO*///  	/*************************************
/*TODO*///  	 *
/*TODO*///  	 *	General MCR/68k debug
/*TODO*///  	 *
/*TODO*///  	 *************************************/
/*TODO*///  	
/*TODO*///  	#if DEBUG_VIDEO
/*TODO*///  	void mcr68_debug(void)
/*TODO*///  	{
/*TODO*///  		static FILE *f;
/*TODO*///  		if (keyboard_pressed(KEYCODE_9))
/*TODO*///  		{
/*TODO*///  			int offs;
/*TODO*///  	
/*TODO*///  			if (!f) f = fopen("mcr.log", "w");
/*TODO*///  			if (f != 0)
/*TODO*///  			{
/*TODO*///  				fprintf(f, "\n\n=================================================================\n");
/*TODO*///  				for (offs = 0; offs < videoram_size; offs += 4)
/*TODO*///  				{
/*TODO*///  					fprintf(f, "%02X%02X ", LOW_BYTE(&videoram.read(offs + 2)), LOW_BYTE(&videoram.read(offs + 0)));
/*TODO*///  					if (offs % (32 * 4) == 31 * 4) fprintf(f, "\n");
/*TODO*///  				}
/*TODO*///  				fprintf(f, "\n\n");
/*TODO*///  				for (offs = 0; offs < spriteram_size; offs += 8)
/*TODO*///  					fprintf(f, "Sprite %03d: %02X %02X %02X %02X\n", offs / 8,
/*TODO*///  						LOW_BYTE(&spriteram.read(offs + 0)),
/*TODO*///  						LOW_BYTE(&spriteram.read(offs + 2)),
/*TODO*///  						LOW_BYTE(&spriteram.read(offs + 4)),
/*TODO*///  						LOW_BYTE(&spriteram.read(offs + 6)));
/*TODO*///  			}
/*TODO*///  			fflush(f);
/*TODO*///  		}
/*TODO*///  	}
/*TODO*///  	#endif
  	
  	
  	
  	/*************************************
  	 *
  	 *	Zwackery palette RAM writes
  	 *
  	 *************************************/
  	
  	public static WriteHandlerPtr zwackery_paletteram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
  	{
  		int oldword = paletteram.READ_WORD(offset);
  		int newword = COMBINE_WORD(oldword, data);
  		int r, g, b;
  	
  		paletteram.WRITE_WORD(offset, newword);
  	
  		r = (~newword >> 10) & 31;
  		b = (~newword >> 5) & 31;
  		g = (~newword >> 0) & 31;
  	
  		/* up to 8 bits */
  		r = (r << 3) | (r >> 2);
  		g = (g << 3) | (g >> 2);
  		b = (b << 3) | (b >> 2);
  	
  		palette_change_color(offset / 2, r, g, b);
  	} };
  	
  	
  	
  	/*************************************
  	 *
  	 *	Zwackery video RAM writes
  	 *
  	 *************************************/
  	
  	public static WriteHandlerPtr zwackery_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
  	{
  		int oldword = videoram.READ_WORD(offset);
  		int newword = COMBINE_WORD(oldword, data);
  	
  		if (oldword != newword)
  		{
  			dirtybuffer[offset & ~1] = 1;
  			videoram.WRITE_WORD(offset, newword);
  		}
  	} };
  	
  	
  	
  	/*************************************
  	 *
  	 *	Zwackery video RAM writes
  	 *
  	 *************************************/
  	
  	public static WriteHandlerPtr zwackery_spriteram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
  	{
  		/* yech -- Zwackery relies on the upper 8 bits of a spriteram read being $ff! */
  		/* to make this happen we always write $ff in the upper 8 bits */
  		int oldword = spriteram.READ_WORD(offset);
  		int newword = COMBINE_WORD(oldword, data);
  		spriteram.WRITE_WORD(offset, newword | 0xff00);
  	} };
  	
  	
  	
  	/*************************************
  	 *
  	 *	Zwackery color data conversion
  	 *
  	 *************************************/
  	
  	public static VhConvertColorPromHandlerPtr zwackery_convert_color_prom = new VhConvertColorPromHandlerPtr() {
              @Override
              public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
                UBytePtr colordatabase = new UBytePtr(memory_region(REGION_GFX3));
                colordatabase.offset=0;
                //System.out.println("offs1: "+colordatabase.offset);
  		GfxElement gfx0 = Machine.gfx[0];
  		GfxElement gfx2 = Machine.gfx[2];
  		int code, y, x, ix;
  	
  		/* "colorize" each code */
  		for (code = 0; code < gfx0.total_elements; code++)
  		{
  			int _coldata=code * 32;
                        UBytePtr coldata = new UBytePtr(colordatabase, _coldata);
                        int _gfxdata0=code * gfx0.char_modulo;
  			UBytePtr gfxdata0 = new UBytePtr(gfx0.gfxdata, _gfxdata0);
                        int _gfxdata2=code * gfx2.char_modulo;
  			UBytePtr gfxdata2 = new UBytePtr(gfx2.gfxdata, _gfxdata2);
  	
  			/* assume 16 rows */
  			for (y = 0; y < 16; y++)
  			{
  				UBytePtr cd = new UBytePtr(coldata);
                                cd.offset=_coldata;
  				UBytePtr gd0 = new UBytePtr(gfxdata0);
                                gd0.offset=_gfxdata0;
  				UBytePtr gd2 = new UBytePtr(gfxdata2);
                                gd2.offset=_gfxdata2;
  	
  				/* 16 colums, in batches of 4 pixels */
  				for (x = 0; x < 16; x += 4)
  				{
                                        //System.out.println("offs2: "+cd.offset);
  					int pen0 = cd.readinc();
  					int pen1 = cd.readinc();
  					int tp0, tp1;
  	
  					/* every 4 pixels gets its own foreground/background colors */
  					for (ix = 0; ix < 4; ix++, gd0.inc())
  						gd0.write( gd0.read()!=0 ? pen1 : pen0);
  	
  					/* for gfx 2, we convert all low-priority pens to 0 */
  					tp0 = (pen0 & 0x80)!=0 ? pen0 : 0;
  					tp1 = (pen1 & 0x80)!=0 ? pen1 : 0;
  					for (ix = 0; ix < 4; ix++, gd2.inc())
  						gd2.write(gd2.readinc()!=0 ? tp1 : tp0);
  				}
  	
  				/* advance */
  				if (y % 4 == 3) coldata = cd;
  				gfxdata0.inc( gfx0.line_modulo );
  				gfxdata2.inc( gfx2.line_modulo );
  			}
  		}
              }
        };
        
  	
  	/*************************************
  	 *
  	 *	Zwackery background update
  	 *
  	 *************************************/
  	
  	static void zwackery_mark_background()
  	{
  		UBytePtr colordatabase = new UBytePtr(memory_region(REGION_GFX3));
  		int offs;
  	
  		/* for every character in the Video RAM, mark the colors */
  		for (offs = videoram_size[0] - 2; offs >= 0; offs -= 2)
  		{
  			int data = videoram.READ_WORD(offs);
  			int color = (data >> 13) & 7;
  			int code = data & 0x3ff;
  			int i;
  	
  			/* get color data pointers */
  			UBytePtr coldata = new UBytePtr(colordatabase, code * 32);
  			UBytePtr used_colors = new UBytePtr(palette_used_colors, color << 8);
  	
  			/* each character uses up to 32 unique colors */
  			for (i = 0; i < 32; i++){
  				used_colors.write(coldata.read(), PALETTE_COLOR_VISIBLE);
                                coldata.inc();
                        }
  		}
  	}
  	
  	
  	static void zwackery_update_background(osd_bitmap bitmap, int overrender)
  	{
  		int offs;
  	
  		/* for every character in the Video RAM, check if it has been modified */
  		/* since last time and update it accordingly. */
  		for (offs = videoram_size[0] - 2; offs >= 0; offs -= 2)
  		{
  			/* this works for overrendering as well, since the sprite code will mark */
  			/* intersecting tiles for us */
  			if (dirtybuffer[offs] != 0)
  			{
  				int data = videoram.READ_WORD(offs);
  				int mx = (offs / 2) % 32;
  				int my = (offs / 2) / 32;
  				int color = (data >> 13) & 7;
  				int code = data & 0x3ff;
  	
  				/* standard case: draw with no transparency */
  				if (overrender==0)
  					drawgfx(bitmap, Machine.gfx[0], code, color, data & 0x0800, data & 0x1000,
  							16 * mx, 16 * my, Machine.visible_area, TRANSPARENCY_NONE, 0);
  	
  				/* overrender case: for non-zero colors, draw with transparency pen 0 */
  				/* we use gfx[2] here, which was generated above to have all low-priority */
  				/* colors set to pen 0 */
  				else if (color != 0)
  					drawgfx(bitmap, Machine.gfx[2], code, color, data & 0x0800, data & 0x1000,
  							16 * mx, 16 * my, Machine.visible_area, TRANSPARENCY_PEN, 0);
  	
/*TODO*///  	#if DEBUG_VIDEO
/*TODO*///  	if (show_bg_colors != 0)
/*TODO*///  	{
/*TODO*///  		char c = "01234567"[color];
/*TODO*///  		drawgfx(bitmap, Machine.uifont, c, 1, 0, 0, 16 * mx + 0, 16 * my, 0, TRANSPARENCY_PEN, 0);
/*TODO*///  		drawgfx(bitmap, Machine.uifont, c, 1, 0, 0, 16 * mx + 4, 16 * my, 0, TRANSPARENCY_PEN, 0);
/*TODO*///  		drawgfx(bitmap, Machine.uifont, c, 0, 0, 0, 16 * mx + 2, 16 * my, 0, TRANSPARENCY_PEN, 0);
/*TODO*///  	}
/*TODO*///  	#endif
  	
  				/* only clear the dirty flag if we're not overrendering */
  				dirtybuffer[offs] = 0;
  			}
  		}
  	}
  	
  	
  	
  	/*************************************
  	 *
  	 *	Sprite update
  	 *
  	 *************************************/
  	
  	static void zwackery_mark_sprites()
  	{
  		int[] used=new int[32];
  		int offs, i;
  	
  		/* clear the usage array */
/*TODO*///  		memset(&used, 0, sizeof(used));
  	
  		/* loop over spriteram */
  		for (offs = 0; offs < spriteram_size[0]; offs += 8)
  		{
  			int code, color, flags;
  	
  			/* get the code and skip if zero */
  			code = spriteram.READ_WORD(offs + 4)&0xff;
  			if (code == 0)
  				continue;
  	
  			/* extract the flag bits and determine the color */
  			//flags = LOW_BYTE(&spriteram.read(offs + 2));
                        flags = spriteram.READ_WORD(offs + 2)&0xff;
  			color = ((~flags >> 2) & 0xff) | ((flags & 0x02) << 3);
  	
  			/* mark the appropriate pens */
  			used[color] |= Machine.gfx[1].pen_usage[code];
  		}
  	
  		/* use the usage array to mark the global palette_used_colors */
  		for (offs = 0; offs < 32; offs++)
  		{
  			int u = used[offs];
  			if (u != 0)
  			{
  				palette_used_colors.write(0x800 + offs * 16 + 0, PALETTE_COLOR_TRANSPARENT);
  				for (i = 1; i < 16; i++)
  					if ((u & (1 << i)) != 0)
  						palette_used_colors.write(0x800 + offs * 16 + i, PALETTE_COLOR_USED);
  			}
  		}
  	}
  	
  	
  	static void zwackery_update_sprites(osd_bitmap bitmap, int priority)
  	{
  		int offs;
  	
  		/* loop over sprite RAM */
  		for (offs = 0; offs < spriteram_size[0]; offs += 8)
  		{
  			int code, color, flipx, flipy, x, y, sx, sy, xcount, ycount, flags;
  	
  			/* get the code and skip if zero */
  			code = spriteram.READ_WORD(offs + 4)&0xff;
  			if (code == 0)
  				continue;
  	
  			/* extract the flag bits and determine the color */
  			flags = spriteram.READ_WORD(offs + 2) & 0xff;
  			color = ((~flags >> 2) & 0xff) | ((flags & 0x02) << 3);
  	
  			/* for low priority, draw everything but color 7 */
  			if (priority==0)
  			{
  				if (color == 7)
  					continue;
  			}
  	
  			/* for high priority, only draw color 7 */
  			else
  			{
  				if (color != 7)
  					continue;
  			}
  	
  			/* determine flipping and coordinates */
  			flipx = ~flags & 0x40;
  			flipy = flags & 0x80;
  			x = (231 - (spriteram.READ_WORD(offs + 6)&0xff)) * 2;
  			y = (241 - (spriteram.READ_WORD(offs)&0xff)) * 2;
  	
  			if (x <= -32) x += 512;
  	
  			/* draw the sprite */
  			drawgfx(bitmap, Machine.gfx[1], code, color, flipx, flipy, x, y,
  					Machine.visible_area, TRANSPARENCY_PEN, 0);
  	
/*TODO*///  	#if DEBUG_VIDEO
/*TODO*///  	if (show_colors != 0)
/*TODO*///  	{
/*TODO*///  		char c = "0123456789ABCDEF"[color];
/*TODO*///  		drawgfx(bitmap, Machine.uifont, c, 1, 0, 0, x + 8,  y + 8, 0, TRANSPARENCY_PEN, 0);
/*TODO*///  		drawgfx(bitmap, Machine.uifont, c, 1, 0, 0, x + 12, y + 8, 0, TRANSPARENCY_PEN, 0);
/*TODO*///  		drawgfx(bitmap, Machine.uifont, c, 0, 0, 0, x + 10, y + 8, 0, TRANSPARENCY_PEN, 0);
/*TODO*///  	}
/*TODO*///  	#endif
  	
  			/* sprites use color 0 for background pen and 8 for the 'under tile' pen.
  				The color 8 is used to cover over other sprites. */
  			if ((Machine.gfx[1].pen_usage[code] & 0x0100) != 0)
  			{
  				rectangle clip=new rectangle();
  	
  				clip.min_x = x;
  				clip.max_x = x + 31;
  				clip.min_y = y;
  				clip.max_y = y + 31;
  	
  				copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, clip, TRANSPARENCY_THROUGH, Machine.pens[8 + color * 16]);
  			}
  	
  			/* mark tiles underneath as dirty for overrendering */
  			if (priority == 0)
  			{
  				sx = x / 16;
  				sy = y / 16;
  				xcount = (x & 15)!=0 ? 3 : 2;
  				ycount = (y & 15)!=0 ? 3 : 2;
  	
  				for (y = sy; y < sy + ycount; y++)
  					for (x = sx; x < sx + xcount; x++)
  						dirtybuffer[(32 * (y & 31) + (x & 31)) * 2] = 1;
  			}
  		}
  	}
  	
  	
  	
  	/*************************************
  	 *
  	 *	Zwackery MCR/68k update
  	 *
  	 *************************************/
  	
  	public static VhUpdateHandlerPtr zwackery_vh_screenrefresh = new VhUpdateHandlerPtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
  	{
/*TODO*///  	#if DEBUG_VIDEO
/*TODO*///  		zwackery_debug();
/*TODO*///  	#endif
  	
  		/* mark the palette */
  		palette_init_used_colors();
  		zwackery_mark_background();
  		zwackery_mark_sprites();
  	
  		/* update palette */
  		if (palette_recalc() != null)
  			memset(dirtybuffer, 1, videoram_size[0]);
  	
  		/* draw the background */
  		zwackery_update_background(tmpbitmap, 0);
  	
  		/* copy it to the destination */
  		copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.visible_area, TRANSPARENCY_NONE, 0);
  	
  		/* draw the low-priority sprites */
  		zwackery_update_sprites(bitmap, 0);
  	
  		/* draw the background */
  		zwackery_update_background(bitmap, 1);
  	
  		/* draw the high-priority sprites */
  		zwackery_update_sprites(bitmap, 1);
  	} };
  	
  	
  	
/*TODO*///  	/*************************************
/*TODO*///  	 *
/*TODO*///  	 *	Zwackery debug
/*TODO*///  	 *
/*TODO*///  	 *************************************/
/*TODO*///  	
/*TODO*///  	#if DEBUG_VIDEO
/*TODO*///  	void zwackery_debug(void)
/*TODO*///  	{
/*TODO*///  		static int last_h_state;
/*TODO*///  		static int last_l_state;
/*TODO*///  		static FILE *f;
/*TODO*///  		int i;
/*TODO*///  	
/*TODO*///  		if (keyboard_pressed(KEYCODE_CAPSLOCK))
/*TODO*///  		{
/*TODO*///  			if (!show_bg_colors) memset(dirtybuffer, 1, videoram_size);
/*TODO*///  			show_bg_colors = 1;
/*TODO*///  		}
/*TODO*///  		else
/*TODO*///  		{
/*TODO*///  			if (show_bg_colors != 0) memset(dirtybuffer, 1, videoram_size);
/*TODO*///  			show_bg_colors = 0;
/*TODO*///  		}
/*TODO*///  	
/*TODO*///  		if (keyboard_pressed(KEYCODE_9))
/*TODO*///  		{
/*TODO*///  			int offs;
/*TODO*///  	
/*TODO*///  			if (!f) f = fopen("mcr.log", "w");
/*TODO*///  			if (f != 0)
/*TODO*///  			{
/*TODO*///  				fprintf(f, "\n\n=================================================================\n");
/*TODO*///  				for (offs = 0; offs < videoram_size; offs += 2)
/*TODO*///  				{
/*TODO*///  					fprintf(f, "%04X ", READ_WORD(&videoram.read(offs)));
/*TODO*///  					if (offs % (32 * 2) == 31 * 2) fprintf(f, "\n");
/*TODO*///  				}
/*TODO*///  				fprintf(f, "\n\n");
/*TODO*///  				for (offs = 0; offs < spriteram_size; offs += 8)
/*TODO*///  					fprintf(f, "Sprite %03d: %02X %02X %02X %02X\n", offs / 8,
/*TODO*///  						READ_WORD(&spriteram.read(offs + 0)),
/*TODO*///  						READ_WORD(&spriteram.read(offs + 2)),
/*TODO*///  						READ_WORD(&spriteram.read(offs + 4)),
/*TODO*///  						READ_WORD(&spriteram.read(offs + 6)));
/*TODO*///  			}
/*TODO*///  			fflush(f);
/*TODO*///  		}
/*TODO*///  	
/*TODO*///  		show_colors = keyboard_pressed(KEYCODE_8);
/*TODO*///  	
/*TODO*///  		if (keyboard_pressed(KEYCODE_H))
/*TODO*///  		{
/*TODO*///  			last_h_state = 1;
/*TODO*///  			for (i = 0; i < 2048; i++)
/*TODO*///  				if ((i & 0x80) != 0) palette_change_color(i, 255, 255, 255);
/*TODO*///  		}
/*TODO*///  		else if (last_h_state != 0)
/*TODO*///  		{
/*TODO*///  			last_h_state = 0;
/*TODO*///  			for (i = 0; i < 2048; i++)
/*TODO*///  				if ((i & 0x80) != 0)
/*TODO*///  				{
/*TODO*///  					int word = READ_WORD(&paletteram.read(i * 2));
/*TODO*///  	
/*TODO*///  					int r = (~word >> 10) & 31;
/*TODO*///  					int b = (~word >> 5) & 31;
/*TODO*///  					int g = (~word >> 0) & 31;
/*TODO*///  	
/*TODO*///  					/* up to 8 bits */
/*TODO*///  					r = (r << 3) | (r >> 2);
/*TODO*///  					g = (g << 3) | (g >> 2);
/*TODO*///  					b = (b << 3) | (b >> 2);
/*TODO*///  	
/*TODO*///  					palette_change_color(i, r, g, b);
/*TODO*///  				}
/*TODO*///  		}
/*TODO*///  	
/*TODO*///  		if (keyboard_pressed(KEYCODE_L))
/*TODO*///  		{
/*TODO*///  			last_l_state = 1;
/*TODO*///  			for (i = 0; i < 2048; i++)
/*TODO*///  				if (!(i & 0x80)) palette_change_color(i, 255, 255, 255);
/*TODO*///  		}
/*TODO*///  		else if (last_l_state != 0)
/*TODO*///  		{
/*TODO*///  			last_l_state = 0;
/*TODO*///  			for (i = 0; i < 2048; i++)
/*TODO*///  				if (!(i & 0x80))
/*TODO*///  				{
/*TODO*///  					int word = READ_WORD(&paletteram.read(i * 2));
/*TODO*///  	
/*TODO*///  					int r = (~word >> 10) & 31;
/*TODO*///  					int b = (~word >> 5) & 31;
/*TODO*///  					int g = (~word >> 0) & 31;
/*TODO*///  	
/*TODO*///  					/* up to 8 bits */
/*TODO*///  					r = (r << 3) | (r >> 2);
/*TODO*///  					g = (g << 3) | (g >> 2);
/*TODO*///  					b = (b << 3) | (b >> 2);
/*TODO*///  	
/*TODO*///  					palette_change_color(i, r, g, b);
/*TODO*///  				}
/*TODO*///  		}
/*TODO*///  	}
/*TODO*///  	#endif
/*TODO*///  	
}
