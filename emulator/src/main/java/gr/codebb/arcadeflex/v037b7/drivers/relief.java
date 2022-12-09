/***************************************************************************

	Relief Pitcher

    driver by Aaron Giles

****************************************************************************/


/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.drivers;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import gr.codebb.arcadeflex.common.SubArrays.UShortArray;
import static common.libc.cstring.memcpy;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.inputH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.relief.*;
import static gr.codebb.arcadeflex.v037b7.machine.atarigen.*;
import static gr.codebb.arcadeflex.v037b7.machine.atarigenH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import arcadeflex.v036.sound._2413intfH.YM2413interface;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295.*;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295H.*;
import static gr.codebb.arcadeflex.v037b7.sound.ym2413.*;

public class relief
{
	
	static int ym2413_volume;
	static int overall_volume;
	static int adpcm_bank_base;
	
	
	
	/*************************************
	 *
	 *	Interrupt handling
	 *
	 *************************************/
	
	static atarigen_int_callbackPtr update_interrupts = new atarigen_int_callbackPtr() {
            @Override
            public void handler() {
                int newstate = 0;
	
		if (atarigen_scanline_int_state != 0)
			newstate = 4;
	
		if (newstate != 0)
			cpu_set_irq_line(0, newstate, ASSERT_LINE);
		else
			cpu_set_irq_line(0, 7, CLEAR_LINE);
            }
        };
        	
	
	/*************************************
	 *
	 *	Initialization
	 *
	 *************************************/
	
	public static InitMachineHandlerPtr init_machine = new InitMachineHandlerPtr() { public void handler() 
	{
		atarigen_eeprom_reset();
		atarigen_video_control_reset();
		atarigen_interrupt_reset(update_interrupts);
		atarigen_scanline_timer_reset(relief_scanline_update, 8);
	
		OKIM6295_set_bank_base(0, ALL_VOICES, 0);
		ym2413_volume = 15;
		overall_volume = 127;
		adpcm_bank_base = 0;
	} };
	
	
	
	/*************************************
	 *
	 *	I/O handling
	 *
	 *************************************/
	
	public static ReadHandlerPtr special_port2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int result = input_port_2_r.handler(offset);
		if (atarigen_cpu_to_sound_ready != 0) result ^= 0x0020;
		if ((result & 0x0080)==0 || atarigen_get_hblank()!=0) result ^= 0x0001;
		return result;
	} };
	
	
	
	/*************************************
	 *
	 *	Audio control I/O
	 *
	 *************************************/
	
	public static WriteHandlerPtr audio_control_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		//(void)offset;
		if ((data & 0x00ff0000)==0)
		{
			ym2413_volume = (data >> 1) & 15;
			atarigen_set_ym2413_vol((ym2413_volume * overall_volume * 100) / (127 * 15));
			adpcm_bank_base = (0x040000 * ((data >> 6) & 3)) | (adpcm_bank_base & 0x100000);
		}
		if ((data & 0xff000000)==0)
			adpcm_bank_base = (0x100000 * ((data >> 8) & 1)) | (adpcm_bank_base & 0x0c0000);
	
		OKIM6295_set_bank_base(0, ALL_VOICES, adpcm_bank_base);
	} };
	
	
	public static WriteHandlerPtr audio_volume_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		//(void)offset;
		if ((data & 0x00ff0000)==0)
		{
			overall_volume = data & 127;
			atarigen_set_ym2413_vol((ym2413_volume * overall_volume * 100) / (127 * 15));
			atarigen_set_oki6295_vol(overall_volume * 100 / 127);
		}
	} };
	
	
	
	/*************************************
	 *
	 *	MSM5295 I/O
	 *
	 *************************************/
	
	public static ReadHandlerPtr adpcm_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return OKIM6295_status_0_r.handler(offset) | 0xff00;
	} };
	
	
	public static WriteHandlerPtr adpcm_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000)==0)
			OKIM6295_data_0_w.handler(offset, data & 0xff);
	} };
	
	
	
	/*************************************
	 *
	 *	YM2413 I/O
	 *
	 *************************************/
	
	public static ReadHandlerPtr ym2413_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		//(void)offset;
		return YM2413_status_port_0_r.handler(0) | 0xff00;
	} };
	
	
	public static WriteHandlerPtr ym2413_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000)==0)
		{
			if ((offset & 2) != 0)
				YM2413_data_port_0_w.handler(0, data & 0xff);
			else
				YM2413_register_port_0_w.handler(0, data & 0xff);
		}
	} };
	
	
	
	/*************************************
	 *
	 *	Main CPU memory handlers
	 *
	 *************************************/
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x140000, 0x140003, ym2413_r ),
		new MemoryReadAddress( 0x140010, 0x140011, adpcm_r ),
		new MemoryReadAddress( 0x180000, 0x180fff, atarigen_eeprom_upper_r ),
		new MemoryReadAddress( 0x260000, 0x260001, input_port_0_r ),
		new MemoryReadAddress( 0x260002, 0x260003, input_port_1_r ),
		new MemoryReadAddress( 0x260010, 0x260011, special_port2_r ),
		new MemoryReadAddress( 0x260012, 0x260013, input_port_3_r ),
		new MemoryReadAddress( 0x3effc0, 0x3effff, atarigen_video_control_r ),
		new MemoryReadAddress( 0xfe0000, 0xfe0fff, MRA_BANK1 ),
		new MemoryReadAddress( 0xfeffc0, 0xfeffff, atarigen_video_control_r ),
		new MemoryReadAddress( 0xff0000, 0xff5fff, MRA_BANK3 ),
		new MemoryReadAddress( 0xff6000, 0xff7fff, MRA_BANK4 ),
		new MemoryReadAddress( 0xff8000, 0xff8fff, MRA_BANK5 ),
		new MemoryReadAddress( 0xff9000, 0xffffff, MRA_BANK6 ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x140000, 0x140003, ym2413_w ),
		new MemoryWriteAddress( 0x140010, 0x140011, adpcm_w ),
		new MemoryWriteAddress( 0x140020, 0x140021, audio_volume_w ),
		new MemoryWriteAddress( 0x140030, 0x140031, audio_control_w ),
		new MemoryWriteAddress( 0x180000, 0x180fff, atarigen_eeprom_w, atarigen_eeprom, atarigen_eeprom_size ),
		new MemoryWriteAddress( 0x1c0030, 0x1c0031, atarigen_eeprom_enable_w ),
		new MemoryWriteAddress( 0x2a0000, 0x2a0001, watchdog_reset_w ),
		new MemoryWriteAddress( 0x3effc0, 0x3effff, atarigen_video_control_w, atarigen_video_control_data ),
		new MemoryWriteAddress( 0xfe0000, 0xfe0fff, atarigen_666_paletteram_w, paletteram ),
		new MemoryWriteAddress( 0xfeffc0, 0xfeffff, atarigen_video_control_w ),
		new MemoryWriteAddress( 0xff0000, 0xff1fff, relief_playfield2ram_w, atarigen_playfield2ram, atarigen_playfield2ram_size ),
		new MemoryWriteAddress( 0xff2000, 0xff3fff, relief_playfieldram_w, atarigen_playfieldram, atarigen_playfieldram_size ),
		new MemoryWriteAddress( 0xff4000, 0xff5fff, relief_colorram_w, atarigen_playfieldram_color ),
		new MemoryWriteAddress( 0xff6000, 0xff7fff, MWA_BANK4, atarigen_spriteram, atarigen_spriteram_size ),
		new MemoryWriteAddress( 0xff8000, 0xff8fff, MWA_BANK5, atarigen_alpharam, atarigen_alpharam_size ),
		new MemoryWriteAddress( 0xff9000, 0xffffff, MWA_BANK6 ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	
	/*************************************
	 *
	 *	Port definitions
	 *
	 *************************************/
	
	static InputPortPtr input_ports_relief = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* 260000 */
		PORT_BIT(  0x00ff, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT(  0x0100, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );
		PORT_BIT(  0x0100, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT(  0x0200, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT(  0x0400, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT(  0x0800, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT(  0x1000, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 );
		PORT_BIT(  0x2000, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1 );
		PORT_BIT(  0x4000, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER1 );
		PORT_BIT(  0x8000, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER1 );
	
		PORT_START(); 	/* 260002 */
		PORT_BIT(  0x00ff, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT(  0x0100, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );
		PORT_BIT(  0x0100, IP_ACTIVE_LOW, IPT_START3 );
		PORT_BIT(  0x0200, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT(  0x0400, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT(  0x0800, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT(  0x1000, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
		PORT_BIT(  0x2000, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2 );
		PORT_BIT(  0x4000, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2 );
		PORT_BIT(  0x8000, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2 );
	
		PORT_START(); 	/* 260010 */
		PORT_BIT(  0x0001, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT(  0x0002, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT(  0x0004, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT(  0x0008, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT(  0x0010, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT(  0x0020, IP_ACTIVE_LOW, IPT_UNUSED );/* tested before writing to 260040 */
		PORT_SERVICE( 0x0040, IP_ACTIVE_LOW );
		PORT_BIT(  0x0080, IP_ACTIVE_LOW, IPT_VBLANK );
		PORT_BIT(  0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* 260012 */
		PORT_BIT(  0x0001, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT(  0x0002, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT(  0x0020, IP_ACTIVE_LOW, IPT_SERVICE );
		PORT_BIT(  0xffdc, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Graphics definitions
	 *
	 *************************************/
	
	static GfxLayout pfanmolayout = new GfxLayout
	(
		8,8,	/* 8*8 sprites */
		32768,	/* 32768 of them */
		4,		/* 4 bits per pixel */
		new int[] { 0x80000*3*8, 0x80000*2*8, 0x80000*1*8, 0x80000*0*8 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 2*8, 4*8, 6*8, 8*8, 10*8, 12*8, 14*8 },
		16*8	/* every sprite takes 16 consecutive bytes */
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0x000000, pfanmolayout,   0, 64 ),		/* alpha  playfield */
		new GfxDecodeInfo( REGION_GFX1, 0x000001, pfanmolayout, 256, 16 ),		/* sprites */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	/*************************************
	 *
	 *	Sound definitions
	 *
	 *************************************/
	
	static OKIM6295interface okim6295_interface = new OKIM6295interface
	(
		1,					/* 1 chip */
		new int[] { ATARI_CLOCK_14MHz/4/3/165 },
		new int[] { REGION_SOUND1 },
		new int[] { 75 }
	);
	
	
	static YM2413interface ym2413_interface = new YM2413interface
	(
		1,					/* 1 chip */
		ATARI_CLOCK_14MHz/4,
		new int[] { 75 },
		new WriteYmHandlerPtr[]{ null }
	);
	
	
	/*************************************
	 *
	 *	Machine driver
	 *
	 *************************************/
	
	static MachineDriver machine_driver_relief = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,		/* verified */
				ATARI_CLOCK_14MHz/2,
				readmem,writemem,null,null,
				ignore_interrupt,1
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
		relief_vh_start,
		relief_vh_stop,
		relief_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_OKIM6295,
				okim6295_interface
			),
			new MachineSound(
				SOUND_YM2413,
				ym2413_interface
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
	
		/* invert the graphics bits */
		for (i = 0; i < memory_region_length(REGION_GFX1); i++)
			memory_region(REGION_GFX1).write(i, memory_region(REGION_GFX1).read(i) ^ 0xff);
	
		/* expand the ADPCM data to avoid lots of memcpy's during gameplay */
		/* the upper 128k is fixed, the lower 128k is bankswitched */
		memcpy(new UBytePtr(base, 0x000000), new UBytePtr(base, 0x100000), 0x20000);
		memcpy(new UBytePtr(base, 0x040000), new UBytePtr(base, 0x100000), 0x20000);
		memcpy(new UBytePtr(base, 0x080000), new UBytePtr(base, 0x140000), 0x20000);
		memcpy(new UBytePtr(base, 0x0c0000), new UBytePtr(base, 0x160000), 0x20000);
		memcpy(new UBytePtr(base, 0x100000), new UBytePtr(base, 0x180000), 0x20000);
		memcpy(new UBytePtr(base, 0x140000), new UBytePtr(base, 0x1a0000), 0x20000);
		memcpy(new UBytePtr(base, 0x180000), new UBytePtr(base, 0x1c0000), 0x20000);
		memcpy(new UBytePtr(base, 0x1c0000), new UBytePtr(base, 0x1e0000), 0x20000);
	
		memcpy(new UBytePtr(base, 0x020000), new UBytePtr(base, 0x120000), 0x20000);
		memcpy(new UBytePtr(base, 0x060000), new UBytePtr(base, 0x120000), 0x20000);
		memcpy(new UBytePtr(base, 0x0a0000), new UBytePtr(base, 0x120000), 0x20000);
		memcpy(new UBytePtr(base, 0x0e0000), new UBytePtr(base, 0x120000), 0x20000);
		memcpy(new UBytePtr(base, 0x120000), new UBytePtr(base, 0x120000), 0x20000);
		memcpy(new UBytePtr(base, 0x160000), new UBytePtr(base, 0x120000), 0x20000);
		memcpy(new UBytePtr(base, 0x1a0000), new UBytePtr(base, 0x120000), 0x20000);
		memcpy(new UBytePtr(base, 0x1e0000), new UBytePtr(base, 0x120000), 0x20000);
	}
	
	
	
	/*************************************
	 *
	 *	Driver initialization
	 *
	 *************************************/
	
	public static InitDriverHandlerPtr init_relief = new InitDriverHandlerPtr() { public void handler() 
	{
		char default_eeprom[] =
		{
			0x0001,0x0166,0x0128,0x01E6,0x0100,0x012C,0x0300,0x0144,
			0x0700,0x01C0,0x2F00,0x01EC,0x0B00,0x0148,0x0140,0x0100,
			0x0124,0x0188,0x0120,0x0600,0x0196,0x013C,0x0192,0x0150,
			0x0166,0x0128,0x01E6,0x0100,0x012C,0x0300,0x0144,0x0700,
			0x01C0,0x2F00,0x01EC,0x0B00,0x0148,0x0140,0x0100,0x0124,
			0x0188,0x0120,0x0600,0x0196,0x013C,0x0192,0x0150,0xFF00,
			0x9500,0x0000
		};
	
		atarigen_eeprom_default = new UShortArray(default_eeprom);
	
		rom_decode();
	} };
	
	
	public static InitDriverHandlerPtr init_relief2 = new InitDriverHandlerPtr() { public void handler() 
	{
		char default_eeprom[] =
		{
			0x0001,0x01FD,0x019F,0x015E,0x01FF,0x019E,0x03FF,0x015F,
			0x07FF,0x01FD,0x12FF,0x01FC,0x01FB,0x07FF,0x01F7,0x01FF,
			0x01DF,0x02FF,0x017F,0x03FF,0x0300,0x0110,0x0300,0x0140,
			0x0300,0x018E,0x0400,0x0180,0x0101,0x0300,0x0180,0x0204,
			0x0120,0x0182,0x0100,0x0102,0x0600,0x01D5,0x0138,0x0192,
			0x0150,0x01FD,0x019F,0x015E,0x01FF,0x019E,0x03FF,0x015F,
			0x07FF,0x01FD,0x12FF,0x01FC,0x01FB,0x07FF,0x01F7,0x01FF,
			0x01DF,0x02FF,0x017F,0x03FF,0x0300,0x0110,0x0300,0x0140,
			0x0300,0x018E,0x0400,0x0180,0x0101,0x0300,0x0180,0x0204,
			0x0120,0x0182,0x0100,0x0102,0x0600,0x01D5,0x0138,0x0192,
			0x0150,0xE600,0x01C3,0x019D,0x0131,0x0100,0x0116,0x0100,
			0x010A,0x0190,0x010E,0x014A,0x0200,0x010B,0x018D,0x0121,
			0x0100,0x0145,0x0100,0x0109,0x0184,0x012C,0x0200,0x0107,
			0x01AA,0x0149,0x60FF,0x3300,0x0000
		};
	
		atarigen_eeprom_default = new UShortArray(default_eeprom);
	
		rom_decode();
	} };
	
	
	
	/*************************************
	 *
	 *	ROM definition(s)
	 *
	 *************************************/
	
	static RomLoadHandlerPtr rom_relief = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1 );/* 8*64k for 68000 code */
		ROM_LOAD_EVEN( "0011d.19e", 0x00000, 0x20000, 0xcb3f73ad );
		ROM_LOAD_ODD ( "0012d.19j", 0x00000, 0x20000, 0x90655721 );
		ROM_LOAD_EVEN( "093-0013.17e", 0x40000, 0x20000, 0x1e1e82e5 );
		ROM_LOAD_ODD ( "093-0014.17j", 0x40000, 0x20000, 0x19e5decd );
	
		ROM_REGION( 0x240000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "093-0025.14s", 0x000000, 0x80000, 0x1b9e5ef2 );
		ROM_LOAD( "093-0026.8d",  0x080000, 0x80000, 0x09b25d93 );
		ROM_LOAD( "093-0027.18s", 0x100000, 0x80000, 0x5bc1c37b );
		ROM_LOAD( "093-0028.10d", 0x180000, 0x80000, 0x55fb9111 );
		ROM_LOAD( "093-0029.4d",  0x200000, 0x40000, 0xe4593ff4 );
	
		ROM_REGION( 0x200000, REGION_SOUND1 );/* 2MB for ADPCM data */
		ROM_LOAD( "093-0030.9b",  0x100000, 0x80000, 0xf4c567f5 );
		ROM_LOAD( "093-0031.10b", 0x180000, 0x80000, 0xba908d73 );
	ROM_END(); }}; 
	
	static RomLoadHandlerPtr rom_relief2 = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1 );/* 8*64k for 68000 code */
		ROM_LOAD_EVEN( "093-0011.19e", 0x00000, 0x20000, 0x794cea33 );
		ROM_LOAD_ODD ( "093-0012.19j", 0x00000, 0x20000, 0x577495f8 );
		ROM_LOAD_EVEN( "093-0013.17e", 0x40000, 0x20000, 0x1e1e82e5 );
		ROM_LOAD_ODD ( "093-0014.17j", 0x40000, 0x20000, 0x19e5decd );
	
		ROM_REGION( 0x240000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "093-0025.14s", 0x000000, 0x80000, 0x1b9e5ef2 );
		ROM_LOAD( "093-0026.8d",  0x080000, 0x80000, 0x09b25d93 );
		ROM_LOAD( "093-0027.18s", 0x100000, 0x80000, 0x5bc1c37b );
		ROM_LOAD( "093-0028.10d", 0x180000, 0x80000, 0x55fb9111 );
		ROM_LOAD( "093-0029.4d",  0x200000, 0x40000, 0xe4593ff4 );
	
		ROM_REGION( 0x200000, REGION_SOUND1 );/* 2MB for ADPCM data */
		ROM_LOAD( "093-0030.9b",  0x100000, 0x80000, 0xf4c567f5 );
		ROM_LOAD( "093-0031.10b", 0x180000, 0x80000, 0xba908d73 );
	ROM_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Game driver(s)
	 *
	 *************************************/
	
	public static GameDriver driver_relief	   = new GameDriver("1992"	,"relief"	,"relief.java"	,rom_relief,null	,machine_driver_relief	,input_ports_relief	,init_relief	,ROT0	,	"Atari Games", "Relief Pitcher (set 1)" );
	public static GameDriver driver_relief2	   = new GameDriver("1992"	,"relief2"	,"relief.java"	,rom_relief2,driver_relief	,machine_driver_relief	,input_ports_relief	,init_relief2	,ROT0	,	"Atari Games", "Relief Pitcher (set 2)" );
}
