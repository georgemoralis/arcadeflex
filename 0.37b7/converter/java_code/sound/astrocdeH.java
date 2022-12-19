
#ifndef ASTROCADE_H
#define ASTROCADE_H

#define MAX_ASTROCADE_CHIPS 2   /* max number of emulated chips */

struct astrocade_interface
{
	int num;			/* total number of sound chips in the machine */
	int baseclock;			/* astrocade clock rate  */
	int volume[MAX_ASTROCADE_CHIPS];			/* master volume */
};



#endif
