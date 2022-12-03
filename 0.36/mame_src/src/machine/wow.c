/**************************************************************************

	Interrupt System Hardware for Bally/Midway games

 	Mike@Dissfulfils.co.uk

**************************************************************************/

#include "driver.h"



void wow_update_line(int line, int gorf);


/****************************************************************************
 * Scanline Interrupt System
 ****************************************************************************/

static int NextScanInt=0;			/* Normal */
static int CurrentScan=0;
static int InterruptFlag=0;

int wow_controller1=32;			/* Seawolf II */
int wow_controller2=32;

static int GorfDelay;				/* Gorf */
static int Countdown=0;

void wow_interrupt_enable_w(int offset, int data)
{
    InterruptFlag = data;

    if (data & 0x01)					/* Disable Interrupts? */
  	    interrupt_enable_w(0,0);
    else
  		interrupt_enable_w(0,1);

    /* Gorf Special interrupt */

    if (data & 0x10)
 	{
  		GorfDelay =(CurrentScan + 7) & 0xFF;

        /* Gorf Special *MUST* occur before next scanline interrupt */

        if ((NextScanInt > CurrentScan) && (NextScanInt < GorfDelay))
        {
          	GorfDelay = NextScanInt - 1;
        }

#ifdef MAME_DEBUG
        if (errorlog) fprintf(errorlog,"Gorf Delay set to %02x\n",GorfDelay);
#endif

    }

#ifdef MAME_DEBUG
    if (errorlog) fprintf(errorlog,"Interrupt Flag set to %02x\n",InterruptFlag);
#endif
}

void wow_interrupt_w(int offset, int data)
{
	/* A write to 0F triggers an interrupt at that scanline */

#ifdef MAME_DEBUG
	if (errorlog) fprintf(errorlog,"Scanline interrupt set to %02x\n",data);
#endif

    NextScanInt = data;
}

int wow_interrupt(void)
{
	int res=ignore_interrupt();
    int Direction;

    CurrentScan++;

    if (CurrentScan == Machine->drv->cpu[0].vblank_interrupts_per_frame)
	{
		CurrentScan = 0;

    	/*
		 * Seawolf2 needs to emulate rotary ports
         *
         * Checked each flyback, takes 1 second to traverse screen
         */

        Direction = input_port_0_r(0);

        if ((Direction & 2) && (wow_controller1 > 0))
			wow_controller1--;

		if ((Direction & 1) && (wow_controller1 < 63))
			wow_controller1++;

        Direction = input_port_1_r(0);

        if ((Direction & 2) && (wow_controller2 > 0))
			wow_controller2--;

		if ((Direction & 1) && (wow_controller2 < 63))
			wow_controller2++;
    }

    if (CurrentScan < 204) wow_update_line(CurrentScan, 0);

    /* Scanline interrupt enabled ? */

    if ((InterruptFlag & 0x08) && (CurrentScan == NextScanInt))
		res = interrupt();

    return res;
}

/****************************************************************************
 * Gorf - Interrupt routine and Timer hack
 ****************************************************************************/

int gorf_interrupt(void)
{
	int res=ignore_interrupt();

    CurrentScan++;

    if (CurrentScan == 256)
	{
		CurrentScan=0;
    }

    if (CurrentScan < 204) wow_update_line(CurrentScan, 1);

    /* Scanline interrupt enabled ? */

    if ((InterruptFlag & 0x08) && (CurrentScan == NextScanInt))
		res = interrupt();

    /* Gorf Special Bits */

    if (Countdown>0) Countdown--;

    if ((InterruptFlag & 0x10) && (CurrentScan==GorfDelay))
		res = interrupt() & 0xF0;

/*	cpu_clear_pending_interrupts(0); */

//	Z80_Clear_Pending_Interrupts();					/* Temporary Fix */
	cpu_set_irq_line(0,0,CLEAR_LINE);

    return res;
}

int gorf_timer_r(int offset)
{
	static int Skip=0;
	unsigned char *RAM = memory_region(REGION_CPU1);


	if ((RAM[0x5A93]==160) || (RAM[0x5A93]==4)) 	/* INVADERS AND    */
	{												/* GALAXIAN SCREEN */
        if (cpu_get_pc()==0x3086)
        {
    	    if(--Skip==-1)
            {
                Skip=2;
            }
        }

	   	return Skip;
    }
    else
    {
    	return RAM[0xD0A5];
    }

}


int wow_video_retrace_r(int offset)
{
    return CurrentScan;
}


/****************************************************************************
 * Seawolf Controllers
 ****************************************************************************/

/*
 * Seawolf2 uses rotary controllers on input ports 10 + 11
 * each controller responds 0-63 for reading, with bit 7 as
 * fire button. Controller values are calculated in the
 * interrupt routine, and just formatted & returned here.
 *
 * The controllers look like they returns Grays binary,
 * so I use a table to translate my simple counter into it!
 */

static const int ControllerTable[64] = {
    0  , 1  , 3  , 2  , 6  , 7  , 5  , 4  ,
    12 , 13 , 15 , 14 , 10 , 11 , 9  , 8  ,
    24 , 25 , 27 , 26 , 30 , 31 , 29 , 28 ,
    20 , 21 , 23 , 22 , 18 , 19 , 17 , 16 ,
    48 , 49 , 51 , 50 , 54 , 55 , 53 , 52 ,
    60 , 61 , 63 , 62 , 58 , 59 , 57 , 56 ,
    40 , 41 , 43 , 42 , 46 , 47 , 45 , 44 ,
    36 , 37 , 39 , 38 , 34 , 35 , 33 , 32
};

int seawolf2_controller1_r(int offset)
{
    return (input_port_0_r(0) & 0xC0) + ControllerTable[wow_controller1];
}

int seawolf2_controller2_r(int offset)
{
    return (input_port_1_r(0) & 0x80) + ControllerTable[wow_controller2];
}


static int ebases_trackball_select = 0;

void ebases_trackball_select_w(int offset, int data)
{
	ebases_trackball_select = data;
}

int ebases_trackball_r(int offset)
{
	int ret = readinputport(3 + ebases_trackball_select);
	if (errorlog) fprintf(errorlog, "Port %d = %d\n", ebases_trackball_select, ret);
	return ret;
}
