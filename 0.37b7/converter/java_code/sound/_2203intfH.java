#ifndef YM2203INTF_H
#define YM2203INTF_H

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package sound;

public class _2203intfH
{
	
	#define MAX_2203 4
	
	#define YM2203interface AY8910interface
	
	/* volume level for YM2203 */
	#define YM2203_VOL(FM_VOLUME,SSG_VOLUME) (((FM_VOLUME)<<16)+(SSG_VOLUME))
	
	
	
	
	
	
	void YM2203UpdateRequest(int chip);
	
	#endif
}
