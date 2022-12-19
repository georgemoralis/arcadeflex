/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package machine;

public class digdug
{
	
	
	UBytePtr digdug_sharedram;
	static unsigned char interrupt_enable_1,interrupt_enable_2,interrupt_enable_3;
	
	static int credits;
	
	static void *nmi_timer;
	
	
	
	public static InitMachinePtr digdig_init_machine = new InitMachinePtr() { public void handler() 
	{
		credits = 0;
		nmi_timer = 0;
		interrupt_enable_1 = interrupt_enable_2 = interrupt_enable_3 = 0;
		digdug_halt_w (0, 0);
	} };
	
	
	public static ReadHandlerPtr digdug_sharedram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return digdug_sharedram[offset];
	} };
	
	
	public static WriteHandlerPtr digdug_sharedram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* a video ram write */
		if (offset < 0x400)
			dirtybuffer[offset] = 1;
	
		/* location 9b3d is set to zero just before CPU 2 spins */
		if (offset == 0x1b3d && data == 0 && cpu_get_pc () == 0x1df1 && cpu_getactivecpu () == 1)
			cpu_spinuntil_int ();
	
		digdug_sharedram[offset] = data;
	} };
	
	
	/***************************************************************************
	
	 Emulate the custom IO chip.
	
	***************************************************************************/
	static int customio_command;
	static int leftcoinpercred,leftcredpercoin;
	static int rightcoinpercred,rightcredpercoin;
	static unsigned char customio[16];
	static int mode;
	
	public static WriteHandlerPtr digdug_customio_data_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		customio[offset] = data;
	
	logerror("%04x: custom IO offset %02x data %02x\n",cpu_get_pc(),offset,data);
	
		switch (customio_command)
		{
			case 0xc1:
				if (offset == 8)
				{
					leftcoinpercred = customio[2] & 0x0f;
					leftcredpercoin = customio[3] & 0x0f;
					rightcoinpercred = customio[4] & 0x0f;
					rightcredpercoin = customio[5] & 0x0f;
				}
				break;
		}
	} };
	
	
	public static ReadHandlerPtr digdug_customio_data_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch (customio_command)
		{
			case 0x71:
				if (offset == 0)
				{
					if (mode != 0)	/* switch mode */
					{
						/* bit 7 is the service switch */
	                                        return readinputport(4);
					}
					else	/* credits mode: return number of credits in BCD format */
					{
						int in;
						static int leftcoininserted;
						static int rightcoininserted;
	
	
	                                        in = readinputport(4);
	
						/* check if the user inserted a coin */
						if (leftcoinpercred > 0)
						{
							if ((in & 0x01) == 0 && credits < 99)
							{
								leftcoininserted++;
								if (leftcoininserted >= leftcoinpercred)
								{
									credits += leftcredpercoin;
									leftcoininserted = 0;
								}
							}
							if ((in & 0x02) == 0 && credits < 99)
							{
								rightcoininserted++;
								if (rightcoininserted >= rightcoinpercred)
								{
									credits += rightcredpercoin;
									rightcoininserted = 0;
								}
							}
						}
						else credits = 2;
	
	
						/* check for 1 player start button */
						if ((in & 0x10) == 0)
							if (credits >= 1) credits--;
	
						/* check for 2 players start button */
						if ((in & 0x20) == 0)
							if (credits >= 2) credits -= 2;
	
						return (credits / 10) * 16 + credits % 10;
					}
				}
				else if (offset == 1)
				{
					int p2 = readinputport (2);
	
					if (mode == 0)
					{
						/* check directions, according to the following 8-position rule */
						/*         0          */
						/*        7 1         */
						/*       6 8 2        */
						/*        5 3         */
						/*         4          */
						if ((p2 & 0x01) == 0)		/* up */
							p2 = (p2 & ~0x0f) | 0x00;
						else if ((p2 & 0x02) == 0)	/* right */
							p2 = (p2 & ~0x0f) | 0x02;
						else if ((p2 & 0x04) == 0)	/* down */
							p2 = (p2 & ~0x0f) | 0x04;
						else if ((p2 & 0x08) == 0) /* left */
							p2 = (p2 & ~0x0f) | 0x06;
						else
							p2 = (p2 & ~0x0f) | 0x08;
					}
	
					return p2;
				}
	                        else if (offset == 2)
				{
	                                int p2 = readinputport (3);
	
					if (mode == 0)
					{
						/* check directions, according to the following 8-position rule */
						/*         0          */
						/*        7 1         */
						/*       6 8 2        */
						/*        5 3         */
						/*         4          */
						if ((p2 & 0x01) == 0)		/* up */
							p2 = (p2 & ~0x0f) | 0x00;
						else if ((p2 & 0x02) == 0)	/* right */
							p2 = (p2 & ~0x0f) | 0x02;
						else if ((p2 & 0x04) == 0)	/* down */
							p2 = (p2 & ~0x0f) | 0x04;
						else if ((p2 & 0x08) == 0) /* left */
							p2 = (p2 & ~0x0f) | 0x06;
						else
							p2 = (p2 & ~0x0f) | 0x08;
					}
	
	                                return p2; /*p2 jochen*/
				}
				break;
	
			case 0xb1:	/* status? */
				if (offset <= 2)
					return 0;
				break;
	
			case 0xd2:	/* checking the dipswitches */
				if (offset == 0)
					return readinputport (0);
				else if (offset == 1)
					return readinputport (1);
				break;
		}
	
		return -1;
	} };
	
	
	public static ReadHandlerPtr digdug_customio_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return customio_command;
	} };
	
	void digdug_nmi_generate (int param)
	{
		cpu_cause_interrupt (0, Z80_NMI_INT);
	}
	
	
	public static WriteHandlerPtr digdug_customio_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (data != 0x10 && data != 0x71)
			logerror("%04x: custom IO command %02x\n",cpu_get_pc(),data);
	
		customio_command = data;
	
		switch (data)
		{
			case 0x10:
				if (nmi_timer != 0) timer_remove (nmi_timer);
				nmi_timer = 0;
				return;
	
			case 0xa1:	/* go into switch mode */
				mode = 1;
				break;
	
			case 0xc1:
			case 0xe1:	/* go into credit mode */
				mode = 0;
				break;
	
			case 0xb1:	/* status? */
				credits = 0;	/* this is a good time to reset the credits counter */
				break;
		}
	
		nmi_timer = timer_pulse (TIME_IN_USEC (50), 0, digdug_nmi_generate);
	} };
	
	
	
	public static WriteHandlerPtr digdug_halt_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 1) != 0)
		{
			cpu_set_reset_line(1,CLEAR_LINE);
			cpu_set_reset_line(2,CLEAR_LINE);
		}
		else
		{
			cpu_set_reset_line(1,ASSERT_LINE);
			cpu_set_reset_line(2,ASSERT_LINE);
		}
	} };
	
	
	
	public static WriteHandlerPtr digdug_interrupt_enable_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		interrupt_enable_1 = (data&1);
	} };
	
	
	
	public static InterruptPtr digdug_interrupt_1 = new InterruptPtr() { public int handler() 
	{
		if (interrupt_enable_1 != 0) return interrupt();
		else return ignore_interrupt();
	} };
	
	
	
	public static WriteHandlerPtr digdug_interrupt_enable_2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		interrupt_enable_2 = data & 1;
	} };
	
	
	
	public static InterruptPtr digdug_interrupt_2 = new InterruptPtr() { public int handler() 
	{
		if (interrupt_enable_2 != 0) return interrupt();
		else return ignore_interrupt();
	} };
	
	
	
	public static WriteHandlerPtr digdug_interrupt_enable_3_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		interrupt_enable_3 = !(data & 1);
	} };
	
	
	
	public static InterruptPtr digdug_interrupt_3 = new InterruptPtr() { public int handler() 
	{
		if (interrupt_enable_3 != 0) return nmi_interrupt();
		else return ignore_interrupt();
	} };
}
