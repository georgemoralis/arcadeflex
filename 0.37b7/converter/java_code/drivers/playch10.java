/***************************************************************************

Playchoice 10 - (c) 1986 Nintendo of America

	Written by Ernesto Corvi.

	Portions of this code are heavily based on
	Brad Oliver's MESS implementation of the NES.

	Thanks to people that contributed to this driver, namely:
	- Brad Oliver, the NES guy.
	- Aaron Giles, the smart guy.
	- Al Kossow, da man!.

****************************************************************************

BIOS:
	Memory Map
	----------
	0000 - 3fff = Program ROM (8T)
	8000 - 87ff = RAM (8V)
	8800 - 8fff = RAM (8W)
	9000 - 97ff = SRAM (8R - Videoram)
	Cxxx = /INST ROM SEL
	Exxx = /IDSEL

	Input Ports
	-----------
	Read:
	- Port 0
	bit0 = CHSelect(?)
	bit1 = Enter button
	bit2 = Reset button
	bit3 = INTDETECT
	bit4 = N/C
	bit5 = Coin 2
	bit6 = Service button
	bit7 = Coin 1
	- Port 1 = Dipswitch 1
	- Port 2 = Dipswitch 2
	- Port 3 = /DETECTCLR

	Write: (always bit 0)
	- Port 0 = SDCS (ShareD CS)
	- Port 1 = /CNTRLMASK
	- Port 2 = /DISPMASK
	- Port 3 = /SOUNDMASK
	- Port 4 = /GAMERES
	- Port 5 = /GAMESTOP
	- Port 6 = N/C
	- Port 7 = N/C
	- Port 8 = NMI Enable
	- Port 9 = DOG DI
	- Port A = /PPURES
	- Port B = CSEL0 \
	- Port C = CSEL1  \ (Cartridge select: 0 to 9)
	- Port D = CSEL2  /
	- Port E = CSEL3 /
	- Port F = 8UP KEY

****************************************************************************

Working games:
--------------
	- 1942								(NF) - Standard board
	- Balloon Fight						(BF) - Standard board
	- Baseball							(BA) - Standard board
	- Captain Sky Hawk					(YW) - i board
	- Castlevania						(CV) - B board
	- Contra							(CT) - B board
	- Double Dragon						(WD) - F board
	- Double Dribble					(DW) - B board
	- Dr. Mario							(VU) - F board
	- Duck Hunt							(DH) - Standard board
	- Excite Bike						(EB) - Standard board
	- Fester's Quest					(EQ) - F board
	- Golf								(GF) - Standard board
	- Gradius							(GR) - A board
	- Hogan's Alley						(HA) - Standard board
	- Kung Fu							(SX) - Standard board
	- Mario Open Golf					(UG) - K board
	- Mega Man 3						(XU) - G board
	- Ninja Gaiden						(NG) - F board
	- Ninja Gaiden 3					(3N) - G board
	- Nintendo World Cup				(XZ) - G board
	- Power Blade						(7T) - G board
	- Pro Wrestling						(PW) - B board
	- Rad Racer							(RC) - D board
	- RC Pro Am							(PM) - F board
	- Rescue Rangers					(RU) - F board
	- Rockin' Kats						(7A) - G board
	- Rush N' Attack					(RA) - B board
	- Super C							(UE) - G board
	- Super Mario Bros					(SM) - Standard board
	- Super Mario Bros 2				(MW) - G board
	- Super Mario Bros 3				(UM) - G board
	- Teenage Mutant Ninja Turtles		(U2) - F board
	- Teenage Mutant Ninja Turtles 2	(2N) - G board
	- Tennis							(TE) - Standard board
	- The Goonies						(GN) - C board
	- Volley Ball						(VB) - Standard board
	- Wild Gunman						(WG) - Standard board
	- Yo Noid							(YC) - F board

Non working games due to mapper/nes emulation issues:
-----------------------------------------------------
	- Gauntlet							(GL) - G board
	- Mike Tyson's Punchout				(PT) - E board
	- Track & Field						(TR) - A board
	- Rygar								(RY) - B board
	- Rad Racer II						(QR) - G board

Non working games due to missing roms:
--------------------------------------
	- Tecmo Bowl						(TW) - F board

Non working games due to missing RP5H01 data:
---------------------------------------------
	- Baseball Stars					(B9) - F board
	- Metroid							(MT) - D board
	- Ninja Gaiden 2					(??) - ? board
	- Pinbot							(io) - H board
	- Solar Jetman						(LJ) - i board
	- Trojan							(??) - ? board

****************************************************************************

Notes & Todo:
-------------

- Fix Mike Tyson's Punchout gfx banking.
- Fix Track & Field. It requires you to press start after starting
  a game without displaying anything on screen. Bad rom?.
- Look at Ninja Gaiden 3. It has some slight timming issues on the
second level. Probably related to the mapper's irq timming.
- Fix Rad Racer II. More timming issues.
- Dipswitches
- Better control layout?. This thing has odd buttons.
- Find dumps of the rest of the RP5H01's and add the remaining games.
- Any PPU optimizations that retain accuracy are certainly welcome.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class playch10
{
	
	/* clock frequency */
	#define N2A03_DEFAULTCLOCK (21477272.724 / 12)
	
	/* from vidhrdw */
	extern extern extern extern 
	/* from machine */
	extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern extern int pc10_sdcs;
	extern int pc10_nmi_enable;
	extern int pc10_dog_di;
	
	/******************************************************************************/
	
	/* local stuff */
	static UINT8 *work_ram, *ram_8w;
	static int up_8w;
	
	public static WriteHandlerPtr up8w_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		up_8w = data & 1;
	} };
	
	public static ReadHandlerPtr ram_8w_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		if ( offset >= 0x400 && up_8w )
			return ram_8w[offset];
	
		return ram_8w[offset & 0x3ff];
	} };
	
	public static WriteHandlerPtr ram_8w_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ( offset >= 0x400 && up_8w )
			ram_8w[offset] = data;
		else
			ram_8w[offset & 0x3ff] = data;
	} };
	
	
	public static WriteHandlerPtr video_w = new WriteHandlerPtr() {public void handler(int offset, int data) {
		/* only write to videoram when allowed */
		if (pc10_sdcs != 0)
			videoram_w( offset, data );
	} };
	
	public static ReadHandlerPtr mirror_ram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return work_ram[ offset & 0x7ff ];
	} };
	
	public static WriteHandlerPtr mirror_ram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		work_ram[ offset & 0x7ff ] = data;
	} };
	
	public static WriteHandlerPtr sprite_dma_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int source = ( data & 7 ) * 0x100;
	
		ppu2c03b_spriteram_dma( 0, &work_ram[source] );
	} };
	
	static public static nvramPtr nvram_handler  = new nvramPtr() { public void handler(Object file, int read_or_write) 
	{
		UINT8 *mem = memory_region( REGION_CPU2 ) + 0x6000;
	
		if (read_or_write != 0)
			osd_fwrite( file, mem, 0x1000 );
		else if (file != 0)
			osd_fread( file, mem, 0x1000 );
		else
			memset(mem, 0, 0x1000);
	} };
	
	/******************************************************************************/
	
	/* BIOS */
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0x87ff, MRA_RAM ),	/* 8V */
		new MemoryReadAddress( 0x8800, 0x8fff, ram_8w_r ),	/* 8W */
		new MemoryReadAddress( 0x9000, 0x97ff, videoram_r ),
		new MemoryReadAddress( 0xc000, 0xdfff, MRA_ROM ),
		new MemoryReadAddress( 0xe000, 0xffff, pc10_prot_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x3fff, MWA_ROM ),
		new MemoryWriteAddress( 0x8000, 0x87ff, MWA_RAM ), /* 8V */
		new MemoryWriteAddress( 0x8800, 0x8fff, ram_8w_w, ram_8w ), /* 8W */
		new MemoryWriteAddress( 0x9000, 0x97ff, video_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0xc000, 0xdfff, MWA_ROM ),
		new MemoryWriteAddress( 0xe000, 0xffff, pc10_prot_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static IOReadPort readport[] =
	{
		new IOReadPort( 0x00, 0x00, pc10_port_0_r ),	/* coins, service */
		new IOReadPort( 0x01, 0x01, input_port_1_r ),	/* dipswitch 1 */
		new IOReadPort( 0x02, 0x02, input_port_2_r ), /* dipswitch 2 */
		new IOReadPort( 0x03, 0x03, pc10_detectclr_r ),
		new IOReadPort( -1 )  /* end of table */
	};
	
	static IOWritePort writeport[] =
	{
		new IOWritePort( 0x00, 0x00, pc10_SDCS_w ),
		new IOWritePort( 0x01, 0x01, pc10_CNTRLMASK_w ),
		new IOWritePort( 0x02, 0x02, pc10_DISPMASK_w ),
		new IOWritePort( 0x03, 0x03, pc10_SOUNDMASK_w ),
		new IOWritePort( 0x04, 0x04, pc10_GAMERES_w ),
		new IOWritePort( 0x05, 0x05, pc10_GAMESTOP_w ),
		new IOWritePort( 0x06, 0x07, IOWP_NOP ),
		new IOWritePort( 0x08, 0x08, pc10_NMIENABLE_w ),
		new IOWritePort( 0x09, 0x09, pc10_DOGDI_w ),
		new IOWritePort( 0x0a, 0x0a, pc10_PPURES_w ),
		new IOWritePort( 0x0b, 0x0e, pc10_CARTSEL_w ),
		new IOWritePort( 0x0f, 0x0f, up8w_w ),
		new IOWritePort( -1 )  /* end of table */
	};
	
	/* Cart */
	static MemoryReadAddress cart_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x07ff, MRA_RAM ),
		new MemoryReadAddress( 0x0800, 0x1fff, mirror_ram_r ),
		new MemoryReadAddress( 0x2000, 0x3fff, ppu2c03b_0_r ),
		new MemoryReadAddress( 0x4000, 0x4015, NESPSG_0_r ),
		new MemoryReadAddress( 0x4016, 0x4016, pc10_in0_r ),
		new MemoryReadAddress( 0x4017, 0x4017, pc10_in1_r ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress cart_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x07ff, MWA_RAM, work_ram ),
		new MemoryWriteAddress( 0x0800, 0x1fff, mirror_ram_w ),
		new MemoryWriteAddress( 0x2000, 0x3fff, ppu2c03b_0_w ),
		new MemoryWriteAddress( 0x4011, 0x4011, DAC_0_data_w ),
		new MemoryWriteAddress( 0x4014, 0x4014, sprite_dma_w ),
		new MemoryWriteAddress( 0x4000, 0x4015, NESPSG_0_w ),
		new MemoryWriteAddress( 0x4016, 0x4016, pc10_in0_w ),
		new MemoryWriteAddress( 0x4017, 0x4017, MWA_NOP ), /* in 1 writes ignored */
		new MemoryWriteAddress( 0x8000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	/******************************************************************************/
	
	static InputPortPtr input_ports_playch10 = new InputPortPtr(){ public void handler() { 
    PORT_START(); 	/* These are the BIOS buttons */
	    PORT_BITX(0x01, IP_ACTIVE_HIGH, IPT_SERVICE2, "Channel Select", KEYCODE_9, IP_JOY_NONE );/* CHSelect 		*/
	    PORT_BITX(0x02, IP_ACTIVE_HIGH, IPT_SERVICE3, "Enter", KEYCODE_0, IP_JOY_NONE );			/* Enter button 	*/
	    PORT_BITX(0x04, IP_ACTIVE_HIGH, IPT_SERVICE4, "Reset", KEYCODE_MINUS, IP_JOY_NONE );		/* Reset button 	*/
	    PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNUSED );											/* INT Detect		*/
	    PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNUSED );											/* N/C				*/
	    PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_COIN2 );												/* Coin 2			*/
	    PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_SERVICE1 );											/* Service button	*/
	    PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_COIN1 );												/* Coin 1			*/
	
	    PORT_START(); 	/* DSW A */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_SERVICE( 0x80, IP_ACTIVE_HIGH );
		PORT_START();  /* DSW B */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 );	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 );	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 );/* select button - masked */
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_START2 );/* start button - masked */
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP );	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN );	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT );	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT );
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNUSED );/* wired to 1p select button */
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNUSED );/* wired to 1p start button */
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_PLAYER2 );	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
		PORT_START(); 	/* IN2 - FAKE - Gun X pos */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_X, 70, 30, 0, 255 );
		PORT_START(); 	/* IN3 - FAKE - Gun Y pos */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_Y, 50, 30, 0, 255 );INPUT_PORTS_END(); }}; 
	
	
	static GfxLayout bios_charlayout = new GfxLayout
	(
	    8,8,    /* 8*8 characters */
	    1024,   /* 1024 characters */
	    3,      /* 3 bits per pixel */
	    new int[] { 0, 0x2000*8, 0x4000*8 },     /* the bitplanes are separated */
	    new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
	    new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
	    8*8     /* every char takes 8 consecutive bytes */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, bios_charlayout,   0,  32 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	public static InterruptPtr playch10_interrupt = new InterruptPtr() { public int handler()  {
	
		/* LS161A, Sheet 1 - bottom left of Z80 */
		if ( !pc10_dog_di && !pc10_nmi_enable ) {
			cpu_set_reset_line( 0, PULSE_LINE );
			return ignore_interrupt();
		}
	
		if (pc10_nmi_enable != 0)
			return nmi_interrupt();
	
		return ignore_interrupt();
	} };
	
	static NESinterface nes_interface = new NESinterface
	(
		1,
		new int[] { REGION_CPU2 },
		new int[] { 50 },
	);
	
	static DACinterface nes_dac_interface = new DACinterface
	(
		1,
		new int[] { 50 },
	);
	
	
	#define PC10_MACHINE_DRIVER( name, nvram )								\
	static MachineDriver machine_driver_##name = new MachineDriver\
	(																		\
		/* basic machine hardware */										\
		new MachineCPU[] {																	\
			new MachineCPU(																\
				CPU_Z80,													\
				8000000 / 2,        /* 8 MHz / 2 */							\
				readmem,writemem,readport,writeport,						\
				playch10_interrupt, 1										\
			),																\
			new MachineCPU(																\
				CPU_N2A03,													\
				N2A03_DEFAULTCLOCK,											\
				cart_readmem, cart_writemem, null, null,							\
				ignore_interrupt, 0											\
			)																\
		},																	\
		60, ( ( ( 1.0 / 60.0 ) * 1000000.0 ) / 262 ) * ( 262 - 239 ),  /* fps, vblank duration */	\
		1,	/* cpus dont talk to each other */								\
		pc10_init_machine,													\
																			\
		/* video hardware */												\
		32*8, 30*8*2, new rectangle( 0*8, 32*8-1, 0*8, 30*8*2-1 ),						\
		gfxdecodeinfo,														\
		256+4*16, 256+4*8,													\
		playch10_vh_convert_color_prom,										\
																			\
		VIDEO_TYPE_RASTER | VIDEO_DUAL_MONITOR,								\
		null,																	\
		playch10_vh_start,													\
		playch10_vh_stop,													\
		playch10_vh_screenrefresh,											\
																			\
		/* sound hardware */												\
		0,0,0,0,															\
		new MachineSound[] {																	\
			new MachineSound(																\
				SOUND_NES,													\
				nes_interface												\
			),																\
			new MachineSound(																\
				SOUND_DAC,													\
				nes_dac_interface											\
			)																\
		},																	\
		nvram																\
	);
	
	PC10_MACHINE_DRIVER( playch10, NULL )
	PC10_MACHINE_DRIVER( playchnv, nvram_handler )
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	#define BIOS_CPU											\
		ROM_REGION( 0x10000, REGION_CPU1 );					\
	    ROM_LOAD( "pch1-c.8t",    0x00000, 0x4000, 0xd52fa07a );
	#define BIOS_GFX											\
		ROM_REGION( 0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE );\
		ROM_LOAD( "pch1-c.8p",    0x00000, 0x2000, 0x30c15e23 );\
	    ROM_LOAD( "pch1-c.8m",    0x02000, 0x2000, 0xc1232eee );\
	    ROM_LOAD( "pch1-c.8k",    0x04000, 0x2000, 0x9acffb30 );\
	    ROM_REGION( 0x0300, REGION_PROMS );					\
	    ROM_LOAD( "82s129.6f",    0x0000, 0x0100, 0xe5414ca3 );\
	    ROM_LOAD( "82s129.6e",    0x0100, 0x0100, 0xa2625c6e );\
	    ROM_LOAD( "82s129.6d",    0x0200, 0x0100, 0x1213ebd4 );
	/******************************************************************************/
	
	/* Standard Games */
	static RomLoadPtr rom_pc_smb = new RomLoadPtr(){ public void handler(){ 		/* Super Mario Bros. */
		BIOS_CPU
		ROM_LOAD( "u3sm",    0x0c000, 0x2000, 0x4b5f717d );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x10000, REGION_CPU2 ); /* 64k for code */
	    ROM_LOAD( "u1sm",    0x08000, 0x8000, 0x5cf548d3 );
	    ROM_REGION( 0x02000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "u2sm",    0x00000, 0x2000, 0x867b51ad );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0xbd82d775 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_ebike = new RomLoadPtr(){ public void handler(){ 	/* Excite Bike */
		BIOS_CPU
		ROM_LOAD( "u3eb",    0x0c000, 0x2000, 0x8ff0e787 );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x10000, REGION_CPU2 ); /* 64k for code */
	    ROM_LOAD( "u1eb",    0x0c000, 0x4000, 0x3a94fa0b );
	    ROM_REGION( 0x02000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "u2eb",    0x00000, 0x2000, 0xe5f72401 );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0xa0263750 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_1942 = new RomLoadPtr(){ public void handler(){ 	/* 1942 */
		BIOS_CPU
		ROM_LOAD( "u3",      0x0c000, 0x2000, 0x415b8807 );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x10000, REGION_CPU2 ); /* 64k for code */
	    ROM_LOAD( "u1",    	 0x08000, 0x8000, 0xc4e8c04a );
	    ROM_REGION( 0x02000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "u2",		 0x00000, 0x2000, 0x03379b76 );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0x29893c7f );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_bfght = new RomLoadPtr(){ public void handler(){ 	/* Balloon Fight */
		BIOS_CPU
		ROM_LOAD( "bf-u3",   0x0c000, 0x2000, 0xa9949544 );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x10000, REGION_CPU2 ); /* 64k for code */
	    ROM_LOAD( "bf-u1",   0x0c000, 0x4000, 0x575ed2fe );
	    ROM_REGION( 0x02000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "bf-u2",	 0x00000, 0x2000, 0xc642a1df );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0xbe3c42fb );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_bball = new RomLoadPtr(){ public void handler(){ 	/* Baseball */
		BIOS_CPU
		ROM_LOAD( "ba-u3",   0x0c000, 0x2000, 0x06861a0d );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x10000, REGION_CPU2 ); /* 64k for code */
	    ROM_LOAD( "ba-u1",   0x0c000, 0x4000, 0x39d1fa03 );
	    ROM_REGION( 0x02000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "ba-u2",	 0x00000, 0x2000, 0xcde71b82 );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0x7940cfc4 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_golf = new RomLoadPtr(){ public void handler(){ 	/* Golf */
		BIOS_CPU
		ROM_LOAD( "gf-u3",   0x0c000, 0x2000, 0x882dea87 );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x10000, REGION_CPU2 ); /* 64k for code */
	    ROM_LOAD( "gf-u1",   0x0c000, 0x4000, 0xf9622bfa );
	    ROM_REGION( 0x02000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "gf-u2",	 0x00000, 0x2000, 0xff6fc790 );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0x2cd98ef6 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_kngfu = new RomLoadPtr(){ public void handler(){ 	/* Kung Fu */
		BIOS_CPU
		ROM_LOAD( "sx-u3",   0x0c000, 0x2000, 0xead71b7e );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x10000, REGION_CPU2 ); /* 64k for code */
	    ROM_LOAD( "sx-u1",   0x08000, 0x8000, 0x0516375e );
	    ROM_REGION( 0x02000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "sx-u2",	 0x00000, 0x2000, 0x430b49a4 );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0xa1687f01 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_tenis = new RomLoadPtr(){ public void handler(){ 	/* Tennis */
		BIOS_CPU
		ROM_LOAD( "te-u3",   0x0c000, 0x2000, 0x6928e920 );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x10000, REGION_CPU2 ); /* 64k for code */
	    ROM_LOAD( "te-u1",   0x0c000, 0x4000, 0x8b2e3e81 );
	    ROM_REGION( 0x02000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "te-u2",	 0x00000, 0x2000, 0x3a34c45b );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0xbcc9a48e );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_vball = new RomLoadPtr(){ public void handler(){ 	/* Volley Ball */
		BIOS_CPU
		ROM_LOAD( "vb-u3",   0x0c000, 0x2000, 0x9104354e );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x10000, REGION_CPU2 ); /* 64k for code */
	    ROM_LOAD( "vb-u1",   0x08000, 0x8000, 0x35226b99 );
	    ROM_REGION( 0x02000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "vb-u2",	 0x00000, 0x2000, 0x2415dce2 );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0xf701863f );ROM_END(); }}; 
	
	/* Gun Games */
	static RomLoadPtr rom_pc_duckh = new RomLoadPtr(){ public void handler(){ 	/* Duck Hunt */
		BIOS_CPU
		ROM_LOAD( "u3",      0x0c000, 0x2000, 0x2f9ec5c6 );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x10000, REGION_CPU2 ); /* 64k for code */
	    ROM_LOAD( "u1",      0x0c000, 0x4000, 0x90ca616d );
	    ROM_REGION( 0x04000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "u2",      0x00000, 0x2000, 0x4e049e03 );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0x8cd6aad6 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_hgaly = new RomLoadPtr(){ public void handler(){ 	/* Hogan's Alley */
		BIOS_CPU
		ROM_LOAD( "ha-u3",   0x0c000, 0x2000, 0xa2525180 );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x10000, REGION_CPU2 ); /* 64k for code */
	    ROM_LOAD( "ha-u1",   0x0c000, 0x4000, 0x8963ae6e );
	    ROM_REGION( 0x04000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "ha-u2",   0x00000, 0x2000, 0x5df42fc4 );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0x5ac61521 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_wgnmn = new RomLoadPtr(){ public void handler(){ 	/* Wild Gunman */
		BIOS_CPU
		ROM_LOAD( "wg-u3",   0x0c000, 0x2000, 0xda08afe5 );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x10000, REGION_CPU2 ); /* 64k for code */
	    ROM_LOAD( "wg-u1",   0x0c000, 0x4000, 0x389960db );
	    ROM_REGION( 0x04000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "wg-u2",   0x00000, 0x2000, 0xa5e04856 );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0xdef015a3 );ROM_END(); }}; 
	
	/* A-Board Games */
	static RomLoadPtr rom_pc_tkfld = new RomLoadPtr(){ public void handler(){ 	/* Track & Field */
		BIOS_CPU
		ROM_LOAD( "u4tr",    0x0c000, 0x2000, 0x70184fd7 );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x10000, REGION_CPU2 ); /* 64k for code */
	    ROM_LOAD( "u2tr",    0x08000, 0x8000, 0xd7961e01 );
	    ROM_REGION( 0x08000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "u3tr",    0x00000, 0x8000, 0x03bfbc4b );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0x1e2e7f1e );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_grdus = new RomLoadPtr(){ public void handler(){ 	/* Gradius */
		BIOS_CPU
		ROM_LOAD( "gr-u4",   0x0c000, 0x2000, 0x27d76160 );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x10000, REGION_CPU2 ); /* 64k for code */
	    ROM_LOAD( "gr-u2",   0x08000, 0x8000, 0xaa96889c );
	    ROM_REGION( 0x08000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "gr-u3",   0x00000, 0x8000, 0xde963bec );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0xb8d5bf8a );ROM_END(); }}; 
	
	/* B-Board Games */
	static RomLoadPtr rom_pc_rnatk = new RomLoadPtr(){ public void handler(){ 	/* Rush N' Attack */
		BIOS_CPU
		ROM_LOAD( "ra-u4",   0x0c000, 0x2000, 0xebab7f8c );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x30000, REGION_CPU2 ); /* 64k for code */
	    ROM_LOAD( "ra-u1",   0x10000, 0x10000, 0x5660b3a6 );/* banked */
	    ROM_LOAD( "ra-u2",   0x20000, 0x10000, 0x2a1bca39 );/* banked */
	
		/* No cart gfx - uses vram */
	
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0x1f6596b2 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_cntra = new RomLoadPtr(){ public void handler(){ 	/* Contra */
		BIOS_CPU
		ROM_LOAD( "u4ct",    0x0c000, 0x2000, 0x431486cf );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x30000, REGION_CPU2 ); /* 64k for code */
	    ROM_LOAD( "u1ct",    0x10000, 0x10000, 0x9fcc91d4 );/* banked */
	    ROM_LOAD( "u2ct",    0x20000, 0x10000, 0x612ad51d );/* banked */
	
		/* No cart gfx - uses vram */
	
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0x8ab3977a );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_pwrst = new RomLoadPtr(){ public void handler(){ 	/* Pro Wrestling */
		BIOS_CPU
		ROM_LOAD( "pw-u4",   0x0c000, 0x2000, 0x0f03d71b );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x30000, REGION_CPU2 ); /* 64k for code */
	    ROM_LOAD( "pw-u1",   0x10000, 0x08000, 0x6242c2ce );/* banked */
	    ROM_RELOAD(			 0x18000, 0x08000 );    ROM_LOAD( "pw-u2",   0x20000, 0x10000, 0xef6aa17c );/* banked */
	
		/* No cart gfx - uses vram */
	
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0x4c6b7983 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_cvnia = new RomLoadPtr(){ public void handler(){ 	/* Castlevania */
		BIOS_CPU
		ROM_LOAD( "u4cv",    0x0c000, 0x2000, 0xa2d4245d );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x30000, REGION_CPU2 ); /* 64k for code */
	    ROM_LOAD( "u1cv",    0x10000, 0x10000, 0xadd4fc52 );/* banked */
	    ROM_LOAD( "u2cv",    0x20000, 0x10000, 0x7885e567 );/* banked */
	
		/* No cart gfx - uses vram */
	
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0x7da2f045 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_dbldr = new RomLoadPtr(){ public void handler(){ 	/* Double Dribble */
		BIOS_CPU
		ROM_LOAD( "dw-u4",    0x0c000, 0x2000, 0x5006eef8 );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x30000, REGION_CPU2 ); /* 64k for code */
	    ROM_LOAD( "dw-u1",    0x10000, 0x10000, 0x78e08e61 );/* banked */
	    ROM_LOAD( "dw-u2",    0x20000, 0x10000, 0xab554cde );/* banked */
	
		/* No cart gfx - uses vram */
	
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0x9b5f4bd2 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_rygar = new RomLoadPtr(){ public void handler(){ 	/* Rygar */
		BIOS_CPU
		ROM_LOAD( "ry-u4",    0x0c000, 0x2000, 0x7149071b );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x30000, REGION_CPU2 ); /* 64k for code */
	    ROM_LOAD( "ry-u1",    0x10000, 0x10000, 0xaa2e54bc );/* banked */
	    ROM_LOAD( "ry-u2",    0x20000, 0x10000, 0x80cb158b );/* banked */
	
		/* No cart gfx - uses vram */
	
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0xb69309ab );ROM_END(); }}; 
	
	/* C-Board Games */
	static RomLoadPtr rom_pc_goons = new RomLoadPtr(){ public void handler(){ 	/* The Goonies */
		BIOS_CPU
		ROM_LOAD( "gn-u3",   0x0c000, 0x2000, 0x33adedd2 );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x10000, REGION_CPU2 ); /* 64k for code */
	    ROM_LOAD( "gn-u1",   0x08000, 0x8000, 0xefeb0c34 );
	    ROM_REGION( 0x04000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "gn-u2",   0x00000, 0x4000, 0x0f9c7f49 );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0xcdd62d08 );ROM_END(); }}; 
	
	/* D-Board Games */
	static RomLoadPtr rom_pc_radrc = new RomLoadPtr(){ public void handler(){ 	/* Rad Racer */
		BIOS_CPU
		ROM_LOAD( "rc-u5",   0x0c000, 0x2000, 0xae60fd08 );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x30000, REGION_CPU2 ); /* 64k for code */
	    ROM_LOAD( "rc-u1",   0x10000, 0x10000, 0xdce369a7 );    ROM_LOAD( "rc-u2",   0x20000, 0x10000, 0x389a79b5 );/* banked */
	
		/* No cart gfx - uses vram */
	
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0x17c880f9 );ROM_END(); }}; 
	
	/* E-Board Games */
	static RomLoadPtr rom_pc_miket = new RomLoadPtr(){ public void handler(){ 	/* Mike Tyson's Punchout */
		BIOS_CPU
		ROM_LOAD( "u5pt",    0x0c000, 0x2000, 0xb434e567 );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x30000, REGION_CPU2 ); /* 64k for code */
	    ROM_LOAD( "u1pt",    0x10000, 0x20000, 0xdfd9a2ee );/* banked */
	
	    ROM_REGION( 0x20000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "u3pt",    0x00000, 0x20000, 0x570b48ea );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0x60f7ea1d );ROM_END(); }}; 
	
	/* F-Board Games */
	static RomLoadPtr rom_pc_ngaid = new RomLoadPtr(){ public void handler(){ 	/* Ninja Gaiden */
		BIOS_CPU
		ROM_LOAD( "u2ng",    0x0c000, 0x2000, 0x7505de96 );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x30000, REGION_CPU2 ); /* 64k for code */
		ROM_LOAD( "u4ng",    0x10000, 0x20000, 0x5f1e7b19 );/* banked */
	
	    ROM_REGION( 0x020000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "u1ng",   0x00000, 0x20000, 0xeccd2dcb );/* banked */
	
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0xec5641d6 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_ddrgn = new RomLoadPtr(){ public void handler(){ 	/* Double Dragon */
		BIOS_CPU
		ROM_LOAD( "wd-u2",   0x0c000, 0x2000, 0xdfca1578 );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x30000, REGION_CPU2 ); /* 64k for code */
		ROM_LOAD( "wd-u4",  0x10000, 0x20000, 0x05c97f64 );/* banked */
	
	    ROM_REGION( 0x020000, REGION_GFX2 );/* cart gfx */
		ROM_LOAD( "wd-u1",  0x00000, 0x20000, 0x5ebe0fd0 );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0xf9739d62 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_drmro = new RomLoadPtr(){ public void handler(){ 	/* Dr Mario */
		BIOS_CPU
		ROM_LOAD( "vu-u2",   0x0c000, 0x2000, 0x4b7869ac );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x30000, REGION_CPU2 ); /* 64k for code */
		ROM_LOAD( "vu-u4",  0x10000, 0x08000, 0xcb02a930 );/* banked */
		ROM_RELOAD(			0x18000, 0x08000 );	ROM_RELOAD(			0x20000, 0x08000 );	ROM_RELOAD(			0x28000, 0x08000 );
	    ROM_REGION( 0x020000, REGION_GFX2 );/* cart gfx */
		ROM_LOAD( "vu-u1",  0x00000, 0x08000, 0x064d4ab3 );	ROM_RELOAD(			0x08000, 0x08000 );	ROM_RELOAD(			0x10000, 0x08000 );	ROM_RELOAD(			0x18000, 0x08000 );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0x1b26e58c );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_ftqst = new RomLoadPtr(){ public void handler(){ 	/* Fester's Quest */
		BIOS_CPU
		ROM_LOAD( "eq-u2",   0x0c000, 0x2000, 0x85326040 );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x30000, REGION_CPU2 ); /* 64k for code */
		ROM_LOAD( "eq-u4",  0x10000, 0x20000, 0x953a3eaf );/* banked */
	
	    ROM_REGION( 0x020000, REGION_GFX2 );/* cart gfx */
		ROM_LOAD( "eq-u1",  0x00000, 0x20000, 0x0ca17ab5 );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0x1c601cd7 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_rcpam = new RomLoadPtr(){ public void handler(){ 	/* RC Pro Am */
		BIOS_CPU
		ROM_LOAD( "pm-u2",   0x0c000, 0x2000, 0x358c2de7 );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x30000, REGION_CPU2 ); /* 64k for code */
		ROM_LOAD( "pm-u4",  0x10000, 0x08000, 0x82cfde25 );/* banked */
		ROM_RELOAD(			0x18000, 0x08000 );	ROM_RELOAD(			0x20000, 0x08000 );	ROM_RELOAD(			0x28000, 0x08000 );
	    ROM_REGION( 0x020000, REGION_GFX2 );/* cart gfx */
		ROM_LOAD( "pm-u1",  0x00000, 0x08000, 0x83c90d47 );	ROM_RELOAD(			0x08000, 0x08000 );	ROM_RELOAD(			0x10000, 0x08000 );	ROM_RELOAD(			0x18000, 0x08000 );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0xd71d8085 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_rrngr = new RomLoadPtr(){ public void handler(){ 	/* Rescue Rangers */
		BIOS_CPU
		ROM_LOAD( "ru-u2",   0x0c000, 0x2000, 0x2a4bfc4b );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x30000, REGION_CPU2 ); /* 64k for code */
		ROM_LOAD( "ru-u4",  0x10000, 0x20000, 0x02931525 );/* banked */
	
	    ROM_REGION( 0x020000, REGION_GFX2 );/* cart gfx */
		ROM_LOAD( "ru-u1",  0x00000, 0x20000, 0x218d4224 );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0x1c2e1865 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_ynoid = new RomLoadPtr(){ public void handler(){ 	/* Yo! Noid */
		BIOS_CPU
		ROM_LOAD( "yc-u2",   0x0c000, 0x2000, 0x0449805c );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x30000, REGION_CPU2 ); /* 64k for code */
		ROM_LOAD( "yc-u4",  0x10000, 0x20000, 0x4affeee7 );/* banked */
	
	    ROM_REGION( 0x020000, REGION_GFX2 );/* cart gfx */
		ROM_LOAD( "yc-u1",  0x00000, 0x20000, 0x868f7343 );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0x8c376465 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_tmnt = new RomLoadPtr(){ public void handler(){ 	/* Teenage Mutant Ninja Turtles */
		BIOS_CPU
		ROM_LOAD( "u2u2",   0x0c000, 0x2000, 0xbdce58c0 );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x30000, REGION_CPU2 ); /* 64k for code */
		ROM_LOAD( "u4u2",   0x10000, 0x20000, 0x0ccd28d5 );/* banked */
	
	    ROM_REGION( 0x020000, REGION_GFX2 );/* cart gfx */
		ROM_LOAD( "u1u2",   0x00000, 0x20000, 0x91f01f53 );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0xf5a38e98 );ROM_END(); }}; 
	
	/* G-Board Games */
	static RomLoadPtr rom_pc_smb3 = new RomLoadPtr(){ public void handler(){ 	/* Super Mario Bros 3 */
		BIOS_CPU
		ROM_LOAD( "u3um",    0x0c000, 0x2000, 0x45e92f7f );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x50000, REGION_CPU2 ); /* 64k for code */
		ROM_LOAD( "u4um",    0x10000, 0x20000, 0x590b4d7c );/* banked */
		ROM_LOAD( "u5um",    0x30000, 0x20000, 0xbce25425 );/* banked */
	
	    ROM_REGION( 0x020000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "u1um",    0x00000, 0x20000, 0xc2928c49 );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0xe48f4945 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_gntlt = new RomLoadPtr(){ public void handler(){ 	/* Gauntlet */
		BIOS_CPU
		ROM_LOAD( "u3gl",    0x0c000, 0x2000, 0x57575b92 );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x50000, REGION_CPU2 ); /* 64k for code */
		ROM_LOAD( "gl-0.prg",0x10000, 0x20000, 0xb19c48a5 );/* banked */
		ROM_RELOAD(			 0x30000, 0x20000 );
	    ROM_REGION( 0x010000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "gl-0.chr", 0x00000, 0x10000, 0x22af8849 );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0xba7f2e13 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_pwbld = new RomLoadPtr(){ public void handler(){ 	/* Power Blade */
		BIOS_CPU
		ROM_LOAD( "7t-u3",    0x0c000, 0x2000, 0xedcc21c6 );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x50000, REGION_CPU2 ); /* 64k for code */
		ROM_LOAD( "7t-u5",   0x10000, 0x20000, 0xfaa957b1 );/* banked */
		ROM_RELOAD(			 0x30000, 0x20000 );
	    ROM_REGION( 0x020000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "7t-u1",    0x00000, 0x20000, 0x344be4a6 );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0x31a05a48 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_ngai3 = new RomLoadPtr(){ public void handler(){ 	/* Ninja Gaiden 3 */
		BIOS_CPU
		ROM_LOAD( "u33n",    0x0c000, 0x2000, 0xc7ba0f59 );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x50000, REGION_CPU2 ); /* 64k for code */
		ROM_LOAD( "u53n",    0x10000, 0x20000, 0xf0c77dcb );/* banked */
		ROM_RELOAD(			 0x30000, 0x20000 );
	    ROM_REGION( 0x020000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "u13n",    0x00000, 0x20000, 0x584bcf5d );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0x13755943 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_radr2 = new RomLoadPtr(){ public void handler(){ 	/* Rad Racer II */
		BIOS_CPU
		ROM_LOAD( "qr-u3",    0x0c000, 0x2000, 0x0c8fea63 );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x50000, REGION_CPU2 ); /* 64k for code */
		ROM_LOAD( "qr-u5",    0x10000, 0x10000, 0xab90e397 );/* banked */
		ROM_RELOAD(			  0x20000, 0x10000 );	ROM_RELOAD(			  0x30000, 0x10000 );	ROM_RELOAD(			  0x40000, 0x10000 );
	    ROM_REGION( 0x010000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "qr-u1",    0x00000, 0x10000, 0x07df55d8 );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0x40c4f294 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_rkats = new RomLoadPtr(){ public void handler(){ 	/* Rockin' Kats */
		BIOS_CPU
		ROM_LOAD( "7a-u3",    0x0c000, 0x2000, 0x352b1e3c );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x50000, REGION_CPU2 ); /* 64k for code */
		ROM_LOAD( "7a-u5",    0x10000, 0x20000, 0x319ccfcc );/* banked */
		ROM_RELOAD(			  0x30000, 0x20000 );
	    ROM_REGION( 0x020000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "7a-u1",    0x00000, 0x20000, 0x487aa440 );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0x56ab5bf9 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_suprc = new RomLoadPtr(){ public void handler(){ 	/* Super C */
		BIOS_CPU
		ROM_LOAD( "ue-u3",    0x0c000, 0x2000, 0xa30ca248 );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x50000, REGION_CPU2 ); /* 64k for code */
		ROM_LOAD( "ue-u5",    0x10000, 0x20000, 0xc7fbecc3 );/* banked */
		ROM_RELOAD(			  0x30000, 0x20000 );
	    ROM_REGION( 0x020000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "ue-u1",    0x00000, 0x20000, 0x153295c1 );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0xd477095e );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_tmnt2 = new RomLoadPtr(){ public void handler(){ 	/* Teenage Mutant Ninja Turtles II */
		BIOS_CPU
		ROM_LOAD( "2n-u3",    0x0c000, 0x2000, 0x65298370 );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x50000, REGION_CPU2 ); /* 64k for code */
		ROM_LOAD( "2n-u5",    0x10000, 0x40000, 0x717e1c46 );/* banked */
	
	    ROM_REGION( 0x040000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "2n-u1",    0x00000, 0x40000, 0x0dbc575f );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0x237e8519 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_wcup = new RomLoadPtr(){ public void handler(){ 	/* Nintendo World Cup */
		BIOS_CPU
		ROM_LOAD( "xz-u3",    0x0c000, 0x2000, 0xc26cb22f );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x50000, REGION_CPU2 ); /* 64k for code */
		ROM_LOAD( "xz-u5",    0x10000, 0x20000, 0x314ee295 );/* banked */
		ROM_RELOAD(			  0x30000, 0x20000 );
	    ROM_REGION( 0x020000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "xz-u1",    0x00000, 0x20000, 0x92477d53 );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0xe17e1d76 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_mman3 = new RomLoadPtr(){ public void handler(){ 	/* Mega Man 3 */
		BIOS_CPU
		ROM_LOAD( "xu-u3",   0x0c000, 0x2000, 0xc3984e09 );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x50000, REGION_CPU2 ); /* 64k for code */
		ROM_LOAD( "xu-u4",   0x10000, 0x20000, 0x98a3263c );/* banked */
		ROM_LOAD( "xu-u5",   0x30000, 0x20000, 0xd365647a );/* banked */
	
	    ROM_REGION( 0x020000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "xu-u1",    0x00000, 0x20000, 0x4028916e );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0x0fe6e900 );ROM_END(); }}; 
	
	static RomLoadPtr rom_pc_smb2 = new RomLoadPtr(){ public void handler(){ 	/* Super Mario Bros 2 */
		BIOS_CPU
		ROM_LOAD( "mw-u3",   0x0c000, 0x2000, 0xbeaeb43a );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x50000, REGION_CPU2 ); /* 64k for code */
		ROM_LOAD( "mw-u5",   0x10000, 0x20000, 0x07854b3f );/* banked */
		ROM_RELOAD(			 0x30000, 0x20000 );
	    ROM_REGION( 0x020000, REGION_GFX2 );/* cart gfx */
	    ROM_LOAD( "mw-u1",    0x00000, 0x20000, 0xf2ba1170 );
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0x372f4e84 );ROM_END(); }}; 
	
	/* i-Board Games */
	static RomLoadPtr rom_pc_cshwk = new RomLoadPtr(){ public void handler(){ 	/* Captain Sky Hawk */
		BIOS_CPU
		ROM_LOAD( "yw-u3",   0x0c000, 0x2000, 0x9d988209 );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x30000, REGION_CPU2 ); /* 64k for code */
	    ROM_LOAD( "yw-u1",   0x10000, 0x20000, 0xa5e0208a );/* banked */
	
		/* No cart gfx - uses vram */
	
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0xeb1c794f );ROM_END(); }}; 
	
	/* K-Board Games */
	static RomLoadPtr rom_pc_moglf = new RomLoadPtr(){ public void handler(){ 	/* Mario Open Golf */
		BIOS_CPU
		ROM_LOAD( "ug-u2",   0x0c000, 0x2000, 0xe932fe2b );/* extra bios code for this game */
	    BIOS_GFX
	
	    ROM_REGION( 0x50000, REGION_CPU2 ); /* 64k for code */
	    ROM_LOAD( "ug-u4",   0x10000, 0x40000, 0x091a6a4c );/* banked */
	
		/* No cart gfx - uses vram */
	
	    ROM_REGION( 0x0100,  REGION_USER1 );/* rp5h01 data */
	    ROM_LOAD( "security.prm", 0x00000, 0x10, 0x633766d5 );ROM_END(); }}; 
	
	/***************************************************************************
	
	  BIOS driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_playch10 = new RomLoadPtr(){ public void handler(){ 
	    BIOS_CPU
		BIOS_GFX
	ROM_END(); }}; 
	
	/******************************************************************************/
	
	
	/* A dummy driver, so that the bios can be debugged, and to serve as */
	/* parent for the other drivers, so that we do not have to include */
	/* them in every zip file */
	public static GameDriver driver_playch10	   = new GameDriver("1986"	,"playch10"	,"playch10.java"	,rom_playch10,null	,machine_driver_playch10	,input_ports_playch10	,null	,ROT0	,	"Nintendo of America", "Playchoice-10", NOT_A_DRIVER )
	
	/******************************************************************************/
	
	/*    YEAR  NAME     PARENT	   MACHINE	 INPUT     INIT  	 MONITOR  */
	
	/* Standard Games */
	public static GameDriver driver_pc_tenis	   = new GameDriver("1983"	,"pc_tenis"	,"playch10.java"	,rom_pc_tenis,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_playch10	,ROT0	,	"Nintendo", "PlayChoice-10: Tennis" )
	public static GameDriver driver_pc_bball	   = new GameDriver("1984"	,"pc_bball"	,"playch10.java"	,rom_pc_bball,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_playch10	,ROT0	,	"Nintendo of America", "PlayChoice-10: Baseball" )
	public static GameDriver driver_pc_bfght	   = new GameDriver("1984"	,"pc_bfght"	,"playch10.java"	,rom_pc_bfght,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_playch10	,ROT0	,	"Nintendo", "PlayChoice-10: Balloon Fight" )
	public static GameDriver driver_pc_ebike	   = new GameDriver("1984"	,"pc_ebike"	,"playch10.java"	,rom_pc_ebike,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_playch10	,ROT0	,	"Nintendo", "PlayChoice-10: Excite Bike" )
	public static GameDriver driver_pc_golf	   = new GameDriver("1984"	,"pc_golf"	,"playch10.java"	,rom_pc_golf,driver_	,machine_driver_playch10	,input_ports_playch10	,init_playch10	,playch10	,	ROT0, "Nintendo", "PlayChoice-10: Golf" )
	public static GameDriver driver_pc_kngfu	   = new GameDriver("1984"	,"pc_kngfu"	,"playch10.java"	,rom_pc_kngfu,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_playch10	,ROT0	,	"Irem (Nintendo license)", "PlayChoice-10: Kung Fu" )
	public static GameDriver driver_pc_1942	   = new GameDriver("1985"	,"pc_1942"	,"playch10.java"	,rom_pc_1942,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pc_hrz	,ROT0	,	"Capcom", "PlayChoice-10: 1942" )
	public static GameDriver driver_pc_smb	   = new GameDriver("1985"	,"pc_smb"	,"playch10.java"	,rom_pc_smb,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_playch10	,ROT0	,	"Nintendo", "PlayChoice-10: Super Mario Bros." )
	public static GameDriver driver_pc_vball	   = new GameDriver("1986"	,"pc_vball"	,"playch10.java"	,rom_pc_vball,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_playch10	,ROT0	,	"Nintendo", "PlayChoice-10: Volley ball" )
	
	/* Gun Games */
	public static GameDriver driver_pc_duckh	   = new GameDriver("1984"	,"pc_duckh"	,"playch10.java"	,rom_pc_duckh,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pc_gun	,ROT0	,	"Nintendo", "PlayChoice-10: Duck Hunt" )
	public static GameDriver driver_pc_hgaly	   = new GameDriver("1984"	,"pc_hgaly"	,"playch10.java"	,rom_pc_hgaly,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pc_gun	,ROT0	,	"Nintendo", "PlayChoice-10: Hogan's Alley" )
	public static GameDriver driver_pc_wgnmn	   = new GameDriver("1984"	,"pc_wgnmn"	,"playch10.java"	,rom_pc_wgnmn,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pc_gun	,ROT0	,	"Nintendo", "PlayChoice-10: Wild Gunman" )
	
	/* A-Board Games */
	public static GameDriver driver_pc_grdus	   = new GameDriver("1986"	,"pc_grdus"	,"playch10.java"	,rom_pc_grdus,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pcaboard	,ROT0	,	"Konami", "PlayChoice-10: Gradius" )
	public static GameDriver driver_pc_tkfld	   = new GameDriver("1987"	,"pc_tkfld"	,"playch10.java"	,rom_pc_tkfld,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pcaboard	,ROT0	,	"Konami (Nintendo of America license)", "PlayChoice-10: Track & Field", GAME_NOT_WORKING )
	
	/* B-Board Games */
	public static GameDriver driver_pc_pwrst	   = new GameDriver("1986"	,"pc_pwrst"	,"playch10.java"	,rom_pc_pwrst,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pcbboard	,ROT0	,	"Nintendo", "PlayChoice-10: Pro Wrestling" )
	public static GameDriver driver_pc_cvnia	   = new GameDriver("1987"	,"pc_cvnia"	,"playch10.java"	,rom_pc_cvnia,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pcbboard	,ROT0	,	"Konami (Nintendo of America license)", "PlayChoice-10: Castlevania" )
	public static GameDriver driver_pc_dbldr	   = new GameDriver("1987"	,"pc_dbldr"	,"playch10.java"	,rom_pc_dbldr,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pcbboard	,ROT0	,	"Konami (Nintendo of America license)", "PlayChoice-10: Double Dribble" )
	public static GameDriver driver_pc_rnatk	   = new GameDriver("1987"	,"pc_rnatk"	,"playch10.java"	,rom_pc_rnatk,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pcbboard	,ROT0	,	"Konami (Nintendo of America license)", "PlayChoice-10: Rush N' Attack" )
	public static GameDriver driver_pc_rygar	   = new GameDriver("1987"	,"pc_rygar"	,"playch10.java"	,rom_pc_rygar,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pcbboard	,ROT0	,	"Tecmo (Nintendo of America license)", "PlayChoice-10: Rygar" )
	public static GameDriver driver_pc_cntra	   = new GameDriver("1988"	,"pc_cntra"	,"playch10.java"	,rom_pc_cntra,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pcbboard	,ROT0	,	"Konami (Nintendo of America license)", "PlayChoice-10: Contra" )
	
	/* C-Board Games */
	public static GameDriver driver_pc_goons	   = new GameDriver("1986"	,"pc_goons"	,"playch10.java"	,rom_pc_goons,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pccboard	,ROT0	,	"Konami", "PlayChoice-10: The Goonies" )
	
	/* D-Board Games */
	public static GameDriver driver_pc_radrc	   = new GameDriver("1987"	,"pc_radrc"	,"playch10.java"	,rom_pc_radrc,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pcdboard	,ROT0	,	"Square", "PlayChoice-10: Rad Racer" )
	
	/* E-Board Games */
	public static GameDriver driver_pc_miket	   = new GameDriver("1987"	,"pc_miket"	,"playch10.java"	,rom_pc_miket,driver_playch10	,machine_driver_playchnv	,input_ports_playch10	,init_pceboard	,ROT0	,	"Nintendo", "PlayChoice-10: Mike Tyson's Punchout", GAME_NOT_WORKING )
	
	/* F-Board Games */
	public static GameDriver driver_pc_rcpam	   = new GameDriver("1987"	,"pc_rcpam"	,"playch10.java"	,rom_pc_rcpam,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pcfboard	,ROT0	,	"Rare", "PlayChoice-10: RC Pro Am" )
	public static GameDriver driver_pc_ngaid	   = new GameDriver("1989"	,"pc_ngaid"	,"playch10.java"	,rom_pc_ngaid,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pcfboard	,ROT0	,	"Tecmo (Nintendo of America license)", "PlayChoice-10: Ninja Gaiden" )
	public static GameDriver driver_pc_tmnt	   = new GameDriver("1989"	,"pc_tmnt"	,"playch10.java"	,rom_pc_tmnt,driver_	,machine_driver_playch10	,input_ports_playch10	,init_playch10	,pcfboard	,	ROT0, "Konami (Nintendo of America license)", "PlayChoice-10: Teenage Mutant Ninja Turtles" )
	public static GameDriver driver_pc_drmro	   = new GameDriver("1990"	,"pc_drmro"	,"playch10.java"	,rom_pc_drmro,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pcfboard	,ROT0	,	"Nintendo", "PlayChoice-10: Dr Mario" )
	public static GameDriver driver_pc_ynoid	   = new GameDriver("1990"	,"pc_ynoid"	,"playch10.java"	,rom_pc_ynoid,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pcfboard	,ROT0	,	"Capcom USA (Nintendo of America license)", "PlayChoice-10: Yo! Noid" )
	public static GameDriver driver_pc_ddrgn	   = new GameDriver("19??"	,"pc_ddrgn"	,"playch10.java"	,rom_pc_ddrgn,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pcfboard	,ROT0	,	"Technos?", "PlayChoice-10: Double Dragon" )
	public static GameDriver driver_pc_ftqst	   = new GameDriver("1989"	,"pc_ftqst"	,"playch10.java"	,rom_pc_ftqst,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pcfboard	,ROT0	,	"Sunsoft (Nintendo of America license)", "PlayChoice-10: Fester's Quest" )
	public static GameDriver driver_pc_rrngr	   = new GameDriver("19??"	,"pc_rrngr"	,"playch10.java"	,rom_pc_rrngr,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pcfboard	,ROT0	,	"Capcom USA (Nintendo of America license)", "PlayChoice-10: Rescue Rangers" )
	
	/* G-Board Games */
	public static GameDriver driver_pc_gntlt	   = new GameDriver("1985"	,"pc_gntlt"	,"playch10.java"	,rom_pc_gntlt,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pcgboard	,ROT0	,	"Atari/Tengen (Nintendo of America license)", "PlayChoice-10: Gauntlet", GAME_NOT_WORKING )
	public static GameDriver driver_pc_smb2	   = new GameDriver("1988"	,"pc_smb2"	,"playch10.java"	,rom_pc_smb2,driver_	,machine_driver_playch10	,input_ports_playch10	,init_playch10	,pcgboard	,	ROT0, "Nintendo", "PlayChoice-10: Super Mario Bros. 2" )
	public static GameDriver driver_pc_smb3	   = new GameDriver("1988"	,"pc_smb3"	,"playch10.java"	,rom_pc_smb3,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pcgboard	,ROT0	,	"Nintendo", "PlayChoice-10: Super Mario Bros. 3" )
	public static GameDriver driver_pc_mman3	   = new GameDriver("1990"	,"pc_mman3"	,"playch10.java"	,rom_pc_mman3,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pcgboard	,ROT0	,	"Capcom USA (Nintendo of America license)", "PlayChoice-10: Mega Man 3" )
	public static GameDriver driver_pc_radr2	   = new GameDriver("1990"	,"pc_radr2"	,"playch10.java"	,rom_pc_radr2,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pcgboard	,ROT0	,	"Square (Nintendo of America license)", "PlayChoice-10: Rad Racer II", GAME_NOT_WORKING )
	public static GameDriver driver_pc_suprc	   = new GameDriver("1990"	,"pc_suprc"	,"playch10.java"	,rom_pc_suprc,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pcgboard	,ROT0	,	"Konami (Nintendo of America license)", "PlayChoice-10: Super C" )
	public static GameDriver driver_pc_tmnt2	   = new GameDriver("1990"	,"pc_tmnt2"	,"playch10.java"	,rom_pc_tmnt2,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pcgboard	,ROT0	,	"Konami (Nintendo of America license)", "PlayChoice-10: Teenage mutant Ninja Turtles 2" )
	public static GameDriver driver_pc_wcup	   = new GameDriver("1990"	,"pc_wcup"	,"playch10.java"	,rom_pc_wcup,driver_	,machine_driver_playch10	,input_ports_playch10	,init_playch10	,pcgboard	,	ROT0, "Technos (Nintendo license)", "PlayChoice-10: Nintendo World Cup" )
	public static GameDriver driver_pc_ngai3	   = new GameDriver("1991"	,"pc_ngai3"	,"playch10.java"	,rom_pc_ngai3,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pcgboard	,ROT0	,	"Tecmo (Nintendo of America license)", "PlayChoice-10: Ninja Gaiden 3" )
	public static GameDriver driver_pc_pwbld	   = new GameDriver("1991"	,"pc_pwbld"	,"playch10.java"	,rom_pc_pwbld,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pcgboard	,ROT0	,	"Taito (Nintendo of America license)", "PlayChoice-10: Power Blade" )
	public static GameDriver driver_pc_rkats	   = new GameDriver("1991"	,"pc_rkats"	,"playch10.java"	,rom_pc_rkats,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pcgboard	,ROT0	,	"Atlus (Nintendo of America license)", "PlayChoice-10: Rockin' Kats" )
	
	/* i-Board Games */
	public static GameDriver driver_pc_cshwk	   = new GameDriver("1989"	,"pc_cshwk"	,"playch10.java"	,rom_pc_cshwk,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pciboard	,ROT0	,	"Rare (Nintendo of America license)", "PlayChoice-10: Captain Sky Hawk" )
	
	/* K-Board Games */
	public static GameDriver driver_pc_moglf	   = new GameDriver("1991"	,"pc_moglf"	,"playch10.java"	,rom_pc_moglf,driver_playch10	,machine_driver_playch10	,input_ports_playch10	,init_pckboard	,ROT0	,	"Nintendo", "PlayChoice-10: Mario Open Golf" )
}
