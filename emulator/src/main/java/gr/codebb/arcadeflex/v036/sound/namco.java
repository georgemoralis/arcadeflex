/**
 * *************************************************************************
 *
 * NAMCO sound driver.
 *
 * This driver handles the three known types of NAMCO wavetable sounds:
 *
 * - 3-voice mono (Pac-Man, Pengo, Dig Dug, etc) - 8-voice mono (Mappy, Dig Dug
 * 2, etc) - 8-voice stereo (System 1) - 6-voice stereo (Pole Position 1, Pole
 * Position 2)
 *
 **************************************************************************
 */
package gr.codebb.arcadeflex.v036.sound;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.sound.namcoH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.sound.streams.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;

public class namco extends snd_interface {

    public namco() {
        sound_num = SOUND_NAMCO;
        name = "Namco";
    }

    @Override
    public int chips_num(MachineSound msound) {
        return 0;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return 0;
    }
    /* 8 voices max */
    public static final int MAX_VOICES = 8;

    /* this structure defines the parameters for a channel */
    public static class sound_channel {

        int frequency;
        int counter;
        int[] volume = new int[2];
        int noise_sw;
        int noise_state;
        int noise_seed;
        int noise_counter;
        UBytePtr wave;
    }

    /* globals available to everyone */
    public static UBytePtr namco_soundregs = new UBytePtr();//unsigned char *namco_soundregs;
    public static UBytePtr namco_wavedata = new UBytePtr();//unsigned char *namco_wavedata;

    /* data about the sound system */
    static sound_channel[] channel_list = new sound_channel[MAX_VOICES];
    static int last_channel;

    /* global sound parameters */
    static UBytePtr sound_prom;
    static int samples_per_byte;
    static int num_voices;
    static int sound_enable;
    static int stream;
    static int namco_clock;
    static int sample_rate;

    /*TODO*///
    /*TODO*////* mixer tables and internal buffers */
    /*TODO*///static INT16 *mixer_table;
    static short[] mixer_lookup;
    static ShortPtr mixer_buffer;
    static ShortPtr mixer_buffer_2;

    /* build a table to divide by the number of voices */
    static int mixer_lookup_middle;

    static int make_mixer_table(int voices) {
        int count = voices * 128;
        int i;
        int gain = 16;

        /* allocate memory */
    	//mixer_table = malloc(256 * voices * sizeof(INT16));
        //if (!mixer_table)
        //	return 1;
        /* find the middle of the table */
        //mixer_lookup = mixer_table + (128 * voices);
        mixer_lookup = new short[256 * voices];
        mixer_lookup_middle = voices * 128;
        /* fill in the table - 16 bit case */
        for (i = 0; i < count; i++) {
            short val = (short) (i * gain * 16 / voices);
            if (val > 32767) {
                val = 32767;
            }
            mixer_lookup[mixer_lookup_middle + i] = val;
            mixer_lookup[mixer_lookup_middle - i] = (short) -val;
        }

        return 0;
    }

    /* generate sound to the mix buffer in mono */
    public static StreamInitPtr namco_update_mono = new StreamInitPtr() {
        public void handler(int chip, ShortPtr buffer, int length) {
            ShortPtr mix;
                                
            /* if no sound, we're done */
            if (sound_enable == 0) {
                //memset(buffer, 0, length * sizeof(INT16));
                for (int i = 0; i < length * 2; i++) {
                    buffer.memory[buffer.offset + i] = 0;
                }
                return;
            }
            
            /* zap the contents of the mixer buffer */
            //memset(mixer_buffer, 0, length * sizeof(short));
            for (int i = 0; i < length * 2; i++) {
                mixer_buffer.memory[mixer_buffer.offset + i] = 0;
            }

            /* loop over each voice and add its contribution */
            for (int voice = 0; voice < last_channel; voice++)//; voice < last_channel; voice++)
            {
                int f = channel_list[voice].frequency;
                int v = channel_list[voice].volume[0];
                mix = new ShortPtr(mixer_buffer);

                if (channel_list[voice].noise_sw != 0) {
                    /* only update if we have non-zero volume and frequency */
                    if (v != 0 && (f & 0xff) != 0) {
                        
                        float fbase = (float) sample_rate / (float) namco_clock;
                        int delta = (int) ((float) ((f & 0xff) << 4) * fbase);
                        int c = channel_list[voice].noise_counter;
                        /* add our contribution */
                        for (int i = 0; i < length; i++) {
                            int noise_data;
                            int cnt;

                            if (channel_list[voice].noise_state != 0) {
                                noise_data = 0x07;
                            } else {
                                noise_data = -0x07;
                            }
                            mix.write(0, (short) ( mix.read(0) + noise_data * (v >> 1)));
                            mix.offset += 2;
                            c += delta;
                            cnt = (c >> 12);
                            c &= (1 << 12) - 1;
                            for (; cnt > 0; cnt--) {
                                if (((channel_list[voice].noise_seed + 1) & 2) != 0) {
                                    channel_list[voice].noise_state ^= 1;
                                }
                                if ((channel_list[voice].noise_seed & 1) != 0) {
                                    channel_list[voice].noise_seed ^= 0x28000;
                                }
                                channel_list[voice].noise_seed >>= 1;
                            }
                        }
                        /* update the counter for this voice */
                        channel_list[voice].noise_counter = c;
                    }
                } else {
                    /* only update if we have non-zero volume and frequency */
                    if (v != 0 && f != 0) {
                        int c = channel_list[voice].counter;
                        /* add our contribution */
                        for (int i = 0; i < length; i++) {
                            c += f;
                            int offs = (c >> 15) & 0x1f;
                            //ushort currentmix = mix.read16(0);
                            if (samples_per_byte == 1) /* use only low 4 bits */ {
                                mix.write(0,  (short)( mix.read(0) + ((channel_list[voice].wave.read(offs) & 0x0f) - 8) * v));
                                mix.offset += 2;
                            } else /* use full byte, first the high 4 bits, then the low 4 bits */ {
                                if ((offs & 1) != 0) {
                                    mix.write(0,  (short)( mix.read(0) + ((channel_list[voice].wave.read(offs >> 1) & 0x0f) - 8) * v));
                                    mix.offset += 2;
                                } else {
                                    mix.write(0,  (short)( mix.read(0) + (((channel_list[voice].wave.read(offs >> 1) >> 4) & 0x0f) - 8) * v));
                                    mix.offset += 2;
                                }
                            }
                        }

                        /* update the counter for this voice */
                        channel_list[voice].counter = c;
                    }
                }

            }
            /* mix it down */
            mix = new ShortPtr(mixer_buffer);
            for (int i = 0; i < length; i++) {
                buffer.write(0,  mixer_lookup[mixer_lookup_middle + (short) mix.read(0)]);
                buffer.offset += 2;
                mix.offset += 2;
            }
        }
    };
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
        String mono_name = "NAMCO sound";
        String[] stereo_names = {"NAMCO sound left", "NAMCO sound right"};

        namco_interface intf = (namco_interface) msound.sound_interface;

        namco_clock = intf.samplerate;
        sample_rate = Machine.sample_rate;
        /* get stream channels */
        if (intf.stereo != 0) {
            //throw new UnsupportedOperationException("Namco stereo unsupported ");
                System.out.println("Namco stereo unsupported ");
            	int[] vol=new int[2];
    
    		vol[0] = MIXER(intf.volume,MIXER_PAN_LEFT);
    		vol[1] = MIXER(intf.volume,MIXER_PAN_RIGHT);
/*TODO*///    		stream = stream_init_multi(2, stereo_names, vol, intf.samplerate, 0, namco_update_stereo);
        } else {
            stream = stream_init(mono_name, intf.volume, intf.samplerate, 0, namco_update_mono);
        }
        /* allocate a pair of buffers to mix into - 1 second's worth should be more than enough */
        mixer_buffer = new ShortPtr(2 * intf.samplerate * 2);
        mixer_buffer_2 = new ShortPtr(mixer_buffer, intf.samplerate * 2);

        /* build the mixer table */
        make_mixer_table(intf.voices);
        /* extract globals from the interface */
        num_voices = intf.voices;
        last_channel = num_voices;

        if (intf.region == -1) {
            sound_prom = namco_wavedata;
            samples_per_byte = 2;	/* first 4 high bits, then low 4 bits */

        } else {
            sound_prom = memory_region(intf.region);
            samples_per_byte = 1;	/* use only low 4 bits */

        }

        /* start with sound enabled, many games don't have a sound enable register */
        sound_enable = 1;
        /* reset all the voices */
        for (int i = 0; i < last_channel; i++) //for (voice = channel_list; voice < last_channel; voice++)
        {
            channel_list[i] = new sound_channel();
            channel_list[i].frequency = 0;
            channel_list[i].volume[0] = channel_list[i].volume[1] = 0;
            channel_list[i].wave = sound_prom;
            channel_list[i].counter = 0;
            channel_list[i].noise_sw = 0;
            channel_list[i].noise_state = 0;
            channel_list[i].noise_seed = 1;
            channel_list[i].noise_counter = 0;
        }

        return 0;

    }

    @Override
    public void stop() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    /*TODO*///	free (mixer_table);
    /*TODO*///	free (mixer_buffer);
    }

    /**
     * *****************************************************************************
     */
    public static WriteHandlerPtr pengo_sound_enable_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            sound_enable = data;
        }
    };
    public static WriteHandlerPtr pengo_sound_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int voice;
            int _base;

            /* update the streams */
            stream_update(stream, 0);

            /* set the register */
            namco_soundregs.write(offset, data & 0x0f);

            /* recompute all the voice parameters */
            for (_base = 0, voice = 0; voice < last_channel; voice++, _base += 5) {
                channel_list[voice].frequency = namco_soundregs.read(0x14 + _base);	/* always 0 */

                channel_list[voice].frequency = channel_list[voice].frequency * 16 + namco_soundregs.read(0x13 + _base);
                channel_list[voice].frequency = channel_list[voice].frequency * 16 + namco_soundregs.read(0x12 + _base);
                channel_list[voice].frequency = channel_list[voice].frequency * 16 + namco_soundregs.read(0x11 + _base);
                if (_base == 0) /* the first voice has extra frequency bits */ {
                    channel_list[voice].frequency = channel_list[voice].frequency * 16 + namco_soundregs.read(0x10 + _base);
                } else {
                    channel_list[voice].frequency = channel_list[voice].frequency * 16;
                }

                channel_list[voice].volume[0] = namco_soundregs.read(0x15 + _base) & 0x0f;
                channel_list[voice].wave = new UBytePtr(sound_prom, 32 * (namco_soundregs.read(0x05 + _base) & 7));
            }
        }
    };

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
    public static WriteHandlerPtr mappy_sound_enable_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            sound_enable = offset;
        }
    };
    public static WriteHandlerPtr mappy_sound_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int voice;
            int _base;

            /* update the streams */
            stream_update(stream, 0);

            /* set the register */
            namco_soundregs.write(offset, data);

            /* recompute all the voice parameters */
            for (_base = 0, voice = 0; voice < last_channel; voice++, _base += 8) {
                channel_list[voice].frequency = namco_soundregs.read(0x06 + _base) & 15;	/* high bits are from here */

                channel_list[voice].frequency = channel_list[voice].frequency * 256 + namco_soundregs.read(0x05 + _base);
                channel_list[voice].frequency = channel_list[voice].frequency * 256 + namco_soundregs.read(0x04 + _base);

                channel_list[voice].volume[0] = namco_soundregs.read(0x03 + _base) & 0x0f;
                channel_list[voice].wave = new UBytePtr(sound_prom, 32 * ((namco_soundregs.read(0x06 + _base) >> 4) & 7));
            }
        }
    };
    /**
     * *****************************************************************************
     */
    static int nssw;
    public static WriteHandlerPtr namcos1_sound_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int voice;
            int _base;

            /* verify the offset */
            if (offset > 63) {
                printf("NAMCOS1 sound: Attempting to write past the 64 registers segment\n");
                return;
            }

            /* update the streams */
            stream_update(stream, 0);

            /* set the register */
            namco_soundregs.write(offset, data);

            /* recompute all the voice parameters */
            for (_base = 0, voice = 0; voice < last_channel; voice++, _base += 8) {
                channel_list[voice].frequency = namco_soundregs.read(0x01 + _base) & 15;	/* high bits are from here */

                channel_list[voice].frequency = channel_list[voice].frequency * 256 + namco_soundregs.read(0x02 + _base);
                channel_list[voice].frequency = channel_list[voice].frequency * 256 + namco_soundregs.read(0x03 + _base);

                channel_list[voice].volume[0] = namco_soundregs.read(0x00 + _base) & 0x0f;
                channel_list[voice].volume[1] = namco_soundregs.read(0x04 + _base) & 0x0f;
                channel_list[voice].wave = new UBytePtr(sound_prom, 32 / samples_per_byte * ((namco_soundregs.read(0x01 + _base) >> 4) & 15));

                nssw = ((namco_soundregs.read(0x04 + _base) & 0x80) >> 7);
                if ((voice + 1) < last_channel) {
                    channel_list[voice + 1].noise_sw = nssw;
                }
            }
            //voice = 0;
           channel_list[0].noise_sw = nssw;
        }
    };
    public static ReadHandlerPtr namcos1_sound_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return namco_soundregs.read(offset);
        }
    };
    public static WriteHandlerPtr namcos1_wavedata_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* update the streams */
            stream_update(stream, 0);

            namco_wavedata.write(offset, data);
        }
    };
    public static ReadHandlerPtr namcos1_wavedata_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return namco_wavedata.read(offset);
        }
    };

    /**
     * *****************************************************************************
     */
    public static WriteHandlerPtr snkwave_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            int freq0 = 0xff;
            sound_channel voice = channel_list[0];//sound_channel *voice = channel_list
            if (offset == 0) {
                freq0 = data;
            }
            if (offset == 1) {
                stream_update(stream, 0);
                if (data == 0xff || freq0 == 0) {
                    voice.volume[0] = 0x0;
                } else {
                    voice.volume[0] = 0x8;
                    voice.frequency = (data << 16) / freq0;
                }
            }
        }
    };

    @Override
    public void update() {
        //no functionality expected
    }

    @Override
    public void reset() {
        //no functionality expected
    }

}
