
package gr.codebb.arcadeflex.v036.sound;


public class sn76496H 
{
    public static final int MAX_76496= 4;
    public static class SN76496interface
    {
        public SN76496interface(int num,int[] baseclock,int[] volume)
        {
            this.num =num;
            this.baseclock=baseclock;
            this.volume=volume;
        }
        int num;	/* total number of 76496 in the machine */
        int[] baseclock;//[MAX_76496];
        int[] volume;//[MAX_76496];
    };
}
