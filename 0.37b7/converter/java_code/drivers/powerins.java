/***************************************************************************

						  -= Power Instinct =-
							(C) 1993 Atlus

				driver by	Luca Elia (eliavit@unina.it)

CPU:	MC68000
Sound:	OKIM6295

- Note:	To enter test mode press F2 (Test)
		Use 9 (Service Coin) to change page.

TODO:
- sprites flip y (not used by the game)


***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class powerins
{
	
	/* Variables that vidhrdw has access to */
	
	/* Variables defined in vidhrdw */
	extern UBytePtr powerins_vram_0, *powerins_vctrl_0;
	extern UBytePtr powerins_vram_1, *powerins_vctrl_1;
	extern UBytePtr powerins_vregs;
	
	/* Functions defined in vidhrdw */
	READ_HANDLER ( powerins_vregs_r );
	
	
	
	
	
	/***************************************************************************
	
									Memory Maps
	
	***************************************************************************/
	
	public static ReadHandlerPtr powerins_input_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch (offset)
		{
			case 0x00:	return readinputport(1);		// Coins + Start Buttons
			case 0x02:	return readinputport(0);		// P1+P2
			case 0x08:	return readinputport(2);		// DSW 1
			case 0x0a:	return readinputport(3);		// DSW 2
			case 0x3e:	return OKIM6295_status_0_r(0);	// OKI Status
	
			default:
				logerror("PC %06X - Read input %02X !\n", cpu_get_pc(), offset);
				return 0;
		}
	} };
	
	static MemoryReadAddress powerins_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x0fffff, MRA_ROM ),	// ROM
		new MemoryReadAddress( 0x100000, 0x10003f, powerins_input_r ),	// Input Ports
		new MemoryReadAddress( 0x120000, 0x120fff, MRA_BANK2 ),	// Palette
	/**/new MemoryReadAddress( 0x130000, 0x130007, MRA_BANK3 ),	// VRAM 0 Control
		new MemoryReadAddress( 0x140000, 0x143fff, MRA_BANK4 ),	// VRAM 0
		new MemoryReadAddress( 0x170000, 0x170fff, MRA_BANK5 ),	// VRAM 1
		new MemoryReadAddress( 0x180000, 0x18ffff, MRA_BANK6 ),	// RAM + Sprites
	//	new MemoryReadAddress( 0x990000, 0x99003f, powerins_vregs_r ),	// Fake: use to see the video regs.
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress powerins_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x0fffff, MWA_ROM ),	// ROM
		new MemoryWriteAddress( 0x100000, 0x10003f, powerins_vregs_w, powerins_vregs ),	// Video Regs
		new MemoryWriteAddress( 0x120000, 0x120fff, powerins_paletteram_w, paletteram ),	// Palette
		new MemoryWriteAddress( 0x130000, 0x130007, MWA_BANK3, powerins_vctrl_0 ),	// VRAM 0 Control
		new MemoryWriteAddress( 0x140000, 0x143fff, powerins_vram_0_w, powerins_vram_0 ),	// VRAM 0
		new MemoryWriteAddress( 0x170000, 0x170fff, powerins_vram_1_w, powerins_vram_1 ),	// VRAM 1
		new MemoryWriteAddress( 0x171000, 0x171fff, powerins_vram_1_w ),	// Mirror of VRAM 1?
		new MemoryWriteAddress( 0x180000, 0x18ffff, MWA_BANK6, spriteram ),	// RAM + Sprites
		new MemoryWriteAddress( -1 )
	};
	
	
	
	
	/***************************************************************************
	
									Input Ports
	
	***************************************************************************/
	
	static InputPortPtr input_ports_powerins = new InputPortPtr(){ public void handler() { 
	PORT_START(); 	// IN0 - $100002 - Player 1 & 2
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 );	PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT	 | IPF_PLAYER1 );	PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER1 );	PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER1 );	PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1        | IPF_PLAYER1 );	PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2        | IPF_PLAYER1 );	PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3        | IPF_PLAYER1 );	PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_BUTTON4        | IPF_PLAYER1 );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );	PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT	 | IPF_PLAYER2 );	PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 );	PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER2 );	PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1        | IPF_PLAYER2 );	PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2        | IPF_PLAYER2 );	PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3        | IPF_PLAYER2 );	PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_BUTTON4        | IPF_PLAYER2 );
		PORT_START(); 	// IN1 - $100000 - Coins
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_COIN1    );	PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_COIN2    );	PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_START1   );	PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_START2   );	PORT_BITX(0x0020, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN  );	PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN  );
		PORT_START(); 	// IN2 - $100008 - DSW 1
		PORT_DIPNAME( 0x0001, 0x0001, DEF_STR( "Free_Play") );
		PORT_DIPSETTING(      0x0001, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x000e, 0x000e, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(      0x0008, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(      0x0004, DEF_STR( "3C_1C") );
	//	PORT_DIPSETTING(      0x000c, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x000e, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x0006, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(      0x000a, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(      0x0002, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0x0070, 0x0070, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(      0x0040, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(      0x0020, DEF_STR( "3C_1C") );
	//	PORT_DIPSETTING(      0x0060, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x0070, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x0030, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(      0x0050, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(      0x0010, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0x0080, 0x0080, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(      0x0080, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") ); )
	
		PORT_START(); 	// IN3 - $10000a - DSW 2
		PORT_DIPNAME( 0x0001, 0x0001, "Coin Slots" );	PORT_DIPSETTING(      0x0001, "1" );	PORT_DIPSETTING(      0x0000, "2" );	PORT_DIPNAME( 0x0002, 0x0002, "2 Player Game" );	PORT_DIPSETTING(      0x0002, "2 Credits" );	PORT_DIPSETTING(      0x0000, "1 Credit" );	PORT_DIPNAME( 0x0004, 0x0000, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(      0x0004, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0008, 0x0008, "Allow Continue" );	PORT_DIPSETTING(      0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0008, DEF_STR( "On") );
		PORT_DIPNAME( 0x0010, 0x0010, "Blood" );	PORT_DIPSETTING(      0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0010, DEF_STR( "On") );
		PORT_DIPNAME( 0x0020, 0x0020, "Unknown 2-5" );	PORT_DIPSETTING(      0x0020, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0040, 0x0040, "Unknown 2-6" );	PORT_DIPSETTING(      0x0040, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0080, 0x0080, "Unknown 2-7" );	PORT_DIPSETTING(      0x0080, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	
	
	
	/***************************************************************************
	
									Graphics Layouts
	
	***************************************************************************/
	
	/* 8x8x4 tiles */
	static GfxLayout layout_8x8x4 = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,1),
		4,
		new int[] {0,1,2,3},
		new int[] {0*4,1*4,2*4,3*4,4*4,5*4,6*4,7*4},
		new int[] {0*32,1*32,2*32,3*32,4*32,5*32,6*32,7*32},
		8*8*4
	);
	
	
	/* 16x16x4 tiles (made of four 8x8 tiles) */
	static GfxLayout layout_16x16x4 = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,1),
		4,
		new int[] {0,1,2,3},
		new int[] {0*4,1*4,2*4,3*4,4*4,5*4,6*4,7*4,
		 128*4,129*4,130*4,131*4,132*4,133*4,134*4,135*4},
		new int[] {0*32,1*32,2*32,3*32,4*32,5*32,6*32,7*32,
		 8*32,9*32,10*32,11*32,12*32,13*32,14*32,15*32},
		16*16*4
	);
	
	
	/* 16x16x4 tiles (made of four 8x8 tiles). The bytes are swapped */
	static GfxLayout layout_16x16x4_swap = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,1),
		4,
		new int[] {0,1,2,3},
		new int[] {2*4,3*4,0*4,1*4,6*4,7*4,4*4,5*4,
		 130*4,131*4,128*4,129*4,134*4,135*4,132*4,133*4},
		new int[] {0*32,1*32,2*32,3*32,4*32,5*32,6*32,7*32,
		 8*32,9*32,10*32,11*32,12*32,13*32,14*32,15*32},
		16*16*4
	);
	
	
	static GfxDecodeInfo powerins_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, layout_16x16x4,      0x000, 0x20 ), // [0] Tiles
		new GfxDecodeInfo( REGION_GFX2, 0, layout_8x8x4,        0x200, 0x10 ), // [1] Tiles
		new GfxDecodeInfo( REGION_GFX3, 0, layout_16x16x4_swap, 0x400, 0x40 ), // [2] Sprites
		new GfxDecodeInfo( -1 )
	};
	
	
	
	
	
	
	/***************************************************************************
	
									Machine Drivers
	
	***************************************************************************/
	
	static OKIM6295interface powerins_okim6295_interface = new OKIM6295interface
	(
		1,
		new int[] { 6000 },		/* ? */
		new int[] { REGION_SOUND1 },
		new int[] { 100 }
	);
	
	
	
	static MachineDriver machine_driver_powerins = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				10000000,	/* ? (it affects the game's speed!) */
				powerins_readmem, powerins_writemem,null,null,
				m68_level4_irq, 1
			),
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,
		1,
		null,
	
		/* video hardware */
		320, 256, new rectangle( 0, 320-1, 0+16, 256-16-1 ),
		powerins_gfxdecodeinfo,
		0x800, 0x800,
		null,
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		powerins_vh_start,
		null,
		powerins_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_OKIM6295,
				powerins_okim6295_interface
			)
		}
	);
	
	
	
	
	
	
	/***************************************************************************
	
									ROMs Loading
	
	***************************************************************************/
	
	
	
	/***************************************************************************
	
									Power Instinct
	
	Location     Device       File ID     Checksum
	----------------------------------------------
	             27C240        ROM1         4EA1    [ MAIN PROGRAM ]
	             27C240        ROM2         FE60    [ PROGRAM DATA ]
	             27C010        ROM3         B9F7    [  CHARACTER   ]
	             27C040        ROM4         2780    [  BACKGROUND  ]
	             27C040        ROM5         98E0    [   PCM DATA   ]
	            23C1600        ROM6         D9E9    [  BACKGROUND  ]
	            23C1600        ROM7         8B04    [  MOTION OBJ  ]
	            23C1600        ROM8         54B2    [  MOTION OBJ  ]
	            23C1600        ROM9         C7C8    [  MOTION OBJ  ]
	            23C1600        ROM10        852A    [  MOTION OBJ  ]
	
	Notes:  This archive is of a bootleg version
	        The main program is encrypted, using PLDs
	
	Brief hardware overview
	-----------------------
	
	Main processor  -  68000
	                -  TPC1020AFN-084C (CPLD)
	
	Sound processor -  Main processor
	                -  K-665-9249      (M6295)
	
	***************************************************************************/
	
	static RomLoadPtr rom_powerins = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );	/* 68000 Code */
		ROM_LOAD_WIDE_SWAP( "rom1", 0x000000, 0x080000, 0xb86c84d6 );	ROM_LOAD_WIDE_SWAP( "rom2", 0x080000, 0x080000, 0xd3d7a782 );
		ROM_REGION( 0x280000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* Layer 0 */
		ROM_LOAD( "rom6",  0x000000, 0x200000, 0xb6c10f80 );	ROM_LOAD( "rom4",  0x200000, 0x080000, 0x2dd76149 );
		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* Layer 1 */
		ROM_LOAD( "rom3",  0x000000, 0x020000, 0x6a579ee0 );
		ROM_REGION( 0x800000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* Sprites */
		ROM_LOAD( "rom10", 0x000000, 0x200000, 0xefad50e8 );	ROM_LOAD( "rom9",  0x200000, 0x200000, 0x08229592 );	ROM_LOAD( "rom8",  0x400000, 0x200000, 0xb02fdd6d );	ROM_LOAD( "rom7",  0x600000, 0x200000, 0x92ab9996 );
		ROM_REGION( 0x090000, REGION_SOUND1 );/* 8 bit adpcm (banked) */
		ROM_LOAD( "rom5", 0x000000, 0x030000, 0x88579c8f );	ROM_CONTINUE(     0x040000, 0x050000 );ROM_END(); }}; 
	
	
	
	public static GameDriver driver_powerins	   = new GameDriver("1993"	,"powerins"	,"powerins.java"	,rom_powerins,null	,machine_driver_powerins	,input_ports_powerins	,null	,ROT0_16BIT	,	"Atlus", "Power Instinct (USA) [bootleg]" )
}
