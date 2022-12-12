/***************************************************************************

Bobble Bobble memory map (preliminary)

driver by Chris Moore

CPU #1
0000-bfff ROM (8000-bfff is banked)
c000-dcff Graphic RAM. This contains pointers to the video RAM columns and
          to the sprites are contained in Object RAM.
dd00-dfff Object RAM (groups of four bytes: X position, code [offset in the
          Graphic RAM], Y position, gfx bank)
CPU #2
0000-7fff ROM

CPU #1 AND #2
e000-f7fe RAM
f800-f9ff Palette RAM
fc01-fdff RAM

read:
ff00      DSWA
ff01      DSWB
ff02      IN0
ff03      IN1


Service mode works only if the language switch is set to Japanese.

- The protection feature which randomizes the EXTEND letters in the original
  version is not emulated properly.


***************************************************************************/
/***************************************************************************

Tokio memory map

CPU 1
0000-bfff ROM (8000-bfff is banked)
c000-dcff Graphic RAM. This contains pointers to the video RAM columns and
          to the sprites contained in Object RAM.
dd00-dfff Object RAM (groups of four bytes: X position, code [offset in the
          Graphic RAM], Y position, gfx bank)
e000-f7ff RAM (Shared)
f800-f9ff Palette RAM

fa03 - DSW0
fa04 - DSW1
fa05 - Coins
fa06 - Controls Player 1
fa07 - Controls Player 1

CPU 2
0000-7fff ROM
8000-97ff RAM (Shared)

CPU 3
0000-7fff ROM
8000-8fff RAM


  Here goes a list of known deficiencies of our drivers:

  - The bootleg romset is functional. The original one hangs at
    the title screen. This is because Fredrik and I have worked
    on the first one, and got mostly done. Later Victor added support
    for the original set (mainly sound), which is still deficient.

  - Score saving is still wrong, I think.

  - Sound support is probably incomplete. There are a couple of unknown
    accesses done by the CPU, including to the YM2203 I/O ports. At the
	very least, there should be some filters.

  - "fake-r" routine make the "original" roms to restart the game after
    some seconds.

    Well, we know very little about the 0xFE00 address. It could be
    some watchdog or a synchronization timer.

    I remember scanning the main CPU code to find how it was
    used on the bootleg set. Then I just figured out a constant value
    that made the game run (it hang if just set unhandled, that is,
    returning zero).

    Maybe the solution is to patch the bootleg ROMs to skip some tests
    at this location (I remember some of them being in the
    initialization routine of the main CPU).

                       Marcelo de G. Malheiros <malheiro@dca.fee.unicamp.br>
                                                                   1998.9.25

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"



/* vidhrdw/bublbobl.c */
extern unsigned char *bublbobl_objectram;
extern int bublbobl_objectram_size;
void bublbobl_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom);
void bublbobl_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh);

/* machine/bublbobl.c */
extern unsigned char *bublbobl_sharedram1,*bublbobl_sharedram2;
int bublbobl_sharedram1_r(int offset);
int bublbobl_sharedram2_r(int offset);
void bublbobl_sharedram1_w(int offset,int data);
void bublbobl_sharedram2_w(int offset,int data);
int bublbobl_m68705_interrupt(void);
int bublbobl_68705_portA_r(int offset);
void bublbobl_68705_portA_w(int offset,int data);
void bublbobl_68705_ddrA_w(int offset,int data);
int bublbobl_68705_portB_r(int offset);
void bublbobl_68705_portB_w(int offset,int data);
void bublbobl_68705_ddrB_w(int offset,int data);
void bublbobl_bankswitch_w(int offset,int data);
void tokio_bankswitch_w(int offset,int data);
void tokio_nmitrigger_w(int offset, int data);
int tokio_fake_r(int offset);
void bublbobl_sound_command_w(int offset,int data);
void bublbobl_sh_nmi_disable_w(int offset,int data);
void bublbobl_sh_nmi_enable_w(int offset,int data);



static struct MemoryReadAddress bublbobl_readmem[] =
{
    { 0x0000, 0x7fff, MRA_ROM },
    { 0x8000, 0xbfff, MRA_BANK1 },
	{ 0xc000, 0xdfff, MRA_RAM },
    { 0xe000, 0xf7ff, bublbobl_sharedram1_r },
	{ 0xf800, 0xf9ff, paletteram_r },
    { 0xfc00, 0xfcff, bublbobl_sharedram2_r },
    { -1 }  /* end of table */
};

static struct MemoryWriteAddress bublbobl_writemem[] =
{
	{ 0x0000, 0xbfff, MWA_ROM },
	{ 0xc000, 0xdcff, MWA_RAM, &videoram, &videoram_size },
	{ 0xdd00, 0xdfff, MWA_RAM, &bublbobl_objectram, &bublbobl_objectram_size },
	{ 0xe000, 0xf7ff, bublbobl_sharedram1_w, &bublbobl_sharedram1 },
	{ 0xf800, 0xf9ff, paletteram_RRRRGGGGBBBBxxxx_swap_w, &paletteram },
	{ 0xfa00, 0xfa00, bublbobl_sound_command_w },
	{ 0xfa80, 0xfa80, watchdog_reset_w },	/* not sure - could go to the 68705 */
	{ 0xfb40, 0xfb40, bublbobl_bankswitch_w },
	{ 0xfc00, 0xfcff, bublbobl_sharedram2_w, &bublbobl_sharedram2 },
	{ -1 }  /* end of table */
};

static struct MemoryReadAddress m68705_readmem[] =
{
	{ 0x0000, 0x0000, bublbobl_68705_portA_r },
	{ 0x0001, 0x0001, bublbobl_68705_portB_r },
	{ 0x0002, 0x0002, input_port_0_r },	/* COIN */
	{ 0x0010, 0x007f, MRA_RAM },
	{ 0x0080, 0x07ff, MRA_ROM },
	{ -1 }	/* end of table */
};

static struct MemoryWriteAddress m68705_writemem[] =
{
	{ 0x0000, 0x0000, bublbobl_68705_portA_w },
	{ 0x0001, 0x0001, bublbobl_68705_portB_w },
	{ 0x0004, 0x0004, bublbobl_68705_ddrA_w },
	{ 0x0005, 0x0005, bublbobl_68705_ddrB_w },
	{ 0x0010, 0x007f, MWA_RAM },
	{ 0x0080, 0x07ff, MWA_ROM },
	{ -1 }	/* end of table */
};


static struct MemoryReadAddress boblbobl_readmem[] =
{
	{ 0x0000, 0x7fff, MRA_ROM },
	{ 0x8000, 0xbfff, MRA_BANK1 },
	{ 0xc000, 0xdfff, MRA_RAM },
	{ 0xe000, 0xf7ff, bublbobl_sharedram1_r },
	{ 0xf800, 0xf9ff, paletteram_r },
	{ 0xfc00, 0xfcff, bublbobl_sharedram2_r },
	{ 0xff00, 0xff00, input_port_0_r },
	{ 0xff01, 0xff01, input_port_1_r },
	{ 0xff02, 0xff02, input_port_2_r },
	{ 0xff03, 0xff03, input_port_3_r },
	{ -1 }  /* end of table */
};

static struct MemoryWriteAddress boblbobl_writemem[] =
{
	{ 0x0000, 0xbfff, MWA_ROM },
	{ 0xc000, 0xdcff, MWA_RAM, &videoram, &videoram_size },
	{ 0xdd00, 0xdfff, MWA_RAM, &bublbobl_objectram, &bublbobl_objectram_size },
	{ 0xe000, 0xf7ff, bublbobl_sharedram1_w, &bublbobl_sharedram1 },
	{ 0xf800, 0xf9ff, paletteram_RRRRGGGGBBBBxxxx_swap_w, &paletteram },
	{ 0xfa00, 0xfa00, bublbobl_sound_command_w },
	{ 0xfa80, 0xfa80, MWA_NOP },
	{ 0xfb40, 0xfb40, bublbobl_bankswitch_w },
	{ 0xfc00, 0xfcff, bublbobl_sharedram2_w, &bublbobl_sharedram2 },
	{ -1 }  /* end of table */
};

static struct MemoryReadAddress bublbobl_readmem2[] =
{
	{ 0x0000, 0x7fff, MRA_ROM },
	{ 0xe000, 0xf7ff, bublbobl_sharedram1_r },
	{ -1 }  /* end of table */
};

static struct MemoryWriteAddress bublbobl_writemem2[] =
{
	{ 0x0000, 0x7fff, MWA_ROM },
	{ 0xe000, 0xf7ff, bublbobl_sharedram1_w },
	{ -1 }  /* end of table */
};


static struct MemoryReadAddress sound_readmem[] =
{
	{ 0x0000, 0x7fff, MRA_ROM },
	{ 0x8000, 0x8fff, MRA_RAM },
	{ 0x9000, 0x9000, YM2203_status_port_0_r },
	{ 0x9001, 0x9001, YM2203_read_port_0_r },
	{ 0xa000, 0xa000, YM3526_status_port_0_r },
	{ 0xb000, 0xb000, soundlatch_r },
	{ 0xb001, 0xb001, MRA_NOP },	/* ??? */
	{ 0xe000, 0xefff, MRA_ROM },	/* space for diagnostic ROM? */
	{ -1 }	/* end of table */
};

static struct MemoryWriteAddress sound_writemem[] =
{
	{ 0x0000, 0x7fff, MWA_ROM },
	{ 0x8000, 0x8fff, MWA_RAM },
	{ 0x9000, 0x9000, YM2203_control_port_0_w },
	{ 0x9001, 0x9001, YM2203_write_port_0_w },
	{ 0xa000, 0xa000, YM3526_control_port_0_w },
	{ 0xa001, 0xa001, YM3526_write_port_0_w },
	{ 0xb000, 0xb000, MWA_NOP },	/* ??? */
	{ 0xb001, 0xb001, bublbobl_sh_nmi_enable_w },
	{ 0xb002, 0xb002, bublbobl_sh_nmi_disable_w },
	{ 0xe000, 0xefff, MWA_ROM },	/* space for diagnostic ROM? */
	{ -1 }	/* end of table */
};


static struct MemoryReadAddress tokio_readmem[] =
{
	{ 0x0000, 0x7fff, MRA_ROM },
	{ 0x8000, 0xbfff, MRA_BANK1 },
	{ 0xc000, 0xdfff, MRA_RAM },
	{ 0xe000, 0xf7ff, bublbobl_sharedram1_r },
	{ 0xf800, 0xf9ff, paletteram_r },
	{ 0xfa03, 0xfa03, input_port_0_r },
	{ 0xfa04, 0xfa04, input_port_1_r },
	{ 0xfa05, 0xfa05, input_port_2_r },
	{ 0xfa06, 0xfa06, input_port_3_r },
	{ 0xfa07, 0xfa07, input_port_4_r },
	{ 0xfe00, 0xfe00, tokio_fake_r },
	{ -1 }  /* end of table */
};

static struct MemoryWriteAddress tokio_writemem[] =
{
	{ 0x0000, 0xbfff, MWA_ROM },
	{ 0xc000, 0xdcff, MWA_RAM, &videoram, &videoram_size },
	{ 0xdd00, 0xdfff, MWA_RAM, &bublbobl_objectram, &bublbobl_objectram_size },
	{ 0xe000, 0xf7ff, bublbobl_sharedram1_w, &bublbobl_sharedram1 },
	{ 0xf800, 0xf9ff, paletteram_RRRRGGGGBBBBxxxx_swap_w, &paletteram },
	{ 0xfa00, 0xfa00, MWA_NOP },
	{ 0xfa80, 0xfa80, tokio_bankswitch_w },
	{ 0xfb00, 0xfb00, MWA_NOP }, /* ??? */
	{ 0xfb80, 0xfb80, tokio_nmitrigger_w },
	{ 0xfc00, 0xfc00, bublbobl_sound_command_w },
	{ 0xfe00, 0xfe00, MWA_NOP }, /* ??? */
	{ -1 }  /* end of table */
};

static struct MemoryReadAddress tokio_readmem2[] =
{
	{ 0x0000, 0x7fff, MRA_ROM },
	{ 0x8000, 0x97ff, bublbobl_sharedram1_r },
	{ -1 }  /* end of table */
};

static struct MemoryWriteAddress tokio_writemem2[] =
{
	{ 0x0000, 0x7fff, MWA_ROM },
	{ 0x8000, 0x97ff, bublbobl_sharedram1_w },
	{ -1 }  /* end of table */
};

static struct MemoryReadAddress tokio_sound_readmem[] =
{
	{ 0x0000, 0x7fff, MRA_ROM },
	{ 0x8000, 0x8fff, MRA_RAM },
	{ 0x9000, 0x9000, soundlatch_r },
//	{ 0x9800, 0x9800, MRA_NOP },	/* ??? */
	{ 0xb000, 0xb000, YM2203_status_port_0_r },
	{ 0xb001, 0xb001, YM2203_read_port_0_r },
	{ 0xe000, 0xefff, MRA_ROM },	/* space for diagnostic ROM? */
	{ -1 }	/* end of table */
};

static struct MemoryWriteAddress tokio_sound_writemem[] =
{
	{ 0x0000, 0x7fff, MWA_ROM },
	{ 0x8000, 0x8fff, MWA_RAM },
//	{ 0x9000, 0x9000, MWA_NOP },	/* ??? */
	{ 0xa000, 0xa000, bublbobl_sh_nmi_disable_w },
	{ 0xa800, 0xa800, bublbobl_sh_nmi_enable_w },
	{ 0xb000, 0xb000, YM2203_control_port_0_w },
	{ 0xb001, 0xb001, YM2203_write_port_0_w },
	{ 0xe000, 0xefff, MWA_ROM },	/* space for diagnostic ROM? */
	{ -1 }	/* end of table */
};



INPUT_PORTS_START( bublbobl )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN3 )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 )
	PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED )

	PORT_START      /* DSW0 */
	PORT_DIPNAME( 0x01, 0x00, "Language" )
	PORT_DIPSETTING(    0x01, "Japanese" )
	PORT_DIPSETTING(    0x00, "English" )
	PORT_DIPNAME( 0x02, 0x02, DEF_STR( Flip_Screen ) )
	PORT_DIPSETTING(    0x02, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_SERVICE( 0x04, IP_ACTIVE_LOW )
	PORT_DIPNAME( 0x08, 0x08, DEF_STR( Demo_Sounds ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x08, DEF_STR( On ) )
	PORT_DIPNAME( 0x30, 0x30, DEF_STR( Coin_A ) )
	PORT_DIPSETTING(    0x10, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0x30, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 2C_3C ) )
	PORT_DIPSETTING(    0x20, DEF_STR( 1C_2C ) )
	PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( Coin_B ) )
	PORT_DIPSETTING(    0x40, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0xc0, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 2C_3C ) )
	PORT_DIPSETTING(    0x80, DEF_STR( 1C_2C ) )

	PORT_START      /* DSW1 */
	PORT_DIPNAME( 0x03, 0x03, DEF_STR( Difficulty ) )
	PORT_DIPSETTING(    0x02, "Easy" )
	PORT_DIPSETTING(    0x03, "Medium" )
	PORT_DIPSETTING(    0x01, "Hard" )
	PORT_DIPSETTING(    0x00, "Hardest" )
	PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( Bonus_Life ) )
	PORT_DIPSETTING(    0x08, "20000 80000" )
	PORT_DIPSETTING(    0x0c, "30000 100000" )
	PORT_DIPSETTING(    0x04, "40000 200000" )
	PORT_DIPSETTING(    0x00, "50000 250000" )
	PORT_DIPNAME( 0x30, 0x30, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x10, "1" )
	PORT_DIPSETTING(    0x00, "2" )
	PORT_DIPSETTING(    0x30, "3" )
	PORT_DIPSETTING(    0x20, "5" )
	PORT_DIPNAME( 0xc0, 0xc0, "Spare" )
	PORT_DIPSETTING(    0x00, "A" )
	PORT_DIPSETTING(    0x40, "B" )
	PORT_DIPSETTING(    0x80, "C" )
	PORT_DIPSETTING(    0xc0, "D" )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START1 )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )

	PORT_START      /* IN2 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START2 )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )
INPUT_PORTS_END

INPUT_PORTS_START( boblbobl )
	PORT_START      /* DSW0 */
	PORT_DIPNAME( 0x01, 0x00, "Language" )
	PORT_DIPSETTING(    0x00, "English" )
	PORT_DIPSETTING(    0x01, "Japanese" )
	PORT_DIPNAME( 0x02, 0x00, DEF_STR( Flip_Screen ) )
	PORT_DIPSETTING(    0x02, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_SERVICE( 0x04, IP_ACTIVE_LOW )
	PORT_DIPNAME( 0x08, 0x08, DEF_STR( Demo_Sounds ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x08, DEF_STR( On ) )
	PORT_DIPNAME( 0x30, 0x30, DEF_STR( Coin_A ) )
	PORT_DIPSETTING(    0x10, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0x30, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 2C_3C ) )
	PORT_DIPSETTING(    0x20, DEF_STR( 1C_2C ) )
	PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( Coin_B ) )
	PORT_DIPSETTING(    0x40, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0xc0, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 2C_3C ) )
	PORT_DIPSETTING(    0x80, DEF_STR( 1C_2C ) )

	PORT_START      /* DSW1 */
	PORT_DIPNAME( 0x03, 0x03, DEF_STR( Difficulty ) )
	PORT_DIPSETTING(    0x02, "Easy" )
	PORT_DIPSETTING(    0x03, "Medium" )
	PORT_DIPSETTING(    0x01, "Hard" )
	PORT_DIPSETTING(    0x00, "Hardest" )
	PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( Bonus_Life ) )
	PORT_DIPSETTING(    0x08, "20000 80000" )
	PORT_DIPSETTING(    0x0c, "30000 100000" )
	PORT_DIPSETTING(    0x04, "40000 200000" )
	PORT_DIPSETTING(    0x00, "50000 250000" )
	PORT_DIPNAME( 0x30, 0x30, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x10, "1" )
	PORT_DIPSETTING(    0x00, "2" )
	PORT_DIPSETTING(    0x30, "3" )
	PORT_DIPSETTING(    0x20, "5" )
	PORT_DIPNAME( 0xc0, 0x00, "Monster Speed" )
	PORT_DIPSETTING(    0x00, "Normal" )
	PORT_DIPSETTING(    0x40, "Medium" )
	PORT_DIPSETTING(    0x80, "High" )
	PORT_DIPSETTING(    0xc0, "Very High" )

	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN2 )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN1 )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START1 )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_TILT ) /* ?????*/
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN3 )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START2 )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )
INPUT_PORTS_END

INPUT_PORTS_START( sboblbob )
	PORT_START      /* DSW0 */
	PORT_DIPNAME( 0x01, 0x00, "Game" )
	PORT_DIPSETTING(    0x01, "Bobble Bobble" )
	PORT_DIPSETTING(    0x00, "Super Bobble Bobble" )
	PORT_DIPNAME( 0x02, 0x02, DEF_STR( Flip_Screen ) )
	PORT_DIPSETTING(    0x02, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_SERVICE( 0x04, IP_ACTIVE_LOW )
	PORT_DIPNAME( 0x08, 0x08, DEF_STR( Demo_Sounds ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x08, DEF_STR( On ) )
	PORT_DIPNAME( 0x30, 0x30, DEF_STR( Coin_A ) )
	PORT_DIPSETTING(    0x10, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0x30, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 2C_3C ) )
	PORT_DIPSETTING(    0x20, DEF_STR( 1C_2C ) )
	PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( Coin_B ) )
	PORT_DIPSETTING(    0x40, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0xc0, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 2C_3C ) )
	PORT_DIPSETTING(    0x80, DEF_STR( 1C_2C ) )

	PORT_START      /* DSW1 */
	PORT_DIPNAME( 0x03, 0x03, DEF_STR( Difficulty ) )
	PORT_DIPSETTING(    0x02, "Easy" )
	PORT_DIPSETTING(    0x03, "Medium" )
	PORT_DIPSETTING(    0x01, "Hard" )
	PORT_DIPSETTING(    0x00, "Hardest" )
	PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( Bonus_Life ) )
	PORT_DIPSETTING(    0x08, "20000 80000" )
	PORT_DIPSETTING(    0x0c, "30000 100000" )
	PORT_DIPSETTING(    0x04, "40000 200000" )
	PORT_DIPSETTING(    0x00, "50000 250000" )
	PORT_DIPNAME( 0x30, 0x30, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x10, "1" )
	PORT_DIPSETTING(    0x00, "2" )
	PORT_DIPSETTING(    0x30, "3" )
	PORT_BITX( 0,       0x20, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "100", IP_KEY_NONE, IP_JOY_NONE )
	PORT_DIPNAME( 0xc0, 0x00, "Monster Speed" )
	PORT_DIPSETTING(    0x00, "Normal" )
	PORT_DIPSETTING(    0x40, "Medium" )
	PORT_DIPSETTING(    0x80, "High" )
	PORT_DIPSETTING(    0xc0, "Very High" )

	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN2 )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN1 )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START1 )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_TILT ) /* ?????*/
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN3 )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START2 )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )
INPUT_PORTS_END

INPUT_PORTS_START( tokio )
	PORT_START      /* DSW0 */
	PORT_DIPNAME( 0x01, 0x00, DEF_STR( Cabinet ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Upright ) )
	PORT_DIPSETTING(    0x01, DEF_STR( Cocktail ) )
	PORT_DIPNAME( 0x02, 0x02, DEF_STR( Flip_Screen ) )
	PORT_DIPSETTING(    0x02, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_SERVICE( 0x04, IP_ACTIVE_LOW )
	PORT_DIPNAME( 0x08, 0x08, "Demo Sounds?" )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x08, DEF_STR( On ) )
	PORT_DIPNAME( 0x30, 0x30, DEF_STR( Coin_A ) )
	PORT_DIPSETTING(    0x10, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0x30, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 2C_3C ) )
	PORT_DIPSETTING(    0x20, DEF_STR( 1C_2C ) )
	PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( Coin_B ) )
	PORT_DIPSETTING(    0x40, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0xc0, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 2C_3C ) )
	PORT_DIPSETTING(    0x80, DEF_STR( 1C_2C ) )

	PORT_START      /* DSW1 */
	PORT_DIPNAME( 0x03, 0x02, DEF_STR( Difficulty ) )
	PORT_DIPSETTING(    0x03, "Easy" )
	PORT_DIPSETTING(    0x02, "Medium" )
	PORT_DIPSETTING(    0x01, "Hard" )
	PORT_DIPSETTING(    0x00, "Hardest" )
	PORT_DIPNAME( 0x0c, 0x08, DEF_STR( Bonus_Life ) )
	PORT_DIPSETTING(    0x0C, "100000 400000" )
	PORT_DIPSETTING(    0x08, "200000 400000" )
	PORT_DIPSETTING(    0x04, "300000 400000" )
	PORT_DIPSETTING(    0x00, "400000 400000" )
	PORT_DIPNAME( 0x30, 0x30, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x30, "3" )
	PORT_DIPSETTING(    0x20, "4" )
	PORT_DIPSETTING(    0x10, "5" )
	PORT_BITX( 0,       0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "99", IP_KEY_NONE, IP_JOY_NONE )
	PORT_DIPNAME( 0x40, 0x40, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x40, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x80, 0x00, "Language" )
	PORT_DIPSETTING(    0x00, "English" )
	PORT_DIPSETTING(    0x80, "Japanese" )

	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN3 ) /* service */
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_2WAY )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_2WAY )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START1 )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )

	PORT_START      /* IN2 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_2WAY | IPF_COCKTAIL )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_2WAY | IPF_COCKTAIL )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START2 )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )
INPUT_PORTS_END



static struct GfxLayout charlayout =
{
	8,8,	/* the characters are 8x8 pixels */
	256*8*8,	/* 256 chars per bank * 8 banks per ROM pair * 8 ROM pairs */
	4,	/* 4 bits per pixel */
	{ 0, 4, 8*0x8000*8, 8*0x8000*8+4 },
	{ 3, 2, 1, 0, 8+3, 8+2, 8+1, 8+0 },
	{ 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
	16*8	/* every char takes 16 bytes in two ROMs */
};

static struct GfxDecodeInfo gfxdecodeinfo[] =
{
	/* read all graphics into one big graphics region */
	{ REGION_GFX1, 0x00000, &charlayout, 0, 16 },
	{ -1 }	/* end of array */
};



/* handler called by the 2203 emulator when the internal timers cause an IRQ */
static void irqhandler(int irq)
{
	cpu_set_irq_line(2,0,irq ? ASSERT_LINE : CLEAR_LINE);
}

static struct YM2203interface ym2203_interface =
{
	1,			/* 1 chip */
	3000000,	/* 3 MHz ??? (hand tuned) */
	{ YM2203_VOL(25,25) },
	{ 0 },
	{ 0 },
	{ 0 },
	{ 0 },
	{ irqhandler }
};


static struct YM3526interface ym3526_interface =
{
	1,			/* 1 chip (no more supported) */
	3000000,	/* 3 MHz ??? (hand tuned) */
	{ 255 }		/* (not supported) */
};


static struct YM2203interface tokio_ym2203_interface =
{
	1,		/* 1 chip */
	3000000,	/* 3 MHz ??? */
	{ YM2203_VOL(100,20) },
	{ 0 },
	{ 0 },
	{ 0 },
	{ 0 },
	{ irqhandler }
};



static struct MachineDriver machine_driver_bublbobl =
{
	/* basic machine hardware */
	{
		{
			CPU_Z80,
			6000000,		/* 6 Mhz??? */
			bublbobl_readmem,bublbobl_writemem,0,0,
			ignore_interrupt,0	/* IRQs are triggered by the 68705 */
		},
		{
			CPU_Z80,
			6000000,		/* 6 Mhz??? */
			bublbobl_readmem2,bublbobl_writemem2,0,0,
			interrupt,1
		},
		{
			CPU_Z80 | CPU_AUDIO_CPU,
			4000000,	/* 4 Mhz ??? */
			sound_readmem,sound_writemem,0,0,
			ignore_interrupt,0	/* NMIs are triggered by the main CPU */
								/* IRQs are triggered by the YM2203 */
		},
		{
			CPU_M68705,
			4000000/2,	/* xtal is 4MHz, I think it's divided by 2 internally */
			m68705_readmem,m68705_writemem,0,0,
			bublbobl_m68705_interrupt,2	/* ??? should come from the same */
					/* clock which latches the INT pin on the second Z80 */
		}
	},
	60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
	100,	/* 100 CPU slices per frame - an high value to ensure proper */
			/* synchronization of the CPUs */
	0,		/* init_machine() */

	/* video hardware */
	32*8, 32*8,	{ 0, 32*8-1, 2*8, 30*8-1 },
	gfxdecodeinfo,
	256, 256,
	bublbobl_vh_convert_color_prom,

	VIDEO_TYPE_RASTER|VIDEO_MODIFIES_PALETTE,
	0,
	generic_vh_start,
	generic_vh_stop,
	bublbobl_vh_screenrefresh,

	/* sound hardware */
	0,0,0,0,
	{
		{
			SOUND_YM2203,
			&ym2203_interface
		},
		{
			SOUND_YM3526,
			&ym3526_interface
		}
	}
};

static struct MachineDriver machine_driver_boblbobl =
{
	/* basic machine hardware */
	{
		{
			CPU_Z80,
			6000000,		/* 6 Mhz??? */
			boblbobl_readmem,boblbobl_writemem,0,0,
			interrupt,1	/* interrupt mode 1, unlike Bubble Bobble */
		},
		{
			CPU_Z80,
			6000000,		/* 6 Mhz??? */
			bublbobl_readmem2,bublbobl_writemem2,0,0,
			interrupt,1
		},
		{
			CPU_Z80 | CPU_AUDIO_CPU,
			4000000,	/* 4 Mhz ??? */
			sound_readmem,sound_writemem,0,0,
			ignore_interrupt,0	/* NMIs are triggered by the main CPU */
								/* IRQs are triggered by the YM2203 */
		}
	},
	60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
	100,	/* 100 CPU slices per frame - an high value to ensure proper */
			/* synchronization of the CPUs */
	0,		/* init_machine() */

	/* video hardware */
	32*8, 32*8,	{ 0, 32*8-1, 2*8, 30*8-1 },
	gfxdecodeinfo,
	256, 256,
	bublbobl_vh_convert_color_prom,

	VIDEO_TYPE_RASTER|VIDEO_MODIFIES_PALETTE,
	0,
	generic_vh_start,
	generic_vh_stop,
	bublbobl_vh_screenrefresh,

	/* sound hardware */
	0,0,0,0,
	{
		{
			SOUND_YM2203,
			&ym2203_interface
		},
		{
			SOUND_YM3526,
			&ym3526_interface
		}
	}
};

static struct MachineDriver machine_driver_tokio =
{
	/* basic machine hardware */
	{		/* MachineCPU */
		{       /* Main CPU */
			CPU_Z80,
			4000000,		/* 4 Mhz??? */
			tokio_readmem,tokio_writemem,0,0,
			interrupt,1
		},
		{       /* Video CPU */
			CPU_Z80,
			4000000,		/* 4 Mhz??? */
			tokio_readmem2,tokio_writemem2,0,0,
			interrupt,1
		},
		{       /* Audio CPU */
			CPU_Z80 | CPU_AUDIO_CPU,
			4000000,	        /* 4 Mhz ??? */
			tokio_sound_readmem,tokio_sound_writemem,0,0,
			ignore_interrupt,0
						/* NMIs are triggered by the main CPU */
						/* IRQs are triggered by the YM2203 */
		}
	},
	60, DEFAULT_60HZ_VBLANK_DURATION, /* frames/second, vblank duration */
	100,	/* 100 CPU slices per frame - an high value to ensure proper */
			/* synchronization of the CPUs */
	0,	/* init_machine() */

	/* video hardware */
	32*8, 32*8,	{ 0, 32*8-1, 2*8, 30*8-1 },
	gfxdecodeinfo,
	256, 256,
	bublbobl_vh_convert_color_prom,

	VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
	0,
	generic_vh_start,
	generic_vh_stop,
	bublbobl_vh_screenrefresh,

	/* sound hardware */
	0,0,0,0,
	{
		{
			SOUND_YM2203,
			&tokio_ym2203_interface
		}
	}
};


/***************************************************************************

  Game driver(s)

***************************************************************************/

ROM_START( bublbobl )
	ROM_REGION( 0x1c000, REGION_CPU1 )	/* 64k+64k for the first CPU */
	ROM_LOAD( "a78_06.bin",   0x00000, 0x8000, 0x32c8305b )
	ROM_LOAD( "a78_05.bin",   0x08000, 0x4000, 0x53f4bc6e )	/* banked at 8000-bfff. I must load */
	ROM_CONTINUE(             0x10000, 0xc000 )				/* bank 0 at 8000 because the code falls into */
															/* it from 7fff, so bank switching wouldn't work */
	ROM_REGION( 0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE )
	ROM_LOAD( "a78_09.bin",   0x00000, 0x8000, 0x20358c22 )    /* 1st plane */
	ROM_LOAD( "a78_10.bin",   0x08000, 0x8000, 0x930168a9 )
	ROM_LOAD( "a78_11.bin",   0x10000, 0x8000, 0x9773e512 )
	ROM_LOAD( "a78_12.bin",   0x18000, 0x8000, 0xd045549b )
	ROM_LOAD( "a78_13.bin",   0x20000, 0x8000, 0xd0af35c5 )
	ROM_LOAD( "a78_14.bin",   0x28000, 0x8000, 0x7b5369a8 )
	/* 0x30000-0x3ffff empty */
	ROM_LOAD( "a78_15.bin",   0x40000, 0x8000, 0x6b61a413 )    /* 2nd plane */
	ROM_LOAD( "a78_16.bin",   0x48000, 0x8000, 0xb5492d97 )
	ROM_LOAD( "a78_17.bin",   0x50000, 0x8000, 0xd69762d5 )
	ROM_LOAD( "a78_18.bin",   0x58000, 0x8000, 0x9f243b68 )
	ROM_LOAD( "a78_19.bin",   0x60000, 0x8000, 0x66e9438c )
	ROM_LOAD( "a78_20.bin",   0x68000, 0x8000, 0x9ef863ad )
	/* 0x70000-0x7ffff empty */

	ROM_REGION( 0x10000, REGION_CPU2 )	/* 64k for the second CPU */
	ROM_LOAD( "a78_08.bin",   0x0000, 0x08000, 0xae11a07b )

	ROM_REGION( 0x10000, REGION_CPU3 )	/* 64k for the third CPU */
	ROM_LOAD( "a78_07.bin",   0x0000, 0x08000, 0x4f9a26e8 )

	ROM_REGION( 0x0800, REGION_CPU4 )	/* 2k for the microcontroller */
	ROM_LOAD( "68705.bin",    0x0000, 0x0800, 0x78caa635 )	/* from a pirate board */
ROM_END

ROM_START( bublbobr )
	ROM_REGION( 0x1c000, REGION_CPU1 )	/* 64k+64k for the first CPU */
	ROM_LOAD( "25.cpu",       0x00000, 0x8000, 0x2d901c9d )
	ROM_LOAD( "24.cpu",       0x08000, 0x4000, 0xb7afedc4 )	/* banked at 8000-bfff. I must load */
	ROM_CONTINUE(             0x10000, 0xc000 )				/* bank 0 at 8000 because the code falls into */
															/* it from 7fff, so bank switching wouldn't work */
	ROM_REGION( 0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE )
	ROM_LOAD( "a78_09.bin",   0x00000, 0x8000, 0x20358c22 )    /* 1st plane */
	ROM_LOAD( "a78_10.bin",   0x08000, 0x8000, 0x930168a9 )
	ROM_LOAD( "a78_11.bin",   0x10000, 0x8000, 0x9773e512 )
	ROM_LOAD( "a78_12.bin",   0x18000, 0x8000, 0xd045549b )
	ROM_LOAD( "a78_13.bin",   0x20000, 0x8000, 0xd0af35c5 )
	ROM_LOAD( "a78_14.bin",   0x28000, 0x8000, 0x7b5369a8 )
	/* 0x30000-0x3ffff empty */
	ROM_LOAD( "a78_15.bin",   0x40000, 0x8000, 0x6b61a413 )    /* 2nd plane */
	ROM_LOAD( "a78_16.bin",   0x48000, 0x8000, 0xb5492d97 )
	ROM_LOAD( "a78_17.bin",   0x50000, 0x8000, 0xd69762d5 )
	ROM_LOAD( "a78_18.bin",   0x58000, 0x8000, 0x9f243b68 )
	ROM_LOAD( "a78_19.bin",   0x60000, 0x8000, 0x66e9438c )
	ROM_LOAD( "a78_20.bin",   0x68000, 0x8000, 0x9ef863ad )
	/* 0x70000-0x7ffff empty */

	ROM_REGION( 0x10000, REGION_CPU2 )	/* 64k for the second CPU */
	ROM_LOAD( "a78_08.bin",   0x0000, 0x08000, 0xae11a07b )

	ROM_REGION( 0x10000, REGION_CPU3 )	/* 64k for the third CPU */
	ROM_LOAD( "a78_07.bin",   0x0000, 0x08000, 0x4f9a26e8 )

	ROM_REGION( 0x0800, REGION_CPU4 )	/* 2k for the microcontroller */
	ROM_LOAD( "68705.bin",    0x0000, 0x0800, 0x78caa635 )	/* from a pirate board */
ROM_END

ROM_START( bubbobr1 )
	ROM_REGION( 0x1c000, REGION_CPU1 )	/* 64k+64k for the first CPU */
	ROM_LOAD( "a78_06.bin",   0x00000, 0x8000, 0x32c8305b )
	ROM_LOAD( "a78_21.bin",   0x08000, 0x4000, 0x2844033d )	/* banked at 8000-bfff. I must load */
	ROM_CONTINUE(             0x10000, 0xc000 )				/* bank 0 at 8000 because the code falls into */
															/* it from 7fff, so bank switching wouldn't work */
	ROM_REGION( 0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE )
	ROM_LOAD( "a78_09.bin",   0x00000, 0x8000, 0x20358c22 )    /* 1st plane */
	ROM_LOAD( "a78_10.bin",   0x08000, 0x8000, 0x930168a9 )
	ROM_LOAD( "a78_11.bin",   0x10000, 0x8000, 0x9773e512 )
	ROM_LOAD( "a78_12.bin",   0x18000, 0x8000, 0xd045549b )
	ROM_LOAD( "a78_13.bin",   0x20000, 0x8000, 0xd0af35c5 )
	ROM_LOAD( "a78_14.bin",   0x28000, 0x8000, 0x7b5369a8 )
	/* 0x30000-0x3ffff empty */
	ROM_LOAD( "a78_15.bin",   0x40000, 0x8000, 0x6b61a413 )    /* 2nd plane */
	ROM_LOAD( "a78_16.bin",   0x48000, 0x8000, 0xb5492d97 )
	ROM_LOAD( "a78_17.bin",   0x50000, 0x8000, 0xd69762d5 )
	ROM_LOAD( "a78_18.bin",   0x58000, 0x8000, 0x9f243b68 )
	ROM_LOAD( "a78_19.bin",   0x60000, 0x8000, 0x66e9438c )
	ROM_LOAD( "a78_20.bin",   0x68000, 0x8000, 0x9ef863ad )
	/* 0x70000-0x7ffff empty */

	ROM_REGION( 0x10000, REGION_CPU2 )	/* 64k for the second CPU */
	ROM_LOAD( "a78_08.bin",   0x0000, 0x08000, 0xae11a07b )

	ROM_REGION( 0x10000, REGION_CPU3 )	/* 64k for the third CPU */
	ROM_LOAD( "a78_07.bin",   0x0000, 0x08000, 0x4f9a26e8 )

	ROM_REGION( 0x0800, REGION_CPU4 )	/* 2k for the microcontroller */
	ROM_LOAD( "68705.bin",    0x0000, 0x0800, 0x78caa635 )	/* from a pirate board */
ROM_END

ROM_START( boblbobl )
	ROM_REGION( 0x1c000, REGION_CPU1 )	/* 64k+64k for the first CPU */
	ROM_LOAD( "bb3",          0x00000, 0x8000, 0x01f81936 )
	ROM_LOAD( "bb5",          0x08000, 0x4000, 0x13118eb1 )	/* banked at 8000-bfff. I must load */
	ROM_CONTINUE(             0x10000, 0x4000 )				/* bank 0 at 8000 because the code falls into */
															/* it from 7fff, so bank switching wouldn't work */
	ROM_LOAD( "bb4",          0x14000, 0x8000, 0xafda99d8 )	/* banked at 8000-bfff */

	ROM_REGION( 0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE )
	ROM_LOAD( "a78_09.bin",   0x00000, 0x8000, 0x20358c22 )    /* 1st plane */
	ROM_LOAD( "a78_10.bin",   0x08000, 0x8000, 0x930168a9 )
	ROM_LOAD( "a78_11.bin",   0x10000, 0x8000, 0x9773e512 )
	ROM_LOAD( "a78_12.bin",   0x18000, 0x8000, 0xd045549b )
	ROM_LOAD( "a78_13.bin",   0x20000, 0x8000, 0xd0af35c5 )
	ROM_LOAD( "a78_14.bin",   0x28000, 0x8000, 0x7b5369a8 )
	/* 0x30000-0x3ffff empty */
	ROM_LOAD( "a78_15.bin",   0x40000, 0x8000, 0x6b61a413 )    /* 2nd plane */
	ROM_LOAD( "a78_16.bin",   0x48000, 0x8000, 0xb5492d97 )
	ROM_LOAD( "a78_17.bin",   0x50000, 0x8000, 0xd69762d5 )
	ROM_LOAD( "a78_18.bin",   0x58000, 0x8000, 0x9f243b68 )
	ROM_LOAD( "a78_19.bin",   0x60000, 0x8000, 0x66e9438c )
	ROM_LOAD( "a78_20.bin",   0x68000, 0x8000, 0x9ef863ad )
	/* 0x70000-0x7ffff empty */

	ROM_REGION( 0x10000, REGION_CPU2 )	/* 64k for the second CPU */
	ROM_LOAD( "a78_08.bin",   0x0000, 0x08000, 0xae11a07b )

	ROM_REGION( 0x10000, REGION_CPU3 )	/* 64k for the third CPU */
	ROM_LOAD( "a78_07.bin",   0x0000, 0x08000, 0x4f9a26e8 )
ROM_END

ROM_START( sboblbob )
	ROM_REGION( 0x1c000, REGION_CPU1 )	/* 64k+64k for the first CPU */
	ROM_LOAD( "bbb-3.rom",    0x00000, 0x8000, 0xf304152a )
	ROM_LOAD( "bb5",          0x08000, 0x4000, 0x13118eb1 )	/* banked at 8000-bfff. I must load */
	ROM_CONTINUE(             0x10000, 0x4000 )				/* bank 0 at 8000 because the code falls into */
															/* it from 7fff, so bank switching wouldn't work */
	ROM_LOAD( "bbb-4.rom",    0x14000, 0x8000, 0x94c75591 )	/* banked at 8000-bfff */

	ROM_REGION( 0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE )
	ROM_LOAD( "a78_09.bin",   0x00000, 0x8000, 0x20358c22 )    /* 1st plane */
	ROM_LOAD( "a78_10.bin",   0x08000, 0x8000, 0x930168a9 )
	ROM_LOAD( "a78_11.bin",   0x10000, 0x8000, 0x9773e512 )
	ROM_LOAD( "a78_12.bin",   0x18000, 0x8000, 0xd045549b )
	ROM_LOAD( "a78_13.bin",   0x20000, 0x8000, 0xd0af35c5 )
	ROM_LOAD( "a78_14.bin",   0x28000, 0x8000, 0x7b5369a8 )
	/* 0x30000-0x3ffff empty */
	ROM_LOAD( "a78_15.bin",   0x40000, 0x8000, 0x6b61a413 )    /* 2nd plane */
	ROM_LOAD( "a78_16.bin",   0x48000, 0x8000, 0xb5492d97 )
	ROM_LOAD( "a78_17.bin",   0x50000, 0x8000, 0xd69762d5 )
	ROM_LOAD( "a78_18.bin",   0x58000, 0x8000, 0x9f243b68 )
	ROM_LOAD( "a78_19.bin",   0x60000, 0x8000, 0x66e9438c )
	ROM_LOAD( "a78_20.bin",   0x68000, 0x8000, 0x9ef863ad )
	/* 0x70000-0x7ffff empty */

	ROM_REGION( 0x10000, REGION_CPU2 )	/* 64k for the second CPU */
	ROM_LOAD( "a78_08.bin",   0x0000, 0x08000, 0xae11a07b )

	ROM_REGION( 0x10000, REGION_CPU3 )	/* 64k for the third CPU */
	ROM_LOAD( "a78_07.bin",   0x0000, 0x08000, 0x4f9a26e8 )
ROM_END

ROM_START( tokio )
	ROM_REGION( 0x30000, REGION_CPU1 )	/* main CPU */
	ROM_LOAD( "a7127-1.256", 0x00000, 0x8000, 0x8c180896 )
    /* ROMs banked at 8000-bfff */
	ROM_LOAD( "a7128-1.256", 0x10000, 0x8000, 0x1b447527 )
	ROM_LOAD( "a7104.256",   0x18000, 0x8000, 0xa0a4ce0e )
	ROM_LOAD( "a7105.256",   0x20000, 0x8000, 0x6da0b945 )
	ROM_LOAD( "a7106-1.256", 0x28000, 0x8000, 0x56927b3f )

	ROM_REGION( 0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE )
	ROM_LOAD( "a7108.256",   0x00000, 0x8000, 0x0439ab13 )    /* 1st plane */
	ROM_LOAD( "a7109.256",   0x08000, 0x8000, 0xedb3d2ff )
	ROM_LOAD( "a7110.256",   0x10000, 0x8000, 0x69f0888c )
	ROM_LOAD( "a7111.256",   0x18000, 0x8000, 0x4ae07c31 )
	ROM_LOAD( "a7112.256",   0x20000, 0x8000, 0x3f6bd706 )
	ROM_LOAD( "a7113.256",   0x28000, 0x8000, 0xf2c92aaa )
	ROM_LOAD( "a7114.256",   0x30000, 0x8000, 0xc574b7b2 )
	ROM_LOAD( "a7115.256",   0x38000, 0x8000, 0x12d87e7f )
	ROM_LOAD( "a7116.256",   0x40000, 0x8000, 0x0bce35b6 )    /* 2nd plane */
	ROM_LOAD( "a7117.256",   0x48000, 0x8000, 0xdeda6387 )
	ROM_LOAD( "a7118.256",   0x50000, 0x8000, 0x330cd9d7 )
	ROM_LOAD( "a7119.256",   0x58000, 0x8000, 0xfc4b29e0 )
	ROM_LOAD( "a7120.256",   0x60000, 0x8000, 0x65acb265 )
	ROM_LOAD( "a7121.256",   0x68000, 0x8000, 0x33cde9b2 )
	ROM_LOAD( "a7122.256",   0x70000, 0x8000, 0xfb98eac0 )
	ROM_LOAD( "a7123.256",   0x78000, 0x8000, 0x30bd46ad )

	ROM_REGION( 0x10000, REGION_CPU2 )	/* video CPU */
	ROM_LOAD( "a7101.256",   0x00000, 0x8000, 0x0867c707 )

	ROM_REGION( 0x10000, REGION_CPU3 )	/* audio CPU */
	ROM_LOAD( "a7107.256",   0x0000, 0x08000, 0xf298cc7b )
ROM_END

ROM_START( tokiob )
	ROM_REGION( 0x30000, REGION_CPU1 ) /* main CPU */
	ROM_LOAD( "2",           0x00000, 0x8000, 0xf583b1ef )
    /* ROMs banked at 8000-bfff */
	ROM_LOAD( "3",           0x10000, 0x8000, 0x69dacf44 )
	ROM_LOAD( "a7104.256",   0x18000, 0x8000, 0xa0a4ce0e )
	ROM_LOAD( "a7105.256",   0x20000, 0x8000, 0x6da0b945 )
	ROM_LOAD( "6",           0x28000, 0x8000, 0x1490e95b )

	ROM_REGION( 0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE )
	ROM_LOAD( "a7108.256",   0x00000, 0x8000, 0x0439ab13 )    /* 1st plane */
	ROM_LOAD( "a7109.256",   0x08000, 0x8000, 0xedb3d2ff )
	ROM_LOAD( "a7110.256",   0x10000, 0x8000, 0x69f0888c )
	ROM_LOAD( "a7111.256",   0x18000, 0x8000, 0x4ae07c31 )
	ROM_LOAD( "a7112.256",   0x20000, 0x8000, 0x3f6bd706 )
	ROM_LOAD( "a7113.256",   0x28000, 0x8000, 0xf2c92aaa )
	ROM_LOAD( "a7114.256",   0x30000, 0x8000, 0xc574b7b2 )
	ROM_LOAD( "a7115.256",   0x38000, 0x8000, 0x12d87e7f )
	ROM_LOAD( "a7116.256",   0x40000, 0x8000, 0x0bce35b6 )    /* 2nd plane */
	ROM_LOAD( "a7117.256",   0x48000, 0x8000, 0xdeda6387 )
	ROM_LOAD( "a7118.256",   0x50000, 0x8000, 0x330cd9d7 )
	ROM_LOAD( "a7119.256",   0x58000, 0x8000, 0xfc4b29e0 )
	ROM_LOAD( "a7120.256",   0x60000, 0x8000, 0x65acb265 )
	ROM_LOAD( "a7121.256",   0x68000, 0x8000, 0x33cde9b2 )
	ROM_LOAD( "a7122.256",   0x70000, 0x8000, 0xfb98eac0 )
	ROM_LOAD( "a7123.256",   0x78000, 0x8000, 0x30bd46ad )

	ROM_REGION( 0x10000, REGION_CPU2 )	/* video CPU */
	ROM_LOAD( "a7101.256",   0x00000, 0x8000, 0x0867c707 )

	ROM_REGION( 0x10000, REGION_CPU3 )	/* audio CPU */
	ROM_LOAD( "a7107.256",   0x0000, 0x08000, 0xf298cc7b )
ROM_END



#define MOD_PAGE(page,addr,data) memory_region(REGION_CPU1)[page ? addr-0x8000+0x10000+0x4000*(page-1) : addr] = data;

void init_boblbobl(void)
{
    /* these shouldn't be necessary, surely - this is a bootleg ROM
     * with the protection removed - so what are all these JP's to
     * 0xa288 doing?  and why does the emulator fail the ROM checks?
     */

	MOD_PAGE(3,0x9a71,0x00); MOD_PAGE(3,0x9a72,0x00); MOD_PAGE(3,0x9a73,0x00);
	MOD_PAGE(3,0xa4af,0x00); MOD_PAGE(3,0xa4b0,0x00); MOD_PAGE(3,0xa4b1,0x00);
	MOD_PAGE(3,0xa55d,0x00); MOD_PAGE(3,0xa55e,0x00); MOD_PAGE(3,0xa55f,0x00);
	MOD_PAGE(3,0xb561,0x00); MOD_PAGE(3,0xb562,0x00); MOD_PAGE(3,0xb563,0x00);
}



GAMEX( 1986, bublbobl, 0,        bublbobl, bublbobl, 0,        ROT0,  "Taito Corporation", "Bubble Bobble", GAME_NO_COCKTAIL )
GAMEX( 1986, bublbobr, bublbobl, bublbobl, bublbobl, 0,        ROT0,  "Taito America Corporation (Romstar license)", "Bubble Bobble (US set 1)", GAME_NO_COCKTAIL )
GAMEX( 1986, bubbobr1, bublbobl, bublbobl, bublbobl, 0,        ROT0,  "Taito America Corporation (Romstar license)", "Bubble Bobble (US set 2)", GAME_NO_COCKTAIL )
GAMEX( 1986, boblbobl, bublbobl, boblbobl, boblbobl, boblbobl, ROT0,  "bootleg", "Bobble Bobble", GAME_NO_COCKTAIL )
GAMEX( 1986, sboblbob, bublbobl, boblbobl, sboblbob, 0,        ROT0,  "bootleg", "Super Bobble Bobble", GAME_NO_COCKTAIL )
GAMEX( 1986, tokio,    0,        tokio,    tokio,    0,        ROT90, "Taito", "Tokio / Scramble Formation", GAME_NOT_WORKING | GAME_NO_COCKTAIL )
GAMEX( 1986, tokiob,   tokio,    tokio,    tokio,    0,        ROT90, "bootleg", "Tokio / Scramble Formation (bootleg)", GAME_NO_COCKTAIL )
