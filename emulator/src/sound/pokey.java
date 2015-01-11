package sound;

import static arcadeflex.libc_old.*;
import static arcadeflex.ptrlib.*;
import static mame.sndintrfH.*;
import static sound.pokeyH.*;
import static mame.mame.*;
import static sound.streams.*;
import static mame.driverH.*;
import static mame.common.*;
import static mame.sndintrf.*;
import static mame.timer.*;
import static mame.timerH.*;

public class pokey extends snd_interface {

    public pokey() {
        this.sound_num = SOUND_POKEY;
        this.name = "Pokey";
        for (int i = 0; i < MAXPOKEYS; i++) {
            _pokey[i] = new POKEYregisters();
        }
    }

    @Override
    public int chips_num(MachineSound msound) {
        return ((POKEYinterface) msound.sound_interface).num;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return ((POKEYinterface) msound.sound_interface).baseclock;
    }

    @Override
    public void update() {
        //no functionality expected
    }

    @Override
    public void reset() {
        //no functionality expected
    }

    /*TODO*///#ifndef BIG_SWITCH
/*TODO*///#ifndef HEAVY_MACRO_USAGE
/*TODO*///#define HEAVY_MACRO_USAGE   1
/*TODO*///#endif
/*TODO*///#else
/*TODO*///#define HEAVY_MACRO_USAGE	BIG_SWITCH
/*TODO*///#endif
/*TODO*///
/*TODO*///#define SUPPRESS_INAUDIBLE	1
/*TODO*///
/*TODO*////* Four channels with a range of 0..32767 and volume 0..15 */
/*TODO*/////#define POKEY_DEFAULT_GAIN (32767/15/4)
/*TODO*///
/*TODO*////*
/*TODO*/// * But we raise the gain and risk clipping, the old Pokey did
/*TODO*/// * this too. It defined POKEY_DEFAULT_GAIN 6 and this was
/*TODO*/// * 6 * 15 * 4 = 360, 360/256 = 1.40625
/*TODO*/// * I use 15/11 = 1.3636, so this is a little lower.
/*TODO*/// */
/*TODO*///#define POKEY_DEFAULT_GAIN (32767/11/4)
/*TODO*///
/*TODO*///#define VERBOSE 		0
/*TODO*///#define VERBOSE_SOUND	0
/*TODO*///#define VERBOSE_TIMER	0
/*TODO*///#define VERBOSE_POLY	0
/*TODO*///#define VERBOSE_RAND	0
/*TODO*///
/*TODO*///#if VERBOSE
/*TODO*///#define LOG(x) if( errorlog ) fprintf x
/*TODO*///#else
/*TODO*///#define LOG(x)
/*TODO*///#endif
/*TODO*///
/*TODO*///#if VERBOSE_SOUND
/*TODO*///#define LOG_SOUND(x) if( errorlog ) fprintf x
/*TODO*///#else
/*TODO*///#define LOG_SOUND(x)
/*TODO*///#endif
/*TODO*///
/*TODO*///#if VERBOSE_TIMER
/*TODO*///#define LOG_TIMER(x) if( errorlog ) fprintf x
/*TODO*///#else
/*TODO*///#define LOG_TIMER(x)
/*TODO*///#endif
/*TODO*///
/*TODO*///#if VERBOSE_POLY
/*TODO*///#define LOG_POLY(x) if( errorlog ) fprintf x
/*TODO*///#else
/*TODO*///#define LOG_POLY(x)
/*TODO*///#endif
/*TODO*///
/*TODO*///#if VERBOSE_RAND
/*TODO*///#define LOG_RAND(x) if( errorlog ) fprintf x
/*TODO*///#else
/*TODO*///#define LOG_RAND(x)
/*TODO*///#endif
/*TODO*///
    public static final int CHAN1 = 0;
    public static final int CHAN2 = 1;
    public static final int CHAN3 = 2;
    public static final int CHAN4 = 3;

    public static final int TIMER1 = 0;
    public static final int TIMER2 = 1;
    public static final int TIMER4 = 2;
    /*TODO*///
/*TODO*////* values to add to the divisors for the different modes */
/*TODO*///#define DIVADD_LOCLK		1
/*TODO*///#define DIVADD_HICLK		4
/*TODO*///#define DIVADD_HICLK_JOINED 7
/*TODO*///
/*TODO*////* AUDCx */
/*TODO*///#define NOTPOLY5	0x80	/* selects POLY5 or direct CLOCK */
/*TODO*///#define POLY4		0x40	/* selects POLY4 or POLY17 */
/*TODO*///#define PURE		0x20	/* selects POLY4/17 or PURE tone */
/*TODO*///#define VOLUME_ONLY 0x10	/* selects VOLUME OUTPUT ONLY */
/*TODO*///#define VOLUME_MASK 0x0f	/* volume mask */
/*TODO*///
/*TODO*////* AUDCTL */
/*TODO*///#define POLY9		0x80	/* selects POLY9 or POLY17 */
/*TODO*///#define CH1_HICLK	0x40	/* selects 1.78979 MHz for Ch 1 */
/*TODO*///#define CH3_HICLK	0x20	/* selects 1.78979 MHz for Ch 3 */
/*TODO*///#define CH12_JOINED 0x10	/* clocks channel 1 w/channel 2 */
/*TODO*///#define CH34_JOINED 0x08	/* clocks channel 3 w/channel 4 */
/*TODO*///#define CH1_FILTER	0x04	/* selects channel 1 high pass filter */
/*TODO*///#define CH2_FILTER	0x02	/* selects channel 2 high pass filter */
/*TODO*///#define CLK_15KHZ	0x01	/* selects 15.6999 kHz or 63.9211 kHz */
/*TODO*///
/*TODO*////* IRQEN (D20E) */
/*TODO*///#define IRQ_BREAK	0x80	/* BREAK key pressed interrupt */
/*TODO*///#define IRQ_KEYBD	0x40	/* keyboard data ready interrupt */
/*TODO*///#define IRQ_SERIN	0x20	/* serial input data ready interrupt */
/*TODO*///#define IRQ_SEROR	0x10	/* serial output register ready interrupt */
/*TODO*///#define IRQ_SEROC	0x08	/* serial output complete interrupt */
/*TODO*///#define IRQ_TIMR4	0x04	/* timer channel #4 interrupt */
/*TODO*///#define IRQ_TIMR2	0x02	/* timer channel #2 interrupt */
/*TODO*///#define IRQ_TIMR1	0x01	/* timer channel #1 interrupt */
/*TODO*///
/*TODO*////* SKSTAT (R/D20F) */
/*TODO*///#define SK_FRAME	0x80	/* serial framing error */
/*TODO*///#define SK_OVERRUN	0x40	/* serial overrun error */
/*TODO*///#define SK_KBERR	0x20	/* keyboard overrun error */
/*TODO*///#define SK_SERIN	0x10	/* serial input high */
/*TODO*///#define SK_SHIFT	0x08	/* shift key pressed */
/*TODO*///#define SK_KEYBD	0x04	/* keyboard key pressed */
/*TODO*///#define SK_SEROUT	0x02	/* serial output active */
/*TODO*///
/*TODO*////* SKCTL (W/D20F) */
/*TODO*///#define SK_BREAK	0x80	/* serial out break signal */
/*TODO*///#define SK_BPS		0x70	/* bits per second */
/*TODO*///#define SK_FM		0x08	/* FM mode */
/*TODO*///#define SK_PADDLE	0x04	/* fast paddle a/d conversion */
    public static final int SK_RESET = 0x03;	/* reset serial/keyboard interface */

    public static final int DIV_64 = 28;		 /* divisor for 1.78979 MHz clock to 63.9211 kHz */
    /*TODO*///#define DIV_15		114 	 /* divisor for 1.78979 MHz clock to 15.6999 kHz */
/*TODO*///


    public static class POKEYregisters {

        int[] counter = new int[4];		/* channel counter */

        int[] divisor = new int[4];		/* channel divisor (modulo value) */

        long[]/*UINT32*/ volume = new long[4];		/* channel volume - derived */

        int[]/*UINT8*/ output = new int[4];		/* channel output signal (1 active, 0 inactive) */

        int[]/*UINT8*/ audible = new int[4];		/* channel plays an audible tone/effect */

        long /*UINT32*/ samplerate_24_8; /* sample rate in 24.8 format */

        long /*UINT32*/ samplepos_fract; /* sample position fractional part */

        long /*UINT32*/ samplepos_whole; /* sample position whole part */

        long /*UINT32*/ polyadjust;		/* polynome adjustment */

        long /*UINT32*/ p4;              /* poly4 index */

        long /*UINT32*/ p5;              /* poly5 index */

        long /*UINT32*/ p9;              /* poly9 index */

        long /*UINT32*/ p17;             /* poly17 index */

        long /*UINT32*/ r9;				/* rand9 index */

        long /*UINT32*/ r17;             /* rand17 index */

        long /*UINT32*/ clockmult;		/* clock multiplier */

        int channel;            /* streams channel */

        timer_entry[] timer = new timer_entry[3]; 		/* timers for channel 1,2 and 4 events */

        timer_entry rtimer;           /* timer for calculating the random offset */

        timer_entry[] ptimer = new timer_entry[8];		/* pot timers */

        ReadHandlerPtr[] pot_r = new ReadHandlerPtr[8];
        ReadHandlerPtr allpot_r;
        ReadHandlerPtr serin_r;
        WriteHandlerPtr serout_w;
        interrupt_cbPtr interrupt_cb;
        int[]/*UINT8*/ AUDF = new int[4];          /* AUDFx (D200, D202, D204, D206) */

        int[]/*UINT8*/ AUDC = new int[4];			/* AUDCx (D201, D203, D205, D207) */

        int[]/*UINT8*/ POTx = new int[8];			/* POTx   (R/D200-D207) */

        int/*UINT8*/ AUDCTL;			/* AUDCTL (W/D208) */

        int/*UINT8*/ ALLPOT;			/* ALLPOT (R/D208) */

        int/*UINT8*/ KBCODE;			/* KBCODE (R/D209) */

        int/*UINT8*/ RANDOM;			/* RANDOM (R/D20A) */

        int/*UINT8*/ SERIN;			/* SERIN  (R/D20D) */

        int/*UINT8*/ SEROUT;			/* SEROUT (W/D20D) */

        int/*UINT8*/ IRQST;			/* IRQST  (R/D20E) */

        int/*UINT8*/ IRQEN;			/* IRQEN  (W/D20E) */

        int/*UINT8*/ SKSTAT;			/* SKSTAT (R/D20F) */

        int/*UINT8*/ SKCTL;			/* SKCTL  (W/D20F) */

    };

    static POKEYinterface intf;
    static POKEYregisters[] _pokey = new POKEYregisters[MAXPOKEYS];

    static /*UINT8*/ char[] poly4 = new char[0x0f];
    static /*UINT8*/ char[] poly5 = new char[0x1f];
    static /*UINT8*/ char[] poly9;
    static /*UINT8*/ char[] poly17;

    /*TODO*///#define P4(chip)  poly4[pokey[chip].p4]
/*TODO*///#define P5(chip)  poly5[pokey[chip].p5]
/*TODO*///#define P9(chip)  poly9[pokey[chip].p9]
/*TODO*///#define P17(chip) poly17[pokey[chip].p17]
/*TODO*///
    /* 128K random values derived from the 17bit polynome */
    static /*UINT8*/ char[] rand9;
    static /*UINT8*/ char[] rand17;
    /*TODO*///
/*TODO*///#define SAMPLE	-1
/*TODO*///
/*TODO*///#define ADJUST_EVENT(chip)												\
/*TODO*///	pokey[chip].counter[CHAN1] -= event;								\
/*TODO*///	pokey[chip].counter[CHAN2] -= event;								\
/*TODO*///	pokey[chip].counter[CHAN3] -= event;								\
/*TODO*///	pokey[chip].counter[CHAN4] -= event;								\
/*TODO*///	pokey[chip].samplepos_whole -= event;								\
/*TODO*///	pokey[chip].polyadjust += event
/*TODO*///
/*TODO*///#if SUPPRESS_INAUDIBLE
/*TODO*///
/*TODO*///#define PROCESS_CHANNEL(chip,ch)                                        \
/*TODO*///	int toggle = 0; 													\
/*TODO*///	ADJUST_EVENT(chip); 												\
/*TODO*///	/* reset the channel counter */ 									\
/*TODO*///	if( pokey[chip].audible[ch] )										\
/*TODO*///		pokey[chip].counter[ch] = pokey[chip].divisor[ch];				\
/*TODO*///	else																\
/*TODO*///		pokey[chip].counter[ch] = 0x7fffffff;							\
/*TODO*///	pokey[chip].p4 = (pokey[chip].p4+pokey[chip].polyadjust)%0x0000f;	\
/*TODO*///	pokey[chip].p5 = (pokey[chip].p5+pokey[chip].polyadjust)%0x0001f;	\
/*TODO*///	pokey[chip].p9 = (pokey[chip].p9+pokey[chip].polyadjust)%0x001ff;	\
/*TODO*///	pokey[chip].p17 = (pokey[chip].p17+pokey[chip].polyadjust)%0x1ffff; \
/*TODO*///	pokey[chip].polyadjust = 0; 										\
/*TODO*///	if( (pokey[chip].AUDC[ch] & NOTPOLY5) || P5(chip) ) 				\
/*TODO*///	{																	\
/*TODO*///		if( pokey[chip].AUDC[ch] & PURE )								\
/*TODO*///			toggle = 1; 												\
/*TODO*///		else															\
/*TODO*///		if( pokey[chip].AUDC[ch] & POLY4 )								\
/*TODO*///			toggle = pokey[chip].output[ch] == !P4(chip);				\
/*TODO*///		else															\
/*TODO*///		if( pokey[chip].AUDCTL & POLY9 )								\
/*TODO*///			toggle = pokey[chip].output[ch] == !P9(chip);				\
/*TODO*///		else															\
/*TODO*///			toggle = pokey[chip].output[ch] == !P17(chip);				\
/*TODO*///	}																	\
/*TODO*///	if( toggle )														\
/*TODO*///	{																	\
/*TODO*///		if( pokey[chip].audible[ch] )									\
/*TODO*///		{																\
/*TODO*///			if( pokey[chip].output[ch] )								\
/*TODO*///				sum -= pokey[chip].volume[ch];							\
/*TODO*///			else														\
/*TODO*///				sum += pokey[chip].volume[ch];							\
/*TODO*///		}																\
/*TODO*///		pokey[chip].output[ch] ^= 1;									\
/*TODO*///	}																	\
/*TODO*///	/* is this a filtering channel (3/4) and is the filter active? */	\
/*TODO*///	if( pokey[chip].AUDCTL & ((CH1_FILTER|CH2_FILTER) & (0x10 >> ch)) ) \
/*TODO*///    {                                                                   \
/*TODO*///		if( pokey[chip].output[ch-2] )									\
/*TODO*///        {                                                               \
/*TODO*///			pokey[chip].output[ch-2] = 0;								\
/*TODO*///			if( pokey[chip].audible[ch] )								\
/*TODO*///				sum -= pokey[chip].volume[ch-2];						\
/*TODO*///        }                                                               \
/*TODO*///    }                                                                   \
/*TODO*///
/*TODO*///#else
/*TODO*///
/*TODO*///#define PROCESS_CHANNEL(chip,ch)                                        \
/*TODO*///	int toggle = 0; 													\
/*TODO*///	ADJUST_EVENT(chip); 												\
/*TODO*///	/* reset the channel counter */ 									\
/*TODO*///	pokey[chip].counter[ch] = p[chip].divisor[ch];						\
/*TODO*///	pokey[chip].p4 = (pokey[chip].p4+pokey[chip].polyadjust)%0x0000f;	\
/*TODO*///	pokey[chip].p5 = (pokey[chip].p5+pokey[chip].polyadjust)%0x0001f;	\
/*TODO*///	pokey[chip].p9 = (pokey[chip].p9+pokey[chip].polyadjust)%0x001ff;	\
/*TODO*///	pokey[chip].p17 = (pokey[chip].p17+pokey[chip].polyadjust)%0x1ffff; \
/*TODO*///	pokey[chip].polyadjust = 0; 										\
/*TODO*///	if( (pokey[chip].AUDC[ch] & NOTPOLY5) || P5(chip) ) 				\
/*TODO*///	{																	\
/*TODO*///		if( pokey[chip].AUDC[ch] & PURE )								\
/*TODO*///			toggle = 1; 												\
/*TODO*///		else															\
/*TODO*///		if( pokey[chip].AUDC[ch] & POLY4 )								\
/*TODO*///			toggle = pokey[chip].output[ch] == !P4(chip);				\
/*TODO*///		else															\
/*TODO*///		if( pokey[chip].AUDCTL & POLY9 )								\
/*TODO*///			toggle = pokey[chip].output[ch] == !P9(chip);				\
/*TODO*///		else															\
/*TODO*///			toggle = pokey[chip].output[ch] == !P17(chip);				\
/*TODO*///	}																	\
/*TODO*///	if( toggle )														\
/*TODO*///	{																	\
/*TODO*///		if( pokey[chip].output[ch] )									\
/*TODO*///			sum -= pokey[chip].volume[ch];								\
/*TODO*///		else															\
/*TODO*///			sum += pokey[chip].volume[ch];								\
/*TODO*///		pokey[chip].output[ch] ^= 1;									\
/*TODO*///	}																	\
/*TODO*///	/* is this a filtering channel (3/4) and is the filter active? */	\
/*TODO*///	if( pokey[chip].AUDCTL & ((CH1_FILTER|CH2_FILTER) & (0x10 >> ch)) ) \
/*TODO*///    {                                                                   \
/*TODO*///		if( pokey[chip].output[ch-2] )									\
/*TODO*///        {                                                               \
/*TODO*///			pokey[chip].output[ch-2] = 0;								\
/*TODO*///			sum -= pokey[chip].volume[ch-2];							\
/*TODO*///        }                                                               \
/*TODO*///    }                                                                   \
/*TODO*///
/*TODO*///#endif
/*TODO*///
/*TODO*///#define PROCESS_SAMPLE(chip)                                            \
/*TODO*///    ADJUST_EVENT(chip);                                                 \
/*TODO*///    /* adjust the sample position */                                    \
/*TODO*///	pokey[chip].samplepos_fract += pokey[chip].samplerate_24_8; 		\
/*TODO*///	if( pokey[chip].samplepos_fract & 0xffffff00 )						\
/*TODO*///	{																	\
/*TODO*///		pokey[chip].samplepos_whole += pokey[chip].samplepos_fract>>8;	\
/*TODO*///		pokey[chip].samplepos_fract &= 0x000000ff;						\
/*TODO*///	}																	\
/*TODO*///	/* store sum of output signals into the buffer */					\
/*TODO*///	*buffer++ = (sum > 65535) ? 0x7fff : sum - 0x8000;					\
/*TODO*///	length--
/*TODO*///
/*TODO*///#if HEAVY_MACRO_USAGE
/*TODO*///
/*TODO*////*
/*TODO*/// * This version of PROCESS_POKEY repeats the search for the minimum
/*TODO*/// * event value without using an index to the channel. That way the
/*TODO*/// * PROCESS_CHANNEL macros can be called with fixed values and expand
/*TODO*/// * to much more efficient code
/*TODO*/// */
/*TODO*///
/*TODO*///#define PROCESS_POKEY(chip) 											\
/*TODO*///	UINT32 sum = 0; 													\
/*TODO*///	if( pokey[chip].output[CHAN1] ) 									\
/*TODO*///		sum += pokey[chip].volume[CHAN1];								\
/*TODO*///	if( pokey[chip].output[CHAN2] ) 									\
/*TODO*///		sum += pokey[chip].volume[CHAN2];								\
/*TODO*///	if( pokey[chip].output[CHAN3] ) 									\
/*TODO*///		sum += pokey[chip].volume[CHAN3];								\
/*TODO*///	if( pokey[chip].output[CHAN4] ) 									\
/*TODO*///		sum += pokey[chip].volume[CHAN4];								\
/*TODO*///    while( length > 0 )                                                 \
/*TODO*///	{																	\
/*TODO*///		if( pokey[chip].counter[CHAN1] < pokey[chip].samplepos_whole )	\
/*TODO*///		{																\
/*TODO*///			if( pokey[chip].counter[CHAN2] <  pokey[chip].counter[CHAN1] ) \
/*TODO*///			{															\
/*TODO*///				if( pokey[chip].counter[CHAN3] <  pokey[chip].counter[CHAN2] ) \
/*TODO*///				{														\
/*TODO*///					if( pokey[chip].counter[CHAN4] < pokey[chip].counter[CHAN3] ) \
/*TODO*///					{													\
/*TODO*///						UINT32 event = pokey[chip].counter[CHAN4];		\
/*TODO*///                        PROCESS_CHANNEL(chip,CHAN4);                    \
/*TODO*///					}													\
/*TODO*///					else												\
/*TODO*///					{													\
/*TODO*///						UINT32 event = pokey[chip].counter[CHAN3];		\
/*TODO*///                        PROCESS_CHANNEL(chip,CHAN3);                    \
/*TODO*///					}													\
/*TODO*///				}														\
/*TODO*///				else													\
/*TODO*///				if( pokey[chip].counter[CHAN4] < pokey[chip].counter[CHAN2] )  \
/*TODO*///				{														\
/*TODO*///					UINT32 event = pokey[chip].counter[CHAN4];			\
/*TODO*///                    PROCESS_CHANNEL(chip,CHAN4);                        \
/*TODO*///				}														\
/*TODO*///                else                                                    \
/*TODO*///				{														\
/*TODO*///					UINT32 event = pokey[chip].counter[CHAN2];			\
/*TODO*///                    PROCESS_CHANNEL(chip,CHAN2);                        \
/*TODO*///				}														\
/*TODO*///            }                                                           \
/*TODO*///			else														\
/*TODO*///			if( pokey[chip].counter[CHAN3] < pokey[chip].counter[CHAN1] ) \
/*TODO*///			{															\
/*TODO*///				if( pokey[chip].counter[CHAN4] < pokey[chip].counter[CHAN3] ) \
/*TODO*///				{														\
/*TODO*///					UINT32 event = pokey[chip].counter[CHAN4];			\
/*TODO*///                    PROCESS_CHANNEL(chip,CHAN4);                        \
/*TODO*///				}														\
/*TODO*///                else                                                    \
/*TODO*///				{														\
/*TODO*///					UINT32 event = pokey[chip].counter[CHAN3];			\
/*TODO*///                    PROCESS_CHANNEL(chip,CHAN3);                        \
/*TODO*///				}														\
/*TODO*///            }                                                           \
/*TODO*///			else														\
/*TODO*///			if( pokey[chip].counter[CHAN4] < pokey[chip].counter[CHAN1] ) \
/*TODO*///			{															\
/*TODO*///				UINT32 event = pokey[chip].counter[CHAN4];				\
/*TODO*///                PROCESS_CHANNEL(chip,CHAN4);                            \
/*TODO*///			}															\
/*TODO*///            else                                                        \
/*TODO*///			{															\
/*TODO*///				UINT32 event = pokey[chip].counter[CHAN1];				\
/*TODO*///                PROCESS_CHANNEL(chip,CHAN1);                            \
/*TODO*///			}															\
/*TODO*///		}																\
/*TODO*///		else															\
/*TODO*///		if( pokey[chip].counter[CHAN2] < pokey[chip].samplepos_whole )	\
/*TODO*///		{																\
/*TODO*///			if( pokey[chip].counter[CHAN3] < pokey[chip].counter[CHAN2] ) \
/*TODO*///			{															\
/*TODO*///				if( pokey[chip].counter[CHAN4] < pokey[chip].counter[CHAN3] ) \
/*TODO*///				{														\
/*TODO*///					UINT32 event = pokey[chip].counter[CHAN4];			\
/*TODO*///                    PROCESS_CHANNEL(chip,CHAN4);                        \
/*TODO*///				}														\
/*TODO*///				else													\
/*TODO*///				{														\
/*TODO*///					UINT32 event = pokey[chip].counter[CHAN3];			\
/*TODO*///                    PROCESS_CHANNEL(chip,CHAN3);                        \
/*TODO*///				}														\
/*TODO*///			}															\
/*TODO*///			else														\
/*TODO*///			if( pokey[chip].counter[CHAN4] < pokey[chip].counter[CHAN2] ) \
/*TODO*///			{															\
/*TODO*///				UINT32 event = pokey[chip].counter[CHAN4];				\
/*TODO*///                PROCESS_CHANNEL(chip,CHAN4);                            \
/*TODO*///			}															\
/*TODO*///			else														\
/*TODO*///			{															\
/*TODO*///				UINT32 event = pokey[chip].counter[CHAN2];				\
/*TODO*///                PROCESS_CHANNEL(chip,CHAN2);                            \
/*TODO*///			}															\
/*TODO*///		}																\
/*TODO*///		else															\
/*TODO*///		if( pokey[chip].counter[CHAN3] < pokey[chip].samplepos_whole )	\
/*TODO*///        {                                                               \
/*TODO*///			if( pokey[chip].counter[CHAN4] < pokey[chip].counter[CHAN3] ) \
/*TODO*///			{															\
/*TODO*///				UINT32 event = pokey[chip].counter[CHAN4];				\
/*TODO*///                PROCESS_CHANNEL(chip,CHAN4);                            \
/*TODO*///			}															\
/*TODO*///			else														\
/*TODO*///			{															\
/*TODO*///				UINT32 event = pokey[chip].counter[CHAN3];				\
/*TODO*///                PROCESS_CHANNEL(chip,CHAN3);                            \
/*TODO*///			}															\
/*TODO*///		}																\
/*TODO*///		else															\
/*TODO*///		if( pokey[chip].counter[CHAN4] < pokey[chip].samplepos_whole )	\
/*TODO*///		{																\
/*TODO*///			UINT32 event = pokey[chip].counter[CHAN4];					\
/*TODO*///            PROCESS_CHANNEL(chip,CHAN4);                                \
/*TODO*///        }                                                               \
/*TODO*///		else															\
/*TODO*///		{																\
/*TODO*///			UINT32 event = pokey[chip].samplepos_whole; 				\
/*TODO*///			PROCESS_SAMPLE(chip);										\
/*TODO*///		}																\
/*TODO*///	}																	\
/*TODO*///	timer_reset(pokey[chip].rtimer, TIME_NEVER)
/*TODO*///
/*TODO*///void pokey0_update(int param, INT16 *buffer, int length) { PROCESS_POKEY(0); }
/*TODO*///void pokey1_update(int param, INT16 *buffer, int length) { PROCESS_POKEY(1); }
/*TODO*///void pokey2_update(int param, INT16 *buffer, int length) { PROCESS_POKEY(2); }
/*TODO*///void pokey3_update(int param, INT16 *buffer, int length) { PROCESS_POKEY(3); }
/*TODO*///void (*update[MAXPOKEYS])(int,INT16*,int) =
/*TODO*///	{ pokey0_update,pokey1_update,pokey2_update,pokey3_update };
/*TODO*///
/*TODO*///#else   /* no HEAVY_MACRO_USAGE */
/*TODO*////*
/*TODO*/// * And this version of PROCESS_POKEY uses event and channel variables
/*TODO*/// * so that the PROCESS_CHANNEL macro needs to index memory at runtime.
/*TODO*/// */
/*TODO*///
/*TODO*///#define PROCESS_POKEY(chip)                                             \
/*TODO*///	UINT32 sum = 0; 													\
/*TODO*///	if( pokey[chip].output[CHAN1] ) 									\
/*TODO*///		sum += pokey[chip].volume[CHAN1];								\
/*TODO*///	if( pokey[chip].output[CHAN2] ) 									\
/*TODO*///		sum += pokey[chip].volume[CHAN2];								\
/*TODO*///	if( pokey[chip].output[CHAN3] ) 									\
/*TODO*///		sum += pokey[chip].volume[CHAN3];								\
/*TODO*///	if( pokey[chip].output[CHAN4] ) 									\
/*TODO*///        sum += pokey[chip].volume[CHAN4];                               \
/*TODO*///    while( length > 0 )                                                 \
/*TODO*///	{																	\
/*TODO*///		UINT32 event = pokey[chip].samplepos_whole; 					\
/*TODO*///		UINT32 channel = SAMPLE;										\
/*TODO*///		if( pokey[chip].counter[CHAN1] < event )						\
/*TODO*///        {                                                               \
/*TODO*///			event = pokey[chip].counter[CHAN1]; 						\
/*TODO*///			channel = CHAN1;											\
/*TODO*///		}																\
/*TODO*///		if( pokey[chip].counter[CHAN2] < event )						\
/*TODO*///        {                                                               \
/*TODO*///			event = pokey[chip].counter[CHAN2]; 						\
/*TODO*///			channel = CHAN2;											\
/*TODO*///        }                                                               \
/*TODO*///		if( pokey[chip].counter[CHAN3] < event )						\
/*TODO*///        {                                                               \
/*TODO*///			event = pokey[chip].counter[CHAN3]; 						\
/*TODO*///			channel = CHAN3;											\
/*TODO*///        }                                                               \
/*TODO*///		if( pokey[chip].counter[CHAN4] < event )						\
/*TODO*///        {                                                               \
/*TODO*///			event = pokey[chip].counter[CHAN4]; 						\
/*TODO*///			channel = CHAN4;											\
/*TODO*///        }                                                               \
/*TODO*///        if( channel == SAMPLE )                                         \
/*TODO*///		{																\
/*TODO*///            PROCESS_SAMPLE(chip);                                       \
/*TODO*///        }                                                               \
/*TODO*///		else															\
/*TODO*///		{																\
/*TODO*///			PROCESS_CHANNEL(chip,channel);								\
/*TODO*///		}																\
/*TODO*///	}																	\
/*TODO*///	timer_reset(pokey[chip].rtimer, TIME_NEVER)
/*TODO*///
/*TODO*///void pokey_update(int param, INT16 *buffer, int length) { PROCESS_POKEY(param); }
/*TODO*///void (*update[MAXPOKEYS])(int,INT16*,int) =
/*TODO*///	{ pokey_update,pokey_update,pokey_update,pokey_update };
/*TODO*///
    public static StreamInitPtr pokey_update = new StreamInitPtr() {
        public void handler(int chip, UShortPtr buffer, int length) {

        }
    };
    StreamInitPtr[] update = {pokey_update, pokey_update, pokey_update, pokey_update};
    /*TODO*///#endif
/*TODO*///
/*TODO*///void pokey_sh_update(void)
/*TODO*///{
/*TODO*///	int chip;
/*TODO*///
/*TODO*///	for( chip = 0; chip < intf.num; chip++ )
/*TODO*///		stream_update(pokey[chip].channel, 0);
/*TODO*///}
/*TODO*///

    static void poly_init(char[] poly, int size, int left, int right, int add) {
        int mask = (1 << size) - 1;
        int x = 0;
        int pi = 0;
        //LOG_POLY((errorlog,"poly %d\n", size));
        for (int i = 0; i < mask; i++) {
            poly[pi++] = (char) ((x & 1) & 0xFF);
            //LOG_POLY((errorlog,"%05x: %d\n", x, x&1));
                /* calculate next bit */
            x = ((x << left) + (x >>> right) + add) & mask;
        }
    }

    static void rand_init(char[] rng, int size, int left, int right, int add) {
        int mask = (1 << size) - 1;
        int x = 0;
        int ri = 0;
        //	LOG_RAND((errorlog,"rand %d\n", size));
        for (int i = 0; i < mask; i++) {
            rng[ri] = (char) ((x >>> (size - 8)) & 0xFF);   /* use the upper 8 bits */

            //LOG_RAND((errorlog, "%05x: %02x\n", x, *rng));

            ri++;
            /* calculate next bit */
            x = ((x << left) + (x >>> right) + add) & mask;
        }
    }

    @Override
    public int start(MachineSound msound) {
        int chip;
        intf = (POKEYinterface) msound.sound_interface;

        poly9 = new char[0x1ff + 1];
        rand9 = new char[0x1ff + 1];
        poly17 = new char[0x1ffff + 1];
        rand17 = new char[0x1ffff + 1];

        /* initialize the poly counters */
        poly_init(poly4, 4, 3, 1, 0x00004);
        poly_init(poly5, 5, 3, 2, 0x00008);
        poly_init(poly9, 9, 2, 7, 0x00080);
        poly_init(poly17, 17, 7, 10, 0x18000);

        /* initialize the random arrays */
        rand_init(rand9, 9, 2, 7, 0x00080);
        rand_init(rand17, 17, 7, 10, 0x18000);

        for (chip = 0; chip < intf.num; chip++) {
            _pokey[chip] = new POKEYregisters();
            String name = "";
            //memset(p, 0, sizeof(struct POKEYregisters));

            _pokey[chip].samplerate_24_8 = (Machine.sample_rate) != 0 ? (intf.baseclock << 8) / Machine.sample_rate : 1;
            _pokey[chip].divisor[CHAN1] = 4;
            _pokey[chip].divisor[CHAN2] = 4;
            _pokey[chip].divisor[CHAN3] = 4;
            _pokey[chip].divisor[CHAN4] = 4;
            _pokey[chip].clockmult = DIV_64;
            _pokey[chip].KBCODE = 0x09;		 /* Atari 800 'no key' */

            _pokey[chip].SKCTL = SK_RESET;	 /* let the RNG run after reset */

            _pokey[chip].rtimer = timer_set(TIME_NEVER, chip, null);

            _pokey[chip].pot_r[0] = intf.pot0_r[chip];
            _pokey[chip].pot_r[1] = intf.pot1_r[chip];
            _pokey[chip].pot_r[2] = intf.pot2_r[chip];
            _pokey[chip].pot_r[3] = intf.pot3_r[chip];
            _pokey[chip].pot_r[4] = intf.pot4_r[chip];
            _pokey[chip].pot_r[5] = intf.pot5_r[chip];
            _pokey[chip].pot_r[6] = intf.pot6_r[chip];
            _pokey[chip].pot_r[7] = intf.pot7_r[chip];
            _pokey[chip].allpot_r = intf.allpot_r[chip];
            if(intf.serin_r!=null)
                _pokey[chip].serin_r = intf.serin_r[chip];
            if(intf.serout_w!=null)
                _pokey[chip].serout_w = intf.serout_w[chip];
            if(intf.interrupt_cb!=null)
                _pokey[chip].interrupt_cb = intf.interrupt_cb[chip];

            sprintf(name, "Pokey #%d", chip);
            _pokey[chip].channel = stream_init(name, intf.mixing_level[chip], Machine.sample_rate, chip, update[chip]);

            if (_pokey[chip].channel == -1) {
                //perror("failed to initialize sound channel");
                return 1;
            }
        }

        return 0;
    }

    @Override
    public void stop() {

        if( rand17!=null ) 
	rand17 = null;
	if( poly17!=null ) 
	poly17 = null;
	if( rand9!=null )  
	rand9 = null;
	if( poly9!=null )  
	poly9 = null;
    }
/*TODO*///static void pokey_timer_expire(int param)
/*TODO*///{
/*TODO*///	int chip = param >> 3;
/*TODO*///	int timers = param & 7;
/*TODO*///	struct POKEYregisters *p = &pokey[chip];
/*TODO*///
/*TODO*///	LOG_TIMER((errorlog, "POKEY #%d timer %d with IRQEN $%02x\n", chip, param, p->IRQEN));
/*TODO*///
/*TODO*///    /* check if some of the requested timer interrupts are enabled */
/*TODO*///	timers &= p->IRQEN;
/*TODO*///
/*TODO*///    if( timers )
/*TODO*///    {
/*TODO*///		/* set the enabled timer irq status bits */
/*TODO*///		p->IRQST |= timers;
/*TODO*///        /* call back an application supplied function to handle the interrupt */
/*TODO*///		if( p->interrupt_cb )
/*TODO*///			(*p->interrupt_cb)(timers);
/*TODO*///    }
/*TODO*///}
/*TODO*///
/*TODO*///#if VERBOSE_SOUND
/*TODO*///static char *audc2str(int val)
/*TODO*///{
/*TODO*///	static char buff[80];
/*TODO*///	if( val & NOTPOLY5 )
/*TODO*///	{
/*TODO*///		if( val & PURE )
/*TODO*///			strcpy(buff,"pure");
/*TODO*///		else
/*TODO*///		if( val & POLY4 )
/*TODO*///			strcpy(buff,"poly4");
/*TODO*///		else
/*TODO*///			strcpy(buff,"poly9/17");
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		if( val & PURE )
/*TODO*///			strcpy(buff,"poly5");
/*TODO*///		else
/*TODO*///		if( val & POLY4 )
/*TODO*///			strcpy(buff,"poly4+poly5");
/*TODO*///		else
/*TODO*///			strcpy(buff,"poly9/17+poly5");
/*TODO*///    }
/*TODO*///	return buff;
/*TODO*///}
/*TODO*///
/*TODO*///static char *audctl2str(int val)
/*TODO*///{
/*TODO*///	static char buff[80];
/*TODO*///	if( val & POLY9 )
/*TODO*///		strcpy(buff,"poly9");
/*TODO*///	else
/*TODO*///		strcpy(buff,"poly17");
/*TODO*///	if( val & CH1_HICLK )
/*TODO*///		strcat(buff,"+ch1hi");
/*TODO*///	if( val & CH3_HICLK )
/*TODO*///		strcat(buff,"+ch3hi");
/*TODO*///	if( val & CH12_JOINED )
/*TODO*///		strcat(buff,"+ch1/2");
/*TODO*///	if( val & CH34_JOINED )
/*TODO*///		strcat(buff,"+ch3/4");
/*TODO*///	if( val & CH1_FILTER )
/*TODO*///		strcat(buff,"+ch1filter");
/*TODO*///	if( val & CH2_FILTER )
/*TODO*///		strcat(buff,"+ch2filter");
/*TODO*///	if( val & CLK_15KHZ )
/*TODO*///		strcat(buff,"+clk15");
/*TODO*///    return buff;
/*TODO*///}
/*TODO*///#endif
/*TODO*///
/*TODO*///static void pokey_serin_ready(int chip)
/*TODO*///{
/*TODO*///	struct POKEYregisters *p = &pokey[chip];
/*TODO*///    if( p->IRQEN & IRQ_SERIN )
/*TODO*///	{
/*TODO*///		/* set the enabled timer irq status bits */
/*TODO*///		p->IRQST |= IRQ_SERIN;
/*TODO*///		/* call back an application supplied function to handle the interrupt */
/*TODO*///		if( p->interrupt_cb )
/*TODO*///			(*p->interrupt_cb)(IRQ_SERIN);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static void pokey_serout_ready(int chip)
/*TODO*///{
/*TODO*///	struct POKEYregisters *p = &pokey[chip];
/*TODO*///    if( p->IRQEN & IRQ_SEROR )
/*TODO*///	{
/*TODO*///		p->IRQST |= IRQ_SEROR;
/*TODO*///		if( p->interrupt_cb )
/*TODO*///			(*p->interrupt_cb)(IRQ_SEROR);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static void pokey_serout_complete(int chip)
/*TODO*///{
/*TODO*///	struct POKEYregisters *p = &pokey[chip];
/*TODO*///    if( p->IRQEN & IRQ_SEROC )
/*TODO*///	{
/*TODO*///		p->IRQST |= IRQ_SEROC;
/*TODO*///		if( p->interrupt_cb )
/*TODO*///			(*p->interrupt_cb)(IRQ_SEROC);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///static void pokey_pot_trigger(int param)
/*TODO*///{
/*TODO*///	int chip = param >> 3;
/*TODO*///    int pot = param & 7;
/*TODO*///	struct POKEYregisters *p = &pokey[chip];
/*TODO*///
/*TODO*///	LOG((errorlog, "POKEY #%d POT%d triggers after %dus\n", chip, pot, (int)(1000000ul*timer_timeelapsed(p->ptimer[pot]))));
/*TODO*///	p->ptimer[pot] = NULL;
/*TODO*///	p->ALLPOT &= ~(1 << pot);	/* set the enabled timer irq status bits */
/*TODO*///}
/*TODO*///
/*TODO*////* A/D conversion time:
/*TODO*/// * In normal, slow mode (SKCTL bit SK_PADDLE is clear) the conversion
/*TODO*/// * takes N scanlines, where N is the paddle value. A single scanline
/*TODO*/// * takes approximately 64us to finish (1.78979MHz clock).
/*TODO*/// * In quick mode (SK_PADDLE set) the conversion is done very fast
/*TODO*/// * (takes two scalines) but the result is not as accurate.
/*TODO*/// */
/*TODO*///#define AD_TIME (double)(((p->SKCTL & SK_PADDLE) ? 64.0*2/228 : 64.0) * FREQ_17_EXACT / intf.baseclock)
/*TODO*///
/*TODO*///static void pokey_potgo(int chip)
/*TODO*///{
/*TODO*///	struct POKEYregisters *p = &pokey[chip];
/*TODO*///    int pot;
/*TODO*///
/*TODO*///	LOG((errorlog, "POKEY #%d pokey_potgo\n", chip));
/*TODO*///
/*TODO*///    p->ALLPOT = 0xff;
/*TODO*///
/*TODO*///    for( pot = 0; pot < 8; pot++ )
/*TODO*///	{
/*TODO*///        if( p->ptimer[pot] )
/*TODO*///		{
/*TODO*///			timer_remove(p->ptimer[pot]);
/*TODO*///			p->ptimer[pot] = NULL;
/*TODO*///			p->POTx[pot] = 0xff;
/*TODO*///		}
/*TODO*///		if( p->pot_r[pot] )
/*TODO*///		{
/*TODO*///			int r = (*p->pot_r[pot])(pot);
/*TODO*///			LOG((errorlog, "POKEY #%d pot_r(%d) returned $%02x\n", chip, pot, r));
/*TODO*///			if( r != -1 )
/*TODO*///			{
/*TODO*///				if (r > 228)
/*TODO*///                    r = 228;
/*TODO*///                /* final value */
/*TODO*///                p->POTx[pot] = r;
/*TODO*///				p->ptimer[pot] = timer_set(TIME_IN_USEC(r * AD_TIME), (chip<<3)|pot, pokey_pot_trigger);
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///int pokey_register_r(int chip, int offs)
/*TODO*///{
/*TODO*///	struct POKEYregisters *p = &pokey[chip];
/*TODO*///    int data = 0, pot;
/*TODO*///
/*TODO*///#ifdef MAME_DEBUG
/*TODO*///	if( chip >= intf.num )
/*TODO*///	{
/*TODO*///		if( errorlog ) fprintf(errorlog, "POKEY #%d is >= number of Pokeys!\n", chip);
/*TODO*///		return data;
/*TODO*///	}
/*TODO*///#endif
/*TODO*///
/*TODO*///    switch (offs & 15)
/*TODO*///	{
/*TODO*///	case POT0_C: case POT1_C: case POT2_C: case POT3_C:
/*TODO*///	case POT4_C: case POT5_C: case POT6_C: case POT7_C:
/*TODO*///		pot = offs & 7;
/*TODO*///		if( p->pot_r[pot] )
/*TODO*///		{
/*TODO*///			/*
/*TODO*///			 * If the conversion is not yet finished (ptimer running),
/*TODO*///			 * get the current value by the linear interpolation of
/*TODO*///			 * the final value using the elapsed time.
/*TODO*///			 */
/*TODO*///			if( p->ALLPOT & (1 << pot) )
/*TODO*///			{
/*TODO*///				data = (UINT8)(timer_timeelapsed(p->ptimer[pot]) / AD_TIME);
/*TODO*///				LOG((errorlog,"POKEY #%d read POT%d (interpolated) $%02x\n", chip, pot, data));
/*TODO*///            }
/*TODO*///			else
/*TODO*///			{
/*TODO*///				data = p->POTx[pot];
/*TODO*///				LOG((errorlog,"POKEY #%d read POT%d (final value)  $%02x\n", chip, pot, data));
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else
/*TODO*///		if (errorlog)
/*TODO*///			fprintf(errorlog,"PC %04x: warning - read p[chip] #%d POT%d\n", cpu_get_pc(), chip, pot);
/*TODO*///		break;
/*TODO*///
/*TODO*///    case ALLPOT_C:
/*TODO*///		if( p->allpot_r )
/*TODO*///		{
/*TODO*///			data = (*p->allpot_r)(offs);
/*TODO*///			LOG((errorlog,"POKEY #%d ALLPOT callback $%02x\n", chip, data));
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			data = p->ALLPOT;
/*TODO*///			LOG((errorlog,"POKEY #%d ALLPOT internal $%02x\n", chip, data));
/*TODO*///		}
/*TODO*///		break;
/*TODO*///
/*TODO*///	case KBCODE_C:
/*TODO*///		data = p->KBCODE;
/*TODO*///		break;
/*TODO*///
/*TODO*///	case RANDOM_C:
/*TODO*///		/****************************************************************
/*TODO*///		 * If the 2 least significant bits of SKCTL are 0, the random
/*TODO*///		 * number generator is disabled (SKRESET). Thanks to Eric Smith
/*TODO*///		 * for pointing out this critical bit of info! If the random
/*TODO*///		 * number generator is enabled, get a new random number. Take
/*TODO*///		 * the time gone since the last read into account and read the
/*TODO*///		 * new value from an appropriate offset in the rand17 table.
/*TODO*///		 ****************************************************************/
/*TODO*///		if( p->SKCTL & SK_RESET )
/*TODO*///		{
/*TODO*///			UINT32 adjust = (UINT32)(timer_timeelapsed(p->rtimer) * intf.baseclock);
/*TODO*///			p->r9 = (p->r9 + adjust) % 0x001ff;
/*TODO*///			p->r17 = (p->r17 + adjust) % 0x1ffff;
/*TODO*///			if( p->AUDCTL & POLY9 )
/*TODO*///			{
/*TODO*///				p->RANDOM = rand9[p->r9];
/*TODO*///				LOG_RAND((errorlog, "POKEY #%d adjust %u rand9[$%05x]: $%02x\n", chip, adjust, p->r9, p->RANDOM));
/*TODO*///			}
/*TODO*///            else
/*TODO*///			{
/*TODO*///				p->RANDOM = rand17[p->r17];
/*TODO*///				LOG_RAND((errorlog, "POKEY #%d adjust %u rand17[$%05x]: $%02x\n", chip, adjust, p->r17, p->RANDOM));
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			LOG_RAND((errorlog, "POKEY #%d rand17 freezed (SKCTL): $%02x\n", chip, p->RANDOM));
/*TODO*///		}
/*TODO*///		timer_reset(p->rtimer, TIME_NEVER);
/*TODO*///		data = p->RANDOM;
/*TODO*///		break;
/*TODO*///
/*TODO*///	case SERIN_C:
/*TODO*///		if( p->serin_r )
/*TODO*///			p->SERIN = (*p->serin_r)(offs);
/*TODO*///		data = p->SERIN;
/*TODO*///		LOG((errorlog, "POKEY #%d SERIN  $%02x\n", chip, data));
/*TODO*///		break;
/*TODO*///
/*TODO*///	case IRQST_C:
/*TODO*///		/* IRQST is an active low input port; we keep it active high */
/*TODO*///		/* internally to ease the (un-)masking of bits */
/*TODO*///		data = p->IRQST ^ 0xff;
/*TODO*///		LOG((errorlog, "POKEY #%d IRQST  $%02x\n", chip, data));
/*TODO*///		break;
/*TODO*///
/*TODO*///	case SKSTAT_C:
/*TODO*///		/* SKSTAT is also an active low input port */
/*TODO*///		data = p->SKSTAT ^ 0xff;
/*TODO*///		LOG((errorlog, "POKEY #%d SKSTAT $%02x\n", chip, data));
/*TODO*///		break;
/*TODO*///
/*TODO*///	default:
/*TODO*///		LOG((errorlog, "POKEY #%d register $%02x\n", chip, offs));
/*TODO*///        break;
/*TODO*///    }
/*TODO*///    return data;
/*TODO*///}
/*TODO*///
    public static ReadHandlerPtr pokey1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            throw new UnsupportedOperationException("Not supported yet.");
            //return pokey_register_r(0, offset);
        }
    };
    public static ReadHandlerPtr pokey2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            throw new UnsupportedOperationException("Not supported yet.");
            //return pokey_register_r(1, offset);
        }
    };
    /*TODO*///int pokey3_r (int offset)
/*TODO*///{
/*TODO*///	return pokey_register_r(2, offset);
/*TODO*///}
/*TODO*///
/*TODO*///int pokey4_r (int offset)
/*TODO*///{
/*TODO*///	return pokey_register_r(3, offset);
/*TODO*///}
/*TODO*///
/*TODO*///int quad_pokey_r (int offset)
/*TODO*///{
/*TODO*///	int pokey_num = (offset >> 3) & ~0x04;
/*TODO*///	int control = (offset & 0x20) >> 2;
/*TODO*///	int pokey_reg = (offset % 8) | control;
/*TODO*///
/*TODO*///	return pokey_register_r(pokey_num, pokey_reg);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void pokey_register_w(int chip, int offs, int data)
/*TODO*///{
/*TODO*///	struct POKEYregisters *p = &pokey[chip];
/*TODO*///	int ch_mask = 0, new_val;
/*TODO*///
/*TODO*///#ifdef MAME_DEBUG
/*TODO*///	if( chip >= intf.num )
/*TODO*///	{
/*TODO*///		if( errorlog ) fprintf(errorlog, "POKEY #%d is >= number of Pokeys!\n", chip);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///#endif
/*TODO*///	stream_update(p->channel, 0);
/*TODO*///
/*TODO*///    /* determine which address was changed */
/*TODO*///	switch (offs & 15)
/*TODO*///    {
/*TODO*///    case AUDF1_C:
/*TODO*///		if( data == p->AUDF[CHAN1] )
/*TODO*///            return;
/*TODO*///		LOG_SOUND((errorlog, "POKEY #%d AUDF1  $%02x\n", chip, data));
/*TODO*///		p->AUDF[CHAN1] = data;
/*TODO*///        ch_mask = 1 << CHAN1;
/*TODO*///		if( p->AUDCTL & CH12_JOINED )		/* if ch 1&2 tied together */
/*TODO*///            ch_mask |= 1 << CHAN2;    /* then also change on ch2 */
/*TODO*///        break;
/*TODO*///
/*TODO*///    case AUDC1_C:
/*TODO*///		if( data == p->AUDC[CHAN1] )
/*TODO*///            return;
/*TODO*///		LOG_SOUND((errorlog, "POKEY #%d AUDC1  $%02x (%s)\n", chip, data, audc2str(data)));
/*TODO*///		p->AUDC[CHAN1] = data;
/*TODO*///        ch_mask = 1 << CHAN1;
/*TODO*///        break;
/*TODO*///
/*TODO*///    case AUDF2_C:
/*TODO*///		if( data == p->AUDF[CHAN2] )
/*TODO*///            return;
/*TODO*///		LOG_SOUND((errorlog, "POKEY #%d AUDF2  $%02x\n", chip, data));
/*TODO*///		p->AUDF[CHAN2] = data;
/*TODO*///        ch_mask = 1 << CHAN2;
/*TODO*///        break;
/*TODO*///
/*TODO*///    case AUDC2_C:
/*TODO*///		if( data == p->AUDC[CHAN2] )
/*TODO*///            return;
/*TODO*///		LOG_SOUND((errorlog, "POKEY #%d AUDC2  $%02x (%s)\n", chip, data, audc2str(data)));
/*TODO*///		p->AUDC[CHAN2] = data;
/*TODO*///        ch_mask = 1 << CHAN2;
/*TODO*///        break;
/*TODO*///
/*TODO*///    case AUDF3_C:
/*TODO*///		if( data == p->AUDF[CHAN3] )
/*TODO*///            return;
/*TODO*///		LOG_SOUND((errorlog, "POKEY #%d AUDF3  $%02x\n", chip, data));
/*TODO*///		p->AUDF[CHAN3] = data;
/*TODO*///        ch_mask = 1 << CHAN3;
/*TODO*///
/*TODO*///		if( p->AUDCTL & CH34_JOINED )	/* if ch 3&4 tied together */
/*TODO*///            ch_mask |= 1 << CHAN4;  /* then also change on ch4 */
/*TODO*///        break;
/*TODO*///
/*TODO*///    case AUDC3_C:
/*TODO*///		if( data == p->AUDC[CHAN3] )
/*TODO*///            return;
/*TODO*///		LOG_SOUND((errorlog, "POKEY #%d AUDC3  $%02x (%s)\n", chip, data, audc2str(data)));
/*TODO*///		p->AUDC[CHAN3] = data;
/*TODO*///        ch_mask = 1 << CHAN3;
/*TODO*///        break;
/*TODO*///
/*TODO*///    case AUDF4_C:
/*TODO*///		if( data == p->AUDF[CHAN4] )
/*TODO*///            return;
/*TODO*///		LOG_SOUND((errorlog, "POKEY #%d AUDF4  $%02x\n", chip, data));
/*TODO*///		p->AUDF[CHAN4] = data;
/*TODO*///        ch_mask = 1 << CHAN4;
/*TODO*///        break;
/*TODO*///
/*TODO*///    case AUDC4_C:
/*TODO*///		if( data == p->AUDC[CHAN4] )
/*TODO*///            return;
/*TODO*///		LOG_SOUND((errorlog, "POKEY #%d AUDC4  $%02x (%s)\n", chip, data, audc2str(data)));
/*TODO*///		p->AUDC[CHAN4] = data;
/*TODO*///        ch_mask = 1 << CHAN4;
/*TODO*///        break;
/*TODO*///
/*TODO*///    case AUDCTL_C:
/*TODO*///		if( data == p->AUDCTL )
/*TODO*///            return;
/*TODO*///		LOG_SOUND((errorlog, "POKEY #%d AUDCTL $%02x (%s)\n", chip, data, audctl2str(data)));
/*TODO*///		p->AUDCTL = data;
/*TODO*///        ch_mask = 15;       /* all channels */
/*TODO*///        /* determine the base multiplier for the 'div by n' calculations */
/*TODO*///		p->clockmult = (p->AUDCTL & CLK_15KHZ) ? DIV_15 : DIV_64;
/*TODO*///        break;
/*TODO*///
/*TODO*///    case STIMER_C:
/*TODO*///        /* first remove any existing timers */
/*TODO*///		LOG_TIMER((errorlog, "POKEY #%d STIMER $%02x\n", chip, data));
/*TODO*///		if( p->timer[TIMER1] )
/*TODO*///			timer_remove(p->timer[TIMER1]);
/*TODO*///		if( p->timer[TIMER2] )
/*TODO*///			timer_remove(p->timer[TIMER2]);
/*TODO*///		if( p->timer[TIMER4] )
/*TODO*///			timer_remove(p->timer[TIMER4]);
/*TODO*///		p->timer[TIMER1] = NULL;
/*TODO*///		p->timer[TIMER2] = NULL;
/*TODO*///		p->timer[TIMER4] = NULL;
/*TODO*///
/*TODO*///        /* reset all counters to zero (side effect) */
/*TODO*///		p->polyadjust = 0;
/*TODO*///		p->counter[CHAN1] = 0;
/*TODO*///		p->counter[CHAN2] = 0;
/*TODO*///		p->counter[CHAN3] = 0;
/*TODO*///		p->counter[CHAN4] = 0;
/*TODO*///
/*TODO*///        /* joined chan#1 and chan#2 ? */
/*TODO*///		if( p->AUDCTL & CH12_JOINED )
/*TODO*///        {
/*TODO*///			if( p->divisor[CHAN2] > 4 )
/*TODO*///			{
/*TODO*///				LOG_TIMER((errorlog, "POKEY #%d timer1+2 after %d clocks\n", chip, p->divisor[CHAN2]));
/*TODO*///				/* set timer #1 _and_ #2 event after timer_div clocks of joined CHAN1+CHAN2 */
/*TODO*///				p->timer[TIMER2] =
/*TODO*///					timer_pulse(1.0 * p->divisor[CHAN2] / intf.baseclock,
/*TODO*///						(chip<<3)|IRQ_TIMR2|IRQ_TIMR1, pokey_timer_expire);
/*TODO*///			}
/*TODO*///        }
/*TODO*///        else
/*TODO*///        {
/*TODO*///			if( p->divisor[CHAN1] > 4 )
/*TODO*///			{
/*TODO*///				LOG_TIMER((errorlog, "POKEY #%d timer1 after %d clocks\n", chip, p->divisor[CHAN1]));
/*TODO*///				/* set timer #1 event after timer_div clocks of CHAN1 */
/*TODO*///				p->timer[TIMER1] =
/*TODO*///					timer_pulse(1.0 * p->divisor[CHAN1] / intf.baseclock,
/*TODO*///						(chip<<3)|IRQ_TIMR1, pokey_timer_expire);
/*TODO*///			}
/*TODO*///
/*TODO*///			if( p->divisor[CHAN2] > 4 )
/*TODO*///			{
/*TODO*///				LOG_TIMER((errorlog, "POKEY #%d timer2 after %d clocks\n", chip, p->divisor[CHAN2]));
/*TODO*///				/* set timer #2 event after timer_div clocks of CHAN2 */
/*TODO*///				p->timer[TIMER2] =
/*TODO*///					timer_pulse(1.0 * p->divisor[CHAN2] / intf.baseclock,
/*TODO*///						(chip<<3)|IRQ_TIMR2, pokey_timer_expire);
/*TODO*///			}
/*TODO*///        }
/*TODO*///
/*TODO*///		/* Note: p[chip] does not have a timer #3 */
/*TODO*///
/*TODO*///		if( p->AUDCTL & CH34_JOINED )
/*TODO*///        {
/*TODO*///            /* not sure about this: if audc4 == 0000xxxx don't start timer 4 ? */
/*TODO*///			if( p->AUDC[CHAN4] & 0xf0 )
/*TODO*///            {
/*TODO*///				if( p->divisor[CHAN4] > 4 )
/*TODO*///				{
/*TODO*///					LOG_TIMER((errorlog, "POKEY #%d timer4 after %d clocks\n", chip, p->divisor[CHAN4]));
/*TODO*///					/* set timer #4 event after timer_div clocks of CHAN4 */
/*TODO*///					p->timer[TIMER4] =
/*TODO*///						timer_pulse(1.0 * p->divisor[CHAN4] / intf.baseclock,
/*TODO*///							(chip<<3)|IRQ_TIMR4, pokey_timer_expire);
/*TODO*///				}
/*TODO*///            }
/*TODO*///        }
/*TODO*///        else
/*TODO*///        {
/*TODO*///			if( p->divisor[CHAN4] > 4 )
/*TODO*///			{
/*TODO*///				LOG_TIMER((errorlog, "POKEY #%d timer4 after %d clocks\n", chip, p->divisor[CHAN4]));
/*TODO*///				/* set timer #4 event after timer_div clocks of CHAN4 */
/*TODO*///				p->timer[TIMER4] =
/*TODO*///					timer_pulse(1.0 * p->divisor[CHAN4] / intf.baseclock,
/*TODO*///						(chip<<3)|IRQ_TIMR4, pokey_timer_expire);
/*TODO*///			}
/*TODO*///        }
/*TODO*///		if( p->timer[TIMER1] )
/*TODO*///			timer_enable(p->timer[TIMER1], p->IRQEN & IRQ_TIMR1);
/*TODO*///		if( p->timer[TIMER2] )
/*TODO*///			timer_enable(p->timer[TIMER2], p->IRQEN & IRQ_TIMR2);
/*TODO*///		if( p->timer[TIMER4] )
/*TODO*///			timer_enable(p->timer[TIMER4], p->IRQEN & IRQ_TIMR4);
/*TODO*///        break;
/*TODO*///
/*TODO*///    case SKREST_C:
/*TODO*///        /* reset SKSTAT */
/*TODO*///		LOG((errorlog, "POKEY #%d SKREST $%02x\n", chip, data));
/*TODO*///		p->SKSTAT &= ~(SK_FRAME|SK_OVERRUN|SK_KBERR);
/*TODO*///        break;
/*TODO*///
/*TODO*///    case POTGO_C:
/*TODO*///		LOG((errorlog, "POKEY #%d POTGO  $%02x\n", chip, data));
/*TODO*///		pokey_potgo(chip);
/*TODO*///        break;
/*TODO*///
/*TODO*///    case SEROUT_C:
/*TODO*///		LOG((errorlog, "POKEY #%d SEROUT $%02x\n", chip, data));
/*TODO*///		if (p->serout_w)
/*TODO*///			(*p->serout_w)(offs, data);
/*TODO*///		p->SKSTAT |= SK_SEROUT;
/*TODO*///        /*
/*TODO*///         * These are arbitrary values, tested with some custom boot
/*TODO*///         * loaders from Ballblazer and Escape from Fractalus
/*TODO*///         * The real times are unknown
/*TODO*///         */
/*TODO*///        timer_set(TIME_IN_USEC(200), chip, pokey_serout_ready);
/*TODO*///        /* 10 bits (assumption 1 start, 8 data and 1 stop bit) take how long? */
/*TODO*///        timer_set(TIME_IN_USEC(2000), chip, pokey_serout_complete);
/*TODO*///        break;
/*TODO*///
/*TODO*///    case IRQEN_C:
/*TODO*///		LOG((errorlog, "POKEY #%d IRQEN  $%02x\n", chip, data));
/*TODO*///
/*TODO*///        /* acknowledge one or more IRQST bits ? */
/*TODO*///		if( p->IRQST & ~data )
/*TODO*///        {
/*TODO*///            /* reset IRQST bits that are masked now */
/*TODO*///			p->IRQST &= data;
/*TODO*///        }
/*TODO*///        else
/*TODO*///        {
/*TODO*///			/* enable/disable timers now to avoid unneeded
/*TODO*///               breaking of the CPU cores for masked timers */
/*TODO*///			if( p->timer[TIMER1] && ((p->IRQEN^data) & IRQ_TIMR1) )
/*TODO*///				timer_enable(p->timer[TIMER1], data & IRQ_TIMR1);
/*TODO*///			if( p->timer[TIMER2] && ((p->IRQEN^data) & IRQ_TIMR2) )
/*TODO*///				timer_enable(p->timer[TIMER2], data & IRQ_TIMR2);
/*TODO*///			if( p->timer[TIMER4] && ((p->IRQEN^data) & IRQ_TIMR4) )
/*TODO*///				timer_enable(p->timer[TIMER4], data & IRQ_TIMR4);
/*TODO*///        }
/*TODO*///		/* store irq enable */
/*TODO*///		p->IRQEN = data;
/*TODO*///        break;
/*TODO*///
/*TODO*///    case SKCTL_C:
/*TODO*///		if( data == p->SKCTL )
/*TODO*///            return;
/*TODO*///		LOG((errorlog, "POKEY #%d SKCTL  $%02x\n", chip, data));
/*TODO*///		p->SKCTL = data;
/*TODO*///        if( !(data & SK_RESET) )
/*TODO*///        {
/*TODO*///            pokey_register_w(chip, IRQEN_C,  0);
/*TODO*///            pokey_register_w(chip, SKREST_C, 0);
/*TODO*///        }
/*TODO*///        break;
/*TODO*///    }
/*TODO*///
/*TODO*///	/************************************************************
/*TODO*///	 * As defined in the manual, the exact counter values are
/*TODO*///	 * different depending on the frequency and resolution:
/*TODO*///	 *	  64 kHz or 15 kHz - AUDF + 1
/*TODO*///	 *	  1.79 MHz, 8-bit  - AUDF + 4
/*TODO*///	 *	  1.79 MHz, 16-bit - AUDF[CHAN1]+256*AUDF[CHAN2] + 7
/*TODO*///	 ************************************************************/
/*TODO*///
/*TODO*///    /* only reset the channels that have changed */
/*TODO*///
/*TODO*///    if( ch_mask & (1 << CHAN1) )
/*TODO*///    {
/*TODO*///        /* process channel 1 frequency */
/*TODO*///		if( p->AUDCTL & CH1_HICLK )
/*TODO*///			new_val = p->AUDF[CHAN1] + DIVADD_HICLK;
/*TODO*///        else
/*TODO*///			new_val = (p->AUDF[CHAN1] + DIVADD_LOCLK) * p->clockmult;
/*TODO*///
/*TODO*///		LOG_SOUND((errorlog, "POKEY #%d chan1 %d\n", chip, new_val));
/*TODO*///
/*TODO*///		p->volume[CHAN1] = (p->AUDC[CHAN1] & VOLUME_MASK) * POKEY_DEFAULT_GAIN;
/*TODO*///        p->divisor[CHAN1] = new_val;
/*TODO*///		if( new_val < p->counter[CHAN1] )
/*TODO*///			p->counter[CHAN1] = new_val;
/*TODO*///		if( p->interrupt_cb && p->timer[TIMER1] )
/*TODO*///			timer_reset(p->timer[TIMER1], 1.0 * new_val / intf.baseclock);
/*TODO*///		p->audible[CHAN1] = !(
/*TODO*///			(p->AUDC[CHAN1] & VOLUME_ONLY) ||
/*TODO*///			(p->AUDC[CHAN1] & VOLUME_MASK) == 0 ||
/*TODO*///			((p->AUDC[CHAN1] & PURE) && new_val < (p->samplerate_24_8 >> 8)));
/*TODO*///		if( !p->audible[CHAN1] )
/*TODO*///		{
/*TODO*///			p->output[CHAN1] = 1;
/*TODO*///			p->counter[CHAN1] = 0x7fffffff;
/*TODO*///			/* 50% duty cycle should result in half volume */
/*TODO*///            p->volume[CHAN1] >>= 1;
/*TODO*///        }
/*TODO*///    }
/*TODO*///
/*TODO*///    if( ch_mask & (1 << CHAN2) )
/*TODO*///    {
/*TODO*///        /* process channel 2 frequency */
/*TODO*///		if( p->AUDCTL & CH12_JOINED )
/*TODO*///        {
/*TODO*///			if( p->AUDCTL & CH1_HICLK )
/*TODO*///				new_val = p->AUDF[CHAN2] * 256 + p->AUDF[CHAN1] + DIVADD_HICLK_JOINED;
/*TODO*///            else
/*TODO*///				new_val = (p->AUDF[CHAN2] * 256 + p->AUDF[CHAN1] + DIVADD_LOCLK) * p->clockmult;
/*TODO*///			LOG_SOUND((errorlog, "POKEY #%d chan1+2 %d\n", chip, new_val));
/*TODO*///        }
/*TODO*///        else
/*TODO*///		{
/*TODO*///			new_val = (p->AUDF[CHAN2] + DIVADD_LOCLK) * p->clockmult;
/*TODO*///			LOG_SOUND((errorlog, "POKEY #%d chan2 %d\n", chip, new_val));
/*TODO*///		}
/*TODO*///
/*TODO*///		p->volume[CHAN2] = (p->AUDC[CHAN2] & VOLUME_MASK) * POKEY_DEFAULT_GAIN;
/*TODO*///		p->divisor[CHAN2] = new_val;
/*TODO*///		if( new_val < p->counter[CHAN2] )
/*TODO*///			p->counter[CHAN2] = new_val;
/*TODO*///		if( p->interrupt_cb && p->timer[TIMER2] )
/*TODO*///			timer_reset(p->timer[TIMER2], 1.0 * new_val / intf.baseclock);
/*TODO*///		p->audible[CHAN2] = !(
/*TODO*///			(p->AUDC[CHAN2] & VOLUME_ONLY) ||
/*TODO*///			(p->AUDC[CHAN2] & VOLUME_MASK) == 0 ||
/*TODO*///			((p->AUDC[CHAN2] & PURE) && new_val < (p->samplerate_24_8 >> 8)));
/*TODO*///		if( !p->audible[CHAN2] )
/*TODO*///		{
/*TODO*///			p->output[CHAN2] = 1;
/*TODO*///			p->counter[CHAN2] = 0x7fffffff;
/*TODO*///			/* 50% duty cycle should result in half volume */
/*TODO*///			p->volume[CHAN2] >>= 1;
/*TODO*///        }
/*TODO*///    }
/*TODO*///
/*TODO*///    if( ch_mask & (1 << CHAN3) )
/*TODO*///    {
/*TODO*///        /* process channel 3 frequency */
/*TODO*///		if( p->AUDCTL & CH3_HICLK )
/*TODO*///			new_val = p->AUDF[CHAN3] + DIVADD_HICLK;
/*TODO*///        else
/*TODO*///			new_val = (p->AUDF[CHAN3] + DIVADD_LOCLK) * p->clockmult;
/*TODO*///
/*TODO*///		LOG_SOUND((errorlog, "POKEY #%d chan3 %d\n", chip, new_val));
/*TODO*///
/*TODO*///		p->volume[CHAN3] = (p->AUDC[CHAN3] & VOLUME_MASK) * POKEY_DEFAULT_GAIN;
/*TODO*///		p->divisor[CHAN3] = new_val;
/*TODO*///		if( new_val < p->counter[CHAN3] )
/*TODO*///			p->counter[CHAN3] = new_val;
/*TODO*///		/* channel 3 does not have a timer associated */
/*TODO*///		p->audible[CHAN3] = !(
/*TODO*///			(p->AUDC[CHAN3] & VOLUME_ONLY) ||
/*TODO*///			(p->AUDC[CHAN3] & VOLUME_MASK) == 0 ||
/*TODO*///			((p->AUDC[CHAN3] & PURE) && new_val < (p->samplerate_24_8 >> 8))) ||
/*TODO*///			(p->AUDCTL & CH1_FILTER);
/*TODO*///		if( !p->audible[CHAN3] )
/*TODO*///		{
/*TODO*///			p->output[CHAN3] = 1;
/*TODO*///			p->counter[CHAN3] = 0x7fffffff;
/*TODO*///			/* 50% duty cycle should result in half volume */
/*TODO*///			p->volume[CHAN3] >>= 1;
/*TODO*///        }
/*TODO*///    }
/*TODO*///
/*TODO*///    if( ch_mask & (1 << CHAN4) )
/*TODO*///    {
/*TODO*///        /* process channel 4 frequency */
/*TODO*///		if( p->AUDCTL & CH34_JOINED )
/*TODO*///        {
/*TODO*///			if( p->AUDCTL & CH3_HICLK )
/*TODO*///				new_val = p->AUDF[CHAN4] * 256 + p->AUDF[CHAN3] + DIVADD_HICLK_JOINED;
/*TODO*///            else
/*TODO*///				new_val = (p->AUDF[CHAN4] * 256 + p->AUDF[CHAN3] + DIVADD_LOCLK) * p->clockmult;
/*TODO*///			LOG_SOUND((errorlog, "POKEY #%d chan3+4 %d\n", chip, new_val));
/*TODO*///        }
/*TODO*///        else
/*TODO*///		{
/*TODO*///			new_val = (p->AUDF[CHAN4] + DIVADD_LOCLK) * p->clockmult;
/*TODO*///			LOG_SOUND((errorlog, "POKEY #%d chan4 %d\n", chip, new_val));
/*TODO*///		}
/*TODO*///
/*TODO*///		p->volume[CHAN4] = (p->AUDC[CHAN4] & VOLUME_MASK) * POKEY_DEFAULT_GAIN;
/*TODO*///		p->divisor[CHAN4] = new_val;
/*TODO*///		if( new_val < p->counter[CHAN4] )
/*TODO*///			p->counter[CHAN4] = new_val;
/*TODO*///		if( p->interrupt_cb && p->timer[TIMER4] )
/*TODO*///			timer_reset(p->timer[TIMER4], 1.0 * new_val / intf.baseclock);
/*TODO*///		p->audible[CHAN4] = !(
/*TODO*///			(p->AUDC[CHAN4] & VOLUME_ONLY) ||
/*TODO*///			(p->AUDC[CHAN4] & VOLUME_MASK) == 0 ||
/*TODO*///			((p->AUDC[CHAN4] & PURE) && new_val < (p->samplerate_24_8 >> 8))) ||
/*TODO*///			(p->AUDCTL & CH2_FILTER);
/*TODO*///		if( !p->audible[CHAN4] )
/*TODO*///		{
/*TODO*///			p->output[CHAN4] = 1;
/*TODO*///			p->counter[CHAN4] = 0x7fffffff;
/*TODO*///			/* 50% duty cycle should result in half volume */
/*TODO*///			p->volume[CHAN4] >>= 1;
/*TODO*///        }
/*TODO*///    }
/*TODO*///}
/*TODO*///
    public static WriteHandlerPtr pokey1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            //pokey_register_w(0, offset, data);
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };
    public static WriteHandlerPtr pokey2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            //pokey_register_w(1, offset, data);
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };

    /*TODO*///void pokey3_w (int offset,int data)
/*TODO*///{
/*TODO*///    pokey_register_w(2,offset,data);
/*TODO*///}
/*TODO*///
/*TODO*///void pokey4_w (int offset,int data)
/*TODO*///{
/*TODO*///    pokey_register_w(3,offset,data);
/*TODO*///}
/*TODO*///
/*TODO*///void quad_pokey_w (int offset,int data)
/*TODO*///{
/*TODO*///    int pokey_num = (offset >> 3) & ~0x04;
/*TODO*///    int control = (offset & 0x20) >> 2;
/*TODO*///    int pokey_reg = (offset % 8) | control;
/*TODO*///
/*TODO*///    pokey_register_w(pokey_num, pokey_reg, data);
/*TODO*///}
/*TODO*///
/*TODO*///void pokey1_serin_ready(int after)
/*TODO*///{
/*TODO*///	timer_set(1.0 * after / intf.baseclock, 0, pokey_serin_ready);
/*TODO*///}
/*TODO*///
/*TODO*///void pokey2_serin_ready(int after)
/*TODO*///{
/*TODO*///	timer_set(1.0 * after / intf.baseclock, 1, pokey_serin_ready);
/*TODO*///}
/*TODO*///
/*TODO*///void pokey3_serin_ready(int after)
/*TODO*///{
/*TODO*///	timer_set(1.0 * after / intf.baseclock, 2, pokey_serin_ready);
/*TODO*///}
/*TODO*///
/*TODO*///void pokey4_serin_ready(int after)
/*TODO*///{
/*TODO*///	timer_set(1.0 * after / intf.baseclock, 3, pokey_serin_ready);
/*TODO*///}
/*TODO*///
/*TODO*///void pokey_break_w(int chip, int shift)
/*TODO*///{
/*TODO*///	struct POKEYregisters *p = &pokey[chip];
/*TODO*///    if( shift )                     /* shift code ? */
/*TODO*///		p->SKSTAT |= SK_SHIFT;
/*TODO*///	else
/*TODO*///		p->SKSTAT &= ~SK_SHIFT;
/*TODO*///	/* check if the break IRQ is enabled */
/*TODO*///	if( p->IRQEN & IRQ_BREAK )
/*TODO*///	{
/*TODO*///		/* set break IRQ status and call back the interrupt handler */
/*TODO*///		p->IRQST |= IRQ_BREAK;
/*TODO*///		if( p->interrupt_cb )
/*TODO*///			(*p->interrupt_cb)(IRQ_BREAK);
/*TODO*///    }
/*TODO*///}
/*TODO*///
/*TODO*///void pokey1_break_w(int shift)
/*TODO*///{
/*TODO*///	pokey_break_w(0, shift);
/*TODO*///}
/*TODO*///
/*TODO*///void pokey2_break_w(int shift)
/*TODO*///{
/*TODO*///	pokey_break_w(1, shift);
/*TODO*///}
/*TODO*///
/*TODO*///void pokey3_break_w(int shift)
/*TODO*///{
/*TODO*///	pokey_break_w(2, shift);
/*TODO*///}
/*TODO*///
/*TODO*///void pokey4_break_w(int shift)
/*TODO*///{
/*TODO*///	pokey_break_w(3, shift);
/*TODO*///}
/*TODO*///
/*TODO*///void pokey_kbcode_w(int chip, int kbcode, int make)
/*TODO*///{
/*TODO*///	struct POKEYregisters *p = &pokey[chip];
/*TODO*///    /* make code ? */
/*TODO*///	if( make )
/*TODO*///	{
/*TODO*///		p->KBCODE = kbcode;
/*TODO*///		p->SKSTAT |= SK_KEYBD;
/*TODO*///		if( kbcode & 0x40 ) 		/* shift code ? */
/*TODO*///			p->SKSTAT |= SK_SHIFT;
/*TODO*///		else
/*TODO*///			p->SKSTAT &= ~SK_SHIFT;
/*TODO*///
/*TODO*///		if( p->IRQEN & IRQ_KEYBD )
/*TODO*///		{
/*TODO*///			/* last interrupt not acknowledged ? */
/*TODO*///			if( p->IRQST & IRQ_KEYBD )
/*TODO*///				p->SKSTAT |= SK_KBERR;
/*TODO*///			p->IRQST |= IRQ_KEYBD;
/*TODO*///			if( p->interrupt_cb )
/*TODO*///				(*p->interrupt_cb)(IRQ_KEYBD);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		p->KBCODE = kbcode;
/*TODO*///		p->SKSTAT &= ~SK_KEYBD;
/*TODO*///    }
/*TODO*///}
/*TODO*///
/*TODO*///void pokey1_kbcode_w(int kbcode, int make)
/*TODO*///{
/*TODO*///	pokey_kbcode_w(0, kbcode, make);
/*TODO*///}
/*TODO*///
/*TODO*///void pokey2_kbcode_w(int kbcode, int make)
/*TODO*///{
/*TODO*///	pokey_kbcode_w(1, kbcode, make);
/*TODO*///}
/*TODO*///
/*TODO*///void pokey3_kbcode_w(int kbcode, int make)
/*TODO*///{
/*TODO*///	pokey_kbcode_w(2, kbcode, make);
/*TODO*///}
/*TODO*///
/*TODO*///void pokey4_kbcode_w(int kbcode, int make)
/*TODO*///{
/*TODO*///	pokey_kbcode_w(3, kbcode, make);
/*TODO*///}
/*TODO*///
/*TODO*///
}
