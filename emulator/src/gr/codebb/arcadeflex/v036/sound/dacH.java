/*
 *  this file should be fully compatible for 0.36
 */
package gr.codebb.arcadeflex.v036.sound;


public class dacH {
    public static final int  MAX_DAC = 4;

    public static class DACinterface
    {
        public DACinterface(int num ,int[] mixing_level)
        {
            this.num=num;
            this.mixing_level=mixing_level;
        }
        int num;	/* total number of DACs */
        int mixing_level[];
    };    
}
