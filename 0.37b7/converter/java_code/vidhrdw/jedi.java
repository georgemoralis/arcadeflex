/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

#define JEDI_PLAYFIELD

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class jedi
{
	
	UBytePtr jedi_backgroundram;
	size_t jedi_backgroundram_size;
	UBytePtr jedi_PIXIRAM;
	static unsigned int jedi_vscroll;
	static unsigned int jedi_hscroll;
	static unsigned int jedi_alpha_bank;
	static int video_off,smooth_table;
	static UBytePtr dirtybuffer2;
	static struct osd_bitmap *tmpbitmap2,*tmpbitmap3;
	
	
	
	/* Color RAM format
	   Color RAM is 1024x12
	   RAM address: A0..A3 = Playfield color code
	                A4..A7 = Motion object color code
	                A8..A9 = Alphanumeric color code
	   RAM data:
	                0..2 = Blue
	                3..5 = Green
	                6..8 = Blue
	                9..11 = Intesnsity
	    Output resistor values:
	               bit 0 = 22K
	               bit 1 = 10K
	               bit 2 = 4.7K
	*/
	
	
	public static WriteHandlerPtr jedi_paletteram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	    int r,g,b;
		int bits,intensity;
	    unsigned int color;
	
	
		paletteram.write(offset,data);
	color = paletteram.read(offset & 0x3FF)| (paletteram.read(offset | 0x400)<< 8);
		intensity = (color >> 9) & 0x07;
		bits = (color >> 6) & 0x07;
		r = 5 * bits * intensity;
		bits = (color >> 3) & 0x07;
		g = 5 * bits * intensity;
		bits = (color >> 0) & 0x07;
		b = 5 * bits * intensity;
	
		palette_change_color (offset & 0x3ff,r,g,b);
	} };
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	public static VhStartPtr jedi_vh_start = new VhStartPtr() { public int handler() 
	{
		if ((dirtybuffer = malloc(videoram_size[0])) == 0)
		{
			return 1;
		}
		memset(dirtybuffer,1,videoram_size[0]);
	
		if ((tmpbitmap = bitmap_alloc_depth(Machine.drv.screen_width,Machine.drv.screen_height,8)) == 0)
		{
			free(dirtybuffer);
			return 1;
		}
	
		if ((dirtybuffer2 = malloc(jedi_backgroundram_size)) == 0)
		{
			bitmap_free(tmpbitmap);
			free(dirtybuffer);
			return 1;
		}
		memset(dirtybuffer2,1,jedi_backgroundram_size);
	
		if ((tmpbitmap2 = bitmap_alloc_depth(Machine.drv.screen_width,Machine.drv.screen_height,8)) == 0)
		{
			bitmap_free(tmpbitmap);
			free(dirtybuffer);
			free(dirtybuffer2);
			return 1;
		}
	
		/* the background area is 256x256, doubled by the hardware*/
		if ((tmpbitmap3 = bitmap_alloc_depth(256,256,8)) == 0)
		{
			bitmap_free(tmpbitmap);
			bitmap_free(tmpbitmap2);
			free(dirtybuffer);
			free(dirtybuffer2);
			return 1;
		}
	
		/* reserve color 1024 for black (disabled display) */
		palette_change_color(1024,0,0,0);
	
		return 0;
	} };
	
	public static VhStopPtr jedi_vh_stop = new VhStopPtr() { public void handler() 
	{
		bitmap_free(tmpbitmap);
		bitmap_free(tmpbitmap2);
		bitmap_free(tmpbitmap3);
		free(dirtybuffer);
		free(dirtybuffer2);
	} };
	
	
	
	public static WriteHandlerPtr jedi_backgroundram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (jedi_backgroundram[offset] != data)
		{
			dirtybuffer2[offset] = 1;
	
			jedi_backgroundram[offset] = data;
		}
	} };
	
	public static WriteHandlerPtr jedi_vscroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	    jedi_vscroll = data | (offset << 8);
	} };
	
	public static WriteHandlerPtr jedi_hscroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	    jedi_hscroll = data | (offset << 8);
	} };
	
	public static WriteHandlerPtr jedi_alpha_banksel_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (jedi_alpha_bank != 2*(data & 0x80))
		{
			jedi_alpha_bank = 2*(data & 0x80);
			memset(dirtybuffer,1,videoram_size[0]);
		}
	} };
	
	public static WriteHandlerPtr jedi_video_off_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		video_off = data;
	} };
	
	public static WriteHandlerPtr jedi_PIXIRAM_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		smooth_table = data & 0x03;
	} };
	
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr jedi_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
	
	
		if (palette_recalc())
		{
			memset(dirtybuffer,1,videoram_size[0]);
			memset(dirtybuffer2,1,jedi_backgroundram_size);
		}
	
		if (video_off != 0)
		{
			fillbitmap(bitmap,Machine.pens[1024],&Machine.visible_area);
			return;
		}
	
	
		/* Return of the Jedi has a peculiar playfield/sprite priority system. That */
		/* is, there is no priority system ;-) The color of the pixel which appears on */
		/* screen depends on all three of the foreground, background and sprites. The */
		/* 1024 colors palette is appropriately set up by the program to "emulate" a */
		/* priority system, but it can also be used to display completely different */
		/* colors (see the palette test in service mode) */
	
	    /* foreground */
	    for (offs = videoram_size[0] - 1;offs >= 0;offs--)
		{
			if (dirtybuffer[offs])
			{
				int sx,sy;
	
	
				dirtybuffer[offs] = 0;
	
				sx = offs % 64;
				sy = offs / 64;
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram.read(offs)+ jedi_alpha_bank,
						0,
						0,0,
						8*sx,8*sy,
						&Machine.visible_area,TRANSPARENCY_NONE_RAW,0);
			}
	    }
	
	
		/* background */
		for (offs = jedi_backgroundram_size/2 - 1;offs >= 0;offs--)
		{
			if (dirtybuffer2[offs] != 0 || dirtybuffer2[offs + 0x400] != 0)
			{
				int sx,sy,b,c;
	
	
				dirtybuffer2[offs] = dirtybuffer2[offs + 0x400] = 0;
	
				sx = offs % 32;
				sy = offs / 32;
				c = (jedi_backgroundram[offs] & 0xFF);
				b = (jedi_backgroundram[offs + 0x400] & 0x0F);
				c |= (b & 0x01) << 8;
				c |= (b & 0x08) << 6;
				c |= (b & 0x02) << 9;
	
				drawgfx(tmpbitmap3,Machine.gfx[1],
						c,
						0,
						b & 0x04,0,
						8*sx,8*sy,
						0,TRANSPARENCY_NONE_RAW,0);
			}
		}
	
	
		/* Draw the sprites. Note that it is important to draw them exactly in this */
		/* order, to have the correct priorities. */
		fillbitmap(tmpbitmap2,0,&Machine.visible_area);
	
	    for (offs = 0;offs < 0x30;offs++)
		{
			int x,y,flipx,flipy;
			int b,c;
	
	
			b = ((spriteram.read(offs+0x40)& 0x02) >> 1);
			b = b | ((spriteram.read(offs+0x40)& 0x40) >> 5);
			b = b | (spriteram.read(offs+0x40)& 0x04);
	
			c = spriteram.read(offs)+ (b * 256);
			if (spriteram.read(offs+0x40)& 0x08) c |= 1;	/* double height */
	
			/* coordinates adjustments made to match screenshot */
			x = spriteram.read(offs+0x100)+ ((spriteram.read(offs+0x40)& 0x01) << 8) - 2;
			y = 240-spriteram.read(offs+0x80)+ 1;
			flipx = spriteram.read(offs+0x40)& 0x10;
			flipy = spriteram.read(offs+0x40)& 0x20;
	
			drawgfx(tmpbitmap2,Machine.gfx[2],
					c,
					0,
					flipx,flipy,
					x,y,
					&Machine.visible_area,TRANSPARENCY_PEN_RAW,0);
	
			if (spriteram.read(offs+0x40)& 0x08)	/* double height */
				drawgfx(tmpbitmap2,Machine.gfx[2],
						c-1,
						0,
						flipx,flipy,
						x,y-16,
						&Machine.visible_area,TRANSPARENCY_PEN_RAW,0);
	    }
	
	
		/* compose the three layers */
		{
			int x,y;
			UINT8 *s1,*s2,*s3,*s3b;
			UINT8 *prom = memory_region(REGION_PROMS) + smooth_table * 0x100;
	
	
			if (bitmap.depth == 16)
			{
				for (y = 0;y < bitmap.height;y++)
				{
					UINT16 *d = (UINT16 *)bitmap.line[y];
	
					s1 = tmpbitmap.line[y];
					s2 = tmpbitmap2.line[y];
					s3 = tmpbitmap3.line[((y + jedi_vscroll) & 0x1ff) / 2];
					s3b = tmpbitmap3.line[((y + jedi_vscroll - 1) & 0x1ff) / 2];
	
					for (x = 0;x < bitmap.width;x++)
					{
						int tl,tr,bl,br,mixt,mixb,mix;
	
						tr = s3b[((x + jedi_hscroll + 1) & 0x1ff) / 2];
						br = s3 [((x + jedi_hscroll + 1) & 0x1ff) / 2];
	
						if ((x + jedi_hscroll) & 1)
						{
							tl = s3b[((x + jedi_hscroll) & 0x1ff) / 2];
							bl = s3 [((x + jedi_hscroll) & 0x1ff) / 2];
							mixt = prom[16 * tl + tr];
							mixb = prom[16 * bl + br];
							mix = prom[0x400 + 16 * mixt + mixb];
						}
						else
							mix = prom[0x400 + 16 * tr + br];
	
						*(d++) = Machine.pens[(*(s1++) << 8) | (*(s2++) << 4) | mix ];
					}
				}
			}
			else
			{
				for (y = 0;y < bitmap.height;y++)
				{
					UINT8 *d = bitmap.line[y];
	
					s1 = tmpbitmap.line[y];
					s2 = tmpbitmap2.line[y];
					s3 = tmpbitmap3.line[((y + jedi_vscroll) & 0x1ff) / 2];
					s3b = tmpbitmap3.line[((y + jedi_vscroll - 1) & 0x1ff) / 2];
	
					for (x = 0;x < bitmap.width;x++)
					{
						int tl,tr,bl,br,mixt,mixb,mix;
	
						tr = s3b[((x + jedi_hscroll + 1) & 0x1ff) / 2];
						br = s3 [((x + jedi_hscroll + 1) & 0x1ff) / 2];
	
						if ((x + jedi_hscroll) & 1)
						{
							tl = s3b[((x + jedi_hscroll) & 0x1ff) / 2];
							bl = s3 [((x + jedi_hscroll) & 0x1ff) / 2];
							mixt = prom[16 * tl + tr];
							mixb = prom[16 * bl + br];
							mix = prom[0x400 + 16 * mixt + mixb];
						}
						else
							mix = prom[0x400 + 16 * tr + br];
	
						*(d++) = Machine.pens[(*(s1++) << 8) | (*(s2++) << 4) | mix ];
					}
				}
			}
		}
	} };
}
