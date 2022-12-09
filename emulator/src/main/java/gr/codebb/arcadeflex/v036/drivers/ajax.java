/***************************************************************************

"AJAX/Typhoon"	(Konami GX770)

Driver by:
	Manuel Abadia <manu@teleline.es>

TO DO:
- Find the CPU core bug, that makes the 052001 to read from 0x0000

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package gr.codebb.arcadeflex.v036.drivers;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
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
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.memory.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.ajax.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konamiic.*;
import static gr.codebb.arcadeflex.v036.platform.fileio.*;
import arcadeflex.v036.mame.sndintrfH.MachineSound;
import static arcadeflex.v036.mame.sndintrfH.*;
import static arcadeflex.v036.sound.k007232.*;
import static arcadeflex.v036.sound.k007232H.*;
import static arcadeflex.v036.sound._2151intf.*;
import static arcadeflex.v036.sound._2151intfH.*;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;
import static gr.codebb.arcadeflex.v036.machine.ajax.*;

public class ajax
{
	
	/****************************************************************************/
	static YM2151interface ym2151_interface = new YM2151interface
	(
		1,			/* 1 chip */
		3579545,	/* 3.58 MHz */
		new int[]{ YM3012_VOL(50,MIXER_PAN_LEFT,50,MIXER_PAN_RIGHT) },
                new WriteYmHandlerPtr[]{ null },
		new WriteHandlerPtr[]{ null }
                
        );
	/*	sound_bank_w:
		Handled by the LS273 Octal +ve edge trigger D-type Flip-flop with Reset at B11:
	
		Bit	Description
		---	-----------
		7	CONT1 (???) \
		6	CONT2 (???) / One or both bits are set to 1 when you kill a enemy
		5	\
		3	/ 4MBANKH
		4	\
		2	/ 4MBANKL
		1	\
		0	/ 2MBANK
	*/
	
	public static WriteHandlerPtr sound_bank_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		UBytePtr RAM;
		int bank_A, bank_B;
	
		/* banks # for the 007232 (chip 1) */
		RAM = memory_region(REGION_SOUND1);
		bank_A = 0x20000 * ((data >> 1) & 0x01);
		bank_B = 0x20000 * ((data >> 0) & 0x01);
		K007232_bankswitch(0,new UBytePtr(RAM,bank_A),new UBytePtr(RAM,bank_B));
	
		/* banks # for the 007232 (chip 2) */
		RAM = memory_region(REGION_SOUND2);
		bank_A = 0x20000 * ((data >> 4) & 0x03);
		bank_B = 0x20000 * ((data >> 2) & 0x03);
		K007232_bankswitch(1,new UBytePtr(RAM,bank_A),new UBytePtr(RAM,bank_B));
	} };
	
	public static portwritehandlerPtr volume_callback0 = new portwritehandlerPtr() { public void handler(int v)
        {
		K007232_set_volume(0,0,(v >> 4) * 0x11,0);
		K007232_set_volume(0,1,0,(v & 0x0f) * 0x11);
	}};
	
	public static WriteHandlerPtr k007232_extvol_w = new WriteHandlerPtr() { public void handler(int offset, int v)
	{
		/* channel A volume (mono) */
		K007232_set_volume(1,0,(v & 0x0f) * 0x11/2,(v & 0x0f) * 0x11/2);
	} };
	
	public static portwritehandlerPtr volume_callback1 = new portwritehandlerPtr() { public void handler(int v)
        {
		/* channel B volume/pan */
		K007232_set_volume(1,1,(v & 0x0f) * 0x11/2,(v >> 4) * 0x11/2);
	}};
	
	static K007232_interface k007232_interface = new K007232_interface
	(
		2,			/* number of chips */
		new int[]{ REGION_SOUND1, REGION_SOUND2 },	/* memory regions */
		new int[]{ K007232_VOL(20,MIXER_PAN_CENTER,20,MIXER_PAN_CENTER),
			K007232_VOL(50,MIXER_PAN_LEFT,50,MIXER_PAN_RIGHT) },/* volume */
		new portwritehandlerPtr[]{ volume_callback0,  volume_callback1 }	/* external port callback */
        );
        
	static MemoryReadAddress ajax_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x01c0, ajax_ls138_f10_r ),			/* inputs + DIPSW */
		new MemoryReadAddress( 0x0800, 0x0807, K051937_r ),					/* sprite control registers */
		new MemoryReadAddress( 0x0c00, 0x0fff, K051960_r ),					/* sprite RAM 2128SL at J7 */
		new MemoryReadAddress( 0x1000, 0x1fff, MRA_RAM ),		/* palette */
		new MemoryReadAddress( 0x2000, 0x3fff, ajax_sharedram_r ),			/* shared RAM with the 6809 */
		new MemoryReadAddress( 0x4000, 0x5fff, MRA_RAM ),					/* RAM 6264L at K10*/
		new MemoryReadAddress( 0x6000, 0x7fff, MRA_BANK2 ),					/* banked ROM */
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),					/* ROM N11 */
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress ajax_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x01c0, ajax_ls138_f10_w ),			/* bankswitch + sound command + FIRQ command */
		new MemoryWriteAddress( 0x0800, 0x0807, K051937_w ),					/* sprite control registers */
		new MemoryWriteAddress( 0x0c00, 0x0fff, K051960_w ),					/* sprite RAM 2128SL at J7 */
		new MemoryWriteAddress( 0x1000, 0x1fff, paletteram_xBBBBBGGGGGRRRRR_swap_w, paletteram ),/* palette */
		new MemoryWriteAddress( 0x2000, 0x3fff, ajax_sharedram_w ),			/* shared RAM with the 6809 */
		new MemoryWriteAddress( 0x4000, 0x5fff, MWA_RAM ),					/* RAM 6264L at K10 */
		new MemoryWriteAddress( 0x6000, 0x7fff, MWA_ROM ),					/* banked ROM */
		new MemoryWriteAddress( 0x8000, 0xffff, MWA_ROM ),					/* ROM N11 */
		new MemoryWriteAddress( -1 )
	};
	
	static MemoryReadAddress ajax_readmem_2[] =
	{
		new MemoryReadAddress( 0x0000, 0x07ff, K051316_0_r ),		/* 051316 zoom/rotation layer */
		new MemoryReadAddress( 0x1000, 0x17ff, K051316_rom_0_r ),	/* 051316 (ROM test) */
		new MemoryReadAddress( 0x2000, 0x3fff, ajax_sharedram_r ),	/* shared RAM with the 052001 */
		new MemoryReadAddress( 0x4000, 0x7fff, K052109_r ),			/* video RAM + color RAM + video registers */
		new MemoryReadAddress( 0x8000, 0x9fff, MRA_BANK1 ),			/* banked ROM */
		new MemoryReadAddress( 0xa000, 0xffff, MRA_ROM ),			/* ROM I16 */
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress ajax_writemem_2[] =
	{
		new MemoryWriteAddress( 0x0000, 0x07ff, K051316_0_w ),			/* 051316 zoom/rotation layer */
		new MemoryWriteAddress( 0x0800, 0x080f, K051316_ctrl_0_w ),		/* 051316 control registers */
		new MemoryWriteAddress( 0x1800, 0x1800, ajax_bankswitch_w_2 ),	/* bankswitch control */
		new MemoryWriteAddress( 0x2000, 0x3fff, ajax_sharedram_w, ajax_sharedram ),/* shared RAM with the 052001 */
		new MemoryWriteAddress( 0x4000, 0x7fff, K052109_w ),				/* video RAM + color RAM + video registers */
		new MemoryWriteAddress( 0x8000, 0x9fff, MWA_ROM ),				/* banked ROM */
		new MemoryWriteAddress( 0xa000, 0xffff, MWA_ROM ),				/* ROM I16 */
		new MemoryWriteAddress( -1 )
	};
	
	static MemoryReadAddress ajax_readmem_sound[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),				/* ROM F6 */
		new MemoryReadAddress( 0x8000, 0x87ff, MRA_RAM ),				/* RAM 2128SL at D16 */
		new MemoryReadAddress( 0xa000, 0xa00d, K007232_read_port_0_r ),	/* 007232 registers (chip 1) */
		new MemoryReadAddress( 0xb000, 0xb00d, K007232_read_port_1_r ),	/* 007232 registers (chip 2) */
		new MemoryReadAddress( 0xc001, 0xc001, YM2151_status_port_0_r ),	/* YM2151 */
		new MemoryReadAddress( 0xe000, 0xe000, soundlatch_r ),			/* soundlatch_r */
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress ajax_writemem_sound[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),					/* ROM F6 */
		new MemoryWriteAddress( 0x8000, 0x87ff, MWA_RAM ),					/* RAM 2128SL at D16 */
		new MemoryWriteAddress( 0x9000, 0x9000, sound_bank_w ),				/* 007232 bankswitch */
		new MemoryWriteAddress( 0xa000, 0xa00d, K007232_write_port_0_w ),		/* 007232 registers (chip 1) */
		new MemoryWriteAddress( 0xb000, 0xb00d, K007232_write_port_1_w ),		/* 007232 registers (chip 2) */
		new MemoryWriteAddress( 0xb80c, 0xb80c, k007232_extvol_w ),	/* extra volume, goes to the 007232 w/ A11 */
												/* selecting a different latch for the external port */
		new MemoryWriteAddress( 0xc000, 0xc000, YM2151_register_port_0_w ),	/* YM2151 */
		new MemoryWriteAddress( 0xc001, 0xc001, YM2151_data_port_0_w ),		/* YM2151 */
		new MemoryWriteAddress( -1 )
	};
	
	
	static InputPortPtr input_ports_ajax = new InputPortPtr(){ public void handler() { 
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
	//	PORT_DIPSETTING(    0x00, "Coin Slot 2 Invalidity" );
	
		PORT_START(); 	/* DSW #2 */
		PORT_DIPNAME( 0x03, 0x02, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x03, "2" );
		PORT_DIPSETTING(	0x02, "3" );
		PORT_DIPSETTING(	0x01, "5" );
		PORT_DIPSETTING(	0x00, "7" );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(	0x04, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x18, 0x10, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(	0x18, "30000 150000" );
		PORT_DIPSETTING(	0x10, "50000 200000" );
		PORT_DIPSETTING(	0x08, "30000" );
		PORT_DIPSETTING(	0x00, "50000" );
		PORT_DIPNAME( 0x60, 0x40, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(	0x60, "Easy" );
		PORT_DIPSETTING(	0x40, "Normal" );
		PORT_DIPSETTING(	0x20, "Difficult" );
		PORT_DIPSETTING(	0x00, "Very difficult" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(	0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* DSW #3 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(	0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "Upright Controls" );
		PORT_DIPSETTING(    0x02, "Single" );
		PORT_DIPSETTING(    0x00, "Dual" );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x08, 0x08, "Control in 3D Stages" );
		PORT_DIPSETTING(	0x08, "Normal" );
		PORT_DIPSETTING(	0x00, "Inverted" );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* COINSW & START */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );/* service */
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* PLAYER 1 INPUTS */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* PLAYER 2 INPUTS */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	

	
	
	
	static MachineDriver machine_driver_ajax = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_KONAMI,	/* Konami Custom 052001 */
				3000000,	/* 12/4 MHz*/
				ajax_readmem,ajax_writemem,null,null,
				ajax_interrupt,1	/* IRQs triggered by the 051960 */
			),
			new MachineCPU(
				CPU_M6809,	/* 6809E */
				3000000,	/* ? */
				ajax_readmem_2, ajax_writemem_2,null,null,
				ignore_interrupt,1	/* FIRQs triggered by the 052001 */
			),
			new MachineCPU(
				CPU_Z80,	/* Z80A */
				3579545,	/* 3.58 MHz */
				ajax_readmem_sound, ajax_writemem_sound,null,null,
				ignore_interrupt,0	/* IRQs triggered by the 052001 */
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,
		10,
		ajax_init_machine,
	
		/* video hardware */
		64*8, 32*8, new rectangle( 14*8, (64-14)*8-1, 2*8, 30*8-1 ),
		null,	/* gfx decoded by konamiic.c */
		2048, 2048,
		null,
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		ajax_vh_start,
		ajax_vh_stop,
		ajax_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,//SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			),
			new MachineSound(
				SOUND_K007232,
				k007232_interface
			)
		}
	);
	
	
	
	static RomLoadPtr rom_ajax = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x28000, REGION_CPU1 );/* 052001 code */
		ROM_LOAD( "m01.n11",	0x10000, 0x08000, 0x4a64e53a );/* banked ROM */
		ROM_CONTINUE(			0x08000, 0x08000 );			/* fixed ROM */
		ROM_LOAD( "l02.n12",	0x18000, 0x10000, 0xad7d592b );/* banked ROM */
	
		ROM_REGION( 0x22000, REGION_CPU2 );/* 64k + 72k for banked ROMs */
		ROM_LOAD( "l05.i16",	0x20000, 0x02000, 0xed64fbb2 );/* banked ROM */
		ROM_CONTINUE(			0x0a000, 0x06000 );			/* fixed ROM */
		ROM_LOAD( "f04.g16",	0x10000, 0x10000, 0xe0e4ec9c );/* banked ROM */
	
		ROM_REGION( 0x10000, REGION_CPU3 );/* 64k for the SOUND CPU */
		ROM_LOAD( "h03.f16",	0x00000, 0x08000, 0x2ffd2afc );
	
	    ROM_REGION( 0x080000, REGION_GFX1 );/* graphics (addressable by the main CPU) */
		ROM_LOAD( "770c13",		0x000000, 0x040000, 0xb859ca4e );/* characters (N22) */
		ROM_LOAD( "770c12",		0x040000, 0x040000, 0x50d14b72 );/* characters (K22) */
	
	    ROM_REGION( 0x100000, REGION_GFX2 );/* graphics (addressable by the main CPU) */
		ROM_LOAD( "770c09",		0x000000, 0x080000, 0x1ab4a7ff );/* sprites (N4) */
		ROM_LOAD( "770c08",		0x080000, 0x080000, 0xa8e80586 );/* sprites (K4) */
	
		ROM_REGION( 0x080000, REGION_GFX3 );/* graphics (addressable by the main CPU) */
		ROM_LOAD( "770c06",		0x000000, 0x040000, 0xd0c592ee );/* zoom/rotate (F4) */
		ROM_LOAD( "770c07",		0x040000, 0x040000, 0x0b399fb1 );/* zoom/rotate (H4) */
	
		ROM_REGION( 0x0200, REGION_PROMS );
		ROM_LOAD( "63s241.j11",	0x0000, 0x0200, 0x9bdd719f );/* priority encoder (not used) */
	
		ROM_REGION( 0x040000, REGION_SOUND1 );/* 007232 data (chip 1) */
		ROM_LOAD( "770c10",		0x000000, 0x040000, 0x7fac825f );
	
		ROM_REGION( 0x080000, REGION_SOUND2 );/* 007232 data (chip 2) */
		ROM_LOAD( "770c11",		0x000000, 0x080000, 0x299a615a );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_ajaxj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x28000, REGION_CPU1 );/* 052001 code */
		ROM_LOAD( "770l01.bin",	0x10000, 0x08000, 0x7cea5274 );/* banked ROM */
		ROM_CONTINUE(			0x08000, 0x08000 );			/* fixed ROM */
		ROM_LOAD( "l02.n12",	0x18000, 0x10000, 0xad7d592b );/* banked ROM */
	
		ROM_REGION( 0x22000, REGION_CPU2 );/* 64k + 72k for banked ROMs */
		ROM_LOAD( "l05.i16",	0x20000, 0x02000, 0xed64fbb2 );/* banked ROM */
		ROM_CONTINUE(			0x0a000, 0x06000 );			/* fixed ROM */
		ROM_LOAD( "f04.g16",	0x10000, 0x10000, 0xe0e4ec9c );/* banked ROM */
	
		ROM_REGION( 0x10000, REGION_CPU3 );/* 64k for the SOUND CPU */
		ROM_LOAD( "770f03.bin",	0x00000, 0x08000, 0x3fe914fd );
	
	    ROM_REGION( 0x080000, REGION_GFX1 );/* graphics (addressable by the main CPU) */
		ROM_LOAD( "770c13",		0x000000, 0x040000, 0xb859ca4e );/* characters (N22) */
		ROM_LOAD( "770c12",		0x040000, 0x040000, 0x50d14b72 );/* characters (K22) */
	
	    ROM_REGION( 0x100000, REGION_GFX2 );/* graphics (addressable by the main CPU) */
		ROM_LOAD( "770c09",		0x000000, 0x080000, 0x1ab4a7ff );/* sprites (N4) */
		ROM_LOAD( "770c08",		0x080000, 0x080000, 0xa8e80586 );/* sprites (K4) */
	
		ROM_REGION( 0x080000, REGION_GFX3 );/* graphics (addressable by the main CPU) */
		ROM_LOAD( "770c06",		0x000000, 0x040000, 0xd0c592ee );/* zoom/rotate (F4) */
		ROM_LOAD( "770c07",		0x040000, 0x040000, 0x0b399fb1 );/* zoom/rotate (H4) */
	
		ROM_REGION( 0x0200, REGION_PROMS );
		ROM_LOAD( "63s241.j11",	0x0000, 0x0200, 0x9bdd719f );/* priority encoder (not used) */
	
		ROM_REGION( 0x040000, REGION_SOUND1 );/* 007232 data (chip 1) */
		ROM_LOAD( "770c10",		0x000000, 0x040000, 0x7fac825f );
	
		ROM_REGION( 0x080000, REGION_SOUND2 );/* 007232 data (chip 2) */
		ROM_LOAD( "770c11",		0x000000, 0x080000, 0x299a615a );
	ROM_END(); }}; 
	
	
	public static InitDriverPtr init_ajax = new InitDriverPtr() { public void handler() 
	{
		konami_rom_deinterleave_2(REGION_GFX1);
		konami_rom_deinterleave_2(REGION_GFX2);
	} };
	
	
	
	public static GameDriver driver_ajax	   = new GameDriver("1987"	,"ajax"	,"ajax.java"	,rom_ajax,null	,machine_driver_ajax	,input_ports_ajax	,init_ajax	,ROT90	,	"Konami", "Ajax" );
	public static GameDriver driver_ajaxj	   = new GameDriver("1987"	,"ajaxj"	,"ajax.java"	,rom_ajaxj,driver_ajax	,machine_driver_ajax	,input_ports_ajax	,init_ajax	,ROT90	,	"Konami", "Ajax (Japan)" );
}
