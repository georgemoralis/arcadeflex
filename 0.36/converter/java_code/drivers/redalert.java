/***************************************************************************

Irem Red Alert Driver

Everything in this driver is guesswork and speculation.  If something
seems wrong, it probably is.

If you have any questions about how this driver works, don't hesitate to
ask.  - Mike Balfour (mab22@po.cwru.edu)
***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;

public class redalert
{
	
	/* vidhrdw/redalert.c */
	
	/* sndhrdw/redalert.c */
	public static WriteHandlerPtr redalert_c030_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static ReadHandlerPtr redalert_voicecommand_r = new ReadHandlerPtr() { public int handler(int offset);
	public static WriteHandlerPtr redalert_soundlatch_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static ReadHandlerPtr redalert_AY8910_A_r = new ReadHandlerPtr() { public int handler(int offset);
	public static WriteHandlerPtr redalert_AY8910_B_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static WriteHandlerPtr redalert_AY8910_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static ReadHandlerPtr redalert_sound_register_IC1_r = new ReadHandlerPtr() { public int handler(int offset);
	public static WriteHandlerPtr redalert_sound_register_IC2_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x01ff, MRA_RAM ), /* Zero page / stack */
		new MemoryReadAddress( 0x0200, 0x0fff, MRA_RAM ), /* ? */
		new MemoryReadAddress( 0x1000, 0x1fff, MRA_RAM ), /* Scratchpad video RAM */
		new MemoryReadAddress( 0x2000, 0x4fff, MRA_RAM ), /* Video RAM */
		new MemoryReadAddress( 0x5000, 0xbfff, MRA_ROM ),
		new MemoryReadAddress( 0xc100, 0xc100, input_port_0_r ),
		new MemoryReadAddress( 0xc110, 0xc110, input_port_1_r ),
		new MemoryReadAddress( 0xc120, 0xc120, input_port_2_r ),
		new MemoryReadAddress( 0xc170, 0xc170, input_port_3_r ), /* Vertical Counter? */
		new MemoryReadAddress( 0xf000, 0xffff, MRA_ROM ), /* remapped ROM for 6502 vectors */
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x01ff, MWA_RAM ),
		new MemoryWriteAddress( 0x0200, 0x0fff, MWA_RAM ), /* ? */
		new MemoryWriteAddress( 0x1000, 0x1fff, MWA_RAM ), /* Scratchpad video RAM */
		new MemoryWriteAddress( 0x2000, 0x3fff, redalert_backram_w, redalert_backram ),
		new MemoryWriteAddress( 0x4000, 0x43ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x4400, 0x47ff, redalert_spriteram1_w, redalert_spriteram1 ),
		new MemoryWriteAddress( 0x4800, 0x4bff, redalert_characterram_w, redalert_characterram ),
		new MemoryWriteAddress( 0x4c00, 0x4fff, redalert_spriteram2_w, redalert_spriteram2 ),
		new MemoryWriteAddress( 0x5000, 0xbfff, MWA_ROM ),
		new MemoryWriteAddress( 0xc130, 0xc130, redalert_c030_w ),
	//	new MemoryWriteAddress( 0xc140, 0xc140, redalert_c040_w ), /* Output port? */
		new MemoryWriteAddress( 0xc150, 0xc150, redalert_backcolor_w ),
		new MemoryWriteAddress( 0xc160, 0xc160, redalert_soundlatch_w ),
		new MemoryWriteAddress( 0xf000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x03ff, MRA_RAM ),
		new MemoryReadAddress( 0x7800, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0xf800, 0xffff, MRA_ROM ),
		new MemoryReadAddress( 0x1001, 0x1001, redalert_sound_register_IC1_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x03ff, MWA_RAM ),
		new MemoryWriteAddress( 0x7800, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0xf800, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( 0x1000, 0x1000, redalert_AY8910_w ),
		new MemoryWriteAddress( 0x1001, 0x1001, redalert_sound_register_IC2_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress voice_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0x83ff, MRA_RAM ),
	//	new MemoryReadAddress( 0xc000, 0xc000, redalert_voicecommand_r ), /* reads command from D0-D5? */
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress voice_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x3fff, MWA_ROM ),
		new MemoryWriteAddress( 0x8000, 0x83ff, MWA_RAM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	static InputPortPtr input_ports_redalert = new InputPortPtr(){ public void handler() { 
		PORT_START(); 			   /* DIP Switches */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );
		PORT_DIPSETTING(    0x01, "4" );
		PORT_DIPSETTING(    0x02, "5" );
		PORT_DIPSETTING(    0x03, "6" );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "5000" );
		PORT_DIPSETTING(    0x08, "7000" );
		PORT_DIPNAME( 0x30, 0x10, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x30, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_SERVICE( 0x80, IP_ACTIVE_HIGH );
	
		PORT_START(); 			   /* IN1 */
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT );
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT );
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* Meter */
	
		PORT_START(); 			   /* IN2  */
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* Meter */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_PLAYER2 );
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* Meter */
	
		PORT_START(); 			   /* IN3 - some type of video counter? */
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START(); 			   /* Fake input for coins */
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_SERVICE );
	INPUT_PORTS_END(); }}; 
	
	
	static GfxLayout backlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		0x400,	  /* 1024 characters */
		1,	/* 1 bits per pixel */
		new int[] { 0 }, /* No info needed for bit offsets */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8 /* every char takes 8 consecutive bytes */
	);
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		128,	/* 128 characters */
		1,	/* 1 bits per pixel */
		new int[] { 0 }, /* No info needed for bit offsets */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8 /* every char takes 8 consecutive bytes */
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		128,	/* 128 characters */
		2,		/* 1 bits per pixel */
		new int[] { 0, 0x800*8 }, /* No info needed for bit offsets */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8 /* every char takes 8 consecutive bytes */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( 0, 0x3000, backlayout,	0, 8 ), 	/* the game dynamically modifies this */
		new GfxDecodeInfo( 0, 0x4800, charlayout,	0, 8 ), 	/* the game dynamically modifies this */
		new GfxDecodeInfo( 0, 0x4400, spritelayout,16, 4 ), 	/* the game dynamically modifies this */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	/* Arbitrary colors */
	static unsigned char palette[] =
	{
		0x40,0x80,0xff,	/* Background */
		0x00,0x00,0xff,	/* Blue */
		0xff,0x00,0xff,	/* Magenta */
		0x00,0xff,0xff,	/* Cyan */
		0xff,0x00,0x00,	/* Red */
		0xff,0x80,0x00,	/* Orange */
		0xff,0xff,0x00,	/* Yellow */
		0xff,0xff,0xff,	/* White */
		0x00,0x00,0x00,	/* Black */
	};
	
	/* Arbitrary colortable */
	static unsigned short colortable[] =
	{
		0,7,
		0,6,
		0,2,
		0,4,
		0,3,
		0,6,
		0,1,
		0,8,
	
		0,8,8,8,
		0,6,4,7,
		0,6,4,1,
		0,8,5,1,
	};
	
	static void init_palette(unsigned char *game_palette, unsigned short *game_colortable,const unsigned char *color_prom)
	{
		memcpy(game_palette,palette,sizeof(palette));
		memcpy(game_colortable,colortable,sizeof(colortable));
	}
	
	
	
	public static InterruptPtr redalert_interrupt = new InterruptPtr() { public int handler() 
	{
		static int lastcoin = 0;
		int newcoin;
	
		newcoin = input_port_4_r(0);
	
		if (newcoin)
		{
			if ((newcoin & 0x01) && !(lastcoin & 0x01))
			{
				lastcoin = newcoin;
				return nmi_interrupt();
			}
			if ((newcoin & 0x02) && !(lastcoin & 0x02))
			{
				lastcoin = newcoin;
				return nmi_interrupt();
			}
		}
	
		lastcoin = newcoin;
		return interrupt();
	} };
	
	static struct AY8910interface ay8910_interface =
	{
		1,			/* 1 chip */
		2000000,	/* 2 MHz */
		{ 50 },		/* Volume */
		{ redalert_AY8910_A_r },		/* Port A Read */
		{ 0 },		/* Port B Read */
		{ 0 },		/* Port A Write */
		{ redalert_AY8910_B_w }		/* Port B Write */
	};
	
	
	
	static MachineDriver machine_driver_redalert = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6502,
				1000000,	   /* ???? */
				readmem,writemem,null,null,
				redalert_interrupt,1
			),
			new MachineCPU(
				CPU_M6502 | CPU_AUDIO_CPU,
				1000000,	   /* 1 MHz */
				sound_readmem,sound_writemem,null,null,
				/* IRQ is hooked to a 555 timer, whose freq is 1150 Hz */
				0,null,
				interrupt,1150
			),
			new MachineCPU(
				CPU_8085A | CPU_AUDIO_CPU,
				1000000,	   /* 1 MHz? */
				voice_readmem,voice_writemem,null,null,
				ignore_interrupt,1
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,      /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 1*8, 31*8-1 ),
		gfxdecodeinfo,
		sizeof(palette) / sizeof(palette[0]) / 3, sizeof(colortable) / sizeof(colortable[null]),
		init_palette,
	
		VIDEO_TYPE_RASTER,
		null,
		generic_vh_start,
		generic_vh_stop,
		redalert_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_AY8910,
				ay8910_interface
			)
		}
	
	);
	
	
	/***************************************************************************
	
	  Game ROMs
	
	***************************************************************************/
	
	static RomLoadPtr rom_redalert = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "rag5",         	0x5000, 0x1000, 0xd7c9cdd6 );
		ROM_LOAD( "rag6",         	0x6000, 0x1000, 0xcb2a308c );
		ROM_LOAD( "rag7n",        	0x7000, 0x1000, 0x82ab2dae );
		ROM_LOAD( "rag8n",        	0x8000, 0x1000, 0xb80eece9 );
		ROM_RELOAD(                 0xf000, 0x1000 );
		ROM_LOAD( "rag9",         	0x9000, 0x1000, 0x2b7d1295 );
		ROM_LOAD( "ragab",        	0xa000, 0x1000, 0xab99f5ed );
		ROM_LOAD( "ragb",         	0xb000, 0x1000, 0x8e0d1661 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for code */
		ROM_LOAD( "w3s1",         	0x7800, 0x0800, 0x4af956a5 );
		ROM_RELOAD(                0xf800, 0x0800 );
	
		ROM_REGION( 0x10000, REGION_CPU3 );/* 64k for code */
		ROM_LOAD( "ras1b",        	0x0000, 0x1000, 0xec690845 );
		ROM_LOAD( "ras2",         	0x1000, 0x1000, 0xfae94cfc );
		ROM_LOAD( "ras3",         	0x2000, 0x1000, 0x20d56f3e );
		ROM_LOAD( "ras4",         	0x3000, 0x1000, 0x130e66db );
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_redalert	   = new GameDriver("1981"	,"redalert"	,"redalert.java"	,rom_redalert,null	,machine_driver_redalert	,input_ports_redalert	,null	,ROT270	,	"GDI + Irem", "Red Alert", GAME_WRONG_COLORS | GAME_IMPERFECT_SOUND)
}
