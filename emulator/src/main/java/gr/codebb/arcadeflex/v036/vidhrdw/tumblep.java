/***************************************************************************

   Tumblepop Video emulation - Bryan McPhail, mish@tendril.co.uk

*********************************************************************

Uses Data East custom chip 55 for backgrounds, custom chip 52 for sprites.

See Dark Seal & Caveman Ninja drivers for info on these chips.

Tumblepop is one of few games to take advantage of the playfields ability
to switch between 8*8 tiles and 16*16 tiles.

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package gr.codebb.arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;
import static gr.codebb.arcadeflex.v036.platform.video.*;


public class tumblep
{
	
	public static final int TILERAM_SIZE	=0x1000;
	
	public static UBytePtr tumblep_pf2_data=new UBytePtr();
        public static UBytePtr tumblep_pf3_data=new UBytePtr();
	static char[] tumblep_pf3_dirty;
        static char[] tumblep_pf2_dirty;
	static osd_bitmap tumblep_pf1_bitmap;
	static osd_bitmap tumblep_pf2_bitmap;
	static osd_bitmap tumblep_pf3_bitmap;
	
	static UBytePtr tumblep_control_0=new UBytePtr(16);
	static int[] offsetx=new int[4];
        static int[] offsety=new int[4];
	static int pf_bank;
	
	/******************************************************************************/
	
	static void tumblep_update_palette()
	{
		int offs,color,code,i,pal_base;
		int[] colmask=new int[16];
	    int[] pen_usage;
	
		palette_init_used_colors();
	
		pen_usage=Machine.gfx[1].pen_usage;
		pal_base = Machine.drv.gfxdecodeinfo[1].color_codes_start;
		for (color = 0;color < 16;color++) colmask[color] = 0;
		for (offs = 0; offs < TILERAM_SIZE;offs += 2)
		{
			code = tumblep_pf2_data.READ_WORD(offs);
			color = (code & 0xf000) >> 12;
			code &= 0x0fff;
			colmask[color] |= pen_usage[code];
		}
	
		for (color = 0;color < 16;color++)
		{
			if ((colmask[color] & (1 << 0))!=0)
				palette_used_colors.write(pal_base + 16 * color, PALETTE_COLOR_TRANSPARENT);
			for (i = 1;i < 16;i++)
			{
				if ((colmask[color] & (1 << i))!=0)
					palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
			}
		}
	
		pen_usage=Machine.gfx[0].pen_usage;
		pal_base = Machine.drv.gfxdecodeinfo[2].color_codes_start;
		for (color = 0;color < 16;color++) colmask[color] = 0;
		for (offs = 0; offs < TILERAM_SIZE;offs += 2)
		{
			code = tumblep_pf3_data.READ_WORD(offs);
			color = (code & 0xf000) >> 12;
			code &= 0x0fff;
			colmask[color] |= pen_usage[code];
		}
	
		for (color = 0;color < 16;color++)
		{
			if ((colmask[color] & (1 << 0))!=0)
				palette_used_colors.write(pal_base + 16 * color, PALETTE_COLOR_TRANSPARENT);
			for (i = 1;i < 16;i++)
			{
				if ((colmask[color] & (1 << i))!=0)
					palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
			}
		}
	
		pen_usage=Machine.gfx[2].pen_usage;
		pal_base = Machine.drv.gfxdecodeinfo[2].color_codes_start;
		for (color = 0;color < 16;color++) colmask[color] = 0;
		for (offs = 0; offs < TILERAM_SIZE;offs += 2)
		{
			code = tumblep_pf3_data.READ_WORD(offs);
			color = (code & 0xf000) >> 12;
			code &= 0x0fff;
			colmask[color] |= pen_usage[code];
		}
	
		for (color = 0;color < 16;color++)
		{
			if ((colmask[color] & (1 << 0))!=0)
				palette_used_colors.write(pal_base + 16 * color, PALETTE_COLOR_TRANSPARENT);
			for (i = 1;i < 16;i++)
			{
				if ((colmask[color] & (1 << i))!=0)
					palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
			}
		}
	
		/* Sprites */
		pen_usage=Machine.gfx[3].pen_usage;
		pal_base = Machine.drv.gfxdecodeinfo[3].color_codes_start;
		for (color = 0;color < 16;color++) colmask[color] = 0;
	
		for (offs = 0;offs < 0x800;offs += 8)
		{
			int x,y,sprite,multi;
	
			sprite = spriteram.READ_WORD(offs+2) & 0x3fff;
			if (sprite==0) continue;
	
			y = spriteram.READ_WORD(offs);
			x = spriteram.READ_WORD(offs+4);
			color = (x >>9) &0xf;
	
			multi = (1 << ((y & 0x0600) >> 9)) - 1;	/* 1x, 2x, 4x, 8x height */
	
			sprite &= ~multi;
	
			while (multi >= 0)
			{
				colmask[color] |= pen_usage[sprite + multi];
				multi--;
			}
		}
	
		for (color = 0;color < 16;color++)
		{
			for (i = 1;i < 16;i++)
			{
				if ((colmask[color] & (1 << i))!=0)
					palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
			}
		}
	
		if (palette_recalc()!=null)
		{
			memset(tumblep_pf2_dirty,1,TILERAM_SIZE);
			memset(tumblep_pf3_dirty,1,TILERAM_SIZE);
		}
	}
	
	static void tumblep_drawsprites(osd_bitmap bitmap)
	{
		int offs;
	
		for (offs = 0;offs < 0x800;offs += 8)
		{
			int x,y,sprite,colour,multi,fx,fy,inc,flash;
	
			sprite = spriteram.READ_WORD(offs+2) & 0x3fff;
			if (sprite==0) continue;
	
			y = spriteram.READ_WORD(offs);
			flash=y&0x1000;
			if (flash!=0 && (cpu_getcurrentframe() & 1)!=0) continue;
	
			x = spriteram.READ_WORD(offs+4);
			colour = (x >>9) & 0xf;
	
			fx = y & 0x2000;
			fy = y & 0x4000;
			multi = (1 << ((y & 0x0600) >> 9)) - 1;	/* 1x, 2x, 4x, 8x height */
	
			x = x & 0x01ff;
			y = y & 0x01ff;
			if (x >= 320) x -= 512;
			if (y >= 256) y -= 512;
			y = 240 - y;
	        x = 304 - x;
	
			sprite &= ~multi;
			if (fy != 0)
				inc = -1;
			else
			{
				sprite += multi;
				inc = 1;
			}
	
			while (multi >= 0)
			{
				drawgfx(bitmap,Machine.gfx[3],
						sprite - multi * inc,
						colour,
						fx,fy,
						x,y - 16 * multi,
						Machine.drv.visible_area,TRANSPARENCY_PEN,0);
	
				multi--;
			}
		}
	}
	
	static void tumblep_pf2_update()
	{
		int offs,mx,my,color,tile,quarter;
	
		for (quarter = 0;quarter < 4;quarter++)
		{
			mx = -1;
			my = 0;
	
	  		for (offs = 0x400 * quarter; offs < 0x400 * quarter + 0x400;offs += 2)
			{
				mx++;
				if (mx == 32)
				{
					mx = 0;
					my++;
				}
	
				if (tumblep_pf2_dirty[offs]!=0)
				{
					tumblep_pf2_dirty[offs] = 0;
					tile = tumblep_pf2_data.READ_WORD(offs);
					color = (tile & 0xf000) >> 12;
	
					drawgfx(tumblep_pf2_bitmap,Machine.gfx[1],
							tile & 0x0fff,
							color,
							0,0,
							16*mx + offsetx[quarter],16*my + offsety[quarter],
							null,TRANSPARENCY_NONE,0);
				}
			}
		}
	}
	
	static void tumblep_pf3_update()
	{
		int offs,mx,my,color,tile,quarter;
	
		for (quarter = 0;quarter < 4;quarter++)
		{
			mx = -1;
			my = 0;
	
	   		for (offs = 0x400 * quarter; offs < 0x400 * quarter + 0x400;offs += 2)
			{
				mx++;
				if (mx == 32)
				{
					mx = 0;
					my++;
				}
	
				if (tumblep_pf3_dirty[offs]!=0)
				{
					tumblep_pf3_dirty[offs] = 0;
					tile = tumblep_pf3_data.READ_WORD(offs);
					color = (tile & 0xf000) >> 12;
	
					drawgfx(tumblep_pf3_bitmap,Machine.gfx[2],
							tile & 0x0fff,
							color,
							0,0,
							16*mx + offsetx[quarter],16*my + offsety[quarter],
							null,TRANSPARENCY_NONE,0);
				}
			}
		}
	}
	
	/******************************************************************************/
	
	public static VhUpdatePtr tumblep_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int scrollx,scrolly;
		int mx,my,offs,tile,color;
	
		tumblep_update_palette();
	
		tumblep_pf2_update();
		if (pf_bank==0)
			tumblep_pf3_update();
	
		/* Background */
		scrollx=-tumblep_control_0.READ_WORD(6);
		scrolly=-tumblep_control_0.READ_WORD(8);
	 	copyscrollbitmap(bitmap,tumblep_pf2_bitmap,1,new int[]{scrollx},1,new int[]{scrolly},Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	
		/* Foreground */
		scrollx=-tumblep_control_0.READ_WORD(2);
		scrolly=-tumblep_control_0.READ_WORD(4);
	
		/* Draw 16*16 background */
		if (pf_bank==0)
			copyscrollbitmap(bitmap,tumblep_pf3_bitmap,1,new int[]{scrollx},1,new int[]{scrolly},Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
	
		/* Sprites */
		tumblep_drawsprites(bitmap);
	
		/* Draw 8*8 background */
		if (pf_bank != 0) {
			mx = -1;
			my = 0;
			for (offs = 0; offs < 0x1000 ;offs += 2)
			{
				mx++;
				if (mx == 64)
				{
					mx = 0;
					my++;
				}
	
				if (tumblep_pf3_dirty[offs]!=0)
				{
					tumblep_pf3_dirty[offs] = 0;
					tile = tumblep_pf3_data.READ_WORD(offs);
					color = (tile & 0xf000) >> 12;
	
					drawgfx(tumblep_pf1_bitmap,Machine.gfx[0],
							tile & 0x0fff,
							color,
							0,0,
							8*mx,8*my,
							null,TRANSPARENCY_NONE,0);
				}
			}
	
			scrollx=-tumblep_control_0.READ_WORD(2);
			scrolly=-tumblep_control_0.READ_WORD(4);
			copyscrollbitmap(bitmap,tumblep_pf1_bitmap,1,new int[]{scrollx},1,new int[]{scrolly},Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
		}
	} };
	
	public static VhUpdatePtr tumblepb_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int scrollx,scrolly;
		int mx,my,offs,tile,color;
	
		tumblep_update_palette();
	
		tumblep_pf2_update();
		if (pf_bank==0)
			tumblep_pf3_update();
	
		/* Background */
		scrollx=-tumblep_control_0.READ_WORD(6)+1;
		scrolly=-tumblep_control_0.READ_WORD(8);
	 	copyscrollbitmap(bitmap,tumblep_pf2_bitmap,1,new int[]{scrollx},1,new int[]{scrolly},Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	
		/* Foreground */
		scrollx=-tumblep_control_0.READ_WORD(2)+5;
		scrolly=-tumblep_control_0.READ_WORD(4);
	
		/* Draw 16*16 background */
		if (pf_bank==0)
			copyscrollbitmap(bitmap,tumblep_pf3_bitmap,1,new int[]{scrollx},1,new int[]{scrolly},Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
	
		/* Sprites */
		tumblep_drawsprites(bitmap);
	
		/* Draw 8*8 background */
		if (pf_bank != 0) {
			mx = -1;
			my = 0;
			for (offs = 0; offs < 0x1000 ;offs += 2)
			{
				mx++;
				if (mx == 64)
				{
					mx = 0;
					my++;
				}
	
				if (tumblep_pf3_dirty[offs]!=0)
				{
					tumblep_pf3_dirty[offs] = 0;
					tile = tumblep_pf3_data.READ_WORD(offs);
					color = (tile & 0xf000) >> 12;
	
					drawgfx(tumblep_pf1_bitmap,Machine.gfx[0],
							tile & 0x0fff,
							color,
							0,0,
							8*mx,8*my,
							null,TRANSPARENCY_NONE,0);
				}
			}
	
			scrollx=-tumblep_control_0.READ_WORD(2)+5;
			scrolly=-tumblep_control_0.READ_WORD(4);
			copyscrollbitmap(bitmap,tumblep_pf1_bitmap,1,new int[]{scrollx},1,new int[]{scrolly},Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
		}
	} };
	
	/******************************************************************************/
	
	public static WriteHandlerPtr tumblep_pf2_data_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int oldword = tumblep_pf2_data.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword,data);
	
		if (oldword != newword)
		{
			tumblep_pf2_data.WRITE_WORD(offset,newword);
			tumblep_pf2_dirty[offset] = 1;
		}
	} };
	
	public static WriteHandlerPtr tumblep_pf3_data_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int oldword = tumblep_pf3_data.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword,data);
	
		if (oldword != newword)
		{
			tumblep_pf3_data.WRITE_WORD(offset,newword);
			tumblep_pf3_dirty[offset] = 1;
		}
	} };
	static int last=0;
	public static WriteHandlerPtr tumblep_control_0_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		
	
		COMBINE_WORD_MEM(tumblep_control_0,offset,data);
	
	    /* Check for playfield 'bankswitch' */
		if (offset==0xc) {
			if ((data&0xff)==0x80) {
				if (last==0) {
					last=1;
					memset(tumblep_pf3_dirty,1,TILERAM_SIZE);
	            }
				pf_bank=1;
	        }
			else {
				if (last != 0) {
					last=0;
					memset(tumblep_pf3_dirty,1,TILERAM_SIZE);
	            }
	        	pf_bank=0;
	        }
		}
	} };
	
	/******************************************************************************/
	
	public static VhStopPtr tumblep_vh_stop = new VhStopPtr() { public void handler() 
	{
		osd_free_bitmap(tumblep_pf2_bitmap);
		osd_free_bitmap(tumblep_pf3_bitmap);
		osd_free_bitmap(tumblep_pf1_bitmap);
		tumblep_pf3_dirty=null;
		tumblep_pf2_dirty=null;
	} };
	
	public static VhStartPtr tumblep_vh_start = new VhStartPtr() { public int handler() 
	{
		/* Allocate bitmaps */
		if ((tumblep_pf1_bitmap = osd_create_bitmap(512,256)) == null) {
			tumblep_vh_stop.handler();
			return 1;
		}
	
		if ((tumblep_pf2_bitmap = osd_create_bitmap(1024,512)) == null) {
			tumblep_vh_stop.handler();
			return 1;
		}
	
		if ((tumblep_pf3_bitmap = osd_create_bitmap(1024,512)) == null) {
			tumblep_vh_stop.handler();
			return 1;
		}
	
		tumblep_pf3_dirty = new char[TILERAM_SIZE];
		tumblep_pf2_dirty = new char[TILERAM_SIZE];
		memset(tumblep_pf2_dirty,1,TILERAM_SIZE);
		memset(tumblep_pf3_dirty,1,TILERAM_SIZE);
	
		offsetx[0] = 0;
		offsetx[1] = 0;
		offsetx[2] = 512;
		offsetx[3] = 512;
		offsety[0] = 0;
		offsety[1] = 256;
		offsety[2] = 0;
		offsety[3] = 256;
	
		return 0;
	} };
}
