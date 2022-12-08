/***************************************************************************

							Ginga NinkyouDen
						    (C) 1987 Jaleco

				    driver by Luca Elia (eliavit@unina.it)

CPU   : 68000 68B09
SOUND : YM2149 Y8950(MSX AUDIO)
OSC.  : 6.000MHz 3.579545MHz

* CTC uses MB-8873E (MC-6840)

					Interesting routines (main cpu)
					-------------------------------

Interrupts:	1-7]	d17a:	clears 20018 etc.

f4b2	print string:	a1.(char)*,0x25(%)	d7.w=color	a0.screen (30000)
f5d6	print 7 digit BCD number: d0.l to (a1)+ color $3000


					Interesting locations (main cpu)
					--------------------------------

20014	# of players (1-2)
20018	cleared by interrupts
2001c	credits (max 9)
20020	internal timer?
20024	initial lives
20058	current lives p1
2005c	current lives p2
20070	coins
200a4	time
200a8	energy

60008		values: 0 1 ffff
6000c		bit:	0	flip sceen?	<-	70002>>14
					1	?			<-

6000e	soundlatch	<- 20038 2003c 20040


								To Do
								-----

- The sound section will benefit from proper MC6840 and YM8950 emulation

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.drivers;

import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.M6809_INT_IRQ;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.v036.mame.memory.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import gr.codebb.arcadeflex.v037b7.sound._3812intfH.Y8950interface;
import static arcadeflex.v036.sound.ay8910.*;
import arcadeflex.v036.sound.ay8910H.AY8910interface;
import static gr.codebb.arcadeflex.v037b7.sound.y8950intf.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.ginganin.*;
import static common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;

public class ginganin
{
	
	/* Variables only used here */
	
	/* Variables defined in vidhrdw */
	
	/* Functions defined in vidhrdw */
	
	
	/*
	**
	**				Main cpu data
	**
	*/
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x01ffff, MRA_ROM ),
		new MemoryReadAddress( 0x020000, 0x023fff, MRA_BANK1 ),
		new MemoryReadAddress( 0x030000, 0x0307ff, MRA_BANK2 ),
		new MemoryReadAddress( 0x040000, 0x0407ff, MRA_BANK3 ),
		new MemoryReadAddress( 0x050000, 0x0507ff, MRA_BANK4 ),
		new MemoryReadAddress( 0x060000, 0x06000f, MRA_BANK5 ),
		new MemoryReadAddress( 0x068000, 0x06bfff, MRA_BANK6 ),	// bg lives in ROM
		new MemoryReadAddress( 0x070000, 0x070001, input_port_0_r ),	// controls
		new MemoryReadAddress( 0x070002, 0x070003, input_port_1_r ),	// DSWs
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress writemem[] =
	{
	/* The ROM area: 10000-13fff is written with: 0000 0000 0000 0001, at startup only. Why? */
		new MemoryWriteAddress( 0x020000, 0x023fff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x030000, 0x0307ff, ginganin_txtram_w, ginganin_txtram ),
		new MemoryWriteAddress( 0x040000, 0x0407ff, MWA_BANK3, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x050000, 0x0507ff, paletteram_RRRRGGGGBBBBxxxx_word_w, paletteram ),
		new MemoryWriteAddress( 0x060000, 0x06000f, ginganin_vregs_w, ginganin_vregs ),
		new MemoryWriteAddress( 0x068000, 0x06bfff, ginganin_fgram_w, ginganin_fgram ),
		new MemoryWriteAddress( -1 )
	};
	
	
	/*
	**
	** 				Sound cpu data
	**
	*/
	
	/* based on snk.c: */
	
	/* Added by Takahiro Nogi. 1999/09/27 */
	static int MC6840_index0;
	static int MC6840_register0;
	static int MC6840_index1;
	static int MC6840_register1;
	static int S_TEMPO = 0;
	static int S_TEMPO_OLD = 0;
	static int MC6809_CTR = 0;
	static int MC6809_FLAG = 0;
	
	
	public static WriteHandlerPtr MC6840_control_port_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* MC6840 Emulation by Takahiro Nogi. 1999/09/27
		(This routine hasn't been completed yet.) */
	
	//	char	mess[80];
	
		MC6840_index0 = data;
	
		if ((MC6840_index0 & 0x80) != 0) {	// enable timer output
			if ((MC6840_register0 != S_TEMPO) && (MC6840_register0 != 0)) {
				S_TEMPO = MC6840_register0;
			//	sprintf(mess, "I0:0x%02X R0:0x%02X I1:0x%02X R1:0x%02X", MC6840_index0, MC6840_register0, MC6840_index1, MC6840_register1);
			//	usrintf_showmessage(mess);
			}
			MC6809_FLAG = 1;
		} else {
			MC6809_FLAG = 0;
		}
	//	logerror("MC6840 Write:(0x%02X)0x%02X\n", MC6840_register0_index, data);
	} };
	
	public static WriteHandlerPtr MC6840_control_port_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* MC6840 Emulation by Takahiro Nogi. 1999/09/27
		(This routine hasn't been completed yet.) */
	
		MC6840_index1 = data;
	} };
	
	public static WriteHandlerPtr MC6840_write_port_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* MC6840 Emulation by Takahiro Nogi. 1999/09/27
		(This routine hasn't been completed yet.) */
	
		MC6840_register0 = data;
	} };
	
	public static WriteHandlerPtr MC6840_write_port_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* MC6840 Emulation by Takahiro Nogi. 1999/09/27
		(This routine hasn't been completed yet.) */
	
		MC6840_register1 = data;
	} };
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x07ff, MRA_RAM ),
		new MemoryReadAddress( 0x1800, 0x1800, soundlatch_r ),
		new MemoryReadAddress( 0x4000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )
	};
	
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x07ff, MWA_RAM ),
		new MemoryWriteAddress( 0x0800, 0x0800, MC6840_control_port_0_w ),	// Takahiro Nogi. 1999/09/27
		new MemoryWriteAddress( 0x0801, 0x0801, MC6840_control_port_1_w ),	// Takahiro Nogi. 1999/09/27
		new MemoryWriteAddress( 0x0802, 0x0802, MC6840_write_port_0_w ),	// Takahiro Nogi. 1999/09/27
		new MemoryWriteAddress( 0x0803, 0x0803, MC6840_write_port_1_w ),	// Takahiro Nogi. 1999/09/27
		new MemoryWriteAddress( 0x2000, 0x2000, Y8950_control_port_0_w ),
		new MemoryWriteAddress( 0x2001, 0x2001, Y8950_write_port_0_w ),
		new MemoryWriteAddress( 0x2800, 0x2800, AY8910_control_port_0_w ),
		new MemoryWriteAddress( 0x2801, 0x2801, AY8910_write_port_0_w ),
		new MemoryWriteAddress( -1 )
	};
	
	
	
	
	/*	Input Ports:	[0] Controls	[1] DSWs */
	
	static InputPortPtr input_ports_ginganin = new InputPortPtr(){ public void handler() { 
	
		PORT_START(); 	// IN0 - Controls - Read from 70000.w
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_START2 );
	
		PORT_START(); 	// IN1 - DSWs - Read from 70002.w
		PORT_DIPNAME( 0x0007, 0x0007, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(      0x0004, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(      0x0002, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(      0x0006, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x0007, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x0003, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(      0x0005, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(      0x0001, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0x0038, 0x0038, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(      0x0020, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(      0x0010, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(      0x0030, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x0038, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x0018, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(      0x0028, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(      0x0008, DEF_STR( "1C_4C") );
		PORT_BITX(    0x0040, 0x0040, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Infinite Lives", IP_KEY_NONE, IP_JOY_NONE );
		PORT_DIPSETTING(      0x0040, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_BITX(    0x0080, 0x0080, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Free Play & Invulnerability", IP_KEY_NONE, IP_JOY_NONE );
		PORT_DIPSETTING(      0x0080, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
	
		PORT_DIPNAME( 0x0300, 0x0300, DEF_STR( "Lives") );
		PORT_DIPSETTING(      0x0000, "2");
		PORT_DIPSETTING(      0x0300, "3");
		PORT_DIPSETTING(      0x0100, "4");
		PORT_DIPSETTING(      0x0200, "5");
		PORT_DIPNAME( 0x0400, 0x0400, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0400, DEF_STR( "On") );
		PORT_DIPNAME( 0x0800, 0x0000, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Upright") );
		PORT_DIPSETTING(      0x0800, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x1000, 0x1000, DEF_STR( "Unknown") );	// probably unused
		PORT_DIPSETTING(      0x1000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x2000, 0x2000, DEF_STR( "Unknown") );	// it does something
		PORT_DIPSETTING(      0x2000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x4000, 0x4000, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(      0x4000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_BITX(    0x8000, 0x8000, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Freeze", IP_KEY_NONE, IP_JOY_NONE );
		PORT_DIPSETTING(      0x8000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
	
	INPUT_PORTS_END(); }}; 
	
	
	
	/*
	**
	** 				Gfx data
	**
	*/
	
	
	static GfxLayout tilelayout = new GfxLayout
	(
		16,16,
		(0x20000)*8/(16*16*4),
		4,
		new int[] {0, 1, 2, 3},
		new int[] {0*4,1*4,2*4,3*4,4*4,5*4,6*4,7*4,
		 0*4+32*16,1*4+32*16,2*4+32*16,3*4+32*16,4*4+32*16,5*4+32*16,6*4+32*16,7*4+32*16},
		new int[] {0*32,1*32,2*32,3*32,4*32,5*32,6*32,7*32,
		 8*32,9*32,10*32,11*32,12*32,13*32,14*32,15*32},
		16*16*4
	);
	
	
	
	
	        
	//layout8x8  (txtlayout,   0x04000)
        static GfxLayout txtlayout = new GfxLayout
	(
		8,8,
		(0x04000)*8/(8*8*4),
		4,
		new int[] {0, 1, 2, 3},
		new int[] {0*4,1*4,2*4,3*4,4*4,5*4,6*4,7*4}, 
		new int[] {0*32,1*32,2*32,3*32,4*32,5*32,6*32,7*32},
		8*8*4
	);
	//layout16x16(spritelayout,0x50000)
        static GfxLayout spritelayout = new GfxLayout
	(
		16,16,
		(0x50000)*8/(16*16*4),
		4,
		new int[] {0, 1, 2, 3},
		new int[] {0*4,1*4,2*4,3*4,4*4,5*4,6*4,7*4,
		 0*4+32*16,1*4+32*16,2*4+32*16,3*4+32*16,4*4+32*16,5*4+32*16,6*4+32*16,7*4+32*16},
		new int[] {0*32,1*32,2*32,3*32,4*32,5*32,6*32,7*32,
		 8*32,9*32,10*32,11*32,12*32,13*32,14*32,15*32},
		16*16*4
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, tilelayout,   256*3, 16 ), // [0] bg
		new GfxDecodeInfo( REGION_GFX2, 0, tilelayout,   256*2, 16 ), // [1] fg
		new GfxDecodeInfo( REGION_GFX3, 0, txtlayout,    256*0, 16 ), // [2] txt
		new GfxDecodeInfo( REGION_GFX4, 0, spritelayout, 256*1, 16 ), // [3] sprites
		new GfxDecodeInfo( -1 )
	};
	
	
	
	
	public static InterruptPtr ginganin_interrupt = new InterruptPtr() { public int handler() 
	{
		return 1;	/* ? (vectors 1-7 cointain the same address) */
	} };
	
	/* Modified by Takahiro Nogi. 1999/09/27 */
	public static InterruptPtr ginganin_sound_interrupt = new InterruptPtr() { public int handler() 
	{
		/* MC6840 Emulation by Takahiro Nogi. 1999/09/27
		(This routine hasn't been completed yet.) */
	
		if (S_TEMPO_OLD != S_TEMPO) {
			S_TEMPO_OLD = S_TEMPO;
			MC6809_CTR = 0;
		}
	
		if (MC6809_FLAG != 0) {
			if (MC6809_CTR > S_TEMPO) {
				MC6809_CTR = 0;
				return M6809_INT_IRQ;
			} else {
				MC6809_CTR++;
			}
		}
	
		return 0;
	} };
	
	
	
	static AY8910interface AY8910_interface = new AY8910interface
	(
		1,
		3579545 / 2 ,	/* ? */
		new int[] { 10 },
		new ReadHandlerPtr[] { null },
		new ReadHandlerPtr[] { null },
		new WriteHandlerPtr[] { null },
		new WriteHandlerPtr[] { null }
	);
	
	
	/* The Y8950 is basically a YM3526 with ADPCM built in */
	static Y8950interface y8950_interface = new Y8950interface
	(
		1,
		3579545,	/* ? */
		new int[]{ 63 },
		new WriteYmHandlerPtr[]{ null },
		new int[]{ REGION_SOUND1 },  // ROM region
		new ReadHandlerPtr[] { null },  /* keyboarc read  */
		new WriteHandlerPtr[]{ null },  /* keyboard write */
		new ReadHandlerPtr[]{ null },  /* I/O read  */
		new WriteHandlerPtr[]{ null }   /* I/O write */
	);
	
	static MachineDriver machine_driver_ginganin = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				6000000,	/* ? */
				readmem,writemem,null,null,
				ginganin_interrupt, 1
			),
			new MachineCPU(
				CPU_M6809 | CPU_AUDIO_CPU,
				1000000,	/* ? */		// Takahiro Nogi. 1999/09/27 (3579545 . 1000000)
				sound_readmem,sound_writemem,null,null,
				ginganin_sound_interrupt, 60	// Takahiro Nogi. 1999/09/27 (1 . 60)
			),
		},
		60,DEFAULT_60HZ_VBLANK_DURATION,
		1,
		null,
	
		/* video hardware */
		256, 256, new rectangle( 0, 255, 0 + 16 , 255 - 16 ),
		gfxdecodeinfo,
		256 * 4, 256 * 4,
		null,
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		ginganin_vh_start,
		null,
		ginganin_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_AY8910,
				AY8910_interface
			),
			new MachineSound(
				SOUND_Y8950,
				y8950_interface
			)
		}
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_ginganin = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000, REGION_CPU1 );/* main cpu */
		ROM_LOAD_EVEN( "gn_02.bin", 0x00000, 0x10000, 0x4a4e012f );
		ROM_LOAD_ODD(  "gn_01.bin", 0x00000, 0x10000, 0x30256fcb );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* sound cpu */
		ROM_LOAD( "gn_05.bin", 0x00000, 0x10000, 0xe76e10e7 );
	
		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "gn_15.bin", 0x000000, 0x10000, 0x1b8ac9fb );// bg
		ROM_LOAD( "gn_14.bin", 0x010000, 0x10000, 0xe73fe668 );
	
		ROM_REGION( 0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "gn_12.bin", 0x000000, 0x10000, 0xc134a1e9 );// fg
		ROM_LOAD( "gn_13.bin", 0x010000, 0x10000, 0x1d3bec21 );
	
		ROM_REGION( 0x04000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "gn_10.bin", 0x000000, 0x04000, 0xae371b2d );// txt
	
		ROM_REGION( 0x50000, REGION_GFX4 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "gn_06.bin", 0x000000, 0x10000, 0xbdc65835 );// sprites
		ROM_CONTINUE(          0x040000, 0x10000 );
		ROM_LOAD( "gn_07.bin", 0x010000, 0x10000, 0xc2b8eafe );
		ROM_LOAD( "gn_08.bin", 0x020000, 0x10000, 0xf7c73c18 );
		ROM_LOAD( "gn_09.bin", 0x030000, 0x10000, 0xa5e07c3b );
	
		ROM_REGION( 0x08000, REGION_GFX5 );/* background tilemaps */
		ROM_LOAD( "gn_11.bin", 0x00000, 0x08000, 0xf0d0e605 );
	
		ROM_REGION( 0x20000, REGION_SOUND1 );/* samples */
		ROM_LOAD( "gn_04.bin", 0x00000, 0x10000, 0x0ed9133b );
		ROM_LOAD( "gn_03.bin", 0x10000, 0x10000, 0xf1ba222c );
	
	ROM_END(); }}; 
	
	
	
	public static InitDriverPtr init_ginganin = new InitDriverPtr() { public void handler() 
	{
		UBytePtr RAM;
	
	/* main cpu patches */
		RAM = new UBytePtr(memory_region(REGION_CPU1));
		RAM.WRITE_WORD(0x408,0x6000);	RAM.WRITE_WORD(0x40a,0x001c);	// avoid writes to rom getting to the log
	
	
	/* sound cpu patches */
		RAM = new UBytePtr(memory_region(REGION_CPU2));
	
		/* let's clear the RAM: ROM starts at 0x4000 */
		memset (new UBytePtr(RAM, 0),0,0x800);
	
	} };
	
	
	public static GameDriver driver_ginganin	   = new GameDriver("1987"	,"ginganin"	,"ginganin.java"	,rom_ginganin,null	,machine_driver_ginganin	,input_ports_ginganin	,init_ginganin	,ROT0	,	"Jaleco", "Ginga NinkyouDen" );
}
