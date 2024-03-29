/***************************************************************************
  Star Fire

driver by Daniel Boris, Olivier Galibert

Memory Map:

Read:
0000 - 5FFF: ROM
8000 - 9FFF: Working RAM
A000 - BFFF: Pallette RAM (best I can figure)
C000 - DFFF: Video RAM
E000 - FFFF: Video RAM

9400,9800  Dip switch

9401:   Bit null: Coin
        Bit 1: Start
        Bit 2: Fire
        Bit 3: Tie sound on
        Bit 4: Laser sound on
        Bit 5: Slam/Static
        Bit 6,7: high

9805:   Velocity ADC
9806:   Horizontal motion ADC
9807:   Vertical motion ADC

Write:
8000 - 9FFF: Working RAM
A000 - BFFF: Pallette RAM (best I can figure)
C000 - DFFF: Video RAM

9402:   Bit null: Size (something to do with laser sound)
        Bit 1: Explosion sound
        Bit 2: Tie Weapon sound
        Bit 3: Laser sound
        Bit 4: Tacking Computer sound
        Bit 5: Lock sound
        Bit 6: Scanner sound
        Bit 7: Overheat sound

9400:   Video shift control
        Right half of screen:
        Bit null: Mirror bits
        Bit 1..3: Roll right x bits
        Left half of screen:
        Bit 4: Mirror bits
        Bit 5..7: Roll right x bits

9401:   Bit null..3: Video write logic function (A = Data written, B = Data in mem)
            null:   A
            1:   A or B
            7:   !A or B
            C:   null
            D:   !A and B
        Bit 4: Roll (something to do with video)
        Bit 5: PROT  (something to do with video)
        Bit 6: TRANS (something to do with pallette RAM)
        Bit 7: CDRM (something to do with pallette RAM)

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;

public class starfire
{
	
	/* In vidhrdw/starfire.c */
	void starfire_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh);
	
	/* In machine/starfire.c */
	
	
	unsigned char *starfire_ram;
	
	static MemoryReadAddress starfire_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x57ff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0x83ff, MRA_RAM ),
		new MemoryReadAddress( 0x8400, 0x97ff, starfire_shadow_r ),
		new MemoryReadAddress( 0x9800, 0x9fff, starfire_input_r ),
		new MemoryReadAddress( 0xa000, 0xbfff, starfire_colorram_r ),
		new MemoryReadAddress( 0xc000, 0xffff, starfire_videoram_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress starfire_writemem[] =
	{
		new MemoryWriteAddress( 0x8000, 0x83ff, MWA_RAM, starfire_ram ),
		new MemoryWriteAddress( 0x8400, 0x8fff, starfire_shadow_w ),
		new MemoryWriteAddress( 0x9000, 0x9fff, starfire_output_w ),
		new MemoryWriteAddress( 0xa000, 0xbfff, starfire_colorram_w ),
		new MemoryWriteAddress( 0xc000, 0xffff, starfire_videoram_w ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress fireone_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x6fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0x83ff, MRA_RAM ),
		new MemoryReadAddress( 0x8400, 0x97ff, starfire_shadow_r ),
		new MemoryReadAddress( 0x9800, 0x9fff, fireone_input_r ),
		new MemoryReadAddress( 0xa000, 0xbfff, starfire_colorram_r ),
		new MemoryReadAddress( 0xc000, 0xffff, starfire_videoram_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress fireone_writemem[] =
	{
		new MemoryWriteAddress( 0x8000, 0x83ff, MWA_RAM, starfire_ram ),
		new MemoryWriteAddress( 0x8400, 0x8fff, starfire_shadow_w ),
		new MemoryWriteAddress( 0x9000, 0x9fff, fireone_output_w ),
		new MemoryWriteAddress( 0xa000, 0xbfff, starfire_colorram_w ),
		new MemoryWriteAddress( 0xc000, 0xffff, starfire_videoram_w ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	static InputPortPtr input_ports_starfire = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* DSW0 */
		PORT_DIPNAME( 0x03, 0x00, "Time" );
		PORT_DIPSETTING(    0x00, "90 Sec" );
		PORT_DIPSETTING(    0x01, "80 Sec" );
		PORT_DIPSETTING(    0x02, "70 Sec" );
		PORT_DIPSETTING(    0x03, "60 Sec" );
		PORT_DIPNAME( 0x04, 0x00, "Coin(s);to Start" )
		PORT_DIPSETTING(    0x00, "1" );
		PORT_DIPSETTING(    0x04, "2" );
		PORT_DIPNAME( 0x08, 0x00, "Fuel per Coin" );
		PORT_DIPSETTING(    0x00, "300" );
		PORT_DIPSETTING(    0x08, "600" );
		PORT_DIPNAME( 0x30, 0x00, "Bonus" );
		PORT_DIPSETTING(    0x00, "300 points" );
		PORT_DIPSETTING(    0x10, "500 points" );
		PORT_DIPSETTING(    0x20, "700 points" );
		PORT_DIPSETTING(    0x30, "None" );
		PORT_DIPNAME( 0x40, 0x00, "Score Table Hold" );
		PORT_DIPSETTING(    0x00, "fixed length" );
		PORT_DIPSETTING(    0x40, "fixed length+fire" );
		PORT_SERVICE( 0x80, IP_ACTIVE_HIGH );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1);
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START1);
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_BUTTON1);
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
	
		PORT_START();   /* IN2 */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_X, 100, 10, 0, 255 );
	
		PORT_START();   /* IN3 */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_Y | IPF_REVERSE, 100, 10, 0, 255 );
	
		PORT_START();  /* Throttle (IN4) */
		PORT_BITX( 0xFF, 0x00, IP_ACTIVE_HIGH | IPF_TOGGLE, "Throttle", KEYCODE_Z, IP_JOY_NONE );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_fireone = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* DSW0 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x03, "2 Coins/1 Player" );
		PORT_DIPSETTING(    0x02, "2 Coins/1 or 2 Players" );
		PORT_DIPSETTING(    0x00, "1 Coin/1 Player" );
		PORT_DIPSETTING(    0x01, "1 Coin/1 or 2 Players" );
		PORT_DIPNAME( 0x0c, 0x0c, "Time" );
		PORT_DIPSETTING(    0x00, "75 Sec" );
		PORT_DIPSETTING(    0x04, "90 Sec" );
		PORT_DIPSETTING(    0x08, "105 Sec" );
		PORT_DIPSETTING(    0x0c, "120 Sec" );
		PORT_DIPNAME( 0x30, 0x00, "Bonus difficulty" );
		PORT_DIPSETTING(    0x00, "Easy" );
		PORT_DIPSETTING(    0x10, "Normal" );
		PORT_DIPSETTING(    0x20, "Hard" );
		PORT_DIPSETTING(    0x30, "Very hard" );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x80, IP_ACTIVE_HIGH );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_START1);
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2);
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_COIN1);
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_COIN2);
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);
	
		PORT_START();   /* IN2 */
		PORT_ANALOG( 0x3f, 0x20, IPT_AD_STICK_X, 100, 10, 0, 63 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON2);
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_BUTTON1);
	
		PORT_START();   /* IN3 */
		PORT_ANALOG( 0x3f, 0x20, IPT_AD_STICK_Y, 100, 10, 0, 63 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON4);
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_BUTTON3);
	INPUT_PORTS_END(); }}; 
	
	static MachineDriver machine_driver_starfire = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
	            2500000,    /* 2.5 Mhz */
				starfire_readmem, starfire_writemem,null,null,
	            starfire_interrupt,2
			)
		},
		57, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* single CPU, no need for interleaving */
	    null,
	
		/* video hardware */
	    256,256,
	    { 0, 256-1, 0, 256-1 },
	    0,
	    64, 64, null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
	    starfire_vh_start,
	    starfire_vh_stop,
	    starfire_vh_screenrefresh,
	
		/* sound hardware */
	    0,0,0,0
	);
	
	static MachineDriver machine_driver_fireone = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
	            2500000,    /* 2.5 Mhz */
				fireone_readmem, fireone_writemem,null,null,
	            starfire_interrupt,2
			)
		},
		57, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* single CPU, no need for interleaving */
	    null,
	
		/* video hardware */
	    256,256,
	    { 0, 256-1, 0, 256-1 },
	    0,
	    64, 64, null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
	    starfire_vh_start,
	    starfire_vh_stop,
	    starfire_vh_screenrefresh,
	
		/* sound hardware */
	    0,0,0,0
	);
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_starfire = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "sfire.1a",     0x0000, 0x0800, 0x9990af64 );
		ROM_LOAD( "sfire.2a",     0x0800, 0x0800, 0x6e17ba33 );
		ROM_LOAD( "sfire.1b",     0x1000, 0x0800, 0x946175d0 );
		ROM_LOAD( "sfire.2b",     0x1800, 0x0800, 0x67be4275 );
		ROM_LOAD( "sfire.1c",     0x2000, 0x0800, 0xc56b4e07 );
		ROM_LOAD( "sfire.2c",     0x2800, 0x0800, 0xb4b9d3a7 );
		ROM_LOAD( "sfire.1d",     0x3000, 0x0800, 0xfd52ffb5 );
		ROM_LOAD( "sfire.2d",     0x3800, 0x0800, 0x51c69fe3 );
		ROM_LOAD( "sfire.1e",     0x4000, 0x0800, 0x01994ec8 );
		ROM_LOAD( "sfire.2e",     0x4800, 0x0800, 0xef3d1b71 );
		ROM_LOAD( "sfire.1f",     0x5000, 0x0800, 0xaf31dc39 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_fireone = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "fo-ic13.7b",     0x0000, 0x0800, 0xf927f086 );
		ROM_LOAD( "fo-ic24.7c",     0x0800, 0x0800, 0x0d2d8723 );
		ROM_LOAD( "fo-ic12.6b",     0x1000, 0x0800, 0xac7783d9 );
		ROM_LOAD( "fo-ic23.6c",     0x1800, 0x0800, 0x15c74ee7 );
		ROM_LOAD( "fo-ic11.5b",     0x2000, 0x0800, 0x721930a1 );
		ROM_LOAD( "fo-ic22.5c",     0x2800, 0x0800, 0xf0c965b4 );
		ROM_LOAD( "fo-ic10.4b",     0x3000, 0x0800, 0x27a7b2c0 );
		ROM_LOAD( "fo-ic21.4c",     0x3800, 0x0800, 0xb142c857 );
		ROM_LOAD( "fo-ic09.3b",     0x4000, 0x0800, 0x1c076b1b );
		ROM_LOAD( "fo-ic20.3c",     0x4800, 0x0800, 0xb4ac6e71 );
		ROM_LOAD( "fo-ic08.2b",     0x5000, 0x0800, 0x5839e2ff );
		ROM_LOAD( "fo-ic19.2c",     0x5800, 0x0800, 0x9fd85e11 );
		ROM_LOAD( "fo-ic07.1b",     0x6000, 0x0800, 0xb90baae1 );
		ROM_LOAD( "fo-ic18.1c",     0x6800, 0x0800, 0x771ee5ba );
	ROM_END(); }}; 
	
	
	public static GameDriver driver_starfire	   = new GameDriver("1979"	,"starfire"	,"starfire.java"	,rom_starfire,null	,machine_driver_starfire	,input_ports_starfire	,null	,ROT0	,	"Exidy", "Star Fire", GAME_NOT_WORKING | GAME_NO_SOUND )
	public static GameDriver driver_fireone	   = new GameDriver("1979"	,"fireone"	,"starfire.java"	,rom_fireone,null	,machine_driver_fireone	,input_ports_fireone	,null	,ROT0	,	"Exidy", "Fire One", GAME_NOT_WORKING | GAME_NO_SOUND )
}
