/***************************************************************************

Tail to Nose / Super Formula - (c) 1989 Video System Co.

Driver by Nicola Salmoria


press F1+F3 to see ROM/RAM tests and the final animation

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class tail2nos
{
	
	
	extern UBytePtr tail2nos_bgvideoram;
	
	
	
	public static ReadHandlerPtr pip  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return rand()&0xffff;
	} };
	static public static InitMachinePtr tail2nos_init_machine = new InitMachinePtr() { public void handler() 
	{
		/* point to the extra ROMs */
		cpu_setbank(1,memory_region(REGION_USER1));
		cpu_setbank(2,memory_region(REGION_USER2));
	
		/* initialize sound bank */
		cpu_setbank(9,memory_region(REGION_CPU2) + 0x10000);
	} };
	
	
	public static WriteHandlerPtr sound_command_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
		{
			soundlatch_w.handler(offset,data & 0xff);
			cpu_cause_interrupt(1,Z80_NMI_INT);
		}
	} };
	
	public static ReadHandlerPtr tail2nos_K051316_0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return K051316_0_r(offset >> 1);
	} };
	
	public static WriteHandlerPtr tail2nos_K051316_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
			K051316_0_w(offset >> 1, data & 0xff);
	} };
	
	public static WriteHandlerPtr tail2nos_K051316_ctrl_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
			K051316_ctrl_0_w(offset >> 1, data & 0xff);
	} };
	
	public static WriteHandlerPtr sound_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		cpu_setbank(9,memory_region(REGION_CPU2) + 0x10000 + (data & 0x01) * 0x8000);
	} };
	
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
		new MemoryReadAddress( 0x200000, 0x27ffff, MRA_BANK1 ),	/* extra ROM */
		new MemoryReadAddress( 0x2c0000, 0x2dffff, MRA_BANK2 ),	/* extra ROM */
		new MemoryReadAddress( 0x400000, 0x41ffff, tail2nos_zoomdata_r ),
		new MemoryReadAddress( 0x500000, 0x500fff, tail2nos_K051316_0_r ),
		new MemoryReadAddress( 0xff8000, 0xffbfff, MRA_BANK3 ),	/* work RAM */
		new MemoryReadAddress( 0xffc000, 0xffc2ff, MRA_BANK4 ),	/* sprites */
		new MemoryReadAddress( 0xffc300, 0xffcfff, MRA_BANK5 ),
		new MemoryReadAddress( 0xffd000, 0xffdfff, tail2nos_bgvideoram_r ),
		new MemoryReadAddress( 0xffe000, 0xffefff, paletteram_word_r ),
		new MemoryReadAddress( 0xfff000, 0xfff001, input_port_0_r ),
		new MemoryReadAddress( 0xfff004, 0xfff005, input_port_1_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x200000, 0x27ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x2c0000, 0x2dffff, MWA_ROM ),
		new MemoryWriteAddress( 0x400000, 0x41ffff, tail2nos_zoomdata_w ),
		new MemoryWriteAddress( 0x500000, 0x500fff, tail2nos_K051316_0_w ),
		new MemoryWriteAddress( 0x510000, 0x51001f, tail2nos_K051316_ctrl_0_w ),
		new MemoryWriteAddress( 0xff8000, 0xffbfff, MWA_BANK3 ),	/* work RAM */
		new MemoryWriteAddress( 0xffc000, 0xffc2ff, MWA_BANK4, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0xffc300, 0xffcfff, MWA_BANK5 ),
		new MemoryWriteAddress( 0xffd000, 0xffdfff, tail2nos_bgvideoram_w, tail2nos_bgvideoram ),
		new MemoryWriteAddress( 0xffe000, 0xffefff, paletteram_xRRRRRGGGGGBBBBB_word_w, paletteram ),
		new MemoryWriteAddress( 0xfff000, 0xfff001, tail2nos_gfxbank_w ),
		new MemoryWriteAddress( 0xfff008, 0xfff009, sound_command_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x77ff, MRA_ROM ),
		new MemoryReadAddress( 0x7800, 0x7fff, MRA_RAM ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_BANK9 ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x77ff, MWA_ROM ),
		new MemoryWriteAddress( 0x7800, 0x7fff, MWA_RAM ),
		new MemoryWriteAddress( 0x8000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static IOReadPort sound_readport[] =
	{
		new IOReadPort( 0x07, 0x07, soundlatch_r ),
	#if 0
		new IOReadPort( 0x18, 0x18, YM2610_status_port_0_A_r ),
		new IOReadPort( 0x1a, 0x1a, YM2610_status_port_0_B_r ),
	#endif
		new IOReadPort( -1 )	/* end of table */
	};
	
	static IOWritePort sound_writeport[] =
	{
		new IOWritePort( 0x07, 0x07, IOWP_NOP ),	/* clear pending command */
		new IOWritePort( 0x08, 0x08, YM2608_control_port_0_A_w ),
		new IOWritePort( 0x09, 0x09, YM2608_data_port_0_A_w ),
		new IOWritePort( 0x0a, 0x0a, YM2608_control_port_0_B_w ),
		new IOWritePort( 0x0b, 0x0b, YM2608_data_port_0_B_w ),
		new IOWritePort( -1 )	/* end of table */
	};
	
	
	
	static InputPortPtr input_ports_tail2nos = new InputPortPtr(){ public void handler() { 
	PORT_START(); 
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_2WAY );	PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY );	PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON2 );	PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON1 );	PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_START2 );	PORT_BITX(0x1000, IP_ACTIVE_LOW, IPT_SERVICE, "Test Advance", KEYCODE_F1, IP_JOY_DEFAULT );	PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 
		PORT_DIPNAME( 0x000f, 0x0000, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(      0x0009, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(      0x0008, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(      0x0007, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(      0x0006, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x000b, "6 Coins/4 Credits" );	PORT_DIPSETTING(      0x000c, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x000d, "5 Coins/6 Credits" );	PORT_DIPSETTING(      0x000e, DEF_STR( "4C_5C") );
		PORT_DIPSETTING(      0x000a, DEF_STR( "2C_3C") );
	//	PORT_DIPSETTING(      0x000f, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(      0x0001, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(      0x0002, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(      0x0003, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(      0x0004, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(      0x0005, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x00f0, 0x0000, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(      0x0090, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(      0x0080, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(      0x0070, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(      0x0060, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x00b0, "6 Coins/4 Credits" );	PORT_DIPSETTING(      0x00c0, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x00d0, "5 Coins/6 Credits" );	PORT_DIPSETTING(      0x00e0, DEF_STR( "4C_5C") );
		PORT_DIPSETTING(      0x00a0, DEF_STR( "2C_3C") );
	//	PORT_DIPSETTING(      0x00f0, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(      0x0010, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(      0x0020, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(      0x0030, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(      0x0040, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(      0x0050, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x0300, 0x0000, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(      0x0100, "Easy" );	PORT_DIPSETTING(      0x0000, "Normal" );	PORT_DIPSETTING(      0x0200, "Hard" );	PORT_DIPSETTING(      0x0300, "Super" );	PORT_DIPNAME( 0x0400, 0x0000, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(      0x0400, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_SERVICE( 0x0800, IP_ACTIVE_HIGH );	PORT_DIPNAME( 0x1000, 0x1000, "Game Mode" );	PORT_DIPSETTING(      0x1000, "Single" );	PORT_DIPSETTING(      0x0000, "Multiple" );	PORT_DIPNAME( 0x2000, 0x0000, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x2000, DEF_STR( "On") );
		PORT_DIPNAME( 0x4000, 0x4000, "Control Panel" );	PORT_DIPSETTING(      0x4000, "Standard" );	PORT_DIPSETTING(      0x0000, "Original" );	PORT_DIPNAME( 0x8000, 0x0000, "Country" );	PORT_DIPSETTING(      0x0000, "Domestic" );	PORT_DIPSETTING(      0x8000, "Overseas" );INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout tail2nos_charlayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,1),
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] { 1*4, 0*4, 3*4, 2*4, 5*4, 4*4, 7*4, 6*4 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8
	);
	
	static GfxLayout tail2nos_spritelayout = new GfxLayout
	(
		16,32,
		RGN_FRAC(1,2),
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] { 1*4, 0*4, 3*4, 2*4, RGN_FRAC(1,2)+1*4, RGN_FRAC(1,2)+0*4, RGN_FRAC(1,2)+3*4, RGN_FRAC(1,2)+2*4,
				5*4, 4*4, 7*4, 6*4, RGN_FRAC(1,2)+5*4, RGN_FRAC(1,2)+4*4, RGN_FRAC(1,2)+7*4, RGN_FRAC(1,2)+6*4 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32,
				8*32, 9*32, 10*32, 11*32, 12*32, 13*32, 14*32, 15*32,
				16*32, 17*32, 18*32, 19*32, 20*32, 21*32, 22*32, 23*32,
				24*32, 25*32, 26*32, 27*32, 28*32, 29*32, 30*32, 31*32 },
		128*8
	);
	
	static GfxDecodeInfo tail2nos_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, tail2nos_charlayout,   0, 128 ),
		new GfxDecodeInfo( REGION_GFX2, 0, tail2nos_spritelayout, 0, 128 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static void irqhandler(int irq)
	{
		cpu_set_irq_line(1,0,irq ? ASSERT_LINE : CLEAR_LINE);
	}
	
	static struct YM2608interface ym2608_interface =
	{
		1,
		8000000,	/* 8 MHz??? */
		{ 25 },
		{ 0 },
		{ 0 },
		{ 0 },
		{ sound_bankswitch_w },
		{ irqhandler },
		{ REGION_SOUND1 },
		{ YM3012_VOL(50,MIXER_PAN_LEFT,50,MIXER_PAN_RIGHT) }
	};
	
	
	
	static MachineDriver machine_driver_tail2nos = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				20000000/2,	/* 10 MHz (?) */
				readmem,writemem,null,null,
				m68_level6_irq,1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				8000000/2,	/* 4 MHz ??? */
				sound_readmem,sound_writemem,sound_readport,sound_writeport,
				ignore_interrupt,0	/* NMIs are triggered by the main CPU */
									/* IRQs are triggered by the YM2608 */
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,       /* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		tail2nos_init_machine,
	
		/* video hardware */
		64*8, 32*8, new rectangle( 0*8, 40*8-1, 1*8, 31*8-1 ),
		tail2nos_gfxdecodeinfo,
		2048, 2048,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		tail2nos_vh_start,
		tail2nos_vh_stop,
		tail2nos_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2608,
				ym2608_interface,
			)
		}
	);
	
	
	
	static RomLoadPtr rom_tail2nos = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );/* 68000 code */
		ROM_LOAD_EVEN( "v4",           0x00000, 0x10000, 0x1d4240c2 );	ROM_LOAD_ODD ( "v7",           0x00000, 0x10000, 0x0fb70066 );	ROM_LOAD_EVEN( "v3",           0x20000, 0x10000, 0xe2e0abad );	ROM_LOAD_ODD ( "v6",           0x20000, 0x10000, 0x069817a7 );
		ROM_REGION( 0x80000, REGION_USER1 );	/* extra ROM mapped at 200000 */
		ROM_LOAD_WIDE_SWAP( "a23",     0x00000, 0x80000, 0xd851cf04 );
		ROM_REGION( 0x20000, REGION_USER2 );	/* extra ROM mapped at 2c0000 */
		ROM_LOAD_EVEN( "v5",           0x00000, 0x10000, 0xa9fe15a1 );	ROM_LOAD_ODD ( "v8",           0x00000, 0x10000, 0x4fb6a43e );
		ROM_REGION( 0x20000, REGION_CPU2 );/* 64k for the audio CPU + banks */
		ROM_LOAD( "v2",           0x00000, 0x08000, 0x920d8920 );	ROM_LOAD( "v1",           0x10000, 0x10000, 0xbf35c1a4 );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "a24",          0x00000, 0x80000, 0xb1e9de43 );	ROM_LOAD( "o1s",          0x80000, 0x40000, 0xe27a8eb4 );
		ROM_REGION( 0x080000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "oj1",          0x000000, 0x40000, 0x39c36b35 );	ROM_LOAD( "oj2",          0x040000, 0x40000, 0x77ccaea2 );
		ROM_REGION( 0x20000, REGION_GFX3 );/* gfx data for the 051316 */
		/* RAM, not ROM - handled at run time */
	
		ROM_REGION( 0x20000, REGION_SOUND1 );/* sound samples */
		ROM_LOAD( "osb",          0x00000, 0x20000, 0xd49ab2f5 );ROM_END(); }}; 
	
	static RomLoadPtr rom_sformula = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );/* 68000 code */
		ROM_LOAD_EVEN( "ic129.4",      0x00000, 0x10000, 0x672bf690 );	ROM_LOAD_ODD ( "ic130.7",      0x00000, 0x10000, 0x73f0c91c );	ROM_LOAD_EVEN( "v3",           0x20000, 0x10000, 0xe2e0abad );	ROM_LOAD_ODD ( "v6",           0x20000, 0x10000, 0x069817a7 );
		ROM_REGION( 0x80000, REGION_USER1 );	/* extra ROM mapped at 200000 */
		ROM_LOAD_WIDE_SWAP( "a23",     0x00000, 0x80000, 0xd851cf04 );
		ROM_REGION( 0x20000, REGION_USER2 );	/* extra ROM mapped at 2c0000 */
		ROM_LOAD_EVEN( "v5",           0x00000, 0x10000, 0xa9fe15a1 );	ROM_LOAD_ODD ( "v8",           0x00000, 0x10000, 0x4fb6a43e );
		ROM_REGION( 0x20000, REGION_CPU2 );/* 64k for the audio CPU + banks */
		ROM_LOAD( "v2",           0x00000, 0x08000, 0x920d8920 );	ROM_LOAD( "v1",           0x10000, 0x10000, 0xbf35c1a4 );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "a24",          0x00000, 0x80000, 0xb1e9de43 );	ROM_LOAD( "o1s",          0x80000, 0x40000, 0xe27a8eb4 );
		ROM_REGION( 0x080000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "oj1",          0x000000, 0x40000, 0x39c36b35 );	ROM_LOAD( "oj2",          0x040000, 0x40000, 0x77ccaea2 );
		ROM_REGION( 0x20000, REGION_GFX3 );/* gfx data for the 051316 */
		/* RAM, not ROM - handled at run time */
	
		ROM_REGION( 0x20000, REGION_SOUND1 );/* sound samples */
		ROM_LOAD( "osb",          0x00000, 0x20000, 0xd49ab2f5 );ROM_END(); }}; 
	
	
	
	public static GameDriver driver_tail2nos	   = new GameDriver("1989"	,"tail2nos"	,"tail2nos.java"	,rom_tail2nos,null	,machine_driver_tail2nos	,input_ports_tail2nos	,null	,ROT90	,	"V-System Co.", "Tail to Nose - Great Championship", GAME_NO_COCKTAIL )
	public static GameDriver driver_sformula	   = new GameDriver("1989"	,"sformula"	,"tail2nos.java"	,rom_sformula,driver_tail2nos	,machine_driver_tail2nos	,input_ports_tail2nos	,null	,ROT90	,	"V-System Co.", "Super Formula (Japan)", GAME_NO_COCKTAIL )
}
