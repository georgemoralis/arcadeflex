/***************************************************************************
  Rainbow Islands (and Jumping)

  driver by Mike Coates

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class rainbow
{
	
	/***************************************************************************
	  Video Hardware - Uses similar engine to Rastan
	***************************************************************************/
	
	extern size_t rastan_videoram_size;
	
	extern UBytePtr rastan_ram;
	extern UBytePtr rastan_videoram1,*rastan_videoram3;
	extern UBytePtr rastan_spriteram;
	extern UBytePtr rastan_scrollx;
	extern UBytePtr rastan_scrolly;
	
	
	
	
	
	
	/***************************************************************************
	  Sound Hardware
	
	  Rainbow uses a YM2151 and YM2103
	  Jumping uses two YM2203's
	***************************************************************************/
	
	void rastan_irq_handler(int irq);
	
	
	static MemoryReadAddress rastan_s_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new MemoryReadAddress( 0x4000, 0x7fff, MRA_BANK5 ),
		new MemoryReadAddress( 0x8000, 0x8fff, MRA_RAM ),
		new MemoryReadAddress( 0x9001, 0x9001, YM2151_status_port_0_r ),
		new MemoryReadAddress( 0x9002, 0x9100, MRA_RAM ),
		new MemoryReadAddress( 0xa001, 0xa001, rastan_a001_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress rastan_s_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0x8000, 0x8fff, MWA_RAM ),
		new MemoryWriteAddress( 0x9000, 0x9000, YM2151_register_port_0_w ),
		new MemoryWriteAddress( 0x9001, 0x9001, YM2151_data_port_0_w ),
		new MemoryWriteAddress( 0xa000, 0xa000, rastan_a000_w ),
		new MemoryWriteAddress( 0xa001, 0xa001, rastan_a001_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	public static WriteHandlerPtr rastan_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_CPU2);
		int banknum = ( data - 1 ) & 3;
		cpu_setbank( 5, &RAM[ 0x10000 + ( banknum * 0x4000 ) ] );
	} };
	
	static YM2151interface ym2151_interface = new YM2151interface
	(
		1,			/* 1 chip */
		4000000,	/* 4 MHz ? */
		new int[] { YM3012_VOL(50,MIXER_PAN_LEFT,50,MIXER_PAN_RIGHT) },
		new WriteYmHandlerPtr[] { rastan_irq_handler },
		new WriteHandlerPtr[] { rastan_bankswitch_w }
	);
	
	/***************************************************************************
	  Rainbow Islands Specific
	***************************************************************************/
	
	
	
	public static WriteHandlerPtr rainbow_sound_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (offset == 0)
		{
			rastan_sound_port_w(0,data & 0xff);
		}
		else if (offset == 2)
		{
			rastan_sound_comm_w(0,data & 0xff);
		}
	} };
	
	static MemoryReadAddress rainbow_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x10c000, 0x10ffff, MRA_BANK1 ),	/* RAM */
		new MemoryReadAddress( 0x200000, 0x20ffff, paletteram_word_r ),
		new MemoryReadAddress( 0x390000, 0x390003, input_port_0_r ),
		new MemoryReadAddress( 0x3B0000, 0x3B0003, input_port_1_r ),
		new MemoryReadAddress( 0x3e0000, 0x3e0003, rastan_sound_r ),
		new MemoryReadAddress( 0x800000, 0x80ffff, rainbow_c_chip_r ),
		new MemoryReadAddress( 0xc00000, 0xc03fff, rastan_videoram1_r ),
		new MemoryReadAddress( 0xc04000, 0xc07fff, MRA_BANK2 ),
		new MemoryReadAddress( 0xc08000, 0xc0bfff, rastan_videoram3_r ),
		new MemoryReadAddress( 0xc0c000, 0xc0ffff, MRA_BANK3 ),
		new MemoryReadAddress( 0xd00000, 0xd0ffff, MRA_BANK4 ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress rainbow_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x10c000, 0x10ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x200000, 0x20ffff, paletteram_xBBBBBGGGGGRRRRR_word_w, paletteram ),
		new MemoryWriteAddress( 0x800000, 0x80ffff, rainbow_c_chip_w ),
		new MemoryWriteAddress( 0xc00000, 0xc03fff, rastan_videoram1_w, rastan_videoram1, rastan_videoram_size ),
		new MemoryWriteAddress( 0xc04000, 0xc07fff, MWA_BANK2 ),
		new MemoryWriteAddress( 0xc08000, 0xc0bfff, rastan_videoram3_w, rastan_videoram3 ),
		new MemoryWriteAddress( 0xc0c000, 0xc0ffff, MWA_BANK3 ),
		new MemoryWriteAddress( 0xc20000, 0xc20003, rastan_scrollY_w, rastan_scrolly ),  /* scroll Y  1st.w plane1  2nd.w plane2 */
		new MemoryWriteAddress( 0xc40000, 0xc40003, rastan_scrollX_w, rastan_scrollx ),  /* scroll X  1st.w plane1  2nd.w plane2 */
		new MemoryWriteAddress( 0xc50000, 0xc50003, rastan_flipscreen_w ), /* bit 0  flipscreen */
		new MemoryWriteAddress( 0xd00000, 0xd0ffff, MWA_BANK4, rastan_spriteram ),
		new MemoryWriteAddress( 0x3e0000, 0x3e0003, rainbow_sound_w ),
		new MemoryWriteAddress( 0x3a0000, 0x3a0003, MWA_NOP ),
		new MemoryWriteAddress( 0x3c0000, 0x3c0003, MWA_NOP ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static InputPortPtr input_ports_rainbow = new InputPortPtr(){ public void handler() { 
	PORT_START(); 	/* DIP SWITCH A */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_6C") );
	
		PORT_START(); 	/* DIP SWITCH B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Medium" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "None" );	PORT_DIPSETTING(    0x04, "100k,1000k" );	PORT_DIPNAME( 0x08, 0x08, "Complete Bonus" );	PORT_DIPSETTING(    0x00, "100K Points" );	PORT_DIPSETTING(    0x08, "1 Up" );	PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x10, "1" );	PORT_DIPSETTING(    0x00, "2" );	PORT_DIPSETTING(    0x30, "3" );	PORT_DIPSETTING(    0x20, "4" );	PORT_DIPNAME( 0x40, 0x00, "Language" );	PORT_DIPSETTING(    0x00, "English" );	PORT_DIPSETTING(    0x40, "Japanese" );	PORT_DIPNAME( 0x80, 0x00, "Coin Type" );	PORT_DIPSETTING(    0x00, "Type 1" );	PORT_DIPSETTING(    0x80, "Type 2" );
		PORT_START(); 	/* 800007 */
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_SERVICE );
		PORT_START();  /* 800009 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 );	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_START(); 	/* 80000B */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_START(); 	/* 80000d */
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_START(); 	/* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );INPUT_PORTS_END(); }}; 
	
	static GfxLayout spritelayout1 = new GfxLayout
	(
		8,8,	/* 8*8 sprites */
		16384,	/* 16384 sprites */
		4,		/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },
	    new int[] { 8, 12, 0, 4, 24, 28, 16, 20 },
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8	/* every sprite takes 32 consecutive bytes */
	);
	
	static GfxLayout spritelayout2 = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		4096,	/* 1024 sprites */
		4,		/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },
		new int[] { 8, 12, 0, 4, 24, 28, 16, 20, 40, 44, 32, 36, 56, 60, 48, 52 },
		new int[] { 0*64, 1*64, 2*64, 3*64, 4*64, 5*64, 6*64, 7*64,
				8*64, 9*64, 10*64, 11*64, 12*64, 13*64, 14*64, 15*64 },
		128*8	/* every sprite takes 128 consecutive bytes */
	);
	
	static GfxLayout spritelayout3 = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		1024,	/* 1024 sprites */
		4,		/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },
		new int[] {
		0, 4, 0x10000*8+0 ,0x10000*8+4,
		8+0, 8+4, 0x10000*8+8+0, 0x10000*8+8+4,
		16+0, 16+4, 0x10000*8+16+0, 0x10000*8+16+4,
		24+0, 24+4, 0x10000*8+24+0, 0x10000*8+24+4
		},
		new int[] { 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32,
				8*32, 9*32, 10*32, 11*32, 12*32, 13*32, 14*32, 15*32 },
		64*8	/* every sprite takes 64 consecutive bytes */
	);
	
	static GfxDecodeInfo rainbowe_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0x000000, spritelayout1, 0, 0x80 ),	/* sprites 8x8 */
		new GfxDecodeInfo( REGION_GFX2, 0x000000, spritelayout2, 0, 0x80 ),	/* sprites 16x16 */
		new GfxDecodeInfo( REGION_GFX2, 0x080000, spritelayout3, 0, 0x80 ),	/* sprites 16x16 */
		new GfxDecodeInfo( -1 ) 										/* end of array */
	};
	
	static MachineDriver machine_driver_rainbow = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				8000000,	/* 8 MHz */
				rainbow_readmem,rainbow_writemem,null,null,
				rainbow_interrupt,1
			),
			new MachineCPU(
				CPU_Z80,
				4000000,	/* 4 MHz */
				rastan_s_readmem,rastan_s_writemem,null,null,
				ignore_interrupt,1
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		10,	/* 10 CPU slices per frame - enough for the sound CPU to read all commands */
		null,
	
		/* video hardware */
		40*8, 32*8, new rectangle( 0*8, 40*8-1, 1*8, 31*8-1 ),
		rainbowe_gfxdecodeinfo,
		2048, 2048,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		rastan_vh_start,
		rastan_vh_stop,
		rainbow_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
	  			SOUND_YM2151,
				ym2151_interface
			),
		}
	);
	
	
	/***************************************************************************
	  Jumping Specific
	***************************************************************************/
	
	static int jumping_latch = 0;
	
	public static WriteHandlerPtr jumping_sound_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (offset == 0)
		{
			jumping_latch = data & 0xff; /*M68000 writes .b to $400007*/
			/*logerror("jumping M68k write latch=%02x\n",jumping_latch);*/
			cpu_cause_interrupt(1,Z80_IRQ_INT);
		}
	} };
	
	public static ReadHandlerPtr jumping_latch_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		/*logerror("jumping Z80 reads latch=%02x\n",jumping_latch);*/
		return jumping_latch;
	} };
	
	static MemoryReadAddress jumping_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x08ffff, MRA_ROM ),
		new MemoryReadAddress( 0x10c000, 0x10ffff, MRA_BANK1 ),		/* RAM */
		new MemoryReadAddress( 0x200000, 0x20ffff, paletteram_word_r ),
		new MemoryReadAddress( 0x400000, 0x400001, input_port_0_r ),
		new MemoryReadAddress( 0x400002, 0x400003, input_port_1_r ),
		new MemoryReadAddress( 0x401000, 0x401001, input_port_2_r ),
		new MemoryReadAddress( 0x401002, 0x401003, input_port_3_r ),
		new MemoryReadAddress( 0xc00000, 0xc03fff, rastan_videoram1_r ),
		new MemoryReadAddress( 0xc04000, 0xc07fff, MRA_BANK2 ),
		new MemoryReadAddress( 0xc08000, 0xc0bfff, rastan_videoram3_r ),
		new MemoryReadAddress( 0xc0c000, 0xc0ffff, MRA_BANK3 ),
		new MemoryReadAddress( 0x440000, 0x4407ff, MRA_BANK4 ),
		new MemoryReadAddress( 0xd00000, 0xd01fff, MRA_BANK5 ), 		/* Needed for Attract Mode */
		new MemoryReadAddress( 0x420000, 0x420001, MRA_NOP),			/* Read, but result not used */
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress jumping_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x08ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x10c000, 0x10ffff, MWA_BANK1 ),
		new MemoryWriteAddress( 0x200000, 0x20ffff, paletteram_xxxxBBBBGGGGRRRR_word_w , paletteram ),
		new MemoryWriteAddress( 0xc00000, 0xc03fff, rastan_videoram1_w, rastan_videoram1, rastan_videoram_size ),
		new MemoryWriteAddress( 0xc04000, 0xc07fff, MWA_BANK2 ),
		new MemoryWriteAddress( 0xc08000, 0xc0bfff, rastan_videoram3_w, rastan_videoram3 ),
		new MemoryWriteAddress( 0xc0c000, 0xc0ffff, MWA_BANK3 ),
		new MemoryWriteAddress( 0x430000, 0x430003, rastan_scrollY_w, rastan_scrolly ), /* scroll Y  1st.w plane1  2nd.w plane2 */
		new MemoryWriteAddress( 0xc20000, 0xc20003, MWA_NOP ),			/*seems it is a leftover from rainbow, games writes scroll y here, too */
	   	new MemoryWriteAddress( 0xc40000, 0xc40003, rastan_scrollX_w, rastan_scrollx ), /* scroll X  1st.w plane1  2nd.w plane2 */
		new MemoryWriteAddress( 0x440000, 0x4407ff, MWA_BANK4, rastan_spriteram ),
		new MemoryWriteAddress( 0x400006, 0x400007, jumping_sound_w ),
		new MemoryWriteAddress( 0xd00000, 0xd01fff, MWA_BANK5 ), 			/* Needed for Attract Mode */
		new MemoryWriteAddress( 0x3c0000, 0x3c0001, MWA_NOP ),			/* Watchdog ? */
		new MemoryWriteAddress( 0x800000, 0x80ffff, MWA_NOP ),			/* Original C-Chip location (not used) */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	#if 0
	public static WriteHandlerPtr jumping_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_CPU2);
		int banknum = (data & 1);
		/*if (!(data & 8)) logerror("bankswitch not ORed with 8 !!!\n");*/
	
		/*if (banknum != 1) logerror("bank selected =%02x\n", banknum);*/
		cpu_setbank( 6, &RAM[ 0x10000 + ( banknum * 0x2000 ) ] );
	} };
	#endif
	
	static MemoryReadAddress jumping_sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
	/*new MemoryReadAddress( ????, ????, MRA_BANK6 ),*/
		new MemoryReadAddress( 0x8000, 0x8fff, MRA_RAM ),
		new MemoryReadAddress( 0xb000, 0xb000, YM2203_status_port_0_r ),
		new MemoryReadAddress( 0xb400, 0xb400, YM2203_status_port_1_r ),
		new MemoryReadAddress( 0xb800, 0xb800, jumping_latch_r ),
		new MemoryReadAddress( 0xc000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress jumping_sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0x8000, 0x8fff, MWA_RAM ),
		new MemoryWriteAddress( 0xb000, 0xb000, YM2203_control_port_0_w ),
		new MemoryWriteAddress( 0xb001, 0xb001, YM2203_write_port_0_w ),
		new MemoryWriteAddress( 0xb400, 0xb400, YM2203_control_port_1_w ),
		new MemoryWriteAddress( 0xb401, 0xb401, YM2203_write_port_1_w ),
		new MemoryWriteAddress( 0xbc00, 0xbc00, MWA_NOP ),
	/*new MemoryWriteAddress( 0xbc00, 0xbc00, jumping_bankswitch_w ),*/ /*looks like a bankswitch, but sound works with or without it*/
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static InputPortPtr input_ports_jumping = new InputPortPtr(){ public void handler() { 

		PORT_START(); 	/* DIP SWITCH A */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_6C") );
	
		PORT_START(); 	/* DIP SWITCH B */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Medium" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x04, "100k,1000k" );	PORT_DIPSETTING(    0x00, "None" );	PORT_DIPNAME( 0x08, 0x00, "Complete Bonus" );	PORT_DIPSETTING(    0x08, "1 Up" );	PORT_DIPSETTING(    0x00, "None" );	PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x10, "1" );	PORT_DIPSETTING(    0x00, "2" );	PORT_DIPSETTING(    0x30, "3" );	PORT_DIPSETTING(    0x20, "4" );	PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, "Coin Type" );	PORT_DIPSETTING(    0x00, "Type 1" );	PORT_DIPSETTING(    0x80, "Type 2" );
	    PORT_START();   /* 401001 - Coins Etc. */
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_START2 );
		PORT_START(); 	/* 401003 - Player Controls */
	  	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON1 );  	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON2 );  	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY );  	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY );INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout jumping_tilelayout = new GfxLayout
	(
		8,8,	/* 8*8 sprites */
		16384,	/* 16384 sprites */
		4,		/* 4 bits per pixel */
		new int[] { 0, 0x20000*8, 0x40000*8, 0x60000*8 },
	    new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8		/* every sprite takes 8 consecutive bytes */
	);
	
	static GfxLayout jumping_spritelayout = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		5120,	/* 5120 sprites */
		4,		/* 4 bits per pixel */
		new int[] { 0x78000*8,0x50000*8,0x28000*8,0 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8*16+0, 8*16+1, 8*16+2, 8*16+3, 8*16+4, 8*16+5, 8*16+6, 8*16+7 },
		new int[] { 0, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8, 8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		32*8	/* every sprite takes 32 consecutive bytes */
	);
	
	static GfxDecodeInfo jumping_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, jumping_tilelayout,   0, 0x80 ),	/* sprites 8x8 */
		new GfxDecodeInfo( REGION_GFX2, 0, jumping_spritelayout, 0, 0x80 ),	/* sprites 16x16 */
		new GfxDecodeInfo( -1 ) 												/* end of array */
	};
	
	
	static YM2203interface ym2203_interface = new YM2203interface
	(
		2,		/* 2 chips */
		3579545,	/* ?? MHz */
		new int[] { YM2203_VOL(30,30), YM2203_VOL(30,30) },
		new ReadHandlerPtr[] { 0 },
		new ReadHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteYmHandlerPtr[] { 0 }
	);
	
	static MachineDriver machine_driver_jumping = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				8000000,	/* 8 MHz */
				jumping_readmem,jumping_writemem,null,null,
				rainbow_interrupt,1
			),
			new MachineCPU(
				CPU_Z80,
				4000000,	/* 4 MHz */
				jumping_sound_readmem,jumping_sound_writemem,null,null,
				ignore_interrupt,1
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		10,	/* 10 CPU slices per frame - enough ? */
		null,
	
		/* video hardware */
		40*8, 32*8, new rectangle( 0*8, 40*8-1, 1*8, 31*8-1 ),
		jumping_gfxdecodeinfo,
		2048, 2048,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		rastan_vh_start,
		rastan_vh_stop,
		jumping_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203_interface
			)
		}
	);
	
	
	
	static RomLoadPtr rom_rainbow = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1 );		 /* 8*64k for 68000 code */
		ROM_LOAD_EVEN( "b22-10",     0x00000, 0x10000, 0x3b013495 );	ROM_LOAD_ODD ( "b22-11",     0x00000, 0x10000, 0x80041a3d );	ROM_LOAD_EVEN( "b22-08",     0x20000, 0x10000, 0x962fb845 );	ROM_LOAD_ODD ( "b22-09",     0x20000, 0x10000, 0xf43efa27 );	ROM_LOAD_EVEN( "ri_m03.rom", 0x40000, 0x20000, 0x3ebb0fb8 );	ROM_LOAD_ODD ( "ri_m04.rom", 0x40000, 0x20000, 0x91625e7f );
		ROM_REGION( 0x1c000, REGION_CPU2 );		 /* 64k for the audio CPU */
		ROM_LOAD( "b22-14",     	 0x00000, 0x4000, 0x113c1a5b );	ROM_CONTINUE(           	 0x10000, 0xc000 );
		ROM_REGION( 0x080000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "ri_m01.rom", 	 0x000000, 0x80000, 0xb76c9168 ); /* 8x8 gfx */
	
		ROM_REGION( 0x0a0000, REGION_GFX2 | REGIONFLAG_DISPOSE );  	ROM_LOAD( "ri_m02.rom", 	 0x000000, 0x80000, 0x1b87ecf0 ); /* sprites */
		ROM_LOAD( "b22-13",     	 0x080000, 0x10000, 0x2fda099f );	ROM_LOAD( "b22-12",     	 0x090000, 0x10000, 0x67a76dc6 );
		ROM_REGION( 0x10000, REGION_USER1 );		 /* Dump of C-Chip */
		ROM_LOAD( "jb1_f89",    	 0x0000, 0x10000, 0x0810d327 );ROM_END(); }}; 
	
	static RomLoadPtr rom_rainbowe = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1 );		   /* 8*64k for 68000 code */
		ROM_LOAD_EVEN( "ri_01.rom",    0x00000, 0x10000, 0x50690880 );	ROM_LOAD_ODD ( "ri_02.rom",    0x00000, 0x10000, 0x4dead71f );	ROM_LOAD_EVEN( "ri_03.rom",    0x20000, 0x10000, 0x4a4cb785 );	ROM_LOAD_ODD ( "ri_04.rom",    0x20000, 0x10000, 0x4caa53bd );	ROM_LOAD_EVEN( "ri_m03.rom",   0x40000, 0x20000, 0x3ebb0fb8 );	ROM_LOAD_ODD ( "ri_m04.rom",   0x40000, 0x20000, 0x91625e7f );
		ROM_REGION( 0x1c000, REGION_CPU2 );			/* 64k for the audio CPU */
		ROM_LOAD( "b22-14",      		0x00000, 0x4000, 0x113c1a5b );	ROM_CONTINUE(            		0x10000, 0xc000 );
		ROM_REGION( 0x080000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "ri_m01.rom",   	    0x000000, 0x80000, 0xb76c9168 );       /* 8x8 gfx */
	
		ROM_REGION( 0x0a0000, REGION_GFX2 | REGIONFLAG_DISPOSE );  	ROM_LOAD( "ri_m02.rom",         0x000000, 0x80000, 0x1b87ecf0 );       /* sprites */
		ROM_LOAD( "b22-13",             0x080000, 0x10000, 0x2fda099f );	ROM_LOAD( "b22-12",             0x090000, 0x10000, 0x67a76dc6 );
		/* C-Chip is missing! */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_jumping = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0xA0000, REGION_CPU1 );	/* 8*64k for code, 64k*2 for protection chip */
		ROM_LOAD_EVEN( "jb1_h4",       0x00000, 0x10000, 0x3fab6b31 );	ROM_LOAD_ODD ( "jb1_h8",       0x00000, 0x10000, 0x8c878827 );	ROM_LOAD_EVEN( "jb1_i4",       0x20000, 0x10000, 0x443492cf );	ROM_LOAD_ODD ( "jb1_i8",       0x20000, 0x10000, 0xed33bae1 );	ROM_LOAD_EVEN( "ri_m03.rom",   0x40000, 0x20000, 0x3ebb0fb8 );	ROM_LOAD_ODD ( "ri_m04.rom",   0x40000, 0x20000, 0x91625e7f );	ROM_LOAD_ODD ( "jb1_f89",      0x80000, 0x10000, 0x0810d327 );	/* Dump of C-Chip? */
	
		ROM_REGION( 0x14000, REGION_CPU2 );	/* 64k for the audio CPU */
		ROM_LOAD( "jb1_cd67",      0x00000, 0x8000, 0x8527c00e );	ROM_CONTINUE(              0x10000, 0x4000 );	ROM_CONTINUE(              0x0c000, 0x4000 );
	
		ROM_REGION( 0x080000, REGION_GFX1 | REGIONFLAG_DISPOSE );	ROM_LOAD( "jb2_ic8",           0x00000, 0x10000, 0x65b76309 );		/* 8x8 characters */
		ROM_LOAD( "jb2_ic7",           0x10000, 0x10000, 0x43a94283 );	ROM_LOAD( "jb2_ic10",          0x20000, 0x10000, 0xe61933fb );	ROM_LOAD( "jb2_ic9",           0x30000, 0x10000, 0xed031eb2 );	ROM_LOAD( "jb2_ic12",          0x40000, 0x10000, 0x312700ca );	ROM_LOAD( "jb2_ic11",          0x50000, 0x10000, 0xde3b0b88 );	ROM_LOAD( "jb2_ic14",          0x60000, 0x10000, 0x9fdc6c8e );	ROM_LOAD( "jb2_ic13",          0x70000, 0x10000, 0x06226492 );
		ROM_REGION( 0x0a0000, REGION_GFX2 | REGIONFLAG_DISPOSE );	ROM_LOAD( "jb2_ic62",          0x00000, 0x10000, 0x8548db6c );		/* 16x16 sprites */
		ROM_LOAD( "jb2_ic61",          0x10000, 0x10000, 0x37c5923b );	ROM_LOAD( "jb2_ic60",          0x20000, 0x08000, 0x662a2f1e );	ROM_LOAD( "jb2_ic78",          0x28000, 0x10000, 0x925865e1 );	ROM_LOAD( "jb2_ic77",          0x38000, 0x10000, 0xb09695d1 );	ROM_LOAD( "jb2_ic76",          0x48000, 0x08000, 0x41937743 );	ROM_LOAD( "jb2_ic93",          0x50000, 0x10000, 0xf644eeab );	ROM_LOAD( "jb2_ic92",          0x60000, 0x10000, 0x3fbccd33 );	ROM_LOAD( "jb2_ic91",          0x70000, 0x08000, 0xd886c014 );	ROM_LOAD( "jb2_i121",          0x78000, 0x10000, 0x93df1e4d );	ROM_LOAD( "jb2_i120",          0x88000, 0x10000, 0x7c4e893b );	ROM_LOAD( "jb2_i119",          0x98000, 0x08000, 0x7e1d58d8 );ROM_END(); }}; 
	
	
	
	/* sprite roms need all bits reversing, as colours are    */
	/* mapped back to front from the pattern used by Rainbow! */
	static public static InitDriverPtr init_jumping = new InitDriverPtr() { public void handler() 
	{
		/* Sprite colour map is reversed - switch to normal */
		int i;
	
		for (i = 0;i < memory_region_length(REGION_GFX2);i++)
				memory_region(REGION_GFX2)[i] ^= 0xff;
	} };
	
	
	
	public static GameDriver driver_rainbow	   = new GameDriver("1987"	,"rainbow"	,"rainbow.java"	,rom_rainbow,null	,machine_driver_rainbow	,input_ports_rainbow	,null	,ROT0	,	"Taito Corporation", "Rainbow Islands" )
	public static GameDriver driver_rainbowe	   = new GameDriver("1988"	,"rainbowe"	,"rainbow.java"	,rom_rainbowe,driver_rainbow	,machine_driver_rainbow	,input_ports_rainbow	,null	,ROT0	,	"Taito Corporation", "Rainbow Islands (Extra)", GAME_NOT_WORKING )
	public static GameDriver driver_jumping	   = new GameDriver("1989"	,"jumping"	,"rainbow.java"	,rom_jumping,driver_rainbow	,machine_driver_jumping	,input_ports_jumping	,init_jumping	,ROT0	,	"bootleg", "Jumping" )
}
