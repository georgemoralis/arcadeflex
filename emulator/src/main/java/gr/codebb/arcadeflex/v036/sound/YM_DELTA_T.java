package gr.codebb.arcadeflex.v036.sound;

/**
 *
 * @author shadow
 */
import gr.codebb.arcadeflex.common.SubArrays.IntSubArray;
import static gr.codebb.arcadeflex.v036.platform.ptrlib.*;

public class YM_DELTA_T {

    /*from ymdeltat.h*/
    public static final int YM_DELTAT_SHIFT = (16);

    public static void YM_DELTAT_DECODE_PRESET(YM_DELTAT DELTAT) {
        ym_deltat_memory = DELTAT.memory;
    }

    static UBytePtr ym_deltat_memory;      /* memory pointer */

    /* Forecast to next Forecast (rate = *8) */
    /* 1/8 , 3/8 , 5/8 , 7/8 , 9/8 , 11/8 , 13/8 , 15/8 */
    static int[] ym_deltat_decode_tableB1 = {
        1, 3, 5, 7, 9, 11, 13, 15,
        -1, -3, -5, -7, -9, -11, -13, -15,};
    /* delta to next delta (rate= *64) */
    /* 0.9 , 0.9 , 0.9 , 0.9 , 1.2 , 1.6 , 2.0 , 2.4 */
    static int[] ym_deltat_decode_tableB2 = {
        57, 57, 57, 57, 77, 102, 128, 153,
        57, 57, 57, 57, 77, 102, 128, 153
    };
    /* DELTA-T particle adjuster */
    public static final int YM_DELTAT_DELTA_MAX =(24576);
    public static final int YM_DELTAT_DELTA_MIN =(127);
    public static final int YM_DELTAT_DELTA_DEF =(127);

    public static final int YM_DELTAT_DECODE_RANGE =32768;
    public static final int YM_DELTAT_DECODE_MIN =(-(YM_DELTAT_DECODE_RANGE));
    public static final int YM_DELTAT_DECODE_MAX =((YM_DELTAT_DECODE_RANGE)-1);
    /* DELTA-T-ADPCM write register */
    public static void YM_DELTAT_ADPCM_Write(YM_DELTAT DELTAT, int r, int v) {
	if(r>=0x10) return;
	DELTAT.reg[r] = v&0xFF; /* stock data */

	switch( r ){
	case 0x00:	/* START,REC,MEMDATA,REPEAT,SPOFF,--,--,RESET */
		if(( v&0x80 )!=0){
			DELTAT.portstate = (v&0x90)&0xFF; /* start req,memory mode,repeat flag copy */
			/**** start ADPCM ****/
			DELTAT.volume_w_step = (int)((double)DELTAT.volume * DELTAT.step / (1<<YM_DELTAT_SHIFT));
			DELTAT.now_addr = (DELTAT.start)<<1;
			DELTAT.now_step = (1<<YM_DELTAT_SHIFT)-DELTAT.step;
			/*adpcm.adpcmm   = 0;*/
			DELTAT.adpcmx   = 0;
			DELTAT.adpcml   = 0;
			DELTAT.adpcmd   = YM_DELTAT_DELTA_DEF;
			DELTAT.next_leveling=0;
			DELTAT.flag     = 1; /* start ADPCM */

			if( DELTAT.step==0 )
			{
				DELTAT.flag = 0;
				DELTAT.portstate = 0x00;
			}
			/**** PCM memory check & limit check ****/
			if(DELTAT.memory == null){			// Check memory Mapped
				//Log(LOG_ERR,"YM Delta-T ADPCM rom not mapped\n");
				DELTAT.flag = 0;
				DELTAT.portstate = 0x00;
				//if(errorlog) fprintf(errorlog,"DELTAT memory 0\n");
			}else{
				if( DELTAT.end >= DELTAT.memory_size )
				{		// Check End in Range
					//Log(LOG_ERR,"YM Delta-T ADPCM end out of range: $%08x\n",DELTAT.end);
					DELTAT.end = DELTAT.memory_size - 1;
					//if(errorlog) fprintf(errorlog,"DELTAT end over\n");
				}
				if( DELTAT.start >= DELTAT.memory_size )
				{		// Check Start in Range
					//Log(LOG_ERR,"YM Delta-T ADPCM start out of range: $%08x\n",DELTAT.start);
					DELTAT.flag = 0;
					DELTAT.portstate = 0x00;
					//if(errorlog) fprintf(errorlog,"DELTAT start under\n");
				}
			}
		} else if(( v&0x01 )!=0){
			DELTAT.flag = 0;
			DELTAT.portstate = 0x00;
		}
		break;
	case 0x01:	/* L,R,-,-,SAMPLE,DA/AD,RAMTYPE,ROM */
		DELTAT.portcontrol = v&0xff;
		DELTAT.pan = new IntSubArray(DELTAT.output_pointer, (v >> 6) & 0x03);//&DELTAT.output_pointer[(v>>6)&0x03];
		break;
	case 0x02:	/* Start Address L */
	case 0x03:	/* Start Address H */
		DELTAT.start  = (DELTAT.reg[0x3]*0x0100 | DELTAT.reg[0x2]) << DELTAT.portshift;
		break;
	case 0x04:	/* Stop Address L */
	case 0x05:	/* Stop Address H */
		DELTAT.end    = (DELTAT.reg[0x5]*0x0100 | DELTAT.reg[0x4]) << DELTAT.portshift;
		DELTAT.end   += (1<<DELTAT.portshift) - 1;
		break;
	case 0x06:	/* Prescale L (PCM and Recoard frq) */
	case 0x07:	/* Proscale H */
	case 0x08:	/* ADPCM data */
	  break;
	case 0x09:	/* DELTA-N L (ADPCM Playback Prescaler) */
	case 0x0a:	/* DELTA-N H */
		DELTAT.delta  = (DELTAT.reg[0xa]*0x0100 | DELTAT.reg[0x9]);
		DELTAT.step     = (/*UINT32*/int)((double)(DELTAT.delta*(1<<(YM_DELTAT_SHIFT-16)))*(DELTAT.freqbase));
		DELTAT.volume_w_step = (int)(double)(DELTAT.volume * DELTAT.step / (1<<YM_DELTAT_SHIFT));
		break;
	case 0x0b:	/* Level control (volume , voltage flat) */
		{
			int oldvol = DELTAT.volume;
			DELTAT.volume = (v&0xff)*(DELTAT.output_range/256) / YM_DELTAT_DECODE_RANGE;
			if( oldvol != 0 )
			{
				DELTAT.adpcml      = (int)((double)DELTAT.adpcml      / (double)oldvol * (double)DELTAT.volume);
				DELTAT.sample_step = (int)((double)DELTAT.sample_step / (double)oldvol * (double)DELTAT.volume);
			}
			DELTAT.volume_w_step = (int)((double)DELTAT.volume * (double)DELTAT.step / (double)(1<<YM_DELTAT_SHIFT));
		}
		break;
	}
    }

    public static void YM_DELTAT_ADPCM_Reset(YM_DELTAT DELTAT, int pan) {
        DELTAT.now_addr = 0;
        DELTAT.now_step = 0;
        DELTAT.step = 0;
        DELTAT.start = 0;
        DELTAT.end = 0;
        /* F2610.adpcm[i].delta     = 21866; */
        DELTAT.volume = 0;
        DELTAT.pan = new IntSubArray(DELTAT.output_pointer, pan);
        /* DELTAT.flagMask  = 0; */
        DELTAT.arrivedFlag = 0;
        DELTAT.flag = 0;
        DELTAT.adpcmx = 0;
        DELTAT.adpcmd = 127;
        DELTAT.adpcml = 0;
        /*DELTAT.adpcmm    = 0;*/
        DELTAT.volume_w_step = 0;
        DELTAT.next_leveling = 0;
        DELTAT.portstate = 0;
        /* DELTAT.portshift = 8; */
    }

    /**
     * ** ADPCM B (Delta-T control type) ***
     */
    public static void YM_DELTAT_ADPCM_CALC(YM_DELTAT DELTAT) {
	/*UINT32*/int step;
	int data;
	int old_m;
	int now_leveling;
	int delta_next;

	DELTAT.now_step += DELTAT.step;
	if ( DELTAT.now_step >= (1<<YM_DELTAT_SHIFT) )
	{
		step = (int)(DELTAT.now_step >> YM_DELTAT_SHIFT);
		DELTAT.now_step &= (1<<YM_DELTAT_SHIFT)-1;
		do{
			if ( DELTAT.now_addr > (DELTAT.end<<1) ) {
				if(( DELTAT.portstate&0x10 )!=0){
					/**** repeat start ****/
					DELTAT.now_addr = DELTAT.start<<1;
					DELTAT.adpcmx   = 0;
					DELTAT.adpcmd   = YM_DELTAT_DELTA_DEF;
					DELTAT.next_leveling = 0;
					DELTAT.flag     = 1;
				}else{
					DELTAT.arrivedFlag |= DELTAT.flagMask;
					DELTAT.flag = 0;
					DELTAT.adpcml = 0;
					now_leveling = 0;
					return;
				}
			}
			if(( DELTAT.now_addr&1 )!=0) data = DELTAT.now_data & 0x0f;
			else
			{
				DELTAT.now_data = (ym_deltat_memory.read((int)(DELTAT.now_addr>>1)));
				data = DELTAT.now_data >> 4;
			}
			DELTAT.now_addr++;
			/* shift Measurement value */
			old_m      = DELTAT.adpcmx/*adpcmm*/;
			/* ch.adpcmm = YM_DELTAT_Limit( ch.adpcmx + (decode_tableB3[data] * ch.adpcmd / 8) ,YM_DELTAT_DECODE_MAX, YM_DELTAT_DECODE_MIN ); */
			/* Forecast to next Forecast */
			DELTAT.adpcmx += (ym_deltat_decode_tableB1[data] * DELTAT.adpcmd / 8);
		//	YM_DELTAT_Limit(DELTAT.adpcmx,YM_DELTAT_DECODE_MAX, YM_DELTAT_DECODE_MIN);
                        if ( DELTAT.adpcmx > YM_DELTAT_DECODE_MAX ) DELTAT.adpcmx = YM_DELTAT_DECODE_MAX;			
                        else if ( DELTAT.adpcmx < YM_DELTAT_DECODE_MIN ) DELTAT.adpcmx = YM_DELTAT_DECODE_MIN;
			/* delta to next delta */
			DELTAT.adpcmd = (DELTAT.adpcmd * ym_deltat_decode_tableB2[data] ) / 64;
		//	YM_DELTAT_Limit(DELTAT.adpcmd,YM_DELTAT_DELTA_MAX, YM_DELTAT_DELTA_MIN );
                        if ( DELTAT.adpcmd > YM_DELTAT_DELTA_MAX ) DELTAT.adpcmd = YM_DELTAT_DELTA_MAX;			
                        else if ( DELTAT.adpcmd < YM_DELTAT_DELTA_MIN ) DELTAT.adpcmd = YM_DELTAT_DELTA_MIN;
			/* shift leveling value */
			delta_next        = DELTAT.adpcmx/*adpcmm*/ - old_m;
			now_leveling      = DELTAT.next_leveling;
			DELTAT.next_leveling = old_m + (delta_next / 2);
		}while(--step!=0);
		/* delta step of re-sampling */
		DELTAT.sample_step = (DELTAT.next_leveling - now_leveling) * DELTAT.volume_w_step;
		/* output of start point */
		DELTAT.adpcml  = now_leveling * DELTAT.volume;
		/* adjust to now */
		DELTAT.adpcml += (int)((double)DELTAT.sample_step * ((double)DELTAT.now_step/(double)DELTAT.step));
	}
	DELTAT.adpcml += DELTAT.sample_step;

	/* output for work of output channels (outd[OPNxxxx])*/
	DELTAT.pan.write(DELTAT.pan.read() + DELTAT.adpcml);//*(DELTAT.pan) += DELTAT.adpcml;
    }
}
