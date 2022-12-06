/***************************************************************************

MAGMAX
(c)1985 NihonBussan Co.,Ltd.

Driver by Takahiro Nogi (nogi@kt.rim.or.jp) 1999/11/05 -
Additional tweaking by Jarek Burczynski

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.drivers;

import arcadeflex.v036.generic.funcPtr.TimerCallbackHandlerPtr;
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static gr.codebb.arcadeflex.v036.cpu.m68000.m68000H.MC68000_IRQ_1;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.timer.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.magmax.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b7.sound.ay8910.*;
import static gr.codebb.arcadeflex.v037b7.sound.ay8910H.*;
import static arcadeflex.v036.vidhrdw.generic.*;

public class magmax
{
	
	static timer_entry scanline_timer;
	
	static int sound_latch = 0;
	static int LS74_clr = 0;
	static int LS74_q   = 0;
	
	public static WriteHandlerPtr magmax_sound_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		sound_latch = data << 1;
		cpu_set_irq_line(1, 0, ASSERT_LINE);
	} };
	
	public static ReadHandlerPtr magmax_sound_irq_ack  = new ReadHandlerPtr() { public int handler(int offset)
	{
		cpu_set_irq_line(1, 0, CLEAR_LINE);
		return 0;
	} };
	
	public static ReadHandlerPtr magmax_sound_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return (sound_latch | LS74_q);
	} };
	
	public static WriteHandlerPtr ay8910_portB_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/*bit 0 is input to CLR line of the LS74*/
		LS74_clr = data & 1;
		if (LS74_clr == 0)
			LS74_q = 0;
	} };
	static TimerCallbackHandlerPtr scanline_callback = new TimerCallbackHandlerPtr() {
            @Override
            public void handler(int scanline) {
                /* bit 0 goes hi whenever line V6 from video part goes lo.hi */
		/* that is when scanline is 64 and 192 accordingly */
		if (LS74_clr != 0)
			LS74_q = 1;
	
		scanline += 128;
		scanline &= 255;
	
		scanline_timer = timer_set( cpu_getscanlinetime( scanline ), scanline, scanline_callback );
            }
        };
        
	public static InitMachinePtr init_machine = new InitMachinePtr() { public void handler() 
	{
            System.out.println("init_machine");
		scanline_timer = timer_set(cpu_getscanlinetime( 64 ), 64, scanline_callback );
	} };
	
	
	
	public static WriteHandlerPtr ay8910_portA_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	/*There are three AY8910 chips and four(!) separate amplifiers on the board
	* Each of AY channels is hardware mapped in following order:
	* amplifier 0 <- AY0 CHA
	* amplifier 1 <- AY0 CHB + AY0 CHC + AY1 CHA + AY1 CHB
	* amplifier 2 <- AY1 CHC + AY2 CHA
	* amplifier 3 <- AY2 CHB + AY2 CHC
	*
	* Each of the amps has its own analog cuircit:
	* amp0, amp1 and amp2 are different from each other amp3 is the same as amp2
	*
	* Outputs of those amps are inputs to post amps, each having own cuircit
	* that is partially controlled by AY #0 port A.
	* PORT A BIT 0 - control postamp 0
	* PORT A BIT 1 - control postamp 1
	* PORT A BIT 2 - control postamp 2
	* PORT A BIT 3 - control postamp 3
	*
	* The "control" means assert/clear input pins on chip called 4066 (it is analog switch)
	* This is NOT implemented here.
	*/
	
	
			//missing implementation
	
	} };
	
	
	
	public static WriteHandlerPtr magmax_write = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		switch (offset)
		{
			case 0x00: /* VRAM CONTROL REGISTER */
				// bit0 - coin counter 1
				// bit1 - coin counter 2
				// bit2 - flip screen (INV)
				// bit3 - page bank to be displayed (PG)
				// bit4 - sprite bank LSB (DP0)
				// bit5 - sprite bank MSB (DP1)
				// bit6 - BG display enable (BE)
				magmax_vreg = COMBINE_WORD(magmax_vreg, data);
				break;
	
			case 0x02: /* SCROLL X */
				COMBINE_WORD_MEM(new UBytePtr(magmax_scroll_x), 0, data);
				break;
	
			case 0x04: /* SCROLL Y */
				COMBINE_WORD_MEM(new UBytePtr(magmax_scroll_y), 0, data);
				break;
	
			case 0x0c: /* SOUND COMMAND */
				magmax_sound_w.handler(0, data);
				break;
	
			case 0x0e: /* irq acknowledge */
				break;
	
			default:
/*TODO*///				logerror("offs:0x%02X data:0x%08X\n", offset, data);
				break;
		}
	} };
	
	public static ReadHandlerPtr magmax_read  = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch (offset)
		{
			case 0: /* PLAYER CONTROL 1 */
				return readinputport(0);
	
			case 2: /* PLAYER CONTROL 2 */
				return readinputport(1);
	
			case 4: /* START BUTTONS, COINS, TEST */
				return readinputport(2);
	
			case 6: /* DIPSWITCH A and B */
				return ((readinputport(4) << 8) | readinputport(3));
		}
	
		return 0xffff;
	} };
	
	
	static MemoryReadAddress magmax_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x013fff, MRA_ROM ),
		new MemoryReadAddress( 0x018000, 0x018fff, MRA_BANK1 ),
		new MemoryReadAddress( 0x020000, 0x0207ff, MRA_BANK2 ),
		new MemoryReadAddress( 0x028000, 0x0281ff, MRA_BANK3 ),
		new MemoryReadAddress( 0x030000, 0x03000f, magmax_read ),
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress magmax_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x013fff, MWA_ROM ),
		new MemoryWriteAddress( 0x018000, 0x018fff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x020000, 0x0207ff, MWA_BANK2, videoram, videoram_size ),
		new MemoryWriteAddress( 0x028000, 0x0281ff, MWA_BANK3, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x030010, 0x03001f, magmax_write ),
		new MemoryWriteAddress( -1 )
	};
	
	static MemoryReadAddress magmax_soundreadmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new MemoryReadAddress( 0x4000, 0x4000, magmax_sound_irq_ack ),
		new MemoryReadAddress( 0x6000, 0x67ff, MRA_RAM ),
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress magmax_soundwritemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x3fff, MWA_ROM ),
		new MemoryWriteAddress( 0x6000, 0x67ff, MWA_RAM ),
		new MemoryWriteAddress( -1 )
	};
	
	static IOReadPort magmax_soundreadport[] =
	{
		new IOReadPort( 0x06, 0x06, magmax_sound_r ),
		new IOReadPort( -1 )
	};
	
	static IOWritePort magmax_soundwriteport[] =
	{
		new IOWritePort( 0x00, 0x00, AY8910_control_port_0_w ),
		new IOWritePort( 0x01, 0x01, AY8910_write_port_0_w ),
		new IOWritePort( 0x02, 0x02, AY8910_control_port_1_w ),
		new IOWritePort( 0x03, 0x03, AY8910_write_port_1_w ),
		new IOWritePort( 0x04, 0x04, AY8910_control_port_2_w ),
		new IOWritePort( 0x05, 0x05, AY8910_write_port_2_w ),
		new IOWritePort( -1 )
	};
	
	
	static InputPortPtr input_ports_magmax = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* Player 1 controls */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* Player 2 controls */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL  );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* Coin, Start, Test, Dipswitch */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_SERVICE( 0x20, IP_ACTIVE_LOW );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* Dipswitch 1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "3" );
		PORT_DIPSETTING(    0x02, "4" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_DIPSETTING(    0x00, "6" );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x0c, "30000 every" );
		PORT_DIPSETTING(    0x04, "70000 every" );
		PORT_DIPSETTING(    0x08, "50000 every" );
		PORT_DIPSETTING(    0x00, "90000 every" );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* Dipswitch 2 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x10, "Easy" );
		PORT_DIPSETTING(    0x00, "Hard" );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown"));
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8, 8,	/* 8*8 characters */
		256,	/* 256 characters */
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },
		new int[] { 4, 0, 12, 8, 20, 16, 28, 24 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16, 16,	/* 16*16 characters */
		512,	/* 512 characters */
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },
		new int[] { 4, 0, 4+512*64*8, 0+512*64*8, 12, 8, 12+512*64*8, 8+512*64*8,
		  20, 16, 20+512*64*8, 16+512*64*8, 28, 24, 28+512*64*8, 24+512*64*8 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32,
			8*32, 9*32, 10*32, 11*32, 12*32, 13*32, 14*32, 15*32 },
		64*8
	);
	
	static GfxLayout tilelayout = new GfxLayout
	(
		8, 8,	/* 8*8 characters */
		1024,	/* 1024 characters */
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },
		new int[] { 4, 0, 12, 8, 20, 16, 28, 24 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,           0,  1 ), /*no color codes*/
		new GfxDecodeInfo( REGION_GFX2, 0, spritelayout,      1*16, 16 ), /*16 color codes*/
		new GfxDecodeInfo( REGION_GFX3, 0, tilelayout,  1*16+16*16,  1 ), /*not used by emulation, here only to be able to view the graphics with F4*/
		new GfxDecodeInfo( -1 )
	};
	
	
	public static WriteHandlerPtr ay8910_data_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	// ?
	} };
	
	static AY8910interface ay8910_interface = new AY8910interface
	(
		3,			/* 3 chips */
		10000000/8,		/* 1.25 MHz */
		new int[] { 35, 35, 35 },
		new ReadHandlerPtr[] { null, null, null }, //read port A
		new ReadHandlerPtr[] { null, null, null }, //read port B
		new WriteHandlerPtr[] { ay8910_portA_0_w, null, null }, //ay8910_data_w, ay8910_data_w, ay8910_data_w ), //write port A
		new WriteHandlerPtr[] { ay8910_portB_0_w, null, null } //ay8910_data_w, ay8910_data_w, ay8910_data_w } //write port B
	);
	
	
	public static InterruptPtr magmax_interrupt = new InterruptPtr() { public int handler() 
	{
		return MC68000_IRQ_1;
	} };
	
	
	static MachineDriver machine_driver_magmax = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				8000000,	/* 8 Mhz */
				magmax_readmem, magmax_writemem, null, null,
				magmax_interrupt, 1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				10000000/4,	/* 2.5 Mhz */
				magmax_soundreadmem, magmax_soundwritemem, magmax_soundreadport, magmax_soundwriteport,
				ignore_interrupt, 1
			),
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,
		1,
		init_machine,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		256, 1*16 + 16*16 + 1*16,
		magmax_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		magmax_vh_start,
		magmax_vh_stop,
		magmax_vh_screenrefresh,
	
		/* sound hardware */
		0, 0, 0, 0,
		new MachineSound[] {
			new MachineSound(
				SOUND_AY8910,
				ay8910_interface
			)
		}
	);
	
	
	static RomLoadPtr rom_magmax = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x14000, REGION_CPU1 );/* main cpu code */
		ROM_LOAD_ODD ( "1.3b", 0x00000, 0x4000, 0x33793cbb );
		ROM_LOAD_EVEN( "6.3d", 0x00000, 0x4000, 0x677ef450 );
		ROM_LOAD_ODD ( "2.5b", 0x08000, 0x4000, 0x1a0c84df );
		ROM_LOAD_EVEN( "7.5d", 0x08000, 0x4000, 0x01c35e95 );
		ROM_LOAD_ODD ( "3.6b", 0x10000, 0x2000, 0xd06e6cae );
		ROM_LOAD_EVEN( "8.6d", 0x10000, 0x2000, 0x790a82be );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* sound cpu code */
		ROM_LOAD( "15.17b", 0x00000, 0x2000, 0x19e7b983 );
		ROM_LOAD( "16.18b", 0x02000, 0x2000, 0x055e3126 );
	
		ROM_REGION( 0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* chars */
		ROM_LOAD( "23.15g", 0x00000, 0x2000, 0xa7471da2 );
	
		ROM_REGION( 0x10000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* sprites */
		ROM_LOAD( "17.3e",  0x00000, 0x2000, 0x8e305b2e );
		ROM_LOAD( "18.5e",  0x02000, 0x2000, 0x14c55a60 );
		ROM_LOAD( "19.6e",  0x04000, 0x2000, 0xfa4141d8 );
		ROM_LOAD( "20.3g",  0x08000, 0x2000, 0x6fa3918b );
		ROM_LOAD( "21.5g",  0x0a000, 0x2000, 0xdd52eda4 );
		ROM_LOAD( "22.6g",  0x0c000, 0x2000, 0x4afc98ff );
	
		ROM_REGION( 0x08000, REGION_GFX3 );/* background tiles (data used at runtime so no REGIONFLAG_DISPOSE)*/
		ROM_LOAD( "11.15f", 0x00000, 0x2000, 0x91f3edb6 );/* surface */
		ROM_LOAD( "12.17f", 0x02000, 0x2000, 0x99771eff );/* underground */
		ROM_LOAD( "13.18f", 0x04000, 0x2000, 0x75f30159 );/* surface of mechanical level */
		ROM_LOAD( "14.20f", 0x06000, 0x2000, 0x96babcba );/* underground of mechanical level */
	
		ROM_REGION( 0x4000, REGION_USER1 );/* surface scroll control */
		ROM_LOAD( "4.18b",  0x00000, 0x2000, 0x1550942e );
		ROM_LOAD( "5.20b",  0x02000, 0x2000, 0x3b93017f );
	
		ROM_REGION( 0x4000, REGION_USER2 );/* BG control data */
		ROM_LOAD( "9.18d",  0x00000, 0x2000, 0x9ecc9ab8 );/* surface */
		ROM_LOAD( "10.20d", 0x02000, 0x2000, 0xe2ff7293 );/* underground */
	
		ROM_REGION( 0x0200, REGION_USER3 );/* BG control data */
		ROM_LOAD( "mag_b.14d",  0x0000, 0x0100, 0xa0fb7297 );/* background control PROM */
		ROM_LOAD( "mag_c.15d",  0x0100, 0x0100, 0xd84a6f78 );/* background control PROM */
	
		ROM_REGION( 0x0500, REGION_PROMS );/* color PROMs */
		ROM_LOAD( "mag_e.10f",  0x0000, 0x0100, 0x75e4f06a );/* red */
		ROM_LOAD( "mag_d.10e",  0x0100, 0x0100, 0x34b6a6e3 );/* green */
		ROM_LOAD( "mag_a.10d",  0x0200, 0x0100, 0xa7ea7718 );/* blue */
		ROM_LOAD( "mag_g.2e",   0x0300, 0x0100, 0x830be358 );/* sprites color lookup table */
		ROM_LOAD( "mag_f.13b",  0x0400, 0x0100, 0x4a6f9a6d );/* state machine data used for video signals generation (not used in emulation)*/
	ROM_END(); }}; 
	
	
	public static GameDriver driver_magmax	   = new GameDriver("1985"	,"magmax"	,"magmax.java"	,rom_magmax,null	,machine_driver_magmax	,input_ports_magmax	,null	,ROT0	,	"Nichibutsu", "Mag Max" );
}
