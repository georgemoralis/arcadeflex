#include "driver.h"



void congo_daio(int offset, int data)
{
	if (offset == 1)
	{
		if (data & 2) sample_start(0,0,0);
	}
	else if (offset == 2)
	{
		data ^= 0xff;

		if (data & 0x80)
		{
			if (data & 8) sample_start(1,1,0);
			if (data & 4) sample_start(2,2,0);
			if (data & 2) sample_start(3,3,0);
			if (data & 1) sample_start(4,4,0);
		}
	}
}
