package gr.codebb.arcadeflex.v037b7.sound;

public class rf5c68H {
    /*********************************************************/
    /*    ricoh RF5C68(or clone) PCM controller              */
    /*********************************************************/
    
    /******************************************/
    public static final int  RF5C68_PCM_MAX    = (8);

    public static class RF5C68PCM
    {
            int clock;
            int[] env = new int[RF5C68_PCM_MAX];
            int[] pan = new int[RF5C68_PCM_MAX];
            int[] addr = new int[RF5C68_PCM_MAX];
            int[] start = new int[RF5C68_PCM_MAX];
            int[] step = new int[RF5C68_PCM_MAX];
            int[] loop = new int[RF5C68_PCM_MAX];
            int[][] pcmx = new int[2][RF5C68_PCM_MAX];
            int[] flag = new int[RF5C68_PCM_MAX];

            int[] pcmd = new int[RF5C68_PCM_MAX];
            int[] pcma = new int[RF5C68_PCM_MAX];
    };

    public static class RF5C68interface
    {
            int clock;
            int volume;
            
            public RF5C68interface(int clock, int volume) {
                this.clock = clock;
                this.volume = volume;
            }
    };
    
}
