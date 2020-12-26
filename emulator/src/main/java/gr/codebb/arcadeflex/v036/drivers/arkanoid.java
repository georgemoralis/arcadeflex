package gr.codebb.arcadeflex.v036.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.arkanoid.*;
import static gr.codebb.arcadeflex.v037b7.machine.arkanoid.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.v036.sound.ay8910H.*;
import static gr.codebb.arcadeflex.v036.sound.ay8910.*;

public class arkanoid {
    

    	static MemoryReadAddress readmem[] =
	{
    /*TODO*///	{ 0x0000, 0xbfff, MRA_ROM },
    /*TODO*///	{ 0xc000, 0xcfff, MRA_RAM },
    /*TODO*///	{ 0xd001, 0xd001, AY8910_read_port_0_r },
    /*TODO*///	{ 0xd00c, 0xd00c, arkanoid_68705_input_0_r },  /* mainly an input port, with 2 bits from the 68705 */
    /*TODO*///	{ 0xd010, 0xd010, input_port_1_r },
    /*TODO*///	{ 0xd018, 0xd018, arkanoid_Z80_mcu_r },  /* input from the 68705 */
    /*TODO*///	{ 0xe000, 0xefff, MRA_RAM },
    /*TODO*///	{ 0xf000, 0xffff, MRA_ROM },
    /*TODO*///	{ -1 }	/* end of table */
        };
	
	static MemoryWriteAddress writemem[] =
	{
    /*TODO*///	{ 0x0000, 0xbfff, MWA_ROM },
    /*TODO*///	{ 0xc000, 0xcfff, MWA_RAM },
    /*TODO*///	{ 0xd000, 0xd000, AY8910_control_port_0_w },
    /*TODO*///	{ 0xd001, 0xd001, AY8910_write_port_0_w },
    /*TODO*///	{ 0xd008, 0xd008, arkanoid_d008_w },	/* gfx bank, flip screen etc. */
    /*TODO*///	{ 0xd010, 0xd010, watchdog_reset_w },
    /*TODO*///	{ 0xd018, 0xd018, arkanoid_Z80_mcu_w }, /* output to the 68705 */
    /*TODO*///	{ 0xe000, 0xe7ff, videoram_w, &videoram, &videoram_size },
    /*TODO*///	{ 0xe800, 0xe83f, MWA_RAM, &spriteram, &spriteram_size },
    /*TODO*///	{ 0xe840, 0xefff, MWA_RAM },
    /*TODO*///	{ 0xf000, 0xffff, MWA_ROM },
    /*TODO*///	{ -1 }	/* end of table */
        };
        
	static MemoryReadAddress boot_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0xbfff, MRA_ROM ),
		new MemoryReadAddress( 0xc000, 0xcfff, MRA_RAM ),
		new MemoryReadAddress( 0xd001, 0xd001, AY8910_read_port_0_r ),
		new MemoryReadAddress( 0xd00c, 0xd00c, input_port_0_r ),
		new MemoryReadAddress( 0xd010, 0xd010, input_port_1_r ),
		new MemoryReadAddress( 0xd018, 0xd018, arkanoid_input_2_r ),
		new MemoryReadAddress( 0xe000, 0xefff, MRA_RAM ),
		new MemoryReadAddress( 0xf000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress boot_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new MemoryWriteAddress( 0xc000, 0xcfff, MWA_RAM ),
		new MemoryWriteAddress( 0xd000, 0xd000, AY8910_control_port_0_w ),
		new MemoryWriteAddress( 0xd001, 0xd001, AY8910_write_port_0_w ),
		new MemoryWriteAddress( 0xd008, 0xd008, arkanoid_d008_w ),	/* gfx bank, flip screen etc. */
		new MemoryWriteAddress( 0xd010, 0xd010, watchdog_reset_w ),
		new MemoryWriteAddress( 0xd018, 0xd018, MWA_NOP ),
		new MemoryWriteAddress( 0xe000, 0xe7ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0xe800, 0xe83f, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0xe840, 0xefff, MWA_RAM ),
		new MemoryWriteAddress( 0xf000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};

	static MemoryReadAddress mcu_readmem[] =
	{
    /*TODO*///	{ 0x0000, 0x0000, arkanoid_68705_portA_r },
    /*TODO*///	{ 0x0001, 0x0001, arkanoid_input_2_r },
    /*TODO*///	{ 0x0002, 0x0002, arkanoid_68705_portC_r },
    /*TODO*///	{ 0x0010, 0x007f, MRA_RAM },
    /*TODO*///	{ 0x0080, 0x07ff, MRA_ROM },
    /*TODO*///	{ -1 }	/* end of table */
    	};
	
	static MemoryWriteAddress mcu_writemem[] =
	{
    /*TODO*///	{ 0x0000, 0x0000, arkanoid_68705_portA_w },
    /*TODO*///	{ 0x0002, 0x0002, arkanoid_68705_portC_w },
    /*TODO*///	{ 0x0004, 0x0004, arkanoid_68705_ddrA_w },
    /*TODO*///	{ 0x0006, 0x0006, arkanoid_68705_ddrC_w },
    /*TODO*///	{ 0x0010, 0x007f, MWA_RAM },
    /*TODO*///	{ 0x0080, 0x07ff, MWA_ROM },
    /*TODO*///	{ -1 }	/* end of table */
        };

	static InputPortPtr input_ports_arkanoid = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_TILT );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );/* input from the 68705, some bootlegs need it to be 1 */
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* input from the 68705 */
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0xf8, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* IN2 - spinner Player 1 */
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL, 30, 15, 0, 0);
	
		PORT_START();       /* IN3 - spinner Player 2  */
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_COCKTAIL, 30, 15, 0, 0);
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x01, 0x00, "Allow Continue" );
		PORT_DIPSETTING(    0x01, DEF_STR( "No") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x08, "Easy" );
		PORT_DIPSETTING(    0x00, "Hard" );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x10, "20000 60000" );
		PORT_DIPSETTING(    0x00, "20000" );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x20, "3" );
		PORT_DIPSETTING(    0x00, "5" );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x40, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_6C") );
	INPUT_PORTS_END(); }}; 
	
	/* These are the input ports of the real Japanese ROM set                        */
	/* 'Block' uses the these ones as well.	The Tayto bootleg is different			 */
	/*  in coinage and # of lives.                    								 */
	
	static InputPortPtr input_ports_arknoidj = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_TILT );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );/* input from the 68705, some bootlegs need it to be 1 */
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* input from the 68705 */
	
		PORT_START(); 	/* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0xf8, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* IN2 - spinner (multiplexed for player 1 and 2) */
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL, 30, 15, 0, 0);
	
		PORT_START();       /* IN3 - spinner Player 2  */
		PORT_ANALOG( 0xff, 0x00, IPT_DIAL | IPF_COCKTAIL, 30, 15, 0, 0);
	
		PORT_START(); 	/* DSW1 */
		PORT_DIPNAME( 0x01, 0x00, "Allow Continue" );
		PORT_DIPSETTING(    0x01, DEF_STR( "No") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x04, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x08, "Easy" );
		PORT_DIPSETTING(    0x00, "Hard" );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x10, "20000 60000" );
		PORT_DIPSETTING(    0x00, "20000?" );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x20, "3" );
		PORT_DIPSETTING(    0x00, "5" );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Cocktail") );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		4096,	/* 4096 characters */
		3,	/* 3 bits per pixel */
		new int[] { 2*4096*8*8, 4096*8*8, 0 },	/* the two bitplanes are separated */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8	/* every char takes 8 consecutive bytes */
	);
	
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,  0, 64 ),
		/* sprites use the same characters above, but are 16x8 */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
    
    	static AY8910interface ay8910_interface = new AY8910interface
	(
		1,	/* 1 chips */
		1500000,	/* 1.5 MHz ???? */
		new int[]{ 33 },
		new ReadHandlerPtr[]{ null },
		new ReadHandlerPtr[]{ input_port_4_r },
		new WriteHandlerPtr[]{ null },
		new WriteHandlerPtr[]{ null }
	);
        
        	static MachineDriver machine_driver_arkanoid = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				6000000,	/* 6 Mhz ?? */
				readmem,writemem,null,null,
				interrupt,1
			),
			new MachineCPU(
				CPU_M68705,
				500000,	/* .5 Mhz (don't know really how fast, but it doesn't need to even be this fast) */
				mcu_readmem,mcu_writemem,null,null,
				ignore_interrupt,1
			),
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		100, /* 100 CPU slices per second to synchronize between the MCU and the main CPU */
		arkanoid_init_machine,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		512, 512,
		arkanoid_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY,
		null,
		generic_vh_start,
		generic_vh_stop,
		arkanoid_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
                    new MachineSound
                    (
				SOUND_AY8910,
				ay8910_interface
                    )
		}
	);
    	static MachineDriver machine_driver_bootleg = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				6000000,	/* 6 Mhz ?? */
				boot_readmem,boot_writemem,null,null,
				interrupt,1
			),
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		512, 512,
		arkanoid_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY,
		null,
		generic_vh_start,
		generic_vh_stop,
		arkanoid_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
                    new MachineSound
                    (
				SOUND_AY8910,
				ay8910_interface
                    )
		}
	);
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_arkanoid = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "a75_01-1.rom", 0x0000, 0x8000, 0x5bcda3b0 );
		ROM_LOAD( "a75_11.rom",   0x8000, 0x8000, 0xeafd7191 );
	
		ROM_REGION( 0x0800, REGION_CPU2 );/* 8k for the microcontroller */
		ROM_LOAD( "arkanoid.uc",  0x0000, 0x0800, 0x515d77b6 );
	
		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "a75_03.rom",   0x00000, 0x8000, 0x038b74ba );
		ROM_LOAD( "a75_04.rom",   0x08000, 0x8000, 0x71fae199 );
		ROM_LOAD( "a75_05.rom",   0x10000, 0x8000, 0xc76374e2 );
	
		ROM_REGION( 0x0600, REGION_PROMS );
		ROM_LOAD( "07.bpr",       0x0000, 0x0200, 0x0af8b289 );/* red component */
		ROM_LOAD( "08.bpr",       0x0200, 0x0200, 0xabb002fb );/* green component */
		ROM_LOAD( "09.bpr",       0x0400, 0x0200, 0xa7c6c277 );/* blue component */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_arknoidu = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "a75-19.bin",   0x0000, 0x8000, 0xd3ad37d7 );
		ROM_LOAD( "a75-18.bin",   0x8000, 0x8000, 0xcdc08301 );
	
		ROM_REGION( 0x0800, REGION_CPU2 );/* 8k for the microcontroller */
		ROM_LOAD( "arknoidu.uc",  0x0000, 0x0800, BADCRC( 0xde518e47 ));
	
		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "a75_03.rom",   0x00000, 0x8000, 0x038b74ba );
		ROM_LOAD( "a75_04.rom",   0x08000, 0x8000, 0x71fae199 );
		ROM_LOAD( "a75_05.rom",   0x10000, 0x8000, 0xc76374e2 );
	
		ROM_REGION( 0x0600, REGION_PROMS );
		ROM_LOAD( "07.bpr",       0x0000, 0x0200, 0x0af8b289 );/* red component */
		ROM_LOAD( "08.bpr",       0x0200, 0x0200, 0xabb002fb );/* green component */
		ROM_LOAD( "09.bpr",       0x0400, 0x0200, 0xa7c6c277 );/* blue component */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_arknoidj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "a75-21.rom",   0x0000, 0x8000, 0xbf0455fc );
		ROM_LOAD( "a75-22.rom",   0x8000, 0x8000, 0x3a2688d3 );
	
		ROM_REGION( 0x0800, REGION_CPU2 );/* 8k for the microcontroller */
		ROM_LOAD( "arknoidj.uc",  0x0000, 0x0800, BADCRC( 0x0a4abef6 ));
	
		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "a75_03.rom",   0x00000, 0x8000, 0x038b74ba );
		ROM_LOAD( "a75_04.rom",   0x08000, 0x8000, 0x71fae199 );
		ROM_LOAD( "a75_05.rom",   0x10000, 0x8000, 0xc76374e2 );
	
		ROM_REGION( 0x0600, REGION_PROMS );
		ROM_LOAD( "07.bpr",       0x0000, 0x0200, 0x0af8b289 );/* red component */
		ROM_LOAD( "08.bpr",       0x0200, 0x0200, 0xabb002fb );/* green component */
		ROM_LOAD( "09.bpr",       0x0400, 0x0200, 0xa7c6c277 );/* blue component */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_arkbl2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "e1.6d",        0x0000, 0x8000, 0xdd4f2b72 );
		ROM_LOAD( "e2.6f",        0x8000, 0x8000, 0xbbc33ceb );
	
		ROM_REGION( 0x0800, REGION_CPU2 );/* 8k for the microcontroller */
		ROM_LOAD( "68705p3.6i",   0x0000, 0x0800, 0x389a8cfb );
	
		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "a75_03.rom",   0x00000, 0x8000, 0x038b74ba );
		ROM_LOAD( "a75_04.rom",   0x08000, 0x8000, 0x71fae199 );
		ROM_LOAD( "a75_05.rom",   0x10000, 0x8000, 0xc76374e2 );
	
		ROM_REGION( 0x0600, REGION_PROMS );
		ROM_LOAD( "07.bpr",       0x0000, 0x0200, 0x0af8b289 );/* red component */
		ROM_LOAD( "08.bpr",       0x0200, 0x0200, 0xabb002fb );/* green component */
		ROM_LOAD( "09.bpr",       0x0400, 0x0200, 0xa7c6c277 );/* blue component */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_arkbl3 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "arkanunk.1",   0x0000, 0x8000, 0xb0f73900 );
		ROM_LOAD( "arkanunk.2",   0x8000, 0x8000, 0x9827f297 );
	
		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "a75_03.rom",   0x00000, 0x8000, 0x038b74ba );
		ROM_LOAD( "a75_04.rom",   0x08000, 0x8000, 0x71fae199 );
		ROM_LOAD( "a75_05.rom",   0x10000, 0x8000, 0xc76374e2 );
	
		ROM_REGION( 0x0600, REGION_PROMS );
		ROM_LOAD( "07.bpr",       0x0000, 0x0200, 0x0af8b289 );/* red component */
		ROM_LOAD( "08.bpr",       0x0200, 0x0200, 0xabb002fb );/* green component */
		ROM_LOAD( "09.bpr",       0x0400, 0x0200, 0xa7c6c277 );/* blue component */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_arkatayt = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "arkanoid.1",   0x0000, 0x8000, 0x6e0a2b6f );
		ROM_LOAD( "arkanoid.2",   0x8000, 0x8000, 0x5a97dd56 );
	
		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "a75_03.rom",   0x00000, 0x8000, 0x038b74ba );
		ROM_LOAD( "a75_04.rom",   0x08000, 0x8000, 0x71fae199 );
		ROM_LOAD( "a75_05.rom",   0x10000, 0x8000, 0xc76374e2 );
	
		ROM_REGION( 0x0600, REGION_PROMS );
		ROM_LOAD( "07.bpr",       0x0000, 0x0200, 0x0af8b289 );/* red component */
		ROM_LOAD( "08.bpr",       0x0200, 0x0200, 0xabb002fb );/* green component */
		ROM_LOAD( "09.bpr",       0x0400, 0x0200, 0xa7c6c277 );/* blue component */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_arkblock = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "block01.bin",  0x0000, 0x8000, 0x5be667e1 );
		ROM_LOAD( "block02.bin",  0x8000, 0x8000, 0x4f883ef1 );
	
		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "a75_03.rom",   0x00000, 0x8000, 0x038b74ba );
		ROM_LOAD( "a75_04.rom",   0x08000, 0x8000, 0x71fae199 );
		ROM_LOAD( "a75_05.rom",   0x10000, 0x8000, 0xc76374e2 );
	
		ROM_REGION( 0x0600, REGION_PROMS );
		ROM_LOAD( "07.bpr",       0x0000, 0x0200, 0x0af8b289 );/* red component */
		ROM_LOAD( "08.bpr",       0x0200, 0x0200, 0xabb002fb );/* green component */
		ROM_LOAD( "09.bpr",       0x0400, 0x0200, 0xa7c6c277 );/* blue component */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_arkbloc2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "ark-6.bin",    0x0000, 0x8000, 0x0be015de );
		ROM_LOAD( "arkgc.2",      0x8000, 0x8000, 0x9f0d4754 );
	
		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "a75_03.rom",   0x00000, 0x8000, 0x038b74ba );
		ROM_LOAD( "a75_04.rom",   0x08000, 0x8000, 0x71fae199 );
		ROM_LOAD( "a75_05.rom",   0x10000, 0x8000, 0xc76374e2 );
	
		ROM_REGION( 0x0600, REGION_PROMS );
		ROM_LOAD( "07.bpr",       0x0000, 0x0200, 0x0af8b289 );/* red component */
		ROM_LOAD( "08.bpr",       0x0200, 0x0200, 0xabb002fb );/* green component */
		ROM_LOAD( "09.bpr",       0x0400, 0x0200, 0xa7c6c277 );/* blue component */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_arkangc = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "arkgc.1",      0x0000, 0x8000, 0xc54232e6 );
		ROM_LOAD( "arkgc.2",      0x8000, 0x8000, 0x9f0d4754 );
	
		ROM_REGION( 0x18000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "a75_03.rom",   0x00000, 0x8000, 0x038b74ba );
		ROM_LOAD( "a75_04.rom",   0x08000, 0x8000, 0x71fae199 );
		ROM_LOAD( "a75_05.rom",   0x10000, 0x8000, 0xc76374e2 );
	
		ROM_REGION( 0x0600, REGION_PROMS );
		ROM_LOAD( "07.bpr",       0x0000, 0x0200, 0x0af8b289 );/* red component */
		ROM_LOAD( "08.bpr",       0x0200, 0x0200, 0xabb002fb );/* green component */
		ROM_LOAD( "09.bpr",       0x0400, 0x0200, 0xa7c6c277 );/* blue component */
	ROM_END(); }}; 

    	public static GameDriver driver_arkanoid  = new GameDriver("1986"	,"arkanoid"	,"arkanoid.java"	,rom_arkanoid,null	,machine_driver_arkanoid	,input_ports_arkanoid	,null	,ROT90	,	"Taito Corporation Japan", "Arkanoid (World)" );
	public static GameDriver driver_arknoidu  = new GameDriver("1986"	,"arknoidu"	,"arkanoid.java"	,rom_arknoidu,driver_arkanoid	,machine_driver_arkanoid	,input_ports_arkanoid	,null	,ROT90	,	"Taito America Corporation (Romstar license)", "Arkanoid (US)" );
	public static GameDriver driver_arknoidj  = new GameDriver("1986"	,"arknoidj"	,"arkanoid.java"	,rom_arknoidj,driver_arkanoid	,machine_driver_arkanoid	,input_ports_arknoidj	,null	,ROT90	,	"Taito Corporation", "Arkanoid (Japan)" );
    /*TODO*///	GAMEX(1986, arkbl2,   arkanoid, arkanoid, arknoidj, null, ROT90, "bootleg", "Arkanoid (Japanese bootleg Set 2)", GAME_NOT_WORKING )
    /*TODO*///	GAMEX(1986, arkbl3,   arkanoid, bootleg,  arknoidj, null, ROT90, "bootleg", "Arkanoid (Japanese bootleg Set 3)", GAME_NOT_WORKING )
	public static GameDriver driver_arkatayt   = new GameDriver("1986"	,"arkatayt"	,"arkanoid.java"	,rom_arkatayt,driver_arkanoid	,machine_driver_bootleg	,input_ports_arknoidj	,null	,ROT90	,	"bootleg", "Arkanoid (Tayto bootleg, Japanese)" );
    /*TODO*///	GAMEX(1986, arkblock, arkanoid, bootleg,  arknoidj, null, ROT90, "bootleg", "Block (bootleg, Japanese)", GAME_NOT_WORKING )
	public static GameDriver driver_arkbloc2   = new GameDriver("1986"	,"arkbloc2"	,"arkanoid.java"	,rom_arkbloc2,driver_arkanoid	,machine_driver_bootleg	,input_ports_arknoidj	,null	,ROT90	,	"bootleg", "Block (Game Corporation bootleg)" );
	public static GameDriver driver_arkangc	   = new GameDriver("1986"	,"arkangc"	,"arkanoid.java"	,rom_arkangc,driver_arkanoid	,machine_driver_bootleg	,input_ports_arknoidj	,null	,ROT90	,	"bootleg", "Arkanoid (Game Corporation bootleg)" ); 
}
