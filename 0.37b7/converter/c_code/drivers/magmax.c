/***************************************************************************

MAGMAX
(c)1985 NihonBussan Co.,Ltd.

Driver by Takahiro Nogi (nogi@kt.rim.or.jp) 1999/11/05 -
Additional tweaking by Jarek Burczynski

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"
#include "cpu/m68000/m68000.h"

void magmax_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable, const unsigned char *color_prom);
void magmax_vh_screenrefresh(struct osd_bitmap *bitmap, int full_refresh);
int magmax_vh_start(void);
void magmax_vh_stop(void);

extern unsigned short magmax_vreg;
extern unsigned char magmax_scroll_x[];
extern unsigned char magmax_scroll_y[];


static void * scanline_timer;

static unsigned char sound_latch = 0;
static unsigned char LS74_clr = 0;
static unsigned char LS74_q   = 0;

static WRITE_HANDLER( magmax_sound_w )
{
	sound_latch = data << 1;
	cpu_set_irq_line(1, 0, ASSERT_LINE);
}

static READ_HANDLER( magmax_sound_irq_ack )
{
	cpu_set_irq_line(1, 0, CLEAR_LINE);
	return 0;
}

static READ_HANDLER( magmax_sound_r )
{
	return (sound_latch | LS74_q);
}

WRITE_HANDLER( ay8910_portB_0_w )
{
	/*bit 0 is input to CLR line of the LS74*/
	LS74_clr = data & 1;
	if (LS74_clr == 0)
		LS74_q = 0;
}
static void scanline_callback(int scanline)
{
	/* bit 0 goes hi whenever line V6 from video part goes lo->hi */
	/* that is when scanline is 64 and 192 accordingly */
	if (LS74_clr != 0)
		LS74_q = 1;

	scanline += 128;
	scanline &= 255;

	scanline_timer = timer_set( cpu_getscanlinetime( scanline ), scanline, scanline_callback );
}

static void init_machine(void)
{
	scanline_timer = timer_set(cpu_getscanlinetime( 64 ), 64, scanline_callback );
}



WRITE_HANDLER( ay8910_portA_0_w )
{
/*There are three AY8910 chips and four(!) separate amplifiers on the board
* Each of AY channels is hardware mapped in following order:
* amplifier 0 <- AY0 CHA
* amplifier 1 <- AY0 CHB + AY0 CHC + AY1 CHA + AY1 CHB
* amplifier 2 <- AY1 CHC + AY2 CHA
* amplifier 3 <- AY2 CHB + AY2 CHC
*
* Each of the amps has its own analog cuircit:
* amp0, amp1 and amp2 are different from each other amp3 is the same as amp2
*
* Outputs of those amps are inputs to post amps, each having own cuircit
* that is partially controlled by AY #0 port A.
* PORT A BIT 0 - control postamp 0
* PORT A BIT 1 - control postamp 1
* PORT A BIT 2 - control postamp 2
* PORT A BIT 3 - control postamp 3
*
* The "control" means assert/clear input pins on chip called 4066 (it is analog switch)
* This is NOT implemented here.
*/


		//missing implementation

}



static WRITE_HANDLER( magmax_write )
{
	switch (offset)
	{
		case 0x00: /* VRAM CONTROL REGISTER */
			// bit0 - coin counter 1
			// bit1 - coin counter 2
			// bit2 - flip screen (INV)
			// bit3 - page bank to be displayed (PG)
			// bit4 - sprite bank LSB (DP0)
			// bit5 - sprite bank MSB (DP1)
			// bit6 - BG display enable (BE)
			magmax_vreg = COMBINE_WORD(magmax_vreg, data);
			break;

		case 0x02: /* SCROLL X */
			COMBINE_WORD_MEM(magmax_scroll_x, data);
			break;

		case 0x04: /* SCROLL Y */
			COMBINE_WORD_MEM(magmax_scroll_y, data);
			break;

		case 0x0c: /* SOUND COMMAND */
			magmax_sound_w(0, data);
			break;

		case 0x0e: /* irq acknowledge */
			break;

		default:
			logerror("offs:0x%02X data:0x%08X\n", offset, data);
			break;
	}
}

static READ_HANDLER( magmax_read )
{
	switch (offset)
	{
		case 0: /* PLAYER CONTROL 1 */
			return readinputport(0);

		case 2: /* PLAYER CONTROL 2 */
			return readinputport(1);

		case 4: /* START BUTTONS, COINS, TEST */
			return readinputport(2);

		case 6: /* DIPSWITCH A and B */
			return ((readinputport(4) << 8) | readinputport(3));
	}

	return 0xffff;
}


static struct MemoryReadAddress magmax_readmem[] =
{
	{ 0x000000, 0x013fff, MRA_ROM },
	{ 0x018000, 0x018fff, MRA_BANK1 },
	{ 0x020000, 0x0207ff, MRA_BANK2 },
	{ 0x028000, 0x0281ff, MRA_BANK3 },
	{ 0x030000, 0x03000f, magmax_read },
	{ -1 }
};

static struct MemoryWriteAddress magmax_writemem[] =
{
	{ 0x000000, 0x013fff, MWA_ROM },
	{ 0x018000, 0x018fff, MWA_BANK1 },
	{ 0x020000, 0x0207ff, MWA_BANK2, &videoram, &videoram_size },
	{ 0x028000, 0x0281ff, MWA_BANK3, &spriteram, &spriteram_size },
	{ 0x030010, 0x03001f, magmax_write },
	{ -1 }
};

static struct MemoryReadAddress magmax_soundreadmem[] =
{
	{ 0x0000, 0x3fff, MRA_ROM },
	{ 0x4000, 0x4000, magmax_sound_irq_ack },
	{ 0x6000, 0x67ff, MRA_RAM },
	{ -1 }
};

static struct MemoryWriteAddress magmax_soundwritemem[] =
{
	{ 0x0000, 0x3fff, MWA_ROM },
	{ 0x6000, 0x67ff, MWA_RAM },
	{ -1 }
};

static struct IOReadPort magmax_soundreadport[] =
{
	{ 0x06, 0x06, magmax_sound_r },
	{ -1 }
};

static struct IOWritePort magmax_soundwriteport[] =
{
	{ 0x00, 0x00, AY8910_control_port_0_w },
	{ 0x01, 0x01, AY8910_write_port_0_w },
	{ 0x02, 0x02, AY8910_control_port_1_w },
	{ 0x03, 0x03, AY8910_write_port_1_w },
	{ 0x04, 0x04, AY8910_control_port_2_w },
	{ 0x05, 0x05, AY8910_write_port_2_w },
	{ -1 }
};


INPUT_PORTS_START( magmax )
	PORT_START	/* Player 1 controls */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )

	PORT_START	/* Player 2 controls */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL  )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )

	PORT_START	/* Coin, Start, Test, Dipswitch */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_SERVICE1 )
	PORT_SERVICE( 0x20, IP_ACTIVE_LOW )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )

	PORT_START	/* Dipswitch 1 */
	PORT_DIPNAME( 0x03, 0x03, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x03, "3" )
	PORT_DIPSETTING(    0x02, "4" )
	PORT_DIPSETTING(    0x01, "5" )
	PORT_DIPSETTING(    0x00, "6" )
	PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( Bonus_Life ) )
	PORT_DIPSETTING(    0x0c, "30000 every" )
	PORT_DIPSETTING(    0x04, "70000 every" )
	PORT_DIPSETTING(    0x08, "50000 every" )
	PORT_DIPSETTING(    0x00, "90000 every" )
	PORT_DIPNAME( 0x10, 0x10, DEF_STR( Demo_Sounds ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x10, DEF_STR( On ) )
	PORT_DIPNAME( 0x20, 0x00, DEF_STR( Cabinet ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Upright ) )
	PORT_DIPSETTING(    0x20, DEF_STR( Cocktail ) )
	PORT_DIPNAME( 0x40, 0x40, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x40, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x80, 0x80, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x80, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )

	PORT_START	/* Dipswitch 2 */
	PORT_DIPNAME( 0x03, 0x03, DEF_STR( Coin_A ) )
	PORT_DIPSETTING(    0x01, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0x03, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING(    0x02, DEF_STR( 1C_2C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Free_Play ) )
	PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( Coin_B ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 3C_1C ) )
	PORT_DIPSETTING(    0x04, DEF_STR( 2C_3C ) )
	PORT_DIPSETTING(    0x0c, DEF_STR( 1C_3C ) )
	PORT_DIPSETTING(    0x08, DEF_STR( 1C_6C ) )
	PORT_DIPNAME( 0x10, 0x10, DEF_STR( Difficulty ) )
	PORT_DIPSETTING(    0x10, "Easy" )
	PORT_DIPSETTING(    0x00, "Hard" )
	PORT_DIPNAME( 0x20, 0x20, DEF_STR( Flip_Screen ) )
	PORT_DIPSETTING(    0x20, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x40, 0x40, DEF_STR( Unknown) )
	PORT_DIPSETTING(    0x40, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x80, 0x80, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x80, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
INPUT_PORTS_END


static struct GfxLayout charlayout =
{
	8, 8,	/* 8*8 characters */
	256,	/* 256 characters */
	4,	/* 4 bits per pixel */
	{ 0, 1, 2, 3 },
	{ 4, 0, 12, 8, 20, 16, 28, 24 },
	{ 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
	32*8
};

static struct GfxLayout spritelayout =
{
	16, 16,	/* 16*16 characters */
	512,	/* 512 characters */
	4,	/* 4 bits per pixel */
	{ 0, 1, 2, 3 },
	{ 4, 0, 4+512*64*8, 0+512*64*8, 12, 8, 12+512*64*8, 8+512*64*8,
	  20, 16, 20+512*64*8, 16+512*64*8, 28, 24, 28+512*64*8, 24+512*64*8 },
	{ 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32,
		8*32, 9*32, 10*32, 11*32, 12*32, 13*32, 14*32, 15*32 },
	64*8
};

static struct GfxLayout tilelayout =
{
	8, 8,	/* 8*8 characters */
	1024,	/* 1024 characters */
	4,	/* 4 bits per pixel */
	{ 0, 1, 2, 3 },
	{ 4, 0, 12, 8, 20, 16, 28, 24 },
	{ 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
	32*8
};

static struct GfxDecodeInfo gfxdecodeinfo[] =
{
	{ REGION_GFX1, 0, &charlayout,           0,  1 }, /*no color codes*/
	{ REGION_GFX2, 0, &spritelayout,      1*16, 16 }, /*16 color codes*/
	{ REGION_GFX3, 0, &tilelayout,  1*16+16*16,  1 }, /*not used by emulation, here only to be able to view the graphics with F4*/
	{ -1 }
};


WRITE_HANDLER( ay8910_data_w )
{
// ?
}

static struct AY8910interface ay8910_interface =
{
	3,			/* 3 chips */
	10000000/8,		/* 1.25 MHz */
	{ 35, 35, 35 },
	{ 0, 0, 0 }, //read port A
	{ 0, 0, 0 }, //read port B
	{ ay8910_portA_0_w, 0, 0 }, //ay8910_data_w, ay8910_data_w, ay8910_data_w }, //write port A
	{ ay8910_portB_0_w, 0, 0 } //ay8910_data_w, ay8910_data_w, ay8910_data_w } //write port B
};


int magmax_interrupt(void)
{
	return MC68000_IRQ_1;
}


static const struct MachineDriver machine_driver_magmax =
{
	{
		{
			CPU_M68000,
			8000000,	/* 8 Mhz */
			magmax_readmem, magmax_writemem, 0, 0,
			magmax_interrupt, 1
		},
		{
			CPU_Z80 | CPU_AUDIO_CPU,
			10000000/4,	/* 2.5 Mhz */
			magmax_soundreadmem, magmax_soundwritemem, magmax_soundreadport, magmax_soundwriteport,
			ignore_interrupt, 1
		},
	},
	60, DEFAULT_60HZ_VBLANK_DURATION,
	1,
	init_machine,

	/* video hardware */
	32*8, 32*8, { 0*8, 32*8-1, 2*8, 30*8-1 },
	gfxdecodeinfo,
	256, 1*16 + 16*16 + 1*16,
	magmax_vh_convert_color_prom,

	VIDEO_TYPE_RASTER,
	0,
	magmax_vh_start,
	magmax_vh_stop,
	magmax_vh_screenrefresh,

	/* sound hardware */
	0, 0, 0, 0,
	{
		{
			SOUND_AY8910,
			&ay8910_interface
		}
	}
};


ROM_START( magmax )
	ROM_REGION( 0x14000, REGION_CPU1 ) /* main cpu code */
	ROM_LOAD_ODD ( "1.3b", 0x00000, 0x4000, 0x33793cbb )
	ROM_LOAD_EVEN( "6.3d", 0x00000, 0x4000, 0x677ef450 )
	ROM_LOAD_ODD ( "2.5b", 0x08000, 0x4000, 0x1a0c84df )
	ROM_LOAD_EVEN( "7.5d", 0x08000, 0x4000, 0x01c35e95 )
	ROM_LOAD_ODD ( "3.6b", 0x10000, 0x2000, 0xd06e6cae )
	ROM_LOAD_EVEN( "8.6d", 0x10000, 0x2000, 0x790a82be )

	ROM_REGION( 0x10000, REGION_CPU2 ) /* sound cpu code */
	ROM_LOAD( "15.17b", 0x00000, 0x2000, 0x19e7b983 )
	ROM_LOAD( "16.18b", 0x02000, 0x2000, 0x055e3126 )

	ROM_REGION( 0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE ) /* chars */
	ROM_LOAD( "23.15g", 0x00000, 0x2000, 0xa7471da2 )

	ROM_REGION( 0x10000, REGION_GFX2 | REGIONFLAG_DISPOSE ) /* sprites */
	ROM_LOAD( "17.3e",  0x00000, 0x2000, 0x8e305b2e )
	ROM_LOAD( "18.5e",  0x02000, 0x2000, 0x14c55a60 )
	ROM_LOAD( "19.6e",  0x04000, 0x2000, 0xfa4141d8 )
	ROM_LOAD( "20.3g",  0x08000, 0x2000, 0x6fa3918b )
	ROM_LOAD( "21.5g",  0x0a000, 0x2000, 0xdd52eda4 )
	ROM_LOAD( "22.6g",  0x0c000, 0x2000, 0x4afc98ff )

	ROM_REGION( 0x08000, REGION_GFX3 ) /* background tiles (data used at runtime so no REGIONFLAG_DISPOSE)*/
	ROM_LOAD( "11.15f", 0x00000, 0x2000, 0x91f3edb6 ) /* surface */
	ROM_LOAD( "12.17f", 0x02000, 0x2000, 0x99771eff ) /* underground */
	ROM_LOAD( "13.18f", 0x04000, 0x2000, 0x75f30159 ) /* surface of mechanical level */
	ROM_LOAD( "14.20f", 0x06000, 0x2000, 0x96babcba ) /* underground of mechanical level */

	ROM_REGION( 0x4000, REGION_USER1 ) /* surface scroll control */
	ROM_LOAD( "4.18b",  0x00000, 0x2000, 0x1550942e )
	ROM_LOAD( "5.20b",  0x02000, 0x2000, 0x3b93017f )

	ROM_REGION( 0x4000, REGION_USER2 ) /* BG control data */
	ROM_LOAD( "9.18d",  0x00000, 0x2000, 0x9ecc9ab8 ) /* surface */
	ROM_LOAD( "10.20d", 0x02000, 0x2000, 0xe2ff7293 ) /* underground */

	ROM_REGION( 0x0200, REGION_USER3 ) /* BG control data */
	ROM_LOAD( "mag_b.14d",  0x0000, 0x0100, 0xa0fb7297 ) /* background control PROM */
	ROM_LOAD( "mag_c.15d",  0x0100, 0x0100, 0xd84a6f78 ) /* background control PROM */

	ROM_REGION( 0x0500, REGION_PROMS ) /* color PROMs */
	ROM_LOAD( "mag_e.10f",  0x0000, 0x0100, 0x75e4f06a ) /* red */
	ROM_LOAD( "mag_d.10e",  0x0100, 0x0100, 0x34b6a6e3 ) /* green */
	ROM_LOAD( "mag_a.10d",  0x0200, 0x0100, 0xa7ea7718 ) /* blue */
	ROM_LOAD( "mag_g.2e",   0x0300, 0x0100, 0x830be358 ) /* sprites color lookup table */
	ROM_LOAD( "mag_f.13b",  0x0400, 0x0100, 0x4a6f9a6d ) /* state machine data used for video signals generation (not used in emulation)*/
ROM_END


GAME( 1985, magmax, 0, magmax, magmax, 0, ROT0, "Nichibutsu", "Mag Max" )
