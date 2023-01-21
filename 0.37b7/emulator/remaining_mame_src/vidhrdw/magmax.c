/***************************************************************************

Video Hardware for MAGMAX.

Driver by Takahiro Nogi (nogi@kt.rim.or.jp) 1999/11/05 -
Additional tweaking by Jarek Burczynski

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"

unsigned char *magmax_videoram;
int magmax_videoram_size;

unsigned char magmax_scroll_x[2];
unsigned char magmax_scroll_y[2];
unsigned short magmax_vreg;
static int flipscreen = 0;


/***************************************************************************

  Convert the color PROMs into a more useable format.

  Mag Max has three 256x4 palette PROMs (one per gun), connected to the
  RGB output this way:

  bit 3 -- 220 ohm resistor  -- RED/GREEN/BLUE
        -- 470 ohm resistor  -- RED/GREEN/BLUE
        -- 1  kohm resistor  -- RED/GREEN/BLUE
  bit 0 -- 2.2kohm resistor  -- RED/GREEN/BLUE

***************************************************************************/
void magmax_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable, const unsigned char *color_prom)
{
	int i;
	#define TOTAL_COLORS(gfxn) (Machine->gfx[gfxn]->total_colors * Machine->gfx[gfxn]->color_granularity)
	#define COLOR(gfxn, offs) (colortable[Machine->drv->gfxdecodeinfo[gfxn].color_codes_start + offs])

	for (i = 0; i < Machine->drv->total_colors; i++)
	{
		int bit0, bit1, bit2, bit3;

		bit0 = (color_prom[0] >> 0) & 0x01;
		bit1 = (color_prom[0] >> 1) & 0x01;
		bit2 = (color_prom[0] >> 2) & 0x01;
		bit3 = (color_prom[0] >> 3) & 0x01;
		*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
		bit0 = (color_prom[Machine->drv->total_colors] >> 0) & 0x01;
		bit1 = (color_prom[Machine->drv->total_colors] >> 1) & 0x01;
		bit2 = (color_prom[Machine->drv->total_colors] >> 2) & 0x01;
		bit3 = (color_prom[Machine->drv->total_colors] >> 3) & 0x01;
		*(palette++) = (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
		bit0 = (color_prom[2*Machine->drv->total_colors] >> 0) & 0x01;
		bit1 = (color_prom[2*Machine->drv->total_colors] >> 1) & 0x01;
		bit2 = (color_prom[2*Machine->drv->total_colors] >> 2) & 0x01;
		bit3 = (color_prom[2*Machine->drv->total_colors] >> 3) & 0x01;
		*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;

		color_prom++;
	}

	color_prom += 2*Machine->drv->total_colors;
	/* color_prom now points to the beginning of the lookup tables */

	/* characters use colors 0-15 */
	for (i = 0; i < TOTAL_COLORS(0);i++)
		COLOR(0, i) = i;

	/*sprites use colors 16-32, color 31 being transparent*/
	for (i = 0; i < TOTAL_COLORS(1);i++)
	{
		COLOR(1, i) = *(color_prom++) + 16;
	}

}


int magmax_vh_start(void)
{
	if (generic_vh_start() != 0) return 1;
	return 0;
}

void magmax_vh_stop(void)
{
	generic_vh_stop();
}


void magmax_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;

	/* bit 2 flip screen */
	if (flipscreen != (magmax_vreg & 0x04))
	{
		flipscreen = magmax_vreg & 0x04;
	}

	/* copy the background graphics */
	if (magmax_vreg & 0x40)		/* background disable */
	{
		fillbitmap(bitmap, Machine->pens[0], &Machine->visible_area);
	}
	else
	{
		unsigned short *pens = Machine->pens;

		int h,v;

		unsigned char * rom18B = memory_region(REGION_USER1); /*rom 20B starts at offset 0x2000*/
		unsigned char * rom18D = memory_region(REGION_USER2); /*rom 20D starts at offset 0x2000*/
		unsigned char * prom14D= memory_region(REGION_USER3); /*prom 15D starts at offset 0x100*/
		unsigned char * rom15F = memory_region(REGION_GFX3);  /*rom 15F offs=0x0; 17F offs=0x2000; 18F offs=0x4000; 20F offs=0x6000*/

		int scroll_h = READ_WORD(magmax_scroll_x) & 0x3fff;
		int scroll_v = READ_WORD(magmax_scroll_y) & 0xff;

		for (v = 2*8; v < 30*8; v++) /*only for visible area*/
		{
			int map_v_scr = scroll_v + v;

			for (h = 0; h < 256; h++)
			{
				int rom18B_addr;
				int rom18D_addr;
				int prom14D_addr;
				int rom15F_addr;

				int graph_data;
				int graph_color;

				int LS273;
				int LS283;
				int map_h_scr;
				int prom_data;

				int map_h = h;

				if ( h & 0x80 )
					map_h ^= 0x7f;

				rom18B_addr = (map_h>>1) & 0x3f;				//6 LSB bits
				rom18B_addr |= (map_v_scr & 0x1fe) << 5;	//8 MSB bits (from bit 6)

				if ( map_h & 0x1 )
					LS273 = rom18B[ rom18B_addr + 0x2000 ];		//pixel 1,3,5,7 from rom 20B
				else
					LS273 = rom18B[ rom18B_addr ];				//pixel 0,2,4,6 from rom 18B

				if (map_v_scr & 0x100)							//clear LS273 @17C,17B
				{
					LS273 = 0x0;
				}

				if ( !(map_v_scr & 0x100) && (h & 0x80) )
					LS273 ^= 0xff;								//this is two LS86 @15C,15B operation


//place for optimization here as LS273 output is const between frames
//it only depends on screen position and virtual bitmap position

				map_h_scr = scroll_h + h;						//this is the first LS283 column @12C,12B,11C,11B

				if ( !(map_v_scr & 0x100) && !(h & 0x80) )
					LS283 = map_h_scr + LS273 + 0xff00 + 0x1;	//this is the output of the second LS283 column	@13C,13B,14C,14B
				else
					LS283 =	map_h_scr + LS273;

				prom14D_addr = (LS283 >> 6 ) & 0xff;

				prom_data = (prom14D[prom14D_addr] << 4) | (prom14D[prom14D_addr + 0x100]);

				rom18D_addr =  ((prom_data & 0x1f)<<8) | (map_v_scr & 0xf8) | ((LS283 & 0x38) >>3);

				if (map_v_scr & 0x100)							//LS139 @20E working as rom 18D/20D selector
					rom18D_addr += 0x2000;						//if carry set on upper LS283 @20A then select rom 20D

				rom15F_addr = (rom18D[ rom18D_addr ] << 5 ) | ((map_v_scr & 0x7)<<2) | ((LS283 & 0x6)>>1);

				//now LS139 @20E working as rom 15F/17F/18F/20F selector
				if (prom_data & 0x10)
					rom15F_addr += 0x4000;
				if (map_v_scr & 0x100)
				  	rom15F_addr += 0x2000;

				if ((LS283 & 1)==0)
					graph_data = rom15F[rom15F_addr] & 0x0f;	//this is function of LS157 @13F
				else
					graph_data = (rom15F[rom15F_addr]>>4) & 0x0f;//as above

				graph_color = (prom_data & 0xe0) >> 1;			//7,6,5 bits are bits 6,5,4 of code color
				graph_color += 2*16;							//after decoding via LS138 @14E, then via LS148 @unreadable (bottom-right part of the page)

				if (map_v_scr & 0x100)
					graph_color += 0x80;	//the MSB bits going to color proms depend on bitmap line (upper-right part of the page)
								//luckily first 32 colors are the same in both upper and lower parts of color proms
								//so we dont need to bother emulating this for sprites and text

				plot_pixel(bitmap,h,v,pens[graph_color + graph_data] ); //"LS273" variable output is const along all bitmap
			}
		}
	}

	/* draw the sprites */
	for (offs = 0; offs < spriteram_size; offs += 8)
	{
		int code;
		int attr = READ_WORD(&spriteram[offs + 4]) & 0xff;
		int color = (attr & 0xf0) >> 4;
		int flipx = attr & 0x04;
		int flipy = attr & 0x08;
		int sx, sy;

		sx = (READ_WORD(&spriteram[offs + 6]) & 0xff) - 0x80 + 0x100 * (attr & 0x01);
		sy = 240 - (READ_WORD(&spriteram[offs]) & 0xff);

		if (flipscreen)
		{
			sx = 255 - sx;
			sy = 255 - sy;
			flipx = !flipx;
			flipy = !flipy;
		}

		code = (READ_WORD(&spriteram[offs + 2]) & 0xff);

		if (code & 0x80)	/* sprite bankswitch */
		{
			code += ((magmax_vreg>>4) & 0x3) * 0x80;
		}

		drawgfx(bitmap, Machine->gfx[1],
				code,
				color,
				flipx, flipy,
				sx, sy,
				&Machine->visible_area, TRANSPARENCY_COLOR, 31);
	}

	/* draw the foreground characters */
	for (offs = 32*32-1; offs >= 0; offs -= 1)
	{
		int sx, sy;
		int page = ((magmax_vreg>>3) & 0x1);

		sx = (offs % 32);
		sy = (offs / 32);

		if (flipscreen)
		{
			sx = 31 - sx;
			sy = 31 - sy;
		}

		drawgfx(bitmap, Machine->gfx[0],
				READ_WORD(&videoram[offs*2 + page]) & 0xff,
				0,
				flipscreen, flipscreen,
				8 * sx, 8 * sy,
				&Machine->visible_area, TRANSPARENCY_PEN, 15);
	}

#if 0
	{
		char mess[80];
		sprintf(mess, "SCR-X:%04X SCR-Y:%04X VREG:%04X", READ_WORD(magmax_scroll_x), READ_WORD(magmax_scroll_y), magmax_vreg);
		ui_text(Machine->scrbitmap, mess, 0, 0);
	}
#endif

}
