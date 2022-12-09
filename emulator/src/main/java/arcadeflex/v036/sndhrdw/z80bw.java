/*
 * ported to v0.36
 * using automatic conversion tool v0.07 + manual fixes
 */
package arcadeflex.v036.sndhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.vidhrdw._8080bw.*;
//TODO
import static arcadeflex.v036.sound.samplesH.*;
import static arcadeflex.v036.sound.samples.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;

public class z80bw {

    /* output port 0x04 definitions - sound effect drive outputs */
    public static final int OUT_PORT_4_UFO = 0x01;
    public static final int OUT_PORT_4_SHOT = 0x02;
    public static final int OUT_PORT_4_BASEHIT = 0x04;
    public static final int OUT_PORT_4_INVADERHIT = 0x08;
    public static final int OUT_PORT_4_UNUSED = 0xf0;

    /* output port 0x05 definitions - sound effect drive outputs */
    public static final int OUT_PORT_5_FLEET1 = 0x01;
    public static final int OUT_PORT_5_FLEET2 = 0x02;
    public static final int OUT_PORT_5_FLEET3 = 0x04;
    public static final int OUT_PORT_5_FLEET4 = 0x08;
    public static final int OUT_PORT_5_UFO2 = 0x10;
    public static final int OUT_PORT_5_FLIP = 0x20;
    public static final int OUT_PORT_5_UNUSED = 0xc0;

    public static void PLAY(int id, int loop) {
        sample_start(id, id, loop);
    }

    public static void STOP(int id) {
        sample_stop(id);
    }
    static String astinvad_sample_names[]
            = {
                "*invaders",
                "0.wav",
                "1.wav",
                "2.wav",
                "3.wav",
                "4.wav",
                "5.wav",
                "6.wav",
                "7.wav",
                "8.wav",
                null /* end of array */};

    public static Samplesinterface astinvad_samples_interface = new Samplesinterface(
            9, /* 9 channels */
            25, /* volume */
            astinvad_sample_names
    );

    /* sample sound IDs - must match sample file name table above */
    public static final int SND_UFO = 0;
    public static final int SND_SHOT = 1;
    public static final int SND_BASEHIT = 2;
    public static final int SND_INVADERHIT = 3;
    public static final int SND_FLEET1 = 4;
    public static final int SND_FLEET2 = 5;
    public static final int SND_FLEET3 = 6;
    public static final int SND_FLEET4 = 7;
    public static final int SND_UFO2 = 8;

    /* LT 20-3-1998 */
    static int port4State;
    public static WriteHandlerPtr astinvad_sh_port_4_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int bitsChanged;
            int bitsGoneHigh;
            int bitsGoneLow;

            bitsChanged = port4State ^ data;
            bitsGoneHigh = bitsChanged & data;
            bitsGoneLow = bitsChanged & ~data;

            port4State = data;

            if ((bitsGoneHigh & OUT_PORT_4_UFO) != 0) {
                PLAY(SND_UFO, 1);
            }
            if ((bitsGoneLow & OUT_PORT_4_UFO) != 0) {
                STOP(SND_UFO);
            }

            if ((bitsGoneHigh & OUT_PORT_4_SHOT) != 0) {
                PLAY(SND_SHOT, 0);
            }
            if ((bitsGoneLow & OUT_PORT_4_SHOT) != 0) {
                STOP(SND_SHOT);
            }

            if ((bitsGoneHigh & OUT_PORT_4_BASEHIT) != 0) {
                PLAY(SND_BASEHIT, 0);
                /* turn all colours red here */
                invaders_screen_red_w(1);
            }
            if ((bitsGoneLow & OUT_PORT_4_BASEHIT) != 0) {
                STOP(SND_BASEHIT);
                /* restore colours here */
                invaders_screen_red_w(0);
            }

            if ((bitsGoneHigh & OUT_PORT_4_INVADERHIT) != 0) {
                PLAY(SND_INVADERHIT, 0);
            }
            if ((bitsGoneLow & OUT_PORT_4_INVADERHIT) != 0) {
                STOP(SND_INVADERHIT);
            }

            if (((bitsChanged & OUT_PORT_4_UNUSED) != 0) && errorlog != null) {
                fprintf(errorlog, "Snd Port 4 = %02X\n", data & OUT_PORT_4_UNUSED);
            }
        }
    };

    static int port5State;
    public static WriteHandlerPtr astinvad_sh_port_5_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            int bitsChanged;
            int bitsGoneHigh;
            int bitsGoneLow;

            bitsChanged = port5State ^ data;
            bitsGoneHigh = bitsChanged & data;
            bitsGoneLow = bitsChanged & ~data;

            port5State = data;

            if ((bitsGoneHigh & OUT_PORT_5_FLEET1) != 0) {
                PLAY(SND_FLEET1, 0);
            }

            if ((bitsGoneHigh & OUT_PORT_5_FLEET2) != 0) {
                PLAY(SND_FLEET2, 0);
            }

            if ((bitsGoneHigh & OUT_PORT_5_FLEET3) != 0) {
                PLAY(SND_FLEET3, 0);
            }

            if ((bitsGoneHigh & OUT_PORT_5_FLEET4) != 0) {
                PLAY(SND_FLEET4, 0);
            }

            if ((bitsGoneHigh & OUT_PORT_5_UFO2) != 0) {
                PLAY(SND_UFO2, 0);
            }
            if ((bitsGoneLow & OUT_PORT_5_UFO2) != 0) {
                STOP(SND_UFO2);
            }

            if ((bitsChanged & OUT_PORT_5_FLIP) != 0) {
                invaders_flipscreen_w(data & 0x20);
            }

            if (((bitsChanged & OUT_PORT_5_UNUSED) != 0) && errorlog != null) {
                fprintf(errorlog, "Snd Port 5 = %02X\n", data & OUT_PORT_5_UNUSED);
            }
        }
    };

}
