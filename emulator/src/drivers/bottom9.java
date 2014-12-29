/***************************************************************************

Bottom of the Ninth (c) 1989 Konami

Similar to S.P.Y.

driver by Nicola Salmoria

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;
import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.commonH.*;
import static mame.inputport.*;
import static mame.drawgfxH.*;
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
import static arcadeflex.ptrlib.*;
import static mame.palette.*;
import static mame.memory.*;
import static vidhrdw.bottom9.*;
import static vidhrdw.konamiic.*;
import static arcadeflex.fileio.*;
import mame.sndintrfH.MachineSound;
import static mame.sndintrfH.SOUND_K007232;
import static sound.k007232.*;
import static sound.k007232H.*;
import static sound.mixerH.*;


public class bottom9
{

	
	public static InterruptPtr bottom9_interrupt = new InterruptPtr() { public int handler() 
	{
		if (K052109_is_IRQ_enabled()!=0) return interrupt.handler();
		else return ignore_interrupt.handler();
	} };
	
	
	static int zoomreadroms,K052109_selected;
	
	public static ReadHandlerPtr bottom9_bankedram1_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		if (K052109_selected!=0)
			return K052109_051960_r.handler(offset);
		else
		{
			if (zoomreadroms!=0)
				return K051316_rom_0_r.handler(offset);
			else
				return K051316_0_r.handler(offset);
		}
	} };
	
	public static WriteHandlerPtr bottom9_bankedram1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (K052109_selected!=0) K052109_051960_w.handler(offset,data);
		else K051316_0_w.handler(offset,data);
	} };
	
	public static ReadHandlerPtr bottom9_bankedram2_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		if (K052109_selected!=0) return K052109_051960_r.handler(offset + 0x2000);
		else return paletteram_r.handler(offset);
	} };
	
	public static WriteHandlerPtr bottom9_bankedram2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (K052109_selected!=0) K052109_051960_w.handler(offset + 0x2000,data);
		else paletteram_xBBBBBGGGGGRRRRR_swap_w.handler(offset,data);
	} };
	
	public static WriteHandlerPtr bankswitch_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
		int offs;
	
		/* bit 0 = RAM bank */
                //if ((data & 1) == 0) usrintf_showmessage("bankswitch RAM bank 0");
	
		/* bit 1-4 = ROM bank */
		if ((data & 0x10)!=0) offs = 0x20000 + (data & 0x06) * 0x1000;
		else offs = 0x10000 + (data & 0x0e) * 0x1000;
		cpu_setbank(1,new UBytePtr(RAM,offs));
	} };
	
	public static WriteHandlerPtr bottom9_1f90_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* bits 0/1 = coin counters */
		coin_counter_w.handler(0,data & 0x01);
		coin_counter_w.handler(1,data & 0x02);
	
		/* bit 2 = enable char ROM reading through the video RAM */
		K052109_set_RMRD_line((data & 0x04)!=0 ? ASSERT_LINE : CLEAR_LINE);
	
		/* bit 3 = disable video */
		bottom9_video_enable = ~data & 0x08;
	
		/* bit 4 = enable 051316 ROM reading */
		zoomreadroms = data & 0x10;
	
		/* bit 5 = RAM bank */
		K052109_selected = data & 0x20;
	} };
	
	public static WriteHandlerPtr bottom9_sh_irqtrigger_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		cpu_cause_interrupt(1,0xff);
	} };
	
	static int nmienable;
	
	public static InterruptPtr bottom9_sound_interrupt = new InterruptPtr() { public int handler() 
	{
		if (nmienable!=0) return nmi_interrupt.handler();
		else return ignore_interrupt.handler();
	} };
	
	public static WriteHandlerPtr nmi_enable_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		nmienable = data;
	} };
	
	public static WriteHandlerPtr sound_bank_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		UBytePtr RAM;
		int bank_A,bank_B;
	
		RAM = memory_region(REGION_SOUND1);
		bank_A = 0x20000 * ((data >> 0) & 0x03);
		bank_B = 0x20000 * ((data >> 2) & 0x03);
		K007232_bankswitch(0,new UBytePtr(RAM,bank_A),new UBytePtr(RAM,bank_B));
		RAM = memory_region(REGION_SOUND2);
		bank_A = 0x20000 * ((data >> 4) & 0x03);
		bank_B = 0x20000 * ((data >> 6) & 0x03);
		K007232_bankswitch(1,new UBytePtr(RAM,bank_A),new UBytePtr(RAM,bank_B));
	} };
	
	
	
	static MemoryReadAddress bottom9_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x07ff, bottom9_bankedram1_r ),
		new MemoryReadAddress( 0x1fd0, 0x1fd0, input_port_4_r ),
		new MemoryReadAddress( 0x1fd1, 0x1fd1, input_port_0_r ),
		new MemoryReadAddress( 0x1fd2, 0x1fd2, input_port_1_r ),
		new MemoryReadAddress( 0x1fd3, 0x1fd3, input_port_2_r ),
		new MemoryReadAddress( 0x1fe0, 0x1fe0, input_port_3_r ),
		new MemoryReadAddress( 0x2000, 0x27ff, bottom9_bankedram2_r ),
		new MemoryReadAddress( 0x0000, 0x3fff, K052109_051960_r ),
		new MemoryReadAddress( 0x4000, 0x5fff, MRA_RAM ),
		new MemoryReadAddress( 0x6000, 0x7fff, MRA_BANK1 ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress bottom9_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x07ff, bottom9_bankedram1_w ),
		new MemoryWriteAddress( 0x1f80, 0x1f80, bankswitch_w ),
		new MemoryWriteAddress( 0x1f90, 0x1f90, bottom9_1f90_w ),
		new MemoryWriteAddress( 0x1fa0, 0x1fa0, watchdog_reset_w ),
		new MemoryWriteAddress( 0x1fb0, 0x1fb0, soundlatch_w ),
		new MemoryWriteAddress( 0x1fc0, 0x1fc0, bottom9_sh_irqtrigger_w ),
		new MemoryWriteAddress( 0x1ff0, 0x1fff, K051316_ctrl_0_w ),
		new MemoryWriteAddress( 0x2000, 0x27ff, bottom9_bankedram2_w, paletteram ),
		new MemoryWriteAddress( 0x0000, 0x3fff, K052109_051960_w ),
		new MemoryWriteAddress( 0x4000, 0x5fff, MWA_RAM ),
		new MemoryWriteAddress( 0x6000, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0x8000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress bottom9_sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0x87ff, MRA_RAM ),
		new MemoryReadAddress( 0xa000, 0xa00d, K007232_read_port_0_r ),
		new MemoryReadAddress( 0xb000, 0xb00d, K007232_read_port_1_r ),
		new MemoryReadAddress( 0xd000, 0xd000, soundlatch_r ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress bottom9_sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0x8000, 0x87ff, MWA_RAM ),
		new MemoryWriteAddress( 0x9000, 0x9000, sound_bank_w ),
		new MemoryWriteAddress( 0xa000, 0xa00d, K007232_write_port_0_w ),
		new MemoryWriteAddress( 0xb000, 0xb00d, K007232_write_port_1_w ),
		new MemoryWriteAddress( 0xf000, 0xf000, nmi_enable_w ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	
	static InputPortPtr input_ports_bottom9 = new InputPortPtr(){ public void handler() { 
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_5C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") );
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_5C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") );
	
		PORT_START(); 
		PORT_DIPNAME( 0x07, 0x04, "Play Time" );
		PORT_DIPSETTING(    0x07, "1'00" );
		PORT_DIPSETTING(    0x06, "1'10" );
		PORT_DIPSETTING(    0x05, "1'20" );
		PORT_DIPSETTING(    0x04, "1'30" );
		PORT_DIPSETTING(    0x03, "1'40" );
		PORT_DIPSETTING(    0x02, "1'50" );
		PORT_DIPSETTING(    0x01, "2'00" );
		PORT_DIPSETTING(    0x00, "2'10" );
		PORT_DIPNAME( 0x18, 0x08, "Bonus Time" );
		PORT_DIPSETTING(    0x18, "00" );
		PORT_DIPSETTING(    0x10, "20" );
		PORT_DIPSETTING(    0x08, "30" );
		PORT_DIPSETTING(    0x00, "40" );
		PORT_DIPNAME( 0x60, 0x40, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(	0x60, "Easy" );
		PORT_DIPSETTING(	0x40, "Normal" );
		PORT_DIPSETTING(	0x20, "Difficult" );
		PORT_DIPSETTING(	0x00, "Very Difficult" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(	0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
	
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x40, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x80, 0x80, "Fielder Control" );
		PORT_DIPSETTING(    0x80, "Normal" );
		PORT_DIPSETTING(    0x00, "Auto" );
	INPUT_PORTS_END(); }}; 
	
	
	public static portwritehandlerPtr volume_callback0 = new portwritehandlerPtr() { public void handler(int v)
        {
            K007232_set_volume(0,0,(v >> 4) * 0x11,0);
            K007232_set_volume(0,1,0,(v & 0x0f) * 0x11);
        }};

	public static portwritehandlerPtr volume_callback1 = new portwritehandlerPtr() { public void handler(int v)
        {
            K007232_set_volume(1,0,(v >> 4) * 0x11,0);
	    K007232_set_volume(1,1,0,(v & 0x0f) * 0x11);
        }};

	static K007232_interface k007232_interface = new K007232_interface
	(
		2,			/* number of chips */
		new int[]{ REGION_SOUND1, REGION_SOUND2 },	/* memory regions */
		new int[]{ K007232_VOL(40,MIXER_PAN_CENTER,40,MIXER_PAN_CENTER),
				K007232_VOL(40,MIXER_PAN_CENTER,40,MIXER_PAN_CENTER) },	/* volume */
		new portwritehandlerPtr[]{ volume_callback0, volume_callback1 }	/* external port callback */
        );
	
	
	
	static MachineDriver machine_driver_bottom9 = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6809,
				2000000, /* ? */
				bottom9_readmem,bottom9_writemem,null,null,
				bottom9_interrupt,1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				3579545,
				bottom9_sound_readmem, bottom9_sound_writemem,null,null,
				bottom9_sound_interrupt,8	/* irq is triggered by the main CPU */
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		64*8, 32*8, new rectangle( 14*8, (64-14)*8-1, 2*8, 30*8-1 ),
		null,	/* gfx decoded by konamiic.c */
		1024, 1024,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		bottom9_vh_start,
		bottom9_vh_stop,
		bottom9_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_K007232,
				k007232_interface
			)
		}
	);
	
	
	/***************************************************************************
	
	  Game ROMs
	
	***************************************************************************/
	
	static RomLoadPtr rom_bottom9 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x28000, REGION_CPU1 );/* code + banked roms */
		ROM_LOAD( "891n03.k17",   0x10000, 0x10000, 0x8b083ff3 );
	    ROM_LOAD( "891-t02.k15",  0x20000, 0x08000, 0x2c10ced2 );
	    ROM_CONTINUE(             0x08000, 0x08000 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* Z80 code */
		ROM_LOAD( "891j01.g8",    0x0000, 0x8000, 0x31b0a0a8 );
	
		ROM_REGION( 0x080000, REGION_GFX1 );/* graphics ( dont dispose as the program can read them ) */
		ROM_LOAD_GFX_EVEN( "891e10c", 0x00000, 0x10000, 0x209b0431 );/* characters */
		ROM_LOAD_GFX_ODD ( "891e10a", 0x00000, 0x10000, 0x8020a9e8 );
		ROM_LOAD_GFX_EVEN( "891e10d", 0x20000, 0x10000, 0x16d5fd7a );
		ROM_LOAD_GFX_ODD ( "891e10b", 0x20000, 0x10000, 0x30121cc0 );
		ROM_LOAD_GFX_EVEN( "891e09c", 0x40000, 0x10000, 0x9dcaefbf );
		ROM_LOAD_GFX_ODD ( "891e09a", 0x40000, 0x10000, 0x56b0ead9 );
		ROM_LOAD_GFX_EVEN( "891e09d", 0x60000, 0x10000, 0x4e1335e6 );
		ROM_LOAD_GFX_ODD ( "891e09b", 0x60000, 0x10000, 0xb6f914fb );
	
		ROM_REGION( 0x100000, REGION_GFX2 );/* graphics ( dont dispose as the program can read them ) */
		ROM_LOAD_GFX_EVEN( "891e06e", 0x00000, 0x10000, 0x0b04db1c );/* sprites */
		ROM_LOAD_GFX_ODD ( "891e06a", 0x00000, 0x10000, 0x5ee37327 );
		ROM_LOAD_GFX_EVEN( "891e06f", 0x20000, 0x10000, 0xf9ada524 );
		ROM_LOAD_GFX_ODD ( "891e06b", 0x20000, 0x10000, 0x2295dfaa );
		ROM_LOAD_GFX_EVEN( "891e06g", 0x40000, 0x10000, 0x04abf78f );
		ROM_LOAD_GFX_ODD ( "891e06c", 0x40000, 0x10000, 0xdbdb0d55 );
		ROM_LOAD_GFX_EVEN( "891e06h", 0x60000, 0x10000, 0x5d5ded8c );
		ROM_LOAD_GFX_ODD ( "891e06d", 0x60000, 0x10000, 0xf9ecbd71 );
		ROM_LOAD_GFX_EVEN( "891e05e", 0x80000, 0x10000, 0xb356e729 );
		ROM_LOAD_GFX_ODD ( "891e05a", 0x80000, 0x10000, 0xbfd5487e );
		ROM_LOAD_GFX_EVEN( "891e05f", 0xa0000, 0x10000, 0xecdd11c5 );
		ROM_LOAD_GFX_ODD ( "891e05b", 0xa0000, 0x10000, 0xaba18d24 );
		ROM_LOAD_GFX_EVEN( "891e05g", 0xc0000, 0x10000, 0xc315f9ae );
		ROM_LOAD_GFX_ODD ( "891e05c", 0xc0000, 0x10000, 0x21fcbc6f );
		ROM_LOAD_GFX_EVEN( "891e05h", 0xe0000, 0x10000, 0xb0aba53b );
		ROM_LOAD_GFX_ODD ( "891e05d", 0xe0000, 0x10000, 0xf6d3f886 );
	
		ROM_REGION( 0x020000, REGION_GFX3 );/* graphics ( dont dispose as the program can read them ) */
		ROM_LOAD( "891e07a",      0x00000, 0x10000, 0xb8d8b939 );/* zoom/rotate */
		ROM_LOAD( "891e07b",      0x10000, 0x10000, 0x83b2f92d );
	
		ROM_REGION( 0x0200, REGION_PROMS );
		ROM_LOAD( "891b11.f23",   0x0000, 0x0100, 0xecb854aa );/* priority encoder (not used) */
	
		ROM_REGION( 0x40000, REGION_SOUND1 );/* samples for 007232 #0 */
		ROM_LOAD( "891e08a",      0x00000, 0x10000, 0xcef667bf );
		ROM_LOAD( "891e08b",      0x10000, 0x10000, 0xf7c14a7a );
		ROM_LOAD( "891e08c",      0x20000, 0x10000, 0x756b7f3c );
		ROM_LOAD( "891e08d",      0x30000, 0x10000, 0xcd0d7305 );
	
		ROM_REGION( 0x40000, REGION_SOUND2 );/* samples for 007232 #1 */
		ROM_LOAD( "891e04a",      0x00000, 0x10000, 0xdaebbc74 );
		ROM_LOAD( "891e04b",      0x10000, 0x10000, 0x5ffb9ad1 );
		ROM_LOAD( "891e04c",      0x20000, 0x10000, 0x2dbbf16b );
		ROM_LOAD( "891e04d",      0x30000, 0x10000, 0x8b0cd2cc );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_bottom9n = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x28000, REGION_CPU1 );/* code + banked roms */
		ROM_LOAD( "891n03.k17",   0x10000, 0x10000, 0x8b083ff3 );
	    ROM_LOAD( "891n02.k15",   0x20000, 0x08000, 0xd44d9ed4 );
	    ROM_CONTINUE(             0x08000, 0x08000 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* Z80 code */
		ROM_LOAD( "891j01.g8",    0x0000, 0x8000, 0x31b0a0a8 );
	
		ROM_REGION( 0x080000, REGION_GFX1 );/* graphics ( dont dispose as the program can read them ) */
		ROM_LOAD_GFX_EVEN( "891e10c", 0x00000, 0x10000, 0x209b0431 );/* characters */
		ROM_LOAD_GFX_ODD ( "891e10a", 0x00000, 0x10000, 0x8020a9e8 );
		ROM_LOAD_GFX_EVEN( "891e10d", 0x20000, 0x10000, 0x16d5fd7a );
		ROM_LOAD_GFX_ODD ( "891e10b", 0x20000, 0x10000, 0x30121cc0 );
		ROM_LOAD_GFX_EVEN( "891e09c", 0x40000, 0x10000, 0x9dcaefbf );
		ROM_LOAD_GFX_ODD ( "891e09a", 0x40000, 0x10000, 0x56b0ead9 );
		ROM_LOAD_GFX_EVEN( "891e09d", 0x60000, 0x10000, 0x4e1335e6 );
		ROM_LOAD_GFX_ODD ( "891e09b", 0x60000, 0x10000, 0xb6f914fb );
	
		ROM_REGION( 0x100000, REGION_GFX2 );/* graphics ( dont dispose as the program can read them ) */
		ROM_LOAD_GFX_EVEN( "891e06e", 0x00000, 0x10000, 0x0b04db1c );/* sprites */
		ROM_LOAD_GFX_ODD ( "891e06a", 0x00000, 0x10000, 0x5ee37327 );
		ROM_LOAD_GFX_EVEN( "891e06f", 0x20000, 0x10000, 0xf9ada524 );
		ROM_LOAD_GFX_ODD ( "891e06b", 0x20000, 0x10000, 0x2295dfaa );
		ROM_LOAD_GFX_EVEN( "891e06g", 0x40000, 0x10000, 0x04abf78f );
		ROM_LOAD_GFX_ODD ( "891e06c", 0x40000, 0x10000, 0xdbdb0d55 );
		ROM_LOAD_GFX_EVEN( "891e06h", 0x60000, 0x10000, 0x5d5ded8c );
		ROM_LOAD_GFX_ODD ( "891e06d", 0x60000, 0x10000, 0xf9ecbd71 );
		ROM_LOAD_GFX_EVEN( "891e05e", 0x80000, 0x10000, 0xb356e729 );
		ROM_LOAD_GFX_ODD ( "891e05a", 0x80000, 0x10000, 0xbfd5487e );
		ROM_LOAD_GFX_EVEN( "891e05f", 0xa0000, 0x10000, 0xecdd11c5 );
		ROM_LOAD_GFX_ODD ( "891e05b", 0xa0000, 0x10000, 0xaba18d24 );
		ROM_LOAD_GFX_EVEN( "891e05g", 0xc0000, 0x10000, 0xc315f9ae );
		ROM_LOAD_GFX_ODD ( "891e05c", 0xc0000, 0x10000, 0x21fcbc6f );
		ROM_LOAD_GFX_EVEN( "891e05h", 0xe0000, 0x10000, 0xb0aba53b );
		ROM_LOAD_GFX_ODD ( "891e05d", 0xe0000, 0x10000, 0xf6d3f886 );
	
		ROM_REGION( 0x020000, REGION_GFX3 );/* graphics ( dont dispose as the program can read them ) */
		ROM_LOAD( "891e07a",      0x00000, 0x10000, 0xb8d8b939 );/* zoom/rotate */
		ROM_LOAD( "891e07b",      0x10000, 0x10000, 0x83b2f92d );
	
		ROM_REGION( 0x0200, REGION_PROMS );
		ROM_LOAD( "891b11.f23",   0x0000, 0x0100, 0xecb854aa );/* priority encoder (not used) */
	
		ROM_REGION( 0x40000, REGION_SOUND1 );/* samples for 007232 #0 */
		ROM_LOAD( "891e08a",      0x00000, 0x10000, 0xcef667bf );
		ROM_LOAD( "891e08b",      0x10000, 0x10000, 0xf7c14a7a );
		ROM_LOAD( "891e08c",      0x20000, 0x10000, 0x756b7f3c );
		ROM_LOAD( "891e08d",      0x30000, 0x10000, 0xcd0d7305 );
	
		ROM_REGION( 0x40000, REGION_SOUND2 );/* samples for 007232 #1 */
		ROM_LOAD( "891e04a",      0x00000, 0x10000, 0xdaebbc74 );
		ROM_LOAD( "891e04b",      0x10000, 0x10000, 0x5ffb9ad1 );
		ROM_LOAD( "891e04c",      0x20000, 0x10000, 0x2dbbf16b );
		ROM_LOAD( "891e04d",      0x30000, 0x10000, 0x8b0cd2cc );
	ROM_END(); }}; 
	
	
	
	public static InitDriverPtr init_bottom9 = new InitDriverPtr() { public void handler() 
	{
		konami_rom_deinterleave_2(REGION_GFX1);
		konami_rom_deinterleave_2(REGION_GFX2);
	} };
	
	
	
	public static GameDriver driver_bottom9	   = new GameDriver("1989"	,"bottom9"	,"bottom9.java"	,rom_bottom9,null	,machine_driver_bottom9	,input_ports_bottom9	,init_bottom9	,ROT0	,	"Konami", "Bottom of the Ninth (version T)" );
	public static GameDriver driver_bottom9n	   = new GameDriver("1989"	,"bottom9n"	,"bottom9.java"	,rom_bottom9n,driver_bottom9	,machine_driver_bottom9	,input_ports_bottom9	,init_bottom9	,ROT0	,	"Konami", "Bottom of the Ninth (version N)" );
}
