/***************************************************************************

Circus memory map

driver by Mike Coates

0000-00FF Base Page RAM
0100-01FF Stack RAM
1000-1FFF ROM
2000      Clown Verticle Position
3000      Clown Horizontal Position
4000-43FF Video RAM
8000      Clown Rotation and Audio Controls
F000-FFF7 ROM
FFF8-FFFF Interrupt and Reset Vectors

A000      Control Switches
C000      Option Switches
D000      Paddle Position and Interrupt Reset

  CHANGES:
  MAB 09 MAR 99 - changed overlay support to use artwork functions

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;

public class circus
{
	
	
	
	
	
	static int circus_interrupt;
	
	static int ripcord_IN2_r (int offset)
	(
		circus_interrupt ++;
		if (errorlog) fprintf (errorlog, "circus_int: %02x\n", circus_interrupt);
		return readinputport (2);
	)
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x01ff, MRA_RAM ),
		new MemoryReadAddress( 0x1000, 0x1fff, MRA_ROM ),
		new MemoryReadAddress( 0x4000, 0x43ff, MRA_RAM ),
		new MemoryReadAddress( 0xa000, 0xa000, input_port_0_r ),
		new MemoryReadAddress( 0xc000, 0xc000, input_port_1_r ), /* DSW */
	//	new MemoryReadAddress( 0xd000, 0xd000, input_port_2_r ),
		new MemoryReadAddress( 0xd000, 0xd000, ripcord_IN2_r ),
		new MemoryReadAddress( 0xf000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x01ff, MWA_RAM ),
		new MemoryWriteAddress( 0x1000, 0x1fff, MWA_ROM ),
		new MemoryWriteAddress( 0x2000, 0x2000, circus_clown_x_w ),
		new MemoryWriteAddress( 0x3000, 0x3000, circus_clown_y_w ),
		new MemoryWriteAddress( 0x4000, 0x43ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x8000, 0x8000, circus_clown_z_w ),
		new MemoryWriteAddress( 0xf000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	static InputPortPtr input_ports_circus = new InputPortPtr(){ public void handler() { 
		PORT_START();  /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_BIT( 0x7c, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_COIN1 );
	
		PORT_START();       /* Dip Switch */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_DIPSETTING(    0x02, "7" );
		PORT_DIPSETTING(    0x03, "9" );
		PORT_DIPNAME( 0x0c, 0x04, DEF_STR( "Coinage") );
	//	PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x10, 0x00, "Top Score" );
		PORT_DIPSETTING(    0x10, "Credit Awarded" );
		PORT_DIPSETTING(    0x00, "No Award" );
		PORT_DIPNAME( 0x20, 0x00, "Bonus" );
		PORT_DIPSETTING(    0x00, "Single Line" );
		PORT_DIPSETTING(    0x20, "Super Bonus" );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_VBLANK );
	
		PORT_START();       /* IN2 - paddle */
		PORT_ANALOG( 0xff, 115, IPT_PADDLE, 30, 10, 64, 167 );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_robotbwl = new InputPortPtr(){ public void handler() { 
		PORT_START();  /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BITX(0x04, IP_ACTIVE_HIGH, 0,"Hook Right", KEYCODE_X, 0 );
		PORT_BITX(0x08, IP_ACTIVE_HIGH, 0,"Hook Left", KEYCODE_Z, 0 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON2 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_COIN1 );
	
		PORT_START();       /* Dip Switch */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, "Beer Frame" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x18, 0x08, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, "1 Coin - 2 Players game" );
	//	PORT_DIPSETTING(    0x18, "1 Coin - 2 Players game" );
		PORT_DIPNAME( 0x60, 0x00, "Bowl Timer" );
		PORT_DIPSETTING(    0x00, "3 seconds" );
		PORT_DIPSETTING(    0x20, "5 seconds" );
		PORT_DIPSETTING(    0x40, "7 seconds" );
		PORT_DIPSETTING(    0x60, "9 seconds" );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_VBLANK );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_crash = new InputPortPtr(){ public void handler() { 
		PORT_START();  /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_4WAY );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_4WAY );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_4WAY );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_COIN1 );
	
		PORT_START();       /* Dip Switch */
		PORT_DIPNAME( 0x03, 0x01, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "2" );
		PORT_DIPSETTING(    0x01, "3" );
		PORT_DIPSETTING(    0x02, "4" );
		PORT_DIPSETTING(    0x03, "5" );
		PORT_DIPNAME( 0x0C, 0x04, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x10, 0x00, "Top Score" );
		PORT_DIPSETTING(    0x00, "No Award" );
		PORT_DIPSETTING(    0x10, "Credit Awarded" );
		PORT_BIT( 0x60, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_VBLANK );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_ripcord = new InputPortPtr(){ public void handler() { 
		PORT_START();  /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_BUTTON2 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_COIN1 );
	
		PORT_START();       /* Dip Switch */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_DIPSETTING(    0x02, "7" );
		PORT_DIPSETTING(    0x03, "9" );
		PORT_DIPNAME( 0x0c, 0x04, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x00, "2 Player - 1 Coin" );
		PORT_DIPSETTING(    0x04, "1 Player - 1 Coin" );
		PORT_DIPSETTING(    0x08, "1 Player - 2 Coin" );
	//	PORT_DIPSETTING(    0x0c, "1 Player - ? Coin" );
		PORT_DIPNAME( 0x10, 0x10, "Top Score" );
		PORT_DIPSETTING(    0x10, "Credit Awarded" );
		PORT_DIPSETTING(    0x00, "No Award" );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_VBLANK );
	
		PORT_START();       /* IN2 - paddle */
		PORT_ANALOG( 0xff, 115, IPT_PADDLE, 30, 10, 64, 167 );
	INPUT_PORTS_END(); }}; 
	
	static unsigned char palette[] =
	{
		0x00,0x00,0x00, /* BLACK */
		0xff,0xff,0xff, /* WHITE */
	};
	
	#define ARTWORK_COLORS 254
	
	static void init_palette(unsigned char *game_palette, unsigned short *game_colortable,const unsigned char *color_prom)
	{
		memcpy(game_palette,palette,sizeof(palette));
	}
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,    /* 8*8 characters */
		256,    /* 256 characters */
		1,              /* 1 bit per pixel */
		new int[] { 0 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8     /* every char takes 8 consecutive bytes */
	);
	
	static GfxLayout clownlayout = new GfxLayout
	(
		16,16,  /* 16*16 characters */
		16,             /* 16 characters */
		1,              /* 1 bit per pixel */
		new int[] { 0 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7,
		  16*8, 16*8+1, 16*8+2, 16*8+3, 16*8+4, 16*8+5, 16*8+6, 16*8+7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
		  8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		16*16   /* every char takes 64 consecutive bytes */
	);
	
	static GfxLayout robotlayout = new GfxLayout
	(
		8,8,  /* 16*16 characters */
		1,      /* 1 character */
		1,      /* 1 bit per pixel */
		new int[] { 0 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,  0, 1 ),
		new GfxDecodeInfo( REGION_GFX2, 0, clownlayout, 0, 1 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo robotbowl_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,  0, 1 ),
		new GfxDecodeInfo( REGION_GFX2, 0, robotlayout, 0, 1 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	/***************************************************************************
	  Machine drivers
	***************************************************************************/
	
	public static InitDriverPtr init_robotbwl = new InitDriverPtr() { public void handler() 
	{
		int i;
	
		/* PROM is reversed, fix it! */
	
		for (i = 0;i < memory_region_length(REGION_GFX2);i++)
			memory_region(REGION_GFX2)[i] ^= 0xFF;
	} };
	
	static int ripcord_interrupt (void)
	{
		circus_interrupt = 0;
		if (1)
			return ignore_interrupt();
		else
			return interrupt();
	}
	
	static DACinterface dac_interface = new DACinterface
	(
		1,
		new int[] { 255, 255 }
	);
	
	static MachineDriver machine_driver_circus = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6502,
				11289000/16,	/* 705.562kHz */
				readmem,writemem,null,null,
				interrupt,1
			)
		},
		57, 3500,	/* frames per second, vblank duration (complete guess) */
		1,      /* single CPU, no need for interleaving */
		null,
	
		/* video hardware */
		32*8, 32*8, { 0*8, 31*8-1, 0*8, 32*8-1 },
		gfxdecodeinfo,
		ARTWORK_COLORS,ARTWORK_COLORS,		/* Leave extra colors for the overlay */
		init_palette,
	
		VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
		null,
		circus_vh_start,
		circus_vh_stop,
		circus_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_DAC,
				dac_interface
			)
		}
	);
	
	
	static MachineDriver machine_driver_robotbwl = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6502,
				11289000/16,	/* 705.562kHz */
				readmem,writemem,null,null,
				interrupt,1
			)
		},
		57, 3500,	/* frames per second, vblank duration (complete guess) */
		1,      /* single CPU, no need for interleaving */
		null,
	
		/* video hardware */
		32*8, 32*8, { 0*8, 31*8-1, 0*8, 32*8-1 },
		robotbowl_gfxdecodeinfo,
		2, 2,
		init_palette,
	
		VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY,
		null,
		generic_vh_start,
		generic_vh_stop,
		robotbowl_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_DAC,
				dac_interface
			)
		}
	);
	
	static MachineDriver machine_driver_crash = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6502,
				11289000/16,	/* 705.562kHz */
				readmem,writemem,null,null,
				interrupt,2
			)
		},
		57, 3500,	/* frames per second, vblank duration (complete guess) */
		1,      /* single CPU, no need for interleaving */
		null,
	
		/* video hardware */
		32*8, 32*8, { 0*8, 31*8-1, 0*8, 32*8-1 },
		gfxdecodeinfo,
		2, 2,
		init_palette,
	
		VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY,
		null,
		generic_vh_start,
		generic_vh_stop,
		crash_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_DAC,
				dac_interface
			)
		}
	);
	
	static MachineDriver machine_driver_ripcord = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6502,
				705562,        /* 11.289MHz / 16 */
				readmem,writemem,null,null,
				ripcord_interrupt,1
			)
		},
		57, 3500,	/* frames per second, vblank duration (complete guess) */
		1,      /* single CPU, no need for interleaving */
		null,
	
		/* video hardware */
		32*8, 32*8, { 0*8, 31*8-1, 0*8, 32*8-1 },
		gfxdecodeinfo,
		2, 2,
		init_palette,
	
		VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY,
		null,
		generic_vh_start,
		generic_vh_stop,
		crash_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_DAC,
				dac_interface
			)
		}
	);
	
	
	
	static RomLoadPtr rom_circus = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "circus.1a",    0x1000, 0x0200, 0x7654ea75 );/* Code */
		ROM_LOAD( "circus.2a",    0x1200, 0x0200, 0xb8acdbc5 );
		ROM_LOAD( "circus.3a",    0x1400, 0x0200, 0x901dfff6 );
		ROM_LOAD( "circus.5a",    0x1600, 0x0200, 0x9dfdae38 );
		ROM_LOAD( "circus.6a",    0x1800, 0x0200, 0xc8681cf6 );
		ROM_LOAD( "circus.7a",    0x1a00, 0x0200, 0x585f633e );
		ROM_LOAD( "circus.8a",    0x1c00, 0x0200, 0x69cc409f );
		ROM_LOAD( "circus.9a",    0x1e00, 0x0200, 0xaff835eb );
		ROM_RELOAD(               0xfe00, 0x0200 );/* for the reset and interrupt vectors */
	
		ROM_REGION( 0x0800, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "circus.4c",    0x0000, 0x0200, 0x6efc315a );/* Character Set */
		ROM_LOAD( "circus.3c",    0x0200, 0x0200, 0x30d72ef5 );
		ROM_LOAD( "circus.2c",    0x0400, 0x0200, 0x361da7ee );
		ROM_LOAD( "circus.1c",    0x0600, 0x0200, 0x1f954bb3 );
	
		ROM_REGION( 0x0200, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "circus.14d",   0x0000, 0x0200, 0x2fde3930 );/* Clown */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_robotbwl = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "robotbwl.1a",  0xf000, 0x0200, 0xdf387a0b );/* Code */
		ROM_LOAD( "robotbwl.2a",  0xf200, 0x0200, 0xc948274d );
		ROM_LOAD( "robotbwl.3a",  0xf400, 0x0200, 0x8fdb3ec5 );
		ROM_LOAD( "robotbwl.5a",  0xf600, 0x0200, 0xba9a6929 );
		ROM_LOAD( "robotbwl.6a",  0xf800, 0x0200, 0x16fd8480 );
		ROM_LOAD( "robotbwl.7a",  0xfa00, 0x0200, 0x4cadbf06 );
		ROM_LOAD( "robotbwl.8a",  0xfc00, 0x0200, 0xbc809ed3 );
		ROM_LOAD( "robotbwl.9a",  0xfe00, 0x0200, 0x07487e27 );
	
		ROM_REGION( 0x0800, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "robotbwl.4c",  0x0000, 0x0200, 0xa5f7acb9 );/* Character Set */
		ROM_LOAD( "robotbwl.3c",  0x0200, 0x0200, 0xd5380c9b );
		ROM_LOAD( "robotbwl.2c",  0x0400, 0x0200, 0x47b3e39c );
		ROM_LOAD( "robotbwl.1c",  0x0600, 0x0200, 0xb2991e7e );
	
		ROM_REGION( 0x0020, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "robotbwl.14d", 0x0000, 0x0020, 0xa402ac06 );/* Ball */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_crash = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "crash.a1",     0x1000, 0x0200, 0xb9571203 );/* Code */
		ROM_LOAD( "crash.a2",     0x1200, 0x0200, 0xb4581a95 );
		ROM_LOAD( "crash.a3",     0x1400, 0x0200, 0x597555ae );
		ROM_LOAD( "crash.a4",     0x1600, 0x0200, 0x0a15d69f );
		ROM_LOAD( "crash.a5",     0x1800, 0x0200, 0xa9c7a328 );
		ROM_LOAD( "crash.a6",     0x1a00, 0x0200, 0xc7d62d27 );
		ROM_LOAD( "crash.a7",     0x1c00, 0x0200, 0x5e5af244 );
		ROM_LOAD( "crash.a8",     0x1e00, 0x0200, 0x3dc50839 );
		ROM_RELOAD(               0xfe00, 0x0200 );/* for the reset and interrupt vectors */
	
		ROM_REGION( 0x0800, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "crash.c4",     0x0000, 0x0200, 0xba16f9e8 );/* Character Set */
		ROM_LOAD( "crash.c3",     0x0200, 0x0200, 0x3c8f7560 );
		ROM_LOAD( "crash.c2",     0x0400, 0x0200, 0x38f3e4ed );
		ROM_LOAD( "crash.c1",     0x0600, 0x0200, 0xe9adf1e1 );
	
		ROM_REGION( 0x0200, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "crash.d14",    0x0000, 0x0200, 0x833f81e4 );/* Cars */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_ripcord = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "9027.1a",      0x1000, 0x0200, 0x56b8dc06 );/* Code */
		ROM_LOAD( "9028.2a",      0x1200, 0x0200, 0xa8a78a30 );
		ROM_LOAD( "9029.4a",      0x1400, 0x0200, 0xfc5c8e07 );
		ROM_LOAD( "9030.5a",      0x1600, 0x0200, 0xb496263c );
		ROM_LOAD( "9031.6a",      0x1800, 0x0200, 0xcdc7d46e );
		ROM_LOAD( "9032.7a",      0x1a00, 0x0200, 0xa6588bec );
		ROM_LOAD( "9033.8a",      0x1c00, 0x0200, 0xfd49b806 );
		ROM_LOAD( "9034.9a",      0x1e00, 0x0200, 0x7caf926d );
		ROM_RELOAD(               0xfe00, 0x0200 );/* for the reset and interrupt vectors */
	
		ROM_REGION( 0x0800, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "9026.5c",      0x0000, 0x0200, 0x06e7adbb );/* Character Set */
		ROM_LOAD( "9025.4c",      0x0200, 0x0200, 0x3129527e );
		ROM_LOAD( "9024.2c",      0x0400, 0x0200, 0xbcb88396 );
		ROM_LOAD( "9023.1c",      0x0600, 0x0200, 0x9f86ed5b );
	
		ROM_REGION( 0x0200, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "9035.14d",     0x0000, 0x0200, 0xc9979802 );
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_circus	   = new GameDriver("1977"	,"circus"	,"circus.java"	,rom_circus,null	,machine_driver_circus	,input_ports_circus	,null	,ROT0	,	"Exidy", "Circus", GAME_IMPERFECT_SOUND )
	public static GameDriver driver_robotbwl	   = new GameDriver("1977"	,"robotbwl"	,"circus.java"	,rom_robotbwl,null	,machine_driver_robotbwl	,input_ports_robotbwl	,init_robotbwl	,ROT0	,	"Exidy", "Robot Bowl", GAME_IMPERFECT_SOUND )
	public static GameDriver driver_crash	   = new GameDriver("1979"	,"crash"	,"circus.java"	,rom_crash,null	,machine_driver_crash	,input_ports_crash	,null	,ROT0	,	"Exidy", "Crash", GAME_IMPERFECT_SOUND )
	public static GameDriver driver_ripcord	   = new GameDriver("1977"	,"ripcord"	,"circus.java"	,rom_ripcord,null	,machine_driver_ripcord	,input_ports_ripcord	,null	,ROT0	,	"Exidy", "Rip Cord", GAME_NOT_WORKING )
}
