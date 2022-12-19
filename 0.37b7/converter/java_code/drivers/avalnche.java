/***************************************************************************

Atari Avalanche Driver

Memory Map:
				0000-1FFF				RAM
				2000-2FFF		R		INPUTS
				3000-3FFF		W		WATCHDOG
				4000-4FFF		W		OUTPUTS
				5000-5FFF		W		SOUND LEVEL
				6000-7FFF		R		PROGRAM ROM
				8000-DFFF				UNUSED
				E000-FFFF				PROGRAM ROM (Remapped)

If you have any questions about how this driver works, don't hesitate to
ask.  - Mike Balfour (mab22@po.cwru.edu)
***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class avalnche
{
	
	/* machine/avalnche.c */
	
	/* vidhrdw/avalnche.c */
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x1fff, MRA_RAM ), /* RAM SEL */
		new MemoryReadAddress( 0x2000, 0x2fff, avalnche_input_r ), /* INSEL */
		new MemoryReadAddress( 0x6000, 0x7fff, MRA_ROM ), /* ROM1-ROM2 */
		new MemoryReadAddress( 0xe000, 0xffff, MRA_ROM ), /* ROM2 for 6502 vectors */
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x1fff, avalnche_videoram_w, videoram, videoram_size ), /* DISPLAY */
		new MemoryWriteAddress( 0x3000, 0x3fff, MWA_NOP ), /* WATCHDOG */
		new MemoryWriteAddress( 0x4000, 0x4fff, avalnche_output_w ), /* OUTSEL */
		new MemoryWriteAddress( 0x5000, 0x5fff, avalnche_noise_amplitude_w ), /* SOUNDLVL */
		new MemoryWriteAddress( 0x6000, 0x7fff, MWA_ROM ), /* ROM1-ROM2 */
		new MemoryWriteAddress( 0xe000, 0xffff, MWA_ROM ), /* ROM1-ROM2 */
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	
	static InputPortPtr input_ports_avalnche = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* IN0 */
		PORT_BIT (0x03, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* Spare */
		PORT_DIPNAME( 0x0c, 0x04, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x30, 0x00, "Language" );	PORT_DIPSETTING(    0x00, "English" );	PORT_DIPSETTING(    0x30, "German" );	PORT_DIPSETTING(    0x20, "French" );	PORT_DIPSETTING(    0x10, "Spanish" );	PORT_BIT (0x40, IP_ACTIVE_HIGH, IPT_START2 );	PORT_BIT (0x80, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_START();  /* IN1 */
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 );	PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_COIN2 );	PORT_DIPNAME( 0x04, 0x04, "Allow Extended Play" );	PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x08, 0x00, "Lives/Extended Play" );	PORT_DIPSETTING(    0x00, "3/450 points" );	PORT_DIPSETTING(    0x08, "5/750 points" );	PORT_BIT ( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );/* SLAM */
		PORT_BITX( 0x20, IP_ACTIVE_HIGH, IPT_SERVICE | IPF_TOGGLE, DEF_STR ( Service_Mode ); KEYCODE_F2, IP_JOY_NONE )
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON1 );/* Serve */
		PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_VBLANK );/* VBLANK */
	
		PORT_START();  /* IN2 */
		PORT_ANALOG( 0xff, 0x00, IPT_PADDLE, 50, 10, 0x40, 0xb7 );INPUT_PORTS_END(); }}; 
	
	
	
	static unsigned char palette[] =
	{
		0x00,0x00,0x00, /* BLACK */
		0xff,0xff,0xff, /* WHITE */
	};
	
	static void init_palette(UBytePtr game_palette, unsigned short *game_colortable,const UBytePtr color_prom)
	{
		memcpy(game_palette,palette,sizeof(palette));
	}
	
	
	
	static DACinterface dac_interface = new DACinterface
	(
		2,
		new int[] { 100, 100 },
	);
	
	
	static MachineDriver machine_driver_avalnche = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6502,
				12096000/16, 	   /* clock input is the "2H" signal divided by two */
				readmem,writemem,null,null,
				avalnche_interrupt,32	/* interrupt at a 4V frequency for sound */
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* single CPU, no need for interleaving */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 32*8-1 ),
		null,
		sizeof(palette) / sizeof(palette[null]) / 3, null,
		init_palette,
	
		VIDEO_TYPE_RASTER,
		null,
		null,
		0,
		avalnche_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_DAC,
				dac_interface
			)
		}
	
	);
	
	
	
	/***************************************************************************
	
	  Game ROMs
	
	***************************************************************************/
	
	static RomLoadPtr rom_avalnche = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		/* Note: These are being loaded into a bogus location, */
		/*		 They are nibble wide rom images which will be */
		/*		 merged and loaded into the proper place by    */
		/*		 orbit_rom_init()							   */
		ROM_LOAD( "30612.d2",     	0x8800, 0x0800, 0x3f975171 );	ROM_LOAD( "30613.e2",     	0x9000, 0x0800, 0x47a224d3 );	ROM_LOAD( "30611.c2",     	0x9800, 0x0800, 0x0ad07f85 );
		ROM_LOAD( "30615.d3",     	0xa800, 0x0800, 0x3e1a86b4 );	ROM_LOAD( "30616.e3",     	0xb000, 0x0800, 0xf620f0f8 );	ROM_LOAD( "30614.c3",     	0xb800, 0x0800, 0xa12d5d64 );ROM_END(); }}; 
	
	
	
	static public static InitDriverPtr init_avalnche = new InitDriverPtr() { public void handler() 
	{
		UBytePtr rom = memory_region(REGION_CPU1);
		int i;
	
		/* Merge nibble-wide roms together,
		   and load them into 0x6000-0x7fff and e000-ffff */
	
		for(i=0;i<0x2000;i++)
		{
			rom[0x6000+i] = (rom[0x8000+i]<<4)+rom[0xA000+i];
			rom[0xE000+i] = (rom[0x8000+i]<<4)+rom[0xA000+i];
		}
	} };
	
	
	
	public static GameDriver driver_avalnche	   = new GameDriver("1978"	,"avalnche"	,"avalnche.java"	,rom_avalnche,null	,machine_driver_avalnche	,input_ports_avalnche	,init_avalnche	,ROT0	,	"Atari", "Avalanche" )
}
