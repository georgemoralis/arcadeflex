/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;

public class kopunch
{
	
	unsigned char *bsvideoram;
	int bsvideoram_size;
	
	void kopunch_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom)
	(
		int i;
	
	
	color_prom+=32+32+16+8;
		for (i = null;i < Machine.drv.total_colors;i++)
		(
			int bit0,bit1,bit2;
	
	
			/* red component */
			bit0 = (*color_prom >> null)  0x01;
			bit1 = (*color_prom >> 1)  0x01;
			bit2 = (*color_prom >> 2)  0x01;
			*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* green component */
			bit0 = (*color_prom >> 3)  0x01;
			bit1 = (*color_prom >> 4)  0x01;
			bit2 = (*color_prom >> 5)  0x01;
			*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* blue component */
			bit0 = null;
			bit1 = (*color_prom >> 6)  0x01;
			bit2 = (*color_prom >> 7)  0x01;
			*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	
			color_prom++;
		)
	}
	
	void kopunch_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
	{
		int offs;
	static int bank=0;
	
	if (keyboard_pressed_memory(KEYCODE_Z)) bank--;
	if (keyboard_pressed_memory(KEYCODE_X)) bank++;
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = videoram_size - 1;offs >= 0;offs--)
		{
			if (dirtybuffer[offs])
			{
				int sx,sy;
	
	
				dirtybuffer[offs] = 0;
	
				sx = offs % 32;
				sy = offs / 32;
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram[offs],
						0,
						0,0,
						8*sx,8*sy,
						&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
			}
		}
	
	
		/* copy the character mapped graphics */
		copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	
	
		for (offs = bsvideoram_size - 1;offs >= 0;offs--)
		{
			int sx,sy;
	
	
			sx = offs % 16 + 8;
			sy = offs / 16 + 8;
	
			drawgfx(bitmap,Machine.gfx[1],
					bsvideoram[offs] + 128 * bank,
					0,
					0,0,
					8*sx,8*sy,
					&Machine.drv.visible_area,TRANSPARENCY_PEN,0);
		}
	}
	
	
	public static InterruptPtr kopunch_interrupt = new InterruptPtr() { public int handler() 
	{
		if (keyboard_pressed(KEYCODE_Q)) return 0xef;	/* RST 28h */
		if (keyboard_pressed(KEYCODE_W)) return 0xf7;	/* RST 30h */
		if (keyboard_pressed(KEYCODE_E)) return 0xff;	/* RST 38h */
	
		return ignore_interrupt();
	} };
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x1fff, MRA_ROM ),
		new MemoryReadAddress( 0x2000, 0x23ff, MRA_RAM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x1fff, MWA_ROM ),
		new MemoryWriteAddress( 0x2000, 0x23ff, MWA_RAM ),
		new MemoryWriteAddress( 0x6000, 0x63ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x7000, 0x70ff, MWA_RAM, bsvideoram, bsvideoram_size ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static IOReadPort readport[] =
	{
		new IOReadPort( -1 )	/* end of table */
	};
	
	static IOWritePort writeport[] =
	{
		new IOWritePort( -1 )	/* end of table */
	};
	
	
	
	
	
	static InputPortPtr input_ports_kopunch = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,
		256,
		3,
		new int[] { 2*256*8*8, 1*256*8*8, 0*256*8*8 },
		new int[] { 7, 6, 5, 4, 3, 2, 1, 0 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8
	);
	
	static GfxLayout charlayout2 = new GfxLayout
	(
		8,8,
		1024,
		3,
		new int[] { 2*1024*8*8, 1*1024*8*8, 0*1024*8*8 },
		new int[] { 7, 6, 5, 4, 3, 2, 1, 0 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,  0, 1 ),
		new GfxDecodeInfo( REGION_GFX2, 0, charlayout2, 0, 1 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static MachineDriver machine_driver_kopunch = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				3072000,	/* 3.072 Mhz ? */
				readmem,writemem,readport,writeport,
				kopunch_interrupt,1
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 0*8, 32*8-1 ),
		gfxdecodeinfo,
		8,8,
		kopunch_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		generic_vh_start,
		generic_vh_stop,
		kopunch_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_kopunch = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "epr1105.x",    0x0000, 0x1000, 0x34ef5e79 );
		ROM_LOAD( "epr1106.x",    0x1000, 0x1000, 0x25a5c68b );
	
		ROM_REGION( 0x1800, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "epr1102",      0x0000, 0x0800, 0x8a52de96 );
		ROM_LOAD( "epr1103",      0x0800, 0x0800, 0xbae5e054 );
		ROM_LOAD( "epr1104",      0x1000, 0x0800, 0x7b119a0e );
	
		ROM_REGION( 0x6000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "epr1107",      0x0000, 0x1000, 0xca00244d );
		ROM_LOAD( "epr1108",      0x1000, 0x1000, 0xcc17c5ed );
		ROM_LOAD( "epr1110",      0x2000, 0x1000, 0xae0aff15 );
		ROM_LOAD( "epr1109",      0x3000, 0x1000, 0x625446ba );
		ROM_LOAD( "epr1112",      0x4000, 0x1000, 0xef6994df );
		ROM_LOAD( "epr1111",      0x5000, 0x1000, 0x28530ec9 );
	
		ROM_REGION( 0x0060, REGION_PROMS );
		ROM_LOAD( "epr1099",      0x0000, 0x0020, 0xfc58c456 );
		ROM_LOAD( "epr1100",      0x0020, 0x0020, 0xbedb66b1 );
		ROM_LOAD( "epr1101",      0x0040, 0x0020, 0x15600f5d );
	ROM_END(); }}; 
	
	
	public static InitDriverPtr init_kopunch = new InitDriverPtr() { public void handler() 
	{
		unsigned char *RAM = memory_region(REGION_CPU1);
	
		/* patch out bad instruction, either the ROM is bad, or there is */
		/* a security chip */
		RAM[0x119] = 0;
	} };
	
	
	public static GameDriver driver_kopunch	   = new GameDriver("1981"	,"kopunch"	,"kopunch.java"	,rom_kopunch,null	,machine_driver_kopunch	,input_ports_kopunch	,init_kopunch	,ROT270	,	"Sega", "KO Punch" )
}
