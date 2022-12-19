
#include "driver.h"

/* needed in vidhrdw/stactics.c */
int stactics_vert_pos;
int stactics_horiz_pos;
unsigned char *stactics_motor_on;

/* defined in vidhrdw/stactics.c */
extern int stactics_vblank_count;
extern int stactics_shot_standby;
extern int stactics_shot_arrive;

READ_HANDLER( stactics_port_0_r )
{
    if (*stactics_motor_on & 0x01)
    {
        return (input_port_0_r(0)&0x7f);
    }
    else if ((stactics_horiz_pos == 0) && (stactics_vert_pos == 0))
    {
        return (input_port_0_r(0)&0x7f);
    }
    else
    {
        return (input_port_0_r(0)|0x80);
    }
}

READ_HANDLER( stactics_port_2_r )
{
    return (input_port_2_r(0)&0xf0)+(stactics_vblank_count&0x08)+(rand()%8);
}

READ_HANDLER( stactics_port_3_r )
{
    return (input_port_3_r(0)&0x7d)+(stactics_shot_standby<<1)
                 +((stactics_shot_arrive^0x01)<<7);
}

READ_HANDLER( stactics_vert_pos_r )
{
    return 0x70-stactics_vert_pos;
}

READ_HANDLER( stactics_horiz_pos_r )
{
    return stactics_horiz_pos+0x80;
}

int stactics_interrupt(void)
{
    /* Run the monitor motors */

    if (*stactics_motor_on & 0x01) /* under joystick control */
    {
		int ip3 = readinputport(3);
		int ip4 = readinputport(4);

		if ((ip4 & 0x01) == 0)	/* up */
			if (stactics_vert_pos > -128)
				stactics_vert_pos--;
		if ((ip4 & 0x02) == 0)	/* down */
			if (stactics_vert_pos < 127)
				stactics_vert_pos++;
		if ((ip3 & 0x20) == 0)	/* left */
			if (stactics_horiz_pos < 127)
				stactics_horiz_pos++;
		if ((ip3 & 0x40) == 0)	/* right */
			if (stactics_horiz_pos > -128)
				stactics_horiz_pos--;
    }
    else /* under self-centering control */
    {
        if (stactics_horiz_pos > 0)
            stactics_horiz_pos--;
        else if (stactics_horiz_pos < 0)
            stactics_horiz_pos++;
        if (stactics_vert_pos > 0)
            stactics_vert_pos--;
        else if (stactics_vert_pos < 0)
            stactics_vert_pos++;
    }

    return interrupt();
}

WRITE_HANDLER( stactics_coin_lockout_w )
{
	coin_lockout_w(offset, ~data & 0x01);
}

