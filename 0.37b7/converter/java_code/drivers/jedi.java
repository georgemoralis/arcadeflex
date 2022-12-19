/****************************************************************************

Return of the Jedi

driver by Dan Boris


Master processor
0000-07ff   R/W     Z-page Working RAM
0800-08ff   R/W     NVRAM
0C00        R       Bit 7 = Right Coin
                    Bit 6 = Left Coin
                    Bit 5 = Aux Coin Switch
                    Bit 4 = Self Test
                    Bit 3 = Spare (High)
                    Bit 2 = Left thumb switch
                    Bit 1 = Right thumb switch
0C01        R       Bit 7 = VBLANK
                    Bit 6 = Sound CPU comm latch full flag
                    Bit 5 = Sound CPU ack latch flag
                    Bit 4 = Not used (High)
                    Bit 3 = Not used (High)
                    Bit 2 = Slam
                    Bit 1 = Not used (High)
                    Bit 0 = Not used (High)
1400        R       Sound CPU ack latch
1800        R       Read A/D conversion
1C00        W       Enable NVRAM
1C01        W       Disable NVRAM
1C80        W       Start A/D conversion (horizontal)
1C82        W       Start A/D conversion (vertical)
1D00        W       NVRAM store
1D80        W       Watchdog clear
1E00        W       IRQ ack
1E80        W       Left coin counter
1E81        W       Right coin counter
1E82        W       LED 1(not used)
1E83        W       LED 2(not used)
1E84        W       Alphanumeric ROM bank select
1E85        W       Not used
1E86        W       Sound CPU reset
1E87        W       Video off
1F00        W       Sound CPU comm latch
1F80        W       Bit 0..2: Program ROM bank select
2000-23FF   R/W     Scrolling playfield (low)
2400-27FF   R/W     Scrolling playfield (high)
2800-2BFF   R/W     Color RAM (low)
2C00-2FFF   R/W     Color RAM (high)
3000-37BF   R/W     Alphanumeric RAM

37C0-37EF   R/W     Motion object picture
3800-382F   R/W     Bit 6,2,1 = Motion object picture bank select
                    Bit 5 = Motion object vertical reflect
                    Bit 4 = Motion object horizontal reflect
                    Bit 3 = Motion object 32 pixels tall
                    Bit 0 = Motion object horizontal position (D8)
3840-386F   R/W     Motion object vertical position
38C0-38EF   R/W     Motion object horizontal position (D7-D0)
3C00-3C01   W       Scrolling playfield vertical position
3D00-3D01   W       Scrolling playfield horizontal position
3E00-3FFF   W       PIXI graphics expander RAM
4000-7FFF   R       Banked program ROM
8000-FFFF   R       Fixed program ROM

Sound processor
0000-07ff   R/W     Z-page Working RAM
0800-083f   R/W     Custom I/O (Quad Pokey)
1000        W       IRQ Ack
1100        W       Speech Data
1200        W       Speech write strobe on
1300        W       Speech write strobe off
1400        W       Main CPU ack latch
1500        W       Bit 0 = Speech chip reset
1800        R       Main CPU comm latch
1C00        R       Bit 7 = Speech chip ready
1C01        R       Bit 7 = Sound CPU comm latch full flag
            R       Bit 6 = Sound CPU ack latch full flag
8000-FFFF   R       Program ROM

****************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class jedi
{
	
	/* sndhrdw jedi.c */
	
	/* vidhrdw jedi.c */
	
	extern UBytePtr jedi_PIXIRAM;
	extern UBytePtr jedi_backgroundram;
	extern size_t jedi_backgroundram_size;
	
	
	
	
	static UBytePtr nvram;
	static size_t nvram_size;
	
	static public static nvramPtr nvram_handler  = new nvramPtr() { public void handler(Object file, int read_or_write) 
	{
		if (read_or_write != 0)
			osd_fwrite(file,nvram,nvram_size);
		else
		{
			if (file != 0)
				osd_fread(file,nvram,nvram_size);
			else
				memset(nvram,0,nvram_size);
		}
	} };
	
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x07ff, MRA_RAM ),
		new MemoryReadAddress( 0x0800, 0x08ff, MRA_RAM ),
		new MemoryReadAddress( 0x0c00, 0x0c00, input_port_0_r ),
		new MemoryReadAddress( 0x0c01, 0x0c01, jedi_mainstat_r ), /* IN1 */
		new MemoryReadAddress( 0x1400, 0x1400, jedi_soundacklatch_r ),
		new MemoryReadAddress( 0x1800, 0x1800, jedi_control_r ),
		new MemoryReadAddress( 0x2000, 0x27ff, MRA_RAM ),
		new MemoryReadAddress( 0x2800, 0x2fff, MRA_RAM ),
		new MemoryReadAddress( 0x3000, 0x37bf, MRA_RAM ),
		new MemoryReadAddress( 0x37c0, 0x3bff, MRA_RAM ),
		new MemoryReadAddress( 0x4000, 0x7fff, MRA_BANK1 ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x07ff, MWA_RAM ),
		new MemoryWriteAddress( 0x0800, 0x08ff, MWA_RAM, nvram, nvram_size ),
		new MemoryWriteAddress( 0x1c80, 0x1c82, jedi_control_w),
		new MemoryWriteAddress( 0x1d80, 0x1d80, watchdog_reset_w ),
		new MemoryWriteAddress( 0x1e00, 0x1e00, MWA_NOP ),	/* IRQ ack */
		new MemoryWriteAddress( 0x1e80, 0x1e80, coin_counter_w ),
		new MemoryWriteAddress( 0x1e84, 0x1e84, jedi_alpha_banksel_w ),
		new MemoryWriteAddress( 0x1e86, 0x1e86, jedi_sound_reset_w ),
		new MemoryWriteAddress( 0x1e87, 0x1e87, jedi_video_off_w ),
		new MemoryWriteAddress( 0x1f00, 0x1f00, jedi_soundlatch_w ),
		new MemoryWriteAddress( 0x1f80, 0x1f80, jedi_rom_banksel_w ),
		new MemoryWriteAddress( 0x2000, 0x27ff, jedi_backgroundram_w, jedi_backgroundram, jedi_backgroundram_size ),
		new MemoryWriteAddress( 0x2800, 0x2fff, jedi_paletteram_w, paletteram ),
		new MemoryWriteAddress( 0x3000, 0x37bf, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x37c0, 0x3bff, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x3c00, 0x3c01, jedi_vscroll_w ),
		new MemoryWriteAddress( 0x3d00, 0x3d01, jedi_hscroll_w ),
		new MemoryWriteAddress( 0x3e00, 0x3fff, jedi_PIXIRAM_w, jedi_PIXIRAM ),
		new MemoryWriteAddress( 0x4000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress readmem2[] =
	{
		new MemoryReadAddress( 0x0000, 0x07ff, MRA_RAM ),
		new MemoryReadAddress( 0x0800, 0x080f, pokey1_r ),
		new MemoryReadAddress( 0x0810, 0x081f, pokey2_r ),
		new MemoryReadAddress( 0x0820, 0x082f, pokey3_r ),
		new MemoryReadAddress( 0x0830, 0x083f, pokey4_r ),
		new MemoryReadAddress( 0x1800, 0x1800, jedi_soundlatch_r ),
		new MemoryReadAddress( 0x1c00, 0x1c00, jedi_speech_ready_r ),
		new MemoryReadAddress( 0x1c01, 0x1c01, jedi_soundstat_r ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem2[] =
	{
		new MemoryWriteAddress( 0x0000, 0x07ff, MWA_RAM ),
		new MemoryWriteAddress( 0x0800, 0x080f, pokey1_w ),
		new MemoryWriteAddress( 0x0810, 0x081f, pokey2_w ),
		new MemoryWriteAddress( 0x0820, 0x082f, pokey3_w ),
		new MemoryWriteAddress( 0x0830, 0x083f, pokey4_w ),
		new MemoryWriteAddress( 0x1000, 0x1000, MWA_NOP ),	/* IRQ ack */
		new MemoryWriteAddress( 0x1100, 0x13ff, jedi_speech_w ),
		new MemoryWriteAddress( 0x1400, 0x1400, jedi_soundacklatch_w ),
		new MemoryWriteAddress( 0x8000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	
	static InputPortPtr input_ports_jedi = new InputPortPtr(){ public void handler() { 
    PORT_START();   /* IN0 */
	    PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON3 );    PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 );    PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON1 );    PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNUSED );	PORT_SERVICE( 0x10, IP_ACTIVE_LOW );    PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_SERVICE1 );    PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN2 );    PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );
	    PORT_START();   /* IN1 */
	    PORT_BIT( 0x03, IP_ACTIVE_HIGH, IPT_UNUSED );    PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_TILT );    PORT_BIT( 0x78, IP_ACTIVE_HIGH, IPT_UNUSED );    PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_VBLANK );
	    PORT_START();   /* IN2 */
	    PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_Y, 100, 10, 0, 255 );
	    PORT_START();   /* IN3 */
	    PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_X, 100, 10, 0, 255 );INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,    /* 8*8 characters */
	    512,    /* 512 characters */
	    2,      /* 2 bits per pixel */
		new int[] { 0, 1 }, /* the bitplanes are packed in one nibble */
		new int[] { 0, 2, 4, 6, 8, 10, 12, 14 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
		16*8   /* every char takes 16 consecutive bytes */
	);
	
	static GfxLayout pflayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		2048,	/* 2048 characters */
		4,	/* 4 bits per pixel */
		new int[] { 0, 4, 2048*16*8, 2048*16*8+4 },
		new int[] { 0, 1, 2, 3, 8+0, 8+1, 8+2, 8+3 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16,
				8*16, 9*16, 10*16, 11*16, 12*16, 13*16, 14*16, 15*16 },
		16*8	/* every char takes 16 consecutive bytes */
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		8,16,	/* 8*16 sprites */
		2048,	/* 2048 sprites */
		4,	/* 4 bits per pixel */
		new int[] { 0, 4, 2048*32*8, 2048*32*8+4 },
		new int[] { 0, 1, 2, 3, 8+0, 8+1, 8+2, 8+3},
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16,
				8*16, 9*16, 10*16, 11*16, 12*16, 13*16, 14*16, 15*16 },
		32*8	/* every sprite takes 32 consecutive bytes */
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,    0, 1 ),
		new GfxDecodeInfo( REGION_GFX2, 0, pflayout,      0, 1 ),
		new GfxDecodeInfo( REGION_GFX3, 0, spritelayout,  0, 1 ),
		new GfxDecodeInfo( -1 )
	};
	
	
	
	static POKEYinterface pokey_interface = new POKEYinterface
	(
		4,  /* 4 chips */
		1500000,	/* 1.5 MHz? */
		new int[] { 30, 30, MIXER(30,MIXER_PAN_LEFT), MIXER(30,MIXER_PAN_RIGHT) },
		/* The 8 pot handlers */
		new ReadHandlerPtr[] { null, null ,null ,null},
		{ null, null ,null ,null},
		{ null, null ,null ,null},
		{ null, null ,null ,null},
		{ null, null ,null ,null},
		{ null, null ,null ,null},
		{ null, null ,null ,null},
		{ null, null ,null ,null},
		/* The allpot handler */
		{ null,null,null,null }
	};
	
	static struct TMS5220interface tms5220_interface =
	{
		672000,     /* clock speed (80*samplerate) */
		100,        /* volume */
		null           /* IRQ handler */
	};
	
	
	
	static MachineDriver machine_driver_jedi = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6502,
	            2500000,    /* 2.5 MHz */
				readmem,writemem,null,null,
	            interrupt,4
			),
			new MachineCPU(
	            CPU_M6502,
	            1500000,        /* 1.5 MHz */
				readmem2,writemem2,null,null,
	            interrupt,4
			)
		},
	    60, DEFAULT_REAL_60HZ_VBLANK_DURATION,   /* frames per second, vblank duration */
	    4,  /* 4 cycles per frame - enough for the two CPUs to properly synchronize */
		null,
	
		/* video hardware */
		37*8, 30*8, new rectangle( 0*8, 37*8-1, 0*8, 30*8-1 ),
		gfxdecodeinfo,
		1024+1,null,	/* no colortable, we do the lookups ourselves */
					/* reserve color 1024 for black (disabled display) */
		null,
	
	    VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
	    jedi_vh_start,
	    jedi_vh_stop,
	    jedi_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_POKEY,
				pokey_interface
			),
			new MachineSound(
				SOUND_TMS5220,
				tms5220_interface
			)
	    },
	
		nvram_handler
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_jedi = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x1C000, REGION_CPU1 );/* 64k for code + 48k for banked ROMs */
		ROM_LOAD( "14f_221.bin",  0x08000, 0x4000, 0x414d05e3 );	ROM_LOAD( "13f_222.bin",  0x0c000, 0x4000, 0x7b3f21be );	ROM_LOAD( "13d_123.bin",  0x10000, 0x4000, 0x877f554a );/* Page 0 */
		ROM_LOAD( "13b_124.bin",  0x14000, 0x4000, 0xe72d41db );/* Page 1 */
		ROM_LOAD( "13a_122.bin",  0x18000, 0x4000, 0xcce7ced5 );/* Page 2 */
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* space for the sound ROMs */
		ROM_LOAD( "01c_133.bin",  0x8000, 0x4000, 0x6c601c69 );	ROM_LOAD( "01a_134.bin",  0xC000, 0x4000, 0x5e36c564 );
		ROM_REGION( 0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "11t_215.bin",  0x00000, 0x2000, 0x3e49491f );/* Alphanumeric */
	
		ROM_REGION( 0x10000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "06r_126.bin",  0x00000, 0x8000, 0x9c55ece8 );/* Playfield */
		ROM_LOAD( "06n_127.bin",  0x08000, 0x8000, 0x4b09dcc5 );
		ROM_REGION( 0x20000, REGION_GFX3 | REGIONFLAG_DISPOSE );	ROM_LOAD( "01h_130.bin",  0x00000, 0x8000, 0x2646a793 );/* Sprites */
		ROM_LOAD( "01f_131.bin",  0x08000, 0x8000, 0x60107350 );	ROM_LOAD( "01m_128.bin",  0x10000, 0x8000, 0x24663184 );	ROM_LOAD( "01k_129.bin",  0x18000, 0x8000, 0xac86b98c );
		ROM_REGION( 0x0800, REGION_PROMS );/* background smoothing */
		ROM_LOAD( "136030.117",   0x0000, 0x0400, 0x9831bd55 );	ROM_LOAD( "136030.118",   0x0400, 0x0400, 0x261fbfe7 );ROM_END(); }}; 
	
	
	
	public static GameDriver driver_jedi	   = new GameDriver("1984"	,"jedi"	,"jedi.java"	,rom_jedi,null	,machine_driver_jedi	,input_ports_jedi	,null	,ROT0	,	"Atari", "Return of the Jedi", GAME_NO_COCKTAIL )
}
