package gr.codebb.arcadeflex.v036.sound;

import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.sound.k051649H.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.sound.streams.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.platform.libc_v2.*;
public class k051649 extends snd_interface {
    public static class k051649_sound_channel {
    	/*unsigned*/ long counter;
	int frequency;
	int volume;
	int key;
	/*unsigned char waveform[32];*/
	byte[] waveform=new byte[32];		/* 19991207.CAB */
}
    public static final int FREQBASEBITS = 16;
    static k051649_sound_channel[] channel_list = new k051649_sound_channel[5];

    /* global sound parameters */
    static int stream, mclock, rate;

    /* mixer tables and internal buffers */
    //static UShortPtr mixer_table;

    static ShortPtr mixer_buffer;

    public k051649() {
        this.name = "051649";
        this.sound_num = SOUND_K051649;
    }

    @Override
    public int chips_num(MachineSound msound) {
        return 0;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return ((k051649_interface) msound.sound_interface).master_clock;
    }

    @Override
    public int start(MachineSound msound) {
        String snd_name = "K051649";
        //k051649_sound_channel *voice=channel_list;
        k051649_interface intf = (k051649_interface) msound.sound_interface;
        int i;

        /* get stream channels */
        stream = stream_init(snd_name, intf.volume, Machine.sample_rate, 0, K051649_update);
        mclock = intf.master_clock;
        rate = Machine.sample_rate;

        /* allocate a buffer to mix into - 1 second's worth should be more than enough */
        mixer_buffer = new ShortPtr(2 * 2 * Machine.sample_rate);//if ((mixer_buffer = malloc(2 * sizeof(short) * Machine->sample_rate)) == 0)
        //return 1;

        /* build the mixer table */
        if (make_mixer_table(5) != 0) {
            mixer_buffer = null;
            return 1;
        }

        /* reset all the voices */
        for (i = 0; i < 5; i++) {
            channel_list[i]=new k051649_sound_channel();
            channel_list[i].frequency = 0;
            channel_list[i].volume = 0;
            channel_list[i].counter = 0;
        }

        return 0;
    }

    @Override
    public void stop() {
        //mixer_table = null;
        mixer_buffer = null;
    }

    @Override
    public void update() {
        //no functionality expected
    }

    @Override
    public void reset() {
        //no functionality expected
    }

    /* build a table to divide by the number of voices */
    static short[] mixer_lookup;
    static int mixer_lookup_middle;
    static int make_mixer_table(int voices) {
        int count = voices * 256;
        int i;
        int gain = 8;

        
        mixer_lookup = new short[512 * voices];
        mixer_lookup_middle = voices * 256;
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
    


    /* generate sound to the mix buffer */
    public static StreamInitPtr K051649_update = new StreamInitPtr() {
        public void handler(int chip, ShortPtr buffer, int length) {

	//k051649_sound_channel * voice = channel_list;
        ShortPtr mix;
        int i, v, f, j, k;

        /* zap the contents of the mixer buffer */
        //memset(mixer_buffer, 0, length * sizeof(short));
        for(i=0; i<length*2; i++)
        {
            mixer_buffer.write(i, (short)0);
        }

        for (j = 0; j < 5; j++) {
            v = channel_list[j].volume;
            f = channel_list[j].frequency;
            k = channel_list[j].key;
            if (v!=0 && f!=0 && k!=0) {
                /*const unsigned char *w = voice[j].waveform;*/
               // const signed char *w = voice[j].waveform;			/* 19991207.CAB */

                int c = (int)channel_list[j].counter;

                mix = new ShortPtr(mixer_buffer);

                /* add our contribution */
                for (i = 0; i < length; i++) {
                    int offs;

                    /* Amuse source:  Cab suggests this method gives greater resolution */
                    c += (long) ((((float) mclock / (float) (f * 16)) * (float) (1 << FREQBASEBITS)) / (float) (rate / 32));
                    offs = (c >> 16) & 0x1f;
                    mix.write(0, (short) ( mix.read(0) + (channel_list[j].waveform[offs] * v) >> 3));
                            mix.offset += 2;
                }

                /* update the counter for this voice */
                channel_list[j].counter = c;
            }
        }

        /* mix it down */
            mix = new ShortPtr(mixer_buffer);
            for (i = 0; i < length; i++) {
                buffer.write(0,  mixer_lookup[mixer_lookup_middle +  mix.read(0)]);
                buffer.offset += 2;
                mix.offset += 2;
            }
    }};

    /**
     * *****************************************************************************
     */
    public static WriteHandlerPtr K051649_waveform_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            stream_update(stream, 0);
            channel_list[offset >> 5].waveform[offset & 0x1f] = (byte) data;
        }
    };

    void K051649_waveform_w(int offset, int data) {

    }
    public static WriteHandlerPtr K051649_volume_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            stream_update(stream, 0);
            channel_list[offset & 0x7].volume = data & 0xf;
        }
    };
    static int[] f = new int[10];
    public static WriteHandlerPtr K051649_frequency_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            f[offset] = data;

            stream_update(stream, 0);
            channel_list[offset >> 1].frequency = (f[offset & 0xe] + (f[offset | 1] << 8)) & 0x3ff;

            /* Channel 5 appears to share waveforms with channel 4 */
            if ((offset >> 1) == 3) {
                channel_list[4].frequency = (f[6] + (f[7] << 8)) & 0x3ff;
            }
        }
    };

    public static WriteHandlerPtr K051649_keyonoff_w = new WriteHandlerPtr() {
        public void handler(int offs, int data) {
            channel_list[0].key = data & 1;
            channel_list[1].key = data & 2;
            channel_list[2].key = data & 4;
            channel_list[3].key = data & 8;
            channel_list[4].key = data & 16;
        }
    };

}
