/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class mpatrol
{
	
	
	
	#define BGHEIGHT (64)
	
	static unsigned char scrollreg[16];
	static unsigned char bg1xpos,bg1ypos,bg2xpos,bg2ypos,bgcontrol;
	static struct osd_bitmap *bgbitmap[3];
	
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Moon Patrol has one 256x8 character palette PROM, one 32x8 background
	  palette PROM, one 32x8 sprite palette PROM and one 256x4 sprite lookup
	  table PROM.
	
	  The character and background palette PROMs are connected to the RGB output
	  this way:
	
	  bit 7 -- 220 ohm resistor  -- BLUE
	        -- 470 ohm resistor  -- BLUE
	        -- 220 ohm resistor  -- GREEN
	        -- 470 ohm resistor  -- GREEN
	        -- 1  kohm resistor  -- GREEN
	        -- 220 ohm resistor  -- RED
	        -- 470 ohm resistor  -- RED
	  bit 0 -- 1  kohm resistor  -- RED
	
	  The sprite palette PROM is connected to the RGB output this way. Note that
	  RED and BLUE are swapped wrt the usual configuration.
	
	  bit 7 -- 220 ohm resistor  -- RED
	        -- 470 ohm resistor  -- RED
	        -- 220 ohm resistor  -- GREEN
	        -- 470 ohm resistor  -- GREEN
	        -- 1  kohm resistor  -- GREEN
	        -- 220 ohm resistor  -- BLUE
	        -- 470 ohm resistor  -- BLUE
	  bit 0 -- 1  kohm resistor  -- BLUE
	
	***************************************************************************/
	public static VhConvertColorPromPtr mpatrol_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
		#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
	
		/* character palette */
		for (i = 0;i < 128;i++)
		{
			int bit0,bit1,bit2;
	
	
			/* red component */
			bit0 = (*color_prom >> 0) & 0x01;
			bit1 = (*color_prom >> 1) & 0x01;
			bit2 = (*color_prom >> 2) & 0x01;
			*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* green component */
			bit0 = (*color_prom >> 3) & 0x01;
			bit1 = (*color_prom >> 4) & 0x01;
			bit2 = (*color_prom >> 5) & 0x01;
			*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* blue component */
			bit0 = 0;
			bit1 = (*color_prom >> 6) & 0x01;
			bit2 = (*color_prom >> 7) & 0x01;
			*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	
			color_prom++;
		}
	
		color_prom += 128;	/* skip the bottom half of the PROM - not used */
		/* color_prom now points to the beginning of the background palette */
	
	
		/* character lookup table */
		for (i = 0;i < TOTAL_COLORS(0)/2;i++)
		{
			COLOR(0,i) = i;
	
			/* also create a color code with transparent pen 0 */
			if (i % 4 == 0)	COLOR(0,i + TOTAL_COLORS(0)/2) = 0;
			else COLOR(0,i + TOTAL_COLORS(0)/2) = i;
		}
	
	
		/* background palette */
		/* reserve one color for the transparent pen (none of the game colors can have */
		/* these RGB components) */
		*(palette++) = 1;
		*(palette++) = 1;
		*(palette++) = 1;
		color_prom++;
	
		for (i = 1;i < 32;i++)
		{
			int bit0,bit1,bit2;
	
	
			/* red component */
			bit0 = (*color_prom >> 0) & 0x01;
			bit1 = (*color_prom >> 1) & 0x01;
			bit2 = (*color_prom >> 2) & 0x01;
			*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* green component */
			bit0 = (*color_prom >> 3) & 0x01;
			bit1 = (*color_prom >> 4) & 0x01;
			bit2 = (*color_prom >> 5) & 0x01;
			*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* blue component */
			bit0 = 0;
			bit1 = (*color_prom >> 6) & 0x01;
			bit2 = (*color_prom >> 7) & 0x01;
			*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	
			color_prom++;
		}
	
		/* color_prom now points to the beginning of the sprite palette */
	
		/* sprite palette */
		for (i = 0;i < 32;i++)
		{
			int bit0,bit1,bit2;
	
	
			/* red component */
			bit0 = 0;
			bit1 = (*color_prom >> 6) & 0x01;
			bit2 = (*color_prom >> 7) & 0x01;
			*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* green component */
			bit0 = (*color_prom >> 3) & 0x01;
			bit1 = (*color_prom >> 4) & 0x01;
			bit2 = (*color_prom >> 5) & 0x01;
			*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* blue component */
			bit0 = (*color_prom >> 0) & 0x01;
			bit1 = (*color_prom >> 1) & 0x01;
			bit2 = (*color_prom >> 2) & 0x01;
			*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	
			color_prom++;
		}
	
		/* color_prom now points to the beginning of the sprite lookup table */
	
		/* sprite lookup table */
		for (i = 0;i < TOTAL_COLORS(1);i++)
		{
			COLOR(1,i) = 128+32 + (*color_prom++);
			if (i % 4 == 3) color_prom += 4;	/* half of the PROM is unused */
		}
	
		color_prom += 128;	/* skip the bottom half of the PROM - not used */
	
		/* background */
		/* the palette is a 32x8 PROM with many colors repeated. The address of */
		/* the colors to pick is as follows: */
		/* xbb00: mountains */
		/* 0xxbb: hills */
		/* 1xxbb: city */
		COLOR(2,0) = 128;
		COLOR(2,1) = 128+4;
		COLOR(2,2) = 128+8;
		COLOR(2,3) = 128+12;
		COLOR(4,0) = 128;
		COLOR(4,1) = 128+1;
		COLOR(4,2) = 128+2;
		COLOR(4,3) = 128+3;
		COLOR(6,0) = 128;
		COLOR(6,1) = 128+16+1;
		COLOR(6,2) = 128+16+2;
		COLOR(6,3) = 128+16+3;
	} };
	
	
	
	/***************************************************************************
	
	  Stop the video hardware emulation.
	
	***************************************************************************/
	public static VhStartPtr mpatrol_vh_start = new VhStartPtr() { public int handler() 
	{
		int i,j;
	
	
		if (generic_vh_start() != 0)
			return 1;
	
		/* prepare the background graphics */
		for (i = 0;i < 3;i++)
		{
			/* temp bitmap for the three background images */
			if ((bgbitmap[i] = bitmap_alloc(256,BGHEIGHT)) == 0)
			{
				generic_vh_stop();
				return 1;
			}
	
			for (j = 0;j < 8;j++)
			{
				drawgfx(bgbitmap[i],Machine.gfx[2 + 2 * i],
						j,0,
						0,0,
						32 * j,0,
						0,TRANSPARENCY_NONE,0);
	
				drawgfx(bgbitmap[i],Machine.gfx[2 + 2 * i + 1],
						j,0,
						0,0,
						32 * j,(BGHEIGHT / 2),
						0,TRANSPARENCY_NONE,0);
			}
		}
	
		return 0;
	} };
	
	
	
	/***************************************************************************
	
	  Stop the video hardware emulation.
	
	***************************************************************************/
	public static VhStopPtr mpatrol_vh_stop = new VhStopPtr() { public void handler() 
	{
		bitmap_free(bgbitmap[0]);
		bitmap_free(bgbitmap[1]);
		bitmap_free(bgbitmap[2]);
		generic_vh_stop();
	} };
	
	
	
	public static WriteHandlerPtr mpatrol_scroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		scrollreg[offset] = data;
	} };
	
	
	
	public static WriteHandlerPtr mpatrol_bg1xpos_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		bg1xpos = data;
	} };
	
	
	
	public static WriteHandlerPtr mpatrol_bg1ypos_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		bg1ypos = data;
	} };
	
	
	
	public static WriteHandlerPtr mpatrol_bg2xpos_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		bg2xpos = data;
	} };
	
	
	
	public static WriteHandlerPtr mpatrol_bg2ypos_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		bg2ypos = data;
	} };
	
	
	
	public static WriteHandlerPtr mpatrol_bgcontrol_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		bgcontrol = data;
	} };
	
	
	
	public static WriteHandlerPtr mpatrol_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* screen flip is handled both by software and hardware */
		data ^= ~readinputport(4) & 1;
		flip_screen_w(0,data & 1);
	
		coin_counter_w.handler(0,data & 0x02);
		coin_counter_w.handler(1,data & 0x20);
	} };
	
	
	public static ReadHandlerPtr mpatrol_input_port_3_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int ret = input_port_3_r.handler(0);
	
		/* Based on the coin mode fill in the upper bits */
		if (input_port_4_r.handler(0) & 0x04)
		{
			/* Mode 1 */
			ret	|= (input_port_5_r.handler(0) << 4);
		}
		else
		{
			/* Mode 2 */
			ret	|= (input_port_5_r.handler(0) & 0xf0);
		}
	
		return ret;
	} };
	
	
	
	static void get_clip(struct rectangle *clip, int min_y, int max_y)
	{
		clip.min_x = Machine.visible_area.min_x;
		clip.max_x = Machine.visible_area.max_x;
	
		if (flip_screen != 0)
		{
			clip.min_y = Machine.drv.screen_height - 1 - max_y;
			clip.max_y = Machine.drv.screen_height - 1 - min_y;
		}
		else
		{
			clip.min_y = min_y;
			clip.max_y = max_y;
		}
	}
	
	static void draw_background(struct osd_bitmap *bitmap,
	                            int xpos, int ypos, int ypos_end, int image,
								int transparency)
	{
		struct rectangle clip1, clip2;
	
		get_clip(&clip1, ypos,            ypos + BGHEIGHT - 1);
		get_clip(&clip2, ypos + BGHEIGHT, ypos_end);
	
		if (flip_screen != 0)
		{
			xpos = 256 - xpos;
			ypos = Machine.drv.screen_height - BGHEIGHT - ypos;
		}
		copybitmap(bitmap,bgbitmap[image],flip_screen,flip_screen,xpos,      ypos,&clip1,transparency,128);
		copybitmap(bitmap,bgbitmap[image],flip_screen,flip_screen,xpos - 256,ypos,&clip1,transparency,128);
		fillbitmap(bitmap,Machine.gfx[image*2+2].colortable[3],&clip2);
	}
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr mpatrol_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs,i;
	
	
		if (full_refresh != 0)
			memset(dirtybuffer,1,videoram_size[0]);
	
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = videoram_size[0] - 1;offs >= 0;offs--)
		{
			if (dirtybuffer[offs])
			{
				int sx,sy,color;
	
	
				dirtybuffer[offs] = 0;
	
				sx = offs % 32;
				sy = offs / 32;
	
				color = colorram.read(offs)& 0x1f;
				if (sy >= 7) color += 32;	/* lines 7-31 are transparent */
	
				if (flip_screen != 0)
				{
					sx = 31 - sx;
					sy = 31 - sy;
				}
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram.read(offs)+ 2 * (colorram.read(offs)& 0x80),
						color,
						flip_screen,flip_screen,
						8*sx,8*sy,
						0,TRANSPARENCY_NONE,0);
			}
		}
	
	
		/* copy the background */
		if ((bgcontrol == 0x04) || (bgcontrol == 0x03))
		{
			struct rectangle clip;
	
			get_clip(&clip, 7*8, bg2ypos-1);
			fillbitmap(bitmap,Machine.pens[0],&clip);
	
			draw_background(bitmap, bg2xpos, bg2ypos, bg1ypos + BGHEIGHT - 1, 0, TRANSPARENCY_NONE);
			draw_background(bitmap, bg1xpos, bg1ypos, Machine.visible_area.max_y,
			                (bgcontrol == 0x04) ? 1 : 2, TRANSPARENCY_COLOR);
		}
		else fillbitmap(bitmap,Machine.pens[0],&Machine.visible_area);
	
	
		/* copy the temporary bitmap to the screen */
		{
			int scroll[32];
			struct rectangle clip;
	
	
			clip.min_x = Machine.visible_area.min_x;
			clip.max_x = Machine.visible_area.max_x;
	
			if (flip_screen != 0)
			{
				clip.min_y = 25 * 8;
				clip.max_y = 32 * 8 - 1;
				copybitmap(bitmap,tmpbitmap,0,0,0,0,&clip,TRANSPARENCY_NONE,0);
	
				clip.min_y = 0;
				clip.max_y = 25 * 8 - 1;
	
				for (i = 0;i < 32;i++)
					scroll[31-i] = -scrollreg[i/2];
	
				copyscrollbitmap(bitmap,tmpbitmap,32,scroll,0,0,&clip,TRANSPARENCY_COLOR,0);
			}
			else
			{
				clip.min_y = 0;
				clip.max_y = 7 * 8 - 1;
				copybitmap(bitmap,tmpbitmap,0,0,0,0,&clip,TRANSPARENCY_NONE,0);
	
				clip.min_y = 7 * 8;
				clip.max_y = 32 * 8 - 1;
	
				for (i = 0;i < 32;i++)
					scroll[i] = scrollreg[i/2];
	
				copyscrollbitmap(bitmap,tmpbitmap,32,scroll,0,0,&clip,TRANSPARENCY_COLOR,0);
			}
		}
	
	
		/* Draw the sprites. */
		for (offs = spriteram_size[0] - 4;offs >= 0;offs -= 4)
		{
			int sx,sy,flipx,flipy;
	
	
			sx = spriteram_2.read(offs + 3);
			sy = 241 - spriteram_2.read(offs);
			flipx = spriteram_2.read(offs + 1)& 0x40;
			flipy = spriteram_2.read(offs + 1)& 0x80;
			if (flip_screen != 0)
			{
				flipx = !flipx;
				flipy = !flipy;
				sx = 240 - sx;
				sy = 242 - sy;
			}
	
			drawgfx(bitmap,Machine.gfx[1],
					spriteram_2.read(offs + 2),
					spriteram_2.read(offs + 1)& 0x3f,
					flipx,flipy,
					sx,sy,
					&Machine.visible_area,TRANSPARENCY_COLOR,128+32);
		}
		for (offs = spriteram_size[0] - 4;offs >= 0;offs -= 4)
		{
			int sx,sy,flipx,flipy;
	
			sx = spriteram.read(offs + 3);
			sy = 241 - spriteram.read(offs);
			flipx = spriteram.read(offs + 1)& 0x40;
			flipy = spriteram.read(offs + 1)& 0x80;
			if (flip_screen != 0)
			{
				flipx = !flipx;
				flipy = !flipy;
				sx = 240 - sx;
				sy = 242 - sy;
			}
	
			drawgfx(bitmap,Machine.gfx[1],
					spriteram.read(offs + 2),
					spriteram.read(offs + 1)& 0x3f,
					flipx,flipy,
					sx,sy,
					&Machine.visible_area,TRANSPARENCY_COLOR,128+32);
		}
	} };
}
