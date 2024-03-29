/******************************************************************

Shark Attack
(C) 1980 PACIFIC NOVELTY MFG. INC.

Thief
(C) 1981 PACIFIC NOVELTY MFG. INC.

NATO Defense
(C) 1982 PACIFIC NOVELTY MFG. INC.

Credits:
	Shark Driver by Victor Trucco and Mike Balfour
	Driver for Thief and NATO Defense by Phil Stroffolino

- 8255 emulation (ports 0x30..0x3f) could be better abstracted

- TMS9927 VTAC: do we need to emulate this?
	The video controller registers effect screen size (currently
	hard-coded on a per-game basis).

- minor blitting glitches in playfield of Thief (XOR vs copy?)

- Nato Defense gfx ROMs may be hooked up wrong;
	see screenshots from flyers

******************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class thief
{
	
	static UINT8 thief_input_select;
	
	extern extern extern extern extern extern extern extern extern extern extern extern 
	extern extern extern 
	
	public static InterruptPtr thief_interrupt = new InterruptPtr() { public int handler() 
	{
		/* SLAM switch causes an NMI if it's pressed */
		if( (input_port_3_r(0) & 0x10) == 0 )
			return nmi_interrupt();
	
		return interrupt();
	} };
	
	/**********************************************************/
	
	
	/*	Following is an attempt to simulate the behavior of the
	**	cassette tape used in several Pacific Novelty games.
	**
	**	It is a leaderless tape that is constructed so that it will
	**	loop continuously.  The IO controller can start and stop the
	**	tape player's motor, and enable/disable each of two audio
	**	tracks.
	*/
	
	enum
	{
		kTalkTrack, kCrashTrack
	};
	
	static void tape_set_audio( int track, int bOn )
	{
		sample_set_volume( track, bOn?100:0 );
	}
	
	static void tape_set_motor( int bOn )
	{
		if (bOn != 0)
		{
			sample_start( 0, 0, 1 );
			sample_start( 1, 1, 1 );
		}
		else
		{
			sample_stop( kTalkTrack );
			sample_stop( kCrashTrack );
		}
	}
	
	/***********************************************************/
	
	public static WriteHandlerPtr thief_input_select_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		thief_input_select = data;
	} };
	
	public static WriteHandlerPtr tape_control_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		switch( data )
		{
		case 0x02: /* coin meter on */
			break;
	
		case 0x03: /* nop */
			break;
	
		case 0x04: /* coin meter off */
			break;
	
		case 0x08: /* talk track on */
			tape_set_audio( kTalkTrack, 1 );
			break;
	
		case 0x09: /* talk track off */
			sample_set_volume( kTalkTrack, 0 );
			break;
	
		case 0x0a: /* motor on */
			tape_set_motor( 1 );
			break;
	
		case 0x0b: /* motor off */
			tape_set_motor( 0 );
			break;
	
		case 0x0c: /* crash track on */
			tape_set_audio( kCrashTrack, 1 );
			break;
	
		case 0x0d: /* crash track off */
			tape_set_audio( kCrashTrack, 0 );
			break;
		}
	} };
	
	public static ReadHandlerPtr thief_io_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch( thief_input_select )
		{
			case 0x01: return readinputport(0); /* dsw#1 */
			case 0x02: return readinputport(1); /* dsw#2 */
			case 0x04: return readinputport(2); /* inp#1 */
			case 0x08: return readinputport(3); /* inp#2 */
		}
		return 0x00;
	} };
	
	static MemoryReadAddress sharkatt_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0x8fff, MRA_RAM ),			/* 2114 (working RAM) */
		new MemoryReadAddress( 0xc000, 0xdfff, thief_videoram_r ),	/* 4116 */
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress sharkatt_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0x8000, 0x8fff, MWA_RAM ),			/* 2114 */
		new MemoryWriteAddress( 0xc000, 0xdfff, thief_videoram_w ),	/* 4116 */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress thief_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0x8fff, MRA_RAM ),			/* 2114 (working RAM) */
		new MemoryReadAddress( 0xa000, 0xafff, MRA_ROM ),			/* NATO Defense diagnostic ROM */
		new MemoryReadAddress( 0xc000, 0xdfff, thief_videoram_r ),	/* 4116 */
		new MemoryReadAddress( 0xe000, 0xe008, thief_coprocessor_r ),
		new MemoryReadAddress( 0xe010, 0xe02f, MRA_ROM ),
		new MemoryReadAddress( 0xe080, 0xe0bf, thief_context_ram_r ),
		new MemoryReadAddress( -1 )
	};
	
	static MemoryWriteAddress thief_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x0000, thief_blit_w ),
		new MemoryWriteAddress( 0x0001, 0x7fff, MWA_ROM ),
		new MemoryWriteAddress( 0x8000, 0x8fff, MWA_RAM ),			/* 2114 */
		new MemoryWriteAddress( 0xc000, 0xdfff, thief_videoram_w ),	/* 4116 */
		new MemoryWriteAddress( 0xe000, 0xe008, thief_coprocessor_w ),
		new MemoryWriteAddress( 0xe010, 0xe02f, MWA_ROM ),
		new MemoryWriteAddress( 0xe080, 0xe0bf, thief_context_ram_w ),
		new MemoryWriteAddress( 0xe0c0, 0xe0c0, thief_context_bank_w ),
		new MemoryWriteAddress( -1 )
	};
	
	static IOReadPort readport[] =
	{
		new IOReadPort( 0x31, 0x31, thief_io_r ), // 8255
		new IOReadPort( 0x41, 0x41, AY8910_read_port_0_r ),
		new IOReadPort( 0x43, 0x43, AY8910_read_port_1_r ),
		new IOReadPort( -1 )
	};
	
	static IOWritePort writeport[] =
	{
		new IOWritePort( 0x00, 0x00, MWA_NOP ), /* watchdog */
		new IOWritePort( 0x10, 0x10, thief_video_control_w ),
		new IOWritePort( 0x30, 0x30, thief_input_select_w ), // 8255
		new IOWritePort( 0x33, 0x33, tape_control_w ),
		new IOWritePort( 0x40, 0x40, AY8910_control_port_0_w ),
		new IOWritePort( 0x41, 0x41, AY8910_write_port_0_w ),
		new IOWritePort( 0x42, 0x42, AY8910_control_port_1_w ),
		new IOWritePort( 0x43, 0x43, AY8910_write_port_1_w ),
		new IOWritePort( 0x50, 0x50, thief_color_plane_w ),
		new IOWritePort( 0x60, 0x6f, thief_vtcsel_w ),
		new IOWritePort( 0x70, 0x7f, thief_color_map_w ),
		new IOWritePort( -1 )
	};
	
	
	
	/**********************************************************/
	
	static InputPortPtr input_ports_sharkatt = new InputPortPtr(){ public void handler() { 
	PORT_START();       /* IN0 */
		PORT_DIPNAME( 0x7f, 0x7f, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x7f, DEF_STR( "1C_1C") ); // if any are set
		PORT_SERVICE( 0x80, IP_ACTIVE_HIGH );
		PORT_START();       /* IN1 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x00, "3" );	PORT_DIPSETTING(	0x01, "4" );	PORT_DIPSETTING(	0x02, "5" );//	PORT_DIPSETTING(	0x03, "5" );	PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "No") );
		PORT_DIPSETTING(	0x04, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "No") );
		PORT_DIPSETTING(	0x08, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "No") );
		PORT_DIPSETTING(	0x10, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "No") );
		PORT_DIPSETTING(	0x20, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "No") );
		PORT_DIPSETTING(	0x40, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "No") );
		PORT_DIPSETTING(	0x80, DEF_STR( "Yes") );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_START();       /* IN3 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_thief = new InputPortPtr(){ public void handler() { 
	PORT_START(); 
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x0c, 0x000, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );	PORT_DIPSETTING(    0x04, "4" );	PORT_DIPSETTING(    0x08, "5" );	PORT_DIPSETTING(    0x0c, "7" );	PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Yes") );
	
		PORT_START(); 
		PORT_DIPNAME( 0x0f, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x00|0x0c, "10K" );	PORT_DIPSETTING(    0x01|0x0c, "20K" );	PORT_DIPSETTING(    0x02|0x0c, "30K" );	PORT_DIPSETTING(    0x03|0x0c, "40K" );	PORT_DIPSETTING(    0x00|0x08, "10K 10K" );	PORT_DIPSETTING(    0x01|0x08, "20K 20K" );	PORT_DIPSETTING(    0x02|0x08, "30K 30K" );	PORT_DIPSETTING(    0x03|0x08, "40K 40K" );	PORT_DIPSETTING(    0x00,      "None" );	PORT_DIPNAME( 0xf0, 0x00, "Mode" );	PORT_DIPSETTING(    0x00, "Normal" );	PORT_DIPSETTING(    0x70, "Display Options" );	PORT_DIPSETTING(    0x80|0x00, "Burn-in Test" );	PORT_DIPSETTING(    0x80|0x10, "Color Bar Test" );	PORT_DIPSETTING(    0x80|0x20, "Cross Hatch" );	PORT_DIPSETTING(    0x80|0x30, "Color Map" );	PORT_DIPSETTING(    0x80|0x40, "VIDSEL Test" );	PORT_DIPSETTING(    0x80|0x50, "VIDBIT Test" );	PORT_DIPSETTING(    0x80|0x60, "I/O Board Test" );	PORT_DIPSETTING(    0x80|0x70, "Reserved" );
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_4WAY );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_4WAY );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_4WAY );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_4WAY | IPF_COCKTAIL );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_4WAY | IPF_COCKTAIL );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_4WAY | IPF_COCKTAIL );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNUSED );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_natodef = new InputPortPtr(){ public void handler() { 
	PORT_START(); 
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x0c, 0x000, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "3" );	PORT_DIPSETTING(    0x04, "4" );	PORT_DIPSETTING(    0x08, "5" );	PORT_DIPSETTING(    0x0c, "7" );	PORT_DIPNAME( 0x30, 0x00, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x00, "Easy" );	PORT_DIPSETTING(    0x10, "Medium" );	PORT_DIPSETTING(    0x20, "Hard" );	PORT_DIPSETTING(    0x30, "Hardest" );	PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Cocktail") );
		PORT_DIPNAME( 0x80, 0x00, "Add a Coin?" );	PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Yes") );
	
		PORT_START(); 
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x0b, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x08, "10K" );	PORT_DIPSETTING(    0x09, "20K" );	PORT_DIPSETTING(    0x0a, "30K" );	PORT_DIPSETTING(    0x0b, "40K" );	PORT_DIPSETTING(    0x00, "None" );	PORT_DIPNAME( 0xf0, 0x00, "Mode" );	PORT_DIPSETTING(    0x00, "Normal" );	PORT_DIPSETTING(    0x70, "Display Options" );	PORT_DIPSETTING(    0x80|0x00, "Burn-in Test" );	PORT_DIPSETTING(    0x80|0x10, "Color Bar Test" );	PORT_DIPSETTING(    0x80|0x20, "Cross Hatch" );	PORT_DIPSETTING(    0x80|0x30, "Color Map" );	PORT_DIPSETTING(    0x80|0x40, "VIDSEL Test" );	PORT_DIPSETTING(    0x80|0x50, "VIDBIT Test" );	PORT_DIPSETTING(    0x80|0x60, "I/O Board Test" );	PORT_DIPSETTING(    0x80|0x70, "Reserved" );
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_4WAY );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_4WAY );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_4WAY );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_START(); 
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_4WAY | IPF_COCKTAIL );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_4WAY | IPF_COCKTAIL );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_4WAY | IPF_COCKTAIL );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_TILT );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_COIN1 );	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNUSED );INPUT_PORTS_END(); }}; 
	
	/**********************************************************/
	
	static AY8910interface ay8910_interface = new AY8910interface
	(
		2,	/* 2 chips */
		4000000/4,	/* Z80 Clock / 4 */
		new int[] { 50, 50 },
		new ReadHandlerPtr[] { 0 },
		new ReadHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 }
	);
	
	/***********************************************************/
	
	static const char *sharkatt_sample_names[] =
	{
		"*sharkatt",
		"talk.wav",
		"crash.wav",
		0	/* end of array */
	};
	
	static Samplesinterface sharkatt_samples_interface = new Samplesinterface
	(
		2,	/* number of channels */
		50,	/* volume */
		sharkatt_sample_names
	);
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	static const char *thief_sample_names[] =
	{
		"*thief",
		"talk.wav",
		"crash.wav",
		0	/* end of array */
	};
	
	static Samplesinterface thief_samples_interface = new Samplesinterface
	(
		2,	/* number of channels */
		50,	/* volume */
		thief_sample_names
	);
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	static const char *natodef_sample_names[] =
	{
		"*natodef",
		"talk.wav",
		"crash.wav",
		0	/* end of array */
	};
	
	static Samplesinterface natodef_samples_interface = new Samplesinterface
	(
		2,	/* number of channels */
		50,	/* volume */
		natodef_sample_names
	);
	
	
	
	static MachineDriver machine_driver_sharkatt = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				4000000,        /* 4 Mhz? */
				sharkatt_readmem,sharkatt_writemem,readport,writeport,
				thief_interrupt,1
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,
		1,      /* single CPU, no need for interleaving */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 0*8, 24*8-1 ),
		null,      /* no gfxdecodeinfo - bitmapped display */
		16, 16,
		null,
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		thief_vh_start,
		thief_vh_stop,
		thief_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_AY8910,
				ay8910_interface
			),
			new MachineSound(
				SOUND_SAMPLES,
				sharkatt_samples_interface
			)
		}
	);
	
	static MachineDriver machine_driver_thief = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				4000000, /* 4 Mhz? */
				thief_readmem,thief_writemem,readport,writeport,
				thief_interrupt,1
			),
		},
		60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
		1, /* single CPU, no need for interleaving */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 0*8, 32*8-1 ),
		null,
		16, 16,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		thief_vh_start,
		thief_vh_stop,
		thief_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_AY8910,
				ay8910_interface
			),
			new MachineSound(
				SOUND_SAMPLES,
				thief_samples_interface
			)
		}
	);
	
	static MachineDriver machine_driver_natodef = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				4000000, /* 4 Mhz? */
				thief_readmem,thief_writemem,readport,writeport,
				thief_interrupt,1
			),
		},
		60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
		1, /* single CPU, no need for interleaving */
		null,
	
		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 0*8, 32*8-1 ),
		null,
		16, 16,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		thief_vh_start,
		thief_vh_stop,
		thief_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_AY8910,
				ay8910_interface
			),
			new MachineSound(
				SOUND_SAMPLES,
				natodef_samples_interface
			)
		}
	);
	
	/**********************************************************/
	
	static RomLoadPtr rom_sharkatt = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );    /* 64k for code */
		ROM_LOAD( "sharkatt.0",   0x0000, 0x800, 0xc71505e9 );	ROM_LOAD( "sharkatt.1",   0x0800, 0x800, 0x3e3abf70 );	ROM_LOAD( "sharkatt.2",   0x1000, 0x800, 0x96ded944 );	ROM_LOAD( "sharkatt.3",   0x1800, 0x800, 0x007283ae );	ROM_LOAD( "sharkatt.4a",  0x2000, 0x800, 0x5cb114a7 );	ROM_LOAD( "sharkatt.5",   0x2800, 0x800, 0x1d88aaad );	ROM_LOAD( "sharkatt.6",   0x3000, 0x800, 0xc164bad4 );	ROM_LOAD( "sharkatt.7",   0x3800, 0x800, 0xd78c4b8b );	ROM_LOAD( "sharkatt.8",   0x4000, 0x800, 0x5958476a );	ROM_LOAD( "sharkatt.9",   0x4800, 0x800, 0x4915eb37 );	ROM_LOAD( "sharkatt.10",  0x5000, 0x800, 0x9d07cb68 );	ROM_LOAD( "sharkatt.11",  0x5800, 0x800, 0x21edc962 );	ROM_LOAD( "sharkatt.12a", 0x6000, 0x800, 0x5dd8785a );ROM_END(); }}; 
	
	static RomLoadPtr rom_thief = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* Z80 code */
		ROM_LOAD( "t8a0ah0a",	0x0000, 0x1000, 0xedbbf71c );	ROM_LOAD( "t2662h2",	0x1000, 0x1000, 0x85b4f6ff );	ROM_LOAD( "tc162h4",	0x2000, 0x1000, 0x70478a82 );	ROM_LOAD( "t0cb4h6",	0x3000, 0x1000, 0x29de0425 );	ROM_LOAD( "tc707h8",	0x4000, 0x1000, 0xea8dd847 );	ROM_LOAD( "t857bh10",	0x5000, 0x1000, 0x403c33b7 );	ROM_LOAD( "t606bh12",	0x6000, 0x1000, 0x4ca2748b );	ROM_LOAD( "tae4bh14",	0x7000, 0x1000, 0x22e7dcc3 );/* diagnostics ROM */
	
		ROM_REGION( 0x400, REGION_CPU2 );/* coprocessor */
		ROM_LOAD( "b8",			0x000, 0x0200, 0xfe865b2a );	/* B8 is a function dispatch table for the coprocessor (unused) */
		ROM_LOAD( "c8", 		0x200, 0x0200, 0x7ed5c923 );	/* C8 is mapped (banked) in CPU1's address space; it contains Z80 code */
	
		ROM_REGION( 0x6000, REGION_GFX1 );/* image ROMs for coprocessor */
		ROM_LOAD_GFX_ODD(  "t079ahd4" ,  0x0000, 0x1000, 0x928bd8ef )
		ROM_LOAD_GFX_EVEN( "tdda7hh4" ,  0x0000, 0x1000, 0xb48f0862 )
		/* next 0x4000 bytes are unmapped (used by Nato Defense) */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_natodef = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* Z80 code */
		ROM_LOAD( "natodef.cp0",	0x0000, 0x1000, 0x8397c787 );	ROM_LOAD( "natodef.cp2",	0x1000, 0x1000, 0x8cfbf26f );	ROM_LOAD( "natodef.cp4",	0x2000, 0x1000, 0xb4c90fb2 );	ROM_LOAD( "natodef.cp6",	0x3000, 0x1000, 0xc6d0d35e );	ROM_LOAD( "natodef.cp8",	0x4000, 0x1000, 0xe4b6c21e );	ROM_LOAD( "natodef.cpa",	0x5000, 0x1000, 0x888ecd42 );	ROM_LOAD( "natodef.cpc",	0x6000, 0x1000, 0xcf713bc9 );	ROM_LOAD( "natodef.cpe",	0x7000, 0x1000, 0x4eef6bf4 );	ROM_LOAD( "natodef.cp5",	0xa000, 0x1000, 0x65c3601b );/* diagnostics ROM */
	
		ROM_REGION( 0x400, REGION_CPU2 );/* coprocessor */
		ROM_LOAD( "b8",			0x000, 0x0200, 0xfe865b2a );	ROM_LOAD( "c8", 		0x200, 0x0200, 0x7ed5c923 );	/* C8 is mapped (banked) in CPU1's address space; it contains Z80 code */
	
		ROM_REGION( 0x6000, REGION_GFX1 );/* image ROMs for coprocessor */
		ROM_LOAD_GFX_ODD(  "natodef.o4",	0x0000, 0x1000, 0x39a868f8 )
		ROM_LOAD_GFX_EVEN( "natodef.e1",	0x0000, 0x1000, 0xb6d1623d )
		ROM_LOAD_GFX_ODD(  "natodef.o3",	0x2000, 0x1000, 0xb217909a ) /* ? */
		ROM_LOAD_GFX_EVEN( "natodef.e2",	0x2000, 0x1000, 0x886c3f05 ) /* ? */
		ROM_LOAD_GFX_ODD(  "natodef.o2",	0x4000, 0x1000, 0x77cc9cfd ) /* ? */
		ROM_LOAD_GFX_EVEN( "natodef.e3",	0x4000, 0x1000, 0x5302410d ) /* ? */
	ROM_END(); }}; 
	
	
	
	public static GameDriver driver_sharkatt	   = new GameDriver("1980"	,"sharkatt"	,"thief.java"	,rom_sharkatt,null	,machine_driver_sharkatt	,input_ports_sharkatt	,null	,ROT0	,	"Pacific Novelty", "Shark Attack" )
	public static GameDriver driver_thief	   = new GameDriver("1981"	,"thief"	,"thief.java"	,rom_thief,null	,machine_driver_thief	,input_ports_thief	,null	,ROT0	,	"Pacific Novelty", "Thief" )
	public static GameDriver driver_natodef	   = new GameDriver("1982"	,"natodef"	,"thief.java"	,rom_natodef,null	,machine_driver_natodef	,input_ports_natodef	,null	,ROT0	,	"Pacific Novelty", "NATO Defense"  )
}
