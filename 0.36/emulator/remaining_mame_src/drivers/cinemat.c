/***************************************************************************

Cinematronics vector game handlers

driver by Aaron Giles

Special thanks to Neil Bradley, Zonn Moore, and Jeff Mitchell of the
Retrocade Alliance

to do:

* Fix Sundance controls

***************************************************************************/


#include "driver.h"
#include "vidhrdw/generic.h"
#include "vidhrdw/vector.h"
#include "cpu/ccpu/ccpu.h"


/* from vidhrdw/cinemat.c */
extern void cinemat_select_artwork (int monitor, int overlay_req, int backdrop_req, struct artwork_element *simple_overlay);
extern void cinemat_init_colors (unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom);
extern int cinemat_vh_start (void);
extern void cinemat_vh_stop (void);
extern void cinemat_vh_screenrefresh (struct osd_bitmap *bitmap, int full_refresh);
extern int cinemat_clear_list(void);

extern void spacewar_init_colors (unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom);
extern int spacewar_vh_start (void);
extern void spacewar_vh_stop (void);
extern void spacewar_vh_screenrefresh (struct osd_bitmap *bitmap, int full_refresh);

extern struct artwork_element starcas_overlay[];
extern struct artwork_element tailg_overlay[];
extern struct artwork_element sundance_overlay[];
extern struct artwork_element solarq_overlay[];

/* from sndhrdw/cinemat.c */
extern void cinemat_sound_init (void);
extern void starcas_sound(UINT8 sound_val, UINT8 bits_changed);
extern void solarq_sound(UINT8 sound_val, UINT8 bits_changed);
extern void ripoff_sound(UINT8 sound_val, UINT8 bits_changed);
extern void spacewar_sound(UINT8 sound_val, UINT8 bits_changed);


static int cinemat_readport (int offset);
static void cinemat_writeport (int offset, int data);

static int speedfrk_readports (int offset);
static int boxingb_readports (int offset);

static struct MemoryReadAddress readmem[] =
{
	{ 0x0000, 0x7fff, MRA_ROM },
	{ -1 }	/* end of table */
};

static struct MemoryWriteAddress writemem[] =
{
	{ 0x0000, 0x7fff, MWA_ROM },
	{ -1 }	/* end of table */
};

static struct IOReadPort readport[] =
{
	{ 0, CCPU_PORT_MAX, cinemat_readport },
	{ -1 }  /* end of table */
};

static struct IOWritePort writeport[] =
{
	{ 0, CCPU_PORT_MAX, cinemat_writeport },
	{ -1 }  /* end of table */
};

static struct IOReadPort speedfrk_readport[] =
{
	{ 0, CCPU_PORT_MAX, speedfrk_readports },
	{ -1 }  /* end of table */
};

static struct IOReadPort boxingb_readport[] =
{
	{ 0, CCPU_PORT_MAX, boxingb_readports },
	{ -1 }  /* end of table */
};

static int cinemat_outputs = 0xff;

static int cinemat_readport (int offset)
{
	switch (offset)
	{
		case CCPU_PORT_IOSWITCHES:
			return readinputport (0);

		case CCPU_PORT_IOINPUTS:
			return (readinputport (1) << 8) + readinputport (2);

		case CCPU_PORT_IOOUTPUTS:
			return cinemat_outputs;

		case CCPU_PORT_IN_JOYSTICKX:
			return readinputport (3);

		case CCPU_PORT_IN_JOYSTICKY:
			return readinputport (4);
	}

	return 0;
}

static void (*cinemat_sound_handler) (UINT8, UINT8);

static void cinemat_writeport (int offset, int data)
{
	switch (offset)
	{
		case CCPU_PORT_IOOUTPUTS:
            if ((cinemat_outputs ^ data) & 0x9f)
            {
                if (cinemat_sound_handler)
                    cinemat_sound_handler (data & 0x9f, (cinemat_outputs ^ data) & 0x9f);

            }
            cinemat_outputs = data;
			break;
	}
}



/* Note: the CPU speed is somewhat arbitrary as the cycle timings in
   the core are incomplete. */
#define CINEMA_MACHINE(driver, minx, miny, maxx, maxy) \
static struct MachineDriver machine_driver_##driver = \
{ \
	/* basic machine hardware */ \
	{ \
		{ \
			CPU_CCPU, \
			5000000, \
			readmem,writemem,readport,writeport, \
			cinemat_clear_list, 1 \
		} \
	}, \
	38, 0,	/* frames per second, vblank duration (vector game, so no vblank) */ \
	1, \
	driver##_init_machine, \
\
	/* video hardware */ \
	400, 300, { minx, maxx, miny, maxy }, \
	0, \
	256, 256, \
 	cinemat_init_colors, \
\
	VIDEO_TYPE_VECTOR, \
	0, \
	cinemat_vh_start, \
	cinemat_vh_stop, \
	cinemat_vh_screenrefresh, \
\
	/* sound hardware */ \
	0,0,0,0, \
	{ \
		{ \
			SOUND_SAMPLES, \
			&driver##_samples_interface \
		} \
	} \
};

#define CINEMA_MACHINEX(driver, minx, miny, maxx, maxy) \
static struct MachineDriver machine_driver_##driver = \
{ \
	/* basic machine hardware */ \
	{ \
		{ \
			CPU_CCPU, \
			5000000, \
			readmem,writemem,driver##_readport,writeport, \
			cinemat_clear_list, 1 \
		} \
	}, \
	38, 0,	/* frames per second, vblank duration (vector game, so no vblank) */ \
	1, \
	driver##_init_machine, \
\
	/* video hardware */ \
	400, 300, { minx, maxx, miny, maxy }, \
	0, \
	256, 256, \
 	cinemat_init_colors, \
\
	VIDEO_TYPE_VECTOR, \
	0, \
	cinemat_vh_start, \
	cinemat_vh_stop, \
	cinemat_vh_screenrefresh, \
\
	/* sound hardware */ \
	0,0,0,0, \
	{ \
		{ \
			SOUND_SAMPLES, \
			&driver##_samples_interface \
		} \
	} \
};



/* switch definitions are all mangled; for ease of use, I created these handy macros */

#define SW7 0x40
#define SW6 0x02
#define SW5 0x04
#define SW4 0x08
#define SW3 0x01
#define SW2 0x20
#define SW1 0x10

#define SW7OFF SW7
#define SW6OFF SW6
#define SW5OFF SW5
#define SW4OFF SW4
#define SW3OFF SW3
#define SW2OFF SW2
#define SW1OFF SW1

#define SW7ON  0
#define SW6ON  0
#define SW5ON  0
#define SW4ON  0
#define SW3ON  0
#define SW2ON  0
#define SW1ON  0


/***************************************************************************

  Spacewar

***************************************************************************/

INPUT_PORTS_START( spacewar )
	PORT_START /* switches */
	PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 1 )
	PORT_DIPNAME( SW2|SW1, SW2ON|SW1ON, "Game Time" )
	PORT_DIPSETTING( SW2OFF|SW1OFF, "1:30/coin" )
	PORT_DIPSETTING( SW2ON |SW1ON,  "2:00/coin" )
	PORT_DIPSETTING( SW2ON |SW1OFF, "3:00/coin" )
	PORT_DIPSETTING( SW2OFF|SW1ON,  "4:00/coin" )
	PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_UNUSED )
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_BUTTON3 | IPF_PLAYER2 )
	PORT_BIT ( 0x04, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER1 )
	PORT_BIT ( 0x02, IP_ACTIVE_LOW,  IPT_BUTTON3 | IPF_PLAYER1 )
	PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER2 )

	PORT_START /* inputs high */
	PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER1 )
	PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER1 )
	PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 )
	PORT_BITX( 0x08, IP_ACTIVE_LOW,  0, "Option 0", KEYCODE_0_PAD, IP_JOY_NONE )
	PORT_BITX( 0x04, IP_ACTIVE_LOW,  0, "Option 5", KEYCODE_5_PAD, IP_JOY_NONE )
	PORT_BIT ( 0x02, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER2 )
	PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER1 )

	PORT_START /* inputs low */
	PORT_BITX( 0x80, IP_ACTIVE_LOW,  0, "Option 7", KEYCODE_7_PAD, IP_JOY_NONE )
	PORT_BITX( 0x40, IP_ACTIVE_LOW,  0, "Option 2", KEYCODE_2_PAD, IP_JOY_NONE )
	PORT_BITX( 0x20, IP_ACTIVE_LOW,  0, "Option 6", KEYCODE_6_PAD, IP_JOY_NONE )
	PORT_BITX( 0x10, IP_ACTIVE_LOW,  0, "Option 1", KEYCODE_1_PAD, IP_JOY_NONE )
	PORT_BITX( 0x08, IP_ACTIVE_LOW,  0, "Option 9", KEYCODE_9_PAD, IP_JOY_NONE )
	PORT_BITX( 0x04, IP_ACTIVE_LOW,  0, "Option 4", KEYCODE_4_PAD, IP_JOY_NONE )
	PORT_BITX( 0x02, IP_ACTIVE_LOW,  0, "Option 8", KEYCODE_8_PAD, IP_JOY_NONE )
	PORT_BITX( 0x01, IP_ACTIVE_LOW,  0, "Option 3", KEYCODE_3_PAD, IP_JOY_NONE )

	PORT_START /* joystick X */
	PORT_BIT ( 0xff, IP_ACTIVE_LOW,  IPT_UNUSED )

	PORT_START /* joystick Y */
	PORT_BIT ( 0xff, IP_ACTIVE_LOW,  IPT_UNUSED )
INPUT_PORTS_END



static const char *spacewar_sample_names[] =
{
	"*spacewar",
	"explode1.wav",
	"fire1.wav",
	"idle.wav",
	"thrust1.wav",
	"thrust2.wav",
	"pop.wav",
	"explode2.wav",
	"fire2.wav",
    0	/* end of array */
};

static struct Samplesinterface spacewar_samples_interface =
{
	8,	/* 8 channels */
	25,	/* volume */
	spacewar_sample_names
};

void spacewar_init_machine (void)
{
	ccpu_Config (0, CCPU_MEMSIZE_4K, CCPU_MONITOR_BILEV);
    cinemat_sound_handler = spacewar_sound;
}

static struct MachineDriver machine_driver_spacewar =
{
	/* basic machine hardware */
	{
		{
			CPU_CCPU,
			5000000,
			readmem,writemem,readport,writeport,
			cinemat_clear_list, 1
		}
	},
	38, 0,	/* frames per second, vblank duration (vector game, so no vblank) */
	1,
	spacewar_init_machine,

	/* video hardware */
	400, 300, { 0, 1024, 0, 768 },
	0,
	256, 256,
 	spacewar_init_colors,

	VIDEO_TYPE_VECTOR,
	0,
	spacewar_vh_start,
	spacewar_vh_stop,
	spacewar_vh_screenrefresh,

	/* sound hardware */
	0,0,0,0,
	{
		{
			SOUND_SAMPLES,
			&spacewar_samples_interface
		}
	}
};


/***************************************************************************

  Barrier

***************************************************************************/

INPUT_PORTS_START( barrier )
	PORT_START /* switches */
	PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 1 )
	PORT_DIPNAME( SW1, SW1ON, "Innings/Game" )
	PORT_DIPSETTING(    SW1ON,  "3" )
	PORT_DIPSETTING(    SW1OFF, "5" )
	PORT_DIPNAME( SW2, SW2OFF, DEF_STR( Demo_Sounds ) )
	PORT_DIPSETTING(    SW2ON,  DEF_STR( Off ) )
	PORT_DIPSETTING(    SW2OFF, DEF_STR( On ) )
	PORT_BIT ( SW7|SW6|SW5|SW4|SW3, IP_ACTIVE_HIGH, IPT_UNUSED )

	PORT_START /* inputs high */
	PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_PLAYER2 )
	PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_PLAYER1 )
	PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_JOYSTICK_UP | IPF_4WAY | IPF_PLAYER2 )
	PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_JOYSTICK_UP | IPF_4WAY | IPF_PLAYER1 )
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_START1 )
	PORT_BIT ( 0x04, IP_ACTIVE_LOW,  IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_PLAYER2 )
	PORT_BIT ( 0x02, IP_ACTIVE_LOW,  IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_PLAYER1 )
	PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_PLAYER2 )

	PORT_START /* inputs low */
	PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BITX( 0x40, IP_ACTIVE_LOW,  0, "Skill C", KEYCODE_C, IP_JOY_NONE )
	PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_START2 )
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_PLAYER1 )
	PORT_BITX( 0x04, IP_ACTIVE_LOW,  0, "Skill B", KEYCODE_B, IP_JOY_NONE )
	PORT_BIT ( 0x02, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BITX( 0x01, IP_ACTIVE_LOW,  0, "Skill A", KEYCODE_A, IP_JOY_NONE )

	PORT_START /* joystick X */
	PORT_BIT ( 0xff, IP_ACTIVE_LOW,  IPT_UNUSED )

	PORT_START /* joystick Y */
	PORT_BIT ( 0xff, IP_ACTIVE_LOW,  IPT_UNUSED )
INPUT_PORTS_END


static const char *barrier_sample_names[] =
{
    0	/* end of array */
};

static struct Samplesinterface barrier_samples_interface =
{
	8,	/* 8 channels */
	25,	/* volume */
	barrier_sample_names
};


void barrier_init_machine (void)
{
	ccpu_Config (1, CCPU_MEMSIZE_4K, CCPU_MONITOR_BILEV);
    cinemat_sound_handler = 0;
}

void init_barrier(void)
{
	cinemat_select_artwork (CCPU_MONITOR_BILEV, 0, 0, 0);
}


CINEMA_MACHINE (barrier, 0, 0, 1024, 768)




/***************************************************************************

  Star Hawk

***************************************************************************/

/* TODO: 4way or 8way stick? */
INPUT_PORTS_START( starhawk )
	PORT_START /* switches */
	PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 2 )
	PORT_DIPNAME( SW7, SW7OFF, DEF_STR( Unknown ) )
	PORT_DIPSETTING( SW7OFF, DEF_STR( Off ) )
	PORT_DIPSETTING( SW7ON,  DEF_STR( On ) )
	PORT_BIT ( SW6, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT ( SW5, IP_ACTIVE_LOW, IPT_START2 )
	PORT_BIT ( SW4, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER1 )
	PORT_BIT ( SW3, IP_ACTIVE_LOW, IPT_START1 )
	PORT_DIPNAME( SW2|SW1, SW2OFF|SW1OFF, "Game Time" )
	PORT_DIPSETTING(    SW2OFF|SW1OFF, "2:00/4:00" )
	PORT_DIPSETTING(    SW2ON |SW1OFF, "1:30/3:00" )
	PORT_DIPSETTING(    SW2OFF|SW1ON,  "1:00/2:00" )
	PORT_DIPSETTING(    SW2ON |SW1ON,  "0:45/1:30" )

	PORT_START /* inputs high */
	PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_BUTTON4 | IPF_PLAYER2 )
	PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER1 )
	PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_BUTTON3 | IPF_PLAYER2 )
	PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON3 | IPF_PLAYER1 )
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2 )
	PORT_BIT ( 0x04, IP_ACTIVE_LOW,  IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2 )
	PORT_BIT ( 0x02, IP_ACTIVE_LOW,  IPT_BUTTON4 | IPF_PLAYER1 )
	PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER2 )

	PORT_START /* inputs low */
	PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2 )
	PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 )
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1 )
	PORT_BIT ( 0x04, IP_ACTIVE_LOW,  IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 )
	PORT_BIT ( 0x02, IP_ACTIVE_LOW,  IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1 )
	PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1 )

	PORT_START /* joystick X */
	PORT_BIT ( 0xff, IP_ACTIVE_LOW,  IPT_UNUSED )

	PORT_START /* joystick Y */
	PORT_BIT ( 0xff, IP_ACTIVE_LOW,  IPT_UNUSED )
INPUT_PORTS_END



static const char *starhawk_sample_names[] =
{
    0	/* end of array */
};

static struct Samplesinterface starhawk_samples_interface =
{
	8,	/* 8 channels */
	25,	/* volume */
	starhawk_sample_names
};


void starhawk_init_machine (void)
{
	ccpu_Config (1, CCPU_MEMSIZE_4K, CCPU_MONITOR_BILEV);
    cinemat_sound_handler = 0;
}

void init_starhawk(void)
{
	cinemat_select_artwork (CCPU_MONITOR_BILEV, 0, 0, 0);
}


CINEMA_MACHINE (starhawk, 0, 0, 1024, 768)




/***************************************************************************

  Star Castles

***************************************************************************/

INPUT_PORTS_START( starcas )
	PORT_START /* switches */
	PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 1 )
	PORT_BITX( SW7, SW7ON, IPT_DIPSWITCH_NAME | IPF_TOGGLE, "Test Pattern", KEYCODE_F2, IP_JOY_NONE )
	PORT_DIPSETTING( SW7ON,               DEF_STR( Off ) )
	PORT_DIPSETTING( SW7OFF,              DEF_STR( On ) )
	PORT_DIPNAME( SW4|SW3, SW4OFF|SW3OFF, DEF_STR( Coinage ) )
	PORT_DIPSETTING( SW4ON |SW3OFF,       DEF_STR( 2C_1C ) )
	PORT_DIPSETTING( SW4ON |SW3ON,        DEF_STR( 4C_3C ) )
	PORT_DIPSETTING( SW4OFF|SW3OFF,       DEF_STR( 1C_1C ) )
	PORT_DIPSETTING( SW4OFF|SW3ON,        DEF_STR( 2C_3C ) )
	PORT_DIPNAME( SW2|SW1, SW2OFF|SW1OFF, DEF_STR( Lives ) )
	PORT_DIPSETTING( SW2OFF|SW1OFF,       "3" )
	PORT_DIPSETTING( SW2ON |SW1OFF,       "4" )
	PORT_DIPSETTING( SW2OFF|SW1ON,        "5" )
	PORT_DIPSETTING( SW2ON |SW1ON,        "6" )
	PORT_BIT ( SW6|SW5, SW6OFF|SW5OFF, IPT_UNUSED )

	PORT_START /* inputs high */
	PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNKNOWN )
	PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNKNOWN )
	PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_UNKNOWN )
	PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON1 )
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_UNKNOWN )
	PORT_BIT ( 0x04, IP_ACTIVE_LOW,  IPT_BUTTON2 )
	PORT_BIT ( 0x02, IP_ACTIVE_LOW,  IPT_UNKNOWN )
	PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_JOYSTICK_RIGHT | IPF_2WAY )

	PORT_START /* inputs low */
	PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNKNOWN )
	PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_JOYSTICK_LEFT | IPF_2WAY )
	PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_UNKNOWN )
	PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_UNKNOWN )
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_UNKNOWN )
	PORT_BIT ( 0x04, IP_ACTIVE_LOW,  IPT_START2 )
	PORT_BIT ( 0x02, IP_ACTIVE_LOW,  IPT_UNKNOWN )
	PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_START1 )

	PORT_START /* joystick X */
	PORT_BIT ( 0xff, IP_ACTIVE_LOW,  IPT_UNKNOWN )

	PORT_START /* joystick Y */
	PORT_BIT ( 0xff, IP_ACTIVE_LOW,  IPT_UNKNOWN )
INPUT_PORTS_END



static const char *starcas_sample_names[] =
{
	"*starcas",
	"lexplode.wav",
	"sexplode.wav",
	"cfire.wav",
	"pfire.wav",
	"drone.wav",
	"shield.wav",
	"star.wav",
	"thrust.wav",
    0	/* end of array */
};

static struct Samplesinterface starcas_samples_interface =
{
	8,	/* 8 channels */
	25,	/* volume */
	starcas_sample_names
};

void starcas_init_machine (void)
{
	ccpu_Config (1, CCPU_MEMSIZE_8K, CCPU_MONITOR_BILEV);
    cinemat_sound_handler = starcas_sound;
    cinemat_sound_init ();
}

void init_starcas(void)
{
	cinemat_select_artwork (CCPU_MONITOR_BILEV, 1, 0, starcas_overlay);
}

CINEMA_MACHINE (starcas, 0, 0, 1024, 768)

/***************************************************************************

  Tailgunner

***************************************************************************/

INPUT_PORTS_START( tailg )
	PORT_START /* switches */
	PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 1 )
	PORT_DIPNAME( SW6|SW2|SW1, SW6OFF|SW2OFF|SW1OFF, "Shield Points" )
	PORT_DIPSETTING( SW6ON |SW2ON |SW1ON,  "15" )
	PORT_DIPSETTING( SW6ON |SW2OFF|SW1ON,  "20" )
	PORT_DIPSETTING( SW6ON |SW2ON |SW1OFF, "30" )
	PORT_DIPSETTING( SW6ON |SW2OFF|SW1OFF, "40" )
	PORT_DIPSETTING( SW6OFF|SW2ON |SW1ON,  "50" )
	PORT_DIPSETTING( SW6OFF|SW2OFF|SW1ON,  "60" )
	PORT_DIPSETTING( SW6OFF|SW2ON |SW1OFF, "70" )
	PORT_DIPSETTING( SW6OFF|SW2OFF|SW1OFF, "80" )
	PORT_DIPNAME( SW3, SW3OFF, DEF_STR( Coinage ) )
	PORT_DIPSETTING( SW3ON,                DEF_STR( 2C_1C ) )
	PORT_DIPSETTING( SW3OFF,               DEF_STR( 1C_1C ) )
	PORT_BIT ( SW7|SW5|SW4, IP_ACTIVE_HIGH, IPT_UNUSED )

	PORT_START /* inputs high */
	PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x04, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x02, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_UNUSED )

	PORT_START /* inputs low */
	PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_START1 )
	PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_BUTTON2 )
	PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_BUTTON1 )
	PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x04, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x02, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_UNUSED )

	PORT_START /* joystick X */
	PORT_ANALOG( 0xfff, 0x800, IPT_AD_STICK_X, 100, 50, 0x200, 0xe00 )

	PORT_START /* joystick Y */
	PORT_ANALOG( 0xfff, 0x800, IPT_AD_STICK_Y, 100, 50, 0x200, 0xe00 )
INPUT_PORTS_END



static const char *tailg_sample_names[] =
{
    0	/* end of array */
};

static struct Samplesinterface tailg_samples_interface =
{
	8,	/* 8 channels */
	25,	/* volume */
	tailg_sample_names
};


void tailg_init_machine (void)
{
	ccpu_Config (0, CCPU_MEMSIZE_8K, CCPU_MONITOR_BILEV);
    cinemat_sound_handler = 0;
}

void init_tailg(void)
{
	cinemat_select_artwork (CCPU_MONITOR_BILEV, 1, 0, tailg_overlay);
}

CINEMA_MACHINE (tailg, 0, 0, 1024, 768)



/***************************************************************************

  Ripoff

***************************************************************************/

INPUT_PORTS_START( ripoff )
	PORT_START /* switches */
	PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 1 )
	PORT_BITX( SW7, SW7OFF, IPT_DIPSWITCH_NAME | IPF_TOGGLE, DEF_STR( Service_Mode ), KEYCODE_F2, IP_JOY_NONE )
	PORT_DIPSETTING( SW7OFF,           DEF_STR( Off ) )
	PORT_DIPSETTING( SW7ON,            DEF_STR( On ) )
	PORT_DIPNAME( SW6, SW6OFF, "Scores" )
	PORT_DIPSETTING( SW6OFF,           "Individual" )
	PORT_DIPSETTING( SW6ON,            "Combined" )
	PORT_DIPNAME( SW5, SW5ON, DEF_STR( Demo_Sounds ) )
	PORT_DIPSETTING( SW5ON,            DEF_STR( Off ) )
	PORT_DIPSETTING( SW5OFF,           DEF_STR( On ) )
	PORT_DIPNAME( SW4|SW3, SW4ON|SW3ON, DEF_STR( Coinage ) )
	PORT_DIPSETTING(    SW4ON |SW3OFF, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    SW4OFF|SW3OFF, DEF_STR( 4C_3C ) )
	PORT_DIPSETTING(    SW4ON |SW3ON,  DEF_STR( 1C_1C ) )
	PORT_DIPSETTING(    SW4OFF|SW3ON,  DEF_STR( 2C_3C ) )
	PORT_DIPNAME( SW2|SW1, SW2OFF|SW1ON, "Fuel Pods" )
	PORT_DIPSETTING(    SW2ON |SW1OFF, "4" )
	PORT_DIPSETTING(    SW2OFF|SW1OFF, "8" )
	PORT_DIPSETTING(    SW2ON |SW1ON,  "12" )
	PORT_DIPSETTING(    SW2OFF|SW1ON,  "16" )

	PORT_START /* inputs high */
	PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER1 )
	PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER1 )
	PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER1 )
	PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER1 )
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x04, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x02, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_UNUSED )

	PORT_START /* inputs low */
	PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER2 )
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_START2 )
	PORT_BIT ( 0x04, IP_ACTIVE_LOW,  IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT ( 0x02, IP_ACTIVE_LOW,  IPT_START1 )
	PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2 )

	PORT_START /* joystick X */
	PORT_BIT ( 0xff, IP_ACTIVE_LOW,  IPT_UNUSED )

	PORT_START /* joystick Y */
	PORT_BIT ( 0xff, IP_ACTIVE_LOW,  IPT_UNUSED )
INPUT_PORTS_END



static const char *ripoff_sample_names[] =
{
	"*ripoff",
    "efire.wav",
	"eattack.wav",
	"bonuslvl.wav",
	"explosn.wav",
	"shipfire.wav",
	"bg1.wav",
	"bg2.wav",
	"bg3.wav",
	"bg4.wav",
	"bg5.wav",
	"bg6.wav",
	"bg7.wav",
	"bg8.wav",
    0	/* end of array */
};

static struct Samplesinterface ripoff_samples_interface =
{
	8,	/* 8 channels */
	25,	/* volume */
	ripoff_sample_names
};

void ripoff_init_machine (void)
{
	ccpu_Config (1, CCPU_MEMSIZE_8K, CCPU_MONITOR_BILEV);
    cinemat_sound_handler = ripoff_sound;
    cinemat_sound_init ();
}

void init_ripoff(void)
{
	cinemat_select_artwork (CCPU_MONITOR_BILEV, 0, 0, 0);
}


CINEMA_MACHINE (ripoff, 0, 0, 1024, 768)




/***************************************************************************

  Speed Freak

***************************************************************************/

static UINT8 speedfrk_steer[] = {0xe, 0x6, 0x2, 0x0, 0x3, 0x7, 0xf};

int speedfrk_in2_r(int offset)
{
    static int last_wheel=0, delta_wheel, last_frame=0, gear=0xe0;
	int val, current_frame;

	/* check the fake gear input port and determine the bit settings for the gear */
	if ((input_port_4_r(0) & 0xf0) != 0xf0)
        gear = input_port_4_r(0) & 0xf0;

    val = gear;

	/* add the start key into the mix */
	if (input_port_2_r(0) & 0x80)
        val |= 0x80;
	else
        val &= ~0x80;

	/* and for the cherry on top, we add the scrambled analog steering */
    current_frame = cpu_getcurrentframe();
    if (current_frame > last_frame)
    {
        /* the shift register is cleared once per 'frame' */
        delta_wheel = input_port_3_r(0) - last_wheel;
        last_wheel += delta_wheel;
        if (delta_wheel > 3)
            delta_wheel = 3;
        else if (delta_wheel < -3)
            delta_wheel = -3;
    }
    last_frame = current_frame;

    val |= speedfrk_steer[delta_wheel + 3];

	return val;
}

static int speedfrk_readports (int offset)
{
	switch (offset)
	{
		case CCPU_PORT_IOSWITCHES:
			return readinputport (0);

		case CCPU_PORT_IOINPUTS:
			return (readinputport (1) << 8) + speedfrk_in2_r (0);

		case CCPU_PORT_IOOUTPUTS:
			return cinemat_outputs;
	}

	return 0;
}

INPUT_PORTS_START( speedfrk )
	PORT_START /* switches */
	PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 1 )
	PORT_DIPNAME( SW7, SW7OFF, DEF_STR( Unknown ) )
	PORT_DIPSETTING( SW7OFF, DEF_STR( Off ) )
	PORT_DIPSETTING( SW7ON,  DEF_STR( On ) )
	PORT_DIPNAME( SW6, SW6OFF, DEF_STR( Unknown ) )
	PORT_DIPSETTING( SW6OFF, DEF_STR( Off ) )
	PORT_DIPSETTING( SW6ON,  DEF_STR( On ) )
	PORT_DIPNAME( SW5, SW5OFF, DEF_STR( Unknown ) )
	PORT_DIPSETTING( SW5OFF, DEF_STR( Off ) )
	PORT_DIPSETTING( SW5ON,  DEF_STR( On ) )
	PORT_DIPNAME( SW4, SW4OFF, DEF_STR( Unknown ) )
	PORT_DIPSETTING( SW4OFF, DEF_STR( Off ) )
	PORT_DIPSETTING( SW4ON,  DEF_STR( On ) )
	PORT_DIPNAME( SW3, SW3OFF, DEF_STR( Unknown ) )
	PORT_DIPSETTING( SW3OFF, DEF_STR( Off ) )
	PORT_DIPSETTING( SW3ON,  DEF_STR( On ) )
	PORT_DIPNAME( SW2|SW1, SW2OFF|SW1ON, "Extra Time" )
	PORT_DIPSETTING(    SW2ON |SW1ON,  "69" )
	PORT_DIPSETTING(    SW2ON |SW1OFF, "99" )
	PORT_DIPSETTING(    SW2OFF|SW1ON,  "129" )
	PORT_DIPSETTING(    SW2OFF|SW1OFF, "159" )

	PORT_START /* inputs high */
	PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x04, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x02, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER1 ) /* gas */

	PORT_START /* inputs low */
	PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_START1 )
	PORT_BIT ( 0x70, IP_ACTIVE_LOW,  IPT_UNUSED ) /* actually the gear shift, see fake below */
	PORT_ANALOG( 0x0f, 0x04, IPT_AD_STICK_X | IPF_CENTER, 25, 1, 0x00, 0x08 )

    PORT_START /* steering wheel */
	PORT_ANALOG( 0xff, 0x00, IPT_DIAL, 100, 1, 0x00, 0xff )

	PORT_START /* in4 - fake for gear shift */
	PORT_BIT ( 0x0f, IP_ACTIVE_HIGH, IPT_UNUSED )
	PORT_BITX( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER2, "1st gear", IP_KEY_DEFAULT, IP_JOY_DEFAULT )
	PORT_BITX( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER2, "2nd gear", IP_KEY_DEFAULT, IP_JOY_DEFAULT )
	PORT_BITX( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2, "3rd gear", IP_KEY_DEFAULT, IP_JOY_DEFAULT )
	PORT_BITX( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER2, "4th gear", IP_KEY_DEFAULT, IP_JOY_DEFAULT )
INPUT_PORTS_END



static const char *speedfrk_sample_names[] =
{
    0	/* end of array */
};

static struct Samplesinterface speedfrk_samples_interface =
{
	8,	/* 8 channels */
	25,	/* volume */
	speedfrk_sample_names
};


void speedfrk_init_machine (void)
{
	ccpu_Config (0, CCPU_MEMSIZE_8K, CCPU_MONITOR_BILEV);
    cinemat_sound_handler = 0;
}

void init_speedfrk(void)
{
	cinemat_select_artwork (CCPU_MONITOR_BILEV, 0, 0, 0);
}

/* we use custom input ports */
CINEMA_MACHINEX (speedfrk, 0, 0, 1024, 768)




/***************************************************************************

  Sundance

***************************************************************************/

INPUT_PORTS_START( sundance )
	PORT_START /* switches */
	PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 1 )
	PORT_BIT ( SW7|SW6|SW5, SW7OFF|SW6OFF|SW5OFF,  IPT_UNUSED )
	PORT_DIPNAME( SW4, SW4ON, DEF_STR( Coinage ) )
	PORT_DIPSETTING( SW4ON,  "1 coin/2 players" )
	PORT_DIPSETTING( SW4OFF, "2 coins/2 players" )
	PORT_DIPNAME( SW3, SW3ON, "Language" )
	PORT_DIPSETTING( SW3OFF, "Japanese" )
	PORT_DIPSETTING( SW3ON,  "English" )
	PORT_DIPNAME( SW2|SW1, SW2OFF|SW1ON, "Game Time" )
	PORT_DIPSETTING(    SW2ON |SW1ON,  "0:45/coin" )
	PORT_DIPSETTING(    SW2OFF|SW1ON,  "1:00/coin" )
	PORT_DIPSETTING(    SW2ON |SW1OFF, "1:30/coin" )
	PORT_DIPSETTING(    SW2OFF|SW1OFF, "2:00/coin" )

	PORT_START /* inputs high */
	PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED ) /* player 1 motion */
	PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED ) /* player 2 motion */
	PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_UNUSED ) /* player 1 motion */
	PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_UNUSED ) /* player 2 motion */
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_UNUSED ) /* 2 suns */
	PORT_BIT ( 0x04, IP_ACTIVE_LOW,  IPT_UNUSED ) /* player 1 motion */
	PORT_BIT ( 0x02, IP_ACTIVE_LOW,  IPT_UNUSED ) /* player 2 motion */
	PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_UNUSED ) /* player 1 motion */

	PORT_START /* inputs low */
	PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER1 )
	PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED ) /* 4 suns */
	PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_UNUSED ) /* Grid */
	PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_UNUSED ) /* 3 suns */
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_START2 )
	PORT_BIT ( 0x04, IP_ACTIVE_LOW,  IPT_START1 )
	PORT_BIT ( 0x02, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_UNUSED ) /* player 2 motion */

	PORT_START /* joystick X */
	PORT_BIT ( 0xff, IP_ACTIVE_LOW,  IPT_UNUSED )

	PORT_START /* joystick Y */
	PORT_BIT ( 0xff, IP_ACTIVE_LOW,  IPT_UNUSED )
INPUT_PORTS_END



static const char *sundance_sample_names[] =
{
    0	/* end of array */
};

static struct Samplesinterface sundance_samples_interface =
{
	8,	/* 8 channels */
	25,	/* volume */
	sundance_sample_names
};


void sundance_init_machine (void)
{
	ccpu_Config (1, CCPU_MEMSIZE_8K, CCPU_MONITOR_16LEV);
    cinemat_sound_handler = 0;
}

void init_sundance(void)
{
	cinemat_select_artwork (CCPU_MONITOR_16LEV, 1, 0, sundance_overlay);
}

CINEMA_MACHINE (sundance, 0, 0, 1024, 768)




/***************************************************************************

  Warrior

***************************************************************************/

INPUT_PORTS_START( warrior )
	PORT_START /* switches */
	PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 1 )
	PORT_DIPNAME( SW7, SW7OFF, DEF_STR( Unknown ) )
	PORT_DIPSETTING( SW7OFF, DEF_STR( Off ) )
	PORT_DIPSETTING( SW7ON,  DEF_STR( On ) )
	PORT_DIPNAME( SW6, SW6OFF, DEF_STR( Unknown ) )
	PORT_DIPSETTING( SW6OFF, DEF_STR( Off ) )
	PORT_DIPSETTING( SW6ON,  DEF_STR( On ) )
	PORT_DIPNAME( SW5, SW5OFF, DEF_STR( Unknown ) )
	PORT_DIPSETTING( SW5OFF, DEF_STR( Off ) )
	PORT_DIPSETTING( SW5ON,  DEF_STR( On ) )
	PORT_DIPNAME( SW4, SW4OFF, DEF_STR( Unknown ) )
	PORT_DIPSETTING( SW4OFF, DEF_STR( Off ) )
	PORT_DIPSETTING( SW4ON,  DEF_STR( On ) )
	PORT_DIPNAME( SW3, SW3ON, "Test Grid" )
	PORT_DIPSETTING( SW3ON,  DEF_STR( Off ) )
	PORT_DIPSETTING( SW3OFF, DEF_STR( On ) )
	PORT_DIPNAME( SW2, SW2OFF, DEF_STR( Unknown ) )
	PORT_DIPSETTING( SW2OFF, DEF_STR( Off ) )
	PORT_DIPSETTING( SW2ON,  DEF_STR( On ) )
	PORT_DIPNAME( SW1, SW1ON, DEF_STR( Unknown ) )
	PORT_DIPSETTING( SW1OFF, DEF_STR( Off ) )
	PORT_DIPSETTING( SW1ON,  DEF_STR( On ) )

	PORT_START /* inputs high */
	PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNKNOWN )
	PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_START1 )
	PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_START2 )
	PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER1 )
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_JOYSTICK_DOWN | IPF_PLAYER1 )
	PORT_BIT ( 0x04, IP_ACTIVE_LOW,  IPT_JOYSTICK_UP | IPF_PLAYER1 )
	PORT_BIT ( 0x02, IP_ACTIVE_LOW,  IPT_JOYSTICK_LEFT | IPF_PLAYER1 )
	PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_JOYSTICK_RIGHT | IPF_PLAYER1 )

	PORT_START /* inputs low */
	PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNKNOWN )
	PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNKNOWN )
	PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_UNKNOWN )
	PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_JOYSTICK_DOWN | IPF_PLAYER2 )
	PORT_BIT ( 0x04, IP_ACTIVE_LOW,  IPT_JOYSTICK_UP | IPF_PLAYER2 )
	PORT_BIT ( 0x02, IP_ACTIVE_LOW,  IPT_JOYSTICK_LEFT | IPF_PLAYER2 )
	PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_JOYSTICK_RIGHT | IPF_PLAYER2 )

	PORT_START /* joystick X */
	PORT_BIT ( 0xff, IP_ACTIVE_LOW,  IPT_UNUSED )

	PORT_START /* joystick Y */
	PORT_BIT ( 0xff, IP_ACTIVE_LOW,  IPT_UNUSED )
INPUT_PORTS_END



static const char *warrior_sample_names[] =
{
    0	/* end of array */
};

static struct Samplesinterface warrior_samples_interface =
{
	8,	/* 8 channels */
	25,	/* volume */
	warrior_sample_names
};


void warrior_init_machine (void)
{
	ccpu_Config (1, CCPU_MEMSIZE_8K, CCPU_MONITOR_BILEV);
    cinemat_sound_handler = 0;
}

void init_warrior(void)
{
	cinemat_select_artwork (CCPU_MONITOR_BILEV, 0, 1, 0);
}

CINEMA_MACHINE (warrior, 0, 0, 1024, 780)




/***************************************************************************

  Armor Attack

***************************************************************************/

INPUT_PORTS_START( armora )
	PORT_START /* switches */
	PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 1 )
	PORT_BITX( SW7, SW7ON, IPT_DIPSWITCH_NAME | IPF_TOGGLE, DEF_STR( Service_Mode ), KEYCODE_F2, IP_JOY_NONE )
	PORT_DIPSETTING( SW7ON,  DEF_STR( Off ) )
	PORT_DIPSETTING( SW7OFF, DEF_STR( On ) )
	PORT_DIPNAME( SW5, SW5OFF, DEF_STR( Demo_Sounds ) )
	PORT_DIPSETTING( SW5OFF, DEF_STR( Off ) )
	PORT_DIPSETTING( SW5ON,  DEF_STR( On ) )
	PORT_DIPNAME( SW4|SW3, SW4OFF|SW3OFF, DEF_STR( Coinage ) )
	PORT_DIPSETTING( SW4ON |SW3OFF, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING( SW4ON |SW3ON,  DEF_STR( 4C_3C ) )
	PORT_DIPSETTING( SW4OFF|SW3OFF, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING( SW4OFF|SW3ON,  DEF_STR( 2C_3C ) )
	PORT_DIPNAME( SW2|SW1, SW2OFF|SW1OFF, "Jeeps" )
	PORT_DIPSETTING( SW2ON |SW1ON,  "2" )
	PORT_DIPSETTING( SW2OFF|SW1ON,  "3" )
	PORT_DIPSETTING( SW2ON |SW1OFF, "4" )
	PORT_DIPSETTING( SW2OFF|SW1OFF, "5" )

	PORT_START /* inputs high */
	PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER1 )
	PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER1 )
	PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER1 )
	PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER1 )
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x04, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x02, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_UNUSED )

	PORT_START /* inputs low */
	PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER2 )
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_START2 )
	PORT_BIT ( 0x04, IP_ACTIVE_LOW,  IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT ( 0x02, IP_ACTIVE_LOW,  IPT_START1 )
	PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2 )

	PORT_START /* joystick X */
	PORT_BIT ( 0xff, IP_ACTIVE_LOW,  IPT_UNUSED )

	PORT_START /* joystick Y */
	PORT_BIT ( 0xff, IP_ACTIVE_LOW,  IPT_UNUSED )
INPUT_PORTS_END



static const char *armora_sample_names[] =
{
    0	/* end of array */
};

static struct Samplesinterface armora_samples_interface =
{
	8,	/* 8 channels */
	25,	/* volume */
	armora_sample_names
};


void armora_init_machine (void)
{
	ccpu_Config (1, CCPU_MEMSIZE_16K, CCPU_MONITOR_BILEV);
    cinemat_sound_handler = 0;
}

void init_armora(void)
{
	cinemat_select_artwork (CCPU_MONITOR_BILEV, 1, 0, 0);
}

CINEMA_MACHINE (armora, 0, 0, 1024, 772)



/***************************************************************************

  Solar Quest

***************************************************************************/

INPUT_PORTS_START( solarq )
	PORT_START /* switches */
	PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 1 )
	PORT_BITX( SW7, SW7ON, IPT_DIPSWITCH_NAME | IPF_TOGGLE, DEF_STR( Service_Mode ), KEYCODE_F2, IP_JOY_NONE )
	PORT_DIPSETTING( SW7ON,  DEF_STR( Off ) )
	PORT_DIPSETTING( SW7OFF, DEF_STR( On ) )
	PORT_DIPNAME( SW2, SW2OFF, "Extra Ship" )
	PORT_DIPSETTING( SW2OFF, "25 captures" )
	PORT_DIPSETTING( SW2ON,  "40 captures" )
	PORT_DIPNAME( SW6, SW6OFF, "Mode" )
	PORT_DIPSETTING( SW6OFF, "Normal" )
	PORT_DIPSETTING( SW6ON,  DEF_STR( Free_Play ) )
	PORT_DIPNAME( SW1|SW3, SW1OFF|SW3OFF, DEF_STR( Coinage ) )
	PORT_DIPSETTING( SW3ON |SW1OFF, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING( SW3ON |SW1ON,  DEF_STR( 4C_3C ) )
	PORT_DIPSETTING( SW3OFF|SW1OFF, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING( SW3OFF|SW1ON,  DEF_STR( 2C_3C ) )
	PORT_DIPNAME( SW5|SW4, SW5OFF|SW5OFF, "Ships" )
	PORT_DIPSETTING( SW5OFF|SW4OFF, "2" )
	PORT_DIPSETTING( SW5ON |SW4OFF, "3" )
	PORT_DIPSETTING( SW5OFF|SW4ON,  "4" )
	PORT_DIPSETTING( SW5ON |SW4ON,  "5" )

	PORT_START /* inputs high */
	PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x04, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x02, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_UNUSED )

	PORT_START /* inputs low */
	PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER1 )
	PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER1 )
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_START1 ) /* also hyperspace */
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_BUTTON3 | IPF_PLAYER1 )
	PORT_BIT ( 0x04, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER1 )
	PORT_BIT ( 0x02, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER1 )
	PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_START2 ) /* also nova */
	PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_BUTTON4 | IPF_PLAYER1 )

	PORT_START /* joystick X */
	PORT_BIT ( 0xff, IP_ACTIVE_LOW,  IPT_UNUSED )

	PORT_START /* joystick Y */
	PORT_BIT ( 0xff, IP_ACTIVE_LOW,  IPT_UNUSED )
INPUT_PORTS_END



static const char *solarq_sample_names[] =
{
	"*solarq",
    "bigexpl.wav",
	"smexpl.wav",
	"lthrust.wav",
	"slaser.wav",
	"pickup.wav",
	"nuke1.wav",
	"nuke2.wav",
	"hypersp.wav",
    "extra.wav",
    "phase.wav",
    "efire.wav",
    0	/* end of array */
};

static struct Samplesinterface solarq_samples_interface =
{
	8,	/* 8 channels */
	25,	/* volume */
	solarq_sample_names
};


void solarq_init_machine (void)
{
	ccpu_Config (1, CCPU_MEMSIZE_16K, CCPU_MONITOR_BILEV);
    cinemat_sound_handler = solarq_sound;
    cinemat_sound_init();
}

void init_solarq(void)
{
	cinemat_select_artwork (CCPU_MONITOR_BILEV, 1, 1, solarq_overlay);
}


CINEMA_MACHINE (solarq, 0, 0, 1024, 768)



/***************************************************************************

  Demon

***************************************************************************/

INPUT_PORTS_START( demon )
	PORT_START /* switches */
	PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 1 )
	PORT_DIPNAME( SW7, SW7OFF, "Game Mode" )
	PORT_DIPSETTING( SW7ON,  DEF_STR( Free_Play ) )
	PORT_DIPSETTING( SW7OFF, "Normal" )
	PORT_DIPNAME( SW6, SW6OFF, DEF_STR( Unknown ) )
	PORT_DIPSETTING( SW6OFF, DEF_STR( Off ) )
	PORT_DIPSETTING( SW6ON,  DEF_STR( On ) )
	PORT_DIPNAME( SW5, SW5OFF, DEF_STR( Unknown ) )
	PORT_DIPSETTING( SW5OFF, DEF_STR( Off ) )
	PORT_DIPSETTING( SW5ON,  DEF_STR( On ) )
	PORT_DIPNAME( SW4, SW4OFF, DEF_STR( Unknown ) )
	PORT_DIPSETTING( SW4OFF, DEF_STR( Off ) )
	PORT_DIPSETTING( SW4ON,  DEF_STR( On ) )
	PORT_DIPNAME( SW3, SW3OFF, DEF_STR( Unknown ) )
	PORT_DIPSETTING( SW3OFF, DEF_STR( Off ) )
	PORT_DIPSETTING( SW3ON,  DEF_STR( On ) )
	PORT_DIPNAME( SW2|SW1, SW2OFF|SW1OFF, DEF_STR( Coinage ) )
	PORT_DIPSETTING( SW2ON |SW1OFF, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING( SW2ON |SW1ON,  DEF_STR( 4C_3C ) )
	PORT_DIPSETTING( SW2OFF|SW1OFF, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING( SW2OFF|SW1ON,  DEF_STR( 2C_3C ) )

	PORT_START /* inputs high */
	PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNKNOWN ) /* also mapped to Button 3, player 2 */
	PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER2 )
	PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT ( 0x04, IP_ACTIVE_LOW,  IPT_BUTTON3 | IPF_PLAYER2 )
	PORT_BIT ( 0x02, IP_ACTIVE_LOW,  IPT_BUTTON3 | IPF_PLAYER1 )
	PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_TILT )

	PORT_START /* inputs low */
	PORT_DIPNAME( 0x80, 0x80, "Test Pattern" )
	PORT_DIPSETTING( 0x80, DEF_STR( Off ) )
	PORT_DIPSETTING( 0x00, DEF_STR( On ) )
	PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNKNOWN )
	PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER1 )
	PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER1 )
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER1 )
	PORT_BIT ( 0x04, IP_ACTIVE_LOW,  IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER1 )
	PORT_BIT ( 0x02, IP_ACTIVE_LOW,  IPT_START2 )
	PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_START1 )

	PORT_START /* joystick X */
	PORT_BIT ( 0xff, IP_ACTIVE_LOW,  IPT_UNUSED )

	PORT_START /* joystick Y */
	PORT_BIT ( 0xff, IP_ACTIVE_LOW,  IPT_UNUSED )
INPUT_PORTS_END



static const char *demon_sample_names[] =
{
    0	/* end of array */
};

static struct Samplesinterface demon_samples_interface =
{
	8,	/* 8 channels */
	25,	/* volume */
	demon_sample_names
};


void demon_init_machine (void)
{
	ccpu_Config (1, CCPU_MEMSIZE_16K, CCPU_MONITOR_BILEV);
    cinemat_sound_handler = 0;
}

void init_demon(void)
{
	cinemat_select_artwork (CCPU_MONITOR_BILEV, 0, 0, 0);
}

CINEMA_MACHINE (demon, 0, 0, 1024, 800)




/***************************************************************************

  War of the Worlds

***************************************************************************/

INPUT_PORTS_START( wotw )
	PORT_START /* switches */
	PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 1 )
	PORT_BITX( SW7, SW7OFF, IPT_DIPSWITCH_NAME | IPF_TOGGLE, DEF_STR( Service_Mode ), KEYCODE_F2, IP_JOY_NONE )
	PORT_DIPSETTING( SW7OFF, DEF_STR( Off ) )
	PORT_DIPSETTING( SW7ON,  DEF_STR( On ) )
	PORT_DIPNAME( SW6, SW6OFF, DEF_STR( Free_Play ) )
	PORT_DIPSETTING( SW6OFF, DEF_STR( Off ) )
	PORT_DIPSETTING( SW6ON,  DEF_STR( On ) )
	PORT_DIPNAME( SW4, SW4OFF, DEF_STR( Coinage ) )
	PORT_DIPSETTING( SW4OFF, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING( SW4ON,  DEF_STR( 2C_3C ) )
	PORT_DIPNAME( SW2, SW2OFF, "Ships" )
	PORT_DIPSETTING( SW2OFF, "3" )
	PORT_DIPSETTING( SW2ON,  "5" )
	PORT_BIT ( SW5|SW3|SW1, IP_ACTIVE_HIGH, IPT_UNKNOWN )

	PORT_START /* inputs high */
	PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON1 )
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x04, IP_ACTIVE_LOW,  IPT_BUTTON2 )
	PORT_BIT ( 0x02, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_JOYSTICK_RIGHT | IPF_2WAY )

	PORT_START /* inputs low */
	PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_JOYSTICK_LEFT | IPF_2WAY )
	PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x04, IP_ACTIVE_LOW,  IPT_START2 )
	PORT_BIT ( 0x02, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_START1 )

	PORT_START /* joystick X */
	PORT_BIT ( 0xff, IP_ACTIVE_LOW,  IPT_UNUSED )

	PORT_START /* joystick Y */
	PORT_BIT ( 0xff, IP_ACTIVE_LOW,  IPT_UNUSED )
INPUT_PORTS_END



static const char *wotw_sample_names[] =
{
    0	/* end of array */
};

static struct Samplesinterface wotw_samples_interface =
{
	8,	/* 8 channels */
	25,	/* volume */
	wotw_sample_names
};


void wotw_init_machine (void)
{
	ccpu_Config (1, CCPU_MEMSIZE_16K, CCPU_MONITOR_WOWCOL);
    cinemat_sound_handler = 0;
}

void init_wotw(void)
{
	cinemat_select_artwork (CCPU_MONITOR_WOWCOL, 0, 0, 0);
}


CINEMA_MACHINE (wotw, 0, 0, 1024, 768)




/***************************************************************************

  Boxing Bugs

***************************************************************************/

static int boxingb_readports (int offset)
{
	switch (offset)
	{
		case CCPU_PORT_IOSWITCHES:
			return readinputport (0);

		case CCPU_PORT_IOINPUTS:
            if (cinemat_outputs  & 0x80)
                return ((input_port_1_r(0) & 0x0f) << 12) + readinputport (2);
            else
                return ((input_port_1_r(0) & 0xf0) << 8) + readinputport (2);

		case CCPU_PORT_IOOUTPUTS:
			return cinemat_outputs;
	}

	return 0;
}

INPUT_PORTS_START( boxingb )
	PORT_START /* switches */
	PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 1 )
	PORT_BITX( SW7, SW7OFF, IPT_DIPSWITCH_NAME | IPF_TOGGLE, DEF_STR( Service_Mode ), KEYCODE_F2, IP_JOY_NONE )
	PORT_DIPSETTING( SW7OFF, DEF_STR( Off ) )
	PORT_DIPSETTING( SW7ON,  DEF_STR( On ) )
	PORT_DIPNAME( SW6, SW6OFF, DEF_STR( Free_Play ) )
	PORT_DIPSETTING( SW6OFF, DEF_STR( Off ) )
	PORT_DIPSETTING( SW6ON,  DEF_STR( On ) )
	PORT_DIPNAME( SW5, SW5OFF, "Attract Sound" )
	PORT_DIPSETTING( SW5OFF, DEF_STR( Off ) )
	PORT_DIPSETTING( SW5ON,  DEF_STR( On ) )
	PORT_DIPNAME( SW4, SW4ON, "Bonus" )
	PORT_DIPSETTING( SW4ON,  "at 30,000" )
	PORT_DIPSETTING( SW4OFF, "at 50,000" )
	PORT_DIPNAME( SW3, SW3ON, "Cannons" )
	PORT_DIPSETTING( SW3OFF, "3" )
	PORT_DIPSETTING( SW3ON,  "5" )
	PORT_DIPNAME( SW2|SW1, SW2OFF|SW1OFF, DEF_STR( Coinage ) )
	PORT_DIPSETTING( SW2ON |SW1OFF, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING( SW2ON |SW1ON,  DEF_STR( 4C_3C ) )
	PORT_DIPSETTING( SW2OFF|SW1OFF, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING( SW2OFF|SW1ON,  DEF_STR( 2C_3C ) )

	PORT_START /* inputs high */
	PORT_ANALOG( 0xff, 0x80, IPT_DIAL, 100, 5, 0x00, 0xff )
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x04, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x02, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_UNUSED )

	PORT_START /* inputs low */
	PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT ( 0x20, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER1 )
	PORT_BIT ( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER1 )
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_BUTTON3 | IPF_PLAYER1 )
	PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_START2 )
	PORT_BIT ( 0x04, IP_ACTIVE_LOW,  IPT_BUTTON3 | IPF_PLAYER2 )
	PORT_BIT ( 0x04, IP_ACTIVE_LOW,  IPT_START1 )
	PORT_BIT ( 0x02, IP_ACTIVE_LOW,  IPT_BUTTON2 | IPF_PLAYER2 )
	PORT_BIT ( 0x01, IP_ACTIVE_LOW,  IPT_BUTTON1 | IPF_PLAYER2 )

INPUT_PORTS_END



static const char *boxingb_sample_names[] =
{
    0	/* end of array */
};

static struct Samplesinterface boxingb_samples_interface =
{
	8,	/* 8 channels */
	25,	/* volume */
	boxingb_sample_names
};


void boxingb_init_machine (void)
{
	ccpu_Config (1, CCPU_MEMSIZE_32K, CCPU_MONITOR_WOWCOL);
    cinemat_sound_handler = 0;
}

void init_boxingb(void)
{
	cinemat_select_artwork (CCPU_MONITOR_WOWCOL, 0, 0, 0);
}


CINEMA_MACHINEX (boxingb, 0, 0, 1024, 768)








ROM_START( spacewar )
	ROM_REGION( 0x1000, REGION_CPU1 )	/* 4k for code */
	ROM_LOAD_GFX_EVEN( "spacewar.1l", 0x0000, 0x0800, 0xedf0fd53 )
	ROM_LOAD_GFX_ODD ( "spacewar.2r", 0x0000, 0x0800, 0x4f21328b )
ROM_END

ROM_START( barrier )
	ROM_REGION( 0x1000, REGION_CPU1 )	/* 4k for code */
	ROM_LOAD_GFX_EVEN( "barrier.t7", 0x0000, 0x0800, 0x7c3d68c8 )
	ROM_LOAD_GFX_ODD ( "barrier.p7", 0x0000, 0x0800, 0xaec142b5 )
ROM_END

ROM_START( starhawk )
	ROM_REGION( 0x1000, REGION_CPU1 )	/* 4k for code */
	ROM_LOAD_GFX_EVEN( "u7", 0x0000, 0x0800, 0x376e6c5c )
	ROM_LOAD_GFX_ODD ( "r7", 0x0000, 0x0800, 0xbb71144f )
ROM_END

ROM_START( starcas )
	ROM_REGION( 0x2000, REGION_CPU1 )	/* 8k for code */
	ROM_LOAD_GFX_EVEN( "starcas3.t7", 0x0000, 0x0800, 0xb5838b5d )
	ROM_LOAD_GFX_ODD ( "starcas3.p7", 0x0000, 0x0800, 0xf6bc2f4d )
	ROM_LOAD_GFX_EVEN( "starcas3.u7", 0x1000, 0x0800, 0x188cd97c )
	ROM_LOAD_GFX_ODD ( "starcas3.r7", 0x1000, 0x0800, 0xc367b69d )
ROM_END

ROM_START( starcas1 )
	ROM_REGION( 0x2000, REGION_CPU1 )	/* 8k for code */
	ROM_LOAD_GFX_EVEN( "starcast.t7", 0x0000, 0x0800, 0x65d0a225 )
	ROM_LOAD_GFX_ODD ( "starcast.p7", 0x0000, 0x0800, 0xd8f58d9a )
	ROM_LOAD_GFX_EVEN( "starcast.u7", 0x1000, 0x0800, 0xd4f35b82 )
	ROM_LOAD_GFX_ODD ( "starcast.r7", 0x1000, 0x0800, 0x9fd3de54 )
ROM_END

ROM_START( tailg )
	ROM_REGION( 0x2000, REGION_CPU1 )	/* 8k for code */
	ROM_LOAD_GFX_EVEN( "tgunner.t70", 0x0000, 0x0800, 0x21ec9a04 )
	ROM_LOAD_GFX_ODD ( "tgunner.p70", 0x0000, 0x0800, 0x8d7410b3 )
	ROM_LOAD_GFX_EVEN( "tgunner.t71", 0x1000, 0x0800, 0x2c954ab6 )
	ROM_LOAD_GFX_ODD ( "tgunner.p71", 0x1000, 0x0800, 0x8e2c8494 )
ROM_END

ROM_START( ripoff )
	ROM_REGION( 0x2000, REGION_CPU1 )	/* 8k for code */
	ROM_LOAD_GFX_EVEN( "ripoff.t7", 0x0000, 0x0800, 0x40c2c5b8 )
	ROM_LOAD_GFX_ODD ( "ripoff.p7", 0x0000, 0x0800, 0xa9208afb )
	ROM_LOAD_GFX_EVEN( "ripoff.u7", 0x1000, 0x0800, 0x29c13701 )
	ROM_LOAD_GFX_ODD ( "ripoff.r7", 0x1000, 0x0800, 0x150bd4c8 )
ROM_END

ROM_START( speedfrk )
	ROM_REGION( 0x2000, REGION_CPU1 )	/* 8k for code */
	ROM_LOAD_GFX_EVEN( "speedfrk.t7", 0x0000, 0x0800, 0x3552c03f )
	ROM_LOAD_GFX_ODD ( "speedfrk.p7", 0x0000, 0x0800, 0x4b90cdec )
	ROM_LOAD_GFX_EVEN( "speedfrk.u7", 0x1000, 0x0800, 0x616c7cf9 )
	ROM_LOAD_GFX_ODD ( "speedfrk.r7", 0x1000, 0x0800, 0xfbe90d63 )
ROM_END

ROM_START( sundance )
	ROM_REGION( 0x2000, REGION_CPU1 )	/* 8k for code */
	ROM_LOAD_GFX_EVEN( "sundance.t7", 0x0000, 0x0800, 0xd5b9cb19 )
	ROM_LOAD_GFX_ODD ( "sundance.p7", 0x0000, 0x0800, 0x445c4f20 )
	ROM_LOAD_GFX_EVEN( "sundance.u7", 0x1000, 0x0800, 0x67887d48 )
	ROM_LOAD_GFX_ODD ( "sundance.r7", 0x1000, 0x0800, 0x10b77ebd )
ROM_END

ROM_START( warrior )
	ROM_REGION( 0x2000, REGION_CPU1 )	/* 8k for code */
	ROM_LOAD_GFX_EVEN( "warrior.t7", 0x0000, 0x0800, 0xac3646f9 )
	ROM_LOAD_GFX_ODD ( "warrior.p7", 0x0000, 0x0800, 0x517d3021 )
	ROM_LOAD_GFX_EVEN( "warrior.u7", 0x1000, 0x0800, 0x2e39340f )
	ROM_LOAD_GFX_ODD ( "warrior.r7", 0x1000, 0x0800, 0x8e91b502 )
ROM_END

ROM_START( armora )
	ROM_REGION( 0x4000, REGION_CPU1 )	/* 16k for code */
	ROM_LOAD_GFX_EVEN( "ar414le.t6", 0x0000, 0x1000, 0xd7e71f84 )
	ROM_LOAD_GFX_ODD ( "ar414lo.p6", 0x0000, 0x1000, 0xdf1c2370 )
	ROM_LOAD_GFX_EVEN( "ar414ue.u6", 0x2000, 0x1000, 0xb0276118 )
	ROM_LOAD_GFX_ODD ( "ar414uo.r6", 0x2000, 0x1000, 0x229d779f )
ROM_END

ROM_START( solarq )
	ROM_REGION( 0x4000, REGION_CPU1 )	/* 16k for code */
	ROM_LOAD_GFX_EVEN( "solar.6t", 0x0000, 0x1000, 0x1f3c5333 )
	ROM_LOAD_GFX_ODD ( "solar.6p", 0x0000, 0x1000, 0xd6c16bcc )
	ROM_LOAD_GFX_EVEN( "solar.6u", 0x2000, 0x1000, 0xa5970e5c )
	ROM_LOAD_GFX_ODD ( "solar.6r", 0x2000, 0x1000, 0xb763fff2 )
ROM_END

ROM_START( demon )
	ROM_REGION( 0x4000, REGION_CPU1 )	/* 16k for code */
	ROM_LOAD_GFX_EVEN( "demon.7t", 0x0000, 0x1000, 0x866596c1 )
	ROM_LOAD_GFX_ODD ( "demon.7p", 0x0000, 0x1000, 0x1109e2f1 )
	ROM_LOAD_GFX_EVEN( "demon.7u", 0x2000, 0x1000, 0xd447a3c3 )
	ROM_LOAD_GFX_ODD ( "demon.7r", 0x2000, 0x1000, 0x64b515f0 )
ROM_END

ROM_START( wotw )
	ROM_REGION( 0x4000, REGION_CPU1 )	/* 16k for code */
	ROM_LOAD_GFX_EVEN( "wow_le.t7", 0x0000, 0x1000, 0xb16440f9 )
	ROM_LOAD_GFX_ODD ( "wow_lo.p7", 0x0000, 0x1000, 0xbfdf4a5a )
	ROM_LOAD_GFX_EVEN( "wow_ue.u7", 0x2000, 0x1000, 0x9b5cea48 )
	ROM_LOAD_GFX_ODD ( "wow_uo.r7", 0x2000, 0x1000, 0xc9d3c866 )
ROM_END

ROM_START( boxingb )
	ROM_REGION( 0x8000, REGION_CPU1 )	/* 32k for code */
	ROM_LOAD_GFX_EVEN( "u1a", 0x0000, 0x1000, 0xd3115b0f )
	ROM_LOAD_GFX_ODD ( "u1b", 0x0000, 0x1000, 0x3a44268d )
	ROM_LOAD_GFX_EVEN( "u2a", 0x2000, 0x1000, 0xc97a9cbb )
	ROM_LOAD_GFX_ODD ( "u2b", 0x2000, 0x1000, 0x98d34ff5 )
	ROM_LOAD_GFX_EVEN( "u3a", 0x4000, 0x1000, 0x5bb3269b )
	ROM_LOAD_GFX_ODD ( "u3b", 0x4000, 0x1000, 0x85bf83ad )
	ROM_LOAD_GFX_EVEN( "u4a", 0x6000, 0x1000, 0x25b51799 )
	ROM_LOAD_GFX_ODD ( "u4b", 0x6000, 0x1000, 0x7f41de6a )
ROM_END



GAME( 1978, spacewar, 0,       spacewar, spacewar, 0,        ROT0,   "Cinematronics", "Space Wars" )
GAME( 1979, barrier,  0,       barrier,  barrier,  barrier,  ROT270, "Vectorbeam", "Barrier" )
GAME( 1981, starhawk, 0,       starhawk, starhawk, starhawk, ROT0,   "Cinematronics", "Star Hawk" )
GAME( 1980, starcas,  0,       starcas,  starcas,  starcas,  ROT0,   "Cinematronics", "Star Castle (version 3)" )
GAME( 1980, starcas1, starcas, starcas,  starcas,  starcas,  ROT0,   "Cinematronics", "Star Castle (older)" )
GAME( 1979, tailg,    0,       tailg,    tailg,    tailg,    ROT0,   "Cinematronics", "Tailgunner" )
GAME( 1979, ripoff,   0,       ripoff,   ripoff,   ripoff,   ROT0,   "Cinematronics", "Rip Off" )
GAME( 19??, speedfrk, 0,       speedfrk, speedfrk, speedfrk, ROT0,   "Vectorbeam", "Speed Freak" )
GAMEX(1979, sundance, 0,       sundance, sundance, sundance, ROT270, "Cinematronics", "Sundance", GAME_NOT_WORKING )
GAME( 1978, warrior,  0,       warrior,  warrior,  warrior,  ROT0,   "Vectorbeam", "Warrior" )
GAME( 1980, armora,   0,       armora,   armora,   armora,   ROT0,   "Cinematronics", "Armor Attack" )
GAME( 1981, solarq,   0,       solarq,   solarq,   solarq,   ORIENTATION_FLIP_X, "Cinematronics", "Solar Quest" )
GAME( 1982, demon,    0,       demon,    demon,    demon,    ROT0,   "Rock-ola", "Demon" )
GAMEX(1981, wotw,     0,       wotw,     wotw,     wotw,     ROT0,   "Cinematronics", "War of the Worlds", GAME_IMPERFECT_COLORS )
GAMEX(1981, boxingb,  0,       boxingb,  boxingb,  boxingb,  ROT0,   "Cinematronics", "Boxing Bugs", GAME_IMPERFECT_COLORS )
