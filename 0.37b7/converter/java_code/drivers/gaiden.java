/***************************************************************************

Ninja Gaiden memory map (preliminary)

000000-03ffff ROM
060000-063fff RAM
070000-070fff Video RAM (text layer)
072000-075fff VRAM (backgrounds)
076000-077fff Sprite RAM
078000-079fff Palette RAM

07a100-07a1ff Unknown

memory mapped ports:

read:
07a001    IN0
07a002    IN2
07a003    IN1
07a004    DWSB
07a005    DSWA
see the input_ports definition below for details on the input bits

write:
07a104-07a105 text layer Y scroll
07a10c-07a10d text layer X scroll
07a204-07a205 front layer Y scroll
07a20c-07a20d front layer X scroll
07a304-07a305 back layer Y scroll
07a30c-07a30d back layer Xscroll

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class gaiden
{
	
	extern UBytePtr gaiden_videoram;
	extern UBytePtr gaiden_videoram2;
	extern UBytePtr gaiden_videoram3;
	
	
	
	
	
	
	
	
	public static InterruptPtr gaiden_interrupt = new InterruptPtr() { public int handler() 
	{
		return 5;  /*Interrupt vector 5*/
	} };
	
	public static ReadHandlerPtr gaiden_input_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch (offset)
		{
			case 0:
				return input_port_4_r.handler (offset);
				break;
			case 2:
				return (input_port_1_r.handler (offset) << 8) + (input_port_0_r.handler (offset));
				break;
			case 4:
				return (input_port_3_r.handler (offset) << 8) + (input_port_2_r.handler (offset));
				break;
		}
	
		return 0;
	} };
	
	
	public static WriteHandlerPtr gaiden_sound_command_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0xff000000) != 0) soundlatch_w.handler(0,data & 0xff);	/* Ninja Gaiden */
		if ((data & 0x00ff0000) != 0) soundlatch_w.handler(0,(data >> 8) & 0xff);	/* Tecmo Knight */
		cpu_cause_interrupt(1,Z80_NMI_INT);
	} };
	
	
	
	/* Tecmo Knight has a simple protection. It writes codes to 0x07a804, and reads */
	/* the answer from 0x07a007. The returned values contain the address of a */
	/* function to jump to. */
	
	static int prot;
	
	public static WriteHandlerPtr tknight_protection_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		static int jumpcode;
		static int jumppoints[] =
		{
			0x0c0c,0x0cac,0x0d42,0x0da2,0x0eea,0x112e,0x1300,0x13fa,
			0x159a,0x1630,0x109a,0x1700,0x1750,0x1806,0x18d6,0x1a44,
			0x1b52
		};
	
		data = (data >> 8) & 0xff;
	
	//logerror("PC %06x: prot = %02x\n",cpu_get_pc(),data);
	
		switch (data & 0xf0)
		{
			case 0x00:	/* init */
				prot = 0x00;
				break;
			case 0x10:	/* high 4 bits of jump code */
				jumpcode = (data & 0x0f) << 4;
				prot = 0x10;
				break;
			case 0x20:	/* low 4 bits of jump code */
				jumpcode |= data & 0x0f;
				if (jumpcode > 16)
				{
	logerror("unknown jumpcode %02x\n",jumpcode);
					jumpcode = 0;
				}
				prot = 0x20;
				break;
			case 0x30:	/* ask for bits 12-15 of function address */
				prot = 0x40 | ((jumppoints[jumpcode] >> 12) & 0x0f);
				break;
			case 0x40:	/* ask for bits 8-11 of function address */
				prot = 0x50 | ((jumppoints[jumpcode] >> 8) & 0x0f);
				break;
			case 0x50:	/* ask for bits 4-7 of function address */
				prot = 0x60 | ((jumppoints[jumpcode] >> 4) & 0x0f);
				break;
			case 0x60:	/* ask for bits 0-3 of function address */
				prot = 0x70 | ((jumppoints[jumpcode] >> 0) & 0x0f);
				break;
		}
	} };
	
	public static ReadHandlerPtr tknight_protection_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	//logerror("PC %06x: read prot %02x\n",cpu_get_pc(),prot);
		return prot;
	} };
	
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
		new MemoryReadAddress( 0x060000, 0x063fff, MRA_BANK1 ),   /* RAM */
		new MemoryReadAddress( 0x070000, 0x070fff, gaiden_videoram_r ),
		new MemoryReadAddress( 0x072000, 0x073fff, gaiden_videoram2_r ),
		new MemoryReadAddress( 0x074000, 0x075fff, gaiden_videoram3_r ),
		new MemoryReadAddress( 0x076000, 0x077fff, MRA_BANK2 ),
		new MemoryReadAddress( 0x078000, 0x0787ff, paletteram_word_r ),
		new MemoryReadAddress( 0x078800, 0x079fff, MRA_NOP ),   /* extra portion of palette RAM, not really used */
		new MemoryReadAddress( 0x07a000, 0x07a005, gaiden_input_r ),
		new MemoryReadAddress( 0x07a006, 0x07a007, tknight_protection_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x060000, 0x063fff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x070000, 0x070fff, gaiden_videoram_w, gaiden_videoram ),
		new MemoryWriteAddress( 0x072000, 0x073fff, gaiden_videoram2_w,  gaiden_videoram2 ),
		new MemoryWriteAddress( 0x074000, 0x075fff, gaiden_videoram3_w,  gaiden_videoram3 ),
		new MemoryWriteAddress( 0x076000, 0x077fff, MWA_BANK2, spriteram ),
		new MemoryWriteAddress( 0x078000, 0x0787ff, paletteram_xxxxBBBBGGGGRRRR_word_w, paletteram ),
		new MemoryWriteAddress( 0x078800, 0x079fff, MWA_NOP ),   /* extra portion of palette RAM, not really used */
		new MemoryWriteAddress( 0x07a104, 0x07a105, gaiden_txscrolly_w ),
		new MemoryWriteAddress( 0x07a10c, 0x07a10d, gaiden_txscrollx_w ),
		new MemoryWriteAddress( 0x07a204, 0x07a205, gaiden_fgscrolly_w ),
		new MemoryWriteAddress( 0x07a20c, 0x07a20d, gaiden_fgscrollx_w ),
		new MemoryWriteAddress( 0x07a304, 0x07a305, gaiden_bgscrolly_w ),
		new MemoryWriteAddress( 0x07a30c, 0x07a30d, gaiden_bgscrollx_w ),
		new MemoryWriteAddress( 0x07a800, 0x07a801, MWA_NOP ),
		new MemoryWriteAddress( 0x07a802, 0x07a803, gaiden_sound_command_w ),
		new MemoryWriteAddress( 0x07a804, 0x07a805, tknight_protection_w ),
		new MemoryWriteAddress( 0x07a806, 0x07a807, MWA_NOP ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0xdfff, MRA_ROM ),
		new MemoryReadAddress( 0xf000, 0xf7ff, MRA_RAM ),
		new MemoryReadAddress( 0xf800, 0xf800, OKIM6295_status_0_r ),
		new MemoryReadAddress( 0xfc00, 0xfc00, MRA_NOP ),	/* ?? */
		new MemoryReadAddress( 0xfc20, 0xfc20, soundlatch_r ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xdfff, MWA_ROM ),
		new MemoryWriteAddress( 0xf000, 0xf7ff, MWA_RAM ),
		new MemoryWriteAddress( 0xf800, 0xf800, OKIM6295_data_0_w ),
		new MemoryWriteAddress( 0xf810, 0xf810, YM2203_control_port_0_w ),
		new MemoryWriteAddress( 0xf811, 0xf811, YM2203_write_port_0_w ),
		new MemoryWriteAddress( 0xf820, 0xf820, YM2203_control_port_1_w ),
		new MemoryWriteAddress( 0xf821, 0xf821, YM2203_write_port_1_w ),
		new MemoryWriteAddress( 0xfc00, 0xfc00, MWA_NOP ),	/* ?? */
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	
	static InputPortPtr input_ports_gaiden = new InputPortPtr(){ public void handler() { 
	PORT_START();       /* PLAYER 1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1 | IPF_8WAY );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 | IPF_8WAY );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER1 | IPF_8WAY );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER1 | IPF_8WAY );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();       /* PLAYER 2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2 | IPF_8WAY );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 | IPF_8WAY );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2 | IPF_8WAY );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2 | IPF_8WAY );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 	/* DSWA */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x1c, 0x1c, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x1c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x14, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0xe0, 0xe0, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") );
	
		PORT_START(); 	/* DSWB */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, "Energy" );	PORT_DIPSETTING(    0x00, "2" );	PORT_DIPSETTING(    0x30, "3" );	PORT_DIPSETTING(    0x10, "4" );	PORT_DIPSETTING(    0x20, "5" );	PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "1" );	PORT_DIPSETTING(    0xc0, "2" );	PORT_DIPSETTING(    0x40, "3" );	PORT_DIPSETTING(    0x80, "4" );
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN2 );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_tknight = new InputPortPtr(){ public void handler() { 
	PORT_START();       /* PLAYER 1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1 | IPF_8WAY );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 | IPF_8WAY );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER1 | IPF_8WAY );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER1 | IPF_8WAY );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();       /* PLAYER 2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2 | IPF_8WAY );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 | IPF_8WAY );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2 | IPF_8WAY );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2 | IPF_8WAY );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 	/* DSWA */
		PORT_DIPNAME( 0xe0, 0xe0, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0x1c, 0x1c, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x1c, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x14, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x01, DEF_STR( "On") );
	
		PORT_START(); 	/* DSWB */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x80, "1" );	PORT_DIPSETTING(    0xc0, "2" );	PORT_DIPSETTING(    0x40, "3" );/*	PORT_DIPSETTING(    0x00, "2" );*/
	
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN2 );INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout tilelayout = new GfxLayout
	(
		8,8,	/* tile size */
		RGN_FRAC(1,1),	/* number of tiles */
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },
		new int[] { 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8	/* offset to next tile */
	);
	
	static GfxLayout tile2layout = new GfxLayout
	(
		16,16,	/* tile size */
		RGN_FRAC(1,1),	/* number of tiles */
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },	/* the bitplanes are packed in one nibble */
		new int[] { 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4,
		  32*8+0*4, 32*8+1*4, 32*8+2*4, 32*8+3*4,
		  32*8+4*4, 32*8+5*4, 32*8+6*4, 32*8+7*4,},
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32,
		  16*32, 17*32, 18*32, 19*32, 20*32, 21*32, 22*32, 23*32},
		128*8	/* offset to next tile */
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		8,8,	/* sprites size */
		RGN_FRAC(1,2),	/* number of sprites */
		4,	/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },	/* the bitplanes are packed in one nibble */
		new int[] { 0,4,RGN_FRAC(1,2),4+RGN_FRAC(1,2),8,12,8+RGN_FRAC(1,2),12+RGN_FRAC(1,2) },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
		16*8	/* offset to next sprite */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, tilelayout,        256, 16 ),	/* tiles 8x8 */
		new GfxDecodeInfo( REGION_GFX2, 0, tile2layout,       768, 16 ),	/* tiles 16x16 */
		new GfxDecodeInfo( REGION_GFX3, 0, tile2layout,       512, 16 ),	/* tiles 16x16 */
		new GfxDecodeInfo( REGION_GFX4, 0, spritelayout,        0, 16 ),	/* sprites 8x8 */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	/* handler called by the 2203 emulator when the internal timers cause an IRQ */
	static void irqhandler(int irq)
	{
		cpu_set_irq_line(1,0,irq ? ASSERT_LINE : CLEAR_LINE);
	}
	
	static YM2203interface ym2203_interface = new YM2203interface
	(
		2,			/* 2 chips */
		4000000,	/* 4 MHz ? (hand tuned) */
		new int[] { YM2203_VOL(60,15), YM2203_VOL(60,15) },
		new ReadHandlerPtr[] { 0 },
		new ReadHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteYmHandlerPtr[] { irqhandler }
	);
	
	
	static OKIM6295interface okim6295_interface = new OKIM6295interface
	(
		1,                  /* 1 chip */
		new int[] { 7576 },			/* 7576Hz frequency */
		new int[] { REGION_SOUND1 },	/* memory region */
		new int[] { 20 }
	);
	
	
	static MachineDriver machine_driver_gaiden = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				8000000,	/* 8 MHz */
				readmem,writemem,null,null,
				gaiden_interrupt,1,null,null
		),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				4000000,	/* 4 MHz */
				sound_readmem,sound_writemem,null,null,
				ignore_interrupt,0	/* NMIs are triggered by the main CPU */
									/* IRQs are triggered by the YM2203 */
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		32*8, 30*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		1024, 1024,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		gaiden_vh_start,
		null,
		gaiden_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203_interface
			),
			new MachineSound(
				SOUND_OKIM6295,
				okim6295_interface
			)
		}
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_gaiden = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );/* 2*128k for 68000 code */
		ROM_LOAD_EVEN( "gaiden.1",     0x00000, 0x20000, 0xe037ff7c );	ROM_LOAD_ODD ( "gaiden.2",     0x00000, 0x20000, 0x454f7314 );
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "gaiden.3",     0x0000, 0x10000, 0x75fd3e6a );  /* Audio CPU is a Z80  */
	
		ROM_REGION( 0x010000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "gaiden.5",     0x000000, 0x10000, 0x8d4035f7 );/* 8x8 tiles */
	
		ROM_REGION( 0x080000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "14.bin",       0x000000, 0x20000, 0x1ecfddaa );	ROM_LOAD( "15.bin",       0x020000, 0x20000, 0x1291a696 );	ROM_LOAD( "16.bin",       0x040000, 0x20000, 0x140b47ca );	ROM_LOAD( "17.bin",       0x060000, 0x20000, 0x7638cccb );
		ROM_REGION( 0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE );	ROM_LOAD( "18.bin",       0x000000, 0x20000, 0x3fadafd6 );	ROM_LOAD( "19.bin",       0x020000, 0x20000, 0xddae9d5b );	ROM_LOAD( "20.bin",       0x040000, 0x20000, 0x08cf7a93 );	ROM_LOAD( "21.bin",       0x060000, 0x20000, 0x1ac892f5 );
		ROM_REGION( 0x100000, REGION_GFX4 | REGIONFLAG_DISPOSE );	ROM_LOAD( "gaiden.6",     0x000000, 0x20000, 0xe7ccdf9f );/* sprites A1 */
		ROM_LOAD( "gaiden.8",     0x020000, 0x20000, 0x7ef7f880 );/* sprites B1 */
		ROM_LOAD( "gaiden.10",    0x040000, 0x20000, 0xa6451dec );/* sprites C1 */
		ROM_LOAD( "gaiden.12",    0x060000, 0x20000, 0x90f1e13a );/* sprites D1 */
		ROM_LOAD( "gaiden.7",     0x080000, 0x20000, 0x016bec95 );/* sprites A2 */
		ROM_LOAD( "gaiden.9",     0x0a0000, 0x20000, 0x6e9b7fd3 );/* sprites B2 */
		ROM_LOAD( "gaiden.11",    0x0c0000, 0x20000, 0x7fbfdf5e );/* sprites C2 */
		ROM_LOAD( "gaiden.13",    0x0e0000, 0x20000, 0x7d9f5c5e );/* sprites D2 */
	
		ROM_REGION( 0x20000, REGION_SOUND1 );/* 128k for ADPCM samples - sound chip is OKIM6295 */
		ROM_LOAD( "gaiden.4",     0x0000, 0x20000, 0xb0e0faf9 );/* samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_shadoww = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );/* 2*128k for 68000 code */
		ROM_LOAD_EVEN( "shadoww.1",    0x00000, 0x20000, 0xfefba387 );	ROM_LOAD_ODD ( "shadoww.2",    0x00000, 0x20000, 0x9b9d6b18 );
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "gaiden.3",     0x0000, 0x10000, 0x75fd3e6a );  /* Audio CPU is a Z80  */
	
		ROM_REGION( 0x010000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "gaiden.5",     0x000000, 0x10000, 0x8d4035f7 );/* 8x8 tiles */
	
		ROM_REGION( 0x080000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "14.bin",       0x000000, 0x20000, 0x1ecfddaa );	ROM_LOAD( "15.bin",       0x020000, 0x20000, 0x1291a696 );	ROM_LOAD( "16.bin",       0x040000, 0x20000, 0x140b47ca );	ROM_LOAD( "17.bin",       0x060000, 0x20000, 0x7638cccb );
		ROM_REGION( 0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE );	ROM_LOAD( "18.bin",       0x000000, 0x20000, 0x3fadafd6 );	ROM_LOAD( "19.bin",       0x020000, 0x20000, 0xddae9d5b );	ROM_LOAD( "20.bin",       0x040000, 0x20000, 0x08cf7a93 );	ROM_LOAD( "21.bin",       0x060000, 0x20000, 0x1ac892f5 );
		ROM_REGION( 0x100000, REGION_GFX4 | REGIONFLAG_DISPOSE );	ROM_LOAD( "gaiden.6",     0x000000, 0x20000, 0xe7ccdf9f );/* sprites A1 */
		ROM_LOAD( "gaiden.8",     0x020000, 0x20000, 0x7ef7f880 );/* sprites B1 */
		ROM_LOAD( "gaiden.10",    0x040000, 0x20000, 0xa6451dec );/* sprites C1 */
		ROM_LOAD( "shadoww.12a",  0x060000, 0x10000, 0x9bb07731 );/* sprites D1 */
		ROM_LOAD( "shadoww.12b",  0x070000, 0x10000, 0xa4a950a2 );/* sprites D1 */
		ROM_LOAD( "gaiden.7",     0x080000, 0x20000, 0x016bec95 );/* sprites A2 */
		ROM_LOAD( "gaiden.9",     0x0a0000, 0x20000, 0x6e9b7fd3 );/* sprites B2 */
		ROM_LOAD( "gaiden.11",    0x0c0000, 0x20000, 0x7fbfdf5e );/* sprites C2 */
		ROM_LOAD( "shadoww.13a",  0x0e0000, 0x10000, 0x996d2fa5 );/* sprites D2 */
		ROM_LOAD( "shadoww.13b",  0x0f0000, 0x10000, 0xb8df8a34 );/* sprites D2 */
	
		ROM_REGION( 0x20000, REGION_SOUND1 );/* 128k for ADPCM samples - sound chip is OKIM6295 */
		ROM_LOAD( "gaiden.4",     0x0000, 0x20000, 0xb0e0faf9 );/* samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_ryukendn = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );/* 2*128k for 68000 code */
		ROM_LOAD_EVEN( "ryukendn.1",  0x00000, 0x20000, 0x6203a5e2 );	ROM_LOAD_ODD ( "ryukendn.2",  0x00000, 0x20000, 0x9e99f522 );
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "ryukendn.3",   0x0000, 0x10000, 0x6b686b69 );  /* Audio CPU is a Z80  */
	
		ROM_REGION( 0x010000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "ryukendn.5",   0x000000, 0x10000, 0x765e7baa );/* 8x8 tiles */
	
		ROM_REGION( 0x080000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "14.bin",       0x000000, 0x20000, 0x1ecfddaa );	ROM_LOAD( "15.bin",       0x020000, 0x20000, 0x1291a696 );	ROM_LOAD( "16.bin",       0x040000, 0x20000, 0x140b47ca );	ROM_LOAD( "17.bin",       0x060000, 0x20000, 0x7638cccb );
		ROM_REGION( 0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE );	ROM_LOAD( "18.bin",       0x000000, 0x20000, 0x3fadafd6 );	ROM_LOAD( "19.bin",       0x020000, 0x20000, 0xddae9d5b );	ROM_LOAD( "20.bin",       0x040000, 0x20000, 0x08cf7a93 );	ROM_LOAD( "21.bin",       0x060000, 0x20000, 0x1ac892f5 );
		ROM_REGION( 0x100000, REGION_GFX4 | REGIONFLAG_DISPOSE );	ROM_LOAD( "gaiden.6",     0x000000, 0x20000, 0xe7ccdf9f );/* sprites A1 */
		ROM_LOAD( "gaiden.8",     0x020000, 0x20000, 0x7ef7f880 );/* sprites B1 */
		ROM_LOAD( "gaiden.10",    0x040000, 0x20000, 0xa6451dec );/* sprites C1 */
		ROM_LOAD( "shadoww.12a",  0x060000, 0x10000, 0x9bb07731 );/* sprites D1 */
		ROM_LOAD( "ryukendn.12b", 0x070000, 0x10000, 0x1773628a );/* sprites D1 */
		ROM_LOAD( "gaiden.7",     0x080000, 0x20000, 0x016bec95 );/* sprites A2 */
		ROM_LOAD( "ryukendn.9a",  0x0a0000, 0x10000, 0xc821e200 );/* sprites B2 */
		ROM_LOAD( "ryukendn.9b",  0x0b0000, 0x10000, 0x6a6233b3 );/* sprites B2 */
		ROM_LOAD( "gaiden.11",    0x0c0000, 0x20000, 0x7fbfdf5e );/* sprites C2 */
		ROM_LOAD( "shadoww.13a",  0x0e0000, 0x10000, 0x996d2fa5 );/* sprites D2 */
		ROM_LOAD( "ryukendn.13b", 0x0f0000, 0x10000, 0x1f43c507 );/* sprites D2 */
	
		ROM_REGION( 0x20000, REGION_SOUND1 );/* 128k for ADPCM samples - sound chip is OKIM6295 */
		ROM_LOAD( "gaiden.4",     0x0000, 0x20000, 0xb0e0faf9 );/* samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_tknight = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );/* 2*128k for 68000 code */
		ROM_LOAD_EVEN( "tkni1.bin",    0x00000, 0x20000, 0x9121daa8 );	ROM_LOAD_ODD ( "tkni2.bin",    0x00000, 0x20000, 0x6669cd87 );
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "tkni3.bin",    0x0000, 0x10000, 0x15623ec7 );  /* Audio CPU is a Z80  */
	
		ROM_REGION( 0x010000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "tkni5.bin",    0x000000, 0x10000, 0x5ed15896 );/* 8x8 tiles */
	
		ROM_REGION( 0x080000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "tkni7.bin",    0x000000, 0x80000, 0x4b4d4286 );
		ROM_REGION( 0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE );	ROM_LOAD( "tkni6.bin",    0x000000, 0x80000, 0xf68fafb1 );
		ROM_REGION( 0x100000, REGION_GFX4 | REGIONFLAG_DISPOSE );	ROM_LOAD( "tkni9.bin",    0x000000, 0x80000, 0xd22f4239 );/* sprites */
		ROM_LOAD( "tkni8.bin",    0x080000, 0x80000, 0x4931b184 );/* sprites */
	
		ROM_REGION( 0x20000, REGION_SOUND1 );/* 128k for ADPCM samples - sound chip is OKIM6295 */
		ROM_LOAD( "tkni4.bin",    0x0000, 0x20000, 0xa7a1dbcf );/* samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_wildfang = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );/* 2*128k for 68000 code */
		ROM_LOAD_EVEN( "1.3st",    0x00000, 0x20000, 0xab876c9b );	ROM_LOAD_ODD ( "2.5st",    0x00000, 0x20000, 0x1dc74b3b );
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "tkni3.bin",    0x0000, 0x10000, 0x15623ec7 );  /* Audio CPU is a Z80  */
	
		ROM_REGION( 0x010000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "tkni5.bin",    0x000000, 0x10000, 0x5ed15896 );/* 8x8 tiles */
	
		ROM_REGION( 0x080000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "14.3a",        0x000000, 0x20000, 0x0d20c10c );	ROM_LOAD( "15.3b",        0x020000, 0x20000, 0x3f40a6b4 );	ROM_LOAD( "16.1a",        0x040000, 0x20000, 0x0f31639e );	ROM_LOAD( "17.1b",        0x060000, 0x20000, 0xf32c158e );
		ROM_REGION( 0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE );	ROM_LOAD( "tkni6.bin",    0x000000, 0x80000, 0xf68fafb1 );
		ROM_REGION( 0x100000, REGION_GFX4 | REGIONFLAG_DISPOSE );	ROM_LOAD( "tkni9.bin",    0x000000, 0x80000, 0xd22f4239 );/* sprites */
		ROM_LOAD( "tkni8.bin",    0x080000, 0x80000, 0x4931b184 );/* sprites */
	
		ROM_REGION( 0x20000, REGION_SOUND1 );/* 128k for ADPCM samples - sound chip is OKIM6295 */
		ROM_LOAD( "tkni4.bin",    0x0000, 0x20000, 0xa7a1dbcf );/* samples */
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_gaiden	   = new GameDriver("1988"	,"gaiden"	,"gaiden.java"	,rom_gaiden,null	,machine_driver_gaiden	,input_ports_gaiden	,null	,ROT0	,	"Tecmo", "Ninja Gaiden (World)", GAME_NO_COCKTAIL )
	public static GameDriver driver_shadoww	   = new GameDriver("1988"	,"shadoww"	,"gaiden.java"	,rom_shadoww,driver_gaiden	,machine_driver_gaiden	,input_ports_gaiden	,null	,ROT0	,	"Tecmo", "Shadow Warriors (US)", GAME_NO_COCKTAIL )
	public static GameDriver driver_ryukendn	   = new GameDriver("1989"	,"ryukendn"	,"gaiden.java"	,rom_ryukendn,driver_gaiden	,machine_driver_gaiden	,input_ports_gaiden	,null	,ROT0	,	"Tecmo", "Ninja Ryukenden (Japan)", GAME_NO_COCKTAIL )
	public static GameDriver driver_tknight	   = new GameDriver("1989"	,"tknight"	,"gaiden.java"	,rom_tknight,null	,machine_driver_gaiden	,input_ports_tknight	,null	,ROT0	,	"Tecmo", "Tecmo Knight", GAME_NO_COCKTAIL )
	public static GameDriver driver_wildfang	   = new GameDriver("1989"	,"wildfang"	,"gaiden.java"	,rom_wildfang,driver_tknight	,machine_driver_gaiden	,input_ports_tknight	,null	,ROT0	,	"Tecmo", "Wild Fang", GAME_NO_COCKTAIL )
}
