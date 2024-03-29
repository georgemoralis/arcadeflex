/* the way I hooked up the CTC is most likely completely wrong */

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;

public class dlair
{
	
	
	
	/*
	   Dragon's Lair has two 7 segment LEDs on the board, used to report error
	   codes.
	   The association between the bits of the port and the led segments is:
	
	    ---null---
	   |       |
	   5       1
	   |       |
	    ---6---
	   |       |
	   4       2
	   |       |
	    ---3---
	
	   bit 7 = enable (null = display off)
	
	   Error codes for led null:
	   1 bad CPU
	   2 bad ROM
	   3 bad RAM a000-a7ff
	   4 bad RAM c000-c7ff
	   5 bad I/O ports null-3
	   P ?
	 */
	
	static int led0,led1;
	
	public static WriteHandlerPtr dlair_led0_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		led0 = data;
	} };
	public static WriteHandlerPtr dlair_led1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		led1 = data;
	} };
	
	void dlair_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
	{
		int offs;
	
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = videoram_size - 2;offs >= 0;offs-=2)
		{
			if (dirtybuffer[offs] || dirtybuffer[offs+1])
			{
				int sx,sy;
	
	
				dirtybuffer[offs] = 0;
				dirtybuffer[offs+1] = 0;
	
				sx = (offs/2) % 32;
				sy = (offs/2) / 32;
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram[offs+1],
						0,
						0,0,
						8*sx,16*sy,
						&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
			}
		}
	
	
		/* copy the character mapped graphics */
		copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	
	if (led0 & 128)
	{
	if ((led0 & 1) == 0) drawgfx(bitmap,Machine.uifont,'x',0,0,0,
		8,0,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	if ((led0 & 2) == 0) drawgfx(bitmap,Machine.uifont,'x',0,0,0,
		16,8,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	if ((led0 & 4) == 0) drawgfx(bitmap,Machine.uifont,'x',0,0,0,
		16,24,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	if ((led0 & 8) == 0) drawgfx(bitmap,Machine.uifont,'x',0,0,0,
		8,32,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	if ((led0 & 16) == 0) drawgfx(bitmap,Machine.uifont,'x',0,0,0,
		0,24,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	if ((led0 & 32) == 0) drawgfx(bitmap,Machine.uifont,'x',0,0,0,
		0,8,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	if ((led0 & 64) == 0) drawgfx(bitmap,Machine.uifont,'x',0,0,0,
		8,16,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	}
	if (led1 & 128)
	{
	if ((led1 & 1) == 0) drawgfx(bitmap,Machine.uifont,'x',0,0,0,
		32+8,0,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	if ((led1 & 2) == 0) drawgfx(bitmap,Machine.uifont,'x',0,0,0,
		32+16,8,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	if ((led1 & 4) == 0) drawgfx(bitmap,Machine.uifont,'x',0,0,0,
		32+16,24,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	if ((led1 & 8) == 0) drawgfx(bitmap,Machine.uifont,'x',0,0,0,
		32+8,32,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	if ((led1 & 16) == 0) drawgfx(bitmap,Machine.uifont,'x',0,0,0,
		32+0,24,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	if ((led1 & 32) == 0) drawgfx(bitmap,Machine.uifont,'x',0,0,0,
		32+0,8,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	if ((led1 & 64) == 0) drawgfx(bitmap,Machine.uifont,'x',0,0,0,
		32+8,16,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	}
	}
	
	
	
	/* z80 ctc */
	static void ctc_interrupt (int state)
	{
		cpu_cause_interrupt (0, Z80_VECTOR(0,state) );
	}
	
	static z80ctc_interface ctc_intf =
	{
		1,                  /* 1 chip */
		{ 0 },              /* clock (filled in from the CPU 0 clock */
		{ 0 },              /* timer disables */
		{ ctc_interrupt },  /* interrupt handler */
		{ 0 },              /* ZC/TO0 callback */
		{ 0 },              /* ZC/TO1 callback */
		{ 0 }               /* ZC/TO2 callback */
	};
	
	
	void dlair_init_machine(void)
	{
	   /* initialize the CTC */
	   ctc_intf.baseclock[0] = Machine.drv.cpu[0].cpu_clock;
	   z80ctc_init(&ctc_intf);
	}
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0xa000, 0xa7ff, MRA_RAM ),
		new MemoryReadAddress( 0xc000, 0xc7ff, MRA_RAM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0xa000, 0xa7ff, MWA_RAM ),
		new MemoryWriteAddress( 0xc000, 0xc3ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0xc400, 0xc7ff, MWA_RAM ),
		new MemoryWriteAddress( 0xe000, 0xe000, dlair_led0_w ),
		new MemoryWriteAddress( 0xe008, 0xe008, dlair_led1_w ),
		new MemoryWriteAddress( 0xe030, 0xe030, watchdog_reset_w ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static unsigned char pip[4];
	public static ReadHandlerPtr pip_r = new ReadHandlerPtr() { public int handler(int offset)
	{
	if (errorlog) fprintf(errorlog,"PC %04x: read I/O port %02x\n",cpu_get_pc(),offset);
		return pip[offset];
	} };
	public static WriteHandlerPtr pip_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	if (errorlog) fprintf(errorlog,"PC %04x: write %02x to I/O port %02x\n",cpu_get_pc(),data,offset);
		pip[offset] = data;
	z80ctc_0_w(offset,data);
	} };
	
	static IOReadPort readport[] =
	{
		new IOReadPort( 0x00, 0x03, pip_r ),
	//	new IOReadPort( 0x80, 0x83, z80ctc_0_r ),
		new IOReadPort( -1 )	/* end of table */
	};
	
	static IOWritePort writeport[] =
	{
		new IOWritePort( 0x00, 0x03, pip_w ),
	//	new IOWritePort( 0x80, 0x83, z80ctc_0_w ),
		new IOWritePort( -1 )	/* end of table */
	};
	
	
	
	static InputPortPtr input_ports_dlair = new InputPortPtr(){ public void handler() { 
		PORT_START(); 
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,16,
		512,
		1,
		new int[] { 0 },
		new int[] { 7, 6, 5, 4, 3, 2, 1, 0 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		16*8	/* every char takes 8 consecutive bytes */
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0x0000, charlayout,  0, 8 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static Z80_DaisyChain daisy_chain[] =
	{
		{ z80ctc_reset, z80ctc_interrupt, z80ctc_reti, 0 }, /* CTC number 0 */
		{ 0,0,0,-1} 		/* end mark */
	};
	
	
	
	static MachineDriver machine_driver_dlair = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				3072000,	/* 3.072 Mhz ? */
				readmem,writemem,readport,writeport,
				null,null, /* interrupts are made by z80 daisy chain system */
				null,null,daisy_chain
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* single CPU, no need for interleaving */
		dlair_init_machine,
	
		/* video hardware */
		32*8, 32*8, { 0*8, 32*8-1, 0*8, 32*8-1 },
		gfxdecodeinfo,
		8,8,
		null,
	
		VIDEO_TYPE_RASTER|VIDEO_SUPPORTS_DIRTY,
		null,
		generic_vh_start,
		generic_vh_stop,
		dlair_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_dlair = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "u45",          0x0000, 0x2000, 0x329b354a );
		ROM_LOAD( "u46",          0x2000, 0x2000, 0x8479612b );
		ROM_LOAD( "u47",          0x4000, 0x2000, 0x6a66f6b4 );
		ROM_LOAD( "u48",          0x6000, 0x2000, 0x36575106 );
	
		ROM_REGION( 0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "u33",          0x0000, 0x2000, 0xe7506d96 );
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_dlair	   = new GameDriver("1983"	,"dlair"	,"dlair.java"	,rom_dlair,null	,machine_driver_dlair	,input_ports_dlair	,null	,ROT0	,	"Cinematronics", "Dragon's Lair", GAME_NOT_WORKING )
	
}
