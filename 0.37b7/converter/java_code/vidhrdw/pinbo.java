/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class pinbo
{
	
	
	static int flipscreen[2];
	
	
	
	public static VhConvertColorPromPtr pinbo_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
	
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2,bit3;
	
			/* red component */
			bit0 = (color_prom.read(0)>> 0) & 0x01;
			bit1 = (color_prom.read(0)>> 1) & 0x01;
			bit2 = (color_prom.read(0)>> 2) & 0x01;
			bit3 = (color_prom.read(0)>> 3) & 0x01;
			*(palette++) =  0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			/* green component */
			bit0 = (color_prom.read(Machine->drv->total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(Machine->drv->total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(Machine->drv->total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(Machine->drv->total_colors)>> 3) & 0x01;
			*(palette++) =  0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			/* blue component */
			bit0 = (color_prom.read(2*Machine->drv->total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(2*Machine->drv->total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(2*Machine->drv->total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(2*Machine->drv->total_colors)>> 3) & 0x01;
			*(palette++) =  0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			color_prom++;
		}
	} };
	
	public static WriteHandlerPtr pinbo_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (flipscreen[0] != (data & 1))
		{
			flipscreen[0] = data & 1;
			memset(dirtybuffer,1,videoram_size[0]);
		}
		if (flipscreen[1] != (data & 2))
		{
			flipscreen[1] = data & 2;
			memset(dirtybuffer,1,videoram_size[0]);
		}
	} };
	
	public static VhUpdatePtr pinbo_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
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
				if (flipscreen[0]) sx = 31 - sx;
				if (flipscreen[1]) sy = 31 - sy;
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram.read(offs)+ ((colorram.read(offs)& 0x70) << 5),
						colorram.read(offs)& 0x0f,
						flipscreen[0],flipscreen[1],
						8*sx,8*sy,
						&Machine.visible_area,TRANSPARENCY_NONE,0);
			}
		}
	
		/* copy the character mapped graphics */
		copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine.visible_area,TRANSPARENCY_NONE,0);
	
		/* Draw the sprites. */
		for (offs = spriteram_size[0] - 4;offs >= 0;offs -= 4)
		{
			int sx,sy,flipx,flipy;
			int code,color;
	
	
			sx = spriteram.read(offs + 3);
			sy = 240 - spriteram.read(offs);
			flipx = spriteram.read(offs+1)& 0x40;
			flipy = spriteram.read(offs+1)& 0x80;
			if (flipscreen[0])
			{
				sx = 240 - sx;
				flipx = !flipx;
			}
			if (flipscreen[1])
			{
				sy = 240 - sy;
				flipy = !flipy;
			}
			code = (spriteram.read(offs+1)& 0x3f) | 0x40 | ((spriteram.read(offs+2)& 0x30) << 3);
			color = (spriteram.read(offs+2)& 0x0f);
	
			drawgfx(bitmap,Machine.gfx[1],
					code,
					color,
					flipx,flipy,
					sx,sy,
					&Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	} };
}
