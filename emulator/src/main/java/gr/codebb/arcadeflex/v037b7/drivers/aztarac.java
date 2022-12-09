/*
 * Aztarac driver
 *
 * Jul 25 1999 by Mathis Rosenhauer
 *
 * Thanks to David Fish for additional hardware information.
 *
 */

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.drivers;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.inputH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static arcadeflex.v036.sound._2151intf.*;
import arcadeflex.v036.sound._2151intfH.YM2151interface;
import static arcadeflex.v036.sound._2151intfH.YM3012_VOL;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;
import static arcadeflex.v036.cpu.z80.z80H.Z80_NMI_INT;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.aztarac.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v036.vidhrdw.vector.*;
import static arcadeflex.v036.machine.foodf.foodf_nvram_handler;
import static arcadeflex.v036.machine.foodf.foodf_nvram_r;
import static arcadeflex.v036.machine.foodf.foodf_nvram_w;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b7.sndhrdw.aztarac.*;
import static arcadeflex.v036.sound.ay8910.*;
import arcadeflex.v036.sound.ay8910H.AY8910interface;
import gr.codebb.arcadeflex.v037b7.sound.okim6295H.OKIM6295interface;

public class aztarac
{
	
	/* from machine/foodf.c */
	
	/* from vidhrdw/aztarac.c */
	
	/* from sndhrdw/aztarac.c */
	
	public static ReadHandlerPtr aztarac_joystick_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    return (((input_port_0_r.handler (offset) - 0xf) << 8) |
	            ((input_port_1_r.handler (offset) - 0xf) & 0xff));
	} };
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x00bfff, MRA_ROM ),
		new MemoryReadAddress( 0x022000, 0x022fff, foodf_nvram_r ),
		new MemoryReadAddress( 0x027000, 0x027001, aztarac_joystick_r ),
		new MemoryReadAddress( 0x027004, 0x027005, input_port_3_r ),
		new MemoryReadAddress( 0x027008, 0x027009, aztarac_sound_r ),
		new MemoryReadAddress( 0x02700c, 0x02700d, input_port_2_r ),
		new MemoryReadAddress( 0x02700e, 0x02700f, watchdog_reset_r ),
		new MemoryReadAddress( 0xff8000, 0xffafff, MRA_BANK1 ),
		new MemoryReadAddress( 0xffe000, 0xffffff, MRA_BANK2 ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x00bfff, MWA_ROM ),
		new MemoryWriteAddress( 0x022000, 0x022fff, foodf_nvram_w ),
		new MemoryWriteAddress( 0x027008, 0x027009, aztarac_sound_w ),
		new MemoryWriteAddress( 0xff8000, 0xffafff, MWA_BANK1, aztarac_vectorram ),
		new MemoryWriteAddress( 0xffb000, 0xffb001, aztarac_ubr_w ),
		new MemoryWriteAddress( 0xffe000, 0xffffff, MWA_BANK2 ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x1fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0x87ff, MRA_RAM ),
		new MemoryReadAddress( 0x8800, 0x8800, aztarac_snd_command_r ),
		new MemoryReadAddress( 0x8c00, 0x8c01, AY8910_read_port_0_r ),
		new MemoryReadAddress( 0x8c02, 0x8c03, AY8910_read_port_1_r ),
		new MemoryReadAddress( 0x8c04, 0x8c05, AY8910_read_port_2_r ),
		new MemoryReadAddress( 0x8c06, 0x8c07, AY8910_read_port_3_r ),
		new MemoryReadAddress( 0x9000, 0x9000, aztarac_snd_status_r ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x1fff, MWA_ROM ),
		new MemoryWriteAddress( 0x8000, 0x87ff, MWA_RAM ),
		new MemoryWriteAddress( 0x8c00, 0x8c00, AY8910_write_port_0_w ),
		new MemoryWriteAddress( 0x8c01, 0x8c01, AY8910_control_port_0_w ),
		new MemoryWriteAddress( 0x8c02, 0x8c02, AY8910_write_port_1_w ),
		new MemoryWriteAddress( 0x8c03, 0x8c03, AY8910_control_port_1_w ),
		new MemoryWriteAddress( 0x8c04, 0x8c04, AY8910_write_port_2_w ),
		new MemoryWriteAddress( 0x8c05, 0x8c05, AY8910_control_port_2_w ),
		new MemoryWriteAddress( 0x8c06, 0x8c06, AY8910_write_port_3_w ),
		new MemoryWriteAddress( 0x8c07, 0x8c07, AY8910_control_port_3_w ),
		new MemoryWriteAddress( 0x9000, 0x9000, aztarac_snd_status_w ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	
	static InputPortPtr input_ports_aztarac = new InputPortPtr(){ public void handler() { 
		PORT_START();  /* IN0 */
		PORT_ANALOG( 0x1f, 0xf, IPT_AD_STICK_X | IPF_CENTER, 100, 1, 0, 0x1e );
	
		PORT_START();  /* IN1 */
		PORT_ANALOG( 0x1f, 0xf, IPT_AD_STICK_Y | IPF_CENTER | IPF_REVERSE, 100, 1, 0, 0x1e );
	
		PORT_START();  /* IN2 */
		PORT_ANALOGX( 0xff, 0x00, IPT_DIAL | IPF_REVERSE, 25, 10, 0, 0, KEYCODE_Z, KEYCODE_X, 0, 0 );
	
		PORT_START();  /* IN3 */
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BITX(0x80, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode"), KEYCODE_F2, IP_JOY_NONE );
	INPUT_PORTS_END(); }}; 
	
	
	
	static AY8910interface ay8910_interface = new AY8910interface
	(
		4,	/* 4 chips */
		2000000,	/* 2 MHz */
		new int[] { 15, 15, 15, 15 },
		new ReadHandlerPtr[] { null, null, null, null },
		new ReadHandlerPtr[] { null, null, null, null },
		new WriteHandlerPtr[] { null, null, null, null },
		new WriteHandlerPtr[] { null, null, null, null }
	);
	
	static MachineDriver machine_driver_aztarac = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				8000000, /* 8 MHz */
				readmem, writemem,null,null,
	            aztarac_vg_interrupt, 1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				2000000,	/* 2 MHz */
				sound_readmem,sound_writemem, null, null,
				null,0,
	            aztarac_snd_timed_irq, 100
			)
		},
		40, 0,	/* frames per second, vblank duration (vector game, so no vblank) */
		1,
		null,
	
		/* video hardware */
		400, 300, new rectangle( 0, 1024-1, 0, 768-1 ),
		null,
		256, 256,
		aztarac_init_colors,
	
		VIDEO_TYPE_VECTOR | VIDEO_SUPPORTS_DIRTY,
		null,
		aztarac_vh_start,
		vector_vh_stop,
		vector_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_AY8910,
				ay8910_interface
			)
	    },
	
		foodf_nvram_handler
	);
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_aztarac = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0xc000, REGION_CPU1 );
		ROM_LOAD_EVEN( "l8_6.bin", 0x000000, 0x001000, 0x25f8da18 );
		ROM_LOAD_ODD ( "n8_0.bin", 0x000000, 0x001000, 0x04e20626 );
		ROM_LOAD_EVEN( "l7_7.bin", 0x002000, 0x001000, 0x230e244c );
		ROM_LOAD_ODD ( "n7_1.bin", 0x002000, 0x001000, 0x37b12697 );
		ROM_LOAD_EVEN( "l6_8.bin", 0x004000, 0x001000, 0x1293fb9d );
		ROM_LOAD_ODD ( "n6_2.bin", 0x004000, 0x001000, 0x712c206a );
		ROM_LOAD_EVEN( "l5_9.bin", 0x006000, 0x001000, 0x743a6501 );
		ROM_LOAD_ODD ( "n5_3.bin", 0x006000, 0x001000, 0xa65cbf99 );
		ROM_LOAD_EVEN( "l4_a.bin", 0x008000, 0x001000, 0x9cf1b0a1 );
		ROM_LOAD_ODD ( "n4_4.bin", 0x008000, 0x001000, 0x5f0080d5 );
		ROM_LOAD_EVEN( "l3_b.bin", 0x00a000, 0x001000, 0x8cc7f7fa );
		ROM_LOAD_ODD ( "n3_5.bin", 0x00a000, 0x001000, 0x40452376 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );
		ROM_LOAD( "j4_c.bin", 0x0000, 0x1000, 0xe897dfcd );
		ROM_LOAD( "j3_d.bin", 0x1000, 0x1000, 0x4016de77 );
	ROM_END(); }}; 
	
	
	
	public static InitDriverPtr init_aztarac = new InitDriverPtr() { public void handler() 
	{
		UBytePtr rom = memory_region(REGION_CPU1);
	
		/* patch IRQ vector 4 to autovector location */
		rom.WRITE_WORD(0x70, 0);
		rom.WRITE_WORD(0x72, 0x0c02);
	} };
	
	
	
	public static GameDriver driver_aztarac	   = new GameDriver("1983"	,"aztarac"	,"aztarac.java"	,rom_aztarac,null	,machine_driver_aztarac	,input_ports_aztarac	,init_aztarac	,ROT0	,	"Centuri", "Aztarac" );
}
