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

public class slapfght
{
	
	
	unsigned char *slapfight_dpram;
	int slapfight_dpram_size;
	
	int slapfight_status;
	int getstar_sequence_index;
	int getstar_sh_intenabled;
	
	static int slapfight_status_state;
	extern unsigned char *getstar_e803;
	
	/* Perform basic machine initialisation */
	
	public static InitMachinePtr slapfight_init_machine = new InitMachinePtr() { public void handler() 
	{
		/* MAIN CPU */
	
		slapfight_status_state=0;
		slapfight_status = 0xc7;
	
		getstar_sequence_index = 0;
		getstar_sh_intenabled = 0;	/* disable sound cpu interrupts */
	
		/* SOUND CPU */
		cpu_set_reset_line(1,ASSERT_LINE);
	} };
	
	/* Interrupt handlers cpu & sound */
	
	public static WriteHandlerPtr slapfight_dpram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	    slapfight_dpram[offset]=data;
	
	//	if (errorlog != 0) fprintf(errorlog,"SLAPFIGHT MAIN  CPU : Write to   $c8%02x = %02x\n",offset,slapfight_dpram[offset]);
	
	
	/*
	
		// Synchronise CPUs
		timer_set(TIME_NOW,0,0);       P'tit Seb 980926 Commented out because it doesn't seem to be necessary
	
		// Now cause the interrupt
	    cpu_cause_interrupt (1, Z80_NMI_INT);
	
	*/
	
	
	    return;
	} };
	
	public static ReadHandlerPtr slapfight_dpram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
	    return slapfight_dpram[offset];
	} };
	
	
	
	/* Slapfight CPU input/output ports
	
	  These ports seem to control memory access
	
	*/
	
	/* Reset and hold sound CPU */
	public static WriteHandlerPtr slapfight_port_00_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		cpu_set_reset_line(1,ASSERT_LINE);
		getstar_sh_intenabled = 0;
	} };
	
	/* Release reset on sound CPU */
	public static WriteHandlerPtr slapfight_port_01_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		cpu_set_reset_line(1,CLEAR_LINE);
	} };
	
	/* Disable and clear hardware interrupt */
	public static WriteHandlerPtr slapfight_port_06_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		interrupt_enable_w(0,0);
	} };
	
	/* Enable hardware interrupt */
	public static WriteHandlerPtr slapfight_port_07_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		interrupt_enable_w(0,1);
	} };
	
	public static WriteHandlerPtr slapfight_port_08_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		unsigned char *RAM = memory_region(REGION_CPU1);
	
		cpu_setbank(1,&RAM[0x10000]);
	} };
	
	public static WriteHandlerPtr slapfight_port_09_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		unsigned char *RAM = memory_region(REGION_CPU1);
	
		cpu_setbank(1,&RAM[0x14000]);
	} };
	
	
	/* Status register */
	
	public static ReadHandlerPtr slapfight_port_00_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		int states[3]={ 0xc7, 0x55, 0x00 };
	
		slapfight_status = states[slapfight_status_state];
	
		slapfight_status_state++;
		if (slapfight_status_state > 2) slapfight_status_state = 0;
	
		return slapfight_status;
	} };
	
	
	
	/*
	 Reads at e803 expect a sequence of values such that:
	 - first value is different from successive
	 - third value is (first+5)^0x56
	 I don't know what writes to this address do (connected to port 0 reads?).
	*/
	public static ReadHandlerPtr getstar_e803_r = new ReadHandlerPtr() { public int handler(int offset)
	{
	unsigned char seq[] = { 0, 1, (0+5)^0x56 };
	unsigned char val;
	
		val = seq[getstar_sequence_index];
		getstar_sequence_index = (getstar_sequence_index+1)%3;
		return val;
	} };
	
	
	
	/* Enable hardware interrupt of sound cpu */
	public static WriteHandlerPtr getstar_sh_intenable_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		getstar_sh_intenabled = 1;
		if (errorlog != 0) fprintf(errorlog,"cpu #1 PC=%d: %d written to a0e0\n",cpu_get_pc(),data);
	} };
	
	
	
	/* Generate interrups only if they have been enabled */
	public static InterruptPtr getstar_interrupt = new InterruptPtr() { public int handler() 
	{
		if (getstar_sh_intenabled != 0)
			return nmi_interrupt();
		else
			return ignore_interrupt();
	} };
	
	public static WriteHandlerPtr getstar_port_04_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	//	cpu_halt(0,0);
	} };
}
