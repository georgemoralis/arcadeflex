/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package machine;

public class superpac
{
	
	
	unsigned char *superpac_sharedram;
	unsigned char *superpac_customio_1,*superpac_customio_2;
	
	static unsigned char interrupt_enable_1,interrupt_enable_2;
	static int coin1, coin2, credits, start1, start2;
	
	static int crednum[] = { 1, 2, 3, 6, 7, 1, 3, 1 };
	static int credden[] = { 1, 1, 1, 1, 1, 2, 2, 3 };
	
	public static InitMachinePtr superpac_init_machine = new InitMachinePtr() { public void handler() 
	{
		/* Reset all flags */
		coin1 = coin2 = start1 = start2 = credits = 0;
	
		/* Disable interrupts */
		interrupt_enable_1 = interrupt_enable_2 = 0;
	} };
	
	
	public static ReadHandlerPtr superpac_sharedram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return superpac_sharedram[offset];
	} };
	
	
	public static ReadHandlerPtr superpac_sharedram_r2 = new ReadHandlerPtr() { public int handler(int offset)
	{
		/* to speed up emulation, we check for the loop the sound CPU sits in most of the time
		   and end the current iteration (things will start going again with the next IRQ) */
	//	if (offset == 0xfb - 0x40 && superpac_sharedram[offset] == 0)
	//		cpu_seticount (0);
		return superpac_sharedram[offset];
	} };
	
	
	public static ReadHandlerPtr pacnpal_sharedram_r2 = new ReadHandlerPtr() { public int handler(int offset)
	{
		return superpac_sharedram[offset];
	} };
	
	
	public static WriteHandlerPtr pacnpal_sharedram_w2 = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		superpac_sharedram[offset] = data;
	} };
	
	
	public static WriteHandlerPtr superpac_sharedram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		superpac_sharedram[offset] = data;
	} };
	
	public static WriteHandlerPtr superpac_reset_2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		cpu_set_reset_line(1,PULSE_LINE);
	} };
	
	
	public static WriteHandlerPtr superpac_customio_w_1 = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		superpac_customio_1[offset] = data;
	} };
	
	
	public static WriteHandlerPtr superpac_customio_w_2 = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		superpac_customio_2[offset] = data;
	} };
	
	
	void superpac_update_credits (void)
	{
		int val = readinputport (3) & 0x0f, temp;
		if ((val & 1) != 0)
		{
			if (!coin1) credits++, coin1++;
		}
		else coin1 = 0;
	
		if ((val & 2) != 0)
		{
			if (!coin2) credits++, coin2++;
		}
		else coin2 = 0;
	
		temp = readinputport (1) & 7;
		val = readinputport (3) >> 4;
		if ((val & 1) != 0)
		{
			if (!start1 && credits >= credden[temp]) credits -= credden[temp], start1++;
		}
		else start1 = 0;
	
		if ((val & 2) != 0)
		{
			if (!start2 && credits >= 2 * credden[temp]) credits -= 2 * credden[temp], start2++;
		}
		else start2 = 0;
	}
	
	
	public static ReadHandlerPtr superpac_customio_r_1 = new ReadHandlerPtr() { public int handler(int offset)
	{
		int val, temp;
	
		superpac_update_credits ();
		switch (superpac_customio_1[8])
		{
			/* mode 1 & 3 are used by Pac & Pal, and returns actual important values */
			case 1:
			case 3:
				switch (offset)
				{
					case 0:
						val = readinputport (3) & 0x0f;
						break;
	
					case 1:
						val = readinputport (2) & 0x0f;
						break;
	
					case 2:
						val = 0;
						break;
	
					case 3:
						val = (readinputport (3) >> 4) & 3;
						val |= val << 2;
	
						/* I don't know the exact mix, but the low bit is used both for
						   the fire button and for player 1 start; I'm just ORing for now */
						val |= readinputport (2) >> 4;
						break;
	
					case 4:
					case 5:
					case 6:
					case 7:
						val = 0xf;
						break;
	
					default:
						val = superpac_customio_1[offset];
						break;
				}
				return val;
	
			/* mode 4 is the standard, and returns actual important values */
			case 4:
				switch (offset)
				{
					case 0:
						temp = readinputport (1) & 7;
						val = (credits * crednum[temp] / credden[temp]) / 10;
						break;
	
					case 1:
						temp = readinputport (1) & 7;
						val = (credits * crednum[temp] / credden[temp]) % 10;
						break;
	
					case 4:
						val = readinputport (2) & 0x0f;
						break;
	
					case 5:
						val = readinputport (2) >> 4;
						break;
	
					case 6:
					case 7:
						val = 0xf;
						break;
	
					default:
						val = superpac_customio_1[offset];
						break;
				}
				return val;
	
			/* mode 8 is the test mode: always return 0 for these locations */
			case 8:
				credits = 0;
				if (offset >= 9 && offset <= 15)
					return 0;
				break;
		}
		return superpac_customio_1[offset];
	} };
	
	public static ReadHandlerPtr superpac_customio_r_2 = new ReadHandlerPtr() { public int handler(int offset)
	{
		int val;
	
		switch (superpac_customio_2[8])
		{
			/* mode 3 is the standard for Pac & Pal, and returns actual important values */
			case 3:
				switch (offset)
				{
					case 0:
					case 1:
					case 2:
					case 3:
						val = 0;
						break;
	
					case 4:
						val = readinputport (0) & 0xf;
						break;
	
					case 5:
						val = readinputport (1) >> 4;
						break;
	
					case 6:
						val = readinputport (1) & 0xf;
						break;
	
					case 7:
						val = (readinputport (3) >> 4) & 0x0c;
						break;
	
					default:
						val = superpac_customio_2[offset];
						break;
				}
				return val;
	
			/* mode 9 is the standard, and returns actual important values */
			case 9:{
				switch (offset)
				{
					case 0:
						val = readinputport (1) & 0x0f;
						break;
	
					case 1:
						val = readinputport (1) >> 4;
						break;
	
					case 2:
						val = 0;
						break;
	
					case 3:
						val = readinputport (0) & 0x0f;
						break;
	
					case 4:
						val = readinputport (0) >> 4;
						break;
	
					case 5:
						val = 0;
						break;
	
					case 6:
						val = (readinputport (3) >> 4) & 0x0c;
						break;
	
					case 7:
						val = 0;
						break;
	
					default:
						val = superpac_customio_2[offset];
						break;
				}
				return val;}
	
			/* mode 8 is the test mode: always return 0 for these locations */
			case 8:
				credits = 0;
				if (offset >= 9 && offset <= 15)
					return 0;
				break;
		}
		return superpac_customio_2[offset];
	} };
	
	
	public static WriteHandlerPtr superpac_interrupt_enable_1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		interrupt_enable_1 = offset;
	} };
	
	
	public static InterruptPtr superpac_interrupt_1 = new InterruptPtr() { public int handler() 
	{
		if (interrupt_enable_1 != 0) return interrupt();
		else return ignore_interrupt();
	} };
	
	
	public static WriteHandlerPtr pacnpal_interrupt_enable_2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		interrupt_enable_2 = offset;
	} };
	
	
	public static WriteHandlerPtr superpac_cpu_enable_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		cpu_set_halt_line(1, offset ? CLEAR_LINE : ASSERT_LINE);
	} };
	
	
	public static InterruptPtr pacnpal_interrupt_2 = new InterruptPtr() { public int handler() 
	{
		if (interrupt_enable_2 != 0) return interrupt();
		else return ignore_interrupt();
	} };
}
