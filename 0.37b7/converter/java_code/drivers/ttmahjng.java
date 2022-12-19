/***************************************************************************


 ***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class ttmahjng
{
	
	extern UBytePtr ttmahjng_sharedram;
	extern UBytePtr ttmahjng_videoram1;
	extern UBytePtr ttmahjng_videoram2;
	extern size_t ttmahjng_videoram_size;
	
	
	
	static int psel;
	public static WriteHandlerPtr input_port_matrix_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		psel = data;
	} };
	
	public static ReadHandlerPtr input_port_matrix_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int	cdata;
	
		cdata = 0;
		switch (psel)
		{
			case	1:
				cdata = readinputport(2);
				break;
			case	2:
				cdata = readinputport(3);
				break;
			case	4:
				cdata = readinputport(4);
				break;
			case	8:
				cdata = readinputport(5);
				break;
			default:
				break;
		}
		return cdata;
	} };
	
	
	static MemoryReadAddress cpu1_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new MemoryReadAddress( 0x4000, 0x43ff, ttmahjng_sharedram_r ),
		new MemoryReadAddress( 0x4800, 0x4800, input_port_0_r ),
		new MemoryReadAddress( 0x5000, 0x5000, input_port_1_r ),
		new MemoryReadAddress( 0x5800, 0x5800, input_port_matrix_r ),
		new MemoryReadAddress( 0x7838, 0x7838, MRA_NOP ),
		new MemoryReadAddress( 0x7859, 0x7859, MRA_NOP ),
		new MemoryReadAddress( 0x8000, 0xbfff, ttmahjng_videoram1_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress cpu1_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x3fff, MWA_ROM ),
		new MemoryWriteAddress( 0x4000, 0x43ff, ttmahjng_sharedram_w, ttmahjng_sharedram ),
		new MemoryWriteAddress( 0x4800, 0x4800, ttmahjng_out0_w ),
		new MemoryWriteAddress( 0x5000, 0x5000, ttmahjng_out1_w ),
		new MemoryWriteAddress( 0x5800, 0x5800, input_port_matrix_w ),
		new MemoryWriteAddress( 0x5f3e, 0x5f3e, MWA_NOP ),
		new MemoryWriteAddress( 0x6800, 0x6800, AY8910_write_port_0_w ),
		new MemoryWriteAddress( 0x6900, 0x6900, AY8910_control_port_0_w ),
		new MemoryWriteAddress( 0x8000, 0xbfff, ttmahjng_videoram1_w, ttmahjng_videoram1, ttmahjng_videoram_size ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress cpu2_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x1fff, MRA_ROM ),
		new MemoryReadAddress( 0x4000, 0x43ff, ttmahjng_sharedram_r ),
		new MemoryReadAddress( 0x8000, 0xbfff, ttmahjng_videoram2_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress cpu2_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x1fff, MWA_ROM ),
		new MemoryWriteAddress( 0x4000, 0x43ff, ttmahjng_sharedram_w ),
		new MemoryWriteAddress( 0x8000, 0xbfff, ttmahjng_videoram2_w, ttmahjng_videoram2 ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	static InputPortPtr input_ports_ttmahjng = new InputPortPtr(){ public void handler() { 
	PORT_START();       /* IN0 */
		PORT_DIPNAME( 0x01, 0x00, "Unknown 01" );	PORT_DIPSETTING(    0x00, "00" );	PORT_DIPSETTING(    0x01, "01" );	PORT_DIPNAME( 0x02, 0x00, "Unknown 02" );	PORT_DIPSETTING(    0x00, "00" );	PORT_DIPSETTING(    0x02, "02" );	PORT_DIPNAME( 0x04, 0x00, "Unknown 04" );	PORT_DIPSETTING(    0x00, "00" );	PORT_DIPSETTING(    0x04, "04" );	PORT_DIPNAME( 0x08, 0x00, "Unknown 08" );	PORT_DIPSETTING(    0x00, "00" );	PORT_DIPSETTING(    0x08, "08" );	PORT_DIPNAME( 0x10, 0x00, "Unknown 10" );	PORT_DIPSETTING(    0x00, "00" );	PORT_DIPSETTING(    0x10, "10" );	PORT_DIPNAME( 0x20, 0x00, "Unknown 20" );	PORT_DIPSETTING(    0x00, "00" );	PORT_DIPSETTING(    0x20, "20" );	PORT_DIPNAME( 0x40, 0x00, "Unknown 40" );	PORT_DIPSETTING(    0x00, "00" );	PORT_DIPSETTING(    0x40, "40" );	PORT_DIPNAME( 0x80, 0x00, "Unknown 80" );	PORT_DIPSETTING(    0x00, "00" );	PORT_DIPSETTING(    0x80, "80" );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_START();       /* IN2 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 A", KEYCODE_A, IP_JOY_NONE );	PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 E", KEYCODE_E, IP_JOY_NONE );	PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 I", KEYCODE_I, IP_JOY_NONE );	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 M", KEYCODE_M, IP_JOY_NONE );	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Kan", KEYCODE_LCONTROL, IP_JOY_NONE );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START1 );	// START2?
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();       /* IN3 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 B", KEYCODE_B, IP_JOY_NONE );	PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 F", KEYCODE_F, IP_JOY_NONE );	PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 J", KEYCODE_J, IP_JOY_NONE );	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 N", KEYCODE_N, IP_JOY_NONE );	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Reach", KEYCODE_LSHIFT, IP_JOY_NONE );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );	// START1?
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();       /* IN4 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 C", KEYCODE_C, IP_JOY_NONE );	PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 G", KEYCODE_G, IP_JOY_NONE );	PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 K", KEYCODE_K, IP_JOY_NONE );	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 Ron", KEYCODE_SPACE, IP_JOY_NONE );	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Chi", KEYCODE_Z, IP_JOY_NONE );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();       /* IN5 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 D", KEYCODE_D, IP_JOY_NONE );	PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 H", KEYCODE_H, IP_JOY_NONE );	PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 L", KEYCODE_L, IP_JOY_NONE );	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 Pon", KEYCODE_LALT, IP_JOY_NONE );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	
	static AY8910interface ay8910_interface = new AY8910interface
	(
		1,			/* 1 chip */
		10000000/8, 		/* 10MHz / 8 = 1.25MHz */
		new int[] { 50 },
		new ReadHandlerPtr[] { 0 },
		new ReadHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 }
	);
	
	
	static MachineDriver machine_driver_ttmahjng = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80 | CPU_16BIT_PORT,
				2500000,	/* 10MHz / 4 = 2.5MHz */
				cpu1_readmem, cpu1_writemem, null, null,
				interrupt, 1
			),
			new MachineCPU(
				CPU_Z80,
				2500000,	/* 10MHz / 4 = 2.5MHz */
				cpu2_readmem, cpu2_writemem, null, null,
				ignore_interrupt, 0
			)
		},
		57, DEFAULT_REAL_60HZ_VBLANK_DURATION,       /* frames per second, vblank duration */
		1,
		null,
	
		/* video hardware */
		256, 256, new rectangle( 0, 256-1, 0, 256-1 ),
		null,
		8, null,
		ttmahjng_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
		null,
		ttmahjng_vh_start,
		ttmahjng_vh_stop,
		ttmahjng_vh_screenrefresh,
	
		/* sound hardware */
		0, 0, 0, 0,
		new MachineSound[] {
			new MachineSound(
				SOUND_AY8910,
				ay8910_interface
			)
		}
	);
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_ttmahjng = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "ju04", 0x0000, 0x1000, 0xfe7c693a );	ROM_LOAD( "ju05", 0x1000, 0x1000, 0x985723d3 );	ROM_LOAD( "ju06", 0x2000, 0x1000, 0x2cd69bc8 );	ROM_LOAD( "ju07", 0x3000, 0x1000, 0x30e8ec63 );
		ROM_REGION( 0x0200, REGION_PROMS );/* color proms */
		/* The upper 128 bytes are 0's, used by the hardware to blank the display */
		ROM_LOAD( "ju03", 0x0000, 0x0100, 0x27d47624 );	ROM_LOAD( "ju09", 0x0100, 0x0100, 0x27d47624 );
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for the second CPU */
		ROM_LOAD( "ju01", 0x0000, 0x0800, 0x0f05ca3c );	ROM_LOAD( "ju02", 0x0800, 0x0800, 0xc1ffeceb );	ROM_LOAD( "ju08", 0x1000, 0x0800, 0x2dcc76b5 );ROM_END(); }}; 
	
	
	public static GameDriver driver_ttmahjng	   = new GameDriver("1981"	,"ttmahjng"	,"ttmahjng.java"	,rom_ttmahjng,null	,machine_driver_ttmahjng	,input_ports_ttmahjng	,null	,ROT0	,	"Taito", "T.T. Mahjong" )
}
