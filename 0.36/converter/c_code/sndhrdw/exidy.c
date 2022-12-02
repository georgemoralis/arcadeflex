#include "driver.h"
#include "cpu/m6502/m6502.h"
#include "machine/6821pia.h"
#include "sound/hc55516.h"

static void *timer;
#define BASE_TIME 1/.894886
#define BASE_FREQ (1789773 * 2)
#define SH6840_FREQ 894886
#define CVSD_CLOCK_FREQ (1000000.0 / 34.0)
#define VOLUME 60

int exidy_sample_channels[6];
unsigned int exidy_sh8253_count[3];     /* 8253 Counter */
int exidy_sh8253_clstate[3];            /* which byte to load */
int riot_divider;
int riot_state;

#define RIOT_IDLE 0
#define RIOT_COUNTUP 1
#define RIOT_COUNTDOWN 2

int mtrap_voice;
int mtrap_count;
int mtrap_vocdata;

static signed char exidy_waveform1[16] =
{
	/* square-wave */
	0x00, 0x7F, 0x00, 0x7F, 0x00, 0x7F, 0x00, 0x7F,
	0x00, 0x7F, 0x00, 0x7F, 0x00, 0x7F, 0x00, 0x7F
};

int exidy_shdata_latch = 0xFF;
int exidy_mhdata_latch = 0xFF;

/* 6532 variables */
static int irq_flag = 0;   /* 6532 interrupt flag register */
static int irq_enable = 0;
static int PA7_irq = 0;  /* IRQ-on-write flag (sound CPU) */

/* 6840 variables */
static int sh6840_CR1,sh6840_CR2,sh6840_CR3;
static int sh6840_MSB;
static unsigned int sh6840_timer[3];
static int exidy_sfxvol[3];
static int exidy_sfxctrl;

/***************************************************************************
	PIA Interface
***************************************************************************/

static void exidy_irq (int state);

/* PIA 0 */
static struct pia6821_interface pia_0_intf =
{
	/*inputs : A/B,CA/B1,CA/B2 */ 0, 0, 0, 0, 0, 0,
	/*outputs: A/B,CA/B2       */ pia_1_portb_w, pia_1_porta_w, pia_1_cb1_w, pia_1_ca1_w,
	/*irqs   : A/B             */ 0, 0
};

/* PIA 1 */
static struct pia6821_interface pia_1_intf =
{
	/*inputs : A/B,CA/B1,CA/B2 */ 0, 0, 0, 0, 0, 0,
	/*outputs: A/B,CA/B2       */ pia_0_portb_w, pia_0_porta_w, pia_0_cb1_w, pia_0_ca1_w,
	/*irqs   : A/B             */ 0, exidy_irq
};

/**************************************************************************
    Start/Stop Sound
***************************************************************************/

int exidy_sh_start(const struct MachineSound *msound)
{
	/* Init 8253 */
	exidy_sh8253_clstate[0]=0;
	exidy_sh8253_clstate[1]=0;
	exidy_sh8253_clstate[2]=0;
	exidy_sh8253_count[0]=0;
	exidy_sh8253_count[1]=0;
	exidy_sh8253_count[2]=0;

	exidy_sample_channels[0] = mixer_allocate_channel(25);
	exidy_sample_channels[1] = mixer_allocate_channel(25);
	exidy_sample_channels[2] = mixer_allocate_channel(25);
    mixer_set_volume(exidy_sample_channels[0],0);
    mixer_play_sample(exidy_sample_channels[0],(signed char*)exidy_waveform1,16,1000,1);
    mixer_set_volume(exidy_sample_channels[1],0);
    mixer_play_sample(exidy_sample_channels[1],(signed char*)exidy_waveform1,16,1000,1);
    mixer_set_volume(exidy_sample_channels[2],0);
    mixer_play_sample(exidy_sample_channels[2],(signed char*)exidy_waveform1,16,1000,1);

	/* Init PIA */
	pia_config(0, PIA_STANDARD_ORDERING, &pia_0_intf);
	pia_config(1, PIA_STANDARD_ORDERING, &pia_1_intf);
	pia_reset();

	/* Init 6532 */
    timer=0;
    riot_divider = 1;
    riot_state = RIOT_IDLE;

	/* Init 6840 */
	sh6840_CR1 = sh6840_CR2 = sh6840_CR3 = 0;
	sh6840_MSB = 0;
	sh6840_timer[0] = sh6840_timer[1] = sh6840_timer[2] = 0;
	exidy_sfxvol[0] = exidy_sfxvol[1] = exidy_sfxvol[2] = 0;
    exidy_sample_channels[3] = mixer_allocate_channel(25);
	exidy_sample_channels[4] = mixer_allocate_channel(25);
	exidy_sample_channels[5] = mixer_allocate_channel(25);
    mixer_set_volume(exidy_sample_channels[3],0);
    mixer_play_sample(exidy_sample_channels[3],(signed char*)exidy_waveform1,16,1000,1);
    mixer_set_volume(exidy_sample_channels[4],0);
    mixer_play_sample(exidy_sample_channels[4],(signed char*)exidy_waveform1,16,1000,1);
    mixer_set_volume(exidy_sample_channels[5],0);
    mixer_play_sample(exidy_sample_channels[5],(signed char*)exidy_waveform1,16,1000,1);

    /* Setup Mousetrap Voice */
	mtrap_voice = 0xff;
    mtrap_count = 0;
	return 0;
}

void exidy_sh_stop(void)
{
	mixer_stop_sample(exidy_sample_channels[0]);
	mixer_stop_sample(exidy_sample_channels[1]);
	mixer_stop_sample(exidy_sample_channels[2]);
}

/*
 *  PIA callback to generate the interrupt to the main CPU
 */

static void exidy_irq (int state)
{
    cpu_set_irq_line (1, 0, state ? ASSERT_LINE : CLEAR_LINE);
}

/**************************************************************************
    6532 RIOT
***************************************************************************/

static void riot_interrupt(int parm)
{
    if (riot_state == RIOT_COUNTUP) {
        irq_flag |= 0x80; /* set timer interrupt flag */
        if (irq_enable) cpu_cause_interrupt (1, M6502_INT_IRQ);
        riot_state = RIOT_COUNTDOWN;
        timer = timer_set (TIME_IN_USEC((1*BASE_TIME)*0xFF), 0, riot_interrupt);
    }
    else {
        timer=0;
        riot_state = RIOT_IDLE;
    }
}


void exidy_shriot_w(int offset,int data)
{
   offset &= 0x7F;
   switch (offset)
   {
   case 0:
        cpu_set_reset_line(2, (data & 0x10) ? CLEAR_LINE : ASSERT_LINE);
		mtrap_voice = data;
		return;
   	case 7: /* 0x87 - Enable Interrupt on PA7 Transitions */
		PA7_irq = data;
		return;
	case 0x14:
	case 0x1c:
        irq_enable=offset & 0x08;
	    riot_divider = 1;
        if (timer) timer_remove(timer);
        timer = timer_set (TIME_IN_USEC((1*BASE_TIME)*data), 0, riot_interrupt);
        riot_state = RIOT_COUNTUP;
        return;
	case 0x15:
	case 0x1d:
        irq_enable=offset & 0x08;
	    riot_divider = 8;
        if (timer) timer_remove(timer);
        timer = timer_set (TIME_IN_USEC((8*BASE_TIME)*data), 0, riot_interrupt);
        riot_state = RIOT_COUNTUP;
        return;
    case 0x16:
    case 0x1e:
        irq_enable=offset & 0x08;
	    riot_divider = 64;
        if (timer) timer_remove(timer);
        timer = timer_set (TIME_IN_USEC((64*BASE_TIME)*data), 0, riot_interrupt);
        riot_state = RIOT_COUNTUP;
		return;
	case 0x17:
    case 0x1f:
        irq_enable=offset & 0x08;
	    riot_divider = 1024;
        if (timer) timer_remove(timer);
        timer = timer_set (TIME_IN_USEC((1024*BASE_TIME)*data), 0, riot_interrupt);
        riot_state = RIOT_COUNTUP;
		return;
	default:
	    if (errorlog) fprintf(errorlog,"Undeclared RIOT write: %x=%x\n",offset,data);
	    return;
	}
	return; /* will never execute this */
}


int exidy_shriot_r(int offset)
{
	static int temp;

	offset &= 0x07;
	switch (offset)
	{
    case 0x02:
          return (mtrap_voice & 0x80) >> 7;
	case 0x05: /* 0x85 - Read Interrupt Flag Register */
	case 0x07:
		temp = irq_flag;
		irq_flag = 0;   /* Clear int flags */
		return temp;
	case 0x04:
	case 0x06:
		irq_flag = 0;
		if (riot_state == RIOT_COUNTUP) {
	        return timer_timeelapsed(timer)/(TIME_IN_USEC((riot_divider*BASE_TIME)));
		}
		else {
			return timer_timeleft(timer)/(TIME_IN_USEC((riot_divider*BASE_TIME)));
		}
	default:
	    if (errorlog) fprintf(errorlog,"Undeclared RIOT read: %x  PC:%x\n",offset,cpu_get_pc());
  	    return 0xff;
	}
	return 0;
}

/**************************************************************************
    8253 Timer
***************************************************************************/

void exidy_sh8253_w(int offset,int data)
{
	int i,c;
	long f;


	i = offset & 0x03;
	if (i == 0x03) {
		c = (data & 0xc0) >> 6;
		if (exidy_sh8253_count[c])
			f = BASE_FREQ / exidy_sh8253_count[c];
		else
            f = 1;

		if ((data & 0x0E) == 0) {
            mixer_set_sample_frequency(exidy_sample_channels[c],f);
            mixer_set_volume(exidy_sample_channels[c],0);
		}
		else {
            mixer_set_sample_frequency(exidy_sample_channels[c],f);
            mixer_set_volume(exidy_sample_channels[c],VOLUME);
		}
	}

	if (i < 0x03)
	{
		if (!exidy_sh8253_clstate[i])
		{
			exidy_sh8253_clstate[i]=1;
			exidy_sh8253_count[i] &= 0xFF00;
			exidy_sh8253_count[i] |= (data & 0xFF);
		}
		else
		{
			exidy_sh8253_clstate[i]=0;
			exidy_sh8253_count[i] &= 0x00FF;
			exidy_sh8253_count[i] |= ((data & 0xFF) << 8);
            if (!exidy_sh8253_count[i])
                f = 1;
            else
                f = BASE_FREQ / exidy_sh8253_count[i];
            mixer_set_sample_frequency(exidy_sample_channels[i],f);
		}
	}


}

int exidy_sh8253_r(int offset)
{
    if (errorlog) fprintf(errorlog,"8253(R): %x\n",offset);
	return 0;
}

/**************************************************************************
    6840 Timer
***************************************************************************/

int exidy_sh6840_r(int offset) {
    if (errorlog) fprintf(errorlog,"6840R %x\n",offset);
    return 0;
}

void exidy_sh6840_w(int offset,int data) {
    	offset &= 0x07;
	switch (offset) {
		case 0:
			if (sh6840_CR2 & 0x01) {
				sh6840_CR1 = data;
                if ((data & 0x38) == 0) mixer_set_volume(exidy_sample_channels[3],0);
			}
			else {
				sh6840_CR3 = data;
                if ((data & 0x38) == 0) mixer_set_volume(exidy_sample_channels[5],0);
			}
			break;

		case 1:
			sh6840_CR2 = data;
            if ((data & 0x38) == 0) mixer_set_volume(exidy_sample_channels[4],0);
			break;
		case 2:
		case 4:
		case 6:
			sh6840_MSB = data;
			break;
		case 3:
			sh6840_timer[0] = (sh6840_MSB << 8) | (data & 0xFF);
            if (sh6840_timer[0] != 0 && !(exidy_sfxctrl & 0x02))
            {
                mixer_set_sample_frequency(exidy_sample_channels[3],SH6840_FREQ/sh6840_timer[0]);
                mixer_set_volume(exidy_sample_channels[3],exidy_sfxvol[0]*VOLUME/7);
            }
            else
                mixer_set_volume(exidy_sample_channels[3],0);
			break;
		case 5:
			sh6840_timer[1] = (sh6840_MSB << 8) | (data & 0xFF);
            if (sh6840_timer[1] != 0)
            {
                mixer_set_sample_frequency(exidy_sample_channels[4],SH6840_FREQ/sh6840_timer[1]);
                mixer_set_volume(exidy_sample_channels[4],exidy_sfxvol[1]*VOLUME/7);
            }
            else
                mixer_set_volume(exidy_sample_channels[4],0);
			break;
		case 7:
			sh6840_timer[2] = (sh6840_MSB << 8) | (data & 0xFF);
            if (sh6840_timer[2] != 0)
            {
                mixer_set_sample_frequency(exidy_sample_channels[5],SH6840_FREQ/sh6840_timer[2]);
                mixer_set_volume(exidy_sample_channels[5],exidy_sfxvol[2]*VOLUME/7);
            }
            else
                mixer_set_volume(exidy_sample_channels[5],0);
			break;
	}
}

/**************************************************************************
    Special Sound FX Control
***************************************************************************/

void exidy_sfxctrl_w(int offset,int data) {
	switch (offset & 0x03) {
	case 0:
		exidy_sfxctrl = data;
        if (!(data & 0x02)) mixer_set_volume(exidy_sample_channels[3],0);
		break;
	case 1:
	case 2:
	case 3:
		exidy_sfxvol[offset - 1] = (data & 0x07);
		break;
	}
}

/**************************************************************************
    Mousetrap Digital Sound
***************************************************************************/


void mtrap_voiceio_w(int offset,int data) {
    if (!(offset & 0x10)) {
    	hc55516_digit_clock_clear_w(0,data);
    	hc55516_clock_set_w(0,data);
	}
    if (!(offset & 0x20)) {
		mtrap_voice &= 0x7F;
		mtrap_voice |= ((data & 0x01) << 7);
	}
}

int mtrap_voiceio_r(int offset) {
	int data=0;

	if (!(offset & 0x80)) {
       data = (mtrap_voice & 0x06) >> 1;
       data |= (mtrap_voice & 0x01) << 2;
       data |= (mtrap_voice & 0x08);
       return data;
	}
    if (!(offset & 0x40)) {
    	int clock_pulse = (int)(timer_get_time() * (2.0 * CVSD_CLOCK_FREQ));
    	return (clock_pulse & 1) << 7;
	}
	return 0;
}


