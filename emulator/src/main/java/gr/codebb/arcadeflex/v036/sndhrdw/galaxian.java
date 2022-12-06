/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package gr.codebb.arcadeflex.v036.sndhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;

import arcadeflex.v036.generic.funcPtr.TimerCallbackHandlerPtr;
import gr.codebb.arcadeflex.common.PtrLib.BytePtr;
import gr.codebb.arcadeflex.common.PtrLib.ShortPtr;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.sound.mixer.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.timer.*;
import static arcadeflex.v036.mame.timerH.*;
import static gr.codebb.arcadeflex.v036.sound.streams.*;

public class galaxian
{
	
	//#define VERBOSE 0
	
	//#define NEW_LFO 0
	//#define NEW_SHOOT 1
	
	public static final int XTAL		=18432000;
	
	public static final double SOUND_CLOCK= (XTAL/6/2);			/* 1.536 Mhz */
	
	//#define SAMPLES 1
	
	public static final double RNG_RATE=	(XTAL/3);			/* RNG clock is XTAL/3 */
	public static final double NOISE_RATE=	(XTAL/3/192/2/2);	/* 2V = 8kHz */
	public static final double NOISE_LENGTH= (NOISE_RATE*4);	/* four seconds of noise */
	
	public static final int SHOOT_RATE =2672;
	public static final int SHOOT_LENGTH =13000;
	
	public static final int TOOTHSAW_LENGTH= 16;
	public static final int TOOTHSAW_VOLUME= 36;
	public static final int STEPS =16;
	public static final int LFO_VOLUME= 6;
	public static final int SHOOT_VOLUME =50;
	public static final int NOISE_VOLUME =50;
	public static final int NOISE_AMPLITUDE =70*256;
	public static final int TOOTHSAW_AMPLITUDE =64;
	
	/* see comments in galaxian_sh_update() */
	public static final double  MINFREQ= (139-139/3);
	public static final double  MAXFREQ= (139+139/3);
	
	/*#if VERBOSE
	#define LOG(x) if (errorlog != 0) fprintf x
	#else
	#define LOG(x)
	#endif*/
	
	static timer_entry lfotimer = null;
	static int freq = (int)MAXFREQ;
	
	static final int STEP =1;
	
	static timer_entry noisetimer = null;
	static int noisevolume;
	static short[] noisewave;
	static short[] shootwave;
	
	static int shoot_length;
	static int shoot_rate;
	

	static int shootsampleloaded = 0;
	static int deathsampleloaded = 0;
	static int last_port1=0;
	static int last_port2=0;
	
	static byte[][] tonewave=new byte[4][TOOTHSAW_LENGTH];
	static int pitch,vol;
	
	static short backgroundwave[] =
	{
	   0x4000, 0x4000, 0x4000, 0x4000, 0x4000, 0x4000, 0x4000, 0x4000,
	   0x4000, 0x4000, 0x4000, 0x4000, 0x4000, 0x4000, 0x4000, 0x4000,
	   0x4000,-0x4000,-0x4000,-0x4000,-0x4000,-0x4000,-0x4000,-0x4000,
	  -0x4000,-0x4000,-0x4000,-0x4000,-0x4000,-0x4000,-0x4000,-0x4000,
	};
	
	static int channelnoise,channelshoot,channellfo;
	static int tone_stream;
	
        static int counter, countdown;
        public static StreamInitPtr tone_update = new StreamInitPtr() {
        public void handler(int ch, ShortPtr buffer, int length) {
		int i,j;
		byte[] w = tonewave[vol];
		
	
		/* only update if we have non-zero volume and frequency */
		if( pitch != 0xff )
		{
			for (i = 0; i < length; i++)
			{
				int mix = 0;
	
				for (j = 0;j < STEPS;j++)
				{
					if (countdown >= 256)
					{
						counter = (counter + 1) % TOOTHSAW_LENGTH;
						countdown = pitch;
					}
					countdown++;
	
					mix += w[counter];
				}
				buffer.write(0, (short)((mix << 8) / STEPS));
                                buffer.offset += 2;
			}
		}
		else
		{
			for( i = 0; i < length; i++ )
				buffer.write(0,(short)0);
                        buffer.offset += 2;
		}
	}};
	
	public static WriteHandlerPtr galaxian_pitch_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		stream_update(tone_stream,0);
	
		pitch = data;
	} };
	
	public static WriteHandlerPtr galaxian_vol_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		stream_update(tone_stream,0);
	
		/* offset 0 = bit 0, offset 1 = bit 1 */
		vol = (vol & ~(1 << offset)) | ((data & 1) << offset);
	} };
	
	public static TimerCallbackHandlerPtr noise_timer_cb = new TimerCallbackHandlerPtr() {
        public void handler(int param) {
		if( noisevolume > 0 )
		{
			noisevolume -= (noisevolume / 10) + 1;
			mixer_set_volume(channelnoise,noisevolume);
		}
	}};
	
	public static WriteHandlerPtr galaxian_noise_enable_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (deathsampleloaded != 0)
		{
			if ((data & 1)!=0 && (last_port1 & 1)==0)
				mixer_play_sample(channelnoise,new BytePtr(Machine.samples.sample[1].data),
						Machine.samples.sample[1].length,
						Machine.samples.sample[1].smpfreq,
						false);
			last_port1=data;
		}
		else
		{
			if ((data & 1) != 0)
			{
				if (noisetimer != null)
				{
					timer_remove(noisetimer);
					noisetimer = null;
				}
				noisevolume = 100;
				mixer_set_volume(channelnoise,noisevolume);
			}
			else
			{
				/* discharge C21, 22uF via 150k+22k R35/R36 */
				if (noisevolume == 100)
				{
					noisetimer = timer_pulse(TIME_IN_USEC(0.693*(155000+22000)*22 / 100), 0, noise_timer_cb);
				}
			}
		}
	} };
	
	public static WriteHandlerPtr galaxian_shoot_enable_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if( (data & 1)!=0 && (last_port2 & 1)==0 )
		{
			if (shootsampleloaded != 0)
			{
				mixer_play_sample(channelshoot,new BytePtr(Machine.samples.sample[0].data),
						Machine.samples.sample[0].length,
						Machine.samples.sample[0].smpfreq,
						false);
			}
			else
			{
/*TODO*///				mixer_play_sample_16(channelshoot, new ShortPtr(shootwave), shoot_length, shoot_rate, false);
				mixer_set_volume(channelshoot,SHOOT_VOLUME);
			}
		}
		last_port2=data;
	} };
	
	
	static String galaxian_sample_names[] =
	{
		"*galaxian",
		"shot.wav",
		"death.wav",
		null	/* end of array */
	};
        public static double V(double r0,double r1) 
        {
            return 2*TOOTHSAW_AMPLITUDE*(r0)/(r0+r1)-TOOTHSAW_AMPLITUDE;
        }
	public static ShStartHandlerPtr galaxian_sh_start = new ShStartHandlerPtr() {
        public int handler(MachineSound msound) {
		int i, j, sweep, charge, countdown, generator, bit1, bit2;
		int[] lfovol = {LFO_VOLUME,LFO_VOLUME,LFO_VOLUME};
	
		Machine.samples = readsamples(galaxian_sample_names,Machine.gamedrv.name);
	
		channelnoise = mixer_allocate_channel(NOISE_VOLUME);
		mixer_set_name(channelnoise,"Noise");
		channelshoot = mixer_allocate_channel(SHOOT_VOLUME);
		mixer_set_name(channelshoot,"Shoot");
		channellfo = mixer_allocate_channels(3,lfovol);
		mixer_set_name(channellfo+0,"Background #0");
		mixer_set_name(channellfo+1,"Background #1");
		mixer_set_name(channellfo+2,"Background #2");
	
		if (Machine.samples != null && Machine.samples.sample[0] != null)	/* We should check also that Samplename[0] = 0 */
			shootsampleloaded = 1;
		else
			shootsampleloaded = 0;
	
		if (Machine.samples != null && Machine.samples.sample[1] != null)	/* We should check also that Samplename[0] = 0 */
			deathsampleloaded = 1;
		else
			deathsampleloaded = 0;
	
		noisewave = new short[(int)NOISE_LENGTH];
		
	
                int SHOOT_SEC =2;
		shoot_rate = Machine.sample_rate;
		shoot_length = SHOOT_SEC * shoot_rate;
		shootwave = new short[shoot_length];
	
		/*
		 * The RNG shifter is clocked with RNG_RATE, bit 17 is
		 * latched every 2V cycles (every 2nd scanline).
		 * This signal is used as a noise source.
		 */
		generator = 0;
		countdown = (int)(NOISE_RATE / 2);
		for( i = 0; i < NOISE_LENGTH; i++ )
		{
			countdown -= RNG_RATE;
			while( countdown < 0 )
			{
				generator <<= 1;
				bit1 = (~generator >> 17) & 1;
				bit2 = (generator >> 5) & 1;
				if ((bit1 ^ bit2)!=0) generator |= 1;
				countdown += NOISE_RATE;
			}
			noisewave[i] = ((generator >> 17) & 1)!=0 ? (short)NOISE_AMPLITUDE : (short)-NOISE_AMPLITUDE;
		}
	
		/* dummy */
		sweep = 100;
		charge = +2;
		j=0;
		{
	int R41__ =100000;
	int R44__ =10000;
	int R45__ =22000;
	int R46__ =10000;
	int R47__ =2200;
	int R48__ =2200;
	double C25__ =0.000001;
	double C27__ =0.00000001;
	double C28__ =0.000047;
	double C29__ =0.00000001;
	double IC8L3_L =0.2;   /* 7400 L level */
	double IC8L3_H =4.5;   /* 7400 H level */
	double NOISE_L =0.2;   /* 7474 L level */
	double NOISE_H =4.5;   /* 7474 H level */
	/*
		key on/off time is programmable
		Therefore,  it is necessary to make separate sample with key on/off.
		And,  calculate the playback point according to the voltage of c28.
	*/
	double SHOOT_KEYON_TIME =0.1;  /* second */
	/*
		NE555-FM input calculation is wrong.
		The frequency is not proportional to the voltage of FM input.
		And,  duty will be changed,too.
	*/
	double NE555_FM_ADJUST_RATE =0.80;
			/* discharge : 100K * 1uF */
			double v  = 5.0;
			double vK = (shoot_rate)!=0 ? Math.exp(-1 / (R41__*C25__) / shoot_rate) : 0;
			/* -- SHOOT KEY port -- */
			double IC8L3 = IC8L3_L; /* key on */
			int IC8Lcnt = (int)(SHOOT_KEYON_TIME * shoot_rate); /* count for key off */
			/* C28 : KEY port capacity */
			/*       connection : 8L-3 - R47(2.2K) - C28(47u) - R48(2.2K) - C29 */
			double c28v = IC8L3_H - (IC8L3_H-(NOISE_H+NOISE_L)/2)/(R46__+R47__+R48__)*R47__;
			double c28K = (shoot_rate)!=0 ? Math.exp(-1 / (22000 * 0.000047 ) / shoot_rate) : 0;
			/* C29 : NOISE capacity */
			/*       connection : NOISE - R46(10K) - C29(0.1u) - R48(2.2K) - C28 */
			double c29v  = IC8L3_H - (IC8L3_H-(NOISE_H+NOISE_L)/2)/(R46__+R47__+R48__)*(R47__+R48__);
			double c29K1 = (shoot_rate)!=0 ? Math.exp(-1 / (22000  * 0.00000001 ) / shoot_rate) : 0; /* form C28   */
			double c29K2 = (shoot_rate)!=0 ? Math.exp(-1 / (100000 * 0.00000001 ) / shoot_rate) : 0; /* from noise */
			/* NE555 timer */
			/* RA = 10K , RB = 22K , C=.01u ,FM = C29 */
			double ne555cnt = 0;
			double ne555step = (shoot_rate)!=0 ? ((1.44/((R44__+R45__*2)*C27__)) / shoot_rate) : 0;
			double ne555duty = (double)(R44__+R45__)/(R44__+R45__*2); /* t1 duty */
			double ne555sr;		/* threshold (FM) rate */
			/* NOISE source */
			double ncnt  = 0.0;
			double nstep = (shoot_rate)!=0 ? ((double)NOISE_RATE / shoot_rate) : 0;
			double noise_sh2; /* voltage level */
	
			for( i = 0; i < shoot_length; i++ )
			{
				/* noise port */
				noise_sh2 = noisewave[(int)((int)ncnt % NOISE_LENGTH)] == NOISE_AMPLITUDE ? (short)NOISE_H : (short)NOISE_L;
				ncnt+=nstep;
				/* calculate NE555 threshold level by FM input */
				ne555sr = c29v*NE555_FM_ADJUST_RATE / (5.0*2/3);
				/* calc output */
				ne555cnt += ne555step;
				if( ne555cnt >= ne555sr) ne555cnt -= ne555sr;
				if( ne555cnt < ne555sr*ne555duty )
				{
					 /* t1 time */
					shootwave[i] =(short) (v/5*0x7fff);
					/* discharge output level */
					if(IC8L3==IC8L3_H)
						v *= vK;
				}
				else
					shootwave[i] = 0;
				/* C28 charge/discharge */
				c28v += (IC8L3-c28v) - (IC8L3-c28v)*c28K;	/* from R47 */
				c28v += (c29v-c28v) - (c29v-c28v)*c28K;		/* from R48 */
				/* C29 charge/discharge */
				c29v += (c28v-c29v) - (c28v-c29v)*c29K1;	/* from R48 */
				c29v += (noise_sh2-c29v) - (noise_sh2-c29v)*c29K2;	/* from R46 */
				/* key off */
				if(IC8L3==IC8L3_L && --IC8Lcnt==0)
					IC8L3=IC8L3_H;
			}
		}
                for(int a=0; a<4; a++)
                {
                    for(int b=0; b<16; b++)
                    {
                        tonewave[a][b]=0;
                    }
                }
		//memset(tonewave, 0, sizeof(tonewave));
	
		for( i = 0; i < TOOTHSAW_LENGTH; i++ )
		{
			
			double r0a = 1.0/1e12, r1a = 1.0/1e12;
			double r0b = 1.0/1e12, r1b = 1.0/1e12;
	
			/* #0: VOL1=0 and VOL2=0
			 * only the 33k and the 22k resistors R51 and R50
			 */
			if ((i & 1) != 0)
			{
				r1a += 1.0/33000;
				r1b += 1.0/33000;
			}
			else
			{
				r0a += 1.0/33000;
				r0b += 1.0/33000;
			}
			if ((i & 4) != 0)
			{
				r1a += 1.0/22000;
				r1b += 1.0/22000;
			}
			else
			{
				r0a += 1.0/22000;
				r0b += 1.0/22000;
			}
			tonewave[0][i] = (byte)V(1.0/r0a, 1.0/r1a);
	
			/* #1: VOL1=1 and VOL2=0
			 * add the 10k resistor R49 for bit QC
			 */
			if ((i & 4) != 0)
				r1a += 1.0/10000;
			else
				r0a += 1.0/10000;
			tonewave[1][i] = (byte)V(1.0/r0a, 1.0/r1a);
	
			/* #2: VOL1=0 and VOL2=1
			 * add the 15k resistor R52 for bit QD
			 */
			if ((i & 8) != 0)
				r1b += 1.0/15000;
			else
				r0b += 1.0/15000;
			tonewave[2][i] = (byte)V(1.0/r0b, 1.0/r1b);
	
			/* #3: VOL1=1 and VOL2=1
			 * add the 10k resistor R49 for QC
			 */
			if ((i & 4) != 0)
				r0b += 1.0/10000;
			else
				r1b += 1.0/10000;
			tonewave[3][i] = (byte)V(1.0/r0b, 1.0/r1b);
			//LOG((errorlog, "tone[%2d]: $%02x $%02x $%02x $%02x\n", i, tonewave[0][i], tonewave[1][i], tonewave[2][i], tonewave[3][i]));
		}
	
		pitch = 0;
		vol = 0;
	
		tone_stream = stream_init("Tone",TOOTHSAW_VOLUME,(int)(SOUND_CLOCK/STEPS),0,tone_update);
	
		if (deathsampleloaded==0)
		{
			mixer_set_volume(channelnoise,0);
/*TODO*///			mixer_play_sample_16(channelnoise,noisewave,NOISE_LENGTH,NOISE_RATE,1);
		}
		if (shootsampleloaded==0)
		{
			mixer_set_volume(channelshoot,0);
/*TODO*///			mixer_play_sample_16(channelshoot,shootwave,SHOOT_LENGTH,SHOOT_RATE,1);
		}
	
		mixer_set_volume(channellfo+0,0);
/*TODO*///		mixer_play_sample_16(channellfo+0,backgroundwave,backgroundwave.length,1000,1);
		mixer_set_volume(channellfo+1,0);
/*TODO*///		mixer_play_sample_16(channellfo+1,backgroundwave,backgroundwave.length,1000,1);
		mixer_set_volume(channellfo+2,0);
/*TODO*///		mixer_play_sample_16(channellfo+2,backgroundwave,backgroundwave.length,1000,1);
	
		return 0;
	}};
	
	
	
	public static ShStopHandlerPtr galaxian_sh_stop = new ShStopHandlerPtr() { public void handler() 
	{
		if (lfotimer != null)
		{
			timer_remove( lfotimer );
			lfotimer = null;
		}
		if (noisetimer != null)
		{
			timer_remove(noisetimer);
			noisetimer = null;
		}
		mixer_stop_sample(channelnoise);
		mixer_stop_sample(channelshoot);
		mixer_stop_sample(channellfo+0);
		mixer_stop_sample(channellfo+1);
		mixer_stop_sample(channellfo+2);

		noisewave = null;
		shootwave = null;
	} };
	
	public static WriteHandlerPtr galaxian_background_enable_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		mixer_set_volume(channellfo+offset,(data & 1)!=0 ? 100 : 0);
	} };
	
        public static TimerCallbackHandlerPtr lfo_timer_cb = new TimerCallbackHandlerPtr() {
        public void handler(int param) {
		if( freq > MINFREQ )
			freq--;
		else
			freq = (int)MAXFREQ;
	}};
	static int[] lfobit=new int[4];
	public static WriteHandlerPtr galaxian_lfo_freq_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		
		double r0, r1, rx = 100000.0;
	
		if( (data & 1) == lfobit[offset] )
			return;
	
		/*
		 * NE555 9R is setup as astable multivibrator
		 * - Ra is between 100k and ??? (open?)
		 * - Rb is zero here (bridge between pins 6 and 7)
		 * - C is 1uF
		 * charge time t1 = 0.693 * (Ra + Rb) * C
		 * discharge time t2 = 0.693 * (Rb) *  C
		 * period T = t1 + t2 = 0.693 * (Ra + 2 * Rb) * C
		 * . min period: 0.693 * 100 kOhm * 1uF . 69300 us = 14.4Hz
		 * . max period: no idea, since I don't know the max. value for Ra :(
		 */
	
		lfobit[offset] = data & 1;
	
		/* R?? 330k to gnd */
		r0 = 1.0/330000;
		/* open is a very high value really ;-) */
		r1 = 1.0/1e12;
	
		/* R18 1M */
		if( lfobit[0]!=0 )
			r1 += 1.0/1000000;
		else
			r0 += 1.0/1000000;
	
		/* R17 470k */
		if( lfobit[1]!=0 )
			r1 += 1.0/470000;
		else
			r0 += 1.0/470000;
	
		/* R16 220k */
		if( lfobit[2]!=0 )
			r1 += 1.0/220000;
		else
			r0 += 1.0/220000;
	
		/* R15 100k */
		if( lfobit[3]!=0 )
			r1 += 1.0/100000;
		else
			r0 += 1.0/100000;
	
		if (lfotimer != null)
		{
			timer_remove( lfotimer );
			lfotimer = null;
		}
	
		r0 = 1.0/r0;
		r1 = 1.0/r1;
	
		/* I used an arbitrary value for max. Ra of 2M */
		rx = rx + 2000000.0 * r0 / (r0+r1);
	
		//LOG((errorlog, "lfotimer bits:%d%d%d%d r0:%d, r1:%d, rx: %d, time: %9.2fus\n", lfobit[3], lfobit[2], lfobit[1], lfobit[0], (int)r0, (int)r1, (int)rx, 0.639 * rx));
		lfotimer = timer_pulse( TIME_IN_USEC(0.639 * rx / (MAXFREQ-MINFREQ)), 0, lfo_timer_cb);
	} };
	
	public static ShUpdateHandlerPtr galaxian_sh_update = new ShUpdateHandlerPtr() { public void handler() 
	{
		/*
		 * NE555 8R, 8S and 8T are used as pulse position modulators
		 * FS1 Ra=100k, Rb=470k and C=0.01uF
		 *	. 0.693 * 1040k * 0.01uF . 7207.2us = 139Hz
		 * FS2 Ra=100k, Rb=330k and C=0.01uF
		 *	. 0.693 * 760k * 0.01uF . 5266.8us = 190Hz
		 * FS2 Ra=100k, Rb=220k and C=0.01uF
		 *	. 0.693 * 540k * 0.01uF . 3742.2us = 267Hz
		 */
	
		mixer_set_sample_frequency(channellfo+0, backgroundwave.length*freq*(100+2*470)/(100+2*470) );
		mixer_set_sample_frequency(channellfo+1, backgroundwave.length*freq*(100+2*300)/(100+2*470) );
		mixer_set_sample_frequency(channellfo+2, backgroundwave.length*freq*(100+2*220)/(100+2*470) );
	} };
}
