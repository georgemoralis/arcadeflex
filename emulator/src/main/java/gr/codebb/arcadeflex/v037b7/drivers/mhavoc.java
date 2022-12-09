package gr.codebb.arcadeflex.v037b7.drivers;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.mame.inputH.*;
import static arcadeflex.v036.mame.sndintrfH.SOUND_POKEY;
import static gr.codebb.arcadeflex.v036.sound.pokeyH.*;
import static gr.codebb.arcadeflex.v036.sound.pokey.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.mame.timerH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.system1H.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.system1.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static common.libc.cstring.memcpy;
import static gr.codebb.arcadeflex.v037b7.mame.memory.memory_set_opcode_base;
import static gr.codebb.arcadeflex.v036.platform.fileio.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v036.vidhrdw.avgdvg.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.vector.*;
import static gr.codebb.arcadeflex.v037b7.machine.slapstic.*;

import static gr.codebb.arcadeflex.v037b7.machine.mhavoc.*;

public class mhavoc
{
	static UBytePtr nvram=new UBytePtr();
	static int[] nvram_size=new int[1];
	
	public static nvramHandlerPtr nvram_handler  = new nvramHandlerPtr() { public void handler(Object file, int read_or_write)
	{
		if (read_or_write != 0)
			osd_fwrite(file,nvram,nvram_size[0]);
		else
		{
			if (file != null)
				osd_fread(file,nvram,nvram_size[0]);
			else
				memset(nvram,0xff,nvram_size[0]);
		}
	} };
	
	
	static UBytePtr gammaram=new UBytePtr();
	
	public static ReadHandlerPtr mhavoc_gammaram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return gammaram.read(offset & 0x7ff);
	} };
	
	public static WriteHandlerPtr mhavoc_gammaram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		gammaram.write(offset & 0x7ff,data);
	} };
	
	
	/* Main board Readmem */
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x01ff, MRA_RAM ),			/* 0.5K Program Ram */
		new MemoryReadAddress( 0x0200, 0x07ff, MRA_BANK1 ),			/* 3K Paged Program RAM	*/
		new MemoryReadAddress( 0x0800, 0x09ff, MRA_RAM ),			/* 0.5K Program RAM */
		new MemoryReadAddress( 0x1000, 0x1000, mhavoc_gamma_r ),		/* Gamma Read Port */
		new MemoryReadAddress( 0x1200, 0x1200, mhavoc_port_0_r ),	/* Alpha Input Port 0 */
		new MemoryReadAddress( 0x1800, 0x1FFF, MRA_RAM),				/* Shared Beta Ram */
		new MemoryReadAddress( 0x2000, 0x3fff, MRA_BANK2 ),			/* Paged Program ROM (32K) */
		new MemoryReadAddress( 0x4000, 0x4fff, MRA_RAM ), /* Vector RAM	(4K) */
		new MemoryReadAddress( 0x5000, 0x5fff, MRA_ROM ),			/* Vector ROM (4K) */
		new MemoryReadAddress( 0x6000, 0x7fff, MRA_BANK3 ),			/* Paged Vector ROM (32K) */
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),			/* Program ROM (32K) */
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	
	/* Main Board Writemem */
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x01ff, MWA_RAM ),			/* 0.5K Program Ram */
		new MemoryWriteAddress( 0x0200, 0x07ff, MWA_BANK1 ),			/* 3K Paged Program RAM */
		new MemoryWriteAddress( 0x0800, 0x09ff, MWA_RAM ),			/* 0.5K Program RAM */
		new MemoryWriteAddress( 0x1200, 0x1200, MWA_NOP ),			/* don't care */
		new MemoryWriteAddress( 0x1400, 0x141f, mhavoc_colorram_w ),	/* ColorRAM */
		new MemoryWriteAddress( 0x1600, 0x1600, mhavoc_out_0_w ),		/* Control Signals */
		new MemoryWriteAddress( 0x1640, 0x1640, avgdvg_go_w ),			/* Vector Generator GO */
		new MemoryWriteAddress( 0x1680, 0x1680, MWA_NOP ),			/* Watchdog Clear */
		new MemoryWriteAddress( 0x16c0, 0x16c0, avgdvg_reset_w ),		/* Vector Generator Reset */
		new MemoryWriteAddress( 0x1700, 0x1700, MWA_NOP ),			/* IRQ ack */
		new MemoryWriteAddress( 0x1740, 0x1740, mhavoc_rom_banksel_w ),/* Program ROM Page Select */
		new MemoryWriteAddress( 0x1780, 0x1780, mhavoc_ram_banksel_w ),/* Program RAM Page Select */
		new MemoryWriteAddress( 0x17c0, 0x17c0, mhavoc_gamma_w ),		/* Gamma Communication Write Port */
		new MemoryWriteAddress( 0x1800, 0x1fff, MWA_RAM ),			/* Shared Beta Ram */
		new MemoryWriteAddress( 0x2000, 0x3fff, MWA_ROM ),			/* Major Havoc writes here.*/
		new MemoryWriteAddress( 0x4000, 0x4fff, MWA_RAM, vectorram, vectorram_size ),/* Vector Generator RAM	*/
		new MemoryWriteAddress( 0x6000, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	/* Gamma board readmem */
	static MemoryReadAddress gamma_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x07ff, MRA_RAM ),			/* Program RAM (2K)	*/
		new MemoryReadAddress( 0x0800, 0x1fff, mhavoc_gammaram_r ),	/* wraps to 0x000-0x7ff */
		new MemoryReadAddress( 0x2000, 0x203f, quad_pokey_r ),		/* Quad Pokey read	*/
		new MemoryReadAddress( 0x2800, 0x2800, mhavoc_port_1_r ),	/* Gamma Input Port	*/
		new MemoryReadAddress( 0x3000, 0x3000, mhavoc_alpha_r ),		/* Alpha Comm. Read Port*/
		new MemoryReadAddress( 0x3800, 0x3803, input_port_2_r ),		/* Roller Controller Input*/
		new MemoryReadAddress( 0x4000, 0x4000, input_port_4_r ),		/* DSW at 8S */
		new MemoryReadAddress( 0x6000, 0x61ff, MRA_RAM ),			/* EEROM		*/
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),			/* Program ROM (16K)	*/
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress gamma_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x07ff, MWA_RAM ),			/* Program RAM (2K)	*/
		new MemoryWriteAddress( 0x0800, 0x1fff, mhavoc_gammaram_w, gammaram ),	/* wraps to 0x000-0x7ff */
		new MemoryWriteAddress( 0x2000, 0x203f, quad_pokey_w ),		/* Quad Pokey write	*/
		new MemoryWriteAddress( 0x4000, 0x4000, mhavoc_irqack_w ),	/* IRQ Acknowledge	*/
		new MemoryWriteAddress( 0x4800, 0x4800, mhavoc_out_1_w ),		/* Coin Counters 	*/
		new MemoryWriteAddress( 0x5000, 0x5000, mhavoc_alpha_w ),		/* Alpha Comm. Write Port */
		new MemoryWriteAddress( 0x6000, 0x61ff, MWA_RAM, nvram, nvram_size ),	/* EEROM		*/
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static InputPortHandlerPtr input_ports_mhavoc = new InputPortHandlerPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 - alpha (player_1 = 0) */
		PORT_BIT ( 0x0f, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		/* PORT_BIT ( 0x10, IP_ACTIVE_LOW, IPT_SERVICE, "Diag Step", KEYCODE_T, IP_JOY_NONE );*/
		PORT_BIT ( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BITX( 0x20, IP_ACTIVE_LOW, IPT_SERVICE, "Diag Step/Coin C", KEYCODE_F1, IP_JOY_NONE );
		PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );/* Left Coin Switch  */
		PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_COIN2 );/* Right Coin */
	
		PORT_START(); 	/* IN1 - gamma */
		PORT_BIT ( 0x0f, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT ( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT ( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 );
	
		PORT_START(); 	/* IN2 - gamma */
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_REVERSE, 100, 40, 0, 0 );
	
		PORT_START();  /* DIP Switch at position 13/14S */
		PORT_DIPNAME( 0x01, 0x00, "Adaptive Difficulty" );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x0c, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x0c, "50000");
		PORT_DIPSETTING(    0x00, "100000");
		PORT_DIPSETTING(    0x04, "200000");
		PORT_DIPSETTING(    0x08, "None");
		PORT_DIPNAME( 0x30, 0x00, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x10, "Easy");
		PORT_DIPSETTING(    0x00, "Medium");
		PORT_DIPSETTING(    0x30, "Hard");
		PORT_DIPSETTING(    0x20, "Demo");
		PORT_DIPNAME( 0xc0, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3 (2 in Free Play)");
		PORT_DIPSETTING(    0xc0, "4 (3 in Free Play)");
		PORT_DIPSETTING(    0x80, "5 (4 in Free Play)");
		PORT_DIPSETTING(    0x40, "6 (5 in Free Play)");
	
		PORT_START();  /* DIP Switch at position 8S */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x0c, 0x0c, "Right Coin Mechanism" );
		PORT_DIPSETTING(    0x0c, "x1" );
		PORT_DIPSETTING(    0x08, "x4" );
		PORT_DIPSETTING(    0x04, "x5" );
		PORT_DIPSETTING(    0x00, "x6" );
		PORT_DIPNAME( 0x10, 0x10, "Left Coin Mechanism" );
		PORT_DIPSETTING(    0x10, "x1" );
		PORT_DIPSETTING(    0x00, "x2" );
		PORT_DIPNAME( 0xe0, 0xe0, "Bonus Credits" );
		PORT_DIPSETTING(    0x80, "2 each 4" );
		PORT_DIPSETTING(    0x40, "1 each 3" );
		PORT_DIPSETTING(    0xa0, "1 each 4" );
		PORT_DIPSETTING(    0x60, "1 each 5" );
		PORT_DIPSETTING(    0xe0, "None" );
	
		PORT_START(); 	/* IN5 - dummy for player_1 = 1 on alpha */
		PORT_BIT ( 0x3f, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_DIPNAME( 0x40, 0x40, "Credit to start" );
		PORT_DIPSETTING(    0x40, "1" );
		PORT_DIPSETTING(    0x00, "2" );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	INPUT_PORTS_END(); }}; 
	
	static InputPortHandlerPtr input_ports_mhavocp = new InputPortHandlerPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 - alpha (player_1 = 0) */
		PORT_BIT ( 0x0f, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		/* PORT_BIT ( 0x10, IP_ACTIVE_LOW, IPT_SERVICE, "Diag Step", KEYCODE_T, IP_JOY_NONE );*/
		PORT_BIT ( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BITX( 0x20, IP_ACTIVE_LOW, IPT_SERVICE, "Diag Step/Coin C", KEYCODE_F1, IP_JOY_NONE );
		PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );/* Left Coin Switch  */
		PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_COIN2 );/* Right Coin */
	
		PORT_START(); 	/* IN1 - gamma */
		PORT_BIT ( 0x0f, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT ( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT ( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 );
	
		PORT_START(); 	/* IN2 - gamma */
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_REVERSE, 100, 40, 0, 0 );
	
		PORT_START();  /* DIP Switch at position 13/14S */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "1" );
		PORT_DIPSETTING(    0x01, "2" );
		PORT_DIPSETTING(    0x02, "3" );
		PORT_DIPSETTING(    0x03, "4" );
		PORT_DIPNAME( 0x0c, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x0c, "50000");
		PORT_DIPSETTING(    0x00, "100000");
		PORT_DIPSETTING(    0x04, "200000");
		PORT_DIPSETTING(    0x08, "None");
		PORT_DIPNAME( 0x30, 0x00, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x10, "Easy");
		PORT_DIPSETTING(    0x00, "Medium");
		PORT_DIPSETTING(    0x30, "Hard");
		PORT_DIPSETTING(    0x20, "Demo");
		PORT_DIPNAME( 0xc0, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3 (2 in Free Play)");
		PORT_DIPSETTING(    0xc0, "4 (3 in Free Play)");
		PORT_DIPSETTING(    0x80, "5 (4 in Free Play)");
		PORT_DIPSETTING(    0x40, "6 (5 in Free Play)");
	
		PORT_START();  /* DIP Switch at position 8S */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x0c, 0x0c, "Right Coin Mechanism" );
		PORT_DIPSETTING(    0x0c, "x1" );
		PORT_DIPSETTING(    0x08, "x4" );
		PORT_DIPSETTING(    0x04, "x5" );
		PORT_DIPSETTING(    0x00, "x6" );
		PORT_DIPNAME( 0x10, 0x10, "Left Coin Mechanism" );
		PORT_DIPSETTING(    0x10, "x1" );
		PORT_DIPSETTING(    0x00, "x2" );
		PORT_DIPNAME( 0xe0, 0xe0, "Bonus Credits" );
		PORT_DIPSETTING(    0x80, "2 each 4" );
		PORT_DIPSETTING(    0x40, "1 each 3" );
		PORT_DIPSETTING(    0xa0, "1 each 4" );
		PORT_DIPSETTING(    0x60, "1 each 5" );
		PORT_DIPSETTING(    0xe0, "None" );
	
		PORT_START(); 	/* IN5 - dummy for player_1 = 1 on alpha */
		PORT_BIT ( 0x3f, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_DIPNAME( 0x40, 0x40, "Credit to start" );
		PORT_DIPSETTING(    0x40, "1" );
		PORT_DIPSETTING(    0x00, "2" );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	INPUT_PORTS_END(); }}; 
	
	
	
	static POKEYinterface pokey_interface = new POKEYinterface
	(
		4,	/* 4 chips */
		1250000,	/* 1.25 MHz??? */
		new int[] { 25, 25, 25, 25 },
		/* The 8 pot handlers */
		new ReadHandlerPtr[] { null, null, null, null },
		new ReadHandlerPtr[] { null, null, null, null },
		new ReadHandlerPtr[] { null, null, null, null },
		new ReadHandlerPtr[] { null, null, null, null },
		new ReadHandlerPtr[] { null, null, null, null },
		new ReadHandlerPtr[] { null, null, null, null },
		new ReadHandlerPtr[] { null, null, null, null },
		new ReadHandlerPtr[] { null, null, null, null },
		/* The allpot handler */
		new ReadHandlerPtr[] { input_port_3_r, null, null, null }
	);
	
	
	
	static MachineDriver machine_driver_mhavoc = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6502,
				2500000,	/* 2.5 MHz */
				readmem,writemem,null,null,
				interrupt,8 /* 2.4576 milliseconds period */
			),
			new MachineCPU(
				CPU_M6502,
				1250000,	/* 1.25 MHz */
				gamma_readmem,gamma_writemem,null,null,
				null, 0 /* no vblank interrupt */
			)
		},
		50, 0,	/* frames per second, vblank duration (vector game, so no vblank) */
				/* fps should be 30, but MH draws "empty" frames */
		1,		/* 1 CPU slice. see machine.c */
		mhavoc_init_machine,
	
		/* video hardware */
		400, 300, new rectangle( 0, 300, 0, 260 ),
		null,
		256,0,
		avg_init_palette_multi,
	
		VIDEO_TYPE_VECTOR | VIDEO_SUPPORTS_DIRTY,
		null,
		avg_start_mhavoc,
		avg_stop,
		vector_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_POKEY,
				pokey_interface
			)
		},
	
		nvram_handler
	);
	
	
	/*
	 * Notes:
	 * the R3 roms are supported as "mhavoc", the R2 roms (with a bug in gameplay)
	 * are supported as "mhavoc2".
	 * "Return to Vax" - Jess Askey's souped up version (errors on self test)
	 * are supported as "mhavocrv".
	 * Prototype is supported as "mhavocp"
	 */
	
	static RomLoadHandlerPtr rom_mhavoc = new RomLoadHandlerPtr(){ public void handler(){ 
		/* Alpha Processor ROMs */
		ROM_REGION( 0x21000, REGION_CPU1 );/* 152KB for ROMs */
		/* Vector Generator ROM */
		ROM_LOAD( "136025.210",   0x05000, 0x2000, 0xc67284ca );
	
		/* Program ROM */
		ROM_LOAD( "136025.216",   0x08000, 0x4000, 0x522a9cc0 );
		ROM_LOAD( "136025.217",   0x0c000, 0x4000, 0xea3d6877 );
	
		/* Paged Program ROM */
		ROM_LOAD( "136025.215",   0x10000, 0x4000, 0xa4d380ca );/* page 0+1 */
		ROM_LOAD( "136025.318",   0x14000, 0x4000, 0xba935067 );/* page 2+3 */
	
		/* Paged Vector Generator ROM */
		ROM_LOAD( "136025.106",   0x18000, 0x4000, 0x2ca83c76 );/* page 0+1 */
		ROM_LOAD( "136025.107",   0x1c000, 0x4000, 0x5f81c5f3 );/* page 2+3 */
	
		/* Gamma Processor ROM */
		ROM_REGION( 0x10000, REGION_CPU2 );/* 16k for code */
		ROM_LOAD( "136025.108",   0x08000, 0x4000, 0x93faf210 );
		ROM_RELOAD(               0x0c000, 0x4000 );/* reset+interrupt vectors */
	ROM_END(); }}; 
	
	static RomLoadHandlerPtr rom_mhavoc2 = new RomLoadHandlerPtr(){ public void handler(){ 
		/* Alpha Processor ROMs */
		ROM_REGION( 0x21000, REGION_CPU1 );
		/* Vector Generator ROM */
		ROM_LOAD( "136025.110",   0x05000, 0x2000, 0x16eef583 );
	
		/* Program ROM */
		ROM_LOAD( "136025.103",   0x08000, 0x4000, 0xbf192284 );
		ROM_LOAD( "136025.104",   0x0c000, 0x4000, 0x833c5d4e );
	
		/* Paged Program ROM - switched to 2000-3fff */
		ROM_LOAD( "136025.101",   0x10000, 0x4000, 0x2b3b591f );/* page 0+1 */
		ROM_LOAD( "136025.109",   0x14000, 0x4000, 0x4d766827 );/* page 2+3 */
	
		/* Paged Vector Generator ROM */
		ROM_LOAD( "136025.106",   0x18000, 0x4000, 0x2ca83c76 );/* page 0+1 */
		ROM_LOAD( "136025.107",   0x1c000, 0x4000, 0x5f81c5f3 );/* page 2+3 */
	
		/* the last 0x1000 is used for the 2 RAM pages */
	
		/* Gamma Processor ROM */
		ROM_REGION( 0x10000, REGION_CPU2 );/* 16k for code */
		ROM_LOAD( "136025.108",   0x08000, 0x4000, 0x93faf210 );
		ROM_RELOAD(               0x0c000, 0x4000 );/* reset+interrupt vectors */
	ROM_END(); }}; 
	
	static RomLoadHandlerPtr rom_mhavocrv = new RomLoadHandlerPtr(){ public void handler(){ 
		/* Alpha Processor ROMs */
		ROM_REGION( 0x21000, REGION_CPU1 );/* 152KB for ROMs */
		/* Vector Generator ROM */
		ROM_LOAD( "136025.210",   0x05000, 0x2000, 0xc67284ca );
	
		/* Program ROM */
		ROM_LOAD( "136025.916",   0x08000, 0x4000, 0x1255bd7f );
		ROM_LOAD( "136025.917",   0x0c000, 0x4000, 0x21889079 );
	
		/* Paged Program ROM */
		ROM_LOAD( "136025.915",   0x10000, 0x4000, 0x4c7235dc );/* page 0+1 */
		ROM_LOAD( "136025.918",   0x14000, 0x4000, 0x84735445 );/* page 2+3 */
	
		/* Paged Vector Generator ROM */
		ROM_LOAD( "136025.106",   0x18000, 0x4000, 0x2ca83c76 );/* page 0+1 */
		ROM_LOAD( "136025.907",   0x1c000, 0x4000, 0x4deea2c9 );/* page 2+3 */
	
		/* Gamma Processor ROM */
		ROM_REGION( 0x10000, REGION_CPU2 );/* 16k for code */
		ROM_LOAD( "136025.908",   0x08000, 0x4000, 0xc52ec664 );
		ROM_RELOAD(               0x0c000, 0x4000 );/* reset+interrupt vectors */
	ROM_END(); }}; 
	
	static RomLoadHandlerPtr rom_mhavocp = new RomLoadHandlerPtr(){ public void handler(){ 
		/* Alpha Processor ROMs */
		ROM_REGION( 0x21000, REGION_CPU1 );
		/* Vector Generator ROM */
		ROM_LOAD( "136025.010",   0x05000, 0x2000, 0x3050c0e6 );
	
		/* Program ROM */
		ROM_LOAD( "136025.016",   0x08000, 0x4000, 0x94caf6c0 );
		ROM_LOAD( "136025.017",   0x0c000, 0x4000, 0x05cba70a );
	
		/* Paged Program ROM - switched to 2000-3fff */
		ROM_LOAD( "136025.015",   0x10000, 0x4000, 0xc567c11b );
		ROM_LOAD( "136025.018",   0x14000, 0x4000, 0xa8c35ccd );
	
		/* Paged Vector Generator ROM */
		ROM_LOAD( "136025.006",   0x18000, 0x4000, 0xe272ed41 );
		ROM_LOAD( "136025.007",   0x1c000, 0x4000, 0xe152c9d8 );
	
		/* the last 0x1000 is used for the 2 RAM pages */
	
		/* Gamma Processor ROM */
		ROM_REGION( 0x10000, REGION_CPU2 );/* 16k for code */
		ROM_LOAD( "136025.008",   0x8000, 0x4000, 0x22ea7399 );
		ROM_RELOAD(               0xc000, 0x4000 );
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_mhavoc	   = new GameDriver("1983"	,"mhavoc"	,"mhavoc.java"	,rom_mhavoc,null	,machine_driver_mhavoc	,input_ports_mhavoc	,null	,ROT0	,	"Atari", "Major Havoc (rev 3)" );
	public static GameDriver driver_mhavoc2	   = new GameDriver("1983"	,"mhavoc2"	,"mhavoc.java"	,rom_mhavoc2,driver_mhavoc	,machine_driver_mhavoc	,input_ports_mhavoc	,null	,ROT0	,	"Atari", "Major Havoc (rev 2)" );
	public static GameDriver driver_mhavocrv	   = new GameDriver("1983"	,"mhavocrv"	,"mhavoc.java"	,rom_mhavocrv,driver_mhavoc	,machine_driver_mhavoc	,input_ports_mhavoc	,null	,ROT0	,	"hack", "Major Havoc (Return to Vax)" );
	public static GameDriver driver_mhavocp	   = new GameDriver("1983"	,"mhavocp"	,"mhavoc.java"	,rom_mhavocp,driver_mhavoc	,machine_driver_mhavoc	,input_ports_mhavocp	,null	,ROT0	,	"Atari", "Major Havoc (prototype)" );
}

