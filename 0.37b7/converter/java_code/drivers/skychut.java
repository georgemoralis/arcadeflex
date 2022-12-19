/***************************************************************************
  ? (M10 m10 hardware)
  Sky Chuter By IREM
  Space Beam (M15 m15 hardware)
  Green Beret (?M15 ?m15 hardware)

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

  (c) 12/2/1998 Lee Taylor

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class skychut
{
	
	extern UINT8* iremm15_chargen;
	
	
	static UINT8 *memory;
	
	static unsigned char palette[] = /* V.V */ /* Smoothed pure colors, overlays are not so contrasted */
	{
		0x00,0x00,0x00, /* BLACK */
		0xff,0x20,0x20, /* RED */
		0x20,0xff,0x20, /* GREEN */
		0xff,0xff,0x20, /* YELLOW */
		0x20,0xff,0xff, /* CYAN */
		0xff,0x20,0xff,  /* PURPLE */
		0xff,0xff,0xff /* WHITE */
	};
	
	static unsigned char spacebeam_palette[] = /* total estimation */
	{
		0xff,0xff,0xff, /* WHITE */
		0xff,0x20,0x20, /* RED */
		0x20,0xff,0x20, /* GREEN */
		0xff,0xff,0x20, /* YELLOW */
		0x20,0xff,0xff, /* CYAN */
		0xff,0x20,0xff,  /* PURPLE */
		0x00,0x00,0xf0, /* blue */
		0x00,0x00,0x00 /* BLACK */
	};
	
	static unsigned short colortable[] =
	{
		0,1,0,2,0,3,0,4,0,5,0,6
	};
	
	static void init_palette(UBytePtr game_palette, unsigned short *game_colortable,const UBytePtr color_prom)
	{
		memcpy(game_palette,palette,sizeof(palette));
		memcpy(game_colortable,colortable,sizeof(colortable));
	}
	
	static void init_greenber_palette(UBytePtr game_palette, unsigned short *game_colortable,const UBytePtr color_prom)
	{
		memcpy(game_palette,spacebeam_palette,sizeof(spacebeam_palette));
	}
	
	static MemoryReadAddress skychut_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x02ff, MRA_RAM ), /* scratch ram */
		new MemoryReadAddress( 0x1000, 0x2fff, MRA_ROM ),
		new MemoryReadAddress( 0x4000, 0x4400, MRA_RAM ),
		new MemoryReadAddress( 0x4800, 0x4bff, MRA_RAM ), /* Foreground colour  */
		new MemoryReadAddress( 0x5000, 0x53ff, MRA_RAM ), /* BKgrnd colour ??? */
		new MemoryReadAddress( 0xa200, 0xa200, input_port_1_r ),
		new MemoryReadAddress( 0xa300, 0xa300, input_port_0_r ),
	/*	new MemoryReadAddress( 0xa700, 0xa700, input_port_2_r ), */
		new MemoryReadAddress( 0xfC00, 0xffff, MRA_ROM ),	/* for the reset / interrupt vectors */
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	
	static MemoryWriteAddress skychut_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x02ff, MWA_RAM, memory ),
		new MemoryWriteAddress( 0x1000, 0x2fff, MWA_ROM ),
		new MemoryWriteAddress( 0x4000, 0x4400, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x4800, 0x4bff, skychut_colorram_w,colorram ), /* foreground colour  */
		new MemoryWriteAddress( 0x5000, 0x53ff, MWA_RAM, iremm15_chargen ), /* background ????? */
		new MemoryWriteAddress( 0xa100, 0xa1ff, MWA_RAM ), /* Sound writes????? */
		new MemoryWriteAddress( 0Xa400, 0xa400, skychut_vh_flipscreen_w ),
		new MemoryWriteAddress( 0xfc00, 0xffff, MWA_ROM ),	/* for the reset / interrupt vectors */
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress greenberet_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x02ff, MRA_RAM ), /* scratch ram */
		new MemoryReadAddress( 0x1000, 0x33ff, MRA_ROM ),
		new MemoryReadAddress( 0x4000, 0x4400, MRA_RAM ),
		new MemoryReadAddress( 0x4800, 0x4bff, MRA_RAM ), /* Foreground colour  */
		new MemoryReadAddress( 0x5000, 0x57ff, MRA_RAM ),
		new MemoryReadAddress( 0xa000, 0xa000, input_port_3_r ),
		new MemoryReadAddress( 0xa200, 0xa200, input_port_1_r ),
		new MemoryReadAddress( 0xa300, 0xa300, input_port_0_r ),
		new MemoryReadAddress( 0xfC00, 0xffff, MRA_ROM ),	/* for the reset / interrupt vectors */
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress greenberet_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x02ff, MWA_RAM, memory ),
		new MemoryWriteAddress( 0x1000, 0x33ff, MWA_ROM ),
		new MemoryWriteAddress( 0x4000, 0x4400, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x4800, 0x4bff, skychut_colorram_w,colorram ), /* foreground colour  */
		new MemoryWriteAddress( 0x5000, 0x57ff, MWA_RAM, iremm15_chargen ),
		new MemoryWriteAddress( 0xa100, 0xa1ff, MWA_RAM ), /* Sound writes????? */
		new MemoryWriteAddress( 0Xa400, 0xa400, skychut_vh_flipscreen_w ),
		new MemoryWriteAddress( 0xfc00, 0xffff, MWA_ROM ),	/* for the reset / interrupt vectors */
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	public static InterruptPtr skychut_interrupt = new InterruptPtr() { public int handler() 
	{
		if (readinputport(2) & 1)	/* Left Coin */
	            return nmi_interrupt();
	        else
	            return interrupt();
	} };
	
	static InputPortPtr input_ports_skychut = new InputPortPtr(){ public void handler() { 
	PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_START1 );	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2);	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_BUTTON1 );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT  );	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT );	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_COCKTAIL );	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_COCKTAIL );	PORT_START(); 	/* IN1 */
		PORT_DIPNAME(0x03, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING (   0x00, "3" );	PORT_DIPSETTING (   0x01, "4" );	PORT_DIPSETTING (   0x02, "5" );	PORT_START(); 	/* FAKE */
		PORT_BIT_IMPULSE( 0x01, IP_ACTIVE_HIGH, IPT_COIN1, 1 );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_spacebeam = new InputPortPtr(){ public void handler() { 
	PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_START1 );	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2);	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_BUTTON1 );	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT );	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT );	PORT_START(); 	/* IN1 */
		PORT_DIPNAME(0x03, 0x01, DEF_STR( "Lives") );
		PORT_DIPSETTING (   0x00, "2" );	PORT_DIPSETTING (   0x01, "3" );	PORT_DIPSETTING (   0x02, "4" );	PORT_DIPSETTING (   0x03, "5" );	PORT_DIPNAME(0x08, 0x00, "?" );	PORT_DIPSETTING (   0x00, DEF_STR(Off);
		PORT_DIPSETTING (   0x08, DEF_STR(On);
		PORT_DIPNAME(0x30, 0x10, DEF_STR(Coinage);
		PORT_DIPSETTING (   0x00, "Testmode" );	PORT_DIPSETTING (   0x10, "1 Coin 1 Play" );	PORT_DIPSETTING (   0x20, "1 Coin 2 Plays" );	PORT_START(); 	/* FAKE */
		PORT_BIT_IMPULSE( 0x01, IP_ACTIVE_HIGH, IPT_COIN1, 1 );	PORT_START(); 	/* IN3 */
		PORT_BIT( 0x03, 0, IPT_UNUSED );	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_BUTTON1|IPF_COCKTAIL );	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT |IPF_COCKTAIL );	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT|IPF_COCKTAIL );INPUT_PORTS_END(); }}; 
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		256,	/* 256 characters */
		1,	/* 1 bits per pixel */
		new int[] { 0 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		new int[] { 7, 6, 5, 4, 3, 2, 1, 0 },
		8*8	/* every char takes 8 consecutive bytes */
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0x0000, charlayout,   0, 7 ),	/* 4 color codes to support midframe */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	static MachineDriver machine_driver_skychut = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6502,
				20000000/8,
				skychut_readmem,skychut_writemem,null,null,
				skychut_interrupt,1
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,       /* frames per second, vblank duration */
		1,      /* single CPU, no need for interleaving */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		sizeof(palette) / sizeof(palette[null]) / 3, sizeof(colortable) / sizeof(colortable[null]),
		init_palette,
	
		VIDEO_TYPE_RASTER|VIDEO_SUPPORTS_DIRTY,
		null,
		generic_vh_start,
		generic_vh_stop,
	    skychut_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
	);
	
	static MachineDriver machine_driver_greenberet = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6502,
				20000000/8,
				greenberet_readmem,greenberet_writemem,null,null,
				skychut_interrupt,1
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,       /* frames per second, vblank duration */
		1,      /* single CPU, no need for interleaving */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		null,
		sizeof(spacebeam_palette) / sizeof(spacebeam_palette[null]) / 3,
		null,
		init_greenber_palette,
	
		VIDEO_TYPE_RASTER|VIDEO_SUPPORTS_DIRTY,
		null,
		generic_vh_start,
		generic_vh_stop,
	    iremm15_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
	);
	
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_iremm10 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION(0x10000,REGION_CPU1);	ROM_LOAD( "b1r",  0x1000, 0x0400, 0xf9a7eb9b );// code, ok
		ROM_LOAD( "b2r",  0x1400, 0x0400, 0xaf11c1aa );// code ok
		ROM_LOAD( "b3r",  0x1800, 0x0400, 0xed49e481 );// code, ok
		ROM_LOAD( "b4r",  0x1c00, 0x0400, 0x6d5db95b );// code, vectors ok,
		ROM_RELOAD(       0xfc00, 0x0400 );/* for the reset and interrupt vectors */
		ROM_LOAD( "b5r",  0x2000, 0x0400, 0xeabba7aa );// graphic
		ROM_LOAD( "b6r",  0x2400, 0x0400, BADCRC( 0x9b7d6e77 )); // ?? bad dump
		ROM_LOAD( "b7r",  0x2800, 0x0400, 0x32045580 );// code, graphic ok,
	
		ROM_REGION( 0x0800, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "b9r",  0x0000, 0x0400, 0x56942cab );// ok
		ROM_LOAD( "b10r", 0x0400, 0x0400, 0xbe4b8585 );// ok
	ROM_END(); }}; 
	
	static RomLoadPtr rom_skychut = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "sc1d",  0x1000, 0x0400, 0x30b5ded1 );	ROM_LOAD( "sc2d",  0x1400, 0x0400, 0xfd1f4b9e );	ROM_LOAD( "sc3d",  0x1800, 0x0400, 0x67ed201e );	ROM_LOAD( "sc4d",  0x1c00, 0x0400, 0x9b23a679 );	ROM_RELOAD(        0xfc00, 0x0400 );/* for the reset and interrupt vectors */
		ROM_LOAD( "sc5a",  0x2000, 0x0400, 0x51d975e6 );	ROM_LOAD( "sc6e",  0x2400, 0x0400, 0x617f302f );	ROM_LOAD( "sc7",   0x2800, 0x0400, 0xdd4c8e1a );	ROM_LOAD( "sc8d",  0x2c00, 0x0400, 0xaca8b798 );
		ROM_REGION( 0x0800, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "sc9d",  0x0000, 0x0400, 0x2101029e );	ROM_LOAD( "sc10d", 0x0400, 0x0400, 0x2f81c70c );ROM_END(); }}; 
	
	static RomLoadPtr rom_spacbeam = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION(0x10000,REGION_CPU1);	ROM_LOAD( "m1b", 0x1000, 0x0400, 0x5a1c3e0b );	ROM_LOAD( "m2b", 0x1400, 0x0400, 0xa02bd9d7 );	ROM_LOAD( "m3b", 0x1800, 0x0400, 0x78040843 );	ROM_LOAD( "m4b", 0x1c00, 0x0400, 0x74705a44 );	ROM_RELOAD(      0xfc00, 0x0400 );/* for the reset and interrupt vectors */
		ROM_LOAD( "m5b", 0x2000, 0x0400, 0xafdf1242 );	ROM_LOAD( "m6b", 0x2400, 0x0400, 0x12afb0c2 );ROM_END(); }}; 
	
	static RomLoadPtr rom_greenber = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION(0x10000,REGION_CPU1);	ROM_LOAD( "gb1", 0x1000, 0x0400, 0x018ff672 );// ok
		ROM_LOAD( "gb2", 0x1400, 0x0400, 0xea8f2267 );// ok
		ROM_LOAD( "gb3", 0x1800, 0x0400, 0x8f337920 );// ok
		ROM_LOAD( "gb4", 0x1c00, 0x0400, 0x7eeac4eb );// ok
		ROM_RELOAD(      0xfc00, 0x0400 );/* for the reset and interrupt vectors */
		ROM_LOAD( "gb5", 0x2000, 0x0400, 0xb2f8e69a );	ROM_LOAD( "gb6", 0x2400, 0x0400, 0x50ea8bd3 );	ROM_LOAD( "gb7", 0x2800, 0x0400, 0x00000000 );// 2be8 entry
		ROM_LOAD( "gb8", 0x2c00, 0x0400, 0x34700b31 );	ROM_LOAD( "gb9", 0x3000, 0x0400, 0xc27b9ba3 );// ok ?
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_iremm10	   = new GameDriver("19??"	,"iremm10"	,"skychut.java"	,rom_iremm10,null	,machine_driver_skychut	,input_ports_skychut	,null	,ROT0	,	"Irem", "? (irem m10 hardware)", GAME_NO_SOUND | GAME_WRONG_COLORS | GAME_NOT_WORKING )
	public static GameDriver driver_skychut	   = new GameDriver("1980"	,"skychut"	,"skychut.java"	,rom_skychut,null	,machine_driver_skychut	,input_ports_skychut	,null	,ROT0	,	"Irem", "Sky Chuter", GAME_NO_SOUND | GAME_WRONG_COLORS )
	public static GameDriver driver_spacbeam	   = new GameDriver("19??"	,"spacbeam"	,"skychut.java"	,rom_spacbeam,null	,machine_driver_greenberet	,input_ports_spacebeam	,null	,ROT0	,	"Irem", "Space Beam", GAME_NO_SOUND | GAME_WRONG_COLORS )
	public static GameDriver driver_greenber	   = new GameDriver("1980"	,"greenber"	,"skychut.java"	,rom_greenber,null	,machine_driver_greenberet	,input_ports_spacebeam	,null	,ROT0	,	"Irem", "Green Beret (IREM)", GAME_NO_SOUND | GAME_WRONG_COLORS | GAME_NOT_WORKING )
}
