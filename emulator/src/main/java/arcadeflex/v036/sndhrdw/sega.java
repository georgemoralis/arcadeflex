/*
 * ported to v0.36
 * using automatic conversion tool v0.01
 */
package arcadeflex.v036.sndhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.sndintrfH.*;
//TODO
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.mame.errorlog;
import static gr.codebb.arcadeflex.v036.platform.libc_old.fprintf;
import static arcadeflex.v036.sound.samples.*;

public class sega {

    /* Some Tac/Scan sound constants */
    public static final int shipStop = 0x10;
    public static final int shipLaser = 0x18;
    public static final int shipExplosion = 0x20;
    public static final int shipDocking = 0x28;
    public static final int shipRoar = 0x40;
    public static final int tunnelHigh = 0x48;
    public static final int stingerThrust = 0x50;
    public static final int stingerLaser = 0x51;
    public static final int stingerStop = 0x52;
    public static final int stingerExplosion = 0x54;
    public static final int enemyBullet0 = 0x61;
    public static final int enemyBullet1 = 0x62;
    public static final int enemyBullet2 = 0x63;
    public static final int enemyExplosion0 = 0x6c;
    public static final int enemyExplosion1 = 0x6d;
    public static final int enemyExplosion2 = 0x6e;
    public static final int tunnelw = 0x09;
    public static final int flight1 = 0x36;
    public static final int flight2 = 0x3b;
    public static final int flight3 = 0x3d;
    public static final int flight4 = 0x3e;
    public static final int flight5 = 0x3f;
    public static final int warp = 0x37;
    public static final int formation = 0x0b;
    public static final int nothing1 = 0x1a;
    public static final int nothing2 = 0x1b;
    public static final int extralife = 0x1c;
    public static final int credit = 0x2c;

    public static final int kVoiceShipRoar = 5;
    public static final int kVoiceShip = 1;
    public static final int kVoiceTunnel = 2;
    public static final int kVoiceStinger = 3;
    public static final int kVoiceEnemy = 4;
    public static final int kVoiceExtra = 8;
    public static final int kVoiceForm = 7;
    public static final int kVoiceWarp = 6;
    public static final int kVoiceExtralife = 9;

    static int roarPlaying;
    /* Is the ship roar noise playing? */

 /*
     * The speech samples are queued in the order they are received
	 * in sega_sh_speech_w (). sega_sh_update () takes care of playing
	 * and updating the sounds in the order they were queued.
     */
    public static final int MAX_SPEECH = 16;
    /* Number of speech samples which can be queued */
    public static final int NOT_PLAYING = -1;
    /* Queue position empty */

    static int[] queue = new int[MAX_SPEECH];
    static int queuePtr = 0;

    public static ShStartHandlerPtr sega_sh_start = new ShStartHandlerPtr() {
        public int handler(MachineSound msound) {
            int i;

            for (i = 0; i < MAX_SPEECH; i++) {
                queue[i] = NOT_PLAYING;
            }

            return 0;
        }
    };

    public static ReadHandlerPtr sega_sh_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            /* 0x80 = universal sound board ready */
 /* 0x01 = speech ready */

            if (sample_playing(0) != 0) {
                return 0x81;
            } else {
                return 0x80;
            }
        }
    };

    public static WriteHandlerPtr sega_sh_speech_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int sound;

            sound = data & 0x7f;
            /* The sound numbers start at 1 but the sample name array starts at 0 */
            sound--;

            if (sound < 0) /* Can this happen? */ {
                return;
            }

            if ((data & 0x80) == 0) {
                /* This typically comes immediately after a speech command. Purpose? */
                return;
            } else if (Machine.samples != null && sound < Machine.samples.sample.length && Machine.samples.sample[sound] != null) {
                int newPtr;

                /* Queue the new sound */
                newPtr = queuePtr;
                while (queue[newPtr] != NOT_PLAYING) {
                    newPtr++;
                    if (newPtr >= MAX_SPEECH) {
                        newPtr = 0;
                    }
                    if (newPtr == queuePtr) {
                        /* The queue has overflowed. Oops. */
                        if (errorlog != null) {
                            fprintf(errorlog, "*** Queue overflow! queuePtr: %02d\n", queuePtr);
                        }
                        return;
                    }
                }
                queue[newPtr] = sound;
            }
        }
    };

    public static ShUpdateHandlerPtr sega_sh_update = new ShUpdateHandlerPtr() {
        public void handler() {
            int sound;

            /* if a sound is playing, return */
            if (sample_playing(0) != 0) {
                return;
            }

            /* Check the queue position. If a sound is scheduled, play it */
            if (queue[queuePtr] != NOT_PLAYING) {
                sound = queue[queuePtr];
                sample_start(0, sound, 0);

                /* Update the queue pointer to the next one */
                queue[queuePtr] = NOT_PLAYING;
                ++queuePtr;
                if (queuePtr >= MAX_SPEECH) {
                    queuePtr = 0;
                }
            }
        }
    };

    public static ShStartHandlerPtr tacscan_sh_start = new ShStartHandlerPtr() {
        public int handler(MachineSound msound) {
            roarPlaying = 0;
            return 0;
        }
    };

    public static WriteHandlerPtr tacscan_sh_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int sound;
            /* index into the sample name array in drivers/sega.c */
            int voice = 0;
            /* which voice to play the sound on */
            int loop;
            /* is this sound continuous? */

            loop = 0;
            switch (data) {
                case shipRoar:
                    /* Play the increasing roar noise */
                    voice = kVoiceShipRoar;
                    sound = 0;
                    roarPlaying = 1;
                    break;
                case shipStop:
                    /* Play the decreasing roar noise */
                    voice = kVoiceShipRoar;
                    sound = 2;
                    roarPlaying = 0;
                    break;
                case shipLaser:
                    voice = kVoiceShip;
                    sound = 3;
                    break;
                case shipExplosion:
                    voice = kVoiceShip;
                    sound = 4;
                    break;
                case shipDocking:
                    voice = kVoiceShip;
                    sound = 5;
                    break;
                case tunnelHigh:
                    voice = kVoiceTunnel;
                    sound = 6;
                    break;
                case stingerThrust:
                    voice = kVoiceStinger;
                    sound = 7;
                    loop = 0; //leave off sound gets stuck on
                    break;
                case stingerLaser:
                    voice = kVoiceStinger;
                    sound = 8;
                    loop = 0;
                    break;
                case stingerExplosion:
                    voice = kVoiceStinger;
                    sound = 9;
                    break;
                case stingerStop:
                    voice = kVoiceStinger;
                    sound = -1;
                    break;
                case enemyBullet0:
                case enemyBullet1:
                case enemyBullet2:
                    voice = kVoiceEnemy;
                    sound = 10;
                    break;
                case enemyExplosion0:
                case enemyExplosion1:
                case enemyExplosion2:
                    voice = kVoiceTunnel;
                    sound = 11;
                    break;
                case tunnelw:
                    voice = kVoiceShip;
                    sound = 12;
                    break;
                case flight1:
                    voice = kVoiceExtra;
                    sound = 13;
                    break;
                case flight2:
                    voice = kVoiceExtra;
                    sound = 14;
                    break;
                case flight3:
                    voice = kVoiceExtra;
                    sound = 15;
                    break;
                case flight4:
                    voice = kVoiceExtra;
                    sound = 16;
                    break;
                case flight5:
                    voice = kVoiceExtra;
                    sound = 17;
                    break;
                case formation:
                    voice = kVoiceForm;
                    sound = 18;
                    break;
                case warp:
                    voice = kVoiceExtra;
                    sound = 19;
                    break;
                case extralife:
                    voice = kVoiceExtralife;
                    sound = 20;
                    break;
                case credit:
                    voice = kVoiceExtra;
                    sound = 21;
                    break;

                default:

                    /* don't play anything */
                    sound = -1;
                    break;
            }
            if (sound != -1) {
                sample_stop(voice);
                /* If the game is over, turn off the stinger noise */
                if (data == shipStop) {
                    sample_stop(kVoiceStinger);
                }
                sample_start(voice, sound, loop);
            }
        }
    };

    public static ShUpdateHandlerPtr tacscan_sh_update = new ShUpdateHandlerPtr() {
        public void handler() {
            /* If the ship roar has started playing but the sample stopped */
 /* play the intermediate roar noise */

            if ((roarPlaying != 0) && (sample_playing(kVoiceShipRoar) == 0)) {
                sample_start(kVoiceShipRoar, 1, 1);
            }
        }
    };

    public static WriteHandlerPtr elim1_sh_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            data ^= 0xff;

            /* Play fireball sample */
            if ((data & 0x02) != 0) {
                sample_start(0, 0, 0);
            }

            /* Play explosion samples */
            if ((data & 0x04) != 0) {
                sample_start(1, 10, 0);
            }
            if ((data & 0x08) != 0) {
                sample_start(1, 9, 0);
            }
            if ((data & 0x10) != 0) {
                sample_start(1, 8, 0);
            }

            /* Play bounce sample */
            if ((data & 0x20) != 0) {
                if (sample_playing(2) != 0) {
                    sample_stop(2);
                }
                sample_start(2, 1, 0);
            }

            /* Play lazer sample */
            if ((data & 0xc0) != 0) {
                if (sample_playing(3) != 0) {
                    sample_stop(3);
                }
                sample_start(3, 5, 0);
            }
        }
    };

    public static WriteHandlerPtr elim2_sh_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            data ^= 0xff;

            /* Play thrust sample */
            if ((data & 0x0f) != 0) {
                sample_start(4, 6, 0);
            } else {
                sample_stop(4);
            }

            /* Play skitter sample */
            if ((data & 0x10) != 0) {
                sample_start(5, 2, 0);
            }

            /* Play eliminator sample */
            if ((data & 0x20) != 0) {
                sample_start(6, 3, 0);
            }

            /* Play electron samples */
            if ((data & 0x40) != 0) {
                sample_start(7, 7, 0);
            }
            if ((data & 0x80) != 0) {
                sample_start(7, 4, 0);
            }
        }
    };

    public static WriteHandlerPtr zektor1_sh_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            data ^= 0xff;

            /* Play fireball sample */
            if ((data & 0x02) != 0) {
                sample_start(0, 19, 0);
            }

            /* Play explosion samples */
            if ((data & 0x04) != 0) {
                sample_start(1, 29, 0);
            }
            if ((data & 0x08) != 0) {
                sample_start(1, 28, 0);
            }
            if ((data & 0x10) != 0) {
                sample_start(1, 27, 0);
            }

            /* Play bounce sample */
            if ((data & 0x20) != 0) {
                if (sample_playing(2) != 0) {
                    sample_stop(2);
                }
                sample_start(2, 20, 0);
            }

            /* Play lazer sample */
            if ((data & 0xc0) != 0) {
                if (sample_playing(3) != 0) {
                    sample_stop(3);
                }
                sample_start(3, 24, 0);
            }
        }
    };

    public static WriteHandlerPtr zektor2_sh_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            data ^= 0xff;

            /* Play thrust sample */
            if ((data & 0x0f) != 0) {
                sample_start(4, 25, 0);
            } else {
                sample_stop(4);
            }

            /* Play skitter sample */
            if ((data & 0x10) != 0) {
                sample_start(5, 21, 0);
            }

            /* Play eliminator sample */
            if ((data & 0x20) != 0) {
                sample_start(6, 22, 0);
            }

            /* Play electron samples */
            if ((data & 0x40) != 0) {
                sample_start(7, 40, 0);
            }
            if ((data & 0x80) != 0) {
                sample_start(7, 41, 0);
            }
        }
    };

    public static WriteHandlerPtr startrek_sh_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            switch (data) {
                case 0x08:
                    /* phaser - trek1.wav */
                    sample_start(1, 0x17, 0);
                    break;
                case 0x0a:
                    /* photon - trek2.wav */
                    sample_start(1, 0x18, 0);
                    break;
                case 0x0e:
                    /* targeting - trek3.wav */
                    sample_start(1, 0x19, 0);
                    break;
                case 0x10:
                    /* dent - trek4.wav */
                    sample_start(2, 0x1a, 0);
                    break;
                case 0x12:
                    /* shield hit - trek5.wav */
                    sample_start(2, 0x1b, 0);
                    break;
                case 0x14:
                    /* enterprise hit - trek6.wav */
                    sample_start(2, 0x1c, 0);
                    break;
                case 0x16:
                    /* enterprise explosion - trek7.wav */
                    sample_start(2, 0x1d, 0);
                    break;
                case 0x1a:
                    /* klingon explosion - trek8.wav */
                    sample_start(2, 0x1e, 0);
                    break;
                case 0x1c:
                    /* dock - trek9.wav */
                    sample_start(1, 0x1f, 0);
                    break;
                case 0x1e:
                    /* starbase hit - trek10.wav */
                    sample_start(1, 0x20, 0);
                    break;
                case 0x11:
                    /* starbase red - trek11.wav */
                    sample_start(1, 0x21, 0);
                    break;
                case 0x22:
                    /* starbase explosion - trek12.wav */
                    sample_start(2, 0x22, 0);
                    break;
                case 0x24:
                    /* small bonus - trek13.wav */
                    sample_start(3, 0x23, 0);
                    break;
                case 0x25:
                    /* large bonus - trek14.wav */
                    sample_start(3, 0x24, 0);
                    break;
                case 0x26:
                    /* starbase intro - trek15.wav */
                    sample_start(1, 0x25, 0);
                    break;
                case 0x27:
                    /* klingon intro - trek16.wav */
                    sample_start(1, 0x26, 0);
                    break;
                case 0x28:
                    /* enterprise intro - trek17.wav */
                    sample_start(1, 0x27, 0);
                    break;
                case 0x29:
                    /* player change - trek18.wav */
                    sample_start(1, 0x28, 0);
                    break;
                case 0x2e:
                    /* klingon fire - trek19.wav */
                    sample_start(2, 0x29, 0);
                    break;
                case 0x04:
                    /* impulse start - trek20.wav */
                    sample_start(3, 0x2a, 0);
                    break;
                case 0x06:
                    /* warp start - trek21.wav */
                    sample_start(3, 0x2b, 0);
                    break;
                case 0x0c:
                    /* red alert start - trek22.wav */
                    sample_start(4, 0x2c, 0);
                    break;
                case 0x18:
                    /* warp suck - trek23.wav */
                    sample_start(4, 0x2d, 0);
                    break;
                case 0x19:
                    /* saucer exit - trek24.wav */
                    sample_start(4, 0x2e, 0);
                    break;
                case 0x2c:
                    /* nomad motion - trek25.wav */
                    sample_start(5, 0x2f, 0);
                    break;
                case 0x2d:
                    /* nomad stopped - trek26.wav */
                    sample_start(5, 0x30, 0);
                    break;
                case 0x2b:
                    /* coin drop music - trek27.wav */
                    sample_start(1, 0x31, 0);
                    break;
                case 0x2a:
                    /* high score music - trek28.wav */
                    sample_start(1, 0x32, 0);
                    break;
            }
        }
    };

    public static WriteHandlerPtr spacfury1_sh_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            data ^= 0xff;

            /* craft growing */
            if ((data & 0x01) != 0) {
                sample_start(1, 0x15, 0);
            }

            /* craft moving */
            if ((data & 0x02) != 0) {
                if (sample_playing(2) == 0) {
                    sample_start(2, 0x16, 1);
                }
            } else {
                sample_stop(2);
            }

            /* Thrust */
            if ((data & 0x04) != 0) {
                if (sample_playing(3) == 0) {
                    sample_start(3, 0x19, 1);
                }
            } else {
                sample_stop(3);
            }

            /* star spin */
            if ((data & 0x40) != 0) {
                sample_start(4, 0x1d, 0);
            }

            /* partial warship? */
            if ((data & 0x80) != 0) {
                sample_start(4, 0x1e, 0);
            }

        }
    };

    public static WriteHandlerPtr spacfury2_sh_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (Machine.samples == null) {
                return;
            }

            data ^= 0xff;

            /* craft joining */
            if ((data & 0x01) != 0) {
                sample_start(5, 0x17, 0);
            }

            /* ship firing */
            if ((data & 0x02) != 0) {
                if (sample_playing(6) != 0) {
                    sample_stop(6);
                }
                sample_start(6, 0x18, 0);

            }

            /* fireball */
            if ((data & 0x04) != 0) {
                sample_start(7, 0x1b, 0);
            }

            /* small explosion */
            if ((data & 0x08) != 0) {
                sample_start(7, 0x1b, 0);
            }
            /* large explosion */
            if ((data & 0x10) != 0) {
                sample_start(7, 0x1a, 0);
            }

            /* docking bang */
            if ((data & 0x20) != 0) {
                sample_start(8, 0x1c, 0);
            }

        }
    };

}
