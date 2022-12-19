/***************************************************************************

Meteoroids Memory Map

driver by Zsolt Vasvari


0000-3fff   R	ROM
4000-43ff	R/W	RAM
7000-7002	R   input ports 0-2
7000-700f	  W most of the locations are unknown, but
7000		  W sound command
7001	      W sound CPU IRQ trigger on bit 3 falling edge
700e		  W main CPU interrupt enable (it uses RST7.5)
8000-83ff   R/W bit 0-7 of character code
9000-93ff   R/W attributes RAM
				bit 0   -  bit 8 of character code
				bit 1-3 -  unused
				bit 4-6 -  color
				bit 7   -  unused
a000-a3ff	R/W X/Y scroll position of each character (can be scrolled up
				to 7 pixels in each direction)


***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class meteor
{
	
	
	extern UBytePtr meteor_scrollram;
	
	
	
	public static InterruptPtr meteor_interrupt = new InterruptPtr() { public int handler() 
	{
		return I8085_RST75;
	} };
	
	
	static int meteor_SN76496_latch;
	static int meteor_SN76496_select;
	
	public static WriteHandlerPtr meteor_SN76496_latch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		meteor_SN76496_latch = data;
	} };
	
	public static ReadHandlerPtr meteor_SN76496_select_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return meteor_SN76496_select;
	} };
	
	public static WriteHandlerPtr meteor_SN76496_select_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	    meteor_SN76496_select = data;
	
		if (~data & 0x40)  SN76496_0_w(0, meteor_SN76496_latch);
		if (~data & 0x20)  SN76496_1_w(0, meteor_SN76496_latch);
		if (~data & 0x10)  SN76496_2_w(0, meteor_SN76496_latch);
	} };
	
	public static ReadHandlerPtr meteor_t0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		/* SN76496 status according to Al - not supported by MAME?? */
		return rand() & 1;
	} };
	
	
	static int meteor_soundtrigger;
	
	public static WriteHandlerPtr meteor_soundtrigger_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((meteor_soundtrigger & 0x08) && (~data & 0x08))
		{
			cpu_cause_interrupt(1, I8039_EXT_INT);
		}
	
		meteor_soundtrigger = data;
	} };
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new MemoryReadAddress( 0x4000, 0x43ff, MRA_RAM ),
		new MemoryReadAddress( 0x7000, 0x7000, input_port_0_r ),
		new MemoryReadAddress( 0x7001, 0x7001, input_port_1_r ),
		new MemoryReadAddress( 0x7002, 0x7002, input_port_2_r ),
		new MemoryReadAddress( 0x8000, 0x83ff, MRA_RAM ),
		new MemoryReadAddress( 0x9000, 0x93ff, MRA_RAM ),
		new MemoryReadAddress( 0xa000, 0xa3ff, MRA_RAM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x3fff, MWA_ROM ),
		new MemoryWriteAddress( 0x4000, 0x43ff, MWA_RAM ),
		new MemoryWriteAddress( 0x7000, 0x7000, soundlatch_w ),
		new MemoryWriteAddress( 0x7001, 0x7001, meteor_soundtrigger_w ),
		new MemoryWriteAddress( 0x700e, 0x700e, interrupt_enable_w ),
		new MemoryWriteAddress( 0x700f, 0x700f, MWA_NOP ),
		new MemoryWriteAddress( 0x8000, 0x83ff, MWA_RAM, videoram, videoram_size ),
		new MemoryWriteAddress( 0x9000, 0x93ff, MWA_RAM, colorram ),
		new MemoryWriteAddress( 0xa000, 0xa3ff, MWA_RAM, meteor_scrollram ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x07ff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x07ff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static IOReadPort sound_readport[] =
	{
		new IOReadPort( I8039_bus, I8039_bus, soundlatch_r ),
		new IOReadPort( I8039_p2,  I8039_p2,  meteor_SN76496_select_r ),
		new IOReadPort( I8039_t0,  I8039_t0,  meteor_t0_r ),
		new IOReadPort( -1 )	/* end of table */
	};
	
	static IOWritePort sound_writeport[] =
	{
		new IOWritePort( I8039_p1,  I8039_p1, meteor_SN76496_latch_w ),
		new IOWritePort( I8039_p2,  I8039_p2, meteor_SN76496_select_w ),
		new IOWritePort( -1 )	/* end of table */
	};
	
	
	static InputPortPtr input_ports_meteor = new InputPortPtr(){ public void handler() { 
	PORT_START();       /* DSW */
		PORT_DIPNAME( 0x03, 0x02, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x18, 0x08, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "2" );	PORT_DIPSETTING(    0x08, "3" );	PORT_DIPSETTING(    0x10, "4" );	PORT_DIPSETTING(    0x18, "5" );	PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );	/* probably unused */
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );  /* probably unused */
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START();       /* IN0 */
		PORT_BIT ( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY );	PORT_BIT ( 0x02, IP_ACTIVE_LOW, IPT_BUTTON1 );	PORT_BIT ( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT ( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT ( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 );	PORT_BIT ( 0x20, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY );
		PORT_START();       /* IN1 */
		PORT_BITX( 0x08, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
		PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_VBLANK );INPUT_PORTS_END(); }}; 
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,    /* 8*8 chars */
		512,    /* 512 characters */
		3,      /* 3 bits per pixel */
		new int[] { 2*512*8*8, 512*8*8, 0 },  /* The bitplanes are seperate */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7},
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8},
		8*8     /* every char takes 8 consecutive bytes */
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,   0, 8 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	/* 1-bit RGB palette */
	static unsigned char palette[] =
	{
		0x00, 0x00, 0x00,
		0xff, 0x00, 0x00,
		0x00, 0xff, 0x00,
		0xff, 0xff, 0x00,
		0x00, 0x00, 0xff,
		0xff, 0x00, 0xff,
		0x00, 0xff, 0xff,
		0xff, 0xff, 0xff
	};
	static unsigned short colortable[] =
	{
		0, 1, 2, 3, 4, 5, 6, 7,
		0, 2, 3, 4, 5, 6, 7, 0,	 /* not sure about these, but they are only used */
		0, 3, 4, 5, 6, 7, 0, 1,  /* to change the text color. During the game,   */
		0, 4, 5, 6, 7, 0, 1, 2,  /* only color 0 is used, which is correct.      */
		0, 5, 6, 7, 0, 1, 2, 3,
		0, 6, 7, 0, 1, 2, 3, 4,
		0, 7, 0, 1, 2, 3, 4, 5,
		0, 0, 1, 2, 3, 4, 5, 6,
	};
	static void init_palette(UBytePtr game_palette, unsigned short *game_colortable,const UBytePtr color_prom)
	{
		memcpy(game_palette,palette,sizeof(palette));
		memcpy(game_colortable,colortable,sizeof(colortable));
	}
	
	
	static SN76496interface sn76496_interface = new SN76496interface
	(
		3,		/* 3 chips */
		new int[] { 2000000, 2000000, 2000000 },	/* 8 MHz / 4 ?*/
		new int[] { 100, 100, 100 }
	);
	
	
	static MachineDriver machine_driver_meteor = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_8085A,
				4000000,        /* 4.00 MHz??? */
				readmem,writemem,null,null,
				meteor_interrupt,1
			),
			new MachineCPU(
	            CPU_I8035 | CPU_AUDIO_CPU,
	            6144000/8,		/* divisor ??? */
				sound_readmem,sound_writemem,sound_readport,sound_writeport,
	            ignore_interrupt,0  /* IRQ's are triggered by the main CPU */
	        )
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,  /* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 0*8, 28*8-1 ),
		gfxdecodeinfo,
		sizeof(palette) / sizeof(palette[null]) / 3, sizeof(colortable) / sizeof(colortable[null]),
		init_palette,
	
		VIDEO_TYPE_RASTER,
		null,
		generic_bitmapped_vh_start,
		generic_bitmapped_vh_stop,
		meteor_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_SN76496,
				sn76496_interface
			)
		}
	);
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	static RomLoadPtr rom_meteor = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );      /* 64k for code */
		ROM_LOAD( "vm1", 	      0x0000, 0x0800, 0x894fe9b1 );	ROM_LOAD( "vm2", 	      0x0800, 0x0800, 0x28685a68 );	ROM_LOAD( "vm3", 	      0x1000, 0x0800, 0xc88fb12a );	ROM_LOAD( "vm4", 	      0x1800, 0x0800, 0xc5b073b9 );							/*0x2000- 0x27ff empty */
		ROM_LOAD( "vm6", 	      0x2800, 0x0800, 0x9969ec43 );	ROM_LOAD( "vm7", 	      0x3000, 0x0800, 0x39f43ac2 );	ROM_LOAD( "vm8", 	      0x3800, 0x0800, 0xa0508de3 );
		ROM_REGION( 0x1000, REGION_CPU2 );	/* sound MCU */
		ROM_LOAD( "vm5", 	      0x0000, 0x0800, 0xb14ccd57 );
		ROM_REGION( 0x3000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "rm1v",         0x0000, 0x0800, 0xd621fe96 );	ROM_LOAD( "rm2v",         0x0800, 0x0800, 0xb3981251 );	ROM_LOAD( "gm1v",         0x1000, 0x0800, 0xd44617e8 );	ROM_LOAD( "gm2v",         0x1800, 0x0800, 0x0997d945 );	ROM_LOAD( "bm1v",         0x2000, 0x0800, 0xcc97c890 );	ROM_LOAD( "bm2v",         0x2800, 0x0800, 0x2858cf5c );ROM_END(); }}; 
	
	
	
	public static GameDriver driver_meteor	   = new GameDriver("1981"	,"meteor"	,"meteor.java"	,rom_meteor,null	,machine_driver_meteor	,input_ports_meteor	,null	,ROT270	,	"Venture Line", "Meteoroids" )
}
