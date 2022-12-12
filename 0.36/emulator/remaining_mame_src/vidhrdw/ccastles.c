/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/
#include "driver.h"
#include "vidhrdw/generic.h"


static struct osd_bitmap *sprite_bm;
static struct osd_bitmap *maskbitmap;

static int flipscreen;
static int screen_flipped;

unsigned char *ccastles_screen_addr;
unsigned char *ccastles_screen_inc;
unsigned char *ccastles_screen_inc_enable;
unsigned char *ccastles_sprite_bank;
unsigned char *ccastles_scrollx;
unsigned char *ccastles_scrolly;

/***************************************************************************

  Convert the color PROMs into a more useable format.

  Crystal Castles doesn't have a color PROM. It uses RAM to dynamically
  create the palette. The resolution is 9 bit (3 bits per gun). The palette
  contains 32 entries, but it is accessed through a memory windows 64 bytes
  long: writing to the first 32 bytes sets the msb of the red component to 0,
  while writing to the last 32 bytes sets it to 1.
  The first 16 entries are used for sprites; the last 16 for the background
  bitmap.

  I don't know the exact values of the resistors between the RAM and the
  RGB output, I assumed the usual ones.
  bit 8 -- inverter -- 220 ohm resistor  -- RED
  bit 7 -- inverter -- 470 ohm resistor  -- RED
        -- inverter -- 1  kohm resistor  -- RED
        -- inverter -- 220 ohm resistor  -- BLUE
        -- inverter -- 470 ohm resistor  -- BLUE
        -- inverter -- 1  kohm resistor  -- BLUE
        -- inverter -- 220 ohm resistor  -- GREEN
        -- inverter -- 470 ohm resistor  -- GREEN
  bit 0 -- inverter -- 1  kohm resistor  -- GREEN

***************************************************************************/
void ccastles_paletteram_w(int offset,int data)
{
	int r,g,b;
	int bit0,bit1,bit2;


	r = (data & 0xC0) >> 6;
	b = (data & 0x38) >> 3;
	g = (data & 0x07);
	/* a write to offset 32-63 means to set the msb of the red component */
	if (offset & 0x20) r += 4;

	/* bits are inverted */
	r = 7-r;
	g = 7-g;
	b = 7-b;

	bit0 = (r >> 0) & 0x01;
	bit1 = (r >> 1) & 0x01;
	bit2 = (r >> 2) & 0x01;
	r = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	bit0 = (g >> 0) & 0x01;
	bit1 = (g >> 1) & 0x01;
	bit2 = (g >> 2) & 0x01;
	g = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	bit0 = (b >> 0) & 0x01;
	bit1 = (b >> 1) & 0x01;
	bit2 = (b >> 2) & 0x01;
	b = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;

	palette_change_color(offset & 0x1f,r,g,b);
}



/***************************************************************************

  Start the video hardware emulation.

***************************************************************************/
int ccastles_vh_start(void)
{
	if ((tmpbitmap = osd_create_bitmap(Machine->drv->screen_width,Machine->drv->screen_height)) == 0)
		return 1;

	if ((maskbitmap = osd_create_bitmap(Machine->drv->screen_width,Machine->drv->screen_height)) == 0)
	{
		osd_free_bitmap(tmpbitmap);
		return 1;
	}

	if ((sprite_bm = osd_create_bitmap(16,16)) == 0)
	{
		osd_free_bitmap(maskbitmap);
		osd_free_bitmap(tmpbitmap);
		return 1;
	}

	return 0;
}



/***************************************************************************

  Stop the video hardware emulation.

***************************************************************************/
void ccastles_vh_stop(void)
{
	osd_free_bitmap(sprite_bm);
	osd_free_bitmap(maskbitmap);
	osd_free_bitmap(tmpbitmap);
}



int ccastles_bitmode_r(int offset)
{
	int addr;

	addr = (ccastles_screen_addr[1]<<7) | (ccastles_screen_addr[0]>>1);

	/* is the address in videoram? */
	if ((addr >= 0x0c00) && (addr < 0x8000))
	{
		/* auto increment in the x-direction if it's enabled */
		if (!ccastles_screen_inc_enable[0])
		{
			if (!ccastles_screen_inc[0])
				ccastles_screen_addr[0] ++;
			else
				ccastles_screen_addr[0] --;
		}

		/* auto increment in the y-direction if it's enabled */
		if (!ccastles_screen_inc_enable[1])
		{
			if (!ccastles_screen_inc[1])
				ccastles_screen_addr[1] ++;
			else
				ccastles_screen_addr[1] --;
		}

		addr -= 0xc00;
		if (ccastles_screen_addr[0] & 0x01)
			return ((videoram[addr] & 0x0f) << 4);
		else
			return (videoram[addr] & 0xf0);
	}

	return 0;
}

void ccastles_bitmode_w(int offset, int data)
{
	int addr;


	addr = (ccastles_screen_addr[1] << 7) | (ccastles_screen_addr[0] >> 1);

	/* is the address in videoram? */
	if ((addr >= 0x0c00) && (addr < 0x8000))
	{
		int x,y,j;
		int mode;

		addr -= 0xc00;

		if (ccastles_screen_addr[0] & 0x01)
		{
			mode = (data >> 4) & 0x0f;
			videoram[addr] = (videoram[addr] & 0xf0) | mode;
		}
		else
		{
			mode = (data & 0xf0);
			videoram[addr] = (videoram[addr] & 0x0f) | mode;
		}

		j = 2*addr;
		x = j%256;
		y = j/256;
		if (!flipscreen)
		{
			plot_pixel(tmpbitmap, x  , y, Machine->pens[16 + ((videoram[addr] & 0xf0) >> 4)]);
			plot_pixel(tmpbitmap, x+1, y, Machine->pens[16 +  (videoram[addr] & 0x0f)      ]);

			/* if bit 3 of the pixel is set, background has priority over sprites when */
			/* the sprite has the priority bit set. We use a second bitmap to remember */
			/* which pixels have priority. */
			plot_pixel(maskbitmap, x  , y, videoram[addr] & 0x80);
			plot_pixel(maskbitmap, x+1, y, videoram[addr] & 0x08);
		}
		else
		{
			y = 231-y;
			x = 254-x;
			if (y >= 0)
			{
				plot_pixel(tmpbitmap, x+1, y, Machine->pens[16 + ((videoram[addr] & 0xf0) >> 4)]);
				plot_pixel(tmpbitmap, x  , y, Machine->pens[16 +  (videoram[addr] & 0x0f)      ]);

				/* if bit 3 of the pixel is set, background has priority over sprites when */
				/* the sprite has the priority bit set. We use a second bitmap to remember */
				/* which pixels have priority. */
				plot_pixel(maskbitmap, x+1, y, videoram[addr] & 0x80);
				plot_pixel(maskbitmap, x  , y, videoram[addr] & 0x08);
			}
		}
	}

	/* auto increment in the x-direction if it's enabled */
	if (!ccastles_screen_inc_enable[0])
	{
		if (!ccastles_screen_inc[0])
			ccastles_screen_addr[0] ++;
		else
			ccastles_screen_addr[0] --;
	}

	/* auto increment in the y-direction if it's enabled */
	if (!ccastles_screen_inc_enable[1])
	{
		if (!ccastles_screen_inc[1])
			ccastles_screen_addr[1] ++;
		else
			ccastles_screen_addr[1] --;
	}

}

void ccastles_flipscreen_w (int offset,int data)
{
	if (flipscreen != (data & 1))
	{
		flipscreen = data & 1;

		screen_flipped = 1;
	}
}



/***************************************************************************

  Draw the game screen in the given osd_bitmap.
  Do NOT call osd_update_display() from this function, it will be called by
  the main emulation engine.

***************************************************************************/
static void redraw_bitmap(void)
{
	int x, y;
	int screen_addr0_save, screen_addr1_save, screen_inc_enable0_save, screen_inc_enable1_save;


	/* save out registers */
	screen_addr0_save = ccastles_screen_addr[0];
	screen_addr1_save = ccastles_screen_addr[1];

	screen_inc_enable0_save = ccastles_screen_inc_enable[0];
	screen_inc_enable1_save = ccastles_screen_inc_enable[1];

	ccastles_screen_inc_enable[0] = ccastles_screen_inc_enable[1] = 1;


	/* redraw bitmap */
	for (y = 0; y < 256; y++)
	{
		ccastles_screen_addr[1] = y;

		for (x = 0; x < 256; x++)
		{
			ccastles_screen_addr[0] = x;

			ccastles_bitmode_w(0, ccastles_bitmode_r(0));
		}
	}


	/* restore registers */
	ccastles_screen_addr[0] = screen_addr0_save;
	ccastles_screen_addr[1] = screen_addr1_save;

	ccastles_screen_inc_enable[0] = screen_inc_enable0_save;
	ccastles_screen_inc_enable[1] = screen_inc_enable1_save;
}


void ccastles_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;
	unsigned char *spriteaddr;
	int scrollx,scrolly;


	if (palette_recalc() || screen_flipped)
	{
		redraw_bitmap();
		screen_flipped = 0;
	}


	scrollx = 255 - *ccastles_scrollx;
	scrolly = 255 - *ccastles_scrolly;

	if (flipscreen)
	{
		scrollx = 254 - scrollx;
		scrolly = 231 - scrolly;
	}

	copyscrollbitmap(bitmap,tmpbitmap,1,&scrollx,1,&scrolly,
				     &Machine->drv->visible_area,
		   			 TRANSPARENCY_NONE,0);


	if (*ccastles_sprite_bank)
		spriteaddr = spriteram;
	else
		spriteaddr = spriteram_2;


	/* Draw the sprites */
	for (offs = 0; offs < spriteram_size; offs += 4)
	{
		int i,j;
		int x,y;

		/* Get the X and Y coordinates from the MOB RAM */
		x = spriteaddr[offs+3];
		y = 216 - spriteaddr[offs+1];

		if (spriteaddr[offs+2] & 0x80)	/* background can have priority over the sprite */
		{
			fillbitmap(sprite_bm,Machine->gfx[0]->colortable[7],0);
			drawgfx(sprite_bm,Machine->gfx[0],
					spriteaddr[offs],1,
					flipscreen,flipscreen,
					0,0,
					0,TRANSPARENCY_PEN,7);

			for (j = 0;j < 16;j++)
			{
				if (y + j >= 0)	/* avoid accesses out of the bitmap boundaries */
				{
					for (i = 0;i < 8;i++)
					{
						int pixa,pixb;

						pixa = read_pixel(sprite_bm, i, j);
						pixb = read_pixel(maskbitmap, (x+scrollx+i)%256, (y+scrolly+j)%232);

						/* if background has priority over sprite, make the */
						/* temporary bitmap transparent */
						if (pixb != 0 && (pixa != Machine->gfx[0]->colortable[0]))
							plot_pixel(sprite_bm, i, j, Machine->gfx[0]->colortable[7]);
					}
				}
			}

			copybitmap(bitmap,sprite_bm,0,0,x,y,&Machine->drv->visible_area,TRANSPARENCY_PEN,Machine->gfx[0]->colortable[7]);
		}
		else
		{
			drawgfx(bitmap,Machine->gfx[0],
					spriteaddr[offs],1,
					flipscreen,flipscreen,
					x,y,
					&Machine->drv->visible_area,TRANSPARENCY_PEN,7);
		}
	}
}
