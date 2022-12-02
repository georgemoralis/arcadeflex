/*
 * Cosmic Chasm driver
 *
 * Jul 15 1999 by Mathis Rosenhauer
 *
 */

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;

public class cchasm
{
	
	/* from machine/cchasm.c */
	public static ReadHandlerPtr cchasm_6840_r = new ReadHandlerPtr() { public int handler(int offset);
	public static WriteHandlerPtr cchasm_6840_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static WriteHandlerPtr cchasm_led_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static WriteHandlerPtr cchasm_watchdog_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	
	/* from vidhrdw/cchasm.c */
	public static WriteHandlerPtr cchasm_refresh_control_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	void cchasm_init_colors (unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom);
	int cchasm_vh_start (void);
	void cchasm_vh_stop (void);
	
	
	/* from sndhrdw/cchasm.c */
	public static WriteHandlerPtr cchasm_io_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static ReadHandlerPtr cchasm_io_r = new ReadHandlerPtr() { public int handler(int offset);
	public static ReadHandlerPtr cchasm_snd_io_r = new ReadHandlerPtr() { public int handler(int offset);
	public static WriteHandlerPtr cchasm_snd_io_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	int cchasm_sh_start(const struct MachineSound *msound);
	void cchasm_sh_update(void);
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x00ffff, MRA_ROM ),
		new MemoryReadAddress( 0x040000, 0x04000f, cchasm_6840_r ),
		new MemoryReadAddress( 0x060000, 0x060001, input_port_0_r ),
		new MemoryReadAddress( 0xf80000, 0xf800ff, cchasm_io_r ),
		new MemoryReadAddress( 0xffb000, 0xffffff, MRA_BANK1 ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x00ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x040000, 0x04000f, cchasm_6840_w ),
		new MemoryWriteAddress( 0x050000, 0x050001, cchasm_refresh_control_w ),
		new MemoryWriteAddress( 0x060000, 0x060001, cchasm_led_w ),
		new MemoryWriteAddress( 0x070000, 0x070001, cchasm_watchdog_w ),
		new MemoryWriteAddress( 0xf80000, 0xf800ff, cchasm_io_w ),
		new MemoryWriteAddress( 0xffb000, 0xffffff, MWA_BANK1, cchasm_ram ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x0fff, MRA_ROM ),
		new MemoryReadAddress( 0x4000, 0x43ff, MRA_RAM ),
		new MemoryReadAddress( 0x5000, 0x53ff, MRA_RAM ),
		new MemoryReadAddress( 0x6000, 0x6fff, cchasm_snd_io_r ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x0fff, MWA_ROM ),
		new MemoryWriteAddress( 0x4000, 0x43ff, MWA_RAM ),
		new MemoryWriteAddress( 0x5000, 0x53ff, MWA_RAM ),
		new MemoryWriteAddress( 0x6000, 0x6fff, cchasm_snd_io_w ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static IOReadPort sound_readport[] =
	{
		new IOReadPort( 0x00, 0x03, z80ctc_0_r ),
		new IOReadPort( -1 )	/* end of table */
	};
	
	static IOWritePort sound_writeport[] =
	{
		new IOWritePort( 0x00, 0x03, z80ctc_0_w ),
		new IOWritePort( -1 )	/* end of table */
	};
	
	
	static InputPortPtr input_ports_cchasm = new InputPortPtr(){ public void handler() { 
		PORT_START();  /* DSW */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x01, "3" );
		PORT_DIPSETTING(    0x00, "5" );
		PORT_DIPNAME( 0x06, 0x06, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x06, "40000" );
		PORT_DIPSETTING(    0x04, "60000" );
		PORT_DIPSETTING(    0x02, "80000" );
		PORT_DIPSETTING(    0x00, "100000" );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x00, "Easy" );
		PORT_DIPSETTING(    0x08, "Hard" );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "Once" );
		PORT_DIPSETTING(    0x10, "Every" );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_1C") );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	
		PORT_START();  /* IN1 */
		PORT_ANALOG( 0xff, 0, IPT_DIAL, 100, 10, 0, 0);
	
		PORT_START();  /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON3 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
	
		PORT_START();  /* IN3 */
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "Test 1", KEYCODE_F1, IP_JOY_NONE );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNUSED );/* Test 2, not used in cchasm */
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNUSED );/* Test 3, not used in cchasm */
	INPUT_PORTS_END(); }}; 
	
	
	
	static struct AY8910interface ay8910_interface =
	{
		2,	/* 2 chips */
		1818182,	/* 1.82 MHz */
		{ 20, 20 },
		{ 0, 0 },
		{ 0, 0 },
		{ 0, 0 },
		{ 0, 0 }
	};
	
	static struct CustomSound_interface custom_interface =
	{
		cchasm_sh_start,
	    0,
		cchasm_sh_update
	};
	
	static Z80_DaisyChain daisy_chain[] =
	{
		{ z80ctc_reset, z80ctc_interrupt, z80ctc_reti, 0 }, /* CTC number 0 */
		{ 0,0,0,-1} 		/* end mark */
	};
	
	static MachineDriver machine_driver_cchasm = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				8000000, /* 8 MHz (from schematics) */
				readmem, writemem,null,null,
				null,null
			),
			new MachineCPU(
				CPU_Z80,
				3584229,	/* 3.58  MHz (from schematics) */
				sound_readmem,sound_writemem,sound_readport,sound_writeport,
				null,null,
	            null,null,daisy_chain
			)
		},
		40, 0,	/* frames per second, vblank duration (vector game, so no vblank) */
		1,
		null,
	
		/* video hardware */
		400, 300, { 0, 1024-1, 0, 768-1 },
		0,
		256, 256,
		cchasm_init_colors,
	
		VIDEO_TYPE_VECTOR,
		null,
		cchasm_vh_start,
		cchasm_vh_stop,
		vector_vh_update,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_AY8910,
				ay8910_interface
			),
			new MachineSound(
				SOUND_CUSTOM,
				custom_interface
			)
		}
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_cchasm = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x010000, REGION_CPU1 );
	    ROM_LOAD_EVEN( "chasm.u4",  0x000000, 0x001000, 0x19244f25 )
	    ROM_LOAD_ODD ( "chasm.u12", 0x000000, 0x001000, 0x5d702c7d )
	    ROM_LOAD_EVEN( "chasm.u8",  0x002000, 0x001000, 0x56a7ce8a )
	    ROM_LOAD_ODD ( "chasm.u16", 0x002000, 0x001000, 0x2e192db0 )
	    ROM_LOAD_EVEN( "chasm.u3",  0x004000, 0x001000, 0x9c71c600 )
	    ROM_LOAD_ODD ( "chasm.u11",  0x004000, 0x001000, 0xa4eb59a5 )
	    ROM_LOAD_EVEN( "chasm.u7",  0x006000, 0x001000, 0x8308dd6e )
	    ROM_LOAD_ODD ( "chasm.u15", 0x006000, 0x001000, 0x9d3abf97 )
	    ROM_LOAD_EVEN( "u2",        0x008000, 0x001000, 0x4e076ae7 )
	    ROM_LOAD_ODD ( "u10",       0x008000, 0x001000, 0xcc9e19ca )
	    ROM_LOAD_EVEN( "chasm.u6",  0x00a000, 0x001000, 0xa96525d2 )
	    ROM_LOAD_ODD ( "chasm.u14", 0x00a000, 0x001000, 0x8e426628 )
	    ROM_LOAD_EVEN( "u1",        0x00c000, 0x001000, 0x88b71027 )
	    ROM_LOAD_ODD ( "chasm.u9",  0x00c000, 0x001000, 0xd90c9773 )
	    ROM_LOAD_EVEN( "chasm.u5",  0x00e000, 0x001000, 0xe4a58b7d )
	    ROM_LOAD_ODD ( "chasm.u13", 0x00e000, 0x001000, 0x877e849c )
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "2732.bin", 0x0000, 0x1000, 0x715adc4a );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_cchasm1 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x010000, REGION_CPU1 );
	    ROM_LOAD_EVEN( "chasm.u4",  0x000000, 0x001000, 0x19244f25 )
	    ROM_LOAD_ODD ( "chasm.u12", 0x000000, 0x001000, 0x5d702c7d )
	    ROM_LOAD_EVEN( "chasm.u8",  0x002000, 0x001000, 0x56a7ce8a )
	    ROM_LOAD_ODD ( "chasm.u16", 0x002000, 0x001000, 0x2e192db0 )
	    ROM_LOAD_EVEN( "chasm.u3",  0x004000, 0x001000, 0x9c71c600 )
	    ROM_LOAD_ODD ( "chasm.u11", 0x004000, 0x001000, 0xa4eb59a5 )
	    ROM_LOAD_EVEN( "chasm.u7",  0x006000, 0x001000, 0x8308dd6e )
	    ROM_LOAD_ODD ( "chasm.u15", 0x006000, 0x001000, 0x9d3abf97 )
	    ROM_LOAD_EVEN( "chasm.u2",  0x008000, 0x001000, 0x008b26ef )
	    ROM_LOAD_ODD ( "chasm.u10", 0x008000, 0x001000, 0xc2c532a3 )
	    ROM_LOAD_EVEN( "chasm.u6",  0x00a000, 0x001000, 0xa96525d2 )
	    ROM_LOAD_ODD ( "chasm.u14", 0x00a000, 0x001000, 0x8e426628 )
	    ROM_LOAD_EVEN( "chasm.u1",  0x00c000, 0x001000, 0xe02293f8 )
	    ROM_LOAD_ODD ( "chasm.u9",  0x00c000, 0x001000, 0xd90c9773 )
	    ROM_LOAD_EVEN( "chasm.u5",  0x00e000, 0x001000, 0xe4a58b7d )
	    ROM_LOAD_ODD ( "chasm.u13", 0x00e000, 0x001000, 0x877e849c )
	
		ROM_REGION( 0x1000, REGION_CPU2 );/* 4k for the audio CPU */
		ROM_LOAD( "2732.bin", 0x0000, 0x1000, 0x715adc4a );
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_cchasm	   = new GameDriver("1983"	,"cchasm"	,"cchasm.java"	,rom_cchasm,null	,machine_driver_cchasm	,input_ports_cchasm	,null	,ROT270	,	"Cinematronics / GCE", "Cosmic Chasm (set 1)" )
	public static GameDriver driver_cchasm1	   = new GameDriver("1983"	,"cchasm1"	,"cchasm.java"	,rom_cchasm1,driver_cchasm	,machine_driver_cchasm	,input_ports_cchasm	,null	,ROT270	,	"Cinematronics / GCE", "Cosmic Chasm (set 2)" )
}
