/***************************************************************************

X-Men

driver by Nicola Salmoria

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
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_r;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_w;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.*;
import static gr.codebb.arcadeflex.v037b7.cpu.z80.z80H.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.memory.*;
import arcadeflex.v036.mame.sndintrfH.MachineSound;
import static arcadeflex.v036.mame.sndintrfH.SOUND_K007232;
import static arcadeflex.v036.mame.sndintrfH.SOUND_YM2151;
import static gr.codebb.arcadeflex.v036.vidhrdw.konamiic.*;
import static arcadeflex.v036.sound.k007232.*;
import static arcadeflex.v036.sound.k007232H.*;
import static arcadeflex.v036.sound._2151intf.*;
import static arcadeflex.v036.sound._2151intfH.*;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konami.K053247.*;
import static arcadeflex.v036.mame.timer.*;
import static arcadeflex.v036.mame.timerH.*;
import static gr.codebb.arcadeflex.v036.mame.inputH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.xmen.*;
import static gr.codebb.arcadeflex.v036.cpu.konami.konamiH.*;
import static gr.codebb.arcadeflex.v036.machine.simpsons.*;
import static arcadeflex.v036.mame.sndintrfH.SOUND_K053260;
import static arcadeflex.v036.sound.k053260.*;
import static arcadeflex.v036.sound.k053260H.*;
import static gr.codebb.arcadeflex.v036.machine.eeprom.*;
import static gr.codebb.arcadeflex.v036.machine.eepromH.*;



public class xmen
{
	
	public static ReadHandlerPtr K052109_halfword_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return K052109_r.handler(offset >>> 1);
	} };
	
	public static WriteHandlerPtr K052109_halfword_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
			K052109_w.handler(offset >>> 1,data & 0xff);
	} };
	
	public static WriteHandlerPtr K053251_halfword_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
			K053251_w.handler(offset >>> 1,data & 0xff);
	} };
	
	
	
	/***************************************************************************
	
	  EEPROM
	
	***************************************************************************/
	
	static int init_eeprom_count;
	
	
	static EEPROM_interface eeprom_interface = new EEPROM_interface
	(
		7,				/* address bits */
		8,				/* data bits */
		"011000",		/*  read command */
		"011100",		/* write command */
		null,				/* erase command */
		"0100000000000",/* lock command */
		"0100110000000" /* unlock command */
        );
	
	public static nvramPtr nvram_handler = new nvramPtr() {
        public void handler(Object file, int read_or_write) {
		if (read_or_write!=0)
			EEPROM_save(file);
		else
		{
			EEPROM_init(eeprom_interface);
	
			if (file!=null)
			{
				init_eeprom_count = 0;
				EEPROM_load(file);
			}
			else
				init_eeprom_count = 10;
		}
	}};
	
	public static ReadHandlerPtr eeprom_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		int res;
	
	if (errorlog!=null) fprintf(errorlog,"%06x eeprom_r\n",cpu_get_pc());
		/* bit 6 is EEPROM data */
		/* bit 7 is EEPROM ready */
		/* bit 14 is service button */
		res = (EEPROM_read_bit() << 6) | input_port_2_r.handler(0);
		if (init_eeprom_count!=0)
		{
			init_eeprom_count--;
			res &= 0xbfff;
		}
		return res;
	} };
	
	public static WriteHandlerPtr eeprom_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	if (errorlog!=null) fprintf(errorlog,"%06x: write %04x to 108000\n",cpu_get_pc(),data);
		if ((data & 0x00ff0000) == 0)
		{
			/* bit 0 = coin counter */
			coin_counter_w.handler(0,data & 0x01);
	
			/* bit 2 is data */
			/* bit 3 is clock (active high) */
			/* bit 4 is cs (active low) */
			EEPROM_write_bit(data & 0x04);
			EEPROM_set_cs_line((data & 0x10)!=0 ? CLEAR_LINE : ASSERT_LINE);
			EEPROM_set_clock_line((data & 0x08)!=0 ? ASSERT_LINE : CLEAR_LINE);
		}
		else
		{
			/* bit 8 = enable sprite ROM reading */
			K053246_set_OBJCHA_line((data & 0x0100)!=0 ? ASSERT_LINE : CLEAR_LINE);
			/* bit 9 = enable char ROM reading through the video RAM */
			K052109_set_RMRD_line((data & 0x0200)!=0 ? ASSERT_LINE : CLEAR_LINE);
		}
	} };
	
	public static ReadHandlerPtr xmen_sound_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		/* fake self test pass until we emulate the sound chip */
		return 0x000f;
	} };
	
	public static WriteHandlerPtr xmen_sound_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (offset == 0)
		{
			if ((data & 0x00ff0000) == 0)
				soundlatch_w.handler(0,data & 0xff);
		}
		if (offset == 2) cpu_cause_interrupt(1,0xff);
	} };
	
	public static WriteHandlerPtr xmen_18fa00_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* bit 2 is interrupt enable */
		interrupt_enable_w.handler(0,data & 0x04);
	} };
	
	public static WriteHandlerPtr sound_bankswitch = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int bankaddress;
		UBytePtr RAM = memory_region(REGION_CPU2);
	
	
		bankaddress = 0x10000 + (data & 0x07) * 0x4000;
		cpu_setbank(4,new UBytePtr(RAM,bankaddress));
	} };
	
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
		new MemoryReadAddress( 0x080000, 0x0fffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x100fff, K053247_word_r ),
		new MemoryReadAddress( 0x101000, 0x101fff, MRA_BANK2 ),
		new MemoryReadAddress( 0x104000, 0x104fff, paletteram_word_r ),
		new MemoryReadAddress( 0x108054, 0x108055, xmen_sound_r ),
		new MemoryReadAddress( 0x10a000, 0x10a001, input_port_0_r ),
		new MemoryReadAddress( 0x10a002, 0x10a003, input_port_1_r ),
		new MemoryReadAddress( 0x10a004, 0x10a005, eeprom_r ),
		new MemoryReadAddress( 0x10a00c, 0x10a00d, K053246_word_r ),
		new MemoryReadAddress( 0x110000, 0x113fff, MRA_BANK1 ),	/* main RAM */
		new MemoryReadAddress( 0x18c000, 0x197fff, K052109_halfword_r ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x080000, 0x0fffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x100fff, K053247_word_w ),
		new MemoryWriteAddress( 0x101000, 0x101fff, MWA_BANK2 ),
		new MemoryWriteAddress( 0x104000, 0x104fff, paletteram_xBBBBBGGGGGRRRRR_word_w, paletteram ),
		new MemoryWriteAddress( 0x108000, 0x108001, eeprom_w ),
		new MemoryWriteAddress( 0x108020, 0x108027, K053246_word_w ),
		new MemoryWriteAddress( 0x10804c, 0x10804f, xmen_sound_w ),
		new MemoryWriteAddress( 0x108060, 0x10807f, K053251_halfword_w ),
		new MemoryWriteAddress( 0x10a000, 0x10a001, watchdog_reset_w ),
		new MemoryWriteAddress( 0x110000, 0x113fff, MWA_BANK1 ),	/* main RAM */
		new MemoryWriteAddress( 0x18fa00, 0x18fa01, xmen_18fa00_w ),
		new MemoryWriteAddress( 0x18c000, 0x197fff, K052109_halfword_w ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0xbfff, MRA_BANK4 ),
		new MemoryReadAddress( 0xc000, 0xdfff, MRA_RAM ),
		new MemoryReadAddress( 0xec01, 0xec01, YM2151_status_port_0_r ),
		new MemoryReadAddress( 0xf002, 0xf002, soundlatch_r ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new MemoryWriteAddress( 0xc000, 0xdfff, MWA_RAM ),
		new MemoryWriteAddress( 0xe800, 0xe800, YM2151_register_port_0_w ),
		new MemoryWriteAddress( 0xec01, 0xec01, YM2151_data_port_0_w ),
	//	new MemoryWriteAddress( 0xf000, 0xf000, soundlatch2_w ),	/* to main cpu */
		new MemoryWriteAddress( 0xf800, 0xf800, sound_bankswitch ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	
	static InputPortPtr input_ports_xmen = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER4 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER4 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER4 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER4 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER4 );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_COIN4 );
	
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER3 );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_COIN3 );
	
		PORT_START(); 	/* COIN  EEPROM and service */
		PORT_BIT( 0x003f, IP_ACTIVE_LOW, IPT_UNKNOWN );/* unused? */
		PORT_BIT( 0x0040, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* EEPROM data */
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );/* EEPROM status - always 1 */
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_START3 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_START4 );
		PORT_BIT( 0x3000, IP_ACTIVE_LOW, IPT_UNKNOWN );/* unused? */
		PORT_BITX(0x4000, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") , KEYCODE_F2, IP_JOY_NONE );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );/* unused? */
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_xmen2p = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_COIN2 );
	/*
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER4 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER4 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER4 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER4 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER4 );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_COIN4 );
	*/
	
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_COIN1 );
	/*
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER3 );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_COIN3 );
	*/
	
		PORT_START(); 	/* COIN  EEPROM and service */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_COIN4 );
		PORT_BIT( 0x003c, IP_ACTIVE_LOW, IPT_UNKNOWN );/* unused? */
		PORT_BIT( 0x0040, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* EEPROM data */
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );/* EEPROM status - always 1 */
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x3000, IP_ACTIVE_LOW, IPT_UNKNOWN );/* unused? */
		PORT_BITX(0x4000, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") , KEYCODE_F2, IP_JOY_NONE );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );/* unused? */
	INPUT_PORTS_END(); }}; 
	
	

        static YM2151interface ym2151_interface = new YM2151interface(
            1, /* 1 chip */
            3579545, /* 3.579545 MHz */
            new int[]{YM3012_VOL(100,MIXER_PAN_LEFT,100,MIXER_PAN_RIGHT)},
            new WriteYmHandlerPtr[]{null},
            new WriteHandlerPtr[]{null}
    );
	
	
	
	public static InterruptPtr xmen_interrupt = new InterruptPtr() { public int handler() 
	{
		if (cpu_getiloops() == 0) return m68_level5_irq.handler();
		else return m68_level3_irq.handler();
	} };
	
	static MachineDriver machine_driver_xmen = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				16000000,	/* ? */
				readmem,writemem,null,null,
				xmen_interrupt,2
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				2*3579545,	/* ????? */
				sound_readmem,sound_writemem,null,null,
				ignore_interrupt,0	/* IRQs are triggered by the main CPU */
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		64*8, 32*8, new rectangle( 14*8, (64-14)*8-1, 2*8, 30*8-1 ),
		null,	/* gfx decoded by konamiic.c */
		2048, 2048,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		xmen_vh_start,
		xmen_vh_stop,
		xmen_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,//SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			)
		},
	
		nvram_handler
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_xmen = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );
		ROM_LOAD_EVEN( "065ubb04.10d",  0x00000, 0x20000, 0xf896c93b );
		ROM_LOAD_ODD ( "065ubb05.10f",  0x00000, 0x20000, 0xe02e5d64 );
		ROM_LOAD_EVEN( "xmen17g.bin",   0x80000, 0x40000, 0xb31dc44c );
		ROM_LOAD_ODD ( "xmen17j.bin",   0x80000, 0x40000, 0x13842fe6 );
	
		ROM_REGION( 0x30000, REGION_CPU2 );	/* 64k+128k fpr sound cpu */
		ROM_LOAD( "065-a01.6f",   0x00000, 0x20000, 0x147d3a4d );
		ROM_RELOAD(               0x10000, 0x20000 );
	
		ROM_REGION( 0x200000, REGION_GFX1 );/* graphics (addressable by the main CPU) */
		ROM_LOAD( "xmen1l.bin",   0x000000, 0x100000, 0x6b649aca );/* tiles */
		ROM_LOAD( "xmen1h.bin",   0x100000, 0x100000, 0xc5dc8fc4 );
	
		ROM_REGION( 0x400000, REGION_GFX2 );/* graphics (addressable by the main CPU) */
		ROM_LOAD( "xmen12l.bin",  0x000000, 0x100000, 0xea05d52f );/* sprites */
		ROM_LOAD( "xmen17l.bin",  0x100000, 0x100000, 0x96b91802 );
		ROM_LOAD( "xmen22h.bin",  0x200000, 0x100000, 0x321ed07a );
		ROM_LOAD( "xmen22l.bin",  0x300000, 0x100000, 0x46da948e );
	
		ROM_REGION( 0x200000, REGION_SOUND1 );/* samples for the 054544 */
		ROM_LOAD( "xmenc25.bin",  0x000000, 0x200000, 0x5adbcee0 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_xmen6p = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );
		ROM_LOAD_EVEN( "xmenb04.bin",   0x00000, 0x20000, 0x0f09b8e0 );
		ROM_LOAD_ODD ( "xmenb05.bin",   0x00000, 0x20000, 0x867becbf );
		ROM_LOAD_EVEN( "xmen17g.bin",   0x80000, 0x40000, 0xb31dc44c );
		ROM_LOAD_ODD ( "xmen17j.bin",   0x80000, 0x40000, 0x13842fe6 );
	
		ROM_REGION( 0x30000, REGION_CPU2 );	/* 64k+128k fpr sound cpu */
		ROM_LOAD( "065-a01.6f",   0x00000, 0x20000, 0x147d3a4d );
		ROM_RELOAD(               0x10000, 0x20000 );
	
		ROM_REGION( 0x200000, REGION_GFX1 );/* graphics (addressable by the main CPU) */
		ROM_LOAD( "xmen1l.bin",   0x000000, 0x100000, 0x6b649aca );/* tiles */
		ROM_LOAD( "xmen1h.bin",   0x100000, 0x100000, 0xc5dc8fc4 );
	
		ROM_REGION( 0x400000, REGION_GFX2 );/* graphics (addressable by the main CPU) */
		ROM_LOAD( "xmen12l.bin",  0x000000, 0x100000, 0xea05d52f );/* sprites */
		ROM_LOAD( "xmen17l.bin",  0x100000, 0x100000, 0x96b91802 );
		ROM_LOAD( "xmen22h.bin",  0x200000, 0x100000, 0x321ed07a );
		ROM_LOAD( "xmen22l.bin",  0x300000, 0x100000, 0x46da948e );
	
		ROM_REGION( 0x200000, REGION_SOUND1 );/* samples for the 054544 */
		ROM_LOAD( "xmenc25.bin",  0x000000, 0x200000, 0x5adbcee0 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_xmen2pj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );
		ROM_LOAD_EVEN( "065jaa04.10d",  0x00000, 0x20000, 0x66746339 );
		ROM_LOAD_ODD ( "065jaa05.10f",  0x00000, 0x20000, 0x1215b706 );
		ROM_LOAD_EVEN( "xmen17g.bin",   0x80000, 0x40000, 0xb31dc44c );
		ROM_LOAD_ODD ( "xmen17j.bin",   0x80000, 0x40000, 0x13842fe6 );
	
		ROM_REGION( 0x30000, REGION_CPU2 );	/* 64k+128k fpr sound cpu */
		ROM_LOAD( "065-a01.6f",   0x00000, 0x20000, 0x147d3a4d );
		ROM_RELOAD(               0x10000, 0x20000 );
	
		ROM_REGION( 0x200000, REGION_GFX1 );/* graphics (addressable by the main CPU) */
		ROM_LOAD( "xmen1l.bin",   0x000000, 0x100000, 0x6b649aca );/* tiles */
		ROM_LOAD( "xmen1h.bin",   0x100000, 0x100000, 0xc5dc8fc4 );
	
		ROM_REGION( 0x400000, REGION_GFX2 );/* graphics (addressable by the main CPU) */
		ROM_LOAD( "xmen12l.bin",  0x000000, 0x100000, 0xea05d52f );/* sprites */
		ROM_LOAD( "xmen17l.bin",  0x100000, 0x100000, 0x96b91802 );
		ROM_LOAD( "xmen22h.bin",  0x200000, 0x100000, 0x321ed07a );
		ROM_LOAD( "xmen22l.bin",  0x300000, 0x100000, 0x46da948e );
	
		ROM_REGION( 0x200000, REGION_SOUND1 );/* samples for the 054544 */
		ROM_LOAD( "xmenc25.bin",  0x000000, 0x200000, 0x5adbcee0 );
	ROM_END(); }}; 
	
	
	
	public static InitDriverPtr init_xmen = new InitDriverPtr() { public void handler() 
	{
		konami_rom_deinterleave_2(REGION_GFX1);
		konami_rom_deinterleave_4(REGION_GFX2);
	} };
	
	public static InitDriverPtr init_xmen6p = new InitDriverPtr() { public void handler() 
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
	
		RAM.WRITE_WORD(0x21a6,0x4e71);
		RAM.WRITE_WORD(0x21a8,0x4e71);
		RAM.WRITE_WORD(0x21aa,0x4e71);
	
		init_xmen.handler();
	} };
	
	
	
	public static GameDriver driver_xmen	   = new GameDriver("1992"	,"xmen"	,"xmen.java"	,rom_xmen,null	,machine_driver_xmen	,input_ports_xmen	,init_xmen	,ROT0	,	"Konami", "X-Men (4 Players)", GAME_IMPERFECT_SOUND );
	public static GameDriver driver_xmen6p	   = new GameDriver("1992"	,"xmen6p"	,"xmen.java"	,rom_xmen6p,driver_xmen	,machine_driver_xmen	,input_ports_xmen	,init_xmen6p	,ROT0	,	"Konami", "X-Men (6 Players)", GAME_IMPERFECT_SOUND | GAME_NOT_WORKING );
	public static GameDriver driver_xmen2pj	   = new GameDriver("1992"	,"xmen2pj"	,"xmen.java"	,rom_xmen2pj,driver_xmen	,machine_driver_xmen	,input_ports_xmen2p	,init_xmen	,ROT0	,	"Konami", "X-Men (2 Players Japan)", GAME_IMPERFECT_SOUND );
}
