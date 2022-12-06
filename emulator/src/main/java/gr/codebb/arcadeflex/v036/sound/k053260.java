package gr.codebb.arcadeflex.v036.sound;

import gr.codebb.arcadeflex.v036.mame.sndintrf;
import gr.codebb.arcadeflex.v036.mame.sndintrfH;
import static gr.codebb.arcadeflex.v036.sound.k053260H.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.timerH.*;
import static arcadeflex.v036.mame.timer.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.sound.streams.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
public class k053260 extends sndintrf.snd_interface {

    public k053260() {
        this.name = "053260";
        this.sound_num = SOUND_K053260;
    }

    @Override
    public int chips_num(sndintrfH.MachineSound msound) {
        return 0;
    }

    @Override
    public int chips_clock(sndintrfH.MachineSound msound) {
        return ((K053260_interface) msound.sound_interface).clock;
    }
    public static final int BASE_SHIFT = 16;

    public static class K053260_channel_def {

        long rate;
        long size;
        long start;
        long bank;
        long volume;
        int play;
        long pan;
        long pos;
        int loop;
        int ppcm; /* packed PCM ( 4 bit signed ) */

        int ppcm_data;
    }
    static K053260_channel_def[] K053260_channel = new K053260_channel_def[4];

    public static class K053260_chip {

        static K053260_interface intf;
        static int channel;
        static int mode;
        static int[] regs = new int[0x30];
        static UBytePtr rom;
        static int rom_size;
        static Object timer; /* SH1 int timer */

    }

    static long[] delta_table;

    static void InitDeltaTable() {
        int i;
        double base = (double) Machine.sample_rate;
        double max = (double) K053260_chip.intf.clock; /* hz */

        long val;

        for (i = 0; i < 0x1000; i++) {
            double v = (double) (0x1000 - i);
            double target = max / v;
            double fixed = (double) (1 << BASE_SHIFT);

            if (target != 0 && base != 0) {
                target = fixed / (base / target);
                val = (long) target;
                if (val == 0) {
                    val = 1;
                }
            } else {
                val = 1;
            }

            delta_table[i] = val;
        }
    }

    static void K053260_reset() {
        int i;

        for (i = 0; i < 4; i++) {
            K053260_channel[i] = new K053260_channel_def();
            K053260_channel[i].rate = 0;
            K053260_channel[i].size = 0;
            K053260_channel[i].start = 0;
            K053260_channel[i].bank = 0;
            K053260_channel[i].volume = 0;
            K053260_channel[i].play = 0;
            K053260_channel[i].pan = 0;
            K053260_channel[i].pos = 0;
            K053260_channel[i].loop = 0;
            K053260_channel[i].ppcm = 0;
            K053260_channel[i].ppcm_data = 0;
        }
    }

    public static int limit(int val, int max, int min) {
        if (val > max) {
            val = max;
        } else if (val < min) {
            val = min;
        }
        return val;
    }
    public static final int MAXOUT = 0x7fff;
    public static final int MINOUT = -0x8000;
    public static StreamInitMultiPtr K053260_update = new StreamInitMultiPtr() {
        public void handler(int chip, ShortPtr[] buffer, int length) {
            int i, j;
            int[] lvol = new int[4];
            int[] rvol = new int[4];
            int[] play = new int[4];
            int[] loop = new int[4];
            int[] ppcm_data = new int[4];
            int[] ppcm = new int[4];
            UBytePtr[] rom = new UBytePtr[4];
            long[] delta = new long[4];
            long[] end = new long[4];
            long[] pos = new long[4];
            int dataL, dataR;
            byte d;


            /* precache some values */
            for (i = 0; i < 4; i++) {
                rom[i] = new UBytePtr(K053260_chip.rom, (int) ((K053260_channel[i].start + (K053260_channel[i].bank << 16))));
                delta[i] = (long) delta_table[(int) K053260_channel[i].rate];
                lvol[i] = (int) (K053260_channel[i].volume * K053260_channel[i].pan);
                rvol[i] = (int) (K053260_channel[i].volume * (8 - K053260_channel[i].pan));
                end[i] = K053260_channel[i].size;
                pos[i] = K053260_channel[i].pos;
                play[i] = K053260_channel[i].play;
                loop[i] = K053260_channel[i].loop;
                ppcm[i] = K053260_channel[i].ppcm;
                ppcm_data[i] = K053260_channel[i].ppcm_data;
                if (ppcm[i] != 0) {
                    delta[i] /= 2;
                }
            }

            for (j = 0; j < length; j++) {

                dataL = dataR = 0;

                for (i = 0; i < 4; i++) {
                    /* see if the voice is on */
                    if (play[i] != 0) {
                        /* see if we're done */
                        if ((pos[i] >> BASE_SHIFT) >= end[i]) {

                            ppcm_data[i] = 0;

                            if (loop[i] != 0) {
                                pos[i] = 0;
                            } else {
                                play[i] = 0;
                                continue;
                            }
                        }

                        if (ppcm[i] != 0) { /* Packed PCM */
                            /* we only update the signal if we're starting or a real sound sample has gone by */
                            /* this is all due to the dynamic sample rate convertion */

                            if (pos[i] == 0 || ((pos[i] ^ (pos[i] - delta[i])) & 0x8000) == 0x8000) {
                                if ((pos[i] & 0x8000) != 0) {
                                    ppcm_data[i] = rom[i].read((int) (pos[i] >> BASE_SHIFT)) & 0x0f;
                                } else {
                                    ppcm_data[i] = ((rom[i].read((int) (pos[i] >> BASE_SHIFT))) >> 4) & 0x0f;
                                }

                                ppcm_data[i] *= 0x11;
                            }

                            d = (byte) ppcm_data[i];

                            d /= 2;

                            pos[i] += delta[i];
                        } else { /* PCM */

                            d = (byte) rom[i].read((int) (pos[i] >> BASE_SHIFT));
                            pos[i] += delta[i];
                        }

                        if ((K053260_chip.mode & 2) != 0) {
                            dataL += (d * lvol[i]) >> 2;
                            dataR += (d * rvol[i]) >> 2;
                        }
                    }
                }
                //buffer[1][j] = limit(dataL, MAXOUT, MINOUT);
                //buffer[0][j] = limit(dataR, MAXOUT, MINOUT);
                if (dataL > MAXOUT) {
                    dataL = MAXOUT;
                } else if (dataL < MINOUT) {
                    dataL = MINOUT;
                }
                if (dataR > MAXOUT) {
                    dataR = MAXOUT;
                } else if (dataR < MINOUT) {
                    dataR = MINOUT;
                }

                buffer[1].write(j, (short) dataL);
                buffer[0].write(j, (short) dataR);

            }

            /* update the regs now */
            for (i = 0; i < 4; i++) {
                K053260_channel[i].pos = pos[i];
                K053260_channel[i].play = play[i];
                K053260_channel[i].ppcm_data = ppcm_data[i];
            }
        }
    };

    @Override
    public int start(sndintrfH.MachineSound msound) {
        String[] names = new String[2];

        int i;

        /* Initialize our chip structure */
        K053260_chip.intf = (K053260_interface) msound.sound_interface;
        K053260_chip.mode = 0;
        K053260_chip.rom = memory_region(K053260_chip.intf.region);
        K053260_chip.rom_size = memory_region_length(K053260_chip.intf.region) - 1;

        K053260_reset();

        for (i = 0; i < 0x30; i++) {
            K053260_chip.regs[i] = 0;
        }

        delta_table = new long[0x1000 * 4];//( unsigned long * )malloc( 0x1000 * sizeof( unsigned long ) );

        if (delta_table == null) {
            return -1;
        }

        for (i = 0; i < 2; i++) {
            names[i] = sprintf("%s Ch %d", sound_name(msound), i);
        }

        K053260_chip.channel = stream_init_multi(2, names,
                K053260_chip.intf.mixing_level, Machine.sample_rate,
                0, K053260_update);

        InitDeltaTable();

        /* setup SH1 timer if necessary */
        if (K053260_chip.intf.irq != null) {
            K053260_chip.timer = timer_pulse(TIME_IN_HZ((K053260_chip.intf.clock / 32)), 0, K053260_chip.intf.irq);
        } else {
            K053260_chip.timer = null;
        }

        return 0;
    }

    @Override
    public void stop() {
        if (delta_table != null) {
            delta_table = null;
        }

        if (K053260_chip.timer != null) {
            timer_remove(K053260_chip.timer);
        }

        K053260_chip.timer = null;
    }

    @Override
    public void update() {
        //no functionality expected
    }

    @Override
    public void reset() {
        //no functionality expected
    }

    public static void check_bounds(int channel) {
        int channel_start = (int) ((K053260_channel[channel].bank << 16) + K053260_channel[channel].start);
        int channel_end = (int) (channel_start + K053260_channel[channel].size - 1);

        if (channel_start > K053260_chip.rom_size) {
            if (errorlog != null) {
                fprintf(errorlog, "K53260: Attempting to start playing past the end of the rom ( start = %06x, end = %06x ).\n", channel_start, channel_end);
            }

            K053260_channel[channel].play = 0;

            return;
        }

        if (channel_end > K053260_chip.rom_size) {
            if (errorlog != null) {
                fprintf(errorlog, "K53260: Attempting to play past the end of the rom ( start = %06x, end = %06x ).\n", channel_start, channel_end);
            }

            K053260_channel[channel].size = K053260_chip.rom_size - channel_start;
        }
//#if LOG
        if (errorlog != null) {
            fprintf(errorlog, "K053260: Sample Start = %06x, Sample End = %06x, Sample rate = %04lx, PPCM = %s\n", channel_start, channel_end, K053260_channel[channel].rate, K053260_channel[channel].ppcm != 0 ? "yes" : "no");
        }
//#endif
    }
    public static WriteHandlerPtr K053260_WriteReg = new WriteHandlerPtr() {
        public void handler(int r, int v) {
            int i, t;

            if (r > 0x2f) {
                if (errorlog != null) {
                    fprintf(errorlog, "K053260: Writing past registers\n");
                }
                return;
            }

            if (Machine.sample_rate != 0) {
                stream_update(K053260_chip.channel, 0);
            }

            /* before we update the regs, we need to check for a latched reg */
            if (r == 0x28) {
                t = K053260_chip.regs[r] ^ v;

                for (i = 0; i < 4; i++) {
                    if ((t & (1 << i)) != 0) {
                        if ((v & (1 << i)) != 0) {
                            K053260_channel[i].play = 1;
                            K053260_channel[i].pos = 0;
                            K053260_channel[i].ppcm_data = 0;
                            check_bounds(i);
                        } else {
                            K053260_channel[i].play = 0;
                        }
                    }
                }

                K053260_chip.regs[r] = v;
                return;
            }

            /* update regs */
            K053260_chip.regs[r] = v;

            /* communication registers */
            if (r < 8) {
                return;
            }

            /* channel setup */
            if (r < 0x28) {
                int channel = (r - 8) / 8;

                switch ((r - 8) & 0x07) {
                    case 0: /* sample rate low */

                        K053260_channel[channel].rate &= 0x0f00;
                        K053260_channel[channel].rate |= v;
                        break;

                    case 1: /* sample rate high */

                        K053260_channel[channel].rate &= 0x00ff;
                        K053260_channel[channel].rate |= (v & 0x0f) << 8;
                        break;

                    case 2: /* size low */

                        K053260_channel[channel].size &= 0xff00;
                        K053260_channel[channel].size |= v;
                        break;

                    case 3: /* size high */

                        K053260_channel[channel].size &= 0x00ff;
                        K053260_channel[channel].size |= v << 8;
                        break;

                    case 4: /* start low */

                        K053260_channel[channel].start &= 0xff00;
                        K053260_channel[channel].start |= v;
                        break;

                    case 5: /* start high */

                        K053260_channel[channel].start &= 0x00ff;
                        K053260_channel[channel].start |= v << 8;
                        break;

                    case 6: /* bank */

                        K053260_channel[channel].bank = v & 0xff;
                        break;

                    case 7: /* volume is 7 bits. Convert to 8 bits now. */

                        K053260_channel[channel].volume = ((v & 0x7f) << 1) | (v & 1);
                        break;
                }

                return;
            }

            switch (r) {
                case 0x2a: /* loop, ppcm */

                    for (i = 0; i < 4; i++) {
                        K053260_channel[i].loop = ((v & (1 << i)) != 0) ? 1 : 0;
                    }

                    for (i = 4; i < 8; i++) {
                        K053260_channel[i - 4].ppcm = ((v & (1 << i)) != 0) ? 1 : 0;
                    }
                    break;

                case 0x2c: /* pan */

                    K053260_channel[0].pan = v & 7;
                    K053260_channel[1].pan = (v >> 3) & 7;
                    break;

                case 0x2d: /* more pan */

                    K053260_channel[2].pan = v & 7;
                    K053260_channel[3].pan = (v >> 3) & 7;
                    break;

                case 0x2f: /* control */

                    K053260_chip.mode = v & 7;
                    /* bit 0 = read ROM */
                    /* bit 1 = enable sound output */
                    /* bit 2 = unknown */
                    break;
            }

        }
    };
    public static ReadHandlerPtr K053260_ReadReg = new ReadHandlerPtr() {
        public int handler(int r) {
            switch (r) {
                case 0x29: /* channel status */ {
                    int i, status = 0;

                    for (i = 0; i < 4; i++) {
                        status |= K053260_channel[i].play << i;
                    }

                    return status;
                }
                case 0x2e: /* read rom */

                    if ((K053260_chip.mode & 1) != 0) {
                        long offs = K053260_channel[0].start + (K053260_channel[0].pos >> BASE_SHIFT) + (K053260_channel[0].bank << 16);

                        K053260_channel[0].pos += (1 << 16);

                        if (offs > K053260_chip.rom_size) {
                            if (errorlog != null) {
                                fprintf(errorlog, "K53260: Attempting to read past rom size on rom Read Mode.\n");
                            }

                            return 0;
                        }

                        return K053260_chip.rom.read((int) offs);
                    }
                    break;
            }

            return K053260_chip.regs[r];
        }
    };
}
