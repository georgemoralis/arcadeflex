package sound;

import mame.sndintrf.snd_interface;


import static platform.libc_v2.ShortPtr;
import static platform.libc_old.*;
import static platform.ptrlib.UBytePtr;
import static cpu.m6502.n2a03.N2A03_DEFAULTCLOCK;
import static cpu.m6502.n2a03.n2a03_irq;
import static mame.common.memory_region;
import static mame.sndintrf.sound_name;
import static mame.sndintrf.sound_scalebufferpos;
import static mame.sndintrfH.MachineSound;
import static mame.sndintrfH.SOUND_NES;
import static mame.mame.Machine;
import static sound.mixer.*;
import static sound.nes_apuH.MAX_NESPSG;
import static sound.nes_apuH.NESinterface;
import static mame.driverH.*;
import static mame.mame.*;

public class nes_apu extends snd_interface {

    public nes_apu() {
        this.sound_num = SOUND_NES;
        this.name = "Nintendo";
    }

    @Override
    public int chips_num(MachineSound msound) {
        return ((NESinterface) msound.sound_interface).num;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return 0;
    }

    /* GLOBAL CONSTANTS */
    public static final int SYNCS_MAX1 = 0x20;
    public static final int SYNCS_MAX2 = 0x80;
    public static final int APU_WRA0 = 0x00;
    public static final int APU_WRA1 = 0x01;
    public static final int APU_WRA2 = 0x02;
    public static final int APU_WRA3 = 0x03;
    public static final int APU_WRB0 = 0x04;
    public static final int APU_WRB1 = 0x05;
    public static final int APU_WRB2 = 0x06;
    public static final int APU_WRB3 = 0x07;
    public static final int APU_WRC0 = 0x08;
    public static final int APU_WRC2 = 0x0A;
    public static final int APU_WRC3 = 0x0B;
    public static final int APU_WRD0 = 0x0C;
    public static final int APU_WRD2 = 0x0E;
    public static final int APU_WRD3 = 0x0F;
    public static final int APU_WRE0 = 0x10;
    public static final int APU_WRE1 = 0x11;
    public static final int APU_WRE2 = 0x12;
    public static final int APU_WRE3 = 0x13;

    public static final int APU_SMASK = 0x15;

    public static final int NOISE_LONG = 0x4000;
    public static final int NOISE_SHORT = 93;

    /* CONSTANTS */

    /* vblank length table used for squares, triangle, noise */
    static int vbl_length[]
            = {
            5, 127, 10, 1, 19, 2, 40, 3, 80, 4, 30, 5, 7, 6, 13, 7,
            6, 8, 12, 9, 24, 10, 48, 11, 96, 12, 36, 13, 8, 14, 16, 15
    };

    /* frequency limit of square channels */
    static int freq_limit[]
            = {
            0x3FF, 0x555, 0x666, 0x71C, 0x787, 0x7C1, 0x7E0, 0x7F0,};

    /* table of noise frequencies */
    static int noise_freq[]
            = {
            4, 8, 16, 32, 64, 96, 128, 160, 202, 254, 380, 508, 762, 1016, 2034, 2046
    };

    /* dpcm transfer freqs */
    static int dpcm_clocks[]
            = {
            428, 380, 340, 320, 286, 254, 226, 214, 190, 160, 142, 128, 106, 85, 72, 54
    };

    /* ratios of pos/neg pulse for square waves */
    /* 2/16 = 12.5%, 4/16 = 25%, 8/16 = 50%, 12/16 = 75% */
    static int duty_lut[]
            = {
            2, 4, 8, 12
    };

    /* Square Wave */
    class square_t {

        public int[] regs = new int[4]; //unsigned 8bit
        public int vbl_length;
        public int freq;
        public float phaseacc;
        public float output_vol;
        public float env_phase;
        public float sweep_phase;
        public int adder; //unsigned 8bit
        public int env_vol; //unsigned 8bit
        public boolean enabled;
    }

    /* Triangle Wave */
    class triangle_t {

        public int[] regs = new int[4]; /* regs[1] unused */  //unsigned 8bit

        public int linear_length;
        public int vbl_length;
        public int write_latency;
        public float phaseacc;
        public float output_vol;
        public int adder; //unsigned 8bit
        public boolean counter_started;
        public boolean enabled;
    }
    /* Noise Wave */

    class noise_t {

        public int[] regs = new int[4]; /* regs[1] unused */ ////unsigned 8bit

        public int cur_pos;
        public int vbl_length;
        public float phaseacc;
        public float output_vol;
        public float env_phase;
        public int env_vol;//unsigned 8bit
        public boolean enabled;
    }
    /* DPCM Wave */

    class dpcm_t {

        public int[] regs = new int[4];//unsigned 8bit
        public int/*uint*/ address;
        public int/*uint*/ length;
        public int bits_left;
        public float phaseacc;
        public float output_vol;
        public int cur_byte;//unsigned 8 bit
        public boolean enabled;
        public boolean irq_occurred;
        public UBytePtr cpu_mem;
        public byte vol;
    }
    /* APU type */

    class apu_t {

        public apu_t() {
            dpcm = new dpcm_t();
            noi = new noise_t();
            tri = new triangle_t();
            squ[0] = new square_t();
            squ[1] = new square_t();
        }

        /* Sound channels */
        public square_t[] squ = new square_t[2];
        public triangle_t tri;
        public noise_t noi;
        public dpcm_t dpcm;

        /* APU registers */
        public int[] regs = new int[22];//unsigned 8 bit

        /* Sound pointers */
        public byte[] buffer; //unsigned 8 bit

        int buf_pos;
    }

    /* GLOBAL VARIABLES */
    static apu_t[] APU = new apu_t[MAX_NESPSG];/* Actual APUs */

    static apu_t cur;/* Pointer to an APU */

    static float apu_incsize;           /* Adjustment increment */

    static char/*uint16*/ samps_per_sync;        /* Number of samples per vsync */

    static char/*uint16*/ buffer_size;           /* Actual buffer size in bytes */

    static char/*uint16*/ real_rate;             /* Actual playback rate */

    static int/*uint16*/ chip_max;              /* Desired number of chips in use */

    static /*uint8*/ int[] noise_lut = new int[NOISE_LONG]; /* Noise sample lookup table */

    static char[]/*uint16*/ vbl_times = new char[0x20];       /* VBL durations in samples */

    static /*uint32*/ int[] sync_times1 = new int[SYNCS_MAX1]; /* Samples per sync table */

    static /*uint32*/ int[] sync_times2 = new int[SYNCS_MAX2]; /* Samples per sync table */

    static int channel;

    /* INTERNAL FUNCTIONS */

    /* INITIALIZE WAVE TIMES RELATIVE TO SAMPLE RATE */
    static void create_vbltimes(char[] table, int[] vbl, int rate) {
        int i;

        for (i = 0; i < 0x20; i++) {
            table[i] = (char) (vbl[i] * rate);
        }
    }
    /* INITIALIZE SAMPLE TIMES IN TERMS OF VSYNCS */

    static void create_syncs(/*unsigned long*/int sps) {
        int i;
        /*unsigned long*/
        int val = sps;

        for (i = 0; i < SYNCS_MAX1; i++) {
            sync_times1[i] = val;
            val += sps;
        }

        val = 0;
        for (i = 0; i < SYNCS_MAX2; i++) {
            sync_times2[i] = val;
            sync_times2[i] >>>= 2;
            val += sps;
        }
    }

    /* INITIALIZE NOISE LOOKUP TABLE */
    static int m = 0x0011;

    static void create_noise(int[] buf, int bits, int size) {

        int xor_val, i;

        for (i = 0; i < size; i++) {
            xor_val = m & 1;
            m = (m >>> 1) & 0xFF;
            xor_val ^= (m & 1);
            m = (m | xor_val << (bits - 1)) & 0xFF;

            buf[i] = m & 0xFF;
        }
    }
    /* OUTPUT SQUARE WAVE SAMPLE (VALUES FROM -16 to +15) */

    static byte apu_square(square_t chan) {
        int env_delay;
        int sweep_delay;
        byte output;

        /* reg0: 0-3=volume, 4=envelope, 5=hold, 6-7=duty cycle
         ** reg1: 0-2=sweep shifts, 3=sweep inc/dec, 4-6=sweep length, 7=sweep on
         ** reg2: 8 bits of freq
         ** reg3: 0-2=high freq, 7-4=vbl length counter
         */
        if (false == chan.enabled) {
            return 0;
        }

        /* enveloping */
        env_delay = sync_times1[chan.regs[0] & 0x0F];
        /* decay is at a rate of (env_regs + 1) / 240 secs */
        chan.env_phase -= 4;
        while (chan.env_phase < 0) {
            chan.env_phase += env_delay;
            if ((chan.regs[0] & 0x20) != 0) {
                chan.env_vol = ((chan.env_vol + 1) & 15) & 0xFF;
            } else if (chan.env_vol < 15) {
                chan.env_vol = (chan.env_vol + 1) & 0xFF;
            }
        }

        /* vbl length counter */
        if (chan.vbl_length > 0 && 0 == (chan.regs[0] & 0x20)) {
            chan.vbl_length--;
        }

        if (0 == chan.vbl_length) {
            return 0;
        }

        /* freqsweeps */
        if ((chan.regs[1] & 0x80) != 0 && (chan.regs[1] & 7) != 0) {
            sweep_delay = sync_times1[(chan.regs[1] >> 4) & 7];
            chan.sweep_phase -= 2;
            while (chan.sweep_phase < 0) {
                chan.sweep_phase += sweep_delay;
                if ((chan.regs[1] & 8) != 0) {
                    chan.freq -= chan.freq >> (chan.regs[1] & 7);
                } else {
                    chan.freq += chan.freq >> (chan.regs[1] & 7);
                }
            }
        }

        if ((0 == (chan.regs[1] & 8) && (chan.freq >> 16) > freq_limit[chan.regs[1] & 7])
                || (chan.freq >> 16) < 4) {
            return 0;
        }
        chan.phaseacc -= (float) apu_incsize; /* # of cycles per sample */

        while (chan.phaseacc < 0) {
            chan.phaseacc += (chan.freq >> 16);
            chan.adder = (chan.adder + 1) & 0x0F;
        }

        if ((chan.regs[0] & 0x10) != 0) /* fixed volume */ {
            output = (byte) (chan.regs[0] & 0x0F);
        } else {
            output = (byte) (0x0F - chan.env_vol);
        }

        if (chan.adder < (duty_lut[chan.regs[0] >> 6])) {
            output = (byte) -output;
        }

        return (byte) output;
    }
    /* OUTPUT TRIANGLE WAVE SAMPLE (VALUES FROM -16 to +15) */

    static byte apu_triangle(triangle_t chan) {
        int freq;
        byte output;
        /* reg0: 7=holdnote, 6-0=linear length counter
         ** reg2: low 8 bits of frequency
         ** reg3: 7-3=length counter, 2-0=high 3 bits of frequency
         */

        if (false == chan.enabled) {
            return 0;
        }

        if (false == chan.counter_started && 0 == (chan.regs[0] & 0x80)) {
            if (chan.write_latency != 0) {
                chan.write_latency--;
            }
            if (0 == chan.write_latency) {
                chan.counter_started = true;
            }
        }

        if (chan.counter_started) {
            if (chan.linear_length > 0) {
                chan.linear_length--;
            }
            if (chan.vbl_length != 0 && 0 == (chan.regs[0] & 0x80)) {
                chan.vbl_length--;
            }

            if (0 == chan.vbl_length) {
                return 0;
            }
        }

        if (0 == chan.linear_length) {
            return 0;
        }

        freq = (((chan.regs[3] & 7) << 8) + chan.regs[2]) + 1;

        if (freq < 4) /* inaudible */ {
            return 0;
        }

        chan.phaseacc -= (float) apu_incsize; /* # of cycles per sample */

        while (chan.phaseacc < 0) {
            chan.phaseacc += freq;
            chan.adder = (chan.adder + 1) & 0x1F;

            output = (byte) ((chan.adder & 7) << 1);
            if ((chan.adder & 8) != 0) {
                output = (byte) (0x10 - output);
            }
            if ((chan.adder & 0x10) != 0) {
                output = (byte) -output;
            }

            chan.output_vol = (byte) output;
        }

        return (byte) chan.output_vol;
    }
    /* OUTPUT NOISE WAVE SAMPLE (VALUES FROM -16 to +15) */

    static byte apu_noise(noise_t chan) {
        int freq, env_delay;
        /*uint8*/
        int outvol;
        /*uint8*/
        int output;

        /* reg0: 0-3=volume, 4=envelope, 5=hold
         ** reg2: 7=small(93 byte) sample,3-0=freq lookup
         ** reg3: 7-4=vbl length counter
         */
        if (false == chan.enabled) {
            return 0;
        }

        /* enveloping */
        env_delay = sync_times1[chan.regs[0] & 0x0F];

        /* decay is at a rate of (env_regs + 1) / 240 secs */
        chan.env_phase -= 4;
        while (chan.env_phase < 0) {
            chan.env_phase += env_delay;
            if ((chan.regs[0] & 0x20) != 0) {
                chan.env_vol = ((chan.env_vol + 1) & 15) & 0xFF;
            } else if (chan.env_vol < 15) {
                chan.env_vol++;
            }
        }

        /* length counter */
        if (0 == (chan.regs[0] & 0x20)) {
            if (chan.vbl_length > 0) {
                chan.vbl_length--;
            }
        }

        if (0 == chan.vbl_length) {
            return 0;
        }

        freq = noise_freq[chan.regs[2] & 0x0F];
        chan.phaseacc -= (float) apu_incsize; /* # of cycles per sample */

        while (chan.phaseacc < 0) {
            chan.phaseacc += freq;

            chan.cur_pos++;
            if (NOISE_SHORT == chan.cur_pos && (chan.regs[2] & 0x80) != 0) {
                chan.cur_pos = 0;
            } else if (NOISE_LONG == chan.cur_pos) {
                chan.cur_pos = 0;
            }
        }

        if ((chan.regs[0] & 0x10) != 0)/* fixed volume */ {
            outvol = chan.regs[0] & 0x0F;
        } else {
            outvol = (0x0F - chan.env_vol) & 0xFF;
        }

        output = noise_lut[chan.cur_pos];
        if (output > outvol) {
            output = outvol;
        }

        if ((noise_lut[chan.cur_pos] & 0x80) != 0) /* make it negative */ {
            output = -output;
        }

        return (byte) output;
    }
    /* RESET DPCM PARAMETERS */

    static void apu_dpcmreset(dpcm_t chan) {
        chan.address = 0xC000 + (char) (chan.regs[2] << 6);
        chan.length = (char) (chan.regs[3] << 4) + 1;
        chan.bits_left = chan.length << 3;
        chan.irq_occurred = false;
    }
    /* OUTPUT DPCM WAVE SAMPLE (VALUES FROM -64 to +63) */
    /* TODO: centerline naughtiness */

    static byte apu_dpcm(dpcm_t chan) {
        int freq, bit_pos;

        /* reg0: 7=irq gen, 6=looping, 3-0=pointer to clock table
         ** reg1: output dc level, 7 bits unsigned
         ** reg2: 8 bits of 64-byte aligned address offset : $C000 + (value * 64)
         ** reg3: length, (value * 16) + 1
         */
        if (chan.enabled) {
            freq = dpcm_clocks[chan.regs[0] & 0x0F];
            chan.phaseacc -= (float) apu_incsize; /* # of cycles per sample */

            while (chan.phaseacc < 0) {
                chan.phaseacc += freq;

                if (0 == chan.length) {
                    if ((chan.regs[0] & 0x40) != 0) {
                        apu_dpcmreset(chan);
                    } else {
                        if ((chan.regs[0] & 0x80) != 0) /* IRQ Generator */ {
                            chan.irq_occurred = true;
                            n2a03_irq();
                        }
                        break;
                    }
                }

                chan.bits_left--;
                bit_pos = 7 - (chan.bits_left & 7);
                if (7 == bit_pos) {
                    chan.cur_byte = chan.cpu_mem.read(chan.address);
                    chan.address++;
                    chan.length--;
                }

                if ((chan.cur_byte & (1 << bit_pos)) != 0) //            chan.regs[1]++;
                {
                    chan.vol++;
                } else //            chan.regs[1]--;
                {
                    chan.vol--;
                }
            }
        }

        if (chan.vol > 63) {
            chan.vol = 63;
        } else if (chan.vol < -64) {
            chan.vol = -64;
        }

        return (byte) (chan.vol >> 1);
    }

    @Override
    public int start(MachineSound msound) {
        NESinterface intf = (NESinterface) msound.sound_interface;
        int i;

        /* Initialize global variables */
        samps_per_sync = (char) (Machine.sample_rate / Machine.drv.frames_per_second);
        buffer_size = samps_per_sync;
        real_rate = (char) (samps_per_sync * Machine.drv.frames_per_second);
        chip_max = intf.num;
        apu_incsize = (float) (N2A03_DEFAULTCLOCK / (float) real_rate);

        /* Use initializer calls */
        create_noise(noise_lut, 13, NOISE_LONG);
        create_vbltimes(vbl_times, vbl_length, samps_per_sync);
        create_syncs(samps_per_sync);

        /* Adjust buffer size if 16 bits */
        buffer_size += samps_per_sync;

        /* Initialize individual chips */
        for (i = 0; i < chip_max; i++) {
            APU[i] = new apu_t();

            //memset(cur,0,sizeof(apu_t));

            /* Check for buffer allocation failure and bail out if necessary */
            /*if ((cur.buffer = malloc(buffer_size))==NULL)
             {
             while (--i >= 0) free(APU[i].buffer);
             return 1;
             }*/
            APU[i].buffer = new byte[buffer_size];
            (APU[i].dpcm).cpu_mem = memory_region(intf.region[i]);
        }

        channel = mixer_allocate_channels(chip_max, intf.volume);
        for (i = 0; i < chip_max; i++) {
            //char name[40];

            name = sprintf("%s #%d", sound_name(msound), i);
            mixer_set_name(channel, name);
        }

        return 0;
    }

    @Override
    public void stop() {
        int i;

        for (i = 0; i < chip_max; i++) {
            APU[i].buffer = null;
        }
    }
    /* WRITE REGISTER VALUE */

    static void apu_regwrite(int chip, int address, /*uint8*/ int value) {
        int chan = (address & 4) != 0 ? 1 : 0;

        cur = APU[chip];
        switch (address) {
            /* squares */
            case APU_WRA0:
            case APU_WRB0:
                cur.squ[chan].regs[0] = value;
                break;

            case APU_WRA1:
            case APU_WRB1:
                cur.squ[chan].regs[1] = value;
                break;

            case APU_WRA2:
            case APU_WRB2:
                cur.squ[chan].regs[2] = value;
                if (cur.squ[chan].enabled) {
                    cur.squ[chan].freq = ((((cur.squ[chan].regs[3] & 7) << 8) + value) + 1) << 16;
                }
                break;

            case APU_WRA3:
            case APU_WRB3:
                cur.squ[chan].regs[3] = value;

                if (cur.squ[chan].enabled) {
                    cur.squ[chan].vbl_length = vbl_times[value >> 3];
                    cur.squ[chan].env_vol = 0;
                    cur.squ[chan].freq = ((((value & 7) << 8) + cur.squ[chan].regs[2]) + 1) << 16;
                }

                break;

            /* triangle */
            case APU_WRC0:
                cur.tri.regs[0] = value;

                if (cur.tri.enabled) {                                          /* ??? */

                    if (false == cur.tri.counter_started) {
                        cur.tri.linear_length = sync_times2[value & 0x7F];
                    }
                }

                break;

            case 0x4009:
                /* unused */
                cur.tri.regs[1] = value;
                break;

            case APU_WRC2:
                cur.tri.regs[2] = value;
                break;

            case APU_WRC3:
                cur.tri.regs[3] = value;

                /* this is somewhat of a hack.  there is some latency on the Real
                 ** Thing between when trireg0 is written to and when the linear
                 ** length counter actually begins its countdown.  we want to prevent
                 ** the case where the program writes to the freq regs first, then
                 ** to reg 0, and the counter accidentally starts running because of
                 ** the sound queue's timestamp processing.
                 **
                 ** set to a few NES sample -- should be sufficient
                 **
                 **     3 * (1789772.727 / 44100) = ~122 cycles, just around one scanline
                 **
                 ** should be plenty of time for the 6502 code to do a couple of table
                 ** dereferences and load up the other triregs
                 */
                cur.tri.write_latency = 3;

                if (cur.tri.enabled) {
                    cur.tri.counter_started = false;
                    cur.tri.vbl_length = vbl_times[value >> 3];
                    cur.tri.linear_length = sync_times2[cur.tri.regs[0] & 0x7F];
                }

                break;

            /* noise */
            case APU_WRD0:
                cur.noi.regs[0] = value;
                break;

            case 0x400D:
                /* unused */
                cur.noi.regs[1] = value;
                break;

            case APU_WRD2:
                cur.noi.regs[2] = value;
                break;

            case APU_WRD3:
                cur.noi.regs[3] = value;

                if (cur.noi.enabled) {
                    cur.noi.vbl_length = vbl_times[value >> 3];
                    cur.noi.env_vol = 0; /* reset envelope */

                }
                break;

            /* DMC */
            case APU_WRE0:
                cur.dpcm.regs[0] = value;
                if (0 == (value & 0x80)) {
                    cur.dpcm.irq_occurred = false;
                }
                break;

            case APU_WRE1: /* 7-bit DAC */

                //cur.dpcm.regs[1] = value - 0x40;

                cur.dpcm.regs[1] = value & 0x7F;
                cur.dpcm.vol = (byte) (cur.dpcm.regs[1] - 64);
                break;

            case APU_WRE2:
                cur.dpcm.regs[2] = value;
                //apu_dpcmreset(cur.dpcm);
                break;

            case APU_WRE3:
                cur.dpcm.regs[3] = value;
                //apu_dpcmreset(cur.dpcm);
                break;

            case APU_SMASK:
                if ((value & 0x01) != 0) {
                    cur.squ[0].enabled = true;
                } else {
                    cur.squ[0].enabled = false;
                    cur.squ[0].vbl_length = 0;
                }

                if ((value & 0x02) != 0) {
                    cur.squ[1].enabled = true;
                } else {
                    cur.squ[1].enabled = false;
                    cur.squ[1].vbl_length = 0;
                }

                if ((value & 0x04) != 0) {
                    cur.tri.enabled = true;
                } else {
                    cur.tri.enabled = false;
                    cur.tri.vbl_length = 0;
                    cur.tri.linear_length = 0;
                    cur.tri.counter_started = false;
                    cur.tri.write_latency = 0;
                }

                if ((value & 0x08) != 0) {
                    cur.noi.enabled = true;
                } else {
                    cur.noi.enabled = false;
                    cur.noi.vbl_length = 0;
                }

                if ((value & 0x10) != 0) {
                    /* only reset dpcm values if DMA is finished */
                    if (false == cur.dpcm.enabled) {
                        cur.dpcm.enabled = true;
                        apu_dpcmreset(cur.dpcm);
                    }
                } else {
                    cur.dpcm.enabled = false;
                }

                cur.dpcm.irq_occurred = false;

                break;
            default:
                if(errorlog!=null) fprintf(errorlog,"invalid apu write: $%02X at $%04X\n", value, address);
                break;
        }
    }
    /* READ VALUES FROM REGISTERS */

    public static int apu_read(int chip, int address) {
        return APU[chip].regs[address] & 0xFF;
    }

    /* WRITE VALUE TO TEMP REGISTRY AND QUEUE EVENT */
    public static void apu_write(int chip, int address, int value) {
        APU[chip].regs[address] = value & 0xFF;
        apu_update(chip);
        apu_regwrite(chip, address, value & 0xFF);
    }

    /* UPDATE SOUND BUFFER USING CURRENT DATA */
    static /*INT16 *buffer16*/ ShortPtr buffer16 = null;

    public static void apu_update(int chip) {

        int accum;
        int endp = sound_scalebufferpos(samps_per_sync);
        int elapsed;

        cur = APU[chip];
        buffer16 = new ShortPtr(cur.buffer);

        /* Recall last position updated and restore pointers */
        elapsed = cur.buf_pos;
        buffer16.offset += (elapsed * 2);

        while (elapsed < endp) {
            elapsed++;

            accum = apu_square(cur.squ[0]);
            accum += apu_square(cur.squ[1]);
            accum += apu_triangle(cur.tri);
            accum += apu_noise(cur.noi);
            accum += apu_dpcm(cur.dpcm);

            /* 8-bit clamps */
            if (accum > 127) {
                accum = 127;
            } else if (accum < -128) {
                accum = -128;
            }

            //* (buffer16++) = accum << 8;
            buffer16.write(0, (short) (accum << 8));
            buffer16.offset += 2;
        }
        cur.buf_pos = endp;
    }

    @Override
    public void update() {
        int i;

        if (real_rate == 0) {
            return;
        }

        for (i = 0; i < chip_max; i++) {
            apu_update(i);
            APU[i].buf_pos = 0;
            mixer_play_streamed_sample_16(channel + i, new ShortPtr(APU[i].buffer), buffer_size, real_rate);
        }
    }

    @Override
    public void reset() {

    }

    public static ReadHandlerPtr NESPSG_0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return apu_read(0, offset);
        }
    };
    public static ReadHandlerPtr NESPSG_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return apu_read(1, offset);
        }
    };
    public static WriteHandlerPtr NESPSG_0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            apu_write(0, offset, data & 0xFF);
        }
    };
    public static WriteHandlerPtr NESPSG_1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            apu_write(1, offset, data & 0xFF);
        }
    };

}
