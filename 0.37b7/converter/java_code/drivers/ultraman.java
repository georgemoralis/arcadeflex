/***************************************************************************

Ultraman (c) 1991  Banpresto / Bandai

Driver by Manuel Abadia <manu@teleline.es>

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class ultraman
{
	
	UBytePtr  ultraman_regs;
	
	/* from vidhrdw/ultraman.c */
	
	public static ReadHandlerPtr ultraman_K051937_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return K051937_r(offset >> 1);
	} };
	
	public static ReadHandlerPtr ultraman_K051960_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return K051960_r(offset >> 1);
	} };
	
	public static ReadHandlerPtr ultraman_K051316_0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return K051316_0_r(offset >> 1);
	} };
	
	public static ReadHandlerPtr ultraman_K051316_1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return K051316_1_r(offset >> 1);
	} };
	
	public static ReadHandlerPtr ultraman_K051316_2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return K051316_2_r(offset >> 1);
	} };
	
	public static WriteHandlerPtr ultraman_K051316_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
			K051316_0_w(offset >> 1, data & 0xff);
	} };
	
	public static WriteHandlerPtr ultraman_K051316_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
			K051316_1_w(offset >> 1, data & 0xff);
	} };
	
	public static WriteHandlerPtr ultraman_K051316_2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
			K051316_2_w(offset >> 1, data & 0xff);
	} };
	
	public static WriteHandlerPtr ultraman_K051316_ctrl_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
			K051316_ctrl_0_w(offset >> 1, data & 0xff);
	} };
	
	public static WriteHandlerPtr ultraman_K051316_ctrl_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
			K051316_ctrl_1_w(offset >> 1, data & 0xff);
	
	} };
	
	public static WriteHandlerPtr ultraman_K051316_ctrl_2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
			K051316_ctrl_2_w(offset >> 1, data & 0xff);
	} };
	
	public static WriteHandlerPtr ultraman_K051937_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
			K051937_w(offset >> 1, data & 0xff);
	} };
	
	public static WriteHandlerPtr ultraman_K051960_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
			K051960_w(offset >> 1, data & 0xff);
	} };
	
	public static WriteHandlerPtr ultraman_reg_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = READ_WORD(&ultraman_regs[offset]);
		int newword = COMBINE_WORD(oldword, data);
	
		WRITE_WORD(&ultraman_regs[offset],newword);
	
		switch (offset){
			/*	bit 0: enable wraparound for scr #1
				bit 1: msb of code for scr #1
				bit 2: enable wraparound for scr #2
				bit 3: msb of code for scr #2
				bit 4: enable wraparound for scr #3
				bit 5: msb of code for scr #3
				bit 6: coin counter 1
				bit 7: coin counter 2 */
			case 0x18:
				if ((oldword & 0x2a) != (newword & 0x2a))
					tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
				K051316_wraparound_enable(0, data & 0x01);
				K051316_wraparound_enable(1, data & 0x04);
				K051316_wraparound_enable(2, data & 0x10);
				coin_counter_w.handler(0, newword & 0x40);
				coin_counter_w.handler(1, newword & 0x80);
				break;
	
			case 0x20:	/* sound code # */
				soundlatch_w.handler(0, newword & 0xff);
				break;
	
			case 0x28:	/* cause interrupt on audio CPU */
				cpu_cause_interrupt(1,Z80_NMI_INT);
				break;
	
			case 0x30:	/* watchdog timer */
				watchdog_reset_w(0, newword & 0xff);
				break;
		}
	} };
	
	static MemoryReadAddress ultraman_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),				/* ROM */
		new MemoryReadAddress( 0x080000, 0x08ffff, MRA_BANK1 ),				/* RAM */
		new MemoryReadAddress( 0x180000, 0x183fff, paletteram_word_r ),		/* Palette */
		new MemoryReadAddress( 0x1c0000, 0x1c0001, input_port_0_r ),			/* Coins + Service */
		new MemoryReadAddress( 0x1c0002, 0x1c0003, input_port_1_r ),			/* 1P controls */
		new MemoryReadAddress( 0x1c0004, 0x1c0005, input_port_2_r ),			/* 2P controls */
		new MemoryReadAddress( 0x1c0006, 0x1c0007, input_port_3_r ),			/* DIPSW #1 */
		new MemoryReadAddress( 0x1c0008, 0x1c0009, input_port_4_r ),			/* DIPSW #2 */
		new MemoryReadAddress( 0x204000, 0x204fff, ultraman_K051316_0_r ),	/* K051316 #0 RAM */
		new MemoryReadAddress( 0x205000, 0x205fff, ultraman_K051316_1_r ),	/* K051316 #1 RAM */
		new MemoryReadAddress( 0x206000, 0x206fff, ultraman_K051316_2_r ),	/* K051316 #2 RAM */
		new MemoryReadAddress( 0x304000, 0x30400f, ultraman_K051937_r ),		/* Sprite control */
		new MemoryReadAddress( 0x304800, 0x304fff, ultraman_K051960_r ),		/* Sprite RAM */
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress ultraman_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),					/* ROM */
		new MemoryWriteAddress( 0x080000, 0x08ffff, MWA_BANK1 ),					/* RAM */
		new MemoryWriteAddress( 0x180000, 0x183fff, paletteram_xRRRRRGGGGGBBBBB_word_w, paletteram ),/* Palette */
		new MemoryWriteAddress( 0x1c0000, 0x1c0031, ultraman_reg_w, ultraman_regs ),	/* counters + sound + watchdog + gfx ctrl */
		new MemoryWriteAddress( 0x204000, 0x204fff, ultraman_K051316_0_w ),		/* K051316 #0 RAM */
		new MemoryWriteAddress( 0x205000, 0x205fff, ultraman_K051316_1_w ),		/* K051316 #1 RAM */
		new MemoryWriteAddress( 0x206000, 0x206fff, ultraman_K051316_2_w ),		/* K051316 #2 RAM */
		new MemoryWriteAddress( 0x207f80, 0x207f9f, ultraman_K051316_ctrl_0_w	),	/* K051316 #0 registers  */
		new MemoryWriteAddress( 0x207fa0, 0x207fbf, ultraman_K051316_ctrl_1_w	),	/* K051316 #1 registers */
		new MemoryWriteAddress( 0x207fc0, 0x207fdf, ultraman_K051316_ctrl_2_w	),	/* K051316 #2 registers */
		new MemoryWriteAddress( 0x304000, 0x30400f, ultraman_K051937_w ),			/* Sprite control */
		new MemoryWriteAddress( 0x304800, 0x304fff, ultraman_K051960_w ),			/* Sprite RAM */
		new MemoryWriteAddress( -1 )
	};
	
	static MemoryReadAddress ultraman_readmem_sound[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),					/* ROM */
		new MemoryReadAddress( 0x8000, 0xbfff, MRA_RAM ),					/* RAM */
		new MemoryReadAddress( 0xc000, 0xc000, soundlatch_r ),				/* Sound latch read */
		new MemoryReadAddress( 0xe000, 0xe000, OKIM6295_status_0_r ),		/* M6295 */
		new MemoryReadAddress( 0xf001, 0xf001, YM2151_status_port_0_r ),		/* YM2151 */
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress ultraman_writemem_sound[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),					/* ROM */
		new MemoryWriteAddress( 0x8000, 0xbfff, MWA_RAM ),					/* RAM */
	//	new MemoryWriteAddress( 0xd000, 0xd000, MWA_NOP ),					/* ??? */
		new MemoryWriteAddress( 0xe000, 0xe000, OKIM6295_data_0_w ),			/* M6295 */
		new MemoryWriteAddress( 0xf000, 0xf000, YM2151_register_port_0_w ),	/* YM2151 */
		new MemoryWriteAddress( 0xf001, 0xf001, YM2151_data_port_0_w ),		/* YM2151 */
		new MemoryWriteAddress( -1 )
	};
	
	static IOWritePort ultraman_writeport_sound[] =
	{
	//	new IOWritePort( 0x00, 0x00, MWA_NOP ),						/* ??? */
		new IOWritePort( -1 )
	};
	
	
	static InputPortPtr input_ports_ultraman = new InputPortPtr(){ public void handler() { 

		PORT_START(); 	/* Coins + Service */
		PORT_BIT( 0x0f, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_COIN3 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_START(); 	/* IN #1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP	  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_START(); 	/* IN #2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP	  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_START(); 	/* DSW #1 */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
	
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "5C_3C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") );
	
		PORT_START(); 	/* DSW #2 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, "Allow Continue" );	PORT_DIPSETTING(	0x00, DEF_STR( "No") );
		PORT_DIPSETTING(	0x04, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(	0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(	0x10, "Easy" );	PORT_DIPSETTING(	0x30, "Normal" );	PORT_DIPSETTING(	0x20, "Difficult" );	PORT_DIPSETTING(	0x00, "Very Difficult" );	PORT_DIPNAME( 0x40, 0x40, "Upright Controls" );	PORT_DIPSETTING(	0x40, "Single" );	PORT_DIPSETTING(	0x00, "Dual" );	PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(	0x80, DEF_STR( "Cocktail") );
	INPUT_PORTS_END(); }}; 
	
	
	
	static YM2151interface ym2151_interface = new YM2151interface
	(
		1,
		24000000/6,	/* 4 MHz (tempo verified against real board) */
		new int[] { YM3012_VOL(100,MIXER_PAN_LEFT,100,MIXER_PAN_RIGHT) },
		new WriteYmHandlerPtr[] { 0 },
	);
	
	static OKIM6295interface okim6295_interface = new OKIM6295interface
	(
		1,					/* 1 chip */
		new int[] { 8000 },			/* 8KHz? */
		new int[] { REGION_SOUND1 },	/* memory region */
		new int[] { 50 }
	);
	
	
	
	static MachineDriver machine_driver_ultraman = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				24000000/2,		/* 12 MHz? */
				ultraman_readmem,ultraman_writemem,null,null,
				m68_level4_irq,1
			),
			new MachineCPU(
				CPU_Z80  | CPU_AUDIO_CPU,
				24000000/6,		/* 4 MHz? */
				ultraman_readmem_sound, ultraman_writemem_sound,null,ultraman_writeport_sound,
				ignore_interrupt,1	/* NMI triggered by the m68000 */
			)
		},
		60,DEFAULT_REAL_60HZ_VBLANK_DURATION,
		10,
		null,
	
		/* video hardware */
		64*8, 32*8, new rectangle( 14*8, (64-14)*8-1, 2*8, 30*8-1 ),
		null,	/* decoded by KonamiIC */
		8192, 8192,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		ultraman_vh_start,
		ultraman_vh_stop,
		ultraman_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			),
			new MachineSound(
				SOUND_OKIM6295,
				okim6295_interface
			)
		}
	
	);
	
	
	
	static RomLoadPtr rom_ultraman = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x040000, REGION_CPU1 );/* 68000 code */
		ROM_LOAD_EVEN(	"910-b01.c11",	0x000000, 0x020000, 0x3d9e4323 );	ROM_LOAD_ODD(	"910-b02.d11",	0x000000, 0x020000, 0xd24c82e9 );
		ROM_REGION( 0x010000, REGION_CPU2 );/* Z80 code */
		ROM_LOAD( "910-a05.d05",	0x00000, 0x08000, 0xebaef189 );
		ROM_REGION( 0x100000, REGION_GFX1 );/* Sprites */
		ROM_LOAD( "910-a19.l04",	0x000000, 0x080000, 0x2dc9ffdc );	ROM_LOAD( "910-a20.l01",	0x080000, 0x080000, 0xa4298dce );
		ROM_REGION( 0x080000, REGION_GFX2 );/* BG 1  */
		ROM_LOAD( "910-a07.j15",	0x000000, 0x020000, 0x8b43a64e );	ROM_LOAD( "910-a08.j16",	0x020000, 0x020000, 0xc3829826 );	ROM_LOAD( "910-a09.j18",	0x040000, 0x020000, 0xee10b519 );	ROM_LOAD( "910-a10.j19",	0x060000, 0x020000, 0xcffbb0c3 );
		ROM_REGION( 0x080000, REGION_GFX3 );/* BG 2 */
		ROM_LOAD( "910-a11.l15",	0x000000, 0x020000, 0x17a5581d );	ROM_LOAD( "910-a12.l16",	0x020000, 0x020000, 0x39763fb5 );	ROM_LOAD( "910-a13.l18",	0x040000, 0x020000, 0x66b25a4f );	ROM_LOAD( "910-a14.l19",	0x060000, 0x020000, 0x09fbd412 );
		ROM_REGION( 0x080000, REGION_GFX4 );/* BG 3 */
		ROM_LOAD( "910-a15.m15",	0x000000, 0x020000, 0x6d5bfbb7 );	ROM_LOAD( "910-a16.m16",	0x020000, 0x020000, 0x5f6f8c3d );	ROM_LOAD( "910-a17.m18",	0x040000, 0x020000, 0x1f3ec4ff );	ROM_LOAD( "910-a18.m19",	0x060000, 0x020000, 0xfdc42929 );
		ROM_REGION( 0x0100, REGION_PROMS );	ROM_LOAD( "910-a21.f14",	0x000000, 0x000100, 0x64460fbc );/* priority encoder (not used) */
	
		ROM_REGION( 0x040000, REGION_SOUND1 );/* M6295 data */
		ROM_LOAD( "910-a06.c06",	0x000000, 0x040000, 0x28fa99c9 );ROM_END(); }}; 
	
	
	
	static public static InitDriverPtr init_ultraman = new InitDriverPtr() { public void handler() 
	{
		konami_rom_deinterleave_2(REGION_GFX1);
	} };
	
	
	public static GameDriver driver_ultraman	   = new GameDriver("1991"	,"ultraman"	,"ultraman.java"	,rom_ultraman,null	,machine_driver_ultraman	,input_ports_ultraman	,init_ultraman	,ROT0	,	"Banpresto/Bandai", "Ultraman (Japan)" )
}
