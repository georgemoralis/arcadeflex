#ifndef saa1099_h
#define saa1099_h

/**********************************************
	Philips SAA1099 Sound driver
**********************************************/

#define MAX_SAA1099 1

/* interface */
struct SAA1099_interface
{
	int numchips;						/* number of chips */
	int volume[MAX_SAA1099][2];			/* playback volume */	
};




#endif
