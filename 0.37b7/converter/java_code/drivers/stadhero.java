/***************************************************************************

	Stadium Hero (Japan)			(c) 1988 Data East Corporation

	Emulation by Bryan McPhail, mish@tendril.co.uk

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class stadhero
{
	
	/* Video emulation definitions */
	
	extern UBytePtr stadhero_pf1_data,*stadhero_pf2_data;
	
	
	/******************************************************************************/
	
	public static ReadHandlerPtr stadhero_control_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch (offset)
		{
			case 0: /* Player 1 & 2 joystick & buttons */
				return (readinputport(0) + (readinputport(1) << 8));
	
			case 2: /* Credits, start buttons */
				return readinputport(2) | (readinputport(2)<<8);
	
			case 4: /* Byte 4: Dipswitch bank 2, Byte 5: Dipswitch Bank 1 */
				return (readinputport(3) + (readinputport(4) << 8));
		}
	
		logerror("CPU #0 PC %06x: warning - read unmapped memory address %06x\n",cpu_get_pc(),0x30c000+offset);
		return 0xffff;
	} };
	
	public static WriteHandlerPtr stadhero_control_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		switch (offset)
		{
			case 4: /* Interrupt ack (VBL - IRQ 5) */
				break;
			case 6: /* 6502 sound cpu */
				soundlatch_w.handler(0,data & 0xff);
				cpu_cause_interrupt(1,M6502_INT_NMI);
				break;
			default:
				logerror("CPU #0 PC %06x: warning - write %02x to unmapped memory address %06x\n",cpu_get_pc(),data,0x30c010+offset);
				break;
		}
	} };
	
	public static WriteHandlerPtr spriteram_mirror_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		WRITE_WORD(&spriteram.read(offset),data);
	} };
	
	/******************************************************************************/
	
	static MemoryReadAddress stadhero_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x01ffff, MRA_ROM ),
		new MemoryReadAddress( 0x200000, 0x2007ff, stadhero_pf1_data_r ),
		new MemoryReadAddress( 0x260000, 0x261fff, stadhero_pf2_data_r ),
		new MemoryReadAddress( 0x30c000, 0x30c00b, stadhero_control_r ),
		new MemoryReadAddress( 0x310000, 0x3107ff, paletteram_word_r ),
		new MemoryReadAddress( 0xff8000, 0xffbfff, MRA_BANK1 ), /* Main ram */
		new MemoryReadAddress( 0xffc000, 0xffc7ff, MRA_BANK2 ), /* Sprites */
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress stadhero_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x01ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x200000, 0x2007ff, stadhero_pf1_data_w, stadhero_pf1_data ),
		new MemoryWriteAddress( 0x240000, 0x240007, stadhero_pf2_control_0_w ),
		new MemoryWriteAddress( 0x240010, 0x240017, stadhero_pf2_control_1_w ),
		new MemoryWriteAddress( 0x260000, 0x261fff, stadhero_pf2_data_w, stadhero_pf2_data ),
		new MemoryWriteAddress( 0x30c000, 0x30c00b, stadhero_control_w ),
		new MemoryWriteAddress( 0x310000, 0x3107ff, paletteram_xxxxBBBBGGGGRRRR_word_w, paletteram ),
		new MemoryWriteAddress( 0xff8000, 0xffbfff, MWA_BANK1 ),
		new MemoryWriteAddress( 0xffc000, 0xffc7ff, MWA_BANK2, spriteram ),
		new MemoryWriteAddress( 0xffc800, 0xffcfff, spriteram_mirror_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	/******************************************************************************/
	
	public static WriteHandlerPtr YM3812_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		switch (offset) {
		case 0:
			YM3812_control_port_0_w(0,data);
			break;
		case 1:
			YM3812_write_port_0_w(0,data);
			break;
		}
	} };
	
	public static WriteHandlerPtr YM2203_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		switch (offset) {
		case 0:
			YM2203_control_port_0_w(0,data);
			break;
		case 1:
			YM2203_write_port_0_w(0,data);
			break;
		}
	} };
	
	static MemoryReadAddress stadhero_s_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x05ff, MRA_RAM ),
		new MemoryReadAddress( 0x3000, 0x3000, soundlatch_r ),
		new MemoryReadAddress( 0x3800, 0x3800, OKIM6295_status_0_r ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress stadhero_s_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x05ff, MWA_RAM ),
		new MemoryWriteAddress( 0x0800, 0x0801, YM2203_w ),
		new MemoryWriteAddress( 0x1000, 0x1001, YM3812_w ),
		new MemoryWriteAddress( 0x3800, 0x3800, OKIM6295_data_0_w ),
		new MemoryWriteAddress( 0x8000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	/******************************************************************************/
	
	static InputPortPtr input_ports_stadhero = new InputPortPtr(){ public void handler() { 
	PORT_START(); 	/* Player 1 controls */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START(); 	/* Player 2 controls */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START(); 	/* Credits, start buttons */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN3 );/* Service */
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_VBLANK );
		PORT_START(); 	/* Dip switch bank 1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_2C") );
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
	
		PORT_START(); 	/* Dip switch bank 2 */
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
	INPUT_PORTS_END(); }}; 
	
	/******************************************************************************/
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 chars */
		4096,
		3,		/* 4 bits per pixel  */
		new int[] { 0x00000*8,0x8000*8,0x10000*8 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8	/* every char takes 8 consecutive bytes */
	);
	
	static GfxLayout tile_3bpp = new GfxLayout
	(
		16,16,
		2048,
		3,
		new int[] { 0x20000*8, 0x10000*8, 0x00000*8 },
		new int[] { 16*8+0, 16*8+1, 16*8+2, 16*8+3, 16*8+4, 16*8+5, 16*8+6, 16*8+7,
				0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		16*16
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,
		4096,
		4,
		new int[] { 0x60000*8,0x40000*8,0x20000*8,0x00000*8 },
		new int[] { 16*8+0, 16*8+1, 16*8+2, 16*8+3, 16*8+4, 16*8+5, 16*8+6, 16*8+7,
				0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		16*16
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,     0, 16 ),	/* Characters 8x8 */
		new GfxDecodeInfo( REGION_GFX2, 0, tile_3bpp,    512, 16 ),	/* Tiles 16x16 */
		new GfxDecodeInfo( REGION_GFX3, 0, spritelayout, 256, 16 ),	/* Sprites 16x16 */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	/******************************************************************************/
	
	static void irqhandler(int linestate)
	{
		cpu_set_irq_line(1,0,linestate);
	}
	
	static YM2203interface ym2203_interface = new YM2203interface
	(
		1,
		1500000,	/* 12MHz clock divided by 8 = 1.50 MHz */
		new int[] { YM2203_VOL(40,95) },
		new ReadHandlerPtr[] { 0 },
		new ReadHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 }
	);
	
	static YM3812interface ym3812_interface = new YM3812interface
	(
		1,			/* 1 chip */
		3000000,	/* 3 MHz (12MHz/4) */
		new int[] { 40 },
		new WriteYmHandlerPtr[] { irqhandler },
	);
	
	static OKIM6295interface okim6295_interface = new OKIM6295interface
	(
		1,              /* 1 chip */
		new int[] { 7757 },           /* 8000Hz frequency */
		new int[] { REGION_SOUND1 },	/* memory region 3 */
		new int[] { 80 }
	);
	
	/******************************************************************************/
	
	static MachineDriver machine_driver_stadhero = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				10000000,
				stadhero_readmem,stadhero_writemem,null,null,
				m68_level5_irq,1 /* VBL */
			),
			new MachineCPU(
				CPU_M6502 | CPU_AUDIO_CPU,
				1500000,
				stadhero_s_readmem,stadhero_s_writemem,null,null,
				ignore_interrupt,0
			)
		},
		58, 529,
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 1*8, 31*8-1 ),
	
		gfxdecodeinfo,
		1024, 1024,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		stadhero_vh_start,
		stadhero_vh_stop,
		stadhero_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203_interface
			),
			new MachineSound(
				SOUND_YM3812,
				ym3812_interface
			),
			new MachineSound(
				SOUND_OKIM6295,
				okim6295_interface
			)
		}
	);
	
	/******************************************************************************/
	
	static RomLoadPtr rom_stadhero = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000, REGION_CPU1 );/* 6*64k for 68000 code */
		ROM_LOAD_EVEN( "ef15.bin",  0x00000, 0x10000, 0xbbba364e );	ROM_LOAD_ODD ( "ef13.bin",  0x00000, 0x10000, 0x97c6717a );
		ROM_REGION( 0x10000, REGION_CPU2 );/* 6502 Sound */
		ROM_LOAD( "ef18.bin",  0x8000, 0x8000, 0x20fd9668 );
		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "ef08.bin",     0x000000, 0x10000, 0xe84752fe );/* chars */
		ROM_LOAD( "ef09.bin",     0x010000, 0x08000, 0x2ade874d );
		ROM_REGION( 0x30000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "ef10.bin",     0x000000, 0x10000, 0xdca3d599 );/* tiles */
		ROM_LOAD( "ef11.bin",     0x010000, 0x10000, 0xaf563e96 );	ROM_LOAD( "ef12.bin",     0x020000, 0x10000, 0x9a1bf51c );
		ROM_REGION( 0x80000, REGION_GFX3 | REGIONFLAG_DISPOSE );	ROM_LOAD( "ef00.bin",     0x000000, 0x10000, 0x94ed257c );/* sprites */
		ROM_LOAD( "ef01.bin",     0x010000, 0x10000, 0x6eb9a721 );	ROM_LOAD( "ef02.bin",     0x020000, 0x10000, 0x850cb771 );	ROM_LOAD( "ef03.bin",     0x030000, 0x10000, 0x24338b96 );	ROM_LOAD( "ef04.bin",     0x040000, 0x10000, 0x9e3d97a7 );	ROM_LOAD( "ef05.bin",     0x050000, 0x10000, 0x88631005 );	ROM_LOAD( "ef06.bin",     0x060000, 0x10000, 0x9f47848f );	ROM_LOAD( "ef07.bin",     0x070000, 0x10000, 0x8859f655 );
		ROM_REGION( 0x10000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "ef17.bin",  0x0000, 0x10000, 0x07c78358 );ROM_END(); }}; 
	
	/******************************************************************************/
	
	public static GameDriver driver_stadhero	   = new GameDriver("1988"	,"stadhero"	,"stadhero.java"	,rom_stadhero,null	,machine_driver_stadhero	,input_ports_stadhero	,null	,ROT0	,	"Data East Corporation", "Stadium Hero (Japan)" )
}
