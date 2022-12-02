/***************************************************************************

Exerion by Jaleco

Exerion is a unique driver in that it has idiosyncracies that are straight
out of Bizarro World. I submit for your approval:

* The mystery reads from $d802 - timer-based protection?
* The freakish graphics encoding scheme, which no other MAME-supported game uses
* The sprite-ram, and all the funky parameters that go along with it
* The unusual parallaxed background. Is it controlled by the 2nd CPU?

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;

public class exerion
{
	
	//#define TRY_CPU2
	
	void exerion_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom);
	int exerion_vh_start (void);
	void exerion_vh_stop (void);
	void exerion_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh);
	
	
	/* This is the first of many Exerion "features." No clue if it's */
	/* protection or some sort of timer. */
	static unsigned char porta;
	static unsigned char portb;
	
	static int exerion_porta_r (int offset)
	(
		porta ^= 0x40;
		return porta;
	)
	
	static void exerion_portb_w (int offset, int data)
	{
		/* pull the expected value from the ROM */
		porta = memory_region(REGION_CPU1)[0x5f76];
		portb = data;
	
		if (errorlog) fprintf(errorlog, "Port B = %02X\n", data);
	}
	
	public static ReadHandlerPtr exerion_protection_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		unsigned char *RAM;
		RAM = memory_region(REGION_CPU1);
	
		if (cpu_get_pc() == 0x4143)
			return RAM[0x33c0 + (RAM[0x600d] << 2) + offset];
		else
			return RAM[0x6008 + offset];
	} };
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x5fff, MRA_ROM ),
		new MemoryReadAddress( 0x6008, 0x600b, exerion_protection_r ),
		new MemoryReadAddress( 0x6000, 0x67ff, MRA_RAM ),
		new MemoryReadAddress( 0x8000, 0x8bff, MRA_RAM ),
		new MemoryReadAddress( 0xa000, 0xa000, input_port_0_r ),
		new MemoryReadAddress( 0xa800, 0xa800, input_port_1_r ),
		new MemoryReadAddress( 0xb000, 0xb000, input_port_2_r ),
		new MemoryReadAddress( 0xd802, 0xd802, AY8910_read_port_1_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x5fff, MWA_ROM ),
		new MemoryWriteAddress( 0x6000, 0x67ff, MWA_RAM ),
		new MemoryWriteAddress( 0x8000, 0x87ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x8800, 0x887f, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x8800, 0x8bff, MWA_RAM ),
		new MemoryWriteAddress( 0xc000, 0xc000, exerion_videoreg_w ),
		new MemoryWriteAddress( 0xc800, 0xc800, soundlatch_w ),
		new MemoryWriteAddress( 0xd000, 0xd000, AY8910_control_port_0_w ),
		new MemoryWriteAddress( 0xd001, 0xd001, AY8910_write_port_0_w ),
		new MemoryWriteAddress( 0xd800, 0xd800, AY8910_control_port_1_w ),
		new MemoryWriteAddress( 0xd801, 0xd801, AY8910_write_port_1_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	#ifdef TRY_CPU2
	static MemoryReadAddress cpu2_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x1fff, MRA_ROM ),
		new MemoryReadAddress( 0x4000, 0x47ff, MRA_RAM ),
		new MemoryReadAddress( 0x6000, 0x6000, soundlatch_r ),
	//  new MemoryReadAddress( 0xa000, 0xa000, MRA_NOP ), /* ??? */
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress cpu2_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x1fff, MWA_ROM ),
		new MemoryWriteAddress( 0x4000, 0x47ff, MWA_RAM ),
	//  new MemoryWriteAddress( 0x8001, 0x8001, MWA_RAM ),
	//  new MemoryWriteAddress( 0x8003, 0x8003, MWA_RAM ),
	//  new MemoryWriteAddress( 0x8005, 0x8005, MWA_RAM ),
	//  new MemoryWriteAddress( 0x8007, 0x8007, MWA_RAM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	#endif
	
	
	/* Exerion triggers NMIs on vblank */
	public static InterruptPtr exerion_interrupt = new InterruptPtr() { public int handler() 
	{
		if (readinputport(3) & 1)
			return nmi_interrupt();
		else return ignore_interrupt();
	} };
	
	static InputPortPtr input_ports_exerion = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
	
		PORT_START();       /* DSW0 */
		PORT_DIPNAME( 0x07, 0x02, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "1" );
		PORT_DIPSETTING(    0x01, "2" );
		PORT_DIPSETTING(    0x02, "3" );
		PORT_DIPSETTING(    0x03, "4" );
		PORT_DIPSETTING(    0x04, "5" );
		PORT_BITX(0,        0x07, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite", IP_KEY_NONE, IP_JOY_NONE );
		PORT_DIPNAME( 0x18, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "10000" );
		PORT_DIPSETTING(    0x08, "20000" );
		PORT_DIPSETTING(    0x10, "30000" );
		PORT_DIPSETTING(    0x18, "40000" );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );      /* used */
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x00, "Easy" );
		PORT_DIPSETTING(    0x40, "Hard" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Cocktail") );
	
		PORT_START();       /* VBLANK */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_VBLANK );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x0c, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* FAKE */
		/* The coin slots are not memory mapped. */
		/* This fake input port is used by the interrupt */
		/* handler to be notified of coin insertions. We use IMPULSE to */
		/* trigger exactly one interrupt, without having to check when the */
		/* user releases the key. */
		PORT_BIT_IMPULSE( 0x01, IP_ACTIVE_HIGH, IPT_COIN1, 1 );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,            /* 8*8 characters */
		512,          /* total number of chars */
		2,              /* 2 bits per pixel (# of planes) */
		new int[] { 0, 4 },       /* start of every bitplane */
		new int[] { 3, 2, 1, 0, 8+3, 8+2, 8+1, 8+0 },
		new int[] { 16*0, 16*1, 16*2, 16*3, 16*4, 16*5, 16*6, 16*7 },
		16*8            /* every char takes 16 consecutive bytes */
	);
	
	static GfxLayout bgcharlayout = new GfxLayout
	(
		32,32,            /* 64*8 characters */
		128,          /* total number of chars */
		2,              /* 2 bits per pixel (# of planes) */
		new int[] { 0, 4 },       /* start of every bitplane */
		new int[] { 3, 2, 1, 0, 8+3, 8+2, 8+1, 8+0,
				2*8+3, 2*8+2, 2*8+1, 2*8+0, 3*8+3, 3*8+2, 3*8+1, 3*8+0,
				4*8+3, 4*8+2, 4*8+1, 4*8+0, 5*8+3, 5*8+2, 5*8+1, 5*8+0,
				6*8+3, 6*8+2, 6*8+1, 6*8+0, 7*8+3, 7*8+2, 7*8+1, 7*8+0 },
		new int[] { 64*0, 64*1, 64*2, 64*3, 64*4, 64*5, 64*6, 64*7,
				64*8, 64*9, 64*10, 64*11, 64*12, 64*13, 64*14, 64*15,
				64*16, 64*17, 64*18, 64*19, 64*20, 64*21, 64*22, 64*23,
				64*24, 64*25, 64*26, 64*27, 64*28, 64*29, 64*30, 64*31 },
		64*32            /* every char takes 16 consecutive bytes */
	);
	
	/* 16 x 16 sprites -- requires reorganizing characters in init_exerion() */
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,          /* 16*16 sprites */
		128*2,          /* total number of sprites in the rom */
		2,              /* 2 bits per pixel (# of planes) */
		new int[] { 0, 4 },       /* start of every bitplane */
		new int[] {  3, 2, 1, 0, 8+3, 8+2, 8+1, 8+0,
				128+3, 128+2, 128+1, 128+0, 128+8+3, 128+8+2, 128+8+1, 128+8+0 },
		new int[] { 16*0, 16*1, 16*2, 16*3, 16*4, 16*5, 16*6, 16*7,
				256+16*0, 256+16*1, 256+16*2, 256+16*3, 256+16*4, 256+16*5, 256+16*6, 256+16*7 },
		64*8            /* every sprite takes 64 consecutive bytes */
	);
	
	/* Quick and dirty way to emulate pixel-doubled sprites. */
	static GfxLayout bigspritelayout = new GfxLayout
	(
		32,32,          /* 32*32 sprites */
		128*2,          /* total number of sprites in the rom */
		2,              /* 2 bits per pixel (# of planes) */
		new int[] { 0, 4 },       /* start of every bitplane */
		new int[] { 3, 3, 2, 2, 1, 1, 0, 0, 8+3, 8+3, 8+2, 8+2, 8+1, 8+1, 8+0, 8+0,
				128+3, 128+3, 128+2, 128+2, 128+1, 128+1, 128+0, 128+0,
				128+8+3, 128+8+3, 128+8+2, 128+8+2, 128+8+1, 128+8+1, 128+8+0, 128+8+0 },
		new int[] { 16*0, 16*0, 16*1, 16*1, 16*2, 16*2, 16*3, 16*3,
				16*4, 16*4, 16*5, 16*5, 16*6, 16*6, 16*7, 16*7,
				256+16*0, 256+16*0, 256+16*1, 256+16*1, 256+16*2, 256+16*2, 256+16*3, 256+16*3,
				256+16*4, 256+16*4, 256+16*5, 256+16*5, 256+16*6, 256+16*6, 256+16*7, 256+16*7 },
		64*8            /* every sprite takes 64 consecutive bytes */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,           0, 64 ),
		new GfxDecodeInfo( REGION_GFX2, 0, bgcharlayout,      64*4, 64 ),
		new GfxDecodeInfo( REGION_GFX3, 0, spritelayout,    2*64*4, 64 ),
		new GfxDecodeInfo( REGION_GFX3, 0, bigspritelayout, 2*64*4, 64 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static struct AY8910interface ay8910_interface =
	{
		2,  /* 2 chips */
		10000000/6, /* 1.666 MHz */
		{ 30, 30 },
		{ 0, exerion_porta_r },
		{ 0 },
		{ 0 },
		{ 0, exerion_portb_w }
	};
	
	
	
	static MachineDriver machine_driver_exerion = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				10000000/3, /* 3.333 MHz */
				readmem,writemem,null,null,
				exerion_interrupt,1
			),
	#ifdef TRY_CPU2
			new MachineCPU(
				CPU_Z80,
				10000000/3, /* 3.333 MHz */
				cpu2_readmem,cpu2_writemem,null,null,
				ignore_interrupt,0
			)
	#endif
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,  /* frames per second, vblank duration */
		1,  /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		64*8, 32*8, new rectangle( 12*8, 52*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		32,64*4+64*4+64*4,
		exerion_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER|VIDEO_SUPPORTS_DIRTY,
		null,
		exerion_vh_start,
		exerion_vh_stop,
		exerion_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_AY8910,
				ay8910_interface
			)
		}
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_exerion = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "exerion.07",   0x0000, 0x2000, 0x4c78d57d );
		ROM_LOAD( "exerion.08",   0x2000, 0x2000, 0xdcadc1df );
		ROM_LOAD( "exerion.09",   0x4000, 0x2000, 0x34cc4d14 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for the second CPU */
		ROM_LOAD( "exerion.05",   0x0000, 0x2000, 0x32f6bff5 );
	
		ROM_REGION( 0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "exerion.06",   0x00000, 0x2000, 0x435a85a4 );/* fg chars */
	
		ROM_REGION( 0x08000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "exerion.03",   0x00000, 0x2000, 0x790595b8 );/* bg chars */
		ROM_LOAD( "exerion.04",   0x02000, 0x2000, 0xd7abd0b9 );
		ROM_LOAD( "exerion.01",   0x04000, 0x2000, 0x5bb755cb );
		ROM_LOAD( "exerion.02",   0x06000, 0x2000, 0xa7ecbb70 );
	
		ROM_REGION( 0x04000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "exerion.11",   0x00000, 0x2000, 0xf0633a09 );/* sprites */
		ROM_LOAD( "exerion.10",   0x02000, 0x2000, 0x80312de0 );
	
		ROM_REGION( 0x0420, REGION_PROMS );
		ROM_LOAD( "exerion.e1",   0x0000, 0x0020, 0x2befcc20 );/* palette */
		ROM_LOAD( "exerion.i8",   0x0020, 0x0100, 0x31db0e08 );/* fg char lookup table */
		ROM_LOAD( "exerion.h10",  0x0120, 0x0100, 0xcdd23f3e );/* sprite lookup table */
		ROM_LOAD( "exerion.i3",   0x0220, 0x0100, 0xfe72ab79 );/* bg char lookup table */
		ROM_LOAD( "exerion.k4",   0x0320, 0x0100, 0xffc2ba43 );/* bg char mixer */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_exeriont = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "prom5.4p",     0x0000, 0x4000, 0x58b4dc1b );
		ROM_LOAD( "prom6.4s",     0x4000, 0x2000, 0xfca18c2d );
	
		ROM_REGION( 0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "exerion.06",   0x00000, 0x2000, 0x435a85a4 );/* fg chars */
	
		ROM_REGION( 0x08000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "exerion.03",   0x00000, 0x2000, 0x790595b8 );/* bg chars */
		ROM_LOAD( "exerion.04",   0x02000, 0x2000, 0xd7abd0b9 );
		ROM_LOAD( "exerion.01",   0x04000, 0x2000, 0x5bb755cb );
		ROM_LOAD( "exerion.02",   0x06000, 0x2000, 0xa7ecbb70 );
	
		ROM_REGION( 0x04000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "exerion.11",   0x00000, 0x2000, 0xf0633a09 );/* sprites */
		ROM_LOAD( "exerion.10",   0x02000, 0x2000, 0x80312de0 );
	
		ROM_REGION( 0x0420, REGION_PROMS );
		ROM_LOAD( "exerion.e1",   0x0000, 0x0020, 0x2befcc20 );/* palette */
		ROM_LOAD( "exerion.i8",   0x0020, 0x0100, 0x31db0e08 );/* fg char lookup table */
		ROM_LOAD( "exerion.h10",  0x0120, 0x0100, 0xcdd23f3e );/* sprite lookup table */
		ROM_LOAD( "exerion.i3",   0x0220, 0x0100, 0xfe72ab79 );/* bg char lookup table */
		ROM_LOAD( "exerion.k4",   0x0320, 0x0100, 0xffc2ba43 );/* bg char mixer */
	
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for the second CPU */
		ROM_LOAD( "exerion.05",   0x0000, 0x2000, 0x32f6bff5 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_exerionb = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "eb5.bin",      0x0000, 0x4000, 0xda175855 );
		ROM_LOAD( "eb6.bin",      0x4000, 0x2000, 0x0dbe2eff );
	
		ROM_REGION( 0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "exerion.06",   0x00000, 0x2000, 0x435a85a4 );/* fg chars */
	
		ROM_REGION( 0x08000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "exerion.03",   0x00000, 0x2000, 0x790595b8 );/* bg chars */
		ROM_LOAD( "exerion.04",   0x02000, 0x2000, 0xd7abd0b9 );
		ROM_LOAD( "exerion.01",   0x04000, 0x2000, 0x5bb755cb );
		ROM_LOAD( "exerion.02",   0x06000, 0x2000, 0xa7ecbb70 );
	
		ROM_REGION( 0x04000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "exerion.11",   0x00000, 0x2000, 0xf0633a09 );/* sprites */
		ROM_LOAD( "exerion.10",   0x02000, 0x2000, 0x80312de0 );
	
		ROM_REGION( 0x0420, REGION_PROMS );
		ROM_LOAD( "exerion.e1",   0x0000, 0x0020, 0x2befcc20 );/* palette */
		ROM_LOAD( "exerion.i8",   0x0020, 0x0100, 0x31db0e08 );/* fg char lookup table */
		ROM_LOAD( "exerion.h10",  0x0120, 0x0100, 0xcdd23f3e );/* sprite lookup table */
		ROM_LOAD( "exerion.i3",   0x0220, 0x0100, 0xfe72ab79 );/* bg char lookup table */
		ROM_LOAD( "exerion.k4",   0x0320, 0x0100, 0xffc2ba43 );/* bg char mixer */
	
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64k for the second CPU */
		ROM_LOAD( "exerion.05",   0x0000, 0x2000, 0x32f6bff5 );
	ROM_END(); }}; 
	
	
	
	static void untangle(unsigned char *dst,unsigned char *src,int length)
	{
		int i, x, offs;
	
		for (i = 0;i < length/16;i++)
		{
			offs = (i/16)*2048 + (i%16)*16;
	
			for (x=0; x<8; x++)
			{
				int d, s;
	
				d = i*16 + x*2;
				s = (offs + 256*x) / 8;
	
				dst[d] = src[s];
				dst[d+1] = src[s+1];
			}
		}
	}
	
	public static InitDriverPtr init_exerion = new InitDriverPtr() { public void handler() 
	{
		int i, offs;
		unsigned char *rom;
		unsigned char *tmp;
	
		/*
			The gfx ROMs are stored in a half-assed fashion that makes using them with
			MAME challenging. We shuffle the bytes around to make it easier.
		*/
	
		tmp = malloc(0x8000);
		if (!tmp) return;
	
		rom = memory_region(REGION_GFX1);
		memcpy(tmp,rom,memory_region_length(REGION_GFX1));
	
		untangle(rom,tmp,memory_region_length(REGION_GFX1));
	
	
		/* reorganize bg chars */
		rom = memory_region(REGION_GFX2);
		memcpy(tmp,rom,memory_region_length(REGION_GFX2));
	
		for(i = 0;i < memory_region_length(REGION_GFX2);i++)
		{
			int s;
	
			s = (i & 0x6000) | ((i & 0x1fe0) >> 2) | ((i & 0x0018) << 8) | (i & 0x0007);
	
			rom[s] = tmp[i];
		}
	
		/* now reorganize the characters that form the sprites, and the sprites */
	
		rom = memory_region(REGION_GFX3);
		memcpy(tmp,rom,memory_region_length(REGION_GFX3));
	
		untangle(rom,tmp,memory_region_length(REGION_GFX3));
	
		/* get a fresh copy to work with */
		memcpy(tmp,rom,memory_region_length(REGION_GFX3));
	
		/* for each of the 256 sprites, make the 4 characters contiguous */
		for (i=0; i<(128*2); i++)
		{
			int d, s;
	
			/* this assumes that sprite 1 is offset by 32 characters (32*16 bytes) from sprite 0 */
			offs = 16 * ((i/32)*2 + (i%32)*32);     // byte offset of sprite
	
			/* this assumes that sprite 1 is right next to sprite 0 (informational only) */
			//offs = 16 * i * 2; // byte offset of sprite
	
			d = i*64;
			s = offs;
	
			memcpy(&rom[d], &tmp[s], 32);  // first 32 bytes
			memcpy(&rom[d+32], &tmp[s+256], 32); // next 32 bytes
		}
	
		free(tmp);
	} };
	
	public static InitDriverPtr init_exerionb = new InitDriverPtr() { public void handler() 
	{
		int A;
		unsigned char *RAM = memory_region(REGION_CPU1);
	
	
		/* the program ROMs have data lines D1 and D2 swapped. Decode them. */
		for (A = 0;A < 0x6000;A++)
			RAM[A] = (RAM[A] & 0xf9) | ((RAM[A] & 2) << 1) | ((RAM[A] & 4) >> 1);
	
		/* also convert the gfx as in Exerion */
		init_exerion();
	} };
	
	
	
	public static GameDriver driver_exerion	   = new GameDriver("1983"	,"exerion"	,"exerion.java"	,rom_exerion,null	,machine_driver_exerion	,input_ports_exerion	,init_exerion	,ROT90	,	"Jaleco", "Exerion", GAME_WRONG_COLORS | GAME_NO_COCKTAIL )
	public static GameDriver driver_exeriont	   = new GameDriver("1983"	,"exeriont"	,"exerion.java"	,rom_exeriont,driver_exerion	,machine_driver_exerion	,input_ports_exerion	,init_exerion	,ROT90	,	"Jaleco (Taito America license)", "Exerion (Taito)", GAME_WRONG_COLORS | GAME_NO_COCKTAIL )
	public static GameDriver driver_exerionb	   = new GameDriver("1983"	,"exerionb"	,"exerion.java"	,rom_exerionb,driver_exerion	,machine_driver_exerion	,input_ports_exerion	,init_exerionb	,ROT90	,	"Jaleco", "Exerion (bootleg)", GAME_WRONG_COLORS | GAME_NO_COCKTAIL )
}
