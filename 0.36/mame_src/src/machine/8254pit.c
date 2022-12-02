
#include "driver.h"
//#include <stdio.h>

#include "machine/8254pit.h"

//extern void *errorlog;


void pit8254_init (pit8254_interface *intf)
{

}

void pit8254_w (int which, int offset, int data)
{
    switch (offset)
    {
        case 0:
            if (errorlog)
            {
                fprintf(errorlog, "PIT8254#%d write %d to timer1\n",
                    which, data);
            }

            break;
        case 1:
            if (errorlog)
            {
                fprintf(errorlog, "PIT8254#%d write %d to timer2\n",
                    which, data);
            }
            break;
        case 2:
            if (errorlog)
            {
                fprintf(errorlog, "PIT8254#%d write %d to timer3\n",
                    which, data);
            }
            break;
        case 3:
            if (errorlog)
            {
                int sc=(data>>6)&3;
                int rw=(data>>4)&3;
                int mode=(data>>1)&0x07;
                int bcd=data&0x01;
                fprintf(errorlog, "PIT8254#%d write %02x to control : ", which, data);
                fprintf(errorlog, "*** SC=%d RW=%d MODE=%d BCD=%d\n",
                    sc, rw, mode, bcd);
            }

            break;
    }
}

int pit8254_r (int which, int offset)
{
    switch (offset)
    {
        case 0:
            if (errorlog)
            {
                fprintf(errorlog, "PIT8254#%d read from timer1\n", which);
            }

            break;
        case 1:
            if (errorlog)
            {
                fprintf(errorlog, "PIT8254#%d read from timer2\n", which);
            }
            break;
        case 2:
            if (errorlog)
            {
               fprintf(errorlog, "PIT8254#%d read from timer3\n", which);
            }
            break;
        case 3:
            if (errorlog)
            {
               fprintf(errorlog, "PIT8254#%d read from control\n", which);
            }
            break;
    }

    return 0;
}

/*
Port handler wrappers.
*/

void pit8254_0_w(int offset, int data)
{
	pit8254_w(0, offset, data);
}

void pit8254_0_counter1_w (int offset, int data)
{
	pit8254_w(0, 0, data);
}

void pit8254_0_counter2_w (int offset, int data)
{
	pit8254_w(0, 1, data);
}

void pit8254_0_counter3_w (int offset, int data)
{
	pit8254_w(0, 2, data);
}

void pit8254_0_control_w  (int offset, int data)
{
	pit8254_w(0, 3, data);
}

void pit8254_1_w(int offset, int data)
{
	pit8254_w(1, 0, data);
}

void pit8254_1_counter1_w (int offset, int data)
{
	pit8254_w(1, 0, data);
}

void pit8254_1_counter2_w (int offset, int data)
{
	pit8254_w(1, 1, data);
}

void pit8254_1_counter3_w (int offset, int data)
{
	pit8254_w(1, 2, data);
}

void pit8254_1_control_w  (int offset, int data)
{
	pit8254_w(1, 3, data);
}


int pit8254_0_r (int offset)
{
	return pit8254_r(0, offset);
}

int pit8254_0_counter1_r (int offset)
{
	return pit8254_r(0, 0);
}

int pit8254_0_counter2_r (int offset)
{
	return pit8254_r(0, 1);
}

int pit8254_0_counter3_r (int offset)
{
	return pit8254_r(0, 2);
}

int pit8254_0_control_r (int offset)
{
	return pit8254_r(0, 3);
}

int pit8254_1_r (int offset)
{
	return pit8254_r(1, offset);
}

int pit8254_1_counter1_r (int offset)
{
	return pit8254_r(1, 0);
}

int pit8254_1_counter2_r (int offset)
{
	return pit8254_r(1, 1);
}

int pit8254_1_counter3_r (int offset)
{
	return pit8254_r(1, 2);
}
int pit8254_1_control_r (int offset)
{
	return pit8254_r(1, 3);
}
