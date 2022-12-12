#include "driver.h"
#include "cpu/i8039/i8039.h"



void mario_sh_w(int offset,int data)
{
	if (data)
		cpu_set_irq_line(1,0,ASSERT_LINE);
	else
		cpu_set_irq_line(1,0,CLEAR_LINE);
}


/* Mario running sample */
void mario_sh1_w(int offset,int data)
{
	static int last;

	if (last!= data)
	{
		last = data;
                if (data && sample_playing(0) == 0) sample_start (0, 3, 0);
	}
}

/* Luigi running sample */
void mario_sh2_w(int offset,int data)
{
	static int last;

	if (last!= data)
	{
		last = data;
                if (data && sample_playing(1) == 0) sample_start (1, 4, 0);
	}
}

/* Misc samples */
void mario_sh3_w (int offset,int data)
{
	static int state[8];

	/* Don't trigger the sample if it's still playing */
	if (state[offset] == data) return;

	state[offset] = data;
	if (data)
	{
		switch (offset)
		{
			case 2: /* ice */
				sample_start (2, 0, 0);
				break;
			case 6: /* coin */
				sample_start (2, 1, 0);
				break;
			case 7: /* skid */
				sample_start (2, 2, 0);
				break;
		}
	}
}
