/***************************************************************************

Food Fight Memory Map
-----------------------------------

driver by Aaron Giles

Function                           Address        R/W  DATA
-------------------------------------------------------------
Program ROM                        000000-00FFFF  R    D0-D15
Program RAM                        014000-01BFFF  R/W  D0-D15
Motion Object RAM                  01C000-01CFFF  R/W  D0-D15

Motion Objects:
  Vertical Position                xxxx00              D0-D7
  Horizontal Position              xxxx00              D8-D15
  Picture                          xxxx10              D0-D7
  Color                            xxxx10              D8-D13
  VFlip                            xxxx10              D14
  HFlip                            xxxx10              D15

Playfield                          800000-8007FF  R/W  D0-D15
  Picture                          xxxxx0              D0-D7+D15
  Color                            xxxxx0              D8-D13

NVRAM                              900000-9001FF  R/W  D0-D3
Analog In                          940000-940007  R    D0-D7
Analog Out                         944000-944007  W

Coin 1 (Digital In)                948000         R    D0
Coin 2                                            R    D1
Start 1                                           R    D2
Start 2                                           R    D3
Coin Aux                                          R    D4
Throw 1                                           R    D5
Throw 2                                           R    D6
Test                                              R    D7

PFFlip                             948000         W    D0
Update                                            W    D1
INT3RST                                           W    D2
INT4RST                                           W    D3
LED 1                                             W    D4
LED 2                                             W    D5
COUNTERL                                          W    D6
COUNTERR                                          W    D7

Color RAM                          950000-9503FF  W    D0-D7
Recall                             954000         W
Watchdog                           958000         W
Audio 1                            A40000-A4001F  R/W  D0-D7
Audio 0                            A80000-A8001F  R/W  D0-D7
Audio 2                            AC0000-AC001F  R/W  D0-D7

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.drivers;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.sound.pokeyH.*;
import static gr.codebb.arcadeflex.v036.sound.pokey.*;
import static arcadeflex.v036.machine.foodf.*;
import arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.vidhrdw.foodf.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.inptportH.*;

public class foodf
{
	
	public static ReadHandlerPtr foodf_pokey1_r  = new ReadHandlerPtr() { public int handler(int offset) { return pokey1_r.handler(offset/2); } };
	public static ReadHandlerPtr foodf_pokey2_r  = new ReadHandlerPtr() { public int handler(int offset) { return pokey2_r.handler(offset/2); } };
	public static ReadHandlerPtr foodf_pokey3_r  = new ReadHandlerPtr() { public int handler(int offset) { return pokey3_r.handler(offset/2); } };
	
	public static WriteHandlerPtr foodf_pokey1_w = new WriteHandlerPtr() {public void handler(int offset, int data) { pokey1_w.handler(offset/2, data & 0xff); } };
	public static WriteHandlerPtr foodf_pokey2_w = new WriteHandlerPtr() {public void handler(int offset, int data) { pokey2_w.handler(offset/2, data & 0xff); } };
	public static WriteHandlerPtr foodf_pokey3_w = new WriteHandlerPtr() {public void handler(int offset, int data) { pokey3_w.handler(offset/2, data & 0xff); } };
	
	
	
	static MemoryReadAddress foodf_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x00ffff, MRA_ROM ),
		new MemoryReadAddress( 0x014000, 0x01bfff, MRA_BANK1 ),
		new MemoryReadAddress( 0x01c000, 0x01cfff, MRA_BANK2 ),
		new MemoryReadAddress( 0x800000, 0x8007ff, foodf_playfieldram_r ),
		new MemoryReadAddress( 0x900000, 0x9001ff, foodf_nvram_r ),
		new MemoryReadAddress( 0x940000, 0x940007, foodf_analog_r ),
		new MemoryReadAddress( 0x948000, 0x948003, foodf_digital_r ),
		new MemoryReadAddress( 0x94c000, 0x94c003, MRA_NOP ), /* Used from PC 0x776E */
		new MemoryReadAddress( 0x958000, 0x958003, MRA_NOP ),
		new MemoryReadAddress( 0xa40000, 0xa4001f, foodf_pokey1_r ),
		new MemoryReadAddress( 0xa80000, 0xa8001f, foodf_pokey2_r ),
		new MemoryReadAddress( 0xac0000, 0xac001f, foodf_pokey3_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress foodf_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x00ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x014000, 0x01bfff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x01c000, 0x01cfff, MWA_BANK2, foodf_spriteram, foodf_spriteram_size ),
		new MemoryWriteAddress( 0x800000, 0x8007ff, foodf_playfieldram_w, foodf_playfieldram, foodf_playfieldram_size ),
		new MemoryWriteAddress( 0x900000, 0x9001ff, foodf_nvram_w ),
		new MemoryWriteAddress( 0x944000, 0x944007, foodf_analog_w ),
		new MemoryWriteAddress( 0x948000, 0x948003, foodf_digital_w ),
		new MemoryWriteAddress( 0x950000, 0x9501ff, foodf_paletteram_w, paletteram ),
		new MemoryWriteAddress( 0x954000, 0x954003, MWA_NOP ),
		new MemoryWriteAddress( 0x958000, 0x958003, MWA_NOP ),
		new MemoryWriteAddress( 0xa40000, 0xa4001f, foodf_pokey1_w ),
		new MemoryWriteAddress( 0xa80000, 0xa8001f, foodf_pokey2_w ),
		new MemoryWriteAddress( 0xac0000, 0xac001f, foodf_pokey3_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	static InputPortPtr input_ports_foodf = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_ANALOG( 0xff, 0x7f, IPT_AD_STICK_X | IPF_PLAYER1 | IPF_REVERSE, 100, 10, 0, 255 );
	
		PORT_START(); 	/* IN1 */
		PORT_ANALOG( 0xff, 0x7f, IPT_AD_STICK_X | IPF_PLAYER2 | IPF_REVERSE | IPF_COCKTAIL, 100, 10, 0, 255 );
	
		PORT_START(); 	/* IN2 */
		PORT_ANALOG( 0xff, 0x7f, IPT_AD_STICK_Y | IPF_PLAYER1 | IPF_REVERSE, 100, 10, 0, 255 );
	
		PORT_START(); 	/* IN3 */
		PORT_ANALOG( 0xff, 0x7f, IPT_AD_STICK_Y | IPF_PLAYER2 | IPF_REVERSE | IPF_COCKTAIL, 100, 10, 0, 255 );
	
		PORT_START(); 	/* IN4 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 chars */
		512,	/* 512 chars */
		2,		/* 2 bits per pixel */
		new int[] { 0, 4 },
		new int[] { 8*8+0, 8*8+1, 8*8+2, 8*8+3, 0, 1, 2, 3 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*16	/* every char takes 16 consecutive bytes */
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		256,	/* 256 of them */
		2,		/* 2 bits per pixel */
		new int[] { 8*0x2000, 0 },
		new int[] { 8*16+0, 8*16+1, 8*16+2, 8*16+3, 8*16+4, 8*16+5, 8*16+6, 8*16+7, 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8, 8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		8*32	/* every sprite takes 32 consecutive bytes */
	);
	
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,   0, 64 ),	/* characters 8x8 */
		new GfxDecodeInfo( REGION_GFX2, 0, spritelayout, 0, 64 ),	/* sprites  playfield */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static POKEYinterface pokey_interface = new POKEYinterface
	(
		3,	/* 3 chips */
		600000,	/* .6 MHz */
		new int[] { 33, 33, 33 },
		/* The 8 pot handlers */
		new ReadHandlerPtr[] { null, null, null },
		new ReadHandlerPtr[] { null, null, null },
		new ReadHandlerPtr[] { null, null, null },
		new ReadHandlerPtr[] { null, null, null },
		new ReadHandlerPtr[] { null, null, null },
		new ReadHandlerPtr[] { null, null, null },
		new ReadHandlerPtr[] { null, null, null },
		new ReadHandlerPtr[] { null, null, null },
		/* The allpot handler */
		new ReadHandlerPtr[] { null, null, null }
	);
	
	
	
	static MachineDriver machine_driver_foodf = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				6000000,	/* 6 MHz */
				foodf_readmem,foodf_writemem,null,null,
				foodf_interrupt,4
			),
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 0*8, 28*8-1 ),
		gfxdecodeinfo,
		256, 256,
		foodf_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		foodf_vh_start,
		foodf_vh_stop,
		foodf_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_POKEY,
				pokey_interface
			)
		},
	
		foodf_nvram_handler
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadHandlerPtr rom_foodf = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for 68000 code */
		ROM_LOAD_EVEN( "foodf.9c",     0x00000, 0x02000, 0xef92dc5c );
		ROM_LOAD_ODD ( "foodf.8c",     0x00000, 0x02000, 0xdfc3d5a8 );
		ROM_LOAD_EVEN( "foodf.9d",     0x04000, 0x02000, 0xea596480 );
		ROM_LOAD_ODD ( "foodf.8d",     0x04000, 0x02000, 0x64b93076 );
		ROM_LOAD_EVEN( "foodf.9e",     0x08000, 0x02000, 0x95159a3e );
		ROM_LOAD_ODD ( "foodf.8e",     0x08000, 0x02000, 0xe6cff1b1 );
		ROM_LOAD_EVEN( "foodf.9f",     0x0c000, 0x02000, 0x608690c9 );
		ROM_LOAD_ODD ( "foodf.8f",     0x0c000, 0x02000, 0x17828dbb );
	
		ROM_REGION( 0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "foodf.6lm",    0x0000, 0x2000, 0xc13c90eb );
	
		ROM_REGION( 0x4000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "foodf.4d",     0x0000, 0x2000, 0x8870e3d6 );
		ROM_LOAD( "foodf.4e",     0x2000, 0x2000, 0x84372edf );
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_foodf	   = new GameDriver("1982"	,"foodf"	,"foodf.java"	,rom_foodf,null	,machine_driver_foodf	,input_ports_foodf	,null	,ROT0	,	"Atari", "Food Fight", GAME_NO_COCKTAIL );
}
