/***************************************************************************

	BattleLane
	1986 Taito

	2x6809

    Driver provided by Paul Leaman (paul@vortexcomputing.demon.co.uk)

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class battlane
{
	
	extern extern extern extern 
	extern UBytePtr battlane_bitmap;
	extern size_t battlane_bitmap_size;
	extern void battlane_set_video_flip(int);
	
	
	/* CPU interrupt control register */
	int battlane_cpu_control;
	
	/* RAM shared between CPU 0 and 1 */
	public static WriteHandlerPtr battlane_shared_ram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		UBytePtr RAM =
			memory_region(REGION_CPU1);
		RAM[offset]=data;
	} };
	
	public static ReadHandlerPtr battlane_shared_ram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UBytePtr RAM =
			memory_region(REGION_CPU1);
		return RAM[offset];
	} };
	
	
	public static WriteHandlerPtr battlane_cpu_command_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		battlane_cpu_control=data;
	
		/*
		CPU control register
	
	        0x80    = Video Flip
	        0x08    = NMI
	        0x04    = CPU 0 IRQ   (0=Activate)
	        0x02    = CPU 1 IRQ   (0=Activate)
	        0x01    = Scroll MSB
		*/
	
	    battlane_set_video_flip(data & 0x80);
	
		/*
	        I think that the NMI is an inhibitor. It is constantly set
	        to zero whenever an NMIs are allowed.
	
	        However, it could also be that setting to zero could
	        cause the NMI to trigger. I really don't know.
		*/
	
	    /*
	    if (~battlane_cpu_control & 0x08)
	    {
	        cpu_set_nmi_line(0, PULSE_LINE);
	        cpu_set_nmi_line(1, PULSE_LINE);
	    }
	    */
	
		/*
	        CPU2's SWI will trigger an 6809 IRQ on the master by resetting 0x04
	        Master will respond by setting the bit back again
		*/
	    cpu_set_irq_line(0, M6809_IRQ_LINE,  data & 0x04 ? CLEAR_LINE : HOLD_LINE);
	
		/*
		Slave function call (e.g. ROM test):
		FA7F: 86 03       LDA   #$03	; Function code
		FA81: 97 6B       STA   $6B
		FA83: 86 0E       LDA   #$0E
		FA85: 84 FD       ANDA  #$FD	; Trigger IRQ
		FA87: 97 66       STA   $66
		FA89: B7 1C 03    STA   $1C03	; Do Trigger
		FA8C: C6 40       LDB   #$40
		FA8E: D5 68       BITB  $68
		FA90: 27 FA       BEQ   $FA8C	; Wait for slave IRQ pre-function dispatch
		FA92: 96 68       LDA   $68
		FA94: 84 01       ANDA  #$01
		FA96: 27 FA       BEQ   $FA92	; Wait for bit to be set
		*/
	
		cpu_set_irq_line(1, M6809_IRQ_LINE,   data & 0x02 ? CLEAR_LINE : HOLD_LINE);
	} };
	
	/* Both CPUs share the same memory */
	
	static MemoryReadAddress battlane_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x0fff, battlane_shared_ram_r ),
	    new MemoryReadAddress( 0x1000, 0x17ff, battlane_tileram_r ),
	    new MemoryReadAddress( 0x1800, 0x18ff, battlane_spriteram_r ),
		new MemoryReadAddress( 0x1c00, 0x1c00, input_port_0_r ),
	    new MemoryReadAddress( 0x1c01, 0x1c01, input_port_1_r ),   /* VBLANK port */
		new MemoryReadAddress( 0x1c02, 0x1c02, input_port_2_r ),
		new MemoryReadAddress( 0x1c03, 0x1c03, input_port_3_r ),
		new MemoryReadAddress( 0x1c04, 0x1c04, YM3526_status_port_0_r ),
		new MemoryReadAddress( 0x2000, 0x3fff, battlane_bitmap_r ),
		new MemoryReadAddress( 0x4000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress battlane_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x0fff, battlane_shared_ram_w ),
	    new MemoryWriteAddress( 0x1000, 0x17ff, battlane_tileram_w ),
	    new MemoryWriteAddress( 0x1800, 0x18ff, battlane_spriteram_w ),
		new MemoryWriteAddress( 0x1c00, 0x1c00, battlane_video_ctrl_w ),
	    new MemoryWriteAddress( 0x1c01, 0x1c01, battlane_scrolly_w ),
	    new MemoryWriteAddress( 0x1c02, 0x1c02, battlane_scrollx_w ),
	    new MemoryWriteAddress( 0x1c03, 0x1c03, battlane_cpu_command_w ),
		new MemoryWriteAddress( 0x1c04, 0x1c04, YM3526_control_port_0_w ),
		new MemoryWriteAddress( 0x1c05, 0x1c05, YM3526_write_port_0_w ),
		new MemoryWriteAddress( 0x1e00, 0x1e3f, MWA_RAM ), /* Palette ??? */
		new MemoryWriteAddress( 0x2000, 0x3fff, battlane_bitmap_w, battlane_bitmap, battlane_bitmap_size ),
		new MemoryWriteAddress( 0x4000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	public static InterruptPtr battlane_cpu1_interrupt = new InterruptPtr() { public int handler() 
	{
	#ifdef MAME_DEBUG
		if (keyboard_pressed(KEYCODE_F))
		{
			FILE *fp;
			while (keyboard_pressed(KEYCODE_F)) ;
			fp=fopen("RAM.DMP", "w+b");
			if (fp != 0)
			{
				UBytePtr RAM =
				memory_region(REGION_CPU1);
	
				fwrite(RAM, 0x4000, 1, fp);
				fclose(fp);
			}
		}
	#endif
	
	    if (cpu_getiloops()==0)
		{
	        /* See note in battlane_cpu_command_w */
	        if (~battlane_cpu_control & 0x08)
	        {
	            cpu_set_nmi_line(1, PULSE_LINE);
	            return M6809_INT_NMI;
	        }
	        else
	            return ignore_interrupt();
		}
	    else
		{
	        /*
			FIRQ seems to drive the music & coin inputs. I have no idea what it is
			attached to
			*/
			return M6809_INT_FIRQ;
		}
	} };
	
	public static InterruptPtr battlane_cpu2_interrupt = new InterruptPtr() { public int handler() 
	{
		/* CPU2's interrupts are generated on-demand by CPU1 */
		return ignore_interrupt();
	} };
	
	
	static InputPortPtr input_ports_battlane = new InputPortPtr(){ public void handler() { 
	PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
	    PORT_START();       /* IN1 */
	    PORT_BIT( 0x7f, IP_ACTIVE_LOW, IPT_UNUSED );    /* Unused bits */
	    PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_VBLANK );    /* VBLank ? */
	
		PORT_START();       /* DSW1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") ); )
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0xc0, 0x80, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0xc0, "Easy"  );	PORT_DIPSETTING(    0x80, "Normal" );	PORT_DIPSETTING(    0x40, "Hard"  );	PORT_DIPSETTING(    0x00, "Very Hard" );
		PORT_START();       /* DSW2 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "3" );	PORT_DIPSETTING(    0x02, "4" );	PORT_DIPSETTING(    0x01, "5" );	PORT_BITX(0,        0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite", IP_KEY_NONE, IP_JOY_NONE );	PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x0c, "20k & every 50k" );	PORT_DIPSETTING(    0x08, "20k & every 70k" );	PORT_DIPSETTING(    0x04, "20k & every 90k" );	PORT_DIPSETTING(    0x00, "None" );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN3 );INPUT_PORTS_END(); }}; 
	
	static GfxLayout spritelayout = new GfxLayout
	(
	    16,16,  /* 16*16 sprites */
	    0x0400,    /* 0x400 sprites */
		6,      /* 6 bits per pixel ??!!! */
	    new int[] { 0, 8, 0x08000*8,0x08000*8+8, 0x10000*8, 0x10000*8+8},
	    new int[] {
	        7, 6, 5, 4, 3, 2, 1, 0,
	      16*8+7, 16*8+6, 16*8+5, 16*8+4, 16*8+3, 16*8+2, 16*8+1, 16*8+0,
	    },
	    new int[] {
	        14*8,14*8,13*8,12*8,11*8,10*8,9*8,8*8,
	        7*8, 6*8, 5*8, 4*8, 3*8, 2*8, 1*8, 0*8,
	    },
	    16*8*2     /* every char takes 16*8*2 consecutive bytes */
	);
	
	static GfxLayout tilelayout = new GfxLayout
	(
		16,16 ,    /* 16*16 tiles */
		256,    /* 256 tiles */
		3,      /* 3 bits per pixel */
	    new int[] {   4, 0, 0x8000*8+4 },    /* plane offset */
		new int[] {
		16+8+0, 16+8+1, 16+8+2, 16+8+3,
		16+0, 16+1, 16+2,   16+3,
		8+0,    8+1,    8+2,    8+3,
		0,       1,    2,      3,
		},
		new int[] { 0*8*4, 1*8*4,  2*8*4,  3*8*4,  4*8*4,  5*8*4,  6*8*4,  7*8*4,
		  8*8*4, 9*8*4, 10*8*4, 11*8*4, 12*8*4, 13*8*4, 14*8*4, 15*8*4
		},
		8*8*4*2     /* every char takes consecutive bytes */
	);
	
	static GfxLayout tilelayout2 = new GfxLayout
	(
		16,16 ,    /* 16*16 tiles */
		256,    /* 256 tiles */
		3,      /* 3 bits per pixel */
	    new int[] { 0x4000*8+4, 0x8000*8, 0x4000*8+0, },    /* plane offset */
		new int[] {
		16+8+0, 16+8+1, 16+8+2, 16+8+3,
		16+0, 16+1, 16+2,   16+3,
		8+0,    8+1,    8+2,    8+3,
		0,       1,    2,      3,
		},
		new int[] { 0*8*4, 1*8*4,  2*8*4,  3*8*4,  4*8*4,  5*8*4,  6*8*4,  7*8*4,
		  8*8*4, 9*8*4, 10*8*4, 11*8*4, 12*8*4, 13*8*4, 14*8*4, 15*8*4
		},
		8*8*4*2     /* every char takes consecutive bytes */
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, spritelayout, 0, 32),
		new GfxDecodeInfo( REGION_GFX2, 0, tilelayout,   0, 32),
		new GfxDecodeInfo( REGION_GFX2, 0, tilelayout2,  0, 32),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static YM3526interface ym3526_interface = new YM3526interface
	(
	    1,              /* 1 chip (no more supported) */
	    3600000,        /* 3.600000 MHz ? (partially supported) */
	    new int[] { 255 }         /* (not supported) */
	);
	
	
	static MachineDriver machine_driver_battlane = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6809,
	            1250000,        /* 1.25 MHz ? */
	            battlane_readmem, battlane_writemem,null,null,
	            battlane_cpu1_interrupt,2
			),
			new MachineCPU(
				CPU_M6809,
	            1250000,        /* 1.25 MHz ? */
	            battlane_readmem, battlane_writemem,null,null,
	            battlane_cpu2_interrupt,1
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,  /* frames per second, vblank duration */
	    null,      /* CPU slices per frame */
	    null,      /* init machine */
	
		/* video hardware */
		32*8, 32*8, new rectangle( 1*8, 31*8-1, 1*8, 31*8-1 ),       /* not sure */
		gfxdecodeinfo,
	    0x40,0x40,
	    NULL, //battlane_vh_convert_color_prom,
	
	    VIDEO_TYPE_RASTER |VIDEO_MODIFIES_PALETTE,
	    null ,
		battlane_vh_start,
		battlane_vh_stop,
		battlane_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
		    new MachineSound(
				SOUND_YM3526,
				ym3526_interface,
		    )
		}
	);
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_battlane = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for main CPU */
		/* first half of da00-5 will be copied at 0x4000-0x7fff */
		ROM_LOAD( "da01-5",    0x8000, 0x8000, 0x7a6c3f02 );
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64K for slave CPU */
		ROM_LOAD( "da00-5",    0x00000, 0x8000, 0x85b4ed73 );/* ...second half goes here */
		ROM_LOAD( "da02-2",    0x08000, 0x8000, 0x69d8dafe );
		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "da05",      0x00000, 0x8000, 0x834829d4 );/* Sprites Plane 1+2 */
		ROM_LOAD( "da04",      0x08000, 0x8000, 0xf083fd4c );/* Sprites Plane 3+4 */
		ROM_LOAD( "da03",      0x10000, 0x8000, 0xcf187f25 );/* Sprites Plane 5+6 */
	
		ROM_REGION( 0x0c000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "da06",      0x00000, 0x8000, 0x9c6a51b3 );/* Tiles*/
		ROM_LOAD( "da07",      0x08000, 0x4000, 0x56df4077 );/* Tiles*/
	
		ROM_REGION( 0x0040, REGION_PROMS );	ROM_LOAD( "82s123.7h", 0x00000, 0x0020, 0xb9933663 );/* palette? */
		ROM_LOAD( "82s123.9n", 0x00020, 0x0020, 0x06491e53 );/* palette? */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_battlan2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for main CPU */
		/* first half of da00-3 will be copied at 0x4000-0x7fff */
		ROM_LOAD( "da01-3",    0x8000, 0x8000, 0xd9e40800 );
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64K for slave CPU */
		ROM_LOAD( "da00-3",    0x00000, 0x8000, 0x7a0a5d58 );/* ...second half goes here */
		ROM_LOAD( "da02-2",    0x08000, 0x8000, 0x69d8dafe );
		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "da05",      0x00000, 0x8000, 0x834829d4 );/* Sprites Plane 1+2 */
		ROM_LOAD( "da04",      0x08000, 0x8000, 0xf083fd4c );/* Sprites Plane 3+4 */
		ROM_LOAD( "da03",      0x10000, 0x8000, 0xcf187f25 );/* Sprites Plane 5+6 */
	
		ROM_REGION( 0x0c000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "da06",      0x00000, 0x8000, 0x9c6a51b3 );/* Tiles*/
		ROM_LOAD( "da07",      0x08000, 0x4000, 0x56df4077 );/* Tiles*/
	
		ROM_REGION( 0x0040, REGION_PROMS );	ROM_LOAD( "82s123.7h", 0x00000, 0x0020, 0xb9933663 );/* palette? */
		ROM_LOAD( "82s123.9n", 0x00020, 0x0020, 0x06491e53 );/* palette? */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_battlan3 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for main CPU */
		/* first half of bl_04.rom will be copied at 0x4000-0x7fff */
		ROM_LOAD( "bl_05.rom", 0x8000, 0x8000, 0x001c4bbe );
		ROM_REGION( 0x10000, REGION_CPU2 );    /* 64K for slave CPU */
		ROM_LOAD( "bl_04.rom", 0x00000, 0x8000, 0x5681564c );/* ...second half goes here */
		ROM_LOAD( "da02-2",    0x08000, 0x8000, 0x69d8dafe );
		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "da05",      0x00000, 0x8000, 0x834829d4 );/* Sprites Plane 1+2 */
		ROM_LOAD( "da04",      0x08000, 0x8000, 0xf083fd4c );/* Sprites Plane 3+4 */
		ROM_LOAD( "da03",      0x10000, 0x8000, 0xcf187f25 );/* Sprites Plane 5+6 */
	
		ROM_REGION( 0x0c000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "da06",      0x00000, 0x8000, 0x9c6a51b3 );/* Tiles*/
		ROM_LOAD( "da07",      0x08000, 0x4000, 0x56df4077 );/* Tiles*/
	
		ROM_REGION( 0x0040, REGION_PROMS );	ROM_LOAD( "82s123.7h", 0x00000, 0x0020, 0xb9933663 );/* palette? */
		ROM_LOAD( "82s123.9n", 0x00020, 0x0020, 0x06491e53 );/* palette? */
	ROM_END(); }}; 
	
	
	
	static public static InitDriverPtr init_battlane = new InitDriverPtr() { public void handler() 
	{
		UBytePtr src,*dest;
		int A;
	
		/* no encryption, but one ROM is shared among two CPUs. We loaded it into the */
		/* second CPU address space, let's copy it to the first CPU's one */
		src = memory_region(REGION_CPU2);
		dest = memory_region(REGION_CPU1);
	
		for(A = 0;A < 0x4000;A++)
			dest[A + 0x4000] = src[A];
	} };
	
	
	
	public static GameDriver driver_battlane	   = new GameDriver("1986"	,"battlane"	,"battlane.java"	,rom_battlane,null	,machine_driver_battlane	,input_ports_battlane	,init_battlane	,ROT90	,	"Technos (Taito license)", "Battle Lane Vol. 5 (set 1)", GAME_WRONG_COLORS | GAME_NO_COCKTAIL )
	public static GameDriver driver_battlan2	   = new GameDriver("1986"	,"battlan2"	,"battlane.java"	,rom_battlan2,driver_battlane	,machine_driver_battlane	,input_ports_battlane	,init_battlane	,ROT90	,	"Technos (Taito license)", "Battle Lane Vol. 5 (set 2)", GAME_WRONG_COLORS | GAME_NO_COCKTAIL )
	public static GameDriver driver_battlan3	   = new GameDriver("1986"	,"battlan3"	,"battlane.java"	,rom_battlan3,driver_battlane	,machine_driver_battlane	,input_ports_battlane	,init_battlane	,ROT90	,	"Technos (Taito license)", "Battle Lane Vol. 5 (set 3)", GAME_WRONG_COLORS | GAME_NO_COCKTAIL )
}
