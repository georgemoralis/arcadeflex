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

public class spiders
{
	
	
	
	/* Declare inividual irq handlers, may be able to combine later */
	/* Main CPU */
	void spiders_irq0a(int state) { cpu_set_irq_line(0,M6809_IRQ_LINE,state ? ASSERT_LINE : CLEAR_LINE); }
	void spiders_irq0b(int state) { cpu_set_irq_line(0,M6809_IRQ_LINE,state ? ASSERT_LINE : CLEAR_LINE); }
	void spiders_irq1a(int state) { cpu_set_irq_line(0,M6809_IRQ_LINE,state ? ASSERT_LINE : CLEAR_LINE); }
	void spiders_irq1b(int state) { cpu_set_irq_line(0,M6809_IRQ_LINE,state ? ASSERT_LINE : CLEAR_LINE); }
	void spiders_irq2a(int state) { cpu_set_irq_line(0,M6809_IRQ_LINE,state ? ASSERT_LINE : CLEAR_LINE); }
	void spiders_irq2b(int state) { cpu_set_irq_line(0,M6809_IRQ_LINE,state ? ASSERT_LINE : CLEAR_LINE); }
	
	/* Sound CPU */
	void spiders_irq3a(int state) { }
	void spiders_irq3b(int state) { }
	
	/* Function prototypes */
	
	public static WriteHandlerPtr spiders_flip_w = new WriteHandlerPtr() { public void handler(int address, int data);
	public static WriteHandlerPtr spiders_vrif_w = new WriteHandlerPtr() { public void handler(int address, int data);
	public static ReadHandlerPtr spiders_vrom_r = new ReadHandlerPtr() { public int handler(int address);
	
	
	/* Declare PIA structure */
	
	/* PIA 0, main CPU */
	static struct pia6821_interface pia_0_intf =
	{
		/*inputs : A/B,CA/B1,CA/B2 */ input_port_0_r, input_port_1_r, 0, 0, 0, 0,
		/*outputs: A/B,CA/B2       */ 0, 0, 0, 0,
		/*irqs   : A/B             */ spiders_irq0a, spiders_irq0b
	} };;
	
	/* PIA 1, main CPU */
	static struct pia6821_interface pia_1_intf =
	{
		/*inputs : A/B,CA/B1,CA/B2 */ spiders_vrom_r, 0, input_port_5_r, 0, 0, 0,
		/*outputs: A/B,CA/B2       */ 0, spiders_vrif_w, 0, spiders_flip_w,
		/*irqs   : A/B             */ spiders_irq1a, spiders_irq1b
	};
	
	/* PIA 2, main CPU */
	static struct pia6821_interface pia_2_intf =
	{
		/*inputs : A/B,CA/B1,CA/B2 */ 0, 0, 0, 0, 0, 0,
		/*outputs: A/B,CA/B2       */ 0, 0, 0, 0,
		/*irqs   : A/B             */ spiders_irq2a, spiders_irq2b
	};
	
	/* PIA 3, sound CPU */
	static struct pia6821_interface pia_3_intf =
	{
		/*inputs : A/B,CA/B1,CA/B2 */ 0, 0, 0, 0, 0, 0,
		/*outputs: A/B,CA/B2       */ 0, 0, 0, 0,
		/*irqs   : A/B             */ spiders_irq3a, spiders_irq3b
	};
	
	
	/***************************************************************************
	
	    Spiders Machine initialisation
	
	***************************************************************************/
	
	public static InitMachinePtr spiders_init_machine = new InitMachinePtr() { public void handler() 
	{
		pia_unconfig();
		pia_config(0, PIA_STANDARD_ORDERING  | PIA_8BIT, &pia_0_intf);
		pia_config(1, PIA_ALTERNATE_ORDERING | PIA_8BIT, &pia_1_intf);
		pia_config(2, PIA_STANDARD_ORDERING  | PIA_8BIT, &pia_2_intf);
		pia_config(3, PIA_STANDARD_ORDERING  | PIA_8BIT, &pia_3_intf);
		pia_reset();
	} };
	
	
	
	/***************************************************************************
	
	    Timed interrupt handler - Updated PIA input ports
	
	***************************************************************************/
	
	int spiders_timed_irq(void)
	{
		/* Update CA1 on PIA1 - copy of PA0 (COIN1?) */
		pia_0_ca1_w(0 , input_port_0_r(0)&0x01);
	
		/* Update CA2 on PIA1 - copy of PA0 (PS2) */
		pia_0_ca2_w(0 , input_port_0_r(0)&0x02);
	
		/* Update CA1 on PIA1 - copy of PA0 (COIN1?) */
		pia_0_cb1_w(0 , input_port_6_r(0));
	
		/* Update CB2 on PIA1 - NOT CONNECTED */
	
		return ignore_interrupt();
	}
	
	
	/***************************************************************************
	
	    Video access port definition (On PIA 2)
	
	    Bit 7 6 5 4 3 2 1 0
	        X                Mode Setup/Read 1/0
	            X X          Latch select (see below)
	                X X X X  Data nibble
	
	    When in setup mode data is clocked into the latch by a read from port a.
	    When in read mode the read from port a auto increments the address.
	
	    Latch 0 - Low byte low nibble
	          1 - Low byte high nibble
	          2 - High order low nibble
	          3 - High order high nibble
	
	***************************************************************************/
	
	static int vrom_address;
	static int vrom_ctrl_mode;
	static int vrom_ctrl_latch;
	static int vrom_ctrl_data;
	
	int spiders_video_flip=0;
	
	public static WriteHandlerPtr spiders_flip_w = new WriteHandlerPtr() { public void handler(int address, int data)
	{
		spiders_video_flip=data;
	} };
	
	public static WriteHandlerPtr spiders_vrif_w = new WriteHandlerPtr() { public void handler(int address, int data)
	{
		vrom_ctrl_mode=(data&0x80)>>7;
		vrom_ctrl_latch=(data&0x30)>>4;
		vrom_ctrl_data=15-(data&0x0f);
	} };
	
	public static ReadHandlerPtr spiders_vrom_r = new ReadHandlerPtr() { public int handler(int address)
	{
		int retval;
		unsigned char *RAM = memory_region(REGION_GFX1);
	
		if (vrom_ctrl_mode != 0)
		{
			retval=RAM[vrom_address];
	//	        if (errorlog != 0) fprintf(errorlog,"VIDEO : Read data %02x from Port address %04x\n",retval,vrom_address);
			vrom_address++;
		}
		else
		{
			switch(vrom_ctrl_latch)
			{
				case 0:
					vrom_address&=0xfff0;
					vrom_address|=vrom_ctrl_data;
					break;
				case 1:
					vrom_address&=0xff0f;
					vrom_address|=vrom_ctrl_data<<4;
					break;
				case 2:
					vrom_address&=0xf0ff;
					vrom_address|=vrom_ctrl_data<<8;
					break;
				case 3:
					vrom_address&=0x0fff;
					vrom_address|=vrom_ctrl_data<<12;
					break;
				default:
					break;
			}
			retval=0;
	//	        if (errorlog != 0) fprintf(errorlog,"VIDEO : Port address set to %04x\n",vrom_address);
		}
		return retval;
	} };
	
}
