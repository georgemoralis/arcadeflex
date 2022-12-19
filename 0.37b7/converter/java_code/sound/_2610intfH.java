#ifndef __2610INTF_H__
#define __2610INTF_H__

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package sound;

public class _2610intfH
{
	#ifdef BUILD_YM2610
	  void YM2610UpdateRequest(int chip);
	#endif
	
	#define   MAX_2610    (2)
	
	#ifndef VOL_YM3012
	/* #define YM3014_VOL(Vol,Pan) VOL_YM3012((Vol)/2,Pan,(Vol)/2,Pan) */
	#define YM3012_VOL(LVol,LPan,RVol,RPan) (MIXER(LVol,LPan)|(MIXER(RVol,RPan) << 16))
	#endif
	
	struct YM2610interface
	{
		int num;	/* total number of 8910 in the machine */
		int baseclock;
		int volumeSSG[MAX_8910]; /* for SSG sound */
		mem_read_handler portAread[MAX_8910];
		mem_read_handler portBread[MAX_8910];
		mem_write_handler portAwrite[MAX_8910];
		mem_write_handler portBwrite[MAX_8910];
		void ( *handler[MAX_8910] )( int irq );	/* IRQ handler for the YM2610 */
		int pcmromb[MAX_2610];		/* Delta-T rom region */
		int pcmroma[MAX_2610];		/* ADPCM   rom region */
		int volumeFM[MAX_2610];		/* use YM3012_VOL macro */
	};
	
	/************************************************/
	/* Sound Hardware Start							*/
	/************************************************/
	
	/************************************************/
	/* Sound Hardware Stop							*/
	/************************************************/
	
	
	/************************************************/
	/* Chip 0 functions								*/
	/************************************************/
	
	/************************************************/
	/* Chip 1 functions								*/
	/************************************************/
	
	#endif
	/**************** end of file ****************/
}
