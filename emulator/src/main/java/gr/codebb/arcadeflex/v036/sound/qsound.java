package gr.codebb.arcadeflex.v036.sound;

import gr.codebb.arcadeflex.common.PtrLib;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.sound.qsoundH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.sound.streams.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;
import static gr.codebb.arcadeflex.v037b7.mame.timer.*;
import static gr.codebb.arcadeflex.v037b7.mame.timerH.*;

public class qsound extends snd_interface {

    public qsound() {
        this.sound_num = SOUND_QSOUND;
        this.name = "QSound";
    }

    @Override
    public int chips_num(MachineSound msound) {
        return 0;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return ((QSound_interface) msound.sound_interface).clock;
    }
    
    static QSound_interface intf;	/* Interface  */
    static UBytePtr qsound_sample_rom;
    static int qsound_stream;				/* Audio stream */
    
    static StreamInitMultiPtr qsound_update=new StreamInitMultiPtr() {
        @Override
        public void handler(int offset, ShortPtr[] buffer, int length) {
                //int i,j;
		//int rvol, lvol, count;
                //int pC_index=0;
		
		//UBytePtr pST;
		//ShortPtr[]  datap=new ShortPtr[2];
	
		if (Machine.sample_rate == 0) return;
	
		int i, j;
            int rvol, lvol, count;
            for (i = 0; i < length; i++)
            {
                buffer[0].write(offset + i, (short) 0);
                buffer[1].write(offset + i, (short) 0);
            }
            for (i = 0; i < 16; i++)
            {
                if (qsound_channel[i]==null)
                    qsound_channel[i] = new QSOUND_CHANNEL();
                
                if (qsound_channel[i].key != 0)
                {
                    rvol = (qsound_channel[i].rvol * qsound_channel[i].vol) >> 8;
                    lvol = (qsound_channel[i].lvol * qsound_channel[i].vol) >> 8;
                    for (j = 0; j < length; j++)
                    {
                        count = (qsound_channel[i].offset) >> 16;
                        qsound_channel[i].offset &= 0xffff;
                        if (count != 0)
                        {
                            qsound_channel[i].address += count;
                            if (qsound_channel[i].address >= qsound_channel[i].end)
                            {
                                if (qsound_channel[i].loop == 0)
                                {
                                    /* Reached the end of a non-looped sample */
                                    qsound_channel[i].key = 0;
                                    break;
                                }
                                /* Reached the end, restart the loop */
                                qsound_channel[i].address = (qsound_channel[i].end - qsound_channel[i].loop) & 0xffff;
                            }
                            qsound_channel[i].lastdt = (new UBytePtr(qsound_sample_rom, qsound_channel[i].bank)).read(qsound_channel[i].address);

                        }
                        buffer[0].write(offset + j, (short) (buffer[0].read(offset + j) + ((qsound_channel[i].lastdt * lvol) >> 6)));
                        buffer[1].write(offset + j, (short) (buffer[1].read(offset + j) + ((qsound_channel[i].lastdt * rvol) >> 6)));
                        //buffer[1][offset + j] += ((qsound_channel[i].lastdt * rvol) >> 6);
                        qsound_channel[i].offset += qsound_channel[i].pitch;
                    }
                }
            }
	
/*TODO*///	#if LOG_WAVE
/*TODO*///		fwrite(datap[0], length*sizeof(QSOUND_SAMPLE), 1, fpRawDataL);
/*TODO*///		fwrite(datap[1], length*sizeof(QSOUND_SAMPLE), 1, fpRawDataR);
/*TODO*///	#endif
        }
    };
	
    public static ShStartPtr qsound_sh_start = new ShStartPtr() { public int handler(MachineSound msound) 
	{
		int i;
	
		if (Machine.sample_rate == 0) return 0;
	
		intf = (QSound_interface) msound.sound_interface;
	
		qsound_sample_rom = new UBytePtr(memory_region(intf.region));
	
/*TODO*///		memset(qsound_channel, 0, sizeof(qsound_channel));
	
/*TODO*///	#if QSOUND_DRIVER1
		qsound_frq_ratio = ((float)intf.clock / (float)QSOUND_CLOCKDIV) /
							(float) Machine.sample_rate;
		qsound_frq_ratio *= 16.0;
	
		/* Create pan table */
		for (i=0; i<33; i++)
		{
			qsound_pan_table[i]=(int)((256/Math.sqrt(32)) * Math.sqrt(i));
		}
/*TODO*///	#else
/*TODO*///		i=0;
/*TODO*///	#endif
	
/*TODO*///	#if LOG_QSOUND
/*TODO*///		logerror("Pan table\n");
/*TODO*///		for (i=0; i<33; i++)
/*TODO*///			logerror("%02x ", qsound_pan_table[i]);
/*TODO*///	#endif
/*TODO*///		{
			/* Allocate stream */
	int CHANNELS = 2 ;
			String[] buf=new String[CHANNELS];
			String[] name=new String[CHANNELS];
			int[]  vol=new int[2];
			name[0] = buf[0];
			name[1] = buf[1];
			buf[0]=sprintf( "%s L", sound_name(msound) );
			buf[1]=sprintf( "%s R", sound_name(msound) );
			vol[0]=MIXER(intf.mixing_level[0], MIXER_PAN_LEFT);
			vol[1]=MIXER(intf.mixing_level[1], MIXER_PAN_RIGHT);
			qsound_stream = stream_init_multi(
				CHANNELS,
				name,
				vol,
				Machine.sample_rate,
				0,
				qsound_update );
/*TODO*///		}
	
/*TODO*///	#if LOG_WAVE
/*TODO*///		fpRawDataR=fopen("qsoundr.raw", "w+b");
/*TODO*///		fpRawDataL=fopen("qsoundl.raw", "w+b");
/*TODO*///		if (!fpRawDataR || !fpRawDataL)
/*TODO*///		{
/*TODO*///			return 1;
/*TODO*///		}
/*TODO*///	#endif
	
		return 0;
	} };
	
	public static ShStopPtr qsound_sh_stop = new ShStopPtr() { public void handler() 
	{
/*TODO*///		if (Machine.sample_rate == 0) return;
/*TODO*///	#if LOG_WAVE
/*TODO*///		if (fpRawDataR != 0)
/*TODO*///		{
/*TODO*///			fclose(fpRawDataR);
/*TODO*///		}
/*TODO*///		if (fpRawDataL != 0)
/*TODO*///		{
/*TODO*///			fclose(fpRawDataL);
/*TODO*///		}
/*TODO*///	#endif
	} };
	

    @Override
    public int start(MachineSound msound) {
        return qsound_sh_start.handler(msound);
    }

    @Override
    public void stop() {
        if (Machine.sample_rate == 0) {
            return;
        }
    }

    @Override
    public void update() {
        //no functionality expected
    }

    @Override
    public void reset() {
        //no functionality expected
    }
    
    public static final int QSOUND_CHANNELS = 16;
    public static final int QSOUND_CLOCKDIV = 166;			 /* Clock divider */
    /*TODO*///typedef signed char QSOUND_SRC_SAMPLE;
    public static final int LENGTH_DIV = 1;
    static int[] qsound_pan_table=new int[33];		 /* Pan volume table */
    static float qsound_frq_ratio;		   /* Frequency ratio */
    
    public static class QSOUND_CHANNEL
	{
		public int bank;	   /* bank (x16)	*/
		public int address;	/* start address */
		public int pitch;	  /* pitch */
		public int reg3;	   /* unknown (always 0x8000) */
		public int loop;	   /* loop address */
		public int end;		/* end address */
		public int vol;		/* master volume */
		public int pan;		/* Pan value */
		public int reg9;	   /* unknown */
	
		/* Work variables */
		public int key;		/* Key on / key off */
	
/*TODO*///	#if QSOUND_DRIVER1
		public int lvol;	   /* left volume */
		public int rvol;	   /* right volume */
		public int lastdt;	 /* last sample value */
		public int offset;	 /* current offset counter */
/*TODO*///	#else
/*TODO*///		QSOUND_SRC_SAMPLE *buffer;
/*TODO*///		int factor;		   /*step factor (fixed point 8-bit)*/
/*TODO*///		int mixl,mixr;		/*mixing factor (fixed point)*/
/*TODO*///		int cursor;		   /*current sample position (fixed point)*/
/*TODO*///		int lpos;			 /*last cursor pos*/
/*TODO*///		int lastsaml;		 /*last left sample (to avoid any calculation)*/
/*TODO*///		int lastsamr;		 /*last right sample*/
/*TODO*///	#endif
	};
	
    
    static int qsound_data;
    static QSOUND_CHANNEL[] qsound_channel=new QSOUND_CHANNEL[QSOUND_CHANNELS];
    
    public static void qsound_set_command(int data, int value)
	{
		int ch=0,reg=0;
		if (data < 0x80)
		{
			ch=data>>3;
			reg=data & 0x07;
		}
		else
		{
			if (data < 0x90)
			{
				ch=data-0x80;
				reg=8;
			}
			else
			{
				if (data >= 0xba && data < 0xca)
				{
					ch=data-0xba;
					reg=9;
				}
				else
				{
					/* Unknown registers */
					ch=99;
					reg=99;
				}
			}
		}
                
                if ((ch<QSOUND_CHANNELS)&&(qsound_channel[ch] == null))
                    qsound_channel[ch] = new QSOUND_CHANNEL();
	
		switch (reg)
		{
			case 0: /* Bank */
				ch=(ch+1)&0x0f;	/* strange ... */
				qsound_channel[ch].bank /= LENGTH_DIV;
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///				if (!value & 0x8000)
/*TODO*///				{
/*TODO*///					char baf[40];
/*TODO*///					sprintf(baf,"Register3=%04x",value);
/*TODO*///					usrintf_showmessage(baf);
/*TODO*///				}
/*TODO*///	#endif
	
				break;
			case 1: /* start */
				qsound_channel[ch].address=value;
				qsound_channel[ch].address/=LENGTH_DIV;
				break;
			case 2: /* pitch */
/*TODO*///	#if QSOUND_DRIVER1
				qsound_channel[ch].pitch=(int)
                                        (long)
                                        ((float)value * qsound_frq_ratio );
				qsound_channel[ch].pitch/=LENGTH_DIV;
/*TODO*///	#else
/*TODO*///				qsound_channel[ch].factor=((float) (value*(6/LENGTH_DIV)) /
/*TODO*///										  (float) Machine.sample_rate)*256.0;
/*TODO*///	
/*TODO*///	#endif
				if (value==0)
				{
					/* Key off */
					qsound_channel[ch].key=0;
				}
				break;
			case 3: /* unknown */
				qsound_channel[ch].reg3=value;
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///				if (value != 0x8000)
/*TODO*///				{
/*TODO*///					char baf[40];
/*TODO*///					sprintf(baf,"Register3=%04x",value);
/*TODO*///					usrintf_showmessage(baf);
/*TODO*///				}
/*TODO*///	#endif
				break;
			case 4: /* loop offset */
				qsound_channel[ch].loop=value/LENGTH_DIV;
				break;
			case 5: /* end */
				qsound_channel[ch].end=value/LENGTH_DIV;
				break;
			case 6: /* master volume */
				if (value==0)
				{
					/* Key off */
					qsound_channel[ch].key=0;
				}
				else if (qsound_channel[ch].key==0)
				{
					/* Key on */
					qsound_channel[ch].key=1;
/*TODO*///	#if QSOUND_DRIVER1
					qsound_channel[ch].offset=0;
					qsound_channel[ch].lastdt=0;
/*TODO*///	#else
/*TODO*///					qsound_channel[ch].cursor=qsound_channel[ch].address <<8 ;
/*TODO*///					qsound_channel[ch].buffer=qsound_sample_rom+
/*TODO*///											 qsound_channel[ch].bank;
/*TODO*///	#endif
				}
				qsound_channel[ch].vol=value;
/*TODO*///	#if QSOUND_DRIVER2
/*TODO*///				calcula_mix(ch);
/*TODO*///	#endif
				break;
	
			case 7:  /* unused */
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///				{
/*TODO*///					char baf[40];
/*TODO*///					sprintf(baf,"UNUSED QSOUND REG 7=%04x",value);
/*TODO*///					usrintf_showmessage(baf);
/*TODO*///				}
/*TODO*///	#endif
	
				break;
			case 8:
				{
/*TODO*///	#if QSOUND_DRIVER1
				   int pandata=(value-0x10)&0x3f;
				   if (pandata > 32)
				   {
						pandata=32;
				   }
				   qsound_channel[ch].rvol=qsound_pan_table[pandata];
				   qsound_channel[ch].lvol=qsound_pan_table[32-pandata];
/*TODO*///	#endif
				   qsound_channel[ch].pan = value;
/*TODO*///	#if QSOUND_DRIVER2
/*TODO*///				   calcula_mix(ch);
/*TODO*///	#endif
				}
				break;
			 case 9:
				qsound_channel[ch].reg9=value;
	/*
	#ifdef MAME_DEBUG
				{
					char baf[40];
					sprintf(baf,"QSOUND REG 9=%04x",value);
					usrintf_showmessage(baf);
				}
	#endif
	*/
				break;
		}
/*TODO*///	#if LOG_QSOUND
/*TODO*///		logerror("QSOUND WRITE %02x CH%02d-R%02d =%04x\n", data, ch, reg, value);
/*TODO*///	#endif
	}
    
    public static WriteHandlerPtr qsound_data_h_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            qsound_data=(qsound_data&0xff)|(data<<8);
        }
    };
    public static WriteHandlerPtr qsound_data_l_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            qsound_data=(qsound_data&0xff00)|data;
        }
    };
    public static WriteHandlerPtr qsound_cmd_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            qsound_set_command(data, qsound_data);
        }
    };
    public static ReadHandlerPtr qsound_status_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            /* Port ready bit (0x80 if ready) */
            return 0x80;
        }
    };

}
