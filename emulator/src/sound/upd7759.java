package sound;

import mame.sndintrf;
import static arcadeflex.ptrlib.*;
import static mame.sndintrfH.*;
import static sound.upd7759H.*;
/**
 *
 * @author shadow
 */
public class upd7759 extends sndintrf.snd_interface {
    /* number of samples stuffed into the rom */
    static /*unsigned char*/int numsam;

    /* playback rate for the streams interface */
    /* BASE_CLOCK or a multiple (if oversampling is active) */
    static int emulation_rate;

    static int base_rate;
    /* define the output rate */
    public static final int CLOCK_DIVIDER	=80;
    
    /* signal fall off factor */
    public static final int FALL_OFF(int n){ return	((n)-(((n)+7)/8)); }

    public static final int SIGNAL_BITS 	=15;	/* signal range */
    public static final int SIGNAL_MAX(){ return (0x7fff >> (15-SIGNAL_BITS));}
    public static final int SIGNAL_MIN(){ return -SIGNAL_MAX(); }

    public static final int STEP_MAX		=32;
    public static final int STEP_MIN		=0;

    public static final int DATA_MAX		=512;
    
    public static class UPD7759sample
    {
        public UPD7759sample()
        {
            
        }
        public UPD7759sample(int offset,int length,int freq)
        {
            this.offset=offset;
            this.length=length;
            this.freq=freq;
        }
    	/*unsigned*/ int offset;	/* offset in that region */
	/*unsigned*/ int length;    /* length of the sample */
	/*unsigned*/ int freq;		/* play back freq of sample */
    };
    /* struct describing a single playing ADPCM voice */
    public static class UPD7759voice
    {
            int playing;            /* 1 if we are actively playing */
            UBytePtr base;    /* pointer to the base memory location */
            int mask;               /* mask to keep us within the buffer */
            int sample; 			/* current sample number (sample data in slave mode) */
            int freq;				/* current sample playback freq */
            int count;              /* total samples to play */
            int signal;             /* current ADPCM signal */
            int step;               /* current ADPCM step */
            int counter;			/* sample counter */
            Object timer;			/* timer used in slave mode */
            int[] data=new int[DATA_MAX]; 	/* data array used in slave mode */
            /*unsigned*/int head;			/* head of data array used in slave mode */
            /*unsigned*/int tail;			/* tail of data array used in slave mode */
            /*unsigned*/int available;
    };
    public upd7759() {
        sound_num = SOUND_UPD7759;
        name = "uPD7759";
    }

    @Override
    public int chips_num(MachineSound msound) {
        return 0;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return ((UPD7759_interface) msound.sound_interface).clock_rate;
    }

    @Override
    public int start(MachineSound msound) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /*
     *   Stop emulation of several UPD7759 output streams
     */
    @Override
    public void stop() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void update() {
        //no functionality expected
    }

    @Override
    public void reset() {
        //no functionality expected
    }

}
