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

public class capbowl
{
	
	static int currentaddress = 0;
	static int GRHighByte = 0;
	static int GRMidByte  = 0;
	static int GRLowByte = 0;
	
	public static InitMachinePtr capbowl_init_machine = new InitMachinePtr() { public void handler() 
	{
		/* Initialize the ticket dispenser to 100 milliseconds */
		/* (I'm not sure what the correct value really is) */
		ticket_dispenser_init(100, TICKET_MOTOR_ACTIVE_HIGH, TICKET_STATUS_ACTIVE_LOW);
	} };
	
	
	public static WriteHandlerPtr capbowl_rom_select_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int bankaddress = 0x10000 + ((data & 0x0c) << 13) + ((data & 0x01) << 14);
		UBytePtr RAM = memory_region(REGION_CPU1);
	
	
		cpu_setbank(1,&RAM[bankaddress]);
	} };
	
	
	/*
		Write to GR Address upper word (2 bits)
	*/
	public static WriteHandlerPtr bowlrama_turbo_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		switch( offset )
		{
			case 0x08:	  /* Write address high byte (only 2 bits used) */
				GRHighByte = data;
				break;
	
			case 0x17:    /* Write address mid byte (8 bits)   */
				GRMidByte = data;
				break;
	
			case 0x18:	  /* Write Address low byte (8 bits)   */
				GRLowByte = data;
				break;
	
			default:
				logerror("PC=%04X Write to unsupported Turbo address %02X Data=%02X\n",cpu_get_pc(),offset, data);
		}
	
		currentaddress = ((GRHighByte << 16) | (GRMidByte << 8) | GRLowByte);
	} };
	
	
	public static ReadHandlerPtr bowlrama_turbo_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int ret = 0;
		int data = memory_region(REGION_GFX1)[currentaddress];
	
		switch (offset)
		{
		case 0:	/* Read Mask */
	
			/*  Graphics data are 4bpp (2 pixels per byte).
				This function returns 0's for new pixel data.
				This allows data to be read as a mask, AND the mask with
				the screen data, then OR new data read by read data command.
			*/
	
			if(!(data & 0xf0))
			{
				ret = 0xf0;  /* High nibble is transparent */
			}
	
			if(!(data & 0x0f))
			{
				ret |= 0x0f;  /* Low nibble is transparent */
			}
	
			break;
	
		case 4: /* Read data and increment address */
	
			ret	= data;
	
			currentaddress = (currentaddress + 1) & 0x3ffff;
	
			GRHighByte = (currentaddress >> 16);
			GRMidByte  = (currentaddress >> 8) & 0xff;
			GRLowByte  = (currentaddress & 0xff);
	
			break;
	
		default:
			logerror("PC=%04X Read from unsupported Turbo address %02X\n",cpu_get_pc(),offset);
		}
	
		return ret;
	} };
}
