/****************************************************************************
 *
 * geebee.c
 *
 * system driver
 * juergen buchmueller <pullmoll@t-online.de>, jan 2000
 *
 * memory map (preliminary)
 * 0000-0fff ROM1 / ROM0
 * 1000-1fff ROM2
 * 2000-2fff VRAM
 * 3000-3fff CGROM
 * 4000-4fff RAM
 * 5000-5fff IN
 *			 A1 A0
 *			  0  0	  SW0
 *					  D0 COIN1
 *					  D1 COIN2
 *					  D2 START1
 *					  D3 START2
 *					  D4 BUTTON1
 *					  D5 TEST MODE
 *			  0  1	  SW1
 *					  - not used in Gee Bee
 *					  - digital joystick left/right and button in
 *						Kaitei Tagara Sagashi (two in Cocktail mode)
 *			  1  0	  DSW2
 *					  D0	cabinet: 0= upright  1= table
 *					  D1	balls:	 0= 3		 1= 5
 *					  D2-D3 coinage: 0=1c/1c 1=1c/2c 2=2c/1c 3=free play
 *					  D4-D5 bonus:	 0=none, 1=40k	 2=70k	 3=100k
 *			  1  1	  VOLIN
 *					  D0-D7 vcount where paddle starts (note: rotated 90 deg!)
 *					  - not used(?) in Kaitei Tagara Sagashi
 * 6000-6fff OUT6
 *			 A1 A0
 *			  0  0	  BALL H
 *			  0  1	  BALL V
 *			  1  0	  n/c
 *			  1  1	  SOUND
 *					  D3 D2 D1 D0	   sound
 *					   x  0  0	0  PURE TONE 4V (2000Hz)
 *					   x  0  0	1  PURE TONE 8V (1000Hz)
 *					   x  0  1	0  PURE TONE 16V (500Hz)
 *					   x  0  1	1  PURE TONE 32V (250Hz)
 *					   x  1  0	0  TONE1 (!1V && !16V)
 *					   x  1  0	1  TONE2 (!2V && !32V)
 *					   x  1  1	0  TONE3 (!4V && !64V)
 *					   x  1  1	1  NOISE
 *					   0  x  x	x  DECAY
 *					   1  x  x	x  FULL VOLUME
 * 7000-7fff OUT7
 *			 A2 A1 A0
 *			  0  0	0 LAMP 1
 *			  0  0	1 LAMP 2
 *			  0  1	0 LAMP 3
 *			  0  1	1 COUNTER
 *			  1  0	0 LOCK OUT COIL
 *			  1  0	1 BGW
 *			  1  1	0 BALL ON
 *			  1  1	1 INV
 * 8000-ffff INTA (read FF)
 *
 * TODO:
 * add second controller for cocktail mode and two players?
 *
 ****************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class geebee
{
	
	/* from machine/geebee.c */
	extern extern 
	/* from vidhrdw/geebee.c */
	extern void geebee_init_palette(UBytePtr sys_palette, unsigned short *sys_colortable, const UBytePtr color_prom);
	extern void navalone_init_palette(UBytePtr sys_palette, unsigned short *sys_colortable, const UBytePtr color_prom);
	
	extern extern extern extern extern 
	/* from sndhrdw/geebee.c */
	extern extern extern 
	/*******************************************************
	 *
	 * memory regions
	 *
	 *******************************************************/
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x1fff, MRA_ROM ),	/* GeeBee uses only the first 4K */
		new MemoryReadAddress( 0x2000, 0x23ff, MRA_RAM ),
		new MemoryReadAddress( 0x3000, 0x37ff, MRA_ROM ),	/* GeeBee uses only the first 1K */
		new MemoryReadAddress( 0x4000, 0x40ff, MRA_RAM ),
		new MemoryReadAddress( 0x5000, 0x5fff, geebee_in_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress readmem_navalone[] =
	{
		new MemoryReadAddress( 0x0000, 0x1fff, MRA_ROM ),
		new MemoryReadAddress( 0x2000, 0x23ff, MRA_RAM ),
		new MemoryReadAddress( 0x3000, 0x37ff, MRA_ROM ),
		new MemoryReadAddress( 0x4000, 0x40ff, MRA_RAM ),
		new MemoryReadAddress( 0x5000, 0x5fff, navalone_in_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x1fff, MWA_ROM ),
		new MemoryWriteAddress( 0x2000, 0x23ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x2400, 0x27ff, videoram_w ), /* mirror used in kaitei */
		new MemoryWriteAddress( 0x3000, 0x37ff, MWA_ROM ),
	    new MemoryWriteAddress( 0x4000, 0x40ff, MWA_RAM ),
		new MemoryWriteAddress( 0x6000, 0x6fff, geebee_out6_w ),
		new MemoryWriteAddress( 0x7000, 0x7fff, geebee_out7_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static IOReadPort readport[] =
	{
		new IOReadPort( 0x50, 0x5f, geebee_in_r ),
		new IOReadPort( -1 )  /* end of table */
	};
	
	static IOReadPort readport_navalone[] =
	{
		new IOReadPort( 0x50, 0x5f, navalone_in_r ),
		new IOReadPort( -1 )  /* end of table */
	};
	
	static IOWritePort writeport[] =
	{
		new IOWritePort( 0x60, 0x6f, geebee_out6_w ),
		new IOWritePort( 0x70, 0x7f, geebee_out7_w ),
		new IOWritePort( -1 )  /* end of table */
	};
	
	static InputPortPtr input_ports_geebee = new InputPortPtr(){ public void handler() { 
	PORT_START(); 		/* IN0 SW0 */
		PORT_BIT	( 0x01, IP_ACTIVE_LOW, IPT_COIN1   );	PORT_BIT	( 0x02, IP_ACTIVE_LOW, IPT_COIN2   );	PORT_BIT	( 0x04, IP_ACTIVE_LOW, IPT_START1  );	PORT_BIT	( 0x08, IP_ACTIVE_LOW, IPT_START2  );	PORT_BIT	( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );	PORT_SERVICE( 0x20, IP_ACTIVE_LOW );	PORT_BIT	( 0xc0, 0x00, IPT_UNUSED );
		PORT_START();       /* IN1 SW1 */
		PORT_BIT	( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT	( 0x02, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT	( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT	( 0x08, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT	( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT	( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT	( 0xc0, 0x00, IPT_UNUSED );
		PORT_START();       /* IN2 DSW2 */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail"));
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );	PORT_DIPSETTING(    0x02, "5" );	PORT_DIPNAME( 0x0c, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "Free_Play") );
		/* Bonus Life moved to two inputs to allow changing 3/5 lives mode separately */
		PORT_BIT	( 0x30, 0x00, IPT_UNUSED );	PORT_BIT	( 0xc0, 0x00, IPT_UNUSED );
		PORT_START(); 		/* IN3 VOLIN */
		PORT_ANALOG( 0xff, 0x58, IPT_PADDLE | IPF_REVERSE, 30, 15, 0x10, 0xa0 );
		PORT_START(); 		/* IN4 FAKE for 3 lives */
		PORT_BIT	( 0x0f, 0x00, IPT_UNUSED );	PORT_DIPNAME( 0x30, 0x00, "Bonus Life (3 lives); )
		PORT_DIPSETTING(    0x10, "40k 80k" );	PORT_DIPSETTING(    0x20, "70k 140k" );	PORT_DIPSETTING(    0x30, "100k 200k" );	PORT_DIPSETTING(    0x00, "None" );	PORT_BIT	( 0xc0, 0x00, IPT_UNUSED );
		PORT_START(); 		/* IN5 FAKE for 5 lives */
		PORT_BIT	( 0x0f, 0x00, IPT_UNUSED );	PORT_DIPNAME( 0x30, 0x00, "Bonus Life (5 lives); )
		PORT_DIPSETTING(    0x10, "60k 120k" );	PORT_DIPSETTING(    0x20, "100k 200k" );	PORT_DIPSETTING(    0x30, "150k 300k" );	PORT_DIPSETTING(    0x00, "None" );	PORT_BIT	( 0xc0, 0x00, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_navalone = new InputPortPtr(){ public void handler() { 
	PORT_START(); 		/* IN0 SW0 */
		PORT_BIT	( 0x01, IP_ACTIVE_LOW, IPT_COIN1   );	PORT_BIT	( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT	( 0x04, IP_ACTIVE_LOW, IPT_START1  );	PORT_BIT	( 0x08, IP_ACTIVE_LOW, IPT_START2  );	PORT_BIT	( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );	PORT_BIT	( 0x20, IP_ACTIVE_LOW, IPT_COIN2   );	PORT_BIT	( 0xc0, 0x00, IPT_UNUSED );
		PORT_START();       /* IN1 SW1 */
		PORT_BIT	( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT	( 0x02, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT	( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT	( 0x08, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT	( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT	( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT	( 0xc0, 0x00, IPT_UNUSED );
		PORT_START();       /* IN2 DSW2 */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail"));
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x00, "2" );	PORT_DIPSETTING(	0x02, "3" );	PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Unknown") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
	    PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x08, DEF_STR( "On") );
	    PORT_DIPNAME( 0x38, 0x10, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x30, DEF_STR( "2C_1C") );
	    PORT_DIPSETTING(    0x10, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x20, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Free_Play") );
	    PORT_BIT    ( 0xc0, 0x00, IPT_UNUSED );
		PORT_START(); 		/* IN3 VOLIN */
		PORT_BIT	( 0xff, 0x58, IPT_UNUSED );
		PORT_START(); 		/* IN4 two-way digital joystick */
		PORT_BIT	( 0x01, 0x00, IPT_JOYSTICK_LEFT );	PORT_BIT	( 0x02, 0x00, IPT_JOYSTICK_RIGHT );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_kaitei = new InputPortPtr(){ public void handler() { 
	PORT_START(); 		/* IN0 SW0 */
		PORT_BIT	( 0x01, IP_ACTIVE_LOW,	IPT_COIN1 );	PORT_BIT	( 0x02, IP_ACTIVE_HIGH, IPT_COIN2 );	PORT_BIT	( 0x04, IP_ACTIVE_LOW,	IPT_START1 );	PORT_BIT	( 0x08, IP_ACTIVE_LOW,	IPT_START2 );	PORT_BIT	( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );    PORT_BIT    ( 0x20, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT	( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BIT	( 0x80, IP_ACTIVE_LOW,	IPT_UNKNOWN );
		PORT_START();       /* IN1 SW1 */
		PORT_BIT	( 0x01, IP_ACTIVE_LOW,	IPT_JOYSTICK_RIGHT );	PORT_BIT	( 0x02, IP_ACTIVE_LOW,	IPT_JOYSTICK_LEFT );	PORT_BIT	( 0x04, IP_ACTIVE_LOW,	IPT_BUTTON1 );	PORT_BIT	( 0x08, IP_ACTIVE_LOW,	IPT_JOYSTICK_RIGHT | IPF_COCKTAIL );	PORT_BIT	( 0x10, IP_ACTIVE_LOW,	IPT_JOYSTICK_LEFT | IPF_COCKTAIL );	PORT_BIT	( 0x20, IP_ACTIVE_LOW,	IPT_BUTTON1 | IPF_COCKTAIL );    PORT_BIT    ( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BIT	( 0x80, IP_ACTIVE_LOW,	IPT_UNKNOWN );
		PORT_START();       /* IN2 DSW2 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(	0x01, DEF_STR( "Upright") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Cocktail"));
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Unknown") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x04, "5" );    PORT_DIPSETTING(    0x00, "7" );    PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x20, DEF_STR( "On") );
	    PORT_BIT    ( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BIT	( 0x80, IP_ACTIVE_LOW,	IPT_UNKNOWN );
		PORT_START(); 		/* IN3 VOLIN */
		PORT_BIT	( 0x01, IP_ACTIVE_LOW,	IPT_UNKNOWN );	PORT_BIT	( 0x02, IP_ACTIVE_LOW,	IPT_UNKNOWN );	PORT_BIT	( 0x04, IP_ACTIVE_LOW,	IPT_UNKNOWN );    PORT_BIT    ( 0x08, IP_ACTIVE_LOW,  IPT_UNKNOWN );    PORT_BIT    ( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );    PORT_BIT    ( 0x20, IP_ACTIVE_LOW,  IPT_UNKNOWN );    PORT_BIT    ( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BIT	( 0x80, IP_ACTIVE_LOW,	IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_kaitein = new InputPortPtr(){ public void handler() { 
	PORT_START(); 		/* IN0 SW0 */
		PORT_BIT	( 0x01, IP_ACTIVE_LOW,	IPT_COIN1 );	PORT_BIT	( 0x02, IP_ACTIVE_LOW,	IPT_COIN2 );	PORT_BIT	( 0x04, IP_ACTIVE_LOW,	IPT_START1 );	PORT_BIT	( 0x08, IP_ACTIVE_LOW,	IPT_START2 );	PORT_BIT	( 0x10, IP_ACTIVE_LOW,	IPT_BUTTON1 );	PORT_BIT	( 0x20, IP_ACTIVE_LOW,	IPT_UNKNOWN );	PORT_BIT	( 0x40, IP_ACTIVE_LOW,	IPT_UNKNOWN );	PORT_BIT	( 0x80, IP_ACTIVE_LOW,	IPT_UNKNOWN );
		PORT_START();       /* IN1 SW1 */
		PORT_BIT	( 0x01, IP_ACTIVE_LOW,	IPT_UNKNOWN );	PORT_BIT	( 0x02, IP_ACTIVE_LOW,	IPT_UNKNOWN );	PORT_BIT	( 0x04, IP_ACTIVE_LOW,	IPT_UNKNOWN );    PORT_BIT    ( 0x08, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT	( 0x10, IP_ACTIVE_LOW,	IPT_UNKNOWN );    PORT_BIT    ( 0x20, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT	( 0x40, IP_ACTIVE_LOW,	IPT_UNKNOWN );    PORT_BIT    ( 0x80, IP_ACTIVE_LOW,  IPT_UNKNOWN );
	    PORT_START();       /* IN2 DSW2 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x00, "2" );	PORT_DIPSETTING(	0x01, "3" );	PORT_DIPSETTING(	0x02, "4" );	PORT_DIPSETTING(	0x03, "5" );	PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Unknown") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x04, DEF_STR( "On") );
	    PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x10, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x30, DEF_STR( "2C_1C") );
	    PORT_DIPSETTING(    0x10, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x20, DEF_STR( "1C_2C") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_BIT	( 0x40, IP_ACTIVE_LOW,	IPT_UNKNOWN );	PORT_BIT	( 0x80, IP_ACTIVE_LOW,	IPT_UNKNOWN );
		PORT_START(); 		/* IN3 VOLIN */
		PORT_BIT	( 0xff, 0x58, IPT_UNUSED );
		PORT_START(); 		/* IN4 two-way digital joystick */
		PORT_BIT	( 0x01, 0x00, IPT_JOYSTICK_LEFT );	PORT_BIT	( 0x02, 0x00, IPT_JOYSTICK_RIGHT );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_sos = new InputPortPtr(){ public void handler() { 
    PORT_START();       /* IN0 SW0 */
	    PORT_BIT    ( 0x01, IP_ACTIVE_LOW, IPT_COIN1   );    PORT_BIT    ( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );    PORT_BIT    ( 0x04, IP_ACTIVE_LOW, IPT_START1  );    PORT_BIT    ( 0x08, IP_ACTIVE_LOW, IPT_START2  );    PORT_BIT    ( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );    PORT_BIT    ( 0x20, IP_ACTIVE_LOW, IPT_COIN2   );    PORT_BIT    ( 0xc0, 0x00, IPT_UNUSED );
	    PORT_START();       /* IN1 SW1 */
	    PORT_BIT    ( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );    PORT_BIT    ( 0x02, IP_ACTIVE_LOW, IPT_UNUSED );    PORT_BIT    ( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );    PORT_BIT    ( 0x08, IP_ACTIVE_LOW, IPT_UNUSED );    PORT_BIT    ( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );    PORT_BIT    ( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );    PORT_BIT    ( 0xc0, 0x00, IPT_UNUSED );
	    PORT_START();       /* IN2 DSW2 */
	    PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
	    PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail"));
	    PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Lives") );
	    PORT_DIPSETTING(    0x00, "2" );    PORT_DIPSETTING(    0x02, "3" );    PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Unknown") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
	    PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x38, 0x08, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x18, DEF_STR( "2C_1C") );
	    PORT_DIPSETTING(    0x08, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x30, DEF_STR( "1C_2C") );
	    PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
	    PORT_BIT    ( 0xc0, 0x00, IPT_UNUSED );
	    PORT_START();       /* IN3 VOLIN */
		PORT_ANALOG( 0xff, 0x58, IPT_PADDLE | IPF_REVERSE, 30, 15, 0x10, 0xa0 );
	INPUT_PORTS_END(); }}; 
	
	static GfxLayout charlayout_1k = new GfxLayout
	(
		8, 8,							   /* 8x8 pixels */
		128,							   /* 128 codes */
		1,								   /* 1 bit per pixel */
		new int[] {0},							   /* no bitplanes */
		/* x offsets */
		new int[] {0,1,2,3,4,5,6,7},
		/* y offsets */
	    new int[] {0*8,1*8,2*8,3*8,4*8,5*8,6*8,7*8},
		8 * 8							   /* eight bytes per code */
	);
	
	static GfxDecodeInfo gfxdecodeinfo_1k[] =
	{
		new GfxDecodeInfo( REGION_CPU1, 0x3000, charlayout_1k, 0, 4 ),
		new GfxDecodeInfo(-1)							   /* end of array */
	};
	
	static GfxLayout charlayout_2k = new GfxLayout
	(
	    8, 8,                              /* 8x8 pixels */
		256,							   /* 256 codes */
	    1,                                 /* 1 bit per pixel */
	    new int[] {0},                               /* no bitplanes */
	    /* x offsets */
	    new int[] {0,1,2,3,4,5,6,7},
	    /* y offsets */
	    new int[] {0*8,1*8,2*8,3*8,4*8,5*8,6*8,7*8},
	    8 * 8                              /* eight bytes per code */
	);
	
	static GfxDecodeInfo gfxdecodeinfo_2k[] =
	{
		new GfxDecodeInfo( REGION_CPU1, 0x3000, charlayout_2k, 0, 4 ),
		new GfxDecodeInfo(-1)							   /* end of array */
	};
	
	static CustomSound_interface custom_interface = new CustomSound_interface
	(
		geebee_sh_start,
		geebee_sh_stop,
		geebee_sh_update
	);
	
	static MachineDriver machine_driver_geebee = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_8080,
				18432000/9, 		/* 18.432 MHz / 9 */
				readmem,writemem,readport,writeport,
				geebee_interrupt,1	/* one interrupt per frame */
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,      /* single CPU, no need for interleaving */
		null,
	
		/* video hardware */
		34*8, 32*8, new rectangle( 0*8, 34*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo_1k,  /* gfxdecodeinfo */
		3+32768, 4*2,		/* extra colors for the overlay */
	    geebee_init_palette,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		geebee_vh_start,
		generic_vh_stop,
		geebee_vh_screenrefresh,
	
		/* sound hardware */
		0, 0, 0, 0,
		new MachineSound[] {
			new MachineSound(
				SOUND_CUSTOM,
				custom_interface
			)
	    }
	);
	
	
	static MachineDriver machine_driver_navalone = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_8080,
				18432000/9, 		/* 18.432 MHz / 9 */
				readmem_navalone,writemem,readport_navalone,writeport,
				geebee_interrupt,1	/* one interrupt per frame */
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,      /* single CPU, no need for interleaving */
		null,
	
		/* video hardware */
		34*8, 32*8, new rectangle( 0*8, 34*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo_2k,  /* gfxdecodeinfo */
		3+32768, 4*2,		/* extra colors for the overlay */
	    navalone_init_palette,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		navalone_vh_start,
		generic_vh_stop,
		geebee_vh_screenrefresh,
	
		/* sound hardware */
		0, 0, 0, 0,
		new MachineSound[] {
			new MachineSound(
				SOUND_CUSTOM,
				custom_interface
			)
	    }
	);
	
	
	
	static MachineDriver machine_driver_kaitei = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_8080,
				18432000/9, 		/* 18.432 MHz / 9 */
				readmem_navalone,writemem,readport_navalone,writeport,
				kaitei_interrupt,1	/* one interrupt per frame */
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,      /* single CPU, no need for interleaving */
		null,
	
		/* video hardware */
		34*8, 32*8, new rectangle( 0*8, 34*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo_2k,  /* gfxdecodeinfo */
		3+32768, 4*2,		/* extra colors for the overlay */
		navalone_init_palette,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		kaitei_vh_start,
		generic_vh_stop,
		geebee_vh_screenrefresh,
	
		/* sound hardware */
		0, 0, 0, 0,
		new MachineSound[] {
			new MachineSound(
				SOUND_CUSTOM,
				custom_interface
			)
	    }
	);
	
	static MachineDriver machine_driver_sos = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_8080,
				18432000/9, 		/* 18.432 MHz / 9 */
				readmem_navalone,writemem,readport_navalone,writeport,
				geebee_interrupt,1	/* one interrupt per frame */
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,      /* single CPU, no need for interleaving */
		null,
	
		/* video hardware */
		34*8, 32*8, new rectangle( 0*8, 34*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo_2k,  /* gfxdecodeinfo */
		3+32768, 4*2,		/* extra colors for the overlay */
		navalone_init_palette,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		sos_vh_start,
		generic_vh_stop,
		geebee_vh_screenrefresh,
	
		/* sound hardware */
		0, 0, 0, 0,
		new MachineSound[] {
			new MachineSound(
				SOUND_CUSTOM,
				custom_interface
			)
	    }
	);
	
	
	
	static RomLoadPtr rom_geebee = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );	ROM_LOAD( "geebee.1k",      0x0000, 0x1000, 0x8a5577e0 );	ROM_LOAD( "geebee.3a",      0x3000, 0x0400, 0xf257b21b );ROM_END(); }}; 
	
	static RomLoadPtr rom_geebeeg = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );	ROM_LOAD( "geebee.1k",      0x0000, 0x1000, 0x8a5577e0 );	ROM_LOAD( "geebeeg.3a",     0x3000, 0x0400, 0xa45932ba );ROM_END(); }}; 
	
	static RomLoadPtr rom_navalone = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );	ROM_LOAD( "navalone.p1",    0x0000, 0x0800, 0x5a32016b );	ROM_LOAD( "navalone.p2",    0x0800, 0x0800, 0xb1c86fe3 );	ROM_LOAD( "navalone.chr",   0x3000, 0x0800, 0xb26c6170 );ROM_END(); }}; 
	
	static RomLoadPtr rom_kaitei = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );	ROM_LOAD( "kaitei_7.1k",    0x0000, 0x0800, 0x32f70d48 );	ROM_RELOAD( 				0x0800, 0x0800 );    ROM_LOAD( "kaitei_1.1m",    0x1000, 0x0400, 0x9a7ab3b9 );	ROM_LOAD( "kaitei_2.1p",    0x1400, 0x0400, 0x5eeb0fff );	ROM_LOAD( "kaitei_3.1s",    0x1800, 0x0400, 0x5dff4df7 );	ROM_LOAD( "kaitei_4.1t",    0x1c00, 0x0400, 0xe5f303d6 );	ROM_LOAD( "kaitei_5.bin",   0x3000, 0x0400, 0x60fdb795 );	ROM_LOAD( "kaitei_6.bin",   0x3400, 0x0400, 0x21399ace );ROM_END(); }}; 
	
	static RomLoadPtr rom_kaitein = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );	ROM_LOAD( "kaitein.p1",     0x0000, 0x0800, 0xd88e10ae );	ROM_LOAD( "kaitein.p2",     0x0800, 0x0800, 0xaa9b5763 );	ROM_LOAD( "kaitein.chr",    0x3000, 0x0800, 0x3125af4d );ROM_END(); }}; 
	
	static RomLoadPtr rom_sos = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );	ROM_LOAD( "sos.p1",         0x0000, 0x0800, 0xf70bdafb );	ROM_LOAD( "sos.p2",         0x0800, 0x0800, 0x58e9c480 );	ROM_LOAD( "sos.chr",        0x3000, 0x0800, 0x66f983e4 );ROM_END(); }}; 
	
	
	public static GameDriver driver_geebee	   = new GameDriver("1978"	,"geebee"	,"geebee.java"	,rom_geebee,null	,machine_driver_geebee	,input_ports_geebee	,null	,ROT90	,	"Namco", "Gee Bee" )
	public static GameDriver driver_geebeeg	   = new GameDriver("1978"	,"geebeeg"	,"geebee.java"	,rom_geebeeg,driver_geebee	,machine_driver_geebee	,input_ports_geebee	,null	,ROT90	,	"[Namco] (Gremlin license)", "Gee Bee (Gremlin)" )
	public static GameDriver driver_navalone	   = new GameDriver("1980"	,"navalone"	,"geebee.java"	,rom_navalone,null	,machine_driver_navalone	,input_ports_navalone	,null	,ROT90	,	"Namco", "Navalone", GAME_IMPERFECT_SOUND )
	public static GameDriver driver_kaitei	   = new GameDriver("1980"	,"kaitei"	,"geebee.java"	,rom_kaitei,null	,machine_driver_kaitei	,input_ports_kaitei	,null	,ROT90	,	"K.K. Tokki", "Kaitei Takara Sagashi" )
	public static GameDriver driver_kaitein	   = new GameDriver("1980"	,"kaitein"	,"geebee.java"	,rom_kaitein,driver_kaitei	,machine_driver_kaitei	,input_ports_kaitein	,null	,ROT90	,	"Namco", "Kaitei Takara Sagashi (Namco)" )
	public static GameDriver driver_sos	   = new GameDriver("1980"	,"sos"	,"geebee.java"	,rom_sos,null	,machine_driver_sos	,input_ports_sos	,null	,ROT90	,	"Namco", "SOS", GAME_IMPERFECT_SOUND )
	
}
