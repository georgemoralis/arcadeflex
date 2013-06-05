/*
 *  this file should be fully compatible for 0.36
 */
package sound;


public class namcoH {
    public static class namco_interface
    {
        public namco_interface(int sr,int v,int vol,int re,int st)
        {
            samplerate=sr;
            voices=v;
            volume=vol;
            region=re;
            stereo=st;
        }
        public int samplerate;	/* sample rate */
        public int voices;		/* number of voices */
        public int volume;		/* playback volume */
        public int region;		/* memory region; -1 to use RAM (pointed to by namco_wavedata) */
        public int stereo;		/* set to 1 to indicate stereo (e.g., System 1) */
    };    
}
//keep that as reference
//#define mappy_soundregs namco_soundregs
//#define pengo_soundregs namco_soundregs
//#define polepos_soundregs namco_soundregs