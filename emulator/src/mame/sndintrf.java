package mame;
import sound.Dummy_snd;
import sound.dac;
import sound.samples;
import sound.namco;
import sound.sn76496;
import static mame.sndintrfH.*;
import sound.ay8910;
import static mame.driverH.*;
import static mame.timer.*;
import static mame.timerH.*;
import static mame.mame.*;
import static arcadeflex.libc_old.*;
import sound.CustomSound;
import sound._3526intf;
import sound._3812intf;
import sound.y8950;

public class sndintrf {
    static int cleared_value = 0x00;
    
    static int latch,read_debug;

    public static timer_callback soundlatch_callback = new timer_callback(){ public void handler(int param){
        if (errorlog!=null && read_debug == 0 && latch != param)
    	fprintf(errorlog,"Warning: sound latch written before being read. Previous: %02x, new: %02x\n",latch,param);
    	latch = param;
    	read_debug = 0;
    }};
    public static WriteHandlerPtr soundlatch_w = new WriteHandlerPtr() { public void handler(int offset, int data)
    {

    	/* make all the CPUs synchronize, and only AFTER that write the new command to the latch */
    	timer_set(TIME_NOW,data,soundlatch_callback);
    }};
    public static ReadHandlerPtr soundlatch_r = new ReadHandlerPtr() { public int handler(int offset)
    {
        read_debug = 1;
        return latch;
    }};
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///void soundlatch_clear_w(int offset, int data)
    /*TODO*////*TODO*///{
    /*TODO*////*TODO*///	latch = cleared_value;
    /*TODO*////*TODO*///}
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///static int latch2,read_debug2;
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///static void soundlatch2_callback(int param)
    /*TODO*////*TODO*///{
    /*TODO*////*TODO*///if (errorlog && read_debug2 == 0 && latch2 != param)
    /*TODO*////*TODO*///	fprintf(errorlog,"Warning: sound latch 2 written before being read. Previous: %02x, new: %02x\n",latch2,param);
    /*TODO*////*TODO*///	latch2 = param;
    /*TODO*////*TODO*///	read_debug2 = 0;
    /*TODO*////*TODO*///}
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///void soundlatch2_w(int offset,int data)
    /*TODO*////*TODO*///{
    /*TODO*////*TODO*///	/* make all the CPUs synchronize, and only AFTER that write the new command to the latch */
    /*TODO*////*TODO*///	timer_set(TIME_NOW,data,soundlatch2_callback);
    /*TODO*////*TODO*///}
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///int soundlatch2_r(int offset)
    /*TODO*////*TODO*///{
    /*TODO*////*TODO*///	read_debug2 = 1;
    /*TODO*////*TODO*///	return latch2;
    /*TODO*////*TODO*///}
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///void soundlatch2_clear_w(int offset, int data)
    /*TODO*////*TODO*///{
    /*TODO*////*TODO*///	latch2 = cleared_value;
    /*TODO*////*TODO*///}
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///static int latch3,read_debug3;
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///static void soundlatch3_callback(int param)
    /*TODO*////*TODO*///{
    /*TODO*////*TODO*///if (errorlog && read_debug3 == 0 && latch3 != param)
    /*TODO*////*TODO*///	fprintf(errorlog,"Warning: sound latch 3 written before being read. Previous: %02x, new: %02x\n",latch3,param);
    /*TODO*////*TODO*///	latch3 = param;
    /*TODO*////*TODO*///	read_debug3 = 0;
    /*TODO*////*TODO*///}
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///void soundlatch3_w(int offset,int data)
    /*TODO*////*TODO*///{
    /*TODO*////*TODO*///	/* make all the CPUs synchronize, and only AFTER that write the new command to the latch */
    /*TODO*////*TODO*///	timer_set(TIME_NOW,data,soundlatch3_callback);
    /*TODO*////*TODO*///}
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///int soundlatch3_r(int offset)
    /*TODO*////*TODO*///{
    /*TODO*////*TODO*///	read_debug3 = 1;
    /*TODO*////*TODO*///	return latch3;
    /*TODO*////*TODO*///}
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///void soundlatch3_clear_w(int offset, int data)
    /*TODO*////*TODO*///{
    /*TODO*////*TODO*///	latch3 = cleared_value;
    /*TODO*////*TODO*///}
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///static int latch4,read_debug4;
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///static void soundlatch4_callback(int param)
    /*TODO*////*TODO*///{
    /*TODO*////*TODO*///if (errorlog && read_debug4 == 0 && latch4 != param)
    /*TODO*////*TODO*///	fprintf(errorlog,"Warning: sound latch 4 written before being read. Previous: %02x, new: %02x\n",latch2,param);
    /*TODO*////*TODO*///	latch4 = param;
    /*TODO*////*TODO*///	read_debug4 = 0;
    /*TODO*////*TODO*///}
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///void soundlatch4_w(int offset,int data)
    /*TODO*////*TODO*///{
    /*TODO*////*TODO*///	/* make all the CPUs synchronize, and only AFTER that write the new command to the latch */
    /*TODO*////*TODO*///	timer_set(TIME_NOW,data,soundlatch4_callback);
    /*TODO*////*TODO*///}
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///int soundlatch4_r(int offset)
    /*TODO*////*TODO*///{
    /*TODO*////*TODO*///	read_debug4 = 1;
    /*TODO*////*TODO*///	return latch4;
    /*TODO*////*TODO*///}
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///void soundlatch4_clear_w(int offset, int data)
    /*TODO*////*TODO*///{
    /*TODO*////*TODO*///	latch4 = cleared_value;
    /*TODO*////*TODO*///}
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///void soundlatch_setclearedvalue(int value)
    /*TODO*////*TODO*///{
    /*TODO*////*TODO*///	cleared_value = value;
    /*TODO*////*TODO*///}
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///
    /*TODO*////*TODO*////***************************************************************************
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///***************************************************************************/
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///static void *sound_update_timer;
    /*TODO*////*TODO*///static double refresh_period;
    /*TODO*////*TODO*///static double refresh_period_inv;
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///
    public static abstract class snd_interface
    {
        public int sound_num;
        public String name;										/* description */
        public abstract int chips_num(MachineSound msound);	/* returns number of chips if applicable */
        public abstract int chips_clock(MachineSound msound);	/* returns chips clock if applicable */
        public abstract int start(MachineSound msound);		/* starts sound emulation */
        public abstract void stop();										/* stops sound emulation */
        public abstract void update();									/* updates emulation once per frame if necessary */
        public abstract void reset();									/* resets sound emulation */
    }

    /*TODO*////*TODO*///#if (HAS_ADPCM)
    /*TODO*////*TODO*///int ADPCM_num(const struct MachineSound *msound) { return ((struct ADPCMinterface*)msound->sound_interface)->num; }
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_OKIM6295)
    /*TODO*////*TODO*///int OKIM6295_num(const struct MachineSound *msound) { return ((struct OKIM6295interface*)msound->sound_interface)->num; }
    /*TODO*////*TODO*///int OKIM6295_clock(const struct MachineSound *msound) { return ((struct OKIM6295interface*)msound->sound_interface)->frequency[0]; }
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_MSM5205)
    /*TODO*////*TODO*///int MSM5205_num(const struct MachineSound *msound) { return ((struct MSM5205interface*)msound->sound_interface)->num; }
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_HC55516)
    /*TODO*////*TODO*///int HC55516_num(const struct MachineSound *msound) { return ((struct hc55516_interface*)msound->sound_interface)->num; }
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_K007232)
    /*TODO*////*TODO*///int K007232_num(const struct MachineSound *msound) { return ((struct K007232_interface*)msound->sound_interface)->num_chips; }
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_YM2203)
    /*TODO*////*TODO*///int YM2203_clock(const struct MachineSound *msound) { return ((struct YM2203interface*)msound->sound_interface)->baseclock; }
    /*TODO*////*TODO*///int YM2203_num(const struct MachineSound *msound) { return ((struct YM2203interface*)msound->sound_interface)->num; }
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_YM2413)
    /*TODO*////*TODO*///int YM2413_clock(const struct MachineSound *msound) { return ((struct YM2413interface*)msound->sound_interface)->baseclock; }
    /*TODO*////*TODO*///int YM2413_num(const struct MachineSound *msound) { return ((struct YM2413interface*)msound->sound_interface)->num; }
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_YM2608)
    /*TODO*////*TODO*///int YM2608_clock(const struct MachineSound *msound) { return ((struct YM2608interface*)msound->sound_interface)->baseclock; }
    /*TODO*////*TODO*///int YM2608_num(const struct MachineSound *msound) { return ((struct YM2608interface*)msound->sound_interface)->num; }
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_YM2610)
    /*TODO*////*TODO*///int YM2610_clock(const struct MachineSound *msound) { return ((struct YM2610interface*)msound->sound_interface)->baseclock; }
    /*TODO*////*TODO*///int YM2610_num(const struct MachineSound *msound) { return ((struct YM2610interface*)msound->sound_interface)->num; }
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_YM2612)
    /*TODO*////*TODO*///int YM2612_clock(const struct MachineSound *msound) { return ((struct YM2612interface*)msound->sound_interface)->baseclock; }
    /*TODO*////*TODO*///int YM2612_num(const struct MachineSound *msound) { return ((struct YM2612interface*)msound->sound_interface)->num; }
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_POKEY)
    /*TODO*////*TODO*///int POKEY_clock(const struct MachineSound *msound) { return ((struct POKEYinterface*)msound->sound_interface)->baseclock; }
    /*TODO*////*TODO*///int POKEY_num(const struct MachineSound *msound) { return ((struct POKEYinterface*)msound->sound_interface)->num; }
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_TIA)
    /*TODO*////*TODO*///int TIA_clock(const struct MachineSound *msound) { return ((struct TIAinterface*)msound->sound_interface)->baseclock; }
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_VLM5030)
    /*TODO*////*TODO*///int VLM5030_clock(const struct MachineSound *msound) { return ((struct VLM5030interface*)msound->sound_interface)->baseclock; }
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_TMS36XX)
    /*TODO*////*TODO*///int TMS36XX_num(const struct MachineSound *msound) { return ((struct TMS36XXinterface*)msound->sound_interface)->num; }
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_TMS5220)
    /*TODO*////*TODO*///int TMS5220_clock(const struct MachineSound *msound) { return ((struct TMS5220interface*)msound->sound_interface)->baseclock; }
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_YM2151 || HAS_YM2151_ALT)
    /*TODO*////*TODO*///int YM2151_clock(const struct MachineSound *msound) { return ((struct YM2151interface*)msound->sound_interface)->baseclock; }
    /*TODO*////*TODO*///int YM2151_num(const struct MachineSound *msound) { return ((struct YM2151interface*)msound->sound_interface)->num; }
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_NES)
    /*TODO*////*TODO*///int NES_num(const struct MachineSound *msound) { return ((struct NESinterface*)msound->sound_interface)->num; }
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_SN76477)
    /*TODO*////*TODO*///int SN76477_num(const struct MachineSound *msound) { return ((struct SN76477interface*)msound->sound_interface)->num; }
    /*TODO*////*TODO*///#endif

    /*TODO*////*TODO*///#if (HAS_MSM5205)
    /*TODO*////*TODO*///int MSM5205_clock(const struct MachineSound *msound) { return ((struct MSM5205interface*)msound->sound_interface)->baseclock; }
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_UPD7759)
    /*TODO*////*TODO*///int UPD7759_clock(const struct MachineSound *msound) { return ((struct UPD7759_interface*)msound->sound_interface)->clock_rate; }
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_ASTROCADE)
    /*TODO*////*TODO*///int ASTROCADE_clock(const struct MachineSound *msound) { return ((struct astrocade_interface*)msound->sound_interface)->baseclock; }
    /*TODO*////*TODO*///int ASTROCADE_num(const struct MachineSound *msound) { return ((struct astrocade_interface*)msound->sound_interface)->num; }
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_K051649)
    /*TODO*////*TODO*///int K051649_clock(const struct MachineSound *msound) { return ((struct k051649_interface*)msound->sound_interface)->master_clock; }
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_K053260)
    /*TODO*////*TODO*///int K053260_clock(const struct MachineSound *msound) { return ((struct K053260_interface*)msound->sound_interface)->clock; }
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_CEM3394)
    /*TODO*////*TODO*///int cem3394_num(const struct MachineSound *msound) { return ((struct cem3394_interface*)msound->sound_interface)->numchips; }
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_QSOUND)
    /*TODO*////*TODO*///int qsound_clock(const struct MachineSound *msound) { return ((struct QSound_interface*)msound->sound_interface)->clock; }
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_SPEAKER)
    /*TODO*////*TODO*///int speaker_num(const struct MachineSound *msound) { return ((struct Speaker_interface*)msound->sound_interface)->num; }
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_WAVE)
    /*TODO*////*TODO*///int wave_num(const struct MachineSound *msound) { return ((struct Wave_interface*)msound->sound_interface)->num; }
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///
    static snd_interface sndintf[] =
    {
          new Dummy_snd(),   
          new CustomSound(), 
          new samples(),
          new dac(),
          new ay8910(),   
   /*TEMPHACK*/   new Dummy_snd(),
    /*TODO*////*TODO*///#if (HAS_YM2203)
    /*TODO*////*TODO*///    {
    /*TODO*////*TODO*///		SOUND_YM2203,
    /*TODO*////*TODO*///		"YM-2203",
    /*TODO*////*TODO*///		YM2203_num,
    /*TODO*////*TODO*///		YM2203_clock,
    /*TODO*////*TODO*///		YM2203_sh_start,
    /*TODO*////*TODO*///		YM2203_sh_stop,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		YM2203_sh_reset
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
    /*TEMPHACK*/   new Dummy_snd(),
    /*TODO*////*TODO*///#if (HAS_YM2151)
    /*TODO*////*TODO*///    {
    /*TODO*////*TODO*///		SOUND_YM2151,
    /*TODO*////*TODO*///		"YM-2151",
    /*TODO*////*TODO*///		YM2151_num,
    /*TODO*////*TODO*///		YM2151_clock,
    /*TODO*////*TODO*///		YM2151_sh_start,
    /*TODO*////*TODO*///		YM2151_sh_stop,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		YM2151_sh_reset
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_YM2151_ALT)
    /*TODO*////*TODO*///    {
    /*TODO*////*TODO*///		SOUND_YM2151,
    /*TODO*////*TODO*///		"YM-2151",
    /*TODO*////*TODO*///		YM2151_num,
    /*TODO*////*TODO*///		YM2151_clock,
    /*TODO*////*TODO*///		YM2151_ALT_sh_start,
    /*TODO*////*TODO*///		YM2151_sh_stop,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		0
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
     /*TEMPHACK*/   new Dummy_snd(),
    /*TODO*////*TODO*///#if (HAS_YM2608)
    /*TODO*////*TODO*///    {
    /*TODO*////*TODO*///		SOUND_YM2608,
    /*TODO*////*TODO*///		"YM-2608",
    /*TODO*////*TODO*///		YM2608_num,
    /*TODO*////*TODO*///		YM2608_clock,
    /*TODO*////*TODO*///		YM2608_sh_start,
    /*TODO*////*TODO*///		YM2608_sh_stop,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		YM2608_sh_reset
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
      /*TEMPHACK*/   new Dummy_snd(),
    /*TODO*////*TODO*///#if (HAS_YM2610)
    /*TODO*////*TODO*///    {
    /*TODO*////*TODO*///		SOUND_YM2610,
    /*TODO*////*TODO*///		"YM-2610",
    /*TODO*////*TODO*///		YM2610_num,
    /*TODO*////*TODO*///		YM2610_clock,
    /*TODO*////*TODO*///		YM2610_sh_start,
    /*TODO*////*TODO*///		YM2610_sh_stop,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		YM2610_sh_reset
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
       /*TEMPHACK*/   new Dummy_snd(),
    /*TODO*////*TODO*///#if (HAS_YM2610B)
    /*TODO*////*TODO*///    {
    /*TODO*////*TODO*///		SOUND_YM2610B,
    /*TODO*////*TODO*///		"YM-2610B",
    /*TODO*////*TODO*///		YM2610_num,
    /*TODO*////*TODO*///		YM2610_clock,
    /*TODO*////*TODO*///		YM2610B_sh_start,
    /*TODO*////*TODO*///		YM2610_sh_stop,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		YM2610_sh_reset
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
        /*TEMPHACK*/   new Dummy_snd(),
    /*TODO*////*TODO*///#if (HAS_YM2612)
    /*TODO*////*TODO*///    {
    /*TODO*////*TODO*///		SOUND_YM2612,
    /*TODO*////*TODO*///		"YM-2612",
    /*TODO*////*TODO*///		YM2612_num,
    /*TODO*////*TODO*///		YM2612_clock,
    /*TODO*////*TODO*///		YM2612_sh_start,
    /*TODO*////*TODO*///		YM2612_sh_stop,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		YM2612_sh_reset
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
         /*TEMPHACK*/   new Dummy_snd(),
    /*TODO*////*TODO*///#if (HAS_YM3438)
    /*TODO*////*TODO*///    {
    /*TODO*////*TODO*///		SOUND_YM3438,
    /*TODO*////*TODO*///		"YM-3438",
    /*TODO*////*TODO*///		YM2612_num,
    /*TODO*////*TODO*///		YM2612_clock,
    /*TODO*////*TODO*///		YM2612_sh_start,
    /*TODO*////*TODO*///		YM2612_sh_stop,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		YM2612_sh_reset
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
          /*TEMPHACK*/   new Dummy_snd(),
    /*TODO*////*TODO*///#if (HAS_YM2413)
    /*TODO*////*TODO*///    {
    /*TODO*////*TODO*///		SOUND_YM2413,
    /*TODO*////*TODO*///		"YM-2413",
    /*TODO*////*TODO*///		YM2413_num,
    /*TODO*////*TODO*///		YM2413_clock,
    /*TODO*////*TODO*///		YM2413_sh_start,
    /*TODO*////*TODO*///		YM2413_sh_stop,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		0
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
              new _3812intf(),
              new _3526intf(),
              new y8950(),

              /*TEMPHACK*/   new Dummy_snd(),
    /*TODO*////*TODO*///#if (HAS_SN76477)
    /*TODO*////*TODO*///    {
    /*TODO*////*TODO*///		SOUND_SN76477,
    /*TODO*////*TODO*///		"SN76477",
    /*TODO*////*TODO*///		SN76477_num,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		SN76477_sh_start,
    /*TODO*////*TODO*///		SN76477_sh_stop,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		0
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
          new sn76496(),

                /*TEMPHACK*/   new Dummy_snd(),
    /*TODO*////*TODO*///#if (HAS_POKEY)
    /*TODO*////*TODO*///    {
    /*TODO*////*TODO*///		SOUND_POKEY,
    /*TODO*////*TODO*///		"Pokey",
    /*TODO*////*TODO*///		POKEY_num,
    /*TODO*////*TODO*///		POKEY_clock,
    /*TODO*////*TODO*///		pokey_sh_start,
    /*TODO*////*TODO*///		pokey_sh_stop,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		0
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
                 /*TEMPHACK*/   new Dummy_snd(),
    /*TODO*////*TODO*///#if (HAS_TIA)
    /*TODO*////*TODO*///    {
    /*TODO*////*TODO*///		SOUND_TIA,
    /*TODO*////*TODO*///		"TIA",
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		TIA_clock,
    /*TODO*////*TODO*///		tia_sh_start,
    /*TODO*////*TODO*///		tia_sh_stop,
    /*TODO*////*TODO*///		tia_sh_update,
    /*TODO*////*TODO*///		0
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
                  /*TEMPHACK*/   new Dummy_snd(),
    /*TODO*////*TODO*///#if (HAS_NES)
    /*TODO*////*TODO*///    {
    /*TODO*////*TODO*///		SOUND_NES,
    /*TODO*////*TODO*///		"Nintendo",
    /*TODO*////*TODO*///		NES_num,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		NESPSG_sh_start,
    /*TODO*////*TODO*///		NESPSG_sh_stop,
    /*TODO*////*TODO*///		NESPSG_sh_update,
    /*TODO*////*TODO*///		0
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
                   /*TEMPHACK*/   new Dummy_snd(),
    /*TODO*////*TODO*///#if (HAS_ASTROCADE)
    /*TODO*////*TODO*///    {
    /*TODO*////*TODO*///		SOUND_ASTROCADE,
    /*TODO*////*TODO*///		"Astrocade",
    /*TODO*////*TODO*///		ASTROCADE_num,
    /*TODO*////*TODO*///		ASTROCADE_clock,
    /*TODO*////*TODO*///		astrocade_sh_start,
    /*TODO*////*TODO*///		astrocade_sh_stop,
    /*TODO*////*TODO*///		astrocade_sh_update,
    /*TODO*////*TODO*///		0
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
           new namco()       
    /*TODO*////*TODO*///#if (HAS_TMS36XX)
    /*TODO*////*TODO*///    {
    /*TODO*////*TODO*///		SOUND_TMS36XX,
    /*TODO*////*TODO*///		"TMS36XX",
    /*TODO*////*TODO*///		TMS36XX_num,
    /*TODO*////*TODO*///        0,
    /*TODO*////*TODO*///		tms36xx_sh_start,
    /*TODO*////*TODO*///		tms36xx_sh_stop,
    /*TODO*////*TODO*///		tms36xx_sh_update,
    /*TODO*////*TODO*///		0
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_TMS5220)
    /*TODO*////*TODO*///    {
    /*TODO*////*TODO*///		SOUND_TMS5220,
    /*TODO*////*TODO*///		"TMS5520",
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		TMS5220_clock,
    /*TODO*////*TODO*///		tms5220_sh_start,
    /*TODO*////*TODO*///		tms5220_sh_stop,
    /*TODO*////*TODO*///		tms5220_sh_update,
    /*TODO*////*TODO*///		0
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_VLM5030)
    /*TODO*////*TODO*///    {
    /*TODO*////*TODO*///		SOUND_VLM5030,
    /*TODO*////*TODO*///		"VLM5030",
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		VLM5030_clock,
    /*TODO*////*TODO*///		VLM5030_sh_start,
    /*TODO*////*TODO*///		VLM5030_sh_stop,
    /*TODO*////*TODO*///		VLM5030_sh_update,
    /*TODO*////*TODO*///		0
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_ADPCM)
    /*TODO*////*TODO*///    {
    /*TODO*////*TODO*///		SOUND_ADPCM,
    /*TODO*////*TODO*///		"ADPCM",
    /*TODO*////*TODO*///		ADPCM_num,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		ADPCM_sh_start,
    /*TODO*////*TODO*///		ADPCM_sh_stop,
    /*TODO*////*TODO*///		ADPCM_sh_update,
    /*TODO*////*TODO*///		0
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_OKIM6295)
    /*TODO*////*TODO*///    {
    /*TODO*////*TODO*///		SOUND_OKIM6295,
    /*TODO*////*TODO*///		"OKI6295",
    /*TODO*////*TODO*///		OKIM6295_num,
    /*TODO*////*TODO*///		OKIM6295_clock,
    /*TODO*////*TODO*///		OKIM6295_sh_start,
    /*TODO*////*TODO*///		OKIM6295_sh_stop,
    /*TODO*////*TODO*///		OKIM6295_sh_update,
    /*TODO*////*TODO*///		0
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_MSM5205)
    /*TODO*////*TODO*///    {
    /*TODO*////*TODO*///		SOUND_MSM5205,
    /*TODO*////*TODO*///		"MSM5205",
    /*TODO*////*TODO*///		MSM5205_num,
    /*TODO*////*TODO*///		MSM5205_clock,
    /*TODO*////*TODO*///		MSM5205_sh_start,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		MSM5205_sh_reset,
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_UPD7759)
    /*TODO*////*TODO*///    {
    /*TODO*////*TODO*///		SOUND_UPD7759,
    /*TODO*////*TODO*///		"uPD7759",
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		UPD7759_clock,
    /*TODO*////*TODO*///		UPD7759_sh_start,
    /*TODO*////*TODO*///		UPD7759_sh_stop,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		0
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_HC55516)
    /*TODO*////*TODO*///    {
    /*TODO*////*TODO*///		SOUND_HC55516,
    /*TODO*////*TODO*///		"HC55516",
    /*TODO*////*TODO*///		HC55516_num,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		hc55516_sh_start,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		0
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_K005289)
    /*TODO*////*TODO*///    {
    /*TODO*////*TODO*///		SOUND_K005289,
    /*TODO*////*TODO*///		"005289",
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		K005289_sh_start,
    /*TODO*////*TODO*///		K005289_sh_stop,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		0
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_K007232)
    /*TODO*////*TODO*///    {
    /*TODO*////*TODO*///		SOUND_K007232,
    /*TODO*////*TODO*///		"007232",
    /*TODO*////*TODO*///		K007232_num,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		K007232_sh_start,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		0
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_K051649)
    /*TODO*////*TODO*///    {
    /*TODO*////*TODO*///		SOUND_K051649,
    /*TODO*////*TODO*///		"051649",
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		K051649_clock,
    /*TODO*////*TODO*///		K051649_sh_start,
    /*TODO*////*TODO*///		K051649_sh_stop,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		0
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_K053260)
    /*TODO*////*TODO*///    {
    /*TODO*////*TODO*///		SOUND_K053260,
    /*TODO*////*TODO*///		"053260",
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		K053260_clock,
    /*TODO*////*TODO*///		K053260_sh_start,
    /*TODO*////*TODO*///		K053260_sh_stop,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		0
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_SEGAPCM)
    /*TODO*////*TODO*///	{
    /*TODO*////*TODO*///		SOUND_SEGAPCM,
    /*TODO*////*TODO*///		"Sega PCM",
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		SEGAPCM_sh_start,
    /*TODO*////*TODO*///		SEGAPCM_sh_stop,
    /*TODO*////*TODO*///		SEGAPCM_sh_update,
    /*TODO*////*TODO*///		0
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_RF5C68)
    /*TODO*////*TODO*///	{
    /*TODO*////*TODO*///		SOUND_RF5C68,
    /*TODO*////*TODO*///		"RF5C68",
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		RF5C68_sh_start,
    /*TODO*////*TODO*///		RF5C68_sh_stop,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		0
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_CEM3394)
    /*TODO*////*TODO*///	{
    /*TODO*////*TODO*///		SOUND_CEM3394,
    /*TODO*////*TODO*///		"CEM3394",
    /*TODO*////*TODO*///		cem3394_num,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		cem3394_sh_start,
    /*TODO*////*TODO*///		cem3394_sh_stop,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		0
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_C140)
    /*TODO*////*TODO*///	{
    /*TODO*////*TODO*///		SOUND_C140,
    /*TODO*////*TODO*///		"C140",
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		C140_sh_start,
    /*TODO*////*TODO*///		C140_sh_stop,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		0
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_QSOUND)
    /*TODO*////*TODO*///	{
    /*TODO*////*TODO*///		SOUND_QSOUND,
    /*TODO*////*TODO*///		"QSound",
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		qsound_clock,
    /*TODO*////*TODO*///		qsound_sh_start,
    /*TODO*////*TODO*///		qsound_sh_stop,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		0
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_SPEAKER)
    /*TODO*////*TODO*///	{
    /*TODO*////*TODO*///		SOUND_SPEAKER,
    /*TODO*////*TODO*///		"Speaker",
    /*TODO*////*TODO*///		speaker_num,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		speaker_sh_start,
    /*TODO*////*TODO*///		speaker_sh_stop,
    /*TODO*////*TODO*///		speaker_sh_update,
    /*TODO*////*TODO*///		0
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
    /*TODO*////*TODO*///#if (HAS_WAVE)
    /*TODO*////*TODO*///	{
    /*TODO*////*TODO*///		SOUND_WAVE,
    /*TODO*////*TODO*///		"Cassette",
    /*TODO*////*TODO*///		wave_num,
    /*TODO*////*TODO*///		0,
    /*TODO*////*TODO*///		wave_sh_start,
    /*TODO*////*TODO*///		wave_sh_stop,
    /*TODO*////*TODO*///		wave_sh_update,
    /*TODO*////*TODO*///		0
    /*TODO*////*TODO*///	},
    /*TODO*////*TODO*///#endif
    };
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///int sound_start(void)
    /*TODO*////*TODO*///{
    /*TODO*////*TODO*///	int totalsound = 0;
    /*TODO*////*TODO*///	int i;
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///	/* Verify the order of entries in the sndintf[] array */
    /*TODO*////*TODO*///	for (i = 0;i < SOUND_COUNT;i++)
    /*TODO*////*TODO*///	{
    /*TODO*////*TODO*///		if (sndintf[i].sound_num != i)
    /*TODO*////*TODO*///		{
    /*TODO*////*TODO*///if (errorlog) fprintf(errorlog,"Sound #%d wrong ID %d: check enum SOUND_... in src/sndintrf.h!\n",i,sndintf[i].sound_num);
    /*TODO*////*TODO*///			return 1;
    /*TODO*////*TODO*///		}
    /*TODO*////*TODO*///	}
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///	/* samples will be read later if needed */
    /*TODO*////*TODO*///	Machine->samples = 0;
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///	refresh_period = TIME_IN_HZ(Machine->drv->frames_per_second);
    /*TODO*////*TODO*///	refresh_period_inv = 1.0 / refresh_period;
    /*TODO*////*TODO*///	sound_update_timer = timer_set(TIME_NEVER,0,NULL);
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///	if (mixer_sh_start() != 0)
    /*TODO*////*TODO*///		return 1;
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///	if (streams_sh_start() != 0)
    /*TODO*////*TODO*///		return 1;
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///	while (Machine->drv->sound[totalsound].sound_type != 0 && totalsound < MAX_SOUND)
    /*TODO*////*TODO*///	{
    /*TODO*////*TODO*///		if ((*sndintf[Machine->drv->sound[totalsound].sound_type].start)(&Machine->drv->sound[totalsound]) != 0)
    /*TODO*////*TODO*///			goto getout;
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///		totalsound++;
    /*TODO*////*TODO*///	}
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///	return 0;
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///getout:
    /*TODO*////*TODO*///	/* TODO: should also free the resources allocated before */
    /*TODO*////*TODO*///	return 1;
    /*TODO*////*TODO*///}
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///void sound_stop(void)
    /*TODO*////*TODO*///{
    /*TODO*////*TODO*///	int totalsound = 0;
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///	while (Machine->drv->sound[totalsound].sound_type != 0 && totalsound < MAX_SOUND)
    /*TODO*////*TODO*///	{
    /*TODO*////*TODO*///		if (sndintf[Machine->drv->sound[totalsound].sound_type].stop)
    /*TODO*////*TODO*///			(*sndintf[Machine->drv->sound[totalsound].sound_type].stop)();
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///		totalsound++;
    /*TODO*////*TODO*///	}
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///	streams_sh_stop();
    /*TODO*////*TODO*///	mixer_sh_stop();
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///	if (sound_update_timer)
    /*TODO*////*TODO*///	{
    /*TODO*////*TODO*///		timer_remove(sound_update_timer);
    /*TODO*////*TODO*///		sound_update_timer = 0;
    /*TODO*////*TODO*///	}
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///	/* free audio samples */
    /*TODO*////*TODO*///	freesamples(Machine->samples);
    /*TODO*////*TODO*///	Machine->samples = 0;
    /*TODO*////*TODO*///}
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///void sound_update(void)
    /*TODO*////*TODO*///{
    /*TODO*////*TODO*///	int totalsound = 0;
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///	profiler_mark(PROFILER_SOUND);
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///	while (Machine->drv->sound[totalsound].sound_type != 0 && totalsound < MAX_SOUND)
    /*TODO*////*TODO*///	{
    /*TODO*////*TODO*///		if (sndintf[Machine->drv->sound[totalsound].sound_type].update)
    /*TODO*////*TODO*///			(*sndintf[Machine->drv->sound[totalsound].sound_type].update)();
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///		totalsound++;
    /*TODO*////*TODO*///	}
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///	streams_sh_update();
    /*TODO*////*TODO*///	mixer_sh_update();
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///	timer_reset(sound_update_timer,TIME_NEVER);
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///	profiler_mark(PROFILER_END);
    /*TODO*////*TODO*///}
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///void sound_reset(void)
    /*TODO*////*TODO*///{
    /*TODO*////*TODO*///	int totalsound = 0;
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///	while (Machine->drv->sound[totalsound].sound_type != 0 && totalsound < MAX_SOUND)
    /*TODO*////*TODO*///	{
    /*TODO*////*TODO*///		if (sndintf[Machine->drv->sound[totalsound].sound_type].reset)
    /*TODO*////*TODO*///			(*sndintf[Machine->drv->sound[totalsound].sound_type].reset)();
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///		totalsound++;
    /*TODO*////*TODO*///	}
    /*TODO*////*TODO*///}
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///
    /*TODO*////*TODO*///
    public static String sound_name(MachineSound msound)
    {
    	 if(msound.sound_type < SOUND_COUNT)
    		return sndintf[msound.sound_type].name;
    	else
    		return "";
    }
    
    public static int sound_num(MachineSound msound)
    {
       if(msound.sound_type < SOUND_COUNT && sndintf[msound.sound_type].chips_num(msound)!=0)
       {
             return sndintf[msound.sound_type].chips_num(msound);
       }
       else
       {
             return 0;
       }
    }
    public static int sound_clock(MachineSound msound)
    {
    	if(msound.sound_type < SOUND_COUNT && sndintf[msound.sound_type].chips_clock(msound)!=0)
        {
    		return sndintf[msound.sound_type].chips_clock(msound);
        }
    	else
        {
            return 0;
        }  		
    }

    /*TODO*////*TODO*///int sound_scalebufferpos(int value)
    /*TODO*////*TODO*///{
    /*TODO*////*TODO*///	int result = (int)((double)value * timer_timeelapsed (sound_update_timer) * refresh_period_inv);
    /*TODO*////*TODO*///	if (value >= 0) return (result < value) ? result : value;
    /*TODO*////*TODO*///	else return (result > value) ? result : value;
    /*TODO*////*TODO*///}
    /*TODO*////*TODO
    
}
