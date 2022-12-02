/***************************************************************************
Atari Super Breakout Driver

Note:  I'm cheating a little bit with the paddle control.  The original
game handles the paddle control as following.  The paddle is a potentiometer.
Every VBlank signal triggers the start of a voltage ramp.  Whenever the
ramp has the same value as the potentiometer, an NMI is generated.	In the
NMI code, the current scanline value is used to calculate the value to
put into location $1F in memory.  I cheat in this driver by just putting
the paddle value directly into $1F, which has the same net result.

If you have any questions about how this driver works, don't hesitate to
ask.  - Mike Balfour (mab22@po.cwru.edu)

CHANGES:
MAB 05 MAR 99 - changed overlay support to use artwork functions
***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;

public class sbrkout
{
	
	/* vidhrdw/sbrkout.c */
	
	/* machine/sbrkout.c */
	
	
	/* sound hardware - temporary */
	
	public static WriteHandlerPtr sbrkout_dac_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	static void sbrkout_tones_4V(int foo);
	static int init_timer=1;
	
	#define TIME_4V 4.075/4
	
	unsigned char *sbrkout_sound;
	
	public static WriteHandlerPtr sbrkout_dac_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		sbrkout_sound[offset]=data;
	
		if (init_timer)
		{
			timer_set (TIME_IN_MSEC(TIME_4V), 0, sbrkout_tones_4V);
			init_timer=0;
		}
	} };
	
	static void sbrkout_tones_4V(int foo)
	{
		static int vlines=0;
	
		if ((*sbrkout_sound) & vlines)
			DAC_data_w(0,255);
		else
			DAC_data_w(0,0);
	
		vlines = (vlines+1) % 16;
	
		timer_set (TIME_IN_MSEC(TIME_4V), 0, sbrkout_tones_4V);
	}
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x001f, 0x001f, input_port_6_r ), /* paddle value */
		new MemoryReadAddress( 0x0000, 0x00ff, MRA_RAM ), /* Zero Page RAM */
		new MemoryReadAddress( 0x0100, 0x01ff, MRA_RAM ), /* ??? */
		new MemoryReadAddress( 0x0400, 0x077f, MRA_RAM ), /* Video Display RAM */
		new MemoryReadAddress( 0x0828, 0x0828, sbrkout_select1 ), /* Select 1 */
		new MemoryReadAddress( 0x082f, 0x082f, sbrkout_select2 ), /* Select 2 */
		new MemoryReadAddress( 0x082e, 0x082e, input_port_5_r ), /* Serve Switch */
		new MemoryReadAddress( 0x0830, 0x0833, sbrkout_read_DIPs ), /* DIP Switches */
		new MemoryReadAddress( 0x0840, 0x0840, input_port_1_r ), /* Coin Switches */
		new MemoryReadAddress( 0x0880, 0x0880, input_port_2_r ), /* Start Switches */
		new MemoryReadAddress( 0x08c0, 0x08c0, input_port_3_r ), /* Self Test Switch */
		new MemoryReadAddress( 0x0c00, 0x0c00, input_port_4_r ), /* Vertical Sync Counter */
		new MemoryReadAddress( 0x2c00, 0x3fff, MRA_ROM ), /* PROGRAM */
		new MemoryReadAddress( 0xfff0, 0xffff, MRA_ROM ), /* PROM8 for 6502 vectors */
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0011, 0x0011, sbrkout_dac_w, sbrkout_sound ), /* Noise Generation Bits */
		new MemoryWriteAddress( 0x0010, 0x0014, MWA_RAM, sbrkout_horiz_ram ), /* Horizontal Ball Position */
		new MemoryWriteAddress( 0x0018, 0x001d, MWA_RAM, sbrkout_vert_ram ), /* Vertical Ball Position / ball picture */
		new MemoryWriteAddress( 0x0000, 0x00ff, MWA_RAM ), /* WRAM */
		new MemoryWriteAddress( 0x0100, 0x01ff, MWA_RAM ), /* ??? */
		new MemoryWriteAddress( 0x0400, 0x07ff, videoram_w, videoram, videoram_size ), /* DISPLAY */
	//		  new MemoryWriteAddress( 0x0c10, 0x0c11, sbrkout_serve_led ), /* Serve LED */
		new MemoryWriteAddress( 0x0c30, 0x0c31, sbrkout_start_1_led ), /* 1 Player Start Light */
		new MemoryWriteAddress( 0x0c40, 0x0c41, sbrkout_start_2_led ), /* 2 Player Start Light */
		new MemoryWriteAddress( 0x0c50, 0x0c51, MWA_RAM ), /* NMI Pot Reading Enable */
		new MemoryWriteAddress( 0x0c70, 0x0c71, MWA_RAM ), /* Coin Counter */
		new MemoryWriteAddress( 0x0c80, 0x0c80, MWA_NOP ), /* Watchdog */
		new MemoryWriteAddress( 0x0e00, 0x0e00, MWA_NOP ), /* IRQ Enable? */
		new MemoryWriteAddress( 0x1000, 0x1000, MWA_RAM ), /* LSB of Pot Reading */
		new MemoryWriteAddress( 0x2c00, 0x3fff, MWA_ROM ), /* PROM1-PROM8 */
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static InputPortPtr input_ports_sbrkout = new InputPortPtr(){ public void handler() { 
		PORT_START(); 		/* DSW - fake port, gets mapped to Super Breakout ports */
		PORT_DIPNAME( 0x03, 0x00, "Language" );
		PORT_DIPSETTING(	0x00, "English" );
		PORT_DIPSETTING(	0x01, "German" );
		PORT_DIPSETTING(	0x02, "French" );
		PORT_DIPSETTING(	0x03, "Spanish" );
		PORT_DIPNAME( 0x0C, 0x08, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x0C, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x08, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x70, 0x00, "Extended Play" );/* P=Progressive, C=Cavity, D=Double */
		PORT_DIPSETTING(	0x10, "200P/200C/200D" );
		PORT_DIPSETTING(	0x20, "400P/300C/400D" );
		PORT_DIPSETTING(	0x30, "600P/400C/600D" );
		PORT_DIPSETTING(	0x40, "900P/700C/800D" );
		PORT_DIPSETTING(	0x50, "1200P/900C/1000D" );
		PORT_DIPSETTING(	0x60, "1600P/1100C/1200D" );
		PORT_DIPSETTING(	0x70, "2000P/1400C/1500D" );
		PORT_DIPSETTING(	0x00, "None" );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x80, "3" );
		PORT_DIPSETTING(	0x00, "5" );
	
		PORT_START(); 		/* IN0 */
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_COIN2 );
	
		PORT_START(); 		/* IN1 */
		PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_START2 );
	
		PORT_START(); 		/* IN2 */
		PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_TILT );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	
		PORT_START(); 		/* IN3 */
		PORT_BIT ( 0xFF, IP_ACTIVE_LOW, IPT_VBLANK );
	
		PORT_START(); 		/* IN4 */
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_BUTTON1 );
	
		PORT_START(); 		/* IN5 */
		PORT_ANALOG( 0xff, 0x00, IPT_PADDLE | IPF_REVERSE, 50, 10, 0, 255 );
	
		PORT_START(); 		/* IN6 - fake port, used to set the game select dial */
		PORT_BITX(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN, "Progressive", KEYCODE_E, IP_JOY_NONE );
		PORT_BITX(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN, "Double", KEYCODE_D, IP_JOY_NONE );
		PORT_BITX(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN, "Cavity", KEYCODE_C, IP_JOY_NONE );
	INPUT_PORTS_END(); }}; 
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		64, 	/* 64 characters */
		1,		/* 1 bit per pixel */
		new int[] { 0 },		  /* no separation in 1 bpp */
		new int[] { 4, 5, 6, 7, 0x200*8 + 4, 0x200*8 + 5, 0x200*8 + 6, 0x200*8 + 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8 /* every char takes 8 consecutive bytes */
	);
	
	static GfxLayout balllayout = new GfxLayout
	(
		3,3,	/* 3*3 character? */
		2,	    /* 2 characters */
		1,		/* 1 bit per pixel */
		new int[] { 0 },	/* no separation in 1 bpp */
		new int[] { 0, 1, 2 },
		new int[] { 0*8, 1*8, 2*8 },
		3*8 /* every char takes 3 consecutive byte */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout, 0, 2 ),
		new GfxDecodeInfo( REGION_GFX2, 0, balllayout, 0, 2 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static unsigned char palette[] =
	{
		0x00,0x00,0x00, /* BLACK  */
		0xff,0xff,0xff, /* WHITE  */
	};
	
	#define ARTWORK_COLORS 254
	
	static unsigned short colortable[ARTWORK_COLORS] =
	{
		0, 0,  /* Don't draw */
		0, 1,  /* Draw */
	};
	
	static void init_palette(unsigned char *game_palette, unsigned short *game_colortable,const unsigned char *color_prom)
	{
		memcpy(game_palette,palette,sizeof(palette));
		memcpy(game_colortable,colortable,sizeof(colortable));
	}
	
	
	static DACinterface dac_interface = new DACinterface
	(
		1,
		new int[] { 100 }
	);
	
	
	
	static MachineDriver machine_driver_sbrkout = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6502,
				375000, 	   /* 375 KHz? Should be 750KHz? */
				readmem,writemem,null,null,
				sbrkout_interrupt,1
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* single CPU, no need for interleaving */
		null,
	
		/* video hardware */
		32*8, 28*8, { 0*8, 32*8-1, 0*8, 28*8-1 },
		gfxdecodeinfo,
		ARTWORK_COLORS,ARTWORK_COLORS,		/* Declare extra colors for the overlay */
		init_palette,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		sbrkout_vh_start,
		sbrkout_vh_stop,
		sbrkout_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_DAC,
				dac_interface
			)
		}
	);
	
	
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_sbrkout = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "033453.c1",    0x2800, 0x0800, 0xa35d00e3 );
		ROM_LOAD( "033454.d1",    0x3000, 0x0800, 0xd42ea79a );
		ROM_LOAD( "033455.e1",    0x3800, 0x0800, 0xe0a6871c );
		ROM_RELOAD(               0xf800, 0x0800 );
	
		ROM_REGION( 0x0400, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "033280.p4",    0x0000, 0x0200, 0x5a69ce85 );
		ROM_LOAD( "033281.r4",    0x0200, 0x0200, 0x066bd624 );
	
		ROM_REGION( 0x0020, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "033282.k6",    0x0000, 0x0020, 0x6228736b );
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_sbrkout	   = new GameDriver("1978"	,"sbrkout"	,"sbrkout.java"	,rom_sbrkout,null	,machine_driver_sbrkout	,input_ports_sbrkout	,null	,ROT270	,	"Atari", "Super Breakout" )
}
