package gr.codebb.arcadeflex.v036.sndhrdw;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.sound.samples.*;

public class zaxxon {
    public static final int TOTAL_SOUNDS =22;
    static int[] soundplaying=new int[TOTAL_SOUNDS];

    public static class sa
    {
        public sa(int channel,int num,int looped,int stoppable,int restartable)
        {
            this.channel=channel;
            this.num=num;
            this.looped=looped;
            this.stoppable=stoppable;
            this.restartable=restartable;
        }
        public sa(int channel)
        {
            this.channel=channel;
            this.num=0;
            this.looped=0;
            this.stoppable=0;
            this.restartable=0;
        }
        public int channel;
        public int num;
        public int looped;
        public int stoppable;
        public int restartable;
    };

    static sa sa[] =
    {
        new sa(  0,  0, 1, 1, 1 ),	/* Line  4 - Homing Missile  (channel 1) */
        new sa(  1,  1, 0, 1, 1 ),	/* Line  5 - Base Missile */
        new sa(  2,  2, 1, 1, 1 ),	/* Line  6 - Laser (force field) (channel 1) */
        new sa(  3,  3, 1, 1, 1 ),	/* Line  7 - Battleship (end of level boss) (channel 1) */
        new sa( -1 ),					/* Line  8 - unused */
        new sa( -1 ),					/* Line  9 - unused */
        new sa( -1 ),					/* Line 10 - unused */
        new sa( -1 ),					/* Line 11 - unused */
        new sa(  4,  4, 0, 0, 1 ),	/* Line 12 - S-Exp (enemy explosion) */
        new sa(  5,  5, 0, 0, 0 ),	/* Line 13 - M-Exp (ship explosion) (channel 1) */
        new sa( -1 ),					/* Line 14 - unused */
        new sa(  6,  6, 0, 0, 1 ),	/* Line 15 - Cannon (ship fire) */
        new sa(  7,  7, 0, 0, 1 ),	/* Line 16 - Shot (enemy fire) */
        new sa( -1 ),					/* Line 17 - unused */
        new sa(  8,  8, 0, 0, 1 ),	/* Line 18 - Alarm 2 (target lock) */
        new sa(  9,  9, 0, 0, 0 ),	/* Line 19 - Alarm 3 (low fuel) (channel 1) */
        new sa( -1 ),					/* Line 20 - unused */
        new sa( -1 ),					/* Line 21 - unused */
        new sa( -1 ),					/* Line 22 - unused */
        new sa( -1 ),					/* Line 23 - unused */
        new sa( 10, 10, 1, 1, 1 ),	/* background */
        new sa( 11, 11, 1, 1, 1 ),	/* background */
    };



    public static WriteHandlerPtr zaxxon_sound_w = new WriteHandlerPtr() { public void handler(int offset, int data)
    {
        int line;
        int noise;


        if (offset == 0)
        {
		/* handle background rumble */
            switch (data & 0x0c)
            {
                case 0x04:
                    soundplaying[20] = 0;
                    sample_stop(sa[20].channel);
                    if (soundplaying[21] == 0)
                    {
                        soundplaying[21] = 1;
                        sample_start(sa[21].channel,sa[21].num,sa[21].looped);
                    }
                    sample_set_volume(sa[21].channel,128 + 40 * (data & 0x03));
                    break;
                case 0x00:
                case 0x08:
                    if (soundplaying[20] == 0)
                    {
                        soundplaying[20] = 1;
                        sample_start(sa[20].channel,sa[20].num,sa[20].looped);
                    }
                    sample_set_volume(sa[20].channel,128 + 40 * (data & 0x03));
                    soundplaying[21] = 0;
                    sample_stop(sa[21].channel);
                    break;
                case 0x0c:
                    soundplaying[20] = 0;
                    sample_stop(sa[20].channel);
                    soundplaying[21] = 0;
                    sample_stop(sa[21].channel);
                    break;
            }
        }

        for (line = 0;line < 8;line++)
        {
            noise = 8 * offset + line - 4;

		/* the first four sound lines are handled separately */
            if (noise >= 0)
            {
                if ((data & (1 << line)) == 0)
                {
				/* trigger sound */
                    if (soundplaying[noise] == 0)
                    {
                        soundplaying[noise] = 1;
                        if (sa[noise].channel != -1)
                        {
                            if (sa[noise].restartable!=0 || sample_playing(sa[noise].channel)==0)
                                sample_start(sa[noise].channel,sa[noise].num,sa[noise].looped);
                        }
                    }
                }
                else
                {
                    if (soundplaying[noise]!=0)
                    {
                        soundplaying[noise] = 0;
                        if (sa[noise].channel != -1 && sa[noise].stoppable!=0)
                            sample_stop(sa[noise].channel);
                    }
                }
            }
        }
    }};

}
