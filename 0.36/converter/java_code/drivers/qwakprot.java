/***************************************************************************

  Qwak (prototype) driver.

  This driver is based *extremely* loosely on the Centipede driver.

  The following memory map is pure speculation:

  0000-01FF     R/W		RAM
  0200-025F     R/W		RAM?  ER2055 NOVRAM maybe?
  0300-03FF     R/W		RAM
  0400-07BF		R/W		Video RAM
  07C0-07FF		R/W		Sprite RAM
  1000			W		???
  2000			W		???
  2001			W		???
  2003          W		Start LED 1
  2004          W		Start LED 2
  3000			R		$40 = !UP			$80 = unused?
  3001			R		$40 = !DOWN			$80 = ???
  3002			R		$40 = !LEFT			$80 = ???
  3003			R		$40 = !RIGHT		$80 = unused?
  3004			R		$40 = !START1		$80 = ???
  3005			R		$40 = !START2		$80 = !COIN
  3006			R		$40 = !BUTTON1		$80 = !COIN
  3007			R		$40 = unused?		$80 = !COIN
  4000          R		???
  6000-600F		R/W		Pokey 1
  7000-700F		R/W		Pokey 2
  8000-BFFF		R		ROM

  TODO:
	- fix colors
	- coins seem to count twice instead of once?
	- find DIP switches (should be at $4000, I would think)
	- figure out what $1000, $2000, and $2001 are used for
	- figure out exactly what the unknown bits in the $3000 area do


If you have any questions about how this driver works, don't hesitate to
ask.  - Mike Balfour (mab22@po.cwru.edu)
***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;

public class qwakprot
{
	
	void qwakprot_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh);
	void qwakprot_paletteram_w (int offset, int data);
	
	
	public static WriteHandlerPtr qwakprot_led_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		osd_led_w(offset,~data >> 7);
	} };
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x01ff, MRA_RAM ),
		new MemoryReadAddress( 0x0200, 0x025f, MRA_RAM ),
		new MemoryReadAddress( 0x0300, 0x03ff, MRA_RAM ),
		new MemoryReadAddress( 0x0400, 0x07ff, MRA_RAM ),
		new MemoryReadAddress( 0x3000, 0x3000, input_port_0_r ),
		new MemoryReadAddress( 0x3001, 0x3001, input_port_1_r ),
		new MemoryReadAddress( 0x3002, 0x3002, input_port_2_r ),
		new MemoryReadAddress( 0x3003, 0x3003, input_port_3_r ),
		new MemoryReadAddress( 0x3004, 0x3004, input_port_4_r ),
		new MemoryReadAddress( 0x3005, 0x3005, input_port_5_r ),
		new MemoryReadAddress( 0x3006, 0x3006, input_port_6_r ),
		new MemoryReadAddress( 0x3007, 0x3007, input_port_7_r ),
		new MemoryReadAddress( 0x4000, 0x4000, input_port_8_r ),			/* just guessing */
		new MemoryReadAddress( 0x6000, 0x600f, pokey1_r ),
		new MemoryReadAddress( 0x7000, 0x700f, pokey2_r ),
		new MemoryReadAddress( 0x8000, 0xbfff, MRA_ROM ),
		new MemoryReadAddress( 0xf000, 0xffff, MRA_ROM ),	/* for the reset / interrupt vectors */
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x01ff, MWA_RAM ),
		new MemoryWriteAddress( 0x0200, 0x025f, MWA_RAM ),
		new MemoryWriteAddress( 0x0300, 0x03ff, MWA_RAM ),
		new MemoryWriteAddress( 0x0400, 0x07bf, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x07c0, 0x07ff, MWA_RAM, spriteram ),
		new MemoryWriteAddress( 0x1c00, 0x1c0f, qwakprot_paletteram_w, paletteram ), /* just guessing */
	//	new MemoryWriteAddress( 0x2000, 0x2001, coin_counter_w ),
		new MemoryWriteAddress( 0x2003, 0x2004, qwakprot_led_w ),
		new MemoryWriteAddress( 0x6000, 0x600f, pokey1_w ),
		new MemoryWriteAddress( 0x7000, 0x700f, pokey2_w ),
		new MemoryWriteAddress( 0x8000, 0xbfff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	
	static InputPortPtr input_ports_qwakprot = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_UP );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );		/* ??? */
	
		PORT_START();       /* IN1 */
		PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN );
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_VBLANK );		/* ??? */
	
		PORT_START();       /* IN2 */
		PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_VBLANK );		/* ??? */
	
		PORT_START();       /* IN3 */
		PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );		/* ??? */
	
		PORT_START();       /* IN4 */
		PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );		/* ??? */
	
		PORT_START();       /* IN5 */
		PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );
	
		PORT_START();       /* IN6 */
		PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_COIN2 );
	
		PORT_START();       /* IN7 */
		PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );		/* ??? */
		PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_COIN3 );
	
		PORT_START();       /* IN8 */
		PORT_DIPNAME( 0x01, 0x00, "DIP 1" );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, "DIP 2" );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, "DIP 3" );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, "DIP 4" );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, "DIP 5" );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, "DIP 6" );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, "DIP 7" );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, "DIP 8" );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		128,	/* 128 characters */
		4,	/* 4 bits per pixel */
		new int[] { 0x3000*8, 0x2000*8, 0x1000*8, 0 },	/* the four bitplanes are separated */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		16*8	/* every char takes 8 consecutive bytes, then skip 8 */
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		8,16,	/* 16*8 sprites */
		128,	/* 128 sprites */
		4,	/* 4 bits per pixel */
		new int[] { 0x3000*8, 0x2000*8, 0x1000*8, 0 },	/* the four bitplanes are separated */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		16*8	/* every sprite takes 16 consecutive bytes */
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0x0800, charlayout,   0, 1 ),
		new GfxDecodeInfo( REGION_GFX1, 0x0808, charlayout,   0, 1 ),
		new GfxDecodeInfo( REGION_GFX1, 0x0000, spritelayout, 0, 1 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static struct POKEYinterface pokey_interface =
	{
		2,	/* 2 chips */
		1500000,	/* 1.5 MHz??? */
		{ 50, 50 },
		/* The 8 pot handlers */
		{ 0, 0 },
		{ 0, 0 },
		{ 0, 0 },
		{ 0, 0 },
		{ 0, 0 },
		{ 0, 0 },
		{ 0, 0 },
		{ 0, 0 },
		/* The allpot handler */
		{ 0, 0 },
	};
	
	
	static MachineDriver machine_driver_qwakprot = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6502,
				12096000/8,	/* 1.512 Mhz?? */
				readmem,writemem,null,null,
				interrupt,4
			)
		},
		60, 1460,	/* frames per second, vblank duration??? */
		1,	/* single CPU, no need for interleaving */
		null,
	
		/* video hardware */
		32*8, 32*8, { 0*8, 32*8-1, 0*8, 30*8-1 },
		gfxdecodeinfo,
		16, 16,
		null,
	
		VIDEO_TYPE_RASTER|VIDEO_SUPPORTS_DIRTY|VIDEO_MODIFIES_PALETTE,
		null,
		generic_vh_start,
		generic_vh_stop,
		qwakprot_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_POKEY,
				pokey_interface
			)
		}
	);
	
	
	/***************************************************************************
	
	  Game ROMs
	
	***************************************************************************/
	
	static RomLoadPtr rom_qwakprot = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "qwak8000.bin", 0x8000, 0x1000, 0x4d002d8a );
		ROM_LOAD( "qwak9000.bin", 0x9000, 0x1000, 0xe0c78fd7 );
		ROM_LOAD( "qwaka000.bin", 0xa000, 0x1000, 0xe5770fc9 );
		ROM_LOAD( "qwakb000.bin", 0xb000, 0x1000, 0x90771cc0 );
		ROM_RELOAD(               0xf000, 0x1000 );/* for the reset and interrupt vectors */
	
		ROM_REGION( 0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "qwakgfx0.bin", 0x0000, 0x1000, 0xbed2c067 );
		ROM_LOAD( "qwakgfx1.bin", 0x1000, 0x1000, 0x73a31d28 );
		ROM_LOAD( "qwakgfx2.bin", 0x2000, 0x1000, 0x07fd9e80 );
		ROM_LOAD( "qwakgfx3.bin", 0x3000, 0x1000, 0xe8416f2b );
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_qwakprot	   = new GameDriver("1982"	,"qwakprot"	,"qwakprot.java"	,rom_qwakprot,null	,machine_driver_qwakprot	,input_ports_qwakprot	,null	,ROT270	,	"Atari", "Qwak (prototype)", GAME_NO_COCKTAIL )
}
