#include "driver.h"
#include "vidhrdw/ppu2c03b.h"
#include "machine/rp5h01.h"

/* Globals */
int pc10_sdcs;			/* ShareD Chip Select */
int pc10_dispmask;		/* Display Mask */
int pc10_nmi_enable;	/* nmi enable */
int pc10_dog_di;		/* watchdog disable */
int pc10_int_detect;	/* interrupt detect */
int pc10_gun_controller;

/* Locals */
static int cart_sel;
static int cntrl_mask;
static int input_latch[2];
static int mirroring;

/*************************************
 *
 *	Init machine
 *
 *************************************/
void pc10_init_machine( void )
{
	/* initialize latches and flip-flops */
	pc10_nmi_enable = pc10_dog_di = pc10_dispmask = pc10_sdcs = pc10_int_detect = 0;

	cart_sel = 0;
	cntrl_mask = 1;

	input_latch[0] = input_latch[1] = 0;

	/* reset the security chip */
	RP5H01_enable_w( 0, 0 );
	RP5H01_0_reset_w( 0, 0 );
	RP5H01_0_reset_w( 0, 1 );
	RP5H01_enable_w( 0, 1 );

	/* reset the ppu */
	ppu2c03b_reset( 0, /* cpu_getscanlineperiod() * */ 2 );

	ppu2c03b_set_mirroring( 0, mirroring );
}

/*************************************
 *
 *	BIOS ports handling
 *
 *************************************/
READ_HANDLER( pc10_port_0_r )
{
	return readinputport( 0 ) | ( ( ~pc10_int_detect & 1 ) << 3 );
}

WRITE_HANDLER( pc10_SDCS_w )
{
	/*
		Hooked to CLR on LS194A - Sheet 2, bottom left.
		Drives character and color code to 0.
		It's used to keep the screen black during redraws.
		Also hooked to the video sram. Prevent writes.
	*/
	pc10_sdcs = ~data & 1;
}

WRITE_HANDLER( pc10_CNTRLMASK_w )
{
	cntrl_mask = ~data & 1;
}

WRITE_HANDLER( pc10_DISPMASK_w )
{
	pc10_dispmask = ~data & 1;
}

WRITE_HANDLER( pc10_SOUNDMASK_w )
{
	/* should mute the APU - unimplemented yet */
}

WRITE_HANDLER( pc10_NMIENABLE_w )
{
	pc10_nmi_enable = data & 1;
}

WRITE_HANDLER( pc10_DOGDI_w )
{
	pc10_dog_di = data & 1;
}

WRITE_HANDLER( pc10_GAMERES_w )
{
	cpu_set_reset_line( 1, ( data & 1 ) ? CLEAR_LINE : ASSERT_LINE );
}

WRITE_HANDLER( pc10_GAMESTOP_w )
{
	cpu_set_halt_line( 1, ( data & 1 ) ? CLEAR_LINE : ASSERT_LINE );
}

WRITE_HANDLER( pc10_PPURES_w )
{
	if ( data & 1 )
		ppu2c03b_reset( 0, /* cpu_getscanlineperiod() * */ 2 );
}

READ_HANDLER( pc10_detectclr_r )
{
	pc10_int_detect = 0;

	return 0;
}

WRITE_HANDLER( pc10_CARTSEL_w )
{
	cart_sel &= ~( 1 << offset );
	cart_sel |= ( data & 1 ) << offset;
}


/*************************************
 *
 *	RP5H01 handling
 *
 *************************************/
READ_HANDLER( pc10_prot_r )
{
	int data = 0xe7;

	/* we only support a single cart connected at slot 0 */
	if ( cart_sel == 0 )
	{
		RP5H01_0_enable_w( 0, 0 );
		data |= ( ( ~RP5H01_counter_r( 0 ) ) << 4 ) & 0x10;	/* D4 */
		data |= ( ( RP5H01_data_r( 0 ) ) << 3 ) & 0x08;		/* D3 */
		RP5H01_0_enable_w( 0, 1 );
	}
	return data;
}

WRITE_HANDLER( pc10_prot_w )
{
	/* we only support a single cart connected at slot 0 */
	if ( cart_sel == 0 )
	{
		RP5H01_0_enable_w( 0, 0 );
		RP5H01_0_test_w( 0, data & 0x10 );		/* D4 */
		RP5H01_0_clock_w( 0, data & 0x08 );		/* D3 */
		RP5H01_0_reset_w( 0, ~data & 0x01 );	/* D0 */
		RP5H01_0_enable_w( 0, 1 );

		/* this thing gets dense at some point						*/
		/* it wants to jump and execute an opcode at $ffff, wich	*/
		/* is the actual protection memory area						*/
		/* setting the whole 0x2000 region every time is a waste	*/
		/* so we just set $ffff with the current value				*/
		memory_region( REGION_CPU1 )[0xffff] = pc10_prot_r(0);
	}
}


/*************************************
 *
 *	Input Ports
 *
 *************************************/
WRITE_HANDLER( pc10_in0_w )
{
	/* Toggling bit 0 high then low resets both controllers */
	if ( data & 1 )
		return;

	/* load up the latches */
	input_latch[0] = readinputport( 3 );
	input_latch[1] = readinputport( 4 );

	/* apply any masking from the BIOS */
	if ( cntrl_mask )
	{
		/* mask out select and start */
		input_latch[0] &= ~0x0c;
	}
}

READ_HANDLER( pc10_in0_r )
{
	int ret = ( input_latch[0] ) & 1;

	/* shift */
	input_latch[0] >>= 1;

	/* some games expect bit 6 to be set because the last entry on the data bus shows up */
	/* in the unused upper 3 bits, so typically a read from $4016 leaves 0x40 there. */
	ret |= 0x40;

	return ret;
}

READ_HANDLER( pc10_in1_r )
{
	int ret = ( input_latch[1] ) & 1;

	/* shift */
	input_latch[1] >>= 1;

	/* do the gun thing */
	if ( pc10_gun_controller )
	{
		int trigger = readinputport( 3 );
		int x = readinputport( 5 );
		int y = readinputport( 6 );
		int pix, color_base;
		UINT16 *pens = Machine->pens;

		/* no sprite hit (yet) */
		ret |= 0x08;

		/* get the pixel at the gun position */
		pix = ppu2c03b_get_pixel( 0, x, y );

		/* get the color base from the ppu */
		color_base = ppu2c03b_get_colorbase( 0 );

		/* look at the screen and see if the cursor is over a bright pixel */
		if ( ( pix == pens[color_base+0x20] ) || ( pix == pens[color_base+0x30] ) ||
			 ( pix == pens[color_base+0x33] ) || ( pix == pens[color_base+0x34] ) )
		{
			ret &= ~0x08; /* sprite hit */
		}

		/* now, add the trigger if not masked */
		if ( !cntrl_mask )
		{
			ret |= ( trigger & 2 ) << 3;
		}
	}

	/* some games expect bit 6 to be set because the last entry on the data bus shows up */
	/* in the unused upper 3 bits, so typically a read from $4016 leaves 0x40 there. */
	ret |= 0x40;

	return ret;
}

/* RP5H01 interface */
static struct RP5H01_interface rp5h01_interface =
{
	1,
	{ REGION_USER1 },
	{ 0 }
};

/*************************************
 *
 *	Common init for all games
 *
 *************************************/
void init_playch10( void )
{
	/* initialize the security chip */
	if ( RP5H01_init( &rp5h01_interface ) )
	{
		exit( -1 );
	}

	/* set the controller to default */
	pc10_gun_controller = 0;

	/* default mirroring */
	mirroring = PPU_MIRROR_NONE;
}

/**********************************************************************************
 *
 *	Game and Board-specific initialization
 *
 **********************************************************************************/

/* Gun games */

void init_pc_gun( void )
{
	/* common init */
	init_playch10();

	/* set the control type */
	pc10_gun_controller = 1;
}


/* Horizontal mirroring */

void init_pc_hrz( void )
{
	/* common init */
	init_playch10();

	/* setup mirroring */
	mirroring = PPU_MIRROR_HORZ;
}

/* MMC1 mapper, used by D and F boards */

static int mmc1_shiftreg;
static int mmc1_shiftcount;
static int mmc1_rom_mask;

static WRITE_HANDLER( mmc1_rom_switch_w )
{
	/* basically, a MMC1 mapper from the nes */
	static int size16k, switchlow, vrom4k;

	int reg = ( offset >> 13 );

	/* reset mapper */
	if ( data & 0x80 )
	{
		mmc1_shiftreg = mmc1_shiftcount = 0;

		size16k = 1;
		switchlow = 1;
		vrom4k = 0;

		return;
	}

	/* see if we need to clock in data */
	if ( mmc1_shiftcount < 5 )
	{
		mmc1_shiftreg >>= 1;
		mmc1_shiftreg |= ( data & 1 ) << 4;
		mmc1_shiftcount++;
	}

	/* are we done shifting? */
	if ( mmc1_shiftcount == 5 )
	{
		/* reset count */
		mmc1_shiftcount = 0;

		/* apply data to registers */
		switch( reg )
		{
			case 0:		/* mirroring and options */
				{
					int _mirroring;

					vrom4k = mmc1_shiftreg & 0x10;
					size16k = mmc1_shiftreg & 0x08;
					switchlow = mmc1_shiftreg & 0x04;

					switch( mmc1_shiftreg & 3 )
					{
						case 0:
							_mirroring = PPU_MIRROR_LOW;
						break;

						case 1:
							_mirroring = PPU_MIRROR_HIGH;
						break;

						case 2:
							_mirroring = PPU_MIRROR_VERT;
						break;

						default:
						case 3:
							_mirroring = PPU_MIRROR_HORZ;
						break;
					}

					/* apply mirroring */
					ppu2c03b_set_mirroring( 0, _mirroring );
				}
			break;

			case 1:	/* video rom banking - bank 0 - 4k or 8k */
				ppu2c03b_set_videorom_bank( 0, 0, ( vrom4k ) ? 4 : 8, ( mmc1_shiftreg & 0x1f ), 256 );
			break;

			case 2: /* video rom banking - bank 1 - 4k only */
				if ( vrom4k )
					ppu2c03b_set_videorom_bank( 0, 4, 4, ( mmc1_shiftreg & 0x1f ), 256 );
			break;

			case 3:	/* program banking */
				{
					int bank = ( mmc1_shiftreg & mmc1_rom_mask ) * 0x4000;

					if ( !size16k )
					{
						/* switch 32k */
						memcpy( &memory_region( REGION_CPU2 )[0x08000], &memory_region( REGION_CPU2 )[0x010000+bank], 0x8000 );
					}
					else
					{
						/* switch 16k */
						if ( switchlow )
						{
							/* low */
							memcpy( &memory_region( REGION_CPU2 )[0x08000], &memory_region( REGION_CPU2 )[0x010000+bank], 0x4000 );
						}
						else
						{
							/* high */
							memcpy( &memory_region( REGION_CPU2 )[0x0c000], &memory_region( REGION_CPU2 )[0x010000+bank], 0x4000 );
						}
					}
				}
			break;
		}
	}
}

/**********************************************************************************/

/* A Board games (Track & Field) - BROKEN? - FIX ME? */

static WRITE_HANDLER( aboard_vrom_switch_w )
{
	ppu2c03b_set_videorom_bank( 0, 0, 8, ( data & 3 ), 512 );
}

void init_pcaboard( void )
{
	/* switches vrom with writes to the $803e-$8041 area */
	install_mem_write_handler( 1, 0x8000, 0x8fff, aboard_vrom_switch_w );

	/* common init */
	init_playch10();
}

/**********************************************************************************/

/* B Board games (Contra, Rush N' Attach, Pro Wrestling) */

static WRITE_HANDLER( bboard_rom_switch_w )
{
	int bankoffset = 0x10000 + ( ( data & 7 ) * 0x4000 );

	memcpy( &memory_region( REGION_CPU2 )[0x08000], &memory_region( REGION_CPU2 )[bankoffset], 0x4000 );
}

void init_pcbboard( void )
{
	/* We do manual banking, in case the code falls through */
	/* Copy the initial banks */
	memcpy( &memory_region( REGION_CPU2 )[0x08000], &memory_region( REGION_CPU2 )[0x28000], 0x8000 );

	/* Roms are banked at $8000 to $bfff */
	install_mem_write_handler( 1, 0x8000, 0xffff, bboard_rom_switch_w );

	/* common init */
	init_playch10();

	/* set the mirroring here */
	mirroring = PPU_MIRROR_VERT;
}

/**********************************************************************************/

/* C Board games (The Goonies) */

static WRITE_HANDLER( cboard_vrom_switch_w )
{
	ppu2c03b_set_videorom_bank( 0, 0, 8, ( ( data >> 1 ) & 1 ), 512 );
}

void init_pccboard( void )
{
	/* switches vrom with writes to $6000 */
	install_mem_write_handler( 1, 0x6000, 0x6000, cboard_vrom_switch_w );

	/* common init */
	init_playch10();
}

/**********************************************************************************/

/* D Board games (Rad Racer) */

void init_pcdboard( void )
{
	/* We do manual banking, in case the code falls through */
	/* Copy the initial banks */
	memcpy( &memory_region( REGION_CPU2 )[0x08000], &memory_region( REGION_CPU2 )[0x28000], 0x8000 );

	mmc1_rom_mask = 0x07;

	/* MMC mapper at writes to $8000-$ffff */
	install_mem_write_handler( 1, 0x8000, 0xffff, mmc1_rom_switch_w );

	/* common init */
	init_playch10();
}

/**********************************************************************************/

/* E Board games (Mike Tyson's Punchout) - BROKEN - FIX ME */

static WRITE_HANDLER( eboard_rom_switch_w )
{
	/* a variation of mapper 9 on a nes */
	switch( offset & 0x7000 )
	{
		case 0x2000: /* code bank switching */
			{
				int bankoffset = 0x10000 + ( data & 0x0f ) * 0x2000;

				memcpy( &memory_region( REGION_CPU2 )[0x08000], &memory_region( REGION_CPU2 )[bankoffset], 0x2000 );
			}
		break;

		case 0x3000: /* gfx bank 0 - 4k */
			ppu2c03b_set_videorom_bank( 0, 0, 1, data, 256 );
		break;

		case 0x4000: /* gfx bank 0 - 4k */
			ppu2c03b_set_videorom_bank( 0, 0, 4, data, 256 );
		break;

		case 0x5000: /* gfx bank 1 - 4k */
			ppu2c03b_set_videorom_bank( 0, 4, 1, data, 256 );
		break;

		case 0x6000: /* gfx bank 1 - 4k */
			ppu2c03b_set_videorom_bank( 0, 4, 4, data, 256 );
		break;

		case 0x7000: /* mirroring */
			ppu2c03b_set_mirroring( 0, data ? PPU_MIRROR_HORZ : PPU_MIRROR_VERT );
		break;
	}
}

void init_pceboard( void )
{
	/* We do manual banking, in case the code falls through */
	/* Copy the initial banks */
	memcpy( &memory_region( REGION_CPU2 )[0x08000], &memory_region( REGION_CPU2 )[0x28000], 0x8000 );

	/* basically a mapper 9 on a nes */
	install_mem_write_handler( 1, 0x8000, 0xffff, eboard_rom_switch_w );

	/* nvram at $6000-$6fff */
	install_mem_read_handler( 1, 0x6000, 0x6fff, MRA_RAM );
	install_mem_write_handler( 1, 0x6000, 0x6fff, MWA_RAM );

	/* common init */
	init_playch10();
}

/**********************************************************************************/

/* F Board games (Ninja Gaiden, Double Dragon) */

void init_pcfboard( void )
{
	/* We do manual banking, in case the code falls through */
	/* Copy the initial banks */
	memcpy( &memory_region( REGION_CPU2 )[0x08000], &memory_region( REGION_CPU2 )[0x28000], 0x8000 );

	mmc1_rom_mask = 0x07;

	/* MMC mapper at writes to $8000-$ffff */
	install_mem_write_handler( 1, 0x8000, 0xffff, mmc1_rom_switch_w );

	/* common init */
	init_playch10();
}

/**********************************************************************************/

/* G Board games (Super Mario Bros. 3) */

static int gboard_scanline_counter;
static int gboard_scanline_latch;
static int gboard_banks[2];

static void gboard_scanline_cb( int num, int scanline, int vblank, int blanked )
{
	if ( !vblank && !blanked )
	{
		if ( --gboard_scanline_counter == -1 )
		{
			gboard_scanline_counter = gboard_scanline_latch;
			cpu_set_irq_line( 1, 0, PULSE_LINE );
		}
	}
}

static WRITE_HANDLER( gboard_rom_switch_w )
{
	/* basically, a MMC3 mapper from the nes */
	static int last_bank = 0xff;
	static int gboard_command;

	switch( offset & 0x7001 )
	{
		case 0x0000:
			gboard_command = data;

			if ( last_bank != ( data & 0xc0 ) )
			{
				int bank;

				/* reset the banks */
				if ( gboard_command & 0x40 )
				{
					/* high bank */
					bank = gboard_banks[0] * 0x2000 + 0x10000;

					memcpy( &memory_region( REGION_CPU2 )[0x0c000], &memory_region( REGION_CPU2 )[bank], 0x2000 );
					memcpy( &memory_region( REGION_CPU2 )[0x08000], &memory_region( REGION_CPU2 )[0x4c000], 0x2000 );
				}
				else
				{
					/* low bank */
					bank = gboard_banks[0] * 0x2000 + 0x10000;

					memcpy( &memory_region( REGION_CPU2 )[0x08000], &memory_region( REGION_CPU2 )[bank], 0x2000 );
					memcpy( &memory_region( REGION_CPU2 )[0x0c000], &memory_region( REGION_CPU2 )[0x4c000], 0x2000 );
				}

				/* mid bank */
				bank = gboard_banks[1] * 0x2000 + 0x10000;
				memcpy( &memory_region( REGION_CPU2 )[0x0a000], &memory_region( REGION_CPU2 )[bank], 0x2000 );

				last_bank = data & 0xc0;
			}
		break;

		case 0x0001:
			{
				UINT8 cmd = gboard_command & 0x07;
				int page = ( gboard_command & 0x80 ) >> 5;
				int bank;

				switch( cmd )
				{
					case 0:	/* char banking */
					case 1: /* char banking */
						data &= 0xfe;
						page ^= ( cmd << 1 );
						ppu2c03b_set_videorom_bank( 0, page, 2, data, 64 );
					break;

					case 2: /* char banking */
					case 3: /* char banking */
					case 4: /* char banking */
					case 5: /* char banking */
						page ^= cmd + 2;
						ppu2c03b_set_videorom_bank( 0, page, 1, data, 64 );
					break;

					case 6: /* program banking */
						if ( gboard_command & 0x40 )
						{
							/* high bank */
							gboard_banks[0] = data & 0x1f;
							bank = ( gboard_banks[0] ) * 0x2000 + 0x10000;

							memcpy( &memory_region( REGION_CPU2 )[0x0c000], &memory_region( REGION_CPU2 )[bank], 0x2000 );
							memcpy( &memory_region( REGION_CPU2 )[0x08000], &memory_region( REGION_CPU2 )[0x4c000], 0x2000 );
						}
						else
						{
							/* low bank */
							gboard_banks[0] = data & 0x1f;
							bank = ( gboard_banks[0] ) * 0x2000 + 0x10000;

							memcpy( &memory_region( REGION_CPU2 )[0x08000], &memory_region( REGION_CPU2 )[bank], 0x2000 );
							memcpy( &memory_region( REGION_CPU2 )[0x0c000], &memory_region( REGION_CPU2 )[0x4c000], 0x2000 );
						}
					break;

					case 7: /* program banking */
						{
							/* mid bank */
							gboard_banks[1] = data & 0x1f;
							bank = gboard_banks[1] * 0x2000 + 0x10000;

							memcpy( &memory_region( REGION_CPU2 )[0x0a000], &memory_region( REGION_CPU2 )[bank], 0x2000 );
						}
					break;
				}
			}
		break;

		case 0x2000: /* mirroring */
			if ( data & 0x40 )
				ppu2c03b_set_mirroring( 0, PPU_MIRROR_HIGH );
			else
				ppu2c03b_set_mirroring( 0, ( data & 1 ) ? PPU_MIRROR_HORZ : PPU_MIRROR_VERT );
		break;

		case 0x2001: /* enable ram at $6000 */
			/* ignored - we always enable it */
		break;

		case 0x4000: /* scanline counter */
			gboard_scanline_counter = data;
		break;

		case 0x4001: /* scanline latch */
			gboard_scanline_latch = data;
		break;

		case 0x6000: /* disable irqs */
			gboard_scanline_counter = gboard_scanline_latch;
			ppu2c03b_set_scanline_callback( 0, 0 );
		break;

		case 0x6001: /* enable irqs */
			ppu2c03b_set_scanline_callback( 0, gboard_scanline_cb );
		break;
	}
}

void init_pcgboard( void )
{
	/* We do manual banking, in case the code falls through */
	/* Copy the initial banks */
	memcpy( &memory_region( REGION_CPU2 )[0x08000], &memory_region( REGION_CPU2 )[0x4c000], 0x4000 );
	memcpy( &memory_region( REGION_CPU2 )[0x0c000], &memory_region( REGION_CPU2 )[0x4c000], 0x4000 );

	/* MMC3 mapper at writes to $8000-$ffff */
	install_mem_write_handler( 1, 0x8000, 0xffff, gboard_rom_switch_w );

	/* extra ram at $6000-$7fff */
	install_mem_read_handler( 1, 0x6000, 0x7fff, MRA_RAM );
	install_mem_write_handler( 1, 0x6000, 0x7fff, MWA_RAM );

	gboard_banks[0] = 0x1e;
	gboard_banks[1] = 0x1f;
	gboard_scanline_counter = 0;
	gboard_scanline_latch = 0;

	/* common init */
	init_playch10();
}

/**********************************************************************************/

/* i Board games (Captain Sky Hawk) */

static WRITE_HANDLER( iboard_rom_switch_w )
{
	int bank = data & 3;

	if ( data & 0x10 )
		ppu2c03b_set_mirroring( 0, PPU_MIRROR_HIGH );
	else
		ppu2c03b_set_mirroring( 0, PPU_MIRROR_LOW );

	memcpy( &memory_region( REGION_CPU2 )[0x08000], &memory_region( REGION_CPU2 )[bank * 0x8000 + 0x10000], 0x8000 );
}

void init_pciboard( void )
{
	/* We do manual banking, in case the code falls through */
	/* Copy the initial banks */
	memcpy( &memory_region( REGION_CPU2 )[0x08000], &memory_region( REGION_CPU2 )[0x10000], 0x8000 );

	/* Roms are banked at $8000 to $bfff */
	install_mem_write_handler( 1, 0x8000, 0xffff, iboard_rom_switch_w );

	/* common init */
	init_playch10();
}

/**********************************************************************************/

/* K Board games (Mario Open Golf) */

void init_pckboard( void )
{
	/* We do manual banking, in case the code falls through */
	/* Copy the initial banks */
	memcpy( &memory_region( REGION_CPU2 )[0x08000], &memory_region( REGION_CPU2 )[0x48000], 0x8000 );

	mmc1_rom_mask = 0x0f;

	/* Roms are banked at $8000 to $bfff */
	install_mem_write_handler( 1, 0x8000, 0xffff, mmc1_rom_switch_w );

	/* common init */
	init_playch10();
}
