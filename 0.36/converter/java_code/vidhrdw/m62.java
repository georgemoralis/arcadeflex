/***************************************************************************

  Video Hardware for Irem Games:
  Battle Road, Lode Runner, Kid Niki, Spelunker

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package vidhrdw;

import static arcadeflex.libc.*;
import static mame.drawgfxH.*;
import static mame.drawgfx.*;
import static vidhrdw.generic.*;
import static mame.driverH.*;
import static mame.osdependH.*;
import static mame.mame.*;

public class m62
{
	
	
	
	static int flipscreen;
	static const unsigned char *sprite_height_prom;
	static int kidniki_background_bank;
	static int irem_background_hscroll;
	static int irem_background_vscroll;
	static int kidniki_text_vscroll;
	static int spelunk2_palbank;
	
	unsigned char *irem_textram;
	int irem_textram_size;
	
	
	static const struct rectangle kungfum_spritevisiblearea =
	{
		16*8, (64-16)*8-1,
		10*8, 32*8-1
	};
	static const struct rectangle kungfum_flipspritevisiblearea =
	{
		16*8, (64-16)*8-1,
		0*8, 22*8-1
	};
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Kung Fu Master has a six 256x4 palette PROMs (one per gun; three for
	  characters, three for sprites).
	  I don't know the exact values of the resistors between the RAM and the
	  RGB output. I assumed these values (the same as Commando)
	
	  bit 3 -- 220 ohm resistor  -- RED/GREEN/BLUE
	        -- 470 ohm resistor  -- RED/GREEN/BLUE
	        -- 1  kohm resistor  -- RED/GREEN/BLUE
	  bit 0 -- 2.2kohm resistor  -- RED/GREEN/BLUE
	
	***************************************************************************/
	public static VhConvertColorPromPtr irem_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(UByte []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
	
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2,bit3;
	
			/* red component */
			bit0 = (color_prom[0] >> 0) & 0x01;
			bit1 = (color_prom[0] >> 1) & 0x01;
			bit2 = (color_prom[0] >> 2) & 0x01;
			bit3 = (color_prom[0] >> 3) & 0x01;
			*(palette++) =  0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			/* green component */
			bit0 = (color_prom[Machine.drv.total_colors] >> 0) & 0x01;
			bit1 = (color_prom[Machine.drv.total_colors] >> 1) & 0x01;
			bit2 = (color_prom[Machine.drv.total_colors] >> 2) & 0x01;
			bit3 = (color_prom[Machine.drv.total_colors] >> 3) & 0x01;
			*(palette++) =  0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			/* blue component */
			bit0 = (color_prom[2*Machine.drv.total_colors] >> 0) & 0x01;
			bit1 = (color_prom[2*Machine.drv.total_colors] >> 1) & 0x01;
			bit2 = (color_prom[2*Machine.drv.total_colors] >> 2) & 0x01;
			bit3 = (color_prom[2*Machine.drv.total_colors] >> 3) & 0x01;
			*(palette++) =  0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			color_prom++;
		}
	
		color_prom += 2*Machine.drv.total_colors;
		/* color_prom now points to the beginning of the sprite height table */
	
		sprite_height_prom = color_prom;	/* we'll need this at run time */
	} };
	
	public static VhConvertColorPromPtr battroad_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(UByte []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
	
	
		for (i = 0;i < 512;i++)
		{
			int bit0,bit1,bit2,bit3;
	
			/* red component */
			bit0 = (color_prom[0] >> 0) & 0x01;
			bit1 = (color_prom[0] >> 1) & 0x01;
			bit2 = (color_prom[0] >> 2) & 0x01;
			bit3 = (color_prom[0] >> 3) & 0x01;
			*(palette++) =  0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			/* green component */
			bit0 = (color_prom[512] >> 0) & 0x01;
			bit1 = (color_prom[512] >> 1) & 0x01;
			bit2 = (color_prom[512] >> 2) & 0x01;
			bit3 = (color_prom[512] >> 3) & 0x01;
			*(palette++) =  0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			/* blue component */
			bit0 = (color_prom[2*512] >> 0) & 0x01;
			bit1 = (color_prom[2*512] >> 1) & 0x01;
			bit2 = (color_prom[2*512] >> 2) & 0x01;
			bit3 = (color_prom[2*512] >> 3) & 0x01;
			*(palette++) =  0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			color_prom++;
		}
	
		color_prom += 2*512;
		/* color_prom now points to the beginning of the character color prom */
	
		for (i = 0;i < 32;i++)
		{
			int bit0,bit1,bit2;
	
	
			bit0 = (color_prom[i] >> 0) & 0x01;
			bit1 = (color_prom[i] >> 1) & 0x01;
			bit2 = (color_prom[i] >> 2) & 0x01;
			*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			bit0 = (color_prom[i] >> 3) & 0x01;
			bit1 = (color_prom[i] >> 4) & 0x01;
			bit2 = (color_prom[i] >> 5) & 0x01;
			*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			bit0 = 0;
			bit1 = (color_prom[i] >> 6) & 0x01;
			bit2 = (color_prom[i] >> 7) & 0x01;
			*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	
		}
	
		color_prom += 32;
		/* color_prom now points to the beginning of the sprite height table */
	
		sprite_height_prom = color_prom;	/* we'll need this at run time */
	} };
	
	public static VhConvertColorPromPtr spelunk2_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(UByte []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
	
	
		/* chars */
		for (i = 0;i < 512;i++)
		{
			int bit0,bit1,bit2,bit3;
	
			/* red component */
			bit0 = (color_prom[0] >> 0) & 0x01;
			bit1 = (color_prom[0] >> 1) & 0x01;
			bit2 = (color_prom[0] >> 2) & 0x01;
			bit3 = (color_prom[0] >> 3) & 0x01;
			*(palette++) =  0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			/* green component */
			bit0 = (color_prom[0] >> 4) & 0x01;
			bit1 = (color_prom[0] >> 5) & 0x01;
			bit2 = (color_prom[0] >> 6) & 0x01;
			bit3 = (color_prom[0] >> 7) & 0x01;
			*(palette++) =  0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			/* blue component */
			bit0 = (color_prom[2*256] >> 0) & 0x01;
			bit1 = (color_prom[2*256] >> 1) & 0x01;
			bit2 = (color_prom[2*256] >> 2) & 0x01;
			bit3 = (color_prom[2*256] >> 3) & 0x01;
			*(palette++) =  0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			color_prom++;
		}
	
		color_prom += 2*256;
	
		/* sprites */
		for (i = 0;i < 256;i++)
		{
			int bit0,bit1,bit2,bit3;
	
			/* red component */
			bit0 = (color_prom[0] >> 0) & 0x01;
			bit1 = (color_prom[0] >> 1) & 0x01;
			bit2 = (color_prom[0] >> 2) & 0x01;
			bit3 = (color_prom[0] >> 3) & 0x01;
			*(palette++) =  0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			/* green component */
			bit0 = (color_prom[256] >> 0) & 0x01;
			bit1 = (color_prom[256] >> 1) & 0x01;
			bit2 = (color_prom[256] >> 2) & 0x01;
			bit3 = (color_prom[256] >> 3) & 0x01;
			*(palette++) =  0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			/* blue component */
			bit0 = (color_prom[2*256] >> 0) & 0x01;
			bit1 = (color_prom[2*256] >> 1) & 0x01;
			bit2 = (color_prom[2*256] >> 2) & 0x01;
			bit3 = (color_prom[2*256] >> 3) & 0x01;
			*(palette++) =  0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			color_prom++;
		}
	
		color_prom += 2*256;
	
	
		/* color_prom now points to the beginning of the sprite height table */
		sprite_height_prom = color_prom;	/* we'll need this at run time */
	} };
	
	
	
	public static VhStartPtr ldrun_vh_start = new VhStartPtr() { public int handler() 
	{
		irem_background_hscroll = 0;
		irem_background_vscroll = 0;
		return generic_vh_start();
	} };
	
	
	static int irem_vh_start( int width, int height )
	{
		irem_background_hscroll = 0;
		irem_background_vscroll = 0;
	
		if ((dirtybuffer = malloc(videoram_size)) == 0)
			return 1;
		memset(dirtybuffer,1,videoram_size);
	
		if ((tmpbitmap = osd_create_bitmap(width,height)) == 0)
		{
			free(dirtybuffer);
			return 1;
		}
	
		return 0;
	}
	
	public static VhStartPtr kidniki_vh_start = new VhStartPtr() { public int handler() 
	{
		return irem_vh_start(512,256);
	} };
	
	public static VhStartPtr spelunkr_vh_start = new VhStartPtr() { public int handler() 
	{
		return irem_vh_start(512,512);
	} };
	
	
	
	public static WriteHandlerPtr irem_flipscreen_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* screen flip is handled both by software and hardware */
		data ^= ~readinputport(4) & 1;
	
		if (flipscreen != (data & 1))
		{
			flipscreen = data & 1;
			memset(dirtybuffer,1,videoram_size);
		}
	
		coin_counter_w(0,data & 2);
		coin_counter_w(1,data & 4);
	} };
	
	
	public static WriteHandlerPtr irem_background_hscroll_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		switch(offset)
		{
			case 0:
				irem_background_hscroll = (irem_background_hscroll&0xff00)|data;
				break;
	
			case 1:
				irem_background_hscroll = (irem_background_hscroll&0xff)|(data<<8);
				break;
		}
	} };
	
	public static WriteHandlerPtr kungfum_scroll_low_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		irem_background_hscroll_w(0,data);
	} };
	public static WriteHandlerPtr kungfum_scroll_high_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		irem_background_hscroll_w(1,data);
	} };
	
	public static WriteHandlerPtr irem_background_vscroll_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		switch( offset ){
			case 0:
			irem_background_vscroll = (irem_background_vscroll&0xff00)|data;
			break;
	
			case 1:
			irem_background_vscroll = (irem_background_vscroll&0xff)|(data<<8);
			break;
		}
	} };
	
	public static WriteHandlerPtr battroad_scroll_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		switch( offset ){
			case 0:
			irem_background_vscroll_w(0, data);
			break;
	
			case 1:
			irem_background_hscroll_w(1, data);
			break;
	
			case 2:
			irem_background_hscroll_w(0, data);
			break;
		}
	} };
	
	public static WriteHandlerPtr ldrun3_vscroll_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		irem_background_vscroll = data;
	} };
	
	public static WriteHandlerPtr ldrun4_hscroll_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		irem_background_hscroll_w(offset ^ 1,data);
	} };
	
	public static WriteHandlerPtr kidniki_text_vscroll_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		switch (offset)
		{
			case 0:
			kidniki_text_vscroll = (kidniki_text_vscroll & 0xff00) | data;
			break;
	
			case 1:
			kidniki_text_vscroll = (kidniki_text_vscroll & 0xff) | (data << 8);
			break;
		}
	} };
	
	public static WriteHandlerPtr kidniki_background_bank_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (kidniki_background_bank != (data & 1))
		{
			kidniki_background_bank = data & 1;
			memset(dirtybuffer,1,videoram_size);
		}
	} };
	
	public static WriteHandlerPtr spelunkr_palbank_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (spelunk2_palbank != (data & 0x01))
		{
			spelunk2_palbank = data & 0x01;
			memset(dirtybuffer,1,videoram_size);
		}
	} };
	
	public static WriteHandlerPtr spelunk2_gfxport_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		switch( offset )
		{
			case 0:
			irem_background_vscroll_w(0,data);
			break;
	
			case 1:
			irem_background_hscroll_w(0,data);
			break;
	
			case 2:
			irem_background_hscroll_w(1,(data&2)>>1);
			irem_background_vscroll_w(1,(data&1));
			if (spelunk2_palbank != ((data & 0x0c) >> 2))
			{
				spelunk2_palbank = (data & 0x0c) >> 2;
				memset(dirtybuffer,1,videoram_size);
			}
			break;
		}
	} };
	
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	static void draw_sprites(struct osd_bitmap *bitmap,
						     const struct rectangle *spritevisiblearea,
						     const struct rectangle *flipspritevisiblearea )
	{
		int offs;
	
		/* sprites must be drawn in this order to get correct priority */
		for (offs = 0;offs < spriteram_size;offs += 8)
		{
			int i,incr,code,col,flipx,flipy,sx,sy;
	
	
			code = spriteram[offs+4] + ((spriteram[offs+5] & 0x07) << 8);
			col = spriteram[offs+0] & 0x1f;
			sx = 256 * (spriteram[offs+7] & 1) + spriteram[offs+6],
			sy = 256+128-15 - (256 * (spriteram[offs+3] & 1) + spriteram[offs+2]),
			flipx = spriteram[offs+5] & 0x40;
			flipy = spriteram[offs+5] & 0x80;
	
			i = sprite_height_prom[(code >> 5) & 0x1f];
			if (i == 1)	/* double height */
			{
				code &= ~1;
				sy -= 16;
			}
			else if (i == 2)	/* quadruple height */
			{
				i = 3;
				code &= ~3;
				sy -= 3*16;
			}
	
			if (flipscreen != 0)
			{
				sx = 496 - sx;
				sy = 242 - i*16 - sy;	/* sprites are slightly misplaced by the hardware */
				flipx = !flipx;
				flipy = !flipy;
			}
	
			if (flipy != 0)
			{
				incr = -1;
				code += i;
			}
			else incr = 1;
	
			do
			{
				drawgfx(bitmap,Machine.gfx[1],
						code + i * incr,col,
						flipx,flipy,
						sx,sy + 16 * i,
						flipscreen ? flipspritevisiblearea : spritevisiblearea,TRANSPARENCY_PEN,0);
	
				i--;
			} while (i >= 0);
		}
	}
	
	
	static void draw_priority_sprites(struct osd_bitmap *bitmap, int prioritylayer)
	{
		int offs;
	
		/* sprites must be drawn in this order to get correct priority */
		for (offs = 0;offs < spriteram_size;offs += 8)
		{
			int i,incr,code,col,flipx,flipy,sx,sy;
	
	
			if (!prioritylayer || (prioritylayer && (spriteram[offs] & 0x10)))
			{
				code = spriteram[offs+4] + ((spriteram[offs+5] & 0x07) << 8);
				col = spriteram[offs+0] & 0x0f;
				sx = 256 * (spriteram[offs+7] & 1) + spriteram[offs+6],
				sy = 256+128-15 - (256 * (spriteram[offs+3] & 1) + spriteram[offs+2]),
				flipx = spriteram[offs+5] & 0x40;
				flipy = spriteram[offs+5] & 0x80;
	
				i = sprite_height_prom[(code >> 5) & 0x1f];
				if (i == 1)	/* double height */
				{
					code &= ~1;
					sy -= 16;
				}
				else if (i == 2)	/* quadruple height */
				{
					i = 3;
					code &= ~3;
					sy -= 3*16;
				}
	
				if (flipscreen != 0)
				{
					sx = 496 - sx;
					sy = 242 - i*16 - sy;	/* sprites are slightly misplaced by the hardware */
					flipx = !flipx;
					flipy = !flipy;
				}
	
				if (flipy != 0)
				{
					incr = -1;
					code += i;
				}
				else incr = 1;
	
				do
				{
					drawgfx(bitmap,Machine.gfx[1],
							code + i * incr,col,
							flipx,flipy,
							sx,sy + 16 * i,
							&Machine.drv.visible_area,TRANSPARENCY_PEN,0);
	
					i--;
				} while (i >= 0);
			}
		}
	}
	
	
	void kungfum_draw_background(struct osd_bitmap *bitmap)
	{
		int offs,i;
	
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = videoram_size/2 - 1;offs >= 0;offs--)
		{
			if (dirtybuffer[offs] || dirtybuffer[offs+0x800])
			{
				int sx,sy,flipx,flipy;
	
	
				dirtybuffer[offs] = dirtybuffer[offs+0x800] = 0;
	
				sx = offs % 64;
				sy = offs / 64;
				flipx = videoram[offs+0x800] & 0x20;
				flipy = 0;
				if (flipscreen != 0)
				{
					sx = 63 - sx;
					sy = 31 - sy;
					flipx = !flipx;
					flipy = !flipy;
				}
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram[offs] + 4 * (videoram[offs+0x800] & 0xc0),
						videoram[offs+0x800] & 0x1f,
						flipx,flipy,
						8*sx,8*sy,
						0,TRANSPARENCY_NONE,0);
			}
		}
	
	
		/* copy the temporary bitmap to the screen */
		{
			int scrollx[32];
	
	
			if (flipscreen != 0)
			{
				for (i = 31;i > 25;i--)
					scrollx[i] = 0;
				for (i = 25;i >= 0;i--)
					scrollx[i] = irem_background_hscroll;
			}
			else
			{
				for (i = 0;i < 6;i++)
					scrollx[i] = 0;
				for (i = 6;i < 32;i++)
					scrollx[i] = -irem_background_hscroll;
			}
	
			copyscrollbitmap(bitmap,tmpbitmap,32,scrollx,0,0,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
		}
	}
	
	static void battroad_draw_background(struct osd_bitmap *bitmap, int prioritylayer)
	{
		int offs;
	
	
		for (offs = videoram_size-2;offs >= 0;offs -= 2)
		{
			if ((dirtybuffer[offs] || dirtybuffer[offs+1]) && !(!prioritylayer && (videoram[offs+1] & 0x04)))
			{
				int sx,sy;
	
	
				dirtybuffer[offs] = 0;
				dirtybuffer[offs+1] = 0;
	
				sx = (offs/2) % 64;
				sy = (offs/2) / 64;
	
				if (flipscreen != 0)
				{
					sx = 63 - sx;
					sy = 31 - sy;
				}
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram[offs] + ((videoram[offs+1] & 0x40) << 3) + ((videoram[offs + 1] & 0x10) << 4),
						videoram[offs+1] & 0x0f,
						flipscreen,flipscreen,
						8*sx,8*sy,
						0,TRANSPARENCY_NONE,0);
			}
		}
	
	
		{
			int scrollx, scrolly;
	
			if (flipscreen != 0)
			{
				scrollx = irem_background_hscroll;
				scrolly = irem_background_vscroll;
			}
			else
			{
				scrollx = -irem_background_hscroll;
				scrolly = -irem_background_vscroll;
			}
	
			if (prioritylayer != 0)
			{
				copyscrollbitmap(bitmap,tmpbitmap,1,&scrollx,1,&scrolly,&Machine.drv.visible_area,TRANSPARENCY_PEN,Machine.pens[0]);
			}
			else
			{
				copyscrollbitmap(bitmap,tmpbitmap,1,&scrollx,1,&scrolly,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
			}
		}
	}
	
	void ldrun_draw_background(struct osd_bitmap *bitmap, int prioritylayer)
	{
		int offs;
	
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = videoram_size-2;offs >= 0;offs -= 2)
		{
			if ((dirtybuffer[offs] || dirtybuffer[offs+1]) && !(!prioritylayer && (videoram[offs+1] & 0x04)))
			{
				int sx,sy,flipx;
	
	
				dirtybuffer[offs] = 0;
				dirtybuffer[offs+1] = 0;
	
				sx = (offs/2) % 64;
				sy = (offs/2) / 64;
				flipx = videoram[offs+1] & 0x20;
	
				if (flipscreen != 0)
				{
					sx = 63 - sx;
					sy = 31 - sy;
					flipx = !flipx;
				}
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram[offs] + ((videoram[offs+1] & 0xc0) << 2),
						videoram[offs+1] & 0x1f,
						flipx,flipscreen,
						8*sx,8*sy,
						0,TRANSPARENCY_NONE,0);
			}
		}
	
	
		{
			int scrolly;	/* ldrun3 only */
	
			if (flipscreen != 0)
				scrolly = irem_background_vscroll;
			else
				scrolly = -irem_background_vscroll;
	
			if (prioritylayer != 0)
			{
				copyscrollbitmap(bitmap,tmpbitmap,0,0,1,&scrolly,&Machine.drv.visible_area,TRANSPARENCY_PEN,Machine.pens[0]);
			}
			else
			{
				copyscrollbitmap(bitmap,tmpbitmap,0,0,1,&scrolly,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
			}
		}
	}
	
	/* almost identical but scrolling background, more characters, */
	/* no char x flip, and more sprites */
	void ldrun4_draw_background(struct osd_bitmap *bitmap)
	{
		int offs;
	
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = videoram_size-2;offs >= 0;offs -= 2)
		{
			if (dirtybuffer[offs] || dirtybuffer[offs+1])
			{
				int sx,sy;
	
	
				dirtybuffer[offs] = 0;
				dirtybuffer[offs+1] = 0;
	
				sx = (offs/2) % 64;
				sy = (offs/2) / 64;
	
				if (flipscreen != 0)
				{
					sx = 63 - sx;
					sy = 31 - sy;
				}
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram[offs] + ((videoram[offs+1] & 0xc0) << 2) + ((videoram[offs+1] & 0x20) << 5),
						videoram[offs+1] & 0x1f,
						flipscreen,flipscreen,
						8*sx,8*sy,
						0,TRANSPARENCY_NONE,0);
			}
		}
	
	
		{
			int scrollx;
	
			if (flipscreen != 0)
				scrollx = irem_background_hscroll + 2;
			else
				scrollx = -irem_background_hscroll + 2;
	
			copyscrollbitmap(bitmap,tmpbitmap,1,&scrollx,0,0,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
		}
	}
	
	void lotlot_draw_background(struct osd_bitmap *bitmap)
	{
		int offs;
	
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = videoram_size-2;offs >= 0;offs -= 2)
		{
			if (dirtybuffer[offs] || dirtybuffer[offs+1])
			{
				int sx,sy,flipx;
	
	
				dirtybuffer[offs] = 0;
				dirtybuffer[offs+1] = 0;
	
				sx = (offs/2) % 32;
				sy = (offs/2) / 32;
				flipx = videoram[offs+1] & 0x20;
	
				if (flipscreen != 0)
				{
					sx = 31 - sx;
					sy = 31 - sy;
					flipx = !flipx;
				}
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram[offs] + ((videoram[offs+1] & 0xc0) << 2),
						videoram[offs+1] & 0x1f,
						flipx,flipscreen,
						12*sx + 64,10*sy - 32,
						0,TRANSPARENCY_NONE,0);
			}
		}
	
	
		copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	
	
		for (offs = irem_textram_size - 2;offs >= 0;offs -= 2)
		{
			int sx,sy;
	
	
			sx = (offs/2) % 32;
			sy = (offs/2) / 32;
	
			if (flipscreen != 0)
			{
				sx = 31 - sx;
				sy = 31 - sy;
			}
	
			drawgfx(bitmap,Machine.gfx[2],
					irem_textram[offs] + ((irem_textram[offs + 1] & 0xc0) << 2),
					(irem_textram[offs + 1] & 0x1f),
					flipscreen,flipscreen,
					12*sx + 64,10*sy - 32,
					&Machine.drv.visible_area,TRANSPARENCY_PEN, 0);
		}
	}
	
	static void kidniki_draw_background(struct osd_bitmap *bitmap)
	{
		int offs;
	
	
		for (offs = videoram_size-2;offs >= 0;offs -= 2)
		{
			if (dirtybuffer[offs] || dirtybuffer[offs+1])
			{
				int sx,sy;
	
	
				dirtybuffer[offs] = 0;
				dirtybuffer[offs+1] = 0;
	
				sx = (offs/2) % 64;
				sy = (offs/2) / 64;
	
				if (flipscreen != 0)
				{
					sx = 63 - sx;
					sy = 31 - sy;
				}
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram[offs] + ((videoram[offs+1] & 0xe0) << 3) + (kidniki_background_bank << 11),
						videoram[offs+1] & 0x1f,
						flipscreen,flipscreen,
						8*sx,8*sy,
						0,TRANSPARENCY_NONE,0);
			}
		}
	
	
		{
			int scrollx;
	
			if (flipscreen != 0)
				scrollx = irem_background_hscroll + 2;
			else
				scrollx = -irem_background_hscroll + 2;
	
			copyscrollbitmap(bitmap,tmpbitmap,1,&scrollx,0,0,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
		}
	}
	
	static void spelunkr_draw_background(struct osd_bitmap *bitmap)
	{
		int offs;
	
	
		for (offs = videoram_size-2;offs >= 0;offs -= 2)
		{
			if (dirtybuffer[offs] || dirtybuffer[offs+1])
			{
				int sx,sy;
	
	
				dirtybuffer[offs] = 0;
				dirtybuffer[offs+1] = 0;
	
				sx = (offs/2) % 64;
				sy = (offs/2) / 64;
	
				if (flipscreen != 0)
				{
					sx = 63 - sx;
					sy = 63 - sy;
				}
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram[offs]
								+ ((videoram[offs+1] & 0x10) << 4)
								+ ((videoram[offs+1] & 0x20) << 6)
								+ ((videoram[offs+1] & 0xc0) << 3),
						(videoram[offs+1] & 0x0f) + (spelunk2_palbank << 4),
						flipscreen,flipscreen,
						8*sx,8*sy,
						0,TRANSPARENCY_NONE,0);
			}
		}
	
	
		{
			int scrollx,scrolly;
	
			if (flipscreen != 0)
			{
				scrollx = irem_background_hscroll;
				scrolly = irem_background_vscroll - 128;
			}
			else
			{
				scrollx = -irem_background_hscroll;
				scrolly = -irem_background_vscroll - 128;
			}
	
			copyscrollbitmap(bitmap,tmpbitmap,1,&scrollx,1,&scrolly,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
		}
	}
	
	static void spelunk2_draw_background(struct osd_bitmap *bitmap)
	{
		int offs;
	
	
		for (offs = videoram_size-2;offs >= 0;offs -= 2)
		{
			if (dirtybuffer[offs] || dirtybuffer[offs+1])
			{
				int sx,sy;
	
	
				dirtybuffer[offs] = 0;
				dirtybuffer[offs+1] = 0;
	
				sx = (offs/2) % 64;
				sy = (offs/2) / 64;
	
				if (flipscreen != 0)
				{
					sx = 63 - sx;
					sy = 63 - sy;
				}
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram[offs] + ((videoram[offs+1] & 0xf0) << 4),
						(videoram[offs+1] & 0x0f) + (spelunk2_palbank << 4),
						flipscreen,flipscreen,
						8*sx,8*sy,
						0,TRANSPARENCY_NONE,0);
			}
		}
	
	
		{
			int scrollx,scrolly;
	
			if (flipscreen != 0)
			{
				scrollx = irem_background_hscroll;
				scrolly = irem_background_vscroll - 128;
			}
			else
			{
				scrollx = -irem_background_hscroll;
				scrolly = -irem_background_vscroll - 128;
			}
	
			copyscrollbitmap(bitmap,tmpbitmap,1,&scrollx,1,&scrolly,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
		}
	}
	
	
	
	
	static void battroad_draw_text(struct osd_bitmap *bitmap)
	{
		int offs;
	
	
		for (offs = irem_textram_size - 2;offs >= 0;offs -= 2)
		{
			int sx,sy;
	
	
			sx = (offs/2) % 32;
			sy = (offs/2) / 32;
	
			if (flipscreen != 0)
			{
				sx = 31 - sx;
				sy = 31 - sy;
			}
	
			drawgfx(bitmap,Machine.gfx[2],
					irem_textram[offs] + ((irem_textram[offs + 1] & 0x40) << 3) + ((irem_textram[offs + 1] & 0x10) << 4),
					(irem_textram[offs + 1] & 0x0f),
					flipscreen,flipscreen,
					8*sx+128,8*sy,
					&Machine.drv.visible_area,TRANSPARENCY_PEN, 0);
		}
	}
	
	static void kidniki_draw_text(struct osd_bitmap *bitmap)
	{
		int offs;
		int scrolly;
	
	
		if (flipscreen != 0)
			scrolly = kidniki_text_vscroll-0x180;
		else
			scrolly = -kidniki_text_vscroll+0x180;
	
	
		for (offs = irem_textram_size - 2;offs >= 0;offs -= 2)
		{
			int sx,sy;
	
	
			sx = (offs/2) % 32;
			sy = (offs/2) / 32;
	
			if (flipscreen != 0)
			{
				sx = 31 - sx;
				sy = 31 - sy;
			}
	
			drawgfx(bitmap,Machine.gfx[2],
					irem_textram[offs] + ((irem_textram[offs + 1] & 0xc0) << 2),
					(irem_textram[offs + 1] & 0x1f),
					flipscreen,flipscreen,
					12*sx + 64,8*sy + scrolly,
					&Machine.drv.visible_area,TRANSPARENCY_PEN, 0);
		}
	}
	
	static void spelunkr_draw_text(struct osd_bitmap *bitmap)
	{
		int offs;
	
	
		for (offs = irem_textram_size - 2;offs >= 0;offs -= 2)
		{
			int sx,sy;
	
	
			sx = (offs/2) % 32;
			sy = (offs/2) / 32;
	
			if (flipscreen != 0)
			{
				sx = 31 - sx;
				sy = 31 - sy;
			}
	
			drawgfx(bitmap,Machine.gfx[2],
					irem_textram[offs] + ((irem_textram[offs + 1] & 0x10) << 4),
					(irem_textram[offs + 1] & 0x0f) + (spelunk2_palbank << 4),
					flipscreen,flipscreen,
					12*sx + 64,8*sy,
					&Machine.drv.visible_area,TRANSPARENCY_PEN, 0);
		}
	}
	
	
	
	public static VhUpdatePtr kungfum_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		kungfum_draw_background(bitmap);
		draw_sprites(bitmap, &kungfum_spritevisiblearea, &kungfum_flipspritevisiblearea);
	} };
	
	public static VhUpdatePtr battroad_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		battroad_draw_background(bitmap, 0);
		draw_priority_sprites(bitmap, 0);
		battroad_draw_background(bitmap, 1);
		draw_priority_sprites(bitmap, 1);
		battroad_draw_text(bitmap);
	} };
	
	public static VhUpdatePtr ldrun_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		ldrun_draw_background(bitmap, 0);
		draw_priority_sprites(bitmap, 0);
		ldrun_draw_background(bitmap, 1);
		draw_priority_sprites(bitmap, 1);
	} };
	
	public static VhUpdatePtr ldrun4_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		ldrun4_draw_background(bitmap);
		draw_sprites(bitmap, &Machine.drv.visible_area, &Machine.drv.visible_area);
	} };
	
	public static VhUpdatePtr lotlot_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		lotlot_draw_background(bitmap);
		draw_sprites(bitmap, &Machine.drv.visible_area, &Machine.drv.visible_area);
	} };
	
	public static VhUpdatePtr kidniki_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		kidniki_draw_background(bitmap);
		draw_sprites(bitmap, &Machine.drv.visible_area, &Machine.drv.visible_area);
		kidniki_draw_text(bitmap);
	} };
	
	void spelunkr_vh_screenrefresh( struct osd_bitmap *bitmap, int full_refresh )
	{
		spelunkr_draw_background(bitmap);
		draw_sprites(bitmap, &Machine.drv.visible_area, &Machine.drv.visible_area);
		spelunkr_draw_text(bitmap);
	}
	
	void spelunk2_vh_screenrefresh( struct osd_bitmap *bitmap, int full_refresh )
	{
		spelunk2_draw_background(bitmap);
		draw_sprites(bitmap, &Machine.drv.visible_area, &Machine.drv.visible_area);
		spelunkr_draw_text(bitmap);
	}
}
