#include "driver.h"
#include "cpu/i8039/i8039.h"

static int walk = 0; /* used to determine if dkongjr is walking or climbing? */

void dkong_sh_w(int offset,int data)
{
	if (data)
		cpu_cause_interrupt(1,I8039_EXT_INT);
}


void dkong_sh1_w(int offset,int data)
{
	static int state[8];

	if (state[offset] != data)
	{
		if (data)
			sample_start (offset, offset, 0);

		state[offset] = data;
	}
}




void dkongjr_sh_death_w(int offset,int data)
{
	static int death = 0;

	if (death != data)
	{
		if (data)
			sample_stop (7);
			sample_start (6, 4, 0);


		death = data;
	}
}

void dkongjr_sh_drop_w(int offset,int data)
{
	static int drop = 0;

	if (drop != data)
	{


		if (data)
			sample_start (7, 5, 0);

		drop = data;
	}
}

void dkongjr_sh_roar_w(int offset,int data)
{
	static int roar = 0;
	if (roar != data)
	{
		if (data)
			sample_start (7,2,0);
		roar = data;
	}
}

void dkongjr_sh_jump_w(int offset,int data)
{
	static int jump = 0;

	if (jump != data)
	{
		if (data)
			sample_start (6,0,0);


			jump = data;
	}
}



void dkongjr_sh_land_w(int offset,int data)
{
	static int land = 0;

	if (land != data)
	{
		if (data)
			sample_stop (7) ;
			sample_start (4,1,0);

			land = data;
	}
}



void dkongjr_sh_climb_w(int offset,int data)
{
	static int climb = 0;

	if (climb != data)
	{
		if (data && walk == 0)
		{
			sample_start (3,3,0);
		}
		else if (data && walk == 1)
		{
			sample_start (3,6,0);
		}
			climb = data;
	}
}


void dkongjr_sh_snapjaw_w(int offset,int data)
{
	static int snapjaw = 0;

	if (snapjaw != data)
	{
		if (data)
			sample_stop (7) ;
			sample_start (4,7,0);

		snapjaw = data;
	}
}


void dkongjr_sh_walk_w(int offset,int data)
{


	if (walk != data )
	{
			walk = data;
	}
}
