/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/


#define VERBOSE_VIDEO
#define SHOW_CHARS

#include "driver.h"
#include "vidhrdw/generic.h"

static int gameplan_this_is_kaos;
static int gameplan_this_is_megatack;
static int clear_to_colour = 0;
static int fix_clear_to_colour = -1;
static char *colour_names[] = {"WHITE", "CYAN", "MAGENTA", "BLUE",
							   "YELLOW", "GREEN", "RED", ".BLACK"};



void gameplan_clear_screen(void);


/***************************************************************************

  Start the video hardware emulation.

***************************************************************************/


int gameplan_vh_start(void)
{
	if (strcmp(Machine->gamedrv->name, "kaos") == 0)
		gameplan_this_is_kaos = 1;
	else
		gameplan_this_is_kaos = 0;

	if (strcmp(Machine->gamedrv->name, "megatack") == 0)
		gameplan_this_is_megatack = 1;
	else
		gameplan_this_is_megatack = 0;

	return generic_bitmapped_vh_start();
}


static int port_b;
static int new_request = 0;
static int finished_sound = 0;
static int cb2 = -1;

int gameplan_sound_r(int offset)
{
#ifdef VERBOSE
	if (errorlog)  fprintf(errorlog, "GAME:  read reg%X at PC %04x\n", offset, cpu_get_pc());
#endif

	if (offset == 0)
	{
#ifdef VERBOSE
		if (errorlog)
		{
			if (finished_sound)  fprintf(errorlog, "[GAME: checking sound request ack: OK (%d)]\n", finished_sound);
			else  fprintf(errorlog, "[GAME: checking sound request ack: BAD (%d)]\n", finished_sound);
		}
#endif

		return finished_sound;
	}
	else
		return 0;
}

void gameplan_sound_w(int offset,int data)
{
#ifdef VERBOSE
	if (errorlog)  fprintf(errorlog, "GAME: write reg%X with %02x at PC %04x\n", offset, data, cpu_get_pc());
#endif

	if (offset == 1)
	{
#ifdef VERBOSE
		if (errorlog) fprintf(errorlog, "[GAME: request sound number %d]\n", data);
#endif

		if (cb2 == 0)
		{
//	enabling this causes a hang in Challenger when entering high score name
//			cpu_set_reset_line(1,PULSE_LINE);
			return;
		}

		port_b = data;
		finished_sound = 0;
		new_request = 1;

		/* shortly after requesting a sound, the game board checks
		   whether the sound board has ackknowledged receipt of the
		   command - yield now to allow it to send the ACK */
//		cpu_yield();	enabling this causes a hang in Challenger when entering high score name
	}
	else if (offset == 0x0c)	/* PCR */
	{
		if (data & 0x80)
		{
			if ((data & 0x60) == 0x60)
				cb2 = 1;
			else if ((data & 0x60) == 0x40)
				cb2 = 0;
			else cb2 = -1;
		}
	}
}

int gameplan_via5_r(int offset)
{
#ifdef VERBOSE
	if (errorlog)  fprintf(errorlog, "SOUND:  read reg%X at PC %04x\n", offset, cpu_get_pc());
#endif

	if (offset == 0)
	{
		new_request = 0;
#ifdef VERBOSE
		if (errorlog) fprintf(errorlog, "[SOUND: received sound request %d]\n", port_b);
#endif
		return port_b;
	}

	if (offset == 5)
	{
		if (new_request == 1)
		{
#ifdef VERBOSE
			if (errorlog) fprintf(errorlog, "[SOUND: checking for new request - found]\n");
#endif
			return 0x40;
		}
		else
		{
#ifdef VERBOSE
			if (errorlog) fprintf(errorlog, "[SOUND: checking for new request - none]\n");
#endif
			return 0;
		}
	}

	return 1;
}

void gameplan_via5_w(int offset, int data)
{
#ifdef VERBOSE
	if (errorlog)  fprintf(errorlog, "SOUND: write reg%X with %02x at PC %04x\n", offset, data, cpu_get_pc());
#endif

	if (offset == 2)
	{
#ifdef VERBOSE
		if (errorlog) fprintf(errorlog, "[SOUND: ack received request %d]\n", data);
#endif
		finished_sound = data;
	}
}

int gameplan_video_r(int offset)
{
	static int x;
	x++;
#if 0
	if (errorlog) fprintf(errorlog, "%04x: reading %d from 200d\n", cpu_get_pc(), x);
#endif
	return x;
}

void gameplan_video_w(int offset,int data)
{
	static int r0 = -1;
	static unsigned char xpos, ypos, colour = 7;

#ifdef VERBOSE
	if (errorlog)  fprintf(errorlog, "VIA 1: PC %04x: %x -> reg%X\n", cpu_get_pc(), data, offset);
#endif

	if (offset == 0)			/* write to 2000 */
	{
		r0 = data;
#ifdef VERBOSE
		if (errorlog) fprintf(errorlog, "  mode = %d\n", data);
#endif
	}
	else if (offset == 1)		/* write to 2001 */
	{
		if (r0 == 0)
		{
			if (gameplan_this_is_kaos)
				colour = ~data & 0x07;
			else if (data & 0x0f)
			{
#ifdef VERBOSE
				if (errorlog) fprintf(errorlog, "  !movement command %02x unknown\n", data);
#endif
			}

#ifdef VERBOSE_VIDEO
			if (errorlog)
			{
#ifdef SHOW_CHARS
				fprintf(errorlog, "%c", colour_names[colour][0]);
#else
				fprintf(errorlog, "  line command %02x at (%d, %d) col %d (%s)\n", data, xpos, ypos, colour, colour_names[colour]);
#endif
				fflush(errorlog);
			}
#endif

			if (data & 0x20)
			{
				if (data & 0x80)
					ypos--;
				else
					ypos++;
			}
			if (data & 0x10)
			{
				if (data & 0x40)
					xpos--;
				else
					xpos++;
			}

			plot_pixel2(Machine->scrbitmap, tmpbitmap, xpos, ypos, Machine->pens[colour]);
		}
		else if (r0 == 1)
		{
			xpos = data;
#ifdef VERBOSE_VIDEO
			if (errorlog)
#ifdef SHOW_CHARS
				fprintf(errorlog, "\n");
#else
				fprintf(errorlog, "  X = %d\n", xpos);
#endif
#endif
		}
		else if (r0 == 2)
		{
			ypos = data;
#ifdef VERBOSE_VIDEO
#ifndef SHOW_CHARS
			if (errorlog) fprintf(errorlog, "  Y = %d\n", ypos);
#endif
#endif
		}
		else if (r0 == 3)
		{
			if (offset == 1 && data == 0)
			{
#ifdef VERBOSE_VIDEO
				if (errorlog) fprintf(errorlog, "  clear screen\n");
#endif
				gameplan_clear_screen();
			}
#ifdef VERBOSE
			else if (errorlog)  fprintf(errorlog, "  !not clear screen: offset = %d, data = %d\n", offset, data);
#endif
		}
#ifdef VERBOSE
		else
		{
			if (errorlog)  fprintf(errorlog, "  !offset = %d, data = %02x\n", offset, data);
		}
#endif
	}
	else if (offset == 2)
	{
		if (data == 7)
		{
			/* This whole 'fix_clear_to_colour' and special casing for
			 * megatack thing is ugly, and doesn't even work properly.
			 */

			if (!gameplan_this_is_megatack || fix_clear_to_colour == -1)
				clear_to_colour = colour;
#ifdef VERBOSE_VIDEO
			if (errorlog)
			{
				if (fix_clear_to_colour == -1)
					fprintf(errorlog, "  clear screen colour = %d (%s)\n", colour, colour_names[colour]);
				else
					fprintf(errorlog, "  clear req colour %d hidden by fixed colour %d\n", colour, fix_clear_to_colour);
			}
#endif
		}
#ifdef VERBOSE
		else
			if (errorlog)  fprintf(errorlog, "  !offset = %d, data = %02x\n", offset, data);
#endif
	}
	else if (offset == 3)
	{
		if (r0 == 0)
		{
#ifdef VERBOSE
			if (errorlog && (data & 0xf8) != 0xf8)  fprintf(errorlog, "  !unknown data (%02x) written for pixel (%3d, %3d)\n", data, xpos, ypos);
#endif

			colour = data & 7;
#ifdef VERBOSE_VIDEO
#ifndef SHOW_CHARS
			if (errorlog)  fprintf(errorlog, "  colour %d, move to (%d, %d)\n", colour, xpos, ypos);
#endif
#endif
		}
		else if ((data & 0xf8) == 0xf8 && data != 0xff)
		{
			clear_to_colour = fix_clear_to_colour = data & 0x07;
#ifdef VERBOSE
			if (errorlog) fprintf(errorlog, "  unusual colour request %d\n", data & 7);
#endif
		}
#ifdef VERBOSE
		else
			if (errorlog) fprintf(errorlog, "  !offset = %d, data = %02x\n", offset, data);
#endif
	}
#ifdef VERBOSE
	else
		if (errorlog)  fprintf(errorlog, "  !offset = %d, data = %02x\n", offset, data);
#endif
}


/***************************************************************************

  Draw the game screen in the given osd_bitmap.
  Do NOT call osd_update_display() from this function, it will be called by
  the main emulation engine.

***************************************************************************/


void gameplan_clear_screen(void)
{
#ifdef VERBOSE_VIDEO
	if (errorlog)  fprintf(errorlog, "  clearing the screen to colour %d (%s)\n", clear_to_colour, colour_names[clear_to_colour]);
#endif

	fillbitmap(tmpbitmap, Machine->pens[clear_to_colour], 0);
	fillbitmap(Machine->scrbitmap, Machine->pens[clear_to_colour], 0);

	fix_clear_to_colour = -1;
}
