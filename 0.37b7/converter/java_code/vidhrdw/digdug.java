/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class digdug
{
	
	
	UBytePtr digdug_vlatches;
	static int playfield, alphacolor, playenable, playcolor;
	
	static int pflastindex = -1, pflastcolor = -1;
	static int flipscreen;
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  digdug has one 32x8 palette PROM and two 256x4 color lookup table PROMs
	  (one for characters, one for sprites). Only the first 128 bytes of the
	  lookup tables seem to be used.
	  The palette PROM is connected to the RGB output this way:
	
	  bit 7 -- 220 ohm resistor  -- BLUE
	        -- 470 ohm resistor  -- BLUE
	        -- 220 ohm resistor  -- GREEN
	        -- 470 ohm resistor  -- GREEN
	        -- 1  kohm resistor  -- GREEN
	        -- 220 ohm resistor  -- RED
	        -- 470 ohm resistor  -- RED
	  bit 0 -- 1  kohm resistor  -- RED
	
	***************************************************************************/
	public static VhConvertColorPromPtr digdug_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
	
		for (i = 0;i < 32;i++)
		{
			int bit0,bit1,bit2;
	
			bit0 = (color_prom.read(31-i)>> 0) & 0x01;
			bit1 = (color_prom.read(31-i)>> 1) & 0x01;
			bit2 = (color_prom.read(31-i)>> 2) & 0x01;
			palette[3*i] = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			bit0 = (color_prom.read(31-i)>> 3) & 0x01;
			bit1 = (color_prom.read(31-i)>> 4) & 0x01;
			bit2 = (color_prom.read(31-i)>> 5) & 0x01;
			palette[3*i + 1] = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			bit0 = 0;
			bit1 = (color_prom.read(31-i)>> 6) & 0x01;
			bit2 = (color_prom.read(31-i)>> 7) & 0x01;
			palette[3*i + 2] = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
		}
	
		/* characters */
		for (i = 0; i < 8; i++)
		{
			colortable[i*2 + 0] = 0;
			colortable[i*2 + 1] = 31 - i*2;
		}
		/* sprites */
		for (i = 0*4;i < 64*4;i++)
			colortable[8*2 + i] = 31 - ((color_prom.read(i + 32)& 0x0f) + 0x10);
		/* playfield */
		for (i = 64*4;i < 128*4;i++)
			colortable[8*2 + i] = 31 - (color_prom.read(i + 32)& 0x0f);
	} };
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	public static VhStartPtr digdug_vh_start = new VhStartPtr() { public int handler() 
	{
		if (generic_vh_start() != 0)
			return 1;
	
		pflastindex = -1;
		pflastcolor = -1;
	
		return 0;
	} };
	
	
	/***************************************************************************
	
	  Stop the video hardware emulation.
	
	***************************************************************************/
	public static VhStopPtr digdug_vh_stop = new VhStopPtr() { public void handler() 
	{
		generic_vh_stop();
	} };
	
	
	public static WriteHandlerPtr digdug_vh_latch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		switch (offset)
		{
			case 0:
				playfield = (playfield & ~1) | (data & 1);
				break;
	
			case 1:
				playfield = (playfield & ~2) | ((data << 1) & 2);
				break;
	
			case 2:
				alphacolor = data & 1;
				break;
	
			case 3:
				playenable = data & 1;
				break;
	
			case 4:
				playcolor = (playcolor & ~1) | (data & 1);
				break;
	
			case 5:
				playcolor = (playcolor & ~2) | ((data << 1) & 2);
				break;
		}
	} };
	
	
	void digdug_draw_sprite(struct osd_bitmap *dest,unsigned int code,unsigned int color,
		int flipx,int flipy,int sx,int sy)
	{
		drawgfx(dest,Machine.gfx[1],code,color,flipx,flipy,sx,sy,&Machine.visible_area,
			TRANSPARENCY_PEN,0);
	}
	
	
	
	public static WriteHandlerPtr digdug_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (flipscreen != (data & 1))
		{
			flipscreen = data & 1;
			memset(dirtybuffer,1,videoram_size[0]);
		}
	} };
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr digdug_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs,pfindex,pfcolor;
		UBytePtr pf;
	
		/* determine the playfield */
		if (playenable != 0)
		{
			pfindex = pfcolor = -1;
			pf = NULL;
		}
		else
		{
			pfindex = playfield;
			pfcolor = playcolor;
			pf = memory_region(REGION_GFX4) + (pfindex << 10);
		}
	
		/* force a full update if the playfield has changed */
		if (pfindex != pflastindex || pfcolor != pflastcolor)
		{
			memset(dirtybuffer,1,videoram_size[0]);
		}
		pflastindex = pfindex;
		pflastcolor = pfcolor;
	
		pfcolor <<= 4;
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = videoram_size[0] - 1;offs >= 0;offs--)
		{
			if (dirtybuffer[offs])
			{
				unsigned char pfval, vrval;
				int sx,sy,mx,my;
	
				dirtybuffer[offs] = 0;
	
				/* Even if digdug's screen is 28x36, the memory layout is 32x32. We therefore */
				/* have to convert the memory coordinates into screen coordinates. */
				/* Note that 32*32 = 1024, while 28*36 = 1008: therefore 16 bytes of Video RAM */
				/* don't map to a screen position. We don't check that here, however: range */
				/* checking is performed by drawgfx(). */
	
				mx = offs % 32;
				my = offs / 32;
	
				if (my <= 1)
				{
					sx = my + 34;
					sy = mx - 2;
				}
				else if (my >= 30)
				{
					sx = my - 30;
					sy = mx - 2;
				}
				else
				{
					sx = mx + 2;
					sy = my - 2;
				}
	
				if (flipscreen != 0)
				{
					sx = 35 - sx;
					sy = 27 - sy;
				}
	
				vrval = videoram.read(offs);
				if (pf != 0)
				{
					/* first draw the playfield */
					pfval = pf[offs];
					drawgfx(tmpbitmap,Machine.gfx[2],
							pfval,
							(pfval >> 4) + pfcolor,
							flipscreen,flipscreen,
							8*sx,8*sy,
							&Machine.visible_area,TRANSPARENCY_NONE,0);
	
					/* overlay with the character */
					if ((vrval & 0x7f) != 0x7f)
						drawgfx(tmpbitmap,Machine.gfx[0],
								vrval,
								(vrval >> 5) | ((vrval >> 4) & 1),
								flipscreen,flipscreen,
								8*sx,8*sy,
								&Machine.visible_area,TRANSPARENCY_PEN,0);
				}
				else
				{
					/* just draw the character */
					drawgfx(tmpbitmap,Machine.gfx[0],
							vrval,
							(vrval >> 5) | ((vrval >> 4) & 1),
							flipscreen,flipscreen,
							8*sx,8*sy,
							&Machine.visible_area,TRANSPARENCY_NONE,0);
				}
			}
		}
	
		/* copy the temporary bitmap to the screen */
		copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine.visible_area,TRANSPARENCY_NONE,0);
	
		/* Draw the sprites. */
		for (offs = 0;offs < spriteram_size[0];offs += 2)
		{
			/* is it on? */
			if ((spriteram_3.read(offs+1)& 2) == 0)
			{
				int sprite = spriteram.read(offs);
				int color = spriteram.read(offs+1);
				int x = spriteram_2.read(offs+1)-40;
				int y = 28*8-spriteram_2.read(offs);
				int flipx = spriteram_3.read(offs)& 1;
				int flipy = spriteram_3.read(offs)& 2;
	
				if (flipscreen != 0)
				{
					flipx = !flipx;
					flipy = !flipy;
				}
	
				if (x < 8) x += 256;
	
				/* normal size? */
				if (sprite < 0x80)
					digdug_draw_sprite(bitmap,sprite,color,flipx,flipy,x,y);
	
				/* double size? */
				else
				{
					sprite = (sprite & 0xc0) | ((sprite & ~0xc0) << 2);
					if (!flipx && !flipy)
					{
						digdug_draw_sprite(bitmap,2+sprite,color,flipx,flipy,x,y);
						digdug_draw_sprite(bitmap,3+sprite,color,flipx,flipy,x+16,y);
						digdug_draw_sprite(bitmap,sprite,color,flipx,flipy,x,y-16);
						digdug_draw_sprite(bitmap,1+sprite,color,flipx,flipy,x+16,y-16);
					}
					else if (flipx && flipy)
					{
						digdug_draw_sprite(bitmap,1+sprite,color,flipx,flipy,x,y);
						digdug_draw_sprite(bitmap,sprite,color,flipx,flipy,x+16,y);
						digdug_draw_sprite(bitmap,3+sprite,color,flipx,flipy,x,y-16);
						digdug_draw_sprite(bitmap,2+sprite,color,flipx,flipy,x+16,y-16);
					}
					else if (flipy != 0)
					{
						digdug_draw_sprite(bitmap,sprite,color,flipx,flipy,x,y);
						digdug_draw_sprite(bitmap,1+sprite,color,flipx,flipy,x+16,y);
						digdug_draw_sprite(bitmap,2+sprite,color,flipx,flipy,x,y-16);
						digdug_draw_sprite(bitmap,3+sprite,color,flipx,flipy,x+16,y-16);
					}
					else /* flipx */
					{
						digdug_draw_sprite(bitmap,3+sprite,color,flipx,flipy,x,y);
						digdug_draw_sprite(bitmap,2+sprite,color,flipx,flipy,x+16,y);
						digdug_draw_sprite(bitmap,1+sprite,color,flipx,flipy,x,y-16);
						digdug_draw_sprite(bitmap,sprite,color,flipx,flipy,x+16,y-16);
					}
				}
			}
		}
	} };
}
