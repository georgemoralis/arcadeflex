/***************************************************************************

Atari Basketball machine

If you have any questions about how this driver works, don't hesitate to
ask.  - Mike Balfour (mab22@po.cwru.edu)
***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"

static int LD1=0;
static int LD2=0;
static unsigned int NMION = 0;

/***************************************************************************
bsktball_nmion_w
***************************************************************************/
WRITE_HANDLER( bsktball_nmion_w )
{
	NMION = offset & 0x01;
}

/***************************************************************************
bsktball_interrupt
***************************************************************************/
/* NMI every 32V, IRQ every VBLANK */
int bsktball_interrupt(void)
{
	static int i256V=0;

	/* We mod by 8 because we're interrupting 8x per frame, 1 per 32V */
	i256V=(i256V+1) % 8;

	if (i256V==0)
		return interrupt();
	else if (NMION)
		return nmi_interrupt();
	else
		return 0;
}

/***************************************************************************
bsktball_ld_w
***************************************************************************/

WRITE_HANDLER( bsktball_ld1_w )
{
	LD1 = (offset & 0x01);
}

WRITE_HANDLER( bsktball_ld2_w )
{
	LD2 = (offset & 0x01);
}


/***************************************************************************
bsktball_in0_r
***************************************************************************/

READ_HANDLER( bsktball_in0_r )
{
	static int DR0=0;		/* PL2 H DIR */
	static int DR1=0;		/* PL2 V DIR */
	static int DR2=0;		/* PL1 H DIR */
	static int DR3=0;		/* PL1 V DIR */

	static int last_p1_horiz=0;
	static int last_p1_vert=0;
	static int last_p2_horiz=0;
	static int last_p2_vert=0;

	int p1_horiz;
	int p1_vert;
	int p2_horiz;
	int p2_vert;
	int temp;

	p1_horiz = input_port_0_r(offset);
	p1_vert  = input_port_1_r(offset);
	p2_horiz = input_port_2_r(offset);
	p2_vert  = input_port_3_r(offset);

	/* Set direction bits */

	/* P1 H DIR */
	if (p1_horiz > last_p1_horiz)
	{
		if ((p1_horiz-last_p1_horiz) > 128)		DR2=0x40;
		else									DR2=0;
	}
	else if (p1_horiz < last_p1_horiz)
	{
		if ((last_p1_horiz-p1_horiz) > 128)		DR2=0;
		else									DR2=0x40;
	}

	/* P1 V DIR */
	if (p1_vert > last_p1_vert)
	{
		if ((p1_vert-last_p1_vert) > 128)		DR3=0;
		else									DR3=0x80;
	}
	else if (p1_vert < last_p1_vert)
	{
		if ((last_p1_vert-p1_vert) > 128)		DR3=0x80;
		else									DR3=0;
	}

	/* P2 H DIR */
	if (p2_horiz > last_p2_horiz)
	{
		if ((p2_horiz-last_p2_horiz) > 128)		DR0=0x10;
		else									DR0=0;
	}
	else if (p2_horiz < last_p2_horiz)
	{
		if ((last_p2_horiz-p2_horiz) > 128)		DR0=0;
		else									DR0=0x10;
	}

	/* P2 V DIR */
	if (p2_vert > last_p2_vert)
	{
		if ((p2_vert-last_p2_vert) > 128)		DR1=0;
		else									DR1=0x20;
	}
	else if (p2_vert < last_p2_vert)
	{
		if ((last_p2_vert-p2_vert) > 128)		DR1=0x20;
		else									DR1=0;
	}


	last_p1_horiz = p1_horiz;
	last_p1_vert  = p1_vert;
	last_p2_horiz = p2_horiz;
	last_p2_vert  = p2_vert;

	/* D0-D3 = Plyr 1 Horiz, D4-D7 = Plyr 1 Vert */
	if ((LD1) & (LD2))
	{
		return ((p1_horiz & 0x0F) | ((p1_vert << 4) & 0xF0));
	}
	/* D0-D3 = Plyr 2 Horiz, D4-D7 = Plyr 2 Vert */
	else if (LD2)
	{
		return ((p2_horiz & 0x0F) | ((p2_vert << 4) & 0xF0));
	}
	else
	{
		temp = input_port_4_r(offset) & 0x0F;
		/* Remap button 1 back to the Start button */
		/* NOTE:  This is an ADDED feature, not a part of the original hardware! */
		temp = (temp) & (temp>>2);

		return (temp | DR0 | DR1 | DR2 | DR3);
	}
}

/***************************************************************************
bsktball_led_w
***************************************************************************/
WRITE_HANDLER( bsktball_led1_w )
{
	set_led_status(0,offset & 0x01);
}

WRITE_HANDLER( bsktball_led2_w )
{
	set_led_status(1,offset & 0x01);
}

