/***************************************************************************

The FairyLand Story

notes:
- The 68705 doesn't pass the startup checksum test (causing "BAD HW"). This
  is patched by changing one unused ROM byte to make it calculate the checksum
  the main cpu expects. Possibilites:
  1) The cpu<->mcu interface is wrong
  2) the 68705 dump is bad
  3) the 68705 program is for a different version of the main program
- anyway, the 68705 isn't returning the expected data and the program hangs
  when you start a game.
- sound communication not understood
- sound section also has a MSM5232

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"
#include "cpu/z80/z80.h"

void flstory_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh);

int flstory_68705_portA_r(int offset);
void flstory_68705_portA_w(int offset,int data);
int flstory_68705_portB_r(int offset);
void flstory_68705_portB_w(int offset,int data);
int flstory_68705_portC_r(int offset);
void flstory_68705_portC_w(int offset,int data);
void flstory_68705_ddrA_w(int offset,int data);
void flstory_68705_ddrB_w(int offset,int data);
void flstory_68705_ddrC_w(int offset,int data);
void flstory_mcu_w(int offset,int data);
int flstory_mcu_r(int offset);
int flstory_mcu_status_r(int offset);


int d400 = 0;

static void sound_command_w(int offset, int data)
{
	d400 = data;
	soundlatch_w(0,data);
	cpu_cause_interrupt(1,Z80_NMI_INT);
}

int read_d401(int offset)
{
/* writing $ef or $06 to $d400 makes bit 0 of d401 go hi */
	/* it looks like writing $ef to $d400 is meant to eventually
	   cause bit 1 of $D401 to go low */

	/* it looks like writing $14 to $d400 is meant to eventually
	   cause bit 1 of $D401 to go high */

	/* 0213 needs d401 to have bit 1 set, or 'BAD SOUND' */

	if (d400 == 0xef)
		return 0x01;
	else
		return 0x03;
}



static struct MemoryReadAddress readmem[] =
{
    { 0x0000, 0xbfff, MRA_ROM },
    { 0xc000, 0xc7ff, MRA_RAM },
	{ 0xd000, 0xd000, flstory_mcu_r },
//	{ 0xd400, 0xd400, MRA_RAM },
	{ 0xd401, 0xd401, read_d401 },
	{ 0xd800, 0xd800, input_port_0_r },
	{ 0xd801, 0xd801, input_port_1_r },
	{ 0xd802, 0xd802, input_port_2_r },
	{ 0xd803, 0xd803, input_port_3_r },
	{ 0xd804, 0xd804, input_port_4_r },
	{ 0xd805, 0xd805, flstory_mcu_status_r },
	{ 0xd806, 0xd806, input_port_5_r },
    { 0xe000, 0xe7ff, MRA_RAM },
    { -1 }  /* end of table */
};

static struct MemoryWriteAddress writemem[] =
{
	{ 0x0000, 0xbfff, MWA_ROM },
	{ 0xc000, 0xc7ff, videoram_w, &videoram, &videoram_size },
	{ 0xd000, 0xd000, flstory_mcu_w },
	{ 0xd001, 0xd001, MWA_RAM },	/* ??? */
	{ 0xd002, 0xd002, MWA_RAM },	/* ??? */
	{ 0xd400, 0xd400, sound_command_w },
//	{ 0xda00, 0xda00, MWA_RAM },
	{ 0xdc00, 0xdc7f, MWA_RAM, &spriteram, &spriteram_size },
	{ 0xdd00, 0xdeff, MWA_RAM },	/* palette? */
	{ 0xdf03, 0xdf03, MWA_RAM },	/* ??? */
	{ 0xe000, 0xe7ff, MWA_RAM },	/* work RAM */
	{ -1 }  /* end of table */
};

static struct MemoryReadAddress sound_readmem[] =
{
    { 0x0000, 0x3fff, MRA_ROM },
	{ 0xc000, 0xc7ff, MRA_RAM },
    { 0xd800, 0xd800, soundlatch_r },
    { 0xe000, 0xefff, MRA_ROM },	/* space for diagnostics ROM */
    { -1 }  /* end of table */
};

static struct MemoryWriteAddress sound_writemem[] =
{
	{ 0x0000, 0x3fff, MWA_ROM },
	{ 0xc000, 0xc7ff, MWA_RAM },
	{ 0xc800, 0xc800, AY8910_control_port_0_w },
	{ 0xc801, 0xc801, AY8910_write_port_0_w },
	{ 0xca00, 0xca0d, MWA_RAM },	/* MSM5232 registers */
	{ 0xcc00, 0xcc00, MWA_RAM },	/* MSM5232 port 0 */
	{ 0xce00, 0xce00, MWA_RAM },	/* MSM5232 port 1 */
	{ 0xda00, 0xda00, MWA_RAM },	/* ??? */
	{ 0xdc00, 0xdc00, MWA_RAM },	/* watchdog ??? */
	{ 0xe000, 0xe000, MWA_ROM },
	{ -1 }  /* end of table */
};

static struct MemoryReadAddress m68705_readmem[] =
{
	{ 0x0000, 0x0000, flstory_68705_portA_r },
	{ 0x0001, 0x0001, flstory_68705_portB_r },
	{ 0x0002, 0x0002, flstory_68705_portC_r },
	{ 0x0010, 0x007f, MRA_RAM },
	{ 0x0080, 0x07ff, MRA_ROM },
	{ -1 }	/* end of table */
};

static struct MemoryWriteAddress m68705_writemem[] =
{
	{ 0x0000, 0x0000, flstory_68705_portA_w },
	{ 0x0001, 0x0001, flstory_68705_portB_w },
	{ 0x0002, 0x0002, flstory_68705_portC_w },
	{ 0x0004, 0x0004, flstory_68705_ddrA_w },
	{ 0x0005, 0x0005, flstory_68705_ddrB_w },
	{ 0x0006, 0x0006, flstory_68705_ddrC_w },
	{ 0x0010, 0x007f, MWA_RAM },
	{ 0x0080, 0x07ff, MWA_ROM },
	{ -1 }	/* end of table */
};



INPUT_PORTS_START( flstory )
	PORT_START      /* D800: DSW0 */
	PORT_DIPNAME(0x03, 0x00, DEF_STR( Bonus_Life ) )
	PORT_DIPSETTING(   0x00, "30000 100000" )
	PORT_DIPSETTING(   0x01, "30000 150000" )
	PORT_DIPSETTING(   0x02, "50000 150000" )
	PORT_DIPSETTING(   0x03, "70000 150000" )
	PORT_DIPNAME(0x04, 0x04, DEF_STR( Free_Play ) )
	PORT_DIPSETTING(   0x04, DEF_STR( Off ) )
	PORT_DIPSETTING(   0x00, DEF_STR( On ) )
	PORT_DIPNAME(0x18, 0x08, "??? (046c)" )
	PORT_DIPSETTING(   0x00, "ENDLESS?" )
	PORT_DIPSETTING(   0x08, "3" )
	PORT_DIPSETTING(   0x10, "4" )
	PORT_DIPSETTING(   0x18, "5" )
	PORT_DIPNAME(0x20, 0x20, "??? (0683)" )
	PORT_DIPSETTING(   0x20, DEF_STR( Off ) )
	PORT_DIPSETTING(   0x00, DEF_STR( On ) )
	PORT_DIPNAME(0x40, 0x40, DEF_STR( Unknown ) )	/* flip screen? */
	PORT_DIPSETTING(   0x40, DEF_STR( Off ) )
	PORT_DIPSETTING(   0x00, DEF_STR( On ) )
	PORT_DIPNAME(0x80, 0x80, DEF_STR( Unknown ) )	/* cabinet? */
	PORT_DIPSETTING(   0x80, DEF_STR( Off ) )
	PORT_DIPSETTING(   0x00, DEF_STR( On ) )

	PORT_START      /* D801: DSW1 */
	PORT_DIPNAME(0x0f, 0x00, DEF_STR( Coin_A ) )
	PORT_DIPSETTING(   0x0f, DEF_STR( 9C_1C ) )
	PORT_DIPSETTING(   0x0e, DEF_STR( 8C_1C ) )
	PORT_DIPSETTING(   0x0d, DEF_STR( 7C_1C ) )
	PORT_DIPSETTING(   0x0c, DEF_STR( 6C_1C ) )
	PORT_DIPSETTING(   0x0b, DEF_STR( 5C_1C ) )
	PORT_DIPSETTING(   0x0a, DEF_STR( 4C_1C ) )
	PORT_DIPSETTING(   0x09, DEF_STR( 3C_1C ) )
	PORT_DIPSETTING(   0x08, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(   0x00, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING(   0x01, DEF_STR( 1C_2C ) )
	PORT_DIPSETTING(   0x02, DEF_STR( 1C_3C ) )
	PORT_DIPSETTING(   0x03, DEF_STR( 1C_4C ) )
	PORT_DIPSETTING(   0x04, DEF_STR( 1C_5C ) )
	PORT_DIPSETTING(   0x05, DEF_STR( 1C_6C ) )
	PORT_DIPSETTING(   0x06, DEF_STR( 1C_7C ) )
	PORT_DIPSETTING(   0x07, DEF_STR( 1C_8C ) )
	PORT_DIPNAME(0xf0, 0x00, DEF_STR( Coin_B ) )
	PORT_DIPSETTING(   0xf0, DEF_STR( 9C_1C ) )
	PORT_DIPSETTING(   0xe0, DEF_STR( 8C_1C ) )
	PORT_DIPSETTING(   0xd0, DEF_STR( 7C_1C ) )
	PORT_DIPSETTING(   0xc0, DEF_STR( 6C_1C ) )
	PORT_DIPSETTING(   0xb0, DEF_STR( 5C_1C ) )
	PORT_DIPSETTING(   0xa0, DEF_STR( 4C_1C ) )
	PORT_DIPSETTING(   0x90, DEF_STR( 3C_1C ) )
	PORT_DIPSETTING(   0x80, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(   0x00, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING(   0x10, DEF_STR( 1C_2C ) )
	PORT_DIPSETTING(   0x20, DEF_STR( 1C_3C ) )
	PORT_DIPSETTING(   0x30, DEF_STR( 1C_4C ) )
	PORT_DIPSETTING(   0x40, DEF_STR( 1C_5C ) )
	PORT_DIPSETTING(   0x50, DEF_STR( 1C_6C ) )
	PORT_DIPSETTING(   0x60, DEF_STR( 1C_7C ) )
	PORT_DIPSETTING(   0x70, DEF_STR( 1C_8C ) )

	PORT_START      /* D802: COINS */
	PORT_DIPNAME(0x01, 0x01, DEF_STR( Unknown ) )
	PORT_DIPSETTING(   0x01, DEF_STR( Off ) )
	PORT_DIPSETTING(   0x00, DEF_STR( On ) )
	PORT_DIPNAME(0x02, 0x02, DEF_STR( Unknown ) )
	PORT_DIPSETTING(   0x02, DEF_STR( Off ) )
	PORT_DIPSETTING(   0x00, DEF_STR( On ) )
	PORT_DIPNAME(0x04, 0x04, DEF_STR( Unknown ) )
	PORT_DIPSETTING(   0x04, DEF_STR( Off ) )
	PORT_DIPSETTING(   0x00, DEF_STR( On ) )
	PORT_DIPNAME(0x08, 0x08, DEF_STR( Unknown ) )
	PORT_DIPSETTING(   0x08, DEF_STR( Off ) )
	PORT_DIPSETTING(   0x00, DEF_STR( On ) )
	PORT_DIPNAME(0x10, 0x10, "Attract Animation" )
	PORT_DIPSETTING(   0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(   0x10, DEF_STR( On ) )
	PORT_DIPNAME(0x20, 0x20, DEF_STR( Unknown ) )
	PORT_DIPSETTING(   0x20, DEF_STR( Off ) )
	PORT_DIPSETTING(   0x00, DEF_STR( On ) )
	PORT_BITX(    0x40, 0x40, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability?", IP_KEY_NONE, IP_JOY_NONE )
	PORT_DIPSETTING(    0x40, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x80, 0x80, DEF_STR( Coinage ) )
	PORT_DIPSETTING(    0x80, "A and B" )
	PORT_DIPSETTING(    0x00, "A only" )

	PORT_START      /* D803: START BUTTONS */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_TILT )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN1 )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_COIN2 )
	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )	/* "BAD IO" if low */
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )	/* "BAD IO" if low */

	PORT_START      /* D804: P1? */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )

	PORT_START      /* D806: P2? */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )
INPUT_PORTS_END



static struct GfxLayout charlayout =
{
	8,8,
	2048,
	4,
	{ 0, 4, 2048*16*8+0, 2048*16*8+4 },
	{ 3, 2, 1, 0, 8+3, 8+2, 8+1, 8+0 },
	{ 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
	16*8
};

static struct GfxLayout spritelayout =
{
	16,16,
	512,
	4,
	{ 0, 4, 512*64*8+0, 512*64*8+4 },
	{ 3, 2, 1, 0, 8+3, 8+2, 8+1, 8+0,
			16*8+3, 16*8+2, 16*8+1, 16*8+0, 16*8+8+3, 16*8+8+2, 16*8+8+1, 16*8+8+0 },
	{ 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16,
			16*16, 17*16, 18*16, 19*16, 20*16, 21*16, 22*16, 23*16 },
	64*8
};

static struct GfxDecodeInfo gfxdecodeinfo[] =
{
	{ REGION_GFX1, 0, &charlayout,   0, 16 },
	{ REGION_GFX2, 0, &spritelayout, 0, 16 },
	{ -1 }	/* end of array */
};



static struct AY8910interface ay8910_interface =
{
	1,	/* 1 chip */
	1500000,	/* ??? */
	{ 50 },
	{ 0 },
	{ 0 },
	{ 0 },
	{ 0 }
};



static struct MachineDriver machine_driver_flstory =
{
    /* basic machine hardware */
    {
		{
			CPU_Z80,
			4000000,		/* ??? */
			readmem,writemem,0,0,
			interrupt,1
		},
		{
			CPU_Z80 | CPU_AUDIO_CPU,
			4000000,		/* ??? */
			sound_readmem,sound_writemem,0,0,
			interrupt,1	/* IRQ generated by ??? */
						/* NMI generated by the main CPU */
		},
		{
			CPU_M68705,
			4000000/2,	/* ??? */
			m68705_readmem,m68705_writemem,0,0,
			ignore_interrupt,0
		}
    },
	60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
	100,	/* 100 CPU slices per frame - an high value to ensure proper */
			/* synchronization of the CPUs */
    0,

    /* video hardware */
    32*8, 32*8, { 0*8, 32*8-1, 2*8, 30*8-1 },
    gfxdecodeinfo,
    256, 256,
    0,

	VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
    0,
	generic_vh_start,
	generic_vh_stop,
	flstory_vh_screenrefresh,

	/* sound hardware */
	0,0,0,0,
	{
		{
			SOUND_AY8910,
			&ay8910_interface
		}
	}
};



/***************************************************************************

  Game driver(s)

***************************************************************************/

ROM_START( flstory )
    ROM_REGION( 0x10000, REGION_CPU1 )	/* 64k for the first CPU */
    ROM_LOAD( "cpu-a45.15", 0x00000, 0x4000, 0xf03fc969 )
    ROM_LOAD( "cpu-a45.16", 0x04000, 0x4000, 0x311aa82e )
    ROM_LOAD( "cpu-a45.17", 0x08000, 0x4000, 0xa2b5d17d )

    ROM_REGION( 0x10000, REGION_CPU2 )	/* 64k for the second CPU */
    ROM_LOAD( "snd.22",     0x0000, 0x2000, 0xd58b201d )
    ROM_LOAD( "snd.23",     0x2000, 0x2000, 0x25e7fd9d )

	ROM_REGION( 0x0800, REGION_CPU3 )	/* 2k for the microcontroller */
	ROM_LOAD( "p5sa54-.09", 0x0000, 0x0800, 0x0e8b8846 )

    ROM_REGION( 0x10000, REGION_GFX1 | REGIONFLAG_DISPOSE )
    ROM_LOAD( "vid-a45.06", 0x00000, 0x4000, 0xdc856a75 )
    ROM_LOAD( "vid-a45.20", 0x04000, 0x4000, 0x1b0edf34 )
    ROM_LOAD( "vid-a45.07", 0x08000, 0x4000, 0xaa4b0762 )
    ROM_LOAD( "vid-a45.21", 0x0c000, 0x4000, 0xfc382bd1 )

    ROM_REGION( 0x10000, REGION_GFX2 | REGIONFLAG_DISPOSE )
    ROM_LOAD( "vid-a45.18", 0x00000, 0x4000, 0x6f08f69e )
    ROM_LOAD( "vid-a45.08", 0x04000, 0x4000, 0xd0b028ca )
    ROM_LOAD( "vid-a45.19", 0x08000, 0x4000, 0x2b572dc9 )
    ROM_LOAD( "vid-a45.09", 0x0c000, 0x4000, 0x8336be58 )
ROM_END

ROM_START( flstoryj )
    ROM_REGION( 0x10000, REGION_CPU1 )	/* 64k for the first CPU */
    ROM_LOAD( "cpu-a45.15", 0x00000, 0x4000, 0xf03fc969 )
    ROM_LOAD( "cpu-a45.16", 0x04000, 0x4000, 0x311aa82e )
    ROM_LOAD( "cpu-a45.17", 0x08000, 0x4000, 0xa2b5d17d )

    ROM_REGION( 0x10000, REGION_CPU2 )	/* 64k for the second CPU */
    ROM_LOAD( "a45_12.8",    0x0000, 0x2000, 0xd6f593fb )
    ROM_LOAD( "a45_13.9",    0x2000, 0x2000, 0x451f92f9 )

	ROM_REGION( 0x0800, REGION_CPU3 )	/* 2k for the microcontroller */
	ROM_LOAD( "p5sa54-.09", 0x0000, 0x0800, 0x0e8b8846 )	/* from the english version - might be wrong */

    ROM_REGION( 0x10000, REGION_GFX1 | REGIONFLAG_DISPOSE )
    ROM_LOAD( "vid-a45.06", 0x00000, 0x4000, 0xdc856a75 )
    ROM_LOAD( "vid-a45.20", 0x04000, 0x4000, 0x1b0edf34 )
    ROM_LOAD( "vid-a45.07", 0x08000, 0x4000, 0xaa4b0762 )
    ROM_LOAD( "vid-a45.21", 0x0c000, 0x4000, 0xfc382bd1 )

    ROM_REGION( 0x10000, REGION_GFX2 | REGIONFLAG_DISPOSE )
    ROM_LOAD( "vid-a45.18", 0x00000, 0x4000, 0x6f08f69e )
    ROM_LOAD( "vid-a45.08", 0x04000, 0x4000, 0xd0b028ca )
    ROM_LOAD( "vid-a45.19", 0x08000, 0x4000, 0x2b572dc9 )
    ROM_LOAD( "vid-a45.09", 0x0c000, 0x4000, 0x8336be58 )
ROM_END



static void init_flstory(void)
{
	/* patch one 68705 unused ROM location to make it pass the startup */
	/* checksum test. It is supposed to return 0xf9, without this patch */
	/* it would return 0x89 */
	memory_region(REGION_CPU3)[0x2c0] = 0x6f;
}



GAMEX( 1985, flstory,  0,       flstory, flstory, flstory, ROT0, "Taito", "The FairyLand Story", GAME_NOT_WORKING )
GAMEX( 1985, flstoryj, flstory, flstory, flstory, flstory, ROT0, "Taito", "The FairyLand Story (Japan)", GAME_NOT_WORKING )
