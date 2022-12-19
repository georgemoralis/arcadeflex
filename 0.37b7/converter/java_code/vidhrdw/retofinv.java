/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class retofinv
{
	
	size_t retofinv_videoram_size;
	UBytePtr retofinv_sprite_ram1;
	UBytePtr retofinv_sprite_ram2;
	UBytePtr retofinv_sprite_ram3;
	UBytePtr retofinv_fg_char_bank;
	UBytePtr retofinv_bg_char_bank;
	UBytePtr retofinv_bg_videoram;
	UBytePtr retofinv_fg_videoram;
	UBytePtr retofinv_bg_colorram;
	UBytePtr retofinv_fg_colorram;
	
	static unsigned char flipscreen=0;
	static UBytePtr bg_dirtybuffer;
	static unsigned bg_bank; /* last background bank active, 0 or 1 */
	static struct osd_bitmap *bitmap_bg;
	
	
	/* data corrections for color rom */
	static unsigned adj_data(unsigned v)
	{
		/* reverse bits 4-7 */
		return (v & 0xF) |
				((v & 0x80) >> 3) | ((v & 0x40) >> 1) | ((v & 0x20) << 1) | ((v & 0x10) << 3);
	}
	
	public static VhConvertColorPromPtr retofinv_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
		#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2,bit3;
	
			bit0 = (color_prom.read(2*Machine->drv->total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(2*Machine->drv->total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(2*Machine->drv->total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(2*Machine->drv->total_colors)>> 3) & 0x01;
			*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			bit0 = (color_prom.read(1*Machine->drv->total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(1*Machine->drv->total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(1*Machine->drv->total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(1*Machine->drv->total_colors)>> 3) & 0x01;
	
			*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			bit0 = (color_prom.read(0*Machine->drv->total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(0*Machine->drv->total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(0*Machine->drv->total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(0*Machine->drv->total_colors)>> 3) & 0x01;
			*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			color_prom++;
		}
	
		color_prom += 2*Machine.drv.total_colors;
		/* color_prom now points to the beginning of the lookup table */
	
		/* foreground colors */
		for (i = 0;i < TOTAL_COLORS(0);i++)
		{
			if (i % 2)
				COLOR(0,i) = i/2;
			else
				COLOR(0,i) = 0;
		}
	
		/* sprites */
		for(i = 0;i < TOTAL_COLORS(2);i++)
			COLOR(2,i) = adj_data(*color_prom++);
	
		/* background bank 0 (gameplay) */
		/* background bank 1 (title screen) */
		for(i = 0;i < TOTAL_COLORS(1);i++)
			COLOR(1,i) = adj_data(color_prom.read(i));
	} };
	
	
	public static VhStartPtr retofinv_vh_start = new VhStartPtr() { public int handler() 
	{
		if ((bg_dirtybuffer = malloc(retofinv_videoram_size)) == 0)
		{
			return 1;
		}
		if ((bitmap_bg = bitmap_alloc(Machine.drv.screen_width,Machine.drv.screen_height)) == 0)
		{
			free(bg_dirtybuffer);
			return 1;
		}
		memset(bg_dirtybuffer,1,retofinv_videoram_size);
		bg_bank = 0;
		return 0;
	} };
	
	public static VhStopPtr retofinv_vh_stop = new VhStopPtr() { public void handler() 
	{
		free(bg_dirtybuffer);
		bitmap_free(bitmap_bg);
	} };
	
	public static WriteHandlerPtr retofinv_flip_screen_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		flipscreen = data;
		memset(bg_dirtybuffer,1,retofinv_videoram_size);
		fillbitmap(bitmap_bg,Machine.pens[0],0);
	} };
	
	public static ReadHandlerPtr retofinv_bg_videoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return retofinv_bg_videoram[offset];
	} };
	
	public static ReadHandlerPtr retofinv_fg_videoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return retofinv_fg_videoram[offset];
	} };
	
	public static ReadHandlerPtr retofinv_bg_colorram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return retofinv_bg_colorram[offset];
	} };
	
	public static ReadHandlerPtr retofinv_fg_colorram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return retofinv_fg_colorram[offset];
	} };
	
	public static WriteHandlerPtr retofinv_bg_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (retofinv_bg_videoram[offset] != data)
		{
			bg_dirtybuffer[offset] = 1;
			retofinv_bg_videoram[offset] = data;
		}
	} };
	
	public static WriteHandlerPtr retofinv_fg_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (retofinv_fg_videoram[offset] != data)
			retofinv_fg_videoram[offset] = data;
	} };
	
	public static WriteHandlerPtr retofinv_bg_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (retofinv_bg_colorram[offset] != data)
		{
			bg_dirtybuffer[offset] = 1;
			retofinv_bg_colorram[offset] = data;
		}
	} };
	
	public static WriteHandlerPtr retofinv_fg_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (retofinv_fg_colorram[offset] != data)
			retofinv_fg_colorram[offset] = data;
	} };
	
	void retofinv_render_sprites(struct osd_bitmap *bitmap)
	{
		int offs,sx,sy,flipx,flipy,tile,palette,size;
		int tileofs0,tileofs1,tileofs2,tileofs3;
	
		for (offs = 0; offs<127; offs+=2)
		{
			{
				sx = 311-(((retofinv_sprite_ram2[offs+1] & 127) << 1) +
				     	  ((retofinv_sprite_ram3[offs+1] & 128) >> 7) +
				     	  ((retofinv_sprite_ram2[offs+1] & 128) << 1));
	
				sy = 	  ((retofinv_sprite_ram2[offs] & 127) << 1) +
				     	  ((retofinv_sprite_ram3[offs] & 128) >> 7) +
				     	  ((retofinv_sprite_ram2[offs] & 128) << 1);
	
				tile    = retofinv_sprite_ram1[offs];
				size	= retofinv_sprite_ram3[offs];
				palette = retofinv_sprite_ram1[offs+1] & 0x3f;
	
				flipx = 0;
				flipy = 0;
				tileofs0 = 0;
				tileofs1 = 1;
				tileofs2 = 2;
				tileofs3 = 3;
	
				if (flipscreen != 0)
				{
					tileofs0 = 2;
					tileofs2 = 0;
					tileofs1 = 3;
					tileofs3 = 1;
					flipx = flipy = 1;
				}
	
				if (!(size & 12))
				{
					/* Patch for disappearing invadres' missile,
						     could it be Z80 bug ? */
					if (tile==0x98) tile--;
	
					drawgfx(bitmap,Machine.gfx[2],
								tile,
								palette,
								flipx,flipy,
								sx,sy,
								&Machine.visible_area,TRANSPARENCY_PEN,0);
				}
				if ((size & 4) != 0)
				{
					if ((size & 8) && (flipscreen)) sx-=16;
					drawgfx(bitmap,Machine.gfx[2],
								tile+tileofs0,
								palette,
								flipx,flipy,
								sx,sy+16,
								&Machine.visible_area,TRANSPARENCY_PEN,0);
	
					drawgfx(bitmap,Machine.gfx[2],
								tile+tileofs2,
								palette,
								flipx,flipy,
								sx,sy,
								&Machine.visible_area,TRANSPARENCY_PEN,0);
				}
				if ((size & 8) != 0)
				{
					if (flipscreen != 0) sx+=32;
					drawgfx(bitmap,Machine.gfx[2],
								tile+tileofs1,
								palette,
								flipx,flipy,
								sx-16,sy+16,
								&Machine.visible_area,TRANSPARENCY_PEN,0);
	
					drawgfx(bitmap,Machine.gfx[2],
								tile+tileofs3,
								palette,
								flipx,flipy,
								sx-16,sy,
								&Machine.visible_area,TRANSPARENCY_PEN,0);
				}
			}
		}
	}
	
	
	void retofinv_draw_background(struct osd_bitmap *bitmap)
	{
		int x,y,offs;
		int sx,sy,tile,palette;
		int bg_dirtybank;
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
	
		/* if bank differ redraw all */
		bg_dirtybank = (retofinv_bg_char_bank[0] & 1) != bg_bank;
	
		/* save active bank */
		bg_bank = (retofinv_bg_char_bank[0] & 1);
	
		for (y = 31; y>=0; y--)
		{
			for (x = 31; x>=0; x--)
			{
				offs = y*32+x;
	
				if (bg_dirtybank || bg_dirtybuffer[offs])
				{
					sx = 31-x;
					sy = 31-y;
	
					if (flipscreen != 0)
					{
						sx = 31 - sx;
						sy = 31 - sy;
					}
	
					bg_dirtybuffer[offs] = 0;
					tile = retofinv_bg_videoram[offs] + 256 * bg_bank;
					palette = retofinv_bg_colorram[offs] & 0x3f;
	
					drawgfx(bitmap_bg,Machine.gfx[1],
							tile,
							palette,
							flipscreen,flipscreen,
							8*sx+16,8*sy,
							&Machine.visible_area,TRANSPARENCY_NONE,0);
				}
			}
		}
	
		copybitmap(bitmap,bitmap_bg,0,0,0,0,&Machine.visible_area,TRANSPARENCY_NONE,0);
	}
	
	
	void retofinv_draw_foreground(struct osd_bitmap *bitmap)
	{
		int x,y,offs;
		int sx,sy,tile,palette,flipx,flipy;
	
		for (x=0; x<32; x++)
		{
			for (y = 30; y<=31; y++)
			{
				offs = y*32+x;
	
				sx = ((62-y)+3) << 3;
				sy = (31-x) << 3;
	
				flipx = flipy = 0;
	
				if (flipscreen != 0)
				{
					sx = 280 - sx;
					sy = 248 - sy;
					flipx = flipy = 1;
				}
	
				tile = retofinv_fg_videoram[offs]+(retofinv_fg_char_bank[0]*256);
				palette = retofinv_fg_colorram[offs];
	
				drawgfx(bitmap,Machine.gfx[0],
							  tile,
							  palette,
							  flipx,flipy,
							  sx,sy,
							  &Machine.visible_area,TRANSPARENCY_NONE,0);
			}
		}
	
		for (x = 29; x>=2 ;x--)
		{
			for (y = 31; y>=0; y--)
			{
				offs = x*32+y;
				sy = ((31-x) << 3);
				sx = ((33-y)) << 3;
	
				flipx = flipy = 0;
	
				if (flipscreen != 0)
				{
					sx = 280 - sx;
					sy = 248 - sy;
					flipx = flipy = 1;
				}
	
				tile = retofinv_fg_videoram[offs]+(retofinv_fg_char_bank[0]*256);
				palette = retofinv_fg_colorram[offs];
	
				drawgfx(bitmap,Machine.gfx[0],
							  tile,
							  palette,
							  flipx,flipy,
							  sx,sy,
							  &Machine.visible_area,TRANSPARENCY_PEN,0);
			}
		}
	
		for (x=0; x<32; x++)
		{
			for (y = 1; y>=0; y--)
			{
				offs = y*32+x;
				sx = (1-y) << 3;
				sy = (31-x) << 3;
	
				flipx = flipy = 0;
	
				if (flipscreen != 0)
				{
					sx = 280 - sx;
					sy = 248 - sy;
					flipx = flipy = 1;
				}
	
				tile = retofinv_fg_videoram[offs]+(retofinv_fg_char_bank[0]*256);
				palette = retofinv_fg_colorram[offs];
	
				drawgfx(bitmap,Machine.gfx[0],
							  tile,
							  palette,
							  flipx,flipy,
							  sx,sy,
							  &Machine.visible_area,TRANSPARENCY_NONE,0);
			}
		}
	}
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	
	public static VhUpdatePtr retofinv_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		retofinv_draw_background(bitmap);
		retofinv_render_sprites(bitmap);
		retofinv_draw_foreground(bitmap);
	} };
}
