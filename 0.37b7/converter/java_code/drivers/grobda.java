/***************************************************************************

Grobda (c) Namco 1984

Driver by Manuel Abadia <manu@teleline.es>

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class grobda
{
	
	extern UBytePtr grobda_snd_sharedram;
	extern UBytePtr grobda_customio_1, *grobda_customio_2;
	extern UBytePtr mappy_soundregs;
	
	/* memory functions */
	
	/* custom IO chips functions */
	
	/* INT functions */
	
	/* video functions */
	
	
	public static WriteHandlerPtr grobda_DAC_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		DAC_data_w(0, (data << 4) | data);
	} };
	
	static MemoryReadAddress readmem_cpu1[] =
	{
		new MemoryReadAddress( 0x0000, 0x03ff, videoram_r ),						/* video RAM */
		new MemoryReadAddress( 0x0400, 0x07ff, colorram_r ),						/* color RAM */
		new MemoryReadAddress( 0x0800, 0x1fff, MRA_RAM ),						/* RAM  sprite RAM */
		new MemoryReadAddress( 0x4040, 0x43ff, grobda_snd_sharedram_r ),			/* shared RAM with CPU #2 */
		new MemoryReadAddress( 0x4800, 0x480f, grobda_customio_1_r ),			/* custom I/O chip #1 interface */
		new MemoryReadAddress( 0x4810, 0x481f, grobda_customio_2_r ),			/* custom I/O chip #2 interface */
		new MemoryReadAddress( 0xa000, 0xffff, MRA_ROM ),						/* ROM */
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress writemem_cpu1[] =
	{
		new MemoryWriteAddress( 0x0000, 0x03ff, videoram_w, videoram, videoram_size ),		/* video RAM */
		new MemoryWriteAddress( 0x0400, 0x07ff, colorram_w, colorram ),						/* color RAM */
		new MemoryWriteAddress( 0x0800, 0x1fff, MWA_RAM, spriteram ),						/* RAM  sprite RAM */
		new MemoryWriteAddress( 0x2000, 0x2000, flip_screen_w ),								/* flip screen */
		new MemoryWriteAddress( 0x4040, 0x43ff, grobda_snd_sharedram_w ),						/* shared RAM with CPU #2 */
		new MemoryWriteAddress( 0x4800, 0x480f, grobda_customio_1_w, grobda_customio_1 ),	/* custom I/O chip #1 interface */
		new MemoryWriteAddress( 0x4810, 0x481f, grobda_customio_2_w, grobda_customio_2 ),	/* custom I/O chip #2 interface */
		new MemoryWriteAddress( 0x5002, 0x5003, grobda_interrupt_ctrl_1_w ),					/* Interrupt control */
	//	new MemoryWriteAddress( 0x5008, 0x5009, MWA_NOP ),									/* ??? */
		new MemoryWriteAddress( 0x500a, 0x500b, grobda_cpu2_enable_w ),						/* sound CPU enable? */
		new MemoryWriteAddress( 0x8000, 0x8000, watchdog_reset_w ),	 						/* watchdog reset */
		new MemoryWriteAddress( 0xa000, 0xffff, MWA_ROM ),									/* ROM */
		new MemoryWriteAddress( -1 )
	};
	
	static MemoryReadAddress readmem_cpu2[] =
	{
		new MemoryReadAddress( 0x0000, 0x003f, MRA_RAM ),				/* sound registers */
		new MemoryReadAddress( 0x0040, 0x03ff, MRA_RAM ),				/* shared RAM with CPU #1 */
		new MemoryReadAddress( 0xe000, 0xffff, MRA_ROM ),				/* ROM */
		new MemoryReadAddress( -1 )
	};
	
	
	static MemoryWriteAddress writemem_cpu2[] =
	{
		new MemoryWriteAddress( 0x0002, 0x0002, grobda_DAC_w ),					/* $12, $22 and $32 are DAC locations as well */
		new MemoryWriteAddress( 0x0000, 0x003f, mappy_sound_w, mappy_soundregs ),/* sound registers */
		new MemoryWriteAddress( 0x0040, 0x03ff, MWA_RAM, grobda_snd_sharedram ),	/* shared RAM with the main CPU */
		new MemoryWriteAddress( 0x2000, 0x2001, grobda_interrupt_ctrl_2_w ),		/* Interrupt control */
		new MemoryWriteAddress( 0x2006, 0x2007, mappy_sound_enable_w ),			/* sound enable? */
		new MemoryWriteAddress( 0xe000, 0xffff, MWA_ROM ),						/* ROM */
		new MemoryWriteAddress( -1 )												/* end of table */
	};
	
	/* The dipswitches and player inputs are not memory mapped, they are handled by an I/O chip. */
	static InputPortPtr input_ports_grobda = new InputPortPtr(){ public void handler() { 
	PORT_START();   /* DSW0 */
		PORT_DIPNAME( 0x07, 0x03, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x07, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0x18, 0x00, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x00, "a" );	PORT_DIPSETTING(    0x08, "b" );	PORT_DIPSETTING(    0x10, "c" );	PORT_DIPSETTING(    0x18, "d" );	PORT_DIPNAME( 0xe0, 0x60, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
	
		PORT_START();   /* DSW1 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x01, "1" );	PORT_DIPSETTING(    0x02, "2" );	PORT_DIPSETTING(    0x00, "3" );	PORT_DIPSETTING(    0x03, "5" );	PORT_DIPNAME( 0x04, 0x04, "Select Mode" );	PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x08, IP_ACTIVE_HIGH );	PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0xc0, "10k 50k and every 50k" );	PORT_DIPSETTING(    0x80, "10k 30k" );	PORT_DIPSETTING(    0x00, "10k" );	PORT_DIPSETTING(    0x40, "none" );
		PORT_START();   /* IN0 */
		PORT_BIT_IMPULSE( 0x01, IP_ACTIVE_HIGH, IPT_START1, 1 );	PORT_BIT_IMPULSE( 0x02, IP_ACTIVE_HIGH, IPT_START2, 1 );	PORT_BIT_IMPULSE( 0x10, IP_ACTIVE_HIGH, IPT_COIN1, 1 );	PORT_BIT_IMPULSE( 0x20, IP_ACTIVE_HIGH, IPT_COIN2, 1 );	PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Cocktail") );
	
		PORT_START();   /* IN1 */
		PORT_BIT(   0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY  );	PORT_BIT(   0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY  );	PORT_BIT(   0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY );	PORT_BIT(   0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY );	PORT_BIT(   0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT(   0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT(   0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT(   0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );
		PORT_START();   /* IN2 */
		PORT_BIT_IMPULSE( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON1, 1 );	PORT_BITX(  0x02, IP_ACTIVE_HIGH, IPT_BUTTON1, 0, IP_KEY_PREVIOUS, IP_JOY_PREVIOUS );	PORT_BIT_IMPULSE( 0x04, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL, 1 );	PORT_BITX(  0x08, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL, 0, IP_KEY_PREVIOUS, IP_JOY_PREVIOUS );	PORT_BIT_IMPULSE( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON2, 1 );	PORT_BITX(  0x20, IP_ACTIVE_HIGH, IPT_BUTTON2, 0, IP_KEY_PREVIOUS, IP_JOY_PREVIOUS );	PORT_BIT_IMPULSE( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL, 1 );	PORT_BITX(  0x80, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL, 0, IP_KEY_PREVIOUS, IP_JOY_PREVIOUS );INPUT_PORTS_END(); }}; 
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,            /* 8*8 characters */
		256,            /* 256 characters */
		2,				/* 2 bits per pixel */
		new int[] { 0, 4 },		/* the bitplanes are packed into one byte */
		new int[] { 8*8+0, 8*8+1, 8*8+2, 8*8+3, 0, 1, 2, 3 },   /* bits are packed in groups of four */
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },   /* characters are rotated 90 degrees */
		16*8			/* every char takes 16 bytes */
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,                                         /* 16*16 sprites */
		256,                                           /* 128 sprites */
		2,                                             /* 2 bits per pixel */
		new int[] { 0, 4 },                                      /* the bitplanes are packed into one byte */
		new int[] { 0, 1, 2, 3, 8*8, 8*8+1, 8*8+2, 8*8+3,
			16*8+0, 16*8+1, 16*8+2, 16*8+3, 24*8+0, 24*8+1, 24*8+2, 24*8+3 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
			32*8, 33*8, 34*8, 35*8, 36*8, 37*8, 38*8, 39*8 },
		64*8                                           /* every sprite takes 64 bytes */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,		0, 64 ),
		new GfxDecodeInfo( REGION_GFX2, 0, spritelayout,	64*4, 64 ),
		new GfxDecodeInfo( -1 ) /* end of table */
	};
	
	static namco_interface namco_interface = new namco_interface
	(
		23920,	/* sample rate (approximate value) */
		8,		/* number of voices */
		100,	/* playback volume */
		REGION_SOUND1	/* memory region */
	);
	
	static DACinterface dac_interface = new DACinterface
	(
		1,
		new int[] { 55 }
	);
	
	static MachineDriver machine_driver_grobda = new MachineDriver
	(
		/* basic machine hardware  */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6809,			/* MAIN CPU */
				1500000,			/* 1.5 MHz? */
				readmem_cpu1,writemem_cpu1,null,null,
				grobda_interrupt_1,1
			),
			new MachineCPU(
				CPU_M6809,			/* SOUND CPU */
				1500000,			/* 1.5 MHz? */
				readmem_cpu2,writemem_cpu2,null,null,
				grobda_interrupt_2,1
			),
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		100,	/* a high value to ensure proper synchronization of the CPUs */
		grobda_init_machine,
	
		/* video hardware */
		36*8, 28*8, new rectangle( 0*8, 36*8-1, 0*8, 28*8-1 ),
		gfxdecodeinfo,
		32,
		4*(64+64),
		grobda_vh_convert_color_prom,
		VIDEO_TYPE_RASTER,
		null,
		generic_vh_start,
		generic_vh_stop,
		grobda_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_NAMCO,
				namco_interface
			),
			new MachineSound(
				SOUND_DAC,
				dac_interface
			)
		}
	);
	
	
	
	static RomLoadPtr rom_grobda = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code for the first CPU  */
		ROM_LOAD( "gr2-3",     0xa000, 0x2000, 0x8e3a23be );	ROM_LOAD( "gr2-2",     0xc000, 0x2000, 0x19ffa83d );	ROM_LOAD( "gr2-1",     0xe000, 0x2000, 0x0089b13a );
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for the second CPU */
		ROM_LOAD( "gr1-4.k1",  0xe000, 0x2000, 0x3fe78c08 );
		ROM_REGION( 0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "gr1-7.c3",  0x0000, 0x1000, 0x4ebfabfd );/* characters */
	
		ROM_REGION( 0x4000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "gr1-5.f3",  0x0000, 0x2000, 0xeed43487 );/* sprites */
		ROM_LOAD( "gr1-6.e3",  0x2000, 0x2000, 0xcebb7362 );/* sprites */
	
		ROM_REGION( 0x0220, REGION_PROMS );	ROM_LOAD( "82s123.4c", 0x0000, 0x0020, 0xc65efa77 );/* palette */
		ROM_LOAD( "mb7052.4e", 0x0020, 0x0100, 0xa0f66911 );/* characters */
		ROM_LOAD( "mb7052.3l", 0x0120, 0x0100, 0xf1f2c234 );/* sprites */
	
		ROM_REGION( 0x0100, REGION_SOUND1 );/* sound prom */
		ROM_LOAD( "mb7052.3m", 0x0000, 0x0100, 0x66eb1467 );ROM_END(); }}; 
	
	static RomLoadPtr rom_grobda2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code for the first CPU  */
		ROM_LOAD( "gr1-3.d1",  0xa000, 0x2000, 0x4ef4a7c1 );	ROM_LOAD( "gr2-2.a",   0xc000, 0x2000, 0xf93e82ae );	ROM_LOAD( "gr1-1.b1",  0xe000, 0x2000, 0x32d42f22 );
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for the second CPU */
		ROM_LOAD( "gr1-4.k1",  0xe000, 0x2000, 0x3fe78c08 );
		ROM_REGION( 0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "gr1-7.c3",  0x0000, 0x1000, 0x4ebfabfd );/* characters */
	
		ROM_REGION( 0x4000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "gr1-5.f3",  0x0000, 0x2000, 0xeed43487 );/* sprites */
		ROM_LOAD( "gr1-6.e3",  0x2000, 0x2000, 0xcebb7362 );/* sprites */
	
		ROM_REGION( 0x0220, REGION_PROMS );	ROM_LOAD( "82s123.4c", 0x0000, 0x0020, 0xc65efa77 );/* palette */
		ROM_LOAD( "mb7052.4e", 0x0020, 0x0100, 0xa0f66911 );/* characters */
		ROM_LOAD( "mb7052.3l", 0x0120, 0x0100, 0xf1f2c234 );/* sprites */
	
		ROM_REGION( 0x0100, REGION_SOUND1 );/* sound prom */
		ROM_LOAD( "mb7052.3m", 0x0000, 0x0100, 0x66eb1467 );ROM_END(); }}; 
	
	static RomLoadPtr rom_grobda3 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code for the first CPU  */
		ROM_LOAD( "gr1-3.d1",  0xa000, 0x2000, 0x4ef4a7c1 );	ROM_LOAD( "gr1-2.c1",  0xc000, 0x2000, 0x7dcc6e8e );	ROM_LOAD( "gr1-1.b1",  0xe000, 0x2000, 0x32d42f22 );
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for the second CPU */
		ROM_LOAD( "gr1-4.k1",  0xe000, 0x2000, 0x3fe78c08 );
		ROM_REGION( 0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "gr1-7.c3",  0x0000, 0x1000, 0x4ebfabfd );/* characters */
	
		ROM_REGION( 0x4000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "gr1-5.f3",  0x0000, 0x2000, 0xeed43487 );/* sprites */
		ROM_LOAD( "gr1-6.e3",  0x2000, 0x2000, 0xcebb7362 );/* sprites */
	
		ROM_REGION( 0x0220, REGION_PROMS );	ROM_LOAD( "82s123.4c", 0x0000, 0x0020, 0xc65efa77 );/* palette */
		ROM_LOAD( "mb7052.4e", 0x0020, 0x0100, 0xa0f66911 );/* characters */
		ROM_LOAD( "mb7052.3l", 0x0120, 0x0100, 0xf1f2c234 );/* sprites */
	
		ROM_REGION( 0x0100, REGION_SOUND1 );/* sound prom */
		ROM_LOAD( "mb7052.3m", 0x0000, 0x0100, 0x66eb1467 );ROM_END(); }}; 
	
	
	public static GameDriver driver_grobda	   = new GameDriver("1984"	,"grobda"	,"grobda.java"	,rom_grobda,null	,machine_driver_grobda	,input_ports_grobda	,null	,ROT90	,	"Namco", "Grobda (New version)" )
	public static GameDriver driver_grobda2	   = new GameDriver("1984"	,"grobda2"	,"grobda.java"	,rom_grobda2,driver_grobda	,machine_driver_grobda	,input_ports_grobda	,null	,ROT90	,	"Namco", "Grobda (Old version set 1)" )
	public static GameDriver driver_grobda3	   = new GameDriver("1984"	,"grobda3"	,"grobda.java"	,rom_grobda3,driver_grobda	,machine_driver_grobda	,input_ports_grobda	,null	,ROT90	,	"Namco", "Grobda (Old version set 2)" )
}
