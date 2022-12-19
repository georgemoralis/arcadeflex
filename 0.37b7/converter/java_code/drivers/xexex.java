/***************************************************************************

Xexex

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class xexex
{
	
	
	static int cur_back_select, cur_back_ctrla;
	static int cur_control2;
	
	static int init_eeprom_count;
	
	static EEPROM_interface eeprom_interface = new EEPROM_interface
	(
		7,				/* address bits */
		8,				/* data bits */
		"011000",		/*  read command */
		"011100",		/* write command */
		"0100100000000",/* erase command */
		"0100000000000",/* lock command */
		"0100110000000" /* unlock command */
	);
	
	public static nvramPtr nvram_handler  = new nvramPtr() { public void handler(Object file, int read_or_write) 
	{
		if (read_or_write != 0)
			EEPROM_save(file);
		else
		{
			EEPROM_init(&eeprom_interface);
	
			if (file != 0)
			{
				init_eeprom_count = 0;
				EEPROM_load(file);
			}
			else
				init_eeprom_count = 10;
		}
	} };
	
	public static WriteHandlerPtr K053251_halfword_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000) == 0)
			K053251_w(offset >> 1,data & 0xff);
	} };
	
	/* the interface with the 053247 is weird. The chip can address only 0x1000 bytes */
	/* of RAM, but they put 0x8000 there. The CPU can access them all. Address lines */
	/* A1, A5 and A6 don't go to the 053247. */
	public static ReadHandlerPtr K053247_scattered_word_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		if ((offset & 0x0062) != 0)
			return READ_WORD(&spriteram.read(offset));
		else
		{
			offset = ((offset & 0x001c) >> 1) | ((offset & 0x7f80) >> 3);
			return K053247_word_r(offset);
		}
	} };
	
	public static WriteHandlerPtr K053247_scattered_word_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((offset & 0x0062) != 0)
			COMBINE_WORD_MEM(&spriteram.read(offset),data);
		else
		{
			offset = ((offset & 0x001c) >> 1) | ((offset & 0x7f80) >> 3);
	//if ((offset&0xf) == 0)
	//	logerror("%04x: write %02x to spriteram %04x\n",cpu_get_pc(),data,offset);
			K053247_word_w(offset,data);
		}
	} };
	
	public static ReadHandlerPtr control0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return input_port_0_r.handler(0);
	} };
	
	public static ReadHandlerPtr control1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int res;
	
		/* bit 0 is EEPROM data */
		/* bit 1 is EEPROM ready */
		/* bit 3 is service button */
		res = EEPROM_read_bit() | input_port_1_r.handler(0);
	
		if (init_eeprom_count != 0)
		{
			init_eeprom_count--;
			res &= 0xf7;
		}
	
		return res;
	} };
	
	public static ReadHandlerPtr control2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return cur_control2;
	} };
	
	public static WriteHandlerPtr control2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* bit 0  is data */
		/* bit 1  is cs (active low) */
		/* bit 2  is clock (active high) */
		/* bit 5  is enable irq 6 */
		/* bit 6  is enable irq 5 */
		/* bit 11 is watchdog */
	
		EEPROM_write_bit(data & 0x01);
		EEPROM_set_cs_line((data & 0x02) ? CLEAR_LINE : ASSERT_LINE);
		EEPROM_set_clock_line((data & 0x04) ? ASSERT_LINE : CLEAR_LINE);
		cur_control2 = data;
	
		/* bit 8 = enable sprite ROM reading */
		K053246_set_OBJCHA_line((data & 0x0100) ? ASSERT_LINE : CLEAR_LINE);
	} };
	
	public static InterruptPtr xexex_interrupt = new InterruptPtr() { public int handler() 
	{
		switch (cpu_getiloops())
		{
			case 0:
				if (K053246_is_IRQ_enabled())
					return 4;
				break;
	
			case 1:
				if (K053246_is_IRQ_enabled() && (cur_control2 & 0x0040))
					return 5;
				break;
	
			case 2:
				if (K053246_is_IRQ_enabled() && (cur_control2 & 0x0020))
					return 6;
				break;
		}
		return ignore_interrupt();
	} };
	
	static int sound_status = 0, sound_cmd = 0;
	
	public static WriteHandlerPtr sound_cmd_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		logerror("Sound command : %d\n", data & 0xff);
		sound_cmd = data & 0xff;
		/*	cpu_set_irq_line(1, 0, HOLD_LINE); */
		if(sound_cmd == 0xfe)
		  sound_status = 0x7f;
	} };
	
	public static WriteHandlerPtr sound_status_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		logerror("Sound status = %d\n", data);
		sound_status = data;
	} };
	
	public static ReadHandlerPtr sound_cmd_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		logerror("Sound CPU read command %d\n", sound_cmd & 0xff);
		cpu_set_irq_line(1, 0, CLEAR_LINE);
		return sound_cmd;
	} };
	
	public static ReadHandlerPtr sound_status_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return sound_status;
	} };
	
	public static WriteHandlerPtr sound_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		cpu_setbank(3, memory_region(REGION_CPU2) + 0x10000 + (data&7)*0x2000);
	} };
	
	public static ReadHandlerPtr back_ctrla_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return cur_back_ctrla;
	} };
	
	public static WriteHandlerPtr back_ctrla_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		data &= 0xff;
		if(data != cur_back_ctrla) {
			logerror("Back: ctrla = %02x (%08x)\n", data, cpu_get_pc());
			cur_back_ctrla = data;
		}
	} };
	
	public static ReadHandlerPtr back_select_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return cur_back_select;
	} };
	
	public static WriteHandlerPtr back_select_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		data &= 0xff;
		if(data != cur_back_select) {
			logerror("Back: select = %02x (%08x)\n", data, cpu_get_pc());
			cur_back_select = data;
		}
	} };
	
	public static ReadHandlerPtr backrom_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		if (!(cur_back_ctrla & 1))
			logerror("Back: Reading rom memory with enable=0\n");
		return *(memory_region(REGION_GFX3) + 2048*cur_back_select + (offset>>2));
	} };
	
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x080000, 0x08ffff, MRA_BANK1 ),
		new MemoryReadAddress( 0x090000, 0x097fff, K053247_scattered_word_r ),
		new MemoryReadAddress( 0x0c0000, 0x0c003f, K054157_r ),
		new MemoryReadAddress( 0x0c4000, 0x0c4001, K053246_word_r ),
		new MemoryReadAddress( 0x0c6000, 0x0c6fff, MRA_BANK4 ),			/* Effects? */
		new MemoryReadAddress( 0x0c800a, 0x0c800b, back_ctrla_r ),
		new MemoryReadAddress( 0x0c800e, 0x0c800f, back_select_r ),
		new MemoryReadAddress( 0x0d6014, 0x0d6015, sound_status_r ),
		new MemoryReadAddress( 0x0da000, 0x0da001, input_port_2_r ),
		new MemoryReadAddress( 0x0da002, 0x0da003, input_port_3_r ),
		new MemoryReadAddress( 0x0dc000, 0x0dc001, control0_r ),
		new MemoryReadAddress( 0x0dc002, 0x0dc003, control1_r ),
		new MemoryReadAddress( 0x0de000, 0x0de001, control2_r ),
		new MemoryReadAddress( 0x100000, 0x17ffff, MRA_ROM ),
		new MemoryReadAddress( 0x180000, 0x181fff, K054157_ram_word_r ),
		new MemoryReadAddress( 0x190000, 0x191fff, MRA_BANK6 ), 			/* Passthrough to tile roms */
		new MemoryReadAddress( 0x1a0000, 0x1a1fff, backrom_r ),
		new MemoryReadAddress( 0x1b0000, 0x1b1fff, xexex_palette_r ),
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x080000, 0x08ffff, MWA_BANK1 ),	/* Work RAM */
		new MemoryWriteAddress( 0x090000, 0x097fff, K053247_scattered_word_w, spriteram ),
		new MemoryWriteAddress( 0x0c0000, 0x0c003f, K054157_w ),
		new MemoryWriteAddress( 0x0c2000, 0x0c2007, K053246_word_w ),
		new MemoryWriteAddress( 0x0c6000, 0x0c6fff, MWA_BANK4 ),
		new MemoryWriteAddress( 0x0c800a, 0x0c800b, back_ctrla_w ),
		new MemoryWriteAddress( 0x0c800e, 0x0c800f, back_select_w ),
		new MemoryWriteAddress( 0x0cc000, 0x0cc01f, K053251_halfword_w ),
		new MemoryWriteAddress( 0x0d600c, 0x0d600d, sound_cmd_w ),
		new MemoryWriteAddress( 0x0de000, 0x0de001, control2_w ),
		new MemoryWriteAddress( 0x100000, 0x17ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x180000, 0x181fff, K054157_ram_word_w ),
		new MemoryWriteAddress( 0x1b0000, 0x1b1fff, xexex_palette_w, paletteram ),
		new MemoryWriteAddress( -1 )
	};
	
	#if 0
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0xbfff, MRA_BANK3 ),
		new MemoryReadAddress( 0xc000, 0xdf7f, MRA_RAM ),
		new MemoryReadAddress( 0xe000, 0xe22f, MRA_RAM ),
		new MemoryReadAddress( 0xec01, 0xec01, YM2151_status_port_0_r ),
		new MemoryReadAddress( 0xf002, 0xf002, sound_cmd_r ),
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new MemoryWriteAddress( 0xc000, 0xdf7f, MWA_RAM ),
		new MemoryWriteAddress( 0xe000, 0xe22f, MWA_RAM ),
		new MemoryWriteAddress( 0xec00, 0xec00, YM2151_register_port_0_w ),
		new MemoryWriteAddress( 0xec01, 0xec01, YM2151_data_port_0_w ),
		new MemoryWriteAddress( 0xf000, 0xf000, sound_status_w ),
		new MemoryWriteAddress( 0xf800, 0xf800, sound_bankswitch_w ),
		new MemoryWriteAddress( -1 )
	};
	#endif
	
	
	
	static InputPortPtr input_ports_xexex = new InputPortPtr(){ public void handler() { 
	PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_SERVICE1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_SERVICE2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_SPECIAL );/* EEPROM data */
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_SPECIAL );/* EEPROM ready (always 1) */
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN );	PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
		PORT_BIT( 0xf0, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );INPUT_PORTS_END(); }}; 
	
	
	
	static MachineDriver machine_driver_xexex = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				16000000,	/* 16 MHz ? (xtal is 32MHz) */
				readmem, writemem, null, null,
				xexex_interrupt, 3	/* ??? */
			),
	#if 0
			new MachineCPU(
				CPU_Z80,
				2000000,	/* 2 MHz ? (xtal is 32MHz/19.432Mhz) */
				sound_readmem, sound_writemem, null, null,
				ignore_interrupt, 1
			),
	#endif
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		64*8, 32*8, new rectangle( 8*8, (64-8)*8-1, 0*8, 32*8-1 ),
		null,	/* gfx decoded by konamiic.c */
		2048, 2048,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		xexex_vh_start,
		xexex_vh_stop,
		xexex_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(0)
		},
	
		nvram_handler
	);
	
	
	static RomLoadPtr rom_xexex = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x180000, REGION_CPU1 );	ROM_LOAD_EVEN( "xex_a01.rom",  0x000000, 0x40000, 0x3ebcb066 );	ROM_LOAD_ODD ( "xex_a02.rom",  0x000000, 0x40000, 0x36ea7a48 );	ROM_LOAD_EVEN( "xex_b03.rom",  0x100000, 0x40000, 0x97833086 );	ROM_LOAD_ODD ( "xex_b04.rom",  0x100000, 0x40000, 0x26ec5dc8 );
		ROM_REGION( 0x30000, REGION_CPU2 );	ROM_LOAD( "xex_a05.rom", 0x000000, 0x020000, 0x0e33d6ec );	ROM_RELOAD(              0x010000, 0x020000 );
		ROM_REGION( 0x200000, REGION_GFX1 );	ROM_LOAD( "xex_b14.rom", 0x000000, 0x100000, 0x02a44bfa );	ROM_LOAD( "xex_b13.rom", 0x100000, 0x100000, 0x633c8eb5 );
		ROM_REGION( 0x400000, REGION_GFX2 );	ROM_LOAD( "xex_b12.rom", 0x000000, 0x100000, 0x08d611b0 );	ROM_LOAD( "xex_b11.rom", 0x100000, 0x100000, 0xa26f7507 );	ROM_LOAD( "xex_b10.rom", 0x200000, 0x100000, 0xee31db8d );	ROM_LOAD( "xex_b09.rom", 0x300000, 0x100000, 0x88f072ef );
		ROM_REGION( 0x80000, REGION_GFX3 );	ROM_LOAD( "xex_b08.rom", 0x000000, 0x080000, 0xca816b7b );ROM_END(); }}; 
	
	static RomLoadPtr rom_xexexj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x180000, REGION_CPU1 );	ROM_LOAD_EVEN( "067jaa01.16d", 0x000000, 0x40000, 0x06e99784 );	ROM_LOAD_ODD ( "067jaa02.16e", 0x000000, 0x40000, 0x30ae5bc4 );	ROM_LOAD_EVEN( "xex_b03.rom",  0x100000, 0x40000, 0x97833086 );	ROM_LOAD_ODD ( "xex_b04.rom",  0x100000, 0x40000, 0x26ec5dc8 );
		ROM_REGION( 0x30000, REGION_CPU2 );	ROM_LOAD( "067jaa05.4e", 0x000000, 0x020000, 0x2f4dd0a8 );	ROM_RELOAD(              0x010000, 0x020000 );
		ROM_REGION( 0x200000, REGION_GFX1 );	ROM_LOAD( "xex_b14.rom", 0x000000, 0x100000, 0x02a44bfa );	ROM_LOAD( "xex_b13.rom", 0x100000, 0x100000, 0x633c8eb5 );
		ROM_REGION( 0x400000, REGION_GFX2 );	ROM_LOAD( "xex_b12.rom", 0x000000, 0x100000, 0x08d611b0 );	ROM_LOAD( "xex_b11.rom", 0x100000, 0x100000, 0xa26f7507 );	ROM_LOAD( "xex_b10.rom", 0x200000, 0x100000, 0xee31db8d );	ROM_LOAD( "xex_b09.rom", 0x300000, 0x100000, 0x88f072ef );
		ROM_REGION( 0x80000, REGION_GFX3 );	ROM_LOAD( "xex_b08.rom", 0x000000, 0x080000, 0xca816b7b );ROM_END(); }}; 
	
	
	
	static public static InitDriverPtr init_xexex = new InitDriverPtr() { public void handler() 
	{
		konami_rom_deinterleave_2(REGION_GFX1);
		konami_rom_deinterleave_4(REGION_GFX2);
	} };
	
	
	public static GameDriver driver_xexex	   = new GameDriver("1991"	,"xexex"	,"xexex.java"	,rom_xexex,null	,machine_driver_xexex	,input_ports_xexex	,init_xexex	,ROT0	,	"Konami", "Xexex (World)", GAME_NOT_WORKING | GAME_NO_SOUND )
	public static GameDriver driver_xexexj	   = new GameDriver("1991"	,"xexexj"	,"xexex.java"	,rom_xexexj,driver_xexex	,machine_driver_xexex	,input_ports_xexex	,init_xexex	,ROT0	,	"Konami", "Xexex (Japan)", GAME_NOT_WORKING | GAME_NO_SOUND )
}
