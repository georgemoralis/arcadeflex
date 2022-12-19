/***************************************************************************

  Poly-Play
  (c) 1985 by VEB Polytechnik Karl-Marx-Stadt

  video hardware

  driver written by Martin Buchholz (buchholz@mail.uni-greifswald.de)

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class polyplay
{
	
	UBytePtr polyplay_characterram;
	static unsigned char dirtycharacter[256];
	
	static int palette_bank;
	
	
	public static VhConvertColorPromPtr polyplay_init_palette = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
	
		static unsigned char polyplay_palette[] =
		{
			0x00,0x00,0x00,
			0xff,0xff,0xff,
	
			0x00,0x00,0x00,
			0xff,0x00,0x00,
			0x00,0xff,0x00,
			0xff,0xff,0x00,
			0x00,0x00,0xff,
			0xff,0x00,0xff,
			0x00,0xff,0xff,
			0xff,0xff,0xff,
		};
	
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
	
			/* red component */
			*(palette++) = polyplay_palette[3*i];
	
			/* green component */
			*(palette++) = polyplay_palette[3*i+1];
	
			/* blue component */
			*(palette++) = polyplay_palette[3*i+2];
	
		}
	
		palette_bank = 0;
	
	} };
	
	
	public static WriteHandlerPtr polyplay_characterram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (polyplay_characterram[offset] != data)
		{
			dirtycharacter[((offset / 8) & 0x7f) + 0x80] = 1;
	
			polyplay_characterram[offset] = data;
		}
	} };
	
	public static ReadHandlerPtr polyplay_characterram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return polyplay_characterram[offset];
	} };
	
	
	public static VhUpdatePtr polyplay_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
	
	
		if (full_refresh != 0)
		{
			memset(dirtybuffer,1,videoram_size[0]);
		}
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = videoram_size[0] - 1;offs >= 0;offs--)
		{
			int charcode;
	
	
			charcode = videoram.read(offs);
	
			if (dirtybuffer[offs] || dirtycharacter[charcode])
			{
				int sx,sy;
	
	
				/* index=0 . 1 bit chr; index=1 . 3 bit chr */
				if (charcode < 0x80) {
	
					/* ROM chr, no need for decoding */
	
					dirtybuffer[offs] = 0;
	
					sx = offs % 64;
					sy = offs / 64;
	
					drawgfx(bitmap,Machine.gfx[0],
							charcode,
							0x0,
							0,0,
							8*sx,8*sy,
							&Machine.visible_area,TRANSPARENCY_NONE,0);
	
				}
				else {
					/* decode modified characters */
					if (dirtycharacter[charcode] == 1)
					{
						decodechar(Machine.gfx[1],charcode-0x80,polyplay_characterram,Machine.drv.gfxdecodeinfo[1].gfxlayout);
						dirtycharacter[charcode] = 2;
					}
	
	
					dirtybuffer[offs] = 0;
	
					sx = offs % 64;
					sy = offs / 64;
	
					drawgfx(bitmap,Machine.gfx[1],
							charcode,
							0x0,
							0,0,
							8*sx,8*sy,
							&Machine.visible_area,TRANSPARENCY_NONE,0);
	
				}
			}
		}
	
	
		for (offs = 0;offs < 256;offs++)
		{
			if (dirtycharacter[offs] == 2) dirtycharacter[offs] = 0;
		}
	} };
}
