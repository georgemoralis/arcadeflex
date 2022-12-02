/***************************************************************************

 COSMIC.C

 emulation of video hardware of cosmic machines of 1979-1980(ish)

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"


static int refresh_tmpbitmap;
static int flipscreen;
static int (*map_color)(int x, int y);

static int color_registers[3];
static int color_base = 0;
static int nomnlnd_background_on=0;


/* No Mans Land - I don't know if there are more screen layouts than this */
/* this one seems to be OK for the start of the game       */

static const signed short nomnlnd_tree_positions[2][2] =
{
	{66,63},{66,159}
};

static const signed short nomnlnd_water_positions[4][2] =
{
	{160,32},{160,96},{160,128},{160,192}
};


void panic_color_register_w(int offset,int data)
{
	/* 7c0c & 7c0e = Rom Address Offset
 	   7c0d        = high / low nibble */

	if (color_registers[offset] != (data & 0x80))
    {
    	color_registers[offset] = data & 0x80;
    	color_base = (color_registers[0] << 2) + (color_registers[2] << 3);
    	refresh_tmpbitmap = 1;
    }
}

void cosmicg_color_register_w(int offset,int data)
{
	if (color_registers[offset] != data)
    {
    	color_registers[offset] = data;

        color_base = 0;

        if (color_registers[0] == 1) color_base += 0x100;
        if (color_registers[1] == 1) color_base += 0x200;

		refresh_tmpbitmap = 1;
    }
}


static int panic_map_color(int x, int y)
{
	/* 8 x 16 coloring */
	unsigned char byte = memory_region(REGION_USER1)[color_base + (x / 16) * 32 + (y / 8)];

	if (color_registers[1])
		return byte >> 4;
	else
		return byte & 0x0f;
}

static int cosmicg_map_color(int x, int y)
{
	unsigned char byte;

	/* 16 x 16 coloring */
	//if ((x >= 224) && (x < 232))
	//	byte = color_rom[color_base + (y / 16) * 16 + 9];
	//else
		byte = memory_region(REGION_USER1)[color_base + (y / 16) * 16 + (x / 16)];

	/* the upper 4 bits are for cocktail mode support */

	return byte & 0x0f;
}

static int magspot2_map_color(int x, int y)
{
	unsigned char byte;

	/* 16 x 8 coloring */

	// Should the top line of the logo be red or white???

	//if ((x >= 216) && (x < 224))
	//	byte = memory_region(REGION_USER1)[26 * 16 + (y / 16)];
	//else
		byte = memory_region(REGION_USER1)[(x / 8) * 16 + (y / 16)];

	if (color_registers[1])
		return byte >> 4;
	else
		return byte & 0x0f;
}


static const unsigned char panic_remap_sprite_code[64][2] =
{
{0x00,0},{0x00,0},{0x00,0},{0x00,0},{0x00,0},{0x00,0},{0x00,0},{0x00,0}, /* 00 */
{0x00,0},{0x26,0},{0x25,0},{0x24,0},{0x23,0},{0x22,0},{0x21,0},{0x20,0}, /* 08 */
{0x00,0},{0x00,0},{0x00,0},{0x00,0},{0x00,0},{0x00,0},{0x00,0},{0x00,0}, /* 10 */
{0x00,0},{0x16,0},{0x15,0},{0x14,0},{0x13,0},{0x12,0},{0x11,0},{0x10,0}, /* 18 */
{0x00,0},{0x00,0},{0x00,0},{0x00,0},{0x00,0},{0x00,0},{0x00,0},{0x00,0}, /* 20 */
{0x00,0},{0x06,0},{0x05,0},{0x04,0},{0x03,0},{0x02,0},{0x01,0},{0x00,0}, /* 28 */
{0x00,0},{0x00,0},{0x00,0},{0x00,0},{0x00,0},{0x00,0},{0x00,0},{0x00,0}, /* 30 */
{0x07,2},{0x06,2},{0x05,2},{0x04,2},{0x03,2},{0x02,2},{0x01,2},{0x00,2}, /* 38 */
};

/*
 * Panic Color table setup
 *
 * Bit 0 = RED, Bit 1 = GREEN, Bit 2 = BLUE
 *
 * First 8 colors are normal intensities
 *
 * But, bit 3 can be used to pull Blue via a 2k resistor to 5v
 * (1k to ground) so second version of table has blue set to 2/3
 *
 */

void panic_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom)
{
	int i;
	#define TOTAL_COLORS(gfxn) (Machine->gfx[gfxn]->total_colors * Machine->gfx[gfxn]->color_granularity)
	#define COLOR(gfxn,offs) (colortable[Machine->drv->gfxdecodeinfo[gfxn].color_codes_start + offs])

	for (i = 0;i < Machine->drv->total_colors;i++)
	{
		*(palette++) = 0xff * ((i >> 0) & 1);
		*(palette++) = 0xff * ((i >> 1) & 1);
		if ((i & 0x0c) == 0x08)
			*(palette++) = 0xaa;
		else
			*(palette++) = 0xff * ((i >> 2) & 1);
	}


	for (i = 0;i < TOTAL_COLORS(0);i++)
		COLOR(0,i) = *(color_prom++) & 0x0f;


    map_color = panic_map_color;
}


/*
 * Cosmic Alien Color table setup
 *
 * 8 colors, 16 sprite color codes
 *
 * Bit 0 = RED, Bit 1 = GREEN, Bit 2 = BLUE
 *
 */

void cosmica_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom)
{
	int i;

	#define TOTAL_COLORS(gfxn) (Machine->gfx[gfxn]->total_colors * Machine->gfx[gfxn]->color_granularity)
	#define COLOR(gfxn,offs) (colortable[Machine->drv->gfxdecodeinfo[gfxn].color_codes_start + offs])

	for (i = 0;i < Machine->drv->total_colors;i++)
	{
		*(palette++) = 0xff * ((i >> 0) & 1);
		*(palette++) = 0xff * ((i >> 1) & 1);
		*(palette++) = 0xff * ((i >> 2) & 1);
	}


	for (i = 0;i < TOTAL_COLORS(0)/2;i++)
	{
		COLOR(0,i)                     =  * color_prom          & 0x07;
		COLOR(0,i+(TOTAL_COLORS(0)/2)) = (*(color_prom++) >> 4) & 0x07;
	}


    map_color = panic_map_color;
}


/*
 * Cosmic guerilla table setup
 *
 * Use AA for normal, FF for Full Red
 * Bit 0 = R, bit 1 = G, bit 2 = B, bit 4 = High Red
 *
 * It's possible that the background is dark gray and not black, as the
 * resistor chain would never drop to zero, Anybody know ?
 *
 */

void cosmicg_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom)
{
	int i;

	#define TOTAL_COLORS(gfxn) (Machine->gfx[gfxn]->total_colors * Machine->gfx[gfxn]->color_granularity)
	#define COLOR(gfxn,offs) (colortable[Machine->drv->gfxdecodeinfo[gfxn].color_codes_start + offs])

	for (i = 0;i < Machine->drv->total_colors;i++)
	{
    	if (i > 8) *(palette++) = 0xff;
        else *(palette++) = 0xaa * ((i >> 0) & 1);

		*(palette++) = 0xaa * ((i >> 1) & 1);
		*(palette++) = 0xaa * ((i >> 2) & 1);
	}


    map_color = cosmicg_map_color;
}

/**************************************************/
/* Magical Spot 2/Devil Zone specific routines    */
/*												  */
/* 16 colors, 8 sprite color codes				  */
/**************************************************/

void magspot2_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom)
{
	int i;
	#define TOTAL_COLORS(gfxn) (Machine->gfx[gfxn]->total_colors * Machine->gfx[gfxn]->color_granularity)
	#define COLOR(gfxn,offs) (colortable[Machine->drv->gfxdecodeinfo[gfxn].color_codes_start + offs])


	for (i = 0;i < Machine->drv->total_colors;i++)
	{
		if ((i & 0x09) == 0x08)
			*(palette++) = 0xaa;
	 	else
			*(palette++) = 0xff * ((i >> 0) & 1);

		*(palette++) = 0xff * ((i >> 1) & 1);
		*(palette++) = 0xff * ((i >> 2) & 1);
	}


	for (i = 0;i < TOTAL_COLORS(0);i++)
	{
		COLOR(0,i) = *(color_prom++) & 0x0f;
	}


    map_color = magspot2_map_color;
}


void nomnlnd_background_w(int offset, int data)
{
	nomnlnd_background_on = data;
}


void cosmica_flipscreen_w(int offset, int data)
{
	if (data != flipscreen)
	{
		flipscreen = data;

		refresh_tmpbitmap = 1;
	}
}


void cosmica_videoram_w(int offset,int data)
{
    int i,x,y,col;

    videoram[offset] = data;

	y = offset / 32;
	x = 8 * (offset % 32);

    col = Machine->pens[map_color(x, y)];

    for (i = 0; i < 8; i++)
    {
		if (flipscreen)
			plot_pixel(tmpbitmap, 255-x, 255-y, (data & 0x80) ? col : Machine->pens[0]);
		else
			plot_pixel(tmpbitmap,     x,     y, (data & 0x80) ? col : Machine->pens[0]);

	    x++;
	    data <<= 1;
    }
}


void cosmicg_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	if (refresh_tmpbitmap)
	{
		int offs;

		for (offs = 0; offs < videoram_size; offs++)
		{
			cosmica_videoram_w(offs, videoram[offs]);
		}

		refresh_tmpbitmap = 0;
	}

	copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);
}


void panic_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;


	cosmicg_vh_screenrefresh(bitmap, full_refresh);


    /* draw the sprites */

	for (offs = spriteram_size - 4;offs >= 0;offs -= 4)
	{
		if (spriteram[offs] != 0)
		{
			int code,bank,flipy;

			/* panic_remap_sprite_code sprite number to my layout */

			code = panic_remap_sprite_code[(spriteram[offs] & 0x3F)][0];
			bank = panic_remap_sprite_code[(spriteram[offs] & 0x3F)][1];
			flipy = spriteram[offs] & 0x40;

			/* Switch Bank */

			if(spriteram[offs+3] & 0x08) bank=1;

			if (flipscreen)
			{
				flipy = !flipy;
			}

			drawgfx(bitmap,Machine->gfx[bank],
					code,
					7 - (spriteram[offs+3] & 0x07),
					flipscreen,flipy,
					256-spriteram[offs+2],spriteram[offs+1],
					&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
		}
	}
}


void cosmica_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;


	cosmicg_vh_screenrefresh(bitmap, full_refresh);


    /* draw the sprites */

	for (offs = spriteram_size - 4;offs >= 0;offs -= 4)
	{
		if (spriteram[offs] != 0)
        {
			int code, color;

			code  = ~spriteram[offs  ] & 0x3f;
			color = ~spriteram[offs+3] & 0x0f;

            if (spriteram[offs] & 0x80)
            {
                /* 16x16 sprite */

			    drawgfx(bitmap,Machine->gfx[0],
					    code,
					    color,
					    0,0,
				    	256-spriteram[offs+2],spriteram[offs+1],
				        &Machine->drv->visible_area,TRANSPARENCY_PEN,0);
            }
            else
            {
                /* 32x32 sprite */

			    drawgfx(bitmap,Machine->gfx[1],
					    code >> 2,
					    color,
					    0,0,
				    	256-spriteram[offs+2],spriteram[offs+1],
				        &Machine->drv->visible_area,TRANSPARENCY_PEN,0);
            }
        }
	}
}


void magspot2_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;


	cosmicg_vh_screenrefresh(bitmap, full_refresh);


    /* draw the sprites */

	for (offs = spriteram_size - 4;offs >= 0;offs -= 4)
	{
		if (spriteram[offs] != 0)
        {
			int code, color;

			code  = ~spriteram[offs  ] & 0x3f;
			color = ~spriteram[offs+3] & 0x07;

            if (spriteram[offs] & 0x80)
            {
                /* 16x16 sprite */

			    drawgfx(bitmap,Machine->gfx[0],
					    code,
					    color,
					    0,0,
				    	256-spriteram[offs+2],spriteram[offs+1],
				        &Machine->drv->visible_area,TRANSPARENCY_PEN,0);
            }
            else
            {
                /* 32x32 sprite */

			    drawgfx(bitmap,Machine->gfx[1],
					    code >> 2,
					    color,
					    0,0,
				    	256-spriteram[offs+2],spriteram[offs+1],
				        &Machine->drv->visible_area,TRANSPARENCY_PEN,0);
            }
        }
	}
}


void nomnlnd_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;


	magspot2_vh_screenrefresh(bitmap, full_refresh);


    if (nomnlnd_background_on)
    {
		// draw trees

        static UINT8 water_animate=0;

        water_animate++;

    	for(offs=0;offs<2;offs++)
        {
			int code,x,y;

			x = nomnlnd_tree_positions[offs][0];
			y = nomnlnd_tree_positions[offs][1];

			if (flipscreen)
			{
				x = 223 - x;
				y = 223 - y;
				code = 2 + offs;
			}
			else
			{
				code = offs;
			}

    		drawgfx(bitmap,Machine->gfx[2],
					code,
					8,
					0,0,
					x,y,
					&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
        }

		// draw water

    	for(offs=0;offs<4;offs++)
        {
			int x,y;

			x = nomnlnd_water_positions[offs][0];
			y = nomnlnd_water_positions[offs][1];

			if (flipscreen)
			{
				x = 239 - x;
				y = 223 - y;
			}

    		drawgfx(bitmap,Machine->gfx[3],
					water_animate >> 3,
					9,
					0,0,
					x,y,
					&Machine->drv->visible_area,TRANSPARENCY_NONE,0);
        }
    }
}
