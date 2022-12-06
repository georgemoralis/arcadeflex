/***************************************************************************

Blasteroids Memory Map
----------------------

BLASTEROIDS 68010 MEMORY MAP

Function                           Address        R/W  DATA
-------------------------------------------------------------
Program ROM                        000000-03FFFF  R    D0-D15
Slapstic Program ROM               038000-03FFFF  R    D0-D15

Watchdog reset                     FF8000         W    xx
IRQ Acknowledge                    FF8200         W    xx
VBLANK Acknowledge                 FF8400         W    xx
Unlock EEPROM                      FF8600         W    xx

Priority RAM (1=MO, 0=PF)          FF8800-FF89FE  W    D0

Audio Send Port                    FF8A01         W    D0-D7
Sound Processor Reset              FF8C00         W    xx
Halt CPU until HBLANK              FF8E00         W    xx

Audio Receive Port                 FF9401         R    D0-D7
Whirly-Gig (Player 1)              FF9801         R    D0-D7
Whirly-Gig (Player 2)              FF9805         R    D0-D7

Self-Test                          FF9C01         R    D7
Audio Busy Flag                                   R    D6
Vertical Blank                                    R    D5
Horizontal Blank                                  R    D4
Player 1 Button 4                                 R    D3
Player 1 Transform                                R    D2
Player 1 Thrust                                   R    D1
Player 1 Fire                                     R    D0

Player 2 Button 4                  FF9C03         R    D3
Player 2 Transform                                R    D2
Player 2 Thrust                                   R    D1
Player 2 Fire                                     R    D0

Color RAM Motion Object            FFA000-FFA1FE  R/W  D0-D14
Color RAM Playfield                FFA200-FFA2FE  R/W  D0-D14

EEPROM                             FFB001-FFB3FF  R/W  D0-D7

Playfield RAM (40x30)              FFC000-FFCFFF  R/W  D0-D15
Row Programmable Interrupt         FFC050-FFCED0  R/W  D15

Motion Object V Position           FFD000-FFDFF8  R/W  D7-D15
Motion Object V Size                              R/W  D0-D3
Motion Object H Flip               FFD002-FFDFFA  R/W  D15
Motion Object V Flip                              R/W  D14
Motion Object Stamp                               R/W  D0-D13
Motion Object Link                 FFD004-FFDFFC  R/W  D3-D11
Motion Object H Position           FFD006-FFDFFE  R/W  D6-D15
Motion Object Palette                             R/W  D0-D3

RAM                                FFE000-FFFFFF  R/W

****************************************************************************/



/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.drivers;

import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.inputH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.blstroid.*;
import static gr.codebb.arcadeflex.v037b7.machine.atarigen.*;
import static gr.codebb.arcadeflex.v037b7.machine.atarigenH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295.*;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295H.*;
import static gr.codebb.arcadeflex.v037b7.sndhrdw.atarijsaH.*;
import static gr.codebb.arcadeflex.v037b7.sndhrdw.atarijsa.*;

public class blstroid
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
	
		if (atarigen_scanline_int_state != 0)
			newstate = 1;
		if (atarigen_video_int_state != 0)
			newstate = 2;
		if (atarigen_sound_int_state != 0)
			newstate = 4;
	
		if (newstate != 0)
			cpu_set_irq_line(0, newstate, ASSERT_LINE);
		else
			cpu_set_irq_line(0, 7, CLEAR_LINE);
            }
        };
	
	public static InitMachinePtr init_machine = new InitMachinePtr() { public void handler() 
	{
		atarigen_eeprom_reset();
		atarigen_interrupt_reset(update_interrupts);
		atarigen_scanline_timer_reset(blstroid_scanline_update, 8);
		atarijsa_reset();
	} };
	
	
	
	/*************************************
	 *
	 *	I/O read dispatch
	 *
	 *************************************/
	
	public static ReadHandlerPtr special_port2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int temp = input_port_2_r.handler(offset);
		if (atarigen_cpu_to_sound_ready != 0) temp ^= 0x0040;
		if (atarigen_get_hblank() != 0) temp ^= 0x0010;
		return temp;
	} };
	
	
	
	/*************************************
	 *
	 *	Main CPU memory handlers
	 *
	 *************************************/
	
	static MemoryReadAddress main_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
		new MemoryReadAddress( 0xff9400, 0xff9401, atarigen_sound_r ),
		new MemoryReadAddress( 0xff9800, 0xff9801, input_port_0_r ),
		new MemoryReadAddress( 0xff9804, 0xff9805, input_port_1_r ),
		new MemoryReadAddress( 0xff9c00, 0xff9c01, special_port2_r ),
		new MemoryReadAddress( 0xff9c02, 0xff9c03, input_port_3_r ),
		new MemoryReadAddress( 0xffa000, 0xffa3ff, paletteram_word_r ),
		new MemoryReadAddress( 0xffb000, 0xffb3ff, atarigen_eeprom_r ),
		new MemoryReadAddress( 0xffc000, 0xffcfff, MRA_BANK1 ),
		new MemoryReadAddress( 0xffd000, 0xffdfff, MRA_BANK2 ),
		new MemoryReadAddress( 0xffe000, 0xffffff, MRA_BANK3 ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	
	static MemoryWriteAddress main_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
		new MemoryWriteAddress( 0xff8000, 0xff8001, watchdog_reset_w ),
		new MemoryWriteAddress( 0xff8200, 0xff8201, atarigen_scanline_int_ack_w ),
		new MemoryWriteAddress( 0xff8400, 0xff8401, atarigen_video_int_ack_w ),
		new MemoryWriteAddress( 0xff8600, 0xff8601, atarigen_eeprom_enable_w ),
		new MemoryWriteAddress( 0xff8800, 0xff89ff, blstroid_priorityram_w ),
		new MemoryWriteAddress( 0xff8a00, 0xff8a01, atarigen_sound_w ),
		new MemoryWriteAddress( 0xff8c00, 0xff8c01, atarigen_sound_reset_w ),
		new MemoryWriteAddress( 0xff8e00, 0xff8e01, atarigen_halt_until_hblank_0_w ),
		new MemoryWriteAddress( 0xffa000, 0xffa3ff, paletteram_xRRRRRGGGGGBBBBB_word_w, paletteram ),
		new MemoryWriteAddress( 0xffb000, 0xffb3ff, atarigen_eeprom_w, atarigen_eeprom, atarigen_eeprom_size ),
		new MemoryWriteAddress( 0xffc000, 0xffcfff, blstroid_playfieldram_w, atarigen_playfieldram, atarigen_playfieldram_size ),
		new MemoryWriteAddress( 0xffd000, 0xffdfff, MWA_BANK2, atarigen_spriteram, atarigen_spriteram_size ),
		new MemoryWriteAddress( 0xffe000, 0xffffff, MWA_BANK3 ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	
	/*************************************
	 *
	 *	Port definitions
	 *
	 *************************************/
	
	static InputPortPtr input_ports_blstroid = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* ff9800 */
		PORT_ANALOG( 0x00ff, 0, IPT_DIAL | IPF_PLAYER1, 60, 10, 0, 0 );
		PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* ff9804 */
		PORT_ANALOG( 0x00ff, 0, IPT_DIAL | IPF_PLAYER2, 60, 10, 0, 0 );
		PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 		/* ff9c00 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT( 0x0020, IP_ACTIVE_HIGH, IPT_VBLANK );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_SERVICE( 0x0080, IP_ACTIVE_LOW );
		PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 		/* ff9c02 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );
		PORT_BIT( 0xfff0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		JSA_I_PORT();	/* audio board port */
	INPUT_PORTS_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Graphics definitions
	 *
	 *************************************/
	
	static GfxLayout pflayout = new GfxLayout
	(
		16,8,
		RGN_FRAC(1,1),
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] { 0,0, 4,4, 8,8, 12,12, 16,16, 20,20, 24,24, 28,28 },
		new int[] { 0*8, 4*8, 8*8, 12*8, 16*8, 20*8, 24*8, 28*8 },
		32*8
	);
	
	
	static GfxLayout molayout = new GfxLayout
	(
		16,8,
		RGN_FRAC(1,2),
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] { RGN_FRAC(1,2)+0, RGN_FRAC(1,2)+4, 0, 4, RGN_FRAC(1,2)+8, RGN_FRAC(1,2)+12, 8, 12,
				RGN_FRAC(1,2)+16, RGN_FRAC(1,2)+20, 16, 20, RGN_FRAC(1,2)+24, RGN_FRAC(1,2)+28, 24, 28 },
		new int[] { 0*8, 4*8, 8*8, 12*8, 16*8, 20*8, 24*8, 28*8 },
		32*8
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, pflayout,  256, 16 ),
		new GfxDecodeInfo( REGION_GFX2, 0, molayout,    0, 16 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	/*************************************
	 *
	 *	Machine driver
	 *
	 *************************************/
	
	static MachineDriver machine_driver_blstroid = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,		/* verified */
				ATARI_CLOCK_14MHz/2,
				main_readmem,main_writemem,null,null,
				atarigen_video_int_gen,1
			),
			JSA_I_CPU()
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,
		init_machine,
	
		/* video hardware */
		40*16, 30*8, new rectangle( 0*8, 40*16-1, 0*8, 30*8-1 ),
		gfxdecodeinfo,
		512,512,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK |
				VIDEO_PIXEL_ASPECT_RATIO_1_2,
		null,
		blstroid_vh_start,
		blstroid_vh_stop,
		blstroid_vh_screenrefresh,
	
		/* sound hardware */
		//JSA_I_STEREO,
                SOUND_SUPPORTS_STEREO,0,0,0,							
		new MachineSound[] {
			new MachineSound(													
				SOUND_YM2151, 									
				atarijsa_ym2151_interface_stereo				
			)
		},
	
		atarigen_nvram_handler
	);
	
	
	
	/*************************************
	 *
	 *	ROM definition(s)
	 *
	 *************************************/
	
	static RomLoadPtr rom_blstroid = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );/* 4*64k for 68000 code */
		ROM_LOAD_EVEN( "057-4123",  0x00000, 0x10000, 0xd14badc4 );
		ROM_LOAD_ODD ( "057-4121",  0x00000, 0x10000, 0xae3e93e8 );
		ROM_LOAD_EVEN( "057-4124",  0x20000, 0x10000, 0xfd2365df );
		ROM_LOAD_ODD ( "057-4122",  0x20000, 0x10000, 0xc364706e );
	
		ROM_REGION( 0x14000, REGION_CPU2 );/* 64k for 6502 code */
		ROM_LOAD( "blstroid.snd", 0x10000, 0x4000, 0xbaa8b5fe );
		ROM_CONTINUE(             0x04000, 0xc000 );
	
		ROM_REGION( 0x040000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "blstroid.1l",  0x000000, 0x10000, 0x3c2daa5b );/* playfield */
		ROM_LOAD( "blstroid.1m",  0x010000, 0x10000, 0xf84f0b97 );/* playfield */
		ROM_LOAD( "blstroid.3l",  0x020000, 0x10000, 0xae5274f0 );/* playfield */
		ROM_LOAD( "blstroid.3m",  0x030000, 0x10000, 0x4bb72060 );/* playfield */
	
		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "blstroid.5m",  0x000000, 0x10000, 0x50e0823f );/* mo */
		ROM_LOAD( "blstroid.6m",  0x010000, 0x10000, 0x729de7a9 );/* mo */
		ROM_LOAD( "blstroid.8m",  0x020000, 0x10000, 0x090e42ab );/* mo */
		ROM_LOAD( "blstroid.10m", 0x030000, 0x10000, 0x1ff79e67 );/* mo */
		ROM_LOAD( "blstroid.11m", 0x040000, 0x10000, 0x4be1d504 );/* mo */
		ROM_LOAD( "blstroid.13m", 0x050000, 0x10000, 0xe4409310 );/* mo */
		ROM_LOAD( "blstroid.14m", 0x060000, 0x10000, 0x7aaca15e );/* mo */
		ROM_LOAD( "blstroid.16m", 0x070000, 0x10000, 0x33690379 );/* mo */
		ROM_LOAD( "blstroid.5n",  0x080000, 0x10000, 0x2720ee71 );/* mo */
		ROM_LOAD( "blstroid.6n",  0x090000, 0x10000, 0x2faecd15 );/* mo */
		ROM_LOAD( "blstroid.8n",  0x0a0000, 0x10000, 0xf10e59ed );/* mo */
		ROM_LOAD( "blstroid.10n", 0x0b0000, 0x10000, 0x4d5fc284 );/* mo */
		ROM_LOAD( "blstroid.11n", 0x0c0000, 0x10000, 0xa70fc6e6 );/* mo */
		ROM_LOAD( "blstroid.13n", 0x0d0000, 0x10000, 0xf423b4f8 );/* mo */
		ROM_LOAD( "blstroid.14n", 0x0e0000, 0x10000, 0x56fa3d16 );/* mo */
		ROM_LOAD( "blstroid.16n", 0x0f0000, 0x10000, 0xf257f738 );/* mo */
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_blstroi2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );/* 4*64k for 68000 code */
		ROM_LOAD_EVEN( "blstroid.6c",  0x00000, 0x10000, 0x5a092513 );
		ROM_LOAD_ODD ( "blstroid.6b",  0x00000, 0x10000, 0x486aac51 );
		ROM_LOAD_EVEN( "blstroid.4c",  0x20000, 0x10000, 0xd0fa38fe );
		ROM_LOAD_ODD ( "blstroid.4b",  0x20000, 0x10000, 0x744bf921 );
	
		ROM_REGION( 0x14000, REGION_CPU2 );/* 64k for 6502 code */
		ROM_LOAD( "blstroid.snd", 0x10000, 0x4000, 0xbaa8b5fe );
		ROM_CONTINUE(             0x04000, 0xc000 );
	
		ROM_REGION( 0x040000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "blstroid.1l",  0x000000, 0x10000, 0x3c2daa5b );/* playfield */
		ROM_LOAD( "blstroid.1m",  0x010000, 0x10000, 0xf84f0b97 );/* playfield */
		ROM_LOAD( "blstroid.3l",  0x020000, 0x10000, 0xae5274f0 );/* playfield */
		ROM_LOAD( "blstroid.3m",  0x030000, 0x10000, 0x4bb72060 );/* playfield */
	
		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "blstroid.5m",  0x000000, 0x10000, 0x50e0823f );/* mo */
		ROM_LOAD( "blstroid.6m",  0x010000, 0x10000, 0x729de7a9 );/* mo */
		ROM_LOAD( "blstroid.8m",  0x020000, 0x10000, 0x090e42ab );/* mo */
		ROM_LOAD( "blstroid.10m", 0x030000, 0x10000, 0x1ff79e67 );/* mo */
		ROM_LOAD( "blstroid.11m", 0x040000, 0x10000, 0x4be1d504 );/* mo */
		ROM_LOAD( "blstroid.13m", 0x050000, 0x10000, 0xe4409310 );/* mo */
		ROM_LOAD( "blstroid.14m", 0x060000, 0x10000, 0x7aaca15e );/* mo */
		ROM_LOAD( "blstroid.16m", 0x070000, 0x10000, 0x33690379 );/* mo */
		ROM_LOAD( "blstroid.5n",  0x080000, 0x10000, 0x2720ee71 );/* mo */
		ROM_LOAD( "blstroid.6n",  0x090000, 0x10000, 0x2faecd15 );/* mo */
		ROM_LOAD( "blstroid.8n",  0x0a0000, 0x10000, 0xf10e59ed );/* mo */
		ROM_LOAD( "blstroid.10n", 0x0b0000, 0x10000, 0x4d5fc284 );/* mo */
		ROM_LOAD( "blstroid.11n", 0x0c0000, 0x10000, 0xa70fc6e6 );/* mo */
		ROM_LOAD( "blstroid.13n", 0x0d0000, 0x10000, 0xf423b4f8 );/* mo */
		ROM_LOAD( "blstroid.14n", 0x0e0000, 0x10000, 0x56fa3d16 );/* mo */
		ROM_LOAD( "blstroid.16n", 0x0f0000, 0x10000, 0xf257f738 );/* mo */
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_blsthead = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );/* 4*64k for 68000 code */
		ROM_LOAD_EVEN( "eheadh0.c6",  0x00000, 0x10000, 0x061f0898 );
		ROM_LOAD_ODD ( "eheadl0.b6",  0x00000, 0x10000, 0xae8df7cb );
		ROM_LOAD_EVEN( "eheadh1.c5",  0x20000, 0x10000, 0x0b7a3cb6 );
		ROM_LOAD_ODD ( "eheadl1.b5",  0x20000, 0x10000, 0x43971694 );
	
		ROM_REGION( 0x14000, REGION_CPU2 );/* 64k for 6502 code */
		ROM_LOAD( "blstroid.snd", 0x10000, 0x4000, 0xbaa8b5fe );
		ROM_CONTINUE(             0x04000, 0xc000 );
	
		ROM_REGION( 0x040000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "blstroid.1l",  0x000000, 0x10000, 0x3c2daa5b );/* playfield */
		ROM_LOAD( "blstroid.1m",  0x010000, 0x10000, 0xf84f0b97 );/* playfield */
		ROM_LOAD( "blstroid.3l",  0x020000, 0x10000, 0xae5274f0 );/* playfield */
		ROM_LOAD( "blstroid.3m",  0x030000, 0x10000, 0x4bb72060 );/* playfield */
	
		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "blstroid.5m",  0x000000, 0x10000, 0x50e0823f );/* mo */
		ROM_LOAD( "blstroid.6m",  0x010000, 0x10000, 0x729de7a9 );/* mo */
		ROM_LOAD( "blstroid.8m",  0x020000, 0x10000, 0x090e42ab );/* mo */
		ROM_LOAD( "blstroid.10m", 0x030000, 0x10000, 0x1ff79e67 );/* mo */
		ROM_LOAD( "mol4.m12",     0x040000, 0x10000, 0x571139ea );/* mo */
		ROM_LOAD( "blstroid.13m", 0x050000, 0x10000, 0xe4409310 );/* mo */
		ROM_LOAD( "blstroid.14m", 0x060000, 0x10000, 0x7aaca15e );/* mo */
		ROM_LOAD( "mol7.m16",     0x070000, 0x10000, 0xd27b2d91 );/* mo */
		ROM_LOAD( "blstroid.5n",  0x080000, 0x10000, 0x2720ee71 );/* mo */
		ROM_LOAD( "blstroid.6n",  0x090000, 0x10000, 0x2faecd15 );/* mo */
		ROM_LOAD( "moh2.n8",      0x0a0000, 0x10000, 0xa15e79e1 );/* mo */
		ROM_LOAD( "blstroid.10n", 0x0b0000, 0x10000, 0x4d5fc284 );/* mo */
		ROM_LOAD( "moh4.n12",     0x0c0000, 0x10000, 0x1a74e960 );/* mo */
		ROM_LOAD( "blstroid.13n", 0x0d0000, 0x10000, 0xf423b4f8 );/* mo */
		ROM_LOAD( "blstroid.14n", 0x0e0000, 0x10000, 0x56fa3d16 );/* mo */
		ROM_LOAD( "moh7.n16",     0x0f0000, 0x10000, 0xa93cbbe7 );/* mo */
	ROM_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Driver initialization
	 *
	 *************************************/
	
	public static InitDriverPtr init_blstroid = new InitDriverPtr() { public void handler() 
	{
		atarigen_eeprom_default = null;
		atarijsa_init(1, 4, 2, 0x80);
	
		/* speed up the 6502 */
		atarigen_init_6502_speedup(1, 0x4157, 0x416f);
	
		/* display messages */
		atarigen_show_sound_message();
	} };
	
	
	
	/*************************************
	 *
	 *	Game driver(s)
	 *
	 *************************************/
	
	public static GameDriver driver_blstroid	   = new GameDriver("1987"	,"blstroid"	,"blstroid.java"	,rom_blstroid,null	,machine_driver_blstroid	,input_ports_blstroid	,init_blstroid	,ROT0	,	"Atari Games", "Blasteroids (version 4)" );
	public static GameDriver driver_blstroi2	   = new GameDriver("1987"	,"blstroi2"	,"blstroid.java"	,rom_blstroi2,driver_blstroid	,machine_driver_blstroid	,input_ports_blstroid	,init_blstroid	,ROT0	,	"Atari Games", "Blasteroids (version 2)" );
	public static GameDriver driver_blsthead	   = new GameDriver("1987"	,"blsthead"	,"blstroid.java"	,rom_blsthead,driver_blstroid	,machine_driver_blstroid	,input_ports_blstroid	,init_blstroid	,ROT0	,	"Atari Games", "Blasteroids (with heads)" );
}
