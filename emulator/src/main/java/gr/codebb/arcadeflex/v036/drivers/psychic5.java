/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package gr.codebb.arcadeflex.v036.drivers;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.inptportH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.psychic5.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static arcadeflex.v036.sound._2203intf.*;
import static arcadeflex.v036.sound._2203intfH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.cpuintrfH.*;


public class psychic5
{
	
	static int psychic5_bank_latch = 0x0;
	
	
	public static ReadHandlerPtr psychic5_bankselect_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return psychic5_bank_latch;
	} };
	
	public static WriteHandlerPtr psychic5_bankselect_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
		int bankaddress;
	
		if (data != psychic5_bank_latch)
		{
			psychic5_bank_latch = data;
			bankaddress = 0x10000 + ((data & 3) * 0x4000);
			cpu_setbank(1,new UBytePtr(RAM,bankaddress));	 /* Select 4 banks of 16k */
		}
	} };
	
	public static InterruptPtr psychic5_interrupt = new InterruptPtr() { public int handler() 
	{
		if (cpu_getiloops() == 0)
		   return 0xd7;		/* RST 10h */
		else
	   	   return 0xcf;		/* RST 08h */
	} };
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0xbfff, MRA_BANK1 ),
		new MemoryReadAddress( 0xc000, 0xdfff, psychic5_paged_ram_r ),
		new MemoryReadAddress( 0xe000, 0xefff, MRA_RAM ),
		new MemoryReadAddress( 0xf000, 0xf000, MRA_RAM ),
		new MemoryReadAddress( 0xf001, 0xf001, MRA_RAM ),			// unknown
		new MemoryReadAddress( 0xf002, 0xf002, psychic5_bankselect_r ),
		new MemoryReadAddress( 0xf003, 0xf003, psychic5_vram_page_select_r ),
		new MemoryReadAddress( 0xf004, 0xf004, MRA_RAM ),			// unknown
		new MemoryReadAddress( 0xf005, 0xf005, MRA_RAM ),			// unknown
		new MemoryReadAddress( 0xf006, 0xf1ff, MRA_NOP ),
		new MemoryReadAddress( 0xf200, 0xf7ff, MRA_RAM ),
		new MemoryReadAddress( 0xf800, 0xffff, MRA_RAM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0x8000, 0xbfff, MWA_BANK1 ),
		new MemoryWriteAddress( 0xc000, 0xdfff, psychic5_paged_ram_w ),
		new MemoryWriteAddress( 0xe000, 0xefff, MWA_RAM ),
		new MemoryWriteAddress( 0xf000, 0xf000, soundlatch_w ),
		new MemoryWriteAddress( 0xf001, 0xf001, MWA_RAM ),			// unknown
		new MemoryWriteAddress( 0xf002, 0xf002, psychic5_bankselect_w ),
		new MemoryWriteAddress( 0xf003, 0xf003, psychic5_vram_page_select_w ),
		new MemoryWriteAddress( 0xf004, 0xf004, MWA_RAM ),			// unknown
		new MemoryWriteAddress( 0xf005, 0xf005, MWA_RAM ),			// unknown
		new MemoryWriteAddress( 0xf006, 0xf1ff, MWA_NOP ),
		new MemoryWriteAddress( 0xf200, 0xf7ff, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0xf800, 0xffff, MWA_RAM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0xc000, 0xc7ff, MRA_RAM ),
		new MemoryReadAddress( 0xe000, 0xe000, soundlatch_r ),
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0xc000, 0xc7ff, MWA_RAM ),
		new MemoryWriteAddress( -1 )
	};
	
	static IOWritePort sound_writeport[] =
	{
		new IOWritePort( 0x00, 0x00, YM2203_control_port_0_w ),
		new IOWritePort( 0x01, 0x01, YM2203_write_port_0_w ),
		new IOWritePort( 0x80, 0x80, YM2203_control_port_1_w ),
		new IOWritePort( 0x81, 0x81, YM2203_write_port_1_w ),
		new IOWritePort( -1 )
	};
	
	static InputPortPtr input_ports_psychic5 = new InputPortPtr(){ public void handler() { 
	    PORT_START(); 
	    PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );
	    PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );
	    PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
	    PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
	    PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
	    PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
	    PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );
	    PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN2 );
	
	    PORT_START(); 		/* player 1 controls */
	    PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
	    PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );
	    PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );
	    PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );
	    PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 );
	    PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 );
	    PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
	    PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
	    PORT_START(); 		/* player 2 controls */
	    PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
	    PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );
	    PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );
	    PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );
	    PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
	    PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
	    PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
	    PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
	    PORT_START();   /* dsw0 */
	    PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Unknown") );
	    PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	    PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Unknown") );
	    PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	    PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Unknown") );
	    PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	    PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Difficulty") );
	    PORT_DIPSETTING(    0x08, "Normal" );
	    PORT_DIPSETTING(    0x00, "Hard" );
	    PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Cabinet") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
	    PORT_DIPSETTING(    0x10, DEF_STR( "Cocktail") );
	    PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Demo_Sounds") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
	    PORT_DIPSETTING(    0x20, DEF_STR( "On") );
	    PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Lives") );
	    PORT_DIPSETTING(    0x80, "2" );
	    PORT_DIPSETTING(    0xc0, "3" );
	    PORT_DIPSETTING(    0x40, "4" );
	    PORT_DIPSETTING(    0x00, "5" );
	
	    PORT_START();   /* dsw1 */
	    PORT_BITX(    0x01, 0x01, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE );
	    PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	    PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
	    PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	    PORT_DIPNAME( 0xe0, 0xe0, DEF_STR( "Coin_A") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "5C_1C") );
	    PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") );
	    PORT_DIPSETTING(    0x40, DEF_STR( "3C_1C") );
	    PORT_DIPSETTING(    0x60, DEF_STR( "2C_1C") );
	    PORT_DIPSETTING(    0xe0, DEF_STR( "1C_1C") );
	    PORT_DIPSETTING(    0xc0, DEF_STR( "1C_2C") );
	    PORT_DIPSETTING(    0xa0, DEF_STR( "1C_3C") );
	    PORT_DIPSETTING(    0x80, DEF_STR( "1C_4C") );
	    PORT_DIPNAME( 0x1c, 0x1c, DEF_STR( "Coin_B") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "5C_1C") );
	    PORT_DIPSETTING(    0x04, DEF_STR( "4C_1C") );
	    PORT_DIPSETTING(    0x08, DEF_STR( "3C_1C") );
	    PORT_DIPSETTING(    0x0c, DEF_STR( "2C_1C") );
	    PORT_DIPSETTING(    0x1c, DEF_STR( "1C_1C") );
	    PORT_DIPSETTING(    0x18, DEF_STR( "1C_2C") );
	    PORT_DIPSETTING(    0x14, DEF_STR( "1C_3C") );
	    PORT_DIPSETTING(    0x10, DEF_STR( "1C_4C") );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,    /* 8x8 characters */
		1024,	/* 1024 characters */
		4,      /* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 }, /* the four bitplanes for pixel are packed into one nibble */
		new int[] { 0, 4, 8, 12, 16, 20, 24, 28 },
		new int[] { 0*8, 4*8, 8*8, 12*8, 16*8, 20*8, 24*8, 28*8 },
		32*8   	/* every char takes 32 consecutive bytes */
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,  /* 16x16 characters */
		1024,	/* 1024 characters */
		4,      /* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },	/* the four bitplanes for pixel are packed into one nibble */
		new int[] { 0, 4, 8, 12, 16, 20, 24, 28, 64*8, 64*8+4, 64*8+8, 64*8+12, 64*8+16, 64*8+20, 64*8+24, 64*8+28 },
		new int[] { 0*8, 4*8, 8*8, 12*8, 16*8, 20*8, 24*8, 28*8, 32*8, 36*8, 40*8, 44*8, 48*8, 52*8, 56*8, 60*8 },
		128*8	/* every char takes 128 consecutive bytes */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, spritelayout,  0*16, 16 ),
		new GfxDecodeInfo( REGION_GFX2, 0, spritelayout, 16*16, 16 ),
		new GfxDecodeInfo( REGION_GFX3, 0, charlayout,   32*16, 16 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	public static WriteYmHandlerPtr irqhandler = new WriteYmHandlerPtr() { public void handler(int irq)
	{
		cpu_set_irq_line(1,0,irq!=0 ? ASSERT_LINE : CLEAR_LINE);
	} };
	
	static YM2203interface ym2203_interface = new YM2203interface
	(
		2,		/* 2 chips   */
		6000000/4,    	/* 1.5 MHz */
		new int[] { YM2203_VOL(50,15), YM2203_VOL(50,15) },
		new ReadHandlerPtr[] { null,null },
		new ReadHandlerPtr[] { null,null },
		new WriteHandlerPtr[] { null,null },
		new WriteHandlerPtr[] { null,null },
		new WriteYmHandlerPtr[] { irqhandler,null }
	);
	
	static MachineDriver machine_driver_psychic5 = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				6000000,
				readmem,writemem,null,null,
				psychic5_interrupt,2
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				6000000,
				sound_readmem,sound_writemem,null,sound_writeport,
				ignore_interrupt,0	/* IRQs are generated by the YM2203 */
			)
		},
		(int)53.8, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		/* frames per second hand tuned to match game and music speed */
		10,                                     /* Allow time for 2nd cpu to interleave*/
		psychic5_init_machine,
		/* video hardware */
		32*8, 32*8,
		new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		48*16,48*16,
		null,
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		psychic5_vh_start,
		psychic5_vh_stop,
		psychic5_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203_interface
			)
		}
	);
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_psychic5 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000, REGION_CPU1 );				/* 2*64K for main CPU, Z80 */
		ROM_LOAD( "p5d",          0x00000, 0x08000, 0x90259249 );
		ROM_LOAD( "p5e",          0x10000, 0x10000, 0x72298f34 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );				/*64K for 2nd z80 CPU*/
		ROM_LOAD( "p5a",          0x00000, 0x08000, 0x50060ecd );
	
		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "p5b",          0x00000, 0x10000, 0x7e3f87d4 );/* sprite tiles */
		ROM_LOAD( "p5c",          0x10000, 0x10000, 0x8710fedb );
	
		ROM_REGION( 0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "p5g",          0x00000, 0x10000, 0xf9262f32 );/* background tiles */
		ROM_LOAD( "p5h",          0x10000, 0x10000, 0xc411171a );
	
		ROM_REGION( 0x08000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "p5f",          0x00000, 0x08000, 0x04d7e21c );/* foreground tiles */
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_psychic5	   = new GameDriver("1987"	,"psychic5"	,"psychic5.java"	,rom_psychic5,null	,machine_driver_psychic5	,input_ports_psychic5	,null	,ROT270	,	"Jaleco", "Psychic 5", GAME_NO_COCKTAIL );
}
