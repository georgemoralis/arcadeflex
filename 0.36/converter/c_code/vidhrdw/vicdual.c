/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"



unsigned char *vicdual_characterram;
static unsigned char dirtycharacter[256];

static int palette_bank;



/***************************************************************************

  Convert the color PROMs into a more useable format.

  The VIC dual game board has one 32x8 palette PROM. The color code is taken
  from the three most significant bits of the character code, plus two
  additional palette bank bits.
  The palette PROM is connected to the RGB output this way:

  bit 7 -- 22 ohm resistor  -- RED   \
        -- 22 ohm resistor  -- BLUE  |  foreground
        -- 22 ohm resistor  -- GREEN /
        -- Unused
        -- 22 ohm resistor  -- RED   \
        -- 22 ohm resistor  -- BLUE  |  background
        -- 22 ohm resistor  -- GREEN /
  bit 0 -- Unused

***************************************************************************/
void vicdual_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom)
{
	int i;
	/* for b&w games we'll use the Head On PROM */
	static unsigned char bw_color_prom[] =
	{
		/* for b/w games, let's use the Head On PROM */
		0xE1,0xE1,0xE1,0xE1,0xE1,0xE1,0xE1,0xE1,0xE1,0xE1,0xE1,0xE1,0xE1,0xE1,0xE1,0xE1,
		0xE1,0xE1,0xE1,0xE1,0xE1,0xE1,0xE1,0xE1,0xE1,0xE1,0xE1,0xE1,0xE1,0xE1,0xE1,0xE1,
	};


	if (color_prom == 0) color_prom = bw_color_prom;

	for (i = 0;i < Machine->drv->total_colors / 2;i++)
	{
		int bit;


		/* background red component */
		bit = (*color_prom >> 3) & 0x01;
		*(palette++) = 0xff * bit;
		/* background green component */
		bit = (*color_prom >> 1) & 0x01;
		*(palette++) = 0xff * bit;
		/* background blue component */
		bit = (*color_prom >> 2) & 0x01;
		*(palette++) = 0xff * bit;

		/* foreground red component */
		bit = (*color_prom >> 7) & 0x01;
		*(palette++) = 0xff * bit;
		/* foreground green component */
		bit = (*color_prom >> 5) & 0x01;
		*(palette++) = 0xff * bit;
		/* foreground blue component */
		bit = (*color_prom >> 6) & 0x01;
		*(palette++) = 0xff * bit;

		color_prom++;
	}

	palette_bank = 0;

	{
		extern struct GameDriver driver_heiankyo;
		extern struct GameDriver driver_invinco;
		extern struct GameDriver driver_digger;
		extern struct GameDriver driver_tranqgun;

		/* Heiankyo Alien doesn't write to port 0x40, it expects it to default to 3 */
		if (Machine->gamedrv == &driver_heiankyo)
			palette_bank = 3;

		/* and many others expect it to default to 1 */
		if (Machine->gamedrv == &driver_invinco ||
				Machine->gamedrv == &driver_digger ||
				Machine->gamedrv == &driver_tranqgun)
			palette_bank = 1;
	}
}



void vicdual_characterram_w(int offset,int data)
{
	if (vicdual_characterram[offset] != data)
	{
		dirtycharacter[(offset / 8) & 0xff] = 1;

		vicdual_characterram[offset] = data;
	}
}

int vicdual_characterram_r(int offset)
{
	return vicdual_characterram[offset];
}

void vicdual_palette_bank_w(int offset, int data)
{
	if (palette_bank != (data & 3))
	{
		palette_bank = data & 3;
		memset(dirtybuffer,1,videoram_size);
	}
}



/***************************************************************************

  Draw the game screen in the given osd_bitmap.
  Do NOT call osd_update_display() from this function, it will be called by
  the main emulation engine.

***************************************************************************/
void vicdual_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;


	if (full_refresh)
	{
		memset(dirtybuffer,1,videoram_size);
	}

	/* for every character in the Video RAM, check if it has been modified */
	/* since last time and update it accordingly. */
	for (offs = videoram_size - 1;offs >= 0;offs--)
	{
		int charcode;


		charcode = videoram[offs];

		if (dirtybuffer[offs] || dirtycharacter[charcode])
		{
			int sx,sy;


			/* decode modified characters */
			if (dirtycharacter[charcode] == 1)
			{
				decodechar(Machine->gfx[0],charcode,vicdual_characterram,Machine->drv->gfxdecodeinfo[0].gfxlayout);
				dirtycharacter[charcode] = 2;
			}


			dirtybuffer[offs] = 0;

			sx = offs % 32;
			sy = offs / 32;

			drawgfx(bitmap,Machine->gfx[0],
					charcode,
					(charcode >> 5) + 8 * palette_bank,
					0,0,
					8*sx,8*sy,
					&Machine->drv->visible_area,TRANSPARENCY_NONE,0);

		}
	}


	for (offs = 0;offs < 256;offs++)
	{
		if (dirtycharacter[offs] == 2) dirtycharacter[offs] = 0;
	}
}
