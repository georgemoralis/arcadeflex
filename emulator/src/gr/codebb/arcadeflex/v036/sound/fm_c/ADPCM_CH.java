
package gr.codebb.arcadeflex.v036.sound.fm_c;

import gr.codebb.arcadeflex.v036.platform.libc.IntSubArray;


public class ADPCM_CH {
    public ADPCM_CH()
    {
        
    }
    	public int/*UINT8*/ flag;			/* port state        */
	public int/*UINT8*/ flagMask;		/* arrived flag mask */
	public int/*UINT8*/ now_data;
	public int/*UINT8*/ now_addr;
	public int/*UINT8*/ now_step;
	public int/*UINT8*/ step;
	public int/*UINT8*/ start;
	public int/*UINT8*/ end;
	public int IL;
	public int volume;					/* calcrated mixing level */
	public IntSubArray pan;					/* &out_ch[OPN_xxxx] */    
	public int /*adpcmm,*/ adpcmx, adpcmd;
	public int adpcml;					/* hiro-shi!! */
}
