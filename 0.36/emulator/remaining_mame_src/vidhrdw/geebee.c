/****************************************************************************
 *
 * geebee.c
 *
 * video driver
 * juergen buchmueller <pullmoll@t-online.de>, jan 2000
 *
 * TODO:
 * backdrop support for lamps? (player1, player2 and serve)
 * what is the counter output anyway?
 * add overlay colors for Navalone and Kaitei Takara Sagashi
 *
 ****************************************************************************/

#include <signal.h>
#include "driver.h"
#include "vidhrdw/generic.h"

/* from machine/geebee.c */
extern int geebee_ball_h;
extern int geebee_ball_v;
extern int geebee_lamp1;
extern int geebee_lamp2;
extern int geebee_lamp3;
extern int geebee_counter;
extern int geebee_lock_out_coil;
extern int geebee_bgw;
extern int geebee_ball_on;
extern int geebee_inv;

#ifdef MAME_DEBUG
char geebee_msg[32+1];
int geebee_cnt;
#endif

#define SCR_HORZ 34
#define SCR_VERT 32

#define WHITE	0
#define PINK1	1
#define PINK2	2
#define ORANGE	3
#define BLUE	4

static unsigned char palette[] =
{
	0x00,0x00,0x00, 0xff,0xff,0xff, 0x7f,0x7f,0x7f, /* BLACK, WHITE, GREY  */
	0x10,0x00,0x18, 0xa0,0x00,0xe0, 0x50,0x00,0x70, /* PINK #1 dark, bright, dim */
	0x14,0x00,0x18, 0xe0,0x00,0xf0, 0x70,0x00,0x78, /* PINK #2 dark, bright, dim  */
	0x14,0x10,0x00, 0xff,0xd0,0x00, 0x80,0x68,0x00, /* ORANGE dark, bright, dim  */
	0x00,0x00,0x20, 0x00,0x00,0xff, 0x00,0x00,0x80, /* BLUE dark, bright, dim  */
};

static unsigned short geebee_colortable[] = {
	 0, 1,	0, 2,  1, 0,  2, 0,
	 3, 4,	3, 5,  4, 3,  5, 3,
	 6, 7,	6, 8,  7, 6,  8, 6,
	 9,10,	9,11, 10, 9, 11, 9,
	12,13, 12,14, 13,12, 14,12
};

static unsigned short navalone_colortable[] = {
	 0, 1,	0, 2,  0, 1,  0, 2,
	 3, 4,	3, 5,  3, 4,  3, 5,
	 6, 7,	6, 8,  6, 7,  6, 8,
	 9,10,	9,11,  9,10,  9,11,
	12,13, 12,14, 12,13, 12,14
};

static UINT8 *overlay;

void setcolor(int _x0, int _x1, int _y0, int _y1, int color)
{
	int x, y;

	if( !overlay )
	{
		overlay = (UINT8 *)malloc(_y1 * _x1);
		if( !overlay )
			raise(SIGABRT);
		_y1--;
		_x1--;
	}

    for( x = _x0; x <= _x1; x++ )
		for( y = _y0; y <= _y1; y++ )
			overlay[y*SCR_HORZ*8+x] = color;
}

INLINE UINT16 getcolor(int x, int y)
{
	return overlay[y*SCR_HORZ*8+x];
}

int geebee_vh_start(void)
{
	if( generic_vh_start() )
		return 1;

	setcolor( 0,  SCR_HORZ*8,0,   SCR_VERT*8,  WHITE);
	/* Use an overlay only in Upright mode */
	if( (readinputport(2) & 0x01) == 0 )
	{
		setcolor( 1*8,	4*8-1,	  0,32*8-1, PINK2);
		setcolor( 4*8,	5*8-1,	  0, 6*8-1, PINK1);
		setcolor( 4*8,	5*8-1, 26*8,32*8-1, PINK1);
		setcolor( 4*8,	5*8-1,	6*8,26*8-1, ORANGE);
		setcolor( 5*8, 28*8-1,	  0, 3*8-1, PINK1);
		setcolor( 5*8, 28*8-1, 29*8,32*8-1, PINK1);
		setcolor( 5*8, 28*8-1,	3*8, 6*8-1, BLUE);
		setcolor( 5*8, 28*8-1, 26*8,29*8-1, BLUE);
		setcolor(12*8, 13*8-1, 15*8,17*8-1, BLUE);
		setcolor(21*8, 23*8-1, 12*8,14*8-1, BLUE);
		setcolor(21*8, 23*8-1, 18*8,20*8-1, BLUE);
		setcolor(28*8, 29*8-1,	  0,32*8-1, PINK2);
		setcolor(29*8, 32*8-1,	  0,32*8-1, PINK1);
	}

	return 0;
}

int navalone_vh_start(void)
{
	if( generic_vh_start() )
		return 1;

	setcolor( 0,  SCR_HORZ*8,0,   SCR_VERT*8,  WHITE);
    /* overlay? */

	return 0;
}

int sos_vh_start(void)
{
	if( generic_vh_start() )
		return 1;

	setcolor( 0,  SCR_HORZ*8,0,   SCR_VERT*8,  WHITE);
    /* overlay? */

	return 0;
}

int kaitei_vh_start(void)
{
	if( generic_vh_start() )
	return 1;

	setcolor( 0,  SCR_HORZ*8,0,   SCR_VERT*8,  WHITE);
    /* overlay? */

	return 0;
}

/* Initialise the palette */
void geebee_init_palette(unsigned char *sys_palette, unsigned short *sys_colortable, const unsigned char *color_prom)
{
	memcpy(sys_palette, palette, sizeof (palette));
	memcpy(sys_colortable, geebee_colortable, sizeof (geebee_colortable));
}

/* Initialise the palette */
void navalone_init_palette(unsigned char *sys_palette, unsigned short *sys_colortable, const unsigned char *color_prom)
{
	memcpy(sys_palette, palette, sizeof (palette));
	memcpy(sys_colortable, navalone_colortable, sizeof (navalone_colortable));
}

void geebee_vh_stop(void)
{
	if( overlay )
		free(overlay);
	overlay = NULL;
	generic_vh_stop();
}

INLINE void geebee_plot(struct osd_bitmap *bitmap, int x, int y)
{
	struct rectangle r = Machine->drv->visible_area;
	if (x >= r.min_x && x <= r.max_x && y >= r.min_y && y <= r.max_y)
		plot_pixel(bitmap,x,y,Machine->pens[3*getcolor(x,y)+1]);
}

INLINE void mark_dirty(int x, int y)
{
	int cx, cy, offs;
	cy = y / 8;
	cx = x / 8;
    if (geebee_inv)
	{
		offs = (32 - cx) + (31 - cy) * 32;
		dirtybuffer[offs % videoram_size] = 1;
		dirtybuffer[(offs - 1) & (videoram_size - 1)] = 1;
		dirtybuffer[(offs - 32) & (videoram_size - 1)] = 1;
		dirtybuffer[(offs - 32 - 1) & (videoram_size - 1)] = 1;
	}
	else
	{
		offs = (cx - 1) + cy * 32;
		dirtybuffer[offs & (videoram_size - 1)] = 1;
		dirtybuffer[(offs + 1) & (videoram_size - 1)] = 1;
		dirtybuffer[(offs + 32) & (videoram_size - 1)] = 1;
		dirtybuffer[(offs + 32 + 1) & (videoram_size - 1)] = 1;
	}
}

void geebee_vh_screenrefresh(struct osd_bitmap *bitmap, int full_refresh)
{
	int offs;

#ifdef MAME_DEBUG
	if( geebee_cnt > 0 )
	{
		ui_text(geebee_msg, Machine->drv->visible_area.min_y, Machine->drv->visible_area.max_x - 8);
		if( --geebee_cnt == 0 )
			full_refresh = 1;
    }
#endif

	if ( full_refresh )
        memset(dirtybuffer, 1, videoram_size);

	if( geebee_inv )
	{
		for( offs = 0; offs < videoram_size; offs++ )
		{
			if( dirtybuffer[offs] )
			{
				int mx,my,sx,sy,code,color;

				dirtybuffer[offs] = 0;
				mx = offs % 32;
				my = offs / 32;

				if (my == 0)
				{
					sx = 8*0;
					sy = 8*mx;
				}
				else if (my == 1)
				{
					sx = 8*33;
					sy = 8*mx;
				}
				else
				{
					sx = 8*(mx+1);
					sy = 8*my;
				}

				code = videoram[offs];
				color = ((geebee_bgw & 1) << 1) | ((code & 0x80) >> 7);
				drawgfx(
					bitmap,Machine->gfx[0],code,4*getcolor(sx,sy)+color,1,1,sx,sy,
					&Machine->drv->visible_area,TRANSPARENCY_NONE,0);
			}
		}
	}
	else
	{
		for( offs = 0; offs < videoram_size; offs++ )
		{
			if( dirtybuffer[offs] )
			{
				int mx,my,sx,sy,code,color;

				dirtybuffer[offs] = 0;
				mx = offs % 32;
				my = offs / 32;

				if (my == 0)
				{
					sx = 8*33;
					sy = 8*mx;
				}
				else if (my == 1)
				{
					sx = 8*0;
					sy = 8*mx;
				}
				else
				{
					sx = 8*(mx+1);
					sy = 8*my;
				}

				code = videoram[offs];
				color = ((geebee_bgw & 1) << 1) | ((code & 0x80) >> 7);
				drawgfx(
					bitmap,Machine->gfx[0],code,4*getcolor(sx,sy)+color,0,0,sx,sy,
					&Machine->drv->visible_area,TRANSPARENCY_NONE,0);
			}
		}
	}

	if( geebee_ball_on )
	{
		int x, y;
		if( geebee_inv )
		{
			mark_dirty(geebee_ball_h-9,geebee_ball_v-2);
			for( y = 0; y < 4; y++ )
				for( x = 0; x < 4; x++ )
					geebee_plot(bitmap,geebee_ball_h+x-9,geebee_ball_v+y-2);
		}
		else
		{
			mark_dirty(geebee_ball_h+5,geebee_ball_v-2);
			for( y = 0; y < 4; y++ )
				for( x = 0; x < 4; x++ )
					geebee_plot(bitmap,geebee_ball_h+x+5,geebee_ball_v+y-2);
		}
	}
}

