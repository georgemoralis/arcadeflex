/***************************************************************************

Exciting Soccer - (c) 1983 Alpha Denshi Co.

Supported sets:
Exciting Soccer - Alpha Denshi
Exciting Soccer (bootleg) - Kazutomi


Preliminary driver by:
Ernesto Corvi
ernesto@imagina.com

Jarek Parchanski
jpdev@friko6.onet.pl


NOTES:
The game supports Coin 2, but the dip switches used for it are the same
as Coin 1. Basically, this allowed to select an alternative coin table
based on wich Coin input was connected.

KNOWN ISSUES/TODO:
- Cocktail mode is unsupported.

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;

public class exctsccr
{
	
	/* from vidhrdw */
	
	/* from machine */
	
	
	public static WriteHandlerPtr exctsccr_DAC_data_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		DAC_signed_data_w(offset,data << 2);
	} };
	
	
	/***************************************************************************
	
		Memory definition(s)
	
	***************************************************************************/
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x5fff, MRA_ROM ),
		new MemoryReadAddress( 0x6000, 0x63ff, MRA_RAM ), /* Alpha mcu (protection) */
		new MemoryReadAddress( 0x7c00, 0x7fff, MRA_RAM ), /* work ram */
		new MemoryReadAddress( 0x8000, 0x83ff, videoram_r ),
		new MemoryReadAddress( 0x8400, 0x87ff, colorram_r ),
		new MemoryReadAddress( 0x8800, 0x8bff, MRA_RAM ), /* ??? */
		new MemoryReadAddress( 0xa000, 0xa000, input_port_0_r ),
		new MemoryReadAddress( 0xa040, 0xa040, input_port_1_r ),
		new MemoryReadAddress( 0xa080, 0xa080, input_port_3_r ),
		new MemoryReadAddress( 0xa0c0, 0xa0c0, input_port_2_r ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x5fff, MWA_ROM ),
		new MemoryWriteAddress( 0x6000, 0x63ff, exctsccr_mcu_w, exctsccr_mcu_ram ), /* Alpha mcu (protection) */
		new MemoryWriteAddress( 0x7c00, 0x7fff, MWA_RAM ), /* work ram */
		new MemoryWriteAddress( 0x8000, 0x83ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x8400, 0x87ff, colorram_w, colorram ),
		new MemoryWriteAddress( 0x8800, 0x8bff, MWA_RAM ), /* ??? */
		new MemoryWriteAddress( 0xa000, 0xa000, MWA_NOP ), /* ??? */
		new MemoryWriteAddress( 0xa001, 0xa001, MWA_NOP ), /* ??? */
		new MemoryWriteAddress( 0xa002, 0xa002, exctsccr_gfx_bank_w ),
		new MemoryWriteAddress( 0xa003, 0xa003, MWA_NOP ), /* Cocktail mode ( 0xff = flip screen, 0x00 = normal ) */
		new MemoryWriteAddress( 0xa006, 0xa006, exctsccr_mcu_control_w ), /* MCU control */
		new MemoryWriteAddress( 0xa007, 0xa007, MWA_NOP ), /* This is also MCU control, but i dont need it */
		new MemoryWriteAddress( 0xa040, 0xa06f, MWA_RAM, spriteram ), /* Sprite pos */
		new MemoryWriteAddress( 0xa080, 0xa080, soundlatch_w ),
		new MemoryWriteAddress( 0xa0c0, 0xa0c0, watchdog_reset_w ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x8fff, MRA_ROM ),
		new MemoryReadAddress( 0xa000, 0xa7ff, MRA_RAM ),
		new MemoryReadAddress( 0xc00d, 0xc00d, soundlatch_r ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x8fff, MWA_ROM ),
		new MemoryWriteAddress( 0xa000, 0xa7ff, MWA_RAM ),
		new MemoryWriteAddress( 0xc008, 0xc009, exctsccr_DAC_data_w ),
		new MemoryWriteAddress( 0xc00c, 0xc00c, soundlatch_w ), /* used to clear the latch */
		new MemoryWriteAddress( 0xc00f, 0xc00f, MWA_NOP ), /* ??? */
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static IOWritePort sound_writeport[] =
	{
		new IOWritePort( 0x82, 0x82, AY8910_write_port_0_w ),
		new IOWritePort( 0x83, 0x83, AY8910_control_port_0_w ),
		new IOWritePort( 0x86, 0x86, AY8910_write_port_1_w ),
		new IOWritePort( 0x87, 0x87, AY8910_control_port_1_w ),
		new IOWritePort( 0x8a, 0x8a, AY8910_write_port_2_w ),
		new IOWritePort( 0x8b, 0x8b, AY8910_control_port_2_w ),
		new IOWritePort( 0x8e, 0x8e, AY8910_write_port_3_w ),
		new IOWritePort( 0x8f, 0x8f, AY8910_control_port_3_w ),
		new IOWritePort( -1 )	/* end of table */
	};
	
	/* Bootleg */
	static MemoryReadAddress bl_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x5fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0x83ff, videoram_r ),
		new MemoryReadAddress( 0x8400, 0x87ff, colorram_r ),
		new MemoryReadAddress( 0x8800, 0x8fff, MRA_RAM ), /* ??? */
		new MemoryReadAddress( 0xa000, 0xa000, input_port_0_r ),
		new MemoryReadAddress( 0xa040, 0xa040, input_port_1_r ),
		new MemoryReadAddress( 0xa080, 0xa080, input_port_3_r ),
		new MemoryReadAddress( 0xa0c0, 0xa0c0, input_port_2_r ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress bl_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x5fff, MWA_ROM ),
		new MemoryWriteAddress( 0x7000, 0x7000, AY8910_write_port_0_w ),
		new MemoryWriteAddress( 0x7001, 0x7001, AY8910_control_port_0_w ),
		new MemoryWriteAddress( 0x8000, 0x83ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x8400, 0x87ff, colorram_w, colorram ),
		new MemoryWriteAddress( 0x8800, 0x8fff, MWA_RAM ), /* ??? */
		new MemoryWriteAddress( 0xa000, 0xa000, MWA_NOP ), /* ??? */
		new MemoryWriteAddress( 0xa001, 0xa001, MWA_NOP ), /* ??? */
		new MemoryWriteAddress( 0xa002, 0xa002, exctsccr_gfx_bank_w ), /* ??? */
		new MemoryWriteAddress( 0xa003, 0xa003, MWA_NOP ), /* Cocktail mode ( 0xff = flip screen, 0x00 = normal ) */
		new MemoryWriteAddress( 0xa006, 0xa006, MWA_NOP ), /* no MCU, but some leftover code still writes here */
		new MemoryWriteAddress( 0xa007, 0xa007, MWA_NOP ), /* no MCU, but some leftover code still writes here */
		new MemoryWriteAddress( 0xa040, 0xa06f, MWA_RAM, spriteram ), /* Sprite Pos */
		new MemoryWriteAddress( 0xa080, 0xa080, soundlatch_w ),
		new MemoryWriteAddress( 0xa0c0, 0xa0c0, watchdog_reset_w ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress bl_sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x5fff, MRA_ROM ),
		new MemoryReadAddress( 0x6000, 0x6000, soundlatch_r ),
		new MemoryReadAddress( 0xe000, 0xe3ff, MRA_RAM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress bl_sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x5fff, MWA_ROM ),
		new MemoryWriteAddress( 0x8000, 0x8000, MWA_NOP ), /* 0 = DAC sound off, 1 = DAC sound on */
		new MemoryWriteAddress( 0xa000, 0xa000, soundlatch_w ), /* used to clear the latch */
		new MemoryWriteAddress( 0xc000, 0xc000, exctsccr_DAC_data_w ),
		new MemoryWriteAddress( 0xe000, 0xe3ff, MWA_RAM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	/***************************************************************************
	
		Input port(s)
	
	***************************************************************************/
	
	static InputPortPtr input_ports_exctsccr = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP   | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT| IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* DSW0 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x03, "A 1C/1C B 3C/1C" );
		PORT_DIPSETTING(    0x01, "A 1C/2C B 1C/4C" );
		PORT_DIPSETTING(    0x00, "A 1C/3C B 1C/6C" );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x10, "Easy" );
		PORT_DIPSETTING(    0x00, "Hard" );
		PORT_DIPNAME( 0x60, 0x00, "Game Time" );
		PORT_DIPSETTING(    0x20, "1 Min." );
		PORT_DIPSETTING(    0x00, "2 Min." );
		PORT_DIPSETTING(    0x60, "3 Min." );
		PORT_DIPSETTING(    0x40, "4 Min." );
		PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* Has to be 0 */
	INPUT_PORTS_END(); }}; 
	
	/***************************************************************************
	
		Graphic(s) decoding
	
	***************************************************************************/
	
	static GfxLayout charlayout1 = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		256,	/* 256 characters */
		3,		/* 3 bits per pixel */
		new int[] { 0x4000*8+4, 0, 4 },	/* plane offset */
		new int[] { 8*8+0, 8*8+1, 8*8+2, 8*8+3, 0, 1, 2, 3 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		16*8	/* every char takes 16 consecutive bytes */
	);
	
	static GfxLayout charlayout2 = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		256,	/* 256 characters */
		3,		/* 3 bits per pixel */
		new int[] { 0x2000*8, 0, 4 },	/* plane offset */
		new int[] { 8*8+0, 8*8+1, 8*8+2, 8*8+3, 0, 1, 2, 3 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		16*8	/* every char takes 16 consecutive bytes */
	);
	
	static GfxLayout spritelayout1 = new GfxLayout
	(
		16,16,	    /* 16*16 sprites */
		64,	        /* 64 sprites */
		3,	        /* 3 bits per pixel */
		new int[] { 0x4000*8+4, 0, 4 },	/* plane offset */
		new int[] { 8*8, 8*8+1, 8*8+2, 8*8+3, 16*8+0, 16*8+1, 16*8+2, 16*8+3,
				24*8+0, 24*8+1, 24*8+2, 24*8+3, 0, 1, 2, 3  },
		new int[] { 0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
				32 * 8, 33 * 8, 34 * 8, 35 * 8, 36 * 8, 37 * 8, 38 * 8, 39 * 8 },
		64*8	/* every sprite takes 64 bytes */
	);
	
	static GfxLayout spritelayout2 = new GfxLayout
	(
		16,16,	    /* 16*16 sprites */
		64,         /* 64 sprites */
		3,	        /* 3 bits per pixel */
		new int[] { 0x2000*8, 0, 4 },	/* plane offset */
		new int[] { 8*8, 8*8+1, 8*8+2, 8*8+3, 16*8+0, 16*8+1, 16*8+2, 16*8+3,
				24*8+0, 24*8+1, 24*8+2, 24*8+3, 0, 1, 2, 3  },
		new int[] { 0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
				32 * 8, 33 * 8, 34 * 8, 35 * 8, 36 * 8, 37 * 8, 38 * 8, 39 * 8 },
		64*8	/* every sprite takes 64 bytes */
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,		/* 16*16 sprites */
		64,	    	/* 64 sprites */
		3,	    	/* 2 bits per pixel */
		new int[] { 0x1000*8+4, 0, 4 },	/* plane offset */
		new int[] { 8*8, 8*8+1, 8*8+2, 8*8+3, 16*8+0, 16*8+1, 16*8+2, 16*8+3,
				24*8+0, 24*8+1, 24*8+2, 24*8+3, 0, 1, 2, 3  },
		new int[] { 0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
				32 * 8, 33 * 8, 34 * 8, 35 * 8, 36 * 8, 37 * 8, 38 * 8, 39 * 8 },
		64*8	/* every sprite takes 64 bytes */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0x0000, charlayout1,      0, 32 ), /* chars */
		new GfxDecodeInfo( REGION_GFX1, 0x2000, charlayout2,      0, 32 ), /* chars */
		new GfxDecodeInfo( REGION_GFX1, 0x1000, spritelayout1, 16*8, 32 ), /* sprites */
		new GfxDecodeInfo( REGION_GFX1, 0x3000, spritelayout2, 16*8, 32 ), /* sprites */
		new GfxDecodeInfo( REGION_GFX2, 0x0000, spritelayout,  16*8, 32 ), /* sprites */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	/***************************************************************************
	
		Sound interface(s)
	
	***************************************************************************/
	
	static struct AY8910interface ay8910_interface =
	{
		4,	/* 4 chips */
		1500000,	/* 1.5 MHz ? */
		{ 15, 15, 15, 15 }, /* volume */
		{ 0, 0, 0, 0 },
		{ 0, 0, 0, 0 },
		{ 0, 0, 0, 0 }, /* it writes 0s thru port A, no clue what for */
		{ 0, 0, 0, 0 }
	};
	
	static DACinterface dac_interface = new DACinterface
	(
		2,
		new int[] { 50, 50 }
	);
	
	/* Bootleg */
	static struct AY8910interface bl_ay8910_interface =
	{
		1,	/* 1 chip */
		1500000,	/* 1.5 MHz ? */
		{ 50 }, /* volume */
		{ 0 },
		{ 0 },
		{ 0 },
		{ 0 }
	};
	
	static DACinterface bl_dac_interface = new DACinterface
	(
		1,
		new int[] { 100 }
	);
	
	/***************************************************************************
	
		Machine driver(s)
	
	***************************************************************************/
	
	static MachineDriver machine_driver_exctsccr = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				4000000,	/* 4.0 Mhz (?) */
				readmem,writemem,null,null,
				interrupt,1
			),
			new MachineCPU(
				CPU_Z80,
				4123456,	/* ??? with 4 MHz, nested NMIs might happen */
				sound_readmem,sound_writemem,0,sound_writeport,
				ignore_interrupt,0,
				nmi_interrupt, 4000 /* 4 khz, updates the dac */
			),
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		32, 64*8,
		exctsccr_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		exctsccr_vh_start,
		exctsccr_vh_stop,
		exctsccr_vh_screenrefresh,
	
		/* sound hardware */
		0, 0, 0, 0,
		new MachineSound[] {
			new MachineSound(
				SOUND_AY8910,
				ay8910_interface
			),
			new MachineSound(
				SOUND_DAC,
				dac_interface
			)
		}
	);
	
	/* Bootleg */
	static MachineDriver machine_driver_exctsccb = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				4000000,	/* 4.0 Mhz (?) */
				bl_readmem,bl_writemem,null,null,
				interrupt,1
			),
			new MachineCPU(
				CPU_Z80,
				3072000,	/* 3.072 Mhz ? */
				bl_sound_readmem,bl_sound_writemem,null,null,
				ignore_interrupt,0
			),
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		32, 64*8,
		exctsccr_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		generic_vh_start,
		exctsccr_vh_stop,
		exctsccr_vh_screenrefresh,
	
		/* sound hardware */
		0, 0, 0, 0,
		new MachineSound[] {
			new MachineSound(
				SOUND_AY8910,
				bl_ay8910_interface
			),
			new MachineSound(
				SOUND_DAC,
				bl_dac_interface
			)
		}
	);
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_exctsccr = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "1_g10.bin",    0x0000, 0x2000, 0xaa68df66 );
		ROM_LOAD( "2_h10.bin",    0x2000, 0x2000, 0x2d8f8326 );
		ROM_LOAD( "3_j10.bin",    0x4000, 0x2000, 0xdce4a04d );
	
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for code */
		ROM_LOAD( "0_h6.bin",     0x0000, 0x2000, 0x3babbd6b );
		ROM_LOAD( "9_f6.bin",     0x2000, 0x2000, 0x639998f5 );
		ROM_LOAD( "8_d6.bin",     0x4000, 0x2000, 0x88651ee1 );
		ROM_LOAD( "7_c6.bin",     0x6000, 0x2000, 0x6d51521e );
		ROM_LOAD( "1_a6.bin",     0x8000, 0x1000, 0x20f2207e );
	
		ROM_REGION( 0x06000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "4_a5.bin",     0x0000, 0x2000, 0xc342229b );
		ROM_LOAD( "5_b5.bin",     0x2000, 0x2000, 0x35f4f8c9 );
		ROM_LOAD( "6_c5.bin",     0x4000, 0x2000, 0xeda40e32 );
	
		ROM_REGION( 0x02000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "2_k5.bin",     0x0000, 0x1000, 0x7f9cace2 );
		ROM_LOAD( "3_l5.bin",     0x1000, 0x1000, 0xdb2d9e0d );
	
		ROM_REGION( 0x0220, REGION_PROMS );
		ROM_LOAD( "prom1.e1",     0x0000, 0x0020, 0xd9b10bf0 );/* palette */
		ROM_LOAD( "prom2.8r",     0x0020, 0x0100, 0x8a9c0edf );/* lookup table */
		ROM_LOAD( "prom3.k5",     0x0120, 0x0100, 0xb5db1c2c );/* lookup table */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_exctscca = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "1_g10.bin",    0x0000, 0x2000, 0xaa68df66 );
		ROM_LOAD( "2_h10.bin",    0x2000, 0x2000, 0x2d8f8326 );
		ROM_LOAD( "3_j10.bin",    0x4000, 0x2000, 0xdce4a04d );
	
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for code */
		ROM_LOAD( "exctsccc.000", 0x0000, 0x2000, 0x642fc42f );
		ROM_LOAD( "exctsccc.009", 0x2000, 0x2000, 0xd88b3236 );
		ROM_LOAD( "8_d6.bin",     0x4000, 0x2000, 0x88651ee1 );
		ROM_LOAD( "7_c6.bin",     0x6000, 0x2000, 0x6d51521e );
		ROM_LOAD( "1_a6.bin",     0x8000, 0x1000, 0x20f2207e );
	
		ROM_REGION( 0x06000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "4_a5.bin",     0x0000, 0x2000, 0xc342229b );
		ROM_LOAD( "5_b5.bin",     0x2000, 0x2000, 0x35f4f8c9 );
		ROM_LOAD( "6_c5.bin",     0x4000, 0x2000, 0xeda40e32 );
	
		ROM_REGION( 0x02000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "2_k5.bin",     0x0000, 0x1000, 0x7f9cace2 );
		ROM_LOAD( "3_l5.bin",     0x1000, 0x1000, 0xdb2d9e0d );
	
		ROM_REGION( 0x0220, REGION_PROMS );
		ROM_LOAD( "prom1.e1",     0x0000, 0x0020, 0xd9b10bf0 );/* palette */
		ROM_LOAD( "prom2.8r",     0x0020, 0x0100, 0x8a9c0edf );/* lookup table */
		ROM_LOAD( "prom3.k5",     0x0120, 0x0100, 0xb5db1c2c );/* lookup table */
	ROM_END(); }}; 
	
	/* Bootleg */
	static RomLoadPtr rom_exctsccb = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "es-1.e2",      0x0000, 0x2000, 0x997c6a82 );
		ROM_LOAD( "es-2.g2",      0x2000, 0x2000, 0x5c66e792 );
		ROM_LOAD( "es-3.h2",      0x4000, 0x2000, 0xe0d504c0 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* sound */
		ROM_LOAD( "es-a.k2",      0x0000, 0x2000, 0x99e87b78 );
		ROM_LOAD( "es-b.l2",      0x2000, 0x2000, 0x8b3db794 );
		ROM_LOAD( "es-c.m2",      0x4000, 0x2000, 0x7bed2f81 );
	
		ROM_REGION( 0x06000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		/* I'm using the ROMs from exctscc2, national flags would be wrong otherwise */
		ROM_LOAD( "vr.5a",        0x0000, 0x2000, BADCRC( 0x4ff1783d );)
		ROM_LOAD( "vr.5b",        0x2000, 0x2000, BADCRC( 0x5605b60b );)
		ROM_LOAD( "vr.5c",        0x4000, 0x2000, BADCRC( 0x1fb84ee6 );)
	
		ROM_REGION( 0x02000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "vr.5k",        0x0000, 0x1000, BADCRC( 0x1d37edfa );)
		ROM_LOAD( "vr.5l",        0x1000, 0x1000, BADCRC( 0xb97f396c );)
	
		ROM_REGION( 0x0220, REGION_PROMS );
		ROM_LOAD( "prom1.e1",     0x0000, 0x0020, 0xd9b10bf0 );/* palette */
		ROM_LOAD( "prom2.8r",     0x0020, 0x0100, 0x8a9c0edf );/* lookup table */
		ROM_LOAD( "prom3.k5",     0x0120, 0x0100, 0xb5db1c2c );/* lookup table */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_exctscc2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "vr.3j",        0x0000, 0x2000, 0xc6115362 );
		ROM_LOAD( "vr.3k",        0x2000, 0x2000, 0xde36ba00 );
		ROM_LOAD( "vr.3l",        0x4000, 0x2000, 0x1ddfdf65 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for code */
		ROM_LOAD( "vr.7d",        0x0000, 0x2000, 0x2c675a43 );
		ROM_LOAD( "vr.7e",        0x2000, 0x2000, 0xe571873d );
		ROM_LOAD( "8_d6.bin",     0x4000, 0x2000, 0x88651ee1 );/* vr.7f */
		ROM_LOAD( "7_c6.bin",     0x6000, 0x2000, 0x6d51521e );/* vr.7h */
		ROM_LOAD( "1_a6.bin",     0x8000, 0x1000, 0x20f2207e );/* vr.7k */
	
		ROM_REGION( 0x06000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "vr.5a",        0x0000, 0x2000, 0x4ff1783d );
		ROM_LOAD( "vr.5b",        0x2000, 0x2000, 0x5605b60b );
		ROM_LOAD( "vr.5c",        0x4000, 0x2000, 0x1fb84ee6 );
	
		ROM_REGION( 0x02000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "vr.5k",        0x0000, 0x1000, 0x1d37edfa );
		ROM_LOAD( "vr.5l",        0x1000, 0x1000, 0xb97f396c );
	
		ROM_REGION( 0x0220, REGION_PROMS );
		ROM_LOAD( "prom1.e1",     0x0000, 0x0020, 0xd9b10bf0 );/* palette */
		ROM_LOAD( "prom2.8r",     0x0020, 0x0100, 0x8a9c0edf );/* lookup table */
		ROM_LOAD( "prom3.k5",     0x0120, 0x0100, 0xb5db1c2c );/* lookup table */
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_exctsccr	   = new GameDriver("1983"	,"exctsccr"	,"exctsccr.java"	,rom_exctsccr,null	,machine_driver_exctsccr	,input_ports_exctsccr	,null	,ROT90	,	"Alpha Denshi Co", "Exciting Soccer", GAME_NO_COCKTAIL )
	public static GameDriver driver_exctscca	   = new GameDriver("1983"	,"exctscca"	,"exctsccr.java"	,rom_exctscca,driver_exctsccr	,machine_driver_exctsccr	,input_ports_exctsccr	,null	,ROT90	,	"Alpha Denshi Co", "Exciting Soccer (alternate music)", GAME_NO_COCKTAIL )
	public static GameDriver driver_exctsccb	   = new GameDriver("1984"	,"exctsccb"	,"exctsccr.java"	,rom_exctsccb,driver_exctsccr	,machine_driver_exctsccb	,input_ports_exctsccr	,null	,ROT90	,	"bootleg", "Exciting Soccer (bootleg)", GAME_NO_COCKTAIL )
	public static GameDriver driver_exctscc2	   = new GameDriver("1983"	,"exctscc2"	,"exctsccr.java"	,rom_exctscc2,driver_exctsccr	,machine_driver_exctsccr	,input_ports_exctsccr	,null	,ROT90	,	"Alpha Denshi Co", "Exciting Soccer II", GAME_NOT_WORKING | GAME_NO_COCKTAIL )
}
