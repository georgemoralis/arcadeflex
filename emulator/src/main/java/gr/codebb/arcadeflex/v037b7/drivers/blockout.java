/***************************************************************************

Block Out

driver by Nicola Salmoria

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.drivers;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.inputH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v036.sound._2151intf.*;
import gr.codebb.arcadeflex.v036.sound._2151intfH.YM2151interface;
import static gr.codebb.arcadeflex.v036.sound._2151intfH.YM3012_VOL;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;
import static gr.codebb.arcadeflex.v037b7.cpu.z80.z80H.Z80_NMI_INT;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.blockout.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static arcadeflex.v036.mame.cpuintrfH.*;
import gr.codebb.arcadeflex.v037b7.sound.okim6295H.OKIM6295interface;

public class blockout
{
	
	
	public static InterruptPtr blockout_interrupt = new InterruptPtr() { public int handler() 
	{
		/* interrupt 6 is vblank */
		/* interrupt 5 reads coin inputs - might have to be triggered only */
		/* when a coin is inserted */
		return 6 - cpu_getiloops();
	} };
	
	public static ReadHandlerPtr blockout_input_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch (offset)
		{
			case 0:
				return input_port_0_r.handler(offset);
			case 2:
				return input_port_1_r.handler(offset);
			case 4:
				return input_port_2_r.handler(offset);
			case 6:
				return input_port_3_r.handler(offset);
			case 8:
				return input_port_4_r.handler(offset);
			default:
	logerror("PC %06x - read input port %06x\n",cpu_get_pc(),0x100000+offset);
				return 0;
		}
	} };
	
	public static WriteHandlerPtr blockout_sound_command_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		switch (offset)
		{
			case 0:
				soundlatch_w.handler(offset,data);
				cpu_cause_interrupt(1,Z80_NMI_INT);
				break;
			case 2:
				/* don't know, maybe reset sound CPU */
				break;
		}
	} };
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x10000b, blockout_input_r ),
		new MemoryReadAddress( 0x180000, 0x1bffff, blockout_videoram_r ),
		new MemoryReadAddress( 0x1d4000, 0x1dffff, MRA_BANK1 ),	/* work RAM */
		new MemoryReadAddress( 0x1f4000, 0x1fffff, MRA_BANK2 ),	/* work RAM */
		new MemoryReadAddress( 0x200000, 0x207fff, blockout_frontvideoram_r ),
		new MemoryReadAddress( 0x208000, 0x21ffff, MRA_BANK3 ),	/* ??? */
		new MemoryReadAddress( 0x280200, 0x2805ff, paletteram_word_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100014, 0x100017, blockout_sound_command_w ),
		new MemoryWriteAddress( 0x180000, 0x1bffff, blockout_videoram_w, blockout_videoram ),
		new MemoryWriteAddress( 0x1d4000, 0x1dffff, MWA_BANK1 ),	/* work RAM */
		new MemoryWriteAddress( 0x1f4000, 0x1fffff, MWA_BANK2 ),	/* work RAM */
		new MemoryWriteAddress( 0x200000, 0x207fff, blockout_frontvideoram_w, blockout_frontvideoram ),
		new MemoryWriteAddress( 0x208000, 0x21ffff, MWA_BANK3 ),	/* ??? */
		new MemoryWriteAddress( 0x280002, 0x280003, blockout_frontcolor_w ),
		new MemoryWriteAddress( 0x280200, 0x2805ff, blockout_paletteram_w, paletteram ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0x87ff, MRA_RAM ),
		new MemoryReadAddress( 0x8801, 0x8801, YM2151_status_port_0_r ),
		new MemoryReadAddress( 0x9800, 0x9800, OKIM6295_status_0_r ),
		new MemoryReadAddress( 0xa000, 0xa000, soundlatch_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0x8000, 0x87ff, MWA_RAM ),
		new MemoryWriteAddress( 0x8800, 0x8800, YM2151_register_port_0_w ),
		new MemoryWriteAddress( 0x8801, 0x8801, YM2151_data_port_0_w ),
		new MemoryWriteAddress( 0x9800, 0x9800, OKIM6295_data_0_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	static InputPortPtr input_ports_blockout = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON4 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
	
		PORT_START(); 
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN3 );
	
		PORT_START(); 
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x00, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
		/* the following two are supposed to control Coin 2, but they don't work. */
		/* This happens on the original board too. */
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );	/* unused? */
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );	/* unused? */
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, "1 Coin to Continue" );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );	/* unused? */
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );	/* unused? */
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );
		PORT_DIPSETTING(    0x03, "Normal" );
		PORT_DIPSETTING(    0x01, "Hard" );
		PORT_DIPSETTING(    0x00, "Very Hard" );
		PORT_DIPNAME( 0x04, 0x04, "Rotate Buttons" );
		PORT_DIPSETTING(    0x00, "2" );
		PORT_DIPSETTING(    0x04, "3" );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
	INPUT_PORTS_END(); }}; 
	
	
	
	
	/* handler called by the 2151 emulator when the internal timers cause an IRQ */
	static WriteYmHandlerPtr blockout_irq_handler = new WriteYmHandlerPtr() {
            @Override
            public void handler(int irq) {
                cpu_set_irq_line(1,0,irq!=0 ? ASSERT_LINE : CLEAR_LINE);
		/* cpu_cause_interrupt(1,0xff); */
            }
        };
	
	static YM2151interface ym2151_interface = new YM2151interface
	(
		1,			/* 1 chip */
		3579545,	/* 3.579545 MHz (?) */
		new int[] { YM3012_VOL(60,MIXER_PAN_LEFT,60,MIXER_PAN_RIGHT) },
		new WriteYmHandlerPtr[] { blockout_irq_handler }
	);
	
	static OKIM6295interface okim6295_interface = new OKIM6295interface
	(
		1,                  /* 1 chip */
		new int[] { 8000 },           /* 8000Hz frequency */
		new int[] { REGION_SOUND1 },	/* memory region */
		new int[] { 50 }
	);
	
	
	
	static MachineDriver machine_driver_blockout = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				8760000,       /* MRH - 8.76 makes gfx/adpcm samples sync better */
				readmem,writemem,null,null,
				blockout_interrupt,2
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				3579545,	/* 3.579545 MHz (?) */
				sound_readmem,sound_writemem,null,null,
				ignore_interrupt,1	/* NMIs are triggered by the main CPU, IRQs by the YM2151 */
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		320, 256, new rectangle( 0, 319, 8, 247 ),
		null,
		513, 0,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		blockout_vh_start,
		blockout_vh_stop,
		blockout_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			),
			new MachineSound(
				SOUND_OKIM6295,
				okim6295_interface
			)
		}
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_blockout = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );/* 2*128k for 68000 code */
		ROM_LOAD_EVEN( "bo29a0-2.bin", 0x00000, 0x20000, 0xb0103427 );
		ROM_LOAD_ODD ( "bo29a1-2.bin", 0x00000, 0x20000, 0x5984d5a2 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "bo29e3-0.bin", 0x0000, 0x8000, 0x3ea01f78 );
	
		ROM_REGION( 0x20000, REGION_SOUND1 );/* 128k for ADPCM samples - sound chip is OKIM6295 */
		ROM_LOAD( "bo29e2-0.bin", 0x0000, 0x20000, 0x15c5a99d );
	
		ROM_REGION( 0x0100, REGION_PROMS );
		ROM_LOAD( "mb7114h.25",   0x0000, 0x0100, 0xb25bbda7 );/* unknown */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_blckout2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );/* 2*128k for 68000 code */
		ROM_LOAD_EVEN( "29a0",         0x00000, 0x20000, 0x605f931e );
		ROM_LOAD_ODD ( "29a1",         0x00000, 0x20000, 0x38f07000 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "bo29e3-0.bin", 0x0000, 0x8000, 0x3ea01f78 );
	
		ROM_REGION( 0x20000, REGION_SOUND1 );/* 128k for ADPCM samples - sound chip is OKIM6295 */
		ROM_LOAD( "bo29e2-0.bin", 0x0000, 0x20000, 0x15c5a99d );
	
		ROM_REGION( 0x0100, REGION_PROMS );
		ROM_LOAD( "mb7114h.25",   0x0000, 0x0100, 0xb25bbda7 );/* unknown */
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_blockout	   = new GameDriver("1989"	,"blockout"	,"blockout.java"	,rom_blockout,null	,machine_driver_blockout	,input_ports_blockout	,null	,ROT0	,	"Technos + California Dreams", "Block Out (set 1)" );
	public static GameDriver driver_blckout2	   = new GameDriver("1989"	,"blckout2"	,"blockout.java"	,rom_blckout2,driver_blockout	,machine_driver_blockout	,input_ports_blockout	,null	,ROT0	,	"Technos + California Dreams", "Block Out (set 2)" );
}
