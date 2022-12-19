
/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package machine;

public class stactics
{
	
	/* needed in vidhrdw/stactics.c */
	int stactics_vert_pos;
	int stactics_horiz_pos;
	UBytePtr stactics_motor_on;
	
	/* defined in vidhrdw/stactics.c */
	extern int stactics_vblank_count;
	extern int stactics_shot_standby;
	extern int stactics_shot_arrive;
	
	public static ReadHandlerPtr stactics_port_0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    if (*stactics_motor_on & 0x01)
	    {
	        return (input_port_0_r.handler(0)&0x7f);
	    }
	    else if ((stactics_horiz_pos == 0) && (stactics_vert_pos == 0))
	    {
	        return (input_port_0_r.handler(0)&0x7f);
	    }
	    else
	    {
	        return (input_port_0_r.handler(0)|0x80);
	    }
	} };
	
	public static ReadHandlerPtr stactics_port_2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    return (input_port_2_r.handler(0)&0xf0)+(stactics_vblank_count&0x08)+(rand()%8);
	} };
	
	public static ReadHandlerPtr stactics_port_3_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    return (input_port_3_r.handler(0)&0x7d)+(stactics_shot_standby<<1)
	                 +((stactics_shot_arrive^0x01)<<7);
	} };
	
	public static ReadHandlerPtr stactics_vert_pos_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    return 0x70-stactics_vert_pos;
	} };
	
	public static ReadHandlerPtr stactics_horiz_pos_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    return stactics_horiz_pos+0x80;
	} };
	
	public static InterruptPtr stactics_interrupt = new InterruptPtr() { public int handler() 
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
	} };
	
	public static WriteHandlerPtr stactics_coin_lockout_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		coin_lockout_w.handler(offset, ~data & 0x01);
	} };
	
}
