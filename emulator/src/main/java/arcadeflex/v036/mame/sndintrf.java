/*
 * ported to 0.36
 */
package arcadeflex.v036.mame;

import arcadeflex.v036.generic.funcPtr.ReadHandlerPtr;
import arcadeflex.v036.generic.funcPtr.TimerCallbackHandlerPtr;
import arcadeflex.v036.generic.funcPtr.WriteHandlerPtr;
import static arcadeflex.v036.mame.timer.timer_set;
import static arcadeflex.v036.mame.timerH.TIME_NOW;
import static gr.codebb.arcadeflex.v036.mame.mame.errorlog;
import static gr.codebb.arcadeflex.v036.platform.libc_old.fprintf;

public class sndintrf {

    static int cleared_value = 0x00;

    static int latch, read_debug;

    public static TimerCallbackHandlerPtr soundlatch_callback = new TimerCallbackHandlerPtr() {
        public void handler(int param) {
            if (errorlog != null && read_debug == 0 && latch != param) {
                fprintf(errorlog, "Warning: sound latch written before being read. Previous: %02x, new: %02x\n", latch, param);
            }
            latch = param;
            read_debug = 0;
        }
    };

    public static WriteHandlerPtr soundlatch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            /* make all the CPUs synchronize, and only AFTER that write the new command to the latch */
            timer_set(TIME_NOW, data, soundlatch_callback);
        }
    };
    public static ReadHandlerPtr soundlatch_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            read_debug = 1;
            return latch;
        }
    };
    public static WriteHandlerPtr soundlatch_clear_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            latch = cleared_value;
        }
    };

    static int latch2, read_debug2;

    public static TimerCallbackHandlerPtr soundlatch2_callback = new TimerCallbackHandlerPtr() {
        public void handler(int param) {
            if (errorlog != null && read_debug2 == 0 && latch2 != param) {
                fprintf(errorlog, "Warning: sound latch 2 written before being read. Previous: %02x, new: %02x\n", latch2, param);
            }
            latch2 = param;
            read_debug2 = 0;
        }
    };
    public static WriteHandlerPtr soundlatch2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            /* make all the CPUs synchronize, and only AFTER that write the new command to the latch */
            timer_set(TIME_NOW, data, soundlatch2_callback);
        }
    };
    public static ReadHandlerPtr soundlatch2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            read_debug2 = 1;
            return latch2;
        }
    };

    public static WriteHandlerPtr soundlatch2_clear_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            latch2 = cleared_value;
        }
    };
    static int latch3, read_debug3;

    static TimerCallbackHandlerPtr soundlatch3_callback = new TimerCallbackHandlerPtr() {
        public void handler(int param) {
            if (errorlog != null && read_debug3 == 0 && latch3 != param) {
                fprintf(errorlog, "Warning: sound latch 3 written before being read. Previous: %02x, new: %02x\n", latch3, param);
            }
            latch3 = param;
            read_debug3 = 0;
        }
    };

    public static WriteHandlerPtr soundlatch3_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* make all the CPUs synchronize, and only AFTER that write the new command to the latch */
            timer_set(TIME_NOW, data, soundlatch3_callback);
        }
    };

    public static ReadHandlerPtr soundlatch3_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            read_debug3 = 1;
            return latch3;
        }
    };

    public static WriteHandlerPtr soundlatch3_clear_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            latch3 = cleared_value;
        }
    };

    /*TODO*///
/*TODO*///static int latch4,read_debug4;
/*TODO*///
/*TODO*///static void soundlatch4_callback(int param)
/*TODO*///{
/*TODO*///if (errorlog && read_debug4 == 0 && latch4 != param)
/*TODO*///	fprintf(errorlog,"Warning: sound latch 4 written before being read. Previous: %02x, new: %02x\n",latch2,param);
/*TODO*///	latch4 = param;
/*TODO*///	read_debug4 = 0;
/*TODO*///}
/*TODO*///
/*TODO*///void soundlatch4_w(int offset,int data)
/*TODO*///{
/*TODO*///	/* make all the CPUs synchronize, and only AFTER that write the new command to the latch */
/*TODO*///	timer_set(TIME_NOW,data,soundlatch4_callback);
/*TODO*///}
/*TODO*///
/*TODO*///int soundlatch4_r(int offset)
/*TODO*///{
/*TODO*///	read_debug4 = 1;
/*TODO*///	return latch4;
/*TODO*///}
/*TODO*///
/*TODO*///void soundlatch4_clear_w(int offset, int data)
/*TODO*///{
/*TODO*///	latch4 = cleared_value;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void soundlatch_setclearedvalue(int value)
/*TODO*///{
/*TODO*///	cleared_value = value;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*///static void *sound_update_timer;
/*TODO*///static double refresh_period;
/*TODO*///static double refresh_period_inv;
/*TODO*///
/*TODO*///
/*TODO*///struct snd_interface
/*TODO*///{
/*TODO*///	unsigned sound_num;										/* ID */
/*TODO*///	const char *name;										/* description */
/*TODO*///	int (*chips_num)(const struct MachineSound *msound);	/* returns number of chips if applicable */
/*TODO*///	int (*chips_clock)(const struct MachineSound *msound);	/* returns chips clock if applicable */
/*TODO*///	int (*start)(const struct MachineSound *msound);		/* starts sound emulation */
/*TODO*///	void (*stop)(void);										/* stops sound emulation */
/*TODO*///	void (*update)(void);									/* updates emulation once per frame if necessary */
/*TODO*///	void (*reset)(void);									/* resets sound emulation */
/*TODO*///};
/*TODO*///
/*TODO*///
/*TODO*///#if (HAS_CUSTOM)
/*TODO*///static const struct CustomSound_interface *cust_intf;
/*TODO*///
/*TODO*///int custom_sh_start(const struct MachineSound *msound)
/*TODO*///{
/*TODO*///	cust_intf = msound->sound_interface;
/*TODO*///
/*TODO*///	if (cust_intf->sh_start)
/*TODO*///		return (*cust_intf->sh_start)(msound);
/*TODO*///	else return 0;
/*TODO*///}
/*TODO*///void custom_sh_stop(void)
/*TODO*///{
/*TODO*///	if (cust_intf->sh_stop) (*cust_intf->sh_stop)();
/*TODO*///}
/*TODO*///void custom_sh_update(void)
/*TODO*///{
/*TODO*///	if (cust_intf->sh_update) (*cust_intf->sh_update)();
/*TODO*///}
/*TODO*///#endif
/*TODO*///#if (HAS_DAC)
/*TODO*///int DAC_num(const struct MachineSound *msound) { return ((struct DACinterface*)msound->sound_interface)->num; }
/*TODO*///#endif
/*TODO*///#if (HAS_ADPCM)
/*TODO*///int ADPCM_num(const struct MachineSound *msound) { return ((struct ADPCMinterface*)msound->sound_interface)->num; }
/*TODO*///#endif
/*TODO*///#if (HAS_OKIM6295)
/*TODO*///int OKIM6295_num(const struct MachineSound *msound) { return ((struct OKIM6295interface*)msound->sound_interface)->num; }
/*TODO*///int OKIM6295_clock(const struct MachineSound *msound) { return ((struct OKIM6295interface*)msound->sound_interface)->frequency[0]; }
/*TODO*///#endif
/*TODO*///#if (HAS_MSM5205)
/*TODO*///int MSM5205_num(const struct MachineSound *msound) { return ((struct MSM5205interface*)msound->sound_interface)->num; }
/*TODO*///#endif
/*TODO*///#if (HAS_HC55516)
/*TODO*///int HC55516_num(const struct MachineSound *msound) { return ((struct hc55516_interface*)msound->sound_interface)->num; }
/*TODO*///#endif
/*TODO*///#if (HAS_K007232)
/*TODO*///int K007232_num(const struct MachineSound *msound) { return ((struct K007232_interface*)msound->sound_interface)->num_chips; }
/*TODO*///#endif
/*TODO*///#if (HAS_AY8910)
/*TODO*///int AY8910_clock(const struct MachineSound *msound) { return ((struct AY8910interface*)msound->sound_interface)->baseclock; }
/*TODO*///int AY8910_num(const struct MachineSound *msound) { return ((struct AY8910interface*)msound->sound_interface)->num; }
/*TODO*///#endif
/*TODO*///#if (HAS_YM2203)
/*TODO*///int YM2203_clock(const struct MachineSound *msound) { return ((struct YM2203interface*)msound->sound_interface)->baseclock; }
/*TODO*///int YM2203_num(const struct MachineSound *msound) { return ((struct YM2203interface*)msound->sound_interface)->num; }
/*TODO*///#endif
/*TODO*///#if (HAS_YM2413)
/*TODO*///int YM2413_clock(const struct MachineSound *msound) { return ((struct YM2413interface*)msound->sound_interface)->baseclock; }
/*TODO*///int YM2413_num(const struct MachineSound *msound) { return ((struct YM2413interface*)msound->sound_interface)->num; }
/*TODO*///#endif
/*TODO*///#if (HAS_YM2608)
/*TODO*///int YM2608_clock(const struct MachineSound *msound) { return ((struct YM2608interface*)msound->sound_interface)->baseclock; }
/*TODO*///int YM2608_num(const struct MachineSound *msound) { return ((struct YM2608interface*)msound->sound_interface)->num; }
/*TODO*///#endif
/*TODO*///#if (HAS_YM2610)
/*TODO*///int YM2610_clock(const struct MachineSound *msound) { return ((struct YM2610interface*)msound->sound_interface)->baseclock; }
/*TODO*///int YM2610_num(const struct MachineSound *msound) { return ((struct YM2610interface*)msound->sound_interface)->num; }
/*TODO*///#endif
/*TODO*///#if (HAS_YM2612)
/*TODO*///int YM2612_clock(const struct MachineSound *msound) { return ((struct YM2612interface*)msound->sound_interface)->baseclock; }
/*TODO*///int YM2612_num(const struct MachineSound *msound) { return ((struct YM2612interface*)msound->sound_interface)->num; }
/*TODO*///#endif
/*TODO*///#if (HAS_POKEY)
/*TODO*///int POKEY_clock(const struct MachineSound *msound) { return ((struct POKEYinterface*)msound->sound_interface)->baseclock; }
/*TODO*///int POKEY_num(const struct MachineSound *msound) { return ((struct POKEYinterface*)msound->sound_interface)->num; }
/*TODO*///#endif
/*TODO*///#if (HAS_TIA)
/*TODO*///int TIA_clock(const struct MachineSound *msound) { return ((struct TIAinterface*)msound->sound_interface)->baseclock; }
/*TODO*///#endif
/*TODO*///#if (HAS_YM3812)
/*TODO*///int YM3812_clock(const struct MachineSound *msound) { return ((struct YM3812interface*)msound->sound_interface)->baseclock; }
/*TODO*///int YM3812_num(const struct MachineSound *msound) { return ((struct YM3812interface*)msound->sound_interface)->num; }
/*TODO*///#endif
/*TODO*///#if (HAS_VLM5030)
/*TODO*///int VLM5030_clock(const struct MachineSound *msound) { return ((struct VLM5030interface*)msound->sound_interface)->baseclock; }
/*TODO*///#endif
/*TODO*///#if (HAS_TMS36XX)
/*TODO*///int TMS36XX_num(const struct MachineSound *msound) { return ((struct TMS36XXinterface*)msound->sound_interface)->num; }
/*TODO*///#endif
/*TODO*///#if (HAS_TMS5220)
/*TODO*///int TMS5220_clock(const struct MachineSound *msound) { return ((struct TMS5220interface*)msound->sound_interface)->baseclock; }
/*TODO*///#endif
/*TODO*///#if (HAS_YM2151 || HAS_YM2151_ALT)
/*TODO*///int YM2151_clock(const struct MachineSound *msound) { return ((struct YM2151interface*)msound->sound_interface)->baseclock; }
/*TODO*///int YM2151_num(const struct MachineSound *msound) { return ((struct YM2151interface*)msound->sound_interface)->num; }
/*TODO*///#endif
/*TODO*///#if (HAS_NES)
/*TODO*///int NES_num(const struct MachineSound *msound) { return ((struct NESinterface*)msound->sound_interface)->num; }
/*TODO*///#endif
/*TODO*///#if (HAS_SN76477)
/*TODO*///int SN76477_num(const struct MachineSound *msound) { return ((struct SN76477interface*)msound->sound_interface)->num; }
/*TODO*///#endif
/*TODO*///#if (HAS_SN76496)
/*TODO*///int SN76496_clock(const struct MachineSound *msound) { return ((struct SN76496interface*)msound->sound_interface)->baseclock[0]; }
/*TODO*///int SN76496_num(const struct MachineSound *msound) { return ((struct SN76496interface*)msound->sound_interface)->num; }
/*TODO*///#endif
/*TODO*///#if (HAS_MSM5205)
/*TODO*///int MSM5205_clock(const struct MachineSound *msound) { return ((struct MSM5205interface*)msound->sound_interface)->baseclock; }
/*TODO*///#endif
/*TODO*///#if (HAS_UPD7759)
/*TODO*///int UPD7759_clock(const struct MachineSound *msound) { return ((struct UPD7759_interface*)msound->sound_interface)->clock_rate; }
/*TODO*///#endif
/*TODO*///#if (HAS_ASTROCADE)
/*TODO*///int ASTROCADE_clock(const struct MachineSound *msound) { return ((struct astrocade_interface*)msound->sound_interface)->baseclock; }
/*TODO*///int ASTROCADE_num(const struct MachineSound *msound) { return ((struct astrocade_interface*)msound->sound_interface)->num; }
/*TODO*///#endif
/*TODO*///#if (HAS_K051649)
/*TODO*///int K051649_clock(const struct MachineSound *msound) { return ((struct k051649_interface*)msound->sound_interface)->master_clock; }
/*TODO*///#endif
/*TODO*///#if (HAS_K053260)
/*TODO*///int K053260_clock(const struct MachineSound *msound) { return ((struct K053260_interface*)msound->sound_interface)->clock; }
/*TODO*///#endif
/*TODO*///#if (HAS_CEM3394)
/*TODO*///int cem3394_num(const struct MachineSound *msound) { return ((struct cem3394_interface*)msound->sound_interface)->numchips; }
/*TODO*///#endif
/*TODO*///#if (HAS_QSOUND)
/*TODO*///int qsound_clock(const struct MachineSound *msound) { return ((struct QSound_interface*)msound->sound_interface)->clock; }
/*TODO*///#endif
/*TODO*///#if (HAS_SPEAKER)
/*TODO*///int speaker_num(const struct MachineSound *msound) { return ((struct Speaker_interface*)msound->sound_interface)->num; }
/*TODO*///#endif
/*TODO*///#if (HAS_WAVE)
/*TODO*///int wave_num(const struct MachineSound *msound) { return ((struct Wave_interface*)msound->sound_interface)->num; }
/*TODO*///#endif
/*TODO*///
/*TODO*///struct snd_interface sndintf[] =
/*TODO*///{
/*TODO*///    {
/*TODO*///		SOUND_DUMMY,
/*TODO*///		"",
/*TODO*///		0,
/*TODO*///		0,
/*TODO*///		0,
/*TODO*///		0,
/*TODO*///		0,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#if (HAS_CUSTOM)
/*TODO*///    {
/*TODO*///		SOUND_CUSTOM,
/*TODO*///		"Custom",
/*TODO*///		0,
/*TODO*///		0,
/*TODO*///		custom_sh_start,
/*TODO*///		custom_sh_stop,
/*TODO*///		custom_sh_update,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_SAMPLES)
/*TODO*///    {
/*TODO*///		SOUND_SAMPLES,
/*TODO*///		"Samples",
/*TODO*///		0,
/*TODO*///		0,
/*TODO*///		samples_sh_start,
/*TODO*///		0,
/*TODO*///		0,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_DAC)
/*TODO*///    {
/*TODO*///		SOUND_DAC,
/*TODO*///		"DAC",
/*TODO*///		DAC_num,
/*TODO*///		0,
/*TODO*///		DAC_sh_start,
/*TODO*///		0,
/*TODO*///		0,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_AY8910)
/*TODO*///    {
/*TODO*///		SOUND_AY8910,
/*TODO*///		"AY-8910",
/*TODO*///		AY8910_num,
/*TODO*///		AY8910_clock,
/*TODO*///		AY8910_sh_start,
/*TODO*///		0,
/*TODO*///		0,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_YM2203)
/*TODO*///    {
/*TODO*///		SOUND_YM2203,
/*TODO*///		"YM-2203",
/*TODO*///		YM2203_num,
/*TODO*///		YM2203_clock,
/*TODO*///		YM2203_sh_start,
/*TODO*///		YM2203_sh_stop,
/*TODO*///		0,
/*TODO*///		YM2203_sh_reset
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_YM2151)
/*TODO*///    {
/*TODO*///		SOUND_YM2151,
/*TODO*///		"YM-2151",
/*TODO*///		YM2151_num,
/*TODO*///		YM2151_clock,
/*TODO*///		YM2151_sh_start,
/*TODO*///		YM2151_sh_stop,
/*TODO*///		0,
/*TODO*///		YM2151_sh_reset
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_YM2151_ALT)
/*TODO*///    {
/*TODO*///		SOUND_YM2151,
/*TODO*///		"YM-2151",
/*TODO*///		YM2151_num,
/*TODO*///		YM2151_clock,
/*TODO*///		YM2151_ALT_sh_start,
/*TODO*///		YM2151_sh_stop,
/*TODO*///		0,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_YM2608)
/*TODO*///    {
/*TODO*///		SOUND_YM2608,
/*TODO*///		"YM-2608",
/*TODO*///		YM2608_num,
/*TODO*///		YM2608_clock,
/*TODO*///		YM2608_sh_start,
/*TODO*///		YM2608_sh_stop,
/*TODO*///		0,
/*TODO*///		YM2608_sh_reset
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_YM2610)
/*TODO*///    {
/*TODO*///		SOUND_YM2610,
/*TODO*///		"YM-2610",
/*TODO*///		YM2610_num,
/*TODO*///		YM2610_clock,
/*TODO*///		YM2610_sh_start,
/*TODO*///		YM2610_sh_stop,
/*TODO*///		0,
/*TODO*///		YM2610_sh_reset
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_YM2610B)
/*TODO*///    {
/*TODO*///		SOUND_YM2610B,
/*TODO*///		"YM-2610B",
/*TODO*///		YM2610_num,
/*TODO*///		YM2610_clock,
/*TODO*///		YM2610B_sh_start,
/*TODO*///		YM2610_sh_stop,
/*TODO*///		0,
/*TODO*///		YM2610_sh_reset
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_YM2612)
/*TODO*///    {
/*TODO*///		SOUND_YM2612,
/*TODO*///		"YM-2612",
/*TODO*///		YM2612_num,
/*TODO*///		YM2612_clock,
/*TODO*///		YM2612_sh_start,
/*TODO*///		YM2612_sh_stop,
/*TODO*///		0,
/*TODO*///		YM2612_sh_reset
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_YM3438)
/*TODO*///    {
/*TODO*///		SOUND_YM3438,
/*TODO*///		"YM-3438",
/*TODO*///		YM2612_num,
/*TODO*///		YM2612_clock,
/*TODO*///		YM2612_sh_start,
/*TODO*///		YM2612_sh_stop,
/*TODO*///		0,
/*TODO*///		YM2612_sh_reset
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_YM2413)
/*TODO*///    {
/*TODO*///		SOUND_YM2413,
/*TODO*///		"YM-2413",
/*TODO*///		YM2413_num,
/*TODO*///		YM2413_clock,
/*TODO*///		YM2413_sh_start,
/*TODO*///		YM2413_sh_stop,
/*TODO*///		0,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_YM3812)
/*TODO*///    {
/*TODO*///		SOUND_YM3812,
/*TODO*///		"YM-3812",
/*TODO*///		YM3812_num,
/*TODO*///		YM3812_clock,
/*TODO*///		YM3812_sh_start,
/*TODO*///		YM3812_sh_stop,
/*TODO*///		0,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_YM3526)
/*TODO*///    {
/*TODO*///		SOUND_YM3526,
/*TODO*///		"YM-3526",
/*TODO*///		YM3812_num,
/*TODO*///		YM3812_clock,
/*TODO*///		YM3812_sh_start,
/*TODO*///		YM3812_sh_stop,
/*TODO*///		0,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_Y8950)
/*TODO*///	{
/*TODO*///		SOUND_Y8950,
/*TODO*///		"Y8950",	/* (MSX-AUDIO) */
/*TODO*///		YM3812_num,
/*TODO*///		YM3812_clock,
/*TODO*///		Y8950_sh_start,
/*TODO*///		Y8950_sh_stop,
/*TODO*///		0,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_SN76477)
/*TODO*///    {
/*TODO*///		SOUND_SN76477,
/*TODO*///		"SN76477",
/*TODO*///		SN76477_num,
/*TODO*///		0,
/*TODO*///		SN76477_sh_start,
/*TODO*///		SN76477_sh_stop,
/*TODO*///		0,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_SN76496)
/*TODO*///    {
/*TODO*///		SOUND_SN76496,
/*TODO*///		"SN76496",
/*TODO*///		SN76496_num,
/*TODO*///		SN76496_clock,
/*TODO*///		SN76496_sh_start,
/*TODO*///        0,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_POKEY)
/*TODO*///    {
/*TODO*///		SOUND_POKEY,
/*TODO*///		"Pokey",
/*TODO*///		POKEY_num,
/*TODO*///		POKEY_clock,
/*TODO*///		pokey_sh_start,
/*TODO*///		pokey_sh_stop,
/*TODO*///		0,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_TIA)
/*TODO*///    {
/*TODO*///		SOUND_TIA,
/*TODO*///		"TIA",
/*TODO*///		0,
/*TODO*///		TIA_clock,
/*TODO*///		tia_sh_start,
/*TODO*///		tia_sh_stop,
/*TODO*///		tia_sh_update,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_NES)
/*TODO*///    {
/*TODO*///		SOUND_NES,
/*TODO*///		"Nintendo",
/*TODO*///		NES_num,
/*TODO*///		0,
/*TODO*///		NESPSG_sh_start,
/*TODO*///		NESPSG_sh_stop,
/*TODO*///		NESPSG_sh_update,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_ASTROCADE)
/*TODO*///    {
/*TODO*///		SOUND_ASTROCADE,
/*TODO*///		"Astrocade",
/*TODO*///		ASTROCADE_num,
/*TODO*///		ASTROCADE_clock,
/*TODO*///		astrocade_sh_start,
/*TODO*///		astrocade_sh_stop,
/*TODO*///		astrocade_sh_update,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_NAMCO)
/*TODO*///    {
/*TODO*///		SOUND_NAMCO,
/*TODO*///		"Namco",
/*TODO*///		0,
/*TODO*///		0,
/*TODO*///		namco_sh_start,
/*TODO*///		namco_sh_stop,
/*TODO*///		0,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_TMS36XX)
/*TODO*///    {
/*TODO*///		SOUND_TMS36XX,
/*TODO*///		"TMS36XX",
/*TODO*///		TMS36XX_num,
/*TODO*///        0,
/*TODO*///		tms36xx_sh_start,
/*TODO*///		tms36xx_sh_stop,
/*TODO*///		tms36xx_sh_update,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_TMS5220)
/*TODO*///    {
/*TODO*///		SOUND_TMS5220,
/*TODO*///		"TMS5520",
/*TODO*///		0,
/*TODO*///		TMS5220_clock,
/*TODO*///		tms5220_sh_start,
/*TODO*///		tms5220_sh_stop,
/*TODO*///		tms5220_sh_update,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_VLM5030)
/*TODO*///    {
/*TODO*///		SOUND_VLM5030,
/*TODO*///		"VLM5030",
/*TODO*///		0,
/*TODO*///		VLM5030_clock,
/*TODO*///		VLM5030_sh_start,
/*TODO*///		VLM5030_sh_stop,
/*TODO*///		VLM5030_sh_update,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_ADPCM)
/*TODO*///    {
/*TODO*///		SOUND_ADPCM,
/*TODO*///		"ADPCM",
/*TODO*///		ADPCM_num,
/*TODO*///		0,
/*TODO*///		ADPCM_sh_start,
/*TODO*///		ADPCM_sh_stop,
/*TODO*///		ADPCM_sh_update,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_OKIM6295)
/*TODO*///    {
/*TODO*///		SOUND_OKIM6295,
/*TODO*///		"OKI6295",
/*TODO*///		OKIM6295_num,
/*TODO*///		OKIM6295_clock,
/*TODO*///		OKIM6295_sh_start,
/*TODO*///		OKIM6295_sh_stop,
/*TODO*///		OKIM6295_sh_update,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_MSM5205)
/*TODO*///    {
/*TODO*///		SOUND_MSM5205,
/*TODO*///		"MSM5205",
/*TODO*///		MSM5205_num,
/*TODO*///		MSM5205_clock,
/*TODO*///		MSM5205_sh_start,
/*TODO*///		0,
/*TODO*///		0,
/*TODO*///		MSM5205_sh_reset,
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_UPD7759)
/*TODO*///    {
/*TODO*///		SOUND_UPD7759,
/*TODO*///		"uPD7759",
/*TODO*///		0,
/*TODO*///		UPD7759_clock,
/*TODO*///		UPD7759_sh_start,
/*TODO*///		UPD7759_sh_stop,
/*TODO*///		0,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_HC55516)
/*TODO*///    {
/*TODO*///		SOUND_HC55516,
/*TODO*///		"HC55516",
/*TODO*///		HC55516_num,
/*TODO*///		0,
/*TODO*///		hc55516_sh_start,
/*TODO*///		0,
/*TODO*///		0,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_K005289)
/*TODO*///    {
/*TODO*///		SOUND_K005289,
/*TODO*///		"005289",
/*TODO*///		0,
/*TODO*///		0,
/*TODO*///		K005289_sh_start,
/*TODO*///		K005289_sh_stop,
/*TODO*///		0,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_K007232)
/*TODO*///    {
/*TODO*///		SOUND_K007232,
/*TODO*///		"007232",
/*TODO*///		K007232_num,
/*TODO*///		0,
/*TODO*///		K007232_sh_start,
/*TODO*///		0,
/*TODO*///		0,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_K051649)
/*TODO*///    {
/*TODO*///		SOUND_K051649,
/*TODO*///		"051649",
/*TODO*///		0,
/*TODO*///		K051649_clock,
/*TODO*///		K051649_sh_start,
/*TODO*///		K051649_sh_stop,
/*TODO*///		0,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_K053260)
/*TODO*///    {
/*TODO*///		SOUND_K053260,
/*TODO*///		"053260",
/*TODO*///		0,
/*TODO*///		K053260_clock,
/*TODO*///		K053260_sh_start,
/*TODO*///		K053260_sh_stop,
/*TODO*///		0,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_SEGAPCM)
/*TODO*///	{
/*TODO*///		SOUND_SEGAPCM,
/*TODO*///		"Sega PCM",
/*TODO*///		0,
/*TODO*///		0,
/*TODO*///		SEGAPCM_sh_start,
/*TODO*///		SEGAPCM_sh_stop,
/*TODO*///		SEGAPCM_sh_update,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_RF5C68)
/*TODO*///	{
/*TODO*///		SOUND_RF5C68,
/*TODO*///		"RF5C68",
/*TODO*///		0,
/*TODO*///		0,
/*TODO*///		RF5C68_sh_start,
/*TODO*///		RF5C68_sh_stop,
/*TODO*///		0,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_CEM3394)
/*TODO*///	{
/*TODO*///		SOUND_CEM3394,
/*TODO*///		"CEM3394",
/*TODO*///		cem3394_num,
/*TODO*///		0,
/*TODO*///		cem3394_sh_start,
/*TODO*///		cem3394_sh_stop,
/*TODO*///		0,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_C140)
/*TODO*///	{
/*TODO*///		SOUND_C140,
/*TODO*///		"C140",
/*TODO*///		0,
/*TODO*///		0,
/*TODO*///		C140_sh_start,
/*TODO*///		C140_sh_stop,
/*TODO*///		0,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_QSOUND)
/*TODO*///	{
/*TODO*///		SOUND_QSOUND,
/*TODO*///		"QSound",
/*TODO*///		0,
/*TODO*///		qsound_clock,
/*TODO*///		qsound_sh_start,
/*TODO*///		qsound_sh_stop,
/*TODO*///		0,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_SPEAKER)
/*TODO*///	{
/*TODO*///		SOUND_SPEAKER,
/*TODO*///		"Speaker",
/*TODO*///		speaker_num,
/*TODO*///		0,
/*TODO*///		speaker_sh_start,
/*TODO*///		speaker_sh_stop,
/*TODO*///		speaker_sh_update,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///#if (HAS_WAVE)
/*TODO*///	{
/*TODO*///		SOUND_WAVE,
/*TODO*///		"Cassette",
/*TODO*///		wave_num,
/*TODO*///		0,
/*TODO*///		wave_sh_start,
/*TODO*///		wave_sh_stop,
/*TODO*///		wave_sh_update,
/*TODO*///		0
/*TODO*///	},
/*TODO*///#endif
/*TODO*///};
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///int sound_start(void)
/*TODO*///{
/*TODO*///	int totalsound = 0;
/*TODO*///	int i;
/*TODO*///
/*TODO*///	/* Verify the order of entries in the sndintf[] array */
/*TODO*///	for (i = 0;i < SOUND_COUNT;i++)
/*TODO*///	{
/*TODO*///		if (sndintf[i].sound_num != i)
/*TODO*///		{
/*TODO*///if (errorlog) fprintf(errorlog,"Sound #%d wrong ID %d: check enum SOUND_... in src/sndintrf.h!\n",i,sndintf[i].sound_num);
/*TODO*///			return 1;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	/* samples will be read later if needed */
/*TODO*///	Machine->samples = 0;
/*TODO*///
/*TODO*///	refresh_period = TIME_IN_HZ(Machine->drv->frames_per_second);
/*TODO*///	refresh_period_inv = 1.0 / refresh_period;
/*TODO*///	sound_update_timer = timer_set(TIME_NEVER,0,NULL);
/*TODO*///
/*TODO*///	if (mixer_sh_start() != 0)
/*TODO*///		return 1;
/*TODO*///
/*TODO*///	if (streams_sh_start() != 0)
/*TODO*///		return 1;
/*TODO*///
/*TODO*///	while (Machine->drv->sound[totalsound].sound_type != 0 && totalsound < MAX_SOUND)
/*TODO*///	{
/*TODO*///		if ((*sndintf[Machine->drv->sound[totalsound].sound_type].start)(&Machine->drv->sound[totalsound]) != 0)
/*TODO*///			goto getout;
/*TODO*///
/*TODO*///		totalsound++;
/*TODO*///	}
/*TODO*///
/*TODO*///	return 0;
/*TODO*///
/*TODO*///
/*TODO*///getout:
/*TODO*///	/* TODO: should also free the resources allocated before */
/*TODO*///	return 1;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///void sound_stop(void)
/*TODO*///{
/*TODO*///	int totalsound = 0;
/*TODO*///
/*TODO*///
/*TODO*///	while (Machine->drv->sound[totalsound].sound_type != 0 && totalsound < MAX_SOUND)
/*TODO*///	{
/*TODO*///		if (sndintf[Machine->drv->sound[totalsound].sound_type].stop)
/*TODO*///			(*sndintf[Machine->drv->sound[totalsound].sound_type].stop)();
/*TODO*///
/*TODO*///		totalsound++;
/*TODO*///	}
/*TODO*///
/*TODO*///	streams_sh_stop();
/*TODO*///	mixer_sh_stop();
/*TODO*///
/*TODO*///	if (sound_update_timer)
/*TODO*///	{
/*TODO*///		timer_remove(sound_update_timer);
/*TODO*///		sound_update_timer = 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* free audio samples */
/*TODO*///	freesamples(Machine->samples);
/*TODO*///	Machine->samples = 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///void sound_update(void)
/*TODO*///{
/*TODO*///	int totalsound = 0;
/*TODO*///
/*TODO*///
/*TODO*///	profiler_mark(PROFILER_SOUND);
/*TODO*///
/*TODO*///	while (Machine->drv->sound[totalsound].sound_type != 0 && totalsound < MAX_SOUND)
/*TODO*///	{
/*TODO*///		if (sndintf[Machine->drv->sound[totalsound].sound_type].update)
/*TODO*///			(*sndintf[Machine->drv->sound[totalsound].sound_type].update)();
/*TODO*///
/*TODO*///		totalsound++;
/*TODO*///	}
/*TODO*///
/*TODO*///	streams_sh_update();
/*TODO*///	mixer_sh_update();
/*TODO*///
/*TODO*///	timer_reset(sound_update_timer,TIME_NEVER);
/*TODO*///
/*TODO*///	profiler_mark(PROFILER_END);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void sound_reset(void)
/*TODO*///{
/*TODO*///	int totalsound = 0;
/*TODO*///
/*TODO*///
/*TODO*///	while (Machine->drv->sound[totalsound].sound_type != 0 && totalsound < MAX_SOUND)
/*TODO*///	{
/*TODO*///		if (sndintf[Machine->drv->sound[totalsound].sound_type].reset)
/*TODO*///			(*sndintf[Machine->drv->sound[totalsound].sound_type].reset)();
/*TODO*///
/*TODO*///		totalsound++;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///const char *sound_name(const struct MachineSound *msound)
/*TODO*///{
/*TODO*///	if (msound->sound_type < SOUND_COUNT)
/*TODO*///		return sndintf[msound->sound_type].name;
/*TODO*///	else
/*TODO*///		return "";
/*TODO*///}
/*TODO*///
/*TODO*///int sound_num(const struct MachineSound *msound)
/*TODO*///{
/*TODO*///	if (msound->sound_type < SOUND_COUNT && sndintf[msound->sound_type].chips_num)
/*TODO*///		return (*sndintf[msound->sound_type].chips_num)(msound);
/*TODO*///	else
/*TODO*///		return 0;
/*TODO*///}
/*TODO*///
/*TODO*///int sound_clock(const struct MachineSound *msound)
/*TODO*///{
/*TODO*///	if (msound->sound_type < SOUND_COUNT && sndintf[msound->sound_type].chips_clock)
/*TODO*///		return (*sndintf[msound->sound_type].chips_clock)(msound);
/*TODO*///	else
/*TODO*///		return 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///int sound_scalebufferpos(int value)
/*TODO*///{
/*TODO*///	int result = (int)((double)value * timer_timeelapsed (sound_update_timer) * refresh_period_inv);
/*TODO*///	if (value >= 0) return (result < value) ? result : value;
/*TODO*///	else return (result > value) ? result : value;
/*TODO*///}
/*TODO*///
}
