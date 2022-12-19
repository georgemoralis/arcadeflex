/***************************************************************************

Knuckle Joe - (c) 1985 Seibu Kaihatsu ( Taito License )

driver by Ernesto Corvi

Notes:
This board seems to be an Irem design.
The sound hardware is definitely the 6803-based one used by the classic Irem
games, and the video hardware is pretty much like Irem games too. The only
strange thing is that the screen is flipped vertically.

TODO:
- lots of unknown dipswitches

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class kncljoe
{
	
	
	/* from vidhrdw */
	void kncljoe_vh_convert_color_prom(UBytePtr palette,unsigned short *colortable,const UBytePtr color_prom);
	WRITE_HANDLER(kncljoe_videoram_w);
	WRITE_HANDLER(kncljoe_control_w);
	WRITE_HANDLER(kncljoe_scroll_w);
	
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0xbfff, MRA_ROM ),
		new MemoryReadAddress( 0xc000, 0xcfff, videoram_r ),		/* videoram */
		new MemoryReadAddress( 0xd800, 0xd800, input_port_0_r ), /* IN 0 */
		new MemoryReadAddress( 0xd801, 0xd801, input_port_1_r ), /* IN 1 */
		new MemoryReadAddress( 0xd802, 0xd802, input_port_2_r ), /* IN 2 */
		new MemoryReadAddress( 0xd803, 0xd803, input_port_3_r ),	/* DSW A */
		new MemoryReadAddress( 0xd804, 0xd804, input_port_4_r ),	/* DSW B */
		new MemoryReadAddress( 0xe800, 0xefff, MRA_RAM ),		/* spriteram */
		new MemoryReadAddress( 0xf000, 0xffff, MRA_RAM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new MemoryWriteAddress( 0xc000, 0xcfff, kncljoe_videoram_w, videoram ),
		new MemoryWriteAddress( 0xd000, 0xd000, kncljoe_scroll_w ),
		new MemoryWriteAddress( 0xd800, 0xd800, irem_sound_cmd_w ),
		new MemoryWriteAddress( 0xd801, 0xd801, kncljoe_control_w ),
		new MemoryWriteAddress( 0xe800, 0xefff, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0xf000, 0xffff, MWA_RAM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	/******************************************************************************/
	
	static InputPortPtr input_ports_kncljoe = new InputPortPtr(){ public void handler() { 
	PORT_START(); 	/* IN 0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT_IMPULSE( 0x04, IP_ACTIVE_LOW, IPT_COIN1, 4 );	PORT_BIT_IMPULSE( 0x08, IP_ACTIVE_LOW, IPT_COIN2, 4 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 	/* IN 1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 	/* IN 2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 	/* DSW A */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
		PORT_DIPSETTING( 0x00, DEF_STR( "5C_1C") );
		PORT_DIPSETTING( 0x04, DEF_STR( "4C_1C") );
		PORT_DIPSETTING( 0x02, DEF_STR( "3C_1C") );
		PORT_DIPSETTING( 0x06, DEF_STR( "2C_1C") );
		PORT_DIPSETTING( 0x07, DEF_STR( "1C_1C") );
		PORT_DIPSETTING( 0x03, DEF_STR( "1C_2C") );
		PORT_DIPSETTING( 0x05, DEF_STR( "1C_3C") );
		PORT_DIPSETTING( 0x01, DEF_STR( "1C_5C") );
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Coin_B") );
		PORT_DIPSETTING( 0x00, DEF_STR( "3C_1C") );
		PORT_DIPSETTING( 0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING( 0x18, DEF_STR( "1C_1C") );
		PORT_DIPSETTING( 0x08, DEF_STR( "1C_2C") );
		PORT_BITX(    0x20, 0x20, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE );	PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, "RAM Test?" );	PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
		PORT_START();  /* DSW B */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(	0x02, DEF_STR( "Upright") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x04, "3" );	PORT_DIPSETTING(    0x00, "5" );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,3),
		3,
		new int[] { RGN_FRAC(2,3), RGN_FRAC(1,3), RGN_FRAC(0,3) },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,3),
		3,
		new int[] { RGN_FRAC(2,3), RGN_FRAC(1,3), RGN_FRAC(0,3) },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7,
				8*8+0, 8*8+1, 8*8+2, 8*8+3, 8*8+4, 8*8+5, 8*8+6, 8*8+7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				16*8, 17*8, 18*8, 19*8, 20*8, 21*8, 22*8, 23*8 },
		32*8
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,   0x00, 16 ),	/* colors 0x00-0x7f direct mapped */
		new GfxDecodeInfo( REGION_GFX2, 0, spritelayout, 0x80, 16 ),	/* colors 0x80-0x8f with lookup table */
		new GfxDecodeInfo( REGION_GFX3, 0, spritelayout, 0x80, 16 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static MachineDriver machine_driver_kncljoe = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				4000000, /* ? MHz */
				readmem,writemem,null,null,
				interrupt,1
			),
			IREM_AUDIO_CPU
		},
		30, DEFAULT_30HZ_VBLANK_DURATION,  /* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 1*8, 31*8-1, 0*8, 32*8-1 ),
		gfxdecodeinfo,
		128+16, 16*8+16*8,
		kncljoe_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		kncljoe_vh_start,
		null,
		kncljoe_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			IREM_AUDIO
		}
	);
	
	
	
	static RomLoadPtr rom_kncljoe = new RomLoadPtr(){ public void handler(){ 
	    ROM_REGION( 0x10000, REGION_CPU1 ); /* 64k for code */
		ROM_LOAD( "kj-1.bin", 0x0000, 0x4000, 0x4e4f5ff2 );	ROM_LOAD( "kj-2.bin", 0x4000, 0x4000, 0xcb11514b );	ROM_LOAD( "kj-3.bin", 0x8000, 0x4000, 0x0f50697b );
	    ROM_REGION( 0x10000, REGION_CPU2 ); /* 64k for audio code */
		ROM_LOAD( "kj-13.bin",0xe000, 0x2000, 0x0a0be3f5 );
	    ROM_REGION( 0xc000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
		ROM_LOAD( "kj-10.bin", 0x0000,  0x4000, 0x74d3ba33 );    ROM_LOAD( "kj-11.bin", 0x4000,  0x4000, 0x8ea01455 );    ROM_LOAD( "kj-12.bin", 0x8000,  0x4000, 0x33367c41 );
	    ROM_REGION( 0x18000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* sprites */
		ROM_LOAD( "kj-4.bin", 0x00000,  0x8000, 0xa499ea10 );	ROM_LOAD( "kj-6.bin", 0x08000,  0x8000, 0x815f5c0a );	ROM_LOAD( "kj-5.bin", 0x10000,  0x8000, 0x11111759 );
	    ROM_REGION( 0xc000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
		ROM_LOAD( "kj-7.bin", 0x0000,   0x4000, 0x121fcccb );	ROM_LOAD( "kj-9.bin", 0x4000,   0x4000, 0xaffbe3eb );	ROM_LOAD( "kj-8.bin", 0x8000,   0x4000, 0xe057e72a );
	    ROM_REGION( 0x420, REGION_PROMS );	ROM_LOAD( "kjclr1.bin",  0x000, 0x100, 0xc3378ac2 );/* tile red */
		ROM_LOAD( "kjclr2.bin",  0x100, 0x100, 0x2126da97 );/* tile green */
		ROM_LOAD( "kjclr3.bin",  0x200, 0x100, 0xfde62164 );/* tile blue */
		ROM_LOAD( "kjprom5.bin", 0x300, 0x020, 0x5a81dd9f );/* sprite palette */
		ROM_LOAD( "kjprom4.bin", 0x320, 0x100, 0x48dc2066 );/* sprite clut */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_kncljoea = new RomLoadPtr(){ public void handler(){ 
	    ROM_REGION( 0x10000, REGION_CPU1 ); /* 64k for code */
		ROM_LOAD( "kj01.bin", 0x0000, 0x4000, 0xf251019e );	ROM_LOAD( "kj-2.bin", 0x4000, 0x4000, 0xcb11514b );	ROM_LOAD( "kj-3.bin", 0x8000, 0x4000, 0x0f50697b );
	    ROM_REGION( 0x10000, REGION_CPU2 ); /* 64k for audio code */
		ROM_LOAD( "kj-13.bin",0xe000, 0x2000, 0x0a0be3f5 );
	    ROM_REGION( 0xc000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* tiles */
		ROM_LOAD( "kj-10.bin", 0x0000,  0x4000, 0x74d3ba33 );    ROM_LOAD( "kj-11.bin", 0x4000,  0x4000, 0x8ea01455 );    ROM_LOAD( "kj-12.bin", 0x8000,  0x4000, 0x33367c41 );
	    ROM_REGION( 0x18000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* sprites */
		ROM_LOAD( "kj-4.bin", 0x00000,  0x8000, 0xa499ea10 );	ROM_LOAD( "kj-6.bin", 0x08000,  0x8000, 0x815f5c0a );	ROM_LOAD( "kj-5.bin", 0x10000,  0x8000, 0x11111759 );
	    ROM_REGION( 0xc000, REGION_GFX3 | REGIONFLAG_DISPOSE );/* sprites */
		ROM_LOAD( "kj-7.bin", 0x0000,   0x4000, 0x121fcccb );	ROM_LOAD( "kj-9.bin", 0x4000,   0x4000, 0xaffbe3eb );	ROM_LOAD( "kj-8.bin", 0x8000,   0x4000, 0xe057e72a );
	    ROM_REGION( 0x420, REGION_PROMS );	ROM_LOAD( "kjclr1.bin",  0x000, 0x100, 0xc3378ac2 );/* tile red */
		ROM_LOAD( "kjclr2.bin",  0x100, 0x100, 0x2126da97 );/* tile green */
		ROM_LOAD( "kjclr3.bin",  0x200, 0x100, 0xfde62164 );/* tile blue */
		ROM_LOAD( "kjprom5.bin", 0x300, 0x020, 0x5a81dd9f );/* sprite palette */
		ROM_LOAD( "kjprom4.bin", 0x320, 0x100, 0x48dc2066 );/* sprite clut */
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_kncljoe	   = new GameDriver("1985"	,"kncljoe"	,"kncljoe.java"	,rom_kncljoe,null	,machine_driver_kncljoe	,input_ports_kncljoe	,null	,ROT0	,	"[Seibu Kaihatsu] (Taito license)", "Knuckle Joe (set 1)" )
	public static GameDriver driver_kncljoea	   = new GameDriver("1985"	,"kncljoea"	,"kncljoe.java"	,rom_kncljoea,driver_kncljoe	,machine_driver_kncljoe	,input_ports_kncljoe	,null	,ROT0	,	"[Seibu Kaihatsu] (Taito license)", "Knuckle Joe (set 2)" )
}
