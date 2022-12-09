/***************************************************************************

Xybots Memory Map
-----------------------------------

driver by Aaron Giles

XYBOTS 68000 MEMORY MAP

Function                           Address        R/W  DATA
-------------------------------------------------------------
Program ROM                        000000-007FFF  R    D0-D15
Program ROM/SLAPSTIC               008000-00FFFF  R    D0-D15
Program ROM                        010000-02FFFF  R    D0-D15

NOTE: All addresses can be accessed in byte or word mode.


XYBOTS 6502 MEMORY MAP


***************************************************************************/



/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.drivers;
  //generic imports
import static arcadeflex.v036.generic.funcPtr.*;      
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.inputH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.xybots.*;
import static gr.codebb.arcadeflex.v037b7.machine.atarigen.*;
import static gr.codebb.arcadeflex.v037b7.machine.atarigenH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.sndhrdw.atarijsa.*;
import static gr.codebb.arcadeflex.v037b7.sndhrdw.atarijsaH.*;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295.*;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295H.*;

public class xybots
{
	
	/*************************************
	 *
	 *	Initialization
	 *
	 *************************************/
	
	static atarigen_int_callbackPtr update_interrupts = new atarigen_int_callbackPtr() {
            @Override
            public void handler() {
                int newstate = 0;
	
		if (atarigen_video_int_state != 0)
			newstate = 1;
		if (atarigen_sound_int_state != 0)
			newstate = 2;
	
		if (newstate != 0)
			cpu_set_irq_line(0, newstate, ASSERT_LINE);
		else
			cpu_set_irq_line(0, 7, CLEAR_LINE);
            }
        };
	
	public static InitMachineHandlerPtr init_machine = new InitMachineHandlerPtr() { public void handler() 
	{
		atarigen_eeprom_reset();
		atarigen_slapstic_reset();
		atarigen_interrupt_reset(update_interrupts);
		atarigen_scanline_timer_reset(xybots_scanline_update, 8);
		atarijsa_reset();
	} };
	
	
	
	/*************************************
	 *
	 *	I/O handlers
	 *
	 *************************************/
	static int h256 = 0x0400;
        
	public static ReadHandlerPtr special_port1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	
		int result = input_port_1_r.handler(offset);
	
		if (atarigen_cpu_to_sound_ready != 0) result ^= 0x0200;
		result ^= h256 ^= 0x0400;
		return result;
	} };
	
	
	
	/*************************************
	 *
	 *	Main CPU memory handlers
	 *
	 *************************************/
	
	static MemoryReadAddress main_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
		new MemoryReadAddress( 0xff8000, 0xff8fff, MRA_BANK3 ),
		new MemoryReadAddress( 0xff9000, 0xffadff, MRA_BANK2 ),
		new MemoryReadAddress( 0xffae00, 0xffafff, MRA_BANK4 ),
		new MemoryReadAddress( 0xffb000, 0xffbfff, MRA_BANK5 ),
		new MemoryReadAddress( 0xffc000, 0xffc7ff, paletteram_word_r ),
		new MemoryReadAddress( 0xffd000, 0xffdfff, atarigen_eeprom_r ),
		new MemoryReadAddress( 0xffe000, 0xffe0ff, atarigen_sound_r ),
		new MemoryReadAddress( 0xffe100, 0xffe1ff, input_port_0_r ),
		new MemoryReadAddress( 0xffe200, 0xffe2ff, special_port1_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	
	static MemoryWriteAddress main_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
		new MemoryWriteAddress( 0xff8000, 0xff8fff, MWA_BANK3, atarigen_alpharam, atarigen_alpharam_size ),
		new MemoryWriteAddress( 0xff9000, 0xffadff, MWA_BANK2 ),
		new MemoryWriteAddress( 0xffae00, 0xffafff, MWA_BANK4, atarigen_spriteram, atarigen_spriteram_size ),
		new MemoryWriteAddress( 0xffb000, 0xffbfff, xybots_playfieldram_w, atarigen_playfieldram, atarigen_playfieldram_size ),
		new MemoryWriteAddress( 0xffc000, 0xffc7ff, paletteram_IIIIRRRRGGGGBBBB_word_w, paletteram ),
		new MemoryWriteAddress( 0xffd000, 0xffdfff, atarigen_eeprom_w, atarigen_eeprom, atarigen_eeprom_size ),
		new MemoryWriteAddress( 0xffe800, 0xffe8ff, atarigen_eeprom_enable_w ),
		new MemoryWriteAddress( 0xffe900, 0xffe9ff, atarigen_sound_w ),
		new MemoryWriteAddress( 0xffea00, 0xffeaff, watchdog_reset_w ),
		new MemoryWriteAddress( 0xffeb00, 0xffebff, atarigen_video_int_ack_w ),
		new MemoryWriteAddress( 0xffee00, 0xffeeff, atarigen_sound_reset_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	
	/*************************************
	 *
	 *	Port definitions
	 *
	 *************************************/
	
	static InputPortPtr input_ports_xybots = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* ffe100 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BITX(0x0004, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2, "P2 Twist Right", KEYCODE_W, IP_JOY_DEFAULT );
		PORT_BITX(0x0008, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2, "P2 Twist Left", KEYCODE_Q, IP_JOY_DEFAULT );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER2 | IPF_8WAY );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BITX(0x0400, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1, "P1 Twist Right", KEYCODE_X, IP_JOY_DEFAULT );
		PORT_BITX(0x0800, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1, "P1 Twist Left", KEYCODE_Z, IP_JOY_DEFAULT );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER1 | IPF_8WAY );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER1 | IPF_8WAY );
	
		PORT_START(); 	/* ffe200 */
		PORT_BIT( 0x00ff, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_SERVICE( 0x0100, IP_ACTIVE_LOW );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_UNUSED );	/* /AUDBUSY */
		PORT_BIT( 0x0400, IP_ACTIVE_HIGH, IPT_UNUSED );/* 256H */
		PORT_BIT( 0x0800, IP_ACTIVE_HIGH, IPT_VBLANK );/* VBLANK */
		PORT_BIT( 0xf000, IP_ACTIVE_LOW, IPT_UNUSED );
	
		JSA_I_PORT_SWAPPED();	/* audio port */
	INPUT_PORTS_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Graphics definitions
	 *
	 *************************************/
	
	static GfxLayout anlayout = new GfxLayout
	(
		8,8,	/* 8*8 chars */
		512,	/* 512 chars */
		2,		/* 2 bits per pixel */
		new int[] { 0, 4 },
		new int[] { 0, 1, 2, 3, 8, 9, 10, 11 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
		8*16	/* every char takes 16 consecutive bytes */
	);
	
	
	static GfxLayout pflayout = new GfxLayout
	(
		8,8,	/* 8*8 chars */
		8192,	/* 8192 chars */
		4,		/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },
		new int[] { 0, 4, 8, 12, 16, 20, 24, 28 },
		new int[] { 0*8, 4*8, 8*8, 12*8, 16*8, 20*8, 24*8, 28*8 },
		32*8	/* every char takes 32 consecutive bytes */
	);
	
	
	static GfxLayout molayout = new GfxLayout
	(
		8,8,	/* 8*8 chars */
		16384,	/* 16384 chars */
		4,		/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },
		new int[] { 0, 4, 8, 12, 16, 20, 24, 28 },
		new int[] { 0*8, 4*8, 8*8, 12*8, 16*8, 20*8, 24*8, 28*8 },
		32*8	/* every char takes 32 consecutive bytes */
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, pflayout,      512, 16 ),		/* playfield */
		new GfxDecodeInfo( REGION_GFX2, 0, molayout,      256, 48 ),		/* sprites */
		new GfxDecodeInfo( REGION_GFX3, 0, anlayout,        0, 64 ),		/* characters 8x8 */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	/*************************************
	 *
	 *	Machine driver
	 *
	 *************************************/
	
	static MachineDriver machine_driver_xybots = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,		/* verified */
				ATARI_CLOCK_14MHz/2,
				main_readmem,main_writemem,null,null,
				atarigen_video_int_gen,1
			),
			new MachineCPU(														
                            CPU_M6502,											
                            ATARI_CLOCK_14MHz/8,								
                            atarijsa1_readmem,atarijsa1_writemem,null,null,			
                            null,0,												
                            atarigen_6502_irq_gen,
                                (int)(1000000000.0/((double)ATARI_CLOCK_14MHz/4/4/16/16/14))
                        )
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,
		init_machine,
	
		/* video hardware */
		42*8, 30*8, new rectangle( 0*8, 42*8-1, 0*8, 30*8-1 ),
		gfxdecodeinfo,
		1024,1024,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK,
		null,
		xybots_vh_start,
		xybots_vh_stop,
		xybots_vh_screenrefresh,
	
		/* sound hardware */
                SOUND_SUPPORTS_STEREO,0,0,0,
                new MachineSound[]{
                    new MachineSound(
                            SOUND_YM2151,
                            atarijsa_ym2151_interface_stereo_swapped
                    )
                },
		
		atarigen_nvram_handler
	);
	
	
	
	/*************************************
	 *
	 *	ROM definition(s)
	 *
	 *************************************/
	
	static RomLoadHandlerPtr rom_xybots = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x90000, REGION_CPU1 );/* 8*64k for 68000 code */
		ROM_LOAD_EVEN( "2112.c17",     0x00000, 0x10000, 0x16d64748 );
		ROM_LOAD_ODD ( "2113.c19",     0x00000, 0x10000, 0x2677d44a );
		ROM_LOAD_EVEN( "2114.b17",     0x20000, 0x08000, 0xd31890cb );
		ROM_LOAD_ODD ( "2115.b19",     0x20000, 0x08000, 0x750ab1b0 );
	
		ROM_REGION( 0x14000, REGION_CPU2 );/* 64k for 6502 code */
		ROM_LOAD( "xybots.snd",   0x10000, 0x4000, 0x3b9f155d );
		ROM_CONTINUE(             0x04000, 0xc000 );
	
		ROM_REGION( 0x40000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "2102.l13",     0x00000, 0x08000, 0xc1309674 );
		ROM_RELOAD(               0x08000, 0x08000 );
		ROM_LOAD( "2103.l11",     0x10000, 0x10000, 0x907c024d );
		ROM_LOAD( "2117.l7",      0x30000, 0x10000, 0x0cc9b42d );
	
		ROM_REGION( 0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "1105.de1",     0x00000, 0x10000, 0x315a4274 );
		ROM_LOAD( "1106.e1",      0x10000, 0x10000, 0x3d8c1dd2 );
		ROM_LOAD( "1107.f1",      0x20000, 0x10000, 0xb7217da5 );
		ROM_LOAD( "1108.fj1",     0x30000, 0x10000, 0x77ac65e1 );
		ROM_LOAD( "1109.j1",      0x40000, 0x10000, 0x1b482c53 );
		ROM_LOAD( "1110.k1",      0x50000, 0x10000, 0x99665ff4 );
		ROM_LOAD( "1111.kl1",     0x60000, 0x10000, 0x416107ee );
	
		ROM_REGION( 0x02000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "1101.c4",      0x00000, 0x02000, 0x59c028a2 );
	ROM_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Driver initialization
	 *
	 *************************************/
	
	public static InitDriverHandlerPtr init_xybots = new InitDriverHandlerPtr() { public void handler() 
	{
		atarigen_eeprom_default = null;
		atarigen_slapstic_init(0, 0x008000, 107);
	
		atarijsa_init(1, 2, 1, 0x0100);
	
		/* speed up the 6502 */
		atarigen_init_6502_speedup(1, 0x4157, 0x416f);
	
		/* display messages */
	/*	atarigen_show_slapstic_message(); -- no known slapstic problems */
		atarigen_show_sound_message();
	} };
	
	
	
	public static GameDriver driver_xybots	   = new GameDriver("1987"	,"xybots"	,"xybots.java"	,rom_xybots,null	,machine_driver_xybots	,input_ports_xybots	,init_xybots	,ROT0	,	"Atari Games", "Xybots" );
}
