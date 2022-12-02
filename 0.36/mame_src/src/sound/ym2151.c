#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include "driver.h"
#include "ym2151.h"


/*undef this to not use MAME timer system*/
#define USE_MAME_TIMERS

/*#define FM_EMU*/
#ifdef FM_EMU
	FILE *errorlog = NULL;
	#define INLINE static __inline__
	#ifdef USE_MAME_TIMERS
		#undef USE_MAME_TIMERS
	#endif
#endif
#ifdef USE_MAME_TIMERS
	/*#define LOG_CYM_FILE*/
	#ifdef LOG_CYM_FILE
		FILE * cymfile = NULL;
		void * cymfiletimer = 0;
	#endif
#endif


/*operator data*/
typedef struct{
	unsigned int phase;		/*accumulated operator phase*/
	unsigned int freq;		/*operator frequency*/
	signed   int DTfreq;	/*operator detune frequency*/

	unsigned int MUL;		/*phase multiply*/
	unsigned int DT1;		/*DT1|MUL * 32  */
	unsigned int DT2;		/*DT2 index     */

	signed   int *connect;	/*operator output 'direction'*/

/*Begin of channel specific data*/
/*note: each operator number 0 contains channel specific data*/
	unsigned int FeedBack;	/*feedback shift value for operators 0 in each channel*/
	signed   int FB;		/*operator self feedback value used only by operators 0*/
	signed   int FB0;		/*previous output value*/
	unsigned int KC;		/*operator KC (copied to all operators)*/
	unsigned int KCindex;	/*speedup*/
	unsigned int KF;		/*operator KF (copied to all operators)*/
	unsigned int PMS;		/*channel PMS*/
	unsigned int AMS;		/*channel AMS*/
/*End of channel specific data*/

	unsigned int AMSmask;	/*LFO AMS enable mask*/

	unsigned int state;		/*Envelope state: 4-attack(AR) 3-decay(D1R) 2-sustain(D2R) 1-release(RR) 0-off*/
	unsigned int delta_AR;	/*volume delta for attack phase*/
	unsigned int TL;		/*Total attenuation Level*/
	signed   int volume;	/*operator attenuation level*/
	unsigned int delta_D1R;	/*volume delta for decay phase*/
	unsigned int D1L;		/*EG switches to D2R, when envelope reaches this level*/
	unsigned int delta_D2R;	/*volume delta for sustain phase*/
	unsigned int delta_RR;	/*volume delta for release phase*/

	unsigned int key;		/*0=last key was KEY OFF, 1=last key was KEY ON*/

	unsigned int KS;		/*Key Scale     */
	unsigned int AR;		/*Attack rate   */
	unsigned int D1R;		/*Decay rate    */
	unsigned int D2R;		/*Sustain rate  */
	unsigned int RR;		/*Release rate  */

	signed   int LFOpm;		/*phase modulation from LFO*/
	signed   int a_vol;		/*used for attack phase calculations*/

} OscilRec;


typedef struct
{
	OscilRec Oscils[32];	/*there are 32 operators in YM2151*/

	unsigned int PAN[16];	/*channels output masks (0xffffffff = enable)*/

	unsigned int LFOphase;	/*accumulated LFO phase         */
	unsigned int LFOfrq;	/*LFO frequency                 */
	unsigned int LFOwave;	/*LFO waveform (0-saw, 1-square, 2-triangle, 3-random noise)*/
	unsigned int PMD;		/*LFO Phase Modulation Depth    */
	unsigned int AMD;		/*LFO Amplitude Modulation Depth*/
	unsigned int LFA;		/*current AM from LFO*/
	signed   int LFP;		/*current PM from LFO*/

	unsigned int test;		/*TEST register*/

	unsigned int CT;		/*output control pins (bit7 CT2, bit6 CT1)*/
	unsigned int noise;		/*noise register (bit 7 - noise enable, bits 4-0 - noise freq*/

	unsigned int IRQenable;	/*IRQ enable for timer B (bit 3) and timer A (bit 2)*/
	unsigned int status;	/*chip status (BUSY, IRQ Flags)*/

#ifdef USE_MAME_TIMERS
	void *TimATimer,*TimBTimer;	/*ASG 980324 -- added for tracking timers*/
	double TimerATime[1024];	/*Timer A times for MAME*/
	double TimerBTime[256];		/*Timer B times for MAME*/
#else
	int TimA,TimB;				/*timer A,B enable (0-disabled)*/
	signed int TimAVal,TimBVal;	/*current value of timer*/
	unsigned int TimerA[1024];	/*Timer A deltas*/
	unsigned int TimerB[256];	/*Timer B deltas*/
#endif
	unsigned int TimAIndex;		/*Timer A index*/
	unsigned int TimBIndex;		/*Timer B index*/

	unsigned int TimAOldIndex;	/*Timer A previous index*/
	unsigned int TimBOldIndex;	/*Timer B previous index*/

	/*
	*   Frequency-deltas to get the closest frequency possible.
	*   There're 11 octaves because of DT2 (max 950 cents over base frequency)
	*   and LFO phase modulation (max 700 cents below AND over base frequency)
	*   Summary:   octave  explanation
	*              0       note code - LFO PM
	*              1       note code
	*              2       note code
	*              3       note code
	*              4       note code
	*              5       note code
	*              6       note code
	*              7       note code
	*              8       note code
	*              9       note code + DT2 + LFO PM
	*              10      note code + DT2 + LFO PM
	*/
	unsigned int freq[11*12*64];/*11 octaves, 12 semitones, 64 'cents'*/

	/*
	*   Frequency deltas for DT1. These deltas alter operator frequency
	*   after it has been taken from frequency-deltas table.
	*/
	signed   int DT1freq[8*16*32];		/*8 DT1 levels,16 MUL lelvels, 32 KC values*/

	unsigned int EG_tab[32+64+32];		/*envelope deltas (32 + 64 rates + 32 RKS)*/

	unsigned int LFOfreq[256];			/*frequency deltas for LFO*/

	void (*irqhandler)(int irq);				/*IRQ function handler*/
	void (*porthandler)(int offset, int data);	/*port write function handler*/

	unsigned int clock;			/*chip clock in Hz (passed from 2151intf.c)*/
	unsigned int sampfreq;		/*sampling frequency in Hz (passed from 2151intf.c)*/

} YM2151;


/*
**  Shifts below are subject to change when sampling frequency changes...
*/
#define FREQ_SH			16  /* 16.16 fixed point (frequency calculations) */
#define ENV_SH			16  /* 16.16 fixed point (envelope calculations)  */
#define LFO_SH			23  /*  9.23 fixed point (LFO calculations)       */
#define TIMER_SH		16  /* 16.16 fixed point (timers calculations)    */

#define FREQ_MASK		((1<<FREQ_SH)-1)
#define ENV_MASK		((1<<ENV_SH)-1)

#define ENV_BITS		10
#define ENV_LEN			(1<<ENV_BITS)
#define ENV_STEP		(128.0/ENV_LEN)
#define ENV_QUIET		((int)(0x68/(ENV_STEP)))

#define MAX_ATT_INDEX	((ENV_LEN<<ENV_SH)-1) /*1023.ffff*/
#define MIN_ATT_INDEX	(      (1<<ENV_SH)-1) /*   0.ffff*/

#define EG_ATT			4
#define EG_DEC			3
#define EG_SUS			2
#define EG_REL			1
#define EG_OFF			0

#define SIN_BITS		10
#define SIN_LEN			(1<<SIN_BITS)
#define SIN_MASK		(SIN_LEN-1)

#define TL_RES_LEN		(256) /* 8 bits addressing (real chip) */

#define LFO_BITS		9
#define LFO_LEN			(1<<LFO_BITS)
#define LFO_MASK		(LFO_LEN-1)

#if (SAMPLE_BITS==16)
	#define FINAL_SH	(0)
	#define MAXOUT		(+32767)
	#define MINOUT		(-32768)
#else
	#define FINAL_SH	(8)
	#define MAXOUT		(+127)
	#define MINOUT		(-128)
#endif


/* TL_TAB_LEN is calculated as:
 * 13 - sinus amplitude bits  (Y axis)
 * 2  - sinus sign bit        (Y axis)
 * ENV_LEN - sinus resolution (X axis)
*/
#define TL_TAB_LEN (13*2*TL_RES_LEN)
static signed int TL_TAB[TL_TAB_LEN];

/* sin waveform table in 'decibel' scale*/
static unsigned int sin_tab[SIN_LEN];

/* four AM/PM LFO waveforms (8 in total)*/
static unsigned int lfo_tab[LFO_LEN*4*2];

/* LFO amplitude modulation depth table (128 levels)*/
static unsigned int lfo_md_tab[128];

/* translate from D1L to volume index (16 D1L levels)*/
static unsigned int D1L_tab[16];

/*
 * translate from key code KC (OCT2 OCT1 OCT0 N3 N2 N1 N0) to
 * index in frequency-deltas table. (9 octaves * 16 note codes)
*/
static unsigned int KC_TO_INDEX[9*16];

/*
 *   DT2 defines offset in cents from base note
 *
 *   This table defines offset in frequency-deltas table.
 *   User's Manual page 22
 *
 *   Values below were calculated using formula: value =  orig.val / 1.5625
 *
 *	DT2=0 DT2=1 DT2=2 DT2=3
 *	0     600   781   950
*/
static unsigned int DT2_tab[4] = { 0, 384, 500, 608 };

/*
 *   DT1 defines offset in Hertz from base note
 *   This table is converted while initialization...
 *   Detune table in YM2151 User's Manual is wrong (checked against the real chip)
*/
static unsigned char DT1_tab[4*32] = { /* 4*32 DT1 values */
/* DT1=0 */
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,

/* DT1=1 */
  0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2,
  2, 3, 3, 3, 4, 4, 4, 5, 5, 6, 6, 7, 8, 8, 8, 8,

/* DT1=2 */
  1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5,
  5, 6, 6, 7, 8, 8, 9,10,11,12,13,14,16,16,16,16,

/* DT1=3 */
  2, 2, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 6, 6, 7,
  8, 8, 9,10,11,12,13,14,16,17,19,20,22,22,22,22
};


static YM2151 * YMPSG = NULL;	/* array of YM2151's */
static unsigned int YMNumChips;	/* total # of YM2151's emulated */


/*these variables stay here because of speedup purposes only */
static YM2151 * PSG;
static signed int chanout[8];
static signed int c1,m2,c2; /*Phase Modulation input for operators 2,3,4*/



/*save output as raw 16-bit sample*/
/*#define SAVE_SAMPLE*/
/*#define SAVE_SEPARATE_CHANNELS*/

#if defined SAVE_SAMPLE || defined SAVE_SEPARATE_CHANNELS
static FILE *sample[9];
#endif



/* own PI definition */
#ifdef PI
	#undef PI
#endif
#define PI 3.14159265358979323846

static void init_tables(void)
{
	signed int i,x;
	signed int n;
	double o,m;

	for (x=0; x<TL_RES_LEN; x++)
	{
		m = (1<<16) / pow(2, (x+1) * (ENV_STEP/4.0) / 8.0);
		m = floor(m);

		/* we never reach (1<<16) here due to the (x+1) */
		/* result fits within 16 bits at maximum */

		n = (int)m;		/* 16 bits here */
		n >>= 4;		/* 12 bits here */
		if (n&1)		/* round to closest */
			n = (n>>1)+1;
		else
			n = n>>1;
						/* 11 bits here (rounded) */
		n <<= 2;		/* 13 bits here (as in real chip) */
		TL_TAB[ x*2 + 0 ] = n;
		TL_TAB[ x*2 + 1 ] = -TL_TAB[ x*2 + 0 ];

		for (i=1; i<13; i++)
		{
			TL_TAB[ x*2+0 + i*2*TL_RES_LEN ] =  TL_TAB[ x*2+0 ]>>i;
			TL_TAB[ x*2+1 + i*2*TL_RES_LEN ] = -TL_TAB[ x*2+0 + i*2*TL_RES_LEN ];
		}
	#if 0
		if (errorlog)
		{
			fprintf(errorlog,"tl %04i", x);
			for (i=0; i<13; i++)
				fprintf(errorlog,", [%02i] %4i", i*2, TL_TAB[ x*2+1 + i*2*TL_RES_LEN ]);
			fprintf(errorlog,"\n");
		}
	#endif
	}
	/*if (errorlog) fprintf(errorlog,"TL_TAB_LEN = %i (%i bytes)\n",TL_TAB_LEN, (int)sizeof(TL_TAB));*/


	for (i=0; i<SIN_LEN; i++)
	{
		/* non-standard sinus */
		m = sin( ((i*2)+1) * PI / SIN_LEN ); /* checked against the real chip */

		/* we never reach zero here due to ((i*2)+1) */

		if (m>0.0)
			o = 8*log(1.0/m)/log(2);  /* convert to 'decibels' */
		else
			o = 8*log(-1.0/m)/log(2); /* convert to 'decibels' */

		o = o / (ENV_STEP/4);

		n = (int)(2.0*o);
		if (n&1)		/* round to closest */
			n = (n>>1)+1;
		else
			n = n>>1;

		sin_tab[ i ] = n*2 + (m>=0.0? 0: 1 );
		/*if (errorlog) fprintf(errorlog,"sin offs %04i= %i\n", i, sin_tab[i]);*/
	}

	/*if (errorlog) fprintf(errorlog,"ENV_QUIET= %08x\n",ENV_QUIET );*/


	/* calculate LFO AM waveforms*/
	for (x=0; x<4; x++)
	{
	    for (i=0; i<LFO_LEN; i++)
	    {
		switch (x)
		{
		case 0:	/* saw (255 down to 0) */
			m = 255 - (i/2);
			break;
		case 1: /* square (255,0) */
			if (i<256)
				m = 255;
			else
				m = 0;
			break;
		case 2: /* triangle (255 down to 0, up to 255) */
			if (i<256)
				m = 255 - i;
			else
				m = i - 256;
			break;
		case 3: /* random (range 0 to 255) */
			m = ((int)rand()) & 255;
			break;
		}
		/* we reach m = zero here !!!*/

		if (m>0.0)
			o = 8*log(255.0/m)/log(2);  /* convert to 'decibels' */
		else
		{
			if (m<0.0)
				o = 8*log(-255.0/m)/log(2); /* convert to 'decibels' */
			else
				o = 8*log(255.0/0.01)/log(2); /* small number */
		}

		o = o / (ENV_STEP/4);

		n = (int)(2.0*o);
		if (n&1)		/* round to closest */
			n = (n>>1)+1;
		else
			n = n>>1;

		lfo_tab[ x*LFO_LEN*2 + i*2 ] = n*2 + (m>=0.0? 0: 1 );
		/*if (errorlog) fprintf(errorlog,"lfo am waveofs[%i] %04i = %i\n", x, i*2, lfo_tab[ x*LFO_LEN*2 + i*2 ] );*/
	    }
	}
	for (i=0; i<128; i++)
	{
		m = i*2; /*m=0,2,4,6,8,10,..,252,254*/

		/* we reach m = zero here !!!*/

		if (m>0.0)
			o = 8*log(8192.0/m)/log(2);  /* convert to 'decibels' */
		else
			o = 8*log(8192.0/0.01)/log(2); /* small number (m=0)*/

		o = o / (ENV_STEP/4);

		n = (int)(2.0*o);
		if (n&1)		/* round to closest */
			n = (n>>1)+1;
		else
			n = n>>1;

		lfo_md_tab[ i ] = n*2;
		/*if (errorlog) fprintf(errorlog,"lfo_md_tab[%i](%i) = ofs %i shr by %i\n", i, i*2, (lfo_md_tab[i]>>1)&255, lfo_md_tab[i]>>9 );*/
	}

	/* calculate LFO PM waveforms*/
	for (x=0; x<4; x++)
	{
	    for (i=0; i<LFO_LEN; i++)
	    {
		switch (x)
		{
		case 0:	/* saw (0 to 127, -128 to -1) */
			if (i<256)
				m = (i/2);
			else
				m = (i/2)-256;
			break;
		case 1: /* square (127,-128) */
			if (i<256)
				m = 127;
			else
				m = -128;
			break;
		case 2: /* triangle (0 to 127,127 to -128,-127 to 0) */
			if (i<128)
				m = i; /*0 to 127*/
			else
			{
				if (i<384)
					m = 255 - i; /*127 down to -128*/
				else
					m = i - 511; /*-127 to 0*/
			}
			break;
		case 3: /* random (range -128 to 127) */
			m = ((int)rand()) & 255;
			m -=128;
			break;
		}
		/* we reach m = zero here !!!*/

		if (m>0.0)
			o = 8*log(127.0/m)/log(2);  /* convert to 'decibels' */
		else
		{
			if (m<0.0)
				o = 8*log(-128.0/m)/log(2); /* convert to 'decibels' */
			else
				o = 8*log(127.0/0.01)/log(2); /* small number */
		}

		o = o / (ENV_STEP/4);

		n = (int)(2.0*o);
		if (n&1)		/* round to closest */
			n = (n>>1)+1;
		else
			n = n>>1;

		lfo_tab[ x*LFO_LEN*2 + i*2 + 1 ] = n*2 + (m>=0.0? 0: 1 );
		/*if (errorlog) fprintf(errorlog,"lfo pm waveofs[%i] %04i = %i\n", x, i*2+1, lfo_tab[ x*LFO_LEN*2 + i*2 + 1 ] );*/
	    }
	}


	/* calculate D1L_tab table */
	for (i=0; i<16; i++)
	{
		m = (i<15?i:i+16) * (4.0/ENV_STEP);   /*every 3 'dB' except for all bits = 1 = 45dB+48dB*/
		D1L_tab[i] = m * (1<<ENV_SH);
		/*if (errorlog) fprintf(errorlog,"D1L_tab[%04x]=%08x\n",i,D1L_tab[i] );*/
	}

	/* calculate KC_TO_INDEX table */
	x=0;
	for (i=0; i<8*16; i++)
	{
		KC_TO_INDEX[i]=((i>>4)*12*64) + x*64 + 12*64;
		if ((i&0x03) != 0x03) x++;	/* change note code */
		if ((i&0x0f) == 0x0f) x=0;	/* new octave */
		/*if (errorlog) fprintf(errorlog,"KC_TO_INDEX[%02i] Note=%02i\n",i,KC_TO_INDEX[i]);*/
	}

#ifdef SAVE_SAMPLE
sample[8]=fopen("sampsum.raw","ab");
#endif
#ifdef SAVE_SEPARATE_CHANNELS
sample[0]=fopen("samp0.raw","wb");
sample[1]=fopen("samp1.raw","wb");
sample[2]=fopen("samp2.raw","wb");
sample[3]=fopen("samp3.raw","wb");
sample[4]=fopen("samp4.raw","wb");
sample[5]=fopen("samp5.raw","wb");
sample[6]=fopen("samp6.raw","wb");
sample[7]=fopen("samp7.raw","wb");
#endif
}


static void init_chip_tables(YM2151 *chip)
{
	int i,j;
	double mult,pom,pom2,clk,phaseinc,Hz;

	double scaler;	/* formula below is true for chip clock=3579545 */
	/* so we need to scale its output accordingly to the chip clock */

	scaler= (double)chip->clock / 3579545.0;

	/*this loop calculates Hertz values for notes from c-0 to b-7*/
	/*including 64 'cents' (100/64 that is 1.5625 of real cent) per note*/
	/* i*100/64/1200 is equal to i/768 */

	mult = (1<<FREQ_SH);
	for (i=0; i<1*12*64; i++)
	{
		/* 3.4375 Hz is note A; C# is 4 semitones higher */
		Hz = scaler * 3.4375 * pow (2, (i+4*64) / 768.0 );

		/* calculate phase increment */
		phaseinc = (Hz*SIN_LEN) / (double)chip->sampfreq;

		chip->freq[i] = phaseinc*mult;
		for (j=1; j<11; j++)
		{
			chip->freq[i+j*12*64] = chip->freq[i]*(1<<j);
		}
	#if 0
		if (errorlog)
		{
			pom = (double)chip->freq[i] / mult;
			pom = pom * (double)chip->sampfreq / (double)SIN_LEN;
			fprintf(errorlog,"freq[%04i][%08x]= real %20.15f Hz  emul %20.15f Hz\n", i, chip->freq[i], Hz, pom);
		}
	#endif
	}

	mult = (1<<FREQ_SH);
	for (j=0; j<4; j++)
	{
		for (i=0; i<32; i++)
		{
			int x, y, mul;

			Hz = ( (double)DT1_tab[j*32+i] * ((double)chip->clock/8.0) ) / (double)(1<<24);
			/*Important note:
			**  The frequency calculated above is HALF of the frequency found in the Manual.
			**  This is intentional as Manual gives the frequencies for MUL = 1, not MUL = 0.5
			*/
			/*calculate phase increment*/
			phaseinc = (Hz*SIN_LEN) / (double)chip->sampfreq;

			/*positive and negative values*/
			chip->DT1freq[ (j+0)*16*32 + i ] = phaseinc * mult;
			chip->DT1freq[ (j+4)*16*32 + i ] = -chip->DT1freq[ (j+0)*16*32 + i ];

			for (mul=1; mul<16; mul++)
			{
				x = (j+0)*16*32 + mul*32;
				y = (j+4)*16*32 + mul*32;
				/*positive and negative values*/
				chip->DT1freq[ x + i ] = phaseinc * mult * (mul*2);
				chip->DT1freq[ y + i ] = -chip->DT1freq[ x + i ];
			}
		#if 0
			if (errorlog)
			{
				for (mul=0; mul<16; mul++)
				{
					x = j*16*32 + mul*32 + i;
					pom = (double)chip->DT1freq[x] / mult;
					pom = pom * (double)chip->sampfreq / (double)SIN_LEN;
					fprintf(errorlog,"DT1(%03i)[%02i %02i mul=%02i][%08x]= real %19.15f Hz  emul %19.15f Hz\n",
							 x, j, i, mul, chip->DT1freq[x], Hz*((mul>0)?mul*2:1), pom);
				}
			}
		#endif
		}
	}

	mult = (1<<LFO_SH);
	clk  = (double)chip->clock;
	for (i=0; i<256; i++)
	{
		j = i & 0x0f;
		pom = fabs(  (clk/65536/(1<<(i/16)) ) - (clk/65536/32/(1<<(i/16)) * (j+1)) );

		/*calculate phase increment*/
		chip->LFOfreq[0xff-i] = ( (pom*LFO_LEN) / (double)chip->sampfreq ) * mult; /*fixed point*/
		/*if (errorlog) fprintf(errorlog, "LFO[%02x] (%08x)= real %20.15f Hz  emul %20.15f Hz\n",0xff-i, chip->LFOfreq[0xff-i], pom,
			(((double)chip->LFOfreq[0xff-i] / mult) * (double)chip->sampfreq ) / (double)LFO_LEN );*/
	}

	for (i=0; i<34; i++)
		chip->EG_tab[i] = 0;		/* infinity */

	for (i=2; i<64; i++)
	{
		pom2 = (double)chip->clock / (double)chip->sampfreq;
		if (i<60) pom2 *= ( 1 + (i&3)*0.25 );
		pom2 *= 1<<((i>>2));
		pom2 /= 768.0 * 1024.0;
		pom2 *= (double)(1<<ENV_SH);
		chip->EG_tab[32+i] = pom2;
	#if 0
		if (errorlog)
			fprintf(errorlog,"Rate %2i %1i  Decay [real %11.4f ms][emul %11.4f ms][d=%08x]\n",i>>2, i&3,
			( ((double)(ENV_LEN<<ENV_SH)) / pom2 )                       * (1000.0 / (double)chip->sampfreq),
			( ((double)(ENV_LEN<<ENV_SH)) / (double)chip->EG_tab[32+i] ) * (1000.0 / (double)chip->sampfreq), chip->EG_tab[32+i] );
	#endif
	}

	for (i=0; i<32; i++)
	{
		chip->EG_tab[ 32+64+i ] = chip->EG_tab[32+63];
	}

	/* precalculate timers' deltas */
	/* User's Manual pages 15,16  */
	mult = (1<<TIMER_SH);
	for (i=0; i<1024; i++)
	{
		/* ASG 980324: changed to compute both TimerA and TimerATime */
		pom= ( 64.0  *  (1024.0-i) / (double)chip->clock );
		#ifdef USE_MAME_TIMERS
			chip->TimerATime[i] = pom;
		#else
			chip->TimerA[i] = pom * (double)chip->sampfreq * mult;  /*number of samples that timer period takes (fixed point) */
		#endif
	}
	for (i=0; i<256; i++)
	{
		/* ASG 980324: changed to compute both TimerB and TimerBTime */
		pom= ( 1024.0 * (256.0-i)  / (double)chip->clock );
		#ifdef USE_MAME_TIMERS
			chip->TimerBTime[i] = pom;
		#else
			chip->TimerB[i] = pom * (double)chip->sampfreq * mult;  /*number of samples that timer period takes (fixed point) */
		#endif
	}

}


/*#define RESET_FEEDBACK_ON_KEYON*/

INLINE void envelope_KONKOFF(OscilRec * op, int v)
{
	if (v&0x08)
	{
		if (!op->key)
		{
			op->key   = 1;      /*KEYON'ed*/
			op->phase = 0;      /*clear phase */
			op->state = EG_ATT; /*KEY ON = attack*/
		}
	}
	else
	{
		if (op->key)
		{
			op->key   = 0;      /*KEYOFF'ed*/
			if (op->state>EG_REL)
				op->state = EG_REL; /*release*/
		}
	}

	op+=8;

	if (v&0x20)
	{
		if (!op->key)
		{
			op->key   = 1;
			op->phase = 0;
			op->state = EG_ATT;
		}
	}
	else
	{
		if (op->key)
		{
			op->key   = 0;
			if (op->state>EG_REL)
				op->state = EG_REL;
		}
	}

	op+=8;

	if (v&0x10)
	{
		if (!op->key)
		{
			op->key   = 1;
			op->phase = 0;
			op->state = EG_ATT;
		}
	}
	else
	{
		if (op->key)
		{
			op->key   = 0;
			if (op->state>EG_REL)
				op->state = EG_REL;
		}
	}

	op+=8;

	if (v&0x40)
	{
		if (!op->key)
		{
			op->key   = 1;
			op->phase = 0;
			op->state = EG_ATT;
		}
	}
	else
	{
		if (op->key)
		{
			op->key   = 0;
			if (op->state>EG_REL)
				op->state = EG_REL;
		}
	}
}


#ifdef USE_MAME_TIMERS
static void timer_callback_a (int n)
{
	YM2151 *chip = &YMPSG[n];
	chip->TimATimer = timer_set (chip->TimerATime[ chip->TimAIndex ], n, timer_callback_a);
	chip->TimAOldIndex = chip->TimAIndex;
	if (chip->IRQenable & 0x04)
	{
		int oldstate = chip->status & 3;
		chip->status |= 1;
		if ((!oldstate) && (chip->irqhandler)) (*chip->irqhandler)(1);
	}
}
static void timer_callback_b (int n)
{
	YM2151 *chip = &YMPSG[n];
	chip->TimBTimer = timer_set (chip->TimerBTime[ chip->TimBIndex ], n, timer_callback_b);
	chip->TimBOldIndex = chip->TimBIndex;
	if (chip->IRQenable & 0x08)
	{
		int oldstate = chip->status & 3;
		chip->status |= 2;
		if ((!oldstate) && (chip->irqhandler)) (*chip->irqhandler)(1);
	}
}
#if 0
static void timer_callback_chip_write_busy (int n)
{
	YM2151 *chip = &YMPSG[n];
	chip->status &= 0x7f;	/*reset busy flag*/
}
#endif
#endif


INLINE void set_connect( OscilRec *om1, int v, int cha)
{
	OscilRec *om2 = om1+8;
	OscilRec *oc1 = om1+16;
	/*OscilRec *oc2 = om1+24;*/
	/*oc2->connect = &chanout[cha];*/

	/* set connect algorithm */

	switch( v & 7 )
	{
	case 0:
		/* M1---C1---M2---C2---OUT */
		om1->connect = &c1;
		oc1->connect = &m2;
		om2->connect = &c2;
		break;
	case 1:
		/* M1-+-M2---C2---OUT */
		/* C1-+               */
		om1->connect = &m2;
		oc1->connect = &m2;
		om2->connect = &c2;
		break;
	case 2:
		/* M1------+-C2---OUT */
		/* C1---M2-+          */
		om1->connect = &c2;
		oc1->connect = &m2;
		om2->connect = &c2;
		break;
	case 3:
		/* M1---C1-+-C2---OUT */
		/* M2------+          */
		om1->connect = &c1;
		oc1->connect = &c2;
		om2->connect = &c2;
		break;
	case 4:
		/* M1---C1-+--OUT */
		/* M2---C2-+      */
		om1->connect = &c1;
		oc1->connect = &chanout[cha];
		om2->connect = &c2;
		break;
	case 5:
		/*    +-C1-+     */
		/* M1-+-M2-+-OUT */
		/*    +-C2-+     */
		om1->connect = 0;	/* special mark */
		oc1->connect = &chanout[cha];
		om2->connect = &chanout[cha];
		break;
	case 6:
		/* M1---C1-+     */
		/*      M2-+-OUT */
		/*      C2-+     */
		om1->connect = &c1;
		oc1->connect = &chanout[cha];
		om2->connect = &chanout[cha];
		break;
	case 7:
		/* M1-+     */
		/* C1-+-OUT */
		/* M2-+     */
		/* C2-+     */
		om1->connect = &chanout[cha];
		oc1->connect = &chanout[cha];
		om2->connect = &chanout[cha];
		break;
	}
}


INLINE void refresh_EG( YM2151 *chip, OscilRec * op)
{
	unsigned int kc;
	unsigned int v;

	kc = op->KC;

	/*v = 32 + 2*RATE + RKS (max 126)*/

	v = kc >> op->KS;
	if ((op->AR+v) < 32+62)
		op->delta_AR  = chip->EG_tab[ op->AR + v];
	else
		op->delta_AR  = MAX_ATT_INDEX+1;
	op->delta_D1R = chip->EG_tab[op->D1R + v];
	op->delta_D2R = chip->EG_tab[op->D2R + v];
	op->delta_RR  = chip->EG_tab[ op->RR + v];

	op+=8;

	v = kc >> op->KS;
	if ((op->AR+v) < 32+62)
		op->delta_AR  = chip->EG_tab[ op->AR + v];
	else
		op->delta_AR  = MAX_ATT_INDEX+1;
	op->delta_D1R = chip->EG_tab[op->D1R + v];
	op->delta_D2R = chip->EG_tab[op->D2R + v];
	op->delta_RR  = chip->EG_tab[ op->RR + v];

	op+=8;

	v = kc >> op->KS;
	if ((op->AR+v) < 32+62)
		op->delta_AR  = chip->EG_tab[ op->AR + v];
	else
		op->delta_AR  = MAX_ATT_INDEX+1;
	op->delta_D1R = chip->EG_tab[op->D1R + v];
	op->delta_D2R = chip->EG_tab[op->D2R + v];
	op->delta_RR  = chip->EG_tab[ op->RR + v];

	op+=8;

	v = kc >> op->KS;
	if ((op->AR+v) < 32+62)
		op->delta_AR  = chip->EG_tab[ op->AR + v];
	else
		op->delta_AR  = MAX_ATT_INDEX+1;
	op->delta_D1R = chip->EG_tab[op->D1R + v];
	op->delta_D2R = chip->EG_tab[op->D2R + v];
	op->delta_RR  = chip->EG_tab[ op->RR + v];

}


/* write a register on YM2151 chip number 'n' */
void YM2151WriteReg(int n, int r, int v)
{
	YM2151 *chip = &(YMPSG[n]);
	OscilRec *op = &chip->Oscils[ r&0x1f ];

	/*adjust bus to 8 bits*/
	r &= 0xff;
	v &= 0xff;

#if 0
	/*There's no info on what YM2151 really does when busy flag is set*/
	if ( chip->status & 0x80 ) return;
	timer_set ( 68.0 / (double)chip->clock, n, timer_callback_chip_write_busy);
	chip->status |= 0x80;    /* set busy flag for 68 chip clock cycles */
#endif

#ifdef LOG_CYM_FILE
	if ((cymfile) && (r!=0) )
	{
		fputc( (unsigned char)r, cymfile );
		fputc( (unsigned char)v, cymfile );
	}
#endif

	switch(r & 0xe0){
	case 0x00:
		switch(r){
		case 0x01: /*LFO reset(bit 1), Test Register (other bits)*/
			chip->test = v;
			if (v&2) chip->LFOphase = 0;
			break;

		case 0x08:
			envelope_KONKOFF(&chip->Oscils[ v&7 ], v );
			break;

		case 0x0f: /*noise mode enable, noise freq*/
			chip->noise = v;
			/*if ((v&0x80)) printf("YM2151 noise (%02x)\n",v);*/
			#if 0
			if (v){
				char t[80];
				sprintf(t,"YM2151 noise mode = %02x",v);
				usrintf_showmessage(t);
				if (errorlog) fprintf(errorlog,"YM2151 noise (%02x)\n",v);
			}
			#endif
			break;

		case 0x10: /*timer A hi*/
			chip->TimAIndex = (chip->TimAIndex & 0x003) | (v<<2);
			break;

		case 0x11: /*timer A low*/
			chip->TimAIndex = (chip->TimAIndex & 0x3fc) | (v & 3);
			break;

		case 0x12: /*timer B*/
			chip->TimBIndex = v;
			break;

		case 0x14: /*CSM, irq flag reset, irq enable, timer start/stop*/
			/*if ((v&0x80)) printf("\nYM2151 CSM MODE ON\n");*/

			chip->IRQenable = v;	/*bit 3-timer B, bit 2-timer A*/

			if (v&0x20)	/*reset timer B irq flag*/
			{
				int oldstate = chip->status & 3;
				chip->status &= 0xfd;
				if ((oldstate==2) && (chip->irqhandler)) (*chip->irqhandler)(0);
			}

			if (v&0x10)	/*reset timer A irq flag*/
			{
				int oldstate = chip->status & 3;
				chip->status &= 0xfe;
				if ((oldstate==1) && (chip->irqhandler)) (*chip->irqhandler)(0);

			}

			if (v&0x02){	/*load and start timer B*/
				#ifdef USE_MAME_TIMERS
				/* ASG 980324: added a real timer */
				/* start timer _only_ if it wasn't already started (it will reload time value next round)*/
					if (!chip->TimBTimer)
					{
						chip->TimBTimer = timer_set (chip->TimerBTime[ chip->TimBIndex ], n, timer_callback_b);
						chip->TimBOldIndex = chip->TimBIndex;
					}
					#if 0
					else
					{
						/*check if timer value has changed since last start and update if necessary*/
						if (chip->TimBIndex!=chip->TimBOldIndex)
						{
							double timepassed = timer_timeelapsed(chip->TimBTimer);
							timer_remove (chip->TimBTimer);
							chip->TimBTimer = timer_set (chip->TimerBTime[ chip->TimBIndex ] - timepassed, n, timer_callback_b);
							chip->TimBOldIndex = chip->TimBIndex;
						}
					}
					#endif
				#else
					if (!chip->TimB)
					{
						chip->TimB = 1;
						chip->TimBVal = chip->TimerB[ chip->TimBIndex ];
					}
				#endif
			}else{		/*stop timer B*/
				#ifdef USE_MAME_TIMERS
				/* ASG 980324: added a real timer */
					if (chip->TimBTimer) timer_remove (chip->TimBTimer);
					chip->TimBTimer = 0;
				#else
					chip->TimB = 0;
				#endif
			}

			if (v&0x01){	/*load and start timer A*/
				#ifdef USE_MAME_TIMERS
				/* ASG 980324: added a real timer */
				/* start timer _only_ if it wasn't already started (it will reload time value next round)*/
					if (!chip->TimATimer)
					{
						chip->TimATimer = timer_set (chip->TimerATime[ chip->TimAIndex ], n, timer_callback_a);
						chip->TimAOldIndex = chip->TimAIndex;
					}
					#if 0
					else
					{
						/*check if timer value has changed since last start and update if necessary*/
						if (chip->TimAIndex!=chip->TimAOldIndex)
						{
							double timepassed = timer_timeelapsed(chip->TimATimer);
							timer_remove (chip->TimATimer);
							chip->TimATimer = timer_set (chip->TimerATime[ chip->TimAIndex ] - timepassed, n, timer_callback_a);
							chip->TimAOldIndex = chip->TimAIndex;
						}
					}
					#endif
				#else
					if (!chip->TimA)
					{
						chip->TimA = 1;
						chip->TimAVal = chip->TimerA[ chip->TimAIndex ];
					}
				#endif
			}else{		/*stop timer A*/
				#ifdef USE_MAME_TIMERS
				/* ASG 980324: added a real timer */
					if (chip->TimATimer) timer_remove (chip->TimATimer);
					chip->TimATimer = 0;
				#else
					chip->TimA = 0;
				#endif
			}
			break;

		case 0x18: /*LFO frequency*/
			chip->LFOfrq = chip->LFOfreq[ v ];
			break;

		case 0x19: /*PMD (bit 7==1) or AMD (bit 7==0)*/
			if (v&0x80)
				chip->PMD = lfo_md_tab[ v&0x7f ] + 512;
			else
				chip->AMD = lfo_md_tab[ v&0x7f ];
			break;

		case 0x1b: /*CT2, CT1, LFO waveform*/
			chip->CT = v;
			chip->LFOwave = (v & 3) * LFO_LEN*2;
			if (chip->porthandler) (*chip->porthandler)(0 , (chip->CT) >> 6 );
			break;

		default:
			if (errorlog) fprintf(errorlog,"YM2151 Write %02x to undocumented register #%02x\n",v,r);
			break;
		}
		break;

	case 0x20:
		op = &chip->Oscils[r & 7];
		switch(r & 0x18){
		case 0x00: /*RL enable, Feedback, Connection */
			op->FeedBack = ((v>>3)&7) ? ((v>>3)&7)+6:0;
			chip->PAN[ (r&7)*2    ] = (v & 0x40) ? 0xffffffff : 0x0;
			chip->PAN[ (r&7)*2 +1 ] = (v & 0x80) ? 0xffffffff : 0x0;
			set_connect(op, v, r&7);
			break;

		case 0x08: /*Key Code*/
			v &= 0x7f;
			/*v++;*/
			if (v != op->KC)
			{
				unsigned int kc,kc_channel;

				kc = KC_TO_INDEX[v];
				(op+0)->KC = v;
				(op+0)->KCindex = kc;
				(op+8)->KC = v;
				(op+8)->KCindex = kc;
				(op+16)->KC = v;
				(op+16)->KCindex = kc;
				(op+24)->KC = v;
				(op+24)->KCindex = kc;

				kc_channel = op->KCindex + op->KF;
				kc = v>>2;

				(op+0)->freq = chip->freq[ kc_channel + (op+0)->DT2 ] * (op+0)->MUL;
				(op+0)->DTfreq = chip->DT1freq[ (op+0)->DT1 + kc ];
				(op+8)->freq = chip->freq[ kc_channel + (op+8)->DT2 ] * (op+8)->MUL;
				(op+8)->DTfreq = chip->DT1freq[ (op+8)->DT1 + kc ];
				(op+16)->freq = chip->freq[ kc_channel + (op+16)->DT2 ] * (op+16)->MUL;
				(op+16)->DTfreq = chip->DT1freq[ (op+16)->DT1 + kc ];
				(op+24)->freq = chip->freq[ kc_channel + (op+24)->DT2 ] * (op+24)->MUL;
				(op+24)->DTfreq = chip->DT1freq[ (op+24)->DT1 + kc ];

				refresh_EG( chip, op );
			}
			break;

		case 0x10: /*Key Fraction*/
			v >>= 2;
			if (v != op->KF)
			{
				unsigned int kc_channel;

				(op+0)->KF = v;
				(op+8)->KF = v;
				(op+16)->KF = v;
				(op+24)->KF = v;

				kc_channel = op->KCindex + op->KF;

				(op+0)->freq = chip->freq[ kc_channel + (op+0)->DT2 ] * (op+0)->MUL;
				(op+8)->freq = chip->freq[ kc_channel + (op+8)->DT2 ] * (op+8)->MUL;
				(op+16)->freq = chip->freq[ kc_channel + (op+16)->DT2 ] * (op+16)->MUL;
				(op+24)->freq = chip->freq[ kc_channel + (op+24)->DT2 ] * (op+24)->MUL;
			}
			break;

		case 0x18: /*PMS,AMS*/
			op->PMS = (v>>4) & 7;
			op->AMS = v & 3;
			break;
		}
		break;

	case 0x40: /*DT1, MUL*/
		{
			unsigned int oldDT1 = op->DT1;
			unsigned int oldMUL = op->MUL;
			op->DT1 = (v&0x7f)<<5;
			op->MUL = (v&0x0f) ? (v&0x0f)<<1: 1;
			if (oldDT1 != op->DT1)
			{
				op->DTfreq = chip->DT1freq[ op->DT1 + (op->KC>>2) ];
			}
			if (oldMUL != op->MUL)
			{
				unsigned int kc_channel;

				kc_channel = op->KCindex + op->KF;
				op->freq = chip->freq[ kc_channel + op->DT2 ] * op->MUL;
			}
		}
		break;

	case 0x60: /*TL*/
		op->TL = (v&0x7f)<<(ENV_BITS-7); /*7bit TL*/
		break;

	case 0x80: /*KS, AR*/
		{
			int oldKS = op->KS;
			int oldAR = op->AR;
			op->KS = 5-(v>>6);
			op->AR = (v&0x1f) ? 32 + ((v&0x1f)<<1) : 0;

			if ( (op->AR != oldAR) || (op->KS != oldKS) )
			{
				if ((op->AR + (op->KC>>op->KS)) < 32+62)
					op->delta_AR  = chip->EG_tab[op->AR  + (op->KC>>op->KS) ];
				else
					op->delta_AR  = MAX_ATT_INDEX+1;
			}

			if (op->KS != oldKS)
			{
				op->delta_D1R = chip->EG_tab[op->D1R + (op->KC>>op->KS) ];
				op->delta_D2R = chip->EG_tab[op->D2R + (op->KC>>op->KS) ];
				op->delta_RR  = chip->EG_tab[op->RR  + (op->KC>>op->KS) ];
			}
		}
		break;

	case 0xa0: /*AMS-EN, D1R*/
		op->AMSmask = (v&0x80) ? 0xffffffff : 0;
		op->D1R     = (v&0x1f) ? 32 + ((v&0x1f)<<1) : 0;
		op->delta_D1R = chip->EG_tab[op->D1R + (op->KC>>op->KS) ];
		break;

	case 0xc0: /*DT2, D2R*/
		{
			unsigned int oldDT2 = op->DT2;
			op->DT2 = DT2_tab[ v>>6 ];
			if (op->DT2 != oldDT2)
			{
				unsigned int kc_channel;

				kc_channel = op->KCindex + op->KF;
				op->freq = chip->freq[ kc_channel + op->DT2 ] * op->MUL;
			}
		}
		op->D2R = (v&0x1f) ? 32 + ((v&0x1f)<<1) : 0;
		op->delta_D2R = chip->EG_tab[op->D2R + (op->KC>>op->KS) ];
		break;

	case 0xe0: /*D1L, RR*/
		op->D1L = D1L_tab[ v>>4 ];
		op->RR  = 34 + ((v&0x0f)<<2);
		op->delta_RR  = chip->EG_tab[op->RR  + (op->KC>>op->KS) ];
		break;
	}
}


#ifdef LOG_CYM_FILE
static void cymfile_callback (int n)
{
	if (cymfile)
	{
		fputc( (unsigned char)0, cymfile );
	}
}
#endif


int YM2151ReadStatus( int n )
{
	return YMPSG[n].status;
}


/*
** Initialize YM2151 emulator(s).
**
** 'num' is the number of virtual YM2151's to allocate
** 'clock' is the chip clock in Hz
** 'rate' is sampling rate
*/
int YM2151Init(int num, int clock, int rate)
{
	int i;

	if (YMPSG) return (-1);	/* duplicate init. */

	YMNumChips = num;

	YMPSG = (YM2151 *)malloc(sizeof(YM2151) * YMNumChips);
	if (YMPSG == NULL) return (1);
#if 0
	TL_TAB = (signed int *)malloc(sizeof(signed int) * TL_TAB_LEN );
	if (TL_TAB == NULL)
	{
		free(YMPSG);
		YMPSG=NULL;
		return (1);
	}
#endif

#ifdef FM_EMU
	errorlog=fopen("errorlog.txt","wb");
#endif

	init_tables();
	for (i=0 ; i<YMNumChips; i++)
	{
		YMPSG[i].clock = clock;
		/*rate = clock/64;*/
		YMPSG[i].sampfreq = rate ? rate : 44100;	/* avoid division by 0 in init_chip_tables() */
		YMPSG[i].irqhandler = NULL;					/*interrupt handler */
		YMPSG[i].porthandler = NULL;				/*port write handler*/
		init_chip_tables(&YMPSG[i]);
		YM2151ResetChip(i);
		/*if (errorlog) fprintf(errorlog,"YM2151[init] clock=%i sampfreq=%i\n", YMPSG[i].clock, YMPSG[i].sampfreq);*/
	}

#ifdef LOG_CYM_FILE
	cymfile = fopen("2151_.cym","wb");
	if (cymfile)
		cymfiletimer = timer_pulse ( TIME_IN_HZ(110), 0, cymfile_callback); /*110 Hz pulse timer*/
	else
    		if (errorlog) fprintf(errorlog,"Could not create file 2151_.cym\n");
#endif

	return(0);
}

void YM2151Shutdown()
{
	if (!YMPSG) return;

	free(YMPSG);
	YMPSG = NULL;
#if 0
	if (TL_TAB)
	{
		free(TL_TAB);
		TL_TAB=NULL;
	}
#endif

#ifdef FM_EMU
	fclose(errorlog);
	errorlog = NULL;
#endif

#ifdef LOG_CYM_FILE
	fclose(cymfile);
	cymfile = NULL;
	if (cymfiletimer) timer_remove (cymfiletimer);
	cymfiletimer = 0;
#endif

#ifdef SAVE_SAMPLE
	fclose(sample[8]);
#endif
#ifdef SAVE_SEPARATE_CHANNELS
	fclose(sample[0]);
	fclose(sample[1]);
	fclose(sample[2]);
	fclose(sample[3]);
	fclose(sample[4]);
	fclose(sample[5]);
	fclose(sample[6]);
	fclose(sample[7]);
#endif
}


/*
** reset all chip registers.
*/
void YM2151ResetChip(int num)
{
	int i;
	YM2151 *chip = &YMPSG[num];

	/* initialize hardware registers */

	for (i=0; i<32; i++)
	{
		memset(&chip->Oscils[i],'\0',sizeof(OscilRec));
		chip->Oscils[i].volume = MAX_ATT_INDEX;
	}

	chip->LFOphase = 0;
	chip->LFOfrq   = 0;
	chip->LFOwave  = 0;
	chip->PMD = lfo_md_tab[ 0 ]+512;
	chip->AMD = lfo_md_tab[ 0 ];
	chip->LFA = 0;
	chip->LFP = 0;

	chip->test= 0;

	chip->IRQenable = 0;
#ifdef USE_MAME_TIMERS
	/* ASG 980324 -- reset the timers before writing to the registers */
	chip->TimATimer = 0;
	chip->TimBTimer = 0;
#else
	chip->TimA      = 0;
	chip->TimB      = 0;
	chip->TimAVal   = 0;
	chip->TimBVal   = 0;
#endif
	chip->TimAIndex = 0;
	chip->TimBIndex = 0;
	chip->TimAOldIndex = 0;
	chip->TimBOldIndex = 0;

	chip->noise     = 0;

	chip->status    = 0;

	YM2151WriteReg(num, 0x1b, 0); /*only because of CT1, CT2 output pins*/
	for (i=0x20; i<0x100; i++)   /*just to set the PM operators */
	{
		YM2151WriteReg(num, i, 0);
	}

}


INLINE void lfo_calc(void)
{
unsigned int phase, lfx;

	if (PSG->test&2)
	{
		PSG->LFOphase = 0;
		phase = PSG->LFOwave;
	}
	else
	{
		phase = (PSG->LFOphase>>LFO_SH) & LFO_MASK;
		phase = phase*2 + PSG->LFOwave;
	}

	lfx = lfo_tab[phase] + PSG->AMD;

	PSG->LFA = 0;
	if (lfx < TL_TAB_LEN)
		PSG->LFA = TL_TAB[ lfx ];

	lfx = lfo_tab[phase+1] + PSG->PMD;

	PSG->LFP = 0;
	if (lfx < TL_TAB_LEN)
		PSG->LFP = TL_TAB[ lfx ];
}


INLINE void calc_lfo_pm(OscilRec *op)
{
signed int mod_ind,pom;

	mod_ind = PSG->LFP; /* -128..+127 (8bits signed)*/
	if (op->PMS < 6)
		mod_ind >>= (6 - op->PMS);
	else
		mod_ind <<= (op->PMS - 5);

	if (mod_ind)
	{
		unsigned int kc_channel;

		kc_channel = op->KCindex + op->KF + mod_ind;

		pom = PSG->freq[ kc_channel + op->DT2 ] * op->MUL;
		op->phase += (pom - op->freq);

		op+=8;
		pom = PSG->freq[ kc_channel + op->DT2 ] * op->MUL;
		op->phase += (pom - op->freq);

		op+=8;
		pom = PSG->freq[ kc_channel + op->DT2 ] * op->MUL;
		op->phase += (pom - op->freq);

		op+=8;
		pom = PSG->freq[ kc_channel + op->DT2 ] * op->MUL;
		op->phase += (pom - op->freq);
	}
}


INLINE signed int op_calc(OscilRec * OP, unsigned int env, signed int pm)
{
	unsigned int p;

	p = (env<<3) + sin_tab[ ( ((signed int)((OP->phase & ~FREQ_MASK) + (pm<<15))) >> FREQ_SH ) & SIN_MASK ];

	if (p >= TL_TAB_LEN)
		return 0;

	return TL_TAB[p];
}

INLINE signed int op_calc1(OscilRec * OP, unsigned int env, signed int pm)
{
	unsigned int p;
	signed int i;

	i = (OP->phase & ~FREQ_MASK) + pm;

/*if (errorlog) fprintf(errorlog,"i=%08x (i>>16)&511=%8i phase=%i [pm=%08x] ",i, (i>>16)&511, OP->phase>>FREQ_SH, pm);*/

	p = (env<<3) + sin_tab[ (i>>FREQ_SH) & SIN_MASK];

/*if (errorlog) fprintf(errorlog," (p&255=%i p>>8=%i) out= %i\n", p&255,p>>8, TL_TAB[p&255]>>(p>>8) );*/

	if (p >= TL_TAB_LEN)
		return 0;

	return TL_TAB[p];
}


#define volume_calc(OP) (OP->TL + (((unsigned int)OP->volume)>>ENV_SH) + (AM & OP->AMSmask))

INLINE void chan_calc(unsigned int chan)
{
OscilRec *OP;
unsigned int env;
unsigned int AM;

	chanout[chan]= c1 = m2 = c2 = 0;
	AM = 0;

	OP = &PSG->Oscils[chan]; /*M1*/

	if (OP->AMS)
		AM = PSG->LFA << (OP->AMS-1);

	if (OP->PMS)
		calc_lfo_pm(OP);

	env = volume_calc(OP);
	{
		signed int out;

		out = OP->FB0 + OP->FB;
		OP->FB0 = OP->FB;

		if (!OP->connect)
			/* algorithm 5 */
			c1 = m2 = c2 = OP->FB0;
		else
			/* other algorithms */
			*OP->connect = OP->FB0;

		OP->FB = 0;

		if (env < ENV_QUIET)
			OP->FB = op_calc1(OP, env, (out<<OP->FeedBack) );
	}

	OP += 16; /*C1*/
	env = volume_calc(OP);
	if (env < ENV_QUIET)
		*OP->connect += op_calc(OP, env, c1);

	OP -= 8;  /*M2*/
	env = volume_calc(OP);
	if (env < ENV_QUIET)
		*OP->connect += op_calc(OP, env, m2);

	OP += 16; /*C2*/
	env = volume_calc(OP);
	if (env < ENV_QUIET)
		chanout[chan] += op_calc(OP, env, c2);

}

INLINE void advance(void)
{
OscilRec *op;
int i;

    if (!(PSG->test&2))
        PSG->LFOphase += PSG->LFOfrq;

    op = &PSG->Oscils[0]; /*CH0 M1*/
    i = 32;
    do
    {
        op->phase += op->freq;
        op->phase += op->DTfreq;

	switch(op->state)
	{
	case EG_ATT:	/*attack phase*/
		{
			signed int step;

			step = op->volume;
			op->volume -= op->delta_AR;
			step = (step>>ENV_SH) - (((unsigned int)op->volume)>>ENV_SH); /*number of levels passed since last time*/
			if (step > 0)
			{
				signed int tmp_volume;

				tmp_volume = op->volume + (step<<ENV_SH); /*adjust by number of levels*/
				do
				{
					tmp_volume = tmp_volume - (1<<ENV_SH) - ((tmp_volume>>4) & ~ENV_MASK);
					if (tmp_volume <= MIN_ATT_INDEX)
						break;
					step--;
				}while(step);
				op->volume = tmp_volume;
			}

			if (op->volume <= MIN_ATT_INDEX)
			{
				if (op->volume < 0)
					op->volume = 0; /*this is not quite correct (checked)*/
				op->state = EG_DEC;
			}
		}
		break;

	case EG_DEC:	/*decay phase*/
		if ( (op->volume += op->delta_D1R) >= op->D1L )
		{
			op->volume = op->D1L; /*this is not quite correct (checked)*/
			op->state = EG_SUS;
		}
		break;

	case EG_SUS:	/*sustain phase*/
		if ( (op->volume += op->delta_D2R) > MAX_ATT_INDEX )
		{
			op->state = EG_OFF;
			op->volume = MAX_ATT_INDEX;
		}
		break;

	case EG_REL:	/*release phase*/
		if ( (op->volume += op->delta_RR) > MAX_ATT_INDEX )
		{
			op->state = EG_OFF;
			op->volume = MAX_ATT_INDEX;
		}
		break;
	}
	op++;
	i--;
    }while (i);
}

INLINE signed int acc_calc(signed int value)
{
	if (value>=0)
	{
		if (value < 0x0200)
			return (value);
		if (value < 0x0400)
			return (value & 0xfffffffe);
		if (value < 0x0800)
			return (value & 0xfffffffc);
		if (value < 0x1000)
			return (value & 0xfffffff8);
		if (value < 0x2000)
			return (value & 0xfffffff0);
		if (value < 0x4000)
			return (value & 0xffffffe0);
		return (value & 0xffffffc0);
	}
	/*else value < 0*/
	if (value > -0x0200)
		return (value);
	if (value > -0x0400)
		return (value & 0xfffffffe);
	if (value > -0x0800)
		return (value & 0xfffffffc);
	if (value > -0x1000)
		return (value & 0xfffffff8);
	if (value > -0x2000)
		return (value & 0xfffffff0);
	if (value > -0x4000)
		return (value & 0xffffffe0);
	return (value & 0xffffffc0);

}

#ifdef SAVE_SEPARATE_CHANNELS
  #define SAVE_SINGLE_CHANNEL(j) \
  {	signed int pom= -(chanout[j] & PSG->PAN[j*2]); \
	if (pom > 32767) pom = 32767; else if (pom < -32768) pom = -32768; \
	fputc((unsigned short)pom&0xff,sample[j]); \
	fputc(((unsigned short)pom>>8)&0xff,sample[j]);  }
#else
  #define SAVE_SINGLE_CHANNEL(j)
#endif

/*first macro saves left and right channels to mono file*/
/*second macro saves left and right channels to stereo file*/
#if 1	/*MONO*/
	#ifdef SAVE_SAMPLE
	  #define SAVE_ALL_CHANNELS \
	  {	signed int pom = outr; \
		pom >>= FINAL_SH; \
		if (pom > 32767) pom = 32767; else if (pom < -32768) pom = -32768; \
		/*pom = acc_calc(pom);*/ \
		/*fprintf(sample[8]," %i\n",pom);*/ \
		fputc((unsigned short)pom&0xff,sample[8]); \
		fputc(((unsigned short)pom>>8)&0xff,sample[8]); \
	  }
	#else
	  #define SAVE_ALL_CHANNELS
	#endif
#else	/*STEREO*/
	#ifdef SAVE_SAMPLE
	  #define SAVE_ALL_CHANNELS \
	  {	signed int pom = outl; \
		pom >>= FINAL_SH; \
		if (pom > 32767) pom = 32767; else if (pom < -32768) pom = -32768; \
		fputc((unsigned short)pom&0xff,sample[8]); \
		fputc(((unsigned short)pom>>8)&0xff,sample[8]); \
		pom = outr; \
		pom >>= FINAL_SH; \
		if (pom > 32767) pom = 32767; else if (pom < -32768) pom = -32768; \
		fputc((unsigned short)pom&0xff,sample[8]); \
		fputc(((unsigned short)pom>>8)&0xff,sample[8]); \
	  }
	#else
	  #define SAVE_ALL_CHANNELS
	#endif
#endif


/*
** Generate samples for one of the YM2151's
**
** 'num' is the number of virtual YM2151
** '**buffers' is table of pointers to the buffers: left and right
** 'length' is the number of samples that should be generated
*/
void YM2151UpdateOne(int num, INT16 **buffers, int length)
{
	int i;
	signed int outl,outr;
	SAMP *bufL, *bufR;

	bufL = buffers[0];
	bufR = buffers[1];

	PSG = &YMPSG[num];

	#ifdef USE_MAME_TIMERS
		/* ASG 980324 - handled by real timers now */
	#else
	/* calculate timers */
	if (PSG->TimA)
	{
		PSG->TimAVal -= ( length << TIMER_SH );
		if (PSG->TimAVal <= 0)
		{
			PSG->TimAVal += PSG->TimerA[ PSG->TimAIndex ];
			if (PSG->IRQenable & 0x04)
			{
				int oldstate = PSG->status & 3;
				PSG->status |= 1;
				if ((!oldstate) && (PSG->irqhandler)) (*PSG->irqhandler)(1);
			}
		}
	}
	if (PSG->TimB)
	{
		PSG->TimBVal -= ( length << TIMER_SH );
		if (PSG->TimBVal<=0)
		{
			PSG->TimBVal += PSG->TimerB[ PSG->TimBIndex ];
			if ( PSG->IRQenable & 0x08 )
			{
				int oldstate = PSG->status & 3;
				PSG->status |= 2;
				if ((!oldstate) && (PSG->irqhandler)) (*PSG->irqhandler)(1);
			}
		}
	}
	#endif

	for (i=0; i<length; i++)
	{

		chan_calc(0);
		SAVE_SINGLE_CHANNEL(0)
		chan_calc(1);
		SAVE_SINGLE_CHANNEL(1)
		chan_calc(2);
		SAVE_SINGLE_CHANNEL(2)
		chan_calc(3);
		SAVE_SINGLE_CHANNEL(3)
		chan_calc(4);
		SAVE_SINGLE_CHANNEL(4)
		chan_calc(5);
		SAVE_SINGLE_CHANNEL(5)
		chan_calc(6);
		SAVE_SINGLE_CHANNEL(6)
		chan_calc(7);
		SAVE_SINGLE_CHANNEL(7)

		SAVE_ALL_CHANNELS

		outl = chanout[0] & PSG->PAN[0];
		outr = chanout[0] & PSG->PAN[1];
		outl += (chanout[1] & PSG->PAN[2]);
		outr += (chanout[1] & PSG->PAN[3]);
		outl += (chanout[2] & PSG->PAN[4]);
		outr += (chanout[2] & PSG->PAN[5]);
		outl += (chanout[3] & PSG->PAN[6]);
		outr += (chanout[3] & PSG->PAN[7]);
		outl += (chanout[4] & PSG->PAN[8]);
		outr += (chanout[4] & PSG->PAN[9]);
		outl += (chanout[5] & PSG->PAN[10]);
		outr += (chanout[5] & PSG->PAN[11]);
		outl += (chanout[6] & PSG->PAN[12]);
		outr += (chanout[6] & PSG->PAN[13]);
		outl += (chanout[7] & PSG->PAN[14]);
		outr += (chanout[7] & PSG->PAN[15]);

		outl >>= FINAL_SH;
		outr >>= FINAL_SH;
		if (outl > MAXOUT) outl = MAXOUT;
			else if (outl < MINOUT) outl = MINOUT;
		if (outr > MAXOUT) outr = MAXOUT;
			else if (outr < MINOUT) outr = MINOUT;
		((SAMP*)bufL)[i] = (SAMP)outl;
		((SAMP*)bufR)[i] = (SAMP)outr;

		lfo_calc();
		advance();

	}
}

void YM2151SetIrqHandler(int n, void(*handler)(int irq))
{
	YMPSG[n].irqhandler = handler;
}

void YM2151SetPortWriteHandler(int n, void(*handler)(int offset, int data))
{
	YMPSG[n].porthandler = handler;
}

