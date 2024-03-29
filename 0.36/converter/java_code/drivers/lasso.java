/*	Lasso
**	(C)1982 SNK
**
**	lasso can't be slung up/down (left/right works fine; probably corrupt code ROM)
**  [I don't think so, there is another set that matches 100% this one - NS]
**	sound isn't hooked up
**	imperfect color
**	background color, lasso color arbitrary
**	priority of lasso bitmap plane relative to background/sprites unknown
**	possible timing problems
**	input port issues: see fire buttons on high score entry screen
*/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;

public class lasso
{
	
		unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom );
	
	
	unsigned char *lasso_vram; /* 0x2000 bytes for a 256x256x1 bitmap */
	
	static UINT8 *shareram;
	
	public static ReadHandlerPtr shareram_r = new ReadHandlerPtr() { public int handler(int offset){
		return shareram[offset];
	} };
	
	public static WriteHandlerPtr shareram_w = new WriteHandlerPtr() { public void handler(int offset, int value){
		shareram[offset] = value;
	} };
	
	static struct AY8910interface ay8910_interface =
	{
		1,	/* 1 chip? */
		2000000, /* 2 MHz? */
		{ 100 }, /* volume */
		{ 0 },
		{ 0 },
		{ 0 },
		{ 0 }
	};
	
	/* 17f0 on CPU1 maps to 07f0 on CPU2 */
	
	static MemoryReadAddress readmem1[] ={
		new MemoryReadAddress( 0x0000, 0x03ff, MRA_RAM ), // work ram
		new MemoryReadAddress( 0x0400, 0x0bff, MRA_RAM ), // videoram
		new MemoryReadAddress( 0x0c00, 0x0c7f, MRA_RAM ), // spriteram
		new MemoryReadAddress( 0x1000, 0x17ff, MRA_RAM ), // shareram
		new MemoryReadAddress( 0x1804, 0x1804, input_port_0_r ),
		new MemoryReadAddress( 0x1805, 0x1805, input_port_1_r ),
		new MemoryReadAddress( 0x1806, 0x1806, input_port_2_r ),
		new MemoryReadAddress( 0x1807, 0x1807, input_port_3_r ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress writemem1[] ={
		new MemoryWriteAddress( 0x0000, 0x03ff, MWA_RAM ),
		new MemoryWriteAddress( 0x0400, 0x0bff, MWA_RAM, videoram ),
		new MemoryWriteAddress( 0x0c00, 0x0c7f, MWA_RAM, spriteram ),
		new MemoryWriteAddress( 0x1000, 0x17ff, MWA_RAM, shareram ),
		new MemoryWriteAddress( 0x1800, 0x1800, soundlatch_w ),
		new MemoryWriteAddress( 0x1801, 0x1802, MWA_NOP ),
		new MemoryWriteAddress( 0x1806, 0x1806, MWA_NOP ),
		new MemoryWriteAddress( 0x8000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )
	};
	
	static MemoryReadAddress readmem2[] ={
		new MemoryReadAddress( 0x0000, 0x07ff, shareram_r ),
		new MemoryReadAddress( 0x2000, 0x3fff, MRA_RAM ),
		new MemoryReadAddress( 0x8000, 0x8fff, MRA_ROM ),
		new MemoryReadAddress( 0xf000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress writemem2[] ={
		new MemoryWriteAddress( 0x0000, 0x07ff, shareram_w ),
		new MemoryWriteAddress( 0x2000, 0x3fff, MWA_RAM, lasso_vram ),
		new MemoryWriteAddress( 0x8000, 0x8fff, MWA_ROM ),
		new MemoryWriteAddress( 0xf000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )
	};
	
	static MemoryReadAddress readmem3[] ={
		new MemoryReadAddress( 0x0000, 0x01ff, MRA_RAM ),
		new MemoryReadAddress( 0x5000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0xb004, 0xb004, soundlatch_r ),
	//	new MemoryReadAddress( 0xb005, 0xb005, MWA_NOP ),
		new MemoryReadAddress( 0xf000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress writemem3[] ={
		new MemoryWriteAddress( 0x0000, 0x01ff, MWA_RAM ),
		new MemoryWriteAddress( 0x7000, 0x7fff, MWA_ROM ),
	//	new MemoryWriteAddress( 0xb000, 0xb000, AY8910_control_port_0_w ),
	//	new MemoryWriteAddress( 0xb001, 0xb001, AY8910_write_port_0_w ),
		new MemoryWriteAddress( 0xf000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )
	};
	
	
	
	
	static InputPortPtr input_ports_lasso = new InputPortPtr(){ public void handler() { 
		PORT_START();  /* 1804 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_4WAY );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_4WAY );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_4WAY );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 );/* lasso */
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 );/* shoot */
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNUSED );
	
		PORT_START();  /* 1805 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_COCKTAIL | IPF_4WAY );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_COCKTAIL | IPF_4WAY );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_COCKTAIL | IPF_4WAY );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_COCKTAIL | IPF_4WAY );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON2	| IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNUSED );
	
		PORT_START();  /* 1806 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(	0x01, DEF_STR( "Upright") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x0e, 0x0e, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x02, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x0e, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x08, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x0c, DEF_STR( "1C_6C") );
	//	PORT_DIPSETTING(	0x06, DEF_STR( "1C_1C") );
	//	PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
	//	PORT_DIPSETTING(	0x0a, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x30, "3" );
		PORT_DIPSETTING(    0x10, "4" );
		PORT_DIPSETTING(    0x20, "5" );
	//	PORT_DIPSETTING(    0x00, "3" );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "Show Instructions" );
		PORT_DIPSETTING(	0x00, DEF_STR( "No") );
		PORT_DIPSETTING(	0x80, DEF_STR( "Yes") );
	
		PORT_START();  /* 1807 */
		PORT_BIT( 0x0f, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT_IMPULSE( 0x10, IP_ACTIVE_LOW, IPT_SERVICE, 1 );
		PORT_BIT_IMPULSE( 0x20, IP_ACTIVE_LOW, IPT_COIN1, 1 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_START1 );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout tile_layout = new GfxLayout(
		8,8,
		0x100,
		2,
		new int[] { 0, 0x2000*8 },
		new int[] { 0,1,2,3,4,5,6,7 },
		new int[] { 0*8,1*8,2*8,3*8,4*8,5*8,6*8,7*8 },
		64
	);
	
	static GfxLayout sprite_layout = new GfxLayout(
		16,16,
		0x40,
		2,
		new int[] { 0x4000+0, 0x4000+0x2000*8 },
		new int[] {
			0,1,2,3,4,5,6,7,
			64+0,64+1,64+2,64+3,64+4,64+5,64+6,64+7
		},
		new int[] {
			0*8,1*8,2*8,3*8,4*8,5*8,6*8,7*8,
			128+0*8,128+1*8,128+2*8,128+3*8,128+4*8,128+5*8,128+6*8,128+7*8
		},
		256
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] ={
		new GfxDecodeInfo( REGION_GFX1, 0x0000, tile_layout, 0, 0x10 ),
		new GfxDecodeInfo( REGION_GFX1, 0x0000, sprite_layout, 0, 0x10 ),
		new GfxDecodeInfo( -1 )
	};
	
	int lasso_interrupt( void ){
		static int which;
		which = 1-which;
		if( which ){
			return nmi_interrupt();
		}
		else {
			return interrupt();
		}
	}
	
	static MachineDriver machine_driver_lasso = new MachineDriver(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6502,
				4000000,
				readmem1,writemem1,null,null,
				lasso_interrupt,2,
			),
			new MachineCPU(
				CPU_M6502,
				4000000,
				readmem2,writemem2,null,null,
				ignore_interrupt,1,
			),
			new MachineCPU(
				CPU_M6502 | CPU_AUDIO_CPU,
				4000000,
				readmem3,writemem3,null,null,
				interrupt,1,
			),
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
		500, /* CPU slices */
		null, /* init machine */
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0, 255, 0, 255-16 ),
		gfxdecodeinfo,
		0x41,0x41,
		lasso_vh_convert_color_prom,
		VIDEO_TYPE_RASTER,
		null,
		null,
		null,
		lasso_screenrefresh,
	
		/* sound hardware */
		null,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_AY8910,
				ay8910_interface
			)
		}
	);
	
	/*
	USES THREE 6502 CPU'S
	
	CHIP #  POSITION  TYPE
	-----------------------
	WMA     IC19      2732   DAUGHTER BD	sound cpu
	WMB     IC18       "      "				sound data
	WMC     IC17       "      "				sound data
	
	WM5     IC45       "     CONN BD		lasso graphics coprocessor
	82S123  IC69              "
	82S123  IC70              "
	WM4     IC22      2764   BOTTOM BD		main cpu
	WM3     IC21       "      "				main cpu
	
	WM2     IC66       "      "				graphics
	WM1     IC65       "      "				graphics
	*/
	static RomLoadPtr rom_lasso = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 6502 code (main cpu) */
		ROM_LOAD( "wm3",	0x8000, 0x2000, 0xf93addd6 );
		ROM_RELOAD(			0xc000, 0x2000);
		ROM_LOAD( "wm4",	0xe000, 0x2000, 0x77719859 );
		ROM_RELOAD(			0xa000, 0x2000);
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 6502 code (lasso animation) */
		ROM_LOAD( "wm5",	0xf000, 0x1000, 0x7dc3ff07 );
		ROM_RELOAD(			0x8000, 0x1000);
	
		ROM_REGION( 0x10000, REGION_CPU3 );/* 6502 code (sound) */
		ROM_LOAD( "wmc",	0x5000, 0x1000, 0x8b4eb242 );/* ? */
		ROM_LOAD( "wmb",	0x6000, 0x1000, 0x4658bcb9 );/* ? */
		ROM_LOAD( "wma",	0x7000, 0x1000, 0x2e7de3e9 );
		ROM_RELOAD( 		0xf000, 0x1000 );
	
		ROM_REGION( 0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "wm1",	0x0000, 0x2000, 0x7db77256 );
		ROM_LOAD( "wm2",	0x2000, 0x2000, 0x9e7d0b6f );
	
		ROM_REGION( 0x40, REGION_PROMS );
		ROM_LOAD( "82s123.69",	0x00, 0x20, 0x1eabb04d );
		ROM_LOAD( "82s123.70",	0x20, 0x20, 0x09060f8c );
	ROM_END(); }}; 
	
	
	public static GameDriver driver_lasso	   = new GameDriver("1982"	,"lasso"	,"lasso.java"	,rom_lasso,null	,machine_driver_lasso	,input_ports_lasso	,null	,ROT90	,	"SNK", "Lasso", GAME_IMPERFECT_SOUND | GAME_IMPERFECT_COLORS | GAME_NO_COCKTAIL )
}
