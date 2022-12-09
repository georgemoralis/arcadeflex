/***************************************************************************

EPRoM Memory Map
----------------

driver by Aaron Giles


EPRoM 68010 MEMORY MAP

Program ROM             000000-05FFFF   R    D15-D0
Program ROM shared      060000-07FFFF   R    D15-D0
Program ROM             080000-09FFFF   R    D15-D0

EEPROM                  0E0001-0E0FFF  R/W   D7-D0    (odd bytes only)
Program RAM             160000-16FFFF  R/W   D15-D0
UNLOCK EEPROM           1Fxxxx          W

Player 1 Input (left)   260000          R    D11-D8 Active lo
Player 2 Input (right)  260010          R    D11-D8 Active lo
      D8:    start
      D9:    fire
      D10:   spare
      D11:   duck

VBLANK                  260010          R    D0 Active lo
Self-test                               R    D1 Active lo
Input buffer full (@260030)             R    D2 Active lo
Output buffer full (@360030)            R    D3 Active lo
ADEOC, end of conversion                R    D4 Active hi

ADC0, analog port       260020          R    D0-D7
ADC1                    260022          R    D0-D7
ADC2                    260024          R    D0-D7
ADC3                    260026          R    D0-D7

Read sound processor    260030          R    D0-D7

Watch Dog               2E0000          W    xx        (128 msec. timeout)

VBLANK Interrupt ack.   360000          W    xx

Video off               360010          W    D5 (active hi)
Video intensity                         W    D1-D4 (0=full on)
EXTRA cpu reset                         W    D0 (lo to reset)

Sound processor reset   360020          W    xx

Write sound processor   360030          W    D0-D7

Color RAM Alpha         3E0000-3E01FF  R/W   D15-D0
Color RAM Motion Object 3E0200-3E03FF  R/W   D15-D0
Color RAM Playfield     3E0400-3E07FE  R/W   D15-D0
Color RAM STAIN         3E0800-3E0FFE  R/W   D15-D0

Playfield Picture RAM   3F0000-3F1FFF  R/W   D15-D0
Motion Object RAM       3F2000-3F3FFF  R/W   D15-D0
Alphanumerics RAM       3F4000-3F4EFF  R/W   D15-D0
Scroll and MOB config   3F4F00-3F4F70  R/W   D15-D0
SLIP pointers           3F4F80-3F4FFF  R/W   D9-D0
Working RAM             3F5000-3F7FFF  R/W   D15-D0
Playfield palette RAM   3F8000-3F9FFF  R/W   D11-D8

-----------------------------------------------------------

EPRoM EXTRA 68010 MEMORY MAP

Program ROM             000000-05FFFF   R    D15-D0
Program ROM shared      060000-07FFFF   R    D15-D0

Program RAM             160000-16FFFF  R/W   D15-D0

Player 1 Input (left)   260000          R    D11-D8 Active lo
Player 2 Input (right)  260010          R    D11-D8 Active lo
      D8:    start
      D9:    fire
      D10:   spare
      D11:   duck

VBLANK                  260010          R    D0 Active lo
Self-test                               R    D1 Active lo
Input buffer full (@260030)             R    D2 Active lo
Output buffer full (@360030)            R    D3 Active lo
ADEOC, end of conversion                R    D4 Active hi

ADC0, analog port       260020          R    D0-D7
ADC1                    260022          R    D0-D7
ADC2                    260024          R    D0-D7
ADC3                    260026          R    D0-D7

Read sound processor    260030          R    D0-D7

VBLANK Interrupt ack.   360000          W    xx

Video off               360010          W    D5 (active hi)
Video intensity                         W    D1-D4 (0=full on)
EXTRA cpu reset                         W    D0 (lo to reset)

Sound processor reset   360020          W    xx

Write sound processor   360030          W    D0-D7

****************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.drivers;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.inputH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.eprom.*;
import static gr.codebb.arcadeflex.v037b7.machine.atarigen.*;
import static gr.codebb.arcadeflex.v037b7.machine.atarigenH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295.*;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295H.*;
import static gr.codebb.arcadeflex.v037b7.sndhrdw.atarijsa.*;
import static gr.codebb.arcadeflex.v037b7.sndhrdw.atarijsaH.*;
import static common.libc.cstring.memcpy;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;

public class eprom
{
	
	public static UBytePtr sync_data = new UBytePtr();
	
	
	/*************************************
	 *
	 *	Initialization
	 *
	 *************************************/
	
	public static atarigen_int_callbackPtr update_interrupts = new atarigen_int_callbackPtr() {
            @Override
            public void handler() {
		int newstate = 0;
		int newstate2 = 0;
	
		if (atarigen_video_int_state != 0){
			newstate |= 4;
                        newstate2 |= 4;
                }
                
		if (atarigen_sound_int_state != 0)
			newstate |= 6;
	
		if (newstate != 0)
			cpu_set_irq_line(0, newstate, ASSERT_LINE);
		else
			cpu_set_irq_line(0, 7, CLEAR_LINE);
	
		if (newstate2 != 0)
			cpu_set_irq_line(1, newstate2, ASSERT_LINE);
		else
			cpu_set_irq_line(1, 7, CLEAR_LINE);
            }
        };
	
	public static InitMachineHandlerPtr init_machine = new InitMachineHandlerPtr() { public void handler() 
	{
		atarigen_eeprom_reset();
		atarigen_interrupt_reset(update_interrupts);
		atarigen_scanline_timer_reset(eprom_scanline_update, 8);
		atarijsa_reset();
	} };
	
	
	
	/*************************************
	 *
	 *	I/O handling
	 *
	 *************************************/
	
	public static ReadHandlerPtr special_port1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int result = input_port_1_r.handler(offset);
	
		if (atarigen_sound_to_cpu_ready != 0) result ^= 0x0004;
		if (atarigen_cpu_to_sound_ready != 0) result ^= 0x0008;
		result ^= 0x0010;
	
		return result;
	} };
	
        static int last_offset;
	
	public static ReadHandlerPtr adc_r  = new ReadHandlerPtr() { public int handler(int offset)
	{		
		int result = readinputport(2 + ((last_offset / 2) & 3));
		last_offset = offset;
		return result;
	} };
	
	
	
	/*************************************
	 *
	 *	Latch write handler
	 *
	 *************************************/
	
	public static WriteHandlerPtr eprom_latch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		//(void)offset;
	
		/* reset extra CPU */
		if ((data & 0x00ff0000)==0)
		{
			if ((data & 1) != 0)
				cpu_set_reset_line(1,CLEAR_LINE);
			else
				cpu_set_reset_line(1,ASSERT_LINE);
		}
	} };
	
	
	
	/*************************************
	 *
	 *	Synchronization
	 *
	 *************************************/
	
	public static ReadHandlerPtr sync_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return sync_data.READ_WORD(offset);
	} };
	
	
	public static WriteHandlerPtr sync_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = sync_data.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword, data);
		sync_data.WRITE_WORD(offset, newword);
		if ((oldword & 0xff00) != (newword & 0xff00))
			cpu_yield();
	} };
	
	
	
	/*************************************
	 *
	 *	Main CPU memory handlers
	 *
	 *************************************/
	
	static MemoryReadAddress main_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x09ffff, MRA_ROM ),
		new MemoryReadAddress( 0x0e0000, 0x0e0fff, atarigen_eeprom_r ),
		new MemoryReadAddress( 0x16cc00, 0x16cc01, sync_r ),
		new MemoryReadAddress( 0x160000, 0x16ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x260000, 0x26000f, input_port_0_r ),
		new MemoryReadAddress( 0x260010, 0x26001f, special_port1_r ),
		new MemoryReadAddress( 0x260020, 0x26002f, adc_r ),
		new MemoryReadAddress( 0x260030, 0x260031, atarigen_sound_r ),
		new MemoryReadAddress( 0x3e0000, 0x3e0fff, paletteram_word_r ),
		new MemoryReadAddress( 0x3f0000, 0x3f1fff, MRA_BANK2 ),
		new MemoryReadAddress( 0x3f2000, 0x3f3fff, MRA_BANK3 ),
		new MemoryReadAddress( 0x3f4000, 0x3f4fff, MRA_BANK4 ),
		new MemoryReadAddress( 0x3f5000, 0x3f7fff, MRA_BANK5 ),
		new MemoryReadAddress( 0x3f8000, 0x3f9fff, MRA_BANK6 ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	
	static MemoryWriteAddress main_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x09ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x0e0000, 0x0e0fff, atarigen_eeprom_w, atarigen_eeprom, atarigen_eeprom_size ),
		new MemoryWriteAddress( 0x16cc00, 0x16cc01, sync_w, sync_data ),
		new MemoryWriteAddress( 0x160000, 0x16ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x1f0000, 0x1fffff, atarigen_eeprom_enable_w ),
		new MemoryWriteAddress( 0x2e0000, 0x2e0001, watchdog_reset_w ),
		new MemoryWriteAddress( 0x360000, 0x360001, atarigen_video_int_ack_w ),
		new MemoryWriteAddress( 0x360010, 0x360011, eprom_latch_w ),
		new MemoryWriteAddress( 0x360020, 0x360021, atarigen_sound_reset_w ),
		new MemoryWriteAddress( 0x360030, 0x360031, atarigen_sound_w ),
		new MemoryWriteAddress( 0x3e0000, 0x3e0fff, paletteram_IIIIRRRRGGGGBBBB_word_w, paletteram ),
		new MemoryWriteAddress( 0x3f0000, 0x3f1fff, eprom_playfieldram_w, atarigen_playfieldram, atarigen_playfieldram_size ),
		new MemoryWriteAddress( 0x3f2000, 0x3f3fff, MWA_BANK3, atarigen_spriteram, atarigen_spriteram_size ),
		new MemoryWriteAddress( 0x3f4000, 0x3f4fff, MWA_BANK4, atarigen_alpharam, atarigen_alpharam_size ),
		new MemoryWriteAddress( 0x3f5000, 0x3f7fff, MWA_BANK5 ),
		new MemoryWriteAddress( 0x3f8000, 0x3f9fff, eprom_playfieldpalram_w, eprom_playfieldpalram, eprom_playfieldpalram_size ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	
	/*************************************
	 *
	 *	Extra CPU memory handlers
	 *
	 *************************************/
	
	static MemoryReadAddress extra_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x16cc00, 0x16cc01, sync_r ),
		new MemoryReadAddress( 0x160000, 0x16ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x260000, 0x26000f, input_port_0_r ),
		new MemoryReadAddress( 0x260010, 0x26001f, special_port1_r ),
		new MemoryReadAddress( 0x260020, 0x26002f, adc_r ),
		new MemoryReadAddress( 0x260030, 0x260031, atarigen_sound_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	
	static MemoryWriteAddress extra_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x16cc00, 0x16cc01, sync_w, sync_data ),
		new MemoryWriteAddress( 0x160000, 0x16ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x360000, 0x360001, atarigen_video_int_ack_w ),
		new MemoryWriteAddress( 0x360010, 0x360011, eprom_latch_w ),
		new MemoryWriteAddress( 0x360020, 0x360021, atarigen_sound_reset_w ),
		new MemoryWriteAddress( 0x360030, 0x360031, atarigen_sound_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	
	/*************************************
	 *
	 *	Port definitions
	 *
	 *************************************/
	
	static InputPortHandlerPtr input_ports_eprom = new InputPortHandlerPtr(){ public void handler() { 
		PORT_START(); 		/* 26000 */
		PORT_BIT( 0x00ff, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0xf000, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 		/* 26010 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_VBLANK );
		PORT_SERVICE( 0x0002, IP_ACTIVE_LOW );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_UNUSED );/* Input buffer full (@260030) */
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_UNUSED );/* Output buffer full (@360030) */
		PORT_BIT( 0x0010, IP_ACTIVE_HIGH, IPT_UNUSED );/* ADEOC, end of conversion */
		PORT_BIT( 0x00e0, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0xf000, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* ADC0 @ 0x260020 */
		PORT_ANALOG( 0x00ff, 0x0080, IPT_AD_STICK_Y | IPF_PLAYER1, 100, 10, 0x10, 0xf0 );
		PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* ADC1 @ 0x260022 */
		PORT_ANALOG( 0x00ff, 0x0080, IPT_AD_STICK_X | IPF_REVERSE | IPF_PLAYER1, 100, 10, 0x10, 0xf0 );
		PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* ADC0 @ 0x260024 */
		PORT_ANALOG( 0x00ff, 0x0080, IPT_AD_STICK_Y | IPF_PLAYER2, 100, 10, 0x10, 0xf0 );
		PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* ADC1 @ 0x260026 */
		PORT_ANALOG( 0x00ff, 0x0080, IPT_AD_STICK_X | IPF_REVERSE | IPF_PLAYER2, 100, 10, 0x10, 0xf0 );
		PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
	
		JSA_I_PORT();	/* audio board port */
	INPUT_PORTS_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Graphics definitions
	 *
	 *************************************/
	
	static GfxLayout anlayout = new GfxLayout
	(
		8,8,	/* 8*8 chars */
		1024,	/* 1024 chars */
		2,		/* 2 bits per pixel */
		new int[] { 0, 4 },
		new int[] { 0, 1, 2, 3, 8, 9, 10, 11 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
		8*16	/* every char takes 16 consecutive bytes */
	);
	
	
	static GfxLayout pfmolayout = new GfxLayout
	(
		8,8,	/* 8*8 sprites */
		32768,	/* 32768 of them */
		4,		/* 4 bits per pixel */
		new int[] { 0*8*0x40000, 1*8*0x40000, 2*8*0x40000, 3*8*0x40000 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8	/* every sprite takes 8 consecutive bytes */
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, pfmolayout,  256, 32 ),	/* sprites  playfield */
		new GfxDecodeInfo( REGION_GFX2, 0, anlayout,      0, 64 ),	/* characters 8x8 */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	/*************************************
	 *
	 *	Machine driver
	 *
	 *************************************/
	
	static MachineDriver machine_driver_eprom = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				ATARI_CLOCK_14MHz/2,
				main_readmem,main_writemem,null,null,
				atarigen_video_int_gen,1
			),
			new MachineCPU(
				CPU_M68000,
				ATARI_CLOCK_14MHz/2,
				extra_readmem,extra_writemem,null,null,
				ignore_interrupt,1
			),
			//JSA_I_CPU
                        new MachineCPU(											
                            CPU_M6502,											
                            ATARI_CLOCK_14MHz/8,								
                            atarijsa1_readmem,atarijsa1_writemem,null,null,			
                            null,0,												
                            atarigen_6502_irq_gen,(int)(1000000000.0/((double)ATARI_CLOCK_14MHz/4/4/16/16/14))
                        )
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		10,
		init_machine,
	
		/* video hardware */
		42*8, 30*8, new rectangle( 0*8, 42*8-1, 0*8, 30*8-1 ),
		gfxdecodeinfo,
		2048, 2048,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK,
		null,
		eprom_vh_start,
		eprom_vh_stop,
		eprom_vh_screenrefresh,
	
		/* sound hardware */
		//JSA_I_MONO_WITH_SPEECH,
                0,0,0,0,												
		new MachineSound[]{
                    new MachineSound(
				SOUND_YM2151, 									
				atarijsa_ym2151_interface_mono					
                    ),													
                    new MachineSound(													
                            SOUND_TMS5220, 									
                            atarijsa_tms5220_interface 					
                    )
                },
	
		atarigen_nvram_handler
	);
	
	
	
	/*************************************
	 *
	 *	ROM decoding
	 *
	 *************************************/
	
	static void rom_decode()
	{
		int i;
	
		/* invert the graphics bits on the playfield and motion objects */
		for (i = 0; i < memory_region_length(REGION_GFX1); i++)
			memory_region(REGION_GFX1).write(i, memory_region(REGION_GFX1).read(i) ^ 0xff);
	
		/* copy the shared ROM from region 0 to region 1 */
		memcpy(new UBytePtr(memory_region(REGION_CPU2), 0x60000), new UBytePtr(memory_region(REGION_CPU1), 0x60000), 0x20000);
	}
	
	
	
	/*************************************
	 *
	 *	ROM definition(s)
	 *
	 *************************************/
	
	static RomLoadHandlerPtr rom_eprom = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0xa0000, REGION_CPU1 );/* 10*64k for 68000 code */
		ROM_LOAD_EVEN( "136069.50a",   0x00000, 0x10000, 0x08888dec );
		ROM_LOAD_ODD ( "136069.40a",   0x00000, 0x10000, 0x29cb1e97 );
		ROM_LOAD_EVEN( "136069.50b",   0x20000, 0x10000, 0x702241c9 );
		ROM_LOAD_ODD ( "136069.40b",   0x20000, 0x10000, 0xfecbf9e2 );
		ROM_LOAD_EVEN( "136069.50d",   0x40000, 0x10000, 0x0f2f1502 );
		ROM_LOAD_ODD ( "136069.40d",   0x40000, 0x10000, 0xbc6f6ae8 );
		ROM_LOAD_EVEN( "136069.40k",   0x60000, 0x10000, 0x130650f6 );
		ROM_LOAD_ODD ( "136069.50k",   0x60000, 0x10000, 0x1da21ed8 );
	
		ROM_REGION( 0x80000, REGION_CPU2 );/* 8*64k for 68000 code */
		ROM_LOAD_EVEN( "136069.10s",   0x00000, 0x10000, 0xdeff6469 );
		ROM_LOAD_ODD ( "136069.10u",   0x00000, 0x10000, 0x5d7afca2 );
	
		ROM_REGION( 0x14000, REGION_CPU3 );/* 64k + 16k for 6502 code */
		ROM_LOAD( "136069.7b",    0x10000, 0x4000, 0x86e93695 );
		ROM_CONTINUE(             0x04000, 0xc000 );
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "136069.47s",   0x00000, 0x10000, 0x0de9d98d );
		ROM_LOAD( "136069.43s",   0x10000, 0x10000, 0x8eb106ad );
		ROM_LOAD( "136069.38s",   0x20000, 0x10000, 0xbf3d0e18 );
		ROM_LOAD( "136069.32s",   0x30000, 0x10000, 0x48fb2e42 );
		ROM_LOAD( "136069.76s",   0x40000, 0x10000, 0x602d939d );
		ROM_LOAD( "136069.70s",   0x50000, 0x10000, 0xf6c973af );
		ROM_LOAD( "136069.64s",   0x60000, 0x10000, 0x9cd52e30 );
		ROM_LOAD( "136069.57s",   0x70000, 0x10000, 0x4e2c2e7e );
		ROM_LOAD( "136069.47u",   0x80000, 0x10000, 0xe7edcced );
		ROM_LOAD( "136069.43u",   0x90000, 0x10000, 0x9d3e144d );
		ROM_LOAD( "136069.38u",   0xa0000, 0x10000, 0x23f40437 );
		ROM_LOAD( "136069.32u",   0xb0000, 0x10000, 0x2a47ff7b );
		ROM_LOAD( "136069.76u",   0xc0000, 0x10000, 0xb0cead58 );
		ROM_LOAD( "136069.70u",   0xd0000, 0x10000, 0xfbc3934b );
		ROM_LOAD( "136069.64u",   0xe0000, 0x10000, 0x0e07493b );
		ROM_LOAD( "136069.57u",   0xf0000, 0x10000, 0x34f8f0ed );
	
		ROM_REGION( 0x04000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "1360691.25d",  0x00000, 0x04000, 0x409d818e );
	ROM_END(); }}; 
	
	static RomLoadHandlerPtr rom_eprom2 = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0xa0000, REGION_CPU1 );/* 10*64k for 68000 code */
		ROM_LOAD_EVEN( "1025.50a",   0x00000, 0x10000, 0xb0c9a476 );
		ROM_LOAD_ODD ( "1024.40a",   0x00000, 0x10000, 0x4cc2c50c );
		ROM_LOAD_EVEN( "1027.50b",   0x20000, 0x10000, 0x84f533ea );
		ROM_LOAD_ODD ( "1026.40b",   0x20000, 0x10000, 0x506396ce );
		ROM_LOAD_EVEN( "1029.50d",   0x40000, 0x10000, 0x99810b9b );
		ROM_LOAD_ODD ( "1028.40d",   0x40000, 0x10000, 0x08ab41f2 );
		ROM_LOAD_EVEN( "1033.40k",   0x60000, 0x10000, 0x395fc203 );
		ROM_LOAD_ODD ( "1032.50k",   0x60000, 0x10000, 0xa19c8acb );
		ROM_LOAD_EVEN( "1037.50e",   0x80000, 0x10000, 0xad39a3dd );
		ROM_LOAD_ODD ( "1036.40e",   0x80000, 0x10000, 0x34fc8895 );
	
		ROM_REGION( 0x80000, REGION_CPU2 );/* 8*64k for 68000 code */
		ROM_LOAD_EVEN( "1035.10s",   0x00000, 0x10000, 0xffeb5647 );
		ROM_LOAD_ODD ( "1034.10u",   0x00000, 0x10000, 0xc68f58dd );
	
		ROM_REGION( 0x14000, REGION_CPU3 );/* 64k + 16k for 6502 code */
		ROM_LOAD( "136069.7b",    0x10000, 0x4000, 0x86e93695 );
		ROM_CONTINUE(             0x04000, 0xc000 );
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "136069.47s",   0x00000, 0x10000, 0x0de9d98d );
		ROM_LOAD( "136069.43s",   0x10000, 0x10000, 0x8eb106ad );
		ROM_LOAD( "136069.38s",   0x20000, 0x10000, 0xbf3d0e18 );
		ROM_LOAD( "136069.32s",   0x30000, 0x10000, 0x48fb2e42 );
		ROM_LOAD( "136069.76s",   0x40000, 0x10000, 0x602d939d );
		ROM_LOAD( "136069.70s",   0x50000, 0x10000, 0xf6c973af );
		ROM_LOAD( "136069.64s",   0x60000, 0x10000, 0x9cd52e30 );
		ROM_LOAD( "136069.57s",   0x70000, 0x10000, 0x4e2c2e7e );
		ROM_LOAD( "136069.47u",   0x80000, 0x10000, 0xe7edcced );
		ROM_LOAD( "136069.43u",   0x90000, 0x10000, 0x9d3e144d );
		ROM_LOAD( "136069.38u",   0xa0000, 0x10000, 0x23f40437 );
		ROM_LOAD( "136069.32u",   0xb0000, 0x10000, 0x2a47ff7b );
		ROM_LOAD( "136069.76u",   0xc0000, 0x10000, 0xb0cead58 );
		ROM_LOAD( "136069.70u",   0xd0000, 0x10000, 0xfbc3934b );
		ROM_LOAD( "136069.64u",   0xe0000, 0x10000, 0x0e07493b );
		ROM_LOAD( "136069.57u",   0xf0000, 0x10000, 0x34f8f0ed );
	
		ROM_REGION( 0x04000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "1360691.25d",  0x00000, 0x04000, 0x409d818e );
	ROM_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Driver initialization
	 *
	 *************************************/
	
	public static InitDriverHandlerPtr init_eprom = new InitDriverHandlerPtr() { public void handler() 
	{
		atarigen_eeprom_default = null;
		atarijsa_init(2, 6, 1, 0x0002);
	
		/* speed up the 6502 */
		atarigen_init_6502_speedup(2, 0x4158, 0x4170);
	
		/* display messages */
		atarigen_show_sound_message();
	
		rom_decode();
	} };
	
	
	
	/*************************************
	 *
	 *	Game driver(s)
	 *
	 *************************************/
	
	public static GameDriver driver_eprom	   = new GameDriver("1989"	,"eprom"	,"eprom.java"	,rom_eprom,null	,machine_driver_eprom	,input_ports_eprom	,init_eprom	,ROT0	,	"Atari Games", "Escape from the Planet of the Robot Monsters (set 1)" );
	public static GameDriver driver_eprom2	   = new GameDriver("1989"	,"eprom2"	,"eprom.java"	,rom_eprom2,driver_eprom	,machine_driver_eprom	,input_ports_eprom	,init_eprom	,ROT0	,	"Atari Games", "Escape from the Planet of the Robot Monsters (set 2)" );
}
