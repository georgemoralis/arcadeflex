/*
 * ported to v0.36
 * 
 */
/**
 * Changelog
 * =========
 * 14/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.sndhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
//sound imports
import static arcadeflex.v036.sound.ay8910.*;
import static arcadeflex.v036.sound.samples.*;

public class carnival {

    public static final int CPU_MUSIC_ID = 1;
    /* music CPU id number */


 /* output port 0x01 definitions - sound effect drive outputs */
    public static final int OUT_PORT_1_RIFLE_SHOT = 0x01;
    public static final int OUT_PORT_1_CLANG = 0x02;
    public static final int OUT_PORT_1_DUCK_1 = 0x04;
    public static final int OUT_PORT_1_DUCK_2 = 0x08;
    public static final int OUT_PORT_1_DUCK_3 = 0x10;
    public static final int OUT_PORT_1_PIPE_HIT = 0x20;
    public static final int OUT_PORT_1_BONUS_1 = 0x40;
    public static final int OUT_PORT_1_BONUS_2 = 0x80;

    /* output port 0x02 definitions - sound effect drive outputs */
    public static final int OUT_PORT_2_BEAR = 0x04;
    public static final int OUT_PORT_2_MUSIC_T1 = 0x08;
    public static final int OUT_PORT_2_MUSIC_RESET = 0x10;
    public static final int OUT_PORT_2_RANKING = 0x20;

    /* music CPU port definitions */
    public static final int MUSIC_PORT2_PSG_BDIR = 0x40;
    /* bit 6 on P2 */
    public static final int MUSIC_PORT2_PSG_BC1 = 0x80;
    /* bit 7 on P2 */

    public static final int PSG_BC_INACTIVE = 0;
    public static final int PSG_BC_READ = MUSIC_PORT2_PSG_BC1;
    public static final int PSG_BC_WRITE = MUSIC_PORT2_PSG_BDIR;
    public static final int PSG_BC_LATCH_ADDRESS = (MUSIC_PORT2_PSG_BDIR | MUSIC_PORT2_PSG_BC1);

    public static void PLAY(int id, int loop) {
        sample_start(id, id, loop);
    }

    public static void STOP(int id) {
        sample_stop(id);
    }

    /* sample file names */
    public static String carnival_sample_names[]
            = {
                "*carnival",
                "bear.wav",
                "bonus1.wav",
                "bonus2.wav",
                "clang.wav",
                "duck1.wav",
                "duck2.wav",
                "duck3.wav",
                "pipehit.wav",
                "ranking.wav",
                "rifle.wav",
                null
            };

    /* sample sound IDs - must match sample file name table above */
    public static final int SND_BEAR = 0;
    public static final int SND_BONUS_1 = 1;
    public static final int SND_BONUS_2 = 2;
    public static final int SND_CLANG = 3;
    public static final int SND_DUCK_1 = 4;
    public static final int SND_DUCK_2 = 5;
    public static final int SND_DUCK_3 = 6;
    public static final int SND_PIPE_HIT = 7;
    public static final int SND_RANKING = 8;
    public static final int SND_RIFLE_SHOT = 9;

    static int port2State = 0;
    static int psgData = 0;

    static int port1State = 0;
    public static WriteHandlerPtr carnival_sh_port1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            int bitsChanged;
            int bitsGoneHigh;
            int bitsGoneLow;

            /* U64 74LS374 8 bit latch */
 /* bit 0: connector pin 36 - rifle shot */
 /* bit 1: connector pin 35 - clang */
 /* bit 2: connector pin 33 - duck #1 */
 /* bit 3: connector pin 34 - duck #2 */
 /* bit 4: connector pin 32 - duck #3 */
 /* bit 5: connector pin 31 - pipe hit */
 /* bit 6: connector pin 30 - bonus #1 */
 /* bit 7: connector pin 29 - bonus #2 */
            bitsChanged = port1State ^ data;
            bitsGoneHigh = bitsChanged & data;
            bitsGoneLow = bitsChanged & ~data;

            port1State = data;

            if ((bitsGoneLow & OUT_PORT_1_RIFLE_SHOT) != 0) {
                PLAY(SND_RIFLE_SHOT, 0);
            }

            if ((bitsGoneLow & OUT_PORT_1_CLANG) != 0) {
                PLAY(SND_CLANG, 0);
            }
            if ((bitsGoneHigh & OUT_PORT_1_CLANG) != 0) {
                STOP(SND_CLANG);
            }

            if ((bitsGoneLow & OUT_PORT_1_DUCK_1) != 0) {
                PLAY(SND_DUCK_1, 1);
            }
            if ((bitsGoneHigh & OUT_PORT_1_DUCK_1) != 0) {
                STOP(SND_DUCK_1);
            }

            if ((bitsGoneLow & OUT_PORT_1_DUCK_2) != 0) {
                PLAY(SND_DUCK_2, 1);
            }
            if ((bitsGoneHigh & OUT_PORT_1_DUCK_2) != 0) {
                STOP(SND_DUCK_2);
            }

            if ((bitsGoneLow & OUT_PORT_1_DUCK_3) != 0) {
                PLAY(SND_DUCK_3, 1);
            }
            if ((bitsGoneHigh & OUT_PORT_1_DUCK_3) != 0) {
                STOP(SND_DUCK_3);
            }

            if ((bitsGoneLow & OUT_PORT_1_PIPE_HIT) != 0) {
                PLAY(SND_PIPE_HIT, 0);
            }

            if ((bitsGoneLow & OUT_PORT_1_BONUS_1) != 0) {
                PLAY(SND_BONUS_1, 0);
            }

            if ((bitsGoneLow & OUT_PORT_1_BONUS_2) != 0) {
                PLAY(SND_BONUS_2, 0);
            }
        }
    };

    public static WriteHandlerPtr carnival_sh_port2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int bitsChanged;
            int bitsGoneHigh;
            int bitsGoneLow;

            /* U63 74LS374 8 bit latch */
 /* bit 0: connector pin 48 */
 /* bit 1: connector pin 47 */
 /* bit 2: connector pin 45 - bear */
 /* bit 3: connector pin 46 - Music !T1 input */
 /* bit 4: connector pin 44 - Music reset */
 /* bit 5: connector pin 43 - ranking */
 /* bit 6: connector pin 42 */
 /* bit 7: connector pin 41 */
            bitsChanged = port2State ^ data;
            bitsGoneHigh = bitsChanged & data;
            bitsGoneLow = bitsChanged & ~data;

            port2State = data;

            if ((bitsGoneLow & OUT_PORT_2_BEAR) != 0) {
                PLAY(SND_BEAR, 0);
            }

            if ((bitsGoneLow & OUT_PORT_2_RANKING) != 0) {
                PLAY(SND_RANKING, 0);
            }

            if ((bitsGoneHigh & OUT_PORT_2_MUSIC_RESET) != 0) {
                /* reset output is no longer asserted active low */
                cpu_set_reset_line(CPU_MUSIC_ID, PULSE_LINE);
            }
        }
    };

    public static ReadHandlerPtr carnival_music_port_t1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            /* note: 8039 T1 signal is inverted on music board */
            return (port2State & OUT_PORT_2_MUSIC_T1) != 0 ? 0 : 1;
        }
    };

    public static WriteHandlerPtr carnival_music_port_1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            psgData = data;
        }
    };

    static int psgSelect = 0;
    public static WriteHandlerPtr carnival_music_port_2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            int newSelect;

            newSelect = data & (MUSIC_PORT2_PSG_BDIR | MUSIC_PORT2_PSG_BC1);
            if (psgSelect != newSelect) {
                psgSelect = newSelect;

                switch (psgSelect) {
                    case PSG_BC_INACTIVE:
                        /* do nowt */
                        break;

                    case PSG_BC_READ:
                        /* not very sensible for a write */
                        break;

                    case PSG_BC_WRITE:
                        AY8910_write_port_0_w.handler(0, psgData);
                        break;

                    case PSG_BC_LATCH_ADDRESS:
                        AY8910_control_port_0_w.handler(0, psgData);
                        break;
                }
            }
        }
    };
}
