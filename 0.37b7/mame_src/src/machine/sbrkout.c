/***************************************************************************

Atari Super Breakout machine

If you have any questions about how this driver works, don't hesitate to
ask.  - Mike Balfour (mab22@po.cwru.edu)
***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"
#include "cpu/m6502/m6502.h"

#define SBRKOUT_PROGRESSIVE 0x00
#define SBRKOUT_DOUBLE      0x01
#define SBRKOUT_CAVITY      0x02

int sbrkout_game_switch = SBRKOUT_PROGRESSIVE;

/***************************************************************************
Interrupt

Super Breakout has a three-position dial used to select which game to
play - Progressive, Double, and Cavity.  We use the interrupt to check
for a key press representing one of these three choices and set our
game switch appropriately.  We can't just check for key values at the time
the game checks the game switch, because we would probably lose a *lot* of
key presses.  Also, MAME doesn't currently support a switch control like
DIP switches that's used as a runtime control.
***************************************************************************/
int sbrkout_interrupt(void)
{
    int game_switch;

    game_switch=input_port_7_r(0);

    if (game_switch & 0x01)
        sbrkout_game_switch=SBRKOUT_PROGRESSIVE;
    else if (game_switch & 0x02)
        sbrkout_game_switch=SBRKOUT_DOUBLE;
    else if (game_switch & 0x04)
        sbrkout_game_switch=SBRKOUT_CAVITY;

    return interrupt();
}

READ_HANDLER( sbrkout_select1_r )
{
    if (sbrkout_game_switch==SBRKOUT_CAVITY)
        return 0x80;
    else return 0x00;
}

READ_HANDLER( sbrkout_select2_r )
{
    if (sbrkout_game_switch==SBRKOUT_DOUBLE)
        return 0x80;
    else return 0x00;
}

WRITE_HANDLER( sbrkout_irq_w )
{
        /* generate irq */
        cpu_cause_interrupt(0,M6502_INT_IRQ);
}


/***************************************************************************
Read DIPs

We remap all of our DIP switches from a single byte to four bytes.  This is
because some of the DIP switch settings would be spread across multiple
bytes, and MAME doesn't currently support that.
***************************************************************************/

READ_HANDLER( sbrkout_read_DIPs_r )
{
        switch (offset)
        {
                /* DSW */
                case 0x00:      return ((input_port_0_r(0) & 0x03) << 6);
                case 0x01:      return ((input_port_0_r(0) & 0x0C) << 4);
                case 0x02:      return ((input_port_0_r(0) & 0xC0) << 0);
                case 0x03:      return ((input_port_0_r(0) & 0x30) << 2);

                /* Just in case */
                default:        return 0xFF;
        }
}

/***************************************************************************
Lamps

The LEDs are turned on and off by two consecutive memory addresses.  The
first address turns them off, the second address turns them on.  This is
reversed for the Serve LED, which has a NOT on the signal.
***************************************************************************/
WRITE_HANDLER( sbrkout_start_1_led_w )
{
	set_led_status(0,offset & 1);
}

WRITE_HANDLER( sbrkout_start_2_led_w )
{
	set_led_status(1,offset & 1);
}

WRITE_HANDLER( sbrkout_serve_led_w )
{
	set_led_status(2,~offset & 1);
}

