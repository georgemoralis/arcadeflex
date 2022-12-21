/***************************************************************************

Football Champ - (C) 1990 Taito Corporation.

Preliminary driver by:

Ernesto Corvi
<ernesto@balancesoftware.com>

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;

public class footchmp
{
	
	/* from sndhrdw/rastan */
	
	/* from vidhrdw */
	
	/* Input ports handling */
	public static ReadHandlerPtr footchmp_input_r = new ReadHandlerPtr() { public int handler(int offset){
	
		switch ( offset ) {
			case 0x00:
				return readinputport( 0 ); /* DSW A */
			break;
	
			case 0x02:
				return readinputport( 1 ); /* DSW B */
			break;
	
			case 0x04:
				return readinputport( 2 ); /* IN0 */
			break;
	
			/* 0x06 ?? */
			/* 0x08 ?? */
	
			case 0x0a:
				return readinputport( 3 ); /* IN1 */
			break;
	
			case 0x0c:
				return readinputport( 4 ); /* IN2 */
			break;
	
			case 0x0e:
				return readinputport( 5 ); /* IN3 */
			break;
	
			case 0x10:
				return readinputport( 6 ); /* IN4 */
			break;
		}
	
		return 0xff;
	} };
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x10ffff, MRA_BANK1 ), /* work ram */
		new MemoryReadAddress( 0x200000, 0x20ffff, footchmp_spriteram_r ), /* sprite ram */
		new MemoryReadAddress( 0x400000, 0x400fff, footchmp_layer0ram_r ), /* videoram? */
		new MemoryReadAddress( 0x401000, 0x401fff, footchmp_layer1ram_r ), /* videoram? */
		new MemoryReadAddress( 0x402000, 0x402fff, footchmp_layer2ram_r ), /* videoram? */
		new MemoryReadAddress( 0x403000, 0x403fff, footchmp_layer3ram_r ), /* videoram? */
		new MemoryReadAddress( 0x40c000, 0x40dfff, footchmp_textram_r ), /* text layer */
		new MemoryReadAddress( 0x40e000, 0x40ffff, footchmp_chargen_r ), /* character generator */
	//	new MemoryReadAddress( 0x430000, 0x43002f, MRA_BANK3 ), /* video control (scroll,etc)? */
		new MemoryReadAddress( 0x600000, 0x601fff, paletteram_word_r ), /* palette */
		new MemoryReadAddress( 0x700000, 0x700011, footchmp_input_r ), /* inputs */
		new MemoryReadAddress( 0xa00000, 0xa00003, rastan_sound_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x10ffff, MWA_BANK1 ), /* work ram */
		new MemoryWriteAddress( 0x200000, 0x20ffff, footchmp_spriteram_w, spriteram ), /* sprite ram */
		new MemoryWriteAddress( 0x300000, 0x30000f, footchmp_spritebank_w ),
		new MemoryWriteAddress( 0x500002, 0x500003, MWA_NOP ), /* watchdog? */
		new MemoryWriteAddress( 0x400000, 0x400fff, footchmp_layer0ram_w, footchmp_layer0_ram ), /* videoram? */
		new MemoryWriteAddress( 0x401000, 0x401fff, footchmp_layer1ram_w, footchmp_layer1_ram ), /* videoram? */
		new MemoryWriteAddress( 0x402000, 0x402fff, footchmp_layer2ram_w, footchmp_layer2_ram ), /* videoram? */
		new MemoryWriteAddress( 0x403000, 0x403fff, footchmp_layer3ram_w, footchmp_layer3_ram ), /* videoram? */
		new MemoryWriteAddress( 0x404000, 0x4043ff, MWA_NOP ), /* ??? */
		new MemoryWriteAddress( 0x40c000, 0x40dfff, footchmp_textram_w, footchmp_text_ram ), /* text layer */
		new MemoryWriteAddress( 0x40e000, 0x40ffff, footchmp_chargen_w, footchmp_chargen_ram ), /* character generator */
		new MemoryWriteAddress( 0x430000, 0x43002f, footchmp_scroll_w ), /* video control (scroll,etc)? */
		new MemoryWriteAddress( 0x600000, 0x601fff, paletteram_RRRRGGGGBBBBxxxx_word_w, paletteram ), /* palette */
		new MemoryWriteAddress( 0x800000, 0x800001, MWA_NOP ), /* irq ack? */
		new MemoryWriteAddress( 0xa00000, 0xa00003, rastan_sound_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static void sound_bankswitch_w (int offset, int data)
	{
		unsigned char *RAM = memory_region(REGION_CPU2);
		int banknum = (data - 1) & 3;
	
		cpu_setbank (2, &RAM [0x10000 + (banknum * 0x4000)]);
	}
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new MemoryReadAddress( 0x4000, 0x7fff, MRA_BANK2 ),
		new MemoryReadAddress( 0xc000, 0xdfff, MRA_RAM ),
		new MemoryReadAddress( 0xe000, 0xe000, YM2610_status_port_0_A_r ),
		new MemoryReadAddress( 0xe001, 0xe001, YM2610_read_port_0_r ),
		new MemoryReadAddress( 0xe002, 0xe002, YM2610_status_port_0_B_r ),
		new MemoryReadAddress( 0xe200, 0xe200, MRA_NOP ),
		new MemoryReadAddress( 0xe201, 0xe201, r_rd_a001 ),
		new MemoryReadAddress( 0xea00, 0xea00, MRA_NOP ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0xc000, 0xdfff, MWA_RAM ),
		new MemoryWriteAddress( 0xe000, 0xe000, YM2610_control_port_0_A_w ),
		new MemoryWriteAddress( 0xe001, 0xe001, YM2610_data_port_0_A_w ),
		new MemoryWriteAddress( 0xe002, 0xe002, YM2610_control_port_0_B_w ),
		new MemoryWriteAddress( 0xe003, 0xe003, YM2610_data_port_0_B_w ),
		new MemoryWriteAddress( 0xe200, 0xe200, r_wr_a000 ),
		new MemoryWriteAddress( 0xe201, 0xe201, r_wr_a001 ),
		new MemoryWriteAddress( 0xe400, 0xe403, MWA_NOP ), /* pan */
		new MemoryWriteAddress( 0xee00, 0xee00, MWA_NOP ), /* ? */
		new MemoryWriteAddress( 0xf000, 0xf000, MWA_NOP ), /* ? */
		new MemoryWriteAddress( 0xf200, 0xf200, sound_bankswitch_w ),	/* ?? */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static InputPortPtr input_ports_footchmp = new InputPortPtr(){ public void handler() { 
		PORT_START();  /* DSW A */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
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
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();  /* DSW B */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x10, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x30, "2 Players" );
		PORT_DIPSETTING(    0x10, "4 Players" );
		PORT_DIPSETTING(    0x20, "4 Players (No Select); )
	//	PORT_DIPSETTING(    0x00, "4 Players" );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, "Game Version" );
		PORT_DIPSETTING(    0x00, "Normal" );
		PORT_DIPSETTING(    0x80, "European" );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN4 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_SERVICE );/* P1 service */
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );	/* P3 service */
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );	/* P4 service */
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_TILT );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
	
		PORT_START();       /* IN3 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START3 );
	
		PORT_START();       /* IN4 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER4 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER4 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER4 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER4 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START4 );
	INPUT_PORTS_END(); }}; 
	
	
	#define XOFFS (0x80000*8)
	static GfxLayout tilelayout = new GfxLayout
	(
		16,16,	/* 16*16 tiles */
		8192,	/* 8192 tiles */
		4,		/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },
		new int[] { 1*4, 0*4, 3*4, 2*4, XOFFS + 1*4, XOFFS + 0*4, XOFFS + 3*4, XOFFS + 2*4, 5*4, 4*4, 7*4, 6*4, XOFFS + 5*4, XOFFS + 4*4, XOFFS + 7*4, XOFFS + 6*4 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32, 8*32, 9*32, 10*32, 11*32, 12*32, 13*32, 14*32, 15*32 },
		64*8	/* every tile takes 64 consecutive bytes */
	);
	#undef XOFFS
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		16384,	/* 16384 sprites */
		4,		/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },
		new int[] { 1*4, 0*4, 3*4, 2*4, 5*4, 4*4, 7*4, 6*4, 9*4, 8*4, 11*4, 10*4, 13*4, 12*4, 15*4, 14*4 },
		new int[] { 0*64, 1*64, 2*64, 3*64, 4*64, 5*64, 6*64, 7*64, 8*64, 9*64, 10*64, 11*64, 12*64, 13*64, 14*64, 15*64 },
		128*8	/* every sprite takes 128 consecutive bytes */
	);
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		256,	/* 256 characters */
		4,		/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },
		new int[] { 3*4, 2*4, 1*4, 0*4, 7*4, 6*4, 5*4, 4*4 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8	/* every character takes 32 consecutive bytes */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, tilelayout,   0, 256 ),	/* tiles */
		new GfxDecodeInfo( REGION_GFX2, 0, spritelayout, 0, 256 ),	/* sprites */
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,   0, 128 ),	/* chars - generated at run time */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	/* handler called by the YM2610 emulator when the internal timers cause an IRQ */
	static void irqhandler(int irq)
	{
		cpu_set_irq_line(1,0,irq ? ASSERT_LINE : CLEAR_LINE);
	}
	
	static struct YM2610interface ym2610_interface =
	{
		1,	/* 1 chip */
		8000000,	/* 8 MHz ?????? */
		{ 30 },
		{ 0 },
		{ 0 },
		{ 0 },
		{ 0 },
		{ irqhandler },
		{ REGION_SOUND1 },
		{ REGION_SOUND1 },
		{ YM3012_VOL(60,MIXER_PAN_LEFT,60,MIXER_PAN_RIGHT) }
	};
	
	static int footchmp_irq( void ) {
	
		/* the hardware must execute a irq5, then it sits waiting for an irq6 to happen */
		/* i dont know the source of the ints, so i just trigger one every time */
	
		if (cpu_getiloops() == 0)
			return m68_level5_irq();
	
		return m68_level6_irq();
	}
	
	static MachineDriver machine_driver_footchmp = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				12000000,	/* 6 Mhz ??? */
				readmem,writemem,null,null,
				footchmp_irq,2
			),
			new MachineCPU(
				CPU_Z80,
				4000000,	/* 4 MHz ??? */
				sound_readmem, sound_writemem,null,null,
				ignore_interrupt,0	/* IRQs are triggered by the YM2610 */
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,
		null,
	
		/* video hardware */
		40*8, 32*8, new rectangle( 0*8, 40*8-1, 0*8, 32*8-1 ),
	
		gfxdecodeinfo,
		4096,4096,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		footchmp_vh_start,
		footchmp_vh_stop,
		footchmp_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2610,
				ym2610_interface
			)
		}
	);
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_footchmp = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1 );    /* 512k for 68000 code */
		ROM_LOAD_EVEN( "efc6.bin", 0x00000, 0x20000, 0xf78630fb )
		ROM_LOAD_ODD ( "efc4.bin", 0x00000, 0x20000, 0x32c109cb )
		ROM_LOAD_EVEN( "efc7.bin", 0x40000, 0x20000, 0x80d46fef )
		ROM_LOAD_ODD ( "efc5.bin", 0x40000, 0x20000, 0x40ac4828 )
	
		ROM_REGION( 0x1c000, REGION_CPU2 );    /* 64k for Z80 code */
		ROM_LOAD( "efc70.bin", 0x00000, 0x04000, 0x05aa7fd7 );
		ROM_CONTINUE(		   0x10000, 0x0c000 );
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "efc1.bin",	0x000000, 0x080000, 0x9a17fe8c );
		ROM_LOAD( "efc2.bin",	0x080000, 0x080000, 0xacde7071 );
	
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "efc9.bin",	0x000000, 0x100000, 0xf43782e6 );
		ROM_LOAD( "efc10.bin",	0x100000, 0x100000, 0x060a8b61 );
	
		ROM_REGION( 0x100000, REGION_SOUND1 );    /* YM2610 samples */
		ROM_LOAD( "efc57.bin", 0x00000, 0x100000, 0x609938d5 );
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_footchmp	   = new GameDriver("1990"	,"footchmp"	,"footchmp.java"	,rom_footchmp,null	,machine_driver_footchmp	,input_ports_footchmp	,null	,ROT0	,	"Taito Corporation Japan", "Football Champ (World)" )
}
