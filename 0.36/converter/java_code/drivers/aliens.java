/***************************************************************************

Aliens (c) 1990 Konami Co. Ltd

Preliminary driver by:
	Manuel Abadia <manu@teleline.es>

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;

public class aliens
{
	
	/* prototypes */
	public static InitMachinePtr aliens_init_machine = new InitMachinePtr() { public void handler() ;
	static void aliens_banking( int lines );
	
	
	void aliens_vh_stop( void );
	int aliens_vh_start( void );
	void aliens_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh);
	
	
	static int palette_selected;
	static unsigned char *ram;
	
	
	public static InterruptPtr aliens_interrupt = new InterruptPtr() { public int handler() 
	{
		if (K051960_is_IRQ_enabled()) return interrupt();
		else return ignore_interrupt();
	} };
	
	public static ReadHandlerPtr bankedram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		if (palette_selected)
			return paletteram_r(offset);
		else
			return ram[offset];
	} };
	
	public static WriteHandlerPtr bankedram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (palette_selected)
			paletteram_xBBBBBGGGGGRRRRR_swap_w(offset,data);
		else
			ram[offset] = data;
	} };
	
	public static WriteHandlerPtr aliens_coin_counter_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* bits 0-1 = coin counters */
		coin_counter_w(0,data & 0x01);
		coin_counter_w(1,data & 0x02);
	
		/* bit 5 = select work RAM or palette */
		palette_selected = data & 0x20;
	
		/* bit 6 = enable char ROM reading through the video RAM */
		K052109_set_RMRD_line((data & 0x40) ? ASSERT_LINE : CLEAR_LINE);
	
		/* other bits unknown */
	#if 0
	{
		char baf[40];
		sprintf(baf,"%02x",data);
		usrintf_showmessage(baf);
	}
	#endif
	} };
	
	public static WriteHandlerPtr aliens_sh_irqtrigger_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		soundlatch_w.handler(offset,data);
		cpu_cause_interrupt(1,0xff);
	} };
	
	public static WriteHandlerPtr aliens_snd_bankswitch_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_SOUND1);
		/* b1: bank for chanel A */
		/* b0: bank for chanel B */
	
		int bank_A = 0x20000*((data >> 1) & 0x01);
		int bank_B = 0x20000*((data) & 0x01);
	
		K007232_bankswitch(0,RAM + bank_A,RAM + bank_B);
	} };
	
	
	static MemoryReadAddress aliens_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x03ff, bankedram_r ),			/* palette + work RAM */
		new MemoryReadAddress( 0x0400, 0x1fff, MRA_RAM ),
		new MemoryReadAddress( 0x2000, 0x3fff, MRA_BANK1 ),				/* banked ROM */
		new MemoryReadAddress( 0x5f80, 0x5f80, input_port_2_r ),			/* DIPSW #3 */
		new MemoryReadAddress( 0x5f81, 0x5f81, input_port_3_r ),			/* Player 1 inputs */
		new MemoryReadAddress( 0x5f82, 0x5f82, input_port_4_r ),			/* Player 2 inputs */
		new MemoryReadAddress( 0x5f83, 0x5f83, input_port_1_r ),			/* DIPSW #2 */
		new MemoryReadAddress( 0x5f84, 0x5f84, input_port_0_r ),			/* DIPSW #1 */
		new MemoryReadAddress( 0x5f88, 0x5f88, watchdog_reset_r ),
		new MemoryReadAddress( 0x4000, 0x7fff, K052109_051960_r ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),				/* ROM e24_j02.bin */
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress aliens_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x03ff, bankedram_w, ram ),			/* palette + work RAM */
		new MemoryWriteAddress( 0x0400, 0x1fff, MWA_RAM ),
		new MemoryWriteAddress( 0x2000, 0x3fff, MWA_ROM ),					/* banked ROM */
		new MemoryWriteAddress( 0x5f88, 0x5f88, aliens_coin_counter_w ),		/* coin counters */
		new MemoryWriteAddress( 0x5f8c, 0x5f8c, aliens_sh_irqtrigger_w ),		/* cause interrupt on audio CPU */
		new MemoryWriteAddress( 0x4000, 0x7fff, K052109_051960_w ),
		new MemoryWriteAddress( 0x8000, 0xffff, MWA_ROM ),					/* ROM e24_j02.bin */
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress aliens_readmem_sound[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),				/* ROM g04_b03.bin */
		new MemoryReadAddress( 0x8000, 0x87ff, MRA_RAM ),				/* RAM */
		new MemoryReadAddress( 0xa001, 0xa001, YM2151_status_port_0_r ),
		new MemoryReadAddress( 0xc000, 0xc000, soundlatch_r ),			/* soundlatch_r */
		new MemoryReadAddress( 0xe000, 0xe00d, K007232_read_port_0_r ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress aliens_writemem_sound[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),					/* ROM g04_b03.bin */
		new MemoryWriteAddress( 0x8000, 0x87ff, MWA_RAM ),					/* RAM */
		new MemoryWriteAddress( 0xa000, 0xa000, YM2151_register_port_0_w ),
		new MemoryWriteAddress( 0xa001, 0xa001, YM2151_data_port_0_w ),
		new MemoryWriteAddress( 0xe000, 0xe00d, K007232_write_port_0_w ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	/***************************************************************************
	
		Input Ports
	
	***************************************************************************/
	
	static InputPortPtr input_ports_aliens = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* DSW #1 */
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
	
		PORT_START(); 	/* DSW #2 */
		PORT_DIPNAME( 0x03, 0x02, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "1" );
		PORT_DIPSETTING(    0x02, "2" );
		PORT_DIPSETTING(    0x01, "3" );
		PORT_DIPSETTING(    0x00, "5" );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x60, 0x40, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(	0x60, "Easy" );
		PORT_DIPSETTING(	0x40, "Normal" );
		PORT_DIPSETTING(	0x20, "Difficult" );
		PORT_DIPSETTING(	0x00, "Very Difficult" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(	0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* DSW #3 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* PLAYER 1 INPUTS */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START(); 	/* PLAYER 2 INPUTS */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
	INPUT_PORTS_END(); }}; 
	
	
	/***************************************************************************
	
		Machine Driver
	
	***************************************************************************/
	
	static void volume_callback(int v)
	{
		K007232_set_volume(0,0,(v & 0x0f) * 0x11,0);
		K007232_set_volume(0,1,0,(v >> 4) * 0x11);
	}
	
	static struct K007232_interface k007232_interface =
	{
		1,		/* number of chips */
		{ REGION_SOUND1 },	/* memory regions */
		{ K007232_VOL(20,MIXER_PAN_CENTER,20,MIXER_PAN_CENTER) },	/* volume */
		{ volume_callback }	/* external port callback */
	};
	
	static struct YM2151interface ym2151_interface =
	{
		1, /* 1 chip */
		3579545, /* 3.579545 MHz */
		{ YM3012_VOL(60,MIXER_PAN_LEFT,60,MIXER_PAN_RIGHT) },
		{ 0 },
		{ aliens_snd_bankswitch_w }
	};
	
	static MachineDriver machine_driver_aliens = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_KONAMI,
				3000000,		/* ? */
				aliens_readmem,aliens_writemem,null,null,
	            aliens_interrupt,1
	        ),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				3579545,		/* ? */
				aliens_readmem_sound, aliens_writemem_sound,null,null,
				ignore_interrupt,0	/* interrupts are triggered by the main CPU */
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		aliens_init_machine,
	
		/* video hardware */
		64*8, 32*8, new rectangle( 14*8, (64-14)*8-1, 2*8, 30*8-1 ),
		0,	/* gfx decoded by konamiic.c */
		512, 512,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		aliens_vh_start,
		aliens_vh_stop,
		aliens_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			),
			new MachineSound(
				SOUND_K007232,
				k007232_interface
			)
		}
	);
	
	
	/***************************************************************************
	
	  Game ROMs
	
	***************************************************************************/
	
	static RomLoadPtr rom_aliens = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x38000, REGION_CPU1 );/* code + banked roms */
		ROM_LOAD( "e24_j02.bin", 0x10000, 0x08000, 0x56c20971 );
		ROM_CONTINUE(            0x08000, 0x08000 );
		ROM_LOAD( "c24_j01.bin", 0x18000, 0x20000, 0x6a529cd6 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the sound CPU */
		ROM_LOAD( "g04_b03.bin", 0x00000, 0x08000, 0x1ac4d283 );
	
		ROM_REGION( 0x200000, REGION_GFX1 );/* graphics */
		ROM_LOAD( "k13_b11.bin", 0x000000, 0x80000, 0x89c5c885 );/* characters (set 1) */
		ROM_LOAD( "j13_b07.bin", 0x080000, 0x40000, 0xe9c56d66 );/* characters (set 2) */
		/* second half empty */
		ROM_LOAD( "k19_b12.bin", 0x100000, 0x80000, 0xea6bdc17 );/* characters (set 1) */
		ROM_LOAD( "j19_b08.bin", 0x180000, 0x40000, 0xf9387966 );/* characters (set 2) */
		/* second half empty */
	
		ROM_REGION( 0x200000, REGION_GFX2 );/* graphics */
		ROM_LOAD( "k08_b10.bin", 0x000000, 0x80000, 0x0b1035b1 );/* sprites (set 1) */
		ROM_LOAD( "j08_b06.bin", 0x080000, 0x40000, 0x081a0566 );/* sprites (set 2) */
		/* second half empty */
		ROM_LOAD( "k02_b09.bin", 0x100000, 0x80000, 0xe76b3c19 );/* sprites (set 1) */
		ROM_LOAD( "j02_b05.bin", 0x180000, 0x40000, 0x19a261f2 );/* sprites (set 2) */
		/* second half empty */
	
		ROM_REGION( 0x0100, REGION_PROMS );
		ROM_LOAD( "821a08.h14",  0x0000, 0x0100, 0x7da55800 );/* priority encoder (not used) */
	
		ROM_REGION( 0x40000, REGION_SOUND1 );/* samples for 007232 */
		ROM_LOAD( "875b04.bin",  0x00000, 0x40000, 0x4e209ac8 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_aliens2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x38000, REGION_CPU1 );/* code + banked roms */
		ROM_LOAD( "e24_p02.bin", 0x10000, 0x08000, 0x4edd707d );
		ROM_CONTINUE(            0x08000, 0x08000 );
		ROM_LOAD( "c24_n01.bin", 0x18000, 0x20000, 0x106cf59c );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the sound CPU */
		ROM_LOAD( "g04_b03.bin", 0x00000, 0x08000, 0x1ac4d283 );
	
		ROM_REGION( 0x200000, REGION_GFX1 );/* graphics */
		ROM_LOAD( "k13_b11.bin", 0x000000, 0x80000, 0x89c5c885 );/* characters (set 1) */
		ROM_LOAD( "j13_b07.bin", 0x080000, 0x40000, 0xe9c56d66 );/* characters (set 2) */
		/* second half empty */
		ROM_LOAD( "k19_b12.bin", 0x100000, 0x80000, 0xea6bdc17 );/* characters (set 1) */
		ROM_LOAD( "j19_b08.bin", 0x180000, 0x40000, 0xf9387966 );/* characters (set 2) */
		/* second half empty */
	
		ROM_REGION( 0x200000, REGION_GFX2 );/* graphics */
		ROM_LOAD( "k08_b10.bin", 0x000000, 0x80000, 0x0b1035b1 );/* sprites (set 1) */
		ROM_LOAD( "j08_b06.bin", 0x080000, 0x40000, 0x081a0566 );/* sprites (set 2) */
		/* second half empty */
		ROM_LOAD( "k02_b09.bin", 0x100000, 0x80000, 0xe76b3c19 );/* sprites (set 1) */
		ROM_LOAD( "j02_b05.bin", 0x180000, 0x40000, 0x19a261f2 );/* sprites (set 2) */
		/* second half empty */
	
		ROM_REGION( 0x0100, REGION_PROMS );
		ROM_LOAD( "821a08.h14",  0x0000, 0x0100, 0x7da55800 );/* priority encoder (not used) */
	
		ROM_REGION( 0x40000, REGION_SOUND1 );/* samples for 007232 */
		ROM_LOAD( "875b04.bin",  0x00000, 0x40000, 0x4e209ac8 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_aliensu = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x38000, REGION_CPU1 );/* code + banked roms */
		ROM_LOAD( "e24_n02.bin", 0x10000, 0x08000, 0x24dd612e );
		ROM_CONTINUE(            0x08000, 0x08000 );
		ROM_LOAD( "c24_n01.bin", 0x18000, 0x20000, 0x106cf59c );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the sound CPU */
		ROM_LOAD( "g04_b03.bin", 0x00000, 0x08000, 0x1ac4d283 );
	
		ROM_REGION( 0x200000, REGION_GFX1 );/* graphics */
		ROM_LOAD( "k13_b11.bin", 0x000000, 0x80000, 0x89c5c885 );/* characters (set 1) */
		ROM_LOAD( "j13_b07.bin", 0x080000, 0x40000, 0xe9c56d66 );/* characters (set 2) */
		/* second half empty */
		ROM_LOAD( "k19_b12.bin", 0x100000, 0x80000, 0xea6bdc17 );/* characters (set 1) */
		ROM_LOAD( "j19_b08.bin", 0x180000, 0x40000, 0xf9387966 );/* characters (set 2) */
		/* second half empty */
	
		ROM_REGION( 0x200000, REGION_GFX2 );/* graphics */
		ROM_LOAD( "k08_b10.bin", 0x000000, 0x80000, 0x0b1035b1 );/* sprites (set 1) */
		ROM_LOAD( "j08_b06.bin", 0x080000, 0x40000, 0x081a0566 );/* sprites (set 2) */
		/* second half empty */
		ROM_LOAD( "k02_b09.bin", 0x100000, 0x80000, 0xe76b3c19 );/* sprites (set 1) */
		ROM_LOAD( "j02_b05.bin", 0x180000, 0x40000, 0x19a261f2 );/* sprites (set 2) */
		/* second half empty */
	
		ROM_REGION( 0x0100, REGION_PROMS );
		ROM_LOAD( "821a08.h14",  0x0000, 0x0100, 0x7da55800 );/* priority encoder (not used) */
	
		ROM_REGION( 0x40000, REGION_SOUND1 );/* samples for 007232 */
		ROM_LOAD( "875b04.bin",  0x00000, 0x40000, 0x4e209ac8 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_aliensj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x38000, REGION_CPU1 );/* code + banked roms */
		ROM_LOAD( "875m02.e24",  0x10000, 0x08000, 0x54a774e5 );
		ROM_CONTINUE(            0x08000, 0x08000 );
		ROM_LOAD( "875m01.c24",  0x18000, 0x20000, 0x1663d3dc );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the sound CPU */
		ROM_LOAD( "875k03.g4",   0x00000, 0x08000, 0xbd86264d );
	
		ROM_REGION( 0x200000, REGION_GFX1 );/* graphics */
		ROM_LOAD( "k13_b11.bin", 0x000000, 0x80000, 0x89c5c885 );/* characters (set 1) */
		ROM_LOAD( "j13_b07.bin", 0x080000, 0x40000, 0xe9c56d66 );/* characters (set 2) */
		/* second half empty */
		ROM_LOAD( "k19_b12.bin", 0x100000, 0x80000, 0xea6bdc17 );/* characters (set 1) */
		ROM_LOAD( "j19_b08.bin", 0x180000, 0x40000, 0xf9387966 );/* characters (set 2) */
		/* second half empty */
	
		ROM_REGION( 0x200000, REGION_GFX2 );/* graphics */
		ROM_LOAD( "k08_b10.bin", 0x000000, 0x80000, 0x0b1035b1 );/* sprites (set 1) */
		ROM_LOAD( "j08_b06.bin", 0x080000, 0x40000, 0x081a0566 );/* sprites (set 2) */
		/* second half empty */
		ROM_LOAD( "k02_b09.bin", 0x100000, 0x80000, 0xe76b3c19 );/* sprites (set 1) */
		ROM_LOAD( "j02_b05.bin", 0x180000, 0x40000, 0x19a261f2 );/* sprites (set 2) */
		/* second half empty */
	
		ROM_REGION( 0x0100, REGION_PROMS );
		ROM_LOAD( "821a08.h14",  0x0000, 0x0100, 0x7da55800 );/* priority encoder (not used) */
	
		ROM_REGION( 0x40000, REGION_SOUND1 );/* samples for 007232 */
		ROM_LOAD( "875b04.bin",  0x00000, 0x40000, 0x4e209ac8 );
	ROM_END(); }}; 
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static void aliens_banking( int lines )
	{
		unsigned char *RAM = memory_region(REGION_CPU1);
		int offs = 0x18000;
	
	
		if (lines & 0x10) offs -= 0x8000;
	
		offs += (lines & 0x0f)*0x2000;
		cpu_setbank( 1, &RAM[offs] );
	}
	
	public static InitMachinePtr aliens_init_machine = new InitMachinePtr() { public void handler() 
	{
		unsigned char *RAM = memory_region(REGION_CPU1);
	
		konami_cpu_setlines_callback = aliens_banking;
	
		/* init the default bank */
		cpu_setbank( 1, &RAM[0x10000] );
	} };
	
	
	
	public static InitDriverPtr init_aliens = new InitDriverPtr() { public void handler() 
	{
		konami_rom_deinterleave_2(REGION_GFX1);
		konami_rom_deinterleave_2(REGION_GFX2);
	} };
	
	
	
	public static GameDriver driver_aliens	   = new GameDriver("1990"	,"aliens"	,"aliens.java"	,rom_aliens,null	,machine_driver_aliens	,input_ports_aliens	,init_aliens	,ROT0	,	"Konami", "Aliens (World set 1)" )
	public static GameDriver driver_aliens2	   = new GameDriver("1990"	,"aliens2"	,"aliens.java"	,rom_aliens2,driver_aliens	,machine_driver_aliens	,input_ports_aliens	,init_aliens	,ROT0	,	"Konami", "Aliens (World set 2)" )
	public static GameDriver driver_aliensu	   = new GameDriver("1990"	,"aliensu"	,"aliens.java"	,rom_aliensu,driver_aliens	,machine_driver_aliens	,input_ports_aliens	,init_aliens	,ROT0	,	"Konami", "Aliens (US)" )
	public static GameDriver driver_aliensj	   = new GameDriver("1990"	,"aliensj"	,"aliens.java"	,rom_aliensj,driver_aliens	,machine_driver_aliens	,input_ports_aliens	,init_aliens	,ROT0	,	"Konami", "Aliens (Japan)" )
}
