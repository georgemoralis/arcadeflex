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
import mame.driverH.*;

public class adpcmH {
    public static final int MAX_ADPCM =8;
    
    /* NOTE: Actual sample data is specified in the sound_prom parameter of the game driver, but
       since the MAME code expects this to be an array of char *'s, we do a small kludge here */
    
    public static class ADPCMsample
    {
    	int num;       /* trigger number (-1 to mark the end) */
    	int offset;    /* offset in that region */
    	int length;    /* length of the sample */
    };
    
    
    /* a generic ADPCM interface, for unknown chips */
    

    public static class ADPCMinterface
    {
        public ADPCMinterface(int num ,int frequency,int region ,WriteHandlerPtr init,int[] mixing_level)
        {
            this.num=num;
            this.frequency=frequency;
            this.region=region;
            //this.init = init;
            this.mixing_level=mixing_level;
        }
        int num;			       /* total number of ADPCM decoders in the machine */
    	int frequency;             /* playback frequency */
    	int region;                /* memory region where the samples come from */
 /*fortest only*/   	WriteHandlerPtr test;//void (*init)(const struct ADPCMinterface *, struct ADPCMsample *, int max); /* initialization function */
    	int[] mixing_level;     /* master volume */
    }; 

}
