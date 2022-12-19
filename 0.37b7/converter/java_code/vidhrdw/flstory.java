/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/
/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class flstory
{
	
	
	static int palette_bank;
	
	
	public static VhConvertColorPromPtr flstory_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
	
	
		/* no color PROMs here, only RAM, but the gfx data is inverted so we */
		/* cannot use the default lookup table */
		for (i = 0;i < Machine.drv.color_table_len;i++)
			colortable[i] = i ^ 0x0f;
	} };
	
	
	
	public static VhStartPtr flstory_vh_start = new VhStartPtr() { public int handler() 
	{
		paletteram = malloc(0x200);
		paletteram_2 = malloc(0x200);
		return generic_vh_start();
	} };
	
	public static VhStopPtr flstory_vh_stop = new VhStopPtr() { public void handler() 
	{
		free(paletteram);
		paletteram = 0;
		free(paletteram_2);
		paletteram_2 = 0;
		generic_vh_stop();
	} };
	
	
	
	public static WriteHandlerPtr flstory_palette_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((offset & 0x100) != 0)
			paletteram_xxxxBBBBGGGGRRRR_split2_w((offset & 0xff) + (palette_bank << 8),data);
		else
			paletteram_xxxxBBBBGGGGRRRR_split1_w((offset & 0xff) + (palette_bank << 8),data);
	} };
	
	public static WriteHandlerPtr flstory_gfxctrl_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		palette_bank = (data & 0x20) >> 5;
	//logerror("%04x: gfxctrl = %02x\n",cpu_get_pc(),data);
	} };
	
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr flstory_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
	
	
		if (palette_recalc())
			memset(dirtybuffer,1,videoram_size[0]);
	
		for (offs = videoram_size[0] - 2;offs >= 0;offs -= 2)
		{
			if (dirtybuffer[offs] || dirtybuffer[offs+1])
			{
				int sx,sy;
	
	
				dirtybuffer[offs] = 0;
				dirtybuffer[offs+1] = 0;
	
				sx = (offs/2)%32;
				sy = (offs/2)/32;
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram.read(offs)+ ((videoram.read(offs + 1)& 0xc0) << 2) + 0xc00,
						videoram.read(offs + 1)& 0x07,
						videoram.read(offs + 1)& 0x08,1,
						8*sx,8*sy,
						&Machine.visible_area,TRANSPARENCY_NONE,0);
			}
		}
	
		copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine.visible_area,TRANSPARENCY_NONE,0);
	
		for (offs = 0;offs < spriteram_size[0];offs += 4)
		{
			int code,sx,sy,flipx,flipy;
	
	
			code = spriteram.read(offs+2)+ ((spriteram.read(offs+1)& 0x30) << 4);
			sx = spriteram.read(offs+3);
			sy = 240 - spriteram.read(offs+0);
			flipx = spriteram.read(offs+1)&0x40;
			flipy = spriteram.read(offs+1)&0x80;
	
			drawgfx(bitmap,Machine.gfx[1],
					code,
					spriteram.read(offs+1)& 0x0f,
					flipx,flipy,
					sx,sy,
					&Machine.visible_area,TRANSPARENCY_PEN,0);
			/* wrap around */
			if (sx > 240)
				drawgfx(bitmap,Machine.gfx[1],
						code,
						spriteram.read(offs+1)& 0x0f,
						flipx,flipy,
						sx-256,sy,
						&Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	
		/* redraw chars with priority over sprites */
		for (offs = videoram_size[0] - 2;offs >= 0;offs -= 2)
		{
			if (videoram.read(offs + 1)& 0x20)
			{
				int sx,sy;
	
	
				sx = (offs/2)%32;
				sy = (offs/2)/32;
	
				drawgfx(bitmap,Machine.gfx[0],
						videoram.read(offs)+ ((videoram.read(offs + 1)& 0xc0) << 2) + 0xc00,
						videoram.read(offs + 1)& 0x07,
						videoram.read(offs + 1)& 0x08,1,
						8*sx,8*sy,
						&Machine.visible_area,TRANSPARENCY_PEN,0);
			}
		}
	} };
}
