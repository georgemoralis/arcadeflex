/*
 * Zaccaria/Zelco 2650 Games
 *
 * The Invaders
 */

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class zac2650
{
	
	extern UBytePtr s2636ram;
	
	
	
	
	
	static MemoryReadAddress readmem[] ={
		new MemoryReadAddress( 0x0000, 0x17ff, MRA_ROM ),
	    new MemoryReadAddress( 0x1800, 0x1bff, MRA_RAM ),
	    new MemoryReadAddress( 0x1E80, 0x1E80, tinvader_port_0_r ),
	    new MemoryReadAddress( 0x1E81, 0x1E81, input_port_1_r ),
	    new MemoryReadAddress( 0x1E82, 0x1E82, input_port_2_r ),
	    new MemoryReadAddress( 0x1D00, 0x1Dff, MRA_RAM ),
	    new MemoryReadAddress( 0x1F00, 0x1FFF, s2636_r ),			/* S2636 Chip */
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress writemem[] ={
		new MemoryWriteAddress( 0x0000, 0x17FF, MWA_ROM ),
		new MemoryWriteAddress( 0x1800, 0x1bff, videoram_w, videoram, videoram_size ),
	    new MemoryWriteAddress( 0x1D00, 0x1dff, MWA_RAM ),
	    new MemoryWriteAddress( 0x1E80, 0x1E80, tinvader_sound_w ),
	    new MemoryWriteAddress( 0x1F00, 0x1FFF, s2636_w, s2636ram ),
		new MemoryWriteAddress( -1 )
	};
	
	static InputPortPtr input_ports_tinvader = new InputPortPtr(){ public void handler() { 

		PORT_START();  /* 1E80 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN3 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED  );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );/* Missile-Background Collision */
	
	    PORT_START();  /* 1E81 */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );	PORT_DIPSETTING(    0x01, "4" );    PORT_DIPNAME( 0x02, 0x00, "Lightning Speed" );/* Velocita Laser Inv */
		PORT_DIPSETTING(    0x00, "Slow" );	PORT_DIPSETTING(    0x02, "Fast" );	PORT_DIPNAME( 0x1C, 0x04, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x08, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x0C, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x10, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(	0x14, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(	0x18, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(	0x1C, DEF_STR( "1C_7C") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "1000" );	PORT_DIPSETTING(    0x20, "1500" );    PORT_DIPNAME( 0x40, 0x00, "Extended Play" );	PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
	
		PORT_START();  /* 1E82 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_4WAY );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	/* Almost identical, no number of bases selection */
	
	static InputPortPtr input_ports_sinvader = new InputPortPtr(){ public void handler() { 

		PORT_START();  /* 1E80 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN3 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED  );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );/* Missile-Background Collision */
	
	    PORT_START();  /* 1E81 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED  );    PORT_DIPNAME( 0x02, 0x00, "Lightning Speed" );/* Velocita Laser Inv */
		PORT_DIPSETTING(    0x00, "Slow" );	PORT_DIPSETTING(    0x02, "Fast" );	PORT_DIPNAME( 0x1C, 0x04, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x08, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x0C, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x10, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(	0x14, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(	0x18, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(	0x1C, DEF_STR( "1C_7C") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "1000" );	PORT_DIPSETTING(    0x20, "1500" );    PORT_DIPNAME( 0x40, 0x00, "Extended Play" );	PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
	
		PORT_START();  /* 1E82 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_4WAY );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	static unsigned char tinvader_palette[] =
	{
		0x00,0x00,0x00, /* BLACK */
		0xff,0xff,0xff, /* WHITE */
		0x20,0xff,0x20, /* GREEN */
		0xff,0x20,0xff, /* PURPLE */
	};
	
	static void init_palette(UBytePtr game_palette, unsigned short *game_colortable,const UBytePtr color_prom)
	{
		#define COLOR(gfxn,offs) (game_colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
		memcpy(game_palette,tinvader_palette,sizeof(tinvader_palette));
	
		COLOR(0,0) = 0;		/* Game uses first two only */
		COLOR(0,1) = 1;
	    COLOR(0,2) = 0;
	    COLOR(0,3) = 0;
	}
	
	static GfxLayout tinvader_character = new GfxLayout
	(
		8,8,
		128,
		1,
		new int[] { 0 },
		new int[] { 0,1,2,3,4,5,6,7 },
	   	new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8
	);
	
	
	/* These are really 6x8, but overlay an 8x8 screen  */
	/* so we stretch them slightly to occupy same space */
	
	static GfxLayout s2636_character8 = new GfxLayout
	(
		8,8,
		16,
		1,
		new int[] { 0 },
		new int[] { 0,1,1,2,3,4,5,5 },
	   	new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8
	);
	
	static GfxLayout s2636_character16 = new GfxLayout
	(
		16,16,
		16,
		1,
		new int[] { 0 },
		new int[] { 0,0,1,1,1,2,2,2,3,3,3,4,4,4,5,5 },
	   	new int[] { 0*8,0*8,1*8,1*8,2*8,2*8,3*8,3*8,4*8,4*8,5*8,5*8,6*8,6*8,7*8,7*8 },
		8*8
	);
	
	static GfxDecodeInfo tinvader_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, tinvader_character,  0, 1 ),
	  	new GfxDecodeInfo( REGION_CPU1, 0x1F00, s2636_character8, 0, 2 ),	/* dynamic */
	  	new GfxDecodeInfo( REGION_CPU1, 0x1F00, s2636_character16, 0, 2 ),	/* dynamic */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	public static InterruptPtr tinvader_interrupt = new InterruptPtr() { public int handler() 
	{
	    /* Sense line is VBL */
		if (cpu_getiloops() == 0)
			cpu_set_irq_line( 0, 1, ASSERT_LINE);
		else if (cpu_getiloops() == 15)
			cpu_set_irq_line( 0, 1, CLEAR_LINE );
	
	    return ignore_interrupt();
	} };
	
	static MachineDriver machine_driver_tinvader = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_S2650,
				3800000,
				readmem,writemem,null,null,
				tinvader_interrupt,16,
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
		1, /* CPU slices */
		null, /* init machine */
	
		/* video hardware */
		30*8, 32*8, new rectangle( 0, 239, 0, 255 ),
		tinvader_gfxdecodeinfo,
		32768+5,4,
		init_palette,
		VIDEO_TYPE_RASTER,
		null,
		tinvader_vh_start,
		tinvader_vh_stop,
	    tinvader_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
	);
	
	public static WriteHandlerPtr tinvader_sound_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	    /* sounds are NOT the same as space invaders */
	
		logerror("Register %x = Data %d\n",data & 0xfe,data & 0x01);
	
	    /* 08 = hit invader */
	    /* 20 = bonus (extra base) */
	    /* 40 = saucer */
		/* 84 = fire */
	    /* 90 = die */
	    /* c4 = hit saucer */
	} };
	
	static RomLoadPtr rom_sia2650 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x2000, REGION_CPU1 );	ROM_LOAD( "42_1.bin",   0x0000, 0x0800, 0xa85550a9 );	ROM_LOAD( "44_2.bin",   0x0800, 0x0800, 0x48d5a3ed );	ROM_LOAD( "46_3.bin",   0x1000, 0x0800, 0xd766e784 );
		ROM_REGION( 0x400, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "06_inv.bin", 0x0000, 0x0400, 0x7bfed23e );ROM_END(); }}; 
	
	static RomLoadPtr rom_tinv2650 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x2000, REGION_CPU1 );	ROM_LOAD( "42_1.bin",   0x0000, 0x0800, 0xa85550a9 );	ROM_LOAD( "44_2t.bin",  0x0800, 0x0800, 0x083c8621 );	ROM_LOAD( "46_3t.bin",  0x1000, 0x0800, 0x12c0934f );
		ROM_REGION( 0x400, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "06_inv.bin", 0x0000, 0x0400, 0x7bfed23e );ROM_END(); }}; 
	
	
	public static GameDriver driver_sia2650	   = new GameDriver("19??"	,"sia2650"	,"zac2650.java"	,rom_sia2650,null	,machine_driver_tinvader	,input_ports_sinvader	,null	,ROT270	,	"Zaccaria/Zelco", "Super Invader Attack", GAME_NO_SOUND )
	public static GameDriver driver_tinv2650	   = new GameDriver("19??"	,"tinv2650"	,"zac2650.java"	,rom_tinv2650,driver_sia2650	,machine_driver_tinvader	,input_ports_tinvader	,null	,ROT270	,	"Zaccaria/Zelco", "The Invaders", GAME_NO_SOUND )
}
