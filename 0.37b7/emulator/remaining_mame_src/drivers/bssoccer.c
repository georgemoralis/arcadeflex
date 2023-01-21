/***************************************************************************

							-= Back Street Soccer =-

					driver by	Luca Elia (eliavit@unina.it)


CPU:	68000   +  Z80 [Music]  +  Z80 x 2 [4 Bit PCM]
Sound:	YM2151  +  DAC x 4

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"

/* Variables and functions only used here */

/* Variables that vidhrdw has access to */

/* Variables and functions defined in vidhrdw */

extern unsigned char *bssoccer_vregs;

WRITE_HANDLER( bssoccer_vregs_w );
void bssoccer_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh);


/***************************************************************************


								Main CPU


***************************************************************************/

static struct MemoryReadAddress bssoccer_readmem[] =
{
	{ 0x000000, 0x1fffff, MRA_ROM					},	// ROM
	{ 0x200000, 0x203fff, MRA_BANK1					},	// RAM
	{ 0x400000, 0x4001ff, MRA_BANK2					},	// Palette
	{ 0x400200, 0x400fff, MRA_BANK3					},	//
	{ 0x600000, 0x61ffff, MRA_BANK4					},	// Sprites
	{ 0xa00000, 0xa00001, input_port_0_r			},	// P1 (Inputs)
	{ 0xa00002, 0xa00003, input_port_1_r			},	// P2
	{ 0xa00004, 0xa00005, input_port_2_r			},	// P3
	{ 0xa00006, 0xa00007, input_port_3_r			},	// P4
	{ 0xa00008, 0xa00009, input_port_4_r			},	// DSWs
	{ 0xa0000a, 0xa0000b, input_port_5_r			},	// Coins
	{ -1 }
};

static struct MemoryWriteAddress bssoccer_writemem[] =
{
	{ 0x000000, 0x1fffff, MWA_ROM							},	// ROM
	{ 0x200000, 0x203fff, MWA_BANK1							},	// RAM
	{ 0x400000, 0x4001ff, paletteram_xBBBBBGGGGGRRRRR_word_w, &paletteram	},	// Palette
	{ 0x400200, 0x400fff, MWA_BANK3							},	//
	{ 0x600000, 0x61ffff, MWA_BANK4, &spriteram				},	// Sprites
	{ 0xa00000, 0xa00009, bssoccer_vregs_w, &bssoccer_vregs	},	// Latches + Registers
	{ -1 }
};


/***************************************************************************


									Z80 #1

		Plays the music (YM2151) and controls the 2 Z80s in charge
		of playing the PCM samples


***************************************************************************/

static struct MemoryReadAddress bssoccer_sound_readmem[] =
{
	{ 0x0000, 0x7fff, MRA_ROM					},	// ROM
	{ 0xf000, 0xf7ff, MRA_RAM					},	// RAM
	{ 0xf801, 0xf801, YM2151_status_port_0_r	},	// YM2151
	{ 0xfc00, 0xfc00, soundlatch_r				},	// From Main CPU
	{ -1 }
};

static struct MemoryWriteAddress bssoccer_sound_writemem[] =
{
	{ 0x0000, 0x7fff, MWA_ROM					},	// ROM
	{ 0xf000, 0xf7ff, MWA_RAM					},	// RAM
	{ 0xf800, 0xf800, YM2151_register_port_0_w	},	// YM2151
	{ 0xf801, 0xf801, YM2151_data_port_0_w		},	//
	{ 0xfd00, 0xfd00, soundlatch2_w				},	// To PCM Z80 #1
	{ 0xfe00, 0xfe00, soundlatch3_w				},	// To PCM Z80 #2
	{ -1 }
};


/***************************************************************************


								Z80 #2 & #3

		Dumb PCM samples players (e.g they don't even have RAM!)


***************************************************************************/

/* Bank Switching */

static WRITE_HANDLER( bssoccer_pcm_1_bankswitch_w )
{
	unsigned char *RAM = memory_region(REGION_CPU3);
	int bank = data & 7;
	cpu_setbank(14, &RAM[bank * 0x10000 + 0x1000]);
}

static WRITE_HANDLER( bssoccer_pcm_2_bankswitch_w )
{
	unsigned char *RAM = memory_region(REGION_CPU4);
	int bank = data & 7;
	cpu_setbank(15, &RAM[bank * 0x10000 + 0x1000]);
}



/* Memory maps: Yes, *no* RAM */

static struct MemoryReadAddress bssoccer_pcm_1_readmem[] =
{
	{ 0x0000, 0x0fff, MRA_ROM			},	// ROM
	{ 0x1000, 0xffff, MRA_BANK14		},	// Banked ROM
	{ -1 }
};
static struct MemoryWriteAddress bssoccer_pcm_1_writemem[] =
{
	{ 0x0000, 0xffff, MWA_ROM			},	// ROM
	{ -1 }
};


static struct MemoryReadAddress bssoccer_pcm_2_readmem[] =
{
	{ 0x0000, 0x0fff, MRA_ROM			},	// ROM
	{ 0x1000, 0xffff, MRA_BANK15		},	// Banked ROM
	{ -1 }
};
static struct MemoryWriteAddress bssoccer_pcm_2_writemem[] =
{
	{ 0x0000, 0xffff, MWA_ROM			},	// ROM
	{ -1 }
};



/* 2 DACs per CPU - 4 bits per sample */

static WRITE_HANDLER( bssoccer_DAC_1_w )
{
	DAC_data_w( 0 + (offset & 1), (data & 0xf) * 0x11 );
}

static WRITE_HANDLER( bssoccer_DAC_2_w )
{
	DAC_data_w( 2 + (offset & 1), (data & 0xf) * 0x11 );
}



static struct IOReadPort bssoccer_pcm_1_readport[] =
{
	{ 0x00, 0x00, soundlatch2_r					},	// From The Sound Z80
	{ -1 }
};
static struct IOWritePort bssoccer_pcm_1_writeport[] =
{
	{ 0x00, 0x01, bssoccer_DAC_1_w				},	// 2 x DAC
	{ 0x03, 0x03, bssoccer_pcm_1_bankswitch_w	},	// Rom Bank
	{ -1 }
};

static struct IOReadPort bssoccer_pcm_2_readport[] =
{
	{ 0x00, 0x00, soundlatch3_r					},	// From The Sound Z80
	{ -1 }
};
static struct IOWritePort bssoccer_pcm_2_writeport[] =
{
	{ 0x00, 0x01, bssoccer_DAC_2_w				},	// 2 x DAC
	{ 0x03, 0x03, bssoccer_pcm_2_bankswitch_w	},	// Rom Bank
	{ -1 }
};




/***************************************************************************


								Input Ports


***************************************************************************/

#define	JOY(_n_) \
	PORT_BIT(  0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_UP     |  IPF_PLAYER##_n_ ) \
	PORT_BIT(  0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN   |  IPF_PLAYER##_n_ ) \
	PORT_BIT(  0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT   |  IPF_PLAYER##_n_ ) \
	PORT_BIT(  0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT  |  IPF_PLAYER##_n_ ) \
	PORT_BIT(  0x0010, IP_ACTIVE_LOW, IPT_BUTTON1         |  IPF_PLAYER##_n_ ) \
	PORT_BIT(  0x0020, IP_ACTIVE_LOW, IPT_BUTTON2         |  IPF_PLAYER##_n_ ) \
	PORT_BIT(  0x0040, IP_ACTIVE_LOW, IPT_BUTTON3         |  IPF_PLAYER##_n_ ) \
	PORT_BIT(  0x0080, IP_ACTIVE_LOW, IPT_START##_n_                         )

INPUT_PORTS_START( bssoccer )

	PORT_START	// IN0 - Player 1
	JOY(1)

	PORT_START	// IN1 - Player 2
	JOY(2)

	PORT_START	// IN2 - Player 3
	JOY(3)

	PORT_START	// IN3 - Player 4
	JOY(4)

	PORT_START	// IN4 - 2 DSWs
	PORT_DIPNAME( 0x0007, 0x0007, DEF_STR( Coinage ) )
	PORT_DIPSETTING(      0x0000, DEF_STR( 4C_1C ) )
	PORT_DIPSETTING(      0x0001, DEF_STR( 3C_1C ) )
	PORT_DIPSETTING(      0x0002, DEF_STR( 2C_1C ) )
	PORT_DIPSETTING(      0x0007, DEF_STR( 1C_1C ) )
	PORT_DIPSETTING(      0x0006, DEF_STR( 1C_2C ) )
	PORT_DIPSETTING(      0x0005, DEF_STR( 1C_3C ) )
	PORT_DIPSETTING(      0x0004, DEF_STR( 1C_4C ) )
	PORT_DIPSETTING(      0x0003, DEF_STR( 1C_5C ) )
	PORT_DIPNAME( 0x0018, 0x0018, DEF_STR( Difficulty ) )
	PORT_DIPSETTING(      0x0010, "Easy"     )
	PORT_DIPSETTING(      0x0018, "Normal"   )
	PORT_DIPSETTING(      0x0008, "Hard"     )
	PORT_DIPSETTING(      0x0000, "Hardest?" )
	PORT_DIPNAME( 0x0020, 0x0020, DEF_STR( Demo_Sounds ) )
	PORT_DIPSETTING(      0x0000, DEF_STR( Off ) )
	PORT_DIPSETTING(      0x0020, DEF_STR( On ) )
	PORT_DIPNAME( 0x0040, 0x0040, DEF_STR( Flip_Screen ) )
	PORT_DIPSETTING(      0x0040, DEF_STR( Off ) )
	PORT_DIPSETTING(      0x0000, DEF_STR( On ) )
	PORT_SERVICE( 0x0080, IP_ACTIVE_LOW )

	PORT_DIPNAME( 0x0300, 0x0300, "Play Time P1" )
	PORT_DIPSETTING(      0x0300, "1:30" )
	PORT_DIPSETTING(      0x0200, "1:45" )
	PORT_DIPSETTING(      0x0100, "2:00" )
	PORT_DIPSETTING(      0x0000, "2:15" )
	PORT_DIPNAME( 0x0c00, 0x0c00, "Play Time P2" )
	PORT_DIPSETTING(      0x0c00, "1:30" )
	PORT_DIPSETTING(      0x0800, "1:45" )
	PORT_DIPSETTING(      0x0400, "2:00" )
	PORT_DIPSETTING(      0x0000, "2:15" )
	PORT_DIPNAME( 0x3000, 0x3000, "Play Time P3" )
	PORT_DIPSETTING(      0x3000, "1:30" )
	PORT_DIPSETTING(      0x2000, "1:45" )
	PORT_DIPSETTING(      0x1000, "2:00" )
	PORT_DIPSETTING(      0x0000, "2:15" )
	PORT_DIPNAME( 0xc000, 0xc000, "Play Time P4" )
	PORT_DIPSETTING(      0xc000, "1:30" )
	PORT_DIPSETTING(      0x8000, "1:45" )
	PORT_DIPSETTING(      0x4000, "2:00" )
	PORT_DIPSETTING(      0x0000, "2:15" )

	PORT_START	// IN5 - Coins
	PORT_DIPNAME( 0x0001, 0x0001, "Copyright" )			// these 4 are shown in test mode
	PORT_DIPSETTING(      0x0001, "Distributer Unico" )
	PORT_DIPSETTING(      0x0000, "All Rights Reserved" )
	PORT_DIPNAME( 0x0002, 0x0002, DEF_STR( Unknown ) )	// used!
	PORT_DIPSETTING(      0x0002, DEF_STR( Off ) )
	PORT_DIPSETTING(      0x0000, DEF_STR( On ) )
	PORT_DIPNAME( 0x0002, 0x0002, DEF_STR( Unknown ) )
	PORT_DIPSETTING(      0x0004, DEF_STR( Off ) )
	PORT_DIPSETTING(      0x0000, DEF_STR( On ) )
	PORT_DIPNAME( 0x0002, 0x0002, DEF_STR( Unknown ) )
	PORT_DIPSETTING(      0x0008, DEF_STR( Off ) )
	PORT_DIPSETTING(      0x0000, DEF_STR( On ) )
	PORT_BIT(  0x0010, IP_ACTIVE_LOW,  IPT_COIN1   )
	PORT_BIT(  0x0020, IP_ACTIVE_LOW,  IPT_COIN2   )
	PORT_BIT(  0x0040, IP_ACTIVE_LOW,  IPT_COIN3   )
	PORT_BIT(  0x0080, IP_ACTIVE_LOW,  IPT_COIN4   )

INPUT_PORTS_END



/***************************************************************************


								Graphics Layouts


***************************************************************************/

/* Tiles are 8x8x4 but the minimum sprite size is 2x2 tiles */

static struct GfxLayout layout_8x8x4 =
{
	8,8,
	RGN_FRAC(1,2),
	4,
	{ RGN_FRAC(1,2)+0,RGN_FRAC(1,2)+4,	0,4 },
	{3,2,1,0, 11,10,9,8},
	{0*16,1*16,2*16,3*16,4*16,5*16,6*16,7*16},
	8*8*2
};

static struct GfxDecodeInfo bssoccer_gfxdecodeinfo[] =
{
	{ REGION_GFX1, 0, &layout_8x8x4, 0, 16 }, // [0] Sprites
	{ -1 }
};




/***************************************************************************


								Machine drivers


***************************************************************************/

static struct YM2151interface bssoccer_ym2151_interface =
{
	1,
	3579545,	/* ? */
	{ YM3012_VOL(10,MIXER_PAN_LEFT,10,MIXER_PAN_RIGHT) },
	{ 0 },		/* irq handler */
	{ 0 }		/* port write handler */
};

static struct DACinterface bssoccer_dac_interface =
{
	4,
	{
		MIXER(23,MIXER_PAN_LEFT), MIXER(23,MIXER_PAN_RIGHT),
		MIXER(23,MIXER_PAN_LEFT), MIXER(23,MIXER_PAN_RIGHT)
	}
};

int bssoccer_interrupt(void)
{
	switch (cpu_getiloops())
	{
		case 0:		return 1;
		case 1:		return 2;
		default:	return ignore_interrupt();
	}
}

static const struct MachineDriver machine_driver_bssoccer =
{
	{
		{
			CPU_M68000,
			8000000,	/* ? */
			bssoccer_readmem, bssoccer_writemem,0,0,
			bssoccer_interrupt, 2
		},
		{
			CPU_Z80 | CPU_AUDIO_CPU,	/* Z80B */
			3579545,	/* ? */
			bssoccer_sound_readmem,	 bssoccer_sound_writemem,
			0,0,
			ignore_interrupt, 1		/* No interrupts! */
		},
		{
			CPU_Z80 | CPU_AUDIO_CPU,	/* Z80B */
			5000000,	/* ? */
			bssoccer_pcm_1_readmem,	 bssoccer_pcm_1_writemem,
			bssoccer_pcm_1_readport, bssoccer_pcm_1_writeport,
			ignore_interrupt, 1		/* No interrupts! */
		},
		{
			CPU_Z80 | CPU_AUDIO_CPU,	/* Z80B */
			5000000,	/* ? */
			bssoccer_pcm_2_readmem,	 bssoccer_pcm_2_writemem,
			bssoccer_pcm_2_readport, bssoccer_pcm_2_writeport,
			ignore_interrupt, 1		/* No interrupts! */
		}
	},
	60,DEFAULT_60HZ_VBLANK_DURATION,
	100,
	0,

	/* video hardware */
	256, 256, { 0, 256-1, 0+16, 256-16-1 },
	bssoccer_gfxdecodeinfo,
	256, 256,
	0,
	VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
	0,
	0,	/* No need for a vh_start: we only have sprites */
	0,
	bssoccer_vh_screenrefresh,

	/* sound hardware */
	SOUND_SUPPORTS_STEREO,0,0,0,
	{
		{
			SOUND_YM2151,
			&bssoccer_ym2151_interface
		},
		{
			SOUND_DAC,
			&bssoccer_dac_interface
		},
	}
};


/***************************************************************************


								ROMs Loading


***************************************************************************/


/***************************************************************************

							[ Back Street Soccer ]

  68000-10  32MHz
            14.318MHz
  01   02                    12
  03   04                   Z80B
  6264 6264       YM2151
                  6116
                   11      13
  62256           Z80B    Z80B
  62256
  62256   05 06                  SW2
          07 08                  SW1
          09 10          6116-45
                                     6116-45
                         6116-45     6116-45

***************************************************************************/

ROM_START( bssoccer )

	ROM_REGION( 0x200000, REGION_CPU1 )		/* 68000 Code */
	ROM_LOAD_EVEN( "02", 0x000000, 0x080000, 0x32871005 )
	ROM_LOAD_ODD(  "01", 0x000000, 0x080000, 0xace00db6 )
	ROM_LOAD_EVEN( "04", 0x100000, 0x080000, 0x25ee404d )
	ROM_LOAD_ODD(  "03", 0x100000, 0x080000, 0x1a131014 )

	ROM_REGION( 0x010000, REGION_CPU2 )		/* Z80 #1 - Music */
	ROM_LOAD( "11", 0x000000, 0x010000, 0xdf7ae9bc ) // 1xxxxxxxxxxxxxxx = 0xFF

	ROM_REGION( 0x080000, REGION_CPU3 )		/* Z80 #2 - PCM */
	ROM_LOAD( "13", 0x000000, 0x080000, 0x2b273dca )

	ROM_REGION( 0x080000, REGION_CPU4 )		/* Z80 #3 - PCM */
	ROM_LOAD( "12", 0x000000, 0x080000, 0x6b73b87b )

	ROM_REGION( 0x300000, REGION_GFX1 | REGIONFLAG_DISPOSE )	/* Sprites */
	ROM_LOAD( "05", 0x000000, 0x080000, 0xa5245bd4 )
	ROM_LOAD( "07", 0x080000, 0x080000, 0xfdb765c2 )
	ROM_LOAD( "09", 0x100000, 0x080000, 0x0e82277f )
	ROM_LOAD( "06", 0x180000, 0x080000, 0xd42ce84b )
	ROM_LOAD( "08", 0x200000, 0x080000, 0x96cd2136 )
	ROM_LOAD( "10", 0x280000, 0x080000, 0x1ca94d21 )

ROM_END


void init_bssoccer(void)
{
	unsigned char *RAM	=	memory_region(REGION_GFX1);
	int i, len			=	memory_region_length(REGION_GFX1);

	for (i=0;i<len;i++)	RAM[i]^=0xff;	// invert all the bits of sprites
}


GAME( 1996, bssoccer, 0, bssoccer, bssoccer, bssoccer, ROT0, "Suna", "Back Street Soccer" )
