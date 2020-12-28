/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package gr.codebb.arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.common.libc.cstring.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;

public class taitof2
{
	
	public static UBytePtr taitof2_scrollx=new UBytePtr();
	public static UBytePtr taitof2_scrolly=new UBytePtr();
	public static UBytePtr f2_backgroundram=new UBytePtr();
	public static int[] f2_backgroundram_size=new int[1];
	public static UBytePtr f2_foregroundram=new UBytePtr();
	public static int[] f2_foregroundram_size=new int[1];
	public static UBytePtr f2_textram=new UBytePtr();
	public static int[] f2_textram_size=new int[1];
	public static UBytePtr taitof2_characterram=new UBytePtr();
	static char[] char_dirty;	/* 256 chars */
	public static int[] f2_characterram_size=new int[1];
	static char[] text_dirty;
	static char[] bg_dirty;
	static char[] fg_dirty;
	public static int[]  f2_paletteram_size=new int[1];
	
	static osd_bitmap tmpbitmap2;
	static osd_bitmap tmpbitmap3;
	static int[] spritebank=new int[8];
	
	public static VhStartPtr taitof2_vh_start = new VhStartPtr() { public int handler() 
	{
		char_dirty = new char[f2_characterram_size[0]/16];
		if (char_dirty==null) return 1;
		memset (char_dirty,1,f2_characterram_size[0]/16);
	
		text_dirty = new char[f2_textram_size[0]/2];
		if (text_dirty==null)
		{
			char_dirty=null;
			return 1;
		}
	
		bg_dirty = new char[f2_backgroundram_size[0]/4];
		if (bg_dirty==null)
		{
			char_dirty=null;
			text_dirty=null;
			return 1;
		}
		memset (bg_dirty,1,f2_backgroundram_size[0]/4);
	
		fg_dirty = new char[f2_foregroundram_size[0]/4];
		if (fg_dirty==null)
		{
			char_dirty=null;
			text_dirty=null;
			bg_dirty=null;
			return 1;
		}
		memset (fg_dirty,1,f2_foregroundram_size[0]/4);
	
		/* create a temporary bitmap slightly larger than the screen for the background */
		if ((tmpbitmap = osd_create_bitmap(64*8, 64*8)) == null)
		{
			char_dirty=null;
			text_dirty=null;
			bg_dirty=null;
			fg_dirty=null;
			return 1;
		}
	
		/* create a temporary bitmap slightly larger than the screen for the foreground */
		if ((tmpbitmap2 = osd_create_bitmap(64*8, 64*8)) == null)
		{
			char_dirty=null;
			text_dirty=null;
			bg_dirty=null;
			fg_dirty=null;
			osd_free_bitmap (tmpbitmap);
			return 1;
		}
	
		/* create a temporary bitmap slightly larger than the screen for the text layer */
		if ((tmpbitmap3 = osd_create_bitmap(64*8, 64*8)) == null)
		{
			char_dirty=null;
			text_dirty=null;
			bg_dirty=null;
			fg_dirty=null;
			osd_free_bitmap (tmpbitmap);
			osd_free_bitmap (tmpbitmap2);
			return 1;
		}
	
		{
			int i;
	
			for (i = 0; i < 8; i ++)
				spritebank[i] = 0x400 * i;
		}
	
		{
			int i;
	
			memset (paletteram, 0, f2_paletteram_size[0]); /* probably not needed */
			for (i = 0; i < f2_paletteram_size[0]/2; i++)
				palette_change_color(i,0,0,0);
		}
	
		return 0;
	} };
	
	public static VhStopPtr taitof2_vh_stop = new VhStopPtr() { public void handler() 
	{
		char_dirty=null;
			text_dirty=null;
			bg_dirty=null;
			fg_dirty=null;
		osd_free_bitmap (tmpbitmap);
		osd_free_bitmap (tmpbitmap2);
		osd_free_bitmap (tmpbitmap3);
	} };
	
	/* we have to straighten out the 16-bit word into bytes for gfxdecode() to work */
	public static ReadHandlerPtr taitof2_characterram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		int res;
	
		res = taitof2_characterram.READ_WORD(offset);
	
		//#ifdef LSB_FIRST
		res = ((res & 0x00ff) << 8) | ((res & 0xff00) >> 8);
		//#endif
	
		return res;
	} };
	
	public static WriteHandlerPtr taitof2_characterram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int oldword = taitof2_characterram.READ_WORD(offset);
		int newword;
	
	
		//#ifdef LSB_FIRST
		data = ((data & 0x00ff00ff) << 8) | ((data & 0xff00ff00) >> 8);
		//#endif
	
		newword = COMBINE_WORD (oldword,data);
		if (oldword != newword)
		{
			taitof2_characterram.WRITE_WORD(offset,newword);
			char_dirty[offset / 16] = 1;
		}
	} };
	
	public static ReadHandlerPtr taitof2_text_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return f2_textram.READ_WORD(offset);
	} };
	
	public static WriteHandlerPtr taitof2_text_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int oldword = f2_textram.READ_WORD(offset);
		int newword = COMBINE_WORD (oldword, data);
	
		if (oldword != newword)
		{
			f2_textram.WRITE_WORD(offset,newword);
			text_dirty[offset / 2] = 1;
		}
	} };
	
	public static ReadHandlerPtr taitof2_background_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return f2_backgroundram.READ_WORD(offset);
	} };
	
	public static WriteHandlerPtr taitof2_background_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int oldword = f2_backgroundram.READ_WORD(offset);
		int newword = COMBINE_WORD (oldword, data);
	
		if (oldword != newword)
		{
			f2_backgroundram.WRITE_WORD(offset,newword);
			bg_dirty[offset / 4] = 1;
		}
	} };
	
	public static ReadHandlerPtr taitof2_foreground_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return f2_foregroundram.READ_WORD(offset);
	} };
	
	public static WriteHandlerPtr taitof2_foreground_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int oldword = f2_foregroundram.READ_WORD(offset);
		int newword = COMBINE_WORD (oldword, data);
	
		if (oldword != newword)
		{
			f2_foregroundram.WRITE_WORD(offset,newword);
			fg_dirty[offset / 4] = 1;
		}
	} };
	
	public static WriteHandlerPtr taitof2_spritebank_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (errorlog != null) fprintf (errorlog, "bank %d, new value: %04x\n", offset >> 1, data << 10);
		if ((offset >> 1) < 4) return;
	//	if (data == 0) data = (offset >> 1) * 0x400;
		spritebank[offset >> 1] = data << 10;
	} };
	
	static void taitof2_update_palette ()
	{
		int i;
		int offs,code,color,spritecont;
		char[] palette_map=new char[256];
	
		memset (palette_map, 0, palette_map.length);
	
		/* Background layer */
		for (offs = 0;offs < f2_backgroundram_size[0];offs += 4)
		{
			int tile;
	
			tile = f2_backgroundram.READ_WORD(offs + 2);
			color = (f2_backgroundram.READ_WORD(offs) & 0xff);
	
			palette_map[color] |= Machine.gfx[1].pen_usage[tile];
		}
	
		/* Background layer */
		for (offs = 0;offs < f2_foregroundram_size[0];offs += 4)
		{
			int tile;
	
			tile = f2_foregroundram.READ_WORD(offs + 2);
			color = (f2_foregroundram.READ_WORD(offs) & 0xff);
	
			palette_map[color] |= Machine.gfx[1].pen_usage[tile];
		}
	
		color = 0;
	
		/* Sprites */
		for (offs = 0;offs < 0x3400;offs += 16)
		{
			spritecont = (videoram.READ_WORD(offs+8) & 0xff00) >> 8;
	//		if (!spritecont) continue;
	
			code = videoram.READ_WORD(offs) & 0x1fff;
	
			{
				int bank;
	
				bank = (code & 0x1c00) >> 10;
				code = spritebank[bank] + (code & 0x3ff);
			}
	
			if ((spritecont & 0xf4) == 0)
			{
				color = videoram.READ_WORD(offs+8) & 0x00ff;
			}
			if (code==0) continue;
	
			palette_map[color] |= Machine.gfx[0].pen_usage[code];
		}
	
		/* Do the text layer */
		for (offs = 0;offs < 0x1000;offs += 2)
		{
			int tile;
	
			tile = f2_textram.READ_WORD(offs) & 0xff;
			color = (f2_textram.READ_WORD(offs) & 0x0f00) >> 8;
	
			palette_map[color] |= Machine.gfx[2].pen_usage[tile];
		}
	
		/* Tell MAME about the color useage */
		for (i = 0;i < 256;i++)
		{
			int usage = palette_map[i];
			int j;
	
			if (usage != 0)
			{
				palette_used_colors.write(i * 16 + 0, PALETTE_COLOR_TRANSPARENT);
				for (j = 1; j < 16; j++)
					if ((palette_map[i] & (1 << j))!=0)
						palette_used_colors.write(i * 16 + j, PALETTE_COLOR_USED);
					else
						palette_used_colors.write(i * 16 + j,PALETTE_COLOR_UNUSED);
			}
			else
				memset(palette_used_colors,i * 16,PALETTE_COLOR_UNUSED,16);
		}
	
		if (palette_recalc ()!=null)
		{
			memset (text_dirty, 1, f2_textram_size[0]/2);
			memset (bg_dirty, 1, f2_backgroundram_size[0]/4);
			memset (fg_dirty, 1, f2_foregroundram_size[0]/4);
		}
	}
	
	static void taitof2_draw_sprites (osd_bitmap bitmap)
	{
		/*
			Sprite format:
			0000: 000xxxxxxxxxxxxx: tile code (0x0000 - 0x1fff)
			0002: unused?
			0004: 0000xxxxxxxxxxxx: x-coordinate absolute (-0x1ff to 0x01ff)
			      0x00000000000000: don't compensate for scrolling ??
			0006: 0000xxxxxxxxxxxx: y-coordinate absolute (-0x1ff to 0x01ff)
			0008: 00000000xxxxxxxx: color (0x00 - 0xff)
			      000000xx00000000: flipx & flipy
			      00000x0000000000: if clear, latch x & y coordinates, tile & color ?
			      0000x00000000000: if set, next sprite entry is part of sequence
			      000x000000000000: if clear, use latched y coordinate, else use current y
			      00x0000000000000: if set, y += 16
			      0x00000000000000: if clear, use latched x coordinate, else use current x
			      x000000000000000: if set, x += 16
			000a - 000f : unused?
	
			Additionally, the first 3 sprite entries are configuration info instead of
			actual sprites:
	
			offset 0x24: sprite x offset
			offset 0x26: sprite y offset
		*/
		int x,y,offs,code,color,spritecont,flipx,flipy;
		int xcurrent,ycurrent;
		int scroll1x, scroll1y;
		int scrollx=0, scrolly=0;
		int curx,cury;
	
		scroll1x = videoram.READ_WORD(0x24);
		scroll1y = videoram.READ_WORD(0x26);
	
		x = y = 0;
		xcurrent = ycurrent = 0;
		color = 0;
	
		for (offs = 0x30;offs < 0x3400;offs += 16)
		{
	        spritecont = (videoram.READ_WORD(offs+8) & 0xff00) >> 8;
	
			if ((spritecont & 0xf4) == 0)
			{
				x = videoram.READ_WORD(offs+4);
				if ((x & 0x4000) != 0)
				{
					scrollx = 0;
					scrolly = 0;
				}
				else
				{
					scrollx = scroll1x;
					scrolly = scroll1y;
				}
				x &= 0x1ff;
				y = videoram.READ_WORD(offs+6) & 0x01ff;
				color = videoram.READ_WORD(offs+8) & 0x00ff;
	
				xcurrent = x;
				ycurrent = y;
			}
			else
			{
				if ((spritecont & 0x10) == 0)
					y = ycurrent;
				else if ((spritecont & 0x20) != 0)
					y += 16;
	
				if ((spritecont & 0x40) == 0)
					x = xcurrent;
				else if ((spritecont & 0x80) != 0)
					x += 16;
			}
	
			code = videoram.READ_WORD(offs) & 0x1fff;
			if (code==0) continue;
	
			{
				int bank;
	
				bank = (code & 0x1c00) >> 10;
				code = spritebank[bank] + (code & 0x3ff);
			}
	
			flipx = spritecont & 0x01;
			flipy = spritecont & 0x02;
	
			// AJP (fixes sprites off right side of screen)
			curx = (x + scrollx) & 0x1ff;
			if (curx>0x140) curx -= 0x200;
			cury = (y + scrolly) & 0x1ff;
			if (cury>0x100) cury -= 0x200;
	
			if (color!=0xff)
			{
				drawgfx (bitmap,Machine.gfx[0],
					code,
					color,
					flipx,flipy,
					curx,cury,
					null,TRANSPARENCY_PEN,0);
			}
			else
			{
				// Mask sprite
				rectangle myclip=new rectangle();
				int tscrollx, tscrolly;
	
				myclip.min_x = curx;
				myclip.max_x = curx+15;
				myclip.min_y = cury;
				myclip.max_y = cury+15;
				tscrollx = taitof2_scrollx.READ_WORD(0) - ((videoram.READ_WORD(0x14)&0x7f)-0x50);
				tscrolly = taitof2_scrolly.READ_WORD(0) - 8;
				copyscrollbitmap (tmpbitmap3,tmpbitmap,1,new int[]{tscrollx},1,new int[]{tscrolly},myclip,TRANSPARENCY_NONE,0);
	
				tscrollx = taitof2_scrollx.READ_WORD(2) - ((videoram.READ_WORD(0x14)&0x7f)-0x50);
				tscrolly = taitof2_scrolly.READ_WORD(2) - 8;
				copyscrollbitmap (tmpbitmap3,tmpbitmap2,1,new int[]{tscrollx},1,new int[]{tscrolly},myclip,TRANSPARENCY_PEN,palette_transparent_pen);
	
				drawgfx (tmpbitmap3,Machine.gfx[0],
					code,
					color,
					flipx,flipy,
					curx,cury,
					null,TRANSPARENCY_PEN,15);
	
				copybitmap (bitmap,tmpbitmap3,0,0,0,0,myclip,TRANSPARENCY_PEN,palette_transparent_pen);
	
			}
		}
	}
	
	public static VhUpdatePtr taitof2_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
		int scrollx, scrolly;
	
		taitof2_update_palette ();
	
		/* Do the background layer */
		// This first pair seems to work only when video flip is on
	//	scrollx = READ_WORD(&taitof2_scrollx[0]) - READ_WORD(&taitof2_scrollx[4]);
	//	scrolly = READ_WORD(&taitof2_scrolly[0]) - READ_WORD(&taitof2_scrolly[4]);
		scrollx = taitof2_scrollx.READ_WORD(0) - ((videoram.READ_WORD(0x14)&0x7f)-0x50);
		scrolly = taitof2_scrolly.READ_WORD(0) - 8;
	
		for (offs = 0;offs < f2_backgroundram_size[0];offs += 4)
		{
			int tile, color, flipx, flipy;
	
			tile = f2_backgroundram.READ_WORD(offs + 2);
			color = (f2_backgroundram.READ_WORD(offs) & 0xff);
			flipy = (f2_backgroundram.READ_WORD(offs) & 0x8000);
			flipx = (f2_backgroundram.READ_WORD(offs) & 0x4000);
	
			if (bg_dirty[offs/4]!=0)
			{
				int sx,sy;
	
	
				bg_dirty[offs/4] = 0;
	
				sy = (offs/4) / 64;
				sx = (offs/4) % 64;
	
				drawgfx(tmpbitmap,Machine.gfx[1],
					tile,
					color,
					flipx,flipy,
					8*sx,8*sy,
					null,TRANSPARENCY_NONE,0);
			}
		}
	
		copyscrollbitmap(bitmap,tmpbitmap,1,new int[]{scrollx},1,new int[]{scrolly},Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	
		/* Do the foreground layer */
		// This first pair seems to work only when video flip is on
	//	scrollx = READ_WORD(&taitof2_scrollx[2]) - READ_WORD(&taitof2_scrollx[4]);
	//	scrolly = READ_WORD(&taitof2_scrolly[2]) - READ_WORD(&taitof2_scrolly[4]);
		scrollx = taitof2_scrollx.READ_WORD(2) - ((videoram.READ_WORD(0x14)&0x7f)-0x50);
		scrolly = taitof2_scrolly.READ_WORD(2) - 8;
	
		for (offs = 0;offs < f2_foregroundram_size[0];offs += 4)
		{
			int tile, color, flipx, flipy;
	
			tile = f2_foregroundram.READ_WORD(offs + 2);
			color = (f2_foregroundram.READ_WORD(offs) & 0xff);
			flipy = (f2_foregroundram.READ_WORD(offs) & 0x8000);
			flipx = (f2_foregroundram.READ_WORD(offs) & 0x4000);
	
			if (fg_dirty[offs/4]!=0)
			{
				int sx,sy;
	
	
				fg_dirty[offs/4] = 0;
	
				sy = (offs/4) / 64;
				sx = (offs/4) % 64;
	
				drawgfx(tmpbitmap2,Machine.gfx[1],
					tile,
					color,
					flipy,flipy,
					8*sx,8*sy,
					null,TRANSPARENCY_NONE,0);
			}
		}
	
		copyscrollbitmap(bitmap,tmpbitmap2,1,new int[]{scrollx},1,new int[]{scrolly},Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
	
		taitof2_draw_sprites (bitmap);
	
		/* Decode any characters that have changed */
		{
			int i;
	
			for (i = 0; i < 256; i ++)
			{
				if (char_dirty[i]!=0)
				{
					decodechar (Machine.gfx[2],i,taitof2_characterram, Machine.drv.gfxdecodeinfo[2].gfxlayout);
					char_dirty[i] = 0;
				}
			}
		}
	
		for (offs = 0;offs < 0x1000;offs += 2)
		{
			int tile, color, flipx, flipy;
	
			tile = f2_textram.READ_WORD(offs) & 0xff;
			if (tile==0) continue;
	
			color = (f2_textram.READ_WORD(offs) & 0x0f00) >> 8;
			flipy = (f2_textram.READ_WORD(offs) & 0x8000);
			flipx = (f2_textram.READ_WORD(offs) & 0x4000);
	
	//		if (text_dirty[offs/2] || char_dirty[tile])
			{
				int sx,sy;
	
	
	//			text_dirty[offs/2] = 0;
	//			char_dirty[tile] = 0;
	
				sy = (offs/2) / 64;
				sx = (offs/2) % 64;
	
				drawgfx(bitmap,Machine.gfx[2],
					tile,
					color*4,
					flipx,flipy,
					8*sx,8*sy,
					null,TRANSPARENCY_PEN,0);
			}
		}
	
	//	copyscrollbitmap(bitmap,tmpbitmap3,1,&scrollx,1,&scrolly,&Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
	
	} };
	
}
