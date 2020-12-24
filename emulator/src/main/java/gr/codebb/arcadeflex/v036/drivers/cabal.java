/******************************************************************
Cabal Bootleg
(c)1998 Red Corp

driver by Carlos A. Lozano Baides

68000 + Z80

The original uses 2xYM3931 for sound
The bootleg uses YM2151 + 2xZ80 used as ADPCM players

MEMORY MAP
0x000000 - 0x03ffff   ROM
0x040000 - 0x0437ff   RAM
0x043800 - 0x0437ff   VRAM (Sprites)
0x060000 - 0x0607ff   VRAM (Tiles)
0x080000 - 0x0803ff   VRAM (Background)
0x0A0000 - 0xA0000f   Input Ports
0x0C0040 - 0x0c0040   Watchdog??
0x0C0080 - 0x0C0080   Watchdog??
0x0E0000 - 0x0E07ff   COLORRAM (----BBBBGGGGRRRR)
0x0E8000 - 0x0E800f   Output Ports/Input Ports

VRAM(Background)
0x80000 - 32 bytes (16 tiles)
0x80040 - 32 bytes (16 tiles)
0x80080 - 32 bytes (16 tiles)
0x800c0 - 32 bytes (16 tiles)
0x80100 - 32 bytes (16 tiles)
...
0x803c0 - 32 bytes (16 tiles)

VRAM(Tiles)
0x60000-0x607ff (1024 tiles 8x8 tiles, 2 bytes every tile)

VRAM(Sprites)
0x43800-0x43bff (128 sprites, 8 bytes every sprite)

COLORRAM(Colors)
0xe0000-0xe07ff (1024 colors, ----BBBBGGGGRRRR)

******************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package gr.codebb.arcadeflex.v036.drivers;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.cabal.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.sound._2151intf.*;
import static gr.codebb.arcadeflex.v036.sound._2151intfH.*;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;
import static gr.codebb.arcadeflex.v036.sound.adpcmH.*;
import static gr.codebb.arcadeflex.v036.sound.adpcm.*;
import static gr.codebb.arcadeflex.v036.cpu.z80.z80H.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;

public class cabal
{
	
	static int cabal_sound_command1, cabal_sound_command2;
	
	
	public static InitMachinePtr cabal_init_machine = new InitMachinePtr() { public void handler()  {
		cabal_sound_command1 = cabal_sound_command2 = 0xff;
	} };
	
	public static ReadHandlerPtr cabal_background_r = new ReadHandlerPtr() { public int handler(int offset){
		return videoram.READ_WORD(offset);
	} };
	
	public static WriteHandlerPtr cabal_background_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		int oldword = videoram.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword,data);
		if( oldword != newword ){
			videoram.WRITE_WORD(offset,newword);
			dirtybuffer[offset/2] = 1;
		}
	} };
	
	/******************************************************************************************/
	
	
	static YM2151interface ym2151_interface = new YM2151interface
	(
	 	1,			/* 1 chip */
	 	3579580,	/* 3.58 MHZ ? */ /* 4 MHZ in raine */
		new int[]{ YM3012_VOL(80,MIXER_PAN_LEFT,80,MIXER_PAN_RIGHT) },
	 	new WriteYmHandlerPtr[]{ null }
                );
	
	static ADPCMinterface adpcm_interface = new ADPCMinterface(
	
		2,			/* total number of ADPCM decoders in the machine */
		8000,		/* playback frequency */
		REGION_SOUND1, /* memory region where the samples come from */
		null,			/* initialization function */
		new int[]{40,40}
                );
	
	public static WriteHandlerPtr cabal_play_adpcm = new WriteHandlerPtr() { public void handler(int channel, int which){
		if( which!=0xff ){
			UBytePtr RAM = memory_region(REGION_SOUND1);
			int offset = channel*0x10000;
			int start, len;
	
			which = which&0x7f;
			if( which!=0 ){
				which = which*2+0x100;
				start = RAM.read(offset+which) + 256*RAM.read(offset+which+1);
				len = (RAM.read(offset+start)*256 + RAM.read(offset+start+1))*2;
				start+=2;
				ADPCM_play( channel,offset+start,len );
			}
		}
	} };
	static int coin = 0;
	public static ReadHandlerPtr cabal_coin_r = new ReadHandlerPtr() { public int handler(int offset){
		
		int val = readinputport( 3 );
	
		if ( ( val & 0x04 )==0 ){
			if ( coin == 0 ){
				coin = 1;
				return val;
			}
		} else {
			coin = 0;
		}
	
	 	return val | 0x04;
	} };
	
	public static ReadHandlerPtr cabal_io_r = new ReadHandlerPtr() { public int handler(int offset){
		// if( errorlog ) fprintf( errorlog, "INPUT a000[%02x] \n", offset);
		switch (offset){
			case 0x0: return readinputport(4) + (readinputport(5)<<8); /* DIPSW */
			case 0x8: return 0xff + (readinputport(0)<<8);/* IN0 */
			case 0x10: return readinputport(1) + (readinputport(2)<<8); /* IN1,IN2 */
			default: return (0xffff);
		}
	} };
	
	public static WriteHandlerPtr cabal_snd_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		switch (offset) {
			case 0x0:
				cabal_sound_command1 = data;
				cpu_cause_interrupt( 1, Z80_NMI_INT );
			break;
	
			case 0x2: /* ?? */
				cabal_sound_command2 = data & 0xff;
			break;
	
			case 0x8: /* ?? */
			break;
		}
	} };
	
	static MemoryReadAddress readmem_cpu[] ={
		new MemoryReadAddress( 0x00000, 0x3ffff, MRA_ROM ),
		new MemoryReadAddress( 0x40000, 0x437ff, MRA_RAM ),
		new MemoryReadAddress( 0x43c00, 0x4ffff, MRA_RAM ),
		new MemoryReadAddress( 0x43800, 0x43bff, MRA_RAM ),
		new MemoryReadAddress( 0x60000, 0x607ff, MRA_BANK1 ),  /* text layer */
		new MemoryReadAddress( 0x80000, 0x801ff, cabal_background_r ), /* background layer */
		new MemoryReadAddress( 0x80200, 0x803ff, MRA_BANK2 ),
		new MemoryReadAddress( 0xa0000, 0xa0012, cabal_io_r ),
		new MemoryReadAddress( 0xe0000, 0xe07ff, paletteram_word_r ),
		new MemoryReadAddress( 0xe8000, 0xe800f, cabal_coin_r ),
		new MemoryReadAddress( -1 )
	};
	static MemoryWriteAddress writemem_cpu[] ={
		new MemoryWriteAddress( 0x00000, 0x3ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x40000, 0x437ff, MWA_RAM ),
		new MemoryWriteAddress( 0x43800, 0x43bff, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x43c00, 0x4ffff, MWA_RAM ),
		new MemoryWriteAddress( 0x60000, 0x607ff, MWA_BANK1, colorram ),
		new MemoryWriteAddress( 0x80000, 0x801ff, cabal_background_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x80200, 0x803ff, MWA_BANK2 ),
		new MemoryWriteAddress( 0xc0040, 0xc0041, MWA_NOP ), /* ??? */
		new MemoryWriteAddress( 0xc0080, 0xc0081, MWA_NOP ), /* ??? */
		new MemoryWriteAddress( 0xe0000, 0xe07ff, paletteram_xxxxBBBBGGGGRRRR_word_w, paletteram),
		new MemoryWriteAddress( 0xe8000, 0xe800f, cabal_snd_w ),
		new MemoryWriteAddress( -1 )
	};
	
	/*********************************************************************/
	
	public static ReadHandlerPtr cabal_snd_read = new ReadHandlerPtr() { public int handler(int offset){
		switch(offset){
			case 0x08: return cabal_sound_command2;
			case 0x0a: return cabal_sound_command1;
			default: return(0xff);
		}
	} };
	
	public static WriteHandlerPtr cabal_snd_write = new WriteHandlerPtr() { public void handler(int offset, int data){
		switch( offset ){
			case 0x00: cabal_play_adpcm.handler(0, data ); break;
			case 0x02: cabal_play_adpcm.handler(1, data ); break;
	     }
	} };
	
	static MemoryReadAddress readmem_sound[] =
	{
		new MemoryReadAddress( 0x0000, 0x1fff, MRA_ROM ),
		new MemoryReadAddress( 0x2000, 0x2fff, MRA_RAM ),
		new MemoryReadAddress( 0x4000, 0x400d, cabal_snd_read ),
		new MemoryReadAddress( 0x400f, 0x400f, YM2151_status_port_0_r ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress writemem_sound[] =
	{
		new MemoryWriteAddress( 0x0000, 0x1fff, MWA_ROM ),
		new MemoryWriteAddress( 0x2000, 0x2fff, MWA_RAM ),
		new MemoryWriteAddress( 0x4000, 0x400d, cabal_snd_write ),
		new MemoryWriteAddress( 0x400e, 0x400e, YM2151_register_port_0_w ),
		new MemoryWriteAddress( 0x400f, 0x400f, YM2151_data_port_0_w ),
		new MemoryWriteAddress( 0x6000, 0x6000, MWA_NOP ),  /*???*/
		new MemoryWriteAddress( 0x8000, 0xffff, MWA_ROM ),
	 	new MemoryWriteAddress( -1 )
	};
	
	static MemoryReadAddress cabalbl_readmem_sound[] =
	{
		new MemoryReadAddress( 0x0000, 0x1fff, MRA_ROM ),
		new MemoryReadAddress( 0x2000, 0x2fff, MRA_RAM ),
		new MemoryReadAddress( 0x4000, 0x400d, cabal_snd_read ),
		new MemoryReadAddress( 0x400f, 0x400f, YM2151_status_port_0_r ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress cabalbl_writemem_sound[] =
	{
		new MemoryWriteAddress( 0x0000, 0x1fff, MWA_ROM ),
		new MemoryWriteAddress( 0x2000, 0x2fff, MWA_RAM ),
		new MemoryWriteAddress( 0x4000, 0x400d, cabal_snd_write ),
		new MemoryWriteAddress( 0x400e, 0x400e, YM2151_register_port_0_w ),
		new MemoryWriteAddress( 0x400f, 0x400f, YM2151_data_port_0_w ),
		new MemoryWriteAddress( 0x6000, 0x6000, MWA_NOP ),  /*???*/
		new MemoryWriteAddress( 0x8000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )
	};
	
	/* ADPCM CPU (common) */
	
	/*#if 0
	static MemoryReadAddress cabalbl_readmem_adpcm[] ={
		new MemoryReadAddress( 0x0000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )
	};
	static MemoryWriteAddress cabalbl_writemem_adpcm[] ={
		new MemoryWriteAddress( 0x0000, 0xffff, MWA_NOP ),
		new MemoryWriteAddress( -1 )
	};
	#endif*/
	
	
	/***************************************************************************/
	
	static InputPortPtr input_ports_cabal = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY);
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY);
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY);
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x0f, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2);
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON3 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START(); 	/* IN3 */
		PORT_BIT( 0x03, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0xf8, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DIPSW1 */
		PORT_DIPNAME( 0x0f, 0x0e, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x0b, "3 Coins/1 Credit 5/2" );
		PORT_DIPSETTING(    0x0c, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x0d, "2 Coins/1 Credit 3/2" );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x03, "2 Coins/2 Credits 3/4" );
		PORT_DIPSETTING(    0x02, DEF_STR( "3C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_8C") );
		PORT_DIPSETTING(    0x06, "1 Coin/10 Credits" );
		PORT_DIPSETTING(    0x05, "1 Coin/12 Credits" );
		PORT_DIPSETTING(    0x00, "Free 99 Credits" );
	/* 0x10 is different from the Free 99 Credits.
	   When you start the game the credits decrease using the Free 99,
	   while they stay at 99 using the 0x10 dip. */
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Free_Play") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, "Invert Buttons" );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "Trackball" );
		PORT_DIPSETTING(    0x80, "Small" );
		PORT_DIPSETTING(    0x00, "Large" );
	
		PORT_START(); 	/* DIPSW2 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x02, "2" );
		PORT_DIPSETTING(    0x03, "3" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_BITX( 0,       0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite", IP_KEY_NONE, IP_JOY_NONE );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x0c, "20k 50k" );
		PORT_DIPSETTING(    0x08, "30k 100k" );
		PORT_DIPSETTING(    0x04, "50k 150k" );
		PORT_DIPSETTING(    0x00, "70K" );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x30, "Easy" );
		PORT_DIPSETTING(    0x20, "Normal" );
		PORT_DIPSETTING(    0x10, "Hard" );
		PORT_DIPSETTING(    0x00, "Very Hard" );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout text_layout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		1024,	/* 1024 characters */
		2,	/* 4 bits per pixel */
		new int[] { 0,4 },//0x4000*8+0, 0x4000*8+4, 0, 4 ),
		new int[]{ 3, 2, 1, 0, 8+3, 8+2, 8+1, 8+0},
		new int[]{ 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
		16*8	/* every char takes 16 consecutive bytes */
        );
	
	static GfxLayout tile_layout = new GfxLayout
	(
		16,16,  /* 16*16 tiles */
		4*1024,   /* 1024 tiles */
		4,      /* 4 bits per pixel */
		new int[] { 0, 4, 0x40000*8+0, 0x40000*8+4 },
		new int[] { 3, 2, 1, 0, 8+3, 8+2, 8+1, 8+0,
				32*8+3, 32*8+2, 32*8+1, 32*8+0, 33*8+3, 33*8+2, 33*8+1, 33*8+0 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16,
				8*16, 9*16, 10*16, 11*16, 12*16, 13*16, 14*16, 15*16 },
		64*8
	);
	
	static GfxLayout sprite_layout = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		4*1024,	/* 1024 sprites */
		4,	/* 4 bits per pixel */
		new int[] { 0, 4, 0x40000*8+0, 0x40000*8+4 },	/* the two bitplanes for 4 pixels are packed into one byte */
		new int[] { 3, 2, 1, 0, 8+3, 8+2, 8+1, 8+0,
				16+3, 16+2, 16+1, 16+0, 24+3, 24+2, 24+1, 24+0 },
		new int[] { 30*16, 28*16, 26*16, 24*16, 22*16, 20*16, 18*16, 16*16,
				14*16, 12*16, 10*16,  8*16,  6*16,  4*16,  2*16,  0*16 },
		64*8	/* every sprite takes 64 consecutive bytes */
	);
	
	static GfxDecodeInfo cabal_gfxdecodeinfo[] ={
		new GfxDecodeInfo( REGION_GFX1, 0, text_layout,		0, 1024/4 ),
		new GfxDecodeInfo( REGION_GFX2, 0, tile_layout,		32*16, 16 ),
		new GfxDecodeInfo( REGION_GFX3, 0, sprite_layout,	16*16, 16 ),
		new GfxDecodeInfo( -1 )
	};
	
	static MachineDriver machine_driver_cabal = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				12000000, /* 12 Mhz */
				readmem_cpu,writemem_cpu,null,null,
				m68_level1_irq,1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				4000000,	/* 4 Mhz */
				readmem_sound,writemem_sound,null,null,
				interrupt,1
			),
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
		1, /* CPU slices per frame */
		cabal_init_machine, /* init machine */
	
		/* video hardware */
		256, 256, new rectangle( 0, 255, 16, 255-16 ),
	
		cabal_gfxdecodeinfo,
		1024,1024,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		generic_vh_start,
		generic_vh_stop,
		cabal_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,null
	);
	
	static MachineDriver machine_driver_cabalbl = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				12000000, /* 12 Mhz */
				readmem_cpu,writemem_cpu,null,null,
				m68_level1_irq,1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				4000000,	/* 4 Mhz */
				cabalbl_readmem_sound,cabalbl_writemem_sound,null,null,
				interrupt,1
			),
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
		10, /* CPU slices per frame */
		cabal_init_machine, /* init machine */
	
		/* video hardware */
		256, 256, new rectangle( 0, 255, 16, 255-16 ),
	
		cabal_gfxdecodeinfo,
		1024,1024,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		generic_vh_start,
		generic_vh_stop,
		cabal_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,//_ALT,
				ym2151_interface
			),
			new MachineSound(
				SOUND_ADPCM,
				adpcm_interface
			)
		}
	);
	
	static RomLoadPtr rom_cabal = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x50000, REGION_CPU1 );/* 64k for cpu code */
		ROM_LOAD_EVEN( "h7_512.bin",      0x00000, 0x10000, 0x8fe16fb4 );
		ROM_LOAD_ODD ( "h6_512.bin",      0x00000, 0x10000, 0x6968101c );
		ROM_LOAD_EVEN( "k7_512.bin",      0x20000, 0x10000, 0x562031a2 );
		ROM_LOAD_ODD ( "k6_512.bin",      0x20000, 0x10000, 0x4fda2856 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for sound cpu code */
		ROM_LOAD( "4-3n",           0x0000, 0x2000, 0x4038eff2 );
		ROM_LOAD( "3-3p",           0x8000, 0x8000, 0xd9defcbf );
	
		ROM_REGION( 0x4000,  REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "t6_128.bin",     0x00000, 0x04000, 0x1ccee214 );/* characters */
	
		/* the gfx ROMs were missing in this set, I'm using the ones from */
		/* the bootleg */
		ROM_REGION( 0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cabal_17.bin",   0x00000, 0x10000, 0x3b6d2b09 );/* tiles, planes0,1 */
		ROM_LOAD( "cabal_16.bin",   0x10000, 0x10000, 0x77bc7a60 );
		ROM_LOAD( "cabal_18.bin",   0x20000, 0x10000, 0x0bc50075 );
		ROM_LOAD( "cabal_19.bin",   0x30000, 0x10000, 0x67e4fe47 );
		ROM_LOAD( "cabal_15.bin",   0x40000, 0x10000, 0x1023319b );/* tiles, planes2,3 */
		ROM_LOAD( "cabal_14.bin",   0x50000, 0x10000, 0x420b0801 );
		ROM_LOAD( "cabal_12.bin",   0x60000, 0x10000, 0x543fcb37 );
		ROM_LOAD( "cabal_13.bin",   0x70000, 0x10000, 0xd28d921e );
	
		ROM_REGION( 0x80000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cabal_05.bin",   0x00000, 0x10000, 0x4e49c28e );/* sprites, planes0,1 */
		ROM_LOAD( "cabal_06.bin",   0x10000, 0x10000, 0x6a0e739d );
		ROM_LOAD( "cabal_07.bin",   0x20000, 0x10000, 0x581a50c1 );
		ROM_LOAD( "cabal_08.bin",   0x30000, 0x10000, 0x702735c9 );
		ROM_LOAD( "cabal_04.bin",   0x40000, 0x10000, 0x34d3cac8 );/* sprites, planes2,3 */
		ROM_LOAD( "cabal_03.bin",   0x50000, 0x10000, 0x7065e840 );
		ROM_LOAD( "cabal_02.bin",   0x60000, 0x10000, 0x0e1ec30e );
		ROM_LOAD( "cabal_01.bin",   0x70000, 0x10000, 0x55c44764 );
	
		ROM_REGION( 0x20000, REGION_SOUND1 );/* Samples? */
		ROM_LOAD( "2-1s",           0x00000, 0x10000, 0x850406b4 );
		ROM_LOAD( "1-1u",           0x10000, 0x10000, 0x8b3e0789 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_cabal2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x50000, REGION_CPU1 );/* 64k for cpu code */
		ROM_LOAD_EVEN( "9-7h",            0x00000, 0x10000, 0xebbb9484 );
		ROM_LOAD_ODD ( "7-6h",            0x00000, 0x10000, 0x51aeb49e );
		ROM_LOAD_EVEN( "8-7k",            0x20000, 0x10000, 0x4c24ed9a );
		ROM_LOAD_ODD ( "6-6k",            0x20000, 0x10000, 0x681620e8 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for sound cpu code */
		ROM_LOAD( "4-3n",           0x0000, 0x2000, 0x4038eff2 );
		ROM_LOAD( "3-3p",           0x8000, 0x8000, 0xd9defcbf );
	
		ROM_REGION( 0x4000,  REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "5-6s",           0x00000, 0x04000, 0x6a76955a );/* characters */
	
		/* the gfx ROMs were missing in this set, I'm using the ones from */
		/* the bootleg */
		ROM_REGION( 0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cabal_17.bin",   0x00000, 0x10000, 0x3b6d2b09 );/* tiles, planes0,1 */
		ROM_LOAD( "cabal_16.bin",   0x10000, 0x10000, 0x77bc7a60 );
		ROM_LOAD( "cabal_18.bin",   0x20000, 0x10000, 0x0bc50075 );
		ROM_LOAD( "cabal_19.bin",   0x30000, 0x10000, 0x67e4fe47 );
		ROM_LOAD( "cabal_15.bin",   0x40000, 0x10000, 0x1023319b );/* tiles, planes2,3 */
		ROM_LOAD( "cabal_14.bin",   0x50000, 0x10000, 0x420b0801 );
		ROM_LOAD( "cabal_12.bin",   0x60000, 0x10000, 0x543fcb37 );
		ROM_LOAD( "cabal_13.bin",   0x70000, 0x10000, 0xd28d921e );
	
		ROM_REGION( 0x80000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cabal_05.bin",   0x00000, 0x10000, 0x4e49c28e );/* sprites, planes0,1 */
		ROM_LOAD( "cabal_06.bin",   0x10000, 0x10000, 0x6a0e739d );
		ROM_LOAD( "cabal_07.bin",   0x20000, 0x10000, 0x581a50c1 );
		ROM_LOAD( "cabal_08.bin",   0x30000, 0x10000, 0x702735c9 );
		ROM_LOAD( "cabal_04.bin",   0x40000, 0x10000, 0x34d3cac8 );/* sprites, planes2,3 */
		ROM_LOAD( "cabal_03.bin",   0x50000, 0x10000, 0x7065e840 );
		ROM_LOAD( "cabal_02.bin",   0x60000, 0x10000, 0x0e1ec30e );
		ROM_LOAD( "cabal_01.bin",   0x70000, 0x10000, 0x55c44764 );
	
		ROM_REGION( 0x20000, REGION_SOUND1 );/* Samples? */
		ROM_LOAD( "2-1s",           0x00000, 0x10000, 0x850406b4 );
		ROM_LOAD( "1-1u",           0x10000, 0x10000, 0x8b3e0789 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_cabalbl = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x50000, REGION_CPU1 );/* 64k for cpu code */
		ROM_LOAD_EVEN( "cabal_24.bin",    0x00000, 0x10000, 0x00abbe0c );
		ROM_LOAD_ODD ( "cabal_22.bin",    0x00000, 0x10000, 0x78c4af27 );
		ROM_LOAD_EVEN( "cabal_23.bin",    0x20000, 0x10000, 0xd763a47c );
		ROM_LOAD_ODD ( "cabal_21.bin",    0x20000, 0x10000, 0x96d5e8af );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for sound cpu code */
		ROM_LOAD( "cabal_11.bin",    0x0000, 0x10000, 0xd308a543 );
	
		ROM_REGION( 0x4000,  REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "5-6s",           0x00000, 0x04000, 0x6a76955a );/* characters */
	
		ROM_REGION( 0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cabal_17.bin",   0x00000, 0x10000, 0x3b6d2b09 );/* tiles, planes0,1 */
		ROM_LOAD( "cabal_16.bin",   0x10000, 0x10000, 0x77bc7a60 );
		ROM_LOAD( "cabal_18.bin",   0x20000, 0x10000, 0x0bc50075 );
		ROM_LOAD( "cabal_19.bin",   0x30000, 0x10000, 0x67e4fe47 );
		ROM_LOAD( "cabal_15.bin",   0x40000, 0x10000, 0x1023319b );/* tiles, planes2,3 */
		ROM_LOAD( "cabal_14.bin",   0x50000, 0x10000, 0x420b0801 );
		ROM_LOAD( "cabal_12.bin",   0x60000, 0x10000, 0x543fcb37 );
		ROM_LOAD( "cabal_13.bin",   0x70000, 0x10000, 0xd28d921e );
	
		ROM_REGION( 0x80000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cabal_05.bin",   0x00000, 0x10000, 0x4e49c28e );/* sprites, planes0,1 */
		ROM_LOAD( "cabal_06.bin",   0x10000, 0x10000, 0x6a0e739d );
		ROM_LOAD( "cabal_07.bin",   0x20000, 0x10000, 0x581a50c1 );
		ROM_LOAD( "cabal_08.bin",   0x30000, 0x10000, 0x702735c9 );
		ROM_LOAD( "cabal_04.bin",   0x40000, 0x10000, 0x34d3cac8 );/* sprites, planes2,3 */
		ROM_LOAD( "cabal_03.bin",   0x50000, 0x10000, 0x7065e840 );
		ROM_LOAD( "cabal_02.bin",   0x60000, 0x10000, 0x0e1ec30e );
		ROM_LOAD( "cabal_01.bin",   0x70000, 0x10000, 0x55c44764 );
	
		ROM_REGION( 0x20000, REGION_SOUND1 );
		ROM_LOAD( "cabal_09.bin",   0x00000, 0x10000, 0x4ffa7fe3 );/* Z80 code/adpcm data */
		ROM_LOAD( "cabal_10.bin",   0x10000, 0x10000, 0x958789b6 );/* Z80 code/adpcm data */
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_cabal	   = new GameDriver("1988"	,"cabal"	,"cabal.java"	,rom_cabal,null	,machine_driver_cabal	,input_ports_cabal	,null	,ROT0	,	"Tad (Fabtek license)", "Cabal (US set 1)", GAME_NOT_WORKING | GAME_IMPERFECT_SOUND | GAME_NO_COCKTAIL );
	public static GameDriver driver_cabal2	   = new GameDriver("1988"	,"cabal2"	,"cabal.java"	,rom_cabal2,driver_cabal	,machine_driver_cabal	,input_ports_cabal	,null	,ROT0	,	"Tad (Fabtek license)", "Cabal (US set 2)", GAME_NOT_WORKING | GAME_IMPERFECT_SOUND | GAME_NO_COCKTAIL );
	public static GameDriver driver_cabalbl	   = new GameDriver("1988"	,"cabalbl"	,"cabal.java"	,rom_cabalbl,driver_cabal	,machine_driver_cabalbl	,input_ports_cabal	,null	,ROT0	,	"bootleg", "Cabal (bootleg)", GAME_IMPERFECT_SOUND | GAME_NO_COCKTAIL );
}
