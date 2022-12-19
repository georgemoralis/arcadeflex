/****************************************************************************
 *
 * geebee.c
 *
 * sound driver
 * juergen buchmueller <pullmoll@t-online.de>, jan 2000
 *
 ****************************************************************************/

#include <math.h>
#include "driver.h"

static void *volume_timer = NULL;
static UINT16 *decay = NULL;
static int channel;
static int sound_latch = 0;
static int sound_signal = 0;
static int volume = 0;
static int noise = 0;

static void volume_decay(int param)
{
	if( --volume < 0 )
		volume = 0;
}

WRITE_HANDLER( geebee_sound_w )
{
	stream_update(channel,0);
	sound_latch = data;
	volume = 0x7fff; /* set volume */
	noise = 0x0000;  /* reset noise shifter */
	/* faster decay enabled? */
	if( sound_latch & 8 )
	{
		/*
		 * R24 is 10k, Rb is 0, C57 is 1uF
		 * charge time t1 = 0.693 * (R24 + Rb) * C57 -> 0.22176s
		 * discharge time t2 = 0.693 * (Rb) * C57 -> 0
		 * Then C33 is only charged via D6 (1N914), not discharged!
		 * Decay:
		 * discharge C33 (1uF) through R50 (22k) -> 0.14058s
		 */
		if( volume_timer )
			timer_remove(volume_timer);
		volume_timer = timer_pulse(TIME_IN_HZ(32768/0.14058), 0, volume_decay);
	}
	else
	{
		/*
		 * discharge only after R49 (100k) in the amplifier section,
		 * so the volume shouldn't very fast and only when the signal
		 * is gated through 6N (4066).
		 * I can only guess here that the decay should be slower,
		 * maybe half as fast?
		 */
		if( volume_timer )
			timer_remove(volume_timer);
		volume_timer = timer_pulse(TIME_IN_HZ(32768/0.2906), 0, volume_decay);
    }
}

static void geebee_sound_update(int param, INT16 *buffer, int length)
{
    static int vcarry = 0;
    static int vcount = 0;

    while (length--)
    {
		*buffer++ = sound_signal;
		/* 1V = HSYNC = 18.432MHz / 3 / 2 / 384 = 8000Hz */
		vcarry -= 18432000 / 3 / 2 / 384;
        while (vcarry < 0)
        {
            vcarry += Machine->sample_rate;
            vcount++;
			/* noise clocked with raising edge of 2V */
			if ((vcount & 3) == 2)
			{
				/* bit0 = bit0 ^ !bit10 */
				if ((noise & 1) == ((noise >> 10) & 1))
					noise = ((noise << 1) & 0xfffe) | 1;
				else
					noise = (noise << 1) & 0xfffe;
			}
            switch (sound_latch & 7)
            {
            case 0: /* 4V */
				sound_signal = (vcount & 0x04) ? decay[volume] : 0;
                break;
            case 1: /* 8V */
				sound_signal = (vcount & 0x08) ? decay[volume] : 0;
                break;
            case 2: /* 16V */
				sound_signal = (vcount & 0x10) ? decay[volume] : 0;
                break;
            case 3: /* 32V */
				sound_signal = (vcount & 0x20) ? decay[volume] : 0;
                break;
            case 4: /* TONE1 */
				sound_signal = !(vcount & 0x01) && !(vcount & 0x10) ? decay[volume] : 0;
                break;
            case 5: /* TONE2 */
				sound_signal = !(vcount & 0x02) && !(vcount & 0x20) ? decay[volume] : 0;
                break;
            case 6: /* TONE3 */
				sound_signal = !(vcount & 0x04) && !(vcount & 0x40) ? decay[volume] : 0;
                break;
			default: /* NOISE */
				/* QH of 74164 #4V */
                sound_signal = (noise & 0x8000) ? decay[volume] : 0;
            }
        }
    }
}

int geebee_sh_start(const struct MachineSound *msound)
{
	int i;

	decay = (UINT16 *)malloc(32768 * sizeof(INT16));
	if( !decay )
		return 1;

    for( i = 0; i < 0x8000; i++ )
		decay[0x7fff-i] = (INT16) (0x7fff/exp(1.0*i/4096));

	channel = stream_init("GeeBee", 100, Machine->sample_rate, 0, geebee_sound_update);
    return 0;
}

void geebee_sh_stop(void)
{
	if( volume_timer )
		timer_remove(volume_timer);
	volume_timer = NULL;
    if( decay )
		free(decay);
	decay = NULL;
}

void geebee_sh_update(void)
{
	stream_update(channel,0);
}
