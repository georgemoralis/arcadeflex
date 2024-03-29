/***************************************************************************

Shisen

driver by Nicola Salmoria

***************************************************************************/
/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.drivers;
//generic imports
import static arcadeflex.toberemoved.v036.sndhrdw.m72.m72_init_sound;
import static arcadeflex.toberemoved.v036.sndhrdw.m72.m72_sample_r;
import static arcadeflex.toberemoved.v036.sndhrdw.m72.m72_sample_w;
import static arcadeflex.toberemoved.v036.sndhrdw.m72.m72_sound_command_w;
import static arcadeflex.toberemoved.v036.sndhrdw.m72.m72_sound_irq_ack_w;
import static arcadeflex.toberemoved.v036.sndhrdw.m72.m72_ym2151_irq_handler;
import static arcadeflex.toberemoved.v036.sndhrdw.m72.shisen_sample_addr_w;
import static arcadeflex.v036.generic.funcPtr.*;
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static arcadeflex.v036.sound._2151intf.*;
import static arcadeflex.v036.sound._2151intfH.*;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;
import static arcadeflex.v036.sound.dacH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.shisen.*;

public class shisen
{
	
	/* in vidhrdw/sichuan2.c */
	
	
	
	public static ReadHandlerPtr sichuan2_dsw1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int ret = input_port_3_r.handler(0);
	
		/* Based on the coin mode fill in the upper bits */
		if ((input_port_4_r.handler(0) & 0x04) != 0)
		{
			/* Mode 1 */
			ret	|= (input_port_5_r.handler(0) << 4);
		}
		else
		{
			/* Mode 2 */
			ret	|= (input_port_5_r.handler(0) & 0xf0);
		}
	
		return ret;
	} };
	
	public static WriteHandlerPtr sichuan2_coin_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0xf9) != 0x01) logerror("coin ctrl = %02x\n",data);
	
		coin_counter_w.handler(0,data & 2);
		coin_counter_w.handler(1,data & 4);
	} };
	
	
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0xbfff, MRA_BANK1 ),
		new MemoryReadAddress( 0xc800, 0xcaff, MRA_RAM ),
		new MemoryReadAddress( 0xd000, 0xffff, MRA_RAM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new MemoryWriteAddress( 0xc800, 0xcaff, sichuan2_paletteram_w, paletteram ),
		new MemoryWriteAddress( 0xd000, 0xdfff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0xe000, 0xffff, MWA_RAM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static IOReadPort readport[] =
	{
		new IOReadPort( 0x00, 0x00, sichuan2_dsw1_r ),
		new IOReadPort( 0x01, 0x01, input_port_4_r ),
		new IOReadPort( 0x02, 0x02, input_port_0_r ),
		new IOReadPort( 0x03, 0x03, input_port_1_r ),
		new IOReadPort( 0x04, 0x04, input_port_2_r ),
		new IOReadPort( -1 )	/* end of table */
	};
	
	static IOWritePort writeport[] =
	{
		new IOWritePort( 0x00, 0x00, sichuan2_coin_w ),
		new IOWritePort( 0x01, 0x01, m72_sound_command_w ),
		new IOWritePort( 0x02, 0x02, sichuan2_bankswitch_w ),
		new IOWritePort( -1 )	/* end of table */
	};
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new MemoryReadAddress( 0xfd00, 0xffff, MRA_RAM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x3fff, MWA_ROM ),
		new MemoryWriteAddress( 0xfd00, 0xffff, MWA_RAM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static IOReadPort sound_readport[] =
	{
		new IOReadPort( 0x01, 0x01, YM2151_status_port_0_r ),
		new IOReadPort( 0x80, 0x80, soundlatch_r ),
		new IOReadPort( 0x84, 0x84, m72_sample_r ),
		new IOReadPort( -1 )	/* end of table */
	};
	
	static IOWritePort sound_writeport[] =
	{
		new IOWritePort( 0x00, 0x00, YM2151_register_port_0_w ),
		new IOWritePort( 0x01, 0x01, YM2151_data_port_0_w ),
		new IOWritePort( 0x80, 0x81, shisen_sample_addr_w ),
		new IOWritePort( 0x82, 0x82, m72_sample_w ),
		new IOWritePort( 0x83, 0x83, m72_sound_irq_ack_w ),
		new IOWritePort( -1 )	/* end of table */
	};
	
	
	
	static InputPortHandlerPtr input_ports_shisen = new InputPortHandlerPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* COIN */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x03, 0x03, "Timer" );
		PORT_DIPSETTING(    0x03, "1" );
		PORT_DIPSETTING(    0x02, "2" );
		PORT_DIPSETTING(    0x01, "3" );
		PORT_DIPSETTING(    0x00, "4" );
		PORT_DIPNAME( 0x0c, 0x0c, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x0c, "1" );
		PORT_DIPSETTING(    0x08, "2" );
		PORT_DIPSETTING(    0x04, "3" );
		PORT_DIPSETTING(    0x00, "4" );
		PORT_BIT( 0xf0, IP_ACTIVE_HIGH, IPT_UNUSED ); /* Gets filled in based on the coin mode */
	
		PORT_START(); 	/* DSW2 */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, "Coin Mode" );
		PORT_DIPSETTING(    0x04, "Mode 1" );
		PORT_DIPSETTING(    0x00, "Mode 2" );
		PORT_DIPNAME( 0x08, 0x08, "Naughty Pics" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, "Gal Select" );
		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Yes") );
		/* In stop mode, press 2 to stop and 1 to restart */
		PORT_BITX   ( 0x20, 0x20, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Stop Mode", IP_KEY_NONE, IP_JOY_NONE );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, "Max Players" );
		PORT_DIPSETTING(    0x00, "1" );
		PORT_DIPSETTING(    0x40, "2" );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	
		/* Fake port to support the two different coin modes */
		PORT_START(); 
		PORT_DIPNAME( 0x0f, 0x0f, "Coinage Mode 1" );  /* mapped on coin mode 1 */
		PORT_DIPSETTING(    0x0a, DEF_STR( "6C_1C") );
		PORT_DIPSETTING(    0x0b, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x0c, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x0d, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "8C_3C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "5C_3C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x09, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Free_Play") );
	 	PORT_DIPNAME( 0x30, 0x30, "Coin A  Mode 2" );  /* mapped on coin mode 2 */
		PORT_DIPSETTING(    0x00, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0xc0, 0xc0, "Coin B  Mode 2" );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_6C") );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,
		4096*8,
		4,
		new int[] { 0, 4, 0x80000*8+0, 0x80000*8+4 },
		new int[] { 0, 1, 2, 3, 8*8+0, 8*8+1, 8*8+2, 8*8+3 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		16*8
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0x00000, charlayout,  0, 16 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static YM2151interface ym2151_interface = new YM2151interface
	(
		1,			/* 1 chip */
		3579645,	/* 3.579645 MHz */
		new int[] { YM3012_VOL(100,MIXER_PAN_LEFT,100,MIXER_PAN_RIGHT) },
		new WriteYmHandlerPtr[] { m72_ym2151_irq_handler },
		new WriteHandlerPtr[] { null }
	);
	
	static DACinterface dac_interface = new DACinterface
	(
		1,	/* 1 channel */
		new int[] { 50 }
	);
	
	
	
	static MachineDriver machine_driver_shisen = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				6000000,	/* 6 MHz ? */
				readmem,writemem,readport,writeport,
				interrupt,1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				3579645,		   /* 3.579645 MHz? (Vigilante) */
				sound_readmem,sound_writemem,sound_readport,sound_writeport,
				nmi_interrupt,128	/* clocked by V1? (Vigilante) */
									/* IRQs are generated by main Z80 and YM2151 */
			)
		},
		55, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,	/* 1 CPU slice per frame - interleaving is forced when a sound command is written */
		m72_init_sound,
	
		/* video hardware */
		64*8, 32*8, new rectangle( 0*8, 64*8-1, 0*8, 32*8-1 ),
		gfxdecodeinfo,
		256, 256,
		null,
	
		VIDEO_TYPE_RASTER|VIDEO_MODIFIES_PALETTE|VIDEO_SUPPORTS_DIRTY|VIDEO_PIXEL_ASPECT_RATIO_1_2,
		null,
		generic_vh_start,
		generic_vh_stop,
		sichuan2_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			),
			new MachineSound(
				SOUND_DAC,
				dac_interface
			)
		}
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadHandlerPtr rom_sichuan2 = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x30000, REGION_CPU1 );/* 64k+128k for main CPU */
		ROM_LOAD( "ic06.06",      0x00000, 0x10000, 0x98a2459b );
		ROM_RELOAD(               0x10000, 0x10000 );
		ROM_LOAD( "ic07.03",      0x20000, 0x10000, 0x0350f6e2 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "ic01.01",      0x00000, 0x10000, 0x51b0a26c );
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ic08.04",      0x00000, 0x10000, 0x1c0e221c );
		ROM_LOAD( "ic09.05",      0x10000, 0x10000, 0x8a7d8284 );
		ROM_LOAD( "ic12.08",      0x20000, 0x10000, 0x48e1d043 );
		ROM_LOAD( "ic13.09",      0x30000, 0x10000, 0x3feff3f2 );
		ROM_LOAD( "ic14.10",      0x40000, 0x10000, 0xb76a517d );
		ROM_LOAD( "ic15.11",      0x50000, 0x10000, 0x8ff5ee7a );
		ROM_LOAD( "ic16.12",      0x60000, 0x10000, 0x64e5d837 );
		ROM_LOAD( "ic17.13",      0x70000, 0x10000, 0x02c1b2c4 );
		ROM_LOAD( "ic18.14",      0x80000, 0x10000, 0xf5a8370e );
		ROM_LOAD( "ic19.15",      0x90000, 0x10000, 0x7a9b7671 );
		ROM_LOAD( "ic20.16",      0xa0000, 0x10000, 0x7fb396ad );
		ROM_LOAD( "ic21.17",      0xb0000, 0x10000, 0xfb83c652 );
		ROM_LOAD( "ic22.18",      0xc0000, 0x10000, 0xd8b689e9 );
		ROM_LOAD( "ic23.19",      0xd0000, 0x10000, 0xe6611947 );
		ROM_LOAD( "ic10.06",      0xe0000, 0x10000, 0x473b349a );
		ROM_LOAD( "ic11.07",      0xf0000, 0x10000, 0xd9a60285 );
	
		ROM_REGION( 0x40000, REGION_SOUND1 );/* samples */
		ROM_LOAD( "ic02.02",      0x00000, 0x10000, 0x92f0093d );
		ROM_LOAD( "ic03.03",      0x10000, 0x10000, 0x116a049c );
		ROM_LOAD( "ic04.04",      0x20000, 0x10000, 0x6840692b );
		ROM_LOAD( "ic05.05",      0x30000, 0x10000, 0x92ffe22a );
	ROM_END(); }}; 
	
	static RomLoadHandlerPtr rom_sichuana = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x30000, REGION_CPU1 );/* 64k+128k for main CPU */
		ROM_LOAD( "sichuan.a6",   0x00000, 0x10000, 0xf8ac05ef );
		ROM_RELOAD(               0x10000, 0x10000 );
		ROM_LOAD( "ic07.03",      0x20000, 0x10000, 0x0350f6e2 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "ic01.01",      0x00000, 0x10000, 0x51b0a26c );
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ic08.04",      0x00000, 0x10000, 0x1c0e221c );
		ROM_LOAD( "ic09.05",      0x10000, 0x10000, 0x8a7d8284 );
		ROM_LOAD( "ic12.08",      0x20000, 0x10000, 0x48e1d043 );
		ROM_LOAD( "ic13.09",      0x30000, 0x10000, 0x3feff3f2 );
		ROM_LOAD( "ic14.10",      0x40000, 0x10000, 0xb76a517d );
		ROM_LOAD( "ic15.11",      0x50000, 0x10000, 0x8ff5ee7a );
		ROM_LOAD( "ic16.12",      0x60000, 0x10000, 0x64e5d837 );
		ROM_LOAD( "ic17.13",      0x70000, 0x10000, 0x02c1b2c4 );
		ROM_LOAD( "ic18.14",      0x80000, 0x10000, 0xf5a8370e );
		ROM_LOAD( "ic19.15",      0x90000, 0x10000, 0x7a9b7671 );
		ROM_LOAD( "ic20.16",      0xa0000, 0x10000, 0x7fb396ad );
		ROM_LOAD( "ic21.17",      0xb0000, 0x10000, 0xfb83c652 );
		ROM_LOAD( "ic22.18",      0xc0000, 0x10000, 0xd8b689e9 );
		ROM_LOAD( "ic23.19",      0xd0000, 0x10000, 0xe6611947 );
		ROM_LOAD( "ic10.06",      0xe0000, 0x10000, 0x473b349a );
		ROM_LOAD( "ic11.07",      0xf0000, 0x10000, 0xd9a60285 );
	
		ROM_REGION( 0x40000, REGION_SOUND1 );/* samples */
		ROM_LOAD( "ic02.02",      0x00000, 0x10000, 0x92f0093d );
		ROM_LOAD( "ic03.03",      0x10000, 0x10000, 0x116a049c );
		ROM_LOAD( "ic04.04",      0x20000, 0x10000, 0x6840692b );
		ROM_LOAD( "ic05.05",      0x30000, 0x10000, 0x92ffe22a );
	ROM_END(); }}; 
	
	static RomLoadHandlerPtr rom_shisen = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x30000, REGION_CPU1 );/* 64k+128k for main CPU */
		ROM_LOAD( "a-27-a.rom",   0x00000, 0x20000, 0xde2ecf05 );
		ROM_RELOAD(               0x10000, 0x20000 );
	
		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "ic01.01",      0x00000, 0x10000, 0x51b0a26c );
	
		ROM_REGION( 0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ic08.04",      0x00000, 0x10000, 0x1c0e221c );
		ROM_LOAD( "ic09.05",      0x10000, 0x10000, 0x8a7d8284 );
		ROM_LOAD( "ic12.08",      0x20000, 0x10000, 0x48e1d043 );
		ROM_LOAD( "ic13.09",      0x30000, 0x10000, 0x3feff3f2 );
		ROM_LOAD( "ic14.10",      0x40000, 0x10000, 0xb76a517d );
		ROM_LOAD( "ic15.11",      0x50000, 0x10000, 0x8ff5ee7a );
		ROM_LOAD( "ic16.12",      0x60000, 0x10000, 0x64e5d837 );
		ROM_LOAD( "ic17.13",      0x70000, 0x10000, 0x02c1b2c4 );
		ROM_LOAD( "ic18.14",      0x80000, 0x10000, 0xf5a8370e );
		ROM_LOAD( "ic19.15",      0x90000, 0x10000, 0x7a9b7671 );
		ROM_LOAD( "ic20.16",      0xa0000, 0x10000, 0x7fb396ad );
		ROM_LOAD( "ic21.17",      0xb0000, 0x10000, 0xfb83c652 );
		ROM_LOAD( "ic22.18",      0xc0000, 0x10000, 0xd8b689e9 );
		ROM_LOAD( "ic23.19",      0xd0000, 0x10000, 0xe6611947 );
		ROM_LOAD( "ic10.06",      0xe0000, 0x10000, 0x473b349a );
		ROM_LOAD( "ic11.07",      0xf0000, 0x10000, 0xd9a60285 );
	
		ROM_REGION( 0x40000, REGION_SOUND1 );/* samples */
		ROM_LOAD( "ic02.02",      0x00000, 0x10000, 0x92f0093d );
		ROM_LOAD( "ic03.03",      0x10000, 0x10000, 0x116a049c );
		ROM_LOAD( "ic04.04",      0x20000, 0x10000, 0x6840692b );
		ROM_LOAD( "ic05.05",      0x30000, 0x10000, 0x92ffe22a );
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_sichuan2	   = new GameDriver("1989"	,"sichuan2"	,"shisen.java"	,rom_sichuan2,null	,machine_driver_shisen	,input_ports_shisen	,null	,ROT0	,	"Tamtex", "Sichuan II (hack?) (set 1)" );
	public static GameDriver driver_sichuana	   = new GameDriver("1989"	,"sichuana"	,"shisen.java"	,rom_sichuana,driver_sichuan2	,machine_driver_shisen	,input_ports_shisen	,null	,ROT0	,	"Tamtex", "Sichuan II (hack ?) (set 2)" );
	public static GameDriver driver_shisen	   = new GameDriver("1989"	,"shisen"	,"shisen.java"	,rom_shisen,driver_sichuan2	,machine_driver_shisen	,input_ports_shisen	,null	,ROT0	,	"Tamtex", "Shisensho - Joshiryo-Hen (Japan)" );
}
