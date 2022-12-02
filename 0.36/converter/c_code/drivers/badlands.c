/***************************************************************************

	Badlands

    driver by Aaron Giles

****************************************************************************/


#include "driver.h"
#include "machine/atarigen.h"
#include "vidhrdw/generic.h"


void badlands_playfieldram_w(int offset, int data);
void badlands_pf_bank_w(int offset, int data);

int badlands_vh_start(void);
void badlands_vh_stop(void);
void badlands_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh);

void badlands_scanline_update(int scanline);


static UINT8 pedal_value[2];

static UINT8 *bank_base;
static UINT8 *bank_source_data;



/*************************************
 *
 *	Initialization
 *
 *************************************/

static void update_interrupts(void)
{
	int newstate = 0;

	if (atarigen_video_int_state)
		newstate = 1;
	if (atarigen_sound_int_state)
		newstate = 2;

	if (newstate)
		cpu_set_irq_line(0, newstate, ASSERT_LINE);
	else
		cpu_set_irq_line(0, 7, CLEAR_LINE);
}


static void scanline_update(int scanline)
{
	badlands_scanline_update(scanline);

	/* sound IRQ is on 32V */
	if (scanline % 32 == 0)
	{
		if (scanline & 32)
			atarigen_6502_irq_ack_r(0);
		else if (!(readinputport(0) & 0x40))
			atarigen_6502_irq_gen();
	}
}


static void init_machine(void)
{
	pedal_value[0] = pedal_value[1] = 0x80;

	atarigen_eeprom_reset();
	atarigen_interrupt_reset(update_interrupts);
	atarigen_scanline_timer_reset(scanline_update, 8);

	atarigen_sound_io_reset(1);
	memcpy(bank_base, &bank_source_data[0x0000], 0x1000);
}



/*************************************
 *
 *	Interrupt handling
 *
 *************************************/

static int vblank_int(void)
{
	int pedal_state = input_port_4_r(0);
	int i;

	/* update the pedals once per frame */
    for (i = 0; i < 2; i++)
	{
		pedal_value[i]--;
		if (pedal_state & (1 << i))
			pedal_value[i]++;
	}

	return atarigen_video_int_gen();
}



/*************************************
 *
 *	I/O read dispatch
 *
 *************************************/

static int sound_busy_r(int offset)
{
	int temp = 0xfeff;

	(void)offset;
	if (atarigen_cpu_to_sound_ready) temp ^= 0x0100;
	return temp;
}


static int pedal_0_r(int offset)
{
	(void)offset;
	return pedal_value[0];
}


static int pedal_1_r(int offset)
{
	(void)offset;
	return pedal_value[1];
}



/*************************************
 *
 *	Audio I/O handlers
 *
 *************************************/

static int audio_io_r(int offset)
{
	int result = 0xff;

	switch (offset & 0x206)
	{
		case 0x000:		/* n/c */
			if (errorlog) fprintf(errorlog, "audio_io_r: Unknown read at %04X\n", offset & 0x206);
			break;

		case 0x002:		/* /RDP */
			result = atarigen_6502_sound_r(offset);
			break;

		case 0x004:		/* /RDIO */
			/*
				0x80 = self test
				0x40 = NMI line state (active low)
				0x20 = sound output full
				0x10 = self test
				0x08 = +5V
				0x04 = +5V
				0x02 = coin 2
				0x01 = coin 1
			*/
			result = readinputport(3);
			if (!(readinputport(0) & 0x0080)) result ^= 0x90;
			if (atarigen_cpu_to_sound_ready) result ^= 0x40;
			if (atarigen_sound_to_cpu_ready) result ^= 0x20;
			result ^= 0x10;
			break;

		case 0x006:		/* /IRQACK */
			atarigen_6502_irq_ack_r(0);
			break;

		case 0x200:		/* /VOICE */
		case 0x202:		/* /WRP */
		case 0x204:		/* /WRIO */
		case 0x206:		/* /MIX */
			if (errorlog) fprintf(errorlog, "audio_io_r: Unknown read at %04X\n", offset & 0x206);
			break;
	}

	return result;
}


static void audio_io_w(int offset, int data)
{
	switch (offset & 0x206)
	{
		case 0x000:		/* n/c */
		case 0x002:		/* /RDP */
		case 0x004:		/* /RDIO */
			if (errorlog) fprintf(errorlog, "audio_io_w: Unknown write (%02X) at %04X\n", data & 0xff, offset & 0x206);
			break;

		case 0x006:		/* /IRQACK */
			atarigen_6502_irq_ack_r(0);
			break;

		case 0x200:		/* n/c */
		case 0x206:		/* n/c */
			break;

		case 0x202:		/* /WRP */
			atarigen_6502_sound_w(offset, data);
			break;

		case 0x204:		/* WRIO */
			/*
				0xc0 = bank address
				0x20 = coin counter 2
				0x10 = coin counter 1
				0x08 = n/c
				0x04 = n/c
				0x02 = n/c
				0x01 = YM2151 reset (active low)
			*/

			/* update the bank */
			memcpy(bank_base, &bank_source_data[0x1000 * ((data >> 6) & 3)], 0x1000);
			break;
	}
}



/*************************************
 *
 *	Main CPU memory handlers
 *
 *************************************/

static struct MemoryReadAddress main_readmem[] =
{
	{ 0x000000, 0x03ffff, MRA_ROM },
	{ 0xfc0000, 0xfc1fff, sound_busy_r },
	{ 0xfd0000, 0xfd1fff, atarigen_eeprom_r },
	{ 0xfe4000, 0xfe5fff, input_port_0_r },
	{ 0xfe6000, 0xfe6001, input_port_1_r },
	{ 0xfe6002, 0xfe6003, input_port_2_r },
	{ 0xfe6004, 0xfe6005, pedal_0_r },
	{ 0xfe6006, 0xfe6007, pedal_1_r },
	{ 0xfea000, 0xfebfff, atarigen_sound_upper_r },
	{ 0xffc000, 0xffc3ff, paletteram_word_r },
	{ 0xffe000, 0xffefff, MRA_BANK1 },
	{ 0xfff000, 0xffffff, MRA_BANK2 },
	{ -1 }  /* end of table */
};


static struct MemoryWriteAddress main_writemem[] =
{
	{ 0x000000, 0x03ffff, MWA_ROM },
	{ 0xfc0000, 0xfc1fff, atarigen_sound_reset_w },
	{ 0xfd0000, 0xfd1fff, atarigen_eeprom_w, &atarigen_eeprom, &atarigen_eeprom_size },
	{ 0xfe0000, 0xfe1fff, watchdog_reset_w },
	{ 0xfe2000, 0xfe3fff, atarigen_video_int_ack_w },
	{ 0xfe8000, 0xfe9fff, atarigen_sound_upper_w },
	{ 0xfec000, 0xfedfff, badlands_pf_bank_w },
	{ 0xfee000, 0xfeffff, atarigen_eeprom_enable_w },
	{ 0xffc000, 0xffc3ff, atarigen_expanded_666_paletteram_w, &paletteram },
	{ 0xffe000, 0xffefff, badlands_playfieldram_w, &atarigen_playfieldram, &atarigen_playfieldram_size },
	{ 0xfff000, 0xffffff, MWA_BANK2, &atarigen_spriteram, &atarigen_spriteram_size },
	{ -1 }  /* end of table */
};



/*************************************
 *
 *	Sound CPU memory handlers
 *
 *************************************/

static struct MemoryReadAddress audio_readmem[] =
{
	{ 0x0000, 0x1fff, MRA_RAM },
	{ 0x2000, 0x2001, YM2151_status_port_0_r },
	{ 0x2800, 0x2bff, audio_io_r },
	{ 0x3000, 0xffff, MRA_ROM },
	{ -1 }  /* end of table */
};


static struct MemoryWriteAddress audio_writemem[] =
{
	{ 0x0000, 0x1fff, MWA_RAM },
	{ 0x2000, 0x2000, YM2151_register_port_0_w },
	{ 0x2001, 0x2001, YM2151_data_port_0_w },
	{ 0x2800, 0x2bff, audio_io_w },
	{ 0x3000, 0xffff, MWA_ROM },
	{ -1 }  /* end of table */
};


/*************************************
 *
 *	Port definitions
 *
 *************************************/

INPUT_PORTS_START( badlands )
	PORT_START		/* fe4000 */
	PORT_BIT( 0x000f, IP_ACTIVE_LOW, IPT_UNUSED )
	PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 )
	PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_START1 )
	PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 )
	PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_START2 )
	PORT_BIT( 0x0040, IP_ACTIVE_HIGH, IPT_VBLANK )
	PORT_SERVICE( 0x0080, IP_ACTIVE_LOW )
	PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED )

	PORT_START      /* fe6000 */
	PORT_ANALOG( 0x00ff, 0, IPT_DIAL | IPF_PLAYER1, 50, 10, 0, 0 )
	PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED )

	PORT_START      /* fe6002 */
	PORT_ANALOG( 0x00ff, 0, IPT_DIAL | IPF_PLAYER2, 50, 10, 0, 0 )
	PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED )

	PORT_START		/* audio port */
	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 )
	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_COIN2 )
	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_COIN3 )
	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNUSED )
	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNUSED )
	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNUSED )	/* output buffer full */
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED )		/* input buffer full */
	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNUSED )	/* self test */

	PORT_START      /* fake for pedals */
	PORT_BIT( 0x0001, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1 )
	PORT_BIT( 0x0002, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 )
	PORT_BIT( 0xfffe, IP_ACTIVE_HIGH, IPT_UNUSED )
INPUT_PORTS_END



/*************************************
 *
 *	Graphics definitions
 *
 *************************************/

static struct GfxLayout pflayout =
{
	8,8,	/* 8*8 chars */
	12288,	/* 12288 chars */
	4,		/* 4 bits per pixel */
	{ 0, 1, 2, 3 },
	{ 0, 4, 8, 12, 16, 20, 24, 28 },
	{ 0*8, 4*8, 8*8, 12*8, 16*8, 20*8, 24*8, 28*8 },
	32*8	/* every char takes 32 consecutive bytes */
};


static struct GfxLayout molayout =
{
	16,8,	/* 16*8 chars */
	3072,	/* 3072 chars */
	4,		/* 4 bits per pixel */
	{ 0, 1, 2, 3 },
	{ 0, 4, 8, 12, 16, 20, 24, 28, 32, 36, 40, 44, 48, 52, 56, 60 },
	{ 0*8, 8*8, 16*8, 24*8, 32*8, 40*8, 48*8, 56*8 },
	64*8	/* every char takes 32 consecutive bytes */
};


static struct GfxDecodeInfo gfxdecodeinfo[] =
{
	{ REGION_GFX1, 0, &pflayout,    0, 8 },
	{ REGION_GFX2, 0, &molayout,  128, 8 },
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
	{ YM3012_VOL(30,MIXER_PAN_CENTER,30,MIXER_PAN_CENTER) },
	{ 0 }
};



/*************************************
 *
 *	Machine driver
 *
 *************************************/

static struct MachineDriver machine_driver_badlands =
{
	/* basic machine hardware */
	{
		{
			CPU_M68000,		/* verified */
			ATARI_CLOCK_14MHz/2,
			main_readmem,main_writemem,0,0,
			vblank_int,1
		},
		{
			CPU_M6502,
			ATARI_CLOCK_14MHz/8,
			audio_readmem,audio_writemem,0,0,
			ignore_interrupt,1
		}
	},
	60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
	1,
	init_machine,

	/* video hardware */
	42*8, 30*8, { 0*8, 42*8-1, 0*8, 30*8-1 },
	gfxdecodeinfo,
	256,256,
	0,

	VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK |
			VIDEO_SUPPORTS_DIRTY,
	0,
	badlands_vh_start,
	badlands_vh_stop,
	badlands_vh_screenrefresh,

	/* sound hardware */
	0,0,0,0,
	{
		{
			SOUND_YM2151,
			&ym2151_interface
		}
	},

	atarigen_nvram_handler
};



/*************************************
 *
 *	ROM decoding
 *
 *************************************/

static void rom_decode(void)
{
	int i;

	for (i = 0; i < memory_region_length(REGION_GFX1); i++)
		memory_region(REGION_GFX1)[i] ^= 0xff;
	for (i = 0; i < memory_region_length(REGION_GFX2); i++)
		memory_region(REGION_GFX2)[i] ^= 0xff;
}



/*************************************
 *
 *	ROM definition(s)
 *
 *************************************/

ROM_START( badlands )
	ROM_REGION( 0x40000, REGION_CPU1 )	/* 4*64k for 68000 code */
	ROM_LOAD_EVEN( "1008.20f",  0x00000, 0x10000, 0xa3da5774 )
	ROM_LOAD_ODD ( "1006.27f",  0x00000, 0x10000, 0xaa03b4f3 )
	ROM_LOAD_EVEN( "1009.17f",  0x20000, 0x10000, 0x0e2e807f )
	ROM_LOAD_ODD ( "1007.24f",  0x20000, 0x10000, 0x99a20c2c )

	ROM_REGION( 0x14000, REGION_CPU2 )	/* 64k for 6502 code */
	ROM_LOAD( "1018.9c", 0x10000, 0x4000, 0xa05fd146 )
	ROM_CONTINUE(        0x04000, 0xc000 )

	ROM_REGION( 0x60000, REGION_GFX1 | REGIONFLAG_DISPOSE )
	ROM_LOAD( "1012.4n",  0x000000, 0x10000, 0x5d124c6c )	/* playfield */
	ROM_LOAD( "1013.2n",  0x010000, 0x10000, 0xb1ec90d6 )
	ROM_LOAD( "1014.4s",  0x020000, 0x10000, 0x248a6845 )
	ROM_LOAD( "1015.2s",  0x030000, 0x10000, 0x792296d8 )
	ROM_LOAD( "1016.4u",  0x040000, 0x10000, 0x878f7c66 )
	ROM_LOAD( "1017.2u",  0x050000, 0x10000, 0xad0071a3 )

	ROM_REGION( 0x30000, REGION_GFX2 | REGIONFLAG_DISPOSE )
	ROM_LOAD( "1010.14r", 0x000000, 0x10000, 0xc15f629e )	/* mo */
	ROM_LOAD( "1011.10r", 0x010000, 0x10000, 0xfb0b6717 )
	ROM_LOAD( "1019.14t", 0x020000, 0x10000, 0x0e26bff6 )
ROM_END



/*************************************
 *
 *	Driver initialization
 *
 *************************************/

static void init_badlands(void)
{
	atarigen_eeprom_default = NULL;

	/* initialize the audio system */
	bank_base = &memory_region(REGION_CPU2)[0x03000];
	bank_source_data = &memory_region(REGION_CPU2)[0x10000];

	/* speed up the 6502 */
	atarigen_init_6502_speedup(1, 0x4155, 0x416d);

	/* display messages */
	atarigen_show_sound_message();

	rom_decode();
}



/*************************************
 *
 *	Game driver(s)
 *
 *************************************/

GAME( 1989, badlands, 0, badlands, badlands, badlands, ROT0, "Atari Games", "Bad Lands" )
