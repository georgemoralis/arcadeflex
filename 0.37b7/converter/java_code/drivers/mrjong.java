/***************************************************************************

Mr.Jong
(c)1983 Kiwako (This game is distributed by Sanritsu.)

Crazy Blocks
(c)1983 Kiwako/ECI

Driver by Takahiro Nogi (nogi@kt.rim.or.jp) 2000/03/20 -

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class mrjong
{
	
	
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0x87ff, MRA_RAM ),
		new MemoryReadAddress( 0xa000, 0xa7ff, MRA_RAM ),
		new MemoryReadAddress( 0xe000, 0xe3ff, videoram_r ),
		new MemoryReadAddress( 0xe400, 0xe7ff, colorram_r ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0x8000, 0x87ff, MWA_RAM ),
		new MemoryWriteAddress( 0xa000, 0xa7ff, MWA_RAM ),
		new MemoryWriteAddress( 0xe000, 0xe3ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0xe400, 0xe7ff, colorram_w, colorram ),
		new MemoryWriteAddress( 0xe000, 0xe03f, MWA_RAM, spriteram, spriteram_size),	/* here to initialize the pointer */
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	public static WriteHandlerPtr io_0x00_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		mrjong_flipscreen_w(0, ((data & 0x04) > 2));
	} };
	
	public static ReadHandlerPtr io_0x03_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return 0x00;
	} };
	
	static IOReadPort readport[] =
	{
		new IOReadPort( 0x00, 0x00, input_port_0_r ),		// Input 1
		new IOReadPort( 0x01, 0x01, input_port_1_r ),		// Input 2
		new IOReadPort( 0x02, 0x02, input_port_2_r ),		// DipSw 1
		new IOReadPort( 0x03, 0x03, io_0x03_r ),		// Unknown
		new IOReadPort( -1 )	/* end of table */
	};
	
	static IOWritePort writeport[] =
	{
		new IOWritePort( 0x00, 0x00, io_0x00_w ),
		new IOWritePort( 0x01, 0x01, SN76496_0_w ),
		new IOWritePort( 0x02, 0x02, SN76496_1_w ),
		new IOWritePort( -1 )	/* end of table */
	};
	
	
	static InputPortPtr input_ports_mrjong = new InputPortPtr(){ public void handler() { 
	PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_4WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_4WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_4WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_START1 );	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_START2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );	// ????
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_4WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_4WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_4WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_COIN1 );	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_COIN2 );	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "30k");	PORT_DIPSETTING(    0x04, "50k");	PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x00, "Normal");	PORT_DIPSETTING(    0x08, "Hard");	PORT_DIPNAME( 0x30, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3");	PORT_DIPSETTING(    0x10, "4");	PORT_DIPSETTING(    0x20, "5");	PORT_DIPSETTING(    0x30, "6");	PORT_DIPNAME( 0xc0, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_3C") );
	INPUT_PORTS_END(); }}; 
	
	
	static GfxLayout tilelayout = new GfxLayout
	(
		8, 8,				/* 8*8 characters */
		512,				/* 512 characters */
		2,				/* 2 bits per pixel */
		new int[] { 0, 512*8*8 },			/* the two bitplanes are separated */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },	/* pretty straightforward layout */
		new int[] { 7*8, 6*8, 5*8, 4*8, 3*8, 2*8, 1*8, 0*8 },
		8*8				/* every char takes 8 consecutive bytes */
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16, 16,				/* 16*16 sprites */
		128,				/* 128 sprites */
		2,				/* 2 bits per pixel */
		new int[] { 0, 128*16*16 },		/* the bitplanes are separated */
		new int[] { 8*8+0, 8*8+1, 8*8+2, 8*8+3, 8*8+4, 8*8+5, 8*8+6, 8*8+7,	/* pretty straightforward layout */
				0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 23*8, 22*8, 21*8, 20*8, 19*8, 18*8, 17*8, 16*8,
				7*8, 6*8, 5*8, 4*8, 3*8, 2*8, 1*8, 0*8 },
		32*8				/* every sprite takes 32 consecutive bytes */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0x0000, tilelayout,      0, 32 ),
		new GfxDecodeInfo( REGION_GFX1, 0x0000, spritelayout,    0, 32 ),
		new GfxDecodeInfo( -1 )		/* end of array */
	};
	
	
	static SN76496interface sn76496_interface = new SN76496interface
	(
		2,				/* 2 chips (SN76489) */
		new int[] { 15468000/6, 15468000/6 },	/* 2.578 MHz */
		new int[] { 100, 100 }
	);
	
	
	static MachineDriver machine_driver_mrjong = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				15468000/6,	/* 2.578 MHz?? */
				readmem, writemem, readport, writeport,
				nmi_interrupt, 1
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,					/* single CPU, no need for interleaving */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 30*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		16, 4*32,
		mrjong_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		generic_vh_start,
		generic_vh_stop,
		mrjong_vh_screenrefresh,
	
		/* sound hardware */
		0, 0, 0, 0,
		new MachineSound[] {
			new MachineSound(
				SOUND_SN76496,			/* SN76489 x2 */
				sn76496_interface
			)
		}
	);
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_mrjong = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* code */
		ROM_LOAD( "mj00", 0x0000, 0x2000, 0xd211aed3 );	ROM_LOAD( "mj01", 0x2000, 0x2000, 0x49a9ca7e );	ROM_LOAD( "mj02", 0x4000, 0x2000, 0x4b50ae6a );	ROM_LOAD( "mj03", 0x6000, 0x2000, 0x2c375a17 );
		ROM_REGION( 0x2000, REGION_GFX1 );/* gfx */
		ROM_LOAD( "mj21", 0x0000, 0x1000, 0x1ea99dab );	ROM_LOAD( "mj20", 0x1000, 0x1000, 0x7eb1d381 );
		ROM_REGION( 0x0120, REGION_PROMS );/* color */
		ROM_LOAD( "mj61", 0x0000, 0x0020, 0xa85e9b27 );	ROM_LOAD( "mj60", 0x0020, 0x0100, 0xdd2b304f );ROM_END(); }}; 
	
	static RomLoadPtr rom_crazyblk = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* code */
		ROM_LOAD( "c1.a6", 0x0000, 0x2000, 0xe2a211a2 );	ROM_LOAD( "c2.a7", 0x2000, 0x2000, 0x75070978 );	ROM_LOAD( "c3.a7", 0x4000, 0x2000, 0x696ca502 );	ROM_LOAD( "c4.a8", 0x6000, 0x2000, 0xc7f5a247 );
		ROM_REGION( 0x2000, REGION_GFX1 );/* gfx */
		ROM_LOAD( "c6.h5", 0x0000, 0x1000, 0x2b2af794 );	ROM_LOAD( "c5.h4", 0x1000, 0x1000, 0x98d13915 );
		ROM_REGION( 0x0120, REGION_PROMS );/* color */
		ROM_LOAD( "clr.j7", 0x0000, 0x0020, 0xee1cf1d5 );	ROM_LOAD( "clr.g5", 0x0020, 0x0100, 0xbcb1e2e3 );ROM_END(); }}; 
	
	
	public static GameDriver driver_mrjong	   = new GameDriver("1983"	,"mrjong"	,"mrjong.java"	,rom_mrjong,null	,machine_driver_mrjong	,input_ports_mrjong	,null	,ROT90	,	"Kiwako", "Mr. Jong (Japan)" )
	public static GameDriver driver_crazyblk	   = new GameDriver("1983"	,"crazyblk"	,"mrjong.java"	,rom_crazyblk,driver_mrjong	,machine_driver_mrjong	,input_ports_mrjong	,null	,ROT90	,	"Kiwako (ECI license)", "Crazy Blocks" )
}
