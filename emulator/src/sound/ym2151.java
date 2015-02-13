package sound;

import static sound.streams.*;
import static arcadeflex.ptrlib.*;
import static mame.driverH.*;

public class ym2151 {
    /*TODO*////*operator data*/
    /*TODO*///typedef struct{
    /*TODO*///	unsigned int phase;		/*accumulated operator phase*/
    /*TODO*///	unsigned int freq;		/*operator frequency*/
    /*TODO*///	signed   int DTfreq;	/*operator detune frequency*/
    /*TODO*///
    /*TODO*///	unsigned int MUL;		/*phase multiply*/
    /*TODO*///	unsigned int DT1;		/*DT1|MUL * 32  */
    /*TODO*///	unsigned int DT2;		/*DT2 index     */
    /*TODO*///
    /*TODO*///	signed   int *connect;	/*operator output 'direction'*/
    /*TODO*///
    /*TODO*////*Begin of channel specific data*/
    /*TODO*////*note: each operator number 0 contains channel specific data*/
    /*TODO*///	unsigned int FeedBack;	/*feedback shift value for operators 0 in each channel*/
    /*TODO*///	signed   int FB;		/*operator self feedback value used only by operators 0*/
    /*TODO*///	signed   int FB0;		/*previous output value*/
    /*TODO*///	unsigned int KC;		/*operator KC (copied to all operators)*/
    /*TODO*///	unsigned int KCindex;	/*speedup*/
    /*TODO*///	unsigned int KF;		/*operator KF (copied to all operators)*/
    /*TODO*///	unsigned int PMS;		/*channel PMS*/
    /*TODO*///	unsigned int AMS;		/*channel AMS*/
    /*TODO*////*End of channel specific data*/
    /*TODO*///
    /*TODO*///	unsigned int AMSmask;	/*LFO AMS enable mask*/
    /*TODO*///
    /*TODO*///	unsigned int state;		/*Envelope state: 4-attack(AR) 3-decay(D1R) 2-sustain(D2R) 1-release(RR) 0-off*/
    /*TODO*///	unsigned int delta_AR;	/*volume delta for attack phase*/
    /*TODO*///	unsigned int TL;		/*Total attenuation Level*/
    /*TODO*///	signed   int volume;	/*operator attenuation level*/
    /*TODO*///	unsigned int delta_D1R;	/*volume delta for decay phase*/
    /*TODO*///	unsigned int D1L;		/*EG switches to D2R, when envelope reaches this level*/
    /*TODO*///	unsigned int delta_D2R;	/*volume delta for sustain phase*/
    /*TODO*///	unsigned int delta_RR;	/*volume delta for release phase*/
    /*TODO*///
    /*TODO*///	unsigned int key;		/*0=last key was KEY OFF, 1=last key was KEY ON*/
    /*TODO*///
    /*TODO*///	unsigned int KS;		/*Key Scale     */
    /*TODO*///	unsigned int AR;		/*Attack rate   */
    /*TODO*///	unsigned int D1R;		/*Decay rate    */
    /*TODO*///	unsigned int D2R;		/*Sustain rate  */
    /*TODO*///	unsigned int RR;		/*Release rate  */
    /*TODO*///
    /*TODO*///	signed   int LFOpm;		/*phase modulation from LFO*/
    /*TODO*///	signed   int a_vol;		/*used for attack phase calculations*/
    /*TODO*///
    /*TODO*///} OscilRec;
    /*TODO*///
    /*TODO*///
    /*TODO*///typedef struct
    /*TODO*///{
    /*TODO*///	OscilRec Oscils[32];	/*there are 32 operators in YM2151*/
    /*TODO*///
    /*TODO*///	unsigned int PAN[16];	/*channels output masks (0xffffffff = enable)*/
    /*TODO*///
    /*TODO*///	unsigned int LFOphase;	/*accumulated LFO phase         */
    /*TODO*///	unsigned int LFOfrq;	/*LFO frequency                 */
    /*TODO*///	unsigned int LFOwave;	/*LFO waveform (0-saw, 1-square, 2-triangle, 3-random noise)*/
    /*TODO*///	unsigned int PMD;		/*LFO Phase Modulation Depth    */
    /*TODO*///	unsigned int AMD;		/*LFO Amplitude Modulation Depth*/
    /*TODO*///	unsigned int LFA;		/*current AM from LFO*/
    /*TODO*///	signed   int LFP;		/*current PM from LFO*/
    /*TODO*///
    /*TODO*///	unsigned int test;		/*TEST register*/
    /*TODO*///
    /*TODO*///	unsigned int CT;		/*output control pins (bit7 CT2, bit6 CT1)*/
    /*TODO*///	unsigned int noise;		/*noise register (bit 7 - noise enable, bits 4-0 - noise freq*/
    /*TODO*///
    /*TODO*///	unsigned int IRQenable;	/*IRQ enable for timer B (bit 3) and timer A (bit 2)*/
    /*TODO*///	unsigned int status;	/*chip status (BUSY, IRQ Flags)*/
    /*TODO*///
    /*TODO*///
    /*TODO*///	void *TimATimer,*TimBTimer;	/*ASG 980324 -- added for tracking timers*/
    /*TODO*///	double TimerATime[1024];	/*Timer A times for MAME*/
    /*TODO*///	double TimerBTime[256];		/*Timer B times for MAME*/
    /*TODO*///
    /*TODO*///	unsigned int TimAIndex;		/*Timer A index*/
    /*TODO*///	unsigned int TimBIndex;		/*Timer B index*/
    /*TODO*///
    /*TODO*///	unsigned int TimAOldIndex;	/*Timer A previous index*/
    /*TODO*///	unsigned int TimBOldIndex;	/*Timer B previous index*/
    /*TODO*///
    /*TODO*///	/*
    /*TODO*///	*   Frequency-deltas to get the closest frequency possible.
    /*TODO*///	*   There're 11 octaves because of DT2 (max 950 cents over base frequency)
    /*TODO*///	*   and LFO phase modulation (max 700 cents below AND over base frequency)
    /*TODO*///	*   Summary:   octave  explanation
    /*TODO*///	*              0       note code - LFO PM
    /*TODO*///	*              1       note code
    /*TODO*///	*              2       note code
    /*TODO*///	*              3       note code
    /*TODO*///	*              4       note code
    /*TODO*///	*              5       note code
    /*TODO*///	*              6       note code
    /*TODO*///	*              7       note code
    /*TODO*///	*              8       note code
    /*TODO*///	*              9       note code + DT2 + LFO PM
    /*TODO*///	*              10      note code + DT2 + LFO PM
    /*TODO*///	*/
    /*TODO*///	unsigned int freq[11*12*64];/*11 octaves, 12 semitones, 64 'cents'*/
    /*TODO*///
    /*TODO*///	/*
    /*TODO*///	*   Frequency deltas for DT1. These deltas alter operator frequency
    /*TODO*///	*   after it has been taken from frequency-deltas table.
    /*TODO*///	*/
    /*TODO*///	signed   int DT1freq[8*16*32];		/*8 DT1 levels,16 MUL lelvels, 32 KC values*/
    /*TODO*///
    /*TODO*///	unsigned int EG_tab[32+64+32];		/*envelope deltas (32 + 64 rates + 32 RKS)*/
    /*TODO*///
    /*TODO*///	unsigned int LFOfreq[256];			/*frequency deltas for LFO*/
    /*TODO*///
    /*TODO*///	void (*irqhandler)(int irq);				/*IRQ function handler*/
    /*TODO*///	void (*porthandler)(int offset, int data);	/*port write function handler*/
    /*TODO*///
    /*TODO*///	unsigned int clock;			/*chip clock in Hz (passed from 2151intf.c)*/
    /*TODO*///	unsigned int sampfreq;		/*sampling frequency in Hz (passed from 2151intf.c)*/
    /*TODO*///
    /*TODO*///} YM2151;
    /*TODO*///
    /*TODO*///
    /*TODO*////*
    /*TODO*///**  Shifts below are subject to change when sampling frequency changes...
    /*TODO*///*/
    /*TODO*///#define FREQ_SH			16  /* 16.16 fixed point (frequency calculations) */
    /*TODO*///#define ENV_SH			16  /* 16.16 fixed point (envelope calculations)  */
    /*TODO*///#define LFO_SH			23  /*  9.23 fixed point (LFO calculations)       */
    /*TODO*///#define TIMER_SH		16  /* 16.16 fixed point (timers calculations)    */
    /*TODO*///
    /*TODO*///#define FREQ_MASK		((1<<FREQ_SH)-1)
    /*TODO*///#define ENV_MASK		((1<<ENV_SH)-1)
    /*TODO*///
    /*TODO*///#define ENV_BITS		10
    /*TODO*///#define ENV_LEN			(1<<ENV_BITS)
    /*TODO*///#define ENV_STEP		(128.0/ENV_LEN)
    /*TODO*///#define ENV_QUIET		((int)(0x68/(ENV_STEP)))
    /*TODO*///
    /*TODO*///#define MAX_ATT_INDEX	((ENV_LEN<<ENV_SH)-1) /*1023.ffff*/
    /*TODO*///#define MIN_ATT_INDEX	(      (1<<ENV_SH)-1) /*   0.ffff*/
    /*TODO*///
    /*TODO*///#define EG_ATT			4
    /*TODO*///#define EG_DEC			3
    /*TODO*///#define EG_SUS			2
    /*TODO*///#define EG_REL			1
    /*TODO*///#define EG_OFF			0
    /*TODO*///
    /*TODO*///#define SIN_BITS		10
    /*TODO*///#define SIN_LEN			(1<<SIN_BITS)
    /*TODO*///#define SIN_MASK		(SIN_LEN-1)
    /*TODO*///
    /*TODO*///#define TL_RES_LEN		(256) /* 8 bits addressing (real chip) */
    /*TODO*///
    /*TODO*///#define LFO_BITS		9
    /*TODO*///#define LFO_LEN			(1<<LFO_BITS)
    /*TODO*///#define LFO_MASK		(LFO_LEN-1)
    /*TODO*///
    /*TODO*///#if (SAMPLE_BITS==16)
    /*TODO*///	#define FINAL_SH	(0)
    /*TODO*///	#define MAXOUT		(+32767)
    /*TODO*///	#define MINOUT		(-32768)
    /*TODO*///#else
    /*TODO*///	#define FINAL_SH	(8)
    /*TODO*///	#define MAXOUT		(+127)
    /*TODO*///	#define MINOUT		(-128)
    /*TODO*///#endif
    /*TODO*///
    /*TODO*///
    /*TODO*////* TL_TAB_LEN is calculated as:
    /*TODO*/// * 13 - sinus amplitude bits  (Y axis)
    /*TODO*/// * 2  - sinus sign bit        (Y axis)
    /*TODO*/// * ENV_LEN - sinus resolution (X axis)
    /*TODO*///*/
    /*TODO*///#define TL_TAB_LEN (13*2*TL_RES_LEN)
    /*TODO*///static signed int TL_TAB[TL_TAB_LEN];
    /*TODO*///
    /*TODO*////* sin waveform table in 'decibel' scale*/
    /*TODO*///static unsigned int sin_tab[SIN_LEN];
    /*TODO*///
    /*TODO*////* four AM/PM LFO waveforms (8 in total)*/
    /*TODO*///static unsigned int lfo_tab[LFO_LEN*4*2];
    /*TODO*///
    /*TODO*////* LFO amplitude modulation depth table (128 levels)*/
    /*TODO*///static unsigned int lfo_md_tab[128];
    /*TODO*///
    /*TODO*////* translate from D1L to volume index (16 D1L levels)*/
    /*TODO*///static unsigned int D1L_tab[16];
    /*TODO*///
    /*TODO*////*
    /*TODO*/// * translate from key code KC (OCT2 OCT1 OCT0 N3 N2 N1 N0) to
    /*TODO*/// * index in frequency-deltas table. (9 octaves * 16 note codes)
    /*TODO*///*/
    /*TODO*///static unsigned int KC_TO_INDEX[9*16];
    /*TODO*///
    /*TODO*////*
    /*TODO*/// *   DT2 defines offset in cents from base note
    /*TODO*/// *
    /*TODO*/// *   This table defines offset in frequency-deltas table.
    /*TODO*/// *   User's Manual page 22
    /*TODO*/// *
    /*TODO*/// *   Values below were calculated using formula: value =  orig.val / 1.5625
    /*TODO*/// *
    /*TODO*/// *	DT2=0 DT2=1 DT2=2 DT2=3
    /*TODO*/// *	0     600   781   950
    /*TODO*///*/
    /*TODO*///static unsigned int DT2_tab[4] = { 0, 384, 500, 608 };
    /*TODO*///
    /*TODO*////*
    /*TODO*/// *   DT1 defines offset in Hertz from base note
    /*TODO*/// *   This table is converted while initialization...
    /*TODO*/// *   Detune table in YM2151 User's Manual is wrong (checked against the real chip)
    /*TODO*///*/
    /*TODO*///static unsigned char DT1_tab[4*32] = { /* 4*32 DT1 values */
    /*TODO*////* DT1=0 */
    /*TODO*///  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    /*TODO*///  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    /*TODO*///
    /*TODO*////* DT1=1 */
    /*TODO*///  0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2,
    /*TODO*///  2, 3, 3, 3, 4, 4, 4, 5, 5, 6, 6, 7, 8, 8, 8, 8,
    /*TODO*///
    /*TODO*////* DT1=2 */
    /*TODO*///  1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5,
    /*TODO*///  5, 6, 6, 7, 8, 8, 9,10,11,12,13,14,16,16,16,16,
    /*TODO*///
    /*TODO*////* DT1=3 */
    /*TODO*///  2, 2, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 6, 6, 7,
    /*TODO*///  8, 8, 9,10,11,12,13,14,16,17,19,20,22,22,22,22
    /*TODO*///};
    /*TODO*///
    /*TODO*///
    /*TODO*///static YM2151 * YMPSG = NULL;	/* array of YM2151's */
    /*TODO*///static unsigned int YMNumChips;	/* total # of YM2151's emulated */
    /*TODO*///
    /*TODO*///
    /*TODO*////*these variables stay here because of speedup purposes only */
    /*TODO*///static YM2151 * PSG;
    /*TODO*///static signed int chanout[8];
    /*TODO*///static signed int c1,m2,c2; /*Phase Modulation input for operators 2,3,4*/
    /*TODO*///
    /*TODO*///static void init_tables(void)
    /*TODO*///{
    /*TODO*///	signed int i,x;
    /*TODO*///	signed int n;
    /*TODO*///	double o,m;
    /*TODO*///
    /*TODO*///	for (x=0; x<TL_RES_LEN; x++)
    /*TODO*///	{
    /*TODO*///		m = (1<<16) / pow(2, (x+1) * (ENV_STEP/4.0) / 8.0);
    /*TODO*///		m = floor(m);
    /*TODO*///
    /*TODO*///		/* we never reach (1<<16) here due to the (x+1) */
    /*TODO*///		/* result fits within 16 bits at maximum */
    /*TODO*///
    /*TODO*///		n = (int)m;		/* 16 bits here */
    /*TODO*///		n >>= 4;		/* 12 bits here */
    /*TODO*///		if (n&1)		/* round to closest */
    /*TODO*///			n = (n>>1)+1;
    /*TODO*///		else
    /*TODO*///			n = n>>1;
    /*TODO*///						/* 11 bits here (rounded) */
    /*TODO*///		n <<= 2;		/* 13 bits here (as in real chip) */
    /*TODO*///		TL_TAB[ x*2 + 0 ] = n;
    /*TODO*///		TL_TAB[ x*2 + 1 ] = -TL_TAB[ x*2 + 0 ];
    /*TODO*///
    /*TODO*///		for (i=1; i<13; i++)
    /*TODO*///		{
    /*TODO*///			TL_TAB[ x*2+0 + i*2*TL_RES_LEN ] =  TL_TAB[ x*2+0 ]>>i;
    /*TODO*///			TL_TAB[ x*2+1 + i*2*TL_RES_LEN ] = -TL_TAB[ x*2+0 + i*2*TL_RES_LEN ];
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	/*if (errorlog) fprintf(errorlog,"TL_TAB_LEN = %i (%i bytes)\n",TL_TAB_LEN, (int)sizeof(TL_TAB));*/
    /*TODO*///
    /*TODO*///	for (i=0; i<SIN_LEN; i++)
    /*TODO*///	{
    /*TODO*///		/* non-standard sinus */
    /*TODO*///		m = sin( ((i*2)+1) * PI / SIN_LEN ); /* checked against the real chip */
    /*TODO*///
    /*TODO*///		/* we never reach zero here due to ((i*2)+1) */
    /*TODO*///
    /*TODO*///		if (m>0.0)
    /*TODO*///			o = 8*log(1.0/m)/log(2);  /* convert to 'decibels' */
    /*TODO*///		else
    /*TODO*///			o = 8*log(-1.0/m)/log(2); /* convert to 'decibels' */
    /*TODO*///
    /*TODO*///		o = o / (ENV_STEP/4);
    /*TODO*///
    /*TODO*///		n = (int)(2.0*o);
    /*TODO*///		if (n&1)		/* round to closest */
    /*TODO*///			n = (n>>1)+1;
    /*TODO*///		else
    /*TODO*///			n = n>>1;
    /*TODO*///
    /*TODO*///		sin_tab[ i ] = n*2 + (m>=0.0? 0: 1 );
    /*TODO*///		/*if (errorlog) fprintf(errorlog,"sin offs %04i= %i\n", i, sin_tab[i]);*/
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/*if (errorlog) fprintf(errorlog,"ENV_QUIET= %08x\n",ENV_QUIET );*/
    /*TODO*///
    /*TODO*///
    /*TODO*///	/* calculate LFO AM waveforms*/
    /*TODO*///	for (x=0; x<4; x++)
    /*TODO*///	{
    /*TODO*///	    for (i=0; i<LFO_LEN; i++)
    /*TODO*///	    {
    /*TODO*///		switch (x)
    /*TODO*///		{
    /*TODO*///		case 0:	/* saw (255 down to 0) */
    /*TODO*///			m = 255 - (i/2);
    /*TODO*///			break;
    /*TODO*///		case 1: /* square (255,0) */
    /*TODO*///			if (i<256)
    /*TODO*///				m = 255;
    /*TODO*///			else
    /*TODO*///				m = 0;
    /*TODO*///			break;
    /*TODO*///		case 2: /* triangle (255 down to 0, up to 255) */
    /*TODO*///			if (i<256)
    /*TODO*///				m = 255 - i;
    /*TODO*///			else
    /*TODO*///				m = i - 256;
    /*TODO*///			break;
    /*TODO*///		case 3: /* random (range 0 to 255) */
    /*TODO*///			m = ((int)rand()) & 255;
    /*TODO*///			break;
    /*TODO*///		}
    /*TODO*///		/* we reach m = zero here !!!*/
    /*TODO*///
    /*TODO*///		if (m>0.0)
    /*TODO*///			o = 8*log(255.0/m)/log(2);  /* convert to 'decibels' */
    /*TODO*///		else
    /*TODO*///		{
    /*TODO*///			if (m<0.0)
    /*TODO*///				o = 8*log(-255.0/m)/log(2); /* convert to 'decibels' */
    /*TODO*///			else
    /*TODO*///				o = 8*log(255.0/0.01)/log(2); /* small number */
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		o = o / (ENV_STEP/4);
    /*TODO*///
    /*TODO*///		n = (int)(2.0*o);
    /*TODO*///		if (n&1)		/* round to closest */
    /*TODO*///			n = (n>>1)+1;
    /*TODO*///		else
    /*TODO*///			n = n>>1;
    /*TODO*///
    /*TODO*///		lfo_tab[ x*LFO_LEN*2 + i*2 ] = n*2 + (m>=0.0? 0: 1 );
    /*TODO*///		/*if (errorlog) fprintf(errorlog,"lfo am waveofs[%i] %04i = %i\n", x, i*2, lfo_tab[ x*LFO_LEN*2 + i*2 ] );*/
    /*TODO*///	    }
    /*TODO*///	}
    /*TODO*///	for (i=0; i<128; i++)
    /*TODO*///	{
    /*TODO*///		m = i*2; /*m=0,2,4,6,8,10,..,252,254*/
    /*TODO*///
    /*TODO*///		/* we reach m = zero here !!!*/
    /*TODO*///
    /*TODO*///		if (m>0.0)
    /*TODO*///			o = 8*log(8192.0/m)/log(2);  /* convert to 'decibels' */
    /*TODO*///		else
    /*TODO*///			o = 8*log(8192.0/0.01)/log(2); /* small number (m=0)*/
    /*TODO*///
    /*TODO*///		o = o / (ENV_STEP/4);
    /*TODO*///
    /*TODO*///		n = (int)(2.0*o);
    /*TODO*///		if (n&1)		/* round to closest */
    /*TODO*///			n = (n>>1)+1;
    /*TODO*///		else
    /*TODO*///			n = n>>1;
    /*TODO*///
    /*TODO*///		lfo_md_tab[ i ] = n*2;
    /*TODO*///		/*if (errorlog) fprintf(errorlog,"lfo_md_tab[%i](%i) = ofs %i shr by %i\n", i, i*2, (lfo_md_tab[i]>>1)&255, lfo_md_tab[i]>>9 );*/
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* calculate LFO PM waveforms*/
    /*TODO*///	for (x=0; x<4; x++)
    /*TODO*///	{
    /*TODO*///	    for (i=0; i<LFO_LEN; i++)
    /*TODO*///	    {
    /*TODO*///		switch (x)
    /*TODO*///		{
    /*TODO*///		case 0:	/* saw (0 to 127, -128 to -1) */
    /*TODO*///			if (i<256)
    /*TODO*///				m = (i/2);
    /*TODO*///			else
    /*TODO*///				m = (i/2)-256;
    /*TODO*///			break;
    /*TODO*///		case 1: /* square (127,-128) */
    /*TODO*///			if (i<256)
    /*TODO*///				m = 127;
    /*TODO*///			else
    /*TODO*///				m = -128;
    /*TODO*///			break;
    /*TODO*///		case 2: /* triangle (0 to 127,127 to -128,-127 to 0) */
    /*TODO*///			if (i<128)
    /*TODO*///				m = i; /*0 to 127*/
    /*TODO*///			else
    /*TODO*///			{
    /*TODO*///				if (i<384)
    /*TODO*///					m = 255 - i; /*127 down to -128*/
    /*TODO*///				else
    /*TODO*///					m = i - 511; /*-127 to 0*/
    /*TODO*///			}
    /*TODO*///			break;
    /*TODO*///		case 3: /* random (range -128 to 127) */
    /*TODO*///			m = ((int)rand()) & 255;
    /*TODO*///			m -=128;
    /*TODO*///			break;
    /*TODO*///		}
    /*TODO*///		/* we reach m = zero here !!!*/
    /*TODO*///
    /*TODO*///		if (m>0.0)
    /*TODO*///			o = 8*log(127.0/m)/log(2);  /* convert to 'decibels' */
    /*TODO*///		else
    /*TODO*///		{
    /*TODO*///			if (m<0.0)
    /*TODO*///				o = 8*log(-128.0/m)/log(2); /* convert to 'decibels' */
    /*TODO*///			else
    /*TODO*///				o = 8*log(127.0/0.01)/log(2); /* small number */
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		o = o / (ENV_STEP/4);
    /*TODO*///
    /*TODO*///		n = (int)(2.0*o);
    /*TODO*///		if (n&1)		/* round to closest */
    /*TODO*///			n = (n>>1)+1;
    /*TODO*///		else
    /*TODO*///			n = n>>1;
    /*TODO*///
    /*TODO*///		lfo_tab[ x*LFO_LEN*2 + i*2 + 1 ] = n*2 + (m>=0.0? 0: 1 );
    /*TODO*///		/*if (errorlog) fprintf(errorlog,"lfo pm waveofs[%i] %04i = %i\n", x, i*2+1, lfo_tab[ x*LFO_LEN*2 + i*2 + 1 ] );*/
    /*TODO*///	    }
    /*TODO*///	}
    /*TODO*///
    /*TODO*///
    /*TODO*///	/* calculate D1L_tab table */
    /*TODO*///	for (i=0; i<16; i++)
    /*TODO*///	{
    /*TODO*///		m = (i<15?i:i+16) * (4.0/ENV_STEP);   /*every 3 'dB' except for all bits = 1 = 45dB+48dB*/
    /*TODO*///		D1L_tab[i] = m * (1<<ENV_SH);
    /*TODO*///		/*if (errorlog) fprintf(errorlog,"D1L_tab[%04x]=%08x\n",i,D1L_tab[i] );*/
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* calculate KC_TO_INDEX table */
    /*TODO*///	x=0;
    /*TODO*///	for (i=0; i<8*16; i++)
    /*TODO*///	{
    /*TODO*///		KC_TO_INDEX[i]=((i>>4)*12*64) + x*64 + 12*64;
    /*TODO*///		if ((i&0x03) != 0x03) x++;	/* change note code */
    /*TODO*///		if ((i&0x0f) == 0x0f) x=0;	/* new octave */
    /*TODO*///		/*if (errorlog) fprintf(errorlog,"KC_TO_INDEX[%02i] Note=%02i\n",i,KC_TO_INDEX[i]);*/
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///static void init_chip_tables(YM2151 *chip)
    /*TODO*///{
    /*TODO*///	int i,j;
    /*TODO*///	double mult,pom,pom2,clk,phaseinc,Hz;
    /*TODO*///
    /*TODO*///	double scaler;	/* formula below is true for chip clock=3579545 */
    /*TODO*///	/* so we need to scale its output accordingly to the chip clock */
    /*TODO*///
    /*TODO*///	scaler= (double)chip->clock / 3579545.0;
    /*TODO*///
    /*TODO*///	/*this loop calculates Hertz values for notes from c-0 to b-7*/
    /*TODO*///	/*including 64 'cents' (100/64 that is 1.5625 of real cent) per note*/
    /*TODO*///	/* i*100/64/1200 is equal to i/768 */
    /*TODO*///
    /*TODO*///	mult = (1<<FREQ_SH);
    /*TODO*///	for (i=0; i<1*12*64; i++)
    /*TODO*///	{
    /*TODO*///		/* 3.4375 Hz is note A; C# is 4 semitones higher */
    /*TODO*///		Hz = scaler * 3.4375 * pow (2, (i+4*64) / 768.0 );
    /*TODO*///
    /*TODO*///		/* calculate phase increment */
    /*TODO*///		phaseinc = (Hz*SIN_LEN) / (double)chip->sampfreq;
    /*TODO*///
    /*TODO*///		chip->freq[i] = phaseinc*mult;
    /*TODO*///		for (j=1; j<11; j++)
    /*TODO*///		{
    /*TODO*///			chip->freq[i+j*12*64] = chip->freq[i]*(1<<j);
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	mult = (1<<FREQ_SH);
    /*TODO*///	for (j=0; j<4; j++)
    /*TODO*///	{
    /*TODO*///		for (i=0; i<32; i++)
    /*TODO*///		{
    /*TODO*///			int x, y, mul;
    /*TODO*///
    /*TODO*///			Hz = ( (double)DT1_tab[j*32+i] * ((double)chip->clock/8.0) ) / (double)(1<<24);
    /*TODO*///			/*Important note:
    /*TODO*///			**  The frequency calculated above is HALF of the frequency found in the Manual.
    /*TODO*///			**  This is intentional as Manual gives the frequencies for MUL = 1, not MUL = 0.5
    /*TODO*///			*/
    /*TODO*///			/*calculate phase increment*/
    /*TODO*///			phaseinc = (Hz*SIN_LEN) / (double)chip->sampfreq;
    /*TODO*///
    /*TODO*///			/*positive and negative values*/
    /*TODO*///			chip->DT1freq[ (j+0)*16*32 + i ] = phaseinc * mult;
    /*TODO*///			chip->DT1freq[ (j+4)*16*32 + i ] = -chip->DT1freq[ (j+0)*16*32 + i ];
    /*TODO*///
    /*TODO*///			for (mul=1; mul<16; mul++)
    /*TODO*///			{
    /*TODO*///				x = (j+0)*16*32 + mul*32;
    /*TODO*///				y = (j+4)*16*32 + mul*32;
    /*TODO*///				/*positive and negative values*/
    /*TODO*///				chip->DT1freq[ x + i ] = phaseinc * mult * (mul*2);
    /*TODO*///				chip->DT1freq[ y + i ] = -chip->DT1freq[ x + i ];
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	mult = (1<<LFO_SH);
    /*TODO*///	clk  = (double)chip->clock;
    /*TODO*///	for (i=0; i<256; i++)
    /*TODO*///	{
    /*TODO*///		j = i & 0x0f;
    /*TODO*///		pom = fabs(  (clk/65536/(1<<(i/16)) ) - (clk/65536/32/(1<<(i/16)) * (j+1)) );
    /*TODO*///
    /*TODO*///		/*calculate phase increment*/
    /*TODO*///		chip->LFOfreq[0xff-i] = ( (pom*LFO_LEN) / (double)chip->sampfreq ) * mult; /*fixed point*/
    /*TODO*///		/*if (errorlog) fprintf(errorlog, "LFO[%02x] (%08x)= real %20.15f Hz  emul %20.15f Hz\n",0xff-i, chip->LFOfreq[0xff-i], pom,
    /*TODO*///			(((double)chip->LFOfreq[0xff-i] / mult) * (double)chip->sampfreq ) / (double)LFO_LEN );*/
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	for (i=0; i<34; i++)
    /*TODO*///		chip->EG_tab[i] = 0;		/* infinity */
    /*TODO*///
    /*TODO*///	for (i=2; i<64; i++)
    /*TODO*///	{
    /*TODO*///		pom2 = (double)chip->clock / (double)chip->sampfreq;
    /*TODO*///		if (i<60) pom2 *= ( 1 + (i&3)*0.25 );
    /*TODO*///		pom2 *= 1<<((i>>2));
    /*TODO*///		pom2 /= 768.0 * 1024.0;
    /*TODO*///		pom2 *= (double)(1<<ENV_SH);
    /*TODO*///		chip->EG_tab[32+i] = pom2;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	for (i=0; i<32; i++)
    /*TODO*///	{
    /*TODO*///		chip->EG_tab[ 32+64+i ] = chip->EG_tab[32+63];
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* precalculate timers' deltas */
    /*TODO*///	/* User's Manual pages 15,16  */
    /*TODO*///	mult = (1<<TIMER_SH);
    /*TODO*///	for (i=0; i<1024; i++)
    /*TODO*///	{
    /*TODO*///		/* ASG 980324: changed to compute both TimerA and TimerATime */
    /*TODO*///		pom= ( 64.0  *  (1024.0-i) / (double)chip->clock );
    /*TODO*///			chip->TimerATime[i] = pom;
    /*TODO*///	}
    /*TODO*///	for (i=0; i<256; i++)
    /*TODO*///	{
    /*TODO*///		/* ASG 980324: changed to compute both TimerB and TimerBTime */
    /*TODO*///		pom= ( 1024.0 * (256.0-i)  / (double)chip->clock );
    /*TODO*///			chip->TimerBTime[i] = pom;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///INLINE void envelope_KONKOFF(OscilRec * op, int v)
    /*TODO*///{
    /*TODO*///	if (v&0x08)
    /*TODO*///	{
    /*TODO*///		if (!op->key)
    /*TODO*///		{
    /*TODO*///			op->key   = 1;      /*KEYON'ed*/
    /*TODO*///			op->phase = 0;      /*clear phase */
    /*TODO*///			op->state = EG_ATT; /*KEY ON = attack*/
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		if (op->key)
    /*TODO*///		{
    /*TODO*///			op->key   = 0;      /*KEYOFF'ed*/
    /*TODO*///			if (op->state>EG_REL)
    /*TODO*///				op->state = EG_REL; /*release*/
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	op+=8;
    /*TODO*///
    /*TODO*///	if (v&0x20)
    /*TODO*///	{
    /*TODO*///		if (!op->key)
    /*TODO*///		{
    /*TODO*///			op->key   = 1;
    /*TODO*///			op->phase = 0;
    /*TODO*///			op->state = EG_ATT;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		if (op->key)
    /*TODO*///		{
    /*TODO*///			op->key   = 0;
    /*TODO*///			if (op->state>EG_REL)
    /*TODO*///				op->state = EG_REL;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	op+=8;
    /*TODO*///
    /*TODO*///	if (v&0x10)
    /*TODO*///	{
    /*TODO*///		if (!op->key)
    /*TODO*///		{
    /*TODO*///			op->key   = 1;
    /*TODO*///			op->phase = 0;
    /*TODO*///			op->state = EG_ATT;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		if (op->key)
    /*TODO*///		{
    /*TODO*///			op->key   = 0;
    /*TODO*///			if (op->state>EG_REL)
    /*TODO*///				op->state = EG_REL;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	op+=8;
    /*TODO*///
    /*TODO*///	if (v&0x40)
    /*TODO*///	{
    /*TODO*///		if (!op->key)
    /*TODO*///		{
    /*TODO*///			op->key   = 1;
    /*TODO*///			op->phase = 0;
    /*TODO*///			op->state = EG_ATT;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		if (op->key)
    /*TODO*///		{
    /*TODO*///			op->key   = 0;
    /*TODO*///			if (op->state>EG_REL)
    /*TODO*///				op->state = EG_REL;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///static void timer_callback_a (int n)
    /*TODO*///{
    /*TODO*///	YM2151 *chip = &YMPSG[n];
    /*TODO*///	chip->TimATimer = timer_set (chip->TimerATime[ chip->TimAIndex ], n, timer_callback_a);
    /*TODO*///	chip->TimAOldIndex = chip->TimAIndex;
    /*TODO*///	if (chip->IRQenable & 0x04)
    /*TODO*///	{
    /*TODO*///		int oldstate = chip->status & 3;
    /*TODO*///		chip->status |= 1;
    /*TODO*///		if ((!oldstate) && (chip->irqhandler)) (*chip->irqhandler)(1);
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///static void timer_callback_b (int n)
    /*TODO*///{
    /*TODO*///	YM2151 *chip = &YMPSG[n];
    /*TODO*///	chip->TimBTimer = timer_set (chip->TimerBTime[ chip->TimBIndex ], n, timer_callback_b);
    /*TODO*///	chip->TimBOldIndex = chip->TimBIndex;
    /*TODO*///	if (chip->IRQenable & 0x08)
    /*TODO*///	{
    /*TODO*///		int oldstate = chip->status & 3;
    /*TODO*///		chip->status |= 2;
    /*TODO*///		if ((!oldstate) && (chip->irqhandler)) (*chip->irqhandler)(1);
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///INLINE void set_connect( OscilRec *om1, int v, int cha)
    /*TODO*///{
    /*TODO*///	OscilRec *om2 = om1+8;
    /*TODO*///	OscilRec *oc1 = om1+16;
    /*TODO*///	/*OscilRec *oc2 = om1+24;*/
    /*TODO*///	/*oc2->connect = &chanout[cha];*/
    /*TODO*///
    /*TODO*///	/* set connect algorithm */
    /*TODO*///
    /*TODO*///	switch( v & 7 )
    /*TODO*///	{
    /*TODO*///	case 0:
    /*TODO*///		/* M1---C1---M2---C2---OUT */
    /*TODO*///		om1->connect = &c1;
    /*TODO*///		oc1->connect = &m2;
    /*TODO*///		om2->connect = &c2;
    /*TODO*///		break;
    /*TODO*///	case 1:
    /*TODO*///		/* M1-+-M2---C2---OUT */
    /*TODO*///		/* C1-+               */
    /*TODO*///		om1->connect = &m2;
    /*TODO*///		oc1->connect = &m2;
    /*TODO*///		om2->connect = &c2;
    /*TODO*///		break;
    /*TODO*///	case 2:
    /*TODO*///		/* M1------+-C2---OUT */
    /*TODO*///		/* C1---M2-+          */
    /*TODO*///		om1->connect = &c2;
    /*TODO*///		oc1->connect = &m2;
    /*TODO*///		om2->connect = &c2;
    /*TODO*///		break;
    /*TODO*///	case 3:
    /*TODO*///		/* M1---C1-+-C2---OUT */
    /*TODO*///		/* M2------+          */
    /*TODO*///		om1->connect = &c1;
    /*TODO*///		oc1->connect = &c2;
    /*TODO*///		om2->connect = &c2;
    /*TODO*///		break;
    /*TODO*///	case 4:
    /*TODO*///		/* M1---C1-+--OUT */
    /*TODO*///		/* M2---C2-+      */
    /*TODO*///		om1->connect = &c1;
    /*TODO*///		oc1->connect = &chanout[cha];
    /*TODO*///		om2->connect = &c2;
    /*TODO*///		break;
    /*TODO*///	case 5:
    /*TODO*///		/*    +-C1-+     */
    /*TODO*///		/* M1-+-M2-+-OUT */
    /*TODO*///		/*    +-C2-+     */
    /*TODO*///		om1->connect = 0;	/* special mark */
    /*TODO*///		oc1->connect = &chanout[cha];
    /*TODO*///		om2->connect = &chanout[cha];
    /*TODO*///		break;
    /*TODO*///	case 6:
    /*TODO*///		/* M1---C1-+     */
    /*TODO*///		/*      M2-+-OUT */
    /*TODO*///		/*      C2-+     */
    /*TODO*///		om1->connect = &c1;
    /*TODO*///		oc1->connect = &chanout[cha];
    /*TODO*///		om2->connect = &chanout[cha];
    /*TODO*///		break;
    /*TODO*///	case 7:
    /*TODO*///		/* M1-+     */
    /*TODO*///		/* C1-+-OUT */
    /*TODO*///		/* M2-+     */
    /*TODO*///		/* C2-+     */
    /*TODO*///		om1->connect = &chanout[cha];
    /*TODO*///		oc1->connect = &chanout[cha];
    /*TODO*///		om2->connect = &chanout[cha];
    /*TODO*///		break;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///INLINE void refresh_EG( YM2151 *chip, OscilRec * op)
    /*TODO*///{
    /*TODO*///	unsigned int kc;
    /*TODO*///	unsigned int v;
    /*TODO*///
    /*TODO*///	kc = op->KC;
    /*TODO*///
    /*TODO*///	/*v = 32 + 2*RATE + RKS (max 126)*/
    /*TODO*///
    /*TODO*///	v = kc >> op->KS;
    /*TODO*///	if ((op->AR+v) < 32+62)
    /*TODO*///		op->delta_AR  = chip->EG_tab[ op->AR + v];
    /*TODO*///	else
    /*TODO*///		op->delta_AR  = MAX_ATT_INDEX+1;
    /*TODO*///	op->delta_D1R = chip->EG_tab[op->D1R + v];
    /*TODO*///	op->delta_D2R = chip->EG_tab[op->D2R + v];
    /*TODO*///	op->delta_RR  = chip->EG_tab[ op->RR + v];
    /*TODO*///
    /*TODO*///	op+=8;
    /*TODO*///
    /*TODO*///	v = kc >> op->KS;
    /*TODO*///	if ((op->AR+v) < 32+62)
    /*TODO*///		op->delta_AR  = chip->EG_tab[ op->AR + v];
    /*TODO*///	else
    /*TODO*///		op->delta_AR  = MAX_ATT_INDEX+1;
    /*TODO*///	op->delta_D1R = chip->EG_tab[op->D1R + v];
    /*TODO*///	op->delta_D2R = chip->EG_tab[op->D2R + v];
    /*TODO*///	op->delta_RR  = chip->EG_tab[ op->RR + v];
    /*TODO*///
    /*TODO*///	op+=8;
    /*TODO*///
    /*TODO*///	v = kc >> op->KS;
    /*TODO*///	if ((op->AR+v) < 32+62)
    /*TODO*///		op->delta_AR  = chip->EG_tab[ op->AR + v];
    /*TODO*///	else
    /*TODO*///		op->delta_AR  = MAX_ATT_INDEX+1;
    /*TODO*///	op->delta_D1R = chip->EG_tab[op->D1R + v];
    /*TODO*///	op->delta_D2R = chip->EG_tab[op->D2R + v];
    /*TODO*///	op->delta_RR  = chip->EG_tab[ op->RR + v];
    /*TODO*///
    /*TODO*///	op+=8;
    /*TODO*///
    /*TODO*///	v = kc >> op->KS;
    /*TODO*///	if ((op->AR+v) < 32+62)
    /*TODO*///		op->delta_AR  = chip->EG_tab[ op->AR + v];
    /*TODO*///	else
    /*TODO*///		op->delta_AR  = MAX_ATT_INDEX+1;
    /*TODO*///	op->delta_D1R = chip->EG_tab[op->D1R + v];
    /*TODO*///	op->delta_D2R = chip->EG_tab[op->D2R + v];
    /*TODO*///	op->delta_RR  = chip->EG_tab[ op->RR + v];
    /*TODO*///
    /*TODO*///}
    /*TODO*///
    
    /* write a register on YM2151 chip number 'n' */
    public static void YM2151WriteReg(int n, int r, int v)
    {
    /*TODO*///	YM2151 *chip = &(YMPSG[n]);
    /*TODO*///	OscilRec *op = &chip->Oscils[ r&0x1f ];
    /*TODO*///
    /*TODO*///	/*adjust bus to 8 bits*/
    /*TODO*///	r &= 0xff;
    /*TODO*///	v &= 0xff;
    /*TODO*///
    /*TODO*///	switch(r & 0xe0){
    /*TODO*///	case 0x00:
    /*TODO*///		switch(r){
    /*TODO*///		case 0x01: /*LFO reset(bit 1), Test Register (other bits)*/
    /*TODO*///			chip->test = v;
    /*TODO*///			if (v&2) chip->LFOphase = 0;
    /*TODO*///			break;
    /*TODO*///
    /*TODO*///		case 0x08:
    /*TODO*///			envelope_KONKOFF(&chip->Oscils[ v&7 ], v );
    /*TODO*///			break;
    /*TODO*///
    /*TODO*///		case 0x0f: /*noise mode enable, noise freq*/
    /*TODO*///			chip->noise = v;
    /*TODO*///			/*if ((v&0x80)) printf("YM2151 noise (%02x)\n",v);*/
    /*TODO*///			break;
    /*TODO*///
    /*TODO*///		case 0x10: /*timer A hi*/
    /*TODO*///			chip->TimAIndex = (chip->TimAIndex & 0x003) | (v<<2);
    /*TODO*///			break;
    /*TODO*///
    /*TODO*///		case 0x11: /*timer A low*/
    /*TODO*///			chip->TimAIndex = (chip->TimAIndex & 0x3fc) | (v & 3);
    /*TODO*///			break;
    /*TODO*///
    /*TODO*///		case 0x12: /*timer B*/
    /*TODO*///			chip->TimBIndex = v;
    /*TODO*///			break;
    /*TODO*///
    /*TODO*///		case 0x14: /*CSM, irq flag reset, irq enable, timer start/stop*/
    /*TODO*///			/*if ((v&0x80)) printf("\nYM2151 CSM MODE ON\n");*/
    /*TODO*///
    /*TODO*///			chip->IRQenable = v;	/*bit 3-timer B, bit 2-timer A*/
    /*TODO*///
    /*TODO*///			if (v&0x20)	/*reset timer B irq flag*/
    /*TODO*///			{
    /*TODO*///				int oldstate = chip->status & 3;
    /*TODO*///				chip->status &= 0xfd;
    /*TODO*///				if ((oldstate==2) && (chip->irqhandler)) (*chip->irqhandler)(0);
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			if (v&0x10)	/*reset timer A irq flag*/
    /*TODO*///			{
    /*TODO*///				int oldstate = chip->status & 3;
    /*TODO*///				chip->status &= 0xfe;
    /*TODO*///				if ((oldstate==1) && (chip->irqhandler)) (*chip->irqhandler)(0);
    /*TODO*///
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			if (v&0x02){	/*load and start timer B*/
    /*TODO*///				/* ASG 980324: added a real timer */
    /*TODO*///				/* start timer _only_ if it wasn't already started (it will reload time value next round)*/
    /*TODO*///					if (!chip->TimBTimer)
    /*TODO*///					{
    /*TODO*///						chip->TimBTimer = timer_set (chip->TimerBTime[ chip->TimBIndex ], n, timer_callback_b);
    /*TODO*///						chip->TimBOldIndex = chip->TimBIndex;
    /*TODO*///					}
    /*TODO*///			}else{		/*stop timer B*/
    /*TODO*///				/* ASG 980324: added a real timer */
    /*TODO*///					if (chip->TimBTimer) timer_remove (chip->TimBTimer);
    /*TODO*///					chip->TimBTimer = 0;
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			if (v&0x01){	/*load and start timer A*/
    /*TODO*///				/* ASG 980324: added a real timer */
    /*TODO*///				/* start timer _only_ if it wasn't already started (it will reload time value next round)*/
    /*TODO*///					if (!chip->TimATimer)
    /*TODO*///					{
    /*TODO*///						chip->TimATimer = timer_set (chip->TimerATime[ chip->TimAIndex ], n, timer_callback_a);
    /*TODO*///						chip->TimAOldIndex = chip->TimAIndex;
    /*TODO*///					}
    /*TODO*///			}else{		/*stop timer A*/
    /*TODO*///				/* ASG 980324: added a real timer */
    /*TODO*///					if (chip->TimATimer) timer_remove (chip->TimATimer);
    /*TODO*///					chip->TimATimer = 0;
    /*TODO*///			}
    /*TODO*///			break;
    /*TODO*///
    /*TODO*///		case 0x18: /*LFO frequency*/
    /*TODO*///			chip->LFOfrq = chip->LFOfreq[ v ];
    /*TODO*///			break;
    /*TODO*///
    /*TODO*///		case 0x19: /*PMD (bit 7==1) or AMD (bit 7==0)*/
    /*TODO*///			if (v&0x80)
    /*TODO*///				chip->PMD = lfo_md_tab[ v&0x7f ] + 512;
    /*TODO*///			else
    /*TODO*///				chip->AMD = lfo_md_tab[ v&0x7f ];
    /*TODO*///			break;
    /*TODO*///
    /*TODO*///		case 0x1b: /*CT2, CT1, LFO waveform*/
    /*TODO*///			chip->CT = v;
    /*TODO*///			chip->LFOwave = (v & 3) * LFO_LEN*2;
    /*TODO*///			if (chip->porthandler) (*chip->porthandler)(0 , (chip->CT) >> 6 );
    /*TODO*///			break;
    /*TODO*///
    /*TODO*///		default:
    /*TODO*///			if (errorlog) fprintf(errorlog,"YM2151 Write %02x to undocumented register #%02x\n",v,r);
    /*TODO*///			break;
    /*TODO*///		}
    /*TODO*///		break;
    /*TODO*///
    /*TODO*///	case 0x20:
    /*TODO*///		op = &chip->Oscils[r & 7];
    /*TODO*///		switch(r & 0x18){
    /*TODO*///		case 0x00: /*RL enable, Feedback, Connection */
    /*TODO*///			op->FeedBack = ((v>>3)&7) ? ((v>>3)&7)+6:0;
    /*TODO*///			chip->PAN[ (r&7)*2    ] = (v & 0x40) ? 0xffffffff : 0x0;
    /*TODO*///			chip->PAN[ (r&7)*2 +1 ] = (v & 0x80) ? 0xffffffff : 0x0;
    /*TODO*///			set_connect(op, v, r&7);
    /*TODO*///			break;
    /*TODO*///
    /*TODO*///		case 0x08: /*Key Code*/
    /*TODO*///			v &= 0x7f;
    /*TODO*///			/*v++;*/
    /*TODO*///			if (v != op->KC)
    /*TODO*///			{
    /*TODO*///				unsigned int kc,kc_channel;
    /*TODO*///
    /*TODO*///				kc = KC_TO_INDEX[v];
    /*TODO*///				(op+0)->KC = v;
    /*TODO*///				(op+0)->KCindex = kc;
    /*TODO*///				(op+8)->KC = v;
    /*TODO*///				(op+8)->KCindex = kc;
    /*TODO*///				(op+16)->KC = v;
    /*TODO*///				(op+16)->KCindex = kc;
    /*TODO*///				(op+24)->KC = v;
    /*TODO*///				(op+24)->KCindex = kc;
    /*TODO*///
    /*TODO*///				kc_channel = op->KCindex + op->KF;
    /*TODO*///				kc = v>>2;
    /*TODO*///
    /*TODO*///				(op+0)->freq = chip->freq[ kc_channel + (op+0)->DT2 ] * (op+0)->MUL;
    /*TODO*///				(op+0)->DTfreq = chip->DT1freq[ (op+0)->DT1 + kc ];
    /*TODO*///				(op+8)->freq = chip->freq[ kc_channel + (op+8)->DT2 ] * (op+8)->MUL;
    /*TODO*///				(op+8)->DTfreq = chip->DT1freq[ (op+8)->DT1 + kc ];
    /*TODO*///				(op+16)->freq = chip->freq[ kc_channel + (op+16)->DT2 ] * (op+16)->MUL;
    /*TODO*///				(op+16)->DTfreq = chip->DT1freq[ (op+16)->DT1 + kc ];
    /*TODO*///				(op+24)->freq = chip->freq[ kc_channel + (op+24)->DT2 ] * (op+24)->MUL;
    /*TODO*///				(op+24)->DTfreq = chip->DT1freq[ (op+24)->DT1 + kc ];
    /*TODO*///
    /*TODO*///				refresh_EG( chip, op );
    /*TODO*///			}
    /*TODO*///			break;
    /*TODO*///
    /*TODO*///		case 0x10: /*Key Fraction*/
    /*TODO*///			v >>= 2;
    /*TODO*///			if (v != op->KF)
    /*TODO*///			{
    /*TODO*///				unsigned int kc_channel;
    /*TODO*///
    /*TODO*///				(op+0)->KF = v;
    /*TODO*///				(op+8)->KF = v;
    /*TODO*///				(op+16)->KF = v;
    /*TODO*///				(op+24)->KF = v;
    /*TODO*///
    /*TODO*///				kc_channel = op->KCindex + op->KF;
    /*TODO*///
    /*TODO*///				(op+0)->freq = chip->freq[ kc_channel + (op+0)->DT2 ] * (op+0)->MUL;
    /*TODO*///				(op+8)->freq = chip->freq[ kc_channel + (op+8)->DT2 ] * (op+8)->MUL;
    /*TODO*///				(op+16)->freq = chip->freq[ kc_channel + (op+16)->DT2 ] * (op+16)->MUL;
    /*TODO*///				(op+24)->freq = chip->freq[ kc_channel + (op+24)->DT2 ] * (op+24)->MUL;
    /*TODO*///			}
    /*TODO*///			break;
    /*TODO*///
    /*TODO*///		case 0x18: /*PMS,AMS*/
    /*TODO*///			op->PMS = (v>>4) & 7;
    /*TODO*///			op->AMS = v & 3;
    /*TODO*///			break;
    /*TODO*///		}
    /*TODO*///		break;
    /*TODO*///
    /*TODO*///	case 0x40: /*DT1, MUL*/
    /*TODO*///		{
    /*TODO*///			unsigned int oldDT1 = op->DT1;
    /*TODO*///			unsigned int oldMUL = op->MUL;
    /*TODO*///			op->DT1 = (v&0x7f)<<5;
    /*TODO*///			op->MUL = (v&0x0f) ? (v&0x0f)<<1: 1;
    /*TODO*///			if (oldDT1 != op->DT1)
    /*TODO*///			{
    /*TODO*///				op->DTfreq = chip->DT1freq[ op->DT1 + (op->KC>>2) ];
    /*TODO*///			}
    /*TODO*///			if (oldMUL != op->MUL)
    /*TODO*///			{
    /*TODO*///				unsigned int kc_channel;
    /*TODO*///
    /*TODO*///				kc_channel = op->KCindex + op->KF;
    /*TODO*///				op->freq = chip->freq[ kc_channel + op->DT2 ] * op->MUL;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///		break;
    /*TODO*///
    /*TODO*///	case 0x60: /*TL*/
    /*TODO*///		op->TL = (v&0x7f)<<(ENV_BITS-7); /*7bit TL*/
    /*TODO*///		break;
    /*TODO*///
    /*TODO*///	case 0x80: /*KS, AR*/
    /*TODO*///		{
    /*TODO*///			int oldKS = op->KS;
    /*TODO*///			int oldAR = op->AR;
    /*TODO*///			op->KS = 5-(v>>6);
    /*TODO*///			op->AR = (v&0x1f) ? 32 + ((v&0x1f)<<1) : 0;
    /*TODO*///
    /*TODO*///			if ( (op->AR != oldAR) || (op->KS != oldKS) )
    /*TODO*///			{
    /*TODO*///				if ((op->AR + (op->KC>>op->KS)) < 32+62)
    /*TODO*///					op->delta_AR  = chip->EG_tab[op->AR  + (op->KC>>op->KS) ];
    /*TODO*///				else
    /*TODO*///					op->delta_AR  = MAX_ATT_INDEX+1;
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			if (op->KS != oldKS)
    /*TODO*///			{
    /*TODO*///				op->delta_D1R = chip->EG_tab[op->D1R + (op->KC>>op->KS) ];
    /*TODO*///				op->delta_D2R = chip->EG_tab[op->D2R + (op->KC>>op->KS) ];
    /*TODO*///				op->delta_RR  = chip->EG_tab[op->RR  + (op->KC>>op->KS) ];
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///		break;
    /*TODO*///
    /*TODO*///	case 0xa0: /*AMS-EN, D1R*/
    /*TODO*///		op->AMSmask = (v&0x80) ? 0xffffffff : 0;
    /*TODO*///		op->D1R     = (v&0x1f) ? 32 + ((v&0x1f)<<1) : 0;
    /*TODO*///		op->delta_D1R = chip->EG_tab[op->D1R + (op->KC>>op->KS) ];
    /*TODO*///		break;
    /*TODO*///
    /*TODO*///	case 0xc0: /*DT2, D2R*/
    /*TODO*///		{
    /*TODO*///			unsigned int oldDT2 = op->DT2;
    /*TODO*///			op->DT2 = DT2_tab[ v>>6 ];
    /*TODO*///			if (op->DT2 != oldDT2)
    /*TODO*///			{
    /*TODO*///				unsigned int kc_channel;
    /*TODO*///
    /*TODO*///				kc_channel = op->KCindex + op->KF;
    /*TODO*///				op->freq = chip->freq[ kc_channel + op->DT2 ] * op->MUL;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///		op->D2R = (v&0x1f) ? 32 + ((v&0x1f)<<1) : 0;
    /*TODO*///		op->delta_D2R = chip->EG_tab[op->D2R + (op->KC>>op->KS) ];
    /*TODO*///		break;
    /*TODO*///
    /*TODO*///	case 0xe0: /*D1L, RR*/
    /*TODO*///		op->D1L = D1L_tab[ v>>4 ];
    /*TODO*///		op->RR  = 34 + ((v&0x0f)<<2);
    /*TODO*///		op->delta_RR  = chip->EG_tab[op->RR  + (op->KC>>op->KS) ];
    /*TODO*///		break;
    /*TODO*///	}
    }
    public static int YM2151ReadStatus( int n )
    {
        throw new UnsupportedOperationException("Unsupported");
    /*TODO*///	return YMPSG[n].status;
    }

    /*
    ** Initialize YM2151 emulator(s).
    **
    ** 'num' is the number of virtual YM2151's to allocate
    ** 'clock' is the chip clock in Hz
    ** 'rate' is sampling rate
    */
    public static int YM2151Init(int num, int clock, int rate)
    {
    /*TODO*///	int i;
    /*TODO*///
    /*TODO*///	if (YMPSG) return (-1);	/* duplicate init. */
    /*TODO*///
    /*TODO*///	YMNumChips = num;
    /*TODO*///
    /*TODO*///	YMPSG = (YM2151 *)malloc(sizeof(YM2151) * YMNumChips);
    /*TODO*///	if (YMPSG == NULL) return (1);
    /*TODO*///
    /*TODO*///	init_tables();
    /*TODO*///	for (i=0 ; i<YMNumChips; i++)
    /*TODO*///	{
    /*TODO*///		YMPSG[i].clock = clock;
    /*TODO*///		/*rate = clock/64;*/
    /*TODO*///		YMPSG[i].sampfreq = rate ? rate : 44100;	/* avoid division by 0 in init_chip_tables() */
    /*TODO*///		YMPSG[i].irqhandler = NULL;					/*interrupt handler */
    /*TODO*///		YMPSG[i].porthandler = NULL;				/*port write handler*/
    /*TODO*///		init_chip_tables(&YMPSG[i]);
    /*TODO*///		YM2151ResetChip(i);
    /*TODO*///		/*if (errorlog) fprintf(errorlog,"YM2151[init] clock=%i sampfreq=%i\n", YMPSG[i].clock, YMPSG[i].sampfreq);*/
    /*TODO*///	}
    /*TODO*///
            return(0);
    }

    public static void YM2151Shutdown()
    {
    /*TODO*///	if (!YMPSG) return;
    /*TODO*///
    /*TODO*///	free(YMPSG);
    /*TODO*///	YMPSG = NULL;
    }


    /*
    ** reset all chip registers.
    */
    public static void YM2151ResetChip(int num)
    {
    /*TODO*///	int i;
    /*TODO*///	YM2151 *chip = &YMPSG[num];
    /*TODO*///
    /*TODO*///	/* initialize hardware registers */
    /*TODO*///
    /*TODO*///	for (i=0; i<32; i++)
    /*TODO*///	{
    /*TODO*///		memset(&chip->Oscils[i],'\0',sizeof(OscilRec));
    /*TODO*///		chip->Oscils[i].volume = MAX_ATT_INDEX;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	chip->LFOphase = 0;
    /*TODO*///	chip->LFOfrq   = 0;
    /*TODO*///	chip->LFOwave  = 0;
    /*TODO*///	chip->PMD = lfo_md_tab[ 0 ]+512;
    /*TODO*///	chip->AMD = lfo_md_tab[ 0 ];
    /*TODO*///	chip->LFA = 0;
    /*TODO*///	chip->LFP = 0;
    /*TODO*///
    /*TODO*///	chip->test= 0;
    /*TODO*///
    /*TODO*///	chip->IRQenable = 0;
    /*TODO*///
    /*TODO*///	/* ASG 980324 -- reset the timers before writing to the registers */
    /*TODO*///	chip->TimATimer = 0;
    /*TODO*///	chip->TimBTimer = 0;
    /*TODO*///	chip->TimAIndex = 0;
    /*TODO*///	chip->TimBIndex = 0;
    /*TODO*///	chip->TimAOldIndex = 0;
    /*TODO*///	chip->TimBOldIndex = 0;
    /*TODO*///
    /*TODO*///	chip->noise     = 0;
    /*TODO*///
    /*TODO*///	chip->status    = 0;
    /*TODO*///
    /*TODO*///	YM2151WriteReg(num, 0x1b, 0); /*only because of CT1, CT2 output pins*/
    /*TODO*///	for (i=0x20; i<0x100; i++)   /*just to set the PM operators */
    /*TODO*///	{
    /*TODO*///		YM2151WriteReg(num, i, 0);
    /*TODO*///	}
    /*TODO*///
    }
    /*TODO*///
    /*TODO*///
    /*TODO*///INLINE void lfo_calc(void)
    /*TODO*///{
    /*TODO*///unsigned int phase, lfx;
    /*TODO*///
    /*TODO*///	if (PSG->test&2)
    /*TODO*///	{
    /*TODO*///		PSG->LFOphase = 0;
    /*TODO*///		phase = PSG->LFOwave;
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		phase = (PSG->LFOphase>>LFO_SH) & LFO_MASK;
    /*TODO*///		phase = phase*2 + PSG->LFOwave;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	lfx = lfo_tab[phase] + PSG->AMD;
    /*TODO*///
    /*TODO*///	PSG->LFA = 0;
    /*TODO*///	if (lfx < TL_TAB_LEN)
    /*TODO*///		PSG->LFA = TL_TAB[ lfx ];
    /*TODO*///
    /*TODO*///	lfx = lfo_tab[phase+1] + PSG->PMD;
    /*TODO*///
    /*TODO*///	PSG->LFP = 0;
    /*TODO*///	if (lfx < TL_TAB_LEN)
    /*TODO*///		PSG->LFP = TL_TAB[ lfx ];
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///INLINE void calc_lfo_pm(OscilRec *op)
    /*TODO*///{
    /*TODO*///signed int mod_ind,pom;
    /*TODO*///
    /*TODO*///	mod_ind = PSG->LFP; /* -128..+127 (8bits signed)*/
    /*TODO*///	if (op->PMS < 6)
    /*TODO*///		mod_ind >>= (6 - op->PMS);
    /*TODO*///	else
    /*TODO*///		mod_ind <<= (op->PMS - 5);
    /*TODO*///
    /*TODO*///	if (mod_ind)
    /*TODO*///	{
    /*TODO*///		unsigned int kc_channel;
    /*TODO*///
    /*TODO*///		kc_channel = op->KCindex + op->KF + mod_ind;
    /*TODO*///
    /*TODO*///		pom = PSG->freq[ kc_channel + op->DT2 ] * op->MUL;
    /*TODO*///		op->phase += (pom - op->freq);
    /*TODO*///
    /*TODO*///		op+=8;
    /*TODO*///		pom = PSG->freq[ kc_channel + op->DT2 ] * op->MUL;
    /*TODO*///		op->phase += (pom - op->freq);
    /*TODO*///
    /*TODO*///		op+=8;
    /*TODO*///		pom = PSG->freq[ kc_channel + op->DT2 ] * op->MUL;
    /*TODO*///		op->phase += (pom - op->freq);
    /*TODO*///
    /*TODO*///		op+=8;
    /*TODO*///		pom = PSG->freq[ kc_channel + op->DT2 ] * op->MUL;
    /*TODO*///		op->phase += (pom - op->freq);
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///INLINE signed int op_calc(OscilRec * OP, unsigned int env, signed int pm)
    /*TODO*///{
    /*TODO*///	unsigned int p;
    /*TODO*///
    /*TODO*///	p = (env<<3) + sin_tab[ ( ((signed int)((OP->phase & ~FREQ_MASK) + (pm<<15))) >> FREQ_SH ) & SIN_MASK ];
    /*TODO*///
    /*TODO*///	if (p >= TL_TAB_LEN)
    /*TODO*///		return 0;
    /*TODO*///
    /*TODO*///	return TL_TAB[p];
    /*TODO*///}
    /*TODO*///
    /*TODO*///INLINE signed int op_calc1(OscilRec * OP, unsigned int env, signed int pm)
    /*TODO*///{
    /*TODO*///	unsigned int p;
    /*TODO*///	signed int i;
    /*TODO*///
    /*TODO*///	i = (OP->phase & ~FREQ_MASK) + pm;
    /*TODO*///
    /*TODO*////*if (errorlog) fprintf(errorlog,"i=%08x (i>>16)&511=%8i phase=%i [pm=%08x] ",i, (i>>16)&511, OP->phase>>FREQ_SH, pm);*/
    /*TODO*///
    /*TODO*///	p = (env<<3) + sin_tab[ (i>>FREQ_SH) & SIN_MASK];
    /*TODO*///
    /*TODO*////*if (errorlog) fprintf(errorlog," (p&255=%i p>>8=%i) out= %i\n", p&255,p>>8, TL_TAB[p&255]>>(p>>8) );*/
    /*TODO*///
    /*TODO*///	if (p >= TL_TAB_LEN)
    /*TODO*///		return 0;
    /*TODO*///
    /*TODO*///	return TL_TAB[p];
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///#define volume_calc(OP) (OP->TL + (((unsigned int)OP->volume)>>ENV_SH) + (AM & OP->AMSmask))
    /*TODO*///
    /*TODO*///INLINE void chan_calc(unsigned int chan)
    /*TODO*///{
    /*TODO*///OscilRec *OP;
    /*TODO*///unsigned int env;
    /*TODO*///unsigned int AM;
    /*TODO*///
    /*TODO*///	chanout[chan]= c1 = m2 = c2 = 0;
    /*TODO*///	AM = 0;
    /*TODO*///
    /*TODO*///	OP = &PSG->Oscils[chan]; /*M1*/
    /*TODO*///
    /*TODO*///	if (OP->AMS)
    /*TODO*///		AM = PSG->LFA << (OP->AMS-1);
    /*TODO*///
    /*TODO*///	if (OP->PMS)
    /*TODO*///		calc_lfo_pm(OP);
    /*TODO*///
    /*TODO*///	env = volume_calc(OP);
    /*TODO*///	{
    /*TODO*///		signed int out;
    /*TODO*///
    /*TODO*///		out = OP->FB0 + OP->FB;
    /*TODO*///		OP->FB0 = OP->FB;
    /*TODO*///
    /*TODO*///		if (!OP->connect)
    /*TODO*///			/* algorithm 5 */
    /*TODO*///			c1 = m2 = c2 = OP->FB0;
    /*TODO*///		else
    /*TODO*///			/* other algorithms */
    /*TODO*///			*OP->connect = OP->FB0;
    /*TODO*///
    /*TODO*///		OP->FB = 0;
    /*TODO*///
    /*TODO*///		if (env < ENV_QUIET)
    /*TODO*///			OP->FB = op_calc1(OP, env, (out<<OP->FeedBack) );
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	OP += 16; /*C1*/
    /*TODO*///	env = volume_calc(OP);
    /*TODO*///	if (env < ENV_QUIET)
    /*TODO*///		*OP->connect += op_calc(OP, env, c1);
    /*TODO*///
    /*TODO*///	OP -= 8;  /*M2*/
    /*TODO*///	env = volume_calc(OP);
    /*TODO*///	if (env < ENV_QUIET)
    /*TODO*///		*OP->connect += op_calc(OP, env, m2);
    /*TODO*///
    /*TODO*///	OP += 16; /*C2*/
    /*TODO*///	env = volume_calc(OP);
    /*TODO*///	if (env < ENV_QUIET)
    /*TODO*///		chanout[chan] += op_calc(OP, env, c2);
    /*TODO*///
    /*TODO*///}
    /*TODO*///
    /*TODO*///INLINE void advance(void)
    /*TODO*///{
    /*TODO*///OscilRec *op;
    /*TODO*///int i;
    /*TODO*///
    /*TODO*///    if (!(PSG->test&2))
    /*TODO*///        PSG->LFOphase += PSG->LFOfrq;
    /*TODO*///
    /*TODO*///    op = &PSG->Oscils[0]; /*CH0 M1*/
    /*TODO*///    i = 32;
    /*TODO*///    do
    /*TODO*///    {
    /*TODO*///        op->phase += op->freq;
    /*TODO*///        op->phase += op->DTfreq;
    /*TODO*///
    /*TODO*///	switch(op->state)
    /*TODO*///	{
    /*TODO*///	case EG_ATT:	/*attack phase*/
    /*TODO*///		{
    /*TODO*///			signed int step;
    /*TODO*///
    /*TODO*///			step = op->volume;
    /*TODO*///			op->volume -= op->delta_AR;
    /*TODO*///			step = (step>>ENV_SH) - (((unsigned int)op->volume)>>ENV_SH); /*number of levels passed since last time*/
    /*TODO*///			if (step > 0)
    /*TODO*///			{
    /*TODO*///				signed int tmp_volume;
    /*TODO*///
    /*TODO*///				tmp_volume = op->volume + (step<<ENV_SH); /*adjust by number of levels*/
    /*TODO*///				do
    /*TODO*///				{
    /*TODO*///					tmp_volume = tmp_volume - (1<<ENV_SH) - ((tmp_volume>>4) & ~ENV_MASK);
    /*TODO*///					if (tmp_volume <= MIN_ATT_INDEX)
    /*TODO*///						break;
    /*TODO*///					step--;
    /*TODO*///				}while(step);
    /*TODO*///				op->volume = tmp_volume;
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			if (op->volume <= MIN_ATT_INDEX)
    /*TODO*///			{
    /*TODO*///				if (op->volume < 0)
    /*TODO*///					op->volume = 0; /*this is not quite correct (checked)*/
    /*TODO*///				op->state = EG_DEC;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///		break;
    /*TODO*///
    /*TODO*///	case EG_DEC:	/*decay phase*/
    /*TODO*///		if ( (op->volume += op->delta_D1R) >= op->D1L )
    /*TODO*///		{
    /*TODO*///			op->volume = op->D1L; /*this is not quite correct (checked)*/
    /*TODO*///			op->state = EG_SUS;
    /*TODO*///		}
    /*TODO*///		break;
    /*TODO*///
    /*TODO*///	case EG_SUS:	/*sustain phase*/
    /*TODO*///		if ( (op->volume += op->delta_D2R) > MAX_ATT_INDEX )
    /*TODO*///		{
    /*TODO*///			op->state = EG_OFF;
    /*TODO*///			op->volume = MAX_ATT_INDEX;
    /*TODO*///		}
    /*TODO*///		break;
    /*TODO*///
    /*TODO*///	case EG_REL:	/*release phase*/
    /*TODO*///		if ( (op->volume += op->delta_RR) > MAX_ATT_INDEX )
    /*TODO*///		{
    /*TODO*///			op->state = EG_OFF;
    /*TODO*///			op->volume = MAX_ATT_INDEX;
    /*TODO*///		}
    /*TODO*///		break;
    /*TODO*///	}
    /*TODO*///	op++;
    /*TODO*///	i--;
    /*TODO*///    }while (i);
    /*TODO*///}
    /*TODO*///
    /*TODO*///INLINE signed int acc_calc(signed int value)
    /*TODO*///{
    /*TODO*///	if (value>=0)
    /*TODO*///	{
    /*TODO*///		if (value < 0x0200)
    /*TODO*///			return (value);
    /*TODO*///		if (value < 0x0400)
    /*TODO*///			return (value & 0xfffffffe);
    /*TODO*///		if (value < 0x0800)
    /*TODO*///			return (value & 0xfffffffc);
    /*TODO*///		if (value < 0x1000)
    /*TODO*///			return (value & 0xfffffff8);
    /*TODO*///		if (value < 0x2000)
    /*TODO*///			return (value & 0xfffffff0);
    /*TODO*///		if (value < 0x4000)
    /*TODO*///			return (value & 0xffffffe0);
    /*TODO*///		return (value & 0xffffffc0);
    /*TODO*///	}
    /*TODO*///	/*else value < 0*/
    /*TODO*///	if (value > -0x0200)
    /*TODO*///		return (value);
    /*TODO*///	if (value > -0x0400)
    /*TODO*///		return (value & 0xfffffffe);
    /*TODO*///	if (value > -0x0800)
    /*TODO*///		return (value & 0xfffffffc);
    /*TODO*///	if (value > -0x1000)
    /*TODO*///		return (value & 0xfffffff8);
    /*TODO*///	if (value > -0x2000)
    /*TODO*///		return (value & 0xfffffff0);
    /*TODO*///	if (value > -0x4000)
    /*TODO*///		return (value & 0xffffffe0);
    /*TODO*///	return (value & 0xffffffc0);
    /*TODO*///
    /*TODO*///}

    /*
    ** Generate samples for one of the YM2151's
    **
    ** 'num' is the number of virtual YM2151
    ** '**buffers' is table of pointers to the buffers: left and right
    ** 'length' is the number of samples that should be generated
    */
    public static StreamInitMultiPtr YM2151UpdateOne = new StreamInitMultiPtr() {
            public void handler(int num, UShortPtr[] buffer, int length) {
    /*TODO*///	int i;
    /*TODO*///	signed int outl,outr;
    /*TODO*///	SAMP *bufL, *bufR;
    /*TODO*///
    /*TODO*///	bufL = buffers[0];
    /*TODO*///	bufR = buffers[1];
    /*TODO*///
    /*TODO*///	PSG = &YMPSG[num];
    /*TODO*///	
    /*TODO*///	for (i=0; i<length; i++)
    /*TODO*///	{
    /*TODO*///
    /*TODO*///		chan_calc(0);
    /*TODO*///		SAVE_SINGLE_CHANNEL(0)
    /*TODO*///		chan_calc(1);
    /*TODO*///		SAVE_SINGLE_CHANNEL(1)
    /*TODO*///		chan_calc(2);
    /*TODO*///		SAVE_SINGLE_CHANNEL(2)
    /*TODO*///		chan_calc(3);
    /*TODO*///		SAVE_SINGLE_CHANNEL(3)
    /*TODO*///		chan_calc(4);
    /*TODO*///		SAVE_SINGLE_CHANNEL(4)
    /*TODO*///		chan_calc(5);
    /*TODO*///		SAVE_SINGLE_CHANNEL(5)
    /*TODO*///		chan_calc(6);
    /*TODO*///		SAVE_SINGLE_CHANNEL(6)
    /*TODO*///		chan_calc(7);
    /*TODO*///		SAVE_SINGLE_CHANNEL(7)
    /*TODO*///
    /*TODO*///		SAVE_ALL_CHANNELS
    /*TODO*///
    /*TODO*///		outl = chanout[0] & PSG->PAN[0];
    /*TODO*///		outr = chanout[0] & PSG->PAN[1];
    /*TODO*///		outl += (chanout[1] & PSG->PAN[2]);
    /*TODO*///		outr += (chanout[1] & PSG->PAN[3]);
    /*TODO*///		outl += (chanout[2] & PSG->PAN[4]);
    /*TODO*///		outr += (chanout[2] & PSG->PAN[5]);
    /*TODO*///		outl += (chanout[3] & PSG->PAN[6]);
    /*TODO*///		outr += (chanout[3] & PSG->PAN[7]);
    /*TODO*///		outl += (chanout[4] & PSG->PAN[8]);
    /*TODO*///		outr += (chanout[4] & PSG->PAN[9]);
    /*TODO*///		outl += (chanout[5] & PSG->PAN[10]);
    /*TODO*///		outr += (chanout[5] & PSG->PAN[11]);
    /*TODO*///		outl += (chanout[6] & PSG->PAN[12]);
    /*TODO*///		outr += (chanout[6] & PSG->PAN[13]);
    /*TODO*///		outl += (chanout[7] & PSG->PAN[14]);
    /*TODO*///		outr += (chanout[7] & PSG->PAN[15]);
    /*TODO*///
    /*TODO*///		outl >>= FINAL_SH;
    /*TODO*///		outr >>= FINAL_SH;
    /*TODO*///		if (outl > MAXOUT) outl = MAXOUT;
    /*TODO*///			else if (outl < MINOUT) outl = MINOUT;
    /*TODO*///		if (outr > MAXOUT) outr = MAXOUT;
    /*TODO*///			else if (outr < MINOUT) outr = MINOUT;
    /*TODO*///		((SAMP*)bufL)[i] = (SAMP)outl;
    /*TODO*///		((SAMP*)bufR)[i] = (SAMP)outr;
    /*TODO*///
    /*TODO*///		lfo_calc();
    /*TODO*///		advance();
    /*TODO*///
    /*TODO*///	}
    }};
    public static void YM2151SetIrqHandler(int n, WriteYmHandlerPtr handler)
    {
    /*TODO*///	YMPSG[n].irqhandler = handler;
    }
    public static void YM2151SetPortWriteHandler(int n, WriteHandlerPtr handler)
    {
    /*TODO*///	YMPSG[n].porthandler = handler;
    }  
}
