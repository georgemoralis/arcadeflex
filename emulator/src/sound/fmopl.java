package sound;

import static sound.fmoplH.*;
import static arcadeflex.ptrlib.*;
import sound.fm.FM_OPL;
import sound.fmoplH.*;

public class fmopl {
    /*TODO*///#ifndef PI
   /*TODO*///#define PI 3.14159265358979323846
   /*TODO*///#endif
   /*TODO*///
   /* -------------------- preliminary define section --------------------- */
   /* attack/decay rate time rate */
   public static final int OPL_ARRATE     =141280;  /* RATE 4 =  2826.24ms @ 3.6MHz */
   public static final int OPL_DRRATE    =1956000;  /* RATE 4 = 39280.64ms @ 3.6MHz */
   
   public static final int DELTAT_MIXING_LEVEL =(1); /* DELTA-T ADPCM MIXING LEVEL */

   public static final int FREQ_BITS =24;			/* frequency turn          */

   /* counter bits = 20 , octerve 7 */
   public static final int FREQ_RATE   =(1<<(FREQ_BITS-20));
   public static final int FREQ_RATETL_BITS=    (FREQ_BITS+2);
   /*TODO*///
   /*TODO*////* final output shift , limit minimum and maximum */
   /*TODO*///#define OPL_OUTSB   (TL_BITS+3-16)		/* OPL output final shift 16bit */
   /*TODO*///#define OPL_MAXOUT (0x7fff<<OPL_OUTSB)
   /*TODO*///#define OPL_MINOUT (-0x8000<<OPL_OUTSB)
   /*TODO*///
   /*TODO*////* -------------------- quality selection --------------------- */
   
   /* sinwave entries */
   /* used static memory = SIN_ENT * 4 (byte) */
   public static final int SIN_ENT =2048;
   
   /* output level entries (envelope,sinwave) */
   /* envelope counter lower bits */
   public static final int ENV_BITS =16;
   /* envelope output entries */
   public static final int EG_ENT   =4096;
   /* used dynamic memory = EG_ENT*4*4(byte)or EG_ENT*6*4(byte) */
   /* used static  memory = EG_ENT*4 (byte)                     */
   
   public static final int EG_OFF   =((2*EG_ENT)<<ENV_BITS);  /* OFF          */
   public static final int EG_DED   =EG_OFF;
   public static final int EG_DST   =(EG_ENT<<ENV_BITS);      /* DECAY  START */
   public static final int EG_AED   =EG_DST;
   public static final int EG_AST   =0;                       /* ATTACK START */
   
   /*TODO*///#define EG_STEP (96.0/EG_ENT) /* OPL is 0.1875 dB step  */

   /* LFO table entries */
   public static final int VIB_ENT= 512;
   public static final int VIB_SHIFT= (32-9);
   public static final int AMS_ENT= 512;
   public static final int AMS_SHIFT= (32-9);

   public static final int VIB_RATE =256;
   /*TODO*///
   /*TODO*////* -------------------- local defines , macros --------------------- */
   /*TODO*///
   /*TODO*////* register number to channel number , slot offset */
   /*TODO*///#define SLOT1 0
   /*TODO*///#define SLOT2 1
   /*TODO*///
   /*TODO*////* envelope phase */
   /*TODO*///#define ENV_MOD_RR  0x00
   /*TODO*///#define ENV_MOD_DR  0x01
   /*TODO*///#define ENV_MOD_AR  0x02
   /*TODO*///
   /*TODO*////* -------------------- tables --------------------- */
   /*TODO*///static const int slot_array[32]=
   /*TODO*///{
   /*TODO*///	 0, 2, 4, 1, 3, 5,-1,-1,
   /*TODO*///	 6, 8,10, 7, 9,11,-1,-1,
   /*TODO*///	12,14,16,13,15,17,-1,-1,
   /*TODO*///	-1,-1,-1,-1,-1,-1,-1,-1
   /*TODO*///};
   /*TODO*///
   /*TODO*////* key scale level */
   /*TODO*///#define ML (0.1875*2/EG_STEP)
   /*TODO*///static const UINT32 KSL_TABLE[8*16]=
   /*TODO*///{
   /*TODO*///	/* OCT 0 */
   /*TODO*///	 0.000*ML, 0.000*ML, 0.000*ML, 0.000*ML,
   /*TODO*///	 0.000*ML, 0.000*ML, 0.000*ML, 0.000*ML,
   /*TODO*///	 0.000*ML, 0.000*ML, 0.000*ML, 0.000*ML,
   /*TODO*///	 0.000*ML, 0.000*ML, 0.000*ML, 0.000*ML,
   /*TODO*///	/* OCT 1 */
   /*TODO*///	 0.000*ML, 0.000*ML, 0.000*ML, 0.000*ML,
   /*TODO*///	 0.000*ML, 0.000*ML, 0.000*ML, 0.000*ML,
   /*TODO*///	 0.000*ML, 0.750*ML, 1.125*ML, 1.500*ML,
   /*TODO*///	 1.875*ML, 2.250*ML, 2.625*ML, 3.000*ML,
   /*TODO*///	/* OCT 2 */
   /*TODO*///	 0.000*ML, 0.000*ML, 0.000*ML, 0.000*ML,
   /*TODO*///	 0.000*ML, 1.125*ML, 1.875*ML, 2.625*ML,
   /*TODO*///	 3.000*ML, 3.750*ML, 4.125*ML, 4.500*ML,
   /*TODO*///	 4.875*ML, 5.250*ML, 5.625*ML, 6.000*ML,
   /*TODO*///	/* OCT 3 */
   /*TODO*///	 0.000*ML, 0.000*ML, 0.000*ML, 1.875*ML,
   /*TODO*///	 3.000*ML, 4.125*ML, 4.875*ML, 5.625*ML,
   /*TODO*///	 6.000*ML, 6.750*ML, 7.125*ML, 7.500*ML,
   /*TODO*///	 7.875*ML, 8.250*ML, 8.625*ML, 9.000*ML,
   /*TODO*///	/* OCT 4 */
   /*TODO*///	 0.000*ML, 0.000*ML, 3.000*ML, 4.875*ML,
   /*TODO*///	 6.000*ML, 7.125*ML, 7.875*ML, 8.625*ML,
   /*TODO*///	 9.000*ML, 9.750*ML,10.125*ML,10.500*ML,
   /*TODO*///	10.875*ML,11.250*ML,11.625*ML,12.000*ML,
   /*TODO*///	/* OCT 5 */
   /*TODO*///	 0.000*ML, 3.000*ML, 6.000*ML, 7.875*ML,
   /*TODO*///	 9.000*ML,10.125*ML,10.875*ML,11.625*ML,
   /*TODO*///	12.000*ML,12.750*ML,13.125*ML,13.500*ML,
   /*TODO*///	13.875*ML,14.250*ML,14.625*ML,15.000*ML,
   /*TODO*///	/* OCT 6 */
   /*TODO*///	 0.000*ML, 6.000*ML, 9.000*ML,10.875*ML,
   /*TODO*///	12.000*ML,13.125*ML,13.875*ML,14.625*ML,
   /*TODO*///	15.000*ML,15.750*ML,16.125*ML,16.500*ML,
   /*TODO*///	16.875*ML,17.250*ML,17.625*ML,18.000*ML,
   /*TODO*///	/* OCT 7 */
   /*TODO*///	 0.000*ML, 9.000*ML,12.000*ML,13.875*ML,
   /*TODO*///	15.000*ML,16.125*ML,16.875*ML,17.625*ML,
   /*TODO*///	18.000*ML,18.750*ML,19.125*ML,19.500*ML,
   /*TODO*///	19.875*ML,20.250*ML,20.625*ML,21.000*ML
   /*TODO*///};
   /*TODO*///#undef ML
   /*TODO*///
   /*TODO*////* sustain lebel table (3db per step) */
   /*TODO*////* 0 - 15: 0, 3, 6, 9,12,15,18,21,24,27,30,33,36,39,42,93 (dB)*/
   /*TODO*///#define SC(db) (db*((3/EG_STEP)*(1<<ENV_BITS)))+EG_DST
   /*TODO*///static const INT32 SL_TABLE[16]={
   /*TODO*/// SC( 0),SC( 1),SC( 2),SC(3 ),SC(4 ),SC(5 ),SC(6 ),SC( 7),
   /*TODO*/// SC( 8),SC( 9),SC(10),SC(11),SC(12),SC(13),SC(14),SC(31)
   /*TODO*///};
   /*TODO*///#undef SC
   /*TODO*///
   /*TODO*///#define TL_MAX (EG_ENT*2) /* limit(tl + ksr + envelope) + sinwave */
   /*TODO*////* TotalLevel : 48 24 12  6  3 1.5 0.75 (dB) */
   /*TODO*////* TL_TABLE[ 0      to TL_MAX          ] : plus  section */
   /*TODO*////* TL_TABLE[ TL_MAX to TL_MAX+TL_MAX-1 ] : minus section */
   /*TODO*///static INT32 *TL_TABLE;
   /*TODO*///
   /*TODO*////* pointers to TL_TABLE with sinwave output offset */
   /*TODO*///static INT32 **SIN_TABLE;
   /*TODO*///
   /*TODO*////* LFO table */
   /*TODO*///static INT32 *AMS_TABLE;
   /*TODO*///static INT32 *VIB_TABLE;
   /*TODO*///
   /*TODO*////* envelope output curve table */
   /*TODO*////* attack + decay + OFF */
   /*TODO*///static INT32 ENV_CURVE[2*EG_ENT+1];
   /*TODO*///
   /*TODO*////* multiple table */
   /*TODO*///#define ML 2
   /*TODO*///static const UINT32 MUL_TABLE[16]= {
   /*TODO*////* 1/2, 1, 2, 3, 4, 5, 6, 7, 8, 9,10,11,12,13,14,15 */
   /*TODO*///   0.50*ML, 1.00*ML, 2.00*ML, 3.00*ML, 4.00*ML, 5.00*ML, 6.00*ML, 7.00*ML,
   /*TODO*///   8.00*ML, 9.00*ML,10.00*ML,10.00*ML,12.00*ML,12.00*ML,15.00*ML,15.00*ML
   /*TODO*///};
   /*TODO*///#undef ML
   /*TODO*///
   /*TODO*////* dummy attack / decay rate ( when rate == 0 ) */
   /*TODO*///static INT32 RATE_0[16]=
   /*TODO*///{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
   /*TODO*///
   /*TODO*////* -------------------- static state --------------------- */
   /*TODO*///
   /*TODO*////* lock level of common table */
   /*TODO*///static int num_lock = 0;
   /*TODO*///
   /*TODO*////* work table */
   /*TODO*///static void *cur_chip = NULL;	/* current chip point */
   /*TODO*////* currenct chip state */
   /*TODO*////* static FMSAMPLE  *bufL,*bufR; */
   /*TODO*///static OPL_CH *S_CH;
   /*TODO*///static OPL_CH *E_CH;
   /*TODO*///OPL_SLOT *SLOT7_1,*SLOT7_2,*SLOT8_1,*SLOT8_2;
   /*TODO*///
   /*TODO*///static INT32 outd[1];
   /*TODO*///static INT32 ams;
   /*TODO*///static INT32 vib;
   /*TODO*///INT32  *ams_table;
   /*TODO*///INT32  *vib_table;
   /*TODO*///static INT32 amsIncr;
   /*TODO*///static INT32 vibIncr;
   /*TODO*///static INT32 feedback2;		/* connect for SLOT 2 */
   /*TODO*///
   /*TODO*////* log output level */
   /*TODO*///#define LOG_ERR  3      /* ERROR       */
   /*TODO*///#define LOG_WAR  2      /* WARNING     */
   /*TODO*///#define LOG_INF  1      /* INFORMATION */
   /*TODO*///
   /*TODO*///#define LOG_LEVEL LOG_INF
   /*TODO*///
   /*TODO*///static void Log(int level,char *format,...)
   /*TODO*///{
   /*TODO*///	va_list argptr;
   /*TODO*///
   /*TODO*///	if( level < LOG_LEVEL ) return;
   /*TODO*///	va_start(argptr,format);
   /*TODO*///	/* */
   /*TODO*///	if (errorlog) vfprintf( errorlog, format , argptr);
   /*TODO*///}
   /*TODO*///
   /*TODO*////* --------------------- subroutines  --------------------- */
   /*TODO*///
   /*TODO*///INLINE int Limit( int val, int max, int min ) {
   /*TODO*///	if ( val > max )
   /*TODO*///		val = max;
   /*TODO*///	else if ( val < min )
   /*TODO*///		val = min;
   /*TODO*///
   /*TODO*///	return val;
   /*TODO*///}
   /*TODO*///
   /*TODO*////* status set and IRQ handling */
   /*TODO*///INLINE void OPL_STATUS_SET(FM_OPL *OPL,int flag)
   /*TODO*///{
   /*TODO*///	/* set status flag */
   /*TODO*///	OPL->status |= flag;
   /*TODO*///	if(!(OPL->status & 0x80))
   /*TODO*///	{
   /*TODO*///		if(OPL->status & OPL->statusmask)
   /*TODO*///		{	/* IRQ on */
   /*TODO*///			OPL->status |= 0x80;
   /*TODO*///			/* callback user interrupt handler (IRQ is OFF to ON) */
   /*TODO*///			if(OPL->IRQHandler) (OPL->IRQHandler)(OPL->IRQParam,1);
   /*TODO*///		}
   /*TODO*///	}
   /*TODO*///}
   /*TODO*///
   /*TODO*////* status reset and IRQ handling */
   /*TODO*///INLINE void OPL_STATUS_RESET(FM_OPL *OPL,int flag)
   /*TODO*///{
   /*TODO*///	/* reset status flag */
   /*TODO*///	OPL->status &=~flag;
   /*TODO*///	if((OPL->status & 0x80))
   /*TODO*///	{
   /*TODO*///		if (!(OPL->status & OPL->statusmask) )
   /*TODO*///		{
   /*TODO*///			OPL->status &= 0x7f;
   /*TODO*///			/* callback user interrupt handler (IRQ is ON to OFF) */
   /*TODO*///			if(OPL->IRQHandler) (OPL->IRQHandler)(OPL->IRQParam,0);
   /*TODO*///		}
   /*TODO*///	}
   /*TODO*///}
   /*TODO*///
   /*TODO*////* IRQ mask set */
   /*TODO*///INLINE void OPL_STATUSMASK_SET(FM_OPL *OPL,int flag)
   /*TODO*///{
   /*TODO*///	OPL->statusmask = flag;
   /*TODO*///	/* IRQ handling check */
   /*TODO*///	OPL_STATUS_SET(OPL,0);
   /*TODO*///	OPL_STATUS_RESET(OPL,0);
   /*TODO*///}
   /*TODO*///
   /*TODO*////* ----- key on  ----- */
   /*TODO*///INLINE void OPL_KEYON(OPL_SLOT *SLOT)
   /*TODO*///{
   /*TODO*///	/* sin wave restart */
   /*TODO*///	SLOT->Cnt = 0;
   /*TODO*///	/* set attack */
   /*TODO*///	SLOT->evm = ENV_MOD_AR;
   /*TODO*///	SLOT->evs = SLOT->evsa;
   /*TODO*///	SLOT->evc = EG_AST;
   /*TODO*///	SLOT->eve = EG_AED;
   /*TODO*///}
   /*TODO*////* ----- key off ----- */
   /*TODO*///INLINE void OPL_KEYOFF(OPL_SLOT *SLOT)
   /*TODO*///{
   /*TODO*///	if( SLOT->evm > ENV_MOD_RR)
   /*TODO*///	{
   /*TODO*///		/* set envelope counter from envleope output */
   /*TODO*///		SLOT->evm = ENV_MOD_RR;
   /*TODO*///		if( !(SLOT->evc&EG_DST) )
   /*TODO*///			//SLOT->evc = (ENV_CURVE[SLOT->evc>>ENV_BITS]<<ENV_BITS) + EG_DST;
   /*TODO*///			SLOT->evc = EG_DST;
   /*TODO*///		SLOT->eve = EG_DED;
   /*TODO*///		SLOT->evs = SLOT->evsr;
   /*TODO*///	}
   /*TODO*///}
   /*TODO*///
   /*TODO*////* ---------- calcrate Envelope Generator & Phase Generator ---------- */
   /*TODO*////* return : envelope output */
   /*TODO*///INLINE UINT32 OPL_CALC_SLOT( OPL_SLOT *SLOT )
   /*TODO*///{
   /*TODO*///	/* calcrate envelope generator */
   /*TODO*///	if( (SLOT->evc+=SLOT->evs) >= SLOT->eve )
   /*TODO*///	{
   /*TODO*///		switch( SLOT->evm ){
   /*TODO*///		case ENV_MOD_AR: /* ATTACK -> DECAY1 */
   /*TODO*///			/* next DR */
   /*TODO*///			SLOT->evm = ENV_MOD_DR;
   /*TODO*///			SLOT->evc = EG_DST;
   /*TODO*///			SLOT->eve = SLOT->SL;
   /*TODO*///			SLOT->evs = SLOT->evsd;
   /*TODO*///			break;
   /*TODO*///		case ENV_MOD_DR: /* DECAY -> SL or RR */
   /*TODO*///			SLOT->evc = SLOT->SL;
   /*TODO*///			SLOT->eve = EG_DED;
   /*TODO*///			if(SLOT->eg_typ)
   /*TODO*///			{
   /*TODO*///				SLOT->evs = 0;
   /*TODO*///			}
   /*TODO*///			else
   /*TODO*///			{
   /*TODO*///				SLOT->evm = ENV_MOD_RR;
   /*TODO*///				SLOT->evs = SLOT->evsr;
   /*TODO*///			}
   /*TODO*///			break;
   /*TODO*///		case ENV_MOD_RR: /* RR -> OFF */
   /*TODO*///			SLOT->evc = EG_OFF;
   /*TODO*///			SLOT->eve = EG_OFF+1;
   /*TODO*///			SLOT->evs = 0;
   /*TODO*///			break;
   /*TODO*///		}
   /*TODO*///	}
   /*TODO*///	/* calcrate envelope */
   /*TODO*///	return SLOT->TLL+ENV_CURVE[SLOT->evc>>ENV_BITS]+(SLOT->ams ? ams : 0);
   /*TODO*///}
   /*TODO*///
   /*TODO*////* set algorythm connection */
   /*TODO*///static void set_algorythm( OPL_CH *CH)
   /*TODO*///{
   /*TODO*///	INT32 *carrier = &outd[0];
   /*TODO*///	CH->connect1 = CH->CON ? carrier : &feedback2;
   /*TODO*///	CH->connect2 = carrier;
   /*TODO*///}
   /*TODO*///
   /*TODO*////* ---------- frequency counter for operater update ---------- */
   /*TODO*///INLINE void CALC_FCSLOT(OPL_CH *CH,OPL_SLOT *SLOT)
   /*TODO*///{
   /*TODO*///	int ksr;
   /*TODO*///
   /*TODO*///	/* frequency step counter */
   /*TODO*///	SLOT->Incr = CH->fc * SLOT->mul;
   /*TODO*///	ksr = CH->kcode >> SLOT->KSR;
   /*TODO*///
   /*TODO*///	if( SLOT->ksr != ksr )
   /*TODO*///	{
   /*TODO*///		SLOT->ksr = ksr;
   /*TODO*///		/* attack , decay rate recalcration */
   /*TODO*///		SLOT->evsa = SLOT->AR[ksr];
   /*TODO*///		SLOT->evsd = SLOT->DR[ksr];
   /*TODO*///		SLOT->evsr = SLOT->RR[ksr];
   /*TODO*///	}
   /*TODO*///	SLOT->TLL = SLOT->TL + (CH->ksl_base>>SLOT->ksl);
   /*TODO*///}
   /*TODO*///
   /*TODO*////* set multi,am,vib,EG-TYP,KSR,mul */
   /*TODO*///INLINE void set_mul(FM_OPL *OPL,int slot,int v)
   /*TODO*///{
   /*TODO*///	OPL_CH   *CH   = &OPL->P_CH[slot/2];
   /*TODO*///	OPL_SLOT *SLOT = &CH->SLOT[slot&1];
   /*TODO*///
   /*TODO*///	SLOT->mul    = MUL_TABLE[v&0x0f];
   /*TODO*///	SLOT->KSR    = (v&0x10) ? 0 : 2;
   /*TODO*///	SLOT->eg_typ = (v&0x20)>>5;
   /*TODO*///	SLOT->vib    = (v&0x40);
   /*TODO*///	SLOT->ams    = (v&0x80);
   /*TODO*///	CALC_FCSLOT(CH,SLOT);
   /*TODO*///}
   /*TODO*///
   /*TODO*////* set ksl & tl */
   /*TODO*///INLINE void set_ksl_tl(FM_OPL *OPL,int slot,int v)
   /*TODO*///{
   /*TODO*///	OPL_CH   *CH   = &OPL->P_CH[slot/2];
   /*TODO*///	OPL_SLOT *SLOT = &CH->SLOT[slot&1];
   /*TODO*///	int ksl = v>>6; /* 0 / 1.5 / 3 / 6 db/OCT */
   /*TODO*///
   /*TODO*///	SLOT->ksl = ksl ? 3-ksl : 31;
   /*TODO*///	SLOT->TL  = (v&0x3f)*(0.75/EG_STEP); /* 0.75db step */
   /*TODO*///
   /*TODO*///	if( !(OPL->mode&0x80) )
   /*TODO*///	{	/* not CSM latch total level */
   /*TODO*///		SLOT->TLL = SLOT->TL + (CH->ksl_base>>SLOT->ksl);
   /*TODO*///	}
   /*TODO*///}
   /*TODO*///
   /*TODO*////* set attack rate & decay rate  */
   /*TODO*///INLINE void set_ar_dr(FM_OPL *OPL,int slot,int v)
   /*TODO*///{
   /*TODO*///	OPL_CH   *CH   = &OPL->P_CH[slot/2];
   /*TODO*///	OPL_SLOT *SLOT = &CH->SLOT[slot&1];
   /*TODO*///	int ar = v>>4;
   /*TODO*///	int dr = v&0x0f;
   /*TODO*///
   /*TODO*///	SLOT->AR = ar ? &OPL->AR_TABLE[ar<<2] : RATE_0;
   /*TODO*///	SLOT->evsa = SLOT->AR[SLOT->ksr];
   /*TODO*///	if( SLOT->evm == ENV_MOD_AR ) SLOT->evs = SLOT->evsa;
   /*TODO*///
   /*TODO*///	SLOT->DR = dr ? &OPL->DR_TABLE[dr<<2] : RATE_0;
   /*TODO*///	SLOT->evsd = SLOT->DR[SLOT->ksr];
   /*TODO*///	if( SLOT->evm == ENV_MOD_DR ) SLOT->evs = SLOT->evsd;
   /*TODO*///}
   /*TODO*///
   /*TODO*////* set sustain level & release rate */
   /*TODO*///INLINE void set_sl_rr(FM_OPL *OPL,int slot,int v)
   /*TODO*///{
   /*TODO*///	OPL_CH   *CH   = &OPL->P_CH[slot/2];
   /*TODO*///	OPL_SLOT *SLOT = &CH->SLOT[slot&1];
   /*TODO*///	int sl = v>>4;
   /*TODO*///	int rr = v & 0x0f;
   /*TODO*///
   /*TODO*///	SLOT->SL = SL_TABLE[sl];
   /*TODO*///	if( SLOT->evm == ENV_MOD_DR ) SLOT->eve = SLOT->SL;
   /*TODO*///	SLOT->RR = &OPL->DR_TABLE[rr<<2];
   /*TODO*///	SLOT->evsr = SLOT->RR[SLOT->ksr];
   /*TODO*///	if( SLOT->evm == ENV_MOD_RR ) SLOT->evs = SLOT->evsr;
   /*TODO*///}
   /*TODO*///
   /*TODO*////* operator output calcrator */
   /*TODO*///#define OP_OUT(slot,env,con)   slot->wavetable[((slot->Cnt+con)/(0x1000000/SIN_ENT))&(SIN_ENT-1)][env]
   /*TODO*////* ---------- calcrate one of channel ---------- */
   /*TODO*///INLINE void OPL_CALC_CH( OPL_CH *CH )
   /*TODO*///{
   /*TODO*///	UINT32 env_out;
   /*TODO*///	OPL_SLOT *SLOT;
   /*TODO*///
   /*TODO*///	feedback2 = 0;
   /*TODO*///	/* SLOT 1 */
   /*TODO*///	SLOT = &CH->SLOT[SLOT1];
   /*TODO*///	env_out=OPL_CALC_SLOT(SLOT);
   /*TODO*///	if( env_out < EG_ENT-1 )
   /*TODO*///	{
   /*TODO*///		/* PG */
   /*TODO*///		if(SLOT->vib) SLOT->Cnt += (SLOT->Incr*vib/VIB_RATE);
   /*TODO*///		else          SLOT->Cnt += SLOT->Incr;
   /*TODO*///		/* connectoion */
   /*TODO*///		if(CH->FB)
   /*TODO*///		{
   /*TODO*///			int feedback1 = (CH->op1_out[0]+CH->op1_out[1])>>CH->FB;
   /*TODO*///			CH->op1_out[1] = CH->op1_out[0];
   /*TODO*///			*CH->connect1 += CH->op1_out[0] = OP_OUT(SLOT,env_out,feedback1);
   /*TODO*///		}
   /*TODO*///		else
   /*TODO*///		{
   /*TODO*///			*CH->connect1 += OP_OUT(SLOT,env_out,0);
   /*TODO*///		}
   /*TODO*///	}else
   /*TODO*///	{
   /*TODO*///		CH->op1_out[1] = CH->op1_out[0];
   /*TODO*///		CH->op1_out[0] = 0;
   /*TODO*///	}
   /*TODO*///	/* SLOT 2 */
   /*TODO*///	SLOT = &CH->SLOT[SLOT2];
   /*TODO*///	env_out=OPL_CALC_SLOT(SLOT);
   /*TODO*///	if( env_out < EG_ENT-1 )
   /*TODO*///	{
   /*TODO*///		/* PG */
   /*TODO*///		if(SLOT->vib) SLOT->Cnt += (SLOT->Incr*vib/VIB_RATE);
   /*TODO*///		else          SLOT->Cnt += SLOT->Incr;
   /*TODO*///		/* connectoion */
   /*TODO*///		outd[0] += OP_OUT(SLOT,env_out, feedback2);
   /*TODO*///	}
   /*TODO*///}
   /*TODO*///
   /*TODO*////* ---------- calcrate rythm block ---------- */
   /*TODO*///#define WHITE_NOISE_db 6.0
   /*TODO*///INLINE void OPL_CALC_RH( OPL_CH *CH )
   /*TODO*///{
   /*TODO*///	UINT32 env_tam,env_sd,env_top,env_hh;
   /*TODO*///	int whitenoise = (rand()&1)*(WHITE_NOISE_db/EG_STEP);
   /*TODO*///	INT32 tone8;
   /*TODO*///
   /*TODO*///	OPL_SLOT *SLOT;
   /*TODO*///	int env_out;
   /*TODO*///
   /*TODO*///	/* BD : same as FM serial mode and output level is large */
   /*TODO*///	feedback2 = 0;
   /*TODO*///	/* SLOT 1 */
   /*TODO*///	SLOT = &CH[6].SLOT[SLOT1];
   /*TODO*///	env_out=OPL_CALC_SLOT(SLOT);
   /*TODO*///	if( env_out < EG_ENT-1 )
   /*TODO*///	{
   /*TODO*///		/* PG */
   /*TODO*///		if(SLOT->vib) SLOT->Cnt += (SLOT->Incr*vib/VIB_RATE);
   /*TODO*///		else          SLOT->Cnt += SLOT->Incr;
   /*TODO*///		/* connectoion */
   /*TODO*///		if(CH[6].FB)
   /*TODO*///		{
   /*TODO*///			int feedback1 = (CH[6].op1_out[0]+CH[6].op1_out[1])>>CH[6].FB;
   /*TODO*///			CH[6].op1_out[1] = CH[6].op1_out[0];
   /*TODO*///			feedback2 = CH[6].op1_out[0] = OP_OUT(SLOT,env_out,feedback1);
   /*TODO*///		}
   /*TODO*///		else
   /*TODO*///		{
   /*TODO*///			feedback2 = OP_OUT(SLOT,env_out,0);
   /*TODO*///		}
   /*TODO*///	}else
   /*TODO*///	{
   /*TODO*///		feedback2 = 0;
   /*TODO*///		CH[6].op1_out[1] = CH[6].op1_out[0];
   /*TODO*///		CH[6].op1_out[0] = 0;
   /*TODO*///	}
   /*TODO*///	/* SLOT 2 */
   /*TODO*///	SLOT = &CH[6].SLOT[SLOT2];
   /*TODO*///	env_out=OPL_CALC_SLOT(SLOT);
   /*TODO*///	if( env_out < EG_ENT-1 )
   /*TODO*///	{
   /*TODO*///		/* PG */
   /*TODO*///		if(SLOT->vib) SLOT->Cnt += (SLOT->Incr*vib/VIB_RATE);
   /*TODO*///		else          SLOT->Cnt += SLOT->Incr;
   /*TODO*///		/* connectoion */
   /*TODO*///		outd[0] += OP_OUT(SLOT,env_out, feedback2)*2;
   /*TODO*///	}
   /*TODO*///
   /*TODO*///	// SD  (17) = mul14[fnum7] + white noise
   /*TODO*///	// TAM (15) = mul15[fnum8]
   /*TODO*///	// TOP (18) = fnum6(mul18[fnum8]+whitenoise)
   /*TODO*///	// HH  (14) = fnum7(mul18[fnum8]+whitenoise) + white noise
   /*TODO*///	env_sd =OPL_CALC_SLOT(SLOT7_2) + whitenoise;
   /*TODO*///	env_tam=OPL_CALC_SLOT(SLOT8_1);
   /*TODO*///	env_top=OPL_CALC_SLOT(SLOT8_2);
   /*TODO*///	env_hh =OPL_CALC_SLOT(SLOT7_1) + whitenoise;
   /*TODO*///
   /*TODO*///	/* PG */
   /*TODO*///	if(SLOT7_1->vib) SLOT7_1->Cnt += (2*SLOT7_1->Incr*vib/VIB_RATE);
   /*TODO*///	else             SLOT7_1->Cnt += 2*SLOT7_1->Incr;
   /*TODO*///	if(SLOT7_2->vib) SLOT7_2->Cnt += ((CH[7].fc*8)*vib/VIB_RATE);
   /*TODO*///	else             SLOT7_2->Cnt += (CH[7].fc*8);
   /*TODO*///	if(SLOT8_1->vib) SLOT8_1->Cnt += (SLOT8_1->Incr*vib/VIB_RATE);
   /*TODO*///	else             SLOT8_1->Cnt += SLOT8_1->Incr;
   /*TODO*///	if(SLOT8_2->vib) SLOT8_2->Cnt += ((CH[8].fc*48)*vib/VIB_RATE);
   /*TODO*///	else             SLOT8_2->Cnt += (CH[8].fc*48);
   /*TODO*///
   /*TODO*///	tone8 = OP_OUT(SLOT8_2,whitenoise,0 );
   /*TODO*///
   /*TODO*///	/* SD */
   /*TODO*///	if( env_sd < EG_ENT-1 )
   /*TODO*///		outd[0] += OP_OUT(SLOT7_1,env_sd, 0)*8;
   /*TODO*///	/* TAM */
   /*TODO*///	if( env_tam < EG_ENT-1 )
   /*TODO*///		outd[0] += OP_OUT(SLOT8_1,env_tam, 0)*2;
   /*TODO*///	/* TOP-CY */
   /*TODO*///	if( env_top < EG_ENT-1 )
   /*TODO*///		outd[0] += OP_OUT(SLOT7_2,env_top,tone8)*2;
   /*TODO*///	/* HH */
   /*TODO*///	if( env_hh  < EG_ENT-1 )
   /*TODO*///		outd[0] += OP_OUT(SLOT7_2,env_hh,tone8)*2;
   /*TODO*///}
   
   /* ----------- initialize time tabls ----------- */
   static void init_timetables(FM_OPL OPL , int ARRATE , int DRRATE )
   {
   	int i;
   	double rate;
   
   	/* make attack rate & decay rate tables */
   	for (i = 0;i < 4;i++) OPL.AR_TABLE[i] = OPL.DR_TABLE[i] = 0;
   	for (i = 4;i <= 60;i++){
   		rate  = OPL.freqbase;						/* frequency rate */
   		if( i < 60 ) rate *= 1.0+(i&3)*0.25;		/* b0-1 : x1 , x1.25 , x1.5 , x1.75 */
   		rate *= 1<<((i>>2)-1);						/* b2-5 : shift bit */
   		rate *= (double)(EG_ENT<<ENV_BITS);
   		OPL.AR_TABLE[i] = (int)(rate / ARRATE);
   		OPL.DR_TABLE[i] = (int)(rate / DRRATE);
   	}
   	for (i = 60;i < 76;i++)
   	{
   		OPL.AR_TABLE[i] = EG_AED-1;
   		OPL.DR_TABLE[i] = OPL.DR_TABLE[60];
   	}
   }
   
   /*TODO*////* ---------- generic table initialize ---------- */
   /*TODO*///static int OPLOpenTable( void )
   /*TODO*///{
   /*TODO*///	int s,t;
   /*TODO*///	double rate;
   /*TODO*///	int i,j;
   /*TODO*///	double pom;
   /*TODO*///
   /*TODO*///	/* allocate dynamic tables */
   /*TODO*///	if( (TL_TABLE = malloc(TL_MAX*2*sizeof(INT32))) == NULL)
   /*TODO*///		return 0;
   /*TODO*///	if( (SIN_TABLE = malloc(SIN_ENT*4 *sizeof(INT32 *))) == NULL)
   /*TODO*///	{
   /*TODO*///		free(TL_TABLE);
   /*TODO*///		return 0;
   /*TODO*///	}
   /*TODO*///	if( (AMS_TABLE = malloc(AMS_ENT*2 *sizeof(INT32))) == NULL)
   /*TODO*///	{
   /*TODO*///		free(TL_TABLE);
   /*TODO*///		free(SIN_TABLE);
   /*TODO*///		return 0;
   /*TODO*///	}
   /*TODO*///	if( (VIB_TABLE = malloc(VIB_ENT*2 *sizeof(INT32))) == NULL)
   /*TODO*///	{
   /*TODO*///		free(TL_TABLE);
   /*TODO*///		free(SIN_TABLE);
   /*TODO*///		free(AMS_TABLE);
   /*TODO*///		return 0;
   /*TODO*///	}
   /*TODO*///	/* make total level table */
   /*TODO*///	for (t = 0;t < EG_ENT-1 ;t++){
   /*TODO*///		rate = ((1<<TL_BITS)-1)/pow(10,EG_STEP*t/20);	/* dB -> voltage */
   /*TODO*///		TL_TABLE[       t] =  (int)rate;
   /*TODO*///		TL_TABLE[TL_MAX+t] = -TL_TABLE[t];
   /*TODO*////*		Log(LOG_INF,"TotalLevel(%3d) = %x\n",t,TL_TABLE[t]);*/
   /*TODO*///	}
   /*TODO*///	/* fill volume off area */
   /*TODO*///	for ( t = EG_ENT-1; t < TL_MAX ;t++){
   /*TODO*///		TL_TABLE[t] = TL_TABLE[TL_MAX+t] = 0;
   /*TODO*///	}
   /*TODO*///
   /*TODO*///	/* make sinwave table (total level offet) */
   /*TODO*///	/* degree 0 = degree 180                   = off */
   /*TODO*///	SIN_TABLE[0] = SIN_TABLE[SIN_ENT/2]         = &TL_TABLE[EG_ENT-1];
   /*TODO*///	for (s = 1;s <= SIN_ENT/4;s++){
   /*TODO*///		pom = sin(2*PI*s/SIN_ENT); /* sin     */
   /*TODO*///		pom = 20*log10(1/pom);	   /* decibel */
   /*TODO*///		j = pom / EG_STEP;         /* TL_TABLE steps */
   /*TODO*///
   /*TODO*///        /* degree 0   -  90    , degree 180 -  90 : plus section */
   /*TODO*///		SIN_TABLE[          s] = SIN_TABLE[SIN_ENT/2-s] = &TL_TABLE[j];
   /*TODO*///        /* degree 180 - 270    , degree 360 - 270 : minus section */
   /*TODO*///		SIN_TABLE[SIN_ENT/2+s] = SIN_TABLE[SIN_ENT  -s] = &TL_TABLE[TL_MAX+j];
   /*TODO*////*		Log(LOG_INF,"sin(%3d) = %f:%f db\n",s,pom,(double)j * EG_STEP);*/
   /*TODO*///	}
   /*TODO*///	for (s = 0;s < SIN_ENT;s++)
   /*TODO*///	{
   /*TODO*///		SIN_TABLE[SIN_ENT*1+s] = s<(SIN_ENT/2) ? SIN_TABLE[s] : &TL_TABLE[EG_ENT];
   /*TODO*///		SIN_TABLE[SIN_ENT*2+s] = SIN_TABLE[s % (SIN_ENT/2)];
   /*TODO*///		SIN_TABLE[SIN_ENT*3+s] = (s/(SIN_ENT/4))&1 ? &TL_TABLE[EG_ENT] : SIN_TABLE[SIN_ENT*2+s];
   /*TODO*///	}
   /*TODO*///
   /*TODO*///	/* envelope counter -> envelope output table */
   /*TODO*///	for (i=0; i<EG_ENT; i++)
   /*TODO*///	{
   /*TODO*///		/* ATTACK curve */
   /*TODO*///		pom = pow( ((double)(EG_ENT-1-i)/EG_ENT) , 8 ) * EG_ENT;
   /*TODO*///		/* if( pom >= EG_ENT ) pom = EG_ENT-1; */
   /*TODO*///		ENV_CURVE[i] = (int)pom;
   /*TODO*///		/* DECAY ,RELEASE curve */
   /*TODO*///		ENV_CURVE[(EG_DST>>ENV_BITS)+i]= i;
   /*TODO*///	}
   /*TODO*///	/* off */
   /*TODO*///	ENV_CURVE[EG_OFF>>ENV_BITS]= EG_ENT-1;
   /*TODO*///	/* make LFO ams table */
   /*TODO*///	for (i=0; i<AMS_ENT; i++)
   /*TODO*///	{
   /*TODO*///		pom = (1.0+sin(2*PI*i/AMS_ENT))/2; /* sin */
   /*TODO*///		AMS_TABLE[i]         = (1.0/EG_STEP)*pom; /* 1dB   */
   /*TODO*///		AMS_TABLE[AMS_ENT+i] = (4.8/EG_STEP)*pom; /* 4.8dB */
   /*TODO*///	}
   /*TODO*///	/* make LFO vibrate table */
   /*TODO*///	for (i=0; i<VIB_ENT; i++)
   /*TODO*///	{
   /*TODO*///		/* 100cent = 1seminote = 6% ?? */
   /*TODO*///		pom = (double)VIB_RATE*0.06*sin(2*PI*i/VIB_ENT); /* +-100sect step */
   /*TODO*///		VIB_TABLE[i]         = VIB_RATE + (pom*0.07); /* +- 7cent */
   /*TODO*///		VIB_TABLE[VIB_ENT+i] = VIB_RATE + (pom*0.14); /* +-14cent */
   /*TODO*///		/* Log(LOG_INF,"vib %d=%d\n",i,VIB_TABLE[VIB_ENT+i]); */
   /*TODO*///	}
   /*TODO*///	return 1;
   /*TODO*///}
   /*TODO*///
   /*TODO*///
   /*TODO*///static void OPLCloseTable( void )
   /*TODO*///{
   /*TODO*///	free(TL_TABLE);
   /*TODO*///	free(SIN_TABLE);
   /*TODO*///	free(AMS_TABLE);
   /*TODO*///	free(VIB_TABLE);
   /*TODO*///}
   /*TODO*///
   /*TODO*////* CSM Key Controll */
   /*TODO*///INLINE void CSMKeyControll(OPL_CH *CH)
   /*TODO*///{
   /*TODO*///	OPL_SLOT *slot1 = &CH->SLOT[SLOT1];
   /*TODO*///	OPL_SLOT *slot2 = &CH->SLOT[SLOT2];
   /*TODO*///	/* all key off */
   /*TODO*///	OPL_KEYOFF(slot1);
   /*TODO*///	OPL_KEYOFF(slot2);
   /*TODO*///	/* total level latch */
   /*TODO*///	slot1->TLL = slot1->TL + (CH->ksl_base>>slot1->ksl);
   /*TODO*///	slot1->TLL = slot1->TL + (CH->ksl_base>>slot1->ksl);
   /*TODO*///	/* key on */
   /*TODO*///	CH->op1_out[0] = CH->op1_out[1] = 0;
   /*TODO*///	OPL_KEYON(slot1);
   /*TODO*///	OPL_KEYON(slot2);
   /*TODO*///}
   
   /* ---------- opl initialize ---------- */
   public static void OPL_initalize(FM_OPL OPL)
   {
   	int fn;
   
   	/* frequency base */
        OPL.freqbase = (OPL.rate) != 0 ? ((double)OPL.clock / OPL.rate) / 72 : 0;

   	/* Timer base time */
   	OPL.TimerBase = 1.0/((double)OPL.clock / 72.0 );
   	/* make time tables */
   	init_timetables( OPL , OPL_ARRATE , OPL_DRRATE );
   	/* make fnumber -> increment counter table */
   	for( fn=0 ; fn < 1024 ; fn++ )
   	{
/*RECHECK*/   		OPL.FN_TABLE[fn] = (long)(OPL.freqbase * fn * FREQ_RATE * (1<<7) / 2)& 0xffffffffL;//converting to unsigned
   	}
   	/* LFO freq.table */
        OPL.amsIncr = (int)(OPL.rate != 0 ? (double)AMS_ENT * (1 << AMS_SHIFT) / OPL.rate * 3.7 * ((double)OPL.clock / 3600000) : 0);
        OPL.vibIncr = (int)(OPL.rate != 0 ? (double)VIB_ENT * (1 << VIB_SHIFT) / OPL.rate * 6.4 * ((double)OPL.clock / 3600000) : 0);
        

   }
   
   /* ---------- write a OPL registers ---------- */
   public static void OPLWriteReg(FM_OPL OPL, int r, int v)
   {
   /*TODO*///	OPL_CH *CH;
   /*TODO*///	int slot;
   /*TODO*///	int block_fnum;
   /*TODO*///
   /*TODO*///	switch(r&0xe0)
   /*TODO*///	{
   /*TODO*///	case 0x00: /* 00-1f:controll */
   /*TODO*///		switch(r&0x1f)
   /*TODO*///		{
   /*TODO*///		case 0x01:
   /*TODO*///			/* wave selector enable */
   /*TODO*///			if(OPL->type&OPL_TYPE_WAVESEL)
   /*TODO*///			{
   /*TODO*///				OPL->wavesel = v&0x20;
   /*TODO*///				if(!OPL->wavesel)
   /*TODO*///				{
   /*TODO*///					/* preset compatible mode */
   /*TODO*///					int c;
   /*TODO*///					for(c=0;c<OPL->max_ch;c++)
   /*TODO*///					{
   /*TODO*///						OPL->P_CH[c].SLOT[SLOT1].wavetable = &SIN_TABLE[0];
   /*TODO*///						OPL->P_CH[c].SLOT[SLOT2].wavetable = &SIN_TABLE[0];
   /*TODO*///					}
   /*TODO*///				}
   /*TODO*///			}
   /*TODO*///			return;
   /*TODO*///		case 0x02:	/* Timer 1 */
   /*TODO*///			OPL->T[0] = (256-v)*4;
   /*TODO*///			break;
   /*TODO*///		case 0x03:	/* Timer 2 */
   /*TODO*///			OPL->T[1] = (256-v)*16;
   /*TODO*///			return;
   /*TODO*///		case 0x04:	/* IRQ clear / mask and Timer enable */
   /*TODO*///			if(v&0x80)
   /*TODO*///			{	/* IRQ flag clear */
   /*TODO*///				OPL_STATUS_RESET(OPL,0x7f);
   /*TODO*///			}
   /*TODO*///			else
   /*TODO*///			{	/* set IRQ mask ,timer enable*/
   /*TODO*///				UINT8 st1 = v&1;
   /*TODO*///				UINT8 st2 = (v>>1)&1;
   /*TODO*///				/* IRQRST,T1MSK,t2MSK,EOSMSK,BRMSK,x,ST2,ST1 */
   /*TODO*///				OPL_STATUS_RESET(OPL,v&0x78);
   /*TODO*///				OPL_STATUSMASK_SET(OPL,((~v)&0x78)|0x01);
   /*TODO*///				/* timer 2 */
   /*TODO*///				if(OPL->st[1] != st2)
   /*TODO*///				{
   /*TODO*///					double interval = st2 ? (double)OPL->T[1]*OPL->TimerBase : 0.0;
   /*TODO*///					OPL->st[1] = st2;
   /*TODO*///					if (OPL->TimerHandler) (OPL->TimerHandler)(OPL->TimerParam+1,interval);
   /*TODO*///				}
   /*TODO*///				/* timer 1 */
   /*TODO*///				if(OPL->st[0] != st1)
   /*TODO*///				{
   /*TODO*///					double interval = st1 ? (double)OPL->T[0]*OPL->TimerBase : 0.0;
   /*TODO*///					OPL->st[0] = st1;
   /*TODO*///					if (OPL->TimerHandler) (OPL->TimerHandler)(OPL->TimerParam+0,interval);
   /*TODO*///				}
   /*TODO*///			}
   /*TODO*///			return;
   /*TODO*///#if BUILD_Y8950
   /*TODO*///		case 0x06:		/* Key Board OUT */
   /*TODO*///			if(OPL->type&OPL_TYPE_KEYBOARD)
   /*TODO*///			{
   /*TODO*///				if(OPL->keyboardhandler_w)
   /*TODO*///					OPL->keyboardhandler_w(OPL->keyboard_param,v);
   /*TODO*///				else
   /*TODO*///					Log(LOG_WAR,"OPL:write unmapped KEYBOARD port\n");
   /*TODO*///			}
   /*TODO*///			return;
   /*TODO*///		case 0x07:	/* DELTA-T controll : START,REC,MEMDATA,REPT,SPOFF,x,x,RST */
   /*TODO*///			if(OPL->type&OPL_TYPE_ADPCM)
   /*TODO*///				YM_DELTAT_ADPCM_Write(OPL->deltat,r-0x07,v);
   /*TODO*///			return;
   /*TODO*///		case 0x08:	/* MODE,DELTA-T : CSM,NOTESEL,x,x,smpl,da/ad,64k,rom */
   /*TODO*///			OPL->mode = v;
   /*TODO*///			v&=0x1f;	/* for DELTA-T unit */
   /*TODO*///		case 0x09:		/* START ADD */
   /*TODO*///		case 0x0a:
   /*TODO*///		case 0x0b:		/* STOP ADD  */
   /*TODO*///		case 0x0c:
   /*TODO*///		case 0x0d:		/* PRESCALE   */
   /*TODO*///		case 0x0e:
   /*TODO*///		case 0x0f:		/* ADPCM data */
   /*TODO*///		case 0x10: 		/* DELTA-N    */
   /*TODO*///		case 0x11: 		/* DELTA-N    */
   /*TODO*///		case 0x12: 		/* EG-CTRL    */
   /*TODO*///			if(OPL->type&OPL_TYPE_ADPCM)
   /*TODO*///				YM_DELTAT_ADPCM_Write(OPL->deltat,r-0x07,v);
   /*TODO*///			return;
   /*TODO*///#if 0
   /*TODO*///		case 0x15:		/* DAC data    */
   /*TODO*///		case 0x16:
   /*TODO*///		case 0x17:		/* SHIFT    */
   /*TODO*///			return;
   /*TODO*///		case 0x18:		/* I/O CTRL (Direction) */
   /*TODO*///			if(OPL->type&OPL_TYPE_IO)
   /*TODO*///				OPL->portDirection = v&0x0f;
   /*TODO*///			return;
   /*TODO*///		case 0x19:		/* I/O DATA */
   /*TODO*///			if(OPL->type&OPL_TYPE_IO)
   /*TODO*///			{
   /*TODO*///				OPL->portLatch = v;
   /*TODO*///				if(OPL->porthandler_w)
   /*TODO*///					OPL->porthandler_w(OPL->port_param,v&OPL->portDirection);
   /*TODO*///			}
   /*TODO*///			return;
   /*TODO*///		case 0x1a:		/* PCM data */
   /*TODO*///			return;
   /*TODO*///#endif
   /*TODO*///#endif
   /*TODO*///		}
   /*TODO*///		break;
   /*TODO*///	case 0x20:	/* am,vib,ksr,eg type,mul */
   /*TODO*///		slot = slot_array[r&0x1f];
   /*TODO*///		if(slot == -1) return;
   /*TODO*///		set_mul(OPL,slot,v);
   /*TODO*///		return;
   /*TODO*///	case 0x40:
   /*TODO*///		slot = slot_array[r&0x1f];
   /*TODO*///		if(slot == -1) return;
   /*TODO*///		set_ksl_tl(OPL,slot,v);
   /*TODO*///		return;
   /*TODO*///	case 0x60:
   /*TODO*///		slot = slot_array[r&0x1f];
   /*TODO*///		if(slot == -1) return;
   /*TODO*///		set_ar_dr(OPL,slot,v);
   /*TODO*///		return;
   /*TODO*///	case 0x80:
   /*TODO*///		slot = slot_array[r&0x1f];
   /*TODO*///		if(slot == -1) return;
   /*TODO*///		set_sl_rr(OPL,slot,v);
   /*TODO*///		return;
   /*TODO*///	case 0xa0:
   /*TODO*///		switch(r)
   /*TODO*///		{
   /*TODO*///		case 0xbd:
   /*TODO*///			/* amsep,vibdep,r,bd,sd,tom,tc,hh */
   /*TODO*///			{
   /*TODO*///			UINT8 rkey = OPL->rythm^v;
   /*TODO*///			OPL->ams_table = &AMS_TABLE[v&0x80 ? AMS_ENT : 0];
   /*TODO*///			OPL->vib_table = &VIB_TABLE[v&0x40 ? VIB_ENT : 0];
   /*TODO*///			OPL->rythm  = v&0x3f;
   /*TODO*///			if(OPL->rythm&0x20)
   /*TODO*///			{
   /*TODO*///#if 0
   /*TODO*///				usrintf_showmessage("OPL Rythm mode select");
   /*TODO*///#endif
   /*TODO*///				/* BD key on/off */
   /*TODO*///				if(rkey&0x10)
   /*TODO*///				{
   /*TODO*///					if(v&0x10)
   /*TODO*///					{
   /*TODO*///						OPL->P_CH[6].op1_out[0] = OPL->P_CH[6].op1_out[1] = 0;
   /*TODO*///						OPL_KEYON(&OPL->P_CH[6].SLOT[SLOT1]);
   /*TODO*///						OPL_KEYON(&OPL->P_CH[6].SLOT[SLOT2]);
   /*TODO*///					}
   /*TODO*///					else
   /*TODO*///					{
   /*TODO*///						OPL_KEYOFF(&OPL->P_CH[6].SLOT[SLOT1]);
   /*TODO*///						OPL_KEYOFF(&OPL->P_CH[6].SLOT[SLOT2]);
   /*TODO*///					}
   /*TODO*///				}
   /*TODO*///				/* SD key on/off */
   /*TODO*///				if(rkey&0x08)
   /*TODO*///				{
   /*TODO*///					if(v&0x08) OPL_KEYON(&OPL->P_CH[7].SLOT[SLOT2]);
   /*TODO*///					else       OPL_KEYOFF(&OPL->P_CH[7].SLOT[SLOT2]);
   /*TODO*///				}/* TAM key on/off */
   /*TODO*///				if(rkey&0x04)
   /*TODO*///				{
   /*TODO*///					if(v&0x04) OPL_KEYON(&OPL->P_CH[8].SLOT[SLOT1]);
   /*TODO*///					else       OPL_KEYOFF(&OPL->P_CH[8].SLOT[SLOT1]);
   /*TODO*///				}
   /*TODO*///				/* TOP-CY key on/off */
   /*TODO*///				if(rkey&0x02)
   /*TODO*///				{
   /*TODO*///					if(v&0x02) OPL_KEYON(&OPL->P_CH[8].SLOT[SLOT2]);
   /*TODO*///					else       OPL_KEYOFF(&OPL->P_CH[8].SLOT[SLOT2]);
   /*TODO*///				}
   /*TODO*///				/* HH key on/off */
   /*TODO*///				if(rkey&0x01)
   /*TODO*///				{
   /*TODO*///					if(v&0x01) OPL_KEYON(&OPL->P_CH[7].SLOT[SLOT1]);
   /*TODO*///					else       OPL_KEYOFF(&OPL->P_CH[7].SLOT[SLOT1]);
   /*TODO*///				}
   /*TODO*///			}
   /*TODO*///			}
   /*TODO*///			return;
   /*TODO*///		}
   /*TODO*///		/* keyon,block,fnum */
   /*TODO*///		if( (r&0x0f) > 8) return;
   /*TODO*///		CH = &OPL->P_CH[r&0x0f];
   /*TODO*///		if(!(r&0x10))
   /*TODO*///		{	/* a0-a8 */
   /*TODO*///			block_fnum  = (CH->block_fnum&0x1f00) | v;
   /*TODO*///		}
   /*TODO*///		else
   /*TODO*///		{	/* b0-b8 */
   /*TODO*///			int keyon = (v>>5)&1;
   /*TODO*///			block_fnum = ((v&0x1f)<<8) | (CH->block_fnum&0xff);
   /*TODO*///			if(CH->keyon != keyon)
   /*TODO*///			{
   /*TODO*///				if( (CH->keyon=keyon) )
   /*TODO*///				{
   /*TODO*///					CH->op1_out[0] = CH->op1_out[1] = 0;
   /*TODO*///					OPL_KEYON(&CH->SLOT[SLOT1]);
   /*TODO*///					OPL_KEYON(&CH->SLOT[SLOT2]);
   /*TODO*///				}
   /*TODO*///				else
   /*TODO*///				{
   /*TODO*///					OPL_KEYOFF(&CH->SLOT[SLOT1]);
   /*TODO*///					OPL_KEYOFF(&CH->SLOT[SLOT2]);
   /*TODO*///				}
   /*TODO*///			}
   /*TODO*///		}
   /*TODO*///		/* update */
   /*TODO*///		if(CH->block_fnum != block_fnum)
   /*TODO*///		{
   /*TODO*///			int blockRv = 7-(block_fnum>>10);
   /*TODO*///			int fnum   = block_fnum&0x3ff;
   /*TODO*///			CH->block_fnum = block_fnum;
   /*TODO*///
   /*TODO*///			CH->ksl_base = KSL_TABLE[block_fnum>>6];
   /*TODO*///			CH->fc = OPL->FN_TABLE[fnum]>>blockRv;
   /*TODO*///			CH->kcode = CH->block_fnum>>9;
   /*TODO*///			if( (OPL->mode&0x40) && CH->block_fnum&0x100) CH->kcode |=1;
   /*TODO*///			CALC_FCSLOT(CH,&CH->SLOT[SLOT1]);
   /*TODO*///			CALC_FCSLOT(CH,&CH->SLOT[SLOT2]);
   /*TODO*///		}
   /*TODO*///		return;
   /*TODO*///	case 0xc0:
   /*TODO*///		/* FB,C */
   /*TODO*///		if( (r&0x0f) > 8) return;
   /*TODO*///		CH = &OPL->P_CH[r&0x0f];
   /*TODO*///		{
   /*TODO*///		int feedback = (v>>1)&7;
   /*TODO*///		CH->FB   = feedback ? (8+1) - feedback : 0;
   /*TODO*///		CH->CON = v&1;
   /*TODO*///		set_algorythm(CH);
   /*TODO*///		}
   /*TODO*///		return;
   /*TODO*///	case 0xe0: /* wave type */
   /*TODO*///		slot = slot_array[r&0x1f];
   /*TODO*///		if(slot == -1) return;
   /*TODO*///		CH = &OPL->P_CH[slot/2];
   /*TODO*///		if(OPL->wavesel)
   /*TODO*///		{
   /*TODO*///			/* Log(LOG_INF,"OPL SLOT %d wave select %d\n",slot,v&3); */
   /*TODO*///			CH->SLOT[slot&1].wavetable = &SIN_TABLE[(v&0x03)*SIN_ENT];
   /*TODO*///		}
   /*TODO*///		return;
   /*TODO*///	}
    }

   /*TODO*////* lock/unlock for common table */
   /*TODO*///static int OPL_LockTable(void)
   /*TODO*///{
   /*TODO*///	num_lock++;
   /*TODO*///	if(num_lock>1) return 0;
   /*TODO*///	/* first time */
   /*TODO*///	cur_chip = NULL;
   /*TODO*///	/* allocate total level table (128kb space) */
   /*TODO*///	if( !OPLOpenTable() )
   /*TODO*///	{
   /*TODO*///		num_lock--;
   /*TODO*///		return -1;
   /*TODO*///	}
   /*TODO*///	return 0;
   /*TODO*///}
   /*TODO*///static void OPL_UnLockTable(void)
   /*TODO*///{
   /*TODO*///	if(num_lock) num_lock--;
   /*TODO*///	if(num_lock) return;
   /*TODO*///	/* last time */
   /*TODO*///	cur_chip = NULL;
   /*TODO*///	OPLCloseTable();
   /*TODO*///}
   /*TODO*///
   /*TODO*///#if (BUILD_YM3812 || BUILD_YM3526)
   /*TODO*////*******************************************************************************/
   /*TODO*////*		YM3812 local section                                                   */
   /*TODO*////*******************************************************************************/
   /*TODO*///
   /*TODO*////* ---------- update one of chip ----------- */
   public static void YM3812UpdateOne(FM_OPL OPL, UShortPtr buffer, int length)
   {
   /*TODO*///    int i;
   /*TODO*///	int data;
   /*TODO*///	FMSAMPLE *buf = buffer;
   /*TODO*///	UINT32 amsCnt  = OPL->amsCnt;
   /*TODO*///	UINT32 vibCnt  = OPL->vibCnt;
   /*TODO*///	UINT8 rythm = OPL->rythm&0x20;
   /*TODO*///	OPL_CH *CH,*R_CH;
   /*TODO*///
   /*TODO*///	if( (void *)OPL != cur_chip ){
   /*TODO*///		cur_chip = (void *)OPL;
   /*TODO*///		/* channel pointers */
   /*TODO*///		S_CH = OPL->P_CH;
   /*TODO*///		E_CH = &S_CH[9];
   /*TODO*///		/* rythm slot */
   /*TODO*///		SLOT7_1 = &S_CH[7].SLOT[SLOT1];
   /*TODO*///		SLOT7_2 = &S_CH[7].SLOT[SLOT2];
   /*TODO*///		SLOT8_1 = &S_CH[8].SLOT[SLOT1];
   /*TODO*///		SLOT8_2 = &S_CH[8].SLOT[SLOT2];
   /*TODO*///		/* LFO state */
   /*TODO*///		amsIncr = OPL->amsIncr;
   /*TODO*///		vibIncr = OPL->vibIncr;
   /*TODO*///		ams_table = OPL->ams_table;
   /*TODO*///		vib_table = OPL->vib_table;
   /*TODO*///	}
   /*TODO*///	R_CH = rythm ? &S_CH[6] : E_CH;
   /*TODO*///    for( i=0; i < length ; i++ )
   /*TODO*///	{
   /*TODO*///		/*            channel A         channel B         channel C      */
   /*TODO*///		/* LFO */
   /*TODO*///		ams = ams_table[(amsCnt+=amsIncr)>>AMS_SHIFT];
   /*TODO*///		vib = vib_table[(vibCnt+=vibIncr)>>VIB_SHIFT];
   /*TODO*///		outd[0] = 0;
   /*TODO*///		/* FM part */
   /*TODO*///		for(CH=S_CH ; CH < R_CH ; CH++)
   /*TODO*///			OPL_CALC_CH(CH);
   /*TODO*///		/* Rythn part */
   /*TODO*///		if(rythm)
   /*TODO*///			OPL_CALC_RH(S_CH);
   /*TODO*///		/* limit check */
   /*TODO*///		data = Limit( outd[0] , OPL_MAXOUT, OPL_MINOUT );
   /*TODO*///		/* store to sound buffer */
   /*TODO*///		buf[i] = data >> OPL_OUTSB;
   /*TODO*///	}
   /*TODO*///
   /*TODO*///	OPL->amsCnt = amsCnt;
   /*TODO*///	OPL->vibCnt = vibCnt;
   }
   /*TODO*///#endif /* (BUILD_YM3812 || BUILD_YM3526) */
   /*TODO*///
   /*TODO*///#if BUILD_Y8950
   /*TODO*///
   /*TODO*///void Y8950UpdateOne(FM_OPL *OPL, INT16 *buffer, int length)
   /*TODO*///{
   /*TODO*///    int i;
   /*TODO*///	int data;
   /*TODO*///	FMSAMPLE *buf = buffer;
   /*TODO*///	UINT32 amsCnt  = OPL->amsCnt;
   /*TODO*///	UINT32 vibCnt  = OPL->vibCnt;
   /*TODO*///	UINT8 rythm = OPL->rythm&0x20;
   /*TODO*///	OPL_CH *CH,*R_CH;
   /*TODO*///	YM_DELTAT *DELTAT = OPL->deltat;
   /*TODO*///
   /*TODO*///	/* setup DELTA-T unit */
   /*TODO*///	YM_DELTAT_DECODE_PRESET(DELTAT);
   /*TODO*///
   /*TODO*///	if( (void *)OPL != cur_chip ){
   /*TODO*///		cur_chip = (void *)OPL;
   /*TODO*///		/* channel pointers */
   /*TODO*///		S_CH = OPL->P_CH;
   /*TODO*///		E_CH = &S_CH[9];
   /*TODO*///		/* rythm slot */
   /*TODO*///		SLOT7_1 = &S_CH[7].SLOT[SLOT1];
   /*TODO*///		SLOT7_2 = &S_CH[7].SLOT[SLOT2];
   /*TODO*///		SLOT8_1 = &S_CH[8].SLOT[SLOT1];
   /*TODO*///		SLOT8_2 = &S_CH[8].SLOT[SLOT2];
   /*TODO*///		/* LFO state */
   /*TODO*///		amsIncr = OPL->amsIncr;
   /*TODO*///		vibIncr = OPL->vibIncr;
   /*TODO*///		ams_table = OPL->ams_table;
   /*TODO*///		vib_table = OPL->vib_table;
   /*TODO*///	}
   /*TODO*///	R_CH = rythm ? &S_CH[6] : E_CH;
   /*TODO*///    for( i=0; i < length ; i++ )
   /*TODO*///	{
   /*TODO*///		/*            channel A         channel B         channel C      */
   /*TODO*///		/* LFO */
   /*TODO*///		ams = ams_table[(amsCnt+=amsIncr)>>AMS_SHIFT];
   /*TODO*///		vib = vib_table[(vibCnt+=vibIncr)>>VIB_SHIFT];
   /*TODO*///		outd[0] = 0;
   /*TODO*///		/* deltaT ADPCM */
   /*TODO*///		if( DELTAT->flag )
   /*TODO*///			YM_DELTAT_ADPCM_CALC(DELTAT);
   /*TODO*///		/* FM part */
   /*TODO*///		for(CH=S_CH ; CH < R_CH ; CH++)
   /*TODO*///			OPL_CALC_CH(CH);
   /*TODO*///		/* Rythn part */
   /*TODO*///		if(rythm)
   /*TODO*///			OPL_CALC_RH(S_CH);
   /*TODO*///		/* limit check */
   /*TODO*///		data = Limit( outd[0] , OPL_MAXOUT, OPL_MINOUT );
   /*TODO*///		/* store to sound buffer */
   /*TODO*///		buf[i] = data >> OPL_OUTSB;
   /*TODO*///	}
   /*TODO*///	OPL->amsCnt = amsCnt;
   /*TODO*///	OPL->vibCnt = vibCnt;
   /*TODO*///	/* deltaT START flag */
   /*TODO*///	if( !DELTAT->flag )
   /*TODO*///		OPL->status &= 0xfe;
   /*TODO*///}
   /*TODO*///#endif
   
   /* ---------- reset one of chip ---------- */
   public static void OPLResetChip(FM_OPL OPL)
   {
   	int c,s;
   	int i;
   /*TODO*///
   /*TODO*///	/* reset chip */
   /*TODO*///	OPL->mode   = 0;	/* normal mode */
   /*TODO*///	OPL_STATUS_RESET(OPL,0x7f);
   /*TODO*///	/* reset with register write */
   /*TODO*///	OPLWriteReg(OPL,0x01,0); /* wabesel disable */
   /*TODO*///	OPLWriteReg(OPL,0x02,0); /* Timer1 */
   /*TODO*///	OPLWriteReg(OPL,0x03,0); /* Timer2 */
   /*TODO*///	OPLWriteReg(OPL,0x04,0); /* IRQ mask clear */
   /*TODO*///	for(i = 0xff ; i >= 0x20 ; i-- ) OPLWriteReg(OPL,i,0);
   /*TODO*///	/* reset OPerator paramater */
   /*TODO*///	for( c = 0 ; c < OPL->max_ch ; c++ )
   /*TODO*///	{
   /*TODO*///		OPL_CH *CH = &OPL->P_CH[c];
   /*TODO*///		/* OPL->P_CH[c].PAN = OPN_CENTER; */
   /*TODO*///		for(s = 0 ; s < 2 ; s++ )
   /*TODO*///		{
   /*TODO*///			/* wave table */
   /*TODO*///			CH->SLOT[s].wavetable = &SIN_TABLE[0];
   /*TODO*///			/* CH->SLOT[s].evm = ENV_MOD_RR; */
   /*TODO*///			CH->SLOT[s].evc = EG_OFF;
   /*TODO*///			CH->SLOT[s].eve = EG_OFF+1;
   /*TODO*///			CH->SLOT[s].evs = 0;
   /*TODO*///		}
   /*TODO*///	}
   /*TODO*///#if BUILD_Y8950
   /*TODO*///	if(OPL->type&OPL_TYPE_ADPCM)
   /*TODO*///	{
   /*TODO*///		YM_DELTAT *DELTAT = OPL->deltat;
   /*TODO*///
   /*TODO*///		DELTAT->freqbase = OPL->freqbase;
   /*TODO*///		DELTAT->output_pointer = outd;
   /*TODO*///		DELTAT->portshift = 5;
   /*TODO*///		DELTAT->output_range = DELTAT_MIXING_LEVEL<<TL_BITS;
   /*TODO*///		YM_DELTAT_ADPCM_Reset(DELTAT,0);
   /*TODO*///	}
   /*TODO*///#endif
   }
   
   /* ----------  Create one of vietual YM3812 ----------       */
   /* 'rate'  is sampling rate and 'bufsiz' is the size of the  */
    public static FM_OPL OPLCreate(int type, int clock, int rate)
    {

   /*TODO*///	char *ptr;
   	FM_OPL OPL;
   /*TODO*///	int state_size;
   	int max_ch = 9; /* normaly 9 channels */
   /*TODO*///
   /*TODO*///	if( OPL_LockTable() ==-1) return NULL;
   /*TODO*///	/* allocate OPL state space */
   /*TODO*///	state_size  = sizeof(FM_OPL);
   /*TODO*///	state_size += sizeof(OPL_CH)*max_ch;
   /*TODO*///#if BUILD_Y8950
   /*TODO*///	if(type&OPL_TYPE_ADPCM) state_size+= sizeof(YM_DELTAT);
   /*TODO*///#endif
   /*TODO*///	/* allocate memory block */
   /*TODO*///	ptr = malloc(state_size);
   /*TODO*///	if(ptr==NULL) return NULL;
   /*TODO*///	/* clear */
   /*TODO*///	memset(ptr,0,state_size);
                OPL = new FM_OPL();  //OPL        = (FM_OPL *)ptr; ptr+=sizeof(FM_OPL);
   /*TODO*///	OPL->P_CH  = (OPL_CH *)ptr; ptr+=sizeof(OPL_CH)*max_ch;
   /*TODO*///#if BUILD_Y8950
   /*TODO*///	if(type&OPL_TYPE_ADPCM) OPL->deltat = (YM_DELTAT *)ptr; ptr+=sizeof(YM_DELTAT);
   /*TODO*///#endif
   	/* set channel state pointer */
   	OPL.type  = type;
   	OPL.clock = clock;
   	OPL.rate  = rate;
   	OPL.max_ch = max_ch;
   	/* init grobal tables */
   	OPL_initalize(OPL);
   	/* reset chip */
   	OPLResetChip(OPL);
   	return OPL;
   }
   /* ----------  Destroy one of vietual YM3812 ----------       */
   public static void OPLDestroy(FM_OPL OPL)
   {
   /*TODO*///	OPL_UnLockTable();
   /*TODO*///	free(OPL);
   }
   /* ----------  Option handlers ----------       */
   
   public static void OPLSetTimerHandler(FM_OPL OPL,OPL_TIMERHANDLERPtr TimerHandler,int channelOffset)
   {
   	OPL.TimerHandler   = TimerHandler;
   	OPL.TimerParam = channelOffset;
   }
   public static void OPLSetIRQHandler(FM_OPL OPL,OPL_IRQHANDLERPtr IRQHandler,int param)
   {
   	OPL.IRQHandler     = IRQHandler;
   	OPL.IRQParam = param;
   }
   public static void OPLSetUpdateHandler(FM_OPL OPL,OPL_UPDATEHANDLERPtr UpdateHandler,int param)
   {
   	OPL.UpdateHandler = UpdateHandler;
   	OPL.UpdateParam = param;
   }
   /*TODO*///#if BUILD_Y8950
   /*TODO*///void OPLSetPortHandler(FM_OPL *OPL,OPL_PORTHANDLER_W PortHandler_w,OPL_PORTHANDLER_R PortHandler_r,int param)
   /*TODO*///{
   /*TODO*///	OPL->porthandler_w = PortHandler_w;
   /*TODO*///	OPL->porthandler_r = PortHandler_r;
   /*TODO*///	OPL->port_param = param;
   /*TODO*///}
   /*TODO*///
   /*TODO*///void OPLSetKeyboardHandler(FM_OPL *OPL,OPL_PORTHANDLER_W KeyboardHandler_w,OPL_PORTHANDLER_R KeyboardHandler_r,int param)
   /*TODO*///{
   /*TODO*///	OPL->keyboardhandler_w = KeyboardHandler_w;
   /*TODO*///	OPL->keyboardhandler_r = KeyboardHandler_r;
   /*TODO*///	OPL->keyboard_param = param;
   /*TODO*///}
   /*TODO*///#endif
   /*TODO*////* ---------- YM3812 I/O interface ---------- */
   public static int OPLWrite(FM_OPL OPL,int a,int v)
   {
   	if( (a&1)==0 )
   	{	/* address port */
   		OPL.address = v & 0xff;
   	}
   	else
   	{	/* data port */
   		if(OPL.UpdateHandler!=null) OPL.UpdateHandler.handler(OPL.UpdateParam,0);
   		OPLWriteReg(OPL,OPL.address,v);
   	}
   	return (OPL.status>>7)&0xFF; //status is uint8
       
   }
   public static /*unsigned*/char OPLRead(FM_OPL OPL,int a)
   {
   /*TODO*///	if( !(a&1) )
   /*TODO*///	{	/* status port */
   /*TODO*///		return OPL->status & (OPL->statusmask|0x80);
   /*TODO*///	}
   /*TODO*///	/* data port */
   /*TODO*///	switch(OPL->address)
   /*TODO*///	{
   /*TODO*///	case 0x05: /* KeyBoard IN */
   /*TODO*///		if(OPL->type&OPL_TYPE_KEYBOARD)
   /*TODO*///		{
   /*TODO*///			if(OPL->keyboardhandler_r)
   /*TODO*///				return OPL->keyboardhandler_r(OPL->keyboard_param);
   /*TODO*///			else
   /*TODO*///				Log(LOG_WAR,"OPL:read unmapped KEYBOARD port\n");
   /*TODO*///		}
   /*TODO*///		return 0;
   /*TODO*///	case 0x19: /* I/O DATA    */
   /*TODO*///		if(OPL->type&OPL_TYPE_IO)
   /*TODO*///		{
   /*TODO*///			if(OPL->porthandler_r)
   /*TODO*///				return OPL->porthandler_r(OPL->port_param);
   /*TODO*///			else
   /*TODO*///				Log(LOG_WAR,"OPL:read unmapped I/O port\n");
   /*TODO*///		}
   /*TODO*///		return 0;
   /*TODO*///	case 0x1a: /* PCM-DATA    */
   /*TODO*///		return 0;
   /*TODO*///	}
	return 0;
    }
   public static int OPLTimerOver(FM_OPL OPL,int c)
   {
       throw new UnsupportedOperationException("unimplemented");
   /*TODO*///	if( c )
   /*TODO*///	{	/* Timer B */
   /*TODO*///		OPL_STATUS_SET(OPL,0x20);
   /*TODO*///	}
   /*TODO*///	else
   /*TODO*///	{	/* Timer A */
   /*TODO*///		OPL_STATUS_SET(OPL,0x40);
   /*TODO*///		/* CSM mode key,TL controll */
   /*TODO*///		if( OPL->mode & 0x80 )
   /*TODO*///		{	/* CSM mode total level latch and auto key on */
   /*TODO*///			int ch;
   /*TODO*///			if(OPL->UpdateHandler) OPL->UpdateHandler(OPL->UpdateParam,0);
   /*TODO*///			for(ch=0;ch<9;ch++)
   /*TODO*///				CSMKeyControll( &OPL->P_CH[ch] );
   /*TODO*///		}
   /*TODO*///	}
   /*TODO*///	/* reload timer */
   /*TODO*///	if (OPL->TimerHandler) (OPL->TimerHandler)(OPL->TimerParam+c,(double)OPL->T[c]*OPL->TimerBase);
   /*TODO*///	return OPL->status>>7;
   }   
}
