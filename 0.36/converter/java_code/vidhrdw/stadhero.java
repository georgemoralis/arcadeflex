/***************************************************************************

  stadhero video emulation - Bryan McPhail, mish@tendril.co.uk

*********************************************************************

	MXC-06 chip to produce sprites, see dec0.c
	BAC-06 chip for background?

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

public class stadhero
{
	
	unsigned char *stadhero_pf1_data,*stadhero_pf2_data;
	static unsigned char *stadhero_pf2_dirty;
	static struct osd_bitmap *stadhero_pf2_bitmap;
	
	static unsigned char stadhero_pf2_control_0[8];
	static unsigned char stadhero_pf2_control_1[8];
	
	/******************************************************************************/
	
	static void stadhero_drawsprites(struct osd_bitmap *bitmap,int pri_mask,int pri_val)
	{
		int offs;
	
		for (offs = 0;offs < 0x800;offs += 8)
		{
			int x,y,sprite,colour,multi,fx,fy,inc,flash;
	
			y = READ_WORD(&spriteram[offs]);
			if ((y&0x8000) == 0) continue;
	
			x = READ_WORD(&spriteram[offs+4]);
			colour = x >> 12;
			if ((colour & pri_mask) != pri_val) continue;
	
			flash=x&0x800;
			if (flash && (cpu_getcurrentframe() & 1)) continue;
	
			fx = y & 0x2000;
			fy = y & 0x4000;
			multi = (1 << ((y & 0x1800) >> 11)) - 1;	/* 1x, 2x, 4x, 8x height */
												/* multi = 0   1   3   7 */
	
			sprite = READ_WORD (&spriteram[offs+2]) & 0x0fff;
	
			x = x & 0x01ff;
			y = y & 0x01ff;
			if (x >= 256) x -= 512;
			if (y >= 256) y -= 512;
			x = 240 - x;
			y = 240 - y;
	
			if (x>256) continue; /* Speedup */
	
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
				drawgfx(bitmap,Machine.gfx[2],
						sprite - multi * inc,
						colour,
						fx,fy,
						x,y - 16 * multi,
						&Machine.drv.visible_area,TRANSPARENCY_PEN,0);
	
				multi--;
			}
		}
	}
	
	/******************************************************************************/
	
	static void stadhero_pf2_update(void)
	{
		int offs,mx,my,color,tile,quarter;
		int offsetx[16],offsety[16];
	
		offsetx[0]=0; offsety[0]=0;
		offsetx[1]=0; offsety[1]=256;
		offsetx[2]=0; offsety[2]=512;
		offsetx[3]=0; offsety[3]=768;
	
		offsetx[4]=256; offsety[4]=0;
		offsetx[5]=256; offsety[5]=256;
		offsetx[6]=256; offsety[6]=512;
		offsetx[7]=256; offsety[7]=768;
	
		offsetx[8]=512; offsety[8]=0;
		offsetx[9]=512; offsety[9]=256;
		offsetx[10]=512; offsety[10]=512;
		offsetx[11]=512; offsety[11]=768;
	
		offsetx[12]=768; offsety[12]=0;
		offsetx[13]=768; offsety[13]=256;
		offsetx[14]=768; offsety[14]=512;
		offsetx[15]=768; offsety[15]=768;
	
		for (quarter = 0;quarter < 16;quarter++)
		{
			mx = -1;
			my = 0;
	
			for (offs = 0x200 * quarter; offs < 0x200 * quarter + 0x200;offs += 2)
			{
				mx++;
				if (mx == 16)
				{
					mx = 0;
					my++;
				}
	
				if (stadhero_pf2_dirty[offs])
				{
					stadhero_pf2_dirty[offs] = 0;
					tile = READ_WORD(&stadhero_pf2_data[offs]);
					color = (tile & 0xf000) >> 12;
	
					drawgfx(stadhero_pf2_bitmap,Machine.gfx[1],
							tile & 0x0fff,
							color,
							0,0,
							16*mx + offsetx[quarter],16*my + offsety[quarter],
							0,TRANSPARENCY_NONE,0);
				}
			}
		}
	}
	
	/******************************************************************************/
	
	void stadhero_pf2_draw(struct osd_bitmap *bitmap)
	{
		int scrolly,scrollx;
	
		scrollx = - READ_WORD(&stadhero_pf2_control_1[0]);
		scrolly = - READ_WORD(&stadhero_pf2_control_1[2]);
	
		copyscrollbitmap(bitmap,stadhero_pf2_bitmap,1,&scrollx,1,&scrolly,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	}
	
	/******************************************************************************/
	
	public static VhUpdatePtr stadhero_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
		int color,code,i;
		int colmask[16];
		int pal_base;
		int mx,my,tile;
	
		palette_init_used_colors();
	
		pal_base = Machine.drv.gfxdecodeinfo[0].color_codes_start;
		for (color = 0;color < 16;color++) colmask[color] = 0;
		for (offs = 0; offs < 0x800;offs += 2)
		{
			code = READ_WORD(&stadhero_pf1_data[offs]);
			color = (code & 0xf000) >> 12;
			code &= 0x0fff;
			colmask[color] |= Machine.gfx[0].pen_usage[code];
		}
	
		for (color = 0;color < 16;color++)
		{
			if (colmask[color] & (1 << 0))
				palette_used_colors[pal_base + 8 * color] = PALETTE_COLOR_TRANSPARENT;
			for (i = 1;i < 8;i++)
			{
				if (colmask[color] & (1 << i))
					palette_used_colors[pal_base + 8 * color + i] = PALETTE_COLOR_USED;
			}
		}
	
		pal_base = Machine.drv.gfxdecodeinfo[1].color_codes_start;
		for (color = 0;color < 16;color++) colmask[color] = 0;
		for (offs = 0; offs < 0x2000;offs += 2)
		{
			code = READ_WORD(&stadhero_pf2_data[offs]);
			color = (code & 0xf000) >> 12;
			code &= 0x0fff;
			colmask[color] |= Machine.gfx[1].pen_usage[code];
		}
	
		for (color = 0;color < 16;color++)
		{
			for (i = 0;i < 8;i++)
			{
				if (colmask[color] & (1 << i))
					palette_used_colors[pal_base + 8 * color + i] = PALETTE_COLOR_USED;
			}
		}
	
		pal_base = Machine.drv.gfxdecodeinfo[2].color_codes_start;
		for (color = 0;color < 16;color++) colmask[color] = 0;
		for (offs = 0;offs < 0x800;offs += 8)
		{
			int x,y,sprite,multi;
	
			y = READ_WORD(&spriteram[offs]);
			if ((y&0x8000) == 0) continue;
	
			x = READ_WORD(&spriteram[offs+4]);
			color = (x & 0xf000) >> 12;
	
			multi = (1 << ((y & 0x1800) >> 11)) - 1;	/* 1x, 2x, 4x, 8x height */
												/* multi = 0   1   3   7 */
	
			sprite = READ_WORD (&spriteram[offs+2]) & 0x0fff;
			sprite &= ~multi;
	
			while (multi >= 0)
			{
				colmask[color] |= Machine.gfx[2].pen_usage[sprite + multi];
				multi--;
			}
		}
	
		for (color = 0;color < 16;color++)
		{
			for (i = 1;i < 16;i++)
			{
				if (colmask[color] & (1 << i))
					palette_used_colors[pal_base + 16 * color + i] = PALETTE_COLOR_USED;
			}
		}
	
		if (palette_recalc())
		{
			memset(stadhero_pf2_dirty,1,0x2000);
		}
	
		stadhero_pf2_update();
		stadhero_pf2_draw(bitmap);
	
		stadhero_drawsprites(bitmap,0x00,0x00);
	
		for (offs = 0x800 - 2;offs >= 0;offs -= 2)
		{
			tile=READ_WORD(&stadhero_pf1_data[offs]);
	
			if (!tile) continue;
	
			color=tile>>12;
			mx = (offs/2) % 32;
			my = (offs/2) / 32;
	
			drawgfx(bitmap,Machine.gfx[0],
					tile&0xfff,color,0,0,8*mx,8*my,
					&Machine.drv.visible_area,TRANSPARENCY_PEN,0);
		}
	} };
	
	/******************************************************************************/
	
	public static WriteHandlerPtr stadhero_pf1_data_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		COMBINE_WORD_MEM(&stadhero_pf1_data[offset],data);
	} };
	
	public static ReadHandlerPtr stadhero_pf1_data_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return READ_WORD(&stadhero_pf1_data[offset]);
	} };
	
	public static WriteHandlerPtr stadhero_pf2_control_0_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		COMBINE_WORD_MEM(&stadhero_pf2_control_0[offset],data);
	} };
	
	public static WriteHandlerPtr stadhero_pf2_control_1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		COMBINE_WORD_MEM(&stadhero_pf2_control_1[offset],data);
	} };
	
	public static WriteHandlerPtr stadhero_pf2_data_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int oldword = READ_WORD(&stadhero_pf2_data[offset]);
		int newword = COMBINE_WORD(oldword,data);
	
		if (oldword != newword)
		{
			WRITE_WORD(&stadhero_pf2_data[offset],newword);
			stadhero_pf2_dirty[offset] = 1;
		}
	} };
	
	public static ReadHandlerPtr stadhero_pf2_data_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return READ_WORD(&stadhero_pf2_data[offset]);
	} };
	
	/******************************************************************************/
	
	public static VhStopPtr stadhero_vh_stop = new VhStopPtr() { public void handler() 
	{
		osd_free_bitmap(stadhero_pf2_bitmap);
		free(stadhero_pf2_dirty);
	} };
	
	public static VhStartPtr stadhero_vh_start = new VhStartPtr() { public int handler() 
	{
		if ((stadhero_pf2_bitmap = osd_create_bitmap(1024,1024)) == 0) {
			stadhero_vh_stop ();
			return 1;
		}
	
		stadhero_pf2_dirty = malloc(0x2000);
		memset(stadhero_pf2_dirty,1,0x2000);
		return 0;
	} };
	
	/******************************************************************************/
}
