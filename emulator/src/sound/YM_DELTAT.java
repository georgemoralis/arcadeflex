package sound;

import static arcadeflex.ptrlib.*;
import static arcadeflex.libc.*;


/**
 *
 * @author shadow
 */
public class YM_DELTAT {
        public YM_DELTAT()
        {
            reg=new int[16];
        }
 	public UBytePtr memory;
	public int memory_size;
	public double freqbase;
	public int[] output_pointer; /* pointer of output pointers */
	public int output_range;

	public /*UINT8*/int[] reg;
	public /*UINT8*/int portstate,portcontrol;
	public int portshift;

	public /*UINT8*/int flag;          /* port state        */
	public /*UINT8*/int flagMask;      /* arrived flag mask */
	public /*UINT8*/int now_data;
	public /*UINT32*/ long now_addr;
	public /*UINT32*/ long now_step;
	public /*UINT32*/ long step;
	public /*UINT32*/ long start;
	public /*UINT32*/ long end;
	public /*UINT32*/ long delta;
	public int volume;
	public IntSubArray pan;        /* &output_pointer[pan] */
	public int /*adpcmm,*/ adpcmx, adpcmd;
	public int adpcml;			/* hiro-shi!! */

	/* leveling and re-sampling state for DELTA-T */
	public int volume_w_step;   /* volume with step rate */
	public int next_leveling;   /* leveling value        */
	public int sample_step;     /* step of re-sampling   */

	public /*UINT8*/int arrivedFlag;    /* flag of arrived end address */   
}
