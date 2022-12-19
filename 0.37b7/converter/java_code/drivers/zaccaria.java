/***************************************************************************

Jack Rabbit memory map (preliminary)

driver by Nicola Salmoria

0000-5fff ROM
6000-63ff Video RAM
6400-67ff Color RAM
7000-77ff RAM
8000-dfff ROM

read:
6400-6406 protection
6c00-6c06 protection
6c00      COIN
6e00      dip switches (three ports multiplexed)
7800      IN0
7801      IN1
7802      IN2
7c00      watchdog reset?

write:
6800-683f even addresses = column [row] scroll; odd addresses = column [row] color?
6840-685f sprites
6881-68bc sprites
6c02      ?
6c07      NMI enable
7802      e0/d0/b0 select port which will appear at 6e00

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class zaccaria
{
	
	
	extern UBytePtr zaccaria_attributesram;
	
	
	
	/* all connections unknown */
	static struct pia6821_interface pia_0_intf =
	{
		/*inputs : A/B,CA/B1,CA/B2 */ 0, 0, 0, 0, 0, 0,
		/*outputs: A/B,CA/B2       */ 0, 0, 0, 0,
		/*irqs   : A/B             */ 0, 0
	};
	
	
	/* all connections unknown */
	static struct pia6821_interface pia_1_intf =
	{
		/*inputs : A/B,CA/B1,CA/B2 */ 0, 0, 0, 0, 0, 0,
		/*outputs: A/B,CA/B2       */ 0, 0, 0, 0,
		/*irqs   : A/B             */ 0, 0
	};
	
	
	static public static InitMachinePtr zaccaria_init_machine = new InitMachinePtr() { public void handler() 
	{
		pia_unconfig();
		pia_config(0, PIA_STANDARD_ORDERING | PIA_8BIT, &pia_0_intf);
		pia_config(1, PIA_STANDARD_ORDERING | PIA_8BIT, &pia_1_intf);
		pia_reset();
	} };
	
	
	
	struct GameDriver monymony_driver;
	
	public static ReadHandlerPtr zaccaria_prot1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch (offset)
		{
			case 0:
				return 0x50;    /* Money Money */
	
			case 4:
				return 0x40;    /* Jack Rabbit */
	
			case 6:
				if (Machine.gamedrv == &monymony_driver)
					return 0x70;    /* Money Money */
				return 0xa0;    /* Jack Rabbit */
	
			default:
				return 0;
		}
	} };
	
	public static ReadHandlerPtr zaccaria_prot2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch (offset)
		{
			case 0:
				return input_port_6_r.handler(0);   /* bits 4 and 5 must be 0 in Jack Rabbit */
	
			case 2:
				return 0x10;    /* Jack Rabbit */
	
			case 4:
				return 0x80;    /* Money Money */
	
			case 6:
				return 0x00;    /* Money Money */
	
			default:
				return 0;
		}
	} };
	
	
	static int dsw;
	
	public static WriteHandlerPtr zaccaria_dsw_sel_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		switch (data)
		{
			case 0xe0:
				dsw = 0;
				break;
	
			case 0xd0:
				dsw = 1;
				break;
	
			case 0xb0:
				dsw = 2;
				break;
	
			default:
				break;
	logerror("PC %04x: portsel = %02x\n",cpu_get_pc(),data);
		}
	} };
	
	public static ReadHandlerPtr zaccaria_dsw_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return readinputport(dsw);
	} };
	
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x5fff, MRA_ROM ),
		new MemoryReadAddress( 0x6000, 0x63ff, MRA_RAM ),
		new MemoryReadAddress( 0x6400, 0x6407, zaccaria_prot1_r ),
		new MemoryReadAddress( 0x6c00, 0x6c07, zaccaria_prot2_r ),
		new MemoryReadAddress( 0x6e00, 0x6e00, zaccaria_dsw_r ),
		new MemoryReadAddress( 0x7000, 0x77ff, MRA_RAM ),
		new MemoryReadAddress( 0x7800, 0x7800, input_port_3_r ),
		new MemoryReadAddress( 0x7801, 0x7801, input_port_4_r ),
		new MemoryReadAddress( 0x7802, 0x7802, input_port_5_r ),
		new MemoryReadAddress( 0x7c00, 0x7c00, watchdog_reset_r ),   /* not sure */
		new MemoryReadAddress( 0x8000, 0xdfff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x5fff, MWA_ROM ),
		new MemoryWriteAddress( 0x6000, 0x63ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x6400, 0x67ff, colorram_w, colorram ),
		new MemoryWriteAddress( 0x6800, 0x683f, zaccaria_attributes_w, zaccaria_attributesram ),
		new MemoryWriteAddress( 0x6840, 0x685f, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x6881, 0x68bc, MWA_RAM, spriteram_2, spriteram_2_size ),
	new MemoryWriteAddress( 0x6c02, 0x6c02, MWA_NOP ),    /* ??? */
		new MemoryWriteAddress( 0x6c07, 0x6c07, interrupt_enable_w ),
		new MemoryWriteAddress( 0x7000, 0x77ff, MWA_RAM ),
		new MemoryWriteAddress( 0x7802, 0x7802, zaccaria_dsw_sel_w ),
		new MemoryWriteAddress( 0x8000, 0xdfff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress sound_readmem1[] =
	{
		new MemoryReadAddress( 0x0000, 0x007f, MRA_RAM ),
		new MemoryReadAddress( 0x500c, 0x500f, pia_0_r ),
		new MemoryReadAddress( 0xf000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress sound_writemem1[] =
	{
		new MemoryWriteAddress( 0x0000, 0x007f, MWA_RAM ),
		new MemoryWriteAddress( 0x500c, 0x500f, pia_0_w ),
		new MemoryWriteAddress( 0xf000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress sound_readmem2[] =
	{
		new MemoryReadAddress( 0x0000, 0x007f, MRA_RAM ),
		new MemoryReadAddress( 0x0090, 0x0093, pia_1_r ),
		new MemoryReadAddress( 0xe000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress sound_writemem2[] =
	{
		new MemoryWriteAddress( 0x0000, 0x007f, MWA_RAM ),
		new MemoryWriteAddress( 0x0090, 0x0093, pia_1_w ),
		new MemoryWriteAddress( 0xe000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	
	static InputPortPtr input_ports_monymony = new InputPortPtr(){ public void handler() { 
	PORT_START(); 
		PORT_DIPNAME( 0x03, 0x01, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "2" );	PORT_DIPSETTING(    0x01, "3" );	PORT_DIPSETTING(    0x02, "4" );	PORT_DIPSETTING(    0x03, "5" );	PORT_BITX(    0x04, 0x00, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Infinite Lives", IP_KEY_NONE, IP_JOY_NONE );	PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x00, "Easy" );	PORT_DIPSETTING(    0x08, "Hard" );	PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x20, 0x00, "Freeze" );	PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, "Cross Hatch Pattern" );	PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );  /* random high scores? */
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 
		PORT_DIPNAME( 0x03, 0x01, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x01, "200000" );	PORT_DIPSETTING(    0x02, "300000" );	PORT_DIPSETTING(    0x03, "400000" );	PORT_DIPSETTING(    0x00, "None" );	PORT_DIPNAME( 0x04, 0x04, "Table Title" );	PORT_DIPSETTING(    0x04, "High Scores" );	PORT_DIPSETTING(    0x00, "Todays High Scores" );	PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x80, IP_ACTIVE_HIGH );
		PORT_START(); 
		PORT_DIPNAME( 0x03, 0x02, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x03, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x8c, 0x84, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x8c, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x88, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x84, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x70, 0x50, "Coin C" );	PORT_DIPSETTING(    0x70, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_7C") );
	
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_TILT );  /* I think */
		PORT_BIT( 0xf8, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );	PORT_BIT( 0x30, 0x00, IPT_UNKNOWN );/* protection check in Jack Rabbit - must be 0 */
		PORT_BIT( 0xc8, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_jackrabt = new InputPortPtr(){ public void handler() { 
	PORT_START(); 
		PORT_DIPNAME( 0x03, 0x01, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "2" );	PORT_DIPSETTING(    0x01, "3" );	PORT_DIPSETTING(    0x02, "4" );	PORT_DIPSETTING(    0x03, "5" );	PORT_BITX(    0x04, 0x00, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Infinite Lives", IP_KEY_NONE, IP_JOY_NONE );	PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x20, 0x00, "Freeze" );	PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, "Cross Hatch Pattern" );	PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, "Table Title" );	PORT_DIPSETTING(    0x04, "High Scores" );	PORT_DIPSETTING(    0x00, "Todays High Scores" );	PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x80, IP_ACTIVE_HIGH );
		PORT_START(); 
		PORT_DIPNAME( 0x03, 0x02, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x03, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_3C") );
		PORT_DIPNAME( 0x8c, 0x84, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x8c, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x88, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x84, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x70, 0x50, "Coin C" );	PORT_DIPSETTING(    0x70, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_7C") );
	
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_TILT );  /* I think */
		PORT_BIT( 0xf8, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );	PORT_BIT( 0x30, 0x00, IPT_UNKNOWN );/* protection check in Jack Rabbit - must be 0 */
		PORT_BIT( 0xc8, IP_ACTIVE_LOW, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,    /* 8*8 characters */
		1024,   /* 1024 characters */
		3,  /* 3 bits per pixel */
		new int[] { 2*1024*8*8, 1*1024*8*8, 0*1024*8*8},
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8 /* every char takes 8 consecutive bytes */
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,  /* 16*16 sprites */
		256,    /* 256 sprites */
		3,  /* 3 bits per pixel */
		new int[] { 2*1024*8*8, 1*1024*8*8, 0*1024*8*8},
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7,
				8*8+0, 8*8+1, 8*8+2, 8*8+3, 8*8+4, 8*8+5, 8*8+6, 8*8+7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				16*8, 17*8, 18*8, 19*8, 20*8, 21*8, 22*8, 23*8 },
		32*8    /* every sprite takes 32 consecutive bytes */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,      0, 32 ),
		new GfxDecodeInfo( REGION_GFX1, 0, spritelayout, 32*8, 32 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static MachineDriver machine_driver_zaccaria = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				3000000,    /* 3 MHz ????? */
				readmem,writemem,null,null,
				nmi_interrupt,1
			),
			new MachineCPU(
				CPU_M6802 | CPU_AUDIO_CPU,
				6000000/4,  /* ????? */
				sound_readmem1,sound_writemem1,null,null,
				ignore_interrupt,1
			),
			new MachineCPU(
				CPU_M6802 | CPU_AUDIO_CPU,
				6000000/4,  /* ????? */
				sound_readmem2,sound_writemem2,null,null,
				ignore_interrupt,1
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,   /* frames per second, vblank duration */
		1,  /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		zaccaria_init_machine,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		512, 32*8+32*8,
		zaccaria_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		generic_vh_start,
		generic_vh_stop,
		zaccaria_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_monymony = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "1a",           0x0000, 0x1000, 0x13c227ca );	ROM_CONTINUE(             0x8000, 0x1000 );	ROM_LOAD( "1b",           0x1000, 0x1000, 0x87372545 );	ROM_CONTINUE(             0x9000, 0x1000 );	ROM_LOAD( "1c",           0x2000, 0x1000, 0x6aea9c01 );	ROM_CONTINUE(             0xa000, 0x1000 );	ROM_LOAD( "1d",           0x3000, 0x1000, 0x5fdec451 );	ROM_CONTINUE(             0xb000, 0x1000 );	ROM_LOAD( "2a",           0x4000, 0x1000, 0xaf830e3c );	ROM_CONTINUE(             0xc000, 0x1000 );	ROM_LOAD( "2c",           0x5000, 0x1000, 0x31da62b1 );	ROM_CONTINUE(             0xd000, 0x1000 );
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for first 6802 */
		ROM_LOAD( "1i",           0x7000, 0x1000, 0x94e3858b ); /* ?? */
		ROM_CONTINUE(             0xf000, 0x1000 );
		ROM_REGION( 0x10000, REGION_CPU3 );/* 64k for second 6802 */
		ROM_LOAD( "1h",           0x6000, 0x1000, 0xaad76193 ); /* ?? */
		ROM_CONTINUE(             0xe000, 0x1000 );	ROM_LOAD( "1g",           0x7000, 0x1000, 0x1e8ffe3e ); /* ?? */
		ROM_CONTINUE(             0xf000, 0x1000 );
		ROM_REGION( 0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "2d",           0x0000, 0x2000, 0x82ab4d1a );	ROM_LOAD( "1f",           0x2000, 0x2000, 0x40d4e4d1 );	ROM_LOAD( "1e",           0x4000, 0x2000, 0x36980455 );
		ROM_REGION( 0x0400, REGION_PROMS );	ROM_LOAD( "monymony.9g",  0x0000, 0x0200, 0xfc9a0f21 );	ROM_LOAD( "monymony.9f",  0x0200, 0x0200, 0x93106704 );
		ROM_REGION( 0x2000, REGION_SOUND1 );/* TMS5200 sample data??? */
		ROM_LOAD( "2g",           0x0000, 0x2000, 0x78b01b98 );ROM_END(); }}; 
	
	static RomLoadPtr rom_jackrabt = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "cpu-01.1a",    0x0000, 0x1000, 0x499efe97 );	ROM_CONTINUE(             0x8000, 0x1000 );	ROM_LOAD( "cpu-01.2l",    0x1000, 0x1000, 0x4772e557 );	ROM_LOAD( "cpu-01.3l",    0x2000, 0x1000, 0x1e844228 );	ROM_LOAD( "cpu-01.4l",    0x3000, 0x1000, 0xebffcc38 );	ROM_LOAD( "cpu-01.5l",    0x4000, 0x1000, 0x275e0ed6 );	ROM_LOAD( "cpu-01.6l",    0x5000, 0x1000, 0x8a20977a );	ROM_LOAD( "cpu-01.2h",    0x9000, 0x1000, 0x21f2be2a );	ROM_LOAD( "cpu-01.3h",    0xa000, 0x1000, 0x59077027 );	ROM_LOAD( "cpu-01.4h",    0xb000, 0x1000, 0x0b9db007 );	ROM_LOAD( "cpu-01.5h",    0xc000, 0x1000, 0x785e1a01 );	ROM_LOAD( "cpu-01.6h",    0xd000, 0x1000, 0xdd5979cf );
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for first 6802 */
		ROM_LOAD( "9snd.1i",      0x7000, 0x1000, 0x3dab977f ); /* ?? */
		ROM_CONTINUE(             0xf000, 0x1000 );
		ROM_REGION( 0x10000, REGION_CPU3 );/* 64k for second 6802 */
		ROM_LOAD( "8snd.1h",      0x6000, 0x1000, 0xf4507111 ); /* ?? */
		ROM_CONTINUE(             0xe000, 0x1000 );	ROM_LOAD( "7snd.1g",      0x7000, 0x1000, 0xc722eff8 ); /* ?? */
		ROM_CONTINUE(             0xf000, 0x1000 );
		ROM_REGION( 0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "1bg.2d",       0x0000, 0x2000, 0x9f880ef5 );	ROM_LOAD( "2bg.1f",       0x2000, 0x2000, 0xafc04cd7 );	ROM_LOAD( "3bg.1e",       0x4000, 0x2000, 0x14f23cdd );
		ROM_REGION( 0x0400, REGION_PROMS );	ROM_LOAD( "jr-ic9g",      0x0000, 0x0200, 0x85577107 );	ROM_LOAD( "jr-ic9f",      0x0200, 0x0200, 0x085914d1 );
		ROM_REGION( 0x2000, REGION_SOUND1 );/* TMS5200 sample data??? */
		ROM_LOAD( "13snd.2g",     0x0000, 0x2000, 0xfc05654e );ROM_END(); }}; 
	
	static RomLoadPtr rom_jackrab2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "1cpu2.1a",     0x0000, 0x1000, 0xf9374113 );	ROM_CONTINUE(             0x8000, 0x1000 );	ROM_LOAD( "2cpu2.1b",     0x1000, 0x1000, 0x0a0eea4a );	ROM_CONTINUE(             0x9000, 0x1000 );	ROM_LOAD( "3cpu2.1c",     0x2000, 0x1000, 0x291f5772 );	ROM_CONTINUE(             0xa000, 0x1000 );	ROM_LOAD( "4cpu2.1d",     0x3000, 0x1000, 0x10972cfb );	ROM_CONTINUE(             0xb000, 0x1000 );	ROM_LOAD( "5cpu2.2a",     0x4000, 0x1000, 0xaa95d06d );	ROM_CONTINUE(             0xc000, 0x1000 );	ROM_LOAD( "6cpu2.2c",     0x5000, 0x1000, 0x404496eb );	ROM_CONTINUE(             0xd000, 0x1000 );
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for first 6802 */
		ROM_LOAD( "9snd.1i",      0x7000, 0x1000, 0x3dab977f ); /* ?? */
		ROM_CONTINUE(             0xf000, 0x1000 );
		ROM_REGION( 0x10000, REGION_CPU3 );/* 64k for second 6802 */
		ROM_LOAD( "8snd.1h",      0x6000, 0x1000, 0xf4507111 ); /* ?? */
		ROM_CONTINUE(             0xe000, 0x1000 );	ROM_LOAD( "7snd.1g",      0x7000, 0x1000, 0xc722eff8 ); /* ?? */
		ROM_CONTINUE(             0xf000, 0x1000 );
		ROM_REGION( 0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "1bg.2d",       0x0000, 0x2000, 0x9f880ef5 );	ROM_LOAD( "2bg.1f",       0x2000, 0x2000, 0xafc04cd7 );	ROM_LOAD( "3bg.1e",       0x4000, 0x2000, 0x14f23cdd );
		ROM_REGION( 0x0400, REGION_PROMS );	ROM_LOAD( "jr-ic9g",      0x0000, 0x0200, 0x85577107 );	ROM_LOAD( "jr-ic9f",      0x0200, 0x0200, 0x085914d1 );
		ROM_REGION( 0x2000, REGION_SOUND1 );/* TMS5200 sample data??? */
		ROM_LOAD( "13snd.2g",     0x0000, 0x2000, 0xfc05654e );ROM_END(); }}; 
	
	static RomLoadPtr rom_jackrabs = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "1cpu.1a",      0x0000, 0x1000, 0x6698dc65 );	ROM_CONTINUE(             0x8000, 0x1000 );	ROM_LOAD( "2cpu.1b",      0x1000, 0x1000, 0x42b32929 );	ROM_CONTINUE(             0x9000, 0x1000 );	ROM_LOAD( "3cpu.1c",      0x2000, 0x1000, 0x89b50c9a );	ROM_CONTINUE(             0xa000, 0x1000 );	ROM_LOAD( "4cpu.1d",      0x3000, 0x1000, 0xd5520665 );	ROM_CONTINUE(             0xb000, 0x1000 );	ROM_LOAD( "5cpu.2a",      0x4000, 0x1000, 0x0f9a093c );	ROM_CONTINUE(             0xc000, 0x1000 );	ROM_LOAD( "6cpu.2c",      0x5000, 0x1000, 0xf53d6356 );	ROM_CONTINUE(             0xd000, 0x1000 );
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for first 6802 */
		ROM_LOAD( "9snd.1i",      0x7000, 0x1000, 0x3dab977f ); /* ?? */
		ROM_CONTINUE(             0xf000, 0x1000 );
		ROM_REGION( 0x10000, REGION_CPU3 );/* 64k for second 6802 */
		ROM_LOAD( "8snd.1h",      0x6000, 0x1000, 0xf4507111 ); /* ?? */
		ROM_CONTINUE(             0xe000, 0x1000 );	ROM_LOAD( "7snd.1g",      0x7000, 0x1000, 0xc722eff8 ); /* ?? */
		ROM_CONTINUE(             0xf000, 0x1000 );
		ROM_REGION( 0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "1bg.2d",       0x0000, 0x2000, 0x9f880ef5 );	ROM_LOAD( "2bg.1f",       0x2000, 0x2000, 0xafc04cd7 );	ROM_LOAD( "3bg.1e",       0x4000, 0x2000, 0x14f23cdd );
		ROM_REGION( 0x0400, REGION_PROMS );	ROM_LOAD( "jr-ic9g",      0x0000, 0x0200, 0x85577107 );	ROM_LOAD( "jr-ic9f",      0x0200, 0x0200, 0x085914d1 );
		ROM_REGION( 0x2000, REGION_SOUND1 );/* TMS5200 sample data??? */
		ROM_LOAD( "13snd.2g",     0x0000, 0x2000, 0xfc05654e );ROM_END(); }}; 
	
	
	
	public static GameDriver driver_monymony	   = new GameDriver("1983"	,"monymony"	,"zaccaria.java"	,rom_monymony,null	,machine_driver_zaccaria	,input_ports_monymony	,null	,ROT90	,	"Zaccaria", "Money Money", GAME_NO_SOUND | GAME_NO_COCKTAIL )
	public static GameDriver driver_jackrabt	   = new GameDriver("1984"	,"jackrabt"	,"zaccaria.java"	,rom_jackrabt,null	,machine_driver_zaccaria	,input_ports_jackrabt	,null	,ROT90	,	"Zaccaria", "Jack Rabbit (set 1)", GAME_NO_SOUND | GAME_NO_COCKTAIL )
	public static GameDriver driver_jackrab2	   = new GameDriver("1984"	,"jackrab2"	,"zaccaria.java"	,rom_jackrab2,driver_jackrabt	,machine_driver_zaccaria	,input_ports_jackrabt	,null	,ROT90	,	"Zaccaria", "Jack Rabbit (set 2)", GAME_NO_SOUND | GAME_NO_COCKTAIL )
	public static GameDriver driver_jackrabs	   = new GameDriver("1984"	,"jackrabs"	,"zaccaria.java"	,rom_jackrabs,driver_jackrabt	,machine_driver_zaccaria	,input_ports_jackrabt	,null	,ROT90	,	"Zaccaria", "Jack Rabbit (special)", GAME_NO_SOUND | GAME_NO_COCKTAIL )
}
