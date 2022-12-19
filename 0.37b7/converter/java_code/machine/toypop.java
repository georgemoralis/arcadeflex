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

public class toypop
{
	
	UBytePtr toypop_sharedram_1, *toypop_sharedram_2, *toypop_customio, *toypop_speedup;
	static unsigned char interrupt_enable_68k;
	
	public static InitMachinePtr toypop_init_machine = new InitMachinePtr() { public void handler() 
	{
		interrupt_enable_68k = 0;
	} };
	
	public static ReadHandlerPtr toypop_cycle_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		/* to speed up emulation, we check for the loop the 68000 CPU sits in almost all of the time
		   and end the current iteration (things will start going again with the next IRQ) */
		if (READ_WORD(&toypop_speedup[0]) == 0 && cpu_get_pc() == 0x11c) cpu_spinuntil_int();
	
		return READ_WORD(&toypop_speedup[0]);
	} };
	
	public static ReadHandlerPtr toypop_sharedram_1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		/* to speed up emulation, we check for the loop the sound CPU sits in most of the time
		   and end the current iteration (things will start going again with the next IRQ) */
		if (offset == 0xa1 - 0x40 && toypop_sharedram_1[offset] == 0 && cpu_get_pc() == 0xe4df)
			cpu_spinuntil_int();
		return toypop_sharedram_1[offset];
	} };
	
	public static WriteHandlerPtr toypop_sharedram_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		toypop_sharedram_1[offset] = data;
	} };
	
	public static ReadHandlerPtr toypop_sharedram_2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return toypop_sharedram_2[offset >> 1];
	} };
	
	public static WriteHandlerPtr toypop_sharedram_2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		toypop_sharedram_2[offset >> 1] = data & 0xff;
	} };
	
	public static WriteHandlerPtr toypop_interrupt_enable_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		interrupt_enable_68k = 1;
	} };
	
	public static WriteHandlerPtr toypop_interrupt_disable_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		interrupt_enable_68k = 0;
	} };
	
	public static InterruptPtr toypop_interrupt = new InterruptPtr() { public int handler() 
	{
		if (interrupt_enable_68k != 0)
			// not sure whether the cause of the interrupt is a VBLANK or the main CPU.
			// Anyway, this works
			return 1;
		else
			return ignore_interrupt();
	} };
	
	public static ReadHandlerPtr toypop_customio_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int mode = toypop_customio[8];
	
		/* mode 5 values are actually checked against these numbers during power up */
		if (mode == 5)
			switch (offset) {
				case 2:
					return 15;
				case 6:
					return 12;
				case 16:
					return 6;
				case 17:
					return 9;
				case 32:
					return 6;
				case 33:
					return 9;
				default:
					return toypop_customio[offset];
			}
		else
			switch (offset) {
				case 4:
					return readinputport(0) & 0x0f;
				case 5:
					return readinputport(0) >> 4;
				case 6:
					return readinputport(1) & 0x0f;
				case 7:
					return readinputport(1) >> 4;
				case 16:
					return readinputport(2) & 0x0f;
				case 17:
					return readinputport(2) >> 4;
				case 18:
					return readinputport(3) & 0x0f;
				case 19:
					return readinputport(3) >> 4;
				default:
					return toypop_customio[offset];
			}
	} };
	
	public static WriteHandlerPtr toypop_cpu_reset_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		// this makes the service mode work
		cpu_set_reset_line(1, PULSE_LINE);
	} };
}
