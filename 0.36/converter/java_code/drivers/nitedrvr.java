/***************************************************************************

Atari Night Driver Driver :)

Memory Map:
		0000-01FF	R/W 	SCRAM (Scratchpad RAM)
		0200-03FF	 W		PFW (Playfield Write)
		0400-05FF	 W		HVC (Horiz/Vert/Char for Roadway)
		0600-07FF	 R		IN0
		0800-09FF	 R		IN1
		0A00-0BFF	 W		OUT0
		0C00-0DFF	 W		OUT1
		0E00-0FFF	 -		OUT2 (Not used)
		8000-83FF	 R		PFR (Playfield Read)
		8400-87FF			Steering Reset
		8800-8FFF	 -		Spare (Not used)
		9000-97FF	 R		Program ROM1
		9800-9FFF	 R		Program ROM2
	   (F800-FFFF)	 R		Program ROM2 - only needed for the 6502 vectors

If you have any questions about how this driver works, don't hesitate to
ask.  - Mike Balfour (mab22@po.cwru.edu)
***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;

public class nitedrvr
{
	
	/* machine/nitedrvr.c */
	
	
	/* vidhrdw/nitedrvr.c */
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x00ff, nitedrvr_ram_r ), /* SCRAM */
		new MemoryReadAddress( 0x0100, 0x01ff, nitedrvr_ram_r ), /* SCRAM */
		new MemoryReadAddress( 0x0600, 0x07ff, nitedrvr_in0_r ),
		new MemoryReadAddress( 0x0800, 0x09ff, nitedrvr_in1_r ),
		new MemoryReadAddress( 0x8000, 0x807f, videoram_r ), /* PFR */
		new MemoryReadAddress( 0x8080, 0x80ff, videoram_r ), /* PFR */
		new MemoryReadAddress( 0x8100, 0x817f, videoram_r ), /* PFR */
		new MemoryReadAddress( 0x8180, 0x81ff, videoram_r ), /* PFR */
		new MemoryReadAddress( 0x8200, 0x827f, videoram_r ), /* PFR */
		new MemoryReadAddress( 0x8280, 0x82ff, videoram_r ), /* PFR */
		new MemoryReadAddress( 0x8300, 0x837f, videoram_r ), /* PFR */
		new MemoryReadAddress( 0x8380, 0x83ff, videoram_r ), /* PFR */
		new MemoryReadAddress( 0x8400, 0x87ff, nitedrvr_steering_reset_r ),
		new MemoryReadAddress( 0x9000, 0x9fff, MRA_ROM ), /* ROM1-ROM2 */
		new MemoryReadAddress( 0xfff0, 0xffff, MRA_ROM ), /* ROM2 for 6502 vectors */
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x00ff, nitedrvr_ram_w, nitedrvr_ram ), /* SCRAM */
		new MemoryWriteAddress( 0x0100, 0x01ff, nitedrvr_ram_w ), /* SCRAM */
		new MemoryWriteAddress( 0x0200, 0x027f, videoram_w, videoram, videoram_size ), /* PFW */
		new MemoryWriteAddress( 0x0280, 0x02ff, videoram_w ), /* PFW */
		new MemoryWriteAddress( 0x0300, 0x037f, videoram_w ), /* PFW */
		new MemoryWriteAddress( 0x0380, 0x03ff, videoram_w ), /* PFW */
		new MemoryWriteAddress( 0x0400, 0x05ff, nitedrvr_hvc_w, nitedrvr_hvc ), /* POSH, POSV, CHAR, Watchdog */
		new MemoryWriteAddress( 0x0a00, 0x0bff, nitedrvr_out0_w ),
		new MemoryWriteAddress( 0x0c00, 0x0dff, nitedrvr_out1_w ),
		new MemoryWriteAddress( 0x8400, 0x87ff, nitedrvr_steering_reset_w ),
		new MemoryWriteAddress( 0x9000, 0x9fff, MWA_ROM ), /* ROM1-ROM2 */
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static InputPortPtr input_ports_nitedrvr = new InputPortPtr(){ public void handler() { 
		PORT_START(); 		/* fake port, gets mapped to Night Driver ports */
			PORT_DIPNAME( 0x30, 0x10, "Cost" );
			PORT_DIPSETTING(	0x00, "2 plays/coin" );
			PORT_DIPSETTING(	0x10, "1 play/coin" );
			PORT_DIPSETTING(	0x20, "1 play/coin" );/* not a typo */
			PORT_DIPSETTING(	0x30, "1 play/2 coins" );
			PORT_DIPNAME( 0xC0, 0x80, "Seconds" );
			PORT_DIPSETTING(	0x00, "50" );
			PORT_DIPSETTING(	0x40, "75" );
			PORT_DIPSETTING(	0x80, "100" );
			PORT_DIPSETTING(	0xC0, "125" );
	
		PORT_START(); 		/* fake port, gets mapped to Night Driver ports */
			PORT_DIPNAME( 0x10, 0x00, "Track Set" );
			PORT_DIPSETTING(	0x00, "Normal" );
			PORT_DIPSETTING(	0x10, "Reverse" );
			PORT_DIPNAME( 0x20, 0x20, "Bonus Time Allowed" );
			PORT_DIPSETTING(	0x00, "None" );
			PORT_DIPSETTING(	0x20, "Score=350" );
			PORT_BIT (0x40, IP_ACTIVE_HIGH, IPT_VBLANK );
			PORT_BITX(0x80, IP_ACTIVE_LOW, IPT_SERVICE | IPF_TOGGLE, "Self Test", KEYCODE_F2, IP_JOY_NONE );
	
		PORT_START(); 		/* fake port, gets mapped to Night Driver ports */
			PORT_BITX(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_PLAYER2, "1st gear", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
			PORT_BITX(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_PLAYER2, "2nd gear", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
			PORT_BITX(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER2, "3rd gear", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
			PORT_BITX(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_PLAYER2, "4th gear", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
	
		PORT_START(); 		/* fake port, gets mapped to Night Driver ports */
			PORT_BIT (0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* Spare */
			PORT_DIPNAME( 0x20, 0x00, "Difficult Bonus" );
			PORT_DIPSETTING(	0x00, "Normal" );
			PORT_DIPSETTING(	0x20, "Difficult" );
			PORT_BIT (0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
			PORT_BIT (0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 		/* fake port, gets mapped to Night Driver ports */
			PORT_BIT (0x01, IP_ACTIVE_LOW, IPT_COIN1 );
			PORT_BIT (0x02, IP_ACTIVE_LOW, IPT_COIN2 );
			PORT_BIT (0x04, IP_ACTIVE_LOW, IPT_START1 );
			PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_BUTTON1, "Gas", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
			PORT_BITX(0x10, IP_ACTIVE_HIGH, IPT_BUTTON2, "Novice Track", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
			PORT_BITX(0x20, IP_ACTIVE_HIGH, IPT_BUTTON3, "Expert Track", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
			PORT_BITX(0x40, IP_ACTIVE_HIGH, IPT_BUTTON4, "Pro Track", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
			PORT_BIT (0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* Alternating signal? */
	
		PORT_START(); 		/* fake port used for steering */
			PORT_ANALOG( 0xff, 0x00, IPT_DIAL, 100, 10, 0, 0 );
	
	INPUT_PORTS_END(); }}; 
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		64, 	/* 64 characters */
		1,		/* 1 bit per pixel */
		new int[] { 0 },		  /* no separation in 1 bpp */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8 /* every char takes 8 consecutive bytes */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout, 0x00, 0x02 ), /* offset into colors, # of colors */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static unsigned char palette[] =
	{
		0x00,0x00,0x00, /* BLACK */
		0xff,0xff,0xff, /* WHITE */
		0x55,0x55,0x55, /* DK GREY - for MAME text only */
		0x80,0x80,0x80, /* LT GREY - for MAME text only */
	};
	static unsigned short colortable[] =
	{
		0x00, 0x01,
		0x01, 0x00,
	};
	static void init_palette(unsigned char *game_palette, unsigned short *game_colortable,const unsigned char *color_prom)
	{
		memcpy(game_palette,palette,sizeof(palette));
		memcpy(game_colortable,colortable,sizeof(colortable));
	}
	
	
	static MachineDriver machine_driver_nitedrv = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6502,
				1000000,	   /* 1 MHz ???? */
				readmem,writemem,null,null,
				interrupt,1
			)
		},
		57, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* single CPU, no need for interleaving */
		null,
	
		/* video hardware */
		32*8, 32*8, { 0*8, 32*8-1, 0*8, 32*8-1 },
		gfxdecodeinfo,
		sizeof(palette) / sizeof(palette[null]) / 3, sizeof(colortable) / sizeof(colortable[null]),
		init_palette,
	
		VIDEO_TYPE_RASTER,
		null,
		generic_vh_start,
		generic_vh_stop,
		nitedrvr_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0
	
	);
	
	
	
	
	
	/***************************************************************************
	
	  Game ROMs
	
	***************************************************************************/
	
	static RomLoadPtr rom_nitedrvr = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "6569-01.d2",   0x9000, 0x0800, 0x7afa7542 );
		ROM_LOAD( "6570-01.f2",   0x9800, 0x0800, 0xbf5d77b1 );
		ROM_RELOAD( 			  0xf800, 0x0800 );
	
		ROM_REGION( 0x0200, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "6568-01.p2",   0x0000, 0x0200, 0xf80d8889 );
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_nitedrvr	   = new GameDriver("1976"	,"nitedrvr"	,"nitedrvr.java"	,rom_nitedrvr,null	,machine_driver_nitedrv	,input_ports_nitedrvr	,null	,ROT0	,	"Atari", "Night Driver", GAME_NO_SOUND )
}
