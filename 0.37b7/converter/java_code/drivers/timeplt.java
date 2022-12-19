/***************************************************************************

Time Pilot memory map (preliminary)

driver by Nicola Salmoria

Main processor memory map.
0000-5fff ROM
a000-a3ff Color RAM
a400-a7ff Video RAM
a800-afff RAM
b000-b7ff sprite RAM (only areas 0xb010 and 0xb410 are used).

memory mapped ports:

read:
c000      video scan line. This is used by the program to multiplex the cloud
          sprites, drawing them twice offset by 128 pixels.
c200      DSW2
c300      IN0
c320      IN1
c340      IN2
c360      DSW1

write:
c000      command for the audio CPU
c200      watchdog reset
c300      interrupt enable
c302      flip screen
c304      trigger interrupt on audio CPU
c308	  Protection ???  Stuffs in some values computed from ROM content
c30a	  coin counter 1
c30c	  coin counter 2

interrupts:
standard NMI at 0x66

SOUND BOARD:
same as Pooyan

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class timeplt
{
	
	
	extern UBytePtr timeplt_videoram,*timeplt_colorram;
	
	
	/* defined in sndhrdw/timeplt.c */
	extern struct MemoryReadAddress timeplt_sound_readmem[];
	extern struct MemoryWriteAddress timeplt_sound_writemem[];
	extern struct AY8910interface timeplt_ay8910_interface;
	
	
	
	public static WriteHandlerPtr timeplt_coin_counter_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		coin_counter_w.handler(offset >> 1, data);
	} };
	
	public static ReadHandlerPtr psurge_protection_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return 0x80;
	} };
	
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x5fff, MRA_ROM ),
		new MemoryReadAddress( 0x6004, 0x6004, psurge_protection_r ),	/* psurge only */
		new MemoryReadAddress( 0xa000, 0xbfff, MRA_RAM ),
		new MemoryReadAddress( 0xc000, 0xc000, timeplt_scanline_r ),
		new MemoryReadAddress( 0xc200, 0xc200, input_port_4_r ),	/* DSW2 */
		new MemoryReadAddress( 0xc300, 0xc300, input_port_0_r ),	/* IN0 */
		new MemoryReadAddress( 0xc320, 0xc320, input_port_1_r ),	/* IN1 */
		new MemoryReadAddress( 0xc340, 0xc340, input_port_2_r ),	/* IN2 */
		new MemoryReadAddress( 0xc360, 0xc360, input_port_3_r ),	/* DSW1 */
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x5fff, MWA_ROM ),
		new MemoryWriteAddress( 0xa000, 0xa3ff, timeplt_colorram_w, timeplt_colorram ),
		new MemoryWriteAddress( 0xa400, 0xa7ff, timeplt_videoram_w, timeplt_videoram ),
		new MemoryWriteAddress( 0xa800, 0xafff, MWA_RAM ),
		new MemoryWriteAddress( 0xb010, 0xb03f, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0xb410, 0xb43f, MWA_RAM, spriteram_2 ),
		new MemoryWriteAddress( 0xc000, 0xc000, soundlatch_w ),
		new MemoryWriteAddress( 0xc200, 0xc200, watchdog_reset_w ),
		new MemoryWriteAddress( 0xc300, 0xc300, interrupt_enable_w ),
		new MemoryWriteAddress( 0xc302, 0xc302, timeplt_flipscreen_w ),
		new MemoryWriteAddress( 0xc304, 0xc304, timeplt_sh_irqtrigger_w ),
		new MemoryWriteAddress( 0xc30a, 0xc30c, timeplt_coin_counter_w ),  /* c30b is not used */
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	static InputPortPtr input_ports_timeplt = new InputPortPtr(){ public void handler() { 
	PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 	/* DSW0 */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "3" );	PORT_DIPSETTING(    0x02, "4" );	PORT_DIPSETTING(    0x01, "5" );	PORT_BITX( 0,       0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "255", IP_KEY_NONE, IP_JOY_NONE );	PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x08, 0x08, "Bonus" );	PORT_DIPSETTING(    0x08, "10000 50000" );	PORT_DIPSETTING(    0x00, "20000 60000" );	PORT_DIPNAME( 0x70, 0x70, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x70, "1 (Easiest); )
		PORT_DIPSETTING(    0x60, "2" );	PORT_DIPSETTING(    0x50, "3" );	PORT_DIPSETTING(    0x40, "4" );	PORT_DIPSETTING(    0x30, "5 (Average); )
		PORT_DIPSETTING(    0x20, "6" );	PORT_DIPSETTING(    0x10, "7" );	PORT_DIPSETTING(    0x00, "8 (Hardest); )
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_psurge = new InputPortPtr(){ public void handler() { 
	PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();  /* DSW0 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, "Initial Energy" );	PORT_DIPSETTING(    0x00, "4" );	PORT_DIPSETTING(    0x08, "6" );	PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x30, "3" );	PORT_DIPSETTING(    0x20, "4" );	PORT_DIPSETTING(    0x10, "5" );	PORT_DIPSETTING(    0x00, "6" );	PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
	
		PORT_START();  /* DSW1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_5C") );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_5C") );
		PORT_BITX(0x10,     0x10, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Infinite Shots", IP_KEY_NONE, IP_JOY_NONE );	PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, "Stop at Junctions" );	PORT_DIPSETTING(    0x80, DEF_STR( "No") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		512,	/* 512 characters */
		2,	/* 2 bits per pixel */
		new int[] { 4, 0 },
		new int[] { 0, 1, 2, 3, 8*8+0,8*8+1,8*8+2,8*8+3 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		16*8	/* every char takes 16 consecutive bytes */
	);
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		256,	/* 256 sprites */
		2,	/* 2 bits per pixel */
		new int[] { 4, 0 },
		new int[] { 0, 1, 2, 3,  8*8, 8*8+1, 8*8+2, 8*8+3,
				16*8+0, 16*8+1, 16*8+2, 16*8+3,  24*8+0, 24*8+1, 24*8+2, 24*8+3 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				32*8, 33*8, 34*8, 35*8, 36*8, 37*8, 38*8, 39*8 },
		64*8	/* every sprite takes 64 consecutive bytes */
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,        0, 32 ),
		new GfxDecodeInfo( REGION_GFX2, 0, spritelayout,   32*4, 64 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static MachineDriver machine_driver_timeplt = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				3072000,	/* 3.072 MHz (?) */
				readmem,writemem,null,null,
				nmi_interrupt,1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				14318180/8,	/* 1.789772727 MHz */						\
				timeplt_sound_readmem,timeplt_sound_writemem,null,null,
				ignore_interrupt,1	/* interrupts are triggered by the main CPU */
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		32,32*4+64*4,
		timeplt_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		timeplt_vh_start,
		null,
		timeplt_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_AY8910,
				timeplt_ay8910_interface
			)
		}
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_timeplt = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "tm1",          0x0000, 0x2000, 0x1551f1b9 );	ROM_LOAD( "tm2",          0x2000, 0x2000, 0x58636cb5 );	ROM_LOAD( "tm3",          0x4000, 0x2000, 0xff4e0d83 );
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "tm7",          0x0000, 0x1000, 0xd66da813 );
		ROM_REGION( 0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "tm6",          0x0000, 0x2000, 0xc2507f40 );
		ROM_REGION( 0x4000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "tm4",          0x0000, 0x2000, 0x7e437c3e );	ROM_LOAD( "tm5",          0x2000, 0x2000, 0xe8ca87b9 );
		ROM_REGION( 0x0240, REGION_PROMS );	ROM_LOAD( "timeplt.b4",   0x0000, 0x0020, 0x34c91839 );/* palette */
		ROM_LOAD( "timeplt.b5",   0x0020, 0x0020, 0x463b2b07 );/* palette */
		ROM_LOAD( "timeplt.e9",   0x0040, 0x0100, 0x4bbb2150 );/* sprite lookup table */
		ROM_LOAD( "timeplt.e12",  0x0140, 0x0100, 0xf7b7663e );/* char lookup table */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_timepltc = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "cd1y",         0x0000, 0x2000, 0x83ec72c2 );	ROM_LOAD( "cd2y",         0x2000, 0x2000, 0x0dcf5287 );	ROM_LOAD( "cd3y",         0x4000, 0x2000, 0xc789b912 );
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "tm7",          0x0000, 0x1000, 0xd66da813 );
		ROM_REGION( 0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "tm6",          0x0000, 0x2000, 0xc2507f40 );
		ROM_REGION( 0x4000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "tm4",          0x0000, 0x2000, 0x7e437c3e );	ROM_LOAD( "tm5",          0x2000, 0x2000, 0xe8ca87b9 );
		ROM_REGION( 0x0240, REGION_PROMS );	ROM_LOAD( "timeplt.b4",   0x0000, 0x0020, 0x34c91839 );/* palette */
		ROM_LOAD( "timeplt.b5",   0x0020, 0x0020, 0x463b2b07 );/* palette */
		ROM_LOAD( "timeplt.e9",   0x0040, 0x0100, 0x4bbb2150 );/* sprite lookup table */
		ROM_LOAD( "timeplt.e12",  0x0140, 0x0100, 0xf7b7663e );/* char lookup table */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_spaceplt = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "sp1",          0x0000, 0x2000, 0xac8ca3ae );	ROM_LOAD( "sp2",          0x2000, 0x2000, 0x1f0308ef );	ROM_LOAD( "sp3",          0x4000, 0x2000, 0x90aeca50 );
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "tm7",          0x0000, 0x1000, 0xd66da813 );
		ROM_REGION( 0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "sp6",          0x0000, 0x2000, 0x76caa8af );
		ROM_REGION( 0x4000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "sp4",          0x0000, 0x2000, 0x3781ce7a );	ROM_LOAD( "tm5",          0x2000, 0x2000, 0xe8ca87b9 );
		ROM_REGION( 0x0240, REGION_PROMS );	ROM_LOAD( "timeplt.b4",   0x0000, 0x0020, 0x34c91839 );/* palette */
		ROM_LOAD( "timeplt.b5",   0x0020, 0x0020, 0x463b2b07 );/* palette */
		ROM_LOAD( "timeplt.e9",   0x0040, 0x0100, 0x4bbb2150 );/* sprite lookup table */
		ROM_LOAD( "timeplt.e12",  0x0140, 0x0100, 0xf7b7663e );/* char lookup table */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_psurge = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "p1",           0x0000, 0x2000, 0x05f9ba12 );	ROM_LOAD( "p2",           0x2000, 0x2000, 0x3ff41576 );	ROM_LOAD( "p3",           0x4000, 0x2000, 0xe8fe120a );
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "p6",           0x0000, 0x1000, 0xb52d01fa );	ROM_LOAD( "p7",           0x1000, 0x1000, 0x9db5c0ce );
		ROM_REGION( 0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "p4",           0x0000, 0x2000, 0x26fd7f81 );
		ROM_REGION( 0x4000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "p5",           0x0000, 0x2000, 0x6066ec8e );	ROM_LOAD( "tm5",          0x2000, 0x2000, 0xe8ca87b9 );
		ROM_REGION( 0x0240, REGION_PROMS );	ROM_LOAD( "timeplt.b4",   0x0000, 0x0020, 0x00000000 );/* palette */
		ROM_LOAD( "timeplt.b5",   0x0020, 0x0020, 0x00000000 );/* palette */
		ROM_LOAD( "timeplt.e9",   0x0040, 0x0100, 0x00000000 );/* sprite lookup table */
		ROM_LOAD( "timeplt.e12",  0x0140, 0x0100, 0x00000000 );/* char lookup table */
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_timeplt	   = new GameDriver("1982"	,"timeplt"	,"timeplt.java"	,rom_timeplt,null	,machine_driver_timeplt	,input_ports_timeplt	,init_timeplt	,ROT270	,	"Konami", "Time Pilot" )
	public static GameDriver driver_timepltc	   = new GameDriver("1982"	,"timepltc"	,"timeplt.java"	,rom_timepltc,driver_timeplt	,machine_driver_timeplt	,input_ports_timeplt	,init_timeplt	,ROT270	,	"Konami (Centuri license)", "Time Pilot (Centuri)" )
	public static GameDriver driver_spaceplt	   = new GameDriver("1982"	,"spaceplt"	,"timeplt.java"	,rom_spaceplt,driver_timeplt	,machine_driver_timeplt	,input_ports_timeplt	,init_timeplt	,ROT270	,	"bootleg", "Space Pilot" )
	public static GameDriver driver_psurge	   = new GameDriver("1988"	,"psurge"	,"timeplt.java"	,rom_psurge,null	,machine_driver_timeplt	,input_ports_psurge	,init_psurge	,ROT90	,	"<unknown>", "Power Surge" )
}
