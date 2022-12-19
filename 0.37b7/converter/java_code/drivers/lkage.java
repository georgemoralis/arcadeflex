/***************************************************************************

Legend of Kage
(C)1985 Taito
CPU: Z80 (x2), MC68705
Sound: YM2203 (x2)

Phil Stroffolino
pjstroff@hotmail.com

TODO:
- Note that all the bootlegs are derived from a different version of the
  original which hasn't been found yet.
- SOUND: lots of unknown writes to the YM2203 I/O ports
- lkage is verfied to be an original set, but it seems to work regardless of what
  the mcu does. Moreover, the mcu returns a checksum which is different from the
  one I think the game expects (89, while the game seems to expect 5d). But the
  game works anyway, it never gives the usual Taito "BAD HW" message.
- sprite and tilemap placement is most certainly wrong

Take the following observations with a grain of salt (might not be true):
- attract mode is bogus (observe the behavior of the player)
- the second stage isn't supposed to have (red) Samurai, only Ninja.
- The final stage is almost impossible in MAME! On the arcade, I could make my
  way to the top fairly easily, but in MAME I have to use invulnerability.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class lkage
{
	
	
	extern UBytePtr lkage_scroll, *lkage_vreg;
	
	
	
	static int sound_nmi_enable,pending_nmi;
	
	public static timer_callback nmi_callback = new timer_callback() { public void handler(int param) 
	{
		if (sound_nmi_enable != 0) cpu_cause_interrupt(1,Z80_NMI_INT);
		else pending_nmi = 1;
	} };
	
	public static WriteHandlerPtr lkage_sound_command_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		soundlatch_w.handler(offset,data);
		timer_set(TIME_NOW,data,nmi_callback);
	} };
	
	public static WriteHandlerPtr lkage_sh_nmi_disable_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		sound_nmi_enable = 0;
	} };
	
	public static WriteHandlerPtr lkage_sh_nmi_enable_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		sound_nmi_enable = 1;
		if (pending_nmi != 0)
		{ /* probably wrong but commands may go lost otherwise */
			cpu_cause_interrupt(1,Z80_NMI_INT);
			pending_nmi = 0;
		}
	} };
	
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0xdfff, MRA_ROM ),
		new MemoryReadAddress( 0xe000, 0xe7ff, MRA_RAM ),
		new MemoryReadAddress( 0xe800, 0xefff, paletteram_r ),
		new MemoryReadAddress( 0xf000, 0xf003, MRA_RAM ),
		new MemoryReadAddress( 0xf062, 0xf062, lkage_mcu_r ),
		new MemoryReadAddress( 0xf080, 0xf080, input_port_0_r ), /* DSW1 */
		new MemoryReadAddress( 0xf081, 0xf081, input_port_1_r ), /* DSW2 (coinage) */
		new MemoryReadAddress( 0xf082, 0xf082, input_port_2_r ), /* DSW3 */
		new MemoryReadAddress( 0xf083, 0xf083, input_port_3_r ),	/* start buttons, insert coin, tilt */
		new MemoryReadAddress( 0xf084, 0xf084, input_port_4_r ),	/* P1 controls */
		new MemoryReadAddress( 0xf086, 0xf086, input_port_5_r ),	/* P2 controls */
		new MemoryReadAddress( 0xf087, 0xf087, lkage_mcu_status_r ),
	//	new MemoryReadAddress( 0xf0a3, 0xf0a3, MRA_NOP ), /* unknown */
		new MemoryReadAddress( 0xf0c0, 0xf0c5, MRA_RAM ),
		new MemoryReadAddress( 0xf100, 0xf15f, MRA_RAM ),
		new MemoryReadAddress( 0xf400, 0xffff, MRA_RAM ),
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xdfff, MWA_ROM ),
		new MemoryWriteAddress( 0xe000, 0xe7ff, MWA_RAM ),
		new MemoryWriteAddress( 0xe800, 0xefff, MWA_RAM, paletteram ),
	//	paletteram_xxxxRRRRGGGGBBBB_w, paletteram },
		{ 0xf000, 0xf003, MWA_RAM, &lkage_vreg }, /* video registers */
		{ 0xf060, 0xf060, lkage_sound_command_w },
		{ 0xf061, 0xf061, MWA_NOP }, /* unknown */
		{ 0xf062, 0xf062, lkage_mcu_w },
	//	{ 0xf063, 0xf063, MWA_NOP }, /* unknown */
	//	{ 0xf0a2, 0xf0a2, MWA_NOP }, /* unknown */
	//	{ 0xf0a3, 0xf0a3, MWA_NOP }, /* unknown */
		{ 0xf0c0, 0xf0c5, MWA_RAM, &lkage_scroll }, /* scrolling */
	//	{ 0xf0e1, 0xf0e1, MWA_NOP }, /* unknown */
		{ 0xf100, 0xf15f, MWA_RAM, &spriteram }, /* spriteram */
		{ 0xf400, 0xffff, lkage_videoram_w, &videoram }, /* videoram */
		{ -1 }
	};
	
	public static ReadHandlerPtr port_fetch_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return memory_region(REGION_USER1)[offset];
	} };
	
	static IOReadPort readport[] =
	{
		new IOReadPort( 0x4000, 0x7fff, port_fetch_r ),
		new IOReadPort( -1 )
	};
	
	
	static MemoryReadAddress m68705_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x0000, lkage_68705_portA_r ),
		new MemoryReadAddress( 0x0001, 0x0001, lkage_68705_portB_r ),
		new MemoryReadAddress( 0x0002, 0x0002, lkage_68705_portC_r ),
		new MemoryReadAddress( 0x0010, 0x007f, MRA_RAM ),
		new MemoryReadAddress( 0x0080, 0x07ff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress m68705_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x0000, lkage_68705_portA_w ),
		new MemoryWriteAddress( 0x0001, 0x0001, lkage_68705_portB_w ),
		new MemoryWriteAddress( 0x0002, 0x0002, lkage_68705_portC_w ),
		new MemoryWriteAddress( 0x0004, 0x0004, lkage_68705_ddrA_w ),
		new MemoryWriteAddress( 0x0005, 0x0005, lkage_68705_ddrB_w ),
		new MemoryWriteAddress( 0x0006, 0x0006, lkage_68705_ddrC_w ),
		new MemoryWriteAddress( 0x0010, 0x007f, MWA_RAM ),
		new MemoryWriteAddress( 0x0080, 0x07ff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	
	/***************************************************************************/
	
	/* sound section is almost identical to Bubble Bobble, YM2203 instead of YM3526 */
	
	static MemoryReadAddress readmem_sound[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0x87ff, MRA_RAM ),
		new MemoryReadAddress( 0x9000, 0x9000, YM2203_status_port_0_r ),
		new MemoryReadAddress( 0xa000, 0xa000, YM2203_status_port_1_r ),
		new MemoryReadAddress( 0xb000, 0xb000, soundlatch_r ),
		new MemoryReadAddress( 0xb001, 0xb001, MRA_NOP ),	/* ??? */
		new MemoryReadAddress( 0xe000, 0xefff, MRA_ROM ),	/* space for diagnostic ROM? */
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress writemem_sound[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0x8000, 0x87ff, MWA_RAM ),
		new MemoryWriteAddress( 0x9000, 0x9000, YM2203_control_port_0_w ),
		new MemoryWriteAddress( 0x9001, 0x9001, YM2203_write_port_0_w ),
		new MemoryWriteAddress( 0xa000, 0xa000, YM2203_control_port_1_w ),
		new MemoryWriteAddress( 0xa001, 0xa001, YM2203_write_port_1_w ),
		new MemoryWriteAddress( 0xb000, 0xb000, MWA_NOP ),	/* ??? */
		new MemoryWriteAddress( 0xb001, 0xb001, lkage_sh_nmi_enable_w ),
		new MemoryWriteAddress( 0xb002, 0xb002, lkage_sh_nmi_disable_w ),
		new MemoryWriteAddress( 0xe000, 0xefff, MWA_ROM ),	/* space for diagnostic ROM? */
		new MemoryWriteAddress( -1 )
	};
	
	/***************************************************************************/
	
	static InputPortPtr input_ports_lkage = new InputPortPtr(){ public void handler() { 
	PORT_START();       /* DSW1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x03, "10000" );/* unconfirmed */
		PORT_DIPSETTING(    0x02, "15000" );/* unconfirmed */
		PORT_DIPSETTING(    0x01, "20000" );/* unconfirmed */
		PORT_DIPSETTING(    0x00, "24000" );/* unconfirmed */
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Free_Play") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x18, "3" );	PORT_DIPSETTING(    0x10, "4" );	PORT_DIPSETTING(    0x08, "5" );	PORT_BITX(0,  0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "255", IP_KEY_NONE, IP_JOY_NONE );	PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Cocktail") );
	
		PORT_START();       /* DSW2 */
		PORT_DIPNAME( 0x0f, 0x00, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "9C_1C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "8C_1C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "7C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "6C_1C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_8C") );
		PORT_DIPNAME( 0xf0, 0x00, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "9C_1C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "8C_1C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "7C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "6C_1C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "1C_8C") );
	
		PORT_START();       /* DSW3 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x03, "Easiest" );/* unconfirmed */
		PORT_DIPSETTING(    0x02, "Easy" );   /* unconfirmed */
		PORT_DIPSETTING(    0x01, "Normal" ); /* unconfirmed */
		PORT_DIPSETTING(    0x00, "Hard" );   /* unconfirmed */
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, "Coinage Display" );	PORT_DIPSETTING(    0x10, "Coins/Credits" );	PORT_DIPSETTING(    0x00, "Insert Coin" );	PORT_DIPNAME( 0x20, 0x20, "Year Display" );	PORT_DIPSETTING(    0x00, "1985" );	PORT_DIPSETTING(    0x20, "MCMLXXXIV" );	PORT_BITX(    0x40, 0x40, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE );	PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "Coin Slots" );	PORT_DIPSETTING(    0x80, "A and B" );	PORT_DIPSETTING(    0x00, "A only" );
		PORT_START();       /* Service */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_COIN3 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_COIN1 );	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_COIN2 );	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout tile_layout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,4),
		4,
		new int[] { RGN_FRAC(1,4),RGN_FRAC(0,4),RGN_FRAC(3,4),RGN_FRAC(2,4) },
		new int[] { 7, 6, 5, 4, 3, 2, 1, 0 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8
	);
	
	static GfxLayout sprite_layout = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,4),
		4,
		new int[] { RGN_FRAC(1,4),RGN_FRAC(0,4),RGN_FRAC(3,4),RGN_FRAC(2,4) },
		new int[] { 7, 6, 5, 4, 3, 2, 1, 0,
				64+7, 64+6, 64+5, 64+4, 64+3, 64+2, 64+1, 64+0 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				128+0*8, 128+1*8, 128+2*8, 128+3*8, 128+4*8, 128+5*8, 128+6*8, 128+7*8 },
		32*8
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0x0000, tile_layout,  128, 3 ),
		new GfxDecodeInfo( REGION_GFX1, 0x0000, sprite_layout,  0, 8 ),
		new GfxDecodeInfo( -1 )
	};
	
	
	
	static void irqhandler(int irq)
	{
		cpu_set_irq_line(1,0,irq ? ASSERT_LINE : CLEAR_LINE);
	}
	
	static YM2203interface ym2203_interface = new YM2203interface
	(
		2,          /* 2 chips */
		4000000,    /* 4 MHz ? (hand tuned) */
		new int[] { YM2203_VOL(40,15), YM2203_VOL(40,15) },
		new ReadHandlerPtr[] { 0 },
		new ReadHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteYmHandlerPtr[] { irqhandler }
	);
	
	
	
	static MachineDriver machine_driver_lkage = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80 | CPU_16BIT_PORT,
				6000000,	/* ??? */
				readmem,writemem,readport,0,
				interrupt,1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				6000000,	/* ??? */
				readmem_sound,writemem_sound,null,null,
				ignore_interrupt,0	/* NMIs are triggered by the main CPU */
									/* IRQs are triggered by the YM2203 */
			),
			new MachineCPU(
				CPU_M68705,
				4000000/2,	/* ??? */
				m68705_readmem,m68705_writemem,null,null,
				ignore_interrupt,0
			)
		},
		60,DEFAULT_REAL_60HZ_VBLANK_DURATION,
		1, /* CPU slices */
		null, /* init machine */
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		176,176,
			/*
				there are actually 1024 colors in paletteram, however, we use a 100% correct
				reduced "virtual palette" to achieve some optimizations in the video driver.
			*/
		null,
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		lkage_vh_start,
		0,
		lkage_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203_interface
			)
		}
	);
	
	static MachineDriver machine_driver_lkageb = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80 | CPU_16BIT_PORT,
				6000000,	/* ??? */
				readmem,writemem,readport,0,
				interrupt,1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				6000000,	/* ??? */
				readmem_sound,writemem_sound,null,null,
				ignore_interrupt,0	/* NMIs are triggered by the main CPU */
									/* IRQs are triggered by the YM2203 */
			)
		},
		60,DEFAULT_REAL_60HZ_VBLANK_DURATION,
		1, /* CPU slices */
		null, /* init machine */
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		176,176,
			/*
				there are actually 1024 colors in paletteram, however, we use a 100% correct
				reduced "virtual palette" to achieve some optimizations in the video driver.
			*/
		null,
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		lkage_vh_start,
		0,
		lkage_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203_interface
			)
		}
	);
	
	
	
	static RomLoadPtr rom_lkage = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x14000, REGION_CPU1 );/* Z80 code (main CPU) */
		ROM_LOAD( "a54-01-1.37", 0x0000, 0x8000, 0x973da9c5 );	ROM_LOAD( "a54-02-1.38", 0x8000, 0x8000, 0x27b509da );
		ROM_REGION( 0x10000, REGION_CPU2 );/* Z80 code (sound CPU) */
		ROM_LOAD( "a54-04.54",   0x0000, 0x8000, 0x541faf9a );
		ROM_REGION( 0x10000, REGION_CPU3 );/* 68705 MCU code */
		ROM_LOAD( "a54-09.53",   0x0000, 0x0800, 0x0e8b8846 );
		ROM_REGION( 0x4000, REGION_USER1 );/* data */
		ROM_LOAD( "a54-03.51",   0x0000, 0x4000, 0x493e76d8 );
		ROM_REGION( 0x10000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "a54-05-1.84", 0x0000, 0x4000, 0x0033c06a );	ROM_LOAD( "a54-06-1.85", 0x4000, 0x4000, 0x9f04d9ad );	ROM_LOAD( "a54-07-1.86", 0x8000, 0x4000, 0xb20561a4 );	ROM_LOAD( "a54-08-1.87", 0xc000, 0x4000, 0x3ff3b230 );
		ROM_REGION( 0x0200, REGION_PROMS );	ROM_LOAD( "a54-10.2",    0x0000, 0x0200, 0x17dfbd14 );/* unknown */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_lkageb = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* Z80 code (main CPU) */
		ROM_LOAD( "ic37_1",      0x0000, 0x8000, 0x05694f7b );	ROM_LOAD( "ic38_2",      0x8000, 0x8000, 0x22efe29e );
		ROM_REGION( 0x10000, REGION_CPU2 );/* Z80 code (sound CPU) */
		ROM_LOAD( "a54-04.54",   0x0000, 0x8000, 0x541faf9a );
		ROM_REGION( 0x4000, REGION_USER1 );/* data */
		ROM_LOAD( "a54-03.51",   0x0000, 0x4000, 0x493e76d8 );
		ROM_REGION( 0x10000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "ic93_5",      0x0000, 0x4000, 0x76753e52 );	ROM_LOAD( "ic94_6",      0x4000, 0x4000, 0xf33c015c );	ROM_LOAD( "ic95_7",      0x8000, 0x4000, 0x0e02c2e8 );	ROM_LOAD( "ic96_8",      0xc000, 0x4000, 0x4ef5f073 );
		ROM_REGION( 0x0200, REGION_PROMS );	ROM_LOAD( "a54-10.2",    0x0000, 0x0200, 0x17dfbd14 );/* unknown */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_lkageb2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* Z80 code (main CPU) */
		ROM_LOAD( "lok.a",       0x0000, 0x8000, 0x866df793 );	ROM_LOAD( "lok.b",       0x8000, 0x8000, 0xfba9400f );
		ROM_REGION( 0x10000, REGION_CPU2 );/* Z80 code (sound CPU) */
		ROM_LOAD( "a54-04.54",   0x0000, 0x8000, 0x541faf9a );
		ROM_REGION( 0x4000, REGION_USER1 );/* data */
		ROM_LOAD( "a54-03.51",   0x0000, 0x4000, 0x493e76d8 );
		ROM_REGION( 0x10000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "ic93_5",      0x0000, 0x4000, 0x76753e52 );	ROM_LOAD( "ic94_6",      0x4000, 0x4000, 0xf33c015c );	ROM_LOAD( "ic95_7",      0x8000, 0x4000, 0x0e02c2e8 );	ROM_LOAD( "ic96_8",      0xc000, 0x4000, 0x4ef5f073 );
		ROM_REGION( 0x0200, REGION_PROMS );	ROM_LOAD( "a54-10.2",    0x0000, 0x0200, 0x17dfbd14 );/* unknown */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_lkageb3 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* Z80 code (main CPU) */
		ROM_LOAD( "z1.bin",      0x0000, 0x8000, 0x60cac488 );	ROM_LOAD( "z2.bin",      0x8000, 0x8000, 0x22c95f17 );
		ROM_REGION( 0x10000, REGION_CPU2 );/* Z80 code (sound CPU) */
		ROM_LOAD( "a54-04.54",   0x0000, 0x8000, 0x541faf9a );
		ROM_REGION( 0x4000, REGION_USER1 );/* data */
		ROM_LOAD( "a54-03.51",   0x0000, 0x4000, 0x493e76d8 );
		ROM_REGION( 0x10000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "ic93_5",      0x0000, 0x4000, 0x76753e52 );	ROM_LOAD( "ic94_6",      0x4000, 0x4000, 0xf33c015c );	ROM_LOAD( "ic95_7",      0x8000, 0x4000, 0x0e02c2e8 );	ROM_LOAD( "ic96_8",      0xc000, 0x4000, 0x4ef5f073 );
		ROM_REGION( 0x0200, REGION_PROMS );	ROM_LOAD( "a54-10.2",    0x0000, 0x0200, 0x17dfbd14 );/* unknown */
	ROM_END(); }}; 
	
	
	
	public static ReadHandlerPtr fake_mcu_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return 0;
	} };
	
	public static ReadHandlerPtr fake_status_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return 3;
	} };
	
	public static InitDriverPtr init_lkageb = new InitDriverPtr() { public void handler() 
	{
		install_mem_read_handler(0,0xf062,0xf062,fake_mcu_r);
		install_mem_read_handler(0,0xf087,0xf087,fake_status_r);
	} };
	
	
	public static GameDriver driver_lkage	   = new GameDriver("1984"	,"lkage"	,"lkage.java"	,rom_lkage,null	,machine_driver_lkage	,input_ports_lkage	,null	,ROT0	,	"Taito Corporation", "The Legend of Kage" )
	public static GameDriver driver_lkageb	   = new GameDriver("1984"	,"lkageb"	,"lkage.java"	,rom_lkageb,driver_lkage	,machine_driver_lkageb	,input_ports_lkage	,init_lkageb	,ROT0	,	"bootleg", "The Legend of Kage (bootleg set 1)", GAME_NOT_WORKING )
	public static GameDriver driver_lkageb2	   = new GameDriver("1984"	,"lkageb2"	,"lkage.java"	,rom_lkageb2,driver_lkage	,machine_driver_lkageb	,input_ports_lkage	,null	,ROT0	,	"bootleg", "The Legend of Kage (bootleg set 2)" )
	public static GameDriver driver_lkageb3	   = new GameDriver("1984"	,"lkageb3"	,"lkage.java"	,rom_lkageb3,driver_lkage	,machine_driver_lkageb	,input_ports_lkage	,null	,ROT0	,	"bootleg", "The Legend of Kage (bootleg set 3)" )
}
