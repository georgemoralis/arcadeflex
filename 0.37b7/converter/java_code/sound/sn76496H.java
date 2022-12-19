#ifndef SN76496_H
#define SN76496_H

#define MAX_76496 4

struct SN76496interface
{
	int num;	/* total number of 76496 in the machine */
	int baseclock[MAX_76496];
	int volume[MAX_76496];
};


#endif
