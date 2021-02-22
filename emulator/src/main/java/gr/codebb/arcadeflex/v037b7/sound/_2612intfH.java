/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.sound;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import gr.codebb.arcadeflex.v037b7.sound.ay8910H.AY8910interface;

public class _2612intfH
{
/*TODO*///	#ifdef BUILD_YM2612
/*TODO*///	  void YM2612UpdateRequest(int chip);
/*TODO*///	#endif
	
	public static final int   MAX_2612    = (2);
	
	public static class YM2612interface extends AY8910interface {
            
            public YM2612interface(int num, int baseclock, int[] mixing_level, ReadHandlerPtr[] pAr, ReadHandlerPtr[] pBr, WriteHandlerPtr[] pAw, WriteHandlerPtr[] pBw, WriteYmHandlerPtr[] ym_handler) {
                super(num, baseclock, mixing_level, pAr, pBr, pAw, pBw, ym_handler);                
            }

            //without ym2203 handler
            public YM2612interface(int num, int baseclock, int[] mixing_level, ReadHandlerPtr[] pAr, ReadHandlerPtr[] pBr, WriteHandlerPtr[] pAw, WriteHandlerPtr[] pBw) {
                super(num, baseclock, mixing_level, pAr, pBr, pAw, pBw);
                this.YM2203_handler = null;
            }
            
            
        }
	
	
	/************************************************/
	/* Chip 0 functions								*/
	/************************************************/
	
	/************************************************/
	/* Chip 1 functions								*/
	/************************************************/
	
	/**************************************************/
	/*   YM2612 left/right position change (TAITO)    */
	/**************************************************/
	
/*TODO*///	#endif
	/**************** end of file ****************/
}
