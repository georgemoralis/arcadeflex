
/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package sndhrdw;
import static mame.driverH.*;
import static mame.cpuintrf.*;
import static mame.cpuintrfH.*;
import static mame.sndintrf.*;
import sound.samplesH.Samplesinterface;

public class astrof
{
	
	/* Make sure that the sample name definitions in drivers/astrof.c matches these */
	
	public static final int SAMPLE_FIRE		= 0;
	public static final int SAMPLE_EKILLED	 =1;
	public static final int SAMPLE_WAVE		 =2;
	public static final int SAMPLE_BOSSFIRE  =6;
	public static final int SAMPLE_FUEL		 =7;
	public static final int SAMPLE_DEATH	 =8;
	public static final int SAMPLE_BOSSHIT	 =9;
	public static final int SAMPLE_BOSSKILL  =10;
	
	public static final int CHANNEL_FIRE	  =0;
	public static final int CHANNEL_EXPLOSION =1;
	public static final int CHANNEL_WAVE      =2;  /* Background humm */
	public static final int CHANNEL_BOSSFIRE  =2;	  /* There is no background humm on the boss level */
	public static final int CHANNEL_FUEL	  =3;
	
	
	/* Make sure that the public static final int's in sndhrdw/astrof.c matches these */
	static String astrof_sample_names[] =
	{
		"*astrof",
		"fire.wav",
		"ekilled.wav",
		"wave1.wav",
		"wave2.wav",
		"wave3.wav",
		"wave4.wav",
		"bossfire.wav",
		"fuel.wav",
		"death.wav",
		"bosshit.wav",
		"bosskill.wav",
		null   /* end of array */
	};
	
	public static Samplesinterface astrof_samples_interface = new Samplesinterface
	(
		4,	/* 4 channels */
		25,	/* volume */
		astrof_sample_names
        );
	
	static String tomahawk_sample_names[] =
	{
		"*tomahawk",
		/* We don't have these yet */
		null   /* end of array */
	};
	
	public static Samplesinterface tomahawk_samples_interface = new Samplesinterface
        (
		1,	/* 1 channel for now */
		25,	/* volume */
		tomahawk_sample_names
        );
	
	
	static int start_explosion = 0;
	static int death_playing = 0;
	static int bosskill_playing = 0;
	
	public static WriteHandlerPtr astrof_sample1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
/*TODO*///		static int last = 0;
	
/*TODO*///		if (death_playing != 0)
/*TODO*///		{
/*TODO*///			death_playing = sample_playing(CHANNEL_EXPLOSION);
/*TODO*///		}
/*TODO*///	
/*TODO*///		if (bosskill_playing != 0)
/*TODO*///		{
/*TODO*///			bosskill_playing = sample_playing(CHANNEL_EXPLOSION);
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* Bit 2 - Explosion trigger */
/*TODO*///		if ((data & 0x04) && !(last & 0x04))
/*TODO*///		{
/*TODO*///			/* I *know* that the effect select port will be written shortly
/*TODO*///			   after this one, so this works */
/*TODO*///			start_explosion = 1;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* Bit 0/1/3 - Background noise */
/*TODO*///		if ((data & 0x08) != (last & 0x08))
/*TODO*///		{
/*TODO*///			if ((data & 0x08) != 0)
/*TODO*///			{
/*TODO*///				int sample = SAMPLE_WAVE + (data & 3);
/*TODO*///				sample_start(CHANNEL_WAVE,sample,1);
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				sample_stop(CHANNEL_WAVE);
/*TODO*///			}
/*TODO*///		}
	
		/* Bit 4 - Boss Laser */
/*TODO*///		if ((data & 0x10) && !(last & 0x10))
/*TODO*///		{
/*TODO*///			if (!bosskill_playing)
/*TODO*///			{
/*TODO*///				sample_start(CHANNEL_BOSSFIRE,SAMPLE_BOSSFIRE,0);
/*TODO*///			}
/*TODO*///		}
	
		/* Bit 5 - Fire */
/*TODO*///		if ((data & 0x20) && !(last & 0x20))
/*TODO*///		{
/*TODO*///			if (!bosskill_playing)
/*TODO*///			{
/*TODO*///				sample_start(CHANNEL_FIRE,SAMPLE_FIRE,0);
/*TODO*///			}
/*TODO*///		}
	
		/* Bit 6 - Don't know. Probably something to do with the explosion sounds */
	
		/* Bit 7 - Don't know. Maybe a global sound enable bit? */
	
/*TODO*///		last = data;
	} };
	
	
	public static WriteHandlerPtr astrof_sample2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
/*TODO*///		static int last = 0;
	
		/* Bit 0-2 Explosion select (triggered by Bit 2 of the other port */
/*TODO*///		if (start_explosion != 0)
/*TODO*///		{
/*TODO*///			if ((data & 0x04) != 0)
/*TODO*///			{
/*TODO*///				/* This is really a compound effect, made up of I believe 3 sound
/*TODO*///				   effects, but since our sample contains them all, disable playing
/*TODO*///				   the other effects while the explosion is playing */
/*TODO*///				if (!bosskill_playing)
/*TODO*///				{
/*TODO*///					sample_start(CHANNEL_EXPLOSION,SAMPLE_BOSSKILL,0);
/*TODO*///					bosskill_playing = 1;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			else if ((data & 0x02) != 0)
/*TODO*///			{
/*TODO*///				sample_start(CHANNEL_EXPLOSION,SAMPLE_BOSSHIT,0);
/*TODO*///			}
/*TODO*///			else if ((data & 0x01) != 0)
/*TODO*///			{
/*TODO*///				sample_start(CHANNEL_EXPLOSION,SAMPLE_EKILLED,0);
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				if (!death_playing)
/*TODO*///				{
/*TODO*///					sample_start(CHANNEL_EXPLOSION,SAMPLE_DEATH,0);
/*TODO*///					death_playing = 1;
/*TODO*///				}
/*TODO*///			}
	
/*TODO*///			start_explosion = 0;
/*TODO*///		}
	
		/* Bit 3 - Low Fuel Warning */
/*TODO*///		if ((data & 0x08) && !(last & 0x08))
/*TODO*///		{
/*TODO*///			sample_start(CHANNEL_FUEL,SAMPLE_FUEL,0);
/*TODO*///		}
/*TODO*///	
/*TODO*///		last = data;
	} };
	
}
