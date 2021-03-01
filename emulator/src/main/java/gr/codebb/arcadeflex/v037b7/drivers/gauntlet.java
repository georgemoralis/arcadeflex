/***************************************************************************

	Atari Gauntlet hardware

	driver by Aaron Giles

	Games supported:
		* Gauntlet (1985) [3 sets]
		* Gauntlet 2-player Version (1985)
		* Gauntlet II (1986)
		* Vindicators Part II (1988)

	Known bugs:
		* none at this time

****************************************************************************

	Memory map

****************************************************************************

	========================================================================
	MAIN CPU
	========================================================================
	000000-037FFF   R     xxxxxxxx xxxxxxxx   Program ROM
	038000-03FFFF   R     xxxxxxxx xxxxxxxx   Slapstic-protected ROM
	040000-07FFFF   R     xxxxxxxx xxxxxxxx   Program ROM
	800000-801FFF   R/W   xxxxxxxx xxxxxxxx   Program RAM
	802000-802FFF   R/W   -------- xxxxxxxx   EEPROM
	803000          R     -------- xxxxxxxx   Input port 1
	803002          R     -------- xxxxxxxx   Input port 2
	803004          R     -------- xxxxxxxx   Input port 3
	803006          R     -------- xxxxxxxx   Input port 4
	803008          R     -------- -xxxx---   Status port
	                R     -------- -x------      (VBLANK)
	                R     -------- --x-----      (Sound command buffer full)
	                R     -------- ---x----      (Sound response buffer full)
	                R     -------- ----x---      (Self test)
	80300E          R     -------- xxxxxxxx   Sound response read
	803100            W   -------- --------   Watchdog reset
	80312E            W   -------- -------x   Sound CPU reset
	803140            W   -------- --------   VBLANK IRQ acknowledge
	803150            W   -------- --------   EEPROM enable
	803170            W   -------- xxxxxxxx   Sound command write
	900000-901FFF   R/W   xxxxxxxx xxxxxxxx   Playfield RAM (64x64 tiles)
	                R/W   x------- --------      (Horizontal flip)
	                R/W   -xxx---- --------      (Palette select)
	                R/W   ----xxxx xxxxxxxx      (Tile index)
	902000-903FFF   R/W   xxxxxxxx xxxxxxxx   Motion object RAM (1024 entries x 4 words)
	                R/W   -xxxxxxx xxxxxxxx      (0: Tile index)
	                R/W   xxxxxxxx x-------      (1024: X position)
	                R/W   -------- ----xxxx      (1024: Palette select)
	                R/W   xxxxxxxx x-------      (2048: Y position)
	                R/W   -------- -x------      (2048: Horizontal flip)
	                R/W   -------- --xxx---      (2048: Number of X tiles - 1)
	                R/W   -------- -----xxx      (2048: Number of Y tiles - 1)
	                R/W   ------xx xxxxxxxx      (3072: Link to next object)
	904000-904FFF   R/W   xxxxxxxx xxxxxxxx   Spare video RAM
	905000-905FFF   R/W   xxxxxxxx xxxxxxxx   Alphanumerics RAM (64x32 tiles)
	                R/W   x------- --------      (Opaque/transparent)
	                R/W   -xxxxx-- --------      (Palette select)
	                R/W   ------xx xxxxxxxx      (Tile index)
	905F6E          R/W   xxxxxxxx x-----xx   Playfield Y scroll/tile bank select
	                R/W   xxxxxxxx x-------      (Playfield Y scroll)
	                R/W   -------- ------xx      (Playfield tile bank select)
	910000-9101FF   R/W   xxxxxxxx xxxxxxxx   Alphanumercs palette RAM (256 entries)
	                R/W   xxxx---- --------      (Intensity)
	                R/W   ----xxxx --------      (Red)
	                R/W   -------- xxxx----      (Green)
	                R/W   -------- ----xxxx      (Blue)
	910200-9103FF   R/W   xxxxxxxx xxxxxxxx   Motion object palette RAM (256 entries)
	910400-9105FF   R/W   xxxxxxxx xxxxxxxx   Playfield palette RAM (256 entries)
	910600-9107FF   R/W   xxxxxxxx xxxxxxxx   Extra palette RAM (256 entries)
	930000            W   xxxxxxxx x-------   Playfield X scroll
	========================================================================
	Interrupts:
		IRQ4 = VBLANK
		IRQ6 = sound CPU communications
	========================================================================


	========================================================================
	SOUND CPU
	========================================================================
	0000-0FFF   R/W   xxxxxxxx   Program RAM
	1000          W   xxxxxxxx   Sound response write
	1010        R     xxxxxxxx   Sound command read
	1020        R     ----xxxx   Coin inputs
	            R     ----x---      (Coin 1)
	            R     -----x--      (Coin 2)
	            R     ------x-      (Coin 3)
	            R     -------x      (Coin 4)
	1020          W   xxxxxxxx   Mixer control
	              W   xxx-----      (TMS5220 volume)
	              W   ---xx---      (POKEY volume)
	              W   -----xxx      (YM2151 volume)
	1030        R     xxxx----   Sound status read
	            R     x-------      (Sound command buffer full)
	            R     -x------      (Sound response buffer full)
	            R     --x-----      (TMS5220 ready)
	            R     ---x----      (Self test)
	1030          W   x-------   YM2151 reset
	1031          W   x-------   TMS5220 data strobe
	1032          W   x-------   TMS5220 reset
	1033          W   x-------   TMS5220 frequency
	1800-180F   R/W   xxxxxxxx   POKEY communications
	1810-1811   R/W   xxxxxxxx   YM2151 communications
	1820          W   xxxxxxxx   TMS5220 data latch
	1830        R/W   --------   IRQ acknowledge
	4000-FFFF   R     xxxxxxxx   Program ROM
	========================================================================
	Interrupts:
		IRQ = timed interrupt
		NMI = latch on sound command
	========================================================================

****************************************************************************/



/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.drivers;

public class gauntlet
{
/*TODO*///	
/*TODO*///	
/*TODO*///	extern UINT8 vindctr2_screen_refresh;
/*TODO*///	
/*TODO*///	
/*TODO*///	void gauntlet_scanline_update(int scanline);
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	static UINT8 *speed_check;
/*TODO*///	static UINT32 last_speed_check;
/*TODO*///	
/*TODO*///	static UINT8 speech_val;
/*TODO*///	static UINT8 last_speech_write;
/*TODO*///	static UINT8 speech_squeak;
/*TODO*///	
/*TODO*///	static UINT16 last_sound_reset;
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Initialization of globals.
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static void update_interrupts(void)
/*TODO*///	{
/*TODO*///		int newstate = 0;
/*TODO*///	
/*TODO*///		if (atarigen_video_int_state != 0)
/*TODO*///			newstate |= 4;
/*TODO*///		if (atarigen_sound_int_state != 0)
/*TODO*///			newstate |= 6;
/*TODO*///	
/*TODO*///		if (newstate != 0)
/*TODO*///			cpu_set_irq_line(0, newstate, ASSERT_LINE);
/*TODO*///		else
/*TODO*///			cpu_set_irq_line(0, 7, CLEAR_LINE);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static void scanline_update(int scanline)
/*TODO*///	{
/*TODO*///		gauntlet_scanline_update(scanline);
/*TODO*///	
/*TODO*///		/* sound IRQ is on 32V */
/*TODO*///		if (scanline % 32 == 0)
/*TODO*///		{
/*TODO*///			if ((scanline & 32) != 0)
/*TODO*///				atarigen_6502_irq_gen();
/*TODO*///			else
/*TODO*///				atarigen_6502_irq_ack_r(0);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static public static InitMachinePtr init_machine = new InitMachinePtr() { public void handler() 
/*TODO*///	{
/*TODO*///		last_speed_check = 0;
/*TODO*///		last_speech_write = 0x80;
/*TODO*///		last_sound_reset = 1;
/*TODO*///		speech_squeak = 0;
/*TODO*///	
/*TODO*///		atarigen_eeprom_reset();
/*TODO*///		atarigen_slapstic_reset();
/*TODO*///		atarigen_interrupt_reset(update_interrupts);
/*TODO*///		atarigen_scanline_timer_reset(scanline_update, 8);
/*TODO*///		atarigen_sound_io_reset(1);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Controller read dispatch.
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static int fake_inputs(int real_port, int fake_port)
/*TODO*///	{
/*TODO*///		int result = readinputport(real_port);
/*TODO*///		int fake = readinputport(fake_port);
/*TODO*///	
/*TODO*///		if ((fake & 0x01) != 0)			/* up */
/*TODO*///		{
/*TODO*///			if ((fake & 0x04) != 0)		/* up and left */
/*TODO*///				result &= ~0x20;
/*TODO*///			else if ((fake & 0x08) != 0)	/* up and right */
/*TODO*///				result &= ~0x10;
/*TODO*///			else					/* up only */
/*TODO*///				result &= ~0x30;
/*TODO*///		}
/*TODO*///		else if ((fake & 0x02) != 0)		/* down */
/*TODO*///		{
/*TODO*///			if ((fake & 0x04) != 0)		/* down and left */
/*TODO*///				result &= ~0x80;
/*TODO*///			else if ((fake & 0x08) != 0)	/* down and right */
/*TODO*///				result &= ~0x40;
/*TODO*///			else					/* down only */
/*TODO*///				result &= ~0xc0;
/*TODO*///		}
/*TODO*///		else if ((fake & 0x04) != 0)		/* left only */
/*TODO*///			result &= ~0x60;
/*TODO*///		else if ((fake & 0x08) != 0)		/* right only */
/*TODO*///			result &= ~0x90;
/*TODO*///	
/*TODO*///		return result;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr control_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		/* differentiate Gauntlet input from Vindicators 2 inputs via the refresh flag */
/*TODO*///		if (!vindctr2_screen_refresh)
/*TODO*///		{
/*TODO*///			/* Gauntlet case */
/*TODO*///			int p1 = input_port_6_r.handler(offset);
/*TODO*///			switch (offset)
/*TODO*///			{
/*TODO*///				case 0:
/*TODO*///					return readinputport(p1);
/*TODO*///				case 2:
/*TODO*///					return readinputport((p1 != 1) ? 1 : 0);
/*TODO*///				case 4:
/*TODO*///					return readinputport((p1 != 2) ? 2 : 0);
/*TODO*///				case 6:
/*TODO*///					return readinputport((p1 != 3) ? 3 : 0);
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			/* Vindicators 2 case */
/*TODO*///			switch (offset)
/*TODO*///			{
/*TODO*///				case 0:
/*TODO*///					return fake_inputs(0, 6);
/*TODO*///				case 2:
/*TODO*///					return fake_inputs(1, 7);
/*TODO*///				case 4:
/*TODO*///				case 6:
/*TODO*///					return readinputport(offset / 2);
/*TODO*///			}
/*TODO*///		}
/*TODO*///		return 0xffff;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	I/O read dispatch.
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr input_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		int temp;
/*TODO*///	
/*TODO*///		switch (offset)
/*TODO*///		{
/*TODO*///			case 0:
/*TODO*///				temp = input_port_4_r.handler(offset);
/*TODO*///				if (atarigen_cpu_to_sound_ready != 0) temp ^= 0x0020;
/*TODO*///				if (atarigen_sound_to_cpu_ready != 0) temp ^= 0x0010;
/*TODO*///				return temp;
/*TODO*///	
/*TODO*///			case 6:
/*TODO*///				return atarigen_sound_r(0);
/*TODO*///		}
/*TODO*///		return 0xffff;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr switch_6502_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		int temp = 0x30;
/*TODO*///	
/*TODO*///		if (atarigen_cpu_to_sound_ready != 0) temp ^= 0x80;
/*TODO*///		if (atarigen_sound_to_cpu_ready != 0) temp ^= 0x40;
/*TODO*///		if (tms5220_ready_r()) temp ^= 0x20;
/*TODO*///		if (!(input_port_4_r.handler(offset) & 0x0008)) temp ^= 0x10;
/*TODO*///	
/*TODO*///		return temp;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Controller write dispatch.
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr input_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		switch (offset)
/*TODO*///		{
/*TODO*///			case 0x0e:		/* sound CPU reset */
/*TODO*///			{
/*TODO*///				int newword = COMBINE_WORD(last_sound_reset, data);
/*TODO*///				int diff = newword ^ last_sound_reset;
/*TODO*///				last_sound_reset = newword;
/*TODO*///				if ((diff & 1) != 0)
/*TODO*///				{
/*TODO*///					cpu_set_reset_line(1, (newword & 1) ? CLEAR_LINE : ASSERT_LINE);
/*TODO*///					atarigen_sound_reset();
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Sound TMS5220 write.
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr tms5220_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		(void)offset;
/*TODO*///		speech_val = data;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Sound control write.
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr sound_ctl_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		switch (offset & 7)
/*TODO*///		{
/*TODO*///			case 0:	/* music reset, bit D7, low reset */
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 1:	/* speech write, bit D7, active low */
/*TODO*///				if (((data ^ last_speech_write) & 0x80) && (data & 0x80))
/*TODO*///					tms5220_data_w(0, speech_val);
/*TODO*///				last_speech_write = data;
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 2:	/* speech reset, bit D7, active low */
/*TODO*///				if (((data ^ last_speech_write) & 0x80) && (data & 0x80))
/*TODO*///					tms5220_reset();
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 3:	/* speech squeak, bit D7 */
/*TODO*///				data = 5 | ((data >> 6) & 2);
/*TODO*///				tms5220_set_frequency(ATARI_CLOCK_14MHz/2 / (16 - data));
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Sound mixer write.
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr mixer_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		(void)offset;
/*TODO*///		atarigen_set_ym2151_vol((data & 7) * 100 / 7);
/*TODO*///		atarigen_set_pokey_vol(((data >> 3) & 3) * 100 / 3);
/*TODO*///		atarigen_set_tms5220_vol(((data >> 5) & 7) * 100 / 7);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Speed cheats
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr speedup_68010_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		int result = READ_WORD(&speed_check[offset]);
/*TODO*///		int time = cpu_gettotalcycles();
/*TODO*///		int delta = time - last_speed_check;
/*TODO*///	
/*TODO*///		last_speed_check = time;
/*TODO*///		if (delta <= 100 && result == 0 && delta >= 0)
/*TODO*///			cpu_spin();
/*TODO*///	
/*TODO*///		return result;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr speedup_68010_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		last_speed_check -= 1000;
/*TODO*///		COMBINE_WORD_MEM(&speed_check[offset], data);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Main CPU memory handlers
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static MemoryReadAddress main_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x000000, 0x07ffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( 0x800000, 0x801fff, MRA_BANK1 ),
/*TODO*///		new MemoryReadAddress( 0x802000, 0x802fff, atarigen_eeprom_r ),
/*TODO*///		new MemoryReadAddress( 0x803000, 0x803007, control_r ),
/*TODO*///		new MemoryReadAddress( 0x803008, 0x80300f, input_r ),
/*TODO*///		new MemoryReadAddress( 0x900000, 0x901fff, MRA_BANK2 ),
/*TODO*///		new MemoryReadAddress( 0x902000, 0x903fff, MRA_BANK3 ),
/*TODO*///		new MemoryReadAddress( 0x904000, 0x904fff, MRA_BANK4 ),
/*TODO*///		new MemoryReadAddress( 0x905000, 0x905eff, MRA_BANK5 ),
/*TODO*///		new MemoryReadAddress( 0x905f00, 0x905fff, MRA_BANK6 ),
/*TODO*///		new MemoryReadAddress( 0x910000, 0x9107ff, paletteram_word_r ),
/*TODO*///		new MemoryReadAddress( 0x930000, 0x930003, MRA_BANK7 ),
/*TODO*///		new MemoryReadAddress( -1 )  /* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	static MemoryWriteAddress main_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x000000, 0x07ffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( 0x800000, 0x801fff, MWA_BANK1 ),
/*TODO*///		new MemoryWriteAddress( 0x802000, 0x802fff, atarigen_eeprom_w, atarigen_eeprom, atarigen_eeprom_size ),
/*TODO*///		new MemoryWriteAddress( 0x803100, 0x803103, watchdog_reset_w ),
/*TODO*///		new MemoryWriteAddress( 0x803120, 0x80312f, input_w ),
/*TODO*///		new MemoryWriteAddress( 0x803140, 0x803143, atarigen_video_int_ack_w ),
/*TODO*///		new MemoryWriteAddress( 0x803150, 0x803153, atarigen_eeprom_enable_w ),
/*TODO*///		new MemoryWriteAddress( 0x803170, 0x803173, atarigen_sound_w ),
/*TODO*///		new MemoryWriteAddress( 0x900000, 0x901fff, gauntlet_playfieldram_w, atarigen_playfieldram, atarigen_playfieldram_size ),
/*TODO*///		new MemoryWriteAddress( 0x902000, 0x903fff, MWA_BANK3, atarigen_spriteram, atarigen_spriteram_size ),
/*TODO*///		new MemoryWriteAddress( 0x904000, 0x904fff, MWA_BANK4 ),
/*TODO*///		new MemoryWriteAddress( 0x905000, 0x905eff, MWA_BANK5, atarigen_alpharam, atarigen_alpharam_size ),
/*TODO*///		new MemoryWriteAddress( 0x905f6e, 0x905f6f, gauntlet_vscroll_w, atarigen_vscroll ),
/*TODO*///		new MemoryWriteAddress( 0x905f00, 0x905fff, MWA_BANK6 ),
/*TODO*///		new MemoryWriteAddress( 0x910000, 0x9107ff, paletteram_IIIIRRRRGGGGBBBB_word_w, paletteram ),
/*TODO*///		new MemoryWriteAddress( 0x930000, 0x930001, gauntlet_hscroll_w, atarigen_hscroll ),
/*TODO*///		new MemoryWriteAddress( -1 )  /* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Sound CPU memory handlers
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static MemoryReadAddress sound_readmem[] =
/*TODO*///	{
/*TODO*///		new MemoryReadAddress( 0x0000, 0x0fff, MRA_RAM ),
/*TODO*///		new MemoryReadAddress( 0x1010, 0x101f, atarigen_6502_sound_r ),
/*TODO*///		new MemoryReadAddress( 0x1020, 0x102f, input_port_5_r ),
/*TODO*///		new MemoryReadAddress( 0x1030, 0x103f, switch_6502_r ),
/*TODO*///		new MemoryReadAddress( 0x1800, 0x180f, pokey1_r ),
/*TODO*///		new MemoryReadAddress( 0x1811, 0x1811, YM2151_status_port_0_r ),
/*TODO*///		new MemoryReadAddress( 0x1830, 0x183f, atarigen_6502_irq_ack_r ),
/*TODO*///		new MemoryReadAddress( 0x4000, 0xffff, MRA_ROM ),
/*TODO*///		new MemoryReadAddress( -1 )  /* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	static MemoryWriteAddress sound_writemem[] =
/*TODO*///	{
/*TODO*///		new MemoryWriteAddress( 0x0000, 0x0fff, MWA_RAM ),
/*TODO*///		new MemoryWriteAddress( 0x1000, 0x100f, atarigen_6502_sound_w ),
/*TODO*///		new MemoryWriteAddress( 0x1020, 0x102f, mixer_w ),
/*TODO*///		new MemoryWriteAddress( 0x1030, 0x103f, sound_ctl_w ),
/*TODO*///		new MemoryWriteAddress( 0x1800, 0x180f, pokey1_w ),
/*TODO*///		new MemoryWriteAddress( 0x1810, 0x1810, YM2151_register_port_0_w ),
/*TODO*///		new MemoryWriteAddress( 0x1811, 0x1811, YM2151_data_port_0_w ),
/*TODO*///		new MemoryWriteAddress( 0x1820, 0x182f, tms5220_w ),
/*TODO*///		new MemoryWriteAddress( 0x1830, 0x183f, atarigen_6502_irq_ack_w ),
/*TODO*///		new MemoryWriteAddress( 0x4000, 0xffff, MWA_ROM ),
/*TODO*///		new MemoryWriteAddress( -1 )  /* end of table */
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Port definitions
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_gauntlet = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START(); 	/* 803000 */
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x000c, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER1 | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER1 | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER1 | IPF_8WAY );
/*TODO*///		PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///	
/*TODO*///		PORT_START(); 	/* 803002 */
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x000c, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER2 | IPF_8WAY );
/*TODO*///		PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///	
/*TODO*///		PORT_START(); 	/* 803004 */
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_START3 );
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x000c, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER3 | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER3 | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER3 | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER3 | IPF_8WAY );
/*TODO*///		PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///	
/*TODO*///		PORT_START(); 	/* 803006 */
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_START4 );
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4 );
/*TODO*///		PORT_BIT( 0x000c, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER4 | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_PLAYER4 | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_PLAYER4 | IPF_8WAY );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_PLAYER4 | IPF_8WAY );
/*TODO*///		PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///	
/*TODO*///		PORT_START(); 	/* 803008 */
/*TODO*///		PORT_BIT( 0x0007, IP_ACTIVE_HIGH, IPT_UNUSED );
/*TODO*///		PORT_SERVICE( 0x0008, IP_ACTIVE_LOW );
/*TODO*///		PORT_BIT( 0x0030, IP_ACTIVE_HIGH, IPT_SPECIAL );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW,  IPT_VBLANK );
/*TODO*///		PORT_BIT( 0xff80, IP_ACTIVE_HIGH, IPT_UNUSED );
/*TODO*///	
/*TODO*///		PORT_START(); 	/* 1020 (sound) */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN4 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN3 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///	
/*TODO*///		PORT_START(); 	/* Fake! */
/*TODO*///		PORT_DIPNAME( 0x0003, 0x0000, "Player 1 Plays" );
/*TODO*///		PORT_DIPSETTING(      0x0000, "Red/Warrior" );
/*TODO*///		PORT_DIPSETTING(      0x0001, "Blue/Valkyrie" );
/*TODO*///		PORT_DIPSETTING(      0x0002, "Yellow/Wizard" );
/*TODO*///		PORT_DIPSETTING(      0x0003, "Green/Elf" );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_vindctr2 = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START(); 	/* 803000 */
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_UP    | IPF_PLAYER1 | IPF_2WAY );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_UP   | IPF_PLAYER1 | IPF_2WAY );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_DOWN  | IPF_PLAYER1 | IPF_2WAY );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_DOWN | IPF_PLAYER1 | IPF_2WAY );
/*TODO*///		PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///	
/*TODO*///		PORT_START(); 	/* 803002 */
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_UP    | IPF_PLAYER2 | IPF_2WAY );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_UP   | IPF_PLAYER2 | IPF_2WAY );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_DOWN  | IPF_PLAYER2 | IPF_2WAY );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_DOWN | IPF_PLAYER2 | IPF_2WAY );
/*TODO*///		PORT_BIT( 0xff00, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///	
/*TODO*///		PORT_START(); 	/* 803004 */
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BIT( 0xfffc, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///	
/*TODO*///		PORT_START(); 	/* 803006 */
/*TODO*///		PORT_BIT( 0xffff, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///	
/*TODO*///		PORT_START(); 	/* 803008 */
/*TODO*///		PORT_BIT( 0x0007, IP_ACTIVE_HIGH, IPT_UNUSED );
/*TODO*///		PORT_SERVICE( 0x0008, IP_ACTIVE_LOW );
/*TODO*///		PORT_BIT( 0x0030, IP_ACTIVE_HIGH, IPT_SPECIAL );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW,  IPT_VBLANK );
/*TODO*///		PORT_BIT( 0xff80, IP_ACTIVE_HIGH, IPT_UNUSED );
/*TODO*///	
/*TODO*///		PORT_START(); 	/* 1020 (sound) */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN4 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN3 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0xf0, IP_ACTIVE_LOW, IPT_UNUSED );
/*TODO*///	
/*TODO*///		PORT_START(); 	/* single joystick */
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_CHEAT | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_CHEAT | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_CHEAT | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_CHEAT | IPF_PLAYER1 );
/*TODO*///	
/*TODO*///		PORT_START(); 	/* single joystick */
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_CHEAT | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_CHEAT | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_CHEAT | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_CHEAT | IPF_PLAYER2 );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Graphics definitions
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static GfxLayout anlayout = new GfxLayout
/*TODO*///	(
/*TODO*///		8,8,
/*TODO*///		RGN_FRAC(1,1),
/*TODO*///		2,
/*TODO*///		new int[] { 0, 4 },
/*TODO*///		new int[] { 0, 1, 2, 3, 8, 9, 10, 11 },
/*TODO*///		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
/*TODO*///		8*16
/*TODO*///	);
/*TODO*///	
/*TODO*///	
/*TODO*///	static GfxLayout pfmolayout = new GfxLayout
/*TODO*///	(
/*TODO*///		8,8,
/*TODO*///		RGN_FRAC(1,4),
/*TODO*///		4,
/*TODO*///		new int[] { RGN_FRAC(3,4), RGN_FRAC(2,4), RGN_FRAC(1,4), RGN_FRAC(0,4) },
/*TODO*///		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
/*TODO*///		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
/*TODO*///		8*8
/*TODO*///	);
/*TODO*///	
/*TODO*///	
/*TODO*///	static GfxDecodeInfo gfxdecodeinfo[] =
/*TODO*///	{
/*TODO*///		new GfxDecodeInfo( REGION_GFX2, 0, pfmolayout,  256, 32 ),
/*TODO*///		new GfxDecodeInfo( REGION_GFX1, 0, anlayout,      0, 64 ),
/*TODO*///		new GfxDecodeInfo( -1 ) /* end of array */
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Sound definitions
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static YM2151interface ym2151_interface = new YM2151interface
/*TODO*///	(
/*TODO*///		1,
/*TODO*///		ATARI_CLOCK_14MHz/4,
/*TODO*///		new int[] { YM3012_VOL(48,MIXER_PAN_LEFT,48,MIXER_PAN_RIGHT) },
/*TODO*///		new WriteYmHandlerPtr[] { 0 }
/*TODO*///	);
/*TODO*///	
/*TODO*///	
/*TODO*///	static POKEYinterface pokey_interface = new POKEYinterface
/*TODO*///	(
/*TODO*///		1,
/*TODO*///		ATARI_CLOCK_14MHz/8,
/*TODO*///		new int[] { 32 },
/*TODO*///	);
/*TODO*///	
/*TODO*///	
/*TODO*///	static struct TMS5220interface tms5220_interface =
/*TODO*///	{
/*TODO*///		ATARI_CLOCK_14MHz/2/11,	/* potentially ATARI_CLOCK_14MHz/2/9 as well */
/*TODO*///		80,
/*TODO*///		0
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Machine driver
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static MachineDriver machine_driver_gauntlet = new MachineDriver
/*TODO*///	(
/*TODO*///		/* basic machine hardware */
/*TODO*///		new MachineCPU[] {
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_M68010,		/* verified */
/*TODO*///				ATARI_CLOCK_14MHz/2,
/*TODO*///				main_readmem,main_writemem,null,null,
/*TODO*///				atarigen_video_int_gen,1
/*TODO*///			),
/*TODO*///			new MachineCPU(
/*TODO*///				CPU_M6502,
/*TODO*///				ATARI_CLOCK_14MHz/8,
/*TODO*///				sound_readmem,sound_writemem,null,null,
/*TODO*///				null,null
/*TODO*///			)
/*TODO*///		},
/*TODO*///		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
/*TODO*///		1,
/*TODO*///		init_machine,
/*TODO*///	
/*TODO*///		/* video hardware */
/*TODO*///		42*8, 30*8, new rectangle( 0*8, 42*8-1, 0*8, 30*8-1 ),
/*TODO*///		gfxdecodeinfo,
/*TODO*///		1024,1024,
/*TODO*///		null,
/*TODO*///	
/*TODO*///		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK,
/*TODO*///		null,
/*TODO*///		gauntlet_vh_start,
/*TODO*///		gauntlet_vh_stop,
/*TODO*///		gauntlet_vh_screenrefresh,
/*TODO*///	
/*TODO*///		/* sound hardware */
/*TODO*///		SOUND_SUPPORTS_STEREO,0,0,0,
/*TODO*///		new MachineSound[] {
/*TODO*///			new MachineSound(
/*TODO*///				SOUND_YM2151,
/*TODO*///				ym2151_interface
/*TODO*///			),
/*TODO*///			new MachineSound(
/*TODO*///				SOUND_POKEY,
/*TODO*///				pokey_interface
/*TODO*///			),
/*TODO*///			new MachineSound(
/*TODO*///				SOUND_TMS5220,
/*TODO*///				tms5220_interface
/*TODO*///			)
/*TODO*///		},
/*TODO*///	
/*TODO*///		atarigen_nvram_handler
/*TODO*///	);
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	ROM definition(s)
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_gauntlet = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x80000, REGION_CPU1 );/* 8*64k for 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "gauntlt1.9a",  0x00000, 0x08000, 0x46fe8743 );
/*TODO*///		ROM_LOAD_ODD ( "gauntlt1.9b",  0x00000, 0x08000, 0x276e15c4 );
/*TODO*///		ROM_LOAD_EVEN( "gauntlt1.10a", 0x38000, 0x04000, 0x6d99ed51 );
/*TODO*///		ROM_LOAD_ODD ( "gauntlt1.10b", 0x38000, 0x04000, 0x545ead91 );
/*TODO*///		ROM_LOAD_EVEN( "gauntlt1.7a",  0x40000, 0x08000, 0x6fb8419c );
/*TODO*///		ROM_LOAD_ODD ( "gauntlt1.7b",  0x40000, 0x08000, 0x931bd2a0 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for 6502 code */
/*TODO*///		ROM_LOAD( "gauntlt1.16r", 0x4000, 0x4000, 0x6ee7f3cc );
/*TODO*///		ROM_LOAD( "gauntlt1.16s", 0x8000, 0x8000, 0xfa19861f );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "gauntlt1.6p",  0x00000, 0x04000, 0x6c276a1d );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "gauntlt1.1a",  0x00000, 0x08000, 0x91700f33 );
/*TODO*///		ROM_LOAD( "gauntlt1.1b",  0x08000, 0x08000, 0x869330be );
/*TODO*///	
/*TODO*///		ROM_LOAD( "gauntlt1.1l",  0x10000, 0x08000, 0xd497d0a8 );
/*TODO*///		ROM_LOAD( "gauntlt1.1mn", 0x18000, 0x08000, 0x29ef9882 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "gauntlt1.2a",  0x20000, 0x08000, 0x9510b898 );
/*TODO*///		ROM_LOAD( "gauntlt1.2b",  0x28000, 0x08000, 0x11e0ac5b );
/*TODO*///	
/*TODO*///		ROM_LOAD( "gauntlt1.2l",  0x30000, 0x08000, 0x29a5db41 );
/*TODO*///		ROM_LOAD( "gauntlt1.2mn", 0x38000, 0x08000, 0x8bf3b263 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_gauntir1 = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x80000, REGION_CPU1 );/* 8*64k for 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "gaun1ir1.9a",  0x00000, 0x08000, 0xfd871f81 );
/*TODO*///		ROM_LOAD_ODD ( "gaun1ir1.9b",  0x00000, 0x08000, 0xbcb2fb1d );
/*TODO*///		ROM_LOAD_EVEN( "gaun1ir1.10a", 0x38000, 0x04000, 0x4642cd95 );
/*TODO*///		ROM_LOAD_ODD ( "gaun1ir1.10b", 0x38000, 0x04000, 0xc8df945e );
/*TODO*///		ROM_LOAD_EVEN( "gaun1ir1.7a",  0x40000, 0x08000, 0xc57377b3 );
/*TODO*///		ROM_LOAD_ODD ( "gaun1ir1.7b",  0x40000, 0x08000, 0x1cac2071 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for 6502 code */
/*TODO*///		ROM_LOAD( "gauntlt1.16r", 0x4000, 0x4000, 0x6ee7f3cc );
/*TODO*///		ROM_LOAD( "gauntlt1.16s", 0x8000, 0x8000, 0xfa19861f );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "gauntlt1.6p",  0x00000, 0x04000, 0x6c276a1d );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "gauntlt1.1a",  0x00000, 0x08000, 0x91700f33 );
/*TODO*///		ROM_LOAD( "gauntlt1.1b",  0x08000, 0x08000, 0x869330be );
/*TODO*///	
/*TODO*///		ROM_LOAD( "gauntlt1.1l",  0x10000, 0x08000, 0xd497d0a8 );
/*TODO*///		ROM_LOAD( "gauntlt1.1mn", 0x18000, 0x08000, 0x29ef9882 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "gauntlt1.2a",  0x20000, 0x08000, 0x9510b898 );
/*TODO*///		ROM_LOAD( "gauntlt1.2b",  0x28000, 0x08000, 0x11e0ac5b );
/*TODO*///	
/*TODO*///		ROM_LOAD( "gauntlt1.2l",  0x30000, 0x08000, 0x29a5db41 );
/*TODO*///		ROM_LOAD( "gauntlt1.2mn", 0x38000, 0x08000, 0x8bf3b263 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_gauntir2 = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x80000, REGION_CPU1 );/* 8*64k for 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "gaun1ir1.9a",  0x00000, 0x08000, 0xfd871f81 );
/*TODO*///		ROM_LOAD_ODD ( "gaun1ir1.9b",  0x00000, 0x08000, 0xbcb2fb1d );
/*TODO*///		ROM_LOAD_EVEN( "gaun1ir1.10a", 0x38000, 0x04000, 0x4642cd95 );
/*TODO*///		ROM_LOAD_ODD ( "gaun1ir1.10b", 0x38000, 0x04000, 0xc8df945e );
/*TODO*///		ROM_LOAD_EVEN( "gaun1ir2.7a",  0x40000, 0x08000, 0x73e1ad79 );
/*TODO*///		ROM_LOAD_ODD ( "gaun1ir2.7b",  0x40000, 0x08000, 0xfd248cea );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for 6502 code */
/*TODO*///		ROM_LOAD( "gauntlt1.16r", 0x4000, 0x4000, 0x6ee7f3cc );
/*TODO*///		ROM_LOAD( "gauntlt1.16s", 0x8000, 0x8000, 0xfa19861f );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "gauntlt1.6p",  0x00000, 0x04000, 0x6c276a1d );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "gauntlt1.1a",  0x00000, 0x08000, 0x91700f33 );
/*TODO*///		ROM_LOAD( "gauntlt1.1b",  0x08000, 0x08000, 0x869330be );
/*TODO*///	
/*TODO*///		ROM_LOAD( "gauntlt1.1l",  0x10000, 0x08000, 0xd497d0a8 );
/*TODO*///		ROM_LOAD( "gauntlt1.1mn", 0x18000, 0x08000, 0x29ef9882 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "gauntlt1.2a",  0x20000, 0x08000, 0x9510b898 );
/*TODO*///		ROM_LOAD( "gauntlt1.2b",  0x28000, 0x08000, 0x11e0ac5b );
/*TODO*///	
/*TODO*///		ROM_LOAD( "gauntlt1.2l",  0x30000, 0x08000, 0x29a5db41 );
/*TODO*///		ROM_LOAD( "gauntlt1.2mn", 0x38000, 0x08000, 0x8bf3b263 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_gaunt2p = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x80000, REGION_CPU1 );/* 8*64k for 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "gaunt2p.9a",   0x00000, 0x08000, 0x8784133f );
/*TODO*///		ROM_LOAD_ODD ( "gaunt2p.9b",   0x00000, 0x08000, 0x2843bde3 );
/*TODO*///		ROM_LOAD_EVEN( "gauntlt1.10a", 0x38000, 0x04000, 0x6d99ed51 );
/*TODO*///		ROM_LOAD_ODD ( "gauntlt1.10b", 0x38000, 0x04000, 0x545ead91 );
/*TODO*///		ROM_LOAD_EVEN( "gaunt2p.7a",   0x40000, 0x08000, 0x5b4ee415 );
/*TODO*///		ROM_LOAD_ODD ( "gaunt2p.7b",   0x40000, 0x08000, 0x41f5c9e2 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for 6502 code */
/*TODO*///		ROM_LOAD( "gauntlt1.16r", 0x4000, 0x4000, 0x6ee7f3cc );
/*TODO*///		ROM_LOAD( "gauntlt1.16s", 0x8000, 0x8000, 0xfa19861f );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "gauntlt1.6p",  0x00000, 0x04000, 0x6c276a1d );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "gauntlt1.1a",  0x00000, 0x08000, 0x91700f33 );
/*TODO*///		ROM_LOAD( "gauntlt1.1b",  0x08000, 0x08000, 0x869330be );
/*TODO*///	
/*TODO*///		ROM_LOAD( "gauntlt1.1l",  0x10000, 0x08000, 0xd497d0a8 );
/*TODO*///		ROM_LOAD( "gauntlt1.1mn", 0x18000, 0x08000, 0x29ef9882 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "gauntlt1.2a",  0x20000, 0x08000, 0x9510b898 );
/*TODO*///		ROM_LOAD( "gauntlt1.2b",  0x28000, 0x08000, 0x11e0ac5b );
/*TODO*///	
/*TODO*///		ROM_LOAD( "gauntlt1.2l",  0x30000, 0x08000, 0x29a5db41 );
/*TODO*///		ROM_LOAD( "gauntlt1.2mn", 0x38000, 0x08000, 0x8bf3b263 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_gaunt2 = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x80000, REGION_CPU1 );/* 8*64k for 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "gauntlt2.9a",  0x00000, 0x08000, 0x46fe8743 );
/*TODO*///		ROM_LOAD_ODD ( "gauntlt2.9b",  0x00000, 0x08000, 0x276e15c4 );
/*TODO*///		ROM_LOAD_EVEN( "gauntlt2.10a", 0x38000, 0x04000, 0x45dfda47 );
/*TODO*///		ROM_LOAD_ODD ( "gauntlt2.10b", 0x38000, 0x04000, 0x343c029c );
/*TODO*///		ROM_LOAD_EVEN( "gauntlt2.7a",  0x40000, 0x08000, 0x58a0a9a3 );
/*TODO*///		ROM_LOAD_ODD ( "gauntlt2.7b",  0x40000, 0x08000, 0x658f0da8 );
/*TODO*///		ROM_LOAD_EVEN( "gauntlt2.6a",  0x50000, 0x08000, 0xae301bba );
/*TODO*///		ROM_LOAD_ODD ( "gauntlt2.6b",  0x50000, 0x08000, 0xe94aaa8a );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for 6502 code */
/*TODO*///		ROM_LOAD( "gauntlt2.16r", 0x4000, 0x4000, 0x5c731006 );
/*TODO*///		ROM_LOAD( "gauntlt2.16s", 0x8000, 0x8000, 0xdc3591e7 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "gauntlt2.6p",  0x00000, 0x04000, 0xd101905d );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x60000, REGION_GFX2 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "gauntlt2.1a",  0x00000, 0x08000, 0x09df6e23 );
/*TODO*///		ROM_LOAD( "gauntlt2.1b",  0x08000, 0x08000, 0x869330be );
/*TODO*///		ROM_LOAD( "gauntlt2.1c",  0x10000, 0x04000, 0xe4c98f01 );
/*TODO*///		ROM_RELOAD(               0x14000, 0x04000 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "gauntlt2.1l",  0x18000, 0x08000, 0x33cb476e );
/*TODO*///		ROM_LOAD( "gauntlt2.1mn", 0x20000, 0x08000, 0x29ef9882 );
/*TODO*///		ROM_LOAD( "gauntlt2.1p",  0x28000, 0x04000, 0xc4857879 );
/*TODO*///		ROM_RELOAD(               0x2c000, 0x04000 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "gauntlt2.2a",  0x30000, 0x08000, 0xf71e2503 );
/*TODO*///		ROM_LOAD( "gauntlt2.2b",  0x38000, 0x08000, 0x11e0ac5b );
/*TODO*///		ROM_LOAD( "gauntlt2.2c",  0x40000, 0x04000, 0xd9c2c2d1 );
/*TODO*///		ROM_RELOAD(               0x44000, 0x04000 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "gauntlt2.2l",  0x48000, 0x08000, 0x9e30b2e9 );
/*TODO*///		ROM_LOAD( "gauntlt2.2mn", 0x50000, 0x08000, 0x8bf3b263 );
/*TODO*///		ROM_LOAD( "gauntlt2.2p",  0x58000, 0x04000, 0xa32c732a );
/*TODO*///		ROM_RELOAD(               0x5c000, 0x04000 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_vindctr2 = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( 0x80000, REGION_CPU1 );/* 8*64k for 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "1186", 0x00000, 0x08000, 0xaf138263 );
/*TODO*///		ROM_LOAD_ODD ( "1187", 0x00000, 0x08000, 0x44baff64 );
/*TODO*///		ROM_LOAD_EVEN( "1196", 0x38000, 0x04000, 0xc92bf6dd );
/*TODO*///		ROM_LOAD_ODD ( "1197", 0x38000, 0x04000, 0xd7ace347 );
/*TODO*///		ROM_LOAD_EVEN( "3188", 0x40000, 0x08000, 0x10f558d2 );
/*TODO*///		ROM_LOAD_ODD ( "3189", 0x40000, 0x08000, 0x302e24b6 );
/*TODO*///		ROM_LOAD_EVEN( "2190", 0x50000, 0x08000, 0xe7dc2b74 );
/*TODO*///		ROM_LOAD_ODD ( "2191", 0x50000, 0x08000, 0xed8ed86e );
/*TODO*///		ROM_LOAD_EVEN( "2192", 0x60000, 0x08000, 0xeec2c93d );
/*TODO*///		ROM_LOAD_ODD ( "2193", 0x60000, 0x08000, 0x3fbee9aa );
/*TODO*///		ROM_LOAD_EVEN( "1194", 0x70000, 0x08000, 0xe6bcf458 );
/*TODO*///		ROM_LOAD_ODD ( "1195", 0x70000, 0x08000, 0xb9bf245d );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x10000, REGION_CPU2 );/* 64k for 6502 code */
/*TODO*///		ROM_LOAD( "1160", 0x4000, 0x4000, 0xeef0a003 );
/*TODO*///		ROM_LOAD( "1161", 0x8000, 0x8000, 0x68c74337 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "1198",  0x00000, 0x04000, 0xf99b631a );
/*TODO*///	
/*TODO*///		ROM_REGION( 0xc0000, REGION_GFX2 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "1101", 0x00000, 0x08000, 0xdd3833ad );
/*TODO*///		ROM_LOAD( "1166", 0x08000, 0x08000, 0xe2db50a0 );
/*TODO*///		ROM_LOAD( "1170", 0x10000, 0x08000, 0xf050ab43 );
/*TODO*///		ROM_LOAD( "1174", 0x18000, 0x08000, 0xb6704bd1 );
/*TODO*///		ROM_LOAD( "1178", 0x20000, 0x08000, 0xd3006f05 );
/*TODO*///		ROM_LOAD( "1182", 0x28000, 0x08000, 0x9046e985 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "1102", 0x30000, 0x08000, 0xd505b04a );
/*TODO*///		ROM_LOAD( "1167", 0x38000, 0x08000, 0x1869c76d );
/*TODO*///		ROM_LOAD( "1171", 0x40000, 0x08000, 0x1b229c2b );
/*TODO*///		ROM_LOAD( "1175", 0x48000, 0x08000, 0x73c41aca );
/*TODO*///		ROM_LOAD( "1179", 0x50000, 0x08000, 0x9b7cb0ef );
/*TODO*///		ROM_LOAD( "1183", 0x58000, 0x08000, 0x393bba42 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "1103", 0x60000, 0x08000, 0x50e76162 );
/*TODO*///		ROM_LOAD( "1168", 0x68000, 0x08000, 0x35c78469 );
/*TODO*///		ROM_LOAD( "1172", 0x70000, 0x08000, 0x314ac268 );
/*TODO*///		ROM_LOAD( "1176", 0x78000, 0x08000, 0x061d79db );
/*TODO*///		ROM_LOAD( "1180", 0x80000, 0x08000, 0x89c1fe16 );
/*TODO*///		ROM_LOAD( "1184", 0x88000, 0x08000, 0x541209d3 );
/*TODO*///	
/*TODO*///		ROM_LOAD( "1104", 0x90000, 0x08000, 0x9484ba65 );
/*TODO*///		ROM_LOAD( "1169", 0x98000, 0x08000, 0x132d3337 );
/*TODO*///		ROM_LOAD( "1173", 0xa0000, 0x08000, 0x98de2426 );
/*TODO*///		ROM_LOAD( "1177", 0xa8000, 0x08000, 0x9d0824f8 );
/*TODO*///		ROM_LOAD( "1181", 0xb0000, 0x08000, 0x9e62b27c );
/*TODO*///		ROM_LOAD( "1185", 0xb8000, 0x08000, 0x9d62f6b7 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	ROM decoding
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static void rom_decode(void)
/*TODO*///	{
/*TODO*///		UINT32 *p1, *p2, temp;
/*TODO*///		UINT8 *data;
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		/* swap the top and bottom halves of the main CPU ROM images */
/*TODO*///		p1 = (UINT32 *)&memory_region(REGION_CPU1)[0x000000];
/*TODO*///		p2 = (UINT32 *)&memory_region(REGION_CPU1)[0x008000];
/*TODO*///		for (i = 0; i < 0x8000 / 4; i++)
/*TODO*///			temp = *p1, *p1++ = *p2, *p2++ = temp;
/*TODO*///		p1 = (UINT32 *)&memory_region(REGION_CPU1)[0x040000];
/*TODO*///		p2 = (UINT32 *)&memory_region(REGION_CPU1)[0x048000];
/*TODO*///		for (i = 0; i < 0x8000 / 4; i++)
/*TODO*///			temp = *p1, *p1++ = *p2, *p2++ = temp;
/*TODO*///		p1 = (UINT32 *)&memory_region(REGION_CPU1)[0x050000];
/*TODO*///		p2 = (UINT32 *)&memory_region(REGION_CPU1)[0x058000];
/*TODO*///		for (i = 0; i < 0x8000 / 4; i++)
/*TODO*///			temp = *p1, *p1++ = *p2, *p2++ = temp;
/*TODO*///		p1 = (UINT32 *)&memory_region(REGION_CPU1)[0x060000];
/*TODO*///		p2 = (UINT32 *)&memory_region(REGION_CPU1)[0x068000];
/*TODO*///		for (i = 0; i < 0x8000 / 4; i++)
/*TODO*///			temp = *p1, *p1++ = *p2, *p2++ = temp;
/*TODO*///		p1 = (UINT32 *)&memory_region(REGION_CPU1)[0x070000];
/*TODO*///		p2 = (UINT32 *)&memory_region(REGION_CPU1)[0x078000];
/*TODO*///		for (i = 0; i < 0x8000 / 4; i++)
/*TODO*///			temp = *p1, *p1++ = *p2, *p2++ = temp;
/*TODO*///	
/*TODO*///		/* highly strange -- the address bits on the chip at 2J (and only that
/*TODO*///		   chip) are scrambled -- this is verified on the schematics! */
/*TODO*///		if (memory_region_length(REGION_GFX2) >= 0xc0000)
/*TODO*///		{
/*TODO*///			data = malloc(0x8000);
/*TODO*///			if (data != 0)
/*TODO*///			{
/*TODO*///				memcpy(data, &memory_region(REGION_GFX2)[0x88000], 0x8000);
/*TODO*///				for (i = 0; i < 0x8000; i++)
/*TODO*///				{
/*TODO*///					int srcoffs = (i & 0x4000) | ((i << 11) & 0x3800) | ((i >> 3) & 0x07ff);
/*TODO*///					memory_region(REGION_GFX2)[0x88000 + i] = data[srcoffs];
/*TODO*///				}
/*TODO*///				free(data);
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* also invert the graphics bits on the playfield and motion objects */
/*TODO*///		for (i = 0; i < memory_region_length(REGION_GFX2); i++)
/*TODO*///			memory_region(REGION_GFX2)[i] ^= 0xff;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Driver initialization
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static public static InitDriverPtr init_gauntlet = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		atarigen_eeprom_default = NULL;
/*TODO*///		atarigen_slapstic_init(0, 0x038000, 104);
/*TODO*///	
/*TODO*///		vindctr2_screen_refresh = 0;
/*TODO*///	
/*TODO*///		/* speed up the 6502 */
/*TODO*///		atarigen_init_6502_speedup(1, 0x410f, 0x4127);
/*TODO*///	
/*TODO*///		/* speed up the 68010 */
/*TODO*///		speed_check = install_mem_write_handler(0, 0x904002, 0x904003, speedup_68010_w);
/*TODO*///		install_mem_read_handler(0, 0x904002, 0x904003, speedup_68010_r);
/*TODO*///	
/*TODO*///		/* display messages */
/*TODO*///		atarigen_show_sound_message();
/*TODO*///	
/*TODO*///		rom_decode();
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	static public static InitDriverPtr init_gaunt2p = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		atarigen_eeprom_default = NULL;
/*TODO*///		atarigen_slapstic_init(0, 0x038000, 107);
/*TODO*///	
/*TODO*///		vindctr2_screen_refresh = 0;
/*TODO*///	
/*TODO*///		/* speed up the 6502 */
/*TODO*///		atarigen_init_6502_speedup(1, 0x410f, 0x4127);
/*TODO*///	
/*TODO*///		/* speed up the 68010 */
/*TODO*///		speed_check = install_mem_write_handler(0, 0x904002, 0x904003, speedup_68010_w);
/*TODO*///		install_mem_read_handler(0, 0x904002, 0x904003, speedup_68010_r);
/*TODO*///	
/*TODO*///		/* display messages */
/*TODO*///		atarigen_show_sound_message();
/*TODO*///	
/*TODO*///		rom_decode();
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	static public static InitDriverPtr init_gauntlet2 = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		atarigen_eeprom_default = NULL;
/*TODO*///		atarigen_slapstic_init(0, 0x038000, 106);
/*TODO*///	
/*TODO*///		vindctr2_screen_refresh = 0;
/*TODO*///	
/*TODO*///		/* speed up the 6502 */
/*TODO*///		atarigen_init_6502_speedup(1, 0x410f, 0x4127);
/*TODO*///	
/*TODO*///		/* speed up the 68010 */
/*TODO*///		speed_check = install_mem_write_handler(0, 0x904002, 0x904003, speedup_68010_w);
/*TODO*///		install_mem_read_handler(0, 0x904002, 0x904003, speedup_68010_r);
/*TODO*///	
/*TODO*///		/* display messages */
/*TODO*///		atarigen_show_sound_message();
/*TODO*///	
/*TODO*///		rom_decode();
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	static public static InitDriverPtr init_vindctr2 = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		atarigen_eeprom_default = NULL;
/*TODO*///		atarigen_slapstic_init(0, 0x038000, 118);
/*TODO*///	
/*TODO*///		vindctr2_screen_refresh = 1;
/*TODO*///	
/*TODO*///		/* speed up the 6502 */
/*TODO*///		atarigen_init_6502_speedup(1, 0x40ff, 0x4117);
/*TODO*///	
/*TODO*///		/* display messages */
/*TODO*///		atarigen_show_sound_message();
/*TODO*///	
/*TODO*///		rom_decode();
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Game driver(s)
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	public static GameDriver driver_gauntlet	   = new GameDriver("1985"	,"gauntlet"	,"gauntlet.java"	,rom_gauntlet,null	,machine_driver_gauntlet	,input_ports_gauntlet	,init_gauntlet	,ROT0	,	"Atari Games", "Gauntlet" )
/*TODO*///	public static GameDriver driver_gauntir1	   = new GameDriver("1985"	,"gauntir1"	,"gauntlet.java"	,rom_gauntir1,driver_gauntlet	,machine_driver_gauntlet	,input_ports_gauntlet	,init_gauntlet	,ROT0	,	"Atari Games", "Gauntlet (Intermediate Release 1)" )
/*TODO*///	public static GameDriver driver_gauntir2	   = new GameDriver("1985"	,"gauntir2"	,"gauntlet.java"	,rom_gauntir2,driver_gauntlet	,machine_driver_gauntlet	,input_ports_gauntlet	,init_gauntlet	,ROT0	,	"Atari Games", "Gauntlet (Intermediate Release 2)" )
/*TODO*///	public static GameDriver driver_gaunt2p	   = new GameDriver("1985"	,"gaunt2p"	,"gauntlet.java"	,rom_gaunt2p,driver_gauntlet	,machine_driver_gauntlet	,input_ports_gauntlet	,init_gaunt2p	,ROT0	,	"Atari Games", "Gauntlet (2 Players)" )
/*TODO*///	public static GameDriver driver_gaunt2	   = new GameDriver("1986"	,"gaunt2"	,"gauntlet.java"	,rom_gaunt2,null	,machine_driver_gauntlet	,input_ports_gauntlet	,init_gauntlet2	,ROT0	,	"Atari Games", "Gauntlet II" )
/*TODO*///	public static GameDriver driver_vindctr2	   = new GameDriver("1988"	,"vindctr2"	,"gauntlet.java"	,rom_vindctr2,null	,machine_driver_gauntlet	,input_ports_vindctr2	,init_vindctr2	,ROT0	,	"Atari Games", "Vindicators Part II" )
}
