/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

  * History *

  MJC - 01.02.98 - Line based dirty color / dirty rectangle handling
                   Sparkle Circuit for Gorf

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"
#include "cpu/z80/z80.h"


void wow_update_line(int line, int gorf);


unsigned char *wow_videoram;
static int magic_expand_color, magic_control, collision;

#define MAX_STARS 750							/* Vars for Stars */
                                      			/* Used in Gorf & WOW */
struct star
{
	int x,y,color;
};

static int star_color[8] = {0,1,2,3,3,2,1,0};
static struct star stars[MAX_STARS];
static int total_stars;


static int colors[8] = {0,0,0,0,0xc7,0xf3,0x7c,0x51}; /* otherwise WOW stars out blank */

static UINT64 current_line_color;
static UINT64 line_color[256];

static int Latch[16];

/* ======================================================================= */

int wow_intercept_r(int offset)
{
	int res;

	res = collision;
	collision = 0;

	return res;
}


/* Switches color registers at this zone - 40 zones (NOT USED) */

void wow_colour_split_w(int offset, int data)
{
#if 0
	static int ColourSplit;
	int NewSplit;

    NewSplit = (data * 2) - 1;

	if(ColourSplit != NewSplit)
    {
        ColourSplit = NewSplit;
        memset(dirtybuffer,1,204);
    }

#ifdef MAME_DEBUG
    if (errorlog) fprintf(errorlog,"colors split set to %02d\n",ColourSplit);
#endif
#endif
}


void wow_colour_register_w(int offset, int data)
{
	if(colors[offset] != data)
    {
		colors[offset] = data;

		current_line_color = ((UINT64)colors[0] << 56) | ((UINT64)colors[1] << 48) |
							 ((UINT64)colors[2] << 40) | ((UINT64)colors[3] << 32) |
        					 ((UINT64)colors[4] << 24) | ((UINT64)colors[5] << 16) |
        					 ((UINT64)colors[6] << 8)  | ((UINT64)colors[7] << 0);
	}

#ifdef MAME_DEBUG
    if (errorlog) fprintf(errorlog,"colors %01x set to %02x\n",offset,data);
#endif
}


void wow_videoram_w(int offset,int data)
{
	if ((offset < 0x4000) && (wow_videoram[offset] != data))
	{
		wow_videoram[offset] = data;
        dirtybuffer[offset / 80] = 1;
    }
}


void wow_magic_expand_color_w(int offset,int data)
{
#ifdef MAME_DEBUG
	if (errorlog) fprintf(errorlog,"%04x: magic_expand_color = %02x\n",cpu_get_pc(),data);
#endif

	magic_expand_color = data;
}


void wow_magic_control_w(int offset,int data)
{
#ifdef MAME_DEBUG
	if (errorlog) fprintf(errorlog,"%04x: magic_control = %02x\n",cpu_get_pc(),data);
#endif

	magic_control = data;
}


static void copywithflip(int offset,int data)
{
	if (magic_control & 0x40)	/* copy backwards */
	{
		int bits,stib,k;

		bits = data;
		stib = 0;
		for (k = 0;k < 4;k++)
		{
			stib >>= 2;
			stib |= (bits & 0xc0);
			bits <<= 2;
		}

		data = stib;
	}

	if (magic_control & 0x40)	/* copy backwards */
	{
		int shift,data1,mask;


		shift = magic_control & 3;
		data1 = 0;
		mask = 0xff;
		while (shift > 0)
		{
			data1 <<= 2;
			data1 |= (data & 0xc0) >> 6;
			data <<= 2;
			mask <<= 2;
			shift--;
		}

		if (magic_control & 0x30)
		{
			/* TODO: the collision detection should be made */
			/* independently for each of the four pixels    */

			if ((mask & wow_videoram[offset]) || (~mask & wow_videoram[offset-1]))
				collision |= 0xff;
			else collision &= 0x0f;
		}

		if (magic_control & 0x20) data ^= wow_videoram[offset];	/* draw in XOR mode */
		else if (magic_control & 0x10) data |= wow_videoram[offset];	/* draw in OR mode */
		else data |= ~mask & wow_videoram[offset];	/* draw in copy mode */
		wow_videoram_w(offset,data);
		if (magic_control & 0x20) data1 ^= wow_videoram[offset-1];	/* draw in XOR mode */
		else if (magic_control & 0x10) data1 |= wow_videoram[offset-1];	/* draw in OR mode */
		else data1 |= mask & wow_videoram[offset-1];	/* draw in copy mode */
		wow_videoram_w(offset-1,data1);
	}
	else
	{
		int shift,data1,mask;


		shift = magic_control & 3;
		data1 = 0;
		mask = 0xff;
		while (shift > 0)
		{
			data1 >>= 2;
			data1 |= (data & 0x03) << 6;
			data >>= 2;
			mask >>= 2;
			shift--;
		}

		if (magic_control & 0x30)
		{
			/* TODO: the collision detection should be made independently for */
			/* each of the four pixels */
			if ((mask & wow_videoram[offset]) || (~mask & wow_videoram[offset+1]))
				collision |= 0xff;
			else collision &= 0x0f;
		}

		if (magic_control & 0x20)
			data ^= wow_videoram[offset];	/* draw in XOR mode */
		else if (magic_control & 0x10)
			data |= wow_videoram[offset];	/* draw in OR mode */
		else
			data |= ~mask & wow_videoram[offset];	/* draw in copy mode */
		wow_videoram_w(offset,data);
		if (magic_control & 0x20)
			data1 ^= wow_videoram[offset+1];	/* draw in XOR mode */
		else if (magic_control & 0x10)
			data1 |= wow_videoram[offset+1];	/* draw in OR mode */
		else
			data1 |= mask & wow_videoram[offset+1];	/* draw in copy mode */
		wow_videoram_w(offset+1,data1);
	}
}


void wow_magicram_w(int offset,int data)
{
	if (magic_control & 0x08)	/* expand mode */
	{
		int bits,bibits,k;
		static int count;

		bits = data;
		if (count) bits <<= 4;
		bibits = 0;
		for (k = 0;k < 4;k++)
		{
			bibits <<= 2;
			if (bits & 0x80) bibits |= (magic_expand_color >> 2) & 0x03;
			else bibits |= magic_expand_color & 0x03;
			bits <<= 1;
		}

		copywithflip(offset,bibits);

		count ^= 1;
	}
	else copywithflip(offset,data);
}


void wow_pattern_board_w(int offset,int data)
{
	static int src;
	static int mode;	/*  bit 0 = direction
							bit 1 = expand mode
							bit 2 = constant
							bit 3 = flush
							bit 4 = flip
							bit 5 = flop */
	static int skip;	/* bytes to skip after row copy */
	static int dest;
	static int length;	/* row length */
	static int loops;	/* rows to copy - 1 */
	unsigned char *RAM = memory_region(REGION_CPU1);


	switch (offset)
	{
		case 0:
			src = data;
			break;
		case 1:
			src = src + data * 256;
			break;
		case 2:
			mode = data & 0x3f;			/* register is 6 bit wide */
			break;
		case 3:
			skip = data;
			break;
		case 4:
			dest = skip + data * 256;	/* register 3 is shared between skip and dest */
			break;
		case 5:
			length = data;
			break;
		case 6:
			loops = data;
			break;
	}

	if (offset == 6)	/* trigger blit */
	{
		int i,j;

#ifdef MAME_DEBUG
		if (errorlog) fprintf(errorlog,"%04x: blit src %04x mode %02x skip %d dest %04x length %d loops %d\n",
			cpu_get_pc(),src,mode,skip,dest,length,loops);
#endif

        /* Special scroll screen for Gorf */

        if (src==(dest+0x4000))
        {
        	if(dest==0)
            {
				for (i=0x3FFF;i>=0;i--) wow_magicram_w(i,RAM[i+0x4000]);

                /* Redraw screen to avoid tears */

                for (i=203;i>=0;i--) wow_update_line(i, 1);

		        /* Cycle Steal (slow scroll down!) */

				z80_ICount -= 65336;
            }
        }
        else
        {
		    for (i = 0; i <= loops;i++)
		    {
			    for (j = 0;j <= length;j++)
			    {
				    if (!(mode & 0x08) || j < length)
					{
                        if (mode & 0x01)			/* Direction */
						    RAM[src]=RAM[dest];
                        else
						    if (dest >= 0) cpu_writemem16(dest,RAM[src]);	/* ASG 971005 */
					}

				    if ((j & 1) || !(mode & 0x02))  /* Expand Mode - don't increment source on odd loops */
					    if (mode & 0x04) src++;		/* Constant mode - don't increment at all! */

				    if (mode & 0x20) dest++;		/* copy forwards */
				    else dest--;					/* backwards */
			    }

			    if ((j & 1) && (mode & 0x02))	    /* always increment source at end of line */
				    if (mode & 0x04) src++;			/* Constant mode - don't increment at all! */

			    if ((mode & 0x08) && (mode & 0x04)) /* Correct src if in flush mode */
				    src--;                          /* and NOT in Constant mode */

			    if (mode & 0x20) dest--;			/* copy forwards */
			    else dest++;						/* backwards */

			    dest += (int)((signed char)skip);	/* extend the sign of the skip register */

		    /* Note: actually the hardware doesn't handle the sign of the skip register, */
		    /* when incrementing the destination address the carry bit is taken from the */
		    /* mode register. To faithfully emulate the hardware I should do: */
#if 0
			    {
				    int lo,hi;

				    lo = dest & 0x00ff;
				    hi = dest & 0xff00;
				    lo += skip;
				    if (mode & 0x10)
				    {
					    if (lo < 0x100) hi -= 0x100;
				    }
				    else
				    {
					    if (lo > 0xff) hi += 0x100;
				    }
				    dest = hi | (lo & 0xff);
			    }
#endif
		    }
    	}
	}
}


static void init_star_field(void)
{
	int generator;
	int x,y;

	total_stars = 0;
	generator = 0;

	for (y = 203;y >= 0;y--)
	{
		for (x = 319;x >= 0;x--)
		{
			int bit1,bit2;

			generator <<= 1;
			bit1 = (~generator >> 17) & 1;
			bit2 = (generator >> 5) & 1;

			if (bit1 ^ bit2) generator |= 1;

			if (y >= Machine->drv->visible_area.min_x &&
				y <= Machine->drv->visible_area.max_x &&
				((~generator >> 16) & 1) &&
				(generator & 0x3f) == 0x3f)
			{
				int color;

				color = (~(generator >> 8)) & 0x07;
				if (color && (total_stars < MAX_STARS))
				{
					stars[total_stars].x      = x;
					stars[total_stars].y      = y;
					stars[total_stars].color = color-1;

					total_stars++;
				}
			}
		}
	}
}


/* GORF Special Registers
 *
 * These are data writes, done by IN commands
 *
 * The data is placed on the upper bits 8-11 bits of the address bus (B)
 * and is used to drive 2 8 bit addressable latches to control :-
 *
 * IO 15
 *   0
 *   1
 *   2 Star Field Blue/Black (it's always on)
 *   3 Sparkle 1
 *   4 Sparkle 2
 *   5 Sparkle 3
 *   6 Second Amp On/Off ?
 *   7 Drv7
 *
 * IO 16
 *   0
 *   1
 *   2
 *   3
 *   4
 *   5
 *   6
 *   7 Space Cadet Light ?
 *
 */

int gorf_io_r(int offset)
{
	int data;

	data = (cpu_get_reg(Z80_BC) >> 8) & 0x0F;

    Latch[(offset << 3) + (data >> 1)] = (data & 0x01);

#ifdef MAME_DEBUG
    if (errorlog) fprintf(errorlog,"Gorf Latch IO %02x set to %d (%02x)\n",(offset << 3) + (data >> 1),data & 0x01,data);
#endif

    return data;			/* Probably not used */
}


/****************************************************************************
 * Gorf specific routines
 ****************************************************************************/

static void draw_stars(struct osd_bitmap *bitmap, int gorf)
{
    static int speed = 0;
	int offs;

	int color = (gorf ? 0 : 4);

    if (colors[color] != 0)
    {
	    speed = (speed + 1) & 3;

        if (speed==0)								/* Time to change color */
        {
		    for (offs = total_stars-1;offs >= 0;offs--)
				stars[offs].color = (stars[offs].color + 1) & 7;
        }

	    for (offs = total_stars-1;offs >= 0;offs--)
	    {
		    int x,y;

		    x = stars[offs].x;
		    y = stars[offs].y;

		    if (read_pixel(bitmap, x, y) == Machine->pens[colors[color]])
			    plot_pixel(bitmap, x, y, Machine->pens[colors[color]+star_color[stars[offs].color]]);
	    }
    }
}


void gorf_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;
    int Sparkle=0;
    int SparkleLow=0;
    int SparkleHigh=0;
	unsigned char *RAM = memory_region(REGION_CPU1);


  	copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);


    /* Plot the stars (on color 0 only) */

	draw_stars(bitmap, 1);

    /*
     * Sparkle Circuit
     *
     * Because of the way the dirty rectangles are implemented, this will
     * be updated every 4 frames. It needs to be calculated every frame.
     *
     */

	if (RAM[0x5A93]==160) 							/* INVADERS */
    {
        Sparkle     = 3;
        SparkleLow  = 62;
        SparkleHigh = 103;
    }

	if (RAM[0x5A93]==5) 							/* FLAG SHIP */
    {
        Sparkle     = 3;
        SparkleLow  = 148;
        SparkleHigh = 188;
    }

    if (Sparkle)
    {
    	int line;

   	    for (line = SparkleLow; line <= SparkleHigh; line++)
   	    {
       	    for (offs = 203; offs >= 0; offs--)
       	    {
           	    if ( (read_pixel(bitmap, line, offs) == Machine->pens[colors[3]]) &&
           	        !(rand() & 0x04))
               	    plot_pixel(bitmap, line, offs, Machine->pens[colors[0]]);
			}
		}
    }
}

/****************************************************************************
 * Seawolf specific routines
 ****************************************************************************/

void seawolf2_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	extern int wow_controller1;
	extern int wow_controller2;

    int x,y,centre;
	unsigned char *RAM = memory_region(REGION_CPU1);


	copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);


    /* Draw a sight */

    if(RAM[0xc1fb] != 0)	/* Number of Players */
    {
    	/* Yellow sight for Player 1 */

        centre = 317 - (wow_controller1-18) * 10;

        if (centre<2)   centre=2;
        if (centre>317) centre=317;

		for (y=35-10;y<35+11;y++)
			plot_pixel(bitmap, centre, y, Machine->pens[0x77]);

   	    for (x=centre-20;x<centre+21;x++)
       	    if((x>0) && (x<319))
				plot_pixel(bitmap, x, 35, Machine->pens[0x77]);


        /* Red sight for Player 2 */

        if(RAM[0xc1fb] == 2)
		{
            centre = 316 - (wow_controller2-18) * 10;

            if (centre<1)   centre=1;
            if (centre>316) centre=316;

			for (y=33-10;y<33+11;y++)
				plot_pixel(bitmap, centre, y, Machine->pens[0x58]);

	   	    for (x=centre-20;x<centre+21;x++)
	       	    if((x>0) && (x<319))
					plot_pixel(bitmap, x, 33, Machine->pens[0x58]);
        }
    }
}

/****************************************************************************
 * Wizard of Wor
 ****************************************************************************/

int wow_vh_start(void)
{
	if (generic_vh_start() != 0)
		return 1;

	init_star_field();

	return 0;
}


void wow_vh_screenrefresh_stars(struct osd_bitmap *bitmap,int full_refresh)
{
  	copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);

    /* Plot the stars (on color 4 only) */

	draw_stars(bitmap, 0);
}


/****************************************************************************
 * Standard WOW routines
 ****************************************************************************/

void wow_update_line(int line, int gorf)
{
	/* Copy one line to bitmap, using current color register settings */

    int memloc;
    int i,x;
    int data,color;

    /* Redraw line if anything changed */

	if (dirtybuffer[line] || (line_color[line] != current_line_color))
    {
		line_color[line]  = current_line_color;
        dirtybuffer[line] = 0;

        memloc = line * 80;


        for (i = 0; i < 80; i++, memloc++)
        {
			data = wow_videoram[memloc];

            for (x = i*4+3; x >= i*4; x--)
            {
            	color = (data & 0x03) | (gorf ? 0 : 4);

				plot_pixel(tmpbitmap, x, line, Machine->pens[colors[color]]);

                data >>= 2;
            }
        }
    }
}


void wow_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);
}

