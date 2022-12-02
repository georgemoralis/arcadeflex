/****************************************************************************

	Exterminator memory map

driver by Zsolt Vasvari and Alex Pasadyn


 Master CPU (TMS34010, all addresses are in bits)

 00000000-000fffff RW Video RAM (256x256x15)
 00c00000-00ffffff RW RAM
 01000000-010fffff  W Host Control Interface (HSTADRL)
 01100000-011fffff  W Host Control Interface (HSTADRH)
 01200000-012fffff RW Host Control Interface (HSTDATA)
 01300000-013fffff  W Host Control Interface (HSTCTLH)
 01400000-01400007 R  Input Port null
 01400008-0140000f R  Input Port 1
 01440000-01440007 R  Input Port 2
 01440008-0144000f R  Input Port 3
 01480000-01480007 R  Input Port 4
 01500000-0150000f  W Output Port null (See machine/exterm.c)
 01580000-0158000f  W Sound Command
 015c0000-015c000f  W Watchdog
 01800000-01807fff RW Palette RAM
 02800000-02807fff RW EEPROM
 03000000-03ffffff R  ROM
 3f000000-3fffffff R  ROM Mirror
 c0000000-c00001ff RW TMS34010 I/O Registers
 ff000000-ffffffff R  ROM Mirror


 Slave CPU (TMS34010, all addresses are in bits)

 00000000-000fffff RW Video RAM (2 banks of 256x256x8)
 c0000000-c00001ff RW TMS34010 I/O Registers
 ff800000-ffffffff RW RAM


 DAC Controller CPU (6502)

 0000-07ff RW RAM
 4000      R  Sound Command
 8000-8001  W 2 Channels of DAC output
 8000-ffff R  ROM


 YM2151 Controller CPU (6502)

 0000-07ff RW RAM
 4000       W YM2151 Command/Data Register (Controlled by a bit A000)
 6000  		W NMI occurence rate (fed into a binary counter)
 6800      R  Sound Command
 7000      R  Causes NMI on DAC CPU
 8000-ffff R  ROM
 a000       W Control register (see sndhrdw/gottlieb.c)

****************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;

public class exterm
{
	
	static unsigned char *eeprom;
	static int eeprom_size;
	static int code_rom_size;
	unsigned char *exterm_code_rom;
	
	
	/* Functions in vidhrdw/exterm.c */
	void exterm_init_palette(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom);
	int  exterm_vh_start(void);
	void exterm_vh_stop (void);
	public static ReadHandlerPtr exterm_master_videoram_r = new ReadHandlerPtr() { public int handler(int offset);
	public static ReadHandlerPtr exterm_slave_videoram_r = new ReadHandlerPtr() { public int handler(int offset);
	public static WriteHandlerPtr exterm_paletteram_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	void exterm_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh);
	void exterm_to_shiftreg_master(unsigned int address, unsigned short* shiftreg);
	void exterm_from_shiftreg_master(unsigned int address, unsigned short* shiftreg);
	void exterm_to_shiftreg_slave(unsigned int address, unsigned short* shiftreg);
	void exterm_from_shiftreg_slave(unsigned int address, unsigned short* shiftreg);
	
	/* Functions in sndhrdw/gottlieb.c */
	public static WriteHandlerPtr gottlieb_sh_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static ReadHandlerPtr gottlieb_cause_dac_nmi_r = new ReadHandlerPtr() { public int handler(int offset);
	public static WriteHandlerPtr gottlieb_nmi_rate_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static WriteHandlerPtr exterm_sound_control_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static WriteHandlerPtr exterm_ym2151_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	
	/* Functions in machine/exterm.c */
	public static WriteHandlerPtr exterm_host_data_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static ReadHandlerPtr exterm_host_data_r = new ReadHandlerPtr() { public int handler(int offset);
	public static ReadHandlerPtr exterm_coderom_r = new ReadHandlerPtr() { public int handler(int offset);
	public static ReadHandlerPtr exterm_input_port_0_1_r = new ReadHandlerPtr() { public int handler(int offset);
	public static ReadHandlerPtr exterm_input_port_2_3_r = new ReadHandlerPtr() { public int handler(int offset);
	public static WriteHandlerPtr exterm_output_port_0_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static ReadHandlerPtr exterm_master_speedup_r = new ReadHandlerPtr() { public int handler(int offset);
	public static WriteHandlerPtr exterm_slave_speedup_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static ReadHandlerPtr exterm_sound_dac_speedup_r = new ReadHandlerPtr() { public int handler(int offset);
	public static ReadHandlerPtr exterm_sound_ym2151_speedup_r = new ReadHandlerPtr() { public int handler(int offset);
	
	
	static void nvram_handler(void *file, int read_or_write)
	{
		if (read_or_write)
			osd_fwrite(file,eeprom,eeprom_size);
		else
		{
			if (file)
				osd_fread(file,eeprom,eeprom_size);
			else
				memset(eeprom,0,eeprom_size);
		}
	} };
	
	
	static struct tms34010_config master_config =
	{
		0,							/* halt on reset */
		NULL,						/* generate interrupt */
		exterm_to_shiftreg_master,	/* write to shiftreg function */
		exterm_from_shiftreg_master	/* read from shiftreg function */
	};
	
	
	static MemoryReadAddress master_readmem[] =
	{
		new MemoryReadAddress( TOBYTE(0x00000000), TOBYTE(0x000fffff), exterm_master_videoram_r ),
		new MemoryReadAddress( TOBYTE(0x00c800e0), TOBYTE(0x00c800ef), exterm_master_speedup_r ),
		new MemoryReadAddress( TOBYTE(0x00c00000), TOBYTE(0x00ffffff), MRA_BANK1 ),
		new MemoryReadAddress( TOBYTE(0x01000000), TOBYTE(0x0100000f), MRA_NOP ), /* Off by one bug in RAM test, prevent log entry */
		new MemoryReadAddress( TOBYTE(0x01200000), TOBYTE(0x012fffff), exterm_host_data_r ),
		new MemoryReadAddress( TOBYTE(0x01400000), TOBYTE(0x0140000f), exterm_input_port_0_1_r ),
		new MemoryReadAddress( TOBYTE(0x01440000), TOBYTE(0x0144000f), exterm_input_port_2_3_r ),
		new MemoryReadAddress( TOBYTE(0x01480000), TOBYTE(0x0148000f), input_port_4_r ),
		new MemoryReadAddress( TOBYTE(0x01800000), TOBYTE(0x01807fff), paletteram_word_r ),
		new MemoryReadAddress( TOBYTE(0x01808000), TOBYTE(0x0180800f), MRA_NOP ), /* Off by one bug in RAM test, prevent log entry */
		new MemoryReadAddress( TOBYTE(0x02800000), TOBYTE(0x02807fff), MRA_BANK2 ),
		new MemoryReadAddress( TOBYTE(0x03000000), TOBYTE(0x03ffffff), exterm_coderom_r ),
		new MemoryReadAddress( TOBYTE(0x3f000000), TOBYTE(0x3fffffff), exterm_coderom_r ),
		new MemoryReadAddress( TOBYTE(0xc0000000), TOBYTE(0xc00001ff), TMS34010_io_register_r ),
		new MemoryReadAddress( TOBYTE(0xff000000), TOBYTE(0xffffffff), MRA_BANK3 ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	public static WriteHandlerPtr placeholder = new WriteHandlerPtr() { public void handler(int offset, int data)
	{} };
	
	static MemoryWriteAddress master_writemem[] =
	{
		new MemoryWriteAddress( TOBYTE(0x00000000), TOBYTE(0x000fffff), placeholder, exterm_master_videoram ),
	/*new MemoryWriteAddress( TOBYTE(0x00000000), TOBYTE(0x000fffff), exterm_master_videoram_16_w ),	 OR		*/
	/*new MemoryWriteAddress( TOBYTE(0x00000000), TOBYTE(0x000fffff), exterm_master_videoram_8_w ),				*/
		new MemoryWriteAddress( TOBYTE(0x00c00000), TOBYTE(0x00ffffff), MWA_BANK1 ),
		new MemoryWriteAddress( TOBYTE(0x00c800e0), TOBYTE(0x00c800ef), placeholder, exterm_master_speedup ),
		new MemoryWriteAddress( TOBYTE(0x01000000), TOBYTE(0x013fffff), exterm_host_data_w ),
		new MemoryWriteAddress( TOBYTE(0x01500000), TOBYTE(0x0150000f), exterm_output_port_0_w ),
		new MemoryWriteAddress( TOBYTE(0x01580000), TOBYTE(0x0158000f), gottlieb_sh_w ),
		new MemoryWriteAddress( TOBYTE(0x015c0000), TOBYTE(0x015c000f), watchdog_reset_w ),
		new MemoryWriteAddress( TOBYTE(0x01800000), TOBYTE(0x01807fff), exterm_paletteram_w, paletteram ),
		new MemoryWriteAddress( TOBYTE(0x02800000), TOBYTE(0x02807fff), MWA_BANK2, eeprom, eeprom_size ), /* EEPROM */
		new MemoryWriteAddress( TOBYTE(0xc0000000), TOBYTE(0xc00001ff), TMS34010_io_register_w ),
		new MemoryWriteAddress( TOBYTE(0xff000000), TOBYTE(0xffffffff), MWA_BANK3, exterm_code_rom, code_rom_size ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	static struct tms34010_config slave_config =
	{
		1,							/* halt on reset */
		NULL,						/* generate interrupt */
		exterm_to_shiftreg_slave,	/* write to shiftreg function */
		exterm_from_shiftreg_slave	/* read from shiftreg function */
	};
	
	
	static MemoryReadAddress slave_readmem[] =
	{
		new MemoryReadAddress( TOBYTE(0x00000000), TOBYTE(0x000fffff), exterm_slave_videoram_r ),
		new MemoryReadAddress( TOBYTE(0xc0000000), TOBYTE(0xc00001ff), TMS34010_io_register_r ),
		new MemoryReadAddress( TOBYTE(0xff800000), TOBYTE(0xffffffff), MRA_BANK4 ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress slave_writemem[] =
	{
		new MemoryWriteAddress( TOBYTE(0x00000000), TOBYTE(0x000fffff), placeholder, exterm_slave_videoram ),
	/*new MemoryWriteAddress( TOBYTE(0x00000000), TOBYTE(0x000fffff), exterm_slave_videoram_16_w ),      OR		*/
	/*new MemoryWriteAddress( TOBYTE(0x00000000), TOBYTE(0x000fffff), exterm_slave_videoram_8_w ),       OR		*/
		new MemoryWriteAddress( TOBYTE(0xc0000000), TOBYTE(0xc00001ff), TMS34010_io_register_w ),
		new MemoryWriteAddress( TOBYTE(0xfffffb90), TOBYTE(0xfffffb90), exterm_slave_speedup_w, exterm_slave_speedup ),
		new MemoryWriteAddress( TOBYTE(0xff800000), TOBYTE(0xffffffff), MWA_BANK4 ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	static MemoryReadAddress sound_dac_readmem[] =
	{
		new MemoryReadAddress( 0x0007, 0x0007, exterm_sound_dac_speedup_r ),
		new MemoryReadAddress( 0x0000, 0x07ff, MRA_RAM ),
		new MemoryReadAddress( 0x4000, 0x4000, soundlatch_r ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress sound_dac_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x07ff, MWA_RAM ),
		new MemoryWriteAddress( 0x8000, 0x8001, DAC_data_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	static MemoryReadAddress sound_ym2151_readmem[] =
	{
		new MemoryReadAddress( 0x02b6, 0x02b6, exterm_sound_ym2151_speedup_r ),
		new MemoryReadAddress( 0x0000, 0x07ff, MRA_RAM ),
		new MemoryReadAddress( 0x6800, 0x6800, soundlatch_r ),
		new MemoryReadAddress( 0x7000, 0x7000, gottlieb_cause_dac_nmi_r ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress sound_ym2151_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x07ff, MWA_RAM ),
		new MemoryWriteAddress( 0x4000, 0x4000, exterm_ym2151_w ),
		new MemoryWriteAddress( 0x6000, 0x6000, gottlieb_nmi_rate_w ),
		new MemoryWriteAddress( 0xa000, 0xa000, exterm_sound_control_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	
	static InputPortPtr input_ports_exterm = new InputPortPtr(){ public void handler() { 
	
		PORT_START();       /* IN0 LO */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1);
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1);
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1);
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
	
		PORT_START();       /* IN0 HI */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1, "Aim Left",  KEYCODE_Z, IP_JOY_DEFAULT );
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1, "Aim Right", KEYCODE_X, IP_JOY_DEFAULT );
		PORT_BIT( 0xec, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	
		PORT_START();       /* IN1 LO */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2);
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2);
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2);
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
	
		PORT_START();       /* IN1 HI */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2, "2 Aim Left",  KEYCODE_H, IP_JOY_DEFAULT );
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2, "2 Aim Right", KEYCODE_J, IP_JOY_DEFAULT );
		PORT_BIT( 0xfc, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* DSW */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unused") ); /* According to the test screen */
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		/* Note that the coin settings don't match the setting shown on the test screen,
		   but instead what the game appears to used. This is either a bug in the game,
		   or I don't know what else. */
		PORT_DIPNAME( 0x06, 0x06, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x28, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_8C") );
		PORT_DIPNAME( 0x40, 0x40, "Memory Test" );
		PORT_DIPSETTING(    0x40, "Single" );
		PORT_DIPSETTING(    0x00, "Continous" );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Free_Play") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	
	static DACinterface dac_interface = new DACinterface
	(
		2, 			/* 2 channels on 1 chip */
		new int[] { 50, 50 },
	);
	
	static struct YM2151interface ym2151_interface =
	{
		1,			/* 1 chip */
		4000000,	/* 4 MHz */
		{ YM3012_VOL(50,MIXER_PAN_LEFT,50,MIXER_PAN_RIGHT) },
		{ 0 }
	};
	
	
	static MachineDriver machine_driver_exterm = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_TMS34010,
				40000000/8,	/* 40 Mhz */
	            master_readmem,master_writemem,null,null,
	            ignore_interrupt,0,  /* Display Interrupts caused internally */
	            null,null,master_config
			),
			new MachineCPU(
				CPU_TMS34010,
				40000000/8,	/* 40 Mhz */
	            slave_readmem,slave_writemem,null,null,
	            ignore_interrupt,0,  /* Display Interrupts caused internally */
	            null,null,slave_config
			),
			new MachineCPU(
				CPU_M6502 | CPU_AUDIO_CPU,
				2000000,	/* 2 Mhz */
				sound_dac_readmem,sound_dac_writemem,null,null,
				ignore_interrupt,0	/* IRQ caused when sound command is written */
									/* NMIs are triggered by the YM2151 CPU */
			),
			new MachineCPU(
				CPU_M6502 | CPU_AUDIO_CPU,
				2000000,	/* 2 Mhz */
				sound_ym2151_readmem,sound_ym2151_writemem,null,null,
				ignore_interrupt,0	/* IRQ caused when sound command is written */
									/* NMIs are triggered by a programmable timer */
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,
		null,
	
		/* video hardware, the reason for 263 is that the VCOUNT register is
		   supposed to go from 0 to the value in VEND-1, which is 263 */
	    256, 263, { 0, 255, 0, 238 },
	
		null,
		4096+32768,null,
	    exterm_init_palette,
	
	    VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		exterm_vh_start,
		exterm_vh_stop,
		exterm_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_DAC,
				dac_interface
			),
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
	
	static RomLoadPtr rom_exterm = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x200000, REGION_CPU1 );    /* 2MB for 34010 code */
		ROM_LOAD_ODD(  "v101bg0",  0x000000, 0x10000, 0x8c8e72cf )
		ROM_LOAD_EVEN( "v101bg1",  0x000000, 0x10000, 0xcc2da0d8 )
		ROM_LOAD_ODD(  "v101bg2",  0x020000, 0x10000, 0x2dcb3653 )
		ROM_LOAD_EVEN( "v101bg3",  0x020000, 0x10000, 0x4aedbba0 )
		ROM_LOAD_ODD(  "v101bg4",  0x040000, 0x10000, 0x576922d4 )
		ROM_LOAD_EVEN( "v101bg5",  0x040000, 0x10000, 0xa54a4bc2 )
		ROM_LOAD_ODD(  "v101bg6",  0x060000, 0x10000, 0x7584a676 )
		ROM_LOAD_EVEN( "v101bg7",  0x060000, 0x10000, 0xa4f24ff6 )
		ROM_LOAD_ODD(  "v101bg8",  0x080000, 0x10000, 0xfda165d6 )
		ROM_LOAD_EVEN( "v101bg9",  0x080000, 0x10000, 0xe112a4c4 )
		ROM_LOAD_ODD(  "v101bg10", 0x0a0000, 0x10000, 0xf1a5cf54 )
		ROM_LOAD_EVEN( "v101bg11", 0x0a0000, 0x10000, 0x8677e754 )
		ROM_LOAD_ODD(  "v101fg0",  0x180000, 0x10000, 0x38230d7d )
		ROM_LOAD_EVEN( "v101fg1",  0x180000, 0x10000, 0x22a2bd61 )
		ROM_LOAD_ODD(  "v101fg2",  0x1a0000, 0x10000, 0x9420e718 )
		ROM_LOAD_EVEN( "v101fg3",  0x1a0000, 0x10000, 0x84992aa2 )
		ROM_LOAD_ODD(  "v101fg4",  0x1c0000, 0x10000, 0x38da606b )
		ROM_LOAD_EVEN( "v101fg5",  0x1c0000, 0x10000, 0x842de63a )
		ROM_LOAD_ODD(  "v101p0",   0x1e0000, 0x10000, 0x6c8ee79a )
		ROM_LOAD_EVEN( "v101p1",   0x1e0000, 0x10000, 0x557bfc84 )
	
		ROM_REGION( 0x1000, REGION_CPU2 ); /* Slave CPU memory space. There are no ROMs mapped here */
	
		ROM_REGION( 0x10000, REGION_CPU3 ); /* 64k for DAC code */
		ROM_LOAD( "v101d1", 0x08000, 0x08000, 0x83268b7d );
	
		ROM_REGION( 0x10000, REGION_CPU4 ); /* 64k for YM2151 code */
		ROM_LOAD( "v101y1", 0x08000, 0x08000, 0xcbeaa837 );
	ROM_END(); }}; 
	
	
	public static InitDriverPtr init_exterm = new InitDriverPtr() { public void handler() 
	{
		memcpy (exterm_code_rom,memory_region(REGION_CPU1),code_rom_size);
	
		TMS34010_set_stack_base(0, cpu_bankbase[1], TOBYTE(0x00c00000));
		TMS34010_set_stack_base(1, cpu_bankbase[4], TOBYTE(0xff800000));
	} };
	
	
	public static GameDriver driver_exterm	   = new GameDriver("1989"	,"exterm"	,"exterm.java"	,rom_exterm,null	,machine_driver_exterm	,input_ports_exterm	,init_exterm	,ROT0_16BIT	,	"Gottlieb / Premier Technology", "Exterminator" )
}
