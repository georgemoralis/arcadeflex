/***************************************************************************

							-= Back Street Soccer =-

					driver by	Luca Elia (eliavit@unina.it)


CPU:	68000   +  Z80 [Music]  +  Z80 x 2 [4 Bit PCM]
Sound:	YM2151  +  DAC x 4

***************************************************************************/

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
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.inputH.KEYCODE_F2;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static arcadeflex.v036.sound._2151intf.*;
import arcadeflex.v036.sound._2151intfH.YM2151interface;
import static arcadeflex.v036.sound._2151intfH.YM3012_VOL;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konamiic.*;
import static arcadeflex.v036.cpu.z80.z80H.Z80_NMI_INT;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295.*;
import gr.codebb.arcadeflex.v037b7.sound.okim6295H.OKIM6295interface;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.bssoccer.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static arcadeflex.v036.sound.dac.*;
import arcadeflex.v036.sound.dacH.DACinterface;
import static arcadeflex.v036.vidhrdw.generic.*;

public class bssoccer
{
	
	/* Variables and functions only used here */
	
	/* Variables that vidhrdw has access to */
	
	/* Variables and functions defined in vidhrdw */
	
	
	
	/***************************************************************************
	
	
									Main CPU
	
	
	***************************************************************************/
	
	static MemoryReadAddress bssoccer_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x1fffff, MRA_ROM					),	// ROM
		new MemoryReadAddress( 0x200000, 0x203fff, MRA_BANK1					),	// RAM
		new MemoryReadAddress( 0x400000, 0x4001ff, MRA_BANK2					),	// Palette
		new MemoryReadAddress( 0x400200, 0x400fff, MRA_BANK3					),	//
		new MemoryReadAddress( 0x600000, 0x61ffff, MRA_BANK4					),	// Sprites
		new MemoryReadAddress( 0xa00000, 0xa00001, input_port_0_r			),	// P1 (Inputs)
		new MemoryReadAddress( 0xa00002, 0xa00003, input_port_1_r			),	// P2
		new MemoryReadAddress( 0xa00004, 0xa00005, input_port_2_r			),	// P3
		new MemoryReadAddress( 0xa00006, 0xa00007, input_port_3_r			),	// P4
		new MemoryReadAddress( 0xa00008, 0xa00009, input_port_4_r			),	// DSWs
		new MemoryReadAddress( 0xa0000a, 0xa0000b, input_port_5_r			),	// Coins
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress bssoccer_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x1fffff, MWA_ROM							),	// ROM
		new MemoryWriteAddress( 0x200000, 0x203fff, MWA_BANK1							),	// RAM
		new MemoryWriteAddress( 0x400000, 0x4001ff, paletteram_xBBBBBGGGGGRRRRR_word_w, paletteram	),	// Palette
		new MemoryWriteAddress( 0x400200, 0x400fff, MWA_BANK3							),	//
		new MemoryWriteAddress( 0x600000, 0x61ffff, MWA_BANK4, spriteram				),	// Sprites
		new MemoryWriteAddress( 0xa00000, 0xa00009, bssoccer_vregs_w, bssoccer_vregs	),	// Latches + Registers
		new MemoryWriteAddress( -1 )
	};
	
	
	/***************************************************************************
	
	
										Z80 #1
	
			Plays the music (YM2151) and controls the 2 Z80s in charge
			of playing the PCM samples
	
	
	***************************************************************************/
	
	static MemoryReadAddress bssoccer_sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM					),	// ROM
		new MemoryReadAddress( 0xf000, 0xf7ff, MRA_RAM					),	// RAM
		new MemoryReadAddress( 0xf801, 0xf801, YM2151_status_port_0_r	),	// YM2151
		new MemoryReadAddress( 0xfc00, 0xfc00, soundlatch_r				),	// From Main CPU
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress bssoccer_sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM					),	// ROM
		new MemoryWriteAddress( 0xf000, 0xf7ff, MWA_RAM					),	// RAM
		new MemoryWriteAddress( 0xf800, 0xf800, YM2151_register_port_0_w	),	// YM2151
		new MemoryWriteAddress( 0xf801, 0xf801, YM2151_data_port_0_w		),	//
		new MemoryWriteAddress( 0xfd00, 0xfd00, soundlatch2_w				),	// To PCM Z80 #1
		new MemoryWriteAddress( 0xfe00, 0xfe00, soundlatch3_w				),	// To PCM Z80 #2
		new MemoryWriteAddress( -1 )
	};
	
	
	/***************************************************************************
	
	
									Z80 #2 & #3
	
			Dumb PCM samples players (e.g they don't even have RAM!)
	
	
	***************************************************************************/
	
	/* Bank Switching */
	
	public static WriteHandlerPtr bssoccer_pcm_1_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_CPU3);
		int bank = data & 7;
		cpu_setbank(14, new UBytePtr(RAM, bank * 0x10000 + 0x1000));
	} };
	
	public static WriteHandlerPtr bssoccer_pcm_2_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_CPU4);
		int bank = data & 7;
		cpu_setbank(15, new UBytePtr(RAM, bank * 0x10000 + 0x1000));
	} };
	
	
	
	/* Memory maps: Yes, *no* RAM */
	
	static MemoryReadAddress bssoccer_pcm_1_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x0fff, MRA_ROM			),	// ROM
		new MemoryReadAddress( 0x1000, 0xffff, MRA_BANK14		),	// Banked ROM
		new MemoryReadAddress( -1 )
	};
	static MemoryWriteAddress bssoccer_pcm_1_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xffff, MWA_ROM			),	// ROM
		new MemoryWriteAddress( -1 )
	};
	
	
	static MemoryReadAddress bssoccer_pcm_2_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x0fff, MRA_ROM			),	// ROM
		new MemoryReadAddress( 0x1000, 0xffff, MRA_BANK15		),	// Banked ROM
		new MemoryReadAddress( -1 )
	};
	static MemoryWriteAddress bssoccer_pcm_2_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xffff, MWA_ROM			),	// ROM
		new MemoryWriteAddress( -1 )
	};
	
	
	
	/* 2 DACs per CPU - 4 bits per sample */
	
	public static WriteHandlerPtr bssoccer_DAC_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		DAC_data_w.handler( 0 + (offset & 1), (data & 0xf) * 0x11 );
	} };
	
	public static WriteHandlerPtr bssoccer_DAC_2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		DAC_data_w.handler( 2 + (offset & 1), (data & 0xf) * 0x11 );
	} };
	
	
	
	static IOReadPort bssoccer_pcm_1_readport[] =
	{
		new IOReadPort( 0x00, 0x00, soundlatch2_r					),	// From The Sound Z80
		new IOReadPort( -1 )
	};
	static IOWritePort bssoccer_pcm_1_writeport[] =
	{
		new IOWritePort( 0x00, 0x01, bssoccer_DAC_1_w				),	// 2 x DAC
		new IOWritePort( 0x03, 0x03, bssoccer_pcm_1_bankswitch_w	),	// Rom Bank
		new IOWritePort( -1 )
	};
	
	static IOReadPort bssoccer_pcm_2_readport[] =
	{
		new IOReadPort( 0x00, 0x00, soundlatch3_r					),	// From The Sound Z80
		new IOReadPort( -1 )
	};
	static IOWritePort bssoccer_pcm_2_writeport[] =
	{
		new IOWritePort( 0x00, 0x01, bssoccer_DAC_2_w				),	// 2 x DAC
		new IOWritePort( 0x03, 0x03, bssoccer_pcm_2_bankswitch_w	),	// Rom Bank
		new IOWritePort( -1 )
	};
	
	
	
	
	/***************************************************************************
	
	
									Input Ports
	
	
	***************************************************************************/
	
	
	static InputPortHandlerPtr input_ports_bssoccer = new InputPortHandlerPtr(){ public void handler() { 
	
		PORT_START(); 	// IN0 - Player 1
		//JOY(1)
                PORT_BIT(  0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_UP     |  IPF_PLAYER1 );
		PORT_BIT(  0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN   |  IPF_PLAYER1 );
		PORT_BIT(  0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT   |  IPF_PLAYER1 );
		PORT_BIT(  0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT  |  IPF_PLAYER1 );
		PORT_BIT(  0x0010, IP_ACTIVE_LOW, IPT_BUTTON1         |  IPF_PLAYER1 );
		PORT_BIT(  0x0020, IP_ACTIVE_LOW, IPT_BUTTON2         |  IPF_PLAYER1 );
		PORT_BIT(  0x0040, IP_ACTIVE_LOW, IPT_BUTTON3         |  IPF_PLAYER1 );
		PORT_BIT(  0x0080, IP_ACTIVE_LOW, IPT_START1);
	
		PORT_START(); 	// IN1 - Player 2
		//JOY(2)
                PORT_BIT(  0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_UP     |  IPF_PLAYER2 );
		PORT_BIT(  0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN   |  IPF_PLAYER2 );
		PORT_BIT(  0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT   |  IPF_PLAYER2 );
		PORT_BIT(  0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT  |  IPF_PLAYER2 );
		PORT_BIT(  0x0010, IP_ACTIVE_LOW, IPT_BUTTON1         |  IPF_PLAYER2 );
		PORT_BIT(  0x0020, IP_ACTIVE_LOW, IPT_BUTTON2         |  IPF_PLAYER2 );
		PORT_BIT(  0x0040, IP_ACTIVE_LOW, IPT_BUTTON3         |  IPF_PLAYER2 );
		PORT_BIT(  0x0080, IP_ACTIVE_LOW, IPT_START2);
	
		PORT_START(); 	// IN2 - Player 3
		//JOY(3)
                PORT_BIT(  0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_UP     |  IPF_PLAYER3 );
		PORT_BIT(  0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN   |  IPF_PLAYER3 );
		PORT_BIT(  0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT   |  IPF_PLAYER3 );
		PORT_BIT(  0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT  |  IPF_PLAYER3 );
		PORT_BIT(  0x0010, IP_ACTIVE_LOW, IPT_BUTTON1         |  IPF_PLAYER3 );
		PORT_BIT(  0x0020, IP_ACTIVE_LOW, IPT_BUTTON2         |  IPF_PLAYER3 );
		PORT_BIT(  0x0040, IP_ACTIVE_LOW, IPT_BUTTON3         |  IPF_PLAYER3 );
		PORT_BIT(  0x0080, IP_ACTIVE_LOW, IPT_START3);
	
		PORT_START(); 	// IN3 - Player 4
		//JOY(4)
                PORT_BIT(  0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_UP     |  IPF_PLAYER4 );
		PORT_BIT(  0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN   |  IPF_PLAYER4 );
		PORT_BIT(  0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT   |  IPF_PLAYER4 );
		PORT_BIT(  0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT  |  IPF_PLAYER4 );
		PORT_BIT(  0x0010, IP_ACTIVE_LOW, IPT_BUTTON1         |  IPF_PLAYER4 );
		PORT_BIT(  0x0020, IP_ACTIVE_LOW, IPT_BUTTON2         |  IPF_PLAYER4 );
		PORT_BIT(  0x0040, IP_ACTIVE_LOW, IPT_BUTTON3         |  IPF_PLAYER4 );
		PORT_BIT(  0x0080, IP_ACTIVE_LOW, IPT_START4);
	
		PORT_START(); 	// IN4 - 2 DSWs
		PORT_DIPNAME( 0x0007, 0x0007, DEF_STR( "Coinage") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(      0x0001, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(      0x0002, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(      0x0007, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(      0x0006, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(      0x0005, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(      0x0004, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(      0x0003, DEF_STR( "1C_5C") );
		PORT_DIPNAME( 0x0018, 0x0018, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(      0x0010, "Easy"     );
		PORT_DIPSETTING(      0x0018, "Normal"   );
		PORT_DIPSETTING(      0x0008, "Hard"     );
		PORT_DIPSETTING(      0x0000, "Hardest?" );
		PORT_DIPNAME( 0x0020, 0x0020, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0020, DEF_STR( "On") );
		PORT_DIPNAME( 0x0040, 0x0040, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(      0x0040, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_SERVICE( 0x0080, IP_ACTIVE_LOW );
	
		PORT_DIPNAME( 0x0300, 0x0300, "Play Time P1" );
		PORT_DIPSETTING(      0x0300, "1:30" );
		PORT_DIPSETTING(      0x0200, "1:45" );
		PORT_DIPSETTING(      0x0100, "2:00" );
		PORT_DIPSETTING(      0x0000, "2:15" );
		PORT_DIPNAME( 0x0c00, 0x0c00, "Play Time P2" );
		PORT_DIPSETTING(      0x0c00, "1:30" );
		PORT_DIPSETTING(      0x0800, "1:45" );
		PORT_DIPSETTING(      0x0400, "2:00" );
		PORT_DIPSETTING(      0x0000, "2:15" );
		PORT_DIPNAME( 0x3000, 0x3000, "Play Time P3" );
		PORT_DIPSETTING(      0x3000, "1:30" );
		PORT_DIPSETTING(      0x2000, "1:45" );
		PORT_DIPSETTING(      0x1000, "2:00" );
		PORT_DIPSETTING(      0x0000, "2:15" );
		PORT_DIPNAME( 0xc000, 0xc000, "Play Time P4" );
		PORT_DIPSETTING(      0xc000, "1:30" );
		PORT_DIPSETTING(      0x8000, "1:45" );
		PORT_DIPSETTING(      0x4000, "2:00" );
		PORT_DIPSETTING(      0x0000, "2:15" );
	
		PORT_START(); 	// IN5 - Coins
		PORT_DIPNAME( 0x0001, 0x0001, "Copyright" );		// these 4 are shown in test mode
		PORT_DIPSETTING(      0x0001, "Distributer Unico" );
		PORT_DIPSETTING(      0x0000, "All Rights Reserved" );
		PORT_DIPNAME( 0x0002, 0x0002, DEF_STR( "Unknown") );	// used!
		PORT_DIPSETTING(      0x0002, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0002, 0x0002, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0004, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_DIPNAME( 0x0002, 0x0002, DEF_STR( "Unknown") );
		PORT_DIPSETTING(      0x0008, DEF_STR( "Off") );
		PORT_DIPSETTING(      0x0000, DEF_STR( "On") );
		PORT_BIT(  0x0010, IP_ACTIVE_LOW,  IPT_COIN1   );
		PORT_BIT(  0x0020, IP_ACTIVE_LOW,  IPT_COIN2   );
		PORT_BIT(  0x0040, IP_ACTIVE_LOW,  IPT_COIN3   );
		PORT_BIT(  0x0080, IP_ACTIVE_LOW,  IPT_COIN4   );
	
	INPUT_PORTS_END(); }}; 
	
	
	
	/***************************************************************************
	
	
									Graphics Layouts
	
	
	***************************************************************************/
	
	/* Tiles are 8x8x4 but the minimum sprite size is 2x2 tiles */
	
	static GfxLayout layout_8x8x4 = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,2),
		4,
		new int[] { RGN_FRAC(1,2)+0,RGN_FRAC(1,2)+4,	0,4 },
		new int[] {3,2,1,0, 11,10,9,8},
		new int[] {0*16,1*16,2*16,3*16,4*16,5*16,6*16,7*16},
		8*8*2
	);
	
	static GfxDecodeInfo bssoccer_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, layout_8x8x4, 0, 16 ), // [0] Sprites
		new GfxDecodeInfo( -1 )
	};
	
	
	
	
	/***************************************************************************
	
	
									Machine drivers
	
	
	***************************************************************************/
	
	static YM2151interface bssoccer_ym2151_interface = new YM2151interface
	(
		1,
		3579545,	/* ? */
		new int[] { YM3012_VOL(10,MIXER_PAN_LEFT,10,MIXER_PAN_RIGHT) },
		new WriteYmHandlerPtr[] { null },		/* irq handler */
		new WriteHandlerPtr[] { null }		/* port write handler */
	);
	
	static DACinterface bssoccer_dac_interface = new DACinterface
	(
		4,
		new int[] {
			MIXER(23,MIXER_PAN_LEFT), MIXER(23,MIXER_PAN_RIGHT),
			MIXER(23,MIXER_PAN_LEFT), MIXER(23,MIXER_PAN_RIGHT)
		}
	);
	
	public static InterruptHandlerPtr bssoccer_interrupt = new InterruptHandlerPtr() { public int handler() 
	{
		switch (cpu_getiloops())
		{
			case 0:		return 1;
			case 1:		return 2;
			default:	return ignore_interrupt.handler();
		}
	} };
	
	static MachineDriver machine_driver_bssoccer = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				8000000,	/* ? */
				bssoccer_readmem, bssoccer_writemem,null,null,
				bssoccer_interrupt, 2
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,	/* Z80B */
				3579545,	/* ? */
				bssoccer_sound_readmem,	 bssoccer_sound_writemem,
				null,null,
				ignore_interrupt, 1		/* No interrupts! */
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,	/* Z80B */
				5000000,	/* ? */
				bssoccer_pcm_1_readmem,	 bssoccer_pcm_1_writemem,
				bssoccer_pcm_1_readport, bssoccer_pcm_1_writeport,
				ignore_interrupt, 1		/* No interrupts! */
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,	/* Z80B */
				5000000,	/* ? */
				bssoccer_pcm_2_readmem,	 bssoccer_pcm_2_writemem,
				bssoccer_pcm_2_readport, bssoccer_pcm_2_writeport,
				ignore_interrupt, 1		/* No interrupts! */
			)
		},
		60,DEFAULT_60HZ_VBLANK_DURATION,
		100,
		null,
	
		/* video hardware */
		256, 256, new rectangle( 0, 256-1, 0+16, 256-16-1 ),
		bssoccer_gfxdecodeinfo,
		256, 256,
		null,
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		null,	/* No need for a vh_start: we only have sprites */
		null,
		bssoccer_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,
				bssoccer_ym2151_interface
			),
			new MachineSound(
				SOUND_DAC,
				bssoccer_dac_interface
			),
		}
	);
	
	
	/***************************************************************************
	
	
									ROMs Loading
	
	
	***************************************************************************/
	
	
	/***************************************************************************
	
								[ Back Street Soccer ]
	
	  68000-10  32MHz
	            14.318MHz
	  01   02                    12
	  03   04                   Z80B
	  6264 6264       YM2151
	                  6116
	                   11      13
	  62256           Z80B    Z80B
	  62256
	  62256   05 06                  SW2
	          07 08                  SW1
	          09 10          6116-45
	                                     6116-45
	                         6116-45     6116-45
	
	***************************************************************************/
	
	static RomLoadHandlerPtr rom_bssoccer = new RomLoadHandlerPtr(){ public void handler(){ 
	
		ROM_REGION( 0x200000, REGION_CPU1 );	/* 68000 Code */
		ROM_LOAD_EVEN( "02", 0x000000, 0x080000, 0x32871005 );
		ROM_LOAD_ODD(  "01", 0x000000, 0x080000, 0xace00db6 );
		ROM_LOAD_EVEN( "04", 0x100000, 0x080000, 0x25ee404d );
		ROM_LOAD_ODD(  "03", 0x100000, 0x080000, 0x1a131014 );
	
		ROM_REGION( 0x010000, REGION_CPU2 );	/* Z80 #1 - Music */
		ROM_LOAD( "11", 0x000000, 0x010000, 0xdf7ae9bc );// 1xxxxxxxxxxxxxxx = 0xFF
	
		ROM_REGION( 0x080000, REGION_CPU3 );	/* Z80 #2 - PCM */
		ROM_LOAD( "13", 0x000000, 0x080000, 0x2b273dca );
	
		ROM_REGION( 0x080000, REGION_CPU4 );	/* Z80 #3 - PCM */
		ROM_LOAD( "12", 0x000000, 0x080000, 0x6b73b87b );
	
		ROM_REGION( 0x300000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* Sprites */
		ROM_LOAD( "05", 0x000000, 0x080000, 0xa5245bd4 );
		ROM_LOAD( "07", 0x080000, 0x080000, 0xfdb765c2 );
		ROM_LOAD( "09", 0x100000, 0x080000, 0x0e82277f );
		ROM_LOAD( "06", 0x180000, 0x080000, 0xd42ce84b );
		ROM_LOAD( "08", 0x200000, 0x080000, 0x96cd2136 );
		ROM_LOAD( "10", 0x280000, 0x080000, 0x1ca94d21 );
	
	ROM_END(); }}; 
	
	
	public static InitDriverHandlerPtr init_bssoccer = new InitDriverHandlerPtr() { public void handler() 
	{
		UBytePtr RAM	=	memory_region(REGION_GFX1);
		int i, len			=	memory_region_length(REGION_GFX1);
	
		for (i=0;i<len;i++)	RAM.write(i, RAM.read(i)^0xff);	// invert all the bits of sprites
	} };
	
	
	public static GameDriver driver_bssoccer	   = new GameDriver("1996"	,"bssoccer"	,"bssoccer.java"	,rom_bssoccer,null	,machine_driver_bssoccer	,input_ports_bssoccer	,init_bssoccer	,ROT0	,	"Suna", "Back Street Soccer" );
}
