/** *************************************************************************
 *
 * machine.c
 *
 * Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
 * I/O ports)
 *
 * Tapper machine started by Chris Kirmse
 *
 ************************************************************************** */

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.machine;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b7.cpu.z80.z80H.*;
import static gr.codebb.arcadeflex.v037b7.mame.timer.*;
import static gr.codebb.arcadeflex.v036.sound.samples.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.*;
import static gr.codebb.arcadeflex.v037b7.mame.timerH.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v058.machine.z80fmly.*;
import static gr.codebb.arcadeflex.v058.machine.z80fmlyH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.mcr3.*;

public class mcr {

    /*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Global variables
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	double mcr68_timing_factor;
/*TODO*///	
    public static int/*UINT8*/ mcr_cocktail_flip;
    /*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Statics
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static UINT8 m6840_status;
/*TODO*///	static UINT8 m6840_status_read_since_int;
/*TODO*///	static UINT8 m6840_msb_buffer;
/*TODO*///	static UINT8 m6840_lsb_buffer;
/*TODO*///	static struct counter_state
/*TODO*///	{
/*TODO*///		UINT8	control;
/*TODO*///		UINT16	latch;
/*TODO*///		UINT16	count;
/*TODO*///		void *	timer;
/*TODO*///		double	period;
/*TODO*///	} m6840_state[3];
/*TODO*///	
/*TODO*///	/* MCR/68k interrupt states */
/*TODO*///	static UINT8 m6840_irq_state;
/*TODO*///	static UINT8 m6840_irq_vector;
/*TODO*///	static UINT8 v493_irq_state;
/*TODO*///	static UINT8 v493_irq_vector;
/*TODO*///	
/*TODO*///	static void (*v493_callback)(int param);
/*TODO*///	
/*TODO*///	static UINT8 zwackery_sound_data;
/*TODO*///	
/*TODO*///	static const double m6840_counter_periods[3] = { 1.0 / 30.0, 1000000.0, 1.0 / (512.0 * 30.0) };
/*TODO*///	static double m6840_internal_counter_period;	/* 68000 CLK / 10 */
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Function prototypes
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static void subtract_from_counter(int counter, int count);
/*TODO*///	
/*TODO*///	
/*TODO*///	static void zwackery_pia_irq(int state);
/*TODO*///	
/*TODO*///	static void reload_count(int counter);
/*TODO*///	

    /**
     * ***********************************
     *
     * Graphics declarations
     *
     ************************************
     */
    public static GfxLayout mcr_bg_layout = new GfxLayout(
            16, 16,
            0,
            4,
            new int[]{(0) + 0, (0) + 1, 0, 1},
            new int[]{0, 0, 2, 2, 4, 4, 6, 6,
                8, 8, 10, 10, 12, 12, 14, 14},
            new int[]{0 * 8, 0 * 8, 2 * 8, 2 * 8,
                4 * 8, 4 * 8, 6 * 8, 6 * 8,
                8 * 8, 8 * 8, 10 * 8, 10 * 8,
                12 * 8, 12 * 8, 14 * 8, 14 * 8},
            16 * 8
    );

    public static GfxLayout mcr_sprite_layout = new GfxLayout(
            32, 32,
            RGN_FRAC(1, 4),
            4,
            new int[]{0, 1, 2, 3},
            new int[]{0, 4,
                RGN_FRAC(1, 4) + 0, RGN_FRAC(1, 4) + 4,
                RGN_FRAC(2, 4) + 0, RGN_FRAC(2, 4) + 4,
                RGN_FRAC(3, 4) + 0, RGN_FRAC(3, 4) + 4,
                8, 12,
                RGN_FRAC(1, 4) + 8, RGN_FRAC(1, 4) + 12,
                RGN_FRAC(2, 4) + 8, RGN_FRAC(2, 4) + 12,
                RGN_FRAC(3, 4) + 8, RGN_FRAC(3, 4) + 12,
                16, 20,
                RGN_FRAC(1, 4) + 16, RGN_FRAC(1, 4) + 20,
                RGN_FRAC(2, 4) + 16, RGN_FRAC(2, 4) + 20,
                RGN_FRAC(3, 4) + 16, RGN_FRAC(3, 4) + 20,
                24, 28,
                RGN_FRAC(1, 4) + 24, RGN_FRAC(1, 4) + 28,
                RGN_FRAC(2, 4) + 24, RGN_FRAC(2, 4) + 28,
                RGN_FRAC(3, 4) + 24, RGN_FRAC(3, 4) + 28},
            new int[]{32 * 0, 32 * 1, 32 * 2, 32 * 3,
                32 * 4, 32 * 5, 32 * 6, 32 * 7,
                32 * 8, 32 * 9, 32 * 10, 32 * 11,
                32 * 12, 32 * 13, 32 * 14, 32 * 15,
                32 * 16, 32 * 17, 32 * 18, 32 * 19,
                32 * 20, 32 * 21, 32 * 22, 32 * 23,
                32 * 24, 32 * 25, 32 * 26, 32 * 27,
                32 * 28, 32 * 29, 32 * 30, 32 * 31},
            32 * 32
    );

    /*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	6821 PIA declarations
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	 
/*TODO*///	static struct pia6821_interface zwackery_pia_2_intf =
/*TODO*///	{
/*TODO*///		/*inputs : A/B,CA/B1,CA/B2 */ 0, input_port_0_r, 0, 0, 0, 0,
/*TODO*///		/*outputs: A/B,CA/B2       */ zwackery_pia_2_w, 0, 0, 0,
/*TODO*///		/*irqs   : A/B             */ zwackery_pia_irq, zwackery_pia_irq
/*TODO*///	};
/*TODO*///	
/*TODO*///	static struct pia6821_interface zwackery_pia_3_intf =
/*TODO*///	{
/*TODO*///		/*inputs : A/B,CA/B1,CA/B2 */ input_port_1_r, zwackery_port_2_r, 0, 0, 0, 0,
/*TODO*///		/*outputs: A/B,CA/B2       */ zwackery_pia_3_w, 0, zwackery_ca2_w, 0,
/*TODO*///		/*irqs   : A/B             */ 0, 0
/*TODO*///	};
/*TODO*///	
/*TODO*///	static struct pia6821_interface zwackery_pia_4_intf =
/*TODO*///	{
/*TODO*///		/*inputs : A/B,CA/B1,CA/B2 */ input_port_3_r, input_port_4_r, 0, 0, 0, 0,
/*TODO*///		/*outputs: A/B,CA/B2       */ 0, 0, 0, 0,
/*TODO*///		/*irqs   : A/B             */ 0, 0
/*TODO*///	};
/*TODO*///	
/*TODO*///	
	
	/*************************************
	 *
	 *	Generic MCR CTC interface
	 *
	 *************************************/
	
    public static IntrPtr ctc_interrupt = new IntrPtr() {

        public void handler(int state) {
                cpu_cause_interrupt(0, Z80_VECTOR(0, state));
        }
    };

    public static Z80_DaisyChain mcr_daisy_chain[]
            = {
                new Z80_DaisyChain(z80ctc_reset, z80ctc_interrupt, z80ctc_reti, 0), /* CTC number 0 */
                new Z80_DaisyChain(null, null, null, -1) /* end mark */};

    static z80ctc_interface ctc_intf = new z80ctc_interface(
            1, /* 1 chip */
            new int[]{0}, /* clock (filled in from the CPU 0 clock) */
            new int[]{0}, /* timer disables */
            new IntrPtr[]{ctc_interrupt}, /* interrupt handler */
            new WriteHandlerPtr[]{null}, /* ZC/TO0 callback */
            new WriteHandlerPtr[]{null}, /* ZC/TO1 callback */
            new WriteHandlerPtr[]{null} /* ZC/TO2 callback */
    );

    /**
     * ***********************************
     *
     * Generic MCR machine initialization
     *
     ************************************
     */
    public static InitMachinePtr mcr_init_machine = new InitMachinePtr() {
        public void handler() {
            /* initialize the CTC */
            ctc_intf.baseclock[0] = Machine.drv.cpu[0].cpu_clock;
            z80ctc_init(ctc_intf);

            /* reset cocktail flip */
            mcr_cocktail_flip = 0;

/*TODO*///            /* initialize the sound */
/*TODO*///            mcr_sound_init();
        }
    };
    	
	
	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Generic MCR/68k machine initialization
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static void mcr68_common_init(void)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		/* reset the 6840's */
/*TODO*///		m6840_status = 0x00;
/*TODO*///		m6840_status_read_since_int = 0x00;
/*TODO*///		m6840_msb_buffer = m6840_lsb_buffer = 0;
/*TODO*///		for (i = 0; i < 3; i++)
/*TODO*///		{
/*TODO*///			m6840_state[i].control = 0x00;
/*TODO*///			m6840_state[i].latch = 0xffff;
/*TODO*///			m6840_state[i].count = 0xffff;
/*TODO*///			m6840_state[i].timer = NULL;
/*TODO*///			m6840_state[i].period = m6840_counter_periods[i];
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* initialize the clock */
/*TODO*///		m6840_internal_counter_period = TIME_IN_HZ(Machine.drv.cpu[0].cpu_clock / 10);
/*TODO*///	
/*TODO*///		/* reset cocktail flip */
/*TODO*///		mcr_cocktail_flip = 0;
/*TODO*///	
/*TODO*///		/* initialize the sound */
/*TODO*///		pia_unconfig();
/*TODO*///		mcr_sound_init();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	public static InitMachinePtr mcr68_init_machine = new InitMachinePtr() { public void handler() 
/*TODO*///	{
/*TODO*///		/* for the most part all MCR/68k games are the same */
/*TODO*///		mcr68_common_init();
/*TODO*///		v493_callback = mcr68_493_callback;
/*TODO*///	
/*TODO*///		/* vectors are 1 and 2 */
/*TODO*///		v493_irq_vector = 1;
/*TODO*///		m6840_irq_vector = 2;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	public static InitMachinePtr zwackery_init_machine = new InitMachinePtr() { public void handler() 
/*TODO*///	{
/*TODO*///		/* for the most part all MCR/68k games are the same */
/*TODO*///		mcr68_common_init();
/*TODO*///		v493_callback = zwackery_493_callback;
/*TODO*///	
/*TODO*///		/* append our PIA state onto the existing one and reinit */
/*TODO*///		pia_config(2, PIA_STANDARD_ORDERING | PIA_16BIT_UPPER, &zwackery_pia_2_intf);
/*TODO*///		pia_config(3, PIA_STANDARD_ORDERING | PIA_16BIT_LOWER, &zwackery_pia_3_intf);
/*TODO*///		pia_config(4, PIA_STANDARD_ORDERING | PIA_16BIT_LOWER, &zwackery_pia_4_intf);
/*TODO*///		pia_reset();
/*TODO*///	
/*TODO*///		/* vectors are 5 and 6 */
/*TODO*///		v493_irq_vector = 5;
/*TODO*///		m6840_irq_vector = 6;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	
    /**
     * ***********************************
     *
     * Generic MCR interrupt handler
     *
     ************************************
     */

    public static InterruptPtr mcr_interrupt = new InterruptPtr() {
        public int handler() {
            /* once per frame, pulse the CTC line 3 */
            z80ctc_0_trg3_w.handler(0, 1);
            z80ctc_0_trg3_w.handler(0, 0);

            return ignore_interrupt.handler();
        }
    };

    /*TODO*///	
/*TODO*///	public static InterruptPtr mcr68_interrupt = new InterruptPtr() { public int handler() 
/*TODO*///	{
/*TODO*///		/* update the 6840 VBLANK clock */
/*TODO*///		if (!m6840_state[0].timer)
/*TODO*///			subtract_from_counter(0, 1);
/*TODO*///	
/*TODO*///		logerror("--- VBLANK ---\n");
/*TODO*///	
/*TODO*///		/* also set a timer to generate the 493 signal at a specific time before the next VBLANK */
/*TODO*///		/* the timing of this is crucial for Blasted and Tri-Sports, which check the timing of */
/*TODO*///		/* VBLANK and 493 using counter 2 */
/*TODO*///		timer_set(TIME_IN_HZ(30) - mcr68_timing_factor, 0, v493_callback);
/*TODO*///	
/*TODO*///		return ignore_interrupt();
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	MCR/68k interrupt central
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	static void update_mcr68_interrupts(void)
/*TODO*///	{
/*TODO*///		int newstate = 0;
/*TODO*///	
/*TODO*///		/* all interrupts go through an LS148, which gives priority to the highest */
/*TODO*///		if (v493_irq_state != 0)
/*TODO*///			newstate = v493_irq_vector;
/*TODO*///		if (m6840_irq_state != 0)
/*TODO*///			newstate = m6840_irq_vector;
/*TODO*///	
/*TODO*///		/* set the new state of the IRQ lines */
/*TODO*///		if (newstate != 0)
/*TODO*///			cpu_set_irq_line(0, newstate, ASSERT_LINE);
/*TODO*///		else
/*TODO*///			cpu_set_irq_line(0, 7, CLEAR_LINE);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	public static timer_callback mcr68_493_off_callback = new timer_callback() { public void handler(int param) 
/*TODO*///	{
/*TODO*///		v493_irq_state = 0;
/*TODO*///		update_mcr68_interrupts();
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	public static timer_callback mcr68_493_callback = new timer_callback() { public void handler(int param) 
/*TODO*///	{
/*TODO*///		v493_irq_state = 1;
/*TODO*///		update_mcr68_interrupts();
/*TODO*///		timer_set(cpu_getscanlineperiod(), 0, mcr68_493_off_callback);
/*TODO*///		logerror("--- (INT1) ---\n");
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
    /**
     * ***********************************
     *
     * Generic MCR port write handlers
     *
     ************************************
     */
    public static WriteHandlerPtr mcr_control_port_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /*
			Bit layout is as follows:
				D7 = n/c
				D6 = cocktail flip
				D5 = red LED
				D4 = green LED
				D3 = n/c
				D2 = coin meter 3
				D1 = coin meter 2
				D0 = coin meter 1
             */

            mcr_cocktail_flip = (data >> 6) & 1;
        }
    };

    	
	public static WriteHandlerPtr mcr_scroll_value_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		switch (offset)
		{
			case 0:
				/* low 8 bits of horizontal scroll */
				spyhunt_scrollx = (spyhunt_scrollx & ~0xff) | data;
				break;
	
			case 1:
				/* upper 3 bits of horizontal scroll and upper 1 bit of vertical scroll */
				spyhunt_scrollx = (spyhunt_scrollx & 0xff) | ((data & 0x07) << 8);
				spyhunt_scrolly = (spyhunt_scrolly & 0xff) | ((data & 0x80) << 1);
				break;
	
			case 2:
				/* low 8 bits of vertical scroll */
				spyhunt_scrolly = (spyhunt_scrolly & ~0xff) | data;
				break;
		}
	} };
	
	
	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	Zwackery-specific interfaces
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr zwackery_pia_2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		/* bit 7 is the watchdog */
/*TODO*///		if (!(data & 0x80)) watchdog_reset_w(offset, data);
/*TODO*///	
/*TODO*///		/* bits 5 and 6 control hflip/vflip */
/*TODO*///		/* bits 3 and 4 control coin counters? */
/*TODO*///		/* bits 0, 1 and 2 control meters? */
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr zwackery_pia_3_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		zwackery_sound_data = (data >> 4) & 0x0f;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr zwackery_ca2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		csdeluxe_data_w(offset, (data << 4) | zwackery_sound_data);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	void zwackery_pia_irq(int state)
/*TODO*///	{
/*TODO*///		v493_irq_state = state;
/*TODO*///		update_mcr68_interrupts();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	public static timer_callback zwackery_493_off_callback = new timer_callback() { public void handler(int param) 
/*TODO*///	{
/*TODO*///		pia_2_ca1_w(0, 0);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	public static timer_callback zwackery_493_callback = new timer_callback() { public void handler(int param) 
/*TODO*///	{
/*TODO*///		pia_2_ca1_w(0, 1);
/*TODO*///		timer_set(cpu_getscanlineperiod(), 0, zwackery_493_off_callback);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	M6840 timer utilities
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	INLINE void update_interrupts(void)
/*TODO*///	{
/*TODO*///		m6840_status &= ~0x80;
/*TODO*///	
/*TODO*///		if ((m6840_status & 0x01) && (m6840_state[0].control & 0x40)) m6840_status |= 0x80;
/*TODO*///		if ((m6840_status & 0x02) && (m6840_state[1].control & 0x40)) m6840_status |= 0x80;
/*TODO*///		if ((m6840_status & 0x04) && (m6840_state[2].control & 0x40)) m6840_status |= 0x80;
/*TODO*///	
/*TODO*///		m6840_irq_state = m6840_status >> 7;
/*TODO*///		update_mcr68_interrupts();
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static void subtract_from_counter(int counter, int count)
/*TODO*///	{
/*TODO*///		/* dual-byte mode */
/*TODO*///		if (m6840_state[counter].control & 0x04)
/*TODO*///		{
/*TODO*///			int lsb = m6840_state[counter].count & 0xff;
/*TODO*///			int msb = m6840_state[counter].count >> 8;
/*TODO*///	
/*TODO*///			/* count the clocks */
/*TODO*///			lsb -= count;
/*TODO*///	
/*TODO*///			/* loop while we're less than zero */
/*TODO*///			while (lsb < 0)
/*TODO*///			{
/*TODO*///				/* borrow from the MSB */
/*TODO*///				lsb += (m6840_state[counter].latch & 0xff) + 1;
/*TODO*///				msb--;
/*TODO*///	
/*TODO*///				/* if MSB goes less than zero, we've expired */
/*TODO*///				if (msb < 0)
/*TODO*///				{
/*TODO*///					m6840_status |= 1 << counter;
/*TODO*///					m6840_status_read_since_int &= ~(1 << counter);
/*TODO*///					update_interrupts();
/*TODO*///					msb = (m6840_state[counter].latch >> 8) + 1;
/*TODO*///					LOG(("** Counter %d fired\n", counter));
/*TODO*///				}
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* store the result */
/*TODO*///			m6840_state[counter].count = (msb << 8) | lsb;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* word mode */
/*TODO*///		else
/*TODO*///		{
/*TODO*///			int word = m6840_state[counter].count;
/*TODO*///	
/*TODO*///			/* count the clocks */
/*TODO*///			word -= count;
/*TODO*///	
/*TODO*///			/* loop while we're less than zero */
/*TODO*///			while (word < 0)
/*TODO*///			{
/*TODO*///				/* borrow from the MSB */
/*TODO*///				word += m6840_state[counter].latch + 1;
/*TODO*///	
/*TODO*///				/* we've expired */
/*TODO*///				m6840_status |= 1 << counter;
/*TODO*///				m6840_status_read_since_int &= ~(1 << counter);
/*TODO*///				update_interrupts();
/*TODO*///				LOG(("** Counter %d fired\n", counter));
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* store the result */
/*TODO*///			m6840_state[counter].count = word;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static void counter_fired_callback(int counter)
/*TODO*///	{
/*TODO*///		int count = counter >> 2;
/*TODO*///		counter &= 3;
/*TODO*///	
/*TODO*///		/* reset the timer */
/*TODO*///		m6840_state[counter].timer = NULL;
/*TODO*///	
/*TODO*///		/* subtract it all from the counter; this will generate an interrupt */
/*TODO*///		subtract_from_counter(counter, count);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static void reload_count(int counter)
/*TODO*///	{
/*TODO*///		double period;
/*TODO*///		int count;
/*TODO*///	
/*TODO*///		/* copy the latched value in */
/*TODO*///		m6840_state[counter].count = m6840_state[counter].latch;
/*TODO*///	
/*TODO*///		/* remove any old timers */
/*TODO*///		if (m6840_state[counter].timer)
/*TODO*///			timer_remove(m6840_state[counter].timer);
/*TODO*///		m6840_state[counter].timer = NULL;
/*TODO*///	
/*TODO*///		/* counter 0 is self-updating if clocked externally */
/*TODO*///		if (counter == 0 && !(m6840_state[counter].control & 0x02))
/*TODO*///			return;
/*TODO*///	
/*TODO*///		/* determine the clock period for this timer */
/*TODO*///		if (m6840_state[counter].control & 0x02)
/*TODO*///			period = m6840_internal_counter_period;
/*TODO*///		else
/*TODO*///			period = m6840_counter_periods[counter];
/*TODO*///	
/*TODO*///		/* determine the number of clock periods before we expire */
/*TODO*///		count = m6840_state[counter].count;
/*TODO*///		if (m6840_state[counter].control & 0x04)
/*TODO*///			count = ((count >> 8) + 1) * ((count & 0xff) + 1);
/*TODO*///		else
/*TODO*///			count = count + 1;
/*TODO*///	
/*TODO*///		/* set the timer */
/*TODO*///		m6840_state[counter].timer = timer_set(period * (double)count, (count << 2) + counter, counter_fired_callback);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static UINT16 compute_counter(int counter)
/*TODO*///	{
/*TODO*///		double period;
/*TODO*///		int remaining;
/*TODO*///	
/*TODO*///		/* if there's no timer, return the count */
/*TODO*///		if (!m6840_state[counter].timer)
/*TODO*///			return m6840_state[counter].count;
/*TODO*///	
/*TODO*///		/* determine the clock period for this timer */
/*TODO*///		if (m6840_state[counter].control & 0x02)
/*TODO*///			period = m6840_internal_counter_period;
/*TODO*///		else
/*TODO*///			period = m6840_counter_periods[counter];
/*TODO*///	
/*TODO*///		/* see how many are left */
/*TODO*///		remaining = (int)(timer_timeleft(m6840_state[counter].timer) / period);
/*TODO*///	
/*TODO*///		/* adjust the count for dual byte mode */
/*TODO*///		if (m6840_state[counter].control & 0x04)
/*TODO*///		{
/*TODO*///			int divisor = (m6840_state[counter].count & 0xff) + 1;
/*TODO*///			int msb = remaining / divisor;
/*TODO*///			int lsb = remaining % divisor;
/*TODO*///			remaining = (msb << 8) | lsb;
/*TODO*///		}
/*TODO*///	
/*TODO*///		return remaining;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/*************************************
/*TODO*///	 *
/*TODO*///	 *	M6840 timer I/O
/*TODO*///	 *
/*TODO*///	 *************************************/
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr mcr68_6840_w_common = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		/* offsets 0 and 1 are control registers */
/*TODO*///		if (offset < 2)
/*TODO*///		{
/*TODO*///			int counter = (offset == 1) ? 1 : (m6840_state[1].control & 0x01) ? 0 : 2;
/*TODO*///			UINT8 diffs = data ^ m6840_state[counter].control;
/*TODO*///	
/*TODO*///			m6840_state[counter].control = data;
/*TODO*///	
/*TODO*///			/* reset? */
/*TODO*///			if (counter == 0 && (diffs & 0x01))
/*TODO*///			{
/*TODO*///				/* holding reset down */
/*TODO*///				if ((data & 0x01) != 0)
/*TODO*///				{
/*TODO*///					for (i = 0; i < 3; i++)
/*TODO*///					{
/*TODO*///						if (m6840_state[i].timer)
/*TODO*///							timer_remove(m6840_state[i].timer);
/*TODO*///						m6840_state[i].timer = NULL;
/*TODO*///					}
/*TODO*///				}
/*TODO*///	
/*TODO*///				/* releasing reset */
/*TODO*///				else
/*TODO*///				{
/*TODO*///					for (i = 0; i < 3; i++)
/*TODO*///						reload_count(i);
/*TODO*///				}
/*TODO*///	
/*TODO*///				m6840_status = 0;
/*TODO*///				update_interrupts();
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* changing the clock source? (needed for Zwackery) */
/*TODO*///			if ((diffs & 0x02) != 0)
/*TODO*///				reload_count(counter);
/*TODO*///	
/*TODO*///			LOG(("%06X:Counter %d control = %02X\n", cpu_getpreviouspc(), counter, data));
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* offsets 2, 4, and 6 are MSB buffer registers */
/*TODO*///		else if ((offset & 1) == 0)
/*TODO*///		{
/*TODO*///			LOG(("%06X:MSB = %02X\n", cpu_getpreviouspc(), data));
/*TODO*///			m6840_msb_buffer = data;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* offsets 3, 5, and 7 are Write Timer Latch commands */
/*TODO*///		else
/*TODO*///		{
/*TODO*///			int counter = (offset - 2) / 2;
/*TODO*///			m6840_state[counter].latch = (m6840_msb_buffer << 8) | (data & 0xff);
/*TODO*///	
/*TODO*///			/* clear the interrupt */
/*TODO*///			m6840_status &= ~(1 << counter);
/*TODO*///			update_interrupts();
/*TODO*///	
/*TODO*///			/* reload the count if in an appropriate mode */
/*TODO*///			if (!(m6840_state[counter].control & 0x10))
/*TODO*///				reload_count(counter);
/*TODO*///	
/*TODO*///			LOG(("%06X:Counter %d latch = %04X\n", cpu_getpreviouspc(), counter, m6840_state[counter].latch));
/*TODO*///		}
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr mcr68_6840_r_common  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		/* offset 0 is a no-op */
/*TODO*///		if (offset == 0)
/*TODO*///			return 0;
/*TODO*///	
/*TODO*///		/* offset 1 is the status register */
/*TODO*///		else if (offset == 1)
/*TODO*///		{
/*TODO*///			LOG(("%06X:Status read = %04X\n", cpu_getpreviouspc(), m6840_status));
/*TODO*///			m6840_status_read_since_int |= m6840_status & 0x07;
/*TODO*///			return m6840_status;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* offsets 2, 4, and 6 are Read Timer Counter commands */
/*TODO*///		else if ((offset & 1) == 0)
/*TODO*///		{
/*TODO*///			int counter = (offset - 2) / 2;
/*TODO*///			int result = compute_counter(counter);
/*TODO*///	
/*TODO*///			/* clear the interrupt if the status has been read */
/*TODO*///			if (m6840_status_read_since_int & (1 << counter))
/*TODO*///				m6840_status &= ~(1 << counter);
/*TODO*///			update_interrupts();
/*TODO*///	
/*TODO*///			m6840_lsb_buffer = result & 0xff;
/*TODO*///	
/*TODO*///			LOG(("%06X:Counter %d read = %04X\n", cpu_getpreviouspc(), counter, result));
/*TODO*///			return result >> 8;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* offsets 3, 5, and 7 are LSB buffer registers */
/*TODO*///		else
/*TODO*///			return m6840_lsb_buffer;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr mcr68_6840_upper_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		if (!(data & 0xff000000))
/*TODO*///			mcr68_6840_w_common(offset / 2, (data >> 8) & 0xff);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	public static WriteHandlerPtr mcr68_6840_lower_w = new WriteHandlerPtr() {public void handler(int offset, int data)
/*TODO*///	{
/*TODO*///		if (!(data & 0x00ff0000))
/*TODO*///			mcr68_6840_w_common(offset / 2, data & 0xff);
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr mcr68_6840_upper_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		return (mcr68_6840_r_common(offset / 2) << 8) | 0x00ff;
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
/*TODO*///	public static ReadHandlerPtr mcr68_6840_lower_r  = new ReadHandlerPtr() { public int handler(int offset)
/*TODO*///	{
/*TODO*///		return mcr68_6840_r_common(offset / 2) | 0xff00;
/*TODO*///	} };
}
