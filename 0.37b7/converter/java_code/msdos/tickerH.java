/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package msdos;

public class tickerH
{
	
	
	typedef INT64 TICKER;
	
	extern TICKER ticks_per_sec;
	
	#define TICKS_PER_SEC ticks_per_sec
	
	TICKER ticker(void);
}
