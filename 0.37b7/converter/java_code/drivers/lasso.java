/*	Lasso
**	(C)1982 SNK
**
**	input port issues:
**	- fire button auto-repeats on high score entry screen (real behavior?)
**
**	unknown CPU speeds (effects game timing)
*/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class lasso
{
	
	void lasso_vh_convert_color_prom( UINT8 *palette, UINT16 *colortable, const UINT8 *color_prom );
	
	extern UBytePtr lasso_vram; /* 0x2000 bytes for a 256x256x1 bitmap */
	
	
	
	public static InterruptPtr lasso_interrupt = new InterruptPtr() { public int handler() 
	{
		if (cpu_getiloops() == 0)
			return interrupt();
		else
			return nmi_interrupt(); // coin input
	} };
	
	
	static UINT8 *shareram;
	
	public static ReadHandlerPtr shareram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return shareram[offset];
	} };
	
	public static WriteHandlerPtr shareram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		shareram[offset] = data;
	} };
	
	public static WriteHandlerPtr lasso_sound_command_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		soundlatch_w.handler(offset,data);
		cpu_cause_interrupt( 2, Z80_IRQ_INT ); /* ? */
	} };
	
	public static ReadHandlerPtr sound_status_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		/*	0x01: chip#0 ready; 0x02: chip#1 ready */
		return 0x03;
	} };
	
	static int lasso_chip_data;
	
	public static WriteHandlerPtr sound_data_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		lasso_chip_data =
				((data & 0x80) >> 7) |
				((data & 0x40) >> 5) |
				((data & 0x20) >> 3) |
				((data & 0x10) >> 1) |
				((data & 0x08) << 1) |
				((data & 0x04) << 3) |
				((data & 0x02) << 5) |
				((data & 0x01) << 7);
	} };
	
	public static WriteHandlerPtr sound_select_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (~data & 0x01)	/* chip #0 */
			SN76496_0_w(0,lasso_chip_data);
		if (~data & 0x02)	/* chip #1 */
			SN76496_1_w(0,lasso_chip_data);
	} };
	
	
	
	static InputPortPtr input_ports_lasso = new InputPortPtr(){ public void handler() { 
	PORT_START();  /* 1804 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY );	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_4WAY );	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_4WAY );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_4WAY );	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 );/* lasso */
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 );/* shoot */
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNUSED );	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_START();  /* 1805 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_COCKTAIL | IPF_4WAY );	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_COCKTAIL | IPF_4WAY );	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_COCKTAIL | IPF_4WAY );	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_COCKTAIL | IPF_4WAY );	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL );	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON2	| IPF_COCKTAIL );	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNUSED );	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNUSED );
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
		PORT_DIPSETTING(    0x30, "3" );	PORT_DIPSETTING(    0x10, "4" );	PORT_DIPSETTING(    0x20, "5" );//	PORT_DIPSETTING(    0x00, "3" );	PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "Show Instructions" );	PORT_DIPSETTING(	0x00, DEF_STR( "No") );
		PORT_DIPSETTING(	0x80, DEF_STR( "Yes") );
	
		PORT_START();  /* 1807 */
		PORT_BIT( 0x0f, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BIT_IMPULSE( 0x10, IP_ACTIVE_LOW, IPT_COIN2, 1 );	PORT_BIT_IMPULSE( 0x20, IP_ACTIVE_LOW, IPT_COIN1, 1 );	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_START2 );	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_START1 );INPUT_PORTS_END(); }}; 
	
	
	
	
	/* 17f0 on CPU1 maps to 07f0 on CPU2 */
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x03ff, MRA_RAM ), /* work ram */
		new MemoryReadAddress( 0x0400, 0x0bff, MRA_RAM ), /* videoram */
		new MemoryReadAddress( 0x0c00, 0x0c7f, MRA_RAM ), /* spriteram */
		new MemoryReadAddress( 0x1000, 0x17ff, shareram_r ),
		new MemoryReadAddress( 0x1804, 0x1804, input_port_0_r ),
		new MemoryReadAddress( 0x1805, 0x1805, input_port_1_r ),
		new MemoryReadAddress( 0x1806, 0x1806, input_port_2_r ),
		new MemoryReadAddress( 0x1807, 0x1807, input_port_3_r ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x03ff, MWA_RAM ),
		new MemoryWriteAddress( 0x0400, 0x0bff, lasso_videoram_w, videoram ),
		new MemoryWriteAddress( 0x0c00, 0x0c7f, MWA_RAM, spriteram ),
		new MemoryWriteAddress( 0x1000, 0x17ff, shareram_w ),
		new MemoryWriteAddress( 0x1800, 0x1800, lasso_sound_command_w ),
		new MemoryWriteAddress( 0x1801, 0x1801, lasso_backcolor_w ),
		new MemoryWriteAddress( 0x1802, 0x1802, lasso_cocktail_w ),
		new MemoryWriteAddress( 0x1806, 0x1806, MWA_NOP ), /* spurious write */
		new MemoryWriteAddress( 0x8000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )
	};
	
	static MemoryReadAddress readmem_coprocessor[] =
	{
		new MemoryReadAddress( 0x0000, 0x07ff, MRA_RAM ),	/* shared RAM */
		new MemoryReadAddress( 0x2000, 0x3fff, MRA_RAM ),
		new MemoryReadAddress( 0x8000, 0x8fff, MRA_ROM ),
		new MemoryReadAddress( 0xf000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress writemem_coprocessor[] =
	{
		new MemoryWriteAddress( 0x0000, 0x07ff, MWA_RAM, shareram ),	/* code is executed from here */
		new MemoryWriteAddress( 0x2000, 0x3fff, MWA_RAM, lasso_vram ),
		new MemoryWriteAddress( 0x8000, 0x8fff, MWA_ROM ),
		new MemoryWriteAddress( 0xf000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )
	};
	
	static MemoryReadAddress readmem_sound[] =
	{
		new MemoryReadAddress( 0x0000, 0x01ff, MRA_RAM ),
		new MemoryReadAddress( 0x5000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0xb004, 0xb004, sound_status_r ),
		new MemoryReadAddress( 0xb005, 0xb005, soundlatch_r ),
		new MemoryReadAddress( 0xf000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress writemem_sound[] =
	{
		new MemoryWriteAddress( 0x0000, 0x01ff, MWA_RAM ),
		new MemoryWriteAddress( 0x5000, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0xb000, 0xb000, sound_data_w ),
		new MemoryWriteAddress( 0xb001, 0xb001, sound_select_w ),
		new MemoryWriteAddress( 0xf000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )
	};
	
	
	
	static GfxLayout tile_layout = new GfxLayout
	(
		8,8,
		0x100,
		2,
		new int[] { 0, 0x2000*8 },
		new int[] { 0,1,2,3,4,5,6,7 },
		new int[] { 0*8,1*8,2*8,3*8,4*8,5*8,6*8,7*8 },
		8*8
	);
	
	static GfxLayout sprite_layout = new GfxLayout
	(
		16,16,
		0x40,
		2,
		new int[] { 0, 0x2000*8 },
		new int[] {
			0,1,2,3,4,5,6,7,
			64+0,64+1,64+2,64+3,64+4,64+5,64+6,64+7
		},
		new int[] {
			0*8,1*8,2*8,3*8,4*8,5*8,6*8,7*8,
			128+0*8,128+1*8,128+2*8,128+3*8,128+4*8,128+5*8,128+6*8,128+7*8
		},
		32*8
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0x0000, tile_layout,   0, 0x10 ),
		new GfxDecodeInfo( REGION_GFX1, 0x1000, tile_layout,   0, 0x10 ),
		new GfxDecodeInfo( REGION_GFX1, 0x0800, sprite_layout, 0, 0x10 ),
		new GfxDecodeInfo( REGION_GFX1, 0x1800, sprite_layout, 0, 0x10 ),
		new GfxDecodeInfo( -1 )
	};
	
	
	
	static SN76496interface sn76496_interface = new SN76496interface
	(
		2,	/* 2 chips */
		new int[] { 2000000, 2000000 },	/* ? MHz */
		new int[] { 100, 100 }
	);
	
	
	
	static MachineDriver machine_driver_lasso = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6502,
				2000000,	/* 2 MHz (?) */
				readmem,writemem,null,null,
				lasso_interrupt,2,
			),
			new MachineCPU(
				CPU_M6502,
				2000000,	/* 2 MHz (?) */
				readmem_coprocessor,writemem_coprocessor,null,null,
				ignore_interrupt,1,
			),
			new MachineCPU(
				CPU_M6502 | CPU_AUDIO_CPU,
				600000,	/* ?? (controls music tempo) */
				readmem_sound,writemem_sound,null,null,
				ignore_interrupt,1,
			),
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
		100, /* CPU slices */
		null, /* init machine */
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0, 255, 16, 255-16 ),
		gfxdecodeinfo,
		0x40,0x40,
		lasso_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		lasso_vh_start,
		null,
		lasso_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_SN76496,
				sn76496_interface
			)
		}
	);
	
	/*
	USES THREE 6502 CPUS
	
	CHIP #  POSITION  TYPE
	-----------------------
	WMA     IC19      2732   DAUGHTER BD	sound cpu
	WMB     IC18       "      "				sound data
	WMC     IC17       "      "				sound data
	WM5     IC45       "     CONN BD		bitmap coprocessor
	82S123  IC69              "
	82S123  IC70              "
	WM4     IC22      2764   BOTTOM BD		main cpu
	WM3     IC21       "      "				main cpu
	WM2     IC66       "      "				graphics
	WM1     IC65       "      "				graphics
	*/
	static RomLoadPtr rom_lasso = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 6502 code (main cpu) */
		ROM_LOAD( "wm3",       0x8000, 0x2000, 0xf93addd6 );	ROM_RELOAD(            0xc000, 0x2000);	ROM_LOAD( "wm4",       0xe000, 0x2000, 0x77719859 );	ROM_RELOAD(            0xa000, 0x2000);
		ROM_REGION( 0x10000, REGION_CPU2 );/* 6502 code (lasso image blitter) */
		ROM_LOAD( "wm5",       0xf000, 0x1000, 0x7dc3ff07 );	ROM_RELOAD(            0x8000, 0x1000);
		ROM_REGION( 0x10000, REGION_CPU3 );/* 6502 code (sound) */
		ROM_LOAD( "wmc",       0x5000, 0x1000, 0x8b4eb242 );	ROM_LOAD( "wmb",       0x6000, 0x1000, 0x4658bcb9 );	ROM_LOAD( "wma",       0x7000, 0x1000, 0x2e7de3e9 );	ROM_RELOAD(            0xf000, 0x1000 );
		ROM_REGION( 0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "wm1",       0x0000, 0x2000, 0x7db77256 );	ROM_LOAD( "wm2",       0x2000, 0x2000, 0x9e7d0b6f );
		ROM_REGION( 0x40, REGION_PROMS );	ROM_LOAD( "82s123.69", 0x0000, 0x0020, 0x1eabb04d );	ROM_LOAD( "82s123.70", 0x0020, 0x0020, 0x09060f8c );ROM_END(); }}; 
	
	
	public static GameDriver driver_lasso	   = new GameDriver("1982"	,"lasso"	,"lasso.java"	,rom_lasso,null	,machine_driver_lasso	,input_ports_lasso	,null	,ROT90	,	"SNK", "Lasso" )
}
