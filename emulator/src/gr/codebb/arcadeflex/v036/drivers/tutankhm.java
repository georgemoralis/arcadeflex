/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package gr.codebb.arcadeflex.v036.drivers;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.v036.sndhrdw.timeplt.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.tutankhm.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.platform.ptrlib.*;
import static gr.codebb.arcadeflex.v036.mame.palette.*;

public class tutankhm
{	
	
	public static WriteHandlerPtr tutankhm_bankselect_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int bankaddress;
		UBytePtr RAM = memory_region(REGION_CPU1);
	
	
		bankaddress = 0x10000 + (data & 0x0f) * 0x1000;
		cpu_setbank(1,new UBytePtr(RAM,bankaddress));
	} };
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_RAM ),
		new MemoryReadAddress( 0x8120, 0x8120, watchdog_reset_r ),
		new MemoryReadAddress( 0x8160, 0x8160, input_port_0_r ),	/* DSW2 (inverted bits) */
		new MemoryReadAddress( 0x8180, 0x8180, input_port_1_r ),	/* IN0 I/O: Coin slots, service, 1P/2P buttons */
		new MemoryReadAddress( 0x81a0, 0x81a0, input_port_2_r ),	/* IN1: Player 1 I/O */
		new MemoryReadAddress( 0x81c0, 0x81c0, input_port_3_r ),	/* IN2: Player 2 I/O */
		new MemoryReadAddress( 0x81e0, 0x81e0, input_port_4_r ),	/* DSW1 (inverted bits) */
		new MemoryReadAddress( 0x8800, 0x8fff, MRA_RAM ),
		new MemoryReadAddress( 0x9000, 0x9fff, MRA_BANK1 ),
		new MemoryReadAddress( 0xa000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, tutankhm_videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x8000, 0x800f, paletteram_BBGGGRRR_w, paletteram ),
		new MemoryWriteAddress( 0x8100, 0x8100, MWA_RAM, tutankhm_scrollx ),
		new MemoryWriteAddress( 0x8200, 0x8200, interrupt_enable_w ),
		new MemoryWriteAddress( 0x8202, 0x8203, MWA_RAM ),	/* coin counters */
		new MemoryWriteAddress( 0x8205, 0x8205, MWA_NOP ),	/* ??? */
		new MemoryWriteAddress( 0x8206, 0x8207, tutankhm_flipscreen_w ),
		new MemoryWriteAddress( 0x8300, 0x8300, tutankhm_bankselect_w ),
		new MemoryWriteAddress( 0x8600, 0x8600, timeplt_sh_irqtrigger_w ),
		new MemoryWriteAddress( 0x8700, 0x8700, soundlatch_w ),
		new MemoryWriteAddress( 0x8800, 0x8fff, MWA_RAM ),
		new MemoryWriteAddress( 0xa000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 ) /* end of table */
	};
	
	
	static InputPortPtr input_ports_tutankhm = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* DSW2 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "3" );
		PORT_DIPSETTING(    0x01, "4" );
		PORT_DIPSETTING(    0x02, "5" );
		PORT_BITX( 0,       0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "256", IP_KEY_NONE, IP_JOY_NONE );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x08, "30000" );
		PORT_DIPSETTING(    0x00, "40000" );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x30, "Easy" );
		PORT_DIPSETTING(    0x10, "Normal" );
		PORT_DIPSETTING(    0x20, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x40, 0x40, "Flash Bomb" );
		PORT_DIPSETTING(    0x40, "1 per Life" );
		PORT_DIPSETTING(    0x00, "1 per Game" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
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
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* DSW1 */
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
	/* 0x00 not commented out since the game makes the usual sound if you insert the coin */
	INPUT_PORTS_END(); }}; 
	
	
	
	static MachineDriver machine_driver_tutankhm = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6809,
				1500000,			/* 1.5 Mhz ??? */
				readmem,writemem,null,null,
				interrupt,1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				14318180/8,	/* 1.789772727 MHz */						
				timeplt_sound_readmem,timeplt_sound_writemem,null,null,
				ignore_interrupt,1	/* interrupts are triggered by the main CPU */
			)
		},
		30, DEFAULT_30HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),	/* not sure about the visible area */
		null,					/* GfxDecodeInfo * */
		16, 0,
		null,
	
		VIDEO_TYPE_RASTER|VIDEO_MODIFIES_PALETTE,
		null,						/* vh_init routine */
		generic_vh_start,					/* vh_start routine */
		generic_vh_stop,					/* vh_stop routine */
		tutankhm_vh_screenrefresh,				/* vh_update routine */
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_AY8910,
				timeplt_ay8910_interface
			)
		}
	);
	
	
	static RomLoadPtr rom_tutankhm = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000, REGION_CPU1 );     /* 64k for M6809 CPU code + 64k for ROM banks */
		ROM_LOAD( "h1.bin",       0x0a000, 0x1000, 0xda18679f );/* program ROMs */
		ROM_LOAD( "h2.bin",       0x0b000, 0x1000, 0xa0f02c85 );
		ROM_LOAD( "h3.bin",       0x0c000, 0x1000, 0xea03a1ab );
		ROM_LOAD( "h4.bin",       0x0d000, 0x1000, 0xbd06fad0 );
		ROM_LOAD( "h5.bin",       0x0e000, 0x1000, 0xbf9fd9b0 );
		ROM_LOAD( "h6.bin",       0x0f000, 0x1000, 0xfe079c5b );
		ROM_LOAD( "j1.bin",       0x10000, 0x1000, 0x7eb59b21 );/* graphic ROMs (banked) -- only 9 of 12 are filled */
		ROM_LOAD( "j2.bin",       0x11000, 0x1000, 0x6615eff3 );
		ROM_LOAD( "j3.bin",       0x12000, 0x1000, 0xa10d4444 );
		ROM_LOAD( "j4.bin",       0x13000, 0x1000, 0x58cd143c );
		ROM_LOAD( "j5.bin",       0x14000, 0x1000, 0xd7e7ae95 );
		ROM_LOAD( "j6.bin",       0x15000, 0x1000, 0x91f62b82 );
		ROM_LOAD( "j7.bin",       0x16000, 0x1000, 0xafd0a81f );
		ROM_LOAD( "j8.bin",       0x17000, 0x1000, 0xdabb609b );
		ROM_LOAD( "j9.bin",       0x18000, 0x1000, 0x8ea9c6a6 );
		/* the other banks (1900-1fff) are empty */
	
		ROM_REGION(  0x10000 , REGION_CPU2 );/* 64k for Z80 sound CPU code */
		ROM_LOAD( "11-7a.bin",    0x0000, 0x1000, 0xb52d01fa );
		ROM_LOAD( "10-8a.bin",    0x1000, 0x1000, 0x9db5c0ce );
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_tutankst = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000, REGION_CPU1 );     /* 64k for M6809 CPU code + 64k for ROM banks */
		ROM_LOAD( "h1.bin",       0x0a000, 0x1000, 0xda18679f );/* program ROMs */
		ROM_LOAD( "h2.bin",       0x0b000, 0x1000, 0xa0f02c85 );
		ROM_LOAD( "ra1_3h.cpu",   0x0c000, 0x1000, 0x2d62d7b1 );
		ROM_LOAD( "h4.bin",       0x0d000, 0x1000, 0xbd06fad0 );
		ROM_LOAD( "h5.bin",       0x0e000, 0x1000, 0xbf9fd9b0 );
		ROM_LOAD( "ra1_6h.cpu",   0x0f000, 0x1000, 0xc43b3865 );
		ROM_LOAD( "j1.bin",       0x10000, 0x1000, 0x7eb59b21 );/* graphic ROMs (banked) -- only 9 of 12 are filled */
		ROM_LOAD( "j2.bin",       0x11000, 0x1000, 0x6615eff3 );
		ROM_LOAD( "j3.bin",       0x12000, 0x1000, 0xa10d4444 );
		ROM_LOAD( "j4.bin",       0x13000, 0x1000, 0x58cd143c );
		ROM_LOAD( "j5.bin",       0x14000, 0x1000, 0xd7e7ae95 );
		ROM_LOAD( "j6.bin",       0x15000, 0x1000, 0x91f62b82 );
		ROM_LOAD( "j7.bin",       0x16000, 0x1000, 0xafd0a81f );
		ROM_LOAD( "j8.bin",       0x17000, 0x1000, 0xdabb609b );
		ROM_LOAD( "j9.bin",       0x18000, 0x1000, 0x8ea9c6a6 );
		/* the other banks (1900-1fff) are empty */
	
		ROM_REGION(  0x10000 , REGION_CPU2 );/* 64k for Z80 sound CPU code */
		ROM_LOAD( "11-7a.bin",    0x0000, 0x1000, 0xb52d01fa );
		ROM_LOAD( "10-8a.bin",    0x1000, 0x1000, 0x9db5c0ce );
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_tutankhm	   = new GameDriver("1982"	,"tutankhm"	,"tutankhm.java"	,rom_tutankhm,null	,machine_driver_tutankhm	,input_ports_tutankhm	,null	,ROT90	,	"Konami", "Tutankham" );
	public static GameDriver driver_tutankst	   = new GameDriver("1982"	,"tutankst"	,"tutankhm.java"	,rom_tutankst,driver_tutankhm	,machine_driver_tutankhm	,input_ports_tutankhm	,null	,ROT90	,	"[Konami] (Stern license)", "Tutankham (Stern)" );
}
