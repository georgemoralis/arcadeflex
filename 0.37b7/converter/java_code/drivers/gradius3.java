/***************************************************************************

Gradius 3 (GX945) (c) 1989 Konami

driver by Nicola Salmoria

This board uses the well known 052109 051962 custom gfx chips, however unlike
all other games they fetch gfx data from RAM. The gfx ROMs are memory mapped
on cpu B and the needed parts are copied to RAM at run time.
To handle this efficiently in MAME, some changes would be required to the
tilemap system and to vidhrdw/konamiic.c. For the time being, I'm kludging
my way in.
There's also something wrong in the way tile banks are implemented in
konamiic.c. They don't seem to be used by this game.

The visible area is dubious. It looks like it is supposed to be asymmetrical,
I've set it that way however this will break cocktail flip (since it expects
a symmetrical visible area).

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class gradius3
{
	
	
	extern UBytePtr gradius3_gfxram;
	extern int gradius3_priority;
	
	
	
	public static ReadHandlerPtr K052109_halfword_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return K052109_r(offset >> 1);
	} };
	
	public static WriteHandlerPtr K052109_halfword_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
			K052109_w(offset >> 1,data & 0xff);
	
		/* is this a bug in the game or something else? */
		if ((data & 0x00ff0000) == 0x00ff0000)
			K052109_w(offset >> 1,(data >> 8) & 0xff);
	//		logerror("%06x half %04x = %04x\n",cpu_get_pc(),offset,data);
	} };
	
	public static ReadHandlerPtr K051937_halfword_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return K051937_r(offset >> 1);
	} };
	
	public static WriteHandlerPtr K051937_halfword_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
			K051937_w(offset >> 1,data & 0xff);
	} };
	
	public static ReadHandlerPtr K051960_halfword_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return K051960_r(offset >> 1);
	} };
	
	public static WriteHandlerPtr K051960_halfword_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
			K051960_w(offset >> 1,data & 0xff);
	} };
	
	
	
	static int irqAen,irqBmask;
	
	
	static void gradius3_init(void)
	{
		/* start with cpu B halted */
		cpu_set_reset_line(1,ASSERT_LINE);
		irqAen = 0;
		irqBmask = 0;
	}
	
	static UBytePtr sharedram;
	
	public static ReadHandlerPtr sharedram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return READ_WORD(&sharedram[offset]);
	} };
	
	public static WriteHandlerPtr sharedram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		COMBINE_WORD_MEM(&sharedram[offset],data);
	} };
	
	public static WriteHandlerPtr cpuA_ctrl_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0xff000000) == 0)
		{
			data >>= 8;
	
			/* bits 0-1 are coin counters */
			coin_counter_w.handler(0,data & 0x01);
			coin_counter_w.handler(1,data & 0x02);
	
			/* bit 2 selects layer priority */
			gradius3_priority = data & 0x04;
	
			/* bit 3 enables cpu B */
			cpu_set_reset_line(1,(data & 0x08) ? CLEAR_LINE : ASSERT_LINE);
	
			/* bit 5 enables irq */
			irqAen = data & 0x20;
	
			/* other bits unknown */
	//logerror("%06x: write %04x to c0000\n",cpu_get_pc(),data);
		}
	} };
	
	public static WriteHandlerPtr cpuB_irqenable_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0xff000000) == 0)
			irqBmask = (data >> 8) & 0x07;
	} };
	
	public static InterruptPtr cpuA_interrupt = new InterruptPtr() { public int handler() 
	{
		if (irqAen != 0) return 2;
		return 0;
	} };
	
	public static InterruptPtr cpuB_interrupt = new InterruptPtr() { public int handler() 
	{
		if (cpu_getiloops() & 1)	/* ??? */
		{
			if ((irqBmask & 2) != 0) return 2;
		}
		else
		{
			if ((irqBmask & 1) != 0) return 1;
		}
		return 0;
	} };
	
	public static WriteHandlerPtr cpuB_irqtrigger_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((irqBmask & 4) != 0)
		{
	logerror("%04x trigger cpu B irq 4 %02x\n",cpu_get_pc(),data);
			cpu_cause_interrupt(1,4);
		}
		else
	logerror("%04x MISSED cpu B irq 4 %02x\n",cpu_get_pc(),data);
	} };
	
	public static WriteHandlerPtr sound_command_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0xff000000) == 0)
			soundlatch_w.handler(0,(data >> 8) & 0xff);
	} };
	
	public static WriteHandlerPtr sound_irq_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		cpu_cause_interrupt(2,0xff);
	} };
	
	public static WriteHandlerPtr sound_bank_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_SOUND1);
		int bank_A, bank_B;
	
		/* banks # for the 007232 (chip 1) */
		bank_A = 0x20000 * ((data >> 0) & 0x03);
		bank_B = 0x20000 * ((data >> 2) & 0x03);
		K007232_bankswitch(0,RAM + bank_A,RAM + bank_B);
	} };
	
	
	
	static MemoryReadAddress gradius3_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
		new MemoryReadAddress( 0x040000, 0x043fff, MRA_BANK1 ),	/* main RAM */
		new MemoryReadAddress( 0x080000, 0x080fff, paletteram_word_r ),
		new MemoryReadAddress( 0x0c8000, 0x0c8001, input_port_0_r ),
		new MemoryReadAddress( 0x0c8002, 0x0c8003, input_port_1_r ),
		new MemoryReadAddress( 0x0c8004, 0x0c8005, input_port_2_r ),
		new MemoryReadAddress( 0x0c8006, 0x0c8007, input_port_5_r ),
		new MemoryReadAddress( 0x0d0000, 0x0d0001, input_port_3_r ),
		new MemoryReadAddress( 0x0d0002, 0x0d0003, input_port_4_r ),
		new MemoryReadAddress( 0x100000, 0x103fff, sharedram_r ),
		new MemoryReadAddress( 0x14c000, 0x153fff, K052109_halfword_r ),
		new MemoryReadAddress( 0x180000, 0x19ffff, gradius3_gfxram_r ),
	#if 0
		new MemoryReadAddress( 0x140000, 0x140007, K051937_word_r ),
		new MemoryReadAddress( 0x140400, 0x1407ff, K051960_word_r ),
	#endif
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress gradius3_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x040000, 0x043fff, MWA_BANK1 ),	/* main RAM */
		new MemoryWriteAddress( 0x080000, 0x080fff, paletteram_xRRRRRGGGGGBBBBB_word_w, paletteram ),
		new MemoryWriteAddress( 0x0c0000, 0x0c0001, cpuA_ctrl_w ),	/* halt cpu B, irq enable, priority, coin counters, other? */
		new MemoryWriteAddress( 0x0d8000, 0x0d8001, cpuB_irqtrigger_w ),
		new MemoryWriteAddress( 0x0e0000, 0x0e0001, watchdog_reset_w ),
		new MemoryWriteAddress( 0x0e8000, 0x0e8001, sound_command_w ),
		new MemoryWriteAddress( 0x0f0000, 0x0f0001, sound_irq_w ),
		new MemoryWriteAddress( 0x100000, 0x103fff, sharedram_w, sharedram ),
		new MemoryWriteAddress( 0x14c000, 0x153fff, K052109_halfword_w ),
		new MemoryWriteAddress( 0x180000, 0x19ffff, gradius3_gfxram_w, gradius3_gfxram ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	static MemoryReadAddress gradius3_readmem2[] =
	{
		new MemoryReadAddress( 0x000000, 0x0fffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x103fff, MRA_BANK2 ),	/* main RAM */
		new MemoryReadAddress( 0x200000, 0x203fff, sharedram_r ),
		new MemoryReadAddress( 0x24c000, 0x253fff, K052109_halfword_r ),
		new MemoryReadAddress( 0x280000, 0x29ffff, gradius3_gfxram_r ),
		new MemoryReadAddress( 0x2c0000, 0x2c000f, K051937_halfword_r ),
		new MemoryReadAddress( 0x2c0800, 0x2c0fff, K051960_halfword_r ),
		new MemoryReadAddress( 0x400000, 0x5fffff, gradius3_gfxrom_r ),		/* gfx ROMs are mapped here, and copied to RAM */
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress gradius3_writemem2[] =
	{
		new MemoryWriteAddress( 0x000000, 0x0fffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x103fff, MWA_BANK2 ),	/* main RAM */
		new MemoryWriteAddress( 0x140000, 0x140001, cpuB_irqenable_w ),
		new MemoryWriteAddress( 0x200000, 0x203fff, sharedram_w ),
		new MemoryWriteAddress( 0x24c000, 0x253fff, K052109_halfword_w ),
		new MemoryWriteAddress( 0x280000, 0x29ffff, gradius3_gfxram_w ),
		new MemoryWriteAddress( 0x2c0000, 0x2c000f, K051937_halfword_w ),
		new MemoryWriteAddress( 0x2c0800, 0x2c0fff, K051960_halfword_w ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	static MemoryReadAddress gradius3_s_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0xefff, MRA_ROM ),
		new MemoryReadAddress( 0xf010, 0xf010, soundlatch_r ),
		new MemoryReadAddress( 0xf020, 0xf02d, K007232_read_port_0_r ),
		new MemoryReadAddress( 0xf031, 0xf031, YM2151_status_port_0_r ),
		new MemoryReadAddress( 0xf800, 0xffff, MRA_RAM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress gradius3_s_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xefff, MWA_ROM ),
		new MemoryWriteAddress( 0xf000, 0xf000, sound_bank_w ),				/* 007232 bankswitch */
		new MemoryWriteAddress( 0xf020, 0xf02d, K007232_write_port_0_w ),
		new MemoryWriteAddress( 0xf030, 0xf030, YM2151_register_port_0_w ),
		new MemoryWriteAddress( 0xf031, 0xf031, YM2151_data_port_0_w ),
		new MemoryWriteAddress( 0xf800, 0xffff, MWA_RAM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	
	static InputPortPtr input_ports_gradius3 = new InputPortPtr(){ public void handler() { 
	PORT_START();       /* COINS */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN3 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();       /* PLAYER 1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON3 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START();       /* PLAYER 2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_COCKTAIL | IPF_8WAY );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_COCKTAIL | IPF_8WAY );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_COCKTAIL | IPF_8WAY );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_COCKTAIL | IPF_8WAY );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 	/* DSW1 */
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
	//	PORT_DIPSETTING(    0x00, "Invalid" );
		PORT_START(); 	/* DSW2 */
		PORT_DIPNAME( 0x03, 0x02, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "2" );	PORT_DIPSETTING(    0x02, "3" );	PORT_DIPSETTING(    0x01, "5" );	PORT_DIPSETTING(    0x00, "7" );	PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x18, "20000 and every 70000" );	PORT_DIPSETTING(    0x10, "100000 and every 100000" );	PORT_DIPSETTING(    0x08, "50000" );	PORT_DIPSETTING(    0x00, "100000" );	PORT_DIPNAME( 0x60, 0x60, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x60, "Easy" );	PORT_DIPSETTING(    0x40, "Normal" );	PORT_DIPSETTING(    0x20, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* DSW3 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "Upright Controls" );	PORT_DIPSETTING(    0x02, "Single" );	PORT_DIPSETTING(    0x00, "Dual" );	PORT_SERVICE( 0x04, IP_ACTIVE_LOW );	PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );INPUT_PORTS_END(); }}; 
	
	
	
	static YM2151interface ym2151_interface = new YM2151interface
	(
		1,			/* 1 chip */
		3579545,	/* 3.579545 MHz */
		new int[] { YM3012_VOL(100,MIXER_PAN_LEFT,100,MIXER_PAN_RIGHT) },
		new WriteYmHandlerPtr[] { 0 }
	);
	
	static void volume_callback(int v)
	{
		K007232_set_volume(0,0,(v >> 4) * 0x11,0);
		K007232_set_volume(0,1,0,(v & 0x0f) * 0x11);
	}
	
	static struct K007232_interface k007232_interface =
	{
		1,		/* number of chips */
		{ REGION_SOUND1 },	/* memory regions */
		{ K007232_VOL(20,MIXER_PAN_CENTER,20,MIXER_PAN_CENTER) },	/* volume */
		{ volume_callback }	/* external port callback */
	};
	
	
	
	static MachineDriver machine_driver_gradius3 = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				10000000,	/* 10 MHz */
				gradius3_readmem,gradius3_writemem,null,null,
				cpuA_interrupt,1
			),
			new MachineCPU(
				CPU_M68000,
				10000000,	/* 10 MHz */
				gradius3_readmem2,gradius3_writemem2,null,null,
				cpuB_interrupt,2	/* has three interrupt vectors, 1 2 and 4 */
									/* 4 is triggered by cpu A, the others are unknown but */
									/* required for the game to run. */
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				3579545,	/* 3.579545 MHz */
				gradius3_s_readmem,gradius3_s_writemem,null,null,
				ignore_interrupt,0	/* IRQs are triggered by the main CPU */
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		100, /* CPU slices */
		gradius3_init,
	
		/* video hardware */
		64*8, 32*8, new rectangle( 12*8, (64-14)*8-1, 2*8, 30*8-1 ),	/* asymmetrical! */
		null,	/* gfx decoded by konamiic.c */
		2048, 2048,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		gradius3_vh_start,
		gradius3_vh_stop,
		gradius3_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			),
			new MachineSound(
				SOUND_K007232,
				k007232_interface,
			)
		}
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_gradius3 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );	ROM_LOAD_EVEN( "945s13.f15",   0x00000, 0x20000, 0x70c240a2 );	ROM_LOAD_ODD ( "945s12.e15",   0x00000, 0x20000, 0xbbc300d4 );
		ROM_REGION( 0x100000, REGION_CPU2 );	ROM_LOAD_EVEN( "g3_r17.rom",   0x000000, 0x20000, 0xb4a6df25 );	ROM_LOAD_ODD ( "g3_n17.rom",   0x000000, 0x20000, 0x74e981d2 );	ROM_LOAD_EVEN( "g3_r11.rom",   0x040000, 0x20000, 0x83772304 );	ROM_LOAD_ODD ( "g3_n11.rom",   0x040000, 0x20000, 0xe1fd75b6 );	ROM_LOAD_EVEN( "g3_r15.rom",   0x080000, 0x20000, 0xc1e399b6 );	ROM_LOAD_ODD ( "g3_n15.rom",   0x080000, 0x20000, 0x96222d04 );	ROM_LOAD_EVEN( "g3_r13.rom",   0x0c0000, 0x20000, 0x4c16d4bd );	ROM_LOAD_ODD ( "g3_n13.rom",   0x0c0000, 0x20000, 0x5e209d01 );
		ROM_REGION( 0x10000, REGION_CPU3 );/* 64k for the audio CPU */
		ROM_LOAD( "g3_d9.rom",    0x00000, 0x10000, 0xc8c45365 );
		ROM_REGION( 0x20000, REGION_GFX1 );/* fake */
		/* gfx data is dynamically generated in RAM */
	
		ROM_REGION( 0x200000, REGION_GFX2 );/* graphics (addressable by the main CPU) */
		ROM_LOAD( "945a02.l3",             0x000000, 0x80000, 0x4dfffd74 );	ROM_LOAD_GFX_EVEN( "g3_k6.rom",    0x080000, 0x20000, 0x884e21ee )
		ROM_LOAD_GFX_ODD ( "g3_m6.rom",    0x080000, 0x20000, 0x45bcd921 )
		ROM_LOAD_GFX_EVEN( "g3_k8.rom",    0x0c0000, 0x20000, 0x843bc67d )
		ROM_LOAD_GFX_ODD ( "g3_m8.rom",    0x0c0000, 0x20000, 0x0a98d08e )
		ROM_LOAD( "945a01.h3",             0x100000, 0x80000, 0x339d6dd2 );	ROM_LOAD_GFX_EVEN( "g3_e6.rom",    0x180000, 0x20000, 0xa67ef087 )
		ROM_LOAD_GFX_ODD ( "g3_h6.rom",    0x180000, 0x20000, 0xa56be17a )
		ROM_LOAD_GFX_EVEN( "g3_e8.rom",    0x1c0000, 0x20000, 0x933e68b9 )
		ROM_LOAD_GFX_ODD ( "g3_h8.rom",    0x1c0000, 0x20000, 0xf375e87b )
	
		ROM_REGION( 0x0100, REGION_PROMS );	ROM_LOAD( "945l14.j28",  0x0000, 0x0100, 0xc778c189 );/* priority encoder (not used) */
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* 007232 samples */
		ROM_LOAD( "945a10.bin",   0x00000, 0x40000, 0x1d083e10 );	ROM_LOAD( "g3_c18.rom",   0x40000, 0x20000, 0x6043f4eb );	ROM_LOAD( "g3_c20.rom",   0x60000, 0x20000, 0x89ea3baf );ROM_END(); }}; 
	
	static RomLoadPtr rom_grdius3a = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );	ROM_LOAD_EVEN( "g3_f15.rom",   0x00000, 0x20000, 0x9974fe6b );	ROM_LOAD_ODD ( "g3_e15.rom",   0x00000, 0x20000, 0xe9771b91 );
		ROM_REGION( 0x100000, REGION_CPU2 );	ROM_LOAD_EVEN( "g3_r17.rom",   0x000000, 0x20000, 0xb4a6df25 );	ROM_LOAD_ODD ( "g3_n17.rom",   0x000000, 0x20000, 0x74e981d2 );	ROM_LOAD_EVEN( "g3_r11.rom",   0x040000, 0x20000, 0x83772304 );	ROM_LOAD_ODD ( "g3_n11.rom",   0x040000, 0x20000, 0xe1fd75b6 );	ROM_LOAD_EVEN( "g3_r15.rom",   0x080000, 0x20000, 0xc1e399b6 );	ROM_LOAD_ODD ( "g3_n15.rom",   0x080000, 0x20000, 0x96222d04 );	ROM_LOAD_EVEN( "g3_r13.rom",   0x0c0000, 0x20000, 0x4c16d4bd );	ROM_LOAD_ODD ( "g3_n13.rom",   0x0c0000, 0x20000, 0x5e209d01 );
		ROM_REGION( 0x10000, REGION_CPU3 );/* 64k for the audio CPU */
		ROM_LOAD( "g3_d9.rom",    0x00000, 0x10000, 0xc8c45365 );
		ROM_REGION( 0x20000, REGION_GFX1 );/* fake */
		/* gfx data is dynamically generated in RAM */
	
		ROM_REGION( 0x200000, REGION_GFX2 );/* graphics (addressable by the main CPU) */
		ROM_LOAD( "945a02.l3",             0x000000, 0x80000, 0x4dfffd74 );	ROM_LOAD_GFX_EVEN( "g3_k6.rom",    0x080000, 0x20000, 0x884e21ee )
		ROM_LOAD_GFX_ODD ( "g3_m6.rom",    0x080000, 0x20000, 0x45bcd921 )
		ROM_LOAD_GFX_EVEN( "g3_k8.rom",    0x0c0000, 0x20000, 0x843bc67d )
		ROM_LOAD_GFX_ODD ( "g3_m8.rom",    0x0c0000, 0x20000, 0x0a98d08e )
		ROM_LOAD( "945a01.h3",             0x100000, 0x80000, 0x339d6dd2 );	ROM_LOAD_GFX_EVEN( "g3_e6.rom",    0x180000, 0x20000, 0xa67ef087 )
		ROM_LOAD_GFX_ODD ( "g3_h6.rom",    0x180000, 0x20000, 0xa56be17a )
		ROM_LOAD_GFX_EVEN( "g3_e8.rom",    0x1c0000, 0x20000, 0x933e68b9 )
		ROM_LOAD_GFX_ODD ( "g3_h8.rom",    0x1c0000, 0x20000, 0xf375e87b )
	
		ROM_REGION( 0x0100, REGION_PROMS );	ROM_LOAD( "945l14.j28",  0x0000, 0x0100, 0xc778c189 );/* priority encoder (not used) */
	
		ROM_REGION( 0x80000, REGION_SOUND1 );/* 007232 samples */
		ROM_LOAD( "945a10.bin",   0x00000, 0x40000, 0x1d083e10 );	ROM_LOAD( "g3_c18.rom",   0x40000, 0x20000, 0x6043f4eb );	ROM_LOAD( "g3_c20.rom",   0x60000, 0x20000, 0x89ea3baf );ROM_END(); }}; 
	
	
	
	static public static InitDriverPtr init_gradius3 = new InitDriverPtr() { public void handler() 
	{
		konami_rom_deinterleave_2(REGION_GFX2);
	} };
	
	
	
	public static GameDriver driver_gradius3	   = new GameDriver("1989"	,"gradius3"	,"gradius3.java"	,rom_gradius3,null	,machine_driver_gradius3	,input_ports_gradius3	,init_gradius3	,ROT0	,	"Konami", "Gradius III (Japan)" )
	public static GameDriver driver_grdius3a	   = new GameDriver("1989"	,"grdius3a"	,"gradius3.java"	,rom_grdius3a,driver_gradius3	,machine_driver_gradius3	,input_ports_gradius3	,init_gradius3	,ROT0	,	"Konami", "Gradius III (Asia)" )
	
}
