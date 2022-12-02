/***************************************************************************

Ramtek Star Cruiser Driver

(no known issues)

Frank Palazzolo
palazzol@home.com

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;

public class starcrus
{
	
	/* included from vidhrdw/starcrus.c */
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x0fff, MRA_ROM ), /* Program ROM */
		new MemoryReadAddress( 0x1000, 0x10ff, MRA_RAM ), /* RAM */
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
	    new MemoryWriteAddress( 0x0000, 0x0fff, MWA_ROM ), /* Program ROM */
	    new MemoryWriteAddress( 0x1000, 0x10ff, MWA_RAM ), /* RAM */
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static IOReadPort readport[] =
	{
	    new IOReadPort( 0x00, 0x00, input_port_0_r ),
	    new IOReadPort( 0x01, 0x01, input_port_1_r ),
	    new IOReadPort( 0x02, 0x02, starcrus_coll_det_r ),
	    new IOReadPort( 0x03, 0x03, input_port_2_r ),
	    new IOReadPort( -1 )  /* end of table */
	};
	
	static IOWritePort writeport[] =
	{
	    new IOWritePort( 0x00, 0x00, starcrus_s1_x_w ),
	    new IOWritePort( 0x01, 0x01, starcrus_s1_y_w ),
	    new IOWritePort( 0x02, 0x02, starcrus_s2_x_w ),
	    new IOWritePort( 0x03, 0x03, starcrus_s2_y_w ),
	    new IOWritePort( 0x04, 0x04, starcrus_p1_x_w ),
	    new IOWritePort( 0x05, 0x05, starcrus_p1_y_w ),
	    new IOWritePort( 0x06, 0x06, starcrus_p2_x_w ),
	    new IOWritePort( 0x07, 0x07, starcrus_p2_y_w ),
	    new IOWritePort( 0x08, 0x08, starcrus_ship_parm_1_w ),
	    new IOWritePort( 0x09, 0x09, starcrus_ship_parm_2_w ),
	    new IOWritePort( 0x0a, 0x0a, starcrus_proj_parm_1_w ),
	    new IOWritePort( 0x0b, 0x0b, starcrus_proj_parm_2_w ),
	    new IOWritePort( -1 )  /* end of table */
	};
	
	
	
	static InputPortPtr input_ports_starcrus = new InputPortPtr(){ public void handler() { 
			PORT_START(); 	/* player 1 */
			PORT_BIT (0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY );/* ccw */
			PORT_BIT (0x02, IP_ACTIVE_LOW, IPT_BUTTON2 );/* engine */
			PORT_BIT (0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY );/* cw */
	        PORT_BIT (0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
	        PORT_BIT (0x10, IP_ACTIVE_LOW, IPT_BUTTON3 );/* torpedo */
	        PORT_BIT (0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
	        PORT_BIT (0x40, IP_ACTIVE_LOW, IPT_BUTTON1 );/* phaser */
	        PORT_BIT (0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
	        PORT_START();   /* player 2 */
	        PORT_BIT (0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2 );/* ccw */
	        PORT_BIT (0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );/* engine */
	        PORT_BIT (0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 );/* cw */
	        PORT_BIT (0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
	        PORT_BIT (0x10, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );/* torpedo */
	        PORT_BIT (0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
	        PORT_BIT (0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );/* phaser */
	        PORT_BIT (0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
			PORT_START(); 
	        PORT_DIPNAME ( 0x03, 0x02, "Game Time" );
	        PORT_DIPSETTING ( 0x03, "60 secs" );
	        PORT_DIPSETTING ( 0x02, "90 secs" );
	        PORT_DIPSETTING ( 0x01, "120 secs" );
	        PORT_DIPSETTING ( 0x00, "150 secs" );
	        PORT_DIPNAME ( 0x04, 0x00, DEF_STR( "Coinage") );
	        PORT_DIPSETTING ( 0x04, DEF_STR( "2C_1C") );
	        PORT_DIPSETTING ( 0x00, DEF_STR( "1C_1C") );
	        PORT_BIT (0x08, IP_ACTIVE_LOW, IPT_COIN2 );
	        PORT_BIT (0x10, IP_ACTIVE_LOW, IPT_COIN1 );
	        PORT_DIPNAME ( 0x20, 0x20, "Mode" );
	        PORT_DIPSETTING ( 0x20, "Standard" );
	        PORT_DIPSETTING ( 0x00, "Alternate" );
	        PORT_BIT (0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
	        PORT_BIT (0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout spritelayout1 = new GfxLayout
	(
		16,16,    /* 16x16 sprites */
		4,          /* 4 sprites */
		1,      /* 1 bits per pixel */
		new int[] { 0 },  /* 1 chip */
		new int[] { 0*8+4,  0*8+4,  1*8+4,  1*8+4, 2*8+4, 2*8+4, 3*8+4, 3*8+4,
		  4*8+4,  4*8+4,  5*8+4,  5*8+4, 6*8+4, 6*8+4, 7*8+4, 7*8+4 },
		new int[] { 0, 0, 1*64, 1*64, 2*64, 2*64, 3*64, 3*64,
		  4*64, 4*64, 5*64, 5*64, 6*64, 6*64, 7*64, 7*64 },
		1  /* every sprite takes 1 consecutive bit */
	);
	static GfxLayout spritelayout2 = new GfxLayout
	(
	    16,16,   /* 16x16 sprites */
	    4,       /* 4 sprites */
	    1,       /* 1 bits per pixel */
	    new int[] { 0 },   /* 1 chip */
	    new int[] { 0*8+4,  1*8+4,  2*8+4,  3*8+4, 4*8+4, 5*8+4, 6*8+4, 7*8+4,
	      8*8+4,  9*8+4,  10*8+4,  11*8+4, 12*8+4, 13*8+4, 14*8+4, 15*8+4 },
	    new int[] { 0, 1*128, 2*128, 3*128, 4*128, 5*128, 6*128, 7*128,
	      8*128, 9*128, 10*128, 11*128, 12*128, 13*128, 14*128, 15*128 },
	    1 /* every sprite takes 1 consecutive bytes */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0x0000, spritelayout1, 0, 1 ),
	    new GfxDecodeInfo( REGION_GFX1, 0x0040, spritelayout1, 0, 1 ),
	    new GfxDecodeInfo( REGION_GFX1, 0x0080, spritelayout1, 0, 1 ),
	    new GfxDecodeInfo( REGION_GFX1, 0x00c0, spritelayout1, 0, 1 ),
	    new GfxDecodeInfo( REGION_GFX2, 0x0000, spritelayout1, 0, 1 ),
	    new GfxDecodeInfo( REGION_GFX2, 0x0040, spritelayout1, 0, 1 ),
	    new GfxDecodeInfo( REGION_GFX2, 0x0080, spritelayout1, 0, 1 ),
	    new GfxDecodeInfo( REGION_GFX2, 0x00c0, spritelayout1, 0, 1 ),
	    new GfxDecodeInfo( REGION_GFX3, 0x0000, spritelayout2, 0, 1 ),
	    new GfxDecodeInfo( REGION_GFX3, 0x0100, spritelayout2, 0, 1 ),
	    new GfxDecodeInfo( REGION_GFX3, 0x0200, spritelayout2, 0, 1 ),
	    new GfxDecodeInfo( REGION_GFX3, 0x0300, spritelayout2, 0, 1 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	static unsigned char palette[] =
	{
		0x00,0x00,0x00, /* Black */
	    0xff,0xff,0xff, /* White */
	};
	static unsigned short colortable[] =
	{
		0x00, 0x01, /* White on Black */
	};
	static void init_palette(unsigned char *game_palette, unsigned short *game_colortable,const unsigned char *color_prom)
	{
		memcpy(game_palette,palette,sizeof(palette));
		memcpy(game_colortable,colortable,sizeof(colortable));
	}
	
	static const char *starcrus_sample_names[] =
	{
	    "*starcrus",
	    "engine.wav",	/* engine sound, channel 0 */
	    "explos1.wav",	/* explosion sound, first part, channel 1 */
	    "explos2.wav",	/* explosion sound, second part, channel 1 */
	    "launch.wav",	/* launch sound, channels 2 and 3 */
	    0   /* end of array */
	};
	
	static struct Samplesinterface samples_interface =
	{
	    4,	/* 4 channels */
		100,	/* volume */
		starcrus_sample_names
	};
	
	
	static MachineDriver machine_driver_starcrus = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_8080,
				9750000/9,  /* 8224 chip is a divide by 9 */
				readmem,writemem,readport,writeport,
				interrupt,1
			)
		},
		57, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* single CPU, no need for interleaving */
		null,
	
		/* video hardware */
	    32*8, 32*8, { 0*8, 32*8-1, 0*8, 32*8-1 },
		gfxdecodeinfo,
		sizeof(palette) / sizeof(palette[null]) / 3, sizeof(colortable) / sizeof(colortable[null]),
		init_palette,
	
		VIDEO_TYPE_RASTER,
		null,
		starcrus_vh_start,
		starcrus_vh_stop,
		starcrus_vh_screenrefresh,
	
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
	
	  Game ROMs
	
	***************************************************************************/
	
	static RomLoadPtr rom_starcrus = new RomLoadPtr(){ public void handler(){ 
	    ROM_REGION( 0x10000, REGION_CPU1 ); /* code */
		ROM_LOAD( "starcrus.j1",   0x0000, 0x0200, 0x0ee60a50 );
		ROM_LOAD( "starcrus.k1",   0x0200, 0x0200, 0xa7bc3bc4 );
		ROM_LOAD( "starcrus.l1",   0x0400, 0x0200, 0x10d233ec );
		ROM_LOAD( "starcrus.m1",   0x0600, 0x0200, 0x2facbfee );
		ROM_LOAD( "starcrus.n1",   0x0800, 0x0200, 0x42083247 );
		ROM_LOAD( "starcrus.p1",   0x0a00, 0x0200, 0x61dfe581 );
		ROM_LOAD( "starcrus.r1",   0x0c00, 0x0200, 0x010cdcfe );
		ROM_LOAD( "starcrus.s1",   0x0e00, 0x0200, 0xda4e276b );
	
	    ROM_REGION( 0x0200, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "starcrus.e6",   0x0000, 0x0200, 0x54887a25 );
	
	    ROM_REGION( 0x0200, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "starcrus.l2",   0x0000, 0x0200, 0x54887a25 );
	
	    ROM_REGION( 0x0400, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "starcrus.j4",   0x0000, 0x0200, 0x25f15ae1 );
		ROM_LOAD( "starcrus.g5",   0x0200, 0x0200, 0x73b27f6e );
	ROM_END(); }}; 
	
	
	public static GameDriver driver_starcrus	   = new GameDriver("1977"	,"starcrus"	,"starcrus.java"	,rom_starcrus,null	,machine_driver_starcrus	,input_ports_starcrus	,null	,ROT0	,	"Ramtek", "Star Cruiser" )
}
