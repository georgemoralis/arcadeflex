/***************************************************************************

	Atari Bad Lands hardware

    driver by Aaron Giles

	Games supported:
		* Bad Lands (1989)

	Known bugs:
		* none at this time

****************************************************************************

	Memory map

****************************************************************************

	========================================================================
	MAIN CPU
	========================================================================
	000000-03FFFF   R     xxxxxxxx xxxxxxxx   Program ROM
	FC0000          R     -------x --------   Sound command buffer full
	FC0000            W   -------- --------   Sound CPU reset
	FD0000-FD1FFF   R/W   -------- xxxxxxxx   EEPROM
	FE0000            W   -------- --------   Watchdog reset
	FE2000            W   -------- --------   VBLANK IRQ acknowledge
	FE4000          R     -------- xxxx----   Switch inputs
	                R     -------- x-------      (Self test)
	                R     -------- -x------      (VBLANK)
	                R     -------- --x-----      (Player 2 button)
	                R     -------- ---x----      (Player 1 button)
	FE6000          R     -------- xxxxxxxx   Player 1 steering
	FE6002          R     -------- xxxxxxxx   Player 2 steering
	FE6004          R     -------- xxxxxxxx   Player 1 pedal
	FE6006          R     -------- xxxxxxxx   Player 2 pedal
	FE8000            W   xxxxxxxx --------   Sound command write
	FEA000          R     xxxxxxxx --------   Sound response read
	FEC000            W   -------- -------x   Playfield tile bank select
	FEE000            W   -------- --------   EEPROM enable
	FFC000-FFC0FF   R/W   xxxxxxxx xxxxxxxx   Playfield palette RAM (128 entries)
	                R/W   x------- --------      (RGB 1 LSB)
	                R/W   -xxxxx-- --------      (Red 5 MSB)
	                R/W   ------xx xxx-----      (Green 5 MSB)
	                R/W   -------- ---xxxxx      (Blue 5 MSB)
	FFC100-FFC1FF   R/W   xxxxxxxx xxxxxxxx   Motion object palette RAM (128 entries)
	FFC200-FFC3FF   R/W   xxxxxxxx xxxxxxxx   Extra palette RAM (256 entries)
	FFE000-FFEFFF   R/W   xxxxxxxx xxxxxxxx   Playfield RAM (64x32 tiles)
	                R/W   xxx----- --------      (Palette select)
	                R/W   ---x---- --------      (Tile bank select)
	                R/W   ----xxxx xxxxxxxx      (Tile index)
	FFF000-FFFFFF   R/W   xxxxxxxx xxxxxxxx   Motion object RAM (32 entries x 4 words)
	                R/W   ----xxxx xxxxxxxx      (0: Tile index)
	                R/W   xxxxxxxx x-------      (1: Y position)
	                R/W   -------- ----xxxx      (1: Number of Y tiles - 1)
	                R/W   xxxxxxxx x-------      (3: X position)
	                R/W   -------- ----x---      (3: Priority)
	                R/W   -------- -----xxx      (3: Palette select)
	========================================================================
	Interrupts:
		IRQ1 = VBLANK
		IRQ2 = sound CPU communications
	========================================================================


	========================================================================
	SOUND CPU (based on JSA II, but implemented onboard)
	========================================================================
	0000-1FFF   R/W   xxxxxxxx   Program RAM
	2000-2001   R/W   xxxxxxxx   YM2151 communications
	2802        R     xxxxxxxx   Sound command read
	2804        R     xxxx--xx   Status input
	            R     x-------      (Self test)
	            R     -x------      (Sound command buffer full)
	            R     --x-----      (Sound response buffer full)
	            R     ---x----      (Self test)
	            R     ------xx      (Coin inputs)
	2806        R/W   --------   IRQ acknowledge
	2A02          W   xxxxxxxx   Sound response write
	2A04          W   xxxx---x   Sound control
	              W   xx------      (ROM bank select)
	              W   --xx----      (Coin counters)
	              W   -------x      (YM2151 reset)
	3000-3FFF   R     xxxxxxxx   Banked ROM
	4000-FFFF   R     xxxxxxxx   Program ROM
	========================================================================
	Interrupts:
		IRQ = timed interrupt ORed with YM2151 interrupt
		NMI = latch on sound command
	========================================================================

****************************************************************************/


/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.drivers;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import arcadeflex.v036.generic.funcPtr.TimerCallbackHandlerPtr;
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.badlands.*;
import static gr.codebb.arcadeflex.v037b7.machine.atarigen.*;
import static gr.codebb.arcadeflex.v037b7.machine.atarigenH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static common.libc.cstring.*;
import static arcadeflex.v036.sound._2151intf.*;
import arcadeflex.v036.sound._2151intfH.YM2151interface;
import static arcadeflex.v036.sound._2151intfH.YM3012_VOL;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;

public class badlands
{
	
	public static int[] pedal_value=new int[2];
	
	public static UBytePtr bank_base=new UBytePtr();
	public static UBytePtr bank_source_data=new UBytePtr();
	
	
	
	/*************************************
	 *
	 *	Initialization
	 *
	 *************************************/
	
	public static atarigen_int_callbackPtr update_interrupts = new atarigen_int_callbackPtr() {
            @Override
            public void handler() {
                int newstate = 0;
	
		if (atarigen_video_int_state != 0)
			newstate = 1;
		if (atarigen_sound_int_state != 0)
			newstate = 2;
	
		if (newstate != 0)
			cpu_set_irq_line(0, newstate, ASSERT_LINE);
		else
			cpu_set_irq_line(0, 7, CLEAR_LINE);
            }
        };
	
	public static TimerCallbackHandlerPtr scanline_update = new TimerCallbackHandlerPtr() {
            @Override
            public void handler(int scanline) {
                badlands_scanline_update(scanline);
	
		/* sound IRQ is on 32V */
		if (scanline % 32 == 0)
		{
			if ((scanline & 32) != 0)
				atarigen_6502_irq_ack_r.handler(0);
			else if ((readinputport(0) & 0x40)==0)
				atarigen_6502_irq_gen.handler();
		}
            }
        };
        
	
	public static InitMachineHandlerPtr init_machine = new InitMachineHandlerPtr() { public void handler() 
	{
		pedal_value[0] = pedal_value[1] = 0x80;
	
		atarigen_eeprom_reset();
		atarigen_interrupt_reset(update_interrupts);
		atarigen_scanline_timer_reset(scanline_update, 8);
	
		atarigen_sound_io_reset(1);
		memcpy(new UBytePtr(bank_base), new UBytePtr(bank_source_data, 0x0000), 0x1000);
	} };
	
	
	
	/*************************************
	 *
	 *	Interrupt handling
	 *
	 *************************************/
	
	static InterruptHandlerPtr vblank_int = new InterruptHandlerPtr() {
            @Override
            public int handler() {
                int pedal_state = input_port_4_r.handler(0);
		int i;
	
		/* update the pedals once per frame */
                for (i = 0; i < 2; i++)
		{
			pedal_value[i]--;
			if ((pedal_state & (1 << i)) != 0)
				pedal_value[i]++;
		}
	
		return atarigen_video_int_gen.handler();
            }
        };
	
	
	/*************************************
	 *
	 *	I/O read dispatch
	 *
	 *************************************/
	
	public static ReadHandlerPtr sound_busy_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int temp = 0xfeff;
	
		//(void)offset;
		if (atarigen_cpu_to_sound_ready != 0) temp ^= 0x0100;
		return temp;
	} };
	
	
	public static ReadHandlerPtr pedal_0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		//(void)offset;
		return pedal_value[0];
	} };
	
	
	public static ReadHandlerPtr pedal_1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		//(void)offset;
		return pedal_value[1];
	} };
	
	
	
	/*************************************
	 *
	 *	Audio I/O handlers
	 *
	 *************************************/
	
	public static ReadHandlerPtr audio_io_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int result = 0xff;
	
		switch (offset & 0x206)
		{
			case 0x000:		/* n/c */
				logerror("audio_io_r: Unknown read at %04X\n", offset & 0x206);
				break;
	
			case 0x002:		/* /RDP */
				result = atarigen_6502_sound_r.handler(offset);
				break;
	
			case 0x004:		/* /RDIO */
				/*
					0x80 = self test
					0x40 = NMI line state (active low)
					0x20 = sound output full
					0x10 = self test
					0x08 = +5V
					0x04 = +5V
					0x02 = coin 2
					0x01 = coin 1
				*/
				result = readinputport(3);
				if ((readinputport(0) & 0x0080)==0) result ^= 0x90;
				if (atarigen_cpu_to_sound_ready != 0) result ^= 0x40;
				if (atarigen_sound_to_cpu_ready != 0) result ^= 0x20;
				result ^= 0x10;
				break;
	
			case 0x006:		/* /IRQACK */
				atarigen_6502_irq_ack_r.handler(0);
				break;
	
			case 0x200:		/* /VOICE */
			case 0x202:		/* /WRP */
			case 0x204:		/* /WRIO */
			case 0x206:		/* /MIX */
				logerror("audio_io_r: Unknown read at %04X\n", offset & 0x206);
				break;
		}
	
		return result;
	} };
	
	
	public static WriteHandlerPtr audio_io_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		switch (offset & 0x206)
		{
			case 0x000:		/* n/c */
			case 0x002:		/* /RDP */
			case 0x004:		/* /RDIO */
				logerror("audio_io_w: Unknown write (%02X) at %04X\n", data & 0xff, offset & 0x206);
				break;
	
			case 0x006:		/* /IRQACK */
				atarigen_6502_irq_ack_r.handler(0);
				break;
	
			case 0x200:		/* n/c */
			case 0x206:		/* n/c */
				break;
	
			case 0x202:		/* /WRP */
				atarigen_6502_sound_w.handler(offset, data);
				break;
	
			case 0x204:		/* WRIO */
				/*
					0xc0 = bank address
					0x20 = coin counter 2
					0x10 = coin counter 1
					0x08 = n/c
					0x04 = n/c
					0x02 = n/c
					0x01 = YM2151 reset (active low)
				*/
	
				/* update the bank */
				memcpy(new UBytePtr(bank_base), new UBytePtr(bank_source_data, 0x1000 * ((data >> 6) & 3)), 0x1000);
				break;
		}
	} };
	
	
	
	/*************************************
	 *
	 *	Main CPU memory handlers
	 *
	 *************************************/
	
	static MemoryReadAddress main_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x03ffff, MRA_ROM ),
		new MemoryReadAddress( 0xfc0000, 0xfc1fff, sound_busy_r ),
		new MemoryReadAddress( 0xfd0000, 0xfd1fff, atarigen_eeprom_r ),
		new MemoryReadAddress( 0xfe4000, 0xfe5fff, input_port_0_r ),
		new MemoryReadAddress( 0xfe6000, 0xfe6001, input_port_1_r ),
		new MemoryReadAddress( 0xfe6002, 0xfe6003, input_port_2_r ),
		new MemoryReadAddress( 0xfe6004, 0xfe6005, pedal_0_r ),
		new MemoryReadAddress( 0xfe6006, 0xfe6007, pedal_1_r ),
		new MemoryReadAddress( 0xfea000, 0xfebfff, atarigen_sound_upper_r ),
		new MemoryReadAddress( 0xffc000, 0xffc3ff, paletteram_word_r ),
		new MemoryReadAddress( 0xffe000, 0xffefff, MRA_BANK1 ),
		new MemoryReadAddress( 0xfff000, 0xffffff, MRA_BANK2 ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	
	static MemoryWriteAddress main_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x03ffff, MWA_ROM ),
		new MemoryWriteAddress( 0xfc0000, 0xfc1fff, atarigen_sound_reset_w ),
		new MemoryWriteAddress( 0xfd0000, 0xfd1fff, atarigen_eeprom_w, atarigen_eeprom, atarigen_eeprom_size ),
		new MemoryWriteAddress( 0xfe0000, 0xfe1fff, watchdog_reset_w ),
		new MemoryWriteAddress( 0xfe2000, 0xfe3fff, atarigen_video_int_ack_w ),
		new MemoryWriteAddress( 0xfe8000, 0xfe9fff, atarigen_sound_upper_w ),
		new MemoryWriteAddress( 0xfec000, 0xfedfff, badlands_pf_bank_w ),
		new MemoryWriteAddress( 0xfee000, 0xfeffff, atarigen_eeprom_enable_w ),
		new MemoryWriteAddress( 0xffc000, 0xffc3ff, atarigen_expanded_666_paletteram_w, paletteram ),
		new MemoryWriteAddress( 0xffe000, 0xffefff, badlands_playfieldram_w, atarigen_playfieldram, atarigen_playfieldram_size ),
		new MemoryWriteAddress( 0xfff000, 0xffffff, MWA_BANK2, atarigen_spriteram, atarigen_spriteram_size ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	
	/*************************************
	 *
	 *	Sound CPU memory handlers
	 *
	 *************************************/
	
	static MemoryReadAddress audio_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x1fff, MRA_RAM ),
		new MemoryReadAddress( 0x2000, 0x2001, YM2151_status_port_0_r ),
		new MemoryReadAddress( 0x2800, 0x2bff, audio_io_r ),
		new MemoryReadAddress( 0x3000, 0xffff, MRA_ROM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	
	static MemoryWriteAddress audio_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0x1fff, MWA_RAM ),
		new MemoryWriteAddress( 0x2000, 0x2000, YM2151_register_port_0_w ),
		new MemoryWriteAddress( 0x2001, 0x2001, YM2151_data_port_0_w ),
		new MemoryWriteAddress( 0x2800, 0x2bff, audio_io_w ),
		new MemoryWriteAddress( 0x3000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	/*************************************
	 *
	 *	Port definitions
	 *
	 *************************************/
	
	static InputPortHandlerPtr input_ports_badlands = new InputPortHandlerPtr(){ public void handler() { 
		PORT_START(); 		/* fe4000 */
		PORT_BIT( 0x000f, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x0040, IP_ACTIVE_HIGH, IPT_VBLANK );
		PORT_SERVICE( 0x0080, IP_ACTIVE_LOW );
		PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* fe6000 */
		PORT_ANALOG( 0x00ff, 0, IPT_DIAL | IPF_PLAYER1, 50, 10, 0, 0 );
		PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START();       /* fe6002 */
		PORT_ANALOG( 0x00ff, 0, IPT_DIAL | IPF_PLAYER2, 50, 10, 0, 0 );
		PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 		/* audio port */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_SPECIAL );/* self test */
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_SPECIAL );/* response buffer full */
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_SPECIAL );/* command buffer full */
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_SPECIAL );/* self test */
	
		PORT_START();       /* fake for pedals */
		PORT_BIT( 0x0001, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0xfffe, IP_ACTIVE_HIGH, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Graphics definitions
	 *
	 *************************************/
	
	static GfxLayout pflayout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,1),
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] { 0, 4, 8, 12, 16, 20, 24, 28 },
		new int[] { 0*8, 4*8, 8*8, 12*8, 16*8, 20*8, 24*8, 28*8 },
		32*8
	);
	
	
	static GfxLayout molayout = new GfxLayout
	(
		16,8,
		RGN_FRAC(1,1),
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] { 0, 4, 8, 12, 16, 20, 24, 28, 32, 36, 40, 44, 48, 52, 56, 60 },
		new int[] { 0*8, 8*8, 16*8, 24*8, 32*8, 40*8, 48*8, 56*8 },
		64*8
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, pflayout,    0, 8 ),
		new GfxDecodeInfo( REGION_GFX2, 0, molayout,  128, 8 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	/*************************************
	 *
	 *	Sound definitions
	 *
	 *************************************/
	
	static YM2151interface ym2151_interface = new YM2151interface
	(
		1,
		ATARI_CLOCK_14MHz/4,
		new int[] { YM3012_VOL(30,MIXER_PAN_CENTER,30,MIXER_PAN_CENTER) },
		new WriteYmHandlerPtr[] { null }
	);
	
	
	
	/*************************************
	 *
	 *	Machine driver
	 *
	 *************************************/
	
	static MachineDriver machine_driver_badlands = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,		/* verified */
				ATARI_CLOCK_14MHz/2,
				main_readmem,main_writemem,null,null,
				vblank_int,1
			),
			new MachineCPU(
				CPU_M6502,
				ATARI_CLOCK_14MHz/8,
				audio_readmem,audio_writemem,null,null,
				ignore_interrupt,1
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
		1,
		init_machine,
	
		/* video hardware */
		42*8, 30*8, new rectangle( 0*8, 42*8-1, 0*8, 30*8-1 ),
		gfxdecodeinfo,
		256,256,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK,
		null,
		badlands_vh_start,
		badlands_vh_stop,
		badlands_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_YM2151,
				ym2151_interface
			)
		},
	
		atarigen_nvram_handler
	);
	
	
	
	/*************************************
	 *
	 *	ROM decoding
	 *
	 *************************************/
	
	static void rom_decode()
	{
		int i;
	
		for (i = 0; i < memory_region_length(REGION_GFX1); i++)
			memory_region(REGION_GFX1).write(i, memory_region(REGION_GFX1).read(i) ^ 0xff);
		for (i = 0; i < memory_region_length(REGION_GFX2); i++)
			memory_region(REGION_GFX2).write(i, memory_region(REGION_GFX2).read(i) ^ 0xff);
	}
	
	
	
	/*************************************
	 *
	 *	ROM definition(s)
	 *
	 *************************************/
	
	static RomLoadHandlerPtr rom_badlands = new RomLoadHandlerPtr(){ public void handler(){ 
		ROM_REGION( 0x40000, REGION_CPU1 );/* 4*64k for 68000 code */
		ROM_LOAD_EVEN( "1008.20f",  0x00000, 0x10000, 0xa3da5774 );
		ROM_LOAD_ODD ( "1006.27f",  0x00000, 0x10000, 0xaa03b4f3 );
		ROM_LOAD_EVEN( "1009.17f",  0x20000, 0x10000, 0x0e2e807f );
		ROM_LOAD_ODD ( "1007.24f",  0x20000, 0x10000, 0x99a20c2c );
	
		ROM_REGION( 0x14000, REGION_CPU2 );/* 64k for 6502 code */
		ROM_LOAD( "1018.9c", 0x10000, 0x4000, 0xa05fd146 );
		ROM_CONTINUE(        0x04000, 0xc000 );
	
		ROM_REGION( 0x60000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "1012.4n",  0x000000, 0x10000, 0x5d124c6c );/* playfield */
		ROM_LOAD( "1013.2n",  0x010000, 0x10000, 0xb1ec90d6 );
		ROM_LOAD( "1014.4s",  0x020000, 0x10000, 0x248a6845 );
		ROM_LOAD( "1015.2s",  0x030000, 0x10000, 0x792296d8 );
		ROM_LOAD( "1016.4u",  0x040000, 0x10000, 0x878f7c66 );
		ROM_LOAD( "1017.2u",  0x050000, 0x10000, 0xad0071a3 );
	
		ROM_REGION( 0x30000, REGION_GFX2 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "1010.14r", 0x000000, 0x10000, 0xc15f629e );/* mo */
		ROM_LOAD( "1011.10r", 0x010000, 0x10000, 0xfb0b6717 );
		ROM_LOAD( "1019.14t", 0x020000, 0x10000, 0x0e26bff6 );
	ROM_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Driver initialization
	 *
	 *************************************/
	
	public static InitDriverHandlerPtr init_badlands = new InitDriverHandlerPtr() { public void handler() 
	{
		atarigen_eeprom_default = null;
	
		/* initialize the audio system */
		bank_base = new UBytePtr(memory_region(REGION_CPU2), 0x03000);
		bank_source_data = new UBytePtr(memory_region(REGION_CPU2), 0x10000);
	
		/* speed up the 6502 */
		atarigen_init_6502_speedup(1, 0x4155, 0x416d);
	
		/* display messages */
		atarigen_show_sound_message();
	
		rom_decode();
	} };
	
	
	
	/*************************************
	 *
	 *	Game driver(s)
	 *
	 *************************************/
	
	public static GameDriver driver_badlands	   = new GameDriver("1989"	,"badlands"	,"badlands.java"	,rom_badlands,null	,machine_driver_badlands	,input_ports_badlands	,init_badlands	,ROT0	,	"Atari Games", "Bad Lands" );
}
