/***************************************************************************
				Wiping
			    (C) 1982 Nichibutsu

				    driver by

			Allard van der Bas (allard@mindless.com)

1 x Z80 CPU main game, 1 x Z80 with ???? sound hardware.
----------------------------------------------------------------------------
Main processor :

0xA800 - 0xA807	: 64 bits of input and dipswitches.

dip: null.7 1.7 2.7
       null   null   null	coin 1: 1 coin null credit.

       1   1   1	coin 1: 1 coin 7 credit.

dip: 3.7 4.7 5.7
       null   null   null	coin 2: null coin 1 credit.

       1   1   1	coin 2: 7 coin 1 credit.

dip:  7.6
	null		bonus at 30K and 70K
	1		bonus at 50K and 150K

dip: 6.7 7.7
       null   null		2 lives
       null   1		3 lives
       1   null		4 lives
       1   1		5 lives

***************************************************************************/
/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;

public class wiping
{
	
	
	public static WriteHandlerPtr wiping_flipscreen_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	void wiping_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom);
	void wiping_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh);
	
	int wiping_sh_start(const struct MachineSound *msound);
	void wiping_sh_stop(void);
	public static WriteHandlerPtr wiping_sound_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	
	
	static unsigned char *sharedram1,*sharedram2;
	
	public static ReadHandlerPtr shared1_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return sharedram1[offset];
	} };
	
	public static ReadHandlerPtr shared2_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return sharedram2[offset];
	} };
	
	public static WriteHandlerPtr shared1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		sharedram1[offset] = data;
	} };
	
	public static WriteHandlerPtr shared2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		sharedram2[offset] = data;
	} };
	
	
	/* input ports are rotated 90 degrees */
	public static ReadHandlerPtr read_ports = new ReadHandlerPtr() { public int handler(int offset)
	{
		int i,res;
	
	
		res = 0;
		for (i = 0;i < 8;i++)
			res |= ((readinputport(i) >> offset) & 1) << i;
	
		return res;
	} };
	
	public static WriteHandlerPtr subcpu_reset_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (data & 1)
			cpu_set_reset_line(1,CLEAR_LINE);
		else
			cpu_set_reset_line(1,ASSERT_LINE);
	} };
	
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x5fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0x8bff, MRA_RAM ),
		new MemoryReadAddress( 0x9000, 0x93ff, shared1_r ),
		new MemoryReadAddress( 0x9800, 0x9bff, shared2_r ),
		new MemoryReadAddress( 0xa800, 0xa807, read_ports ),
		new MemoryReadAddress( 0xb000, 0xb7ff, MRA_RAM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x5fff, MWA_ROM ),
		new MemoryWriteAddress( 0x8000, 0x83ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x8400, 0x87ff, colorram_w, colorram ),
		new MemoryWriteAddress( 0x8800, 0x88ff, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x8900, 0x8bff, MWA_RAM ),
		new MemoryWriteAddress( 0x9000, 0x93ff, shared1_w, sharedram1 ),
		new MemoryWriteAddress( 0x9800, 0x9bff, shared2_w, sharedram2 ),
		new MemoryWriteAddress( 0xa000, 0xa000, interrupt_enable_w ),
		new MemoryWriteAddress( 0xa002, 0xa002, wiping_flipscreen_w ),
		new MemoryWriteAddress( 0xa003, 0xa003, subcpu_reset_w ),
		new MemoryWriteAddress( 0xb000, 0xb7ff, MWA_RAM ),
		new MemoryWriteAddress( 0xb800, 0xb800, watchdog_reset_w ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	/* Sound cpu data */
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x1fff, MRA_ROM ),
		new MemoryReadAddress( 0x9000, 0x93ff, shared1_r ),
		new MemoryReadAddress( 0x9800, 0x9bff, shared2_r ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x1fff, MWA_ROM ),
		new MemoryWriteAddress( 0x4000, 0x7fff, wiping_sound_w, wiping_soundregs ),
		new MemoryWriteAddress( 0x9000, 0x93ff, shared1_w ),
		new MemoryWriteAddress( 0x9800, 0x9bff, shared2_w ),
		new MemoryWriteAddress( 0xa001, 0xa001, interrupt_enable_w ),
		new MemoryWriteAddress( -1 )
	};
	
	
	
	static InputPortPtr input_ports_wiping = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* 0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_4WAY );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_4WAY );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_4WAY );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0xe0, IP_ACTIVE_HIGH, IPT_UNUSED );
	
		PORT_START(); 	/* 1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0xe0, IP_ACTIVE_HIGH, IPT_UNUSED );
	
		PORT_START(); 	/* 2 */
	
		PORT_START(); 	/* 3 */
	
		PORT_START(); 	/* 4 */
	
		PORT_START(); 	/* 5 */
	
		PORT_START(); 	/* 6 */
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x05, IP_ACTIVE_LOW, IPT_COIN2 );/* note that this changes two bits */
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
		PORT_SERVICE( 0x40, IP_ACTIVE_HIGH );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "30000 70000" );
		PORT_DIPSETTING(    0x80, "50000 150000" );
	
		PORT_START(); 	/* 7 */
		PORT_DIPNAME( 0x07, 0x01, DEF_STR( "Coin_B") ); )
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_7C") );
	//	PORT_DIPSETTING(    0x00, "Disable" );
		PORT_DIPNAME( 0x38, 0x08, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x38, DEF_STR( "7C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "6C_1C") );
		PORT_DIPSETTING(    0x28, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0xc0, 0x40, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "2" );
		PORT_DIPSETTING(    0x40, "3" );
		PORT_DIPSETTING(    0x80, "4" );
		PORT_DIPSETTING(    0xc0, "5" );
	INPUT_PORTS_END(); }}; 
	
	/* identical apart from bonus life */
	static InputPortPtr input_ports_rugrats = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* 0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_4WAY );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_4WAY );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_4WAY );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0xe0, IP_ACTIVE_HIGH, IPT_UNUSED );
	
		PORT_START(); 	/* 1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0xe0, IP_ACTIVE_HIGH, IPT_UNUSED );
	
		PORT_START(); 	/* 2 */
	
		PORT_START(); 	/* 3 */
	
		PORT_START(); 	/* 4 */
	
		PORT_START(); 	/* 5 */
	
		PORT_START(); 	/* 6 */
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x05, IP_ACTIVE_LOW, IPT_COIN2 );/* note that this changes two bits */
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
		PORT_SERVICE( 0x40, IP_ACTIVE_HIGH );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "100000 200000" );
		PORT_DIPSETTING(    0x80, "150000 300000" );
	
		PORT_START(); 	/* 7 */
		PORT_DIPNAME( 0x07, 0x01, DEF_STR( "Coin_B") ); )
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_7C") );
	//	PORT_DIPSETTING(    0x00, "Disable" );
		PORT_DIPNAME( 0x38, 0x08, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x38, DEF_STR( "7C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "6C_1C") );
		PORT_DIPSETTING(    0x28, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0xc0, 0x40, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "2" );
		PORT_DIPSETTING(    0x40, "3" );
		PORT_DIPSETTING(    0x80, "4" );
		PORT_DIPSETTING(    0xc0, "5" );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		256,	/* 256 characters */
		2,	/* 2 bits per pixel */
		new int[] { 0, 4 },	/* the two bitplanes are packed in one byte */
		new int[] { 0, 1, 2, 3, 8+0, 8+1, 8+2, 8+3 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
		16*8	/* every char takes 16 consecutive bytes */
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		128,	/* 128 sprites */
		2,	/* 2 bits per pixel */
		new int[] { 0, 4 },	/* the two bitplanes are packed in one byte */
		new int[] { 0, 1, 2, 3, 8+0, 8+1, 8+2, 8+3,
				16*8+0, 16*8+1, 16*8+2, 16*8+3, 17*8+0, 17*8+1, 17*8+2, 17*8+3 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16,
				16*16, 17*16, 18*16, 19*16, 20*16, 21*16, 22*16, 23*16 },
		64*8	/* every sprite takes 64 consecutive bytes */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,      0, 64 ),
		new GfxDecodeInfo( REGION_GFX2, 0, spritelayout, 64*4, 64 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static struct CustomSound_interface custom_interface =
	{
		wiping_sh_start,
		wiping_sh_stop,
		0
	};
	
	
	
	static MachineDriver machine_driver_wiping = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				18432000/6,	/* 3.072 Mhz */
				readmem,writemem,null,null,
				interrupt,1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				18432000/6,	/* 3.072 Mhz */
				sound_readmem,sound_writemem,null,null,
				null,null,
				interrupt,140	/* periodic interrupt, don't know about the frequency */
			),
		},
		60,DEFAULT_60HZ_VBLANK_DURATION,
		1,
		null, /* init machine */
	
		/* video hardware */
		36*8, 28*8, new rectangle( 0*8, 36*8-1, 0*8, 28*8-1 ),
		gfxdecodeinfo,
		32, 64*4+64*4,
		wiping_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY,
		null,
		generic_vh_start,
		generic_vh_stop,
		wiping_vh_screenrefresh,
	
		/* sound hardware */
		null,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_CUSTOM,
				custom_interface
			)
		}
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_wiping = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* main cpu code */
		ROM_LOAD( "1",            0x0000, 0x2000, 0xb55d0d19 );
		ROM_LOAD( "2",            0x2000, 0x2000, 0xb1f96e47 );
		ROM_LOAD( "3",            0x4000, 0x2000, 0xc67bab5a );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* sound cpu */
		ROM_LOAD( "4",            0x0000, 0x1000, 0xa1547e18 );
	
		ROM_REGION( 0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "8",            0x0000, 0x1000, 0x601160f6 );/* chars */
	
		ROM_REGION( 0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "7",            0x0000, 0x2000, 0x2c2cc054 );/* sprites */
	
		ROM_REGION( 0x0220, REGION_PROMS );
		ROM_LOAD( "wip-g13.bin",  0x0000, 0x0020, 0xb858b897 );/* palette */
		ROM_LOAD( "wip-f4.bin",   0x0020, 0x0100, 0x3f56c8d5 );/* char lookup table */
		ROM_LOAD( "wip-e11.bin",  0x0120, 0x0100, 0xe7400715 );/* sprite lookup table */
	
		ROM_REGION( 0x4000, REGION_SOUND1 );/* samples */
		ROM_LOAD( "rugr5c8",	  0x0000, 0x2000, 0x67bafbbf );
		ROM_LOAD( "rugr6c9",	  0x2000, 0x2000, 0xcac84a87 );
	
		ROM_REGION( 0x0200, REGION_SOUND2 );/* 4bit.8bit sample expansion PROMs */
		ROM_LOAD( "wip-e8.bin",   0x0000, 0x0100, 0xbd2c080b );/* low 4 bits */
		ROM_LOAD( "wip-e9.bin",   0x0100, 0x0100, 0x4017a2a6 );/* high 4 bits */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_rugrats = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* main cpu code */
		ROM_LOAD( "rugr1d1",      0x0000, 0x2000, 0xe7e1bd6d );
		ROM_LOAD( "rugr2d2",      0x2000, 0x2000, 0x5f47b9ad );
		ROM_LOAD( "rugr3d3",      0x4000, 0x2000, 0x3d748d1a );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* sound cpu */
		ROM_LOAD( "rugr4c4",      0x0000, 0x2000, 0xd4a92c38 );
	
		ROM_REGION( 0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "rugr8d2",      0x0000, 0x1000, 0xa3dcaca5 );/* chars */
	
		ROM_REGION( 0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "rugr7c13",     0x0000, 0x2000, 0xfe1191dd );/* sprites */
	
		ROM_REGION( 0x0220, REGION_PROMS );
		ROM_LOAD( "prom.13g",     0x0000, 0x0020, 0xf21238f0 );/* palette */
		ROM_LOAD( "prom.4f",      0x0020, 0x0100, 0xcfc90f3d );/* char lookup table */
		ROM_LOAD( "prom.11e",     0x0120, 0x0100, 0xcfc90f3d );/* sprite lookup table */
	
		ROM_REGION( 0x4000, REGION_SOUND1 );/* samples */
		ROM_LOAD( "rugr5c8",	  0x0000, 0x2000, 0x67bafbbf );
		ROM_LOAD( "rugr6c9",	  0x2000, 0x2000, 0xcac84a87 );
	
		ROM_REGION( 0x0200, REGION_SOUND2 );/* 4bit.8bit sample expansion PROMs */
		ROM_LOAD( "wip-e8.bin",   0x0000, 0x0100, 0xbd2c080b );/* low 4 bits */
		ROM_LOAD( "wip-e9.bin",   0x0100, 0x0100, 0x4017a2a6 );/* high 4 bits */
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_wiping	   = new GameDriver("1982"	,"wiping"	,"wiping.java"	,rom_wiping,null	,machine_driver_wiping	,input_ports_wiping	,null	,ROT90	,	"Nichibutsu", "Wiping" )
	public static GameDriver driver_rugrats	   = new GameDriver("1983"	,"rugrats"	,"wiping.java"	,rom_rugrats,driver_wiping	,machine_driver_wiping	,input_ports_rugrats	,null	,ROT90	,	"Nichibutsu", "Rug Rats" )
}
