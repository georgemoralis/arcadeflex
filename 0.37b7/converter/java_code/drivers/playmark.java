/***************************************************************************

Big Twins
World Beach Volley

driver by Nicola Salmoria

The games run on different, but similar, hardware. The sprite system is the
same (almost - the tile size is different).

Even if the two games are from the same year, World Beach Volley is much more
advanced - more colourful, and stores setting in an EEPROM.

An interesting thing about this hardware is that the same gfx ROMs are used
to generate both 8x8 and 16x16 tiles for different tilemaps.


TODO:
- Sound is controlled by a pic16c57 whose ROM is missing.

Big Twins:
- The pixel bitmap might be larger than what I handle, or the vertical scroll
  register has an additional meaning. The usual scroll value is 0x7f0, the game
  is setting it to 0x5f0 while updating the bitmap, so this should either scroll
  the changing region out of view, or disable it. During gameplay, the image
  that scrolls down might have to be different since the scroll register is in
  the range 0x600-0x6ff.
  As it is handled now, it is certainly wrong because after game over the
  bitmap is left on screen.

World Beach Volley:
- sprite/tile priority issue during attract mode (plane should go behind palm)
- The histogram functions don't seem to work.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class playmark
{
	
	
	extern UBytePtr bigtwin_bgvideoram;
	extern size_t bigtwin_bgvideoram_size;
	extern UBytePtr wbeachvl_videoram1,*wbeachvl_videoram2,*wbeachvl_videoram3;
	
	
	
	
	public static WriteHandlerPtr coinctrl_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0xff000000) == 0)
		{
			coin_counter_w.handler(0,data & 0x0100);
			coin_counter_w.handler(1,data & 0x0200);
		}
	} };
	
	
	/***************************************************************************
	
	  EEPROM
	
	***************************************************************************/
	
	static EEPROM_interface eeprom_interface = new EEPROM_interface
	(
		6,			/* address bits */
		16,			/* data bits */
		"110",		/*  read command */
		"101",		/* write command */
		0,			/* erase command */
		"100000000",/* lock command */
		"100110000"	/* unlock command */
	);
	
	public static nvramPtr wbeachvl_nvram_handler  = new nvramPtr() { public void handler(Object file, int read_or_write) 
	{
		if (read_or_write != 0)
		{
			EEPROM_save(file);
		}
		else
		{
			EEPROM_init(&eeprom_interface);
	
			if (file != 0)
				EEPROM_load(file);
			else
			{
				UINT8 *init = malloc(128);
				if (init != 0)
				{
					memset(init,0,128);
					EEPROM_set_data(init,128);
					free(init);
				}
			}
		}
	} };
	
	public static ReadHandlerPtr wbeachvl_port0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int bit;
	
		bit = EEPROM_read_bit() << 7;
	
		return (input_port_0_r.handler(0) & 0x7f) | bit;
	} };
	
	public static WriteHandlerPtr wbeachvl_coin_eeprom_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
		{
			/* bits 0-3 are coin counters? (only 0 used?) */
			coin_counter_w.handler(0,data & 0x01);
			coin_counter_w.handler(1,data & 0x02);
			coin_counter_w.handler(2,data & 0x04);
			coin_counter_w.handler(3,data & 0x08);
	
			/* bits 5-7 control EEPROM */
			EEPROM_set_cs_line((data & 0x20) ? CLEAR_LINE : ASSERT_LINE);
			EEPROM_write_bit(data & 0x80);
			EEPROM_set_clock_line((data & 0x40) ? CLEAR_LINE : ASSERT_LINE);
		}
	} };
	
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x0fffff, MRA_ROM ),
		new MemoryReadAddress( 0x440000, 0x4403ff, MRA_BANK1 ),
		new MemoryReadAddress( 0x700010, 0x700011, input_port_0_r ),
		new MemoryReadAddress( 0x700012, 0x700013, input_port_1_r ),
		new MemoryReadAddress( 0x700014, 0x700015, input_port_2_r ),
		new MemoryReadAddress( 0x70001a, 0x70001b, input_port_3_r ),
		new MemoryReadAddress( 0x70001c, 0x70001d, input_port_4_r ),
		new MemoryReadAddress( 0xff0000, 0xffffff, MRA_BANK4 ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x0fffff, MWA_ROM ),
		new MemoryWriteAddress( 0x304000, 0x304001, MWA_NOP ),	/* watchdog? irq ack? */
		new MemoryWriteAddress( 0x440000, 0x4403ff, MWA_BANK1, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x500000, 0x5007ff, wbeachvl_fgvideoram_w, wbeachvl_videoram2 ),
	new MemoryWriteAddress( 0x500800, 0x501fff, MWA_NOP ),	/* unused RAM? */
		new MemoryWriteAddress( 0x502000, 0x503fff, wbeachvl_txvideoram_w, wbeachvl_videoram1 ),
	new MemoryWriteAddress( 0x504000, 0x50ffff, MWA_NOP ),	/* unused RAM? */
		new MemoryWriteAddress( 0x510000, 0x51000b, bigtwin_scroll_w ),
		new MemoryWriteAddress( 0x51000c, 0x51000d, MWA_NOP ),	/* always 3? */
		new MemoryWriteAddress( 0x600000, 0x67ffff, bigtwin_bgvideoram_w, bigtwin_bgvideoram, bigtwin_bgvideoram_size ),
		new MemoryWriteAddress( 0x700016, 0x700017, coinctrl_w ),
		new MemoryWriteAddress( 0x70001e, 0x70001f, MWA_NOP ),//sound_command_w },
		{ 0x780000, 0x7807ff, bigtwin_paletteram_w, &paletteram },
	//	{ 0xe00000, 0xe00001, ?? written on startup
		{ 0xff0000, 0xffffff, MWA_BANK4 },
		{ -1 }  /* end of table */
	};
	
	static MemoryReadAddress wbeachvl_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x440000, 0x440fff, MRA_BANK1 ),
		new MemoryReadAddress( 0x500000, 0x501fff, wbeachvl_bgvideoram_r ),
		new MemoryReadAddress( 0x504000, 0x505fff, wbeachvl_fgvideoram_r ),
		new MemoryReadAddress( 0x508000, 0x509fff, wbeachvl_txvideoram_r ),
		new MemoryReadAddress( 0xff0000, 0xffffff, MRA_BANK5 ),
		new MemoryReadAddress( 0x710010, 0x710011, wbeachvl_port0_r ),
		new MemoryReadAddress( 0x710012, 0x710013, input_port_1_r ),
		new MemoryReadAddress( 0x710014, 0x710015, input_port_2_r ),
		new MemoryReadAddress( 0x710018, 0x710019, input_port_3_r ),
		new MemoryReadAddress( 0x71001a, 0x71001b, input_port_4_r ),
	//	new MemoryReadAddress( 0x71001c, 0x71001d, ??
		{ -1 }  /* end of table */
	);
	
	static MemoryWriteAddress wbeachvl_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x440000, 0x440fff, MWA_BANK1, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x500000, 0x501fff, wbeachvl_bgvideoram_w, wbeachvl_videoram3 ),
		new MemoryWriteAddress( 0x504000, 0x505fff, wbeachvl_fgvideoram_w, wbeachvl_videoram2 ),
		new MemoryWriteAddress( 0x508000, 0x509fff, wbeachvl_txvideoram_w, wbeachvl_videoram1 ),
		new MemoryWriteAddress( 0x510000, 0x51000b, wbeachvl_scroll_w ),
		new MemoryWriteAddress( 0x51000c, 0x51000d, MWA_NOP ),	/* always 3? */
	//	new MemoryWriteAddress( 0x700000, 0x700001, ?? written on startup
		{ 0x710016, 0x710017, wbeachvl_coin_eeprom_w },
		{ 0x71001e, 0x71001f, MWA_NOP },//sound_command_w ),
		new MemoryWriteAddress( 0x780000, 0x780fff, paletteram_RRRRRGGGGGBBBBBx_word_w, paletteram ),
		new MemoryWriteAddress( 0xff0000, 0xffffff, MWA_BANK5 ),
	#if 0
		new MemoryWriteAddress( 0x700016, 0x700017, coinctrl_w ),
	#endif
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	
	static InputPortPtr input_ports_bigtwin = new InputPortPtr(){ public void handler() { 
	PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START(); 
		PORT_DIPNAME( 0x01, 0x00, "Language" );	PORT_DIPSETTING(    0x00, "English" );	PORT_DIPSETTING(    0x01, "Italian" );	PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, "Censor Pictures" );	PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 
		PORT_DIPNAME( 0x01, 0x01, "Coin Mode" );	PORT_DIPSETTING(    0x01, "Mode 1" );	PORT_DIPSETTING(    0x00, "Mode 2" );	/* TODO: support coin mode 2 */
		PORT_DIPNAME( 0x1e, 0x1e, "Coinage Mode 1" );	PORT_DIPSETTING(    0x14, DEF_STR( "6C_1C") );
		PORT_DIPSETTING(    0x16, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x1a, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "8C_3C") );
		PORT_DIPSETTING(    0x1c, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "5C_3C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x1e, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x12, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
	#if 0
		PORT_DIPNAME( 0x06, 0x06, "Coin A Mode 2" );	PORT_DIPSETTING(    0x00, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0x18, 0x18, "Coin B Mode 2" );	PORT_DIPSETTING(    0x18, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_6C") );
	#endif
		PORT_DIPNAME( 0x20, 0x20, "Minimum Credits to Start" );	PORT_DIPSETTING(    0x20, "1" );	PORT_DIPSETTING(    0x00, "2" );	PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_wbeachvl = new InputPortPtr(){ public void handler() { 
	PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN4 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_SPECIAL );/* ?? see code at 746a. sound status? */
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_SPECIAL );/* EEPROM data */
	
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER3 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START3 );
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER4 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER4 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START4 );INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,4),
		4,
		new int[] { RGN_FRAC(3,4), RGN_FRAC(2,4), RGN_FRAC(1,4), RGN_FRAC(0,4) },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		32*8
	);
	
	static GfxLayout tilelayout = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,4),
		4,
		new int[] { RGN_FRAC(3,4), RGN_FRAC(2,4), RGN_FRAC(1,4), RGN_FRAC(0,4) },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7,
				16*8+0, 16*8+1, 16*8+2, 16*8+3, 16*8+4, 16*8+5, 16*8+6, 16*8+7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		32*8
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		32,32,
		RGN_FRAC(1,4),
		4,
		new int[] { RGN_FRAC(3,4), RGN_FRAC(2,4), RGN_FRAC(1,4), RGN_FRAC(0,4) },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7,
				16*8+0, 16*8+1, 16*8+2, 16*8+3, 16*8+4, 16*8+5, 16*8+6, 16*8+7,
				32*8+0, 32*8+1, 32*8+2, 32*8+3, 32*8+4, 32*8+5, 32*8+6, 32*8+7,
				48*8+0, 48*8+1, 48*8+2, 48*8+3, 48*8+4, 48*8+5, 48*8+6, 48*8+7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8,
				64*8, 65*8, 66*8, 67*8, 68*8, 69*8, 70*8, 71*8,
				72*8, 73*8, 74*8, 75*8, 76*8, 77*8, 78*8, 79*8 },
		128*8
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX2, 0, spritelayout, 0x200, 16 ),	/* colors 0x200-0x2ff */
		new GfxDecodeInfo( REGION_GFX1, 0, tilelayout,   0x000,  8 ),	/* colors 0x000-0x07f */
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,   0x080,  8 ),	/* colors 0x080-0x0ff */
		/* background bitmap uses colors 0x100-0x1ff */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	static GfxLayout wcharlayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,6),
		6,
		new int[] { RGN_FRAC(5,6), RGN_FRAC(4,6), RGN_FRAC(3,6), RGN_FRAC(2,6), RGN_FRAC(1,6), RGN_FRAC(0,6) },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8
	);
	
	static GfxLayout wtilelayout = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,6),
		6,
		new int[] { RGN_FRAC(5,6), RGN_FRAC(4,6), RGN_FRAC(3,6), RGN_FRAC(2,6), RGN_FRAC(1,6), RGN_FRAC(0,6) },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7,
				16*8+0, 16*8+1, 16*8+2, 16*8+3, 16*8+4, 16*8+5, 16*8+6, 16*8+7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		32*8
	);
	
	/* tiles are 6 bpp, sprites only 5bpp */
	static GfxLayout wspritelayout = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,6),
		5,
		new int[] { RGN_FRAC(4,6), RGN_FRAC(3,6), RGN_FRAC(2,6), RGN_FRAC(1,6), RGN_FRAC(0,6) },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7,
				16*8+0, 16*8+1, 16*8+2, 16*8+3, 16*8+4, 16*8+5, 16*8+6, 16*8+7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		32*8
	);
	
	static GfxDecodeInfo wbeachvl_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, wspritelayout, 0x600, 16 ),	/* colors 0x600-0x7ff */
		new GfxDecodeInfo( REGION_GFX1, 0, wtilelayout,   0x000, 16 ),	/* colors 0x000-0x3ff */
		new GfxDecodeInfo( REGION_GFX1, 0, wcharlayout,   0x400,  8 ),	/* colors 0x400-0x5ff */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static OKIM6295interface okim6295_interface = new OKIM6295interface
	(
		1,					/* 1 chip */
		new int[] { 8000 },			/* 8000Hz frequency? */
		new int[] { REGION_SOUND1 },	/* memory region */
		new int[] { 100 }
	);
	
	
	
	static MachineDriver machine_driver_bigtwin = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				12000000,	/* 12 MHz? */
				readmem,writemem,null,null,
				m68_level2_irq,1
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* single CPU, no need for interleaving */
		null,
	
		/* video hardware */
		64*8, 32*8, new rectangle( 0*8, 40*8-1, 2*8, 32*8-1 ),
		gfxdecodeinfo,
		1024, 1024,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		bigtwin_vh_start,
		bigtwin_vh_stop,
		bigtwin_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_OKIM6295,
				okim6295_interface
			)
		}
	);
	
	static MachineDriver machine_driver_wbeachvl = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				12000000,	/* 12 MHz? */
				wbeachvl_readmem,wbeachvl_writemem,null,null,
				m68_level2_irq,1
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* single CPU, no need for interleaving */
		null,
	
		/* video hardware */
		64*8, 32*8, new rectangle( 0*8, 40*8-1, 2*8, 32*8-1 ),
		wbeachvl_gfxdecodeinfo,
		2048, 2048,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		wbeachvl_vh_start,
		0,
		wbeachvl_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_OKIM6295,
				okim6295_interface
			)
		},
	
		wbeachvl_nvram_handler
	);
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_bigtwin = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x100000, REGION_CPU1 );/* 68000 code */
		ROM_LOAD_EVEN( "bt_02.bin",    0x000000, 0x80000, 0xe6767f60 );	ROM_LOAD_ODD ( "bt_03.bin",    0x000000, 0x80000, 0x5aba6990 );
		ROM_REGION( 0x0800, REGION_CPU2 );/* sound (missing) */
		ROM_LOAD( "pic16c57",     0x0000, 0x0800, 0x00000000 );
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "bt_04.bin",    0x00000, 0x40000, 0x6f628fbc );	ROM_LOAD( "bt_05.bin",    0x40000, 0x40000, 0x6a9b1752 );	ROM_LOAD( "bt_06.bin",    0x80000, 0x40000, 0x411cf852 );	ROM_LOAD( "bt_07.bin",    0xc0000, 0x40000, 0x635c81fd );
		ROM_REGION( 0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "bt_08.bin",    0x00000, 0x20000, 0x2749644d );	ROM_LOAD( "bt_09.bin",    0x20000, 0x20000, 0x1d1897af );	ROM_LOAD( "bt_10.bin",    0x40000, 0x20000, 0x2a03432e );	ROM_LOAD( "bt_11.bin",    0x60000, 0x20000, 0x2c980c4c );
		ROM_REGION( 0x40000, REGION_SOUND1 );/* OKIM6295 samples */
		ROM_LOAD( "bt_01.bin",    0x00000, 0x40000, 0xff6671dc );ROM_END(); }}; 
	
	static RomLoadPtr rom_wbeachvl = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1 );/* 68000 code */
		ROM_LOAD_EVEN( "wbv_02.bin",   0x000000, 0x40000, 0xc7cca29e );	ROM_LOAD_ODD ( "wbv_03.bin",   0x000000, 0x40000, 0xdb4e69d5 );
		ROM_REGION( 0x0800, REGION_CPU2 );/* sound (missing) */
		ROM_LOAD( "pic16c57",     0x0000, 0x0800, 0x00000000 );
		ROM_REGION( 0x600000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "wbv_10.bin",   0x000000, 0x80000, 0x50680f0b );	ROM_LOAD( "wbv_04.bin",   0x080000, 0x80000, 0xdf9cbff1 );	ROM_LOAD( "wbv_11.bin",   0x100000, 0x80000, 0xe59ad0d1 );	ROM_LOAD( "wbv_05.bin",   0x180000, 0x80000, 0x51245c3c );	ROM_LOAD( "wbv_12.bin",   0x200000, 0x80000, 0x36b87d0b );	ROM_LOAD( "wbv_06.bin",   0x280000, 0x80000, 0x9eb808ef );	ROM_LOAD( "wbv_13.bin",   0x300000, 0x80000, 0x7021107b );	ROM_LOAD( "wbv_07.bin",   0x380000, 0x80000, 0x4fff9fe8 );	ROM_LOAD( "wbv_14.bin",   0x400000, 0x80000, 0x0595e675 );	ROM_LOAD( "wbv_08.bin",   0x480000, 0x80000, 0x07e4b416 );	ROM_LOAD( "wbv_15.bin",   0x500000, 0x80000, 0x4e1a82d2 );	ROM_LOAD( "wbv_09.bin",   0x580000, 0x20000, 0x894ce354 );	/* 5a0000-5fffff is empty */
	
		ROM_REGION( 0x100000, REGION_SOUND1 );/* OKIM6295 samples */
		ROM_LOAD( "wbv_01.bin",   0x00000, 0x100000, 0xac33f25f );ROM_END(); }}; 
	
	
	
	public static GameDriver driver_bigtwin	   = new GameDriver("1995"	,"bigtwin"	,"playmark.java"	,rom_bigtwin,null	,machine_driver_bigtwin	,input_ports_bigtwin	,null	,ROT0_16BIT	,	"Playmark", "Big Twin", GAME_NO_COCKTAIL | GAME_NO_SOUND )
	public static GameDriver driver_wbeachvl	   = new GameDriver("1995"	,"wbeachvl"	,"playmark.java"	,rom_wbeachvl,null	,machine_driver_wbeachvl	,input_ports_wbeachvl	,null	,ROT0_16BIT	,	"Playmark", "World Beach Volley", GAME_NO_COCKTAIL | GAME_NO_SOUND )
}
