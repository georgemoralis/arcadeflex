/***************************************************************************

	vidhrdw.c

	Functions to emulate the video hardware of the machine.

	There are only a few differences between the video hardware of Mysterious
	Stones and Mat Mania. The tile bank select bit is different and the sprite
	selection seems to be different as well. Additionally, the palette is stored
	differently. I'm also not sure that the 2nd tile page is really used in
	Mysterious Stones.

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package gr.codebb.arcadeflex.v036.vidhrdw;
import static gr.codebb.arcadeflex.v036.platform.ptrlib.*;
import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.palette.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;


public class matmania
{
	
	
	
	public static UBytePtr matmania_videoram2=new UBytePtr();
        public static UBytePtr matmania_colorram2=new UBytePtr();
	public static int[] matmania_videoram2_size=new int[1];
	public static UBytePtr matmania_videoram3=new UBytePtr();
        public static UBytePtr matmania_colorram3=new UBytePtr();
	public static int[] matmania_videoram3_size=new int[1];
	public static UBytePtr matmania_scroll=new UBytePtr();
	static osd_bitmap tmpbitmap2;
	static char[] dirtybuffer2;
	
	public static UBytePtr matmania_pageselect=new UBytePtr();
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Mat Mania is unusual in that it has both PROMs and RAM to control the
	  palette. PROMs are used for characters and background tiles, RAM for
	  sprites.
	  I don't know for sure how the PROMs are connected to the RGB output,
	  but it's probably the usual:
	
	  bit 7 -- 220 ohm resistor  -- GREEN
	        -- 470 ohm resistor  -- GREEN
	        -- 1  kohm resistor  -- GREEN
	        -- 2.2kohm resistor  -- GREEN
	        -- 220 ohm resistor  -- RED
	        -- 470 ohm resistor  -- RED
	        -- 1  kohm resistor  -- RED
	  bit 0 -- 2.2kohm resistor  -- RED
	
	  bit 3 -- 220 ohm resistor  -- BLUE
	        -- 470 ohm resistor  -- BLUE
	        -- 1  kohm resistor  -- BLUE
	  bit 0 -- 2.2kohm resistor  -- BLUE
	
	***************************************************************************/
	public static VhConvertColorPromPtr matmania_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
		//#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		//#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
                int p_inc=0;
		for (i = 0;i < 64;i++)
		{
			int bit0,bit1,bit2,bit3;
	
	
			bit0 = (color_prom.read(0) >> 0) & 0x01;
			bit1 = (color_prom.read(0) >> 1) & 0x01;
			bit2 = (color_prom.read(0) >> 2) & 0x01;
			bit3 = (color_prom.read(0) >> 3) & 0x01;
			palette[p_inc++]=(char)(0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
			bit0 = (color_prom.read(0) >> 4) & 0x01;
			bit1 = (color_prom.read(0) >> 5) & 0x01;
			bit2 = (color_prom.read(0) >> 6) & 0x01;
			bit3 = (color_prom.read(0) >> 7) & 0x01;
			palette[p_inc++]=(char)(0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
			bit0 = (color_prom.read(64) >> 0) & 0x01;
			bit1 = (color_prom.read(64) >> 1) & 0x01;
			bit2 = (color_prom.read(64) >> 2) & 0x01;
			bit3 = (color_prom.read(64) >> 3) & 0x01;
			palette[p_inc++]=(char)(0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
	
			color_prom.inc();
		}
	} };
	
	
	
	public static WriteHandlerPtr matmania_paletteram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int bit0,bit1,bit2,bit3,val;
		int r,g,b;
		int offs2;
	
	
		paletteram.write(offset,data);
		offs2 = offset & 0x0f;
	
		val = paletteram.read(offs2);
		bit0 = (val >> 0) & 0x01;
		bit1 = (val >> 1) & 0x01;
		bit2 = (val >> 2) & 0x01;
		bit3 = (val >> 3) & 0x01;
		r = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
		val = paletteram.read(offs2 | 0x10);
		bit0 = (val >> 0) & 0x01;
		bit1 = (val >> 1) & 0x01;
		bit2 = (val >> 2) & 0x01;
		bit3 = (val >> 3) & 0x01;
		g = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
		val = paletteram.read(offs2 | 0x20);
		bit0 = (val >> 0) & 0x01;
		bit1 = (val >> 1) & 0x01;
		bit2 = (val >> 2) & 0x01;
		bit3 = (val >> 3) & 0x01;
		b = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
		palette_change_color(offs2 + 64,r,g,b);
	} };
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	public static VhStartPtr matmania_vh_start = new VhStartPtr() { public int handler() 
	{
		if ((dirtybuffer = new char[videoram_size[0]]) == null)
			return 1;
		memset(dirtybuffer,1,videoram_size[0]);
	
		if ((dirtybuffer2 = new char[matmania_videoram3_size[0]]) == null)
		{
			dirtybuffer=null;
			return 1;
		}
		memset(dirtybuffer2,1,matmania_videoram3_size[0]);
	
		/* Mat Mania has a virtual screen twice as large as the visible screen */
		if ((tmpbitmap = osd_create_bitmap(Machine.drv.screen_width,2* Machine.drv.screen_height)) == null)
		{
			dirtybuffer=null;
			dirtybuffer2=null;
			return 1;
		}
	
		/* Mat Mania has a virtual screen twice as large as the visible screen */
		if ((tmpbitmap2 = osd_create_bitmap(Machine.drv.screen_width,2 * Machine.drv.screen_height)) == null)
		{
			tmpbitmap=null;
			dirtybuffer=null;
			dirtybuffer2=null;
			return 1;
		}
	
		return 0;
	} };
	
	
	
	/***************************************************************************
	
	  Stop the video hardware emulation.
	
	***************************************************************************/
	public static VhStopPtr matmania_vh_stop = new VhStopPtr() { public void handler() 
	{
		dirtybuffer=null;
		dirtybuffer2=null;
		osd_free_bitmap(tmpbitmap);
		osd_free_bitmap(tmpbitmap2);
	} };
	
	
	public static WriteHandlerPtr matmania_videoram3_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (matmania_videoram3.read(offset) != data)
		{
			dirtybuffer2[offset] = 1;
	
			matmania_videoram3.write(offset,data);
		}
	} };
	
	
	
	public static WriteHandlerPtr matmania_colorram3_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (matmania_colorram3.read(offset) != data)
		{
			dirtybuffer2[offset] = 1;
	
			matmania_colorram3.write(offset,data);
		}
	} };
	
	
	public static VhUpdatePtr matmania_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
	
	
		if (palette_recalc()!=null)
		{
			memset(dirtybuffer,1,videoram_size[0]);
			memset(dirtybuffer2,1,matmania_videoram3_size[0]);
		}
	
		/* Update the tiles in the left tile ram bank */
		for (offs = videoram_size[0] - 1;offs >= 0;offs--)
		{
			if (dirtybuffer[offs]!=0)
			{
				int sx,sy;
	
	
				dirtybuffer[offs] = 0;
	
				sx = 15 - offs / 32;
				sy = offs % 32;
	
				drawgfx(tmpbitmap,Machine.gfx[1],
						videoram.read(offs) + ((colorram.read(offs) & 0x08) << 5),
						(colorram.read(offs) & 0x30) >> 4,
						0,(sy >= 16)?1:0,	/* flip horizontally tiles on the right half of the bitmap */
						16*sx,16*sy,
						null,TRANSPARENCY_NONE,0);
			}
		}
	
		/* Update the tiles in the right tile ram bank */
		for (offs = matmania_videoram3_size[0] - 1;offs >= 0;offs--)
		{
			if (dirtybuffer2[offs]!=0)
			{
				int sx,sy;
	
	
				dirtybuffer2[offs] = 0;
	
				sx = 15 - offs / 32;
				sy = offs % 32;
	
				drawgfx(tmpbitmap2,Machine.gfx[1],
						matmania_videoram3.read(offs) + ((matmania_colorram3.read(offs)& 0x08) << 5),
						(matmania_colorram3.read(offs) & 0x30) >> 4,
						0,(sy >= 16)?1:0,	/* flip horizontally tiles on the right half of the bitmap */
						16*sx,16*sy,
						null,TRANSPARENCY_NONE,0);
			}
		}
	
	
		/* copy the temporary bitmap to the screen */
		{
			int scrolly;
	
	
			scrolly = -matmania_scroll.read();
			if (matmania_pageselect.read()!=0)
				copyscrollbitmap(bitmap,tmpbitmap2,0,null,1,new int[]{scrolly},Machine.drv.visible_area,TRANSPARENCY_NONE,0);
			else
				copyscrollbitmap(bitmap,tmpbitmap,0,null,1,new int[]{scrolly},Machine.drv.visible_area,TRANSPARENCY_NONE,0);
		}
	
	
		/* Draw the sprites */
		for (offs = 0;offs < spriteram_size[0];offs += 4)
		{
			if ((spriteram.read(offs) & 0x01)!=0)
			{
				drawgfx(bitmap,Machine.gfx[2],
						spriteram.read(offs+1) + ((spriteram.read(offs) & 0xf0) << 4),
						(spriteram.read(offs) & 0x08) >> 3,
						spriteram.read(offs) & 0x04,spriteram.read(offs) & 0x02,
						239 - spriteram.read(offs+3),(240 - spriteram.read(offs+2)) & 0xff,
						Machine.drv.visible_area,TRANSPARENCY_PEN,0);
			}
		}
	
	
		/* draw the frontmost playfield. They are characters, but draw them as sprites */
		for (offs = matmania_videoram2_size[0] - 1;offs >= 0;offs--)
		{
			int sx,sy;
	
	
			sx = 31 - offs / 32;
			sy = offs % 32;
	
			drawgfx(bitmap,Machine.gfx[0],
					matmania_videoram2.read(offs) + 256 * (matmania_colorram2.read(offs) & 0x07),
					(matmania_colorram2.read(offs) & 0x30) >> 4,
					0,0,
					8*sx,8*sy,
                                        Machine.drv.visible_area,TRANSPARENCY_PEN,0);
		}
	} };
	
	public static VhUpdatePtr maniach_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
	
	
		if (palette_recalc()!=null)
		{
			memset(dirtybuffer,1,videoram_size[0]);
			memset(dirtybuffer2,1,matmania_videoram3_size[0]);
		}
	
		/* Update the tiles in the left tile ram bank */
		for (offs = videoram_size[0] - 1;offs >= 0;offs--)
		{
			if (dirtybuffer[offs]!=0)
			{
				int sx,sy;
	
	
				dirtybuffer[offs] = 0;
	
				sx = 15 - offs / 32;
				sy = offs % 32;
	
				drawgfx(tmpbitmap,Machine.gfx[1],
						videoram.read(offs) + ((colorram.read(offs) & 0x03) << 8),
						(colorram.read(offs) & 0x30) >> 4,
						0,(sy >= 16)?1:0,	/* flip horizontally tiles on the right half of the bitmap */
						16*sx,16*sy,
						null,TRANSPARENCY_NONE,0);
			}
		}
	
		/* Update the tiles in the right tile ram bank */
		for (offs = matmania_videoram3_size[0] - 1;offs >= 0;offs--)
		{
			if (dirtybuffer2[offs]!=0)
			{
				int sx,sy;
	
	
				dirtybuffer2[offs] = 0;
	
				sx = 15 - offs / 32;
				sy = offs % 32;
	
				drawgfx(tmpbitmap2,Machine.gfx[1],
						matmania_videoram3.read(offs) + ((matmania_colorram3.read(offs) & 0x03) << 8),
						(matmania_colorram3.read(offs) & 0x30) >> 4,
						0,(sy >= 16)?1:0,	/* flip horizontally tiles on the right half of the bitmap */
						16*sx,16*sy,
						null,TRANSPARENCY_NONE,0);
			}
		}
	
	
		/* copy the temporary bitmap to the screen */
		{
			int scrolly;
	
                        scrolly = -matmania_scroll.read();
			if (matmania_pageselect.read()!=0)
				copyscrollbitmap(bitmap,tmpbitmap2,0,null,1,new int[]{scrolly},Machine.drv.visible_area,TRANSPARENCY_NONE,0);
			else
				copyscrollbitmap(bitmap,tmpbitmap,0,null,1,new int[]{scrolly},Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	
		}
	
	
		/* Draw the sprites */
		for (offs = 0;offs < spriteram_size[0];offs += 4)
		{
			if ((spriteram.read(offs) & 0x01)!=0)
			{
				drawgfx(bitmap,Machine.gfx[2],
						spriteram.read(offs+1) + ((spriteram.read(offs) & 0xf0) << 4),
						(spriteram.read(offs) & 0x08) >> 3,
						spriteram.read(offs) & 0x04,spriteram.read(offs) & 0x02,
						239 - spriteram.read(offs+3),(240 - spriteram.read(offs+2)) & 0xff,
						Machine.drv.visible_area,TRANSPARENCY_PEN,0);
			}
		}
	
	
		/* draw the frontmost playfield. They are characters, but draw them as sprites */
		for (offs = matmania_videoram2_size[0] - 1;offs >= 0;offs--)
		{
			int sx,sy;
	
	
			sx = 31 - offs / 32;
			sy = offs % 32;
	
			drawgfx(bitmap,Machine.gfx[0],
					matmania_videoram2.read(offs) + 256 * (matmania_colorram2.read(offs) & 0x07),
					(matmania_colorram2.read(offs) & 0x30) >> 4,
					0,0,
					8*sx,8*sy,
					Machine.drv.visible_area,TRANSPARENCY_PEN,0);
		}
	} };
}
