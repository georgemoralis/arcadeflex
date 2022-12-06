/***************************************************************************

Super Basketball memory map (preliminary)
(Hold down Start 1  Start 2 keys to enter test mode on start up;
 use Start 1 to change modes)

driver by Zsolt Vasvari

MAIN BOARD:
2000-2fff RAM
3000-33ff Color RAM
3400-37ff Video RAM
3800-39ff Sprite RAM
6000-ffff ROM

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package gr.codebb.arcadeflex.v036.drivers;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.vidhrdw.sbasketb.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b7.sndhrdw.trackfld.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v058.sound.vlm5030.*;
import static gr.codebb.arcadeflex.v058.sound.vlm5030H.*;
import static gr.codebb.arcadeflex.v037b7.sound.dac.*;

public class sbasketb
{
	public static WriteHandlerPtr sbasketb_sh_irqtrigger_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		cpu_cause_interrupt(1,0xff);
	} };
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x2000, 0x3bff, MRA_RAM ),
		new MemoryReadAddress( 0x3c10, 0x3c10, MRA_NOP ),    /* ???? */
		new MemoryReadAddress( 0x3e00, 0x3e00, input_port_0_r ),
		new MemoryReadAddress( 0x3e01, 0x3e01, input_port_1_r ),
		new MemoryReadAddress( 0x3e02, 0x3e02, input_port_2_r ),
		new MemoryReadAddress( 0x3e03, 0x3e03, MRA_NOP ),
		new MemoryReadAddress( 0x3e80, 0x3e80, input_port_3_r ),
		new MemoryReadAddress( 0x3f00, 0x3f00, input_port_4_r ),
		new MemoryReadAddress( 0x6000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x2000, 0x2fff, MWA_RAM ),
		new MemoryWriteAddress( 0x3000, 0x33ff, colorram_w, colorram ),
		new MemoryWriteAddress( 0x3400, 0x37ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x3800, 0x39ff, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x3a00, 0x3bff, MWA_RAM ),           /* Probably unused, but initialized */
		new MemoryWriteAddress( 0x3c00, 0x3c00, watchdog_reset_w ),
		new MemoryWriteAddress( 0x3c20, 0x3c20, MWA_RAM, sbasketb_palettebank ),
		new MemoryWriteAddress( 0x3c80, 0x3c80, sbasketb_flipscreen_w ),
		new MemoryWriteAddress( 0x3c81, 0x3c81, interrupt_enable_w ),
		new MemoryWriteAddress( 0x3c83, 0x3c84, coin_counter_w ),
		new MemoryWriteAddress( 0x3c85, 0x3c85, MWA_RAM, sbasketb_spriteram_select ),
		new MemoryWriteAddress( 0x3d00, 0x3d00, soundlatch_w ),
		new MemoryWriteAddress( 0x3d80, 0x3d80, sbasketb_sh_irqtrigger_w ),
		new MemoryWriteAddress( 0x3f80, 0x3f80, MWA_RAM, sbasketb_scroll ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x1fff, MRA_ROM ),
		new MemoryReadAddress( 0x4000, 0x43ff, MRA_RAM ),
		new MemoryReadAddress( 0x6000, 0x6000, soundlatch_r ),
		new MemoryReadAddress( 0x8000, 0x8000, hyperspt_sh_timer_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x4000, 0x43ff, MWA_RAM ),
		new MemoryWriteAddress( 0xa000, 0xa000, VLM5030_data_w ), /* speech data */
		new MemoryWriteAddress( 0xc000, 0xdfff, hyperspt_sound_w ),     /* speech and output controll */
		new MemoryWriteAddress( 0xe000, 0xe000, DAC_data_w ),
		new MemoryWriteAddress( 0xe001, 0xe001, konami_SN76496_latch_w ),  /* Loads the snd command into the snd latch */
		new MemoryWriteAddress( 0xe002, 0xe002, konami_SN76496_0_w ),      /* This address triggers the SN chip to read the data port. */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	
	static InputPortPtr input_ports_sbasketb = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL  );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW0 */
		PORT_DIPNAME( 0x03, 0x00, "Game Time" );
		PORT_DIPSETTING(    0x03, "30" );
		PORT_DIPSETTING(    0x01, "40" );
		PORT_DIPSETTING(    0x02, "50" );
		PORT_DIPSETTING(    0x00, "60" );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x08, 0x08, "Starting Score" );
		PORT_DIPSETTING(    0x08, "70-78" );
		PORT_DIPSETTING(    0x00, "100-115" );
		PORT_DIPNAME( 0x10, 0x00, "Ranking" );
		PORT_DIPSETTING(    0x00, "Data Remaining" );
		PORT_DIPSETTING(    0x10, "Data Initialized" );
		PORT_DIPNAME( 0x60, 0x60, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x60, "Easy" );
		PORT_DIPSETTING(    0x40, "Medium" );
		PORT_DIPSETTING(    0x20, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* DSW1 */
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
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
	INPUT_PORTS_END(); }}; 
	
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,    /* 8*8 characters */
		512,    /* 512 characters */
		4,      /* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 }, /* the bitplanes are packed */
		new int[] { 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4 },
		new int[] { 0*4*8, 1*4*8, 2*4*8, 3*4*8, 4*4*8, 5*4*8, 6*4*8, 7*4*8 },
		8*4*8     /* every char takes 32 consecutive bytes */
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,  /* 16*16 sprites */
		128 * 3,/* 384 sprites */
		4,      /* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },        /* the bitplanes are packed */
		new int[] { 0*4, 1*4,  2*4,  3*4,  4*4,  5*4,  6*4,  7*4,
				8*4, 9*4, 10*4, 11*4, 12*4, 13*4, 14*4, 15*4 },
		new int[] { 0*4*16, 1*4*16,  2*4*16,  3*4*16,  4*4*16,  5*4*16,  6*4*16,  7*4*16,
				8*4*16, 9*4*16, 10*4*16, 11*4*16, 12*4*16, 13*4*16, 14*4*16, 15*4*16 },
		32*4*8    /* every sprite takes 128 consecutive bytes */
	);
	
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,       0, 16 ),
		new GfxDecodeInfo( REGION_GFX2, 0, spritelayout, 16*16, 16*16 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	/* filenames for sample files */
	static String sbasketb_sample_names[] =
	{
		"00.wav","01.wav","02.wav","03.wav","04.wav","05.wav","06.wav","07.wav",
		"08.wav","09.wav","0a.wav","0b.wav","0c.wav","0d.wav","0e.wav","0f.wav",
		"10.wav","11.wav","12.wav","13.wav","14.wav","15.wav","16.wav","17.wav",
		"18.wav","19.wav","1a.wav","1b.wav","1c.wav","1d.wav","1e.wav","1f.wav",
		"20.wav","21.wav","22.wav","23.wav","24.wav","25.wav","26.wav","27.wav",
		"28.wav","29.wav","2a.wav","2b.wav","2c.wav","2d.wav","2e.wav","2f.wav",
		"30.wav","31.wav","32.wav","33.wav",
		null
	};
	
	public static VLM5030interface sbasketb_vlm5030_interface = new VLM5030interface
	(
		3580000,    /* master clock  */
		255,        /* volume        */
		REGION_SOUND1,	/* memory region  */
		0,         /* memory size    */
		sbasketb_sample_names
        );
	
	
	static MachineDriver machine_driver_sbasketb = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6809,
				1400000,        /* 1.400 Mhz ??? */
				readmem,writemem,null,null,
				interrupt,1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				14318000/4,	/* 3.5795 Mhz */
				sound_readmem,sound_writemem,null,null,
				ignore_interrupt,1	/* interrupts are triggered by the main CPU */
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		256,16*16+16*16*16,
		sbasketb_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		generic_vh_start,
		generic_vh_stop,
		sbasketb_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_DAC,
				konami_dac_interface
			),
			new MachineSound(
				SOUND_SN76496,
				konami_sn76496_interface
			),
			new MachineSound(
				SOUND_VLM5030,
				sbasketb_vlm5030_interface
			)
		}
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_sbasketb = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "sbb_j13.bin",  0x6000, 0x2000, 0x263ec36b );
		ROM_LOAD( "sbb_j11.bin",  0x8000, 0x4000, 0x0a4d7a82 );
		ROM_LOAD( "sbb_j09.bin",  0xc000, 0x4000, 0x4f9dd9a0 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for audio cpu */
		ROM_LOAD( "sbb_e13.bin",  0x0000, 0x2000, 0x1ec7458b );
	
		ROM_REGION( 0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "sbb_e12.bin",  0x0000, 0x4000, 0xe02c54da );
	
		ROM_REGION( 0x0c000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "sbb_h06.bin",  0x0000, 0x4000, 0xcfbbff07 );
		ROM_LOAD( "sbb_h08.bin",  0x4000, 0x4000, 0xc75901b6 );
		ROM_LOAD( "sbb_h10.bin",  0x8000, 0x4000, 0x95bc5942 );
	
		ROM_REGION( 0x0500, REGION_PROMS );
		ROM_LOAD( "405e17",       0x0000, 0x0100, 0xb4c36d57 );/* palette red component */
		ROM_LOAD( "405e16",       0x0100, 0x0100, 0x0b7b03b8 );/* palette green component */
		ROM_LOAD( "405e18",       0x0200, 0x0100, 0x9e533bad );/* palette blue component */
		ROM_LOAD( "405e20",       0x0300, 0x0100, 0x8ca6de2f );/* character lookup table */
		ROM_LOAD( "405e19",       0x0400, 0x0100, 0xe0bc782f );/* sprite lookup table */
	
		ROM_REGION( 0x10000, REGION_SOUND1 );    /* 64k for speech rom */
		ROM_LOAD( "sbb_e15.bin",  0x0000, 0x2000, 0x01bb5ce9 );
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_sbasketb	   = new GameDriver("1984"	,"sbasketb"	,"sbasketb.java"	,rom_sbasketb,null	,machine_driver_sbasketb	,input_ports_sbasketb	,null	,ROT90	,	"Konami", "Super Basketball" );
}
