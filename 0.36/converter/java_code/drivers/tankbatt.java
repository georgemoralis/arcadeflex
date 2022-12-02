/***************************************************************************

Tank Battalion memory map (preliminary)

driver by Brad Oliver

$0000-$000f : bullet ram, first entry is player's bullet
$0010-$01ff : zero page  stack
$0200-$07ff : RAM
$0800-$0bff : videoram
$0c00-$0c1f : I/O

Read:
	$0c00-$0c03 : p1 joystick
	$0c04
	$0c07       : stop at grid self-test if bit 7 is low
	$0c0f 		: stop at first self-test if bit 7 is low

	$0c18		: Cabinet, null = table, 1 = upright
	$0c19-$0c1a	: Coinage, 00 = free play, 01 = 2 coin 1 credit, 10 = 1 coin 2 credits, 11 = 1 coin 1 credit
	$0c1b-$0c1c : Bonus, 00 = 10000, 01 = 15000, 10 = 20000, 11 = none
	$0c1d		: Tanks, null = 3, 1 = 2
	$0c1e-$0c1f : ??

Write:
	$0c00-$0c01 : p1/p2 start leds
	$0c02		: ?? written to at end of IRQ, either null or 1 - coin counter?
	$0c03		: ?? written to during IRQ if grid test is on
	$0c08		: ?? written to during IRQ if grid test is on
	$0c09		: Sound - coin ding
	$0c0a		: NMI enable (active low) ?? game only ??
	$0c0b		: Sound - background noise, null - low rumble, 1 - high rumble
	$0c0c		: Sound - player fire
	$0c0d		: Sound - explosion
	$0c0f		: NMI enable (active high) ?? demo only ??

	$0c10		: IRQ ack ??
	$0c18		: Watchdog ?? Not written to while game screen is up

$2000-$3fff : ROM

TODO:
	. Resistor values on the color prom need to be verified

Changes:
	28 Feb 98 LBO
		. Fixed the coin interrupts
		. Fixed the color issues, should be 100% if I guessed at the resistor values properly
		. Fixed the 2nd player cocktail joystick, had the polarity reversed
		. Hacked the sound sample triggers so they work better

Known issues:
	. The 'moving' tank rumble noise seems to keep playing a second too long
	. Sample support is all a crapshoot. I have no idea how it really works

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;

public class tankbatt
{
	
	
	static int tankbatt_nmi_enable; /* No need to init this - the game will set it on reset */
	static int tankbatt_sound_enable;
	
	void tankbatt_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom);
	void tankbatt_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh);
	
	public static WriteHandlerPtr tankbatt_led_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		osd_led_w(offset, data);
	} };
	
	int tankbatt_in0_r (int offset)
	{
		int val;
	
		val = readinputport(0);
		return ((val << (7-offset)) & 0x80);
	}
	
	int tankbatt_in1_r (int offset)
	{
		int val;
	
		val = readinputport(1);
		return ((val << (7-offset)) & 0x80);
	}
	
	int tankbatt_dsw_r (int offset)
	{
		int val;
	
		val = readinputport(2);
		return ((val << (7-offset)) & 0x80);
	}
	
	void tankbatt_interrupt_enable_w (int offset, int data)
	{
		tankbatt_nmi_enable = !data;
		tankbatt_sound_enable = !data;
		if (data != 0) cpu_clear_pending_interrupts(0);
		/* hack - turn off the engine noise if the normal game nmi's are disabled */
		if (data) sample_stop (2);
	//	interrupt_enable_w (offset, !data);
	}
	
	void tankbatt_demo_interrupt_enable_w (int offset, int data)
	{
		tankbatt_nmi_enable = data;
		if (data != 0) cpu_clear_pending_interrupts(0);
	//	interrupt_enable_w (offset, data);
	}
	
	void tankbatt_sh_expl_w (int offset, int data)
	{
		if (tankbatt_sound_enable)
			sample_start (1, 3, 0);
	}
	
	void tankbatt_sh_engine_w (int offset, int data)
	{
		if (tankbatt_sound_enable)
		{
			if (data)
				sample_start (2, 2, 1);
			else
				sample_start (2, 1, 1);
		}
		else sample_stop (2);
	}
	
	void tankbatt_sh_fire_w (int offset, int data)
	{
		if (tankbatt_sound_enable)
			sample_start (0, 0, 0);
	}
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x01ff, MRA_RAM ),
		new MemoryReadAddress( 0x0c00, 0x0c07, tankbatt_in0_r ),
		new MemoryReadAddress( 0x0c08, 0x0c0f, tankbatt_in1_r ),
		new MemoryReadAddress( 0x0c18, 0x0c1f, tankbatt_dsw_r ),
		new MemoryReadAddress( 0x0200, 0x0bff, MRA_RAM ),
		new MemoryReadAddress( 0x6000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0xf800, 0xffff, MRA_ROM ),	/* for the reset / interrupt vectors */
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0010, 0x01ff, MWA_RAM ),
		new MemoryWriteAddress( 0x0800, 0x0bff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x0000, 0x000f, MWA_RAM, tankbatt_bulletsram, tankbatt_bulletsram_size ),
		new MemoryWriteAddress( 0x0c18, 0x0c18, MWA_NOP ), /* watchdog ?? */
		new MemoryWriteAddress( 0x0c00, 0x0c01, tankbatt_led_w ),
		new MemoryWriteAddress( 0x0c0a, 0x0c0a, tankbatt_interrupt_enable_w ),
		new MemoryWriteAddress( 0x0c0b, 0x0c0b, tankbatt_sh_engine_w ),
		new MemoryWriteAddress( 0x0c0c, 0x0c0c, tankbatt_sh_fire_w ),
		new MemoryWriteAddress( 0x0c0d, 0x0c0d, tankbatt_sh_expl_w ),
		new MemoryWriteAddress( 0x0c0f, 0x0c0f, tankbatt_demo_interrupt_enable_w ),
		new MemoryWriteAddress( 0x0200, 0x07ff, MWA_RAM ),
		new MemoryWriteAddress( 0x2000, 0x3fff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	int tankbatt_interrupt (void)
	{
		if ((readinputport (0) & 0x60) == 0) return interrupt ();
		if (tankbatt_nmi_enable) return nmi_interrupt ();
		else return ignore_interrupt ();
	}
	
	static InputPortPtr input_ports_tankbatt = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT ( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY );
		PORT_BIT ( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY );
		PORT_BIT ( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY );
		PORT_BIT ( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY );
		PORT_BIT ( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT_IMPULSE( 0x60, IP_ACTIVE_LOW, IPT_COIN1, 2 );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_TILT );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT ( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT ( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT ( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT ( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT ( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT ( 0x20, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_START2 );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	
		PORT_START(); 	/* DSW */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
	 	PORT_DIPNAME( 0x06, 0x06, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x04, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x18, 0x08, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "10000" );
		PORT_DIPSETTING(    0x10, "15000" );
		PORT_DIPSETTING(    0x08, "20000" );
		PORT_DIPSETTING(    0x18, "None" );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x20, "2" );
		PORT_DIPSETTING(    0x00, "3" );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		256,	/* 256 characters */
		1,	/* 1 bit per pixel */
		new int[] { 0 },	/* only one bitplane */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8	/* every char takes 8 consecutive bytes */
	);
	
	static GfxLayout bulletlayout = new GfxLayout
	(
		/* there is no gfx ROM for this one, it is generated by the hardware */
		3,3,	/* 3*3 box */
		1,	/* just one */
		1,	/* 1 bit per pixel */
		new int[] { 8*8 },
		new int[] { 2, 2, 2 },   /* I "know" that this bit of the */
		{ 2, 2, 2 },   /* graphics ROMs is 1 */
		0	/* no use */
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,   0, 64 ),
		new GfxDecodeInfo( REGION_GFX1, 0, bulletlayout, 0, 64 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static const char *tankbatt_sample_names[] =
	{
		"*tankbatt",
		"fire.wav",
		"engine1.wav",
		"engine2.wav",
		"explode1.wav",
	    0	/* end of array */
	};
	
	static struct Samplesinterface samples_interface =
	{
		3,	/* 3 channels */
		25,	/* volume */
		tankbatt_sample_names
	};
	
	
	
	static MachineDriver machine_driver_tankbatt = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6502,
				1000000,	/* 1 Mhz ???? */
				readmem,writemem,null,null,
				tankbatt_interrupt,1
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		65, 128,
		tankbatt_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		generic_vh_start,
		generic_vh_stop,
		tankbatt_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_SAMPLES,
				samples_interface
			)
		}
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_tankbatt = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "tb1-1.bin",    0x6000, 0x0800, 0x278a0b8c );
		ROM_LOAD( "tb1-2.bin",    0x6800, 0x0800, 0xe0923370 );
		ROM_LOAD( "tb1-3.bin",    0x7000, 0x0800, 0x85005ea4 );
		ROM_LOAD( "tb1-4.bin",    0x7800, 0x0800, 0x3dfb5bcf );
		ROM_RELOAD(               0xf800, 0x0800 );/* for the reset and interrupt vectors */
	
		ROM_REGION( 0x0800, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "tb1-5.bin",    0x0000, 0x0800, 0xaabd4fb1 );
	
		ROM_REGION( 0x0100, REGION_PROMS );
		ROM_LOAD( "tankbatt.clr", 0x0000, 0x0100, 0x1150d613 );
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_tankbatt	   = new GameDriver("1980"	,"tankbatt"	,"tankbatt.java"	,rom_tankbatt,null	,machine_driver_tankbatt	,input_ports_tankbatt	,null	,ROT90	,	"Namco", "Tank Battalion" )
}
