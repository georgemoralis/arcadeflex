/***************************************************************************

  atarigen.c

  General functions for mid-to-late 80's Atari raster games.

***************************************************************************/


/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.machine;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import gr.codebb.arcadeflex.common.SubArrays.IntSubArray;
import gr.codebb.arcadeflex.common.SubArrays.UShortArray;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v037b7.mame.timer.*;
import static gr.codebb.arcadeflex.v037b7.mame.timerH.*;
import static gr.codebb.arcadeflex.v037b7.machine.atarigenH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.platform.fileio.*;
import static gr.codebb.arcadeflex.common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.inputH.*;
import gr.codebb.arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.usrintrf.ui_text;
import static gr.codebb.arcadeflex.v036.sound.mixer.*;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;
import static gr.codebb.arcadeflex.v037b7.cpu.m6502.m6502H.M6502_INT_IRQ;
import static gr.codebb.arcadeflex.v037b7.machine.slapstic.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.*;

public class atarigen
{
	
	
	/*--------------------------------------------------------------------------
	
		Atari generic interrupt model (required)
	
			atarigen_scanline_int_state - state of the scanline interrupt line
			atarigen_sound_int_state - state of the sound interrupt line
			atarigen_video_int_state - state of the video interrupt line
	
			atarigen_int_callback - called when the interrupt state changes
	
			atarigen_interrupt_reset - resets & initializes the interrupt state
			atarigen_update_interrupts - forces the interrupts to be reevaluted
	
			atarigen_scanline_int_set - scanline interrupt initialization
			atarigen_scanline_int_gen - scanline interrupt generator
			atarigen_scanline_int_ack_w - scanline interrupt acknowledgement
	
			atarigen_sound_int_gen - sound interrupt generator
			atarigen_sound_int_ack_w - sound interrupt acknowledgement
	
			atarigen_video_int_gen - video interrupt generator
			atarigen_video_int_ack_w - video interrupt acknowledgement
	
	--------------------------------------------------------------------------*/
	
	/* globals */
	public static int atarigen_scanline_int_state;
	public static int atarigen_sound_int_state;
	public static int atarigen_video_int_state;
	
	/* statics */
	public static atarigen_int_callbackPtr update_int_callback;
	public static timer_entry scanline_interrupt_timer;

/*TODO*///	/* prototypes */
	
	
	/*
	 *	Interrupt initialization
	 *
	 *	Resets the various interrupt states.
	 *
	 */
	
	public static void atarigen_interrupt_reset(atarigen_int_callbackPtr update_int)
	{
		/* set the callback */
		update_int_callback = update_int;
	
		/* reset the interrupt states */
		atarigen_video_int_state = atarigen_sound_int_state = atarigen_scanline_int_state = 0;
		scanline_interrupt_timer = null;
	}
	
	
	/*
	 *	Update interrupts
	 *
	 *	Forces the interrupt callback to be called with the current VBLANK and sound interrupt states.
	 *
	 */
	
	public static void atarigen_update_interrupts()
	{
		(update_int_callback).handler();
	}
	
	
	
	/*
	 *	Scanline interrupt initialization
	 *
	 *	Sets the scanline when the next scanline interrupt should be generated.
	 *
	 */
	
	public static void atarigen_scanline_int_set(int scanline)
	{
		if (scanline_interrupt_timer != null)
			timer_remove(scanline_interrupt_timer);
		scanline_interrupt_timer = timer_set(cpu_getscanlinetime(scanline), 0, scanline_interrupt_callback);
	}
	
	
	/*
	 *	Scanline interrupt generator
	 *
	 *	Standard interrupt routine which sets the scanline interrupt state.
	 *
	 */
	
	public static int atarigen_scanline_int_gen()
	{
		atarigen_scanline_int_state = 1;
		(update_int_callback).handler();
		return 0;
	}
	
	
	/*
	 *	Scanline interrupt acknowledge write handler
	 *
	 *	Resets the state of the scanline interrupt.
	 *
	 */
	
	public static WriteHandlerPtr atarigen_scanline_int_ack_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		atarigen_scanline_int_state = 0;
		(update_int_callback).handler();
	} };
	
	
	/*
	 *	Sound interrupt generator
	 *
	 *	Standard interrupt routine which sets the sound interrupt state.
	 *
	 */
	
	public static int atarigen_sound_int_gen()
	{
		atarigen_sound_int_state = 1;
		(update_int_callback).handler();
		return 0;
	}
	
	
	/*
	 *	Sound interrupt acknowledge write handler
	 *
	 *	Resets the state of the sound interrupt.
	 *
	 */
	
	public static WriteHandlerPtr atarigen_sound_int_ack_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		atarigen_sound_int_state = 0;
		(update_int_callback).handler();
	} };
	
	
	/*
	 *	Video interrupt generator
	 *
	 *	Standard interrupt routine which sets the video interrupt state.
	 *
	 */
	
	public static InterruptPtr atarigen_video_int_gen = new InterruptPtr() {
            @Override
            public int handler() {
		atarigen_video_int_state = 1;
		(update_int_callback).handler();
		return 0;
            }
        };
	
	
	/*
	 *	Video interrupt acknowledge write handler
	 *
	 *	Resets the state of the video interrupt.
	 *
	 */
	
	public static WriteHandlerPtr atarigen_video_int_ack_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		atarigen_video_int_state = 0;
		(update_int_callback).handler();
	} };
	
	
	/*
	 *	Scanline interrupt generator
	 *
	 *	Signals an interrupt.
	 *
	 */
	
	public static timer_callback scanline_interrupt_callback = new timer_callback() { public void handler(int param) 
	{
		/* generate the interrupt */
		atarigen_scanline_int_gen();
	
		/* set a new timer to go off at the same scan line next frame */
		scanline_interrupt_timer = timer_set(TIME_IN_HZ(Machine.drv.frames_per_second), 0, scanline_interrupt_callback);
	} };
	
	
	
	/*--------------------------------------------------------------------------
	
		EEPROM I/O (optional)
	
			atarigen_eeprom_default - pointer to compressed default data
			atarigen_eeprom - pointer to base of EEPROM memory
			atarigen_eeprom_size - size of EEPROM memory
	
			atarigen_eeprom_reset - resets the EEPROM system
	
			atarigen_eeprom_enable_w - write handler to enable EEPROM access
			atarigen_eeprom_w - write handler for EEPROM data (low byte)
			atarigen_eeprom_r - read handler for EEPROM data (low byte)
	
			atarigen_nvram_handler - load/save EEPROM data
	
	--------------------------------------------------------------------------*/
	
	/* globals */
	public static UShortArray atarigen_eeprom_default;
        public static UBytePtr atarigen_eeprom=new UBytePtr();
	public static int[] atarigen_eeprom_size    = new int[1];

	/* statics */
	public static int unlocked;
	
/*TODO*///	/* prototypes */
/*TODO*///	static void decompress_eeprom_word(const UINT16 *data);
/*TODO*///	static void decompress_eeprom_byte(const UINT16 *data);
	
	
	/*
	 *	EEPROM reset
	 *
	 *	Makes sure that the unlocked state is cleared when we reset.
	 *
	 */
	
	public static void atarigen_eeprom_reset()
	{
		unlocked = 0;
	}
	
	
	/*
	 *	EEPROM enable write handler
	 *
	 *	Any write to this handler will allow one byte to be written to the
	 *	EEPROM data area the next time.
	 *
	 */
	
	public static WriteHandlerPtr atarigen_eeprom_enable_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		unlocked = 1;
	} };
	
	
	/*
	 *	EEPROM write handler (low byte of word)
	 *
	 *	Writes a "word" to the EEPROM, which is almost always accessed via
	 *	the low byte of the word only. If the EEPROM hasn't been unlocked,
	 *	the write attempt is ignored.
	 *
	 */
	
	public static WriteHandlerPtr atarigen_eeprom_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (unlocked==0)
			return;
	
		COMBINE_WORD_MEM(atarigen_eeprom, offset, data);
		unlocked = 0;
	} };
	
	
	/*
	 *	EEPROM read handler (low byte of word)
	 *
	 *	Reads a "word" from the EEPROM, which is almost always accessed via
	 *	the low byte of the word only.
	 *
	 */
	
	public static ReadHandlerPtr atarigen_eeprom_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return atarigen_eeprom.READ_WORD(offset) | 0xff00;
	} };
	
	public static ReadHandlerPtr atarigen_eeprom_upper_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return atarigen_eeprom.READ_WORD(offset) | 0x00ff;
	} };
	
	
	/*
	 *	Standard high score load
	 *
	 *	Loads the EEPROM data as a "high score".
	 *
	 */
	
	public static nvramPtr atarigen_nvram_handler  = new nvramPtr() { public void handler(Object file, int read_or_write) 
	{
		if (read_or_write != 0)
			osd_fwrite(file, atarigen_eeprom, atarigen_eeprom_size[0]);
		else
		{
			if (file != null)
				osd_fread(file, atarigen_eeprom, atarigen_eeprom_size[0]);
			else
			{
				/* all 0xff's work for most games */
				memset(atarigen_eeprom, 0xff, atarigen_eeprom_size[0]);
	
				/* anything else must be decompressed */
				if (atarigen_eeprom_default != null)
				{
					if (atarigen_eeprom_default.read(0) == 0)
						decompress_eeprom_byte(new UShortArray(atarigen_eeprom_default, 1));
					else
						decompress_eeprom_word(new UShortArray(atarigen_eeprom_default, 1));
				}
			}
		}
	} };
	
	
	
	/*
	 *	Decompress word-based EEPROM data
	 *
	 *	Used for decompressing EEPROM data that has every other byte invalid.
	 *
	 */
	
	public static void decompress_eeprom_word(UShortArray data)
	{
		UShortPtr dest = new UShortPtr(atarigen_eeprom);
		int value;
	
		while ((value = data.read()) != 0)
		{
                        data.inc(1);
			int count = (value >> 8);
			value = (value << 8) | (value & 0xff);
	
			while (count-- != 0)
			{
				//dest.WRITE_WORD(0, value);
                                dest.write(0, (char) value);
				dest.inc(1);
			}
		}
	}
	
	
	/*
	 *	Decompress byte-based EEPROM data
	 *
	 *	Used for decompressing EEPROM data that is byte-packed.
	 *
	 */
	
	public static void decompress_eeprom_byte(UShortArray data)
	{
		UBytePtr dest = new UBytePtr(atarigen_eeprom);
		int value;
	
		while ((value = data.read()) != 0)
		{
                        data.inc(1);
			int count = (value >> 8);
			value = (value << 8) | (value & 0xff);
	
			while (count-- != 0)
				dest.writeinc( value );
		}
	}
	
	
	
/*TODO*///	/*--------------------------------------------------------------------------
/*TODO*///	
/*TODO*///		Slapstic I/O (optional)
/*TODO*///	
/*TODO*///			atarigen_slapstic - pointer to base of slapstic memory
/*TODO*///	
/*TODO*///			atarigen_slapstic_init - select and initialize the slapstic handlers
/*TODO*///			atarigen_slapstic_reset - resets the slapstic state
/*TODO*///	
/*TODO*///			atarigen_slapstic_w - write handler for slapstic data
/*TODO*///			atarigen_slapstic_r - read handler for slapstic data
/*TODO*///	
/*TODO*///			slapstic_init - low-level init routine
/*TODO*///			slapstic_reset - low-level reset routine
/*TODO*///			slapstic_bank - low-level routine to return the current bank
/*TODO*///			slapstic_tweak - low-level tweak routine
/*TODO*///	
/*TODO*///	--------------------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	/* globals */
	static int atarigen_slapstic_num;
	static UBytePtr atarigen_slapstic;
	
	
	/*
	 *	Slapstic initialization
	 *
	 *	Installs memory handlers for the slapstic and sets the chip number.
	 *
	 */
	
	public static void atarigen_slapstic_init(int cpunum, int base, int chipnum)
	{
		atarigen_slapstic_num = chipnum;
		atarigen_slapstic = null;
		if (chipnum != 0)
		{
			slapstic_init(chipnum);
			atarigen_slapstic = install_mem_read_handler(cpunum, base, base + 0x7fff, atarigen_slapstic_r);
			atarigen_slapstic = install_mem_write_handler(cpunum, base, base + 0x7fff, atarigen_slapstic_w);
		}
	}
	
	
	/*
	 *	Slapstic initialization
	 *
	 *	Makes the selected slapstic number active and resets its state.
	 *
	 */
	
	public static void atarigen_slapstic_reset()
	{
            System.out.println("atarigen_slapstic_reset "+atarigen_slapstic_num);
		if (atarigen_slapstic_num != 0)
			slapstic_reset();
	}
	
	
	/*
	 *	Slapstic write handler
	 *
	 *	Assuming that the slapstic sits in ROM memory space, we just simply
	 *	tweak the slapstic at this address and do nothing more.
	 *
	 */
	
	public static WriteHandlerPtr atarigen_slapstic_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		slapstic_tweak(offset / 2);
	} };
	
	
	/*
	 *	Slapstic read handler
	 *
	 *	Tweaks the slapstic at the appropriate address and then reads a
	 *	word from the underlying memory.
	 *
	 */
	
	public static ReadHandlerPtr atarigen_slapstic_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int bank = slapstic_tweak(offset / 2) * 0x2000;
		return atarigen_slapstic.READ_WORD(bank + (offset & 0x1fff));
	} };
	
	
	
	
	/***********************************************************************************************/
	/***********************************************************************************************/
	/***********************************************************************************************/
	/***********************************************************************************************/
	/***********************************************************************************************/
	
	
	
	/*--------------------------------------------------------------------------
	
		Sound I/O
	
			atarigen_sound_io_reset - reset the sound I/O system
	
			atarigen_6502_irq_gen - standard 6502 IRQ interrupt generator
			atarigen_6502_irq_ack_r - standard 6502 IRQ interrupt acknowledgement
			atarigen_6502_irq_ack_w - standard 6502 IRQ interrupt acknowledgement
	
			atarigen_ym2151_irq_gen - YM2151 sound IRQ generator
	
			atarigen_sound_w - Main CPU . sound CPU data write (low byte)
			atarigen_sound_r - Sound CPU . main CPU data read (low byte)
			atarigen_sound_upper_w - Main CPU . sound CPU data write (high byte)
			atarigen_sound_upper_r - Sound CPU . main CPU data read (high byte)
	
			atarigen_sound_reset_w - 6502 CPU reset
			atarigen_6502_sound_w - Sound CPU . main CPU data write
			atarigen_6502_sound_r - Main CPU . sound CPU data read
	
	--------------------------------------------------------------------------*/
	
	/* constants */
	public static final int SOUND_INTERLEAVE_RATE		= (int) TIME_IN_USEC(50);
        public static final int SOUND_INTERLEAVE_REPEAT		= 20;
	
	/* globals */
	public static int atarigen_cpu_to_sound_ready;
	public static int atarigen_sound_to_cpu_ready;
	
/*TODO*///	/* statics */
	public static int sound_cpu_num;
	public static int atarigen_cpu_to_sound;
	public static int atarigen_sound_to_cpu;
	public static int timed_int;
	public static int ym2151_int;
/*TODO*///	
/*TODO*///	/* prototypes */
/*TODO*///	static 
	
	/*
	 *	Sound I/O reset
	 *
	 *	Resets the state of the sound I/O.
	 *
	 */
	
	public static void atarigen_sound_io_reset(int cpu_num)
	{
		/* remember which CPU is the sound CPU */
		sound_cpu_num = cpu_num;
	
		/* reset the internal interrupts states */
		timed_int = ym2151_int = 0;
	
		/* reset the sound I/O states */
		atarigen_cpu_to_sound = atarigen_sound_to_cpu = 0;
		atarigen_cpu_to_sound_ready = atarigen_sound_to_cpu_ready = 0;
	}
	
	
	/*
	 *	6502 IRQ generator
	 *
	 *	Generates an IRQ signal to the 6502 sound processor.
	 *
	 */
	
	public static InterruptPtr atarigen_6502_irq_gen = new InterruptPtr() { public int handler() 
	{
		timed_int = 1;
		update_6502_irq();
		return 0;
	} };
	
	
	/*
	 *	6502 IRQ acknowledgement
	 *
	 *	Resets the IRQ signal to the 6502 sound processor. Both reads and writes can be used.
	 *
	 */
	
	public static ReadHandlerPtr atarigen_6502_irq_ack_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		timed_int = 0;
		update_6502_irq();
		return 0;
	} };
	
	public static WriteHandlerPtr atarigen_6502_irq_ack_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		timed_int = 0;
		update_6502_irq();
	} };
	
	
	/*
	 *	YM2151 IRQ generation
	 *
	 *	Sets the state of the YM2151's IRQ line.
	 *
	 */
	
	public static WriteYmHandlerPtr atarigen_ym2151_irq_gen = new WriteYmHandlerPtr() {
            @Override
            public void handler(int irq) {
                ym2151_int = irq;
		update_6502_irq();
            }
        };
        
        
	/*
	 *	Sound CPU write handler
	 *
	 *	Write handler which resets the sound CPU in response.
	 *
	 */
	
	public static WriteHandlerPtr atarigen_sound_reset_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		timer_set(TIME_NOW, 0, delayed_sound_reset);
	} };
	
	
	/*
	 *	Sound CPU reset handler
	 *
	 *	Resets the state of the sound CPU manually.
	 *
	 */
	
	public static void atarigen_sound_reset()
	{
		timer_set(TIME_NOW, 1, delayed_sound_reset);
	}
	
	
	/*
	 *	Main . sound CPU data write handlers
	 *
	 *	Handles communication from the main CPU to the sound CPU. Two versions are provided,
	 *	one with the data byte in the low 8 bits, and one with the data byte in the upper 8
	 *	bits.
	 *
	 */
	
	public static WriteHandlerPtr atarigen_sound_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000)==0)
			timer_set(TIME_NOW, data & 0xff, delayed_sound_w);
	} };
	
	public static WriteHandlerPtr atarigen_sound_upper_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0xff000000)==0)
			timer_set(TIME_NOW, (data >> 8) & 0xff, delayed_sound_w);
	} };
	
	
	/*
	 *	Sound . main CPU data read handlers
	 *
	 *	Handles reading data communicated from the sound CPU to the main CPU. Two versions
	 *	are provided, one with the data byte in the low 8 bits, and one with the data byte
	 *	in the upper 8 bits.
	 *
	 */
	
	public static ReadHandlerPtr atarigen_sound_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		atarigen_sound_to_cpu_ready = 0;
		atarigen_sound_int_ack_w.handler(0, 0);
		return atarigen_sound_to_cpu | 0xff00;
	} };
	
	public static ReadHandlerPtr atarigen_sound_upper_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		atarigen_sound_to_cpu_ready = 0;
		atarigen_sound_int_ack_w.handler(0, 0);
		return (atarigen_sound_to_cpu << 8) | 0x00ff;
	} };
	
	
	/*
	 *	Sound . main CPU data write handler
	 *
	 *	Handles communication from the sound CPU to the main CPU.
	 *
	 */
	
	public static WriteHandlerPtr atarigen_6502_sound_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		timer_set(TIME_NOW, data, delayed_6502_sound_w);
	} };
	
	
	/*
	 *	Main . sound CPU data read handler
	 *
	 *	Handles reading data communicated from the main CPU to the sound CPU.
	 *
	 */
	
	public static ReadHandlerPtr atarigen_6502_sound_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		atarigen_cpu_to_sound_ready = 0;
		cpu_set_nmi_line(sound_cpu_num, CLEAR_LINE);
		return atarigen_cpu_to_sound;
	} };
	
	
	/*
	 *	6502 IRQ state updater
	 *
	 *	Called whenever the IRQ state changes. An interrupt is generated if
	 *	either atarigen_6502_irq_gen() was called, or if the YM2151 generated
	 *	an interrupt via the atarigen_ym2151_irq_gen() callback.
	 *
	 */
	
	public static void update_6502_irq()
	{
		if (timed_int!=0 || ym2151_int!=0)
			cpu_set_irq_line(sound_cpu_num, M6502_INT_IRQ, ASSERT_LINE);
		else
			cpu_set_irq_line(sound_cpu_num, M6502_INT_IRQ, CLEAR_LINE);
	}
	
	
	/*
	 *	Sound communications timer
	 *
	 *	Set whenever a command is written from the main CPU to the sound CPU, in order to
	 *	temporarily bump up the interleave rate. This helps ensure that communications
	 *	between the two CPUs works properly.
	 *
	 */
	
	public static timer_callback sound_comm_timer = new timer_callback() {
            @Override
            public void handler(int reps_left) {
                if (--reps_left != 0)
			timer_set(SOUND_INTERLEAVE_RATE, reps_left, sound_comm_timer);
            }
        };
        
	
	/*
	 *	Sound CPU reset timer
	 *
	 *	Synchronizes the sound reset command between the two CPUs.
	 *
	 */
	
	static timer_callback delayed_sound_reset = new timer_callback() {
            @Override
            public void handler(int param) {
                /* unhalt and reset the sound CPU */
		if (param == 0)
		{
			cpu_set_halt_line(sound_cpu_num, CLEAR_LINE);
			cpu_set_reset_line(sound_cpu_num, PULSE_LINE);
		}
	
		/* reset the sound write state */
		atarigen_sound_to_cpu_ready = 0;
		atarigen_sound_int_ack_w.handler(0, 0);
            }
        };
        
	
	/*
	 *	Main . sound data write timer
	 *
	 *	Synchronizes a data write from the main CPU to the sound CPU.
	 *
	 */
	
	public static timer_callback delayed_sound_w = new timer_callback() {
            @Override
            public void handler(int param) {
                /* warn if we missed something */
		if (atarigen_cpu_to_sound_ready != 0)
			logerror("Missed command from 68010\n");
	
		/* set up the states and signal an NMI to the sound CPU */
		atarigen_cpu_to_sound = param;
		atarigen_cpu_to_sound_ready = 1;
		cpu_set_nmi_line(sound_cpu_num, ASSERT_LINE);
	
		/* allocate a high frequency timer until a response is generated */
		/* the main CPU is *very* sensistive to the timing of the response */
		timer_set(SOUND_INTERLEAVE_RATE, SOUND_INTERLEAVE_REPEAT, sound_comm_timer);
            }
        };
        
	
	
	/*
	 *	Sound . main data write timer
	 *
	 *	Synchronizes a data write from the sound CPU to the main CPU.
	 *
	 */
	
	static timer_callback delayed_6502_sound_w = new timer_callback() {
            @Override
            public void handler(int param) {
                /* warn if we missed something */
		if (atarigen_sound_to_cpu_ready != 0)
			logerror("Missed result from 6502\n");
	
		/* set up the states and signal the sound interrupt to the main CPU */
		atarigen_sound_to_cpu = param;
		atarigen_sound_to_cpu_ready = 1;
		atarigen_sound_int_gen();
            }
        };
	
	
/*TODO*///	/*--------------------------------------------------------------------------
/*TODO*///	
/*TODO*///		Misc sound helpers
/*TODO*///	
/*TODO*///			atarigen_init_6502_speedup - installs 6502 speedup cheat handler
/*TODO*///			atarigen_set_ym2151_vol - set the volume of the 2151 chip
/*TODO*///			atarigen_set_ym2413_vol - set the volume of the 2151 chip
/*TODO*///			atarigen_set_pokey_vol - set the volume of the POKEY chip(s)
/*TODO*///			atarigen_set_tms5220_vol - set the volume of the 5220 chip
/*TODO*///			atarigen_set_oki6295_vol - set the volume of the OKI6295
/*TODO*///	
/*TODO*///	--------------------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	/* statics */
	public static UBytePtr speed_a, speed_b;
	public static int speed_pc;
	
	/* prototypes */
	
	
	/*
	 *	6502 CPU speedup cheat installer
	 *
	 *	Installs a special read handler to catch the main spin loop in the
	 *	6502 sound code. The addresses accessed seem to be the same across
	 *	a large number of games, though the PC shifts.
	 *
	 */
	
	public static void atarigen_init_6502_speedup(int cpunum, int compare_pc1, int compare_pc2)
	{
		UBytePtr memory = new UBytePtr(memory_region(REGION_CPU1+cpunum));
		int address_low, address_high;
	
		/* determine the pointer to the first speed check location */
		address_low = memory.read(compare_pc1 + 1) | (memory.read(compare_pc1 + 2) << 8);
		address_high = memory.read(compare_pc1 + 4) | (memory.read(compare_pc1 + 5) << 8);
		if (address_low != address_high - 1)
			logerror("Error: address %04X does not point to a speedup location!", compare_pc1);
		speed_a = new UBytePtr(memory, address_low);
	
		/* determine the pointer to the second speed check location */
		address_low = memory.read(compare_pc2 + 1) | (memory.read(compare_pc2 + 2) << 8);
		address_high = memory.read(compare_pc2 + 4) | (memory.read(compare_pc2 + 5) << 8);
		if (address_low != address_high - 1)
			logerror("Error: address %04X does not point to a speedup location!", compare_pc2);
		speed_b = new UBytePtr(memory, address_low);
	
		/* install a handler on the second address */
		speed_pc = compare_pc2;
		install_mem_read_handler(cpunum, address_low, address_low, m6502_speedup_r);
	}
	
	
	/*
	 *	Set the YM2151 volume
	 *
	 *	What it says.
	 *
	 */
	
	public static void atarigen_set_ym2151_vol(int volume)
	{
		int ch;
	
		for (ch = 0; ch < MIXER_MAX_CHANNELS; ch++)
		{
			String name = mixer_get_name(ch);
/*TODO*///			if (name && strstr(name, "2151"))
				mixer_set_volume(ch, volume);
		}
	}
	
	
	/*
	 *	Set the YM2413 volume
	 *
	 *	What it says.
	 *
	 */
	
	public static void atarigen_set_ym2413_vol(int volume)
	{
		int ch;
	
		for (ch = 0; ch < MIXER_MAX_CHANNELS; ch++)
		{
			String name = mixer_get_name(ch);
/*TODO*///			if (name && strstr(name, "3812"))/*"2413")) -- need this change until 2413 stands alone */
				mixer_set_volume(ch, volume);
		}
	}
	
	
	/*
	 *	Set the POKEY volume
	 *
	 *	What it says.
	 *
	 */
	
	public static void atarigen_set_pokey_vol(int volume)
	{
		int ch;
	
		for (ch = 0; ch < MIXER_MAX_CHANNELS; ch++)
		{
			String name = mixer_get_name(ch);
/*TODO*///			if (name && strstr(name, "POKEY"))
				mixer_set_volume(ch, volume);
		}
	}
	
	
	/*
	 *	Set the TMS5220 volume
	 *
	 *	What it says.
	 *
	 */
	
	public static void atarigen_set_tms5220_vol(int volume)
	{
		int ch;
	
		for (ch = 0; ch < MIXER_MAX_CHANNELS; ch++)
		{
			String name = mixer_get_name(ch);
/*TODO*///			if (name && strstr(name, "5220"))
				mixer_set_volume(ch, volume);
		}
	}
	
	
	/*
	 *	Set the OKI6295 volume
	 *
	 *	What it says.
	 *
	 */
	
	public static void atarigen_set_oki6295_vol(int volume)
	{
		int ch;
	
		for (ch = 0; ch < MIXER_MAX_CHANNELS; ch++)
		{
			String name = mixer_get_name(ch);
/*TODO*///			if (name!=null && strstr(name, "6295"))
				mixer_set_volume(ch, volume);
		}
	}
	
	
	/*
	 *	Generic 6502 CPU speedup handler
	 *
	 *	Special shading renderer that runs any pixels under pen 1 through a lookup table.
	 *
	 */
	
	public static ReadHandlerPtr m6502_speedup_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int result = speed_b.read(0);
	
		if (cpu_getpreviouspc() == speed_pc && speed_a.read(0) == speed_a.read(1) && result == speed_b.read(1))
			cpu_spinuntil_int();
	
		return result;
	} };
	
	
	
	
	/***********************************************************************************************/
	/***********************************************************************************************/
	/***********************************************************************************************/
	/***********************************************************************************************/
	/***********************************************************************************************/
	
	
	
	/* general video globals */
        public static UBytePtr atarigen_playfieldram = new UBytePtr();
        public static UBytePtr atarigen_playfield2ram = new UBytePtr();
        public static UBytePtr atarigen_playfieldram_color = new UBytePtr();
        public static UBytePtr atarigen_playfield2ram_color = new UBytePtr();
	public static UBytePtr atarigen_spriteram = new UBytePtr();
        public static UBytePtr atarigen_alpharam = new UBytePtr();
        public static UBytePtr atarigen_vscroll = new UBytePtr();
        public static UBytePtr atarigen_hscroll = new UBytePtr();

	public static int[] atarigen_playfieldram_size  = new int[1];
	public static int[] atarigen_playfield2ram_size = new int[1];
	public static int[] atarigen_spriteram_size     = new int[1];
	public static int[] atarigen_alpharam_size      = new int[1];
	
	
	
	/*--------------------------------------------------------------------------
	
		Video scanline timing
	
			atarigen_scanline_timer_reset - call to reset the system
	
	--------------------------------------------------------------------------*/
	
	/* statics */
	public static timer_callback scanline_callback;
	public static int scanlines_per_callback;
	public static double scanline_callback_period;
	public static int last_scanline;

	/* prototypes */
	
	/*
	 *	Scanline timer callback
	 *
	 *	Called once every n scanlines to generate the periodic callback to the main system.
	 *
	 */
	
	public static void atarigen_scanline_timer_reset(timer_callback update_graphics, int frequency)
	{
		/* set the scanline callback */
		scanline_callback = update_graphics;
		scanline_callback_period = (double)frequency * cpu_getscanlineperiod();
		scanlines_per_callback = frequency;
	
		/* compute the last scanline */
		last_scanline = (int)(TIME_IN_HZ(Machine.drv.frames_per_second) / cpu_getscanlineperiod());
	
		/* set a timer to go off on the next VBLANK */
		timer_set(cpu_getscanlinetime(Machine.drv.screen_height), 0, vblank_timer);
	}
	
	
	/*
	 *	VBLANK timer callback
	 *
	 *	Called once every VBLANK to prime the scanline timers.
	 *
	 */
	
	public static timer_callback vblank_timer = new timer_callback() {
            @Override
            public void handler(int param) {
                /* set a timer to go off at scanline 0 */
		timer_set(TIME_IN_USEC(Machine.drv.vblank_duration), 0, scanline_timer);
	
		/* set a timer to go off on the next VBLANK */
		timer_set(cpu_getscanlinetime(Machine.drv.screen_height), 1, vblank_timer);
            }
        };
        
	
	/*
	 *	Scanline timer callback
	 *
	 *	Called once every n scanlines to generate the periodic callback to the main system.
	 *
	 */
	
	static timer_callback scanline_timer = new timer_callback() {
            @Override
            public void handler(int scanline) {
		/* if this is scanline 0, we reset the MO and playfield system */
		if (scanline == 0)
		{
			atarigen_mo_reset();
			atarigen_pf_reset();
			atarigen_pf2_reset();
		}
	
		/* callback */
		if (scanline_callback != null)
		{
			(scanline_callback).handler(scanline);
	
			/* generate another? */
			scanline += scanlines_per_callback;
			if (scanline < last_scanline && scanlines_per_callback!=0)
				timer_set(scanline_callback_period, scanline, scanline_timer);
		}
            }
        };
	
	
	/*--------------------------------------------------------------------------
	
		Video Controller I/O: used in Shuuz, Thunderjaws, Relief Pitcher, Off the Wall
	
			atarigen_video_control_data - pointer to base of control memory
			atarigen_video_control_latch1 - latch #1 value (-1 means disabled)
			atarigen_video_control_latch2 - latch #2 value (-1 means disabled)
	
			atarigen_video_control_reset - initializes the video controller
	
			atarigen_video_control_w - write handler for the video controller
			atarigen_video_control_r - read handler for the video controller
	
	--------------------------------------------------------------------------*/
	
	/* globals */
	public static UBytePtr atarigen_video_control_data = new UBytePtr();
	public static atarigen_video_control_state_desc atarigen_video_control_state;
	
	/* statics */
	public static int actual_video_control_latch1;
	public static int actual_video_control_latch2;
	
	
	/*
	 *	Video controller initialization
	 *
	 *	Resets the state of the video controller.
	 *
	 */
	
	public static void atarigen_video_control_reset()
	{
		/* clear the RAM we use */
		memset(atarigen_video_control_data, 0, 0x40);
		//memset(&atarigen_video_control_state, 0, sizeof(atarigen_video_control_state));
                atarigen_video_control_state = new atarigen_video_control_state_desc();
	
		/* reset the latches */
		atarigen_video_control_state.latch1 = atarigen_video_control_state.latch2 = -1;
		actual_video_control_latch1 = actual_video_control_latch2 = -1;
	}
	
	
	/*
	 *	Video controller update
	 *
	 *	Copies the data from the specified location once/frame into the video controller registers
	 *
	 */
	
	public static void atarigen_video_control_update(UBytePtr data)
	{
		int i;
	//System.out.println("atarigen_video_control_update");
		/* echo all the commands to the video controller */
		for (i = 0; i < 0x38; i += 2)
			if (data.READ_WORD(i) != 0)
				atarigen_video_control_w.handler(i, data.READ_WORD(i));
	
		/* use this for debugging the video controller values */
/*TODO*///	#if 0
/*TODO*///		if (keyboard_pressed(KEYCODE_8))
/*TODO*///		{
/*TODO*///			static FILE *out;
/*TODO*///			if (!out) out = fopen("scroll.log", "w");
/*TODO*///			if (out != 0)
/*TODO*///			{
/*TODO*///				for (i = 0; i < 64; i++)
/*TODO*///					fprintf(out, "%04X ", READ_WORD(&data[2 * i]));
/*TODO*///				fprintf(out, "\n");
/*TODO*///			}
/*TODO*///		}
/*TODO*///	#endif
	}
	
	
	/*
	 *	Video controller write
	 *
	 *	Handles an I/O write to the video controller.
	 *
	 */
	
	public static WriteHandlerPtr atarigen_video_control_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = atarigen_video_control_data.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword, data);
		atarigen_video_control_data.WRITE_WORD(offset, newword);
	
		/* switch off the offset */
		switch (offset)
		{
			/* set the scanline interrupt here */
			case 0x06:
				if (oldword != newword)
					atarigen_scanline_int_set(newword & 0x1ff);
				break;
	
			/* latch enable */
			case 0x14:
	
				/* reset the latches when disabled */
				if ((newword & 0x0080)==0) {
					atarigen_video_control_state.latch1 = atarigen_video_control_state.latch2 = -1;
                                } else {
					atarigen_video_control_state.latch1 = actual_video_control_latch1;
					atarigen_video_control_state.latch2 = actual_video_control_latch2;
                                }
	
				/* check for rowscroll enable */
				atarigen_video_control_state.rowscroll_enable = (newword & 0x2000) >> 13;
	
				/* check for palette banking */
				atarigen_video_control_state.palette_bank = ((newword & 0x0400) >> 10) ^ 1;
				break;
	
			/* indexed parameters */
			case 0x20: case 0x22: case 0x24: case 0x26:
			case 0x28: case 0x2a: case 0x2c: case 0x2e:
			case 0x30: case 0x32: case 0x34: case 0x36:
				switch (newword & 15)
				{
					case 9:
						atarigen_video_control_state.sprite_xscroll = (newword >> 7) & 0x1ff;
						break;
	
					case 10:
						atarigen_video_control_state.pf2_xscroll = (newword >> 7) & 0x1ff;
						break;
	
					case 11:
						atarigen_video_control_state.pf1_xscroll = (newword >> 7) & 0x1ff;
						break;
	
					case 13:
						atarigen_video_control_state.sprite_yscroll = (newword >> 7) & 0x1ff;
						break;
	
					case 14:
						atarigen_video_control_state.pf2_yscroll = (newword >> 7) & 0x1ff;
						break;
	
					case 15:
						atarigen_video_control_state.pf1_yscroll = (newword >> 7) & 0x1ff;
						break;
				}
				break;
	
			/* latch 1 value */
			case 0x38:
				actual_video_control_latch1 = newword;
				actual_video_control_latch2 = -1;
				if ((atarigen_video_control_data.READ_WORD(0x14) & 0x80) != 0)
					atarigen_video_control_state.latch1 = actual_video_control_latch1;
				break;
	
			/* latch 2 value */
			case 0x3a:
				actual_video_control_latch1 = -1;
				actual_video_control_latch2 = newword;
				if ((atarigen_video_control_data.READ_WORD(0x14) & 0x80) != 0)
					atarigen_video_control_state.latch2 = actual_video_control_latch2;
				break;
	
			/* scanline IRQ ack here */
			case 0x3c:
				atarigen_scanline_int_ack_w.handler(0, 0);
				break;
	
			/* log anything else */
			case 0x00:
			default:
				if (oldword != newword)
					logerror("video_control_w(%02X, %04X) ** [prev=%04X]\n", offset, newword, oldword);
				break;
		}
	} };
	
	
	/*
	 *	Video controller read
	 *
	 *	Handles an I/O read from the video controller.
	 *
	 */
	
	public static ReadHandlerPtr atarigen_video_control_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		logerror("video_control_r(%02X)\n", offset);
	
		/* a read from offset 0 returns the current scanline */
		/* also sets bit 0x4000 if we're in VBLANK */
		if (offset == 0)
		{
			int result = cpu_getscanline();
	
			if (result > 255)
				result = 255;
			if (result > Machine.visible_area.max_y)
				result |= 0x4000;
	
			return result;
		}
		else
			return atarigen_video_control_data.READ_WORD(offset);
	} };
	
	
	
	/*--------------------------------------------------------------------------
	
		Motion object rendering
	
			atarigen_mo_desc - description of the M.O. layout
	
			atarigen_mo_callback - called back for each M.O. during processing
	
			atarigen_mo_init - initializes and configures the M.O. list walker
			atarigen_mo_free - frees all memory allocated by atarigen_mo_init
			atarigen_mo_reset - reset for a new frame (use only if not using interrupt system)
			atarigen_mo_update - updates the M.O. list for the given scanline
			atarigen_mo_process - processes the current list
	
	--------------------------------------------------------------------------*/

	/* statics */
	public static atarigen_mo_desc modesc;

	public static UShortArray molist;
        public static UShortArray molist_end;
        public static UShortArray molist_last;
	public static UShortArray molist_upper_bound;
	
	
	/*
	 *	Motion object render initialization
	 *
	 *	Allocates memory for the motion object display cache.
	 *
	 */
	
	public static int atarigen_mo_init(atarigen_mo_desc source_desc)
	{
		modesc = new atarigen_mo_desc(source_desc);
		if (modesc.entrywords == 0) modesc.entrywords = 4;
		modesc.entrywords++;
	
		/* make sure everything is free */
		atarigen_mo_free();
	
		/* allocate memory for the cached list */
		molist = new UShortArray(modesc.maxcount * 2 * modesc.entrywords * (Machine.drv.screen_height / 8));
		if (molist==null)
			return 1;
		molist_upper_bound = new UShortArray(molist, (modesc.maxcount * modesc.entrywords * (Machine.drv.screen_height / 8)));
	
		/* initialize the end/last pointers */
		atarigen_mo_reset();
	
		return 0;
	}
	
	
	/*
	 *	Motion object render free
	 *
	 *	Frees all data allocated for the motion objects.
	 *
	 */
	
	public static void atarigen_mo_free()
	{
		if (molist != null)
			molist = null;
	}
	
	
	/*
	 *	Motion object render reset
	 *
	 *	Resets the motion object system for a new frame. Note that this is automatically called
	 *	if you're using the scanline timing system.
	 *
	 */
	
	public static void atarigen_mo_reset()
	{
		molist_end = new UShortArray(molist);
		molist_last = null;
	}
	
	
	/*
	 *	Motion object updater
	 *
	 *	Parses the current motion object list, caching all entries.
	 *
	 */
	
	public static void atarigen_mo_update(UBytePtr base, int link, int scanline)
	{
		int entryskip = modesc.entryskip, wordskip = modesc.wordskip, wordcount = modesc.entrywords - 1;
		IntSubArray spritevisit=new IntSubArray(ATARIGEN_MAX_MAXCOUNT);
                
                //for (int _i=0 ; _i<ATARIGEN_MAX_MAXCOUNT ; _i++)
                //    spritevisit[_i] = 1;
                
		UShortArray data, data_start, prev_data;
		int match = 0;
                
                //System.out.println("offset: "+base.offset);
	
		/* set up local pointers */
		data_start = data = new UShortArray(molist_end);
		prev_data = molist_last;
	
		/* if the last list entries were on the same scanline, overwrite them */
		if (prev_data != null)
		{
                        //prev_data = new UShortPtr(molist_last);
                        
			if (prev_data.read(0) == scanline){
                                data_start = data = new UShortArray(prev_data);
                        } else {
				match = 1;
                        }
		}
	
		/* visit all the sprites and copy their data into the display list */
		memset(spritevisit, 0, modesc.linkmask + 1);
		while (spritevisit.read(link)==0)
		{
			UBytePtr modata = new UBytePtr(base, link * entryskip);
                        //System.out.println("offset2: "+modata.offset);
			int[] tempdata=new int[16];
			int temp, i;
                        
                        //System.out.println("data.offset1: "+data.offset);
                        //System.out.println("molist_upper_bound.offset1: "+molist_upper_bound.offset);
	
			/* bounds checking */
			if (data.offset >= molist_upper_bound.offset)
			{
				logerror("Motion object list exceeded maximum\n");
				break;
			}
	
			/* start with the scanline */
			data.write(0, (char) scanline);
                        data.inc(1);
                        
                        int _max_data_lenght=modata.memory.length -1;
	
			/* add the data words */
			for (i = temp = 0; (i < wordcount) && (modata.offset < _max_data_lenght); i++, temp += wordskip){
				tempdata[i] = modata.READ_WORD(temp);
                                data.write(0, (char) modata.READ_WORD(temp));
                                data.inc(1);
                        }
	
			/* is this one to ignore? (note that ignore is predecremented by 4) */
			if (tempdata[modesc.ignoreword] == 0xffff)
				data.inc( -wordcount + 1 );
	
			/* update our match status */
			else if (match != 0)
			{
				prev_data.inc(1);
				for (i = 0; i < wordcount; i++)
					if (prev_data.read(0) != tempdata[i])
					{
                                                prev_data.inc(1);
						match = 0;
						break;
					}
			}
	
			/* link to the next object */
			spritevisit.write(link, 1);
			if (modesc.linkword >= 0)
				link = (tempdata[modesc.linkword] >> modesc.linkshift) & modesc.linkmask;
			else
				link = (link + 1) & modesc.linkmask;
		}
	
		/* if we didn't match the last set of entries, update the counters */
		if (match==0)
		{
			molist_end = new UShortArray(data);
			molist_last = new UShortArray(data_start);
		}
	}
	
	
	/*
	 *	Motion object updater using SLIPs
	 *
	 *	Updates motion objects using a SLIP read from a table, assuming a 512-pixel high playfield.
	 *
	 */
	
	public static void atarigen_mo_update_slip_512(UBytePtr base, int scroll, int scanline, UBytePtr slips)
	{
                //int _base_index=base.offset;
                //base.offset=0;
		/* catch a fractional character off the top of the screen */
		if (scanline == 0 && (scroll & 7) != 0)
		{
			int pfscanline = scroll & 0x1f8;
			int link = (slips.READ_WORD(2 * (pfscanline / 8)) >> modesc.linkshift) & modesc.linkmask;
			atarigen_mo_update(base, link, 0);
		}
                
                //base.offset=_base_index;
                
                int _maxSlips = slips.memory.length -1;
	
		/* if we're within screen bounds, grab the next batch of MO's and process */
		if ((scanline < Machine.drv.screen_height) && (slips.offset < _maxSlips))
		{
			int pfscanline = (scanline + scroll + 7) & 0x1f8;
			int link = (slips.READ_WORD(2 * (pfscanline / 8)) >> modesc.linkshift) & modesc.linkmask;
			atarigen_mo_update(base, link, (pfscanline - scroll) & 0x1ff);
		}
	}
	
	
	/*
	 *	Motion object processor
	 *
	 *	Processes the cached motion object entries.
	 *
	 */
	
	public static void atarigen_mo_process(atarigen_mo_callback callback, Object param)
	{
            //System.out.println("atarigen_mo_process");
		UShortArray base = new UShortArray(molist);
		int last_start_scan = -1;
		rectangle clip=new rectangle();
	
		/* create a clipping rectangle so that only partial sections are updated at a time */
		clip.min_x = 0;
		clip.max_x = Machine.drv.screen_width - 1;
	
		/* loop over the list until the end */
		while (base.offset < molist_end.offset)
		{
			UShortArray data, first, last;
			int start_scan = base.read(0);
                        int step;
	
			last_start_scan = start_scan;
			clip.min_y = start_scan;
	
			/* look for an entry whose scanline start is different from ours; that's our bottom */
			for (data = new UShortArray(base); data.offset < molist_end.offset; data.inc( modesc.entrywords ))
				if (data.read(0) != start_scan)
				{
					clip.max_y = data.read(0);
					break;
				}
	
			/* if we didn't find any additional regions, go until the bottom of the screen */
			if (data.offset == molist_end.offset)
				clip.max_y = Machine.drv.screen_height - 1;
	
			/* set the start and end points */
			if (modesc.reverse != 0)
			{
				first = new UShortArray(data, - modesc.entrywords);
				last = new UShortArray(base, - modesc.entrywords);
				step = -modesc.entrywords;
			}
			else
			{
				first = new UShortArray(base);
				last = new UShortArray(data);
				step = modesc.entrywords;
			}
	
			/* update the base */
			base = new UShortArray(data);
                        
                        //System.out.println("data.offset: "+data.offset);
                        //System.out.println("last.offset: "+last.offset);
	
			/* render the mos */
			for (data = new UShortArray(first); data.offset != last.offset; data.inc( step ))
				(callback).handler(new UShortArray(data, 1), clip, param);
		}
	}
	
	
/*TODO*///	
/*TODO*///	/*--------------------------------------------------------------------------
/*TODO*///	
/*TODO*///		RLE Motion object rendering/decoding
/*TODO*///	
/*TODO*///			atarigen_rle_init - prescans the RLE objects
/*TODO*///			atarigen_rle_free - frees all memory allocated by atarigen_rle_init
/*TODO*///			atarigen_rle_render - render an RLE-compressed motion object
/*TODO*///	
/*TODO*///	--------------------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	/* globals */
/*TODO*///	int atarigen_rle_count;
/*TODO*///	struct atarigen_rle_descriptor *atarigen_rle_info;
/*TODO*///	
/*TODO*///	/* statics */
/*TODO*///	static UINT8 rle_region;
/*TODO*///	static UINT8 rle_bpp[8];
/*TODO*///	static UINT16 *rle_table[8];
/*TODO*///	static UINT16 *rle_colortable;
/*TODO*///	
/*TODO*///	/* prototypes */
/*TODO*///	static static void draw_rle_zoom(struct osd_bitmap *bitmap, const struct atarigen_rle_descriptor *gfx,
/*TODO*///			UINT32 color, int flipy, int sx, int sy, int scalex, int scaley,
/*TODO*///			const struct rectangle *clip);
/*TODO*///	static void draw_rle_zoom_16(struct osd_bitmap *bitmap, const struct atarigen_rle_descriptor *gfx,
/*TODO*///			UINT32 color, int flipy, int sx, int sy, int scalex, int scaley,
/*TODO*///			const struct rectangle *clip);
/*TODO*///	static void draw_rle_zoom_hflip(struct osd_bitmap *bitmap, const struct atarigen_rle_descriptor *gfx,
/*TODO*///			UINT32 color, int flipy, int sx, int sy, int scalex, int scaley,
/*TODO*///			const struct rectangle *clip);
/*TODO*///	static void draw_rle_zoom_hflip_16(struct osd_bitmap *bitmap, const struct atarigen_rle_descriptor *gfx,
/*TODO*///			UINT32 color, int flipy, int sx, int sy, int scalex, int scaley,
/*TODO*///			const struct rectangle *clip);
/*TODO*///	
/*TODO*///	/*
/*TODO*///	 *	RLE motion object initialization
/*TODO*///	 *
/*TODO*///	 *	Pre-parses the motion object list and potentially pre-decompresses the data.
/*TODO*///	 *
/*TODO*///	 */
/*TODO*///	
/*TODO*///	int atarigen_rle_init(int region, int colorbase)
/*TODO*///	{
/*TODO*///		const UINT16 *base = (const UINT16 *)memory_region(region);
/*TODO*///		int lowest_address = memory_region_length(region);
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		rle_region = region;
/*TODO*///		rle_colortable = &Machine.remapped_colortable[colorbase];
/*TODO*///	
/*TODO*///		/* build and allocate the tables */
/*TODO*///		if (build_rle_tables())
/*TODO*///			return 1;
/*TODO*///	
/*TODO*///		/* first determine the lowest address of all objects */
/*TODO*///		for (i = 0; i < lowest_address; i += 4)
/*TODO*///		{
/*TODO*///			int offset = ((base[i + 2] & 0xff) << 16) | base[i + 3];
/*TODO*///			if (offset > i && offset < lowest_address)
/*TODO*///				lowest_address = offset;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* that determines how many objects */
/*TODO*///		atarigen_rle_count = lowest_address / 4;
/*TODO*///		atarigen_rle_info = malloc(sizeof(struct atarigen_rle_descriptor) * atarigen_rle_count);
/*TODO*///		if (!atarigen_rle_info)
/*TODO*///		{
/*TODO*///			atarigen_rle_free();
/*TODO*///			return 1;
/*TODO*///		}
/*TODO*///		memset(atarigen_rle_info, 0, sizeof(struct atarigen_rle_descriptor) * atarigen_rle_count);
/*TODO*///	
/*TODO*///		/* now loop through and prescan the objects */
/*TODO*///		for (i = 0; i < atarigen_rle_count; i++)
/*TODO*///			prescan_rle(i);
/*TODO*///	
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*
/*TODO*///	 *	RLE motion object free
/*TODO*///	 *
/*TODO*///	 *	Frees all memory allocated to track the motion objects.
/*TODO*///	 *
/*TODO*///	 */
/*TODO*///	
/*TODO*///	void atarigen_rle_free(void)
/*TODO*///	{
/*TODO*///		/* free the info data */
/*TODO*///		if (atarigen_rle_info != 0)
/*TODO*///			free(atarigen_rle_info);
/*TODO*///		atarigen_rle_info = NULL;
/*TODO*///	
/*TODO*///		/* free the tables */
/*TODO*///		if (rle_table[0])
/*TODO*///			free(rle_table[0]);
/*TODO*///		memset(rle_table, 0, sizeof(rle_table));
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*
/*TODO*///	 *	RLE motion object render
/*TODO*///	 *
/*TODO*///	 *	Renders a compressed motion object.
/*TODO*///	 *
/*TODO*///	 */
/*TODO*///	
/*TODO*///	void atarigen_rle_render(struct osd_bitmap *bitmap, struct atarigen_rle_descriptor *info, int color, int hflip, int vflip,
/*TODO*///		int x, int y, int xscale, int yscale, const struct rectangle *clip)
/*TODO*///	{
/*TODO*///		int scaled_xoffs = (xscale * info.xoffs) >> 12;
/*TODO*///		int scaled_yoffs = (yscale * info.yoffs) >> 12;
/*TODO*///	
/*TODO*///		/* we're hflipped, account for it */
/*TODO*///		if (hflip != 0) scaled_xoffs = ((xscale * info.width) >> 12) - scaled_xoffs;
/*TODO*///	
/*TODO*///		/* adjust for the x and y offsets */
/*TODO*///		x -= scaled_xoffs;
/*TODO*///		y -= scaled_yoffs;
/*TODO*///	
/*TODO*///		/* bail on a NULL object */
/*TODO*///		if (!info.data)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* 16-bit case */
/*TODO*///		if (bitmap.depth == 16)
/*TODO*///		{
/*TODO*///			if (!hflip)
/*TODO*///				draw_rle_zoom_16(bitmap, info, color, vflip, x, y, xscale << 4, yscale << 4, clip);
/*TODO*///			else
/*TODO*///				draw_rle_zoom_hflip_16(bitmap, info, color, vflip, x, y, xscale << 4, yscale << 4, clip);
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* 8-bit case */
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if (!hflip)
/*TODO*///				draw_rle_zoom(bitmap, info, color, vflip, x, y, xscale << 4, yscale << 4, clip);
/*TODO*///			else
/*TODO*///				draw_rle_zoom_hflip(bitmap, info, color, vflip, x, y, xscale << 4, yscale << 4, clip);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*
/*TODO*///	 *	Builds internally-used tables
/*TODO*///	 *
/*TODO*///	 *	Special two-byte tables with the upper byte giving the count and the lower
/*TODO*///	 *	byte giving the pixel value.
/*TODO*///	 *
/*TODO*///	 */
/*TODO*///	
/*TODO*///	static int build_rle_tables(void)
/*TODO*///	{
/*TODO*///		UINT16 *base;
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		/* allocate all 5 tables */
/*TODO*///		base = malloc(0x500 * sizeof(UINT16));
/*TODO*///		if (!base)
/*TODO*///			return 1;
/*TODO*///	
/*TODO*///		/* assign the tables */
/*TODO*///		rle_table[0] = &base[0x000];
/*TODO*///		rle_table[1] = &base[0x100];
/*TODO*///		rle_table[2] = rle_table[3] = &base[0x200];
/*TODO*///		rle_table[4] = rle_table[6] = &base[0x300];
/*TODO*///		rle_table[5] = rle_table[7] = &base[0x400];
/*TODO*///	
/*TODO*///		/* set the bpps */
/*TODO*///		rle_bpp[0] = 4;
/*TODO*///		rle_bpp[1] = rle_bpp[2] = rle_bpp[3] = 5;
/*TODO*///		rle_bpp[4] = rle_bpp[5] = rle_bpp[6] = rle_bpp[7] = 6;
/*TODO*///	
/*TODO*///		/* build the 4bpp table */
/*TODO*///		for (i = 0; i < 256; i++)
/*TODO*///			rle_table[0][i] = (((i & 0xf0) + 0x10) << 4) | (i & 0x0f);
/*TODO*///	
/*TODO*///		/* build the 5bpp table */
/*TODO*///		for (i = 0; i < 256; i++)
/*TODO*///			rle_table[2][i] = (((i & 0xe0) + 0x20) << 3) | (i & 0x1f);
/*TODO*///	
/*TODO*///		/* build the special 5bpp table */
/*TODO*///		for (i = 0; i < 256; i++)
/*TODO*///		{
/*TODO*///			if ((i & 0x0f) == 0)
/*TODO*///				rle_table[1][i] = (((i & 0xf0) + 0x10) << 4) | (i & 0x0f);
/*TODO*///			else
/*TODO*///				rle_table[1][i] = (((i & 0xe0) + 0x20) << 3) | (i & 0x1f);
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* build the 6bpp table */
/*TODO*///		for (i = 0; i < 256; i++)
/*TODO*///			rle_table[5][i] = (((i & 0xc0) + 0x40) << 2) | (i & 0x3f);
/*TODO*///	
/*TODO*///		/* build the special 6bpp table */
/*TODO*///		for (i = 0; i < 256; i++)
/*TODO*///		{
/*TODO*///			if ((i & 0x0f) == 0)
/*TODO*///				rle_table[4][i] = (((i & 0xf0) + 0x10) << 4) | (i & 0x0f);
/*TODO*///			else
/*TODO*///				rle_table[4][i] = (((i & 0xc0) + 0x40) << 2) | (i & 0x3f);
/*TODO*///		}
/*TODO*///	
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*
/*TODO*///	 *	Prescans an RLE-compressed object
/*TODO*///	 *
/*TODO*///	 *	Determines the pen usage, width, height, and other data for an RLE object.
/*TODO*///	 *
/*TODO*///	 */
/*TODO*///	
/*TODO*///	static void prescan_rle(int which)
/*TODO*///	{
/*TODO*///		UINT16 *base = (UINT16 *)&memory_region(rle_region)[which * 8];
/*TODO*///		struct atarigen_rle_descriptor *rle_data = &atarigen_rle_info[which];
/*TODO*///		UINT32 usage = 0, usage_hi = 0;
/*TODO*///		int width = 0, height, flags, offset;
/*TODO*///		const UINT16 *table;
/*TODO*///	
/*TODO*///		/* look up the offset */
/*TODO*///		rle_data.xoffs = (INT16)base[0];
/*TODO*///		rle_data.yoffs = (INT16)base[1];
/*TODO*///	
/*TODO*///		/* determine the depth and table */
/*TODO*///		flags = base[2];
/*TODO*///		rle_data.bpp = rle_bpp[(flags >> 8) & 7];
/*TODO*///		table = rle_data.table = rle_table[(flags >> 8) & 7];
/*TODO*///	
/*TODO*///		/* determine the starting offset */
/*TODO*///		offset = ((base[2] & 0xff) << 16) | base[3];
/*TODO*///		rle_data.data = base = (UINT16 *)&memory_region(rle_region)[offset * 2];
/*TODO*///	
/*TODO*///		/* make sure it's valid */
/*TODO*///		if (offset < which * 4 || offset > memory_region_length(rle_region))
/*TODO*///		{
/*TODO*///			memset(rle_data, 0, sizeof(*rle_data));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* first pre-scan to determine the width and height */
/*TODO*///		for (height = 0; height < 1024; height++)
/*TODO*///		{
/*TODO*///			int tempwidth = 0;
/*TODO*///			int entry_count = *base++;
/*TODO*///	
/*TODO*///			/* if the high bit is set, assume we're inverted */
/*TODO*///			if ((entry_count & 0x8000) != 0)
/*TODO*///			{
/*TODO*///				entry_count ^= 0xffff;
/*TODO*///	
/*TODO*///				/* also change the ROM data so we don't have to do this again at runtime */
/*TODO*///				base[-1] ^= 0xffff;
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* we're done when we hit 0 */
/*TODO*///			if (entry_count == 0)
/*TODO*///				break;
/*TODO*///	
/*TODO*///			/* track the width */
/*TODO*///			while (entry_count--)
/*TODO*///			{
/*TODO*///				int word = *base++;
/*TODO*///				int count, value;
/*TODO*///	
/*TODO*///				/* decode the low byte first */
/*TODO*///				count = table[word & 0xff];
/*TODO*///				value = count & 0xff;
/*TODO*///				tempwidth += count >> 8;
/*TODO*///				if (value < 32)
/*TODO*///					usage |= 1 << value;
/*TODO*///				else
/*TODO*///					usage_hi |= 1 << (value - 32);
/*TODO*///	
/*TODO*///				/* decode the upper byte second */
/*TODO*///				count = table[word >> 8];
/*TODO*///				value = count & 0xff;
/*TODO*///				tempwidth += count >> 8;
/*TODO*///				if (value < 32)
/*TODO*///					usage |= 1 << value;
/*TODO*///				else
/*TODO*///					usage_hi |= 1 << (value - 32);
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* only remember the max */
/*TODO*///			if (tempwidth > width) width = tempwidth;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* fill in the data */
/*TODO*///		rle_data.width = width;
/*TODO*///		rle_data.height = height;
/*TODO*///		rle_data.pen_usage = usage;
/*TODO*///		rle_data.pen_usage_hi = usage_hi;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*
/*TODO*///	 *	Draw a compressed RLE object
/*TODO*///	 *
/*TODO*///	 *	What it says. RLE decoding is performed on the fly to an 8-bit bitmap.
/*TODO*///	 *
/*TODO*///	 */
/*TODO*///	
/*TODO*///	void draw_rle_zoom(struct osd_bitmap *bitmap, const struct atarigen_rle_descriptor *gfx,
/*TODO*///			UINT32 color, int flipy, int sx, int sy, int scalex, int scaley,
/*TODO*///			const struct rectangle *clip)
/*TODO*///	{
/*TODO*///		const UINT16 *palette = &rle_colortable[color];
/*TODO*///		const UINT16 *row_start = gfx.data;
/*TODO*///		const UINT16 *table = gfx.table;
/*TODO*///		volatile int current_row = 0;
/*TODO*///	
/*TODO*///		int scaled_width = (scalex * gfx.width + 0x7fff) >> 16;
/*TODO*///		int scaled_height = (scaley * gfx.height + 0x7fff) >> 16;
/*TODO*///	
/*TODO*///		int pixels_to_skip = 0, xclipped = 0;
/*TODO*///		int dx, dy, ex, ey;
/*TODO*///		int y, sourcey;
/*TODO*///	
/*TODO*///		/* make sure we didn't end up with 0 */
/*TODO*///		if (scaled_width == 0) scaled_width = 1;
/*TODO*///		if (scaled_height == 0) scaled_height = 1;
/*TODO*///	
/*TODO*///		/* compute the remaining parameters */
/*TODO*///		dx = (gfx.width << 16) / scaled_width;
/*TODO*///		dy = (gfx.height << 16) / scaled_height;
/*TODO*///		ex = sx + scaled_width - 1;
/*TODO*///		ey = sy + scaled_height - 1;
/*TODO*///		sourcey = dy / 2;
/*TODO*///	
/*TODO*///		/* left edge clip */
/*TODO*///		if (sx < clip.min_x)
/*TODO*///			pixels_to_skip = clip.min_x - sx, xclipped = 1;
/*TODO*///		if (sx > clip.max_x)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* right edge clip */
/*TODO*///		if (ex > clip.max_x)
/*TODO*///			ex = clip.max_x, xclipped = 1;
/*TODO*///		else if (ex < clip.min_x)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* top edge clip */
/*TODO*///		if (sy < clip.min_y)
/*TODO*///		{
/*TODO*///			sourcey += (clip.min_y - sy) * dy;
/*TODO*///			sy = clip.min_y;
/*TODO*///		}
/*TODO*///		else if (sy > clip.max_y)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* bottom edge clip */
/*TODO*///		if (ey > clip.max_y)
/*TODO*///			ey = clip.max_y;
/*TODO*///		else if (ey < clip.min_y)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* loop top to bottom */
/*TODO*///		for (y = sy; y <= ey; y++, sourcey += dy)
/*TODO*///		{
/*TODO*///			UINT8 *dest = &bitmap.line[y][sx];
/*TODO*///			int j, sourcex = dx / 2, rle_end = 0;
/*TODO*///			const UINT16 *base;
/*TODO*///			int entry_count;
/*TODO*///	
/*TODO*///			/* loop until we hit the row we're on */
/*TODO*///			for ( ; current_row != (sourcey >> 16); current_row++)
/*TODO*///				row_start += 1 + *row_start;
/*TODO*///	
/*TODO*///			/* grab our starting parameters from this row */
/*TODO*///			base = row_start;
/*TODO*///			entry_count = *base++;
/*TODO*///	
/*TODO*///			/* non-clipped case */
/*TODO*///			if (!xclipped)
/*TODO*///			{
/*TODO*///				/* decode the pixels */
/*TODO*///				for (j = 0; j < entry_count; j++)
/*TODO*///				{
/*TODO*///					int word = *base++;
/*TODO*///					int count, value;
/*TODO*///	
/*TODO*///					/* decode the low byte first */
/*TODO*///					count = table[word & 0xff];
/*TODO*///					value = count & 0xff;
/*TODO*///					rle_end += (count & 0xff00) << 8;
/*TODO*///	
/*TODO*///					/* store copies of the value until we pass the end of this chunk */
/*TODO*///					if (value != 0)
/*TODO*///					{
/*TODO*///						value = palette[value];
/*TODO*///						while (sourcex < rle_end)
/*TODO*///							*dest++ = value, sourcex += dx;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						while (sourcex < rle_end)
/*TODO*///							dest++, sourcex += dx;
/*TODO*///					}
/*TODO*///	
/*TODO*///					/* decode the upper byte second */
/*TODO*///					count = table[word >> 8];
/*TODO*///					value = count & 0xff;
/*TODO*///					rle_end += (count & 0xff00) << 8;
/*TODO*///	
/*TODO*///					/* store copies of the value until we pass the end of this chunk */
/*TODO*///					if (value != 0)
/*TODO*///					{
/*TODO*///						value = palette[value];
/*TODO*///						while (sourcex < rle_end)
/*TODO*///							*dest++ = value, sourcex += dx;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						while (sourcex < rle_end)
/*TODO*///							dest++, sourcex += dx;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* clipped case */
/*TODO*///			else
/*TODO*///			{
/*TODO*///				const UINT8 *end = &bitmap.line[y][ex];
/*TODO*///				int to_be_skipped = pixels_to_skip;
/*TODO*///	
/*TODO*///				/* decode the pixels */
/*TODO*///				for (j = 0; j < entry_count && dest <= end; j++)
/*TODO*///				{
/*TODO*///					int word = *base++;
/*TODO*///					int count, value;
/*TODO*///	
/*TODO*///					/* decode the low byte first */
/*TODO*///					count = table[word & 0xff];
/*TODO*///					value = count & 0xff;
/*TODO*///					rle_end += (count & 0xff00) << 8;
/*TODO*///	
/*TODO*///					/* store copies of the value until we pass the end of this chunk */
/*TODO*///					if (to_be_skipped != 0)
/*TODO*///					{
/*TODO*///						while (to_be_skipped && sourcex < rle_end)
/*TODO*///							dest++, sourcex += dx, to_be_skipped--;
/*TODO*///						if (to_be_skipped != 0) goto next1;
/*TODO*///					}
/*TODO*///					if (value != 0)
/*TODO*///					{
/*TODO*///						value = palette[value];
/*TODO*///						while (sourcex < rle_end && dest <= end)
/*TODO*///							*dest++ = value, sourcex += dx;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						while (sourcex < rle_end)
/*TODO*///							dest++, sourcex += dx;
/*TODO*///					}
/*TODO*///	
/*TODO*///				next1:
/*TODO*///					/* decode the upper byte second */
/*TODO*///					count = table[word >> 8];
/*TODO*///					value = count & 0xff;
/*TODO*///					rle_end += (count & 0xff00) << 8;
/*TODO*///	
/*TODO*///					/* store copies of the value until we pass the end of this chunk */
/*TODO*///					if (to_be_skipped != 0)
/*TODO*///					{
/*TODO*///						while (to_be_skipped && sourcex < rle_end)
/*TODO*///							dest++, sourcex += dx, to_be_skipped--;
/*TODO*///						if (to_be_skipped != 0) goto next2;
/*TODO*///					}
/*TODO*///					if (value != 0)
/*TODO*///					{
/*TODO*///						value = palette[value];
/*TODO*///						while (sourcex < rle_end && dest <= end)
/*TODO*///							*dest++ = value, sourcex += dx;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						while (sourcex < rle_end)
/*TODO*///							dest++, sourcex += dx;
/*TODO*///					}
/*TODO*///				next2:
/*TODO*///					;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*
/*TODO*///	 *	Draw a compressed RLE object
/*TODO*///	 *
/*TODO*///	 *	What it says. RLE decoding is performed on the fly to a 16-bit bitmap.
/*TODO*///	 *
/*TODO*///	 */
/*TODO*///	
/*TODO*///	void draw_rle_zoom_16(struct osd_bitmap *bitmap, const struct atarigen_rle_descriptor *gfx,
/*TODO*///			UINT32 color, int flipy, int sx, int sy, int scalex, int scaley,
/*TODO*///			const struct rectangle *clip)
/*TODO*///	{
/*TODO*///		const UINT16 *palette = &rle_colortable[color];
/*TODO*///		const UINT16 *row_start = gfx.data;
/*TODO*///		const UINT16 *table = gfx.table;
/*TODO*///		volatile int current_row = 0;
/*TODO*///	
/*TODO*///		int scaled_width = (scalex * gfx.width + 0x7fff) >> 16;
/*TODO*///		int scaled_height = (scaley * gfx.height + 0x7fff) >> 16;
/*TODO*///	
/*TODO*///		int pixels_to_skip = 0, xclipped = 0;
/*TODO*///		int dx, dy, ex, ey;
/*TODO*///		int y, sourcey;
/*TODO*///	
/*TODO*///		/* make sure we didn't end up with 0 */
/*TODO*///		if (scaled_width == 0) scaled_width = 1;
/*TODO*///		if (scaled_height == 0) scaled_height = 1;
/*TODO*///	
/*TODO*///		/* compute the remaining parameters */
/*TODO*///		dx = (gfx.width << 16) / scaled_width;
/*TODO*///		dy = (gfx.height << 16) / scaled_height;
/*TODO*///		ex = sx + scaled_width - 1;
/*TODO*///		ey = sy + scaled_height - 1;
/*TODO*///		sourcey = dy / 2;
/*TODO*///	
/*TODO*///		/* left edge clip */
/*TODO*///		if (sx < clip.min_x)
/*TODO*///			pixels_to_skip = clip.min_x - sx, xclipped = 1;
/*TODO*///		if (sx > clip.max_x)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* right edge clip */
/*TODO*///		if (ex > clip.max_x)
/*TODO*///			ex = clip.max_x, xclipped = 1;
/*TODO*///		else if (ex < clip.min_x)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* top edge clip */
/*TODO*///		if (sy < clip.min_y)
/*TODO*///		{
/*TODO*///			sourcey += (clip.min_y - sy) * dy;
/*TODO*///			sy = clip.min_y;
/*TODO*///		}
/*TODO*///		else if (sy > clip.max_y)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* bottom edge clip */
/*TODO*///		if (ey > clip.max_y)
/*TODO*///			ey = clip.max_y;
/*TODO*///		else if (ey < clip.min_y)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* loop top to bottom */
/*TODO*///		for (y = sy; y <= ey; y++, sourcey += dy)
/*TODO*///		{
/*TODO*///			UINT16 *dest = (UINT16 *)&bitmap.line[y][sx * 2];
/*TODO*///			int j, sourcex = dx / 2, rle_end = 0;
/*TODO*///			const UINT16 *base;
/*TODO*///			int entry_count;
/*TODO*///	
/*TODO*///			/* loop until we hit the row we're on */
/*TODO*///			for ( ; current_row != (sourcey >> 16); current_row++)
/*TODO*///				row_start += 1 + *row_start;
/*TODO*///	
/*TODO*///			/* grab our starting parameters from this row */
/*TODO*///			base = row_start;
/*TODO*///			entry_count = *base++;
/*TODO*///	
/*TODO*///			/* non-clipped case */
/*TODO*///			if (!xclipped)
/*TODO*///			{
/*TODO*///				/* decode the pixels */
/*TODO*///				for (j = 0; j < entry_count; j++)
/*TODO*///				{
/*TODO*///					int word = *base++;
/*TODO*///					int count, value;
/*TODO*///	
/*TODO*///					/* decode the low byte first */
/*TODO*///					count = table[word & 0xff];
/*TODO*///					value = count & 0xff;
/*TODO*///					rle_end += (count & 0xff00) << 8;
/*TODO*///	
/*TODO*///					/* store copies of the value until we pass the end of this chunk */
/*TODO*///					if (value != 0)
/*TODO*///					{
/*TODO*///						value = palette[value];
/*TODO*///						while (sourcex < rle_end)
/*TODO*///							*dest++ = value, sourcex += dx;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						while (sourcex < rle_end)
/*TODO*///							dest++, sourcex += dx;
/*TODO*///					}
/*TODO*///	
/*TODO*///					/* decode the upper byte second */
/*TODO*///					count = table[word >> 8];
/*TODO*///					value = count & 0xff;
/*TODO*///					rle_end += (count & 0xff00) << 8;
/*TODO*///	
/*TODO*///					/* store copies of the value until we pass the end of this chunk */
/*TODO*///					if (value != 0)
/*TODO*///					{
/*TODO*///						value = palette[value];
/*TODO*///						while (sourcex < rle_end)
/*TODO*///							*dest++ = value, sourcex += dx;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						while (sourcex < rle_end)
/*TODO*///							dest++, sourcex += dx;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* clipped case */
/*TODO*///			else
/*TODO*///			{
/*TODO*///				const UINT16 *end = (const UINT16 *)&bitmap.line[y][ex * 2];
/*TODO*///				int to_be_skipped = pixels_to_skip;
/*TODO*///	
/*TODO*///				/* decode the pixels */
/*TODO*///				for (j = 0; j < entry_count && dest <= end; j++)
/*TODO*///				{
/*TODO*///					int word = *base++;
/*TODO*///					int count, value;
/*TODO*///	
/*TODO*///					/* decode the low byte first */
/*TODO*///					count = table[word & 0xff];
/*TODO*///					value = count & 0xff;
/*TODO*///					rle_end += (count & 0xff00) << 8;
/*TODO*///	
/*TODO*///					/* store copies of the value until we pass the end of this chunk */
/*TODO*///					if (to_be_skipped != 0)
/*TODO*///					{
/*TODO*///						while (to_be_skipped && sourcex < rle_end)
/*TODO*///							dest++, sourcex += dx, to_be_skipped--;
/*TODO*///						if (to_be_skipped != 0) goto next3;
/*TODO*///					}
/*TODO*///					if (value != 0)
/*TODO*///					{
/*TODO*///						value = palette[value];
/*TODO*///						while (sourcex < rle_end && dest <= end)
/*TODO*///							*dest++ = value, sourcex += dx;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						while (sourcex < rle_end)
/*TODO*///							dest++, sourcex += dx;
/*TODO*///					}
/*TODO*///	
/*TODO*///				next3:
/*TODO*///					/* decode the upper byte second */
/*TODO*///					count = table[word >> 8];
/*TODO*///					value = count & 0xff;
/*TODO*///					rle_end += (count & 0xff00) << 8;
/*TODO*///	
/*TODO*///					/* store copies of the value until we pass the end of this chunk */
/*TODO*///					if (to_be_skipped != 0)
/*TODO*///					{
/*TODO*///						while (to_be_skipped && sourcex < rle_end)
/*TODO*///							dest++, sourcex += dx, to_be_skipped--;
/*TODO*///						if (to_be_skipped != 0) goto next4;
/*TODO*///					}
/*TODO*///					if (value != 0)
/*TODO*///					{
/*TODO*///						value = palette[value];
/*TODO*///						while (sourcex < rle_end && dest <= end)
/*TODO*///							*dest++ = value, sourcex += dx;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						while (sourcex < rle_end)
/*TODO*///							dest++, sourcex += dx;
/*TODO*///					}
/*TODO*///				next4:
/*TODO*///					;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*
/*TODO*///	 *	Draw a horizontally-flipped RLE-compressed object
/*TODO*///	 *
/*TODO*///	 *	What it says. RLE decoding is performed on the fly to an 8-bit bitmap.
/*TODO*///	 *
/*TODO*///	 */
/*TODO*///	
/*TODO*///	void draw_rle_zoom_hflip(struct osd_bitmap *bitmap, const struct atarigen_rle_descriptor *gfx,
/*TODO*///			UINT32 color, int flipy, int sx, int sy, int scalex, int scaley,
/*TODO*///			const struct rectangle *clip)
/*TODO*///	{
/*TODO*///		const UINT16 *palette = &rle_colortable[color];
/*TODO*///		const UINT16 *row_start = gfx.data;
/*TODO*///		const UINT16 *table = gfx.table;
/*TODO*///		volatile int current_row = 0;
/*TODO*///	
/*TODO*///		int scaled_width = (scalex * gfx.width + 0x7fff) >> 16;
/*TODO*///		int scaled_height = (scaley * gfx.height + 0x7fff) >> 16;
/*TODO*///		int pixels_to_skip = 0, xclipped = 0;
/*TODO*///		int dx, dy, ex, ey;
/*TODO*///		int y, sourcey;
/*TODO*///	
/*TODO*///		/* make sure we didn't end up with 0 */
/*TODO*///		if (scaled_width == 0) scaled_width = 1;
/*TODO*///		if (scaled_height == 0) scaled_height = 1;
/*TODO*///	
/*TODO*///		/* compute the remaining parameters */
/*TODO*///		dx = (gfx.width << 16) / scaled_width;
/*TODO*///		dy = (gfx.height << 16) / scaled_height;
/*TODO*///		ex = sx + scaled_width - 1;
/*TODO*///		ey = sy + scaled_height - 1;
/*TODO*///		sourcey = dy / 2;
/*TODO*///	
/*TODO*///		/* left edge clip */
/*TODO*///		if (sx < clip.min_x)
/*TODO*///			sx = clip.min_x, xclipped = 1;
/*TODO*///		if (sx > clip.max_x)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* right edge clip */
/*TODO*///		if (ex > clip.max_x)
/*TODO*///			pixels_to_skip = ex - clip.max_x, xclipped = 1;
/*TODO*///		else if (ex < clip.min_x)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* top edge clip */
/*TODO*///		if (sy < clip.min_y)
/*TODO*///		{
/*TODO*///			sourcey += (clip.min_y - sy) * dy;
/*TODO*///			sy = clip.min_y;
/*TODO*///		}
/*TODO*///		else if (sy > clip.max_y)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* bottom edge clip */
/*TODO*///		if (ey > clip.max_y)
/*TODO*///			ey = clip.max_y;
/*TODO*///		else if (ey < clip.min_y)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* loop top to bottom */
/*TODO*///		for (y = sy; y <= ey; y++, sourcey += dy)
/*TODO*///		{
/*TODO*///			UINT8 *dest = &bitmap.line[y][ex];
/*TODO*///			int j, sourcex = dx / 2, rle_end = 0;
/*TODO*///			const UINT16 *base;
/*TODO*///			int entry_count;
/*TODO*///	
/*TODO*///			/* loop until we hit the row we're on */
/*TODO*///			for ( ; current_row != (sourcey >> 16); current_row++)
/*TODO*///				row_start += 1 + *row_start;
/*TODO*///	
/*TODO*///			/* grab our starting parameters from this row */
/*TODO*///			base = row_start;
/*TODO*///			entry_count = *base++;
/*TODO*///	
/*TODO*///			/* non-clipped case */
/*TODO*///			if (!xclipped)
/*TODO*///			{
/*TODO*///				/* decode the pixels */
/*TODO*///				for (j = 0; j < entry_count; j++)
/*TODO*///				{
/*TODO*///					int word = *base++;
/*TODO*///					int count, value;
/*TODO*///	
/*TODO*///					/* decode the low byte first */
/*TODO*///					count = table[word & 0xff];
/*TODO*///					value = count & 0xff;
/*TODO*///					rle_end += (count & 0xff00) << 8;
/*TODO*///	
/*TODO*///					/* store copies of the value until we pass the end of this chunk */
/*TODO*///					if (value != 0)
/*TODO*///					{
/*TODO*///						value = palette[value];
/*TODO*///						while (sourcex < rle_end)
/*TODO*///							*dest-- = value, sourcex += dx;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						while (sourcex < rle_end)
/*TODO*///							dest--, sourcex += dx;
/*TODO*///					}
/*TODO*///	
/*TODO*///					/* decode the upper byte second */
/*TODO*///					count = table[word >> 8];
/*TODO*///					value = count & 0xff;
/*TODO*///					rle_end += (count & 0xff00) << 8;
/*TODO*///	
/*TODO*///					/* store copies of the value until we pass the end of this chunk */
/*TODO*///					if (value != 0)
/*TODO*///					{
/*TODO*///						value = palette[value];
/*TODO*///						while (sourcex < rle_end)
/*TODO*///							*dest-- = value, sourcex += dx;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						while (sourcex < rle_end)
/*TODO*///							dest--, sourcex += dx;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* clipped case */
/*TODO*///			else
/*TODO*///			{
/*TODO*///				const UINT8 *start = &bitmap.line[y][sx];
/*TODO*///				int to_be_skipped = pixels_to_skip;
/*TODO*///	
/*TODO*///				/* decode the pixels */
/*TODO*///				for (j = 0; j < entry_count && dest >= start; j++)
/*TODO*///				{
/*TODO*///					int word = *base++;
/*TODO*///					int count, value;
/*TODO*///	
/*TODO*///					/* decode the low byte first */
/*TODO*///					count = table[word & 0xff];
/*TODO*///					value = count & 0xff;
/*TODO*///					rle_end += (count & 0xff00) << 8;
/*TODO*///	
/*TODO*///					/* store copies of the value until we pass the end of this chunk */
/*TODO*///					if (to_be_skipped != 0)
/*TODO*///					{
/*TODO*///						while (to_be_skipped && sourcex < rle_end)
/*TODO*///							dest--, sourcex += dx, to_be_skipped--;
/*TODO*///						if (to_be_skipped != 0) goto next1;
/*TODO*///					}
/*TODO*///					if (value != 0)
/*TODO*///					{
/*TODO*///						value = palette[value];
/*TODO*///						while (sourcex < rle_end && dest >= start)
/*TODO*///							*dest-- = value, sourcex += dx;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						while (sourcex < rle_end)
/*TODO*///							dest--, sourcex += dx;
/*TODO*///					}
/*TODO*///	
/*TODO*///				next1:
/*TODO*///					/* decode the upper byte second */
/*TODO*///					count = table[word >> 8];
/*TODO*///					value = count & 0xff;
/*TODO*///					rle_end += (count & 0xff00) << 8;
/*TODO*///	
/*TODO*///					/* store copies of the value until we pass the end of this chunk */
/*TODO*///					if (to_be_skipped != 0)
/*TODO*///					{
/*TODO*///						while (to_be_skipped && sourcex < rle_end)
/*TODO*///							dest--, sourcex += dx, to_be_skipped--;
/*TODO*///						if (to_be_skipped != 0) goto next2;
/*TODO*///					}
/*TODO*///					if (value != 0)
/*TODO*///					{
/*TODO*///						value = palette[value];
/*TODO*///						while (sourcex < rle_end && dest >= start)
/*TODO*///							*dest-- = value, sourcex += dx;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						while (sourcex < rle_end)
/*TODO*///							dest--, sourcex += dx;
/*TODO*///					}
/*TODO*///				next2:
/*TODO*///					;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*
/*TODO*///	 *	Draw a horizontally-flipped RLE-compressed object
/*TODO*///	 *
/*TODO*///	 *	What it says. RLE decoding is performed on the fly to a 16-bit bitmap.
/*TODO*///	 *
/*TODO*///	 */
/*TODO*///	
/*TODO*///	void draw_rle_zoom_hflip_16(struct osd_bitmap *bitmap, const struct atarigen_rle_descriptor *gfx,
/*TODO*///			UINT32 color, int flipy, int sx, int sy, int scalex, int scaley,
/*TODO*///			const struct rectangle *clip)
/*TODO*///	{
/*TODO*///		const UINT16 *palette = &rle_colortable[color];
/*TODO*///		const UINT16 *row_start = gfx.data;
/*TODO*///		const UINT16 *table = gfx.table;
/*TODO*///		volatile int current_row = 0;
/*TODO*///	
/*TODO*///		int scaled_width = (scalex * gfx.width + 0x7fff) >> 16;
/*TODO*///		int scaled_height = (scaley * gfx.height + 0x7fff) >> 16;
/*TODO*///		int pixels_to_skip = 0, xclipped = 0;
/*TODO*///		int dx, dy, ex, ey;
/*TODO*///		int y, sourcey;
/*TODO*///	
/*TODO*///		/* make sure we didn't end up with 0 */
/*TODO*///		if (scaled_width == 0) scaled_width = 1;
/*TODO*///		if (scaled_height == 0) scaled_height = 1;
/*TODO*///	
/*TODO*///		/* compute the remaining parameters */
/*TODO*///		dx = (gfx.width << 16) / scaled_width;
/*TODO*///		dy = (gfx.height << 16) / scaled_height;
/*TODO*///		ex = sx + scaled_width - 1;
/*TODO*///		ey = sy + scaled_height - 1;
/*TODO*///		sourcey = dy / 2;
/*TODO*///	
/*TODO*///		/* left edge clip */
/*TODO*///		if (sx < clip.min_x)
/*TODO*///			sx = clip.min_x, xclipped = 1;
/*TODO*///		if (sx > clip.max_x)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* right edge clip */
/*TODO*///		if (ex > clip.max_x)
/*TODO*///			pixels_to_skip = ex - clip.max_x, xclipped = 1;
/*TODO*///		else if (ex < clip.min_x)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* top edge clip */
/*TODO*///		if (sy < clip.min_y)
/*TODO*///		{
/*TODO*///			sourcey += (clip.min_y - sy) * dy;
/*TODO*///			sy = clip.min_y;
/*TODO*///		}
/*TODO*///		else if (sy > clip.max_y)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* bottom edge clip */
/*TODO*///		if (ey > clip.max_y)
/*TODO*///			ey = clip.max_y;
/*TODO*///		else if (ey < clip.min_y)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* loop top to bottom */
/*TODO*///		for (y = sy; y <= ey; y++, sourcey += dy)
/*TODO*///		{
/*TODO*///			UINT16 *dest = (UINT16 *)&bitmap.line[y][ex * 2];
/*TODO*///			int j, sourcex = dx / 2, rle_end = 0;
/*TODO*///			const UINT16 *base;
/*TODO*///			int entry_count;
/*TODO*///	
/*TODO*///			/* loop until we hit the row we're on */
/*TODO*///			for ( ; current_row != (sourcey >> 16); current_row++)
/*TODO*///				row_start += 1 + *row_start;
/*TODO*///	
/*TODO*///			/* grab our starting parameters from this row */
/*TODO*///			base = row_start;
/*TODO*///			entry_count = *base++;
/*TODO*///	
/*TODO*///			/* non-clipped case */
/*TODO*///			if (!xclipped)
/*TODO*///			{
/*TODO*///				/* decode the pixels */
/*TODO*///				for (j = 0; j < entry_count; j++)
/*TODO*///				{
/*TODO*///					int word = *base++;
/*TODO*///					int count, value;
/*TODO*///	
/*TODO*///					/* decode the low byte first */
/*TODO*///					count = table[word & 0xff];
/*TODO*///					value = count & 0xff;
/*TODO*///					rle_end += (count & 0xff00) << 8;
/*TODO*///	
/*TODO*///					/* store copies of the value until we pass the end of this chunk */
/*TODO*///					if (value != 0)
/*TODO*///					{
/*TODO*///						value = palette[value];
/*TODO*///						while (sourcex < rle_end)
/*TODO*///							*dest-- = value, sourcex += dx;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						while (sourcex < rle_end)
/*TODO*///							dest--, sourcex += dx;
/*TODO*///					}
/*TODO*///	
/*TODO*///					/* decode the upper byte second */
/*TODO*///					count = table[word >> 8];
/*TODO*///					value = count & 0xff;
/*TODO*///					rle_end += (count & 0xff00) << 8;
/*TODO*///	
/*TODO*///					/* store copies of the value until we pass the end of this chunk */
/*TODO*///					if (value != 0)
/*TODO*///					{
/*TODO*///						value = palette[value];
/*TODO*///						while (sourcex < rle_end)
/*TODO*///							*dest-- = value, sourcex += dx;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						while (sourcex < rle_end)
/*TODO*///							dest--, sourcex += dx;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* clipped case */
/*TODO*///			else
/*TODO*///			{
/*TODO*///				const UINT16 *start = (const UINT16 *)&bitmap.line[y][sx * 2];
/*TODO*///				int to_be_skipped = pixels_to_skip;
/*TODO*///	
/*TODO*///				/* decode the pixels */
/*TODO*///				for (j = 0; j < entry_count && dest >= start; j++)
/*TODO*///				{
/*TODO*///					int word = *base++;
/*TODO*///					int count, value;
/*TODO*///	
/*TODO*///					/* decode the low byte first */
/*TODO*///					count = table[word & 0xff];
/*TODO*///					value = count & 0xff;
/*TODO*///					rle_end += (count & 0xff00) << 8;
/*TODO*///	
/*TODO*///					/* store copies of the value until we pass the end of this chunk */
/*TODO*///					if (to_be_skipped != 0)
/*TODO*///					{
/*TODO*///						while (to_be_skipped && sourcex < rle_end)
/*TODO*///							dest--, sourcex += dx, to_be_skipped--;
/*TODO*///						if (to_be_skipped != 0) goto next3;
/*TODO*///					}
/*TODO*///					if (value != 0)
/*TODO*///					{
/*TODO*///						value = palette[value];
/*TODO*///						while (sourcex < rle_end && dest >= start)
/*TODO*///							*dest-- = value, sourcex += dx;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						while (sourcex < rle_end)
/*TODO*///							dest--, sourcex += dx;
/*TODO*///					}
/*TODO*///	
/*TODO*///				next3:
/*TODO*///					/* decode the upper byte second */
/*TODO*///					count = table[word >> 8];
/*TODO*///					value = count & 0xff;
/*TODO*///					rle_end += (count & 0xff00) << 8;
/*TODO*///	
/*TODO*///					/* store copies of the value until we pass the end of this chunk */
/*TODO*///					if (to_be_skipped != 0)
/*TODO*///					{
/*TODO*///						while (to_be_skipped && sourcex < rle_end)
/*TODO*///							dest--, sourcex += dx, to_be_skipped--;
/*TODO*///						if (to_be_skipped != 0) goto next4;
/*TODO*///					}
/*TODO*///					if (value != 0)
/*TODO*///					{
/*TODO*///						value = palette[value];
/*TODO*///						while (sourcex < rle_end && dest >= start)
/*TODO*///							*dest-- = value, sourcex += dx;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						while (sourcex < rle_end)
/*TODO*///							dest--, sourcex += dx;
/*TODO*///					}
/*TODO*///				next4:
/*TODO*///					;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*--------------------------------------------------------------------------
/*TODO*///	
/*TODO*///		Playfield rendering
/*TODO*///	
/*TODO*///			atarigen_pf_state - data block describing the playfield
/*TODO*///	
/*TODO*///			atarigen_pf_callback - called back for each chunk during processing
/*TODO*///	
/*TODO*///			atarigen_pf_init - initializes and configures the playfield state
/*TODO*///			atarigen_pf_free - frees all memory allocated by atarigen_pf_init
/*TODO*///			atarigen_pf_reset - reset for a new frame (use only if not using interrupt system)
/*TODO*///			atarigen_pf_update - updates the playfield state for the given scanline
/*TODO*///			atarigen_pf_process - processes the current list of parameters
/*TODO*///	
/*TODO*///			atarigen_pf2_init - same as above but for a second playfield
/*TODO*///			atarigen_pf2_free - same as above but for a second playfield
/*TODO*///			atarigen_pf2_reset - same as above but for a second playfield
/*TODO*///			atarigen_pf2_update - same as above but for a second playfield
/*TODO*///			atarigen_pf2_process - same as above but for a second playfield
/*TODO*///	
/*TODO*///	--------------------------------------------------------------------------*/
	
	/* types */
	public static class playfield_data
	{
		public osd_bitmap bitmap;
		public UBytePtr dirty;
		public UBytePtr visit;
	
		public int tilewidth;
		public int tileheight;
		public int tilewidth_shift;
		public int tileheight_shift;
		public int xtiles_mask;
		public int ytiles_mask;

		public int entries;
		public int[] scanline;
		public atarigen_pf_state[] state;
		public atarigen_pf_state[] last_state;
	};
	
	/* globals */
	public static  osd_bitmap atarigen_pf_bitmap;
	public static UBytePtr atarigen_pf_dirty;
        public static UBytePtr atarigen_pf_visit;

	public static osd_bitmap atarigen_pf2_bitmap;
        public static UBytePtr atarigen_pf2_dirty;
        public static UBytePtr atarigen_pf2_visit;

	public static osd_bitmap atarigen_pf_overrender_bitmap;
	public static int[] atarigen_overrender_colortable=new int[32];
	
	/* statics */
	public static playfield_data playfield = new playfield_data();
	public static playfield_data playfield2 = new playfield_data();

/*TODO*///	/* prototypes */
/*TODO*///	static int internal_pf_init(struct playfield_data *pf, const struct atarigen_pf_desc *source_desc);
/*TODO*///	static void internal_pf_free(struct playfield_data *pf);
/*TODO*///	static void internal_pf_reset(struct playfield_data *pf);
/*TODO*///	static void internal_pf_update(struct playfield_data *pf, const struct atarigen_pf_state *state, int scanline);
/*TODO*///	static void internal_pf_process(struct playfield_data *pf, atarigen_pf_callback callback, void *param, const struct rectangle *clip);
/*TODO*///	static int compute_shift(int size);
/*TODO*///	static int compute_mask(int count);
	
	
	/*
	 *	Playfield render initialization
	 *
	 *	Allocates memory for the playfield and initializes all structures.
	 *
	 */
	
	public static int internal_pf_init(playfield_data pf, atarigen_pf_desc source_desc)
	{
		/* allocate the bitmap */
		if (source_desc.noscroll == 0)
			pf.bitmap = bitmap_alloc(source_desc.tilewidth * source_desc.xtiles,
										source_desc.tileheight * source_desc.ytiles);
		else
			pf.bitmap = bitmap_alloc(Machine.drv.screen_width,
										Machine.drv.screen_height);
		if (pf.bitmap==null)
			return 1;
	
		/* allocate the dirty tile map */
		pf.dirty = new UBytePtr(source_desc.xtiles * source_desc.ytiles);
		if (pf.dirty==null)
		{
			internal_pf_free(pf);
			return 1;
		}
		memset(pf.dirty, 0xff, source_desc.xtiles * source_desc.ytiles);
	
		/* allocate the visitation map */
		pf.visit = new UBytePtr(source_desc.xtiles * source_desc.ytiles);
		if (pf.visit==null)
		{
			internal_pf_free(pf);
			return 1;
		}
	
		/* allocate the list of scanlines */
		pf.scanline = new int[source_desc.ytiles * source_desc.tileheight];
		if (pf.scanline==null)
		{
			internal_pf_free(pf);
			return 1;
		}
	
		/* allocate the list of parameters */
                int _lo = source_desc.ytiles * source_desc.tileheight;
		pf.state = new atarigen_pf_state[_lo];
                for (int _i=0 ; _i<_lo ; _i++)
                    pf.state[_i] = new atarigen_pf_state();
                
		if (pf.state==null)
		{
			internal_pf_free(pf);
			return 1;
		}
	
		/* copy the basic data */
		pf.tilewidth = source_desc.tilewidth;
		pf.tileheight = source_desc.tileheight;
		pf.tilewidth_shift = compute_shift(source_desc.tilewidth);
		pf.tileheight_shift = compute_shift(source_desc.tileheight);
		pf.xtiles_mask = compute_mask(source_desc.xtiles);
		pf.ytiles_mask = compute_mask(source_desc.ytiles);
	
		/* initialize the last state to all zero */
		pf.last_state = pf.state;
/*TODO*///		memset(pf.last_state, 0, sizeof(*pf.last_state));
	
		/* reset */
		internal_pf_reset(pf);
	
		return 0;
	}
	
	public static int atarigen_pf_init(atarigen_pf_desc source_desc)
	{
		int result = internal_pf_init(playfield, source_desc);
		if (result==0)
		{
			/* allocate the overrender bitmap */
			atarigen_pf_overrender_bitmap = bitmap_alloc(Machine.drv.screen_width, Machine.drv.screen_height);
			if (atarigen_pf_overrender_bitmap==null)
			{
				internal_pf_free(playfield);
				return 1;
			}
	
			atarigen_pf_bitmap = playfield.bitmap;
			atarigen_pf_dirty = new UBytePtr(playfield.dirty);
			atarigen_pf_visit = new UBytePtr(playfield.visit);
		}
		return result;
	}
	
	public static int atarigen_pf2_init(atarigen_pf_desc source_desc)
	{
		int result = internal_pf_init(playfield2, source_desc);
		if (result==0)
		{
			atarigen_pf2_bitmap = playfield2.bitmap;
			atarigen_pf2_dirty = new UBytePtr(playfield2.dirty);
			atarigen_pf2_visit = new UBytePtr(playfield2.visit);
		}
		return result;
	}
	
	
	/*
	 *	Playfield render free
	 *
	 *	Frees all memory allocated by the playfield system.
	 *
	 */
	
	public static void internal_pf_free(playfield_data pf)
	{
		if (pf.bitmap != null)
			pf.bitmap = null;
	
		if (pf.dirty != null)
			pf.dirty = null;
	
		if (pf.visit != null)
			pf.visit = null;
	
		if (pf.scanline != null)
			pf.scanline = null;
	
		if (pf.state != null)
			pf.state = null;
	}
	
	public static void atarigen_pf_free()
	{
		internal_pf_free(playfield);
	
		/* free the overrender bitmap */
		if (atarigen_pf_overrender_bitmap != null)
			bitmap_free(atarigen_pf_overrender_bitmap);
		atarigen_pf_overrender_bitmap = null;
	}
	
	public static void atarigen_pf2_free()
	{
		internal_pf_free(playfield2);
	}
	
	
	/*
	 *	Playfield render reset
	 *
	 *	Resets the playfield system for a new frame. Note that this is automatically called
	 *	if you're using the interrupt system.
	 *
	 */
	
	public static void internal_pf_reset(playfield_data pf)
	{
		/* verify memory has been allocated -- we're called even if we're not used */
		if (pf.scanline!=null && pf.state!=null)
		{
			pf.entries = 0;
			internal_pf_update(pf, pf.last_state[0], 0);
		}
	}
	
	public static void atarigen_pf_reset()
	{
		internal_pf_reset(playfield);
	}
	
	public static void atarigen_pf2_reset()
	{
		internal_pf_reset(playfield2);
	}
	
	
	/*
	 *	Playfield render update
	 *
	 *	Sets the parameters for a given scanline.
	 *
	 */
	
	public static void internal_pf_update(playfield_data pf, atarigen_pf_state state, int scanline)
	{
		if (pf.entries > 0)
		{
			/* if the current scanline matches the previous one, just overwrite */
			if (pf.scanline[pf.entries - 1] == scanline)
				pf.entries--;
	
			/* if the current data matches the previous data, ignore it */
			else if (pf.last_state[0].hscroll == state.hscroll &&
                            pf.last_state[0].vscroll == state.vscroll &&
                            pf.last_state[0].param[0] == state.param[0] &&
                            pf.last_state[0].param[1] == state.param[1])
				return;
		}
	
		/* remember this entry as the last set of parameters */
		pf.last_state[0] = pf.state[pf.entries];
	
		/* copy in the data */
		pf.scanline[pf.entries] = scanline;
		pf.state[pf.entries++] = state;
	
		/* set the final scanline to be huge -- it will be clipped during processing */
		pf.scanline[pf.entries] = 100000;
	}

	public static void atarigen_pf_update(atarigen_pf_state state, int scanline)
	{
		internal_pf_update(playfield, state, scanline);
	}
	
	public static void atarigen_pf2_update(atarigen_pf_state state, int scanline)
	{
		internal_pf_update(playfield2, state, scanline);
	}
	
	
	/*
	 *	Playfield render process
	 *
	 *	Processes the playfield in chunks.
	 *
	 */
	
	public static void internal_pf_process(playfield_data pf, atarigen_pf_callback callback, Object param, rectangle clip)
	{
		rectangle curclip=new rectangle();
		rectangle tiles=new rectangle();
		int y;
	
		/* preinitialization */
		curclip.min_x = clip.min_x;
		curclip.max_x = clip.max_x;
	
		/* loop over all entries */
		for (y = 0; y < pf.entries; y++)
		{
			atarigen_pf_state current = pf.state[y];
	
			/* determine the clip rect */
			curclip.min_y = pf.scanline[y];
			curclip.max_y = pf.scanline[y + 1] - 1;
	
			/* skip if we're clipped out */
			if (curclip.min_y > clip.max_y || curclip.max_y < clip.min_y)
				continue;
	
			/* clip the clipper */
			if (curclip.min_y < clip.min_y)
				curclip.min_y = clip.min_y;
			if (curclip.max_y > clip.max_y)
				curclip.max_y = clip.max_y;
	
			/* determine the tile rect */
			tiles.min_x = ((current.hscroll + curclip.min_x) >> pf.tilewidth_shift) & pf.xtiles_mask;
			tiles.max_x = ((current.hscroll + curclip.max_x + pf.tilewidth) >> pf.tilewidth_shift) & pf.xtiles_mask;
			tiles.min_y = ((current.vscroll + curclip.min_y) >> pf.tileheight_shift) & pf.ytiles_mask;
			tiles.max_y = ((current.vscroll + curclip.max_y + pf.tileheight) >> pf.tileheight_shift) & pf.ytiles_mask;
	
			/* call the callback */
			(callback).handler(curclip, tiles, current, param);
		}
	}
	
	public static void atarigen_pf_process(atarigen_pf_callback callback, Object param, rectangle clip)
	{
		internal_pf_process(playfield, callback, param, clip);
	}
	
	public static void atarigen_pf2_process(atarigen_pf_callback callback, Object param, rectangle clip)
	{
		internal_pf_process(playfield2, callback, param, clip);
	}
	
	
	/*
	 *	Shift value computer
	 *
	 *	Determines the log2(value).
	 *
	 */
	
	static int compute_shift(int size)
	{
		int i;
	
		/* loop until we shift to zero */
		for (i = 0; i < 32; i++)
			if ((size >>= 1)==0)
				break;
		return i;
	}
	
	
	/*
	 *	Mask computer
	 *
	 *	Determines the best mask to use for the given value.
	 *
	 */
	
	static int compute_mask(int count)
	{
		int shift = compute_shift(count);
	
		/* simple case - count is an even power of 2 */
		if (count == (1 << shift))
			return count - 1;
	
		/* slightly less simple case - round up to the next power of 2 */
		else
			return (1 << (shift + 1)) - 1;
	}
	
	
	
	
	
	/*--------------------------------------------------------------------------
	
		Misc Video stuff
	
			atarigen_get_hblank - returns the current HBLANK state
			atarigen_halt_until_hblank_0_w - write handler for a HBLANK halt
			atarigen_666_paletteram_w - 6-6-6 special RGB paletteram handler
			atarigen_expanded_666_paletteram_w - byte version of above
	
	--------------------------------------------------------------------------*/
	
	/* prototypes */
	
	
	/*
	 *	Compute HBLANK state
	 *
	 *	Returns a guesstimate about the current HBLANK state, based on the assumption that
	 *	HBLANK represents 10% of the scanline period.
	 *
	 */
	
	public static int atarigen_get_hblank()
	{
		return ((cpu_gethorzbeampos() > (Machine.drv.screen_width * 9 / 10)) ? 1 : 0);
	}
	
	
/*TODO*///	/*
/*TODO*///	 *	Halt CPU 0 until HBLANK
/*TODO*///	 *
/*TODO*///	 *	What it says.
/*TODO*///	 *
/*TODO*///	 */
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr atarigen_halt_until_hblank_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		/* halt the CPU until the next HBLANK */
/*TODO*///		int hpos = cpu_gethorzbeampos();
/*TODO*///		int hblank = Machine.drv.screen_width * 9 / 10;
/*TODO*///		double fraction;
/*TODO*///	
/*TODO*///		/* if we're in hblank, set up for the next one */
/*TODO*///		if (hpos >= hblank)
/*TODO*///			hblank += Machine.drv.screen_width;
/*TODO*///	
/*TODO*///		/* halt and set a timer to wake up */
/*TODO*///		fraction = (double)(hblank - hpos) / (double)Machine.drv.screen_width;
/*TODO*///		timer_set(cpu_getscanlineperiod() * fraction, 0, unhalt_cpu);
/*TODO*///		cpu_set_halt_line(0, ASSERT_LINE);
/*TODO*///	} };
	
	
	/*
	 *	6-6-6 RGB palette RAM handler
	 *
	 *	What it says.
	 *
	 */
	
	public static WriteHandlerPtr atarigen_666_paletteram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = paletteram.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword,data);
		paletteram.WRITE_WORD(offset,newword);
	
		{
			int r, g, b;
	
			r = ((newword >> 9) & 0x3e) | ((newword >> 15) & 1);
			g = ((newword >> 4) & 0x3e) | ((newword >> 15) & 1);
			b = ((newword << 1) & 0x3e) | ((newword >> 15) & 1);
	
			r = (r << 2) | (r >> 4);
			g = (g << 2) | (g >> 4);
			b = (b << 2) | (b >> 4);
	
			palette_change_color(offset / 2, r, g, b);
		}
	} };
	
	
	/*
	 *	6-6-6 RGB expanded palette RAM handler
	 *
	 *	What it says.
	 *
	 */
	
	public static WriteHandlerPtr atarigen_expanded_666_paletteram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		COMBINE_WORD_MEM(paletteram,offset, data);
	
		if ((data & 0xff000000)==0)
		{
			int palentry = offset / 4;
			int newword = (paletteram.READ_WORD(palentry * 4) & 0xff00) | (paletteram.READ_WORD(palentry * 4 + 2) >> 8);
	
			int r, g, b;
	
			r = ((newword >> 9) & 0x3e) | ((newword >> 15) & 1);
			g = ((newword >> 4) & 0x3e) | ((newword >> 15) & 1);
			b = ((newword << 1) & 0x3e) | ((newword >> 15) & 1);
	
			r = (r << 2) | (r >> 4);
			g = (g << 2) | (g >> 4);
			b = (b << 2) | (b >> 4);
	
			palette_change_color(palentry & 0x1ff, r, g, b);
		}
	} };
	
	
/*TODO*///	/*
/*TODO*///	 *	CPU unhalter
/*TODO*///	 *
/*TODO*///	 *	Timer callback to release the CPU from a halted state.
/*TODO*///	 *
/*TODO*///	 */
/*TODO*///	
/*TODO*///	static void unhalt_cpu(int param)
/*TODO*///	{
/*TODO*///		cpu_set_halt_line(param, CLEAR_LINE);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*--------------------------------------------------------------------------
/*TODO*///	
/*TODO*///		General stuff
/*TODO*///	
/*TODO*///			atarigen_show_slapstic_message - display warning about slapstic
/*TODO*///			atarigen_show_sound_message - display warning about coins
/*TODO*///			atarigen_update_messages - update messages
/*TODO*///	
/*TODO*///	--------------------------------------------------------------------------*/
	
	/* statics */
	public static String[] message_text=new String[10];
	public static int message_countdown;
	
/*TODO*///	/*
/*TODO*///	 *	Display a warning message about slapstic protection
/*TODO*///	 *
/*TODO*///	 *	What it says.
/*TODO*///	 *
/*TODO*///	 */
/*TODO*///	
/*TODO*///	void atarigen_show_slapstic_message(void)
/*TODO*///	{
/*TODO*///		message_text[0] = "There are known problems with";
/*TODO*///		message_text[1] = "later levels of this game due";
/*TODO*///		message_text[2] = "to incomplete slapstic emulation.";
/*TODO*///		message_text[3] = "You have been warned.";
/*TODO*///		message_text[4] = NULL;
/*TODO*///		message_countdown = 15 * Machine.drv.frames_per_second;
/*TODO*///	}
	
	
	/*
	 *	Display a warning message about sound being disabled
	 *
	 *	What it says.
	 *
	 */
	
	public static void atarigen_show_sound_message()
	{
		if (Machine.sample_rate == 0)
		{
			message_text[0] = "This game may have trouble accepting";
			message_text[1] = "coins, or may even behave strangely,";
			message_text[2] = "because you have disabled sound.";
			message_text[3] = null;
			message_countdown = 15 * Machine.drv.frames_per_second;
		}
	}
	
	
	/*
	 *	Update on-screen messages
	 *
	 *	What it says.
	 *
	 */
	
	public static void atarigen_update_messages()
	{
		if (message_countdown!=0 && message_text[0]!=null)
		{
			int maxwidth = 0;
			int lines, x, y, i, j;
	
			/* first count lines and determine the maximum width */
			for (lines = 0; lines < 10; lines++)
			{
				if (message_text[lines]==null) break;
				x = strlen(message_text[lines]);
				if (x > maxwidth) maxwidth = x;
			}
			maxwidth += 2;
	
			/* determine y offset */
			x = (Machine.uiwidth - Machine.uifontwidth * maxwidth) / 2;
			y = (Machine.uiheight - Machine.uifontheight * (lines + 2)) / 2;
	
			/* draw a row of spaces at the top and bottom */
			for (i = 0; i < maxwidth; i++)
			{
				ui_text(/*Machine.scrbitmap,*/ " ", x + i * Machine.uifontwidth, y);
				ui_text(/*Machine.scrbitmap,*/ " ", x + i * Machine.uifontwidth, y + (lines + 1) * Machine.uifontheight);
			}
			y += Machine.uifontheight;
	
			/* draw the message */
			for (i = 0; i < lines; i++)
			{
				int width = strlen(message_text[i]) * Machine.uifontwidth;
				int dx = (Machine.uifontwidth * maxwidth - width) / 2;
	
				for (j = 0; j < dx; j += Machine.uifontwidth)
				{
					ui_text(/*Machine.scrbitmap,*/ " ", x + j, y);
					ui_text(/*Machine.scrbitmap,*/ " ", x + (maxwidth - 1) * Machine.uifontwidth - j, y);
				}
	
				ui_text(/*Machine.scrbitmap,*/ message_text[i], x + dx, y);
				y += Machine.uifontheight;
			}
	
			/* decrement the counter */
			message_countdown--;
	
			/* if a coin is inserted, make the message go away */
			if (keyboard_pressed_memory(KEYCODE_5)!=0 || keyboard_pressed_memory(KEYCODE_6)!=0 ||
			    keyboard_pressed_memory(KEYCODE_7)!=0 || keyboard_pressed_memory(KEYCODE_8)!=0)
				message_countdown = 0;
		}
		else
			message_text[0] = null;
	}
	
	
}
