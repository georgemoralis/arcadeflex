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

public class mappy
{
	
	
	UBytePtr mappy_sharedram;
	UBytePtr mappy_customio_1,*mappy_customio_2;
	
	static unsigned char interrupt_enable_1,interrupt_enable_2;
	static int credits, coin, start1, start2;
	static int io_chip_1_enabled, io_chip_2_enabled;
	
	public static InitMachinePtr mappy_init_machine = new InitMachinePtr() { public void handler() 
	{
		/* Reset all flags */
		credits = coin = start1 = start2 = 0;
		interrupt_enable_1 = interrupt_enable_2 = 0;
	} };
	
	public static InitMachinePtr motos_init_machine = new InitMachinePtr() { public void handler() 
	{
		/* Reset all flags */
		credits = coin = start1 = start2 = 0;
	} };
	
	
	public static ReadHandlerPtr mappy_sharedram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return mappy_sharedram[offset];
	} };
	
	public static WriteHandlerPtr mappy_sharedram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		mappy_sharedram[offset] = data;
	} };
	
	
	public static WriteHandlerPtr mappy_customio_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		mappy_customio_1[offset] = data;
	} };
	
	
	public static WriteHandlerPtr mappy_customio_2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		mappy_customio_2[offset] = data;
	} };
	
	public static WriteHandlerPtr mappy_reset_2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		io_chip_1_enabled = io_chip_2_enabled = 0;
		cpu_set_reset_line(1,PULSE_LINE);
	} };
	
	public static WriteHandlerPtr mappy_io_chips_enable_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		io_chip_1_enabled = io_chip_2_enabled = 1;
	} };
	
	/*************************************************************************************
	 *
	 *         Mappy custom I/O ports
	 *
	 *************************************************************************************/
	
	public static ReadHandlerPtr mappy_customio_1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		static int crednum[] = { 1, 2, 3, 6, 1, 3, 1, 2 };
		static int credden[] = { 1, 1, 1, 1, 2, 2, 3, 3 };
		int val, temp, mode = mappy_customio_1[8];
	
		logerror("I/O read 1: mode %d offset %d\n", mode, offset);
	
		/* mode 3 is the standard, and returns actual important values */
		if (mode == 1 || mode == 3)
		{
			switch (offset)
			{
				case 0:		/* Coin slots, low nibble of port 4 */
				{
					static int lastval;
	
					val = readinputport (4) & 0x0f;
	
					/* bit 0 is a trigger for the coin slot */
					if ((val & 1) && ((val ^ lastval) & 1)) ++credits;
	
					return lastval = val;
				}
	
				case 1:		/* Start buttons, high nibble of port 4 */
				{
					static int lastval;
	
					temp = readinputport (1) & 7;
					val = readinputport (4) >> 4;
	
					/* bit 0 is a trigger for the 1 player start */
					if ((val & 1) && ((val ^ lastval) & 1))
					{
						if (credits >= credden[temp]) credits -= credden[temp];
						else val &= ~1;	/* otherwise you can start with no credits! */
					}
					/* bit 1 is a trigger for the 2 player start */
					if ((val & 2) && ((val ^ lastval) & 2))
					{
						if (credits >= 2 * credden[temp]) credits -= 2 * credden[temp];
						else val &= ~2;	/* otherwise you can start with no credits! */
					}
	
					return lastval = val;
				}
	
				case 2:		/* High BCD of credits */
					temp = readinputport (1) & 7;
					return (credits * crednum[temp] / credden[temp]) / 10;
	
				case 3:		/* Low BCD of credits */
					temp = readinputport (1) & 7;
					return (credits * crednum[temp] / credden[temp]) % 10;
	
				case 4:		/* Player 1 joystick */
					return readinputport (3) & 0x0f;
	
				case 5:		/* Player 1 buttons */
					return readinputport (3) >> 4;
	
				case 6:		/* Player 2 joystick */
					return readinputport (5) & 0x0f;
	
				case 7:		/* Player 2 joystick */
					return readinputport (5) >> 4;
			}
		}
	
		/* mode 5 values are actually checked against these numbers during power up */
		else if (mode == 5)
		{
			static int testvals[] = { 8, 4, 6, 14, 13, 9, 13 };
			if (offset >= 1 && offset <= 7)
				return testvals[offset - 1];
		}
	
		/* by default, return what was stored there */
		return mappy_customio_1[offset];
	} };
	
	
	public static ReadHandlerPtr mappy_customio_2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int mode = mappy_customio_2[8];
	
		logerror("I/O read 2: mode %d, offset %d\n", mappy_customio_2[8], offset);
	
		/* mode 4 is the standard, and returns actual important values */
		if (mode == 4)
		{
			switch (offset)
			{
				case 0:		/* DSW1, low nibble */
					return readinputport (1) & 0x0f;
	
				case 1:		/* DSW1, high nibble */
					return readinputport (1) >> 4;
	
				case 2:		/* DSW0, low nibble */
					return readinputport (0) & 0x0f;
	
				case 4:		/* DSW0, high nibble */
					return readinputport (0) >> 4;
	
				case 6:		/* DSW2 - service switch */
					return readinputport (2) & 0x0f;
	
				case 3:		/* read, but unknown */
				case 5:		/* read, but unknown */
				case 7:		/* read, but unknown */
					return 0;
			}
		}
	
		/* mode 5 values are actually checked against these numbers during power up */
		else if (mode == 5)
		{
			static int testvals[] = { 8, 4, 6, 14, 13, 9, 13 };
			if (offset >= 1 && offset <= 7)
				return testvals[offset - 1];
		}
	
		/* by default, return what was stored there */
		return mappy_customio_2[offset];
	} };
	
	
	/*************************************************************************************
	 *
	 *         Dig Dug 2 custom I/O ports
	 *
	 *************************************************************************************/
	
	public static ReadHandlerPtr digdug2_customio_1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		static int crednum[] = { 1, 1, 2, 2 };
		static int credden[] = { 1, 2, 1, 3 };
		int val, temp, mode = mappy_customio_1[8];
	
		/*logerror("I/O read 1: mode %d offset %d\n", mode, offset);*/
	
		if (io_chip_1_enabled != 0)
		{
			/* mode 3 is the standard, and returns actual important values */
			if (mode == 1 || mode == 3)
			{
				switch (offset)
				{
					case 0:		/* Coin slots, low nibble of port 4 */
					{
						static int lastval;
	
						val = readinputport (4) & 0x0f;
	
						/* bit 0 is a trigger for the coin slot */
						if ((val & 1) && ((val ^ lastval) & 1)) ++credits;
	
						return lastval = val;
					}
	
					case 1:		/* Start buttons, high nibble of port 4 */
					{
						static int lastval;
	
						temp = (readinputport (0) >> 6) & 3;
						val = readinputport (4) >> 4;
	
						/* bit 0 is a trigger for the 1 player start */
						if ((val & 1) && ((val ^ lastval) & 1))
							if (credits >= credden[temp]) credits -= credden[temp];
						/* bit 1 is a trigger for the 2 player start */
						if ((val & 2) && ((val ^ lastval) & 2))
							if (credits >= 2 * credden[temp]) credits -= 2 * credden[temp];
	
						return lastval = val;
					}
	
					case 2:		/* High BCD of credits */
						temp = (readinputport (0) >> 6) & 3;
						return (credits * crednum[temp] / credden[temp]) / 10;
	
					case 3:		/* Low BCD of credits */
						temp = (readinputport (0) >> 6) & 3;
						return (credits * crednum[temp] / credden[temp]) % 10;
	
					case 4:		/* Player 1 joystick */
						return readinputport (3) & 0x0f;
	
					case 5:		/* Player 1 buttons */
						return readinputport (3) >> 4;
	
					case 6:		/* Player 2 joystick */
						return readinputport (5) & 0x0f;
	
					case 7:		/* Player 2 buttons */
						return readinputport (5) >> 4;
				}
			}
		}
		/* by default, return what was stored there */
		return mappy_customio_1[offset];
	} };
	
	
	public static ReadHandlerPtr digdug2_customio_2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int mode = mappy_customio_2[8];
	
		/*logerror("I/O read 2: mode %d, offset %d\n", mode, offset);*/
	
		if (io_chip_2_enabled != 0)
		{
			/* mode 4 is the standard, and returns actual important values */
			if (mode == 4)
			{
				switch (offset)
				{
					case 2:		/* DSW0, low nibble */
						return readinputport (0) & 0x0f;
	
					case 4:		/* DSW0, high nibble */
						return readinputport (0) >> 4;
	
					case 5:		/* DSW1, high nibble */
						return readinputport (1) >> 4;
	
					case 6:		/* DSW1, low nibble */
						return readinputport (1) & 0x0f;
	
					case 7:		/* DSW2 - service switch */
						return readinputport (2) & 0x0f;
	
					case 0:		/* read, but unknown */
					case 1:		/* read, but unknown */
					case 3:		/* read, but unknown */
						return 0;
				}
			}
		}
		/* by default, return what was stored there */
		return mappy_customio_2[offset];
	} };
	
	
	/*************************************************************************************
	 *
	 *         Motos custom I/O ports
	 *
	 *************************************************************************************/
	
	public static ReadHandlerPtr motos_customio_1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int val, mode = mappy_customio_1[8];
	
		logerror("I/O read 1: mode %d offset %d\n", mode, offset);
	
		/* mode 1 is the standard, and returns actual important values */
		if (mode == 1)
		{
			switch (offset)
			{
				case 0:		/* Coin slots, low nibble of port 3 */
				{
					static int lastval;
	
					val = readinputport (3) & 0x0f;
	
					/* bit 0 is a trigger for the coin slot */
					if ((val & 1) && ((val ^ lastval) & 1)) ++credits;
	
					return lastval = val;
				}
	
				case 1:		/* Player 1 joystick */
					return readinputport (2) & 0x0f;
				case 3:		/* Start buttons, high nibble of port 3 */
					return readinputport (3) >> 4;
				case 9:
					return 0;
				case 2:
				case 4:
				case 5:
				case 6:
				case 7:		/* Player 2 joystick */
					return readinputport (4) & 0x0f;
			}
		}
		else if (mode == 8)  /* I/O tests chip 1 */
	    {
	        switch (offset)
	        {
	            case 0:
	                return 0x06;
	                break;
	            case 1:
	                return 0x09;
	                break;
	            default:
					/* by default, return what was stored there */
	                return mappy_customio_2[offset];
	        }
	    }
	
		/* by default, return what was stored there */
		return mappy_customio_1[offset];
	} };
	
	
	public static ReadHandlerPtr motos_customio_2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int mode = mappy_customio_2[8];
	
		logerror("I/O read 2: mode %d, offset %d\n", mode, offset);
	
		/* mode 9 is the standard, and returns actual important values */
		if (mode == 9)
		{
			switch (offset)
			{
				case 2:		/* DSW0, low nibble */
					return readinputport (0) & 0x0f;
	
				case 4:		/* DSW0, high nibble */
					return readinputport (0) >> 4;
	
	                  case 6:         /* DSW1, high nibble + Player 1 buttons, high nibble + Player 2? button, high nibble */
	
	                        return (readinputport (1) >> 4) | (readinputport (2) >> 4) | (readinputport (4) >> 4);
				case 0:
				case 1:
				case 3:
				case 5:
				case 7:
					return 0;
			}
		}
		else if (mode == 8)  /* I/O tests chip 2 */
	    {
	        switch (offset)
	        {
	            case 0:
	                return 0x06;
	                break;
	            case 1:
	                return 0x09;
	                break;
	            default:
					/* by default, return what was stored there */
	                return mappy_customio_2[offset];
	        }
	    }
	
		/* by default, return what was stored there */
		return mappy_customio_2[offset];
	} };
	
	
	/*************************************************************************************
	 *
	 *         Tower of Druaga custom I/O ports
	 *
	 *************************************************************************************/
	
	public static ReadHandlerPtr todruaga_customio_1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		static int crednum[] = { 1, 1, 2, 2 };
		static int credden[] = { 1, 2, 1, 3 };
		int val, temp, mode = mappy_customio_1[8];
	
		logerror("%04x: I/O read 1: mode %d offset %d\n", cpu_get_pc(), mode, offset);
	
		if (io_chip_1_enabled != 0)
		{
			/* mode 3 is the standard, and returns actual important values */
			if (mode == 1 || mode == 3)
			{
				switch (offset)
				{
					case 0:		/* Coin slots, low nibble of port 5 */
					{
						static int lastval;
	
						val = readinputport (5) & 0x0f;
	
						/* bit 0 is a trigger for the coin slot */
						if ((val & 1) && ((val ^ lastval) & 1)) ++credits;
	
						return lastval = val;
					}
	
					case 1:		/* Start buttons, high nibble of port 5 */
					{
						static int lastval;
	
						temp = (readinputport (0) >> 6) & 3;
						val = readinputport (5) >> 4;
						val |= (readinputport (3) & 0x80) >> 7;	/* player 1 start */
	
						/* bit 0 is a trigger for the 1 player start */
						if ((val & 1) && ((val ^ lastval) & 1))
							if (credits >= credden[temp]) credits -= credden[temp];
						/* bit 1 is a trigger for the 2 player start */
						if ((val & 2) && ((val ^ lastval) & 2))
							if (credits >= 2 * credden[temp]) credits -= 2 * credden[temp];
	
						return lastval = val;
					}
	
					case 2:		/* High BCD of credits */
						temp = (readinputport (0) >> 6) & 3;
						return (credits * crednum[temp] / credden[temp]) / 10;
	
					case 3:		/* Low BCD of credits */
						temp = (readinputport (0) >> 6) & 3;
						return (credits * crednum[temp] / credden[temp]) % 10;
	
					case 4:		/* Player 1 joystick */
						return readinputport (3) & 0x0f;
	
					case 5:		/* Player 1 buttons */
						return readinputport (3) >> 4;
	
					case 6:		/* Player 2 joystick */
						return readinputport (6) & 0x0f;
	
					case 7:		/* Player 2 buttons */
						return readinputport (6) >> 4;
				}
			}
		}
		/* by default, return what was stored there */
		return mappy_customio_1[offset];
	} };
	
	
	public static ReadHandlerPtr todruaga_customio_2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int mode = mappy_customio_2[8];
	
		logerror("%04x: I/O read 2: mode %d, offset %d\n", cpu_get_pc(), mode, offset);
	
		if (io_chip_1_enabled != 0)
		{
			/* mode 4 is the standard, and returns actual important values */
			if (mode == 4)
			{
				switch (offset)
				{
					case 2:		/* DSW0, low nibble */
						return readinputport (0) & 0x0f;
	
					case 4:		/* DSW0, high nibble */
						return readinputport (0) >> 4;
	
					case 5:		/* DSW1, high nibble */
						return readinputport (1) >> 4;
	
					case 6:		/* DSW1, low nibble */
						return readinputport (1) & 0x0f;
	
					case 7:		/* DSW2 - service switch */
						return readinputport (2) & 0x0f;
	
					case 0:		/* read, but unknown */
					case 1:		/* read, but unknown */
					case 3:		/* read, but unknown */
						return 0;
				}
			}
		}
	
		/* by default, return what was stored there */
		return mappy_customio_2[offset];
	} };
	
	
	
	public static WriteHandlerPtr mappy_interrupt_enable_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		interrupt_enable_1 = offset;
	} };
	
	
	
	public static InterruptPtr mappy_interrupt_1 = new InterruptPtr() { public int handler() 
	{
		if (interrupt_enable_1 != 0) return interrupt();
		else return ignore_interrupt();
	} };
	
	
	
	public static WriteHandlerPtr mappy_interrupt_enable_2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		interrupt_enable_2 = offset;
	} };
	
	
	
	public static InterruptPtr mappy_interrupt_2 = new InterruptPtr() { public int handler() 
	{
		if (interrupt_enable_2 != 0) return interrupt();
		else return ignore_interrupt();
	} };
	
	
	public static WriteHandlerPtr mappy_cpu_enable_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		cpu_set_halt_line(1, offset ? CLEAR_LINE : ASSERT_LINE);
	} };
}
