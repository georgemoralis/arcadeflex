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

public class bublbobl
{
	
	
	
	UBytePtr bublbobl_sharedram1,*bublbobl_sharedram2;
	extern int bublbobl_video_enable;
	
	
	public static ReadHandlerPtr bublbobl_sharedram1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return bublbobl_sharedram1[offset];
	} };
	public static ReadHandlerPtr bublbobl_sharedram2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return bublbobl_sharedram2[offset];
	} };
	public static WriteHandlerPtr bublbobl_sharedram1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		bublbobl_sharedram1[offset] = data;
	} };
	public static WriteHandlerPtr bublbobl_sharedram2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		bublbobl_sharedram2[offset] = data;
	} };
	
	
	
	public static WriteHandlerPtr bublbobl_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		UBytePtr ROM = memory_region(REGION_CPU1);
	
		/* bits 0-2 select ROM bank */
		cpu_setbank(1,&ROM[0x10000 + 0x4000 * ((data ^ 4) & 7)]);
	
		/* bit 3 n.c. */
	
		/* bit 4 resets second Z80 */
	
		/* bit 5 resets mcu */
	
		/* bit 6 enables display */
		bublbobl_video_enable = data & 0x40;
	
		/* bit 7 flips screen */
		flip_screen_w(0,data & 0x80);
	} };
	
	public static WriteHandlerPtr tokio_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		UBytePtr ROM = memory_region(REGION_CPU1);
	
		/* bits 0-2 select ROM bank */
		cpu_setbank(1,&ROM[0x10000 + 0x4000 * (data & 7)]);
	
		/* bits 3-7 unknown */
	} };
	
	public static WriteHandlerPtr tokio_videoctrl_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* bit 7 flips screen */
		flip_screen_w(0,data & 0x80);
	
		/* other bits unknown */
	} };
	
	public static WriteHandlerPtr bublbobl_nmitrigger_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		cpu_cause_interrupt(1,Z80_NMI_INT);
	} };
	
	public static ReadHandlerPtr tokio_fake_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	  return 0xbf; /* ad-hoc value set to pass initial testing */
	} };
	
	
	
	static int sound_nmi_enable,pending_nmi;
	
	public static timer_callback nmi_callback = new timer_callback() { public void handler(int param) 
	{
		if (sound_nmi_enable != 0) cpu_cause_interrupt(2,Z80_NMI_INT);
		else pending_nmi = 1;
	} };
	
	public static WriteHandlerPtr bublbobl_sound_command_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		soundlatch_w.handler(offset,data);
		timer_set(TIME_NOW,data,nmi_callback);
	} };
	
	public static WriteHandlerPtr bublbobl_sh_nmi_disable_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		sound_nmi_enable = 0;
	} };
	
	public static WriteHandlerPtr bublbobl_sh_nmi_enable_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		sound_nmi_enable = 1;
		if (pending_nmi != 0)
		{
			cpu_cause_interrupt(2,Z80_NMI_INT);
			pending_nmi = 0;
		}
	} };
	
	
	
	/***************************************************************************
	
	 Bubble Bobble 68705 protection interface
	
	 The following is ENTIRELY GUESSWORK!!!
	
	***************************************************************************/
	public static InterruptPtr bublbobl_m68705_interrupt = new InterruptPtr() { public int handler() 
	{
		/* I don't know how to handle the interrupt line so I just toggle it every time. */
		if (cpu_getiloops() & 1)
			cpu_set_irq_line(3,0,CLEAR_LINE);
		else
			cpu_set_irq_line(3,0,ASSERT_LINE);
	
	    return 0;
	} };
	
	
	
	static unsigned char portA_in,portA_out,ddrA;
	
	public static ReadHandlerPtr bublbobl_68705_portA_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	//logerror("%04x: 68705 port A read %02x\n",cpu_get_pc(),portA_in);
		return (portA_out & ddrA) | (portA_in & ~ddrA);
	} };
	
	public static WriteHandlerPtr bublbobl_68705_portA_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	//logerror("%04x: 68705 port A write %02x\n",cpu_get_pc(),data);
		portA_out = data;
	} };
	
	public static WriteHandlerPtr bublbobl_68705_ddrA_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		ddrA = data;
	} };
	
	
	
	/*
	 *  Port B connections:
	 *
	 *  all bits are logical 1 when read (+5V pullup)
	 *
	 *  0   W  enables latch which holds data from main Z80 memory
	 *  1   W  loads the latch which holds the low 8 bits of the address of
	 *               the main Z80 memory location to access
	 *  2   W  loads the latch which holds the high 4 bits of the address of
	 *               the main Z80 memory location to access
	 *         00-07 = read input ports
	 *         0c-0f = access z80 memory at 0xfc00
	 *  3   W  selects Z80 memory access direction (0 = write 1 = read)
	 *  4   W  clocks main Z80 memory access (goes to a PAL)
	 *  5   W  clocks a flip-flop which causes IRQ on the main Z80
	 *  6   W  not used?
	 *  7   W  not used?
	 */
	
	static unsigned char portB_in,portB_out,ddrB;
	
	public static ReadHandlerPtr bublbobl_68705_portB_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return (portB_out & ddrB) | (portB_in & ~ddrB);
	} };
	
	static int address,latch;
	
	public static WriteHandlerPtr bublbobl_68705_portB_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	//logerror("%04x: 68705 port B write %02x\n",cpu_get_pc(),data);
	
		if ((ddrB & 0x01) && (~data & 0x01) && (portB_out & 0x01))
		{
			portA_in = latch;
		}
		if ((ddrB & 0x02) && (data & 0x02) && (~portB_out & 0x02)) /* positive edge trigger */
		{
			address = (address & 0xff00) | portA_out;
	//logerror("%04x: 68705 address %02x\n",cpu_get_pc(),portA_out);
		}
		if ((ddrB & 0x04) && (data & 0x04) && (~portB_out & 0x04)) /* positive edge trigger */
		{
			address = (address & 0x00ff) | ((portA_out & 0x0f) << 8);
		}
		if ((ddrB & 0x10) && (~data & 0x10) && (portB_out & 0x10))
		{
			if ((data & 0x08) != 0)	/* read */
			{
				if ((address & 0x0800) == 0x0000)
				{
	//logerror("%04x: 68705 read input port %02x\n",cpu_get_pc(),address);
					latch = readinputport((address & 3) + 1);
				}
				else if ((address & 0x0c00) == 0x0c00)
				{
	//logerror("%04x: 68705 read %02x from address %04x\n",cpu_get_pc(),bublbobl_sharedram2[address],address);
					latch = bublbobl_sharedram2[address & 0x03ff];
				}
				else
	logerror("%04x: 68705 unknown read address %04x\n",cpu_get_pc(),address);
			}
			else	/* write */
			{
				if ((address & 0x0c00) == 0x0c00)
				{
	//logerror("%04x: 68705 write %02x to address %04x\n",cpu_get_pc(),portA_out,address);
					bublbobl_sharedram2[address & 0x03ff] = portA_out;
				}
				else
	logerror("%04x: 68705 unknown write to address %04x\n",cpu_get_pc(),address);
			}
		}
		if ((ddrB & 0x20) && (~data & 0x20) && (portB_out & 0x20))
		{
			/* hack to get random EXTEND letters (who is supposed to do this? 68705? PAL?) */
			bublbobl_sharedram2[0x7c] = rand()%6;
	
			cpu_irq_line_vector_w(0,0,bublbobl_sharedram2[0]);
			cpu_set_irq_line(0,0,HOLD_LINE);
		}
		if ((ddrB & 0x40) && (~data & 0x40) && (portB_out & 0x40))
		{
	logerror("%04x: 68705 unknown port B bit %02x\n",cpu_get_pc(),data);
		}
		if ((ddrB & 0x80) && (~data & 0x80) && (portB_out & 0x80))
		{
	logerror("%04x: 68705 unknown port B bit %02x\n",cpu_get_pc(),data);
		}
	
		portB_out = data;
	} };
	
	public static WriteHandlerPtr bublbobl_68705_ddrB_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		ddrB = data;
	} };
}
