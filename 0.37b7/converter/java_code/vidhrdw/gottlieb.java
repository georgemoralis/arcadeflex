/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class gottlieb
{
	
	
	
	UBytePtr gottlieb_characterram;
	#define MAX_CHARS 256
	static UBytePtr dirtycharacter;
	static int background_priority=0;
	static unsigned char hflip=0;
	static unsigned char vflip=0;
	static int spritebank;
	
	
	
	/***************************************************************************
	
	  Gottlieb games dosn't have a color PROM. They use 32 bytes of RAM to
	  dynamically create the palette. Each couple of bytes defines one
	  color (4 bits per pixel; the high 4 bits of the second byte are unused).
	
	  The RAM is conected to the RGB output this way:
	
	  bit 7 -- 240 ohm resistor  -- GREEN
	        -- 470 ohm resistor  -- GREEN
	        -- 1  kohm resistor  -- GREEN
	        -- 2  kohm resistor  -- GREEN
	        -- 240 ohm resistor  -- RED
	        -- 470 ohm resistor  -- RED
	        -- 1  kohm resistor  -- RED
	  bit 0 -- 2  kohm resistor  -- RED
	
	  bit 7 -- unused
	        -- unused
	        -- unused
	        -- unused
	        -- 240 ohm resistor  -- BLUE
	        -- 470 ohm resistor  -- BLUE
	        -- 1  kohm resistor  -- BLUE
	  bit 0 -- 2  kohm resistor  -- BLUE
	
	***************************************************************************/
	public static WriteHandlerPtr gottlieb_paletteram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int bit0,bit1,bit2,bit3;
		int r,g,b,val;
	
	
		paletteram.write(offset,data);

		/* red component */
		val = paletteram.read(offset | 1);
		bit0 = (val >> 0) & 0x01;
		bit1 = (val >> 1) & 0x01;
		bit2 = (val >> 2) & 0x01;
		bit3 = (val >> 3) & 0x01;
		r = 0x10 * bit0 + 0x21 * bit1 + 0x46 * bit2 + 0x88 * bit3;
	
		/* green component */
		val = paletteram.read(offset & ~1);
		bit0 = (val >> 4) & 0x01;
		bit1 = (val >> 5) & 0x01;
		bit2 = (val >> 6) & 0x01;
		bit3 = (val >> 7) & 0x01;
		g = 0x10 * bit0 + 0x21 * bit1 + 0x46 * bit2 + 0x88 * bit3;
	
		/* blue component */
		val = paletteram.read(offset & ~1);
		bit0 = (val >> 0) & 0x01;
		bit1 = (val >> 1) & 0x01;
		bit2 = (val >> 2) & 0x01;
		bit3 = (val >> 3) & 0x01;
		b = 0x10 * bit0 + 0x21 * bit1 + 0x46 * bit2 + 0x88 * bit3;
	
		palette_change_color(offset / 2,r,g,b);
	} };
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr gottlieb_vh_start = new VhStartPtr() { public int handler() 
	{
		if (generic_vh_start() != 0)
			return 1;
	
		if ((dirtycharacter = malloc(MAX_CHARS)) == 0)
		{
			generic_vh_stop();
			return 1;
		}
		/* Some games have character gfx data in ROM, some others in RAM. We don't */
		/* want to recalculate chars if data is in ROM, so let's start with the array */
		/* initialized to 0. */
		memset(dirtycharacter,0,MAX_CHARS);
	
		return 0;
	} };
	
	/***************************************************************************
	
	  Stop the video hardware emulation.
	
	***************************************************************************/
	public static VhStopPtr gottlieb_vh_stop = new VhStopPtr() { public void handler() 
	{
		free(dirtycharacter);
		generic_vh_stop();
	} };
	
	
	
	public static WriteHandlerPtr gottlieb_video_outputs_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		extern 	static int last = 0;
	
	
		background_priority = data & 1;
	
		hflip = data & 2;
		vflip = data & 4;
		if ((data & 6) != (last & 6))
			memset(dirtybuffer,1,videoram_size[0]);
	
		/* in Q*Bert Qubes only, bit 4 controls the sprite bank */
		spritebank = (data & 0x10) >> 4;
	
		if ((last&0x20) && !(data&0x20)) gottlieb_knocker();
	
		last = data;
	} };
	
	public static WriteHandlerPtr usvsthem_video_outputs_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		background_priority = data & 1;
	
		/* in most games, bits 1 and 2 flip screen, however in the laser */
		/* disc games they are different. */
	
		/* bit 1 controls the sprite bank. */
		spritebank = (data & 0x02) >> 1;
	
		/* bit 2 video enable (0 = black screen) */
	
		/* bit 3 genlock control (1 = show laserdisc image) */
	} };
	
	
	
	public static WriteHandlerPtr gottlieb_characterram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (gottlieb_characterram[offset] != data)
		{
			dirtycharacter[offset / 32] = 1;
			gottlieb_characterram[offset] = data;
		}
	} };
	
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr gottlieb_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
	    int offs;
	
	
		/* update palette */
		if (palette_recalc())
			memset(dirtybuffer, 1, videoram_size[0]);
	
	    /* recompute character graphics */
	    for (offs = 0;offs < Machine.drv.gfxdecodeinfo[0].gfxlayout.total;offs++)
		{
			if (dirtycharacter[offs])
				decodechar(Machine.gfx[0],offs,gottlieb_characterram,Machine.drv.gfxdecodeinfo[0].gfxlayout);
		}
	
	
	    /* for every character in the Video RAM, check if it has been modified */
	    /* since last time and update it accordingly. */
	    for (offs = videoram_size[0] - 1;offs >= 0;offs--)
		{
			if (dirtybuffer[offs] || dirtycharacter[videoram.read(offs)])
			{
				int sx,sy;
	
	
				dirtybuffer[offs] = 0;
	
				sx = offs % 32;
				sy = offs / 32;
				if (hflip != 0) sx = 31 - sx;
				if (vflip != 0) sy = 29 - sy;
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram.read(offs),
						0,
						hflip,vflip,
						8*sx,8*sy,
						&Machine.visible_area,TRANSPARENCY_NONE,0);
			}
	    }
	
		memset(dirtycharacter,0,MAX_CHARS);
	
	
		/* copy the character mapped graphics */
		copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine.visible_area,TRANSPARENCY_NONE,0);
	
	
		/* Draw the sprites. Note that it is important to draw them exactly in this */
		/* order, to have the correct priorities. */
	    for (offs = 0;offs < spriteram_size[0] - 8;offs += 4)     /* it seems there's something strange with sprites #62 and #63 */
		{
		    int sx,sy;
	
	
			/* coordinates hand tuned to make the position correct in Q*Bert Qubes start */
			/* of level animation. */
			sx = (spriteram.read(offs + 1)) - 4;
			if (hflip != 0) sx = 233 - sx;
			sy = (spriteram.read(offs)) - 13;
			if (vflip != 0) sy = 228 - sy;
	
			if (spriteram.read(offs)|| spriteram.read(offs + 1))	/* needed to avoid garbage on screen */
				drawgfx(bitmap,Machine.gfx[1],
						(255 ^ spriteram.read(offs + 2)) + 256 * spritebank,
						0,
						hflip,vflip,
						sx,sy,
						&Machine.visible_area,
						background_priority ? TRANSPARENCY_THROUGH : TRANSPARENCY_PEN,
						background_priority ? Machine.pens[0]     : 0);
		}
	} };
}
