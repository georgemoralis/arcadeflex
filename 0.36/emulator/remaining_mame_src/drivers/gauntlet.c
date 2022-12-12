/***************************************************************************

Gauntlet Memory Map
-----------------------------------

driver by Aaron Giles

GAUNTLET 68010 MEMORY MAP

Function                           Address        R/W  DATA
-------------------------------------------------------------
Program ROM/Operating System       000000-00FFFF  R    D0-D15
Program ROM/SLAPSTIC               038000-03FFFF  R    D0-D15
Program ROM/Main                   040000-07FFFF  R    D0-D15
Spare RAM                          800000-801FFF  R/W  D0-D15

EEPROM                             802001-802FFF  R/W  D7-D0

Player 1 Input (See detail below)  803001         R    D0-D71
Player 2 Input                     803003         R    D0-D7
Player 3 Input                     803005         R    D0-D7
Player 4 Input                     803007         R    D0-D7

Player Inputs:
  Joystick Up                                          D7
  Joystick Down                                        D6
  Joystick Left                                        D5
  Joystick Right                                       D4
  Spare                                                D3
  Spare                                                D2
  Fire                                                 D1
  Magic/Start                                          D0

VBLANK (Active Low)                803009         R    D6
Outbut/Buffer Full (@803170)       803009         R    D5
 (Active High)
Input/Buffer Full (@80300F)        803009         R    D4
 (Active High)
Self-Test (Active Low)             803009         R    D3

Read Sound Processor (6502)        80300F         R    D0-D7

Watchdog (128 msec. timeout)       803100         W    xx

LED-1 (Low On)                     803121         W    D0
LED-2 (Low-On)                     803123         W    D0
LED-3 (Low On)                     803125         W    D0
LED-4 (Low On)                     803127         W    D0
Sound Processor Reset (Low Reset)  80312F         W    D0

VBlank Acknowledge                 803140         W    xx
Unlock EEPROM                      803150         W    xx
Write Sound Processor (6502)       803171         W    D0-D7

Playfield RAM                      900000-901FFF  R/W  D0-D15
Motion Object Picture              902000-9027FF  R/W  D0-D15
Motion Object Horizontal Position  902800-902FFF  R/W  D0-D15
Motion Object Vertical Position    903000-9037FF  R/W  D0-D15
Motion Object Link                 903800-903FFF  R/W  D0-D15
Spare RAM                          904000-904FFF  R/W  D0-D15
Alphanumerics RAM                  905000-905FFF  R/W  D0-D15

Playfield Vertical Scroll          905F6E,905F6F  R/W  D7-D15
Playfield ROM Bank Select          905F6F         R/W

Color RAM Alpha                    910000-9101FF  R/W  D0-D15
Color RAM Motion Object            910200-9103FF  R/W  D0-D15
Color RAM Playfield Shadow         910400-9104FF  R/W  D0-D15
Color RAM Playfield                910500-9105FF  R/W  D0-D15
Color RAM (Spare)                  910600-9107FF  R/W  D0-D15

Playfield Horizontal Scroll        930000,930001  W    D0-D8

NOTE: All addresses can be accessed in byte or word mode.


GAUNTLET 6502 MEMORY MAP

Function                                  Address     R/W  Data
---------------------------------------------------------------
Program RAM                               0000-0FFF   R/W  D0-D7

Write 68010 Port (Outbut Buffer)          1000        W    D0-D7
Read 68010 Port (Input Buffer)            1010        R    D0-D7

Audio Mix:
 Speech Mix                               1020        W    D5-D7
 Effects Mix                              1020        W    D3,D4
 Music Mix                                1020        W    D0-D2

Coin 1 (Left)                             1020        R    D3
Coin 2                                    1020        R    D2
Coin 3                                    1020        R    D1
Coin 4 (Right)                            1020        R    D0

Data Available (@ 1010) (Active High)     1030-1030   R    D7
Output Buffer Full (@1000) (Active High)  1030        R    D6
Speech Ready (Active Low)                 1030        R    D5
Self-Test (Active Low)                    1030        R    D4

Music Reset (Low Reset)                   1030        W    D7
Speech Write (Active Low)                 1031        W    D7
Speech Reset (Active Low)                 1032        W    D7
Speech Squeak (Low = 650KHz Clock)        1033        W    D7
Coin Counter Right (Active High)          1034        W    D7
Coin Counter Left (Active High)           1035        W    D7

Effects                                   1800-180F   R/W  D0-D7
Music                                     1810-1811   R/W  D0-D7
Speech                                    1820        W    D0-D7
Interrupt Acknowledge                     1830        R/W  xx

Program ROM (48K bytes)                   4000-FFFF   R    D0-D7
***************************************************************************/



#include "driver.h"
#include "machine/atarigen.h"
#include "vidhrdw/generic.h"


extern int vindctr2_screen_refresh;

void gauntlet_playfieldram_w(int offset, int data);
void gauntlet_hscroll_w(int offset, int data);
void gauntlet_vscroll_w(int offset, int data);

void gauntlet_scanline_update(int scanline);

int gauntlet_vh_start(void);
void gauntlet_vh_stop(void);
void gauntlet_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh);


static UINT8 *speed_check;
static UINT32 last_speed_check;

static UINT8 speech_val;
static UINT8 last_speech_write;
static UINT8 speech_squeak;

static UINT16 last_sound_reset;



/*************************************
 *
 *	Initialization of globals.
 *
 *************************************/

static void update_interrupts(void)
{
	int newstate = 0;

	if (atarigen_video_int_state)
		newstate |= 4;
	if (atarigen_sound_int_state)
		newstate |= 6;

	if (newstate)
		cpu_set_irq_line(0, newstate, ASSERT_LINE);
	else
		cpu_set_irq_line(0, 7, CLEAR_LINE);
}


static void scanline_update(int scanline)
{
	gauntlet_scanline_update(scanline);

	/* sound IRQ is on 32V */
	if (scanline % 32 == 0)
	{
		if (scanline & 32)
			atarigen_6502_irq_gen();
		else
			atarigen_6502_irq_ack_r(0);
	}
}


static void init_machine(void)
{
	last_speed_check = 0;
	last_speech_write = 0x80;
	last_sound_reset = 1;
	speech_squeak = 0;

	atarigen_eeprom_reset();
	atarigen_slapstic_reset();
	atarigen_interrupt_reset(update_interrupts);
	atarigen_scanline_timer_reset(scanline_update, 8);
	atarigen_sound_io_reset(1);
}



/*************************************
 *
 *	Controller read dispatch.
 *
 *************************************/

static int fake_inputs(int real_port, int fake_port)
{
	int result = readinputport(real_port);
	int fake = readinputport(fake_port);

	if (fake & 0x01)			/* up */
	{
		if (fake & 0x04)		/* up and left */
			result &= ~0x20;
		else if (fake & 0x08)	/* up and right */
			result &= ~0x10;
		else					/* up only */
			result &= ~0x30;
	}
	else if (fake & 0x02)		/* down */
	{
		if (fake & 0x04)		/* down and left */
			result &= ~0x80;
		else if (fake & 0x08)	/* down and right */
			result &= ~0x40;
		else					/* down only */
			result &= ~0xc0;
	}
	else if (fake & 0x04)		/* left only */
		result &= ~0x60;
	else if (fake & 0x08)		/* right only */
		result &= ~0x90;

	return result;
}


static int control_r(int offset)
{
	/* differentiate Gauntlet input from Vindicators 2 inputs via the refresh flag */
	if (!vindctr2_screen_refresh)
	{
		/* Gauntlet case */
		int p1 = input_port_6_r(offset);
		switch (offset)
		{
			case 0:
				return readinputport(p1);
			case 2:
				return readinputport((p1 != 1) ? 1 : 0);
			case 4:
				return readinputport((p1 != 2) ? 2 : 0);
			case 6:
				return readinputport((p1 != 3) ? 3 : 0);
		}
	}
	else
	{
		/* Vindicators 2 case */
		switch (offset)
		{
			case 0:
				return fake_inputs(0, 6);
			case 2:
				return fake_inputs(1, 7);
			case 4:
			case 6:
				return readinputport(offset / 2);
		}
	}
	return 0xffff;
}



/*************************************
 *
 *	I/O read dispatch.
 *
 *************************************/

static int input_r(int offset)
{
	int temp;

	switch (offset)
	{
		case 0:
			temp = input_port_5_r(offset);
			if (atarigen_cpu_to_sound_ready) temp ^= 0x0020;
			if (atarigen_sound_to_cpu_ready) temp ^= 0x0010;
			return temp;

		case 6:
			return atarigen_sound_r(0);
	}
	return 0xffff;
}


static int switch_6502_r(int offset)
{
	int temp = 0x30;

	if (atarigen_cpu_to_sound_ready) temp ^= 0x80;
	if (atarigen_sound_to_cpu_ready) temp ^= 0x40;
	if (tms5220_ready_r()) temp ^= 0x20;
	if (!(input_port_5_r(offset) & 0x0008)) temp ^= 0x10;

	return temp;
}



/*************************************
 *
 *	Controller write dispatch.
 *
 *************************************/

static void input_w(int offset, int data)
{
	switch (offset)
	{
		case 0x0e:		/* sound CPU reset */
		{
			int newword = COMBINE_WORD(last_sound_reset, data);
			int diff = newword ^ last_sound_reset;
			last_sound_reset = newword;
			if (diff & 1)
			{
				cpu_set_reset_line(1, (newword & 1) ? CLEAR_LINE : ASSERT_LINE);
				atarigen_sound_reset();
			}
			break;
		}
	}
}



/*************************************
 *
 *	Sound TMS5220 write.
 *
 *************************************/

static void tms5220_w(int offset, int data)
{
	(void)offset;
	speech_val = data;
}



/*************************************
 *
 *	Sound control write.
 *
 *************************************/

static void sound_ctl_w(int offset, int data)
{
	switch (offset & 7)
	{
		case 0:	/* music reset, bit D7, low reset */
			break;

		case 1:	/* speech write, bit D7, active low */
			if (((data ^ last_speech_write) & 0x80) && (data & 0x80))
				tms5220_data_w(0, speech_val);
			last_speech_write = data;
			break;

		case 2:	/* speech reset, bit D7, active low */
			if (((data ^ last_speech_write) & 0x80) && (data & 0x80))
				tms5220_reset();
			break;

		case 3:	/* speech squeak, bit D7 */
			data = 5 | ((data >> 6) & 2);
			tms5220_set_frequency(ATARI_CLOCK_14MHz/2 / (16 - data));
			break;
	}
}



/*************************************
 *
 *	Sound mixer write.
 *
 *************************************/

static void mixer_w(int offset, int data)
{
	(void)offset;
	atarigen_set_ym2151_vol((data & 7) * 100 / 7);
	atarigen_set_pokey_vol(((data >> 3) & 3) * 100 / 3);
	atarigen_set_tms5220_vol(((data >> 5) & 7) * 100 / 7);
}



/*************************************
 *
 *	Speed cheats
 *
 *************************************/

static int speedup_68010_r(int offset)
{
	int result = READ_WORD(&speed_check[offset]);
	int time = cpu_gettotalcycles();
	int delta = time - last_speed_check;

	last_speed_check = time;
	if (delta <= 100 && result == 0 && delta >= 0)
		cpu_spin();

	return result;
}


static void speedup_68010_w(int offset, int data)
{
	last_speed_check -= 1000;
	COMBINE_WORD_MEM(&speed_check[offset], data);
}



/*************************************
 *
 *	Main CPU memory handlers
 *
 *************************************/

static struct MemoryReadAddress main_readmem[] =
{
	{ 0x000000, 0x07ffff, MRA_ROM },
	{ 0x800000, 0x801fff, MRA_BANK1 },
	{ 0x802000, 0x802fff, atarigen_eeprom_r },
	{ 0x803000, 0x803007, control_r },
	{ 0x803008, 0x80300f, input_r },
	{ 0x900000, 0x901fff, MRA_BANK2 },
	{ 0x902000, 0x903fff, MRA_BANK3 },
	{ 0x904000, 0x904fff, MRA_BANK4 },
	{ 0x905000, 0x905eff, MRA_BANK5 },
	{ 0x905f00, 0x905fff, MRA_BANK6 },
	{ 0x910000, 0x9107ff, paletteram_word_r },
	{ 0x930000, 0x930003, MRA_BANK7 },
	{ -1 }  /* end of table */
};


static struct MemoryWriteAddress main_writemem[] =
{
	{ 0x000000, 0x07ffff, MWA_ROM },
	{ 0x800000, 0x801fff, MWA_BANK1 },
	{ 0x802000, 0x802fff, atarigen_eeprom_w, &atarigen_eeprom, &atarigen_eeprom_size },
	{ 0x803100, 0x803103, watchdog_reset_w },
	{ 0x803120, 0x80312f, input_w },
	{ 0x803140, 0x803143, atarigen_video_int_ack_w },
	{ 0x803150, 0x803153, atarigen_eeprom_enable_w },
	{ 0x803170, 0x803173, atarigen_sound_w },
	{ 0x900000, 0x901fff, gauntlet_playfieldram_w, &atarigen_playfieldram, &atarigen_playfieldram_size },
	{ 0x902000, 0x903fff, MWA_BANK3, &atarigen_spriteram, &atarigen_spriteram_size },
	{ 0x904000, 0x904fff, MWA_BANK4 },
	{ 0x905f6e, 0x905f6f, gauntlet_vscroll_w, &atarigen_vscroll },
	{ 0x905000, 0x905eff, MWA_BANK5, &atarigen_alpharam, &atarigen_alpharam_size },
	{ 0x905f00, 0x905fff, MWA_BANK6 },
	{ 0x910000, 0x9107ff, paletteram_IIIIRRRRGGGGBBBB_word_w, &paletteram },
	{ 0x930000, 0x930001, gauntlet_hscroll_w, &atarigen_hscroll },
	{ -1 }  /* end of table */
};



/*************************************
 *
 *	Sound CPU memory handlers
 *
 *************************************/

static struct MemoryReadAddress sound_readmem[] =
{
	{ 0x0000, 0x0fff, MRA_RAM },
	{ 0x1010, 0x101f, atarigen_6502_sound_r },
	{ 0x1020, 0x102f, input_port_4_r },
	{ 0x1030, 0x103f, switch_6502_r },
	{ 0x1800, 0x180f, pokey1_r },
	{ 0x1811, 0x1811, YM2151_status_port_0_r },
	{ 0x1830, 0x183f, atarigen_6502_irq_ack_r },
	{ 0x4000, 0xffff, MRA_ROM },
	{ -1 }  /* end of table */
};


static struct MemoryWriteAddress sound_writemem[] =
{
	{ 0x0000, 0x0fff, MWA_RAM },
	{ 0x1000, 0x100f, atarigen_6502_sound_w },
	{ 0x1020, 0x102f, mixer_w },
	{ 0x1030, 0x103f, sound_ctl_w },
	{ 0x1800, 0x180f, pokey1_w },
	{ 0x1810, 0x1810, YM2151_register_port_0_w },
	{ 0x1811, 0x1811, YM2151_data_port_0_w },
	{ 0x1820, 0x182f, tms5220_w },
	{ 0x1830, 0x183f, atarigen_6502_irq_ack_w },
	{ 0x4000, 0xffff, MWA_ROM },
	{ -1 }  /* end of table */
};



/*************************************
 *
 *	Port definitions
 *
 *************************************/

INPUT_PORTS_START( gauntlet )
	PORT_START	/* IN0 */
	PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_START1 )
	PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 )
	PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 )
	PORT_BIT( 0x000c, IP_ACTIVE_LOW, IPT_UNUSED )
	PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 | IPF_8WAY )
	PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER1 | IPF_8WAY )
	PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER1 | IPF_8WAY )
	PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER1 | IPF_8WAY )
	PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED )

	PORT_START	/* IN1 */
	PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_START2 )
	PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 )
	PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0x000c, IP_ACTIVE_LOW, IPT_UNUSED )
	PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 | IPF_8WAY )
	PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 | IPF_8WAY )
	PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 | IPF_8WAY )
	PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER2 | IPF_8WAY )
	PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED )

	PORT_START	/* IN2 */
	PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_START3 )
	PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 )
	PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 )
	PORT_BIT( 0x000c, IP_ACTIVE_LOW, IPT_UNUSED )
	PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER3 | IPF_8WAY )
	PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER3 | IPF_8WAY )
	PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER3 | IPF_8WAY )
	PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER3 | IPF_8WAY )
	PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED )

	PORT_START	/* IN3 */
	PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_START4 )
	PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4 )
	PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4 )
	PORT_BIT( 0x000c, IP_ACTIVE_LOW, IPT_UNUSED )
	PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER4 | IPF_8WAY )
	PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER4 | IPF_8WAY )
	PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER4 | IPF_8WAY )
	PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER4 | IPF_8WAY )
	PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED )

	PORT_START	/* IN4 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN4 )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN3 )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN2 )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN1 )
	PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED )

	PORT_START	/* DSW */
	PORT_BIT( 0x0007, IP_ACTIVE_HIGH, IPT_UNUSED )
	PORT_SERVICE( 0x0008, IP_ACTIVE_LOW )
	PORT_BIT( 0x0030, IP_ACTIVE_HIGH, IPT_UNUSED )
	PORT_BIT( 0x0040, IP_ACTIVE_LOW,  IPT_VBLANK )
	PORT_BIT( 0xff80, IP_ACTIVE_HIGH, IPT_UNUSED )

	PORT_START	/* Fake! */
	PORT_DIPNAME( 0x0003, 0x0000, "Player 1 Plays" )
	PORT_DIPSETTING(      0x0000, "Red/Warrior" )
	PORT_DIPSETTING(      0x0001, "Blue/Valkyrie" )
	PORT_DIPSETTING(      0x0002, "Yellow/Wizard" )
	PORT_DIPSETTING(      0x0003, "Green/Elf" )
INPUT_PORTS_END


INPUT_PORTS_START( vindctr2 )
	PORT_START	/* IN0 */
	PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 )
	PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 )
	PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 )
	PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 )
	PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_UP    | IPF_PLAYER1 | IPF_2WAY )
	PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_UP   | IPF_PLAYER1 | IPF_2WAY )
	PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_DOWN  | IPF_PLAYER1 | IPF_2WAY )
	PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_DOWN | IPF_PLAYER1 | IPF_2WAY )
	PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED )

	PORT_START	/* IN1 */
	PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 )
	PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 )
	PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 )
	PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_UP    | IPF_PLAYER2 | IPF_2WAY )
	PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_UP   | IPF_PLAYER2 | IPF_2WAY )
	PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_DOWN  | IPF_PLAYER2 | IPF_2WAY )
	PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_DOWN | IPF_PLAYER2 | IPF_2WAY )
	PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED )

	PORT_START	/* IN2 */
	PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_START1 )
	PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_START2 )
	PORT_BIT( 0xfffc, IP_ACTIVE_LOW, IPT_UNUSED )

	PORT_START	/* IN3 */
	PORT_BIT( 0xffff, IP_ACTIVE_LOW, IPT_UNUSED )

	PORT_START	/* IN4 */
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN4 )
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN3 )
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN2 )
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN1 )
	PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED )

	PORT_START	/* DSW */
	PORT_BIT( 0x0007, IP_ACTIVE_HIGH, IPT_UNUSED )
	PORT_SERVICE( 0x0008, IP_ACTIVE_LOW )
	PORT_BIT( 0x0030, IP_ACTIVE_HIGH, IPT_UNUSED )
	PORT_BIT( 0x0040, IP_ACTIVE_LOW,  IPT_VBLANK )
	PORT_BIT( 0xff80, IP_ACTIVE_HIGH, IPT_UNUSED )

	PORT_START	/* single joystick */
	PORT_BIT( 0x0001, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_CHEAT | IPF_PLAYER1 )
	PORT_BIT( 0x0002, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_CHEAT | IPF_PLAYER1 )
	PORT_BIT( 0x0004, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_CHEAT | IPF_PLAYER1 )
	PORT_BIT( 0x0008, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_CHEAT | IPF_PLAYER1 )

	PORT_START	/* single joystick */
	PORT_BIT( 0x0001, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_CHEAT | IPF_PLAYER2 )
	PORT_BIT( 0x0002, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_CHEAT | IPF_PLAYER2 )
	PORT_BIT( 0x0004, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_CHEAT | IPF_PLAYER2 )
	PORT_BIT( 0x0008, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_CHEAT | IPF_PLAYER2 )
INPUT_PORTS_END



/*************************************
 *
 *	Graphics definitions
 *
 *************************************/

static struct GfxLayout anlayout =
{
	8,8,	/* 8*8 chars */
	1024,	/* 1024 chars */
	2,		/* 2 bits per pixel */
	{ 0, 4 },
	{ 0, 1, 2, 3, 8, 9, 10, 11 },
	{ 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
	8*16	/* every char takes 16 consecutive bytes */
};


static struct GfxLayout pfmolayout =
{
	8,8,	/* 8*8 tiles */
	6*4096,	/* up to 6*4096 of them */
	4,		/* 4 bits per pixel */
	{ 3*8*0x30000, 2*8*0x30000, 1*8*0x30000, 0*8*0x30000 },
	{ 0, 1, 2, 3, 4, 5, 6, 7 },
	{ 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
	8*8	/* every sprite takes 8 consecutive bytes */
};


static struct GfxDecodeInfo gfxdecodeinfo[] =
{
	{ REGION_GFX2, 0, &pfmolayout,  256, 32 },		/* playfield & motion objects */
	{ REGION_GFX1, 0, &anlayout,      0, 64 },		/* alphanumerics */
	{ -1 } /* end of array */
};



/*************************************
 *
 *	Sound definitions
 *
 *************************************/

static struct YM2151interface ym2151_interface =
{
	1,			/* 1 chip */
	ATARI_CLOCK_14MHz/4,
	{ YM3012_VOL(48,MIXER_PAN_LEFT,48,MIXER_PAN_RIGHT) },
	{ 0 }
};


static struct POKEYinterface pokey_interface =
{
	1,			/* 1 chip */
	ATARI_CLOCK_14MHz/8,
	{ 32 },
};


static struct TMS5220interface tms5220_interface =
{
	ATARI_CLOCK_14MHz/2/11,	/* potentially ATARI_CLOCK_14MHz/2/9 as well */
	80,
	0
};



/*************************************
 *
 *	Machine driver
 *
 *************************************/

static struct MachineDriver machine_driver_gauntlet =
{
	/* basic machine hardware */
	{
		{
			CPU_M68010,		/* verified */
			ATARI_CLOCK_14MHz/2,
			main_readmem,main_writemem,0,0,
			atarigen_video_int_gen,1
		},
		{
			CPU_M6502,
			ATARI_CLOCK_14MHz/8,
			sound_readmem,sound_writemem,0,0,
			0,0
		}
	},
	60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
	1,
	init_machine,

	/* video hardware */
	42*8, 30*8, { 0*8, 42*8-1, 0*8, 30*8-1 },
	gfxdecodeinfo,
	1024,1024,
	0,

	VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK,
	0,
	gauntlet_vh_start,
	gauntlet_vh_stop,
	gauntlet_vh_screenrefresh,

	/* sound hardware */
	SOUND_SUPPORTS_STEREO,0,0,0,
	{
		{
			SOUND_YM2151,
			&ym2151_interface
		},
		{
			SOUND_POKEY,
			&pokey_interface
		},
		{
			SOUND_TMS5220,
			&tms5220_interface
		}
	},

	atarigen_nvram_handler
};



/*************************************
 *
 *	ROM definition(s)
 *
 *************************************/

ROM_START( gauntlet )
	ROM_REGION( 0x80000, REGION_CPU1 )	/* 8*64k for 68000 code */
	ROM_LOAD_EVEN( "gauntlt1.9a",  0x00000, 0x08000, 0x46fe8743 )
	ROM_LOAD_ODD ( "gauntlt1.9b",  0x00000, 0x08000, 0x276e15c4 )
	ROM_LOAD_EVEN( "gauntlt1.10a", 0x38000, 0x04000, 0x6d99ed51 )
	ROM_LOAD_ODD ( "gauntlt1.10b", 0x38000, 0x04000, 0x545ead91 )
	ROM_LOAD_EVEN( "gauntlt1.7a",  0x40000, 0x08000, 0x6fb8419c )
	ROM_LOAD_ODD ( "gauntlt1.7b",  0x40000, 0x08000, 0x931bd2a0 )

	ROM_REGION( 0x10000, REGION_CPU2 )	/* 64k for 6502 code */
	ROM_LOAD( "gauntlt1.16r", 0x4000, 0x4000, 0x6ee7f3cc )
	ROM_LOAD( "gauntlt1.16s", 0x8000, 0x8000, 0xfa19861f )

	ROM_REGION( 0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE )
	ROM_LOAD( "gauntlt1.6p",  0x00000, 0x04000, 0x6c276a1d )

	ROM_REGION( 0xc0000, REGION_GFX2 | REGIONFLAG_DISPOSE )
	ROM_LOAD( "gauntlt1.1a",  0x00000, 0x08000, 0x91700f33 )
	ROM_LOAD( "gauntlt1.1b",  0x08000, 0x08000, 0x869330be )

	ROM_LOAD( "gauntlt1.1l",  0x30000, 0x08000, 0xd497d0a8 )
	ROM_LOAD( "gauntlt1.1mn", 0x38000, 0x08000, 0x29ef9882 )

	ROM_LOAD( "gauntlt1.2a",  0x60000, 0x08000, 0x9510b898 )
	ROM_LOAD( "gauntlt1.2b",  0x68000, 0x08000, 0x11e0ac5b )

	ROM_LOAD( "gauntlt1.2l",  0x90000, 0x08000, 0x29a5db41 )
	ROM_LOAD( "gauntlt1.2mn", 0x98000, 0x08000, 0x8bf3b263 )
ROM_END


ROM_START( gauntir1 )
	ROM_REGION( 0x80000, REGION_CPU1 )	/* 8*64k for 68000 code */
	ROM_LOAD_EVEN( "gaun1ir1.9a",  0x00000, 0x08000, 0xfd871f81 )
	ROM_LOAD_ODD ( "gaun1ir1.9b",  0x00000, 0x08000, 0xbcb2fb1d )
	ROM_LOAD_EVEN( "gaun1ir1.10a", 0x38000, 0x04000, 0x4642cd95 )
	ROM_LOAD_ODD ( "gaun1ir1.10b", 0x38000, 0x04000, 0xc8df945e )
	ROM_LOAD_EVEN( "gaun1ir1.7a",  0x40000, 0x08000, 0xc57377b3 )
	ROM_LOAD_ODD ( "gaun1ir1.7b",  0x40000, 0x08000, 0x1cac2071 )

	ROM_REGION( 0x10000, REGION_CPU2 )	/* 64k for 6502 code */
	ROM_LOAD( "gauntlt1.16r", 0x4000, 0x4000, 0x6ee7f3cc )
	ROM_LOAD( "gauntlt1.16s", 0x8000, 0x8000, 0xfa19861f )

	ROM_REGION( 0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE )
	ROM_LOAD( "gauntlt1.6p",  0x00000, 0x04000, 0x6c276a1d )

	ROM_REGION( 0xc0000, REGION_GFX2 | REGIONFLAG_DISPOSE )
	ROM_LOAD( "gauntlt1.1a",  0x00000, 0x08000, 0x91700f33 )
	ROM_LOAD( "gauntlt1.1b",  0x08000, 0x08000, 0x869330be )

	ROM_LOAD( "gauntlt1.1l",  0x30000, 0x08000, 0xd497d0a8 )
	ROM_LOAD( "gauntlt1.1mn", 0x38000, 0x08000, 0x29ef9882 )

	ROM_LOAD( "gauntlt1.2a",  0x60000, 0x08000, 0x9510b898 )
	ROM_LOAD( "gauntlt1.2b",  0x68000, 0x08000, 0x11e0ac5b )

	ROM_LOAD( "gauntlt1.2l",  0x90000, 0x08000, 0x29a5db41 )
	ROM_LOAD( "gauntlt1.2mn", 0x98000, 0x08000, 0x8bf3b263 )
ROM_END


ROM_START( gauntir2 )
	ROM_REGION( 0x80000, REGION_CPU1 )	/* 8*64k for 68000 code */
	ROM_LOAD_EVEN( "gaun1ir1.9a",  0x00000, 0x08000, 0xfd871f81 )
	ROM_LOAD_ODD ( "gaun1ir1.9b",  0x00000, 0x08000, 0xbcb2fb1d )
	ROM_LOAD_EVEN( "gaun1ir1.10a", 0x38000, 0x04000, 0x4642cd95 )
	ROM_LOAD_ODD ( "gaun1ir1.10b", 0x38000, 0x04000, 0xc8df945e )
	ROM_LOAD_EVEN( "gaun1ir2.7a",  0x40000, 0x08000, 0x73e1ad79 )
	ROM_LOAD_ODD ( "gaun1ir2.7b",  0x40000, 0x08000, 0xfd248cea )

	ROM_REGION( 0x10000, REGION_CPU2 )	/* 64k for 6502 code */
	ROM_LOAD( "gauntlt1.16r", 0x4000, 0x4000, 0x6ee7f3cc )
	ROM_LOAD( "gauntlt1.16s", 0x8000, 0x8000, 0xfa19861f )

	ROM_REGION( 0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE )
	ROM_LOAD( "gauntlt1.6p",  0x00000, 0x04000, 0x6c276a1d )

	ROM_REGION( 0xc0000, REGION_GFX2 | REGIONFLAG_DISPOSE )
	ROM_LOAD( "gauntlt1.1a",  0x00000, 0x08000, 0x91700f33 )
	ROM_LOAD( "gauntlt1.1b",  0x08000, 0x08000, 0x869330be )

	ROM_LOAD( "gauntlt1.1l",  0x30000, 0x08000, 0xd497d0a8 )
	ROM_LOAD( "gauntlt1.1mn", 0x38000, 0x08000, 0x29ef9882 )

	ROM_LOAD( "gauntlt1.2a",  0x60000, 0x08000, 0x9510b898 )
	ROM_LOAD( "gauntlt1.2b",  0x68000, 0x08000, 0x11e0ac5b )

	ROM_LOAD( "gauntlt1.2l",  0x90000, 0x08000, 0x29a5db41 )
	ROM_LOAD( "gauntlt1.2mn", 0x98000, 0x08000, 0x8bf3b263 )
ROM_END


ROM_START( gaunt2p )
	ROM_REGION( 0x80000, REGION_CPU1 )	/* 8*64k for 68000 code */
	ROM_LOAD_EVEN( "gaunt2p.9a",   0x00000, 0x08000, 0x8784133f )
	ROM_LOAD_ODD ( "gaunt2p.9b",   0x00000, 0x08000, 0x2843bde3 )
	ROM_LOAD_EVEN( "gauntlt1.10a", 0x38000, 0x04000, 0x6d99ed51 )
	ROM_LOAD_ODD ( "gauntlt1.10b", 0x38000, 0x04000, 0x545ead91 )
	ROM_LOAD_EVEN( "gaunt2p.7a",   0x40000, 0x08000, 0x5b4ee415 )
	ROM_LOAD_ODD ( "gaunt2p.7b",   0x40000, 0x08000, 0x41f5c9e2 )

	ROM_REGION( 0x10000, REGION_CPU2 )	/* 64k for 6502 code */
	ROM_LOAD( "gauntlt1.16r", 0x4000, 0x4000, 0x6ee7f3cc )
	ROM_LOAD( "gauntlt1.16s", 0x8000, 0x8000, 0xfa19861f )

	ROM_REGION( 0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE )
	ROM_LOAD( "gauntlt1.6p",  0x00000, 0x04000, 0x6c276a1d )

	ROM_REGION( 0xc0000, REGION_GFX2 | REGIONFLAG_DISPOSE )
	ROM_LOAD( "gauntlt1.1a",  0x00000, 0x08000, 0x91700f33 )
	ROM_LOAD( "gauntlt1.1b",  0x08000, 0x08000, 0x869330be )

	ROM_LOAD( "gauntlt1.1l",  0x30000, 0x08000, 0xd497d0a8 )
	ROM_LOAD( "gauntlt1.1mn", 0x38000, 0x08000, 0x29ef9882 )

	ROM_LOAD( "gauntlt1.2a",  0x60000, 0x08000, 0x9510b898 )
	ROM_LOAD( "gauntlt1.2b",  0x68000, 0x08000, 0x11e0ac5b )

	ROM_LOAD( "gauntlt1.2l",  0x90000, 0x08000, 0x29a5db41 )
	ROM_LOAD( "gauntlt1.2mn", 0x98000, 0x08000, 0x8bf3b263 )
ROM_END


ROM_START( gaunt2 )
	ROM_REGION( 0x80000, REGION_CPU1 )	/* 8*64k for 68000 code */
	ROM_LOAD_EVEN( "gauntlt2.9a",  0x00000, 0x08000, 0x46fe8743 )
	ROM_LOAD_ODD ( "gauntlt2.9b",  0x00000, 0x08000, 0x276e15c4 )
	ROM_LOAD_EVEN( "gauntlt2.10a", 0x38000, 0x04000, 0x45dfda47 )
	ROM_LOAD_ODD ( "gauntlt2.10b", 0x38000, 0x04000, 0x343c029c )
	ROM_LOAD_EVEN( "gauntlt2.7a",  0x40000, 0x08000, 0x58a0a9a3 )
	ROM_LOAD_ODD ( "gauntlt2.7b",  0x40000, 0x08000, 0x658f0da8 )
	ROM_LOAD_EVEN( "gauntlt2.6a",  0x50000, 0x08000, 0xae301bba )
	ROM_LOAD_ODD ( "gauntlt2.6b",  0x50000, 0x08000, 0xe94aaa8a )

	ROM_REGION( 0x10000, REGION_CPU2 )	/* 64k for 6502 code */
	ROM_LOAD( "gauntlt2.16r", 0x4000, 0x4000, 0x5c731006 )
	ROM_LOAD( "gauntlt2.16s", 0x8000, 0x8000, 0xdc3591e7 )

	ROM_REGION( 0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE )
	ROM_LOAD( "gauntlt2.6p",  0x00000, 0x04000, 0xd101905d )

	ROM_REGION( 0xc0000, REGION_GFX2 | REGIONFLAG_DISPOSE )
	ROM_LOAD( "gauntlt2.1a",  0x00000, 0x08000, 0x09df6e23 )
	ROM_LOAD( "gauntlt2.1b",  0x08000, 0x08000, 0x869330be )
	ROM_LOAD( "gauntlt2.1c",  0x10000, 0x04000, 0xe4c98f01 )
	ROM_RELOAD(               0x14000, 0x04000 )

	ROM_LOAD( "gauntlt2.1l",  0x30000, 0x08000, 0x33cb476e )
	ROM_LOAD( "gauntlt2.1mn", 0x38000, 0x08000, 0x29ef9882 )
	ROM_LOAD( "gauntlt2.1p",  0x40000, 0x04000, 0xc4857879 )
	ROM_RELOAD(               0x44000, 0x04000 )

	ROM_LOAD( "gauntlt2.2a",  0x60000, 0x08000, 0xf71e2503 )
	ROM_LOAD( "gauntlt2.2b",  0x68000, 0x08000, 0x11e0ac5b )
	ROM_LOAD( "gauntlt2.2c",  0x70000, 0x04000, 0xd9c2c2d1 )
	ROM_RELOAD(               0x74000, 0x04000 )

	ROM_LOAD( "gauntlt2.2l",  0x90000, 0x08000, 0x9e30b2e9 )
	ROM_LOAD( "gauntlt2.2mn", 0x98000, 0x08000, 0x8bf3b263 )
	ROM_LOAD( "gauntlt2.2p",  0xa0000, 0x04000, 0xa32c732a )
	ROM_RELOAD(               0xa4000, 0x04000 )
ROM_END


ROM_START( vindctr2 )
	ROM_REGION( 0x80000, REGION_CPU1 )	/* 8*64k for 68000 code */
	ROM_LOAD_EVEN( "1186", 0x00000, 0x08000, 0xaf138263 )
	ROM_LOAD_ODD ( "1187", 0x00000, 0x08000, 0x44baff64 )
	ROM_LOAD_EVEN( "1196", 0x38000, 0x04000, 0xc92bf6dd )
	ROM_LOAD_ODD ( "1197", 0x38000, 0x04000, 0xd7ace347 )
	ROM_LOAD_EVEN( "3188", 0x40000, 0x08000, 0x10f558d2 )
	ROM_LOAD_ODD ( "3189", 0x40000, 0x08000, 0x302e24b6 )
	ROM_LOAD_EVEN( "2190", 0x50000, 0x08000, 0xe7dc2b74 )
	ROM_LOAD_ODD ( "2191", 0x50000, 0x08000, 0xed8ed86e )
	ROM_LOAD_EVEN( "2192", 0x60000, 0x08000, 0xeec2c93d )
	ROM_LOAD_ODD ( "2193", 0x60000, 0x08000, 0x3fbee9aa )
	ROM_LOAD_EVEN( "1194", 0x70000, 0x08000, 0xe6bcf458 )
	ROM_LOAD_ODD ( "1195", 0x70000, 0x08000, 0xb9bf245d )

	ROM_REGION( 0x10000, REGION_CPU2 )	/* 64k for 6502 code */
	ROM_LOAD( "1160", 0x4000, 0x4000, 0xeef0a003 )
	ROM_LOAD( "1161", 0x8000, 0x8000, 0x68c74337 )

	ROM_REGION( 0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE )
	ROM_LOAD( "1198",  0x00000, 0x04000, 0xf99b631a )

	ROM_REGION( 0xc0000, REGION_GFX2 | REGIONFLAG_DISPOSE )
	ROM_LOAD( "1101", 0x00000, 0x08000, 0xdd3833ad )
	ROM_LOAD( "1166", 0x08000, 0x08000, 0xe2db50a0 )
	ROM_LOAD( "1170", 0x10000, 0x08000, 0xf050ab43 )
	ROM_LOAD( "1174", 0x18000, 0x08000, 0xb6704bd1 )
	ROM_LOAD( "1178", 0x20000, 0x08000, 0xd3006f05 )
	ROM_LOAD( "1182", 0x28000, 0x08000, 0x9046e985 )

	ROM_LOAD( "1102", 0x30000, 0x08000, 0xd505b04a )
	ROM_LOAD( "1167", 0x38000, 0x08000, 0x1869c76d )
	ROM_LOAD( "1171", 0x40000, 0x08000, 0x1b229c2b )
	ROM_LOAD( "1175", 0x48000, 0x08000, 0x73c41aca )
	ROM_LOAD( "1179", 0x50000, 0x08000, 0x9b7cb0ef )
	ROM_LOAD( "1183", 0x58000, 0x08000, 0x393bba42 )

	ROM_LOAD( "1103", 0x60000, 0x08000, 0x50e76162 )
	ROM_LOAD( "1168", 0x68000, 0x08000, 0x35c78469 )
	ROM_LOAD( "1172", 0x70000, 0x08000, 0x314ac268 )
	ROM_LOAD( "1176", 0x78000, 0x08000, 0x061d79db )
	ROM_LOAD( "1180", 0x80000, 0x08000, 0x89c1fe16 )
	ROM_LOAD( "1184", 0x88000, 0x08000, 0x541209d3 )

	ROM_LOAD( "1104", 0x90000, 0x08000, 0x9484ba65 )
	ROM_LOAD( "1169", 0x98000, 0x08000, 0x132d3337 )
	ROM_LOAD( "1173", 0xa0000, 0x08000, 0x98de2426 )
	ROM_LOAD( "1177", 0xa8000, 0x08000, 0x9d0824f8 )
	ROM_LOAD( "1181", 0xb0000, 0x08000, 0x9e62b27c )
	ROM_LOAD( "1185", 0xb8000, 0x08000, 0x9d62f6b7 )
ROM_END



/*************************************
 *
 *	ROM decoding
 *
 *************************************/

static void rom_decode(void)
{
	UINT32 *p1, *p2, temp;
	UINT8 *data;
	int i;

	/* swap the top and bottom halves of the main CPU ROM images */
	p1 = (UINT32 *)&memory_region(REGION_CPU1)[0x000000];
	p2 = (UINT32 *)&memory_region(REGION_CPU1)[0x008000];
	for (i = 0; i < 0x8000 / 4; i++)
		temp = *p1, *p1++ = *p2, *p2++ = temp;
	p1 = (UINT32 *)&memory_region(REGION_CPU1)[0x040000];
	p2 = (UINT32 *)&memory_region(REGION_CPU1)[0x048000];
	for (i = 0; i < 0x8000 / 4; i++)
		temp = *p1, *p1++ = *p2, *p2++ = temp;
	p1 = (UINT32 *)&memory_region(REGION_CPU1)[0x050000];
	p2 = (UINT32 *)&memory_region(REGION_CPU1)[0x058000];
	for (i = 0; i < 0x8000 / 4; i++)
		temp = *p1, *p1++ = *p2, *p2++ = temp;
	p1 = (UINT32 *)&memory_region(REGION_CPU1)[0x060000];
	p2 = (UINT32 *)&memory_region(REGION_CPU1)[0x068000];
	for (i = 0; i < 0x8000 / 4; i++)
		temp = *p1, *p1++ = *p2, *p2++ = temp;
	p1 = (UINT32 *)&memory_region(REGION_CPU1)[0x070000];
	p2 = (UINT32 *)&memory_region(REGION_CPU1)[0x078000];
	for (i = 0; i < 0x8000 / 4; i++)
		temp = *p1, *p1++ = *p2, *p2++ = temp;

	/* highly strange -- the address bits on the chip at 2J (and only that
	   chip) are scrambled -- this is verified on the schematics! */
	data = malloc(0x8000);
	if (data)
	{
		memcpy(data, &memory_region(REGION_GFX2)[0x88000], 0x8000);
		for (i = 0; i < 0x8000; i++)
		{
			int srcoffs = (i & 0x4000) | ((i << 11) & 0x3800) | ((i >> 3) & 0x07ff);
			memory_region(REGION_GFX2)[0x88000 + i] = data[srcoffs];
		}
		free(data);
	}

	/* also invert the graphics bits on the playfield and motion objects */
	for (i = 0; i < memory_region_length(REGION_GFX2); i++)
		memory_region(REGION_GFX2)[i] ^= 0xff;
}



/*************************************
 *
 *	Driver initialization
 *
 *************************************/

static void init_gauntlet(void)
{
	atarigen_eeprom_default = NULL;
	atarigen_slapstic_init(0, 0x038000, 104);

	vindctr2_screen_refresh = 0;

	/* speed up the 6502 */
	atarigen_init_6502_speedup(1, 0x410f, 0x4127);

	/* speed up the 68010 */
	speed_check = install_mem_write_handler(0, 0x904002, 0x904003, speedup_68010_w);
	install_mem_read_handler(0, 0x904002, 0x904003, speedup_68010_r);

	/* display messages */
/*	atarigen_show_slapstic_message(); -- no known slapstic problems */
	atarigen_show_sound_message();

	rom_decode();
}


static void init_gaunt2p(void)
{
	atarigen_eeprom_default = NULL;
	atarigen_slapstic_init(0, 0x038000, 107);

	vindctr2_screen_refresh = 0;

	/* speed up the 6502 */
	atarigen_init_6502_speedup(1, 0x410f, 0x4127);

	/* speed up the 68010 */
	speed_check = install_mem_write_handler(0, 0x904002, 0x904003, speedup_68010_w);
	install_mem_read_handler(0, 0x904002, 0x904003, speedup_68010_r);

	/* display messages */
/*	atarigen_show_slapstic_message(); -- no known slapstic problems */
	atarigen_show_sound_message();

	rom_decode();
}


static void init_gauntlet2(void)
{
	atarigen_eeprom_default = NULL;
	atarigen_slapstic_init(0, 0x038000, 106);

	vindctr2_screen_refresh = 0;

	/* speed up the 6502 */
	atarigen_init_6502_speedup(1, 0x410f, 0x4127);

	/* speed up the 68010 */
	speed_check = install_mem_write_handler(0, 0x904002, 0x904003, speedup_68010_w);
	install_mem_read_handler(0, 0x904002, 0x904003, speedup_68010_r);

	/* display messages */
/*	atarigen_show_slapstic_message(); -- no known slapstic problems */
	atarigen_show_sound_message();

	rom_decode();
}


static void init_vindctr2(void)
{
	atarigen_eeprom_default = NULL;
	atarigen_slapstic_init(0, 0x038000, 118);

	vindctr2_screen_refresh = 1;

	/* speed up the 6502 */
	atarigen_init_6502_speedup(1, 0x40ff, 0x4117);

	/* display messages */
/*	atarigen_show_slapstic_message(); -- no known slapstic problems - yet! */
	atarigen_show_sound_message();

	rom_decode();
}



/*************************************
 *
 *	Game driver(s)
 *
 *************************************/

GAME( 1985, gauntlet, 0,        gauntlet, gauntlet, gauntlet,  ROT0, "Atari Games", "Gauntlet" )
GAME( 1985, gauntir1, gauntlet, gauntlet, gauntlet, gauntlet,  ROT0, "Atari Games", "Gauntlet (Intermediate Release 1)" )
GAME( 1985, gauntir2, gauntlet, gauntlet, gauntlet, gauntlet,  ROT0, "Atari Games", "Gauntlet (Intermediate Release 2)" )
GAME( 1985, gaunt2p,  gauntlet, gauntlet, gauntlet, gaunt2p,   ROT0, "Atari Games", "Gauntlet (2 Players)" )
GAME( 1986, gaunt2,   0,        gauntlet, gauntlet, gauntlet2, ROT0, "Atari Games", "Gauntlet II" )
GAME( 1988, vindctr2, 0,        gauntlet, vindctr2, vindctr2,  ROT0, "Atari Games", "Vindicators Part II" )
