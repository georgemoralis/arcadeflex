
/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;
import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.commonH.*;
import static mame.inputport.*;
import static mame.drawgfxH.*;
import static mame.sndintrfH.*;
import static mame.cpuintrf.*;
import static mame.common.*;
import static mame.inputportH.*;
import static platform.ptrlib.*;
import static sound._2203intfH.*;
import static sound._2203intf.*;
import static cpu.m6502.m6502H.*;
import static vidhrdw.shootout.*;
import static vidhrdw.btime.*;
import static mame.sndintrf.*;
import static vidhrdw.generic.*;
import static platform.libc_old.*;
import static mame.memory.*;


public class shootout
{

	/*******************************************************************************/
	
	public static WriteHandlerPtr shootout_bankswitch_w = new WriteHandlerPtr() { public void handler(int offset, int v)
	{
		int bankaddress;
		UBytePtr RAM;
	
		v=v&0xf;
		RAM = memory_region(REGION_CPU1);
		bankaddress = 0x10000 + ( 0x4000 * v );
	
		cpu_setbank(1,new UBytePtr(RAM,bankaddress));
	} };
	
	public static WriteHandlerPtr sound_cpu_command_w = new WriteHandlerPtr() { public void handler(int offset, int v)
	{
		soundlatch_w.handler( offset, v );
		cpu_cause_interrupt( 1, M6502_INT_NMI );
	} };
	
	/* stub for reading input ports as active low (makes building ports much easier) */
	public static ReadHandlerPtr low_input_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return ~readinputport( offset );
	} };
	
	/*******************************************************************************/
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x0fff, MRA_RAM ),
		new MemoryReadAddress( 0x1000, 0x1003, low_input_r ),
		new MemoryReadAddress( 0x2000, 0x27ff, MRA_RAM ),	/* foreground */
		new MemoryReadAddress( 0x2800, 0x2bff, videoram_r ), /* background videoram */
		new MemoryReadAddress( 0x2c00, 0x2fff, colorram_r ), /* background colorram */
		new MemoryReadAddress( 0x4000, 0x7fff, MRA_BANK1 ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x0fff, MWA_RAM ),
		new MemoryWriteAddress( 0x1000, 0x1000, shootout_bankswitch_w ),
		new MemoryWriteAddress( 0x1001, 0x1001, MWA_NOP ), /* Todo:  Flipscreen */
		new MemoryWriteAddress( 0x1002, 0x1002, coin_counter_w ),
		new MemoryWriteAddress( 0x1003, 0x1003, sound_cpu_command_w ),
		new MemoryWriteAddress( 0x1004, 0x17ff, MWA_RAM ),
		new MemoryWriteAddress( 0x1800, 0x19ff, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x2000, 0x27ff, MWA_RAM, shootout_textram ),
		new MemoryWriteAddress( 0x2800, 0x2bff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x2c00, 0x2fff, colorram_w, colorram ),
		new MemoryWriteAddress( 0x4000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress readmem_alt[] =
	{
		new MemoryReadAddress( 0x0000, 0x0fff, MRA_RAM ),
		new MemoryReadAddress( 0x1000, 0x1003, low_input_r ),
		new MemoryReadAddress( 0x2000, 0x21ff, MRA_RAM ),
		new MemoryReadAddress( 0x2800, 0x2800, YM2203_status_port_0_r ),
		new MemoryReadAddress( 0x3000, 0x37ff, MRA_RAM ),	/* foreground */
		new MemoryReadAddress( 0x3800, 0x3bff, videoram_r ), /* background videoram */
		new MemoryReadAddress( 0x3c00, 0x3fff, colorram_r ), /* background colorram */
		new MemoryReadAddress( 0x4000, 0x7fff, MRA_BANK1 ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem_alt[] =
	{
		new MemoryWriteAddress( 0x0000, 0x0fff, MWA_RAM ),
		new MemoryWriteAddress( 0x1800, 0x1800, coin_counter_w ),
		new MemoryWriteAddress( 0x2000, 0x21ff, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x2800, 0x2800, YM2203_control_port_0_w ),
		new MemoryWriteAddress( 0x2801, 0x2801, YM2203_write_port_0_w ),
		new MemoryWriteAddress( 0x3000, 0x37ff, MWA_RAM, shootout_textram ),
		new MemoryWriteAddress( 0x3800, 0x3bff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x3c00, 0x3fff, colorram_w, colorram ),
		new MemoryWriteAddress( 0x4000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	/*******************************************************************************/
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x07ff, MRA_RAM ),
		new MemoryReadAddress( 0x4000, 0x4000, YM2203_status_port_0_r ),
		new MemoryReadAddress( 0xa000, 0xa000, soundlatch_r ),
		new MemoryReadAddress( 0xc000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x07ff, MWA_RAM ),
		new MemoryWriteAddress( 0x4000, 0x4000, YM2203_control_port_0_w ),
		new MemoryWriteAddress( 0x4001, 0x4001, YM2203_write_port_0_w ),
		new MemoryWriteAddress( 0xd000, 0xd000, interrupt_enable_w ),
		new MemoryWriteAddress( 0xc000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	/*******************************************************************************/
	
	static InputPortPtr input_ports_shootout = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x03, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x0c, 0x00, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x80, 0x00, "Freeze" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_START2 );
	
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_COIN2 );
	
		PORT_START();  	/* DSW2 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x02, "1" );
		PORT_DIPSETTING(    0x00, "3" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_BITX(0,        0x03, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite", IP_KEY_NONE, IP_JOY_NONE );
		PORT_DIPNAME( 0x0c, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "20k 70k" );
		PORT_DIPSETTING(    0x04, "30k 80k" );
		PORT_DIPSETTING(    0x08, "40k 90k" );
		PORT_DIPSETTING(    0x0c, "70k" );
		PORT_DIPNAME( 0x30, 0x00, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x00, "Easy" );
		PORT_DIPSETTING(    0x10, "Medium" );
		PORT_DIPSETTING(    0x20, "Hard" );
		PORT_DIPSETTING(    0x30, "Hardest" );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* this is set when either coin is inserted */
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_VBLANK );
	INPUT_PORTS_END(); }}; 
	
	
	static GfxLayout char_layout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		0x400,	/* 1024 characters */
		2,	/* 2 bits per pixel */
		new int[] { 0,4 },	/* the bitplanes are packed in the same byte */
		new int[] { (0x2000*8)+0, (0x2000*8)+1, (0x2000*8)+2, (0x2000*8)+3, 0, 1, 2, 3 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8	/* every char takes 8 consecutive bytes */
	);
	static GfxLayout sprite_layout = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		0x800,	/* 2048 sprites */
		3,	/* 3 bits per pixel */
		new int[] { 0*0x10000*8, 1*0x10000*8, 2*0x10000*8 },	/* the bitplanes are separated */
		new int[] { 128+0, 128+1, 128+2, 128+3, 128+4, 128+5, 128+6, 128+7, 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8, 8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		32*8	/* every char takes 32 consecutive bytes */
	);
	static GfxLayout tile_layout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		0x800,	/* 2048 characters */
		2,	/* 2 bits per pixel */
		new int[] { 0,4 },	/* the bitplanes are packed in the same byte */
		new int[] { (0x4000*8)+0, (0x4000*8)+1, (0x4000*8)+2, (0x4000*8)+3, 0, 1, 2, 3 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8	/* every char takes 8 consecutive bytes */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, char_layout,   16*4+8*8, 16 ), /* characters */
		new GfxDecodeInfo( REGION_GFX2, 0, sprite_layout, 16*4,      8 ), /* sprites */
		new GfxDecodeInfo( REGION_GFX3, 0, tile_layout,   0,        16 ), /* tiles */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	public static WriteYmHandlerPtr shootout_snd_irq = new WriteYmHandlerPtr() {
            public void handler(int linestate) {

		cpu_set_irq_line(1,0,linestate);
	}};
	
	public static WriteYmHandlerPtr shootout_snd2_irq = new WriteYmHandlerPtr() {
            public void handler(int linestate) {
		cpu_set_irq_line(0,0,linestate);
	}};
	
	static YM2203interface ym2203_interface = new YM2203interface
	(
		1,	/* 1 chip */
		1500000,	/* 1.5 MHz */
		new int[] { YM2203_VOL(50,50) },
		new ReadHandlerPtr[] { null },
		new ReadHandlerPtr[] { null },
		new WriteHandlerPtr[] { null },
		new WriteHandlerPtr[] { null },
		new WriteYmHandlerPtr[] { shootout_snd_irq }
	);
	
	static YM2203interface ym2203_interface2 = new YM2203interface
	(
		1,	/* 1 chip */
		1500000,	/* 1.5 MHz */
		new int[] { YM2203_VOL(50,50) },
		new ReadHandlerPtr[] { null },
		new ReadHandlerPtr[] { null },
		new WriteHandlerPtr[] { shootout_bankswitch_w },
		new WriteHandlerPtr[] { null }, /* Todo:  Port B write is flipscreen */
		new WriteYmHandlerPtr[] { shootout_snd2_irq }
	);
	static int coin = 0;
	public static InterruptPtr shootout_interrupt = new InterruptPtr() { public int handler() 
	{
		
	
		if (( readinputport( 2 ) & 0xc0 )!=0) {
			if ( coin == 0 ) {
				coin = 1;
				return nmi_interrupt.handler();
			}
		} else
			coin = 0;
	
		return 0;
	} };
	
	static MachineDriver machine_driver_shootout = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6502,
				2000000,	/* 2 Mhz? */
				readmem,writemem,null,null,
				shootout_interrupt,1 /* nmi's are triggered at coin up */
			),
			new MachineCPU(
				CPU_M6502 | CPU_AUDIO_CPU,
				1500000,
				sound_readmem,sound_writemem,null,null,
				ignore_interrupt,0 /* this is a guess, but sounds just about right */
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 1*8, 31*8-1 ),
		gfxdecodeinfo,
		256,256,
		btime_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		shootout_vh_start,
		generic_vh_stop,
		shootout_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203_interface
			)
		}
	);
	
	static MachineDriver machine_driver_shootouj = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6502,
				2000000,	/* 2 Mhz? */
				readmem_alt,writemem_alt,null,null,
				shootout_interrupt,1 /* nmi's are triggered at coin up */
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 1*8, 31*8-1 ),
		gfxdecodeinfo,
		256,256,
		btime_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		shootout_vh_start,
		generic_vh_stop,
		shootouj_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203_interface2
			)
		}
	);
	
	
	static RomLoadPtr rom_shootout = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2*0x20000, REGION_CPU1 );/* 128k for code + 128k for decrypted opcodes */
		ROM_LOAD( "cu00.b1",        0x08000, 0x8000, 0x090edeb6 );/* opcodes encrypted */
		/* banked at 0x4000-0x8000 */
		ROM_LOAD( "cu02.c3",        0x10000, 0x8000, 0x2a913730 );/* opcodes encrypted */
		ROM_LOAD( "cu01.c1",        0x18000, 0x4000, 0x8843c3ae );/* opcodes encrypted */
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for code */
		ROM_LOAD( "cu09.j1",        0x0c000, 0x4000, 0xc4cbd558 );/* Sound CPU */
	
		ROM_REGION( 0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cu11.h19",       0x00000, 0x4000, 0xeff00460 );/* foreground characters */
	
		ROM_REGION( 0x30000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cu04.c7",        0x00000, 0x8000, 0xceea6b20 );/* sprites */
		ROM_LOAD( "cu03.c5",        0x08000, 0x8000, 0xb786bb3e );
		ROM_LOAD( "cu06.c10",       0x10000, 0x8000, 0x2ec1d17f );
		ROM_LOAD( "cu05.c9",        0x18000, 0x8000, 0xdd038b85 );
		ROM_LOAD( "cu08.c13",       0x20000, 0x8000, 0x91290933 );
		ROM_LOAD( "cu07.c12",       0x28000, 0x8000, 0x19b6b94f );
	
		ROM_REGION( 0x08000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cu10.h17",       0x00000, 0x2000, 0x3854c877 );/* background tiles */
		ROM_CONTINUE(               0x04000, 0x2000 );
		ROM_CONTINUE(               0x02000, 0x2000 );
		ROM_CONTINUE(               0x06000, 0x2000 );
	
		ROM_REGION( 0x0200, REGION_PROMS );
		ROM_LOAD( "gb08.k10",       0x0000, 0x0100, 0x509c65b6 );
		ROM_LOAD( "gb09.k6",        0x0100, 0x0100, 0xaa090565 );/* unknown */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_shootouj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000, REGION_CPU1 );/* 2 * 128k for code  */
		ROM_LOAD( "cg02.bin",    0x08000, 0x8000, 0x8fc5d632 );
		ROM_LOAD( "cg00.bin",    0x10000, 0x8000, 0xef6ced1e );
		ROM_LOAD( "cg01.bin",    0x18000, 0x4000, 0x74cf11ca );
	
		ROM_REGION( 0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cu11.h19",       0x00000, 0x4000, 0xeff00460 );/* foreground characters */
	
		ROM_REGION( 0x30000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cg03.bin",    0x00000, 0x8000, 0x5252ec19 );/* sprites */
		ROM_LOAD( "cg04.bin",    0x10000, 0x8000, 0xdb06cfe9 );
		ROM_LOAD( "cg05.bin",    0x20000, 0x8000, 0xd634d6b8 );
	
		ROM_REGION( 0x08000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cu10.h17",       0x00000, 0x2000, 0x3854c877 );/* background tiles */
		ROM_CONTINUE(               0x04000, 0x2000 );
		ROM_CONTINUE(               0x02000, 0x2000 );
		ROM_CONTINUE(               0x06000, 0x2000 );
	
		ROM_REGION( 0x0200, REGION_PROMS );
		ROM_LOAD( "gb08.k10",       0x0000, 0x0100, 0x509c65b6 );
		ROM_LOAD( "gb09.k6",        0x0100, 0x0100, 0xaa090565 );/* unknown */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_shootoub = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2 * 0x20000, REGION_CPU1 );/* 128k for code  */
		ROM_LOAD( "shootout.006", 0x08000, 0x8000, 0x2c054888 );
		ROM_LOAD( "shootout.008", 0x10000, 0x8000, 0x9651b656 );
		ROM_LOAD( "cg01.bin",     0x18000, 0x4000, 0x74cf11ca );
	
		ROM_REGION( 0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cu11.h19",       0x00000, 0x4000, 0xeff00460 );/* foreground characters */
	
		ROM_REGION( 0x30000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "shootout.005",   0x00000, 0x8000, 0xe6357ba3 );/* sprites */
		ROM_LOAD( "shootout.004",   0x10000, 0x8000, 0x7f422c93 );
		ROM_LOAD( "shootout.003",   0x20000, 0x8000, 0xeea94535 );
	
		ROM_REGION( 0x08000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cu10.h17",       0x00000, 0x2000, 0x3854c877 );/* background tiles */
		ROM_CONTINUE(               0x04000, 0x2000 );
		ROM_CONTINUE(               0x02000, 0x2000 );
		ROM_CONTINUE(               0x06000, 0x2000 );
	
		ROM_REGION( 0x0200, REGION_PROMS );
		ROM_LOAD( "gb08.k10",       0x0000, 0x0100, 0x509c65b6 );
		ROM_LOAD( "gb09.k6",        0x0100, 0x0100, 0xaa090565 );/* unknown */
	ROM_END(); }}; 
	
	public static InitDriverPtr init_shootout = new InitDriverPtr() { public void handler() 
	{
		UBytePtr rom = memory_region(REGION_CPU1);
		int diff = memory_region_length(REGION_CPU1) / 2;
		int A;
	
		memory_set_opcode_base(0,new UBytePtr(rom,diff));
	
		for (A = 0;A < diff;A++)
			rom.write(A+diff, (rom.read(A) & 0x9f) | ((rom.read(A) & 0x40) >> 1) | ((rom.read(A) & 0x20) << 1));
	} };
	
	public static GameDriver driver_shootout	   = new GameDriver("1985"	,"shootout"	,"shootout.java"	,rom_shootout,null	,machine_driver_shootout	,input_ports_shootout	,init_shootout	,ROT0	,	"Data East USA", "Shoot Out (US)", GAME_NO_COCKTAIL );
	public static GameDriver driver_shootouj	   = new GameDriver("1985"	,"shootouj"	,"shootout.java"	,rom_shootouj,driver_shootout	,machine_driver_shootouj	,input_ports_shootout	,null	,ROT0	,	"Data East USA", "Shoot Out (Japan)", GAME_NO_COCKTAIL );
	public static GameDriver driver_shootoub	   = new GameDriver("1985"	,"shootoub"	,"shootout.java"	,rom_shootoub,driver_shootout	,machine_driver_shootouj	,input_ports_shootout	,init_shootout	,ROT0	,	"bootleg", "Shoot Out (Korean Bootleg)", GAME_NO_COCKTAIL );
}
