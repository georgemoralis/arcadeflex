package sound;

import static sound.streams.*;
import static arcadeflex.ptrlib.*;
import static mame.driverH.*;
import static arcadeflex.libc_old.*;
import static mame.mame.*;
import static mame.timer.*;


public class ym2151 {
    public static class OscilRec
    {
        public OscilRec()
        {
            
        }
    	/*unsigned int*/long phase;		/*accumulated operator phase*/
        /*unsigned int*/long freq;		/*operator frequency*/
        int DTfreq;	/*operator detune frequency*/

    /*unsigned int*/long MUL;		/*phase multiply*/
    /*unsigned int*/long DT1;		/*DT1|MUL * 32  */
    /*unsigned int*/long DT2;		/*DT2 index     */
    /*TODO*///
    /*TODO*///	signed   int *connect;	/*operator output 'direction'*/
    /*TODO*///
    /*TODO*////*Begin of channel specific data*/
    /*TODO*////*note: each operator number 0 contains channel specific data*/
    /*TODO*///	unsigned int FeedBack;	/*feedback shift value for operators 0 in each channel*/
    /*TODO*///	signed   int FB;		/*operator self feedback value used only by operators 0*/
    /*TODO*///	signed   int FB0;		/*previous output value*/
    /*unsigned int*/long KC;		/*operator KC (copied to all operators)*/
    /*unsigned int*/long KCindex;	/*speedup*/
    /*unsigned int*/long KF;		/*operator KF (copied to all operators)*/
    /*unsigned int*/long PMS;		/*channel PMS*/
    /*unsigned int*/long AMS;		/*channel AMS*/
    /*End of channel specific data*/

    /*unsigned int*/long AMSmask;	/*LFO AMS enable mask*/

    /*unsigned int*/int  state;		/*Envelope state: 4-attack(AR) 3-decay(D1R) 2-sustain(D2R) 1-release(RR) 0-off*/
    /*unsigned int*/long delta_AR;	/*volume delta for attack phase*/
    /*unsigned int*/long TL;		/*Total attenuation Level*/
    int volume;	/*operator attenuation level*/
    /*unsigned int*/long delta_D1R;	/*volume delta for decay phase*/
    /*unsigned int*/long D1L;		/*EG switches to D2R, when envelope reaches this level*/
    /*unsigned int*/long delta_D2R;	/*volume delta for sustain phase*/
    /*unsigned int*/long delta_RR;	/*volume delta for release phase*/

    /*unsigned int*/int key;		/*0=last key was KEY OFF, 1=last key was KEY ON*/

    /*unsigned int*/long KS;		/*Key Scale     */
    /*unsigned int*/long AR;		/*Attack rate   */
    /*unsigned int*/long D1R;		/*Decay rate    */
    /*unsigned int*/long D2R;		/*Sustain rate  */
    /*unsigned int*/long RR;		/*Release rate  */

    int LFOpm;		/*phase modulation from LFO*/
    int a_vol;		/*used for attack phase calculations*/

    }
    public static class _YM2151
    {
        public _YM2151()
        {
            Oscils=new OscilRec[32];
            PAN=new long[16];
            TimerATime=new double[1024];
            TimerBTime=new double[256];
            freq=new long[11*12*64];
            DT1freq=new int[8*16*32];
            EG_tab=new long[32+64+32];
            LFOfreq=new long[256];
        }
    	OscilRec[] Oscils;	/*there are 32 operators in YM2151*/
        
        /*unsigned int*/long[] PAN;	/*channels output masks (0xffffffff = enable)*/

        /*unsigned int*/long LFOphase;	/*accumulated LFO phase         */
        /*unsigned int*/long LFOfrq;	/*LFO frequency                 */
        /*unsigned int*/long LFOwave;	/*LFO waveform (0-saw, 1-square, 2-triangle, 3-random noise)*/
        /*unsigned int*/long PMD;		/*LFO Phase Modulation Depth    */
        /*unsigned int*/long AMD;		/*LFO Amplitude Modulation Depth*/
        /*unsigned int*/long LFA;		/*current AM from LFO*/
        int LFP;		/*current PM from LFO*/

        /*unsigned int*/long test;		/*TEST register*/

        /*unsigned int*/long CT;		/*output control pins (bit7 CT2, bit6 CT1)*/
        /*unsigned int*/long noise;		/*noise register (bit 7 - noise enable, bits 4-0 - noise freq*/

        /*unsigned int*/long IRQenable;	/*IRQ enable for timer B (bit 3) and timer A (bit 2)*/
       /*unsigned*/ int status;	/*chip status (BUSY, IRQ Flags)*/
    
    
    	Object TimATimer,TimBTimer;	/*ASG 980324 -- added for tracking timers*/
    	double[] TimerATime;	/*Timer A times for MAME*/
    	double[] TimerBTime;		/*Timer B times for MAME*/
    
    	/*unsigned int*/long TimAIndex;		/*Timer A index*/
    	/*unsigned int*/long TimBIndex;		/*Timer B index*/
    
    	/*unsigned int*/long TimAOldIndex;	/*Timer A previous index*/
    	/*unsigned int*/long TimBOldIndex;	/*Timer B previous index*/
    
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
    	/*unsigned int*/long[] freq;/*11 octaves, 12 semitones, 64 'cents'*/
    
    	/*
    	*   Frequency deltas for DT1. These deltas alter operator frequency
    	*   after it has been taken from frequency-deltas table.
    	*/
    	int[] DT1freq;		/*8 DT1 levels,16 MUL lelvels, 32 KC values*/
    
    	/*unsigned int*/long[] EG_tab;		/*envelope deltas (32 + 64 rates + 32 RKS)*/
    
    	/*unsigned int*/long[] LFOfreq;			/*frequency deltas for LFO*/
    
        WriteYmHandlerPtr irqhandler;				/*IRQ function handler*/
        WriteHandlerPtr porthandler;	/*port write function handler*/

    	/*unsigned*/ int clock;			/*chip clock in Hz (passed from 2151intf.c)*/
    	/*unsigned*/ int sampfreq;		/*sampling frequency in Hz (passed from 2151intf.c)*/
    
    }
    
    /*
    **  Shifts below are subject to change when sampling frequency changes...
    */
    public static final int FREQ_SH			=16;  /* 16.16 fixed point (frequency calculations) */
    public static final int ENV_SH			=16;  /* 16.16 fixed point (envelope calculations)  */
    public static final int LFO_SH			=23;  /*  9.23 fixed point (LFO calculations)       */
    public static final int TIMER_SH		=16;  /* 16.16 fixed point (timers calculations)    */
    
    public static final int FREQ_MASK		=((1<<FREQ_SH)-1);
    public static final int ENV_MASK		=((1<<ENV_SH)-1);
    
    public static final int ENV_BITS		=10;
    public static final int ENV_LEN			=(1<<ENV_BITS);
    public static final double ENV_STEP		=(128.0/ENV_LEN);
    public static final int ENV_QUIET		=((int)(0x68/(ENV_STEP)));
    
    public static final int  MAX_ATT_INDEX	=((ENV_LEN<<ENV_SH)-1); /*1023.ffff*/
    public static final int  MIN_ATT_INDEX	=(      (1<<ENV_SH)-1); /*   0.ffff*/
    
    public static final int  EG_ATT			=4;
    public static final int  EG_DEC			=3;
    public static final int  EG_SUS			=2;
    public static final int  EG_REL			=1;
    public static final int  EG_OFF			=0;

    public static final int SIN_BITS		=10;
    public static final int SIN_LEN			=(1<<SIN_BITS);
    public static final int SIN_MASK		=(SIN_LEN-1);
    
    public static final int TL_RES_LEN		=(256); /* 8 bits addressing (real chip) */
    
    public static final int LFO_BITS		=9;
    public static final int LFO_LEN			=(1<<LFO_BITS);
    public static final int LFO_MASK		=(LFO_LEN-1);

    public static final int FINAL_SH	=(0);
    public static final int MAXOUT		=(+32767);
    public static final int MINOUT		=(-32768);

    
    /* TL_TAB_LEN is calculated as:
     * 13 - sinus amplitude bits  (Y axis)
     * 2  - sinus sign bit        (Y axis)
     * ENV_LEN - sinus resolution (X axis)
    */
    public static final int TL_TAB_LEN =(13*2*TL_RES_LEN);
    static int[] TL_TAB=new int[TL_TAB_LEN];
    
    /* sin waveform table in 'decibel' scale*/
    static /*unsigned int*/long[] sin_tab=new long[SIN_LEN];
    
    /* four AM/PM LFO waveforms (8 in total)*/
    static /*unsigned int*/long[] lfo_tab=new long[LFO_LEN*4*2];
    
    /* LFO amplitude modulation depth table (128 levels)*/
    static /*unsigned int*/long[] lfo_md_tab=new long[128];
    
    /* translate from D1L to volume index (16 D1L levels)*/
    static /*unsigned int*/long[] D1L_tab=new long[16];
    
    /*
     * translate from key code KC (OCT2 OCT1 OCT0 N3 N2 N1 N0) to
     * index in frequency-deltas table. (9 octaves * 16 note codes)
    */
    static /*unsigned int*/long[] KC_TO_INDEX=new long[9*16];
    
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
    static int DT2_tab[] = { 0, 384, 500, 608 };
    
    /*
     *   DT1 defines offset in Hertz from base note
     *   This table is converted while initialization...
     *   Detune table in YM2151 User's Manual is wrong (checked against the real chip)
    */
    static int DT1_tab[] = { /* 4*32 DT1 values */
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
    
    
    static _YM2151[] YMPSG = null;	/* array of YM2151's */
    static int YMNumChips;	/* total # of YM2151's emulated */
    
    
    /*these variables stay here because of speedup purposes only */
    static _YM2151[] PSG;
    static int[] chanout=new int[8];
    static int c1,m2,c2; /*Phase Modulation input for operators 2,3,4*/
    
    static void init_tables()
    {
    	int i,x;
    	int n;
    	double o,m=0.0;
    
    	for (x=0; x<TL_RES_LEN; x++)
    	{
    		m = (1<<16) / Math.pow(2, (x+1) * (ENV_STEP/4.0) / 8.0);
    		m = Math.floor(m);
    
    		/* we never reach (1<<16) here due to the (x+1) */
    		/* result fits within 16 bits at maximum */
    
    		n = (int)m;		/* 16 bits here */
    		n >>= 4;		/* 12 bits here */
    		if ((n&1)!=0)		/* round to closest */
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
    	}
    	/*if (errorlog) fprintf(errorlog,"TL_TAB_LEN = %i (%i bytes)\n",TL_TAB_LEN, (int)sizeof(TL_TAB));*/
    
    	for (i=0; i<SIN_LEN; i++)
    	{
    		/* non-standard sinus */
    		m = Math.sin( ((i*2)+1) * Math.PI / SIN_LEN ); /* checked against the real chip */
    
    		/* we never reach zero here due to ((i*2)+1) */
    
    		if (m>0.0)
    			o = 8*Math.log(1.0/m)/Math.log(2);  /* convert to 'decibels' */
    		else
    			o = 8*Math.log(-1.0/m)/Math.log(2); /* convert to 'decibels' */
    
    		o = o / (ENV_STEP/4);
    
    		n = (int)(2.0*o);
    		if ((n&1)!=0)		/* round to closest */
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
    			o = 8*Math.log(255.0/m)/Math.log(2);  /* convert to 'decibels' */
    		else
    		{
    			if (m<0.0)
    				o = 8*Math.log(-255.0/m)/Math.log(2); /* convert to 'decibels' */
    			else
    				o = 8*Math.log(255.0/0.01)/Math.log(2); /* small number */
    		}
    
    		o = o / (ENV_STEP/4);
    
    		n = (int)(2.0*o);
    		if ((n&1)!=0)		/* round to closest */
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
    			o = 8*Math.log(8192.0/m)/Math.log(2);  /* convert to 'decibels' */
    		else
    			o = 8*Math.log(8192.0/0.01)/Math.log(2); /* small number (m=0)*/
    
    		o = o / (ENV_STEP/4);
    
    		n = (int)(2.0*o);
    		if ((n&1)!=0)		/* round to closest */
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
    			o = 8*Math.log(127.0/m)/Math.log(2);  /* convert to 'decibels' */
    		else
    		{
    			if (m<0.0)
    				o = 8*Math.log(-128.0/m)/Math.log(2); /* convert to 'decibels' */
    			else
    				o = 8*Math.log(127.0/0.01)/Math.log(2); /* small number */
    		}
    
    		o = o / (ENV_STEP/4);
    
    		n = (int)(2.0*o);
    		if ((n&1)!=0)		/* round to closest */
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
    		D1L_tab[i] = (long)(m * (1<<ENV_SH));
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
    }
    static void init_chip_tables(_YM2151 chip)
    {
    	int i,j;
    	double mult,pom,pom2,clk,phaseinc,Hz;
    
    	double scaler;	/* formula below is true for chip clock=3579545 */
    	/* so we need to scale its output accordingly to the chip clock */
    
    	scaler= (double)chip.clock / 3579545.0;
    
    	/*this loop calculates Hertz values for notes from c-0 to b-7*/
    	/*including 64 'cents' (100/64 that is 1.5625 of real cent) per note*/
    	/* i*100/64/1200 is equal to i/768 */
    
    	mult = (1<<FREQ_SH);
    	for (i=0; i<1*12*64; i++)
    	{
    		/* 3.4375 Hz is note A; C# is 4 semitones higher */
    		Hz = scaler * 3.4375 * Math.pow (2, (i+4*64) / 768.0 );
    
    		/* calculate phase increment */
    		phaseinc = (Hz*SIN_LEN) / (double)chip.sampfreq;
    
    		chip.freq[i] = (long)(phaseinc*mult);
    		for (j=1; j<11; j++)
    		{
    			chip.freq[i+j*12*64] = chip.freq[i]*(1<<j);
    		}
    	}
    
    	mult = (1<<FREQ_SH);
    	for (j=0; j<4; j++)
    	{
    		for (i=0; i<32; i++)
    		{
    			int x, y, mul;
    
    			Hz = ( (double)DT1_tab[j*32+i] * ((double)chip.clock/8.0) ) / (double)(1<<24);
    			/*Important note:
    			**  The frequency calculated above is HALF of the frequency found in the Manual.
    			**  This is intentional as Manual gives the frequencies for MUL = 1, not MUL = 0.5
    			*/
    			/*calculate phase increment*/
    			phaseinc = (Hz*SIN_LEN) / (double)chip.sampfreq;
    
    			/*positive and negative values*/
    			chip.DT1freq[ (j+0)*16*32 + i ] = (int)(phaseinc * mult);
    			chip.DT1freq[ (j+4)*16*32 + i ] = -chip.DT1freq[ (j+0)*16*32 + i ];
    
    			for (mul=1; mul<16; mul++)
    			{
    				x = (j+0)*16*32 + mul*32;
    				y = (j+4)*16*32 + mul*32;
    				/*positive and negative values*/
    				chip.DT1freq[ x + i ] =(int)(phaseinc * mult * (mul*2));
    				chip.DT1freq[ y + i ] = -chip.DT1freq[ x + i ];
    			}
    		}
    	}
    
    	mult = (1<<LFO_SH);
    	clk  = (double)chip.clock;
    	for (i=0; i<256; i++)
    	{
    		j = i & 0x0f;
    		pom = Math.abs(  (clk/65536/(1<<(i/16)) ) - (clk/65536/32/(1<<(i/16)) * (j+1)) );
    
    		/*calculate phase increment*/
    		chip.LFOfreq[0xff-i] = (long)(( (pom*LFO_LEN) / (double)chip.sampfreq ) * mult); /*fixed point*/
    		/*if (errorlog) fprintf(errorlog, "LFO[%02x] (%08x)= real %20.15f Hz  emul %20.15f Hz\n",0xff-i, chip.LFOfreq[0xff-i], pom,
    			(((double)chip.LFOfreq[0xff-i] / mult) * (double)chip.sampfreq ) / (double)LFO_LEN );*/
    	}
    
    	for (i=0; i<34; i++)
    		chip.EG_tab[i] = 0;		/* infinity */
    
    	for (i=2; i<64; i++)
    	{
    		pom2 = (double)chip.clock / (double)chip.sampfreq;
    		if (i<60) pom2 *= ( 1 + (i&3)*0.25 );
    		pom2 *= 1<<((i>>2));
    		pom2 /= 768.0 * 1024.0;
    		pom2 *= (double)(1<<ENV_SH);
    		chip.EG_tab[32+i] = (long)pom2;
    	}
    
    	for (i=0; i<32; i++)
    	{
    		chip.EG_tab[ 32+64+i ] = chip.EG_tab[32+63];
    	}
    
    	/* precalculate timers' deltas */
    	/* User's Manual pages 15,16  */
    	mult = (1<<TIMER_SH);
    	for (i=0; i<1024; i++)
    	{
    		/* ASG 980324: changed to compute both TimerA and TimerATime */
    		pom= ( 64.0  *  (1024.0-i) / (double)chip.clock );
    			chip.TimerATime[i] = pom;
    	}
    	for (i=0; i<256; i++)
    	{
    		/* ASG 980324: changed to compute both TimerB and TimerBTime */
    		pom= ( 1024.0 * (256.0-i)  / (double)chip.clock );
    			chip.TimerBTime[i] = pom;
    	}
    }
    public static void envelope_KONKOFF(OscilRec[] op,int op_offset, int v)
    {
    	if ((v&0x08)!=0)
    	{
    		if (op[op_offset].key==0)
    		{
    			op[op_offset].key   = 1;      /*KEYON'ed*/
    			op[op_offset].phase = 0;      /*clear phase */
    			op[op_offset].state = EG_ATT; /*KEY ON = attack*/
    		}
    	}
    	else
    	{
    		if (op[op_offset].key!=0)
    		{
    			op[op_offset].key   = 0;      /*KEYOFF'ed*/
    			if (op[op_offset].state>EG_REL)
    				op[op_offset].state = EG_REL; /*release*/
    		}
    	}
    
    	op_offset+=8;
    
    	if ((v&0x20)!=0)
    	{
    		if (op[op_offset].key==0)
    		{
    			op[op_offset].key   = 1;
    			op[op_offset].phase = 0;
    			op[op_offset].state = EG_ATT;
    		}
    	}
    	else
    	{
    		if (op[op_offset].key!=0)
    		{
    			op[op_offset].key   = 0;
    			if (op[op_offset].state>EG_REL)
    				op[op_offset].state = EG_REL;
    		}
    	}
    
    	op_offset+=8;
    
    	if ((v&0x10)!=0)
    	{
    		if (op[op_offset].key==0)
    		{
    			op[op_offset].key   = 1;
    			op[op_offset].phase = 0;
    			op[op_offset].state = EG_ATT;
    		}
    	}
    	else
    	{
    		if (op[op_offset].key!=0)
    		{
    			op[op_offset].key   = 0;
    			if (op[op_offset].state>EG_REL)
    				op[op_offset].state = EG_REL;
    		}
    	}
    
    	op_offset+=8;
    
    	if ((v&0x40)!=0)
    	{
    		if (op[op_offset].key==0)
    		{
    			op[op_offset].key   = 1;
    			op[op_offset].phase = 0;
    			op[op_offset].state = EG_ATT;
    		}
    	}
    	else
    	{
    		if (op[op_offset].key!=0)
    		{
    			op[op_offset].key   = 0;
    			if (op[op_offset].state>EG_REL)
    				op[op_offset].state = EG_REL;
    		}
    	}
    }
    
    
    public static timer_callback timer_callback_a = new timer_callback(){ public void handler(int n)
    {
    	_YM2151 chip = YMPSG[n];
    	chip.TimATimer = timer_set (chip.TimerATime[ (int)chip.TimAIndex ], n, timer_callback_a);
    	chip.TimAOldIndex = chip.TimAIndex;
    	if ((chip.IRQenable & 0x04)!=0)
    	{
    		int oldstate = chip.status & 3;
    		chip.status |= 1;
    		if ((oldstate==0) && (chip.irqhandler!=null)) (chip.irqhandler).handler(1);
    	}
    }};
    public static timer_callback timer_callback_b = new timer_callback(){ public void handler(int n)
    {
    	_YM2151 chip = YMPSG[n];
    	chip.TimBTimer = timer_set (chip.TimerBTime[ (int)chip.TimBIndex ], n, timer_callback_b);
    	chip.TimBOldIndex = chip.TimBIndex;
    	if ((chip.IRQenable & 0x08)!=0)
    	{
    		int oldstate = chip.status & 3;
    		chip.status |= 2;
    		if ((oldstate==0) && (chip.irqhandler)!=null) (chip.irqhandler).handler(1);
    	}
    }};
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
        //System.out.println("n= "+n + " r="+r + " v="+v);
    	_YM2151 chip = YMPSG[n];
        OscilRec op = chip.Oscils[ r&0x1f ];
    
    	/*adjust bus to 8 bits*/
    	r &= 0xff;
    	v &= 0xff;
    
    	switch(r & 0xe0){
    	case 0x00:
    		switch(r){
    		case 0x01: /*LFO reset(bit 1), Test Register (other bits)*/
    			chip.test = v;
    			if ((v&2)!=0) chip.LFOphase = 0;
    			break;
    
    		case 0x08:
    			envelope_KONKOFF(chip.Oscils, v&7 , v );
    			break;
    		case 0x0f: /*noise mode enable, noise freq*/
    			chip.noise = v;
    			/*if ((v&0x80)) printf("YM2151 noise (%02x)\n",v);*/
    			break;
    
    		case 0x10: /*timer A hi*/
    			chip.TimAIndex = (chip.TimAIndex & 0x003) | (v<<2);
    			break;
    
    		case 0x11: /*timer A low*/
    			chip.TimAIndex = (chip.TimAIndex & 0x3fc) | (v & 3);
    			break;
    
    		case 0x12: /*timer B*/
    			chip.TimBIndex = v;
    			break;
    
    		case 0x14: /*CSM, irq flag reset, irq enable, timer start/stop*/
    			/*if ((v&0x80)) printf("\nYM2151 CSM MODE ON\n");*/
    
    			chip.IRQenable = v;	/*bit 3-timer B, bit 2-timer A*/
    
    			if ((v&0x20)!=0)	/*reset timer B irq flag*/
    			{
    				int oldstate = chip.status & 3;
    				chip.status &= 0xfd;
    				if ((oldstate==2) && (chip.irqhandler)!=null) (chip.irqhandler).handler(0);
    			}
    
    			if ((v&0x10)!=0)	/*reset timer A irq flag*/
    			{
    				int oldstate = chip.status & 3;
    				chip.status &= 0xfe;
    				if ((oldstate==1) && (chip.irqhandler)!=null) (chip.irqhandler).handler(0);
    
    			}
    
    			if ((v&0x02)!=0){	/*load and start timer B*/
    				/* ASG 980324: added a real timer */
    				/* start timer _only_ if it wasn't already started (it will reload time value next round)*/
    					if (chip.TimBTimer==null)
    					{
    						chip.TimBTimer = timer_set (chip.TimerBTime[ (int)chip.TimBIndex ], n, timer_callback_b);
    						chip.TimBOldIndex = chip.TimBIndex;
    					}
    			}else{		/*stop timer B*/
    				/* ASG 980324: added a real timer */
    					if (chip.TimBTimer!=null) timer_remove (chip.TimBTimer);
    					chip.TimBTimer = null;
    			}
    
    			if ((v&0x01)!=0){	/*load and start timer A*/
    				/* ASG 980324: added a real timer */
    				/* start timer _only_ if it wasn't already started (it will reload time value next round)*/
    					if (chip.TimATimer==null)
    					{
    						chip.TimATimer = timer_set (chip.TimerATime[ (int)chip.TimAIndex ], n, timer_callback_a);
    						chip.TimAOldIndex = chip.TimAIndex;
    					}
    			}else{		/*stop timer A*/
    				/* ASG 980324: added a real timer */
    					if (chip.TimATimer!=null) timer_remove (chip.TimATimer);
    					chip.TimATimer = null;
    			}
    			break;
    
    		case 0x18: /*LFO frequency*/
    			chip.LFOfrq = chip.LFOfreq[ v ];
    			break;
    
    		case 0x19: /*PMD (bit 7==1) or AMD (bit 7==0)*/
    			if ((v&0x80)!=0)
    				chip.PMD = lfo_md_tab[ v&0x7f ] + 512;
    			else
    				chip.AMD = lfo_md_tab[ v&0x7f ];
    			break;
    
    		case 0x1b: /*CT2, CT1, LFO waveform*/
    			chip.CT = v;
    			chip.LFOwave = (v & 3) * LFO_LEN*2;
    			if (chip.porthandler!=null) (chip.porthandler).handler(0 , (int)((chip.CT) >> 6) );
    			break;
    
    		default:
    			if (errorlog!=null) fprintf(errorlog,"YM2151 Write %02x to undocumented register #%02x\n",v,r);
    			break;
    		}
    		break;
   
    	case 0x20:
    		op = chip.Oscils[r & 7];
    		switch(r & 0x18){
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
    		case 0x18: /*PMS,AMS*/
    			op.PMS = (v>>4) & 7;
    			op.AMS = v & 3;
    			break;
    		}
    		break;
    
    	case 0x40: /*DT1, MUL*/
    		{
    			/*unsigned int*/long oldDT1 = op.DT1;
    			/*unsigned int*/long oldMUL = op.MUL;
    			op.DT1 = (v&0x7f)<<5;
    			op.MUL = (v&0x0f)!=0 ? (v&0x0f)<<1: 1;
    			if (oldDT1 != op.DT1)
    			{
    				op.DTfreq = chip.DT1freq[ (int)(op.DT1 + (op.KC>>2)) ];
    			}
    			if (oldMUL != op.MUL)
    			{
    				/*unsigned int*/long kc_channel;
    
    				kc_channel = op.KCindex + op.KF;
    				op.freq = (chip.freq[ (int)(kc_channel + op.DT2) ] * op.MUL)&0xFFFFFFFFL;//todo neccesary???
    			}
    		}
    		break;
    
    	case 0x60: /*TL*/
    		op.TL = (v&0x7f)<<(ENV_BITS-7); /*7bit TL*/
    		break;
    
    	case 0x80: /*KS, AR*/
    		{
    			long oldKS = op.KS;
    			long oldAR = op.AR;
    			op.KS = 5-(v>>6);
    			op.AR = (v&0x1f)!=0 ? 32 + ((v&0x1f)<<1) : 0;
    
    			if ( (op.AR != oldAR) || (op.KS != oldKS) )
    			{
    				if ((op.AR + (op.KC>>op.KS)) < 32+62)
    					op.delta_AR  = chip.EG_tab[(int)(op.AR  + (op.KC>>op.KS)) ];
    				else
    					op.delta_AR  = MAX_ATT_INDEX+1;
    			}
    
    			if (op.KS != oldKS)
    			{
    				op.delta_D1R = chip.EG_tab[(int)(op.D1R + (op.KC>>op.KS)) ];
    				op.delta_D2R = chip.EG_tab[(int)(op.D2R + (op.KC>>op.KS)) ];
    				op.delta_RR  = chip.EG_tab[(int)(op.RR  + (op.KC>>op.KS)) ];
    			}
    		}
    		break;
    
    	case 0xa0: /*AMS-EN, D1R*/
    		op.AMSmask = (v&0x80)!=0 ? 0xffffffff : 0;
    		op.D1R     = (v&0x1f)!=0 ? 32 + ((v&0x1f)<<1) : 0;
    		op.delta_D1R = chip.EG_tab[(int)(op.D1R + (op.KC>>op.KS)) ];
    		break;
   
   	case 0xc0: /*DT2, D2R*/
   		{
   			/*unsigned int*/long oldDT2 = op.DT2;
   			op.DT2 = DT2_tab[ v>>6 ];
   			if (op.DT2 != oldDT2)
   			{
   				/*unsigned int*/long kc_channel;
   
   				kc_channel = op.KCindex + op.KF;
   				op.freq = (chip.freq[ (int)(kc_channel + op.DT2) ] * op.MUL) & 0xFFFFFFFFL;//todo neccesary?
   			}
   		}
   		op.D2R = (v&0x1f)!=0 ? 32 + ((v&0x1f)<<1) : 0;
   		op.delta_D2R = chip.EG_tab[(int)(op.D2R + (op.KC>>op.KS)) ];
   		break;
   
   	case 0xe0: /*D1L, RR*/
   		op.D1L = D1L_tab[ v>>4 ];
   		op.RR  = 34 + ((v&0x0f)<<2);
   		op.delta_RR  = chip.EG_tab[(int)(op.RR  + (op.KC>>op.KS)) ];
   		break;
    	}
    }
    public static int YM2151ReadStatus( int n )
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
    public static int YM2151Init(int num, int clock, int rate)
    {
    	int i;
    
    	if (YMPSG!=null) return (-1);	/* duplicate init. */
    
    	YMNumChips = num;
        
    	YMPSG = new _YM2151[YMNumChips];//(YM2151 *)malloc(sizeof(YM2151) * YMNumChips);
    	if (YMPSG == null) return (1);
    
    	init_tables();
    	for (i=0 ; i<YMNumChips; i++)
    	{
            YMPSG[i] = new _YM2151();
            YMPSG[i].clock = clock;
            /*rate = clock/64;*/
            YMPSG[i].sampfreq = rate!=0 ? rate : 44100;	/* avoid division by 0 in init_chip_tables() */
            YMPSG[i].irqhandler = null;					/*interrupt handler */
    	    YMPSG[i].porthandler = null;				/*port write handler*/
            init_chip_tables(YMPSG[i]);
            YM2151ResetChip(i);
        	/*if (errorlog) fprintf(errorlog,"YM2151[init] clock=%i sampfreq=%i\n", YMPSG[i].clock, YMPSG[i].sampfreq);*/
        }
        return(0);
    }

    public static void YM2151Shutdown()
    {
    	if (YMPSG==null) return;
    	YMPSG = null;
    }


    /*
    ** reset all chip registers.
    */
    public static void YM2151ResetChip(int num)
    {
        int i;
    	_YM2151 chip = YMPSG[num];
    
    	/* initialize hardware registers */
    
    	for (i=0; i<32; i++)
    	{
    		chip.Oscils[i]=new OscilRec();//memset(&chip->Oscils[i],'\0',sizeof(OscilRec));
    		chip.Oscils[i].volume = MAX_ATT_INDEX;
    	}
    
    	chip.LFOphase = 0;
    	chip.LFOfrq   = 0;
    	chip.LFOwave  = 0;
    	chip.PMD = lfo_md_tab[ 0 ]+512;
    	chip.AMD = lfo_md_tab[ 0 ];
    	chip.LFA = 0;
    	chip.LFP = 0;
    
    	chip.test= 0;
    
    	chip.IRQenable = 0;
    
    	/* ASG 980324 -- reset the timers before writing to the registers */
    	chip.TimATimer = null;
    	chip.TimBTimer = null;
    	chip.TimAIndex = 0;
    	chip.TimBIndex = 0;
    	chip.TimAOldIndex = 0;
    	chip.TimBOldIndex = 0;
    
    	chip.noise     = 0;
    
    	chip.status    = 0;
        
        YM2151WriteReg(num, 0x1b, 0); /*only because of CT1, CT2 output pins*/
    	for (i=0x20; i<0x100; i++)   /*just to set the PM operators */
    	{
    		YM2151WriteReg(num, i, 0);
    	}
    
    }
    
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
    public static int acc_calc(int value)
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
    	YMPSG[n].irqhandler = handler;
    }
    public static void YM2151SetPortWriteHandler(int n, WriteHandlerPtr handler)
    {
    	YMPSG[n].porthandler = handler;
    }  
}
