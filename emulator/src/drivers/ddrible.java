/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */ 
package drivers;
import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.commonH.*;
import static mame.inputport.*;
import static mame.drawgfxH.*;
import static vidhrdw.generic.*;
import static mame.cpuintrf.*;
import static mame.cpuintrfH.*;
import static mame.inputportH.*;
import static mame.mame.*;
import static arcadeflex.libc_old.*;
import static arcadeflex.libc.*;
import static mame.sndintrf.soundlatch_r;
import static mame.sndintrf.soundlatch_w;
import static cpu.m6809.m6809H.*;
import static machine.ddrible.*;
import static cpu.z80.z80H.*;
import static mame.common.*;
import static mame.commonH.*;
import static mame.palette.*;
import static vidhrdw.ddrible.*;

public class ddrible
{

	public static ReadHandlerPtr ddrible_vlm5030_busy_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return rand(); /* patch */
/*TODO*///		if (VLM5030_BSY()) return 1;
/*TODO*///		else return 0;
	} };
	
	public static WriteHandlerPtr ddrible_vlm5030_ctrl_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
/*TODO*///		UBytePtr SPEECH_ROM = memory_region(REGION_SOUND1);
		/* b7 : vlm data bus OE   */
		/* b6 : VLM5030-RST       */
		/* b5 : VLM5030-ST        */
		/* b4 : VLM5300-VCU       */
		/* b3 : ROM bank select   */
/*TODO*///		VLM5030_RST( data & 0x40 ? 1 : 0 );
/*TODO*///		VLM5030_ST(  data & 0x20 ? 1 : 0 );
/*TODO*///		VLM5030_VCU( data & 0x10 ? 1 : 0 );
/*TODO*///		VLM5030_set_rom(&SPEECH_ROM[data & 0x08 ? 0x10000 : 0]);
		/* b2 : SSG-C rc filter enable */
		/* b1 : SSG-B rc filter enable */
		/* b0 : SSG-A rc filter enable */
/*TODO*///		set_RC_filter(2,1000,2200,1000,data & 0x04 ? 150000 : 0); /* YM2203-SSG-C */
/*TODO*///		set_RC_filter(1,1000,2200,1000,data & 0x02 ? 150000 : 0); /* YM2203-SSG-B */
/*TODO*///		set_RC_filter(0,1000,2200,1000,data & 0x01 ? 150000 : 0); /* YM2203-SSG-A */
	} };
	
		/* CPU 0 read addresses */
	static MemoryReadAddress readmem_cpu0[] =
	{
		new MemoryReadAddress( 0x1800, 0x187f, MRA_RAM ),			/* palette? */
		new MemoryReadAddress( 0x2000, 0x3fff, MRA_RAM ),			/* Video RAM 1 + Object RAM 1 */
		new MemoryReadAddress( 0x4000, 0x5fff, ddrible_sharedram_r ),/* shared RAM with CPU #1 */
		new MemoryReadAddress( 0x6000, 0x7fff, MRA_RAM ),			/* Video RAM 2 + Object RAM 2 */
		new MemoryReadAddress( 0x8000, 0x9fff, MRA_BANK1 ),			/* banked ROM */
		new MemoryReadAddress( 0xa000, 0xffff, MRA_ROM ),			/* ROM */
		new MemoryReadAddress( -1 )									/* end of table */
	};
	
		/* CPU 0 write addresses */
	static MemoryWriteAddress writemem_cpu0[] =
	{
		new MemoryWriteAddress( 0x0000, 0x0000, ddrible_fg_scrolly_w ),
		new MemoryWriteAddress( 0x0001, 0x0002, ddrible_fg_scrollx_w ),
		new MemoryWriteAddress( 0x0003, 0x0003, ddrible_bank_select_w ),
		new MemoryWriteAddress( 0x0004, 0x0004, int_0_w ),
	//	new MemoryWriteAddress( 0x0020, 0x005f, MWA_RAM ),				/* unused? */
		new MemoryWriteAddress( 0x0800, 0x0800, ddrible_bg_scrolly_w ),
		new MemoryWriteAddress( 0x0801, 0x0802, ddrible_bg_scrollx_w ),
		new MemoryWriteAddress( 0x0803, 0x0803, ddrible_bank_select_w_2 ),
		new MemoryWriteAddress( 0x0804, 0x0804, int_1_w ),
	//	new MemoryWriteAddress( 0x0820, 0x085f, MWA_RAM ),				/* unused? */
		new MemoryWriteAddress( 0x1800, 0x187f, paletteram_xBBBBBGGGGGRRRRR_swap_w, paletteram ), /* seems wrong, MSB is used as well */
		new MemoryWriteAddress( 0x2000, 0x2fff, ddrible_fg_videoram_w, ddrible_fg_videoram ),/* Video RAM 2 */
		new MemoryWriteAddress( 0x3000, 0x3fff, MWA_RAM, ddrible_spriteram_1 ),				/* Object RAM 1 */
		new MemoryWriteAddress( 0x4000, 0x5fff, ddrible_sharedram_w, ddrible_sharedram ),	/* shared RAM with CPU #1 */
		new MemoryWriteAddress( 0x6000, 0x6fff, ddrible_bg_videoram_w, ddrible_bg_videoram ),/* Video RAM 2 */
		new MemoryWriteAddress( 0x7000, 0x7fff, MWA_RAM, ddrible_spriteram_2 ),				/* Object RAM 2 + Work RAM */
		new MemoryWriteAddress( 0x8000, 0x8000, ddrible_bankswitch_w ),						/* bankswitch control */
		new MemoryWriteAddress( 0x8001, 0xffff, MWA_ROM ),									/* ROM */
		new MemoryWriteAddress( -1 )															/* end of table */
	};
	
		/* CPU 1 read addresses */
	static MemoryReadAddress readmem_cpu1[] =
	{
		new MemoryReadAddress( 0x0000, 0x1fff, ddrible_sharedram_r ),		/* shared RAM with CPU #0 */
		new MemoryReadAddress( 0x2000, 0x27ff, ddrible_snd_sharedram_r ),	/* shared RAM with CPU #2 */
		new MemoryReadAddress( 0x2800, 0x2800, input_port_3_r ),				/* DSW #1 */
		new MemoryReadAddress( 0x2801, 0x2801, input_port_0_r ),				/* player 1 inputs */
		new MemoryReadAddress( 0x2802, 0x2802, input_port_1_r ),				/* player 2 inputs */
		new MemoryReadAddress( 0x2803, 0x2803, input_port_2_r ),				/* coinsw  start */
		new MemoryReadAddress( 0x2c00, 0x2c00, input_port_4_r ),				/* DSW #2 */
		new MemoryReadAddress( 0x3000, 0x3000, input_port_5_r ),				/* DSW #3 */
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),					/* ROM */
		new MemoryReadAddress( -1 )											/* end of table */
	};
	
		/* CPU 1 write addresses */
	static MemoryWriteAddress writemem_cpu1[] =
	{
		new MemoryWriteAddress( 0x0000, 0x1fff, ddrible_sharedram_w ),		/* shared RAM with CPU #0 */
		new MemoryWriteAddress( 0x2000, 0x27ff, ddrible_snd_sharedram_w ),	/* shared RAM with CPU #2 */
		new MemoryWriteAddress( 0x3400, 0x3400, ddrible_coin_counter_w ),		/* coin counters */
		new MemoryWriteAddress( 0x3c00, 0x3c00, watchdog_reset_w ),			/* watchdog reset */
		new MemoryWriteAddress( 0x8000, 0xffff, MWA_ROM ),					/* ROM */
		new MemoryWriteAddress( -1 )											/* end of table */
	};
	
		/* CPU 2 (SOUND CPU) read addresses */
	static MemoryReadAddress readmem_cpu2[] =
	{
		new MemoryReadAddress( 0x0000, 0x07ff, ddrible_snd_sharedram_r ),	/* shared RAM with CPU #1 */
/*TODO*///		new MemoryReadAddress( 0x1000, 0x1000, YM2203_status_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0x1001, 0x1001, YM2203_read_port_0_r ),
		new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
		/* CPU 2 (SOUND CPU) write addresses */
	static MemoryWriteAddress writemem_cpu2[] =
	{
		new MemoryWriteAddress( 0x0000, 0x07ff, ddrible_snd_sharedram_w, ddrible_snd_sharedram  ),/* shared RAM with CPU #1 */
/*TODO*///		new MemoryWriteAddress( 0x1000, 0x1000, YM2203_control_port_0_w ),
/*TODO*///		new MemoryWriteAddress( 0x1001, 0x1001, YM2203_write_port_0_w ),
/*TODO*///		new MemoryWriteAddress( 0x3000, 0x3000, VLM5030_data_w ),
		new MemoryWriteAddress( 0x8000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static InputPortPtr input_ports_ddribble = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* PLAYER 1 INPUTS */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON3 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* PLAYER 2 INPUTS */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* COINSW & START */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* DSW #1 */
		PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x02, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_5C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_7C") );
		PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_5C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") );
	
		PORT_START(); 	/* DSW #2 */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x60, 0x40, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(	0x60, "Easy" );
		PORT_DIPSETTING(	0x40, "Normal" );
		PORT_DIPSETTING(	0x20, "Hard" );
		PORT_DIPSETTING(	0x00, "Hardest" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(	0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* DSW #3 */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(	0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x08, 0x00, "Allow vs match with 1 Credit" );
		PORT_DIPSETTING(	0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	static GfxLayout charlayout1 = new GfxLayout
	(
		8,8,			/* 8*8 characters */
		4096,			/* 4096 characters */
		4,				/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },	/* the four bitplanes are packed in one nibble */
		new int[] { 0*4, 1*4, 0x20000*8+0*4, 0x20000*8+1*4, 2*4, 3*4, 0x20000*8+2*4, 0x20000*8+3*4 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
		16*8			/* every char takes 16 consecutive bytes */
	);
	
	static GfxLayout charlayout2 = new GfxLayout
	(
		8,8,			/* 8*8 characters */
		8192,			/* 8192 characters */
		4,				/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 }, /* the four bitplanes are packed in one nibble */
		new int[] { 0*4, 1*4, 0x20000*8+0*4, 0x20000*8+1*4, 2*4, 3*4, 0x20000*8+2*4, 0x20000*8+3*4 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
		16*8			/* every char takes 16 consecutive bytes */
	);
	
	static GfxLayout spritelayout1 = new GfxLayout
	(
		16,16,			/* 16*16 sprites */
		1024,			/* 1024 sprites */
		4,				/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },	/* the four bitplanes are packed in one nibble */
		new int[] { 0*4, 1*4, 0x20000*8+0*4, 0x20000*8+1*4, 2*4, 3*4, 0x20000*8+2*4, 0x20000*8+3*4,
			16*8+0*4, 16*8+1*4, 0x20000*8+16*8+0*4, 0x20000*8+16*8+1*4, 16*8+2*4, 16*8+3*4, 0x20000*8+16*8+2*4, 0x20000*8+16*8+3*4 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16,
				16*16, 17*16, 18*16, 19*16, 20*16, 21*16, 22*16, 23*16 },
		64*8			/* every sprite takes 64 consecutive bytes */
	);
	
	static GfxLayout spritelayout2 = new GfxLayout
	(
		16,16,			/* 16*16 sprites */
		2048,			/* 2048 sprites */
		4,				/* 4 bits per pixel */
		new int[] { 0, 1, 2, 3 },	/* the four bitplanes are packed in one nibble */
		new int[] { 0*4, 1*4, 0x20000*8+0*4, 0x20000*8+1*4, 2*4, 3*4, 0x20000*8+2*4, 0x20000*8+3*4,
			16*8+0*4, 16*8+1*4, 0x20000*8+16*8+0*4, 0x20000*8+16*8+1*4, 16*8+2*4, 16*8+3*4, 0x20000*8+16*8+2*4, 0x20000*8+16*8+3*4 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16,
				16*16, 17*16, 18*16, 19*16, 20*16, 21*16, 22*16, 23*16 },
		64*8			/* every sprite takes 64 consecutive bytes */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0x00000, charlayout1,	 48,  1 ),	/* colors 48-63 */
		new GfxDecodeInfo( REGION_GFX2, 0x00000, charlayout2,	 16,  1 ),	/* colors 16-31 */
		new GfxDecodeInfo( REGION_GFX1, 0x10000, spritelayout1,  32,  1 ),	/* colors 32-47 */
		new GfxDecodeInfo( REGION_GFX2, 0x40000, spritelayout2,  64, 16 ),	/* colors  0-15 but using lookup table */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
/*TODO*///	static struct YM2203interface ym2203_interface =
/*TODO*///	{
/*TODO*///		1,			/* 1 chip */
/*TODO*///		3580000,	/* 3.58 MHz */
/*TODO*///		{ YM2203_VOL(25,25) },
/*TODO*///		{ 0 },
/*TODO*///		{ ddrible_vlm5030_busy_r },
/*TODO*///		{ ddrible_vlm5030_ctrl_w },
/*TODO*///		{ 0 }
/*TODO*///	};
	
/*TODO*///	static struct VLM5030interface vlm5030_interface =
/*TODO*///	{
/*TODO*///		3580000,    /* 3.58 MHz */
/*TODO*///		25,         /* volume */
/*TODO*///		REGION_SOUND1,	/* memory region of speech rom */
/*TODO*///		0x10000,    /* memory size 64Kbyte * 2 bank */
/*TODO*///		0           /* VCU pin level (default) */
/*TODO*///	};
	
	static MachineDriver machine_driver_ddribble = new MachineDriver
	(
		/* basic machine hardware  */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M6809,			/* CPU #0 */
				1536000,			/* 18432000/12 MHz? */
				readmem_cpu0,writemem_cpu0,null,null,
				ddrible_interrupt_0,1
			),
			new MachineCPU(
				CPU_M6809,			/* CPU #1 */
				1536000,			/* 18432000/12 MHz? */
				readmem_cpu1,writemem_cpu1,null,null,
				ddrible_interrupt_1,1
			),
			new MachineCPU(
				CPU_M6809,			/* SOUND CPU */
				1536000,			/* 18432000/12 MHz? */
				readmem_cpu2,writemem_cpu2,null,null,
				ignore_interrupt,1
			),
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,
		100,					/* we need heavy synch */
		ddrible_init_machine,	/* init machine routine */
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		64, 64 + 256,
		ddrible_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		ddrible_vh_start,
		null,
		ddrible_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		/*new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203_interface
			),
			new MachineSound(
				SOUND_VLM5030,
				vlm5030_interface
			)
		}*/
                null
	);
	
	
	static RomLoadPtr rom_ddribble = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x1a000, REGION_CPU1 );/* 64K CPU #0 + 40K for Banked ROMS */
		ROM_LOAD( "690c03.bin",	0x10000, 0x0a000, 0x07975a58 );
		ROM_CONTINUE(			0x0a000, 0x06000 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64 for the CPU #1 */
		ROM_LOAD( "690c02.bin", 0x08000, 0x08000, 0xf07c030a );
	
		ROM_REGION( 0x10000, REGION_CPU3 );/* 64k for the SOUND CPU */
		ROM_LOAD( "690b01.bin", 0x08000, 0x08000, 0x806b8453 );
	
		ROM_REGION( 0x40000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "690a05.bin",	0x00000, 0x20000, 0x6a816d0d );/* characters (set 1) & obtects (set 1) */
		ROM_LOAD( "690a06.bin",	0x20000, 0x20000, 0x46300cd0 );/* characters (set 1) & objects (set 1) */
	
		ROM_REGION( 0x80000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "690a10.bin", 0x00000, 0x20000, 0x61efa222 );/* characters (set 2) */
		ROM_LOAD( "690a09.bin", 0x20000, 0x20000, 0xab682186 );/* characters (set 2) */
		ROM_LOAD( "690a08.bin",	0x40000, 0x20000, 0x9a889944 );/* objects (set 2) */
		ROM_LOAD( "690a07.bin",	0x60000, 0x20000, 0xfaf81b3f );/* objects (set 2) */
	
		ROM_REGION( 0x0100, REGION_PROMS );
		ROM_LOAD( "690a11.i15", 0x0000, 0x0100, 0xf34617ad );/* sprite lookup table */
	
		ROM_REGION( 0x20000, REGION_SOUND1 );/* 128k for the VLM5030 data */
		ROM_LOAD( "690a04.bin", 0x00000, 0x20000, 0x1bfeb763 );
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_ddribble	   = new GameDriver("1986"	,"ddribble"	,"ddrible.java"	,rom_ddribble,null	,machine_driver_ddribble	,input_ports_ddribble	,null	,ROT0	,	"Konami", "Double Dribble", GAME_NO_COCKTAIL );
}
