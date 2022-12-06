package gr.codebb.arcadeflex.v037b7.sndhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.sound.streams.*;

public class asteroid {
	
	public static final int VMAX    =32767;
	public static final int VMIN	=0;
	
	public static final int SAUCEREN    =0;
	public static final int SAUCRFIREEN =1;
	public static final int SAUCERSEL   =2;
	public static final int THRUSTEN    =3;
	public static final int SHIPFIREEN	=4;
	public static final int LIFEEN		=5;
	
	public static final int EXPITCH0	=(1<<6);
	public static final int EXPITCH1	=(1<<7);
	public static final int EXPAUDSHIFT =2;
	public static final int EXPAUDMASK	=(0x0f<<EXPAUDSHIFT);
	
	static int NE555_T1(int Ra,int Rb,double C){ return	(int)(VMAX*2/3/(0.639*((Ra)+(Rb))*(C))); }
	static int NE555_T2(int Ra,int Rb,double C){ return	(int)(VMAX*2/3/(0.639*(Rb)*(C)));}
	static int NE555_F(int Ra,int Rb,double C){ return	(int)(1.44/(((Ra)+2*(Rb))*(C)));}
	static int channel;
	static int explosion_latch;
	static int thump_latch;
	static int[] sound_latch=new int[8];
	
	static int polynome;
	static int thump_frequency;
	
	static ShortPtr discharge;
	static short[] vol_explosion=new short[16];
	static int EXP(int charge,int n){ return (charge!=0 ? 0x7fff - discharge.read(0x7fff-n) : discharge.read(n));}

	static int exp_counter, exp_sample_counter;
	static int exp_out;
	public static int explosion(int samplerate)
	{


		exp_counter -= 12000;
		while( exp_counter <= 0 )
		{
			exp_counter += samplerate;
			if( ((polynome & 0x4000) == 0) == ((polynome & 0x0040) == 0) )
				polynome = (polynome << 1) | 1;
			else
				polynome <<= 1;
			if( ++exp_sample_counter == 16 )
			{
				exp_sample_counter = 0;
				if ((explosion_latch & EXPITCH0) != 0)
					exp_sample_counter |= 2 + 8;
				else
					exp_sample_counter |= 4;
				if ((explosion_latch & EXPITCH1) != 0)
					exp_sample_counter |= 1 + 8;
			}
			/* ripple count output is high? */
			if( exp_sample_counter == 15 )
				exp_out = polynome & 1;
		}
		if (exp_out != 0)
			return vol_explosion[(explosion_latch & EXPAUDMASK) >> EXPAUDSHIFT];
	
	    return 0;
	}
	static int turst_counter, turst_out, turst_amp;
	public static int thrust(int samplerate)
	{
	    if( sound_latch[THRUSTEN]!=0 )
		{
			/* SHPSND filter */
			turst_counter -= 110;
			while( turst_counter <= 0 )
			{
				turst_counter += samplerate;
				turst_out = polynome & 1;
			}
			if (turst_out != 0)
			{
				if( turst_amp < VMAX )
					turst_amp += (VMAX - turst_amp) * 32768 / 32 / samplerate + 1;
			}
			else
			{
				if( turst_amp > VMIN )
					turst_amp -= turst_amp * 32768 / 32 / samplerate + 1;
			}
			return turst_amp;
		}
		return 0;
	}
	static int thump_counter, thump_out;
	public static int thump(int samplerate)
	{
	    if ((thump_latch & 0x10) != 0)
		{
			thump_counter -= thump_frequency;
			while( thump_counter <= 0 )
			{
				thump_counter += samplerate;
				thump_out ^= 1;
			}
			if (thump_out != 0)
				return VMAX;
		}
		return 0;
	}

	static int sa_vco, sa_vco_charge, sa_vco_counter;
	static int sa_out, sa_counter;

	public static int saucer(int samplerate)
	{

		double v5;
	
	    /* saucer sound enabled ? */
		if( sound_latch[SAUCEREN]!=0 )
		{
			/* NE555 setup as astable multivibrator:
			 * C = 10u, Ra = 5.6k, Rb = 10k
			 * or, with /SAUCERSEL being low:
			 * C = 10u, Ra = 5.6k, Rb = 6k (10k parallel with 15k)
			 */
			if (sa_vco_charge != 0)
			{
				if( sound_latch[SAUCERSEL]!=0 )
					sa_vco_counter -= NE555_T1(5600,10000,10e-6);
				else
					sa_vco_counter -= NE555_T1(5600,6000,10e-6);
				if( sa_vco_counter <= 0 )
				{
					int steps = (-sa_vco_counter / samplerate) + 1;
					sa_vco_counter += steps * samplerate;
					if( (sa_vco += steps) >= VMAX*2/3 )
					{
						sa_vco = VMAX*2/3;
						sa_vco_charge = 0;
					}
				}
			}
			else
			{
				if( sound_latch[SAUCERSEL]!=0 )
					sa_vco_counter -= NE555_T2(5600,10000,10e-6);
				else
					sa_vco_counter -= NE555_T2(5600,6000,10e-6);
				if( sa_vco_counter <= 0 )
				{
					int steps = (-sa_vco_counter / samplerate) + 1;
					sa_vco_counter += steps * samplerate;
					if( (sa_vco -= steps) <= VMAX*1/3 )
					{
						sa_vco = VMIN*1/3;
						sa_vco_charge = 1;
					}
				}
			}
			/*
			 * NE566 voltage controlled oscillator
			 * Co = 0.047u, Ro = 10k
			 * to = 2.4 * (Vcc - V5) / (Ro * Co * Vcc)
			 */
			if( sound_latch[SAUCERSEL]!=0 )
				v5 = 12.0 - 1.66 - 5.0 * EXP(sa_vco_charge,sa_vco) / 32768;
			else
				v5 = 11.3 - 1.66 - 5.0 * EXP(sa_vco_charge,sa_vco) / 32768;
			sa_counter -= Math.floor(2.4 * (12.0 - v5) / (10000 * 0.047e-6 * 12.0));
			while( sa_counter <= 0 )
			{
				sa_counter += samplerate;
				sa_out ^= 1;
			}
			if (sa_out != 0)
				return VMAX;
		}
		return 0;
	}
	static int sau_vco, sau_vco_counter;
	static int sau_amp, sau_amp_counter;
	static int sau_out, sau_counter;
	public static int saucerfire(int samplerate)
	{
	    if( sound_latch[SAUCRFIREEN]!=0 )
		{
			if( sau_vco < VMAX*12/5 )
			{
				/* charge C38 (10u) through R54 (10K) from 5V to 12V */
				//#define C38_CHARGE_TIME (VMAX)
				sau_vco_counter -= VMAX;
				while( sau_vco_counter <= 0 )
				{
					sau_vco_counter += samplerate;
					if( ++sau_vco == VMAX*12/5 )
						break;
				}
			}
			if( sau_amp > VMIN )
			{
				/* discharge C39 (10u) through R58 (10K) and diode CR6,
				 * but only during the time the output of the NE555 is low.
				 */
				if (sau_out != 0)
				{
					//#define C39_DISCHARGE_TIME (int)(VMAX)
					sau_amp_counter -= (int)(VMAX);
					while( sau_amp_counter <= 0 )
					{
						sau_amp_counter += samplerate;
						if( --sau_amp == VMIN )
							break;
					}
				}
			}
			if (sau_out != 0)
			{
				/* C35 = 1u, Ra = 3.3k, Rb = 680
				 * discharge = 0.693 * 680 * 1e-6 = 4.7124e-4 . 2122 Hz
				 */
				sau_counter -= 2122;
				if( sau_counter <= 0 )
				{
					int n = -sau_counter / samplerate + 1;
					sau_counter += n * samplerate;
					sau_out = 0;
				}
			}
			else
			{
				/* C35 = 1u, Ra = 3.3k, Rb = 680
				 * charge 0.693 * (3300+680) * 1e-6 = 2.75814e-3 . 363Hz
				 */
				sau_counter -= 363 * 2 * (VMAX*12/5-sau_vco) / 32768;
				if( sau_counter <= 0 )
				{
					int n = -sau_counter / samplerate + 1;
					sau_counter += n * samplerate;
					sau_out = 1;
				}
			}
	        if (sau_out != 0)
				return sau_amp;
		}
		else
		{
			/* charge C38 and C39 */
			sau_amp = VMAX;
			sau_vco = VMAX;
		}
		return 0;
	}
	static int sh_vco, sh_vco_counter;
	static int sh_amp, sh_amp_counter;
	static int sh_out, sh_counter;
	public static int shipfire(int samplerate)
	{
	    if( sound_latch[SHIPFIREEN]!=0 )
		{
			if( sh_vco < VMAX*12/5 )
			{
				/* charge C47 (1u) through R52 (33K) and Q3 from 5V to 12V */
				//#define C47_CHARGE_TIME (VMAX * 3)
				sh_vco_counter -= (VMAX * 3);
				while( sh_vco_counter <= 0 )
				{
					sh_vco_counter += samplerate;
					if( ++sh_vco == VMAX*12/5 )
						break;
				}
	        }
			if( sh_amp > VMIN )
			{
				/* discharge C48 (10u) through R66 (2.7K) and CR8,
				 * but only while the output of theNE555 is low.
				 */
				if (sh_out != 0)
				{
					//#define C48_DISCHARGE_TIME (VMAX * 3)
					sh_amp_counter -=  (VMAX * 3);
					while( sh_amp_counter <= 0 )
					{
						sh_amp_counter += samplerate;
						if( --sh_amp == VMIN )
							break;
					}
				}
			}
	
			if (sh_out != 0)
			{
				/* C50 = 1u, Ra = 3.3k, Rb = 680
				 * discharge = 0.693 * 680 * 1e-6 = 4.7124e-4 . 2122 Hz
				 */
				sh_counter -= 2122;
				if( sh_counter <= 0 )
				{
					int n = -sh_counter / samplerate + 1;
					sh_counter += n * samplerate;
					sh_out = 0;
				}
			}
			else
			{
				/* C50 = 1u, Ra = R65 (3.3k), Rb = R61 (680)
				 * charge = 0.693 * (3300+680) * 1e-6) = 2.75814e-3 . 363Hz
				 */
				sh_counter -= 363 * 2 * (VMAX*12/5-sh_vco) / 32768;
				if( sh_counter <= 0 )
				{
					int n = -sh_counter / samplerate + 1;
					sh_counter += n * samplerate;
					sh_out = 1;
				}
			}
			if (sh_out != 0)
				return sh_amp;
		}
		else
		{
			/* charge C47 and C48 */
			sh_amp = VMAX;
			sh_vco = VMAX;
		}
		return 0;
	}
	static int life_counter, life_out;
	public static int life(int samplerate)
	{

	    if( sound_latch[LIFEEN]!=0 )
		{
			life_counter -= 3000;
			while( life_counter <= 0 )
			{
				life_counter += samplerate;
				life_out ^= 1;
			}
			if (life_out != 0)
				return VMAX;
		}
		return 0;
	}

	public static StreamInitPtr asteroid_sound_update = new StreamInitPtr() {
		public void handler(int param, ShortPtr buffer, int length) {
		int samplerate = Machine.sample_rate;
	
	    while( length-- > 0 )
		{
			int sum = 0;
	
			sum += explosion(samplerate) / 7;
			sum += thrust(samplerate) / 7;
			sum += thump(samplerate) / 7;
			sum += saucer(samplerate) / 7;
			sum += saucerfire(samplerate) / 7;
			sum += shipfire(samplerate) / 7;
			sum += life(samplerate) / 7;

			buffer.writeinc((short)sum);
		}
	}};
	
	static void explosion_init()
	{
		int i;
	
	    for( i = 0; i < 16; i++ )
	    {
	        /* r0 = open, r1 = open */
	        double r0 = 1.0/1e12, r1 = 1.0/1e12;
	
	        /* R14 */
	        if ((i & 1) != 0)
	            r1 += 1.0/47000;
	        else
	            r0 += 1.0/47000;
	        /* R15 */
	        if ((i & 2) != 0)
	            r1 += 1.0/22000;
	        else
	            r0 += 1.0/22000;
	        /* R16 */
	        if ((i & 4) != 0)
	            r1 += 1.0/12000;
	        else
	            r0 += 1.0/12000;
	        /* R17 */
	        if ((i & 8) != 0)
	            r1 += 1.0/5600;
	        else
	            r0 += 1.0/5600;
	        r0 = 1.0/r0;
	        r1 = 1.0/r1;
	        vol_explosion[i] = (short)(VMAX * r0 / (r0 + r1));
	    }
	
	}
	
	public static ShStartHandlerPtr asteroid_sh_start = new ShStartHandlerPtr() { public int handler(MachineSound msound) 
	{
	    int i;
	
		discharge = new ShortPtr(32768*2);//(INT16 *)malloc(32768 * sizeof(INT16));
		if( discharge==null )
	        return 1;
	
	    for( i = 0; i < 0x8000; i++ )
			discharge.write(0x7fff-i,(short) (0x7fff/Math.exp(1.0*i/4096)));
	
		/* initialize explosion volume lookup table */
		explosion_init();
	
	    channel = stream_init("Custom", 100, Machine.sample_rate, 0, asteroid_sound_update);
	    if( channel == -1 )
	        return 1;
	
	    return 0;
	} };
	
	public static ShStopHandlerPtr asteroid_sh_stop = new ShStopHandlerPtr() { public void handler() 
	{
		if (discharge != null)
			discharge = null;
	} };
	
	public static ShUpdateHandlerPtr asteroid_sh_update = new ShUpdateHandlerPtr() { public void handler() 
	{
		stream_update(channel, 0);
	} };
	
	
	public static WriteHandlerPtr asteroid_explode_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if( data == explosion_latch )
			return;
	
	    stream_update(channel, 0);
		explosion_latch = data;
	} };
	
	
	
	public static WriteHandlerPtr asteroid_thump_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		double r0 = 1/47000, r1 = 1/1e12;
	
	    if( data == thump_latch )
			return;
	
	    stream_update(channel, 0);
		thump_latch = data;
	
		if ((thump_latch & 1) != 0)
			r1 += 1.0/220000;
		else
			r0 += 1.0/220000;
		if ((thump_latch & 2) != 0)
			r1 += 1.0/100000;
		else
			r0 += 1.0/100000;
		if ((thump_latch & 4) != 0)
			r1 += 1.0/47000;
		else
			r0 += 1.0/47000;
		if ((thump_latch & 8) != 0)
			r1 += 1.0/22000;
		else
			r0 += 1.0/22000;
	
		/* NE555 setup as voltage controlled astable multivibrator
		 * C = 0.22u, Ra = 22k...???, Rb = 18k
		 * frequency = 1.44 / ((22k + 2*18k) * 0.22n) = 56Hz .. huh?
		 */
		thump_frequency = (int)(56 + 56 * r0 / (r0 + r1));
	} };
	
	
	public static WriteHandlerPtr asteroid_sounds_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		data &= 0x80;
	    if( data == sound_latch[offset] )
			return;
	
	    stream_update(channel, 0);
		sound_latch[offset] = data;
	} };


	public static StreamInitPtr astdelux_sound_update = new StreamInitPtr() {
		public void handler(int param, ShortPtr buffer, int length) {
		int samplerate = Machine.sample_rate;
	
	    while( length-- > 0)
		{
			int sum = 0;
	
			sum += explosion(samplerate) / 2;
			sum += thrust(samplerate) / 2;
	
			buffer.writeinc((short)sum);
		}
	}};
	
	public static ShStartHandlerPtr astdelux_sh_start = new ShStartHandlerPtr() { public int handler(MachineSound msound) 
	{
		/* initialize explosion volume lookup table */
		explosion_init();
	
		channel = stream_init("Custom", 50, Machine.sample_rate, 0, astdelux_sound_update);
	    if( channel == -1 )
	        return 1;
	
	    return 0;
	} };
	
	public static ShStopHandlerPtr astdelux_sh_stop = new ShStopHandlerPtr() { public void handler() 
	{
	} };
	
	public static ShUpdateHandlerPtr astdelux_sh_update = new ShUpdateHandlerPtr() { public void handler() 
	{
		stream_update(channel, 0);
	} };
	
	
	public static WriteHandlerPtr astdelux_sounds_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		data = ~data & 0x80;
		if( data == sound_latch[THRUSTEN] )
			return;
	    stream_update(channel, 0);
		sound_latch[THRUSTEN] = data;
	} };
	
    
}
