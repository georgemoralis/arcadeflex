/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sound;

/**
 *
 * @author shadow
 */
public class fmoplH {
    /*TODO*///#define BUILD_YM3812 (HAS_YM3812)
    /*TODO*///#define BUILD_YM3526 (HAS_YM3526)
    /*TODO*///#define BUILD_Y8950  (HAS_Y8950)
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
    /*TODO*///#if BUILD_Y8950
    /*TODO*///#include "ymdeltat.h"
    /*TODO*///#endif
    /*TODO*///
         public static abstract interface OPL_TIMERHANDLERPtr { public abstract void handler(int channel,double interval_Sec); }
         public static abstract interface OPL_IRQHANDLERPtr { public abstract void handler(int param,int irq); }
         public static abstract interface OPL_UPDATEHANDLERPtr { public abstract void handler(int param,int min_interval_us); }
    /*TODO*///typedef void (*OPL_PORTHANDLER_W)(int param,unsigned char data);
    /*TODO*///typedef unsigned char (*OPL_PORTHANDLER_R)(int param);
    /*TODO*///
    /* !!!!! here is private section , do not access there member direct !!!!! */
    public static final int OPL_TYPE_WAVESEL   =0x01;  /* waveform select    */
    public static final int OPL_TYPE_ADPCM     =0x02; /* DELTA-T ADPCM unit */
    public static final int OPL_TYPE_KEYBOARD  =0x04;  /* keyboard interface */
    public static final int OPL_TYPE_IO        =0x08;  /* I/O port */
    /*TODO*///
    /*TODO*////* ---------- OPL one of slot  ---------- */
    /*TODO*///typedef struct fm_opl_slot {
    /*TODO*///	INT32 TL;		/* total level     :TL << 8            */
    /*TODO*///	INT32 TLL;		/* adjusted now TL                     */
    /*TODO*///	UINT8  KSR;		/* key scale rate  :(shift down bit)   */
    /*TODO*///	INT32 *AR;		/* attack rate     :&AR_TABLE[AR<<2]   */
    /*TODO*///	INT32 *DR;		/* decay rate      :&DR_TALBE[DR<<2]   */
    /*TODO*///	INT32 SL;		/* sustin level    :SL_TALBE[SL]       */
    /*TODO*///	INT32 *RR;		/* release rate    :&DR_TABLE[RR<<2]   */
    /*TODO*///	UINT8 ksl;		/* keyscale level  :(shift down bits)  */
    /*TODO*///	UINT8 ksr;		/* key scale rate  :kcode>>KSR         */
    /*TODO*///	UINT32 mul;		/* multiple        :ML_TABLE[ML]       */
    /*TODO*///	UINT32 Cnt;		/* frequency count :                   */
    /*TODO*///	UINT32 Incr;	/* frequency step  :                   */
    /*TODO*///	/* envelope generator state */
    /*TODO*///	UINT8 eg_typ;	/* envelope type flag                  */
    /*TODO*///	UINT8 evm;		/* envelope phase                      */
    /*TODO*///	INT32 evc;		/* envelope counter                    */
    /*TODO*///	INT32 eve;		/* envelope counter end point          */
    /*TODO*///	INT32 evs;		/* envelope counter step               */
    /*TODO*///	INT32 evsa;	/* envelope step for AR :AR[ksr]       */
    /*TODO*///	INT32 evsd;	/* envelope step for DR :DR[ksr]       */
    /*TODO*///	INT32 evsr;	/* envelope step for RR :RR[ksr]       */
    /*TODO*///	/* LFO */
    /*TODO*///	UINT8 ams;		/* ams flag                            */
    /*TODO*///	UINT8 vib;		/* vibrate flag                        */
    /*TODO*///	/* wave selector */
    /*TODO*///	INT32 **wavetable;
    /*TODO*///}OPL_SLOT;
    /*TODO*///
    /*TODO*////* ---------- OPL one of channel  ---------- */
    /*TODO*///typedef struct fm_opl_channel {
    /*TODO*///	OPL_SLOT SLOT[2];
    /*TODO*///	UINT8 CON;			/* connection type                     */
    /*TODO*///	UINT8 FB;			/* feed back       :(shift down bit)   */
    /*TODO*///	INT32 *connect1;	/* slot1 output pointer                */
    /*TODO*///	INT32 *connect2;	/* slot2 output pointer                */
    /*TODO*///	INT32 op1_out[2];	/* slot1 output for selfeedback        */
    /*TODO*///	/* phase generator state */
    /*TODO*///	UINT32  block_fnum;	/* block+fnum      :                   */
    /*TODO*///	UINT8 kcode;		/* key code        : KeyScaleCode      */
    /*TODO*///	UINT32  fc;			/* Freq. Increment base                */
    /*TODO*///	UINT32  ksl_base;	/* KeyScaleLevel Base step             */
    /*TODO*///	UINT8 keyon;		/* key on/off flag                     */
    /*TODO*///} OPL_CH;
    /*TODO*///
    /*TODO*////* OPL state */

    /* ---------- Generic interface section ---------- */
    public static final int OPL_TYPE_YM3526= (0);
    public static final int OPL_TYPE_YM3812= (OPL_TYPE_WAVESEL);
    public static final int OPL_TYPE_Y8950 = (OPL_TYPE_ADPCM|OPL_TYPE_KEYBOARD|OPL_TYPE_IO);   
}
