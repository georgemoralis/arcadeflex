/*********************************************************************

  usrintrf.c

  Functions used to handle MAME's crude user interface.

*********************************************************************/

#include "driver.h"
#include "info.h"
#include "vidhrdw/vector.h"
#include "datafile.h"
#include <stdarg.h>

#ifdef MESS
  #include "mess/mess.h"
#endif

#define SEL_BITS 12
#define SEL_MASK ((1<<SEL_BITS)-1)

extern int mame_debug;

extern int need_to_clear_bitmap;	/* used to tell updatescreen() to clear the bitmap */
extern int bitmap_dirty;	/* set by osd_clearbitmap() */

/* Variables for stat menu */
extern char build_version[];
extern unsigned int dispensed_tickets;
extern unsigned int coins[COIN_COUNTERS];
extern unsigned int coinlockedout[COIN_COUNTERS];

/* MARTINEZ.F 990207 Memory Card */
#ifndef NEOFREE
#ifndef TINY_COMPILE
int 		memcard_menu(int);
extern int	mcd_action;
extern int	mcd_number;
extern int	memcard_status;
extern int	memcard_number;
extern int	memcard_manager;
#endif
#endif

extern int neogeo_memcard_load(int);
extern void neogeo_memcard_save(void);
extern void neogeo_memcard_eject(void);
extern int neogeo_memcard_create(int);
/* MARTINEZ.F 990207 Memory Card End */



static int setup_selected;
static int osd_selected;
static int jukebox_selected;
static int single_step;



void set_ui_visarea (int xmin, int ymin, int xmax, int ymax)
{
	int temp,w,h;

	/* special case for vectors */
	if(Machine->drv->video_attributes == VIDEO_TYPE_VECTOR)
	{
		if (Machine->ui_orientation & ORIENTATION_SWAP_XY)
		{
			temp=xmin; xmin=ymin; ymin=temp;
			temp=xmax; xmax=ymax; ymax=temp;
		}
	}
	else
	{
		if (Machine->orientation & ORIENTATION_SWAP_XY)
		{
			w = Machine->drv->screen_height;
			h = Machine->drv->screen_width;
		}
		else
		{
			w = Machine->drv->screen_width;
			h = Machine->drv->screen_height;
		}

		if (Machine->ui_orientation & ORIENTATION_FLIP_X)
		{
			temp = w - xmin - 1;
			xmin = w - xmax - 1;
			xmax = temp ;
		}

		if (Machine->ui_orientation & ORIENTATION_FLIP_Y)
		{
			temp = h - ymin - 1;
			ymin = h - ymax - 1;
			ymax = temp;
		}

		if (Machine->ui_orientation & ORIENTATION_SWAP_XY)
		{
			temp = xmin; xmin = ymin; ymin = temp;
			temp = xmax; xmax = ymax; ymax = temp;
		}

	}
	Machine->uiwidth = xmax-xmin+1;
	Machine->uiheight = ymax-ymin+1;
	Machine->uixmin = xmin;
	Machine->uiymin = ymin;
}



struct GfxElement *builduifont(void)
{
	static unsigned char fontdata6x8[] =
	{
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x7c,0x80,0x98,0x90,0x80,0xbc,0x80,0x7c,0xf8,0x04,0x64,0x44,0x04,0xf4,0x04,0xf8,
		0x7c,0x80,0x98,0x88,0x80,0xbc,0x80,0x7c,0xf8,0x04,0x64,0x24,0x04,0xf4,0x04,0xf8,
		0x7c,0x80,0x88,0x98,0x80,0xbc,0x80,0x7c,0xf8,0x04,0x24,0x64,0x04,0xf4,0x04,0xf8,
		0x7c,0x80,0x90,0x98,0x80,0xbc,0x80,0x7c,0xf8,0x04,0x44,0x64,0x04,0xf4,0x04,0xf8,
		0x30,0x48,0x84,0xb4,0xb4,0x84,0x48,0x30,0x30,0x48,0x84,0x84,0x84,0x84,0x48,0x30,
		0x00,0xfc,0x84,0x8c,0xd4,0xa4,0xfc,0x00,0x00,0xfc,0x84,0x84,0x84,0x84,0xfc,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x30,0x68,0x78,0x78,0x30,0x00,0x00,
		0x80,0xc0,0xe0,0xf0,0xe0,0xc0,0x80,0x00,0x04,0x0c,0x1c,0x3c,0x1c,0x0c,0x04,0x00,
		0x20,0x70,0xf8,0x20,0x20,0xf8,0x70,0x20,0x48,0x48,0x48,0x48,0x48,0x00,0x48,0x00,
		0x00,0x00,0x30,0x68,0x78,0x30,0x00,0x00,0x00,0x30,0x68,0x78,0x78,0x30,0x00,0x00,
		0x70,0xd8,0xe8,0xe8,0xf8,0xf8,0x70,0x00,0x1c,0x7c,0x74,0x44,0x44,0x4c,0xcc,0xc0,
		0x20,0x70,0xf8,0x70,0x70,0x70,0x70,0x00,0x70,0x70,0x70,0x70,0xf8,0x70,0x20,0x00,
		0x00,0x10,0xf8,0xfc,0xf8,0x10,0x00,0x00,0x00,0x20,0x7c,0xfc,0x7c,0x20,0x00,0x00,
		0xb0,0x54,0xb8,0xb8,0x54,0xb0,0x00,0x00,0x00,0x28,0x6c,0xfc,0x6c,0x28,0x00,0x00,
		0x00,0x30,0x30,0x78,0x78,0xfc,0x00,0x00,0xfc,0x78,0x78,0x30,0x30,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x20,0x20,0x20,0x20,0x20,0x00,0x20,0x00,
		0x50,0x50,0x50,0x00,0x00,0x00,0x00,0x00,0x00,0x50,0xf8,0x50,0xf8,0x50,0x00,0x00,
		0x20,0x70,0xc0,0x70,0x18,0xf0,0x20,0x00,0x40,0xa4,0x48,0x10,0x20,0x48,0x94,0x08,
		0x60,0x90,0xa0,0x40,0xa8,0x90,0x68,0x00,0x10,0x20,0x40,0x00,0x00,0x00,0x00,0x00,
		0x20,0x40,0x40,0x40,0x40,0x40,0x20,0x00,0x10,0x08,0x08,0x08,0x08,0x08,0x10,0x00,
		0x20,0xa8,0x70,0xf8,0x70,0xa8,0x20,0x00,0x00,0x20,0x20,0xf8,0x20,0x20,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x30,0x30,0x60,0x00,0x00,0x00,0xf8,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x30,0x30,0x00,0x00,0x08,0x10,0x20,0x40,0x80,0x00,0x00,
		0x70,0x88,0x88,0x88,0x88,0x88,0x70,0x00,0x10,0x30,0x10,0x10,0x10,0x10,0x10,0x00,
		0x70,0x88,0x08,0x10,0x20,0x40,0xf8,0x00,0x70,0x88,0x08,0x30,0x08,0x88,0x70,0x00,
		0x10,0x30,0x50,0x90,0xf8,0x10,0x10,0x00,0xf8,0x80,0xf0,0x08,0x08,0x88,0x70,0x00,
		0x70,0x80,0xf0,0x88,0x88,0x88,0x70,0x00,0xf8,0x08,0x08,0x10,0x20,0x20,0x20,0x00,
		0x70,0x88,0x88,0x70,0x88,0x88,0x70,0x00,0x70,0x88,0x88,0x88,0x78,0x08,0x70,0x00,
		0x00,0x00,0x30,0x30,0x00,0x30,0x30,0x00,0x00,0x00,0x30,0x30,0x00,0x30,0x30,0x60,
		0x10,0x20,0x40,0x80,0x40,0x20,0x10,0x00,0x00,0x00,0xf8,0x00,0xf8,0x00,0x00,0x00,
		0x40,0x20,0x10,0x08,0x10,0x20,0x40,0x00,0x70,0x88,0x08,0x10,0x20,0x00,0x20,0x00,
		0x30,0x48,0x94,0xa4,0xa4,0x94,0x48,0x30,0x70,0x88,0x88,0xf8,0x88,0x88,0x88,0x00,
		0xf0,0x88,0x88,0xf0,0x88,0x88,0xf0,0x00,0x70,0x88,0x80,0x80,0x80,0x88,0x70,0x00,
		0xf0,0x88,0x88,0x88,0x88,0x88,0xf0,0x00,0xf8,0x80,0x80,0xf0,0x80,0x80,0xf8,0x00,
		0xf8,0x80,0x80,0xf0,0x80,0x80,0x80,0x00,0x70,0x88,0x80,0x98,0x88,0x88,0x70,0x00,
		0x88,0x88,0x88,0xf8,0x88,0x88,0x88,0x00,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x00,
		0x08,0x08,0x08,0x08,0x88,0x88,0x70,0x00,0x88,0x90,0xa0,0xc0,0xa0,0x90,0x88,0x00,
		0x80,0x80,0x80,0x80,0x80,0x80,0xf8,0x00,0x88,0xd8,0xa8,0x88,0x88,0x88,0x88,0x00,
		0x88,0xc8,0xa8,0x98,0x88,0x88,0x88,0x00,0x70,0x88,0x88,0x88,0x88,0x88,0x70,0x00,
		0xf0,0x88,0x88,0xf0,0x80,0x80,0x80,0x00,0x70,0x88,0x88,0x88,0x88,0x88,0x70,0x08,
		0xf0,0x88,0x88,0xf0,0x88,0x88,0x88,0x00,0x70,0x88,0x80,0x70,0x08,0x88,0x70,0x00,
		0xf8,0x20,0x20,0x20,0x20,0x20,0x20,0x00,0x88,0x88,0x88,0x88,0x88,0x88,0x70,0x00,
		0x88,0x88,0x88,0x88,0x88,0x50,0x20,0x00,0x88,0x88,0x88,0x88,0xa8,0xd8,0x88,0x00,
		0x88,0x50,0x20,0x20,0x20,0x50,0x88,0x00,0x88,0x88,0x88,0x50,0x20,0x20,0x20,0x00,
		0xf8,0x08,0x10,0x20,0x40,0x80,0xf8,0x00,0x30,0x20,0x20,0x20,0x20,0x20,0x30,0x00,
		0x40,0x40,0x20,0x20,0x10,0x10,0x08,0x08,0x30,0x10,0x10,0x10,0x10,0x10,0x30,0x00,
		0x20,0x50,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xfc,
		0x40,0x20,0x10,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x70,0x08,0x78,0x88,0x78,0x00,
		0x80,0x80,0xf0,0x88,0x88,0x88,0xf0,0x00,0x00,0x00,0x70,0x88,0x80,0x80,0x78,0x00,
		0x08,0x08,0x78,0x88,0x88,0x88,0x78,0x00,0x00,0x00,0x70,0x88,0xf8,0x80,0x78,0x00,
		0x18,0x20,0x70,0x20,0x20,0x20,0x20,0x00,0x00,0x00,0x78,0x88,0x88,0x78,0x08,0x70,
		0x80,0x80,0xf0,0x88,0x88,0x88,0x88,0x00,0x20,0x00,0x20,0x20,0x20,0x20,0x20,0x00,
		0x20,0x00,0x20,0x20,0x20,0x20,0x20,0xc0,0x80,0x80,0x90,0xa0,0xe0,0x90,0x88,0x00,
		0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x00,0x00,0x00,0xf0,0xa8,0xa8,0xa8,0xa8,0x00,
		0x00,0x00,0xb0,0xc8,0x88,0x88,0x88,0x00,0x00,0x00,0x70,0x88,0x88,0x88,0x70,0x00,
		0x00,0x00,0xf0,0x88,0x88,0xf0,0x80,0x80,0x00,0x00,0x78,0x88,0x88,0x78,0x08,0x08,
		0x00,0x00,0xb0,0xc8,0x80,0x80,0x80,0x00,0x00,0x00,0x78,0x80,0x70,0x08,0xf0,0x00,
		0x20,0x20,0x70,0x20,0x20,0x20,0x18,0x00,0x00,0x00,0x88,0x88,0x88,0x98,0x68,0x00,
		0x00,0x00,0x88,0x88,0x88,0x50,0x20,0x00,0x00,0x00,0xa8,0xa8,0xa8,0xa8,0x50,0x00,
		0x00,0x00,0x88,0x50,0x20,0x50,0x88,0x00,0x00,0x00,0x88,0x88,0x88,0x78,0x08,0x70,
		0x00,0x00,0xf8,0x10,0x20,0x40,0xf8,0x00,0x08,0x10,0x10,0x20,0x10,0x10,0x08,0x00,
		0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x40,0x20,0x20,0x10,0x20,0x20,0x40,0x00,
		0x00,0x68,0xb0,0x00,0x00,0x00,0x00,0x00,0x20,0x50,0x20,0x50,0xa8,0x50,0x00,0x00,
	};
#if 0	   /* HJB 990215 unused!? */
	static unsigned char fontdata8x8[] =
	{
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
		0x3C,0x42,0x99,0xBD,0xBD,0x99,0x42,0x3C,0x3C,0x42,0x81,0x81,0x81,0x81,0x42,0x3C,
		0xFE,0x82,0x8A,0xD2,0xA2,0x82,0xFE,0x00,0xFE,0x82,0x82,0x82,0x82,0x82,0xFE,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x38,0x64,0x74,0x7C,0x38,0x00,0x00,
		0x80,0xC0,0xF0,0xFC,0xF0,0xC0,0x80,0x00,0x01,0x03,0x0F,0x3F,0x0F,0x03,0x01,0x00,
		0x18,0x3C,0x7E,0x18,0x7E,0x3C,0x18,0x00,0xEE,0xEE,0xEE,0xCC,0x00,0xCC,0xCC,0x00,
		0x00,0x00,0x30,0x68,0x78,0x30,0x00,0x00,0x00,0x38,0x64,0x74,0x7C,0x38,0x00,0x00,
		0x3C,0x66,0x7A,0x7A,0x7E,0x7E,0x3C,0x00,0x0E,0x3E,0x3A,0x22,0x26,0x6E,0xE4,0x40,
		0x18,0x3C,0x7E,0x3C,0x3C,0x3C,0x3C,0x00,0x3C,0x3C,0x3C,0x3C,0x7E,0x3C,0x18,0x00,
		0x08,0x7C,0x7E,0x7E,0x7C,0x08,0x00,0x00,0x10,0x3E,0x7E,0x7E,0x3E,0x10,0x00,0x00,
		0x58,0x2A,0xDC,0xC8,0xDC,0x2A,0x58,0x00,0x24,0x66,0xFF,0xFF,0x66,0x24,0x00,0x00,
		0x00,0x10,0x10,0x38,0x38,0x7C,0xFE,0x00,0xFE,0x7C,0x38,0x38,0x10,0x10,0x00,0x00,
		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x1C,0x1C,0x1C,0x18,0x00,0x18,0x18,0x00,
		0x6C,0x6C,0x24,0x00,0x00,0x00,0x00,0x00,0x00,0x28,0x7C,0x28,0x7C,0x28,0x00,0x00,
		0x10,0x38,0x60,0x38,0x0C,0x78,0x10,0x00,0x40,0xA4,0x48,0x10,0x24,0x4A,0x04,0x00,
		0x18,0x34,0x18,0x3A,0x6C,0x66,0x3A,0x00,0x18,0x18,0x20,0x00,0x00,0x00,0x00,0x00,
		0x30,0x60,0x60,0x60,0x60,0x60,0x30,0x00,0x0C,0x06,0x06,0x06,0x06,0x06,0x0C,0x00,
		0x10,0x54,0x38,0x7C,0x38,0x54,0x10,0x00,0x00,0x18,0x18,0x7E,0x18,0x18,0x00,0x00,
		0x00,0x00,0x00,0x00,0x18,0x18,0x30,0x00,0x00,0x00,0x00,0x00,0x3E,0x00,0x00,0x00,
		0x00,0x00,0x00,0x00,0x18,0x18,0x00,0x00,0x00,0x04,0x08,0x10,0x20,0x40,0x00,0x00,
		0x38,0x4C,0xC6,0xC6,0xC6,0x64,0x38,0x00,0x18,0x38,0x18,0x18,0x18,0x18,0x7E,0x00,
		0x7C,0xC6,0x0E,0x3C,0x78,0xE0,0xFE,0x00,0x7E,0x0C,0x18,0x3C,0x06,0xC6,0x7C,0x00,
		0x1C,0x3C,0x6C,0xCC,0xFE,0x0C,0x0C,0x00,0xFC,0xC0,0xFC,0x06,0x06,0xC6,0x7C,0x00,
		0x3C,0x60,0xC0,0xFC,0xC6,0xC6,0x7C,0x00,0xFE,0xC6,0x0C,0x18,0x30,0x30,0x30,0x00,
		0x78,0xC4,0xE4,0x78,0x86,0x86,0x7C,0x00,0x7C,0xC6,0xC6,0x7E,0x06,0x0C,0x78,0x00,
		0x00,0x00,0x18,0x00,0x00,0x18,0x00,0x00,0x00,0x00,0x18,0x00,0x00,0x18,0x18,0x30,
		0x1C,0x38,0x70,0xE0,0x70,0x38,0x1C,0x00,0x00,0x7C,0x00,0x00,0x7C,0x00,0x00,0x00,
		0x70,0x38,0x1C,0x0E,0x1C,0x38,0x70,0x00,0x7C,0xC6,0xC6,0x1C,0x18,0x00,0x18,0x00,
		0x3C,0x42,0x99,0xA1,0xA5,0x99,0x42,0x3C,0x38,0x6C,0xC6,0xC6,0xFE,0xC6,0xC6,0x00,
		0xFC,0xC6,0xC6,0xFC,0xC6,0xC6,0xFC,0x00,0x3C,0x66,0xC0,0xC0,0xC0,0x66,0x3C,0x00,
		0xF8,0xCC,0xC6,0xC6,0xC6,0xCC,0xF8,0x00,0xFE,0xC0,0xC0,0xFC,0xC0,0xC0,0xFE,0x00,
		0xFE,0xC0,0xC0,0xFC,0xC0,0xC0,0xC0,0x00,0x3E,0x60,0xC0,0xCE,0xC6,0x66,0x3E,0x00,
		0xC6,0xC6,0xC6,0xFE,0xC6,0xC6,0xC6,0x00,0x7E,0x18,0x18,0x18,0x18,0x18,0x7E,0x00,
		0x06,0x06,0x06,0x06,0xC6,0xC6,0x7C,0x00,0xC6,0xCC,0xD8,0xF0,0xF8,0xDC,0xCE,0x00,
		0x60,0x60,0x60,0x60,0x60,0x60,0x7E,0x00,0xC6,0xEE,0xFE,0xFE,0xD6,0xC6,0xC6,0x00,
		0xC6,0xE6,0xF6,0xFE,0xDE,0xCE,0xC6,0x00,0x7C,0xC6,0xC6,0xC6,0xC6,0xC6,0x7C,0x00,
		0xFC,0xC6,0xC6,0xC6,0xFC,0xC0,0xC0,0x00,0x7C,0xC6,0xC6,0xC6,0xDE,0xCC,0x7A,0x00,
		0xFC,0xC6,0xC6,0xCE,0xF8,0xDC,0xCE,0x00,0x78,0xCC,0xC0,0x7C,0x06,0xC6,0x7C,0x00,
		0x7E,0x18,0x18,0x18,0x18,0x18,0x18,0x00,0xC6,0xC6,0xC6,0xC6,0xC6,0xC6,0x7C,0x00,
		0xC6,0xC6,0xC6,0xEE,0x7C,0x38,0x10,0x00,0xC6,0xC6,0xD6,0xFE,0xFE,0xEE,0xC6,0x00,
		0xC6,0xEE,0x3C,0x38,0x7C,0xEE,0xC6,0x00,0x66,0x66,0x66,0x3C,0x18,0x18,0x18,0x00,
		0xFE,0x0E,0x1C,0x38,0x70,0xE0,0xFE,0x00,0x3C,0x30,0x30,0x30,0x30,0x30,0x3C,0x00,
		0x60,0x60,0x30,0x18,0x0C,0x06,0x06,0x00,0x3C,0x0C,0x0C,0x0C,0x0C,0x0C,0x3C,0x00,
		0x18,0x3C,0x66,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xFF,
		0x30,0x30,0x18,0x00,0x00,0x00,0x00,0x00,0x00,0x3C,0x06,0x3E,0x66,0x66,0x3C,0x00,
		0x60,0x7C,0x66,0x66,0x66,0x66,0x7C,0x00,0x00,0x3C,0x66,0x60,0x60,0x66,0x3C,0x00,
		0x06,0x3E,0x66,0x66,0x66,0x66,0x3E,0x00,0x00,0x3C,0x66,0x66,0x7E,0x60,0x3C,0x00,
		0x1C,0x30,0x78,0x30,0x30,0x30,0x30,0x00,0x00,0x3E,0x66,0x66,0x66,0x3E,0x06,0x3C,
		0x60,0x7C,0x76,0x66,0x66,0x66,0x66,0x00,0x18,0x00,0x38,0x18,0x18,0x18,0x18,0x00,
		0x0C,0x00,0x1C,0x0C,0x0C,0x0C,0x0C,0x38,0x60,0x60,0x66,0x6C,0x78,0x6C,0x66,0x00,
		0x38,0x18,0x18,0x18,0x18,0x18,0x18,0x00,0x00,0xEC,0xFE,0xFE,0xFE,0xD6,0xC6,0x00,
		0x00,0x7C,0x76,0x66,0x66,0x66,0x66,0x00,0x00,0x3C,0x66,0x66,0x66,0x66,0x3C,0x00,
		0x00,0x7C,0x66,0x66,0x66,0x7C,0x60,0x60,0x00,0x3E,0x66,0x66,0x66,0x3E,0x06,0x06,
		0x00,0x7E,0x70,0x60,0x60,0x60,0x60,0x00,0x00,0x3C,0x60,0x3C,0x06,0x66,0x3C,0x00,
		0x30,0x78,0x30,0x30,0x30,0x30,0x1C,0x00,0x00,0x66,0x66,0x66,0x66,0x6E,0x3E,0x00,
		0x00,0x66,0x66,0x66,0x66,0x3C,0x18,0x00,0x00,0xC6,0xD6,0xFE,0xFE,0x7C,0x6C,0x00,
		0x00,0x66,0x3C,0x18,0x3C,0x66,0x66,0x00,0x00,0x66,0x66,0x66,0x66,0x3E,0x06,0x3C,
		0x00,0x7E,0x0C,0x18,0x30,0x60,0x7E,0x00,0x0E,0x18,0x0C,0x38,0x0C,0x18,0x0E,0x00,
		0x18,0x18,0x18,0x00,0x18,0x18,0x18,0x00,0x70,0x18,0x30,0x1C,0x30,0x18,0x70,0x00,
		0x00,0x00,0x76,0xDC,0x00,0x00,0x00,0x00,0x10,0x28,0x10,0x54,0xAA,0x44,0x00,0x00,
	};
#endif
	static struct GfxLayout fontlayout6x8 =
	{
		6,8,	/* 6*8 characters */
		128,	/* 128 characters */
		1,	/* 1 bit per pixel */
		{ 0 },
		{ 0, 1, 2, 3, 4, 5, 6, 7 }, /* straightforward layout */
		{ 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8 /* every char takes 8 consecutive bytes */
	};
	static struct GfxLayout fontlayout12x8 =
	{
		12,8,	/* 12*8 characters */
		128,	/* 128 characters */
		1,	/* 1 bit per pixel */
		{ 0 },
		{ 0,0, 1,1, 2,2, 3,3, 4,4, 5,5, 6,6, 7,7 }, /* straightforward layout */
		{ 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8 /* every char takes 8 consecutive bytes */
	};
	static struct GfxLayout fontlayout12x16 =
	{
		12,16,	/* 6*8 characters */
		128,	/* 128 characters */
		1,	/* 1 bit per pixel */
		{ 0 },
		{ 0,0, 1,1, 2,2, 3,3, 4,4, 5,5, 6,6, 7,7 }, /* straightforward layout */
		{ 0*8,0*8, 1*8,1*8, 2*8,2*8, 3*8,3*8, 4*8,4*8, 5*8,5*8, 6*8,6*8, 7*8,7*8 },
		8*8 /* every char takes 8 consecutive bytes */
	};
#if 0	/* HJB 990215 unused!? */
	static struct GfxLayout fontlayout8x8 =
	{
		8,8,	/* 8*8 characters */
		128,	/* 128 characters */
		1,	/* 1 bit per pixel */
		{ 0 },
		{ 0, 1, 2, 3, 4, 5, 6, 7 }, /* straightforward layout */
		{ 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8 /* every char takes 8 consecutive bytes */
	};
#endif
	struct GfxElement *font;
	static unsigned short colortable[2*2];	/* ASG 980209 */
	int trueorientation;


	/* hack: force the display into standard orientation to avoid */
	/* creating a rotated font */
	trueorientation = Machine->orientation;
	Machine->orientation = Machine->ui_orientation;

	if ((Machine->drv->video_attributes & VIDEO_PIXEL_ASPECT_RATIO_MASK)
			== VIDEO_PIXEL_ASPECT_RATIO_1_2)
	{
		font = decodegfx(fontdata6x8,&fontlayout12x8);
		Machine->uifontwidth = 12;
		Machine->uifontheight = 8;
	}
	else if (Machine->uiwidth >= 420 && Machine->uiheight >= 420)
	{
		font = decodegfx(fontdata6x8,&fontlayout12x16);
		Machine->uifontwidth = 12;
		Machine->uifontheight = 16;
	}
	else
	{
		font = decodegfx(fontdata6x8,&fontlayout6x8);
		Machine->uifontwidth = 6;
		Machine->uifontheight = 8;
	}

	if (font)
	{
		/* colortable will be set at run time */
		memset(colortable,0,sizeof(colortable));
		font->colortable = colortable;
		font->total_colors = 2;
	}

	Machine->orientation = trueorientation;

	return font;
}



/***************************************************************************

  Display text on the screen. If erase is 0, it superimposes the text on
  the last frame displayed.

***************************************************************************/

void displaytext(const struct DisplayText *dt,int erase,int update_screen)
{
	int trueorientation;


	if (erase)
		osd_clearbitmap(Machine->scrbitmap);


	/* hack: force the display into standard orientation to avoid */
	/* rotating the user interface */
	trueorientation = Machine->orientation;
	Machine->orientation = Machine->ui_orientation;

	osd_mark_dirty (0,0,Machine->uiwidth-1,Machine->uiheight-1,1);	/* ASG 971011 */

	while (dt->text)
	{
		int x,y;
		const char *c;


		x = dt->x;
		y = dt->y;
		c = dt->text;

		while (*c)
		{
			int wrapped;


			wrapped = 0;

			if (*c == '\n')
			{
				x = dt->x;
				y += Machine->uifontheight + 1;
				wrapped = 1;
			}
			else if (*c == ' ')
			{
				/* don't try to word wrap at the beginning of a line (this would cause */
				/* an endless loop if a word is longer than a line) */
				if (x != dt->x)
				{
					int nextlen=0;
					const char *nc;


					nc = c+1;
					while (*nc && *nc != ' ' && *nc != '\n')
					{
						nextlen += Machine->uifontwidth;
						nc++;
					}

					/* word wrap */
					if (x + Machine->uifontwidth + nextlen > Machine->uiwidth)
					{
						x = dt->x;
						y += Machine->uifontheight + 1;
						wrapped = 1;
					}
				}
			}

			if (!wrapped)
			{
				drawgfx(Machine->scrbitmap,Machine->uifont,*c,dt->color,0,0,x+Machine->uixmin,y+Machine->uiymin,0,TRANSPARENCY_NONE,0);
				x += Machine->uifontwidth;
			}

			c++;
		}

		dt++;
	}

	Machine->orientation = trueorientation;

	if (update_screen) osd_update_video_and_audio();
}

/* Writes messages on the screen. */
static void ui_text_ex(const char* buf_begin, const char* buf_end, int x, int y, int color)
{
	int trueorientation;

	/* hack: force the display into standard orientation to avoid */
	/* rotating the text */
	trueorientation = Machine->orientation;
	Machine->orientation = Machine->ui_orientation;

	for (;buf_begin != buf_end; ++buf_begin)
	{
		drawgfx(Machine->scrbitmap,Machine->uifont,*buf_begin,color,0,0,
				x + Machine->uixmin,
				y + Machine->uiymin, 0,TRANSPARENCY_NONE,0);
		x += Machine->uifontwidth;
	}

	Machine->orientation = trueorientation;
}

/* Writes messages on the screen. */
void ui_text(const char *buf,int x,int y)
{
	ui_text_ex(buf, buf + strlen(buf), x, y, DT_COLOR_WHITE);
}

INLINE void drawpixel(int x, int y, unsigned short color)
{
	int temp;

	if (Machine->ui_orientation & ORIENTATION_SWAP_XY)
	{
		temp = x; x = y; y = temp;
	}
	if (Machine->ui_orientation & ORIENTATION_FLIP_X)
		x = Machine->scrbitmap->width - x - 1;
	if (Machine->ui_orientation & ORIENTATION_FLIP_Y)
		y = Machine->scrbitmap->height - y - 1;

	if (Machine->scrbitmap->depth == 16)
		*(unsigned short *)&Machine->scrbitmap->line[y][x*2] = color;
	else
		Machine->scrbitmap->line[y][x] = color;

	osd_mark_dirty(x,y,x,y,1);
}

INLINE void drawhline_norotate(int x, int w, int y, unsigned short color)
{
	if (Machine->scrbitmap->depth == 16)
	{
		int i;
		for (i = x; i < x+w; i++)
			*(unsigned short *)&Machine->scrbitmap->line[y][i*2] = color;
	}
	else
		memset(&Machine->scrbitmap->line[y][x], color, w);

	osd_mark_dirty(x,y,x+w-1,y,1);
}

INLINE void drawvline_norotate(int x, int y, int h, unsigned short color)
{
	int i;

	if (Machine->scrbitmap->depth == 16)
	{
		for (i = y; i < y+h; i++)
			*(unsigned short *)&Machine->scrbitmap->line[i][x*2] = color;
	}
	else
	{
		for (i = y; i < y+h; i++)
			Machine->scrbitmap->line[i][x] = color;
	}

	osd_mark_dirty(x,y,x,y+h-1,1);
}

INLINE void drawhline(int x, int w, int y, unsigned short color)
{
	if (Machine->ui_orientation & ORIENTATION_SWAP_XY)
	{
		if (Machine->ui_orientation & ORIENTATION_FLIP_X)
			y = Machine->scrbitmap->width - y - 1;
		if (Machine->ui_orientation & ORIENTATION_FLIP_Y)
			x = Machine->scrbitmap->height - x - w;

		drawvline_norotate(y,x,w,color);
	}
	else
	{
		if (Machine->ui_orientation & ORIENTATION_FLIP_X)
			x = Machine->scrbitmap->width - x - w;
		if (Machine->ui_orientation & ORIENTATION_FLIP_Y)
			y = Machine->scrbitmap->height - y - 1;

		drawhline_norotate(x,w,y,color);
	}
}

INLINE void drawvline(int x, int y, int h, unsigned short color)
{
	if (Machine->ui_orientation & ORIENTATION_SWAP_XY)
	{
		if (Machine->ui_orientation & ORIENTATION_FLIP_X)
			y = Machine->scrbitmap->width - y - h;
		if (Machine->ui_orientation & ORIENTATION_FLIP_Y)
			x = Machine->scrbitmap->height - x - 1;

		drawhline_norotate(y,h,x,color);
	}
	else
	{
		if (Machine->ui_orientation & ORIENTATION_FLIP_X)
			x = Machine->scrbitmap->width - x - 1;
		if (Machine->ui_orientation & ORIENTATION_FLIP_Y)
			y = Machine->scrbitmap->height - y - h;

		drawvline_norotate(x,y,h,color);
	}
}


void ui_drawbox(int leftx,int topy,int width,int height)
{
	int y;
	unsigned short black,white;


	if (leftx < 0) leftx = 0;
	if (topy < 0) topy = 0;
	if (width > Machine->uiwidth) width = Machine->uiwidth;
	if (height > Machine->uiheight) height = Machine->uiheight;

	leftx += Machine->uixmin;
	topy += Machine->uiymin;

	black = Machine->uifont->colortable[0];
	white = Machine->uifont->colortable[1];

	drawhline(leftx,width,topy, 		white);
	drawhline(leftx,width,topy+height-1,white);
	drawvline(leftx,		topy,height,white);
	drawvline(leftx+width-1,topy,height,white);
	for (y = topy+1;y < topy+height-1;y++)
		drawhline(leftx+1,width-2,y,black);
}


static void drawbar(int leftx,int topy,int width,int height,int percentage,int default_percentage)
{
	int y;
	unsigned short black,white;


	if (leftx < 0) leftx = 0;
	if (topy < 0) topy = 0;
	if (width > Machine->uiwidth) width = Machine->uiwidth;
	if (height > Machine->uiheight) height = Machine->uiheight;

	leftx += Machine->uixmin;
	topy += Machine->uiymin;

	black = Machine->uifont->colortable[0];
	white = Machine->uifont->colortable[1];

	for (y = topy;y < topy + height/8;y++)
		drawpixel(leftx+(width-1)*default_percentage/100, y, white);

	drawhline(leftx,width,topy+height/8,white);

	for (y = topy+height/8;y < topy+height-height/8;y++)
		drawhline(leftx,1+(width-1)*percentage/100,y,white);

	drawhline(leftx,width,topy+height-height/8-1,white);

	for (y = topy+height-height/8;y < topy + height;y++)
		drawpixel(leftx+(width-1)*default_percentage/100, y, white);
}

/* Extract one line from a multiline buffer */
/* Return the characters number of the line, pbegin point to the start of the next line */
static unsigned multiline_extract(const char** pbegin, const char* end, unsigned max)
{
	unsigned mac = 0;
	const char* begin = *pbegin;
	while (begin != end && mac < max)
	{
		if (*begin == '\n')
		{
			*pbegin = begin + 1; /* strip final space */
			return mac;
		}
		else if (*begin == ' ')
		{
			const char* word_end = begin + 1;
			while (word_end != end && *word_end != ' ' && *word_end != '\n')
				++word_end;
			if (mac + word_end - begin > max)
			{
				if (mac)
				{
					*pbegin = begin + 1;
					return mac; /* strip final space */
				} else {
					*pbegin = begin + max;
					return max;
				}
			}
			mac += word_end - begin;
			begin = word_end;
		} else {
			++mac;
			++begin;
		}
	}
	if (begin != end && (*begin == '\n' || *begin == ' '))
		++begin;
	*pbegin = begin;
	return mac;
}

/* Compute the output size of a multiline string */
static void multiline_size(int* dx, int* dy, const char* begin, const char* end, unsigned max)
{
	unsigned rows = 0;
	unsigned cols = 0;
	while (begin != end)
	{
		unsigned len;
		len = multiline_extract(&begin,end,max);
		if (len > cols)
			cols = len;
		++rows;
	}
	*dx = cols * Machine->uifontwidth;
	*dy = (rows-1) * 3*Machine->uifontheight/2 + Machine->uifontheight;
}

/* Compute the output size of a multiline string with box */
static void multilinebox_size(int* dx, int* dy, const char* begin, const char* end, unsigned max)
{
	multiline_size(dx,dy,begin,end,max);
	*dx += Machine->uifontwidth;
	*dy += Machine->uifontheight;
}

/* Display a multiline string */
static void ui_multitext_ex(const char* begin, const char* end, unsigned max, int x, int y, int color)
{
	while (begin != end)
	{
		const char* line_begin = begin;
		unsigned len = multiline_extract(&begin,end,max);
		ui_text_ex(line_begin, line_begin + len,x,y,color);
		y += 3*Machine->uifontheight/2;
	}
}

/* Display a multiline string with box */
static void ui_multitextbox_ex(const char* begin, const char* end, unsigned max, int x, int y, int dx, int dy, int color)
{
	ui_drawbox(x,y,dx,dy);
	x += Machine->uifontwidth/2;
	y += Machine->uifontheight/2;
	ui_multitext_ex(begin,end,max,x,y,color);
}

void ui_displaymenu(const char **items,const char **subitems,char *flag,int selected,int arrowize_subitem)
{
	struct DisplayText dt[256];
	int curr_dt;
	char lefthilight[2] = "\x1a";
	char righthilight[2] = "\x1b";
	char uparrow[2] = "\x18";
	char downarrow[2] = "\x19";
	char leftarrow[2] = "\x11";
	char rightarrow[2] = "\x10";
	int i,count,len,maxlen,highlen;
	int leftoffs,topoffs,visible,topitem;
	int selected_long;


	i = 0;
	maxlen = 0;
	highlen = Machine->uiwidth / Machine->uifontwidth;
	while (items[i])
	{
		len = 3 + strlen(items[i]);
		if (subitems && subitems[i])
			len += 2 + strlen(subitems[i]);
		if (len > maxlen && len <= highlen)
			maxlen = len;
		i++;
	}
	count = i;

	visible = Machine->uiheight / (3 * Machine->uifontheight / 2) - 1;
	topitem = 0;
	if (visible > count) visible = count;
	else
	{
		topitem = selected - visible / 2;
		if (topitem < 0) topitem = 0;
		if (topitem > count - visible) topitem = count - visible;
	}

	leftoffs = (Machine->uiwidth - maxlen * Machine->uifontwidth) / 2;
	topoffs = (Machine->uiheight - (3 * visible + 1) * Machine->uifontheight / 2) / 2;

	/* black background */
	ui_drawbox(leftoffs,topoffs,maxlen * Machine->uifontwidth,(3 * visible + 1) * Machine->uifontheight / 2);

	selected_long = 0;
	curr_dt = 0;
	for (i = 0;i < visible;i++)
	{
		int item = i + topitem;

		if (i == 0 && item > 0)
		{
			dt[curr_dt].text = uparrow;
			dt[curr_dt].color = DT_COLOR_WHITE;
			dt[curr_dt].x = (Machine->uiwidth - Machine->uifontwidth * strlen(uparrow)) / 2;
			dt[curr_dt].y = topoffs + (3*i+1)*Machine->uifontheight/2;
			curr_dt++;
		}
		else if (i == visible - 1 && item < count - 1)
		{
			dt[curr_dt].text = downarrow;
			dt[curr_dt].color = DT_COLOR_WHITE;
			dt[curr_dt].x = (Machine->uiwidth - Machine->uifontwidth * strlen(downarrow)) / 2;
			dt[curr_dt].y = topoffs + (3*i+1)*Machine->uifontheight/2;
			curr_dt++;
		}
		else
		{
			if (subitems && subitems[item])
			{
				int sublen;
				len = strlen(items[item]);
				dt[curr_dt].text = items[item];
				dt[curr_dt].color = DT_COLOR_WHITE;
				dt[curr_dt].x = leftoffs + 3*Machine->uifontwidth/2;
				dt[curr_dt].y = topoffs + (3*i+1)*Machine->uifontheight/2;
				curr_dt++;
				sublen = strlen(subitems[item]);
				if (sublen > maxlen-5-len)
				{
					dt[curr_dt].text = "...";
					sublen = strlen(dt[curr_dt].text);
					if (item == selected)
						selected_long = 1;
				} else {
					dt[curr_dt].text = subitems[item];
				}
				/* If this item is flagged, draw it in inverse print */
				dt[curr_dt].color = (flag && flag[item]) ? DT_COLOR_YELLOW : DT_COLOR_WHITE;
				dt[curr_dt].x = leftoffs + Machine->uifontwidth * (maxlen-1-sublen) - Machine->uifontwidth/2;
				dt[curr_dt].y = topoffs + (3*i+1)*Machine->uifontheight/2;
				curr_dt++;
			}
			else
			{
				dt[curr_dt].text = items[item];
				dt[curr_dt].color = DT_COLOR_WHITE;
				dt[curr_dt].x = (Machine->uiwidth - Machine->uifontwidth * strlen(items[item])) / 2;
				dt[curr_dt].y = topoffs + (3*i+1)*Machine->uifontheight/2;
				curr_dt++;
			}
		}
	}

	i = selected - topitem;
	if (subitems && subitems[selected] && arrowize_subitem)
	{
		if (arrowize_subitem & 1)
		{
			dt[curr_dt].text = leftarrow;
			dt[curr_dt].color = DT_COLOR_WHITE;
			dt[curr_dt].x = leftoffs + Machine->uifontwidth * (maxlen-2 - strlen(subitems[selected])) - Machine->uifontwidth/2 - 1;
			dt[curr_dt].y = topoffs + (3*i+1)*Machine->uifontheight/2;
			curr_dt++;
		}
		if (arrowize_subitem & 2)
		{
			dt[curr_dt].text = rightarrow;
			dt[curr_dt].color = DT_COLOR_WHITE;
			dt[curr_dt].x = leftoffs + Machine->uifontwidth * (maxlen-1) - Machine->uifontwidth/2;
			dt[curr_dt].y = topoffs + (3*i+1)*Machine->uifontheight/2;
			curr_dt++;
		}
	}
	else
	{
		dt[curr_dt].text = righthilight;
		dt[curr_dt].color = DT_COLOR_WHITE;
		dt[curr_dt].x = leftoffs + Machine->uifontwidth * (maxlen-1) - Machine->uifontwidth/2;
		dt[curr_dt].y = topoffs + (3*i+1)*Machine->uifontheight/2;
		curr_dt++;
	}
	dt[curr_dt].text = lefthilight;
	dt[curr_dt].color = DT_COLOR_WHITE;
	dt[curr_dt].x = leftoffs + Machine->uifontwidth/2;
	dt[curr_dt].y = topoffs + (3*i+1)*Machine->uifontheight/2;
	curr_dt++;

	dt[curr_dt].text = 0;	/* terminate array */

	displaytext(dt,0,0);

	if (selected_long)
	{
		int long_dx;
		int long_dy;
		int long_x;
		int long_y;
		unsigned long_max;

		long_max = (Machine->uiwidth / Machine->uifontwidth) - 2;
		multilinebox_size(&long_dx,&long_dy,subitems[selected],subitems[selected] + strlen(subitems[selected]), long_max);

		long_x = Machine->uiwidth - long_dx;
		long_y = topoffs + (i+1) * 3*Machine->uifontheight/2;

		/* if too low display up */
		if (long_y + long_dy > Machine->uiheight)
			long_y = topoffs + i * 3*Machine->uifontheight/2 - long_dy;

		ui_multitextbox_ex(subitems[selected],subitems[selected] + strlen(subitems[selected]), long_max, long_x,long_y,long_dx,long_dy, DT_COLOR_WHITE);
	}
}


void ui_displaymessagewindow(const char *text)
{
	struct DisplayText dt[256];
	int curr_dt;
	char *c,*c2;
	int i,len,maxlen,lines;
	char textcopy[2048];
	int leftoffs,topoffs;
	int maxcols,maxrows;

	maxcols = (Machine->uiwidth / Machine->uifontwidth) - 1;
	maxrows = (2 * Machine->uiheight - Machine->uifontheight) / (3 * Machine->uifontheight);

	/* copy text, calculate max len, count lines, wrap long lines and crop height to fit */
	maxlen = 0;
	lines = 0;
	c = (char *)text;
	c2 = textcopy;
	while (*c)
	{
		len = 0;
		while (*c && *c != '\n')
		{
			*c2++ = *c++;
			len++;
			if (len == maxcols && *c != '\n')
			{
				/* attempt word wrap */
				char *csave = c, *c2save = c2;
				int lensave = len;

				/* back up to last space or beginning of line */
				while (*c != ' ' && *c != '\n' && c > text)
					--c, --c2, --len;

				/* if no space was found, hard wrap instead */
				if (*c != ' ')
					c = csave, c2 = c2save, len = lensave;
				else
					c++;

				*c2++ = '\n'; /* insert wrap */
				break;
			}
		}

		if (*c == '\n')
			*c2++ = *c++;

		if (len > maxlen) maxlen = len;

		lines++;
		if (lines == maxrows)
			break;
	}
	*c2 = '\0';

	maxlen += 1;

	leftoffs = (Machine->uiwidth - Machine->uifontwidth * maxlen) / 2;
	if (leftoffs < 0) leftoffs = 0;
	topoffs = (Machine->uiheight - (3 * lines + 1) * Machine->uifontheight / 2) / 2;

	/* black background */
	ui_drawbox(leftoffs,topoffs,maxlen * Machine->uifontwidth,(3 * lines + 1) * Machine->uifontheight / 2);

	curr_dt = 0;
	c = textcopy;
	i = 0;
	while (*c)
	{
		c2 = c;
		while (*c && *c != '\n')
			c++;

		if (*c == '\n')
		{
			*c = '\0';
			c++;
		}

		if (*c2 == '\t')    /* center text */
		{
			c2++;
			dt[curr_dt].x = (Machine->uiwidth - Machine->uifontwidth * (c - c2)) / 2;
		}
		else
			dt[curr_dt].x = leftoffs + Machine->uifontwidth/2;

		dt[curr_dt].text = c2;
		dt[curr_dt].color = DT_COLOR_WHITE;
		dt[curr_dt].y = topoffs + (3*i+1)*Machine->uifontheight/2;
		curr_dt++;

		i++;
	}

	dt[curr_dt].text = 0;	/* terminate array */

	displaytext(dt,0,0);
}



#ifndef NEOFREE
#ifndef TINY_COMPILE
extern int no_of_tiles;
void NeoMVSDrawGfx(unsigned char **line,const struct GfxElement *gfx,
		unsigned int code,unsigned int color,int flipx,int flipy,int sx,int sy,
		int zx,int zy,const struct rectangle *clip);
void NeoMVSDrawGfx16(unsigned char **line,const struct GfxElement *gfx,
		unsigned int code,unsigned int color,int flipx,int flipy,int sx,int sy,
		int zx,int zy,const struct rectangle *clip);
extern struct GameDriver driver_neogeo;
#endif
#endif

static void showcharset(void)
{
	int i;
	char buf[80];
	int bank,color,firstdrawn;
	int palpage;
	int trueorientation;
	int changed;
	int game_is_neogeo=0;
	unsigned char *orig_used_colors=0;


	if (palette_used_colors)
	{
		orig_used_colors = malloc(Machine->drv->total_colors * sizeof(unsigned char));
		if (!orig_used_colors) return;

		memcpy(orig_used_colors,palette_used_colors,Machine->drv->total_colors * sizeof(unsigned char));
	}

#ifndef NEOFREE
#ifndef TINY_COMPILE
	if (Machine->gamedrv->clone_of == &driver_neogeo ||
			(Machine->gamedrv->clone_of &&
				Machine->gamedrv->clone_of->clone_of == &driver_neogeo))
		game_is_neogeo=1;
#endif
#endif

	bank = -1;
	color = 0;
	firstdrawn = 0;
	palpage = 0;

	changed = 1;

	do
	{
		int cpx,cpy,skip_chars;

		if (bank >= 0)
		{
			cpx = Machine->uiwidth / Machine->gfx[bank]->width;
			cpy = (Machine->uiheight - Machine->uifontheight) / Machine->gfx[bank]->height;
			skip_chars = cpx * cpy;
		}
		else cpx = cpy = skip_chars = 0;

		if (changed)
		{
			int lastdrawn=0;

			osd_clearbitmap(Machine->scrbitmap);

			/* validity chack after char bank change */
			if (bank >= 0)
			{
				if (firstdrawn >= Machine->gfx[bank]->total_elements)
				{
					firstdrawn = Machine->gfx[bank]->total_elements - skip_chars;
					if (firstdrawn < 0) firstdrawn = 0;
				}
			}

			if(bank!=2 || !game_is_neogeo)
			{
				if (bank >= 0)
				{
					int table_offs;
					int flipx,flipy;

					/* hack: force the display into standard orientation to avoid */
					/* rotating the user interface */
					trueorientation = Machine->orientation;
					Machine->orientation = Machine->ui_orientation;

					if (palette_used_colors)
					{
						memset(palette_used_colors,PALETTE_COLOR_TRANSPARENT,Machine->drv->total_colors * sizeof(unsigned char));
						table_offs = Machine->gfx[bank]->colortable - Machine->remapped_colortable
								+ Machine->gfx[bank]->color_granularity * color;
						for (i = 0;i < Machine->gfx[bank]->color_granularity;i++)
							palette_used_colors[Machine->game_colortable[table_offs + i]] = PALETTE_COLOR_USED;
						palette_recalc();	/* do it twice in case of previous overflow */
						palette_recalc();	/*(we redraw the screen only when it changes) */
					}

#ifndef PREROTATE_GFX
					flipx = (Machine->orientation ^ trueorientation) & ORIENTATION_FLIP_X;
					flipy = (Machine->orientation ^ trueorientation) & ORIENTATION_FLIP_Y;

					if (Machine->orientation & ORIENTATION_SWAP_XY)
					{
						int t;
						t = flipx; flipx = flipy; flipy = t;
					}
#else
					flipx = 0;
					flipy = 0;
#endif

					for (i = 0; i+firstdrawn < Machine->gfx[bank]->total_elements && i<cpx*cpy; i++)
					{
						drawgfx(Machine->scrbitmap,Machine->gfx[bank],
								i+firstdrawn,color,  /*sprite num, color*/
								flipx,flipy,
								(i % cpx) * Machine->gfx[bank]->width + Machine->uixmin,
								Machine->uifontheight + (i / cpx) * Machine->gfx[bank]->height + Machine->uiymin,
								0,TRANSPARENCY_NONE,0);

						lastdrawn = i+firstdrawn;
					}

					Machine->orientation = trueorientation;
				}
				else
				{
					int sx,sy,x,y,colors;

					colors = Machine->drv->total_colors - 256 * palpage;
					if (colors > 256) colors = 256;
					if (palette_used_colors)
					{
						memset(palette_used_colors,PALETTE_COLOR_UNUSED,Machine->drv->total_colors * sizeof(unsigned char));
						memset(palette_used_colors+256*palpage,PALETTE_COLOR_USED,colors * sizeof(unsigned char));
						palette_recalc();	/* do it twice in case of previous overflow */
						palette_recalc();	/*(we redraw the screen only when it changes) */
					}

					for (i = 0;i < 16;i++)
					{
						char bf[40];

						sx = 3*Machine->uifontwidth + (Machine->uifontwidth*4/3)*(i % 16);
						sprintf(bf,"%X",i);
						ui_text(bf,sx,2*Machine->uifontheight);
						if (16*i < colors)
						{
							sy = 3*Machine->uifontheight + (Machine->uifontheight)*(i % 16);
							sprintf(bf,"%3X",i+16*palpage);
							ui_text(bf,0,sy);
						}
					}

					for (i = 0;i < colors;i++)
					{
						sx = Machine->uixmin + 3*Machine->uifontwidth + (Machine->uifontwidth*4/3)*(i % 16);
						sy = Machine->uiymin + 2*Machine->uifontheight + (Machine->uifontheight)*(i / 16) + Machine->uifontheight;
						for (y = 0;y < Machine->uifontheight;y++)
						{
							for (x = 0;x < Machine->uifontwidth*4/3;x++)
							{
								int tx,ty;
								if (Machine->ui_orientation & ORIENTATION_SWAP_XY)
								{
									ty = sx + x;
									tx = sy + y;
								}
								else
								{
									tx = sx + x;
									ty = sy + y;
								}
								if (Machine->ui_orientation & ORIENTATION_FLIP_X)
									tx = Machine->scrbitmap->width-1 - tx;
								if (Machine->ui_orientation & ORIENTATION_FLIP_Y)
									ty = Machine->scrbitmap->height-1 - ty;

								if (Machine->scrbitmap->depth == 16)
									((unsigned short *)Machine->scrbitmap->line[ty])[tx]
											= Machine->pens[i + 256*palpage];
								else
									Machine->scrbitmap->line[ty][tx]
											= Machine->pens[i + 256*palpage];
							}
						}
					}
				}
			}
#ifndef NEOFREE
#ifndef TINY_COMPILE
			else	/* neogeo sprite tiles */
			{
				struct rectangle clip;

				clip.min_x = Machine->uixmin;
				clip.max_x = Machine->uixmin + Machine->uiwidth - 1;
				clip.min_y = Machine->uiymin;
				clip.max_y = Machine->uiymin + Machine->uiheight - 1;

				if (palette_used_colors)
				{
					memset(palette_used_colors,PALETTE_COLOR_TRANSPARENT,Machine->drv->total_colors * sizeof(unsigned char));
					memset(palette_used_colors+Machine->gfx[bank]->color_granularity*color,PALETTE_COLOR_USED,Machine->gfx[bank]->color_granularity * sizeof(unsigned char));
					palette_recalc();	/* do it twice in case of previous overflow */
					palette_recalc();	/*(we redraw the screen only when it changes) */
				}

				for (i = 0; i+firstdrawn < no_of_tiles && i<cpx*cpy; i++)
				{
					if (Machine->scrbitmap->depth == 16)
						NeoMVSDrawGfx16(Machine->scrbitmap->line,Machine->gfx[bank],
							i+firstdrawn,color,  /*sprite num, color*/
							0,0,
							(i % cpx) * Machine->gfx[bank]->width + Machine->uixmin,
							Machine->uifontheight+1 + (i / cpx) * Machine->gfx[bank]->height + Machine->uiymin,
							16,16,&clip);
					else
						NeoMVSDrawGfx(Machine->scrbitmap->line,Machine->gfx[bank],
							i+firstdrawn,color,  /*sprite num, color*/
							0,0,
							(i % cpx) * Machine->gfx[bank]->width + Machine->uixmin,
							Machine->uifontheight+1 + (i / cpx) * Machine->gfx[bank]->height + Machine->uiymin,
							16,16,&clip);

					lastdrawn = i+firstdrawn;
				}
			}
#endif
#endif

			if (bank >= 0)
				sprintf(buf,"GFXSET %d COLOR %2X CODE %X-%X",bank,color,firstdrawn,lastdrawn);
			else
				strcpy(buf,"PALETTE");
			ui_text(buf,0,0);

			changed = 0;
		}

		/* Necessary to keep the video from getting stuck if a frame happens to be skipped in here */
/* I beg to differ - the OS dependant code must not assume that */
/* osd_skip_this_frame() is called before osd_update_video_and_audio() - NS */
//		osd_skip_this_frame();
		osd_update_video_and_audio();

		if (code_pressed(KEYCODE_LCONTROL) || code_pressed(KEYCODE_RCONTROL))
		{
			skip_chars = cpx;
		}
		if (code_pressed(KEYCODE_LSHIFT) || code_pressed(KEYCODE_RSHIFT))
		{
			skip_chars = 1;
		}


		if (input_ui_pressed_repeat(IPT_UI_RIGHT,8))
		{
			if (bank+1 < MAX_GFX_ELEMENTS && Machine->gfx[bank + 1])
			{
				bank++;
//				firstdrawn = 0;
				changed = 1;
			}
		}

		if (input_ui_pressed_repeat(IPT_UI_LEFT,8))
		{
			if (bank > -1)
			{
				bank--;
//				firstdrawn = 0;
				changed = 1;
			}
		}

		if (code_pressed_memory_repeat(KEYCODE_PGDN,4))
		{
			if (bank >= 0)
			{
				if (firstdrawn + skip_chars < Machine->gfx[bank]->total_elements)
				{
					firstdrawn += skip_chars;
					changed = 1;
				}
			}
			else
			{
				if (256 * (palpage + 1) < Machine->drv->total_colors)
				{
					palpage++;
					changed = 1;
				}
			}
		}

		if (code_pressed_memory_repeat(KEYCODE_PGUP,4))
		{
			if (bank >= 0)
			{
				firstdrawn -= skip_chars;
				if (firstdrawn < 0) firstdrawn = 0;
				changed = 1;
			}
			else
			{
				if (palpage > 0)
				{
					palpage--;
					changed = 1;
				}
			}
		}

		if (input_ui_pressed_repeat(IPT_UI_UP,6))
		{
			if (bank >= 0)
			{
				if (color < Machine->gfx[bank]->total_colors - 1)
				{
					color++;
					changed = 1;
				}
			}
		}

		if (input_ui_pressed_repeat(IPT_UI_DOWN,6))
		{
			if (color > 0)
			{
				color--;
				changed = 1;
			}
		}

		if (input_ui_pressed(IPT_UI_SNAPSHOT))
			osd_save_snapshot();
	} while (!input_ui_pressed(IPT_UI_SHOW_GFX) &&
			!input_ui_pressed(IPT_UI_CANCEL));

	/* clear the screen before returning */
	osd_clearbitmap(Machine->scrbitmap);

	if (palette_used_colors)
	{
		/* this should force a full refresh by the video driver */
		memset(palette_used_colors,PALETTE_COLOR_TRANSPARENT,Machine->drv->total_colors * sizeof(unsigned char));
		palette_recalc();
		/* restore the game used colors array */
		memcpy(palette_used_colors,orig_used_colors,Machine->drv->total_colors * sizeof(unsigned char));
		free(orig_used_colors);
	}

	return;
}


#ifdef MAME_DEBUG
static void showtotalcolors(void)
{
	char *used;
	int i,l,x,y,total;
	unsigned char r,g,b;
	char buf[40];
	int trueorientation;


	used = malloc(64*64*64);
	if (!used) return;

	for (i = 0;i < 64*64*64;i++)
		used[i] = 0;

	if (Machine->scrbitmap->depth == 16)
	{
		for (y = 0;y < Machine->scrbitmap->height;y++)
		{
			for (x = 0;x < Machine->scrbitmap->width;x++)
			{
				osd_get_pen(((unsigned short *)Machine->scrbitmap->line[y])[x],&r,&g,&b);
				r >>= 2;
				g >>= 2;
				b >>= 2;
				used[64*64*r+64*g+b] = 1;
			}
		}
	}
	else
	{
		for (y = 0;y < Machine->scrbitmap->height;y++)
		{
			for (x = 0;x < Machine->scrbitmap->width;x++)
			{
				osd_get_pen(Machine->scrbitmap->line[y][x],&r,&g,&b);
				r >>= 2;
				g >>= 2;
				b >>= 2;
				used[64*64*r+64*g+b] = 1;
			}
		}
	}

	total = 0;
	for (i = 0;i < 64*64*64;i++)
		if (used[i]) total++;

	/* hack: force the display into standard orientation to avoid */
	/* rotating the text */
	trueorientation = Machine->orientation;
	Machine->orientation = Machine->ui_orientation;

	sprintf(buf,"%5d colors",total);
	l = strlen(buf);
	for (i = 0;i < l;i++)
		drawgfx(Machine->scrbitmap,Machine->uifont,buf[i],total>256?DT_COLOR_YELLOW:DT_COLOR_WHITE,0,0,Machine->uixmin+i*Machine->uifontwidth,Machine->uiymin,0,TRANSPARENCY_NONE,0);

	Machine->orientation = trueorientation;

	free(used);
}
#endif


static int setdipswitches(int selected)
{
	const char *menu_item[128];
	const char *menu_subitem[128];
	struct InputPort *entry[128];
	char flag[40];
	int i,sel;
	struct InputPort *in;
	int total;
	int arrowize;


	sel = selected - 1;


	in = Machine->input_ports;

	total = 0;
	while (in->type != IPT_END)
	{
		if ((in->type & ~IPF_MASK) == IPT_DIPSWITCH_NAME && input_port_name(in) != 0 &&
				(in->type & IPF_UNUSED) == 0 &&
				!(!options.cheat && (in->type & IPF_CHEAT)))
		{
			entry[total] = in;
			menu_item[total] = input_port_name(in);

			total++;
		}

		in++;
	}

	if (total == 0) return 0;

	menu_item[total] = "Return to Main Menu";
	menu_item[total + 1] = 0;	/* terminate array */
	total++;


	for (i = 0;i < total;i++)
	{
		flag[i] = 0; /* TODO: flag the dip if it's not the real default */
		if (i < total - 1)
		{
			in = entry[i] + 1;
			while ((in->type & ~IPF_MASK) == IPT_DIPSWITCH_SETTING &&
					in->default_value != entry[i]->default_value)
				in++;

			if ((in->type & ~IPF_MASK) != IPT_DIPSWITCH_SETTING)
				menu_subitem[i] = "INVALID";
			else menu_subitem[i] = input_port_name(in);
		}
		else menu_subitem[i] = 0;	/* no subitem */
	}

	arrowize = 0;
	if (sel < total - 1)
	{
		in = entry[sel] + 1;
		while ((in->type & ~IPF_MASK) == IPT_DIPSWITCH_SETTING &&
				in->default_value != entry[sel]->default_value)
			in++;

		if ((in->type & ~IPF_MASK) != IPT_DIPSWITCH_SETTING)
			/* invalid setting: revert to a valid one */
			arrowize |= 1;
		else
		{
			if (((in-1)->type & ~IPF_MASK) == IPT_DIPSWITCH_SETTING &&
					!(!options.cheat && ((in-1)->type & IPF_CHEAT)))
				arrowize |= 1;
		}
	}
	if (sel < total - 1)
	{
		in = entry[sel] + 1;
		while ((in->type & ~IPF_MASK) == IPT_DIPSWITCH_SETTING &&
				in->default_value != entry[sel]->default_value)
			in++;

		if ((in->type & ~IPF_MASK) != IPT_DIPSWITCH_SETTING)
			/* invalid setting: revert to a valid one */
			arrowize |= 2;
		else
		{
			if (((in+1)->type & ~IPF_MASK) == IPT_DIPSWITCH_SETTING &&
					!(!options.cheat && ((in+1)->type & IPF_CHEAT)))
				arrowize |= 2;
		}
	}

	ui_displaymenu(menu_item,menu_subitem,flag,sel,arrowize);

	if (input_ui_pressed_repeat(IPT_UI_DOWN,8))
		sel = (sel + 1) % total;

	if (input_ui_pressed_repeat(IPT_UI_UP,8))
		sel = (sel + total - 1) % total;

	if (input_ui_pressed_repeat(IPT_UI_RIGHT,8))
	{
		if (sel < total - 1)
		{
			in = entry[sel] + 1;
			while ((in->type & ~IPF_MASK) == IPT_DIPSWITCH_SETTING &&
					in->default_value != entry[sel]->default_value)
				in++;

			if ((in->type & ~IPF_MASK) != IPT_DIPSWITCH_SETTING)
				/* invalid setting: revert to a valid one */
				entry[sel]->default_value = (entry[sel]+1)->default_value & entry[sel]->mask;
			else
			{
				if (((in+1)->type & ~IPF_MASK) == IPT_DIPSWITCH_SETTING &&
						!(!options.cheat && ((in+1)->type & IPF_CHEAT)))
					entry[sel]->default_value = (in+1)->default_value & entry[sel]->mask;
			}

			/* tell updatescreen() to clean after us (in case the window changes size) */
			need_to_clear_bitmap = 1;
		}
	}

	if (input_ui_pressed_repeat(IPT_UI_LEFT,8))
	{
		if (sel < total - 1)
		{
			in = entry[sel] + 1;
			while ((in->type & ~IPF_MASK) == IPT_DIPSWITCH_SETTING &&
					in->default_value != entry[sel]->default_value)
				in++;

			if ((in->type & ~IPF_MASK) != IPT_DIPSWITCH_SETTING)
				/* invalid setting: revert to a valid one */
				entry[sel]->default_value = (entry[sel]+1)->default_value & entry[sel]->mask;
			else
			{
				if (((in-1)->type & ~IPF_MASK) == IPT_DIPSWITCH_SETTING &&
						!(!options.cheat && ((in-1)->type & IPF_CHEAT)))
					entry[sel]->default_value = (in-1)->default_value & entry[sel]->mask;
			}

			/* tell updatescreen() to clean after us (in case the window changes size) */
			need_to_clear_bitmap = 1;
		}
	}

	if (input_ui_pressed(IPT_UI_SELECT))
	{
		if (sel == total - 1) sel = -1;
	}

	if (input_ui_pressed(IPT_UI_CANCEL))
		sel = -1;

	if (input_ui_pressed(IPT_UI_CONFIGURE))
		sel = -2;

	if (sel == -1 || sel == -2)
	{
		/* tell updatescreen() to clean after us */
		need_to_clear_bitmap = 1;
	}

	return sel + 1;
}

/* This flag is used for record OR sequence of key/joy */
/* when is !=0 the first sequence is record, otherwise the first free */
/* it's used byt setdefkeysettings, setdefjoysettings, setkeysettings, setjoysettings */
static int record_first_insert = 1;

static char menu_subitem_buffer[400][96];

static int setdefcodesettings(int selected)
{
	const char *menu_item[400];
	const char *menu_subitem[400];
	struct ipd *entry[400];
	char flag[400];
	int i,sel;
	struct ipd *in;
	int total;
	extern struct ipd inputport_defaults[];

	sel = selected - 1;


	if (Machine->input_ports == 0)
		return 0;

	in = inputport_defaults;

	total = 0;
	while (in->type != IPT_END)
	{
		if (in->name != 0  && (in->type & ~IPF_MASK) != IPT_UNKNOWN && (in->type & IPF_UNUSED) == 0
			&& !(!options.cheat && (in->type & IPF_CHEAT)))
		{
			entry[total] = in;
			menu_item[total] = in->name;

			total++;
		}

		in++;
	}

	if (total == 0) return 0;

	menu_item[total] = "Return to Main Menu";
	menu_item[total + 1] = 0;	/* terminate array */
	total++;

	for (i = 0;i < total;i++)
	{
		if (i < total - 1)
		{
			seq_name(&entry[i]->seq,menu_subitem_buffer[i],sizeof(menu_subitem_buffer[0]));
			menu_subitem[i] = menu_subitem_buffer[i];
		} else
			menu_subitem[i] = 0;	/* no subitem */
		flag[i] = 0;
	}

	if (sel > SEL_MASK)   /* are we waiting for a new key? */
	{
		int ret;

		menu_subitem[sel & SEL_MASK] = "    ";
		ui_displaymenu(menu_item,menu_subitem,flag,sel & SEL_MASK,3);

		ret = seq_read_async(&entry[sel & SEL_MASK]->seq,record_first_insert);

		if (ret >= 0)
		{
			sel &= 0xff;

			if (ret > 0 || seq_get_1(&entry[sel]->seq) == CODE_NONE)
			{
				seq_set_1(&entry[sel]->seq,CODE_NONE);
				ret = 1;
			}

			/* tell updatescreen() to clean after us (in case the window changes size) */
			need_to_clear_bitmap = 1;

			record_first_insert = ret != 0;
		}


		return sel + 1;
	}


	ui_displaymenu(menu_item,menu_subitem,flag,sel,0);

	if (input_ui_pressed_repeat(IPT_UI_DOWN,8))
	{
		sel = (sel + 1) % total;
		record_first_insert = 1;
	}

	if (input_ui_pressed_repeat(IPT_UI_UP,8))
	{
		sel = (sel + total - 1) % total;
		record_first_insert = 1;
	}

	if (input_ui_pressed(IPT_UI_SELECT))
	{
		if (sel == total - 1) sel = -1;
		else
		{
			seq_read_async_start();

			sel |= 1 << SEL_BITS;	/* we'll ask for a key */

			/* tell updatescreen() to clean after us (in case the window changes size) */
			need_to_clear_bitmap = 1;
		}
	}

	if (input_ui_pressed(IPT_UI_CANCEL))
		sel = -1;

	if (input_ui_pressed(IPT_UI_CONFIGURE))
		sel = -2;

	if (sel == -1 || sel == -2)
	{
		/* tell updatescreen() to clean after us */
		need_to_clear_bitmap = 1;

		record_first_insert = 1;
	}

	return sel + 1;
}



static int setcodesettings(int selected)
{
	const char *menu_item[400];
	const char *menu_subitem[400];
	struct InputPort *entry[400];
	char flag[400];
	int i,sel;
	struct InputPort *in;
	int total;


	sel = selected - 1;


	if (Machine->input_ports == 0)
		return 0;

	in = Machine->input_ports;

	total = 0;
	while (in->type != IPT_END)
	{
		if (input_port_name(in) != 0 && seq_get_1(&in->seq) != CODE_NONE && (in->type & ~IPF_MASK) != IPT_UNKNOWN)
		{
			entry[total] = in;
			menu_item[total] = input_port_name(in);

			total++;
		}

		in++;
	}

	if (total == 0) return 0;

	menu_item[total] = "Return to Main Menu";
	menu_item[total + 1] = 0;	/* terminate array */
	total++;

	for (i = 0;i < total;i++)
	{
		if (i < total - 1)
		{
			seq_name(input_port_seq(entry[i]),menu_subitem_buffer[i],sizeof(menu_subitem_buffer[0]));
			menu_subitem[i] = menu_subitem_buffer[i];

			/* If the key isn't the default, flag it */
			if (seq_get_1(&entry[i]->seq) != CODE_DEFAULT)
				flag[i] = 1;
			else
				flag[i] = 0;

		} else
			menu_subitem[i] = 0;	/* no subitem */
	}

	if (sel > SEL_MASK)   /* are we waiting for a new key? */
	{
		int ret;

		menu_subitem[sel & SEL_MASK] = "    ";
		ui_displaymenu(menu_item,menu_subitem,flag,sel & SEL_MASK,3);

		ret = seq_read_async(&entry[sel & SEL_MASK]->seq,record_first_insert);

		if (ret >= 0)
		{
			sel &= 0xff;

			if (ret > 0 || seq_get_1(&entry[sel]->seq) == CODE_NONE)
			{
				seq_set_1(&entry[sel]->seq, CODE_DEFAULT);
				ret = 1;
			}

			/* tell updatescreen() to clean after us (in case the window changes size) */
			need_to_clear_bitmap = 1;

			record_first_insert = ret != 0;
		}

		return sel + 1;
	}


	ui_displaymenu(menu_item,menu_subitem,flag,sel,0);

	if (input_ui_pressed_repeat(IPT_UI_DOWN,8))
	{
		sel = (sel + 1) % total;
		record_first_insert = 1;
	}

	if (input_ui_pressed_repeat(IPT_UI_UP,8))
	{
		sel = (sel + total - 1) % total;
		record_first_insert = 1;
	}

	if (input_ui_pressed(IPT_UI_SELECT))
	{
		if (sel == total - 1) sel = -1;
		else
		{
			seq_read_async_start();

			sel |= 1 << SEL_BITS;	/* we'll ask for a key */

			/* tell updatescreen() to clean after us (in case the window changes size) */
			need_to_clear_bitmap = 1;
		}
	}

	if (input_ui_pressed(IPT_UI_CANCEL))
		sel = -1;

	if (input_ui_pressed(IPT_UI_CONFIGURE))
		sel = -2;

	if (sel == -1 || sel == -2)
	{
		/* tell updatescreen() to clean after us */
		need_to_clear_bitmap = 1;

		record_first_insert = 1;
	}

	return sel + 1;
}


static int calibratejoysticks(int selected)
{
	char *msg;
	char buf[2048];
	int sel;
	static int calibration_started = 0;

	sel = selected - 1;

	if (calibration_started == 0)
	{
		osd_joystick_start_calibration();
		calibration_started = 1;
		strcpy (buf, "");
	}

	if (sel > SEL_MASK) /* Waiting for the user to acknowledge joystick movement */
	{
		if (input_ui_pressed(IPT_UI_CANCEL))
		{
			calibration_started = 0;
			sel = -1;
		}
		else if (input_ui_pressed(IPT_UI_SELECT))
		{
			osd_joystick_calibrate();
			sel &= 0xff;
		}

		ui_displaymessagewindow(buf);
	}
	else
	{
		msg = osd_joystick_calibrate_next();
		need_to_clear_bitmap = 1;
		if (msg == 0)
		{
			calibration_started = 0;
			osd_joystick_end_calibration();
			sel = -1;
		}
		else
		{
			strcpy (buf, msg);
			ui_displaymessagewindow(buf);
			sel |= 1 << SEL_BITS;
		}
	}

	if (input_ui_pressed(IPT_UI_CONFIGURE))
		sel = -2;

	if (sel == -1 || sel == -2)
	{
		/* tell updatescreen() to clean after us */
		need_to_clear_bitmap = 1;
	}

	return sel + 1;
}


static int settraksettings(int selected)
{
	const char *menu_item[40];
	const char *menu_subitem[40];
	struct InputPort *entry[40];
	int i,sel;
	struct InputPort *in;
	int total,total2;
	int arrowize;


	sel = selected - 1;


	if (Machine->input_ports == 0)
		return 0;

	in = Machine->input_ports;

	/* Count the total number of analog controls */
	total = 0;
	while (in->type != IPT_END)
	{
		if (((in->type & 0xff) > IPT_ANALOG_START) && ((in->type & 0xff) < IPT_ANALOG_END)
				&& !(!options.cheat && (in->type & IPF_CHEAT)))
		{
			entry[total] = in;
			total++;
		}
		in++;
	}

	if (total == 0) return 0;

	/* Each analog control has 3 entries - key & joy delta, reverse, sensitivity */

#define ENTRIES 3

	total2 = total * ENTRIES;

	menu_item[total2] = "Return to Main Menu";
	menu_item[total2 + 1] = 0;	/* terminate array */
	total2++;

	arrowize = 0;
	for (i = 0;i < total2;i++)
	{
		if (i < total2 - 1)
		{
			char label[30][40];
			char setting[30][40];
			int sensitivity,delta;
			int reverse;

			strcpy (label[i], input_port_name(entry[i/ENTRIES]));
			sensitivity = IP_GET_SENSITIVITY(entry[i/ENTRIES]);
			delta = IP_GET_DELTA(entry[i/ENTRIES]);
			reverse = (entry[i/ENTRIES]->type & IPF_REVERSE);

			switch (i%ENTRIES)
			{
				case 0:
					strcat (label[i], " Key/Joy Speed");
					sprintf(setting[i],"%d",delta);
					if (i == sel) arrowize = 3;
					break;
				case 1:
					strcat (label[i], " Reverse");
					if (reverse)
						sprintf(setting[i],"On");
					else
						sprintf(setting[i],"Off");
					if (i == sel) arrowize = 3;
					break;
				case 2:
					strcat (label[i], " Sensitivity");
					sprintf(setting[i],"%3d%%",sensitivity);
					if (i == sel) arrowize = 3;
					break;
			}

			menu_item[i] = label[i];
			menu_subitem[i] = setting[i];

			in++;
		}
		else menu_subitem[i] = 0;	/* no subitem */
	}

	ui_displaymenu(menu_item,menu_subitem,0,sel,arrowize);

	if (input_ui_pressed_repeat(IPT_UI_DOWN,8))
		sel = (sel + 1) % total2;

	if (input_ui_pressed_repeat(IPT_UI_UP,8))
		sel = (sel + total2 - 1) % total2;

	if (input_ui_pressed_repeat(IPT_UI_LEFT,8))
	{
		if ((sel % ENTRIES) == 0)
		/* keyboard/joystick delta */
		{
			int val = IP_GET_DELTA(entry[sel/ENTRIES]);

			val --;
			if (val < 1) val = 1;
			IP_SET_DELTA(entry[sel/ENTRIES],val);
		}
		else if ((sel % ENTRIES) == 1)
		/* reverse */
		{
			int reverse = entry[sel/ENTRIES]->type & IPF_REVERSE;
			if (reverse)
				reverse=0;
			else
				reverse=IPF_REVERSE;
			entry[sel/ENTRIES]->type &= ~IPF_REVERSE;
			entry[sel/ENTRIES]->type |= reverse;
		}
		else if ((sel % ENTRIES) == 2)
		/* sensitivity */
		{
			int val = IP_GET_SENSITIVITY(entry[sel/ENTRIES]);

			val --;
			if (val < 1) val = 1;
			IP_SET_SENSITIVITY(entry[sel/ENTRIES],val);
		}
	}

	if (input_ui_pressed_repeat(IPT_UI_RIGHT,8))
	{
		if ((sel % ENTRIES) == 0)
		/* keyboard/joystick delta */
		{
			int val = IP_GET_DELTA(entry[sel/ENTRIES]);

			val ++;
			if (val > 255) val = 255;
			IP_SET_DELTA(entry[sel/ENTRIES],val);
		}
		else if ((sel % ENTRIES) == 1)
		/* reverse */
		{
			int reverse = entry[sel/ENTRIES]->type & IPF_REVERSE;
			if (reverse)
				reverse=0;
			else
				reverse=IPF_REVERSE;
			entry[sel/ENTRIES]->type &= ~IPF_REVERSE;
			entry[sel/ENTRIES]->type |= reverse;
		}
		else if ((sel % ENTRIES) == 2)
		/* sensitivity */
		{
			int val = IP_GET_SENSITIVITY(entry[sel/ENTRIES]);

			val ++;
			if (val > 255) val = 255;
			IP_SET_SENSITIVITY(entry[sel/ENTRIES],val);
		}
	}

	if (input_ui_pressed(IPT_UI_SELECT))
	{
		if (sel == total2 - 1) sel = -1;
	}

	if (input_ui_pressed(IPT_UI_CANCEL))
		sel = -1;

	if (input_ui_pressed(IPT_UI_CONFIGURE))
		sel = -2;

	if (sel == -1 || sel == -2)
	{
		/* tell updatescreen() to clean after us */
		need_to_clear_bitmap = 1;
	}

	return sel + 1;
}

#ifndef MESS
static int mame_stats(int selected)
{
	char temp[10];
	char buf[2048];
	int sel, i;


	sel = selected - 1;

	buf[0] = 0;

	if (dispensed_tickets)
	{
		strcat(buf, "Tickets dispensed: ");
		sprintf(temp, "%d\n\n", dispensed_tickets);
		strcat(buf, temp);
	}

	for (i=0;  i<COIN_COUNTERS; i++)
	{
		sprintf(temp, "Coin %c: ", i+'A');
		strcat(buf, temp);
		if (!coins[i])
			strcat (buf, "NA");
		else
		{
			sprintf (temp, "%d", coins[i]);
			strcat (buf, temp);
		}
		if (coinlockedout[i])
		{
			strcat(buf, " (locked)\n");
		}
		else
		{
			strcat(buf, "\n");
		}
	}

	{
		/* menu system, use the normal menu keys */
		strcat(buf,"\n\t\x1a Return to Main Menu \x1b");

		ui_displaymessagewindow(buf);

		if (input_ui_pressed(IPT_UI_SELECT))
			sel = -1;

		if (input_ui_pressed(IPT_UI_CANCEL))
			sel = -1;

		if (input_ui_pressed(IPT_UI_CONFIGURE))
			sel = -2;
	}

	if (sel == -1 || sel == -2)
	{
		/* tell updatescreen() to clean after us */
		need_to_clear_bitmap = 1;
	}

	return sel + 1;
}
#endif

int showcopyright(void)
{
	int done;
	char buf[1000];


	sprintf(buf,
			"Usage of emulators in conjunction with ROMs you don't own "
			"is forbidden by copyright law.\n\n"
			"IF YOU ARE NOT LEGALLY ENTITLED TO PLAY \"%s\" ON THIS EMULATOR, "
			"PRESS ESC.\n\n"
			"Otherwise, type OK to continue",
			Machine->gamedrv->description);
	ui_displaymessagewindow(buf);

	setup_selected = -1;////
	done = 0;
	do
	{
		osd_update_video_and_audio();
		osd_poll_joysticks();
		if (input_ui_pressed(IPT_UI_CANCEL))
		{
			setup_selected = 0;////
			return 1;
		}
		if (keyboard_pressed_memory(KEYCODE_O) ||
				input_ui_pressed(IPT_UI_LEFT))
			done = 1;
		if (done == 1 && (keyboard_pressed_memory(KEYCODE_K) ||
				input_ui_pressed(IPT_UI_RIGHT)))
			done = 2;
	} while (done < 2);

	setup_selected = 0;////
	osd_clearbitmap(Machine->scrbitmap);
	osd_update_video_and_audio();

	return 0;
}

static int displaygameinfo(int selected)
{
	int i;
	char buf[2048];
	int sel;


	sel = selected - 1;


	sprintf(buf,"%s\n%s %s\n\nCPU:\n",Machine->gamedrv->description,Machine->gamedrv->year,Machine->gamedrv->manufacturer);
	i = 0;
	while (i < MAX_CPU && Machine->drv->cpu[i].cpu_type)
	{
		if (Machine->drv->cpu[i].cpu_clock >= 1000000)
			sprintf(&buf[strlen(buf)],"%s %d.%06d MHz",
					cputype_name(Machine->drv->cpu[i].cpu_type),
					Machine->drv->cpu[i].cpu_clock / 1000000,
					Machine->drv->cpu[i].cpu_clock % 1000000);
		else
			sprintf(&buf[strlen(buf)],"%s %d.%03d kHz",
					cputype_name(Machine->drv->cpu[i].cpu_type),
					Machine->drv->cpu[i].cpu_clock / 1000,
					Machine->drv->cpu[i].cpu_clock % 1000);

		if (Machine->drv->cpu[i].cpu_type & CPU_AUDIO_CPU)
			strcat(buf," (sound)");

		strcat(buf,"\n");

		i++;
	}

	strcat(buf,"\nSound");
	if (Machine->drv->sound_attributes & SOUND_SUPPORTS_STEREO)
		sprintf(&buf[strlen(buf)]," (stereo)");
	strcat(buf,":\n");

	i = 0;
	while (i < MAX_SOUND && Machine->drv->sound[i].sound_type)
	{
		if (sound_num(&Machine->drv->sound[i]))
			sprintf(&buf[strlen(buf)],"%dx",sound_num(&Machine->drv->sound[i]));

		sprintf(&buf[strlen(buf)],"%s",sound_name(&Machine->drv->sound[i]));

		if (sound_clock(&Machine->drv->sound[i]))
		{
			if (sound_clock(&Machine->drv->sound[i]) >= 1000000)
				sprintf(&buf[strlen(buf)]," %d.%06d MHz",
						sound_clock(&Machine->drv->sound[i]) / 1000000,
						sound_clock(&Machine->drv->sound[i]) % 1000000);
			else
				sprintf(&buf[strlen(buf)]," %d.%03d kHz",
						sound_clock(&Machine->drv->sound[i]) / 1000,
						sound_clock(&Machine->drv->sound[i]) % 1000);
		}

		strcat(buf,"\n");

		i++;
	}

	if (Machine->drv->video_attributes & VIDEO_TYPE_VECTOR)
		sprintf(&buf[strlen(buf)],"\nVector Game\n");
	else
	{
		int pixelx,pixely,tmax,tmin,rem;

		pixelx = 4 * (Machine->drv->visible_area.max_y - Machine->drv->visible_area.min_y + 1);
		pixely = 3 * (Machine->drv->visible_area.max_x - Machine->drv->visible_area.min_x + 1);

		/* calculate MCD */
		if (pixelx >= pixely)
		{
			tmax = pixelx;
			tmin = pixely;
		}
		else
		{
			tmax = pixely;
			tmin = pixelx;
		}
		while ( (rem = tmax % tmin) )
		{
			tmax = tmin;
			tmin = rem;
		}
		/* tmin is now the MCD */

		pixelx /= tmin;
		pixely /= tmin;

		sprintf(&buf[strlen(buf)],"\nScreen resolution:\n");
		sprintf(&buf[strlen(buf)],"%d x %d (%s) %f Hz\n",
				Machine->drv->visible_area.max_x - Machine->drv->visible_area.min_x + 1,
				Machine->drv->visible_area.max_y - Machine->drv->visible_area.min_y + 1,
				(Machine->gamedrv->flags & ORIENTATION_SWAP_XY) ? "V" : "H",
				Machine->drv->frames_per_second);
#if 0
		sprintf(&buf[strlen(buf)],"pixel aspect ratio %d:%d\n",
				pixelx,pixely);
		sprintf(&buf[strlen(buf)],"%d colors ",Machine->drv->total_colors);
		if (Machine->gamedrv->flags & GAME_REQUIRES_16BIT)
			strcat(buf,"(16-bit required)\n");
		else if (Machine->drv->video_attributes & VIDEO_MODIFIES_PALETTE)
			strcat(buf,"(dynamic)\n");
		else strcat(buf,"(static)\n");
#endif
	}


	if (sel == -1)
	{
		/* startup info, print MAME version and ask for any key */

		#ifndef MESS
		strcat(buf,"\n\tMAME ");    /* \t means that the line will be centered */
		#else
		strcat(buf,"\n\tMESS ");    /* \t means that the line will be centered */
		#endif

		strcat(buf,build_version);
		strcat(buf,"\n\tPress any key");
		ui_drawbox(0,0,Machine->uiwidth,Machine->uiheight);
		ui_displaymessagewindow(buf);

		sel = 0;
		if (code_read_async() != CODE_NONE)
			sel = -1;
	}
	else
	{
		/* menu system, use the normal menu keys */
		strcat(buf,"\n\t\x1a Return to Main Menu \x1b");

		ui_displaymessagewindow(buf);

		if (input_ui_pressed(IPT_UI_SELECT))
			sel = -1;

		if (input_ui_pressed(IPT_UI_CANCEL))
			sel = -1;

		if (input_ui_pressed(IPT_UI_CONFIGURE))
			sel = -2;
	}

	if (sel == -1 || sel == -2)
	{
		/* tell updatescreen() to clean after us */
		need_to_clear_bitmap = 1;
	}

	return sel + 1;
}


int showgamewarnings(void)
{
	int i;
	char buf[2048];

	if (Machine->gamedrv->flags &
			(GAME_NOT_WORKING | GAME_WRONG_COLORS | GAME_IMPERFECT_COLORS |
			  GAME_NO_SOUND | GAME_IMPERFECT_SOUND | GAME_NO_COCKTAIL))
	{
		int done;

		#ifndef MESS
		strcpy(buf, "There are known problems with this game:\n\n");
		#else
		strcpy(buf, "There are known problems with this system:\n\n");
		#endif


#ifdef MESS
		if (Machine->gamedrv->flags & GAME_COMPUTER)
		{
			strcpy(buf, "The emulated system is a computer: \n\n");
			strcat(buf, "The keyboard emulation may not be 100% accurate.\n");
		}
#endif

		if (Machine->gamedrv->flags & GAME_IMPERFECT_COLORS)
		{
			strcat(buf, "The colors aren't 100% accurate.\n");
		}

		if (Machine->gamedrv->flags & GAME_WRONG_COLORS)
		{
			strcat(buf, "The colors are completely wrong.\n");
		}

		if (Machine->gamedrv->flags & GAME_IMPERFECT_SOUND)
		{
			strcat(buf, "The sound emulation isn't 100% accurate.\n");
		}

		if (Machine->gamedrv->flags & GAME_NO_SOUND)
		{
			strcat(buf, "The game lacks sound.\n");
		}

		if (Machine->gamedrv->flags & GAME_NO_COCKTAIL)
		{
			strcat(buf, "Screen flipping in cocktail mode is not supported.\n");
		}

		if (Machine->gamedrv->flags & GAME_NOT_WORKING)
		{
			const struct GameDriver *maindrv;
			int foundworking;

			#ifdef MESS
			strcpy(buf,"THIS SYSTEM DOESN'T WORK PROPERLY");
			#else
			strcpy(buf,"THIS GAME DOESN'T WORK PROPERLY");
			#endif

			if (Machine->gamedrv->clone_of && !(Machine->gamedrv->clone_of->flags & NOT_A_DRIVER))
				maindrv = Machine->gamedrv->clone_of;
			else maindrv = Machine->gamedrv;

			foundworking = 0;
			i = 0;
			while (drivers[i])
			{
				if (drivers[i] == maindrv || drivers[i]->clone_of == maindrv)
				{
					if ((drivers[i]->flags & GAME_NOT_WORKING) == 0)
					{
						if (foundworking == 0)
							strcat(buf,"\n\nThere are working clones of this game. They are:\n\n");
						foundworking = 1;

						sprintf(&buf[strlen(buf)],"%s\n",drivers[i]->name);
					}
				}
				i++;
			}
		}

		strcat(buf,"\n\nType OK to continue");

		ui_displaymessagewindow(buf);

		done = 0;
		do
		{
			osd_update_video_and_audio();
			osd_poll_joysticks();
			if (input_ui_pressed(IPT_UI_CANCEL))
				return 1;
			if (code_pressed_memory(KEYCODE_O) ||
					input_ui_pressed(IPT_UI_LEFT))
				done = 1;
			if (done == 1 && (code_pressed_memory(KEYCODE_K) ||
					input_ui_pressed(IPT_UI_RIGHT)))
				done = 2;
		} while (done < 2);
	}


	osd_clearbitmap(Machine->scrbitmap);

	/* clear the input memory */
	while (code_read_async() != CODE_NONE);

	while (displaygameinfo(0) == 1)
	{
		osd_update_video_and_audio();
		osd_poll_joysticks();
	}

	#ifdef MESS
	while (displayimageinfo(0) == 1)
	{
		osd_update_video_and_audio();
		osd_poll_joysticks();
	}
	#endif

	osd_clearbitmap(Machine->scrbitmap);
	/* make sure that the screen is really cleared, in case autoframeskip kicked in */
	osd_update_video_and_audio();
	osd_update_video_and_audio();
	osd_update_video_and_audio();
	osd_update_video_and_audio();

	return 0;
}

/* Word-wraps the text in the specified buffer to fit in maxwidth characters per line.
   The contents of the buffer are modified.
   Known limitations: Words longer than maxwidth cause the function to fail. */
static void wordwrap_text_buffer (char *buffer, int maxwidth)
{
	int width = 0;

	while (*buffer)
	{
		if (*buffer == '\n')
		{
			buffer++;
			width = 0;
			continue;
		}

		width++;

		if (width > maxwidth)
		{
			/* backtrack until a space is found */
			while (*buffer != ' ')
			{
				buffer--;
				width--;
			}
			if (width < 1) return;	/* word too long */

			/* replace space with a newline */
			*buffer = '\n';
		}
		else
			buffer++;
	}
}

static int count_lines_in_buffer (char *buffer)
{
	int lines = 0;
	char c;

	while ( (c = *buffer++) )
		if (c == '\n') lines++;

	return lines;
}

/* Display lines from buffer, starting with line 'scroll', in a width x height text window */
static void display_scroll_message (int *scroll, int width, int height, char *buf)
{
	struct DisplayText dt[256];
	int curr_dt = 0;
	char uparrow[2] = "\x18";
	char downarrow[2] = "\x19";
	char textcopy[2048];
	char *copy;
	int leftoffs,topoffs;
	int first = *scroll;
	int buflines,showlines;
	int i;


	/* draw box */
	leftoffs = (Machine->uiwidth - Machine->uifontwidth * (width + 1)) / 2;
	if (leftoffs < 0) leftoffs = 0;
	topoffs = (Machine->uiheight - (3 * height + 1) * Machine->uifontheight / 2) / 2;
	ui_drawbox(leftoffs,topoffs,(width + 1) * Machine->uifontwidth,(3 * height + 1) * Machine->uifontheight / 2);

	buflines = count_lines_in_buffer (buf);
	if (first > 0)
	{
		if (buflines <= height)
			first = 0;
		else
		{
			height--;
			if (first > (buflines - height))
				first = buflines - height;
		}
		*scroll = first;
	}

	if (first != 0)
	{
		/* indicate that scrolling upward is possible */
		dt[curr_dt].text = uparrow;
		dt[curr_dt].color = DT_COLOR_WHITE;
		dt[curr_dt].x = (Machine->uiwidth - Machine->uifontwidth * strlen(uparrow)) / 2;
		dt[curr_dt].y = topoffs + (3*curr_dt+1)*Machine->uifontheight/2;
		curr_dt++;
	}

	if ((buflines - first) > height)
		showlines = height - 1;
	else
		showlines = height;

	/* skip to first line */
	while (first > 0)
	{
		char c;

		while ( (c = *buf++) )
		{
			if (c == '\n')
			{
				first--;
				break;
			}
		}
	}

	/* copy 'showlines' lines from buffer, starting with line 'first' */
	copy = textcopy;
	for (i = 0; i < showlines; i++)
	{
		char *copystart = copy;

		while (*buf && *buf != '\n')
		{
			*copy = *buf;
			copy++;
			buf++;
		}
		*copy = '\0';
		copy++;
		if (*buf == '\n')
			buf++;

		if (*copystart == '\t') /* center text */
		{
			copystart++;
			dt[curr_dt].x = (Machine->uiwidth - Machine->uifontwidth * (copy - copystart)) / 2;
		}
		else
			dt[curr_dt].x = leftoffs + Machine->uifontwidth/2;

		dt[curr_dt].text = copystart;
		dt[curr_dt].color = DT_COLOR_WHITE;
		dt[curr_dt].y = topoffs + (3*curr_dt+1)*Machine->uifontheight/2;
		curr_dt++;
	}

	if (showlines == (height - 1))
	{
		/* indicate that scrolling downward is possible */
		dt[curr_dt].text = downarrow;
		dt[curr_dt].color = DT_COLOR_WHITE;
		dt[curr_dt].x = (Machine->uiwidth - Machine->uifontwidth * strlen(downarrow)) / 2;
		dt[curr_dt].y = topoffs + (3*curr_dt+1)*Machine->uifontheight/2;
		curr_dt++;
	}

	dt[curr_dt].text = 0;	/* terminate array */

	displaytext(dt,0,0);
}


/* Display text entry for current driver from history.dat and mameinfo.dat. */
static int displayhistory (int selected)
{
	#ifndef MESS
	char *msg = "\tHistory not available\n\n\t\x1a Return to Main Menu \x1b";
	#else
	char *msg = "\tSysInfo.dat Missing\n\n\t\x1a Return to Main Menu \x1b";
	#endif
	static int scroll = 0;
	static char *buf = 0;
	int maxcols,maxrows;
	int sel;


	sel = selected - 1;


	maxcols = (Machine->uiwidth / Machine->uifontwidth) - 1;
	maxrows = (2 * Machine->uiheight - Machine->uifontheight) / (3 * Machine->uifontheight);
	maxcols -= 2;
	maxrows -= 8;

	if (!buf)
	{
		/* allocate a buffer for the text */
		buf = malloc (8192);
		if (buf)
		{
			/* try to load entry */
			if (load_driver_history (Machine->gamedrv, buf, 8192) == 0)
			{
				scroll = 0;
				wordwrap_text_buffer (buf, maxcols);
				strcat(buf,"\n\t\x1a Return to Main Menu \x1b\n");
			}
			else
			{
				free (buf);
				buf = 0;
			}
		}
	}

	{
		if (buf)
			display_scroll_message (&scroll, maxcols, maxrows, buf);
		else
			ui_displaymessagewindow (msg);

		if ((scroll > 0) && input_ui_pressed_repeat(IPT_UI_UP,4))
		{
			if (scroll == 2) scroll = 0;	/* 1 would be the same as 0, but with arrow on top */
			else scroll--;
		}

		if (input_ui_pressed_repeat(IPT_UI_DOWN,4))
		{
			if (scroll == 0) scroll = 2;	/* 1 would be the same as 0, but with arrow on top */
			else scroll++;
		}

		if (input_ui_pressed(IPT_UI_SELECT))
			sel = -1;

		if (input_ui_pressed(IPT_UI_CANCEL))
			sel = -1;

		if (input_ui_pressed(IPT_UI_CONFIGURE))
			sel = -2;
	}

	if (sel == -1 || sel == -2)
	{
		/* tell updatescreen() to clean after us */
		need_to_clear_bitmap = 1;

		/* force buffer to be recreated */
		if (buf)
		{
			free (buf);
			buf = 0;
		}
	}

	return sel + 1;

}


#ifndef NEOFREE
#ifndef TINY_COMPILE
int memcard_menu(int selection)
{
	int sel;
	int menutotal = 0;
	const char *menuitem[10];
	char	buffer[300];
	char	*msg;

	sel = selection - 1 ;

	sprintf(buffer, "Load Memory Card %03d", mcd_number);
	menuitem[menutotal++] = buffer;
	menuitem[menutotal++] = "Eject Memory Card";
	menuitem[menutotal++] = "Create Memory Card";
	menuitem[menutotal++] = "Call Memory Card Manager (RESET)";
	menuitem[menutotal++] = "Return to Main Menu";
	menuitem[menutotal] = 0;

	if (mcd_action!=0)
	{
		switch(mcd_action)
		{
		case	1:
			msg = "\nFailed To Load Memory Card!\n\n";
			break;
		case	2:
			msg = "\nLoad OK!\n\n";
			break;
		case	3:
			msg = "\nMemory Card Ejected!\n\n";
			break;
		case	4:
			msg = "\nMemory Card Created OK!\n\n";
			break;
		case	5:
			msg = "\nFailed To Create Memory Card!\n(It already exists ?)\n\n";
			break;
		default:
			msg = "\nDAMN!! Internal Error!\n\n";
		}
		ui_displaymessagewindow(msg);
		if (input_ui_pressed(IPT_UI_SELECT))
			mcd_action = 0;
	}
	else
	{
		ui_displaymenu(menuitem,0,0,sel,0);

		if (input_ui_pressed_repeat(IPT_UI_RIGHT,8))
			mcd_number = (mcd_number + 1) % 1000;

		if (input_ui_pressed_repeat(IPT_UI_LEFT,8))
			mcd_number = (mcd_number + 999) % 1000;

		if (input_ui_pressed_repeat(IPT_UI_DOWN,8))
			sel = (sel + 1) % menutotal;

		if (input_ui_pressed_repeat(IPT_UI_UP,8))
			sel = (sel + menutotal - 1) % menutotal;

		if (input_ui_pressed(IPT_UI_SELECT))
		{
			switch(sel)
			{
			case 0:
				neogeo_memcard_eject();
				if (neogeo_memcard_load(mcd_number))
				{
					memcard_status=1;
					memcard_number=mcd_number;
					mcd_action = 2;
				}
				else
					mcd_action = 1;
				break;
			case 1:
				neogeo_memcard_eject();
				mcd_action = 3;
				break;
			case 2:
				if (neogeo_memcard_create(mcd_number))
					mcd_action = 4;
				else
					mcd_action = 5;
				break;
			case 3:
				memcard_manager=1;
				sel=-2;
				machine_reset();
				break;
			case 4:
				sel=-1;
				break;
			}
		}

		if (input_ui_pressed(IPT_UI_CANCEL))
			sel = -1;

		if (input_ui_pressed(IPT_UI_CONFIGURE))
			sel = -2;

		if (sel == -1 || sel == -2)
		{
			/* tell updatescreen() to clean after us */
			need_to_clear_bitmap = 1;
		}
	}

	return sel + 1;
}
#endif
#endif


#ifndef MESS
enum { UI_SWITCH = 0,UI_DEFCODE,UI_CODE,UI_ANALOG,UI_CALIBRATE,
		UI_STATS,UI_GAMEINFO, UI_HISTORY,
		UI_CHEAT,UI_RESET,UI_MEMCARD,UI_EXIT };
#else
enum { UI_SWITCH = 0,UI_DEFCODE,UI_CODE,UI_ANALOG,UI_CALIBRATE,
		UI_GAMEINFO, UI_IMAGEINFO,UI_FILEMANAGER,UI_TAPECONTROL,
		UI_HISTORY,UI_CHEAT,UI_RESET,UI_MEMCARD,UI_EXIT };
#endif


#define MAX_SETUPMENU_ITEMS 20
static const char *menu_item[MAX_SETUPMENU_ITEMS];
static int menu_action[MAX_SETUPMENU_ITEMS];
static int menu_total;


static void setup_menu_init(void)
{
	menu_total = 0;

	menu_item[menu_total] = "Input (general)"; menu_action[menu_total++] = UI_DEFCODE;
	#ifndef MESS
	menu_item[menu_total] = "Input (this game)"; menu_action[menu_total++] = UI_CODE;
	#else
	menu_item[menu_total] = "Input (this machine)"; menu_action[menu_total++] = UI_CODE;
	#endif
	menu_item[menu_total] = "Dip Switches"; menu_action[menu_total++] = UI_SWITCH;

	/* Determine if there are any analog controls */
	{
		struct InputPort *in;
		int num;

		in = Machine->input_ports;

		num = 0;
		while (in->type != IPT_END)
		{
			if (((in->type & 0xff) > IPT_ANALOG_START) && ((in->type & 0xff) < IPT_ANALOG_END)
					&& !(!options.cheat && (in->type & IPF_CHEAT)))
				num++;
			in++;
		}

		if (num != 0)
		{
			menu_item[menu_total] = "Analog Controls"; menu_action[menu_total++] = UI_ANALOG;
		}
	}

	/* Joystick calibration possible? */
	if ((osd_joystick_needs_calibration()) != 0)
	{
		menu_item[menu_total] = "Calibrate Joysticks"; menu_action[menu_total++] = UI_CALIBRATE;
	}

	#ifndef MESS
	menu_item[menu_total] = "Bookkeeping Info"; menu_action[menu_total++] = UI_STATS;
	menu_item[menu_total] = "Game Information"; menu_action[menu_total++] = UI_GAMEINFO;
	menu_item[menu_total] = "Game History"; menu_action[menu_total++] = UI_HISTORY;
	#else
	menu_item[menu_total] = "Machine Information"; menu_action[menu_total++] = UI_GAMEINFO;
	menu_item[menu_total] = "Image Information"; menu_action[menu_total++] = UI_IMAGEINFO;
	menu_item[menu_total] = "File Manager"; menu_action[menu_total++] = UI_FILEMANAGER;
	menu_item[menu_total] = "Tape Control"; menu_action[menu_total++] = UI_TAPECONTROL;
	menu_item[menu_total] = "Machine Usage & History"; menu_action[menu_total++] = UI_HISTORY;
	#endif

	if (options.cheat)
	{
		menu_item[menu_total] = "Cheat"; menu_action[menu_total++] = UI_CHEAT;
	}

#ifndef NEOFREE
#ifndef TINY_COMPILE
	if (Machine->gamedrv->clone_of == &driver_neogeo ||
			(Machine->gamedrv->clone_of &&
				Machine->gamedrv->clone_of->clone_of == &driver_neogeo))
	{
		menu_item[menu_total] = "Memory Card"; menu_action[menu_total++] = UI_MEMCARD;
	}
#endif
#endif

	#ifndef MESS
	menu_item[menu_total] = "Reset Game"; menu_action[menu_total++] = UI_RESET;
	menu_item[menu_total] = "Return to Game"; menu_action[menu_total++] = UI_EXIT;
	#else
	menu_item[menu_total] = "Reset Machine"; menu_action[menu_total++] = UI_RESET;
	menu_item[menu_total] = "Return to Machine"; menu_action[menu_total++] = UI_EXIT;
	#endif
	menu_item[menu_total] = 0; /* terminate array */
}


static int setup_menu(int selected)
{
	int sel,res;
	static int menu_lastselected = 0;


	if (selected == -1)
		sel = menu_lastselected;
	else sel = selected - 1;

	if (sel > SEL_MASK)
	{
		switch (menu_action[sel & SEL_MASK])
		{
			case UI_SWITCH:
				res = setdipswitches(sel >> SEL_BITS);
				if (res == -1)
				{
					menu_lastselected = sel;
					sel = -1;
				}
				else
					sel = (sel & SEL_MASK) | (res << SEL_BITS);
				break;

			case UI_DEFCODE:
				res = setdefcodesettings(sel >> SEL_BITS);
				if (res == -1)
				{
					menu_lastselected = sel;
					sel = -1;
				}
				else
					sel = (sel & SEL_MASK) | (res << SEL_BITS);
				break;

			case UI_CODE:
				res = setcodesettings(sel >> SEL_BITS);
				if (res == -1)
				{
					menu_lastselected = sel;
					sel = -1;
				}
				else
					sel = (sel & SEL_MASK) | (res << SEL_BITS);
				break;

			case UI_ANALOG:
				res = settraksettings(sel >> SEL_BITS);
				if (res == -1)
				{
					menu_lastselected = sel;
					sel = -1;
				}
				else
					sel = (sel & SEL_MASK) | (res << SEL_BITS);
				break;

			case UI_CALIBRATE:
				res = calibratejoysticks(sel >> SEL_BITS);
				if (res == -1)
				{
					menu_lastselected = sel;
					sel = -1;
				}
				else
					sel = (sel & SEL_MASK) | (res << SEL_BITS);
				break;


			#ifndef MESS
			case UI_STATS:
				res = mame_stats(sel >> SEL_BITS);
				if (res == -1)
				{
					menu_lastselected = sel;
					sel = -1;
				}
				else
					sel = (sel & SEL_MASK) | (res << SEL_BITS);
				break;
			#endif

			case UI_GAMEINFO:
				res = displaygameinfo(sel >> SEL_BITS);
				if (res == -1)
				{
					menu_lastselected = sel;
					sel = -1;
				}
				else
					sel = (sel & SEL_MASK) | (res << SEL_BITS);
				break;

			#ifdef MESS
			case UI_IMAGEINFO:
				res = displayimageinfo(sel >> SEL_BITS);
				if (res == -1)
				{
					menu_lastselected = sel;
					sel = -1;
				}
				else
					sel = (sel & SEL_MASK) | (res << SEL_BITS);
				break;
			case UI_FILEMANAGER:
				res = filemanager(sel >> SEL_BITS);
				if (res == -1)
				{
					menu_lastselected = sel;
					sel = -1;
				}
				else
					sel = (sel & SEL_MASK) | (res << SEL_BITS);
				break;
			case UI_TAPECONTROL:
				res = tapecontrol(sel >> SEL_BITS);
				if (res == -1)
				{
					menu_lastselected = sel;
					sel = -1;
				}
				else
					sel = (sel & SEL_MASK) | (res << SEL_BITS);
				break;
			#endif

			case UI_HISTORY:
				res = displayhistory(sel >> SEL_BITS);
				if (res == -1)
				{
					menu_lastselected = sel;
					sel = -1;
				}
				else
					sel = (sel & SEL_MASK) | (res << SEL_BITS);
				break;

			case UI_CHEAT:
osd_sound_enable(0);
while (seq_pressed(input_port_type_seq(IPT_UI_SELECT)))
	osd_update_video_and_audio();	  /* give time to the sound hardware to apply the volume change */
				cheat_menu();
osd_sound_enable(1);
sel = sel & SEL_MASK;
				break;

#ifndef NEOFREE
#ifndef TINY_COMPILE
			case UI_MEMCARD:
				res = memcard_menu(sel >> SEL_BITS);
				if (res == -1)
				{
					menu_lastselected = sel;
					sel = -1;
				}
				else
					sel = (sel & SEL_MASK) | (res << SEL_BITS);
				break;
#endif
#endif
		}

		return sel + 1;
	}


	ui_displaymenu(menu_item,0,0,sel,0);

	if (input_ui_pressed_repeat(IPT_UI_DOWN,8))
		sel = (sel + 1) % menu_total;

	if (input_ui_pressed_repeat(IPT_UI_UP,8))
		sel = (sel + menu_total - 1) % menu_total;

	if (input_ui_pressed(IPT_UI_SELECT))
	{
		switch (menu_action[sel])
		{
			case UI_SWITCH:
			case UI_DEFCODE:
			case UI_CODE:
			case UI_ANALOG:
			case UI_CALIBRATE:
			#ifndef MESS
			case UI_STATS:
			case UI_GAMEINFO:
			#else
			case UI_GAMEINFO:
			case UI_IMAGEINFO:
			case UI_FILEMANAGER:
			case UI_TAPECONTROL:
			#endif
			case UI_HISTORY:
			case UI_CHEAT:
			case UI_MEMCARD:
				sel |= 1 << SEL_BITS;
				/* tell updatescreen() to clean after us */
				need_to_clear_bitmap = 1;
				break;

			case UI_RESET:
				machine_reset();
				break;

			case UI_EXIT:
				menu_lastselected = 0;
				sel = -1;
				break;
		}
	}

	if (input_ui_pressed(IPT_UI_CANCEL) ||
			input_ui_pressed(IPT_UI_CONFIGURE))
	{
		menu_lastselected = sel;
		sel = -1;
	}

	if (sel == -1)
	{
		/* tell updatescreen() to clean after us */
		need_to_clear_bitmap = 1;
	}

	return sel + 1;
}



/*********************************************************************

  start of On Screen Display handling

*********************************************************************/

static void displayosd(const char *text,int percentage,int default_percentage)
{
	struct DisplayText dt[2];
	int avail;


	avail = (Machine->uiwidth / Machine->uifontwidth) * 19 / 20;

	ui_drawbox((Machine->uiwidth - Machine->uifontwidth * avail) / 2,
			(Machine->uiheight - 7*Machine->uifontheight/2),
			avail * Machine->uifontwidth,
			3*Machine->uifontheight);

	avail--;

	drawbar((Machine->uiwidth - Machine->uifontwidth * avail) / 2,
			(Machine->uiheight - 3*Machine->uifontheight),
			avail * Machine->uifontwidth,
			Machine->uifontheight,
			percentage,default_percentage);

	dt[0].text = text;
	dt[0].color = DT_COLOR_WHITE;
	dt[0].x = (Machine->uiwidth - Machine->uifontwidth * strlen(text)) / 2;
	dt[0].y = (Machine->uiheight - 2*Machine->uifontheight) + 2;
	dt[1].text = 0; /* terminate array */
	displaytext(dt,0,0);
}



static void onscrd_volume(int increment,int arg)
{
	char buf[20];
	int attenuation;

	if (increment)
	{
		attenuation = osd_get_mastervolume();
		attenuation += increment;
		if (attenuation > 0) attenuation = 0;
		if (attenuation < -32) attenuation = -32;
		osd_set_mastervolume(attenuation);
	}
	attenuation = osd_get_mastervolume();

	sprintf(buf,"Volume %3ddB",attenuation);
	displayosd(buf,100 * (attenuation + 32) / 32,100);
}

static void onscrd_mixervol(int increment,int arg)
{
	static void *driver = 0;
	char buf[40];
	int volume,ch;
	int doallchannels = 0;
	int proportional = 0;


	if (code_pressed(KEYCODE_LSHIFT) || code_pressed(KEYCODE_RSHIFT))
		doallchannels = 1;
	if (!code_pressed(KEYCODE_LCONTROL) && !code_pressed(KEYCODE_RCONTROL))
		increment *= 5;
	if (code_pressed(KEYCODE_LALT) || code_pressed(KEYCODE_RALT))
		proportional = 1;

	if (increment)
	{
		if (proportional)
		{
			static int old_vol[MIXER_MAX_CHANNELS];
			float ratio = 1.0;
			int overflow = 0;

			if (driver != Machine->drv)
			{
				driver = (void *)Machine->drv;
				for (ch = 0; ch < MIXER_MAX_CHANNELS; ch++)
					old_vol[ch] = mixer_get_mixing_level(ch);
			}

			volume = mixer_get_mixing_level(arg);
			if (old_vol[arg])
				ratio = (float)(volume + increment) / (float)old_vol[arg];

			for (ch = 0; ch < MIXER_MAX_CHANNELS; ch++)
			{
				if (mixer_get_name(ch) != 0)
				{
					volume = ratio * old_vol[ch];
					if (volume < 0 || volume > 100)
						overflow = 1;
				}
			}

			if (!overflow)
			{
				for (ch = 0; ch < MIXER_MAX_CHANNELS; ch++)
				{
					volume = ratio * old_vol[ch];
					mixer_set_mixing_level(ch,volume);
				}
			}
		}
		else
		{
			driver = 0; /* force reset of saved volumes */

			volume = mixer_get_mixing_level(arg);
			volume += increment;
			if (volume > 100) volume = 100;
			if (volume < 0) volume = 0;

			if (doallchannels)
			{
				for (ch = 0;ch < MIXER_MAX_CHANNELS;ch++)
					mixer_set_mixing_level(ch,volume);
			}
			else
				mixer_set_mixing_level(arg,volume);
		}
	}
	volume = mixer_get_mixing_level(arg);

	if (proportional)
		sprintf(buf,"ALL CHANNELS Relative %3d%%", volume);
	else if (doallchannels)
		sprintf(buf,"ALL CHANNELS Volume %3d%%",volume);
	else
		sprintf(buf,"%s Volume %3d%%",mixer_get_name(arg),volume);
	displayosd(buf,volume,mixer_get_default_mixing_level(arg));
}

static void onscrd_brightness(int increment,int arg)
{
	char buf[20];
	int brightness;


	if (increment)
	{
		brightness = osd_get_brightness();
		brightness += 5 * increment;
		if (brightness < 0) brightness = 0;
		if (brightness > 100) brightness = 100;
		osd_set_brightness(brightness);
	}
	brightness = osd_get_brightness();

	sprintf(buf,"Brightness %3d%%",brightness);
	displayosd(buf,brightness,100);
}

static void onscrd_gamma(int increment,int arg)
{
	char buf[20];
	float gamma_correction;

	if (increment)
	{
		gamma_correction = osd_get_gamma();

		gamma_correction += 0.05 * increment;
		if (gamma_correction < 0.5) gamma_correction = 0.5;
		if (gamma_correction > 2.0) gamma_correction = 2.0;

		osd_set_gamma(gamma_correction);
	}
	gamma_correction = osd_get_gamma();

	sprintf(buf,"Gamma %1.2f",gamma_correction);
	displayosd(buf,100*(gamma_correction-0.5)/(2.0-0.5),100*(1.0-0.5)/(2.0-0.5));
}

static void onscrd_vector_intensity(int increment,int arg)
{
	char buf[30];
	float intensity_correction;

	if (increment)
	{
		intensity_correction = vector_get_intensity();

		intensity_correction += 0.05 * increment;
		if (intensity_correction < 0.5) intensity_correction = 0.5;
		if (intensity_correction > 3.0) intensity_correction = 3.0;

		vector_set_intensity(intensity_correction);
	}
	intensity_correction = vector_get_intensity();

	sprintf(buf,"Vector intensity %1.2f",intensity_correction);
	displayosd(buf,100*(intensity_correction-0.5)/(3.0-0.5),100*(1.5-0.5)/(3.0-0.5));
}


static void onscrd_overclock(int increment,int arg)
{
	char buf[30];
	double overclock;
	int cpu, doallcpus = 0, oc;

	if (code_pressed(KEYCODE_LSHIFT) || code_pressed(KEYCODE_RSHIFT))
		doallcpus = 1;
	if (!code_pressed(KEYCODE_LCONTROL) && !code_pressed(KEYCODE_RCONTROL))
		increment *= 5;
	if( increment )
	{
		overclock = timer_get_overclock(arg);
		overclock += 0.01 * increment;
		if (overclock < 0.01) overclock = 0.01;
		if (overclock > 2.0) overclock = 2.0;
		if( doallcpus )
			for( cpu = 0; cpu < cpu_gettotalcpu(); cpu++ )
				timer_set_overclock(cpu, overclock);
		else
			timer_set_overclock(arg, overclock);
	}

	oc = 100 * timer_get_overclock(arg) + 0.5;

	if( doallcpus )
		sprintf(buf,"ALL CPUS Overclock %3d%%", oc);
	else
		sprintf(buf,"Overclock CPU#%d %3d%%", arg, oc);
	displayosd(buf,oc/2,100/2);
}

#define MAX_OSD_ITEMS 30
static void (*onscrd_fnc[MAX_OSD_ITEMS])(int increment,int arg);
static int onscrd_arg[MAX_OSD_ITEMS];
static int onscrd_total_items;

static void onscrd_init(void)
{
	int item,ch;


	item = 0;

	onscrd_fnc[item] = onscrd_volume;
	onscrd_arg[item] = 0;
	item++;

	for (ch = 0;ch < MIXER_MAX_CHANNELS;ch++)
	{
		if (mixer_get_name(ch) != 0)
		{
			onscrd_fnc[item] = onscrd_mixervol;
			onscrd_arg[item] = ch;
			item++;
		}
	}

	if (options.cheat)
	{
		for (ch = 0;ch < cpu_gettotalcpu();ch++)
		{
			onscrd_fnc[item] = onscrd_overclock;
			onscrd_arg[item] = ch;
			item++;
		}
	}

	onscrd_fnc[item] = onscrd_brightness;
	onscrd_arg[item] = 0;
	item++;

	onscrd_fnc[item] = onscrd_gamma;
	onscrd_arg[item] = 0;
	item++;

	if (Machine->drv->video_attributes & VIDEO_TYPE_VECTOR)
	{
		onscrd_fnc[item] = onscrd_vector_intensity;
		onscrd_arg[item] = 0;
		item++;
	}

	onscrd_total_items = item;
}

static int on_screen_display(int selected)
{
	int increment,sel;
	static int lastselected = 0;


	if (selected == -1)
		sel = lastselected;
	else sel = selected - 1;

	increment = 0;
	if (input_ui_pressed_repeat(IPT_UI_LEFT,8))
		increment = -1;
	if (input_ui_pressed_repeat(IPT_UI_RIGHT,8))
		increment = 1;
	if (input_ui_pressed_repeat(IPT_UI_DOWN,8))
		sel = (sel + 1) % onscrd_total_items;
	if (input_ui_pressed_repeat(IPT_UI_UP,8))
		sel = (sel + onscrd_total_items - 1) % onscrd_total_items;

	(*onscrd_fnc[sel])(increment,onscrd_arg[sel]);

	lastselected = sel;

	if (input_ui_pressed(IPT_UI_ON_SCREEN_DISPLAY))
	{
		sel = -1;

		/* tell updatescreen() to clean after us */
		need_to_clear_bitmap = 1;
	}

	return sel + 1;
}

/*********************************************************************

  end of On Screen Display handling

*********************************************************************/


static void displaymessage(const char *text)
{
	struct DisplayText dt[2];
	int avail;


	if (Machine->uiwidth < Machine->uifontwidth * strlen(text))
	{
		ui_displaymessagewindow(text);
		return;
	}

	avail = strlen(text)+2;

	ui_drawbox((Machine->uiwidth - Machine->uifontwidth * avail) / 2,
			Machine->uiheight - 3*Machine->uifontheight,
			avail * Machine->uifontwidth,
			2*Machine->uifontheight);

	dt[0].text = text;
	dt[0].color = DT_COLOR_WHITE;
	dt[0].x = (Machine->uiwidth - Machine->uifontwidth * strlen(text)) / 2;
	dt[0].y = Machine->uiheight - 5*Machine->uifontheight/2;
	dt[1].text = 0; /* terminate array */
	displaytext(dt,0,0);
}


static char messagetext[80];
static int messagecounter;

void CLIB_DECL usrintf_showmessage(const char *text,...)
{
	va_list arg;
	va_start(arg,text);
	vsprintf(messagetext,text,arg);
	va_end(arg);
	messagecounter = 2 * Machine->drv->frames_per_second;
}




int handle_user_interface(void)
{
	static int show_profiler;
#ifdef MAME_DEBUG
	static int show_total_colors;
#endif

#ifdef MESS
if (Machine->gamedrv->flags & GAME_COMPUTER)
{
	static int ui_active = 0, ui_toggle_key = 0;
	static int ui_display_count = 4 * 60;

	if( input_ui_pressed(IPT_UI_TOGGLE_UI) )
	{
		if( !ui_toggle_key )
		{
			ui_toggle_key = 1;
			ui_active = !ui_active;
			ui_display_count = 4 * 60;
			bitmap_dirty = 1;
		 }
	}
	else
	{
		ui_toggle_key = 0;
	}

	if( ui_active )
	{
		if( ui_display_count > 0 )
		{
			char text[] = "KBD: UI  (ScrLock)";
			int x, x0 = Machine->uiwidth - sizeof(text) * Machine->uifont->width - 2;
			int y0 = Machine->uiymin + Machine->uiheight - Machine->uifont->height - 2;
			for( x = 0; text[x]; x++ )
			{
				drawgfx(Machine->scrbitmap,
					Machine->uifont,text[x],0,0,0,
					x0+x*Machine->uifont->width,
					y0,0,TRANSPARENCY_NONE,0);
			}
			if( --ui_display_count == 0 )
				bitmap_dirty = 1;
		}
	}
	else
	{
		if( ui_display_count > 0 )
		{
			char text[] = "KBD: EMU (ScrLock)";
			int x, x0 = Machine->uiwidth - sizeof(text) * Machine->uifont->width - 2;
			int y0 = Machine->uiymin + Machine->uiheight - Machine->uifont->height - 2;
			for( x = 0; text[x]; x++ )
			{
				drawgfx(Machine->scrbitmap,
					Machine->uifont,text[x],0,0,0,
					x0+x*Machine->uifont->width,
					y0,0,TRANSPARENCY_NONE,0);
			}
			if( --ui_display_count == 0 )
				bitmap_dirty = 1;
		}
		return 0;
	}
}
#endif

	/* if the user pressed F12, save the screen to a file */
	if (input_ui_pressed(IPT_UI_SNAPSHOT))
		osd_save_snapshot();

	/* This call is for the cheat, it must be called at least each frames */
	if (options.cheat) DoCheat();

	/* if the user pressed ESC, stop the emulation */
	/* but don't quit if the setup menu is on screen */
	if (setup_selected == 0 && input_ui_pressed(IPT_UI_CANCEL))
		return 1;

	if (setup_selected == 0 && input_ui_pressed(IPT_UI_CONFIGURE))
	{
		setup_selected = -1;
		if (osd_selected != 0)
		{
			osd_selected = 0;	/* disable on screen display */
			/* tell updatescreen() to clean after us */
			need_to_clear_bitmap = 1;
		}
	}
	if (setup_selected != 0) setup_selected = setup_menu(setup_selected);

	if (!mame_debug && osd_selected == 0 && input_ui_pressed(IPT_UI_ON_SCREEN_DISPLAY))
	{
		osd_selected = -1;
		if (setup_selected != 0)
		{
			setup_selected = 0; /* disable setup menu */
			/* tell updatescreen() to clean after us */
			need_to_clear_bitmap = 1;
		}
	}
	if (osd_selected != 0) osd_selected = on_screen_display(osd_selected);


#if 0
	if (keyboard_pressed_memory(KEYCODE_BACKSPACE))
	{
		if (jukebox_selected != -1)
		{
			jukebox_selected = -1;
			cpu_halt(0,1);
		}
		else
		{
			jukebox_selected = 0;
			cpu_halt(0,0);
		}
	}

	if (jukebox_selected != -1)
	{
		char buf[40];
		watchdog_reset_w(0,0);
		if (keyboard_pressed_memory(KEYCODE_LCONTROL))
		{
#include "cpu/z80/z80.h"
			soundlatch_w(0,jukebox_selected);
			cpu_cause_interrupt(1,Z80_NMI_INT);
		}
		if (input_ui_pressed_repeat(IPT_UI_RIGHT,8))
		{
			jukebox_selected = (jukebox_selected + 1) & 0xff;
		}
		if (input_ui_pressed_repeat(IPT_UI_LEFT,8))
		{
			jukebox_selected = (jukebox_selected - 1) & 0xff;
		}
		if (input_ui_pressed_repeat(IPT_UI_UP,8))
		{
			jukebox_selected = (jukebox_selected + 16) & 0xff;
		}
		if (input_ui_pressed_repeat(IPT_UI_DOWN,8))
		{
			jukebox_selected = (jukebox_selected - 16) & 0xff;
		}
		sprintf(buf,"sound cmd %02x",jukebox_selected);
		displaymessage(buf);
	}
#endif


	/* if the user pressed F3, reset the emulation */
	if (input_ui_pressed(IPT_UI_RESET_MACHINE))
		machine_reset();


	if (single_step || input_ui_pressed(IPT_UI_PAUSE)) /* pause the game */
	{
/*		osd_selected = 0;	   disable on screen display, since we are going   */
							/* to change parameters affected by it */

		if (single_step == 0)
		{
			osd_sound_enable(0);
			osd_pause(1);
		}

		while (!input_ui_pressed(IPT_UI_PAUSE))
		{
#ifdef MAME_NET
			osd_net_sync();
#endif /* MAME_NET */
			profiler_mark(PROFILER_VIDEO);
			if (osd_skip_this_frame() == 0)
			{
				if (need_to_clear_bitmap || bitmap_dirty)
				{
					osd_clearbitmap(Machine->scrbitmap);
					need_to_clear_bitmap = 0;
					(*Machine->drv->vh_update)(Machine->scrbitmap,bitmap_dirty);
					bitmap_dirty = 0;
				}
#ifdef MAME_DEBUG
/* keep calling vh_screenrefresh() while paused so we can stuff */
/* debug code in there */
(*Machine->drv->vh_update)(Machine->scrbitmap,bitmap_dirty);
#endif
			}
			profiler_mark(PROFILER_END);

			if (input_ui_pressed(IPT_UI_SNAPSHOT))
				osd_save_snapshot();

			if (setup_selected == 0 && input_ui_pressed(IPT_UI_CANCEL))
				return 1;

			if (setup_selected == 0 && input_ui_pressed(IPT_UI_CONFIGURE))
			{
				setup_selected = -1;
				if (osd_selected != 0)
				{
					osd_selected = 0;	/* disable on screen display */
					/* tell updatescreen() to clean after us */
					need_to_clear_bitmap = 1;
				}
			}
			if (setup_selected != 0) setup_selected = setup_menu(setup_selected);

			if (!mame_debug && osd_selected == 0 && input_ui_pressed(IPT_UI_ON_SCREEN_DISPLAY))
			{
				osd_selected = -1;
				if (setup_selected != 0)
				{
					setup_selected = 0; /* disable setup menu */
					/* tell updatescreen() to clean after us */
					need_to_clear_bitmap = 1;
				}
			}
			if (osd_selected != 0) osd_selected = on_screen_display(osd_selected);

			/* show popup message if any */
			if (messagecounter > 0) displaymessage(messagetext);

			osd_update_video_and_audio();
			osd_poll_joysticks();
		}

		if (code_pressed(KEYCODE_LSHIFT) || code_pressed(KEYCODE_RSHIFT))
			single_step = 1;
		else
		{
			single_step = 0;
			osd_pause(0);
			osd_sound_enable(1);
		}
	}


	/* show popup message if any */
	if (messagecounter > 0)
	{
		displaymessage(messagetext);

		if (--messagecounter == 0)
			/* tell updatescreen() to clean after us */
			need_to_clear_bitmap = 1;
	}


	if (input_ui_pressed(IPT_UI_SHOW_PROFILER))
	{
		show_profiler ^= 1;
		if (show_profiler)
			profiler_start();
		else
		{
			profiler_stop();
			/* tell updatescreen() to clean after us */
			need_to_clear_bitmap = 1;
		}
	}
#ifdef MAME_DEBUG
	if (input_ui_pressed(IPT_UI_SHOW_COLORS))
	{
		show_total_colors ^= 1;
		if (show_total_colors == 0)
			/* tell updatescreen() to clean after us */
			need_to_clear_bitmap = 1;
	}
	if (show_total_colors) showtotalcolors();
#endif

	if (show_profiler) profiler_show();


	/* if the user pressed F4, show the character set */
	if (input_ui_pressed(IPT_UI_SHOW_GFX))
	{
		osd_sound_enable(0);

		showcharset();

		osd_sound_enable(1);
	}

	return 0;
}


void init_user_interface(void)
{
	extern int snapno;	/* in common.c */

	snapno = 0; /* reset snapshot counter */

	setup_menu_init();
	setup_selected = 0;

	onscrd_init();
	osd_selected = 0;

	jukebox_selected = -1;

	single_step = 0;
}

int onscrd_active(void)
{
	return osd_selected;
}

int setup_active(void)
{
	return setup_selected;
}

