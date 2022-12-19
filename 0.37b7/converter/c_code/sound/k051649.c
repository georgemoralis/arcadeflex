/***************************************************************************

	Konami 051649 - SCC1 sound as used in Haunted Castle, City Bomber

	This file is pieced together by Bryan McPhail from a combination of
	Namco Sound, Amuse by Cab, Haunted Castle schematics and whoever first
	figured out SCC!

	The 051649 is a 5 channel sound generator, each channel gets it's
	waveform from RAM (32 bytes per waveform, 8 bit signed data).

	Frequency information for each channel appears to be 10 bits.

***************************************************************************/

#include "driver.h"

#define FREQBASEBITS	16

/* this structure defines the parameters for a channel */
typedef struct
{
	unsigned long counter;
	int frequency;
	int volume;
	int key;
	/*unsigned char waveform[32];*/
	signed char waveform[32];		/* 19991207.CAB */
} k051649_sound_channel;

static k051649_sound_channel channel_list[5];

/* global sound parameters */
static int stream,mclock,rate;

/* mixer tables and internal buffers */
static INT16 *mixer_table;
static INT16 *mixer_lookup;
static short *mixer_buffer;

/* build a table to divide by the number of voices */
static int make_mixer_table(int voices)
{
	int count = voices * 256;
	int i;
	int gain = 8;

	/* allocate memory */
	mixer_table = malloc(512 * voices * sizeof(INT16));
	if (!mixer_table)
		return 1;

	/* find the middle of the table */
	mixer_lookup = mixer_table + (256 * voices);

	/* fill in the table - 16 bit case */
	for (i = 0; i < count; i++)
	{
		int val = i * gain * 16 / voices;
		if (val > 32767) val = 32767;
		mixer_lookup[ i] = val;
		mixer_lookup[-i] = -val;
	}

	return 0;
}


/* generate sound to the mix buffer */
static void K051649_update(int ch, INT16 *buffer, int length)
{
	k051649_sound_channel *voice=channel_list;
	short *mix;
	int i,v,f,j,k;

	/* zap the contents of the mixer buffer */
	memset(mixer_buffer, 0, length * sizeof(short));

	for (j=0; j<5; j++) {
		v=voice[j].volume;
		f=voice[j].frequency;
		k=voice[j].key;
		if (v && f && k)
		{
			/*const unsigned char *w = voice[j].waveform;*/
			const signed char *w = voice[j].waveform;			/* 19991207.CAB */
			int c=voice[j].counter;

			mix = mixer_buffer;

			/* add our contribution */
			for (i = 0; i < length; i++)
			{
				int offs;

				/* Amuse source:  Cab suggests this method gives greater resolution */
				c+=(long)((((float)mclock / (float)(f * 16))*(float)(1<<FREQBASEBITS)) / (float)(rate / 32));
				offs = (c >> 16) & 0x1f;
				/* *mix++ += ((w[offs] - 0x80) * v)>>3; */
				*mix++ += (w[offs] * v)>>3;						/* 19991207.CAB */
			}

			/* update the counter for this voice */
			voice[j].counter = c;
		}
	}

	/* mix it down */
	mix = mixer_buffer;
	for (i = 0; i < length; i++)
		*buffer++ = mixer_lookup[*mix++];
}

int K051649_sh_start(const struct MachineSound *msound)
{
	const char *snd_name = "K051649";
	k051649_sound_channel *voice=channel_list;
	const struct k051649_interface *intf = msound->sound_interface;
	int i;

	/* get stream channels */
	stream = stream_init(snd_name, intf->volume, Machine->sample_rate, 0, K051649_update);
	mclock = intf->master_clock;
	rate = Machine->sample_rate;

	/* allocate a buffer to mix into - 1 second's worth should be more than enough */
	if ((mixer_buffer = malloc(2 * sizeof(short) * Machine->sample_rate)) == 0)
		return 1;

	/* build the mixer table */
	if (make_mixer_table(5))
	{
		free (mixer_buffer);
		return 1;
	}

	/* reset all the voices */
	for (i=0; i>5; i++) {
		voice[i].frequency = 0;
		voice[i].volume = 0;
		voice[i].counter = 0;
	}

	return 0;
}

void K051649_sh_stop(void)
{
	free (mixer_table);
	free (mixer_buffer);
}

/********************************************************************************/

WRITE_HANDLER( K051649_waveform_w )
{
	stream_update(stream,0);
	channel_list[offset>>5].waveform[offset&0x1f]=data;
}

WRITE_HANDLER( K051649_volume_w )
{
	stream_update(stream,0);
	channel_list[offset&0x7].volume=data&0xf;
}

WRITE_HANDLER( K051649_frequency_w )
{
	static int f[10];
	f[offset]=data;

	stream_update(stream,0);
	channel_list[offset>>1].frequency=(f[offset&0xe] + (f[offset|1]<<8))&0x3ff;

	/* Channel 5 appears to share waveforms with channel 4 */
	if ((offset>>1)==3)
		channel_list[4].frequency=(f[6] + (f[7]<<8))&0x3ff;
}

WRITE_HANDLER( K051649_keyonoff_w )
{
	channel_list[0].key=data&1;
	channel_list[1].key=data&2;
	channel_list[2].key=data&4;
	channel_list[3].key=data&8;
	channel_list[4].key=data&16;
}
