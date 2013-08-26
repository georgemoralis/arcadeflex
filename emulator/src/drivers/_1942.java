
/*
 * ported to v0.36
 * using automatic conversion tool v0.08 + manual fixes 
 */ 
package drivers;
import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.commonH.*;
import static mame.inputport.*;
import static mame.drawgfxH.*;
import static vidhrdw.generic.*;
import static mame.sndintrfH.*;
import static mame.cpuintrf.*;
import static mame.common.*;
import static arcadeflex.input.*;
import static mame.inputportH.*;
import static mame.inputH.*;
import static arcadeflex.libc.*;
import static arcadeflex.libc_old.*;
import static vidhrdw._1942.*;
import static sound.samplesH.*;
import static mame.memory.*;
import static sound.ay8910.*;
import static sound.ay8910H.*;
import static mame.mame.*;
import static mame.sndintrf.*;

public class _1942
{
        public static WriteHandlerPtr c1942_bankswitch_w = new WriteHandlerPtr() { public void handler(int offset, int data)
        {	
		int bankaddress;
		UBytePtr RAM = memory_region(REGION_CPU1);
	
	
		bankaddress = 0x10000 + (data & 0x03) * 0x4000;
		cpu_setbank(1,new UBytePtr(RAM,bankaddress));
        }};
	
	
	public static InterruptPtr c1942_interrupt = new InterruptPtr() { public int handler()
	{
		if (cpu_getiloops() != 0) return 0x00cf;	/* RST 08h */
		else return 0x00d7;	/* RST 10h - vblank */
	}};
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0xbfff, MRA_BANK1 ),
		new MemoryReadAddress( 0xc000, 0xc000, input_port_0_r ),	/* IN0 */
		new MemoryReadAddress( 0xc001, 0xc001, input_port_1_r ),	/* IN1 */
		new MemoryReadAddress( 0xc002, 0xc002, input_port_2_r ),	/* IN2 */
		new MemoryReadAddress( 0xc003, 0xc003, input_port_3_r ),	/* DSW0 */
		new MemoryReadAddress( 0xc004, 0xc004, input_port_4_r ),	/* DSW1 */
		new MemoryReadAddress( 0xd000, 0xdbff, MRA_RAM ),
		new MemoryReadAddress( 0xe000, 0xefff, MRA_RAM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new MemoryWriteAddress( 0xc800, 0xc800, soundlatch_w ),
		new MemoryWriteAddress( 0xc802, 0xc803, MWA_RAM, c1942_scroll ),
		new MemoryWriteAddress( 0xc804, 0xc804, c1942_flipscreen_w ),
		new MemoryWriteAddress( 0xc805, 0xc805, c1942_palette_bank_w, c1942_palette_bank ),
		new MemoryWriteAddress( 0xc806, 0xc806, c1942_bankswitch_w ),
		new MemoryWriteAddress( 0xcc00, 0xcc7f, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0xd000, 0xd3ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0xd400, 0xd7ff, colorram_w, colorram ),
		new MemoryWriteAddress( 0xd800, 0xdbff, c1942_background_w, c1942_backgroundram, c1942_backgroundram_size ),
		new MemoryWriteAddress( 0xe000, 0xefff, MWA_RAM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new MemoryReadAddress( 0x4000, 0x47ff, MRA_RAM ),
		new MemoryReadAddress( 0x6000, 0x6000, soundlatch_r ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x3fff, MWA_ROM ),
		new MemoryWriteAddress( 0x4000, 0x47ff, MWA_RAM ),
		new MemoryWriteAddress( 0x8000, 0x8000, AY8910_control_port_0_w ),
		new MemoryWriteAddress( 0x8001, 0x8001, AY8910_write_port_0_w ),
		new MemoryWriteAddress( 0xc000, 0xc000, AY8910_control_port_1_w ),
		new MemoryWriteAddress( 0xc001, 0xc001, AY8910_write_port_1_w ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	
	static InputPortPtr input_ports_1942 = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably unused */
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably unused */
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably unused */
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably unused */
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably unused */
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably unused */
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably unused */
	
		PORT_START(); 	/* DSW0 */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x30, "20000 80000" );
		PORT_DIPSETTING(    0x20, "20000 100000" );
		PORT_DIPSETTING(    0x10, "30000 80000" );
		PORT_DIPSETTING(    0x00, "30000 100000" );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x80, "1" );
		PORT_DIPSETTING(    0x40, "2" );
		PORT_DIPSETTING(    0xc0, "3" );
		PORT_DIPSETTING(    0x00, "5" );
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_SERVICE( 0x08, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x60, 0x60, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x40, "Easy" );
		PORT_DIPSETTING(    0x60, "Normal" );
		PORT_DIPSETTING(    0x20, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x80, 0x80, "Freeze" );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		512,	/* 512 characters */
		2,	/* 2 bits per pixel */
		new int[] { 4, 0 },
		new int[] { 0, 1, 2, 3, 8+0, 8+1, 8+2, 8+3 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
		16*8	/* every char takes 16 consecutive bytes */
	);
	static GfxLayout tilelayout = new GfxLayout
	(
		16,16,	/* 16*16 tiles */
		512,	/* 512 tiles */
		3,	/* 3 bits per pixel */
		new int[] { 0, 512*32*8, 2*512*32*8 },	/* the bitplanes are separated */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7,
				16*8+0, 16*8+1, 16*8+2, 16*8+3, 16*8+4, 16*8+5, 16*8+6, 16*8+7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		32*8	/* every tile takes 32 consecutive bytes */
	);
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		512,	/* 512 sprites */
		4,	/* 4 bits per pixel */
		new int[] { 512*64*8+4, 512*64*8+0, 4, 0 },
		new int[] { 0, 1, 2, 3, 8+0, 8+1, 8+2, 8+3,
				32*8+0, 32*8+1, 32*8+2, 32*8+3, 33*8+0, 33*8+1, 33*8+2, 33*8+3 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16,
				8*16, 9*16, 10*16, 11*16, 12*16, 13*16, 14*16, 15*16 },
		64*8	/* every sprite takes 64 consecutive bytes */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,             0, 64 ),
		new GfxDecodeInfo( REGION_GFX2, 0, tilelayout,          64*4, 4*32 ),
		new GfxDecodeInfo( REGION_GFX3, 0, spritelayout, 64*4+4*32*8, 16 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static AY8910interface ay8910_interface = new AY8910interface
	(
		2,	/* 2 chips */
		1500000,	/* 1.5 MHz ? */
		new int[]{ 25, 25 },
		new ReadHandlerPtr[]{ null,null },
		new ReadHandlerPtr[]{ null,null },
		new WriteHandlerPtr[]{ null,null },
		new WriteHandlerPtr[]{ null,null }
	);

	
	
	static MachineDriver machine_driver_1942 = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				4000000,	/* 4 Mhz (?) */
				readmem,writemem,null,null,
				c1942_interrupt,2
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				3000000,	/* 3 Mhz ??? */
				sound_readmem,sound_writemem,null,null,
				interrupt,4
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		256, 64*4+4*32*8+16*16,
		c1942_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		c1942_vh_start,
		c1942_vh_stop,
		c1942_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
                    new MachineSound
                    (
				SOUND_AY8910,
				ay8910_interface
			),
		}
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_1942 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x1c000, REGION_CPU1 );/* 64k for code + 3*16k for the banked ROMs images */
		ROM_LOAD( "1-n3a.bin",    0x00000, 0x4000, 0x40201bab );
		ROM_LOAD( "1-n4.bin",     0x04000, 0x4000, 0xa60ac644 );
		ROM_LOAD( "1-n5.bin",     0x10000, 0x4000, 0x835f7b24 );
		ROM_LOAD( "1-n6.bin",     0x14000, 0x2000, 0x821c6481 );
		ROM_LOAD( "1-n7.bin",     0x18000, 0x4000, 0x5df525e1 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "1-c11.bin",    0x0000, 0x4000, 0xbd87f06b );
	
		ROM_REGION( 0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "1-f2.bin",     0x0000, 0x2000, 0x6ebca191 );/* characters */
	
		ROM_REGION( 0xc000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "2-a1.bin",     0x0000, 0x2000, 0x3884d9eb );/* tiles */
		ROM_LOAD( "2-a2.bin",     0x2000, 0x2000, 0x999cf6e0 );
		ROM_LOAD( "2-a3.bin",     0x4000, 0x2000, 0x8edb273a );
		ROM_LOAD( "2-a4.bin",     0x6000, 0x2000, 0x3a2726c3 );
		ROM_LOAD( "2-a5.bin",     0x8000, 0x2000, 0x1bd3d8bb );
		ROM_LOAD( "2-a6.bin",     0xa000, 0x2000, 0x658f02c4 );
	
		ROM_REGION( 0x10000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "2-l1.bin",     0x00000, 0x4000, 0x2528bec6 );/* sprites */
		ROM_LOAD( "2-l2.bin",     0x04000, 0x4000, 0xf89287aa );
		ROM_LOAD( "2-n1.bin",     0x08000, 0x4000, 0x024418f8 );
		ROM_LOAD( "2-n2.bin",     0x0c000, 0x4000, 0xe2c7e489 );
	
		ROM_REGION( 0x0600, REGION_PROMS );
		ROM_LOAD( "08e_sb-5.bin", 0x0000, 0x0100, 0x93ab8153 );/* red component */
		ROM_LOAD( "09e_sb-6.bin", 0x0100, 0x0100, 0x8ab44f7d );/* green component */
		ROM_LOAD( "10e_sb-7.bin", 0x0200, 0x0100, 0xf4ade9a4 );/* blue component */
		ROM_LOAD( "f01_sb-0.bin", 0x0300, 0x0100, 0x6047d91b );/* char lookup table */
		ROM_LOAD( "06d_sb-4.bin", 0x0400, 0x0100, 0x4858968d );/* tile lookup table */
		ROM_LOAD( "03k_sb-8.bin", 0x0500, 0x0100, 0xf6fad943 );/* sprite lookup table */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_1942a = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x1c000, REGION_CPU1 );/* 64k for code + 3*16k for the banked ROMs images */
		ROM_LOAD( "1-n3.bin",     0x00000, 0x4000, 0x612975f2 );
		ROM_LOAD( "1-n4.bin",     0x04000, 0x4000, 0xa60ac644 );
		ROM_LOAD( "1-n5.bin",     0x10000, 0x4000, 0x835f7b24 );
		ROM_LOAD( "1-n6.bin",     0x14000, 0x2000, 0x821c6481 );
		ROM_LOAD( "1-n7.bin",     0x18000, 0x4000, 0x5df525e1 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "1-c11.bin",    0x0000, 0x4000, 0xbd87f06b );
	
		ROM_REGION( 0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "1-f2.bin",     0x0000, 0x2000, 0x6ebca191 );/* characters */
	
		ROM_REGION( 0xc000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "2-a1.bin",     0x0000, 0x2000, 0x3884d9eb );/* tiles */
		ROM_LOAD( "2-a2.bin",     0x2000, 0x2000, 0x999cf6e0 );
		ROM_LOAD( "2-a3.bin",     0x4000, 0x2000, 0x8edb273a );
		ROM_LOAD( "2-a4.bin",     0x6000, 0x2000, 0x3a2726c3 );
		ROM_LOAD( "2-a5.bin",     0x8000, 0x2000, 0x1bd3d8bb );
		ROM_LOAD( "2-a6.bin",     0xa000, 0x2000, 0x658f02c4 );
	
		ROM_REGION( 0x10000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "2-l1.bin",     0x00000, 0x4000, 0x2528bec6 );/* sprites */
		ROM_LOAD( "2-l2.bin",     0x04000, 0x4000, 0xf89287aa );
		ROM_LOAD( "2-n1.bin",     0x08000, 0x4000, 0x024418f8 );
		ROM_LOAD( "2-n2.bin",     0x0c000, 0x4000, 0xe2c7e489 );
	
		ROM_REGION( 0x0600, REGION_PROMS );
		ROM_LOAD( "08e_sb-5.bin", 0x0000, 0x0100, 0x93ab8153 );/* red component */
		ROM_LOAD( "09e_sb-6.bin", 0x0100, 0x0100, 0x8ab44f7d );/* green component */
		ROM_LOAD( "10e_sb-7.bin", 0x0200, 0x0100, 0xf4ade9a4 );/* blue component */
		ROM_LOAD( "f01_sb-0.bin", 0x0300, 0x0100, 0x6047d91b );/* char lookup table */
		ROM_LOAD( "06d_sb-4.bin", 0x0400, 0x0100, 0x4858968d );/* tile lookup table */
		ROM_LOAD( "03k_sb-8.bin", 0x0500, 0x0100, 0xf6fad943 );/* sprite lookup table */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_1942b = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x1c000, REGION_CPU1 );/* 64k for code + 3*16k for the banked ROMs images */
		ROM_LOAD( "srb-03.n3",    0x00000, 0x4000, 0xd9dafcc3 );
		ROM_LOAD( "srb-04.n4",    0x04000, 0x4000, 0xda0cf924 );
		ROM_LOAD( "srb-05.n5",    0x10000, 0x4000, 0xd102911c );
		ROM_LOAD( "srb-06.n6",    0x14000, 0x2000, 0x466f8248 );
		ROM_LOAD( "srb-07.n7",    0x18000, 0x4000, 0x0d31038c );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "1-c11.bin",    0x0000, 0x4000, 0xbd87f06b );
	
		ROM_REGION( 0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "1-f2.bin",     0x0000, 0x2000, 0x6ebca191 );/* characters */
	
		ROM_REGION( 0xc000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "2-a1.bin",     0x0000, 0x2000, 0x3884d9eb );/* tiles */
		ROM_LOAD( "2-a2.bin",     0x2000, 0x2000, 0x999cf6e0 );
		ROM_LOAD( "2-a3.bin",     0x4000, 0x2000, 0x8edb273a );
		ROM_LOAD( "2-a4.bin",     0x6000, 0x2000, 0x3a2726c3 );
		ROM_LOAD( "2-a5.bin",     0x8000, 0x2000, 0x1bd3d8bb );
		ROM_LOAD( "2-a6.bin",     0xa000, 0x2000, 0x658f02c4 );
	
		ROM_REGION( 0x10000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "2-l1.bin",     0x00000, 0x4000, 0x2528bec6 );/* sprites */
		ROM_LOAD( "2-l2.bin",     0x04000, 0x4000, 0xf89287aa );
		ROM_LOAD( "2-n1.bin",     0x08000, 0x4000, 0x024418f8 );
		ROM_LOAD( "2-n2.bin",     0x0c000, 0x4000, 0xe2c7e489 );
	
		ROM_REGION( 0x0600, REGION_PROMS );
		ROM_LOAD( "08e_sb-5.bin", 0x0000, 0x0100, 0x93ab8153 );/* red component */
		ROM_LOAD( "09e_sb-6.bin", 0x0100, 0x0100, 0x8ab44f7d );/* green component */
		ROM_LOAD( "10e_sb-7.bin", 0x0200, 0x0100, 0xf4ade9a4 );/* blue component */
		ROM_LOAD( "f01_sb-0.bin", 0x0300, 0x0100, 0x6047d91b );/* char lookup table */
		ROM_LOAD( "06d_sb-4.bin", 0x0400, 0x0100, 0x4858968d );/* tile lookup table */
		ROM_LOAD( "03k_sb-8.bin", 0x0500, 0x0100, 0xf6fad943 );/* sprite lookup table */
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_1942	   = new GameDriver("1984"	,"1942"	,"_1942.java"	,rom_1942,null	,machine_driver_1942	,input_ports_1942	,null	,ROT270	,	"Capcom", "1942 (set 1)" );
	public static GameDriver driver_1942a	   = new GameDriver("1984"	,"1942a"	,"_1942.java"	,rom_1942a,driver_1942	,machine_driver_1942	,input_ports_1942	,null	,ROT270	,	"Capcom", "1942 (set 2)" );
	public static GameDriver driver_1942b	   = new GameDriver("1984"	,"1942b"	,"_1942.java"	,rom_1942b,driver_1942	,machine_driver_1942	,input_ports_1942	,null	,ROT270	,	"Capcom", "1942 (set 3)" );
}
