/***************************************************************************

								-=  Magix  =-
							 (c) 1995  Yun Sung

				driver by	Luca Elia (eliavit@unina.it)

CPU : Z80B
SND : Z80A + YM3812 + OKI M5205
OSC : 16.000 MHz

Notes:

- Title changes to "Rock" via DSW
- In service mode press Service Coin (e.g. '9')

To Do:

- Better Sound

***************************************************************************/
/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class magix
{
	
	/* Variables defined in vidhrdw */
	extern UBytePtr magix_videoram_0,*magix_videoram_1;
	
	/* Functions defined in vidhrdw */
	
	
	READ_HANDLER ( magix_videoram_r );
	
	
	
	
	
	public static InitMachinePtr magix_init_machine = new InitMachinePtr() { public void handler() 
	{
		UBytePtr RAM = memory_region(REGION_CPU1) + 0x24000;
	
		magix_videoram_0 = RAM + 0x0000;	// Ram is banked
		magix_videoram_1 = RAM + 0x2000;
		magix_videobank_w(0,0);
	} };
	
	
	/***************************************************************************
	
	
									Main CPU
	
	
	***************************************************************************/
	
	
	/***************************************************************************
										Magix
	***************************************************************************/
	
	public static WriteHandlerPtr magix_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
	
		int bank = data & 7;
		if (bank != data)	logerror("CPU #0 - PC %04X: Bank %02X\n",cpu_get_pc(),data);
	
		if (bank < 3)	RAM = &RAM[0x4000 * bank];
		else			RAM = &RAM[0x4000 * (bank-3) + 0x10000];
	
		cpu_setbank(1, RAM);
	} };
	
	/*
		Banked Video RAM:
	
		c000-c7ff	Palette	(bit 1 of port 0 switches between 2 banks)
	
		c800-cfff	Color	(bit 0 of port 0 switches between 2 banks)
		d000-dfff	Tiles	""
	*/
	
	static MemoryReadAddress magix_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM				),	// ROM
		new MemoryReadAddress( 0x8000, 0xbfff, MRA_BANK1				),	// Banked ROM
		new MemoryReadAddress( 0xc000, 0xdfff, magix_videoram_r		),	// Video RAM (Banked)
		new MemoryReadAddress( 0xe000, 0xffff, MRA_RAM				),	// RAM
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress magix_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x0000, MWA_ROM				),	// ROM
		new MemoryWriteAddress( 0x0001, 0x0001, magix_bankswitch_w	),	// ROM Bankswitching
		new MemoryWriteAddress( 0x0002, 0xbfff, MWA_ROM				),	// ROM
		new MemoryWriteAddress( 0xc000, 0xdfff, magix_videoram_w		),	// Video RAM (Banked)
		new MemoryWriteAddress( 0xe000, 0xffff, MWA_RAM				),	// RAM
		new MemoryWriteAddress( -1 )
	};
	
	
	
	
	static IOReadPort magix_readport[] =
	{
		new IOReadPort( 0x00, 0x00, input_port_0_r		),	// Coins
		new IOReadPort( 0x01, 0x01, input_port_1_r		),	// P1
		new IOReadPort( 0x02, 0x02, input_port_2_r		),	// P2
		new IOReadPort( 0x03, 0x03, input_port_3_r		),	// DSW 1
		new IOReadPort( 0x04, 0x04, input_port_4_r		),	// DSW 2
		new IOReadPort( -1 )
	};
	
	static IOWritePort magix_writeport[] =
	{
		new IOWritePort( 0x00, 0x00, magix_videobank_w		),	// Video RAM Bank
		new IOWritePort( 0x01, 0x01, magix_bankswitch_w	),	// ROM Bankswitching (again?)
		new IOWritePort( 0x02, 0x02, soundlatch_w			),	// To Sound CPU
		new IOWritePort( 0x06, 0x06, magix_flipscreen_w	),	// Flip Screen
		new IOWritePort( 0x07, 0x07, IOWP_NOP				),	// ? (end of IRQ, random value)
		new IOWritePort( -1 )
	};
	
	
	
	
	
	
	/***************************************************************************
	
	
									Sound CPU
	
	
	***************************************************************************/
	
	
	/***************************************************************************
										Magix
	***************************************************************************/
	
	static int adpcm;
	
	public static WriteHandlerPtr magix_sound_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_CPU2);
		int bank = data & 7;
	
		if ( bank != (data&(~0x20)) ) 	logerror("CPU #1 - PC %04X: Bank %02X\n",cpu_get_pc(),data);
	
		if (bank < 3)	RAM = &RAM[0x4000 * bank];
		else			RAM = &RAM[0x4000 * (bank-3) + 0x10000];
	
		cpu_setbank(3, RAM);
	
		MSM5205_reset_w(0,data & 0x20);
	} };
	
	public static WriteHandlerPtr magix_adpcm_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* Swap the nibbles */
		adpcm = ((data&0xf)<<4) | ((data >>4)&0xf);
	} };
	
	
	
	static MemoryReadAddress magix_sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM			),	// ROM
		new MemoryReadAddress( 0x8000, 0xbfff, MRA_BANK3			),	// Banked ROM
		new MemoryReadAddress( 0xf000, 0xf7ff, MRA_RAM			),	// RAM
		new MemoryReadAddress( 0xf800, 0xf800, soundlatch_r		),	// From Main CPU
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress magix_sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM					),	// ROM
		new MemoryWriteAddress( 0x8000, 0xbfff, MWA_ROM					),	// Banked ROM
		new MemoryWriteAddress( 0xe000, 0xe000, magix_sound_bankswitch_w	),	// ROM Bankswitching
		new MemoryWriteAddress( 0xe400, 0xe400, magix_adpcm_w				),
		new MemoryWriteAddress( 0xec00, 0xec00, YM3812_control_port_0_w	),	// YM3812
		new MemoryWriteAddress( 0xec01, 0xec01, YM3812_write_port_0_w		),
		new MemoryWriteAddress( 0xf000, 0xf7ff, MWA_RAM					),	// RAM
		new MemoryWriteAddress( -1 )
	};
	
	static IOReadPort magix_sound_readport[] =
	{
		new IOReadPort( -1 )
	};
	static IOWritePort magix_sound_writeport[] =
	{
		new IOWritePort( -1 )
	};
	
	
	
	
	
	/***************************************************************************
	
	
									Input Ports
	
	
	***************************************************************************/
	
	static InputPortPtr input_ports_magix = new InputPortPtr(){ public void handler() { 

		PORT_START(); 	// IN0 - Coins
		PORT_BIT(  0x01, IP_ACTIVE_LOW, IPT_UNKNOWN  );	PORT_BIT(  0x02, IP_ACTIVE_LOW, IPT_START2   );	PORT_BIT(  0x01, IP_ACTIVE_LOW, IPT_UNKNOWN  );	PORT_BIT(  0x08, IP_ACTIVE_LOW, IPT_START1   );	PORT_BIT(  0x01, IP_ACTIVE_LOW, IPT_UNKNOWN  );	PORT_BIT(  0x20, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT(  0x01, IP_ACTIVE_LOW, IPT_UNKNOWN  );	PORT_BIT(  0x80, IP_ACTIVE_LOW, IPT_COIN1    );
		PORT_START(); 	// IN1 - Player 1
		PORT_BIT(  0x01, IP_ACTIVE_LOW, IPT_UNKNOWN  );	PORT_BIT(  0x02, IP_ACTIVE_LOW, IPT_UNKNOWN  );	PORT_BIT(  0x04, IP_ACTIVE_LOW, IPT_BUTTON2        | IPF_PLAYER1 );// same as button1 !?
		PORT_BIT(  0x08, IP_ACTIVE_LOW, IPT_BUTTON1        | IPF_PLAYER1 );	PORT_BIT(  0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 );	PORT_BIT(  0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER1 );	PORT_BIT(  0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER1 );	PORT_BIT(  0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER1 );
		PORT_START(); 	// IN2 - Player 2
		PORT_BIT(  0x01, IP_ACTIVE_LOW, IPT_UNKNOWN  );	PORT_BIT(  0x02, IP_ACTIVE_LOW, IPT_UNKNOWN  );	PORT_BIT(  0x04, IP_ACTIVE_LOW, IPT_BUTTON2        | IPF_PLAYER2 );// same as button1 !?
		PORT_BIT(  0x08, IP_ACTIVE_LOW, IPT_BUTTON1        | IPF_PLAYER2 );	PORT_BIT(  0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );	PORT_BIT(  0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 );	PORT_BIT(  0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 );	PORT_BIT(  0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER2 );
		PORT_START(); 	// IN3 - DSW 1
		PORT_SERVICE( 0x01, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x06, 0x06, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x00, "Easy" );	PORT_DIPSETTING(    0x06, "Normal" );	PORT_DIPSETTING(    0x04, "Hard" );	PORT_DIPSETTING(    0x02, "Hardest" );	PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0xe0, 0xe0, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_4C") );
	
		PORT_START(); 	// IN4 - DSW 2
		PORT_DIPNAME( 0x01, 0x01, "Title" );	PORT_DIPSETTING(    0x01, "Magix" );	PORT_DIPSETTING(    0x00, "Rock" );	PORT_DIPNAME( 0x02, 0x02, "Unknown 2-1" );// the rest seems unused
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, "Unknown 2-2" );	PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, "Unknown 2-3" );	PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, "Unknown 2-4" );	PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, "Unknown 2-5" );	PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, "Unknown 2-6" );	PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "Unknown 2-7" );	PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
	INPUT_PORTS_END(); }}; 
	
	
	
	
	/***************************************************************************
	
	
									Graphics Layouts
	
	
	***************************************************************************/
	
	/* 8x8x4 tiles in 2 roms */
	static GfxLayout layout_8x8x4_split = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,2),
		4,
		new int[] {0,1,2,3},
		new int[] {RGN_FRAC(1,2)+1*4,RGN_FRAC(1,2)+0*4,1*4,0*4, RGN_FRAC(1,2)+3*4,RGN_FRAC(1,2)+2*4,3*4,2*4},
		new int[] {0*16,1*16,2*16,3*16,4*16,5*16,6*16,7*16},
		4*8*4
	);
	
	
	
	/*	Tiles in the background are 6x8 (!). They are 8 planes deep and are
		oddily spread between 3 ROMs. We just stretch each tile to an 8x8 one */
	
	static GfxLayout layout_6x8x8_stretch = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,3),
		8,
		new int[] {0,1,2,3,4,5,6,7},
		new int[] {	RGN_FRAC(2,3) + 0*8, RGN_FRAC(1,3) + 0*8, RGN_FRAC(0,3) + 0*8,
			RGN_FRAC(0,3) + 0*8,	/* Repeated pixel to pad to 8x8 */
	
			RGN_FRAC(2,3) + 1*8, RGN_FRAC(1,3) + 1*8, RGN_FRAC(0,3) + 1*8,
			RGN_FRAC(0,3) + 1*8,	/* Repeated pixel to pad to 8x8 */
		},
		new int[] {0*32,0*32+2*8, 1*32,1*32+2*8, 2*32,2*32+2*8, 3*32,3*32+2*8},
		4*4*8
	);
	
	
	/***************************************************************************
										Magix
	***************************************************************************/
	
	static GfxDecodeInfo magix_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, layout_6x8x8_stretch, 0, 0x08 ), // [0] Tiles (Background)
		new GfxDecodeInfo( REGION_GFX2, 0, layout_8x8x4_split,	 0,	0x40 ), // [1] Tiles (Text)
		new GfxDecodeInfo( -1 )
	};
	
	
	
	
	
	
	/***************************************************************************
	
	
									Machine Drivers
	
	
	***************************************************************************/
	
	
	/***************************************************************************
										Magix
	***************************************************************************/
	
	public static vclk_interruptPtr magix_adpcm_int = new vclk_interruptPtr() { public void handler(int irq) 
	{
		static int toggle=0;
	
		MSM5205_data_w (0,adpcm>>4);
		adpcm<<=4;
	
		toggle ^= 1;
		if (toggle != 0)
			cpu_set_nmi_line(1,PULSE_LINE);
	} };
	
	static YM3812interface magix_ym3812_interface = new YM3812interface
	(
		1,
		4000000,	/* ? */
		new int[] { 50 },
		new WriteYmHandlerPtr[] { 0  },
	);
	
	static MSM5205interface magix_msm5205_interface = new MSM5205interface
	(
		1,
		384000,
		new vclk_interruptPtr[] { magix_adpcm_int },	/* interrupt function */
		new int[] { MSM5205_S96_4B },		/* 4KHz, 4 Bits */
		{ 80 }
	);
	
	
	static MachineDriver machine_driver_magix = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,					/* Z80B */
				8000000,					/* ? */
				magix_readmem, magix_writemem,magix_readport,magix_writeport,
				interrupt, 1	/* No nmi routine */
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,	/* Z80A */
				4000000,					/* ? */
				magix_sound_readmem, magix_sound_writemem,magix_sound_readport,magix_sound_writeport,
				interrupt, 1	/* NMI caused by the MSM5205? */
			)
		},
		60,DEFAULT_60HZ_VBLANK_DURATION,
		1,
		magix_init_machine,
	
		/* video hardware */
		512, 256, new rectangle( 0+64, 512-64-1, 0+8, 256-8-1 ),
		magix_gfxdecodeinfo,
		0x800, 0x800,
		null,
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		magix_vh_start,
		null,
		magix_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM3812,
				magix_ym3812_interface
			),
			new MachineSound(
				SOUND_MSM5205,
				magix_msm5205_interface
			)
		}
	);
	
	
	
	
	
	
	/***************************************************************************
	
	
									ROMs Loading
	
	
	***************************************************************************/
	
	
	
	/***************************************************************************
	
										Magix
	
	***************************************************************************/
	
	static RomLoadPtr rom_magix = new RomLoadPtr(){ public void handler(){ 
	
		ROM_REGION( 0x24000+0x4000, REGION_CPU1 );	/* Main Z80 Code */
		ROM_LOAD( "magix.07", 0x00000, 0x0c000, 0xd4d0b68b );	ROM_CONTINUE(         0x10000, 0x14000             );	/* $2000 bytes for bank 0 of video ram (text) */
		/* $2000 bytes for bank 1 of video ram (background) */
	
		ROM_REGION( 0x24000, REGION_CPU2 );	/* Sound Z80 Code */
		ROM_LOAD( "magix.08", 0x00000, 0x0c000, 0x6fd60be9 );	ROM_CONTINUE(         0x10000, 0x14000             );
		ROM_REGION( 0x180000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* Background */
		ROM_LOAD( "magix.01",  0x000000, 0x80000, 0x4590d782 );	ROM_LOAD( "magix.02",  0x080000, 0x80000, 0x09efb8e5 );	ROM_LOAD( "magix.03",  0x100000, 0x80000, 0xc8cb0373 );
		ROM_REGION( 0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* Text */
		ROM_LOAD( "magix.05", 0x00000, 0x20000, 0x862d378c );// only first $8000 bytes != 0
		ROM_LOAD( "magix.06", 0x20000, 0x20000, 0x8b2ab901 );// only first $8000 bytes != 0
	
	ROM_END(); }}; 
	
	
	/***************************************************************************
	
	
									Game Drivers
	
	
	***************************************************************************/
	
	// Title changes to "Rock" via DSW
	public static GameDriver driver_magix	   = new GameDriver("1995"	,"magix"	,"magix.java"	,rom_magix,null	,machine_driver_magix	,input_ports_magix	,null	,ROT0_16BIT	,	"Yun Sung", "Magix", GAME_IMPERFECT_SOUND )
}
