/****************************************************************************

Royal Mahjong (V1.01  03/15/1982)
driver by Zsolt Vasvari

Location     Device      File ID
--------------------------------
O1            2732         ROM1
O1/2          2732         ROM2
O2/3          2732         ROM3
O4/5          2732         ROM4
O4            2732         ROM5
O4/5          2732         ROM6
K6       TBP18S030    F-ROM.BPR

Notes:    Falcon PCB No. FRM-03

****************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class royalmah
{
	
	
	public static VhConvertColorPromPtr royalmah_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2;
	
	
			/* red component */
			bit0 = (*color_prom >> 0) & 0x01;
			bit1 = (*color_prom >> 1) & 0x01;
			bit2 = (*color_prom >> 2) & 0x01;
			*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* green component */
			bit0 = (*color_prom >> 3) & 0x01;
			bit1 = (*color_prom >> 4) & 0x01;
			bit2 = (*color_prom >> 5) & 0x01;
			*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* blue component */
			bit0 = 0;
			bit1 = (*color_prom >> 6) & 0x01;
			bit2 = (*color_prom >> 7) & 0x01;
			*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	
			color_prom++;
		}
	} };
	
	
	public static WriteHandlerPtr royalmah_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int i;
		UINT8 x, y;
		UINT8 col1, col2;
	
	
		videoram.write(offset,data);

		col1 = videoram.read(offset & 0x3fff);
		col2 = videoram.read(offset | 0x4000);
	
		y = (offset >> 6);
		x = (offset & 0x3f) << 2;
	
		for (i = 0; i < 4; i++)
		{
			int col = ((col1 & 0x01) >> 0) | ((col1 & 0x10) >> 3) | ((col2 & 0x01) << 2) | ((col2 & 0x10) >> 1);
	
			plot_pixel(Machine.scrbitmap, x+i, y, Machine.pens[col]);
	
			col1 >>= 1;
			col2 >>= 1;
		}
	} };
	
	
	public static VhUpdatePtr royalmah_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		if (full_refresh != 0)
		{
			int offs;
	
			/* redraw bitmap */
	
			for (offs = 0; offs < videoram_size[0]; offs++)
			{
				royalmah_videoram_w(offs, videoram.read(offs));
			}
		}
	} };
	
	
	public static WriteHandlerPtr royalmah_rom_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* using this handler will avoid all the entries in the error log that are the result of
		   the RLD and RRD instructions this games uses to print text on the screen */
	} };
	
	
	static int royalmah_input_port_select;
	
	public static WriteHandlerPtr royalmah_input_port_select_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		royalmah_input_port_select = data;
	} };
	
	public static ReadHandlerPtr royalmah_player_1_port_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int ret = (input_port_0_r.handler(offset) & 0xc0) | 0x3f;
	
		if ((royalmah_input_port_select & 0x01) == 0)  ret &= input_port_0_r.handler(offset);
		if ((royalmah_input_port_select & 0x02) == 0)  ret &= input_port_1_r.handler(offset);
		if ((royalmah_input_port_select & 0x04) == 0)  ret &= input_port_2_r.handler(offset);
		if ((royalmah_input_port_select & 0x08) == 0)  ret &= input_port_3_r.handler(offset);
		if ((royalmah_input_port_select & 0x10) == 0)  ret &= input_port_4_r.handler(offset);
	
		return ret;
	} };
	
	public static ReadHandlerPtr royalmah_player_2_port_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int ret = (input_port_5_r.handler(offset) & 0xc0) | 0x3f;
	
		if ((royalmah_input_port_select & 0x01) == 0)  ret &= input_port_5_r.handler(offset);
		if ((royalmah_input_port_select & 0x02) == 0)  ret &= input_port_6_r.handler(offset);
		if ((royalmah_input_port_select & 0x04) == 0)  ret &= input_port_7_r.handler(offset);
		if ((royalmah_input_port_select & 0x08) == 0)  ret &= input_port_8_r.handler(offset);
		if ((royalmah_input_port_select & 0x10) == 0)  ret &= input_port_9_r.handler(offset);
	
		return ret;
	} };
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x5fff, MRA_ROM ),
		new MemoryReadAddress( 0x7000, 0x77ff, MRA_RAM ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_RAM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x5fff, royalmah_rom_w ),
		new MemoryWriteAddress( 0x7000, 0x77ff, MWA_RAM ),
		new MemoryWriteAddress( 0x8000, 0xffff, royalmah_videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static IOReadPort readport[] =
	{
		new IOReadPort( 0x01, 0x01, AY8910_read_port_0_r ),
		new IOReadPort( 0x10, 0x10, input_port_11_r ),
		new IOReadPort( 0x11, 0x11, input_port_10_r ),
		new IOReadPort( -1 )	/* end of table */
	};
	
	static IOWritePort writeport[] =
	{
		new IOWritePort( 0x02, 0x02, AY8910_write_port_0_w ),
		new IOWritePort( 0x03, 0x03, AY8910_control_port_0_w ),
		new IOWritePort( 0x11, 0x11, royalmah_input_port_select_w ),
		new IOWritePort( -1 )	/* end of table */
	};
	
	
	static InputPortPtr input_ports_royalmah = new InputPortPtr(){ public void handler() { 
	PORT_START(); 	/* P1 IN0 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 A", KEYCODE_A, IP_JOY_NONE );	PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 E", KEYCODE_E, IP_JOY_NONE );	PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 I", KEYCODE_I, IP_JOY_NONE );	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 M", KEYCODE_M, IP_JOY_NONE );	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Kan", KEYCODE_O, IP_JOY_NONE );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START1 );	PORT_BITX(0x40, IP_ACTIVE_LOW, 0, "P1 Credit Clear", KEYCODE_5, IP_JOY_NONE );	PORT_BITX(0x80, IP_ACTIVE_LOW, 0, "P2 Credit Clear", KEYCODE_6, IP_JOY_NONE );
		PORT_START(); 	/* P1 IN1 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 B", KEYCODE_B, IP_JOY_NONE );	PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 F", KEYCODE_F, IP_JOY_NONE );	PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 J", KEYCODE_J, IP_JOY_NONE );	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 N", KEYCODE_N, IP_JOY_NONE );	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Reach", KEYCODE_R, IP_JOY_NONE );	PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P1 Bet", KEYCODE_7, IP_JOY_NONE );	PORT_BIT( 0xc0, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_START(); 	/* P1 IN2 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 C", KEYCODE_C, IP_JOY_NONE );	PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 G", KEYCODE_G, IP_JOY_NONE );	PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 K", KEYCODE_K, IP_JOY_NONE );	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 Chii", KEYCODE_S, IP_JOY_NONE );	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Ron", KEYCODE_T, IP_JOY_NONE );	PORT_BIT( 0xe0, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_START(); 	/* P1 IN3 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 D", KEYCODE_D, IP_JOY_NONE );	PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 H", KEYCODE_H, IP_JOY_NONE );	PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 L", KEYCODE_L, IP_JOY_NONE );	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 Pon", KEYCODE_U, IP_JOY_NONE );	PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_START(); 	/* P1 IN4 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P1 Last Chance", KEYCODE_V, IP_JOY_NONE );	PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P1 Take Score", KEYCODE_W, IP_JOY_NONE );	PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P1 Double Up", KEYCODE_X, IP_JOY_NONE );	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P1 Flip Flop", KEYCODE_Y, IP_JOY_NONE );	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P1 Big", KEYCODE_Z, IP_JOY_NONE );	PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P1 Small", KEYCODE_Q, IP_JOY_NONE );	PORT_BIT( 0xc0, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_START(); 	/* P2 IN0 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P2 A", IP_KEY_DEFAULT, IP_JOY_NONE );	PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P2 E", IP_KEY_DEFAULT, IP_JOY_NONE );	PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P2 I", IP_KEY_DEFAULT, IP_JOY_NONE );	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P2 M", IP_KEY_DEFAULT, IP_JOY_NONE );	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P2 Kan", IP_KEY_DEFAULT, IP_JOY_NONE );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_START(); 	/* P2 IN1 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P2 B", IP_KEY_DEFAULT, IP_JOY_NONE );	PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P2 F", IP_KEY_DEFAULT, IP_JOY_NONE );	PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P2 J", IP_KEY_DEFAULT, IP_JOY_NONE );	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P2 N", IP_KEY_DEFAULT, IP_JOY_NONE );	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P2 Reach", IP_KEY_DEFAULT, IP_JOY_NONE );	PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P2 Bet", KEYCODE_8, IP_JOY_NONE );	PORT_BIT( 0xc0, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_START(); 	/* P2 IN2 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P2 C", IP_KEY_DEFAULT, IP_JOY_NONE );	PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P2 G", IP_KEY_DEFAULT, IP_JOY_NONE );	PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P2 K", IP_KEY_DEFAULT, IP_JOY_NONE );	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P2 Chii", IP_KEY_DEFAULT, IP_JOY_NONE );	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P2 Ron", IP_KEY_DEFAULT, IP_JOY_NONE );	PORT_BIT( 0xe0, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_START(); 	/* P2 IN3 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P2 D", IP_KEY_DEFAULT, IP_JOY_NONE );	PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P2 H", IP_KEY_DEFAULT, IP_JOY_NONE );	PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P2 L", IP_KEY_DEFAULT, IP_JOY_NONE );	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P2 Pon", IP_KEY_DEFAULT, IP_JOY_NONE );	PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_START(); 	/* P2 IN4 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "P2 Last Chance", IP_KEY_DEFAULT, IP_JOY_NONE );	PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "P2 Take Score", IP_KEY_DEFAULT, IP_JOY_NONE );	PORT_BITX(0x04, IP_ACTIVE_LOW, 0, "P2 Double Up", IP_KEY_DEFAULT, IP_JOY_NONE );	PORT_BITX(0x08, IP_ACTIVE_LOW, 0, "P2 Flip Flop", IP_KEY_DEFAULT, IP_JOY_NONE );	PORT_BITX(0x10, IP_ACTIVE_LOW, 0, "P2 Big", IP_KEY_DEFAULT, IP_JOY_NONE );	PORT_BITX(0x20, IP_ACTIVE_LOW, 0, "P2 Small", IP_KEY_DEFAULT, IP_JOY_NONE );	PORT_BIT( 0xc0, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT );						/* 'Note' = 10 Credits ('Note' probably means 'Paper Money') */
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT );/* Memory Reset */
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP ); /* Analizer (Statistics) */
		PORT_SERVICE( 0x08, IP_ACTIVE_HIGH );	PORT_BIT( 0xf0, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_START(); 	/* DSW1 */
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
	INPUT_PORTS_END(); }}; 
	
	
	static AY8910interface ay8910_interface = new AY8910interface
	(
		1,	/* 1 chip */
		1500000,	/* 1.5 MHz ? */
		new int[] { 50 },
		new ReadHandlerPtr[] { royalmah_player_1_port_r },
		new ReadHandlerPtr[] { royalmah_player_2_port_r },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 }
	);
	
	
	static MachineDriver machine_driver_royalmah = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				3000000,        /* 3.00 MHz ? */
				readmem,writemem,readport,writeport,
				interrupt,1
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
		1,
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0, 255, 0, 255 ),
		null,
		16,null,
		royalmah_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		null,
		null,
		royalmah_vh_screenrefresh,
	
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
	
	static RomLoadPtr rom_royalmah = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for main CPU */
		ROM_LOAD( "rom1",   	0x0000, 0x1000, 0x69b37a62 );	ROM_LOAD( "rom2",   	0x1000, 0x1000, 0x0c8351b6 );	ROM_LOAD( "rom3",   	0x2000, 0x1000, 0xb7736596 );	ROM_LOAD( "rom4",   	0x3000, 0x1000, 0xe3c7c15c );	ROM_LOAD( "rom5",   	0x4000, 0x1000, 0x16c09c73 );	ROM_LOAD( "rom6",   	0x5000, 0x1000, 0x92687327 );
		ROM_REGION( 0x0020, REGION_PROMS );	ROM_LOAD( "f-rom.bpr",  0x0000, 0x0020, 0xd3007282 );ROM_END(); }}; 
	
	
	public static GameDriver driver_royalmah	   = new GameDriver("1982"	,"royalmah"	,"royalmah.java"	,rom_royalmah,null	,machine_driver_royalmah	,input_ports_royalmah	,null	,ROT180	,	"Falcon", "Royal Mahjong" )
}
