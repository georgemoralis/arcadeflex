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

import arcadeflex.v036.generic.funcPtr.TimerCallbackHandlerPtr;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.timer.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.*;
import static arcadeflex.v036.mame.timerH.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v058.machine.z80fmly.*;
import static gr.codebb.arcadeflex.v058.machine.z80fmlyH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.mcr3.*;
import static gr.codebb.arcadeflex.v036.machine._6821pia.*;
import static gr.codebb.arcadeflex.v036.machine._6812piaH.*;
import static gr.codebb.arcadeflex.v037b7.drivers.mcr68.zwackery_port_2_r;
import static gr.codebb.arcadeflex.v037b7.sndhrdw.mcr.*;

public class mcr {

    	
    /*************************************
     *
     *	Global variables
     *
     *************************************/
	
    public static double mcr68_timing_factor;

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
	static int m6840_status;
	static int m6840_status_read_since_int;
	static int m6840_msb_buffer;
	static int m6840_lsb_buffer;
	static class counter_state
	{
		public int	control;
		public int	latch;
		public int	count;
		public timer_entry	timer;
		public double	period;
	};
        static counter_state[] m6840_state = new counter_state[3];
        
        static {
            for (int _i =0 ; _i<3 ; _i++)
                m6840_state[_i] = new counter_state();
        }

	/* MCR/68k interrupt states */
	public static int m6840_irq_state;
	public static int m6840_irq_vector;
	public static int v493_irq_state;
	public static int v493_irq_vector;

	public static TimerCallbackHandlerPtr v493_callback;

	static int zwackery_sound_data;

	static double m6840_counter_periods[] = { 1.0 / 30.0, 1000000.0, 1.0 / (512.0 * 30.0) };
	static double m6840_internal_counter_period;	/* 68000 CLK / 10 */
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
    
        /*************************************
	 *
	 *	Zwackery-specific interfaces
	 *
	 *************************************/
	
	public static WriteHandlerPtr zwackery_pia_2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* bit 7 is the watchdog */
		if ((data & 0x80)==0) watchdog_reset_w.handler(offset, data);
	
		/* bits 5 and 6 control hflip/vflip */
		/* bits 3 and 4 control coin counters? */
		/* bits 0, 1 and 2 control meters? */
	} };
        
        static irqfuncPtr zwackery_pia_irq = new irqfuncPtr() {
            @Override
            public void handler(int state) {
                v493_irq_state = state;
		update_mcr68_interrupts();
            }
        };
        
        public static WriteHandlerPtr zwackery_pia_3_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		zwackery_sound_data = (data >> 4) & 0x0f;
	} };
	
	
	public static WriteHandlerPtr zwackery_ca2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		csdeluxe_data_w.handler(offset, (data << 4) | zwackery_sound_data);
	} };

    	
	/*************************************
	 *
	 *	6821 PIA declarations
	 *
	 *************************************/
	 
	public static pia6821_interface zwackery_pia_2_intf = new pia6821_interface
	(
		/*inputs : A/B,CA/B1,CA/B2 */ null, input_port_0_r, null, null, null, null,
		/*outputs: A/B,CA/B2       */ zwackery_pia_2_w, null, null, null,
		/*irqs   : A/B             */ zwackery_pia_irq, zwackery_pia_irq
	);
	
	public static pia6821_interface zwackery_pia_3_intf = new pia6821_interface
	(
		/*inputs : A/B,CA/B1,CA/B2 */ input_port_1_r, zwackery_port_2_r, null, null, null, null,
		/*outputs: A/B,CA/B2       */ zwackery_pia_3_w, null, zwackery_ca2_w, null,
		/*irqs   : A/B             */ null, null
	);
	
	public static pia6821_interface zwackery_pia_4_intf = new pia6821_interface
	(
		/*inputs : A/B,CA/B1,CA/B2 */ input_port_3_r, input_port_4_r, null, null, null, null,
		/*outputs: A/B,CA/B2       */ null, null, null, null,
		/*irqs   : A/B             */ null, null
	);
	
	
	
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
            System.out.println("mcr_init_machine");
            /* initialize the CTC */
            ctc_intf.baseclock[0] = Machine.drv.cpu[0].cpu_clock;
            z80ctc_init(ctc_intf);

            /* reset cocktail flip */
            mcr_cocktail_flip = 0;

            /* initialize the sound */
            mcr_sound_init();
        }
    };
    	
	
	
	/*************************************
	 *
	 *	Generic MCR/68k machine initialization
	 *
	 *************************************/
	
	public static void mcr68_common_init()
	{
		int i;
	
		/* reset the 6840's */
		m6840_status = 0x00;
		m6840_status_read_since_int = 0x00;
		m6840_msb_buffer = m6840_lsb_buffer = 0;
		for (i = 0; i < 3; i++)
		{
                        m6840_state[i]=new counter_state();
			m6840_state[i].control = 0x00;
			m6840_state[i].latch = 0xffff;
			m6840_state[i].count = 0xffff;
			m6840_state[i].timer = null;
			m6840_state[i].period = m6840_counter_periods[i];
		}
	
		/* initialize the clock */
		m6840_internal_counter_period = TIME_IN_HZ(Machine.drv.cpu[0].cpu_clock / 10);
	
		/* reset cocktail flip */
		mcr_cocktail_flip = 0;
	
		/* initialize the sound */
		pia_unconfig();
		mcr_sound_init();
	}
	
	
	
       
	
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

    	
	public static InterruptPtr mcr68_interrupt = new InterruptPtr() { public int handler() 
	{
		/* update the 6840 VBLANK clock */
		if (m6840_state[0].timer==null)
			subtract_from_counter(0, 1);
	
		logerror("--- VBLANK ---\n");
	
		/* also set a timer to generate the 493 signal at a specific time before the next VBLANK */
		/* the timing of this is crucial for Blasted and Tri-Sports, which check the timing of */
		/* VBLANK and 493 using counter 2 */
		timer_set(TIME_IN_HZ(30) - mcr68_timing_factor, 0, v493_callback);
	
		return ignore_interrupt.handler();
	} };
	
	
	
	/*************************************
	 *
	 *	MCR/68k interrupt central
	 *
	 *************************************/
	
	static void update_mcr68_interrupts()
	{
		int newstate = 0;
	
		/* all interrupts go through an LS148, which gives priority to the highest */
		if (v493_irq_state != 0)
			newstate = v493_irq_vector;
		if (m6840_irq_state != 0)
			newstate = m6840_irq_vector;
	
		/* set the new state of the IRQ lines */
		if (newstate != 0)
			cpu_set_irq_line(0, newstate, ASSERT_LINE);
		else
			cpu_set_irq_line(0, 7, CLEAR_LINE);
	}
	
	
	public static TimerCallbackHandlerPtr mcr68_493_off_callback = new TimerCallbackHandlerPtr() { public void handler(int param) 
	{
		v493_irq_state = 0;
		update_mcr68_interrupts();
	} };
	
	
	public static TimerCallbackHandlerPtr mcr68_493_callback = new TimerCallbackHandlerPtr() { public void handler(int param) 
	{
		v493_irq_state = 1;
		update_mcr68_interrupts();
		timer_set(cpu_getscanlineperiod(), 0, mcr68_493_off_callback);
		logerror("--- (INT1) ---\n");
	} };
	
	
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
				spyhunt_scrollx = (short) ((spyhunt_scrollx & ~0xff) | data);
				break;
	
			case 1:
				/* upper 3 bits of horizontal scroll and upper 1 bit of vertical scroll */
				spyhunt_scrollx = (short) ((spyhunt_scrollx & 0xff) | ((data & 0x07) << 8));
				spyhunt_scrolly = (short) ((spyhunt_scrolly & 0xff) | ((data & 0x80) << 1));
				break;
	
			case 2:
				/* low 8 bits of vertical scroll */
				spyhunt_scrolly = (short) ((spyhunt_scrolly & ~0xff) | data);
				break;
		}
	} };
	
	
	
	
	
	
	
	
	
	
        
	
	
	public static TimerCallbackHandlerPtr zwackery_493_off_callback = new TimerCallbackHandlerPtr() { public void handler(int param) 
	{
		pia_2_ca1_w.handler(0, 0);
	} };
	
	
	public static TimerCallbackHandlerPtr zwackery_493_callback = new TimerCallbackHandlerPtr() { public void handler(int param) 
	{
		pia_2_ca1_w.handler(0, 1);
		timer_set(cpu_getscanlineperiod(), 0, zwackery_493_off_callback);
	} };
	
	
	
	/*************************************
	 *
	 *	M6840 timer utilities
	 *
	 *************************************/
	
	public static void update_interrupts()
	{
		m6840_status &= ~0x80;
	
		if ((m6840_status & 0x01)!=0 && (m6840_state[0].control & 0x40)!=0) m6840_status |= 0x80;
		if ((m6840_status & 0x02)!=0 && (m6840_state[1].control & 0x40)!=0) m6840_status |= 0x80;
		if ((m6840_status & 0x04)!=0 && (m6840_state[2].control & 0x40)!=0) m6840_status |= 0x80;
	
		m6840_irq_state = m6840_status >> 7;
		update_mcr68_interrupts();
	}
	
	
	static void subtract_from_counter(int counter, int count)
	{
		/* dual-byte mode */
		if ((m6840_state[counter].control & 0x04) != 0)
		{
			int lsb = m6840_state[counter].count & 0xff;
			int msb = m6840_state[counter].count >> 8;
	
			/* count the clocks */
			lsb -= count;
	
			/* loop while we're less than zero */
			while (lsb < 0)
			{
				/* borrow from the MSB */
				lsb += (m6840_state[counter].latch & 0xff) + 1;
				msb--;
	
				/* if MSB goes less than zero, we've expired */
				if (msb < 0)
				{
					m6840_status |= 1 << counter;
					m6840_status_read_since_int &= ~(1 << counter);
					update_interrupts();
					msb = (m6840_state[counter].latch >> 8) + 1;
/*TODO*///					LOG(("** Counter %d fired\n", counter));
				}
			}
	
			/* store the result */
			m6840_state[counter].count = (msb << 8) | lsb;
		}
	
		/* word mode */
		else
		{
			int word = m6840_state[counter].count;
	
			/* count the clocks */
			word -= count;
	
			/* loop while we're less than zero */
			while (word < 0)
			{
				/* borrow from the MSB */
				word += m6840_state[counter].latch + 1;
	
				/* we've expired */
				m6840_status |= 1 << counter;
				m6840_status_read_since_int &= ~(1 << counter);
				update_interrupts();
/*TODO*///				LOG(("** Counter %d fired\n", counter));
			}
	
			/* store the result */
			m6840_state[counter].count = word;
		}
	}
	
	
	static TimerCallbackHandlerPtr counter_fired_callback = new TimerCallbackHandlerPtr() {
            @Override
            public void handler(int counter) {
                int count = counter >> 2;
		counter &= 3;
	
		/* reset the timer */
		m6840_state[counter].timer = null;
	
		/* subtract it all from the counter; this will generate an interrupt */
		subtract_from_counter(counter, count);
            }
        };
        	
	static void reload_count(int counter)
	{
		double period;
		int count;
	
		/* copy the latched value in */
		m6840_state[counter].count = m6840_state[counter].latch;
	
		/* remove any old timers */
		if ((m6840_state[counter].timer) != null)
			timer_remove(m6840_state[counter].timer);
		m6840_state[counter].timer = null;
	
		/* counter 0 is self-updating if clocked externally */
		if (counter == 0 && (m6840_state[counter].control & 0x02)==0)
			return;
	
		/* determine the clock period for this timer */
		if ((m6840_state[counter].control & 0x02) != 0)
			period = m6840_internal_counter_period;
		else
			period = m6840_counter_periods[counter];
	
		/* determine the number of clock periods before we expire */
		count = m6840_state[counter].count;
		if ((m6840_state[counter].control & 0x04) != 0)
			count = ((count >> 8) + 1) * ((count & 0xff) + 1);
		else
			count = count + 1;
	
		/* set the timer */
		m6840_state[counter].timer = timer_set(period * (double)count, (count << 2) + counter, counter_fired_callback);
	}
	
	
	static int compute_counter(int counter)
	{
		double period;
		int remaining;
	
		/* if there's no timer, return the count */
		if (m6840_state[counter].timer==null)
			return m6840_state[counter].count;
	
		/* determine the clock period for this timer */
		if ((m6840_state[counter].control & 0x02) != 0)
			period = m6840_internal_counter_period;
		else
			period = m6840_counter_periods[counter];
	
		/* see how many are left */
		remaining = (int)(timer_timeleft(m6840_state[counter].timer) / period);
	
		/* adjust the count for dual byte mode */
		if ((m6840_state[counter].control & 0x04) != 0)
		{
			int divisor = (m6840_state[counter].count & 0xff) + 1;
			int msb = remaining / divisor;
			int lsb = remaining % divisor;
			remaining = (msb << 8) | lsb;
		}
	
		return remaining;
	}
	
	
	
	/*************************************
	 *
	 *	M6840 timer I/O
	 *
	 *************************************/
	
	public static WriteHandlerPtr mcr68_6840_w_common = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int i;
	
		/* offsets 0 and 1 are control registers */
		if (offset < 2)
		{
			int counter = (offset == 1) ? 1 : (m6840_state[1].control & 0x01)!=0 ? 0 : 2;
			int diffs = data ^ m6840_state[counter].control;
	
			m6840_state[counter].control = data;
	
			/* reset? */
			if (counter == 0 && (diffs & 0x01)!=0)
			{
				/* holding reset down */
				if ((data & 0x01) != 0)
				{
					for (i = 0; i < 3; i++)
					{
						if (m6840_state[i].timer != null)
							timer_remove(m6840_state[i].timer);
						m6840_state[i].timer = null;
					}
				}
	
				/* releasing reset */
				else
				{
					for (i = 0; i < 3; i++)
						reload_count(i);
				}
	
				m6840_status = 0;
				update_interrupts();
			}
	
			/* changing the clock source? (needed for Zwackery) */
			if ((diffs & 0x02) != 0)
				reload_count(counter);
	
/*TODO*///			LOG(("%06X:Counter %d control = %02X\n", cpu_getpreviouspc(), counter, data));
		}
	
		/* offsets 2, 4, and 6 are MSB buffer registers */
		else if ((offset & 1) == 0)
		{
/*TODO*///			LOG(("%06X:MSB = %02X\n", cpu_getpreviouspc(), data));
			m6840_msb_buffer = data;
		}
	
		/* offsets 3, 5, and 7 are Write Timer Latch commands */
		else
		{
			int counter = (offset - 2) / 2;
			m6840_state[counter].latch = (m6840_msb_buffer << 8) | (data & 0xff);
	
			/* clear the interrupt */
			m6840_status &= ~(1 << counter);
			update_interrupts();
	
			/* reload the count if in an appropriate mode */
			if ((m6840_state[counter].control & 0x10)==0)
				reload_count(counter);
	
/*TODO*///			LOG(("%06X:Counter %d latch = %04X\n", cpu_getpreviouspc(), counter, m6840_state[counter].latch));
		}
	} };
	
	
	public static ReadHandlerPtr mcr68_6840_r_common  = new ReadHandlerPtr() { public int handler(int offset)
	{
		/* offset 0 is a no-op */
		if (offset == 0)
			return 0;
	
		/* offset 1 is the status register */
		else if (offset == 1)
		{
/*TODO*///			LOG(("%06X:Status read = %04X\n", cpu_getpreviouspc(), m6840_status));
			m6840_status_read_since_int |= m6840_status & 0x07;
			return m6840_status;
		}
	
		/* offsets 2, 4, and 6 are Read Timer Counter commands */
		else if ((offset & 1) == 0)
		{
			int counter = (offset - 2) / 2;
			int result = compute_counter(counter);
	
			/* clear the interrupt if the status has been read */
			if ((m6840_status_read_since_int & (1 << counter)) != 0)
				m6840_status &= ~(1 << counter);
			update_interrupts();
	
			m6840_lsb_buffer = result & 0xff;
	
/*TODO*///			LOG(("%06X:Counter %d read = %04X\n", cpu_getpreviouspc(), counter, result));
			return result >> 8;
		}
	
		/* offsets 3, 5, and 7 are LSB buffer registers */
		else
			return m6840_lsb_buffer;
	} };
	
	
	public static WriteHandlerPtr mcr68_6840_upper_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0xff000000)==0)
			mcr68_6840_w_common.handler(offset / 2, (data >> 8) & 0xff);
	} };
	
	
	public static WriteHandlerPtr mcr68_6840_lower_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0x00ff0000)==0)
			mcr68_6840_w_common.handler(offset / 2, data & 0xff);
	} };
	
	
	public static ReadHandlerPtr mcr68_6840_upper_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return (mcr68_6840_r_common.handler(offset / 2) << 8) | 0x00ff;
	} };
	
	
	public static ReadHandlerPtr mcr68_6840_lower_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return mcr68_6840_r_common.handler(offset / 2) | 0xff00;
	} };
}
