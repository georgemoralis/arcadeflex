/***************************************************************************

Rollergames (GX999) (c) 1991 Konami

driver by Nicola Salmoria

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers.WIP;
import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.commonH.*;
import static mame.inputport.*;
import static mame.drawgfxH.*;
import static arcadeflex.ptrlib.*;
import static vidhrdw.generic.*;
import static mame.cpuintrf.*;
import static mame.cpuintrfH.*;
import static mame.inputportH.*;
import static mame.mame.*;
import static arcadeflex.libc_old.*;
import static arcadeflex.libc.*;
import static mame.sndintrf.soundlatch_r;
import static mame.sndintrf.soundlatch_w;
import static cpu.konami.konamiH.*;
import static cpu.konami.konami.*;
import static cpu.z80.z80H.*;
import static mame.common.*;
import static mame.commonH.*;
import static mame.palette.*;
import static mame.memory.*;
import mame.sndintrfH.MachineSound;
import static mame.sndintrfH.SOUND_YM3812;
import static mame.timer.*;
import static mame.timerH.*;
import static vidhrdw.rollerg.*;
import static vidhrdw.konamiic.*;
import static sound._3812intf.*;
import static sound._3812intfH.*;

public class rollerg
{

	
	static int readzoomroms;
	public static konami_cpu_setlines_callbackPtr rollerg_banking = new konami_cpu_setlines_callbackPtr() { public void handler(int lines)
        {
		UBytePtr RAM = memory_region(REGION_CPU1);
		int offs = 0;
	
	
		offs = 0x10000 + ((lines & 0x07) * 0x4000);
		if (offs >= 0x28000) offs -= 0x20000;
		cpu_setbank(1,new UBytePtr(RAM,offs));
	}};
	
	public static InitMachinePtr rollerg_init_machine = new InitMachinePtr() { public void handler() 
	{
		konami_cpu_setlines_callback = rollerg_banking;
	
		readzoomroms = 0;
	} };
        
	public static WriteHandlerPtr rollerg_0010_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
                if (errorlog!=null) fprintf(errorlog,"%04x: write %02x to 0010\n",cpu_get_pc(),data);
	
		/* bits 0/1 are coin counters */
		coin_counter_w.handler(0,data & 0x01);
		coin_counter_w.handler(1,data & 0x02);
	
		/* bit 2 enables 051316 ROM reading */
		readzoomroms = data & 0x04;
	
		/* bit 5 enables 051316 wraparound */
		K051316_wraparound_enable(0, data & 0x20);
	
		/* other bits unknown */
	} };
	
	public static ReadHandlerPtr rollerg_K051316_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		if (readzoomroms!=0) return K051316_rom_0_r.handler(offset);
		else return K051316_0_r.handler(offset);
	} };
	
	public static ReadHandlerPtr rollerg_sound_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		/* If the sound CPU is running, read the status, otherwise
		   just make it pass the test */
/*TODO*///		if (Machine.sample_rate != 0) 	return K053260_ReadReg(2 + offset);
/*TODO*///		else 
            return 0x00;
	} };
	
	public static WriteHandlerPtr soundirq_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		cpu_cause_interrupt(1,0xff);
	} };
	public static timer_callback nmi_callback = new timer_callback(){ public void handler(int trigger)
        {
		cpu_set_nmi_line(1,ASSERT_LINE);
	}};
	
	public static WriteHandlerPtr sound_arm_nmi = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		cpu_set_nmi_line(1,CLEAR_LINE);
		timer_set(TIME_IN_USEC(50),0,nmi_callback);	/* kludge until the K053260 is emulated correctly */
	} };
	
	public static ReadHandlerPtr pip = new ReadHandlerPtr() { public int handler(int offset)
	{
		return 0x7f;
	} };
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0020, 0x0020, watchdog_reset_r ),
		new MemoryReadAddress( 0x0030, 0x0031, rollerg_sound_r ),	/* K053260 */
		new MemoryReadAddress( 0x0050, 0x0050, input_port_0_r ),
		new MemoryReadAddress( 0x0051, 0x0051, input_port_1_r ),
		new MemoryReadAddress( 0x0052, 0x0052, input_port_4_r ),
		new MemoryReadAddress( 0x0053, 0x0053, input_port_2_r ),
		new MemoryReadAddress( 0x0060, 0x0060, input_port_3_r ),
		new MemoryReadAddress( 0x0061, 0x0061, pip ),				/* ????? */
		new MemoryReadAddress( 0x0300, 0x030f, K053244_r ),
		new MemoryReadAddress( 0x0800, 0x0fff, rollerg_K051316_r ),
		new MemoryReadAddress( 0x1000, 0x17ff, K053245_r ),
		new MemoryReadAddress( 0x1800, 0x1fff, paletteram_r ),
		new MemoryReadAddress( 0x2000, 0x3aff, MRA_RAM ),
		new MemoryReadAddress( 0x4000, 0x7fff, MRA_BANK1 ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0010, 0x0010, rollerg_0010_w ),
		new MemoryWriteAddress( 0x0020, 0x0020, watchdog_reset_w ),
/*TODO*///		new MemoryWriteAddress( 0x0030, 0x0031, K053260_WriteReg ),
		new MemoryWriteAddress( 0x0040, 0x0040, soundirq_w ),
		new MemoryWriteAddress( 0x0200, 0x020f, K051316_ctrl_0_w ),
		new MemoryWriteAddress( 0x0300, 0x030f, K053244_w ),
		new MemoryWriteAddress( 0x0800, 0x0fff, K051316_0_w ),
		new MemoryWriteAddress( 0x1000, 0x17ff, K053245_w ),
		new MemoryWriteAddress( 0x1800, 0x1fff, paletteram_xBBBBBGGGGGRRRRR_swap_w, paletteram ),
		new MemoryWriteAddress( 0x2000, 0x3aff, MWA_RAM ),
		new MemoryWriteAddress( 0x4000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress readmem_sound[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0x87ff, MRA_RAM ),
/*TODO*///		new MemoryReadAddress( 0xa000, 0xa02f, K053260_ReadReg ),
		new MemoryReadAddress( 0xc000, 0xc000, YM3812_status_port_0_r ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem_sound[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0x8000, 0x87ff, MWA_RAM ),
/*TODO*///		new MemoryWriteAddress( 0xa000, 0xa02f, K053260_WriteReg ),
		new MemoryWriteAddress( 0xc000, 0xc000, YM3812_control_port_0_w ),
		new MemoryWriteAddress( 0xc001, 0xc001, YM3812_write_port_0_w ),
		new MemoryWriteAddress( 0xfc00, 0xfc00, sound_arm_nmi ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	/***************************************************************************
	
		Input Ports
	
	***************************************************************************/
	
	static InputPortPtr input_ports_rollerg = new InputPortPtr(){ public void handler() { 
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
	
		PORT_START(); 
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") );
	//	PORT_DIPSETTING(    0x00, "Disabled" );
	
		PORT_START(); 
		PORT_DIPNAME( 0x03, 0x02, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "1" );
		PORT_DIPSETTING(    0x02, "2" );
		PORT_DIPSETTING(    0x01, "3" );
		PORT_DIPSETTING(    0x00, "5" );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x18, 0x10, "Bonus Energy" );
		PORT_DIPSETTING(    0x00, "1/2 for Stage Winner" );
		PORT_DIPSETTING(    0x08, "1/4 for Stage Winner" );
		PORT_DIPSETTING(    0x10, "1/4 for Cycle Winner" );
		PORT_DIPSETTING(    0x18, "None" );
		PORT_DIPNAME( 0x60, 0x40, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x60, "Easy" );
		PORT_DIPSETTING(    0x40, "Normal" );
		PORT_DIPSETTING(    0x20, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN3 );
	INPUT_PORTS_END(); }}; 
	
	
	
	/***************************************************************************
	
		Machine Driver
	
	***************************************************************************/
	
	static YM3812interface ym3812_interface = new YM3812interface
	(
		1,
		3579545,
		new int[]{ 80 },
		new WriteYmHandlerPtr[]{null }
        );
	
/*TODO*///	static struct K053260_interface k053260_interface =
/*TODO*///	{
/*TODO*///		3579545,
/*TODO*///		REGION_SOUND1, /* memory region */
/*TODO*///		{ MIXER(100,MIXER_PAN_LEFT), MIXER(100,MIXER_PAN_RIGHT) },
/*TODO*///		0
/*TODO*///	};
	
	static MachineDriver machine_driver_rollerg = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_KONAMI,
				3000000,		/* ? */
				readmem,writemem,null,null,
				interrupt,1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				3579545,
				readmem_sound, writemem_sound,null,null,
				ignore_interrupt,0	/* IRQs are triggered by the main CPU */
									/* NMIs are generated by the 053260 */
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		rollerg_init_machine,
	
		/* video hardware */
		64*8, 32*8, new rectangle( 14*8, (64-14)*8-1, 2*8, 30*8-1 ),
		null,	/* gfx decoded by konamiic.c */
		1024, 1024,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		rollerg_vh_start,
		rollerg_vh_stop,
		rollerg_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM3812,
				ym3812_interface
			)/*,
			new MachineSound(
				SOUND_K053260,
				k053260_interface
			)*/
		}
	);
	
	
	
	/***************************************************************************
	
	  Game ROMs
	
	***************************************************************************/
	
	static RomLoadPtr rom_rollerg = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x28000, REGION_CPU1 );/* code + banked roms */
		ROM_LOAD( "999m02.g7",  0x10000, 0x18000, 0x3df8db93 );
		ROM_CONTINUE(           0x08000, 0x08000 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the sound CPU */
		ROM_LOAD( "999m01.e11", 0x0000, 0x8000, 0x1fcfb22f );
	
		ROM_REGION( 0x200000, REGION_GFX1 );/* graphics ( don't dispose as the program can read them ) */
		ROM_LOAD( "999h06.k2",  0x000000, 0x100000, 0xeda05130 );/* sprites */
		ROM_LOAD( "999h05.k8",  0x100000, 0x100000, 0x5f321c7d );
	
		ROM_REGION( 0x080000, REGION_GFX2 );/* graphics ( don't dispose as the program can read them ) */
		ROM_LOAD( "999h03.d23", 0x000000, 0x040000, 0xea1edbd2 );/* zoom */
		ROM_LOAD( "999h04.f23", 0x040000, 0x040000, 0xc1a35355 );
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* samples for 053260 */
		ROM_LOAD( "999h09.c5",  0x000000, 0x080000, 0xc5188783 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_rollergj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x28000, REGION_CPU1 );/* code + banked roms */
		ROM_LOAD( "999v02.bin", 0x10000, 0x18000, 0x0dd8c3ac );
		ROM_CONTINUE(           0x08000, 0x08000 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the sound CPU */
		ROM_LOAD( "999m01.e11", 0x0000, 0x8000, 0x1fcfb22f );
	
		ROM_REGION( 0x200000, REGION_GFX1 );/* graphics ( don't dispose as the program can read them ) */
		ROM_LOAD( "999h06.k2",  0x000000, 0x100000, 0xeda05130 );/* sprites */
		ROM_LOAD( "999h05.k8",  0x100000, 0x100000, 0x5f321c7d );
	
		ROM_REGION( 0x080000, REGION_GFX2 );/* graphics ( don't dispose as the program can read them ) */
		ROM_LOAD( "999h03.d23", 0x000000, 0x040000, 0xea1edbd2 );/* zoom */
		ROM_LOAD( "999h04.f23", 0x040000, 0x040000, 0xc1a35355 );
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* samples for 053260 */
		ROM_LOAD( "999h09.c5",  0x000000, 0x080000, 0xc5188783 );
	ROM_END(); }}; 
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	
	
	public static InitDriverPtr init_rollerg = new InitDriverPtr() { public void handler() 
	{
		konami_rom_deinterleave_2(REGION_GFX1);
	} };
	
	
	
	public static GameDriver driver_rollerg	   = new GameDriver("1991"	,"rollerg"	,"rollerg.java"	,rom_rollerg,null	,machine_driver_rollerg	,input_ports_rollerg	,init_rollerg	,ROT0	,	"Konami", "Rollergames (US)" );
	public static GameDriver driver_rollergj	   = new GameDriver("1991"	,"rollergj"	,"rollerg.java"	,rom_rollergj,driver_rollerg	,machine_driver_rollerg	,input_ports_rollerg	,init_rollerg	,ROT0	,	"Konami", "Rollergames (Japan)" );
}
