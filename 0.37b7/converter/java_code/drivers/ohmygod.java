/***************************************************************************

Oh My God! (c) 1993 Atlus

driver by Nicola Salmoria

Notes:
- not sure about the scroll registers
- lots of unknown RAM, maybe other gfx planes not used by this game

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class ohmygod
{
	
	
	extern UBytePtr ohmygod_videoram;
	
	
	
	
	int sndbank;
	
	static public static InitMachinePtr ohmygod_init_machine = new InitMachinePtr() { public void handler() 
	{
		UBytePtr rom = memory_region(REGION_SOUND1);
	
		/* the game requires the watchdog to fire during boot, so we have
		   to initialize it */
		watchdog_reset_r(0);
	
		sndbank = 0;
		memcpy(rom + 0x20000,rom + 0x40000 + 0x20000 * sndbank,0x20000);
	} };
	
	public static WriteHandlerPtr ohmygod_ctrl_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		UBytePtr rom = memory_region(REGION_SOUND1);
	
	
		coin_counter_w.handler(0,data & 0x1000);
		coin_counter_w.handler(1,data & 0x2000);
	
		/* ADPCM bank switch */
		if (sndbank != ((data & 0xf0) >> 4))
		{
			sndbank = ((data & 0xf0) >> 4);
			memcpy(rom + 0x20000,rom + 0x40000 + 0x20000 * sndbank,0x20000);
		}
	} };
	
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x300000, 0x303fff, MRA_BANK1 ),
		new MemoryReadAddress( 0x304000, 0x307fff, ohmygod_videoram_r ),
		new MemoryReadAddress( 0x308000, 0x30ffff, MRA_BANK2 ),
		new MemoryReadAddress( 0x700000, 0x701fff, MRA_BANK3 ),
		new MemoryReadAddress( 0x702000, 0x703fff, MRA_BANK4 ),
		new MemoryReadAddress( 0x704000, 0x707fff, MRA_BANK5 ),
		new MemoryReadAddress( 0x708000, 0x70bfff, MRA_BANK6 ),
		new MemoryReadAddress( 0x800000, 0x800001, input_port_0_r ),
		new MemoryReadAddress( 0x800002, 0x800003, input_port_1_r ),
		new MemoryReadAddress( 0xa00000, 0xa00001, input_port_2_r ),
		new MemoryReadAddress( 0xa00002, 0xa00003, input_port_3_r ),
		new MemoryReadAddress( 0xb00000, 0xb00001, OKIM6295_status_0_r ),
		new MemoryReadAddress( 0xc00000, 0xc00001, watchdog_reset_r ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x300000, 0x303fff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x304000, 0x307fff, ohmygod_videoram_w, ohmygod_videoram ),
		new MemoryWriteAddress( 0x308000, 0x30ffff, MWA_BANK2 ),
		new MemoryWriteAddress( 0x400000, 0x400003, ohmygod_scroll_w ),
		new MemoryWriteAddress( 0x600000, 0x6007ff, paletteram_xGGGGGRRRRRBBBBB_word_w, paletteram ),
		new MemoryWriteAddress( 0x700000, 0x701fff, MWA_BANK3, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x702000, 0x703fff, MWA_BANK4, spriteram_2 ),
		new MemoryWriteAddress( 0x704000, 0x707fff, MWA_BANK5 ),
		new MemoryWriteAddress( 0x708000, 0x70bfff, MWA_BANK6 ),	/* work RAM */
		new MemoryWriteAddress( 0x900000, 0x900001, ohmygod_ctrl_w ),
		new MemoryWriteAddress( 0xb00000, 0xb00001, OKIM6295_data_0_w ),
		new MemoryWriteAddress( 0xd00000, 0xd00001, ohmygod_spritebank_w ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	
	static InputPortPtr input_ports_ohmygod = new InputPortPtr(){ public void handler() { 
	PORT_START(); 
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER1 );	PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER1 );	PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER1 );	PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 );	PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BITX(0x0200, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER2 );	PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 );	PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 );	PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );	PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );	PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 
		PORT_BIT( 0x00ff, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_DIPNAME( 0x0f00, 0x0f00, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(      0x0700, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(      0x0800, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(      0x0500, "6 Coins/3 Credits" );	PORT_DIPSETTING(      0x0900, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x0400, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(      0x0f00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x0300, "5 Coins/6 Credits" );	PORT_DIPSETTING(      0x0200, DEF_STR( "4C_5C") );
	//	PORT_DIPSETTING(      0x0600, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(      0x0100, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(      0x0e00, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(      0x0d00, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(      0x0c00, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(      0x0b00, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(      0x0a00, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0xf000, 0xf000, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(      0x7000, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(      0x8000, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(      0x5000, "6 Coins/3 Credits" );	PORT_DIPSETTING(      0x9000, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x4000, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(      0xf000, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x3000, "5 Coins/6 Credits" );	PORT_DIPSETTING(      0x2000, DEF_STR( "4C_5C") );
	//	PORT_DIPSETTING(      0x6000, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(      0x1000, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(      0xe000, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(      0xd000, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(      0xc000, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(      0xb000, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(      0xa000, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Free_Play") );
	
		PORT_START(); 
		PORT_BIT( 0x00ff, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_DIPNAME( 0x0300, 0x0300, "1P Difficulty" );	PORT_DIPSETTING(      0x0200, "Easy" );	PORT_DIPSETTING(      0x0300, "Normal" );	PORT_DIPSETTING(      0x0100, "Hard" );	PORT_DIPSETTING(      0x0000, "Very Hard" );	PORT_DIPNAME( 0x0c00, 0x0c00, "VS Difficulty" );	PORT_DIPSETTING(      0x0c00, "Normal Jake" );	PORT_DIPSETTING(      0x0800, "Hard Jake" );	PORT_DIPSETTING(      0x0400, "Normal" );	PORT_DIPSETTING(      0x0000, "Hard" );	PORT_DIPNAME( 0x1000, 0x1000, "Vs Matches/Credit" );	PORT_DIPSETTING(      0x0000, "1" );	PORT_DIPSETTING(      0x1000, "3" );	PORT_DIPNAME( 0x2000, 0x2000, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x2000, DEF_STR( "On") );
		PORT_DIPNAME( 0x4000, 0x4000, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x4000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x8000, 0x8000, "Test Mode" );	PORT_DIPSETTING(      0x8000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,1),
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] { 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,1),
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] { 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4,
				8*4, 9*4, 10*4, 11*4, 12*4, 13*4, 14*4, 15*4, },
		new int[] { 0*64, 1*64, 2*64, 3*64, 4*64, 5*64, 6*64, 7*64,
				8*64, 9*64, 10*64, 11*64, 12*64, 13*64, 14*64, 15*64 },
		128*8
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,     0, 16 ),	/* colors   0-255 */
		new GfxDecodeInfo( REGION_GFX2, 0, spritelayout, 512, 16 ),	/* colors 512-767 */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static OKIM6295interface okim6295_interface = new OKIM6295interface
	(
		1,          /* 1 chip */
		new int[] { 16000 },	/* 16 kHz ??? */
		new int[] { REGION_SOUND1 },	/* memory region */
		new int[] { 100 }
	);
	
	
	
	static MachineDriver machine_driver_ohmygod = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				12000000,	/* ??? */
				readmem,writemem,null,null,
				m68_level1_irq,1
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,
		ohmygod_init_machine,
	
		/* video hardware */
		64*8, 32*8, new rectangle( 12*8, (64-12)*8-1, 0*8, 30*8-1 ),
		gfxdecodeinfo,
		1024, 1024,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		ohmygod_vh_start,
		null,
		ohmygod_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
	  	new MachineSound[] {
			new MachineSound(
				SOUND_OKIM6295,
				okim6295_interface
			)
		}
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_ohmygod = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1 );	ROM_LOAD_WIDE_SWAP( "omg-p.114", 0x00000, 0x80000, 0x48fa40ca );
		ROM_REGION( 0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "omg-b.117",    0x00000, 0x80000, 0x73621fa6 );
		ROM_REGION( 0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "omg-s.120",    0x00000, 0x80000, 0x6413bd36 );
		ROM_REGION( 0x240000, REGION_SOUND1 );	ROM_LOAD( "omg-g.107",    0x00000, 0x200000, 0x7405573c );	/* 00000-1ffff is fixed, 20000-3ffff is banked */
		ROM_RELOAD(               0x40000, 0x200000 );ROM_END(); }}; 
	
	
	
	public static GameDriver driver_ohmygod	   = new GameDriver("1993"	,"ohmygod"	,"ohmygod.java"	,rom_ohmygod,null	,machine_driver_ohmygod	,input_ports_ohmygod	,null	,ROT0	,	"Atlus", "Oh My God! (Japan)" )
}
