/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 *
 *
 *
 */ 
package gr.codebb.arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.v036.platform.ptrlib.*;

public class punchout
{
	
	
	public static final int TOP_MONITOR_ROWS =30;
	public static final int BOTTOM_MONITOR_ROWS =30;
	
	public static final int BIGSPRITE_WIDTH =128;
	public static final int BIGSPRITE_HEIGHT =256;
	public static final int ARMWREST_BIGSPRITE_WIDTH =256;
	public static final int ARMWREST_BIGSPRITE_HEIGHT =128;
	
	public static UBytePtr punchout_videoram2=new UBytePtr();
	public static int[] punchout_videoram2_size=new int[1];
	public static UBytePtr punchout_bigsprite1ram=new UBytePtr();
	public static int[] punchout_bigsprite1ram_size=new int[1];
	public static UBytePtr punchout_bigsprite2ram=new UBytePtr();
	public static int[] punchout_bigsprite2ram_size=new int[1];
	public static UBytePtr punchout_scroll=new UBytePtr();
	public static UBytePtr punchout_bigsprite1=new UBytePtr();
	public static UBytePtr punchout_bigsprite2=new UBytePtr();
	public static UBytePtr punchout_palettebank=new UBytePtr();
	static char[] dirtybuffer2;
        static char[] bs1dirtybuffer;
        static char[] bs2dirtybuffer;
	static osd_bitmap bs1tmpbitmap;
        static osd_bitmap bs2tmpbitmap;
	
	static int top_palette_bank,bottom_palette_bank;
	
	static rectangle topvisiblearea = new rectangle
	(
		0*8, 32*8-1,
		0*8, (TOP_MONITOR_ROWS-2)*8-1
        );
	static rectangle bottomvisiblearea = new rectangle
	(
		0*8, 32*8-1,
		(TOP_MONITOR_ROWS+2)*8, (TOP_MONITOR_ROWS+BOTTOM_MONITOR_ROWS)*8-1
        );
	static rectangle backgroundvisiblearea = new rectangle
	(
		0*8, 64*8-1,
		(TOP_MONITOR_ROWS+2)*8, (TOP_MONITOR_ROWS+BOTTOM_MONITOR_ROWS)*8-1
        );
	
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Punch Out has a six 512x4 palette PROMs (one per gun; three for the top
	  monitor chars, three for everything else).
	  The PROMs are connected to the RGB output this way:
	
	  bit 3 -- 240 ohm resistor -- inverter  -- RED/GREEN/BLUE
	        -- 470 ohm resistor -- inverter  -- RED/GREEN/BLUE
	        -- 1  kohm resistor -- inverter  -- RED/GREEN/BLUE
	  bit 0 -- 2  kohm resistor -- inverter  -- RED/GREEN/BLUE
	
	***************************************************************************/
	static void convert_palette(UByte []palette,UBytePtr color_prom)
	{
		int i;
	
                int p_inc=0;
		for (i = 0;i < 1024;i++)
		{
			int bit0,bit1,bit2,bit3;
	
	
			bit0 = (color_prom.read(0) >> 0) & 0x01;
			bit1 = (color_prom.read(0) >> 1) & 0x01;
			bit2 = (color_prom.read(0) >> 2) & 0x01;
			bit3 = (color_prom.read(0) >> 3) & 0x01;
			palette[p_inc++].set((char)(255 - (0x10 * bit0 + 0x21 * bit1 + 0x46 * bit2 + 0x88 * bit3)));
			bit0 = (color_prom.read(1024) >> 0) & 0x01;
			bit1 = (color_prom.read(1024) >> 1) & 0x01;
			bit2 = (color_prom.read(1024) >> 2) & 0x01;
			bit3 = (color_prom.read(1024) >> 3) & 0x01;
			palette[p_inc++].set((char)(255 - (0x10 * bit0 + 0x21 * bit1 + 0x46 * bit2 + 0x88 * bit3)));
			bit0 = (color_prom.read(2*1024) >> 0) & 0x01;
			bit1 = (color_prom.read(2*1024) >> 1) & 0x01;
			bit2 = (color_prom.read(2*1024) >> 2) & 0x01;
			bit3 = (color_prom.read(2*1024) >> 3) & 0x01;
			palette[p_inc++].set((char)(255 - (0x10 * bit0 + 0x21 * bit1 + 0x46 * bit2 + 0x88 * bit3)));
	
			color_prom.inc();
		}
	
		/* reserve the last color for the transparent pen (none of the game colors has */
		/* these RGB components) */
		palette[p_inc++].set((char)(240));
		palette[p_inc++].set((char)(240));
		palette[p_inc++].set((char)(240));
	}
	
	
	/* these depend on jumpers on the board and change from game to game */
	static int gfx0inv,gfx1inv,gfx2inv,gfx3inv;
	static int TOTAL_COLORS(int gfxn) 
        {
            return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
        }
        public static VhConvertColorPromPtr punchout_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(UByte []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
		//#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		//#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + (offs)])
	
	
		convert_palette(palette,color_prom);
	
	
		/* top monitor chars */
		for (i = 0;i < TOTAL_COLORS(0);i++)
                    colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i ^ gfx0inv] = (char) (i);
	
		/* bottom monitor chars */
		for (i = 0;i < TOTAL_COLORS(1);i++)
                        colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i ^ gfx1inv] = (char) (i + 512);
	
		/* big sprite #1 */
		for (i = 0;i < TOTAL_COLORS(2);i++)
		{
			if (i % 8 == 0) 
                        {
                            //COLOR(2,i ^ gfx2inv) = 1024;
                            colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i ^ gfx2inv] = (char) (1024);
                        }	/* transparent */
			else 
                        {
                            //COLOR(2,i ^ gfx2inv) = i + 512;
                            colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i ^ gfx2inv] = (char) (i+512);
                        }
		}
	
		/* big sprite #2 */
		for (i = 0;i < TOTAL_COLORS(3);i++)
		{
			if (i % 4 == 0)
                        {
                            //COLOR(3,i ^ gfx3inv) = 1024;	/* transparent */
                            colortable[Machine.drv.gfxdecodeinfo[3].color_codes_start + i ^ gfx3inv] = (char) (1024);
                        }
			else 
                        {
                            //COLOR(3,i ^ gfx3inv) = i + 512;
                            colortable[Machine.drv.gfxdecodeinfo[3].color_codes_start + i ^ gfx3inv] = (char) (i+512);
                        }
		}
	}};
	public static VhConvertColorPromPtr armwrest_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(UByte []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
		//#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		//#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + (offs)])
	
	
		convert_palette(palette,color_prom);
	
	
		/* top monitor / bottom monitor backround chars */
		for (i = 0;i < TOTAL_COLORS(0);i++)
			colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) (i);
	
		/* bottom monitor foreground chars */
		for (i = 0;i < TOTAL_COLORS(1);i++)
			colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char) (i + 512);
	
		/* big sprite #1 */
		for (i = 0;i < TOTAL_COLORS(2);i++)
		{
			if (i % 8 == 7) 
                        {
                            colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i] = (char) (1024);	/* transparent */
                        }
			else 
                        { 
                            colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i] = (char) (i + 512);
                        }
		}
	
		/* big sprite #2 - pen order is inverted */
		for (i = 0;i < TOTAL_COLORS(3);i++)
		{
			if (i % 4 == 3)
                        {
                            //COLOR(3,i ^ 3) = 1024;	/* transparent */
                            colortable[Machine.drv.gfxdecodeinfo[3].color_codes_start + i ^ 3] = (char) (1024);
                        }
			else
                        {
                            //COLOR(3,i ^ 3) = i + 512;
                            colortable[Machine.drv.gfxdecodeinfo[3].color_codes_start + i ^ 3] = (char) (i+512);
                        }
		}
	}};
	
	
	
	static void gfx_fix()
	{
		/* one graphics ROM (4v) doesn't */
		/* exist but must be seen as a 0xff fill for colors to come out properly */
		//memset(memory_region(REGION_GFX3) + 0x2c000,0xff,0x4000);
                for(int i=0; i<0x4000; i++)
                {
                    memory_region(REGION_GFX3).write(0x2c000+i, 0xff);
                }
	}
	public static InitDriverPtr init_punchout = new InitDriverPtr() { public void handler() 
	{
		gfx_fix();
	
		gfx0inv = 0x03;
		gfx1inv = 0xfc;
		gfx2inv = 0xff;
		gfx3inv = 0xfc;
	}};
	public static InitDriverPtr init_spnchout = new InitDriverPtr() { public void handler() 
	{
		gfx_fix();
	
		gfx0inv = 0x00;
		gfx1inv = 0xff;
		gfx2inv = 0xff;
		gfx3inv = 0xff;
	}};
	public static InitDriverPtr init_spnchotj = new InitDriverPtr() { public void handler() 
	{
		gfx_fix();
	
		gfx0inv = 0xfc;
		gfx1inv = 0xff;
		gfx2inv = 0xff;
		gfx3inv = 0xff;
	}};
	public static InitDriverPtr init_armwrest = new InitDriverPtr() { public void handler() 
	{
		gfx_fix();
	
		/* also, ROM 2k is enabled only when its top half is accessed. The other half must */
		/* be seen as a 0xff fill for colors to come out properly */
		//memset(memory_region(REGION_GFX2) + 0x08000,0xff,0x2000);
                for(int i=0; i<0x2000; i++)
                {
                    memory_region(REGION_GFX2).write(0x08000+i, 0xff);
                }
	}};
	
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	public static VhStartPtr punchout_vh_start = new VhStartPtr() { public int handler() 
	{
		if ((dirtybuffer = new char[videoram_size[0]]) == null)
			return 1;
		memset(dirtybuffer,1,videoram_size[0]);
	
		if ((dirtybuffer2 = new char[punchout_videoram2_size[0]]) == null)
		{
			dirtybuffer=null;
			return 1;
		}
		memset(dirtybuffer2,1,punchout_videoram2_size[0]);
	
		if ((tmpbitmap = osd_create_bitmap(512,480)) == null)
		{
			dirtybuffer=null;
			dirtybuffer2=null;
			return 1;
		}
	
		if ((bs1dirtybuffer = new char[punchout_bigsprite1ram_size[0]]) == null)
		{
			osd_free_bitmap(tmpbitmap);
			dirtybuffer=null;
			dirtybuffer2=null;
			return 1;
		}
		memset(bs1dirtybuffer,1,punchout_bigsprite1ram_size[0]);
	
		if ((bs1tmpbitmap = osd_create_bitmap(BIGSPRITE_WIDTH,BIGSPRITE_HEIGHT)) == null)
		{
			osd_free_bitmap(tmpbitmap);
			dirtybuffer=null;
			dirtybuffer2=null;
			bs1dirtybuffer=null;
			return 1;
		}
	
		if ((bs2dirtybuffer = new char[punchout_bigsprite2ram_size[0]]) == null)
		{
			osd_free_bitmap(tmpbitmap);
			osd_free_bitmap(bs1tmpbitmap);
			dirtybuffer=null;
			dirtybuffer2=null;
			bs1dirtybuffer=null;
			return 1;
		}
		memset(bs2dirtybuffer,1,punchout_bigsprite2ram_size[0]);
	
		if ((bs2tmpbitmap = osd_create_bitmap(BIGSPRITE_WIDTH,BIGSPRITE_HEIGHT)) == null)
		{
			osd_free_bitmap(tmpbitmap);
			osd_free_bitmap(bs1tmpbitmap);
			dirtybuffer=null;
			dirtybuffer2=null;
			bs1dirtybuffer=null;
			bs2dirtybuffer=null;
			return 1;
		}
	
		return 0;
	} };
	
	public static VhStartPtr armwrest_vh_start = new VhStartPtr() { public int handler() 
	{
		if ((dirtybuffer = new char[videoram_size[0]]) == null)
			return 1;
		memset(dirtybuffer,1,videoram_size[0]);
	
		if ((dirtybuffer2 =new char[punchout_videoram2_size[0]]) == null)
		{
			dirtybuffer=null;
			return 1;
		}
		memset(dirtybuffer2,1,punchout_videoram2_size[0]);
	
		if ((tmpbitmap = osd_create_bitmap(512,480)) == null)
		{
			dirtybuffer=null;
			dirtybuffer2=null;
			return 1;
		}
	
		if ((bs1dirtybuffer = new char[punchout_bigsprite1ram_size[0]]) == null)
		{
			osd_free_bitmap(tmpbitmap);
			dirtybuffer=null;
			dirtybuffer2=null;
			return 1;
		}
		memset(bs1dirtybuffer,1,punchout_bigsprite1ram_size[0]);
	
		if ((bs1tmpbitmap = osd_create_bitmap(ARMWREST_BIGSPRITE_WIDTH,ARMWREST_BIGSPRITE_HEIGHT)) == null)
		{
			osd_free_bitmap(tmpbitmap);
			dirtybuffer=null;
			dirtybuffer2=null;
			bs1dirtybuffer=null;
			return 1;
		}
	
		if ((bs2dirtybuffer = new char[punchout_bigsprite2ram_size[0]]) == null)
		{
			osd_free_bitmap(tmpbitmap);
			osd_free_bitmap(bs1tmpbitmap);
			dirtybuffer=null;
			dirtybuffer2=null;
			bs1dirtybuffer=null;
			return 1;
		}
		memset(bs2dirtybuffer,1,punchout_bigsprite2ram_size[0]);
	
		if ((bs2tmpbitmap = osd_create_bitmap(BIGSPRITE_WIDTH,BIGSPRITE_HEIGHT)) == null)
		{
			osd_free_bitmap(tmpbitmap);
			osd_free_bitmap(bs1tmpbitmap);
			dirtybuffer=null;
			dirtybuffer2=null;
			bs1dirtybuffer=null;
			bs2dirtybuffer=null;
			return 1;
		}
	
		return 0;
	} };
	
	
	
	/***************************************************************************
	
	  Stop the video hardware emulation.
	
	***************************************************************************/
	public static VhStopPtr punchout_vh_stop = new VhStopPtr() { public void handler() 
	{
		dirtybuffer=null;
		dirtybuffer2=null;
		bs1dirtybuffer=null;
		bs2dirtybuffer=null;
		osd_free_bitmap(tmpbitmap);
		osd_free_bitmap(bs1tmpbitmap);
		osd_free_bitmap(bs2tmpbitmap);
	} };
	
	
	
	public static WriteHandlerPtr punchout_videoram2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (punchout_videoram2.read(offset) != data)
		{
			dirtybuffer2[offset] = 1;
	
			punchout_videoram2.write(offset,data);
		}
	} };
	
	public static WriteHandlerPtr punchout_bigsprite1ram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (punchout_bigsprite1ram.read(offset) != data)
		{
			bs1dirtybuffer[offset] = 1;
	
			punchout_bigsprite1ram.write(offset,data);
		}
	} };
	
	public static WriteHandlerPtr punchout_bigsprite2ram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (punchout_bigsprite2ram.read(offset) != data)
		{
			bs2dirtybuffer[offset] = 1;
	
			punchout_bigsprite2ram.write(offset,data);
		}
	} };
	
	
	
	public static WriteHandlerPtr punchout_palettebank_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		punchout_palettebank.write(data);
	
		if (top_palette_bank != ((data >> 1) & 0x01))
		{
			top_palette_bank = (data >> 1) & 0x01;
			memset(dirtybuffer,1,videoram_size[0]);
		}
		if (bottom_palette_bank != ((data >> 0) & 0x01))
		{
			bottom_palette_bank = (data >> 0) & 0x01;
			memset(dirtybuffer2,1,punchout_videoram2_size[0]);
			memset(bs1dirtybuffer,1,punchout_bigsprite1ram_size[0]);
			memset(bs2dirtybuffer,1,punchout_bigsprite2ram_size[0]);
		}
	} };
	
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr punchout_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
	
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = videoram_size[0] - 2;offs >= 0;offs -= 2)
		{
			if (dirtybuffer[offs]!=0 || dirtybuffer[offs + 1]!=0)
			{
				int sx,sy;
	
	
				dirtybuffer[offs] = 0;
				dirtybuffer[offs + 1] = 0;
	
				sx = offs/2 % 32;
				sy = offs/2 / 32;
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram.read(offs) + 256 * (videoram.read(offs + 1) & 0x03),
						((videoram.read(offs + 1) & 0x7c) >> 2) + 64 * top_palette_bank,
						videoram.read(offs + 1) & 0x80,0,
						8*sx,8*sy - 8*(32-TOP_MONITOR_ROWS),
						topvisiblearea,TRANSPARENCY_NONE,0);
			}
		}
	
		for (offs = punchout_videoram2_size[0] - 2;offs >= 0;offs -= 2)
		{
			if (dirtybuffer2[offs]!=0 | dirtybuffer2[offs + 1]!=0)
			{
				int sx,sy;
	
	
				dirtybuffer2[offs] = 0;
				dirtybuffer2[offs + 1] = 0;
	
				sx = offs/2 % 64;
				sy = offs/2 / 64;
	
				drawgfx(tmpbitmap,Machine.gfx[1],
						punchout_videoram2.read(offs) + 256 * (punchout_videoram2.read(offs + 1) & 0x03),
						((punchout_videoram2.read(offs + 1) & 0x7c) >> 2) + 64 * bottom_palette_bank,
						punchout_videoram2.read(offs + 1) & 0x80,0,
						8*sx,8*sy + 8*TOP_MONITOR_ROWS,
						backgroundvisiblearea,TRANSPARENCY_NONE,0);
			}
		}
	
		for (offs = punchout_bigsprite1ram_size[0] - 4;offs >= 0;offs -= 4)
		{
			if (bs1dirtybuffer[offs]!=0 | bs1dirtybuffer[offs + 1]!=0 | bs1dirtybuffer[offs + 3]!=0)
			{
				int sx,sy;
	
	
				bs1dirtybuffer[offs] = 0;
				bs1dirtybuffer[offs + 1] = 0;
				bs1dirtybuffer[offs + 3] = 0;
	
				sx = offs/4 % 16;
				sy = offs/4 / 16;
	
				drawgfx(bs1tmpbitmap,Machine.gfx[2],
						punchout_bigsprite1ram.read(offs) + 256 * (punchout_bigsprite1ram.read(offs + 1) & 0x1f),
						(punchout_bigsprite1ram.read(offs + 3) & 0x1f) + 32 * bottom_palette_bank,
						punchout_bigsprite1ram.read(offs + 3) & 0x80,0,
						8*sx,8*sy,
						null,TRANSPARENCY_NONE,0);
			}
		}
	
		for (offs = punchout_bigsprite2ram_size[0] - 4;offs >= 0;offs -= 4)
		{
			if (bs2dirtybuffer[offs]!=0 | bs2dirtybuffer[offs + 1]!=0 | bs2dirtybuffer[offs + 3]!=0)
			{
				int sx,sy;
	
	
				bs2dirtybuffer[offs] = 0;
				bs2dirtybuffer[offs + 1] = 0;
				bs2dirtybuffer[offs + 3] = 0;
	
				sx = offs/4 % 16;
				sy = offs/4 / 16;
	
				drawgfx(bs2tmpbitmap,Machine.gfx[3],
						punchout_bigsprite2ram.read(offs) + 256 * (punchout_bigsprite2ram.read(offs + 1) & 0x0f),
						(punchout_bigsprite2ram.read(offs + 3) & 0x3f) + 64 * bottom_palette_bank,
						punchout_bigsprite2ram.read(offs + 3) & 0x80,0,
						8*sx,8*sy,
						null,TRANSPARENCY_NONE,0);
			}
		}
	
	
		/* copy the character mapped graphics */
		{
			int[] scroll=new int[64];
	
	
			for (offs = 0;offs < TOP_MONITOR_ROWS;offs++)
				scroll[offs] = 0;
			for (offs = 0;offs < BOTTOM_MONITOR_ROWS;offs++)
				scroll[TOP_MONITOR_ROWS + offs] = -(58 + punchout_scroll.read(2*offs) + 256 * (punchout_scroll.read(2*offs + 1) & 0x01));
	
			copyscrollbitmap(bitmap,tmpbitmap,TOP_MONITOR_ROWS + BOTTOM_MONITOR_ROWS,scroll,0,null,Machine.drv.visible_area,TRANSPARENCY_NONE,0);
		}
	
		/* copy the two big sprites */
		{
			int sx,sy,zoom,height;
	
	
			zoom = punchout_bigsprite1.read(0) + 256 * (punchout_bigsprite1.read(1) & 0x0f);
			if (zoom != 0)
			{
				sx = 1024 - (punchout_bigsprite1.read(2) + 256 * (punchout_bigsprite1.read(3) & 0x0f)) / 4;
				if (sx > 1024-127) sx -= 1024;
				sx = sx * (0x1000 / 4) / zoom;	/* adjust x position basing on zoom */
				sx -= 57;	/* adjustment to match the screen shots */
	
				sy = -punchout_bigsprite1.read(4) + 256 * (punchout_bigsprite1.read(5) & 1);
				sy = sy * (0x1000 / 4) / zoom;	/* adjust y position basing on zoom */
	
				/* when the sprite is reduced, it fits more than */
				/* once in the screen, so if the first draw is */
				/* offscreen the second can be visible */
				height = 256 * (0x1000 / 4) / zoom;	/* height of the zoomed sprite */
				if (sy <= -height+16) sy += 2*height;	/* if offscreen, try moving it lower */
	
				sy += 3;	/* adjustment to match the screen shots */
					/* have to be at least 3, using 2 creates a blank line at the bottom */
					/* of the screen when you win the championship and jump around with */
					/* the belt */
	
				if ((punchout_bigsprite1.read(7) & 1)!=0)	/* display in top monitor */
				{
					copybitmapzoom(bitmap,bs1tmpbitmap,
							punchout_bigsprite1.read(6) & 1,0,
							sx,sy - 8*(32-TOP_MONITOR_ROWS),
							topvisiblearea,TRANSPARENCY_COLOR,1024,
							0x10000 * 0x1000 / 4 / zoom,0x10000 * 0x1000 / 4 / zoom);
                                    
				}
				if ((punchout_bigsprite1.read(7) & 2)!=0)	/* display in bottom monitor */
				{
					copybitmapzoom(bitmap,bs1tmpbitmap,
							punchout_bigsprite1.read(6) & 1,0,
							sx,sy + 8*TOP_MONITOR_ROWS,
							bottomvisiblearea,TRANSPARENCY_COLOR,1024,
							0x10000 * 0x1000 / 4 / zoom,0x10000 * 0x1000 / 4 / zoom);
                                    
				}
			}
		}
		{
			int sx,sy;
	
	
			sx = 512 - (punchout_bigsprite2.read(0) + 256 * (punchout_bigsprite2.read(1) & 1));
			if (sx > 512-127) sx -= 512;
			sx -= 55;	/* adjustment to match the screen shots */
	
			sy = -punchout_bigsprite2.read(2) + 256 * (punchout_bigsprite2.read(3) & 1);
			sy += 3;	/* adjustment to match the screen shots */
	
			copybitmap(bitmap,bs2tmpbitmap,
					punchout_bigsprite2.read(4) & 1,0,
					sx,sy + 8*TOP_MONITOR_ROWS,
					bottomvisiblearea,TRANSPARENCY_COLOR,1024);
		}
	} };
	
	
	public static VhUpdatePtr armwrest_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
	
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = punchout_videoram2_size[0] - 2;offs >= 0;offs -= 2)
		{
			if (dirtybuffer2[offs]!=0 | dirtybuffer2[offs + 1]!=0)
			{
				int sx,sy;
	
	
				dirtybuffer2[offs] = 0;
				dirtybuffer2[offs + 1] = 0;
	
				sx = offs/2 % 32;
				sy = offs/2 / 32;
	
				if (sy >= 32)
				{
					/* top screen */
					sy -= 32;
					drawgfx(tmpbitmap,Machine.gfx[0],
							punchout_videoram2.read(offs) + 256 * (punchout_videoram2.read(offs + 1) & 0x03) +
									8 * (punchout_videoram2.read(offs + 1) & 0x80),
							((punchout_videoram2.read(offs + 1) & 0x7c) >> 2) + 64 * top_palette_bank,
							0,0,
							8*sx,8*sy - 8*(32-TOP_MONITOR_ROWS),
							topvisiblearea,TRANSPARENCY_NONE,0);
				}
				else
					/* bottom screen background */
					drawgfx(tmpbitmap,Machine.gfx[0],
							punchout_videoram2.read(offs) + 256 * (punchout_videoram2.read(offs + 1) & 0x03),
							128 + ((punchout_videoram2.read(offs + 1) & 0x7c) >> 2) + 64 * bottom_palette_bank,
							punchout_videoram2.read(offs + 1) & 0x80,0,
							8*sx,8*sy + 8*TOP_MONITOR_ROWS,
							backgroundvisiblearea,TRANSPARENCY_NONE,0);
			}
		}
	
		for (offs = punchout_bigsprite1ram_size[0] - 4;offs >= 0;offs -= 4)
		{
			if (bs1dirtybuffer[offs]!=0 | bs1dirtybuffer[offs + 1]!=0 | bs1dirtybuffer[offs + 3]!=0)
			{
				int sx,sy;
	
	
				bs1dirtybuffer[offs] = 0;
				bs1dirtybuffer[offs + 1] = 0;
				bs1dirtybuffer[offs + 3] = 0;
	
				sx = offs/4 % 16;
				sy = offs/4 / 16;
				if (sy >= 16)
				{
					sy -= 16;
					sx += 16;
				}
	
				drawgfx(bs1tmpbitmap,Machine.gfx[2],
						punchout_bigsprite1ram.read(offs) + 256 * (punchout_bigsprite1ram.read(offs + 1) & 0x1f),
						(punchout_bigsprite1ram.read(offs + 3) & 0x1f) + 32 * bottom_palette_bank,
						punchout_bigsprite1ram.read(offs + 3) & 0x80,0,
						8*sx,8*sy,
						null,TRANSPARENCY_NONE,0);
			}
		}
	
		for (offs = punchout_bigsprite2ram_size[0] - 4;offs >= 0;offs -= 4)
		{
			if (bs2dirtybuffer[offs]!=0 | bs2dirtybuffer[offs + 1]!=0 | bs2dirtybuffer[offs + 3]!=0)
			{
				int sx,sy;
	
	
				bs2dirtybuffer[offs] = 0;
				bs2dirtybuffer[offs + 1] = 0;
				bs2dirtybuffer[offs + 3] = 0;
	
				sx = offs/4 % 16;
				sy = offs/4 / 16;
	
				drawgfx(bs2tmpbitmap,Machine.gfx[3],
						punchout_bigsprite2ram.read(offs) + 256 * (punchout_bigsprite2ram.read(offs + 1) & 0x0f),
						(punchout_bigsprite2ram.read(offs + 3) & 0x3f) + 64 * bottom_palette_bank,
						punchout_bigsprite2ram.read(offs + 3) & 0x80,0,
						8*sx,8*sy,
						null,TRANSPARENCY_NONE,0);
			}
		}
	
	
		/* copy the character mapped graphics */
		copybitmap(bitmap,tmpbitmap,0,0,0,0,Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	
	
		/* copy the two big sprites */
		{
			int sx,sy,zoom,height;
	
	
			zoom = punchout_bigsprite1.read(0) + 256 * (punchout_bigsprite1.read(1) & 0x0f);
			if (zoom != 0)
			{
				sx = 1024 - (punchout_bigsprite1.read(2) + 256 * (punchout_bigsprite1.read(3) & 0x0f)) / 4;
				if (sx > 1024-127) sx -= 1024;
				sx = sx * (0x1000 / 4) / zoom;	/* adjust x position basing on zoom */
				sx -= 57;	/* adjustment to match the screen shots */
	
				sy = -punchout_bigsprite1.read(4) + 256 * (punchout_bigsprite1.read(5) & 1);
				sy = sy * (0x1000 / 4) / zoom;	/* adjust y position basing on zoom */
	
				/* when the sprite is reduced, it fits more than */
				/* once in the screen, so if the first draw is */
				/* offscreen the second can be visible */
				height = 256 * (0x1000 / 4) / zoom;	/* height of the zoomed sprite */
				if (sy <= -height+16) sy += 2*height;	/* if offscreen, try moving it lower */
	
				sy += 3;	/* adjustment to match the screen shots */
					/* have to be at least 3, using 2 creates a blank line at the bottom */
					/* of the screen when you win the championship and jump around with */
					/* the belt */
	
				if ((punchout_bigsprite1.read(7) & 1)!=0)	/* display in top monitor */
				{
					copybitmapzoom(bitmap,bs1tmpbitmap,
							punchout_bigsprite1.read(6) & 1,0,
							sx,sy - 8*(32-TOP_MONITOR_ROWS),
							topvisiblearea,TRANSPARENCY_COLOR,1024,
							0x10000 * 0x1000 / 4 / zoom,0x10000 * 0x1000 / 4 / zoom);
                                    
				}
				if ((punchout_bigsprite1.read(7) & 2)!=0)	/* display in bottom monitor */
				{
					copybitmapzoom(bitmap,bs1tmpbitmap,
							punchout_bigsprite1.read(6) & 1,0,
							sx,sy + 8*TOP_MONITOR_ROWS,
							bottomvisiblearea,TRANSPARENCY_COLOR,1024,
							0x10000 * 0x1000 / 4 / zoom,0x10000 * 0x1000 / 4 / zoom);
                                    
				}
			}
		}
		{
			int sx,sy;
	
	
			sx = 512 - (punchout_bigsprite2.read(0) + 256 * (punchout_bigsprite2.read(1) & 1));
			if (sx > 512-127) sx -= 512;
			sx -= 55;	/* adjustment to match the screen shots */
	
			sy = -punchout_bigsprite2.read(2) + 256 * (punchout_bigsprite2.read(3) & 1);
			sy += 3;	/* adjustment to match the screen shots */
	
			copybitmap(bitmap,bs2tmpbitmap,
					punchout_bigsprite2.read(4) & 1,0,
					sx,sy + 8*TOP_MONITOR_ROWS,
					bottomvisiblearea,TRANSPARENCY_COLOR,1024);
		}
	
	
		/* draw the foregound chars */
		for (offs = videoram_size[0] - 2;offs >= 0;offs -= 2)
		{
			int sx,sy;
	
	
			dirtybuffer[offs] = 0;
			dirtybuffer[offs + 1] = 0;
	
			sx = offs/2 % 32;
			sy = offs/2 / 32;
	
			drawgfx(bitmap,Machine.gfx[1],
					videoram.read(offs) + 256 * (videoram.read(offs + 1) & 0x07),
					((videoram.read(offs + 1) & 0xf8) >> 3) + 32 * bottom_palette_bank,
					videoram.read(offs + 1) & 0x80,0,
					8*sx,8*sy + 8*TOP_MONITOR_ROWS,
					backgroundvisiblearea,TRANSPARENCY_PEN,7);
		}
	} };
}
