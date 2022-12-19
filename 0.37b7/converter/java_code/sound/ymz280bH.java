/**********************************************************************************************
 *
 *   Yamaha YMZ280B driver
 *   by Aaron Giles
 *
 **********************************************************************************************/

#ifndef YMZ280B_H
#define YMZ280B_H

#define MAX_YMZ280B 			2

struct YMZ280Binterface
{
	int num;                  						/* total number of chips */
	int baseclock[MAX_YMZ280B];						/* input clock */
	int region[MAX_YMZ280B];						/* memory region where the sample ROM lives */
	int mixing_level[MAX_YMZ280B];					/* master volume */
	void (*irq_callback[MAX_YMZ280B])(int state);	/* irq callback */
};

void YMZ280B_set_sample_base(int which, void *base);


#endif
