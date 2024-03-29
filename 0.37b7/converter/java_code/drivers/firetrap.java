/***************************************************************************

Fire Trap memory map (preliminary)

driver by Nicola Salmoria

Z80:
0000-7fff ROM
8000-bfff Banked ROM (4 banks)
c000-cfff RAM
d000-d7ff bg #1 video/color RAM (alternating pages 0x100 long)
d000-dfff bg #2 video/color RAM (alternating pages 0x100 long)
e000-e3ff fg video RAM
e400-e7ff fg color RAM
e800-e97f sprites RAM

memory mapped ports:
read:
f010      IN0
f011      IN1
f012      IN2
f013      DSW0
f014      DSW1
f015      from pin 10 of 8751 controller
f016      from port #1 of 8751 controller

write:
f000      IRQ acknowledge
f001      sound command (also causes NMI on sound CPU)
f002      ROM bank selection
f003      flip screen
f004      NMI disable
f005      to port #2 of 8751 controller (signal on P3.2)
f008-f009 bg #1 x scroll
f00a-f00b bg #1 y scroll
f00c-f00d bg #2 x scroll
f00e-f00f bg #2 y scroll

interrupts:
VBlank triggers NMI.
the 8751 triggers IRQ

6502:
0000-07ff RAM
4000-7fff Banked ROM (2 banks)
8000-ffff ROM

read:
3400      command from the main cpu

write:
1000-1001 YM3526
2000      ADPCM data for the MSM5205 chip
2400      bit 0 = to sound chip MSM5205 (1 = play sample); bit 1 = IRQ enable
2800      ROM bank select


8751:
Who knows, it's protected. The bootleg doesn't have it.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class firetrap
{
	
	
	
	extern UBytePtr firetrap_bg1videoram;
	extern UBytePtr firetrap_bg2videoram;
	extern UBytePtr firetrap_fgvideoram;
	
	
	
	static int firetrap_irq_enable = 0;
	
	public static WriteHandlerPtr firetrap_nmi_disable_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		interrupt_enable_w(offset,~data & 1);
	} };
	
	public static WriteHandlerPtr firetrap_bankselect_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int bankaddress;
		UBytePtr RAM = memory_region(REGION_CPU1);
	
	
		bankaddress = 0x10000 + (data & 0x03) * 0x4000;
		cpu_setbank(1,&RAM[bankaddress]);
	} };
	
	public static ReadHandlerPtr firetrap_8751_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	//logerror("PC:%04x read from 8751\n",cpu_get_pc());
	
		/* Check for coin insertion */
		/* the following only works in the bootleg version, which doesn't have an */
		/* 8751 - the real thing is much more complicated than that. */
		if ((readinputport(2) & 0x70) != 0x70) return 0xff;
		else return 0;
	} };
	
	public static WriteHandlerPtr firetrap_8751_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	logerror("PC:%04x write %02x to 8751\n",cpu_get_pc(),data);
		cpu_cause_interrupt(0,0xff);
	} };
	
	public static WriteHandlerPtr firetrap_sound_command_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		soundlatch_w.handler(offset,data);
		cpu_cause_interrupt(1,M6502_INT_NMI);
	} };
	
	public static WriteHandlerPtr firetrap_sound_2400_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		MSM5205_reset_w(offset,~data & 0x01);
		firetrap_irq_enable = data & 0x02;
	} };
	
	public static WriteHandlerPtr firetrap_sound_bankselect_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int bankaddress;
		UBytePtr RAM = memory_region(REGION_CPU2);
	
	
		bankaddress = 0x10000 + (data & 0x01) * 0x4000;
		cpu_setbank(2,&RAM[bankaddress]);
	} };
	
	static int msm5205next;
	
	public static vclk_interruptPtr firetrap_adpcm_int = new vclk_interruptPtr() { public void handler(int data) 
	{
		static int toggle=0;
	
		MSM5205_data_w (0,msm5205next>>4);
		msm5205next<<=4;
	
		toggle ^= 1;
		if (firetrap_irq_enable && toggle)
			cpu_cause_interrupt (1, M6502_INT_IRQ);
	} };
	
	public static WriteHandlerPtr firetrap_adpcm_data_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		msm5205next = data;
	} };
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0xbfff, MRA_BANK1 ),
		new MemoryReadAddress( 0xc000, 0xe97f, MRA_RAM ),
		new MemoryReadAddress( 0xf010, 0xf010, input_port_0_r ),
		new MemoryReadAddress( 0xf011, 0xf011, input_port_1_r ),
		new MemoryReadAddress( 0xf012, 0xf012, input_port_2_r ),
		new MemoryReadAddress( 0xf013, 0xf013, input_port_3_r ),
		new MemoryReadAddress( 0xf014, 0xf014, input_port_4_r ),
		new MemoryReadAddress( 0xf016, 0xf016, firetrap_8751_r ),
		new MemoryReadAddress( 0xf800, 0xf8ff, MRA_ROM ),	/* extra ROM in the bootleg with unprotection code */
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new MemoryWriteAddress( 0xc000, 0xcfff, MWA_RAM ),
		new MemoryWriteAddress( 0xd000, 0xd7ff, firetrap_bg1videoram_w, firetrap_bg1videoram ),
		new MemoryWriteAddress( 0xd800, 0xdfff, firetrap_bg2videoram_w, firetrap_bg2videoram ),
		new MemoryWriteAddress( 0xe000, 0xe7ff, firetrap_fgvideoram_w,  firetrap_fgvideoram ),
		new MemoryWriteAddress( 0xe800, 0xe97f, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0xf000, 0xf000, MWA_NOP ),	/* IRQ acknowledge */
		new MemoryWriteAddress( 0xf001, 0xf001, firetrap_sound_command_w ),
		new MemoryWriteAddress( 0xf002, 0xf002, firetrap_bankselect_w ),
		new MemoryWriteAddress( 0xf003, 0xf003, flip_screen_w ),
		new MemoryWriteAddress( 0xf004, 0xf004, firetrap_nmi_disable_w ),
	//	new MemoryWriteAddress( 0xf005, 0xf005, firetrap_8751_w ),
		new MemoryWriteAddress( 0xf008, 0xf009, firetrap_bg1_scrollx_w ),
		new MemoryWriteAddress( 0xf00a, 0xf00b, firetrap_bg1_scrolly_w ),
		new MemoryWriteAddress( 0xf00c, 0xf00d, firetrap_bg2_scrollx_w ),
		new MemoryWriteAddress( 0xf00e, 0xf00f, firetrap_bg2_scrolly_w ),
		new MemoryWriteAddress( 0xf800, 0xf8ff, MWA_ROM ),	/* extra ROM in the bootleg with unprotection code */
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x07ff, MRA_RAM ),
		new MemoryReadAddress( 0x3400, 0x3400, soundlatch_r ),
		new MemoryReadAddress( 0x4000, 0x7fff, MRA_BANK2 ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x07ff, MWA_RAM ),
		new MemoryWriteAddress( 0x1000, 0x1000, YM3526_control_port_0_w ),
		new MemoryWriteAddress( 0x1001, 0x1001, YM3526_write_port_0_w ),
		new MemoryWriteAddress( 0x2000, 0x2000, firetrap_adpcm_data_w ),	/* ADPCM data for the MSM5205 chip */
		new MemoryWriteAddress( 0x2400, 0x2400, firetrap_sound_2400_w ),
		new MemoryWriteAddress( 0x2800, 0x2800, firetrap_sound_bankselect_w ),
		new MemoryWriteAddress( 0x4000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	
	static InputPortPtr input_ports_firetrap = new InputPortPtr(){ public void handler() { 
	PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_UP | IPF_4WAY );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_DOWN | IPF_4WAY );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_LEFT | IPF_4WAY );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_RIGHT | IPF_4WAY );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_UP | IPF_4WAY );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_DOWN | IPF_4WAY );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_LEFT | IPF_4WAY );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_RIGHT | IPF_4WAY );
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_UP | IPF_4WAY | IPF_COCKTAIL );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_DOWN | IPF_4WAY | IPF_COCKTAIL );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_LEFT | IPF_4WAY | IPF_COCKTAIL );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_RIGHT | IPF_4WAY | IPF_COCKTAIL );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_UP | IPF_4WAY | IPF_COCKTAIL );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_DOWN | IPF_4WAY | IPF_COCKTAIL );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_LEFT | IPF_4WAY | IPF_COCKTAIL );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_RIGHT | IPF_4WAY | IPF_COCKTAIL );
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN3 );/* bootleg only */
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_COIN1 );/* bootleg only */
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN2 );/* bootleg only */
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_VBLANK );
		PORT_START();       /* DSW0 */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
	//	PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
	//	PORT_DIPSETTING(    0x01, DEF_STR( "1C_1C") );
	//	PORT_DIPSETTING(    0x02, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* DSW1 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x02, "Easy" );	PORT_DIPSETTING(    0x03, "Normal" );	PORT_DIPSETTING(    0x01, "Hard" );	PORT_DIPSETTING(    0x00, "Hardest" );	PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "1" );	PORT_DIPSETTING(    0x0c, "3" );	PORT_DIPSETTING(    0x08, "4" );	PORT_DIPSETTING(    0x04, "5" );	PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x30, "50000 70000" );	PORT_DIPSETTING(    0x20, "60000 80000" );	PORT_DIPSETTING(    0x10, "80000 100000" );	PORT_DIPSETTING(    0x00, "50000" );	PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );	PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,2),
		2,
		new int[] { 0, 4 },
		new int[] { 3, 2, 1, 0, RGN_FRAC(1,2)+3, RGN_FRAC(1,2)+2, RGN_FRAC(1,2)+1, RGN_FRAC(1,2)+0 },
		new int[] { 7*8, 6*8, 5*8, 4*8, 3*8, 2*8, 1*8, 0*8 },
		8*8
	);
	static GfxLayout tilelayout = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,4),
		4,
		new int[] { 0, 4, RGN_FRAC(1,2)+0, RGN_FRAC(1,2)+4 },
		new int[] { 3, 2, 1, 0, RGN_FRAC(1,4)+3, RGN_FRAC(1,4)+2, RGN_FRAC(1,4)+1, RGN_FRAC(1,4)+0,
				16*8+3, 16*8+2, 16*8+1, 16*8+0, RGN_FRAC(1,4)+16*8+3, RGN_FRAC(1,4)+16*8+2, RGN_FRAC(1,4)+16*8+1, RGN_FRAC(1,4)+16*8+0 },
		new int[] { 15*8, 14*8, 13*8, 12*8, 11*8, 10*8, 9*8, 8*8,
				7*8, 6*8, 5*8, 4*8, 3*8, 2*8, 1*8, 0*8 },
		32*8
	);
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,4),
		4,
		new int[] { RGN_FRAC(0,4), RGN_FRAC(1,4), RGN_FRAC(2,4), RGN_FRAC(3,4) },
		new int[] { 7, 6, 5, 4, 3, 2, 1, 0,
				16*8+7, 16*8+6, 16*8+5, 16*8+4, 16*8+3, 16*8+2, 16*8+1, 16*8+0 },
		new int[] { 15*8, 14*8, 13*8, 12*8, 11*8, 10*8, 9*8, 8*8,
				7*8, 6*8, 5*8, 4*8, 3*8, 2*8, 1*8, 0*8 },
		32*8
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,   0x00, 16 ),	/* colors 0x00-0x3f */
		new GfxDecodeInfo( REGION_GFX2, 0, tilelayout,   0x80,  4 ),	/* colors 0x80-0xbf */
		new GfxDecodeInfo( REGION_GFX3, 0, tilelayout,   0xc0,  4 ),	/* colors 0xc0-0xff */
		new GfxDecodeInfo( REGION_GFX4, 0, spritelayout, 0x40,  4 ),	/* colors 0x40-0x7f */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static YM3526interface ym3526_interface = new YM3526interface
	(
		1,			/* 1 chip (no more supported) */
		3600000,	/* 3.600000 MHz ? */
		new int[] { 100 }		/* volume */
	);
	
	static MSM5205interface msm5205_interface = new MSM5205interface
	(
		1,					/* 1 chip             */
		384000,				/* 384KHz ?           */
		new vclk_interruptPtr[] { firetrap_adpcm_int },/* interrupt function */
		new int[] { MSM5205_S48_4B},	/* 8KHz ?             */
		new int[] { 60 }
	);
	
	
	
	static MachineDriver machine_driver_firetrap = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				6000000,	/* 6 MHz */
				readmem,writemem,null,null,
				nmi_interrupt,1
			),
			new MachineCPU(
				CPU_M6502 | CPU_AUDIO_CPU,
				3072000/2,	/* 1.536 MHz? */
				sound_readmem,sound_writemem,null,null,
				ignore_interrupt,0
								/* IRQs are caused by the ADPCM chip */
								/* NMIs are caused by the main CPU */
			),
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 1*8, 31*8-1 ),
		gfxdecodeinfo,
		256,256,
		firetrap_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		firetrap_vh_start,
		null,
		firetrap_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM3526,
				ym3526_interface
			),
			new MachineSound(
				SOUND_MSM5205,
				msm5205_interface
			)
		}
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_firetrap = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000, REGION_CPU1 );/* 64k for code + 64k for banked ROMs */
		ROM_LOAD( "di02.bin",     0x00000, 0x8000, 0x3d1e4bf7 );	ROM_LOAD( "di01.bin",     0x10000, 0x8000, 0x9bbae38b );	ROM_LOAD( "di00.bin",     0x18000, 0x8000, 0xd0dad7de );
		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the sound CPU + 32k for banked ROMs */
		ROM_LOAD( "di17.bin",     0x08000, 0x8000, 0x8605f6b9 );	ROM_LOAD( "di18.bin",     0x10000, 0x8000, 0x49508c93 );
		/* there's also a protected 8751 microcontroller with ROM onboard */
	
		ROM_REGION( 0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* characters */
		ROM_LOAD( "di03.bin",     0x00000, 0x2000, 0x46721930 );
		ROM_REGION( 0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* tiles */
		ROM_LOAD( "di06.bin",     0x00000, 0x2000, 0x441d9154 );	ROM_CONTINUE(             0x08000, 0x2000 );	ROM_CONTINUE(             0x02000, 0x2000 );	ROM_CONTINUE(             0x0a000, 0x2000 );	ROM_LOAD( "di04.bin",     0x04000, 0x2000, 0x8e6e7eec );	ROM_CONTINUE(             0x0c000, 0x2000 );	ROM_CONTINUE(             0x06000, 0x2000 );	ROM_CONTINUE(             0x0e000, 0x2000 );	ROM_LOAD( "di07.bin",     0x10000, 0x2000, 0xef0a7e23 );	ROM_CONTINUE(             0x18000, 0x2000 );	ROM_CONTINUE(             0x12000, 0x2000 );	ROM_CONTINUE(             0x1a000, 0x2000 );	ROM_LOAD( "di05.bin",     0x14000, 0x2000, 0xec080082 );	ROM_CONTINUE(             0x1c000, 0x2000 );	ROM_CONTINUE(             0x16000, 0x2000 );	ROM_CONTINUE(             0x1e000, 0x2000 );
		ROM_REGION( 0x20000, REGION_GFX3 | REGIONFLAG_DISPOSE );	ROM_LOAD( "di09.bin",     0x00000, 0x2000, 0xd11e28e8 );	ROM_CONTINUE(             0x08000, 0x2000 );	ROM_CONTINUE(             0x02000, 0x2000 );	ROM_CONTINUE(             0x0a000, 0x2000 );	ROM_LOAD( "di08.bin",     0x04000, 0x2000, 0xc32a21d8 );	ROM_CONTINUE(             0x0c000, 0x2000 );	ROM_CONTINUE(             0x06000, 0x2000 );	ROM_CONTINUE(             0x0e000, 0x2000 );	ROM_LOAD( "di11.bin",     0x10000, 0x2000, 0x6424d5c3 );	ROM_CONTINUE(             0x18000, 0x2000 );	ROM_CONTINUE(             0x12000, 0x2000 );	ROM_CONTINUE(             0x1a000, 0x2000 );	ROM_LOAD( "di10.bin",     0x14000, 0x2000, 0x9b89300a );	ROM_CONTINUE(             0x1c000, 0x2000 );	ROM_CONTINUE(             0x16000, 0x2000 );	ROM_CONTINUE(             0x1e000, 0x2000 );
		ROM_REGION( 0x20000, REGION_GFX4 | REGIONFLAG_DISPOSE );/* sprites */
		ROM_LOAD( "di16.bin",     0x00000, 0x8000, 0x0de055d7 );	ROM_LOAD( "di13.bin",     0x08000, 0x8000, 0x869219da );	ROM_LOAD( "di14.bin",     0x10000, 0x8000, 0x6b65812e );	ROM_LOAD( "di15.bin",     0x18000, 0x8000, 0x3e27f77d );
		ROM_REGION( 0x0200, REGION_PROMS );	ROM_LOAD( "firetrap.3b",  0x0000,  0x0100, 0x8bb45337 );/* palette red and green component */
		ROM_LOAD( "firetrap.4b",  0x0100,  0x0100, 0xd5abfc64 );/* palette blue component */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_firetpbl = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x28000, REGION_CPU1 );/* 64k for code + 96k for banked ROMs */
		ROM_LOAD( "ft0d.bin",     0x00000, 0x8000, 0x793ef849 );	ROM_LOAD( "ft0a.bin",     0x08000, 0x8000, 0x613313ee );/* unprotection code */
		ROM_LOAD( "ft0c.bin",     0x10000, 0x8000, 0x5c8a0562 );	ROM_LOAD( "ft0b.bin",     0x18000, 0x8000, 0xf2412fe8 );
		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the sound CPU + 32k for banked ROMs */
		ROM_LOAD( "di17.bin",     0x08000, 0x8000, 0x8605f6b9 );	ROM_LOAD( "di18.bin",     0x10000, 0x8000, 0x49508c93 );
		ROM_REGION( 0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE );/* characters */
		ROM_LOAD( "ft0e.bin",     0x00000, 0x2000, 0xa584fc16 );
		ROM_REGION( 0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE );/* tiles */
		ROM_LOAD( "di06.bin",     0x00000, 0x2000, 0x441d9154 );	ROM_CONTINUE(             0x08000, 0x2000 );	ROM_CONTINUE(             0x02000, 0x2000 );	ROM_CONTINUE(             0x0a000, 0x2000 );	ROM_LOAD( "di04.bin",     0x04000, 0x2000, 0x8e6e7eec );	ROM_CONTINUE(             0x0c000, 0x2000 );	ROM_CONTINUE(             0x06000, 0x2000 );	ROM_CONTINUE(             0x0e000, 0x2000 );	ROM_LOAD( "di07.bin",     0x10000, 0x2000, 0xef0a7e23 );	ROM_CONTINUE(             0x18000, 0x2000 );	ROM_CONTINUE(             0x12000, 0x2000 );	ROM_CONTINUE(             0x1a000, 0x2000 );	ROM_LOAD( "di05.bin",     0x14000, 0x2000, 0xec080082 );	ROM_CONTINUE(             0x1c000, 0x2000 );	ROM_CONTINUE(             0x16000, 0x2000 );	ROM_CONTINUE(             0x1e000, 0x2000 );
		ROM_REGION( 0x20000, REGION_GFX3 | REGIONFLAG_DISPOSE );	ROM_LOAD( "di09.bin",     0x00000, 0x2000, 0xd11e28e8 );	ROM_CONTINUE(             0x08000, 0x2000 );	ROM_CONTINUE(             0x02000, 0x2000 );	ROM_CONTINUE(             0x0a000, 0x2000 );	ROM_LOAD( "di08.bin",     0x04000, 0x2000, 0xc32a21d8 );	ROM_CONTINUE(             0x0c000, 0x2000 );	ROM_CONTINUE(             0x06000, 0x2000 );	ROM_CONTINUE(             0x0e000, 0x2000 );	ROM_LOAD( "di11.bin",     0x10000, 0x2000, 0x6424d5c3 );	ROM_CONTINUE(             0x18000, 0x2000 );	ROM_CONTINUE(             0x12000, 0x2000 );	ROM_CONTINUE(             0x1a000, 0x2000 );	ROM_LOAD( "di10.bin",     0x14000, 0x2000, 0x9b89300a );	ROM_CONTINUE(             0x1c000, 0x2000 );	ROM_CONTINUE(             0x16000, 0x2000 );	ROM_CONTINUE(             0x1e000, 0x2000 );
		ROM_REGION( 0x20000, REGION_GFX4 | REGIONFLAG_DISPOSE );/* sprites */
		ROM_LOAD( "di16.bin",     0x00000, 0x8000, 0x0de055d7 );	ROM_LOAD( "di13.bin",     0x08000, 0x8000, 0x869219da );	ROM_LOAD( "di14.bin",     0x10000, 0x8000, 0x6b65812e );	ROM_LOAD( "di15.bin",     0x18000, 0x8000, 0x3e27f77d );
		ROM_REGION( 0x0200, REGION_PROMS );	ROM_LOAD( "firetrap.3b",  0x0000,  0x0100, 0x8bb45337 );/* palette red and green component */
		ROM_LOAD( "firetrap.4b",  0x0100,  0x0100, 0xd5abfc64 );/* palette blue component */
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_firetrap	   = new GameDriver("1986"	,"firetrap"	,"firetrap.java"	,rom_firetrap,null	,machine_driver_firetrap	,input_ports_firetrap	,null	,ROT90	,	"Data East USA", "Fire Trap", GAME_UNEMULATED_PROTECTION )
	public static GameDriver driver_firetpbl	   = new GameDriver("1986"	,"firetpbl"	,"firetrap.java"	,rom_firetpbl,driver_firetrap	,machine_driver_firetrap	,input_ports_firetrap	,null	,ROT90	,	"bootleg", "Fire Trap (Japan bootleg)" )
}
