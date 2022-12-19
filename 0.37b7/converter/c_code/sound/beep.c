/***************************************************************************
	beep.c

	This is used for computers/systems which can only output a constant tone.
	This tone can be turned on and off.
	e.g. PCW and PCW16 computer systems
	KT - 25-Jun-2000

	Sound handler
****************************************************************************/
#include "driver.h"
#include "sound/beep.h"

struct beep_sound
{
	int 	stream; 	/* stream number */
	int 	enable; 	/* enable beep */
	int 	frequency;	/* set frequency - this can be changed using the appropiate function */
	int 	incr;		/* initial wave state */
	INT16	signal; 	/* current signal */
};

static struct beep_interface *intf;
static struct beep_sound beeps[MAX_BEEP];

/************************************/
/* Stream updater                   */
/************************************/
static void beep_sound_update( int num, INT16 *sample, int length )
{
	INT16 signal = beeps[num].signal;
	int clock = 0, rate = Machine->sample_rate / 2;

    /* get progress through wave */
	int incr = beeps[num].incr;

	if (beeps[num].frequency > 0)
		clock = beeps[num].frequency;

	/* if we're not enabled, just fill with 0 */
	if ( !beeps[num].enable || Machine->sample_rate == 0 || clock == 0 )
	{
		memset( sample, 0, length * sizeof( INT16 ) );
		return;
	}

	/* fill in the sample */
	while( length-- > 0 )
	{
		*sample++ = signal;
		incr -= clock;
		while( incr < 0 )
		{
			incr += rate;
			signal = -signal;
		}
	}

	/* store progress through wave */
	beeps[num].incr = incr;
	beeps[num].signal = signal;
}

/************************************/
/* Sound handler start              */
/************************************/
int beep_sh_start( const struct MachineSound *msound ) 
{
	int i;

	intf = msound->sound_interface;

	for (i=0; i < intf->num; i++)
	{
		struct beep_sound *pBeep = &beeps[i];
		char buf[32];

		if( intf->num > 1 )
			sprintf(buf, "Beep #%d", i+1);
		else
			strcpy(buf, "Beep");

		pBeep->stream = stream_init( "Generic Beep", intf->mixing_level[i], Machine->sample_rate, i, beep_sound_update );
		pBeep->enable = 0;
		pBeep->frequency = 3250;
		pBeep->incr = 0;
		pBeep->signal = 0x07fff;
	}
	return 0;
}

/************************************/
/* Sound handler stop               */
/************************************/
void beep_sh_stop( void )
{
}

/************************************/
/* Sound handler update 			*/
/************************************/
void beep_sh_update(void )
{
     int i;

#if MAME_DEBUG
	if (intf == NULL)
	{
		logerror("beep_sh_update: sound driver not initialized\n");
		return;
    }
#endif
	for (i=0; i < intf->num; i++)
		stream_update( beeps[i].stream, 0 );
}

/***************************************************/
/* changing state to on from off will restart tone */
/***************************************************/
void beep_set_state( int num, int on )
{
#if MAME_DEBUG
	if (intf == NULL)
	{
		logerror("beep_set_state: sound driver not initialized\n");
		return;
	}
	if (num >= intf->num)
	{
		logerror("beep_set_state: num (%d) out of range (%d)\n", num, intf->num);
        return;
    }
#endif
    /* only update if new state is not the same as old state */
	if (beeps[num].enable == on)
		return;

	stream_update( beeps[num].stream, 0 );

	beeps[num].enable = on;
	/* restart wave from beginning */
	beeps[num].incr = 0;
	beeps[num].signal = 0x07fff;
}

/***************************************************/
/* setting new frequency starts from beginning	   */
/***************************************************/
void beep_set_frequency(int num,int frequency)
{
#if MAME_DEBUG
	if (intf == NULL)
	{
		logerror("beep_set_frequency: sound driver not initialized\n");
		return;
	}
	if (num >= intf->num)
	{
		logerror("beep_set_frequency: num (%d) out of range (%d)\n", num, intf->num);
        return;
    }
#endif
	if (beeps[num].frequency == frequency)
		return;

	stream_update(beeps[num].stream,num);
	beeps[num].frequency = frequency;
	beeps[num].signal = 0x07fff;
	beeps[num].incr = 0;
}

/***************************************************/
/* change a channel volume						   */
/***************************************************/
void beep_set_volume(int num, int volume)
{
#if MAME_DEBUG
	if (intf == NULL)
	{
		logerror("beep_set_volume: sound driver not initialized\n");
		return;
	}
	if (num >= intf->num)
	{
		logerror("beep_set_volume: num (%d) out of range (%d)\n", num, intf->num);
        return;
    }
#endif
	stream_update( beeps[num].stream, 0 );

	volume = 100 * volume / 7;

	mixer_set_volume( beeps[num].stream, volume );
}
