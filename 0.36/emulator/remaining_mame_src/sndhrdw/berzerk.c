/*
 * Berzerk/Frenzy Soundhardware Driver
 * Copyright Alex Judd 1997/98
 * V1.1 for Mame 0.31 13March98
 *
 * these were the original values but I have adjusted them to take account of the
 * default sample rate of 22050. I guess the original samples were at 24000.
 * case 0 : samplefrequency = 19500;
 * case 1 : samplefrequency = 20800;
 * case 2 : samplefrequency = 22320;
 * case 3 : samplefrequency = 24000;
 * case 4 : samplefrequency = 26000;
 * case 5 : samplefrequency = 28400;
 * case 6 : samplefrequency = 31250;
 * case 7 : samplefrequency = 34700;
 *
 */

#include "driver.h"
#include <math.h>



#define VOICE_PITCH_MODULATION	/* remove this to disable voice pitch modulation */

/* Note: We have the ability to replace the real death sound (33) and the
 * real voices with sample (34) as no good death sample could be obtained.
 */
//#define FAKE_DEATH_SOUND


/* volume and channel controls */
#define VOICE_VOLUME 100
#define SOUND_VOLUME 80
#define SOUND_CHANNEL 1    /* channel for sound effects */
#define VOICE_CHANNEL 5    /* special channel for voice sounds only */
#define DEATH_CHANNEL 6    /* special channel for fake death sound only */

/* berzerk sound */
int lastfreq = 0;
int lastnoise = 0;
int lastvoice = 0;
int lastdata = 0;

/* sound controls */
int berzerknoisemulate = 0;
int samplereset = 0;
int voicevolume = 100;
long samplefrequency = 22320;
extern int berzerkplayvoice; /* works in parallel with the read function in machine.c */
extern int collision;
int deathsound = 0;          /* trigger for playing collision sound */
int nextdata5 = -1;

int berzerk_sh_start(const struct MachineSound *msound)
{
	int i;


	berzerknoisemulate = 1;

	if (Machine->samples)
	{
		for (i = 0;i < 5;i++)
		{
			if (Machine->samples->sample[i])
				berzerknoisemulate = 0;
		}
	}
	return 0;
}

void berzerk_sound_control_a_w(int offset,int data)
{
	int noise = 0;
	int voice = 0;
	int voicefrequency = 0;

/* Throw out all the non-need sound info. (for the moment) */

	if (offset < 3) return;

/* Decode message for voice samples */

	if (offset==4)
	{
		if ((data & 0x40) == 0)
		{
			voice = data;
			berzerkplayvoice = 0;
		}
		else
		{
			/* We should use the volume and frequency on the voice samples */
			voicevolume = ((data & 0x38) >> 3);
			if (voicevolume > 0) voicevolume=255;

			voicefrequency = (data & 0x07);
#ifdef VOICE_PITCH_MODULATION
			switch(voicefrequency)
			{
			case 0 : samplefrequency = 17640; break;
			case 1 : samplefrequency = 19404; break;
			case 2 : samplefrequency = 20947; break;
			case 3 : samplefrequency = 22050; break;
			case 4 : samplefrequency = 26019; break;
			case 5 : samplefrequency = 27783; break;
			case 6 : samplefrequency = 31250; break;
			case 7 : samplefrequency = 34700; break;
			default: samplefrequency = 22050; break;
			}
#endif
			return;
		}
	}

/* Check for player fire sample reset */

	if ((offset==3) || (offset==5))
	{
		if (lastnoise==70)
		{
			if ((offset==3) && (data==172))
			{
				nextdata5 = 25;
				return;
			}

			if (offset==5)
			{
				if (data==nextdata5)
				{
#ifdef FAKE_DEATH_SOUND
					deathsound = 1; /* trigger death sound */
#else
					deathsound = 2; /* trigger death sound */
#endif
					lastnoise = 64; /* reset death sound */
				}

				nextdata5 = -1;
			}

			return;
		}

		if (lastnoise==69)
		{
			if ((offset==3) && (data==50))
			{
				nextdata5 = 50;
				return;
			}

			if (offset==5)
			{
				if (data==nextdata5)
				{
					lastnoise = 64; /* reset laser sound */
				}

				nextdata5 = -1;
			}
		}

		return;
	}

/* Check to see what game sound should be playing */

	if ((data > 60) && (data < 72) && (offset==6))
		noise = data;
	else
		noise = lastnoise;

/* One day I'll do this... */

	if (berzerknoisemulate)
	{
		return;
		if (voicefrequency != 1750*(4-noise))
		{
			voicefrequency = 1750*(4-noise);
			voicevolume = 85*noise;
		}

		if (noise)
		{
			sample_set_freq(2,voicefrequency);
			sample_set_volume(2,voicevolume);
		}
		else
		{
			sample_set_volume(2,0);
			voicevolume = 0;
		}
	}
	else
	{
		if ((offset==6) && (lastnoise != noise))  /* Game sound effects */
		{
			switch (noise)
			{
			case 69 : /* shot sample */
				sample_start(1,30,0);
				break;
			case 70 : /* baddie laser */
if (errorlog) fprintf(errorlog,"Trying death sound");
				switch(deathsound)
				{
				case 1 :
					sample_start(2,33,0);
					deathsound=0;
					break;
				case 2 :
					sample_start(DEATH_CHANNEL,34,0);
					deathsound=3;
					break;
				case 0 :
					sample_start(2,31,0);
					break;
				}
				break;
			case 71 : /* kill baddie */
				sample_start(3,32,0);
				break;
			default :
				break;
			}
		}
		lastnoise = noise;           /* make sure we only play the sound once */

		if (offset==4)               /* Game voice sounds */
		{
			if (deathsound<2)	       /* no voices for real death sound */
			{
				if (lastvoice==24 && voice==27)
				{
					lastvoice=voice;    /* catch for the 'chicken ayye' problem */
					return;
				}
				sample_start(VOICE_CHANNEL,voice,0);
#ifdef VOICE_PITCH_MODULATION
				sample_set_freq(VOICE_CHANNEL,samplefrequency);
#endif
				lastvoice=voice;
			}
		}
	} /* End of berzerknoisemulate */
}

void berzerk_sound_control_b_w(int offset,int data)
{
	if (errorlog)  fprintf(errorlog, "B Data value %d and offset %d at %d\n", data, offset, lastfreq);
}

void berzerk_sh_update(void)
{
	berzerkplayvoice = !sample_playing(VOICE_CHANNEL);
	if (deathsound==3 && sample_playing(DEATH_CHANNEL) == 0 && lastnoise != 70)
		deathsound=0;               /* reset death sound */
}
