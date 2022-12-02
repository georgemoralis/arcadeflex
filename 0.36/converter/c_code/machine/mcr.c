/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

  Tapper machine started by Chris Kirmse

***************************************************************************/

#include <stdio.h>

#include "driver.h"
#include "machine/z80fmly.h"
#include "machine/mcr.h"
#include "sndhrdw/mcr.h"
#include "cpu/m6800/m6800.h"
#include "cpu/m6809/m6809.h"
#include "cpu/z80/z80.h"


/* change this define to errorlog to get 6840 timer logging */
#define m6840log 0



/*************************************
 *
 *	Global variables
 *
 *************************************/

double mcr68_timing_factor;

UINT16 mcr_hiscore_start;
UINT16 mcr_hiscore_length;
const UINT8 *mcr_hiscore_init;
UINT16 mcr_hiscore_init_length;

int (*mcr_port04_r[5])(int offset);
void (*mcr_port47_w[4])(int offset, int data);
UINT8 mcr_cocktail_flip;



/*************************************
 *
 *	Statics
 *
 *************************************/

static UINT8 m6840_status;
static UINT8 m6840_status_read_since_int;
static UINT8 m6840_msb_buffer;
static UINT8 m6840_lsb_buffer;
static struct counter_state
{
	UINT8	control;
	UINT16	latch;
	UINT16	count;
	void *	timer;
	double	period;
} m6840_state[3];

/* MCR/68k interrupt states */
static UINT8 m6840_irq_state;
static UINT8 m6840_irq_vector;
static UINT8 v493_irq_state;
static UINT8 v493_irq_vector;

static void (*v493_callback)(int param);

static UINT8 zwackery_sound_data;

static const double m6840_counter_periods[3] = { 1.0 / 30.0, 1000000.0, 1.0 / (512.0 * 30.0) };
static double m6840_internal_counter_period;	/* 68000 CLK / 10 */



/*************************************
 *
 *	Function prototypes
 *
 *************************************/

static void subtract_from_counter(int counter, int count);

static void mcr68_493_callback(int param);
static void zwackery_493_callback(int param);

static void zwackery_pia_2_w(int offset, int data);
static void zwackery_pia_3_w(int offset, int data);
static void zwackery_ca2_w(int offset, int data);
static void zwackery_pia_irq(int state);

static void reload_count(int counter);



/*************************************
 *
 *	6821 PIA declarations
 *
 *************************************/

extern int zwackery_port_2_r(int offset);

static struct pia6821_interface zwackery_pia_2_intf =
{
	/*inputs : A/B,CA/B1,CA/B2 */ 0, input_port_0_r, 0, 0, 0, 0,
	/*outputs: A/B,CA/B2       */ zwackery_pia_2_w, 0, 0, 0,
	/*irqs   : A/B             */ zwackery_pia_irq, zwackery_pia_irq
};

static struct pia6821_interface zwackery_pia_3_intf =
{
	/*inputs : A/B,CA/B1,CA/B2 */ input_port_1_r, zwackery_port_2_r, 0, 0, 0, 0,
	/*outputs: A/B,CA/B2       */ zwackery_pia_3_w, 0, zwackery_ca2_w, 0,
	/*irqs   : A/B             */ 0, 0
};

static struct pia6821_interface zwackery_pia_4_intf =
{
	/*inputs : A/B,CA/B1,CA/B2 */ input_port_3_r, input_port_4_r, 0, 0, 0, 0,
	/*outputs: A/B,CA/B2       */ 0, 0, 0, 0,
	/*irqs   : A/B             */ 0, 0
};



/*************************************
 *
 *	Generic MCR CTC interface
 *
 *************************************/

static void ctc_interrupt(int state)
{
	cpu_cause_interrupt(0, Z80_VECTOR(0, state));
}


Z80_DaisyChain mcr_daisy_chain[] =
{
	{ z80ctc_reset, z80ctc_interrupt, z80ctc_reti, 0 }, /* CTC number 0 */
	{ 0, 0, 0, -1} 		/* end mark */
};


static z80ctc_interface ctc_intf =
{
	1,                  /* 1 chip */
	{ 0 },              /* clock (filled in from the CPU 0 clock) */
	{ 0 },              /* timer disables */
	{ ctc_interrupt },  /* interrupt handler */
	{ 0 },              /* ZC/TO0 callback */
	{ 0 },              /* ZC/TO1 callback */
	{ 0 }               /* ZC/TO2 callback */
};



/*************************************
 *
 *	Generic MCR machine initialization
 *
 *************************************/

void mcr_init_machine(void)
{
	/* initialize the CTC */
	ctc_intf.baseclock[0] = Machine->drv->cpu[0].cpu_clock;
	z80ctc_init(&ctc_intf);

	/* reset cocktail flip */
	mcr_cocktail_flip = 0;

	/* initialize the sound */
	mcr_sound_init();
}



/*************************************
 *
 *	Generic MCR/68k machine initialization
 *
 *************************************/

static void mcr68_common_init(void)
{
	int i;

	/* reset the 6840's */
	m6840_status = 0x00;
	m6840_status_read_since_int = 0x00;
	m6840_msb_buffer = m6840_lsb_buffer = 0;
	for (i = 0; i < 3; i++)
	{
		m6840_state[i].control = 0x00;
		m6840_state[i].latch = 0xffff;
		m6840_state[i].count = 0xffff;
		m6840_state[i].timer = NULL;
		m6840_state[i].period = m6840_counter_periods[i];
	}

	/* initialize the clock */
	m6840_internal_counter_period = TIME_IN_HZ(Machine->drv->cpu[0].cpu_clock / 10);

	/* reset cocktail flip */
	mcr_cocktail_flip = 0;

	/* initialize the sound */
	pia_unconfig();
	mcr_sound_init();
}


void mcr68_init_machine(void)
{
	/* for the most part all MCR/68k games are the same */
	mcr68_common_init();
	v493_callback = mcr68_493_callback;

	/* vectors are 1 and 2 */
	v493_irq_vector = 1;
	m6840_irq_vector = 2;
}


void zwackery_init_machine(void)
{
	/* for the most part all MCR/68k games are the same */
	mcr68_common_init();
	v493_callback = zwackery_493_callback;

	/* append our PIA state onto the existing one and reinit */
	pia_config(2, PIA_STANDARD_ORDERING | PIA_16BIT_UPPER, &zwackery_pia_2_intf);
	pia_config(3, PIA_STANDARD_ORDERING | PIA_16BIT_LOWER, &zwackery_pia_3_intf);
	pia_config(4, PIA_STANDARD_ORDERING | PIA_16BIT_LOWER, &zwackery_pia_4_intf);
	pia_reset();

	/* vectors are 5 and 6 */
	v493_irq_vector = 5;
	m6840_irq_vector = 6;
}



/*************************************
 *
 *	Generic MCR interrupt handler
 *
 *************************************/

int mcr_interrupt(void)
{
	/* once per frame, pulse the CTC line 3 */
	z80ctc_0_trg3_w(0, 1);
	z80ctc_0_trg3_w(0, 0);

	return ignore_interrupt();
}


int mcr68_interrupt(void)
{
	/* update the 6840 VBLANK clock */
	if (!m6840_state[0].timer)
		subtract_from_counter(0, 1);

	if (errorlog) fprintf(errorlog, "--- VBLANK ---\n");

	/* also set a timer to generate the 493 signal at a specific time before the next VBLANK */
	/* the timing of this is crucial for Blasted and Tri-Sports, which check the timing of */
	/* VBLANK and 493 using counter 2 */
	timer_set(TIME_IN_HZ(30) - mcr68_timing_factor, 0, v493_callback);

	return ignore_interrupt();
}



/*************************************
 *
 *	MCR/68k interrupt central
 *
 *************************************/

static void update_mcr68_interrupts(void)
{
	int newstate = 0;

	/* all interrupts go through an LS148, which gives priority to the highest */
	if (v493_irq_state)
		newstate = v493_irq_vector;
	if (m6840_irq_state)
		newstate = m6840_irq_vector;

	/* set the new state of the IRQ lines */
	if (newstate)
		cpu_set_irq_line(0, newstate, ASSERT_LINE);
	else
		cpu_set_irq_line(0, 7, CLEAR_LINE);
}


static void mcr68_493_off_callback(int param)
{
	v493_irq_state = 0;
	update_mcr68_interrupts();
}


static void mcr68_493_callback(int param)
{
	v493_irq_state = 1;
	update_mcr68_interrupts();
	timer_set(cpu_getscanlineperiod(), 0, mcr68_493_off_callback);
	if (errorlog) fprintf(errorlog, "--- (INT1) ---\n");
}



/*************************************
 *
 *	Generic MCR port write handlers
 *
 *************************************/

void mcr_dummy_w(int offset, int data)
{
}


void mcr_port_01_w(int offset, int data)
{
	mcr_cocktail_flip = (data >> 6) & 1;
}


void mcr_port_47_dispatch_w(int offset, int data)
{
	(*mcr_port47_w[offset])(offset, data);
}


void mcr_scroll_value_w(int offset, int data)
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
}



/*************************************
 *
 *	Generic MCR port read handlers
 *
 *************************************/

int mcr_port_04_dispatch_r(int offset)
{
	return (*mcr_port04_r[offset])(offset);
}



/*************************************
 *
 *	Generic MCR hiscore save/load
 *
 *************************************/

void mcr_nvram_handler(void *file,int read_or_write)
{
	unsigned char *ram = memory_region(REGION_CPU1);


	if (read_or_write)
		osd_fwrite(file, &ram[mcr_hiscore_start], mcr_hiscore_length);
	else
	{
		if (file)
			osd_fread(file, &ram[mcr_hiscore_start], mcr_hiscore_length);
		/* copy data if we failed */
		else if (mcr_hiscore_init && mcr_hiscore_init_length)
			memcpy(&ram[mcr_hiscore_start], mcr_hiscore_init, mcr_hiscore_init_length);
	}
}



/*************************************
 *
 *	Zwackery-specific interfaces
 *
 *************************************/

void zwackery_pia_2_w(int offset, int data)
{
	/* bit 7 is the watchdog */
	if (!(data & 0x80)) watchdog_reset_w(offset, data);

	/* bits 5 and 6 control hflip/vflip */
	/* bits 3 and 4 control coin counters? */
	/* bits 0, 1 and 2 control meters? */
}


void zwackery_pia_3_w(int offset, int data)
{
	zwackery_sound_data = (data >> 4) & 0x0f;
}


void zwackery_ca2_w(int offset, int data)
{
	csdeluxe_data_w(offset, (data << 4) | zwackery_sound_data);
}


void zwackery_pia_irq(int state)
{
	v493_irq_state = state;
	update_mcr68_interrupts();
}


static void zwackery_493_off_callback(int param)
{
	pia_2_ca1_w(0, 0);
}


static void zwackery_493_callback(int param)
{
	pia_2_ca1_w(0, 1);
	timer_set(cpu_getscanlineperiod(), 0, zwackery_493_off_callback);
}



/*************************************
 *
 *	M6840 timer utilities
 *
 *************************************/

INLINE void update_interrupts(void)
{
	m6840_status &= ~0x80;

	if ((m6840_status & 0x01) && (m6840_state[0].control & 0x40)) m6840_status |= 0x80;
	if ((m6840_status & 0x02) && (m6840_state[1].control & 0x40)) m6840_status |= 0x80;
	if ((m6840_status & 0x04) && (m6840_state[2].control & 0x40)) m6840_status |= 0x80;

	m6840_irq_state = m6840_status >> 7;
	update_mcr68_interrupts();
}


static void subtract_from_counter(int counter, int count)
{
	/* dual-byte mode */
	if (m6840_state[counter].control & 0x04)
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
				if (m6840log) fprintf(m6840log, "** Counter %d fired\n", counter);
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
			if (m6840log) fprintf(m6840log, "** Counter %d fired\n", counter);
		}

		/* store the result */
		m6840_state[counter].count = word;
	}
}


static void counter_fired_callback(int counter)
{
	int count = counter >> 2;
	counter &= 3;

	/* reset the timer */
	m6840_state[counter].timer = NULL;

	/* subtract it all from the counter; this will generate an interrupt */
	subtract_from_counter(counter, count);
}


static void reload_count(int counter)
{
	double period;
	int count;

	/* copy the latched value in */
	m6840_state[counter].count = m6840_state[counter].latch;

	/* remove any old timers */
	if (m6840_state[counter].timer)
		timer_remove(m6840_state[counter].timer);
	m6840_state[counter].timer = NULL;

	/* counter 0 is self-updating if clocked externally */
	if (counter == 0 && !(m6840_state[counter].control & 0x02))
		return;

	/* determine the clock period for this timer */
	if (m6840_state[counter].control & 0x02)
		period = m6840_internal_counter_period;
	else
		period = m6840_counter_periods[counter];

	/* determine the number of clock periods before we expire */
	count = m6840_state[counter].count;
	if (m6840_state[counter].control & 0x04)
		count = ((count >> 8) + 1) * ((count & 0xff) + 1);
	else
		count = count + 1;

	/* set the timer */
	m6840_state[counter].timer = timer_set(period * (double)count, (count << 2) + counter, counter_fired_callback);
}


static UINT16 compute_counter(int counter)
{
	double period;
	int remaining;

	/* if there's no timer, return the count */
	if (!m6840_state[counter].timer)
		return m6840_state[counter].count;

	/* determine the clock period for this timer */
	if (m6840_state[counter].control & 0x02)
		period = m6840_internal_counter_period;
	else
		period = m6840_counter_periods[counter];

	/* see how many are left */
	remaining = (int)(timer_timeleft(m6840_state[counter].timer) / period);

	/* adjust the count for dual byte mode */
	if (m6840_state[counter].control & 0x04)
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

static void mcr68_6840_w_common(int offset, int data)
{
	int i;

	/* offsets 0 and 1 are control registers */
	if (offset < 2)
	{
		int counter = (offset == 1) ? 1 : (m6840_state[1].control & 0x01) ? 0 : 2;
		UINT8 diffs = data ^ m6840_state[counter].control;

		m6840_state[counter].control = data;

		/* reset? */
		if (counter == 0 && (diffs & 0x01))
		{
			/* holding reset down */
			if (data & 0x01)
			{
				for (i = 0; i < 3; i++)
				{
					if (m6840_state[i].timer)
						timer_remove(m6840_state[i].timer);
					m6840_state[i].timer = NULL;
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
		if (diffs & 0x02)
			reload_count(counter);

		if (m6840log) fprintf(m6840log, "%06X:Counter %d control = %02X\n", cpu_getpreviouspc(), counter, data);
	}

	/* offsets 2, 4, and 6 are MSB buffer registers */
	else if ((offset & 1) == 0)
	{
		if (m6840log) fprintf(m6840log, "%06X:MSB = %02X\n", cpu_getpreviouspc(), data);
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
		if (!(m6840_state[counter].control & 0x10))
			reload_count(counter);

		if (m6840log) fprintf(m6840log, "%06X:Counter %d latch = %04X\n", cpu_getpreviouspc(), counter, m6840_state[counter].latch);
	}
}


static int mcr68_6840_r_common(int offset)
{
	/* offset 0 is a no-op */
	if (offset == 0)
		return 0;

	/* offset 1 is the status register */
	else if (offset == 1)
	{
		if (m6840log) fprintf(m6840log, "%06X:Status read = %04X\n", cpu_getpreviouspc(), m6840_status);
		m6840_status_read_since_int |= m6840_status & 0x07;
		return m6840_status;
	}

	/* offsets 2, 4, and 6 are Read Timer Counter commands */
	else if ((offset & 1) == 0)
	{
		int counter = (offset - 2) / 2;
		int result = compute_counter(counter);

		/* clear the interrupt if the status has been read */
		if (m6840_status_read_since_int & (1 << counter))
			m6840_status &= ~(1 << counter);
		update_interrupts();

		m6840_lsb_buffer = result & 0xff;

		if (m6840log) fprintf(m6840log, "%06X:Counter %d read = %04X\n", cpu_getpreviouspc(), counter, result);
		return result >> 8;
	}

	/* offsets 3, 5, and 7 are LSB buffer registers */
	else
		return m6840_lsb_buffer;
}


void mcr68_6840_upper_w(int offset, int data)
{
	if (!(data & 0xff000000))
		mcr68_6840_w_common(offset / 2, (data >> 8) & 0xff);
}


void mcr68_6840_lower_w(int offset, int data)
{
	if (!(data & 0x00ff0000))
		mcr68_6840_w_common(offset / 2, data & 0xff);
}


int mcr68_6840_upper_r(int offset)
{
	return (mcr68_6840_r_common(offset / 2) << 8) | 0x00ff;
}


int mcr68_6840_lower_r(int offset)
{
	return mcr68_6840_r_common(offset / 2) | 0xff00;
}
