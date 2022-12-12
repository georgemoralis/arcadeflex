/***************************************************************************

Taito F2 System

driver by Brad Oliver, Andrew Prime

TODO:
- growl_05.rom is not being used, does the YM2610 need separate ROM regions
  like NeoGeo?

The Taito F2 system is a fairly flexible hardware platform. It supports 4
separate layers of graphics - one 64x64 tiled scrolling background plane
of 8x8 tiles, a similar foreground plane, a sprite plane capable of handling
all the video chores by itself (used in e.g. Super Space Invaders) and a text
plane which may or may not scroll. The text plane has 8x8 characters which are
generated in RAM.

Sound is handled by a Z80 with a YM2610 connected to it.

The memory map for each of the games is similar but not identical.

Memory map for Liquid Kids

CPU 1 : 68000, uses irqs 5 & 6. One of the IRQs just sets a flag which is
checked in the other IRQ routine. Could be timed to vblank...

0x000000 - 0x0fffff : ROM (not all used)
0x100000 - 0x10ffff : 64k of RAM
0x200000 - 0x201fff : palette RAM, 4096 total colors
0x300000 - 0x30000f : input ports and dipswitches (writes may be IRQ acknowledge)
0x320000 - 0x320003 : communication with sound CPU
0x800000 - 0x803fff : 64x64 background layer
0x804000 - 0x805fff : 64x64 text layer
0x806000 - 0x807fff : 256 (512?) character generator RAM
0x808000 - 0x80bfff : 64x64 foreground layer
0x80c000 - 0x80ffff : unused?
0x820000 - 0x820005 : x scroll for 3 layers (3rd is unknown)
0x820006 - 0x82000b : y scroll for 3 layers (3rd is unknown)
0x82000c - 0x82000f : unknown (leds?)
0x900000 - 0x90ffff : 64k of sprite RAM
0xb00002 - 0xb00002 : watchdog?

TODO:
	* There are some occasional sprite glitches - IRQ timing issue?
	* Dipswitches are wrong
	* No high score save yet
	* Does Growl bankswitch the sprites? $4000 total, but the sprite list
	  only contains tile numbers up to $1fff

F2 Game List

? Final Bout (unknown)
. Mega Blade (3)
. http://www.taito.co.jp/his/A_HIS/HTM/QUI_TORI.HTM (4)
. Liquid Kids (7)
. Super Space Invaders / Majestic 12 (8)
. Gun Frontier (9)
. Growl / Runark (10)
. Hat Trick Pro (11)
. Mahjong Quest (12)
. http://www.taito.co.jp/his/A_HIS/HTM/YOUYU.HTM (13)
. http://www.taito.co.jp/his/A_HIS/HTM/KOSHIEN.HTM (14)
. Ninja Kids (15)
. http://www.taito.co.jp/his/A_HIS/HTM/Q_QUEST.HTM (no number)
. Metal Black (no number)
. http://www.taito.co.jp/his/A_HIS/HTM/QUI_TIK.HTM (no number)
? Dinorex (no number)
? Pulirula (no number)

This list is translated version of
http://www.aianet.or.jp/~eisetu/rom/rom_tait.html
This page also contains info for other Taito boards.

F2 Motherboard ( Big ) K1100432A, J1100183A
               (Small) K1100608A, J1100242A

*Apr.1989 Final Blow (B82, M4300123A, K1100433A)
*Jul.1989 Don Doko Don (B95, M4300131A, K1100454A, J1100195A)
*Oct.1989 Mega Blast (C11)
Feb.1990 Quiz Torimonochou (C41, K1100554A)
*Apr.1990 Cameltry (C38, M4300167A, K1100556A)
Jul.1990 Quiz H.Q. (C53, K1100594A)
*Aug.1990 Thunder Fox (C28, M4300181A, K1100580A) (exists in F1 version too)
*Sep.1990 Liquid Kids/Mizubaku Daibouken (C49, K1100593A)
*Nov.1990 MJ-12/Super Space Invaders (C64, M4300195A, K1100616A, J1100248A)
Jan.1991 Gun Frontier (C71, M4300199A, K1100625A, K1100629A(overseas))
*Feb.1991 Growl/Runark (C74, M4300210A, K1100639A)
Mar.1991 Hat Trick Hero/Euro Football Championship (C80, K11J0646A)
Mar.1991 Yuu-yu no Quiz de Go!Go! (C83, K11J0652A)
Apr.1991 Ah Eikou no Koshien (C81, M43J0214A, K11J654A)
*Apr.1991 Ninja Kids (C85, M43J0217A, K11J0659A)
May.1991 Mahjong Quest (C77, K1100637A, K1100637B)
Jul.1991 Quiz Quest (C92, K11J0678A)
Sep.1991 Metal Black (D12)
Oct.1991 Drift Out (Visco) (M43X0241A, K11X0695A)
*Nov.1991 PuLiRuLa (C98, M43J0225A, K11J0672A)
Feb.1992 Quiz Chikyu Boueigun (D19, K11J0705A)
Jul.1992 Dead Connection (D28, K11J0715A)
*Nov.1992 Dinorex (D39, K11J0254A)
Mar.1993 Quiz Jinsei Gekijou (D48, M43J0262A, K11J0742A)
Aug.1993 Quiz Crayon Shinchan (D55, K11J0758A)
Dec.1993 Crayon Shinchan Orato Asobo (D63, M43J0276A, K11J0779A)

Mar.1992 Yes.No. Shinri Tokimeki Chart (Fortune teller machine) (D20, K11J0706B)

* means emulated by Raine. I don't know driftout in Raine is
F2 version or not.
Thunder Fox, Drift Out, "Quiz Crayon Shinchan", and "Crayon Shinchan
Orato Asobo" has "Not F2" version PCB.
Foreign version of Cameltry uses different hardware (B89's PLD,
K1100573A, K1100574A).

***************************************************************************/

#include "driver.h"
#include "cpu/m68000/m68000.h"
#include "vidhrdw/generic.h"

extern unsigned char *taitof2_scrollx;
extern unsigned char *taitof2_scrolly;
extern unsigned char *f2_backgroundram;
extern int f2_backgroundram_size;
extern unsigned char *f2_foregroundram;
extern int f2_foregroundram_size;
extern unsigned char *f2_textram;
extern int f2_textram_size;
extern unsigned char *taitof2_characterram;
extern int f2_characterram_size;
extern int f2_paletteram_size;

int taitof2_vh_start (void);
void taitof2_vh_stop (void);
int taitof2_characterram_r (int offset);
void taitof2_characterram_w (int offset,int data);
int taitof2_text_r (int offset);
void taitof2_text_w (int offset,int data);
int taitof2_background_r (int offset);
void taitof2_background_w (int offset,int data);
int taitof2_foreground_r (int offset);
void taitof2_foreground_w (int offset,int data);
void taitof2_spritebank_w (int offset,int data);
void taitof2_vh_screenrefresh (struct osd_bitmap *bitmap,int full_refresh);

int ssi_videoram_r (int offset);
void ssi_videoram_w (int offset,int data);

void rastan_sound_port_w (int offset,int data);
void rastan_sound_comm_w (int offset,int data);
int rastan_sound_comm_r (int offset);

void r_wr_a000(int offset,int data);
void r_wr_a001(int offset,int data);
int  r_rd_a001(int offset);

void ssi_sound_w(int offset,int data);
int ssi_sound_r(int offset);

void cchip1_init_machine(void);
int cchip1_r (int offset);
void cchip1_w (int offset, int data);

static unsigned char *taitof2_ram; /* used for high score save */

static void bankswitch_w (int offset, int data)
{
	unsigned char *RAM = memory_region(REGION_CPU2);
	int banknum = (data - 1) & 3;

	cpu_setbank (2, &RAM [0x10000 + (banknum * 0x4000)]);
}

static int taitof2_input_r (int offset)
{
    switch (offset)
    {
         case 0x00:
              return readinputport(3); /* DSW A */

         case 0x02:
              return readinputport(4); /* DSW B */

         case 0x04:
              return readinputport(0); /* IN0 */

         case 0x06:
              return readinputport(1); /* IN1 */

         case 0x0e:
              return readinputport(2); /* IN2 */
    }

if (errorlog) fprintf(errorlog,"CPU #0 PC %06x: warning - read unmapped memory address %06x\n",cpu_get_pc(),0x100000+offset);

	return 0xff;
}

static int growl_dsw_r (int offset)
{
    switch (offset)
    {
         case 0x00:
              return readinputport(3); /* DSW A */

         case 0x02:
              return readinputport(4); /* DSW B */
    }

if (errorlog) fprintf(errorlog,"CPU #0 PC %06x: warning - read unmapped memory address %06x\n",cpu_get_pc(),0x100000+offset);

	return 0xff;
}

static int growl_input_r (int offset)
{
    switch (offset)
    {
         case 0x00:
              return readinputport(0); /* IN0 */

         case 0x02:
              return readinputport(1); /* IN1 */

         case 0x04:
              return readinputport(2); /* IN2 */

    }

if (errorlog) fprintf(errorlog,"CPU #0 PC %06x: warning - read unmapped memory address %06x\n",cpu_get_pc(),0x100000+offset);

	return 0xff;
}

int megab_input_r (int offset)
{
	switch (offset)
	{
		case 0x00:
			return readinputport (0);
		case 0x02:
			return readinputport (1);
		case 0x04:
			return readinputport (2);
		case 0x06:
			return readinputport (3);
		default:
			if (errorlog) fprintf (errorlog, "megab_input_r offset: %04x\n", offset);
			return 0xff;
	}
}

void liquidk_interrupt5(int x)
{
	cpu_cause_interrupt(0,MC68000_IRQ_5);
}

static int liquidk_interrupt(void)
{
	timer_set(TIME_IN_CYCLES(200000-5000,0),0,liquidk_interrupt5);
	return MC68000_IRQ_6;
}

void taitof2_sound_w(int offset,int data)
{
	if (offset == 0)
		rastan_sound_port_w (0, data & 0xff);
	else if (offset == 2)
		rastan_sound_comm_w (0, data & 0xff);
}

int taitof2_sound_r(int offset)
{
	if (offset == 2)
		return ((rastan_sound_comm_r (0) & 0xff));
	else return 0;
}

static int sound_hack_r (int offs)
{
	return YM2610_status_port_0_A_r (0) | 1;
}

static struct MemoryReadAddress liquidk_readmem[] =
{
	{ 0x000000, 0x07ffff, MRA_ROM },
	{ 0x100000, 0x10ffff, MRA_BANK1 },
	{ 0x200000, 0x201fff, paletteram_word_r },
	{ 0x300000, 0x30000f, taitof2_input_r },
	{ 0x320000, 0x320003, taitof2_sound_r },
	{ 0x800000, 0x803fff, taitof2_background_r },
	{ 0x804000, 0x805fff, taitof2_text_r },
	{ 0x806000, 0x806fff, taitof2_characterram_r },
	{ 0x807000, 0x807fff, MRA_BANK3 },
	{ 0x808000, 0x80bfff, taitof2_foreground_r },
	{ 0x80c000, 0x80ffff, MRA_BANK4 },
	{ 0x900000, 0x90ffff, ssi_videoram_r },
	{ -1 }  /* end of table */
};

static struct MemoryWriteAddress liquidk_writemem[] =
{
	{ 0x000000, 0x07ffff, MWA_ROM },
	{ 0x100000, 0x10ffff, MWA_BANK1, &taitof2_ram },
	{ 0x200000, 0x201fff, paletteram_RRRRGGGGBBBBxxxx_word_w, &paletteram, &f2_paletteram_size },
	{ 0x300000, 0x300001, MWA_NOP }, /* irq ack? liquidk */
	{ 0x320000, 0x320003, taitof2_sound_w },
	{ 0x800000, 0x803fff, taitof2_background_w, &f2_backgroundram, &f2_backgroundram_size }, /* background layer */
	{ 0x804000, 0x805fff, taitof2_text_w, &f2_textram, &f2_textram_size }, /* text layer */
	{ 0x806000, 0x806fff, taitof2_characterram_w, &taitof2_characterram, &f2_characterram_size },
	{ 0x807000, 0x807fff, MWA_BANK3 }, /* unused? */
	{ 0x808000, 0x80bfff, taitof2_foreground_w, &f2_foregroundram, &f2_foregroundram_size }, /* foreground layer */
	{ 0x80c000, 0x80ffff, MWA_BANK4 }, /* unused? */
	{ 0x820000, 0x820005, MWA_BANK5, &taitof2_scrollx },
	{ 0x820006, 0x82000b, MWA_BANK6, &taitof2_scrolly },
	{ 0x900000, 0x90ffff, ssi_videoram_w, &videoram, &videoram_size  },
	{ 0xb00002, 0xb00003, MWA_NOP },	/* watchdog ?? liquidk */
	{ -1 }  /* end of table */
};

static struct MemoryReadAddress growl_readmem[] =
{
	{ 0x000000, 0x0fffff, MRA_ROM },
	{ 0x100000, 0x10ffff, MRA_BANK1 },
	{ 0x200000, 0x201fff, paletteram_word_r },
	{ 0x300000, 0x30000f, growl_dsw_r },
	{ 0x320000, 0x32000f, growl_input_r },
	{ 0x400000, 0x400003, ssi_sound_r },
	{ 0x508000, 0x50800f, input_port_5_r }, /* IN3 */
	{ 0x50c000, 0x50c00f, input_port_6_r }, /* IN4 */
	{ 0x800000, 0x803fff, taitof2_background_r },
	{ 0x804000, 0x805fff, taitof2_text_r },
	{ 0x806000, 0x806fff, taitof2_characterram_r },
	{ 0x807000, 0x807fff, MRA_BANK3 },
	{ 0x808000, 0x80bfff, taitof2_foreground_r },
	{ 0x80c000, 0x80ffff, MRA_BANK4 },
	{ 0x900000, 0x90ffff, ssi_videoram_r },
	{ -1 }  /* end of table */
};

static struct MemoryWriteAddress growl_writemem[] =
{
	{ 0x000000, 0x0fffff, MWA_ROM },
	{ 0x100000, 0x10ffff, MWA_BANK1, &taitof2_ram },
	{ 0x200000, 0x201fff, paletteram_RRRRGGGGBBBBxxxx_word_w, &paletteram, &f2_paletteram_size },
	{ 0x340000, 0x340001, MWA_NOP }, /* irq ack? growl */
	{ 0x400000, 0x400003, ssi_sound_w },
	{ 0x500000, 0x50000f, taitof2_spritebank_w },
	{ 0x800000, 0x803fff, taitof2_background_w, &f2_backgroundram, &f2_backgroundram_size }, /* background layer */
	{ 0x804000, 0x805fff, taitof2_text_w, &f2_textram, &f2_textram_size }, /* text layer */
	{ 0x806000, 0x806fff, taitof2_characterram_w, &taitof2_characterram, &f2_characterram_size },
	{ 0x807000, 0x807fff, MWA_BANK3 }, /* unused? */
	{ 0x808000, 0x80bfff, taitof2_foreground_w, &f2_foregroundram, &f2_foregroundram_size }, /* foreground layer */
	{ 0x80c000, 0x80ffff, MWA_BANK4 }, /* unused? */
	{ 0x820000, 0x820005, MWA_BANK5, &taitof2_scrollx },
	{ 0x820006, 0x82000b, MWA_BANK6, &taitof2_scrolly },
	{ 0x900000, 0x90ffff, ssi_videoram_w, &videoram, &videoram_size  },
	{ 0xb00000, 0xb00001, MWA_NOP },	/* watchdog ?? growl */
	{ -1 }  /* end of table */
};

static struct MemoryReadAddress megab_readmem[] =
{
	{ 0x000000, 0x0fffff, MRA_ROM },
	{ 0x100000, 0x100003, ssi_sound_r },
	{ 0x120000, 0x12000f, megab_input_r },
	{ 0x180000, 0x180fff, cchip1_r },
	{ 0x200000, 0x20ffff, MRA_BANK1 },
	{ 0x300000, 0x301fff, paletteram_word_r },
	{ 0x600000, 0x603fff, taitof2_background_r },
	{ 0x604000, 0x605fff, taitof2_text_r },
	{ 0x606000, 0x606fff, taitof2_characterram_r },
	{ 0x607000, 0x607fff, MRA_BANK3 },
	{ 0x608000, 0x60bfff, taitof2_foreground_r },
	{ 0x60c000, 0x60ffff, MRA_BANK4 },
	{ 0x610000, 0x61ffff, MRA_BANK7 }, /* unused? */
	{ 0x800000, 0x80ffff, ssi_videoram_r },
	{ -1 }  /* end of table */
};

static struct MemoryWriteAddress megab_writemem[] =
{
	{ 0x000000, 0x0fffff, MWA_ROM },
	{ 0x200000, 0x20ffff, MWA_BANK1 },
	{ 0x300000, 0x301fff, paletteram_RRRRGGGGBBBBxxxx_word_w, &paletteram, &f2_paletteram_size },
	{ 0x100000, 0x100003, ssi_sound_w },
	{ 0x120000, 0x120001, MWA_NOP }, /* irq ack? */
	{ 0x180000, 0x180fff, cchip1_w },
	{ 0x400000, 0x400001, MWA_NOP },	/* watchdog ?? */
	{ 0x600000, 0x603fff, taitof2_background_w, &f2_backgroundram, &f2_backgroundram_size }, /* background layer */
	{ 0x604000, 0x605fff, taitof2_text_w, &f2_textram, &f2_textram_size }, /* text layer */
	{ 0x606000, 0x606fff, taitof2_characterram_w, &taitof2_characterram, &f2_characterram_size },
	{ 0x607000, 0x607fff, MWA_BANK3 }, /* unused? */
	{ 0x608000, 0x60bfff, taitof2_foreground_w, &f2_foregroundram, &f2_foregroundram_size }, /* foreground layer */
	{ 0x60c000, 0x60ffff, MWA_BANK4 }, /* unused? */
	{ 0x610000, 0x61ffff, MWA_BANK7 }, /* unused? */
	{ 0x620000, 0x620005, MWA_BANK5, &taitof2_scrollx },
	{ 0x620006, 0x62000b, MWA_BANK6, &taitof2_scrolly },
	{ 0x800000, 0x80ffff, ssi_videoram_w, &videoram, &videoram_size  },
	{ -1 }  /* end of table */
};


static struct MemoryReadAddress sound_readmem[] =
{
	{ 0x0000, 0x3fff, MRA_ROM },
	{ 0x4000, 0x7fff, MRA_BANK2 },
	{ 0xc000, 0xdfff, MRA_RAM },
	{ 0xe000, 0xe000, sound_hack_r },
//	{ 0xe000, 0xe000, YM2610_status_port_0_A_r },
	{ 0xe001, 0xe001, YM2610_read_port_0_r },
	{ 0xe002, 0xe002, YM2610_status_port_0_B_r },
	{ 0xe200, 0xe200, MRA_NOP },
	{ 0xe201, 0xe201, r_rd_a001 },
	{ 0xea00, 0xea00, MRA_NOP },
	{ -1 }  /* end of table */
};

static struct MemoryWriteAddress sound_writemem[] =
{
	{ 0x0000, 0x7fff, MWA_ROM },
	{ 0xc000, 0xdfff, MWA_RAM },
	{ 0xe000, 0xe000, YM2610_control_port_0_A_w },
	{ 0xe001, 0xe001, YM2610_data_port_0_A_w },
	{ 0xe002, 0xe002, YM2610_control_port_0_B_w },
	{ 0xe003, 0xe003, YM2610_data_port_0_B_w },
	{ 0xe200, 0xe200, r_wr_a000 },
	{ 0xe201, 0xe201, r_wr_a001 },
	{ 0xe400, 0xe403, MWA_NOP }, /* pan */
	{ 0xee00, 0xee00, MWA_NOP }, /* ? */
	{ 0xf000, 0xf000, MWA_NOP }, /* ? */
	{ 0xf200, 0xf200, bankswitch_w },	/* ?? */
	{ -1 }  /* end of table */
};


INPUT_PORTS_START( liquidk )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1)

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2)

	PORT_START      /* IN2 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN3 )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )

	PORT_START /* DSW A */
	PORT_DIPNAME( 0x01, 0x00, DEF_STR( Cabinet ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Upright ) )
	PORT_DIPSETTING(    0x01, DEF_STR( Cocktail ) )
	PORT_DIPNAME( 0x02, 0x02, DEF_STR( Flip_Screen ) )
	PORT_DIPSETTING(    0x02, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_SERVICE( 0x04, IP_ACTIVE_LOW )
	PORT_DIPNAME( 0x08, 0x08, DEF_STR( Demo_Sounds ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x08, DEF_STR( On ) )
	PORT_DIPNAME( 0x30, 0x30, DEF_STR( Coin_A ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 4C_1C ) )
	PORT_DIPSETTING(    0x10, DEF_STR( 3C_1C ) )
	PORT_DIPSETTING(    0x20, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0x30, DEF_STR( 1C_1C ) )
	PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( Coin_B ) )
	PORT_DIPSETTING(    0xc0, DEF_STR( 1C_2C ) )
	PORT_DIPSETTING(    0x80, DEF_STR( 1C_3C ) )
	PORT_DIPSETTING(    0x40, DEF_STR( 1C_4C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 1C_6C ) )

	PORT_START /* DSW B */
	PORT_DIPNAME( 0x01, 0x01, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x01, DEF_STR( On ) )
	PORT_DIPNAME( 0x02, 0x02, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x02, DEF_STR( On ) )
	PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( Bonus_Life ) )
	PORT_DIPSETTING(    0x0c, "30k 100k" )
	PORT_DIPSETTING(    0x08, "30k 150k" )
	PORT_DIPSETTING(    0x04, "50k 250k" )
	PORT_DIPSETTING(    0x00, "50k 350k" )
	PORT_DIPNAME( 0x30, 0x30, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x20, "2" )
	PORT_DIPSETTING(    0x30, "3" )
	PORT_DIPSETTING(    0x00, "4" )
	PORT_DIPSETTING(    0x10, "5" )
	PORT_DIPNAME( 0x40, 0x40, "Allow Continue" )
	PORT_DIPSETTING(    0x00, DEF_STR( No ) )
	PORT_DIPSETTING(    0x40, DEF_STR( Yes ) )
	PORT_DIPNAME( 0x80, 0x80, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x80, DEF_STR( On ) )
INPUT_PORTS_END


INPUT_PORTS_START( finalb )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1)

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2)

	PORT_START      /* IN2 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN3 )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )

	PORT_START /* DSW A */
	PORT_DIPNAME( 0x01, 0x01, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x01, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x02, 0x02, "Flip Screen?" )
	PORT_DIPSETTING(    0x02, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_SERVICE( 0x04, IP_ACTIVE_LOW )
	PORT_DIPNAME( 0x08, 0x08, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x08, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x30, 0x30, DEF_STR( Coin_A ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 4C_1C ) )
	PORT_DIPSETTING(    0x10, DEF_STR( 3C_1C ) )
	PORT_DIPSETTING(    0x20, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0x30, DEF_STR( 1C_1C ) )
	PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( Coin_B ) )
	PORT_DIPSETTING(    0xc0, DEF_STR( 1C_2C ) )
	PORT_DIPSETTING(    0x80, DEF_STR( 1C_3C ) )
	PORT_DIPSETTING(    0x40, DEF_STR( 1C_4C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 1C_6C ) )

	PORT_START /* DSW B */
	PORT_DIPNAME( 0x01, 0x01, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x01, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x02, 0x02, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x02, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x0c, 0x0c, "Shields" )
	PORT_DIPSETTING(    0x00, "None" )
	PORT_DIPSETTING(    0x0c, "1" )
	PORT_DIPSETTING(    0x04, "2" )
	PORT_DIPSETTING(    0x08, "3" )
	PORT_DIPNAME( 0x10, 0x10, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x00, "2" )
	PORT_DIPSETTING(    0x10, "3" )
	PORT_DIPNAME( 0x20, 0x20, "2 Players Mode" )
	PORT_DIPSETTING(    0x00, "Alternate" )
	PORT_DIPSETTING(    0x20, "Simultaneous" )
	PORT_DIPNAME( 0x40, 0x40, "Allow Continue" )
	PORT_DIPSETTING(    0x00, DEF_STR( No ) )
	PORT_DIPSETTING(    0x40, DEF_STR( Yes ) )
	PORT_DIPNAME( 0x80, 0x80, "Allow Simultaneous Game" )
	PORT_DIPSETTING(    0x00, DEF_STR( No ) )
	PORT_DIPSETTING(    0x80, DEF_STR( Yes ) )
INPUT_PORTS_END

INPUT_PORTS_START( growl )
	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 )

	PORT_START      /* IN2 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT )
	PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_SERVICE, "Service A", KEYCODE_9, IP_JOY_NONE )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )

	PORT_START /* DSW A */
	PORT_DIPNAME( 0x01, 0x01, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x01, DEF_STR( On ) )
	PORT_DIPNAME( 0x02, 0x02, DEF_STR( Flip_Screen ) )
	PORT_DIPSETTING(    0x02, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_SERVICE( 0x04, IP_ACTIVE_LOW )
	PORT_DIPNAME( 0x08, 0x08, DEF_STR( Demo_Sounds ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x08, DEF_STR( On ) )
	PORT_DIPNAME( 0x30, 0x30, DEF_STR( Coin_A ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 4C_1C ) )
	PORT_DIPSETTING(    0x10, DEF_STR( 3C_1C ) )
	PORT_DIPSETTING(    0x20, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0x30, DEF_STR( 1C_1C ) )
	PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( Coin_B ) )
	PORT_DIPSETTING(    0xc0, DEF_STR( 1C_2C ) )
	PORT_DIPSETTING(    0x80, DEF_STR( 1C_3C ) )
	PORT_DIPSETTING(    0x40, DEF_STR( 1C_4C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 1C_6C ) )

	PORT_START /* DSW B */
	PORT_DIPNAME( 0x01, 0x01, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x01, DEF_STR( On ) )
	PORT_DIPNAME( 0x02, 0x02, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x02, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_DIPNAME( 0x04, 0x04, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x04, DEF_STR ( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR ( On) )
	PORT_DIPNAME( 0x08, 0x08, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x08, DEF_STR ( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR ( On) )
	PORT_DIPNAME( 0x30, 0x30, "Game Type" )
	PORT_DIPSETTING(    0x30, "1 or 2 Players only" )
	PORT_DIPSETTING(    0x20, "Up to 4 Players dipendent" )
	PORT_DIPSETTING(    0x10, "Up to 4 Players indipendent" )
	PORT_DIPSETTING(    0x00, "Up to 4 Players indipendent" )
	PORT_DIPNAME( 0x40, 0x40, "Unknown" )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x40, DEF_STR( On ) )
	PORT_DIPNAME( 0x80, 0x80, "Unknown" )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x80, DEF_STR( On ) )

	PORT_START      /* IN3 */
	PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 )
	PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 )
	PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 )
	PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 )
	PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 )
	PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 )
	PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER3 )
	PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_START3 )
	PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER4 )
	PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER4 )
	PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER4 )
	PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER4 )
	PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4 )
	PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4 )
	PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER4 )
	PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_START4 )

	PORT_START      /* IN4 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN3 )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN4 )
	PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_SERVICE, "Service B", KEYCODE_0, IP_JOY_NONE )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )
INPUT_PORTS_END

INPUT_PORTS_START( megab )
	PORT_START /* DSW A */
	PORT_DIPNAME( 0x01, 0x01, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x01, DEF_STR( On ) )
	PORT_DIPNAME( 0x02, 0x02, DEF_STR( Flip_Screen ) )
	PORT_DIPSETTING(    0x02, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_SERVICE( 0x04, IP_ACTIVE_LOW )
	PORT_DIPNAME( 0x08, 0x08, DEF_STR( Demo_Sounds ) )
	PORT_DIPSETTING(    0x08, DEF_STR( Off ) )
	PORT_DIPSETTING(    0x00, DEF_STR( On ) )
	PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED )

	PORT_START /* DSW B */
	PORT_DIPNAME( 0x03, 0x03, DEF_STR( Coin_A ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 4C_1C ) )
	PORT_DIPSETTING(    0x01, DEF_STR( 3C_1C ) )
	PORT_DIPSETTING(    0x02, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(    0x03, DEF_STR( 1C_1C ) )
	PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( Coin_B ) )
	PORT_DIPSETTING(    0x0c, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING(    0x08, DEF_STR( 1C_3C ) )
	PORT_DIPSETTING(    0x04, DEF_STR( 1C_4C ) )
	PORT_DIPSETTING(    0x00, DEF_STR( 1C_6C ) )
	PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED )

	PORT_START /* DSW c */
	PORT_DIPNAME( 0x03, 0x03, DEF_STR( Difficulty ) )
	PORT_DIPSETTING(    0x03, "Norm" )
	PORT_DIPSETTING(    0x02, "Easy" )
	PORT_DIPSETTING(    0x01, "Hard" )
	PORT_DIPSETTING(    0x00, "Very Hard" )
	PORT_DIPNAME( 0x0c, 0x0c, "Bonus" )
	PORT_DIPSETTING(    0x0c, "50k, 150k" )
	PORT_DIPSETTING(    0x0a, "Bonus 2??" )
	PORT_DIPSETTING(    0x08, "Bonus 3??" )
	PORT_DIPSETTING(    0x00, "Bonus 4??" )
	PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED )

	PORT_START /* DSW D */
	PORT_DIPNAME( 0x03, 0x03, DEF_STR( Lives ) )
	PORT_DIPSETTING(    0x03, "3" )
	PORT_DIPSETTING(    0x02, "2" )
	PORT_DIPSETTING(    0x01, "4" )
	PORT_DIPSETTING(    0x00, "5" )
	PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( Unknown ) )
	PORT_DIPSETTING(    0x00, "1" )
	PORT_DIPSETTING(    0x04, "2" )
	PORT_DIPSETTING(    0x08, "3" )
	PORT_DIPSETTING(    0x0c, "4" )
	PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED )

	PORT_START      /* IN0 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER1 )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER1 )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER1 )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 )

	PORT_START      /* IN1 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER2 )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 )

	PORT_START      /* IN2 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_TILT )
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN )
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN )

INPUT_PORTS_END


static struct GfxLayout tilelayout =
{
	16,16,	/* 16*16 sprites */
	16384,	/* 16384 sprites */
	4,	/* 4 bits per pixel */
	{ 0, 1, 2, 3 },
	{ 1*4, 0*4, 3*4, 2*4, 5*4, 4*4, 7*4, 6*4, 9*4, 8*4, 11*4, 10*4, 13*4, 12*4, 15*4, 14*4 },
	{ 0*64, 1*64, 2*64, 3*64, 4*64, 5*64, 6*64, 7*64, 8*64, 9*64, 10*64, 11*64, 12*64, 13*64, 14*64, 15*64 },
	128*8	/* every sprite takes 32 consecutive bytes */
};

static struct GfxLayout charlayout =
{
	8,8,	/* 8*8 characters */
	32768,	/* 32768 characters */
	4,	/* 4 bits per pixel */
	{ 0, 1, 2, 3 },
	{ 2*4, 3*4, 0*4, 1*4, 6*4, 7*4, 4*4, 5*4 },
	{ 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
	32*8	/* every sprite takes 32 consecutive bytes */
};

static struct GfxLayout charlayout2 =
{
	8,8,	/* 8*8 characters */
	256,	/* 256 characters */
	2,	/* 4 bits per pixel */
	{ 0, 8 },
	{ 0, 1, 2, 3, 4, 5, 6, 7 },
	{ 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
	16*8	/* every sprite takes 16 consecutive bytes */
};

#define NUM_TILES 8192
static struct GfxLayout finalb_tilelayout =
{
	16,16,	/* 16*16 sprites */
	NUM_TILES,	/* 8192 sprites */
	4,	/* 4 bits per pixel */
	{ 0, 1, 2, 3 },
	{
		1*4, 0*4, NUM_TILES*64*8 + 1*4, NUM_TILES*64*8 + 0*4,
		3*4, 2*4, NUM_TILES*64*8 + 3*4, NUM_TILES*64*8 + 2*4,
		5*4, 4*4, NUM_TILES*64*8 + 5*4, NUM_TILES*64*8 + 4*4,
		7*4, 6*4, NUM_TILES*64*8 + 7*4, NUM_TILES*64*8 + 6*4
	},
	{ 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32, 8*32, 9*32, 10*32, 11*32, 12*32, 13*32, 14*32, 15*32 },
	64*8	/* every sprite takes 64 consecutive bytes */
};
#undef NUM_TILES

#define NUM_CHARS 8192
static struct GfxLayout finalb_charlayout =
{
	8,8,	/* 8*8 characters */
	NUM_CHARS,	/* 8192 characters */
	4,	/* 4 bits per pixel */
	{ 0, 1, 2, 3 },
	{
		NUM_CHARS*16*8 + 0*4, NUM_CHARS*16*8 + 1*4, 0*4, 1*4,
		NUM_CHARS*16*8 + 2*4, NUM_CHARS*16*8 + 3*4, 2*4, 3*4
	},
	{ 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
	16*8	/* every char takes 16 consecutive bytes */
};
#undef NUM_CHARS

static struct GfxDecodeInfo gfxdecodeinfo[] =
{
	{ REGION_GFX2, 0x0, &tilelayout,  0, 256 },  /* sprites & playfield */
	{ REGION_GFX1, 0x0, &charlayout,  0, 256 },  /* sprites & playfield */
    { 0, 0x000000, &charlayout2, 0, 256 },	/* the game dynamically modifies this */
	{ -1 } /* end of array */
};

static struct GfxDecodeInfo finalb_gfxdecodeinfo[] =
{
	{ REGION_GFX2, 0x0, &finalb_tilelayout,  0, 256 },  /* sprites & playfield */
	{ REGION_GFX1, 0x0, &finalb_charlayout,  0, 256 },  /* sprites & playfield */
    { 0, 0x000000, &charlayout2, 0, 256 },	/* the game dynamically modifies this */
	{ -1 } /* end of array */
};



/* handler called by the YM2610 emulator when the internal timers cause an IRQ */
static void irqhandler(int irq)
{
	cpu_set_irq_line(1,0,irq ? ASSERT_LINE : CLEAR_LINE);
}

static struct YM2610interface ym2610_interface =
{
	1,	/* 1 chip */
	8000000,	/* 8 MHz ?????? */
	{ 30 },
	{ 0 },
	{ 0 },
	{ 0 },
	{ 0 },
	{ irqhandler },
	{ REGION_SOUND1 },
	{ REGION_SOUND1 },
	{ YM3012_VOL(60,MIXER_PAN_LEFT,60,MIXER_PAN_RIGHT) }
};



static struct MachineDriver machine_driver_liquidk =
{
	/* basic machine hardware */
	{
		{
			CPU_M68000,
			12000000,	/* 12 MHz ? */
			liquidk_readmem, liquidk_writemem, 0, 0,
			liquidk_interrupt, 1
		},
		{
			CPU_Z80 | CPU_AUDIO_CPU,
			4000000,	/* 4 MHz ??? */
			sound_readmem, sound_writemem, 0, 0,
			ignore_interrupt, 0	/* IRQs are triggered by the YM2610 */
		}
	},
	60, 2000,	/* frames per second, vblank duration hand tuned to avoid flicker */
	1,
	0,

	/* video hardware */
	40*8, 32*8, { 0*8, 40*8-1, 2*8, 30*8-1 },

	gfxdecodeinfo,
	4096, 4096,
	0,

	VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_AFTER_VBLANK,
	0,
	taitof2_vh_start,
	taitof2_vh_stop,
	taitof2_vh_screenrefresh,

	/* sound hardware */
	SOUND_SUPPORTS_STEREO,0,0,0,
	{
		{
			SOUND_YM2610,
			&ym2610_interface
		}
	}
};

static struct MachineDriver machine_driver_finalb =
{
	/* basic machine hardware */
	{
		{
			CPU_M68000,
			12000000,	/* 12 MHz ??? */
			liquidk_readmem, liquidk_writemem, 0, 0,
			liquidk_interrupt, 1
		},
		{
			CPU_Z80 | CPU_AUDIO_CPU,
			4000000,	/* 4 MHz ??? */
			sound_readmem, sound_writemem, 0, 0,
			ignore_interrupt, 0	/* IRQs are triggered by the YM2610 */
		}
	},
	60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
	1,
	0,

	/* video hardware */
	40*8, 32*8, { 0*8, 40*8-1, 2*8, 30*8-1 },

	finalb_gfxdecodeinfo,
	4096, 4096,
	0,

	VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
	0,
	taitof2_vh_start,
	taitof2_vh_stop,
	taitof2_vh_screenrefresh,

	/* sound hardware */
	SOUND_SUPPORTS_STEREO,0,0,0,
	{
		{
			SOUND_YM2610,
			&ym2610_interface
		}
	}
};

static struct MachineDriver machine_driver_growl =
{
	/* basic machine hardware */
	{
		{
			CPU_M68000,
			12000000,	/* 12 MHz ??? */
			growl_readmem, growl_writemem, 0, 0,
			liquidk_interrupt, 1
		},
		{
			CPU_Z80 | CPU_AUDIO_CPU,
			4000000,	/* 4 MHz ??? */
			sound_readmem, sound_writemem, 0, 0,
			ignore_interrupt, 0	/* IRQs are triggered by the YM2610 */
		}
	},
	60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
	1,
	0,

	/* video hardware */
	40*8, 32*8, { 0*8, 40*8-1, 2*8, 30*8-1 },

	gfxdecodeinfo,
	4096, 4096,
	0,

	VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
	0,
	taitof2_vh_start,
	taitof2_vh_stop,
	taitof2_vh_screenrefresh,

	/* sound hardware */
	SOUND_SUPPORTS_STEREO,0,0,0,
	{
		{
			SOUND_YM2610,
			&ym2610_interface
		}
	}
};

static struct MachineDriver machine_driver_megab =
{
	/* basic machine hardware */
	{
		{
			CPU_M68000,
			12000000,	/* 12 MHz ??? */
			megab_readmem, megab_writemem, 0, 0,
			liquidk_interrupt, 1
		},
		{
			CPU_Z80 | CPU_AUDIO_CPU,
			4000000,	/* 4 MHz ??? */
			sound_readmem, sound_writemem, 0, 0,
			ignore_interrupt, 0	/* IRQs are triggered by the YM2610 */
		}
	},
	60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
	1,
	cchip1_init_machine,

	/* video hardware */
	40*8, 32*8, { 0*8, 40*8-1, 2*8, 30*8-1 },

	gfxdecodeinfo,
	4096, 4096,
	0,

	VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
	0,
	taitof2_vh_start,
	taitof2_vh_stop,
	taitof2_vh_screenrefresh,

	/* sound hardware */
	SOUND_SUPPORTS_STEREO,0,0,0,
	{
		{
			SOUND_YM2610,
			&ym2610_interface
		}
	}
};



/***************************************************************************

  Game driver(s)

***************************************************************************/

ROM_START( finalb )
	ROM_REGION( 0x40000, REGION_CPU1 )     /* 256k for 68000 code */
	ROM_LOAD_EVEN( "fb_09.rom",  0x00000, 0x20000, 0x632f1ecd )
	ROM_LOAD_ODD ( "fb_17.rom",  0x00000, 0x20000, 0xe91b2ec9 )
//	ROM_LOAD_EVEN( "fb_m01.rom", 0x40000, 0x80000, 0xb63003c4 ) /* palette? */
//	ROM_LOAD_ODD ( "fb_m02.rom", 0x40000, 0x80000, 0x5802ee3c ) /* palette? */

	ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE)      /* temporary space for graphics (disposed after conversion) */
	ROM_LOAD( "fb_m06.rom", 0x000000, 0x020000, 0xfc450a25 )
	ROM_LOAD( "fb_m07.rom", 0x020000, 0x020000, 0xec3df577 )

	ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE)      /* temporary space for graphics (disposed after conversion) */
	ROM_LOAD( "fb_m03.rom", 0x000000, 0x080000, 0xdaa11561 ) /* sprites */
	ROM_LOAD( "fb_m04.rom", 0x080000, 0x080000, 0x6346f98e ) /* sprites */
//	ROM_LOAD( "fb_m05.rom", 0x000000, 0x080000, 0xaa90b93a ) /* palette? */

	ROM_REGION( 0x1c000, REGION_CPU2 )      /* sound cpu */
	ROM_LOAD( "fb_10.rom",   0x00000, 0x04000, 0xa38aaaed )
	ROM_CONTINUE(            0x10000, 0x0c000 ) /* banked stuff */

	ROM_REGION( 0x40000, REGION_SOUND1 )	/* ADPCM samples */
	ROM_LOAD ( "fb_m06.rom", 0x00000, 0x20000, 0xfc450a25 )
	ROM_LOAD ( "fb_m07.rom", 0x20000, 0x20000, 0xec3df577 )
ROM_END

ROM_START( megab )
	ROM_REGION( 0x100000, REGION_CPU1 )     /* 256k for 68000 code */
	ROM_LOAD_EVEN( "c11-07",  0x00000, 0x20000, 0x11d228b6 )
	ROM_LOAD_ODD ( "c11-08",  0x00000, 0x20000, 0xa79d4dca )

	ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE)      /* temporary space for graphics (disposed after conversion) */
	ROM_LOAD( "c11-01", 0x000000, 0x080000, 0xfd1ea532 )
//	ROM_LOAD( "c11-02", 0x180000, 0x080000, 0x451cc187 )

	ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE)      /* temporary space for graphics (disposed after conversion) */
	ROM_LOAD( "c11-03", 0x000000, 0x080000, 0x46718c7a )
	ROM_LOAD( "c11-04", 0x080000, 0x080000, 0x663f33cc )
	ROM_LOAD( "c11-05", 0x100000, 0x080000, 0x733e6d8e )

	ROM_REGION( 0x1c000, REGION_CPU2 )      /* sound cpu */
	ROM_LOAD( "c11-12", 0x00000, 0x04000, 0xb11094f1 )
	ROM_CONTINUE(       0x10000, 0x0c000 ) /* banked stuff */

	ROM_REGION( 0x40000, REGION_SOUND1 )	/* ADPCM samples */
	ROM_LOAD( "c11-11", 0x00000, 0x20000, 0x263ecbf9 )
	ROM_LOAD( "c11-06", 0x20000, 0x20000, 0x7c249894 )
ROM_END

ROM_START( liquidk )
	ROM_REGION( 0x80000, REGION_CPU1 )     /* 512k for 68000 code */
	ROM_LOAD_EVEN( "lq09.bin",  0x00000, 0x20000, 0x6ae09eb9 )
	ROM_LOAD_ODD ( "lq11.bin",  0x00000, 0x20000, 0x42d2be6e )
	ROM_LOAD_EVEN( "lq10.bin",  0x40000, 0x20000, 0x50bef2e0 )
	ROM_LOAD_ODD ( "lq12.bin",  0x40000, 0x20000, 0xcb16bad5 )

	ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE)      /* temporary space for graphics (disposed after conversion) */
	ROM_LOAD( "lk_scr.bin",  0x000000, 0x080000, 0xc3364f9b )

	ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE)      /* temporary space for graphics (disposed after conversion) */
	ROM_LOAD( "lk_obj0.bin", 0x000000, 0x080000, 0x67cc3163 )
	ROM_LOAD( "lk_obj1.bin", 0x080000, 0x080000, 0xd2400710 )

	ROM_REGION( 0x1c000, REGION_CPU2 )      /* sound cpu */
	ROM_LOAD( "lq08.bin",    0x00000, 0x04000, 0x413c310c )
	ROM_CONTINUE(            0x10000, 0x0c000 ) /* banked stuff */

	ROM_REGION( 0x80000, REGION_SOUND1 )	/* ADPCM samples */
	ROM_LOAD( "lk_snd.bin",  0x00000, 0x80000, 0x474d45a4 )
ROM_END

ROM_START( liquidku )
	ROM_REGION( 0x80000, REGION_CPU1 )     /* 512k for 68000 code */
	ROM_LOAD_EVEN( "lq09.bin",  0x00000, 0x20000, 0x6ae09eb9 )
	ROM_LOAD_ODD ( "lq11.bin",  0x00000, 0x20000, 0x42d2be6e )
	ROM_LOAD_EVEN( "lq10.bin",  0x40000, 0x20000, 0x50bef2e0 )
	ROM_LOAD_ODD ( "lq14.bin",  0x40000, 0x20000, 0xbc118a43 )

	ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE)      /* temporary space for graphics (disposed after conversion) */
	ROM_LOAD( "lk_scr.bin",  0x000000, 0x080000, 0xc3364f9b )

	ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE)      /* temporary space for graphics (disposed after conversion) */
	ROM_LOAD( "lk_obj0.bin", 0x000000, 0x080000, 0x67cc3163 )
	ROM_LOAD( "lk_obj1.bin", 0x080000, 0x080000, 0xd2400710 )

	ROM_REGION( 0x1c000, REGION_CPU2 )      /* sound cpu */
	ROM_LOAD( "lq08.bin",    0x00000, 0x04000, 0x413c310c )
	ROM_CONTINUE(            0x10000, 0x0c000 ) /* banked stuff */

	ROM_REGION( 0x80000, REGION_SOUND1 )	/* ADPCM samples */
	ROM_LOAD( "lk_snd.bin",  0x00000, 0x80000, 0x474d45a4 )
ROM_END

ROM_START( mizubaku )
	ROM_REGION( 0x80000, REGION_CPU1 )     /* 512k for 68000 code */
	ROM_LOAD_EVEN( "lq09.bin",  0x00000, 0x20000, 0x6ae09eb9 )
	ROM_LOAD_ODD ( "lq11.bin",  0x00000, 0x20000, 0x42d2be6e )
	ROM_LOAD_EVEN( "lq10.bin",  0x40000, 0x20000, 0x50bef2e0 )
	ROM_LOAD_ODD ( "c49-13",    0x40000, 0x20000, 0x2518dbf9 )

	ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE)      /* temporary space for graphics (disposed after conversion) */
	ROM_LOAD( "lk_scr.bin",  0x000000, 0x080000, 0xc3364f9b )

	ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE)      /* temporary space for graphics (disposed after conversion) */
	ROM_LOAD( "lk_obj0.bin", 0x000000, 0x080000, 0x67cc3163 )
	ROM_LOAD( "lk_obj1.bin", 0x080000, 0x080000, 0xd2400710 )

	ROM_REGION( 0x1c000, REGION_CPU2 )      /* sound cpu */
	ROM_LOAD( "lq08.bin",    0x00000, 0x04000, 0x413c310c )
	ROM_CONTINUE(            0x10000, 0x0c000 ) /* banked stuff */

	ROM_REGION( 0x80000, REGION_SOUND1 )	/* ADPCM samples */
	ROM_LOAD( "lk_snd.bin",  0x00000, 0x80000, 0x474d45a4 )
ROM_END

ROM_START( growl )
	ROM_REGION( 0x100000, REGION_CPU1 )     /* 1024k for 68000 code */
	ROM_LOAD_EVEN( "growl_10.rom",  0x00000, 0x40000, 0xca81a20b )
	ROM_LOAD_ODD ( "growl_08.rom",  0x00000, 0x40000, 0xaa35dd9e )
	ROM_LOAD_EVEN( "growl_11.rom",  0x80000, 0x40000, 0xee3bd6d5 )
	ROM_LOAD_ODD ( "growl_14.rom",  0x80000, 0x40000, 0xb6c24ec7 )

	ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE)      /* temporary space for graphics (disposed after conversion) */
	ROM_LOAD( "growl_01.rom", 0x000000, 0x100000, 0x3434ce80 ) /* characters */

	ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE)      /* temporary space for graphics (disposed after conversion) */
	ROM_LOAD( "growl_03.rom", 0x000000, 0x100000, 0x1a0d8951 ) /* sprites */
	ROM_LOAD( "growl_02.rom", 0x100000, 0x100000, 0x15a21506 ) /* sprites */

	ROM_REGION( 0x1c000, REGION_CPU2 )      /* sound cpu */
	ROM_LOAD( "growl_12.rom", 0x00000, 0x04000, 0xbb6ed668 )
	ROM_CONTINUE(             0x10000, 0x0c000 ) /* banked stuff */

	ROM_REGION( 0x100000, REGION_SOUND1 )	/* ADPCM samples */
	ROM_LOAD( "growl_04.rom", 0x000000, 0x100000, 0x2d97edf2 )

	ROM_REGION( 0x080000, REGION_SOUND2 )	/* ADPCM samples */
	ROM_LOAD( "growl_05.rom", 0x000000, 0x080000, 0xe29c0828 )
ROM_END

ROM_START( growlu )
	ROM_REGION( 0x100000, REGION_CPU1 )     /* 1024k for 68000 code */
	ROM_LOAD_EVEN( "growl_10.rom",  0x00000, 0x40000, 0xca81a20b )
	ROM_LOAD_ODD ( "growl_08.rom",  0x00000, 0x40000, 0xaa35dd9e )
	ROM_LOAD_EVEN( "growl_11.rom",  0x80000, 0x40000, 0xee3bd6d5 )
	ROM_LOAD_ODD ( "c74-13.rom",    0x80000, 0x40000, 0xc1c57e51 )

	ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE)      /* temporary space for graphics (disposed after conversion) */
	ROM_LOAD( "growl_01.rom", 0x000000, 0x100000, 0x3434ce80 ) /* characters */

	ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE)      /* temporary space for graphics (disposed after conversion) */
	ROM_LOAD( "growl_03.rom", 0x000000, 0x100000, 0x1a0d8951 ) /* sprites */
	ROM_LOAD( "growl_02.rom", 0x100000, 0x100000, 0x15a21506 ) /* sprites */

	ROM_REGION( 0x1c000, REGION_CPU2 )      /* sound cpu */
	ROM_LOAD( "growl_12.rom", 0x00000, 0x04000, 0xbb6ed668 )
	ROM_CONTINUE(             0x10000, 0x0c000 ) /* banked stuff */

	ROM_REGION( 0x100000, REGION_SOUND1 )	/* ADPCM samples */
	ROM_LOAD( "growl_04.rom", 0x000000, 0x100000, 0x2d97edf2 )

	ROM_REGION( 0x080000, REGION_SOUND2 )	/* ADPCM samples */
	ROM_LOAD( "growl_05.rom", 0x000000, 0x080000, 0xe29c0828 )
ROM_END

ROM_START( runark )
	ROM_REGION( 0x100000, REGION_CPU1 )     /* 1024k for 68000 code */
	ROM_LOAD_EVEN( "growl_10.rom",  0x00000, 0x40000, 0xca81a20b )
	ROM_LOAD_ODD ( "growl_08.rom",  0x00000, 0x40000, 0xaa35dd9e )
	ROM_LOAD_EVEN( "growl_11.rom",  0x80000, 0x40000, 0xee3bd6d5 )
	ROM_LOAD_ODD ( "c74_09.14",     0x80000, 0x40000, 0x58cc2feb )

	ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE)      /* temporary space for graphics (disposed after conversion) */
	ROM_LOAD( "growl_01.rom", 0x000000, 0x100000, 0x3434ce80 ) /* characters */

	ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE)      /* temporary space for graphics (disposed after conversion) */
	ROM_LOAD( "growl_03.rom", 0x000000, 0x100000, 0x1a0d8951 ) /* sprites */
	ROM_LOAD( "growl_02.rom", 0x100000, 0x100000, 0x15a21506 ) /* sprites */

	ROM_REGION( 0x1c000, REGION_CPU2 )      /* sound cpu */
	ROM_LOAD( "growl_12.rom", 0x00000, 0x04000, 0xbb6ed668 )
	ROM_CONTINUE(             0x10000, 0x0c000 ) /* banked stuff */

	ROM_REGION( 0x100000, REGION_SOUND1 )	/* ADPCM samples */
	ROM_LOAD( "growl_04.rom", 0x000000, 0x100000, 0x2d97edf2 )

	ROM_REGION( 0x080000, REGION_SOUND2 )	/* ADPCM samples */
	ROM_LOAD( "growl_05.rom", 0x000000, 0x080000, 0xe29c0828 )
ROM_END



GAMEX( 1988, finalb,   0,       finalb,  finalb,  0, ROT0,   "Taito", "Final Blow", GAME_NO_COCKTAIL )
GAMEX( 1989, megab,    0,       megab,   megab,   0, ROT0,   "Taito", "Mega Blade", GAME_NO_COCKTAIL )
GAMEX( 1990, liquidk,  0,       liquidk, liquidk, 0, ROT180, "Taito Corporation Japan", "Liquid Kids (World)", GAME_NO_COCKTAIL )
GAMEX( 1990, liquidku, liquidk, liquidk, liquidk, 0, ROT180, "Taito America Corporation", "Liquid Kids (US)", GAME_NO_COCKTAIL )
GAMEX( 1990, mizubaku, liquidk, liquidk, liquidk, 0, ROT180, "Taito Corporation", "Mizubaku Daibouken (Japan)", GAME_NO_COCKTAIL )
GAMEX( 1990, growl,    0,       growl,   growl,   0, ROT0,   "Taito Corporation Japan", "Growl (World)", GAME_NO_COCKTAIL )
GAMEX( 1990, growlu,   growl,   growl,   growl,   0, ROT0,   "Taito America Corporation", "Growl (US)", GAME_NO_COCKTAIL )
GAMEX( 1990, runark,   growl,   growl,   growl,   0, ROT0,   "Taito Corporation", "Runark (Japan)", GAME_NO_COCKTAIL )
