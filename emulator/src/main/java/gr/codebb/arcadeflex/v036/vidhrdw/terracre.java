/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package gr.codebb.arcadeflex.v036.vidhrdw;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;

public class terracre
{
	
	
	public static UBytePtr terrac_videoram=new UBytePtr();
	public static int[] terrac_videoram_size=new int[1];
	public static UBytePtr terrac_scrolly=new UBytePtr(2);
	
	static osd_bitmap tmpbitmap2;
	static char[] dirtybuffer2;
	
	static UBytePtr spritepalettebank=new UBytePtr();
	
	
	/***************************************************************************
	  Convert color prom.
	***************************************************************************/
	static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
	public static VhConvertColorPromHandlerPtr terrac_vh_convert_color_prom = new VhConvertColorPromHandlerPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
		//#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		//#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
                int p_inc = 0;
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2,bit3;
	
			bit0 = (color_prom.read(0) >> 0) & 0x01;
			bit1 = (color_prom.read(0) >> 1) & 0x01;
			bit2 = (color_prom.read(0) >> 2) & 0x01;
			bit3 = (color_prom.read(0) >> 3) & 0x01;
			palette[p_inc++]=(char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
			bit0 = (color_prom.read(Machine.drv.total_colors) >> 0) & 0x01;
			bit1 = (color_prom.read(Machine.drv.total_colors) >> 1) & 0x01;
			bit2 = (color_prom.read(Machine.drv.total_colors) >> 2) & 0x01;
			bit3 = (color_prom.read(Machine.drv.total_colors) >> 3) & 0x01;
			palette[p_inc++]=(char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
			bit0 = (color_prom.read(2*Machine.drv.total_colors) >> 0) & 0x01;
			bit1 = (color_prom.read(2*Machine.drv.total_colors) >> 1) & 0x01;
			bit2 = (color_prom.read(2*Machine.drv.total_colors) >> 2) & 0x01;
			bit3 = (color_prom.read(2*Machine.drv.total_colors) >> 3) & 0x01;
			palette[p_inc++]=(char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
	
			color_prom.inc();
		}
	
		color_prom.inc(2*Machine.drv.total_colors);
		/* color_prom now points to the beginning of the lookup table */
	
	
		/* characters use colors 0-15 */
		for (i = 0;i < TOTAL_COLORS(0);i++)
			colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char)i;
	
		/* background tiles use colors 192-255 in four banks */
		/* the bottom two bits of the color code select the palette bank for */
		/* pens 0-7; the top two bits for pens 8-15. */
		for (i = 0;i < TOTAL_COLORS(1);i++)
		{
			if ((i & 8) != 0) 
                        {
                            colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char)(192 + (i & 0x0f) + ((i & 0xc0) >> 2));
                        }
			else 
                        {
                            colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char)(192 + (i & 0x0f) + ((i & 0x30) >> 0));
                        }
		}
	
		/* sprites use colors 128-191 in four banks */
		/* The lookup table tells which colors to pick from the selected bank */
		/* the bank is selected by another PROM and depends on the top 8 bits of */
		/* the sprite code. The PROM selects the bank *separately* for pens 0-7 and */
		/* 8-15 (like for tiles). */
		for (i = 0;i < TOTAL_COLORS(2)/16;i++)
		{
			int j;
	
			for (j = 0;j < 16;j++)
			{
				if ((i & 8) != 0)
					colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start +i + j * (TOTAL_COLORS(2)/16)] = (char)(128 + ((j & 0x0c) << 2) + (color_prom.read() & 0x0f));
				else
					colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start +i + j * (TOTAL_COLORS(2)/16)] = (char)(128 + ((j & 0x03) << 4) + (color_prom.read() & 0x0f));
			}
	
			color_prom.inc();
		}
	
		/* color_prom now points to the beginning of the sprite palette bank table */
		spritepalettebank = new UBytePtr(color_prom);	/* we'll need it at run time */
	} };
	
	
	
	public static WriteHandlerPtr terrac_videoram2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int oldword = terrac_videoram.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword,data);
	
		if (oldword != newword)
		{
			terrac_videoram.WRITE_WORD(offset,newword);
			dirtybuffer2[offset] = 1;
			dirtybuffer2[offset+1] = 1;
		}
	} };
	
	public static ReadHandlerPtr terrac_videoram2_r = new ReadHandlerPtr() { public int handler(int offset)
	{
	   return terrac_videoram.READ_WORD(offset);
	} };
	
	
	/***************************************************************************
	  Stop the video hardware emulation.
	***************************************************************************/
	
	public static VhStopHandlerPtr terrac_vh_stop = new VhStopHandlerPtr() { public void handler() 
	{
	        dirtybuffer2=null;
		osd_free_bitmap(tmpbitmap2);
		generic_vh_stop.handler();
	} };
	
	/***************************************************************************
	  Start the video hardware emulation.
	***************************************************************************/
	
	
	public static VhStartHandlerPtr terrac_vh_start = new VhStartHandlerPtr() { public int handler() 
	{
		if (generic_vh_start.handler()!= 0)
			return 1;
	
		if ((dirtybuffer2 = new char[terrac_videoram_size[0]]) == null)
		{
			terrac_vh_stop.handler();
			return 1;
		}
		memset(dirtybuffer2,1,terrac_videoram_size[0]);
	
		/* the background area is 4 x 1 (90 Rotated!) */
		if ((tmpbitmap2 = osd_new_bitmap(4*Machine.drv.screen_width,
				1*Machine.drv.screen_height,Machine.scrbitmap.depth)) == null)
		{
			terrac_vh_stop.handler();
			return 1;
		}
	
		return 0;
	} };
	
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdateHandlerPtr terracre_vh_screenrefresh = new VhUpdateHandlerPtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs,x,y;
	
	
		for (y = 0; y < 64; y++)
		{
			for (x = 0; x < 16; x++)
			{
				if ((dirtybuffer2[x*2 + y*64]!=0) || (dirtybuffer2[x*2 + y*64+1])!=0)
				{
					int code = terrac_videoram.READ_WORD(x*2 + y*64) & 0x01ff;
					int color = (terrac_videoram.READ_WORD(x*2 + y*64)&0x7800)>>11;
	
					dirtybuffer2[x*2 + y*64] = dirtybuffer2[x*2 + y*64+1] = 0;
	
					drawgfx(tmpbitmap2,Machine.gfx[1],
							code,
							color,
							0,0,
							16 * y,16 * x,
							null,TRANSPARENCY_NONE,0);
				}
			}
		}
	
		/* copy the background graphics */
		if ((terrac_scrolly.READ_WORD(0) & 0x2000)!=0)	/* background disable */
			fillbitmap(bitmap,Machine.pens[0],Machine.drv.visible_area);
		else
		{
			int scrollx;
	
			scrollx = -terrac_scrolly.READ_WORD(0);
	
			copyscrollbitmap(bitmap,tmpbitmap2,1,new int[]{scrollx},0,null,Machine.drv.visible_area,TRANSPARENCY_NONE,0);
		}
	
	
	
		for (x = 0;x <spriteram_size[0];x += 8)
		{
			int code;
			int attr = spriteram.READ_WORD(x+4) & 0xff;
			int color = (attr & 0xf0) >> 4;
			int flipx = attr & 0x04;
			int flipy = attr & 0x08;
			int sx,sy;
	
			sx = (spriteram.READ_WORD(x+6) & 0xff) - 0x80 + 256 * (attr & 1);
			sy = 240 - (spriteram.READ_WORD(x) & 0xff);
	
			code = (spriteram.READ_WORD(x+2) & 0xff) + ((attr & 0x02) << 7);
	
			drawgfx(bitmap,Machine.gfx[2],
					code,
					color + 16 * (spritepalettebank.read(code >> 1) & 0x0f),
					flipx,flipy,
					sx,sy,
					Machine.drv.visible_area,TRANSPARENCY_PEN,0);
		}
	
	
		for (offs = videoram_size[0] - 2;offs >= 0;offs -= 2)
		{
			int sx,sy;
	
	
			sx = (offs/2) / 32;
			sy = (offs/2) % 32;
	
			drawgfx(bitmap,Machine.gfx[0],
					videoram.READ_WORD(offs) & 0xff,
					0,
					0,0,
					8*sx,8*sy,
					Machine.drv.visible_area,TRANSPARENCY_PEN,15);
		}
	} };
}
