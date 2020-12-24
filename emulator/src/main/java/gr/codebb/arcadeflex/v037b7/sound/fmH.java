/**
 * ported to v0.37b7
 *
 */
package gr.codebb.arcadeflex.v037b7.sound;

import static gr.codebb.arcadeflex.v036.sound.ay8910.*;

public class fmH {

    /*TODO*////* --- select emulation chips --- */
/*TODO*///#define BUILD_YM2203  (HAS_YM2203)		/* build YM2203(OPN)   emulator */
/*TODO*///#define BUILD_YM2608  (HAS_YM2608)		/* build YM2608(OPNA)  emulator */
/*TODO*///#define BUILD_YM2610  (HAS_YM2610)		/* build YM2610(OPNB)  emulator */
/*TODO*///#define BUILD_YM2610B (HAS_YM2610B)		/* build YM2610B(OPNB?)emulator */
/*TODO*///#define BUILD_YM2612  (HAS_YM2612 || HAS_YM3438)		/* build YM2612(OPN2)  emulator */
/*TODO*///#define BUILD_YM2151  (HAS_YM2151)		/* build YM2151(OPM)   emulator */
/*TODO*///
/*TODO*////* --- system optimize --- */
/*TODO*////* select stereo output buffer : 1=mixing / 0=separate */
/*TODO*///#define FM_STEREO_MIX 0
    /* select bit size of output : 8 or 16 */
    public static final int FM_OUTPUT_BIT = 16;

    /*TODO*////* select timer system internal or external */
/*TODO*///#define FM_INTERNAL_TIMER 0
/*TODO*///
/*TODO*////* --- speedup optimize --- */
/*TODO*////* support LFO unit */
/*TODO*///#define FM_LFO_SUPPORT 1
/*TODO*////* support OPN SSG type envelope mode */
/*TODO*///#define FM_SEG_SUPPORT 0
/*TODO*///
    /* --- external SSG(YM2149/AY-3-8910)emulator interface port */
 /* used by YM2203,YM2608,and YM2610 */

 /* SSGClk   : Set SSG Clock      */
 /* int n    = chip number        */
 /* int clk  = MasterClock(Hz)    */
 /* int rate = sample rate(Hz) */
    public static void SSGClk(int chip, int clock) {
        AY8910_set_clock(chip, clock);
    }

    /* SSGWrite : Write SSG port     */
 /* int n    = chip number        */
 /* int a    = address            */
 /* int v    = data               */
    public static void SSGWrite(int n, int a, int v) {
        AY8910Write(n, a, v);
    }

    /* SSGRead  : Read SSG port */
 /* int n    = chip number   */
 /* return   = Read data     */
    public static int SSGRead(int n) {
        return AY8910Read(n);
    }

    /* SSGReset : Reset SSG chip */
 /* int n    = chip number   */
    public static void SSGReset(int chip) {
        AY8910_reset(chip);
    }

    /*TODO*///
/*TODO*////* --- external callback funstions for realtime update --- */
/*TODO*///#if BUILD_YM2203
/*TODO*///  /* in 2203intf.c */
/*TODO*///  #define YM2203UpdateReq(chip) YM2203UpdateRequest(chip)
/*TODO*///#endif
/*TODO*///#if BUILD_YM2608
/*TODO*///  /* in 2608intf.c */
/*TODO*///  #define YM2608UpdateReq(chip) YM2608UpdateRequest(chip);
/*TODO*///#endif
/*TODO*///#if BUILD_YM2610
/*TODO*///  /* in 2610intf.c */
/*TODO*///  #define YM2610UpdateReq(chip) YM2610UpdateRequest(chip);
/*TODO*///#endif
/*TODO*///#if BUILD_YM2612
/*TODO*///  /* in 2612intf.c */
/*TODO*///  #define YM2612UpdateReq(chip) YM2612UpdateRequest(chip);
/*TODO*///#endif
/*TODO*///#if BUILD_YM2151
/*TODO*///  /* in 2151intf.c */
/*TODO*///  #define YM2151UpdateReq(chip) YM2151UpdateRequest(chip);
/*TODO*///#endif
/*TODO*///
/*TODO*////* compiler dependence */
/*TODO*///#ifndef OSD_CPU_H
/*TODO*///#define OSD_CPU_H
/*TODO*///typedef unsigned char	UINT8;   /* unsigned  8bit */
/*TODO*///typedef unsigned short	UINT16;  /* unsigned 16bit */
/*TODO*///typedef unsigned int	UINT32;  /* unsigned 32bit */
/*TODO*///typedef signed char		INT8;    /* signed  8bit   */
/*TODO*///typedef signed short	INT16;   /* signed 16bit   */
/*TODO*///typedef signed int		INT32;   /* signed 32bit   */
/*TODO*///#endif
/*TODO*///
/*TODO*///#define YM2203_NUMBUF 1
/*TODO*///#if FM_STEREO_MIX
/*TODO*///  #define YM2151_NUMBUF 1
/*TODO*///  #define YM2608_NUMBUF 1
/*TODO*///  #define YM2612_NUMBUF 1
/*TODO*///  #define YM2610_NUMBUF 1
/*TODO*///#else
/*TODO*///  #define YM2151_NUMBUF 2    /* FM L+R */
    public static final int YM2608_NUMBUF = 2;
    /* FM L+R+ADPCM+RYTHM */
    public static final int YM2610_NUMBUF = 2;/* FM L+R+ADPCMA+ADPCMB */
 /*TODO*///  #define YM2612_NUMBUF 2    /* FM L+R */
/*TODO*///#endif
/*TODO*///
/*TODO*///#if (FM_OUTPUT_BIT==16)
/*TODO*///typedef INT16 FMSAMPLE;
/*TODO*///typedef unsigned long FMSAMPLE_MIX;
/*TODO*///#endif
/*TODO*///#if (FM_OUTPUT_BIT==8)
/*TODO*///typedef unsigned char  FMSAMPLE;
/*TODO*///typedef unsigned short FMSAMPLE_MIX;
/*TODO*///#endif
    public static abstract interface FM_TIMERHANDLER_Ptr {

        public abstract void handler(int n, int c, double count, double stepTime);
    }

    public static abstract interface FM_IRQHANDLER_Ptr {

        public abstract void handler(int n, int irq);
    }
    /* FM_TIMERHANDLER : Stop or Start timer         */
 /* int n          = chip number                  */
 /* int c          = Channel 0=TimerA,1=TimerB    */
 /* int count      = timer count (0=stop)         */
 /* doube stepTime = step time of one count (sec.)*/

 /* FM_IRQHHANDLER : IRQ level changing sense     */
 /* int n       = chip number                     */
 /* int irq     = IRQ level 0=OFF,1=ON            */

 /*TODO*///
/*TODO*///#if BUILD_YM2608
/*TODO*////* -------------------- YM2608(OPNA) Interface -------------------- */
/*TODO*///int YM2608Init(int num, int baseclock, int rate,
/*TODO*///               void **pcmroma,int *pcmsizea,short *rhythmrom,int *rhythmpos,
/*TODO*///               FM_TIMERHANDLER TimerHandler,FM_IRQHANDLER IRQHandler);
/*TODO*///void YM2608Shutdown(void);
/*TODO*///void YM2608ResetChip(int num);
/*TODO*///void YM2608UpdateOne(int num, INT16 **buffer, int length);
/*TODO*///
/*TODO*///int YM2608Write(int n, int a,unsigned char v);
/*TODO*///unsigned char YM2608Read(int n,int a);
/*TODO*///int YM2608TimerOver(int n, int c );
/*TODO*///#endif /* BUILD_YM2608 */
/*TODO*///
/*TODO*///#if (BUILD_YM2610||BUILD_YM2610B)
/*TODO*////* -------------------- YM2610(OPNB) Interface -------------------- */
/*TODO*///int YM2610Init(int num, int baseclock, int rate,
/*TODO*///               void **pcmroma,int *pcmasize,void **pcmromb,int *pcmbsize,
/*TODO*///               FM_TIMERHANDLER TimerHandler,FM_IRQHANDLER IRQHandler);
/*TODO*///void YM2610Shutdown(void);
/*TODO*///void YM2610ResetChip(int num);
/*TODO*///void YM2610UpdateOne(int num, INT16 **buffer, int length);
/*TODO*///#if BUILD_YM2610B
/*TODO*///void YM2610BUpdateOne(int num, INT16 **buffer, int length);
/*TODO*///#endif
/*TODO*///
/*TODO*///int YM2610Write(int n, int a,unsigned char v);
/*TODO*///unsigned char YM2610Read(int n,int a);
/*TODO*///int YM2610TimerOver(int n, int c );
/*TODO*///
/*TODO*///#endif /* BUILD_YM2610 */
/*TODO*///
/*TODO*///#if BUILD_YM2612
/*TODO*///int YM2612Init(int num, int baseclock, int rate,
/*TODO*///               FM_TIMERHANDLER TimerHandler,FM_IRQHANDLER IRQHandler);
/*TODO*///void YM2612Shutdown(void);
/*TODO*///void YM2612ResetChip(int num);
/*TODO*///void YM2612UpdateOne(int num, INT16 **buffer, int length);
/*TODO*///int YM2612Write(int n, int a,unsigned char v);
/*TODO*///unsigned char YM2612Read(int n,int a);
/*TODO*///int YM2612TimerOver(int n, int c );
/*TODO*///
/*TODO*///#endif /* BUILD_YM2612 */
/*TODO*///
/*TODO*///#if BUILD_YM2151
/*TODO*////* -------------------- YM2151(OPM) Interface -------------------- */
/*TODO*///int OPMInit(int num, int baseclock, int rate,
/*TODO*///               FM_TIMERHANDLER TimerHandler,FM_IRQHANDLER IRQHandler);
/*TODO*///void OPMShutdown(void);
/*TODO*///void OPMResetChip(int num);
/*TODO*///
/*TODO*///void OPMUpdateOne(int num, INT16 **buffer, int length );
/*TODO*////* ---- set callback hander when port CT0/1 write ----- */
/*TODO*////* CT.bit0 = CT0 , CT.bit1 = CT1 */
/*TODO*////*
/*TODO*///typedef void (*mem_write_handler)(int offset,int data);
/*TODO*///*/
/*TODO*///void OPMSetPortHander(int n,mem_write_handler PortWrite);
/*TODO*////* JB 981119  - so it will match MAME's memory write functions scheme*/
/*TODO*///
/*TODO*///int YM2151Write(int n,int a,unsigned char v);
/*TODO*///unsigned char YM2151Read(int n,int a);
/*TODO*///int YM2151TimerOver(int n,int c);
/*TODO*///#endif /* BUILD_YM2151 */
/*TODO*///
/*TODO*///#endif /* _H_FM_FM_ */
/*TODO*///    
}
