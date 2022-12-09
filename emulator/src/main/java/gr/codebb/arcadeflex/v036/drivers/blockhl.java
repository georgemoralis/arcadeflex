/***************************************************************************

Block Hole (GX973) (c) 1989 Konami

driver by Nicola Salmoria

Notes:
Quarth works, but Block Hole crashes when it reaches the title screen. An
interrupt happens, and after rti the ROM bank is not the same as before so
it jumps to garbage code.
If you want to see this happen, place a breakpoint at 0x8612, and trace
after that.
The code is almost identical in the two versions, it looks like Quarth is
working just because luckily the interrupt doesn't happen at that point.
It seems that the interrupt handler trashes the selected ROM bank and forces
it to null. To prevent crashes, I only generate interrupts when the ROM bank is
already null. There might be another interrupt enable register, but I haven't
found it.

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package gr.codebb.arcadeflex.v036.drivers;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.inptportH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_r;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_w;
import static gr.codebb.arcadeflex.v036.cpu.konami.konamiH.*;
import static gr.codebb.arcadeflex.v036.cpu.konami.konami.*;
import static arcadeflex.v036.cpu.z80.z80H.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.memory.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.blockhl.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konamiic.*;

public class blockhl
{
        public static konami_cpu_setlines_callbackPtr blockhl_banking = new konami_cpu_setlines_callbackPtr() { public void handler(int lines)
        {
                UBytePtr RAM = memory_region(REGION_CPU1);
		int offs;
	
		/* bits 0-1 = ROM bank */
		rombank = lines & 0x03;
		offs = 0x10000 + (lines & 0x03) * 0x2000;
		cpu_setbank(1,new UBytePtr(RAM,offs));
	
		/* bits 3/4 = coin counters */
		coin_counter_w.handler(0,lines & 0x08);
		coin_counter_w.handler(1,lines & 0x10);
	
		/* bit 5 = select palette RAM or work RAM at 5800-5fff */
		palette_selected = ~lines & 0x20;
	
		/* bit 6 = enable char ROM reading through the video RAM */
		K052109_set_RMRD_line( ( lines & 0x40 )!=0 ? ASSERT_LINE : CLEAR_LINE );
	
		/* bit 7 used but unknown */
	
		/* other bits unknown */
	
                if (errorlog!=null && (lines & 0x84) != 0x80) fprintf(errorlog,"%04x: setlines %02x\n",cpu_get_pc(),lines);
	}};
	
	public static InitMachineHandlerPtr blockhl_init_machine = new InitMachineHandlerPtr() { public void handler() 
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
	
		konami_cpu_setlines_callback = blockhl_banking;
	
		paletteram = new UBytePtr(RAM,0x18000);
	} };

	static int palette_selected;
	static UBytePtr ram=new UBytePtr();
	static int rombank;
	
	public static InterruptHandlerPtr blockhl_interrupt = new InterruptHandlerPtr() { public int handler() 
	{
		if (K052109_is_IRQ_enabled()!=0 && rombank == 0)	/* kludge to prevent crashes */
			return KONAMI_INT_IRQ;
		else
			return ignore_interrupt.handler();
	} };
	
	public static ReadHandlerPtr bankedram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		if (palette_selected!=0)
			return paletteram_r.handler(offset);
		else
			return ram.read(offset);
	} };
	
	public static WriteHandlerPtr bankedram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (palette_selected!=0)
			paletteram_xBBBBBGGGGGRRRRR_swap_w.handler(offset,data);
		else
			ram.write(offset,data);
	} };
	
	public static WriteHandlerPtr blockhl_sh_irqtrigger_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		cpu_cause_interrupt(1,0xff);
	} };
	
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x1f94, 0x1f94, input_port_4_r ),
		new MemoryReadAddress( 0x1f95, 0x1f95, input_port_0_r ),
		new MemoryReadAddress( 0x1f96, 0x1f96, input_port_1_r ),
		new MemoryReadAddress( 0x1f97, 0x1f97, input_port_2_r ),
		new MemoryReadAddress( 0x1f98, 0x1f98, input_port_3_r ),
		new MemoryReadAddress( 0x0000, 0x3fff, K052109_051960_r ),
		new MemoryReadAddress( 0x4000, 0x57ff, MRA_RAM ),
		new MemoryReadAddress( 0x5800, 0x5fff, bankedram_r ),
		new MemoryReadAddress( 0x6000, 0x7fff, MRA_BANK1 ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x1f84, 0x1f84, soundlatch_w ),
		new MemoryWriteAddress( 0x1f88, 0x1f88, blockhl_sh_irqtrigger_w ),
		new MemoryWriteAddress( 0x1f8c, 0x1f8c, watchdog_reset_w ),
		new MemoryWriteAddress( 0x0000, 0x3fff, K052109_051960_w ),
		new MemoryWriteAddress( 0x4000, 0x57ff, MWA_RAM ),
		new MemoryWriteAddress( 0x5800, 0x5fff, bankedram_w, ram ),
		new MemoryWriteAddress( 0x6000, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0x8000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0x87ff, MRA_RAM ),
		new MemoryReadAddress( 0xa000, 0xa000, soundlatch_r ),
/*TODO*///		new MemoryReadAddress( 0xc001, 0xc001, YM2151_status_port_0_r ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0x8000, 0x87ff, MWA_RAM ),
/*TODO*///		new MemoryWriteAddress( 0xc000, 0xc000, YM2151_register_port_0_w ),
/*TODO*///		new MemoryWriteAddress( 0xc001, 0xc001, YM2151_data_port_0_w ),
		new MemoryWriteAddress( 0xe00c, 0xe00d, MWA_NOP ),		/* leftover from missing 007232? */
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	/***************************************************************************
	
		Input Ports
	
	***************************************************************************/
	
	static InputPortHandlerPtr input_ports_blockhl = new InputPortHandlerPtr(){ public void handler() { 
		PORT_START(); 	/* PLAYER 1 INPUTS */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START(); 	/* PLAYER 2 INPUTS */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
	
		PORT_START(); 	/* DSW #1 */
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
	//	PORT_DIPSETTING(    0x00, "Invalid" );
	
		PORT_START(); 	/* DSW #2 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x01, "1" );
		PORT_DIPSETTING(    0x00, "2" );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x60, 0x40, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x60, "Easy" );
		PORT_DIPSETTING(    0x40, "Normal" );
		PORT_DIPSETTING(    0x20, "Difficult" );
		PORT_DIPSETTING(    0x00, "Very Difficult" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* DSW #3 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x40, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	
	/***************************************************************************
	
		Machine Driver
	
	***************************************************************************/
	
/*TODO*///	static struct YM2151interface ym2151_interface =
/*TODO*///	{
/*TODO*///		1, /* 1 chip */
/*TODO*///		3579545, /* 3.579545 MHz */
/*TODO*///		{ YM3012_VOL(60,MIXER_PAN_LEFT,60,MIXER_PAN_RIGHT) },
/*TODO*///		{ 0 },
/*TODO*///		{ 0 }
/*TODO*///	};
	
	static MachineDriver machine_driver_blockhl = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_KONAMI,		/* Konami custom 052526 */
				3000000,		/* ? */
				readmem,writemem,null,null,
	            blockhl_interrupt,1
	        ),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				3579545,		/* ? */
				sound_readmem,sound_writemem,null,null,
				ignore_interrupt,0	/* interrupts are triggered by the main CPU */
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		blockhl_init_machine,
	
		/* video hardware */
		64*8, 32*8, new rectangle( 14*8, (64-14)*8-1, 2*8, 30*8-1 ),
		null,	/* gfx decoded by konamiic.c */
		1024, 1024,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		blockhl_vh_start,
		blockhl_vh_stop,
		blockhl_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		/*new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			)
		}*/
                null
	);
	
	
	/***************************************************************************
	
	  Game ROMs
	
	***************************************************************************/
	
	static RomLoadHandlerPtr rom_blockhl = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x18800, REGION_CPU1 );/* code + banked roms + space for banked RAM */
		ROM_LOAD( "973l02.e21", 0x10000, 0x08000, 0xe14f849a );
		ROM_CONTINUE(           0x08000, 0x08000 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the sound CPU */
		ROM_LOAD( "973d01.g6",  0x0000, 0x8000, 0xeeee9d92 );
	
		ROM_REGION( 0x20000, REGION_GFX1 );/* graphics (addressable by the main CPU) */
		ROM_LOAD_GFX_EVEN( "973f07.k15", 0x00000, 0x08000, 0x1a8cd9b4 );/* tiles */
		ROM_LOAD_GFX_ODD ( "973f08.k18", 0x00000, 0x08000, 0x952b51a6 );
		ROM_LOAD_GFX_EVEN( "973f09.k20", 0x10000, 0x08000, 0x77841594 );
		ROM_LOAD_GFX_ODD ( "973f10.k23", 0x10000, 0x08000, 0x09039fab );
	
		ROM_REGION( 0x20000, REGION_GFX2 );/* graphics (addressable by the main CPU) */
		ROM_LOAD_GFX_EVEN( "973f06.k12", 0x00000, 0x08000, 0x51acfdb6 );/* sprites */
		ROM_LOAD_GFX_ODD ( "973f05.k9",  0x00000, 0x08000, 0x4cfea298 );
		ROM_LOAD_GFX_EVEN( "973f04.k7",  0x10000, 0x08000, 0x69ca41bd );
		ROM_LOAD_GFX_ODD ( "973f03.k4",  0x10000, 0x08000, 0x21e98472 );
	
		ROM_REGION( 0x0100, REGION_PROMS );/* PROMs */
		ROM_LOAD( "973a11.h10", 0x0000, 0x0100, 0x46d28fe9 );/* priority encoder (not used) */
	ROM_END(); }}; 
	
	static RomLoadHandlerPtr rom_quarth = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x18800, REGION_CPU1 );/* code + banked roms + space for banked RAM */
		ROM_LOAD( "973j02.e21", 0x10000, 0x08000, 0x27a90118 );
		ROM_CONTINUE(           0x08000, 0x08000 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the sound CPU */
		ROM_LOAD( "973d01.g6",  0x0000, 0x8000, 0xeeee9d92 );
	
		ROM_REGION( 0x20000, REGION_GFX1 );/* graphics (addressable by the main CPU) */
		ROM_LOAD_GFX_EVEN( "973e07.k15", 0x00000, 0x08000, 0x0bd6b0f8 );/* tiles */
		ROM_LOAD_GFX_ODD ( "973e08.k18", 0x00000, 0x08000, 0x104d0d5f );
		ROM_LOAD_GFX_EVEN( "973e09.k20", 0x10000, 0x08000, 0xbd3a6f24 );
		ROM_LOAD_GFX_ODD ( "973e10.k23", 0x10000, 0x08000, 0xcf5e4b86 );
	
		ROM_REGION( 0x20000, REGION_GFX2 );/* graphics (addressable by the main CPU) */
		ROM_LOAD_GFX_EVEN( "973e06.k12", 0x00000, 0x08000, 0x0d58af85 );/* sprites */
		ROM_LOAD_GFX_ODD ( "973e05.k9",  0x00000, 0x08000, 0x15d822cb );
		ROM_LOAD_GFX_EVEN( "973e04.k7",  0x10000, 0x08000, 0xd70f4a2c );
		ROM_LOAD_GFX_ODD ( "973e03.k4",  0x10000, 0x08000, 0x2c5a4b4b );
	
		ROM_REGION( 0x0100, REGION_PROMS );/* PROMs */
		ROM_LOAD( "973a11.h10", 0x0000, 0x0100, 0x46d28fe9 );/* priority encoder (not used) */
	ROM_END(); }}; 
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	

	
	
	public static InitDriverHandlerPtr init_blockhl = new InitDriverHandlerPtr() { public void handler() 
	{
		konami_rom_deinterleave_2(REGION_GFX1);
		konami_rom_deinterleave_2(REGION_GFX2);
	} };
	
	
	
	public static GameDriver driver_blockhl	   = new GameDriver("1989"	,"blockhl"	,"blockhl.java"	,rom_blockhl,null	,machine_driver_blockhl	,input_ports_blockhl	,init_blockhl	,ROT0	,	"Konami", "Block Hole" );
	public static GameDriver driver_quarth	   = new GameDriver("1989"	,"quarth"	,"blockhl.java"	,rom_quarth,driver_blockhl	,machine_driver_blockhl	,input_ports_blockhl	,init_blockhl	,ROT0	,	"Konami", "Quarth (Japan)" );
}
