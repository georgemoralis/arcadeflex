/***************************************************************************

	Shuuz

    driver by Aaron Giles

****************************************************************************/


/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package gr.codebb.arcadeflex.v036.drivers;
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295.*;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295H.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.inputH.*;
import static gr.codebb.arcadeflex.v036.machine.atarigen.*;
import static gr.codebb.arcadeflex.v036.machine.atarigenH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.shuuz.*;
public class shuuz
{

	
	
	
	/*************************************
	 *
	 *	Interrupt handling
	 *
	 *************************************/
	public static atarigen_void_callbackPtr update_interrupts = new atarigen_void_callbackPtr() {
        public void handler() {
		int newstate = 0;
	
		if (atarigen_scanline_int_state!=0)
			newstate = 4;
	
		if (newstate!=0)
			cpu_set_irq_line(0, newstate, ASSERT_LINE);
		else
			cpu_set_irq_line(0, 7, CLEAR_LINE);
	}};
	
	
	
	/*************************************
	 *
	 *	Initialization
	 *
	 *************************************/
	
	public static InitMachinePtr init_machine = new InitMachinePtr() { public void handler() 
	{
		atarigen_eeprom_reset();
		atarigen_video_control_reset();
		atarigen_interrupt_reset(update_interrupts);
		atarigen_scanline_timer_reset(shuuz_scanline_update, 8);
	} };
	
	
	public static WriteHandlerPtr latch_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
            
	} };
	
	
	
	/*************************************
	 *
	 *	LETA I/O
	 *
	 *************************************/
	static int[] cur=new int[2];
	public static ReadHandlerPtr leta_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		/* trackball -- rotated 45 degrees? */
		
		int which = (offset >> 1) & 1;
	
		/* when reading the even ports, do a real analog port update */
		if (which == 0)
		{
			int dx = (byte)input_port_2_r.handler(offset);
			int dy = (byte)input_port_3_r.handler(offset);
	
			cur[0] = dx + dy;
			cur[1] = dx - dy;
		}
	
		/* clip the result to -0x3f to +0x3f to remove directional ambiguities */
		return cur[which];
	} };
	
	
	
	/*************************************
	 *
	 *	MSM5295 I/O
	 *
	 *************************************/
	
	public static ReadHandlerPtr adpcm_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return OKIM6295_status_0_r.handler(offset) | 0xff00;
	} };
	
	
	public static WriteHandlerPtr adpcm_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000)==0)
			OKIM6295_data_0_w.handler(offset, data & 0xff);
	} };
	
	
	
	/*************************************
	 *
	 *	Additional I/O
	 *
	 *************************************/
	
	public static ReadHandlerPtr special_port0_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		int result = input_port_0_r.handler(offset);
	
		if (((result & 0x0800)!=0) && atarigen_get_hblank())
			result &= ~0x0800;
	
		return result;
	} };
	
	
	
	/*************************************
	 *
	 *	Main CPU memory handlers
	 *
	 *************************************/
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x100fff, atarigen_eeprom_r ),
		new MemoryReadAddress( 0x103000, 0x103003, leta_r ),
		new MemoryReadAddress( 0x105000, 0x105001, special_port0_r ),
		new MemoryReadAddress( 0x105002, 0x105003, input_port_1_r ),
		new MemoryReadAddress( 0x106000, 0x106001, adpcm_r ),
		new MemoryReadAddress( 0x107000, 0x107007, MRA_NOP ),
		new MemoryReadAddress( 0x3e0000, 0x3e087f, MRA_BANK1 ),
		new MemoryReadAddress( 0x3effc0, 0x3effff, atarigen_video_control_r ),
		new MemoryReadAddress( 0x3f4000, 0x3f7fff, MRA_BANK2 ),
		new MemoryReadAddress( 0x3f8000, 0x3fcfff, MRA_BANK3 ),
		new MemoryReadAddress( 0x3fd000, 0x3fd3ff, MRA_BANK4 ),
		new MemoryReadAddress( 0x3fd400, 0x3fffff, MRA_BANK5 ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x100fff, atarigen_eeprom_w, atarigen_eeprom, atarigen_eeprom_size ),
		new MemoryWriteAddress( 0x101000, 0x101fff, atarigen_eeprom_enable_w ),
		new MemoryWriteAddress( 0x102000, 0x102001, watchdog_reset_w ),
		new MemoryWriteAddress( 0x105000, 0x105001, latch_w ),
		new MemoryWriteAddress( 0x106000, 0x106001, adpcm_w ),
		new MemoryWriteAddress( 0x107000, 0x107007, MWA_NOP ),
		new MemoryWriteAddress( 0x3e0000, 0x3e087f, atarigen_666_paletteram_w, paletteram ),
		new MemoryWriteAddress( 0x3effc0, 0x3effff, atarigen_video_control_w, atarigen_video_control_data ),
		new MemoryWriteAddress( 0x3f4000, 0x3f7fff, shuuz_playfieldram_w, atarigen_playfieldram, atarigen_playfieldram_size ),
		new MemoryWriteAddress( 0x3f8000, 0x3fcfff, MWA_BANK3 ),
		new MemoryWriteAddress( 0x3fd000, 0x3fd3ff, MWA_BANK4, atarigen_spriteram ),
		new MemoryWriteAddress( 0x3fd400, 0x3fffff, MWA_BANK5 ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	
	/*************************************
	 *
	 *	Port definitions
	 *
	 *************************************/
	
	static InputPortPtr input_ports_shuuz = new InputPortPtr(){ public void handler() { 
		PORT_START(); 
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x07fc, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_VBLANK );
		PORT_BIT( 0xf000, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x07fc, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_SERVICE( 0x0800, IP_ACTIVE_LOW );
		PORT_BIT( 0xf000, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 
	    PORT_ANALOG( 0xff, 0, IPT_TRACKBALL_X | IPF_PLAYER1, 50, 30, 0, 0 );
		PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 
	    PORT_ANALOG( 0xff, 0, IPT_TRACKBALL_Y | IPF_REVERSE | IPF_PLAYER1, 50, 30, 0, 0 );
		PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_shuuz2 = new InputPortPtr(){ public void handler() { 
		PORT_START(); 
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x00fc, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BITX(  0x0100, IP_ACTIVE_LOW, 0, "Step Debug SW", KEYCODE_S, IP_JOY_NONE );
		PORT_BIT( 0x0600, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_VBLANK );
		PORT_BITX(  0x1000, IP_ACTIVE_LOW, 0, "Playfield Debug SW", KEYCODE_Y, IP_JOY_NONE );
		PORT_BITX(  0x2000, IP_ACTIVE_LOW, 0, "Reset Debug SW", KEYCODE_E, IP_JOY_NONE );
		PORT_BITX(  0x4000, IP_ACTIVE_LOW, 0, "Crosshair Debug SW", KEYCODE_C, IP_JOY_NONE );
		PORT_BITX(  0x8000, IP_ACTIVE_LOW, 0, "Freeze Debug SW", KEYCODE_F, IP_JOY_NONE );
	
		PORT_START(); 
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x00fc, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BITX(  0x0100, IP_ACTIVE_LOW, 0, "Replay Debug SW", KEYCODE_R, IP_JOY_NONE );
		PORT_BIT( 0x0600, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_SERVICE( 0x0800, IP_ACTIVE_LOW );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER1 );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER1 );
	
		PORT_START(); 
	    PORT_ANALOG( 0xff, 0, IPT_TRACKBALL_X | IPF_PLAYER1, 50, 30, 0, 0 );
		PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 
	    PORT_ANALOG( 0xff, 0, IPT_TRACKBALL_Y | IPF_REVERSE | IPF_PLAYER1, 50, 30, 0, 0 );
		PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Graphics definitions
	 *
	 *************************************/
	
	static GfxLayout pflayout = new GfxLayout
	(
		8,8,	/* 8*8 sprites */
		16384,	/* 16384 of them */
		4,		/* 4 bits per pixel */
		new int[] { 0, 4, 0+0x40000*8, 4+0x40000*8 },
		new int[] { 0, 1, 2, 3, 8, 9, 10, 11 },
		new int[] { 0*8, 2*8, 4*8, 6*8, 8*8, 10*8, 12*8, 14*8 },
		16*8	/* every sprite takes 16 consecutive bytes */
	);
	
	
	static GfxLayout molayout = new GfxLayout
	(
		8,8,	/* 8*8 sprites */
		32768,	/* 32768 of them */
		4,		/* 4 bits per pixel */
		new int[] { 0, 4, 0+0x80000*8, 4+0x80000*8 },
		new int[] { 0, 1, 2, 3, 8, 9, 10, 11 },
		new int[] { 0*8, 2*8, 4*8, 6*8, 8*8, 10*8, 12*8, 14*8 },
		16*8	/* every sprite takes 16 consecutive bytes */
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, pflayout,  256, 16 ),		/* sprites  playfield */
		new GfxDecodeInfo( REGION_GFX2, 0, molayout,    0, 16 ),		/* sprites  playfield */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	/*************************************
	 *
	 *	Sound definitions
	 *
	 *************************************/
	
	static  OKIM6295interface okim6295_interface = new OKIM6295interface
	(
		1,					/* 1 chip */
		new int[]{ ATARI_CLOCK_14MHz/16/132 },
		new int[]{ REGION_SOUND1 },
		new int[]{ 100 }
        );
	
	
	
	/*************************************
	 *
	 *	Machine driver
	 *
	 *************************************/
	
	static MachineDriver machine_driver_shuuz = new MachineDriver
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
		1024, 1024,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK | VIDEO_SUPPORTS_DIRTY,
		null,
		shuuz_vh_start,
		shuuz_vh_stop,
		shuuz_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_OKIM6295,
				okim6295_interface
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
	
		for (i = 0; i < memory_region_length(REGION_GFX1); i++)
			new UBytePtr(memory_region(REGION_GFX1)).write(i,new UBytePtr(memory_region(REGION_GFX1)).read() ^ 0xff);
		for (i = 0; i < memory_region_length(REGION_GFX2); i++)
			new UBytePtr(memory_region(REGION_GFX2)).write(i,new UBytePtr(memory_region(REGION_GFX2)).read() ^ 0xff);
	}
	
	
	
	/*************************************
	 *
	 *	ROM definition(s)
	 *
	 *************************************/
	
	static RomLoadPtr rom_shuuz = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );/* 4*64k for 68000 code */
		ROM_LOAD_EVEN( "4010.23p",     0x00000, 0x20000, 0x1c2459f8 );
		ROM_LOAD_ODD ( "4011.13p",     0x00000, 0x20000, 0x6db53a85 );
	
		ROM_REGION( 0x080000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "2030.43x", 0x000000, 0x20000, 0x8ecf1ed8 );
		ROM_LOAD( "2032.20x", 0x020000, 0x20000, 0x5af184e6 );
		ROM_LOAD( "2031.87x", 0x040000, 0x20000, 0x72e9db63 );
		ROM_LOAD( "2033.65x", 0x060000, 0x20000, 0x8f552498 );
	
		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "1020.43u", 0x000000, 0x20000, 0xd21ad039 );
		ROM_LOAD( "1022.20u", 0x020000, 0x20000, 0x0c10bc90 );
		ROM_LOAD( "1024.43m", 0x040000, 0x20000, 0xadb09347 );
		ROM_LOAD( "1026.20m", 0x060000, 0x20000, 0x9b20e13d );
		ROM_LOAD( "1021.87u", 0x080000, 0x20000, 0x8388910c );
		ROM_LOAD( "1023.65u", 0x0a0000, 0x20000, 0x71353112 );
		ROM_LOAD( "1025.87m", 0x0c0000, 0x20000, 0xf7b20a64 );
		ROM_LOAD( "1027.65m", 0x0e0000, 0x20000, 0x55d54952 );
	
		ROM_REGION( 0x40000, REGION_SOUND1 );/* ADPCM data */
		ROM_LOAD( "1040.75b", 0x00000, 0x20000, 0x0896702b );
		ROM_LOAD( "1041.65b", 0x20000, 0x20000, 0xb3b07ce9 );
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_shuuz2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );/* 4*64k for 68000 code */
		ROM_LOAD_EVEN( "23p.rom",     0x00000, 0x20000, 0x98aec4e7 );
		ROM_LOAD_ODD ( "13p.rom",     0x00000, 0x20000, 0xdd9d5d5c );
	
		ROM_REGION( 0x080000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "2030.43x", 0x000000, 0x20000, 0x8ecf1ed8 );
		ROM_LOAD( "2032.20x", 0x020000, 0x20000, 0x5af184e6 );
		ROM_LOAD( "2031.87x", 0x040000, 0x20000, 0x72e9db63 );
		ROM_LOAD( "2033.65x", 0x060000, 0x20000, 0x8f552498 );
	
		ROM_REGION( 0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "1020.43u", 0x000000, 0x20000, 0xd21ad039 );
		ROM_LOAD( "1022.20u", 0x020000, 0x20000, 0x0c10bc90 );
		ROM_LOAD( "1024.43m", 0x040000, 0x20000, 0xadb09347 );
		ROM_LOAD( "1026.20m", 0x060000, 0x20000, 0x9b20e13d );
		ROM_LOAD( "1021.87u", 0x080000, 0x20000, 0x8388910c );
		ROM_LOAD( "1023.65u", 0x0a0000, 0x20000, 0x71353112 );
		ROM_LOAD( "1025.87m", 0x0c0000, 0x20000, 0xf7b20a64 );
		ROM_LOAD( "1027.65m", 0x0e0000, 0x20000, 0x55d54952 );
	
		ROM_REGION( 0x40000, REGION_SOUND1 );/* ADPCM data */
		ROM_LOAD( "1040.75b", 0x00000, 0x20000, 0x0896702b );
		ROM_LOAD( "1041.65b", 0x20000, 0x20000, 0xb3b07ce9 );
	ROM_END(); }}; 
	
	
	
	public static InitDriverPtr init_shuuz = new InitDriverPtr() { public void handler() 
	{
		atarigen_eeprom_default = null;
	
		rom_decode();
	} };
	
	
	
	public static GameDriver driver_shuuz	   = new GameDriver("1990"	,"shuuz"	,"shuuz.java"	,rom_shuuz,null	,machine_driver_shuuz	,input_ports_shuuz	,init_shuuz	,ROT0	,	"Atari Games", "Shuuz (version 8.0)" );
	public static GameDriver driver_shuuz2	   = new GameDriver("1990"	,"shuuz2"	,"shuuz.java"	,rom_shuuz2,driver_shuuz	,machine_driver_shuuz	,input_ports_shuuz2	,init_shuuz	,ROT0	,	"Atari Games", "Shuuz (version 7.1)" );
}
