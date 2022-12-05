/***************************************************************************

Konami games memory map (preliminary)

Based on drivers from Juno First emulator by Chris Hardy (chrish@kcbbs.gen.nz)

Track'n'Field

MAIN BOARD:
0000-17ff RAM
1800-183f Sprite RAM Pt 1
1C00-1C3f Sprite RAM Pt 2
3800-3bff Color RAM
3000-33ff Video RAM
6000-ffff ROM
1200-12ff IO

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.09
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
import static gr.codebb.arcadeflex.v036.sound.sn76496.*;
import static arcadeflex.v036.vidhrdw.trackfld.*;
import static gr.codebb.arcadeflex.v037b7.machine.konami.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b7.sndhrdw.trackfld.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v058.sound.vlm5030.*;
import static gr.codebb.arcadeflex.v058.sound.vlm5030H.*;
import static gr.codebb.arcadeflex.v037b7.sound.dac.*;

public class trackfld
{
	
	
	/* handle fake button for speed cheat */
       static int cheat_track = 0;
	static int bits_track[] = { 0xee, 0xff, 0xbb, 0xaa };
	public static ReadHandlerPtr konami_IN1_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		int res;
		
	
		res = readinputport(1);
	
		if ((res & 0x80) == 0)
		{
			res |= 0x55;
			res &= bits_track[cheat_track];
			cheat_track = (++cheat_track)%4;
		}
		return res;
	} };
	
	
	
	/*
	 Track'n'Field has 1k of battery backed RAM which can be erased by setting a dipswitch
	*/
	static UBytePtr nvram=new UBytePtr();
	static int[] nvram_size=new int[1];
	static int we_flipped_the_switch;
	
        public static nvramPtr nvram_handler = new nvramPtr(){ public void handler(Object file,int read_or_write)
        {
	/*	if (read_or_write)
		{
			osd_fwrite(file,nvram,nvram_size);
	
			if (we_flipped_the_switch)
			{
				struct InputPort *in;
	
	
				/* find the dip switch which resets the high score table, and set it */
				/* back to off. */
	/*			in = Machine.input_ports;
	
				while (in.type != IPT_END)
				{
					if (in.name != NULL && in.name != IP_NAME_DEFAULT &&
							strcmp(in.name,"World Records") == 0)
					{
						if (in.default_value == 0)
							in.default_value = in.mask;
						break;
					}
	
					in++;
				}
	
				we_flipped_the_switch = 0;
			}
		}
		else
		{
			if (file)
			{
				osd_fread(file,nvram,nvram_size);
				we_flipped_the_switch = 0;
			}
			else
			{
				struct InputPort *in;
	
	
				/* find the dip switch which resets the high score table, and set it on */
	/*			in = Machine.input_ports;
	
				while (in.type != IPT_END)
				{
					if (in.name != NULL && in.name != IP_NAME_DEFAULT &&
							strcmp(in.name,"World Records") == 0)
					{
						if (in.default_value == in.mask)
						{
							in.default_value = 0;
							we_flipped_the_switch = 1;
						}
						break;
					}
	
					in++;
				}
			}
		}*/
	}};
	
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x1800, 0x185f, MRA_RAM ),
		new MemoryReadAddress( 0x1c00, 0x1c5f, MRA_RAM ),
		new MemoryReadAddress( 0x1200, 0x1200, input_port_4_r ), /* DIP 2 */
		new MemoryReadAddress( 0x1280, 0x1280, input_port_0_r ), /* IO Coin */
	//	new MemoryReadAddress( 0x1281, 0x1281, input_port_1_r ), /* P1 IO */
		new MemoryReadAddress( 0x1281, 0x1281, konami_IN1_r ),	/* P1 IO and handle fake button for cheating */
		new MemoryReadAddress( 0x1282, 0x1282, input_port_2_r ), /* P2 IO */
		new MemoryReadAddress( 0x1283, 0x1283, input_port_3_r ), /* DIP 1 */
		new MemoryReadAddress( 0x2800, 0x3fff, MRA_RAM ),
		new MemoryReadAddress( 0x6000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x1000, 0x1000, watchdog_reset_w ),
		new MemoryWriteAddress( 0x1080, 0x1080, trackfld_flipscreen_w ),
		new MemoryWriteAddress( 0x1081, 0x1081, konami_sh_irqtrigger_w ),  /* cause interrupt on audio CPU */
		new MemoryWriteAddress( 0x1083, 0x1083, MWA_NOP ),  /* Coin counter 1 */
		new MemoryWriteAddress( 0x1084, 0x1084, MWA_NOP ),  /* Coin counter 2 */
		new MemoryWriteAddress( 0x1087, 0x1087, interrupt_enable_w ),
		new MemoryWriteAddress( 0x1100, 0x1100, soundlatch_w ),
		new MemoryWriteAddress( 0x2800, 0x2fff, MWA_RAM ),
		new MemoryWriteAddress( 0x1800, 0x183f, MWA_RAM, spriteram_2 ),
		new MemoryWriteAddress( 0x1840, 0x185f, MWA_RAM, trackfld_scroll ),  /* Scroll amount */
		new MemoryWriteAddress( 0x1C00, 0x1c3f, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x1C40, 0x1C5f, MWA_RAM, trackfld_scroll2 ),  /* Scroll amount */
		new MemoryWriteAddress( 0x2800, 0x2bff, MWA_RAM ),
		new MemoryWriteAddress( 0x2c00, 0x2fff, MWA_RAM, nvram, nvram_size ),
		new MemoryWriteAddress( 0x3000, 0x37ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x3800, 0x3fff, colorram_w, colorram ),
		new MemoryWriteAddress( 0x6000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new MemoryReadAddress( 0x4000, 0x43ff, MRA_RAM ),
		new MemoryReadAddress( 0x6000, 0x6000, soundlatch_r ),
		new MemoryReadAddress( 0x8000, 0x8000, trackfld_sh_timer_r ),
		new MemoryReadAddress( 0xe002, 0xe002, trackfld_speech_r ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x3fff, MWA_ROM ),
		new MemoryWriteAddress( 0x4000, 0x43ff, MWA_RAM ),
		new MemoryWriteAddress( 0xa000, 0xa000, SN76496_0_w ),	/* Loads the snd command into the snd latch */
		new MemoryWriteAddress( 0xc000, 0xc000, MWA_NOP ),		/* This address triggers the SN chip to read the data port. */
		new MemoryWriteAddress( 0xe000, 0xe000, DAC_data_w ),
	/* There are lots more addresses which are used for setting a two bit volume
		controls for speech and music
	
		Currently these are un-supported by Mame
	*/
		new MemoryWriteAddress( 0xe001, 0xe001, MWA_NOP ), /* watch dog ? */
		new MemoryWriteAddress( 0xe004, 0xe004, VLM5030_data_w ),
		new MemoryWriteAddress( 0xe000, 0xefff, trackfld_sound_w ), /* e003 speech control */
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress hyprolyb_sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new MemoryReadAddress( 0x4000, 0x43ff, MRA_RAM ),
		new MemoryReadAddress( 0x6000, 0x6000, soundlatch_r ),
		new MemoryReadAddress( 0x8000, 0x8000, trackfld_sh_timer_r ),
		new MemoryReadAddress( 0xe002, 0xe002, hyprolyb_speech_r ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress hyprolyb_sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x3fff, MWA_ROM ),
		new MemoryWriteAddress( 0x4000, 0x43ff, MWA_RAM ),
		new MemoryWriteAddress( 0xa000, 0xa000, SN76496_0_w ),	/* Loads the snd command into the snd latch */
		new MemoryWriteAddress( 0xc000, 0xc000, MWA_NOP ),		/* This address triggers the SN chip to read the data port. */
		new MemoryWriteAddress( 0xe000, 0xe000, DAC_data_w ),
	/* There are lots more addresses which are used for setting a two bit volume
		controls for speech and music
	
		Currently these are un-supported by Mame
	*/
		new MemoryWriteAddress( 0xe001, 0xe001, MWA_NOP ), /* watch dog ? */
		new MemoryWriteAddress( 0xe004, 0xe004, hyprolyb_ADPCM_data_w ),
		new MemoryWriteAddress( 0xe000, 0xefff, MWA_NOP ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	
	static InputPortPtr input_ports_trackfld = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START3 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
	//	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		/* Fake button to press buttons 1 and 3 impossibly fast. Handle via konami_IN1_r */
		PORT_BITX(0x80, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_CHEAT | IPF_PLAYER1, "Run Like Hell Cheat", IP_KEY_DEFAULT, IP_JOY_DEFAULT );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER3 /*| IPF_COCKTAIL*/ );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 /*| IPF_COCKTAIL*/ );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 /*| IPF_COCKTAIL*/ );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START4 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER4 /*| IPF_COCKTAIL*/ );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4 /*| IPF_COCKTAIL*/ );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4 /*| IPF_COCKTAIL*/ );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* DSW0 */
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
		PORT_DIPSETTING(    0x00, "Disabled" );
	/* 0x00 disables Coin 2. It still accepts coins and makes the sound, but
	   it doesn't give you any credit */
	
		PORT_START();       /* DSW1 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x01, "1" );
		PORT_DIPSETTING(    0x00, "2" );
		PORT_DIPNAME( 0x02, 0x00, "After Last Event" );
		PORT_DIPSETTING(    0x02, "Game Over" );
		PORT_DIPSETTING(    0x00, "Game Continues" );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x08, "None" );
		PORT_DIPSETTING(    0x00, "100000" );
		PORT_DIPNAME( 0x10, 0x10, "World Records" );
		PORT_DIPSETTING(    0x10, "Don't Erase" );
		PORT_DIPSETTING(    0x00, "Erase on Reset" );
		PORT_DIPNAME( 0x60, 0x60, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x60, "Easy" );
		PORT_DIPSETTING(    0x40, "Normal" );
		PORT_DIPSETTING(    0x20, "Hard" );
		PORT_DIPSETTING(    0x00, "Difficult" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		768,	/* 768 characters */
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },	/* the four bitplanes are packed in one nibble */
		new int[] { 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8	/* every char takes 8 consecutive bytes */
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		256,	/* 256 sprites */
		4,	/* 4 bits per pixel */
		new int[] { 256*64*8+4, 256*64*8+0, 4, 0 },
		new int[] { 0, 1, 2, 3, 8*8+0, 8*8+1, 8*8+2, 8*8+3,
				16*8+0, 16*8+1, 16*8+2, 16*8+3, 24*8+0, 24*8+1, 24*8+2, 24*8+3 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				32*8, 33*8, 34*8, 35*8, 36*8, 37*8, 38*8, 39*8 },
		64*8	/* every sprite takes 64 consecutive bytes */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,       0, 16 ),
		new GfxDecodeInfo( REGION_GFX2, 0, spritelayout, 16*16, 16 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	/* filename for trackn field sample files */
	static String trackfld_sample_names[] =
	{
		"*trackfld",
		"00.wav","01.wav","02.wav","03.wav","04.wav","05.wav","06.wav","07.wav",
		"08.wav","09.wav","0a.wav","0b.wav","0c.wav","0d.wav","0e.wav","0f.wav",
		"10.wav","11.wav","12.wav","13.wav","14.wav","15.wav","16.wav","17.wav",
		"18.wav","19.wav","1a.wav","1b.wav","1c.wav","1d.wav","1e.wav","1f.wav",
		"20.wav","21.wav","22.wav","23.wav","24.wav","25.wav","26.wav","27.wav",
		"28.wav","29.wav","2a.wav","2b.wav","2c.wav","2d.wav","2e.wav","2f.wav",
		"30.wav","31.wav","32.wav","33.wav","34.wav","35.wav","36.wav","37.wav",
		"38.wav","39.wav","3a.wav","3b.wav","3c.wav","3d.wav",
		null
	};
	
	static VLM5030interface trackfld_vlm5030_interface = new VLM5030interface
	(
		3580000,    /* master clock  */
		255,        /* volume        */
		REGION_SOUND1,	/* memory region  */
		0,         /* memory size    */
		trackfld_sample_names
        );
	
	
	
	static MachineDriver machine_driver_tracklfd = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6809,
				2048000,        /* 1.400 Mhz ??? */
				readmem,writemem,null,null,
				interrupt,1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				14318180/4,	/* Z80 Clock is derived from a 14.31818 Mhz crystal */
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
		32,16*16+16*16,
		trackfld_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		trackfld_vh_start,
		trackfld_vh_stop,
		trackfld_vh_screenrefresh,
	
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
				trackfld_vlm5030_interface
			)
		},
	
		nvram_handler
	);
	
	/* same as the original, but uses ADPCM instead of VLM5030 */
	/* also different memory handlers do handle that */
	static MachineDriver machine_driver_hyprolyb = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6809,
				2048000,        /* 1.400 Mhz ??? */
				readmem,writemem,null,null,
				interrupt,1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				14318180/4,	/* Z80 Clock is derived from a 14.31818 Mhz crystal */
				hyprolyb_sound_readmem,hyprolyb_sound_writemem,null,null,
				ignore_interrupt,0	/* interrupts are triggered by the main CPU */
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		32,16*16+16*16,
		trackfld_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		trackfld_vh_start,
		trackfld_vh_stop,
		trackfld_vh_screenrefresh,
	
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
				SOUND_ADPCM,
				hyprolyb_adpcm_interface
			)
		},
	
		nvram_handler
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_trackfld = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2*0x10000, REGION_CPU1 );    /* 64k for code + 64k for decrypted opcodes */
		ROM_LOAD( "a01_e01.bin",  0x6000, 0x2000, 0x2882f6d4 );
		ROM_LOAD( "a02_e02.bin",  0x8000, 0x2000, 0x1743b5ee );
		ROM_LOAD( "a03_k03.bin",  0xA000, 0x2000, 0x6c0d1ee9 );
		ROM_LOAD( "a04_e04.bin",  0xC000, 0x2000, 0x21d6c448 );
		ROM_LOAD( "a05_e05.bin",  0xE000, 0x2000, 0xf08c7b7e );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "c2_d13.bin",   0x0000, 0x2000, 0x95bf79b6 );
	
		ROM_REGION( 0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "h16_e12.bin",  0x0000, 0x2000, 0x50075768 );
		ROM_LOAD( "h15_e11.bin",  0x2000, 0x2000, 0xdda9e29f );
		ROM_LOAD( "h14_e10.bin",  0x4000, 0x2000, 0xc2166a5c );
	
		ROM_REGION( 0x8000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "c11_d06.bin",  0x0000, 0x2000, 0x82e2185a );
		ROM_LOAD( "c12_d07.bin",  0x2000, 0x2000, 0x800ff1f1 );
		ROM_LOAD( "c13_d08.bin",  0x4000, 0x2000, 0xd9faf183 );
		ROM_LOAD( "c14_d09.bin",  0x6000, 0x2000, 0x5886c802 );
	
		ROM_REGION( 0x0220, REGION_PROMS );
		ROM_LOAD( "tfprom.1",     0x0000, 0x0020, 0xd55f30b5 );/* palette */
		ROM_LOAD( "tfprom.3",     0x0020, 0x0100, 0xd2ba4d32 );/* sprite lookup table */
		ROM_LOAD( "tfprom.2",     0x0120, 0x0100, 0x053e5861 );/* char lookup table */
	
		ROM_REGION( 0x10000, REGION_SOUND1 );/* 64k for speech rom */
		ROM_LOAD( "c9_d15.bin",   0x0000, 0x2000, 0xf546a56b );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_trackflc = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2*0x10000, REGION_CPU1 );    /* 64k for code + 64k for decrypted opcodes */
		ROM_LOAD( "f01.1a",       0x6000, 0x2000, 0x4e32b360 );
		ROM_LOAD( "f02.2a",       0x8000, 0x2000, 0x4e7ebf07 );
		ROM_LOAD( "l03.3a",       0xA000, 0x2000, 0xfef4c0ea );
		ROM_LOAD( "f04.4a",       0xC000, 0x2000, 0x73940f2d );
		ROM_LOAD( "f05.5a",       0xE000, 0x2000, 0x363fd761 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "c2_d13.bin",   0x0000, 0x2000, 0x95bf79b6 );
	
		ROM_REGION( 0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "h16_e12.bin",  0x0000, 0x2000, 0x50075768 );
		ROM_LOAD( "h15_e11.bin",  0x2000, 0x2000, 0xdda9e29f );
		ROM_LOAD( "h14_e10.bin",  0x4000, 0x2000, 0xc2166a5c );
	
		ROM_REGION( 0x8000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "c11_d06.bin",  0x0000, 0x2000, 0x82e2185a );
		ROM_LOAD( "c12_d07.bin",  0x2000, 0x2000, 0x800ff1f1 );
		ROM_LOAD( "c13_d08.bin",  0x4000, 0x2000, 0xd9faf183 );
		ROM_LOAD( "c14_d09.bin",  0x6000, 0x2000, 0x5886c802 );
	
		ROM_REGION( 0x0220, REGION_PROMS );
		ROM_LOAD( "tfprom.1",     0x0000, 0x0020, 0xd55f30b5 );/* palette */
		ROM_LOAD( "tfprom.3",     0x0020, 0x0100, 0xd2ba4d32 );/* sprite lookup table */
		ROM_LOAD( "tfprom.2",     0x0120, 0x0100, 0x053e5861 );/* char lookup table */
	
		ROM_REGION( 0x10000, REGION_SOUND1 );/* 64k for speech rom */
		ROM_LOAD( "c9_d15.bin",   0x0000, 0x2000, 0xf546a56b );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_hyprolym = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2*0x10000, REGION_CPU1 );    /* 64k for code + 64k for decrypted opcodes */
		ROM_LOAD( "hyprolym.a01", 0x6000, 0x2000, 0x82257fb7 );
		ROM_LOAD( "hyprolym.a02", 0x8000, 0x2000, 0x15b83099 );
		ROM_LOAD( "hyprolym.a03", 0xA000, 0x2000, 0xe54cc960 );
		ROM_LOAD( "hyprolym.a04", 0xC000, 0x2000, 0xd099b1e8 );
		ROM_LOAD( "hyprolym.a05", 0xE000, 0x2000, 0x974ff815 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for the audio CPU */
		ROM_LOAD( "c2_d13.bin",   0x0000, 0x2000, 0x95bf79b6 );
	
		ROM_REGION( 0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "hyprolym.h16", 0x0000, 0x2000, 0x768bb63d );
		ROM_LOAD( "hyprolym.h15", 0x2000, 0x2000, 0x3af0e2a8 );
		ROM_LOAD( "h14_e10.bin",  0x4000, 0x2000, 0xc2166a5c );
	
		ROM_REGION( 0x8000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "c11_d06.bin",  0x0000, 0x2000, 0x82e2185a );
		ROM_LOAD( "c12_d07.bin",  0x2000, 0x2000, 0x800ff1f1 );
		ROM_LOAD( "c13_d08.bin",  0x4000, 0x2000, 0xd9faf183 );
		ROM_LOAD( "c14_d09.bin",  0x6000, 0x2000, 0x5886c802 );
	
		ROM_REGION( 0x0220, REGION_PROMS );
		ROM_LOAD( "tfprom.1",     0x0000, 0x0020, 0xd55f30b5 );/* palette */
		ROM_LOAD( "tfprom.3",     0x0020, 0x0100, 0xd2ba4d32 );/* sprite lookup table */
		ROM_LOAD( "tfprom.2",     0x0120, 0x0100, 0x053e5861 );/* char lookup table */
	
		ROM_REGION( 0x10000, REGION_SOUND1 );/* 64k for speech rom */
		ROM_LOAD( "c9_d15.bin",   0x0000, 0x2000, 0xf546a56b );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_hyprolyb = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2*0x10000, REGION_CPU1 );    /* 64k for code + 64k for decrypted opcodes */
		ROM_LOAD( "a1.1",         0x6000, 0x2000, 0x9aee2d5a );
		ROM_LOAD( "hyprolym.a02", 0x8000, 0x2000, 0x15b83099 );
		ROM_LOAD( "a3.3",         0xA000, 0x2000, 0x2d6fc308 );
		ROM_LOAD( "hyprolym.a04", 0xC000, 0x2000, 0xd099b1e8 );
		ROM_LOAD( "hyprolym.a05", 0xE000, 0x2000, 0x974ff815 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for the audio CPU */
		ROM_LOAD( "c2_d13.bin",   0x0000, 0x2000, 0x95bf79b6 );
	
		ROM_REGION( 0x10000, REGION_CPU3 );/*  64k for the 6802 which plays ADPCM samples */
		/* this bootleg uses a 6802 to "emulate" the VLM5030 speech chip */
		/* I didn't bother to emulate the 6802, I just play the samples. */
		ROM_LOAD( "2764.1",       0x8000, 0x2000, 0xa4cddeb8 );
		ROM_LOAD( "2764.2",       0xa000, 0x2000, 0xe9919365 );
		ROM_LOAD( "2764.3",       0xc000, 0x2000, 0xc3ec42e1 );
		ROM_LOAD( "2764.4",       0xe000, 0x2000, 0x76998389 );
	
		ROM_REGION( 0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "hyprolym.h16", 0x0000, 0x2000, 0x768bb63d );
		ROM_LOAD( "hyprolym.h15", 0x2000, 0x2000, 0x3af0e2a8 );
		ROM_LOAD( "h14_e10.bin",  0x4000, 0x2000, 0xc2166a5c );
	
		ROM_REGION( 0x8000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "c11_d06.bin",  0x0000, 0x2000, 0x82e2185a );
		ROM_LOAD( "c12_d07.bin",  0x2000, 0x2000, 0x800ff1f1 );
		ROM_LOAD( "c13_d08.bin",  0x4000, 0x2000, 0xd9faf183 );
		ROM_LOAD( "c14_d09.bin",  0x6000, 0x2000, 0x5886c802 );
	
		ROM_REGION( 0x0220, REGION_PROMS );
		ROM_LOAD( "tfprom.1",     0x0000, 0x0020, 0xd55f30b5 );/* palette */
		ROM_LOAD( "tfprom.3",     0x0020, 0x0100, 0xd2ba4d32 );/* sprite lookup table */
		ROM_LOAD( "tfprom.2",     0x0120, 0x0100, 0x053e5861 );/* char lookup table */
	ROM_END(); }}; 
	
	
	public static InitDriverPtr init_trackfld = new InitDriverPtr() { public void handler() 
	{
		konami1_decode();
	} };
	
	
	public static GameDriver driver_trackfld	   = new GameDriver("1983"	,"trackfld"	,"trackfld.java"	,rom_trackfld,null	,machine_driver_tracklfd	,input_ports_trackfld	,init_trackfld	,ROT0	,	"Konami", "Track  Field" );
	public static GameDriver driver_trackflc	   = new GameDriver("1983"	,"trackflc"	,"trackfld.java"	,rom_trackflc,driver_trackfld	,machine_driver_tracklfd	,input_ports_trackfld	,init_trackfld	,ROT0	,	"Konami (Centuri license)", "Track  Field (Centuri)" );
	public static GameDriver driver_hyprolym	   = new GameDriver("1983"	,"hyprolym"	,"trackfld.java"	,rom_hyprolym,driver_trackfld	,machine_driver_tracklfd	,input_ports_trackfld	,init_trackfld	,ROT0	,	"Konami", "Hyper Olympic" );
	public static GameDriver driver_hyprolyb	   = new GameDriver("1983"	,"hyprolyb"	,"trackfld.java"	,rom_hyprolyb,driver_trackfld	,machine_driver_hyprolyb	,input_ports_trackfld	,init_trackfld	,ROT0	,	"bootleg", "Hyper Olympic (bootleg)" );
}
