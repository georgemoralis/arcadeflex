package sound;


public class fm {
/*TODO*///#define YM2610B_WARNING
/*TODO*///
/*TODO*////* YM2608 rhythm data is PCM ,not an ADPCM */
/*TODO*///#define YM2608_RHYTHM_PCM
/*TODO*///
/*TODO*///
/*TODO*////***** shared function building option ****/
/*TODO*///#define BUILD_OPN (BUILD_YM2203||BUILD_YM2608||BUILD_YM2610||BUILD_YM2612)
/*TODO*///#define BUILD_OPNB (BUILD_YM2610||BUILD_YM2610B)
/*TODO*///#define BUILD_FM_ADPCMA (BUILD_YM2608||BUILD_OPNB)
/*TODO*///#define BUILD_FM_ADPCMB (BUILD_YM2608||BUILD_OPNB)
/*TODO*///#define BUILD_STEREO (BUILD_YM2608||BUILD_YM2610||BUILD_YM2612||BUILD_YM2151)
/*TODO*///#define BUILD_LFO (BUILD_YM2608||BUILD_YM2610||BUILD_YM2612||BUILD_YM2151)
/*TODO*///
/*TODO*///#if BUILD_FM_ADPCMB
/*TODO*////* include external DELTA-T ADPCM unit */
/*TODO*///  #include "ymdeltat.h"		/* DELTA-T ADPCM UNIT */
/*TODO*///  #define DELTAT_MIXING_LEVEL (4) /* DELTA-T ADPCM MIXING LEVEL */
/*TODO*///#endif
/*TODO*///
/*TODO*////* ------------------------------------------------------------------ */
/*TODO*///#ifdef __RAINE__
/*TODO*///#define INTERNAL_TIMER 			/* use internal timer */
/*TODO*///#endif
/*TODO*///
/*TODO*////* -------------------- sound quality define selection --------------------- */
/*TODO*////* sinwave entries */
/*TODO*////* used static memory = SIN_ENT * 4 (byte) */
/*TODO*///#define SIN_ENT 2048
/*TODO*///
/*TODO*////* lower bits of envelope counter */
/*TODO*///#define ENV_BITS 16
/*TODO*///
/*TODO*////* envelope output entries */
/*TODO*///#define EG_ENT   4096
/*TODO*///#define EG_STEP (96.0/EG_ENT) /* OPL == 0.1875 dB */
/*TODO*///
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*////* LFO table entries */
/*TODO*///#define LFO_ENT 512
/*TODO*///#define LFO_SHIFT (32-9)
/*TODO*///#define LFO_RATE 0x10000
/*TODO*///#endif
/*TODO*///
/*TODO*////* -------------------- preliminary define section --------------------- */
/*TODO*////* attack/decay rate time rate */
/*TODO*///#define OPM_ARRATE     399128
/*TODO*///#define OPM_DRRATE    5514396
/*TODO*////* It is not checked , because I haven't YM2203 rate */
/*TODO*///#define OPN_ARRATE  OPM_ARRATE
/*TODO*///#define OPN_DRRATE  OPM_DRRATE
/*TODO*///
/*TODO*////* PG output cut off level : 78dB(14bit)? */
/*TODO*///#define PG_CUT_OFF ((int)(78.0/EG_STEP))
/*TODO*////* EG output cut off level : 68dB? */
/*TODO*///#define EG_CUT_OFF ((int)(68.0/EG_STEP))
/*TODO*///
/*TODO*///#define FREQ_BITS 24		/* frequency turn          */
/*TODO*///
/*TODO*////* PG counter is 21bits @oct.7 */
/*TODO*///#define FREQ_RATE   (1<<(FREQ_BITS-21))
/*TODO*///#define TL_BITS    (FREQ_BITS+2)
/*TODO*////* OPbit = 14(13+sign) : TL_BITS+1(sign) / output = 16bit */
/*TODO*///#define TL_SHIFT (TL_BITS+1-(14-16))
/*TODO*///
/*TODO*////* output final shift */
/*TODO*///#define FM_OUTSB  (TL_SHIFT-FM_OUTPUT_BIT)
/*TODO*///#define FM_MAXOUT ((1<<(TL_SHIFT-1))-1)
/*TODO*///#define FM_MINOUT (-(1<<(TL_SHIFT-1)))
/*TODO*///
/*TODO*////* -------------------- local defines , macros --------------------- */
/*TODO*///
/*TODO*////* envelope counter position */
/*TODO*///#define EG_AST   0					/* start of Attack phase */
/*TODO*///#define EG_AED   (EG_ENT<<ENV_BITS)	/* end   of Attack phase */
/*TODO*///#define EG_DST   EG_AED				/* start of Decay/Sustain/Release phase */
/*TODO*///#define EG_DED   (EG_DST+((EG_ENT-1)<<ENV_BITS))	/* end   of Decay/Sustain/Release phase */
/*TODO*///#define EG_OFF   EG_DED				/* off */
/*TODO*///#if FM_SEG_SUPPORT
/*TODO*///#define EG_UST   ((2*EG_ENT)<<ENV_BITS)  /* start of SEG UPSISE */
/*TODO*///#define EG_UED   ((3*EG_ENT)<<ENV_BITS)  /* end of SEG UPSISE */
/*TODO*///#endif
/*TODO*///
/*TODO*////* register number to channel number , slot offset */
/*TODO*///#define OPN_CHAN(N) (N&3)
/*TODO*///#define OPN_SLOT(N) ((N>>2)&3)
/*TODO*///#define OPM_CHAN(N) (N&7)
/*TODO*///#define OPM_SLOT(N) ((N>>3)&3)
/*TODO*////* slot number */
/*TODO*///#define SLOT1 0
/*TODO*///#define SLOT2 2
/*TODO*///#define SLOT3 1
/*TODO*///#define SLOT4 3
/*TODO*///
/*TODO*////* bit0 = Right enable , bit1 = Left enable */
/*TODO*///#define OUTD_RIGHT  1
/*TODO*///#define OUTD_LEFT   2
/*TODO*///#define OUTD_CENTER 3
/*TODO*///
/*TODO*////* FM timer model */
/*TODO*///#define FM_TIMER_SINGLE (0)
/*TODO*///#define FM_TIMER_INTERVAL (1)
/*TODO*///
/*TODO*////* ---------- OPN / OPM one channel  ---------- */
/*TODO*///typedef struct fm_slot {
/*TODO*///	INT32 *DT;			/* detune          :DT_TABLE[DT]       */
/*TODO*///	int DT2;			/* multiple,Detune2:(DT2<<4)|ML for OPM*/
/*TODO*///	int TL;				/* total level     :TL << 8            */
/*TODO*///	UINT8 KSR;			/* key scale rate  :3-KSR              */
/*TODO*///	const INT32 *AR;	/* attack rate     :&AR_TABLE[AR<<1]   */
/*TODO*///	const INT32 *DR;	/* decay rate      :&DR_TABLE[DR<<1]   */
/*TODO*///	const INT32 *SR;	/* sustin rate     :&DR_TABLE[SR<<1]   */
/*TODO*///	int   SL;			/* sustin level    :SL_TABLE[SL]       */
/*TODO*///	const INT32 *RR;	/* release rate    :&DR_TABLE[RR<<2+2] */
/*TODO*///	UINT8 SEG;			/* SSG EG type     :SSGEG              */
/*TODO*///	UINT8 ksr;			/* key scale rate  :kcode>>(3-KSR)     */
/*TODO*///	UINT32 mul;			/* multiple        :ML_TABLE[ML]       */
/*TODO*///	/* Phase Generator */
/*TODO*///	UINT32 Cnt;			/* frequency count :                   */
/*TODO*///	UINT32 Incr;		/* frequency step  :                   */
/*TODO*///	/* Envelope Generator */
/*TODO*///	void (*eg_next)(struct fm_slot *SLOT);	/* pointer of phase handler */
/*TODO*///	INT32 evc;			/* envelope counter                    */
/*TODO*///	INT32 eve;			/* envelope counter end point          */
/*TODO*///	INT32 evs;			/* envelope counter step               */
/*TODO*///	INT32 evsa;			/* envelope step for Attack            */
/*TODO*///	INT32 evsd;			/* envelope step for Decay             */
/*TODO*///	INT32 evss;			/* envelope step for Sustain           */
/*TODO*///	INT32 evsr;			/* envelope step for Release           */
/*TODO*///	INT32 TLL;			/* adjusted TotalLevel                 */
/*TODO*///	/* LFO */
/*TODO*///	UINT8 amon;			/* AMS enable flag              */
/*TODO*///	UINT32 ams;			/* AMS depth level of this SLOT */
/*TODO*///}FM_SLOT;
/*TODO*///
/*TODO*///typedef struct fm_chan {
/*TODO*///	FM_SLOT	SLOT[4];
/*TODO*///	UINT8 PAN;			/* PAN :NONE,LEFT,RIGHT or CENTER */
/*TODO*///	UINT8 ALGO;			/* Algorythm                      */
/*TODO*///	UINT8 FB;			/* shift count of self feed back  */
/*TODO*///	INT32 op1_out[2];	/* op1 output for beedback        */
/*TODO*///	/* Algorythm (connection) */
/*TODO*///	INT32 *connect1;		/* pointer of SLOT1 output    */
/*TODO*///	INT32 *connect2;		/* pointer of SLOT2 output    */
/*TODO*///	INT32 *connect3;		/* pointer of SLOT3 output    */
/*TODO*///	INT32 *connect4;		/* pointer of SLOT4 output    */
/*TODO*///	/* LFO */
/*TODO*///	INT32 pms;				/* PMS depth level of channel */
/*TODO*///	UINT32 ams;				/* AMS depth level of channel */
/*TODO*///	/* Phase Generator */
/*TODO*///	UINT32 fc;			/* fnum,blk    :adjusted to sampling rate */
/*TODO*///	UINT8 fn_h;			/* freq latch  :                   */
/*TODO*///	UINT8 kcode;		/* key code    :                   */
/*TODO*///} FM_CH;
/*TODO*///
/*TODO*////* OPN/OPM common state */
/*TODO*///typedef struct fm_state {
/*TODO*///	UINT8 index;		/* chip index (number of chip) */
/*TODO*///	int clock;			/* master clock  (Hz)  */
/*TODO*///	int rate;			/* sampling rate (Hz)  */
/*TODO*///	double freqbase;	/* frequency base      */
/*TODO*///	double TimerBase;	/* Timer base time     */
/*TODO*///	UINT8 address;		/* address register    */
/*TODO*///	UINT8 irq;			/* interrupt level     */
/*TODO*///	UINT8 irqmask;		/* irq mask            */
/*TODO*///	UINT8 status;		/* status flag         */
/*TODO*///	UINT32 mode;		/* mode  CSM / 3SLOT   */
/*TODO*///	int TA;				/* timer a             */
/*TODO*///	int TAC;			/* timer a counter     */
/*TODO*///	UINT8 TB;			/* timer b             */
/*TODO*///	int TBC;			/* timer b counter     */
/*TODO*///	/* speedup customize */
/*TODO*///	/* local time tables */
/*TODO*///	INT32 DT_TABLE[8][32];	/* DeTune tables       */
/*TODO*///	INT32 AR_TABLE[94];		/* Atttack rate tables */
/*TODO*///	INT32 DR_TABLE[94];		/* Decay rate tables   */
/*TODO*///	/* Extention Timer and IRQ handler */
/*TODO*///	FM_TIMERHANDLER	Timer_Handler;
/*TODO*///	FM_IRQHANDLER	IRQ_Handler;
/*TODO*///	/* timer model single / interval */
/*TODO*///	UINT8 timermodel;
/*TODO*///}FM_ST;
/*TODO*///
/*TODO*////* -------------------- tables --------------------- */
/*TODO*///
/*TODO*///#if 0	/* OPN/OPM does not contain KeyScaleLevel ? */
/*TODO*///
/*TODO*////* key scale level */
/*TODO*///#define DV (1/EG_STEP)
/*TODO*///static const UINT8 KSL[32]=
/*TODO*///{
/*TODO*///#if 1
/*TODO*/// 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
/*TODO*///#else
/*TODO*/// 0.000/DV , 0.000/DV , 0.000/DV , 0.000/DV ,	/* OCT 0 */
/*TODO*/// 0.000/DV , 0.000/DV , 0.000/DV , 1.875/DV ,	/* OCT 1 */
/*TODO*/// 0.000/DV , 0.000/DV , 3.000/DV , 4.875/DV ,	/* OCT 2 */
/*TODO*/// 0.000/DV , 3.000/DV , 6.000/DV , 7.875/DV ,	/* OCT 3 */
/*TODO*/// 0.000/DV , 6.000/DV , 9.000/DV ,10.875/DV ,	/* OCT 4 */
/*TODO*/// 0.000/DV , 9.000/DV ,12.000/DV ,13.875/DV ,	/* OCT 5 */
/*TODO*/// 0.000/DV ,12.000/DV ,15.000/DV ,16.875/DV ,	/* OCT 6 */
/*TODO*/// 0.000/DV ,15.000/DV ,18.000/DV ,19.875/DV		/* OCT 7 */
/*TODO*///#endif
/*TODO*///};
/*TODO*///#undef DV
/*TODO*///#endif
/*TODO*///
/*TODO*////* sustain lebel table (3db per step) */
/*TODO*////* 0 - 15: 0, 3, 6, 9,12,15,18,21,24,27,30,33,36,39,42,93 (dB)*/
/*TODO*///#define SC(db) (db*((3/EG_STEP)*(1<<ENV_BITS)))+EG_DST
/*TODO*///static const int SL_TABLE[16]={
/*TODO*/// SC( 0),SC( 1),SC( 2),SC(3 ),SC(4 ),SC(5 ),SC(6 ),SC( 7),
/*TODO*/// SC( 8),SC( 9),SC(10),SC(11),SC(12),SC(13),SC(14),SC(31)
/*TODO*///};
/*TODO*///#undef SC
/*TODO*///
/*TODO*////* size of TL_TABLE = sinwave(max cut_off) + cut_off(tl + ksr + envelope + ams) */
/*TODO*///#define TL_MAX (PG_CUT_OFF+EG_CUT_OFF+1)
/*TODO*///
/*TODO*////* TotalLevel : 48 24 12  6  3 1.5 0.75 (dB) */
/*TODO*////* TL_TABLE[ 0      to TL_MAX          ] : plus  section */
/*TODO*////* TL_TABLE[ TL_MAX to TL_MAX+TL_MAX-1 ] : minus section */
/*TODO*///static INT32 *TL_TABLE;
/*TODO*///
/*TODO*////* pointers to TL_TABLE with sinwave output offset */
/*TODO*///static INT32 *SIN_TABLE[SIN_ENT];
/*TODO*///
/*TODO*////* envelope output curve table */
/*TODO*///#if FM_SEG_SUPPORT
/*TODO*////* attack + decay + SSG upside + OFF */
/*TODO*///static INT32 ENV_CURVE[3*EG_ENT+1];
/*TODO*///#else
/*TODO*////* attack + decay + OFF */
/*TODO*///static INT32 ENV_CURVE[2*EG_ENT+1];
/*TODO*///#endif
/*TODO*////* envelope counter conversion table when change Decay to Attack phase */
/*TODO*///static int DRAR_TABLE[EG_ENT];
/*TODO*///
/*TODO*///#define OPM_DTTABLE OPN_DTTABLE
/*TODO*///static UINT8 OPN_DTTABLE[4 * 32]={
/*TODO*////* this table is YM2151 and YM2612 data */
/*TODO*////* FD=0 */
/*TODO*///  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
/*TODO*///  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
/*TODO*////* FD=1 */
/*TODO*///  0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2,
/*TODO*///  2, 3, 3, 3, 4, 4, 4, 5, 5, 6, 6, 7, 8, 8, 8, 8,
/*TODO*////* FD=2 */
/*TODO*///  1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5,
/*TODO*///  5, 6, 6, 7, 8, 8, 9,10,11,12,13,14,16,16,16,16,
/*TODO*////* FD=3 */
/*TODO*///  2, 2, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 6, 6, 7,
/*TODO*///  8 , 8, 9,10,11,12,13,14,16,17,19,20,22,22,22,22
/*TODO*///};
/*TODO*///
/*TODO*////* multiple table */
/*TODO*///#define ML(n) (n*2)
/*TODO*///static const int MUL_TABLE[4*16]= {
/*TODO*////* 1/2, 1, 2, 3, 4, 5, 6, 7, 8, 9,10,11,12,13,14,15 */
/*TODO*///   ML(0.50),ML( 1.00),ML( 2.00),ML( 3.00),ML( 4.00),ML( 5.00),ML( 6.00),ML( 7.00),
/*TODO*///   ML(8.00),ML( 9.00),ML(10.00),ML(11.00),ML(12.00),ML(13.00),ML(14.00),ML(15.00),
/*TODO*////* DT2=1 *SQL(2)   */
/*TODO*///   ML(0.71),ML( 1.41),ML( 2.82),ML( 4.24),ML( 5.65),ML( 7.07),ML( 8.46),ML( 9.89),
/*TODO*///   ML(11.30),ML(12.72),ML(14.10),ML(15.55),ML(16.96),ML(18.37),ML(19.78),ML(21.20),
/*TODO*////* DT2=2 *SQL(2.5) */
/*TODO*///   ML( 0.78),ML( 1.57),ML( 3.14),ML( 4.71),ML( 6.28),ML( 7.85),ML( 9.42),ML(10.99),
/*TODO*///   ML(12.56),ML(14.13),ML(15.70),ML(17.27),ML(18.84),ML(20.41),ML(21.98),ML(23.55),
/*TODO*////* DT2=3 *SQL(3)   */
/*TODO*///   ML( 0.87),ML( 1.73),ML( 3.46),ML( 5.19),ML( 6.92),ML( 8.65),ML(10.38),ML(12.11),
/*TODO*///   ML(13.84),ML(15.57),ML(17.30),ML(19.03),ML(20.76),ML(22.49),ML(24.22),ML(25.95)
/*TODO*///};
/*TODO*///#undef ML
/*TODO*///
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///
/*TODO*///#define PMS_RATE 0x400
/*TODO*///
/*TODO*////* LFO runtime work */
/*TODO*///static INT32 *LFO_wave;
/*TODO*///static UINT32 lfo_amd;
/*TODO*///static INT32 lfo_pmd;
/*TODO*///#endif
/*TODO*///
/*TODO*////* Dummy table of Attack / Decay rate ( use when rate == 0 ) */
/*TODO*///static const INT32 RATE_0[32]=
/*TODO*///{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
/*TODO*///
/*TODO*////* -------------------- state --------------------- */
/*TODO*///
/*TODO*////* some globals */
/*TODO*///#define TYPE_SSG    0x01    /* SSG support          */
/*TODO*///#define TYPE_OPN    0x02    /* OPN device           */
/*TODO*///#define TYPE_LFOPAN 0x04    /* OPN type LFO and PAN */
/*TODO*///#define TYPE_6CH    0x08    /* FM 6CH / 3CH         */
/*TODO*///#define TYPE_DAC    0x10    /* YM2612's DAC device  */
/*TODO*///#define TYPE_ADPCM  0x20    /* two ADPCM unit       */
/*TODO*///
/*TODO*///#define TYPE_YM2203 (TYPE_SSG)
/*TODO*///#define TYPE_YM2608 (TYPE_SSG |TYPE_LFOPAN |TYPE_6CH |TYPE_ADPCM)
/*TODO*///#define TYPE_YM2610 (TYPE_SSG |TYPE_LFOPAN |TYPE_6CH |TYPE_ADPCM)
/*TODO*///#define TYPE_YM2612 (TYPE_6CH |TYPE_LFOPAN |TYPE_DAC)
/*TODO*///
/*TODO*////* current chip state */
/*TODO*///static void *cur_chip = 0;		/* pointer of current chip struct */
/*TODO*///static FM_ST  *State;			/* basic status */
/*TODO*///static FM_CH  *cch[8];			/* pointer of FM channels */
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///static UINT32 LFOCnt,LFOIncr;	/* LFO PhaseGenerator */
/*TODO*///#endif
/*TODO*///
/*TODO*////* runtime work */
/*TODO*///static INT32 out_ch[4];		/* channel output NONE,LEFT,RIGHT or CENTER */
/*TODO*///static INT32 pg_in1,pg_in2,pg_in3,pg_in4;	/* PG input of SLOTs */
/*TODO*///
/*TODO*////* log output level */
/*TODO*///#define LOG_ERR  3      /* ERROR       */
/*TODO*///#define LOG_WAR  2      /* WARNING     */
/*TODO*///#define LOG_INF  1      /* INFORMATION */
/*TODO*///
/*TODO*///#define LOG_LEVEL LOG_INF
/*TODO*///
/*TODO*///#ifndef __RAINE__
/*TODO*///static void CLIB_DECL Log(int level,char *format,...)
/*TODO*///{
/*TODO*///	va_list argptr;
/*TODO*///
/*TODO*///	if( level < LOG_LEVEL ) return;
/*TODO*///	va_start(argptr,format);
/*TODO*///	/* */
/*TODO*///	if (errorlog) vfprintf( errorlog, format , argptr);
/*TODO*///}
/*TODO*///#endif
/*TODO*///
/*TODO*////* ----- limitter ----- */
/*TODO*///#define Limit(val, max,min) { \
/*TODO*///	if ( val > max )      val = max; \
/*TODO*///	else if ( val < min ) val = min; \
/*TODO*///}
/*TODO*///
/*TODO*////* ----- buffering one of data(STEREO chip) ----- */
/*TODO*///#ifdef FM_STEREO_MIX
/*TODO*////* stereo mixing */
/*TODO*///#define FM_BUFFERING_STEREO \
/*TODO*///{														\
/*TODO*///	/* get left & right output with clipping */			\
/*TODO*///	out_ch[OUTD_LEFT]  += out_ch[OUTD_CENTER];				\
/*TODO*///	Limit( out_ch[OUTD_LEFT] , FM_MAXOUT, FM_MINOUT );	\
/*TODO*///	out_ch[OUTD_RIGHT] += out_ch[OUTD_CENTER];				\
/*TODO*///	Limit( out_ch[OUTD_RIGHT], FM_MAXOUT, FM_MINOUT );	\
/*TODO*///	/* buffering */										\
/*TODO*///	((FMSAMPLE_MIX *)bufL)[i] = ((out_ch[OUTD_LEFT]>>FM_OUTSB)<<FM_OUTPUT_BIT)|(out_ch[OUTD_RIGHT]>>FM_OUTSB); \
/*TODO*///}
/*TODO*///#else
/*TODO*////* stereo separate */
/*TODO*///#define FM_BUFFERING_STEREO \
/*TODO*///{														\
/*TODO*///	/* get left & right output with clipping */			\
/*TODO*///	out_ch[OUTD_LEFT]  += out_ch[OUTD_CENTER];				\
/*TODO*///	Limit( out_ch[OUTD_LEFT] , FM_MAXOUT, FM_MINOUT );	\
/*TODO*///	out_ch[OUTD_RIGHT] += out_ch[OUTD_CENTER];				\
/*TODO*///	Limit( out_ch[OUTD_RIGHT], FM_MAXOUT, FM_MINOUT );	\
/*TODO*///	/* buffering */										\
/*TODO*///	bufL[i] = out_ch[OUTD_LEFT] >>FM_OUTSB;				\
/*TODO*///	bufR[i] = out_ch[OUTD_RIGHT]>>FM_OUTSB;				\
/*TODO*///}
/*TODO*///#endif
/*TODO*///
/*TODO*///#ifdef INTERNAL_TIMER
/*TODO*////* ----- internal timer mode , update timer */
/*TODO*////* ---------- calcrate timer A ---------- */
/*TODO*///#define INTERNAL_TIMER_A(ST,CSM_CH)					\
/*TODO*///{													\
/*TODO*///	if( ST->TAC &&  (ST->Timer_Handler==0) )		\
/*TODO*///		if( (ST->TAC -= ST->freqbase*4096) <= 0 )	\
/*TODO*///		{											\
/*TODO*///			TimerAOver( ST );						\
/*TODO*///			/* CSM mode total level latch and auto key on */	\
/*TODO*///			if( ST->mode & 0x80 )					\
/*TODO*///				CSMKeyControll( CSM_CH );			\
/*TODO*///		}											\
/*TODO*///}
/*TODO*////* ---------- calcrate timer B ---------- */
/*TODO*///#define INTERNAL_TIMER_B(ST,step)						\
/*TODO*///{														\
/*TODO*///	if( ST->TBC && (ST->Timer_Handler==0) )				\
/*TODO*///		if( (ST->TBC -= ST->freqbase*4096*step) <= 0 )	\
/*TODO*///			TimerBOver( ST );							\
/*TODO*///}
/*TODO*///#else
/*TODO*////* external timer mode */
/*TODO*///#define INTERNAL_TIMER_A(ST,CSM_CH)
/*TODO*///#define INTERNAL_TIMER_B(ST,step)
/*TODO*///#endif
/*TODO*///
/*TODO*////* --------------------- subroutines  --------------------- */
/*TODO*////* status set and IRQ handling */
/*TODO*///INLINE void FM_STATUS_SET(FM_ST *ST,int flag)
/*TODO*///{
/*TODO*///	/* set status flag */
/*TODO*///	ST->status |= flag;
/*TODO*///	if ( !(ST->irq) && (ST->status & ST->irqmask) )
/*TODO*///	{
/*TODO*///		ST->irq = 1;
/*TODO*///		/* callback user interrupt handler (IRQ is OFF to ON) */
/*TODO*///		if(ST->IRQ_Handler) (ST->IRQ_Handler)(ST->index,1);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* status reset and IRQ handling */
/*TODO*///INLINE void FM_STATUS_RESET(FM_ST *ST,int flag)
/*TODO*///{
/*TODO*///	/* reset status flag */
/*TODO*///	ST->status &=~flag;
/*TODO*///	if ( (ST->irq) && !(ST->status & ST->irqmask) )
/*TODO*///	{
/*TODO*///		ST->irq = 0;
/*TODO*///		/* callback user interrupt handler (IRQ is ON to OFF) */
/*TODO*///		if(ST->IRQ_Handler) (ST->IRQ_Handler)(ST->index,0);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* IRQ mask set */
/*TODO*///INLINE void FM_IRQMASK_SET(FM_ST *ST,int flag)
/*TODO*///{
/*TODO*///	ST->irqmask = flag;
/*TODO*///	/* IRQ handling check */
/*TODO*///	FM_STATUS_SET(ST,0);
/*TODO*///	FM_STATUS_RESET(ST,0);
/*TODO*///}
/*TODO*///
/*TODO*////* ---------- event hander of Phase Generator ---------- */
/*TODO*///
/*TODO*////* Release end -> stop counter */
/*TODO*///static void FM_EG_Release( FM_SLOT *SLOT )
/*TODO*///{
/*TODO*///	SLOT->evc = EG_OFF;
/*TODO*///	SLOT->eve = EG_OFF+1;
/*TODO*///	SLOT->evs = 0;
/*TODO*///}
/*TODO*///
/*TODO*////* SUSTAIN end -> stop counter */
/*TODO*///static void FM_EG_SR( FM_SLOT *SLOT )
/*TODO*///{
/*TODO*///	SLOT->evc = EG_OFF;
/*TODO*///	SLOT->eve = EG_OFF+1;
/*TODO*///	SLOT->evs = 0;
/*TODO*///}
/*TODO*///
/*TODO*////* Decay end -> Sustain */
/*TODO*///static void FM_EG_DR( FM_SLOT *SLOT )
/*TODO*///{
/*TODO*///	SLOT->eg_next = FM_EG_SR;
/*TODO*///	SLOT->evc = SLOT->SL;
/*TODO*///	SLOT->eve = EG_DED;
/*TODO*///	SLOT->evs = SLOT->evss;
/*TODO*///}
/*TODO*///
/*TODO*////* Attack end -> Decay */
/*TODO*///static void FM_EG_AR( FM_SLOT *SLOT )
/*TODO*///{
/*TODO*///	/* next DR */
/*TODO*///	SLOT->eg_next = FM_EG_DR;
/*TODO*///	SLOT->evc = EG_DST;
/*TODO*///	SLOT->eve = SLOT->SL;
/*TODO*///	SLOT->evs = SLOT->evsd;
/*TODO*///}
/*TODO*///
/*TODO*///#if FM_SEG_SUPPORT
/*TODO*///static void FM_EG_SSG_SR( FM_SLOT *SLOT );
/*TODO*///
/*TODO*////* SEG down side end  */
/*TODO*///static void FM_EG_SSG_DR( FM_SLOT *SLOT )
/*TODO*///{
/*TODO*///	if( SLOT->SEG&2){
/*TODO*///		/* reverce */
/*TODO*///		SLOT->eg_next = FM_EG_SSG_SR;
/*TODO*///		SLOT->evc = SLOT->SL + (EG_UST - EG_DST);
/*TODO*///		SLOT->eve = EG_UED;
/*TODO*///		SLOT->evs = SLOT->evss;
/*TODO*///	}else{
/*TODO*///		/* again */
/*TODO*///		SLOT->evc = EG_DST;
/*TODO*///	}
/*TODO*///	/* hold */
/*TODO*///	if( SLOT->SEG&1) SLOT->evs = 0;
/*TODO*///}
/*TODO*///
/*TODO*////* SEG upside side end */
/*TODO*///static void FM_EG_SSG_SR( FM_SLOT *SLOT )
/*TODO*///{
/*TODO*///	if( SLOT->SEG&2){
/*TODO*///		/* reverce  */
/*TODO*///		SLOT->eg_next = FM_EG_SSG_DR;
/*TODO*///		SLOT->evc = EG_DST;
/*TODO*///		SLOT->eve = EG_DED;
/*TODO*///		SLOT->evs = SLOT->evsd;
/*TODO*///	}else{
/*TODO*///		/* again */
/*TODO*///		SLOT->evc = SLOT->SL + (EG_UST - EG_DST);
/*TODO*///	}
/*TODO*///	/* hold check */
/*TODO*///	if( SLOT->SEG&1) SLOT->evs = 0;
/*TODO*///}
/*TODO*///
/*TODO*////* SEG Attack end */
/*TODO*///static void FM_EG_SSG_AR( FM_SLOT *SLOT )
/*TODO*///{
/*TODO*///	if( SLOT->SEG&4){	/* start direction */
/*TODO*///		/* next SSG-SR (upside start ) */
/*TODO*///		SLOT->eg_next = FM_EG_SSG_SR;
/*TODO*///		SLOT->evc = SLOT->SL + (EG_UST - EG_DST);
/*TODO*///		SLOT->eve = EG_UED;
/*TODO*///		SLOT->evs = SLOT->evss;
/*TODO*///	}else{
/*TODO*///		/* next SSG-DR (downside start ) */
/*TODO*///		SLOT->eg_next = FM_EG_SSG_DR;
/*TODO*///		SLOT->evc = EG_DST;
/*TODO*///		SLOT->eve = EG_DED;
/*TODO*///		SLOT->evs = SLOT->evsd;
/*TODO*///	}
/*TODO*///}
/*TODO*///#endif /* FM_SEG_SUPPORT */
/*TODO*///
/*TODO*////* ----- key on of SLOT ----- */
/*TODO*///#define FM_KEY_IS(SLOT) ((SLOT)->eg_next!=FM_EG_Release)
/*TODO*///
/*TODO*///INLINE void FM_KEYON(FM_CH *CH , int s )
/*TODO*///{
/*TODO*///	FM_SLOT *SLOT = &CH->SLOT[s];
/*TODO*///	if( !FM_KEY_IS(SLOT) )
/*TODO*///	{
/*TODO*///		/* restart Phage Generator */
/*TODO*///		SLOT->Cnt = 0;
/*TODO*///		/* phase -> Attack */
/*TODO*///#if FM_SEG_SUPPORT
/*TODO*///		if( SLOT->SEG&8 ) SLOT->eg_next = FM_EG_SSG_AR;
/*TODO*///		else
/*TODO*///#endif
/*TODO*///		SLOT->eg_next = FM_EG_AR;
/*TODO*///		SLOT->evs     = SLOT->evsa;
/*TODO*///#if 0
/*TODO*///		/* convert decay count to attack count */
/*TODO*///		/* --- This caused the problem by credit sound of paper boy. --- */
/*TODO*///		SLOT->evc = EG_AST + DRAR_TABLE[ENV_CURVE[SLOT->evc>>ENV_BITS]];/* + SLOT->evs;*/
/*TODO*///#else
/*TODO*///		/* reset attack counter */
/*TODO*///		SLOT->evc = EG_AST;
/*TODO*///#endif
/*TODO*///		SLOT->eve = EG_AED;
/*TODO*///	}
/*TODO*///}
/*TODO*////* ----- key off of SLOT ----- */
/*TODO*///INLINE void FM_KEYOFF(FM_CH *CH , int s )
/*TODO*///{
/*TODO*///	FM_SLOT *SLOT = &CH->SLOT[s];
/*TODO*///	if( FM_KEY_IS(SLOT) )
/*TODO*///	{
/*TODO*///		/* if Attack phase then adjust envelope counter */
/*TODO*///		if( SLOT->evc < EG_DST )
/*TODO*///			SLOT->evc = (ENV_CURVE[SLOT->evc>>ENV_BITS]<<ENV_BITS) + EG_DST;
/*TODO*///		/* phase -> Release */
/*TODO*///		SLOT->eg_next = FM_EG_Release;
/*TODO*///		SLOT->eve     = EG_DED;
/*TODO*///		SLOT->evs     = SLOT->evsr;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* setup Algorythm and PAN connection */
/*TODO*///static void setup_connection( FM_CH *CH )
/*TODO*///{
/*TODO*///	INT32 *carrier = &out_ch[CH->PAN]; /* NONE,LEFT,RIGHT or CENTER */
/*TODO*///
/*TODO*///	switch( CH->ALGO ){
/*TODO*///	case 0:
/*TODO*///		/*  PG---S1---S2---S3---S4---OUT */
/*TODO*///		CH->connect1 = &pg_in2;
/*TODO*///		CH->connect2 = &pg_in3;
/*TODO*///		CH->connect3 = &pg_in4;
/*TODO*///		break;
/*TODO*///	case 1:
/*TODO*///		/*  PG---S1-+-S3---S4---OUT */
/*TODO*///		/*  PG---S2-+               */
/*TODO*///		CH->connect1 = &pg_in3;
/*TODO*///		CH->connect2 = &pg_in3;
/*TODO*///		CH->connect3 = &pg_in4;
/*TODO*///		break;
/*TODO*///	case 2:
/*TODO*///		/* PG---S1------+-S4---OUT */
/*TODO*///		/* PG---S2---S3-+          */
/*TODO*///		CH->connect1 = &pg_in4;
/*TODO*///		CH->connect2 = &pg_in3;
/*TODO*///		CH->connect3 = &pg_in4;
/*TODO*///		break;
/*TODO*///	case 3:
/*TODO*///		/* PG---S1---S2-+-S4---OUT */
/*TODO*///		/* PG---S3------+          */
/*TODO*///		CH->connect1 = &pg_in2;
/*TODO*///		CH->connect2 = &pg_in4;
/*TODO*///		CH->connect3 = &pg_in4;
/*TODO*///		break;
/*TODO*///	case 4:
/*TODO*///		/* PG---S1---S2-+--OUT */
/*TODO*///		/* PG---S3---S4-+      */
/*TODO*///		CH->connect1 = &pg_in2;
/*TODO*///		CH->connect2 = carrier;
/*TODO*///		CH->connect3 = &pg_in4;
/*TODO*///		break;
/*TODO*///	case 5:
/*TODO*///		/*         +-S2-+     */
/*TODO*///		/* PG---S1-+-S3-+-OUT */
/*TODO*///		/*         +-S4-+     */
/*TODO*///		CH->connect1 = 0;	/* special case */
/*TODO*///		CH->connect2 = carrier;
/*TODO*///		CH->connect3 = carrier;
/*TODO*///		break;
/*TODO*///	case 6:
/*TODO*///		/* PG---S1---S2-+     */
/*TODO*///		/* PG--------S3-+-OUT */
/*TODO*///		/* PG--------S4-+     */
/*TODO*///		CH->connect1 = &pg_in2;
/*TODO*///		CH->connect2 = carrier;
/*TODO*///		CH->connect3 = carrier;
/*TODO*///		break;
/*TODO*///	case 7:
/*TODO*///		/* PG---S1-+     */
/*TODO*///		/* PG---S2-+-OUT */
/*TODO*///		/* PG---S3-+     */
/*TODO*///		/* PG---S4-+     */
/*TODO*///		CH->connect1 = carrier;
/*TODO*///		CH->connect2 = carrier;
/*TODO*///		CH->connect3 = carrier;
/*TODO*///	}
/*TODO*///	CH->connect4 = carrier;
/*TODO*///}
/*TODO*///
/*TODO*////* set detune & multiple */
/*TODO*///INLINE void set_det_mul(FM_ST *ST,FM_CH *CH,FM_SLOT *SLOT,int v)
/*TODO*///{
/*TODO*///	SLOT->mul = MUL_TABLE[v&0x0f];
/*TODO*///	SLOT->DT  = ST->DT_TABLE[(v>>4)&7];
/*TODO*///	CH->SLOT[SLOT1].Incr=-1;
/*TODO*///}
/*TODO*///
/*TODO*////* set total level */
/*TODO*///INLINE void set_tl(FM_CH *CH,FM_SLOT *SLOT , int v,int csmflag)
/*TODO*///{
/*TODO*///	v &= 0x7f;
/*TODO*///	v = (v<<7)|v; /* 7bit -> 14bit */
/*TODO*///	SLOT->TL = (v*EG_ENT)>>14;
/*TODO*///	if( !csmflag )
/*TODO*///	{	/* not CSM latch total level */
/*TODO*///		SLOT->TLL = SLOT->TL /* + KSL[CH->kcode] */;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* set attack rate & key scale  */
/*TODO*///INLINE void set_ar_ksr(FM_CH *CH,FM_SLOT *SLOT,int v,INT32 *ar_table)
/*TODO*///{
/*TODO*///	SLOT->KSR  = 3-(v>>6);
/*TODO*///	SLOT->AR   = (v&=0x1f) ? &ar_table[v<<1] : RATE_0;
/*TODO*///	SLOT->evsa = SLOT->AR[SLOT->ksr];
/*TODO*///	if( SLOT->eg_next == FM_EG_AR ) SLOT->evs = SLOT->evsa;
/*TODO*///	CH->SLOT[SLOT1].Incr=-1;
/*TODO*///}
/*TODO*////* set decay rate */
/*TODO*///INLINE void set_dr(FM_SLOT *SLOT,int v,INT32 *dr_table)
/*TODO*///{
/*TODO*///	SLOT->DR = (v&=0x1f) ? &dr_table[v<<1] : RATE_0;
/*TODO*///	SLOT->evsd = SLOT->DR[SLOT->ksr];
/*TODO*///	if( SLOT->eg_next == FM_EG_DR ) SLOT->evs = SLOT->evsd;
/*TODO*///}
/*TODO*////* set sustain rate */
/*TODO*///INLINE void set_sr(FM_SLOT *SLOT,int v,INT32 *dr_table)
/*TODO*///{
/*TODO*///	SLOT->SR = (v&=0x1f) ? &dr_table[v<<1] : RATE_0;
/*TODO*///	SLOT->evss = SLOT->SR[SLOT->ksr];
/*TODO*///	if( SLOT->eg_next == FM_EG_SR ) SLOT->evs = SLOT->evss;
/*TODO*///}
/*TODO*////* set release rate */
/*TODO*///INLINE void set_sl_rr(FM_SLOT *SLOT,int v,INT32 *dr_table)
/*TODO*///{
/*TODO*///	SLOT->SL = SL_TABLE[(v>>4)];
/*TODO*///	SLOT->RR = &dr_table[((v&0x0f)<<2)|2];
/*TODO*///	SLOT->evsr = SLOT->RR[SLOT->ksr];
/*TODO*///	if( SLOT->eg_next == FM_EG_Release ) SLOT->evs = SLOT->evsr;
/*TODO*///}
/*TODO*///
/*TODO*////* operator output calcrator */
/*TODO*///#define OP_OUT(PG,EG)   SIN_TABLE[(PG/(0x1000000/SIN_ENT))&(SIN_ENT-1)][EG]
/*TODO*///#define OP_OUTN(PG,EG)  NOISE_TABLE[(PG/(0x1000000/SIN_ENT))&(SIN_ENT-1)][EG]
/*TODO*///
/*TODO*////* eg calcration */
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///#define FM_CALC_EG(OUT,SLOT)						\
/*TODO*///{													\
/*TODO*///	if( (SLOT.evc += SLOT.evs) >= SLOT.eve) 		\
/*TODO*///		SLOT.eg_next(&(SLOT));						\
/*TODO*///	OUT = SLOT.TLL+ENV_CURVE[SLOT.evc>>ENV_BITS];	\
/*TODO*///	if(SLOT.ams)									\
/*TODO*///		OUT += (SLOT.ams*lfo_amd/LFO_RATE);			\
/*TODO*///}
/*TODO*///#else
/*TODO*///#endif
/*TODO*///
/*TODO*////* ---------- calcrate one of channel ---------- */
/*TODO*///INLINE void FM_CALC_CH( FM_CH *CH )
/*TODO*///{
/*TODO*///	UINT32 eg_out1,eg_out2,eg_out3,eg_out4;  //envelope output
/*TODO*///
/*TODO*///	/* Phase Generator */
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///	INT32 pms = lfo_pmd * CH->pms / LFO_RATE;
/*TODO*///	if(pms)
/*TODO*///	{
/*TODO*///		pg_in1 = (CH->SLOT[SLOT1].Cnt += CH->SLOT[SLOT1].Incr + (INT32)(pms * CH->SLOT[SLOT1].Incr) / PMS_RATE);
/*TODO*///		pg_in2 = (CH->SLOT[SLOT2].Cnt += CH->SLOT[SLOT2].Incr + (INT32)(pms * CH->SLOT[SLOT2].Incr) / PMS_RATE);
/*TODO*///		pg_in3 = (CH->SLOT[SLOT3].Cnt += CH->SLOT[SLOT3].Incr + (INT32)(pms * CH->SLOT[SLOT3].Incr) / PMS_RATE);
/*TODO*///		pg_in4 = (CH->SLOT[SLOT4].Cnt += CH->SLOT[SLOT4].Incr + (INT32)(pms * CH->SLOT[SLOT4].Incr) / PMS_RATE);
/*TODO*///	}
/*TODO*///	else
/*TODO*///#endif
/*TODO*///	{
/*TODO*///		pg_in1 = (CH->SLOT[SLOT1].Cnt += CH->SLOT[SLOT1].Incr);
/*TODO*///		pg_in2 = (CH->SLOT[SLOT2].Cnt += CH->SLOT[SLOT2].Incr);
/*TODO*///		pg_in3 = (CH->SLOT[SLOT3].Cnt += CH->SLOT[SLOT3].Incr);
/*TODO*///		pg_in4 = (CH->SLOT[SLOT4].Cnt += CH->SLOT[SLOT4].Incr);
/*TODO*///	}
/*TODO*///
/*TODO*///	/* Envelope Generator */
/*TODO*///	FM_CALC_EG(eg_out1,CH->SLOT[SLOT1]);
/*TODO*///	FM_CALC_EG(eg_out2,CH->SLOT[SLOT2]);
/*TODO*///	FM_CALC_EG(eg_out3,CH->SLOT[SLOT3]);
/*TODO*///	FM_CALC_EG(eg_out4,CH->SLOT[SLOT4]);
/*TODO*///
/*TODO*///	/* Connection */
/*TODO*///	if( eg_out1 < EG_CUT_OFF )	/* SLOT 1 */
/*TODO*///	{
/*TODO*///		if( CH->FB ){
/*TODO*///			/* with self feed back */
/*TODO*///			pg_in1 += (CH->op1_out[0]+CH->op1_out[1])>>CH->FB;
/*TODO*///			CH->op1_out[1] = CH->op1_out[0];
/*TODO*///		}
/*TODO*///		CH->op1_out[0] = OP_OUT(pg_in1,eg_out1);
/*TODO*///		/* output slot1 */
/*TODO*///		if( !CH->connect1 )
/*TODO*///		{
/*TODO*///			/* algorythm 5  */
/*TODO*///			pg_in2 += CH->op1_out[0];
/*TODO*///			pg_in3 += CH->op1_out[0];
/*TODO*///			pg_in4 += CH->op1_out[0];
/*TODO*///		}else{
/*TODO*///			/* other algorythm */
/*TODO*///			*CH->connect1 += CH->op1_out[0];
/*TODO*///		}
/*TODO*///	}
/*TODO*///	if( eg_out2 < EG_CUT_OFF )	/* SLOT 2 */
/*TODO*///		*CH->connect2 += OP_OUT(pg_in2,eg_out2);
/*TODO*///	if( eg_out3 < EG_CUT_OFF )	/* SLOT 3 */
/*TODO*///		*CH->connect3 += OP_OUT(pg_in3,eg_out3);
/*TODO*///	if( eg_out4 < EG_CUT_OFF )	/* SLOT 4 */
/*TODO*///		*CH->connect4 += OP_OUT(pg_in4,eg_out4);
/*TODO*///}
/*TODO*////* ---------- frequency counter for operater update ---------- */
/*TODO*///INLINE void CALC_FCSLOT(FM_SLOT *SLOT , int fc , int kc )
/*TODO*///{
/*TODO*///	int ksr;
/*TODO*///
/*TODO*///	/* frequency step counter */
/*TODO*///	/* SLOT->Incr= (fc+SLOT->DT[kc])*SLOT->mul; */
/*TODO*///	SLOT->Incr= fc*SLOT->mul + SLOT->DT[kc];
/*TODO*///	ksr = kc >> SLOT->KSR;
/*TODO*///	if( SLOT->ksr != ksr )
/*TODO*///	{
/*TODO*///		SLOT->ksr = ksr;
/*TODO*///		/* attack , decay rate recalcration */
/*TODO*///		SLOT->evsa = SLOT->AR[ksr];
/*TODO*///		SLOT->evsd = SLOT->DR[ksr];
/*TODO*///		SLOT->evss = SLOT->SR[ksr];
/*TODO*///		SLOT->evsr = SLOT->RR[ksr];
/*TODO*///	}
/*TODO*///	SLOT->TLL = SLOT->TL /* + KSL[kc]*/;
/*TODO*///}
/*TODO*///
/*TODO*////* ---------- frequency counter  ---------- */
/*TODO*///INLINE void CALC_FCOUNT(FM_CH *CH )
/*TODO*///{
/*TODO*///	if( CH->SLOT[SLOT1].Incr==-1){
/*TODO*///		int fc = CH->fc;
/*TODO*///		int kc = CH->kcode;
/*TODO*///		CALC_FCSLOT(&CH->SLOT[SLOT1] , fc , kc );
/*TODO*///		CALC_FCSLOT(&CH->SLOT[SLOT2] , fc , kc );
/*TODO*///		CALC_FCSLOT(&CH->SLOT[SLOT3] , fc , kc );
/*TODO*///		CALC_FCSLOT(&CH->SLOT[SLOT4] , fc , kc );
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* ----------- initialize time tabls ----------- */
/*TODO*///static void init_timetables( FM_ST *ST , UINT8 *DTTABLE , int ARRATE , int DRRATE )
/*TODO*///{
/*TODO*///	int i,d;
/*TODO*///	double rate;
/*TODO*///
/*TODO*///	/* DeTune table */
/*TODO*///	for (d = 0;d <= 3;d++){
/*TODO*///		for (i = 0;i <= 31;i++){
/*TODO*///			rate = (double)DTTABLE[d*32 + i] * ST->freqbase * FREQ_RATE;
/*TODO*///			ST->DT_TABLE[d][i]   =  rate;
/*TODO*///			ST->DT_TABLE[d+4][i] = -rate;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	/* make Attack & Decay tables */
/*TODO*///	for (i = 0;i < 4;i++) ST->AR_TABLE[i] = ST->DR_TABLE[i] = 0;
/*TODO*///	for (i = 4;i < 64;i++){
/*TODO*///		rate  = ST->freqbase;						/* frequency rate */
/*TODO*///		if( i < 60 ) rate *= 1.0+(i&3)*0.25;		/* b0-1 : x1 , x1.25 , x1.5 , x1.75 */
/*TODO*///		rate *= 1<<((i>>2)-1);						/* b2-5 : shift bit */
/*TODO*///		rate *= (double)(EG_ENT<<ENV_BITS);
/*TODO*///		ST->AR_TABLE[i] = rate / ARRATE;
/*TODO*///		ST->DR_TABLE[i] = rate / DRRATE;
/*TODO*///	}
/*TODO*///	ST->AR_TABLE[62] = EG_AED-1;
/*TODO*///	ST->AR_TABLE[63] = EG_AED-1;
/*TODO*///	for (i = 64;i < 94 ;i++){	/* make for overflow area */
/*TODO*///		ST->AR_TABLE[i] = ST->AR_TABLE[63];
/*TODO*///		ST->DR_TABLE[i] = ST->DR_TABLE[63];
/*TODO*///	}
/*TODO*///
/*TODO*///#if 0
/*TODO*///	for (i = 0;i < 64 ;i++){
/*TODO*///		Log(LOG_WAR,"rate %2d , ar %f ms , dr %f ms \n",i,
/*TODO*///			((double)(EG_ENT<<ENV_BITS) / ST->AR_TABLE[i]) * (1000.0 / ST->rate),
/*TODO*///			((double)(EG_ENT<<ENV_BITS) / ST->DR_TABLE[i]) * (1000.0 / ST->rate) );
/*TODO*///	}
/*TODO*///#endif
/*TODO*///}
/*TODO*///
/*TODO*////* ---------- reset one of channel  ---------- */
/*TODO*///static void reset_channel( FM_ST *ST , FM_CH *CH , int chan )
/*TODO*///{
/*TODO*///	int c,s;
/*TODO*///
/*TODO*///	ST->mode   = 0;	/* normal mode */
/*TODO*///	FM_STATUS_RESET(ST,0xff);
/*TODO*///	ST->TA     = 0;
/*TODO*///	ST->TAC    = 0;
/*TODO*///	ST->TB     = 0;
/*TODO*///	ST->TBC    = 0;
/*TODO*///
/*TODO*///	for( c = 0 ; c < chan ; c++ )
/*TODO*///	{
/*TODO*///		CH[c].fc = 0;
/*TODO*///		CH[c].PAN = OUTD_CENTER;
/*TODO*///		for(s = 0 ; s < 4 ; s++ )
/*TODO*///		{
/*TODO*///			CH[c].SLOT[s].SEG = 0;
/*TODO*///			CH[c].SLOT[s].eg_next= FM_EG_Release;
/*TODO*///			CH[c].SLOT[s].evc = EG_OFF;
/*TODO*///			CH[c].SLOT[s].eve = EG_OFF+1;
/*TODO*///			CH[c].SLOT[s].evs = 0;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* ---------- generic table initialize ---------- */
/*TODO*///static int FMInitTable( void )
/*TODO*///{
/*TODO*///	int s,t;
/*TODO*///	double rate;
/*TODO*///	int i,j;
/*TODO*///	double pom;
/*TODO*///
/*TODO*///	/* allocate total level table plus+minus section */
/*TODO*///	TL_TABLE = malloc(2*TL_MAX*sizeof(int));
/*TODO*///	if( TL_TABLE == 0 ) return 0;
/*TODO*///	/* make total level table */
/*TODO*///	for (t = 0;t < TL_MAX ;t++){
/*TODO*///		if(t >= PG_CUT_OFF)
/*TODO*///			rate = 0;	/* under cut off area */
/*TODO*///		else
/*TODO*///			rate = ((1<<TL_BITS)-1)/pow(10,EG_STEP*t/20);	/* dB -> voltage */
/*TODO*///		TL_TABLE[       t] =  (int)rate;
/*TODO*///		TL_TABLE[TL_MAX+t] = -TL_TABLE[t];
/*TODO*////*		Log(LOG_INF,"TotalLevel(%3d) = %x\n",t,TL_TABLE[t]);*/
/*TODO*///	}
/*TODO*///
/*TODO*///	/* make sinwave table (total level offet) */
/*TODO*///
/*TODO*///	for (s = 1;s <= SIN_ENT/4;s++){
/*TODO*///		pom = sin(2.0*PI*s/SIN_ENT); /* sin   */
/*TODO*///		pom = 20*log10(1/pom);	     /* -> decibel */
/*TODO*///		j = pom / EG_STEP;    /* TL_TABLE steps */
/*TODO*///		/* cut off check */
/*TODO*///		if(j > PG_CUT_OFF)
/*TODO*///			j = PG_CUT_OFF;
/*TODO*///		/* degree 0   -  90    , degree 180 -  90 : plus section */
/*TODO*///		SIN_TABLE[          s] = SIN_TABLE[SIN_ENT/2-s] = &TL_TABLE[j];
/*TODO*///		/* degree 180 - 270    , degree 360 - 270 : minus section */
/*TODO*///		SIN_TABLE[SIN_ENT/2+s] = SIN_TABLE[SIN_ENT  -s] = &TL_TABLE[TL_MAX+j];
/*TODO*///		/* Log(LOG_INF,"sin(%3d) = %f:%f db\n",s,pom,(double)j * EG_STEP); */
/*TODO*///	}
/*TODO*///	/* degree 0 = degree 180                   = off */
/*TODO*///	SIN_TABLE[0] = SIN_TABLE[SIN_ENT/2]        = &TL_TABLE[PG_CUT_OFF];
/*TODO*///
/*TODO*///	/* envelope counter -> envelope output table */
/*TODO*///	for (i=0; i<EG_ENT; i++)
/*TODO*///	{
/*TODO*///		/* ATTACK curve */
/*TODO*///		/* !!!!! preliminary !!!!! */
/*TODO*///		pom = pow( ((double)(EG_ENT-1-i)/EG_ENT) , 8 ) * EG_ENT;
/*TODO*///		/* if( pom >= EG_ENT ) pom = EG_ENT-1; */
/*TODO*///		ENV_CURVE[i] = (int)pom;
/*TODO*///		/* DECAY ,RELEASE curve */
/*TODO*///		ENV_CURVE[(EG_DST>>ENV_BITS)+i]= i;
/*TODO*///#if FM_SEG_SUPPORT
/*TODO*///		/* DECAY UPSIDE (SSG ENV) */
/*TODO*///		ENV_CURVE[(EG_UST>>ENV_BITS)+i]= EG_ENT-1-i;
/*TODO*///#endif
/*TODO*///	}
/*TODO*///	/* off */
/*TODO*///	ENV_CURVE[EG_OFF>>ENV_BITS]= EG_ENT-1;
/*TODO*///
/*TODO*///	/* decay to reattack envelope converttable */
/*TODO*///	j = EG_ENT-1;
/*TODO*///	for (i=0; i<EG_ENT; i++)
/*TODO*///	{
/*TODO*///		while( j && (ENV_CURVE[j] < i) ) j--;
/*TODO*///		DRAR_TABLE[i] = j<<ENV_BITS;
/*TODO*///		/* Log(LOG_INF,"DR %06X = %06X,AR=%06X\n",i,DRAR_TABLE[i],ENV_CURVE[DRAR_TABLE[i]>>ENV_BITS] ); */
/*TODO*///	}
/*TODO*///	return 1;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///static void FMCloseTable( void )
/*TODO*///{
/*TODO*///	if( TL_TABLE ) free( TL_TABLE );
/*TODO*///	return;
/*TODO*///}
/*TODO*///
/*TODO*////* OPN/OPM Mode  Register Write */
/*TODO*///INLINE void FMSetMode( FM_ST *ST ,int n,int v )
/*TODO*///{
/*TODO*///	/* b7 = CSM MODE */
/*TODO*///	/* b6 = 3 slot mode */
/*TODO*///	/* b5 = reset b */
/*TODO*///	/* b4 = reset a */
/*TODO*///	/* b3 = timer enable b */
/*TODO*///	/* b2 = timer enable a */
/*TODO*///	/* b1 = load b */
/*TODO*///	/* b0 = load a */
/*TODO*///	ST->mode = v;
/*TODO*///
/*TODO*///	/* reset Timer b flag */
/*TODO*///	if( v & 0x20 )
/*TODO*///		FM_STATUS_RESET(ST,0x02);
/*TODO*///	/* reset Timer a flag */
/*TODO*///	if( v & 0x10 )
/*TODO*///		FM_STATUS_RESET(ST,0x01);
/*TODO*///	/* load b */
/*TODO*///	if( v & 0x02 )
/*TODO*///	{
/*TODO*///		if( ST->TBC == 0 )
/*TODO*///		{
/*TODO*///			ST->TBC = ( 256-ST->TB)<<4;
/*TODO*///			/* External timer handler */
/*TODO*///			if (ST->Timer_Handler) (ST->Timer_Handler)(n,1,(double)ST->TBC,ST->TimerBase);
/*TODO*///		}
/*TODO*///	}else if (ST->timermodel == FM_TIMER_INTERVAL)
/*TODO*///	{	/* stop interbval timer */
/*TODO*///		if( ST->TBC != 0 )
/*TODO*///		{
/*TODO*///			ST->TBC = 0;
/*TODO*///			if (ST->Timer_Handler) (ST->Timer_Handler)(n,1,0,ST->TimerBase);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	/* load a */
/*TODO*///	if( v & 0x01 )
/*TODO*///	{
/*TODO*///		if( ST->TAC == 0 )
/*TODO*///		{
/*TODO*///			ST->TAC = (1024-ST->TA);
/*TODO*///			/* External timer handler */
/*TODO*///			if (ST->Timer_Handler) (ST->Timer_Handler)(n,0,(double)ST->TAC,ST->TimerBase);
/*TODO*///		}
/*TODO*///	}else if (ST->timermodel == FM_TIMER_INTERVAL)
/*TODO*///	{	/* stop interbval timer */
/*TODO*///		if( ST->TAC != 0 )
/*TODO*///		{
/*TODO*///			ST->TAC = 0;
/*TODO*///			if (ST->Timer_Handler) (ST->Timer_Handler)(n,0,0,ST->TimerBase);
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* Timer A Overflow */
/*TODO*///INLINE void TimerAOver(FM_ST *ST)
/*TODO*///{
/*TODO*///	/* status set if enabled */
/*TODO*///	if(ST->mode & 0x04) FM_STATUS_SET(ST,0x01);
/*TODO*///	/* clear or reload the counter */
/*TODO*///	if (ST->timermodel == FM_TIMER_INTERVAL)
/*TODO*///	{
/*TODO*///		ST->TAC = (1024-ST->TA);
/*TODO*///		if (ST->Timer_Handler) (ST->Timer_Handler)(ST->index,0,(double)ST->TAC,ST->TimerBase);
/*TODO*///	}
/*TODO*///	else ST->TAC = 0;
/*TODO*///}
/*TODO*////* Timer B Overflow */
/*TODO*///INLINE void TimerBOver(FM_ST *ST)
/*TODO*///{
/*TODO*///	/* status set if enabled */
/*TODO*///	if(ST->mode & 0x08) FM_STATUS_SET(ST,0x02);
/*TODO*///	/* clear or reload the counter */
/*TODO*///	if (ST->timermodel == FM_TIMER_INTERVAL)
/*TODO*///	{
/*TODO*///		ST->TBC = ( 256-ST->TB)<<4;
/*TODO*///		if (ST->Timer_Handler) (ST->Timer_Handler)(ST->index,1,(double)ST->TBC,ST->TimerBase);
/*TODO*///	}
/*TODO*///	else ST->TBC = 0;
/*TODO*///}
/*TODO*////* CSM Key Controll */
/*TODO*///INLINE void CSMKeyControll(FM_CH *CH)
/*TODO*///{
/*TODO*///	/* int ksl = KSL[CH->kcode]; */
/*TODO*///	/* all key off */
/*TODO*///	FM_KEYOFF(CH,SLOT1);
/*TODO*///	FM_KEYOFF(CH,SLOT2);
/*TODO*///	FM_KEYOFF(CH,SLOT3);
/*TODO*///	FM_KEYOFF(CH,SLOT4);
/*TODO*///	/* total level latch */
/*TODO*///	CH->SLOT[SLOT1].TLL = CH->SLOT[SLOT1].TL /*+ ksl*/;
/*TODO*///	CH->SLOT[SLOT2].TLL = CH->SLOT[SLOT2].TL /*+ ksl*/;
/*TODO*///	CH->SLOT[SLOT3].TLL = CH->SLOT[SLOT3].TL /*+ ksl*/;
/*TODO*///	CH->SLOT[SLOT4].TLL = CH->SLOT[SLOT4].TL /*+ ksl*/;
/*TODO*///	/* all key on */
/*TODO*///	FM_KEYON(CH,SLOT1);
/*TODO*///	FM_KEYON(CH,SLOT2);
/*TODO*///	FM_KEYON(CH,SLOT3);
/*TODO*///	FM_KEYON(CH,SLOT4);
/*TODO*///}
/*TODO*///
/*TODO*///#if BUILD_OPN
/*TODO*////***********************************************************/
/*TODO*////* OPN unit                                                */
/*TODO*////***********************************************************/
/*TODO*///
/*TODO*////* OPN 3slot struct */
/*TODO*///typedef struct opn_3slot {
/*TODO*///	UINT32  fc[3];		/* fnum3,blk3  :calcrated */
/*TODO*///	UINT8 fn_h[3];		/* freq3 latch            */
/*TODO*///	UINT8 kcode[3];		/* key code    :          */
/*TODO*///}FM_3SLOT;
/*TODO*///
/*TODO*////* OPN/A/B common state */
/*TODO*///typedef struct opn_f {
/*TODO*///	UINT8 type;		/* chip type         */
/*TODO*///	FM_ST ST;				/* general state     */
/*TODO*///	FM_3SLOT SL3;			/* 3 slot mode state */
/*TODO*///	FM_CH *P_CH;			/* pointer of CH     */
/*TODO*///	UINT32 FN_TABLE[2048]; /* fnumber -> increment counter */
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///	/* LFO */
/*TODO*///	UINT32 LFOCnt;
/*TODO*///	UINT32 LFOIncr;
/*TODO*///	UINT32 LFO_FREQ[8];/* LFO FREQ table */
/*TODO*///	INT32 LFO_wave[LFO_ENT];
/*TODO*///#endif
/*TODO*///} FM_OPN;
/*TODO*///
/*TODO*////* OPN key frequency number -> key code follow table */
/*TODO*////* fnum higher 4bit -> keycode lower 2bit */
/*TODO*///static const UINT8 OPN_FKTABLE[16]={0,0,0,0,0,0,0,1,2,3,3,3,3,3,3,3};
/*TODO*///
/*TODO*////* ---------- priscaler set(and make time tables) ---------- */
/*TODO*///void OPNSetPris(FM_OPN *OPN , int pris , int TimerPris, int SSGpris)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///
/*TODO*///	/* frequency base */
/*TODO*///	OPN->ST.freqbase = (OPN->ST.rate) ? ((double)OPN->ST.clock / OPN->ST.rate) / pris : 0;
/*TODO*///	/* Timer base time */
/*TODO*///	OPN->ST.TimerBase = 1.0/((double)OPN->ST.clock / (double)TimerPris);
/*TODO*///	/* SSG part  priscaler set */
/*TODO*///	if( SSGpris ) SSGClk( OPN->ST.index, OPN->ST.clock * 2 / SSGpris );
/*TODO*///	/* make time tables */
/*TODO*///	init_timetables( &OPN->ST , OPN_DTTABLE , OPN_ARRATE , OPN_DRRATE );
/*TODO*///	/* make fnumber -> increment counter table */
/*TODO*///	for( i=0 ; i < 2048 ; i++ )
/*TODO*///	{
/*TODO*///		/* it is freq table for octave 7 */
/*TODO*///		/* opn freq counter = 20bit */
/*TODO*///		OPN->FN_TABLE[i] = (double)i * OPN->ST.freqbase * FREQ_RATE * (1<<7) / 2;
/*TODO*///	}
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///	/* LFO wave table */
/*TODO*///	for(i=0;i<LFO_ENT;i++)
/*TODO*///	{
/*TODO*///		OPN->LFO_wave[i]= i<LFO_ENT/2 ? i*LFO_RATE/(LFO_ENT/2) : (LFO_ENT-i)*LFO_RATE/(LFO_ENT/2);
/*TODO*///	}
/*TODO*///	/* LFO freq. table */
/*TODO*///	{
/*TODO*///		/* 3.98Hz,5.56Hz,6.02Hz,6.37Hz,6.88Hz,9.63Hz,48.1Hz,72.2Hz @ 8MHz */
/*TODO*///		static const double freq_table[8] = { 3.98,5.56,6.02,6.37,6.88,9.63,48.1,72.2 };
/*TODO*///		for(i=0;i<8;i++)
/*TODO*///		{
/*TODO*///			OPN->LFO_FREQ[i] = (OPN->ST.rate) ? ( (double)LFO_ENT*(1<<LFO_SHIFT)
/*TODO*///					/ (OPN->ST.rate / freq_table[i]
/*TODO*///					* (OPN->ST.freqbase*OPN->ST.rate/(8000000.0/144))) ) : 0;
/*TODO*///
/*TODO*///		}
/*TODO*///	}
/*TODO*///#endif
/*TODO*////*	Log(LOG_INF,"OPN %d set priscaler %d\n",OPN->ST.index,pris);*/
/*TODO*///}
/*TODO*///
/*TODO*////* ---------- write a OPN mode register 0x20-0x2f ---------- */
/*TODO*///static void OPNWriteMode(FM_OPN *OPN, int r, int v)
/*TODO*///{
/*TODO*///	UINT8 c;
/*TODO*///	FM_CH *CH;
/*TODO*///
/*TODO*///	switch(r){
/*TODO*///	case 0x21:	/* Test */
/*TODO*///		break;
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///	case 0x22:	/* LFO FREQ (YM2608/YM2612) */
/*TODO*///		if( OPN->type & TYPE_LFOPAN )
/*TODO*///		{
/*TODO*///			OPN->LFOIncr = (v&0x08) ? OPN->LFO_FREQ[v&7] : 0;
/*TODO*///			cur_chip = NULL;
/*TODO*///		}
/*TODO*///		break;
/*TODO*///#endif
/*TODO*///	case 0x24:	/* timer A High 8*/
/*TODO*///		OPN->ST.TA = (OPN->ST.TA & 0x03)|(((int)v)<<2);
/*TODO*///		break;
/*TODO*///	case 0x25:	/* timer A Low 2*/
/*TODO*///		OPN->ST.TA = (OPN->ST.TA & 0x3fc)|(v&3);
/*TODO*///		break;
/*TODO*///	case 0x26:	/* timer B */
/*TODO*///		OPN->ST.TB = v;
/*TODO*///		break;
/*TODO*///	case 0x27:	/* mode , timer controll */
/*TODO*///		FMSetMode( &(OPN->ST),OPN->ST.index,v );
/*TODO*///		break;
/*TODO*///	case 0x28:	/* key on / off */
/*TODO*///		c = v&0x03;
/*TODO*///		if( c == 3 ) break;
/*TODO*///		if( (v&0x04) && (OPN->type & TYPE_6CH) ) c+=3;
/*TODO*///		CH = OPN->P_CH;
/*TODO*///		CH = &CH[c];
/*TODO*///		/* csm mode */
/*TODO*///		if( c == 2 && (OPN->ST.mode & 0x80) ) break;
/*TODO*///		if(v&0x10) FM_KEYON(CH,SLOT1); else FM_KEYOFF(CH,SLOT1);
/*TODO*///		if(v&0x20) FM_KEYON(CH,SLOT2); else FM_KEYOFF(CH,SLOT2);
/*TODO*///		if(v&0x40) FM_KEYON(CH,SLOT3); else FM_KEYOFF(CH,SLOT3);
/*TODO*///		if(v&0x80) FM_KEYON(CH,SLOT4); else FM_KEYOFF(CH,SLOT4);
/*TODO*////*		Log(LOG_INF,"OPN %d:%d : KEY %02X\n",n,c,v&0xf0);*/
/*TODO*///		break;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* ---------- write a OPN register (0x30-0xff) ---------- */
/*TODO*///static void OPNWriteReg(FM_OPN *OPN, int r, int v)
/*TODO*///{
/*TODO*///	UINT8 c;
/*TODO*///	FM_CH *CH;
/*TODO*///	FM_SLOT *SLOT;
/*TODO*///
/*TODO*///	/* 0x30 - 0xff */
/*TODO*///	if( (c = OPN_CHAN(r)) == 3 ) return; /* 0xX3,0xX7,0xXB,0xXF */
/*TODO*///	if( (r >= 0x100) /* && (OPN->type & TYPE_6CH) */ ) c+=3;
/*TODO*///		CH = OPN->P_CH;
/*TODO*///		CH = &CH[c];
/*TODO*///
/*TODO*///	SLOT = &(CH->SLOT[OPN_SLOT(r)]);
/*TODO*///	switch( r & 0xf0 ) {
/*TODO*///	case 0x30:	/* DET , MUL */
/*TODO*///		set_det_mul(&OPN->ST,CH,SLOT,v);
/*TODO*///		break;
/*TODO*///	case 0x40:	/* TL */
/*TODO*///		set_tl(CH,SLOT,v,(c == 2) && (OPN->ST.mode & 0x80) );
/*TODO*///		break;
/*TODO*///	case 0x50:	/* KS, AR */
/*TODO*///		set_ar_ksr(CH,SLOT,v,OPN->ST.AR_TABLE);
/*TODO*///		break;
/*TODO*///	case 0x60:	/*     DR */
/*TODO*///		/* bit7 = AMS_ON ENABLE(YM2612) */
/*TODO*///		set_dr(SLOT,v,OPN->ST.DR_TABLE);
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		if( OPN->type & TYPE_LFOPAN)
/*TODO*///		{
/*TODO*///			SLOT->amon = v>>7;
/*TODO*///			SLOT->ams = CH->ams * SLOT->amon;
/*TODO*///		}
/*TODO*///#endif
/*TODO*///		break;
/*TODO*///	case 0x70:	/*     SR */
/*TODO*///		set_sr(SLOT,v,OPN->ST.DR_TABLE);
/*TODO*///		break;
/*TODO*///	case 0x80:	/* SL, RR */
/*TODO*///		set_sl_rr(SLOT,v,OPN->ST.DR_TABLE);
/*TODO*///		break;
/*TODO*///	case 0x90:	/* SSG-EG */
/*TODO*///#if !FM_SEG_SUPPORT
/*TODO*///		if(v&0x08) Log(LOG_ERR,"OPN %d,%d,%d :SSG-TYPE envelope selected (not supported )\n",OPN->ST.index,c,OPN_SLOT(r));
/*TODO*///#endif
/*TODO*///		SLOT->SEG = v&0x0f;
/*TODO*///		break;
/*TODO*///	case 0xa0:
/*TODO*///		switch( OPN_SLOT(r) ){
/*TODO*///		case 0:		/* 0xa0-0xa2 : FNUM1 */
/*TODO*///			{
/*TODO*///				UINT32 fn  = (((UINT32)( (CH->fn_h)&7))<<8) + v;
/*TODO*///				UINT8 blk = CH->fn_h>>3;
/*TODO*///				/* make keyscale code */
/*TODO*///				CH->kcode = (blk<<2)|OPN_FKTABLE[(fn>>7)];
/*TODO*///				/* make basic increment counter 32bit = 1 cycle */
/*TODO*///				CH->fc = OPN->FN_TABLE[fn]>>(7-blk);
/*TODO*///				CH->SLOT[SLOT1].Incr=-1;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///		case 1:		/* 0xa4-0xa6 : FNUM2,BLK */
/*TODO*///			CH->fn_h = v&0x3f;
/*TODO*///			break;
/*TODO*///		case 2:		/* 0xa8-0xaa : 3CH FNUM1 */
/*TODO*///			if( r < 0x100)
/*TODO*///			{
/*TODO*///				UINT32 fn  = (((UINT32)(OPN->SL3.fn_h[c]&7))<<8) + v;
/*TODO*///				UINT8 blk = OPN->SL3.fn_h[c]>>3;
/*TODO*///				/* make keyscale code */
/*TODO*///				OPN->SL3.kcode[c]= (blk<<2)|OPN_FKTABLE[(fn>>7)];
/*TODO*///				/* make basic increment counter 32bit = 1 cycle */
/*TODO*///				OPN->SL3.fc[c] = OPN->FN_TABLE[fn]>>(7-blk);
/*TODO*///				(OPN->P_CH)[2].SLOT[SLOT1].Incr=-1;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///		case 3:		/* 0xac-0xae : 3CH FNUM2,BLK */
/*TODO*///			if( r < 0x100)
/*TODO*///				OPN->SL3.fn_h[c] = v&0x3f;
/*TODO*///			break;
/*TODO*///		}
/*TODO*///		break;
/*TODO*///	case 0xb0:
/*TODO*///		switch( OPN_SLOT(r) ){
/*TODO*///		case 0:		/* 0xb0-0xb2 : FB,ALGO */
/*TODO*///			{
/*TODO*///				int feedback = (v>>3)&7;
/*TODO*///				CH->ALGO = v&7;
/*TODO*///				CH->FB   = feedback ? 8+1 - feedback : 0;
/*TODO*///				setup_connection( CH );
/*TODO*///			}
/*TODO*///			break;
/*TODO*///		case 1:		/* 0xb4-0xb6 : L , R , AMS , PMS (YM2612/YM2608) */
/*TODO*///			if( OPN->type & TYPE_LFOPAN)
/*TODO*///			{
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///				/* b0-2 PMS */
/*TODO*///				/* 0,3.4,6.7,10,14,20,40,80(cent) */
/*TODO*///				static const double pmd_table[8]={0,3.4,6.7,10,14,20,40,80};
/*TODO*///				static const int amd_table[4]={0/EG_STEP,1.4/EG_STEP,5.9/EG_STEP,11.8/EG_STEP };
/*TODO*///				CH->pms = (1.5/1200.0)*pmd_table[(v>>4) & 0x07] * PMS_RATE;
/*TODO*///				/* b4-5 AMS */
/*TODO*///				/* 0 , 1.4 , 5.9 , 11.8(dB) */
/*TODO*///				CH->ams = amd_table[(v>>4) & 0x03];
/*TODO*///				CH->SLOT[SLOT1].ams = CH->ams * CH->SLOT[SLOT1].amon;
/*TODO*///				CH->SLOT[SLOT2].ams = CH->ams * CH->SLOT[SLOT2].amon;
/*TODO*///				CH->SLOT[SLOT3].ams = CH->ams * CH->SLOT[SLOT3].amon;
/*TODO*///				CH->SLOT[SLOT4].ams = CH->ams * CH->SLOT[SLOT4].amon;
/*TODO*///#endif
/*TODO*///				/* PAN */
/*TODO*///				CH->PAN = (v>>6)&0x03; /* PAN : b6 = R , b7 = L */
/*TODO*///				setup_connection( CH );
/*TODO*///				/* Log(LOG_INF,"OPN %d,%d : PAN %d\n",n,c,CH->PAN);*/
/*TODO*///			}
/*TODO*///			break;
/*TODO*///		}
/*TODO*///		break;
/*TODO*///	}
/*TODO*///}
/*TODO*///#endif /* BUILD_OPN */
/*TODO*///
/*TODO*///#if BUILD_YM2203
/*TODO*////*******************************************************************************/
/*TODO*////*		YM2203 local section                                                   */
/*TODO*////*******************************************************************************/
/*TODO*///
/*TODO*////* here's the virtual YM2203(OPN) */
/*TODO*///typedef struct ym2203_f {
/*TODO*///	FM_OPN OPN;				/* OPN state         */
/*TODO*///	FM_CH CH[3];			/* channel state     */
/*TODO*///} YM2203;
/*TODO*///
/*TODO*///static YM2203 *FM2203=NULL;	/* array of YM2203's */
/*TODO*///static int YM2203NumChips;	/* total chip */
/*TODO*///
/*TODO*////* ---------- update one of chip ----------- */
/*TODO*///void YM2203UpdateOne(int num, INT16 *buffer, int length)
/*TODO*///{
/*TODO*///	YM2203 *F2203 = &(FM2203[num]);
/*TODO*///	FM_OPN *OPN =   &(FM2203[num].OPN);
/*TODO*///	int i;
/*TODO*///	FM_CH *ch;
/*TODO*///	FMSAMPLE *buf = buffer;
/*TODO*///
/*TODO*///	cur_chip = (void *)F2203;
/*TODO*///	State = &F2203->OPN.ST;
/*TODO*///	cch[0]   = &F2203->CH[0];
/*TODO*///	cch[1]   = &F2203->CH[1];
/*TODO*///	cch[2]   = &F2203->CH[2];
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///	/* LFO */
/*TODO*///	lfo_amd = lfo_pmd = 0;
/*TODO*///#endif
/*TODO*///	/* frequency counter channel A */
/*TODO*///	CALC_FCOUNT( cch[0] );
/*TODO*///	/* frequency counter channel B */
/*TODO*///	CALC_FCOUNT( cch[1] );
/*TODO*///	/* frequency counter channel C */
/*TODO*///	if( (State->mode & 0xc0) ){
/*TODO*///		/* 3SLOT MODE */
/*TODO*///		if( cch[2]->SLOT[SLOT1].Incr==-1){
/*TODO*///			/* 3 slot mode */
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT1] , OPN->SL3.fc[1] , OPN->SL3.kcode[1] );
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT2] , OPN->SL3.fc[2] , OPN->SL3.kcode[2] );
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT3] , OPN->SL3.fc[0] , OPN->SL3.kcode[0] );
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT4] , cch[2]->fc , cch[2]->kcode );
/*TODO*///		}
/*TODO*///	}else CALC_FCOUNT( cch[2] );
/*TODO*///
/*TODO*///    for( i=0; i < length ; i++ )
/*TODO*///	{
/*TODO*///		/*            channel A         channel B         channel C      */
/*TODO*///		out_ch[OUTD_CENTER] = 0;
/*TODO*///		/* calcrate FM */
/*TODO*///		for( ch=cch[0] ; ch <= cch[2] ; ch++)
/*TODO*///			FM_CALC_CH( ch );
/*TODO*///		/* limit check */
/*TODO*///		Limit( out_ch[OUTD_CENTER] , FM_MAXOUT, FM_MINOUT );
/*TODO*///		/* store to sound buffer */
/*TODO*///		buf[i] = out_ch[OUTD_CENTER] >> FM_OUTSB;
/*TODO*///		/* timer controll */
/*TODO*///		INTERNAL_TIMER_A( State , cch[2] )
/*TODO*///	}
/*TODO*///	INTERNAL_TIMER_B(State,length)
/*TODO*///}
/*TODO*///
/*TODO*////* ---------- reset one of chip ---------- */
/*TODO*///void YM2203ResetChip(int num)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///	FM_OPN *OPN = &(FM2203[num].OPN);
/*TODO*///
/*TODO*///	/* Reset Priscaler */
/*TODO*///	OPNSetPris( OPN , 6*12 , 6*12 ,4); /* 1/6 , 1/4 */
/*TODO*///	/* reset SSG section */
/*TODO*///	SSGReset(OPN->ST.index);
/*TODO*///	/* status clear */
/*TODO*///	FM_IRQMASK_SET(&OPN->ST,0x03);
/*TODO*///	OPNWriteMode(OPN,0x27,0x30); /* mode 0 , timer reset */
/*TODO*///	reset_channel( &OPN->ST , FM2203[num].CH , 3 );
/*TODO*///	/* reset OPerator paramater */
/*TODO*///	for(i = 0xb6 ; i >= 0xb4 ; i-- ) OPNWriteReg(OPN,i,0xc0); /* PAN RESET */
/*TODO*///	for(i = 0xb2 ; i >= 0x30 ; i-- ) OPNWriteReg(OPN,i,0);
/*TODO*///	for(i = 0x26 ; i >= 0x20 ; i-- ) OPNWriteReg(OPN,i,0);
/*TODO*///}
/*TODO*///
/*TODO*////* ----------  Initialize YM2203 emulator(s) ----------    */
/*TODO*////* 'num' is the number of virtual YM2203's to allocate     */
/*TODO*////* 'rate' is sampling rate and 'bufsiz' is the size of the */
/*TODO*////* buffer that should be updated at each interval          */
/*TODO*///int YM2203Init(int num, int clock, int rate,
/*TODO*///               FM_TIMERHANDLER TimerHandler,FM_IRQHANDLER IRQHandler)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///
/*TODO*///	if (FM2203) return (-1);	/* duplicate init. */
/*TODO*///	cur_chip = NULL;	/* hiro-shi!! */
/*TODO*///
/*TODO*///	YM2203NumChips = num;
/*TODO*///
/*TODO*///	/* allocate ym2203 state space */
/*TODO*///	if( (FM2203 = (YM2203 *)malloc(sizeof(YM2203) * YM2203NumChips))==NULL)
/*TODO*///		return (-1);
/*TODO*///	/* clear */
/*TODO*///	memset(FM2203,0,sizeof(YM2203) * YM2203NumChips);
/*TODO*///	/* allocate total level table (128kb space) */
/*TODO*///	if( !FMInitTable() )
/*TODO*///	{
/*TODO*///		free( FM2203 );
/*TODO*///		return (-1);
/*TODO*///	}
/*TODO*///
/*TODO*///	for ( i = 0 ; i < YM2203NumChips; i++ ) {
/*TODO*///		FM2203[i].OPN.ST.index = i;
/*TODO*///		FM2203[i].OPN.type = TYPE_YM2203;
/*TODO*///		FM2203[i].OPN.P_CH = FM2203[i].CH;
/*TODO*///		FM2203[i].OPN.ST.clock = clock;
/*TODO*///		FM2203[i].OPN.ST.rate = rate;
/*TODO*///		/* FM2203[i].OPN.ST.irq = 0; */
/*TODO*///		/* FM2203[i].OPN.ST.satus = 0; */
/*TODO*///		FM2203[i].OPN.ST.timermodel = FM_TIMER_INTERVAL;
/*TODO*///		/* Extend handler */
/*TODO*///		FM2203[i].OPN.ST.Timer_Handler = TimerHandler;
/*TODO*///		FM2203[i].OPN.ST.IRQ_Handler   = IRQHandler;
/*TODO*///		YM2203ResetChip(i);
/*TODO*///	}
/*TODO*///	return(0);
/*TODO*///}
/*TODO*///
/*TODO*////* ---------- shut down emurator ----------- */
/*TODO*///void YM2203Shutdown(void)
/*TODO*///{
/*TODO*///    if (!FM2203) return;
/*TODO*///
/*TODO*///	FMCloseTable();
/*TODO*///	free(FM2203);
/*TODO*///	FM2203 = NULL;
/*TODO*///}
/*TODO*///
/*TODO*////* ---------- YM2203 I/O interface ---------- */
/*TODO*///int YM2203Write(int n,int a,UINT8 v)
/*TODO*///{
/*TODO*///	FM_OPN *OPN = &(FM2203[n].OPN);
/*TODO*///
/*TODO*///	if( !(a&1) )
/*TODO*///	{	/* address port */
/*TODO*///		OPN->ST.address = v & 0xff;
/*TODO*///		/* Write register to SSG emurator */
/*TODO*///		if( v < 16 ) SSGWrite(n,0,v);
/*TODO*///		switch(OPN->ST.address)
/*TODO*///		{
/*TODO*///		case 0x2d:	/* divider sel */
/*TODO*///			OPNSetPris( OPN, 6*12, 6*12 ,4); /* OPN 1/6 , SSG 1/4 */
/*TODO*///			break;
/*TODO*///		case 0x2e:	/* divider sel */
/*TODO*///			OPNSetPris( OPN, 3*12, 3*12,2); /* OPN 1/3 , SSG 1/2 */
/*TODO*///			break;
/*TODO*///		case 0x2f:	/* divider sel */
/*TODO*///			OPNSetPris( OPN, 2*12, 2*12,1); /* OPN 1/2 , SSG 1/1 */
/*TODO*///			break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{	/* data port */
/*TODO*///		int addr = OPN->ST.address;
/*TODO*///		switch( addr & 0xf0 )
/*TODO*///		{
/*TODO*///		case 0x00:	/* 0x00-0x0f : SSG section */
/*TODO*///			/* Write data to SSG emurator */
/*TODO*///			SSGWrite(n,a,v);
/*TODO*///			break;
/*TODO*///		case 0x20:	/* 0x20-0x2f : Mode section */
/*TODO*///			YM2203UpdateReq(n);
/*TODO*///			/* write register */
/*TODO*///			 OPNWriteMode(OPN,addr,v);
/*TODO*///			break;
/*TODO*///		default:	/* 0x30-0xff : OPN section */
/*TODO*///			YM2203UpdateReq(n);
/*TODO*///			/* write register */
/*TODO*///			 OPNWriteReg(OPN,addr,v);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	return OPN->ST.irq;
/*TODO*///}
/*TODO*///
/*TODO*///UINT8 YM2203Read(int n,int a)
/*TODO*///{
/*TODO*///	YM2203 *F2203 = &(FM2203[n]);
/*TODO*///	int addr = F2203->OPN.ST.address;
/*TODO*///	int ret = 0;
/*TODO*///
/*TODO*///	if( !(a&1) )
/*TODO*///	{	/* status port */
/*TODO*///		ret = F2203->OPN.ST.status;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{	/* data port (ONLY SSG) */
/*TODO*///		if( addr < 16 ) ret = SSGRead(n);
/*TODO*///	}
/*TODO*///	return ret;
/*TODO*///}
/*TODO*///
/*TODO*///int YM2203TimerOver(int n,int c)
/*TODO*///{
/*TODO*///	YM2203 *F2203 = &(FM2203[n]);
/*TODO*///
/*TODO*///	if( c )
/*TODO*///	{	/* Timer B */
/*TODO*///		TimerBOver( &(F2203->OPN.ST) );
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{	/* Timer A */
/*TODO*///		YM2203UpdateReq(n);
/*TODO*///		/* timer update */
/*TODO*///		TimerAOver( &(F2203->OPN.ST) );
/*TODO*///		/* CSM mode key,TL controll */
/*TODO*///		if( F2203->OPN.ST.mode & 0x80 )
/*TODO*///		{	/* CSM mode total level latch and auto key on */
/*TODO*///			CSMKeyControll( &(F2203->CH[2]) );
/*TODO*///		}
/*TODO*///	}
/*TODO*///	return F2203->OPN.ST.irq;
/*TODO*///}
/*TODO*///
/*TODO*///#endif /* BUILD_YM2203 */
/*TODO*///
/*TODO*///#if (BUILD_YM2608||BUILD_OPNB)
/*TODO*////* adpcm type A struct */
/*TODO*///typedef struct adpcm_state {
/*TODO*///	UINT8 flag;			/* port state        */
/*TODO*///	UINT8 flagMask;		/* arrived flag mask */
/*TODO*///	UINT8 now_data;
/*TODO*///	UINT32 now_addr;
/*TODO*///	UINT32 now_step;
/*TODO*///	UINT32 step;
/*TODO*///	UINT32 start;
/*TODO*///	UINT32 end;
/*TODO*///	int IL;
/*TODO*///	int volume;					/* calcrated mixing level */
/*TODO*///	INT32 *pan;					/* &out_ch[OPN_xxxx] */
/*TODO*///	int /*adpcmm,*/ adpcmx, adpcmd;
/*TODO*///	int adpcml;					/* hiro-shi!! */
/*TODO*///}ADPCM_CH;
/*TODO*///
/*TODO*////* here's the virtual YM2610 */
/*TODO*///typedef struct ym2610_f {
/*TODO*///	FM_OPN OPN;				/* OPN state    */
/*TODO*///	FM_CH CH[6];			/* channel state */
/*TODO*///	int address1;			/* address register1 */
/*TODO*///	/* ADPCM-A unit */
/*TODO*///	UINT8 *pcmbuf;			/* pcm rom buffer */
/*TODO*///	UINT32 pcm_size;			/* size of pcm rom */
/*TODO*///	INT32 *adpcmTL;					/* adpcmA total level */
/*TODO*///	ADPCM_CH adpcm[6];				/* adpcm channels */
/*TODO*///	UINT32 adpcmreg[0x30];	/* registers */
/*TODO*///	UINT8 adpcm_arrivedEndAddress;
/*TODO*///	/* Delta-T ADPCM unit */
/*TODO*///	YM_DELTAT deltaT;
/*TODO*///} YM2610;
/*TODO*///
/*TODO*////* here's the virtual YM2608 */
/*TODO*///typedef YM2610 YM2608;
/*TODO*///#endif /* (BUILD_YM2608||BUILD_OPNB) */
/*TODO*///
/*TODO*///#if BUILD_FM_ADPCMA
/*TODO*////***************************************************************/
/*TODO*////*    ADPCMA units are made by Hiromitsu Shioya (MMSND)         */
/*TODO*////***************************************************************/
/*TODO*///
/*TODO*////**** YM2610 ADPCM defines ****/
/*TODO*///#define ADPCMA_MIXING_LEVEL  (3) /* ADPCMA mixing level   */
/*TODO*///#define ADPCM_SHIFT    (16)      /* frequency step rate   */
/*TODO*///#define ADPCMA_ADDRESS_SHIFT 8   /* adpcm A address shift */
/*TODO*///
/*TODO*/////#define ADPCMA_DECODE_RANGE 1024
/*TODO*///#define ADPCMA_DECODE_RANGE 2048
/*TODO*///#define ADPCMA_DECODE_MIN (-(ADPCMA_DECODE_RANGE*ADPCMA_MIXING_LEVEL))
/*TODO*///#define ADPCMA_DECODE_MAX ((ADPCMA_DECODE_RANGE*ADPCMA_MIXING_LEVEL)-1)
/*TODO*///#define ADPCMA_VOLUME_DIV 1
/*TODO*///
/*TODO*///static UINT8 *pcmbufA;
/*TODO*///static UINT32 pcmsizeA;
/*TODO*///
/*TODO*////************************************************************/
/*TODO*////************************************************************/
/*TODO*////* --------------------- subroutines  --------------------- */
/*TODO*////************************************************************/
/*TODO*////************************************************************/
/*TODO*////************************/
/*TODO*////*    ADPCM A tables    */
/*TODO*////************************/
/*TODO*///static int jedi_table[(48+1)*16];
/*TODO*///static int decode_tableA1[16] = {
/*TODO*///  -1*16, -1*16, -1*16, -1*16, 2*16, 5*16, 7*16, 9*16,
/*TODO*///  -1*16, -1*16, -1*16, -1*16, 2*16, 5*16, 7*16, 9*16
/*TODO*///};
/*TODO*///
/*TODO*////* 0.9 , 0.9 , 0.9 , 0.9 , 1.2 , 1.6 , 2.0 , 2.4 */
/*TODO*////* 8 = -1 , 2 5 8 11 */
/*TODO*////* 9 = -1 , 2 5 9 13 */
/*TODO*////* 10= -1 , 2 6 10 14 */
/*TODO*////* 12= -1 , 2 7 12 17 */
/*TODO*////* 20= -2 , 4 12 20 32 */
/*TODO*///
/*TODO*///#if 1
/*TODO*///static void InitOPNB_ADPCMATable(void){
/*TODO*///	int step, nib;
/*TODO*///
/*TODO*///	for (step = 0; step <= 48; step++)
/*TODO*///	{
/*TODO*///		int stepval = floor (16.0 * pow (11.0 / 10.0, (double)step) * ADPCMA_MIXING_LEVEL);
/*TODO*///		/* loop over all nibbles and compute the difference */
/*TODO*///		for (nib = 0; nib < 16; nib++)
/*TODO*///		{
/*TODO*///			int value = stepval*((nib&0x07)*2+1)/8;
/*TODO*///			jedi_table[step*16+nib] = (nib&0x08) ? -value : value;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///#else
/*TODO*///static int decode_tableA2[49] = {
/*TODO*///  0x0010, 0x0011, 0x0013, 0x0015, 0x0017, 0x0019, 0x001c, 0x001f,
/*TODO*///  0x0022, 0x0025, 0x0029, 0x002d, 0x0032, 0x0037, 0x003c, 0x0042,
/*TODO*///  0x0049, 0x0050, 0x0058, 0x0061, 0x006b, 0x0076, 0x0082, 0x008f,
/*TODO*///  0x009d, 0x00ad, 0x00be, 0x00d1, 0x00e6, 0x00fd, 0x0117, 0x0133,
/*TODO*///  0x0151, 0x0173, 0x0198, 0x01c1, 0x01ee, 0x0220, 0x0256, 0x0292,
/*TODO*///  0x02d4, 0x031c, 0x036c, 0x03c3, 0x0424, 0x048e, 0x0502, 0x0583,
/*TODO*///  0x0610
/*TODO*///};
/*TODO*///static void InitOPNB_ADPCMATable(void){
/*TODO*///   int ta,tb,tc;
/*TODO*///   for(ta=0;ta<49;ta++){
/*TODO*///     for(tb=0;tb<16;tb++){
/*TODO*///       tc=0;
/*TODO*///       if(tb&0x04){tc+=((decode_tableA2[ta]*ADPCMA_MIXING_LEVEL));}
/*TODO*///       if(tb&0x02){tc+=((decode_tableA2[ta]*ADPCMA_MIXING_LEVEL)>>1);}
/*TODO*///       if(tb&0x01){tc+=((decode_tableA2[ta]*ADPCMA_MIXING_LEVEL)>>2);}
/*TODO*///       tc+=((decode_tableA2[ta]*ADPCMA_MIXING_LEVEL)>>3);
/*TODO*///       if(tb&0x08){tc=(0-tc);}
/*TODO*///       jedi_table[ta*16+tb]=tc;
/*TODO*///     }
/*TODO*///   }
/*TODO*///}
/*TODO*///#endif
/*TODO*///
/*TODO*////**** ADPCM A (Non control type) ****/
/*TODO*///INLINE void OPNB_ADPCM_CALC_CHA( YM2610 *F2610, ADPCM_CH *ch )
/*TODO*///{
/*TODO*///	UINT32 step;
/*TODO*///	int data;
/*TODO*///
/*TODO*///	ch->now_step += ch->step;
/*TODO*///	if ( ch->now_step >= (1<<ADPCM_SHIFT) )
/*TODO*///	{
/*TODO*///		step = ch->now_step >> ADPCM_SHIFT;
/*TODO*///		ch->now_step &= (1<<ADPCM_SHIFT)-1;
/*TODO*///		/* end check */
/*TODO*///		if ( (ch->now_addr+step) > (ch->end<<1) ) {
/*TODO*///			ch->flag = 0;
/*TODO*///			F2610->adpcm_arrivedEndAddress |= ch->flagMask;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		do{
/*TODO*///#if 0
/*TODO*///			if ( ch->now_addr > (pcmsizeA<<1) ) {
/*TODO*///				Log(LOG_WAR,"YM2610: Attempting to play past adpcm rom size!\n" );
/*TODO*///				return;
/*TODO*///			}
/*TODO*///#endif
/*TODO*///			if( ch->now_addr&1 ) data = ch->now_data & 0x0f;
/*TODO*///			else
/*TODO*///			{
/*TODO*///				ch->now_data = *(pcmbufA+(ch->now_addr>>1));
/*TODO*///				data = (ch->now_data >> 4)&0x0f;
/*TODO*///			}
/*TODO*///			ch->now_addr++;
/*TODO*///
/*TODO*///			ch->adpcmx += jedi_table[ch->adpcmd+data];
/*TODO*///			Limit( ch->adpcmx,ADPCMA_DECODE_MAX, ADPCMA_DECODE_MIN );
/*TODO*///			ch->adpcmd += decode_tableA1[data];
/*TODO*///			Limit( ch->adpcmd, 48*16, 0*16 );
/*TODO*///			/**** calc pcm * volume data ****/
/*TODO*///			ch->adpcml = ch->adpcmx * ch->volume;
/*TODO*///		}while(--step);
/*TODO*///	}
/*TODO*///	/* output for work of output channels (out_ch[OPNxxxx])*/
/*TODO*///	*(ch->pan) += ch->adpcml;
/*TODO*///}
/*TODO*///
/*TODO*////* ADPCM type A */
/*TODO*///static void FM_ADPCMAWrite(YM2610 *F2610,int r,int v)
/*TODO*///{
/*TODO*///	ADPCM_CH *adpcm = F2610->adpcm;
/*TODO*///	UINT8 c = r&0x07;
/*TODO*///
/*TODO*///	F2610->adpcmreg[r] = v&0xff; /* stock data */
/*TODO*///	switch( r ){
/*TODO*///	case 0x00: /* DM,--,C5,C4,C3,C2,C1,C0 */
/*TODO*///	  /* F2610->port1state = v&0xff; */
/*TODO*///	  if( !(v&0x80) ){
/*TODO*///	    /* KEY ON */
/*TODO*///	    for( c = 0; c < 6; c++ ){
/*TODO*///	      if( (1<<c)&v ){
/*TODO*///		/**** start adpcm ****/
/*TODO*///		adpcm[c].step     = (UINT32)((float)(1<<ADPCM_SHIFT)*((float)F2610->OPN.ST.freqbase)/3.0);
/*TODO*///		adpcm[c].now_addr = adpcm[c].start<<1;
/*TODO*///		adpcm[c].now_step = (1<<ADPCM_SHIFT)-adpcm[c].step;
/*TODO*///		/*adpcm[c].adpcmm   = 0;*/
/*TODO*///		adpcm[c].adpcmx   = 0;
/*TODO*///		adpcm[c].adpcmd   = 0;
/*TODO*///		adpcm[c].adpcml   = 0;
/*TODO*///		adpcm[c].flag     = 1;
/*TODO*///		if(F2610->pcmbuf==NULL){			// Check ROM Mapped
/*TODO*///#ifdef __RAINE__
/*TODO*///		  PrintDebug("YM2610: main adpcm rom not mapped\n");
/*TODO*///#else
/*TODO*///		  Log(LOG_WAR,"YM2610: Attempting to play regular adpcm but no rom is mapped\n");
/*TODO*///#endif
/*TODO*///		  adpcm[c].flag = 0;
/*TODO*///		} else{
/*TODO*///		  if(adpcm[c].end >= F2610->pcm_size){		// Check End in Range
/*TODO*///#ifdef __RAINE__
/*TODO*///		    PrintDebug("YM2610: main adpcm end out of range: $%08x\n",adpcm[c].end);
/*TODO*///#endif
/*TODO*///		    adpcm[c].end = F2610->pcm_size-1;
/*TODO*///		  }
/*TODO*///		  if(adpcm[c].start >= F2610->pcm_size){	// Check Start in Range
/*TODO*///#ifdef __RAINE__
/*TODO*///		    PrintDebug("YM2610: main adpcm start out of range: $%08x\n",adpcm[c].start);
/*TODO*///#endif
/*TODO*///		    adpcm[c].flag = 0;
/*TODO*///		  }
/*TODO*///
/*TODO*///Log(LOG_WAR,"YM2610: Start %06X : %02X %02X %02X\n",adpcm[c].start,
/*TODO*///pcmbufA[adpcm[c].start],pcmbufA[adpcm[c].start+1],pcmbufA[adpcm[c].start+2]);
/*TODO*///
/*TODO*///		}
/*TODO*///		/*** (1<<c)&v ***/
/*TODO*///	      }
/*TODO*///	      /**** for loop ****/
/*TODO*///	    }
/*TODO*///	  } else{
/*TODO*///	    /* KEY OFF */
/*TODO*///	    for( c = 0; c < 6; c++ ){
/*TODO*///	      if( (1<<c)&v )  adpcm[c].flag = 0;
/*TODO*///	    }
/*TODO*///	  }
/*TODO*///	  break;
/*TODO*///	case 0x01:	/* B0-5 = TL 0.75dB step */
/*TODO*///		F2610->adpcmTL = &(TL_TABLE[((v&0x3f)^0x3f)*(int)(0.75/EG_STEP)]);
/*TODO*///		for( c = 0; c < 6; c++ ){
/*TODO*///		  adpcm[c].volume = F2610->adpcmTL[adpcm[c].IL*(int)(0.75/EG_STEP)] / ADPCMA_DECODE_RANGE / ADPCMA_VOLUME_DIV;
/*TODO*///		  /**** calc pcm * volume data ****/
/*TODO*///		  adpcm[c].adpcml = adpcm[c].adpcmx * adpcm[c].volume;
/*TODO*///		}
/*TODO*///		break;
/*TODO*///	default:
/*TODO*///		c = r&0x07;
/*TODO*///		if( c >= 0x06 ) return;
/*TODO*///		switch( r&0x38 ){
/*TODO*///		case 0x08:	/* B7=L,B6=R,B4-0=IL */
/*TODO*///			adpcm[c].IL = (v&0x1f)^0x1f;
/*TODO*///			adpcm[c].volume = F2610->adpcmTL[adpcm[c].IL*(int)(0.75/EG_STEP)] / ADPCMA_DECODE_RANGE / ADPCMA_VOLUME_DIV;
/*TODO*///			adpcm[c].pan    = &out_ch[(v>>6)&0x03];
/*TODO*///			/**** calc pcm * volume data ****/
/*TODO*///			adpcm[c].adpcml = adpcm[c].adpcmx * adpcm[c].volume;
/*TODO*///			break;
/*TODO*///		case 0x10:
/*TODO*///		case 0x18:
/*TODO*///			adpcm[c].start  = ( (F2610->adpcmreg[0x18 + c]*0x0100 | F2610->adpcmreg[0x10 + c]) << ADPCMA_ADDRESS_SHIFT);
/*TODO*///			break;
/*TODO*///		case 0x20:
/*TODO*///		case 0x28:
/*TODO*///			adpcm[c].end    = ( (F2610->adpcmreg[0x28 + c]*0x0100 | F2610->adpcmreg[0x20 + c]) << ADPCMA_ADDRESS_SHIFT);
/*TODO*///			adpcm[c].end   += (1<<ADPCMA_ADDRESS_SHIFT) - 1;
/*TODO*///			break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///#endif /* BUILD_FM_ADPCMA */
/*TODO*///
/*TODO*///#if BUILD_YM2608
/*TODO*////*******************************************************************************/
/*TODO*////*		YM2608 local section                                                   */
/*TODO*////*******************************************************************************/
/*TODO*///static YM2608 *FM2608=NULL;	/* array of YM2608's */
/*TODO*///static int YM2608NumChips;	/* total chip */
/*TODO*///
/*TODO*////* YM2608 Rhythm Number */
/*TODO*///#define RY_BD  0
/*TODO*///#define RY_SD  1
/*TODO*///#define RY_TOP 2
/*TODO*///#define RY_HH  3
/*TODO*///#define RY_TOM 4
/*TODO*///#define RY_RIM 5
/*TODO*///
/*TODO*///#if 0
/*TODO*////* Get next pcm data */
/*TODO*///INLINE int YM2608ReadADPCM(int n)
/*TODO*///{
/*TODO*///	YM2608 *F2608 = &(FM2608[n]);
/*TODO*///	if( F2608->ADMode & 0x20 )
/*TODO*///	{	/* buffer memory */
/*TODO*///		/* F2203->OPN.ST.status |= 0x04; */
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{	/* from PCM data register */
/*TODO*///		FM_STATUS_SET(F2608->OPN.ST,0x08); /* BRDY = 1 */
/*TODO*///		return F2608->ADData;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* Put decoded data */
/*TODO*///INLINE void YM2608WriteADPCM(int n,int v)
/*TODO*///{
/*TODO*///	YM2608 *F2608 = &(FM2608[n]);
/*TODO*///	if( F2608->ADMode & 0x20 )
/*TODO*///	{	/* for buffer */
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{	/* for PCM data port */
/*TODO*///		F2608->ADData = v;
/*TODO*///		FM_STATUS_SET(F2608->OPN.ST,0x08) /* BRDY = 1 */
/*TODO*///	}
/*TODO*///}
/*TODO*///#endif
/*TODO*///
/*TODO*////* ---------- IRQ flag Controll Write 0x110 ---------- */
/*TODO*///INLINE void YM2608IRQFlagWrite(FM_ST *ST,int n,int v)
/*TODO*///{
/*TODO*///	if( v & 0x80 )
/*TODO*///	{	/* Reset IRQ flag */
/*TODO*///		FM_STATUS_RESET(ST,0xff);
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{	/* Set IRQ mask */
/*TODO*///		/* !!!!!!!!!! pending !!!!!!!!!! */
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///#ifdef YM2608_RHYTHM_PCM
/*TODO*////**** RYTHM (PCM) ****/
/*TODO*///INLINE void YM2608_RYTHM( YM2608 *F2608, ADPCM_CH *ch )
/*TODO*///
/*TODO*///{
/*TODO*///	UINT32 step;
/*TODO*///
/*TODO*///	ch->now_step += ch->step;
/*TODO*///	if ( ch->now_step >= (1<<ADPCM_SHIFT) )
/*TODO*///	{
/*TODO*///		step = ch->now_step >> ADPCM_SHIFT;
/*TODO*///		ch->now_step &= (1<<ADPCM_SHIFT)-1;
/*TODO*///		/* end check */
/*TODO*///		if ( (ch->now_addr+step) > (ch->end<<1) ) {
/*TODO*///			ch->flag = 0;
/*TODO*///			F2608->adpcm_arrivedEndAddress |= ch->flagMask;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		do{
/*TODO*///			/* get a next pcm data */
/*TODO*///			ch->adpcmx = ((short *)pcmbufA)[ch->now_addr];
/*TODO*///			ch->now_addr++;
/*TODO*///			/**** calc pcm * volume data ****/
/*TODO*///			ch->adpcml = ch->adpcmx * ch->volume;
/*TODO*///		}while(--step);
/*TODO*///	}
/*TODO*///	/* output for work of output channels (out_ch[OPNxxxx])*/
/*TODO*///	*(ch->pan) += ch->adpcml;
/*TODO*///}
/*TODO*///#endif /* YM2608_RHYTHM_PCM */
/*TODO*///
/*TODO*////* ---------- update one of chip ----------- */
/*TODO*///void YM2608UpdateOne(int num, INT16 **buffer, int length)
/*TODO*///{
/*TODO*///	YM2608 *F2608 = &(FM2608[num]);
/*TODO*///	FM_OPN *OPN   = &(FM2608[num].OPN);
/*TODO*///	YM_DELTAT *DELTAT = &(F2608[num].deltaT);
/*TODO*///	int i,j;
/*TODO*///	FM_CH *ch;
/*TODO*///	FMSAMPLE  *bufL,*bufR;
/*TODO*///
/*TODO*///	/* setup DELTA-T unit */
/*TODO*///	YM_DELTAT_DECODE_PRESET(DELTAT);
/*TODO*///
/*TODO*///	/* set bufer */
/*TODO*///	bufL = buffer[0];
/*TODO*///	bufR = buffer[1];
/*TODO*///
/*TODO*///	if( (void *)F2608 != cur_chip ){
/*TODO*///		cur_chip = (void *)F2608;
/*TODO*///
/*TODO*///		State = &OPN->ST;
/*TODO*///		cch[0]   = &F2608->CH[0];
/*TODO*///		cch[1]   = &F2608->CH[1];
/*TODO*///		cch[2]   = &F2608->CH[2];
/*TODO*///		cch[3]   = &F2608->CH[3];
/*TODO*///		cch[4]   = &F2608->CH[4];
/*TODO*///		cch[5]   = &F2608->CH[5];
/*TODO*///		/* setup adpcm rom address */
/*TODO*///		pcmbufA  = F2608->pcmbuf;
/*TODO*///		pcmsizeA = F2608->pcm_size;
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		LFOCnt  = OPN->LFOCnt;
/*TODO*///		LFOIncr = OPN->LFOIncr;
/*TODO*///		if( !LFOIncr ) lfo_amd = lfo_pmd = 0;
/*TODO*///		LFO_wave = OPN->LFO_wave;
/*TODO*///#endif
/*TODO*///	}
/*TODO*///	/* update frequency counter */
/*TODO*///	CALC_FCOUNT( cch[0] );
/*TODO*///	CALC_FCOUNT( cch[1] );
/*TODO*///	if( (State->mode & 0xc0) ){
/*TODO*///		/* 3SLOT MODE */
/*TODO*///		if( cch[2]->SLOT[SLOT1].Incr==-1){
/*TODO*///			/* 3 slot mode */
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT1] , OPN->SL3.fc[1] , OPN->SL3.kcode[1] );
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT2] , OPN->SL3.fc[2] , OPN->SL3.kcode[2] );
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT3] , OPN->SL3.fc[0] , OPN->SL3.kcode[0] );
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT4] , cch[2]->fc , cch[2]->kcode );
/*TODO*///		}
/*TODO*///	}else CALC_FCOUNT( cch[2] );
/*TODO*///	CALC_FCOUNT( cch[3] );
/*TODO*///	CALC_FCOUNT( cch[4] );
/*TODO*///	CALC_FCOUNT( cch[5] );
/*TODO*///	/* buffering */
/*TODO*///    for( i=0; i < length ; i++ )
/*TODO*///	{
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		/* LFO */
/*TODO*///		if( LFOIncr )
/*TODO*///		{
/*TODO*///			lfo_amd = LFO_wave[(LFOCnt+=LFOIncr)>>LFO_SHIFT];
/*TODO*///			lfo_pmd = lfo_amd-(LFO_RATE/2);
/*TODO*///		}
/*TODO*///#endif
/*TODO*///		/* clear output acc. */
/*TODO*///		out_ch[OUTD_LEFT] = out_ch[OUTD_RIGHT]= out_ch[OUTD_CENTER] = 0;
/*TODO*///		/**** deltaT ADPCM ****/
/*TODO*///		if( DELTAT->flag )
/*TODO*///			YM_DELTAT_ADPCM_CALC(DELTAT);
/*TODO*///		/* FM */
/*TODO*///		for(ch = cch[0] ; ch <= cch[5] ; ch++)
/*TODO*///			FM_CALC_CH( ch );
/*TODO*///		for( j = 0; j < 6; j++ )
/*TODO*///		{
/*TODO*///			/**** ADPCM ****/
/*TODO*///			if( F2608->adpcm[j].flag )
/*TODO*///#ifdef YM2608_RHYTHM_PCM
/*TODO*///				YM2608_RYTHM(F2608, &F2608->adpcm[j]);
/*TODO*///#else
/*TODO*///				OPNB_ADPCM_CALC_CHA( F2608, &F2608->adpcm[j]);
/*TODO*///#endif
/*TODO*///		}
/*TODO*///		/* buffering */
/*TODO*///		FM_BUFFERING_STEREO;
/*TODO*///		/* timer A controll */
/*TODO*///		INTERNAL_TIMER_A( State , cch[2] )
/*TODO*///	}
/*TODO*///	INTERNAL_TIMER_B(State,length)
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///	OPN->LFOCnt = LFOCnt;
/*TODO*///#endif
/*TODO*///}
/*TODO*///
/*TODO*////* -------------------------- YM2608(OPNA) ---------------------------------- */
/*TODO*///int YM2608Init(int num, int clock, int rate,
/*TODO*///               void **pcmrom,int *pcmsize,short *rhythmrom,int *rhythmpos,
/*TODO*///               FM_TIMERHANDLER TimerHandler,FM_IRQHANDLER IRQHandler)
/*TODO*///{
/*TODO*///	int i,j;
/*TODO*///
/*TODO*///    if (FM2608) return (-1);	/* duplicate init. */
/*TODO*///    cur_chip = NULL;	/* hiro-shi!! */
/*TODO*///
/*TODO*///	YM2608NumChips = num;
/*TODO*///
/*TODO*///	/* allocate extend state space */
/*TODO*///	if( (FM2608 = (YM2608 *)malloc(sizeof(YM2608) * YM2608NumChips))==NULL)
/*TODO*///		return (-1);
/*TODO*///	/* clear */
/*TODO*///	memset(FM2608,0,sizeof(YM2608) * YM2608NumChips);
/*TODO*///	/* allocate total level table (128kb space) */
/*TODO*///	if( !FMInitTable() )
/*TODO*///	{
/*TODO*///		free( FM2608 );
/*TODO*///		return (-1);
/*TODO*///	}
/*TODO*///
/*TODO*///	for ( i = 0 ; i < YM2608NumChips; i++ ) {
/*TODO*///		FM2608[i].OPN.ST.index = i;
/*TODO*///		FM2608[i].OPN.type = TYPE_YM2608;
/*TODO*///		FM2608[i].OPN.P_CH = FM2608[i].CH;
/*TODO*///		FM2608[i].OPN.ST.clock = clock;
/*TODO*///		FM2608[i].OPN.ST.rate = rate;
/*TODO*///		/* FM2608[i].OPN.ST.irq = 0; */
/*TODO*///		/* FM2608[i].OPN.ST.status = 0; */
/*TODO*///		FM2608[i].OPN.ST.timermodel = FM_TIMER_INTERVAL;
/*TODO*///		/* Extend handler */
/*TODO*///		FM2608[i].OPN.ST.Timer_Handler = TimerHandler;
/*TODO*///		FM2608[i].OPN.ST.IRQ_Handler   = IRQHandler;
/*TODO*///		/* DELTA-T */
/*TODO*///		FM2608[i].deltaT.memory = (UINT8 *)(pcmrom[i]);
/*TODO*///		FM2608[i].deltaT.memory_size = pcmsize[i];
/*TODO*///		/* ADPCM(Rythm) */
/*TODO*///		FM2608[i].pcmbuf   = (UINT8 *)rhythmrom;
/*TODO*///#ifdef YM2608_RHYTHM_PCM
/*TODO*///		/* rhythm sound setup (PCM) */
/*TODO*///		for(j=0;j<6;j++)
/*TODO*///		{
/*TODO*///			/* rhythm sound */
/*TODO*///			FM2608[i].adpcm[j].start = rhythmpos[j];
/*TODO*///			FM2608[i].adpcm[j].end   = rhythmpos[j+1]-1;
/*TODO*///		}
/*TODO*///		FM2608[i].pcm_size = rhythmpos[6];
/*TODO*///#else
/*TODO*///		/* rhythm sound setup (ADPCM) */
/*TODO*///		FM2608[i].pcm_size = rhythmsize;
/*TODO*///#endif
/*TODO*///		YM2608ResetChip(i);
/*TODO*///	}
/*TODO*///	InitOPNB_ADPCMATable();
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////* ---------- shut down emurator ----------- */
/*TODO*///void YM2608Shutdown()
/*TODO*///{
/*TODO*///    if (!FM2608) return;
/*TODO*///
/*TODO*///	FMCloseTable();
/*TODO*///	free(FM2608);
/*TODO*///	FM2608 = NULL;
/*TODO*///}
/*TODO*///
/*TODO*////* ---------- reset one of chip ---------- */
/*TODO*///void YM2608ResetChip(int num)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///	YM2608 *F2608 = &(FM2608[num]);
/*TODO*///	FM_OPN *OPN   = &(FM2608[num].OPN);
/*TODO*///	YM_DELTAT *DELTAT = &(F2608[num].deltaT);
/*TODO*///
/*TODO*///	/* Reset Priscaler */
/*TODO*///	OPNSetPris( OPN, 6*24, 6*24,4*2); /* OPN 1/6 , SSG 1/4 */
/*TODO*///	/* reset SSG section */
/*TODO*///	SSGReset(OPN->ST.index);
/*TODO*///	/* status clear */
/*TODO*///	FM_IRQMASK_SET(&OPN->ST,0x1f);
/*TODO*///	OPNWriteMode(OPN,0x27,0x30); /* mode 0 , timer reset */
/*TODO*///
/*TODO*///	/* extend 3ch. disable */
/*TODO*///	//OPN->type &= (~TYPE_6CH);
/*TODO*///
/*TODO*///	reset_channel( &OPN->ST , F2608->CH , 6 );
/*TODO*///	/* reset OPerator paramater */
/*TODO*///	for(i = 0xb6 ; i >= 0xb4 ; i-- )
/*TODO*///	{
/*TODO*///		OPNWriteReg(OPN,i      ,0xc0);
/*TODO*///		OPNWriteReg(OPN,i|0x100,0xc0);
/*TODO*///	}
/*TODO*///	for(i = 0xb2 ; i >= 0x30 ; i-- )
/*TODO*///	{
/*TODO*///		OPNWriteReg(OPN,i      ,0);
/*TODO*///		OPNWriteReg(OPN,i|0x100,0);
/*TODO*///	}
/*TODO*///	for(i = 0x26 ; i >= 0x20 ; i-- ) OPNWriteReg(OPN,i,0);
/*TODO*///	/* reset ADPCM unit */
/*TODO*///	/**** ADPCM work initial ****/
/*TODO*///	for( i = 0; i < 6+1; i++ ){
/*TODO*///		F2608->adpcm[i].now_addr  = 0;
/*TODO*///		F2608->adpcm[i].now_step  = 0;
/*TODO*///		F2608->adpcm[i].step      = 0;
/*TODO*///		F2608->adpcm[i].start     = 0;
/*TODO*///		F2608->adpcm[i].end       = 0;
/*TODO*///		/* F2608->adpcm[i].delta     = 21866; */
/*TODO*///		F2608->adpcm[i].volume    = 0;
/*TODO*///		F2608->adpcm[i].pan       = &out_ch[OUTD_CENTER]; /* default center */
/*TODO*///		F2608->adpcm[i].flagMask  = (i == 6) ? 0x20 : 0;
/*TODO*///		F2608->adpcm[i].flag      = 0;
/*TODO*///		F2608->adpcm[i].adpcmx    = 0;
/*TODO*///		F2608->adpcm[i].adpcmd    = 127;
/*TODO*///		F2608->adpcm[i].adpcml    = 0;
/*TODO*///	}
/*TODO*///	F2608->adpcmTL = &(TL_TABLE[0x3f*(int)(0.75/EG_STEP)]);
/*TODO*///	/* F2608->port1state = -1; */
/*TODO*///	F2608->adpcm_arrivedEndAddress = 0; /* don't used */
/*TODO*///
/*TODO*///	/* DELTA-T unit */
/*TODO*///	DELTAT->freqbase = OPN->ST.freqbase;
/*TODO*///	DELTAT->output_pointer = out_ch;
/*TODO*///	DELTAT->portshift = 8;		/* allways 8bits shift */
/*TODO*///	DELTAT->output_range = DELTAT_MIXING_LEVEL<<TL_BITS;
/*TODO*///	YM_DELTAT_ADPCM_Reset(DELTAT,OUTD_CENTER);
/*TODO*///}
/*TODO*///
/*TODO*////* YM2608 write */
/*TODO*////* n = number  */
/*TODO*////* a = address */
/*TODO*////* v = value   */
/*TODO*///int YM2608Write(int n, int a,UINT8 v)
/*TODO*///{
/*TODO*///	YM2608 *F2608 = &(FM2608[n]);
/*TODO*///	FM_OPN *OPN   = &(FM2608[n].OPN);
/*TODO*///	int addr;
/*TODO*///
/*TODO*///	switch(a&3){
/*TODO*///	case 0:	/* address port 0 */
/*TODO*///		OPN->ST.address = v & 0xff;
/*TODO*///		/* Write register to SSG emurator */
/*TODO*///		if( v < 16 ) SSGWrite(n,0,v);
/*TODO*///		switch(OPN->ST.address)
/*TODO*///		{
/*TODO*///		case 0x2d:	/* divider sel */
/*TODO*///			OPNSetPris( OPN, 6*24, 6*24, 4*2); /* OPN 1/6 , SSG 1/4 */
/*TODO*///			F2608->deltaT.freqbase = OPN->ST.freqbase;
/*TODO*///			break;
/*TODO*///		case 0x2e:	/* divider sel */
/*TODO*///			OPNSetPris( OPN, 3*24, 3*24,2*2); /* OPN 1/3 , SSG 1/2 */
/*TODO*///			F2608->deltaT.freqbase = OPN->ST.freqbase;
/*TODO*///			break;
/*TODO*///		case 0x2f:	/* divider sel */
/*TODO*///			OPNSetPris( OPN, 2*24, 2*24,1*2); /* OPN 1/2 , SSG 1/1 */
/*TODO*///			F2608->deltaT.freqbase = OPN->ST.freqbase;
/*TODO*///			break;
/*TODO*///		}
/*TODO*///		break;
/*TODO*///	case 1:	/* data port 0    */
/*TODO*///		addr = OPN->ST.address;
/*TODO*///		switch(addr & 0xf0)
/*TODO*///		{
/*TODO*///		case 0x00:	/* SSG section */
/*TODO*///			/* Write data to SSG emurator */
/*TODO*///			SSGWrite(n,a,v);
/*TODO*///			break;
/*TODO*///		case 0x10:	/* 0x10-0x1f : Rhythm section */
/*TODO*///			YM2608UpdateReq(n);
/*TODO*///			FM_ADPCMAWrite(F2608,addr-0x10,v);
/*TODO*///			break;
/*TODO*///		case 0x20:	/* Mode Register */
/*TODO*///			switch(addr)
/*TODO*///			{
/*TODO*///			case 0x29: /* SCH,xirq mask */
/*TODO*///				/* SCH,xx,xxx,EN_ZERO,EN_BRDY,EN_EOS,EN_TB,EN_TA */
/*TODO*///				/* extend 3ch. enable/disable */
/*TODO*///				if(v&0x80) OPN->type |= TYPE_6CH;
/*TODO*///				else       OPN->type &= ~TYPE_6CH;
/*TODO*///				/* IRQ MASK */
/*TODO*///				FM_IRQMASK_SET(&OPN->ST,v&0x1f);
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				YM2608UpdateReq(n);
/*TODO*///				OPNWriteMode(OPN,addr,v);
/*TODO*///			}
/*TODO*///			break;
/*TODO*///		default:	/* OPN section */
/*TODO*///			YM2608UpdateReq(n);
/*TODO*///			OPNWriteReg(OPN,addr,v);
/*TODO*///		}
/*TODO*///		break;
/*TODO*///	case 2:	/* address port 1 */
/*TODO*///		F2608->address1 = v & 0xff;
/*TODO*///		break;
/*TODO*///	case 3:	/* data port 1    */
/*TODO*///		addr = F2608->address1;
/*TODO*///		YM2608UpdateReq(n);
/*TODO*///		switch( addr & 0xf0 )
/*TODO*///		{
/*TODO*///		case 0x00:	/* ADPCM PORT */
/*TODO*///			switch( addr )
/*TODO*///			{
/*TODO*///			case 0x0c:	/* Limit address L */
/*TODO*///				//F2608->ADLimit = (F2608->ADLimit & 0xff00) | v;
/*TODO*///				//break;
/*TODO*///			case 0x0d:	/* Limit address H */
/*TODO*///				//F2608->ADLimit = (F2608->ADLimit & 0x00ff) | (v<<8);
/*TODO*///				//break;
/*TODO*///			case 0x0e:	/* DAC data */
/*TODO*///				//break;
/*TODO*///			case 0x0f:	/* PCM data port */
/*TODO*///				//F2608->ADData = v;
/*TODO*///				//FM_STATUS_RESET(F2608->OPN.ST,0x08);
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				/* 0x00-0x0b */
/*TODO*///				YM_DELTAT_ADPCM_Write(&F2608->deltaT,addr,v);
/*TODO*///			}
/*TODO*///			break;
/*TODO*///		case 0x10:	/* IRQ Flag controll */
/*TODO*///			if( addr == 0x10 )
/*TODO*///				YM2608IRQFlagWrite(&(OPN->ST),n,v);
/*TODO*///			break;
/*TODO*///		default:
/*TODO*///			OPNWriteReg(OPN,addr|0x100,v);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	return OPN->ST.irq;
/*TODO*///}
/*TODO*///UINT8 YM2608Read(int n,int a)
/*TODO*///{
/*TODO*///	YM2608 *F2608 = &(FM2608[n]);
/*TODO*///	int addr = F2608->OPN.ST.address;
/*TODO*///	int ret = 0;
/*TODO*///
/*TODO*///	switch( a&3 ){
/*TODO*///	case 0:	/* status 0 : YM2203 compatible */
/*TODO*///		/* BUSY:x:x:x:x:x:FLAGB:FLAGA */
/*TODO*///		if(addr==0xff) ret = 0x00; /* ID code */
/*TODO*///		else ret = F2608->OPN.ST.status & 0x83;
/*TODO*///		break;
/*TODO*///	case 1:	/* status 0 */
/*TODO*///		if( addr < 16 ) ret = SSGRead(n);
/*TODO*///		break;
/*TODO*///	case 2:	/* status 1 : + ADPCM status */
/*TODO*///		/* BUSY:x:PCMBUSY:ZERO:BRDY:EOS:FLAGB:FLAGA */
/*TODO*///		if(addr==0xff) ret = 0x00; /* ID code */
/*TODO*///		else ret = F2608->OPN.ST.status | (F2608->adpcm[6].flag ? 0x20 : 0);
/*TODO*///		break;
/*TODO*///	case 3:
/*TODO*///		ret = 0;
/*TODO*///		break;
/*TODO*///	}
/*TODO*///	return ret;
/*TODO*///}
/*TODO*///
/*TODO*///int YM2608TimerOver(int n,int c)
/*TODO*///{
/*TODO*///	YM2608 *F2608 = &(FM2608[n]);
/*TODO*///
/*TODO*///	if( c )
/*TODO*///	{	/* Timer B */
/*TODO*///		TimerBOver( &(F2608->OPN.ST) );
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{	/* Timer A */
/*TODO*///		YM2608UpdateReq(n);
/*TODO*///		/* timer update */
/*TODO*///		TimerAOver( &(F2608->OPN.ST) );
/*TODO*///		/* CSM mode key,TL controll */
/*TODO*///		if( F2608->OPN.ST.mode & 0x80 )
/*TODO*///		{	/* CSM mode total level latch and auto key on */
/*TODO*///			CSMKeyControll( &(F2608->CH[2]) );
/*TODO*///		}
/*TODO*///	}
/*TODO*///	return FM2608->OPN.ST.irq;
/*TODO*///}
/*TODO*///
/*TODO*///#endif /* BUILD_YM2608 */
/*TODO*///
/*TODO*///#if BUILD_OPNB
/*TODO*////* -------------------------- YM2610(OPNB) ---------------------------------- */
/*TODO*///static YM2610 *FM2610=NULL;	/* array of YM2610's */
/*TODO*///static int YM2610NumChips;	/* total chip */
/*TODO*///
/*TODO*////* ---------- update one of chip (YM2610B FM6: ADPCM-A6: ADPCM-B:1) ----------- */
/*TODO*///void YM2610UpdateOne(int num, INT16 **buffer, int length)
/*TODO*///{
/*TODO*///	YM2610 *F2610 = &(FM2610[num]);
/*TODO*///	FM_OPN *OPN   = &(FM2610[num].OPN);
/*TODO*///	YM_DELTAT *DELTAT = &(F2610[num].deltaT);
/*TODO*///	int i,j;
/*TODO*///	int ch;
/*TODO*///	FMSAMPLE  *bufL,*bufR;
/*TODO*///
/*TODO*///	/* setup DELTA-T unit */
/*TODO*///	YM_DELTAT_DECODE_PRESET(DELTAT);
/*TODO*///
/*TODO*///	/* buffer setup */
/*TODO*///	bufL = buffer[0];
/*TODO*///	bufR = buffer[1];
/*TODO*///
/*TODO*///	if( (void *)F2610 != cur_chip ){
/*TODO*///		cur_chip = (void *)F2610;
/*TODO*///		State = &OPN->ST;
/*TODO*///		cch[0] = &F2610->CH[1];
/*TODO*///		cch[1] = &F2610->CH[2];
/*TODO*///		cch[2] = &F2610->CH[4];
/*TODO*///		cch[3] = &F2610->CH[5];
/*TODO*///		/* setup adpcm rom address */
/*TODO*///		pcmbufA  = F2610->pcmbuf;
/*TODO*///		pcmsizeA = F2610->pcm_size;
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		LFOCnt  = OPN->LFOCnt;
/*TODO*///		LFOIncr = OPN->LFOIncr;
/*TODO*///		if( !LFOIncr ) lfo_amd = lfo_pmd = 0;
/*TODO*///		LFO_wave = OPN->LFO_wave;
/*TODO*///#endif
/*TODO*///	}
/*TODO*///#ifdef YM2610B_WARNING
/*TODO*///#define FM_MSG_YM2610B "YM2610-%d.CH%d is playing,Check whether the type of the chip is YM2610B\n"
/*TODO*///	/* Check YM2610B worning message */
/*TODO*///	if(errorlog)
/*TODO*///	{
/*TODO*///		if( FM_KEY_IS(&F2610->CH[0].SLOT[3]) )
/*TODO*///			Log(LOG_WAR,FM_MSG_YM2610B,num,0);
/*TODO*///		if( FM_KEY_IS(&F2610->CH[3].SLOT[3]) )
/*TODO*///			Log(LOG_WAR,FM_MSG_YM2610B,num,3);
/*TODO*///	}
/*TODO*///#endif
/*TODO*///	/* update frequency counter */
/*TODO*///	CALC_FCOUNT( cch[0] );
/*TODO*///	if( (State->mode & 0xc0) ){
/*TODO*///		/* 3SLOT MODE */
/*TODO*///		if( cch[1]->SLOT[SLOT1].Incr==-1){
/*TODO*///			/* 3 slot mode */
/*TODO*///			CALC_FCSLOT(&cch[1]->SLOT[SLOT1] , OPN->SL3.fc[1] , OPN->SL3.kcode[1] );
/*TODO*///			CALC_FCSLOT(&cch[1]->SLOT[SLOT2] , OPN->SL3.fc[2] , OPN->SL3.kcode[2] );
/*TODO*///			CALC_FCSLOT(&cch[1]->SLOT[SLOT3] , OPN->SL3.fc[0] , OPN->SL3.kcode[0] );
/*TODO*///			CALC_FCSLOT(&cch[1]->SLOT[SLOT4] , cch[1]->fc , cch[1]->kcode );
/*TODO*///		}
/*TODO*///	}else CALC_FCOUNT( cch[1] );
/*TODO*///	CALC_FCOUNT( cch[2] );
/*TODO*///	CALC_FCOUNT( cch[3] );
/*TODO*///
/*TODO*///	/* buffering */
/*TODO*///    for( i=0; i < length ; i++ )
/*TODO*///	{
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		/* LFO */
/*TODO*///		if( LFOIncr )
/*TODO*///		{
/*TODO*///			lfo_amd = LFO_wave[(LFOCnt+=LFOIncr)>>LFO_SHIFT];
/*TODO*///			lfo_pmd = lfo_amd-(LFO_RATE/2);
/*TODO*///		}
/*TODO*///#endif
/*TODO*///		/* clear output acc. */
/*TODO*///		out_ch[OUTD_LEFT] = out_ch[OUTD_RIGHT]= out_ch[OUTD_CENTER] = 0;
/*TODO*///		/**** deltaT ADPCM ****/
/*TODO*///		if( DELTAT->flag )
/*TODO*///			YM_DELTAT_ADPCM_CALC(DELTAT);
/*TODO*///		/* FM */
/*TODO*///		for(ch = 0 ; ch < 4 ; ch++)
/*TODO*///			FM_CALC_CH( cch[ch] );
/*TODO*///		for( j = 0; j < 6; j++ )
/*TODO*///		{
/*TODO*///			/**** ADPCM ****/
/*TODO*///			if( F2610->adpcm[j].flag )
/*TODO*///				OPNB_ADPCM_CALC_CHA( F2610, &F2610->adpcm[j]);
/*TODO*///		}
/*TODO*///		/* buffering */
/*TODO*///		FM_BUFFERING_STEREO;
/*TODO*///		/* timer A controll */
/*TODO*///		INTERNAL_TIMER_A( State , cch[1] )
/*TODO*///	}
/*TODO*///	INTERNAL_TIMER_B(State,length)
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///	OPN->LFOCnt = LFOCnt;
/*TODO*///#endif
/*TODO*///}
/*TODO*///#endif /* BUILD_OPNB */
/*TODO*///
/*TODO*///#if BUILD_YM2610B
/*TODO*////* ---------- update one of chip (YM2610B FM6: ADPCM-A6: ADPCM-B:1) ----------- */
/*TODO*///void YM2610BUpdateOne(int num, INT16 **buffer, int length)
/*TODO*///{
/*TODO*///	YM2610 *F2610 = &(FM2610[num]);
/*TODO*///	FM_OPN *OPN   = &(FM2610[num].OPN);
/*TODO*///	YM_DELTAT *DELTAT = &(FM2610[num].deltaT);
/*TODO*///	int i,j;
/*TODO*///	FM_CH *ch;
/*TODO*///	FMSAMPLE  *bufL,*bufR;
/*TODO*///
/*TODO*///	/* setup DELTA-T unit */
/*TODO*///	YM_DELTAT_DECODE_PRESET(DELTAT);
/*TODO*///	/* buffer setup */
/*TODO*///	bufL = buffer[0];
/*TODO*///	bufR = buffer[1];
/*TODO*///
/*TODO*///	if( (void *)F2610 != cur_chip ){
/*TODO*///		cur_chip = (void *)F2610;
/*TODO*///		State = &OPN->ST;
/*TODO*///		cch[0] = &F2610->CH[0];
/*TODO*///		cch[1] = &F2610->CH[1];
/*TODO*///		cch[2] = &F2610->CH[2];
/*TODO*///		cch[3] = &F2610->CH[3];
/*TODO*///		cch[4] = &F2610->CH[4];
/*TODO*///		cch[5] = &F2610->CH[5];
/*TODO*///		/* setup adpcm rom address */
/*TODO*///		pcmbufA  = F2610->pcmbuf;
/*TODO*///		pcmsizeA = F2610->pcm_size;
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		LFOCnt  = OPN->LFOCnt;
/*TODO*///		LFOIncr = OPN->LFOIncr;
/*TODO*///		if( !LFOIncr ) lfo_amd = lfo_pmd = 0;
/*TODO*///		LFO_wave = OPN->LFO_wave;
/*TODO*///#endif
/*TODO*///	}
/*TODO*///
/*TODO*///	/* update frequency counter */
/*TODO*///	CALC_FCOUNT( cch[0] );
/*TODO*///	CALC_FCOUNT( cch[1] );
/*TODO*///	if( (State->mode & 0xc0) ){
/*TODO*///		/* 3SLOT MODE */
/*TODO*///		if( cch[2]->SLOT[SLOT1].Incr==-1){
/*TODO*///			/* 3 slot mode */
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT1] , OPN->SL3.fc[1] , OPN->SL3.kcode[1] );
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT2] , OPN->SL3.fc[2] , OPN->SL3.kcode[2] );
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT3] , OPN->SL3.fc[0] , OPN->SL3.kcode[0] );
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT4] , cch[2]->fc , cch[2]->kcode );
/*TODO*///		}
/*TODO*///	}else CALC_FCOUNT( cch[2] );
/*TODO*///	CALC_FCOUNT( cch[3] );
/*TODO*///	CALC_FCOUNT( cch[4] );
/*TODO*///	CALC_FCOUNT( cch[5] );
/*TODO*///
/*TODO*///	/* buffering */
/*TODO*///    for( i=0; i < length ; i++ )
/*TODO*///	{
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		/* LFO */
/*TODO*///		if( LFOIncr )
/*TODO*///		{
/*TODO*///			lfo_amd = LFO_wave[(LFOCnt+=LFOIncr)>>LFO_SHIFT];
/*TODO*///			lfo_pmd = lfo_amd-(LFO_RATE/2);
/*TODO*///		}
/*TODO*///#endif
/*TODO*///		/* clear output acc. */
/*TODO*///		out_ch[OUTD_LEFT] = out_ch[OUTD_RIGHT]= out_ch[OUTD_CENTER] = 0;
/*TODO*///		/**** deltaT ADPCM ****/
/*TODO*///		if( DELTAT->flag )
/*TODO*///			YM_DELTAT_ADPCM_CALC(DELTAT);
/*TODO*///		/* FM */
/*TODO*///		for(ch = cch[0] ; ch <= cch[5] ; ch++)
/*TODO*///			FM_CALC_CH( ch );
/*TODO*///		for( j = 0; j < 6; j++ )
/*TODO*///		{
/*TODO*///			/**** ADPCM ****/
/*TODO*///			if( F2610->adpcm[j].flag )
/*TODO*///				OPNB_ADPCM_CALC_CHA( F2610, &F2610->adpcm[j]);
/*TODO*///		}
/*TODO*///		/* buffering */
/*TODO*///		FM_BUFFERING_STEREO;
/*TODO*///		/* timer A controll */
/*TODO*///		INTERNAL_TIMER_A( State , cch[2] )
/*TODO*///	}
/*TODO*///	INTERNAL_TIMER_B(State,length)
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///	OPN->LFOCnt = LFOCnt;
/*TODO*///#endif
/*TODO*///}
/*TODO*///#endif /* BUILD_YM2610B */
/*TODO*///
/*TODO*///#if BUILD_OPNB
/*TODO*///int YM2610Init(int num, int clock, int rate,
/*TODO*///               void **pcmroma,int *pcmsizea,void **pcmromb,int *pcmsizeb,
/*TODO*///               FM_TIMERHANDLER TimerHandler,FM_IRQHANDLER IRQHandler)
/*TODO*///
/*TODO*///{
/*TODO*///	int i;
/*TODO*///
/*TODO*///    if (FM2610) return (-1);	/* duplicate init. */
/*TODO*///    cur_chip = NULL;	/* hiro-shi!! */
/*TODO*///
/*TODO*///	YM2610NumChips = num;
/*TODO*///
/*TODO*///	/* allocate extend state space */
/*TODO*///	if( (FM2610 = (YM2610 *)malloc(sizeof(YM2610) * YM2610NumChips))==NULL)
/*TODO*///		return (-1);
/*TODO*///	/* clear */
/*TODO*///	memset(FM2610,0,sizeof(YM2610) * YM2610NumChips);
/*TODO*///	/* allocate total level table (128kb space) */
/*TODO*///	if( !FMInitTable() )
/*TODO*///	{
/*TODO*///		free( FM2610 );
/*TODO*///		return (-1);
/*TODO*///	}
/*TODO*///
/*TODO*///	for ( i = 0 ; i < YM2610NumChips; i++ ) {
/*TODO*///		/* FM */
/*TODO*///		FM2610[i].OPN.ST.index = i;
/*TODO*///		FM2610[i].OPN.type = TYPE_YM2610;
/*TODO*///		FM2610[i].OPN.P_CH = FM2610[i].CH;
/*TODO*///		FM2610[i].OPN.ST.clock = clock;
/*TODO*///		FM2610[i].OPN.ST.rate = rate;
/*TODO*///		/* FM2610[i].OPN.ST.irq = 0; */
/*TODO*///		/* FM2610[i].OPN.ST.status = 0; */
/*TODO*///		FM2610[i].OPN.ST.timermodel = FM_TIMER_INTERVAL;
/*TODO*///		/* Extend handler */
/*TODO*///		FM2610[i].OPN.ST.Timer_Handler = TimerHandler;
/*TODO*///		FM2610[i].OPN.ST.IRQ_Handler   = IRQHandler;
/*TODO*///		/* ADPCM */
/*TODO*///		FM2610[i].pcmbuf   = (UINT8 *)(pcmroma[i]);
/*TODO*///		FM2610[i].pcm_size = pcmsizea[i];
/*TODO*///		/* DELTA-T */
/*TODO*///		FM2610[i].deltaT.memory = (UINT8 *)(pcmromb[i]);
/*TODO*///		FM2610[i].deltaT.memory_size = pcmsizeb[i];
/*TODO*///		/* */
/*TODO*///		YM2610ResetChip(i);
/*TODO*///	}
/*TODO*///	InitOPNB_ADPCMATable();
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////* ---------- shut down emurator ----------- */
/*TODO*///void YM2610Shutdown()
/*TODO*///{
/*TODO*///    if (!FM2610) return;
/*TODO*///
/*TODO*///	FMCloseTable();
/*TODO*///	free(FM2610);
/*TODO*///	FM2610 = NULL;
/*TODO*///}
/*TODO*///
/*TODO*////* ---------- reset one of chip ---------- */
/*TODO*///void YM2610ResetChip(int num)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///	YM2610 *F2610 = &(FM2610[num]);
/*TODO*///	FM_OPN *OPN   = &(FM2610[num].OPN);
/*TODO*///	YM_DELTAT *DELTAT = &(FM2610[num].deltaT);
/*TODO*///
/*TODO*///	/* Reset Priscaler */
/*TODO*///	OPNSetPris( OPN, 6*24, 6*24, 4*2); /* OPN 1/6 , SSG 1/4 */
/*TODO*///	/* reset SSG section */
/*TODO*///	SSGReset(OPN->ST.index);
/*TODO*///	/* status clear */
/*TODO*///	FM_IRQMASK_SET(&OPN->ST,0x03);
/*TODO*///	OPNWriteMode(OPN,0x27,0x30); /* mode 0 , timer reset */
/*TODO*///
/*TODO*///	reset_channel( &OPN->ST , F2610->CH , 6 );
/*TODO*///	/* reset OPerator paramater */
/*TODO*///	for(i = 0xb6 ; i >= 0xb4 ; i-- )
/*TODO*///	{
/*TODO*///		OPNWriteReg(OPN,i      ,0xc0);
/*TODO*///		OPNWriteReg(OPN,i|0x100,0xc0);
/*TODO*///	}
/*TODO*///	for(i = 0xb2 ; i >= 0x30 ; i-- )
/*TODO*///	{
/*TODO*///		OPNWriteReg(OPN,i      ,0);
/*TODO*///		OPNWriteReg(OPN,i|0x100,0);
/*TODO*///	}
/*TODO*///	for(i = 0x26 ; i >= 0x20 ; i-- ) OPNWriteReg(OPN,i,0);
/*TODO*///	/**** ADPCM work initial ****/
/*TODO*///	for( i = 0; i < 6+1; i++ ){
/*TODO*///		F2610->adpcm[i].now_addr  = 0;
/*TODO*///		F2610->adpcm[i].now_step  = 0;
/*TODO*///		F2610->adpcm[i].step      = 0;
/*TODO*///		F2610->adpcm[i].start     = 0;
/*TODO*///		F2610->adpcm[i].end       = 0;
/*TODO*///		/* F2610->adpcm[i].delta     = 21866; */
/*TODO*///		F2610->adpcm[i].volume    = 0;
/*TODO*///		F2610->adpcm[i].pan       = &out_ch[OUTD_CENTER]; /* default center */
/*TODO*///		F2610->adpcm[i].flagMask  = (i == 6) ? 0x80 : (1<<i);
/*TODO*///		F2610->adpcm[i].flag      = 0;
/*TODO*///		F2610->adpcm[i].adpcmx    = 0;
/*TODO*///		F2610->adpcm[i].adpcmd    = 127;
/*TODO*///		F2610->adpcm[i].adpcml    = 0;
/*TODO*///	}
/*TODO*///	F2610->adpcmTL = &(TL_TABLE[0x3f*(int)(0.75/EG_STEP)]);
/*TODO*///	/* F2610->port1state = -1; */
/*TODO*///	F2610->adpcm_arrivedEndAddress = 0;
/*TODO*///
/*TODO*///	/* DELTA-T unit */
/*TODO*///	DELTAT->freqbase = OPN->ST.freqbase;
/*TODO*///	DELTAT->output_pointer = out_ch;
/*TODO*///	DELTAT->portshift = 8;		/* allways 8bits shift */
/*TODO*///	DELTAT->output_range = DELTAT_MIXING_LEVEL<<TL_BITS;
/*TODO*///	YM_DELTAT_ADPCM_Reset(DELTAT,OUTD_CENTER);
/*TODO*///}
/*TODO*///
/*TODO*////* YM2610 write */
/*TODO*////* n = number  */
/*TODO*////* a = address */
/*TODO*////* v = value   */
/*TODO*///int YM2610Write(int n, int a,UINT8 v)
/*TODO*///{
/*TODO*///	YM2610 *F2610 = &(FM2610[n]);
/*TODO*///	FM_OPN *OPN   = &(FM2610[n].OPN);
/*TODO*///	int addr;
/*TODO*///	int ch;
/*TODO*///
/*TODO*///	switch( a&3 ){
/*TODO*///	case 0:	/* address port 0 */
/*TODO*///		OPN->ST.address = v & 0xff;
/*TODO*///		/* Write register to SSG emurator */
/*TODO*///		if( v < 16 ) SSGWrite(n,0,v);
/*TODO*///		break;
/*TODO*///	case 1:	/* data port 0    */
/*TODO*///		addr = OPN->ST.address;
/*TODO*///		switch(addr & 0xf0)
/*TODO*///		{
/*TODO*///		case 0x00:	/* SSG section */
/*TODO*///			/* Write data to SSG emurator */
/*TODO*///			SSGWrite(n,a,v);
/*TODO*///			break;
/*TODO*///		case 0x10: /* DeltaT ADPCM */
/*TODO*///			YM2610UpdateReq(n);
/*TODO*///			switch(addr)
/*TODO*///			{
/*TODO*///			case 0x1c: /*  FLAG CONTROL : Extend Status Clear/Mask */
/*TODO*///			{
/*TODO*///				UINT8 statusmask = ~v;
/*TODO*///				/* set arrived flag mask */
/*TODO*///				for(ch=0;ch<6;ch++)
/*TODO*///					F2610->adpcm[ch].flagMask = statusmask&(1<<ch);
/*TODO*///				F2610->deltaT.flagMask = statusmask&0x80;
/*TODO*///				/* clear arrived flag */
/*TODO*///				F2610->adpcm_arrivedEndAddress &= statusmask&0x3f;
/*TODO*///				F2610->deltaT.arrivedFlag      &= F2610->deltaT.flagMask;
/*TODO*///			}
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				/* 0x10-0x1b */
/*TODO*///				YM_DELTAT_ADPCM_Write(&F2610->deltaT,addr-0x10,v);
/*TODO*///			}
/*TODO*///			break;
/*TODO*///		case 0x20:	/* Mode Register */
/*TODO*///			YM2610UpdateReq(n);
/*TODO*///			OPNWriteMode(OPN,addr,v);
/*TODO*///			break;
/*TODO*///		default:	/* OPN section */
/*TODO*///			YM2610UpdateReq(n);
/*TODO*///			/* write register */
/*TODO*///			 OPNWriteReg(OPN,addr,v);
/*TODO*///		}
/*TODO*///		break;
/*TODO*///	case 2:	/* address port 1 */
/*TODO*///		F2610->address1 = v & 0xff;
/*TODO*///		break;
/*TODO*///	case 3:	/* data port 1    */
/*TODO*///		YM2610UpdateReq(n);
/*TODO*///		addr = F2610->address1;
/*TODO*///		if( addr < 0x30 )
/*TODO*///			/* 100-12f : ADPCM A section */
/*TODO*///			FM_ADPCMAWrite(F2610,addr,v);
/*TODO*///		else
/*TODO*///			OPNWriteReg(OPN,addr|0x100,v);
/*TODO*///	}
/*TODO*///	return OPN->ST.irq;
/*TODO*///}
/*TODO*///UINT8 YM2610Read(int n,int a)
/*TODO*///{
/*TODO*///	YM2610 *F2610 = &(FM2610[n]);
/*TODO*///	int addr = F2610->OPN.ST.address;
/*TODO*///	UINT8 ret = 0;
/*TODO*///
/*TODO*///	switch( a&3){
/*TODO*///	case 0:	/* status 0 : YM2203 compatible */
/*TODO*///		ret = F2610->OPN.ST.status & 0x83;
/*TODO*///		break;
/*TODO*///	case 1:	/* data 0 */
/*TODO*///		if( addr < 16 ) ret = SSGRead(n);
/*TODO*///		if( addr == 0xff ) ret = 0x01;
/*TODO*///		break;
/*TODO*///	case 2:	/* status 1 : + ADPCM status */
/*TODO*///		/* ADPCM STATUS (arrived End Address) */
/*TODO*///		/* B,--,A5,A4,A3,A2,A1,A0 */
/*TODO*///		/* B     = ADPCM-B(DELTA-T) arrived end address */
/*TODO*///		/* A0-A5 = ADPCM-A          arrived end address */
/*TODO*///		ret = F2610->adpcm_arrivedEndAddress | F2610->deltaT.arrivedFlag;
/*TODO*///#ifdef __RAINE__
/*TODO*///	  //PrintDebug( "YM2610Status2 %02x\n", ret );
/*TODO*///	  //PrintIngame(120,"YM2610Status2 %02x", ret );
/*TODO*///#endif
/*TODO*///		break;
/*TODO*///	case 3:
/*TODO*///		ret = 0;
/*TODO*///		break;
/*TODO*///	}
/*TODO*///	return ret;
/*TODO*///}
/*TODO*///
/*TODO*///int YM2610TimerOver(int n,int c)
/*TODO*///{
/*TODO*///	YM2610 *F2610 = &(FM2610[n]);
/*TODO*///
/*TODO*///	if( c )
/*TODO*///	{	/* Timer B */
/*TODO*///		TimerBOver( &(F2610->OPN.ST) );
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{	/* Timer A */
/*TODO*///		YM2610UpdateReq(n);
/*TODO*///		/* timer update */
/*TODO*///		TimerAOver( &(F2610->OPN.ST) );
/*TODO*///		/* CSM mode key,TL controll */
/*TODO*///		if( F2610->OPN.ST.mode & 0x80 )
/*TODO*///		{	/* CSM mode total level latch and auto key on */
/*TODO*///			CSMKeyControll( &(F2610->CH[2]) );
/*TODO*///		}
/*TODO*///	}
/*TODO*///	return F2610->OPN.ST.irq;
/*TODO*///}
/*TODO*///
/*TODO*///#endif /* BUILD_OPNB */
/*TODO*///
/*TODO*///
/*TODO*///#if BUILD_YM2612
/*TODO*////*******************************************************************************/
/*TODO*////*		YM2612 local section                                                   */
/*TODO*////*******************************************************************************/
/*TODO*////* here's the virtual YM2612 */
/*TODO*///typedef struct ym2612_f {
/*TODO*///	FM_OPN OPN;						/* OPN state       */
/*TODO*///	FM_CH CH[6];					/* channel state */
/*TODO*///	int address1;	/* address register1 */
/*TODO*///	/* dac output (YM2612) */
/*TODO*///	int dacen;
/*TODO*///	int dacout;
/*TODO*///} YM2612;
/*TODO*///
/*TODO*///static int YM2612NumChips;	/* total chip */
/*TODO*///static YM2612 *FM2612=NULL;	/* array of YM2612's */
/*TODO*///
/*TODO*///static int dacen;
/*TODO*///
/*TODO*////* ---------- update one of chip ----------- */
/*TODO*///void YM2612UpdateOne(int num, INT16 **buffer, int length)
/*TODO*///{
/*TODO*///	YM2612 *F2612 = &(FM2612[num]);
/*TODO*///	FM_OPN *OPN   = &(FM2612[num].OPN);
/*TODO*///	int i;
/*TODO*///	FM_CH *ch,*ech;
/*TODO*///	FMSAMPLE  *bufL,*bufR;
/*TODO*///	int dacout  = F2612->dacout;
/*TODO*///
/*TODO*///	/* set bufer */
/*TODO*///	bufL = buffer[0];
/*TODO*///	bufR = buffer[1];
/*TODO*///
/*TODO*///	if( (void *)F2612 != cur_chip ){
/*TODO*///		cur_chip = (void *)F2612;
/*TODO*///
/*TODO*///		State = &OPN->ST;
/*TODO*///		cch[0]   = &F2612->CH[0];
/*TODO*///		cch[1]   = &F2612->CH[1];
/*TODO*///		cch[2]   = &F2612->CH[2];
/*TODO*///		cch[3]   = &F2612->CH[3];
/*TODO*///		cch[4]   = &F2612->CH[4];
/*TODO*///		cch[5]   = &F2612->CH[5];
/*TODO*///		/* DAC mode */
/*TODO*///		dacen = F2612->dacen;
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		LFOCnt  = OPN->LFOCnt;
/*TODO*///		LFOIncr = OPN->LFOIncr;
/*TODO*///		if( !LFOIncr ) lfo_amd = lfo_pmd = 0;
/*TODO*///		LFO_wave = OPN->LFO_wave;
/*TODO*///#endif
/*TODO*///	}
/*TODO*///	/* update frequency counter */
/*TODO*///	CALC_FCOUNT( cch[0] );
/*TODO*///	CALC_FCOUNT( cch[1] );
/*TODO*///	if( (State->mode & 0xc0) ){
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		/* LFO */
/*TODO*///		if( LFOIncr )
/*TODO*///		{
/*TODO*///			lfo_amd = LFO_wave[(LFOCnt+=LFOIncr)>>LFO_SHIFT];
/*TODO*///			lfo_pmd = lfo_amd-(LFO_RATE/2);
/*TODO*///		}
/*TODO*///#endif
/*TODO*///		/* 3SLOT MODE */
/*TODO*///		if( cch[2]->SLOT[SLOT1].Incr==-1){
/*TODO*///			/* 3 slot mode */
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT1] , OPN->SL3.fc[1] , OPN->SL3.kcode[1] );
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT2] , OPN->SL3.fc[2] , OPN->SL3.kcode[2] );
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT3] , OPN->SL3.fc[0] , OPN->SL3.kcode[0] );
/*TODO*///			CALC_FCSLOT(&cch[2]->SLOT[SLOT4] , cch[2]->fc , cch[2]->kcode );
/*TODO*///		}
/*TODO*///	}else CALC_FCOUNT( cch[2] );
/*TODO*///	CALC_FCOUNT( cch[3] );
/*TODO*///	CALC_FCOUNT( cch[4] );
/*TODO*///	CALC_FCOUNT( cch[5] );
/*TODO*///
/*TODO*///	ech = dacen ? cch[4] : cch[5];
/*TODO*///	/* buffering */
/*TODO*///    for( i=0; i < length ; i++ )
/*TODO*///	{
/*TODO*///		/* clear output acc. */
/*TODO*///		out_ch[OUTD_LEFT] = out_ch[OUTD_RIGHT]= out_ch[OUTD_CENTER] = 0;
/*TODO*///		/* calcrate channel output */
/*TODO*///		for(ch = cch[0] ; ch <= ech ; ch++)
/*TODO*///			FM_CALC_CH( ch );
/*TODO*///		if( dacen )  *cch[5]->connect4 += dacout;
/*TODO*///		/* buffering */
/*TODO*///		FM_BUFFERING_STEREO;
/*TODO*///		/* timer A controll */
/*TODO*///		INTERNAL_TIMER_A( State , cch[2] )
/*TODO*///	}
/*TODO*///	INTERNAL_TIMER_B(State,length)
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///	OPN->LFOCnt = LFOCnt;
/*TODO*///#endif
/*TODO*///}
/*TODO*///
/*TODO*////* -------------------------- YM2612 ---------------------------------- */
/*TODO*///int YM2612Init(int num, int clock, int rate,
/*TODO*///               FM_TIMERHANDLER TimerHandler,FM_IRQHANDLER IRQHandler)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///
/*TODO*///    if (FM2612) return (-1);	/* duplicate init. */
/*TODO*///    cur_chip = NULL;	/* hiro-shi!! */
/*TODO*///
/*TODO*///	YM2612NumChips = num;
/*TODO*///
/*TODO*///	/* allocate extend state space */
/*TODO*///	if( (FM2612 = (YM2612 *)malloc(sizeof(YM2612) * YM2612NumChips))==NULL)
/*TODO*///		return (-1);
/*TODO*///	/* clear */
/*TODO*///	memset(FM2612,0,sizeof(YM2612) * YM2612NumChips);
/*TODO*///	/* allocate total level table (128kb space) */
/*TODO*///	if( !FMInitTable() )
/*TODO*///	{
/*TODO*///		free( FM2612 );
/*TODO*///		return (-1);
/*TODO*///	}
/*TODO*///
/*TODO*///	for ( i = 0 ; i < YM2612NumChips; i++ ) {
/*TODO*///		FM2612[i].OPN.ST.index = i;
/*TODO*///		FM2612[i].OPN.type = TYPE_YM2612;
/*TODO*///		FM2612[i].OPN.P_CH = FM2612[i].CH;
/*TODO*///		FM2612[i].OPN.ST.clock = clock;
/*TODO*///		FM2612[i].OPN.ST.rate = rate;
/*TODO*///		/* FM2612[i].OPN.ST.irq = 0; */
/*TODO*///		/* FM2612[i].OPN.ST.status = 0; */
/*TODO*///		FM2612[i].OPN.ST.timermodel = FM_TIMER_INTERVAL;
/*TODO*///		/* Extend handler */
/*TODO*///		FM2612[i].OPN.ST.Timer_Handler = TimerHandler;
/*TODO*///		FM2612[i].OPN.ST.IRQ_Handler   = IRQHandler;
/*TODO*///		YM2612ResetChip(i);
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////* ---------- shut down emurator ----------- */
/*TODO*///void YM2612Shutdown()
/*TODO*///{
/*TODO*///    if (!FM2612) return;
/*TODO*///
/*TODO*///	FMCloseTable();
/*TODO*///	free(FM2612);
/*TODO*///	FM2612 = NULL;
/*TODO*///}
/*TODO*///
/*TODO*////* ---------- reset one of chip ---------- */
/*TODO*///void YM2612ResetChip(int num)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///	YM2612 *F2612 = &(FM2612[num]);
/*TODO*///	FM_OPN *OPN   = &(FM2612[num].OPN);
/*TODO*///
/*TODO*///	OPNSetPris( OPN , 12*12, 12*12, 0);
/*TODO*///	/* status clear */
/*TODO*///	FM_IRQMASK_SET(&OPN->ST,0x03);
/*TODO*///	OPNWriteMode(OPN,0x27,0x30); /* mode 0 , timer reset */
/*TODO*///
/*TODO*///	reset_channel( &OPN->ST , &F2612->CH[0] , 6 );
/*TODO*///
/*TODO*///	for(i = 0xb6 ; i >= 0xb4 ; i-- )
/*TODO*///	{
/*TODO*///		OPNWriteReg(OPN,i      ,0xc0);
/*TODO*///		OPNWriteReg(OPN,i|0x100,0xc0);
/*TODO*///	}
/*TODO*///	for(i = 0xb2 ; i >= 0x30 ; i-- )
/*TODO*///	{
/*TODO*///		OPNWriteReg(OPN,i      ,0);
/*TODO*///		OPNWriteReg(OPN,i|0x100,0);
/*TODO*///	}
/*TODO*///	for(i = 0x26 ; i >= 0x20 ; i-- ) OPNWriteReg(OPN,i,0);
/*TODO*///	/* DAC mode clear */
/*TODO*///	F2612->dacen = 0;
/*TODO*///}
/*TODO*///
/*TODO*////* YM2612 write */
/*TODO*////* n = number  */
/*TODO*////* a = address */
/*TODO*////* v = value   */
/*TODO*///int YM2612Write(int n, int a,UINT8 v)
/*TODO*///{
/*TODO*///	YM2612 *F2612 = &(FM2612[n]);
/*TODO*///	int addr;
/*TODO*///
/*TODO*///	switch( a&3){
/*TODO*///	case 0:	/* address port 0 */
/*TODO*///		F2612->OPN.ST.address = v & 0xff;
/*TODO*///		break;
/*TODO*///	case 1:	/* data port 0    */
/*TODO*///		addr = F2612->OPN.ST.address;
/*TODO*///		switch( addr & 0xf0 )
/*TODO*///		{
/*TODO*///		case 0x20:	/* 0x20-0x2f Mode */
/*TODO*///			switch( addr )
/*TODO*///			{
/*TODO*///			case 0x2a:	/* DAC data (YM2612) */
/*TODO*///				YM2612UpdateReq(n);
/*TODO*///				F2612->dacout = ((int)v - 0x80)<<(TL_BITS-7);
/*TODO*///				break;
/*TODO*///			case 0x2b:	/* DAC Sel  (YM2612) */
/*TODO*///				/* b7 = dac enable */
/*TODO*///				F2612->dacen = v & 0x80;
/*TODO*///				cur_chip = NULL;
/*TODO*///				break;
/*TODO*///			default:	/* OPN section */
/*TODO*///				YM2612UpdateReq(n);
/*TODO*///				/* write register */
/*TODO*///				 OPNWriteMode(&(F2612->OPN),addr,v);
/*TODO*///			}
/*TODO*///			break;
/*TODO*///		default:	/* 0x30-0xff OPN section */
/*TODO*///			YM2612UpdateReq(n);
/*TODO*///			/* write register */
/*TODO*///			 OPNWriteReg(&(F2612->OPN),addr,v);
/*TODO*///		}
/*TODO*///		break;
/*TODO*///	case 2:	/* address port 1 */
/*TODO*///		F2612->address1 = v & 0xff;
/*TODO*///		break;
/*TODO*///	case 3:	/* data port 1    */
/*TODO*///		addr = F2612->address1;
/*TODO*///		YM2612UpdateReq(n);
/*TODO*///		OPNWriteReg(&(F2612->OPN),addr|0x100,v);
/*TODO*///		break;
/*TODO*///	}
/*TODO*///	return F2612->OPN.ST.irq;
/*TODO*///}
/*TODO*///UINT8 YM2612Read(int n,int a)
/*TODO*///{
/*TODO*///	YM2612 *F2612 = &(FM2612[n]);
/*TODO*///
/*TODO*///	switch( a&3){
/*TODO*///	case 0:	/* status 0 */
/*TODO*///		return F2612->OPN.ST.status;
/*TODO*///	case 1:
/*TODO*///	case 2:
/*TODO*///	case 3:
/*TODO*///		Log(LOG_WAR,"YM2612 #%d:A=%d read unmapped area\n");
/*TODO*///		return F2612->OPN.ST.status;
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///int YM2612TimerOver(int n,int c)
/*TODO*///{
/*TODO*///	YM2612 *F2612 = &(FM2612[n]);
/*TODO*///
/*TODO*///	if( c )
/*TODO*///	{	/* Timer B */
/*TODO*///		TimerBOver( &(F2612->OPN.ST) );
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{	/* Timer A */
/*TODO*///		YM2612UpdateReq(n);
/*TODO*///		/* timer update */
/*TODO*///		TimerAOver( &(F2612->OPN.ST) );
/*TODO*///		/* CSM mode key,TL controll */
/*TODO*///		if( F2612->OPN.ST.mode & 0x80 )
/*TODO*///		{	/* CSM mode total level latch and auto key on */
/*TODO*///			CSMKeyControll( &(F2612->CH[2]) );
/*TODO*///		}
/*TODO*///	}
/*TODO*///	return F2612->OPN.ST.irq;
/*TODO*///}
/*TODO*///
/*TODO*///#endif /* BUILD_YM2612 */
/*TODO*///
/*TODO*///
/*TODO*///#if BUILD_YM2151
/*TODO*////*******************************************************************************/
/*TODO*////*		YM2151 local section                                                   */
/*TODO*////*******************************************************************************/
/*TODO*////* -------------------------- OPM ---------------------------------- */
/*TODO*///#undef  FM_SEG_SUPPORT
/*TODO*///#define FM_SEG_SUPPORT 0	/* OPM has not SEG type envelope */
/*TODO*///
/*TODO*////* here's the virtual YM2151(OPM)  */
/*TODO*///typedef struct ym2151_f {
/*TODO*///	FM_ST ST;					/* general state     */
/*TODO*///	FM_CH CH[8];				/* channel state     */
/*TODO*///	UINT8 ct;					/* CT0,1             */
/*TODO*///	UINT32 NoiseCnt;			/* noise generator   */
/*TODO*///	UINT32 NoiseIncr;			/* noise mode enable & step */
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///	/* LFO */
/*TODO*///	UINT32 LFOCnt;
/*TODO*///	UINT32 LFOIncr;
/*TODO*///	UINT8 pmd;					/* LFO pmd level     */
/*TODO*///	UINT8 amd;					/* LFO amd level     */
/*TODO*///	INT32 *wavetype;			/* LFO waveform      */
/*TODO*///	INT32 LFO_wave[LFO_ENT*4];	/* LFO wave tabel    */
/*TODO*///	UINT8 testreg;				/* test register (LFO reset) */
/*TODO*///#endif
/*TODO*///	UINT32 KC_TABLE[8*12*64+950];/* keycode,keyfunction -> count */
/*TODO*///	void (*PortWrite)(int offset,int data);/*  callback when write CT0/CT1 */
/*TODO*///} YM2151;
/*TODO*///
/*TODO*///static YM2151 *FMOPM=NULL;	/* array of YM2151's */
/*TODO*///static int YM2151NumChips;	/* total chip */
/*TODO*///
/*TODO*////* current chip state */
/*TODO*///static UINT32 NoiseCnt , NoiseIncr;
/*TODO*///
/*TODO*///static INT32 *NOISE_TABLE[SIN_ENT];
/*TODO*///
/*TODO*///static const int DT2_TABLE[4]={ /* 4 DT2 values */
/*TODO*////*
/*TODO*/// *   DT2 defines offset in cents from base note
/*TODO*/// *
/*TODO*/// *   The table below defines offset in deltas table...
/*TODO*/// *   User's Manual page 22
/*TODO*/// *   Values below were calculated using formula:  value = orig.val * 1.5625
/*TODO*/// *
/*TODO*/// * DT2=0 DT2=1 DT2=2 DT2=3
/*TODO*/// * 0     600   781   950
/*TODO*/// */
/*TODO*///	0,    384,  500,  608
/*TODO*///};
/*TODO*///
/*TODO*///static const int KC_TO_SEMITONE[16]={
/*TODO*///	/*translate note code KC into more usable number of semitone*/
/*TODO*///	0*64, 1*64, 2*64, 3*64,
/*TODO*///	3*64, 4*64, 5*64, 6*64,
/*TODO*///	6*64, 7*64, 8*64, 9*64,
/*TODO*///	9*64,10*64,11*64,12*64
/*TODO*///};
/*TODO*///
/*TODO*////* ---------- frequency counter  ---------- */
/*TODO*///INLINE void OPM_CALC_FCOUNT(YM2151 *OPM , FM_CH *CH )
/*TODO*///{
/*TODO*///	if( CH->SLOT[SLOT1].Incr==-1)
/*TODO*///	{
/*TODO*///		int fc = CH->fc;
/*TODO*///		int kc = CH->kcode;
/*TODO*///
/*TODO*///		CALC_FCSLOT(&CH->SLOT[SLOT1] , OPM->KC_TABLE[fc + CH->SLOT[SLOT1].DT2] , kc );
/*TODO*///		CALC_FCSLOT(&CH->SLOT[SLOT2] , OPM->KC_TABLE[fc + CH->SLOT[SLOT2].DT2] , kc );
/*TODO*///		CALC_FCSLOT(&CH->SLOT[SLOT3] , OPM->KC_TABLE[fc + CH->SLOT[SLOT3].DT2] , kc );
/*TODO*///		CALC_FCSLOT(&CH->SLOT[SLOT4] , OPM->KC_TABLE[fc + CH->SLOT[SLOT4].DT2] , kc );
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* ---------- calcrate one of channel7 ---------- */
/*TODO*///INLINE void OPM_CALC_CH7( FM_CH *CH )
/*TODO*///{
/*TODO*///	UINT32 eg_out1,eg_out2,eg_out3,eg_out4;  //envelope output
/*TODO*///
/*TODO*///	/* Phase Generator */
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///	INT32 pms = lfo_pmd * CH->pms / LFO_RATE;
/*TODO*///	if(pms)
/*TODO*///	{
/*TODO*///		pg_in1 = (CH->SLOT[SLOT1].Cnt += CH->SLOT[SLOT1].Incr + (INT32)(pms * CH->SLOT[SLOT1].Incr) / PMS_RATE);
/*TODO*///		pg_in2 = (CH->SLOT[SLOT2].Cnt += CH->SLOT[SLOT2].Incr + (INT32)(pms * CH->SLOT[SLOT2].Incr) / PMS_RATE);
/*TODO*///		pg_in3 = (CH->SLOT[SLOT3].Cnt += CH->SLOT[SLOT3].Incr + (INT32)(pms * CH->SLOT[SLOT3].Incr) / PMS_RATE);
/*TODO*///		pg_in4 = (CH->SLOT[SLOT4].Cnt += CH->SLOT[SLOT4].Incr + (INT32)(pms * CH->SLOT[SLOT4].Incr) / PMS_RATE);
/*TODO*///	}
/*TODO*///	else
/*TODO*///#endif
/*TODO*///	{
/*TODO*///		pg_in1 = (CH->SLOT[SLOT1].Cnt += CH->SLOT[SLOT1].Incr);
/*TODO*///		pg_in2 = (CH->SLOT[SLOT2].Cnt += CH->SLOT[SLOT2].Incr);
/*TODO*///		pg_in3 = (CH->SLOT[SLOT3].Cnt += CH->SLOT[SLOT3].Incr);
/*TODO*///		pg_in4 = (CH->SLOT[SLOT4].Cnt += CH->SLOT[SLOT4].Incr);
/*TODO*///	}
/*TODO*///	/* Envelope Generator */
/*TODO*///	FM_CALC_EG(eg_out1,CH->SLOT[SLOT1]);
/*TODO*///	FM_CALC_EG(eg_out2,CH->SLOT[SLOT2]);
/*TODO*///	FM_CALC_EG(eg_out3,CH->SLOT[SLOT3]);
/*TODO*///	FM_CALC_EG(eg_out4,CH->SLOT[SLOT4]);
/*TODO*///
/*TODO*///	/* connection */
/*TODO*///	if( eg_out1 < EG_CUT_OFF )	/* SLOT 1 */
/*TODO*///	{
/*TODO*///		if( CH->FB ){
/*TODO*///			/* with self feed back */
/*TODO*///			pg_in1 += (CH->op1_out[0]+CH->op1_out[1])>>CH->FB;
/*TODO*///			CH->op1_out[1] = CH->op1_out[0];
/*TODO*///		}
/*TODO*///		CH->op1_out[0] = OP_OUT(pg_in1,eg_out1);
/*TODO*///		/* output slot1 */
/*TODO*///		if( !CH->connect1 )
/*TODO*///		{
/*TODO*///			/* algorythm 5  */
/*TODO*///			pg_in2 += CH->op1_out[0];
/*TODO*///			pg_in3 += CH->op1_out[0];
/*TODO*///			pg_in4 += CH->op1_out[0];
/*TODO*///		}else{
/*TODO*///			/* other algorythm */
/*TODO*///			*CH->connect1 += CH->op1_out[0];
/*TODO*///		}
/*TODO*///	}
/*TODO*///	if( eg_out2 < EG_CUT_OFF )	/* SLOT 2 */
/*TODO*///		*CH->connect2 += OP_OUT(pg_in2,eg_out2);
/*TODO*///	if( eg_out3 < EG_CUT_OFF )	/* SLOT 3 */
/*TODO*///		*CH->connect3 += OP_OUT(pg_in3,eg_out3);
/*TODO*///	/* SLOT 4 */
/*TODO*///	if(NoiseIncr)
/*TODO*///	{
/*TODO*///		NoiseCnt += NoiseIncr;
/*TODO*///		if( eg_out4 < EG_CUT_OFF )
/*TODO*///			*CH->connect4 += OP_OUTN(NoiseCnt,eg_out4);
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		if( eg_out4 < EG_CUT_OFF )
/*TODO*///			*CH->connect4 += OP_OUT(pg_in4,eg_out4);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* ---------- priscaler set(and make time tables) ---------- */
/*TODO*///static void OPMInitTable( int num )
/*TODO*///{
/*TODO*///    YM2151 *OPM = &(FMOPM[num]);
/*TODO*///	int i;
/*TODO*///	double pom;
/*TODO*///	double rate;
/*TODO*///
/*TODO*///	if (FMOPM[num].ST.rate)
/*TODO*///		rate = (double)(1<<FREQ_BITS) / (3579545.0 / FMOPM[num].ST.clock * FMOPM[num].ST.rate);
/*TODO*///	else rate = 1;
/*TODO*///
/*TODO*///	for (i=0; i<8*12*64+950; i++)
/*TODO*///	{
/*TODO*///		/* This calculation type was used from the Jarek's YM2151 emulator */
/*TODO*///		pom = 6.875 * pow (2, ((i+4*64)*1.5625/1200.0) ); /*13.75Hz is note A 12semitones below A-0, so D#0 is 4 semitones above then*/
/*TODO*///		/*calculate phase increment for above precounted Hertz value*/
/*TODO*///		OPM->KC_TABLE[i] = (UINT32)(pom * rate);
/*TODO*///		/*Log(LOG_WAR,"OPM KC %d = %x\n",i,OPM->KC_TABLE[i]);*/
/*TODO*///	}
/*TODO*///
/*TODO*///	/* make time tables */
/*TODO*///	init_timetables( &OPM->ST , OPM_DTTABLE , OPM_ARRATE , OPM_DRRATE );
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///	/* LFO wave table */
/*TODO*///	for(i=0;i<LFO_ENT;i++)
/*TODO*///	{
/*TODO*///		OPM->LFO_wave[          i]= LFO_RATE * i / LFO_ENT /127;
/*TODO*///		OPM->LFO_wave[LFO_ENT  +i]= ( i<LFO_ENT/2 ? 0 : LFO_RATE )/127;
/*TODO*///		OPM->LFO_wave[LFO_ENT*2+i]= LFO_RATE* (i<LFO_ENT/2 ? i : LFO_ENT-i) /(LFO_ENT/2) /127;
/*TODO*///		OPM->LFO_wave[LFO_ENT*3+i]= LFO_RATE * (rand()&0xff) /256 /127;
/*TODO*///	}
/*TODO*///#endif
/*TODO*///	/* NOISE wave table */
/*TODO*///	for(i=0;i<SIN_ENT;i++)
/*TODO*///	{
/*TODO*///		int sign = rand()&1 ? TL_MAX : 0;
/*TODO*///		int lev = rand()&0x1ff;
/*TODO*///		//pom = lev ? 20*log10(0x200/lev) : 0;   /* decibel */
/*TODO*///		//NOISE_TABLE[i] = &TL_TABLE[sign + (int)(pom / EG_STEP)]; /* TL_TABLE steps */
/*TODO*///		NOISE_TABLE[i] = &TL_TABLE[sign + lev * EG_ENT/0x200]; /* TL_TABLE steps */
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* ---------- write a register on YM2151 chip number 'n' ---------- */
/*TODO*///static void OPMWriteReg(int n, int r, int v)
/*TODO*///{
/*TODO*///	UINT8 c;
/*TODO*///	FM_CH *CH;
/*TODO*///	FM_SLOT *SLOT;
/*TODO*///
/*TODO*///    YM2151 *OPM = &(FMOPM[n]);
/*TODO*///
/*TODO*///	c   = OPM_CHAN(r);
/*TODO*///	CH  = &OPM->CH[c];
/*TODO*///	SLOT= &CH->SLOT[OPM_SLOT(r)];
/*TODO*///
/*TODO*///	switch( r & 0xe0 ){
/*TODO*///	case 0x00: /* 0x00-0x1f */
/*TODO*///		switch( r ){
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		case 0x01:	/* test */
/*TODO*///			if( (OPM->testreg&(OPM->testreg^v))&0x02 ) /* fall eggge */
/*TODO*///			{	/* reset LFO counter */
/*TODO*///				OPM->LFOCnt = 0;
/*TODO*///				cur_chip = NULL;
/*TODO*///			}
/*TODO*///			OPM->testreg = v;
/*TODO*///			break;
/*TODO*///#endif
/*TODO*///		case 0x08:	/* key on / off */
/*TODO*///			c = v&7;
/*TODO*///			/* CSM mode */
/*TODO*///			if( OPM->ST.mode & 0x80 ) break;
/*TODO*///			CH = &OPM->CH[c];
/*TODO*///			if(v&0x08) FM_KEYON(CH,SLOT1); else FM_KEYOFF(CH,SLOT1);
/*TODO*///			if(v&0x10) FM_KEYON(CH,SLOT2); else FM_KEYOFF(CH,SLOT2);
/*TODO*///			if(v&0x20) FM_KEYON(CH,SLOT3); else FM_KEYOFF(CH,SLOT3);
/*TODO*///			if(v&0x40) FM_KEYON(CH,SLOT4); else FM_KEYOFF(CH,SLOT4);
/*TODO*///			break;
/*TODO*///		case 0x0f:	/* Noise freq (ch7.op4) */
/*TODO*///			/* b7 = Noise enable */
/*TODO*///			/* b0-4 noise freq  */
/*TODO*///			OPM->NoiseIncr = !(v&0x80) ? 0 :
/*TODO*///				/* !!!!! unknown noise freqency rate !!!!! */
/*TODO*///				(1<<FREQ_BITS) / 65536 * (v&0x1f) * OPM->ST.freqbase;
/*TODO*///			cur_chip = NULL;
/*TODO*///#if 1
/*TODO*///			if( v & 0x80 ){
/*TODO*///				Log(LOG_WAR,"OPM Noise mode selelted\n");
/*TODO*///			}
/*TODO*///#endif
/*TODO*///			break;
/*TODO*///		case 0x10:	/* timer A High 8*/
/*TODO*///			OPM->ST.TA = (OPM->ST.TA & 0x03)|(((int)v)<<2);
/*TODO*///			break;
/*TODO*///		case 0x11:	/* timer A Low 2*/
/*TODO*///			OPM->ST.TA = (OPM->ST.TA & 0x3fc)|(v&3);
/*TODO*///			break;
/*TODO*///		case 0x12:	/* timer B */
/*TODO*///			OPM->ST.TB = v;
/*TODO*///			break;
/*TODO*///		case 0x14:	/* mode , timer controll */
/*TODO*///			FMSetMode( &(OPM->ST),n,v );
/*TODO*///			break;
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		case 0x18:	/* lfreq   */
/*TODO*///			/* f = fm * 2^(LFRQ/16) / (4295*10^6) */
/*TODO*///			{
/*TODO*///				static double drate[16]={
/*TODO*///					1.0        ,1.044273782,1.090507733,1.138788635, //0-3
/*TODO*///					1.189207115,1.241857812,1.296839555,1.354255547, //4-7
/*TODO*///					1.414213562,1.476826146,1.542210825,1.610490332, //8-11
/*TODO*///					1.681792831,1.75625216 ,1.834008086,1.915206561};
/*TODO*///				double rate = pow(2.0,v/16)*drate[v&0x0f] / 4295000000.0;
/*TODO*///				OPM->LFOIncr = (double)LFO_ENT*(1<<LFO_SHIFT) * (OPM->ST.freqbase*64) * rate;
/*TODO*///				cur_chip = NULL;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///		case 0x19:	/* PMD/AMD */
/*TODO*///			if( v & 0x80 ) OPM->pmd = v & 0x7f;
/*TODO*///			else           OPM->amd = v & 0x7f;
/*TODO*///			break;
/*TODO*///#endif
/*TODO*///		case 0x1b:	/* CT , W  */
/*TODO*///			/* b7 = CT1 */
/*TODO*///			/* b6 = CT0 */
/*TODO*///			/* b0-2 = wave form(LFO) 0=nokogiri,1=houkei,2=sankaku,3=noise */
/*TODO*///			//if(OPM->ct != v)
/*TODO*///			{
/*TODO*///				OPM->ct = v>>6;
/*TODO*///				if( OPM->PortWrite != 0)
/*TODO*///					OPM->PortWrite(0, OPM->ct ); /* bit0 = CT0,bit1 = CT1 */
/*TODO*///			}
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///			if( OPM->wavetype != &OPM->LFO_wave[(v&3)*LFO_ENT])
/*TODO*///			{
/*TODO*///				OPM->wavetype = &OPM->LFO_wave[(v&3)*LFO_ENT];
/*TODO*///				cur_chip = NULL;
/*TODO*///			}
/*TODO*///#endif
/*TODO*///			break;
/*TODO*///		}
/*TODO*///		break;
/*TODO*///	case 0x20:	/* 20-3f */
/*TODO*///		switch( OPM_SLOT(r) ){
/*TODO*///		case 0: /* 0x20-0x27 : RL,FB,CON */
/*TODO*///			{
/*TODO*///				int feedback = (v>>3)&7;
/*TODO*///				CH->ALGO = v&7;
/*TODO*///				CH->FB  = feedback ? 8+1 - feedback : 0;
/*TODO*///				/* RL order -> LR order */
/*TODO*///				CH->PAN = ((v>>7)&1) | ((v>>5)&2);
/*TODO*///				setup_connection( CH );
/*TODO*///			}
/*TODO*///			break;
/*TODO*///		case 1: /* 0x28-0x2f : Keycode */
/*TODO*///			{
/*TODO*///				int blk = (v>>4)&7;
/*TODO*///				/* make keyscale code */
/*TODO*///				CH->kcode = (v>>2)&0x1f;
/*TODO*///				/* make basic increment counter 22bit = 1 cycle */
/*TODO*///				CH->fc = (blk * (12*64)) + KC_TO_SEMITONE[v&0x0f] + CH->fn_h;
/*TODO*///				CH->SLOT[SLOT1].Incr=-1;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///		case 2: /* 0x30-0x37 : Keyfunction */
/*TODO*///			CH->fc -= CH->fn_h;
/*TODO*///			CH->fn_h = v>>2;
/*TODO*///			CH->fc += CH->fn_h;
/*TODO*///			CH->SLOT[SLOT1].Incr=-1;
/*TODO*///			break;
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		case 3: /* 0x38-0x3f : PMS / AMS */
/*TODO*///			/* b0-1 AMS */
/*TODO*///			/* AMS * 23.90625db @ AMD=127 */
/*TODO*///			//CH->ams = (v & 0x03) * (23.90625/EG_STEP);
/*TODO*///			CH->ams = (23.90625/EG_STEP) / (1<<(3-(v&3)));
/*TODO*///			CH->SLOT[SLOT1].ams = CH->ams * CH->SLOT[SLOT1].amon;
/*TODO*///			CH->SLOT[SLOT2].ams = CH->ams * CH->SLOT[SLOT2].amon;
/*TODO*///			CH->SLOT[SLOT3].ams = CH->ams * CH->SLOT[SLOT3].amon;
/*TODO*///			CH->SLOT[SLOT4].ams = CH->ams * CH->SLOT[SLOT4].amon;
/*TODO*///			/* b4-6 PMS */
/*TODO*///			/* 0,5,10,20,50,100,400,700 (cent) @ PMD=127 */
/*TODO*///			{
/*TODO*///				/* 1 octabe = 1200cent = +100%/-50% */
/*TODO*///				/* 100cent  = 1seminote = 6% ?? */
/*TODO*///				static const int pmd_table[8] = {0,5,10,20,50,100,400,700};
/*TODO*///				CH->pms = (1.5/1200.0)*pmd_table[(v>>4) & 0x07] * PMS_RATE;
/*TODO*///			}
/*TODO*///			break;
/*TODO*///#endif
/*TODO*///		}
/*TODO*///		break;
/*TODO*///	case 0x40:	/* DT1,MUL */
/*TODO*///		set_det_mul(&OPM->ST,CH,SLOT,v);
/*TODO*///		break;
/*TODO*///	case 0x60:	/* TL */
/*TODO*///		set_tl(CH,SLOT,v,(c == 7) && (OPM->ST.mode & 0x80) );
/*TODO*///		break;
/*TODO*///	case 0x80:	/* KS, AR */
/*TODO*///		set_ar_ksr(CH,SLOT,v,OPM->ST.AR_TABLE);
/*TODO*///		break;
/*TODO*///	case 0xa0:	/* AMS EN,D1R */
/*TODO*///		set_dr(SLOT,v,OPM->ST.DR_TABLE);
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		/* bit7 = AMS ENABLE */
/*TODO*///		SLOT->amon = v>>7;
/*TODO*///		SLOT->ams = CH->ams * SLOT->amon;
/*TODO*///#endif
/*TODO*///		break;
/*TODO*///	case 0xc0:	/* DT2 ,D2R */
/*TODO*///		SLOT->DT2  = DT2_TABLE[v>>6];
/*TODO*///		CH->SLOT[SLOT1].Incr=-1;
/*TODO*///		set_sr(SLOT,v,OPM->ST.DR_TABLE);
/*TODO*///		break;
/*TODO*///	case 0xe0:	/* D1L, RR */
/*TODO*///		set_sl_rr(SLOT,v,OPM->ST.DR_TABLE);
/*TODO*///		break;
/*TODO*///    }
/*TODO*///}
/*TODO*///
/*TODO*////* ---------- read status port ---------- */
/*TODO*///static UINT8 OPMReadStatus(int n)
/*TODO*///{
/*TODO*///	return FMOPM[n].ST.status;
/*TODO*///}
/*TODO*///
/*TODO*///int YM2151Write(int n,int a,UINT8 v)
/*TODO*///{
/*TODO*///	YM2151 *F2151 = &(FMOPM[n]);
/*TODO*///
/*TODO*///	if( !(a&1) )
/*TODO*///	{	/* address port */
/*TODO*///		F2151->ST.address = v & 0xff;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{	/* data port */
/*TODO*///		int addr = F2151->ST.address;
/*TODO*///		YM2151UpdateReq(n);
/*TODO*///		/* write register */
/*TODO*///		 OPMWriteReg(n,addr,v);
/*TODO*///	}
/*TODO*///	return F2151->ST.irq;
/*TODO*///}
/*TODO*///
/*TODO*////* ---------- reset one of chip ---------- */
/*TODO*///void OPMResetChip(int num)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///    YM2151 *OPM = &(FMOPM[num]);
/*TODO*///
/*TODO*///	OPMInitTable( num );
/*TODO*///	reset_channel( &OPM->ST , &OPM->CH[0] , 8 );
/*TODO*///	/* status clear */
/*TODO*///	FM_IRQMASK_SET(&OPM->ST,0x03);
/*TODO*///	OPMWriteReg(num,0x1b,0x00);
/*TODO*///	/* reset OPerator paramater */
/*TODO*///	for(i = 0xff ; i >= 0x20 ; i-- ) OPMWriteReg(num,i,0);
/*TODO*///}
/*TODO*///
/*TODO*////* ----------  Initialize YM2151 emulator(s) ----------    */
/*TODO*////* 'num' is the number of virtual YM2151's to allocate     */
/*TODO*////* 'rate' is sampling rate and 'bufsiz' is the size of the */
/*TODO*////* buffer that should be updated at each interval          */
/*TODO*///int OPMInit(int num, int clock, int rate,
/*TODO*///               FM_TIMERHANDLER TimerHandler,FM_IRQHANDLER IRQHandler)
/*TODO*///{
/*TODO*///    int i;
/*TODO*///
/*TODO*///    if (FMOPM) return (-1);	/* duplicate init. */
/*TODO*///    cur_chip = NULL;	/* hiro-shi!! */
/*TODO*///
/*TODO*///	YM2151NumChips = num;
/*TODO*///
/*TODO*///	/* allocate ym2151 state space */
/*TODO*///	if( (FMOPM = (YM2151 *)malloc(sizeof(YM2151) * YM2151NumChips))==NULL)
/*TODO*///		return (-1);
/*TODO*///
/*TODO*///	/* clear */
/*TODO*///	memset(FMOPM,0,sizeof(YM2151) * YM2151NumChips);
/*TODO*///
/*TODO*///	/* allocate total lebel table (128kb space) */
/*TODO*///	if( !FMInitTable() )
/*TODO*///	{
/*TODO*///		free( FMOPM );
/*TODO*///		return (-1);
/*TODO*///	}
/*TODO*///	for ( i = 0 ; i < YM2151NumChips; i++ ) {
/*TODO*///		FMOPM[i].ST.index = i;
/*TODO*///		FMOPM[i].ST.clock = clock;
/*TODO*///		FMOPM[i].ST.rate = rate;
/*TODO*///		/* FMOPM[i].ST.irq  = 0; */
/*TODO*///		/* FMOPM[i].ST.status = 0; */
/*TODO*///		FMOPM[i].ST.timermodel = FM_TIMER_INTERVAL;
/*TODO*///		FMOPM[i].ST.freqbase  = rate ? ((double)clock / rate) / 64 : 0;
/*TODO*///		FMOPM[i].ST.TimerBase = 1.0/((double)clock / 64.0);
/*TODO*///		/* Extend handler */
/*TODO*///		FMOPM[i].ST.Timer_Handler = TimerHandler;
/*TODO*///		FMOPM[i].ST.IRQ_Handler   = IRQHandler;
/*TODO*///		/* Reset callback handler of CT0/1 */
/*TODO*///		FMOPM[i].PortWrite = 0;
/*TODO*///		OPMResetChip(i);
/*TODO*///	}
/*TODO*///	return(0);
/*TODO*///}
/*TODO*///
/*TODO*////* ---------- shut down emurator ----------- */
/*TODO*///void OPMShutdown()
/*TODO*///{
/*TODO*///    if (!FMOPM) return;
/*TODO*///
/*TODO*///	FMCloseTable();
/*TODO*///	free(FMOPM);
/*TODO*///	FMOPM = NULL;
/*TODO*///}
/*TODO*///
/*TODO*///UINT8 YM2151Read(int n,int a)
/*TODO*///{
/*TODO*///	if( !(a&1) ) return 0;
/*TODO*///	else         return FMOPM[n].ST.status;
/*TODO*///}
/*TODO*///
/*TODO*////* ---------- make digital sound data ---------- */
/*TODO*///void OPMUpdateOne(int num, INT16 **buffer, int length)
/*TODO*///{
/*TODO*///	YM2151 *OPM = &(FMOPM[num]);
/*TODO*///	int i;
/*TODO*///	int amd,pmd;
/*TODO*///	FM_CH *ch;
/*TODO*///	FMSAMPLE  *bufL,*bufR;
/*TODO*///
/*TODO*///	/* set bufer */
/*TODO*///	bufL = buffer[0];
/*TODO*///	bufR = buffer[1];
/*TODO*///
/*TODO*///	if( (void *)OPM != cur_chip ){
/*TODO*///		cur_chip = (void *)OPM;
/*TODO*///
/*TODO*///		State = &OPM->ST;
/*TODO*///		/* channel pointer */
/*TODO*///		cch[0] = &OPM->CH[0];
/*TODO*///		cch[1] = &OPM->CH[1];
/*TODO*///		cch[2] = &OPM->CH[2];
/*TODO*///		cch[3] = &OPM->CH[3];
/*TODO*///		cch[4] = &OPM->CH[4];
/*TODO*///		cch[5] = &OPM->CH[5];
/*TODO*///		cch[6] = &OPM->CH[6];
/*TODO*///		cch[7] = &OPM->CH[7];
/*TODO*///		/* ch7.op4 noise mode / step */
/*TODO*///		NoiseIncr = OPM->NoiseIncr;
/*TODO*///		NoiseCnt  = OPM->NoiseCnt;
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		/* LFO */
/*TODO*///		LFOCnt  = OPM->LFOCnt;
/*TODO*///		//LFOIncr = OPM->LFOIncr;
/*TODO*///		if( !LFOIncr ) lfo_amd = lfo_pmd = 0;
/*TODO*///		LFO_wave = OPM->wavetype;
/*TODO*///#endif
/*TODO*///	}
/*TODO*///	amd = OPM->amd;
/*TODO*///	pmd = OPM->pmd;
/*TODO*///	if(amd==0 && pmd==0)
/*TODO*///		LFOIncr = 0;
/*TODO*///	else
/*TODO*///		LFOIncr = OPM->LFOIncr;
/*TODO*///
/*TODO*///	OPM_CALC_FCOUNT( OPM , cch[0] );
/*TODO*///	OPM_CALC_FCOUNT( OPM , cch[1] );
/*TODO*///	OPM_CALC_FCOUNT( OPM , cch[2] );
/*TODO*///	OPM_CALC_FCOUNT( OPM , cch[3] );
/*TODO*///	OPM_CALC_FCOUNT( OPM , cch[4] );
/*TODO*///	OPM_CALC_FCOUNT( OPM , cch[5] );
/*TODO*///	OPM_CALC_FCOUNT( OPM , cch[6] );
/*TODO*///	/* CSM check */
/*TODO*///	OPM_CALC_FCOUNT( OPM , cch[7] );
/*TODO*///
/*TODO*///	for( i=0; i < length ; i++ )
/*TODO*///	{
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///		/* LFO */
/*TODO*///		if( LFOIncr )
/*TODO*///		{
/*TODO*///			INT32 depth = LFO_wave[(LFOCnt+=LFOIncr)>>LFO_SHIFT];
/*TODO*///			lfo_amd = depth * amd;
/*TODO*///			lfo_pmd = (depth-(LFO_RATE/127/2)) * pmd;
/*TODO*///		}
/*TODO*///#endif
/*TODO*///		/* clear output acc. */
/*TODO*///		out_ch[OUTD_LEFT] = out_ch[OUTD_RIGHT]= out_ch[OUTD_CENTER] = 0;
/*TODO*///		/* calcrate channel output */
/*TODO*///		for(ch = cch[0] ; ch <= cch[6] ; ch++)
/*TODO*///			FM_CALC_CH( ch );
/*TODO*///		OPM_CALC_CH7( cch[7] );
/*TODO*///		/* buffering */
/*TODO*///		FM_BUFFERING_STEREO;
/*TODO*///		/* timer A controll */
/*TODO*///		INTERNAL_TIMER_A( State , cch[7] )
/*TODO*///    }
/*TODO*///	INTERNAL_TIMER_B(State,length)
/*TODO*///	OPM->NoiseCnt = NoiseCnt;
/*TODO*///#if FM_LFO_SUPPORT
/*TODO*///	OPM->LFOCnt = LFOCnt;
/*TODO*///#endif
/*TODO*///}
/*TODO*///
/*TODO*///void OPMSetPortHander(int n,void (*PortWrite)(int offset,int CT) )
/*TODO*///{
/*TODO*///	FMOPM[n].PortWrite = PortWrite;
/*TODO*///}
/*TODO*///
/*TODO*///int YM2151TimerOver(int n,int c)
/*TODO*///{
/*TODO*///	YM2151 *F2151 = &(FMOPM[n]);
/*TODO*///
/*TODO*///	if( c )
/*TODO*///	{	/* Timer B */
/*TODO*///		TimerBOver( &(F2151->ST) );
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{	/* Timer A */
/*TODO*///		YM2151UpdateReq(n);
/*TODO*///		/* timer update */
/*TODO*///		TimerAOver( &(F2151->ST) );
/*TODO*///		/* CSM mode key,TL controll */
/*TODO*///		if( F2151->ST.mode & 0x80 )
/*TODO*///		{	/* CSM mode total level latch and auto key on */
/*TODO*///			CSMKeyControll( &(F2151->CH[0]) );
/*TODO*///			CSMKeyControll( &(F2151->CH[1]) );
/*TODO*///			CSMKeyControll( &(F2151->CH[2]) );
/*TODO*///			CSMKeyControll( &(F2151->CH[3]) );
/*TODO*///			CSMKeyControll( &(F2151->CH[4]) );
/*TODO*///			CSMKeyControll( &(F2151->CH[5]) );
/*TODO*///			CSMKeyControll( &(F2151->CH[6]) );
/*TODO*///			CSMKeyControll( &(F2151->CH[7]) );
/*TODO*///		}
/*TODO*///	}
/*TODO*///	return F2151->ST.irq;
/*TODO*///}
/*TODO*///
/*TODO*///#endif /* BUILD_YM2151 */
/*TODO*///   
}
