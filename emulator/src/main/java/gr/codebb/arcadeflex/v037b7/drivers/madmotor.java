/***************************************************************************

  Mad Motor								(c) 1989 Mitchell Corporation

  But it's really a Data East game..  Bad Dudes era graphics hardware with
  Dark Seal era sound hardware.  Maybe a license for a specific territory?

  "This game is developed by Mitchell, but they entrusted PCB design and some
  routines to Data East."

  Emulation by Bryan McPhail, mish@tendril.co.uk

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.drivers;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.cpu.h6280.h6280.*;
import static gr.codebb.arcadeflex.v036.cpu.h6280.h6280H.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.madmotor.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static arcadeflex.v036.sound._2151intf.*;
import static arcadeflex.v036.sound._2151intfH.*;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.sound._2203intf.*;
import static arcadeflex.v036.sound._2203intfH.*;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295.*;
import gr.codebb.arcadeflex.v037b7.sound.okim6295H.OKIM6295interface;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.*;

public class madmotor
{
	static UBytePtr madmotor_ram=new UBytePtr();
	
	/******************************************************************************/
	
	public static WriteHandlerPtr madmotor_sound_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		soundlatch_w.handler(0,data & 0xff);
		cpu_cause_interrupt(1,H6280_INT_IRQ1);
	} };
	
	public static ReadHandlerPtr madmotor_control_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch (offset)
		{
			case 2: /* Player 1 & Player 2 joysticks & fire buttons */
				return (readinputport(0) + (readinputport(1) << 8));
	
			case 4: /* Dip Switches */
				return (readinputport(3) + (readinputport(4) << 8));
	
			case 6: /* Credits */
				return readinputport(2);
		}
	
		logerror("Unknown control read at %d\n",offset);
		return 0xffff;
	} };
	
	/******************************************************************************/
	
	static MemoryReadAddress madmotor_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
		new MemoryReadAddress( 0x184000, 0x1847ff, madmotor_pf1_rowscroll_r ),
		new MemoryReadAddress( 0x188000, 0x189fff, madmotor_pf1_data_r ),
		new MemoryReadAddress( 0x198000, 0x1987ff, madmotor_pf2_data_r ),
		new MemoryReadAddress( 0x1a4000, 0x1a4fff, madmotor_pf3_data_r ),
		new MemoryReadAddress( 0x18c000, 0x18c001, MRA_NOP ),
		new MemoryReadAddress( 0x19c000, 0x19c001, MRA_NOP ),
		new MemoryReadAddress( 0x3e0000, 0x3e3fff, MRA_BANK1 ),
		new MemoryReadAddress( 0x3e8000, 0x3e87ff, MRA_BANK2 ),
		new MemoryReadAddress( 0x3f0000, 0x3f07ff, paletteram_word_r ),
		new MemoryReadAddress( 0x3f8000, 0x3f800f, madmotor_control_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress madmotor_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x18c000, 0x18c001, MWA_NOP ),
	
		new MemoryWriteAddress( 0x180000, 0x18001f, madmotor_pf1_control_w ),
		new MemoryWriteAddress( 0x184000, 0x1847ff, madmotor_pf1_rowscroll_w, madmotor_pf1_rowscroll ),
		new MemoryWriteAddress( 0x188000, 0x189fff, madmotor_pf1_data_w, madmotor_pf1_data ),
		new MemoryWriteAddress( 0x190000, 0x19001f, madmotor_pf2_control_w ),
		new MemoryWriteAddress( 0x198000, 0x1987ff, madmotor_pf2_data_w, madmotor_pf2_data ),
		new MemoryWriteAddress( 0x1a0000, 0x1a001f, madmotor_pf3_control_w ),
		new MemoryWriteAddress( 0x1a4000, 0x1a4fff, madmotor_pf3_data_w, madmotor_pf3_data ),
	
		new MemoryWriteAddress( 0x3e0000, 0x3e3fff, MWA_BANK1, madmotor_ram ),
		new MemoryWriteAddress( 0x3e8000, 0x3e87ff, MWA_BANK2, spriteram ),
		new MemoryWriteAddress( 0x3f0000, 0x3f07ff, paletteram_xxxxBBBBGGGGRRRR_word_w, paletteram ),
		new MemoryWriteAddress( 0x3fc004, 0x3fc005, madmotor_sound_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	/******************************************************************************/
	
	public static WriteHandlerPtr YM2151_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		switch (offset) {
		case 0:
			YM2151_register_port_0_w.handler(0,data);
			break;
		case 1:
			YM2151_data_port_0_w.handler(0,data);
			break;
		}
	} };
	
	public static WriteHandlerPtr YM2203_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		switch (offset) {
		case 0:
			YM2203_control_port_0_w.handler(0,data);
			break;
		case 1:
			YM2203_write_port_0_w.handler(0,data);
			break;
		}
	} };
	
	/* Physical memory map (21 bits) */
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x00ffff, MRA_ROM ),
		new MemoryReadAddress( 0x100000, 0x100001, YM2203_status_port_0_r ),
		new MemoryReadAddress( 0x110000, 0x110001, YM2151_status_port_0_r ),
		new MemoryReadAddress( 0x120000, 0x120001, OKIM6295_status_0_r ),
		new MemoryReadAddress( 0x130000, 0x130001, OKIM6295_status_1_r ),
		new MemoryReadAddress( 0x140000, 0x140001, soundlatch_r ),
		new MemoryReadAddress( 0x1f0000, 0x1f1fff, MRA_BANK8 ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x00ffff, MWA_ROM ),
		new MemoryWriteAddress( 0x100000, 0x100001, YM2203_w ),
		new MemoryWriteAddress( 0x110000, 0x110001, YM2151_w ),
		new MemoryWriteAddress( 0x120000, 0x120001, OKIM6295_data_0_w ),
		new MemoryWriteAddress( 0x130000, 0x130001, OKIM6295_data_1_w ),
		new MemoryWriteAddress( 0x1f0000, 0x1f1fff, MWA_BANK8 ),
		new MemoryWriteAddress( 0x1fec00, 0x1fec01, H6280_timer_w ),
		new MemoryWriteAddress( 0x1ff402, 0x1ff403, H6280_irq_status_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	/******************************************************************************/
	
	static InputPortHandlerPtr input_ports_madmotor = new InputPortHandlerPtr(){ public void handler() { 
		PORT_START(); 	/* Player 1 controls */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );/* button 3 - unused */
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START(); 	/* Player 2 controls */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );/* button 3 - unused */
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
	
		PORT_START(); 	/* Credits */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_VBLANK );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* Dip switch bank 1 */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x28, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* Dip switch bank 2 */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "2" );
		PORT_DIPSETTING(    0x03, "3" );
		PORT_DIPSETTING(    0x02, "4" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x08, "Easy" );
		PORT_DIPSETTING(    0x0c, "Normal" );
		PORT_DIPSETTING(    0x04, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );
		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	/******************************************************************************/
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 chars */
		4096,
		4,		/* 4 bits per pixel  */
		new int[] { 0x18000*8, 0x8000*8, 0x10000*8, 0x00000*8 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8	/* every char takes 8 consecutive bytes */
	);
	
	static GfxLayout tilelayout = new GfxLayout
	(
		16,16,
		2048,
		4,
		new int[] { 0x30000*8, 0x10000*8, 0x20000*8, 0x00000*8 },
		new int[] { 16*8+0, 16*8+1, 16*8+2, 16*8+3, 16*8+4, 16*8+5, 16*8+6, 16*8+7,
				0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		16*16
	);
	
	static GfxLayout tilelayout2 = new GfxLayout
	(
		16,16,
		4096,
		4,
		new int[] { 0x60000*8, 0x20000*8, 0x40000*8, 0x00000*8 },
		new int[] { 16*8+0, 16*8+1, 16*8+2, 16*8+3, 16*8+4, 16*8+5, 16*8+6, 16*8+7,
				0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		16*16
	);
	
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,
		4096*2,
		4,
		new int[] { 0xc0000*8, 0x80000*8, 0x40000*8, 0x00000*8 },
		new int[] { 16*8+0, 16*8+1, 16*8+2, 16*8+3, 16*8+4, 16*8+5, 16*8+6, 16*8+7,
				0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
		16*16
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,     0, 16 ),	/* Characters 8x8 */
		new GfxDecodeInfo( REGION_GFX2, 0, tilelayout,   512, 16 ),	/* Tiles 16x16 */
		new GfxDecodeInfo( REGION_GFX3, 0, tilelayout2,  768, 16 ),	/* Tiles 16x16 */
		new GfxDecodeInfo( REGION_GFX4, 0, spritelayout, 256, 16 ),	/* Sprites 16x16 */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	/******************************************************************************/
	
	static OKIM6295interface okim6295_interface = new OKIM6295interface
	(
		2,              /* 2 chips */
		new int[] { 7757, 15514 },/* ?? Frequency */
		new int[] { REGION_SOUND1, REGION_SOUND2 },	/* memory regions */
		new int[] { 50, 25 }		/* Note!  Keep chip 1 (voices) louder than chip 2 */
	);
	
	static YM2203interface ym2203_interface = new YM2203interface
	(
		1,
		21470000/6,	/* ?? Audio section crystal is 21.470 MHz */
		new int[] { YM2203_VOL(40,40) },
		new ReadHandlerPtr[] { null },
		new ReadHandlerPtr[] { null },
		new WriteHandlerPtr[] { null },
		new WriteHandlerPtr[] { null }
	);
	
	static WriteYmHandlerPtr sound_irq = new WriteYmHandlerPtr() {
            @Override
            public void handler(int state) {
                cpu_set_irq_line(1,1,state); /* IRQ 2 */
            }
        };
	
	static YM2151interface ym2151_interface = new YM2151interface
	(
		1,
		21470000/6, /* ?? Audio section crystal is 21.470 MHz */
		new int[] { YM3012_VOL(45,MIXER_PAN_LEFT,45,MIXER_PAN_RIGHT) },
		new WriteYmHandlerPtr[] { sound_irq }
	);
	
	static MachineDriver machine_driver_madmotor = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
		 	new MachineCPU(
				CPU_M68000, /* Custom chip 59 */
				12000000, /* 24 MHz crystal */
				madmotor_readmem,madmotor_writemem,null,null,
				m68_level6_irq,1 /* VBL */
			),
			new MachineCPU(
				CPU_H6280 | CPU_AUDIO_CPU, /* Custom chip 45 */
				8053000/2, /* Crystal near CPU is 8.053 MHz */
				sound_readmem,sound_writemem,null,null,
				ignore_interrupt,0
			)
		},
		58, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration taken from Burger Time */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 1*8, 31*8-1 ),
	
		gfxdecodeinfo,
		1024, 1024,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK,
		null,
		madmotor_vh_start,
		null,
		madmotor_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
	  	new MachineSound[] {
			new MachineSound(
				SOUND_YM2203,
				ym2203_interface
			),
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			),
			new MachineSound(
				SOUND_OKIM6295,
				okim6295_interface
			)
		}
	);
	
	/******************************************************************************/
	
	static RomLoadHandlerPtr rom_madmotor = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x80000, REGION_CPU1 );/* 68000 code */
		ROM_LOAD_EVEN( "02", 0x00000, 0x20000, 0x50b554e0 );
		ROM_LOAD_ODD ( "00", 0x00000, 0x20000, 0x2d6a1b3f );
		ROM_LOAD_EVEN( "03", 0x40000, 0x20000, 0x442a0a52 );
		ROM_LOAD_ODD ( "01", 0x40000, 0x20000, 0xe246876e );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* Sound CPU */
		ROM_LOAD( "14",    0x00000, 0x10000, 0x1c28a7e5 );
	
		ROM_REGION( 0x020000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "04",    0x000000, 0x10000, 0x833ca3ab );/* chars */
		ROM_LOAD( "05",    0x010000, 0x10000, 0xa691fbfe );
	
		ROM_REGION( 0x040000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "10",    0x000000, 0x20000, 0x9dbf482b );/* tiles */
		ROM_LOAD( "11",    0x020000, 0x20000, 0x593c48a9 );
	
		ROM_REGION( 0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "06",    0x000000, 0x20000, 0x448850e5 );/* tiles */
		ROM_LOAD( "07",    0x020000, 0x20000, 0xede4d141 );
		ROM_LOAD( "08",    0x040000, 0x20000, 0xc380e5e5 );
		ROM_LOAD( "09",    0x060000, 0x20000, 0x1ee3326a );
	
		ROM_REGION( 0x100000, REGION_GFX4 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "15",    0x000000, 0x20000, 0x90ae9f74 );/* sprites */
		ROM_LOAD( "16",    0x020000, 0x20000, 0xe96ac815 );
		ROM_LOAD( "17",    0x040000, 0x20000, 0xabad9a1b );
		ROM_LOAD( "18",    0x060000, 0x20000, 0x96d8d64b );
		ROM_LOAD( "19",    0x080000, 0x20000, 0xcbd8c9b8 );
		ROM_LOAD( "20",    0x0a0000, 0x20000, 0x47f706a8 );
		ROM_LOAD( "21",    0x0c0000, 0x20000, 0x9c72d364 );
		ROM_LOAD( "22",    0x0e0000, 0x20000, 0x1e78aa60 );
	
		ROM_REGION( 0x20000, REGION_SOUND1 );/* ADPCM samples */
		ROM_LOAD( "12",    0x00000, 0x20000, 0xc202d200 );
	
		ROM_REGION( 0x20000, REGION_SOUND2 );/* ADPCM samples */
		ROM_LOAD( "13",    0x00000, 0x20000, 0xcc4d65e9 );
	ROM_END(); }}; 
	
	/******************************************************************************/
	
	static void madmotor_decrypt()
	{
		UBytePtr RAM = new UBytePtr(memory_region(REGION_CPU1));
		int i;
	
		for (i=0x00000; i<0x80000; i++) {
			RAM.write(i, (RAM.read(i) & 0xdb) | ((RAM.read(i) & 0x04) << 3) | ((RAM.read(i) & 0x20) >> 3));
			RAM.write(i, (RAM.read(i) & 0x7e) | ((RAM.read(i) & 0x01) << 7) | ((RAM.read(i) & 0x80) >> 7));
		}
	}
	
	public static ReadHandlerPtr cycle_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int ret=madmotor_ram.READ_WORD(0);
	
		if (cpu_get_pc()==0x646e && (ret&0x8000)==0x8000) {
			cpu_spinuntil_int();
			return 0;
		}
	
		return ret;
	} };
	
	public static InitDriverHandlerPtr init_madmotor = new InitDriverHandlerPtr() { public void handler() 
	{
		install_mem_read_handler(0, 0x3e0000, 0x3e0001, cycle_r);
		madmotor_decrypt();
	} };
	
	/******************************************************************************/
	
	 /* The title screen is undated, but it's (c) 1989 Data East at 0xefa0 */
	public static GameDriver driver_madmotor	   = new GameDriver("1989"	,"madmotor"	,"madmotor.java"	,rom_madmotor,null	,machine_driver_madmotor	,input_ports_madmotor	,init_madmotor	,ROT0	,	"Mitchell", "Mad Motor" );
}
