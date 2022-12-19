/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class finalizr
{
	
	
	
	UBytePtr finalizr_scroll;
	UBytePtr finalizr_videoram2,*finalizr_colorram2;
	static int spriterambank;
	
	
	
	public static VhConvertColorPromPtr finalizr_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
		#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2,bit3;
	
	
			/* red component */
			bit0 = (color_prom.read(0)>> 0) & 0x01;
			bit1 = (color_prom.read(0)>> 1) & 0x01;
			bit2 = (color_prom.read(0)>> 2) & 0x01;
			bit3 = (color_prom.read(0)>> 3) & 0x01;
			*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			/* green component */
			bit0 = (color_prom.read(0)>> 4) & 0x01;
			bit1 = (color_prom.read(0)>> 5) & 0x01;
			bit2 = (color_prom.read(0)>> 6) & 0x01;
			bit3 = (color_prom.read(0)>> 7) & 0x01;
			*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			/* blue component */
			bit0 = (color_prom.read(Machine->drv->total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(Machine->drv->total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(Machine->drv->total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(Machine->drv->total_colors)>> 3) & 0x01;
			*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			color_prom++;
		}
	
		color_prom += Machine.drv.total_colors;
		/* color_prom now points to the beginning of the lookup tables */
	
		for (i = 0;i < TOTAL_COLORS(1);i++)
		{
			if (*color_prom & 0x0f) COLOR(1,i) = *color_prom & 0x0f;
			else COLOR(1,i) = 0;
			color_prom++;
		}
		for (i = 0;i < TOTAL_COLORS(0);i++)
		{
			COLOR(0,i) = (*(color_prom++) & 0x0f) + 0x10;
		}
	} };
	
	public static VhStartPtr finalizr_vh_start = new VhStartPtr() { public int handler() 
	{
		dirtybuffer = 0;
		tmpbitmap = 0;
	
		if ((dirtybuffer = malloc(videoram_size[0])) == 0)
			return 1;
		memset(dirtybuffer,1,videoram_size[0]);
	
		if ((tmpbitmap = bitmap_alloc(256,256)) == 0)
		{
			free(dirtybuffer);
			return 1;
		}
	
		return 0;
	} };
	
	public static VhStopPtr finalizr_vh_stop = new VhStopPtr() { public void handler() 
	{
		free(dirtybuffer);
		bitmap_free(tmpbitmap);
	
		dirtybuffer = 0;
		tmpbitmap = 0;
	} };
	
	
	
	public static WriteHandlerPtr finalizr_videoctrl_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		spriterambank = data & 8;
	
		/* other bits unknown */
	} };
	
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr finalizr_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
	
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = videoram_size[0] - 1;offs >= 0;offs--)
		{
			if (dirtybuffer[offs])
			{
				int sx,sy;
	
	
				dirtybuffer[offs] = 0;
	
				sx = offs % 32;
				sy = offs / 32;
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram.read(offs)+ ((colorram.read(offs)& 0xc0) << 2),
						(colorram.read(offs)& 0x0f),
						colorram.read(offs)& 0x10,colorram.read(offs)& 0x20,
						8*sx,8*sy,
						0,TRANSPARENCY_NONE,0);
			}
		}
	
	
		/* copy the temporary bitmap to the screen */
		{
			int scroll;
	
	
			scroll = -*finalizr_scroll + 16;
	
			copyscrollbitmap(bitmap,tmpbitmap,1,&scroll,0,0,&Machine.visible_area,TRANSPARENCY_NONE,0);
		}
	
	
		/* Draw the sprites. */
		{
			UBytePtr sr;
	
	
			if (spriterambank != 0)
				sr = spriteram_2;
			else sr = spriteram;
	
			for (offs = 0;offs < spriteram_size[0];offs += 5)
			{
				int sx,sy,flipx,flipy,code,color;
	
	
				sx = 16 + sr[offs+3] - ((sr[offs+4] & 0x01) << 8);
				sy = sr[offs+2];
				flipx = sr[offs+4] & 0x20;
				flipy = sr[offs+4] & 0x40;
				code = sr[offs] + ((sr[offs+1] & 0x0f) << 8);
				color = ((sr[offs+1] & 0xf0)>>4);
	
	//			(sr[offs+4] & 0x02) is used, meaning unknown
	
				switch (sr[offs+4] & 0x1c)
				{
					case 0x10:	/* 32x32? */
					case 0x14:	/* ? */
					case 0x18:	/* ? */
					case 0x1c:	/* ? */
						drawgfx(bitmap,Machine.gfx[1],
								code,
								color,
								flipx,flipy,
								flipx?sx+16:sx,flipy?sy+16:sy,
								&Machine.visible_area,TRANSPARENCY_PEN,0);
						drawgfx(bitmap,Machine.gfx[1],
								code + 1,
								color,
								flipx,flipy,
								flipx?sx:sx+16,flipy?sy+16:sy,
								&Machine.visible_area,TRANSPARENCY_PEN,0);
						drawgfx(bitmap,Machine.gfx[1],
								code + 2,
								color,
								flipx,flipy,
								flipx?sx+16:sx,flipy?sy:sy+16,
								&Machine.visible_area,TRANSPARENCY_PEN,0);
						drawgfx(bitmap,Machine.gfx[1],
								code + 3,
								color,
								flipx,flipy,
								flipx?sx:sx+16,flipy?sy:sy+16,
								&Machine.visible_area,TRANSPARENCY_PEN,0);
						break;
	
					case 0x00:	/* 16x16 */
						drawgfx(bitmap,Machine.gfx[1],
								code,
								color,
								flipx,flipy,
								sx,sy,
								&Machine.visible_area,TRANSPARENCY_PEN,0);
						break;
	
					case 0x04:	/* 16x8 */
						code = ((code & 0x3ff) << 2) | ((code & 0xc00) >> 10);
						drawgfx(bitmap,Machine.gfx[2],
								code & ~1,
								color,
								flipx,flipy,
								flipx?sx+8:sx,sy,
								&Machine.visible_area,TRANSPARENCY_PEN,0);
						drawgfx(bitmap,Machine.gfx[2],
								code | 1,
								color,
								flipx,flipy,
								flipx?sx:sx+8,sy,
								&Machine.visible_area,TRANSPARENCY_PEN,0);
						break;
	
					case 0x08:	/* 8x16 */
						code = ((code & 0x3ff) << 2) | ((code & 0xc00) >> 10);
						drawgfx(bitmap,Machine.gfx[2],
								code & ~2,
								color,
								flipx,flipy,
								sx,flipy?sy+8:sy,
								&Machine.visible_area,TRANSPARENCY_PEN,0);
						drawgfx(bitmap,Machine.gfx[2],
								code | 2,
								color,
								flipx,flipy,
								sx,flipy?sy:sy+8,
								&Machine.visible_area,TRANSPARENCY_PEN,0);
						break;
	
					case 0x0c:	/* 8x8 */
						code = ((code & 0x3ff) << 2) | ((code & 0xc00) >> 10);
						drawgfx(bitmap,Machine.gfx[2],
								code,
								color,
								flipx,flipy,
								sx,sy,
								&Machine.visible_area,TRANSPARENCY_PEN,0);
						break;
				}
			}
		}
	
		for (offs = videoram_size[0] - 1;offs >= 0;offs--)
		{
			int sx,sy;
	
	
			sx = offs % 32;
			if (sx < 6)
			{
				if (sx >= 3) sx += 30;
				sy = offs / 32;
	
				drawgfx(bitmap,Machine.gfx[0],
						finalizr_videoram2[offs] + ((finalizr_colorram2[offs] & 0xc0) << 2),
						(finalizr_colorram2[offs] & 0x0f),
						finalizr_colorram2[offs] & 0x10,finalizr_colorram2[offs] & 0x20,
						8*sx,8*sy,
						&Machine.visible_area,TRANSPARENCY_NONE,0);
			}
		}
	} };
}
