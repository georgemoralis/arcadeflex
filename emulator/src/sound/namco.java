/***************************************************************************

	NAMCO sound driver.

	This driver handles the three known types of NAMCO wavetable sounds:

		- 3-voice mono (Pac-Man, Pengo, Dig Dug, etc)
		- 8-voice mono (Mappy, Dig Dug 2, etc)
		- 8-voice stereo (System 1)
		- 6-voice stereo (Pole Position 1, Pole Position 2)

***************************************************************************/
package sound;
import static arcadeflex.libc_old.*;
import mame.sndintrf;
import mame.sndintrfH;
import static mame.sndintrfH.*;
import static sound.namcoH.*;
import static mame.driverH.*;

public class namco extends sndintrf.snd_interface{
    public namco()
    {
        sound_num=SOUND_NAMCO;
        name="Namco";
    }
    @Override
    public int chips_num(MachineSound msound) {
        return 0;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return 0;
    }
    /*TODO*////* 8 voices max */
    /*TODO*///#define MAX_VOICES 8
    /*TODO*///
    /*TODO*///
    /*TODO*////* this structure defines the parameters for a channel */
    /*TODO*///typedef struct
    /*TODO*///{
    /*TODO*///	int frequency;
    /*TODO*///	int counter;
    /*TODO*///	int volume[2];
    /*TODO*///	int noise_sw;
    /*TODO*///	int noise_state;
    /*TODO*///	int noise_seed;
    /*TODO*///	int noise_counter;
    /*TODO*///	const unsigned char *wave;
    /*TODO*///} sound_channel;
    /*TODO*///
    /*TODO*///
    /*TODO*////* globals available to everyone */
    public static CharPtr namco_soundregs = new CharPtr();//unsigned char *namco_soundregs;
    /*TODO*///unsigned char *namco_wavedata;
    /*TODO*///
    /*TODO*////* data about the sound system */
    /*TODO*///static sound_channel channel_list[MAX_VOICES];
    /*TODO*///static sound_channel *last_channel;
    /*TODO*///
    /*TODO*////* global sound parameters */
    /*TODO*///static const unsigned char *sound_prom;
    /*TODO*///static int samples_per_byte;
    /*TODO*///static int num_voices;
    /*TODO*///static int sound_enable;
    /*TODO*///static int stream;
    /*TODO*///static int namco_clock;
    /*TODO*///static int sample_rate;
    /*TODO*///
    /*TODO*////* mixer tables and internal buffers */
    /*TODO*///static INT16 *mixer_table;
    /*TODO*///static INT16 *mixer_lookup;
    /*TODO*///static short *mixer_buffer;
    /*TODO*///static short *mixer_buffer_2;
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*////* build a table to divide by the number of voices */
    /*TODO*///static int make_mixer_table(int voices)
    /*TODO*///{
    /*TODO*///	int count = voices * 128;
    /*TODO*///	int i;
    /*TODO*///	int gain = 16;
    /*TODO*///
    /*TODO*///
    /*TODO*///	/* allocate memory */
    /*TODO*///	mixer_table = malloc(256 * voices * sizeof(INT16));
    /*TODO*///	if (!mixer_table)
    /*TODO*///		return 1;
    /*TODO*///
    /*TODO*///	/* find the middle of the table */
    /*TODO*///	mixer_lookup = mixer_table + (128 * voices);
    /*TODO*///
    /*TODO*///	/* fill in the table - 16 bit case */
    /*TODO*///	for (i = 0; i < count; i++)
    /*TODO*///	{
    /*TODO*///		int val = i * gain * 16 / voices;
    /*TODO*///		if (val > 32767) val = 32767;
    /*TODO*///		mixer_lookup[ i] = val;
    /*TODO*///		mixer_lookup[-i] = -val;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	return 0;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////* generate sound to the mix buffer in mono */
    /*TODO*///static void namco_update_mono(int ch, INT16 *buffer, int length)
    /*TODO*///{
    /*TODO*///	sound_channel *voice;
    /*TODO*///	short *mix;
    /*TODO*///	int i;
    /*TODO*///
    /*TODO*///	/* if no sound, we're done */
    /*TODO*///	if (sound_enable == 0)
    /*TODO*///	{
    /*TODO*///		memset(buffer, 0, length * sizeof(INT16));
    /*TODO*///		return;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* zap the contents of the mixer buffer */
    /*TODO*///	memset(mixer_buffer, 0, length * sizeof(short));
    /*TODO*///
    /*TODO*///	/* loop over each voice and add its contribution */
    /*TODO*///	for (voice = channel_list; voice < last_channel; voice++)
    /*TODO*///	{
    /*TODO*///		int f = voice->frequency;
    /*TODO*///		int v = voice->volume[0];
    /*TODO*///
    /*TODO*///		mix = mixer_buffer;
    /*TODO*///
    /*TODO*///		if (voice->noise_sw)
    /*TODO*///		{
    /*TODO*///			/* only update if we have non-zero volume and frequency */
    /*TODO*///			if (v && (f & 0xff))
    /*TODO*///			{
    /*TODO*///				float fbase = (float)sample_rate / (float)namco_clock;
    /*TODO*///				int delta = (float)((f & 0xff) << 4) * fbase;
    /*TODO*///				int c = voice->noise_counter;
    /*TODO*///
    /*TODO*///				/* add our contribution */
    /*TODO*///				for (i = 0; i < length; i++)
    /*TODO*///				{
    /*TODO*///					int noise_data;
    /*TODO*///					int cnt;
    /*TODO*///
    /*TODO*///					if (voice->noise_state)	noise_data = 0x07;
    /*TODO*///					else noise_data = -0x07;
    /*TODO*///					*mix++ += noise_data * (v >> 1);
    /*TODO*///
    /*TODO*///					c += delta;
    /*TODO*///					cnt = (c >> 12);
    /*TODO*///					c &= (1 << 12) - 1;
    /*TODO*///					for( ;cnt > 0; cnt--)
    /*TODO*///					{
    /*TODO*///						if ((voice->noise_seed + 1) & 2) voice->noise_state ^= 1;
    /*TODO*///						if (voice->noise_seed & 1) voice->noise_seed ^= 0x28000;
    /*TODO*///						voice->noise_seed >>= 1;
    /*TODO*///					}
    /*TODO*///				}
    /*TODO*///
    /*TODO*///				/* update the counter for this voice */
    /*TODO*///				voice->noise_counter = c;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///		else
    /*TODO*///		{
    /*TODO*///			/* only update if we have non-zero volume and frequency */
    /*TODO*///			if (v && f)
    /*TODO*///			{
    /*TODO*///				const unsigned char *w = voice->wave;
    /*TODO*///				int c = voice->counter;
    /*TODO*///
    /*TODO*///				/* add our contribution */
    /*TODO*///				for (i = 0; i < length; i++)
    /*TODO*///				{
    /*TODO*///					int offs;
    /*TODO*///
    /*TODO*///					c += f;
    /*TODO*///					offs = (c >> 15) & 0x1f;
    /*TODO*///					if (samples_per_byte == 1)	/* use only low 4 bits */
    /*TODO*///						*mix++ += ((w[offs] & 0x0f) - 8) * v;
    /*TODO*///					else	/* use full byte, first the high 4 bits, then the low 4 bits */
    /*TODO*///					{
    /*TODO*///						if (offs & 1)
    /*TODO*///							*mix++ += ((w[offs>>1] & 0x0f) - 8) * v;
    /*TODO*///						else
    /*TODO*///							*mix++ += (((w[offs>>1]>>4) & 0x0f) - 8) * v;
    /*TODO*///					}
    /*TODO*///				}
    /*TODO*///
    /*TODO*///				/* update the counter for this voice */
    /*TODO*///				voice->counter = c;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* mix it down */
    /*TODO*///	mix = mixer_buffer;
    /*TODO*///	for (i = 0; i < length; i++)
    /*TODO*///		*buffer++ = mixer_lookup[*mix++];
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////* generate sound to the mix buffer in stereo */
    /*TODO*///static void namco_update_stereo(int ch, INT16 **buffer, int length)
    /*TODO*///{
    /*TODO*///	sound_channel *voice;
    /*TODO*///	short *lmix, *rmix;
    /*TODO*///	int i;
    /*TODO*///
    /*TODO*///	/* if no sound, we're done */
    /*TODO*///	if (sound_enable == 0)
    /*TODO*///	{
    /*TODO*///		memset(buffer[0], 0, length * sizeof(INT16));
    /*TODO*///		memset(buffer[1], 0, length * sizeof(INT16));
    /*TODO*///		return;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* zap the contents of the mixer buffer */
    /*TODO*///	memset(mixer_buffer, 0, length * sizeof(INT16));
    /*TODO*///	memset(mixer_buffer_2, 0, length * sizeof(INT16));
    /*TODO*///
    /*TODO*///	/* loop over each voice and add its contribution */
    /*TODO*///	for (voice = channel_list; voice < last_channel; voice++)
    /*TODO*///	{
    /*TODO*///		int f = voice->frequency;
    /*TODO*///		int lv = voice->volume[0];
    /*TODO*///		int rv = voice->volume[1];
    /*TODO*///
    /*TODO*///		lmix = mixer_buffer;
    /*TODO*///		rmix = mixer_buffer_2;
    /*TODO*///
    /*TODO*///		if (voice->noise_sw)
    /*TODO*///		{
    /*TODO*///			/* only update if we have non-zero volume and frequency */
    /*TODO*///			if ((lv || rv) && (f & 0xff))
    /*TODO*///			{
    /*TODO*///				float fbase = (float)sample_rate / (float)namco_clock;
    /*TODO*///				int delta = (float)((f & 0xff) << 4) * fbase;
    /*TODO*///				int c = voice->noise_counter;
    /*TODO*///
    /*TODO*///				/* add our contribution */
    /*TODO*///				for (i = 0; i < length; i++)
    /*TODO*///				{
    /*TODO*///					int noise_data;
    /*TODO*///					int cnt;
    /*TODO*///
    /*TODO*///					if (voice->noise_state)	noise_data = 0x07;
    /*TODO*///					else noise_data = -0x07;
    /*TODO*///					*lmix++ += noise_data * (lv >> 1);
    /*TODO*///					*rmix++ += noise_data * (rv >> 1);
    /*TODO*///
    /*TODO*///					c += delta;
    /*TODO*///					cnt = (c >> 12);
    /*TODO*///					c &= (1 << 12) - 1;
    /*TODO*///					for( ;cnt > 0; cnt--)
    /*TODO*///					{
    /*TODO*///						if ((voice->noise_seed + 1) & 2) voice->noise_state ^= 1;
    /*TODO*///						if (voice->noise_seed & 1) voice->noise_seed ^= 0x28000;
    /*TODO*///						voice->noise_seed >>= 1;
    /*TODO*///					}
    /*TODO*///				}
    /*TODO*///
    /*TODO*///				/* update the counter for this voice */
    /*TODO*///				voice->noise_counter = c;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///		else
    /*TODO*///		{
    /*TODO*///			/* only update if we have non-zero volume and frequency */
    /*TODO*///			if ((lv || rv) && f)
    /*TODO*///			{
    /*TODO*///				const unsigned char *w = voice->wave;
    /*TODO*///				int c = voice->counter;
    /*TODO*///
    /*TODO*///				/* add our contribution */
    /*TODO*///				for (i = 0; i < length; i++)
    /*TODO*///				{
    /*TODO*///					int offs;
    /*TODO*///
    /*TODO*///					c += f;
    /*TODO*///					offs = (c >> 15) & 0x1f;
    /*TODO*///					if (samples_per_byte == 1)	/* use only low 4 bits */
    /*TODO*///					{
    /*TODO*///						*lmix++ += ((w[offs] & 0x0f) - 8) * lv;
    /*TODO*///						*rmix++ += ((w[offs] & 0x0f) - 8) * rv;
    /*TODO*///					}
    /*TODO*///					else	/* use full byte, first the high 4 bits, then the low 4 bits */
    /*TODO*///					{
    /*TODO*///						if (offs & 1)
    /*TODO*///						{
    /*TODO*///							*lmix++ += ((w[offs>>1] & 0x0f) - 8) * lv;
    /*TODO*///							*rmix++ += ((w[offs>>1] & 0x0f) - 8) * rv;
    /*TODO*///						}
    /*TODO*///						else
    /*TODO*///						{
    /*TODO*///							*lmix++ += (((w[offs>>1]>>4) & 0x0f) - 8) * lv;
    /*TODO*///							*rmix++ += (((w[offs>>1]>>4) & 0x0f) - 8) * rv;
    /*TODO*///						}
    /*TODO*///					}
    /*TODO*///				}
    /*TODO*///
    /*TODO*///				/* update the counter for this voice */
    /*TODO*///				voice->counter = c;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* mix it down */
    /*TODO*///	lmix = mixer_buffer;
    /*TODO*///	rmix = mixer_buffer_2;
    /*TODO*///	{
    /*TODO*///		INT16 *dest1 = buffer[0];
    /*TODO*///		INT16 *dest2 = buffer[1];
    /*TODO*///		for (i = 0; i < length; i++)
    /*TODO*///		{
    /*TODO*///			*dest1++ = mixer_lookup[*lmix++];
    /*TODO*///			*dest2++ = mixer_lookup[*rmix++];
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    @Override
    public int start(MachineSound msound) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    
    /*TODO*///	const char *mono_name = "NAMCO sound";
    /*TODO*///	const char *stereo_names[] =
    /*TODO*///	{
    /*TODO*///		"NAMCO sound left",
    /*TODO*///		"NAMCO sound right"
    /*TODO*///	};
    /*TODO*///	sound_channel *voice;
    /*TODO*///	const struct namco_interface *intf = msound->sound_interface;
    /*TODO*///
    /*TODO*///	namco_clock = intf->samplerate;
    /*TODO*///	sample_rate = Machine->sample_rate;
    /*TODO*///
    /*TODO*///	/* get stream channels */
    /*TODO*///	if (intf->stereo)
    /*TODO*///	{
    /*TODO*///		int vol[2];
    /*TODO*///
    /*TODO*///		vol[0] = MIXER(intf->volume,MIXER_PAN_LEFT);
    /*TODO*///		vol[1] = MIXER(intf->volume,MIXER_PAN_RIGHT);
    /*TODO*///		stream = stream_init_multi(2, stereo_names, vol, intf->samplerate, 0, namco_update_stereo);
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		stream = stream_init(mono_name, intf->volume, intf->samplerate, 0, namco_update_mono);
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* allocate a pair of buffers to mix into - 1 second's worth should be more than enough */
    /*TODO*///	if ((mixer_buffer = malloc(2 * sizeof(short) * intf->samplerate)) == 0)
    /*TODO*///		return 1;
    /*TODO*///	mixer_buffer_2 = mixer_buffer + intf->samplerate;
    /*TODO*///
    /*TODO*///	/* build the mixer table */
    /*TODO*///	if (make_mixer_table(intf->voices))
    /*TODO*///	{
    /*TODO*///		free (mixer_buffer);
    /*TODO*///		return 1;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* extract globals from the interface */
    /*TODO*///	num_voices = intf->voices;
    /*TODO*///	last_channel = channel_list + num_voices;
    /*TODO*///
    /*TODO*///	if (intf->region == -1)
    /*TODO*///	{
    /*TODO*///		sound_prom = namco_wavedata;
    /*TODO*///		samples_per_byte = 2;	/* first 4 high bits, then low 4 bits */
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		sound_prom = memory_region(intf->region);
    /*TODO*///		samples_per_byte = 1;	/* use only low 4 bits */
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* start with sound enabled, many games don't have a sound enable register */
    /*TODO*///	sound_enable = 1;
    /*TODO*///
    /*TODO*///	/* reset all the voices */
    /*TODO*///	for (voice = channel_list; voice < last_channel; voice++)
    /*TODO*///	{
    /*TODO*///		voice->frequency = 0;
    /*TODO*///		voice->volume[0] = voice->volume[1] = 0;
    /*TODO*///		voice->wave = &sound_prom[0];
    /*TODO*///		voice->counter = 0;
    /*TODO*///		voice->noise_sw = 0;
    /*TODO*///		voice->noise_state = 0;
    /*TODO*///		voice->noise_seed = 1;
    /*TODO*///		voice->noise_counter = 0;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	return 0;
    }
    /*TODO*///
    /*TODO*///
    @Override
    public void stop() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    /*TODO*///	free (mixer_table);
    /*TODO*///	free (mixer_buffer);
    }
    /*TODO*///
    /*TODO*///
    /*TODO*////********************************************************************************/
    /*TODO*///
    /*TODO*///
    public static WriteHandlerPtr pengo_sound_enable_w = new WriteHandlerPtr() { public void handler(int offset, int data)
    {
    /*TODO*///	sound_enable = data;
        throw new UnsupportedOperationException("Not supported yet.");
    }};
    public static WriteHandlerPtr pengo_sound_w = new WriteHandlerPtr() { public void handler(int offset, int data)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    /*TODO*///	sound_channel *voice;
    /*TODO*///	int base;
    /*TODO*///
    /*TODO*///	/* update the streams */
    /*TODO*///	stream_update(stream, 0);
    /*TODO*///
    /*TODO*///	/* set the register */
    /*TODO*///	namco_soundregs[offset] = data & 0x0f;
    /*TODO*///
    /*TODO*///	/* recompute all the voice parameters */
    /*TODO*///	for (base = 0, voice = channel_list; voice < last_channel; voice++, base += 5)
    /*TODO*///	{
    /*TODO*///		voice->frequency = namco_soundregs[0x14 + base];	/* always 0 */
    /*TODO*///		voice->frequency = voice->frequency * 16 + namco_soundregs[0x13 + base];
    /*TODO*///		voice->frequency = voice->frequency * 16 + namco_soundregs[0x12 + base];
    /*TODO*///		voice->frequency = voice->frequency * 16 + namco_soundregs[0x11 + base];
    /*TODO*///		if (base == 0)	/* the first voice has extra frequency bits */
    /*TODO*///			voice->frequency = voice->frequency * 16 + namco_soundregs[0x10 + base];
    /*TODO*///		else
    /*TODO*///			voice->frequency = voice->frequency * 16;
    /*TODO*///
    /*TODO*///		voice->volume[0] = namco_soundregs[0x15 + base] & 0x0f;
    /*TODO*///		voice->wave = &sound_prom[32 * (namco_soundregs[0x05 + base] & 7)];
    /*TODO*///	}
    }};
    /*TODO*///
    /*TODO*///
    /*TODO*////********************************************************************************/
    /*TODO*///
    /*TODO*///void polepos_sound_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	sound_channel *voice;
    /*TODO*///	int base;
    /*TODO*///
    /*TODO*///	/* update the streams */
    /*TODO*///	stream_update(stream, 0);
    /*TODO*///
    /*TODO*///	/* set the register */
    /*TODO*///	namco_soundregs[offset] = data;
    /*TODO*///
    /*TODO*///	/* recompute all the voice parameters */
    /*TODO*///	for (base = 8, voice = channel_list; voice < last_channel; voice++, base += 4)
    /*TODO*///	{
    /*TODO*///		voice->frequency = namco_soundregs[0x01 + base];
    /*TODO*///		voice->frequency = voice->frequency * 256 + namco_soundregs[0x00 + base];
    /*TODO*///
    /*TODO*///		/* the volume seems to vary between one of these five places */
    /*TODO*///		/* it's likely that only 3 or 4 are valid; for now, we just */
    /*TODO*///		/* take the maximum volume and that seems to do the trick */
    /*TODO*///		/* volume[0] = left speaker ?, volume[1] = right speaker ? */
    /*TODO*///		voice->volume[0] = voice->volume[1] = 0;
    /*TODO*///		// front speaker ?
    /*TODO*///		voice->volume[1] |= namco_soundregs[0x02 + base] & 0x0f;
    /*TODO*///		voice->volume[0] |= namco_soundregs[0x02 + base] >> 4;
    /*TODO*///		// rear speaker ?
    /*TODO*///		voice->volume[1] |= namco_soundregs[0x03 + base] & 0x0f;
    /*TODO*///		voice->volume[0] |= namco_soundregs[0x03 + base] >> 4;
    /*TODO*///		voice->volume[1] |= namco_soundregs[0x23 + base] >> 4;
    /*TODO*///		voice->wave = &sound_prom[32 * (namco_soundregs[0x23 + base] & 7)];
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////********************************************************************************/
    /*TODO*///
    /*TODO*///void mappy_sound_enable_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	sound_enable = offset;
    /*TODO*///}
    /*TODO*///
    /*TODO*///void mappy_sound_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	sound_channel *voice;
    /*TODO*///	int base;
    /*TODO*///
    /*TODO*///	/* update the streams */
    /*TODO*///	stream_update(stream, 0);
    /*TODO*///
    /*TODO*///	/* set the register */
    /*TODO*///	namco_soundregs[offset] = data;
    /*TODO*///
    /*TODO*///	/* recompute all the voice parameters */
    /*TODO*///	for (base = 0, voice = channel_list; voice < last_channel; voice++, base += 8)
    /*TODO*///	{
    /*TODO*///		voice->frequency = namco_soundregs[0x06 + base] & 15;	/* high bits are from here */
    /*TODO*///		voice->frequency = voice->frequency * 256 + namco_soundregs[0x05 + base];
    /*TODO*///		voice->frequency = voice->frequency * 256 + namco_soundregs[0x04 + base];
    /*TODO*///
    /*TODO*///		voice->volume[0] = namco_soundregs[0x03 + base] & 0x0f;
    /*TODO*///		voice->wave = &sound_prom[32 * ((namco_soundregs[0x06 + base] >> 4) & 7)];
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////********************************************************************************/
    /*TODO*///
    /*TODO*///void namcos1_sound_w(int offset, int data)
    /*TODO*///{
    /*TODO*///	sound_channel *voice;
    /*TODO*///	int base;
    /*TODO*///	static int nssw;
    /*TODO*///
    /*TODO*///	/* verify the offset */
    /*TODO*///	if (offset > 63)
    /*TODO*///	{
    /*TODO*///		if (errorlog) fprintf(errorlog, "NAMCOS1 sound: Attempting to write past the 64 registers segment\n");
    /*TODO*///		return;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* update the streams */
    /*TODO*///	stream_update(stream,0);
    /*TODO*///
    /*TODO*///	/* set the register */
    /*TODO*///	namco_soundregs[offset] = data;
    /*TODO*///
    /*TODO*///	/* recompute all the voice parameters */
    /*TODO*///	for (base = 0, voice = channel_list; voice < last_channel; voice++, base += 8)
    /*TODO*///	{
    /*TODO*///		voice->frequency = namco_soundregs[0x01 + base] & 15;	/* high bits are from here */
    /*TODO*///		voice->frequency = voice->frequency * 256 + namco_soundregs[0x02 + base];
    /*TODO*///		voice->frequency = voice->frequency * 256 + namco_soundregs[0x03 + base];
    /*TODO*///
    /*TODO*///		voice->volume[0] = namco_soundregs[0x00 + base] & 0x0f;
    /*TODO*///		voice->volume[1] = namco_soundregs[0x04 + base] & 0x0f;
    /*TODO*///		voice->wave = &sound_prom[32/samples_per_byte * ((namco_soundregs[0x01 + base] >> 4) & 15)];
    /*TODO*///
    /*TODO*///		nssw = ((namco_soundregs[0x04 + base] & 0x80) >> 7);
    /*TODO*///		if ((voice + 1) < last_channel) (voice + 1)->noise_sw = nssw;
    /*TODO*///	}
    /*TODO*///	voice = channel_list;
    /*TODO*///	voice->noise_sw = nssw;
    /*TODO*///}
    /*TODO*///
    /*TODO*///int namcos1_sound_r(int offset)
    /*TODO*///{
    /*TODO*///	return namco_soundregs[offset];
    /*TODO*///}
    /*TODO*///
    /*TODO*///void namcos1_wavedata_w(int offset, int data)
    /*TODO*///{
    /*TODO*///	/* update the streams */
    /*TODO*///	stream_update(stream,0);
    /*TODO*///
    /*TODO*///	namco_wavedata[offset] = data;
    /*TODO*///}
    /*TODO*///
    /*TODO*///int namcos1_wavedata_r(int offset)
    /*TODO*///{
    /*TODO*///	return namco_wavedata[offset];
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////********************************************************************************/
    /*TODO*///
    /*TODO*///void snkwave_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	static int freq0 = 0xff;
    /*TODO*///	sound_channel *voice = channel_list;
    /*TODO*///	if( offset==0 ) freq0 = data;
    /*TODO*///	if( offset==1 )
    /*TODO*///	{
    /*TODO*///		stream_update(stream, 0);
    /*TODO*///		if( data==0xff || freq0==0 )
    /*TODO*///		{
    /*TODO*///			voice->volume[0] = 0x0;
    /*TODO*///		}
    /*TODO*///		else
    /*TODO*///		{
    /*TODO*///			voice->volume[0] = 0x8;
    /*TODO*///			voice->frequency = (data<<16)/freq0;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    @Override
    public void update() {
        //no functionality expected
    }

    @Override
    public void reset() {
        //no functionality expected
    }
    
}
