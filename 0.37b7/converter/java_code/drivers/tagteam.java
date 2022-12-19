/***************************************************************************

Tag Team Wrestling hardware description:

This hardware is very similar to the BurgerTime/Lock N Chase family of games
but there are just enough differences to make it a pain to share the
codebase. It looks like this hardware is a bridge between the BurgerTime
family and the later Technos games, like Mat Mania and Mysterious Stones.

The video hardware supports 3 sprite banks instead of 1
The sound hardware appears nearly identical to Mat Mania

TODO:
        * fix hi-score (reset) bug

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class tagteam
{
	
	
	
	
	public static WriteHandlerPtr sound_command_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		soundlatch_w.handler(offset,data);
		cpu_cause_interrupt(1,M6502_INT_IRQ);
	} };
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x07ff, MRA_RAM ),
		new MemoryReadAddress( 0x2000, 0x2000, input_port_1_r ),     /* IN1 */
		new MemoryReadAddress( 0x2001, 0x2001, input_port_0_r ),     /* IN0 */
		new MemoryReadAddress( 0x2002, 0x2002, input_port_2_r ),     /* DSW2 */
		new MemoryReadAddress( 0x2003, 0x2003, input_port_3_r ),     /* DSW1 */
		new MemoryReadAddress( 0x4000, 0x43ff, tagteam_mirrorvideoram_r ),
		new MemoryReadAddress( 0x4400, 0x47ff, tagteam_mirrorcolorram_r ),
		new MemoryReadAddress( 0x4800, 0x4fff, MRA_RAM ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x07ff, MWA_RAM ),
	//	new MemoryWriteAddress( 0x2000, 0x2000, tagteam_unk_w ),
		new MemoryWriteAddress( 0x2001, 0x2001, tagteam_control_w ),
		new MemoryWriteAddress( 0x2002, 0x2002, sound_command_w ),
	//	new MemoryWriteAddress( 0x2003, 0x2003, MWA_NOP ), /* Appears to increment when you're out of the ring */
		new MemoryWriteAddress( 0x4000, 0x43ff, tagteam_mirrorvideoram_w ),
		new MemoryWriteAddress( 0x4400, 0x47ff, tagteam_mirrorcolorram_w ),
		new MemoryWriteAddress( 0x4800, 0x4bff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x4c00, 0x4fff, colorram_w, colorram ),
		new MemoryWriteAddress( 0x8000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x03ff, MRA_RAM ),
		new MemoryReadAddress( 0x2007, 0x2007, soundlatch_r ),
		new MemoryReadAddress( 0x4000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x03ff, MWA_RAM ),
		new MemoryWriteAddress( 0x2000, 0x2000, AY8910_write_port_0_w ),
		new MemoryWriteAddress( 0x2001, 0x2001, AY8910_control_port_0_w ),
		new MemoryWriteAddress( 0x2002, 0x2002, AY8910_write_port_1_w ),
		new MemoryWriteAddress( 0x2003, 0x2003, AY8910_control_port_1_w ),
		new MemoryWriteAddress( 0x2004, 0x2004, DAC_0_data_w ),
		new MemoryWriteAddress( 0x2005, 0x2005, interrupt_enable_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	
	public static InterruptPtr tagteam_interrupt = new InterruptPtr() { public int handler() 
	{
		static int coin;
		int port;
	
		port = readinputport(0) & 0xc0;
	
		if (port != 0xc0)    /* Coin */
		{
			if (coin == 0)
			{
				coin = 1;
				return nmi_interrupt();
			}
		}
		else coin = 0;
	
	        return ignore_interrupt();
	} };
	
	static InputPortPtr input_ports_tagteam = new InputPortPtr(){ public void handler() { 
	PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x02, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_LOW,  IPT_TILT );	PORT_BIT( 0x08, IP_ACTIVE_LOW,  IPT_UNKNOWN );	PORT_BIT( 0x10, IP_ACTIVE_LOW,  IPT_UNUSED );	PORT_BIT( 0x20, IP_ACTIVE_LOW,  IPT_UNUSED );	PORT_BIT( 0x40, IP_ACTIVE_LOW,  IPT_START1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW,  IPT_START2 );
		PORT_START();       /* DSW1 - 7 not used?, 8 = VBLANK! */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x0c, "A 2C/1C B 1C/1C" );	PORT_DIPSETTING(    0x08, "A 2C/1C B 1C/2C" );	PORT_DIPSETTING(    0x04, "A 2C/1C B 1C/3C" );	PORT_DIPSETTING(    0x03, "A 1C/1C B 2C/1C" );	PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x0b, "A 1C/1C B 1C/2C" );	PORT_DIPSETTING(    0x07, "A 1C/1C B 1C/3C" );	PORT_DIPSETTING(    0x02, "A 1C/2C B 2C/1C" );	PORT_DIPSETTING(    0x0a, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x0e, "A 1C/2C B 1C/1C" );	PORT_DIPSETTING(    0x06, "A 1C/2C B 1C/3C" );	PORT_DIPSETTING(    0x01, "A 1C/3C B 2C/1C" );	PORT_DIPSETTING(    0x0d, "A 1C/3C B 1C/1C" );	PORT_DIPSETTING(    0x09, "A 1C/3C B 1C/2C" );	PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x20, 0x00, "Control Panel" );	PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_VBLANK  );
		PORT_START();       /* DSW2 - 3,4,5,6,7,8 = not used? */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x01, "Normal" );	PORT_DIPSETTING(    0x00, "Hard" );	PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
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
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,    /* 8*8 characters */
		3072,   /* 3072 characters */
		3,      /* 3 bits per pixel */
		new int[] { 2*3072*8*8, 3072*8*8, 0 },    /* the bitplanes are separated */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8     /* every char takes 8 consecutive bytes */
	);
	
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,  /* 16*16 sprites */
		768,    /* 768 sprites */
		3,      /* 3 bits per pixel */
		new int[] { 2*768*16*16, 768*16*16, 0 },  /* the bitplanes are separated */
		new int[] { 16*8+0, 16*8+1, 16*8+2, 16*8+3, 16*8+4, 16*8+5, 16*8+6, 16*8+7,
				0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		32*8    /* every sprite takes 32 consecutive bytes */
	);
	
	static GfxDecodeInfo tagteam_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,   0, 4 ), /* chars */
		new GfxDecodeInfo( REGION_GFX1, 0, spritelayout, 0, 4 ), /* sprites */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static AY8910interface ay8910_interface = new AY8910interface
	(
		2,      /* 2 chips */
		1500000,        /* 1.5 MHz ?? */
		new int[] { 25, 25 },
		new ReadHandlerPtr[] { 0 },
		new ReadHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 }
	);
	
	static DACinterface dac_interface = new DACinterface
	(
		1,
		new int[] { 255 }
	);
	
	static MachineDriver machine_driver_tagteam = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6502,
				1500000,	/* 1.5 MHz ?? */
				readmem,writemem,null,null,
				tagteam_interrupt,1
			),
			new MachineCPU(
				CPU_M6502 | CPU_AUDIO_CPU,
				975000,  /* 975 kHz ?? */
				sound_readmem,sound_writemem,null,null,
				nmi_interrupt,16   /* IRQs are triggered by the main CPU */
			)
		},
		57, 3072,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 1*8, 31*8-1 ),
		tagteam_gfxdecodeinfo,
		32, 32,
		tagteam_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		generic_vh_start,
		generic_vh_stop,
		tagteam_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_AY8910,
				ay8910_interface
			),
			new MachineSound(
				SOUND_DAC,
				dac_interface
			)
		}
	);
	
	
	
	static RomLoadPtr rom_bigprowr = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "bf00-1.20",    0x08000, 0x2000, 0x8aba32c9 );	ROM_LOAD( "bf01.33",      0x0a000, 0x2000, 0x0a41f3ae );	ROM_LOAD( "bf02.34",      0x0c000, 0x2000, 0xa28b0a0e );	ROM_LOAD( "bf03.46",      0x0e000, 0x2000, 0xd4cf7ec7 );
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for audio code */
		ROM_LOAD( "bf4.8",        0x04000, 0x2000, 0x0558e1d8 );	ROM_LOAD( "bf5.7",        0x06000, 0x2000, 0xc1073f24 );	ROM_LOAD( "bf6.6",        0x08000, 0x2000, 0x208cd081 );	ROM_LOAD( "bf7.3",        0x0a000, 0x2000, 0x34a033dc );	ROM_LOAD( "bf8.2",        0x0c000, 0x2000, 0xeafe8056 );	ROM_LOAD( "bf9.1",        0x0e000, 0x2000, 0xd589ce1b );
		ROM_REGION( 0x12000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "bf10.89",      0x00000, 0x2000, 0xb1868746 );	ROM_LOAD( "bf11.94",      0x02000, 0x2000, 0xc3fe99c1 );	ROM_LOAD( "bf12.103",     0x04000, 0x2000, 0xc8717a46 );	ROM_LOAD( "bf13.91",      0x06000, 0x2000, 0x23ee34d3 );	ROM_LOAD( "bf14.95",      0x08000, 0x2000, 0xa6721142 );	ROM_LOAD( "bf15.105",     0x0a000, 0x2000, 0x60ae1078 );	ROM_LOAD( "bf16.93",      0x0c000, 0x2000, 0xd33dc245 );	ROM_LOAD( "bf17.96",      0x0e000, 0x2000, 0xccf42380 );	ROM_LOAD( "bf18.107",     0x10000, 0x2000, 0xfd6f006d );
		ROM_REGION( 0x0040, REGION_PROMS );	ROM_LOAD( "fko.8",        0x0000, 0x0020, 0xb6ee1483 );	ROM_LOAD( "fjo.25",       0x0020, 0x0020, 0x24da2b63 );/* What is this prom for? */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_tagteam = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "prowbf0.bin",  0x08000, 0x2000, 0x6ec3afae );	ROM_LOAD( "prowbf1.bin",  0x0a000, 0x2000, 0xb8fdd176 );	ROM_LOAD( "prowbf2.bin",  0x0c000, 0x2000, 0x3d33a923 );	ROM_LOAD( "prowbf3.bin",  0x0e000, 0x2000, 0x518475d2 );
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for audio code */
		ROM_LOAD( "bf4.8",        0x04000, 0x2000, 0x0558e1d8 );	ROM_LOAD( "bf5.7",        0x06000, 0x2000, 0xc1073f24 );	ROM_LOAD( "bf6.6",        0x08000, 0x2000, 0x208cd081 );	ROM_LOAD( "bf7.3",        0x0a000, 0x2000, 0x34a033dc );	ROM_LOAD( "bf8.2",        0x0c000, 0x2000, 0xeafe8056 );	ROM_LOAD( "bf9.1",        0x0e000, 0x2000, 0xd589ce1b );
		ROM_REGION( 0x12000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "prowbf10.bin", 0x00000, 0x2000, 0x48165902 );	ROM_LOAD( "bf11.94",      0x02000, 0x2000, 0xc3fe99c1 );	ROM_LOAD( "prowbf12.bin", 0x04000, 0x2000, 0x69de1ea2 );	ROM_LOAD( "prowbf13.bin", 0x06000, 0x2000, 0xecfa581d );	ROM_LOAD( "bf14.95",      0x08000, 0x2000, 0xa6721142 );	ROM_LOAD( "prowbf15.bin", 0x0a000, 0x2000, 0xd0de7e03 );	ROM_LOAD( "prowbf16.bin", 0x0c000, 0x2000, 0x75ee5705 );	ROM_LOAD( "bf17.96",      0x0e000, 0x2000, 0xccf42380 );	ROM_LOAD( "prowbf18.bin", 0x10000, 0x2000, 0xe73a4bba );
		ROM_REGION( 0x0040, REGION_PROMS );	ROM_LOAD( "fko.8",        0x0000, 0x0020, 0xb6ee1483 );	ROM_LOAD( "fjo.25",       0x0020, 0x0020, 0x24da2b63 );/* What is this prom for? */
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_bigprowr	   = new GameDriver("1983"	,"bigprowr"	,"tagteam.java"	,rom_bigprowr,null	,machine_driver_tagteam	,input_ports_tagteam	,null	,ROT270	,	"Technos", "The Big Pro Wrestling!" )
	public static GameDriver driver_tagteam	   = new GameDriver("1983"	,"tagteam"	,"tagteam.java"	,rom_tagteam,driver_bigprowr	,machine_driver_tagteam	,input_ports_tagteam	,null	,ROT270	,	"Technos (Data East license)", "Tag Team Wrestling" )
}
