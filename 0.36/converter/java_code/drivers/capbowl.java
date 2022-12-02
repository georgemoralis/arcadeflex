/***************************************************************************

  Coors Light Bowling/Bowl-O-Rama memory map

  driver by Zsolt Vasvari

  CPU Board:

  0000-3fff     3 Graphics ROMS mapped in using 0x4800 (Coors Light Bowling only)
  0000-001f		Turbo board area (Bowl-O-Rama only) See Below.
  4000          Display row selected
  4800          Graphics ROM select
  5000-57ff     Battery backed up RAM (Saves machine state after shut off)
                Enter setup menu by holding down the F2 key on the
                high score screen
  5800-5fff		TMS34061 area

                First 0x20 bytes of each row provide a 16 color palette for this
                row. 2 bytes per color: 0000RRRR GGGGBBBB.

                Remaining 0xe0 bytes contain 2 pixels each for a total of
                448 pixels, but only 360 seem to be displayed.
                (Each row appears vertically because the monitor is rotated)

  6000          Sound command
  6800			Trackball Reset. Double duties as a watchdog.
  7000          Input port 1    Bit null-3 Trackball Vertical Position
							  	Bit 4   Player 2 Hook Left
								Bit 5   Player 2 Hook Right
								Bit 6   Upright/Cocktail DIP Switch
                                Bit 7   Coin 2
  7800          Input port 2    Bit null-3 Trackball Horizontal Positon
                                Bit 4   Player 1 Hook Left
                                Bit 5   Player 1 Hook Right
                                Bit 6   Start
                                Bit 7   Coin 1
  8000-ffff		ROM


  Sound Board:

  0000-07ff		RAM
  1000-1001		YM2203
			  	Port A D7 Read  is ticket sensor
				Port B D7 Write is ticket dispenser enable
				Port B D6 looks like a diagnostics LED to indicate that
				          the PCB is operating. It's pulsated by the
						  sound CPU. It is kind of pointless to emulate it.
  2000			Not hooked up according to the schematics
  6000			DAC write
  7000			Sound command read (0x34 is used to dispense a ticket)
  8000-ffff		ROM


  Turbo Board Layout (Plugs in place of GR0):

  Bowl-O-Rama	Copyright 1991 PP Marketing
				Marquee says "EXIT Entertainment"

				This portion: Mike Appolo with the help of Andrew Pines.
				Andrew was one of the game designers for Capcom Bowling,
				Coors Light Bowling, Strata Bowling, and Bowl-O-Rama.

				This game was an upgrade for Capcom Bowling and included a
				"Turbo PCB" that had a GAL address decoder / data mask

  Memory Map for turbo board (where GR0 is on Capcom Bowling PCBs:

  0000   		Read Mask
  0001-0003		Unused
  0004  		Read Data
  0005-0007		Unused
  0008  		GR Address High Byte (GR17-16)
  0009-0016		Unused
  0017			GR Address Middle Byte (GR15-null written as a word to 0017-0018)
  0018  		GR address Low byte
  0019-001f		Unused

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;

public class capbowl
{
	
	void capbowl_init_machine(void);
	
	void capbowl_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh);
	
	int  capbowl_vh_start(void);
	void capbowl_vh_stop(void);
	
	
	public static WriteHandlerPtr capbowl_rom_select_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	
	public static ReadHandlerPtr capbowl_pagedrom_r = new ReadHandlerPtr() { public int handler(int offset);
	
	public static WriteHandlerPtr bowlrama_turbo_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static ReadHandlerPtr bowlrama_turbo_r = new ReadHandlerPtr() { public int handler(int offset);
	
	
	
	static unsigned char *nvram;
	static int nvram_size;
	
	static void nvram_handler(void *file,int read_or_write)
	{
		if (read_or_write)
			osd_fwrite(file,nvram,nvram_size);
		else
		{
			if (file)
				osd_fread(file,nvram,nvram_size);
			else
			{
				/* invalidate nvram to make the game initialize it.
				   A 0xff fill will cause the game to malfunction, so we use a
				   0x01 fill which seems OK */
				memset(nvram,0x01,nvram_size);
			}
		}
	} };
	
	
	public static WriteHandlerPtr capbowl_sndcmd_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		cpu_cause_interrupt(1, M6809_INT_IRQ);
	
		soundlatch_w.handler(offset, data);
	} };
	
	
	/* Handler called by the 2203 emulator when the internal timers cause an IRQ */
	static void firqhandler(int irq)
	{
		cpu_set_irq_line(1,1,irq ? ASSERT_LINE : CLEAR_LINE);
	}
	
	
	/***************************************************************************
	
	  NMI is to trigger the self test. We use a fake input port to tie that
	  event to a keypress.
	
	***************************************************************************/
	public static InterruptPtr capbowl_interrupt = new InterruptPtr() { public int handler() 
	{
		if (readinputport(4) & 1)	/* get status of the F2 key */
			return nmi_interrupt();	/* trigger self test */
	
		return ignore_interrupt();
	} };
	
	
	static int track[2];
	
	public static ReadHandlerPtr track_0_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return (input_port_0_r(offset) & 0xf0) | ((input_port_2_r(offset) - track[0]) & 0x0f);
	} };
	
	public static ReadHandlerPtr track_1_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return (input_port_1_r(offset) & 0xf0) | ((input_port_3_r(offset) - track[1]) & 0x0f);
	} };
	
	public static WriteHandlerPtr track_reset_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* reset the trackball counters */
		track[0] = input_port_2_r(offset);
		track[1] = input_port_3_r(offset);
	
		watchdog_reset_w(offset,data);
	} };
	
	
	
	static MemoryReadAddress capbowl_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x3fff, MRA_BANK1 ),
		new MemoryReadAddress( 0x5000, 0x57ff, MRA_RAM ),
		new MemoryReadAddress( 0x5800, 0x5fff, TMS34061_r ),
		new MemoryReadAddress( 0x7000, 0x7000, track_0_r ),	/* + other inputs */
		new MemoryReadAddress( 0x7800, 0x7800, track_1_r ),	/* + other inputs */
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress bowlrama_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x001f, bowlrama_turbo_r ),
		new MemoryReadAddress( 0x5000, 0x57ff, MRA_RAM ),
		new MemoryReadAddress( 0x5800, 0x5fff, TMS34061_r ),
		new MemoryReadAddress( 0x7000, 0x7000, track_0_r ),	/* + other inputs */
		new MemoryReadAddress( 0x7800, 0x7800, track_1_r ),	/* + other inputs */
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x001f, bowlrama_turbo_w ),	/* Bowl-O-Rama only */
		new MemoryWriteAddress( 0x4000, 0x4000, MWA_RAM, capbowl_rowaddress ),
		new MemoryWriteAddress( 0x4800, 0x4800, capbowl_rom_select_w ),
		new MemoryWriteAddress( 0x5000, 0x57ff, MWA_RAM, nvram, nvram_size ),
		new MemoryWriteAddress( 0x5800, 0x5fff, TMS34061_w ),
		new MemoryWriteAddress( 0x6000, 0x6000, capbowl_sndcmd_w ),
		new MemoryWriteAddress( 0x6800, 0x6800, track_reset_w ),	/* + watchdog */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x07ff, MRA_RAM ),
		new MemoryReadAddress( 0x1000, 0x1000, YM2203_status_port_0_r ),
		new MemoryReadAddress( 0x1001, 0x1001, YM2203_read_port_0_r ),
		new MemoryReadAddress( 0x7000, 0x7000, soundlatch_r ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x07ff, MWA_RAM),
		new MemoryWriteAddress( 0x1000, 0x1000, YM2203_control_port_0_w ),
		new MemoryWriteAddress( 0x1001, 0x1001, YM2203_write_port_0_w ),
		new MemoryWriteAddress( 0x2000, 0x2000, MWA_NOP ),  /* Not hooked up according to the schematics */
		new MemoryWriteAddress( 0x6000, 0x6000, DAC_data_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	
	static InputPortPtr input_ports_capbowl = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		/* low 4 bits are for the trackball */
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Cabinet") ); /* This version of Bowl-O-Rama */
		PORT_DIPSETTING(    0x40, DEF_STR( "Upright") );			   /* is Upright only */
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN2 );
	
		PORT_START(); 	/* IN1 */
		/* low 4 bits are for the trackball */
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );
	
		PORT_START(); 	/* FAKE */
		PORT_ANALOG( 0xff, 0x00, IPT_TRACKBALL_Y | IPF_REVERSE, 20, 40, 0, 0 );
	
		PORT_START(); 	/* FAKE */
		PORT_ANALOG( 0xff, 0x00, IPT_TRACKBALL_X, 20, 40, 0, 0 );
	
		PORT_START(); 	/* FAKE */
		/* This fake input port is used to get the status of the F2 key, */
		/* and activate the test mode, which is triggered by a NMI */
		PORT_BITX(0x01, IP_ACTIVE_HIGH, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
	INPUT_PORTS_END(); }}; 
	
	
	
	static struct YM2203interface ym2203_interface =
	{
		1,			/* 1 chip */
		4000000,	/* 4 MHz */
		{ YM2203_VOL(40,40) },
		{ ticket_dispenser_r },
		{ 0 },
		{ 0 },
		{ ticket_dispenser_w },  /* Also a status LED. See memory map above */
		{ firqhandler }
	};
	
	static DACinterface dac_interface = new DACinterface
	(
		1,
		new int[] { 100 }
	);
	
	
	#define MACHINEDRIVER(NAME, VISIBLE_Y)						\
																\
	static MachineDriver machine_driver_##NAME = new MachineDriver\
	(															\
		/* basic machine hardware */   							\
		new MachineCPU[] {														\
			new MachineCPU(													\
				CPU_M6809,										\
				2000000,        /* 2 Mhz */						\
				NAME##_readmem,writemem,null,null,					\
				capbowl_interrupt, 1,       /* To check Service mode status */ \
			),													\
			new MachineCPU(													\
				CPU_M6809 | CPU_AUDIO_CPU,						\
				2000000,        /* 2 Mhz */						\
				sound_readmem,sound_writemem,null,null,				\
				ignore_interrupt,1	/* interrupts are generated by the sound hardware */ \
			)													\
		},														\
		57, 5000,	/* frames per second, vblank duration (guess) */ \
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */ \
		capbowl_init_machine,									\
																\
		/* video hardware */									\
		360, 256, new rectangle( 0, 359, 0, VISIBLE_Y ),						\
		0,														\
		16*256,16*256,											\
		null,														\
																\
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,				\
		null,														\
		capbowl_vh_start,										\
		capbowl_vh_stop,										\
		capbowl_vh_screenrefresh,								\
																\
		/* sound hardware */									\
		0,0,0,0,												\
		new MachineSound[] {														\
			new MachineSound(													\
				SOUND_YM2203,									\
				ym2203_interface								\
			),													\
			new MachineSound(													\
				SOUND_DAC,										\
				dac_interface									\
			)													\
		},														\
																\
		nvram_handler											\
	);
	
	
	MACHINEDRIVER(capbowl,  244)
	
	MACHINEDRIVER(bowlrama, 239)
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_capbowl = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x28000, REGION_CPU1 );  /* 160k for code and graphics */
		ROM_LOAD( "u6",           0x08000, 0x8000, 0x14924c96 );
		ROM_LOAD( "gr0",          0x10000, 0x8000, 0xef53ca7a );
		ROM_LOAD( "gr1",          0x18000, 0x8000, 0x27ede6ce );
		ROM_LOAD( "gr2",          0x20000, 0x8000, 0xe49238f4 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );  /* 64k for sound */
		ROM_LOAD( "sound",        0x8000, 0x8000, 0x8c9c3b8a );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_capbowl2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x28000, REGION_CPU1 );  /* 160k for code and graphics */
		ROM_LOAD( "progrev3.u6",  0x08000, 0x8000, 0x9162934a );
		ROM_LOAD( "gr0",          0x10000, 0x8000, 0xef53ca7a );
		ROM_LOAD( "gr1",          0x18000, 0x8000, 0x27ede6ce );
		ROM_LOAD( "gr2",          0x20000, 0x8000, 0xe49238f4 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );  /* 64k for sound */
		ROM_LOAD( "sound",        0x8000, 0x8000, 0x8c9c3b8a );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_clbowl = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x28000, REGION_CPU1 );  /* 160k for code and graphics */
		ROM_LOAD( "u6.cl",        0x08000, 0x8000, 0x91e06bc4 );
		ROM_LOAD( "gr0.cl",       0x10000, 0x8000, 0x899c8f15 );
		ROM_LOAD( "gr1.cl",       0x18000, 0x8000, 0x0ac0dc4c );
		ROM_LOAD( "gr2.cl",       0x20000, 0x8000, 0x251f5da5 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );  /* 64k for sound */
		ROM_LOAD( "sound.cl",     0x8000, 0x8000, 0x1eba501e );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_bowlrama = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );     /* 64k for code */
		ROM_LOAD( "u6",           0x08000, 0x08000, 0x7103ad55 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for sound */
		ROM_LOAD( "u30",          0x8000, 0x8000, 0xf3168834 );
	
		ROM_REGION( 0x40000, REGION_GFX1 );    /* 256K for Graphics used at runtime */
		ROM_LOAD( "ux7",          0x00000, 0x40000, 0x8727432a );
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_capbowl	   = new GameDriver("1988"	,"capbowl"	,"capbowl.java"	,rom_capbowl,null	,machine_driver_capbowl	,input_ports_capbowl	,null	,ROT270_16BIT	,	"Incredible Technologies", "Capcom Bowling (set 1)" )
	public static GameDriver driver_capbowl2	   = new GameDriver("1988"	,"capbowl2"	,"capbowl.java"	,rom_capbowl2,driver_capbowl	,machine_driver_capbowl	,input_ports_capbowl	,null	,ROT270_16BIT	,	"Incredible Technologies", "Capcom Bowling (set 2)" )
	public static GameDriver driver_clbowl	   = new GameDriver("1989"	,"clbowl"	,"capbowl.java"	,rom_clbowl,driver_capbowl	,machine_driver_capbowl	,input_ports_capbowl	,null	,ROT270_16BIT	,	"Incredible Technologies", "Coors Light Bowling" )
	public static GameDriver driver_bowlrama	   = new GameDriver("1991"	,"bowlrama"	,"capbowl.java"	,rom_bowlrama,null	,machine_driver_bowlrama	,input_ports_capbowl	,null	,ROT270_16BIT	,	"P  P Marketing", "Bowl-O-Rama" )
}
