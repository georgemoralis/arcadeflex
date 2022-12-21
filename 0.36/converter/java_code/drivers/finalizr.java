/***************************************************************************

Finalizer (GX523) (c) 1985 Konami

driver by Nicola Salmoria

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;

public class finalizr
{
	
	
	void konami1_decode(void);
	
	static unsigned char *finalizr_interrupt_enable;
	
	void finalizr_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom);
	int finalizr_vh_start(void);
	void finalizr_vh_stop(void);
	public static WriteHandlerPtr finalizr_videoctrl_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	void finalizr_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh);
	
	
	
	public static InterruptPtr finalizr_interrupt = new InterruptPtr() { public int handler() 
	{
		if (cpu_getiloops() == 0)
		{
			if (*finalizr_interrupt_enable & 2) return M6809_INT_IRQ;
		}
		else if (cpu_getiloops() % 2)
		{
			if (*finalizr_interrupt_enable & 1) return nmi_interrupt();
		}
		return ignore_interrupt();
	} };
	
	public static WriteHandlerPtr finalizr_coin_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		coin_counter_w(0,data & 0x01);
		coin_counter_w(1,data & 0x02);
	} };
	
	
	static int i8039_irqenable;
	static int i8039_status;
	
	public static WriteHandlerPtr finalizr_i8039_irq_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (i8039_irqenable)
			cpu_cause_interrupt(1,I8039_EXT_INT);
	} };
	
	public static ReadHandlerPtr i8039_irqen_and_status_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return i8039_status;
	} };
	
	public static WriteHandlerPtr i8039_irqen_and_status_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		i8039_irqenable = data & 0x80;
		i8039_status = data;
	} };
	
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0800, 0x0800, input_port_5_r ),
		new MemoryReadAddress( 0x0808, 0x0808, input_port_4_r ),
		new MemoryReadAddress( 0x0810, 0x0810, input_port_0_r ),
		new MemoryReadAddress( 0x0811, 0x0811, input_port_1_r ),
		new MemoryReadAddress( 0x0812, 0x0812, input_port_2_r ),
		new MemoryReadAddress( 0x0813, 0x0813, input_port_3_r ),
		new MemoryReadAddress( 0x2000, 0x2fff, MRA_RAM ),
		new MemoryReadAddress( 0x3000, 0x3fff, MRA_RAM ),
		new MemoryReadAddress( 0x4000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x0000, MWA_NOP ),	/* ??? */
		new MemoryWriteAddress( 0x0001, 0x0001, MWA_RAM, finalizr_scroll ),
		new MemoryWriteAddress( 0x0002, 0x0002, MWA_NOP ),	/* ??? */
		new MemoryWriteAddress( 0x0003, 0x0003, finalizr_videoctrl_w ),
		new MemoryWriteAddress( 0x0004, 0x0004, MWA_RAM, finalizr_interrupt_enable ),
	//	new MemoryWriteAddress( 0x0020, 0x003f, MWA_RAM, finalizr_scroll ),
		new MemoryWriteAddress( 0x0818, 0x0818, watchdog_reset_w ),
		new MemoryWriteAddress( 0x0819, 0x0819, finalizr_coin_w ),
		new MemoryWriteAddress( 0x081a, 0x081a, SN76496_0_w ),	/* This address triggers the SN chip to read the data port. */
		new MemoryWriteAddress( 0x081b, 0x081b, MWA_NOP ),		/* Loads the snd command into the snd latch */
		new MemoryWriteAddress( 0x081c, 0x081c, finalizr_i8039_irq_w ),	/* custom sound chip */
		new MemoryWriteAddress( 0x081d, 0x081d, soundlatch_w ),			/* custom sound chip */
		new MemoryWriteAddress( 0x2000, 0x23ff, colorram_w, colorram ),
		new MemoryWriteAddress( 0x2400, 0x27ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x2800, 0x2bff, MWA_RAM, finalizr_colorram2 ),
		new MemoryWriteAddress( 0x2c00, 0x2fff, MWA_RAM, finalizr_videoram2 ),
		new MemoryWriteAddress( 0x3000, 0x31ff, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x3200, 0x37ff, MWA_RAM ),
		new MemoryWriteAddress( 0x3800, 0x39ff, MWA_RAM, spriteram_2 ),
		new MemoryWriteAddress( 0x3a00, 0x3fff, MWA_RAM ),
		new MemoryWriteAddress( 0x4000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress i8039_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x0fff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress i8039_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x0fff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static IOReadPort i8039_readport[] =
	{
		new IOReadPort( 0x00, 0xff, soundlatch_r ),
		new IOReadPort( I8039_p2, I8039_p2, i8039_irqen_and_status_r ),
		new IOReadPort( 0x111,0x111, IORP_NOP ),
		new IOReadPort( -1 )
	};
	
	static IOWritePort i8039_writeport[] =
	{
		new IOWritePort( I8039_p1, I8039_p1, DAC_data_w ),
		new IOWritePort( I8039_p2, I8039_p2, i8039_irqen_and_status_w ),
		new IOWritePort( -1 )	/* end of table */
	};
	
	
	
	static InputPortPtr input_ports_finalizr = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_VBLANK );
	
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") );
	/* 	PORT_DIPSETTING(    0x00, "Invalid" );*/
	
		PORT_START(); 	/* DSW */
		PORT_DIPNAME( 0x03, 0x02, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "2" );
		PORT_DIPSETTING(    0x02, "3" );
		PORT_DIPSETTING(    0x01, "4" );
		PORT_DIPSETTING(    0x00, "7" );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x18, "30000 150000" );
		PORT_DIPSETTING(    0x10, "50000 300000" );
		PORT_DIPSETTING(    0x08, "30000" );
		PORT_DIPSETTING(    0x00, "50000" );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* DSW */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "Controls" );
		PORT_DIPSETTING(    0x02, "Single" );
		PORT_DIPSETTING(    0x00, "Dual" );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_finalizb = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_VBLANK );
	
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") );
	/* 	PORT_DIPSETTING(    0x00, "Invalid" );*/
	
		PORT_START(); 	/* DSW */
		PORT_DIPNAME( 0x03, 0x02, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "2" );
		PORT_DIPSETTING(    0x02, "3" );
		PORT_DIPSETTING(    0x01, "4" );
		PORT_DIPSETTING(    0x00, "7" );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x18, "20000 100000" );
		PORT_DIPSETTING(    0x10, "30000 150000" );
		PORT_DIPSETTING(    0x08, "20000" );
		PORT_DIPSETTING(    0x00, "30000" );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* DSW */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "Controls" );
		PORT_DIPSETTING(    0x02, "Single" );
		PORT_DIPSETTING(    0x00, "Dual" );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		4096,	/* 4096 characters */
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },	/* the four bitplanes are packed in one nibble */
		new int[] { 0*4, 1*4, 0x10000*8+0*4, 0x10000*8+1*4, 2*4, 3*4, 0x10000*8+2*4, 0x10000*8+3*4 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
		16*8	/* every char takes 16 consecutive bytes */
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		1024,	/* 1024 sprites */
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },	/* the four bitplanes are packed in one nibble */
		new int[] { 0*4, 1*4, 0x10000*8+0*4, 0x10000*8+1*4, 2*4, 3*4, 0x10000*8+2*4, 0x10000*8+3*4,
	           16*8+0*4, 16*8+1*4, 16*8+0x10000*8+0*4, 16*8+0x10000*8+1*4, 16*8+2*4, 16*8+3*4, 16*8+0x10000*8+2*4, 16*8+0x10000*8+3*4 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16,
	           16*16, 17*16, 18*16, 19*16, 20*16, 21*16, 22*16, 23*16 },
		64*8	/* every sprite takes 64 consecutive bytes */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,        0, 16 ),
		new GfxDecodeInfo( REGION_GFX1, 0, spritelayout,  16*16, 16 ),
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,    16*16, 16 ),  /* to handle 8x8 sprites */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static SN76496interface sn76496_interface = new SN76496interface
	(
		1,	/* 1 chip */
		new int[] { 18432000/12 },	/* ?? */
		new int[] { 75 }
	);
	
	static DACinterface dac_interface = new DACinterface
	(
		1,
		new int[] { 75 }
	);
	
	
	
	static MachineDriver machine_driver_finalizr = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6809,
				18432000/6,	/* ??? */
				readmem,writemem,null,null,
				finalizr_interrupt,16	/* 1 IRQ + 8 NMI (generated by a custom IC) */
			),
			new MachineCPU(
				CPU_I8039 | CPU_AUDIO_CPU,
				5*18432000/15,	/* ??? */
				i8039_readmem,i8039_writemem,i8039_readport,i8039_writeport,
				ignore_interrupt,1
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* single CPU, no need for interleaving */
		null,
	
		/* video hardware */
		36*8, 32*8, { 1*8, 35*8-1, 2*8, 30*8-1 },
		gfxdecodeinfo,
		32, 2*16*16,
		finalizr_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		finalizr_vh_start,
		finalizr_vh_stop,
		finalizr_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_SN76496,
				sn76496_interface
			),
			new MachineSound(
				SOUND_DAC,
				dac_interface
			)
		}
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_finalizr = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2*0x10000, REGION_CPU1 );/* 64k for code + 64k for decrypted opcodes */
		ROM_LOAD( "523k01.9c",    0x4000, 0x4000, 0x716633cb );
		ROM_LOAD( "523k02.12c",   0x8000, 0x4000, 0x1bccc696 );
		ROM_LOAD( "523k03.13c",   0xc000, 0x4000, 0xc48927c6 );
	
		ROM_REGION( 0x1000, REGION_CPU2 );/* 8039 */
		ROM_LOAD( "d8749hd.bin",  0x0000, 0x0800, 0x978dfc33 );/* this comes from the bootleg, */
																/* the original has a custom IC */
	
		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "523h04.5e",    0x00000, 0x4000, 0xc056d710 );
		ROM_LOAD( "523h05.6e",    0x04000, 0x4000, 0xae0d0f76 );
		ROM_LOAD( "523h06.7e",    0x08000, 0x4000, 0xd2db9689 );
		/* 0c000-0ffff empty */
		ROM_LOAD( "523h07.5f",    0x10000, 0x4000, 0x50e512ba );
		ROM_LOAD( "523h08.6f",    0x14000, 0x4000, 0x79f44e17 );
		ROM_LOAD( "523h09.7f",    0x18000, 0x4000, 0x8896dc85 );
		/* 1c000-1ffff empty */
	
		ROM_REGION( 0x0240, REGION_PROMS );
		ROM_LOAD( "523h10.2f",    0x0000, 0x0020, 0xec15dd15 );/* palette */
		ROM_LOAD( "523h11.3f",    0x0020, 0x0020, 0x54be2e83 );/* palette */
		ROM_LOAD( "523h12.10f",   0x0040, 0x0100, 0x53166a2a );/* sprites */
		ROM_LOAD( "523h13.11f",   0x0140, 0x0100, 0x4e0647a0 );/* characters */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_finalizb = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2*0x10000, REGION_CPU1 );/* 64k for code + 64k for decrypted opcodes */
		ROM_LOAD( "finalizr.5",   0x4000, 0x8000, 0xa55e3f14 );
		ROM_LOAD( "finalizr.6",   0xc000, 0x4000, 0xce177f6e );
	
		ROM_REGION( 0x1000, REGION_CPU2 );/* 8039 */
		ROM_LOAD( "d8749hd.bin",  0x0000, 0x0800, 0x978dfc33 );
	
		ROM_REGION( 0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "523h04.5e",    0x00000, 0x4000, 0xc056d710 );
		ROM_LOAD( "523h05.6e",    0x04000, 0x4000, 0xae0d0f76 );
		ROM_LOAD( "523h06.7e",    0x08000, 0x4000, 0xd2db9689 );
		/* 0c000-0ffff empty */
		ROM_LOAD( "523h07.5f",    0x10000, 0x4000, 0x50e512ba );
		ROM_LOAD( "523h08.6f",    0x14000, 0x4000, 0x79f44e17 );
		ROM_LOAD( "523h09.7f",    0x18000, 0x4000, 0x8896dc85 );
		/* 1c000-1ffff empty */
	
		ROM_REGION( 0x0240, REGION_PROMS );
		ROM_LOAD( "523h10.2f",    0x0000, 0x0020, 0xec15dd15 );/* palette */
		ROM_LOAD( "523h11.3f",    0x0020, 0x0020, 0x54be2e83 );/* palette */
		ROM_LOAD( "523h12.10f",   0x0040, 0x0100, 0x53166a2a );/* sprites */
		ROM_LOAD( "523h13.11f",   0x0140, 0x0100, 0x4e0647a0 );/* characters */
	ROM_END(); }}; 
	
	
	public static InitDriverPtr init_finalizr = new InitDriverPtr() { public void handler() 
	{
		konami1_decode();
	} };
	
	
	public static GameDriver driver_finalizr	   = new GameDriver("1985"	,"finalizr"	,"finalizr.java"	,rom_finalizr,null	,machine_driver_finalizr	,input_ports_finalizr	,init_finalizr	,ROT90	,	"Konami", "Finalizer - Super Transformation", GAME_IMPERFECT_SOUND | GAME_NO_COCKTAIL )
	public static GameDriver driver_finalizb	   = new GameDriver("1985"	,"finalizb"	,"finalizr.java"	,rom_finalizb,driver_finalizr	,machine_driver_finalizr	,input_ports_finalizb	,init_finalizr	,ROT90	,	"bootleg", "Finalizer - Super Transformation (bootleg)", GAME_IMPERFECT_SOUND | GAME_NO_COCKTAIL )
}
