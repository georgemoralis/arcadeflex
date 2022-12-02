/****************************************************************************/
/*                                                                          */
/* 8080bw.c                                                                 */
/*                                                                          */
/*  Michael Strutts, Nicola Salmoria, Tormod Tjaberg, Mirko Buffoni         */
/*  Lee Taylor, Valerio Verrando, Marco Cassili, Zsolt Vasvari and others   */
/*                                                                          */
/*                                                                          */
/* Exidy                                                                    */
/* -----                                                                    */
/* Bandido                               (bandido)                          */
/*                                                                          */
/* Taito                                                                    */
/* -----                                                                    */
/* Space Invaders Part II (Taito)        (invadpt2)                         */
/* Space Laser  "GPI Intruder"           (spclaser)                         */
/* Galaxy Wars                           (galxwars)                         */
/* Lunar Rescue                          (lrescue)                          */
/* Galaxy Rescue (bootleg?)              (grescue)                          */
/* Lupin III                             (lupin3)                           */
/* Ozma Wars                             (ozmawars)                         */
/* Space Chaser                          (schaser)                          */
/*                                                                          */
/* Nichibutsu                                                               */
/* ----------                                                               */
/* Rolling Crash - Moon Base             (rollingc)                         */
/*                                                                          */
/* Nintendo                                                                 */
/* --------                                                                 */
/* Heli Fire                             (helifire)                         */
/* Space Fever  (color)                  (sfever)                           */
/* Space Fever  (B&W)                    (sfeverbw)                         */
/*                                                                          */
/* Universal                                                                */
/* ---------                                                                */
/* Cosmic Monsters                       (cosmicmo)                         */
/*                                                                          */
/* Zeltec                                                                   */
/* ------                                                                   */
/* Space Attack II                       (spaceatt)                         */
/* Invaders Revenge                      (invrvnge)                         */
/*                                                                          */
/* Super Earth Invasion                  (earthinv)                         */
/* Destination Earth                     (desterth)                         */
/* Space Phantoms                        (spaceph)                          */
/*                                                                          */
/*                                                                          */
/* Known problems:                                                          */
/* --------------                                                           */
/* The accelerator in 280 Zzzap could be handled better.                    */
/*																			*/
/*																			*/
/* To Do:																	*/
/* 																			*/
/* - Space Encounters: 'trench' video overlay                      			*/
/*																			*/
/* - Phantom II: little fluffy clouds you still see 						*/
/*			  them sometimes in the desert  								*/
/*																			*/
/*                                                                          */
/****************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"
#include "cpu/i8039/i8039.h"

/* in machine/8080bw.c */

void invaders_shift_amount_w(int offset,int data);
void invaders_shift_data_w(int offset,int data);
int  invaders_shift_data_r(int offset);
int  invaders_shift_data_rev_r(int offset);
int  invaders_shift_data_comp_r(int offset);
int  invaders_interrupt(void);

int  boothill_shift_data_r(int offset);
int  boothill_port_0_r(int offset);
int  boothill_port_1_r(int offset);
void boothill_sh_port3_w(int offset, int data);
void boothill_sh_port5_w(int offset, int data);

int  gray6bit_controller0_r(int offset);
int  gray6bit_controller1_r(int offset);
int  seawolf_port_0_r(int offset);
int  desertgu_port_1_r(int offset);
void desertgu_controller_select_w(int offset, int data);

void init_machine_bandido(void);
void bandido_sh_port4_w(int offset, int data);
void bandido_sh_port5_w(int offset, int data);
void bandido_sh_putp2(int offset, int data);
int  bandido_sh_getp1(int offset);
int  bandido_sh_getp2(int offset);
int  bandido_sh_gett0(int offset);
int  bandido_sh_gett1(int offset);

/* in video/8080bw.c */

int invaders_vh_start(void);
void invaders_vh_stop(void);

void init_8080bw(void);
void init_invaders(void);
void init_invadpt2(void);
void init_invdpt2m(void);
void init_invrvnge(void);
void init_invad2ct(void);
void init_schaser(void);
void init_rollingc(void);
void init_seawolf(void);
void init_blueshrk(void);
void init_desertgu(void);

void invaders_videoram_w(int offset,int data);
void schaser_colorram_w(int offset,int data);

void invad2ct_sh_port1_w(int offset,int data);
void invaders_sh_port3_w(int offset,int data);
void invaders_sh_port5_w(int offset,int data);
void invad2ct_sh_port7_w(int offset,int data);

void invaders_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh);

void invadpt2_vh_convert_color_prom(unsigned char *pallete, unsigned short *colortable,const unsigned char *color_prom);


static unsigned char invaders_palette[] =
{
	0x00,0x00,0x00, /* BLACK */
	0xff,0xff,0xff, /* WHITE */
};

static void init_palette(unsigned char *game_palette, unsigned short *game_colortable,const unsigned char *color_prom)
{
	memcpy(game_palette,invaders_palette,sizeof(invaders_palette));
}


/*******************************************************/
/*                                                     */
/* Midway "Space Invaders"                             */
/*                                                     */
/*******************************************************/

static struct MemoryReadAddress readmem[] =
{
	{ 0x0000, 0x1fff, MRA_ROM },
	{ 0x2000, 0x3fff, MRA_RAM },
	{ 0x4000, 0x5fff, MRA_ROM },
	{ -1 }  /* end of table */
};

static struct MemoryWriteAddress writemem[] =
{
	{ 0x0000, 0x1fff, MWA_ROM },
	{ 0x2000, 0x23ff, MWA_RAM },
	{ 0x2400, 0x3fff, invaders_videoram_w, &videoram, &videoram_size },
	{ 0x4000, 0x5fff, MWA_ROM },
	{ -1 }  /* end of table */
};

static struct IOReadPort readport[] =
{
	{ 0x00, 0x00, input_port_0_r },
	{ 0x01, 0x01, input_port_1_r },
	{ 0x02, 0x02, input_port_2_r },
	{ 0x03, 0x03, invaders_shift_data_r },
	{ -1 }  /* end of table */
};

static struct IOWritePort writeport[] =
{
	{ 0x02, 0x02, invaders_shift_amount_w },
	{ 0x03, 0x03, invaders_sh_port3_w },
	{ 0x04, 0x04, invaders_shift_data_w },
	{ 0x05, 0x05, invaders_sh_port5_w },
	{ -1 }  /* end of table */
};

INPUT_PORTS_START( invaders )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY )
	PORT_BIT( 0x80, IP_ACTIVE_LOW,  IPT_UNKNOWN )

	PORT_START      /* DSW0 */
	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x00, "3" )
	PORT_DIPSETTING(    0x01, "4" )
	PORT_DIPSETTING(    0x02, "5" )
	PORT_DIPSETTING(    0x03, "6" )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_TILT )
	PORT_DIPNAME( 0x08, 0x00, DEF_STR( Bonus_Life ) )
	PORT_DIPSETTING(    0x08, "1000" )
	PORT_DIPSETTING(    0x00, "1500" )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 )
	PORT_DIPNAME( 0x80, 0x00, "Coin Info" )
	PORT_DIPSETTING(    0x80, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )

	PORT_START		/* Dummy port for cocktail mode */
	PORT_DIPNAME( 0x01, 0x00, DEF_STR( Cabinet ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Upright ) )
	PORT_DIPSETTING(    0x01, DEF_STR( Cocktail ) )
INPUT_PORTS_END


static const char *invaders_sample_names[] =
{
	"*invaders",

/* these are used in invaders, invadpt2, invdpt2m, and others */
	"0.wav",	/* UFO/Saucer */
	"1.wav",	/* Shot/Missle */
	"2.wav",	/* Base Hit/Explosion */
	"3.wav",	/* Invader Hit */
	"4.wav",	/* Fleet move 1 */
	"5.wav",	/* Fleet move 2 */
	"6.wav",	/* Fleet move 3 */
	"7.wav",	/* Fleet move 4 */
	"8.wav",	/* UFO/Saucer Hit */
	"9.wav",	/* Bonus Base */

/* these are only use by invad2ct */
	"10.wav",	/* UFO/Saucer - Player 2 */
	"11.wav",	/* Shot/Missle - Player 2 */
	"12.wav",	/* Base Hit/Explosion - Player 2 */
	"13.wav",	/* Invader Hit - Player 2 */
	"14.wav",	/* Fleet move 1 - Player 2 */
	"15.wav",	/* Fleet move 2 - Player 2 */
	"16.wav",	/* Fleet move 3 - Player 2 */
	"17.wav",	/* Fleet move 4 - Player 2 */
	"18.wav",	/* UFO/Saucer Hit - Player 2 */
	0       /* end of array */
};

static const char *boothill_sample_names[] =
{
	"*boothill", /* in case we ever find any bootlegs hehehe */
	"addcoin.wav",
	"endgame.wav",
	"gunshot.wav",
	"killed.wav",
	0       /* end of array */
};

static struct Samplesinterface samples_interface =
{
	13,	/* 13 channels */
	25,	/* volume */
	invaders_sample_names
};

static struct Samplesinterface boothill_samples_interface =
{
	9,	/* 9 channels */
	25,	/* volume */
	boothill_sample_names
};


static struct MachineDriver machine_driver_invaders =
{
	/* basic machine hardware */
	{
		{
			CPU_8080,
			2000000,        /* 2 Mhz? */
			readmem,writemem,readport,writeport,
			invaders_interrupt,2    /* two interrupts per frame */
		}
	},
	60, DEFAULT_60HZ_VBLANK_DURATION,       /* frames per second, vblank duration */
	1,      /* single CPU, no need for interleaving */
	0,

	/* video hardware */
	32*8, 32*8, { 0*8, 32*8-1, 0*8, 28*8-1 },
	0,      /* no gfxdecodeinfo - bitmapped display */
	256, 0,		/* leave extra colors for the overlay */
	init_palette,

	VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
	0,
	invaders_vh_start,
	invaders_vh_stop,
	invaders_vh_screenrefresh,

	/* sound hardware */
	0, 0, 0, 0,
	{
		{
			SOUND_SAMPLES,
			&samples_interface
		}
	}
};


/*******************************************************/
/*                                                     */
/* Space Invaders TV Version (Taito)                   */
/*                                                     */
/*LT 24-12-1998                                        */
/*******************************************************/

/* same as Invaders with a test mode switch */

INPUT_PORTS_START( sitv )
	PORT_START		/* TEST MODE */
	PORT_SERVICE( 0x01, IP_ACTIVE_LOW )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY )
	PORT_BIT( 0x80, IP_ACTIVE_LOW,  IPT_UNKNOWN )

	PORT_START      /* DSW0 */
	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x00, "3" )
	PORT_DIPSETTING(    0x01, "4" )
	PORT_DIPSETTING(    0x02, "5" )
	PORT_DIPSETTING(    0x03, "6" )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_TILT )
	PORT_DIPNAME( 0x08, 0x00, DEF_STR( Bonus_Life ) )
	PORT_DIPSETTING(    0x08, "1000" )
	PORT_DIPSETTING(    0x00, "1500" )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 )
	PORT_DIPNAME( 0x80, 0x00, "Coin Info" )
	PORT_DIPSETTING(    0x80, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )

	PORT_START		/* Dummy port for cocktail mode */
	PORT_DIPNAME( 0x01, 0x00, DEF_STR( Cabinet ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Upright ) )
	PORT_DIPSETTING(    0x01, DEF_STR( Cocktail ) )
INPUT_PORTS_END


/*******************************************************/
/*                                                     */
/* Midway "Space Invaders Part II"                     */
/*                                                     */
/*******************************************************/

INPUT_PORTS_START( invadpt2 )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )

	PORT_START		/* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY )
	PORT_BIT( 0x80, IP_ACTIVE_LOW,  IPT_UNKNOWN )

	PORT_START      /* DSW0 */
	PORT_DIPNAME( 0x01, 0x00, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x00, "3" )
	PORT_DIPSETTING(    0x01, "4" )
	PORT_DIPNAME( 0x02, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x02, DEF_STR( On ) )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_TILT )
	PORT_DIPNAME( 0x08, 0x00, "Preset Mode" )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x08, DEF_STR( On ) )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 )
	PORT_DIPNAME( 0x80, 0x00, "Coin Info" )
	PORT_DIPSETTING(    0x80, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )

	PORT_START		/* Dummy port for cocktail mode */
	PORT_DIPNAME( 0x01, 0x00, DEF_STR( Cabinet ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Upright ) )
	PORT_DIPSETTING(    0x01, DEF_STR( Cocktail ) )
INPUT_PORTS_END


/* same as regular invaders, but with a color board added */

static struct MachineDriver machine_driver_invadpt2 =
{
	/* basic machine hardware */
	{
		{
			CPU_8080,
			2000000,        /* 2 Mhz? */
			readmem,writemem,readport,writeport,
			invaders_interrupt,2    /* two interrupts per frame */
		}
	},
	60, DEFAULT_60HZ_VBLANK_DURATION,       /* frames per second, vblank duration */
	1,      /* single CPU, no need for interleaving */
	0,

	/* video hardware */
	32*8, 32*8, { 0*8, 32*8-1, 0*8, 28*8-1 },
	0,      /* no gfxdecodeinfo - bitmapped display */
	8, 0,
	invadpt2_vh_convert_color_prom,

	VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY|VIDEO_MODIFIES_PALETTE,
	0,
	invaders_vh_start,
	invaders_vh_stop,
	invaders_vh_screenrefresh,

	/* sound hardware */
	0, 0, 0, 0,
	{
		{
			SOUND_SAMPLES,
			&samples_interface
		}
	}
};


/*******************************************************/
/*                                                     */
/* ?????? "Super Earth Invasion"                       */
/*                                                     */
/*******************************************************/

INPUT_PORTS_START( earthinv )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY )
	PORT_BIT( 0x80, IP_ACTIVE_LOW,  IPT_UNKNOWN )

	PORT_START      /* DSW0 */
	PORT_DIPNAME( 0x01, 0x01, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x00, "2" )
	PORT_DIPSETTING(    0x01, "3" )
	PORT_DIPNAME( 0x02, 0x02, "Pence Coinage" )
	PORT_DIPSETTING(    0x02, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_COIN2 ) /* Pence Coin */
	PORT_DIPNAME( 0x08, 0x08, DEF_STR( Unknown ) ) /* Not bonus */
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x08, DEF_STR( On ) )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 )
	PORT_DIPNAME( 0x80, 0x80, DEF_STR( Coinage ) )
	PORT_DIPSETTING(    0x00, "2C/1C 50p/3C (+ Bonus Life)" )
	PORT_DIPSETTING(    0x80, "1C/1C 50p/5C" )

	PORT_START		/* Dummy port for cocktail mode */
	PORT_DIPNAME( 0x01, 0x00, DEF_STR( Cabinet ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Upright ) )
	PORT_DIPSETTING(    0x01, DEF_STR( Cocktail ) )
INPUT_PORTS_END


/*******************************************************/
/*                                                     */
/* ?????? "Space Attack II"                            */
/*                                                     */
/*******************************************************/

INPUT_PORTS_START( spaceatt )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )

	PORT_START      /* DSW0 */
	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x00, "3" )
	PORT_DIPSETTING(    0x01, "4" )
	PORT_DIPSETTING(    0x02, "5" )
	PORT_DIPSETTING(    0x03, "6" )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_TILT )
	PORT_DIPNAME( 0x08, 0x00, DEF_STR( Bonus_Life ) )
	PORT_DIPSETTING(    0x08, "1000" )
	PORT_DIPSETTING(    0x00, "1500" )
	PORT_DIPNAME( 0x10, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x10, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x20, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x20, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x40, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x40, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x80, 0x00, DEF_STR( Coinage ) )
	PORT_DIPSETTING(    0x80, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 1C_1C ) )

	PORT_START		/* Dummy port for cocktail mode (not used) */
	PORT_BIT( 0xff, IP_ACTIVE_HIGH, IPT_UNKNOWN )
INPUT_PORTS_END


/*******************************************************/
/*                                                     */
/* Zenitone Microsec "Invaders Revenge"                */
/*                                                     */
/*******************************************************/

INPUT_PORTS_START( invrvnge )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY )
	PORT_BIT( 0x80, IP_ACTIVE_LOW,  IPT_UNKNOWN )

	PORT_START      /* DSW0 */
	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x00, "3" )
	PORT_DIPSETTING(    0x01, "4" )
	PORT_DIPSETTING(    0x02, "5" )
	PORT_DIPSETTING(    0x03, "6" )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_TILT )
	PORT_DIPNAME( 0x08, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x08, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 )
	PORT_DIPNAME( 0x80, 0x00, DEF_STR( Coinage ) )
	PORT_DIPSETTING(    0x80, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 1C_1C ) )

	PORT_START		/* Dummy port for cocktail mode */
	PORT_DIPNAME( 0x01, 0x00, DEF_STR( Cabinet ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Upright ) )
	PORT_DIPSETTING(    0x01, DEF_STR( Cocktail ) )
INPUT_PORTS_END


/*******************************************************/
/*                                                     */
/* Midway "Space Invaders II Cocktail"                 */
/*                                                     */
/*******************************************************/

static struct IOWritePort invad2ct_writeport[] =
{
	{ 0x01, 0x01, invad2ct_sh_port1_w },
	{ 0x02, 0x02, invaders_shift_amount_w },
	{ 0x03, 0x03, invaders_sh_port3_w },
	{ 0x04, 0x04, invaders_shift_data_w },
	{ 0x05, 0x05, invaders_sh_port5_w },
	{ 0x07, 0x07, invad2ct_sh_port7_w },
	{ -1 }  /* end of table */
};

INPUT_PORTS_START( invad2ct )
	PORT_START
	PORT_SERVICE(0x01, IP_ACTIVE_LOW) 			  /* dip 8 */
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN ) /* tied to pull-down */
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )  /* tied to pull-up */
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN ) /* tied to pull-down */
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN )  /* tied to pull-up */
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN )  /* tied to pull-up */
    PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )  /* labelled reset but tied to pull-up */
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )  /* tied to pull-up */

	PORT_START
	PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN ) /* tied to pull-down */
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT  | IPF_2WAY )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY )
	PORT_BIT( 0x80, IP_ACTIVE_LOW,  IPT_UNKNOWN )  /* tied to pull-up */

	PORT_START
	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Coinage ) ) /* dips 4 & 3 */
	PORT_DIPSETTING(    0x02, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0x03, DEF_STR( 2C_2C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING(    0x01, DEF_STR( 1C_2C ) )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )   /* tied to pull-up */
	PORT_DIPNAME( 0x08, 0x08, DEF_STR( Unknown ) ) /* dip 2 */
	PORT_DIPSETTING(    0x08, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY | IPF_COCKTAIL )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL )
	PORT_DIPNAME( 0x80, 0x80, DEF_STR( Bonus_Life ) ) /* dip 1 */
	PORT_DIPSETTING(    0x80, "1500" )
	PORT_DIPSETTING(    0x00, "2000" )

	PORT_START		/* Dummy port for cocktail mode (not used) */
	PORT_BIT( 0xff, IP_ACTIVE_HIGH, IPT_UNKNOWN )
INPUT_PORTS_END

static struct MachineDriver machine_driver_invad2ct =
{
	/* basic machine hardware */
	{
		{
			CPU_8080,
			1996800,        /* 19.968Mhz / 10 */
			readmem,writemem,readport,invad2ct_writeport,
			invaders_interrupt,2    /* two interrupts per frame */
		}
	},
	60, DEFAULT_60HZ_VBLANK_DURATION,       /* frames per second, vblank duration */
	1,      /* single CPU, no need for interleaving */
	0,

	/* video hardware */
	32*8, 32*8, { 0*8, 32*8-1, 0*8, 28*8-1 },
	0,      /* no gfxdecodeinfo - bitmapped display */
	256, 0,		/* leave extra colors for the overlay */
	init_palette,

	VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY|VIDEO_MODIFIES_PALETTE,
	0,
	invaders_vh_start,
	invaders_vh_stop,
	invaders_vh_screenrefresh,

	/* sound hardware */
	0, 0, 0, 0,
	{
		{
			SOUND_SAMPLES,
			&samples_interface
		}
	}
};


/*******************************************************/
/*                                                     */
/* Taito "Space Laser"                                 */
/*                                                     */
/*******************************************************/

INPUT_PORTS_START( spclaser )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )

	PORT_START      /* DSW0 */
	PORT_DIPNAME( 0x01, 0x00, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x00, "3" )
	PORT_DIPSETTING(    0x01, "4" )
	PORT_DIPNAME( 0x02, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x02, DEF_STR( On ) )
	PORT_DIPNAME( 0x04, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x04, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x08, 0x00, "Preset Mode" )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x08, DEF_STR( On ) )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 )
	PORT_DIPNAME( 0x80, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x80, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )

	PORT_START		/* Dummy port for cocktail mode (not used) */
	PORT_BIT( 0xff, IP_ACTIVE_HIGH, IPT_UNKNOWN )
INPUT_PORTS_END


/*******************************************************/
/*                                                     */
/* Space War Part 3                                    */
/*                                                     */
/* Added 21/11/1999 By LT                              */
/* Thanks to Peter Fyfe for machine info               */
/*******************************************************/

INPUT_PORTS_START( spacewr3 )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY )
	PORT_BIT( 0x80, IP_ACTIVE_LOW,  IPT_UNKNOWN )

	PORT_START      /* DSW0 */
	PORT_DIPNAME( 0x01, 0x00, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x00, "3" )
	PORT_DIPSETTING(    0x01, "4" )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_DIPNAME( 0x04, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x04, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x08, 0x00, DEF_STR( Bonus_Life ) )
	PORT_DIPSETTING(    0x08, "1000" )
	PORT_DIPSETTING(    0x00, "1500" )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 )
	PORT_DIPNAME( 0x80, 0x00, "Coin Info" )
	PORT_DIPSETTING(    0x80, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )

	PORT_START		/* Dummy port for cocktail mode */
	PORT_DIPNAME( 0x01, 0x00, DEF_STR( Cabinet ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Upright ) )
	PORT_DIPSETTING(    0x01, DEF_STR( Cocktail ) )
INPUT_PORTS_END


/*******************************************************/
/*                                                     */
/* Taito "Galaxy Wars"                                 */
/*                                                     */
/*******************************************************/

INPUT_PORTS_START( galxwars )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY )
	PORT_BIT( 0x80, IP_ACTIVE_LOW,  IPT_UNKNOWN )

	PORT_START      /* DSW0 */
	PORT_DIPNAME( 0x03, 0x01, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x00, "2" )
	PORT_DIPSETTING(    0x01, "3" )
	PORT_DIPSETTING(    0x02, "4" )
	PORT_DIPSETTING(    0x03, "5" )
	PORT_DIPNAME( 0x04, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x04, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x08, 0x00, DEF_STR( Bonus_Life ) )
	PORT_DIPSETTING(    0x00, "3000" )
	PORT_DIPSETTING(    0x08, "5000" )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_PLAYER2 )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 )
	PORT_DIPNAME( 0x80, 0x00, DEF_STR( Coinage ) )
	PORT_DIPSETTING(    0x80, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 1C_1C ) )

	PORT_START		/* Dummy port for cocktail mode */
	PORT_DIPNAME( 0x01, 0x00, DEF_STR( Cabinet ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Upright ) )
	PORT_DIPSETTING(    0x01, DEF_STR( Cocktail ) )
INPUT_PORTS_END


/*******************************************************/
/*                                                     */
/* Taito "Lunar Rescue"                                */
/*                                                     */
/*******************************************************/

INPUT_PORTS_START( lrescue )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY )
	PORT_BIT( 0x80, IP_ACTIVE_LOW,  IPT_UNKNOWN )

	PORT_START      /* DSW0 */
	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x00, "3" )
	PORT_DIPSETTING(    0x01, "4" )
	PORT_DIPSETTING(    0x02, "5" )
	PORT_DIPSETTING(    0x03, "6" )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_TILT )
	PORT_DIPNAME( 0x08, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x08, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 )
	PORT_DIPNAME( 0x80, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x80, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )

	PORT_START		/* Dummy port for cocktail mode */
	PORT_DIPNAME( 0x01, 0x00, DEF_STR( Cabinet ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Upright ) )
	PORT_DIPSETTING(    0x01, DEF_STR( Cocktail ) )
INPUT_PORTS_END


/*******************************************************/
/*                                                     */
/* Universal "Cosmic Monsters"                         */
/*                                                     */
/*******************************************************/

INPUT_PORTS_START( cosmicmo )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY )
	PORT_BIT( 0x80, IP_ACTIVE_LOW,  IPT_UNKNOWN )

	PORT_START      /* DSW0 */
	PORT_DIPNAME( 0x03, 0x01, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x00, "2" )
	PORT_DIPSETTING(    0x01, "3" )
	PORT_DIPSETTING(    0x02, "4" )
	PORT_DIPSETTING(    0x03, "5" )
	PORT_DIPNAME( 0x04, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x04, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x08, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x08, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 )
	PORT_DIPNAME( 0x80, 0x00, DEF_STR( Coinage ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING(    0x80, DEF_STR( 1C_2C ) )

	PORT_START		/* Dummy port for cocktail mode */
	PORT_DIPNAME( 0x01, 0x00, DEF_STR( Cabinet ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Upright ) )
	PORT_DIPSETTING(    0x01, DEF_STR( Cocktail ) )
INPUT_PORTS_END



/*******************************************************/
/*                                                     */
/* Nichibutsu "Rolling Crash"                          */
/*                                                     */
/*******************************************************/

static struct MemoryReadAddress rollingc_readmem[] =
{
	{ 0x0000, 0x1fff, MRA_ROM },
	{ 0x2000, 0x3fff, MRA_RAM },
//  { 0x2000, 0x2002, MRA_RAM },
//  { 0x2003, 0x2003, hack },
	{ 0x4000, 0x5fff, MRA_ROM },
	{ 0xa400, 0xbfff, MRA_RAM },
	{ 0xe400, 0xffff, MRA_RAM },
	{ -1 }  /* end of table */
};

static struct MemoryWriteAddress rollingc_writemem[] =
{
	{ 0x0000, 0x1fff, MWA_ROM },
	{ 0x2000, 0x23ff, MWA_RAM },
	{ 0x2400, 0x3fff, invaders_videoram_w, &videoram, &videoram_size },
	{ 0x4000, 0x5fff, MWA_ROM },
	{ 0xa400, 0xbfff, schaser_colorram_w, &colorram },
	{ 0xe400, 0xffff, MWA_RAM },
	{ -1 }  /* end of table */
};

INPUT_PORTS_START( rollingc )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT ) /* Game Select */
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT ) /* Game Select */
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )

	PORT_START      /* DSW1 */
	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x00, "3" )
	PORT_DIPSETTING(    0x01, "4" )
	PORT_DIPSETTING(    0x02, "5" )
	PORT_DIPSETTING(    0x03, "6" )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_TILT )
	PORT_DIPNAME( 0x08, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x08, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 )
	PORT_DIPNAME( 0x80, 0x00, "Coin Info" )
	PORT_DIPSETTING(    0x80, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )

	PORT_START		/* Dummy port for cocktail mode */
	PORT_DIPNAME( 0x01, 0x00, DEF_STR( Cabinet ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Upright ) )
	PORT_DIPSETTING(    0x01, DEF_STR( Cocktail ) )
INPUT_PORTS_END


static struct MachineDriver machine_driver_rollingc =
{
	/* basic machine hardware */
	{
		{
			CPU_8080,
			2000000,        /* 2 Mhz? */
			rollingc_readmem,rollingc_writemem,readport,writeport,
			invaders_interrupt,2    /* two interrupts per frame */
		}
	},
	60, DEFAULT_60HZ_VBLANK_DURATION,       /* frames per second, vblank duration */
	1,      /* single CPU, no need for interleaving */
	0,

	/* video hardware */
	32*8, 32*8, { 0*8, 32*8-1, 0*8, 28*8-1 },
	0,      /* no gfxdecodeinfo - bitmapped display */
	8, 0,
	invadpt2_vh_convert_color_prom,

	VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
	0,
	invaders_vh_start,
	invaders_vh_stop,
	invaders_vh_screenrefresh,

	/* sound hardware */
	0, 0, 0, 0,
	{
		{
			SOUND_SAMPLES,
			&samples_interface
		}
	}
};



/*******************************************************/
/*                                                     */
/* Exidy "Bandido"                                     */
/*                                                     */
/* Nintendo game which was sold outside of the US as   */
/* "Sheriff"                                           */
/*                                                     */
/*******************************************************/

static struct MemoryReadAddress bandido_readmem[] =
{
	{ 0x0000, 0x27ff, MRA_ROM },
	{ 0x4200, 0x7fff, MRA_RAM },
	{ -1 }  /* end of table */
};

static struct MemoryWriteAddress bandido_writemem[] =
{
	{ 0x0000, 0x27ff, MWA_ROM },
	{ 0x4200, 0x5dff, invaders_videoram_w, &videoram, &videoram_size },
	{ 0x5e00, 0x7fff, MWA_RAM },
	{ -1 }  /* end of table */
};

static struct IOReadPort bandido_readport[] =
{
	{ 0x00, 0x00, input_port_0_r },
	{ 0x01, 0x01, input_port_1_r },
	{ 0x02, 0x02, input_port_2_r },
	{ 0x03, 0x03, invaders_shift_data_r },
	{ 0x04, 0x04, input_port_3_r },
	{ -1 }  /* end of table */
};

static struct IOWritePort bandido_writeport[] =
{
	{ 0x02, 0x02, invaders_shift_amount_w },
	{ 0x03, 0x03, invaders_shift_data_w },
	{ 0x04, 0x04, bandido_sh_port4_w },
	{ 0x05, 0x05, bandido_sh_port5_w },
	{ -1 }  /* end of table */
};

static struct MemoryReadAddress bandido_sound_readmem[] =
{
	{ 0x0000, 0x03ff, MRA_ROM },
	{ -1 }	/* end of table */
};
static struct MemoryWriteAddress bandido_sound_writemem[] =
{
	{ 0x0000, 0x03ff, MWA_ROM },
	{ -1 }	/* end of table */
};

static struct IOReadPort bandido_sound_readport[] =
{
	{ I8039_p1, I8039_p1, bandido_sh_getp1 },
	{ I8039_p2, I8039_p2, bandido_sh_getp2 },
	{ I8039_t0, I8039_t0, bandido_sh_gett0 },
	{ I8039_t1, I8039_t1, bandido_sh_gett1 },
	{ -1 }	/* end of table */
};

static struct IOWritePort bandido_sound_writeport[] =
{
	{ I8039_p2, I8039_p2, bandido_sh_putp2 },
	{ -1 }	/* end of table */
};

/* All of the controls/dips for cocktail mode are as per the schematic */
/* BUT a coffee table version was never manufactured and support was   */
/* probably never completed.                                           */
/* e.g. cocktail players button will give 6 credits!                   */

INPUT_PORTS_START( bandido )
	PORT_START      /* 00 Main Controls */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_RIGHT  | IPF_8WAY )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_LEFT   | IPF_8WAY )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_UP     | IPF_8WAY )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_DOWN   | IPF_8WAY )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_RIGHT | IPF_8WAY )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_LEFT  | IPF_8WAY )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_UP    | IPF_8WAY )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_DOWN  | IPF_8WAY )

	PORT_START      /* 01 Player 2 Controls */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_RIGHT  | IPF_8WAY | IPF_COCKTAIL )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_LEFT   | IPF_8WAY | IPF_COCKTAIL )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_UP     | IPF_8WAY | IPF_COCKTAIL )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_DOWN   | IPF_8WAY | IPF_COCKTAIL )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_RIGHT | IPF_8WAY | IPF_COCKTAIL )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_LEFT  | IPF_8WAY | IPF_COCKTAIL )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_UP    | IPF_8WAY | IPF_COCKTAIL )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_DOWN  | IPF_8WAY | IPF_COCKTAIL )

	PORT_START      /* 02 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_START2 )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN )           /* Marked for   */
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN )           /* Expansion    */
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )           /* on Schematic */
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_COIN1 )

	PORT_START      /* 04 */
	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x00, "3" )
	PORT_DIPSETTING(    0x01, "4" )
	PORT_DIPSETTING(    0x02, "5" )
	PORT_DIPSETTING(    0x03, "6" )
	PORT_DIPNAME( 0x04, 0x00, DEF_STR( Coinage ) )
	PORT_DIPSETTING(    0x04, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 1C_1C ) )
	PORT_DIPNAME( 0x08, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x08, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x10, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x10, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x20, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x20, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x40, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x40, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x80, 0x80, DEF_STR( Cabinet ) )
	PORT_DIPSETTING(    0x80, DEF_STR( Upright ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Cocktail ) )
INPUT_PORTS_END

static struct DACinterface bandido_dac_interface =
{
	1,
	{ 25 }
};

static struct SN76477interface bandido_sn76477_interface =
{
	1,	/* 1 chip */
	{ 25 },  /* mixing level   pin description		 */
	{ RES_K( 36)   },		/*	4  noise_res		 */
	{ RES_K(100)   },		/*	5  filter_res		 */
	{ CAP_U(0.001) },		/*	6  filter_cap		 */
	{ RES_K(620)   },		/*	7  decay_res		 */
	{ CAP_U(1.0)   },		/*	8  attack_decay_cap  */
	{ RES_K(20)    },		/* 10  attack_res		 */
	{ RES_K(150)   },		/* 11  amplitude_res	 */
	{ RES_K(47)    },		/* 12  feedback_res 	 */
	{ 0            },		/* 16  vco_voltage		 */
	{ CAP_U(0.001) },		/* 17  vco_cap			 */
	{ RES_M(1.5)   },		/* 18  vco_res			 */
	{ 0.0		   },		/* 19  pitch_voltage	 */
	{ RES_M(1.5)   },		/* 20  slf_res			 */
	{ CAP_U(0.047) },		/* 21  slf_cap			 */
	{ CAP_U(0.047) },		/* 23  oneshot_cap		 */
	{ RES_K(560)   }		/* 24  oneshot_res		 */
};


static struct MachineDriver machine_driver_bandido =
{
	/* basic machine hardware */
	{
		{
			CPU_8080,
			20160000/8,        /* 2.52 MHz */
			bandido_readmem,bandido_writemem,bandido_readport,bandido_writeport,
			invaders_interrupt,2    /* two interrupts per frame */
		},
		{
			CPU_I8035 | CPU_AUDIO_CPU,
			6000000/15,	/* ??? */
			bandido_sound_readmem,bandido_sound_writemem,
			bandido_sound_readport,bandido_sound_writeport,
			ignore_interrupt,1
		}
	},
	60, DEFAULT_60HZ_VBLANK_DURATION,       /* frames per second, vblank duration */
	1,      /* single CPU, no need for interleaving */
	init_machine_bandido,

	/* video hardware */
	32*8, 32*8, { 0*8, 32*8-1, 0*8, 28*8-1 },
	0,      /* no gfxdecodeinfo - bitmapped display */
	256, 0,		/* leave extra colors for the overlay */
	init_palette,

	VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
	0,
	invaders_vh_start,
	invaders_vh_stop,
	invaders_vh_screenrefresh,

	/* sound hardware */
	0, 0, 0, 0,
	{
		{
			SOUND_DAC,
			&bandido_dac_interface
		},
		{
			SOUND_SN76477,
			&bandido_sn76477_interface
		}
	}
};


/*******************************************************/
/*                                                     */
/* Midway "Space Encounters"                           */
/*                                                     */
/*                                                     */
/*******************************************************/

static struct IOWritePort spcenctr_writeport[] =
{
	{ 0x01, 0x01, invaders_shift_amount_w },
	{ 0x02, 0x02, invaders_shift_data_w },
	{ -1 }  /* end of table */
};

static struct IOReadPort spcenctr_readport[] =
{
	{ 0x00, 0x00, gray6bit_controller0_r }, /* These 2 ports use Gray's binary encoding */
	{ 0x01, 0x01, gray6bit_controller1_r },
	{ 0x02, 0x02, input_port_2_r },
	{ -1 }  /* end of table */
};

INPUT_PORTS_START( spcenctr )
	PORT_START      /* IN0 */
	PORT_ANALOG( 0x3f, 0x1f, IPT_AD_STICK_X | IPF_REVERSE, 25, 10, 0, 0x3f) /* 6 bit horiz encoder - Gray's binary */
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 )    /* fire */
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 )

	PORT_START      /* IN1 */
	PORT_ANALOG( 0x3f, 0x1f, IPT_AD_STICK_Y, 25, 10, 0, 0x3f) /* 6 bit vert encoder - Gray's binary */
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN2 )

	PORT_START      /* IN2 Dips & Coins */
	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Bonus_Life ) )
	PORT_DIPSETTING(    0x00, "2000 4000 8000" )
	PORT_DIPSETTING(    0x01, "3000 6000 12000" )
	PORT_DIPSETTING(    0x02, "4000 8000 16000" )
	PORT_DIPSETTING(    0x03, "5000 10000 20000" )
	PORT_DIPNAME( 0x0c, 0x00, DEF_STR( Coinage ) )
	PORT_DIPSETTING(    0x04, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING(    0x0c, DEF_STR( 2C_3C ) )
	PORT_DIPSETTING(    0x08, DEF_STR( 1C_3C ) )
	PORT_DIPNAME( 0x30, 0x00, "Bonus/Test Mode" )
	PORT_DIPSETTING(    0x00, "Bonus On" )
	PORT_DIPSETTING(    0x30, "Bonus Off" )
	PORT_DIPSETTING(    0x20, "Cross Hatch" )
	PORT_DIPSETTING(    0x10, "Test Mode" )
	PORT_DIPNAME( 0xc0, 0x00, "Time" )
	PORT_DIPSETTING(    0x00, "45" )
	PORT_DIPSETTING(    0x40, "60" )
	PORT_DIPSETTING(    0x80, "75" )
	PORT_DIPSETTING(    0xc0, "90" )
INPUT_PORTS_END

static struct MachineDriver machine_driver_spcenctr =
{
	/* basic machine hardware */
	{
		{
			CPU_8080,
			2000000,        /* 2 Mhz? */
			readmem,writemem,spcenctr_readport,spcenctr_writeport,
			invaders_interrupt,2    /* two interrupts per frame */
		}
	},
	60, DEFAULT_60HZ_VBLANK_DURATION,
	1,      /* single CPU, no need for interleaving */
	0,

	/* video hardware */
	32*8, 32*8, { 0*8, 32*8-1, 0*8, 28*8-1 },
	0,      /* no gfxdecodeinfo - bitmapped display */
	256, 0,		/* leave extra colors for the overlay */
	init_palette,

	VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
	0,
	invaders_vh_start,
	invaders_vh_stop,
    invaders_vh_screenrefresh,

	/* sound hardware */
	0, 0, 0, 0,
	{
		{
			SOUND_SAMPLES,
			&samples_interface
		}
	}
};


/*******************************************************/
/*                                                     */
/* Midway "Boot Hill"                                  */
/*                                                     */
/*******************************************************/

static struct IOReadPort boothill_readport[] =
{
	{ 0x00, 0x00, boothill_port_0_r },
	{ 0x01, 0x01, boothill_port_1_r },
	{ 0x02, 0x02, input_port_2_r },
	{ 0x03, 0x03, boothill_shift_data_r },
	{ -1 }  /* end of table */
};

static struct IOWritePort boothill_writeport[] =
{
	{ 0x01, 0x01, invaders_shift_amount_w },
	{ 0x02, 0x02, invaders_shift_data_w },
	{ 0x03, 0x03, boothill_sh_port3_w },
	{ 0x05, 0x05, boothill_sh_port5_w },
	{ -1 }  /* end of table */
};

INPUT_PORTS_START( boothill )
    /* Gun position uses bits 4-6, handled using fake paddles */
	PORT_START      /* IN0 - Player 2 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 )        /* Move Man */
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 ) /* Fire */

	PORT_START      /* IN1 - Player 1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY ) /* Move Man */
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 ) /* Fire */

	PORT_START      /* IN2 Dips & Coins */
	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Coinage ) )
	PORT_DIPSETTING(    0x02, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING(    0x01, DEF_STR( 1C_2C ) )
//	PORT_DIPSETTING(    0x03, DEF_STR( 1C_2C ) )
	PORT_DIPNAME( 0x0c, 0x00, "Time" )
	PORT_DIPSETTING(    0x00, "64" )
	PORT_DIPSETTING(    0x04, "74" )
	PORT_DIPSETTING(    0x08, "84" )
	PORT_DIPSETTING(    0x0C, "94" )
	PORT_SERVICE( 0x10, IP_ACTIVE_HIGH )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START1 )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 )

	PORT_START                                                                                          /* Player 2 Gun */
	PORT_ANALOGX( 0xff, 0x00, IPT_PADDLE | IPF_PLAYER2, 50, 10, 1, 255, IP_KEY_NONE, IP_KEY_NONE, IP_JOY_NONE, IP_JOY_NONE )

	PORT_START                                                                                          /* Player 1 Gun */
	PORT_ANALOGX( 0xff, 0x00, IPT_PADDLE, 50, 10, 1, 255, KEYCODE_Z, KEYCODE_A, IP_JOY_NONE, IP_JOY_NONE )
INPUT_PORTS_END

static struct MachineDriver machine_driver_gmissile =
{
	/* basic machine hardware */
	{
		{
			CPU_8080,
			2000000,        /* 2 Mhz? */
			readmem,writemem,boothill_readport,spcenctr_writeport,
			invaders_interrupt,2    /* two interrupts per frame */
		}
	},
	60, DEFAULT_60HZ_VBLANK_DURATION,
	1,      /* single CPU, no need for interleaving */
	0,

	/* video hardware */
	32*8, 32*8, { 0*8, 32*8-1, 0*8, 28*8-1 },
	0,      /* no gfxdecodeinfo - bitmapped display */
	256, 0,		/* leave extra colors for the overlay */
	init_palette,

	VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
	0,
	invaders_vh_start,
	invaders_vh_stop,
	invaders_vh_screenrefresh,

	/* sound hardware */
	0, 0, 0, 0,
	{
		{
			SOUND_SAMPLES,
			&samples_interface
		}
	}
};


/* same as gmissile with a different samples_interface */

static struct MachineDriver machine_driver_boothill =
{
	/* basic machine hardware */
	{
		{
			CPU_8080,
			2000000,        /* 2 Mhz? */
			readmem,writemem,boothill_readport,boothill_writeport,
			invaders_interrupt,2    /* two interrupts per frame */
		}
	},
	60, DEFAULT_60HZ_VBLANK_DURATION,
	1,      /* single CPU, no need for interleaving */
	0,

	/* video hardware */
	32*8, 32*8, { 0*8, 32*8-1, 0*8, 28*8-1 },
	0,      /* no gfxdecodeinfo - bitmapped display */
	256, 0,		/* leave extra colors for the overlay */
	init_palette,

	VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
	0,
	invaders_vh_start,
	invaders_vh_stop,
	invaders_vh_screenrefresh,

	/* sound hardware */
	0, 0, 0, 0,
	{
		{
			SOUND_SAMPLES,
			&boothill_samples_interface
		}
	}
};


/*******************************************************/
/*                                                     */
/* Taito "Space Chaser"                                */
/*                                                     */
/*******************************************************/

static struct MemoryReadAddress schaser_readmem[] =
{
	{ 0x0000, 0x1fff, MRA_ROM },
	{ 0x2000, 0x3fff, MRA_RAM },
	{ 0x4000, 0x5fff, MRA_ROM },
	{ 0xc400, 0xdfff, MRA_RAM },
	{ -1 }  /* end of table */
};

static struct MemoryWriteAddress schaser_writemem[] =
{
	{ 0x0000, 0x1fff, MWA_ROM },
	{ 0x2000, 0x23ff, MWA_RAM },
	{ 0x2400, 0x3fff, invaders_videoram_w, &videoram, &videoram_size },
	{ 0x4000, 0x5fff, MWA_ROM },
	{ 0xc400, 0xdfff, schaser_colorram_w, &colorram },
	{ -1 }  /* end of table */
};

INPUT_PORTS_START( schaser )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_4WAY )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_4WAY )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_4WAY )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_START2 )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_COIN1 )

	PORT_START      /* IN1 */
	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x00, "3" )
	PORT_DIPSETTING(    0x01, "4" )
	PORT_DIPSETTING(    0x02, "5" )
	PORT_DIPSETTING(    0x03, "6" )
	PORT_DIPNAME( 0x04, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x04, DEF_STR( On ) )
	PORT_DIPNAME( 0x08, 0x00, DEF_STR( Difficulty ) )
	PORT_DIPSETTING(    0x00, "Easy" )
	PORT_DIPSETTING(    0x08, "Hard" )
	PORT_DIPNAME( 0x10, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x10, DEF_STR( On ) )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN ) /* Tilt  */
	PORT_DIPNAME( 0x40, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x40, DEF_STR( On ) )
	PORT_DIPNAME( 0x80, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x80, DEF_STR( On ) )
INPUT_PORTS_END


static struct MachineDriver machine_driver_schaser =
{
	/* basic machine hardware */
	{
		{
			CPU_8080,
			2000000,        /* 2 Mhz? */
			schaser_readmem,schaser_writemem,readport,writeport,
			invaders_interrupt,2    /* two interrupts per frame */
		}
	},
	60, DEFAULT_60HZ_VBLANK_DURATION,       /* frames per second, vblank duration */
	1,      /* single CPU, no need for interleaving */
	0,

	/* video hardware */
	32*8, 32*8, { 0*8, 32*8-1, 0*8, 28*8-1 },
	0,      /* no gfxdecodeinfo - bitmapped display */
	8, 0,
	invadpt2_vh_convert_color_prom,

	VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
	0,
	invaders_vh_start,
	invaders_vh_stop,
	invaders_vh_screenrefresh,

	/* sound hardware */
	0, 0, 0, 0,
	{
		{
			SOUND_SAMPLES,
			&samples_interface
		}
	}
};


/*******************************************************/
/*                                                     */
/* Midway "Clowns"                                     */
/*                                                     */
/*******************************************************/

/*
 * Clowns (EPROM version)
 */
INPUT_PORTS_START( clowns )
	PORT_START      /* IN0 */
	PORT_ANALOG( 0xff, 0x7f, IPT_PADDLE, 100, 10, 0x01, 0xfe)

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START1 )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )

	PORT_START      /* IN2 Dips & Coins */
	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Coinage ) )
	PORT_DIPSETTING(    0x03, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0x02, DEF_STR( 2C_2C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING(    0x01, DEF_STR( 1C_2C ) )
	PORT_DIPNAME( 0x04, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x04, DEF_STR( On ) )
	PORT_DIPNAME( 0x08, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x08, DEF_STR( On ) )
	PORT_DIPNAME( 0x10, 0x00, "Balloon Resets" )
	PORT_DIPSETTING(    0x00, "Each row" )
	PORT_DIPSETTING(    0x10, "All rows" )
	PORT_DIPNAME( 0x20, 0x00, DEF_STR( Bonus_Life ) )
	PORT_DIPSETTING(    0x00, "3000" )
	PORT_DIPSETTING(    0x20, "4000" )
	PORT_DIPNAME( 0x40, 0x00, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x00, "3" )
	PORT_DIPSETTING(    0x40, "4" )
	PORT_SERVICE( 0x80, IP_ACTIVE_HIGH )
INPUT_PORTS_END

static struct MachineDriver machine_driver_clowns =
{
	/* basic machine hardware */
	{
		{
			CPU_8080,
			2000000,        /* 2 Mhz? */
			readmem,writemem,readport,spcenctr_writeport,
			invaders_interrupt,2    /* two interrupts per frame */
		}
	},
	60, DEFAULT_60HZ_VBLANK_DURATION,
	1,      /* single CPU, no need for interleaving */
	0,

	/* video hardware */
	32*8, 32*8, { 0*8, 32*8-1, 0*8, 28*8-1 },
	0,      /* no gfxdecodeinfo - bitmapped display */
	256, 0,		/* leave extra colors for the overlay */
	init_palette,

	VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
	0,
	invaders_vh_start,
	invaders_vh_stop,
    invaders_vh_screenrefresh,

	/* sound hardware */
	0, 0, 0, 0,
	{
		{
			SOUND_SAMPLES,
			&samples_interface
		}
	}
};


/*******************************************************/
/*                                                     */
/* Midway "Guided Missile"                             */
/*                                                     */
/*******************************************************/

INPUT_PORTS_START( gmissile )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_2WAY )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START2 )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON2 )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN1 )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START1 )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 )

	PORT_START      /* IN2 Dips & Coins */
	PORT_DIPNAME( 0x03, 0x03, DEF_STR( Coinage ) )
	PORT_DIPSETTING(    0x01, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0x03, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 2C_3C ) )
	PORT_DIPSETTING(    0x02, DEF_STR( 1C_2C ) )
	PORT_DIPNAME( 0x0c, 0x0c, "Time" )
	PORT_DIPSETTING(    0x00, "60" )
	PORT_DIPSETTING(    0x08, "70" )
	PORT_DIPSETTING(    0x04, "80" )
	PORT_DIPSETTING(    0x0c, "90" )
	PORT_DIPNAME( 0x30, 0x00, "Extra Play" )
	PORT_DIPSETTING(    0x00, "500" )
	PORT_DIPSETTING(    0x20, "700" )
	PORT_DIPSETTING(    0x10, "1000" )
	PORT_DIPSETTING(    0x30, "None" )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_SERVICE( 0x80, IP_ACTIVE_LOW )
INPUT_PORTS_END

/*******************************************************/
/*                                                     */
/* Midway "Sea Wolf"                                   */
/*                                                     */
/*******************************************************/

static struct IOReadPort seawolf_readport[] =
{
	{ 0x00, 0x00, invaders_shift_data_rev_r },
	{ 0x01, 0x01, seawolf_port_0_r },
	{ 0x02, 0x02, input_port_1_r },
	{ 0x03, 0x03, invaders_shift_data_r },
	{ -1 }  /* end of table */
};

static struct IOWritePort seawolf_writeport[] =
{
	{ 0x03, 0x03, invaders_shift_data_w },
	{ 0x04, 0x04, invaders_shift_amount_w },
	{ -1 }  /* end of table */
};

INPUT_PORTS_START( seawolf )
	PORT_START      /* IN0 */
	PORT_ANALOG( 0x1f, 0x01, IPT_PADDLE, 20, 5, 0, 0x1f)
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_DIPNAME( 0xc0, 0xc0, "Time" )
	PORT_DIPSETTING(    0x00, "61" )
	PORT_DIPSETTING(    0x40, "71" )
	PORT_DIPSETTING(    0x80, "81" )
	PORT_DIPSETTING(    0xc0, "91" )

	PORT_START      /* IN1 Dips & Coins */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_DIPNAME( 0x0c, 0x00, DEF_STR( Coinage ) )
	PORT_DIPSETTING(    0x04, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING(    0x0c, DEF_STR( 2C_3C ) )
	PORT_DIPSETTING(    0x08, DEF_STR( 1C_2C ) )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_TILT ) // Reset High Scores
	PORT_DIPNAME( 0xe0, 0x20, "Extended Play" )
	PORT_DIPSETTING(    0x00, "None" )
	PORT_DIPSETTING(    0x20, "2000" )
	PORT_DIPSETTING(    0x40, "3000" )
	PORT_DIPSETTING(    0x60, "4000" )
	PORT_DIPSETTING(    0x80, "5000" )
	PORT_DIPSETTING(    0xa0, "6000" )
	PORT_DIPSETTING(    0xc0, "7000" )
	PORT_DIPSETTING(    0xe0, "Test Mode" )
INPUT_PORTS_END

static struct MachineDriver machine_driver_seawolf =
{
	/* basic machine hardware */
	{
		{
			CPU_8080,
			2000000,        /* 2 Mhz? */
			readmem,writemem,seawolf_readport,seawolf_writeport,
			invaders_interrupt,2    /* two interrupts per frame */
		}
	},
	60, DEFAULT_60HZ_VBLANK_DURATION,
	1,      /* single CPU, no need for interleaving */
	0,

	/* video hardware */
	32*8, 32*8, { 0*8, 32*8-1, 0*8, 28*8-1 },
	0,      /* no gfxdecodeinfo - bitmapped display */
	256, 0,		/* leave extra colors for the overlay */
	init_palette,

	VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
	0,
	invaders_vh_start,
	invaders_vh_stop,
    invaders_vh_screenrefresh,

	/* sound hardware */
	0, 0, 0, 0,
	{
		{
			SOUND_SAMPLES,
			&samples_interface
		}
	}
};



/*******************************************************/
/*                                                     */
/* Midway "Gun Fight"                                  */
/*                                                     */
/*******************************************************/

static struct IOWritePort gunfight_writeport[] =
{
	{ 0x02, 0x02, invaders_shift_amount_w },
	{ 0x04, 0x04, invaders_shift_data_w },
	{ -1 }  /* end of table */
};

INPUT_PORTS_START( gunfight )
    /* Gun position uses bits 4-6, handled using fake paddles */
	PORT_START      /* IN0 - Player 2 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP   | IPF_8WAY | IPF_PLAYER2 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2 )        /* Move Man */
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT| IPF_8WAY | IPF_PLAYER2 )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 )              /* Fire */

	PORT_START      /* IN1 - Player 1 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY )              /* Move Man */
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_BUTTON1 )                    /* Fire */

#ifdef NOTDEF
	PORT_START      /* IN2 Dips & Coins */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START1 )
	PORT_DIPNAME( 0x0C, 0x00, "Plays" )
	PORT_DIPSETTING(    0x00, "1" )
	PORT_DIPSETTING(    0x04, "2" )
	PORT_DIPSETTING(    0x08, "3" )
	PORT_DIPSETTING(    0x0C, "4" )
	PORT_DIPNAME( 0x30, 0x00, "Time" ) /* These are correct */
	PORT_DIPSETTING(    0x00, "60" )
	PORT_DIPSETTING(    0x10, "70" )
	PORT_DIPSETTING(    0x20, "80" )
	PORT_DIPSETTING(    0x30, "90" )
	PORT_DIPNAME( 0xc0, 0x00, DEF_STR( Coinage ) )
	PORT_DIPSETTING(    0x00, "1 Coin - 1 Player" )
	PORT_DIPSETTING(    0x40, "1 Coin - 2 Players" )
	PORT_DIPSETTING(    0x80, "1 Coin - 3 Players" )
	PORT_DIPSETTING(    0xc0, "1 Coin - 4 Players" )
#endif

	PORT_START      /* IN2 Dips & Coins */
	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Coinage ) )
	PORT_DIPSETTING(    0x00, "1 Coin" )
	PORT_DIPSETTING(    0x01, "2 Coins" )
	PORT_DIPSETTING(    0x02, "3 Coins" )
	PORT_DIPSETTING(    0x03, "4 Coins" )
	PORT_DIPNAME( 0x0C, 0x00, "Plays" )
	PORT_DIPSETTING(    0x00, "1" )
	PORT_DIPSETTING(    0x04, "2" )
	PORT_DIPSETTING(    0x08, "3" )
	PORT_DIPSETTING(    0x0C, "4" )
	PORT_DIPNAME( 0x30, 0x00, "Time" ) /* These are correct */
	PORT_DIPSETTING(    0x00, "60" )
	PORT_DIPSETTING(    0x10, "70" )
	PORT_DIPSETTING(    0x20, "80" )
	PORT_DIPSETTING(    0x30, "90" )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_COIN1 )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_START1 )

	PORT_START                                                                                          /* Player 2 Gun */
	PORT_ANALOGX( 0xff, 0x00, IPT_PADDLE | IPF_PLAYER2, 50, 10, 1, 255, IP_KEY_NONE, IP_KEY_NONE, IP_JOY_NONE, IP_JOY_NONE )

	PORT_START                                                                                          /* Player 1 Gun */
	PORT_ANALOGX( 0xff, 0x00, IPT_PADDLE, 50, 10, 1, 255, KEYCODE_Z, KEYCODE_A, IP_JOY_NONE, IP_JOY_NONE )
INPUT_PORTS_END

static struct MachineDriver machine_driver_gunfight =
{
	/* basic machine hardware */
	{
		{
			CPU_8080,
			2000000,        /* 2 Mhz? */
			readmem,writemem,boothill_readport,gunfight_writeport,
			invaders_interrupt,2    /* two interrupts per frame */
		}
	},
	60, DEFAULT_60HZ_VBLANK_DURATION,
	1,      /* single CPU, no need for interleaving */
	0,

	/* video hardware */
	32*8, 32*8, { 0*8, 32*8-1, 0*8, 28*8-1 },
	0,      /* no gfxdecodeinfo - bitmapped display */
	256, 0,		/* leave extra colors for the overlay */
	init_palette,

	VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
	0,
	invaders_vh_start,
	invaders_vh_stop,
    invaders_vh_screenrefresh,

	/* sound hardware */
	0, 0, 0, 0,
	{
		{
			SOUND_SAMPLES,
			&samples_interface
		}
	}
};


/*******************************************************/
/*                                                     */
/* Midway "280 ZZZAP"                                  */
/*                                                     */
/*******************************************************/

INPUT_PORTS_START( 280zzzap )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN ) /* 4 bit accel */
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_BUTTON1 ) /* Crude approximation using 2 buttons */
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_BUTTON2 )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_TOGGLE ) /* shift */
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_LOW,  IPT_COIN1 )
	PORT_BIT( 0x80, IP_ACTIVE_LOW,  IPT_START1 )

	PORT_START      /* IN1 - Steering Wheel */
	PORT_ANALOG( 0xff, 0x7f, IPT_PADDLE | IPF_REVERSE, 100, 10, 0x01, 0xfe)

	PORT_START      /* IN2 Dips & Coins */
	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Coinage ) )
	PORT_DIPSETTING(    0x01, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING(    0x03, DEF_STR( 2C_3C ) )
	PORT_DIPSETTING(    0x02, DEF_STR( 1C_3C ) )
	PORT_DIPNAME( 0x0c, 0x00, "Time" )
	PORT_DIPSETTING(    0x0c, "60" )
	PORT_DIPSETTING(    0x00, "80" )
	PORT_DIPSETTING(    0x08, "99" )
	PORT_DIPSETTING(    0x04, "Test Mode" )
	PORT_DIPNAME( 0x30, 0x00, "Extended Time" )
	PORT_DIPSETTING(    0x00, "Score >= 2.5" )
	PORT_DIPSETTING(    0x10, "Score >= 2" )
	PORT_DIPSETTING(    0x20, "None" )
/* 0x30 same as 0x20 */
	PORT_DIPNAME( 0xc0, 0x00, "Language")
	PORT_DIPSETTING(    0x00, "English" )
	PORT_DIPSETTING(    0x40, "German" )
	PORT_DIPSETTING(    0x80, "French" )
	PORT_DIPSETTING(    0xc0, "Spanish" )
INPUT_PORTS_END

static struct MachineDriver machine_driver_280zzzap =
{
	/* basic machine hardware */
	{
		{
			CPU_8080,
			2000000,        /* 2 Mhz? */
			readmem,writemem,readport,seawolf_writeport,
			invaders_interrupt,2    /* two interrupts per frame */
		}
	},
	60, DEFAULT_60HZ_VBLANK_DURATION,       /* frames per second, vblank duration */
	1,      /* single CPU, no need for interleaving */
	0,

	/* video hardware */
	32*8, 32*8, { 0*8, 32*8-1, 0*8, 28*8-1 },
	0,      /* no gfxdecodeinfo - bitmapped display */
	256, 0,		/* leave extra colors for the overlay */
	init_palette,

	VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
	0,
	invaders_vh_start,
	invaders_vh_stop,
	invaders_vh_screenrefresh,

	/* sound hardware */
	0, 0, 0, 0
};


/*******************************************************/
/*                                                     */
/* Taito "Lupin III"                                   */
/*                                                     */
/*******************************************************/

INPUT_PORTS_START( lupin3 )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNUSED )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNUSED)
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_TILT )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_BUTTON2 )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_RIGHT | IPF_4WAY )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_DOWN  | IPF_4WAY )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_LEFT  | IPF_4WAY )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_UP    | IPF_4WAY )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_RIGHT | IPF_4WAY | IPF_COCKTAIL )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_DOWN  | IPF_4WAY | IPF_COCKTAIL )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_LEFT  | IPF_4WAY | IPF_COCKTAIL )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_UP    | IPF_4WAY | IPF_COCKTAIL )

	PORT_START      /* DSW0 */
	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x00, "3" )
	PORT_DIPSETTING(    0x01, "4" )
	PORT_DIPSETTING(    0x02, "5" )
	PORT_DIPSETTING(    0x03, "6" )
	PORT_DIPNAME( 0x04, 0x00, DEF_STR( Cabinet ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Upright ) )
	PORT_DIPSETTING(    0x04, DEF_STR( Cocktail ) )
	PORT_DIPNAME( 0x08, 0x00, "Check" )
	PORT_DIPSETTING(    0x00, "8" )
	PORT_DIPSETTING(    0x08, "2" )
	PORT_DIPNAME( 0x10, 0x00, "Language" )
	PORT_DIPSETTING(    0x00, "English" )
	PORT_DIPSETTING(    0x10, "Japan" )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH,  IPT_UNUSED )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH,  IPT_UNUSED )
	PORT_BITX(0x80,     0x00, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x80, DEF_STR( On ) )
INPUT_PORTS_END


/*******************************************************/
/*                                                     */
/* Nintendo "Heli Fire"                                */
/*                                                     */
/*******************************************************/

INPUT_PORTS_START( helifire )
	PORT_START      /* 00 Main Controls */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY  )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNUSED )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNUSED )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNUSED )

	PORT_START      /* 01 Player 2 Controls */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNUSED )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNUSED )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNUSED )

	PORT_START      /* Start and Coin Buttons */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNUSED )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNUSED )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_START2 )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNUSED ) /* Marked for   */
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNUSED ) /* Expansion    */
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNUSED ) /* on Schematic */
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_COIN1 )

	PORT_START      /* DSW */
	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x00, "3" )
	PORT_DIPSETTING(    0x01, "4" )
	PORT_DIPSETTING(    0x02, "5" )
	PORT_DIPSETTING(    0x03, "6" )
	PORT_DIPNAME( 0x0c, 0x00, DEF_STR( Bonus_Life ) )
	PORT_DIPSETTING(    0x0c, "5000" )
	PORT_DIPSETTING(    0x04, "6000" )
	PORT_DIPSETTING(    0x08, "8000" )
	PORT_DIPSETTING(    0x00, "10000" )
	PORT_DIPNAME( 0x10, 0x00, DEF_STR( Coinage ) )
	PORT_DIPSETTING(    0x10, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 1C_1C ) )
	PORT_DIPNAME( 0x80, 0x80, DEF_STR( Cabinet ) )
	PORT_DIPSETTING(    0x80, DEF_STR( Upright ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Cocktail ) )
INPUT_PORTS_END


/*******************************************************/
/*                                                     */
/* Nintendo "Space Fever (Color)"                      */
/*                                                     */
/*******************************************************/

INPUT_PORTS_START( spacefev )
	PORT_START      /* 00 Main Controls */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON1)
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_START2 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_COIN1 )

	PORT_START      /* 01 Player 2 Controls */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 )
	PORT_BITX(0x08, 0x00, 0, "Start Game A", KEYCODE_Q, IP_JOY_NONE )
	PORT_BITX(0x10, 0x00, 0, "Start Game B", KEYCODE_W, IP_JOY_NONE )
	PORT_BITX(0x20, 0x00, 0, "Start Game C", KEYCODE_E, IP_JOY_NONE )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNUSED )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNUSED ) /* If on low the game doesn't start */

	PORT_START      /* DSW */
	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x00, "3" )
	PORT_DIPSETTING(    0x01, "4" )
	PORT_DIPSETTING(    0x02, "5" )
	PORT_DIPSETTING(    0x03, "6" )
//	PORT_DIPNAME( 0xfc, 0x00, DEF_STR( Unknown ) )
//	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
//	PORT_DIPSETTING(    0xfc, DEF_STR( Off ) )
INPUT_PORTS_END


/*******************************************************/
/*                                                     */
/* Taito "Polaris"                                     */
/*                                                     */
/*******************************************************/

static struct IOWritePort polaris_writeport[] =
{
	{ 0x01, 0x01, invaders_shift_amount_w },
	{ 0x03, 0x03, invaders_shift_data_w },
	{ -1 }  /* end of table */
};

INPUT_PORTS_START( polaris )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN )   // Tilt
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN)

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY )

	PORT_START      /* DSW0 */
	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x00, "3" )
	PORT_DIPSETTING(    0x01, "4" )
	PORT_DIPSETTING(    0x02, "5" )
	PORT_DIPSETTING(    0x03, "6" )
	PORT_DIPNAME( 0x04, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x04, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x08, 0x00, DEF_STR( Bonus_Life ) )
	PORT_DIPSETTING(    0x08, "1000" )
	PORT_DIPSETTING(    0x00, "1500" )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT )
	PORT_DIPNAME( 0x80, 0x00, "Preset Mode" )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x80, DEF_STR( On ) )
INPUT_PORTS_END


static struct MachineDriver machine_driver_polaris =
{
	/* basic machine hardware */
	{
		{
			CPU_8080,
			2000000,        /* 2 Mhz? */
			readmem,writemem,readport,polaris_writeport,
			invaders_interrupt,2    /* two interrupts per frame */
		}
	},
	60, DEFAULT_60HZ_VBLANK_DURATION,       /* frames per second, vblank duration */
	1,      /* single CPU, no need for interleaving */
	0,

	/* video hardware */
	32*8, 32*8, { 0*8, 32*8-1, 0*8, 28*8-1 },
	0,      /* no gfxdecodeinfo - bitmapped display */
	8, 0,
	invadpt2_vh_convert_color_prom,

	VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
	0,
	invaders_vh_start,
	invaders_vh_stop,
	invaders_vh_screenrefresh,

	/* sound hardware */
	0,0,0,0
};


/*******************************************************/
/*                                                     */
/* Midway "Laguna Racer"                               */
/*                                                     */
/*******************************************************/

INPUT_PORTS_START( lagunar )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN ) /* 4 bit accel */
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_BUTTON1 ) /* Crude approximation using 2 buttons */
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_BUTTON2 )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON4 | IPF_TOGGLE ) /* shift */
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_LOW,  IPT_COIN1 )
	PORT_BIT( 0x80, IP_ACTIVE_LOW,  IPT_START1 )

	PORT_START      /* IN1 - Steering Wheel */
	PORT_ANALOG( 0xff, 0x7f, IPT_PADDLE | IPF_REVERSE, 100, 10, 0x01, 0xfe)

	PORT_START      /* IN2 Dips & Coins */
	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Coinage ) )
	PORT_DIPSETTING(    0x02, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING(    0x03, DEF_STR( 2C_3C ) )
	PORT_DIPSETTING(    0x01, DEF_STR( 1C_2C ) )
	PORT_DIPNAME( 0x0c, 0x0c, "Time" )
	PORT_DIPSETTING(    0x00, "45" )
	PORT_DIPSETTING(    0x04, "60" )
	PORT_DIPSETTING(    0x08, "75" )
	PORT_DIPSETTING(    0x0c, "90" )
	PORT_DIPNAME( 0x30, 0x00, "Extended Time" )
	PORT_DIPSETTING(    0x00, "350" )
	PORT_DIPSETTING(    0x10, "400" )
	PORT_DIPSETTING(    0x20, "450" )
	PORT_DIPSETTING(    0x30, "500" )
	PORT_DIPNAME( 0xc0, 0x00, "Test Modes")
	PORT_DIPSETTING(    0x00, "Play Mode" )
	PORT_DIPSETTING(    0x40, "RAM/ROM" )
	PORT_DIPSETTING(    0x80, "Steering" )
	PORT_DIPSETTING(    0xc0, "No Extended Play" )
INPUT_PORTS_END


/*******************************************************/
/*                                                     */
/* Midway "M-4"                                        */
/*                                                     */
/*******************************************************/

static struct IOReadPort m4_readport[] =
{
	{ 0x00, 0x00, input_port_0_r },
	{ 0x01, 0x01, input_port_1_r },
	{ 0x02, 0x02, input_port_2_r },
	{ 0x03, 0x03, boothill_shift_data_r },
	{ -1 }  /* end of table */
};

INPUT_PORTS_START( m4 )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT( 0x02, IP_ACTIVE_LOW,  IPT_JOYSTICKLEFT_UP   | IPF_2WAY )
	PORT_BIT( 0x04, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT( 0x08, IP_ACTIVE_LOW,  IPT_JOYSTICKLEFT_DOWN | IPF_2WAY )
	PORT_BIT( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON1 ) /* Left trigger */
	PORT_BIT( 0x20, IP_ACTIVE_LOW,  IPT_BUTTON2 ) /* Left reload */
	PORT_BIT( 0x40, IP_ACTIVE_LOW,  IPT_UNUSED )
	PORT_BIT( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_LOW,  IPT_JOYSTICKRIGHT_UP   | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT( 0x04, IP_ACTIVE_LOW,  IPT_START1 )  /* 1 player */
	PORT_BIT( 0x08, IP_ACTIVE_LOW,  IPT_JOYSTICKRIGHT_DOWN | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT( 0x10, IP_ACTIVE_LOW,  IPT_BUTTON3 ) /* Right trigger */
	PORT_BIT( 0x20, IP_ACTIVE_LOW,  IPT_BUTTON4 ) /* Right reload */
	PORT_BIT( 0x40, IP_ACTIVE_LOW,  IPT_START2 )  /* 2 player */
	PORT_BIT( 0x80, IP_ACTIVE_LOW,  IPT_UNUSED )

	PORT_START      /* IN2 Dips & Coins */
	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Coinage ) )
	PORT_DIPSETTING(    0x02, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0x03, DEF_STR( 2C_2C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING(    0x01, DEF_STR( 1C_2C ) )
	PORT_DIPNAME( 0x0c, 0x0c, "Time" )
	PORT_DIPSETTING(    0x00, "60" )
	PORT_DIPSETTING(    0x04, "70" )
	PORT_DIPSETTING(    0x08, "80" )
	PORT_DIPSETTING(    0x0C, "90" )
	PORT_SERVICE( 0x10, IP_ACTIVE_LOW )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )
INPUT_PORTS_END

static struct MachineDriver machine_driver_m4 =
{
	/* basic machine hardware */
	{
		{
			CPU_8080,
			2000000,        /* 2 Mhz? */
			readmem,writemem,m4_readport,spcenctr_writeport,
			invaders_interrupt,2    /* two interrupts per frame */
		}
	},
	60, DEFAULT_60HZ_VBLANK_DURATION,
	1,      /* single CPU, no need for interleaving */
	0,

	/* video hardware */
	32*8, 32*8, { 0*8, 32*8-1, 0*8, 28*8-1 },
	0,      /* no gfxdecodeinfo - bitmapped display */
	256, 0,		/* leave extra colors for the overlay */
	init_palette,

	VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
	0,
	invaders_vh_start,
	invaders_vh_stop,
    invaders_vh_screenrefresh,

	/* sound hardware */
	0, 0, 0, 0,
	{
		{
			SOUND_SAMPLES,
			&samples_interface
		}
	}
};


/*******************************************************/
/*                                                     */
/* Midway "Phantom II"                                 */
/*                                                     */
/* To Do : little fluffy clouds                        */
/*         you still see them sometimes in the desert  */
/*                                                     */
/*******************************************************/

INPUT_PORTS_START( phantom2 )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_COIN1 )

	PORT_START      /* IN2 Dips & Coins */
	PORT_DIPNAME( 0x01, 0x00, DEF_STR( Coinage ) )
	PORT_DIPSETTING(    0x01, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 1C_1C ) )
	PORT_DIPNAME( 0x06, 0x06, "Time" )
	PORT_DIPSETTING(    0x00, "45sec 20sec 20" )
	PORT_DIPSETTING(    0x02, "60sec 25sec 25" )
	PORT_DIPSETTING(    0x04, "75sec 30sec 30" )
	PORT_DIPSETTING(    0x06, "90sec 35sec 35" )
	PORT_SERVICE( 0x20, IP_ACTIVE_LOW )

INPUT_PORTS_END


/*******************************************************/
/*                                                     */
/* Midway "Dog Patch"                                  */
/*                                                     */
/*******************************************************/

INPUT_PORTS_START( dogpatch )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START1 )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START2 )
	PORT_ANALOG( 0x38, 0x1f, IPT_AD_STICK_X |IPF_PLAYER2, 25, 10, 0x05, 0x48)
	/* 6 bit horiz encoder - Gray's binary? */
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN2 )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON2 )

	PORT_START      /* IN1 */
	PORT_ANALOG( 0x3f, 0x1f, IPT_AD_STICK_X, 25, 10, 0x01, 0x3e) /* 6 bit horiz encoder - Gray's binary? */
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 )

	PORT_START      /* IN2 Dips & Coins */
	PORT_DIPNAME( 0x03, 0x00, "# Cans" )
	PORT_DIPSETTING(    0x03, "10" )
	PORT_DIPSETTING(    0x02, "15" )
	PORT_DIPSETTING(    0x01, "20" )
	PORT_DIPSETTING(    0x00, "25" )
	PORT_DIPNAME( 0x0c, 0x00, DEF_STR( Coinage ) )
	PORT_DIPSETTING(    0x08, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0x0c, DEF_STR( 2C_2C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING(    0x04, DEF_STR( 1C_2C ) )
	PORT_DIPNAME( 0x10, 0x00, "Extended Play" )
	PORT_DIPSETTING(    0x10, "3 extra cans" )
	PORT_DIPSETTING(    0x00, "5 extra cans" )
	PORT_SERVICE( 0x20, IP_ACTIVE_LOW )
	PORT_DIPNAME( 0xc0, 0x00, "Extended Play" )
	PORT_DIPSETTING(    0xc0, "150 Pts" )
	PORT_DIPSETTING(    0x80, "175 Pts" )
	PORT_DIPSETTING(    0x40, "225 Pts" )
	PORT_DIPSETTING(    0x00, "275 Pts" )
INPUT_PORTS_END


/*******************************************************/
/*                                                     */
/* Midway "4 Player Bowling"                           */
/*                                                     */
/*******************************************************/

static struct IOReadPort bowler_readport[] =
{
	{ 0x01, 0x01, invaders_shift_data_comp_r },
	{ 0x02, 0x02, input_port_0_r },				/* dip switch */
	{ 0x04, 0x04, input_port_1_r },				/* coins / switches */
	{ 0x05, 0x05, input_port_2_r },				/* ball vert */
	{ 0x06, 0x06, input_port_3_r },				/* ball horz */
	{ -1 }  /* end of table */
};

INPUT_PORTS_START( bowler )
	PORT_START      /* IN2 */
	PORT_DIPNAME( 0x03, 0x00, "Language" )
	PORT_DIPSETTING(    0x00, "English" )
	PORT_DIPSETTING(    0x01, "French" )
	PORT_DIPSETTING(    0x02, "German" )
  /*PORT_DIPSETTING(    0x03, "German" )*/
	PORT_DIPNAME( 0x04, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x04, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x08, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x08, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x10, 0x00, DEF_STR( Coinage ) )
	PORT_DIPSETTING(    0x10, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 1C_1C ) )
	PORT_DIPNAME( 0x20, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x20, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN ) /* effects button 1 */
	PORT_SERVICE( 0x80, IP_ACTIVE_HIGH )

	PORT_START      /* IN4 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_BUTTON2 )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNUSED )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNUSED )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNUSED )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNUSED )

	PORT_START      /* IN5 */
	PORT_ANALOG( 0xff, 0, IPT_TRACKBALL_Y | IPF_REVERSE, 10, 10, 0, 0)

	PORT_START      /* IN6 */
	PORT_ANALOG( 0xff, 0, IPT_TRACKBALL_X, 10, 10, 0, 0)
INPUT_PORTS_END

static struct MachineDriver machine_driver_bowler =
{
	/* basic machine hardware */
	{
		{
			CPU_8080,
			2000000,        /* 2 Mhz */
			readmem,writemem,bowler_readport,spcenctr_writeport,
			invaders_interrupt,2    /* two interrupts per frame */
		}
	},
	60, DEFAULT_60HZ_VBLANK_DURATION,
	1,      /* single CPU, no need for interleaving */
	0,

	/* video hardware */
	32*8, 32*8, { 0*8, 32*8-1, 0*8, 28*8-1 },
	0,      /* no gfxdecodeinfo - bitmapped display */
	256, 0,		/* leave extra colors for the overlay */
	init_palette,

	VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
	0,
	invaders_vh_start,
	invaders_vh_stop,
    invaders_vh_screenrefresh,

	/* sound hardware */
	0, 0, 0, 0,
};


/*******************************************************/
/*                                                     */
/* Midway "Shuffleboard"                               */
/*                                                     */
/*******************************************************/

static struct IOReadPort shuffle_readport[] =
{
	{ 0x01, 0x01, invaders_shift_data_r },
	{ 0x02, 0x02, input_port_0_r },				/* dip switch */
	{ 0x04, 0x04, input_port_1_r },				/* coins / switches */
	{ 0x05, 0x05, input_port_2_r },				/* ball vert */
	{ 0x06, 0x06, input_port_3_r },				/* ball horz */
	{ -1 }  /* end of table */
};

INPUT_PORTS_START( shuffle )
	PORT_START      /* DSW0 */
	PORT_DIPNAME( 0x03, 0x00, "Language" )
	PORT_DIPSETTING(    0x00, "English" )
	PORT_DIPSETTING(    0x01, "French" )
	PORT_DIPSETTING(    0x02, "German" )
  /*PORT_DIPSETTING(    0x03, "German" )*/
	PORT_DIPNAME( 0x0c, 0x04, "Points to Win" )
	PORT_DIPSETTING(    0x00, "25" )
	PORT_DIPSETTING(    0x04, "35" )
	PORT_DIPSETTING(    0x08, "40" )
	PORT_DIPSETTING(    0x0c, "50" )
	PORT_DIPNAME( 0x30, 0x10, DEF_STR( Coinage ) )
	PORT_DIPSETTING(    0x30, "2 Coins/1 Player  4 Coins/2 Players" )
	PORT_DIPSETTING(    0x20, "2 Coins/1 or 2 Players" )
	PORT_DIPSETTING(    0x10, "1 Coin/1 Player  2 Coins/2 Players" )
	PORT_DIPSETTING(    0x00, "1 Coin/1 or 2 Players" )
	PORT_DIPNAME( 0x40, 0x00, DEF_STR( Unknown ) )	/* time limit? */
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x40, DEF_STR( On ) )
	PORT_SERVICE( 0x80, IP_ACTIVE_LOW )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START1 )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START2 )
	PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_BUTTON1, "Game Select", IP_KEY_DEFAULT, IP_JOY_DEFAULT )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED )

	PORT_START      /* IN2 */
	PORT_ANALOG( 0xff, 0, IPT_TRACKBALL_Y, 10, 10, 0, 0)

	PORT_START      /* IN3 */
	PORT_ANALOG( 0xff, 0, IPT_TRACKBALL_X | IPF_REVERSE, 10, 10, 0, 0)
INPUT_PORTS_END

static struct MachineDriver machine_driver_shuffle =
{
	/* basic machine hardware */
	{
		{
			CPU_8080,
			2000000,        /* 2 Mhz */
			readmem,writemem,shuffle_readport,spcenctr_writeport,
			invaders_interrupt,2    /* two interrupts per frame */
		}
	},
	60, DEFAULT_60HZ_VBLANK_DURATION,
	1,      /* single CPU, no need for interleaving */
	0,

	/* video hardware */
	32*8, 32*8, { 0*8, 32*8-1, 0*8, 28*8-1 },
	0,      /* no gfxdecodeinfo - bitmapped display */
	256, 0,		/* leave extra colors for the overlay */
	init_palette,

	VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
	0,
	invaders_vh_start,
	invaders_vh_stop,
    invaders_vh_screenrefresh,

	/* sound hardware */
	0, 0, 0, 0,
};


/*******************************************************/
/*                                                     */
/* Midway "Blue Shark"                                 */
/*                                                     */
/*******************************************************/

static struct IOReadPort blueshrk_readport[] =
{
	{ 0x00, 0x00, invaders_shift_data_rev_r },
	{ 0x01, 0x01, input_port_1_r },
	{ 0x02, 0x02, input_port_0_r },
	{ 0x03, 0x03, invaders_shift_data_r },
	{ -1 }  /* end of table */
};

INPUT_PORTS_START( blueshrk )
	PORT_START      /* IN2 Dips & Coins */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN1 )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_TILT )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_DIPNAME( 0x60, 0x20, "Replay" )
	PORT_DIPSETTING(    0x20, "14000" )
	PORT_DIPSETTING(    0x40, "18000" )
	PORT_DIPSETTING(    0x60, "22000" )
	PORT_DIPSETTING(    0x00, "None" )
	PORT_SERVICE( 0x80, IP_ACTIVE_LOW )

	PORT_START      /* IN1 */
	PORT_ANALOG( 0x7f, 0x45, IPT_PADDLE, 100, 10, 0xf, 0x7f)
INPUT_PORTS_END

static struct MachineDriver machine_driver_blueshrk =
{
	/* basic machine hardware */
	{
		{
			CPU_8080,
			2000000,
			readmem,writemem,blueshrk_readport,spcenctr_writeport,
			invaders_interrupt,2    /* two interrupts per frame */
		}
	},
	60, DEFAULT_60HZ_VBLANK_DURATION,       /* frames per second, vblank duration */
	1,      /* single CPU, no need for interleaving */
	0,

	/* video hardware */
	32*8, 32*8, { 0*8, 32*8-1, 0*8, 28*8-1 },
	0,      /* no gfxdecodeinfo - bitmapped display */
	256, 0,		/* leave extra colors for the overlay */
	init_palette,

	VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
	0,
	invaders_vh_start,
	invaders_vh_stop,
	invaders_vh_screenrefresh,

	/* sound hardware */
	0, 0, 0, 0,
};


/*******************************************************/
/*                                                     */
/* Midway "Desert Gun"                                 */
/*                                                     */
/*******************************************************/

static struct IOReadPort desertgu_readport[] =
{
	{ 0x00, 0x00, invaders_shift_data_rev_r },
	{ 0x01, 0x01, desertgu_port_1_r },
	{ 0x02, 0x02, input_port_0_r },
	{ 0x03, 0x03, invaders_shift_data_r },
	{ -1 }  /* end of table */
};

static struct IOWritePort desertgu_writeport[] =
{
	{ 0x01, 0x01, invaders_shift_amount_w },
	{ 0x02, 0x02, invaders_shift_data_w },
	{ 0x07, 0x07, desertgu_controller_select_w },
	{ -1 }  /* end of table */
};

INPUT_PORTS_START( desertgu )
	PORT_START
	PORT_DIPNAME( 0x03, 0x00, "Time" )
	PORT_DIPSETTING(    0x00, "40" )
	PORT_DIPSETTING(    0x01, "50" )
	PORT_DIPSETTING(    0x02, "60" )
	PORT_DIPSETTING(    0x03, "70" )
	PORT_DIPNAME( 0x0c, 0x00, "Language" )
	PORT_DIPSETTING(    0x00, "English" )
	PORT_DIPSETTING(    0x04, "German" )
	PORT_DIPSETTING(    0x08, "French" )
    PORT_DIPSETTING(    0x0c, "Norwegian?" )
	PORT_DIPNAME( 0x30, 0x00, "Extended Play" )
	PORT_DIPSETTING(    0x00, "5000" )
	PORT_DIPSETTING(    0x10, "7000" )
	PORT_DIPSETTING(    0x20, "9000" )
	PORT_DIPSETTING(    0x30, "Test Mode" )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 )

	PORT_START      /* IN1 */
	PORT_ANALOG( 0x7f, 0x45, IPT_AD_STICK_X, 70, 10, 0xf, 0x7f)

	PORT_START      /* IN2 */
	PORT_ANALOG( 0x7f, 0x45, IPT_AD_STICK_Y, 70, 10, 0xf, 0x7f)
INPUT_PORTS_END


static struct MachineDriver machine_driver_desertgu =
{
	/* basic machine hardware */
	{
		{
			CPU_8080,
			2000000,
			readmem,writemem,desertgu_readport,desertgu_writeport,
			invaders_interrupt,2    /* two interrupts per frame */
		}
	},
	60, DEFAULT_60HZ_VBLANK_DURATION,       /* frames per second, vblank duration */
	1,      /* single CPU, no need for interleaving */
	0,

	/* video hardware */
	32*8, 32*8, { 0*8, 32*8-1, 0*8, 28*8-1 },
	0,      /* no gfxdecodeinfo - bitmapped display */
	256, 0,		/* leave extra colors for the overlay */
	init_palette,

	VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
	0,
	invaders_vh_start,
	invaders_vh_stop,
	invaders_vh_screenrefresh,

	/* sound hardware */
	0, 0, 0, 0,
};


/*******************************************************/
/*                                                     */
/* Midway "Extra Innings"                              */
/*                                                     */
/*******************************************************/

/*
 * The cocktail version has independent bat, pitch, and field controls
 * while the upright version ties the pairs of inputs together through
 * jumpers in the wiring harness.
 */
INPUT_PORTS_START( einnings )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 )			/* home bat */
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_LEFT )	/* home fielders left */
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_RIGHT )	/* home fielders right */
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_LEFT )	/* home pitch left */
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_RIGHT )	/* home pitch right */
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_DOWN )	/* home pitch slow */
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_UP )	/* home pitch fast */
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON2 )			/* vistor bat */
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_LEFT )	/* vistor fielders left */
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_RIGHT )	/* visitor fielders right */
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_LEFT )	/* visitor pitch left */
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_RIGHT)	/* visitor pitch right */
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_DOWN )	/* visitor pitch slow */
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_UP )	/* visitor pitch fast */
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 )

	PORT_START      /* IN2 Dips & Coins */
	PORT_DIPNAME( 0x07, 0x00, DEF_STR( Coinage ))
	PORT_DIPSETTING(    0x02, "2C/1 In (1 or 2 Players)" )
	PORT_DIPSETTING(    0x03, "2C/1 In 4C/3 In (1 or 2 Pls)" )
	PORT_DIPSETTING(    0x00, "1 Coin/1 Inning (1 or 2 Pls)" )
	PORT_DIPSETTING(    0x01, "1C/1 In 2C/3 In (1 or 2 Pls)" )
	PORT_DIPSETTING(    0x04, "1C/1Pl 2C/2Pl 4C/3Inn" )
	PORT_DIPSETTING(    0x05, "2C/1Pl 4C/2Pl 8C/3Inn" )
/* 0x06 and 0x07 same as 0x00 */
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_SERVICE( 0x40, IP_ACTIVE_LOW )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 )
INPUT_PORTS_END


/*******************************************************/
/*                                                     */
/* Midway "Amazing Maze"                               */
/*                                                     */
/*******************************************************/

INPUT_PORTS_START( maze )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER1 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER1 )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER2 )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_COIN1  )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN)
	PORT_SERVICE( 0x80, IP_ACTIVE_HIGH )

	PORT_START      /* DSW0 - Never read (?) */
	PORT_DIPNAME( 0x01, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x01, DEF_STR( On ) )
	PORT_DIPNAME( 0x02, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x02, DEF_STR( On ) )
	PORT_DIPNAME( 0x04, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x04, DEF_STR( On ) )
	PORT_DIPNAME( 0x08, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x08, DEF_STR( On ) )
	PORT_DIPNAME( 0x10, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x10, DEF_STR( On ) )
	PORT_DIPNAME( 0x20, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x20, DEF_STR( On ) )
	PORT_DIPNAME( 0x40, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x40, DEF_STR( On ) )
	PORT_DIPNAME( 0x80, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x80, DEF_STR( On ) )
INPUT_PORTS_END


/*******************************************************/
/*                                                     */
/* Midway "Tornado Baseball"                           */
/*                                                     */
/*******************************************************/

INPUT_PORTS_START( tornbase )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER1)
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER2 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2 )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN)

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER2)
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT      | IPF_PLAYER1 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_RIGHT | IPF_PLAYER1 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_DOWN  | IPF_PLAYER1 )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_UP    | IPF_PLAYER1 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1 )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1)
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN)

	PORT_START      /* DSW0 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_LOW,  IPT_UNKNOWN )
	PORT_BIT( 0x04, IP_ACTIVE_LOW,  IPT_UNKNOWN )
	PORT_DIPNAME( 0x78, 0x40, DEF_STR( Coinage ) )
	PORT_DIPSETTING(    0x18, "4 Coins/1 Inning 32/9" )
	PORT_DIPSETTING(    0x10, "3 Coins/1 Inning 24/9" )
	PORT_DIPSETTING(    0x38, "4 Coins/2 Innings 16/9" )
	PORT_DIPSETTING(    0x08, "2 Coins/1 Inning 16/9" )
	PORT_DIPSETTING(    0x30, "3 Coins/2 Innings 12/9" )
	PORT_DIPSETTING(    0x28, "2 Coins/2 Innings 8/9" )
	PORT_DIPSETTING(    0x00, "1 Coin/1 Inning 8/9" )
	PORT_DIPSETTING(    0x58, "4 Coins/4 Innings 8/9" )
	PORT_DIPSETTING(    0x50, "3 Coins/4 Innings 6/9" )
	PORT_DIPSETTING(    0x48, "2 Coins/4 Innings 4/9" )
	PORT_DIPSETTING(    0x20, "1 Coin/2 Innings 4/9" )
	PORT_DIPSETTING(    0x40, "1 Coin/4 Innings 2/9" )
	PORT_DIPSETTING(    0x78, "4 Coins/9 Innings" )
	PORT_DIPSETTING(    0x70, "3 Coins/9 Innings" )
	PORT_DIPSETTING(    0x68, "2 Coins/9 Innings" )
	PORT_DIPSETTING(    0x60, "1 Coin/9 Innings" )
	PORT_SERVICE( 0x80, IP_ACTIVE_HIGH )
INPUT_PORTS_END


/*******************************************************/
/*                                                     */
/* Midway "Checkmate"                                  */
/*                                                     */
/*******************************************************/

static struct IOReadPort checkmat_readport[] =
{
	{ 0x00, 0x00, input_port_0_r },
	{ 0x01, 0x01, input_port_1_r },
	{ 0x02, 0x02, input_port_2_r },
	{ 0x03, 0x03, input_port_3_r },
	{ -1 }  /* end of table */
};

static struct IOWritePort checkmat_writeport[] =
{
	{ -1 }  /* end of table */
};

INPUT_PORTS_START( checkmat )
	PORT_START      /* IN0  */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER1 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER1 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER2 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 )

	PORT_START      /* IN1  */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER3 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER3 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER3 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER3 )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER4 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER4 )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER4 )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER4 )

	PORT_START      /* IN2 Dips & Coins */
	PORT_DIPNAME( 0x01, 0x00, DEF_STR( Coinage ) )
	PORT_DIPSETTING(    0x00, "1 Coin/1 or 2 Playera" )
	PORT_DIPSETTING(    0x01, "1 Coin/1 to 4 Players" )
	PORT_DIPNAME( 0x02, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x02, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x0c, 0x00, "Rounds" )
	PORT_DIPSETTING(    0x00, "2" )
	PORT_DIPSETTING(    0x04, "3" )
	PORT_DIPSETTING(    0x08, "4" )
	PORT_DIPSETTING(    0x0c, "5" )
	PORT_DIPNAME( 0x10, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x10, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x60, 0x00, "Language?" )
	PORT_DIPSETTING(    0x00, "English?" )
	PORT_DIPSETTING(    0x20, "German?" )
	PORT_DIPSETTING(    0x40, "French?" )
	PORT_DIPSETTING(    0x60, "Spanish?" )
	PORT_SERVICE( 0x80, IP_ACTIVE_HIGH )

	PORT_START       /* IN3  */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START3 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_START4 )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNUSED )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNUSED )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNUSED )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_COIN1 )
INPUT_PORTS_END

static struct MachineDriver machine_driver_checkmat =
{
	/* basic machine hardware */
	{
		{
			CPU_8080,
			2000000,        /* 2 Mhz? */
			readmem,writemem,checkmat_readport,checkmat_writeport,
			invaders_interrupt,2    /* two interrupts per frame */
		}
	},
	60, DEFAULT_60HZ_VBLANK_DURATION,
	1,      /* single CPU, no need for interleaving */
	0,

	/* video hardware */
	32*8, 32*8, { 0*8, 32*8-1, 0*8, 28*8-1 },
	0,      /* no gfxdecodeinfo - bitmapped display */
	256, 0,		/* leave extra colors for the overlay */
	init_palette,

	VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
	0,
	invaders_vh_start,
	invaders_vh_stop,
    invaders_vh_screenrefresh,

	/* sound hardware */
	0, 0, 0, 0,
	{
		{
			SOUND_SAMPLES,
			&samples_interface
		}
	}
};


/*******************************************************/
/*                                                     */
/* Taito "Ozma Wars"                                   */
/*                                                     */
/*******************************************************/

INPUT_PORTS_START( ozmawars )
	PORT_START		/* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )

	PORT_START		/* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )

	PORT_START      /* DSW0 */
	PORT_DIPNAME( 0x03, 0x00, "Energy" )
	PORT_DIPSETTING(    0x00, "15000" )
	PORT_DIPSETTING(    0x01, "20000" )
	PORT_DIPSETTING(    0x02, "25000" )
	PORT_DIPSETTING(    0x03, "35000" )
	PORT_DIPNAME( 0x04, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x04, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x08, 0x00, "Preset Mode?" )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x08, DEF_STR( On ) )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 )
	PORT_DIPNAME( 0x80, 0x00, DEF_STR( Coinage ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING(    0x80, DEF_STR( 1C_2C ) )

	PORT_START		/* Dummy port for cocktail mode */
	PORT_DIPNAME( 0x01, 0x00, DEF_STR( Cabinet ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Upright ) )
	PORT_DIPSETTING(    0x01, DEF_STR( Cocktail ) )
INPUT_PORTS_END

INPUT_PORTS_START( spaceph )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )

	PORT_START      /* IN1 */
	PORT_DIPNAME( 0x03, 0x00, "Fuel" )
	PORT_DIPSETTING(    0x03, "35000" )
	PORT_DIPSETTING(    0x02, "25000" )
	PORT_DIPSETTING(    0x01, "20000" )
	PORT_DIPSETTING(    0x00, "15000" )
	PORT_DIPNAME( 0x04, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x04, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x08, 0x00, "Bonus Fuel" )
	PORT_DIPSETTING(    0x08, "10000" )
	PORT_DIPSETTING(    0x00, "15000" )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN ) /* Fire */
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN ) /* Left */
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN ) /* Right */
	PORT_DIPNAME( 0x80, 0x00, DEF_STR( Coinage ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING(    0x80, DEF_STR( 1C_2C ) )
INPUT_PORTS_END




/*******************************************************/
/*                                                     */
/* Emag "Super Invaders"                               */
/*                                                     */
/*******************************************************/

INPUT_PORTS_START( sinvemag )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY )
	PORT_BIT( 0x80, IP_ACTIVE_LOW,  IPT_UNKNOWN )

	PORT_START      /* DSW0 */
	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x00, "3" )
	PORT_DIPSETTING(    0x01, "4" )
	PORT_DIPSETTING(    0x02, "5" )
	PORT_DIPSETTING(    0x03, "6" )
	PORT_DIPNAME( 0x04, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x04, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x08, 0x00, DEF_STR( Bonus_Life ) )
	PORT_DIPSETTING(    0x08, "1000" )
	PORT_DIPSETTING(    0x00, "1500" )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 )
	PORT_DIPNAME( 0x80, 0x00, "Coin Info" )
	PORT_DIPSETTING(    0x80, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )

	PORT_START		/* Dummy port for cocktail mode */
	PORT_DIPNAME( 0x01, 0x00, DEF_STR( Cabinet ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Upright ) )
	PORT_DIPSETTING(    0x01, DEF_STR( Cocktail ) )
INPUT_PORTS_END



/*******************************************************/
/*                                                     */
/* Jatre Specter (Taito?)                              */
/*                                                     */
/*******************************************************/

INPUT_PORTS_START( jspecter )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY )
	/* Note: There must have been a toggle switch on the outside of the unit.
	   The difficulty can be set by the player */
	PORT_DIPNAME( 0x80, 0x00, DEF_STR( Difficulty ) )
	PORT_DIPSETTING(    0x80, "Easy" )
	PORT_DIPSETTING(    0x00, "Hard" )

	PORT_START      /* DSW0 */
	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x00, "3" )
	PORT_DIPSETTING(    0x01, "4" )
	PORT_DIPSETTING(    0x02, "5" )
	PORT_DIPSETTING(    0x03, "6" )
	PORT_DIPNAME( 0x04, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x04, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x08, 0x00, DEF_STR( Bonus_Life ) )
	PORT_DIPSETTING(    0x08, "1000" )
	PORT_DIPSETTING(    0x00, "1500" )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 )
	PORT_DIPNAME( 0x80, 0x00, "Coin Info" )
	PORT_DIPSETTING(    0x80, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )

	PORT_START		/* Dummy port for cocktail mode */
	PORT_DIPNAME( 0x01, 0x00, DEF_STR( Cabinet ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Upright ) )
	PORT_DIPSETTING(    0x01, DEF_STR( Cocktail ) )
INPUT_PORTS_END


/*******************************************************/
/*                                                     */
/* Taito "Balloon Bomber"                              */
/*                                                     */
/*******************************************************/

INPUT_PORTS_START( ballbomb )
	PORT_START		/* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )

	PORT_START		/* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )

	PORT_START      /* DSW0 */
	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x00, "3" )
	PORT_DIPSETTING(    0x01, "4" )
	PORT_DIPSETTING(    0x02, "5" )
	PORT_DIPSETTING(    0x03, "6" )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_TILT )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )

	PORT_START		/* Dummy port for cocktail mode */
	PORT_DIPNAME( 0x01, 0x00, DEF_STR( Cabinet ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Upright ) )
	PORT_DIPSETTING(    0x01, DEF_STR( Cocktail ) )
INPUT_PORTS_END


INPUT_PORTS_START( spceking )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )

	PORT_START		/* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY )
	PORT_BIT( 0x80, IP_ACTIVE_LOW,  IPT_UNKNOWN )

	PORT_START      /* DSW0 */
	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x00, "3" )
	PORT_DIPSETTING(    0x01, "4" )
	PORT_DIPSETTING(    0x02, "5" )
	PORT_DIPSETTING(    0x03, "6" )
	PORT_DIPNAME( 0x04, 0x00, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x04, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x08, 0x00, "Preset Mode" )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x08, DEF_STR( On ) )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 )
	PORT_DIPNAME( 0x80, 0x00, "Coin Info" )
	PORT_DIPSETTING(    0x80, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )

	PORT_START		/* Dummy port for cocktail mode */
	PORT_DIPNAME( 0x01, 0x00, DEF_STR( Cabinet ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Upright ) )
	PORT_DIPSETTING(    0x01, DEF_STR( Cocktail ) )
INPUT_PORTS_END





ROM_START( invaders )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "invaders.h",   0x0000, 0x0800, 0x734f5ad8 )
	ROM_LOAD( "invaders.g",   0x0800, 0x0800, 0x6bfaca4a )
	ROM_LOAD( "invaders.f",   0x1000, 0x0800, 0x0ccead96 )
	ROM_LOAD( "invaders.e",   0x1800, 0x0800, 0x14e538b0 )
ROM_END

ROM_START( earthinv )
	ROM_REGION( 0x10000, REGION_CPU1 )             /* 64k for code */
	ROM_LOAD( "earthinv.h",   0x0000, 0x0800, 0x58a750c8 )
	ROM_LOAD( "earthinv.g",   0x0800, 0x0800, 0xb91742f1 )
	ROM_LOAD( "earthinv.f",   0x1000, 0x0800, 0x4acbbc60 )
	ROM_LOAD( "earthinv.e",   0x1800, 0x0800, 0xdf397b12 )
ROM_END

ROM_START( spaceatt )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "spaceatt.h",   0x0000, 0x0800, 0xa31d0756 )
	ROM_LOAD( "spaceatt.g",   0x0800, 0x0800, 0xf41241f7 )
	ROM_LOAD( "spaceatt.f",   0x1000, 0x0800, 0x4c060223 )
	ROM_LOAD( "spaceatt.e",   0x1800, 0x0800, 0x7cf6f604 )
ROM_END

ROM_START( sinvemag )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "sv0h.bin",     0x0000, 0x0400, 0x86bb8cb6 )
	ROM_LOAD( "emag_si.b",    0x0400, 0x0400, 0xfebe6d1a )
	ROM_LOAD( "emag_si.c",    0x0800, 0x0400, 0xaafb24f7 )
	ROM_LOAD( "emag_si.d",    0x1400, 0x0400, 0x68c4b9da )
	ROM_LOAD( "emag_si.e",    0x1800, 0x0400, 0xc4e80586 )
	ROM_LOAD( "emag_si.f",    0x1c00, 0x0400, 0x077f5ef2 )
ROM_END

ROM_START( alieninv )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "1h.bin",       0x0000, 0x0800, 0xc46df7f4 )
	ROM_LOAD( "1g.bin",       0x0800, 0x0800, 0x4b1112d6 )
	ROM_LOAD( "1f.bin",       0x1000, 0x0800, 0Xadca18a5 )
	ROM_LOAD( "1e.bin",       0x1800, 0x0800, 0x0449CB52 )
ROM_END

ROM_START( sitv )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "tv0h.s1",      0x0000, 0x0800, 0xfef18aad )
	ROM_LOAD( "tv04.m1",      0x1800, 0x0800, 0xcd2c67f6 )
	ROM_LOAD( "tv03.n1",      0x1000, 0x0800, 0x0ad3657f )
	ROM_LOAD( "tv02.rp1",     0x0800, 0x0800, 0x3c759a90 )
ROM_END

ROM_START( sicv )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "cv17.bin",     0x0000, 0x0800, 0x3dfbe9e6 )
	ROM_LOAD( "cv18.bin",     0x0800, 0x0800, 0xbc3c82bf )
	ROM_LOAD( "cv19.bin",     0x1000, 0x0800, 0xd202b41c )
	ROM_LOAD( "cv20.bin",     0x1800, 0x0800, 0xc74ee7b6 )

	ROM_REGION( 0x0800, REGION_PROMS )		/* color maps player 1/player 2 */
	ROM_LOAD( "cv01_1.bin",   0x0000, 0x0400, 0xaac24f34 )
	ROM_LOAD( "cv02_2.bin",   0x0400, 0x0400, 0x2bdf83a0 )
ROM_END

ROM_START( sisv )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "sv0h.bin",     0x0000, 0x0400, 0x86bb8cb6 )
	ROM_LOAD( "sv02.bin",     0x0400, 0x0400, 0x0e159534 )
	ROM_LOAD( "sv10.bin",     0x0800, 0x0400, 0x483e651e )
	ROM_LOAD( "sv04.bin",     0x1400, 0x0400, 0x1293b826 )
	ROM_LOAD( "sv09.bin",     0x1800, 0x0400, 0xcd80b13f )
	ROM_LOAD( "sv06.bin",     0x1c00, 0x0400, 0x2c68e0b4 )

	ROM_REGION( 0x0800, REGION_PROMS )		/* color maps player 1/player 2 */
	ROM_LOAD( "cv01_1.bin",   0x0000, 0x0400, 0xaac24f34 )
	ROM_LOAD( "cv02_2.bin",   0x0400, 0x0400, 0x2bdf83a0 )
ROM_END

ROM_START( sisv2 )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "sv0h.bin",     0x0000, 0x0400, 0x86bb8cb6 )
	ROM_LOAD( "emag_si.b",    0x0400, 0x0400, 0xfebe6d1a )
	ROM_LOAD( "sv12",         0x0800, 0x0400, 0xa08e7202 )
	ROM_LOAD( "sv04.bin",     0x1400, 0x0400, 0x1293b826 )
	ROM_LOAD( "sv13",         0x1800, 0x0400, 0xa9011634 )
	ROM_LOAD( "sv14",         0x1c00, 0x0400, 0x58730370 )

	ROM_REGION( 0x0800, REGION_PROMS )		/* color maps player 1/player 2 */
	ROM_LOAD( "cv01_1.bin",   0x0000, 0x0400, 0xaac24f34 )
	ROM_LOAD( "cv02_2.bin",   0x0400, 0x0400, 0x2bdf83a0 )
ROM_END

ROM_START( spceking )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "invaders.h",   0x0000, 0x0800, 0x734f5ad8 )
	ROM_LOAD( "spcekng2",     0x0800, 0x0800, 0x96dcdd42 )
	ROM_LOAD( "spcekng3",     0x1000, 0x0800, 0x95fc96ad )
	ROM_LOAD( "spcekng4",     0x1800, 0x0800, 0x54170ada )
ROM_END

ROM_START( spcewars )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "sanritsu.1",   0x0000, 0x0400, 0xca331679 )
	ROM_LOAD( "sanritsu.2",   0x0400, 0x0400, 0x48dc791c )
	ROM_LOAD( "sanritsu.3",   0x0800, 0x0400, 0xc34842cb )
	ROM_LOAD( "sanritsu.4",   0x0C00, 0x0400, 0xa7fdfd0e )
	ROM_LOAD( "sanritsu.5",   0x1000, 0x0400, 0x77475431 )
	ROM_LOAD( "sanritsu.6",   0x1400, 0x0400, 0x392ef82c )
	ROM_LOAD( "sanritsu.7",   0x1800, 0x0400, 0xb3a93df8 )
	ROM_LOAD( "sanritsu.8",   0x1c00, 0x0400, 0x64fdc3e1 )
	ROM_LOAD( "sanritsu.9",   0x4000, 0x0400, 0xb2f29601 )
ROM_END

ROM_START( spacewr3 )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "ic36.bin",     0x0000, 0x0800, 0x9e30f88a )
	ROM_LOAD( "ic35.bin",     0x0800, 0x0800, 0x40c2d55b )
	ROM_LOAD( "ic34.bin",     0x1000, 0x0800, 0xb435f021 )
	ROM_LOAD( "ic33.bin",     0x1800, 0x0800, 0xcbdc6fe8 )
	ROM_LOAD( "ic32.bin",     0x4000, 0x0800, 0x1e5a753c )
ROM_END

ROM_START( invaderl )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "c01",          0x0000, 0x0400, 0x499f253a )
	ROM_LOAD( "c02",          0x0400, 0x0400, 0x2d0b2e1f )
	ROM_LOAD( "c03",          0x0800, 0x0400, 0x03033dc2 )
	ROM_LOAD( "c04",          0x1400, 0x0400, 0x455b1fa7 )
	ROM_LOAD( "c05",          0x1800, 0x0400, 0x40cbef75 )
	ROM_LOAD( "sv06.bin",     0x1c00, 0x0400, 0x2c68e0b4 )
	ROM_LOAD( "c07",          0x1000, 0x0400, 0x5a7bbf1f )
ROM_END

ROM_START( jspecter )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "3305.u6",      0x0000, 0x1000, 0xab211a4f )
	ROM_LOAD( "3306.u7",      0x1400, 0x1000, 0x0df142a7 )
ROM_END

ROM_START( invadpt2 )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "pv.01",        0x0000, 0x0800, 0x7288a511 )
	ROM_LOAD( "pv.02",        0x0800, 0x0800, 0x097dd8d5 )
	ROM_LOAD( "pv.03",        0x1000, 0x0800, 0x1766337e )
	ROM_LOAD( "pv.04",        0x1800, 0x0800, 0x8f0e62e0 )
	ROM_LOAD( "pv.05",        0x4000, 0x0800, 0x19b505e9 )

	ROM_REGION( 0x0800, REGION_PROMS )		/* color maps player 1/player 2 */
	ROM_LOAD( "pv06_1.bin",   0x0000, 0x0400, 0xa732810b )
	ROM_LOAD( "pv07_2.bin",   0x0400, 0x0400, 0x2c5b91cb )
ROM_END

ROM_START( invdpt2m )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "invdelux.h",   0x0000, 0x0800, 0xe690818f )
	ROM_LOAD( "invdelux.g",   0x0800, 0x0800, 0x4268c12d )
	ROM_LOAD( "invdelux.f",   0x1000, 0x0800, 0xf4aa1880 )
	ROM_LOAD( "invdelux.e",   0x1800, 0x0800, 0x408849c1 )
	ROM_LOAD( "invdelux.d",   0x4000, 0x0800, 0xe8d5afcd )
ROM_END

ROM_START( invad2ct )
    ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
    ROM_LOAD( "invad2ct.h",   0x0000, 0x0800, 0x51d02a71 )
    ROM_LOAD( "invad2ct.g",   0x0800, 0x0800, 0x533ac770 )
    ROM_LOAD( "invad2ct.f",   0x1000, 0x0800, 0xd1799f39 )
    ROM_LOAD( "invad2ct.e",   0x1800, 0x0800, 0x291c1418 )
    ROM_LOAD( "invad2ct.b",   0x5000, 0x0800, 0x8d9a07c4 )
    ROM_LOAD( "invad2ct.a",   0x5800, 0x0800, 0xefdabb03 )
ROM_END

ROM_START( invrvnge )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "invrvnge.h",   0x0000, 0x0800, 0xaca41bbb )
	ROM_LOAD( "invrvnge.g",   0x0800, 0x0800, 0xcfe89dad )
	ROM_LOAD( "invrvnge.f",   0x1000, 0x0800, 0xe350de2c )
	ROM_LOAD( "invrvnge.e",   0x1800, 0x0800, 0x1ec8dfc8 )
ROM_END

ROM_START( invrvnga )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "5m.bin",       0x0000, 0x0800, 0xb145cb71 )
	ROM_LOAD( "5n.bin",       0x0800, 0x0800, 0x660e8af3 )
	ROM_LOAD( "5p.bin",       0x1000, 0x0800, 0x6ec5a9ad )
	ROM_LOAD( "5r.bin",       0x1800, 0x0800, 0x74516811 )
ROM_END

ROM_START( spclaser )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "la01",         0x0000, 0x0800, 0xbedc0078 )
	ROM_LOAD( "spcewarl.2",   0x0800, 0x0800, 0x43bc65c5 )
	ROM_LOAD( "la03",         0x1000, 0x0800, 0x1083e9cc )
	ROM_LOAD( "la04",         0x1800, 0x0800, 0x5116b234 )
ROM_END

ROM_START( laser )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "1.u36",        0x0000, 0x0800, 0xb44e2c41 )
	ROM_LOAD( "2.u35",        0x0800, 0x0800, 0x9876f331 )
	ROM_LOAD( "3.u34",        0x1000, 0x0800, 0xed79000b )
	ROM_LOAD( "4.u33",        0x1800, 0x0800, 0x10a160a1 )
ROM_END

ROM_START( spcewarl )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "spcewarl.1",   0x0000, 0x0800, 0x1fcd34d2 )
	ROM_LOAD( "spcewarl.2",   0x0800, 0x0800, 0x43bc65c5 )
	ROM_LOAD( "spcewarl.3",   0x1000, 0x0800, 0x7820df3a )
	ROM_LOAD( "spcewarl.4",   0x1800, 0x0800, 0xadc05b8d )
ROM_END

ROM_START( galxwars )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "galxwars.0",   0x0000, 0x0400, 0x608bfe7f )
	ROM_LOAD( "galxwars.1",   0x0400, 0x0400, 0xa810b258 )
	ROM_LOAD( "galxwars.2",   0x0800, 0x0400, 0x74f31781 )
	ROM_LOAD( "galxwars.3",   0x0c00, 0x0400, 0xc88f886c )
	ROM_LOAD( "galxwars.4",   0x4000, 0x0400, 0xae4fe8fb )
	ROM_LOAD( "galxwars.5",   0x4400, 0x0400, 0x37708a35 )
ROM_END

ROM_START( starw )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "roma",         0x0000, 0x0400, 0x60e8993c )
	ROM_LOAD( "romb",         0x0400, 0x0400, 0xb8060773 )
	ROM_LOAD( "romc",         0x0800, 0x0400, 0x307ce6b8 )
	ROM_LOAD( "romd",         0x1400, 0x0400, 0x2b0d0a88 )
	ROM_LOAD( "rome",         0x1800, 0x0400, 0x5b1c3ad0 )
	ROM_LOAD( "romf",         0x1c00, 0x0400, 0xc8e42d3d )
ROM_END

ROM_START( lrescue )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "lrescue.1",    0x0000, 0x0800, 0x2bbc4778 )
	ROM_LOAD( "lrescue.2",    0x0800, 0x0800, 0x49e79706 )
	ROM_LOAD( "lrescue.3",    0x1000, 0x0800, 0x1ac969be )
	ROM_LOAD( "lrescue.4",    0x1800, 0x0800, 0x782fee3c )
	ROM_LOAD( "lrescue.5",    0x4000, 0x0800, 0x58fde8bc )
	ROM_LOAD( "lrescue.6",    0x4800, 0x0800, 0xbfb0f65d )

	ROM_REGION( 0x0800, REGION_PROMS )		/* color map */
	ROM_LOAD( "7643-1.cpu",   0x0000, 0x0400, 0x8b2e38de )
	ROM_RELOAD(  			  0x0400, 0x0400 )
ROM_END

ROM_START( grescue )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "lrescue.1",    0x0000, 0x0800, 0x2bbc4778 )
	ROM_LOAD( "lrescue.2",    0x0800, 0x0800, 0x49e79706 )
	ROM_LOAD( "lrescue.3",    0x1000, 0x0800, 0x1ac969be )
	ROM_LOAD( "grescue.4",    0x1800, 0x0800, 0xca412991 )
	ROM_LOAD( "grescue.5",    0x4000, 0x0800, 0xa419a4d6 )
	ROM_LOAD( "lrescue.6",    0x4800, 0x0800, 0xbfb0f65d )

	ROM_REGION( 0x0800, REGION_PROMS )		/* color map */
	ROM_LOAD( "7643-1.cpu",   0x0000, 0x0400, 0x8b2e38de )
	ROM_RELOAD(  			  0x0400, 0x0400 )
ROM_END

ROM_START( desterth )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "36_h.bin",     0x0000, 0x0800, 0xf86923e5 )
	ROM_LOAD( "35_g.bin",     0x0800, 0x0800, 0x797f440d )
	ROM_LOAD( "34_f.bin",     0x1000, 0x0800, 0x993d0846 )
	ROM_LOAD( "33_e.bin",     0x1800, 0x0800, 0x8d155fc5 )
	ROM_LOAD( "32_d.bin",     0x4000, 0x0800, 0x3f531b6f )
	ROM_LOAD( "31_c.bin",     0x4800, 0x0800, 0xab019c30 )
	ROM_LOAD( "42_b.bin",     0x5000, 0x0800, 0xed9dbac6 )

	ROM_REGION( 0x0800, REGION_PROMS )		/* color map */
	ROM_LOAD( "7643-1.cpu",   0x0000, 0x0400, 0x8b2e38de )
	ROM_RELOAD(  			  0x0400, 0x0400 )
ROM_END

ROM_START( cosmicmo )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "cosmicmo.1",   0x0000, 0x0400, 0xd6e4e5da )
	ROM_LOAD( "cosmicmo.2",   0x0400, 0x0400, 0x8f7988e6 )
	ROM_LOAD( "cosmicmo.3",   0x0800, 0x0400, 0x2d2e9dc8 )
	ROM_LOAD( "cosmicmo.4",   0x0c00, 0x0400, 0x26cae456 )
	ROM_LOAD( "cosmicmo.5",   0x4000, 0x0400, 0xb13f228e )
	ROM_LOAD( "cosmicmo.6",   0x4400, 0x0400, 0x4ae1b9c4 )
	ROM_LOAD( "cosmicmo.7",   0x4800, 0x0400, 0x6a13b15b )
ROM_END

ROM_START( rollingc )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "rc01.bin",     0x0000, 0x0400, 0x66fa50bf )
	ROM_LOAD( "rc02.bin",     0x0400, 0x0400, 0x61c06ae4 )
	ROM_LOAD( "rc03.bin",     0x0800, 0x0400, 0x77e39fa0 )
	ROM_LOAD( "rc04.bin",     0x0c00, 0x0400, 0x3fdfd0f3 )
	ROM_LOAD( "rc05.bin",     0x1000, 0x0400, 0xc26a8f5b )
	ROM_LOAD( "rc06.bin",     0x1400, 0x0400, 0x0b98dbe5 )
	ROM_LOAD( "rc07.bin",     0x1800, 0x0400, 0x6242145c )
	ROM_LOAD( "rc08.bin",     0x1c00, 0x0400, 0xd23c2ef1 )
	ROM_LOAD( "rc09.bin",     0x4000, 0x0800, 0x2e2c5b95 )
	ROM_LOAD( "rc10.bin",     0x4800, 0x0800, 0xef94c502 )
	ROM_LOAD( "rc11.bin",     0x5000, 0x0800, 0xa3164b18 )
	ROM_LOAD( "rc12.bin",     0x5800, 0x0800, 0x2052f6d9 )
ROM_END

ROM_START( boothill )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "romh.cpu",     0x0000, 0x0800, 0x1615d077 )
	ROM_LOAD( "romg.cpu",     0x0800, 0x0800, 0x65a90420 )
	ROM_LOAD( "romf.cpu",     0x1000, 0x0800, 0x3fdafd79 )
	ROM_LOAD( "rome.cpu",     0x1800, 0x0800, 0x374529f4 )
ROM_END

ROM_START( schaser )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "rt13.bin",     0x0000, 0x0400, 0x0dfbde68 )
	ROM_LOAD( "rt14.bin",     0x0400, 0x0400, 0x5a508a25 )
	ROM_LOAD( "rt15.bin",     0x0800, 0x0400, 0x2ac43a93 )
	ROM_LOAD( "rt16.bin",     0x0c00, 0x0400, 0xf5583afc )
	ROM_LOAD( "rt17.bin",     0x1000, 0x0400, 0x51cf1155 )
	ROM_LOAD( "rt18.bin",     0x1400, 0x0400, 0x3f0fc73a )
	ROM_LOAD( "rt19.bin",     0x1800, 0x0400, 0xb66ea369 )
	ROM_LOAD( "rt20.bin",     0x1c00, 0x0400, 0xe3a7466a )
	ROM_LOAD( "rt21.bin",     0x4000, 0x0400, 0xb368ac98 )
	ROM_LOAD( "rt22.bin",     0x4400, 0x0400, 0x6e060dfb )
ROM_END

ROM_START( spcenctr )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "4m33.cpu",     0x0000, 0x0800, 0x7458b2db )
	ROM_LOAD( "4m32.cpu",     0x0800, 0x0800, 0x1b873788 )
	ROM_LOAD( "4m31.cpu",     0x1000, 0x0800, 0xd4319c91 )
	ROM_LOAD( "4m30.cpu",     0x1800, 0x0800, 0x9b9a1a45 )
	ROM_LOAD( "4m29.cpu",     0x4000, 0x0800, 0x294d52ce )
	ROM_LOAD( "4m28.cpu",     0x4800, 0x0800, 0xce44c923 )
	ROM_LOAD( "4m27.cpu",     0x5000, 0x0800, 0x098070ab )
	ROM_LOAD( "4m26.cpu",     0x5800, 0x0800, 0x7f1d1f44 )
ROM_END

ROM_START( clowns )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "h2.cpu",       0x0000, 0x0400, 0xff4432eb )
	ROM_LOAD( "g2.cpu",       0x0400, 0x0400, 0x676c934b )
	ROM_LOAD( "f2.cpu",       0x0800, 0x0400, 0x00757962 )
	ROM_LOAD( "e2.cpu",       0x0c00, 0x0400, 0x9e506a36 )
	ROM_LOAD( "d2.cpu",       0x1000, 0x0400, 0xd61b5b47 )
	ROM_LOAD( "c2.cpu",       0x1400, 0x0400, 0x154d129a )
ROM_END

ROM_START( gmissile )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "gm_623.h",     0x0000, 0x0800, 0xa3ebb792 )
	ROM_LOAD( "gm_623.g",     0x0800, 0x0800, 0xa5e740bb )
	ROM_LOAD( "gm_623.f",     0x1000, 0x0800, 0xda381025 )
	ROM_LOAD( "gm_623.e",     0x1800, 0x0800, 0xf350146b )
ROM_END

ROM_START( seawolf )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "sw0041.h",     0x0000, 0x0400, 0x8f597323 )
	ROM_LOAD( "sw0042.g",     0x0400, 0x0400, 0xdb980974 )
	ROM_LOAD( "sw0043.f",     0x0800, 0x0400, 0xe6ffa008 )
	ROM_LOAD( "sw0044.e",     0x0c00, 0x0400, 0xc3557d6a )
ROM_END

ROM_START( gunfight )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "7609h.bin",    0x0000, 0x0400, 0x0b117d73 )
	ROM_LOAD( "7609g.bin",    0x0400, 0x0400, 0x57bc3159 )
	ROM_LOAD( "7609f.bin",    0x0800, 0x0400, 0x8049a6bd )
	ROM_LOAD( "7609e.bin",    0x0c00, 0x0400, 0x773264e2 )
ROM_END

ROM_START( 280zzzap )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "zzzaph",       0x0000, 0x0400, 0x1fa86e1c )
	ROM_LOAD( "zzzapg",       0x0400, 0x0400, 0x9639bc6b )
	ROM_LOAD( "zzzapf",       0x0800, 0x0400, 0xadc6ede1 )
	ROM_LOAD( "zzzape",       0x0c00, 0x0400, 0x472493d6 )
	ROM_LOAD( "zzzapd",       0x1000, 0x0400, 0x4c240ee1 )
	ROM_LOAD( "zzzapc",       0x1400, 0x0400, 0x6e85aeaf )
ROM_END

ROM_START( lupin3 )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "lp12.bin",     0x0000, 0x0800, 0x68a7f47a )
	ROM_LOAD( "lp13.bin",     0x0800, 0x0800, 0xcae9a17b )
	ROM_LOAD( "lp14.bin",     0x1000, 0x0800, 0x3553b9e4 )
	ROM_LOAD( "lp15.bin",     0x1800, 0x0800, 0xacbeef64 )
	ROM_LOAD( "lp16.bin",     0x4000, 0x0800, 0x19fcdc54 )
	ROM_LOAD( "lp17.bin",     0x4800, 0x0800, 0x66289ab2 )
	ROM_LOAD( "lp18.bin",     0x5000, 0x0800, 0x2f07b4ba )
ROM_END

ROM_START( polaris )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "ps-01",        0x0000, 0x0800, 0xc04ce5a9 )
	ROM_LOAD( "ps-09",        0x0800, 0x0800, 0x9a5c8cb2 )
	ROM_LOAD( "ps-08",        0x1000, 0x0800, 0x8680d7ea )
	ROM_LOAD( "ps-04",        0x1800, 0x0800, 0x65694948 )
	ROM_LOAD( "ps-05",        0x4000, 0x0800, 0x772e31f3 )
	ROM_LOAD( "ps-10",        0x4800, 0x0800, 0x3df77bac )

	ROM_REGION( 0x0800, REGION_PROMS )		/* color map */
	ROM_LOAD( "ps07",         0x0000, 0x0400, 0x164aa05d )
	ROM_RELOAD(  			  0x0400, 0x0400 )

	ROM_REGION( 0x0100, REGION_USER1 )		/* don't know what this is for */
	ROM_LOAD( "mb7052.2c",    0x0000, 0x0100, 0x2953253b )
ROM_END

ROM_START( polarisa )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "ps01-1",       0x0000, 0x0800, 0x7d41007c )
	ROM_LOAD( "ps-09",        0x0800, 0x0800, 0x9a5c8cb2 )
	ROM_LOAD( "ps03-1",       0x1000, 0x0800, 0x21f32415 )
	ROM_LOAD( "ps-04",        0x1800, 0x0800, 0x65694948 )
	ROM_LOAD( "ps-05",        0x4000, 0x0800, 0x772e31f3 )
	ROM_LOAD( "ps-10",        0x4800, 0x0800, 0x3df77bac )
	ROM_LOAD( "ps26",         0x5000, 0x0800, 0x9d5c3d50 )

	ROM_REGION( 0x0800, REGION_PROMS )		/* color map */
	ROM_LOAD( "ps07",         0x0000, 0x0400, 0x164aa05d )
	ROM_RELOAD(  			  0x0400, 0x0400 )

	ROM_REGION( 0x0100, REGION_USER1 )		/* don't know what this is for */
	ROM_LOAD( "mb7052.2c",    0x0000, 0x0100, 0x2953253b )
ROM_END

ROM_START( lagunar )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "lagunar.h",    0x0000, 0x0800, 0x0cd5a280 )
	ROM_LOAD( "lagunar.g",    0x0800, 0x0800, 0x824cd6f5 )
	ROM_LOAD( "lagunar.f",    0x1000, 0x0800, 0x62692ca7 )
	ROM_LOAD( "lagunar.e",    0x1800, 0x0800, 0x20e098ed )
ROM_END

ROM_START( m4 )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "m4.h",         0x0000, 0x0800, 0x9ee2a0b5 )
	ROM_LOAD( "m4.g",         0x0800, 0x0800, 0x0e84b9cb )
	ROM_LOAD( "m4.f",         0x1000, 0x0800, 0x9ded9956 )
	ROM_LOAD( "m4.e",         0x1800, 0x0800, 0xb6983238 )
ROM_END

ROM_START( phantom2 )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "phantom2.h",   0x0000, 0x0800, 0x0e3c2439 )
	ROM_LOAD( "phantom2.g",   0x0800, 0x0800, 0xe8df3e52 )
	ROM_LOAD( "phantom2.f",   0x1000, 0x0800, 0x30e83c6d )
	ROM_LOAD( "phantom2.e",   0x1800, 0x0800, 0x8c641cac )
ROM_END

ROM_START( dogpatch )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "dogpatch.h",   0x0000, 0x0800, 0x74ebdf4d )
	ROM_LOAD( "dogpatch.g",   0x0800, 0x0800, 0xac246f70 )
	ROM_LOAD( "dogpatch.f",   0x1000, 0x0800, 0xa975b011 )
	ROM_LOAD( "dogpatch.e",   0x1800, 0x0800, 0xc12b1f60 )
ROM_END

ROM_START( bowler )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "h.cpu",        0x0000, 0x0800, 0x74c29b93 )
	ROM_LOAD( "g.cpu",        0x0800, 0x0800, 0xca26d8b4 )
	ROM_LOAD( "f.cpu",        0x1000, 0x0800, 0xba8a0bfa )
	ROM_LOAD( "e.cpu",        0x1800, 0x0800, 0x4da65a40 )
	ROM_LOAD( "d.cpu",        0x4000, 0x0800, 0xe7dbc9d9 )
ROM_END

ROM_START( shuffle )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "shuffle.h",    0x0000, 0x0800, 0x0d422a18 )
	ROM_LOAD( "shuffle.g",    0x0800, 0x0800, 0x7db7fcf9 )
	ROM_LOAD( "shuffle.f",    0x1000, 0x0800, 0xcd04d848 )
	ROM_LOAD( "shuffle.e",    0x1800, 0x0800, 0x2c118357 )
ROM_END

ROM_START( blueshrk )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "blueshrk.h",   0x0000, 0x0800, 0x4ff94187 )
	ROM_LOAD( "blueshrk.g",   0x0800, 0x0800, 0xe49368fd )
	ROM_LOAD( "blueshrk.f",   0x1000, 0x0800, 0x86cca79d )
ROM_END

ROM_START( einnings )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "ei.h",         0x0000, 0x0800, 0xeff9c7af )
	ROM_LOAD( "ei.g",         0x0800, 0x0800, 0x5d1e66cb )
	ROM_LOAD( "ei.f",         0x1000, 0x0800, 0xed96785d )
	ROM_LOAD( "ei.e",         0x1800, 0x0800, 0xad096a5d )
	ROM_LOAD( "ei.b",         0x5000, 0x0800, 0x56b407d4 )
ROM_END

ROM_START( dplay )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "dplay619.h",   0x0000, 0x0800, 0x6680669b )
	ROM_LOAD( "dplay619.g",   0x0800, 0x0800, 0x0eec7e01 )
	ROM_LOAD( "dplay619.f",   0x1000, 0x0800, 0x3af4b719 )
	ROM_LOAD( "dplay619.e",   0x1800, 0x0800, 0x65cab4fc )
ROM_END

ROM_START( maze )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "invaders.h",   0x0000, 0x0800, 0xf2860cff )
	ROM_LOAD( "invaders.g",   0x0800, 0x0800, 0x65fad839 )
ROM_END

ROM_START( tornbase )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "tb.h",         0x0000, 0x0800, 0x653f4797 )
	ROM_LOAD( "tb.g",         0x0800, 0x0800, BADCRC(0x33468006) )	/* this ROM fails the test */
	ROM_LOAD( "tb.f",         0x1000, 0x0800, 0x215e070c )
ROM_END

ROM_START( checkmat )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "checkmat.h",   0x0000, 0x0400, 0x3481a6d1 )
	ROM_LOAD( "checkmat.g",   0x0400, 0x0400, 0xdf5fa551 )
	ROM_LOAD( "checkmat.f",   0x0800, 0x0400, 0x25586406 )
	ROM_LOAD( "checkmat.e",   0x0c00, 0x0400, 0x59330d84 )
ROM_END

ROM_START( desertgu )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "desertgu.h",   0x0000, 0x0800, 0xc0030d7c )
	ROM_LOAD( "desertgu.g",   0x0800, 0x0800, 0x1ddde10b )
	ROM_LOAD( "desertgu.f",   0x1000, 0x0800, 0x808e46f1 )
	ROM_LOAD( "desertgu.e",   0x1800, 0x0800, 0xac64dc62 )
ROM_END

ROM_START( ozmawars )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "mw01",         0x0000, 0x0800, 0x31f4397d )
	ROM_LOAD( "mw02",         0x0800, 0x0800, 0xd8e77c62 )
	ROM_LOAD( "mw03",         0x1000, 0x0800, 0x3bfa418f )
	ROM_LOAD( "mw04",         0x1800, 0x0800, 0xe190ce6c )
	ROM_LOAD( "mw05",         0x4000, 0x0800, 0x3bc7d4c7 )
	ROM_LOAD( "mw06",         0x4800, 0x0800, 0x99ca2eae )
ROM_END

ROM_START( solfight )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "solfight.m",   0x0000, 0x0800, 0xa4f2814e )
	ROM_LOAD( "solfight.n",   0x0800, 0x0800, 0x5657ec07 )
	ROM_LOAD( "solfight.p",   0x1000, 0x0800, 0xef9ce96d )
	ROM_LOAD( "solfight.r",   0x1800, 0x0800, 0x4f1ef540 )
	ROM_LOAD( "mw05",         0x4000, 0x0800, 0x3bc7d4c7 )
	ROM_LOAD( "solfight.t",   0x4800, 0x0800, 0x3b6fb206 )
ROM_END

ROM_START( spaceph )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "sv01.bin",     0x0000, 0x0400, 0xde84771d )
	ROM_LOAD( "sv02.bin",     0x0400, 0x0400, 0x957fc661 )
	ROM_LOAD( "sv03.bin",     0x0800, 0x0400, 0xdbda38b9 )
	ROM_LOAD( "sv04.bin",     0x0c00, 0x0400, 0xf51544a5 )
	ROM_LOAD( "sv05.bin",     0x1000, 0x0400, 0x98d02683 )
	ROM_LOAD( "sv06.bin",     0x1400, 0x0400, 0x4ec390fd )
	ROM_LOAD( "sv07.bin",     0x1800, 0x0400, 0x170862fd )
	ROM_LOAD( "sv08.bin",     0x1c00, 0x0400, 0x511b12cf )
	ROM_LOAD( "sv09.bin",     0x4000, 0x0400, 0xaf1cd1af )
	ROM_LOAD( "sv10.bin",     0x4400, 0x0400, 0x31b7692e )
	ROM_LOAD( "sv11.bin",     0x4800, 0x0400, 0x50257351 )
	ROM_LOAD( "sv12.bin",     0x4c00, 0x0400, 0xa2a3366a )
ROM_END

ROM_START( ballbomb )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "tn01",         0x0000, 0x0800, 0x551585b5 )
	ROM_LOAD( "tn02",         0x0800, 0x0800, 0x7e1f734f )
	ROM_LOAD( "tn03",         0x1000, 0x0800, 0xd93e20bc )
	ROM_LOAD( "tn04",         0x1800, 0x0800, 0xd0689a22 )
	ROM_LOAD( "tn05-1",       0x4000, 0x0800, 0x5d5e94f1 )

	ROM_REGION( 0x0800, REGION_PROMS )		/* color maps player 1/player 2 */
	ROM_LOAD( "tn06",         0x0000, 0x0400, 0x7ec554c4 )
	ROM_LOAD( "tn07",         0x0400, 0x0400, 0xdeb0ac82 )
ROM_END

ROM_START( yosakdon )
	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
	ROM_LOAD( "yd1.bin", 0x0000, 0x0400, 0x607899c9 )
	ROM_LOAD( "yd2.bin", 0x0400, 0x0400, 0x78336df4 )
	ROM_LOAD( "yd3.bin", 0x0800, 0x0400, 0xc5af6d52 )
	ROM_LOAD( "yd4.bin", 0x0c00, 0x0400, 0xdca8064f )
	ROM_LOAD( "yd5.bin", 0x1400, 0x0400, 0x38804ff1 )
	ROM_LOAD( "yd6.bin", 0x1800, 0x0400, 0x988d2362 )
	ROM_LOAD( "yd7.bin", 0x1c00, 0x0400, 0x2744e68b )
ROM_END

ROM_START( bandido )
	ROM_REGION( 0x10000, REGION_CPU1 )             /* 64k for code */
	ROM_LOAD( "baf1-3",       0x0000, 0x0400, 0xaec94829 )
	ROM_LOAD( "baf2-1",       0x0400, 0x0400, 0xda67721a )
	ROM_LOAD( "bag1-1",       0x0800, 0x0400, 0x3fb7888e )
	ROM_LOAD( "bag2-1",       0x0C00, 0x0400, 0x585fcfee )
	ROM_LOAD( "bah1-1",       0x1000, 0x0400, 0x5cb63677 )
	ROM_LOAD( "bah2-1",       0x1400, 0x0400, 0x79e69a6a )
	ROM_LOAD( "bai1-1",       0x1800, 0x0400, 0xdda7d1e8 )
	ROM_LOAD( "bai2-1",       0x1C00, 0x0400, 0x5c5f3f86 )
	ROM_LOAD( "baj1-1",       0x2000, 0x0400, 0x0aa8b79a )
	ROM_LOAD( "baj2-2",       0x2400, 0x0400, 0xa10b848a )

	ROM_REGION( 0x1000, REGION_CPU2 )	/* Sound 8035 + 76477 Sound Generator */
	ROM_LOAD( "basnd.u2",     0x0000, 0x0400, 0x75731745 )
ROM_END

ROM_START( helifire )
	ROM_REGION( 0x10000, REGION_CPU1 )             /* 64k for code */
	ROM_LOAD( "tub.f1b",      0x0000, 0x0400, 0x032f89ca )
	ROM_LOAD( "tub.f2b",      0x0400, 0x0400, 0x2774e70f )
	ROM_LOAD( "tub.g1b",      0x0800, 0x0400, 0xb5ad6e8a )
	ROM_LOAD( "tub.g2b",      0x0C00, 0x0400, 0x5e015bf4 )
	ROM_LOAD( "tub.h1b",      0x1000, 0x0400, 0x23bb4e5a )
	ROM_LOAD( "tub.h2b",      0x1400, 0x0400, 0x358227c6 )
	ROM_LOAD( "tub.i1b",      0x1800, 0x0400, 0x0c679f44 )
	ROM_LOAD( "tub.i2b",      0x1C00, 0x0400, 0xd8b7a398 )
	ROM_LOAD( "tub.j1b",      0x2000, 0x0400, 0x98ef24db )
	ROM_LOAD( "tub.j2b",      0x2400, 0x0400, 0x5e2b5877 )

	ROM_REGION( 0x1000, REGION_CPU2 )	/* Sound 8035 + 76477 Sound Generator */
	ROM_LOAD( "tub.snd",      0x0000, 0x0400, 0x9d77a31f )
ROM_END

ROM_START( helifira )
	ROM_REGION( 0x10000, REGION_CPU1 )             /* 64k for code */
	ROM_LOAD( "f1a.bin",      0x0000, 0x0400, 0x92c9d6c1 )
	ROM_LOAD( "f2a.bin",      0x0400, 0x0400, 0xa264dde8 )
	ROM_LOAD( "tub.g1b",      0x0800, 0x0400, 0xb5ad6e8a )
	ROM_LOAD( "g2a.bin",      0x0C00, 0x0400, 0xa987ebcd )
	ROM_LOAD( "h1a.bin",      0x1000, 0x0400, 0x25abcaf0 )
	ROM_LOAD( "tub.h2b",      0x1400, 0x0400, 0x358227c6 )
	ROM_LOAD( "tub.i1b",      0x1800, 0x0400, 0x0c679f44 )
	ROM_LOAD( "i2a.bin",      0x1C00, 0x0400, 0x296610fd )
	ROM_LOAD( "tub.j1b",      0x2000, 0x0400, 0x98ef24db )
	ROM_LOAD( "tub.j2b",      0x2400, 0x0400, 0x5e2b5877 )

	ROM_REGION( 0x1000, REGION_CPU2 )	/* Sound 8035 + 76477 Sound Generator */
	ROM_LOAD( "tub.snd",      0x0000, 0x0400, 0x9d77a31f )
ROM_END

ROM_START( spacefev )
	ROM_REGION( 0x10000, REGION_CPU1 )             /* 64k for code */
	ROM_LOAD( "tsf.f1",       0x0000, 0x0400, 0x35f295bd )
	ROM_LOAD( "tsf.f2",       0x0400, 0x0400, 0x0c633f4c )
	ROM_LOAD( "tsf.g1",       0x0800, 0x0400, 0xf3d851cb )
	ROM_LOAD( "tsf.g2",       0x0C00, 0x0400, 0x1faef63a )
	ROM_LOAD( "tsf.h1",       0x1000, 0x0400, 0xb365389d )
	ROM_LOAD( "tsf.h2",       0x1400, 0x0400, 0xa36c61c9 )
	ROM_LOAD( "tsf.i1",       0x1800, 0x0400, 0xd4f3b50d )

	ROM_REGION( 0x1000, REGION_CPU2 )	/* Sound 8035 + 76477 Sound Generator */
	ROM_LOAD( "basnd.u2",     0x0000, 0x0400, 0x75731745 )
ROM_END

ROM_START( sfeverbw )
	ROM_REGION( 0x10000, REGION_CPU1 )             /* 64k for code */
	ROM_LOAD( "spacefev.f1", 0x0000, 0x0400, 0xb8887351 )
	ROM_LOAD( "spacefev.f2", 0x0400, 0x0400, 0xcda933a7 )
	ROM_LOAD( "spacefev.g1", 0x0800, 0x0400, 0xde17578a )
	ROM_LOAD( "spacefev.g2", 0x0C00, 0x0400, 0xf1a90948 )
	ROM_LOAD( "spacefev.h1", 0x1000, 0x0400, 0xeefb4273 )
	ROM_LOAD( "spacefev.h2", 0x1400, 0x0400, 0xe91703e8 )
	ROM_LOAD( "spacefev.i1", 0x1800, 0x0400, 0x41e18df9 )
	ROM_LOAD( "spacefev.i2", 0x1c00, 0x0400, 0xeff9f82d )

	ROM_REGION( 0x1000, REGION_CPU2 )	/* Sound 8035 + 76477 Sound Generator */
	ROM_LOAD( "basnd.u2",    0x0000, 0x0400, 0x75731745 )
ROM_END



/* Midway games */

/* Note - "The Amazing Maze Game" on title screen, but manual, flyer,
           cabinet side art all call it just "Amazing Maze"
        - Desert Gun is also known as Road Runner

   To Do:
   		- 4 Player Bowling has an offscreen display that show how many points the player will be
   		  rewarded for hitting the dot in the Flash game.

   Games confirmed not use an overlay (pure black and white):
   		- 4 Player Bowling
*/

/* board #            rom       parent    machine   inp       init (overlay/color hardware setup) */

/* 596 */ GAME( 1976, seawolf,  0,        seawolf,  seawolf,  seawolf,  ROT0,   "Midway", "Sea Wolf" )
/* 597 */ GAME( 1975, gunfight, 0,        gunfight, gunfight, 8080bw,   ROT0,   "Midway", "Gun Fight" )
/* 605 */ GAME( 1976, tornbase, 0,        invaders, tornbase, 8080bw,	ROT0,   "Midway", "Tornado Baseball" )
/* 610 */ GAME( 1976, 280zzzap, 0,        280zzzap, 280zzzap, 8080bw,	ROT0,   "Midway", "Datsun 280 Zzzap" )
/* 611 */ GAME( 1976, maze,     0,        invaders, maze,     8080bw,	ROT0,   "Midway", "Amazing Maze" )
/* 612 */ GAME( 1977, boothill, 0,        boothill, boothill, 8080bw,   ROT0,   "Midway", "Boot Hill" )
/* 615 */ GAME( 1977, checkmat, 0,        checkmat, checkmat, 8080bw,	ROT0,   "Midway", "Checkmate" )
/* 618 */ GAME( 1977, desertgu, 0,        desertgu, desertgu, desertgu,	ROT0,   "Midway", "Desert Gun" )
/* 619 */ GAME( 1977, dplay,    einnings, gmissile, einnings, 8080bw,	ROT0,   "Midway", "Double Play" )
/* 622 */ GAME( 1977, lagunar,  0,        280zzzap, lagunar,  8080bw,   ROT90,  "Midway", "Laguna Racer" )
/* 623 */ GAME( 1977, gmissile, 0,        gmissile, gmissile, 8080bw,   ROT0,   "Midway", "Guided Missile" )
/* 626 */ GAME( 1977, m4,       0,        m4,       m4,       8080bw,   ROT0,   "Midway", "M-4" )
/* 630 */ GAME( 1978, clowns,   0,        clowns,   clowns,   8080bw,   ROT0,   "Midway", "Clowns" )
/* 640    																		"Midway", "Space Walk" */
/* 642 */ GAME( 1978, einnings, 0,        gmissile, einnings, 8080bw,	ROT0,   "Midway", "Extra Innings" )
/* 643 */ GAME( 1978, shuffle,  0,        shuffle,  shuffle,  8080bw,	ROT90,  "Midway", "Shuffleboard")
/* 644 */ GAME( 1977, dogpatch, 0,        clowns,   dogpatch, 8080bw,   ROT0,   "Midway", "Dog Patch" )
/* 645 */ GAME( 1980, spcenctr, 0,        spcenctr, spcenctr, 8080bw,	ROT0,   "Midway", "Space Encounters" )
/* 652 */ GAME( 1979, phantom2, 0,        m4,       phantom2, 8080bw,   ROT0,   "Midway", "Phantom II" )
/* 730 */ GAME( 1978, bowler,   0,        bowler,   bowler,   8080bw,	ROT90,  "Midway", "4 Player Bowling" )
/* 739 */ GAME( 1978, invaders, 0,        invaders, invaders, invaders, ROT270, "Midway", "Space Invaders" )
/* 742 */ GAME( 1978, blueshrk, 0,        blueshrk, blueshrk, blueshrk, ROT0,   "Midway", "Blue Shark" )
/* 851 */ GAME( 1980, invad2ct, 0,        invad2ct, invad2ct, invad2ct, ROT90,  "Midway", "Space Invaders II (Midway, cocktail)" )
/* 852 */ GAME( 1980, invdpt2m, invadpt2, invaders, invadpt2, invdpt2m, ROT270, "Midway", "Space Invaders Part II (Midway)" )
/* 870    																		"Midway", "Space Invaders Deluxe (cocktail) "*/

		  GAME( 1980, earthinv, invaders, invaders, earthinv, invaders, ROT270, "bootleg", "Super Earth Invasion" )
		  GAME( 1980, spaceatt, invaders, invaders, spaceatt, invaders, ROT270, "Zenitone Microsec", "Space Attack II" )
		  GAME( ????, sinvemag, invaders, invaders, sinvemag, invaders, ROT270, "bootleg", "Super Invaders" )
		  GAME( ????, alieninv, invaders, invaders, earthinv, invaders, ROT270, "bootleg", "Alien Invasion Part II" )
		  GAME( 1978, sitv,     invaders, invaders, sitv,     invaders, ROT270, "Taito", "Space Invaders (TV Version)" )
		  GAME( 1979, sicv,     invaders, invadpt2, invaders, invadpt2, ROT270, "Taito", "Space Invaders (CV Version)" )
		  GAME( 1978, sisv,     invaders, invadpt2, invaders, invadpt2, ROT270, "Taito", "Space Invaders (SV Version)" )
		  GAME( 1978, sisv2,    invaders, invadpt2, invaders, invadpt2, ROT270, "Taito", "Space Invaders (SV Version 2)" )
		  GAME( 1978, spceking, invaders, invaders, spceking, 8080bw,   ROT270, "Leijac (Konami)","Space King" )
		  GAME( 1978, spcewars, invaders, invaders, invadpt2, 8080bw,   ROT270, "Sanritsu", "Space War (Sanritsu)" )
		  GAME( 1978, spacewr3, invaders, invaders, spacewr3, invaders, ROT270, "bootleg", "Space War Part 3" )
		  GAME( 1978, invaderl, invaders, invaders, invaders, invaders, ROT270, "bootleg", "Space Invaders (Logitec)" )
		  GAME( 1979, jspecter, invaders, invaders, jspecter, invaders, ROT270, "Jatre", "Jatre Specter" )
		  GAME( 1979, cosmicmo, invaders, invaders, cosmicmo, invaders, ROT270, "Universal", "Cosmic Monsters" )
		  GAME( 1980, invadpt2, 0,        invadpt2, invadpt2, invadpt2, ROT270, "Taito", "Space Invaders Part II (Taito)" )
		  GAME( ????, invrvnge, 0,        invaders, invrvnge, invrvnge, ROT270, "Zenitone Microsec", "Invader's Revenge" )
		  GAME( ????, invrvnga, invrvnge, invaders, invrvnge, invrvnge, ROT270, "Zenitone Microsec (Dutchford license)", "Invader's Revenge (Dutchford)" )
		  GAME( 1980, spclaser, 0,        invaders, spclaser, invdpt2m, ROT270, "Game Plan, Inc. (Taito)", "Space Laser" )
		  GAME( 1980, laser,    spclaser, invaders, spclaser, invdpt2m, ROT270, "<unknown>", "Laser" )
		  GAME( 1979, spcewarl, spclaser, invaders, spclaser, invdpt2m, ROT270, "Leijac (Konami)","Space War (Leijac)" )
		  GAME( 1979, galxwars, 0,        invaders, galxwars, invaders, ROT270, "Taito", "Galaxy Wars" )
		  GAME( 1979, starw,    galxwars, invaders, galxwars, invaders, ROT270, "bootleg", "Star Wars" )
		  GAME( 1979, lrescue,  0,        invadpt2, lrescue,  invadpt2, ROT270, "Taito", "Lunar Rescue" )
		  GAME( 1979, grescue,  lrescue,  invadpt2, lrescue,  invadpt2, ROT270, "Taito (Universal license?)", "Galaxy Rescue" )
		  GAME( 1979, desterth, lrescue,  invadpt2, invrvnge, invadpt2, ROT270, "bootleg", "Destination Earth" )
		  GAME( 1979, rollingc, 0,        rollingc, rollingc, rollingc, ROT270, "Nichibutsu", "Rolling Crash / Moon Base" )
		  GAME( 1980, schaser,  0,        schaser,  schaser,  schaser,  ROT270, "Taito", "Space Chaser" )
		  GAME( 1980, lupin3,   0,        invaders, lupin3,   8080bw,   ROT270, "Taito", "Lupin III" )
		  GAME( 1980, polaris,  0,        polaris,  polaris,  invadpt2, ROT270, "Taito", "Polaris (set 1)" )
		  GAME( 1980, polarisa, polaris,  polaris,  polaris,  invadpt2, ROT270, "Taito", "Polaris (set 2)" )
		  GAME( 1979, ozmawars, 0,        invaders, ozmawars, 8080bw,   ROT270, "SNK", "Ozma Wars" )
		  GAME( 1979, solfight, ozmawars, invaders, ozmawars, 8080bw,   ROT270, "bootleg", "Solar Fight" )
		  GAME( 1979, spaceph,  ozmawars, invaders, spaceph,  8080bw,   ROT270, "Zilec Games", "Space Phantoms" )
		  GAME( 1980, ballbomb, 0,        invadpt2, ballbomb, invadpt2, ROT270, "Taito", "Balloon Bomber" )
		  GAME( 1979, yosakdon, 0,        invaders, lrescue,  8080bw,   ROT270, "bootleg", "Yosaku To Donbee (bootleg)" )
		  GAMEX(1980, bandido,  0,        bandido,  bandido,  8080bw,	ROT270, "Exidy", "Bandido", GAME_IMPERFECT_SOUND )
		  GAME( 1980, helifire, 0,        bandido,  helifire, 8080bw,	ROT270, "Nintendo", "HeliFire (revision B)" )
		  GAME( 1980, helifira, helifire, bandido,  helifire, 8080bw,	ROT270, "Nintendo", "HeliFire (revision A)" )
		  GAME( 1980, spacefev, 0,        bandido,  spacefev, 8080bw,	ROT270, "Nintendo", "Space Fever (color)" )
		  GAME( 1980, sfeverbw, 0,        bandido,  spacefev, 8080bw,	ROT270, "Nintendo", "Space Fever (black and white)" )
