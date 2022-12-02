/****************************************************************************
 *
 * geebee.c
 *
 * machine driver
 * juergen buchmueller <pullmoll@t-online.de>, jan 2000
 *
 * TODO:
 * backdrop support for lamps? (player1, player2 and serve)
 *
 ****************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"

/* from sndhrdw/geebee.c */
extern void geebee_sound_w(int offs, int data);

/* globals */
int geebee_ball_h;
int geebee_ball_v;
int geebee_lamp1;
int geebee_lamp2;
int geebee_lamp3;
int geebee_counter;
int geebee_lock_out_coil;
int geebee_bgw;
int geebee_ball_on;
int geebee_inv;

#ifdef MAME_DEBUG
extern char geebee_msg[32+1];
extern int geebee_cnt;
#endif

int geebee_interrupt(void)
{
	cpu_set_irq_line(0, 0, PULSE_LINE);
    return ignore_interrupt();
}

int kaitei_interrupt(void)
{
	cpu_set_irq_line(0, 0, HOLD_LINE);
    return ignore_interrupt();
}

int geebee_in_r(int offs)
{
	int data = readinputport(offs & 3);
	if ((offs & 3) == 2)	/* combine with Bonus Life settings ? */
	{
		if (data & 0x02)	/* 5 lives? */
			data |= readinputport(5);
		else				/* 3 lives */
			data |= readinputport(4);
	}
	if (errorlog) fprintf(errorlog, "in_r %d $%02X\n", offs & 3, data);
	return data;
}

int navalone_in_r(int offs)
{
    int data = readinputport(offs & 3);
	if ((offs & 3) == 3)
	{
		int joy = readinputport(4);
		/* map digital two-way joystick to two fixed VOLIN values */
		if( joy & 1 ) data = 0xa0;
		if( joy & 2 ) data = 0x10;
	}
    if (errorlog) fprintf(errorlog, "in_r %d $%02X\n", offs & 3, data);
    return data;
}

void geebee_out6_w(int offs, int data)
{
    switch (offs&3)
    {
	case 0:
		if (errorlog) fprintf(errorlog, "out6_w:0 ball_h   $%02X\n", data);
        geebee_ball_h = data ^ 0xff;
        break;
    case 1:
		if (errorlog) fprintf(errorlog, "out6_w:1 ball_v   $%02X\n", data);
        geebee_ball_v = data ^ 0xff;
        break;
    case 2:
		if (errorlog) fprintf(errorlog, "out6_w:2 n/c      $%02X\n", data);
#ifdef MAME_DEBUG
        sprintf(geebee_msg, "out6_w:2 n/c $%02X", data);
		geebee_cnt = 30;
#endif
        break;
    default:
		if (errorlog) fprintf(errorlog, "out6_w:3 sound    $%02X\n", data);
		geebee_sound_w(offs, data);
        break;
    }
}

void geebee_out7_w(int offs, int data)
{
    switch (offs&7)
    {
    case 0:
		if (errorlog) fprintf(errorlog, "out7_w:0 lamp1    $%02X\n", data);
        geebee_lamp1 = data & 1;
		osd_led_w(0, geebee_lamp1);
        break;
    case 1:
		if (errorlog) fprintf(errorlog, "out7_w:1 lamp2    $%02X\n", data);
        geebee_lamp2 = data & 1;
		osd_led_w(1, geebee_lamp2);
        break;
    case 2:
		if (errorlog) fprintf(errorlog, "out7_w:2 lamp3    $%02X\n", data);
        geebee_lamp3 = data & 1;
		osd_led_w(2, geebee_lamp3);
        break;
    case 3:
		if (errorlog) fprintf(errorlog, "out7_w:3 counter  $%02X\n", data);
        geebee_counter = data & 1;
        break;
    case 4:
		if (errorlog) fprintf(errorlog, "out7_w:4 lockout  $%02X\n", data);
        geebee_lock_out_coil = data & 1;
        break;
    case 5:
		if (errorlog) fprintf(errorlog, "out7_w:5 bgw      $%02X\n", data);
        geebee_bgw = data & 1;
        break;
    case 6:
		if (errorlog) fprintf(errorlog, "out7_w:6 ball on  $%02X\n", data);
        geebee_ball_on = data & 1;
        break;
    case 7:
		if (errorlog) fprintf(errorlog, "out7_w:7 inv      $%02X\n", data);
		if( geebee_inv != (data & 1) )
			memset(dirtybuffer, 1, videoram_size);
        geebee_inv = data & 1;
        break;
    }
}


