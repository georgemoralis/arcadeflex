package drivers;

import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.commonH.*;
import static mame.inputport.*;
import static mame.drawgfxH.*;
import static vidhrdw.generic.*;
import static mame.sndintrfH.*;
import static mame.cpuintrf.*;
import static mame.common.*;
import static arcadeflex.input.*;
import static mame.inputportH.*;
import static mame.inputH.*;
import static arcadeflex.libc.*;
import static arcadeflex.libc_old.*;
import static vidhrdw.cclimber.*;
import static sound.samplesH.*;
import static mame.memory.*;
import static machine.segacrpt.*;
import static sound.ay8910.*;
import static sound.ay8910H.*;
import static mame.mame.*;
import static sound.CustomSound.*;
import static sndhrdw.cclimber.*;

public class cclimber {
    public static InitMachinePtr cclimber_init_machine = new InitMachinePtr() { public void handler() 
    {
        /* Disable interrupts, River Patrol / Silver Land needs this */
        interrupt_enable_w.handler(0, 0);
    }};

	/* Note that River Patrol reads/writes to a000-a4f0. This is a bug in the code.
	   The instruction at 0x0593 should say LD DE,$8000 */
	
	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x5fff, MRA_ROM ),
		new MemoryReadAddress( 0x6000, 0x6bff, MRA_RAM ),	/* Crazy Kong only */
		new MemoryReadAddress( 0x8000, 0x83ff, MRA_RAM ),
		new MemoryReadAddress( 0x8800, 0x8bff, MRA_RAM ),
		new MemoryReadAddress( 0x9000, 0x93ff, MRA_RAM ),	/* video RAM */
		new MemoryReadAddress( 0x9800, 0x9bff, MRA_RAM ),	/* column scroll registers */
		new MemoryReadAddress( 0x9c00, 0x9fff, MRA_RAM ),	/* color RAM */
		new MemoryReadAddress( 0xa000, 0xa000, input_port_0_r ),     /* IN0 */
		new MemoryReadAddress( 0xa800, 0xa800, input_port_1_r ),     /* IN1 */
		new MemoryReadAddress( 0xb000, 0xb000, input_port_2_r ),     /* DSW */
		new MemoryReadAddress( 0xb800, 0xb800, input_port_3_r ),     /* IN2 */
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x5fff, MWA_ROM ),
		new MemoryWriteAddress( 0x6000, 0x6bff, MWA_RAM ),    /* Crazy Kong only */
		new MemoryWriteAddress( 0x8000, 0x83ff, MWA_RAM ),
		new MemoryWriteAddress( 0x8800, 0x88ff, cclimber_bigsprite_videoram_w, cclimber_bsvideoram, cclimber_bsvideoram_size ),
		new MemoryWriteAddress( 0x8900, 0x8bff, MWA_RAM ),  /* not used, but initialized */
		new MemoryWriteAddress( 0x9000, 0x93ff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( 0x9400, 0x97ff, videoram_w ), /* mirror address, used by Crazy Climber to draw windows */
		/* 9800-9bff and 9c00-9fff share the same RAM, interleaved */
		/* (9800-981f for scroll, 9c20-9c3f for color RAM, and so on) */
		new MemoryWriteAddress( 0x9800, 0x981f, MWA_RAM, cclimber_column_scroll ),
		new MemoryWriteAddress( 0x9880, 0x989f, MWA_RAM, spriteram, spriteram_size ),
		new MemoryWriteAddress( 0x98dc, 0x98df, MWA_RAM, cclimber_bigspriteram ),
		new MemoryWriteAddress( 0x9800, 0x9bff, MWA_RAM ),  /* not used, but initialized */
		new MemoryWriteAddress( 0x9c00, 0x9fff, cclimber_colorram_w, colorram ),
		new MemoryWriteAddress( 0xa000, 0xa000, interrupt_enable_w ),
		new MemoryWriteAddress( 0xa001, 0xa002, cclimber_flipscreen_w ),
		new MemoryWriteAddress( 0xa004, 0xa004, cclimber_sample_trigger_w ),
		new MemoryWriteAddress( 0xa800, 0xa800, cclimber_sample_rate_w ),
		new MemoryWriteAddress( 0xb000, 0xb000, cclimber_sample_volume_w ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static IOReadPort readport[] =
	{
		new IOReadPort( 0x0c, 0x0c, AY8910_read_port_0_r ),
		new IOReadPort( -1 )  /* end of table */
	};
	
	static IOWritePort writeport[] =
	{
		new IOWritePort( 0x08, 0x08, AY8910_control_port_0_w ),
		new IOWritePort( 0x09, 0x09, AY8910_write_port_0_w ),
		new IOWritePort( -1 )  /* end of table */
	};
        
 	static InputPortPtr input_ports_cclimber = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_UP     | IPF_8WAY );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_DOWN   | IPF_8WAY );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_LEFT   | IPF_8WAY );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_RIGHT  | IPF_8WAY );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_UP    | IPF_8WAY );
		PORT_BIT( 0x0020, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_DOWN  | IPF_8WAY );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_LEFT  | IPF_8WAY );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_RIGHT | IPF_8WAY );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_UP     | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_DOWN   | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_LEFT   | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_RIGHT  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_UP    | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x0020, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_DOWN  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_LEFT  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_RIGHT | IPF_8WAY | IPF_COCKTAIL );
	
		PORT_START();       /* DSW */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );
		PORT_DIPSETTING(    0x01, "4" );
		PORT_DIPSETTING(    0x02, "5" );
		PORT_DIPSETTING(    0x03, "6" );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "30000" );
		PORT_DIPSETTING(    0x04, "50000" );
		PORT_BITX(    0x08, 0x00, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Rack Test", KEYCODE_F1, IP_JOY_NONE );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x00, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x30, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x0020, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0xc0, 0x00, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "Free_Play") );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
		PORT_BIT( 0xe0, IP_ACTIVE_HIGH, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	/* several differences with cclimber: note that IN2 bits are ACTIVE_LOW, while in */
	/* cclimber they are ACTIVE_HIGH. */
	static InputPortPtr input_ports_ckong = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x07, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_4WAY );
		PORT_BIT( 0x0020, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_4WAY );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_4WAY );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x07, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x0020, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_4WAY | IPF_COCKTAIL );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL );
	
		PORT_START();       /* DSW */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );
		PORT_DIPSETTING(    0x01, "4" );
		PORT_DIPSETTING(    0x02, "5" );
		PORT_DIPSETTING(    0x03, "6" );
		PORT_DIPNAME( 0x0c, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00, "7000" );
		PORT_DIPSETTING(    0x04, "10000" );
		PORT_DIPSETTING(    0x08, "15000" );
		PORT_DIPSETTING(    0x0c, "20000" );
		PORT_DIPNAME( 0x70, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x70, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x0020, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_rpatrolb = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x3e, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY | IPF_COCKTAIL );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0x3e, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY );
	
		PORT_START();       /* DSW */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x0c, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );
		PORT_DIPSETTING(    0x04, "4" );
		PORT_DIPSETTING(    0x08, "5" );
		PORT_DIPSETTING(    0x0c, "6" );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x0020, 0x00, "Unknown 1" ); /* Probably unused */
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x0020, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, "Unknown 2" ); /* Probably unused */
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "Memory Test" );
		PORT_DIPSETTING(    0x00, "Retry on Error" );
		PORT_DIPSETTING(    0x80, "Stop on Error" );
	
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	

	static GfxLayout charlayout = new GfxLayout
	(
		8,8,    /* 8*8 characters */
		512,    /* 512 characters (256 in Crazy Climber) */
		2,      /* 2 bits per pixel */
		new int[] { 0, 512*8*8 }, /* the two bitplanes are separated */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },     /* pretty straightforward layout */
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8     /* every char takes 8 consecutive bytes */
	);
	static GfxLayout bscharlayout = new GfxLayout
	(
		8,8,    /* 8*8 characters */
		256,    /* 256 characters */
		2,      /* 2 bits per pixel */
		new int[] { 0, 256*8*8 }, /* the bitplanes are separated */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },     /* pretty straightforward layout */
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8     /* every char takes 8 consecutive bytes */
	);
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,  /* 16*16 sprites */
		128,    /* 128 sprites (64 in Crazy Climber) */
		2,      /* 2 bits per pixel */
		new int[] { 0, 128*16*16 },       /* the bitplanes are separated */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7,       /* pretty straightforward layout */
				8*8+0, 8*8+1, 8*8+2, 8*8+3, 8*8+4, 8*8+5, 8*8+6, 8*8+7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
				16*8, 17*8, 18*8, 19*8, 20*8, 21*8, 22*8, 23*8 },
		32*8    /* every sprite takes 32 consecutive bytes */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0x0000, charlayout,      0, 16 ), /* char set #1 */
		new GfxDecodeInfo( REGION_GFX1, 0x2000, charlayout,      0, 16 ), /* char set #2 */
		new GfxDecodeInfo( REGION_GFX2, 0x0000, bscharlayout, 16*4,  8 ), /* big sprite char set */
		new GfxDecodeInfo( REGION_GFX1, 0x0000, spritelayout,    0, 16 ), /* sprite set #1 */
		new GfxDecodeInfo( REGION_GFX1, 0x2000, spritelayout,    0, 16 ), /* sprite set #2 */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static AY8910interface ay8910_interface = new AY8910interface
	(
		1,      /* 1 chip */
		1536000,	/* 1.536 MHz */
		new int[]{ 50 },
		new ReadHandlerPtr[]{ null },
		new ReadHandlerPtr[]{ null },
		new WriteHandlerPtr[]{ cclimber_sample_select_w },
		new WriteHandlerPtr[]{ null }
	);
	
	static CustomSound_interface custom_interface = new CustomSound_interface
	(
		cclimber_sh_start,
		cclimber_sh_stop,
		null
	);
   	static MachineDriver machine_driver_cclimber = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				3072000,	/* 3.072 MHz */
				readmem,writemem,readport,writeport,
				nmi_interrupt,1
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,       /* frames per second, vblank duration */
		1,      /* single CPU, no need for interleaving */
		cclimber_init_machine,
	
		/* video hardware */
		32*8, 32*8, new rectangle(0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
		96,16*4+8*4,
		cclimber_vh_convert_color_prom,
	
		VIDEO_TYPE_RASTER,
		null,
		cclimber_vh_start,
		cclimber_vh_stop,
		cclimber_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
                    new MachineSound
                    (
				SOUND_AY8910,
				ay8910_interface
			),
			new MachineSound
                        (
				SOUND_CUSTOM,
				custom_interface
			)
		}
	);
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_cclimber = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2*0x10000, REGION_CPU1 );/* 64k for code + 64k for decrypted opcodes */
		ROM_LOAD( "cc11",         0x0000, 0x1000, 0x217ec4ff );
		ROM_LOAD( "cc10",         0x1000, 0x1000, 0xb3c26cef );
		ROM_LOAD( "cc09",         0x2000, 0x1000, 0x6db0879c );
		ROM_LOAD( "cc08",         0x3000, 0x1000, 0xf48c5fe3 );
		ROM_LOAD( "cc07",         0x4000, 0x1000, 0x3e873baf );
	
		ROM_REGION( 0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cc06",         0x0000, 0x0800, 0x481b64cc );
		/* empty hole - Crazy Kong has an additional ROM here */
		ROM_LOAD( "cc04",         0x1000, 0x0800, 0x332347cb );
		/* empty hole - Crazy Kong has an additional ROM here */
		ROM_LOAD( "cc05",         0x2000, 0x0800, 0x2c33b760 );
		/* empty hole - Crazy Kong has an additional ROM here */
		ROM_LOAD( "cc03",         0x3000, 0x0800, 0x4e4b3658 );
		/* empty hole - Crazy Kong has an additional ROM here */
	
		ROM_REGION( 0x1000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cc02",         0x0000, 0x0800, 0x14f3ecc9 );
		ROM_LOAD( "cc01",         0x0800, 0x0800, 0x21c0f9fb );
	
		ROM_REGION( 0x0060, REGION_PROMS );
		ROM_LOAD( "cclimber.pr1", 0x0000, 0x0020, 0x751c3325 );
		ROM_LOAD( "cclimber.pr2", 0x0020, 0x0020, 0xab1940fa );
		ROM_LOAD( "cclimber.pr3", 0x0040, 0x0020, 0x71317756 );
	
		ROM_REGION( 0x2000, REGION_SOUND1 );/* samples */
		ROM_LOAD( "cc13",         0x0000, 0x1000, 0xe0042f75 );
		ROM_LOAD( "cc12",         0x1000, 0x1000, 0x5da13aaa );
	ROM_END(); }}; 
        
	public static InitDriverPtr init_cclimber = new InitDriverPtr() { public void handler() 
	{
	/*
		translation mask is layed out like this:
	
		  0 1 2 3 4 5 6 7 8 9 a b c d e f
		0 <------A-----. <------A-----.
		1 <------B-----. <------B-----.
		2 <------A-----. <------A-----.
		3 <------B-----. <------B-----.
		4 <------C-----. <------C-----.
		5 <------D-----. <------D-----.
		6 <------C-----. <------C-----.
		7 <------D-----. <------D-----.
		8 <------E-----. <------E-----.
		9 <------F-----. <------F-----.
		a <------E-----. <------E-----.
		b <------F-----. <------F-----.
		c <------G-----. <------G-----.
		d <------H-----. <------H-----.
		e <------G-----. <------G-----.
		f <------H-----. <------H-----.
	
		Where <------A-----. etc. are groups of 8 unrelated values.
	
		therefore in the following table we only keep track of <--A-., <--B-. etc.
	*/
		char xortable[][] =
		{
			/* -1 marks spots which are unused and therefore unknown */
			{
				0x44,0x15,0x45,0x11,0x50,0x15,0x15,0x41,
				0x01,0x50,0x15,0x41,0x11,0x45,0x45,0x11,
				0x11,0x41,0x01,0x55,0x04,0x10,0x51,0x05,
				0x15,0x55,0x51,0x05,0x55,0x40,0x01,0x55,
				0x54,0x50,0x51,0x05,0x11,0x40,0x14,(char)-1,
				0x54,0x10,0x40,0x51,0x05,0x54,0x14,(char)-1,
				0x44,0x14,0x01,0x40,0x14,(char)-1,0x41,0x50,
				0x50,0x41,0x41,0x45,0x14,(char)-1,0x10,0x01
			},
			{
				0x44,0x11,0x04,0x50,0x11,0x50,0x41,0x05,
				0x10,0x50,0x54,0x01,0x54,0x44,(char)  -1,0x40,
				0x54,0x04,0x51,0x15,0x55,0x15,0x14,0x05,
				0x51,0x05,0x55,(char)-1,0x50,0x50,0x40,0x54,
				 (char)-1,0x55,(char)-1,(char)-1,0x10,0x55,0x50,0x04,
				0x41,0x10,0x05,0x51, (char) -1,0x55,0x51,0x54,
				0x01,0x51,0x11,0x45,0x44,0x10,0x14,0x40,
				0x55,0x15,0x41,0x15,0x45,0x10,0x44,0x41
			}
		};
		int A;
		UBytePtr rom = memory_region(REGION_CPU1);
		int diff = memory_region_length(REGION_CPU1) / 2;
	
	
		memory_set_opcode_base(0,new UBytePtr(rom,diff));
	
		for (A = 0x0000;A < 0x10000;A++)
		{
			int i,j;
			char src;
	
	
			src = rom.read(A);
	
			/* pick the translation table from bit 0 of the address */
			i = A & 1;
	
			/* pick the offset in the table from bits 012467 of the source data */
			j = (src & 0x07) + ((src & 0x10) >> 1) + ((src & 0xc0) >> 2);
	
			/* decode the opcodes */
			rom.write(A + diff,src ^ xortable[i][j]);
		}
	} };
	
	static RomLoadPtr rom_cclimbrj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2*0x10000, REGION_CPU1 );/* 64k for code + 64k for decrypted opcodes */
		ROM_LOAD( "cc11j.bin",    0x0000, 0x1000, 0x89783959 );
		ROM_LOAD( "cc10j.bin",    0x1000, 0x1000, 0x14eda506 );
		ROM_LOAD( "cc09j.bin",    0x2000, 0x1000, 0x26489069 );
		ROM_LOAD( "cc08j.bin",    0x3000, 0x1000, 0xb33c96f8 );
		ROM_LOAD( "cc07j.bin",    0x4000, 0x1000, 0xfbc9626c );
	
		ROM_REGION( 0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cc06",         0x0000, 0x0800, 0x481b64cc );
		/* empty hole - Crazy Kong has an additional ROM here */
		ROM_LOAD( "cc04",         0x1000, 0x0800, 0x332347cb );
		/* empty hole - Crazy Kong has an additional ROM here */
		ROM_LOAD( "cc05",         0x2000, 0x0800, 0x2c33b760 );
		/* empty hole - Crazy Kong has an additional ROM here */
		ROM_LOAD( "cc03",         0x3000, 0x0800, 0x4e4b3658 );
		/* empty hole - Crazy Kong has an additional ROM here */
	
		ROM_REGION( 0x1000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cc02",         0x0000, 0x0800, 0x14f3ecc9 );
		ROM_LOAD( "cc01",         0x0800, 0x0800, 0x21c0f9fb );
	
		ROM_REGION( 0x0060, REGION_PROMS );
		ROM_LOAD( "cclimber.pr1", 0x0000, 0x0020, 0x751c3325 );
		ROM_LOAD( "cclimber.pr2", 0x0020, 0x0020, 0xab1940fa );
		ROM_LOAD( "cclimber.pr3", 0x0040, 0x0020, 0x71317756 );
	
		ROM_REGION( 0x2000, REGION_SOUND1 );/* samples */
		ROM_LOAD( "cc13j.bin",    0x0000, 0x1000, 0x5f0bcdfb );
		ROM_LOAD( "cc12j.bin",    0x1000, 0x1000, 0x9003ffbd );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_ccboot = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2*0x10000, REGION_CPU1 );/* 64k for code + 64k for decrypted opcodes */
		ROM_LOAD( "m11.bin",      0x0000, 0x1000, 0x5efbe180 );
		ROM_LOAD( "m10.bin",      0x1000, 0x1000, 0xbe2748c7 );
		ROM_LOAD( "cc09j.bin",    0x2000, 0x1000, 0x26489069 );
		ROM_LOAD( "m08.bin",      0x3000, 0x1000, 0xe3c542d6 );
		ROM_LOAD( "cc07j.bin",    0x4000, 0x1000, 0xfbc9626c );
	
		ROM_REGION( 0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cc06",         0x0000, 0x0800, 0x481b64cc );
		/* empty hole - Crazy Kong has an additional ROM here */
		ROM_LOAD( "m04.bin",      0x1000, 0x0800, 0x6fb80538 );
		/* empty hole - Crazy Kong has an additional ROM here */
		ROM_LOAD( "m05.bin",      0x2000, 0x0800, 0x056af36b );
		/* empty hole - Crazy Kong has an additional ROM here */
		ROM_LOAD( "m03.bin",      0x3000, 0x0800, 0x67127253 );
		/* empty hole - Crazy Kong has an additional ROM here */
	
		ROM_REGION( 0x1000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "m02.bin",      0x0000, 0x0800, 0x7f4877de );
		ROM_LOAD( "m01.bin",      0x0800, 0x0800, 0x49fab908 );
	
		ROM_REGION( 0x0060, REGION_PROMS );
		ROM_LOAD( "cclimber.pr1", 0x0000, 0x0020, 0x751c3325 );
		ROM_LOAD( "cclimber.pr2", 0x0020, 0x0020, 0xab1940fa );
		ROM_LOAD( "cclimber.pr3", 0x0040, 0x0020, 0x71317756 );
	
		ROM_REGION( 0x2000, REGION_SOUND1 );/* samples */
		ROM_LOAD( "cc13j.bin",    0x0000, 0x1000, 0x5f0bcdfb );
		ROM_LOAD( "cc12j.bin",    0x1000, 0x1000, 0x9003ffbd );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_ccboot2 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2*0x10000, REGION_CPU1 );/* 64k for code + 64k for decrypted opcodes */
		ROM_LOAD( "11.4k",        0x0000, 0x1000, 0xb2b17e24 );
		ROM_LOAD( "10.4j",        0x1000, 0x1000, 0x8382bc0f );
		ROM_LOAD( "cc09j.bin",    0x2000, 0x1000, 0x26489069 );
		ROM_LOAD( "m08.bin",      0x3000, 0x1000, 0xe3c542d6 );
		ROM_LOAD( "cc07j.bin",    0x4000, 0x1000, 0xfbc9626c );
	
		ROM_REGION( 0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cc06",         0x0000, 0x0800, 0x481b64cc );
		/* empty hole - Crazy Kong has an additional ROM here */
		ROM_LOAD( "cc04",         0x1000, 0x0800, 0x332347cb );
		/* empty hole - Crazy Kong has an additional ROM here */
		ROM_LOAD( "cc05",         0x2000, 0x0800, 0x2c33b760 );
		/* empty hole - Crazy Kong has an additional ROM here */
		ROM_LOAD( "cc03",         0x3000, 0x0800, 0x4e4b3658 );
		/* empty hole - Crazy Kong has an additional ROM here */
	
		ROM_REGION( 0x1000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "cc02",         0x0000, 0x0800, 0x14f3ecc9 );
		ROM_LOAD( "cc01",         0x0800, 0x0800, 0x21c0f9fb );
	
		ROM_REGION( 0x0060, REGION_PROMS );
		ROM_LOAD( "cclimber.pr1", 0x0000, 0x0020, 0x751c3325 );
		ROM_LOAD( "cclimber.pr2", 0x0020, 0x0020, 0xab1940fa );
		ROM_LOAD( "cclimber.pr3", 0x0040, 0x0020, 0x71317756 );
	
		ROM_REGION( 0x2000, REGION_SOUND1 );/* samples */
		ROM_LOAD( "cc13j.bin",    0x0000, 0x1000, 0x5f0bcdfb );
		ROM_LOAD( "cc12j.bin",    0x1000, 0x1000, 0x9003ffbd );
	ROM_END(); }}; 
	
	public static InitDriverPtr init_cclimbrj = new InitDriverPtr() { public void handler() 
	{
	/*
		translation mask is layed out like this:
	
		  0 1 2 3 4 5 6 7 8 9 a b c d e f
		0 <------A-----. <------A-----.
		1 <------B-----. <------B-----.
		2 <------A-----. <------A-----.
		3 <------B-----. <------B-----.
		4 <------C-----. <------C-----.
		5 <------D-----. <------D-----.
		6 <------C-----. <------C-----.
		7 <------D-----. <------D-----.
		8 <------E-----. <------E-----.
		9 <------F-----. <------F-----.
		a <------E-----. <------E-----.
		b <------F-----. <------F-----.
		c <------G-----. <------G-----.
		d <------H-----. <------H-----.
		e <------G-----. <------G-----.
		f <------H-----. <------H-----.
	
		Where <------A-----. etc. are groups of 8 unrelated values.
	
		therefore in the following table we only keep track of <--A-., <--B-. etc.
	*/
		char xortable[][] =
		{
			{
				0x41,0x55,0x44,0x10,0x55,0x11,0x04,0x55,
				0x15,0x01,0x51,0x45,0x15,0x40,0x10,0x01,
				0x04,0x50,0x55,0x01,0x44,0x15,0x15,0x10,
				0x45,0x11,0x55,0x41,0x50,0x10,0x55,0x10,
				0x14,0x40,0x05,0x54,0x05,0x41,0x04,0x55,
				0x14,0x41,0x01,0x51,0x45,0x50,0x40,0x01,
				0x51,0x01,0x05,0x10,0x10,0x50,0x54,0x41,
				0x40,0x51,0x14,0x50,0x01,0x50,0x15,0x40
			},
			{
				0x50,0x10,0x10,0x51,0x44,0x50,0x50,0x50,
				0x41,0x05,0x11,0x55,0x51,0x11,0x54,0x11,
				0x14,0x54,0x54,0x50,0x54,0x40,0x44,0x04,
				0x14,0x50,0x15,0x44,0x54,0x14,0x05,0x50,
				0x01,0x04,0x55,0x51,0x45,0x40,0x11,0x15,
				0x44,0x41,0x11,0x15,0x41,0x05,0x55,0x51,
				0x51,0x54,0x05,0x01,0x15,0x51,0x41,0x45,
				0x14,0x11,0x41,0x45,0x50,0x55,0x05,0x01
			}
		};
		int A;
		UBytePtr rom = memory_region(REGION_CPU1);
		int diff = memory_region_length(REGION_CPU1) / 2;
	
	
		memory_set_opcode_base(0,new UBytePtr(rom,diff));
	
		for (A = 0x0000;A < 0x10000;A++)
		{
			int i,j;
			char src;
	
	
			src = rom.read(A);
	
			/* pick the translation table from bit 0 of the address */
			i = A & 1;
	
			/* pick the offset in the table from bits 012467 of the source data */
			j = (src & 0x07) + ((src & 0x10) >> 1) + ((src & 0xc0) >> 2);
	
			/* decode the opcodes */
			rom.write(A + diff,src ^ xortable[i][j]);
		}
	} };
	
	static RomLoadPtr rom_ckong = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "d05-07.bin",   0x0000, 0x1000, 0xb27df032 );
		ROM_LOAD( "f05-08.bin",   0x1000, 0x1000, 0x5dc1aaba );
		ROM_LOAD( "h05-09.bin",   0x2000, 0x1000, 0xc9054c94 );
		ROM_LOAD( "k05-10.bin",   0x3000, 0x1000, 0x069c4797 );
		ROM_LOAD( "l05-11.bin",   0x4000, 0x1000, 0xae159192 );
		ROM_LOAD( "n05-12.bin",   0x5000, 0x1000, 0x966bc9ab );
	
		ROM_REGION( 0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "n11-06.bin",   0x0000, 0x1000, 0x2dcedd12 );
		ROM_LOAD( "k11-04.bin",   0x1000, 0x1000, 0x3375b3bd );
		ROM_LOAD( "l11-05.bin",   0x2000, 0x1000, 0xfa7cbd91 );
		ROM_LOAD( "h11-03.bin",   0x3000, 0x1000, 0x5655cc11 );
	
		ROM_REGION( 0x1000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "c11-02.bin",   0x0000, 0x0800, 0xd1352c31 );
		ROM_LOAD( "a11-01.bin",   0x0800, 0x0800, 0xa7a2fdbd );
	
		ROM_REGION( 0x0060, REGION_PROMS );
		ROM_LOAD( "prom.v6",      0x0000, 0x0020, 0xb3fc1505 );
		ROM_LOAD( "prom.u6",      0x0020, 0x0020, 0x26aada9e );
		ROM_LOAD( "prom.t6",      0x0040, 0x0020, 0x676b3166 );
	
		ROM_REGION( 0x2000, REGION_SOUND1 );/* samples */
		ROM_LOAD( "cc13j.bin",    0x0000, 0x1000, 0x5f0bcdfb );
		ROM_LOAD( "cc12j.bin",    0x1000, 0x1000, 0x9003ffbd );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_ckonga = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "d05-07.bin",   0x0000, 0x1000, 0xb27df032 );
		ROM_LOAD( "f05-08.bin",   0x1000, 0x1000, 0x5dc1aaba );
		ROM_LOAD( "h05-09.bin",   0x2000, 0x1000, 0xc9054c94 );
		ROM_LOAD( "10.dat",       0x3000, 0x1000, 0xc3beb501 );
		ROM_LOAD( "l05-11.bin",   0x4000, 0x1000, 0xae159192 );
		ROM_LOAD( "n05-12.bin",   0x5000, 0x1000, 0x966bc9ab );
	
		ROM_REGION( 0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "n11-06.bin",   0x0000, 0x1000, 0x2dcedd12 );
		ROM_LOAD( "k11-04.bin",   0x1000, 0x1000, 0x3375b3bd );
		ROM_LOAD( "l11-05.bin",   0x2000, 0x1000, 0xfa7cbd91 );
		ROM_LOAD( "h11-03.bin",   0x3000, 0x1000, 0x5655cc11 );
	
		ROM_REGION( 0x1000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "c11-02.bin",   0x0000, 0x0800, 0xd1352c31 );
		ROM_LOAD( "a11-01.bin",   0x0800, 0x0800, 0xa7a2fdbd );
	
		ROM_REGION( 0x0060, REGION_PROMS );
		ROM_LOAD( "prom.v6",      0x0000, 0x0020, 0xb3fc1505 );
		ROM_LOAD( "prom.u6",      0x0020, 0x0020, 0x26aada9e );
		ROM_LOAD( "prom.t6",      0x0040, 0x0020, 0x676b3166 );
	
		ROM_REGION( 0x2000, REGION_SOUND1 );/* samples */
		ROM_LOAD( "cc13j.bin",    0x0000, 0x1000, 0x5f0bcdfb );
		ROM_LOAD( "cc12j.bin",    0x1000, 0x1000, 0x9003ffbd );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_ckongjeu = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "d05-07.bin",   0x0000, 0x1000, 0xb27df032 );
		ROM_LOAD( "f05-08.bin",   0x1000, 0x1000, 0x5dc1aaba );
		ROM_LOAD( "h05-09.bin",   0x2000, 0x1000, 0xc9054c94 );
		ROM_LOAD( "ckjeu10.dat",  0x3000, 0x1000, 0x7e6eeec4 );
		ROM_LOAD( "l05-11.bin",   0x4000, 0x1000, 0xae159192 );
		ROM_LOAD( "ckjeu12.dat",  0x5000, 0x1000, 0x0532f270 );
	
		ROM_REGION( 0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "n11-06.bin",   0x0000, 0x1000, 0x2dcedd12 );
		ROM_LOAD( "k11-04.bin",   0x1000, 0x1000, 0x3375b3bd );
		ROM_LOAD( "l11-05.bin",   0x2000, 0x1000, 0xfa7cbd91 );
		ROM_LOAD( "h11-03.bin",   0x3000, 0x1000, 0x5655cc11 );
	
		ROM_REGION( 0x1000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "c11-02.bin",   0x0000, 0x0800, 0xd1352c31 );
		ROM_LOAD( "a11-01.bin",   0x0800, 0x0800, 0xa7a2fdbd );
	
		ROM_REGION( 0x0060, REGION_PROMS );
		ROM_LOAD( "prom.v6",      0x0000, 0x0020, 0xb3fc1505 );
		ROM_LOAD( "prom.u6",      0x0020, 0x0020, 0x26aada9e );
		ROM_LOAD( "prom.t6",      0x0040, 0x0020, 0x676b3166 );
	
		ROM_REGION( 0x2000, REGION_SOUND1 );/* samples */
		ROM_LOAD( "cc13j.bin",    0x0000, 0x1000, 0x5f0bcdfb );
		ROM_LOAD( "cc12j.bin",    0x1000, 0x1000, 0x9003ffbd );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_ckongo = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "o55a-1",       0x0000, 0x1000, 0x8bfb4623 );
		ROM_LOAD( "o55a-2",       0x1000, 0x1000, 0x9ae8089b );
		ROM_LOAD( "o55a-3",       0x2000, 0x1000, 0xe82b33c8 );
		ROM_LOAD( "o55a-4",       0x3000, 0x1000, 0xf038f941 );
		ROM_LOAD( "o55a-5",       0x4000, 0x1000, 0x5182db06 );
		/* no ROM at 5000 */
	
		ROM_REGION( 0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		/* same as ckong but with halves switched */
		ROM_LOAD( "o50b-1",       0x0000, 0x0800, 0xcae9e2bf );
		ROM_CONTINUE(             0x2000, 0x0800 );
		ROM_LOAD( "o50b-2",       0x0800, 0x0800, 0xfba82114 );
		ROM_CONTINUE(             0x2800, 0x0800 );
		ROM_LOAD( "o50b-3",       0x1000, 0x0800, 0x1714764b );
		ROM_CONTINUE(             0x3000, 0x0800 );
		ROM_LOAD( "o50b-4",       0x1800, 0x0800, 0xb7008b57 );
		ROM_CONTINUE(             0x3800, 0x0800 );
	
		ROM_REGION( 0x1000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "c11-02.bin",   0x0000, 0x0800, 0xd1352c31 );
		ROM_LOAD( "a11-01.bin",   0x0800, 0x0800, 0xa7a2fdbd );
	
		ROM_REGION( 0x0060, REGION_PROMS );
		ROM_LOAD( "prom.v6",      0x0000, 0x0020, 0xb3fc1505 );
		ROM_LOAD( "prom.u6",      0x0020, 0x0020, 0x26aada9e );
		ROM_LOAD( "prom.t6",      0x0040, 0x0020, 0x676b3166 );
	
		ROM_REGION( 0x2000, REGION_SOUND1 );/* samples */
		ROM_LOAD( "cc13j.bin",    0x0000, 0x1000, 0x5f0bcdfb );
		ROM_LOAD( "cc12j.bin",    0x1000, 0x1000, 0x9003ffbd );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_ckongalc = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "ck7.bin",      0x0000, 0x1000, 0x2171cac3 );
		ROM_LOAD( "ck8.bin",      0x1000, 0x1000, 0x88b83ff7 );
		ROM_LOAD( "ck9.bin",      0x2000, 0x1000, 0xcff2af47 );
		ROM_LOAD( "ck10.bin",     0x3000, 0x1000, 0x520fa4de );
		ROM_LOAD( "ck11.bin",     0x4000, 0x1000, 0x327dcadf );
		/* no ROM at 5000 */
	
		ROM_REGION( 0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ck6.bin",      0x0000, 0x1000, 0xa8916dc8 );
		ROM_LOAD( "ck4.bin",      0x1000, 0x1000, 0xb62a0367 );
		ROM_LOAD( "ck5.bin",      0x2000, 0x1000, 0xcd3b5dde );
		ROM_LOAD( "ck3.bin",      0x3000, 0x1000, 0x61122c5e );
	
		ROM_REGION( 0x1000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ck2.bin",      0x0000, 0x0800, 0xf67c80f1 );
		ROM_LOAD( "ck1.bin",      0x0800, 0x0800, 0x80eb517d );
	
		ROM_REGION( 0x0060, REGION_PROMS );
		ROM_LOAD( "cclimber.pr1", 0x0000, 0x0020, 0x751c3325 );
		ROM_LOAD( "cclimber.pr2", 0x0020, 0x0020, 0xab1940fa );
		ROM_LOAD( "ck6t.bin",     0x0040, 0x0020, 0xb4e827a5 );
	
		ROM_REGION( 0x2000, REGION_SOUND1 );/* samples */
		ROM_LOAD( "cc13j.bin",    0x0000, 0x1000, 0x5f0bcdfb );
		ROM_LOAD( "ck12.bin",     0x1000, 0x1000, 0x2eb23b60 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_monkeyd = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "ck7.bin",      0x0000, 0x1000, 0x2171cac3 );
		ROM_LOAD( "ck8.bin",      0x1000, 0x1000, 0x88b83ff7 );
		ROM_LOAD( "ck9.bin",      0x2000, 0x1000, 0xcff2af47 );
		ROM_LOAD( "ck10.bin",     0x3000, 0x1000, 0x520fa4de );
		ROM_LOAD( "md5l.bin",     0x4000, 0x1000, 0xd1db1bb0 );
		/* no ROM at 5000 */
	
		ROM_REGION( 0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ck6.bin",      0x0000, 0x1000, 0xa8916dc8 );
		ROM_LOAD( "ck4.bin",      0x1000, 0x1000, 0xb62a0367 );
		ROM_LOAD( "ck5.bin",      0x2000, 0x1000, 0xcd3b5dde );
		ROM_LOAD( "ck3.bin",      0x3000, 0x1000, 0x61122c5e );
	
		ROM_REGION( 0x1000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ck2.bin",      0x0000, 0x0800, 0xf67c80f1 );
		ROM_LOAD( "ck1.bin",      0x0800, 0x0800, 0x80eb517d );
	
		ROM_REGION( 0x0060, REGION_PROMS );
		ROM_LOAD( "cclimber.pr1", 0x0000, 0x0020, 0x00000000 );
		ROM_LOAD( "cclimber.pr2", 0x0020, 0x0020, 0x00000000 );
		ROM_LOAD( "ck6t.bin",     0x0040, 0x0020, 0x00000000 );
	
		ROM_REGION( 0x2000, REGION_SOUND1 );/* samples */
		ROM_LOAD( "cc13j.bin",    0x0000, 0x1000, 0x5f0bcdfb );
		ROM_LOAD( "ck12.bin",     0x1000, 0x1000, 0x2eb23b60 );
	ROM_END(); }}; 
	
	static RomLoadPtr rom_rpatrolb = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "rp1.4l",       0x0000, 0x1000, 0xbfd7ae7a );
		ROM_LOAD( "rp2.4j",       0x1000, 0x1000, 0x03f53340 );
		ROM_LOAD( "rp3.4f",       0x2000, 0x1000, 0x8fa300df );
		ROM_LOAD( "rp4.4e",       0x3000, 0x1000, 0x74a8f1f4 );
		ROM_LOAD( "rp5.4c",       0x4000, 0x1000, 0xd7ef6c87 );
		/* no ROM at 5000 */
	
		ROM_REGION( 0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "rp6.6n",       0x0000, 0x0800, 0x19f18e9e );
		/* 0800-0fff empty */
		ROM_LOAD( "rp8.6k",       0x1000, 0x0800, 0x008738c7 );
		/* 1800-1fff empty */
		ROM_LOAD( "rp7.6l",       0x2000, 0x0800, 0x07f2070d );
		/* 2800-2fff empty */
		ROM_LOAD( "rp9.6h",       0x3000, 0x0800, 0xea5aafca );
		/* 3800-3fff empty */
	
		ROM_REGION( 0x1000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "rp11.6c",      0x0000, 0x0800, 0x065651a5 );
		ROM_LOAD( "rp10.6a",      0x0800, 0x0800, 0x59747c31 );
	
		ROM_REGION( 0x0060, REGION_PROMS );
		ROM_LOAD( "bprom1.9n",    0x0000, 0x0020, 0xf9a2383b );
		ROM_LOAD( "bprom2.9p",    0x0020, 0x0020, 0x1743bd26 );
		ROM_LOAD( "bprom3.9c",    0x0040, 0x0020, 0xee03bc96 );
	
		/* no samples */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_silvland = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "7.2r",         0x0000, 0x1000, 0x57e6be62 );
		ROM_LOAD( "8.1n",         0x1000, 0x1000, 0xbbb2b287 );
		ROM_LOAD( "rp3.4f",       0x2000, 0x1000, 0x8fa300df );
		ROM_LOAD( "10.2n",        0x3000, 0x1000, 0x5536a65d );
		ROM_LOAD( "11.1r",        0x4000, 0x1000, 0x6f23f66f );
		ROM_LOAD( "12.2k",        0x5000, 0x1000, 0x26f1537c );
	
		ROM_REGION( 0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "6.6n",         0x0000, 0x0800, 0xaffb804f );
		/* 0800-0fff empty */
		ROM_LOAD( "4.6k",         0x1000, 0x0800, 0xe487579d );
		/* 1800-1fff empty */
		ROM_LOAD( "5.6l",         0x2000, 0x0800, 0xad4642e5 );
		/* 2800-2fff empty */
		ROM_LOAD( "3.6h",         0x3000, 0x0800, 0x59125a1a );
		/* 3800-3fff empty */
	
		ROM_REGION( 0x1000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "2.6c",         0x0000, 0x0800, 0xc8d32b8e );
		ROM_LOAD( "1.6a",         0x0800, 0x0800, 0xee333daf );
	
		ROM_REGION( 0x0060, REGION_PROMS );
		ROM_LOAD( "mb7051.1v",    0x0000, 0x0020, 0x1d2343b1 );
		ROM_LOAD( "mb7051.1u",    0x0020, 0x0020, 0xc174753c );
		ROM_LOAD( "mb7051.1t",    0x0040, 0x0020, 0x04a1be01 );
	
		/* no samples */
	ROM_END(); }}; 

    /*TODO*////***************************************************************************
    /*TODO*///
    /*TODO*///  Swimmer driver
    /*TODO*///
    /*TODO*///***************************************************************************/
    /*TODO*///
    /*TODO*///void swimmer_bgcolor_w(int offset,int data);
    /*TODO*///void swimmer_palettebank_w(int offset,int data);
    /*TODO*///void swimmer_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom);
    /*TODO*///void swimmer_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh);
    /*TODO*///void swimmer_sidepanel_enable_w(int offset,int data);
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///void swimmer_sh_soundlatch_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	soundlatch_w(offset,data);
    /*TODO*///	cpu_cause_interrupt(1,0xff);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///static struct MemoryReadAddress swimmer_readmem[] =
    /*TODO*///{
    /*TODO*///	{ 0x0000, 0x7fff, MRA_ROM },
    /*TODO*///	{ 0x8000, 0x87ff, MRA_RAM },
    /*TODO*///	{ 0x9000, 0x93ff, MRA_RAM },
    /*TODO*///	{ 0x9400, 0x97ff, videoram_r }, /* mirror address (used by Swimmer) */
    /*TODO*///	{ 0x9c00, 0x9fff, MRA_RAM },
    /*TODO*///	{ 0xa000, 0xa000, input_port_0_r },
    /*TODO*///	{ 0xa800, 0xa800, input_port_1_r },
    /*TODO*///	{ 0xb000, 0xb000, input_port_2_r },
    /*TODO*///	{ 0xb800, 0xb800, input_port_3_r },
    /*TODO*///	{ 0xb880, 0xb880, input_port_4_r },
    /*TODO*///	{ 0xc000, 0xc7ff, MRA_RAM },    /* ??? used by Guzzler */
    /*TODO*///	{ 0xe000, 0xffff, MRA_ROM },    /* Guzzler only */
    /*TODO*///	{ -1 }  /* end of table */
    /*TODO*///};
    /*TODO*///
    /*TODO*///static struct MemoryWriteAddress swimmer_writemem[] =
    /*TODO*///{
    /*TODO*///	{ 0x0000, 0x7fff, MWA_ROM },
    /*TODO*///	{ 0x8000, 0x87ff, MWA_RAM },
    /*TODO*///	{ 0x8800, 0x88ff, cclimber_bigsprite_videoram_w, &cclimber_bsvideoram, &cclimber_bsvideoram_size },
    /*TODO*///	{ 0x8900, 0x89ff, cclimber_bigsprite_videoram_w },      /* mirror for the above (Guzzler writes to both) */
    /*TODO*///	{ 0x9000, 0x93ff, videoram_w, &videoram, &videoram_size },
    /*TODO*///	{ 0x9400, 0x97ff, videoram_w }, /* mirror address (used by Guzzler) */
    /*TODO*///	{ 0x9800, 0x981f, MWA_RAM, &cclimber_column_scroll },
    /*TODO*///	{ 0x9880, 0x989f, MWA_RAM, &spriteram, &spriteram_size },
    /*TODO*///	{ 0x98fc, 0x98ff, MWA_RAM, &cclimber_bigspriteram },
    /*TODO*///	{ 0x9c00, 0x9fff, cclimber_colorram_w, &colorram },
    /*TODO*///	{ 0xa000, 0xa000, interrupt_enable_w },
    /*TODO*///	{ 0xa001, 0xa002, cclimber_flipscreen_w },
    /*TODO*///	{ 0xa003, 0xa003, swimmer_sidepanel_enable_w },
    /*TODO*///	{ 0xa004, 0xa004, swimmer_palettebank_w },
    /*TODO*///	{ 0xa800, 0xa800, swimmer_sh_soundlatch_w },
    /*TODO*///	{ 0xb800, 0xb800, swimmer_bgcolor_w },  /* river color in Swimmer */
    /*TODO*///	{ 0xc000, 0xc7ff, MWA_RAM },    /* ??? used by Guzzler */
    /*TODO*///	{ 0xe000, 0xffff, MWA_ROM },    /* Guzzler only */
    /*TODO*///	{ -1 }  /* end of table */
    /*TODO*///};
    /*TODO*///
    /*TODO*///
    /*TODO*///static struct MemoryReadAddress sound_readmem[] =
    /*TODO*///{
    /*TODO*///	{ 0x0000, 0x0fff, MRA_ROM },
    /*TODO*///	{ 0x2000, 0x23ff, MRA_RAM },
    /*TODO*///	{ 0x3000, 0x3000, soundlatch_r },
    /*TODO*///	{ 0x4000, 0x4001, MRA_RAM },    /* ??? */
    /*TODO*///	{ -1 }  /* end of table */
    /*TODO*///};
    /*TODO*///
    /*TODO*///static struct MemoryWriteAddress sound_writemem[] =
    /*TODO*///{
    /*TODO*///	{ 0x0000, 0x0fff, MWA_ROM },
    /*TODO*///	{ 0x2000, 0x23ff, MWA_RAM },
    /*TODO*///	{ 0x4000, 0x4000, MWA_RAM },    /* ??? */
    /*TODO*///	{ -1 }  /* end of table */
    /*TODO*///};
    /*TODO*///
    /*TODO*///static struct IOWritePort sound_writeport[] =
    /*TODO*///{
    /*TODO*///	{ 0x00, 0x00, AY8910_write_port_0_w },
    /*TODO*///	{ 0x01, 0x01, AY8910_control_port_0_w },
    /*TODO*///	{ 0x80, 0x80, AY8910_write_port_1_w },
    /*TODO*///	{ 0x81, 0x81, AY8910_control_port_1_w },
    /*TODO*///	{ -1 }  /* end of table */
    /*TODO*///};
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///INPUT_PORTS_START( swimmer )
    /*TODO*///	PORT_START      /* IN0 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL )
    /*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL )
    /*TODO*///	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL )
    /*TODO*///	PORT_BIT( 0x0020, IP_ACTIVE_HIGH, IPT_UNKNOWN )   /* probably unused */
    /*TODO*///	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )   /* probably unused */
    /*TODO*///	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )   /* probably unused */
    /*TODO*///
    /*TODO*///	PORT_START      /* IN1 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_8WAY )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_8WAY )
    /*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_8WAY )
    /*TODO*///	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 )
    /*TODO*///	PORT_BIT( 0x0020, IP_ACTIVE_HIGH, IPT_UNKNOWN )   /* probably unused */
    /*TODO*///	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )   /* probably unused */
    /*TODO*///	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )   /* probably unused */
    /*TODO*///
    /*TODO*///	PORT_START      /* DSW1 */
    /*TODO*///	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Lives ) )
    /*TODO*///	PORT_DIPSETTING(    0x00, "3" )
    /*TODO*///	PORT_DIPSETTING(    0x01, "4" )
    /*TODO*///	PORT_DIPSETTING(    0x02, "5" )
    /*TODO*///	PORT_BITX( 0,       0x03, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite", IP_KEY_NONE, IP_JOY_NONE )
    /*TODO*///	PORT_DIPNAME( 0x0c, 0x00, DEF_STR( Bonus_Life ) )
    /*TODO*///	PORT_DIPSETTING(    0x00, "10000" )
    /*TODO*///	PORT_DIPSETTING(    0x04, "20000" )
    /*TODO*///	PORT_DIPSETTING(    0x08, "30000" )
    /*TODO*///	PORT_DIPSETTING(    0x0c, "None" )
    /*TODO*///	PORT_DIPNAME( 0x30, 0x00, DEF_STR( Coin_A ) )
    /*TODO*///	PORT_DIPSETTING(    0x10, DEF_STR( 2C_1C ) )
    /*TODO*///	PORT_DIPSETTING(    0x00, DEF_STR( 1C_1C ) )
    /*TODO*///	PORT_DIPSETTING(    0x0020, DEF_STR( 1C_2C ) )
    /*TODO*///	PORT_DIPSETTING(    0x30, DEF_STR( 1C_3C ) )
    /*TODO*///	PORT_DIPNAME( 0xc0, 0x00, DEF_STR( Coin_B ) )
    /*TODO*///	PORT_DIPSETTING(    0x00, DEF_STR( 1C_1C ) )
    /*TODO*///	PORT_DIPSETTING(    0x40, DEF_STR( 1C_2C ) )
    /*TODO*///	PORT_DIPSETTING(    0x80, DEF_STR( 1C_3C ) )
    /*TODO*///	PORT_DIPSETTING(    0xc0, DEF_STR( 1C_6C ) )
    /*TODO*///
    /*TODO*///	PORT_START      /* IN3/DSW2 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 )
    /*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_START2 )
    /*TODO*///	PORT_DIPNAME( 0x10, 0x10, DEF_STR( Cabinet ) )
    /*TODO*///	PORT_DIPSETTING(    0x10, DEF_STR( Upright ) )
    /*TODO*///	PORT_DIPSETTING(    0x00, DEF_STR( Cocktail ) )
    /*TODO*///	PORT_DIPNAME( 0x0020, 0x0020, DEF_STR( Demo_Sounds ) )
    /*TODO*///	PORT_DIPSETTING(    0x00, DEF_STR( Off ) )
    /*TODO*///	PORT_DIPSETTING(    0x0020, DEF_STR( On ) )
    /*TODO*///	PORT_DIPNAME( 0xc0, 0x80, DEF_STR( Difficulty ) )
    /*TODO*///	PORT_DIPSETTING(    0x00, "Easy" )
    /*TODO*///	PORT_DIPSETTING(    0x40, "???" )
    /*TODO*///	PORT_DIPSETTING(    0x80, "Normal" )
    /*TODO*///	PORT_DIPSETTING(    0xc0, "Hard" )
    /*TODO*///
    /*TODO*///	PORT_START      /* IN4 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_COIN2 )
    /*TODO*///	PORT_BIT( 0xfc, IP_ACTIVE_HIGH, IPT_UNKNOWN )
    /*TODO*///INPUT_PORTS_END
    /*TODO*///
    /*TODO*///INPUT_PORTS_START( guzzler )
    /*TODO*///	PORT_START      /* IN0 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_4WAY | IPF_COCKTAIL )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_4WAY | IPF_COCKTAIL )
    /*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_4WAY | IPF_COCKTAIL )
    /*TODO*///	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL )
    /*TODO*///	PORT_BIT( 0x0020, IP_ACTIVE_HIGH, IPT_UNKNOWN )   /* probably unused */
    /*TODO*///	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )   /* probably unused */
    /*TODO*///	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )   /* probably unused */
    /*TODO*///
    /*TODO*///	PORT_START      /* IN1 */
    /*TODO*///	PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY )
    /*TODO*///	PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_4WAY )
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_4WAY )
    /*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_4WAY )
    /*TODO*///	PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 )
    /*TODO*///	PORT_BIT( 0x0020, IP_ACTIVE_HIGH, IPT_UNKNOWN )   /* probably unused */
    /*TODO*///	PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN )   /* probably unused */
    /*TODO*///	PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN )   /* probably unused */
    /*TODO*///
    /*TODO*///	PORT_START      /* DSW0 */
    /*TODO*///	PORT_DIPNAME( 0x03, 0x00, DEF_STR( Lives ) )
    /*TODO*///	PORT_DIPSETTING(    0x00, "3" )
    /*TODO*///	PORT_DIPSETTING(    0x01, "4" )
    /*TODO*///	PORT_DIPSETTING(    0x02, "5" )
    /*TODO*///	PORT_BITX( 0,       0x03, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "64", IP_KEY_NONE, IP_JOY_NONE )
    /*TODO*///	PORT_DIPNAME( 0x0c, 0x00, DEF_STR( Bonus_Life ) )
    /*TODO*///	PORT_DIPSETTING(    0x04, "20000 50000" )
    /*TODO*///	PORT_DIPSETTING(    0x00, "30000 100000" )
    /*TODO*///	PORT_DIPSETTING(    0x08, "30000" )
    /*TODO*///	PORT_DIPSETTING(    0x0c, "None" )
    /*TODO*///	PORT_DIPNAME( 0x30, 0x00, DEF_STR( Coin_A ) )
    /*TODO*///	PORT_DIPSETTING(    0x10, DEF_STR( 2C_1C ) )
    /*TODO*///	PORT_DIPSETTING(    0x00, DEF_STR( 1C_1C ) )
    /*TODO*///	PORT_DIPSETTING(    0x0020, DEF_STR( 1C_2C ) )
    /*TODO*///	PORT_DIPSETTING(    0x30, DEF_STR( 1C_3C ) )
    /*TODO*///	PORT_DIPNAME( 0xc0, 0x00, DEF_STR( Coin_B ) )
    /*TODO*///	PORT_DIPSETTING(    0x00, DEF_STR( 1C_1C ) )
    /*TODO*///	PORT_DIPSETTING(    0x40, DEF_STR( 1C_2C ) )
    /*TODO*///	PORT_DIPSETTING(    0x80, DEF_STR( 1C_3C ) )
    /*TODO*///	PORT_DIPSETTING(    0xc0, DEF_STR( 1C_6C ) )
    /*TODO*///
    /*TODO*///	PORT_START      /* DSW1 */
    /*TODO*///	PORT_BIT( 0x0f, IP_ACTIVE_HIGH, IPT_UNKNOWN )     /* probably unused */
    /*TODO*///	PORT_DIPNAME( 0x10, 0x10, DEF_STR( Cabinet ) )
    /*TODO*///	PORT_DIPSETTING(    0x10, DEF_STR( Upright ) )
    /*TODO*///	PORT_DIPSETTING(    0x00, DEF_STR( Cocktail ) )
    /*TODO*///	PORT_DIPNAME( 0x0020, 0x00, "High Score Names" )
    /*TODO*///	PORT_DIPSETTING(    0x0020, "3 Letters" )
    /*TODO*///	PORT_DIPSETTING(    0x00, "10 Letters" )
    /*TODO*///	PORT_DIPNAME( 0xc0, 0x00, DEF_STR( Difficulty ) )
    /*TODO*///	PORT_DIPSETTING(    0x00, "Easy" )
    /*TODO*///	PORT_DIPSETTING(    0x40, "Medium" )
    /*TODO*///	PORT_DIPSETTING(    0x80, "Hard" )
    /*TODO*///	PORT_DIPSETTING(    0xc0, "Hardest" )
    /*TODO*///
    /*TODO*///	PORT_START      /* coin */
    /*TODO*///	PORT_BIT_IMPULSE( 0x01, IP_ACTIVE_HIGH, IPT_COIN1, 2)
    /*TODO*///	PORT_BIT_IMPULSE( 0x02, IP_ACTIVE_HIGH, IPT_COIN2, 2)
    /*TODO*///	PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 )
    /*TODO*///	PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_START2 )
    /*TODO*///	PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNKNOWN )     /* probably unused */
    /*TODO*///INPUT_PORTS_END
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///static struct GfxLayout swimmer_charlayout =
    /*TODO*///{
    /*TODO*///	8,8,    /* 8*8 characters */
    /*TODO*///	512,    /* 512 characters */
    /*TODO*///	3,      /* 3 bits per pixel */
    /*TODO*///	{ 0, 512*8*8, 512*2*8*8 },      /* the bitplanes are separated */
    /*TODO*///	{ 0, 1, 2, 3, 4, 5, 6, 7 },	     /* characters are upside down */
    /*TODO*///	{ 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
    /*TODO*///	8*8     /* every char takes 8 consecutive bytes */
    /*TODO*///};
    /*TODO*///
    /*TODO*///static struct GfxLayout swimmer_spritelayout =
    /*TODO*///{
    /*TODO*///	16,16,  /* 16*16 sprites */
    /*TODO*///	128,    /* 128 sprites */
    /*TODO*///	3,	      /* 3 bits per pixel */
    /*TODO*///	{ 0, 128*16*16, 128*2*16*16 },  /* the bitplanes are separated */
    /*TODO*///	{ 0, 1, 2, 3, 4, 5, 6, 7,       /* pretty straightforward layout */
    /*TODO*///			8*8+0, 8*8+1, 8*8+2, 8*8+3, 8*8+4, 8*8+5, 8*8+6, 8*8+7 },
    /*TODO*///	{ 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
    /*TODO*///			16*8, 17*8, 18*8, 19*8, 20*8, 21*8, 22*8, 23*8 },
    /*TODO*///	32*8    /* every sprite takes 32 consecutive bytes */
    /*TODO*///};
    /*TODO*///
    /*TODO*///static struct GfxDecodeInfo swimmer_gfxdecodeinfo[] =
    /*TODO*///{
    /*TODO*///	{ REGION_GFX1, 0, &swimmer_charlayout,      0, 64 }, /* characters */
    /*TODO*///	{ REGION_GFX1, 0, &swimmer_spritelayout,    0, 32 }, /* sprite set #1 */
    /*TODO*///	{ REGION_GFX2, 0, &swimmer_charlayout,   64*8, 4 },  /* big sprite set */
    /*TODO*///	{ -1 } /* end of array */
    /*TODO*///};
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///static struct AY8910interface swimmer_ay8910_interface =
    /*TODO*///{
    /*TODO*///	2,      /* 2 chips */
    /*TODO*///	4000000/2,	/* 2 MHz */
    /*TODO*///	{ 25, 25 },
    /*TODO*///	{ 0 },
    /*TODO*///	{ 0 },
    /*TODO*///	{ 0 },
    /*TODO*///	{ 0 }
    /*TODO*///};
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///static struct MachineDriver machine_driver_swimmer =
    /*TODO*///{
    /*TODO*///	/* basic machine hardware */
    /*TODO*///	{
    /*TODO*///		{
    /*TODO*///			CPU_Z80,
    /*TODO*///			3072000,	/* 3.072 MHz */
    /*TODO*///			swimmer_readmem,swimmer_writemem,0,0,
    /*TODO*///			nmi_interrupt,1
    /*TODO*///		},
    /*TODO*///		{
    /*TODO*///			CPU_Z80 | CPU_AUDIO_CPU,
    /*TODO*///			4000000/2,	/* 2 MHz */
    /*TODO*///			sound_readmem,sound_writemem,0,sound_writeport,
    /*TODO*///			0,0,
    /*TODO*///			nmi_interrupt,4000000/16384 /* IRQs are triggered by the main CPU */
    /*TODO*///		}
    /*TODO*///	},
    /*TODO*///	60, DEFAULT_60HZ_VBLANK_DURATION,       /* frames per second, vblank duration */
    /*TODO*///	1,      /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
    /*TODO*///	0,
    /*TODO*///
    /*TODO*///	/* video hardware */
    /*TODO*///	32*8, 32*8, { 0*8, 32*8-1, 2*8, 30*8-1 },
    /*TODO*///	swimmer_gfxdecodeinfo,
    /*TODO*///	256+32+2,64*8+4*8,
    /*TODO*///	swimmer_vh_convert_color_prom,
    /*TODO*///
    /*TODO*///	VIDEO_TYPE_RASTER|VIDEO_MODIFIES_PALETTE,
    /*TODO*///	0,
    /*TODO*///	cclimber_vh_start,
    /*TODO*///	cclimber_vh_stop,
    /*TODO*///	swimmer_vh_screenrefresh,
    /*TODO*///
    /*TODO*///	/* sound hardware */
    /*TODO*///	0,0,0,0,
    /*TODO*///	{
    /*TODO*///		{
    /*TODO*///			SOUND_AY8910,
    /*TODO*///			&swimmer_ay8910_interface
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///};
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///ROM_START( swimmer )
    /*TODO*///	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
    /*TODO*///	ROM_LOAD( "sw1",          0x0000, 0x1000, 0xf12481e7 )
    /*TODO*///	ROM_LOAD( "sw2",          0x1000, 0x1000, 0xa0b6fdd2 )
    /*TODO*///	ROM_LOAD( "sw3",          0x2000, 0x1000, 0xec93d7de )
    /*TODO*///	ROM_LOAD( "sw4",          0x3000, 0x1000, 0x0107927d )
    /*TODO*///	ROM_LOAD( "sw5",          0x4000, 0x1000, 0xebd8a92c )
    /*TODO*///	ROM_LOAD( "sw6",          0x5000, 0x1000, 0xf8539821 )
    /*TODO*///	ROM_LOAD( "sw7",          0x6000, 0x1000, 0x37efb64e )
    /*TODO*///	ROM_LOAD( "sw8",          0x7000, 0x1000, 0x33d6001e )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x10000, REGION_CPU2 )     /* 64k for sound board */
    /*TODO*///	ROM_LOAD( "sw12",         0x0000, 0x1000, 0x2eee9bcb )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x3000, REGION_GFX1 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "sw15",         0x0000, 0x1000, 0x4f3608cb )  /* chars */
    /*TODO*///	ROM_LOAD( "sw14",         0x1000, 0x1000, 0x7181c8b4 )
    /*TODO*///	ROM_LOAD( "sw13",         0x2000, 0x1000, 0x2eb1af5c )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x3000, REGION_GFX2 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "sw23",         0x0000, 0x0800, 0x9ca67e24 )  /* bigsprite data */
    /*TODO*///	ROM_RELOAD(               0x0800, 0x0800 )	/* Guzzler has larger ROMs */
    /*TODO*///	ROM_LOAD( "sw22",         0x1000, 0x0800, 0x02c10992 )
    /*TODO*///	ROM_RELOAD(               0x1800, 0x0800 )	/* Guzzler has larger ROMs */
    /*TODO*///	ROM_LOAD( "sw21",         0x2000, 0x0800, 0x7f4993c1 )
    /*TODO*///	ROM_RELOAD(               0x2800, 0x0800 )	/* Guzzler has larger ROMs */
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x0220, REGION_PROMS )
    /*TODO*///	ROM_LOAD( "8220.clr",     0x0000, 0x100, 0x72c487ed )
    /*TODO*///	ROM_LOAD( "8212.clr",     0x0100, 0x100, 0x39037799 )
    /*TODO*///	ROM_LOAD( "8221.clr",     0x0200, 0x020, 0x3b2deb3a )
    /*TODO*///ROM_END
    /*TODO*///
    /*TODO*///ROM_START( swimmera )
    /*TODO*///	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
    /*TODO*///	ROM_LOAD( "swa1",         0x0000, 0x1000, 0x42c2b6c5 )
    /*TODO*///	ROM_LOAD( "swa2",         0x1000, 0x1000, 0x49bac195 )
    /*TODO*///	ROM_LOAD( "swa3",         0x2000, 0x1000, 0xa6d8cb01 )
    /*TODO*///	ROM_LOAD( "swa4",         0x3000, 0x1000, 0x7be75182 )
    /*TODO*///	ROM_LOAD( "swa5",         0x4000, 0x1000, 0x78f79573 )
    /*TODO*///	ROM_LOAD( "swa6",         0x5000, 0x1000, 0xfda9b311 )
    /*TODO*///	ROM_LOAD( "swa7",         0x6000, 0x1000, 0x7090e5ee )
    /*TODO*///	ROM_LOAD( "swa8",         0x7000, 0x1000, 0xab86efa9 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x10000, REGION_CPU2 )     /* 64k for sound board */
    /*TODO*///	ROM_LOAD( "sw12",         0x0000, 0x1000, 0x2eee9bcb )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x3000, REGION_GFX1 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "sw15",         0x0000, 0x1000, 0x4f3608cb )  /* chars */
    /*TODO*///	ROM_LOAD( "sw14",         0x1000, 0x1000, 0x7181c8b4 )
    /*TODO*///	ROM_LOAD( "sw13",         0x2000, 0x1000, 0x2eb1af5c )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x3000, REGION_GFX2 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "sw23",         0x0000, 0x0800, 0x9ca67e24 )  /* bigsprite data */
    /*TODO*///	ROM_RELOAD(               0x0800, 0x0800 )	/* Guzzler has larger ROMs */
    /*TODO*///	ROM_LOAD( "sw22",         0x1000, 0x0800, 0x02c10992 )
    /*TODO*///	ROM_RELOAD(               0x1800, 0x0800 )	/* Guzzler has larger ROMs */
    /*TODO*///	ROM_LOAD( "sw21",         0x2000, 0x0800, 0x7f4993c1 )
    /*TODO*///	ROM_RELOAD(               0x2800, 0x0800 )	/* Guzzler has larger ROMs */
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x0220, REGION_PROMS )
    /*TODO*///	ROM_LOAD( "8220.clr",     0x0000, 0x100, 0x72c487ed )
    /*TODO*///	ROM_LOAD( "8212.clr",     0x0100, 0x100, 0x39037799 )
    /*TODO*///	ROM_LOAD( "8221.clr",     0x0200, 0x020, 0x3b2deb3a )
    /*TODO*///ROM_END
    /*TODO*///
    /*TODO*///ROM_START( guzzler )
    /*TODO*///	ROM_REGION( 0x10000, REGION_CPU1 )     /* 64k for code */
    /*TODO*///	ROM_LOAD( "guzz-01.bin",  0x0000, 0x2000, 0x58aaa1e9 )
    /*TODO*///	ROM_LOAD( "guzz-02.bin",  0x2000, 0x2000, 0xf80ceb17 )
    /*TODO*///	ROM_LOAD( "guzz-03.bin",  0x4000, 0x2000, 0xe63c65a2 )
    /*TODO*///	ROM_LOAD( "guzz-04.bin",  0x6000, 0x2000, 0x45be42f5 )
    /*TODO*///	ROM_LOAD( "guzz-16.bin",  0xe000, 0x2000, 0x61ee00b7 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x10000, REGION_CPU2 )     /* 64k for sound board */
    /*TODO*///	ROM_LOAD( "guzz-12.bin",  0x0000, 0x1000, 0xf3754d9e )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x3000, REGION_GFX1 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "guzz-13.bin",  0x0000, 0x1000, 0xafc464e2 )   /* chars */
    /*TODO*///	ROM_LOAD( "guzz-14.bin",  0x1000, 0x1000, 0xacbdfe1f )
    /*TODO*///	ROM_LOAD( "guzz-15.bin",  0x2000, 0x1000, 0x66978c05 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x3000, REGION_GFX2 | REGIONFLAG_DISPOSE )
    /*TODO*///	ROM_LOAD( "guzz-11.bin",  0x0000, 0x1000, 0xec2e9d86 )   /* big sprite */
    /*TODO*///	ROM_LOAD( "guzz-10.bin",  0x1000, 0x1000, 0xbd3f0bf7 )
    /*TODO*///	ROM_LOAD( "guzz-09.bin",  0x2000, 0x1000, 0x18927579 )
    /*TODO*///
    /*TODO*///	ROM_REGION( 0x0220, REGION_PROMS )
    /*TODO*///	ROM_LOAD( "guzzler.003",  0x0000, 0x100, 0xf86930c1 )
    /*TODO*///	ROM_LOAD( "guzzler.002",  0x0100, 0x100, 0xb566ea9e )
    /*TODO*///	ROM_LOAD( "guzzler.001",  0x0200, 0x020, 0x69089495 )
    /*TODO*///ROM_END
    /*TODO*///
        public static GameDriver driver_cclimber	   = new GameDriver("1980"	,"cclimber"	,"cclimber.java"	,rom_cclimber,null	,machine_driver_cclimber	,input_ports_cclimber	,init_cclimber	,ROT0	,	"Nichibutsu", "Crazy Climber (US)" );
	public static GameDriver driver_cclimbrj	   = new GameDriver("1980"	,"cclimbrj"	,"cclimber.java"	,rom_cclimbrj,driver_cclimber	,machine_driver_cclimber	,input_ports_cclimber	,init_cclimbrj	,ROT0	,	"Nichibutsu", "Crazy Climber (Japan)" );
	public static GameDriver driver_ccboot	   = new GameDriver("1980"	,"ccboot"	,"cclimber.java"	,rom_ccboot,driver_cclimber	,machine_driver_cclimber	,input_ports_cclimber	,init_cclimbrj	,ROT0	,	"bootleg", "Crazy Climber (bootleg set 1)" );
	public static GameDriver driver_ccboot2	   = new GameDriver("1980"	,"ccboot2"	,"cclimber.java"	,rom_ccboot2,driver_cclimber	,machine_driver_cclimber	,input_ports_cclimber	,init_cclimbrj	,ROT0	,	"bootleg", "Crazy Climber (bootleg set 2)" );
	public static GameDriver driver_ckong	   = new GameDriver("1981"	,"ckong"	,"cclimber.java"	,rom_ckong,null	,machine_driver_cclimber	,input_ports_ckong	,null	,ROT270	,	"Falcon", "Crazy Kong (set 1)" );
	public static GameDriver driver_ckonga	   = new GameDriver("1981"	,"ckonga"	,"cclimber.java"	,rom_ckonga,driver_ckong	,machine_driver_cclimber	,input_ports_ckong	,null	,ROT270	,	"Falcon", "Crazy Kong (set 2)" );
	public static GameDriver driver_ckongjeu	   = new GameDriver("1981"	,"ckongjeu"	,"cclimber.java"	,rom_ckongjeu,driver_ckong	,machine_driver_cclimber	,input_ports_ckong	,null	,ROT270	,	"bootleg", "Crazy Kong (Jeutel bootleg)" );
	public static GameDriver driver_ckongo	   = new GameDriver("1981"	,"ckongo"	,"cclimber.java"	,rom_ckongo,driver_ckong	,machine_driver_cclimber	,input_ports_ckong	,null	,ROT270	,	"bootleg", "Crazy Kong (Orca bootleg)" );
	public static GameDriver driver_ckongalc	   = new GameDriver("1981"	,"ckongalc"	,"cclimber.java"	,rom_ckongalc,driver_ckong	,machine_driver_cclimber	,input_ports_ckong	,null	,ROT270	,	"bootleg", "Crazy Kong (Alca bootleg)" );
	public static GameDriver driver_monkeyd	   = new GameDriver("1981"	,"monkeyd"	,"cclimber.java"	,rom_monkeyd,driver_ckong	,machine_driver_cclimber	,input_ports_ckong	,null	,ROT270	,	"bootleg", "Monkey Donkey" );
    /*TODO*///GAME( ????, rpatrolb, 0,        cclimber, rpatrolb, 0,        ROT0,   "bootleg", "River Patrol (bootleg)" )
    /*TODO*///GAME( ????, silvland, rpatrolb, cclimber, rpatrolb, 0,        ROT0,   "Falcon", "Silver Land" )
    /*TODO*///
    /*TODO*///GAME( 1982, swimmer,  0,       swimmer, swimmer, 0, ROT0,  "Tehkan", "Swimmer (set 1)" )
    /*TODO*///GAME( 1982, swimmera, swimmer, swimmer, swimmer, 0, ROT0,  "Tehkan", "Swimmer (set 2)" )
    /*TODO*///GAME( 1983, guzzler,  0,       swimmer, guzzler, 0, ROT90, "Tehkan", "Guzzler" )
    /*TODO*///    
}
