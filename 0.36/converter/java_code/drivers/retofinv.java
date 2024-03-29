/***************************************************************************

Return of the Invaders

driver by Jarek Parchanski, Andrea Mazzoleni

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;

public class retofinv
{
	
	int  retofinv_vh_start(void);
	void retofinv_vh_stop(void);
	void retofinv_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom);
	void retofinv_vh_screenrefresh(struct osd_bitmap *bitmap, int full_refresh);
	public static ReadHandlerPtr retofinv_bg_videoram_r = new ReadHandlerPtr() { public int handler(int offset);
	public static ReadHandlerPtr retofinv_fg_videoram_r = new ReadHandlerPtr() { public int handler(int offset);
	public static ReadHandlerPtr retofinv_bg_colorram_r = new ReadHandlerPtr() { public int handler(int offset);
	public static ReadHandlerPtr retofinv_fg_colorram_r = new ReadHandlerPtr() { public int handler(int offset);
	public static WriteHandlerPtr retofinv_bg_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static WriteHandlerPtr retofinv_fg_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static WriteHandlerPtr retofinv_bg_colorram_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static WriteHandlerPtr retofinv_fg_colorram_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static WriteHandlerPtr retofinv_flip_screen_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	
	
	static unsigned char cpu0_me000=0,cpu0_me800_last=0,cpu2_m6000=0;
	
	public static InitMachinePtr retofinv_init_machine = new InitMachinePtr() { public void handler() 
	{
		cpu0_me800_last = 0;
		cpu2_m6000 = 0;
	} };
	
	public static ReadHandlerPtr retofinv_shared_ram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		unsigned char *RAM = memory_region(REGION_CPU1);
		return RAM[0x8800+offset];
	} };
	
	public static WriteHandlerPtr retofinv_shared_ram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
		RAM[0x8800+offset] = data;
	} };
	
	public static WriteHandlerPtr retofinv_protection_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
	
		if (cpu0_me800_last & 0x80)
		{
			cpu0_me000 = data;
			cpu0_me800_last = 0;
			return;
		}
		else if (data < 0x10)
		{
			switch(data)
			{
				case 0x01:
					cpu0_me000 = ((cpu0_me000 >> 3) & 3) + 1;
					break;
				case 0x02:
					cpu0_me000 = cpu0_me000 & 3;
					break;
				default:
					cpu0_me000 = cpu0_me000;
			}
		}
		else if (data > 0x0f)
		{
			switch(data)
			{
				case 0x30:
					cpu0_me000 = cpu0_me000;
					break;
				case 0x40:
					cpu0_me000 = RAM[0x9800];
					break;
				case 0x41:
					cpu0_me000 = RAM[0x9801];
					break;
				case 0x42:
					cpu0_me000 = RAM[0x9802];
					break;
				default:
					cpu0_me000 = 0x3b;
					break;
			}
		}
		cpu0_me800_last = data;
	} };
	
	public static ReadHandlerPtr retofinv_protection_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return cpu0_me000;
	} };
	
	public static WriteHandlerPtr reset_cpu2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	     if (data)
		    cpu_set_reset_line(2,PULSE_LINE);
	} };
	
	public static WriteHandlerPtr reset_cpu1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	    if (data)
		    cpu_set_reset_line(1,PULSE_LINE);
	} };
	
	public static WriteHandlerPtr cpu1_halt_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		cpu_set_halt_line(1, data ? CLEAR_LINE : ASSERT_LINE);
	} };
	
	public static ReadHandlerPtr protection_2_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return 0;
	} };
	
	public static ReadHandlerPtr protection_3_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return 0x30;
	} };
	
	public static WriteHandlerPtr cpu2_m6000_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		cpu2_m6000 = data;
	} };
	
	public static ReadHandlerPtr cpu0_mf800_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return cpu2_m6000;
	} };
	
	public static WriteHandlerPtr soundcommand_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	      soundlatch_w.handler(0, data);
	      cpu_set_irq_line(2, 0, HOLD_LINE);
	} };
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x5fff, MRA_ROM ),
		new MemoryReadAddress( 0x7b00, 0x7b00, protection_2_r ),
		new MemoryReadAddress( 0x8000, 0x83ff, retofinv_fg_videoram_r ),
		new MemoryReadAddress( 0x8400, 0x87ff, retofinv_fg_colorram_r ),
		new MemoryReadAddress( 0x8800, 0x9fff, retofinv_shared_ram_r ),
		new MemoryReadAddress( 0xa000, 0xa3ff, retofinv_bg_videoram_r ),
		new MemoryReadAddress( 0xa400, 0xa7ff, retofinv_bg_colorram_r ),
		new MemoryReadAddress( 0xc800, 0xc800, MRA_NOP ),
		new MemoryReadAddress( 0xc000, 0xc000, input_port_1_r ),
		new MemoryReadAddress( 0xc001, 0xc001, input_port_2_r ),
		new MemoryReadAddress( 0xc002, 0xc002, protection_2_r ),
		new MemoryReadAddress( 0xc003, 0xc003, protection_3_r ),
		new MemoryReadAddress( 0xc004, 0xc004, input_port_0_r ),
		new MemoryReadAddress( 0xc005, 0xc005, input_port_3_r ),
		new MemoryReadAddress( 0xc006, 0xc006, input_port_5_r ),
		new MemoryReadAddress( 0xc007, 0xc007, input_port_4_r ),
		new MemoryReadAddress( 0xe000, 0xe000, retofinv_protection_r ),
		new MemoryReadAddress( 0xf800, 0xf800, cpu0_mf800_r ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x5fff, MWA_ROM ),
		new MemoryWriteAddress( 0x7fff, 0x7fff, MWA_NOP ),
		new MemoryWriteAddress( 0x8000, 0x83ff, retofinv_fg_videoram_w, retofinv_fg_videoram, retofinv_videoram_size ),
		new MemoryWriteAddress( 0x8400, 0x87ff, retofinv_fg_colorram_w, retofinv_fg_colorram ),
		new MemoryWriteAddress( 0x8800, 0x9fff, retofinv_shared_ram_w ),
		new MemoryWriteAddress( 0x8f00, 0x8f7f, MWA_RAM, retofinv_sprite_ram1 ),	/* covered by the above, */
		new MemoryWriteAddress( 0x9700, 0x977f, MWA_RAM, retofinv_sprite_ram2 ),	/* here only to */
		new MemoryWriteAddress( 0x9f00, 0x9f7f, MWA_RAM, retofinv_sprite_ram3 ),	/* initialize the pointers */
		new MemoryWriteAddress( 0xa000, 0xa3ff, retofinv_bg_videoram_w, retofinv_bg_videoram ),
		new MemoryWriteAddress( 0xa400, 0xa7ff, retofinv_bg_colorram_w, retofinv_bg_colorram ),
		new MemoryWriteAddress( 0xb800, 0xb800, retofinv_flip_screen_w ),
		new MemoryWriteAddress( 0xb801, 0xb801, MWA_RAM, retofinv_fg_char_bank ),
		new MemoryWriteAddress( 0xb802, 0xb802, MWA_RAM, retofinv_bg_char_bank ),
		new MemoryWriteAddress( 0xc800, 0xc800, MWA_NOP ),
		new MemoryWriteAddress( 0xc801, 0xc801, reset_cpu2_w ),
		new MemoryWriteAddress( 0xc802, 0xc802, reset_cpu1_w ),
		new MemoryWriteAddress( 0xc803, 0xc803, MWA_NOP ),
		new MemoryWriteAddress( 0xc804, 0xc804, MWA_NOP ),
		new MemoryWriteAddress( 0xc805, 0xc805, cpu1_halt_w ),
		new MemoryWriteAddress( 0xd800, 0xd800, soundcommand_w ),
		new MemoryWriteAddress( 0xd000, 0xd000, MWA_NOP ),
		new MemoryWriteAddress( 0xe800, 0xe800, retofinv_protection_w ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress readmem_sub[] =
	{
		new MemoryReadAddress( 0x0000, 0x1fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0x83ff, retofinv_fg_videoram_r ),
		new MemoryReadAddress( 0x8400, 0x87ff, retofinv_fg_colorram_r ),
		new MemoryReadAddress( 0x8800, 0x9fff, retofinv_shared_ram_r ),
		new MemoryReadAddress( 0xa000, 0xa3ff, retofinv_bg_videoram_r ),
		new MemoryReadAddress( 0xa400, 0xa7ff, retofinv_bg_colorram_r ),
		new MemoryReadAddress( 0xc804, 0xc804, MRA_NOP ),
		new MemoryReadAddress( 0xe000, 0xe000, retofinv_protection_r ),
		new MemoryReadAddress( 0xe800, 0xe800, MRA_NOP ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem_sub[] =
	{
		new MemoryWriteAddress( 0x0000, 0x1fff, MWA_ROM ),
		new MemoryWriteAddress( 0x8000, 0x83ff, retofinv_fg_videoram_w ),
		new MemoryWriteAddress( 0x8400, 0x87ff, retofinv_fg_colorram_w ),
		new MemoryWriteAddress( 0x8800, 0x9fff, retofinv_shared_ram_w ),
		new MemoryWriteAddress( 0xa000, 0xa3ff, retofinv_bg_videoram_w ),
		new MemoryWriteAddress( 0xa400, 0xa7ff, retofinv_bg_colorram_w ),
		new MemoryWriteAddress( 0xc804, 0xc804, MWA_NOP ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress readmem_sound[] =
	{
		new MemoryReadAddress( 0x0000, 0x1fff, MRA_ROM ),
		new MemoryReadAddress( 0x2000, 0x27ff, MRA_RAM ),
		new MemoryReadAddress( 0x4000, 0x4000, soundlatch_r ),
		new MemoryReadAddress( 0xe000, 0xe000, MRA_NOP ),  		/* Rom version ? */
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem_sound[] =
	{
		new MemoryWriteAddress( 0x0000, 0x1fff, MWA_ROM ),
		new MemoryWriteAddress( 0x2000, 0x27ff, MWA_RAM ),
		new MemoryWriteAddress( 0x6000, 0x6000, cpu2_m6000_w ),
		new MemoryWriteAddress( 0x8000, 0x8000, SN76496_0_w ),
		new MemoryWriteAddress( 0xa000, 0xa000, SN76496_1_w ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	static InputPortPtr input_ports_retofinv = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_TILT );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH,IPT_COIN1 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH,IPT_COIN2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_2WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_2WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT  | IPF_2WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
	
		PORT_START();       /* DSW1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x03, "30k, 80k & every 80k" );
		PORT_DIPSETTING(    0x02, "30k, 80k" );
		PORT_DIPSETTING(    0x01, "30k" );
		PORT_DIPSETTING(    0x00, "None" );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Free_Play") );
		PORT_DIPSETTING(    0x04, DEF_STR( "No") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x18, 0x08, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x18, "1" );
		PORT_DIPSETTING(    0x10, "2" );
		PORT_DIPSETTING(    0x08, "3" );
		PORT_DIPSETTING(    0x00, "4" );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Cocktail") );
	
		PORT_START();       /* DSW3 modified by Shingo Suzuki 1999/11/03 */
		PORT_BITX(    0x01, 0x01, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Push Start to Skip Stage", IP_KEY_NONE, IP_JOY_NONE );
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
		PORT_DIPNAME( 0x10, 0x10, "Coin Per Play Display" );
		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x20, 0x20, "Year Display" );
		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Yes") );
		PORT_BITX(    0x40, 0x40, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x80, "A and B" );
		PORT_DIPSETTING(    0x00, "A only" );
	
	        PORT_START();       /* DSW2 */
		PORT_DIPNAME( 0x0f, 0x00, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "9C_1C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "8C_1C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "7C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "6C_1C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_8C") );
		PORT_DIPNAME( 0xf0, 0x00, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "9C_1C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "8C_1C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "7C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "6C_1C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "1C_8C") );
	INPUT_PORTS_END(); }}; 
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		512,	/* 512 characters */
		1,	/* 1 bits per pixel */
		new int[] { 0 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },		/* x bit */
		new int[] { 56, 48, 40, 32, 24, 16, 8, 0 },	/* y bit */
		8*8 	/* every char takes 8 consecutive bytes */
	);
	
	static GfxLayout bglayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		512,	/* 512 characters */
		4,	/* 4 bits per pixel */
		new int[] { 0, 0x2000*8+4, 0x2000*8, 4 },
		new int[] { 8*8+3, 8*8+2, 8*8+1, 8*8+0, 3, 2, 1, 0 },
		new int[] { 7*8, 6*8, 5*8, 4*8, 3*8, 2*8, 1*8, 0*8 },
		16*8	/* every char takes 16 consecutive bytes */
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,	/* 16*16 characters */
		256,	/* 256 characters */
		4,	/* 4 bits per pixel */
		new int[] { 0, 0x4000*8+4, 0x4000*8, 4 },
		new int[] { 24*8+3, 24*8+2, 24*8+1, 24*8+0, 16*8+3, 16*8+2, 16*8+1, 16*8+0,
		  8*8+3, 8*8+2, 8*8+1, 8*8+0, 3, 2, 1, 0 },
		new int[] { 39*8, 38*8, 37*8, 36*8, 35*8, 34*8, 33*8, 32*8,
		  7*8, 6*8, 5*8, 4*8, 3*8, 2*8, 1*8, 0*8 },
	  	64*8	/* every char takes 64 consecutive bytes */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,             0, 256 ),
		new GfxDecodeInfo( REGION_GFX2, 0, bglayout,           256*2,  64 ),
		new GfxDecodeInfo( REGION_GFX3, 0, spritelayout, 64*16+256*2,  64 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static SN76496interface sn76496_interface = new SN76496interface
	(
		2,		/* 2 chips */
		new int[] { 3072000, 3072000 },	/* ??? */
		new int[] { 80, 80 }
	);
	
	
	
	static MachineDriver machine_driver_retofinv = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				3072000,
				readmem, writemem,null,null,
				interrupt,1
			),
			new MachineCPU(
				CPU_Z80,
				3072000,
				readmem_sub, writemem_sub,null,null,
				interrupt,1
			),
			new MachineCPU(
				CPU_Z80,
				3072000,
				readmem_sound, writemem_sound,null,null,
				nmi_interrupt,2
			),
		},
		60,DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		100,					/* 100 CPU slices per frame - enough for the sound CPU to read all commands */
		null,
		/* video hardware */
		36*8, 32*8,
		new rectangle( 0*8, 36*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		256, 256*2+64*16+64*16,
		retofinv_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		retofinv_vh_start,
		retofinv_vh_stop,
		retofinv_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_SN76496,
				sn76496_interface
			)
		}
	);
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_retofinv = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "ic70.rom", 0x0000, 0x2000, 0xeae7459d );
		ROM_LOAD( "ic71.rom", 0x2000, 0x2000, 0x72895e37 );
		ROM_LOAD( "ic72.rom", 0x4000, 0x2000, 0x505dd20b );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for code */
		ROM_LOAD( "ic62.rom", 0x0000, 0x2000, 0xd2899cc1 );
	
		ROM_REGION( 0x10000, REGION_CPU3 );/* 64k for sound cpu */
		ROM_LOAD( "ic17.rom", 0x0000, 0x2000, 0x9025abea );
	
		ROM_REGION( 0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ic61.rom", 0x0000, 0x2000, 0x4e3f501c );
	
		ROM_REGION( 0x04000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ic55.rom", 0x0000, 0x2000, 0xef7f8651 );
		ROM_LOAD( "ic56.rom", 0x2000, 0x2000, 0x03b40905 );
	
		ROM_REGION( 0x08000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ic8.rom",  0x0000, 0x2000, 0x6afdeec8 );
		ROM_LOAD( "ic9.rom",  0x2000, 0x2000, 0xd3dc9da3 );
		ROM_LOAD( "ic10.rom", 0x4000, 0x2000, 0xd10b2eed );
		ROM_LOAD( "ic11.rom", 0x6000, 0x2000, 0x00ca6b3d );
	
		ROM_REGION( 0x0b00, REGION_PROMS );
		ROM_LOAD( "74s287.p6", 0x0000, 0x0100, 0x50030af0 );/* palette blue bits   */
		ROM_LOAD( "74s287.o6", 0x0100, 0x0100, 0xe8f34e11 );/* palette green bits */
		ROM_LOAD( "74s287.q5", 0x0200, 0x0100, 0xe9643b8b );/* palette red bits  */
		ROM_LOAD( "82s191n",   0x0300, 0x0800, 0x93c891e3 );/* lookup table */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_retofin1 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "roi.02",  0x0000, 0x2000, 0xd98fd462 );
		ROM_LOAD( "roi.01b", 0x2000, 0x2000, 0x3379f930 );
		ROM_LOAD( "roi.01",  0x4000, 0x2000, 0x57679062 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for code */
		ROM_LOAD( "ic62.rom", 0x0000, 0x2000, 0xd2899cc1 );
	
		ROM_REGION( 0x10000, REGION_CPU3 );/* 64k for sound cpu */
		ROM_LOAD( "ic17.rom", 0x0000, 0x2000, 0x9025abea );
	
		ROM_REGION( 0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ic61.rom", 0x0000, 0x2000, 0x4e3f501c );
	
		ROM_REGION( 0x04000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ic55.rom", 0x0000, 0x2000, 0xef7f8651 );
		ROM_LOAD( "ic56.rom", 0x2000, 0x2000, 0x03b40905 );
	
		ROM_REGION( 0x08000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ic8.rom",  0x0000, 0x2000, 0x6afdeec8 );
		ROM_LOAD( "ic9.rom",  0x2000, 0x2000, 0xd3dc9da3 );
		ROM_LOAD( "ic10.rom", 0x4000, 0x2000, 0xd10b2eed );
		ROM_LOAD( "ic11.rom", 0x6000, 0x2000, 0x00ca6b3d );
	
		ROM_REGION( 0x0b00, REGION_PROMS );
		ROM_LOAD( "74s287.p6", 0x0000, 0x0100, 0x50030af0 );/* palette blue bits   */
		ROM_LOAD( "74s287.o6", 0x0100, 0x0100, 0xe8f34e11 );/* palette green bits */
		ROM_LOAD( "74s287.q5", 0x0200, 0x0100, 0xe9643b8b );/* palette red bits  */
		ROM_LOAD( "82s191n",   0x0300, 0x0800, 0x93c891e3 );/* lookup table */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_retofin2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "ri-c.1e", 0x0000, 0x2000, 0xe3c31260 );
		ROM_LOAD( "roi.01b", 0x2000, 0x2000, 0x3379f930 );
		ROM_LOAD( "ri-a.1c", 0x4000, 0x2000, 0x3ae7c530 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for code */
		ROM_LOAD( "ic62.rom", 0x0000, 0x2000, 0xd2899cc1 );
	
		ROM_REGION( 0x10000, REGION_CPU3 );/* 64k for sound cpu */
		ROM_LOAD( "ic17.rom", 0x0000, 0x2000, 0x9025abea );
	
		ROM_REGION( 0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ic61.rom", 0x0000, 0x2000, 0x4e3f501c );
	
		ROM_REGION( 0x04000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ic55.rom", 0x0000, 0x2000, 0xef7f8651 );
		ROM_LOAD( "ic56.rom", 0x2000, 0x2000, 0x03b40905 );
	
		ROM_REGION( 0x08000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ic8.rom",  0x0000, 0x2000, 0x6afdeec8 );
		ROM_LOAD( "ic9.rom",  0x2000, 0x2000, 0xd3dc9da3 );
		ROM_LOAD( "ic10.rom", 0x4000, 0x2000, 0xd10b2eed );
		ROM_LOAD( "ic11.rom", 0x6000, 0x2000, 0x00ca6b3d );
	
		ROM_REGION( 0x0b00, REGION_PROMS );
		ROM_LOAD( "74s287.p6", 0x0000, 0x0100, 0x50030af0 );/* palette blue bits   */
		ROM_LOAD( "74s287.o6", 0x0100, 0x0100, 0xe8f34e11 );/* palette green bits */
		ROM_LOAD( "74s287.q5", 0x0200, 0x0100, 0xe9643b8b );/* palette red bits  */
		ROM_LOAD( "82s191n",   0x0300, 0x0800, 0x93c891e3 );/* lookup table */
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_retofinv	   = new GameDriver("1985"	,"retofinv"	,"retofinv.java"	,rom_retofinv,null	,machine_driver_retofinv	,input_ports_retofinv	,null	,ROT270	,	"Taito Corporation", "Return of the Invaders" )
	public static GameDriver driver_retofin1	   = new GameDriver("1985"	,"retofin1"	,"retofinv.java"	,rom_retofin1,driver_retofinv	,machine_driver_retofinv	,input_ports_retofinv	,null	,ROT270	,	"bootleg", "Return of the Invaders (bootleg set 1)" )
	public static GameDriver driver_retofin2	   = new GameDriver("1985"	,"retofin2"	,"retofinv.java"	,rom_retofin2,driver_retofinv	,machine_driver_retofinv	,input_ports_retofinv	,null	,ROT270	,	"bootleg", "Return of the Invaders (bootleg set 2)" )
}
