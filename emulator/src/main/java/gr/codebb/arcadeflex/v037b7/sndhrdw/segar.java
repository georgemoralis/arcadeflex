 /*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.sndhrdw;

import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static gr.codebb.arcadeflex.v036.cpu.i8039.i8039H.I8039_EXT_INT;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.sound.samples.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.sound.tms36xx.*;
import static gr.codebb.arcadeflex.v037b7.sound.dac.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.machine.segacrpt.*;

public class segar {

    public static final int _TOTAL_SOUNDS = 16;

    public static class sa {

        public sa(int channel, int num, int looped, int stoppable, int restartable) {
            this.channel = channel;
            this.num = num;
            this.looped = looped;
            this.stoppable = stoppable;
            this.restartable = restartable;
        }

        public sa(int channel) {
            this.channel = channel;
        }
        int channel;
        int num;
        int looped;
        int stoppable;
        int restartable;
    }

    /**
     * *************************************************************************
     * Astro Blaster
     *
     * Sound is created through two boards. The Astro Blaster Sound Board
     * consists solely of sounds created through discrete circuitry. The G-80
     * Speech Synthesis Board consists of an 8035 and a speech synthesizer.
     * *************************************************************************
     */
    /* Special temporary code for Astro Blaster Speech handling */
    public static final int MAX_SPEECH_QUEUE = 10;
    public static final int NOT_PLAYING = -1;
    /* The queue holds the sample # so -1 will indicate no sample */
    public static final int SPEECH_CHANNEL = 11;
    /* Note that Astro Blaster sounds only use tracks 0-10 */

    static int[] speechQueue = new int[MAX_SPEECH_QUEUE];
    static int speechQueuePtr = 0;
    /* End of speech code */

    public static String astrob_sample_names[]
            = {
                "*astrob",
                "invadr1.wav", "winvadr1.wav", "invadr2.wav", "winvadr2.wav",
                "invadr3.wav", "winvadr3.wav", "invadr4.wav", "winvadr4.wav",
                "asteroid.wav", "refuel.wav",
                "pbullet.wav", "ebullet.wav", "eexplode.wav", "pexplode.wav",
                "deedle.wav", "sonar.wav",
                "01.wav", "02.wav", "03.wav", "04.wav", "05.wav", "06.wav", "07.wav", "08.wav",
                "09.wav", "0a.wav", "0b.wav", "0c.wav", "0d.wav", "0e.wav", "0f.wav", "10.wav",
                "11.wav", "12.wav", "13.wav", "14.wav", "15.wav", "16.wav", "17.wav", "18.wav",
                "19.wav", "1a.wav", "1b.wav", "1c.wav", "1d.wav", "1e.wav", "1f.wav", "20.wav",
                "22.wav", "23.wav",
                null
            };

    public static final int invadr1 = 0;
    public static final int winvadr1 = 1;
    public static final int invadr2 = 2;
    public static final int winvadr2 = 3;
    public static final int invadr3 = 4;
    public static final int winvadr3 = 5;
    public static final int invadr4 = 6;
    public static final int winvadr4 = 7;
    public static final int asteroid = 8;
    public static final int refuel = 9;
    public static final int pbullet = 10;
    public static final int ebullet = 11;
    public static final int eexplode = 12;
    public static final int pexplode = 13;
    public static final int deedle = 14;
    public static final int sonar = 15;
    public static final int v01 = 16;
    public static final int v02 = 17;
    public static final int v03 = 18;
    public static final int v04 = 19;
    public static final int v05 = 20;
    public static final int v06 = 21;
    public static final int v07 = 22;
    public static final int v08 = 23;
    public static final int v09 = 24;
    public static final int v0a = 25;
    public static final int v0b = 26;
    public static final int v0c = 27;
    public static final int v0d = 28;
    public static final int v0e = 29;
    public static final int v0f = 30;
    public static final int v10 = 31;
    public static final int v11 = 32;
    public static final int v12 = 33;
    public static final int v13 = 34;
    public static final int v14 = 35;
    public static final int v15 = 36;
    public static final int v16 = 37;
    public static final int v17 = 38;
    public static final int v18 = 39;
    public static final int v19 = 40;
    public static final int v1a = 41;
    public static final int v1b = 42;
    public static final int v1c = 43;
    public static final int v1d = 44;
    public static final int v1e = 45;
    public static final int v1f = 46;
    public static final int v20 = 47;
    public static final int v22 = 48;
    public static final int v23 = 49;

    static sa astrob_sa[]
            = {
                /* Port 0x3E: */
                new sa(0, invadr1, 1, 1, 1), /* Line  0 - Invader 1 */
                new sa(1, invadr2, 1, 1, 1), /* Line  1 - Invader 2 */
                new sa(2, invadr3, 1, 1, 1), /* Line  2 - Invader 3 */
                new sa(3, invadr4, 1, 1, 1), /* Line  3 - Invader 4 */
                new sa(4, asteroid, 1, 1, 1), /* Line  4 - Asteroids */
                new sa(-1), /* Line  5 - <Mute> */
                new sa(5, refuel, 0, 1, 0), /* Line  6 - Refill */
                new sa(-1), /* Line  7 - <Warp Modifier> */
                /* Port 0x3F: */
                new sa(6, pbullet, 0, 0, 1), /* Line  0 - Laser #1 */
                new sa(7, ebullet, 0, 0, 1), /* Line  1 - Laser #2 */
                new sa(8, eexplode, 0, 0, 1), /* Line  2 - Short Explosion */
                new sa(8, pexplode, 0, 0, 1), /* Line  3 - Long Explosion */
                new sa(-1), /* Line  4 - <Attack Rate> */
                new sa(-1), /* Line  5 - <Rate Reset> */
                new sa(9, deedle, 0, 0, 1), /* Line  6 - Bonus */
                new sa(10, sonar, 0, 0, 1), /* Line  7 - Sonar */};

    /* Special speech handling code.  Someday this will hopefully be
	   replaced with true speech synthesis. */
    public static ShStartPtr astrob_speech_sh_start = new ShStartPtr() {
        public int handler(MachineSound msound) {
            int i;

            for (i = 0; i < MAX_SPEECH_QUEUE; i++) {
                speechQueue[i] = NOT_PLAYING;
            }
            speechQueuePtr = 0;

            return 0;
        }
    };

    public static ShUpdatePtr astrob_speech_sh_update = new ShUpdatePtr() {
        public void handler() {
            int sound;

            if (Machine.samples == null) {
                return;
            }

            if (speechQueue[speechQueuePtr] != NOT_PLAYING) {
                sound = speechQueue[speechQueuePtr];

                if (sample_playing(SPEECH_CHANNEL) == 0) {
                    if (Machine.samples.sample[sound] != null) {
                        sample_start(SPEECH_CHANNEL, sound, 0);
                    }
                    speechQueue[speechQueuePtr] = NOT_PLAYING;
                    speechQueuePtr = ((speechQueuePtr + 1) % MAX_SPEECH_QUEUE);
                }
            }
        }
    };

    static void astrob_queue_speech(int sound) {
        int newPtr;

        /* Queue the new sound */
        newPtr = speechQueuePtr;
        while (speechQueue[newPtr] != NOT_PLAYING) {
            newPtr = ((newPtr + 1) % MAX_SPEECH_QUEUE);
            if (newPtr == speechQueuePtr) {
                /* The queue has overflowed. Oops. */
                logerror("*** Speech queue overflow!\n");
                return;
            }
        }
        speechQueue[newPtr] = sound;
    }

    public static WriteHandlerPtr astrob_speech_port_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (Machine.samples == null) {
                return;
            }

            switch (data) {
                case 0x01:
                    astrob_queue_speech(v01);
                    break;
                case 0x02:
                    astrob_queue_speech(v02);
                    break;
                case 0x03:
                    astrob_queue_speech(v03);
                    break;
                case 0x04:
                    astrob_queue_speech(v04);
                    break;
                case 0x05:
                    astrob_queue_speech(v05);
                    break;
                case 0x06:
                    astrob_queue_speech(v06);
                    break;
                case 0x07:
                    astrob_queue_speech(v07);
                    break;
                case 0x08:
                    astrob_queue_speech(v08);
                    break;
                case 0x09:
                    astrob_queue_speech(v09);
                    break;
                case 0x0a:
                    astrob_queue_speech(v0a);
                    break;
                case 0x0b:
                    astrob_queue_speech(v0b);
                    break;
                case 0x0c:
                    astrob_queue_speech(v0c);
                    break;
                case 0x0d:
                    astrob_queue_speech(v0d);
                    break;
                case 0x0e:
                    astrob_queue_speech(v0e);
                    break;
                case 0x0f:
                    astrob_queue_speech(v0f);
                    break;
                case 0x10:
                    astrob_queue_speech(v10);
                    break;
                case 0x11:
                    astrob_queue_speech(v11);
                    break;
                case 0x12:
                    astrob_queue_speech(v12);
                    break;
                case 0x13:
                    astrob_queue_speech(v13);
                    break;
                case 0x14:
                    astrob_queue_speech(v14);
                    break;
                case 0x15:
                    astrob_queue_speech(v15);
                    break;
                case 0x16:
                    astrob_queue_speech(v16);
                    break;
                case 0x17:
                    astrob_queue_speech(v17);
                    break;
                case 0x18:
                    astrob_queue_speech(v18);
                    break;
                case 0x19:
                    astrob_queue_speech(v19);
                    break;
                case 0x1a:
                    astrob_queue_speech(v1a);
                    break;
                case 0x1b:
                    astrob_queue_speech(v1b);
                    break;
                case 0x1c:
                    astrob_queue_speech(v1c);
                    break;
                case 0x1d:
                    astrob_queue_speech(v1d);
                    break;
                case 0x1e:
                    astrob_queue_speech(v1e);
                    break;
                case 0x1f:
                    astrob_queue_speech(v1f);
                    break;
                case 0x20:
                    astrob_queue_speech(v20);
                    break;
                case 0x22:
                    astrob_queue_speech(v22);
                    break;
                case 0x23:
                    astrob_queue_speech(v23);
                    break;
            }
        }
    };

    public static WriteHandlerPtr astrob_audio_ports_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int line;
            int noise;
            int warp = 0;

            /* First, handle special control lines: */
 /* MUTE */
            if ((offset == 0) && (data & 0x20) != 0) {
                /* Note that this also stops our speech from playing. */
 /* (If our speech ever gets synthesized, this will probably
			   need to call some type of speech_mute function) */
                for (noise = 0; noise < _TOTAL_SOUNDS; noise++) {
                    sample_stop(astrob_sa[noise].channel);
                }
                return;
            }

            /* WARP */
            if ((offset == 0) && ((data & 0x80) == 0)) {
                warp = 1;
            }

            /* ATTACK RATE */
            if ((offset == 1) && ((data & 0x10) == 0)) {
                /* TODO: this seems to modify the speed of the invader sounds */
            }

            /* RATE RESET */
            if ((offset == 1) && ((data & 0x20) == 0)) {
                /* TODO: this seems to modify the speed of the invader sounds */
            }

            /* Now, play our discrete sounds */
            for (line = 0; line < 8; line++) {
                noise = 8 * offset + line;

                if (astrob_sa[noise].channel != -1) {
                    /* trigger sound */
                    if ((data & (1 << line)) == 0) {
                        /* Special case: If we're on Invaders sounds, modify with warp */
                        if ((astrob_sa[noise].num >= invadr1)
                                && (astrob_sa[noise].num <= invadr4)) {
                            if (astrob_sa[noise].restartable != 0 || sample_playing(astrob_sa[noise].channel) == 0) {
                                sample_start(astrob_sa[noise].channel, astrob_sa[noise].num + warp, astrob_sa[noise].looped);
                            }
                        } /* All other sounds are normal */ else {
                            if (astrob_sa[noise].restartable != 0 || sample_playing(astrob_sa[noise].channel) == 0) {
                                sample_start(astrob_sa[noise].channel, astrob_sa[noise].num, astrob_sa[noise].looped);
                            }
                        }
                    } else {
                        if (sample_playing(astrob_sa[noise].channel) != 0 && astrob_sa[noise].stoppable != 0) {
                            sample_stop(astrob_sa[noise].channel);
                        }
                    }
                }
            }
        }
    };

    /**
     * *************************************************************************
     * 005
     *
     * The Sound Board seems to consist of the following: An 8255: Port A
     * controls the sounds that use discrete circuitry A0 - Large Expl. Sound
     * Trig A1 - Small Expl. Sound Trig A2 - Drop Sound Bomb Trig A3 - Shoot
     * Sound Pistol Trig A4 - Missile Sound Trig A5 - Helicopter Sound Trig A6 -
     * Whistle Sound Trig A7 - <unused>
     * Port B controls the melody generator (described below) B0-B3 - connected
     * to addr lines A7-A10 of the 2716 sound ROM B4 - connected to 1CL and 2CL
     * on the LS393, and to RD on an LS293 just before output B5-B6 - connected
     * to 1CK on the LS393 B7 - <unused>
     * Port C is apparently unused
     *
     * Melody Generator: There's an LS393 counter hooked to addr lines A0-A6 of
     * a 2716 ROM. Port B from the 8255 hooks to addr lines A7-A10 of the 2716.
     * D0-D4 output from the 2716 into an 6331. D5 outputs from the 2716 to a
     * 555 timer. D0-D7 on the 6331 output to two LS161s. The LS161s output to
     * the LS293 (also connected to 8255 B4). The output of this feeds into a
     * 4391, which produces "MELODY" output.
     * *************************************************************************
     */
    public static String s005_sample_names[]
            = {
                null
            };

    /**
     * *************************************************************************
     * Space Odyssey
     *
     * The Sound Board consists solely of sounds created through discrete
     * circuitry.
     * *************************************************************************
     */
    public static String spaceod_sample_names[]
            = {
                "fire.wav", "bomb.wav", "eexplode.wav", "pexplode.wav",
                "warp.wav", "birth.wav", "scoreup.wav", "ssound.wav",
                "accel.wav", "damaged.wav", "erocket.wav",
                null
            };

    public static final int sofire = 0;
    public static final int sobomb = 1;
    public static final int soeexplode = 2;
    public static final int sopexplode = 3;
    public static final int sowarp = 4;
    public static final int sobirth = 5;
    public static final int soscoreup = 6;
    public static final int sossound = 7;
    public static final int soaccel = 8;
    public static final int sodamaged = 9;
    public static final int soerocket = 10;

    static sa spaceod_sa[]
            = {
                /* Port 0x0E: */
                //      {  0, sossound,   1, 1, 1 },    /* Line  0 - background noise */
                new sa(-1),
                /* Line  0 - background noise */
                new sa(-1),
                /* Line  1 - unused */
                new sa(1, soeexplode, 0, 0, 1),
                /* Line  2 - Short Explosion */
                new sa(-1),
                /* Line  3 - unused */
                new sa(2, soaccel, 0, 0, 1),
                /* Line  4 - Accelerate */
                new sa(3, soerocket, 0, 0, 1),
                /* Line  5 - Battle Star */
                new sa(4, sobomb, 0, 0, 1),
                /* Line  6 - D. Bomb */
                new sa(5, sopexplode, 0, 0, 1),
                /* Line  7 - Long Explosion */
                /* Port 0x0F: */
                new sa(6, sofire, 0, 0, 1),
                /* Line  0 - Shot */
                new sa(7, soscoreup, 0, 0, 1),
                /* Line  1 - Bonus Up */
                new sa(-1),
                /* Line  2 - unused */
                new sa(8, sowarp, 0, 0, 1),
                /* Line  3 - Warp */
                new sa(-1),
                /* Line  4 - unused */
                new sa(-1),
                /* Line  5 - unused */
                new sa(9, sobirth, 0, 0, 1),
                /* Line  6 - Appearance UFO */
                new sa(10, sodamaged, 0, 0, 1) /* Line  7 - Black Hole */};

    public static WriteHandlerPtr spaceod_audio_ports_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int line;
            int noise;

            for (line = 0; line < 8; line++) {
                noise = 8 * offset + line;

                if (spaceod_sa[noise].channel != -1) {
                    /* trigger sound */
                    if ((data & (1 << line)) == 0) {
                        if (spaceod_sa[noise].restartable != 0 || sample_playing(spaceod_sa[noise].channel) == 0) {
                            sample_start(spaceod_sa[noise].channel, spaceod_sa[noise].num, spaceod_sa[noise].looped);
                        }
                    } else {
                        if (sample_playing(spaceod_sa[noise].channel) != 0 && spaceod_sa[noise].stoppable != 0) {
                            sample_stop(spaceod_sa[noise].channel);
                        }
                    }
                }
            }
        }
    };

    /**
     * *************************************************************************
     * Monster Bash
     *
     * The Sound Board is a fairly complex mixture of different components. An
     * 8255A-5 controls the interface to/from the sound board. Port A connects
     * to a TMS3617 (basic music synthesizer) circuit. Port B connects to two
     * sounds generated by discrete circuitry. Port C connects to a NEC7751
     * (8048 CPU derivative) to control four "samples".
     * *************************************************************************
     */
    static /*unsigned*/ int port_8255_c03 = 0;
    static /*unsigned*/ int port_8255_c47 = 0;
    static /*unsigned*/ int port_7751_p27 = 0;
    static /*unsigned*/ int rom_offset = 0;

    public static String monsterb_sample_names[] = {
        "zap.wav", "jumpdown.wav",
        null
    };

    public static final int mbzap = 0;
    public static final int mbjumpdown = 1;

    /* Monster Bash uses an 8255 to control the sounds, much like Zaxxon */
    public static WriteHandlerPtr monsterb_audio_8255_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* Port A controls the special TMS3617 music chip */
            if (offset == 0) {
                int enable_val;

                /* Lower four data lines get decoded into 13 control lines */
                tms36xx_note_w(0, 0, data & 15);

                /* Top four data lines address an 82S123 ROM that enables/disables voices */
                enable_val = memory_region(REGION_SOUND2).read((data & 0xF0) >> 4);
                tms3617_enable_w(0, enable_val >> 2);
            } /* Port B controls the two discrete sound circuits */ else if (offset == 1) {
                if ((data & 0x01) == 0) {
                    sample_start(0, mbzap, 0);
                }

                if ((data & 0x02) == 0) {
                    sample_start(1, mbjumpdown, 0);
                }

                /* TODO: D7 on Port B might affect TMS3617 output (mute?) */
            } /* Port C controls a NEC7751, which is an 8048 CPU with onboard ROM */ else if (offset == 2) {
                // D0-D2 = P24-P26, D3 = INT
                port_8255_c03 = data & 0x0F;
                if ((data & 0x08) == 0) {
                    cpu_cause_interrupt(1, I8039_EXT_INT);
                }

            } /* Write to 8255 control port, this should be 0x80 for "simple mode" */ else {
                if (data != 0x80) {
                    logerror("8255 Control Port Write = %02X\n", data);
                }
            }
        }
    };

    public static ReadHandlerPtr monsterb_audio_8255_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            // Only PC4 is hooked up
            /* 0x00 = BUSY, 0x10 = NOT BUSY */
            return (port_8255_c47 & 0x10);
        }
    };

    /* read from BUS */
    public static ReadHandlerPtr monsterb_sh_rom_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            UBytePtr sound_rom = memory_region(REGION_SOUND1);

            return sound_rom.read(rom_offset);
        }
    };

    /* read from T1 */
    public static ReadHandlerPtr monsterb_sh_t1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            // Labelled as "TEST", connected to ground
            return 0;
        }
    };

    /* read from P2 */
    public static ReadHandlerPtr monsterb_sh_command_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            // 8255's PC0-2 connects to 7751's S0-2 (P24-P26 on an 8048)
            return ((port_8255_c03 & 0x07) << 4) | port_7751_p27;
        }
    };

    /* write to P1 */
    public static WriteHandlerPtr monsterb_sh_dac_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            DAC_data_w.handler(0, data);
        }
    };

    /* write to P2 */
    public static WriteHandlerPtr monsterb_sh_busy_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            // 8255's PC0-2 connects to 7751's S0-2 (P24-P26 on an 8048)
            // 8255's PC4 connects to 7751's BSY OUT (P27 on an 8048)
            port_8255_c03 = (data & 0x70) >> 4;
            port_8255_c47 = (data & 0x80) >> 3;
            port_7751_p27 = data & 0x80;
        }
    };

    /* write to P4 */
    public static WriteHandlerPtr monsterb_sh_offset_a0_a3_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            rom_offset = (rom_offset & 0x1FF0) | (data & 0x0F);
        }
    };

    /* write to P5 */
    public static WriteHandlerPtr monsterb_sh_offset_a4_a7_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            rom_offset = (rom_offset & 0x1F0F) | ((data & 0x0F) << 4);
        }
    };

    /* write to P6 */
    public static WriteHandlerPtr monsterb_sh_offset_a8_a11_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            rom_offset = (rom_offset & 0x10FF) | ((data & 0x0F) << 8);
        }
    };

    /* write to P7 */
    public static WriteHandlerPtr monsterb_sh_rom_select_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            rom_offset = (rom_offset & 0x0FFF);

            /* D0 = !ROM1 enable, D1 = !ROM2 enable, D2/3 hit empty sockets. */
            if ((data & 0x02) == 0) {
                rom_offset |= 0x1000;
            }
        }
    };

}
