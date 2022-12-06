/*
 * ported to v0.37b7
 * 
 */
package gr.codebb.arcadeflex.v037b7.sound;

import arcadeflex.v036.generic.funcPtr.TimerCallbackHandlerPtr;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static common.libc.cstdio.sprintf;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.sound.streams.*;
import static arcadeflex.v036.mame.timer.*;
import static gr.codebb.arcadeflex.v037b7.sound._2610intfH.*;
import static gr.codebb.arcadeflex.v037b7.sound.fm.*;
import static gr.codebb.arcadeflex.v037b7.sound.fmH.*;


public class _2610intf extends snd_interface {

    /* use FM.C with stream system */
    static int[] stream = new int[MAX_2610];

    /* Global Interface holder */
    static YM2610interface intf;

    static Object[][] Timer = new Object[MAX_2610][];

    public _2610intf() {
        this.name = "YM-2610";
        this.sound_num = SOUND_YM2610;
        for (int i = 0; i < MAX_2610; i++) {
            Timer[i] = new Object[2];
        }
    }

    @Override
    public int chips_num(MachineSound msound) {
        return ((YM2610interface) msound.sound_interface).num;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return ((YM2610interface) msound.sound_interface).baseclock;
    }


    /* IRQ Handler */
    public static FM_IRQHANDLER_Ptr IRQHandler = new FM_IRQHANDLER_Ptr() {

        @Override
        public void handler(int n, int irq) {
            if (intf.YM2203_handler == null) {
                return;
            }
            if (intf.YM2203_handler[n] != null) {
                intf.YM2203_handler[n].handler(irq);
            }
        }
    };
    /* Timer overflow callback from timer.c */
    public static TimerCallbackHandlerPtr TimerCallbackHandlerPtr_2610 = new TimerCallbackHandlerPtr() {
        public void handler(int param) {
            int n = param & 0x7f;
            int c = param >> 7;

//	if(errorlog) fprintf(errorlog,"2610 TimerOver %d\n",c);
            Timer[n][c] = null;
            YM2610TimerOver(n, c);
        }
    };
    /* TimerHandler from fm.c */
    public static FM_TIMERHANDLER_Ptr TimerHandler = new FM_TIMERHANDLER_Ptr() {

        @Override
        public void handler(int n, int c, double count, double stepTime) {
            if (count == 0) {
                /* Reset FM Timer */

                if (Timer[n][c] != null) {
//			if(errorlog) fprintf(errorlog,"2610 TimerReset %d\n",c);
                    timer_remove(Timer[n][c]);
                    Timer[n][c] = null;
                }
            } else {
                /* Start FM Timer */

                double timeSec = (double) count * stepTime;

                if (Timer[n][c] == null) {
                    Timer[n][c] = timer_set(timeSec, (c << 7) | n, TimerCallbackHandlerPtr_2610);
                }
            }
        }
    };

    static void FMTimerInit() {
        for (int i = 0; i < MAX_2610; i++) {
            Timer[i][0] = Timer[i][1] = null;
        }
    }

    /* update request from fm.c */
    public static void YM2610UpdateRequest(int chip) {
        stream_update(stream[chip], 100);
    }

    @Override
    public int start(MachineSound msound) {
        int a, j;
        int rate = Machine.sample_rate;
        //char buf[YM2610_NUMBUF][40];
        String[] name = new String[YM2610_NUMBUF];
        int mixed_vol;
        int[] vol = new int[YM2610_NUMBUF];
        UBytePtr[] pcmbufa = new UBytePtr[YM2610_NUMBUF];
        UBytePtr[] pcmbufb = new UBytePtr[YM2610_NUMBUF];
        int[] pcmsizea = new int[YM2610_NUMBUF];
        int[] pcmsizeb = new int[YM2610_NUMBUF];

        for (int k = 0; k < YM2610_NUMBUF; k++) {
            pcmbufa[k] = new UBytePtr();
            pcmbufb[k] = new UBytePtr();
        }
        intf = (YM2610interface) msound.sound_interface;
        if (intf.num > MAX_2610) {
            return 1;
        }

        if (sndintf[SOUND_AY8910].start(msound) != 0) {
            return 1;
        }

        /* Timer Handler set */
        FMTimerInit();

        /* stream system initialize */
        for (a = 0; a < intf.num; a++) {
            /* stream setup */
            mixed_vol = intf.volumeFM[a];
            /* stream setup */
            for (j = 0; j < YM2610_NUMBUF; j++) {
                //name[j]=buf[j];
                vol[j] = mixed_vol & 0xffff;
                mixed_vol >>= 16;
                name[j] = sprintf("%s #%d Ch%d", sound_name(msound), a, j + 1);
            }
            stream[a] = stream_init_multi(YM2610_NUMBUF, name, vol, rate, a, YM2610UpdateOne);
            /* setup adpcm buffers */
            pcmbufa[a] = memory_region(intf.pcmroma[a]);
            pcmsizea[a] = memory_region_length(intf.pcmroma[a]);
            pcmbufb[a] = memory_region(intf.pcmromb[a]);
            pcmsizeb[a] = memory_region_length(intf.pcmromb[a]);
        }

        /**
         * ** initialize YM2610 ***
         */
        if (YM2610Init(intf.num, intf.baseclock, rate,
                pcmbufa, pcmsizea, pcmbufb, pcmsizeb,
                TimerHandler, IRQHandler) == 0) {
            return 0;
        }

        /* error */
        return 1;
    }

    /*TODO*///int YM2610B_sh_start(const struct MachineSound *msound)
/*TODO*///{
/*TODO*///	int i,j;
/*TODO*///	int rate = Machine->sample_rate;
/*TODO*///	char buf[YM2610_NUMBUF][40];
/*TODO*///	const char *name[YM2610_NUMBUF];
/*TODO*///	int mixed_vol,vol[YM2610_NUMBUF];
/*TODO*///	void *pcmbufa[YM2610_NUMBUF],*pcmbufb[YM2610_NUMBUF];
/*TODO*///	int  pcmsizea[YM2610_NUMBUF],pcmsizeb[YM2610_NUMBUF];
/*TODO*///
/*TODO*///	intf = msound->sound_interface;
/*TODO*///	if( intf->num > MAX_2610 ) return 1;
/*TODO*///
/*TODO*///	if (AY8910_sh_start(msound)) return 1;
/*TODO*///
/*TODO*///	/* Timer Handler set */
/*TODO*///	FMTimerInit();
/*TODO*///
/*TODO*///	/* stream system initialize */
/*TODO*///	for (i = 0;i < intf->num;i++)
/*TODO*///	{
/*TODO*///		/* stream setup */
/*TODO*///		mixed_vol = intf->volumeFM[i];
/*TODO*///		/* stream setup */
/*TODO*///		for (j = 0 ; j < YM2610_NUMBUF ; j++)
/*TODO*///		{
/*TODO*///			name[j]=buf[j];
/*TODO*///			vol[j] = mixed_vol & 0xffff;
/*TODO*///			mixed_vol>>=16;
/*TODO*///			sprintf(buf[j],"%s #%d Ch%d",sound_name(msound),i,j+1);
/*TODO*///		}
/*TODO*///		stream[i] = stream_init_multi(YM2610_NUMBUF,name,vol,rate,i,YM2610BUpdateOne);
/*TODO*///		/* setup adpcm buffers */
/*TODO*///		pcmbufa[i]  = (void *)(memory_region(intf->pcmroma[i]));
/*TODO*///		pcmsizea[i] = memory_region_length(intf->pcmroma[i]);
/*TODO*///		pcmbufb[i]  = (void *)(memory_region(intf->pcmromb[i]));
/*TODO*///		pcmsizeb[i] = memory_region_length(intf->pcmromb[i]);
/*TODO*///	}
/*TODO*///
/*TODO*///	/**** initialize YM2610 ****/
/*TODO*///	if (YM2610Init(intf->num,intf->baseclock,rate,
/*TODO*///		           pcmbufa,pcmsizea,pcmbufb,pcmsizeb,
/*TODO*///		           TimerHandler,IRQHandler) == 0)
/*TODO*///		return 0;
/*TODO*///
/*TODO*///	/* error */
/*TODO*///	return 1;
/*TODO*///}

    @Override
    public void stop() {
        YM2610Shutdown();
    }

    @Override
    public void update() {
        //no functionality expected
    }

    @Override
    public void reset() {
        int i;

        for (i = 0; i < intf.num; i++) {
            YM2610ResetChip(i);
        }
    }
    /**
     * *********************************************
     */
    /* Status Read for YM2610 - Chip 0				*/
    /**
     * *********************************************
     */
    public static ReadHandlerPtr YM2610_status_port_0_A_r = new ReadHandlerPtr() {
        public int handler(int offset) {
//if(errorlog) fprintf(errorlog,"PC %04x: 2610 S0A=%02X\n",cpu_get_pc(),YM2610Read(0,0));
            return YM2610Read(0, 0);
        }
    };
    public static ReadHandlerPtr YM2610_status_port_0_B_r = new ReadHandlerPtr() {
        public int handler(int offset) {
//if(errorlog) fprintf(errorlog,"PC %04x: 2610 S0B=%02X\n",cpu_get_pc(),YM2610Read(0,2));
            return YM2610Read(0, 2);
        }
    };

    /**
     * *********************************************
     */
    /* Status Read for YM2610 - Chip 1				*/
    /**
     * *********************************************
     */
    public static ReadHandlerPtr YM2610_status_port_1_A_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return YM2610Read(1, 0);
        }
    };

    public static ReadHandlerPtr YM2610_status_port_1_B_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return YM2610Read(1, 2);
        }
    };

    /**
     * *********************************************
     */
    /* Port Read for YM2610 - Chip 0				*/
    /**
     * *********************************************
     */
    public static ReadHandlerPtr YM2610_read_port_0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return YM2610Read(0, 1);
        }
    };

    /**
     * *********************************************
     */
    /* Port Read for YM2610 - Chip 1				*/
    /**
     * *********************************************
     */
    public static ReadHandlerPtr YM2610_read_port_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return YM2610Read(1, 1);
        }
    };

    /**
     * *********************************************
     */
    /* Control Write for YM2610 - Chip 0			*/
 /* Consists of 2 addresses						*/
    /**
     * *********************************************
     */
    public static WriteHandlerPtr YM2610_control_port_0_A_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
//if(errorlog) fprintf(errorlog,"PC %04x: 2610 Reg A %02X",cpu_get_pc(),data);
            YM2610Write(0, 0, data);
        }
    };

    public static WriteHandlerPtr YM2610_control_port_0_B_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
//if(errorlog) fprintf(errorlog,"PC %04x: 2610 Reg B %02X",cpu_get_pc(),data);
            YM2610Write(0, 2, data);
        }
    };

    /**
     * *********************************************
     */
    /* Control Write for YM2610 - Chip 1			*/
 /* Consists of 2 addresses						*/
    /**
     * *********************************************
     */
    public static WriteHandlerPtr YM2610_control_port_1_A_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            YM2610Write(1, 0, data);
        }
    };

    public static WriteHandlerPtr YM2610_control_port_1_B_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            YM2610Write(1, 2, data);
        }
    };

    /**
     * *********************************************
     */
    /* Data Write for YM2610 - Chip 0				*/
 /* Consists of 2 addresses						*/
    /**
     * *********************************************
     */
    public static WriteHandlerPtr YM2610_data_port_0_A_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
//if(errorlog) fprintf(errorlog," =%02X\n",data);
            YM2610Write(0, 1, data);
        }
    };

    public static WriteHandlerPtr YM2610_data_port_0_B_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
//if(errorlog) fprintf(errorlog," =%02X\n",data);
            YM2610Write(0, 3, data);
        }
    };

    /**
     * *********************************************
     */
    /* Data Write for YM2610 - Chip 1				*/
 /* Consists of 2 addresses						*/
    /**
     * *********************************************
     */
    public static WriteHandlerPtr YM2610_data_port_1_A_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            YM2610Write(1, 1, data);
        }
    };
    public static WriteHandlerPtr YM2610_data_port_1_B_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            YM2610Write(1, 3, data);
        }
    };
}
