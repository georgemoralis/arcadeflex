/***************************************************************************

Gun Dealer memory map

driver by Nicola Salmoria

Yam! Yam!? runs on the same hardware but has a protection device which can
           access RAM at e000. Program writes to e000 and expects a value back
		   at e001, then jumps to subroutines at e010 and e020. Also, the
		   player and coin inputs appear magically at e004-e006.

0000-7fff ROM
8000-bfff ROM (banked)
c400-c7ff palette RAM
c800-cfff background video RAM
d000-dfff foreground (scrollable) video RAM.
e000-ffff work RAM

read:
c000      DSW0
c001      DSW1
c004      COIN (Gun Dealer only)
c005      IN1 (Gun Dealer only)
c006      IN0 (Gun Dealer only)

write:
c010-c011 foreground scroll x lo-hi (Yam Yam)
c012-c013 foreground scroll y lo-hi (Yam Yam)
c014      flip screen
c015      Yam Yam only, maybe reset protection device
c016      ROM bank selector
c020-c021 foreground scroll x hi-lo (Gun Dealer)
c022-c023 foreground scroll y hi-lo (Gun Dealer)

I/O:
read:
01        YM2203 read

write:
00        YM2203 control
01        YM2203 write

Interrupts:
Runs in interrupt mode 0, the interrupt vectors are 0xcf (RST 08h) and
0xd7 (RST 10h)

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class gundealr
{
	
	
	extern UBytePtr gundealr_bg_videoram,*gundealr_fg_videoram;
	
	
	
	static int input_ports_hack;
	
	public static InterruptPtr yamyam_interrupt = new InterruptPtr() { public int handler() 
	{
		if (cpu_getiloops() == 0)
		{
			if (input_ports_hack != 0)
			{
				UBytePtr RAM = memory_region(REGION_CPU1);
				RAM[0xe004] = readinputport(4);	/* COIN */
				RAM[0xe005] = readinputport(3);	/* IN1 */
				RAM[0xe006] = readinputport(2);	/* IN0 */
			}
			return 0xd7;	/* RST 10h vblank */
		}
		if ((cpu_getiloops() & 1) == 1) return 0xcf;	/* RST 08h sound (hand tuned) */
		else return ignore_interrupt();
	} };
	
	public static WriteHandlerPtr yamyam_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	 	int bankaddress;
		UBytePtr RAM = memory_region(REGION_CPU1);
	
		bankaddress = 0x10000 + (data & 0x07) * 0x4000;
		cpu_setbank(1,&RAM[bankaddress]);
	} };
	
	public static WriteHandlerPtr yamyam_protection_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
	
	logerror("e000 = %02x\n",RAM[0xe000]);
		RAM[0xe000] = data;
		if (data == 0x03) RAM[0xe001] = 0x03;
		if (data == 0x04) RAM[0xe001] = 0x04;
		if (data == 0x05) RAM[0xe001] = 0x05;
		if (data == 0x0a) RAM[0xe001] = 0x08;
		if (data == 0x0d) RAM[0xe001] = 0x07;
	
		if (data == 0x03)
		{
			/*
			read dip switches
			3a 00 c0  ld   a,($c000)
			47        ld   b,a
			3a 01 c0  ld   a,($c001)
			c9        ret
			*/
			RAM[0xe010] = 0x3a;
			RAM[0xe011] = 0x00;
			RAM[0xe012] = 0xc0;
			RAM[0xe013] = 0x47;
			RAM[0xe014] = 0x3a;
			RAM[0xe015] = 0x01;
			RAM[0xe016] = 0xc0;
			RAM[0xe017] = 0xc9;
		}
		if (data == 0x05)
		{
			/*
			add a to hl
			c5        push    bc
			010000    ld      bc,#0000
			4f        ld      c,a
			09        add     hl,bc
			c1        pop     bc
			c9        ret
			*/
			RAM[0xe020] = 0xc5;
			RAM[0xe021] = 0x01;
			RAM[0xe022] = 0x00;
			RAM[0xe023] = 0x00;
			RAM[0xe024] = 0x4f;
			RAM[0xe025] = 0x09;
			RAM[0xe026] = 0xc1;
			RAM[0xe027] = 0xc9;
			/*
			lookup data in table
			cd20e0    call    #e020
			7e        ld      a,(hl)
			c9        ret
			*/
			RAM[0xe010] = 0xcd;
			RAM[0xe011] = 0x20;
			RAM[0xe012] = 0xe0;
			RAM[0xe013] = 0x7e;
			RAM[0xe014] = 0xc9;
		}
	} };
	
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0xbfff, MRA_BANK1 ),
		new MemoryReadAddress( 0xc000, 0xc000, input_port_0_r ),	/* DSW0 */
		new MemoryReadAddress( 0xc001, 0xc001, input_port_1_r ),	/* DSW1 */
		new MemoryReadAddress( 0xc004, 0xc004, input_port_2_r ),	/* COIN (Gun Dealer only) */
		new MemoryReadAddress( 0xc005, 0xc005, input_port_3_r ),	/* IN1 (Gun Dealer only) */
		new MemoryReadAddress( 0xc006, 0xc006, input_port_4_r ),	/* IN0 (Gun Dealer only) */
		new MemoryReadAddress( 0xc400, 0xffff, MRA_RAM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new MemoryWriteAddress( 0xc010, 0xc013, yamyam_fg_scroll_w ),		/* Yam Yam only */
		new MemoryWriteAddress( 0xc014, 0xc014, gundealr_flipscreen_w ),
		new MemoryWriteAddress( 0xc016, 0xc016, yamyam_bankswitch_w ),
		new MemoryWriteAddress( 0xc020, 0xc023, gundealr_fg_scroll_w ),	/* Gun Dealer only */
		new MemoryWriteAddress( 0xc400, 0xc7ff, gundealr_paletteram_w, paletteram ),
		new MemoryWriteAddress( 0xc800, 0xcfff, gundealr_bg_videoram_w, gundealr_bg_videoram ),
		new MemoryWriteAddress( 0xd000, 0xdfff, gundealr_fg_videoram_w, gundealr_fg_videoram ),
		new MemoryWriteAddress( 0xe000, 0xffff, MWA_RAM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static IOReadPort readport[] =
	{
		new IOReadPort( 0x01, 0x01, YM2203_read_port_0_r ),
		new IOReadPort( -1 )	/* end of table */
	};
	
	static IOWritePort writeport[] =
	{
		new IOWritePort( 0x00, 0x00, YM2203_control_port_0_w ),
		new IOWritePort( 0x01, 0x01, YM2203_write_port_0_w ),
		new IOWritePort( -1 )	/* end of table */
	};
	
	
	
	static InputPortPtr input_ports_gundealr = new InputPortPtr(){ public void handler() { 
	PORT_START(); 	/* DSW0 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x0c, "Easy" );	PORT_DIPSETTING(    0x08, "Medium" );	PORT_DIPSETTING(    0x04, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x28, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, "0" );	PORT_DIPSETTING(    0x40, "1" );	PORT_DIPSETTING(    0x80, "2" );	PORT_DIPSETTING(    0xc0, "3" );
		PORT_START(); 	/* COIN */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably unused */
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably unused */
	
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably unused */
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably unused */
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_yamyam = new InputPortPtr(){ public void handler() { 
	PORT_START(); 	/* DSW0 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );	PORT_DIPSETTING(    0x01, "4" );	PORT_DIPSETTING(    0x02, "5" );	PORT_DIPSETTING(    0x03, "6" );	PORT_DIPNAME( 0x0c, 0x00, "Difficulty?" );	PORT_DIPSETTING(    0x00, "Easy?" );	PORT_DIPSETTING(    0x04, "Medium?" );	PORT_DIPSETTING(    0x08, "Hard?" );	PORT_DIPSETTING(    0x0c, "Hardest?" );	PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x07, 0x00, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x07, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "2C_1C") );
	/*	PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") ); */
	/*	PORT_DIPSETTING(    0x02, DEF_STR( "1C_1C") ); */
	/*	PORT_DIPSETTING(    0x01, DEF_STR( "1C_1C") ); */
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0x38, 0x00, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x38, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x28, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "2C_1C") );
	/*	PORT_DIPSETTING(    0x18, DEF_STR( "1C_1C") ); */
	/*	PORT_DIPSETTING(    0x10, DEF_STR( "1C_1C") ); */
	/*	PORT_DIPSETTING(    0x08, DEF_STR( "1C_1C") ); */
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Free_Play") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
		PORT_START(); 	/* COIN */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_TILT );/* "TEST" */
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably unused */
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably unused */
	
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably unused */
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );/* probably unused */
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,1),
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] { 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,1),
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] { 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4,
				16*32+0*4, 16*32+1*4, 16*32+2*4, 16*32+3*4, 16*32+4*4, 16*32+5*4, 16*32+6*4, 16*32+7*4 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32,
				8*32, 9*32, 10*32, 11*32, 12*32, 13*32, 14*32, 15*32 },
		128*8
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,     0, 16 ),	/* colors 0-255 */
		new GfxDecodeInfo( REGION_GFX2, 0, spritelayout, 256, 16 ),	/* colors 256-511 */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static YM2203interface ym2203_interface = new YM2203interface
	(
		1,			/* 1 chip */
		1500000,	/* 1.5 MHz ?????? */
		new int[] { YM2203_VOL(25,25) },
		new ReadHandlerPtr[] { 0 },
		new ReadHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 }
	);
	
	
	
	static MachineDriver machine_driver_gundealr = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				8000000,	/* 8 MHz ??? */
				readmem,writemem,readport,writeport,
				yamyam_interrupt,4	/* ? */
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* single CPU, no need for interleaving */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		512, 512,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		gundealr_vh_start,
		0,
		gundealr_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203_interface
			)
		}
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_gundealr = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x30000, REGION_CPU1 );/* 64k for code + 128k for banks */
		ROM_LOAD( "gundealr.1",   0x00000, 0x10000, 0x5797e830 );	ROM_RELOAD(               0x10000, 0x10000 );/* banked at 0x8000-0xbfff */
	
		ROM_REGION( 0x10000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "gundealr.3",   0x00000, 0x10000, 0x01f99de2 );
		ROM_REGION( 0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "gundealr.2",   0x00000, 0x20000, 0x7874ec41 );ROM_END(); }}; 
	
	static RomLoadPtr rom_gundeala = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x30000, REGION_CPU1 );/* 64k for code + 128k for banks */
		ROM_LOAD( "gundeala.1",   0x00000, 0x10000, 0xd87e24f1 );	ROM_RELOAD(               0x10000, 0x10000 );/* banked at 0x8000-0xbfff */
	
		ROM_REGION( 0x10000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "gundeala.3",   0x00000, 0x10000, 0x836cf1a3 );
		ROM_REGION( 0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "gundeala.2",   0x00000, 0x20000, 0x4b5fb53c );ROM_END(); }}; 
	
	static RomLoadPtr rom_yamyam = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x30000, REGION_CPU1 );/* 64k for code + 128k for banks */
		ROM_LOAD( "b3.f10",       0x00000, 0x20000, 0x96ae9088 );	ROM_RELOAD(               0x10000, 0x20000 );/* banked at 0x8000-0xbfff */
	
		ROM_REGION( 0x10000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "b2.d16",       0x00000, 0x10000, 0xcb4f84ee );
		ROM_REGION( 0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "b1.a16",       0x00000, 0x20000, 0xb122828d );ROM_END(); }}; 
	
	/* only gfx are different, code is the same */
	static RomLoadPtr rom_wiseguy = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x30000, REGION_CPU1 );/* 64k for code + 128k for banks */
		ROM_LOAD( "b3.f10",       0x00000, 0x20000, 0x96ae9088 );	ROM_RELOAD(               0x10000, 0x20000 );/* banked at 0x8000-0xbfff */
	
		ROM_REGION( 0x10000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "wguyb2.bin",   0x00000, 0x10000, 0x1c684c46 );
		ROM_REGION( 0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "b1.a16",       0x00000, 0x20000, 0xb122828d );ROM_END(); }}; 
	
	
	
	static public static InitDriverPtr init_gundealr = new InitDriverPtr() { public void handler() 
	{
		input_ports_hack = 0;
	} };
	
	static public static InitDriverPtr init_yamyam = new InitDriverPtr() { public void handler() 
	{
		input_ports_hack = 1;
		install_mem_write_handler(0, 0xe000, 0xe000, yamyam_protection_w);
	} };
	
	
	
	public static GameDriver driver_gundealr	   = new GameDriver("1990"	,"gundealr"	,"gundealr.java"	,rom_gundealr,null	,machine_driver_gundealr	,input_ports_gundealr	,init_gundealr	,ROT270	,	"Dooyong", "Gun Dealer (set 1)" )
	public static GameDriver driver_gundeala	   = new GameDriver("????"	,"gundeala"	,"gundealr.java"	,rom_gundeala,driver_gundealr	,machine_driver_gundealr	,input_ports_gundealr	,init_gundealr	,ROT270	,	"Dooyong", "Gun Dealer (set 2)" )
	public static GameDriver driver_yamyam	   = new GameDriver("1990"	,"yamyam"	,"gundealr.java"	,rom_yamyam,null	,machine_driver_gundealr	,input_ports_yamyam	,init_yamyam	,ROT0	,	"Dooyong", "Yam! Yam!?" )
	public static GameDriver driver_wiseguy	   = new GameDriver("1990"	,"wiseguy"	,"gundealr.java"	,rom_wiseguy,driver_yamyam	,machine_driver_gundealr	,input_ports_yamyam	,init_yamyam	,ROT0	,	"Dooyong", "Wise Guy" )
}
