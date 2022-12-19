/***************************************************************************

      Poly-Play
	  (c) 1985 by VEB Polytechnik Karl-Marx-Stadt

      driver by Martin Buchholz (buchholz@mail.uni-greifswald.de)

      Very special thanks to the following people, each one of them spent
	  some of their spare time to make this driver working:
	  - Juergen Oppermann and Volker Hann for electronical assistance,
	    repair work and ROM dumping.
	  - Jan-Ole Christian from the Videogamemuseum in Berlin, which houses
	    one of the last existing Poly-Play arcade automatons. He also
		provided me with schematics and service manuals.


memory map:

0000 - 03ff OS ROM
0400 - 07ff Game ROM (used for Abfahrtslauf)
0800 - 0cff Menu Screen ROM

0d00 - 0fff work RAM

1000 - 4fff GAME ROM (pcb 2 - Abfahrtslauf          (1000 - 1bff),
                              Hirschjagd            (1c00 - 27ff),
							  Hase und Wolf         (2800 - 3fff),
                              Schmetterlingsfang    (4000 - 4fff)
5000 - 8fff GAME ROM (pcb 1 - Schiessbude           (5000 - 5fff)
                              Autorennen            (6000 - 73ff)
							  opto-akust. Merkspiel (7400 - 7fff)
							  Wasserrohrbruch       (8000 - 8fff)

e800 - ebff character ROM (chr 00..7f) 1 bit per pixel
ec00 - f7ff character RAM (chr 80..ff) 3 bit per pixel
f800 - ffff video RAM

I/O ports:

read:

83        IN1
          used as hardware random number generator

84        IN0
          bit 0 = fire button
          bit 1 = right
          bit 2 = left
          bit 3 = up
          bit 4 = down
          bit 5 = unused
          bit 6 = Summe Spiele
          bit 7 = coinage (+IRQ to make the game acknowledge it)

85        bit 0-4 = light organ (unemulated :)) )
          bit 5-7 = sound parameter (unemulated, it's very difficult to
		            figure out how those work)

86        ???

87        PIO Control register

write:
80	      Sound Channel 1
81        Sound Channel 2
82        generates 40 Hz timer for timeout in game title screens
83        generates main 75 Hz timer interrupt

The Poly-Play has a simple bookmarking system which can be activated
setting Bit 6 of PORTA (Summe Spiele) to low. It reads a double word
from 0c00 and displays it on the screen.
I currently haven't figured out how the I/O port handling for the book-
mark system works.

Uniquely the Poly-Play has a light organ which totally confuses you whilst
playing the automaton. Bits 1-5 of PORTB control the organ but it's not
emulated now. ;)

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class polyplay
{
	
	static UBytePtr polyplay_ram;
	
	
	/* video hardware access */
	extern UBytePtr polyplay_characterram;
	
	/* I/O Port handling */
	
	/* sound handling */
	void set_channel1(int active);
	void set_channel2(int active);
	int prescale1, prescale2;
	static int channel1_active;
	static int channel1_const;
	static int channel2_active;
	static int channel2_const;
	static void play_channel1(int data);
	void play_channel2(int data);
	
	/* timer handling */
	int timer2_active;
	
	
	/* Polyplay Sound Interface */
	static CustomSound_interface custom_interface = new CustomSound_interface
	(
		polyplay_sh_start,
		polyplay_sh_stop,
		polyplay_sh_update
	);
	
	
	void polyplay_reset(void)
	{
		channel1_active = 0;
		channel1_const = 0;
		channel2_active = 0;
		channel2_const = 0;
		set_channel1(0);
		play_channel1(0);
		set_channel2(0);
		play_channel2(0);
	}
	
	
	/* work RAM access */
	public static WriteHandlerPtr polyplay_ram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		polyplay_ram[offset] = data;
	} };
	
	public static ReadHandlerPtr polyplay_ram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return polyplay_ram[offset];
	} };
	
	/* memory mapping */
	static MemoryReadAddress polyplay_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x0bff, MRA_ROM ),
		new MemoryReadAddress( 0x0c00, 0x0fff, polyplay_ram_r ),
		new MemoryReadAddress( 0x1000, 0x8fff, MRA_ROM ),
	
		new MemoryReadAddress( 0xe800, 0xebff, MRA_ROM),
		new MemoryReadAddress( 0xec00, 0xf7ff, polyplay_characterram_r ),
		new MemoryReadAddress( 0xf800, 0xffff, videoram_r ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress polyplay_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x0bff, MWA_ROM ),
		new MemoryWriteAddress( 0x0c00, 0x0fff, polyplay_ram_w, polyplay_ram ),
		new MemoryWriteAddress( 0x1000, 0x8fff, MWA_ROM ),
	
		new MemoryWriteAddress( 0xe800, 0xebff, MWA_ROM ),
		new MemoryWriteAddress( 0xec00, 0xf7ff, polyplay_characterram_w, polyplay_characterram ),
		new MemoryWriteAddress( 0xf800, 0xffff, videoram_w, videoram, videoram_size ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	/* port mapping */
	static IOReadPort readport_polyplay[] =
	{
		new IOReadPort( 0x84, 0x84, polyplay_input_read ),
		new IOReadPort( 0x83, 0x83, polyplay_random_read ),
		new IOReadPort( -1 )  /* end of table */
	};
	
	static IOWritePort writeport_polyplay[] =
	{	new IOWritePort( 0x80, 0x81, polyplay_sound_channel ),
		new IOWritePort( 0x82, 0x82, polyplay_start_timer2 ),
		new IOWritePort( -1 )  /* end of table */
	};
	
	static InputPortPtr input_ports_polyplay = new InputPortPtr(){ public void handler() { 
	PORT_START(); 	/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1 );	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );	PORT_BITX(0x40, IP_ACTIVE_LOW, IPT_SERVICE, "Bookkeeping Info", KEYCODE_F2, IP_JOY_NONE );	PORT_BIT_IMPULSE( 0x80, IP_ACTIVE_LOW, IPT_COIN1, 10);INPUT_PORTS_END(); }}; 
	
	
	public static WriteHandlerPtr polyplay_sound_channel = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		switch(offset) {
		case 0x00:
			if (channel1_const != 0) {
				if (data <= 1) {
					set_channel1(0);
				}
				channel1_const = 0;
				play_channel1(data*prescale1);
	
			}
			else {
				prescale1 = (data & 0x20) ? 16 : 1;
				if ((data & 0x04) != 0) {
					set_channel1(1);
					channel1_const = 1;
				}
				if ((data == 0x41) || (data == 0x65) || (data == 0x45)) {
					set_channel1(0);
					play_channel1(0);
				}
			}
			break;
		case 0x01:
			if (channel2_const != 0) {
				if (data <= 1) {
					set_channel2(0);
				}
				channel2_const = 0;
				play_channel2(data*prescale2);
	
			}
			else {
				prescale2 = (data & 0x20) ? 16 : 1;
				if ((data & 0x04) != 0) {
					set_channel2(1);
					channel2_const = 1;
				}
				if ((data == 0x41) || (data == 0x65) || (data == 0x45)) {
					set_channel2(0);
					play_channel2(0);
				}
			}
			break;
		}
	} };
	
	public static WriteHandlerPtr polyplay_start_timer2 = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (data == 0x03) {
			timer2_active = 0;
		}
		else {
			if (data == 0xb5) {
				timer_set(TIME_IN_HZ(40), 1, polyplay_timer);
				timer2_active = 1;
			}
		}
	} };
	
	/* random number generator */
	public static ReadHandlerPtr polyplay_random_read  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return rand() % 0xff;
	} };
	
	/* graphic sturctures */
	static GfxLayout charlayout_1_bit = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		128,	/* 128 characters */
		1,	    /* 1 bit per pixel */
		new int[] { 0 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },	/* pretty straightforward layout */
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8	/* every char takes 8 consecutive bytes */
	);
	
	static GfxLayout charlayout_3_bit = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		128,	/* 128 characters */
		3,	    /* 3 bit per pixel */
		new int[] { 0, 128*8*8, 128*8*8 + 128*8*8 },    /* offset for each bitplane */
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },	/* pretty straightforward layout */
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8	/* every char takes 8 consecutive bytes */
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( 0, 0xe800, charlayout_1_bit, 0, 2 ),
		new GfxDecodeInfo( 0, 0xec00, charlayout_3_bit, 2, 8 ),
		new GfxDecodeInfo( -1 )	/* end of array */
	};
	
	
	/* the machine driver */
	
	#define MACHINEDRIVER(NAME, MEM, PORT)				\
	static MachineDriver machine_driver_##NAME = new MachineDriver\
	(													\
		/* basic machine hardware */					\
		new MachineCPU[] {												\
			new MachineCPU(											\
				CPU_Z80,								\
				9830400/4,								\
				MEM##_readmem,MEM##_writemem,           \
				readport_##PORT,writeport_polyplay,	    \
				ignore_interrupt, 1					\
			)											\
		},												\
		50, 0,	/* frames per second, vblank duration */ \
		null,	/* single CPU, no need for interleaving */	\
		polyplay_reset,									\
														\
		/* video hardware */							\
		64*8, 32*8, new rectangle( 0*8, 64*8-1, 0*8, 32*8-1 ),		\
		gfxdecodeinfo,									\
		64, 64,											\
		polyplay_init_palette,					        \
														\
		VIDEO_TYPE_RASTER|VIDEO_SUPPORTS_DIRTY,			\
		null,												\
		generic_vh_start,								\
		generic_vh_stop,								\
		polyplay_vh_screenrefresh,						\
														\
		/* sound hardware */							\
		0,0,0,0,											\
		new MachineSound[] {							                    \
			new MachineSound(						                    \
				SOUND_CUSTOM,		                    \
				custom_interface	                    \
			)						                    \
		}							                    \
	);
	
	MACHINEDRIVER( polyplay, polyplay, polyplay )
	
	
	
	/* ROM loading and mapping */
	static RomLoadPtr rom_polyplay = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1 );/* 64k for code */
		ROM_LOAD( "cpu_0000.37",       0x0000, 0x0400, 0x87884c5f );	ROM_LOAD( "cpu_0400.36",       0x0400, 0x0400, 0xd5c84829 );	ROM_LOAD( "cpu_0800.35",       0x0800, 0x0400, 0x5f36d08e );	ROM_LOAD( "2_-_1000.14",       0x1000, 0x0400, 0x950dfcdb );	ROM_LOAD( "2_-_1400.10",       0x1400, 0x0400, 0x829f74ca );	ROM_LOAD( "2_-_1800.6",        0x1800, 0x0400, 0xb69306f5 );	ROM_LOAD( "2_-_1c00.2",        0x1c00, 0x0400, 0xaede2280 );	ROM_LOAD( "2_-_2000.15",       0x2000, 0x0400, 0x6c7ad0d8 );	ROM_LOAD( "2_-_2400.11",       0x2400, 0x0400, 0xbc7462f0 );	ROM_LOAD( "2_-_2800.7",        0x2800, 0x0400, 0x9ccf1958 );	ROM_LOAD( "2_-_2c00.3",        0x2c00, 0x0400, 0x21827930 );	ROM_LOAD( "2_-_3000.16",       0x3000, 0x0400, 0xb3b3c0ec );	ROM_LOAD( "2_-_3400.12",       0x3400, 0x0400, 0xbd416cd0 );	ROM_LOAD( "2_-_3800.8",        0x3800, 0x0400, 0x1c470b7c );	ROM_LOAD( "2_-_3c00.4",        0x3c00, 0x0400, 0xb8354a19 );	ROM_LOAD( "2_-_4000.17",       0x4000, 0x0400, 0x1e01041e );	ROM_LOAD( "2_-_4400.13",       0x4400, 0x0400, 0xfe4d8959 );	ROM_LOAD( "2_-_4800.9",        0x4800, 0x0400, 0xc45f1d9d );	ROM_LOAD( "2_-_4c00.5",        0x4c00, 0x0400, 0x26950ad6 );	ROM_LOAD( "1_-_5000.30",       0x5000, 0x0400, 0x9f5e2ba1 );	ROM_LOAD( "1_-_5400.26",       0x5400, 0x0400, 0xb5f9a780 );	ROM_LOAD( "1_-_5800.22",       0x5800, 0x0400, 0xd973ad12 );	ROM_LOAD( "1_-_5c00.18",       0x5c00, 0x0400, 0x9c22ea79 );	ROM_LOAD( "1_-_6000.31",       0x6000, 0x0400, 0x245c49ca );	ROM_LOAD( "1_-_6400.27",       0x6400, 0x0400, 0x181e427e );	ROM_LOAD( "1_-_6800.23",       0x6800, 0x0400, 0x8a6c1f97 );	ROM_LOAD( "1_-_6c00.19",       0x6c00, 0x0400, 0x77901dc9 );	ROM_LOAD( "1_-_7000.32",       0x7000, 0x0400, 0x83ffbe57 );	ROM_LOAD( "1_-_7400.28",       0x7400, 0x0400, 0xe2a66531 );	ROM_LOAD( "1_-_7800.24",       0x7800, 0x0400, 0x1d0803ef );	ROM_LOAD( "1_-_7c00.20",       0x7c00, 0x0400, 0x17dfa7e4 );	ROM_LOAD( "1_-_8000.33",       0x8000, 0x0400, 0x6ee02375 );	ROM_LOAD( "1_-_8400.29",       0x8400, 0x0400, 0x9db09598 );	ROM_LOAD( "1_-_8800.25",       0x8800, 0x0400, 0xca2f963f );	ROM_LOAD( "1_-_8c00.21",       0x8c00, 0x0400, 0x0c7dec2d );	ROM_LOAD( "char.1",            0xe800, 0x0400, 0x5242dd6b );ROM_END(); }}; 
	
	
	/* interrupt handling, the game runs in IM 2 */
	public static ReadHandlerPtr polyplay_input_read  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int inp = input_port_0_r.handler(offset);
	
		if ((inp & 0x80) == 0) {    /* Coin inserted */
			cpu_cause_interrupt(0, 0x50);
			coin_counter_w(0, 1);
			timer_set(TIME_IN_SEC(1), 2, polyplay_timer);
		}
	
		return inp;
	} };
	
	static void polyplay_timer(int param)
	{
		switch(param) {
		case 0:
			cpu_cause_interrupt(0, 0x4e);
			break;
		case 1:
			if (timer2_active != 0) {
				timer_set(TIME_IN_HZ(40), 1, polyplay_timer);
				cpu_cause_interrupt(0, 0x4c);
			}
			break;
		case 2:
			coin_counter_w(0, 0);
			break;
		}
	}
	
	/* initialization */
	static public static InitDriverPtr init_polyplay_sound = new InitDriverPtr() { public void handler() 
	{
		timer_init();
		timer_pulse(TIME_IN_HZ(75), 0, polyplay_timer);
		timer2_active = 0;
	} };
	
	/* game driver */
	public static GameDriver driver_polyplay	   = new GameDriver("1985"	,"polyplay"	,"polyplay.java"	,rom_polyplay,null	,machine_driver_polyplay	,input_ports_polyplay	,init_polyplay_sound	,ROT0	,	"VEB Polytechnik Karl-Marx-Stadt", "Poly-Play" )
}
