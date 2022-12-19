/***************************************************************************

Dottori Kun (Head On's mini game)
(c)1990 SEGA

Driver by Takahiro Nogi (nogi@kt.rim.or.jp) 1999/12/15 -


CPU   : Z-80 (4MHz)
SOUND : (none)

14479.MPR  ; PRG (FIRST VER)
14479A.MPR ; PRG (NEW VER)

* This game is only for the test of cabinet
* BackRaster = WHITE on the FIRST version.
* BackRaster = BLACK on the NEW version.
* On the NEW version, push COIN-SW as TEST MODE.
* 0000-3FFF:ROM 8000-85FF:VRAM(128x96) 8600-87FF:WORK-RAM

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class dotrikun
{
	
	
	
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0x87ff, MRA_RAM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x3fff, MWA_ROM ),
		new MemoryWriteAddress( 0x8000, 0x87ff, dotrikun_videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static IOReadPort readport[] =
	{
		new IOReadPort( 0x00, 0x00, input_port_0_r ),
		new IOReadPort( -1 )	/* end of table */
	};
	
	static IOWritePort writeport[] =
	{
		new IOWritePort( 0x00, 0x00, dotrikun_color_w ),
		new IOWritePort( -1 )	/* end of table */
	};
	
	
	static InputPortPtr input_ports_dotrikun = new InputPortPtr(){ public void handler() { 
	PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );INPUT_PORTS_END(); }}; 
	
	
	static MachineDriver machine_driver_dotrikun = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				4000000,		 /* 4 MHz */
				readmem, writemem, readport, writeport,
				interrupt, 1
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,					/* single CPU, no need for interleaving */
		null,
	
		/* video hardware */
		256, 256, new rectangle( 0, 256-1, 0, 192-1 ),
		null,
		2, null,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
		null,
		null,
		0,
		dotrikun_vh_screenrefresh,
	
		/* sound hardware */
		0, 0, 0, 0
	);
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_dotrikun = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "14479a.mpr",	0x0000, 0x4000, 0xb77a50db );ROM_END(); }}; 
	
	static RomLoadPtr rom_dotriku2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "14479.mpr",	0x0000, 0x4000, 0xa6aa7fa5 );ROM_END(); }}; 
	
	
	public static GameDriver driver_dotrikun	   = new GameDriver("1990"	,"dotrikun"	,"dotrikun.java"	,rom_dotrikun,null	,machine_driver_dotrikun	,input_ports_dotrikun	,null	,ROT0	,	"Sega", "Dottori Kun (new version)" )
	public static GameDriver driver_dotriku2	   = new GameDriver("1990"	,"dotriku2"	,"dotrikun.java"	,rom_dotriku2,driver_dotrikun	,machine_driver_dotrikun	,input_ports_dotrikun	,null	,ROT0	,	"Sega", "Dottori Kun (old version)" )
}
