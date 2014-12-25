package sound;

public class fmH {
    /*TODO*////* --- select emulation chips --- */
/*TODO*///#define BUILD_YM2203  (HAS_YM2203)		/* build YM2203(OPN)   emulator */
/*TODO*///#define BUILD_YM2608  (HAS_YM2608)		/* build YM2608(OPNA)  emulator */
/*TODO*///#define BUILD_YM2610  (HAS_YM2610)		/* build YM2610(OPNB)  emulator */
/*TODO*///#define BUILD_YM2610B (HAS_YM2610B)		/* build YM2610B(OPNB?)emulator */
/*TODO*///#define BUILD_YM2612  (HAS_YM2612)		/* build YM2612(OPN2)  emulator */
/*TODO*///#define BUILD_YM2151  (HAS_YM2151)		/* build YM2151(OPM)   emulator */
/*TODO*///
/*TODO*////* --- system optimize --- */
/*TODO*////* select stereo output buffer : mixing / separate */
/*TODO*/////#define FM_STEREO_MIX
/*TODO*////* select output size : 8bit or 16bit */
public static final int FM_OUTPUT_BIT= 16;
/*TODO*///
/*TODO*////* --- speed optimize --- */
/*TODO*///#define FM_LFO_SUPPORT 1 	/* support LFO unit */
/*TODO*///#define FM_SEG_SUPPORT 0	/* OPN SSG type envelope support   */
/*TODO*///
/*TODO*////* --- external SSG(YM2149/AY-3-8910)emulator interface port */
/*TODO*////* used by YM2203,YM2608,and YM2610 */
/*TODO*///
/*TODO*////* SSGClk   : Set SSG Clock      */
/*TODO*////* int n    = chip number        */
/*TODO*////* int clk  = MasterClock(Hz)    */
/*TODO*////* int rate = sample rate(Hz) */
/*TODO*///#define SSGClk(chip,clock) AY8910_set_clock(chip,clock)
/*TODO*///
/*TODO*////* SSGWrite : Write SSG port     */
/*TODO*////* int n    = chip number        */
/*TODO*////* int a    = address            */
/*TODO*////* int v    = data               */
/*TODO*///#define SSGWrite(n,a,v) AY8910Write(n,a,v)
/*TODO*///
/*TODO*////* SSGRead  : Read SSG port */
/*TODO*////* int n    = chip number   */
/*TODO*////* return   = Read data     */
/*TODO*///#define SSGRead(n) AY8910Read(n)
/*TODO*///
/*TODO*////* SSGReset : Reset SSG chip */
/*TODO*////* int n    = chip number   */
/*TODO*///#define SSGReset(chip) AY8910_reset(chip)
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
/*TODO*///
/*TODO*///#ifdef FM_STEREO_MIX
/*TODO*///  #define YM2151_NUMBUF 1
/*TODO*///  #define YM2608_NUMBUF 1
/*TODO*///  #define YM2612_NUMBUF 1
/*TODO*///  #define YM2610_NUMBUF 1
/*TODO*///#else
/*TODO*///  #define YM2151_NUMBUF 2    /* FM L+R */
/*TODO*///  #define YM2608_NUMBUF 2    /* FM L+R+ADPCM+RYTHM */
/*TODO*///  #define YM2610_NUMBUF 2    /* FM L+R+ADPCMA+ADPCMB */
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
/*TODO*///

    public static abstract interface FM_TIMERHANDLERtr {

        public abstract void handler(int n, int c, int count, double stepTime);
    }

    public static abstract interface FM_IRQHANDLEPtr {

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
}
