/***************************************************************************

Lady Frog, or is it Dragon Punch, or is Lady Frog the name of a bootleg
Dragon Punch?

The program jumps straight away to an unmapped memory address. I don't know,
maybe there's a ROM missing.

ladyfrog.001 contains
VIDEO COMPUTER SYSTEM  (C)1989 DYNAX INC  NAGOYA JAPAN  DRAGON PUNCH  VER. 1.30

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.drivers;

import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;

public class ladyfrog
{
	
	
	public static VhUpdatePtr ladyfrog_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		palette_recalc();
	} };
	
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x3fffff, MRA_ROM ),
		new MemoryReadAddress( 0xffc000, 0xffffff, MRA_BANK1 ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x3fffff, MWA_ROM ),
		new MemoryWriteAddress( 0xffc000, 0xffffff, MWA_BANK1 ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x4fff, MRA_ROM ),
		new MemoryReadAddress( 0x7000, 0x7fff, MRA_RAM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x4fff, MWA_ROM ),
		new MemoryWriteAddress( 0x7000, 0x7fff, MWA_RAM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static IOReadPort sound_readport[] =
	{
		new IOReadPort( -1 )  /* end of table */
	};
	
	static IOWritePort sound_writeport[] =
	{
	//	new IOWritePort( 0x12, 0x12, ),	ym2151?
	//	new IOWritePort( 0x13, 0x13, ),
		new IOWritePort( -1 )  /* end of table */
	};
	
	
	
	static InputPortPtr input_ports_ladyfrog = new InputPortPtr(){ public void handler() { 
		PORT_START(); 
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,4),
		4,
		new int[] { RGN_FRAC(0,4), RGN_FRAC(1,4), RGN_FRAC(2,4), RGN_FRAC(3,4) },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,  256, 16 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static MachineDriver machine_driver_ladyfrog = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				10000000,	/* 10 MHz??? */
				readmem,writemem,null,null,
				m68_level6_irq,1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				4000000,	/* 4 MHz??? */
				sound_readmem,sound_writemem,sound_readport,sound_writeport,
				ignore_interrupt,1	/* interrupt mode 0 + NMI */
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		256, 256, new rectangle( 0, 256-1, 0, 256-1 ),
		gfxdecodeinfo,
		1024, 1024,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		null,
		null,
		ladyfrog_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
                null
	);
	
	
	
	static RomLoadPtr rom_ladyfrog = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x400000, REGION_CPU1 );/* 68000 code */
		ROM_LOAD_EVEN( "ladyfrog.002",	0x000000, 0x080000, 0x724cf022 );
		ROM_LOAD_ODD ( "ladyfrog.006",	0x000000, 0x080000, 0xe52a7ae2 );
		ROM_LOAD_EVEN( "ladyfrog.003",	0x100000, 0x080000, 0xa1d49967 );
		ROM_LOAD_ODD ( "ladyfrog.007",	0x100000, 0x080000, 0xe5805c4e );
		ROM_LOAD_EVEN( "ladyfrog.004",	0x200000, 0x080000, 0x709281f5 );
		ROM_LOAD_ODD ( "ladyfrog.008",	0x200000, 0x080000, 0x39adcba4 );
		ROM_LOAD_EVEN( "ladyfrog.005",	0x300000, 0x080000, 0xb683160c );
		ROM_LOAD_ODD ( "ladyfrog.009",	0x300000, 0x080000, 0xe475fb76 );
	
		ROM_REGION( 0x20000, REGION_CPU2 );/* Z80 code */
		ROM_LOAD( "ladyfrog.001",        0x0000, 0x20000, 0xba9eb1c6 );
	
		ROM_REGION( 0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ladyfrog.010",       0x00000, 0x20000, 0x51fd0e1a );
		ROM_LOAD( "ladyfrog.011",       0x20000, 0x20000, 0x610bf6f3 );
		ROM_LOAD( "ladyfrog.012",       0x40000, 0x20000, 0x466ede67 );
		ROM_LOAD( "ladyfrog.013",       0x60000, 0x20000, 0xfad3e8be );
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_ladyfrog	   = new GameDriver("19??"	,"ladyfrog"	,"ladyfrog.java"	,rom_ladyfrog,null	,machine_driver_ladyfrog	,input_ports_ladyfrog	,null	,ROT0	,	"Dynax? Comad?", "Dragon Punch? Lady Frog?" );
}
