/*##########################################################################

	atarigen.c

	General functions for Atari raster games.

##########################################################################*/


/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b16.machine;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v037b16.machine.atarigenH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.timer.*;
import static gr.codebb.arcadeflex.v037b7.mame.timerH.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.platform.fileio.*;
import static gr.codebb.arcadeflex.common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;

public class atarigen
{
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*##########################################################################
/*TODO*///		CONSTANTS
/*TODO*///	##########################################################################*/
/*TODO*///	
/*TODO*///	#define SOUND_INTERLEAVE_RATE		TIME_IN_USEC(50)
/*TODO*///	#define SOUND_INTERLEAVE_REPEAT		20
	
	
	
	/*##########################################################################
		GLOBAL VARIABLES
	##########################################################################*/
	
	public static int 			atarigen_scanline_int_state;
	public static int 			atarigen_sound_int_state;
	public static int 			atarigen_video_int_state;

	public static UShortPtr                 atarigen_eeprom_default;
	public static UBytePtr			atarigen_eeprom;
	public static int[] 			atarigen_eeprom_size=new int[1];

/*TODO*///	int 				atarigen_cpu_to_sound_ready;
/*TODO*///	int 				atarigen_sound_to_cpu_ready;

	public static UBytePtr			atarivc_data;
	public static UBytePtr			atarivc_eof_data;
/*TODO*///	struct atarivc_state_desc atarivc_state;
	
	
	
	/*##########################################################################
		STATIC VARIABLES
	##########################################################################*/
	
	static atarigen_void_callbackPtr update_int_callback;
	static timer_entry		scanline_interrupt_timer;

	static int 		eeprom_unlocked;

/*TODO*///	static UINT8 		atarigen_slapstic_num;
/*TODO*///	static data16_t *	atarigen_slapstic;
/*TODO*///	
/*TODO*///	static UINT8 		sound_cpu_num;
/*TODO*///	static UINT8 		atarigen_cpu_to_sound;
/*TODO*///	static UINT8 		atarigen_sound_to_cpu;
/*TODO*///	static UINT8 		timed_int;
/*TODO*///	static UINT8 		ym2151_int;
/*TODO*///	
/*TODO*///	static UINT8 *		speed_a, *speed_b;
/*TODO*///	static UINT32 		speed_pc;
/*TODO*///	
/*TODO*///	static atarigen_scanline_callback scanline_callback;
/*TODO*///	static int 			scanlines_per_callback;
/*TODO*///	static double 		scanline_callback_period;
/*TODO*///	static int 			last_scanline;
/*TODO*///	
/*TODO*///	static int 			actual_vc_latch0;
/*TODO*///	static int 			actual_vc_latch1;
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*##########################################################################
/*TODO*///		STATIC FUNCTION DECLARATIONS
/*TODO*///	##########################################################################*/
/*TODO*///	
/*TODO*///	
/*TODO*///	static void decompress_eeprom_word(const data16_t *data);
/*TODO*///	static void decompress_eeprom_byte(const data16_t *data);
/*TODO*///	
/*TODO*///	static 
/*TODO*///	static void atarigen_set_vol(int volume, const char *string);
/*TODO*///	
/*TODO*///	
/*TODO*///	static void atarivc_common_w(offs_t offset, data16_t newword);
	
	
	
	
	/*##########################################################################
		INTERRUPT HANDLING
	##########################################################################*/
	
	/*---------------------------------------------------------------
		atarigen_interrupt_reset: Initializes the state of all
		the interrupt sources.
	---------------------------------------------------------------*/
	
	public static void atarigen_interrupt_reset(atarigen_void_callbackPtr update_int)
	{
		/* set the callback */
		update_int_callback = update_int;
	
		/* reset the interrupt states */
		atarigen_video_int_state = atarigen_sound_int_state = atarigen_scanline_int_state = 0;
		scanline_interrupt_timer = null;
	}
	
	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_update_interrupts: Forces the interrupt callback
/*TODO*///		to be called with the current VBLANK and sound interrupt
/*TODO*///		states.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void atarigen_update_interrupts(void)
/*TODO*///	{
/*TODO*///		(*update_int_callback)();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_scanline_int_set: Sets the scanline when the next
/*TODO*///		scanline interrupt should be generated.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void atarigen_scanline_int_set(int scanline)
/*TODO*///	{
/*TODO*///		if (scanline_interrupt_timer != 0)
/*TODO*///			timer_remove(scanline_interrupt_timer);
/*TODO*///		scanline_interrupt_timer = timer_set(cpu_getscanlinetime(scanline), 0, scanline_interrupt_callback);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_scanline_int_gen: Standard interrupt routine
/*TODO*///		which sets the scanline interrupt state.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	int atarigen_scanline_int_gen(void)
/*TODO*///	{
/*TODO*///		atarigen_scanline_int_state = 1;
/*TODO*///		(*update_int_callback)();
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_scanline_int_ack_w: Resets the state of the
/*TODO*///		scanline interrupt.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	WRITE16_HANDLER( atarigen_scanline_int_ack_w )
/*TODO*///	{
/*TODO*///		atarigen_scanline_int_state = 0;
/*TODO*///		(*update_int_callback)();
/*TODO*///	}
/*TODO*///	
/*TODO*///	WRITE32_HANDLER( atarigen_scanline_int_ack32_w )
/*TODO*///	{
/*TODO*///		atarigen_scanline_int_state = 0;
/*TODO*///		(*update_int_callback)();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_sound_int_gen: Standard interrupt routine which
/*TODO*///		sets the sound interrupt state.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	int atarigen_sound_int_gen(void)
/*TODO*///	{
/*TODO*///		atarigen_sound_int_state = 1;
/*TODO*///		(*update_int_callback)();
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_sound_int_ack_w: Resets the state of the sound
/*TODO*///		interrupt.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	WRITE16_HANDLER( atarigen_sound_int_ack_w )
/*TODO*///	{
/*TODO*///		atarigen_sound_int_state = 0;
/*TODO*///		(*update_int_callback)();
/*TODO*///	}
/*TODO*///	
/*TODO*///	WRITE32_HANDLER( atarigen_sound_int_ack32_w )
/*TODO*///	{
/*TODO*///		atarigen_sound_int_state = 0;
/*TODO*///		(*update_int_callback)();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_video_int_gen: Standard interrupt routine which
/*TODO*///		sets the video interrupt state.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	int atarigen_video_int_gen(void)
/*TODO*///	{
/*TODO*///		atarigen_video_int_state = 1;
/*TODO*///		(*update_int_callback)();
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_video_int_ack_w: Resets the state of the video
/*TODO*///		interrupt.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	WRITE16_HANDLER( atarigen_video_int_ack_w )
/*TODO*///	{
/*TODO*///		atarigen_video_int_state = 0;
/*TODO*///		(*update_int_callback)();
/*TODO*///	}
/*TODO*///	
/*TODO*///	WRITE32_HANDLER( atarigen_video_int_ack32_w )
/*TODO*///	{
/*TODO*///		atarigen_video_int_state = 0;
/*TODO*///		(*update_int_callback)();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		scanline_interrupt_callback: Signals an interrupt.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	public static timer_callback scanline_interrupt_callback = new timer_callback() { public void handler(int param) 
/*TODO*///	{
/*TODO*///		/* generate the interrupt */
/*TODO*///		atarigen_scanline_int_gen();
/*TODO*///	
/*TODO*///		/* set a new timer to go off at the same scan line next frame */
/*TODO*///		scanline_interrupt_timer = timer_set(TIME_IN_HZ(Machine.drv.frames_per_second), 0, scanline_interrupt_callback);
/*TODO*///	} };
	
	
	
	/*##########################################################################
		EEPROM HANDLING
	##########################################################################*/
	
	/*---------------------------------------------------------------
		atarigen_eeprom_reset: Makes sure that the unlocked state
		is cleared when we reset.
	---------------------------------------------------------------*/
	
	public static void atarigen_eeprom_reset()
	{
		eeprom_unlocked = 0;
	}
	
	
	/*---------------------------------------------------------------
		atarigen_eeprom_enable_w: Any write to this handler will
		allow one byte to be written to the EEPROM data area the
		next time.
	---------------------------------------------------------------*/
	
	public static WriteHandlerPtr atarigen_eeprom_enable_w = new WriteHandlerPtr() {
            @Override
            public void handler(int offset, int data) {
                eeprom_unlocked = 1;
            }
        };
	
/*TODO*///	WRITE32_HANDLER( atarigen_eeprom_enable32_w )
/*TODO*///	{
/*TODO*///		eeprom_unlocked = 1;
/*TODO*///	}
	
	
	/*---------------------------------------------------------------
		atarigen_eeprom_w: Writes a "word" to the EEPROM, which is
		almost always accessed via the low byte of the word only.
		If the EEPROM hasn't been unlocked, the write attempt is
		ignored.
	---------------------------------------------------------------*/
	
	public static WriteHandlerPtr atarigen_eeprom_w = new WriteHandlerPtr() {
            @Override
            public void handler(int offset, int data) {
                if (eeprom_unlocked==0)
			return;
	
		//COMBINE_DATA(&atarigen_eeprom[offset]);
                COMBINE_WORD_MEM(atarigen_eeprom, offset, data);
            
		eeprom_unlocked = 0;
            }
        };
	
	
/*TODO*///	WRITE32_HANDLER( atarigen_eeprom32_w )
/*TODO*///	{
/*TODO*///		if (!eeprom_unlocked)
/*TODO*///			return;
/*TODO*///	
/*TODO*///		COMBINE_DATA(&((data32_t *)atarigen_eeprom)[offset]);
/*TODO*///		eeprom_unlocked = 0;
/*TODO*///	}
	
	
	/*---------------------------------------------------------------
		atarigen_eeprom_r: Reads a "word" from the EEPROM, which is
		almost always accessed via the low byte of the word only.
	---------------------------------------------------------------*/
	
	public static ReadHandlerPtr atarigen_eeprom_r = new ReadHandlerPtr() {
            @Override
            public int handler(int offset) {
		return atarigen_eeprom.read(offset) | 0xff00;
            }
        };
	
/*TODO*///	READ16_HANDLER( atarigen_eeprom_upper_r )
/*TODO*///	{
/*TODO*///		return atarigen_eeprom[offset] | 0x00ff;
/*TODO*///	}
/*TODO*///	
/*TODO*///	READ32_HANDLER( atarigen_eeprom_upper32_r )
/*TODO*///	{
/*TODO*///		return (atarigen_eeprom[offset * 2] << 16) | atarigen_eeprom[offset * 2 + 1] | 0x00ff00ff;
/*TODO*///	}
	
	
	/*---------------------------------------------------------------
		atarigen_nvram_handler: Loads the EEPROM data.
	---------------------------------------------------------------*/
	
	public static nvramPtr atarigen_nvram_handler  = new nvramPtr() { public void handler(Object file, int read_or_write) 
	{
		if (read_or_write != 0)
			osd_fwrite(file, atarigen_eeprom, atarigen_eeprom_size[0]);
		else if (file != null)
			osd_fread(file, atarigen_eeprom, atarigen_eeprom_size[0]);
		else
		{
			/* all 0xff's work for most games */
			memset(atarigen_eeprom, 0xff, atarigen_eeprom_size[0]);
	
			/* anything else must be decompressed */
			if (atarigen_eeprom_default != null)
			{
				if (atarigen_eeprom_default.read(0) == 0)
					decompress_eeprom_byte(new UShortPtr(atarigen_eeprom_default, 1));
				else
					decompress_eeprom_word(new UShortPtr(atarigen_eeprom_default, 1));
			}
		}
	} };
	
	
	/*---------------------------------------------------------------
		decompress_eeprom_word: Used for decompressing EEPROM data
		that has every other byte invalid.
	---------------------------------------------------------------*/
	
	static void decompress_eeprom_word(UShortPtr data)
	{
		UShortPtr dest = new UShortPtr(atarigen_eeprom);
		int value;
	
		while ((value = data.read(0)) != 0)
		{
                        data.inc(1);
			int count = (value >> 8);
			value = (value << 8) | (value & 0xff);
	
			while (count-- != 0){
				dest.write(0, (char) value);
                                dest.inc(1);
                        }
		}
	}
	
	
	/*---------------------------------------------------------------
		decompress_eeprom_byte: Used for decompressing EEPROM data
		that is byte-packed.
	---------------------------------------------------------------*/
	
	static void decompress_eeprom_byte(UShortPtr data)
	{
		UBytePtr dest = new UBytePtr(atarigen_eeprom.memory);
		int value;
                int _data=0;
	
		while ((value = data.read(_data++)) != 0)
		{
			int count = (value >> 8);
			value = (value << 8) | (value & 0xff);
	
			while (count-- != 0)
				dest.writeinc( value );
		}
	}
	
	
	
/*TODO*///	/*##########################################################################
/*TODO*///		SLAPSTIC HANDLING
/*TODO*///	##########################################################################*/
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_slapstic_init: Installs memory handlers for the
/*TODO*///		slapstic and sets the chip number.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void atarigen_slapstic_init(int cpunum, int base, int chipnum)
/*TODO*///	{
/*TODO*///		atarigen_slapstic_num = chipnum;
/*TODO*///		atarigen_slapstic = NULL;
/*TODO*///		if (chipnum != 0)
/*TODO*///		{
/*TODO*///			slapstic_init(chipnum);
/*TODO*///			atarigen_slapstic = install_mem_read16_handler(cpunum, base, base + 0x7fff, atarigen_slapstic_r);
/*TODO*///			atarigen_slapstic = install_mem_write16_handler(cpunum, base, base + 0x7fff, atarigen_slapstic_w);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_slapstic_reset: Makes the selected slapstic number
/*TODO*///		active and resets its state.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void atarigen_slapstic_reset(void)
/*TODO*///	{
/*TODO*///		if (atarigen_slapstic_num != 0)
/*TODO*///			slapstic_reset();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_slapstic_w: Assuming that the slapstic sits in
/*TODO*///		ROM memory space, we just simply tweak the slapstic at this
/*TODO*///		address and do nothing more.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	WRITE16_HANDLER( atarigen_slapstic_w )
/*TODO*///	{
/*TODO*///		slapstic_tweak(offset);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_slapstic_r: Tweaks the slapstic at the appropriate
/*TODO*///		address and then reads a word from the underlying memory.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	READ16_HANDLER( atarigen_slapstic_r )
/*TODO*///	{
/*TODO*///		int bank = slapstic_tweak(offset) * 0x1000;
/*TODO*///		return atarigen_slapstic[bank + (offset & 0xfff)];
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*##########################################################################
/*TODO*///		SOUND I/O
/*TODO*///	##########################################################################*/
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_sound_io_reset: Resets the state of the sound I/O.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void atarigen_sound_io_reset(int cpu_num)
/*TODO*///	{
/*TODO*///		/* remember which CPU is the sound CPU */
/*TODO*///		sound_cpu_num = cpu_num;
/*TODO*///	
/*TODO*///		/* reset the internal interrupts states */
/*TODO*///		timed_int = ym2151_int = 0;
/*TODO*///	
/*TODO*///		/* reset the sound I/O states */
/*TODO*///		atarigen_cpu_to_sound = atarigen_sound_to_cpu = 0;
/*TODO*///		atarigen_cpu_to_sound_ready = atarigen_sound_to_cpu_ready = 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_6502_irq_gen: Generates an IRQ signal to the 6502
/*TODO*///		sound processor.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	public static InterruptPtr atarigen_6502_irq_gen = new InterruptPtr() { public int handler() 
/*TODO*///	{
/*TODO*///		timed_int = 1;
/*TODO*///		update_6502_irq();
/*TODO*///		return 0;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_6502_irq_ack_r: Resets the IRQ signal to the 6502
/*TODO*///		sound processor. Both reads and writes can be used.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr atarigen_6502_irq_ack_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		timed_int = 0;
/*TODO*///		update_6502_irq();
/*TODO*///		return 0;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr atarigen_6502_irq_ack_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		timed_int = 0;
/*TODO*///		update_6502_irq();
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_ym2151_irq_gen: Sets the state of the YM2151's
/*TODO*///		IRQ line.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void atarigen_ym2151_irq_gen(int irq)
/*TODO*///	{
/*TODO*///		ym2151_int = irq;
/*TODO*///		update_6502_irq();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_sound_reset_w: Write handler which resets the
/*TODO*///		sound CPU in response.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	WRITE16_HANDLER( atarigen_sound_reset_w )
/*TODO*///	{
/*TODO*///		timer_set(TIME_NOW, 0, delayed_sound_reset);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_sound_reset: Resets the state of the sound CPU
/*TODO*///		manually.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void atarigen_sound_reset(void)
/*TODO*///	{
/*TODO*///		timer_set(TIME_NOW, 1, delayed_sound_reset);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_sound_w: Handles communication from the main CPU
/*TODO*///		to the sound CPU. Two versions are provided, one with the
/*TODO*///		data byte in the low 8 bits, and one with the data byte in
/*TODO*///		the upper 8 bits.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	WRITE16_HANDLER( atarigen_sound_w )
/*TODO*///	{
/*TODO*///		if (ACCESSING_LSB != 0)
/*TODO*///			timer_set(TIME_NOW, data & 0xff, delayed_sound_w);
/*TODO*///	}
/*TODO*///	
/*TODO*///	WRITE16_HANDLER( atarigen_sound_upper_w )
/*TODO*///	{
/*TODO*///		if (ACCESSING_MSB != 0)
/*TODO*///			timer_set(TIME_NOW, (data >> 8) & 0xff, delayed_sound_w);
/*TODO*///	}
/*TODO*///	
/*TODO*///	WRITE32_HANDLER( atarigen_sound_upper32_w )
/*TODO*///	{
/*TODO*///		if (ACCESSING_MSB32 != 0)
/*TODO*///			timer_set(TIME_NOW, (data >> 24) & 0xff, delayed_sound_w);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_sound_r: Handles reading data communicated from the
/*TODO*///		sound CPU to the main CPU. Two versions are provided, one
/*TODO*///		with the data byte in the low 8 bits, and one with the data
/*TODO*///		byte in the upper 8 bits.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	READ16_HANDLER( atarigen_sound_r )
/*TODO*///	{
/*TODO*///		atarigen_sound_to_cpu_ready = 0;
/*TODO*///		atarigen_sound_int_ack_w(0, 0, 0);
/*TODO*///		return atarigen_sound_to_cpu | 0xff00;
/*TODO*///	}
/*TODO*///	
/*TODO*///	READ16_HANDLER( atarigen_sound_upper_r )
/*TODO*///	{
/*TODO*///		atarigen_sound_to_cpu_ready = 0;
/*TODO*///		atarigen_sound_int_ack_w(0, 0, 0);
/*TODO*///		return (atarigen_sound_to_cpu << 8) | 0x00ff;
/*TODO*///	}
/*TODO*///	
/*TODO*///	READ32_HANDLER( atarigen_sound_upper32_r )
/*TODO*///	{
/*TODO*///		atarigen_sound_to_cpu_ready = 0;
/*TODO*///		atarigen_sound_int_ack32_w(0, 0, 0);
/*TODO*///		return (atarigen_sound_to_cpu << 24) | 0x00ffffff;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_6502_sound_w: Handles communication from the sound
/*TODO*///		CPU to the main CPU.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr atarigen_6502_sound_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		timer_set(TIME_NOW, data, delayed_6502_sound_w);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_6502_sound_r: Handles reading data communicated
/*TODO*///		from the main CPU to the sound CPU.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr atarigen_6502_sound_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		atarigen_cpu_to_sound_ready = 0;
/*TODO*///		cpu_set_nmi_line(sound_cpu_num, CLEAR_LINE);
/*TODO*///		return atarigen_cpu_to_sound;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		update_6502_irq: Called whenever the IRQ state changes. An
/*TODO*///		interrupt is generated if either atarigen_6502_irq_gen()
/*TODO*///		was called, or if the YM2151 generated an interrupt via
/*TODO*///		the atarigen_ym2151_irq_gen() callback.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void update_6502_irq(void)
/*TODO*///	{
/*TODO*///		if (timed_int || ym2151_int)
/*TODO*///			cpu_set_irq_line(sound_cpu_num, M6502_INT_IRQ, ASSERT_LINE);
/*TODO*///		else
/*TODO*///			cpu_set_irq_line(sound_cpu_num, M6502_INT_IRQ, CLEAR_LINE);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		sound_comm_timer: Set whenever a command is written from
/*TODO*///		the main CPU to the sound CPU, in order to temporarily bump
/*TODO*///		up the interleave rate. This helps ensure that communications
/*TODO*///		between the two CPUs works properly.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	static void sound_comm_timer(int reps_left)
/*TODO*///	{
/*TODO*///		if (--reps_left)
/*TODO*///			timer_set(SOUND_INTERLEAVE_RATE, reps_left, sound_comm_timer);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		delayed_sound_reset: Synchronizes the sound reset command
/*TODO*///		between the two CPUs.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	static void delayed_sound_reset(int param)
/*TODO*///	{
/*TODO*///		/* unhalt and reset the sound CPU */
/*TODO*///		if (param == 0)
/*TODO*///		{
/*TODO*///			cpu_set_halt_line(sound_cpu_num, CLEAR_LINE);
/*TODO*///			cpu_set_reset_line(sound_cpu_num, PULSE_LINE);
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* reset the sound write state */
/*TODO*///		atarigen_sound_to_cpu_ready = 0;
/*TODO*///		atarigen_sound_int_ack_w(0, 0, 0);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		delayed_sound_w: Synchronizes a data write from the main
/*TODO*///		CPU to the sound CPU.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	static void delayed_sound_w(int param)
/*TODO*///	{
/*TODO*///		/* warn if we missed something */
/*TODO*///		if (atarigen_cpu_to_sound_ready != 0)
/*TODO*///			logerror("Missed command from 68010\n");
/*TODO*///	
/*TODO*///		/* set up the states and signal an NMI to the sound CPU */
/*TODO*///		atarigen_cpu_to_sound = param;
/*TODO*///		atarigen_cpu_to_sound_ready = 1;
/*TODO*///		cpu_set_nmi_line(sound_cpu_num, ASSERT_LINE);
/*TODO*///	
/*TODO*///		/* allocate a high frequency timer until a response is generated */
/*TODO*///		/* the main CPU is *very* sensistive to the timing of the response */
/*TODO*///		timer_set(SOUND_INTERLEAVE_RATE, SOUND_INTERLEAVE_REPEAT, sound_comm_timer);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		delayed_6502_sound_w: Synchronizes a data write from the
/*TODO*///		sound CPU to the main CPU.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	static void delayed_6502_sound_w(int param)
/*TODO*///	{
/*TODO*///		/* warn if we missed something */
/*TODO*///		if (atarigen_sound_to_cpu_ready != 0)
/*TODO*///			logerror("Missed result from 6502\n");
/*TODO*///	
/*TODO*///		/* set up the states and signal the sound interrupt to the main CPU */
/*TODO*///		atarigen_sound_to_cpu = param;
/*TODO*///		atarigen_sound_to_cpu_ready = 1;
/*TODO*///		atarigen_sound_int_gen();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*##########################################################################
/*TODO*///		SOUND HELPERS
/*TODO*///	##########################################################################*/
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_init_6502_speedup: Installs a special read handler
/*TODO*///		to catch the main spin loop in the 6502 sound code. The
/*TODO*///		addresses accessed seem to be the same across a large
/*TODO*///		number of games, though the PC shifts.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void atarigen_init_6502_speedup(int cpunum, int compare_pc1, int compare_pc2)
/*TODO*///	{
/*TODO*///		UINT8 *memory = memory_region(REGION_CPU1+cpunum);
/*TODO*///		int address_low, address_high;
/*TODO*///	
/*TODO*///		/* determine the pointer to the first speed check location */
/*TODO*///		address_low = memory[compare_pc1 + 1] | (memory[compare_pc1 + 2] << 8);
/*TODO*///		address_high = memory[compare_pc1 + 4] | (memory[compare_pc1 + 5] << 8);
/*TODO*///		if (address_low != address_high - 1)
/*TODO*///			logerror("Error: address %04X does not point to a speedup location!", compare_pc1);
/*TODO*///		speed_a = &memory[address_low];
/*TODO*///	
/*TODO*///		/* determine the pointer to the second speed check location */
/*TODO*///		address_low = memory[compare_pc2 + 1] | (memory[compare_pc2 + 2] << 8);
/*TODO*///		address_high = memory[compare_pc2 + 4] | (memory[compare_pc2 + 5] << 8);
/*TODO*///		if (address_low != address_high - 1)
/*TODO*///			logerror("Error: address %04X does not point to a speedup location!", compare_pc2);
/*TODO*///		speed_b = &memory[address_low];
/*TODO*///	
/*TODO*///		/* install a handler on the second address */
/*TODO*///		speed_pc = compare_pc2;
/*TODO*///		install_mem_read_handler(cpunum, address_low, address_low, m6502_speedup_r);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_set_vol: Scans for a particular sound chip and
/*TODO*///		changes the volume on all channels associated with it.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void atarigen_set_vol(int volume, const char *string)
/*TODO*///	{
/*TODO*///		int ch;
/*TODO*///	
/*TODO*///		for (ch = 0; ch < MIXER_MAX_CHANNELS; ch++)
/*TODO*///		{
/*TODO*///			const char *name = mixer_get_name(ch);
/*TODO*///			if (name && strstr(name, string))
/*TODO*///				mixer_set_volume(ch, volume);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_set_XXXXX_vol: Sets the volume for a given type
/*TODO*///		of chip.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void atarigen_set_ym2151_vol(int volume)
/*TODO*///	{
/*TODO*///		atarigen_set_vol(volume, "2151");
/*TODO*///	}
/*TODO*///	
/*TODO*///	void atarigen_set_ym2413_vol(int volume)
/*TODO*///	{
/*TODO*///		atarigen_set_vol(volume, "2413");
/*TODO*///	}
/*TODO*///	
/*TODO*///	void atarigen_set_pokey_vol(int volume)
/*TODO*///	{
/*TODO*///		atarigen_set_vol(volume, "POKEY");
/*TODO*///	}
/*TODO*///	
/*TODO*///	void atarigen_set_tms5220_vol(int volume)
/*TODO*///	{
/*TODO*///		atarigen_set_vol(volume, "5220");
/*TODO*///	}
/*TODO*///	
/*TODO*///	void atarigen_set_oki6295_vol(int volume)
/*TODO*///	{
/*TODO*///		atarigen_set_vol(volume, "6295");
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		m6502_speedup_r: Handles speeding up the 6502.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr m6502_speedup_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		int result = speed_b[0];
/*TODO*///	
/*TODO*///		if (cpu_getpreviouspc() == speed_pc && speed_a[0] == speed_a[1] && result == speed_b[1])
/*TODO*///			cpu_spinuntil_int();
/*TODO*///	
/*TODO*///		return result;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*##########################################################################
/*TODO*///		SCANLINE TIMING
/*TODO*///	##########################################################################*/
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_scanline_timer_reset: Sets up the scanline timer.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void atarigen_scanline_timer_reset(atarigen_scanline_callback update_graphics, int frequency)
/*TODO*///	{
/*TODO*///		/* set the scanline callback */
/*TODO*///		scanline_callback = update_graphics;
/*TODO*///		scanline_callback_period = (double)frequency * cpu_getscanlineperiod();
/*TODO*///		scanlines_per_callback = frequency;
/*TODO*///	
/*TODO*///		/* compute the last scanline */
/*TODO*///		last_scanline = (int)(TIME_IN_HZ(Machine.drv.frames_per_second) / cpu_getscanlineperiod());
/*TODO*///	
/*TODO*///		/* set a timer to go off on the next VBLANK */
/*TODO*///		timer_set(cpu_getscanlinetime(Machine.drv.screen_height), 0, vblank_timer);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		vblank_timer: Called once every VBLANK to prime the scanline
/*TODO*///		timers.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	static void vblank_timer(int param)
/*TODO*///	{
/*TODO*///		/* set a timer to go off at scanline 0 */
/*TODO*///		timer_set(TIME_IN_USEC(Machine.drv.vblank_duration), 0, scanline_timer);
/*TODO*///	
/*TODO*///		/* set a timer to go off on the next VBLANK */
/*TODO*///		timer_set(cpu_getscanlinetime(Machine.drv.screen_height), 1, vblank_timer);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		scanline_timer: Called once every n scanlines to generate
/*TODO*///		the periodic callback to the main system.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	static void scanline_timer(int scanline)
/*TODO*///	{
/*TODO*///		/* callback */
/*TODO*///		if (scanline_callback != 0)
/*TODO*///		{
/*TODO*///			(*scanline_callback)(scanline);
/*TODO*///	
/*TODO*///			/* generate another? */
/*TODO*///			scanline += scanlines_per_callback;
/*TODO*///			if (scanline < last_scanline && scanlines_per_callback)
/*TODO*///				timer_set(scanline_callback_period, scanline, scanline_timer);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*##########################################################################
/*TODO*///		VIDEO CONTROLLER
/*TODO*///	##########################################################################*/
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarivc_eof_update: Callback that slurps up data and feeds
/*TODO*///		it into the video controller registers every refresh.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	static void atarivc_eof_update(int param)
/*TODO*///	{
/*TODO*///		atarivc_update(atarivc_eof_data);
/*TODO*///		timer_set(cpu_getscanlinetime(0), 0, atarivc_eof_update);
/*TODO*///	}
	
	
	/*---------------------------------------------------------------
		atarivc_reset: Initializes the video controller.
	---------------------------------------------------------------*/
	
	public static void atarivc_reset(UBytePtr eof_data)
	{
/*TODO*///		/* this allows us to manually reset eof_data to NULL if it's not used */
/*TODO*///		atarivc_eof_data = eof_data;
/*TODO*///	
/*TODO*///		/* clear the RAM we use */
/*TODO*///		memset(atarivc_data, 0, 0x40);
/*TODO*///		memset(&atarivc_state, 0, sizeof(atarivc_state));
/*TODO*///	
/*TODO*///		/* reset the latches */
/*TODO*///		atarivc_state.latch1 = atarivc_state.latch2 = -1;
/*TODO*///		actual_vc_latch0 = actual_vc_latch1 = -1;
/*TODO*///	
/*TODO*///		/* start a timer to go off a little before scanline 0 */
/*TODO*///		if (atarivc_eof_data != 0)
/*TODO*///			timer_set(cpu_getscanlinetime(0), 0, atarivc_eof_update);
	}
	
	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarivc_update: Copies the data from the specified location
/*TODO*///		once/frame into the video controller registers.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	void atarivc_update(const data16_t *data)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		/* echo all the commands to the video controller */
/*TODO*///		for (i = 0; i < 0x1c; i++)
/*TODO*///			if (data[i])
/*TODO*///				atarivc_common_w(i, data[i]);
/*TODO*///	
/*TODO*///		/* update the scroll positions */
/*TODO*///		atarimo_set_xscroll(0, atarivc_state.mo_xscroll, 0);
/*TODO*///		ataripf_set_xscroll(0, atarivc_state.pf0_xscroll, 0);
/*TODO*///		ataripf_set_xscroll(1, atarivc_state.pf1_xscroll, 0);
/*TODO*///		atarimo_set_yscroll(0, atarivc_state.mo_yscroll, 0);
/*TODO*///		ataripf_set_yscroll(0, atarivc_state.pf0_yscroll, 0);
/*TODO*///		ataripf_set_yscroll(1, atarivc_state.pf1_yscroll, 0);
/*TODO*///	
/*TODO*///		/* use this for debugging the video controller values */
/*TODO*///	#if 0
/*TODO*///		if (keyboard_pressed(KEYCODE_8))
/*TODO*///		{
/*TODO*///			static FILE *out;
/*TODO*///			if (!out) out = fopen("scroll.log", "w");
/*TODO*///			if (out != 0)
/*TODO*///			{
/*TODO*///				for (i = 0; i < 64; i++)
/*TODO*///					fprintf(out, "%04X ", data[i]);
/*TODO*///				fprintf(out, "\n");
/*TODO*///			}
/*TODO*///		}
/*TODO*///	#endif
/*TODO*///	}
	
	
	/*---------------------------------------------------------------
		atarivc_w: Handles an I/O write to the video controller.
	---------------------------------------------------------------*/
	
	public static WriteHandlerPtr atarivc_w = new WriteHandlerPtr() {
            @Override
            public void handler(int offset, int data) {
                int oldword = atarivc_data.read(offset);
		int newword = oldword;
	
		//COMBINE_DATA(&newword);
                newword = COMBINE_WORD(oldword, data);
                
		atarivc_common_w.handler(offset, newword);
                
            }
        };
		
	
	
	/*---------------------------------------------------------------
		atarivc_common_w: Does the bulk of the word for an I/O
		write.
	---------------------------------------------------------------*/
	
	static WriteHandlerPtr atarivc_common_w = new WriteHandlerPtr() {
            @Override
            public void handler(int offset, int newword) {
/*TODO*///		int oldword = atarivc_data[offset];
/*TODO*///		atarivc_data[offset] = newword;
/*TODO*///	
/*TODO*///		/* switch off the offset */
/*TODO*///		switch (offset)
/*TODO*///		{
/*TODO*///			/*
/*TODO*///				additional registers:
/*TODO*///	
/*TODO*///					01 = vertical start (for centering)
/*TODO*///					04 = horizontal start (for centering)
/*TODO*///			*/
/*TODO*///	
/*TODO*///			/* set the scanline interrupt here */
/*TODO*///			case 0x03:
/*TODO*///				if (oldword != newword)
/*TODO*///					atarigen_scanline_int_set(newword & 0x1ff);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			/* latch enable */
/*TODO*///			case 0x0a:
/*TODO*///	
/*TODO*///				/* reset the latches when disabled */
/*TODO*///				ataripf_set_latch_lo((newword & 0x0080) ? actual_vc_latch0 : -1);
/*TODO*///				ataripf_set_latch_hi((newword & 0x0080) ? actual_vc_latch1 : -1);
/*TODO*///	
/*TODO*///				/* check for rowscroll enable */
/*TODO*///				atarivc_state.rowscroll_enable = (newword & 0x2000) >> 13;
/*TODO*///	
/*TODO*///				/* check for palette banking */
/*TODO*///				atarivc_state.palette_bank = ((newword & 0x0400) >> 10) ^ 1;
/*TODO*///				break;
/*TODO*///	
/*TODO*///			/* indexed parameters */
/*TODO*///			case 0x10: case 0x11: case 0x12: case 0x13:
/*TODO*///			case 0x14: case 0x15: case 0x16: case 0x17:
/*TODO*///			case 0x18: case 0x19: case 0x1a: case 0x1b:
/*TODO*///				switch (newword & 15)
/*TODO*///				{
/*TODO*///					case 9:
/*TODO*///						atarivc_state.mo_xscroll = (newword >> 7) & 0x1ff;
/*TODO*///						break;
/*TODO*///	
/*TODO*///					case 10:
/*TODO*///						atarivc_state.pf1_xscroll_raw = (newword >> 7) & 0x1ff;
/*TODO*///						atarivc_update_pf_xscrolls();
/*TODO*///						break;
/*TODO*///	
/*TODO*///					case 11:
/*TODO*///						atarivc_state.pf0_xscroll_raw = (newword >> 7) & 0x1ff;
/*TODO*///						atarivc_update_pf_xscrolls();
/*TODO*///						break;
/*TODO*///	
/*TODO*///					case 13:
/*TODO*///						atarivc_state.mo_yscroll = (newword >> 7) & 0x1ff;
/*TODO*///						break;
/*TODO*///	
/*TODO*///					case 14:
/*TODO*///						atarivc_state.pf1_yscroll = (newword >> 7) & 0x1ff;
/*TODO*///						break;
/*TODO*///	
/*TODO*///					case 15:
/*TODO*///						atarivc_state.pf0_yscroll = (newword >> 7) & 0x1ff;
/*TODO*///						break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///	
/*TODO*///			/* latch 1 value */
/*TODO*///			case 0x1c:
/*TODO*///				actual_vc_latch0 = -1;
/*TODO*///				actual_vc_latch1 = newword;
/*TODO*///				ataripf_set_latch_lo((atarivc_data[0x0a] & 0x80) ? actual_vc_latch0 : -1);
/*TODO*///				ataripf_set_latch_hi((atarivc_data[0x0a] & 0x80) ? actual_vc_latch1 : -1);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			/* latch 2 value */
/*TODO*///			case 0x1d:
/*TODO*///				actual_vc_latch0 = newword;
/*TODO*///				actual_vc_latch1 = -1;
/*TODO*///				ataripf_set_latch_lo((atarivc_data[0x0a] & 0x80) ? actual_vc_latch0 : -1);
/*TODO*///				ataripf_set_latch_hi((atarivc_data[0x0a] & 0x80) ? actual_vc_latch1 : -1);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			/* scanline IRQ ack here */
/*TODO*///			case 0x1e:
/*TODO*///				atarigen_scanline_int_ack_w(0, 0, 0);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			/* log anything else */
/*TODO*///			case 0x00:
/*TODO*///			default:
/*TODO*///				if (oldword != newword)
/*TODO*///					logerror("vc_w(%02X, %04X) ** [prev=%04X]\n", offset, newword, oldword);
/*TODO*///				break;
/*TODO*///		}
            }
        };
        
	
	/*---------------------------------------------------------------
		atarivc_r: Handles an I/O read from the video controller.
	---------------------------------------------------------------*/
	
	public static ReadHandlerPtr atarivc_r = new ReadHandlerPtr() {
            @Override
            public int handler(int offset) {
		logerror("vc_r(%02X)\n", offset);
	
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
			return atarivc_data.read(offset);
            }
        };
	
	
	/*##########################################################################
		VIDEO HELPERS
	##########################################################################*/
	
	/*---------------------------------------------------------------
		atarigen_get_hblank: Returns a guesstimate about the current
		HBLANK state, based on the assumption that HBLANK represents
		10% of the scanline period.
	---------------------------------------------------------------*/
	
	public static int atarigen_get_hblank()
	{
		return (cpu_gethorzbeampos() > (Machine.drv.screen_width * 9 / 10))?1:0;
	}
	
	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_halt_until_hblank_0_w: Halts CPU 0 until the
/*TODO*///		next HBLANK.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	WRITE16_HANDLER( atarigen_halt_until_hblank_0_w )
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
/*TODO*///	}
	
	
	/*---------------------------------------------------------------
		atarigen_666_paletteram_w: 6-6-6 RGB palette RAM handler.
	---------------------------------------------------------------*/
	
	public static WriteHandlerPtr atarigen_666_paletteram_w = new WriteHandlerPtr() {
            @Override
            public void handler(int offset, int data) {
                int newword, r, g, b;
	
		//COMBINE_DATA(&paletteram16[offset]);
                int oldword = paletteram.READ_WORD(offset);
                newword = COMBINE_WORD(oldword, data);
                paletteram.WRITE_WORD(offset, newword);
                //
		//newword = paletteram16[offset];
                newword = paletteram.read(offset);
	
		r = ((newword >> 9) & 0x3e) | ((newword >> 15) & 1);
		g = ((newword >> 4) & 0x3e) | ((newword >> 15) & 1);
		b = ((newword << 1) & 0x3e) | ((newword >> 15) & 1);
	
		r = (r << 2) | (r >> 4);
		g = (g << 2) | (g >> 4);
		b = (b << 2) | (b >> 4);
	
		palette_change_color(offset, r, g, b);
            }
        };
	
	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_expanded_666_paletteram_w: 6-6-6 RGB expanded
/*TODO*///		palette RAM handler.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	WRITE16_HANDLER( atarigen_expanded_666_paletteram_w )
/*TODO*///	{
/*TODO*///		COMBINE_DATA(&paletteram16[offset]);
/*TODO*///	
/*TODO*///		if (ACCESSING_MSB != 0)
/*TODO*///		{
/*TODO*///			int palentry = offset / 2;
/*TODO*///			int newword = (paletteram16[palentry * 2] & 0xff00) | (paletteram16[palentry * 2 + 1] >> 8);
/*TODO*///	
/*TODO*///			int r, g, b;
/*TODO*///	
/*TODO*///			r = ((newword >> 9) & 0x3e) | ((newword >> 15) & 1);
/*TODO*///			g = ((newword >> 4) & 0x3e) | ((newword >> 15) & 1);
/*TODO*///			b = ((newword << 1) & 0x3e) | ((newword >> 15) & 1);
/*TODO*///	
/*TODO*///			r = (r << 2) | (r >> 4);
/*TODO*///			g = (g << 2) | (g >> 4);
/*TODO*///			b = (b << 2) | (b >> 4);
/*TODO*///	
/*TODO*///			palette_change_color(palentry & 0x1ff, r, g, b);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		atarigen_666_paletteram32_w: 6-6-6 RGB palette RAM handler.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	WRITE32_HANDLER( atarigen_666_paletteram32_w )
/*TODO*///	{
/*TODO*///		int newword, r, g, b;
/*TODO*///	
/*TODO*///		COMBINE_DATA(&paletteram32[offset]);
/*TODO*///		
/*TODO*///		if (ACCESSING_MSW32 != 0)
/*TODO*///		{
/*TODO*///			newword = paletteram32[offset] >> 16;
/*TODO*///		
/*TODO*///			r = ((newword >> 9) & 0x3e) | ((newword >> 15) & 1);
/*TODO*///			g = ((newword >> 4) & 0x3e) | ((newword >> 15) & 1);
/*TODO*///			b = ((newword << 1) & 0x3e) | ((newword >> 15) & 1);
/*TODO*///		
/*TODO*///			r = (r << 2) | (r >> 4);
/*TODO*///			g = (g << 2) | (g >> 4);
/*TODO*///			b = (b << 2) | (b >> 4);
/*TODO*///		
/*TODO*///			palette_change_color(offset * 2, r, g, b);
/*TODO*///		}
/*TODO*///	
/*TODO*///		if (ACCESSING_LSW32 != 0)
/*TODO*///		{
/*TODO*///			newword = paletteram32[offset] & 0xffff;
/*TODO*///		
/*TODO*///			r = ((newword >> 9) & 0x3e) | ((newword >> 15) & 1);
/*TODO*///			g = ((newword >> 4) & 0x3e) | ((newword >> 15) & 1);
/*TODO*///			b = ((newword << 1) & 0x3e) | ((newword >> 15) & 1);
/*TODO*///		
/*TODO*///			r = (r << 2) | (r >> 4);
/*TODO*///			g = (g << 2) | (g >> 4);
/*TODO*///			b = (b << 2) | (b >> 4);
/*TODO*///		
/*TODO*///			palette_change_color(offset * 2 + 1, r, g, b);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/*---------------------------------------------------------------
/*TODO*///		unhalt_cpu: Timer callback to release the CPU from a halted state.
/*TODO*///	---------------------------------------------------------------*/
/*TODO*///	
/*TODO*///	static void unhalt_cpu(int param)
/*TODO*///	{
/*TODO*///		cpu_set_halt_line(param, CLEAR_LINE);
/*TODO*///	}
	
	
	
	/*##########################################################################
		MISC HELPERS
	##########################################################################*/
	
	/*---------------------------------------------------------------
		atarigen_invert_region: Inverts the bits in a region.
	---------------------------------------------------------------*/
	
	public static void atarigen_invert_region(int region)
	{
		int length = memory_region_length(region);
		UBytePtr base = memory_region(region);
	
		while (length-- != 0){
			base.write(base.read() ^ 0xff);
                        base.inc();
                }
	}
	
	
/*TODO*///	void atarigen_swap_mem(void *ptr1, void *ptr2, int bytes)
/*TODO*///	{
/*TODO*///		UINT8 *p1 = ptr1;
/*TODO*///		UINT8 *p2 = ptr2;
/*TODO*///		while (bytes--)
/*TODO*///		{
/*TODO*///			int temp = *p1;
/*TODO*///			*p1++ = *p2;
/*TODO*///			*p2++ = temp;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}

}
