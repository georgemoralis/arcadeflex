/*****************************************************************************/
/*                                                                           */
/*                    (C) Copyright 1998  Peter J.C.Clare                    */
/*                                                                           */
/*****************************************************************************/
/*                                                                           */
/*                                                                           */
/*      Module name:    carnival.c                                           */
/*                                                                           */
/*      Creation date:  15/03/98                Revision date:  09/01/99     */
/*                                                                           */
/*      Produced by:    Peter J.C.Clare                                      */
/*                                                                           */
/*                                                                           */
/*      Abstract:                                                            */
/*                                                                           */
/*              MAME sound & music driver for Sega/Gremlin Carnival.         */
/*                                                                           */
/*****************************************************************************/
/*                                                                           */
/*      Acknowledgements:                                                    */
/*                                                                           */
/*      Mike Coates, for the original Carnival MAME sound driver.            */
/*      Virtu-Al, for the sound samples & hardware information.              */
/*      The MAME Team, for the emulator framework.                           */
/*                                                                           */
/*****************************************************************************/
/*                                                                           */
/*      Revision history                                                     */
/*      ================                                                     */
/*                                                                           */
/*      Date    Vsn.    Initials        Description                          */
/*      ~~~~    ~~~~    ~~~~~~~~        ~~~~~~~~~~~                          */
/*                                                                           */
/*****************************************************************************/

#include "driver.h"


#define CPU_MUSIC_ID            1       /* music CPU id number */


/* output port 0x01 definitions - sound effect drive outputs */
#define OUT_PORT_1_RIFLE_SHOT   0x01
#define OUT_PORT_1_CLANG        0x02
#define OUT_PORT_1_DUCK_1       0x04
#define OUT_PORT_1_DUCK_2       0x08
#define OUT_PORT_1_DUCK_3       0x10
#define OUT_PORT_1_PIPE_HIT     0x20
#define OUT_PORT_1_BONUS_1      0x40
#define OUT_PORT_1_BONUS_2      0x80


/* output port 0x02 definitions - sound effect drive outputs */
#define OUT_PORT_2_BEAR         0x04
#define OUT_PORT_2_MUSIC_T1     0x08
#define OUT_PORT_2_MUSIC_RESET  0x10
#define OUT_PORT_2_RANKING      0x20


/* music CPU port definitions */
#define MUSIC_PORT2_PSG_BDIR    0x40    /* bit 6 on P2 */
#define MUSIC_PORT2_PSG_BC1     0x80    /* bit 7 on P2 */


#define PSG_BC_INACTIVE         0
#define PSG_BC_READ             MUSIC_PORT2_PSG_BC1
#define PSG_BC_WRITE            MUSIC_PORT2_PSG_BDIR
#define PSG_BC_LATCH_ADDRESS    ( MUSIC_PORT2_PSG_BDIR | MUSIC_PORT2_PSG_BC1 )


#define PLAY(id,loop)           sample_start( id, id, loop )
#define STOP(id)                sample_stop( id )


/* sample file names */
const char *carnival_sample_names[] =
{
	"*carnival",
	"bear.wav",
	"bonus1.wav",
	"bonus2.wav",
	"clang.wav",
	"duck1.wav",
	"duck2.wav",
	"duck3.wav",
	"pipehit.wav",
	"ranking.wav",
	"rifle.wav",
	0
};

/* sample sound IDs - must match sample file name table above */
enum
{
	SND_BEAR = 0,
	SND_BONUS_1,
	SND_BONUS_2,
	SND_CLANG,
	SND_DUCK_1,
	SND_DUCK_2,
	SND_DUCK_3,
	SND_PIPE_HIT,
	SND_RANKING,
	SND_RIFLE_SHOT
};


static int port2State = 0;
static int psgData = 0;


void carnival_sh_port1_w( int offset, int data )
{
	static int port1State = 0;
	int bitsChanged;
	int bitsGoneHigh;
	int bitsGoneLow;


	/* U64 74LS374 8 bit latch */

	/* bit 0: connector pin 36 - rifle shot */
	/* bit 1: connector pin 35 - clang */
	/* bit 2: connector pin 33 - duck #1 */
	/* bit 3: connector pin 34 - duck #2 */
	/* bit 4: connector pin 32 - duck #3 */
	/* bit 5: connector pin 31 - pipe hit */
	/* bit 6: connector pin 30 - bonus #1 */
	/* bit 7: connector pin 29 - bonus #2 */

	bitsChanged  = port1State ^ data;
	bitsGoneHigh = bitsChanged & data;
	bitsGoneLow  = bitsChanged & ~data;

	port1State = data;

	if ( bitsGoneLow & OUT_PORT_1_RIFLE_SHOT )
	{
		PLAY( SND_RIFLE_SHOT, 0 );
	}

	if ( bitsGoneLow & OUT_PORT_1_CLANG )
	{
		PLAY( SND_CLANG, 0 );
	}
	if ( bitsGoneHigh & OUT_PORT_1_CLANG )
	{
		STOP( SND_CLANG );
	}

	if ( bitsGoneLow & OUT_PORT_1_DUCK_1 )
	{
		PLAY( SND_DUCK_1, 1 );
	}
	if ( bitsGoneHigh & OUT_PORT_1_DUCK_1 )
	{
		STOP( SND_DUCK_1 );
	}

	if ( bitsGoneLow & OUT_PORT_1_DUCK_2 )
	{
		PLAY( SND_DUCK_2, 1 );
	}
	if ( bitsGoneHigh & OUT_PORT_1_DUCK_2 )
	{
		STOP( SND_DUCK_2 );
	}

	if ( bitsGoneLow & OUT_PORT_1_DUCK_3 )
	{
		PLAY( SND_DUCK_3, 1 );
	}
	if ( bitsGoneHigh & OUT_PORT_1_DUCK_3 )
	{
		STOP( SND_DUCK_3 );
	}

	if ( bitsGoneLow & OUT_PORT_1_PIPE_HIT )
	{
		PLAY( SND_PIPE_HIT, 0 );
	}

	if ( bitsGoneLow & OUT_PORT_1_BONUS_1 )
	{
		PLAY( SND_BONUS_1, 0 );
	}

	if ( bitsGoneLow & OUT_PORT_1_BONUS_2 )
	{
		PLAY( SND_BONUS_2, 0 );
	}
}


void carnival_sh_port2_w( int offset, int data )
{
	int bitsChanged;
	int bitsGoneHigh;
	int bitsGoneLow;

	/* U63 74LS374 8 bit latch */

	/* bit 0: connector pin 48 */
	/* bit 1: connector pin 47 */
	/* bit 2: connector pin 45 - bear */
	/* bit 3: connector pin 46 - Music !T1 input */
	/* bit 4: connector pin 44 - Music reset */
	/* bit 5: connector pin 43 - ranking */
	/* bit 6: connector pin 42 */
	/* bit 7: connector pin 41 */

	bitsChanged  = port2State ^ data;
	bitsGoneHigh = bitsChanged & data;
	bitsGoneLow  = bitsChanged & ~data;

	port2State = data;

	if ( bitsGoneLow & OUT_PORT_2_BEAR )
	{
		PLAY( SND_BEAR, 0 );
	}

	if ( bitsGoneLow & OUT_PORT_2_RANKING )
	{
		PLAY( SND_RANKING, 0 );
	}

	if ( bitsGoneHigh & OUT_PORT_2_MUSIC_RESET )
	{
		/* reset output is no longer asserted active low */
		cpu_set_reset_line( CPU_MUSIC_ID, PULSE_LINE );
	}
}


int carnival_music_port_t1_r( int offset )
{
	/* note: 8039 T1 signal is inverted on music board */
	return ( port2State & OUT_PORT_2_MUSIC_T1 ) ? 0 : 1;
}


void carnival_music_port_1_w( int offset, int data )
{
	psgData = data;
}


void carnival_music_port_2_w( int offset, int data )
{
	static int psgSelect = 0;
	int newSelect;

	newSelect = data & ( MUSIC_PORT2_PSG_BDIR | MUSIC_PORT2_PSG_BC1 );
	if ( psgSelect != newSelect )
	{
		psgSelect = newSelect;

		switch ( psgSelect )
		{
		case PSG_BC_INACTIVE:
			/* do nowt */
			break;

		case PSG_BC_READ:
			/* not very sensible for a write */
			break;

		case PSG_BC_WRITE:
			AY8910_write_port_0_w( 0, psgData );
			break;

		case PSG_BC_LATCH_ADDRESS:
			AY8910_control_port_0_w( 0, psgData );
			break;
		}
	}
}
