/***************************************************************************

	Batman

    driver by Aaron Giles

****************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.drivers;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import gr.codebb.arcadeflex.common.SubArrays.IntSubArray;
import gr.codebb.arcadeflex.common.SubArrays.UShortArray;
import static common.libc.cstring.memcpy;
import static gr.codebb.arcadeflex.v037b7.machine.atarigen.*;
import static gr.codebb.arcadeflex.v037b7.machine.atarigenH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.mame.sndintrfH.*;

import static gr.codebb.arcadeflex.v037b7.sndhrdw.atarijsaH.*;
import static gr.codebb.arcadeflex.v037b7.sndhrdw.atarijsa.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.batman.*;

public class batman
{
/*TODO*///	
/*TODO*///	
/*TODO*///	void batman_set_alpha_bank(int bank);
/*TODO*///	
/*TODO*///	
/*TODO*///	void batman_scanline_update(int scanline);

	
	static UBytePtr latch_data = new UBytePtr();
	
	
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
			newstate |= 4;
		if (atarigen_sound_int_state != 0)
			newstate |= 6;
	
		if (newstate != 0)
			cpu_set_irq_line(0, newstate, ASSERT_LINE);
		else
			cpu_set_irq_line(0, 7, CLEAR_LINE);
            }
        };
	
	public static InitMachinePtr init_machine = new InitMachinePtr() { public void handler() 
	{
		atarigen_eeprom_reset();
		atarigen_video_control_reset();
		atarigen_interrupt_reset(update_interrupts);
		atarigen_scanline_timer_reset(batman_scanline_update, 8);
		atarijsa_reset();
	} };
	
	
	
	/*************************************
	 *
	 *	I/O handling
	 *
	 *************************************/
	
	public static ReadHandlerPtr special_port2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int result = input_port_2_r.handler(offset);
	
		if (atarigen_sound_to_cpu_ready != 0) result ^= 0x0010;
		if (atarigen_cpu_to_sound_ready != 0) result ^= 0x0020;
	
		return result;
	} };
	
	
	public static WriteHandlerPtr latch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = latch_data.READ_WORD(0);
		int newword = COMBINE_WORD(oldword, data);
		latch_data.WRITE_WORD(0, newword);
	
		/* bit 4 is connected to the /RESET pin on the 6502 */
		if ((newword & 0x0010) != 0)
			cpu_set_reset_line(1, CLEAR_LINE);
		else
			cpu_set_reset_line(1, ASSERT_LINE);
	
		/* alpha bank is selected by the upper 4 bits */
		if (((oldword ^ newword) & 0x7000) != 0)
			batman_set_alpha_bank((newword >> 12) & 7);
	} };
	
	
	
	/*************************************
	 *
	 *	Main CPU memory handlers
	 *
	 *************************************/
	
	static MemoryReadAddress main_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x0bffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x10ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x120000, 0x120fff, atarigen_eeprom_r ),
		new MemoryReadAddress( 0x3e0000, 0x3e0fff, paletteram_word_r ),
		new MemoryReadAddress( 0x3effc0, 0x3effff, atarigen_video_control_r ),
		new MemoryReadAddress( 0x260000, 0x260001, input_port_0_r ),
		new MemoryReadAddress( 0x260002, 0x260003, input_port_1_r ),
		new MemoryReadAddress( 0x260010, 0x260011, special_port2_r ),
		new MemoryReadAddress( 0x260030, 0x260031, atarigen_sound_r ),
		new MemoryReadAddress( 0x3f0000, 0x3f5fff, MRA_BANK3 ),
		new MemoryReadAddress( 0x3f6000, 0x3f7fff, MRA_BANK4 ),
		new MemoryReadAddress( 0x3f8000, 0x3f8fff, MRA_BANK5 ),
		new MemoryReadAddress( 0x3f9000, 0x3fffff, MRA_BANK6 ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	
	static MemoryWriteAddress main_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x0bffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x10ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x120000, 0x120fff, atarigen_eeprom_w, atarigen_eeprom, atarigen_eeprom_size ),
		new MemoryWriteAddress( 0x260040, 0x260041, atarigen_sound_w ),
		new MemoryWriteAddress( 0x260050, 0x260051, latch_w, latch_data ),
		new MemoryWriteAddress( 0x260060, 0x260061, atarigen_eeprom_enable_w ),
		new MemoryWriteAddress( 0x2a0000, 0x2a0001, watchdog_reset_w ),
		new MemoryWriteAddress( 0x3e0000, 0x3e0fff, atarigen_666_paletteram_w, paletteram ),
		new MemoryWriteAddress( 0x3effc0, 0x3effff, atarigen_video_control_w, atarigen_video_control_data ),
		new MemoryWriteAddress( 0x3f0000, 0x3f1fff, batman_playfield2ram_w, atarigen_playfield2ram, atarigen_playfield2ram_size ),
		new MemoryWriteAddress( 0x3f2000, 0x3f3fff, batman_playfieldram_w, atarigen_playfieldram, atarigen_playfieldram_size ),
		new MemoryWriteAddress( 0x3f4000, 0x3f5fff, batman_colorram_w, atarigen_playfieldram_color ),
		new MemoryWriteAddress( 0x3f6000, 0x3f7fff, MWA_BANK4, atarigen_spriteram, atarigen_spriteram_size ),
		new MemoryWriteAddress( 0x3f8000, 0x3f8fff, MWA_BANK5, atarigen_alpharam, atarigen_alpharam_size ),
		new MemoryWriteAddress( 0x3f9000, 0x3fffff, MWA_BANK6 ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	
	/*************************************
	 *
	 *	Port definitions
	 *
	 *************************************/
	
	static InputPortPtr input_ports_batman = new InputPortPtr(){ public void handler() { 
		PORT_START(); 		/* 26000 */
		PORT_BIT( 0x01ff, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER1 );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER1 );
	
		PORT_START(); 		/* 26002 */
		PORT_BIT( 0xffff, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 		/* 26010 */
		PORT_BIT( 0x000f, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_UNUSED );/* Input buffer full (@260030) */
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_UNUSED );/* Output buffer full (@260040) */
		PORT_SERVICE( 0x0040, IP_ACTIVE_LOW );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_VBLANK );
	
		/* audio board port */
                PORT_START(); 												
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN2 );			
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_COIN1 );			
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_TILT );			
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_SERVICE );		
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNUSED );/* self test */
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNUSED );/* output buffer full */
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNUSED );/* input buffer full */
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNUSED );/* self test */
                
	INPUT_PORTS_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Graphics definitions
	 *
	 *************************************/
	
	static GfxLayout anlayout = new GfxLayout
	(
		8,8,	/* 8*8 chars */
		8192,	/* 8192 chars */
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
		new int[] { 0*8*0x80000, 1*8*0x80000, 2*8*0x80000, 3*8*0x80000 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8	/* every sprite takes 8 consecutive bytes */
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX2, 0x040000, pfmolayout,  512, 64 ),		/* sprites  playfield */
		new GfxDecodeInfo( REGION_GFX2, 0x000000, pfmolayout,  256, 64 ),		/* sprites  playfield */
		new GfxDecodeInfo( REGION_GFX1, 0x000000, anlayout,      0, 64 ),		/* characters 8x8 */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	/*************************************
	 *
	 *	Machine driver
	 *
	 *************************************/
	
	static MachineDriver machine_driver_batman = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				ATARI_CLOCK_14MHz,
				main_readmem,main_writemem,null,null,
				ignore_interrupt,1
			),
			new MachineCPU(														
                            CPU_M6502,											
                            ATARI_CLOCK_14MHz/8,								
                            atarijsa3_readmem,atarijsa3_writemem,null,null,			
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
		2048, 2048,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK,
		null,
		batman_vh_start,
		batman_vh_stop,
		batman_vh_screenrefresh,
	
		/* sound hardware */
		//JSA_III_MONO(REGION_SOUND1),
                0, 0, 0, 0,
                new MachineSound[]{
                    new MachineSound(
                            SOUND_YM2151, 									
                            atarijsa_ym2151_interface_mono
                    ),
                    new MachineSound(
                            SOUND_OKIM6295,									
                            atarijsa_okim6295_interface_REGION_SOUND1
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
		UBytePtr base = new UBytePtr(memory_region(REGION_SOUND1));
		int i;
	
		/* invert the graphics bits on the playfield and motion objects */
		for (i = 0; i < memory_region_length(REGION_GFX2); i++)
			memory_region(REGION_GFX2).write(i, memory_region(REGION_GFX2).read(i) ^ 0xff);
	
		/* expand the ADPCM data to avoid lots of memcpy's during gameplay */
		/* the upper 128k is fixed, the lower 128k is bankswitched */
		memcpy(new UBytePtr(base, 0x00000), new UBytePtr(base, 0x80000), 0x20000);
		memcpy(new UBytePtr(base, 0x40000), new UBytePtr(base, 0x80000), 0x20000);
		memcpy(new UBytePtr(base, 0x80000), new UBytePtr(base, 0xa0000), 0x20000);
		memcpy(new UBytePtr(base, 0xc0000), new UBytePtr(base, 0xc0000), 0x20000);
	
		memcpy(new UBytePtr(base, 0x20000), new UBytePtr(base, 0xe0000), 0x20000);
		memcpy(new UBytePtr(base, 0x60000), new UBytePtr(base, 0xe0000), 0x20000);
		memcpy(new UBytePtr(base, 0xa0000), new UBytePtr(base, 0xe0000), 0x20000);
		memcpy(new UBytePtr(base, 0xe0000), new UBytePtr(base, 0xe0000), 0x20000);
	}
	
	
	
	/*************************************
	 *
	 *	Driver initialization
	 *
	 *************************************/
	
	public static InitDriverPtr init_batman = new InitDriverPtr() { public void handler() 
	{
		char default_eeprom[] =
		{
			0x0001,0x01F1,0x0154,0x01C5,0x0100,0x0113,0x0300,0x0173,
			0x0700,0x0154,0x0200,0x0107,0x0100,0x0120,0x0300,0x0165,
			0x0125,0x0100,0x0149,0x019D,0x016C,0x018B,0x01F1,0x0154,
			0x01C5,0x0100,0x0113,0x0300,0x0173,0x0700,0x0154,0x0200,
			0x0107,0x0100,0x0120,0x0300,0x0165,0x0125,0x0100,0x0149,
			0x019D,0x016C,0x018B,0x6800,0x0134,0x0113,0x0148,0x0100,
			0x019A,0x0105,0x01DC,0x01A2,0x013A,0x0139,0x0100,0x0105,
			0x01AB,0x016A,0x0149,0x0100,0x01ED,0x0105,0x0185,0x01B2,
			0x0134,0x0100,0x0105,0x0160,0x01AA,0x0149,0x0100,0x0105,
			0x012A,0x0152,0x0110,0x0100,0x0168,0x0105,0x0113,0x012E,
			0x0150,0x0218,0x01D0,0x0100,0x01D0,0x0300,0x01D0,0x0600,
			0x01D0,0x02C8,0x0000
		};
	
		atarigen_eeprom_default = new UShortArray(default_eeprom);
		atarijsa_init(1, 3, 2, 0x0040);
	
		/* speed up the 6502 */
		atarigen_init_6502_speedup(1, 0x4163, 0x417b);
	
		/* display messages */
		atarigen_show_sound_message();
	
		rom_decode();
	} };
	
	
	
	/*************************************
	 *
	 *	ROM definition(s)
	 *
	 *************************************/
	
	static RomLoadPtr rom_batman = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0xc0000, REGION_CPU1 );/* 6*128k for 68000 code */
		ROM_LOAD_EVEN( "085-2030.10r",  0x00000, 0x20000, 0x7cf4e5bf );
		ROM_LOAD_ODD ( "085-2031.7r",   0x00000, 0x20000, 0x7d7f3fc4 );
		ROM_LOAD_EVEN( "085-2032.91r",  0x40000, 0x20000, 0xd80afb20 );
		ROM_LOAD_ODD ( "085-2033.6r",   0x40000, 0x20000, 0x97efa2b8 );
		ROM_LOAD_EVEN( "085-2034.9r",   0x80000, 0x20000, 0x05388c62 );
		ROM_LOAD_ODD ( "085-2035.5r",   0x80000, 0x20000, 0xe77c92dd );
	
		ROM_REGION( 0x14000, REGION_CPU2 );/* 64k + 16k for 6502 code */
		ROM_LOAD( "085-1040.12c",  0x10000, 0x4000, 0x080db83c );
		ROM_CONTINUE(              0x04000, 0xc000 );
	
		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "085-2009.10m",  0x00000, 0x20000, 0xa82d4923 );/* alphanumerics */
	
		ROM_REGION( 0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "085-1010.13r",  0x000000, 0x20000, 0x466e1365 );/* graphics, plane 0 */
		ROM_LOAD( "085-1014.14r",  0x020000, 0x20000, 0xef53475a );
		ROM_LOAD( "085-1018.15r",  0x040000, 0x20000, 0x4c14f1e5 );
		ROM_LOAD( "085-1022.16r",  0x060000, 0x20000, 0x7476a15d );
	
		ROM_LOAD( "085-1011.13m",  0x080000, 0x20000, 0x8cda5efc );/* graphics, plane 1 */
		ROM_LOAD( "085-1015.14m",  0x0a0000, 0x20000, 0x043e7f8b );
		ROM_LOAD( "085-1019.15m",  0x0c0000, 0x20000, 0x2046d9ec );
		ROM_LOAD( "085-1023.16m",  0x0e0000, 0x20000, 0x75cac686 );
	
		ROM_LOAD( "085-1012.13f",  0x100000, 0x20000, 0xb017f2c3 );/* graphics, plane 2 */
		ROM_LOAD( "085-1016.14f",  0x120000, 0x20000, 0x70aa2360 );
		ROM_LOAD( "085-1020.15f",  0x140000, 0x20000, 0xcc4f4b94 );
		ROM_LOAD( "085-1024.16f",  0x160000, 0x20000, 0xd60d35e0 );
	
		ROM_LOAD( "085-1013.13c",  0x180000, 0x20000, 0x68b64975 );/* graphics, plane 3 */
		ROM_LOAD( "085-1017.14c",  0x1a0000, 0x20000, 0xe4af157b );
		ROM_LOAD( "085-1021.15c",  0x1c0000, 0x20000, 0x9c8ef9ba );
		ROM_LOAD( "085-1025.16c",  0x1e0000, 0x20000, 0x5d30bcd1 );
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* 1MB for ADPCM */
		ROM_LOAD( "085-1041.19e",  0x80000, 0x20000, 0xd97d5dbb );
		ROM_LOAD( "085-1042.17e",  0xa0000, 0x20000, 0x8c496986 );
		ROM_LOAD( "085-1043.15e",  0xc0000, 0x20000, 0x51812d3b );
		ROM_LOAD( "085-1044.12e",  0xe0000, 0x20000, 0x5e2d7f31 );
	ROM_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Game driver(s)
	 *
	 *************************************/
	
	public static GameDriver driver_batman	   = new GameDriver("1991"	,"batman"	,"batman.java"	,rom_batman,null	,machine_driver_batman	,input_ports_batman	,init_batman	,ROT0	,	"Atari Games", "Batman" );
}
