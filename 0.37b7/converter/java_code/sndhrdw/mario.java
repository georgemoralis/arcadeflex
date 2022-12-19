/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package sndhrdw;

public class mario
{
	
	
	
	public static WriteHandlerPtr mario_sh_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (data != 0)
			cpu_set_irq_line(1,0,ASSERT_LINE);
		else
			cpu_set_irq_line(1,0,CLEAR_LINE);
	} };
	
	
	/* Mario running sample */
	public static WriteHandlerPtr mario_sh1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		static int last;
	
		if (last!= data)
		{
			last = data;
	                if (data && sample_playing(0) == 0) sample_start (0, 3, 0);
		}
	} };
	
	/* Luigi running sample */
	public static WriteHandlerPtr mario_sh2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		static int last;
	
		if (last!= data)
		{
			last = data;
	                if (data && sample_playing(1) == 0) sample_start (1, 4, 0);
		}
	} };
	
	/* Misc samples */
	public static WriteHandlerPtr mario_sh3_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		static int state[8];
	
		/* Don't trigger the sample if it's still playing */
		if (state[offset] == data) return;
	
		state[offset] = data;
		if (data != 0)
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
	} };
}
